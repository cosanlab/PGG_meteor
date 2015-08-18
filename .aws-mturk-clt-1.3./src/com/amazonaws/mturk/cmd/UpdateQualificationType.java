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
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.requester.QualificationType;
import com.amazonaws.mturk.requester.QualificationTypeStatus;
import com.amazonaws.mturk.util.FileUtil;
import com.amazonaws.mturk.util.WsdlEnumUtil;

public class UpdateQualificationType extends AbstractCmd {

  private final String ARG_QUALTYPE = "qualtypeid";
  private final String ARG_INPUT_FILE = "input";
  private final String ARG_STATUS = "status";
  private final String ARG_QUESTION = "question";
  private final String ARG_PROPERTIES = "properties";
  private final String ARG_ANSWER = "answer";

  public UpdateQualificationType() {}

  public static void main(String[] args) {
    UpdateQualificationType dh = new UpdateQualificationType();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_QUALTYPE, true, "(optional) Qualification Type Id you wish to update" );
    opt.addOption(ARG_INPUT_FILE, true,
        "(optional) File containing the qualification type ID you wish to update");
    opt.addOption(ARG_STATUS, true, "(optional) Status for the qualification type ( either " +
        QualificationTypeStatus.Active + " or " + QualificationTypeStatus.Inactive + " ) " );
    opt.addOption(ARG_QUESTION, true, "(optional) Path to the file containing a new question");
    opt.addOption(ARG_PROPERTIES, true, 
        "(optional) Path to the file containing new properties for the qualification");
    opt.addOption(ARG_ANSWER, true,
        "(optional) Path to the file containing new answers to the qual test." +
        " System cannot grade the test automatically if this is omitted.");
  }

  protected void printHelp() {
    formatter.printHelp(UpdateQualificationType.class.getName() +
        " -" + ARG_QUALTYPE + " [QualTypeId] " +
        "(-" + ARG_STATUS + " [Active|Inactive]) " +
        "(-" + ARG_QUESTION + " [path to question file]) " +
        "(-" + ARG_PROPERTIES + " [path to properties file]) " +
        "(-" + ARG_ANSWER + " [path to answer file])", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

      if (!cmdLine.hasOption(ARG_QUALTYPE) &&
          !cmdLine.hasOption(ARG_INPUT_FILE)) {
          log.error("Either -" + ARG_QUALTYPE + " or -" + ARG_INPUT_FILE + " should be passed");
        System.exit(-1);
      } 
      
      String qualId = null;
      if (cmdLine.hasOption(ARG_QUALTYPE)) {
          qualId = cmdLine.getOptionValue( ARG_QUALTYPE );
      } else {
          String qualIds[] = null;
          try {
              qualIds = getFieldValuesFromFile(cmdLine.getOptionValue(ARG_INPUT_FILE), ARG_QUALTYPE);
          } catch (IOException e) {
              log.error("error occured reading file: " + e.getLocalizedMessage(), e);
              System.exit(-1);
          }
          if (qualIds == null || qualIds.length == 0) {
              log.error("qualtypeid must be present in the -" + ARG_INPUT_FILE + " file");
              System.exit(-1);
          }
          qualId = qualIds[0];
      }
      
      try {
        updateQualificationType(qualId, 
              cmdLine.getOptionValue( ARG_STATUS ),
              cmdLine.getOptionValue( ARG_QUESTION ),
              cmdLine.getOptionValue( ARG_ANSWER ),
              cmdLine.getOptionValue( ARG_PROPERTIES ) );
      } catch (Exception e) {
        log.error("Error updating qualification type: " + e.getLocalizedMessage(), e);
        System.exit(-1);
      }
  }

  public void updateQualificationType( String qualTypeId, String statusString,
      String questionFile, String answerFile, 
      String propertiesFile ) throws Exception
  {
    String test = null;
    String answerKey = null;
    Properties props = new Properties();
    QualificationTypeStatus status = null;

    try {
      if (questionFile != null ) {
        test = new FileUtil(questionFile).getString();
      }
      if (answerFile != null ) {
        answerKey = new FileUtil(answerFile).getString();
      }
    } catch (FileNotFoundException e ) {
      log.error( "Couldn't find one of the specified files: " + e.getLocalizedMessage(), e);
    } catch (IOException e ) {
      log.error( "Error reading one of the specified files: " + e.getLocalizedMessage(), e);
    }

    try {
      if (propertiesFile != null ) {
        props = super.loadProperties(propertiesFile);
      }
    } catch (Exception e ) {
      log.error( "Failed to load properties file: " + e.getLocalizedMessage(), e);
    }

    try {
        if (statusString != null ) {
          status = WsdlEnumUtil.fromStringIgnoreCase(QualificationTypeStatus.class, statusString);
        } else if (props.getProperty("status") != null ) {
          status = WsdlEnumUtil.fromStringIgnoreCase(QualificationTypeStatus.class, props.getProperty("status"));
        }
    }
    catch (IllegalArgumentException iae) {
        throw new IllegalArgumentException("Invalid status [" + statusString + "]. It should be " + WsdlEnumUtil.getValuesPossibilityDescription(QualificationTypeStatus.class) + ".");
    }
    
    Long duration = null;
    if (props.getProperty("testdurationinseconds") != null ) { 
      duration = Long.valueOf( props.getProperty("testdurationinseconds") );
    }
    Long retryDelay = null;
    if (props.getProperty("retrydelayinseconds") != null ) { 
      retryDelay = Long.valueOf( props.getProperty("retrydelayinseconds") );
    }
    Boolean autoGrant = null;
    if (props.getProperty("autogranted") != null ) { 
      autoGrant = Boolean.valueOf( props.getProperty("autogranted") );
    }
    Integer autoValue = null;
    if (props.getProperty("autograntedvalue") != null ) { 
      autoValue = Integer.valueOf( props.getProperty("autograntedvalue") );
    }

    // merge Velocity templates if any
    test = getMergedTemplate(questionFile);
    answerKey = getMergedTemplate(answerFile);
      
    QualificationType qt = service.updateQualificationType( qualTypeId, 
        props.getProperty("description"), status, test, answerKey,
        duration, retryDelay, autoGrant, autoValue );
    log.info("Updated qualification type: " + qt.getQualificationTypeId() );
  }
}
