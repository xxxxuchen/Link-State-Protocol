package socs.network.message;

import socs.network.node.LinkStateDatabase;
import socs.network.node.Node;
import socs.network.node.RouterDescription;

public class LSAUpdateHandler extends AbstractMsgHandler {
  private SOSPFPacket packet;

  public LSAUpdateHandler(Node node, LinkStateDatabase lsd) {
    super(node, lsd);
  }

  @Override
  public void handleMessage(SOSPFPacket pkt) {
    this.packet = pkt;
    super.handleMessage(packet);
    boolean isUpdate = false;
    // update all lsd in its own link state database
    for (LSA lsa : packet.lsaArray) {
      if (lsd.updateLSA(lsa)) {
        isUpdate = true;
      }
    }
    // broadcast the LSAUpdate packet if there is an LSA update
    if (isUpdate) {
      broadcastLSAUpdate();
    }
  }

  @Override
  protected boolean broadcastCondition(RouterDescription neighbor) {
    // do not send the packet back to the neighbor that sent it
    return !neighbor.getSimulatedIP().equals(packet.srcIP);
  }

  @Override
  public String toString() {
    return "LSAUpdateHandler";
  }
}
