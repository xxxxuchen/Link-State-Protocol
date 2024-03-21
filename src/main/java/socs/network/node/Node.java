package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.sockets.SocketClient;

public interface Node {

  RouterDescription[] getNeighbors();

  public RouterDescription getNeighborFromLink(String simulatedIP);

  public void addLink(Link link);

  public void setStatus(RouterStatus status);

  public RouterDescription getDescription();

  default void sendPacket(SOSPFPacket packet, RouterDescription dst) {
    String hostIP = dst.getProcessIP();
    int port = dst.getProcessPort();
    SocketClient clientSocket = new SocketClient(hostIP, port);
    clientSocket.send(packet);
    clientSocket.close();
  }
}
