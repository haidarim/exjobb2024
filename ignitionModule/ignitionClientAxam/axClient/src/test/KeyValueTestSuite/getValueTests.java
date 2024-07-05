package test.KeyValueTestSuite;

import util.KeyValueString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * white-box testing of KeyValueString.getValue method, this TS covers branch coverage of the method.
 * @author  Mehdi Haidari
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
    public void getValueExceptionTest(){
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
        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("""
                {
                    "name": "Frank",
                    "age": 40,
                    "isStudent": false,
                    "grades": [85, 92, 78,
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }
                  }""", "grades"));
        assertThrows(IllegalArgumentException.class, ()-> KeyValueString.getValue("""
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                   \s
                  \
                """, "address"));


        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("""
                {
                    "name": "Frank",
                    "age": 40,
                    "credit": 5200
                                                                            "isStudent": false,
                    "grades": [85, 92, 78],
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }
                  }""", "credit"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("""
                {
                    "name": "Frank",
                    "age": 40,
                    "credit": 52dffw00
                                                                      ,      "isStudent": false,
                    "grades": [85, 92, 78],
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }
                  }""", "credit"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("""
                  "project": {
                    "title": "JSON Examples",
                    "isCompleted": true,
                    "published": false     "startDate": "2024-04-01",
                    "endDate": "2024-05-15"
                  }
                }""", "published"));

        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue("\"tested\": true   \"published\": false              ", "tested"));

    }


    @Test
        // "key":   "value"
        // "key"            :     "value"
    public void getValueKeyStringValueTest1(){
        String keyValueString = "\"reqId\":          \"getUsedSpace\"";
        assert(KeyValueString.getValue(keyValueString, "reqId").equals("getUsedSpace"));

        String keyValueString2 = "\"usedMemory\"           :                   \"20%\"";
        assert(KeyValueString.getValue(keyValueString2, "usedMemory").equals("20%"));
    }

    @Test
        // "key"            :     "          value                "
    public void getValueKeyStringValueTest2(){
        String keyValueString = "\"reqId\"         :          \"           CPU-temp          \"";
        assert(KeyValueString.getValue(keyValueString, "reqId").equals("CPU-temp"));
    }


    @Test
        // "user"   :   {"name": "u1!, "age": 9990}
    public void getValueObjectTest1(){
        String keyValueString = "\"user\"   :   {\"name\": \"u1!\", \"age\": 9990}";
        assert (KeyValueString.getValue(keyValueString,"user").equals("{\"name\": \"u1!\", \"age\": 9990}"));
    }

    @Test
        // "key": [val1, val2, val3]
    public void getValueArrayTest(){
        assert (KeyValueString.getValue("\"rooms\": [val1, val2, val3]","rooms").equals("[val1, val2, val3]"));
        assert (KeyValueString.getValue("\"rooms\": [val1, val2, val3], \"allBusy\": false","rooms").equals("[val1, val2, val3]"));
    }

    @Test
        // "key" : notString
    public void getValueInvalidString(){
        String s = "\"key\" : notString";
        assertThrows(IllegalArgumentException.class, ()->KeyValueString.getValue(s,"key"));
    }

    @Test
    public void getValueTest(){
        String json = """
                {
                  "person": {
                    "name": "Frank",
                    "age": 40,
                    "credit": 5200
                                                                        ,    "isStudent": false,
                    "grades": [85, 92, 78],
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }
                  },
                  "project": {
                    "title": "JSON Examples",
                    "isCompleted": true,
                    "published": false,     "startDate": "2024-04-01",
                    "endDate": "2024-05-15"
                  }
                }""";

        assert(KeyValueString.getValue(json, "person").equals("""
                {
                    "name": "Frank",
                    "age": 40,
                    "credit": 5200
                                                                        ,    "isStudent": false,
                    "grades": [85, 92, 78],
                    "address": {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }
                  }"""));

        assert(KeyValueString.getValue(json, "age").equals("40"));
        assert(KeyValueString.getValue(json, "credit").equals("5200")); ///////
        assert(KeyValueString.getValue(json, "isCompleted").equals("true"));
        assert(KeyValueString.getValue(json, "published").equals("false"));
        assert(KeyValueString.getValue("\"tested\": true", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true                   ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true     , \"published\": false              ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"tested\": true, \"published\": false              ", "tested").equals("true"));
        assert(KeyValueString.getValue("\"credit\": 50000000                                                 ", "credit").equals("50000000"));
        assert(KeyValueString.getValue(json, "address").equals("""
                {
                      "street": "456 Oak St",
                      "city": "Berlin"
                    }"""));


        assert(KeyValueString.getValue("""
                  "project": {
                    "title": "JSON Examples",
                    "isCompleted": true,
                    "published": false                       ,    "startDate": "2024-04-01",
                    "endDate": "2024-05-15"
                  }
                }""", "published").equals("false"));

        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "true" + "}}", "on").equals("true"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"on\": " + "false" + "}}", "on").equals("false"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"temp\": " + 8 + "}}", "deviceId").equals("4"));
        assert(KeyValueString.getValue("{\"auth-key\": \"string\" ,\"method\":\"post\" ,\"reqId\": \"setTemp\", \"body\": { \"deviceId\": \"" + 4 + "\", \"temp\": " + 8 + "}}", "temp").equals("8"));
    }

}