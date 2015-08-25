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
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;
import com.amazonaws.mturk.requester.SearchHITsResult;
import com.amazonaws.mturk.requester.SearchHITsSortProperty;
import com.amazonaws.mturk.requester.SortDirection;

public class ResetAccount extends AbstractCmd implements BatchItemCallback {

  private final String ARG_FORCE = "force"; 
  
  private int successCount = 0;
  private int failedCount = 0;
  private int unassignableCount = 0;
  private int runningCount = 0;
  private int totalNumberOfHits = 0;

  public ResetAccount() {}

  public static void main(String[] args) {
    ResetAccount dh = new ResetAccount();
    dh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_FORCE, false,
      "(optional) Do not prompt for confirmation (DANGEROUS)");
  }

  protected void printHelp() {
    formatter.printHelp(ResetAccount.class.getName(),
        opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    
    setForce(cmdLine.hasOption(ARG_FORCE));
    deleteAllHITs();
     
  }
  
  private String[] getHits() {
    HIT[] hits = null;
    List<String> ret = new ArrayList<String>();
    SearchHITsResult result = null;    
    String[] responseGroup = new String [] { "Minimal", "HITDetail" };
    int pageSize = 50;
    
    totalNumberOfHits = service.getTotalNumHITsInAccount();
    
    double numPagesDouble = Math.ceil(new Double(totalNumberOfHits) / new Double(pageSize));
    int numPages = (new Double(numPagesDouble)).intValue();

    for (int i = 1; i <= numPages; i = i + 1) {
      result = service.searchHITs(
          SortDirection.Ascending, 
          SearchHITsSortProperty.Enumeration,
          i, 
          pageSize, 
          responseGroup);
            
      hits = result.getHIT();
      if (hits != null) {
        for (HIT h: hits) {
          if (!h.getHITStatus().equals(HITStatus.Unassignable)) {
            ret.add(h.getHITId());
          }  
          else {
            unassignableCount++;
          }
        }
      }
    }

    return (String[]) ret.toArray(new String[] {}); 
  }
 
  public void deleteAllHITs() throws Exception {

    // Confirm, unless the FORCE option is applied
    checkIsUserCertain("PLEASE READ THIS CAREFULLY\n\nYou are about to delete ALL your HITs from Mechanical Turk.");
    
    totalNumberOfHits = service.getTotalNumHITsInAccount();

    if (totalNumberOfHits > 0) {
      log.info("--- Starting to reset account (" + totalNumberOfHits + " HITs) ---");
      String[] batch=getHits();
      if (batch.length > 0) {
        service.deleteHITs(batch, true, true, this);
      }
      
      // print summary
      log.info("--- Finished resetting account ---");
      log.info("  " + successCount + " HITs have been deleted or were deleted previously.");
      log.info("  " + failedCount + " HITs failed to delete.");
      log.info("  " + unassignableCount + " HITs could not be deleted because they are currently being worked on.");
      
      totalNumberOfHits = service.getTotalNumHITsInAccount();
      
      log.info("  " + totalNumberOfHits + " HITs are left in your account.");
    }
    else {
      log.info("There are no HITs in your account to delete.");
    }
  }
  
  public synchronized void processItemResult(Object itemId, boolean succeeded,
      Object result, Exception itemException) {

    runningCount++;
    if (succeeded) {
      successCount++; 
      log.info(String.format("[%s] %s (%d/%d)", itemId, result, runningCount, totalNumberOfHits));
    }
    else {
      failedCount++;
      log.error(String.format("[%s] FAILURE Deleting HIT: %s (%d/%d)", itemId, itemException.getLocalizedMessage(), runningCount, totalNumberOfHits), itemException);
    }    
  }
  
}
