package org.brunel.fyp.langserver.refactorings;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;

import org.brunel.fyp.langserver.MJGARefactoringPattern;
import org.brunel.fyp.langserver.MJGAWorkspaceService;

public class ForEachRefactoringPattern extends MJGARefactoringPattern {

    @Override
    public CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ForEachStmt forEachStmt = (ForEachStmt) node;
        
        ExpressionStmt eStmt = new ExpressionStmt();
        
        NameExpr iterableExpression = forEachStmt.getIterable().asNameExpr();

        VariableDeclarator arrayDeclarationExpr = MJGAWorkspaceService.variableDeclarationExprs
            .stream()
            .filter(variableDeclarationExpr -> variableDeclarationExpr.getName().equals(iterableExpression.getName()))
            .findFirst()
            .get();

        // Arrays.stream(name) or name.stream()
        MethodCallExpr methodCallExprArrays = new MethodCallExpr();
        // stream
        SimpleName simpleNameStream = new SimpleName("stream");

        // Is this an array, e.g. String[]?
        Type arrayDeclarationType = arrayDeclarationExpr.getType();
        NameExpr nameExprArrays;
        if (arrayDeclarationType.isArrayType()) {
            compilationUnit.addImport(new ImportDeclaration("java.util.Arrays", false, false));
            nameExprArrays = new NameExpr(new SimpleName("Arrays"));

            // Name of the array to loop, argument for Arrays.stream()
            methodCallExprArrays.setArguments(new NodeList<Expression>(iterableExpression));
        } else {
            nameExprArrays = iterableExpression;
        }

        nameExprArrays.setParentNode(methodCallExprArrays);
        methodCallExprArrays.setScope(nameExprArrays);
        methodCallExprArrays.setName(simpleNameStream);

        // Arrays.stream(name), forEach, "string -> {
        MethodCallExpr methodCallExpr = new MethodCallExpr();

        // forEach
        methodCallExpr.setName(new SimpleName("forEach"));

        // string
        Parameter parameterString = new Parameter(new UnknownType(), forEachStmt.getVariableDeclarator().getName());
        // "string -> {
        LambdaExpr lambdaExpr = new LambdaExpr(parameterString, (BlockStmt) forEachStmt.getBody());

        // Arrays.streams(name), "string ->
        methodCallExpr.setArguments(new NodeList<Expression>(lambdaExpr));

        methodCallExpr.setScope(methodCallExprArrays);

        eStmt.setExpression(methodCallExpr);

        forEachStmt.replace(eStmt);

        return compilationUnit;
    }
    
}
