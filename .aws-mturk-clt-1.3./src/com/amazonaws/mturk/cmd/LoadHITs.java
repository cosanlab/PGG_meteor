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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITDataOutput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITQuestion;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.exception.ObjectDoesNotExistException;

public class LoadHITs extends AbstractCmd {
  public int MAX_HITS_UNLIMITED = -1;

  private final String ARG_INPUT = "input"; 
  private final String ARG_QUESTION = "question";
  private final String ARG_PROPERTIES = "properties"; 
  private final String ARG_PREVIEW = "preview"; 
  private final String ARG_PREVIEW_FILE = "previewfile"; 
  private final String ARG_MAXHITS = "maxhits";
  private final String ARG_LABEL = "label"; 

  private final String DEFAULT_PREVIEW_FILE = "preview.html";

  public LoadHITs() {
    super();
  }

  public static void main(String[] args) {
    LoadHITs lh = new LoadHITs();
    lh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_INPUT, true,
        "(required) The input file to use (in comma-delimited format, ie. helloworld.input)");
    opt.addOption(ARG_QUESTION, true,
        "(required) The question file to use (ie. helloworld.question)");
    opt.addOption(ARG_PROPERTIES, true,
        "(required) The properties file to use (in key:value form -- ie. helloworld.properties)");
    opt.addOption(ARG_PREVIEW, false,
        "(optional) Stores a preview of the HIT that will be loaded in preview.html.");
    opt.addOption(ARG_PREVIEW_FILE, true,
        "(optional) The path and filename to save your preview file (ie. preview.html)");
    opt.addOption(ARG_MAXHITS, true,
        "(optional) The maximum number of HITs to produce (used for testing purposes)");
    opt.addOption(ARG_LABEL, true,
        "(optional) The label to use for the output files (success and failure)");    
  }
  
  protected void printHelp() {
    formatter.printHelp(LoadHITs.class.getName()
        + " -" + ARG_INPUT + " [input_file]"
        + " -" + ARG_QUESTION + " [question_file]"
        + " -" + ARG_PROPERTIES + " [properties_file]", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_INPUT)) {

      log.error("Missing: -" + ARG_INPUT 
          + " [path to input file -- ie. c:\\mturk\\helloworld.input]");
      System.exit(-1);

    } else if (!cmdLine.hasOption(ARG_QUESTION)) {

      log.error("Missing: -" + ARG_QUESTION 
          + " [question file name only -- no path]");
      System.exit(-1);

    } else if (!cmdLine.hasOption(ARG_PROPERTIES)) {

      log.error("Missing: -" + ARG_PROPERTIES 
          + " [path to config file -- ie. c:\\mturk\\helloworld.properties]");
      System.exit(-1); 

    } else {

      int maxHITs = MAX_HITS_UNLIMITED;

      if (cmdLine.hasOption(ARG_MAXHITS)) {

        String maxHITsValue = cmdLine.getOptionValue(ARG_MAXHITS);

        try {

          maxHITs = Integer.parseInt(maxHITsValue);

        } catch (NumberFormatException e) {
          log.error("Invalid value provided for maxhits: " + maxHITsValue);
          System.exit(-1);
        }
      }

      try {
        loadHITs(cmdLine.getOptionValue(ARG_INPUT), 
            cmdLine.getOptionValue(ARG_QUESTION), cmdLine.getOptionValue(ARG_PROPERTIES), 
            cmdLine.getOptionValue(ARG_PREVIEW_FILE),
            maxHITs, cmdLine.hasOption(ARG_PREVIEW),
            cmdLine.getOptionValue(ARG_LABEL),
            false);
      }
      catch (ObjectDoesNotExistException objEx) {
        log.error(String.format("The qualification for the HITs does not exist. Please review the qualification settings in '%s' before retrying. ", 
              cmdLine.getOptionValue(ARG_PROPERTIES)));
        System.exit(-1);
      }
      catch (Exception e) {
        log.error("Error loading HITs: " + e.getLocalizedMessage(), e);
        System.exit(-1);
      }
    }
  }
  
  public HIT[] loadHITs(String input, String question, String props, 
      String previewFile, int maxHITs, boolean preview) throws Exception {
    return loadHITs(input, question, props, previewFile, maxHITs, preview, null, false);
  }
  
  /**
   * Calculates an approximate price for the batch including fees and
   * displays a warning if there are not enough funds in the account
   */
  private void checkFunds(double reward, int numHits, int numAssignments) {
    double approximatePrice = reward*numHits*numAssignments;
    double balance = service.getAccountBalance();
    
    double feeBase = reward/10;
    if (feeBase < .005) {
      feeBase = .005;
    }
    double feeMinimum = numAssignments*numHits*feeBase;
    double feeActual = approximatePrice*0.1;
    
    if (feeActual < feeMinimum) {
      feeActual = feeMinimum;
    }
    
    approximatePrice += feeActual;
    
    if (balance < approximatePrice) {
      log.info("\nWARNING: It appears you do not have enough funds in your account");
      log.info(String.format("The HITs you are trying to create will create a total liability of $%.3f", approximatePrice));
      log.info(String.format("You current account balance is $%.3f", balance));
      
      log.info("\nTo continue this operation, please press ENTER (or press Ctrl+C to abort and fund your account first): ");

      try {
        new BufferedReader(new InputStreamReader(System.in)).readLine();
      } catch (IOException e) {
        // Do nothing
      }
    }    
  }

  public HIT[] loadHITs(String input, String question, String props, 
      String previewFile, int maxHITs, boolean preview, String outputFile, boolean append) throws Exception {

    HITDataInput hi = new HITDataCSVReader(input);
    HITProperties hc = new HITProperties(props);
    HITQuestion hq = new HITQuestion(question);    
    
    // set the base name for success and failure file
    if (outputFile==null) {
      outputFile = input;
    }

    // Output initializing message
    log.info("--[Initializing]----------");
    log.info(" Input: " + input);
    log.info(" Properties: " + props);
    log.info(" Question File: " + question);

    if (maxHITs != MAX_HITS_UNLIMITED) {
      log.info(" Limiting HITs created to: " + maxHITs);
    }

    log.info(" Preview mode " + (preview ? "enabled" : "disabled"));

    if (preview) {
      if (previewFile == null) {
        previewFile = DEFAULT_PREVIEW_FILE;
      }
      log.info("Preview file: " + previewFile);
    }

    HIT[] hits = null;
    if (preview) {

      log.info("--[Previewing HITs]----------");
      if (previewFile != null)
        service.previewHIT(previewFile, hi, hc, hq);
      else
        log.info(service.previewHIT(hi, hc, hq));

    } else {
      
      if (hi.getNumRows() < maxHITs) {
        maxHITs = hi.getNumRows();
      }
      
      checkFunds(hc.getRewardAmount(), 
          maxHITs==MAX_HITS_UNLIMITED ? hi.getNumRows()-1 : maxHITs, hc.getMaxAssignments());

      log.info("--[Loading HITs]----------");
      Date startTime = new Date();
      log.info("  Start time: " + startTime);
      
      HITDataOutput successWriter = new HITDataCSVWriter(outputFile + ".success", '\t', append, false);
      HITDataOutput failureWriter = new HITDataCSVWriter(outputFile + ".failure", '\t', append, false);
        
      hits = service.createHITs(hi, hc, hq, maxHITs, successWriter, failureWriter);

      Date endTime = new Date();
      log.info("  End time: " + endTime);
      log.info("--[Done Loading HITs]----------");
      log.info("  Total load time: "
          + (endTime.getTime() - startTime.getTime()) / 1000 + " seconds.");
      log.info("  Successfully loaded "
          + hits.length + " HITs.");
      
      int failed = 0;
      if (maxHITs != MAX_HITS_UNLIMITED) {
        failed = maxHITs - hits.length;
      }
      else {
        failed = hi.getNumRows() - hits.length - 1;
      }
      if (failed > 0) {
        log.error("  Failed to load " + failed + " HITs."); 
      }
      else {
        // delete failure file from previous run if it exists
        String failureFilename = outputFile + ".failure";
        File file = new File(failureFilename);
        if (file.exists()) {
          try {
            file.delete();
          }
          catch (Exception ex) {
            log.info("Unable to delete failure file from previous run :" + failureFilename);
          }
        }
      }
    }

    return hits;
  }
}
