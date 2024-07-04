# Communication: 
TCP-client and Pipe-client is located under `./communication/`. 

### TcpClient.java
This class utilize TCP-client sockets to communicate with the Tcp-server. Main methods are as following:

**start(), run(), tearDown():**

```java
    
public class TcpClient {

    private final String host;
    private final int port;
    private volatile boolean running = false; // state flag to expose the state of the service-thread
    private final Object lock = new Object(); // lock object used for synchronization between threads
    private DataOutputStream writer; //writer stream to write to the socket
    private DataInputStream reader; // reader stream to read from the socket
    private Socket axSocket; // the communication socket, used by the service-thread
    private Thread axService; // service-thread i.e. worker thread

    private int connectionTimeout; // connection timeout value
    private int readTimeout; // reading timeout


    /**
     * @throws IllegalArgumentException if passing invalid arguments to it
     * i.e. hostName == null || hostName.isBlank() || (0>port>65535)
     */
    public TcpClient(String hostName, int port, int connectionTimeout, int readTimeout) {
        if (hostName == null || hostName.isBlank()) {
            throw new IllegalArgumentException("invalid host, null or blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        this.port = port;
        this.host = hostName;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

     

    // Other implementations ...
    
    
    /**
     * Start the service and keep the connection alive until user interrupts it
     * @throws RuntimeException when TcpClient is already started and start being called again,
     * or if starting service fail
     */
    public void start() {
        synchronized (lock) {
            if (isRunning()) { // check if the server is already in use
                throw new RuntimeException("TcpClient already running!");
            }
            this.axService = new Thread(this::run);
            axService.start();
            while (!isRunning()) { // sleep until the server starts
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("failed when waiting to running state", e);
                }
            }
        }
    }

     // Other implementations ...

     // Run and initialize the socket, output and input, and keep the connection alive until terminating by user
    private void run() {
        try (Socket s = new Socket()) {
            this.axSocket = s;
            this.axSocket.connect(new InetSocketAddress(host, port), connectionTimeout); // wait, or exception if timeout
            this.axSocket.setSoTimeout(readTimeout); // read timeout

            try (
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.axSocket.getOutputStream()));
                    DataInputStream in = new DataInputStream(new BufferedInputStream(this.axSocket.getInputStream()))
            ) {
                this.writer = out;
                this.reader = in;
                synchronized (lock) {
                    running = true;
                    lock.notifyAll(); // notify for and wake up the thread in start()
                }
                while (isRunning()) {
                    synchronized (lock) {
                        lock.wait(); // Efficiently wait while running, sleep
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            return;
        }
    }


    /**
     * Interrupt the connection, shut down the service
     * @throws  RuntimeException if closing resource fail
     */
    public synchronized void tearDown() {
        synchronized (lock) {
            this.running = false;
            if(this.axSocket != null){
                try{
                    this.axSocket.close(); // close the resource to force worker-thread interrupt
                }catch (IOException e){
                    throw new RuntimeException("failed when closing the axSocket",e);
                }
            }
            if(this.axService != null){
                axService.interrupt(); // send kill signal
            }
            lock.notifyAll(); // Notify waiting thread to wake up
        }
    }

}

```

### WPC
This class is the implementation of client side of the pipe server. 



# Tests
Alla test suites are placed under `./test/`


# util
All utility classes are under `./utils/`.