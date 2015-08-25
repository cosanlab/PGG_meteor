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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataOutput;
import com.amazonaws.mturk.addon.HITDataWriter;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.QualificationType;
import com.amazonaws.mturk.requester.QualificationTypeStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.ClientConfig;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class TestBase extends TestCase {
  
  protected String testDir = "../etc/test";
  protected String inputFile = testDir + "/testInput.tab";
  protected String propertiesFile = testDir + "/testProperties.txt";
  protected String propertiesFile2 = testDir + "/testProperties2.txt";
  protected String qualPropsFile = testDir + "/testQualProps.txt";
  protected String questionFile = testDir + "/testQuestion.txt";
  protected String qualificationTestFile = testDir + "/testQualificationTest.xml";
  protected String answerKeyFile = testDir + "/testAnswerKey.xml";
  protected String previewFile = "testPreview.tmp"; 
  protected String testHITId;
  protected String testHITTypeId;
  protected String testQualTypeId;
  protected String unique;
  
  protected TestStream logger;
  
  private static RequesterService service = null; 
  
  static {
    ClientConfig config = new PropertiesClientConfig();
    config.setServiceURL(ClientConfig.SANDBOX_SERVICE_URL); 
    service = new RequesterService(config);    
  }
  
  public static void main(String[] args) {
    TestRunner.run(TestBase.class);
  }

  public TestBase(String testName) {
    super(testName);
  }
  
  Enumeration old_appenders;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    unique = Long.toString(System.currentTimeMillis());
    
    // Capture output pipes in a StringBuilder
    logger = TestStream.getInstance();
    old_appenders = Logger.getRootLogger().getAllAppenders();
    Logger.getRootLogger().removeAllAppenders();
    Logger.getRootLogger().addAppender( logger );
    logger.resetRunningOutput();
  }
  
  @Override
  protected void tearDown() throws Exception {
    // Reset output pipes to normal
    Logger.getRootLogger().removeAppender( logger );
    while( old_appenders.hasMoreElements() ) {
      Logger.getRootLogger().addAppender( (Appender) old_appenders.nextElement() );
    }
    super.tearDown();
  }

    protected String getTestHITId() throws ServiceException {
        if (testHITId == null) {

            HIT hit = createHIT();
            testHITId = hit.getHITId();
            testHITTypeId = hit.getHITTypeId();
        }

        return testHITId;
    }

    protected HIT createHIT() throws ServiceException {
      HIT hit = service.createHIT(null, // HITTypeId
          "SDK test " + unique,
          "test description", null, // keywords
          RequesterService.getBasicFreeTextQuestion("What's up?"), 0.00,
          (long) 3600, (long) 3600,
          (long) 3600, 1, null, // requesterAnnotation
          null, // qual requirements
          null  // responseGroup
      );

      assertNotNull(hit);
      assertNotNull(hit.getHITId());

      return hit;
    }
  
  protected String getTestHITTypeId() {
    if (testHITTypeId == null) {

      HIT hit = createHIT();
      testHITId = hit.getHITId();
      testHITTypeId = hit.getHITTypeId();
    }

    return testHITTypeId;
  }

    protected String getSuccessFile() throws ServiceException, IOException {
        String testHITId = getTestHITId();
        String testHITTypeId = getTestHITTypeId();

        String successFile = "test" + Long.toString(System.currentTimeMillis()) + ".success";
        String successFilePath = new File(successFile).getAbsolutePath();
        HITDataOutput writer = new HITDataCSVWriter(successFilePath, '\t', false);
        try {
            writer.setFieldNames(new String[] { HITProperties.HITField.HitId.getFieldName(),
                HITProperties.HITField.HitTypeId.getFieldName()});
            
            Map<String, String> values = new HashMap<String, String>(); 
            values.put(HITProperties.HITField.HitId.getFieldName(), testHITId);
            values.put(HITProperties.HITField.HitTypeId.getFieldName(), testHITTypeId);
            writer.writeValues(values);
        }
        finally {
            writer.close();
        }
        return successFilePath;
    }

    protected String getTestQualificationTypeId() throws ServiceException {
        if (testQualTypeId == null) {
            testQualTypeId = this.createQualificationType().getQualificationTypeId();
        }

        return testQualTypeId;
    }
  
    protected QualificationType createQualificationType() throws ServiceException {
        QualificationType qualType = service.createQualificationType(
                "cmd tool test qual type" + unique,
                null, // keywords
                "cmd tool test qual type description", QualificationTypeStatus.Active, (long) 0,
                    null, // test
                    null, // answerKey
                    null, // testDurationInSeconds
                    null, // autoGranted
                    null // autoGrantedValue
                    );

                assertNotNull(qualType);

                return qualType;
    }

    protected void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            assertTrue("Failed to delete " + fileName, file.delete());
        }
    }

    protected void expectError() {
        expectError("ERROR");
    }
  
    /**
     * FIXME - This doesn't work.
     * @param error
     */
    protected void expectError(String error) {
        assertTrue("Expected error <" + error + "> in <" + logger.getRunningOutput() + ">", 
                logger.getRunningOutput().contains(error));
    }

    protected void expectNoError() {
        assertFalse("Didn't expect error in <" + logger.getRunningOutput() + ">", logger.getRunningOutput().contains("ERROR"));
    }

    protected void checkFile(String fileName, boolean shouldExist) {
        File f = new File(fileName);

        if (shouldExist) {
            assertTrue(f.exists());
        }
        else {
            assertFalse(f.exists());
        }
    }
}

