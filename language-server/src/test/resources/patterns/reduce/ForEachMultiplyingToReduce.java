package org.brunel.fyp.langserver;

import java.util.Arrays;

public class ForEachMultiplyingToReduce {
    public static void refactorThis() {
        int[] ints = { 1, 2, 3 };

        int sum = 1;
        for (int number : ints) {
            sum *= number;
        }
    }

    public static void expectedResult() {
        int[] ints = { 1, 2, 3 };
        int sum = 1;
        sum =
                Arrays
                        .stream(ints)
                        .reduce(1, (partial, number) -> partial * number);
    }
}
