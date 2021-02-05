package com.brunel.diogocosta.fyp.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Refactor {

  public static void main(String[] args) {
    int[] int1 = { 1, 2 };
    String[] name1 = { "Diogo", "Costa" };
    List<String> name2 = Arrays.asList("Maria", "Costa");
    ArrayList<String> name3 = new ArrayList<>(List.of("John", "Smith"));

    String result = "";
    for (String string : name1) {
      result += string + ", ";
    }

    int count = 0;
    for (int num : int1) {
      count += num;
    }

    System.out.println(result);
    System.out.println(count);
  }
}
