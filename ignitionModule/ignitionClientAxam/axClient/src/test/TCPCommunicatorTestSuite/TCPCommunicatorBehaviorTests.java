package test.TCPCommunicatorTestSuite;

import test.CommunicationTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import communication.TcpClient;

import util.KeyValueString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test class to check and test behavior of the TcpClient in different scenario
 * @author  Mehdi Haidari
 * */
public class TCPCommunicatorBehaviorTests {

    //test variables and collections
    static HashMap<String, String> actualDataMap, expectedDataMap;

    // storages to store test values
    static private List<String> getRequestList, getExpectedResponseList, postRequestList;
    static  private String key;
    private TcpClient TcpClient;
    private final int timeout = 5000;


    // set up, initialize tests' tools
    @BeforeAll
    static void setUp(){
        getRequestList = CommunicationTestUtil.GET_REQ_LIST;
        getExpectedResponseList = CommunicationTestUtil.GET_RES_LIST;
        key = CommunicationTestUtil.KEY;
        postRequestList = CommunicationTestUtil.POST_REQ_LIST;
    }

    // reset collections before each test case as well as communicator
    @BeforeEach
    void resetMaps(){
        actualDataMap = new HashMap<>();
        expectedDataMap = new HashMap<>();
        TcpClient = new TcpClient(CommunicationTestUtil.HOST, CommunicationTestUtil.PORT, timeout, timeout);
    }

    // Test the communicator's behavior when passing invalid arguments to its constructor
    // Expected behavior: the constructor should throw a IllegalArgumentException
    // test's precondition: none
    @Test
    void constructorBehaviorWhenPassingInvalidArgs(){
        // invalid string
        assertThrows(IllegalArgumentException.class, ()-> new TcpClient("" ,8880, timeout, timeout));
        assertThrows(IllegalArgumentException.class, ()-> new TcpClient("         " ,8880, timeout,timeout));
        assertThrows(IllegalArgumentException.class, ()-> new TcpClient(null ,8880, timeout, timeout));
        //invalid port
        assertThrows(IllegalArgumentException.class, ()-> new TcpClient("127.0.0.1", -80, timeout, timeout));
        assertThrows(IllegalArgumentException.class, ()-> new TcpClient("127.0.0.1", 65536, timeout, timeout));
    }


    // Test the communicator's behavior when start is not called
    // Expected behavior: using sendRequestByTCP should fail, the method should throw a RuntimeException
    // test's precondition: none
    @Test
    void sendRequestWhenTCPCommunicatorNotStartedBehaviorTest(){
        assertThrows(RuntimeException.class, ()->TcpClient.send("hej"));
    }

    // Test the behavior of sendRequestByTCP when passing invalid string as argument
    // Expected behavior: the method should throw a IllegalArgumentException
    // test's preconditions: none
    @Test
    void sendRequestBehaviorWhenPassingInvalidArg(){
        assertThrows(IllegalArgumentException.class, ()->TcpClient.send(""));
        assertThrows(IllegalArgumentException.class, ()->TcpClient.send("        "));
        assertThrows(IllegalArgumentException.class, ()->TcpClient.send(null));
    }

    // Test the communicator's behavior when TcpClient is already started and start being called again
    // Expected behavior: the method should throw a RuntimeException
    // test's precondition: none
    @Test
    void startWhenTCPCommunicatorIsAlreadyStarted(){
        TcpClient.start();
        assertThrows(RuntimeException.class, ()-> TcpClient.start());
    }

    // Test the correctness of value returned by isRunning
    // Expected behavior: the value returned by isRunning should indicate the truth of communicator's status
    // test's precondition: none
    @Test
    void isRunningBehaviorTest(){
        TcpClient.start();
        assertTrue(TcpClient.isRunning());

        TcpClient.tearDown();
        assertFalse(TcpClient.isRunning());
    }

    // Test the behavior of tearDown method
    // Expected behavior: the connection should be aborted immediately  after calling this method.
    // and calling start again should be exception-free
    // test's precondition: none
    @Test
    void tearDownAndRestartBehaviorTest(){
        TcpClient.start();
        TcpClient.tearDown();

        assertFalse(TcpClient.isRunning());

        TcpClient.start();
        assertTrue(TcpClient.isRunning());
    }

