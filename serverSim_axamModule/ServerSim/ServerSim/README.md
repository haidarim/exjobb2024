# Server Simulator:  ServerSim.cs "./"

The `ServerSim.cs` is the entry point of the server simulator wich runs the `TcpServer`, `PipeServer` as well as testsuits. After starting Simulater program teestsuites will be called at first. 

**NOTE:** The `ZipperTest.RunZipperTests()` are currently commented, if you want to enable tests in this class you have to change the ZipperTest. It should be configuered correctly with files on your work directory. 

After all tests passed the `ALL TESTS PASSED` will be logged by the CentralLogger in `./Log/CentralLogger.cs`. 

**NOTE:** TcpServer has asynchronous architectur, but PipeServer has synchronous architectur and there for a Thread i used in SimServer when using PipeServer. The code below is the main entry point in ServerSim.cs: 

```cs
namespace ServerSim
{

    class Server
    {

        private static readonly ILog _log = LogManager.GetLogger(typeof(Server));
        public static void Main(String[] args)
        {
            RunTests();

            TcpServer tcpCommunicator = new TcpServer("127.0.0.1", 2024);
            Thread pipeCommunicator = new Thread(() => { PipeServer.Run(); });

            tcpCommunicator.Start();
            pipeCommunicator.Start();

        }
    }

    // RunTests ....
}

``` 

# Tests: "./test/"
All tests are located under `./test/` directory. 


# Communication: "./communication/"
The implementation of TCP server, Pipe server and handlers is under `./communication`.  
### TcpServer : 
This class is located in `./communication/TcpServer`, this class use TCP socket accept connections. 
The server class creates new `Thread` to handle users's requests using multi threads. 

**NOTE:** Using a Thread-pool mechanisn will make the server faster and more scalable. The current version creates new threads when connected. 

The code bellow is the core point of the TcpServer, the of function that service-worker thead runs as runnable:

```cs

// rest of the TcpServer Implementaion ...
namespace ServerSim.utils
{
    /// <summary>
    ///     This calss is responsble to start a TCP based server, the service will listen to all incomming requestes
    ///     To start the server call the Start() method
    ///     To get status of the server call IsRunning()
    ///     To stop the server call Stop()
    /// </summary>
    public class TcpServer 
    {


        #region service

        /// <summary>
        ///     run the service for serviceWorker
        /// </summary>
        /// <exception cref="Exception"> when failing to set-up the server-service </exception>
        private void _run()
        {
            try
            {
                using (_server = new TcpListener(IPAddress.Parse(this._ip), this._port))
                {
                    // Start the server
                    this._server.Start();

                    lock (_lock)
                    {
                        this._running = true;
                    }

                    Console.WriteLine($"TCP-Server runing on port {this._port}: waiting for connection!");
                    CentralLogger.LogInfo($"server started, server-thread: [{Thread.CurrentThread.ManagedThreadId}], on port: {this._port}", _log);
                    while (IsRunning())
                    {
                        // accept connection and pass to TcpClientHandler
                        TcpClient client = this._server.AcceptTcpClient();
                        TcpClientHandler clientHandler = new TcpClientHandler(client);
                        _clients?.Add(clientHandler);

                        CentralLogger.LogInfo($"client joined: {clientHandler.GetClientIP()}", _log); //

                        // new connection has its own thread
                        Thread ClientThread = new Thread(() => clientHandler.Start());
                        ClientThread.Start();
                        CentralLogger.LogInfo($"client thread: [{ClientThread.ManagedThreadId}] started", _log);
                    }

                    CentralLogger.LogInfo("server is off now!", _log);
                }
            }
            catch (Exception e)
            {
                CentralLogger.LogInfo($"service-thread: {e.Message}", _log);
            }
        }
        #endregion


        // rest of the TcpServer Implementaion ...
        
    }
}
```


### TcpTcpClientHandler: 
This class is the runnable code used by clients' thread in the TcpServer class. 
The `./communication/TcpClientHandler` class is responsible to path the request and send back responses. Each TcpClientHandler has its own object of the custom-API class `RequestHandler`.

