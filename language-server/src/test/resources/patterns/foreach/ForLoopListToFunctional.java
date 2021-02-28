package org.brunel.fyp.langserver;

import java.util.Arrays;
import java.util.List;

public class ForEachToFunctional {
    public static void beforeRefactor() {
        List<String> names = Arrays.asList("Diogo", "Costa");

        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i));
        }
    }

    public static void afterRefactor() {
        String[] names = { "Diogo", "Costa" };
        IntStream
                .range(0, names.size())
                .forEach(
                        (i) -> {
                            System.out.println(names.get(i));
                        }
                );
    }
}
