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

public class ExtendHITs extends AbstractCmd implements BatchItemCallback {

  private static final String ARG_SUCCESSFILE = "successfile"; 
  private static final String ARG_ASSIGNMENTS = "assignments"; 
  private static final String ARG_HOURS = "hours"; 
  
  String[] hitsToExtend = null;
  private int successCount = 0;
  private int failedCount = 0;
  private int runningCount = 0;

  public ExtendHITs() {}

  public static void main(String[] args) {
    ExtendHITs dh = new ExtendHITs();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_SUCCESSFILE, true,
      "(required) A path to the success file returned by the call to LoadHITs");
    opt.addOption(ARG_ASSIGNMENTS, true,
      "(optional) The number of assignments to add to the HITs");
    opt.addOption(ARG_HOURS, true,
      "(optional) The amount of time to extend the expiration date of the HITs, in hours");
  }

  protected void printHelp() {
    formatter.printHelp(ExtendHITs.class.getName() 
        + " -" + ARG_SUCCESSFILE + " [path to success file]"
        + " -" + ARG_ASSIGNMENTS + " [assignments to add]"
        + " -" + ARG_HOURS + " [time in hours to extend the expiration date]",
        opt);
  }
  
  private Integer parseInt(CommandLine cmdLine, String option) {
    Integer ret = null;
    if (cmdLine.hasOption(option)) {
      try {
        int num = Integer.parseInt(cmdLine.getOptionValue(option));

        if (num <= 0) {
          log.fatal("Invalid value for option -"+option+": Please specify a value greater than 0.");
          System.exit(-1);
        }
        ret = new Integer(num);
      }
      catch (NumberFormatException e) {
        log.fatal("Invalid value for option -"+option+": Please specify a valid number value.");
        System.exit(-1);
      }
    }
    
    return ret;
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_SUCCESSFILE)) {
      log.error("Missing: -" + ARG_SUCCESSFILE + " [path to success file]");
      System.exit(-1);
    } 
    
    if (!cmdLine.hasOption(ARG_ASSIGNMENTS) && !cmdLine.hasOption(ARG_HOURS)) {
      log.error("Missing parameters: Please specify an option for either -" + ARG_ASSIGNMENTS + " or -" +ARG_HOURS);
      System.exit(-1);
    } 
    
    Integer assignments = parseInt(cmdLine, ARG_ASSIGNMENTS);
    Integer hours = parseInt(cmdLine, ARG_HOURS);

    Long seconds = null;
    if (hours != null) {
        seconds = hours.longValue()*3600;
    }
    
    log.info("--- Starting to extend HITs ---");

    extendHITs(cmdLine.getOptionValue(ARG_SUCCESSFILE),  
        assignments,
        seconds);
    
    // print summary
    log.info("--- Finished to extend HITs ---");
    log.info(String.format("  %d HITs have been extended (added %d assignment(s), %d hour(s))", successCount,
            (assignments==null) ? 0 : assignments,
            (hours==null) ? 0 : hours));
    log.info(String.format("  %d HITs failed to be extended.", failedCount));    
  }

  public void extendHITs(String successFile, Integer assignments, Long seconds) throws Exception {

    hitsToExtend = super.getFieldValuesFromFile(successFile,
        HITProperties.HITField.HitId.getFieldName());
    
    if (hitsToExtend != null) {
      service.extendHITs(hitsToExtend, assignments, seconds, this);
    }    
  }
  
  public void processItemResult(Object itemId, boolean succeeded,
      Object result, Exception itemException) {
    runningCount++;
    if (succeeded) {
      successCount++; 
      log.info(String.format("[%s] Successfully extended HIT (%d/%d)", 
          itemId, runningCount, hitsToExtend.length));
    }
    else {
      failedCount++;
      log.error(String.format("[%s] Failed to extend HIT: %s (%d/%d)", 
          itemId, itemException.getLocalizedMessage(), runningCount, hitsToExtend.length), itemException);
    }    
  }  
}
