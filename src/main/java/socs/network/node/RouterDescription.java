package socs.network.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates the information of a router.
 * RouterDescription is uniquely identified by processIPAddress, processPortNumber, and simulatedIPAddress.
 * It applies the Flyweight pattern to ensure the uniqueness of RouterDescription object.
 */

public class RouterDescription {
  // Map to store unique instances of RouterDescription
  private static final Map<String, RouterDescription> instances = new HashMap<>();

  //used to socket communication
  private final String processIPAddress;
  private final int processPortNumber;
  //used to identify the router in the simulated network space
  private final String simulatedIPAddress;
  //status of the router
  private RouterStatus status;

  private RouterDescription(String processIPAddress, int processPortNumber, String simulatedIPAddress) {
    this.processIPAddress = processIPAddress;
    this.processPortNumber = processPortNumber;
    this.simulatedIPAddress = simulatedIPAddress;
  }

  // factory method to create and reuse the router description with null status by default
  public static RouterDescription getInstance(String processIPAddress, int processPortNumber,
                                              String simulatedIPAddress) {
    String key = processIPAddress + ":" + processPortNumber + ":" + simulatedIPAddress;
    return instances.computeIfAbsent(key, k -> new RouterDescription(processIPAddress, processPortNumber,
      simulatedIPAddress));
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RouterDescription that = (RouterDescription) o;
    return processPortNumber == that.processPortNumber && Objects.equals(processIPAddress, that.processIPAddress)
      && Objects.equals(simulatedIPAddress, that.simulatedIPAddress) && status == that.status;
  }

  @Override
  public String toString() {
    return "RouterDescription{" +
      "processIPAddress='" + processIPAddress + '\'' +
      ", processPortNumber=" + processPortNumber +
      ", simulatedIPAddress='" + simulatedIPAddress + '\'' +
      ", status=" + status +
      '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(processIPAddress, processPortNumber, simulatedIPAddress, status);
  }
}
