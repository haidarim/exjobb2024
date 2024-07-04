package test.apiTest;

import communication.TcpClient;
import test.CommunicationTestUtil;
import util.CentralLogger;
import util.KeyValueString;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Logger;



/**
 * testing the Api used between client and the server
 * @author  Mehdi Haidari
 * */

public class ApiTest {

    private static  final Logger log = Logger.getLogger(ApiTest.class.getSimpleName());
    private static final TcpClient tcpClient = new TcpClient(CommunicationTestUtil.HOST, CommunicationTestUtil.PORT,4000, 40000);
    private static String loadTextFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            CentralLogger.logInfo("Failed when testing data consistency: " + e.getMessage(), log);
            throw new RuntimeException("very pain", e);
        }
        return sb.toString();
    }

    private static double nanoToSec (long n){
        return (double) (n/1_000_000_000);
    }

    private static  void printTimeTakenForNRequest(int N){
        long totalElapsedTime = 0;
        String request, response;
        for (int i = 0; i<=N; i++){
            int testId = i+ 1, testValue = 2*i;
            request = KeyValueString.toValidKeyValueRequest("secKey123#", "POST", "setPressure", "{\"tankId\":"+ testId + " , \"pressure\": \" "+ testValue + " Pa\"}");
            getRttTime(request, CommunicationTestUtil.POST_SUCCESSFUL_STATUS_CODE);

            request = KeyValueString.toValidKeyValueRequest("secKey123#", "GET", "getPressure", "{\"tankId\": "+ testId +"}");
            totalElapsedTime += getRttTime(request, "{\"tankId\": " + testId +", \"pressure\":\""+ testValue + " Pa\", " + CommunicationTestUtil.GET_SUCCESSFUL_STATUS_CODE +"}");
        }
        System.out.println("Total time for "+ N + " requests: "+ nanoToSec(totalElapsedTime) + " sec");
        System.out.println("average time "+ nanoToSec(totalElapsedTime)/N + " sec");
    }

    private static long getRttTime(String request, String expectedResponse) {
        long start = System.nanoTime();
        String response = tcpClient.send(request);
        long end = System.nanoTime();

        if (!KeyValueString.getResponseBody(
                response
        ).equals(
                expectedResponse
        )) {
            System.out.println(KeyValueString.getResponseBody(response));
            System.out.println(expectedResponse);
            throw new AssertionError("wrong result when Geting request");
        }
         return  end-start;
    }

    public static void main(String[] args) {
        tcpClient.start();
        //printTimeTakenForNRequest(10000);

        String loadedData = loadTextFile("./axClient/src/test/textfile/10MB.txt");
        System.out.println("time taken for size: "+ loadedData.length()+" is: "+ nanoToSec(getRttTime(KeyValueString.toValidKeyValueRequest("secKey123#", "TEST", "test", "{"+ loadedData+"}"), "{\"status\": \"TEST-OK\"}")) + "sec");

        tcpClient.tearDown();
    }
}
