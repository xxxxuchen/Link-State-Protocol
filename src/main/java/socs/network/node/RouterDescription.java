package socs.network.node;

import java.util.Objects;

public class RouterDescription {
  //used to socket communication
  private String processIPAddress;
  private int processPortNumber;
  //used to identify the router in the simulated network space
  private String simulatedIPAddress;
  //status of the router
  private RouterStatus status;


  public RouterDescription(String processIPAddress, int processPortNumber, String simulatedIPAddress) {
    this.processIPAddress = processIPAddress;
    this.processPortNumber = processPortNumber;
    this.simulatedIPAddress = simulatedIPAddress;
    this.status = RouterStatus.INIT;
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

  public void setProcessPort(int processPortNumber) {
    this.processPortNumber = processPortNumber;
  }

  public void setStatus(RouterStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RouterDescription that = (RouterDescription) o;
    return processPortNumber == that.processPortNumber && Objects.equals(processIPAddress, that.processIPAddress) && Objects.equals(simulatedIPAddress, that.simulatedIPAddress) && status == that.status;
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
