package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * No synchronization lock is needed for this class
 * the synchronization of all the underlying data structures and references are already ensured
 */
public class LinkStateDatabase {

  // originated router's simulated IP => LSAInstance
  private final Map<String, LSA> _store = new ConcurrentHashMap<String, LSA>();

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

  //initialize the link state database by adding an entry about the router itself
  private LSA initLinkStateDatabase(RouterDescription rd) {
    LSA lsa = new LSA(rd.getSimulatedIP());
    LinkDescription ld = new LinkDescription(rd.getSimulatedIP(), -1);
    lsa.links.add(ld);
    return lsa;
  }

  // add a link description to its (router) own LSA, and increment the sequence number
  public void addLinkDescription(String neighborIP) {
    int portNum = router.getOutgoingPort(neighborIP);
    LinkDescription ld = new LinkDescription(neighborIP, portNum);
    LSA lsa = _store.get(router.getDescription().getSimulatedIP());
    lsa.lsaSeqNumber.incrementAndGet();
    lsa.links.add(ld);
  }

  // add or update a given LSA in the database
  public boolean updateLSA(LSA lsa) {
    if (_store.containsKey(lsa.linkStateID) &&
      lsa.lsaSeqNumber.get() <= _store.get(lsa.linkStateID).lsaSeqNumber.get()) {
      return false;
    }
    _store.put(lsa.linkStateID, lsa);
    return true;
  }

  // get all the connected neighbors which their status has already been set to TWO_WAY
  public RouterDescription[] getConnectedNeighbors() {
    LSA lsa = _store.get(router.getDescription().getSimulatedIP());
    ArrayList<RouterDescription> neighbors = new ArrayList<>();
    for (LinkDescription ld : lsa.links) {
      // exclude the router itself
      if (!ld.linkID.equals(router.getDescription().getSimulatedIP())) {
        neighbors.add(RouterDescription.getInstance(ld.linkID));
      }
    }
    return neighbors.toArray(new RouterDescription[0]);
  }

  public Vector<LSA> getAllLSAs() {
    return new Vector<>(_store.values());
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
