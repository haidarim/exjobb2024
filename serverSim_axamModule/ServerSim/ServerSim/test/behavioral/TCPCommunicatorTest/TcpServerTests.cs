using log4net;
using NUnit.Framework;
using NUnit.Framework.Legacy;
using ServerSim.Log;
using ServerSim.utils;
using System.Net.Sockets;
using System.Reflection.PortableExecutable;

namespace ServerSim.test.behavioral.TCPCommunicatorTests
{
    
   /// <summary>
   ///  This class tests functionality corectness of the TCPCommunicator by testing methods and the behavior of
   ///  TCPCommunicator, i.e. Behavioral testing. 
   /// </summary>
    public class TcpServerTests
    {
        private static readonly ILog _log = LogManager.GetLogger(typeof(TcpServerTests));
        private static readonly string TestIp = "127.0.0.1";


        /// <summary>
        /// This is a caller method, the only public method of this class, 
        /// which is responsible to run all test cases. 
        /// </summary>
        public static void RunTCPCommunicatorTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>() {
                {"ConstructorBehaviorTest", ConstructorBehaviorTest },
                {"StartMethod_ShouldMakeServiceAvailable",  StartMethod_ShouldMakeServiceAvailable },
                {"ServerStartsAndListensTest",  ServerStartsAndListensTest },
                {"StopBehaviorTest",  StopBehaviorTest },
                {"HandleClientTest",  HandleClientTest },
                {"TestStartAndStopInLoopSingleThread", TestStartAndStopInLoopSingleThread },
                {"TestStartAndStopMultipleThreads", TestStartAndStopMultipleThreads }
            };

            CentralLogger.LogInfo("***** Going through TCPCommunicatorTests ***** \n", _log);

            foreach (string testCase in FunctionHashMap.Keys)
            {
                CentralLogger.LogInfo($"tests ---------->{testCase}", _log);
                FunctionHashMap[testCase]();
                CentralLogger.LogInfo($"passed --------->{testCase}\n\n", _log);
            }
        }


        /// <summary>
        /// Tests the behavior of TCPCommunicator's constructor when passing null, empty, blank IP address, or invalid port (0>port>65535).
        /// Expected behavior: Constructor should throw an exception when null, empty, or blank IP address is passed.
        /// </summary>
        public  static void ConstructorBehaviorTest()
        {
            bool NullCatched = false, EmptyCatched = false, BlankCatched = false;
            bool NegativePortCatched = false, InvalidPortCatched = false; 
            // invalid hostname
            try { new TcpServer(null, 1111); } catch (Exception ex) { CentralLogger.LogInfo(ex.ToString(), _log); NullCatched = true; };
            try { new TcpServer("", 1111); } catch (Exception ex) { CentralLogger.LogInfo(ex.ToString(), _log); EmptyCatched = true; };
            try { new TcpServer("    ", 1111); } catch (Exception ex) { CentralLogger.LogInfo(ex.ToString(), _log); BlankCatched = true; };
            ClassicAssert.IsTrue(NullCatched && EmptyCatched && BlankCatched);

            //invalid port 
            try { new TcpServer(TestIp, -8000); } catch (Exception ex) { CentralLogger.LogInfo(ex.ToString(), _log); NegativePortCatched = true; };
            try { new TcpServer(TestIp, 65536); } catch (Exception ex) { CentralLogger.LogInfo(ex.ToString(), _log); InvalidPortCatched = true; };
            ClassicAssert.IsTrue(InvalidPortCatched && NegativePortCatched);
        }

        /// <summary>
        /// Tests the behavior of the Start() method in TCPCommunicator.
        /// Expected behavior: TCPCommunicator should not be available before calling Start(). After calling the Start() method, the service should become available
        /// </summary>
        private static void StartMethod_ShouldMakeServiceAvailable()
        {
            TcpServer tcpCommunicator = new TcpServer(TestIp, 2222);
            TcpClient client = new TcpClient();

            //server should not start before calling Start();
            bool catched = false;  
            try { client.Connect(TestIp, 2222); }catch(SocketException e) { CentralLogger.LogInfo(e.ToString(), _log) ;catched = true; }

            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());
            ClassicAssert.IsTrue(catched);

