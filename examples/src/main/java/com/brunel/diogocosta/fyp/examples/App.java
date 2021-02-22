package com.brunel.diogocosta.fyp.examples;

import static java.lang.String.valueOf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.UnaryExpr.Operator;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;

public class App {
    private static Logger logger;
    private static final Integer SEQUENCE_SIZE = 10000;
    private static CompilationUnit compilationUnit;
    private static List<VariableDeclarator> variableDeclarationExprs;

    public static void main(String[] args) throws IOException {
        /**
         * Testing refactoring code
         */
        Path filePath = Paths.get("examples//src//main//java//com//brunel//diogocosta//fyp//examples//ForEachForEach.java");
        try {
            ParserConfiguration parserConfig = new ParserConfiguration();
            parserConfig.setAttributeComments(true);
            StaticJavaParser.setConfiguration(parserConfig);
            compilationUnit = StaticJavaParser.parse(new FileInputStream(filePath.toAbsolutePath().toString()));

            /**
             * Variables used for debugging/testing
             */
            // MethodDeclaration mDeclaration = (MethodDeclaration)
            // compilationUnit.getChildNodes().get(2).getChildNodes().get(2);
            // BlockStmt blockStmt = mDeclaration.getBody().get();
            // ExpressionStmt expressionStmt = (ExpressionStmt)
            // blockStmt.getChildNodes().get(1);
            // MethodCallExpr methodCallExprLambda = (MethodCallExpr)
            // expressionStmt.getChildNodes().get(0);

            App.variableDeclarationExprs = compilationUnit.findAll(VariableDeclarator.class);
            compilationUnit.findAll(ForEachStmt.class).stream().forEach(forEachStmt -> {
                refactor(forEachStmt, compilationUnit);
            });

            System.out.println(compilationUnit);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /**
         * Performance tests
         */
        // logger = Logger.getGlobal();

        // logger.fine("Hello World!");

        // timeAndExecuteFunction(() -> generateSequence(), "generateSequence");
        // ArrayList<Integer> sequence = generateSequence();

        // logger.info("--- ITERATING ARRAYLIST ---");
        // timeAndExecuteFunction(() -> forLoopIterate(sequence), "forLoopIterate");
        // timeAndExecuteFunction(() -> forEachIterate(sequence), "forEachIterate");
        // timeAndExecuteFunction(() -> whileIterate(sequence), "whileIterate");
        // timeAndExecuteFunction(() -> whileIterator(sequence), "whileIterator");
        // timeAndExecuteFunction(() -> functionalForEachIterate(sequence),
        // "functionalForEachIterate");
        // timeAndExecuteFunction(() -> functionalStreamForEachIterate(sequence),
        // "functionalStreamForEachIterate");

        // logger.info("--- CONCATENATING ARRAYLIST ELEMENTS ---");
        // timeAndExecuteFunction(() -> forEachIterateConcat(sequence),
        // "forEachIterateConcat");
        // timeAndExecuteFunction(() -> functionalForEachIterateConcat(sequence),
        // "functionalForEachIterateConcat");
        // timeAndExecuteFunction(() -> functionalStreamCollectConcat(sequence),
        // "functionalStreamCollectConcat");

        // logger.info("--- SUMMING ARRAYLIST ELEMENTS ---");
        // timeAndExecuteFunction(() -> forEachIterateSum(sequence),
        // "forEachIterateSum");
        // timeAndExecuteFunction(() -> functionalForEachIterateSum(sequence),
        // "functionalForEachIterateSum");
        // timeAndExecuteFunction(() -> functionalStreamCollectSum(sequence),
        // "functionalStreamCollectSum");
    }

    private static CompilationUnit refactor(Node node, CompilationUnit compilationUnit) {
        ExpressionStmt expression;
        // if (expression != null) {
        //     node.replace(expression);
        // }

        // expression = convertToForEach((ForEachStmt) node);
        expression = convertToReduce((ForEachStmt) node);
        if (expression != null) {
            node.replace(expression);
        }

        return compilationUnit;
    }

    private static ExpressionStmt convertToReduce(ForEachStmt forEachStmt) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Should we use reduce? Are we re-assinging and appending?
        Optional<AssignExpr> assignOptional = forEachStmt.findFirst(AssignExpr.class);
        if (assignOptional.isEmpty()) {
            return null;
        }

        NameExpr assignExpression = assignOptional.get().getTarget().asNameExpr();
        Optional<VariableDeclarator> assignDeclaratorOptional = variableDeclarationExprs
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
        Optional<VariableDeclarator> arrayDeclaratorOptional = variableDeclarationExprs
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

    private static ExpressionStmt convertToMap(ForStmt forStmt) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Are we starting from index 0?
        VariableDeclarationExpr initialisationVariableDeclarationExpr = forStmt.getInitialization().getFirst().get().asVariableDeclarationExpr();
        Optional<IntegerLiteralExpr> startingIndexOptional = initialisationVariableDeclarationExpr.findFirst(IntegerLiteralExpr.class);
        if (startingIndexOptional.isEmpty()) {
            // Something is wrong, abandon!
        }

        Integer startingIndex = Integer.valueOf(startingIndexOptional.get().getValue());
        if (startingIndex > 0) {
            // The index doesn't start at 0, abandon ship!
            return null;
        }

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
        }

