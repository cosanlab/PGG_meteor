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

import com.amazonaws.mturk.cmd.UpdateQualificationType;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class TestUpdateQualificationType extends TestCase {
  
  public static void main(String[] args) {
    TestRunner.run(TestUpdateQualificationType.class);
  }

  public TestUpdateQualificationType(String arg0) {
    super(arg0);
  }

  public void testHappyCase() {
      UpdateQualificationType cmd = new UpdateQualificationType();
      cmd.setSandBoxMode();
      try {
        cmd.updateQualificationType( "bogusID", null, null, null, null );
        fail("expected exception");
      } catch (Exception e ) {
          // expected
      }
  }
	
}
