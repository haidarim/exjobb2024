## `sendRequest(String req, boolean timerOn, int maxTimeInSec)` Tests Documentation

---

This document describes the Behavior-Driven-Testing (BDT) scenarios 
for the `sendRequest` method in the `WindowsPipeCommunicator` class. By testing behavior of pipe client, the behavior of the pipe server will also be tested.  ............ 

These tests cover different scenarios to validate the robustness and correctness of the `sendRequest` method under various conditions, including:
- Situations when pipe server is not available (test cases 1-3). 
- Concurrent and sequential usage (test cases 4-6).
- Handling of invalid inputs (test case 7). 

The tests utilize thread-based concurrency to simulate real-world usage scenarios.

These test cases focuses on verifying interaction between multiple 
components involving inter-process-communication (IPC).  
They ensure the correctness of integrating the `sendRequest` method 
with external dependencies like the pipe server and underlying operating system mechanism. 

**General:** before running the pipe-server, run tests and wait until time t1 as maximum waiting time for test case 1 + t2 as maximum time for test case 2 (time ∑ ti) has passed, 
then run pipe-server. 
--- 

### Test Case 1: Sending request by a single thread when pipe server is not available 
**Objective:** To verify the correctness of the method's timer, 
this test case checks that the timer functions correctly and the method returns when the timer is on and the maximum waiting time is reached. 

**Procedure:**
- Issue a request when pipe is not available and the timer is on
- Check the methods behavior and return value after waiting time is up. 

**Expected behavior:** After maximum waiting time t, the method should return the following message: 
`"waiting time is up! please run pipe server and try again!"`. duration of method execution == maximum waiting time
---

### Test Case 2: Sending request by Threads {T1, ..., Tn} when timer is unavailable and timer is on
**Objective:** Verify the correctness of the timer when multiple threads send requests concurrently with the timer on.

**Procedure:**
- Create a thread pool.
- Each thread T sends a request while timer is on and the maximum waiting time is set to t. 
- The received response are stored in `actual` data structure. 
- Run all threads. 
- Check values stored in `actual`.  

**Test Details:**
- The `actual` storage must contain n keys when all threads have run, where n is the number of threads.
- Each key in `actual` has same value as `"waiting time is up! please run pipe server and try again!"`

--- 

### Test Case 3: Sending request when timer is off as well as pipe server 
**Objective:** Verifying the timer functionality when timer is off, `sendRequest` should waits until pipe server becomes available. 

**Procedure:**
- Make a single request with the timer off
- Maximum waiting time is set to t where 0<=t
- Pipe server is unavailable.  

**Test Details:**
- The method should wait until the pipe becomes available even if the time t is reached, since the timer is off. 
- The request will be sent as soon as the pipe is available. 

--- 

### Test Case 4: Multi-threaded Request Different Data
**Objective:** Verify that multiple threads can request different data through the 
communicator simultaneously. 

**Procedure:** 
- Create a thread pool, initialize threads, each requesting different data. 
- Compare the actual responses with the expected responses. 

**Test Details:**
- Expected responses are predefined for each requested data. 
- The test validates that all threads complete their tasks and the responses match the expected response.

---

### Tests Case 5: Multi-threaded Request Same Data
**Objective:** Confirm that multiple threads can simultaneously request the same data.

**Procedure:**
- Multiple threads request the same data type concurrently.
- Compare the actual responses with the expected response.

---

### Tests Case 6: Request Sequential by a Single-thread
**Objective:** Validate the behavior of sequential single-threaded requests.

**Procedure:**
- Execute requests for all data types sequentially in a single thread.
- Compare the actual responses with the expected responses.

**Test Details:**
- Requests are made one after another in a single thread.
- The test ensures that the responses received match the expected responses in sequence.

---


### Tests Case 7: Invalid Data Request Handling

**Objective:** Ensure that the method handles invalid input appropriately.

**Procedure:**
- Invoke the method with null, empty, and blank string inputs.
- Verify that `IllegalArgumentException` is thrown in each case.

**Test Details:**
- Tests are conducted to verify the method's behavior when given invalid inputs:
    - `null` input
    - Empty string input
    - Blank string input

  