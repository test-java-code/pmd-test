/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTDoStatement.java */

package net.sourceforge.pmd.lang.java.ast;

public class ASTDoStatement extends AbstractJavaNode {
    public ASTDoStatement(int id) {
        super(id);
    }

    public ASTDoStatement(JavaParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
