package org.brunel.fyp.langserver.refactorings;

import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;

import org.brunel.fyp.langserver.MJGARefactoringPattern;
import org.brunel.fyp.langserver.MJGAWorkspaceService;

public class ForEachRefactoringPattern implements MJGARefactoringPattern {

    @Override
    public CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ExpressionStmt expression = convertToReduce((ForEachStmt) node, compilationUnit);
        if (expression != null) {
            node.replace(expression);
        }

        expression = convertToForEach((ForEachStmt) node, compilationUnit);
        if (expression != null) {
            node.replace(expression);
        }

        return compilationUnit;
    }

    private static ExpressionStmt convertToReduce(ForEachStmt forEachStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Should we use reduce? Are we re-assinging and appending?
        Optional<AssignExpr> assignOptional = forEachStmt.findFirst(AssignExpr.class);
        if (assignOptional.isEmpty()) {
            return null;
        }

        NameExpr assignExpression = assignOptional.get().getTarget().asNameExpr();
        Optional<VariableDeclarator> assignDeclaratorOptional = MJGAWorkspaceService.variableDeclarationExprs
            .stream()
            .filter(variable -> variable.getName().getIdentifier()
            .equals(assignExpression.getName().getIdentifier()))
            .findFirst();

        if (assignDeclaratorOptional.isEmpty()) {
            // Cannot find the result variable declaration!?
            return null;
        }

        Optional<Expression> assignDeclaratorOptionalInitializer = assignDeclaratorOptional.get().getInitializer();
        if (assignDeclaratorOptionalInitializer.isEmpty()) {
            // Result variable wasn't been initialised, hmmm...
            return null;
        }

        AssignExpr.Operator assignOperator = assignOptional.get().getOperator();
        if (!assignOperator.equals(AssignExpr.Operator.PLUS)) {
            return null;
        }

        // Workout which type of Array/List is this
        NameExpr arrayVariable = forEachStmt.getIterable().asNameExpr();
        Optional<VariableDeclarator> arrayDeclaratorOptional = MJGAWorkspaceService.variableDeclarationExprs
            .stream()
            .filter(variable -> variable.getName().getIdentifier()
            .equals(arrayVariable.getName().getIdentifier()))
            .findFirst();

        if (arrayDeclaratorOptional.isEmpty()) {
            // Array not declared, wtf o_o
            return null;
        }

        String template = "%s = %s.reduce(%s, (partial, %s) -> partial + %s)";
        Type arrayType = arrayDeclaratorOptional.get().getType();
        if (arrayType.getClass().equals(ArrayType.class)) {
            // e.g. String[]
            template = "%s = Arrays.stream(%s).reduce(%s, (partial, %s) -> partial + %s)";
            compilationUnit.addImport(java.util.Arrays.class);
        }

        template = String.format(
            template,
            assignOptional.get().getTarget().toString(),
            arrayVariable.toString(),
            assignDeclaratorOptionalInitializer.get().toString(),
            forEachStmt.getVariableDeclarator().toString(),
            assignOptional.get().getValue()
        );
        
        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        return replacingExpressionStmt;
    }

    private static ExpressionStmt convertToForEach(ForEachStmt forEachStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Workout which type of Array/List is this
        NameExpr arrayVariable = forEachStmt.getIterable().asNameExpr();
        Optional<VariableDeclarator> arrayDeclaratorOptional = MJGAWorkspaceService.variableDeclarationExprs
            .stream()
            .filter(variable -> variable.getName().getIdentifier()
            .equals(arrayVariable.getName().getIdentifier()))
            .findFirst();

        if (arrayDeclaratorOptional.isEmpty()) {
            // Array not declared, wtf o_o
            return null;
        }

        String template = "%s.forEach(%s -> %s)";
        Type arrayType = arrayDeclaratorOptional.get().getType();
        if (arrayType.getClass().equals(ArrayType.class)) {
            // e.g. String[]
            template = "Arrays.stream(%s).forEach(%s -> %s)";
            compilationUnit.addImport(java.util.Arrays.class);
        }

        template = String.format(template, arrayVariable.toString(), forEachStmt.getVariableDeclarator().toString(), forEachStmt.getBody().toString());
        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        return replacingExpressionStmt;
    }
    
}
