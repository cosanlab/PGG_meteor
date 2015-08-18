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


package com.amazonaws.mturk.cmd.summary;

import com.amazonaws.mturk.addon.HITResults;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * A results summarizer that is specific to the <i>Site Filter</i> sample.
 */
public class SiteFilterResultsSummarizer implements ResultsSummarizer {

    /**
     * Non-public access: allow only the factory to instantiate this.
     */
    SiteFilterResultsSummarizer(){

    }

    /**
     * List of columns in the final summary of the <i>site filter</i> sample.
     */
    private static final List<String> summaryFields
            = Arrays.asList("HIT ID", "Website URL", "Question", "Answer", "Score", "%");

    /**
     * @see ResultsSummarizer#getSummaryFields()
     */
    public List<String> getSummaryFields() {
        return summaryFields;
    }

    /**
     * @see ResultsSummarizer#getSummary(String)
     */
    public Map<String, List<String>> getSummary(String resultsFile) throws ParseException {

        // hitid is always required.
        String[] requiredFields = new String[]{
                "annotation", "Answer.inappropriate", "assignments"
        };
        int[] fieldIndices = new int[3];
        Map<String, List<String[]>> hitAssignments =
                SummaryUtils.parseHitAssignments(resultsFile, requiredFields, fieldIndices, HITResults.DELIMITER);
        int urlIndex = fieldIndices[0];
        int answerIndex = fieldIndices[1];
        int numAssignmentsIndex = fieldIndices[2];

        try {
            // we've built our hitId -> List<assignments> map.
            // hitId -> http://www.website.com
            Map<String, String> hitWebsiteURLs = SummaryUtils.extractQuestion(hitAssignments, urlIndex);

            // get the results summarized.
            // hitId -> "Answer", "Score", "%"
            Map<String, List<String>> hitResultsSummaries =
                    SummaryUtils.summarizeResults(hitAssignments, answerIndex, numAssignmentsIndex);
            if (hitResultsSummaries.size() != hitWebsiteURLs.size()) {
                throw new RuntimeException("Did not find website URLs for all HITs");
            }

            // merge the 2 partial sets of summary fields
            Map<String, List<String>> finalSummaries = new LinkedHashMap<String, List<String>>();

            // see fields listed in summaryFields above
            for (String hitId : hitWebsiteURLs.keySet()) {
                List<String> fields = new ArrayList<String>();
                fields.add(hitId);
                fields.add(hitWebsiteURLs.get(hitId));
                fields.add("Is inappropriate:");
                fields.addAll(hitResultsSummaries.get(hitId)); // Answer, Score, %
                finalSummaries.put(hitId, fields);
            }

            return finalSummaries;

        } catch (Exception e) {
            System.err.println("There was a problem processing the results file: " + e.getMessage());
            System.exit(-1);
        }

        return null; // make javac happy
    }

}
