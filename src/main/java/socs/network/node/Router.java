package socs.network.node;

import socs.network.message.*;
import socs.network.sockets.SocketClient;
import socs.network.sockets.SocketServer;
import socs.network.util.Configuration;
import socs.network.util.Console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

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

  public static volatile boolean readingConfirmation = false; // indicate if it is asking for user confirm

  private AttachRequestStatus attachRequestStatus = AttachRequestStatus.NULL;

  public Router(Configuration config) {
    String simulatedIP = config.getString("socs.network.router.ip");
    int processPort = config.getInt("socs.network.router.port");
    rd = RouterDescription.getInstance("127.0.0.1", processPort, simulatedIP);
    Console.log(rd.toString(), false);
    lsd = new LinkStateDatabase(this);
    packetListener = new PacketListener();
    packetListener.start();
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
  public RouterDescription[] getAttachedNeighbors() {
    synchronized (portsLock) {
      ArrayList<RouterDescription> neighbors = new ArrayList<>();
      for (int i = 0; i < ports.length; i++) {
        if (ports[i] != null) {
          neighbors.add(ports[i].router2);
        }
      }
      return neighbors.toArray(new RouterDescription[0]);
    }
  }

  @Override
  public RouterDescription getDescription() {
    return rd;
  }

  @Override
  public void addAttachedLink(Link link) {
    synchronized (portsLock) {
      if (link == null) {
        // the attach request has been rejected
        attachRequestStatus = AttachRequestStatus.REJECTED;
        portsLock.notify();
        return;
      }
      // request has been accepted
      attachRequestStatus = AttachRequestStatus.ACCEPTED;
      for (int i = 0; i < ports.length; i++) {
        if (ports[i] == null) {
          ports[i] = link;
          break;
        }
      }
      portsLock.notifyAll();
    }
  }

  // remove the attached link and also reset all the related status of this router
  @Override
  public void removeAttachedLink(int portNumber) {
    synchronized (portsLock) {
      if (ports[portNumber] == null) {
        return;
      }
      // reset the neighbor's status, this is necessary because the lifetime of this RouterDescription instance
      // is the same as the router's lifetime, and this connection might be re-established in the future
      ports[portNumber].router2.setStatus(RouterStatus.NULL);
      // reset the attach request status
      attachRequestStatus = AttachRequestStatus.NULL;
      ports[portNumber] = null;
    }
  }

  private void broadcastPacket(short type) {
    synchronized (portsLock) {
      for (Link link : ports) {
        if (link != null) {
          SOSPFPacket pkt = type == 0 ? PacketFactory.createHelloPacket(rd, link.router2, link.router2.getSimulatedIP())
            : PacketFactory.createLSAUpdatePacket(rd, link.router2, lsd.getAllLSAs());
          sendPacket(pkt, link.router2);
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
    // Ensure the destination IP is not the router's own IP
    if (destinationIP.equals(this.rd.getSimulatedIP())) {
      Console.log("The destination IP matches the router's own IP.", false);
      return;
    }

    String path = lsd.getShortestPath(destinationIP);
    Console.log(path, false);
  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(int portNumber) {
    synchronized (portsLock) {
      if (portNumber < 0 || portNumber >= ports.length) {
        Console.log("Invalid neighbor", false);
        return;
      }
      if (ports[portNumber] == null) {
        Console.log("No link exists at port " + portNumber, false);
        return;
      }
      // remove its neighbor's link description from its own LSA and remove its own link description from the
      // neighbor's LSA
      lsd.removeLinkDescriptions(ports[portNumber].router2.getSimulatedIP());
      // send the LSAUpdate packet to all neighbors to synchronize the changes
      broadcastPacket(PacketFactory.LSAUPDATE);
      // remove the attached link from the ports array
      removeAttachedLink(portNumber);
    }
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP) {
    synchronized (portsLock) {
      boolean linkExist =
        Arrays.stream(ports).anyMatch(link -> link != null && link.router2.getSimulatedIP().equals(simulatedIP));
      if (simulatedIP.equals(rd.getSimulatedIP()) || linkExist) {
        Console.log("link already exists", false);
        attachRequestStatus = AttachRequestStatus.REJECTED;
        portsLock.notify();
        return;
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
      if (attachRequestStatus != AttachRequestStatus.ACCEPTED) {
        Console.log("You cannot start the router before a successful attachment!", false);
        return;
      }
      broadcastPacket(PacketFactory.HELLO);
    }
  }

  private void startSynchronously() throws InterruptedException {
    synchronized (portsLock) {
      while (attachRequestStatus == AttachRequestStatus.NULL) {
        // wait for the user to response the attach request
        portsLock.wait();
      }
      if (attachRequestStatus == AttachRequestStatus.REJECTED) {
        Console.log("You cannot start the router before a successful attachment!", false);
        return;
      }
      broadcastPacket(PacketFactory.HELLO);
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
    attachRequestStatus = AttachRequestStatus.NULL;
    this.processAttach(processIP, processPort, simulatedIP);
    try {
      // processStart will wait for the user to accept the request
      this.startSynchronously();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    RouterDescription[] connectedNeighbors = lsd.getConnectedNeighbors();
    Console.log("\n  Port\tIP address", false);
    for (RouterDescription neighbor : connectedNeighbors) {
      Console.log("  " + neighbor.getProcessPort() + "\t" + neighbor.getSimulatedIP(), false);
    }
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  /*
  private void processQuit() {
    Console.log("Initiating router shutdown process.", false);

    SOSPFPacket departurePacket = new SOSPFPacket();
    departurePacket.srcIP = rd.getSimulatedIP();
    departurePacket.sospfType = 1;
    departurePacket.lsaArray = new Vector<LSA>(lsd.getAllLSAs());
    //

    // Remove the link descriptions from LSD for all connected neighbors before sending departure packets
    synchronized (portsLock) {
      for (Link link : ports) {
        if (link != null && link.router2 != null) {
          // Remove the link description from the LSD
          lsd.removeLinkDescriptions(link.router2.getSimulatedIP());

          // Then, attempt to notify the neighbor of departure
          try {
            SocketClient client = new SocketClient(link.router2.getProcessIP(), link.router2.getProcessPort());
            client.send(departurePacket);
            client.close();
          } catch (Exception e) {
            Console.log("Failed to send departure packet to " + link.router2.getSimulatedIP(), false);
          }
        }
      }
    }

    // Clear the ports to signify no longer connected to any neighbors
    for (int i = 0; i < ports.length; i++) {
      ports[i] = null;
    }
    // Terminate the packet listener and interrupt all channel threads
    packetListener.terminate();

    Console.log("Router has been successfully shut down.", false);
    System.exit(0);
  }
   */
  private void processQuit() {
    // remove the link descriptions from LSD for all connected neighbors
    synchronized (portsLock) {
      for (Link link : ports) {
        if (link != null) {
          lsd.removeLinkDescriptions(link.router2.getSimulatedIP());
        }
      }
      // send the LSAUpdate packet to all neighbors after the lsd has updated all the link changes
      broadcastPacket(PacketFactory.LSAUPDATE);
    }
    // Terminate the packet listener and interrupt all channel threads
    packetListener.terminate();
    // remove all the attached links
    for (int i = 0; i < ports.length; i++) {
      removeAttachedLink(i);
    }
    Console.log("Successfully shut down the router.", false);
    System.exit(0);
  }


  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (readingConfirmation) {
          // reading user confirmation on attach request
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
          int portNumber = getOutgoingPort(cmdLine[1]);
          processDisconnect(portNumber);
        } else if (command.startsWith("quit")) {
          isReader.close();
          br.close();
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
            cmdLine[3]);
        } else if (command.equals("start")) {
          Console.log("Starting the router", false);
          processStart();
        } else if (command.startsWith("connect ")) {
          System.out.println("connecting");
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
            cmdLine[3]);
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          Console.log("Invalid command: " + command, false);
        }
        System.out.print("\n>> ");
        command = br.readLine();
      }
    } catch (
      Exception e) {
      e.printStackTrace();
    }
  }

  private class PacketListener extends Thread {

    private final ArrayList<Thread> ChannelThreads = new ArrayList<>();
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

    @Override
    public void start() {
      // register hello message handler
      handlers[0] = new HelloHandler(Router.this, lsd);
      // register LSAUpdate message handler
      handlers[1] = new LSAUpdateHandler(Router.this, lsd);
      super.start();
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
