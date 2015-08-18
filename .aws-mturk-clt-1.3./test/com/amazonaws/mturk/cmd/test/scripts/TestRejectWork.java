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

public class TestRejectWork extends TestBase {
    
    public static void main(String[] args) {
        TestRunner.run(TestRejectWork.class);
    }

    public TestRejectWork(String arg0) {
        super(arg0);
    }

    public void testInvalidFile() throws Exception {
        ScriptResult result = runScript("rejectWork", "--force", "--rejectfile", "filethatwentdavidcopperfield.txt");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testNoRejectFile() throws Exception {
        ScriptResult result = runScript("rejectWork", "--rejectfile");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testInvalidAssignmentIds() throws Exception {
        ScriptResult result = runScript("rejectWork", "--force", "--assignment", "\"asst1,asst2\"");
        assertTrue(result.getExitValue() != 0);
        expectError(result, "Error rejecting assignment");
    }
}
