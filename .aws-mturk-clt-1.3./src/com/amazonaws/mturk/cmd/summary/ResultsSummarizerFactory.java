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

/**
 * Factory for instantiating various types of ResultsSummarizer implementations.
 */
public class ResultsSummarizerFactory {

    /**
     * Given a sample name, returns an appropriate <tt>ResultsSummarizer</tt>
     * instance to summarize the given sample's results.
     * @param sampleName name of the sample of interest.
     * @return A <tt>ResultsSummarizer</tt> instance that can summarize the results
     * of the given sample.
     */
    public static ResultsSummarizer getSummarizer(String sampleName) {
        if("image_category".equalsIgnoreCase(sampleName)){
            return new ImageCategoryResultsSummarizer();
        }

        if("site_filter".equalsIgnoreCase(sampleName)){
            return new SiteFilterResultsSummarizer();
        }

        throw new IllegalArgumentException("Results Summary " +
                "not supported for sample[" + sampleName + "] yet");
    }
    
}
