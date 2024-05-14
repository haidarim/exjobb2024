using NUnit.Framework;
using NUnit.Framework.Legacy;
using ServerSim.utils;
using System.Net.Sockets;
using System.Reflection.PortableExecutable;

namespace ServerSim.test.behavioral.TCPCommunicatorTests
{


   /// <summary>
   /// This class tests functionality corectness of the TCPCommunicator by testing methods and the behavior of
   /// TCPCommunicator, i.e. Behavioral testing. 
   /// </summary>
    public class TCPCommunicatorTests
    {
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
                {"HandleClientTest",  HandleClientTest }
            };

            Console.WriteLine("######################################## Going through TCPCommunicatorTests #####################################");

            foreach (string key in FunctionHashMap.Keys)
            {
                Console.WriteLine($"starting ----------->{key}<-----------------");
                FunctionHashMap[key]();
                Console.WriteLine($"-------------------->{key}<-----------passed\n\n");
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
            try { new TCPCommunicator(null, 11111); } catch (Exception ex) { NullCatched = true; };
            try { new TCPCommunicator("", 11111); } catch (Exception ex) { EmptyCatched = true; };
            try { new TCPCommunicator("    ", 11111); } catch (Exception ex) { BlankCatched = true; };
            ClassicAssert.IsTrue(NullCatched && EmptyCatched && BlankCatched);

            //invalid port 
            try { new TCPCommunicator(TestIp, -8000); } catch (Exception ex) { NegativePortCatched = true; };
            try { new TCPCommunicator(TestIp, 65536); } catch (Exception ex) { InvalidPortCatched = true; };
            ClassicAssert.IsTrue(InvalidPortCatched && NegativePortCatched);
        }


        /// <summary>
        /// Tests the behavior of the Start() method in TCPCommunicator.
        /// Expected behavior: TCPCommunicator should not be available before calling Start(). After calling the Start() method, the service should become available
        /// </summary>
        private static void StartMethod_ShouldMakeServiceAvailable()
        {
            TCPCommunicator tcpCommunicator = new TCPCommunicator(TestIp, 22222);
            TcpClient client = new TcpClient();

            //server should not start before calling Start();
            bool catched = false;  
            try { client.Connect(TestIp, 22222); }catch(SocketException e) {  catched = true; }

            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());
            ClassicAssert.IsTrue(catched);

            //Server shoulf start after calling start and service must be available
            tcpCommunicator.Start();

            ClassicAssert.IsTrue(tcpCommunicator.IsRunning());

            client.Connect(TestIp, 22222);
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

            TCPCommunicator tcpCommunicator = new TCPCommunicator(TestIp, 44444);
            tcpCommunicator.Start();
            TcpClient client = new TcpClient();
            client.Connect(TestIp, 44444);

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
            TCPCommunicator tcpCommunicator = new TCPCommunicator(TestIp, 55555);
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
            TCPCommunicator tcpCommunicator = new TCPCommunicator(TestIp,8000);
            tcpCommunicator.Start();
           
            int Id = 25, Pressure = 90;
            
            TcpClient client = new TcpClient();
            client.Connect(TestIp, 8000); // Connect to the server

            NetworkStream clientStream  = client.GetStream();

            using (StreamWriter writer = new StreamWriter(clientStream))
            using (StreamReader reader = new StreamReader(clientStream))
            {
                string PostRequest = "\"auth-key\": \"secKey123#\", \"method\": \"POST\", \"reqId\": \"setPressure\", \"body\": {\"tankId\":" + Id + ", \"pressure\":\"" + Pressure + " Pa\"}";

                writer.WriteLine(PostRequest);
                writer.Flush();

                // wait for response to the POST-request 
                var response = reader.ReadLine();
                string PostResponse = response != null ? response.ToString() : "null";
                ClassicAssert.AreEqual(PostResponse, "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}");

                Console.WriteLine("here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1"); ;

                // try to send GET-request and also test the result of previous post request............................................................
                string GetRequest = "\"auth-key\": \"secKey123#\", \"method\": \"GET\", \"reqId\": \"getPressure\", \"body\": {\"tankId\":" + Id + "}";

                writer.WriteLine(GetRequest);
                writer.Flush();

                // wait for response to the GET-request 
                response = reader.ReadLine();
                string GetResponse = response != null ? response.ToString() : "null";

                // Assert
                ClassicAssert.AreEqual(GetResponse, "{\"reqId\": \"getPressure\", \"body\": {" + "\"tankId\": " + Id + ", \"pressure\":\"" + Pressure + " Pa\"" + ", \"status-code\": \"200 ok\"}}");
            }
           
            
            tcpCommunicator.Stop();
            ClassicAssert.IsFalse(tcpCommunicator.IsRunning());
            
        }

    }
}
