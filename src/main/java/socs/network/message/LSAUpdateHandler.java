package socs.network.message;

import socs.network.node.LinkStateDatabase;
import socs.network.node.Node;

public class LSAUpdateHandler extends AbstractMsgHandler {

  public LSAUpdateHandler(Node node, LinkStateDatabase lsd) {
    super(node, lsd);
  }

  public void handleMessage(SOSPFPacket packet) {
    super.handleMessage(packet);
    // TODO: implement this
  }

}
