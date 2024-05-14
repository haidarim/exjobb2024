using System.Net;
using System.Net.Sockets;

using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.Diagnostics.Contracts;
using NUnit.Framework;
using ServerSim.communication;

namespace ServerSim.utils
{
    public class TCPCommunicator:ICommunicator
    {

        /// <summary>
        /// extenablity: bradcasting
        /// </summary>

        #region privateVar
        private RequestHandler RQHandler = new RequestHandler(); 
        private volatile bool Running = false;
        private string Ip;
        private int Port;
        private List<TcpClient> Clients; 
        #endregion


        public TCPCommunicator(string Ip, int Port)
        {
            if(string.IsNullOrEmpty(Ip) || string.IsNullOrWhiteSpace(Ip))
            {
                throw new ArgumentException("invalid ip address!");
            }
            if (Port < 0 || Port > 65535)
            {
                throw new ArgumentException("invalid port number!");
            }
            this.Ip = Ip;
            this.Port = Port;
            Clients = new List<TcpClient>();
        }

        public void Start()
        {
            if (Running)
            {
                throw new Exception("servre already running");
            }
            new Thread(()=>Run()).Start();            
        }

        private void Run()
        {
            try
            {
                using (TcpListener server = new TcpListener(IPAddress.Parse(this.Ip), this.Port))
                {
                    // Start the server 
                    server.Start();
                    this.Running = true;

                    Console.WriteLine("TCP-Server: Väntar på anslutning...");

                    while (this.Running)
                    {
                        // accept connections 
                        TcpClient client = server.AcceptTcpClient();

                        Clients.Add(client);
                        Console.WriteLine("Ansluten!");

                        // new connection has its own thread 
                        Thread ClientThread = new Thread(() => RunClientHandler(client));
                        ClientThread.Start();
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"failure in server-setup: {e.Message}");
                throw new Exception("server failed!");
            }
        }

        public void Stop()
        {
            this.Running = false;
            foreach (TcpClient Client in Clients)
            {
                if (Client != null)
                {
                    if (Client.Client != null && Client.Client.Connected)
                    {
                        Client.Client.Shutdown(SocketShutdown.Both);
                    }
                    Client.Close();
                }
            }

            Clients.Clear();
        }

        public bool IsRunning()
        {
            return this.Running;
        }

 

        public void RemoveClient(TcpClient Client)
        {
            if (Clients.Contains(Client) && !Clients.Remove(Client))
            {
                throw new Exception($"ContractFailedEventArgs to remove the client");
            }
            Client.Close();
        }

 

        private void RunClientHandler(TcpClient client)
        {
            /*
            try
            {
                using (NetworkStream clientIOStream = client.GetStream())
                using (BufferedStream bufferedStream = new BufferedStream(clientIOStream))
                using (StreamReader reader = new StreamReader(bufferedStream))
                using (StreamWriter writer = new StreamWriter(bufferedStream, Encoding.UTF8))
                {
                    // read data 
                    var readed = reader.ReadLine();
 
                    if (readed != null)
                    {
                        string request = readed.ToString();

                        string response = reqHandler.HandleRequest(request).Replace("\n", " ").Replace("\r", " ");
                        // send the response 
                        writer.WriteLine(response);
                        writer.Flush(); // töm bufferten och skicka data omedelbart
                        Console.WriteLine("Response sent!");
                    }
                }
            }
            catch(Exception e) 
            {
                Console.WriteLine($"failure when running client handler RunClientHandler(...): {e.Message}");
            }*/
            try
            {



                //StreamReader reader = new StreamReader(clientStream);
                //StreamWriter writer = new StreamWriter(clientStream, Encoding.UTF8);
                using (NetworkStream clientStream = client.GetStream())
                using (BufferedStream bufferedStream = new BufferedStream(clientStream))
                using (StreamReader reader = new StreamReader(bufferedStream))
                using (StreamWriter writer = new StreamWriter(bufferedStream, Encoding.UTF8))
                {
                    string? request;
                    while ((request = reader.ReadLine()) != null) {
                        Console.WriteLine("\n\n readed\n\n");
                        string response = RQHandler.HandleRequest(request).Replace("\n", " ").Replace("\r", " ");

                        writer.WriteLine(response);
                        writer.Flush(); // Flush the buffer and send data immediately
                        Console.WriteLine("Response sent!");
                    }
                    Console.WriteLine("Client disconnected. \"#########\"");
                }

                
                RemoveClient(client);
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

        
        


    }
}
