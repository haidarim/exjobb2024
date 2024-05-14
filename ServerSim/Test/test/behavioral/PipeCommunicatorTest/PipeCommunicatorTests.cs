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
                {"TestStartWhenServerAlreadyRunning", TestStartWhenServerAlreadyRunning }
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
       /// Expected behavior: 
       /// </summary>
        private static  void TestStartWhenServerAlreadyRunning()
        {
            // Arrange
            pipeCommunicator.Start();

            // Act & Assert
            Assert.Throws<Exception>(() => pipeCommunicator.Start());
        }

     
        public void Test_Start_And_Stop_Methods()
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

   
      
        

       
      

        public void Test_Stop_Method_During_Runtime()
        {
            // Arrange
            pipeCommunicator.Start();

            // Act
            pipeCommunicator.Stop();

            // Assert
            ClassicAssert.IsFalse(pipeCommunicator.IsRunning());
        }

    }
}
