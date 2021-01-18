package com.brunel.diogocosta.fyp.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Refactor {

    public static void main(String[] args) {
        String[] name = { "Diogo", "Costa" };
        ArrayList<String> test = new ArrayList<>();
        test.add("Diogo");
        test.add("Costa");
        List<String> test2 = List.of("Diogo", "Costa");
        List<String> test3 = Arrays.asList("Maria", "Costa");

        for (String e : name) {
           System.out.println(e);  
        }
    }
}
