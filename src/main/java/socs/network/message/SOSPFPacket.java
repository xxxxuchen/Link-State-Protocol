package socs.network.message;

import java.io.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class SOSPFPacket implements Serializable {

  //for inter-process communication
  public String srcProcessIP;
  public int srcProcessPort;

  //simulated IP address
  public String srcIP; // the originator of this packet
  public String dstIP;

  //common header
  public short sospfType; //0 - HELLO, 1 - LinkState Update
  public String routerID; // sender of the packet
  public String neighborID; //neighbor's simulated IP address

  //used by LSAUPDATE
  public Vector<LSA> lsaArray = null;

}
