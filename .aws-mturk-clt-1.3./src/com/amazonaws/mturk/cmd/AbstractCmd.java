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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITQuestion;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.ClientConfig;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import org.apache.log4j.Logger;

import com.amazonaws.mturk.util.CLTExceptionFilter;

public abstract class AbstractCmd {
  
  private final String ARG_HELP = "help";
  private final String ARG_HELP_SHORT = "h"; 
  private final String ARG_SANDBOX = "sandbox";
  
  protected static ClientConfig config;
  protected static RequesterService service;
  protected static Logger log = Logger.getLogger(AbstractCmd.class);
  protected static HelpFormatter formatter;
  
  protected Options opt;
  
  protected boolean force = false;
  
  public AbstractCmd() {
    if (config == null) {
      try {
        config = new PropertiesClientConfig();
      }
      catch (IllegalStateException ex) {
        log.error("The Mechanical Turk command line tool is not configured correctly: "+ex.getLocalizedMessage());
        log.error("Please open the configuration file (mturk.properties) and correct this setting before proceeding.");
        System.exit(-1);
      }
    }     
    
    if (formatter == null) {
      formatter = new HelpFormatter();
    }
    
    opt = new Options();
    initOptions();
    opt.addOption(ARG_HELP, false, "Print help for this application");
    opt.addOption(ARG_HELP_SHORT, false, "Print help for this application");
    opt.addOption(ARG_SANDBOX, false, "Run the command in the Mechanical Turk Sandbox (used for testing purposes)");
  }
  
  /**
   * Sets the available options for a specific command
   */
  protected abstract void initOptions();
  
  /**
   * Runs the command with the parameters passed by the command line 
   */
  protected abstract void runCommand(CommandLine cmdLine) throws Exception;
  
  /**
   * Prints the help instructions for the command
   */
  protected abstract void printHelp();
  
  /**
   * Configures the command to run against the Mechanical Turk sandbox (for testing purposes)
   */
  public void setSandBoxMode() {
    config.setServiceURL(ClientConfig.SANDBOX_SERVICE_URL);
    initService();
  }
  
  protected void initService() {
    if (service == null) {
      
      if (ClientConfig.SANDBOX_SERVICE_URL.equalsIgnoreCase(config.getServiceURL())) {
        // configure sandbox/throttling friendly settings 
        System.setProperty("mturk.java.workqueue.threads", "2");   
        if (config.getRetryAttempts() < 6) {
          config.setRetryAttempts(6);
        }
        
        if (config.getRetryDelayMillis() < 500) {
          config.setRetryDelayMillis(500);
        }
      }
      
      service = new RequesterService(config);
      service.appendApplicationSignature("MTurkJavaCLT/1.2.1");
      
      service.addFilter(new CLTExceptionFilter(config));
    }  
  }
  
  public void run(String[] args) {
    try {  
      BasicParser parser = new BasicParser();
      CommandLine cmdLine = parser.parse(opt, args);
      
      if (cmdLine.hasOption(ARG_HELP) || cmdLine.hasOption(ARG_HELP_SHORT)) {
        printHelp();
        System.exit(-1);        
      }
      
      if (cmdLine.hasOption(ARG_SANDBOX) &&
          !ClientConfig.SANDBOX_SERVICE_URL.equalsIgnoreCase(config.getServiceURL())) {
        log.info("Sandbox override");
        
        setSandBoxMode();
      }   
      else {
        initService();
      }
      
      log.debug(String.format("Running command against %s", config.getServiceURL()));
      
      runCommand(cmdLine);
    } catch (Exception e) {
      log.error("An error occurred: " + e.getLocalizedMessage(), e);
      System.exit(-1);
    }
  }
  
  public void setForce(boolean force) {
    this.force = force;
  }

  protected String[] getFieldValuesFromFile(String fileName, String fieldName) throws IOException {
    
    HITDataCSVReader csvReader = new HITDataCSVReader(fileName);    
    
    // check that we have a header 
    String[] fieldNames = csvReader.getFieldNames();
    if (fieldNames == null) {
      log.error("Your file (" + fileName + ") seems to be incorrect. "
              + "The file expects at least 1 row containing field names separate by tabs");

      return null;      
    }    
    
    // Check that the header has a matching field
    int fieldNumber = -1;
    for (int i = 0; i < fieldNames.length; i++) {
      if (fieldName.equals(fieldNames[i])) {
        fieldNumber = i;
        break;
      }
    }

    if (fieldNumber == -1) {      
      log.error("Your file (" + fileName + ") seems to be incorrect. "
              + "The field name '" + fieldName
              + "' is needed but wasn't found");
      
      return null;
    }    
    
    // get the values
    List<String> values = new ArrayList<String>();
    
    Map<String, String> row = null;
    for (int i=1; i<csvReader.getNumRows(); i++) {
      try {
        row = csvReader.getRowAsMap(i);
        if (row.containsKey(fieldName)) {
          values.add(row.get(fieldName));
        }
      }
      catch (java.lang.ArrayIndexOutOfBoundsException e) {
        throw new IllegalStateException(String.format("The format of file '%s' is invalid. An error was found in value row %d. Please correct the error, then rerun the command again.", fileName, i), e);
      }
    }

    return values.toArray(new String[values.size()]);
  }

  protected static Properties loadProperties(String fileName)
      throws IOException {
    
    // Read properties file.
    Properties props = new Properties();
    props.load(new java.io.FileInputStream(new java.io.File(fileName)));
    return props;
    
  }
  
  protected void checkIsUserCertain(String msg) {
    
    if (force) {
      return;
    }
    
    log.info(msg);
    log.info("To confirm this operation, please press ENTER (or press Ctrl+C to abort): ");

    try {
      new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (IOException e) {
      // Do nothing
    }
  }
  
  protected String getComment() {
    if (force) {
      return null;
    }
    
    String comment = null;
    log.info("If you would like to supply a comment to the worker(s), "
        + "please type it below then press ENTER. If not, just hit ENTER:");
      
    try {
        
      BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
      comment = bReader.readLine();
        
    } catch (IOException e) {
      // Do nothing
    }

    return comment;
  }

  protected String getMergedTemplate(String file) throws Exception {
    if (file==null || file.length()==0) {
      return null;
    }
    
    HITQuestion questionTpl = new HITQuestion(file);
    
    return questionTpl.getQuestion();
  }
}
