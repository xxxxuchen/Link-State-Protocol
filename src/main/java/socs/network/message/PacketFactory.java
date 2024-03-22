package socs.network.message;

import socs.network.node.RouterDescription;

public class PacketFactory {
  public static final short HELLO = 0;
  public static final short LSAUPDATE = 1;

  public static SOSPFPacket createHelloPacket(RouterDescription src, RouterDescription dst, String neighborIP) {
    SOSPFPacket packet = initPacket(src, dst, HELLO);
    packet.neighborID = neighborIP;
    return packet;
  }

  public static SOSPFPacket createLSAUpdatePacket(RouterDescription src, RouterDescription dst) {
    SOSPFPacket packet = initPacket(src, dst, LSAUPDATE);
    // TODO: Add LSA array
    return packet;
  }

  private static SOSPFPacket initPacket(RouterDescription src, RouterDescription dst, short type) {
    SOSPFPacket packet = new SOSPFPacket();
    packet.srcProcessIP = src.getProcessIP();
    packet.srcProcessPort = src.getProcessPort();
    packet.srcIP = src.getSimulatedIP();
    packet.dstIP = dst.getSimulatedIP();
    packet.sospfType = type;
    packet.routerID = src.getSimulatedIP();
    return packet;
  }

}
