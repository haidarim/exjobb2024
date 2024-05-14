

using ServerSim.communication;
using ServerSim.test.behavioral.TCPCommunicatorTests;
using ServerSim.test.behavioral.PipeCommunicatorTests;
using ServerSim.test.white_box;
using ServerSim.utils;

namespace ServerSim
{

    class Server
    {


        public static void Main(String[] args)
        {
            RunTests();

            TCPCommunicator tcpCommunicator = new TCPCommunicator("127.0.0.1", 8080);
            PipeCommunicator pipeCommunicator = new PipeCommunicator();
            
            tcpCommunicator.Start();
            pipeCommunicator.Start();          
        }

        private static void RunTests()
        {
            KeyValueStringTests.RunKeyValueStringTests();
            TCPCommunicatorTests.RunTCPCommunicatorTests();
            PipeCommunicatorTests.RunPipeCommunicatorTests();
            //DBConnTest.RunDBConnTests();
            //RequestHandlerTest.RunRequestHandlerTests();
            Console.WriteLine("All Tests Passed!");
            // TODO: other tests here
        }
    }
}
