package socs.network.message;

public interface MessageHandler {
  public void handleMessage(SOSPFPacket packet);
}
