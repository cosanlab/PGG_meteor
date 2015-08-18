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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.amazonaws.mturk.cmd.test.util.ScriptRunner;
import com.amazonaws.mturk.cmd.test.util.ScriptRunner.ScriptResult;

public class TestBase extends com.amazonaws.mturk.cmd.test.TestBase {

    private static final File BIN_DIR;
    
    static {
        String MTURK_CMD_HOME = System.getenv("MTURK_CMD_HOME");
        if (MTURK_CMD_HOME == null) {
            throw new RuntimeException("MTURK_CMD_HOME not set");
        }
        BIN_DIR = new File(MTURK_CMD_HOME, "bin");
    }

    public TestBase(String name) {
        super(name);
    }
    
    protected ScriptResult runScript(String name, String... args) throws InterruptedException, IOException {
        return new ScriptRunner().runScript(BIN_DIR, name, Arrays.asList(args));
    }
    
    protected void expectError(ScriptResult result, String error) {
        assertTrue("Expected error <" + error + "> in <" + result.getOutput() + ">", 
            result.getOutput().contains(error));
    }

    protected void expectFailure(ScriptResult result) {
        expectError(result, "FAILURE");
    }

    protected void expectNoError(ScriptResult result) {
        assertFalse("Didn't expect error in <" + result.getOutput() + ">",
            result.getOutput().contains("ERROR"));
    }
    
    protected int getLineCount(String filename) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        try {
            int lineCount = 0;
            br =  new BufferedReader(new FileReader(filename));
            while (br.readLine() != null) {
                lineCount++;
            }
            return lineCount;
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
