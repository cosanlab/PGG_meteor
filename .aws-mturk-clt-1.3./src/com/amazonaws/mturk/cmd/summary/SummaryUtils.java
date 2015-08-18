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

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.cmd.summary.UnresolvedAnswerException.ErrorReason;

/**
 * Helper class available to various summarizer implementations.
 */
class SummaryUtils {
    private static Logger log = Logger.getLogger(SummaryUtils.class);

    private SummaryUtils(){
        // all methods are static
    }

    /**
     * Builds the <i>common</i> summary of the results for the hits and assignments.
     * @param hitAssignments Map containing hitId -> List<assignment> information.
     * @param answerIndex Index of the answer in the tab-delimited record of an assignment.
     * @param numAssignmentsIndex Index in the max-assignments (hit property) count in the assignment record.
     * @return Common summary of the results.
     */
    public static Map<String, List<String>> summarizeResults(
            Map<String, List<String[]>> hitAssignments, int answerIndex, int numAssignmentsIndex) {

        Map<String, List<String>> hitSummaries = new LinkedHashMap<String, List<String>>();
        for(Map.Entry<String,List<String[]>> hitResults : hitAssignments.entrySet()){
            String hitId = hitResults.getKey();
            List<String[]> assignmentsDetails = hitResults.getValue();
            List<String> answers = new ArrayList<String>();
            Integer numAssignments = null;
            for(String[] a : assignmentsDetails){
                answers.add(a[answerIndex]);

                // record the max number of assignments for this hit.
                if(numAssignments == null){
                    numAssignments = Integer.parseInt(a[numAssignmentsIndex]);
                } else {
                    if(numAssignments != Integer.parseInt(a[numAssignmentsIndex])){
                        System.err.println("Inconsistent hit data (assignments) across assignments of hit [" + hitId + "]. Cannot proceed.");
                        System.exit(-1);
                    }
                }
            }

            String mostFreqAnswer;
            String score;
            String percentCorrect;
            try {
                int[] numMostFreqVotes = new int[1];
                mostFreqAnswer = findMostFrequent(answers, numMostFreqVotes, numAssignments);
                int numCorrect = numMostFreqVotes[0];
                score = numCorrect + " of " + numAssignments;
                percentCorrect = String.valueOf((int)
                        Math.rint(((double)numCorrect*100)/numAssignments)); // not interested in decimal places
            } catch (UnresolvedAnswerException e) {
                // no unique winner for this question.
                
                mostFreqAnswer = "[" + e.getErrorReason().getReason() + "]";
                score = percentCorrect = "[n/a]";
            }

            // "Answer", "Score", "%"
            List<String> hitSummary = Arrays.asList(mostFreqAnswer, score, percentCorrect);
            hitSummaries.put(hitId, hitSummary);

        } // Rof-each-hit

        return hitSummaries;
    } // summarizeResults(..): Map

    /**
     * Given a set of <tt>String</tt>s, finds the most frequently occurring <tt>String</tt>,
     * if there's a unique element that occurs the max times.
     * @param answers All answers for a hit.
     * @param numMostFreqVotes Place holder to be filled-in with the number of
     * occurrences of the most-frequent answer.
     * @param numAssignments
     * @return The most frequent answer.
     * @throws UnresolvedAnswerException In case there are multiple entries in the given list
     * which occur the same number of (max) times in the list.
     */
    public static String findMostFrequent(List<String> answers, int[] numMostFreqVotes, int numAssignments) throws UnresolvedAnswerException {
        if(answers == null || answers.size() == 0){
            throw new UnresolvedAnswerException("No answers submitted", ErrorReason.InProgress);
        }

        boolean partialResults = answers.size() < numAssignments;

        Map<String, Integer> answerCounts = new HashMap<String, Integer>();

        // loop through, build the count-map,
        // and also find the entry with max count.
        int maxCount = -1;
        String mostFreqAnswer = "";
        for(String a : answers){
            Integer c = answerCounts.get(a);
            int newCount = (c == null) ? 1 : c + 1;
            answerCounts.put(a, newCount);
            if(newCount > maxCount){
                maxCount = newCount;
                mostFreqAnswer = a;
            }
        }

        // set maxCount in the supplied param.
        numMostFreqVotes[0] = maxCount;

        // loop again to make sure that there's only 1 answer with max votes
        for(String a : answers){
            Integer c = answerCounts.get(a);
            if(c == maxCount && !a.equals(mostFreqAnswer)){
                throw new UnresolvedAnswerException("Multiple max-voted answers: " + answers, ErrorReason.NoAgreement);
            }
        }

        if(partialResults){
            // not all assignments have been submitted.
            // we'll check if the max-voted answer so far can still remain the winner even if
            // none of the remaining answers voted for it.
            // find the runner-up, and see if it can win if it gets all the remaining votes.
            
            int secondMaxCount = 0;
            // find the answer with 2nd highest votes.
            for(String a : answers){
                if(!a.equals(mostFreqAnswer)){ // exclude the most-freq answer
                    Integer c = answerCounts.get(a);
                    if(c > secondMaxCount){
                        secondMaxCount = c;
                    }
                }
            }

            // see if this answer can win in case all the remaining assignments were submitted with
            // this answer.
            int numRemaining = numAssignments - answers.size();
            if(secondMaxCount + numRemaining >= maxCount){
                throw new UnresolvedAnswerException("Cannot determine winner: " +
                        "Only " + answers.size() + " of " + numAssignments + " assignments " +
                        "submitted, with no clear winner.", ErrorReason.InProgress);
            }
        }


        return mostFreqAnswer;
    }

