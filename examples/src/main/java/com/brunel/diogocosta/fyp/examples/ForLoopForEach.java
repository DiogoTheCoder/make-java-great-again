package com.brunel.diogocosta.fyp.examples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ForLoopForEach {

    public static void main(String[] args) {
        List<String> names = Arrays.asList("Diogo", "Costa");
        IntStream
            .range(0, names.size())
            .forEach(
                i -> {
                    System.out.println(names.get(i));
                }
            );
    }
}
