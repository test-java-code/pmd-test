/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import net.sourceforge.pmd.lang.java.ast.ASTConditionalAndExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalOrExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserDecoratedVisitor;
import net.sourceforge.pmd.lang.java.ast.MethodLikeNode;
import net.sourceforge.pmd.lang.java.metrics.impl.visitors.CycloAssertAwareDecorator;
import net.sourceforge.pmd.lang.java.metrics.impl.visitors.CycloBaseVisitor;
import net.sourceforge.pmd.lang.java.metrics.impl.visitors.CycloPathAwareDecorator;
import net.sourceforge.pmd.lang.metrics.MetricOption;
import net.sourceforge.pmd.lang.metrics.MetricOptions;


/**
 * Cyclomatic Complexity. See the <a href="https://pmd.github.io/latest/pmd_java_metrics_index.html">documentation site</a>.
 *
 * @author Clément Fournier
 * @since June 2017
 */
public final class CycloMetric extends AbstractJavaOperationMetric {


    // TODO:cf Cyclo should develop factorized boolean operators to count them


    @Override
    public double computeFor(final MethodLikeNode node, MetricOptions options) {
        Set<MetricOption> opts = options.getOptions();
        JavaParserDecoratedVisitor visitor = new JavaParserDecoratedVisitor(CycloBaseVisitor.INSTANCE) { // TODO decorators are unmaintainable, change that someday
            // stops the visit when stumbling on a lambda or class decl
            @Override
            public Object visit(JavaNode localNode, Object data) {
                return localNode.isFindBoundary() && !localNode.equals(node) ? data : super.visit(localNode, data); // TODO generalize that to other metrics
            }
        };

        if (opts.contains(CycloOption.CONSIDER_ASSERT)) {
            visitor.decorateWith(new CycloAssertAwareDecorator());
        }

        if (!opts.contains(CycloOption.IGNORE_BOOLEAN_PATHS)) {
            visitor.decorateWith(new CycloPathAwareDecorator());
        }

        MutableInt cyclo = (MutableInt) node.jjtAccept(visitor, new MutableInt(1));
        return (double) cyclo.getValue();
    }


    /**
     * Evaluates the number of paths through a boolean expression. This is the total number of {@code &&} and {@code ||}
     * operators appearing in the expression. This is used in the calculation of cyclomatic and n-path complexity.
     *
     * @param expr Expression to analyse
     *
     * @return The number of paths through the expression
     */
    public static int booleanExpressionComplexity(ASTExpression expr) {
        if (expr == null) {
            return 0;
        }

        List<ASTConditionalAndExpression> andNodes = expr.findDescendantsOfType(ASTConditionalAndExpression.class);
        List<ASTConditionalOrExpression> orNodes = expr.findDescendantsOfType(ASTConditionalOrExpression.class);

        int complexity = 0;

        for (ASTConditionalOrExpression element : orNodes) {
            complexity += element.jjtGetNumChildren() - 1;
        }

        for (ASTConditionalAndExpression element : andNodes) {
            complexity += element.jjtGetNumChildren() - 1;
        }

        return complexity;
    }


    /** Options for CYCLO. */
    public enum CycloOption implements MetricOption {
        /** Do not count the paths in boolean expressions as decision points. */
        IGNORE_BOOLEAN_PATHS("ignoreBooleanPaths"),
        /** Consider assert statements. */
        CONSIDER_ASSERT("considerAssert");

        private final String vName;


        CycloOption(String valueName) {
            this.vName = valueName;
        }


        @Override
        public String valueName() {
            return vName;
        }
    }

}
