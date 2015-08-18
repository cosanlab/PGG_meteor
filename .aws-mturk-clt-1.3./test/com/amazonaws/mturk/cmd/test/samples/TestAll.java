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



package com.amazonaws.mturk.cmd.test.samples;

public class TestAll {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestAll.suite());
    }

    static public junit.framework.Test suite()
    {
        junit.framework.TestSuite newSuite = new junit.framework.TestSuite();
        newSuite.addTestSuite(TestBestImage.class);
        newSuite.addTestSuite(TestExternalHIT.class);
        newSuite.addTestSuite(TestHelloWorld.class);
        newSuite.addTestSuite(TestImageCategory.class);
        newSuite.addTestSuite(TestImageTagging.class);
        newSuite.addTestSuite(TestSimpleSurvey.class);
        newSuite.addTestSuite(TestSiteFilter.class);
        
        // NOTE: Without dispose qual functionality, new titles must be picked
        // each subsequent time these are run
        newSuite.addTestSuite(TestAssignQualification.class);
        newSuite.addTestSuite(TestJavaQualTest.class);
        newSuite.addTestSuite(TestQuizQual.class);
        newSuite.addTestSuite(TestSiteFilterQual.class);
        
        return newSuite;
    }
}
