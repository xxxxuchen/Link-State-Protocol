package socs.network.node;

/**
 * Immutable class
 */
public class Link {

  final RouterDescription router1;
  final RouterDescription router2;

  public Link(RouterDescription r1, RouterDescription r2) {
    router1 = r1;
    router2 = r2;
  }

  public String toString() {
    return router1.getSimulatedIP() + " - " + router2.getSimulatedIP();
  }
}
