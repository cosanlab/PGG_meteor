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

public class UnblockWorker extends AbstractCmd {

    private final String ARG_WORKERID = "workerid";
    private final String ARG_REASON = "reason";
    
    public static void main(String[] args) {
        UnblockWorker bw = new UnblockWorker();
        bw.run(args);
    }
    
    protected void initOptions() {
      opt.addOption(ARG_WORKERID, true,
          "(required) Id of the Worker to unblock");
      opt.addOption(ARG_REASON, true,
          "(required) Reason you are unblocking the worker");
    }
    
    protected void printHelp() {
      formatter.printHelp(UnblockWorker.class.getName() + 
          " -" + ARG_WORKERID + " [worker to unblock]" +
          " -" + ARG_REASON + " [reason for unblocking]", opt);
    }

    protected void runCommand(CommandLine cmdLine) throws Exception {

      if (!cmdLine.hasOption(ARG_WORKERID)) {

        log.fatal("Missing: -" + ARG_WORKERID + " [worker to unblock]");
        System.exit(-1);

      } else if (!cmdLine.hasOption(ARG_REASON)) {

        log.fatal("Missing: -" + ARG_REASON + " [reason for unblocking]");
        System.exit(-1);

      }

      String workerId = cmdLine.getOptionValue( ARG_WORKERID );
      String reason = cmdLine.getOptionValue( ARG_REASON );

      unblockWorker(workerId, reason);
    }
    
    public void unblockWorker(String workerId, String reason) {
        service.unblockWorker( workerId, reason );
        log.info( "Unblocked " + workerId + " with reason: " + reason);
    }
}
