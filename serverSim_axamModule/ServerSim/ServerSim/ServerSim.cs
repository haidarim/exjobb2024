

using ServerSim.communication;
using ServerSim.test.behavioral.TCPCommunicatorTests;

using ServerSim.test.white_box.RequestHandlerTest;
using ServerSim.test.white_box;
using ServerSim.utils;
using ServerSim.test.behavioral.ZipperTest;
using ServerSim.Log;
using log4net;

namespace ServerSim
{

    class Server
    {

        private static readonly ILog _log = LogManager.GetLogger(typeof(Server));
        public static void Main(String[] args)
        {
            RunTests();

            TcpServer tcpCommunicator = new TcpServer("172.18.0.3", 2024);
            Thread pipeCommunicator = new Thread(() => { PipeServer.Run(); });

            tcpCommunicator.Start();
            pipeCommunicator.Start();

        }

        private static void RunTests()
        {
           /* ZipperTest.RunZipperTests();*/ // TO ENABLE THIS TEST SUITE, MAKE SUR YOU HAVE CONFIGURED THE TEST CLASS!
            KeyValueStringTests.RunKeyValueStringTests();
            RequestHandlerTests.RunRequestHandlerTests();
            TcpServerTests.RunTCPCommunicatorTests();

            CentralLogger.LogInfo("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n", _log);
            CentralLogger.LogInfo("<<<<<<<<<< All TESTS PASSED! >>>>>>>>>>", _log);
            CentralLogger.LogInfo("\n\n\n\n", _log);
        }
    }
}
