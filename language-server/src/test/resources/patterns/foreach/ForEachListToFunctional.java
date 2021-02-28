package org.brunel.fyp.langserver;

import java.util.Arrays;
import java.util.List;

public class ForEachToFunctional {
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
