package com.brunel.diogocosta.fyp.examples;

import java.util.Arrays;
import java.util.List;

public class Refactor {
  public static void main(String[] args) {
    List<String> names = Arrays.asList("Diogo", "Costa");

    for (int i = 0; i < names.size(); i++) {
      System.out.println(names.get(i));
    }

    names.forEach(name -> {
      System.out.println(name);
    });
  }
}