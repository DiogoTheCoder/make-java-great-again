package org.brunel.fyp.langserver.refactorings;

import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.UnaryExpr.Operator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.UnknownType;

import org.brunel.fyp.langserver.MJGARefactoringPattern;

public class ForLoopRefactoringPattern extends MJGARefactoringPattern {

    @Override
    public CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ExpressionStmt expression = convertToMap((ForStmt) node, compilationUnit);
        if (expression != null) {
            node.replace(expression);
        }

        expression = convertToForEach((ForStmt) node, compilationUnit);
        if (expression != null) {
            node.replace(expression);
        }

        return compilationUnit;
    }

    private static ExpressionStmt convertToMap(ForStmt forStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Are we looping the entire array?
        Optional<MethodCallExpr> arraySizeCallOptional = forStmt.findFirst(MethodCallExpr.class);
        if (arraySizeCallOptional.isEmpty()) {
            // Not a list, could be an array instead
            return null;
        }

        MethodCallExpr arraySizeCall = arraySizeCallOptional.get().asMethodCallExpr();
        if (!arraySizeCall.getName().toString().equals("size")) {
            // Not looping through the entire array
            return null;
        }

        Optional<NameExpr> arrayNameOptional = arraySizeCall.findFirst(NameExpr.class);
        if (arrayNameOptional.isEmpty()) {
            return null;
        }


        NodeList<Statement> bodyExpression = forStmt.getBody().asBlockStmt().getStatements();
        if (bodyExpression.size() == 1 && bodyExpression.get(0).isExpressionStmt()) {
            Optional<Node> expressionStmtMethodCallOptional = bodyExpression
                .get(0)
                .getChildNodes()
                .stream()
                .filter(expression -> expression.getClass().equals(MethodCallExpr.class))
                .findFirst();
            if (expressionStmtMethodCallOptional.isEmpty()) {
                return null;
            }

            MethodCallExpr expressionStmtMethodCall = (MethodCallExpr) expressionStmtMethodCallOptional.get();
            if (!expressionStmtMethodCall.getName().toString().equals("set")) {
                // We ain't trying to set anything, don't use map therefore
                return null;
            }

            String mapFunction = "";

            // Build something like this: "array = array.stream().map(String::toUpperCase).collect(Collectors.toList())"
            if (expressionStmtMethodCall.toString().contains(".toUpperCase()")) {
                // We are trying to uppercase the elements of the list
                mapFunction = "String::toUpperCase";
            } else if (expressionStmtMethodCall.toString().contains(".toLowerCase()")) {
                // We are trying to lowercase the elements of the list
                mapFunction = "String::toLowerCase";
            }

            String template = String.format("%s = %s.stream().map(%s).collect(Collectors.toList())",
                arrayNameOptional.get().toString(),
                arrayNameOptional.get().toString(),
                mapFunction
            );

            Expression templateExpression = StaticJavaParser.parseExpression(template);
            replacingExpressionStmt.setExpression(templateExpression);

            compilationUnit.addImport(java.util.stream.Collectors.class);
        }

        return replacingExpressionStmt;
    }

    private static ExpressionStmt convertToForEach(ForStmt forStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Get the starting index
        VariableDeclarationExpr initialisationVariableDeclarationExpr = forStmt.getInitialization().getFirst().get().asVariableDeclarationExpr();
        Optional<VariableDeclarator> variableDeclarator = initialisationVariableDeclarationExpr.findFirst(VariableDeclarator.class);
        if (variableDeclarator.isEmpty()) {
            // Something is wrong, abandon!
            return null;
        }

        // "int i = 0;", get me the 1st element, i.e. the variable name
        String elementVariable = variableDeclarator.get().getChildNodes().get(1).toString();
        if (elementVariable == null || elementVariable.isEmpty()) {
            return null;
        }

        Optional<IntegerLiteralExpr> startingIndexOptional = initialisationVariableDeclarationExpr.findFirst(IntegerLiteralExpr.class);
        if (startingIndexOptional.isEmpty()) {
            // Something is wrong, abandon!
            return null;
        }

        String startingIndex = startingIndexOptional.get().getValue();

        // Right side of the comparsion usually has the end index
        String endIndex = forStmt.getCompare().get().asBinaryExpr().getRight().toString();

        String template = String.format("IntStream.range(%s, %s).forEach((%s) -> %s )",
            startingIndex,
            endIndex,
            elementVariable,
            forStmt.getBody().toString()
        );

        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        compilationUnit.addImport(java.util.stream.IntStream.class);

        return replacingExpressionStmt;
    }
    
}