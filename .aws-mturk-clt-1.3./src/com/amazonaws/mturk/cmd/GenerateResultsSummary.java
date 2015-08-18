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
import org.apache.commons.lang.StringUtils;
import com.amazonaws.mturk.cmd.summary.ResultsSummarizerFactory;
import com.amazonaws.mturk.cmd.summary.ResultsSummarizer;

import java.util.Map;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * Base command to generate the summary of the results of a sample.
 */
public class GenerateResultsSummary extends AbstractCmd {

    /**
     * CommandLine option(s) for specifying the results file to be summarized.
     */
    private static final String ARG_RESULTS_FILENAME_OPTION_SHORT = "r";
    private static final String ARG_RESULTS_FILENAME_OPTION_LONG = "resultsFile";

    /**
     * CommandLine option(s) for specifying the file to which the summary is to be written.
     */
    private static final String ARG_SUMMARY_FILENAME_OPTION_SHORT = "o";
    private static final String ARG_SUMMARY_FILENAME_OPTION_LONG = "outputFile";

    /**
     * CommandLine option(s) for specifying the name of the sample whose results are to be summarized.
     */
    private static final String ARG_SAMPLE_NAME_OPTION_SHORT = "s";
    private static final String ARG_SAMPLE_NAME_OPTION_LONG = "sample";

    public static void main(String[] args) {
        GenerateResultsSummary sr = new GenerateResultsSummary();
        sr.run(args);
    }

    /**
     * @see AbstractCmd#initOptions()
     */
    protected void initOptions() {
        opt.addOption(ARG_RESULTS_FILENAME_OPTION_SHORT,
                ARG_RESULTS_FILENAME_OPTION_LONG, true,
                "Name of the file containing the results to be summarized");
        opt.addOption(ARG_SUMMARY_FILENAME_OPTION_SHORT,
                ARG_SUMMARY_FILENAME_OPTION_LONG, true,
                "Name of the output file containing the summary of the results");
        opt.addOption(ARG_SAMPLE_NAME_OPTION_SHORT,
                ARG_SAMPLE_NAME_OPTION_LONG, true,
                "Name of the sample program whose results are to be summarized");
    }

    /**
     * @see AbstractCmd#runCommand(CommandLine)
     */
    protected void runCommand(CommandLine cmdLine) throws Exception {
        String resultsFilename = cmdLine.getOptionValue(ARG_RESULTS_FILENAME_OPTION_LONG);
        if(! new File(resultsFilename).exists()){
            int separatorIndex = resultsFilename.lastIndexOf('/');
            if(separatorIndex < 0){
                separatorIndex = resultsFilename.lastIndexOf('\\');
            }
            log.error("The results file [" + resultsFilename.substring(separatorIndex+1) + "] does not exist. " +
                    "Please execute getResults before summarizing them.");
            System.exit(-1);
        }

        String sampleName = cmdLine.getOptionValue(ARG_SAMPLE_NAME_OPTION_LONG);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer(sampleName);
        List<String> summaryFields = rs.getSummaryFields();

        // summary values for each hitId
        Map<String, List<String>> summary = rs.getSummary(resultsFilename);

        // write the summary to the specified file.
        String summaryFilename = cmdLine.getOptionValue(ARG_SUMMARY_FILENAME_OPTION_LONG);
        writeSummary(summaryFields, summary, summaryFilename);

        int separatorIndex = summaryFilename.lastIndexOf('/');
        if(separatorIndex < 0){
            separatorIndex = summaryFilename.lastIndexOf('\\');
        }
        log.info("Results summary written to file " + summaryFilename.substring(separatorIndex + 1));
    }

    /**
     * Writes the given summary to the specified summary file.
     * @param summaryFields The summary column names.
     * @param summary Summary of a sample's results.
     * @param summaryFilename Name of the file to which the summary is to be written.
     */
    private void writeSummary(List<String> summaryFields, Map<String, List<String>> summary,
                              String summaryFilename) {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(summaryFilename));
            // print in tab-delimited format
            {
                String headerRowCsv = StringUtils.join(summaryFields, "\t");
                // add the manageHIT URL as the last column
                pw.println(headerRowCsv + "\tManage HIT");
            }

            for (Map.Entry<String, List<String>> hitSummary : summary.entrySet()) {
                String hitId = hitSummary.getKey();
                String summaryCsv = StringUtils.join(hitSummary.getValue(), "\t");
                // add the manage-hit URL as the last column
                String manageHITUrl = getManageHITUrl(hitId);
                pw.println(summaryCsv + "\t" + manageHITUrl);
            }

        } catch (FileNotFoundException e) {
            log.error("An error occurred while writing the results summary: " + e.getMessage(), e);
            System.exit(-1);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * @param hitId Hit ID of interest
     * @return
     * Returns the URL to the <i>manage-hit</i> page for the given hitId.
     */
    private String getManageHITUrl(String hitId) {
        return AbstractCmd.config.getRequesterWebsiteURL() + "/mturk/manageHIT?HITId=" + hitId;
    }

    /**
     * @see AbstractCmd#printHelp()
     */
    protected void printHelp() {
        formatter.printHelp(GenerateResultsSummary.class.getSimpleName()
                + " -" + ARG_RESULTS_FILENAME_OPTION_LONG
                + " [path to results file] "
                + " -" + ARG_SUMMARY_FILENAME_OPTION_LONG
                + " [path to output file] "
                + " -" + ARG_SAMPLE_NAME_OPTION_LONG
                + " [name of the sample] "
                + "", opt);
    }
}
