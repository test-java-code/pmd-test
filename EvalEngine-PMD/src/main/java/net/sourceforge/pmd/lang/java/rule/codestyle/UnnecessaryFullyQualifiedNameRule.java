/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.SourceFileScope;

public class UnnecessaryFullyQualifiedNameRule extends AbstractJavaRule {

    private List<ASTImportDeclaration> imports = new ArrayList<>();

    public UnnecessaryFullyQualifiedNameRule() {
        super.addRuleChainVisit(ASTCompilationUnit.class);
        super.addRuleChainVisit(ASTImportDeclaration.class);
        super.addRuleChainVisit(ASTClassOrInterfaceType.class);
        super.addRuleChainVisit(ASTName.class);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        imports.clear();
        return data;
    }

    @Override
    public Object visit(ASTImportDeclaration node, Object data) {
        imports.add(node);
        return data;
    }

    @Override
    public Object visit(ASTClassOrInterfaceType node, Object data) {
        // This name has no qualification, it can't be unnecessarily qualified
        if (node.getImage().indexOf('.') < 0) {
            return data;
        }
        checkImports(node, data);
        return data;
    }

    @Override
    public Object visit(ASTName node, Object data) {
        if (!(node.jjtGetParent() instanceof ASTImportDeclaration)
                && !(node.jjtGetParent() instanceof ASTPackageDeclaration)) {
            // This name has no qualification, it can't be unnecessarily qualified
            if (node.getImage().indexOf('.') < 0) {
                return data;
            }
            checkImports(node, data);
        }
        return data;
    }


    /**
     * Returns true if the name could be imported by this declaration.
     * The name must be fully qualified, the import is either on-demand
     * or static, that is its {@link ASTImportDeclaration#getImportedName()}
     * is the enclosing package or type name of the imported type or static member.
     */
    private boolean declarationMatches(ASTImportDeclaration decl, String name) {
        return name.startsWith(decl.getImportedName())
                && name.lastIndexOf('.') == decl.getImportedName().length();
    }

    private void checkImports(JavaNode node, Object data) {
        String name = node.getImage();
        List<ASTImportDeclaration> matches = new ArrayList<>();

        // Find all "matching" import declarations
        for (ASTImportDeclaration importDeclaration : imports) {
            if (!importDeclaration.isImportOnDemand()) {
                // Exact match of imported class
                if (name.equals(importDeclaration.getImportedName())) {
                    matches.add(importDeclaration);
                    continue;
                }
            }
            // On demand import exactly matches the package of the type
            // Or match of static method call on imported class
            if (declarationMatches(importDeclaration, name)) {
                matches.add(importDeclaration);
            }
        }

        // If there is no direct match, consider if we match the tail end of a
        // direct static import, but also a static method on a class import?
        // For example:
        //
        // import java.util.Arrays;
        // import static java.util.Arrays.asList;
        // static {
        // List list1 = Arrays.asList("foo"); // Array class name not needed!
        // List list2 = asList("foo"); // Preferred, used static import
        // }
        if (matches.isEmpty()) {
            for (ASTImportDeclaration importDeclaration : imports) {
                if (importDeclaration.isStatic()) {
                    String[] importParts = importDeclaration.getImportedName().split("\\.");
                    String[] nameParts = name.split("\\.");
                    if (importDeclaration.isImportOnDemand()) {
                        // Name class part matches class part of static import?
                        if (nameParts[nameParts.length - 2].equals(importParts[importParts.length - 1])) {
                            matches.add(importDeclaration);
                        }
                    } else {
                        // Last 2 parts match?
                        if (nameParts[nameParts.length - 1].equals(importParts[importParts.length - 1])
                                && nameParts[nameParts.length - 2].equals(importParts[importParts.length - 2])) {
                            matches.add(importDeclaration);
                        }
                    }
                }
            }
        }

        if (!matches.isEmpty()) {
            ASTImportDeclaration firstMatch = matches.get(0);

            // Could this done to avoid a conflict?
            if (!isAvoidingConflict(node, name, firstMatch)) {
                String importStr = firstMatch.getImportedName() + (firstMatch.isImportOnDemand() ? ".*" : "");
                String type = firstMatch.isStatic() ? "static " : "";

                addViolation(data, node, new Object[] { node.getImage(), importStr, type });
            }
        }
    }

    private boolean isAvoidingConflict(final JavaNode node, final String name,
            final ASTImportDeclaration firstMatch) {
        // is it a conflict between different imports?
        if (firstMatch.isImportOnDemand() && firstMatch.isStatic()) {
            final String methodCalled = name.substring(name.indexOf('.') + 1);

            // Is there any other static import conflictive?
            for (final ASTImportDeclaration importDeclaration : imports) {
                if (!Objects.equals(importDeclaration, firstMatch) && importDeclaration.isStatic()) {
                    if (declarationMatches(firstMatch, importDeclaration.getImportedName())) {
                        // A conflict against the same class is not an excuse,
                        // ie:
                        // import java.util.Arrays;
                        // import static java.util.Arrays.asList;
                        continue;
                    }

                    if (importDeclaration.isImportOnDemand()) {
                        // We need type resolution to make sure there is a
                        // conflicting method
                        if (importDeclaration.getType() != null) {
                            for (final Method m : importDeclaration.getType().getMethods()) {
                                if (m.getName().equals(methodCalled)) {
                                    return true;
                                }
                            }
                        }
                    } else if (importDeclaration.getImportedName().endsWith(methodCalled)) {
                        return true;
                    }
                }
            }
        }

        final String unqualifiedName = name.substring(name.lastIndexOf('.') + 1);
        final int unqualifiedNameLength = unqualifiedName.length();

        // There could be a conflict between an import on demand and another import, e.g.
        // import One.*;
        // import Two.Problem;
        // Where One.Problem is a legitimate qualification
        if (firstMatch.isImportOnDemand() && !firstMatch.isStatic()) {
            for (ASTImportDeclaration importDeclaration : imports) {
                if (importDeclaration != firstMatch     // NOPMD
                        && !importDeclaration.isStatic()
                        && !importDeclaration.isImportOnDemand()) {

                    // Duplicate imports are legal
                    if (!importDeclaration.getPackageName().equals(firstMatch.getPackageName())
                            && importDeclaration.getImportedSimpleName().equals(unqualifiedName)) {
                        return true;
                    }
                }
            }
        }

        // Is it a conflict with a class in the same file?
        final Set<String> qualifiedTypes = node.getScope().getEnclosingScope(SourceFileScope.class)
                                               .getQualifiedTypeNames().keySet();
        for (final String qualified : qualifiedTypes) {
            int fullLength = qualified.length();
            if (qualified.endsWith(unqualifiedName)
                    && (fullLength == unqualifiedNameLength || qualified.charAt(fullLength - unqualifiedNameLength - 1) == '.')) {
                return true;
            }
        }

        return false;
    }
}
