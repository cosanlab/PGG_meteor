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



package com.amazonaws.mturk.cmd.test.samples;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import com.amazonaws.mturk.cmd.test.util.ScriptRunner;
import com.amazonaws.mturk.cmd.test.util.ScriptRunner.ScriptResult;

public class TestBase extends TestCase {

    private static final File SAMPLES_DIR;
    
    static {
        String MTURK_CMD_HOME = System.getenv("MTURK_CMD_HOME");
        if (MTURK_CMD_HOME == null) {
            throw new RuntimeException("MTURK_CMD_HOME not set");
        }
        SAMPLES_DIR = new File(MTURK_CMD_HOME, "samples");
    }

    public TestBase(String name) {
        super(name);
    }
    
    protected ScriptResult runScript(String sample, String name, String... args) throws InterruptedException, IOException {
        File dir = new File(SAMPLES_DIR, sample);
        return new ScriptRunner().runScript(dir, name, Arrays.asList(args));
    }
}
