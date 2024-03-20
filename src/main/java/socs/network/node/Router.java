package socs.network.node;

import socs.network.message.HelloHandler;
import socs.network.message.LSAUpdateHandler;
import socs.network.message.MessageHandler;
import socs.network.message.SOSPFPacket;
import socs.network.sockets.SocketClient;
import socs.network.sockets.SocketServer;
import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;


public class Router extends AbstractRouter {

  protected LinkStateDatabase lsd;

  private RouterDescription rd;

  //assuming that all routers are with 4 ports
  private Link[] ports = new Link[4];

  // map message type to message handler
  private MessageHandler[] handlers = new MessageHandler[2];


  public Router(Configuration config) {
    String simulatedIP = config.getString("socs.network.router.ip");
    int processPort = config.getInt("socs.network.router.port");
    rd = new RouterDescription("127.0.0.1", processPort, simulatedIP);
    lsd = new LinkStateDatabase(rd);
    registerHandler();
    listenPackets();
  }

  private void registerHandler() {
    // register hello message handler
    handlers[0] = new HelloHandler(this, lsd);

    // register LSAUpdate message handler
    handlers[1] = new LSAUpdateHandler(this, lsd);
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
      System.out.println("Cannot attach to itself");
      return;
    }

    // check if the link already exists
    for (Link link : ports) {
      if (link != null && link.router2.getSimulatedIP().equals(simulatedIP)) {
        System.out.println("Link already exists");
        return;
      }
    }

    // send the HELLO packet to the remote router


    // if success then create a new link
    RouterDescription attachedRouter = new RouterDescription(processIP, processPort, simulatedIP);
    Link link = new Link(rd, attachedRouter);
    for (int i = 0; i < ports.length; i++) {
      if (ports[i] == null) {
        ports[i] = link;
        break;
      }
    }

  }


  /**
   * process request from the remote router.
   * For example: when router2 tries to attach router1. Router1 can decide whether it will accept this request.
   * The intuition is that if router2 is an unknown/anomaly router, it is always safe to reject the attached
   * request from router2.
   */
//  private void requestHandler() {
//
//  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {

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

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
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
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
            cmdLine[3]);
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void listenPackets() {
    Thread listener = new Thread(() -> {
      SocketServer serverSocket = new SocketServer(rd.getProcessPort());
      while (true) {
        SocketClient clientSocket = serverSocket.accept();
        // create a new thread to handle the incoming message
        Thread channelThread = new Thread(() -> {
          while (true) {
            SOSPFPacket packet = clientSocket.receive();
            if (packet != null) {
              // call the corresponding handler callback
              handlers[packet.sospfType].handleMessage(packet);
            }
          }
        });
        channelThread.start();
      }
    });
    listener.start();

  }
}
