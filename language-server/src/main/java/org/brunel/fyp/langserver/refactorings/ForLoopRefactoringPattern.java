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
        VariableDeclarationExpr initialisationVariableDeclarationExpr = forStmt.getInitialization().getFirst().get().asVariableDeclarationExpr();
        VariableDeclarator initialisationVariableDeclarator = initialisationVariableDeclarationExpr.getVariables().getFirst().get();
        IntegerLiteralExpr integerLiteralExpr = initialisationVariableDeclarator.getInitializer().get().asIntegerLiteralExpr();

        BinaryExpr comparisonBinaryExpr = forStmt.getCompare().get().asBinaryExpr();
        Expression comparisonRightExpression = comparisonBinaryExpr.getRight();

        UnaryExpr updateUnaryExpr = forStmt.getUpdate().getFirst().get().asUnaryExpr();
        Operator updateUnaryExprOperator = updateUnaryExpr.getOperator();

        ExpressionStmt eStmt = new ExpressionStmt();
        Optional<Comment> comment = forStmt.getComment();
        if (comment.isPresent()) {
            eStmt.setComment(comment.get());
        }

        // IntStream.range(0, length)
        MethodCallExpr methodCallExprIntStream = new MethodCallExpr();
        // range
        SimpleName simpleNameRange = new SimpleName("range");

        compilationUnit.addImport(new ImportDeclaration("java.util.stream.IntStream", false, false));
        NameExpr nameExprIntStream = new NameExpr(new SimpleName("IntStream"));

        // Name of the array to loop, argument for IntStream.range(startIndex, endIndex)
        methodCallExprIntStream.setArguments(new NodeList<Expression>(integerLiteralExpr, comparisonRightExpression));

        nameExprIntStream.setParentNode(methodCallExprIntStream);
        methodCallExprIntStream.setScope(nameExprIntStream);
        methodCallExprIntStream.setName(simpleNameRange);

        // IntStream.range(0, length), forEach, "string -> {
        MethodCallExpr methodCallExpr = new MethodCallExpr();

        // forEach
        methodCallExpr.setName(new SimpleName("forEach"));

        // string
        Parameter parameterString = new Parameter(new UnknownType(), initialisationVariableDeclarator.getName());
        // "string -> {
        LambdaExpr lambdaExpr = new LambdaExpr(parameterString, (BlockStmt) forStmt.getBody());

        // IntStream.range(0, length), "string ->
        methodCallExpr.setArguments(new NodeList<Expression>(lambdaExpr));

        // Is this a reverse for-loop?
        if (updateUnaryExprOperator.name().equals(Operator.PREFIX_DECREMENT.name()) || updateUnaryExprOperator.name().equals(Operator.POSTFIX_DECREMENT.name()) ) {
            // .boxed()
            MethodCallExpr methodCallExprBoxed = new MethodCallExpr();
            // boxed
            methodCallExprBoxed.setName(new SimpleName("boxed"));

            methodCallExprBoxed.setParentNode(methodCallExprIntStream);
            methodCallExprBoxed.setScope(methodCallExprIntStream);

            // Collections.reverseOrder()
            MethodCallExpr methodCallExprCollections = new MethodCallExpr();
            SimpleName simpleNameReverseOrder = new SimpleName("reverseOrder");
            NameExpr nameExprCollections = new NameExpr(new SimpleName("Collections"));

            nameExprCollections.setParentNode(methodCallExprCollections);
            methodCallExprCollections.setName(simpleNameReverseOrder);
            methodCallExprCollections.setScope(nameExprCollections);

            // Let's combine the methods .boxed() and .sorted() into the same parent
            MethodCallExpr methodCallExprBoxedPlusCollections = new MethodCallExpr();
            methodCallExprBoxedPlusCollections.setName(new SimpleName("sorted"));

            methodCallExprBoxed.setParentNode(methodCallExprBoxedPlusCollections);
            methodCallExprCollections.setParentNode(methodCallExprBoxedPlusCollections);
            methodCallExprBoxedPlusCollections.setScope(methodCallExprBoxed);

            // add Collections.reverseOrder() to arguments
            methodCallExprBoxedPlusCollections.setArguments(new NodeList<Expression>(methodCallExprCollections));

            methodCallExpr.setScope(methodCallExprBoxedPlusCollections);
        } else {
            methodCallExpr.setScope(methodCallExprIntStream);
        }

        eStmt.setExpression(methodCallExpr);
        return eStmt;
    }
    
}
