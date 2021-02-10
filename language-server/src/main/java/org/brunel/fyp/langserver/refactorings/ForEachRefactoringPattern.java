package org.brunel.fyp.langserver.refactorings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;

import org.brunel.fyp.langserver.MJGALanguageServer;
import org.json.JSONObject;

public class ForEachRefactoringPattern implements MJGARefactoringPattern {
    // Shared Variables
    NameExpr arrayVariable;
    VariableDeclarator arrayDeclarator;

    // Reduce Variables
    AssignExpr assignExpr;
    Expression assignDeclaratorInitializer;
    BinaryExpr.Operator assignBinaryExpressionOperator;

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

    @Override
    public Map<String, Boolean> refactorable(Node node, CompilationUnit compilationUnit) {
        return null;
    }

    private ExpressionStmt convertToReduce(ForEachStmt forEachStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        if (!canConvertToReduce(forEachStmt)) {
            return null;
        }


        String template = "%s = %s.reduce(%s, (partial, %s) -> partial %s %s)";
        Type arrayType = this.arrayDeclarator.getType();
        if (arrayType.getClass().equals(ArrayType.class)) {
            // e.g. String[]
            template = "%s = Arrays.stream(%s).reduce(%s, (partial, %s) -> partial %s %s)";
            compilationUnit.addImport(java.util.Arrays.class);
        }

        template = String.format(
                template,
                this.assignExpr.getTarget().toString(),
                this.arrayVariable.toString(),
                this.assignDeclaratorInitializer.toString(),
                forEachStmt.getVariableDeclarator().toString(),
                assignBinaryExpressionOperator.asString(),
                this.assignExpr.getValue()
        );
        
        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        return replacingExpressionStmt;
    }

    private boolean canConvertToReduce(ForEachStmt forEachStmt) {
        // Should we use reduce? Are we re-assigning and appending?
        Optional<AssignExpr> assignOptional = forEachStmt.findFirst(AssignExpr.class);
        if (!assignOptional.isPresent()) {
            return false;
        }

        this.assignExpr = assignOptional.get();

        NameExpr assignExpression = this.assignExpr.getTarget().asNameExpr();
        Optional<VariableDeclarator> assignDeclaratorOptional = MJGALanguageServer.getInstance().getTextDocumentService().getVariableDeclarationExprs()
                .stream()
                .filter(variable -> variable.getName().getIdentifier()
                        .equals(assignExpression.getName().getIdentifier()))
                .findFirst();

        if (!assignDeclaratorOptional.isPresent()) {
            // Cannot find the result variable declaration!?
            return false;
        }

        Optional<Expression> assignDeclaratorOptionalInitializer = assignDeclaratorOptional.get().getInitializer();
        if (!assignDeclaratorOptionalInitializer.isPresent()) {
            // Result variable wasn't been initialised, hmmm...
            return false;
        }

        this.assignDeclaratorInitializer = assignDeclaratorOptionalInitializer.get();

        // Get list of operators from VS Code settings
        JSONObject configurationSettings = MJGALanguageServer.getInstance().getWorkspaceService().getConfigurationSettings();
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(configurationSettings.toString());
        List<Object> operators = configurationSettings
                .getJSONObject("refactor")
                .getJSONObject("reduce")
                .getJSONArray("operators")
                .toList();

        AssignExpr.Operator assignOperator = assignOptional.get().getOperator();
        if (!operators.contains(assignOperator.name())) {
            return false;
        }

        Optional<BinaryExpr.Operator> assignOperatorBinaryExpr = assignOperator.toBinaryOperator();
        if (!assignOperatorBinaryExpr.isPresent()) {
            return false;
        }

        this.assignBinaryExpressionOperator = assignOperatorBinaryExpr.get();

        // Workout which type of Array/List is this
        this.arrayVariable = forEachStmt.getIterable().asNameExpr();
        Optional<VariableDeclarator> arrayDeclaratorOptional = MJGALanguageServer.getInstance().getTextDocumentService().getVariableDeclarationExprs()
                .stream()
                .filter(variable -> variable.getName().getIdentifier()
                        .equals(this.arrayVariable.getName().getIdentifier()))
                .findFirst();

        if (!arrayDeclaratorOptional.isPresent()) {
            // Array not declared, wtf o_o
            return false;
        }

        this.arrayDeclarator = arrayDeclaratorOptional.get();

        return true;
    }

    private ExpressionStmt convertToForEach(ForEachStmt forEachStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        if (!canConvertToForEach(forEachStmt)) {
            return null;
        }

        String template = "%s.forEach(%s -> %s)";
        Type arrayType = this.arrayDeclarator.getType();
        if (arrayType.getClass().equals(ArrayType.class)) {
            // e.g. String[]
            template = "Arrays.stream(%s).forEach(%s -> %s)";
            compilationUnit.addImport(java.util.Arrays.class);
        }

        template = String.format(
                template,
                this.arrayVariable.toString(),
                forEachStmt.getVariableDeclarator().toString(),
                forEachStmt.getBody().toString()
        );

        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        return replacingExpressionStmt;
    }

    private boolean canConvertToForEach(ForEachStmt forEachStmt) {
        // Workout which type of Array/List is this
        this.arrayVariable = forEachStmt.getIterable().asNameExpr();
        Optional<VariableDeclarator> arrayDeclaratorOptional = MJGALanguageServer
                .getInstance()
                .getTextDocumentService()
                .getVariableDeclarationExprs()
                .stream()
                .filter(variable -> variable.getName().getIdentifier()
                        .equals(this.arrayVariable.getName().getIdentifier()))
                .findFirst();

        if (!arrayDeclaratorOptional.isPresent()) {
            // Array not declared, wtf o_o
            return false;
        }

        this.arrayDeclarator = arrayDeclaratorOptional.get();

        return true;
    }
    
}
