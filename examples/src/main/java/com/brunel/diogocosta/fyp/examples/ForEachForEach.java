package com.brunel.diogocosta.fyp.examples;

import java.util.Arrays;
import java.util.List;

public class ForEachForEach {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Diogo", "Costa");

        for (String string : names) {
            System.out.println(string);
        }
    }
}
