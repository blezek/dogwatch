package com.github.dogwatch;

public class DogWatch {
  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    new DogWatchApplication().run(args);
  }

}
