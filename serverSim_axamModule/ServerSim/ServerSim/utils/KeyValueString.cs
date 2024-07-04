using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.utils
{
    public class KeyValueString
    {

        // use false and true with lowecase, this method is sensitiv to upper-and lowercase 
        public static string GetValue(string keyValueString, string key)
        {
            if (string.IsNullOrEmpty(keyValueString) || string.IsNullOrEmpty(key) || string.IsNullOrWhiteSpace(keyValueString) || string.IsNullOrWhiteSpace(key) || !keyValueString.Contains(key))
            {
                throw new ArgumentException("Invalid input or key not found.");
            }

            // start index of the key
            int startOfKey = keyValueString.IndexOf("\"" + key + "\"");
            if (startOfKey == -1)
            {
                throw new ArgumentException("Key not found in the keyValueString.");
            }
            // Find the index of spearation char
            int startOfValue = keyValueString.IndexOf(':', startOfKey);
            if (startOfValue == -1)
            {
                throw new ArgumentException("Invalid format: key : val");
            }
            startOfValue++;

            int endOfValue = startOfValue;
            
            // Skip whitespace after colon
            while (endOfValue < keyValueString.Length && char.IsWhiteSpace(keyValueString[endOfValue]))
            {
                endOfValue++;
            }

            // Determine the type of value
            char valueTypeStart = keyValueString[endOfValue];

            if (valueTypeStart == '"')
            {
                // Value is a string
                startOfValue = endOfValue + 1;
                endOfValue = keyValueString.IndexOf('"', startOfValue);
                if (endOfValue == -1)
                {
                    throw new ArgumentException("Invalid format: missing closing quote for value.");
                }
            }
            else if (valueTypeStart == '{' || valueTypeStart == '[')
            {
                // Value is an object or array 
                int depth = 0;
                bool inQuotes = false;
                char expectedEndChar = (valueTypeStart == '{') ? '}' : ']';

                while (endOfValue < keyValueString.Length)
                {
                    char currentChar = keyValueString[endOfValue];

                    if (currentChar == '"')
                    {
                        inQuotes = !inQuotes;
                    }

                    if (!inQuotes)
                    {
                        if (currentChar == valueTypeStart)
                        {
                            depth++;
                        }
                        else if (currentChar == expectedEndChar)
                        {
                            depth--;
                            if (depth == 0)
                            {
                                break; 
                            }
                        }
                    }

                    endOfValue++;
                }

                if (endOfValue == keyValueString.Length)
                {
                    throw new ArgumentException("Invalid format: missing closing character for object or array.");
                }

                endOfValue++; // Include the closing character
            }
            else
            {
                // boolean or integer
                if (StartsWith(keyValueString, "false", endOfValue)/*keyValueString.Substring(endOfValue, 5).Equals("false")*/)
                {
                    endOfValue += 5;
                }else if (StartsWith(keyValueString, "true", endOfValue)/*keyValueString.Substring(endOfValue, 4).Equals("true")*/)
                {
                    endOfValue += 4;
                }else if (valueTypeStart >= 48 && valueTypeStart<=57)
                {
                    while (endOfValue<keyValueString.Length && keyValueString[endOfValue] != ',' && keyValueString[endOfValue]>=48 && keyValueString[endOfValue]<=57)
                    {
                        endOfValue++;
                    }
                }
                else
                {
                    throw new ArgumentException("invalid valueType");
                }
               
                int current = endOfValue;
                while (current < keyValueString.Length && char.IsWhiteSpace(keyValueString[current]) && keyValueString[current] != ',')
                {
                    current++;
                }
               
                if (current!=keyValueString.Length)
                {
                    char token = keyValueString[current];
                    if (token != ',' && token != '}' && token != ']') {
                        throw new ArgumentException("invalid format: missing closing character for boolean or integer");
                    }
                }
            }
            
            return keyValueString.Substring(startOfValue, endOfValue - startOfValue).Trim();
        }

        private static bool StartsWith(string origin, string prefix, int tooffset)
        {
            string s = origin.Substring(tooffset);
            if (s.Length <prefix.Length)
            {
                return false;
            }
            for (int i = 0; i < prefix.Length; i++)
            {
                if (s[i] != prefix[i])
                {
                    return false;
                }
            }

            return true;
        }


        private void PrintErr(string errMessage, Exception e)
        {
            Console.WriteLine($"{errMessage}: {e.Message}");
        }

    }


}
