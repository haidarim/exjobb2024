using ServerSim.Log;
using ServerSim.utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using log4net.Layout;
using log4net;

namespace ServerSim.communication
{
    /// <summary>
    ///     This Class is responsible to give the client services based on the client's requests. 
    ///     the server can also use this class to send message to client of this class instance. 
    /// </summary>
    public class TcpClientHandler
    {

        #region private vars
        private readonly object _lockObject = new object();
        private BinaryWriter? _writer;
        private TcpClient _tcpClient;
        private RequestHandler RQHandler;
        private readonly IPAddress _ip;
        private readonly ILog _log = LogManager.GetLogger(typeof(TcpClientHandler));
        private readonly Zipper zipper = new Zipper();
        #endregion



        #region constructor
        /// <summary>
        /// Constructor of the client handler
        /// </summary>
        /// <param name="client">TcpClient</param>
        /// <exception cref="ArgumentNullException">if the client is null or if the ip address is not clear</exception>
        public TcpClientHandler(TcpClient client)
        {
            if (client == null)
            {
                CentralLogger.LogInfo("calient should be not-null", _log);
                throw new ArgumentNullException("client");
            }
            this._tcpClient = client;
            this.RQHandler = new RequestHandler();

            var ip = _tcpClient.Client.RemoteEndPoint;
            if (ip == null)
            {
                CentralLogger.LogInfo("null ip", _log);
                throw new ArgumentNullException("null ip");
            }
            lock (_lockObject)
            {
                this._ip = ((IPEndPoint)ip).Address;
            }
            CentralLogger.LogInfo("tcp client object born", _log);
        }

        #endregion



        #region public access

        /// <summary>
        /// Starts the service i.e. initializes streams for data exchange 
        /// </summary>
        public void Start()
        {
            CentralLogger.LogInfo($"started in clientThread: [{Thread.CurrentThread.ManagedThreadId}]", _log);
            Handle();
        }


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


        /// <summary>
        /// returns the client (tcp-socket)
        /// </summary>
        public TcpClient Client { get { return this._tcpClient; } }


        /// <summary>
        /// returns user's ip address 
        /// </summary>
        public IPAddress GetClientIP()
        {
            lock (_lockObject)
            {
                return this._ip;
            }
        }

        #endregion



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
    }
}
