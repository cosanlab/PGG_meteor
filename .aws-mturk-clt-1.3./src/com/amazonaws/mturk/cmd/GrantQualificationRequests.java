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
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;

public class GrantQualificationRequests extends AbstractCmd {
  
  private final static String DELIM_CHAR = ",";
  private final static String QUAL_REQUEST_TO_APPROVE_COLUMN = "qualificationRequestToApprove";
  private final static String QUAL_REQUEST_TO_APPROVE_VALUE_COLUMN = "qualificationRequestToApproveValue";
  
  private final String ARG_QUALREQ = "qualRequest";
  private final String ARG_SCORE = "score";
  private final String ARG_APPROVEFILE = "approvefile";
  private final String ARG_FORCE = "force";
  
  public GrantQualificationRequests () {}
  
  public static void main(String[] args) {
    GrantQualificationRequests jtf = new GrantQualificationRequests();
    jtf.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_QUALREQ, true,
        "The ID of the qualification request to approve (separate multiple qualification request IDs with a comma)");
    opt.addOption(ARG_SCORE, true,
        "The score you wish to associate with the granted qualification");
    opt.addOption(ARG_APPROVEFILE, true,
        "The name of the file that contains the qualification request IDs to be approved (the column must be titled "
        + QUAL_REQUEST_TO_APPROVE_COLUMN + " and " + QUAL_REQUEST_TO_APPROVE_VALUE_COLUMN + ")");
    opt.addOption(ARG_FORCE, false,
        "(optional) Do not prompt for confirmation (DANGEROUS)");
  }
  
  protected void printHelp() {
    formatter.printHelp(GrantQualificationRequests.class.getName() 
        + " -" + ARG_QUALREQ + " [-" + ARG_SCORE + "] | "
        + " -" + ARG_APPROVEFILE + " [path to approval file]}", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_QUALREQ) && !cmdLine.hasOption(ARG_APPROVEFILE)) {

      log.error("Missing: you must supply one of -" + ARG_QUALREQ 
          + " or -" + ARG_APPROVEFILE);
      System.exit(-1);

    }     
    
    Integer score = null;    
    if (cmdLine.hasOption(ARG_SCORE)) { score = Integer.valueOf( cmdLine.getOptionValue( ARG_SCORE ) ); }

    setForce(cmdLine.hasOption(ARG_FORCE));
    if (cmdLine.hasOption(ARG_QUALREQ)) {
      grantQualRequests(cmdLine.getOptionValue(ARG_QUALREQ),score);
    }

    if (cmdLine.hasOption(ARG_APPROVEFILE)) {
      grantQualRequestsInFile(cmdLine.getOptionValue(ARG_APPROVEFILE),score);
    }
  }

  public void grantQualRequestsInFile(String fileName, Integer defaultValue) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException("fileName must not be null");
    }
    
    String[] qualReqs = super.getFieldValuesFromFile(fileName,
        QUAL_REQUEST_TO_APPROVE_COLUMN);
    
    String[] values = super.getFieldValuesFromFile(fileName,
        QUAL_REQUEST_TO_APPROVE_VALUE_COLUMN);
    
    Integer[] int_values = null;
    if (values.length > 0) { 
      int_values = new Integer[values.length];
      for( int i = 0; i < values.length ; i++) {
        
        try
        {
              int_values[i] = Integer.valueOf( values[i] );
        }
        catch( NumberFormatException e )
        {
              int_values[i] = defaultValue;
        }
      }
    }
    else {
      int_values = new Integer[qualReqs.length];
      Arrays.fill(int_values, defaultValue);
    }
    grantQualRequests(qualReqs, int_values);
  }

  public void grantQualRequests(String qualReqIds, Integer value) {
    if (qualReqIds == null) {
      return;
    }
    
    String[] assignments = qualReqIds.split(DELIM_CHAR);
    grantQualRequests(assignments,value);
  }

  private void grantQualRequests(String[] qualReqs, Integer value) {
    Integer[] values = new Integer[qualReqs.length];
    for( int i = 0 ; i < values.length ; i++ ) { values[i] = value; }
    grantQualRequests(qualReqs, values);
  }

  private void grantQualRequests(String[] qualReqs, Integer[] values) {
    
    // If we're not given anything, just no-op
    if (qualReqs == null) {
      return;
    }
    
    checkIsUserCertain("You are about to grant " + qualReqs.length + " qual request(s).");
    Integer value = null;

    for (int i = 0; qualReqs != null && i < qualReqs.length; i++) {
      try {
        if (values != null)
          value = values[i];

        service.grantQualification( qualReqs[i], value );
        log.info("[" + qualReqs[i]
                                + "] QualRequest successfully approved "
                                + (value != null ? " with value (" + value + ")" : ""));

      } catch (Exception e) {
        log.error("Error granting qual request " + qualReqs[i]
                                                            + " with value (" + value + "): " + e.getLocalizedMessage(), e);
      }
    }

  }

}
