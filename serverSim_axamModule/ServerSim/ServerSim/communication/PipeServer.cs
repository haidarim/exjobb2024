using System;
using System.Collections.Generic;
using System.IO.Pipes;
using System.Linq;
using System.Security.AccessControl;
using System.Text;
using System.Threading.Tasks;
using ServerSim.utils;
using ServerSim.Log;
using log4net;

namespace ServerSim.communication
{
    /// <summary>
    /// PipeServer to be used for communication locally at the same Windows OS. This class has a synchronous architecture i.e. blocking, there fore a thread should be employed when using this calss. 
    /// </summary>
    
    public class PipeServer
    {
        #region private vars
        private static RequestHandler requestHandler = new RequestHandler(); // request handler (router layer)
        private static readonly ILog _log = LogManager.GetLogger(typeof(PipeServer)); // loger to be used for centralized logging  

        #endregion


        #region public access
        /// <summary>
        /// Creates NamedPipes and gives service to the pipe client 
        /// </summary>
        public static  void Run()
        {
            while (true)
            {
                try
                {
                    using (NamedPipeServerStream readServer = new NamedPipeServerStream("readPipe"))
                    using (NamedPipeServerStream writeServer = new NamedPipeServerStream("writePipe"))
                    {
                        CentralLogger.LogInfo("PipeServer: waiting for connection ...", _log);
                        readServer.WaitForConnection();
                        
                        CentralLogger.LogInfo("read client connected!", _log);

                        using (StreamReader reader = new StreamReader(readServer)) // make writer nested 
                        {
                            string? req;
                            if ((req = reader.ReadLine()) != null)
                            {
                                reader.Close();
                                reader.Dispose();
                                CentralLogger.LogInfo("req is: " + req, _log);

                                
                                writeServer.WaitForConnection();
                                CentralLogger.LogInfo("write client connected!", _log);
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
                                    CentralLogger.LogInfo("writing failur: " + e.ToString(), _log);
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    CentralLogger.LogInfo("main loop: " + e.Message, _log);
                    break;
                }
            }
            CentralLogger.LogInfo("pipe communication has been closed!", _log);
        }
        #endregion
    }

}
