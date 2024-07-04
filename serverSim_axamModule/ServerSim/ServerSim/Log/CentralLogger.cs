using log4net;
using log4net.Config;
using log4net.Appender;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using log4net.Layout;
using log4net.Repository.Hierarchy;
using log4net.Core;
using System.Runtime.CompilerServices;

namespace ServerSim.Log
{

    /// <summary>
    ///     The centralized Logger for the Server Simulation, used in TcpServer,, Tests, ... 
    /// </summary>
    public class CentralLogger
    {
        #region private vars
        private static ILog log = LogManager.GetLogger(typeof(CentralLogger));
        private static readonly object _logLock = new object();

        #endregion


        #region static block
        static CentralLogger()
        {
            try
            {
                ConsoleAppender consoleAppender = new ConsoleAppender
                {
                    Layout = new PatternLayout("[%date] [%thread] %-5level %logger - %message%newline"),
                    Threshold = Level.All // Set threshold for console appender
                };

                consoleAppender.ActivateOptions(); // activate options such as Layout

                RollingFileAppender fileAppender = new RollingFileAppender
                {
                    File = "tcp_server.log",
                    AppendToFile = false, // Set to false to overwrite existing file
                    RollingStyle = RollingFileAppender.RollingMode.Once, // Ensure it's created only once
                    Layout = new PatternLayout("[%date] [%thread] %-5level %logger - %message%newline"),
                    Threshold = Level.All // Set threshold for file appender
                };
                fileAppender.ActivateOptions(); // activate options such as Layout

                // Configure the logger
                BasicConfigurator.Configure(fileAppender, consoleAppender);
            }
            catch (Exception ex)
            {
                LogInfo("Error initializing logger: " + ex.Message, log);
            }
        }
        #endregion


        #region public access
        public static void LogInfo(string message, ILog logger)
        {
            try
            {
                lock (_logLock)
                {
                    logger.Info(message);
                }
            }catch (Exception e) {
                LogInfo($">>>>>>Logger Jumped out [{Thread.CurrentThread.ManagedThreadId}: exception is: {e.ToString()}]<<<<<<", logger);
            }
        }

        #endregion

    }
}