TcpTcpClientHandler uses the TCP-socket given by the TcpServer. The implementation of main methods are as following:

```cs

namespace ServerSim.communication
{
    /// <summary>
    ///     This Class is responsible to give the client services based on the client's requests. 
    ///     the server can also use this class to send message to client of this class instance. 
    /// </summary>
    public class TcpClientHandler
    {

        /// <summary>
        /// writes the bytes into writing stream 
        /// </summary>
        /// <param name="message">byte[]</param>
        /// <exception cref="ArgumentException">if the message is invalid, null ||empty </exception>
        public void SendMessage(byte[] message)
        {
            if (message == null || message.Length <= 0)
            {
                CentralLogger.LogInfo("null, or empty message in TcpClientHandler", _log);
                throw new ArgumentException("invalid message, null or empty");
            }

            lock (_lockObject) // Lock for the current!
            {
                _writer?.Write(message.Length);
                _writer?.Write(message);
                _writer?.Flush(); // Flush the buffer and send data immediately
                CentralLogger.LogInfo("Response sent!", _log);
            }
        }


        #region private method
        /// <summary>
        /// The service provider method, waits for data (request) and responses back. 
        /// </summary>
        /// <param name="client"></param>
        private void Handle()
        {
            try
            {
                using (NetworkStream clientStream = _tcpClient.GetStream())
                using (BufferedStream bufferedStream = new BufferedStream(clientStream))
                using (BinaryReader reader = new BinaryReader(bufferedStream, Encoding.UTF8))
                using (BinaryWriter writer = new BinaryWriter(bufferedStream, Encoding.UTF8))
                {
                    _writer = writer;
                    while (true)
                    {
                        try
                        {
                            int requestLength = reader.ReadInt32();//IPAddress.NetworkToHostOrder(reader.ReadInt32());
                            byte[] requestBytes = reader.ReadBytes(requestLength);
                            CentralLogger.LogInfo($"reading size of incomming data: {requestBytes.Length}", _log);
                            string request = this.zipper.Decompress(requestBytes);
                            CentralLogger.LogInfo($"Request: {request}", _log);

                            byte[] responseBytes = this.zipper.Compress(RQHandler.HandleRequest(request));
                            SendMessage(responseBytes);
                        }
                        catch (IOException)
                        {
                            break;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                CentralLogger.LogInfo($"Client disconnected: {e.Message}", _log);
            }
            finally
            {
                this._tcpClient.Close();
                CentralLogger.LogInfo("closed the client.Socket!", _log);
            }
        }
        #endregion

        // rest of the TcpClientsHandler.cs

    }
}
```



### PipeServer: 
The pipe server implementation is in `./communication/PipeServer.cs`


# Utility classes "./utils/"
All utils are under `./utils/`. 


**DBConn.cs:** This calss is the communication bridge between server and the data access layer configured to use `redis` database. Used in `RequestHandler.cs`.
**KeyValueString.cs:** An customized `string-json`-like implemntation, used in `RequestHandler.cs`. 
**Zipper.cs:** Compressor utility, used in `TcpClientHandler`.

**RequestHandler.cs:** Is the implementation of the used custom API in the project. The main parts of the custom API are as following: 

```cs
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
        private Dictionary<string, Func<string, bool>> postRequestsHandlers;
        public readonly string auth_key = "secKey123#";
        private readonly object _lockObject = new object();

        public RequestHandler()
        {
            // configurring and setting up the request-functions-map
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
                    else if (mehtod.Equals("TEST"))
                    {
                        responseBody = "\"status\": \"TEST-OK\"";
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


        // rest of the implementaion ...
    }
}
```

# Log "./Log/
The centralized Logger is placed under `./Log/`

# To build and run on Gnu/Linux based systems

**You should have** `dotnet`instaleld on your machine. Then run the given bash below in `./serverSim_axamModule/ServerSim/ServerSim/`:
```sh
chmod +x  ./.run.sh 
./.run.sh 
./.run.sh start
```
