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

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amazonaws.mturk.addon.BatchItemCallback;
import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITResults;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.util.HITResultProcessor;

public class GetResults extends AbstractCmd implements BatchItemCallback {
  
  private final String ARG_SUCCESSFILE = "successfile"; 
  private final String ARG_OUTPUTFILE = "outputfile"; 
  private final String ARG_KVP = "namevaluepairs"; 
  private final String ARG_ANSWER_PREFIX = "answerPrefix"; 
  private static final int SECONDS = 1000;
  private static final int MINUTES = SECONDS * 60;
  private static final int HOURS = MINUTES * 60; 
  
  private HITResultProcessor resultProcessor = null;
  boolean outputAsNameValuePair = false;
  List<String> headerFields = null;
  private int resultsCount=0;
  private int totalAssignments=0;
  private int totalAssignmentsCompleted=0;
  private int rowCount = 0;
  private long totalWorkTimeMillis = 0;
  private Calendar firstHitCreateTime = Calendar.getInstance();
  private Calendar lastAssignmentSubmitTime = Calendar.getInstance();

  public GetResults() {
	  lastAssignmentSubmitTime.setTimeInMillis(0);
  }
  
  public static void main(String[] args) {
    GetResults lh = new GetResults();
    lh.run(args);
  }
  
  protected void initOptions() {
    opt.addOption(ARG_SUCCESSFILE, true,
        "(required) A path to the success file returned by the call to LoadHITs");
    opt.addOption(ARG_OUTPUTFILE, true,
        "(required) The file to which you'd like your results saved");
    opt.addOption(ARG_KVP, false,
        "(optional) Outputs the answer values as name-value-pairs instead of column-format"); 
    opt.addOption(ARG_ANSWER_PREFIX, true,
        "(optional) Answer prefix to use when outputting the answer in column-format (defaults to 'Answer.')");     
  }
  
