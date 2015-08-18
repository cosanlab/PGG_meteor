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

public class RejectWork extends AbstractCmd {
  
  private final static String DELIM_CHAR = ",";
  private final static String ASSIGNMENT_TO_REJECT_COLUMN = "assignmentIdToReject";
  private final static String ASSIGNMENT_TO_REJECT_COMMENT_COLUMN = "assignmentIdToRejectComment";
  
  private final String ARG_ASSIGNMENT = "assignment"; 
  private final String ARG_REJECTFILE = "rejectfile";
  private final String ARG_FORCE = "force";

  private int successCount = 0;
  private int failedCount = 0;
  private int runningCount = 0;
  
  public RejectWork () {}
  
  public static void main(String[] args) {
    RejectWork jtf = new RejectWork();
    jtf.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_ASSIGNMENT, true,
        "The ID of the assignment to reject (separate multiple assignment IDs with a comma)");
    opt.addOption(ARG_REJECTFILE, true,
        "The name of the file that contains the assignment IDs to be rejected (the column must be titled '" +
        ASSIGNMENT_TO_REJECT_COLUMN + "' and the comment column must be titled '" +
        ASSIGNMENT_TO_REJECT_COMMENT_COLUMN + "')");
    opt.addOption(ARG_FORCE, false,
        "(optional) Do not prompt for confirmation (DANGEROUS)");
  }
  
  protected void printHelp() {
    formatter.printHelp(RejectWork.class.getName() 
        + " -" + ARG_ASSIGNMENT + " | "
        + " -" + ARG_REJECTFILE + " [path to rejection file]}", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_ASSIGNMENT) && !cmdLine.hasOption(ARG_REJECTFILE)) {

      log.error("Missing: you must supply one of -assignment or -rejectfile.");
      System.exit(-1);

    }

    setForce(cmdLine.hasOption(ARG_FORCE));
    if (cmdLine.hasOption(ARG_ASSIGNMENT)) {
      rejectAssignments(cmdLine.getOptionValue("assignment"));
    }

    if (cmdLine.hasOption(ARG_REJECTFILE)) {
      rejectAssignmentsInFile(cmdLine.getOptionValue("rejectfile"));
    }
    
    if (failedCount > 0 ) {
      System.exit(-1);
    }
  }

  public void rejectAssignmentsInFile(String fileName) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException();
    }
    
    String[] assignments = super.getFieldValuesFromFile(fileName,
        ASSIGNMENT_TO_REJECT_COLUMN);
    
    String[] comments = super.getFieldValuesFromFile(fileName,
        ASSIGNMENT_TO_REJECT_COMMENT_COLUMN);
    
    rejectAssignments(assignments, comments);
  }

  public void rejectAssignments(String assignmentIds) {
    if (assignmentIds == null) {
      throw new IllegalArgumentException();
    }
    
    String[] assignments = assignmentIds.split(DELIM_CHAR);
    rejectAssignments(assignments);
  }

  private void rejectAssignments(String[] assignments) {
    rejectAssignments(assignments, null);
  }

  private void rejectAssignments(String[] assignments, String[] comments) {
    
    // If we're not given anything, just no-op
    if (assignments == null) {
      return;
    }
    
    checkIsUserCertain("You are about to reject " + assignments.length + " assignment(s).");
    String comment = null;

    if (comments == null) {
      comment = getComment();
    }

    for (int i = 0; assignments != null && i < assignments.length; i++) {
      runningCount++;
      try {
        if (comments != null)
          comment = comments[i];

        service.rejectAssignment(assignments[i], comment);
        successCount++;
        log.info("[" + assignments[i]
                                   + "] Assignment successfully rejected "
                                   + (comment != null ? " with comment (" + comment + ")" : ""));

      } catch (Exception e) {
        failedCount++;
        log.error("Error rejecting assignment " + assignments[i]
                                                              + " with comment [" + comment + "]: " + e.getLocalizedMessage(), e);
      }
    }
  }

}
