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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.QualificationRequest;
import com.amazonaws.mturk.service.axis.RequesterService;

public class EvaluateQualificationRequests extends AbstractCmd {

  public EvaluateQualificationRequests() {

  }
  
  private static final String QUAL_TYPE = "qualtypeid";
  private static final String INPUT_FILE = "input";
  
  public static void main(String[] args) {
    EvaluateQualificationRequests dh = new EvaluateQualificationRequests();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(QUAL_TYPE, true,
      "(optional) The qualification type ID to view requests for");
    opt.addOption(INPUT_FILE, true,
      "(optional) File containing the qualification type ID to view requests for");
    opt.addOption("answers", true,
      "(optional) A file containing the answerKey");
    opt.addOption("preview", false,
      "(optional) This will preview what workers will be granted the qual without actually doing it");
  }
  
  protected void printHelp() {
    formatter.printHelp(EvaluateQualificationRequests.class.getName() + 
        " -" + QUAL_TYPE + " [the qualification type to be evaluated]", opt);    
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(QUAL_TYPE) &&
        !cmdLine.hasOption(INPUT_FILE)) {

      log.error("Either -" + QUAL_TYPE + " or -" + INPUT_FILE + " should be passed");

      System.exit(-1);

    } else {
      String qualIds[] = null;
      if (cmdLine.hasOption(QUAL_TYPE)) {
        qualIds = new String[1];
        qualIds[0] = cmdLine.getOptionValue( QUAL_TYPE );
      }
      else {
        try {
          qualIds = super.getFieldValuesFromFile(cmdLine.getOptionValue(INPUT_FILE), QUAL_TYPE);
        }
        catch (IOException e) {
          log.error("error occured reading file: " + e.getLocalizedMessage(), e);
          System.exit(-1);
        }
        if (qualIds.length == 0) {
          log.error("qualtypeid must be present in the -" + INPUT_FILE + " file");
          System.exit(-1);
        }
      }
      
      for (String qualId : qualIds) {
        evaluateQualificationRequests(qualId,
            cmdLine.getOptionValue("answers"),
            cmdLine.hasOption("preview"));
      }
    }
  }

  private void evaluateQualificationRequests(String qualid, String answerFile,
      boolean preview) {
    
    if (preview) {
        log.info("Preview flag is set. Qualification requests will not be approved or rejected.");
    }
    QualificationRequest[] qr = null;
    
    try {
      
      qr = service.getQualificationRequests(qualid);
      
    } catch (Exception e) {
      log.error("Error fetching qualification requests for qualification "
          + qualid, e);
    }
    
    if (qr == null || qr.length == 0) {
      log.info("No qualification requests found to review for "+qualid);
      return;
    }

    Properties answerKey = null;
    if (answerFile != null) {
      try {
        
        answerKey = loadProperties(answerFile);
        
      } catch (IOException e) {
        log.error("Your answers file '" + answerFile + "' could not be read: " + e.getLocalizedMessage(), e);
      }
    }

    for (int i = 0; qr != null && i < qr.length; i++) {
      // TODO: Make the scoring more advanced by having the properties file define the math behind how it's calculated
      //     You could get really fancy and make the approval/reject logic be based in velocity. You'd then want a 
      //     flag to "dry run" that logic before approving and rejecting a bunch of people.
      //     Better yet, use a regular expression to match. That's the way to do this!
      
      int scoreToGiveIfCorrect = 100;
      boolean passedTest = true;
      log.info("---[Worker " + qr[i].getSubjectId()
          + "]--------------------------------------------------------------");

      try {
        List<QuestionFormAnswersType.AnswerType> answers = (List<QuestionFormAnswersType.AnswerType>) 
          RequesterService.parseAnswers(qr[i].getAnswer()).getAnswer();
        if (answerKey != null) {
          for (QuestionFormAnswersType.AnswerType thisAnswer : answers) {
          
          
            
            // Let's adjudicate the answers
            String correctAnswer = answerKey.getProperty(thisAnswer.getQuestionIdentifier());
            
            if (correctAnswer == null) {
              log.info("Missing answer for question: " + thisAnswer.getQuestionIdentifier() + " in answer key");
              System.exit(-1);
            }
            
            String givenAnswer = RequesterService.getAnswerValue(null, thisAnswer); // null is assignmentId
            
            boolean isCorrect = correctAnswer.equalsIgnoreCase(givenAnswer);
            
            if (isCorrect) {
              
              log.info("Question " + thisAnswer.getQuestionIdentifier() + ":CORRECT [The answer key '" + correctAnswer
                  + "' matches the given answer '" + givenAnswer + "']");
              
            } else {
              
              log.info("Question " + thisAnswer.getQuestionIdentifier() + ":WRONG [The correct answer '" + correctAnswer
                  + "' did not match the given answer '" + givenAnswer + "']");
              
              passedTest = false;
              break;
              
            }
          }
      } else {
          
          log.info("Missing answer key");
          System.exit(-1);
       }

        if (passedTest) {
          
          log.info("Worker " + qr[i].getSubjectId()
              + " has PASSED your test and scored " + scoreToGiveIfCorrect);
          
          try {
            if (!preview) {
              
              service.grantQualification(qr[i].getQualificationRequestId(),
                  scoreToGiveIfCorrect);
              log.info("Qualification request granted.");
              
            }

          } catch (Exception e) {
            
            log.error("Error granting qualification "
                + qr[i].getQualificationTypeId() + " to worker "
                + qr[i].getSubjectId() + " with score " + scoreToGiveIfCorrect, e);
            
          }
        } else {
          
          log.info("Worker " + qr[i].getSubjectId()
                  + " has FAILED your test. They will not be granted the qualification.");
          try {
            
            // null is rejection reason
              if (!preview) {
                  service.rejectQualificationRequest(qr[i].getQualificationRequestId(), null);
                  log.info("Qualification request rejected.");
              }
          } catch (Exception e) {
            
            log.error("Error rejecting qualification request "
                + qr[i].getQualificationTypeId() + " from worker "
                + qr[i].getSubjectId(), e);
            
          }
        }

        log.info("---------------------------------------------------------------------------------------");
      } catch (Exception e) {
        log.error("Error examining test results for worker "
            + qr[i].getSubjectId() + " and qualification request "
            + qr[i].getQualificationRequestId(), e);
      }
    }
  }
}
