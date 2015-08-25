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

public class TestSiteFilterQual extends TestBase {
    
    public static final String SAMPLE_NAME = "site_filter_qual";
    
    public static void main(String[] args) {
        TestRunner.run(TestSiteFilterQual.class);
    }

    public TestSiteFilterQual(String arg0) {
        super(arg0);
    }

    public void testHappyCase() throws Exception {
        ScriptResult result = null;
        
        // TODO: Without dispose qual functionality, a new title must be picked
        // each subsequent time this is run
        result = runScript(SAMPLE_NAME, "createQualification");
        assertEquals(0, result.getExitValue());
        
        result = runScript(SAMPLE_NAME, "updateQualification");
        assertEquals(0, result.getExitValue());
        result = runScript(SAMPLE_NAME, "deactivateQualification");
        assertEquals(0, result.getExitValue());
    }
}
