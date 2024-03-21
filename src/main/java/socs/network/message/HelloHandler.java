package socs.network.message;

import socs.network.node.*;
import socs.network.util.UserInputUtil;

/**
 * TODO: Thread Safe
 */
public class HelloHandler extends AbstractMsgHandler {

  public HelloHandler(Node node, LinkStateDatabase lsd) {
    super(node, lsd);
  }

  @Override
  public void handleMessage(SOSPFPacket packet) {
    RouterDescription neighbor = router.getNeighborFromLink(packet.neighborID);

    // attach request
    if (neighbor == null) {
      // attach request is sent from the originated neighbor
      if (packet.srcIP.equals(packet.neighborID)) {
        super.handleMessage(packet);
        RouterDescription originatedRouter = RouterDescription.getInstance("127.0.0.1", packet.srcProcessPort, packet.srcIP);
        System.out.print("Do you accept this request?(Y/N)：");
        char yn = UserInputUtil.readConfirmSelection();
        if (yn == 'Y') {
          System.out.println("You have accepted the request.");
          // add the link
          Link link = new Link(router.getDescription(), originatedRouter);
          router.addLink(link);
          // change the neighbor id field to the router's simulated IP
          packet.neighborID = router.getDescription().getSimulatedIP();
          // change the source port field to the router's process port
          packet.srcProcessPort = router.getDescription().getProcessPort();
          // send the hello packet back to the neighbor
          router.sendPacket(packet, originatedRouter);
        } else {
          System.out.println("You have rejected the request.");
          // set the neighbor id field to -1
          packet.neighborID = "-1";
          router.sendPacket(packet, originatedRouter);
        }
      } else {
        // response of attach request from the target neighbor
        if (packet.neighborID.equals("-1")) {
          System.out.println("The request has been rejected.");
        } else {
          System.out.println("The request has been accepted.");
          RouterDescription targetRouter = RouterDescription.getInstance("127.0.0.1", packet.srcProcessPort, packet.neighborID);
          // add the link
          Link link = new Link(router.getDescription(), targetRouter);
          router.addLink(link);
        }

      }
    }

    // TODO: Start Request

  }

}
