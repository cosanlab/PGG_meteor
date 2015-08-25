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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.service.axis.WorkQueue;

public class ApproveWork extends AbstractCmd implements BatchItemCallback {

  private final static String DELIM_CHAR = ",";
  private final static String ASSIGNMENT_TO_APPROVE_COLUMN = "assignmentIdToApprove";
  private final static String ASSIGNMENT_TO_APPROVE_COMMENT_COLUMN = "assignmentIdToApproveComment";
  private final static String HIT_TO_APPROVE_COLUMN = "hitid";
  
  private final String ARG_ASSIGNMENT = "assignment"; 
  private final String ARG_APPROVEFILE = "approvefile";
  private final String ARG_SUCCESSFILE = "successfile"; 
  private final String ARG_FORCE = "force";
  
  private int successCount = 0;
  private int failedCount = 0;
  private Map<String, String> resultMap = null;
  private final static String RESULT_TEMPLATE_COMMENT = " with comment '%s'";
  private final static String RESULT_TEMPLATE_HIT = " for HIT %s";
  private String resultTemplate = RESULT_TEMPLATE_COMMENT;
  
  public ApproveWork () {}
  
  public static void main(String[] args) {
    ApproveWork jtf = new ApproveWork();
    jtf.run(args);
  }
  
  protected void initOptions() {
    opt.addOption(ARG_ASSIGNMENT, true,
        "The ID of the assignment to approve (separate multiple assignment IDs with a comma)");
    opt.addOption(ARG_APPROVEFILE, true,
        "The name of the file that contains the assignment IDs to be approved (the column must be titled '" + 
        ASSIGNMENT_TO_APPROVE_COLUMN + "' and the comment column must be titled '" +
        ASSIGNMENT_TO_APPROVE_COMMENT_COLUMN + "')");
    opt.addOption(ARG_FORCE, false,
        "(optional) Do not prompt for confirmation (DANGEROUS)");
    opt.addOption(ARG_SUCCESSFILE, true,
        "A path to the success file returned by the call to LoadHITs. For each HIT in the file," +
        "the operation attempts to approve all assignments.");
  }
  
  protected void printHelp() {
    formatter.printHelp(ApproveWork.class.getName() 
        + " -" + ARG_ASSIGNMENT + " | "
        + " -" + ARG_APPROVEFILE + " [path to approval file]}", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_ASSIGNMENT) && !cmdLine.hasOption(ARG_APPROVEFILE) && !cmdLine.hasOption(ARG_SUCCESSFILE)) {

      log.error("Missing: you must supply one of -" + ARG_ASSIGNMENT 
          + " or -" + ARG_APPROVEFILE + " or -" + ARG_SUCCESSFILE);
      System.exit(-1);

    } 
    
    log.info("--- Starting approval ---");
    
    setForce(cmdLine.hasOption(ARG_FORCE));
    if (cmdLine.hasOption(ARG_ASSIGNMENT)) {
      approveAssignments(cmdLine.getOptionValue(ARG_ASSIGNMENT));
    }
    else if (cmdLine.hasOption(ARG_APPROVEFILE)) {
      approveAssignmentsInFile(cmdLine.getOptionValue(ARG_APPROVEFILE));
    }    
    else if (cmdLine.hasOption(ARG_SUCCESSFILE)) {
      approveHitsInFile(cmdLine.getOptionValue(ARG_SUCCESSFILE));
    }    
    
