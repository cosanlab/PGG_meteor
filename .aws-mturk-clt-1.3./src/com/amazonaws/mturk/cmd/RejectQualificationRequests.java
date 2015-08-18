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

public class RejectQualificationRequests extends AbstractCmd {
  
  private final static String DELIM_CHAR = ",";
  private final static String QUALREQ_TO_REJECT_COLUMN = "qualificationRequestToReject";
  private final static String QUALREQ_TO_REJECT_COMMENT_COLUMN = "qualificationRequestToRejectComment";
  
  private final String ARG_QUALREQ = "qualRequest";
  private final String ARG_REJECTFILE = "rejectfile";
  private final String ARG_FORCE = "force";
  
  public RejectQualificationRequests () {}
  
  public static void main(String[] args) {
    RejectQualificationRequests jtf = new RejectQualificationRequests();
    jtf.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_QUALREQ, true,
        "The ID of the qualification request to reject (separate multiple qualification request IDs with a comma)");
    opt.addOption(ARG_REJECTFILE, true,
        "The name of the file that contains the qualification request IDs to be rejected (the columns must be titled "
        + QUALREQ_TO_REJECT_COLUMN + " and " + QUALREQ_TO_REJECT_COMMENT_COLUMN +")");
    opt.addOption(ARG_FORCE, false,
        "(optional) Do not prompt for confirmation (DANGEROUS)");
  }
  
  protected void printHelp() {
    formatter.printHelp(RejectQualificationRequests.class.getName() 
        + " -" + ARG_QUALREQ + " | "
        + " -" + ARG_REJECTFILE + " [path to rejection file]}", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_QUALREQ) && !cmdLine.hasOption(ARG_REJECTFILE)) {

      log.error("Missing: you must supply one of -" + ARG_QUALREQ + " or -" + ARG_REJECTFILE + ".");
      System.exit(-1);

    } 
    
    setForce(cmdLine.hasOption(ARG_FORCE));
    if (cmdLine.hasOption(ARG_QUALREQ)) {
      rejectQualRequests(cmdLine.getOptionValue(ARG_QUALREQ));
    }

    if (cmdLine.hasOption(ARG_REJECTFILE)) {
      rejectQualRequestsInFile(cmdLine.getOptionValue(ARG_REJECTFILE));
    }
  }

  public void rejectQualRequestsInFile(String fileName) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException();
    }
    
    String[] qualReqs = super.getFieldValuesFromFile(fileName,
        QUALREQ_TO_REJECT_COLUMN);
    
    String[] comments = super.getFieldValuesFromFile(fileName,
        QUALREQ_TO_REJECT_COMMENT_COLUMN);
    
    rejectQualRequests(qualReqs, comments);
  }

  public void rejectQualRequests(String qualReqIds) {
    if (qualReqIds == null) {
      throw new IllegalArgumentException();
    }
    
    String[] qualReqs = qualReqIds.split(DELIM_CHAR);
    rejectQualRequests(qualReqs);
  }

  private void rejectQualRequests(String[] qualReqs) {
    rejectQualRequests(qualReqs, null);
  }

  private void rejectQualRequests(String[] qualReqs, String[] comments) {
    
    // If we're not given anything, just no-op
    if (qualReqs == null) {
      return;
    }

    checkIsUserCertain("You are about to reject " + qualReqs.length + " qualification request(s). Are you sure?");
    String comment = null;

    if (comments == null) {
      comment = getComment();
    }

    for (int i = 0; qualReqs != null && i < qualReqs.length; i++) {
      try {
        if (comments != null)
          comment = comments[i];

        service.rejectQualificationRequest( qualReqs[i], comment );
        log.info("[" + qualReqs[i]
                                + "] Qualification request successfully rejected "
                                + (comment != null ? " with comment (" + comment + ")" : ""));

      } catch (Exception e) {
        log.error("Error rejecting qualification request " + qualReqs[i]
                                                                      + " with comment [" + comment + "]: " + e.getLocalizedMessage(), e);
      }
    }    
  }

}
