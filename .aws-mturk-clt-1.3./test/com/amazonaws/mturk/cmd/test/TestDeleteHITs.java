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

import java.io.IOException;

import junit.textui.TestRunner;

import com.amazonaws.mturk.cmd.DeleteHITs;

public class TestDeleteHITs extends TestBase {
  
  private static DeleteHITs cmd = new DeleteHITs();
  private final static boolean APPROVE = true;
  private final static boolean EXPIRE = true;
  private String successFile;
    
  public static void main(String[] args) {
    // Change the output to log to a file   
    TestRunner.run(TestDeleteHITs.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    cmd.setForce(true);
    cmd.setSandBoxMode();
    successFile = getSuccessFile();
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    //deleteFile(successFile);
  }

  public TestDeleteHITs(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws Exception {
    
    cmd.deleteHITs(successFile, APPROVE, EXPIRE);
    
    expectNoError();
  }
  
  public void testNoExpire() throws Exception {
    
    cmd.deleteHITs(successFile, APPROVE, !EXPIRE);
    
    expectError(); 
  }

  public void testNoExpireNoApprove() throws Exception {
    
    cmd.deleteHITs(successFile, !APPROVE, !EXPIRE);
    
    expectError(); 
  }

  public void testWrongSuccessFile() throws Exception {
    try {
      cmd.deleteHITs("wrongfile", !APPROVE, !EXPIRE);
    }
    catch (IOException e) {
      // expected
    }
  }
}