    // Test sending valid GET-requests using single-thread i.e. sequential
    // Expected behavior: should result the right response corresponding to the request.
    // test's precondition: the server-side is started
    @Test
    void sendingValidGetRequestBySingleThreadTest(){
        TcpClient.start();
        ArrayList<String> responses = new ArrayList<>();
        for (String req : getRequestList){
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", req, "{}");

            String response = KeyValueString.getResponseBody(TcpClient.send(request));

            responses.add(response);
        }
        assert (responses.size() == getExpectedResponseList.size());
        for(int i = 0; i< getExpectedResponseList.size(); i++){
            String s = responses.get(i);
            //System.out.println(s + ": " + " " + i + " " + getExpectedResponseList.get(i));
            assert(s.equals("{"+ getExpectedResponseList.get(i) + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"));
        }
    }

    // Test sending valid GET-requests using multiple-threads
    // Expected behavior: each thread should result the right response corresponding to the request they sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidGetRequestByMultipleThreadsTest(){
        List<Thread> threadList = new ArrayList<>();
        TcpClient.start();
        for (int i = 0; i< getRequestList.size(); i++){
            String s = getRequestList.get(i);
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", s + "\n\r", "{}");
            String expected = getExpectedResponseList.get(i);
            expectedDataMap.put(s, expected);

            Thread t = new Thread(()-> {
                String response = KeyValueString.getResponseBody(TcpClient.send(request));
                actualDataMap.put(s, response);
            });
            threadList.add(t);
        }
        threadList.forEach(Thread::start);

        //wait util all threads have run
        threadList.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assert (actualDataMap.size() == expectedDataMap.size() && actualDataMap.size() == getExpectedResponseList.size());

        for(String s : expectedDataMap.keySet()){
            assert(actualDataMap.get(s).equals("{"+ expectedDataMap.get(s) + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE + "}"));
        }
    }

    // Test sending valid POST-requests using multiple-threads
    // Expected behavior: each thread should result the right response corresponding to the request they sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidPostRequestByMultipleThreadsTest(){
        TcpClient.start();
        List<Thread> threadList = new ArrayList<>();
        for (String s : postRequestList) {
            String reqId = KeyValueString.getValue(s, "reqId"),
                    body = KeyValueString.getValue(s, "body");
            String requestBody = KeyValueString.toValidKeyValueRequest(key, "POST", reqId + "\n\r", body);

            Thread t = new Thread(() -> {
                String response = KeyValueString.getResponseBody(TcpClient.send(requestBody));
                actualDataMap.put(reqId, response);
            });
            threadList.add(t);
        }
        threadList.forEach(Thread::start);

        //wait util all threads have run
        threadList.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assert (actualDataMap.size() == postRequestList.size());

        for(String s : expectedDataMap.keySet()){
            assert(actualDataMap.get(s).equals(CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE));
        }
    }

    // Test sending valid POST-Request and issue a GET-request to get the result of POST-request in server-side by a single thread
    // Expected behavior: should result the right response corresponding to the request they sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidPostAndGetRequestInLoopSingleThread(){
        TcpClient.start();
        int numberOfRequests = 100;
        for (int i = 0 ; i <= numberOfRequests; i++){
            int testId = i +10 , testValue = (i +1) * 50;
            String request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
            String response =  TcpClient.send(request);


            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
            ));

            request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
            response =  TcpClient.send(request);

            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
            ));
        }
    }

    // Test sending valid POST-Request and issue a GET-request to get the result of POST-request in server-side by multiple threads
    // Expected behavior: should result the right response corresponding to the request each thread sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidPostAndGetRequestInLoopMultipleThreads(){
        TcpClient.start();
        int numberOfRequests = 100, threadsNumber = 20;
        List<Thread> threadsPool = new ArrayList<>();
        for (int i = 0; i <= threadsNumber; i++){
            threadsPool.add(new Thread(()-> {
                for (int j = 0 ; j <= numberOfRequests; j++){
                    int testId = j +10 , testValue = (j +1) * 50;
                    String request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
                    String response =  TcpClient.send(request);


                    assert(KeyValueString.getResponseBody(
                            response
                    ).equals(
                            CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
                    ));

                    request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
                    response =  TcpClient.send(request);

                    assert(KeyValueString.getResponseBody(
                            response
                    ).equals(
                            "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
                    ));
                }
            }));
        }

        threadsPool.forEach(Thread::start);
        //wait util all threads have run
        threadsPool.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Test the starting, sending and restarting sequential to check the correctness of the class's behavior
    // the test are done by single thread and sequential because sequential initialization start(), send, and restarting goes faster since threading takes a bit time to get resources
    // Expected behavior: each request should result a valid response.
    // test's precondition:
    @Test
    void startSendAndTearDownSequentialTest(){
        for (int i = 0 ; i <= 30; i++){
            int testId = i +10 , testValue = (i +1) * 50;

            String request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
            TcpClient.start();
            String response =  TcpClient.send(request);
            TcpClient.tearDown();

            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
            ));

            request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
            TcpClient.start();
            response =  TcpClient.send(request);
            TcpClient.tearDown();

            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
            ));
        }
    }

    // Test Multiple instance of TcpClient associated to each Thread
    // N Request Concurrency with M threads
    @Test
    void testMultipleInstance(){ // 10 threads each 10 req == 100 req
        int threadNum = 40;
        int messageNr = 100; // number of message each thread send
        List<TcpClient> clients = new ArrayList<>();
        List<ArrayList<String>> listOfResponseList = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i<threadNum; i++){
            TcpClient client = new TcpClient(CommunicationTestUtil.HOST, CommunicationTestUtil.PORT, timeout, timeout);
            clients.add(client);
            listOfResponseList.add(new ArrayList<>());
            client.start();
        }

        for (int i = 0; i < threadNum; i++){
            int finalI = i;
            threads.add(new Thread(()->{
                for(int j = 0; j<messageNr; j++){
                    String response = clients.get(finalI).send(KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getTemp", "{\"tankId\":"+ "10" + " , \"pressure\": \" "+ 100 + " Pa\"}"));
                    listOfResponseList.get(finalI).add(response);
                }
            }));
        }

        threads.forEach(Thread::start);
        threads.forEach(t->{
            try {
                t.join();
            }catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        });

        assert(threads.size() == threadNum && clients.size() == threads.size());
        assert(listOfResponseList.size()==threadNum);
        listOfResponseList.forEach(list -> {assert (list.size() == messageNr);
            list.forEach(s->{
                System.out.println(KeyValueString.getResponseBody(s)+ "<----- actual");
                System.out.println("{"+ getExpectedResponseList.getFirst() + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE + "}" + "<----- expected");
                assert(KeyValueString.getResponseBody(s).equals("{"+ getExpectedResponseList.getFirst() + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE + "}"));});
        });

        clients.forEach(communication.TcpClient::tearDown);
    }


    @AfterEach
    void tearDown(){
        TcpClient.tearDown();
        assertFalse(TcpClient.isRunning());
    }
}