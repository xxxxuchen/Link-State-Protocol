package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.HashMap;

public class LinkStateDatabase {

  //linkID => LSAInstance
  private final HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private final Node router;


  public LinkStateDatabase(Node rt) {
    this.router = rt;
    RouterDescription rd = router.getDescription();
    LSA l = initLinkStateDatabase(rd);
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  String getShortestPath(String destinationIP) {
    //TODO: fill the implementation here
    return null;
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase(RouterDescription rd) {
    LSA lsa = new LSA(rd.getSimulatedIP());
    LinkDescription ld = new LinkDescription(rd.getSimulatedIP(), -1);
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa : _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
