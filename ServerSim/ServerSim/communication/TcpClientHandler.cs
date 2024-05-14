using ServerSim.utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.communication
{
    
    public class TcpClientHandler
    {
        private readonly object _lockObject = new object();
        private StreamWriter _writer;
        private TcpClient _tcpClient;
        private RequestHandler RQHandler;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="client"></param>
        /// <exception cref="ArgumentNullException"></exception>
        public TcpClientHandler(TcpClient client) {
            if(client == null) {  throw new ArgumentNullException("client"); }
            this._tcpClient = client;
            this.RQHandler = new RequestHandler();
        }

        /// <summary>
        /// 
        /// </summary>
        public void Start()
        {
            Handle();
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="client"></param>
        private void Handle()
        {
            try
            {
                using (NetworkStream clientStream = _tcpClient.GetStream())
                using (BufferedStream bufferedStream = new BufferedStream(clientStream))
                using (StreamReader reader = new StreamReader(bufferedStream))
                using (_writer = new StreamWriter(bufferedStream, Encoding.UTF8))
                {
                    string? request;
                    while ((request = reader.ReadLine()) != null )
                    {
                        Console.WriteLine("\n\n readed\n\n");
                        string response = RQHandler.HandleRequest(request).Replace("\n", " ").Replace("\r", " ");
                        SendMessage(response);                      
                    }
                    Console.WriteLine("Client disconnected. \"#########\"");
                }

                TCPCommunicator.RemoveClient(this);
                Console.WriteLine("Removed");
            }
            catch (Exception e)
            {
                Console.WriteLine($"Failure when running client handler: {e.Message}");
            }
            finally
            {
                Console.WriteLine("closed the client.Socket!");
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="message"></param>
        /// <exception cref="ArgumentException"></exception>
        /// <exception cref="Exception"></exception>
        public void SendMessage(string message)
        {
            if (string.IsNullOrEmpty(message) || string.IsNullOrWhiteSpace(message))
            {
                throw new ArgumentException("invalid message, null or empty");
            }
            
            lock (_lockObject)
            {
                _writer.WriteLine(message);
                _writer.Flush(); // Flush the buffer and send data immediately
                Console.WriteLine("Response sent!");
            }
        }


        /// <summary>
        /// 
        /// </summary>
        public TcpClient Client { get { return this._tcpClient; } }
    }
}
