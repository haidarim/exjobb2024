using NUnit.Framework;
using NUnit.Framework.Legacy;
using ServerSim.communication;
using System;
using System.Collections.Generic;
using System.IO.Pipes;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.test.behavioral.PipeCommunicatorTests
{
    /// <summary>
    /// This class tests functionality corectness of the PipeCommunicator by testing methods and the behavior of
    /// PipeCommunicator, i.e. Behavioral testing. 
    /// </summary>
    public class PipeCommunicatorTests
    {

        private static PipeCommunicator pipeCommunicator = new PipeCommunicator();
        /// <summary>
        /// This is a caller method, the only public method of this class, 
        /// which is responsible to run all test cases. 
        /// </summary>
        public static void RunPipeCommunicatorTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>() {
                {"TestStartWhenServerAlreadyRunning", TestStartWhenServerAlreadyRunning },
                {"TestStartAndStopMethodsBehavior", TestStartAndStopMethodsBehavior }
            };

            Console.WriteLine("######################################### Going through PipeCommunicatorTests ######################################");

            foreach (string key in FunctionHashMap.Keys)
            {
                Console.WriteLine($"starting ----------->{key}<-----------------");
                FunctionHashMap[key]();
                Console.WriteLine($"-------------------->{key}<-----------passed\n\n");
            }
        }

       /// <summary>
       /// Test call the Start when Pipe-server is already started
       /// Expected behavior: The method should throw an Exception
       /// </summary>
        private static  void TestStartWhenServerAlreadyRunning()
        {
            // Arrange
            pipeCommunicator.Start();
            bool catched = false;
            // Act & Assert
            try { pipeCommunicator.Start();  }catch(Exception e) { catched = true; }
            ClassicAssert.IsTrue(catched);
            pipeCommunicator.Stop();
        }

     
        /// <summary>
        /// Test the correctness of Start and Stop mehtods
        /// </summary>
        private static void TestStartAndStopMethodsBehavior()
        {
            // Act
            pipeCommunicator.Start();

            // Assert
            ClassicAssert.IsTrue(pipeCommunicator.IsRunning());

            // Act
            pipeCommunicator.Stop();

            // Assert
            ClassicAssert.IsFalse(pipeCommunicator.IsRunning());
        }

    }
}
