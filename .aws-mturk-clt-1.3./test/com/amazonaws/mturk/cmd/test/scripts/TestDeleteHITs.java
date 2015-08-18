/*
 * Copyright 2012 Amazon Technologies, Inc.
 * 
 * Licensed under the Amazon Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://aws.amazon.com/asl
 * 
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */ 



package com.amazonaws.mturk.cmd.test.scripts;

import com.amazonaws.mturk.cmd.test.util.ScriptRunner.ScriptResult;

import junit.textui.TestRunner;

public class TestDeleteHITs extends TestBase {

    private String successFile;
    
    public static void main(String[] args) {
        TestRunner.run(TestDeleteHITs.class);
    }

    public TestDeleteHITs(String arg0) {
        super(arg0);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        successFile = getSuccessFile();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteFile(successFile);
    }

    public void testHappyCase() throws Exception {
        ScriptResult result = runScript("deleteHITs", "--force", "--successfile", successFile, "--approve", "--expire");
        assertEquals(0, result.getExitValue());
        expectNoError(result);
    }
  
    public void testNoExpire() throws Exception {
        ScriptResult result = runScript("deleteHITs", "--force", "--successfile", successFile, "--approve");
        assertTrue(result.getExitValue() != 0);
        expectFailure(result);
    }

    public void testNoExpireNoApprove() throws Exception {
        ScriptResult result= runScript("deleteHITs", "--force", "--successfile", successFile);
        assertTrue(result.getExitValue() != 0);
        expectFailure(result);
    }
    
    public void testWrongSuccessFile() throws Exception {
        ScriptResult result = runScript("deleteHITs", "--force", "--successfile", "wrongfile");
        assertTrue(result.getExitValue() != 0);
    }
}
