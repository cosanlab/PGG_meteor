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

public class UpdateQualificationScore extends AbstractCmd {
    private final String ARG_QUALTYPE = "qualtypeid";
    private final String WORKER_ID = "workerid";
    private final String SCORE = "score";
    private final String INPUT_FILE = "input"; 
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        UpdateQualificationScore qualScore = new UpdateQualificationScore();
        qualScore.run(args);
    }
    
    protected void initOptions() {
      opt.addOption(ARG_QUALTYPE, true, "(required) Qualification Type Id whose score you want to update" );
      opt.addOption(WORKER_ID, true, "(required) WorkerId you want to update the score" );
      opt.addOption(SCORE, true, "(required) Score to be set for the Qualification" );
      opt.addOption(INPUT_FILE, true, "The input file to use (in comma-delimited format).This should contain the 'workerid' and 'score' columns");
    }
    
    protected void printHelp() {
      formatter.printHelp(UpdateQualificationScore.class.getName() +
          " -" + ARG_QUALTYPE + " [QualTypeId] " +
          " -" + WORKER_ID + " [WorkerId] " +
          " -" + SCORE + " [Score] ",opt);
    }

    protected void runCommand(CommandLine cmdLine) throws Exception {
      if (!cmdLine.hasOption(ARG_QUALTYPE)) {
        log.error("Missing: -" + ARG_QUALTYPE + " [QualTypeId]");
        System.exit(-1);
      } else if (!cmdLine.hasOption(INPUT_FILE)) {
        if (!cmdLine.hasOption(WORKER_ID) || !cmdLine.hasOption(SCORE)) {
          log.error("Either " + INPUT_FILE + " or " + WORKER_ID + " and " + SCORE + " should be passed");
          System.exit(-1);
        }
      }
      
      if (!cmdLine.hasOption(INPUT_FILE)) {
        updateQualificationScore(cmdLine.getOptionValue(ARG_QUALTYPE)
            ,cmdLine.getOptionValue(WORKER_ID)
            ,cmdLine.getOptionValue(SCORE));
      }
      else {
        updateQualificationScoreFromFile(cmdLine.getOptionValue(ARG_QUALTYPE), 
            cmdLine.getOptionValue(INPUT_FILE));
      }

    }
    
    private void updateQualificationScoreFromFile(String qualTypeId, String inputFilename) {
        try {
            String[] workerIds = super.getFieldValuesFromFile(inputFilename, WORKER_ID);
            String[] scores = super.getFieldValuesFromFile(inputFilename, SCORE);
            for (int i = 0; i < workerIds.length; i++) {
                updateQualificationScore(qualTypeId, workerIds[i], scores[i]);
            }
        }
        catch (IOException e) {
            log.error("Error reading from file " + inputFilename, e);
        }
        
    }

    private void updateQualificationScore(String qualTypeId, String workerId, String score) {
        try {
            service.updateQualificationScore(qualTypeId, workerId, Integer.parseInt(score));
            log.info("Successfully updated " + workerId + " score to " + score);
        }
        catch (NumberFormatException e) {
            log.error("Invalid score " + score);
        }
        catch (Exception e) {
            log.error("Error updating " + workerId + " score to " + score, e);
        }
    }

}