    /**
     * Method to parse out the hitId -> List<assignments> information out of the
     * results file.
     * @param resultsFile The results file of interest.
     * @param requiredFields Fields that should be present in a correct results file.
     * @param fieldIndicesPlaceholder Place holders for the required fields' indices.
     * @param fieldSeparator Separator for the fields in an assignment record.
     * @return Map containing the hitId -> List<assignment> information.
     */
    public static Map<String, List<String[]>> parseHitAssignments(
            String resultsFile, String[] requiredFields, int[] fieldIndicesPlaceholder, char fieldSeparator) {

        Map<String, List<String[]>> hitAssignments = new LinkedHashMap<String, List<String[]>>();
        try {
            HITDataCSVReader hitDataReader = new HITDataCSVReader(resultsFile, fieldSeparator);
            List<String> headers = Arrays.asList(hitDataReader.getFieldNames());

            int index=0;
            for(String f : requiredFields){
                int x = headers.indexOf(f);
                if (x < 0){
                    throw new ParseException("Did not find field[" + f + "] in headers-line", 0); // line # 0
                }
                fieldIndicesPlaceholder[index++] = x;
            }
            int hitIdIndex = headers.indexOf(HITProperties.HITField.HitId.getFieldName());
            // 0-th row is the headerRow
            final int numRows = hitDataReader.getNumRows();
            for(int i=1; i<numRows; i++){
                String[] row = hitDataReader.getRowValues(i);
                String hitId = row[hitIdIndex];
                List<String[]> assignments = hitAssignments.get(hitId);
                if (assignments == null) {
                    assignments = new ArrayList<String[]>();
                    hitAssignments.put(hitId, assignments);
                }
                assignments.add(row);
                
            }
            
        } catch (Exception e) {
            System.err.println("There was a problem processing the results file: " + e.getMessage());
            System.exit(-1);
        }

        return hitAssignments;
    } // parseHitAssignments(..): Map

    /**
     * Builds the hitId -> question map from the hit -> List<assignments> map.
     * @param hitAssignments Maps a hit id to the list of assignments belonging to it.
     * @param questionIndex Index of the question field in the tab delimited assignment record.
     * @return Map containing hitId -> question information.
     */
    public static Map<String, String> extractQuestion(Map<String, List<String[]>> hitAssignments, int questionIndex) {
        Map<String, String> hitQuestions = new LinkedHashMap<String, String>();
        for(Map.Entry<String, List<String[]>> hitResults : hitAssignments.entrySet()){
            String hitId = hitResults.getKey();
            List<String[]> assignmentsDetails = hitResults.getValue();
            String input = "";
            for(String[] a : assignmentsDetails){
                String question = a[questionIndex];
                if(input.equals("")){
                    input = question;
                } else if(!input.equals(question)) {
                    throw new IllegalArgumentException("Question mismatch for hit " + hitId + ": " + a);
                }
            }

            hitQuestions.put(hitId, input);
        }

        return hitQuestions;
    }
}
