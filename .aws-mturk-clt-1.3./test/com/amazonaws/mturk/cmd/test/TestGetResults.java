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

import java.io.FileNotFoundException;

import junit.textui.TestRunner;

import com.amazonaws.mturk.cmd.GetResults;

public class TestGetResults extends TestBase {

  private static GetResults cmd = null;
  private static String outputFile = "testGetResults.tmp";
  
  static {
    cmd = new GetResults();
    cmd.setSandBoxMode();
  }

  public static void main(String[] args) {
    TestRunner.run(TestGetResults.class);
  }

  public TestGetResults(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws Exception {

    String successFile = getSuccessFile();
    cmd.getResults(successFile, outputFile);

    expectNoError();
    deleteFile(outputFile);
  }

  public void testWrongSuccessFile() throws Exception {

    try {
      cmd.getResults("wrongfile", outputFile);
      fail("Should throw exception");
    } catch (FileNotFoundException e) {
      // expected
    }
  }

  public void testNullOutputFile() throws Exception {

    try {
      String successFile = getSuccessFile();
      cmd.getResults(successFile, null);
      fail("Should throw exception");
    } catch (Exception e) {
      // expected
    }
    checkFile(outputFile, false);
  }

  public void testNullSuccessFile() throws Exception {

    try {
      cmd.getResults(null, outputFile);
      fail("Should throw exception");
    } catch (Exception e) {
      // expected
    }

    checkFile(outputFile, false);
  }
}
