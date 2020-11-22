package com.brunel.diogocosta.fyp.examples;

import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class App {
    private static Logger logger;
    private static final Integer SEQUENCE_SIZE = 10000;

    public static void main(String[] args) {
        logger = Logger.getGlobal();

        logger.fine("Hello World!");

        timeAndExecuteFunction(() -> generateSequence(), "generateSequence");
        ArrayList<Integer> sequence = generateSequence();

        logger.info("--- ITERATING ARRAYLIST ---");
        timeAndExecuteFunction(() -> forLoopIterate(sequence), "forLoopIterate");
        timeAndExecuteFunction(() -> forEachIterate(sequence), "forEachIterate");
        timeAndExecuteFunction(() -> whileIterate(sequence), "whileIterate");
        timeAndExecuteFunction(() -> whileIterator(sequence), "whileIterator");
        timeAndExecuteFunction(() -> functionalForEachIterate(sequence), "functionalForEachIterate");
        timeAndExecuteFunction(() -> functionalStreamForEachIterate(sequence), "functionalStreamForEachIterate");

        logger.info("--- CONCATENATING ARRAYLIST ELEMENTS ---");
        timeAndExecuteFunction(() -> forEachIterateConcat(sequence), "forEachIterateConcat");
        timeAndExecuteFunction(() -> functionalForEachIterateConcat(sequence), "functionalForEachIterateConcat");
        timeAndExecuteFunction(() -> functionalStreamCollectConcat(sequence), "functionalStreamCollectConcat");

        logger.info("--- SUMMING ARRAYLIST ELEMENTS ---");
        timeAndExecuteFunction(() -> forEachIterateSum(sequence), "forEachIterateSum");
        timeAndExecuteFunction(() -> functionalForEachIterateSum(sequence), "functionalForEachIterateSum");
        timeAndExecuteFunction(() -> functionalStreamCollectSum(sequence), "functionalStreamCollectSum");
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
