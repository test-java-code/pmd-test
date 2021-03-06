package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.AbstractTest;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class Issue257 extends AbstractTest {

    private TypeSolver typeSolver;

    @Before
    public void setup() throws IOException {
        Path pathToJar = adaptPath("src/test/resources/issue257/issue257.jar");
        typeSolver = new CombinedTypeSolver(new JarTypeSolver(pathToJar), new ReflectionTypeSolver());
    }

    @Test
    public void verifyBCanBeSolved() {
        typeSolver.solveType("net.testbug.B");
    }

    @Test
    public void issue257() throws IOException {
        Path pathToSourceFile = adaptPath("src/test/resources/issue257/A.java.txt");
        CompilationUnit cu = JavaParser.parse(pathToSourceFile);
        Statement statement = cu.getClassByName("A").get().getMethodsByName("run").get(0).getBody().get().getStatement(0);
        ExpressionStmt expressionStmt = (ExpressionStmt)statement;
        Expression expression = expressionStmt.getExpression();
        JavaParserFacade.get(typeSolver).getType(expression);
    }

}
