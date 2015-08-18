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

public class TestLoadHITs extends TestBase {

    private String successFile = inputFile + ".success";
    
    public static void main(String[] args) {
        TestRunner.run(TestLoadHITs.class);
    }

    public TestLoadHITs(String arg0) {
        super(arg0);
    }
    
    public void testLoadHITs() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile);
        assertEquals(0, result.getExitValue());
        assertTrue(getLineCount(successFile) > 1);
        deleteFile(successFile);
    }
    
    public void testLoadHITsMaxHITs() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile, "--maxhits", "1");
        assertEquals(result.getExitValue(), 0);
        assertEquals(2, getLineCount(successFile));
        deleteFile(successFile);
    }
    
    public void testLoadHITsMaxHITsZero() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile, "--maxhits", "0");
        assertEquals(result.getExitValue(), 0);
        assertEquals(1, getLineCount(successFile));
        deleteFile(successFile);
    }
    
    public void testLoadHITsWrongInputFile() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", "wrongfile", "--question", questionFile, "--properties", propertiesFile);
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testLoadHITsWrongQuestionFile() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", "wrongfile", "--properties", propertiesFile);
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testLoadHITsWrongPropertiesFile() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", "wrongfile");
        assertTrue(result.getExitValue() != 0);
    }
    
    public void testLoadHITsPreview() throws Exception {
        String questionFile = testDir + "/best_image.question";
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile, "--preview");
        assertEquals(result.getExitValue(), 0);
        checkFile(successFile, false);
    }

    public void testLoadHITsNoPreview() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile, "--previewfile", previewFile);
        assertEquals(result.getExitValue(), 0);
        assertTrue(getLineCount(successFile) > 1);
        deleteFile(successFile);
        checkFile(previewFile, false);
    } 
    public void testLoadHITsPreviewFile() throws Exception {
        ScriptResult result = runScript("loadHITs", "--input", inputFile, "--question", questionFile, "--properties", propertiesFile, "--preview", "--previewfile", previewFile);
        assertEquals(result.getExitValue(), 0);
        checkFile(successFile, false);
        deleteFile(previewFile);
    }
}
