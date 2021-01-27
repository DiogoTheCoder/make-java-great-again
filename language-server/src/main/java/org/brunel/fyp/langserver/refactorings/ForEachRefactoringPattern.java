package org.brunel.fyp.langserver.refactorings;

import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;

import org.brunel.fyp.langserver.MJGARefactoringPattern;
import org.brunel.fyp.langserver.MJGAWorkspaceService;

public class ForEachRefactoringPattern extends MJGARefactoringPattern {

    @Override
    public CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ExpressionStmt expression = convertToForEach((ForEachStmt) node, compilationUnit);
        if (expression != null) {
            node.replace(expression);
        }

        return compilationUnit;
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
