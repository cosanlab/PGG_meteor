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

public class GrantBonus extends AbstractCmd {

  private final String ARG_WORKERID = "workerid";
  private final String ARG_ASSIGNMENT = "assignment";
  private final String ARG_AMOUNT = "amount"; 
  private final String ARG_REASON = "reason";

  public static void main(String[] args) {
    GrantBonus bw = new GrantBonus();
    bw.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_WORKERID, true,
      "(required) Id of the Worker to grant the bonus to");
    opt.addOption(ARG_REASON, true,
      "(required) Reason for the bonus");
    opt.addOption(ARG_AMOUNT, true,
      "(required) Bonus amount to grant");
    opt.addOption(ARG_ASSIGNMENT, true,
      "(required) The ID of the assignment this bonus payment is regarding");    
  }

  protected void printHelp() {
    formatter.printHelp(GrantBonus.class.getName() + 
        " -" + ARG_WORKERID + " [worker]" +
        " -" + ARG_AMOUNT + " [amount]" +
        " -" + ARG_ASSIGNMENT + " [assignment]" +
        " -" + ARG_REASON + " [reason]", opt);
  }

  protected void runCommand(CommandLine cmdLine) throws Exception {
    if (!cmdLine.hasOption(ARG_WORKERID)) {

      log.fatal("Missing: -" + ARG_WORKERID + " [worker to grant bonus to]");
      System.exit(-1);

    } else if (!cmdLine.hasOption(ARG_REASON)) {

      log.fatal("Missing: -" + ARG_REASON + " [reason for bonus]");
      System.exit(-1);

    }
    else if (!cmdLine.hasOption(ARG_ASSIGNMENT)) {

      log.fatal("Missing: -" + ARG_ASSIGNMENT + " [assignment this bonus is regarding]");
      System.exit(-1);

    }
    else if (!cmdLine.hasOption(ARG_AMOUNT)) {

      log.fatal("Missing: -" + ARG_AMOUNT + " [bonus amount]");
      System.exit(-1);

    }
    
    double amount = 0;
    try {
      amount = Double.parseDouble(cmdLine.getOptionValue(ARG_AMOUNT));
    }
    catch (NumberFormatException e) {
      log.fatal("Invalid bonus amount");
      System.exit(-1);
    }

    grantBonus( cmdLine.getOptionValue(ARG_WORKERID),
        cmdLine.getOptionValue(ARG_ASSIGNMENT),
        amount,
        cmdLine.getOptionValue(ARG_REASON));
  }

  public void grantBonus(String workerId, String assignmentId, double amount, String reason) {
    service.grantBonus(workerId, amount, assignmentId, reason);

    log.info("Granted bonus to " + workerId);
  }
}
