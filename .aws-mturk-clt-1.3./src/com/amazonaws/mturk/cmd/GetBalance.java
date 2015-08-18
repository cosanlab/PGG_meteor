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

import com.amazonaws.mturk.service.axis.RequesterService;


public class GetBalance extends AbstractCmd{
  
  public static void main(String[] args) {
    GetBalance gb = new GetBalance();
    gb.run(args);
  }

  protected void initOptions() {

  }
  
  protected void printHelp() {
    formatter.printHelp(GetBalance.class.getName(), opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    try {
      getBalance();
    }
    catch (Exception e) {
      log.error("An error occurred while fetching your balance: " + e.getLocalizedMessage(), e);
      System.exit(-1);
    }
  }
  
  public void getBalance() {
    double balance = service.getAccountBalance();
    log.info("Your account balance: $" + RequesterService.formatCurrency(balance));
  }
}
