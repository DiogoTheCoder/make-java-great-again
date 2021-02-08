package com.brunel.diogocosta.fyp.examples;

import java.util.Arrays;
import java.util.List;

public class ForLoopMap {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Diogo", "Costa");

        for (int i = 0; i < names.size(); i++) {
            names.set(i, names.get(i).toUpperCase());
        }

        System.out.println(names);
    }
}
