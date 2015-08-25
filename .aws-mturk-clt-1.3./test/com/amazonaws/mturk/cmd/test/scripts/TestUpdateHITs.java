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

public class TestUpdateHITs extends TestBase {

    private String successFile = inputFile + ".success";
    
    public static void main(String[] args) {
        TestRunner.run(TestUpdateHITs.class);
    }

    public TestUpdateHITs(String arg0) {
        super(arg0);
    }
    
    public void loadHITs() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile);
        assertEquals(0, result.getExitValue());
        assertTrue(getLineCount(successFile) > 1);
    }
    
    public void testUpdateHITs() throws Exception {
        loadHITs();
        int numHits = getLineCount(successFile) - 1;
        
        ScriptResult result = runScript("updateHITs", "--success", successFile, "--properties", propertiesFile2);
        assertEquals(0, result.getExitValue());
        String expectedText = numHits + " HITS were updated";
        assertTrue("Wrong number of updates", result.getOutput().contains(expectedText));
        
        deleteFile(successFile);
    }
    
    public void testUpdateHITsWrongInputFile() throws Exception {
        ScriptResult result = runScript("updateHITs", "--input", successFile, "--properties", "wrongfile");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testUpdateHITsWrongPropertiesFile() throws Exception {
        ScriptResult result = runScript("updateHITs", "--input", "wrongfile", "--properties", propertiesFile);
        assertTrue(result.getExitValue() != 0);
    }
}
