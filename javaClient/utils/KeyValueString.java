package utils;

/*************************************************************************************************************************************************
 * This class has only one method i.e. {@code getValue(String keyValueString, key)}                                                              *
 *  If the specified key-value-string is a real key-value-string this static method returns the value belonging to the specified key             *
 *  A KeyValueString is a String where:                                                                                                          *
 *      the key should between dubble quotations i.e. "key"                                                                                      *
 *      the key and value should be separated by a ':'                                                                                           *
 *      the key has a value as object, string, array, boolean or integer                                                                         *
 *                                                                                                                                               *
 * A JSON string is a valid key-value string if the key is valid according to JSON format, but not necessarily vice versa.                       *
 *                                                                                                                                               *
 * @author Mehdi Haidari                                                                                                                         *
  * **********************************************************************************************************************************************/
public class KeyValueString {

    /**
     * @param  keyValueString, a key-value-string
     * @param key, a key belonging to the key-value-string
     * @return value of the specified key in the keyValueString
     * @throws IllegalArgumentException if:
     * 1. keyValueString == null || isBlank
     * 2. key == null || isBlank
     * 3. !keyValueString.contains(key)
     * 4. keyValueString is not a valid key-value-string
     * */
    public static  String getValue(String keyValueString, String key) {
        if (keyValueString == null || key == null || keyValueString.isBlank() || key.isBlank() || !keyValueString.contains(key)) {
            throw new IllegalArgumentException("Invalid input or key not found.");
        }

        // start index of the key
        int startOfKey = keyValueString.indexOf("\"" + key + "\"");
        if (startOfKey == -1) {
            throw new IllegalArgumentException("Key not found in the keyValueString.");
        }

        // Find the index of spearation char
        int startOfValue = keyValueString.indexOf(':', startOfKey);
        if(startOfValue == -1){
            throw new IllegalArgumentException("Invalid format: key : val");
        }
        startOfValue ++;
        int endOfValue = startOfValue;
        int keyValueStringLen = keyValueString.length();
        // Skip whitespace after colon
        while (endOfValue < keyValueStringLen && Character.isWhitespace(keyValueString.charAt(endOfValue))) {
            endOfValue++;
        }

        // Determine the type of value
        char valueTypeStart = keyValueString.charAt(endOfValue);
        if (valueTypeStart == '"') {
            // Value is a string
            startOfValue = endOfValue + 1;
            endOfValue = keyValueString.indexOf('"', startOfValue);
            if (endOfValue == -1) {
                throw new IllegalArgumentException("Invalid format: missing closing quote for value.");
            }
        } else if (valueTypeStart == '{' || valueTypeStart == '[') {
            // Value is an object or array (nested structure)
            int depth = 0;
            boolean inQuotes = false;
            char expectedEndChar = (valueTypeStart == '{') ? '}' : ']';

            while (endOfValue < keyValueStringLen) {
                char currentChar = keyValueString.charAt(endOfValue);

                if (currentChar == '"') {
                    inQuotes = !inQuotes; // Toggle quotes state
                }

                if (!inQuotes) {
                    if (currentChar == valueTypeStart) {
                        depth++;
                    } else if (currentChar == expectedEndChar) {
                        depth--;
                        if (depth == 0) {
                            break; // Found the end of the object or array
                        }
                    }
                }

                endOfValue++;
            }
            if (endOfValue == keyValueStringLen) {
                throw new IllegalArgumentException("Invalid format: missing closing character for object or array.");
            }
            endOfValue++; // Include the closing character
        } else{
            //primitive types e.g. boolean, integer
            if(keyValueString.startsWith("false", endOfValue)){
                //boolean false
                endOfValue += 5;
            } else if(keyValueString.startsWith("true", endOfValue)){
                // boolean true
                endOfValue += 4;

            }else if(valueTypeStart >= 48 && valueTypeStart <=57){
                //integer
                while (endOfValue<keyValueStringLen && keyValueString.charAt(endOfValue)!=',' && keyValueString.charAt(endOfValue)>=48 &&  keyValueString.charAt(endOfValue)<=57){
                    endOfValue++;
                }
            }else{
                throw new IllegalArgumentException("Invalid format: invalid dataType!.");
            }

            int current = endOfValue;
            while(current<keyValueStringLen && Character.isWhitespace(keyValueString.charAt(current)) && keyValueString.charAt(current) !=','){
                current ++;
            }
            if (current != keyValueStringLen) {
                char token = keyValueString.charAt(current);
                if(token != ',' && token != ']' && token != '}'){
                    throw new IllegalArgumentException("Invalid format: missing closing character for boolean or integer.");
                }
            }
        }
        return keyValueString.substring(startOfValue, endOfValue).strip();
    }

    /**
     * @throws  AssertionError
     * */
    public static String toValidKeyValueRequest(String key, String method, String reqId, String body){
        if (key == null || key.isBlank() || method == null || method.isBlank() || reqId == null || reqId.isBlank() || body == null || body.isBlank()){
            throw new IllegalArgumentException("Invalid input, null or blank");
        }
        key = key.strip();
        method = method.strip();
        reqId = reqId.strip();
        body = body.strip();
        return "{\"auth-key\": \""+ key +"\" ," +
                "\"method\":\""+ method +"\", " +
                "\"reqId\": \""+ reqId +"\", " +
                "\"body\": " + body +"}";
    }

    public static  String getResponseBody(String response){
        return getValue(response, "body");
    }

}




