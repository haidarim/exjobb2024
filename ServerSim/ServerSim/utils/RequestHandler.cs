using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.utils
{

    //<summary>
    // This class is responsible to handle requests and response correctly to each request. 
    // NOTE: each request should have an unique reqId, since the data structor used is a HashTable
    // therefor function must be grouped under an unique tag
    //</summary>
    public class RequestHandler
    {
        // get request handler map, each func in this map takes a string as @param and results a string 
        private Dictionary<string, Func<string, string>> getRequestsHandlers;

        // post request handler map, each func in this map takes a string as @param and returns a bool as result  
        private Dictionary<string, Func<string,bool>> postRequestsHandlers;
        public  readonly string auth_key = "secKey123#";
        private readonly object _lockObject = new object();

        public RequestHandler()
        {
            // configurring and setting up the request-functions-map
            getRequestsHandlers = new Dictionary<string, Func<string, string>>
            {
                { "getTemp", GetTemp },
                { "getReferenceValue", GetReferenceValue },
                { "getServerName", GetServerName },
                { "getDeviceName", GetDeviceName },
                { "getUsedSpace", GetUsedSpace },
                { "getUserCount", GetUserCount },
                { "getSystemStatus", GetSystemStatus },
                { "getVersionInfo", GetVersionInfo },
                { "getIPAddress", GetIPAddress },
                { "getDiskSpace", GetDiskSpace },
                { "getMemoryUsage", GetMemoryUsage },
                { "getCPUUsage", GetCPUUsage },
                { "getNetworkSpeed", GetNetworkSpeed },
                { "getUserName", GetUserName },
                { "getFileList", GetFileList },
                { "getDirectorySize", GetDirectorySize },
                { "getLatestLog", GetLatestLog },
                { "getSensorData", GetSensorData },
                { "getDatabaseStatus", GetDatabaseStatus },
                { "getWeatherForecast", GetWeatherForecast },
                { "getTrafficInfo", GetTrafficInfo },
                { "getNewsHeadlines", GetNewsHeadlines },
                { "getStockQuote", GetStockQuote },
                { "getCalendarEvents", GetCalendarEvents },
                { "getServerUptime", GetServerUptime },
                { "getSecurityStatus", GetSecurityStatus },
                { "getTaskList", GetTaskList },
                { "getDatabaseRecords", GetDatabaseRecords },
                { "getErrorMessage", GetErrorMessage },
                {"getPressure", GetPressure}
            };

            postRequestsHandlers = new Dictionary<string, Func<string, bool>>
            {
                { "setTankLevel",SetTankLevel },
                { "setPressure", SetPressure },
                { "setTemp",SetTemp},
                { "setSpeed", SetSpeed }
            };

        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        public string HandleRequest(string request)
        {
            lock (_lockObject)
            {
                string mehtod = KeyValueString.GetValue(request, "method");
                string reqId = KeyValueString.GetValue(request, "reqId");
                string requestBody = KeyValueString.GetValue(request, "body");
                string responseBody = "\"status-code\": \"404 bad request\"";

                if (KeyValueString.GetValue(request, "auth-key").Equals(auth_key))
                {
                    if (mehtod.Equals("GET"))
                    {
                        responseBody = HandleGetRequest(reqId, requestBody);
                    }
                    else if (mehtod.Equals("POST"))
                    {
                        responseBody = HandlePostRequest(reqId, requestBody);
                    }
                }

                return "{\"reqId\": \"" + reqId + "\", \"body\": {" + responseBody + "}}";
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="reqId"></param>
        /// <param name="requestBody"></param>
        /// <returns></returns>
        private string HandleGetRequest(string reqId, string requestBody)
        {
            if (getRequestsHandlers.ContainsKey(reqId))
            {
                // calls the method and returns the response 
                return getRequestsHandlers[reqId](requestBody) + ", \"status-code\": \"200 ok\"";
            }

            return "\"status-code\": \"404 bad request\"";
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="reqId"></param>
        /// <param name="requestBody"></param>
        /// <returns></returns>
        private string HandlePostRequest(string reqId, string requestBody)
        {
            if (postRequestsHandlers.ContainsKey(reqId) && postRequestsHandlers[reqId](requestBody))
            {
                // calls the method and returns the response 
                return "\"status-code\": \"201 complete\"";
            }

            return "\"status-code\": \"404 bad request\"";
        }



        private string GetTemp(string requestBody) { return "\"temp\": \"25°C\""; }
        private string GetReferenceValue(string requestBody) { return "\"value\": 100"; }
        private string GetServerName(string requestBody) { return "\"Server Nr\": 10"; }
        private string GetDeviceName(string requestBody) { return "\"deviceName\": \"Crevis CVS\""; }
        private string GetUsedSpace(string requestBody) { return "\"used\": \"20%\", \"free\": \"80%\""; }
        private string GetUserCount(string requestBody) { return "\"c\": 1000"; }
        private string GetSystemStatus(string requestBody) { return "\"status\": \"OK\""; }
        private string GetVersionInfo(string requestBody) { return "\"version\": \"v1.0\""; }
        private string GetIPAddress(string requestBody) { return "\"ip\": \"192.168.1.1\""; }
        private string GetDiskSpace(string requestBody) { return "\"space\": \"50GB\""; }
        private string GetMemoryUsage(string requestBody) { return "\"memory\": \"60%\""; }
        private string GetCPUUsage(string requestBody) { return "\"cpu\": \"30%\""; }
        private string GetNetworkSpeed(string requestBody) { return "\"rate\": \"100 Mbps\""; }
        private string GetUserName(string requestBody) { return "\"user\": \"John Doe\""; }
        private string GetFileList(string requestBody) { return "\"files\": [file1.txt, file2.doc]"; }
        private string GetDirectorySize(string requestBody) { return "\"size\": \"1GB\""; }
        private string GetLatestLog(string requestBody) { return "\"last-log\": {Log entry: \"...\"}"; }
        private string GetSensorData(string requestBody) { return "\"Sensor data\" : \"...\""; }
        private string GetDatabaseStatus(string requestBody) { return "\"status\": \"Connected\""; }
        private string GetWeatherForecast(string requestBody) { return "\"weather\": \"Sunny\""; }
        private string GetTrafficInfo(string requestBody) { return "\"traffic-info\": \"No traffic congestion\""; }
        private string GetNewsHeadlines(string requestBody) { return "\"Breaking news\": \"...\""; }
        private string GetStockQuote(string requestBody) { return "\"p\": \"$100.00\""; }
        private string GetCalendarEvents(string requestBody) { return "\"events\":[Event1, Event2]"; }
        private string GetServerUptime(string requestBody) { return "\"up-time\": \"2 days\""; }
        private string GetSecurityStatus(string requestBody) { return "\"sec-status\": \"Secure\""; }
        private string GetTaskList(string requestBody) { return "\"tanks\": [Task1, Task2]"; }
        private string GetDatabaseRecords(string requestBody) { return "\"recordsNr\": \"5000 records\""; }
        private string GetErrorMessage(string requestBody) { return "\"ErrorMessage\" : \"Something went wrong\""; }




        private string GetPressure(string requestBody)
        {
            string tankId = KeyValueString.GetValue(requestBody, "tankId");
            return "\"tankId\": "+ tankId + ", \"pressure\":" + "\"" + DBConn.GetData($"Tank:{tankId}:pressure") + "\"";
        }

        private bool SetTankLevel(string requestBody)
        {
            string tankId = KeyValueString.GetValue(requestBody, "tankId");
            string level = KeyValueString.GetValue(requestBody, "level");
            return DBConn.SetData($"Tank:{tankId}:level", level);
        }

        private bool SetPressure(string requestBody)
        {
            string tankId = KeyValueString.GetValue(requestBody, "tankId");
            string pressure = KeyValueString.GetValue(requestBody, "pressure");
            return DBConn.SetData($"Tank:{tankId}:pressure", pressure);
        }

        private bool SetTemp(string requestBody)
        {
            string deviceId = KeyValueString.GetValue(requestBody, "deviceId");
            string temp = KeyValueString.GetValue(requestBody, "temp");
            return DBConn.SetData($"Device:{deviceId}:temp", temp);
        }

        private bool SetSpeed(string requestBody)
        {
            string motorId = KeyValueString.GetValue(requestBody, "motorId");
            string speed = KeyValueString.GetValue(requestBody, "speed");
            return DBConn.SetData($"Motor:{motorId}:speed", speed);
        }
    }
}
