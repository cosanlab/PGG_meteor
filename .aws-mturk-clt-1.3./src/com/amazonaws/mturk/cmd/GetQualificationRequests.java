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

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.QualificationRequest;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.FileUtil;

public class GetQualificationRequests extends AbstractCmd {
    private final static String ARG_HELP = "help"; 
    private final static String ARG_QUALTYPEID = "qualtypeid";
    private final String ARG_OUTPUTFILE = "outputfile";
    
    private String outputFileName;
    private FileUtil file;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        GetQualificationRequests cmd = new GetQualificationRequests();
        cmd.run(args);
    }
    
    protected void initOptions() {
      opt.addOption(ARG_HELP, false, "Print help for this application");
      opt.addOption(ARG_QUALTYPEID, true, "(optional) Qualification Type to limit results to");
      opt.addOption(ARG_OUTPUTFILE, true, "(required) The file to which you'd like your results saved");
    }
    
    protected void printHelp() {
      formatter.printHelp(GetQualificationRequests.class.getName()
          + " -" + ARG_OUTPUTFILE + " [path to output file]"
          + " -" + ARG_QUALTYPEID + " QualificationTypeId to fetch", opt);
    }

    protected void runCommand(CommandLine cmdLine) throws Exception {
      if (!cmdLine.hasOption(ARG_OUTPUTFILE)) {
        log.fatal("Missing: -" + ARG_OUTPUTFILE + " [path to output file]");
        System.exit(-1);
      }

      String qualTypeId = null;

      if(cmdLine.hasOption(ARG_QUALTYPEID)) {
        qualTypeId = cmdLine.getOptionValue(ARG_QUALTYPEID);
      }

      outputFileName = cmdLine.getOptionValue(ARG_OUTPUTFILE);

      getQualificationRequests(qualTypeId );

    }

    public void getQualificationRequests(String qualTypeId) throws IOException {
        QualificationRequest[] qualReqs = service.getAllQualificationRequests(qualTypeId);
        
        if(qualReqs != null & qualReqs.length > 0) {
            log.info("Retrieved " +  qualReqs.length + " Qualification Requests");
            getFile().saveString("QualificationTypeId\tQualificationRequestId\tSubjectId\tAnswer\n", true);
            
            for(QualificationRequest qualReq : qualReqs) {
                String answerString = getAnswers(qualReq) + System.getProperty("line.separator");
                getFile().saveString(answerString, true);
            }
            log.info("Answers successfully saved to file: " + this.outputFileName);
        }
        else {
            log.info("No Qualification Requests found");
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private String getAnswers(QualificationRequest qualRequest) {
      String result = qualRequest.getQualificationTypeId() + "\t" 
                  + qualRequest.getQualificationRequestId() + "\t" 
                  + qualRequest.getSubjectId();
      
      String answerXML = qualRequest.getAnswer();
      if (answerXML == null) {
          return result;
      }
      QuestionFormAnswers qfa = RequesterService.parseAnswers(answerXML);
      List<QuestionFormAnswersType.AnswerType> answers = 
        (List<QuestionFormAnswersType.AnswerType>) qfa.getAnswer();

      for (QuestionFormAnswersType.AnswerType answer : answers) {
        String answerValue = RequesterService.getAnswerValue(null, answer, true);

        if (answerValue != null) {
          result += "\t" + answerValue;
        }
      }

      return result;
    }
    
    private FileUtil getFile() throws IOException {

        // Don't create the file until it is needed
        if (file==null) {
          this.file = new FileUtil(outputFileName);
        }

        return file;
      }
}
