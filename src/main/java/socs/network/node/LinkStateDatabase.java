package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Map;
import java.util.PriorityQueue;
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
public String getShortestPath(String destinationIP) {
  Map<String, Integer> dist = new HashMap<>(); // Distance from source to each node
  Map<String, String> prev = new HashMap<>(); // Previous node in optimal path from source
  PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());

  // Initialize distances and priority queue
  for (String ip : _store.keySet()) {
    dist.put(ip, Integer.MAX_VALUE);
    prev.put(ip, null);
    pq.offer(new AbstractMap.SimpleEntry<>(ip, Integer.MAX_VALUE));
  }

  // Set the distance for the starting router (itself) to 0
  String startIP = router.getDescription().getSimulatedIP();
  dist.put(startIP, 0);
  pq.offer(new AbstractMap.SimpleEntry<>(startIP, 0));

  while (!pq.isEmpty()) {
    Map.Entry<String, Integer> entry = pq.poll();
    String currentIP = entry.getKey();
    int currentDist = entry.getValue();

    // If we reached the destination or the smallest distance is INFINITY (unreachable), stop
    if (currentIP.equals(destinationIP) || currentDist == Integer.MAX_VALUE) break;

    LSA currentLSA = _store.get(currentIP);
    for (LinkDescription ld : currentLSA.links) {
      String neighborIP = ld.linkID;
      int weight = 1; // Each hop has a weight of 1
      if (dist.get(currentIP) + weight < dist.get(neighborIP)) {
        dist.put(neighborIP, dist.get(currentIP) + weight);
        prev.put(neighborIP, currentIP);
        pq.offer(new AbstractMap.SimpleEntry<>(neighborIP, dist.get(neighborIP)));
      }
    }
  }

  // Reconstruct the path
  if (dist.get(destinationIP) == Integer.MAX_VALUE) {
    return "No path found"; // Destination is unreachable
  }

  LinkedList<String> path = new LinkedList<>();
  for (String at = destinationIP; at != null; at = prev.get(at)) {
    path.addFirst(at);
  }

  return String.join(" -> ", path);
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
