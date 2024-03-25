package socs.network.message;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LSA implements Serializable {

  //IP address of the router originate this LSA
  public String linkStateID;
  public int lsaSeqNumber = Integer.MIN_VALUE;

  public ConcurrentLinkedDeque<LinkDescription> links = new ConcurrentLinkedDeque<LinkDescription>();

  public LSA(String linkStateID) {
    this.linkStateID = linkStateID;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(linkStateID + ":").append(lsaSeqNumber + "\n");
    for (LinkDescription ld : links) {
      sb.append(ld);
    }
    sb.append("\n");
    return sb.toString();
  }
}
