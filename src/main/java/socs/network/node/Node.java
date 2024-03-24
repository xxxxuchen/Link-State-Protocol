package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.sockets.SocketClient;

public interface Node {

  RouterDescription[] getAttachedNeighbors();

  public RouterDescription getAttachedNeighbor(String simulatedIP);

  public int getOutgoingPort(String simulatedIP);

  public void addLink(Link link);

  public RouterDescription getDescription();

  default void sendPacket(SOSPFPacket packet, RouterDescription dst) {
    String hostIP = dst.getProcessIP();
    int port = dst.getProcessPort();
    SocketClient clientSocket = new SocketClient(hostIP, port);
    clientSocket.send(packet);
    clientSocket.close();
  }
}
