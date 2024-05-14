package ax.test.KeyValueTestSuite;

import org.junit.jupiter.api.Test;
import ax.util.KeyValueString;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * white-box testing of KeyValueString.getValue method, this TS covers branch coverage of the method.
 * */
public class getValueTests {

    @Test
    // (null, "key")
    // ("string", null)
    // ("", "key")
    // ("string", "    ")
    // ("string", "key")
    // ("key","key")
    // ("#key", "key")
    void getValueExceptionTest(){
        // first if statement
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue(null, "key"));
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("json", null));
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("", "key"));
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("json", " "));
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("json", "key"));

        // second if statement
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("key", "key"));
        // third if statement
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("#key", "key"));
        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("\"key\"     val", "key"));

        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("\"user\"   :   {\"name\": \"u1!, \"age\": 9990}", "user"));
        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("{\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78,\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  }", "grades"));
        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    \n" +
                "  ", "address"));


        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("{\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"credit\": 5200\n                                                        "+
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78],\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  }", "credit"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("{\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"credit\": 52dffw00\n                                                      ,  "+
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78],\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  }", "credit"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue( "  \"project\": {\n" +
                "    \"title\": \"JSON Examples\",\n" +
                "    \"isCompleted\": true,\n" +
                "    \"published\": false "+
                "    \"startDate\": \"2024-04-01\",\n" +
                "    \"endDate\": \"2024-05-15\"\n" +
                "  }\n" +
                "}", "published"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("\"tested\": true   \"published\": false              ", "tested"));

    }


    @Test
        // "key":   "value"
        // "key"            :     "value"
    void getValueKeyStringValueTest1(){
        String keyValueString = "\"reqId\":          \"getUsedSpace\"";
        assert(KeyValueString.getValue(keyValueString, "reqId").equals("getUsedSpace"));

        String keyValueString2 = "\"usedMemory\"           :                   \"20%\"";
        assert(KeyValueString.getValue(keyValueString2, "usedMemory").equals("20%"));
    }

    @Test
        // "key"            :     "          value                "
    void getValueKeyStringValueTest2(){
        String keyValueString = "\"reqId\"         :          \"           CPU-temp          \"";
        assert(KeyValueString.getValue(keyValueString, "reqId").equals("CPU-temp"));
    }


    @Test
        // "user"   :   {"name": "u1!, "age": 9990}
    void getValueObjectTest1(){
        String keyValueString = "\"user\"   :   {\"name\": \"u1!\", \"age\": 9990}";
        assert (KeyValueString.getValue(keyValueString,"user").equals("{\"name\": \"u1!\", \"age\": 9990}"));
    }

    @Test
    // "key": [val1, val2, val3]
    void getValueArrayTest(){
        assert (KeyValueString.getValue("\"rooms\": [val1, val2, val3]","rooms").equals("[val1, val2, val3]"));
        assert (KeyValueString.getValue("\"rooms\": [val1, val2, val3], \"allBusy\": false","rooms").equals("[val1, val2, val3]"));
    }

    @Test
    // "key" : notString
    void getValueInvalidString(){
        String s = "\"key\" : notString";
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue(s,"key"));
    }

    @Test
    void getValueTest(){
        String json = "{\n" +
                "  \"person\": {\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"credit\": 5200\n                                                        ,"+
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
                "    \"published\": false, "+
                "    \"startDate\": \"2024-04-01\",\n" +
                "    \"endDate\": \"2024-05-15\"\n" +
                "  }\n" +
                "}";

        assert(KeyValueString.getValue(json, "person").equals("{\n" +
                "    \"name\": \"Frank\",\n" +
                "    \"age\": 40,\n" +
                "    \"credit\": 5200\n                                                        ,"+
                "    \"isStudent\": false,\n" +
                "    \"grades\": [85, 92, 78],\n" +
                "    \"address\": {\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }\n" +
                "  }"));

        assert(KeyValueString.getValue(json, "age").equals("40"));
        assert(KeyValueString.getValue(json, "credit").equals("5200")); ///////
        assert(KeyValueString.getValue(json, "isCompleted").equals("true"));
        assert(KeyValueString.getValue(json, "published").equals("false"));
        assert(KeyValueString.getValue("\"tested\": true", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true                   ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true     , \"published\": false              ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true, \"published\": false              ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"credit\": 50000000                                                 ", "credit").equals("50000000"));
        assert(KeyValueString.getValue(json, "address").equals("{\n" +
                "      \"street\": \"456 Oak St\",\n" +
                "      \"city\": \"Berlin\"\n" +
                "    }"));


        assert(KeyValueString.getValue( "  \"project\": {\n" +
                "    \"title\": \"JSON Examples\",\n" +
                "    \"isCompleted\": true,\n" +
                "    \"published\": false                       ,"+
                "    \"startDate\": \"2024-04-01\",\n" +
                "    \"endDate\": \"2024-05-15\"\n" +
                "  }\n" +
                "}", "published").equals("false"));

        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "true" + "}}", "on").equals("true"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "false" + "}}", "on").equals("false"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"temp\": " + 8 + "}}", "deviceId").equals("4"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"temp\": " + 8 + "}}", "temp").equals("8"));
    }

}