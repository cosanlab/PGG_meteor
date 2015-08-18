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

public class RevokeQualification extends AbstractCmd {

    private final String ARG_QUALTYPE = "qualtypeid";
    private final String ARG_WORKERID = "workerid";
    private final String ARG_REASON = "reason";
    
    public static void main(String[] args) {
        RevokeQualification rq = new RevokeQualification();
        rq.run(args);
    }
    
    protected void initOptions() {
      opt.addOption(ARG_QUALTYPE, true, "(required) Id of the Qualification Type to be revoked");
      opt.addOption(ARG_WORKERID, true, "(required) Id of the Worker whose qualification is to be revoked");
      opt.addOption(ARG_REASON, true, "(optional) Reason you are revoking the qualification");
    }
    
    protected void printHelp() {
      formatter.printHelp(RevokeQualification.class.getName() + 
          " -" + ARG_WORKERID + " [worker to be revoked]" +
          " -" + ARG_QUALTYPE + " [qual to revoke]" +
          " -" + ARG_REASON + " [reason for revocation]", opt);
    }

    protected void runCommand(CommandLine cmdLine) throws Exception {
      if (!cmdLine.hasOption(ARG_WORKERID)) {

        log.error("Missing: -" + ARG_WORKERID + " [worker to be revoked]");
        System.exit(-1);

      } else if (!cmdLine.hasOption(ARG_QUALTYPE)) {

        log.error("Missing: -" + ARG_QUALTYPE + " [qual to revoke]");
        System.exit(-1);

      }

      String qualId = cmdLine.getOptionValue( ARG_QUALTYPE );
      String workerId = cmdLine.getOptionValue( ARG_WORKERID );
      String reason = null;

      if( cmdLine.hasOption( ARG_REASON )) { 
        reason = cmdLine.getOptionValue( ARG_REASON );
      }

      revokeQualification( qualId, workerId, reason );

    }
    
    public void revokeQualification(String qualificationId, String workerId, String reason) {
        service.revokeQualification( qualificationId, workerId, reason );
        log.info( "Revoked qual " + qualificationId + " from " + workerId + " with reason: " + reason );
    }
}
