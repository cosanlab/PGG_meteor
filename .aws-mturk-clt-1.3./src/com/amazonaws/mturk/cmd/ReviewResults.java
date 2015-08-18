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
import org.apache.commons.cli.CommandLine;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITResults;

public class ReviewResults extends AbstractCmd {
  
  private final static String ASSIGNMENT_COLUMN = 
      HITProperties.AssignmentField.AssignmentId.getFieldName();
  
  private final static String REJECT_COLUMN = 
      HITProperties.AssignmentField.RejectFlag.getFieldName();
  
  private final static String DEFAULT_COMMENT = HITResults.EMPTY;
      
  private final String ARG_RESULTS = "resultsfile";

  private int approvedCount = 0;
  private int rejectedCount = 0;
  private int errorCount = 0;
  private int runningCount = 0;
  
  public ReviewResults () {}
  
  public static void main(String[] args) {
    ReviewResults jtf = new ReviewResults();
    jtf.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_RESULTS, true,
        "The name of the results file that contains the row entries for the " 
            + REJECT_COLUMN + " column");
  }
  
  protected void printHelp() {
    formatter.printHelp(ReviewResults.class.getName() 
        + " -" + ARG_RESULTS + " [path to rejection file]}", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_RESULTS)) {

      log.error("Missing: you must supply -" + ARG_RESULTS);
      System.exit(-1);

    }

    if (cmdLine.hasOption(ARG_RESULTS)) {
      reviewAssignments(cmdLine.getOptionValue(ARG_RESULTS));
    }
    
    if (errorCount > 0 ) {
      System.exit(-1);
    }
  }

  public void reviewAssignments(String fileName) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException("File name is null.");
    }
    
    // all assignment IDs
    String[] assignmentIds = super.getFieldValuesFromFile(fileName, ASSIGNMENT_COLUMN);
    
    // all reject flags
    String[] rejectFlags = super.getFieldValuesFromFile(fileName, REJECT_COLUMN);
    
    boolean approveAll = false;
    if (assignmentIds == null) {
        throw new IllegalArgumentException("Could not find any assignmentIds.  Does " 
                + ASSIGNMENT_COLUMN + " column exist?");
        
    } else if (rejectFlags == null) {
        
        // If the reject column doesn't exist, 
        // approve all assignments upon confirmation
        
        checkIsUserCertain("You are about to approve ALL assignments.");
        approveAll = true;
        
    } else if (rejectFlags.length != assignmentIds.length) {
        throw new IllegalArgumentException();
    }
    
    for (int i=0; i<assignmentIds.length; i++) {
        
        String assignmentId = assignmentIds[i];
        if (assignmentId.equals(""))
            continue; // no submitted assignment
        
        try {
            String opStr;
            String rejectFlag = rejectFlags != null ? rejectFlags[i] : HITResults.EMPTY;
            
            if (approveAll || rejectFlag.equals(HITResults.EMPTY)) {
                
                // For each assignment that doesn't have the reject column marked, 
                // the system will approve the assignment.
                
                service.approveAssignment(assignmentId, DEFAULT_COMMENT);
                approvedCount++;
                opStr = "approved";
                
            } else {
                
                // For each assignment that has the reject column marked, 
                // the system will reject the assignment. 

                service.rejectAssignment(assignmentId, DEFAULT_COMMENT);
                rejectedCount++;
                opStr = "rejected";
            }
            
            log.info("[" + assignmentId + "] Assignment successfully " + opStr);
            
        } catch (Exception e) {
            errorCount++;
            log.error("Could not process assignment [" + assignmentId + "], " 
                    + e.getLocalizedMessage(), e);
        }
        
        runningCount++;
    }
    
    log.info("");
    log.info(String.format("Assignments approved: %d/%d (%d%%)", approvedCount, runningCount, approvedCount*100/runningCount));
    log.info(String.format("Assignments rejected: %d/%d (%d%%)", rejectedCount, runningCount, rejectedCount*100/runningCount));
    log.info(String.format("Errors occurred: %d", errorCount));
  }
}
