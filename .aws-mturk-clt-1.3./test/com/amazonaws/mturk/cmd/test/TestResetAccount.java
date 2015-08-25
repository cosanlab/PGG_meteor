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

import com.amazonaws.mturk.cmd.ResetAccount;
import junit.textui.TestRunner;

public class TestResetAccount extends TestBase {

  private static ResetAccount cmd = null;
  
  static {
    cmd = new ResetAccount();
    cmd.setSandBoxMode();
    cmd.setForce(true);
  }

  public static void main(String[] args) {
    TestRunner.run(TestResetAccount.class);
  }

  public TestResetAccount(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws Exception {
    cmd.deleteAllHITs();
  }
}