    // print summary
    log.info("--- Finished approval ---");
    log.info("  " + successCount + " assignments approved.");
    log.info("  " + failedCount + " assignments failed to be approved.");
    if (failedCount > 0) {
        System.exit(-1);
    }
  }

  public void approveAssignmentsInFile(String fileName) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException("fileName must not be null");
    }
    
    String[] assignments = super.getFieldValuesFromFile(fileName,
        ASSIGNMENT_TO_APPROVE_COLUMN);
    
    if (assignments != null) {    
      String[] comments = super.getFieldValuesFromFile(fileName,
          ASSIGNMENT_TO_APPROVE_COMMENT_COLUMN);

      if (comments != null) {
        if (assignments.length != comments.length) {
          log.error("Cannot approve assignments: Row mismatch for assignment IDs and comments");
          return;
        }

        // filter emtpy IDs (since Feature 1826272 getResults now outputs HITs without assignments)
        List<String> effectiveAssignments = new ArrayList<String>();
        List<String> effectiveComments = new ArrayList<String>();
        for (int i = 0; assignments != null && i < assignments.length; i++) {
          if (assignments[i] != null && assignments[i].length() > 0) {
            effectiveAssignments.add(assignments[i]);
            effectiveComments.add(comments[i]);
          }
        }

        assignments = new String[effectiveAssignments.size()];
        effectiveAssignments.toArray(assignments);
        comments = new String[effectiveAssignments.size()];
        effectiveComments.toArray(comments);
      }    

      approveAssignments(assignments, comments);
    }
  }

  public void approveAssignments(String assignmentIds) {
    if (assignmentIds == null) {
      return;
    }
    
    String[] assignments = assignmentIds.split(DELIM_CHAR);
    approveAssignments(assignments);
  }

  private void approveAssignments(String[] assignments) {
    approveAssignments(assignments, null);
  }

  private void approveAssignments(String[] assignments, String[] comments) {
    
    // If we're not given anything, just no-op
    if (assignments == null) {
      return;
    }    
    
    checkIsUserCertain("You are about to approve " + assignments.length + " assignment(s).");
    String defaultComment = null;

    if (comments == null) {
      defaultComment = getComment();
    }

    // setup result map for callback handler
    resultMap = new HashMap<String, String>();
    for (int i=0; i<assignments.length; i++) {
      resultMap.put(assignments[i], (comments != null && comments[i] != null) ? comments[i] : defaultComment);
    }

    service.approveAssignments(assignments, comments, defaultComment, this);
  }

  public void approveHitsInFile(String fileName) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException("fileName must not be null");
    }
    
    String[] hits = super.getFieldValuesFromFile(fileName, HIT_TO_APPROVE_COLUMN);
    resultTemplate = RESULT_TEMPLATE_HIT;

    resultMap = new HashMap<String, String>();
    List<String> assignmentIds = new ArrayList<String>();
    for (int i=0; i<hits.length; i++) {
      try {
        Assignment[] assignments = service.getAllSubmittedAssignmentsForHIT(hits[i]);
        if (assignments != null && assignments.length > 0) {        
          for (Assignment a: assignments) {
            assignmentIds.add(a.getAssignmentId()); 
            resultMap.put(a.getAssignmentId(), a.getHITId());
            submitAssignmentsFromHitIfNecessary(assignmentIds, WorkQueue.getNumberOfThreads());   // batch up assignments 
          }        
        }    
      }
      catch (Exception ex) {
        log.error("ERROR submitting assignments for HIT "+hits[i]+": " + ex.getLocalizedMessage(), ex);
      }
    }
    submitAssignmentsFromHitIfNecessary(assignmentIds, 0);      // submit remaining assignments
  }  
  
  private void submitAssignmentsFromHitIfNecessary(List<String> assignmentList, int threshold) {
    if (assignmentList.size() < threshold) {
      return;
    }
    
    String[] assignmentsToApprove = new String[assignmentList.size()];
    assignmentList.toArray(assignmentsToApprove);
    service.approveAssignments(assignmentsToApprove, null, null, this);
    
    assignmentList.clear();
  }
  
  public void processItemResult(Object itemId, boolean succeeded,
      Object result, Exception itemException) {
    
    String resultVal = resultMap.get(itemId);
    if (succeeded) {
      successCount++;      
      log.info("[" + itemId + "] Assignment successfully approved" + 
          (resultVal != null && resultVal.length() > 0 ? String.format(resultTemplate, resultVal) : ""));
    }
    else {
      failedCount++;
      log.error("Error approving assignment " + itemId + 
          (resultVal != null && resultVal.length() > 0 ? String.format(resultTemplate, resultVal) : "") +
          ": " + itemException.getLocalizedMessage(), itemException);
    }    
  }  
}
