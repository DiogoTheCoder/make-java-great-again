package com.brunel.diogocosta.fyp.examples;

import static java.lang.String.valueOf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.UnknownType;

public class App {
    private static Logger logger;
    private static final Integer SEQUENCE_SIZE = 10000;

    public static void main(String[] args) {
        /**
         * Testing refactoring code
         */
        Path filePath = Paths.get("examples\\src\\main\\java\\com\\brunel\\diogocosta\\fyp\\examples\\Refactor.java");
        CompilationUnit compilationUnit;
        try {
            compilationUnit = StaticJavaParser.parse(new FileInputStream(filePath.toAbsolutePath().toString()));
            compilationUnit.findAll(ExpressionStmt.class)
            .stream()
            .forEach(lambdaExpr -> {
                //System.out.println(lambdaExpr);
                //LambdaExpr lambdaExpr = new LambdaExpr(forEachStmt.getIterable(), forEachStmt.getBody());
                // ExpressionStmt expressionStmt = new ExpressionStmt();
            });
            MethodDeclaration mDeclaration = (MethodDeclaration) compilationUnit.getChildNodes().get(2).getChildNodes().get(2);
            BlockStmt blockStmt = mDeclaration.getBody().get();
            ExpressionStmt expressionStmt = (ExpressionStmt) blockStmt.getChildNodes().get(1);
            MethodCallExpr methodCallExprLambda = (MethodCallExpr) expressionStmt.getChildNodes().get(0);
            compilationUnit.findAll(ForEachStmt.class)
                .stream()
                .forEach(forEachStmt -> {
                    ExpressionStmt eStmt = new ExpressionStmt();

                    // Arrays.stream(name)
                    MethodCallExpr methodCallExprArrays = new MethodCallExpr();
                    // Arrays
                    NameExpr nameExprArrays = new NameExpr(new SimpleName("Arrays"));
                    // stream
                    SimpleName simpleNameStream = new SimpleName("stream");

                    nameExprArrays.setParentNode(methodCallExprArrays);
                    methodCallExprArrays.setScope(nameExprArrays);
                    methodCallExprArrays.setName(simpleNameStream);
                    // Name of the array to loop, argument for Arrays.stream()
                    methodCallExprArrays.setArguments(new NodeList<Expression>(new NameExpr("name")));
                    
                    // Arrays.stream(name), forEach, "string -> {
                    MethodCallExpr methodCallExpr = new MethodCallExpr();

                    // forEach
                    methodCallExpr.setName(new SimpleName("forEach"));

                    // string
                    Parameter parameterString = new Parameter(new UnknownType(), new SimpleName("string"));
                    // "string -> {
                    LambdaExpr lambdaExpr = new LambdaExpr(parameterString, (BlockStmt) forEachStmt.getBody());

                    // Arrays.stream(name), "string ->
                    methodCallExpr.setArguments(new NodeList<Expression>(lambdaExpr));

                    methodCallExpr.setScope(methodCallExprArrays);

                    eStmt.setExpression(methodCallExpr);

                    forEachStmt.replace(eStmt);
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
        // timeAndExecuteFunction(() -> functionalForEachIterate(sequence), "functionalForEachIterate");
        // timeAndExecuteFunction(() -> functionalStreamForEachIterate(sequence), "functionalStreamForEachIterate");

        // logger.info("--- CONCATENATING ARRAYLIST ELEMENTS ---");
        // timeAndExecuteFunction(() -> forEachIterateConcat(sequence), "forEachIterateConcat");
        // timeAndExecuteFunction(() -> functionalForEachIterateConcat(sequence), "functionalForEachIterateConcat");
        // timeAndExecuteFunction(() -> functionalStreamCollectConcat(sequence), "functionalStreamCollectConcat");

        // logger.info("--- SUMMING ARRAYLIST ELEMENTS ---");
        // timeAndExecuteFunction(() -> forEachIterateSum(sequence), "forEachIterateSum");
        // timeAndExecuteFunction(() -> functionalForEachIterateSum(sequence), "functionalForEachIterateSum");
        // timeAndExecuteFunction(() -> functionalStreamCollectSum(sequence), "functionalStreamCollectSum");
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
