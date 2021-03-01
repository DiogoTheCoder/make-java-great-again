package org.brunel.fyp.langserver;

public class ForLoopArrayToFunctional {
    public static void refactorThis() {
        String[] names = { "Diogo", "Costa" };

        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i]);
        }
    }

    public static void expectedResult() {
        String[] names = { "Diogo", "Costa" };
        IntStream
                .range(0, names.length)
                .forEach(
                        (i) -> {
                            System.out.println(names[i]);
                        }
                );
    }
}
