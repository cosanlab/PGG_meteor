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

import com.amazonaws.mturk.cmd.RejectQualificationRequests;

public class TestRejectQualificationRequests extends TestBase {
  
  private String rejectFile = "qualRequestsToReject.txt";
  
  public static void main(String[] args) {
    TestRunner.run(TestRejectQualificationRequests.class);
  }

  public TestRejectQualificationRequests(String arg0) {
    super(arg0);
  }

  public void testHappyCase() throws IOException {
    RejectQualificationRequests cmd = new RejectQualificationRequests();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    cmd.rejectQualRequestsInFile(rejectFile);  
  }
  
  public void testNullRejectFile() throws IOException {
    RejectQualificationRequests cmd = new RejectQualificationRequests();
    cmd.setSandBoxMode();
    
    try {
      cmd.rejectQualRequestsInFile(null);
      fail("Expected failure");
    } catch (IllegalArgumentException e) {
      // expected
    }    
  }
  
  public void testInvalidQualficationRequestIds() throws IOException {
    RejectQualificationRequests cmd = new RejectQualificationRequests();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    try {
      cmd.rejectQualRequests("qualReq1,qualReq2");
      fail("Expected failure");
    } catch (Exception e) {
      // expected
    }
  }
}
