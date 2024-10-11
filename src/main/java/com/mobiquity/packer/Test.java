package com.mobiquity.packer;

import com.mobiquity.exception.APIException;

public class Test {

  public static void main(String[] args) {
    try {
      String result = Packer.pack("example_input");
      System.out.println(result);
    } catch (APIException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }


}
