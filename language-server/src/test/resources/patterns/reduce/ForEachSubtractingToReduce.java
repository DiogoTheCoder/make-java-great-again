package org.brunel.fyp.langserver;

import java.util.Arrays;

public class ForEachSubtractingToReduce {
    public static void refactorThis() {
        int[] ints = { 1, 2, 3 };

        int sum = ints.length;
        for (int number : ints) {
            sum -= number;
        }
    }

    public static void expectedResult() {
        int[] ints = { 1, 2, 3 };
        int sum = ints.length;
        sum =
                Arrays
                        .stream(ints)
                        .reduce(ints.length, (partial, number) -> partial - number);
    }
}
