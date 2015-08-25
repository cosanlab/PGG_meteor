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



package com.amazonaws.mturk.cmd.test.scripts;

public class TestAll {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestAll.suite());
    }

    static public junit.framework.Test suite()
    {
        junit.framework.TestSuite newSuite = new junit.framework.TestSuite();
        newSuite.addTestSuite(TestApproveWork.class);
        newSuite.addTestSuite(TestBlockWorker.class);
        newSuite.addTestSuite(TestCreateQualificationType.class);
        newSuite.addTestSuite(TestDeleteHITs.class);
        newSuite.addTestSuite(TestExtendHITs.class);
        newSuite.addTestSuite(TestGetBalance.class);
        newSuite.addTestSuite(TestGetResults.class);
        newSuite.addTestSuite(TestLoadHITs.class);
        newSuite.addTestSuite(TestRejectWork.class);
        newSuite.addTestSuite(TestUpdateHITs.class);
        newSuite.addTestSuite(TestUpdateQualificationType.class);
        newSuite.addTestSuite(TestResetAccount.class);
        
        return newSuite;
    }
}
