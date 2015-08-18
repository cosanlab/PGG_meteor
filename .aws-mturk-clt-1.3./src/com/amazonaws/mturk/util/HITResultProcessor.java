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


package com.amazonaws.mturk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import com.amazonaws.mturk.addon.AbstractHITDataOutput;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITResults;
/**
 * 
 * Proxy writer to process the results of HIT. 
 * 
 */
public class HITResultProcessor extends AbstractHITDataOutput {
  
  private static final String PREFIX_ANSWER = "Answer.";
  private static final String ORIG_ANSWER_FIELD = HITProperties.AssignmentField.Answers.getFieldName();
  private static final String QUESTION_DELIMITER="\t";
  
  private HITDataCSVWriter realWriter=null;
  private boolean outputAnswersInColumnFormat = true;
  private String answerPrefix = PREFIX_ANSWER;
  private String outputFilename = null;
  
  public HITResultProcessor(String filename, boolean outputAnswersInColumnFormat) throws IOException {
    outputFilename = filename;
    realWriter = new HITDataCSVWriter(filename, '\t', false);
    this.outputAnswersInColumnFormat = outputAnswersInColumnFormat;
  }
  
  public String getAnswerPrefix() {
    return answerPrefix;
  }

  public void setAnswerPrefix(String answerPrefix) {
    this.answerPrefix = answerPrefix;
  }
    
  /**
   * 
   * @param newFieldNames array of field headers of the results
   * Sequentially adds to fieldName set
   */
  public void setFieldNames( String[] newFieldNames ) {                            
    realWriter.setFieldNames(newFieldNames);
  }
  
  /** 
   * 
   * @param line - String array to be written to results
   * Fills the fieldValue map in the order appearing in the array
   * Do not use this. Call writeValues instead 
   * @deprecated
   */
  public void writeLine(String []line) throws IOException {
    realWriter.writeLine(line);
  }
  
  /**
   * Processes various field name and value pairs and saves in the list of field-value pairs 
   */
  public void writeValues(Map<String, String> values) throws IOException {
    processAnswers(values);
    realWriter.writeValues(values);
  }
  
  /**
   * @param values fieldValues map
   * removes the Answer column and adds a new column for every question
   */
  private void processAnswers(Map<String, String> values) {
    String answers = values.get(ORIG_ANSWER_FIELD);

    if (answers == null || HITResults.NO_ANSWER.equals(answers)) {
      return ;  // return if no answers
    }
    
    String[] answerList = answers.split("\t");
    if (outputAnswersInColumnFormat) {  
      values.remove(ORIG_ANSWER_FIELD);
      String answerColumn = null;
      for (int i = 0; i < answerList.length; i += 2) { //question and answers are tab delimited, so will alternate in the array
        answerColumn = getAnswerPrefix() + answerList[i];
        
        if (HITResults.EMPTY_ANSWER.equals(answerList[i+1])) {
          values.put(answerColumn, "");
        }
        else {
          values.put(answerColumn, answerList[i+1]); //add answer as value to question field
        }
      }
    }
    else {
      // write as "Q1=A1|A2...\tQ2=A2 ...."
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < answerList.length; i += 2) { //question and answers are tab delimited, so will alternate in the array
        sb.append(answerList[i]); 
        sb.append("=");
        
        if (!HITResults.EMPTY_ANSWER.equals(answerList[i+1])) {
          sb.append(answerList[i+1]);
        }
        
        sb.append(QUESTION_DELIMITER);
      }
      
      String val = sb.toString();
      if (val.endsWith(QUESTION_DELIMITER)) {
        val = val.substring(0, val.length()-QUESTION_DELIMITER.length());
      }
           
      values.put(ORIG_ANSWER_FIELD, val);
    }    
  }
  
  /**
   * Regenerates the results file with a correct header if the columns
   * had been dynamically changed after the header was written (e.g. for
   * external questions)
   */
  public synchronized void close() {
    realWriter.close();
    // check if headers were dynamically modified
    if (outputFilename != null && this.getFieldNamesSize() != this.realWriter.getFieldNamesSize()) {
      try { 
        // regenerate a new output file with correct headers
        String tmpFilename = outputFilename + ".tmp";
        File org = new File(outputFilename);
        File tmp = new File(tmpFilename);
        
        rewriteHeaders(outputFilename, tmpFilename);
        if (!org.delete()) {
          throw new IOException("Could not delete old result file - " + org + ".");
        }
        if (!tmp.renameTo(org)) {
          throw new Exception("Could not rename temporary result file to actual filename - " + tmp + " to " + org + ".");
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
        System.out.println("Failed to regenerate correct result headers. Please check the outputfile. Error was: "+ex.getMessage());
      }
    }      
  }
  
  /**
   * Writes the updated headers and content from source to destination.
   * 
   * @param source
   * @param destination
   * @throws IOException
   */
  private void rewriteHeaders(String source, String destination) throws IOException {
      BufferedReader reader = null;
      Writer writer = null;
      String newline = System.getProperty("line.separator");
      
      try {
        // write the correct header
        writeHeaders(destination);

        // write the rows from the old file into the new one (containing the complete header)
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"));
        writer = new OutputStreamWriter(new FileOutputStream(destination, true), "UTF-8");
        String row = null;
        String oldHeader = null;
        
        while ((row = reader.readLine()) != null) {
          if (oldHeader == null) {
            oldHeader = row;
          }
          else {
            writer.write(row);
            writer.write(newline);
          }
        }
      }
      finally {
        try {
          if (reader != null) {
            reader.close();
          }
        }
        finally {
          if (writer != null) {
            writer.close();
          }
        }
      }
  }
  
  /**
   * Writes the current headers to fileName.
   * 
   * @param fileName
   */
  private void writeHeaders(String fileName) {
    HITDataCSVWriter writer = new HITDataCSVWriter(fileName, '\t', false);
    try {
      writer.setFieldNames(realWriter.getFieldNames());
    }
    finally {
      writer.close();
    }
  }

}
