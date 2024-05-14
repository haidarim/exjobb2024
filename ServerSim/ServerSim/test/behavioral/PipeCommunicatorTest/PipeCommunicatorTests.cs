using System;
using System.Collections.Generic;
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

        /// <summary>
        /// This is a caller method, the only public method of this class, 
        /// which is responsible to run all test cases. 
        /// </summary>
        public static void RunPipeCommunicatorTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>() {
                
            };

            Console.WriteLine("######################################### Going through PipeCommunicatorTests ######################################");

            foreach (string key in FunctionHashMap.Keys)
            {
                Console.WriteLine($"starting ----------->{key}<-----------------");
                FunctionHashMap[key]();
                Console.WriteLine($"-------------------->{key}<-----------passed\n\n");
            }
        }
    }
}
