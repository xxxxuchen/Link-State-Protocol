package socs.network.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A map to store the port number of a router given its Simulated IP address.
 * It uses a ConcurrentHashMap, which is thread safe.
 */
public class IP2PortMap {

  private static final Map<String, Integer> ip2PortMap = new ConcurrentHashMap<>();


  public static void add(String ip, int port) {
    ip2PortMap.put(ip, port);
  }

  public static int get(String ip) {
    return ip2PortMap.get(ip);
  }

}
