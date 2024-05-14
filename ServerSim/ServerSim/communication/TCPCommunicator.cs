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
    /// <summary>
    /// ---------------------------------------------------------
    /// </summary>
    public class TCPCommunicator:ICommunicator
    {

        /// <summary>
        /// extenablity: bradcasting
        /// </summary>

        #region privateVar
         
        private volatile bool Running = false;
        private string Ip;
        private int Port;
        private static List<TcpClientHandler> Clients; 
        #endregion



        /// <summary>
        /// ..................................................
        /// </summary>
        /// <param name="Ip"></param>
        /// <param name="Port"></param>
        /// <exception cref="ArgumentException"></exception>
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
            Clients = new List<TcpClientHandler>();
        }


        /// <summary>
        /// This method will start server-service and does not release the current thread until the server is started
        /// </summary>
        /// <exception cref="Exception"></exception>
        public void Start()
        {
            if (Running)
            {
                throw new Exception("servre already running");
            }
            new Thread(()=>Run()).Start();
            while (!Running) { continue; }
        }


        /// <summary>
        /// 
        /// </summary>
        /// <exception cref="Exception"></exception>
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
                        // accept connection and pass to TcpClientHandler   
                        TcpClientHandler clientHandler = new TcpClientHandler(server.AcceptTcpClient());
                        Clients.Add(clientHandler);
                        
                        Console.WriteLine("Ansluten!");

                        // new connection has its own thread 
                        Thread ClientThread = new Thread(() => clientHandler.Start());
                        ClientThread.Start();
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"failure in server-setup: {e.Message}");
                this.Running = false; 
                throw new Exception("server failed!");
            }
        }



        /// <summary>
        /// 
        /// </summary>
        public void Stop()
        {
            this.Running = false;
            Clients.Clear();
        }


        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        public bool IsRunning()
        {
            return this.Running;
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="message"></param>
        public static void broadCast(string message)
        {
            Clients.ForEach(client => { client.SendMessage(message); });
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="Client"></param>
        /// <exception cref="Exception"></exception>
        /*public static void RemoveClient(TcpClient Client)
        {
            if (Clients.Contains(Client) && !Clients.Remove(Client))
            {
                throw new Exception($"Failed to remove the client");
            }
            Client.Close();
        }*/

        public static void RemoveClient(TcpClientHandler clientHandler)
        {
            Clients.Remove(clientHandler);
        }



        /*
        ///
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
            }
        ---
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
        }*/

        
        


    }
}
