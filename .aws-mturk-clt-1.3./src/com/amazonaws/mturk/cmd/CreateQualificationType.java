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


package com.amazonaws.mturk.cmd;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.QAPValidator;
import com.amazonaws.mturk.requester.QualificationType;
import com.amazonaws.mturk.requester.QualificationTypeStatus;
import com.amazonaws.mturk.service.exception.ObjectAlreadyExistsException;
import com.amazonaws.mturk.service.exception.ParseErrorException;
import com.amazonaws.mturk.service.exception.ValidationException;
import com.amazonaws.mturk.util.FileUtil;

public class CreateQualificationType extends AbstractCmd {

  private final String ARG_QUESTION = "question";
  private final String ARG_PROPERTIES = "properties";
  private final String ARG_ANSWER = "answer";
  private final String QUALTYPE = "qualtypeid";
  private final String ARG_NO_RETRY = "noretry";

  public CreateQualificationType() {}

  public static void main(String[] args) {
    CreateQualificationType dh = new CreateQualificationType();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_QUESTION, true, 
      "(optional) Path to the file containing the question");
    opt.addOption(ARG_PROPERTIES, true, 
      "(required) Path to the file containing the properties for the qualification");
    opt.addOption(ARG_ANSWER, true,
      "(optional) Path to the file containing the answers to the qual test. Omitting this makes it an untested qualification.");
    opt.addOption(ARG_NO_RETRY, false, 
      "(optional) If you do not want the workers to retry the qualification ");
  }

  protected void printHelp() {
    formatter.printHelp(CreateQualificationType.class.getName() 
        + " -" + ARG_QUESTION + " [path to question file] "
        + " -" + ARG_PROPERTIES + " [path to properties file] "
        + "(-" + ARG_ANSWER + " [path to answer file])",
        opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_PROPERTIES)) {

      log.error("Missing: -" + ARG_PROPERTIES + " [path to properties file]");

      System.exit(-1);

    } 

    boolean success = createQualificationType(cmdLine.getOptionValue(ARG_QUESTION), 
        cmdLine.getOptionValue(ARG_ANSWER), 
        cmdLine.getOptionValue(ARG_PROPERTIES), 
        cmdLine.hasOption(ARG_NO_RETRY));

    if( !success ) { System.exit(-1); } 

  }

  public boolean createQualificationType(String qualFile, String answerFile,
      String propertiesFile, boolean noRetry) {
    String test = null;
    String answerKey = null;
    Properties props = new Properties();

    try {
      if (qualFile != null) { 
        test = new FileUtil(qualFile).getString();
      }
      props = super.loadProperties(propertiesFile);

      if (answerFile != null) {
        answerKey = (new FileUtil(answerFile)).getString();
      }

    } catch (FileNotFoundException e) {
      log.error("Couldn't find one of the necessary files: " + e.getLocalizedMessage(), e);
      return false;
    } catch (IOException e) {
      log.error("Error reading one of the necessary files: " + e.getLocalizedMessage(), e);
      return false;
    }

    String autoGranted = props.getProperty("autogranted");
    String autoGrantedValue = props.getProperty("autograntedvalue");
    String testDuration = props.getProperty("testdurationinseconds", "10800"); //default to 3 hours
    Long retryDelay = noRetry ? null : Long.valueOf(props.getProperty("retrydelayinseconds", "259200")); // default to 3 days
    String qualName = props.getProperty("name");
    
    if (qualName==null || qualName.trim().length()==0) {
      log.error("Cannot create qualification type. The qualification name is not set. Please enter a value in the 'name' field in the properties file ("+propertiesFile+")");
      return false;
    }
    
    try {
      // merge Velocity templates if any
      test = getMergedTemplate(qualFile);
      answerKey = getMergedTemplate(answerFile);
      
      QualificationType qualType = service.createQualificationType(
          qualName, props.getProperty("keywords"), 
          props.getProperty("description"), QualificationTypeStatus.Active, 
          retryDelay,  // retryDelayInSeconds
          test, answerKey, 
          test != null ? Long.valueOf(testDuration) : null, // testDurationInSeconds 
              autoGranted != null ? Boolean.valueOf(autoGranted) : null, // autoGranted
                  autoGrantedValue != null ? Integer.valueOf(autoGrantedValue) : null // autoGrantedValue
      ); 

      if (qualType != null) {

        log.info("Created qualification type: "
            + qualType.getQualificationTypeId());
        FileWriter writer = new FileWriter(propertiesFile+".success");
        try {
            writer.write(QUALTYPE + System.getProperty("line.separator"));
            writer.write(qualType.getQualificationTypeId() + System.getProperty("line.separator"));
        }
        finally {
            writer.close();
        }
        log.info("You can take the test here: " 
            + service.getWebsiteURL() + "/mturk/requestqualification?qualificationId="
            + qualType.getQualificationTypeId() );
        return true;
      }
    }
    catch (ParseErrorException parseEx) {
      try {
        QAPValidator.validate(test);
        log.error("The qualification question is not valid: "+parseEx.getLocalizedMessage(), parseEx);
      }
      catch (ValidationException valEx) {
        log.error("The qualification question is not valid: "+valEx.getLocalizedMessage(), valEx);
      }
      catch (IOException ioEx) {
        log.error("The qualification question is not valid: "+parseEx.getLocalizedMessage(), ioEx);
      }
    }
    catch (ObjectAlreadyExistsException exitsEx) {
      log.error("The qualification type for name '"+qualName+"' already exists.\n");
      return false;
    }
    catch (Exception e) {      
      log.error("Error creating qualification type: " + e.getLocalizedMessage(), e);
      return false;
    }
    return false;
  }
}
