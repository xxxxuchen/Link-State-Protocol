package socs.network.util;

import java.util.*;

public class UserInputUtil {

  private static Scanner scanner = new Scanner(System.in);


  // used to confirm the user's selection, and return 'Y' or 'N'
  public static char readConfirmSelection() {
    char c;
    for (; ; ) {
      String str = readKeyBoard(1, false).toUpperCase();
      c = str.charAt(0);
      if (c == 'Y' || c == 'N') {
        break;
      } else {
        System.out.print("wrong selection, please select again: ");
      }
    }
    return c;
  }

  private static String readKeyBoard(int limit, boolean blankReturn) {
    String line = "";

    while (scanner.hasNextLine()) {
      line = scanner.nextLine();
      if (line.length() == 0) {
        if (blankReturn) return line;
        else continue;
      }

      if (line.length() < 1 || line.length() > limit) {
        System.out.print("length should be less than " + limit + ", please input again: ");
        continue;
      }
      break;
    }

    return line;
  }
}