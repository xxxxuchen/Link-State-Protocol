package socs.network.message;

import socs.network.node.*;
import socs.network.util.Console;
import socs.network.util.IP2PortMap;

public class HelloHandler extends AbstractMsgHandler {

  // the initial router that sends the hello packet
  private RouterDescription originatedRouter;
  private SOSPFPacket packet;

  public HelloHandler(Node node, LinkStateDatabase lsd) {
    super(node, lsd);
  }

  @Override
  public void handleMessage(SOSPFPacket pkt) {
    this.packet = pkt;
    this.originatedRouter = RouterDescription.getInstance("127.0.0.1", packet.srcProcessPort,
      packet.srcIP);
    RouterDescription attachedNeighbor = router.getAttachedNeighbor(packet.routerID);

    // attach request
    if (attachedNeighbor == null) {
      // attach request is sent from the originated neighbor
      if (packet.srcIP.equals(packet.routerID)) {
        super.handleMessage(packet);
        Console.logOneLine("Do you accept this request?(Y/N)：");
      } else {
        // response of attach request from the target neighbor
        if (packet.neighborID.equals("-1")) {
          Console.log("The request has been rejected.", true);
        } else {
          Console.log("The request has been accepted.", true);
          int targetPort = IP2PortMap.get(packet.routerID);
          RouterDescription targetRouter = RouterDescription.getInstance("127.0.0.1", targetPort,
            packet.routerID);
          // add the link
          Link link = new Link(router.getDescription(), targetRouter);
          router.addLink(link);
        }
      }
    } else { // start request
      super.handleMessage(packet);
      if (attachedNeighbor.getStatus() == null && packet.srcIP.equals(packet.routerID)) {
        attachedNeighbor.setStatus(RouterStatus.INIT);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to INIT", true);
        // send the hello packet back to the neighbor
        packet.routerID = router.getDescription().getSimulatedIP();
        router.sendPacket(packet, originatedRouter);
      } else if (attachedNeighbor.getStatus() == null && !packet.srcIP.equals(packet.routerID)) {
        attachedNeighbor.setStatus(RouterStatus.TWO_WAY);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to TWO_WAY", true);
        lsd.addLinkDescription(attachedNeighbor.getSimulatedIP());
        // send the hello packet back to the neighbor
        packet.routerID = router.getDescription().getSimulatedIP();
        router.sendPacket(packet, originatedRouter);
        // broadcast the LSAUpdate packet to all connected neighbors
        broadcastLSAUpdate();
      } else if (attachedNeighbor.getStatus().equals(RouterStatus.INIT) && packet.srcIP.equals(packet.routerID)) {
        attachedNeighbor.setStatus(RouterStatus.TWO_WAY);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to TWO_WAY", true);
        lsd.addLinkDescription(attachedNeighbor.getSimulatedIP());
        // broadcast the LSAUpdate packet to all connected neighbors
        broadcastLSAUpdate();
      }
    }
  }

  @Override
  protected boolean broadcastCondition(RouterDescription neighbor) {
    return neighbor.getStatus().equals(RouterStatus.TWO_WAY);
  }

  @Override
  public void handleAccept() {
    Console.log("You have accepted the request.", false);
    // add the link
    Link link = new Link(router.getDescription(), originatedRouter);
    router.addLink(link);
    // changer the router id to its own simulated IP
    // to indicate this is a reply packet
    packet.routerID = router.getDescription().getSimulatedIP();
    // send the hello packet back to the neighbor
    router.sendPacket(packet, originatedRouter);
  }

  @Override
  public void handleReject() {
    Console.log("You have rejected the request.", false);
    // set the neighbor id field to -1, indicating the request is rejected
    packet.neighborID = "-1";
    router.sendPacket(packet, originatedRouter);
  }

}


