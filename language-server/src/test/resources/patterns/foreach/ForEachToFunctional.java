package org.brunel.fyp.langserver;

import java.util.Arrays;
import java.util.List;

public class ForEachToFunctional {
    public static void beforeRefactor() {
        List<String> names = Arrays.asList("Diogo", "Costa");

        for (String string : names) {
            System.out.println(string);
        }
    }

    public static void afterRefactor() {
        List<String> names = Arrays.asList("Diogo", "Costa");
        names.forEach(
                string -> {
                    System.out.println(string);
                }
        );
    }
}
