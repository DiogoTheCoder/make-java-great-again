package org.brunel.fyp.langserver;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LowerCaseToMap {
    public static void refactorThis() {
        List<String> names = Arrays.asList("Diogo", "Costa");

        for (int i = 0; i < names.size(); i++) {
            names.set(i, names.get(i).toUpperCase());
        }
    }

    public static void expectedResult() {
        List<String> names = Arrays.asList("Diogo", "Costa");
        names =
                names
                        .stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toList());
    }
}
