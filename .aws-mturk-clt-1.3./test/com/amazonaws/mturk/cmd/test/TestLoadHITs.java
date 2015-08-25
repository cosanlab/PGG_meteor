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

import junit.textui.TestRunner;

import com.amazonaws.mturk.cmd.LoadHITs;
import com.amazonaws.mturk.requester.HIT;

public class TestLoadHITs extends TestBase {

  public static void main(String[] args) {
    // Change the output to log to a file   
    TestRunner.run(TestLoadHITs.class);
  }

  public TestLoadHITs(String arg0) {
    super(arg0);
  }

  public void testLoadHITs() throws Exception {
    // Change the output to log to a file
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, "", cmd.MAX_HITS_UNLIMITED, false);

    assertNotNull(hits);
    assertTrue(hits.length > 0);
  }

  public void testLoadHITsMaxHITs() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, "", 1, false);  

    assertTrue(hits.length == 1);
  }

  public void testLoadHITsMaxHITsZero() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT [] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, "", 0, false);  

    assertTrue(hits.length == 0);
  }

  public void testLoadHITsWrongInputFile() throws Exception {
    LoadHITs cmd = new LoadHITs();

    try {
      cmd.loadHITs("wrongfile", questionFile, 
          propertiesFile, "", 1, false);
    } catch (Exception e) {
      // expected
    }
  }

  public void testLoadHITsWrongQuestionFile() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    try {
      cmd.loadHITs(inputFile, "wrongfile", 
          propertiesFile, "", 1, false);
    } catch (Exception e) {
      // expected
    }
  }

  public void testLoadHITsWrongPropertiesFile() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    try {
      cmd.loadHITs(inputFile, questionFile, 
          "wrongfile", "", 1, false);  
    } catch (Exception e) {
      // expected
    }
  }

  public void testLoadHITsNullLabel() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, "", 1, false);  

    assertNotNull(hits);
    assertTrue(hits.length == 1);
  }

  // Known failure.  Preview works when tested manually, but it doesn't work here.
  // Needs investigation.
  public void testLoadHITsPreview() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, this.testDir + "/best_image.question", 
        propertiesFile, null, 1, true);

    assertNull(hits);
  }

  // Known failure.  Preview works when tested manually, but it doesn't work here.
  // Needs investigation.
  public void testLoadHITsNoPreview() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, previewFile, 1, false);  

    assertNotNull(hits);
    checkFile(previewFile, false);
  }

  public void testLoadHITsPreviewFile() throws Exception {
    LoadHITs cmd = new LoadHITs();
    cmd.setSandBoxMode();

    HIT[] hits = cmd.loadHITs(inputFile, questionFile, 
        propertiesFile, previewFile, 1, true);  

    assertNull(hits);
    deleteFile(previewFile);
  }

}

