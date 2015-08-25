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

import com.amazonaws.mturk.cmd.GrantQualificationRequests;

public class TestGrantQualificationRequests extends TestBase {
  
  private String approveFile = "qualRequestsToApprove.txt";
  
  public static void main(String[] args) {
    TestRunner.run(TestGrantQualificationRequests.class);
  }

  public TestGrantQualificationRequests(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws IOException {
    GrantQualificationRequests cmd = new GrantQualificationRequests();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    cmd.grantQualRequestsInFile(approveFile,42);  
  }
  
  public void testNullApproveFile() throws IOException {
    GrantQualificationRequests cmd = new GrantQualificationRequests();
    cmd.setSandBoxMode();
    try {
    	cmd.grantQualRequestsInFile(null,null);
    	fail("Expected exception");
    } catch (IllegalArgumentException e) {
    	// expected
    }
  }
  
  public void testInvalidQualificationRequestIds() throws IOException {
    GrantQualificationRequests cmd = new GrantQualificationRequests();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    try {
    	cmd.grantQualRequests("qualReq1,qualReq2",42);
    	fail("Failed to throw exception");
    } catch (Exception e) {
    	// expected
    }
  }
}
