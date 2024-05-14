using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework.Legacy;
using ServerSim.utils;

namespace ServerSim.test.white_box.RequestHandlerTest
{
    /// <summary>
    /// This class tests correctness of the RequestHandler by testing the behavior of methods  
    /// provided by this class, i.e. Behavioral testing. 
    /// </summary>
    public class RequestHandlerTests
    {
        private static RequestHandler requestHandler = new RequestHandler();

        /// <summary>
        /// This is a caller method, the only public method of this class, 
        /// which is responsible to run all test cases. 
        /// </summary>
        public static void RunRequestHandlerTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>()
            {
                { "InvalidKeyRequestBehaviorTest", InvalidKeyRequestBehaviorTest },
                { "ValidPostRequestBehaviorTest", ValidPostRequestBehaviorTest },
                { "ValidGetRequestBehaviorTest", ValidGetRequestBehaviorTest },
                { "ValidRequestPosAndGetSameReqIdBehaviorTest", ValidRequestPosAndGetSameReqIdBehaviorTest }
            };

            Console.WriteLine("######################################### Going through RequestHandlerTests ######################################");

            foreach (string key in FunctionHashMap.Keys)
            {
                Console.WriteLine($"starting ----------->{key}<-----------------");
                FunctionHashMap[key]();
                Console.WriteLine($"-------------------->{key}<-----------passed\n\n");
            }
        }

        

        /// <summary>
        /// Tests the behavior of RequestHandler when passing invalid key or invalid data
        /// Expected behavior: The main method of RequestHandler i.e. HandleRequest should return the bad request message 
        /// </summary>
        private static void InvalidKeyRequestBehaviorTest()
        {
            // invalid auth-key.................................................................
            string Request = "\"auth-key\": \". . . . #\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\":, \"pressure\":\" \"}";
            string Response = requestHandler.HandleRequest( Request );

            // Assert invalid auth-key
            ClassicAssert.AreEqual(Response, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"404 bad request\"}}");

            // invalid method, reqId.............................................................
            Request = "\"auth-key\": \"secKey123#\", \"method\": \"Post\", \"reqId\": \"setPressure\", \"body\": {\"tankId\": 10, \"pressure\":\"200 Pa \"}";
            Response = requestHandler.HandleRequest(Request);

            // Assert invalid method
            ClassicAssert.AreEqual(Response, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"404 bad request\"}}");

            Request = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPåKaffe\", \"body\": {\"tankId\": 10, \"pressure\":\"200 Pa \"}";
            Response = requestHandler.HandleRequest(Request);

            // Assert invalid reqId
            ClassicAssert.AreEqual(Response, "{\"reqId\": \"setPåKaffe\", \"body\": {\"status-code\": \"404 bad request\"}}");

            // valid request
            Request = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\": 10, \"pressure\":\"200 Pa \"}";
            Response = requestHandler.HandleRequest(Request);
            
            // Assert valid request
            ClassicAssert.AreEqual(Response, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}");
        }

        /// <summary>
        /// Tests the behavior of RequestHandler when requesting  valid POST requests by multiple threads
        /// Expected behavior: Each request should result a valid response
        /// </summary>
        private static void ValidPostRequestBehaviorTest()
        {
            List<string> responses = new List<string>();
            List<Thread> threads = new List<Thread>();

            // a valid post request
            string Request = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\": 10, \"pressure\":\"200 Pa \"}";
            for (int i = 0; i<10; i++)
            {
                Thread t = new Thread(() => { responses.Add(requestHandler.HandleRequest(Request)); });
                threads.Add(t);
            }

            // run all threads
            threads.ForEach(t => { t.Start(); });
            threads.ForEach(t => { t.Join(); });    

            ClassicAssert.IsTrue( responses.Count == 10  && threads.Count == 10);

            // check validity of resposnes 
            responses.ForEach(response => { 
                ClassicAssert.AreEqual(response, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}"); 
            });
        }

        /// <summary>
        /// Tests the behavior of RequestHandler when requesting valid GET requests by multiple threads
        /// Expected behavior: Each request should result a valid response
        /// </summary>
        private static void ValidGetRequestBehaviorTest()
        {
            List<string> responses = new List<string>();
            List<Thread> threads = new List<Thread>();

            // a valid post request
            string Request = "\"auth-key\": \"secKey123#\", \"method\": \"GET\", \"reqId\": \"getTemp\", \"body\": {}";
            for (int i = 0; i < 10; i++)
            {
                Thread t = new Thread(() => { responses.Add(requestHandler.HandleRequest(Request)); });
                threads.Add(t);
            }

            // run all threads
            threads.ForEach(t => { t.Start(); });
            threads.ForEach(t => { t.Join(); });

            ClassicAssert.IsTrue(responses.Count == 10 && threads.Count == 10);

            // check validity of resposnes 
            responses.ForEach(response => {
                ClassicAssert.AreEqual(response, "{\"reqId\": \"getTemp\", \"body\": {\"temp\": \"25°C\", \"status-code\": \"200 ok\"}}");
            });
        }



        /// <summary>
        /// Requesting  valid POST and then GET request with same reId, checkt the post-method by requesting GET same data
        /// Expected behavior: the returned value should be a valid response to the actual request
        /// </summary>
        private static void ValidRequestPosAndGetSameReqIdBehaviorTest()
        {
            for (int i = 0; i<100; i++)
            {
                int testID = i + 2;
                int testValue = (i + 1) * 60;

                // post
                string Request = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\": " + testID + ", \"pressure\":\"" + testValue +  "Pa \"}";
                ClassicAssert.AreEqual(requestHandler.HandleRequest(Request), "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}");
                
                // get to check if previous valid post was correctly done
                Request = "\"auth-key\": \"secKey123#\", \"method\": \"GET\", \"reqId\": \"getPressure\", \"body\": {\"tankId\": " + testID + "}";
                string Response = requestHandler.HandleRequest(Request);
                
                ClassicAssert.AreEqual(Response, "{\"reqId\": \"getPressure\", \"body\": {\"tankId\": " + testID + ", \"pressure\":\"" + testValue +"Pa\", \"status-code\": \"200 ok\"}}");
            }
        }

    }
}
