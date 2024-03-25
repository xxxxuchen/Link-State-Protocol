package socs.network.message;

import socs.network.node.LinkStateDatabase;
import socs.network.node.Node;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;

public class LSAUpdateHandler extends AbstractMsgHandler {

  private SOSPFPacket packet;

  public LSAUpdateHandler(Node node, LinkStateDatabase lsd) {
    super(node, lsd);
  }

  @Override
  public void handleMessage(SOSPFPacket pkt) {
    this.packet = pkt;
    super.handleMessage(packet);
    // update lsd
    for (LSA lsa : packet.lsaArray) {
      lsd.updateLSA(lsa);
    }
    // broadcast the LSAUpdate packet to all connected neighbors except the one that sent the packet
    broadcastLSAUpdate();
  }

  @Override
  protected boolean broadcastCondition(RouterDescription neighbor) {
    return !neighbor.getSimulatedIP().equals(packet.srcIP) && neighbor.getStatus() == RouterStatus.TWO_WAY;
  }

}
