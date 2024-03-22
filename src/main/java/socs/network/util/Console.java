package socs.network.util;

public class Console {

  public static void log(String str) {
    synchronized (Console.class) {
      System.out.println(str);
      System.out.print(">> ");
    }
  }
}
