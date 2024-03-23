package socs.network.message;

import socs.network.node.LinkStateDatabase;
import socs.network.node.Node;
import socs.network.util.Console;

public abstract class AbstractMsgHandler implements MessageHandler {
  protected Node router;
  protected LinkStateDatabase lsd;

  protected AbstractMsgHandler(Node node, LinkStateDatabase lsd) {
    this.router = node;
    this.lsd = lsd;
  }

  public void handleMessage(SOSPFPacket packet) {
    String srcSimulatedIP = packet.srcIP;
    String packetType = packet.sospfType == 0 ? "HELLO" : "LSAUPDATE";
    Console.log("Received " + packetType + " packet from " + srcSimulatedIP, true);
  }
}
