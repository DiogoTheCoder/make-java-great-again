package org.brunel.fyp.langserver;

import java.util.Arrays;

public class ForEachListToFunctional {
    public static void refactorThis() {
        String[] names = { "Diogo", "Costa" };

        for (String string : names) {
            System.out.println(string);
        }
    }

    public static void expectedResult() {
        String[] names = { "Diogo", "Costa" };
        Arrays
                .stream(names)
                .forEach(
                        string -> {
                            System.out.println(string);
                        }
                );
    }
}
