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

import com.amazonaws.mturk.cmd.ExtendHITs;

public class TestExtendHITs extends TestBase {

  private static ExtendHITs cmd = null;
  
  static {
    cmd = new ExtendHITs();
    cmd.setSandBoxMode();
  }

  public static void main(String[] args) {
    TestRunner.run(TestExtendHITs.class);
  }

  public TestExtendHITs(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws Exception {

    String successFile = getSuccessFile();
    cmd.extendHITs(successFile, new Integer(1), null);

    expectNoError();
  }
}
