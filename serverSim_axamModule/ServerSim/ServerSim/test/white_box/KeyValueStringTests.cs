
using log4net;
using NUnit.Framework;
using NUnit.Framework.Legacy;
using ServerSim.Log;
using ServerSim.utils;

namespace ServerSim.test.white_box
{

    public class KeyValueStringTests
    {
        private static readonly ILog _log = LogManager.GetLogger(typeof(KeyValueStringTests));
        public static void RunKeyValueStringTests()
        {
            Dictionary<string, Action> FunctionHashMap = new Dictionary<string, Action>() {
                {" GetValueKeyStringValueTest1",   GetValueKeyStringValueTest1},
                {"GetValueKeyStringValueTest2",  GetValueKeyStringValueTest2 },
                {"GetValueObjectTest1",  GetValueObjectTest1 },
                {"GetValueArrayTest",  GetValueArrayTest },
                {"GetValueInvalidString",  GetValueInvalidString },
                {"GetValueTest", GetValueTest }
            };

            CentralLogger.LogInfo("***** Going through KeyValueStringTests *****\n", _log);

            foreach (string testCase in FunctionHashMap.Keys)
            {
                CentralLogger.LogInfo($"tests ---------->{testCase}", _log);
                FunctionHashMap[testCase]();
                CentralLogger.LogInfo($"passed --------->{testCase}\n\n", _log);
            }
        }



        private static void GetValueKeyStringValueTest1()
        {

            string keyValueString = "\"reqId\":          \"getUsedSpace\"";
            ClassicAssert.AreEqual("getUsedSpace", KeyValueString.GetValue(keyValueString, "reqId"));

            string keyValueString2 = "\"usedMemory\"           :                   \"20%\"";
            ClassicAssert.AreEqual("20%", KeyValueString.GetValue(keyValueString2, "usedMemory"));
        }

        private static void GetValueKeyStringValueTest2()
        {
            string keyValueString = "\"reqId\"         :          \"           CPU-temp          \"";
            ClassicAssert.AreEqual("CPU-temp", KeyValueString.GetValue(keyValueString, "reqId"));
        }

        private static void GetValueObjectTest1()
        {
            string keyValueString = "\"user\"   :   {\"name\": \"u1!\", \"age\": 9990}";
            ClassicAssert.AreEqual("{\"name\": \"u1!\", \"age\": 9990}", KeyValueString.GetValue(keyValueString, "user"));
        }

        private static void GetValueArrayTest()
        {
            string keyValueString = "\"rooms\": [val1, val2, val3]";
            ClassicAssert.AreEqual("[val1, val2, val3]", KeyValueString.GetValue(keyValueString, "rooms"));
        }

        private static void GetValueInvalidString()
        {
            string s = "\"key\" : notString";
            bool thrown = false;
            try { KeyValueString.GetValue(s, "key"); } catch (ArgumentException e) { CentralLogger.LogInfo(e.ToString(), _log); thrown = true; }
            ClassicAssert.IsTrue(thrown);
        }

        private static void GetValueTest()
        {
            string json = "{\n" +
                "  \"person\": {\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78],\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"project\": {\n" +
                "    \"title\": \"JSON Examples\",\n" +
                "    \"isCompleted\": true,\n" +
                "    \"published\": false, " +
                "    \"startDate\": \"2024-04-01\",\n" +
                "    \"endDate\": \"2024-05-15\"\n" +
                "  }\n" +
                "}";

            ClassicAssert.AreEqual("{\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78],\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  }", KeyValueString.GetValue(json, "person"));

            ClassicAssert.AreEqual("40", KeyValueString.GetValue(json, "age"));
            ClassicAssert.AreEqual("true", KeyValueString.GetValue(json, "isCompleted"));
            ClassicAssert.AreEqual("false", KeyValueString.GetValue(json, "published"));
            ClassicAssert.AreEqual("{\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }", KeyValueString.GetValue(json, "address"));

            ClassicAssert.AreEqual("true", KeyValueString.GetValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "true" + "}}", "on"));
            ClassicAssert.AreEqual("8", KeyValueString.GetValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"temp\": " + 8 + "}}", "temp"));
            ClassicAssert.AreEqual("false", KeyValueString.GetValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "false" + "}}", "on"));
            ClassicAssert.AreEqual("{\"ip\": \"192.168.1.1\"}", KeyValueString.GetValue("\"serverIP\": {\"ip\": \"192.168.1.1\"}", "serverIP"));
        }


    }
}
