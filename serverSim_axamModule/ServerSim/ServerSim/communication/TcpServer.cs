using System.Net;
using System.Net.Sockets;

using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.Diagnostics.Contracts;
using NUnit.Framework;
using ServerSim.communication;
using System.Numerics;
using log4net;
using ServerSim.Log;
using System.Net.Http;

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


        #region private variables
        private static readonly object _lock = new object();
        private volatile bool _running = false;
        private string _ip;
        private int _port;
        private static List<TcpClientHandler>? _clients;
        private volatile TcpListener? _server;

        private static readonly ILog _log = LogManager.GetLogger(typeof(TcpServer));

        private Thread? _serviceWorker;
        #endregion
    


        #region constructor
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="ip"> string data indicating the server's ip-address </param>
        /// <param name="port"> integer data indicating the server's port </param>
        /// <exception cref="ArgumentException">if ip-address-string is invalid or if 0>port>65535 </exception>
        public TcpServer(string ip, int port)
        {
            if (string.IsNullOrEmpty(ip) || string.IsNullOrWhiteSpace(ip))
            {
                throw new ArgumentException("invalid ip address!");
            }
            if (port < 0 || port > 65535)
            {
                throw new ArgumentException("invalid port number!");
            }
            this._ip = ip;
            this._port = port;
            _clients = new List<TcpClientHandler>();
        }
        #endregion


        #region public access

        /// <summary>
        ///     This method will start server-service and does not release the current thread until the server is started
        /// </summary>
        /// <exception cref="Exception">if server already running </exception>
        public void Start()
        {
            if (IsRunning())
            {
                throw new Exception("servre already running");
            }
            this._serviceWorker = new Thread(() => _run());
            this._serviceWorker.Start();
            while (!IsRunning()) { continue; } // hold and wait (current thread)
        }



        /// <summary>
        ///     stop service and close all clients socket
        /// </summary>
        public void Stop()
        {
            lock (_lock)
            {
                this._server?.Server?.Close();
                this._running = false;
                if (_clients?.Count > 0)
                {
                    _clients.ForEach(client => { client.Client.Close(); });
                    _clients.Clear();
                    CentralLogger.LogInfo("removed all clients in stop call", _log);
                }

                CentralLogger.LogInfo("Stop done!", _log);
                _serviceWorker?.Interrupt();
                CentralLogger.LogInfo("terminated!", _log);
            }
        }


        /// <summary>
        ///     returs the state of the server
        /// </summary>
        /// <returns>bool, true if server is running else false</returns>
        public bool IsRunning()
        {
            lock (_lock)
            {
                return this._running;
            }
        }


        /// <summary>
        ///     sends the message to all clients
        /// </summary>
        /// <param name="message">string message to be sent to all clients</param>
        public static void broadCast(byte[] message)
        {
            if (_clients?.Count > 0)
            {
                _clients.ForEach(client => { client.SendMessage(message); });
            }
        }



        #endregion

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
                    
                    IPEndPoint server_ip = (IPEndPoint)_server.LocalEndpoint;

                    Console.WriteLine($"TCP-Server:[{server_ip.Address}] runing on port {this._port}: waiting for connection!");
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
        

    }
}
