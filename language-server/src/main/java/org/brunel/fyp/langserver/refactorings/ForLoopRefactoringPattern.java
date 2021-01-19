package org.brunel.fyp.langserver.refactorings;

import java.util.Optional;

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
import com.github.javaparser.ast.type.UnknownType;

import org.brunel.fyp.langserver.MJGARefactoringPattern;

public class ForLoopRefactoringPattern extends MJGARefactoringPattern {

    @Override
    public CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ForStmt forStmt = (ForStmt) node;
        
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
        forStmt.replace(eStmt);

        return compilationUnit;
    }
    
}
