package socs.network.node;

import socs.network.message.SOSPFPacket;

public interface Node {

  public void listenPackets();

  public void sendPackets(SOSPFPacket packet, RouterDescription dst);

}
