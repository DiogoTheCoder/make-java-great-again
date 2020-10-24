package com.brunel.diogocosta.fyp.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class App {
    private static Logger logger;
    private static final Integer SEQUENCE_SIZE = 500000;

    public static void main(String[] args) {
        logger = Logger.getGlobal();

        logger.fine("Hello World!");

        ArrayList<Integer> sequence = generateSequence();
        timeAndExecuteFunction(() -> forLoopIterate(sequence), "forLoopIterate");
        timeAndExecuteFunction(() -> forEachIterate(sequence), "forEachIterate");
        timeAndExecuteFunction(() -> whileIterate(sequence), "whileIterate");
        timeAndExecuteFunction(() -> whileIterator(sequence), "whileIterator");
        timeAndExecuteFunction(() -> whileListIterator(sequence), "whileListIterator");
        timeAndExecuteFunction(() -> functionalForEachIterate(sequence), "functionalForEachIterate");
        timeAndExecuteFunction(() -> functionalStreamForEachIterate(sequence), "functionalStreamForEachIterate");
    }

    private static void forLoopIterate(ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            String number = String.valueOf(list.get(i));
            logger.finest(number);
        }
    }

    private static void forEachIterate(ArrayList<Integer> list) {
        for (Integer integer : list) {
            String number = String.valueOf(integer);
            logger.finest(number);
        }
    }

    private static void whileIterator(ArrayList<Integer> list) {
        Iterator<Integer> listIterator = list.iterator();
        while (listIterator.hasNext()) {
            String number = String.valueOf(listIterator.next());
            logger.finest(number);
        }
    }

    private static void whileListIterator(ArrayList<Integer> list) {
        int i = 0;
        while (i < list.size()) {
            String number = String.valueOf(list.get(i));
            logger.finest(number);
            i++;
        }
    }

    private static void whileIterate(ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            String number = String.valueOf(list.get(i));
            logger.finest(number);
        }
    }

    private static void functionalForEachIterate(ArrayList<Integer> list) {
        list.forEach(integer -> {
            String number = String.valueOf(integer);
            logger.finest(number);
        });
    }

    private static void functionalStreamForEachIterate(ArrayList<Integer> list) {
        list.stream().forEach(integer -> {
            String number = String.valueOf(integer);
            logger.finest(number);
        });
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

            long duration = endTime - startTime;

            String output = String.format("%s: %d milliseconds", logPrefix, duration);

            logger.info(output);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}