        return replacingExpressionStmt;
    }

    private static ExpressionStmt convertToForEach(ForStmt forStmt) {
        ExpressionStmt replacingExpressionStmt = new ExpressionStmt();

        // Get the starting index
        VariableDeclarationExpr initialisationVariableDeclarationExpr = forStmt.getInitialization().getFirst().get()
                .asVariableDeclarationExpr();
        Optional<VariableDeclarator> variableDeclarator = initialisationVariableDeclarationExpr
                .findFirst(VariableDeclarator.class);
        if (variableDeclarator.isEmpty()) {
            // Something is wrong, abandon!
            return null;
        }

        // "int i = 0;", get me the 1st element, i.e. the variable name
        String elementVariable = variableDeclarator.get().getChildNodes().get(1).toString();
        if (elementVariable == null || elementVariable.isEmpty()) {
            return null;
        }

        String startingIndex;
        Optional<IntegerLiteralExpr> startingIndexOptional = initialisationVariableDeclarationExpr
                .findFirst(IntegerLiteralExpr.class);
        if (startingIndexOptional.isEmpty()) {
            // The startingIndexOptional is not a raw number, is it a variable?
            // check for FieldAccessExpr
            Optional<FieldAccessExpr> startingIndexVariableOptional = initialisationVariableDeclarationExpr
                .findFirst(FieldAccessExpr.class);
            if (startingIndexVariableOptional.isEmpty()) {
                // If its not a variable, it might be a method call instead, like .size()
                Optional<MethodCallExpr> startingIndexMethodOptional = initialisationVariableDeclarationExpr
                    .findFirst(MethodCallExpr.class);
                if (startingIndexMethodOptional.isEmpty()) {
                    return null;
                } else {
                    startingIndex = startingIndexMethodOptional.get().toString();
                }
            } else {
                startingIndex = startingIndexVariableOptional.get().toString();
            }
        } else {
            startingIndex = startingIndexOptional.get().getValue();
        }

        // Right side of the comparsion usually has the end index
        String endIndex = forStmt.getCompare().get().asBinaryExpr().getRight().toString();

        Optional<Expression> updateOptional = forStmt.getUpdate().getFirst();
        if (updateOptional.isEmpty()) {
            // Has no update, e.g. i++ or i--, wtf :/
            return null;
        }

        Operator updateOperator = updateOptional.get().asUnaryExpr().getOperator();
        String template = "";
        if (updateOperator.equals(Operator.POSTFIX_DECREMENT) || updateOperator.equals(Operator.PREFIX_DECREMENT)) {
            // We are likely reversing a for loop
            template = String.format(
                "IntStream.range(%s + 1, %s + 1).boxed().sorted(Comparator.reverseOrder()).forEach((%s) -> %s )",
                endIndex,
                startingIndex,
                elementVariable,
                forStmt.getBody().toString()
            );

            compilationUnit.addImport(java.util.Comparator.class);
        } else {
            template = String.format("IntStream.range(%s, %s).forEach((%s) -> %s )", startingIndex, endIndex,
                elementVariable, forStmt.getBody().toString());
        }

        Expression templateExpression = StaticJavaParser.parseExpression(template);
        replacingExpressionStmt.setExpression(templateExpression);

        compilationUnit.addImport(java.util.stream.IntStream.class);

        return replacingExpressionStmt;
    }

    private static void forLoopIterate(ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            String number = valueOf(list.get(i));
            logger.finest(number);
        }
    }

    private static void forEachIterate(ArrayList<Integer> list) {
        for (Integer integer : list) {
            String number = valueOf(integer);
            logger.finest(number);
        }
    }

    private static void whileIterator(ArrayList<Integer> list) {
        Iterator<Integer> listIterator = list.iterator();
        while (listIterator.hasNext()) {
            String number = valueOf(listIterator.next());
            logger.finest(number);
        }
    }

    private static void whileIterate(ArrayList<Integer> list) {
        int i = 0;
        while (i < list.size()) {
            String number = valueOf(list.get(i));
            logger.finest(number);
            i++;
        }
    }

    private static void functionalForEachIterate(ArrayList<Integer> list) {
        list.forEach(integer -> {
            String number = valueOf(integer);
            logger.finest(number);
        });
    }

    private static void functionalStreamForEachIterate(ArrayList<Integer> list) {
        list.stream().forEach(integer -> {
            String number = valueOf(integer);
            logger.finest(number);
        });
    }

    private static void forEachIterateConcat(ArrayList<Integer> list) {
        // You can also use a StringBuilder object
        String output = "";
        for (Integer integer : list) {
            output += ", " + valueOf(integer);
        }

        logger.finest(output);
    }

    private static void functionalForEachIterateConcat(ArrayList<Integer> list) {
        String output = "";
        list.forEach(integer -> output.concat(", " + valueOf(integer)));

        logger.finest(output.toString());
    }

    private static void functionalStreamCollectConcat(ArrayList<Integer> list) {
        String output = list.stream().map(Object::toString).collect(Collectors.joining(", "));
        logger.finest(output);
    }

    private static void forEachIterateSum(ArrayList<Integer> list) {
        // You can also use a StringBuilder object
        int sum = 0;
        for (Integer integer : list) {
            sum += integer;
        }

        logger.finest(valueOf(sum));
    }

    private static void functionalForEachIterateSum(ArrayList<Integer> list) {
        AtomicInteger sum = new AtomicInteger();
        list.forEach(integer -> sum.getAndAdd(integer));

        logger.finest(valueOf(sum));
    }

    private static void functionalStreamCollectSum(ArrayList<Integer> list) {
        Integer sum = list.stream().collect(Collectors.summingInt(Integer::intValue));
        logger.finest(valueOf(sum));
    }

    private static ArrayList<Integer> generateSequence() {
        ArrayList<Integer> sequence = new ArrayList<>();
        for (int i = 0; i < SEQUENCE_SIZE; i++) {
            sequence.add(i);
        }

        return sequence;
    }

    private static void timeAndExecuteFunction(Runnable runnable, String logPrefix) {
        try {
            long startTime = System.currentTimeMillis();
            runnable.run();
            long endTime = System.currentTimeMillis();

            double duration = endTime - startTime;

            String output = String.format("%s: %f milliseconds", logPrefix, duration);

            logger.info(output);

        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}
