package test.WindowsPipeCommunicatorTestSuite;

import communication.WPC;
import test.CommunicationTestUtil;
import util.KeyValueString;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


/*************************************************************************************************
 *  Behavior Driven Testing (BDT) of sendRequest method in WPC class.        *
 *  Documentation can be found in ./sendRequestTestsDocumentation.md                             *
 *  Uses {@code CommunicationTestUtil} as data pool                                              *
 *  @author Mehdi Haidari                                                                        *
 *                                                                     *
 * ***********************************************************************************************/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class sendRequestTests {

    // storages to be used during test cases
    static HashMap<String, String> actualDataMap, expectedDataMap;

    // storages to store test values
    static private List<String> getRequestList, getExpectedResponseList, postRequestList;
    static  private String key;


    // setUp method, this method will initialize the storages of test values
    @BeforeAll
    static void setUp(){
        getRequestList = CommunicationTestUtil.GET_REQ_LIST;
        getExpectedResponseList = CommunicationTestUtil.GET_RES_LIST;
        key = CommunicationTestUtil.KEY;
        postRequestList = CommunicationTestUtil.POST_REQ_LIST;
    }

    // reset and set up tools before each test
    @BeforeEach
    void resetMaps(){
        actualDataMap = new HashMap<>();
        expectedDataMap = new HashMap<>();
    }



    /////////////////////////////////////////////////////// Timer Behavior Test //////////////////////////////////////////////
    @Order(1)
    @Disabled
        // test case 1:  sending request by a single-thread T, when timer is on and pipe is not available
        // collaboration contract: the server must be unavailable for maximum waiting time t, timer on, maximum waiting time t sec.
        // expected behavior: the method should wait maximum waiting time t, when this time is reached then the method should return the error message. The method should wait at least t seconds
        // result:  passed
    void timeOutTruePipeNotExistsSingleThreadTest(){
        int maxTime = 5;
        String request = KeyValueString.toValidKeyValueRequest(key, "GET", "getUsedSpace", "{}");

        long startTime = System.nanoTime();
        String response = WPC.sendRequestByPipe(request, true, maxTime);
        long endTime = System.nanoTime();

        //System
        assert(response.equals("waiting time is up! please run pipe server and try again!"));

        int durationTimeInSec = (int)((endTime-startTime)/1_000_000_000.0); // double to int
        assert durationTimeInSec == maxTime;
    }


    @Order(2)
    @Disabled
        // test case 2: sending request by multiple-threads {T1,..., Ti} when timer is on and pipe is not available
        // collaboration contract: the server must be unavailable for give maximum waiting time t, timer on, maximum waiting time t sec.
        // expected behavior: after all threads' time have been reached i.e. time ∑ ti, the actual collection must have n error message, where n is number of threads.
        // result: passed
    void timeOutPipeNotExistsMultipleThreadTest(){
        int maxTime = 2;

        List<Thread> threadList = new ArrayList<>();
        for (String s : getRequestList) {
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", s, "{}");
            Thread t = new Thread(() -> actualDataMap.put(s, WPC.sendRequestByPipe(request, true,maxTime)));
            threadList.add(t);
        }
        threadList.forEach(Thread::start);

        long startTime = System.nanoTime();
        //wait util all threads have run
        threadList.forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        long endTime = System.nanoTime();

        assert (actualDataMap.size() == getExpectedResponseList.size() && actualDataMap.size() == getRequestList.size());

        for(String s : actualDataMap.keySet()){
            assert(actualDataMap.get(s).equals("waiting time is up! please run pipe server and try again!"));
        }

        int durationTime = (int) ((endTime-startTime)/1_000_000_000.0);
        int expectedTime = maxTime* getRequestList.size();
        assert durationTime == expectedTime;
    }

    @Order(3)
    @Test
        // test case 3: issue a request when timer is off and server is also off
        // collaboration contract: the server must be unavailable, and it must be available after maximum time has been reached.
        // expected behavior: the request should wait until the server becomes available even if the maximum waiting time being reached
        // result: passed
    void timeOutFalseSingleRequestTest(){
        String request = KeyValueString.toValidKeyValueRequest(key, "GET", getRequestList.getFirst(), "{}");
        String response = KeyValueString.getResponseBody(WPC.sendRequestByPipe(request, false, 1));

        assert(response.equals("{"+ getExpectedResponseList.getFirst() + ", " + CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE + "}"));
    }
    //////////////////////////////////////////////////// End of Timer Behavior Test ////////////////////////////////////////////


    ///////////////////////////////////////////////////// Pipe communication Test valid GET-request //////////////////////////////////////////////////

    @Order(4)
    @Test
        // Test case 4: multiple threads try to request GET different data through communicator at the same time
        // collaboration contract: pipe server, timer is off
        // expected behavior: regardless to the maximum waiting time, the method provides right response to each thread as soon as pipe server becomes available.
        // result: passed
    void multiThreadRequestGetDifferentDataTest(){
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i< getRequestList.size(); i++){
            String s = getRequestList.get(i);
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", s + "\n\r", "{}");
            String expected = getExpectedResponseList.get(i);
            expectedDataMap.put(s, expected);

            Thread t = new Thread(()-> {
                String response = KeyValueString.getResponseBody(WPC.sendRequestByPipe(request, false,50));
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

    @Order(5)
    @Test
        //Test case 5: Multiple threads try to request same data at the same time
        // collaboration contract: pipe server
        // expected behavior: regardless to the maximum waiting time, the method provides right response to each thread as soon as pipe server becomes available.
        // result: passed
    void multiThreadedRequestGetSameDataTest(){
        List<Thread> threadList = new ArrayList<>();
        String request = KeyValueString.toValidKeyValueRequest(key, "GET", "getDatabaseRecords", "{}");
        for (int i = 0; i<10; i++){
            expectedDataMap.put("req" + i , "\"recordsNr\": \"5000 records\"");
            int j = i;
            Thread t = new Thread(()-> {
                String response = KeyValueString.getResponseBody(WPC.sendRequestByPipe(request, false,50));
                actualDataMap.put("req" + j, response);
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
        assert (actualDataMap.size() == expectedDataMap.size() && actualDataMap.size() == 10);

        for(String s : expectedDataMap.keySet()){
            assert(actualDataMap.get(s).equals("{"+ expectedDataMap.get(s) + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE + "}"));
        }
    }

    @Order(6)
    @Test
        // Test case 6: sequential requesting data
        // collaboration contract: pipe server
        // expected behavior: each request should be served
    void sequentialSingleThreadedGetRequestTest(){
        ArrayList<String> responses = new ArrayList<>();
        for (String req : getRequestList){
            String request = KeyValueString.toValidKeyValueRequest(key, "GET", req, "{}");
            String response = KeyValueString.getResponseBody(WPC.sendRequestByPipe(request, false,50));
            responses.add(response);
        }
        assert (responses.size() == getExpectedResponseList.size());
        for(int i = 0; i< getExpectedResponseList.size(); i++){
            String s = responses.get(i);
            //System.out.println(s + ": " + " " + i + " " + getExpectedResponseList.get(i));
            assert(s.equals("{"+ getExpectedResponseList.get(i) + ", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"));
        }
    }

    ///////////////////////////////////////////////////////// End of Pipe communication Test valid GET-request  /////////////////////////////////////////



    ///////////////////////////////////////////////////////// Pipe communication Test valid POST-request ////////////////////////////////////////////////
    @Order(7)
    @Test
    // Test case 7: multiple threads try to request POST different data through communicator at the same time
    // collaboration contract: pipe server, timer is off, DB-server
    // expected behavior: regardless to the maximum waiting time, the method provides right response to each thread as soon as pipe server becomes available.
    // result: passed
    void multiThreadRequestPostDifferentDataTest(){
        List<Thread> threadList = new ArrayList<>();
        for (String s : postRequestList) {
            String reqId = KeyValueString.getValue(s, "reqId"),
                    body = KeyValueString.getValue(s, "body");
            String requestBody = KeyValueString.toValidKeyValueRequest(key, "POST", reqId + "\n\r", body);

            Thread t = new Thread(() -> {
                String response = KeyValueString.getResponseBody(WPC.sendRequestByPipe(requestBody, false, 50));
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
    ///////////////////////////////////////////////////////// End of Pipe communication Test valid POST-request  /////////////////////////////////////////


    ///////////////////////////////////////////////////////// Valid POST AND GET request-Test //////////////////////////////
    @Order(8)
    @Test
    // to proof that data being inserted correctly into db by valid request
    void requestPostAndGetRequest(){
        int testId = 10, testValue = 80;
        String request = KeyValueString.toValidKeyValueRequest(key, "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");

        assert (KeyValueString.getResponseBody(
                WPC.sendRequestByPipe(request, false, 50)
        ).equals(
                CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE
        ));

        request = KeyValueString.toValidKeyValueRequest(key, "GET", "getPressure", "{\"tankId\": "+ testId +"}");

        assert (KeyValueString.getResponseBody(
                WPC.sendRequestByPipe(request, false, 50)
        ).equals(
                "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " +CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}"
        ));
    }

    ///////////////////////////////////////////////////////// Invalid POST and GET requests ////////////////////
    //@Order(9)

    ///////////////////////////////////////////////////////// Invalid input //////////////////////////
    @Order(9)
    @Test
        // Test case 8: The method should throw IllegalArgumentException if input is invalid
        // collaboration contract: none
        // expected behavior: the method should throw IllegalArgumentException if input == null || input.isEmpty || input.isBlank
    void invalidDataRequest(){
        // null input
        assertThrows(IllegalArgumentException.class, ()-> WPC.sendRequestByPipe(null, false,50));

        //empty String input
        assertThrows(IllegalArgumentException.class, ()-> WPC.sendRequestByPipe("", false,50));

        // blank String input
        assertThrows(IllegalArgumentException.class, ()-> WPC.sendRequestByPipe("              ", false,50));

    }

}

