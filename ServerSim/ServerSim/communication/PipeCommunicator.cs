using System;
using System.Collections.Generic;
using System.IO.Pipes;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ServerSim.utils;

namespace ServerSim.communication
{
    public class PipeCommunicator:ICommunicator
    {
        private volatile bool isRunning = false; 
        private RequestHandler requestHandler = new RequestHandler();

        public PipeCommunicator() { }

        public bool IsRunning()
        {
            return isRunning;
        }

        public void Stop()
        {
            isRunning = false;
        }

        public void Start()
        {
            if (isRunning)
            {
                throw new Exception("servre already running");
            }
            this.isRunning = true;
            new Thread(() => Run()).Start();
        }

        private void Run()
        {
            while (isRunning)
            {
                try
                {
                    using (NamedPipeServerStream readServer = new NamedPipeServerStream("readPipe"))
                    using (NamedPipeServerStream writeServer = new NamedPipeServerStream("writePipe"))
                    {
                        Console.WriteLine("PipeServer: waiting to connection!");
                        readServer.WaitForConnection();
                        //wait
                        Console.WriteLine("client connected!");

                        using (StreamReader reader = new StreamReader(readServer)) // make writer nested 
                        {
                            string req;
                            if ((req = reader.ReadLine()) != null)
                            {
                                reader.Close();
                                reader.Dispose();
                                Console.WriteLine("req is: " + req);

                                //Console.WriteLine("waiting to pipe!"); // was uist
                                writeServer.WaitForConnection();
                                try
                                {
                                    using (StreamWriter writer = new StreamWriter(writeServer))
                                    {
                                        string response = requestHandler.HandleRequest(req).Replace("\n", " ").Replace("\r", " ");
                                        writer.WriteLine(response);
                                        writer.Flush();
                                        writer.Close();
                                        writer.Dispose();
                                        continue;
                                    }
                                }
                                catch (Exception e)
                                {
                                    Console.WriteLine("writing failur: " + e.ToString());
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("main loop: " + e.Message);
                }
            }
            Console.WriteLine("pipe communication has been closed!");
        }
    }

}
