using log4net;
using NUnit.Framework;
using NUnit.Framework.Legacy;
using ServerSim.Log;
using ServerSim.test.white_box;
using ServerSim.utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.test.behavioral.ZipperTest
{

    ///<summary>
    ///	This class tests functionalities of the Zipper utility class. By testing data context first by 
    ///	compressing and then decompressing same data and check if it's same as the loaded. 
    ///
    ///	NOTE: THIS TEST CALSS CAN NOT BE RUN, TO MAKE USE OF IT GIVE CONCRETE DATA PATHS. 
    ///</summary>
    public  class ZipperTest
    {
        private static readonly ILog _log = LogManager.GetLogger(typeof(KeyValueStringTests));
        public static void RunZipperTests()
        {
            CentralLogger.LogInfo("***** Going through KeyValueStringTests *****\n", _log);

            CentralLogger.LogInfo("tests ---------->TestCompressAndDecompressDataConsistent", _log);
            TestCompressAndDecompressDataConsistent();
            CentralLogger.LogInfo("passed --------->TestCompressAndDecompressDataConsistent\n\n", _log);
        }

        private static void TestCompressAndDecompressDataConsistent()
        {
            string _5MB = ReadFile("../../../test/data/5MB");
            ClassicAssert.AreEqual(_5MB, CompressAndDecompress(_5MB));

            string _10MB = ReadFile("../../../test/data/10MB");
            ClassicAssert.AreEqual(_10MB, CompressAndDecompress(_10MB));

            //
            string _50MB = ReadFile("../../../test/data/50MB");
            ClassicAssert.AreEqual(_50MB, CompressAndDecompress(_50MB));

            //
            string _100MB = ReadFile("../../../test/data/100MB");
            ClassicAssert.AreEqual(_100MB, CompressAndDecompress(_100MB));

            //
            string _1000MB = ReadFile("../../../test/data/1000MB");
            ClassicAssert.AreEqual(_1000MB, CompressAndDecompress(_1000MB));


        }

        private static string CompressAndDecompress(string data)
        {
            Zipper zipper = new Zipper();
            try
            {
                byte[] compressed = zipper.Compress(data);
                return zipper.Decompress(compressed);
            }
            catch (Exception e)
            {
                CentralLogger.LogInfo($"Failed when testing data consistency: {e.Message}", _log);
                throw new Exception("very pain", e);
            }
        }

        private static string ReadFile(string filePath)
        {
            try
            {
                return File.ReadAllText(filePath);
            }
            catch (IOException e)
            {
                CentralLogger.LogInfo($"Failed when testing data consistency: {e.Message}", _log);
                throw new Exception("very pain", e);
            }
        }
    }
}
