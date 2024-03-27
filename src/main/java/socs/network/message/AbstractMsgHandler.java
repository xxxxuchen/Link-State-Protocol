package socs.network.message;

import socs.network.node.LinkStateDatabase;
import socs.network.node.Node;
import socs.network.node.RouterDescription;
import socs.network.util.Console;

public abstract class AbstractMsgHandler implements MessageHandler {
  protected final Node router;
  protected final LinkStateDatabase lsd;

  protected AbstractMsgHandler(Node node, LinkStateDatabase lsd) {
    this.router = node;
    this.lsd = lsd;
  }

  // log the basic information of received packet
  public void handleMessage(SOSPFPacket packet) {
    String srcSimulatedIP = packet.routerID;
    String packetType = packet.sospfType == 0 ? "HELLO" : "LSAUPDATE";
    Console.log("Received " + packetType + " packet from " + srcSimulatedIP, true);
  }

  protected final void broadcastLSAUpdate() {
    RouterDescription[] allNeighbors = router.getAttachedNeighbors();
    for (RouterDescription neighbor : allNeighbors) {
      if (broadcastCondition(neighbor)) {
        SOSPFPacket lsaUpdatePacket = PacketFactory.createLSAUpdatePacket(router.getDescription(), neighbor,
          lsd.getAllLSAs());
        router.sendPacket(lsaUpdatePacket, neighbor);
      }
    }
  }

  protected abstract boolean broadcastCondition(RouterDescription neighbor);

  public void handleAccept() {
    // empty implementation
  }

  public void handleReject() {
    // empty implementation
  }
}
