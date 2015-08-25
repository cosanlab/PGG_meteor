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
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

public class AssignQualification extends AbstractCmd {
    
    private final String ARG_QUALTYPE = "qualtypeid";
    private final String ARG_WORKERID = "workerid";
    private final String ARG_INPUT_FILE = "input";
    private final String ARG_SCORE_FILE = "scorefile";
    private final String ARG_SCORE = "score";
    private final String ARG_DONOT_NOTIFY = "donotnotify";
    
    public static void main(String[] args) {
        AssignQualification aq = new AssignQualification();
        aq.run(args);
    }
    
    protected void initOptions() {
      opt.addOption(ARG_QUALTYPE, true, "Id of the Qualification Type to be assigned");
      opt.addOption(ARG_INPUT_FILE, true, "File containing the Qualification Type to be assigned");
      opt.addOption(ARG_WORKERID, true, "Id of the Worker to receive the Qualification");
      opt.addOption(ARG_SCORE_FILE, true, "File containing list of workers to be assigned the Qualification."
              + " (the columns must be titled " + ARG_WORKERID + " and " + ARG_SCORE + " )");
      opt.addOption(ARG_SCORE, true,    "(optional) Integer value for the Qualification");
      opt.addOption(ARG_DONOT_NOTIFY, false,  "(optional) Flag indicating not to send notification to worker." );
    }
    
    protected void printHelp() {
      log.info(AssignQualification.class.getName() + 
          " -" + ARG_QUALTYPE + " [qual to grant] " + 
          " -" + ARG_WORKERID + " [worker to be granted]" + 
          " -" + ARG_SCORE + " [value to grant]");
      log.info("Or");
      log.info(AssignQualification.class.getName() + 
          " -" + ARG_INPUT_FILE + " [ input file containing Qualification Type] " +
          " -" + ARG_SCORE_FILE + " [ score file ] " + 
          " -" + ARG_SCORE + " [value to grant]");

      PrintWriter pw = new PrintWriter(System.out);
      formatter.printOptions(pw, formatter.defaultWidth, opt, formatter.defaultLeftPad, formatter.defaultDescPad);
      pw.flush();
    }

    protected void runCommand(CommandLine cmdLine) throws Exception {
      if (!cmdLine.hasOption(ARG_QUALTYPE) &&
          !cmdLine.hasOption(ARG_INPUT_FILE)) {
        log.error("Either -" + ARG_QUALTYPE + " or -" + ARG_INPUT_FILE + " must be passed");
        System.exit(-1);

      }
      if (!cmdLine.hasOption(ARG_SCORE_FILE)) { 
        if (!cmdLine.hasOption(ARG_WORKERID)) {
          log.error("Either -" + ARG_WORKERID + " or -" + ARG_SCORE_FILE + " should be passed");
          System.exit(-1);
        } 
      }
      String[] qualIds = null;
      if (cmdLine.hasOption(ARG_QUALTYPE)) {
        qualIds = new String[1];
        qualIds[0] = cmdLine.getOptionValue( ARG_QUALTYPE );
      }
      else {
        qualIds = super.getFieldValuesFromFile(cmdLine.getOptionValue(ARG_INPUT_FILE), ARG_QUALTYPE);
        if (qualIds.length == 0) {
          log.error("qualtypeid must be present in the -" + ARG_INPUT_FILE + " file");
          System.exit(-1);
        }
      }
      
      String strValue = cmdLine.hasOption(ARG_SCORE) ? cmdLine.getOptionValue( ARG_SCORE ) : null;
      if (cmdLine.hasOption(ARG_SCORE_FILE)) {
        assignQualification(qualIds, cmdLine.getOptionValue(ARG_SCORE_FILE), cmdLine.hasOption(ARG_DONOT_NOTIFY), strValue);
      }
      else {
        String workerId = cmdLine.getOptionValue( ARG_WORKERID );
        Boolean notify = !cmdLine.hasOption( ARG_DONOT_NOTIFY );

        assignQualification( qualIds, workerId, strValue, notify );
      }
    }
    
    public void assignQualification(String[] qualIds, String workerFile, Boolean notify, String argScore) throws IOException {
        String[] workers = super.getFieldValuesFromFile(workerFile, ARG_WORKERID);
        String[] scores = super.getFieldValuesFromFile(workerFile, ARG_SCORE);
        for (int i = 0; i < workers.length; i++) {
            String workerScore = (scores.length < 1) ? argScore : scores[i];  
            assignQualification(qualIds, workers[i], workerScore, notify);
        }
    }
    
    public void assignQualification( String[] qualIds, String workerId, String score, Boolean notify ) {
        Integer value = null;
        if (score != null) {
            try {
                // Verify that strValue is an integer and not something else
                value = Integer.valueOf( score );
            } catch (NumberFormatException e ) {
                log.error( "Invalid format for -" + ARG_SCORE + " : " + score);
                System.exit(-1);
            }
        }
        for (String qualId : qualIds) {
            service.assignQualification( qualId, workerId, value, notify );
            String workerScore = value != null ? " with value " + value.toString() : " with default value"; 
            log.info( "Assigned qualification " + qualId + " to " + workerId + workerScore);
        }

    }
    
}