  protected void printHelp() {
    formatter.printHelp(GetResults.class.getName() 
        + " -" + ARG_SUCCESSFILE + " [path to success file]"
        + " -" + ARG_OUTPUTFILE + " [path to output file]", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    
    if (!cmdLine.hasOption(ARG_SUCCESSFILE)) {
      log.fatal("Missing: -" + ARG_SUCCESSFILE + " [path to success file] ");
      System.exit(-1);
    } else if (!cmdLine.hasOption(ARG_OUTPUTFILE)) {
      log.fatal("Missing: -" + ARG_OUTPUTFILE + " [path to output file]");
      System.exit(-1);
    } 

    String outputFile = cmdLine.getOptionValue(ARG_OUTPUTFILE);
    outputAsNameValuePair = cmdLine.hasOption(ARG_KVP);
    getResults(cmdLine.getOptionValue(ARG_SUCCESSFILE), 
        outputFile,
        cmdLine.getOptionValue(ARG_ANSWER_PREFIX));    
  }
  
  public void getResults(String successFile, String outputFile) throws Exception {
    getResults(successFile, outputFile, null);
  }
  
  public void getResults(String successFile, String outputFile,
      String answerPrefix) throws Exception {
	HITDataInput success = new HITDataCSVReader(successFile); 
    rowCount = success.getNumRows()-1;
    
    resultProcessor = new HITResultProcessor(outputFile, !outputAsNameValuePair);
    if (answerPrefix != null) {
      resultProcessor.setAnswerPrefix(answerPrefix);
    }
        
    log.info("--[Retrieving Results]----------");
    
    service.getResults(success, this);
    
    log.info("--[Done Retrieving Results]----------");
    
    resultProcessor.close();
    if (totalAssignments ==0 ) {
      log.info("Could not retrieve work successfully");
    }
    else {
      log.info("\nResults have been written to file '" + outputFile + "'.\n");

      // The remaining log.infos are setup to align the colons (:)
      
      log.info(String.format("Assignments completed: %d/%d (%d%%)", totalAssignmentsCompleted, totalAssignments, totalAssignmentsCompleted*100/totalAssignments));
      
      if ( totalAssignmentsCompleted < totalAssignments ) {
        log.info(String.format("         Time elapsed: %s (h:mm:ss)", getInProgressTimeElapsed()));
      } else {
        log.info(String.format("         Time elapsed: %s (h:mm:ss)", getCompletedTimeElapsed()));
      }
      
      log.info(String.format("  Average submit time: %.1f seconds", getAverageSecsPerAssignment()));
    }
  }
  
  /**
   * Callback passed to the SDK which is invoked after a HIT result has been retrieved
   * or an error occurred
   */
  public synchronized void processItemResult(Object itemId, boolean succeeded, Object result, Exception itemException) {
    
    try {
      resultsCount++;
      if (succeeded) {
        HITResults r = (HITResults)result;
        
        if (headerFields == null) {
          resultProcessor.setFieldNames(getHeaderRow(r));
        }
                
        r.writeResults(resultProcessor);
        log.info(String.format("Retrieved HIT %d/%d, %s", resultsCount, rowCount, itemId));
        updateStatistics(r);
      }
      else {
        log.error(String.format("Error retrieving HIT results for HIT %d/%d (%s): %s", resultsCount, rowCount,
            itemId, itemException.getMessage()), itemException);
      }
    }
    catch (Exception ex) {
      log.error(String.format("Error processing HIT results for HIT %s: %s", itemId, ex.getMessage()), ex);
    }   
  }
  
  /**
   * Gets the HIT and assignment headers and adds additional rows
   * for each question identifier in a HIT in case the ARG_KVP
   * flag is not set
   */
  private String[] getHeaderRow(HITResults r) {
    if (headerFields == null) {
      headerFields = new ArrayList<String>();
      
      for (HITProperties.HITField field : HITProperties.HIT_FIELDS) {
        headerFields.add(field.getFieldName());
      }

      for (HITProperties.AssignmentField field : HITProperties.ASSIGNMENT_FIELDS) {
        headerFields.add(field.getFieldName());
      }                       
    }
    
    if (!outputAsNameValuePair) {
      headerFields.remove(HITProperties.AssignmentField.Answers.getFieldName());
      // parse question for first hit to write out the header based on the question IDs
      try {
        DOMParser p = new DOMParser();
        p.parse(new InputSource(new StringReader(r.getHIT().getQuestion())));
        Document doc = p.getDocument();
        NodeList nodes = doc.getElementsByTagName("QuestionIdentifier");
        for (int i=0; i<nodes.getLength(); i++) {
          headerFields.add(resultProcessor.getAnswerPrefix() + nodes.item(i).getFirstChild().getNodeValue());
        }
      } catch (Exception e) {
        log.error("Failed to parse HIT question to produce proper header information: "+e.getLocalizedMessage(), e);
      }                  
    }
    
    return headerFields.toArray(new String[] {});
  }
  
  /**
   * Updates the statistics for the result that are displayed after
   * all results have been retrieved
   */
  private void updateStatistics(HITResults r) {
    totalAssignments += r.getHIT().getMaxAssignments();
    
    if (r.getHIT().getCreationTime().before(firstHitCreateTime)) {
      firstHitCreateTime=r.getHIT().getCreationTime();
    }
    
    Assignment[] assignments = r.getAssignments();
    if (assignments != null) {

      for (Assignment a: assignments) {
    	AssignmentStatus status = a.getAssignmentStatus();
        Calendar acceptTime = a.getAcceptTime();
        Calendar submitTime = a.getSubmitTime();

        if (status == AssignmentStatus.Submitted
          || status == AssignmentStatus.Approved
          || status == AssignmentStatus.Rejected ) {
          totalAssignmentsCompleted++;
        }
        
        if ( submitTime != null && submitTime.after(lastAssignmentSubmitTime)) {
    	  lastAssignmentSubmitTime=submitTime;
        }
      
        if (acceptTime != null && submitTime != null) {
          totalWorkTimeMillis += (submitTime.getTimeInMillis()-acceptTime.getTimeInMillis());
        }

      }
    }
  }
  
  /**
   * Returns the time since the creation date of the first HIT (in format h:mm:ss)
   */
  private String getInProgressTimeElapsed() {
    long millis = System.currentTimeMillis() - firstHitCreateTime.getTimeInMillis();

    return millisToTimeElapsedString( millis );
  }

  /**
   * Returns the time between the creation of the first HIT and the submit of the last assignment (in format h:mm:ss)
   */
  private String getCompletedTimeElapsed() {
    long millis =  lastAssignmentSubmitTime.getTimeInMillis() - firstHitCreateTime.getTimeInMillis();
    
    return millisToTimeElapsedString( millis );
  }
  
  private String millisToTimeElapsedString( long millis ) {
    long hours = millis / HOURS;
    millis %= HOURS;
    long minutes = millis / MINUTES;
    millis %= MINUTES;
    long seconds = millis / SECONDS;
    
    return String.format("%d:%02d:%02d", hours, minutes, seconds);
  }
  
  /**
   * Returns the average work time per assignment completed in seconds
   * @return
   */
  private double getAverageSecsPerAssignment() {
    return this.totalWorkTimeMillis==0 || this.totalAssignmentsCompleted==0 ? (double) 0.0 
        : (double) (this.totalWorkTimeMillis / this.totalAssignmentsCompleted) / (double) 1000.0;
  }
  
}
