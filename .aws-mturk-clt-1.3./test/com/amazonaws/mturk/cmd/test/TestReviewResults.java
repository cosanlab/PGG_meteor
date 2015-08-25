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

import com.amazonaws.mturk.cmd.RejectWork;
import com.amazonaws.mturk.cmd.ReviewResults;

public class TestReviewResults extends TestBase {
    
  public static void main(String[] args) {
    TestRunner.run(TestReviewResults.class);
  }

  public TestReviewResults(String arg0) {
    super(arg0);
  }

  public void testInvalidFile() {
    ReviewResults cmd = new ReviewResults();
    cmd.setSandBoxMode();
    cmd.setForce(true);
    try {
      cmd.reviewAssignments("filethatwentdavidcopperfield.txt");
    }
    catch (IOException e) {
      // expected
    }
  }
  
  public void testNullRejectFile() throws IOException {
    ReviewResults cmd = new ReviewResults();
    cmd.setSandBoxMode();
    
    try {
      cmd.reviewAssignments(null);
      fail("Expected failure");
    } catch (IllegalArgumentException e) {
      // expected
    }    
  }
}
