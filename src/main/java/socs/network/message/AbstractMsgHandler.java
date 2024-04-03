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
    String srcSimulatedIP = packet.srcIP;
    String packetType = packet.sospfType == 0 ? "HELLO" : "LSAUPDATE";
    Console.log("Received " + packetType + " packet from " + srcSimulatedIP, true);
  }

  // broadcast LSAUpdate packet to connected neighbors according to the broadcast condition
  public final void broadcastLSAUpdate() {
    RouterDescription[] allNeighbors = lsd.getConnectedNeighbors();
    String log = "broadcast LSAUpdate to neighbors: ";
    for (RouterDescription neighbor : allNeighbors) {
      if (neighbor != null && broadcastCondition(neighbor)) {
        SOSPFPacket lsaUpdatePacket = PacketFactory.createLSAUpdatePacket(router.getDescription(), neighbor,
          lsd.getAllLSAs());
        router.sendPacket(lsaUpdatePacket, neighbor);
        log += neighbor.getSimulatedIP() + " ";
      }
    }
    Console.log(log, true);
  }

  protected abstract boolean broadcastCondition(RouterDescription neighbor);

  public void handleAccept() {
    // empty implementation
  }

  public void handleReject(String msg) {
    // empty implementation
  }
}
