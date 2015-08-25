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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.HITDataOutput;
import com.amazonaws.mturk.addon.HITDataWriter;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.requester.HIT;

public class UpdateHITs extends AbstractCmd {
  public int MAX_HITS_UNLIMITED = -1;

  private final static String ARG_SUCCESS = "success"; 
  private final static String ARG_PROPERTIES = "properties"; 

  public static void main(String[] args) {
    UpdateHITs lh = new UpdateHITs();
    lh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_SUCCESS, true,
    "(required) The success file to use (in comma-delimited format -- eg. helloworld.success).This should contain the 'hitid' column");
    opt.addOption(ARG_PROPERTIES, true,
    "(required) The updated properties file (that contains the new values) to use (in key:value form -- eg. helloworld.properties)");
  }

  protected void printHelp() {
    formatter.printHelp(UpdateHITs.class.getName()
        + " -" + ARG_SUCCESS + " [input_file]"
        + " -" + ARG_PROPERTIES + " [properties_file]", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_SUCCESS)) {

      log.error("Missing: -" + ARG_SUCCESS 
          + " [path to success file -- eg. c:\\mturk\\helloworld.success]");
      System.exit(-1);

    } else if (!cmdLine.hasOption(ARG_PROPERTIES)) {

      log.error("Missing: -" + ARG_PROPERTIES 
          + " [path to config file -- eg. c:\\mturk\\helloworld.properties]");
      System.exit(-1); 

    } 

    try {
      updateHITs(cmdLine.getOptionValue(ARG_SUCCESS),
          cmdLine.getOptionValue(ARG_PROPERTIES));
    } catch (Exception e) {
      log.error("Error loading HITs: " + e.getLocalizedMessage(), e);
      System.exit(-1);
    }
  }

  public HIT[] updateHITs(String successFile, String props) throws Exception {

    HITProperties hc = new HITProperties(props);

    // Output initializing message
    log.info("--[Initializing]----------");
    log.info(" Success File: " + successFile);
    log.info(" Properties: " + props);

    log.info("--[Updating HITs]----------");
    Date startTime = new Date();
    log.info("  Start time: " + startTime);

    String[] hitIds = super.getFieldValuesFromFile(successFile, "hitid");
    String[] hitTypeIds = super.getFieldValuesFromFile(successFile, "hittypeid");
    log.info("  Input: " + hitIds.length + " hitids");

    HITDataOutput success = new HITDataWriter(successFile + ".success");
    HITDataOutput failure = null;
    success.setFieldNames( new String[] {"hitid", "hittypeid"} );
    String newHITTypeId;
    try {
      newHITTypeId = service.registerHITType(hc.getAutoApprovalDelay(), 
          hc.getAssignmentDuration(), hc.getRewardAmount(), 
          hc.getTitle(), hc.getKeywords(), hc.getDescription(), 
          hc.getQualificationRequirements());
    } catch (Exception e) {
      log.error("Failed to register new HITType: " + e.getLocalizedMessage(), e);
      return new HIT[0];
    }
    log.info("  New HITTypeId: " + newHITTypeId);

    List<HIT> hits = new ArrayList<HIT>(hitIds.length);

    for (int i=0 ; i<hitIds.length ; i++) {
      try {
        HIT hit = service.getHIT(hitIds[i]);
        service.changeHITTypeOfHIT(hit.getHITId(), newHITTypeId);
        HashMap<String,String> good = new HashMap<String,String>();
        good.put( "hitid", hit.getHITId() );
        good.put( "hittypeid", newHITTypeId );
        success.writeValues( good );
        log.info("Updated HIT #" + i + " (" + hit.getHITId() + ") to new HITTypeId " + newHITTypeId);
        hits.add(hit);
      } catch (Exception e) {
        if (failure == null) {
          failure = new HITDataWriter(successFile + ".failure");
          failure.setFieldNames( new String[] {"hitid", "hittypeid"} );
        }
        HashMap<String,String> fail = new HashMap<String,String>();
        fail.put( "hitid", hitIds[i] );
        fail.put( "hittypeid", hitTypeIds[i] );
        failure.writeValues( fail );
        log.info("Failed to update HIT #" + i + "(" + hitIds[i] + ") to new HITTypeId " + newHITTypeId +" Error message:" + e.getLocalizedMessage());
      }
    }

    Date endTime = new Date();
    log.info("  End time: " + endTime);
    log.info("--[Done Updating HITs]----------");
    log.info(hitIds.length + " HITS were processed");
    log.info(hits.size() + " HITS were updated");
    if (hitIds.length != hits.size())
      log.info(hitIds.length - hits.size() + " HITS could not be updated");
    log.info("  Total load time: "
        + (endTime.getTime() - startTime.getTime()) / 1000 + " seconds.");

    return hits.toArray(new HIT[hits.size()]);
  }
}
