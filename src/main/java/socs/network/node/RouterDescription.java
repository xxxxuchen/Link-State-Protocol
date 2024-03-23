package socs.network.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates the information of a router.
 * It is an immutable class, thread synchronization is not needed.
 * It applies the Flyweight pattern to ensure the uniqueness of instances.
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
  private final RouterStatus status;

  private RouterDescription(String processIPAddress, int processPortNumber, String simulatedIPAddress,
                            RouterStatus status) {
    this.processIPAddress = processIPAddress;
    this.processPortNumber = processPortNumber;
    this.simulatedIPAddress = simulatedIPAddress;
    this.status = status;
  }

  // factory method to create and reuse the router description with INIT status by default
  public static RouterDescription getInstance(String processIPAddress, int processPortNumber,
                                              String simulatedIPAddress) {
    String key = processIPAddress + ":" + processPortNumber + ":" + simulatedIPAddress + ":" + RouterStatus.INIT;
    return instances.computeIfAbsent(key, k -> new RouterDescription(processIPAddress, processPortNumber,
      simulatedIPAddress, RouterStatus.INIT));
  }


  // overloaded factory method to create and reuse the router description with a specific status
  public static RouterDescription getInstance(String processIPAddress, int processPortNumber,
                                              String simulatedIPAddress, RouterStatus status) {
    String key = processIPAddress + ":" + processPortNumber + ":" + simulatedIPAddress + ":" + status;
    return instances.computeIfAbsent(key, k -> new RouterDescription(processIPAddress, processPortNumber,
      simulatedIPAddress, status));
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

  public RouterStatus getStatus() {
    return status;
  }

  // it returns a new instance with a changed status without mutate the state of the object
  public RouterDescription changedStatus(RouterStatus status) {
    return getInstance(this.processIPAddress, this.processPortNumber, this.simulatedIPAddress, status);
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
