package com.brunel.diogocosta.fyp.examples;

public class ForEachReduce {

    public static void main(String[] args) {
        int[] ints = { 1, 2, 3 };
        
        int sum = 0;
        for (int number : ints) {
            sum += number;
        }

        System.out.println(sum);
    }
}
