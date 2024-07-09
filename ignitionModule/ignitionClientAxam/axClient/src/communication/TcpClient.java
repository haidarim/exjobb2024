package communication;

import util.CentralLogger;
import util.Zipper;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * This class is a TCP client service provider. It performs a persistence communication through the TCP socket. Using this class String messages as requests and responses
 * can be sent and respectively received to/from TCP server.
 * The class uses a separate {@code Thread->{}} to perform its tasks (provide the service), in this way it will not block the caller thread, i.e. asynchronous architecture
 * Before sending any message, the {@code start()} method should be called. The connection can be aborted by calling {@code tearDown()}
 * @see #start()
 * @see #send(String)
 * @see #tearDown()
 *
 * @author Mehdi Haidari
 * */
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

            this.axService = new Thread(()->{
                try{
                    run();
                }catch (InterruptedException e){
                    throw new RuntimeException(e.getMessage());
                }
            });
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

    // Run and initialize the socket, output and input, and keep the connection alive until terminating by user
    private void run() throws InterruptedException {
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
            synchronized (lock) {
                running = true;
                lock.notifyAll(); // notify for and wake up the thread in start()
            }
        }
    }


    /**
     * Interrupt the connection, shut down the service
     * @throws  RuntimeException if closing resource fail
     */
    public synchronized void tearDown() {
        synchronized (lock) {
            lock.notifyAll(); // Notify waiting thread to wake up
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

            this.running = false;
        }
    }

    /**
     * @return boolean, which indicates the status of the connection/service
     */
    public boolean isRunning() {
        synchronized (lock) {
            return this.running;
        }
    }

    /**
     * compress the request and return the received response
     * @return String, received response from server
     * @throws RuntimeException if calling this method before starting the TcpClient, i.e. before calling start(),
     * or when any I/O-error occurs
     * @throws IllegalArgumentException when passing invalid string as argument
     */
    public String send(String request) {
        synchronized (lock) {
            if (request == null || request.isBlank()) {
                throw new IllegalArgumentException("Invalid input!");
            }
            if (!isRunning()) {
                throw new RuntimeException("The service is not started yet!");
            }
            try {
                byte[] compressedRequest = Zipper.compress(request); // compressed data and get bytes
                writer.writeInt(Integer.reverseBytes(compressedRequest.length)); // expose the size of data
                writer.write(compressedRequest); // send the actual data
                writer.flush();
                int responseLength = Integer.reverseBytes(reader.readInt()); // get the response length from server
                byte[] compressedResponse = new byte[responseLength]; // to store the response data
                reader.readFully(compressedResponse, 0, responseLength);

                // Decompress the response and return it
                return Zipper.decompress(compressedResponse);
            } catch (IOException e) {
                throw new RuntimeException("Failed when sending request\n" + e.getMessage() + " The error is:"+  e + "\nthe running state is: " + isRunning());
            }
        }
    }


    /**
     * set the connection timeout to the specified value
     * @param connectionTimeout an integer, the timeout value
     * @throws  IllegalArgumentException if the connectionTimeout <= 0
     * */
    public void setConnectionTimeout(int connectionTimeout) {
        if(connectionTimeout<=0){
            throw  new IllegalArgumentException("invalid timeout value");
        }
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * set the reading timeout to the specified value
     * @param readTimeout an integer, the timeout value
     * @throws  IllegalArgumentException if the readTimeout <= 0
     * */
    public void setReadTimeout(int readTimeout) {
        if(readTimeout<=0){
            throw  new IllegalArgumentException("invalid timeout value");
        }
        this.readTimeout = readTimeout;
    }

    /**
     * @return  the connection timeout value
     * */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @return the reading time for connection
     * */
    public int getReadTimeout() {
        return readTimeout;
    }
}