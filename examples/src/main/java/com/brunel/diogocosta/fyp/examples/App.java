package com.brunel.diogocosta.fyp.examples;

import static java.lang.String.valueOf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
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
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;

import javassist.Loader.Simple;
import javassist.expr.MethodCall;

public class App {
    private static Logger logger;
    private static final Integer SEQUENCE_SIZE = 10000;
    private static CompilationUnit compilationUnit;
    private static List<VariableDeclarator> variableDeclarationExprs;

    public static void main(String[] args) {
        /**
         * Testing refactoring code
         */
        Path filePath = Paths.get("examples\\src\\main\\java\\com\\brunel\\diogocosta\\fyp\\examples\\Refactor.java");
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
            compilationUnit.findAll(ForStmt.class).stream().forEach(forStmt -> {
                refactor(forStmt, compilationUnit);
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
        ExpressionStmt expression = convertToMap((ForStmt) node);
        if (expression != null) {
            node.replace(expression);
        }

        expression = convertToForEach((ForStmt) node);
        if (expression != null) {
            node.replace(expression);
        }

        return compilationUnit;
    }

    private static ExpressionStmt convertToMap(ForStmt forStmt) {
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
        }

        return replacingExpressionStmt;
    }

    private static ExpressionStmt convertToForEach(ForStmt forStmt) {
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
