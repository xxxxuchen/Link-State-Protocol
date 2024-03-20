package socs.network.sockets;

import socs.network.message.SOSPFPacket;

import java.io.*;
import java.net.Socket;


public class SocketClient {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;


  public SocketClient(String pHost, int pPort) {
    try {
      socket = new Socket(pHost, pPort);
      initStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SocketClient(Socket socket) {
    this.socket = socket;
    try {
      initStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void send(SOSPFPacket packet) {
    try {
      out.writeObject(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SOSPFPacket receive() {
    try {
      Object obj = in.readObject();
      if (obj instanceof SOSPFPacket) {
        return (SOSPFPacket) obj;
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void close() {
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private void initStream() throws IOException {
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
  }

}
