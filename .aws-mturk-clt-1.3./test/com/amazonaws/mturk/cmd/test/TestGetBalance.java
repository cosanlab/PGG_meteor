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

import com.amazonaws.mturk.cmd.GetBalance;

public class TestGetBalance extends TestBase {

  private static GetBalance cmd = null;
  
  static {
    cmd = new GetBalance();
    cmd.setSandBoxMode();
  }

  public static void main(String[] args) {
    TestRunner.run(TestGetBalance.class);
  }

  public TestGetBalance(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws Exception {
    cmd.getBalance();
  }
}
