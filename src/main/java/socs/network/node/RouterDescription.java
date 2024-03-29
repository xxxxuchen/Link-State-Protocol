package socs.network.node;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates the information of a router.
 * Each RouterDescription object is uniquely identified by simulatedIPAddress.
 * It applies the Flyweight pattern to map the simulatedIPAddress to a unique RouterDescription object.
 */

public class RouterDescription {
  // Map to store unique instances of RouterDescription
  private static final Map<String, RouterDescription> instances = new ConcurrentHashMap<>();

  //used to socket communication
  private final String processIPAddress;
  private final int processPortNumber;
  //used to identify the router in the simulated network space
  private final String simulatedIPAddress;
  //status of the router
  private RouterStatus status = RouterStatus.NULL; // default status is null

  private RouterDescription(String processIPAddress, int processPortNumber, String simulatedIPAddress) {
    this.processIPAddress = processIPAddress;
    this.processPortNumber = processPortNumber;
    this.simulatedIPAddress = simulatedIPAddress;
  }

  // factory method to create the router description with null status by default
  public static RouterDescription getInstance(String processIPAddress, int processPortNumber,
                                              String simulatedIPAddress) {
    return instances.computeIfAbsent(simulatedIPAddress, k -> new RouterDescription(processIPAddress, processPortNumber,
      simulatedIPAddress));
  }

  // map the simulated IP address to the corresponding unique RouterDescription object
  public static RouterDescription getInstance(String simulatedIPAddress) {
    if (instances.containsKey(simulatedIPAddress)) {
      return instances.get(simulatedIPAddress);
    }
    return null;
  }


  public String getProcessIP() {
    return processIPAddress;
  }

  public int getProcessPort() {
    return processPortNumber;
  }

  public String getSimulatedIP() {
    return simulatedIPAddress;
  }

  public synchronized RouterStatus getStatus() {
    return status;
  }

  public synchronized void setStatus(RouterStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Router's Info: " + "\n" +
      "Process Port Number: " + processPortNumber + "\n" +
      "Simulated IP Address: " + simulatedIPAddress + "\n" +
      "=================================================\n";
  }

  @Override
  public int hashCode() {
    return Objects.hash(processIPAddress, processPortNumber, simulatedIPAddress, status);
  }
}
