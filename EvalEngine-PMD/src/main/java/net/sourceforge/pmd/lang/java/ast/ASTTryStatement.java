/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTTryStatement.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.List;


/**
 * Try statement node.
 * <pre>
 * TryStatement ::= "try" ( ResourceSpecification )? Block ( CatchStatement )* [ FinallyStatement ]
 * </pre>
 */
public class ASTTryStatement extends AbstractJavaNode {

    public ASTTryStatement(int id) {
        super(id);
    }

    public ASTTryStatement(JavaParser p, int id) {
        super(p, id);
    }


    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    /**
     * Returns true if this node is a try-with-resources, in which case it
     * has a ResourceSpecification child node.
     */
    public boolean isTryWithResources() {
        return getFirstChildOfType(ASTResourceSpecification.class) != null;
    }


    /**
     * Returns the catch statement nodes of this try statement.
     * If there are none, returns an empty list.
     */
    public List<ASTCatchStatement> getCatchStatements() {
        return findChildrenOfType(ASTCatchStatement.class);
    }


    /**
     * Returns true if this try statement has a  {@code finally} statement,
     * in which case {@link #getFinally()} won't return {@code null}.
     */
    public boolean hasFinally() {
        return getFirstChildOfType(ASTFinallyStatement.class) != null;
    }


    /**
     * Returns the {@code finally} statement of this try statement, if any.
     *
     * @return The finally statement, or null if there is none
     */
    public ASTFinallyStatement getFinally() {
        return getFirstChildOfType(ASTFinallyStatement.class);
    }

}
