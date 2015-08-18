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

import com.amazonaws.mturk.cmd.ApproveWork;

public class TestApproveWork extends TestBase {
  
  public static void main(String[] args) {
    TestRunner.run(TestApproveWork.class);
  } 

  public TestApproveWork(String arg0) {
    super(arg0);
  }
  
  public void testApproveSuccessFile() throws Exception {
    String successFile = super.getSuccessFile();
    
    ApproveWork cmd = new ApproveWork();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    cmd.approveHitsInFile(successFile);
    deleteFile(successFile);
  }  

  public void testInvalidFile() {
    ApproveWork cmd = new ApproveWork();
    cmd.setForce(true);
    cmd.setSandBoxMode();
    try {
      cmd.approveAssignmentsInFile("filethatwentdavidcopperfield.txt");
    }
    catch (IOException e) {
      //expected
    }
  }
  
  public void testNullApproveFile() throws IOException {
    ApproveWork cmd = new ApproveWork();
    cmd.setSandBoxMode();
    try {
    	cmd.approveAssignmentsInFile(null);
    	fail("Expected exception");
    } catch (IllegalArgumentException e) {
    	// expected
    }
  }
  
  public void testInvalidAssignmentIds() throws IOException {
    ApproveWork cmd = new ApproveWork();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    cmd.approveAssignments("asst1,asst2");
    expectError(); 
  }
}
