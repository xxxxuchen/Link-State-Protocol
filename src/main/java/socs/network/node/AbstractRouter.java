package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.sockets.SocketClient;

public abstract class AbstractRouter implements Node {

  public void sendPackets(SOSPFPacket packet, RouterDescription dst) {
    String hostIP = dst.getProcessIP();
    int port = dst.getProcessPort();
    SocketClient clientSocket = new SocketClient(hostIP, port);
    clientSocket.send(packet);
    clientSocket.close();
  }
}
