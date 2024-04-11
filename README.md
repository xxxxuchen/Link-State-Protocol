# Documentation : PA-3 Link State Protocol

## How to compile and run the code

1. Compilation: `mvn compile assembly:single`
2. Initializing a router (adjust the router config file at the end accordingly): `java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router[1-7].conf`

## Testing (Using Handout Example)

In order to test the layout provided within the handout, it is recommended to have have **7 open terminals** since we will need to configure **7 different routers**.

We divide the following section based on what you should be running in each terminal:

**Initiallizing Routers:** Use the same command per terminal but adjust the configuration file accordingly (ex: router1.conf, router2.conf, ..., router7.conf). You will see the **process Port number + simulated IP Address for each**.

Routers 1-7 have simulated addresses and ports; X is the number that is different for each according to router number (i.e. 1 to 7):

```
socs.network.router.ip="192.168.1.X"
socs.network.router.port=300X
```

- **TERMINAL 1:** `java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router1.conf`
- **TERMINAL 2:** `java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router2.conf`
- **......**
- **TERMINAL 7:** `java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router7.conf`

**Connecting Routers (attach and start):** By following the same diagram provided in the handout we need to run the following (after initializing routers):
![Router Layout](images/layout.png)

- **TERMINAL 1:** `connect`
- **TERMINAL 2:** ``
- **......**
- **TERMINAL 7:** ``

## `attach` Command

- We implemented the attach request handler.
- When you use the attach command to attach a router to another router, you need to confirm the request manually
  by typing "Y | y" or "N | n" on the other router's console.
- However, if another router has reached the maximum number of ports (4) available, your attach request will be rejected
  automatically.
- After the request is accepted, you can use the start command to start the router which will start the LSD
  synchronization process.

## `connect` Command (Important)

- Since the connect command is just a combination of an attach command followed by a start command, you still need to
  confirm this request from the other router which you are connecting to. It works exactly the same way as the attach
  command described above, except that you don't need to type start command anymore. It will trigger the LSD
  synchronization directly **after** the request is confirmed.
- It is tricky that LSD update packets must be sent only **after** the request is accepted on the other router. We used
  java thread blocking synchronization technique to achieve this. Specifically, we used the monitor and condition
  variable make thread wait and notify each other. See the code in `socs.network.node.Router.java` for more details.

## Some Examples:

- Attach router1 to router2:
  - type `attach 127.0.0.1 3002 192.168.1.2` on router1's console
- Attach router2 to router3:
  - type `attach 127.0.0.1 3003 192.168.1.3` on router2's console
- Connect router1 to router2:
  - type `connect 127.0.0.1 3002 192.168.1.2` on router1's console
- Disconnect router2 from router1:
  - type `disconnect 192.168.1.2` on router1's console
