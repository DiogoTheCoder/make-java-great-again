package com.brunel.diogocosta.fyp.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Refactor {

  public static void main(String[] args) {
    String[] name1 = { "Diogo", "Costa" };
    List<String> name2 = Arrays.asList("Maria", "Costa");
    ArrayList<String> name3 = new ArrayList<>(List.of("John", "Smith"));

    for (String name : name1) {
      System.out.println(name);
    }
  }
}
