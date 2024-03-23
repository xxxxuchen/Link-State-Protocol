package socs.network.util;

public class Console {

  /**
   * Log a message to the console with carriage return
   *
   * @param str   the message to log
   * @param arrow whether to print the ">> " arrow after the message
   *              set it true if the message is a passive response to network events
   *              set it false if the message is an active request from the user
   */
  public static void log(String str, boolean arrow) {
    synchronized (Console.class) {
      System.out.println(str);
      if (arrow) {
        System.out.print(">> ");
      }
    }
  }

  public static void logOneLine(String str) {
    synchronized (Console.class) {
      System.out.print(str);
    }
  }
}