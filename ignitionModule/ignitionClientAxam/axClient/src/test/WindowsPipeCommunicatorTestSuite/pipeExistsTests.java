package test.WindowsPipeCommunicatorTestSuite;


import communication.WPC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/************************************************
 *  Behavior testing of {@code pipeExists}      *
 *  @author  Mehdi Haidari                      *
 * **********************************************/
public class pipeExistsTests {

    //test case1: pipe does not exist
    @Test
    void pipeExistsTestWhenItDoesNot(){
        assertFalse(WPC.pipeExists("test_in_pipeExistsTests"));
    }

    // test case2: pipe exists
    //run this test when pipe-server is running
    @Test
    void pipeExistsTestWhenItDoes(){
        assertTrue(WPC.pipeExists("readPipe"));
    }
}
