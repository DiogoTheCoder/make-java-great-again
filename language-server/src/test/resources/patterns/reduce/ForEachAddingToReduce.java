package org.brunel.fyp.langserver;

import java.util.Arrays;

public class ForEachAddingToReduce {
    public static void refactorThis() {
        int[] ints = { 1, 2, 3 };

        int sum = 0;
        for (int number : ints) {
            sum += number;
        }
    }

    public static void expectedResult() {
        int[] ints = { 1, 2, 3 };
        int sum = 0;
        sum =
                Arrays
                        .stream(ints)
                        .reduce(0, (partial, number) -> partial + number);
    }
}
