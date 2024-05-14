using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.test.white_box.RequestHandlerTest
{
    /// <summary>
    /// This class tests functionality corectness of the RequestHandler by testing the behavior of methods  
    /// provided by RequestHandler class, i.e. Behavioral testing. 
    /// </summary>
    public class RequestHandlerTests
    {


        /// <summary>
        /// This is a caller method, the only public method of this class, 
        /// which is responsible to run all test cases. 
        /// </summary>
        /// 
        public static void RunRequestHandlerTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>()
            {

            };

            Console.WriteLine("######################################### Going through RequestHandlerTests ######################################");

            foreach (string key in FunctionHashMap.Keys)
            {
                Console.WriteLine($"starting ----------->{key}<-----------------");
                FunctionHashMap[key]();
                Console.WriteLine($"-------------------->{key}<-----------passed\n\n");
            }
        }
    }
}
