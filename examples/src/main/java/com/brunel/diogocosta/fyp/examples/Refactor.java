package com.brunel.diogocosta.fyp.examples;

import java.util.Arrays;

public class Refactor {

  public static void main(String[] args) {
    String[] name = { "Diogo", "Costa" };

    Arrays.stream(name).forEach(string -> {
      System.out.println(string);
    });

    for (String string : name) {
      System.out.println(string);
    }
  }
}
