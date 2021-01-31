package com.brunel.diogocosta.fyp.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Refactor {

  public static void main(String[] args) {
    String[] name1 = { "Diogo", "Costa" };
    List<String> name2 = Arrays.asList("Maria", "Costa");
    ArrayList<String> name3 = new ArrayList<>(List.of("John", "Smith"));

    for (int i = name3.size(); i > 0; i--) {
      System.out.println(name2.get(i - 1));
    }
  }
}
