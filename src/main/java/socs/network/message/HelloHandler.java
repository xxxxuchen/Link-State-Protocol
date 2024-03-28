package socs.network.message;

import socs.network.node.*;
import socs.network.util.Console;

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
    RouterDescription attachedNeighbor = router.getAttachedNeighbor(packet.srcIP);

    // attach request
    if (attachedNeighbor == null) {
      // attach request is sent from the originated neighbor
      if (!packet.srcIP.equals(packet.neighborID)) {
        super.handleMessage(packet);
        Console.logOneLine("Do you accept this request?(Y/N)：");
      } else {
        // response of attach request from the target neighbor
        if (packet.neighborID.equals("-1")) {
          Console.log("The request has been rejected.", true);
        } else {
          Console.log("The request has been accepted.", true);
          RouterDescription targetRouter = RouterDescription.getInstance("127.0.0.1", packet.srcProcessPort,
            packet.srcIP);
          // add the link
          Link link = new Link(router.getDescription(), targetRouter);
          router.addLink(link);
        }
      }
    } else { // start request
      super.handleMessage(packet);
      if (attachedNeighbor.getStatus() == null && !packet.srcIP.equals(packet.neighborID)) {
        attachedNeighbor.setStatus(RouterStatus.INIT);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to INIT", true);
        sendBackHelloPacket();
      } else if (attachedNeighbor.getStatus() == null && packet.srcIP.equals(packet.neighborID)) {
        attachedNeighbor.setStatus(RouterStatus.TWO_WAY);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to TWO_WAY", true);
        lsd.addLinkDescription(attachedNeighbor.getSimulatedIP());
        sendBackHelloPacket();
        broadcastLSAUpdate();
      } else if (attachedNeighbor.getStatus().equals(RouterStatus.INIT) && packet.srcIP.equals(packet.neighborID)) {
        attachedNeighbor.setStatus(RouterStatus.TWO_WAY);
        Console.log("Set " + attachedNeighbor.getSimulatedIP() + " state to TWO_WAY", true);
        lsd.addLinkDescription(attachedNeighbor.getSimulatedIP());
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
    sendBackHelloPacket();
  }

  @Override
  public void handleReject() {
    Console.log("You have rejected the request.", false);
    // set the neighbor id field to -1, indicating the request is rejected
    packet.neighborID = "-1";
    router.sendPacket(packet, originatedRouter);
  }

  private void sendBackHelloPacket() {
    // send the hello packet back to the neighbor
    // set the neighbor id field to its own simulated IP to indicate it's a response message
    SOSPFPacket packet = PacketFactory.createHelloPacket(router.getDescription(), originatedRouter,
      router.getDescription().getSimulatedIP());
    router.sendPacket(packet, originatedRouter);
  }

}


