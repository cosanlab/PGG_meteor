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

public class TestApproveWork extends TestBase {
    
    public static void main(String[] args) {
        TestRunner.run(TestApproveWork.class);
    }

    public TestApproveWork(String arg0) {
        super(arg0);
    }

    public void testApproveSuccessFile() throws Exception {
        String successFile = super.getSuccessFile();
        ScriptResult result = runScript("approveWork", "--force", "--successfile", successFile);
        assertEquals(0, result.getExitValue());
        deleteFile(successFile);
    }
    
    public void testInvalidFile() throws Exception {
        ScriptResult result = runScript("approveWork", "--force", "--approvefile", "filethatwentdavidcopperfield.txt");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testNullApproveFile() throws Exception {
        ScriptResult result = runScript("approveWork", "--approvefile");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testInvalidAssignmentIds() throws Exception {
        ScriptResult result = runScript("approveWork", "--force", "--assignment", "\"asst1,asst2\"");
        assertTrue(result.getExitValue() != 0);
    }
}
