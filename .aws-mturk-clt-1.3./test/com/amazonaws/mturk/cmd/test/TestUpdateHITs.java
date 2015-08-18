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



package com.amazonaws.mturk.cmd.test;

import java.io.File;

import com.amazonaws.mturk.cmd.LoadHITs;
import com.amazonaws.mturk.cmd.UpdateHITs;
import com.amazonaws.mturk.requester.HIT;

import junit.textui.TestRunner;

public class TestUpdateHITs extends TestBase {
  
    public static void main(String[] args) {
        // Change the output to log to a file   
        TestRunner.run(TestUpdateHITs.class);
    }    

    public TestUpdateHITs(String arg0) {
        super(arg0);
    }

    @Override
    public void setUp() {
        new File(inputFile + ".success").delete();
    }
    
    @Override
    public void tearDown() {
        new File(inputFile + ".success").delete();
    }
    
    protected HIT[] loadHITs() throws Exception {
        LoadHITs loadCmd = new LoadHITs();
        loadCmd.setSandBoxMode();
        HIT[] hits = loadCmd.loadHITs(inputFile, questionFile, 
                propertiesFile, "", loadCmd.MAX_HITS_UNLIMITED, false);
        assertNotNull(hits);
        assertTrue(hits.length > 0);
        return hits;
    }

    public void testUpdateHITs() throws Exception {
        HIT[] hits = loadHITs();
        
        UpdateHITs updateCmd = new UpdateHITs();
        updateCmd.setSandBoxMode();
        HIT[] newHits = updateCmd.updateHITs(inputFile + ".success", 
                propertiesFile2);
        assertNotNull(newHits);
        assertTrue("Got 0 updates reading hitids from " + inputFile + ".success", newHits.length > 0);
        assertEquals("Wrong number of updates", hits.length, newHits.length);
    }

    public void testUpdateHITsWrongInputFile() throws Exception {
        UpdateHITs cmd = new UpdateHITs();
        cmd.setSandBoxMode();
        try {
            cmd.updateHITs("wrongfile", propertiesFile);
            fail();
        } catch (Exception e) {
            // expected
        }
    }

    public void testUpdateHITsWrongPropertiesFile() throws Exception {
        UpdateHITs cmd = new UpdateHITs();
        cmd.setSandBoxMode();
        try {
            cmd.updateHITs(inputFile, "wrongfile");  
            fail();
        } catch (Exception e) {
            // expected
        }
    }

}
