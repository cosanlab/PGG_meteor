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

import com.amazonaws.mturk.cmd.BlockWorker;
import com.amazonaws.mturk.service.exception.InvalidParameterValueException;

public class TestBlockWorker extends TestBase {

    public static void main(String[] args) {
        TestRunner.run(TestBlockWorker.class);
      }

      public TestBlockWorker(String arg0) {
        super(arg0);
      }

      public void testHappyCase() throws IOException {
        
        BlockWorker cmd = new BlockWorker();
        cmd.setSandBoxMode();

        try {
            cmd.blockWorker("imaginary","no reason");
            fail("Expected failure");
        } catch (InvalidParameterValueException e) {
            // expected
        }

      }

}
