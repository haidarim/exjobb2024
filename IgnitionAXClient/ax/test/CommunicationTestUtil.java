package ax.test;

import java.util.Arrays;
import java.util.List;

public class CommunicationTestUtil {

    public static final String KEY = "secKey123#";
    public static final List<String> GET_REQ_LIST = Arrays.asList(
            "getTemp", "getReferenceValue", "getServerName", "getDeviceName",
            "getUsedSpace", "getUserCount", "getSystemStatus", "getVersionInfo",
            "getIPAddress", "getDiskSpace", "getMemoryUsage", "getCPUUsage",
            "getNetworkSpeed", "getUserName", "getFileList", "getDirectorySize",
            "getLatestLog", "getSensorData", "getDatabaseStatus", "getWeatherForecast",
            "getTrafficInfo", "getNewsHeadlines", "getStockQuote", "getCalendarEvents",
            "getServerUptime", "getSecurityStatus", "getTaskList", "getDatabaseRecords", "getErrorMessage"
    ), GET_RES_LIST = Arrays.asList(
            "\"temp\": \"25°C\"", "\"value\": 100", "\"Server Nr\": 10", "\"deviceName\": \"Crevis CVS\"", "\"used\": \"20%\", \"free\": \"80%\"", "\"c\": 1000",
            "\"status\": \"OK\"", "\"version\": \"v1.0\"", "\"ip\": \"192.168.1.1\"","\"space\": \"50GB\"", "\"memory\": \"60%\"", "\"cpu\": \"30%\"", "\"rate\": \"100 Mbps\"", "\"user\": \"John Doe\"",
            "\"files\": [file1.txt, file2.doc]", "\"size\": \"1GB\"", "\"last-log\": {Log entry: \"...\"}", "\"Sensor data\" : \"...\"", "\"status\": \"Connected\"", "\"weather\": \"Sunny\"",
            "\"traffic-info\": \"No traffic congestion\"", "\"Breaking news\": \"...\"", "\"p\": \"$100.00\"", "\"events\":[Event1, Event2]", "\"up-time\": \"2 days\"",
            "\"sec-status\": \"Secure\"", "\"tanks\": [Task1, Task2]", "\"recordsNr\": \"5000 records\"", "\"ErrorMessage\" : \"Something went wrong\""
    ), POST_REQ_LIST = Arrays.asList("\"reqId\": \"setTankLevel\", \"body\": {\"tankId\": 10, \"level\": \"25%\"}", "\"reqId\": \"setPressure\", \"body\": {\"tankId\": 10, \"pressure\": \"40 Pa\"}", "\"reqId\": \"setTemp\", \"body\": {\"deviceId\": 10, \"temp\": \"48 C\"}", "\"reqId\": \"setSpeed\", \"body\": {\"motorId\": 10, \"speed\": \"50 Hz\"}");
    public static final String POST_SUCCESSFUL_STATUS_CODE = "{\"status-code\": \"201 complete\"}",
    GET_SUCCESSFUL_STATUS_CODE = "\"status-code\": \"200 ok\"";


    public static final String  HOST = "127.0.0.1";
    public static final int PORT = 8080;



    // TODO: tester för tcp och dela tester i flera behavior under samma suite <--------------------
    //TODO tester for toValidKeyValueRequest + getResponseBody
    // TODO ändra/skriv test docs


    // TODO: server sidan
    // stort test för request hanlder och olika beteende

    // test för tcp kommunication och pipe kommunikation är DONE via BDT i client sidan


    public String className(){
        return getClass().getSimpleName();
    }
    public static void main(String[] args) {
        CommunicationTestUtil ctu = new CommunicationTestUtil();
        System.out.println(ctu.className());
    }
}
