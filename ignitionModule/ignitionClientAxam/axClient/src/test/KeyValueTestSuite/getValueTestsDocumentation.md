
## Test Documentation for KeyValueString Utility
This document outlines the test cases designed to validate the `getValue` method of the `KeyValueString` utility class. The tests cover various scenarios to ensure correctness and robustness, including branch coverage to verify the function's behavior under different conditions.
### Test Suite: getValueTests
The `getValueTests` suite covers various scenarios to ensure the correctness and robustness of the getValue method in the `KeyValueString` class. Each test case is designed to validate different aspects of the method's behavior under specific conditions.

#### Test Cases Overview
1. Exception Tests
* Test Case 1: `getValueExceptionTest` 
Validates that the method throws `IllegalArgumentException` for invalid input conditions:
- `null` `keyValueString`
- `null` `key`
- Empty `keyValueString`
- Blank `key`
- `key` not found in `keyValueString`
- Invalid characters in `keyValueString`

2. Valid Key-Value String Tests
* Test Case 2: `getValueKeyStringValueTest1`
- Validates successful extraction of value from a valid key-value string with trimmed spaces.
* Test Case 3: `getValueKeyStringValueTest2`
- Validates extraction of value from a key-value string with spaces in the value.

3. Object Value Tests
* Test Case 4: `getValueObjectTest1`
- Validates extraction of an object value associated with a specified key from the key-value string.

4. Array Value Tests
* Test Case 5: `getValueArrayTest`
- Validates extraction of an array value associated with a specified key from the key-value string.

5. Invalid Value Tests
* Test Case 6: `getValueInvalidString`
- Validates that the method throws IllegalArgumentException when attempting to extract an invalid string value.

6. Functional Tests
* Test Case 7: `getValueTest`
Comprehensive test covering multiple aspects:
- Extraction of nested objects and arrays.
- Extraction of primitive data types (integer, boolean).
- Verification of extracted values against expected results.


#### Branch Coverage
All tests within the getValueTests suite are designed to achieve branch coverage, ensuring that different paths and conditions within the getValue method are exercised during testing. The test suite aims to not only verify correct functionality but also to cover all logical branches within the method.

#### Execution Instructions
To execute these tests:
* Utilize a JUnit test runner compatible with the project's environment.
* Run individual test cases or execute the entire getValueTests suite to validate the behavior of the getValue method under various conditions.


### Conclusion
The test suite provides thorough coverage of the `getValue` method in the `KeyValueString` utility class, ensuring that the method functions correctly and handles different input scenarios effectively. These tests are essential for maintaining the reliability and functionality of the utility class, supporting both correctness and branch coverage criteria.