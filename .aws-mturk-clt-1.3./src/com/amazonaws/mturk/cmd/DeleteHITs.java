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

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.addon.HITProperties;

public class DeleteHITs extends AbstractCmd implements BatchItemCallback {

  private final String ARG_SUCCESSFILE = "successfile"; 
  private final String ARG_FORCE = "force"; 
  private final String ARG_APPROVE = "approve"; 
  private final String ARG_EXPIRE = "expire"; 
  
  String[] hitsToDelete = null;
  private int successCount = 0;
  private int failedCount = 0;
  private int runningCount = 0;

  public DeleteHITs() {}

  public static void main(String[] args) {
    DeleteHITs dh = new DeleteHITs();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_SUCCESSFILE, true,
    "(required) A path to the success file returned by the call to LoadHITs");
    opt.addOption(ARG_FORCE, false,
    "(optional) Do not prompt for confirmation (DANGEROUS)");
    opt.addOption(ARG_APPROVE, false,
    "(optional) If answers have been submitted for the HIT, this approves them (HITs cannot be deleted until answers are approved)");
    opt.addOption(ARG_EXPIRE, false,
    "(optional) If the HIT is still live, this expires it (Live HITs cannot be deleted)");
  }

  protected void printHelp() {
    formatter.printHelp(DeleteHITs.class.getName() + " -" + ARG_SUCCESSFILE + " [path to success file]",
        opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_SUCCESSFILE)) {

      log.error("Missing: -" + ARG_SUCCESSFILE + " [path to success file]");
      System.exit(-1);

    } 

    log.info("--- Starting to delete HITs ---");
    setForce(cmdLine.hasOption(ARG_FORCE));
    deleteHITs(cmdLine.getOptionValue(ARG_SUCCESSFILE),  
        cmdLine.hasOption(ARG_APPROVE), 
        cmdLine.hasOption(ARG_EXPIRE));
    
    // print summary
    log.info("--- Finished to delete HITs ---");
    log.info("  " + successCount + " HITs have been deleted or were deleted previously.");
    log.info("  " + failedCount + " errors occured.");    
    if (failedCount > 0) {
        System.exit(-1);
    }
  }

  public void deleteHITs(String successFile, boolean approve,
      boolean expire) throws Exception {

    hitsToDelete = super.getFieldValuesFromFile(successFile,
        HITProperties.HITField.HitId.getFieldName());
    
    if (hitsToDelete != null) {

      // Confirm, unless the FORCE option is applied
      checkIsUserCertain("You are about to delete " + hitsToDelete.length + " HITs.");
      
      service.deleteHITs(hitsToDelete, approve, expire, this); // Do the deletion
    }
  }
  
  public synchronized void processItemResult(Object itemId, boolean succeeded,
      Object result, Exception itemException) {
    runningCount++;
    if (succeeded) {
      successCount++; 
      log.info(String.format("[%s] %s (%d/%d)", 
          itemId, result, runningCount, hitsToDelete.length));
    }
    else {
      failedCount++;
      log.error(String.format("[%s] FAILURE Deleting HIT: %s (%d/%d)", 
          itemId, itemException.getLocalizedMessage(), runningCount, hitsToDelete.length), itemException);
    }    
  }
  
}
