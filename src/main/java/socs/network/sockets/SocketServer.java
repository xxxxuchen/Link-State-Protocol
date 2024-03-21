package socs.network.sockets;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

  private ServerSocket serverSocket;

  public SocketServer(int pPort) {
    try {
      serverSocket = new ServerSocket(pPort);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public synchronized SocketClient accept() {
    try {
      Socket socket = serverSocket.accept();
      return new SocketClient(socket);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public int getLocalPort() {
    return serverSocket.getLocalPort();
  }

  public void close() {
    try {
      serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
