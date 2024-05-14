package Tests.TCPCommunicatorTestSuite;

import Tests.CommunicationTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.TCPCommunicator;

import utils.KeyValueString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author  Mehdi Haidari
 * */
public class TCPCommunicatorBehaviorTests {

    //test variables and collections
    static HashMap<String, String> actualDataMap, expectedDataMap;

    // storages to store test values
    static private List<String> getRequestList, getExpectedResponseList, postRequestList;
    static  private String key;
    private TCPCommunicator tcpCommunicator;


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
        tcpCommunicator = new TCPCommunicator(CommunicationTestUtil.HOST, CommunicationTestUtil.PORT);
    }

    // Test the communicator's behavior when passing invalid arguments to its constructor
    // Expected behavior: the constructor should throw a IllegalArgumentException
    // test's precondition: none
    @Test
    void constructorBehaviorWhenPassingInvalidArgs(){
        // invalid string
        assertThrows(IllegalArgumentException.class, ()-> new TCPCommunicator("" ,8880));
        assertThrows(IllegalArgumentException.class, ()-> new TCPCommunicator("         " ,8880));
        assertThrows(IllegalArgumentException.class, ()-> new TCPCommunicator(null ,8880));
        //invalid port
        assertThrows(IllegalArgumentException.class, ()-> new TCPCommunicator("127.0.0.1", -80));
        assertThrows(IllegalArgumentException.class, ()-> new TCPCommunicator("127.0.0.1", 65536));
    }


    // Test the communicator's behavior when start is not called
    // Expected behavior: using sendRequestByTCP should fail, the method should throw a RuntimeException
    // test's precondition: none
    @Test
    void sendRequestWhenTCPCommunicatorNotStartedBehaviorTest(){
        assertThrows(RuntimeException.class, ()->tcpCommunicator.sendRequestByTCP("hej"));
    }

    // Test the behavior of sendRequestByTCP when passing invalid string as argument
    // Expected behavior: the method should throw a IllegalArgumentException
    // test's preconditions: none
    @Test
    void sendRequestBehaviorWhenPassingInvalidArg(){
        assertThrows(IllegalArgumentException.class, ()->tcpCommunicator.sendRequestByTCP(""));
        assertThrows(IllegalArgumentException.class, ()->tcpCommunicator.sendRequestByTCP("        "));
        assertThrows(IllegalArgumentException.class, ()->tcpCommunicator.sendRequestByTCP(null));
    }

    // Test the communicator's behavior when TCPCommunicator is already started and start being called again
    // Expected behavior: the method should throw a RuntimeException
    // test's precondition: none
    @Test
    void startWhenTCPCommunicatorIsAlreadyStarted(){
        tcpCommunicator.start();
        assertThrows(RuntimeException.class, ()-> tcpCommunicator.start());
    }

    // Test the correctness of value returned by isRunning
    // Expected behavior: the value returned by isRunning should indicate the truth of communicator's status
    // test's precondition: none
    @Test
    void isRunningBehaviorTest(){
        tcpCommunicator.start();
        assertTrue(tcpCommunicator.isRunning());

        tcpCommunicator.tearDown();
        assertFalse(tcpCommunicator.isRunning());
    }

    // Test the behavior of tearDown method
    // Expected behavior: the connection should be aborted immediately  after calling this method.
    // and calling start again should be exception-free
    // test's precondition: none
    @Test
    void tearDownAndRestartBehaviorTest(){
        tcpCommunicator.start();
        tcpCommunicator.tearDown();

        assertFalse(tcpCommunicator.isRunning());

        tcpCommunicator.start();
        assertTrue(tcpCommunicator.isRunning());
    }

    // Test sending valid GET-requests using single-thread i.e. sequential
    // Expected behavior: should result the right response corresponding to the request.
    // test's precondition: the server-side is started
    @Test
    void sendingValidGetRequestBySingleThreadTest(){
        tcpCommunicator.start();
        ArrayList<String> responses = new ArrayList<>();
        for (String req : getRequestList){
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", req, "{}");

            String response = KeyValueString.getResponseBody(tcpCommunicator.sendRequestByTCP(request));

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
        tcpCommunicator.start();
        for (int i = 0; i< getRequestList.size(); i++){
            String s = getRequestList.get(i);
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", s + "\n\r", "{}");
            String expected = getExpectedResponseList.get(i);
            expectedDataMap.put(s, expected);

            Thread t = new Thread(()-> {
                String response = KeyValueString.getResponseBody(tcpCommunicator.sendRequestByTCP(request));
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

    // Test sending valid POSt-requests using multiple-threads
    // Expected behavior: each thread should result the right response corresponding to the request they sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidPostRequestByMultipleThreadsTest(){
        tcpCommunicator.start();
        List<Thread> threadList = new ArrayList<>();
        for (String s : postRequestList) {
            String reqId = KeyValueString.getValue(s, "reqId"),
                    body = KeyValueString.getValue(s, "body");
            String requestBody = KeyValueString.toValidKeyValueRequest(key, "POST", reqId + "\n\r", body);

            Thread t = new Thread(() -> {
                String response = KeyValueString.getResponseBody(tcpCommunicator.sendRequestByTCP(requestBody));
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

    // Test sending valid POST-Request and issue a GET-request to get the result of POST-request in server-side
    // Expected behavior: should result the right response corresponding to the request they sent.
    // test's precondition: the server-side is started
    @Test
    void sendingValidPostAndGetRequestInLoop(){
        tcpCommunicator.start();
        for (int i = 0 ; i <= 30; i++){
            int testId = i +10 , testValue = (i +1) * 50;
            String request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
            String response =  tcpCommunicator.sendRequestByTCP(request);


            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
            ));

            request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
            response =  tcpCommunicator.sendRequestByTCP(request);

            assert(KeyValueString.getResponseBody(
                    response
            ).equals(
                    "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
            ));
        }
    }

    // Test the starting, sending and restarting sequential to check the correctness of the class's behavior
    // the test are done by single thread and sequential because sequential initialization start(), send, and restarting goes faster since threading takes a bit time to get resources
    // Expected behavior: each request should result a valid response.
    // test's precondition:
    @Test
    void startSendAndTearDownSequentialTest(){

    }


    @AfterEach
    void tearDown(){
        tcpCommunicator.tearDown();
        assertFalse(tcpCommunicator.isRunning());
    }
}
