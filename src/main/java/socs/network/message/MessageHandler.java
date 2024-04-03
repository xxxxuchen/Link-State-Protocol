package socs.network.message;

public interface MessageHandler {
  public void handleMessage(SOSPFPacket packet);

  public void handleAccept();

  public void handleReject(String msg);
}
