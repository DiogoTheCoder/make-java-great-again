package org.brunel.fyp.langserver.refactorings;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.UnaryExpr.Operator;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;

public class ForLoopRefactoringPattern implements MJGARefactoringPattern {
    // Map Variables
    private MethodCallExpr expressionStmtMethodCall;
    private NameExpr arrayName;

    // forEach Variables
    Expression updateExpr;
    String elementVariable;
    String startingIndex;
    String endingIndex;

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

    @Override
    public LinkedHashMap<RefactorPatternTypes, Boolean> refactorable(Node node, CompilationUnit compilationUnit) {
        return new LinkedHashMap<RefactorPatternTypes, Boolean>() {{
            put(RefactorPatternTypes.MAP, canConvertToMap((ForStmt) node));
            put(RefactorPatternTypes.FOR_EACH, canConvertToForEach((ForStmt) node));
        }};
    }

    private ExpressionStmt convertToMap(ForStmt forStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        if (!canConvertToMap(forStmt)) {
            return null;
        }

        String mapFunction = "";

        // Build something like this: "array =
        // array.stream().map(String::toUpperCase).collect(Collectors.toList())"
        if (expressionStmtMethodCall.toString().contains(".toUpperCase()")) {
            // We are trying to uppercase the elements of the list
            mapFunction = "String::toUpperCase";
        } else if (expressionStmtMethodCall.toString().contains(".toLowerCase()")) {
            // We are trying to lowercase the elements of the list
            mapFunction = "String::toLowerCase";
        }

        String template = String.format("%s = %s.stream().map(%s).collect(Collectors.toList())",
                this.arrayName.toString(), this.arrayName.toString(), mapFunction);

        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        compilationUnit.addImport(java.util.stream.Collectors.class);

        return replacingExpressionStmt;
    }

    private boolean canConvertToMap(ForStmt forStmt) {
        // Are we looping the entire array?
        Optional<MethodCallExpr> arraySizeCallOptional = forStmt.findFirst(MethodCallExpr.class);
        if (!arraySizeCallOptional.isPresent()) {
            // Not a list, could be an array instead
            return false;
        }

        MethodCallExpr arraySizeCall = arraySizeCallOptional.get().asMethodCallExpr();
        if (!arraySizeCall.getName().toString().equals("size")) {
            // Not looping through the entire array
            return false;
        }

        Optional<NameExpr> arrayNameOptional = arraySizeCall.findFirst(NameExpr.class);
        if (!arrayNameOptional.isPresent()) {
            return false;
        }

        this.arrayName = arrayNameOptional.get();

        NodeList<Statement> bodyExpression = forStmt.getBody().asBlockStmt().getStatements();
        if (bodyExpression.size() == 1 && bodyExpression.get(0).isExpressionStmt()) {
            Optional<Node> expressionStmtMethodCallOptional = bodyExpression.get(0).getChildNodes().stream()
                    .filter(expression -> expression.getClass().equals(MethodCallExpr.class)).findFirst();
            if (!expressionStmtMethodCallOptional.isPresent()) {
                return false;
            }

            this.expressionStmtMethodCall = (MethodCallExpr) expressionStmtMethodCallOptional.get();

            // We ain't trying to set anything, don't use map therefore
            return this.expressionStmtMethodCall.getName().toString().equals("set");
        }

        return false;
    }

    private ExpressionStmt convertToForEach(ForStmt forStmt, CompilationUnit compilationUnit) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        if (!canConvertToForEach(forStmt)) {
            return null;
        }

        Operator updateOperator = this.updateExpr.asUnaryExpr().getOperator();
        String template = "";
        if (updateOperator.equals(Operator.POSTFIX_DECREMENT) || updateOperator.equals(Operator.PREFIX_DECREMENT)) {
            // We are likely reversing a for loop
            template = String.format(
                    "IntStream.range(%s + 1, %s + 1).boxed().sorted(Comparator.reverseOrder()).forEach((%s) -> %s )",
                    this.endingIndex,
                    this.startingIndex,
                    this.elementVariable,
                    forStmt.getBody().toString()
            );

            compilationUnit.addImport(java.util.Comparator.class);
        } else {
            template = String.format("IntStream.range(%s, %s).forEach((%s) -> %s )", this.startingIndex, this.endingIndex,
                    this.elementVariable, forStmt.getBody().toString());
        }

        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        compilationUnit.addImport(java.util.stream.IntStream.class);

        return replacingExpressionStmt;
    }

    private boolean canConvertToForEach(ForStmt forStmt) {
        // Get the starting index
        VariableDeclarationExpr initialisationVariableDeclarationExpr = forStmt.getInitialization().getFirst().get()
                .asVariableDeclarationExpr();
        Optional<VariableDeclarator> variableDeclarator = initialisationVariableDeclarationExpr
                .findFirst(VariableDeclarator.class);
        if (!variableDeclarator.isPresent()) {
            // Something is wrong, abandon!
            return false;
        }

        // "int i = 0;", get me the 1st element, i.e. the variable name
        String elementVariable = variableDeclarator.get().getChildNodes().get(1).toString();
        if (elementVariable == null || elementVariable.isEmpty()) {
            return false;
        }

        this.elementVariable = elementVariable;

        String startingIndex;
        Optional<IntegerLiteralExpr> startingIndexOptional = initialisationVariableDeclarationExpr
                .findFirst(IntegerLiteralExpr.class);
        if (!startingIndexOptional.isPresent()) {
            // The startingIndexOptional is not a raw number, is it a variable?
            // check for FieldAccessExpr
            Optional<FieldAccessExpr> startingIndexVariableOptional = initialisationVariableDeclarationExpr
                    .findFirst(FieldAccessExpr.class);
            if (!startingIndexVariableOptional.isPresent()) {
                // If its not a variable, it might be a method call instead, like .size()
                Optional<MethodCallExpr> startingIndexMethodOptional = initialisationVariableDeclarationExpr
                        .findFirst(MethodCallExpr.class);
                if (!startingIndexMethodOptional.isPresent()) {
                    return false;
                } else {
                    startingIndex = startingIndexMethodOptional.get().toString();
                }
            } else {
                startingIndex = startingIndexVariableOptional.get().toString();
            }
        } else {
            startingIndex = startingIndexOptional.get().getValue();
        }

        this.startingIndex = startingIndex;

        Optional<Expression> compareOptional = forStmt.getCompare();
        if (!compareOptional.isPresent()) {
            return false;
        }

        // Right side of the comparsion usually has the end index
        this.endingIndex = compareOptional.get().asBinaryExpr().getRight().toString();

        Optional<Expression> updateOptional = forStmt.getUpdate().getFirst();
        // Has no update, e.g. i++ or i--, wtf :/
        if (!updateOptional.isPresent()) {
            return false;
        }

        this.updateExpr = updateOptional.get();

        return true;
    }


}
