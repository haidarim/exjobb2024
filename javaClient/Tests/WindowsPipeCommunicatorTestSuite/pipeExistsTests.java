package Tests.WindowsPipeCommunicatorTestSuite;


import org.junit.jupiter.api.Test;
import src.WindowsPipeCommunicator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/************************************************
 *  Behavior testing of {@code pipeExists}      *
 * **********************************************/
public class pipeExistsTests {

    //test case1: pipe does not exist
    @Test
    void pipeExistsTestWhenItDoesNot(){
        assertFalse(WindowsPipeCommunicator.pipeExists("test_in_pipeExistsTests"));
    }

    // test case2: pipe exists
    //run this test when pipe-server is running
    @Test
    void pipeExistsTestWhenItDoes(){
        assertTrue(WindowsPipeCommunicator.pipeExists("readPipe"));
    }
}
