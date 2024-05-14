package ax.src;

import ax.util.KeyValueString;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * This class acts as TCPClient and perform a persistence communication  through TCP. Using this class String messages can be sent to the server.
 * The class uses a separate {@code Thread->{}} to perform its tasks, in this way it will not block the caller thread
 * gives back the control to the caller Thread.
 * Before sending any message, the {@code start()} method should be called. The connection can be aborted by calling {@code tearDown()}
 * @see #start()
 * @see #sendRequestByTCP(String)
 * @see #tearDown()
 *
 * @author Mehdi Haidari
 * */
public class TCPCommunicator {

    private volatile boolean running = false;
    private final String host;
    private final int port;
    private volatile boolean  writerAndReaderReady = false;

    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * @throws IllegalArgumentException if passing invalid arguments to it
     * i.e. hostName == null || hostName.isBlank() || (0>port>65535)
     * */
    public TCPCommunicator(String hostName, int port){
        if(hostName == null ||hostName.isBlank()){
            throw new IllegalArgumentException("invalid host, null or blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        this.port = port;
        this.host = hostName;
    }


    /**
     * start the service and keep the connection alive until user interrupts it
     * @throws  RuntimeException when TCPCommunicator is already started and start being called again,
     * or if there is any I/O-error
     * */
    public void start(){
        if(running){
            throw new RuntimeException("TCPCommunicator already running!");
        }
        this.running = true;
        new Thread(this::run).start();
    }

    // run and initialize the socket, output and input, and keep the connection alive until terminating by user
    private void run (){
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            this.writer = out;
            this.reader = in;
            writerAndReaderReady = true;
            while (true){
                if(!this.running){
                    break;
                }
            }
            writerAndReaderReady = false;
        } catch (UnknownHostException e) {
            throw new RuntimeException("unknown host: " + host);
        } catch (IOException e) {
            throw new RuntimeException("connection failed " + host);
        }
    }


    /**
     * interrupt the connection
     * */
    public void tearDown(){
        this.running = false;
    }

    /**
     * @return  boolean, which indicates the status of the connection/service
     * */
    public boolean isRunning(){
        return this.running;
    }


    /**
     * @throws RuntimeException if calling this method before starting the TCPCommunicator, i.e. before calling start(),
     * or when any I/O-error occurs
     * @throws IllegalArgumentException when passing invalid string as argument
     * */
    public synchronized String sendRequestByTCP(String request){
        if (request == null ||request.isBlank()) {
            throw new IllegalArgumentException("invalid input!");
        }
        if(!running){
            throw new RuntimeException("the service is not started yet!");
        }
        String cleanedRequest = request.replaceAll("\n", " ").replaceAll("\r", " ");

        try{
            while (!writerAndReaderReady){continue;}
            // send data to server
            writer.write(cleanedRequest+ "\n");
            writer.flush();

            // response from server
            return reader.readLine();
        }catch (IOException e){
            throw new RuntimeException("failed when sending request\n" + request);
        }
    }

    /*
    /**
     * Decorator pattern
     *
    public synchronized static String sendRequestByTCP(String request){
        if (request == null ||request.isBlank()) {
            throw new IllegalArgumentException("invalid input!");
        }

        String server = "127.0.0.1";
        int serverPort = 8080;

        try (Socket socket = new Socket(server, serverPort);
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String cleanedRequest = request.replaceAll("\n", " ").replaceAll("\r", " ");
            // send data to server
            out.write(cleanedRequest+ "\n");
            out.flush();

            // response from server
            String response = in.readLine();

            //close resources before returning
            socket.close();
            out.close();
            in.close();

            return response;
        } catch (UnknownHostException e) {
            throw new RuntimeException("unknown host: " + server);
        } catch (IOException e) {
            throw new RuntimeException("connection failed " + server);
        }
    }
    */


    public static void main(String[] args) {
        TCPCommunicator tcpCommunicator = new TCPCommunicator("127.0.0.1", 8080);
        tcpCommunicator.start();
        //tcpCommunicator.stop();

        //communicator.stop();
        int testId = 10, testValue = 1500;
        Scanner scanner = new Scanner(System.in);

        // Kontinuerligt läs in data från användaren
        while (true) {
            System.out.println("Ange requestID för exit type exit ");
            String input = scanner.nextLine();

            // Avsluta loopen om användaren anger "exit"
            if (input.equals("exit")) {
                tcpCommunicator.tearDown();
                System.out.println("break from thread   "+ tcpCommunicator.isRunning());
                break;
            }

            String request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", input, "{\"tankId\": "+ testId +"}");
            String response =  tcpCommunicator.sendRequestByTCP(request);
            System.out.println(response);
            /*if(!response.equals(
                    "{\"reqId\":\"" + input + "\", " + CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
            )){
                throw new AssertionError("wrong result when Geting request");
            }*/
        }

        // Stäng scanner när den inte längre behövs
        scanner.close();


        /*String request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");

        String response =  TCPCommunicator.sendRequestByTCP(request);

        System.out.println(response);
        assert (KeyValueString.getResponseBody(
                WindowsPipeCommunicator.sendRequestByPipe(request, false, 50)
        ).equals(
                CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
        ));

        request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");

        assert (KeyValueString.getResponseBody(
                WindowsPipeCommunicator.sendRequestByPipe(request, false, 50)
        ).equals(
                "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
        ));

        response =  TCPCommunicator.sendRequestByTCP(request);
        System.out.println(response);*/

        /*
        for (int i = 0 ; i <= 30; i++){
            request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
            response =  TCPCommunicator.sendRequestByTCP(request);


            if(!KeyValueString.getResponseBody(
                    TCPCommunicator.sendRequestByTCP(request)
            ).equals(
                    CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
            )){
                throw new AssertionError("wrong result when Posting request");
            }

            request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
            response =  TCPCommunicator.sendRequestByTCP(request);

            if(!KeyValueString.getResponseBody(
                    TCPCommunicator.sendRequestByTCP(request)
            ).equals(
                    "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
            )){
                throw new AssertionError("wrong result when Geting request");
            }
        }*/
    }
}
