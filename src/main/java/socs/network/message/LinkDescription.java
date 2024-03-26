package socs.network.message;

import java.io.Serializable;

public class LinkDescription implements Serializable {
  public final String linkID; // connected neighbor's simulated IP address
  public final int portNum;

  public LinkDescription(String linkID, int portNum) {
    this.linkID = linkID;
    this.portNum = portNum;
  }

  public String toString() {
    return linkID + "," + portNum;
  }
}
