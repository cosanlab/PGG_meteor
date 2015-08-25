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

import java.util.List;
import java.util.Map;
import java.text.ParseException;

/**
 * Interface for the ResultsSummarizer.
 */
public interface ResultsSummarizer {

    /**
     * Asks the summarizer to summarize the results contained in the specified file.
     * @param resultsFile The file containing the results to be summarized.
     * @return A summary record for each hitId present in the results file.
     * @throws ParseException In case of problems parsing the results file.
     */
    Map<String, List<String>> getSummary(String resultsFile) throws ParseException;

    /**
     * Gets the names of the various summary fields for the summary of the results
     * for the sample that this summarizer handles.
     * @return
     * Gets the names of the various summary fields.
     */
    List<String> getSummaryFields();

}