            //Server shoulf start after calling start and service must be available
            tcpCommunicator.Start();

            ClassicAssert.IsTrue(tcpCommunicator.IsRunning());

            client.Connect(TestIp, 2222);
            ClassicAssert.IsTrue(client.Client.Connected);
            client.Close();
            tcpCommunicator.Stop();
        }

       

        /// <summary>
        /// Test of service availability.
        /// Expected behavior: After starting the server, client being able to connect.
        /// </summary>
        private static void ServerStartsAndListensTest()
        {

            TcpServer tcpCommunicator = new TcpServer(TestIp, 4444);
            tcpCommunicator.Start();
            TcpClient client = new TcpClient();
            client.Connect(TestIp, 4444);

            ClassicAssert.IsTrue(tcpCommunicator.IsRunning());
            ClassicAssert.IsTrue(client.Client.Connected);
            tcpCommunicator.Stop();
           
            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());
        }

        /// <summary>
        /// stop functionality 
        /// </summary>
        private static void StopBehaviorTest()
        {
            TcpServer tcpCommunicator = new TcpServer(TestIp, 5555);
            tcpCommunicator.Start();
            
            tcpCommunicator.Stop();
            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());
        }



        /// <summary>
        /// Tests the correctness of service provided by server. 
        /// Testprecondition: The DB should be started and lietend before executing this test method!
        /// Expected behavior: if valid requests being send to the server server should return with a valid response. 
        /// </summary>
        private static void HandleClientTest()
        {
            TcpServer tcpCommunicator = new TcpServer(TestIp,8000);
            tcpCommunicator.Start();
            Zipper zipper = new Zipper();
           
            int Id = 25, Pressure = 90;
            
            TcpClient client = new TcpClient();
            client.Connect(TestIp, 8000); // Connect to the server

            NetworkStream clientStream  = client.GetStream();

            using (BinaryWriter writer = new BinaryWriter(clientStream))
            using (BinaryReader reader = new BinaryReader(clientStream))
            {
                string PostRequest = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\":" + Id + ", \"pressure\":\"" + Pressure + " Pa\"}";

                byte[] compressedPostRequest = zipper.Compress(PostRequest);
                writer.Write(compressedPostRequest.Length);
                writer.Write(compressedPostRequest);
                writer.Flush();

                // wait for response to the POST-request 
                int postDataLength = reader.ReadInt32();
                byte[] compressedPostResponse = reader.ReadBytes(postDataLength);
                
                string PostResponse = compressedPostResponse != null ? zipper.Decompress(compressedPostResponse) : "null";
                ClassicAssert.AreEqual(PostResponse, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}");

               
                // try to send GET-request and also test the result of previous post request............................................................
                string GetRequest = "\"auth-key\": \"secKey123#\", \"method\": \"GET\", \"reqId\": \"getPressure\", \"body\": {\"tankId\":" + Id + "}";

                byte[] compressedGetRequest = zipper.Compress(GetRequest);
                writer.Write(compressedGetRequest.Length);
                writer.Write(compressedGetRequest);
                writer.Flush();

                // wait for response to the GET-request 
                int getDataLength = reader.ReadInt32();
                byte[] compressedGetResponse = reader.ReadBytes(getDataLength);
                string GetResponse = zipper.Decompress(compressedGetResponse);
                // Assert
                ClassicAssert.AreEqual(GetResponse, "{\"reqId\": \"getPressure\", \"body\": {" + "\"tankId\": " + Id + ", \"pressure\":\"" + Pressure + " Pa\"" + ", \"status-code\": \"200 ok\"}}");
            }
           
            
            tcpCommunicator.Stop();
            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());

                        
        }

        /// <summary>
        /// 
        /// </summary>
        private static void TestStartAndStopInLoopSingleThread()
        {
            for (int i = 0; i<10; i++)
            {
                StopBehaviorTest();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        private static void TestStartAndStopMultipleThreads()
        {
            for (int i = 0; i < 10; i++)
            {
                new Thread(()=>StopBehaviorTest());
            }
        }

    }
}
