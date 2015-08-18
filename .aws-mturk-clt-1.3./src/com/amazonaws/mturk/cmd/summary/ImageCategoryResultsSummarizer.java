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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A results summarizer that is specific to the <i>Image Category</i> sample.
 */
public class ImageCategoryResultsSummarizer implements ResultsSummarizer{

    /**
     * Default separator for the fields in the assignment records (results file).
     */
    private static final char DEFAULT_FIELD_SEPARATOR = '\t';

    /**
     * Non-public access: allow only the factory to instantiate this.
     */
    ImageCategoryResultsSummarizer(){

    }

    /**
     * List of columns in the final summary of the <i>image category</i> sample.
     */
    private static final List<String> summaryFields =
            Arrays.asList("HIT ID", "Image URL", "Question", "Answer", "Score", "%");

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
                "annotation", "Answer.category", "assignments"
        };

        int[] fieldIndices = new int[3];
        Map<String, List<String[]>> hitAssignments = SummaryUtils.parseHitAssignments(
                resultsFile, requiredFields, fieldIndices, HITResults.DELIMITER);

        int urlIndex = fieldIndices[0];
        int answerIndex = fieldIndices[1];
        int numAssignmentsIndex = fieldIndices[2];

        // we've built our hitId -> List<assignments> map.
        // hitId -> imageFileNNN.jpg
        Map<String, String> hitImageFilenames = SummaryUtils.extractQuestion(hitAssignments, urlIndex);

        // get the results summarized.
        // hitId -> "Answer", "Score", "%"
        Map<String, List<String>> hitResultsSummaries = SummaryUtils.summarizeResults(hitAssignments, answerIndex, numAssignmentsIndex);
        if(hitResultsSummaries.size() != hitImageFilenames.size()){
            throw new RuntimeException("Did not find image names for all HITs");
        }

        // merge the 2 partial sets of summary fields
        Map<String, List<String>> finalSummaries = new LinkedHashMap<String, List<String>>();

        // see fields listed in summaryFields above
        for(String hitId : hitImageFilenames.keySet()){
            List<String> fields = new ArrayList<String>();
            fields.add(hitId);
            fields.add(hitImageFilenames.get(hitId));
            fields.add("Category:");
            fields.addAll(hitResultsSummaries.get(hitId));
            finalSummaries.put(hitId, fields);
        }

        return finalSummaries;
    }
    
}
