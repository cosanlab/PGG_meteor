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

import com.amazonaws.mturk.cmd.test.util.ScriptRunner.ScriptResult;

import junit.textui.TestRunner;

public class TestQuizQual extends TestBase {
    
    public static final String SAMPLE_NAME = "quiz_qual";
    
    public static void main(String[] args) {
        TestRunner.run(TestQuizQual.class);
    }

    public TestQuizQual(String arg0) {
        super(arg0);
    }

    public void testHappyCase() throws Exception {
        // TODO: Without dispose qual functionality, a new title must be picked
        // each subsequent time this is run
        ScriptResult result = runScript(SAMPLE_NAME, "run");
        assertEquals(0, result.getExitValue());
    }
}
