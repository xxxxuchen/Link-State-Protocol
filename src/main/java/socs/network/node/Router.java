package socs.network.node;

import socs.network.message.*;
import socs.network.sockets.SocketClient;
import socs.network.sockets.SocketServer;
import socs.network.util.Configuration;
import socs.network.util.Console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The instance of Router class can be shared by multiple channel threads
 * Methods that access or modify the ports array must be protected by synchronization lock
 */
public class Router implements Node {
  private final LinkStateDatabase lsd;

  private final RouterDescription rd;

  //assuming that all routers are with 4 ports
  private final Link[] ports = new Link[4]; // store attached neighbors

  // map message type to message handler, 0 for helloHandler, 1 for LSAUpdateHandler
  private final MessageHandler[] handlers = new MessageHandler[2];

  private final PacketListener packetListener;

  private final Object portsLock = new Object();

  public static volatile boolean readingConfirmation = false;

  public Router(Configuration config) {
    String simulatedIP = config.getString("socs.network.router.ip");
    int processPort = config.getInt("socs.network.router.port");
    rd = RouterDescription.getInstance("127.0.0.1", processPort, simulatedIP);
    Console.log(rd.toString(), false);
    lsd = new LinkStateDatabase(this);
    packetListener = new PacketListener();
    startListener();
  }

  @Override
  public int getOutgoingPort(String simulatedIP) {
    synchronized (portsLock) {
      for (int i = 0; i < ports.length; i++) {
        if (ports[i] != null && ports[i].router2.getSimulatedIP().equals(simulatedIP)) {
          return i;
        }
      }
      return -1;
    }
  }

  @Override
  public RouterDescription[] getAttachedNeighbors() {
    synchronized (portsLock) {
      RouterDescription[] neighbors = new RouterDescription[ports.length];
      for (int i = 0; i < ports.length; i++) {
        if (ports[i] != null) {
          neighbors[i] = ports[i].router2;
        }
      }
      return neighbors;
    }
  }

  @Override
  public RouterDescription getAttachedNeighbor(String simulatedIP) {
    synchronized (portsLock) {
      for (Link link : ports) {
        if (link != null && link.router2.getSimulatedIP().equals(simulatedIP)) {
          return link.router2;
        }
      }
      return null;
    }
  }

  @Override
  public RouterDescription getDescription() {
    return rd;
  }

  @Override
  public void addLink(Link link) {
    synchronized (portsLock) {
      for (int i = 0; i < ports.length; i++) {
        if (ports[i] == null) {
          ports[i] = link;
          break;
        }
      }
    }
  }


  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP) {
    if (simulatedIP.equals(rd.getSimulatedIP())) {
      Console.log("Cannot attach to itself", false);
      return;
    }

    synchronized (portsLock) {
      // check if the link already exists
      for (Link link : ports) {
        if (link != null && link.router2.getSimulatedIP().equals(simulatedIP)) {
          Console.log("Link already exists", false);
          return;
        }
      }
    }

    // send the HELLO packet to the remote router
    RouterDescription attachedRouter = RouterDescription.getInstance(processIP, processPort, simulatedIP);
    SOSPFPacket helloPacket = PacketFactory.createHelloPacket(rd, attachedRouter, simulatedIP);

    sendPacket(helloPacket, attachedRouter);
  }


  /**
   * broadcast Hello to all attached neighbors
   */
  private void processStart() {
    synchronized (portsLock) {
      for (Link link : ports) {
        if (link != null) {
          SOSPFPacket helloPacket =
            PacketFactory.createHelloPacket(rd, link.router2, link.router2.getSimulatedIP());
          sendPacket(helloPacket, link.router2);
        }
      }
    }
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    String neighbors = "";
    RouterDescription[] connectedNeighbors = lsd.getConnectedNeighbors();
    for (RouterDescription neighbor : connectedNeighbors) {
      neighbors += "\n" + neighbor.getSimulatedIP();
    }
    Console.log(neighbors, false);
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {
    // stop all running thread
    packetListener.terminate();
    System.exit(0);
  }


  private void startListener() {
    // register hello message handler
    handlers[0] = new HelloHandler(this, lsd);
    // register LSAUpdate message handler
    handlers[1] = new LSAUpdateHandler(this, lsd);
    packetListener.start();
  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (readingConfirmation) {
          if (command.equals("Y") || command.equals("y")) {
            handlers[0].handleAccept();
          } else if (command.equals("N") || command.equals("n")) {
            handlers[0].handleReject();
          }
          readingConfirmation = false;
        } else if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
            cmdLine[3]);
        } else if (command.equals("start")) {
          Console.log("Starting the router", false);
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
            cmdLine[3]);
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
          // for reading user confirmation on attach request
        } else {
          //invalid command
          Console.log("Invalid command", false);
          break;
        }
        System.out.print("\n>> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (
      Exception e) {
      e.printStackTrace();
    }
  }

  private class PacketListener extends Thread {

    private final List<Thread> ChannelThreads = new ArrayList<>();
    private final SocketServer serverSocket = new SocketServer(rd.getProcessPort());

    @Override
    public void run() {
      while (!this.isInterrupted()) {
        SocketClient clientSocket = serverSocket.accept();
        // create a new thread to handle the incoming message
        Thread channel = new Thread(() -> {
          while (!Thread.currentThread().isInterrupted()) {
            SOSPFPacket packet = clientSocket.receive();
            if (packet != null) {
              // call the corresponding handler callback
              handlers[packet.sospfType].handleMessage(packet);
            }
          }
        });
        ChannelThreads.add(channel);
        channel.start();
      }
    }

    public void terminate() {
      serverSocket.close();
      this.interrupt();
      for (Thread channel : ChannelThreads) {
        channel.interrupt();
      }
    }
  }
}
