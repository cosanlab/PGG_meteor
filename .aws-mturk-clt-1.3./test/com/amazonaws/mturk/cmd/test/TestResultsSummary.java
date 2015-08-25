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

package com.amazonaws.mturk.cmd.test;

import com.amazonaws.mturk.cmd.summary.ResultsSummarizerFactory;
import com.amazonaws.mturk.cmd.summary.ResultsSummarizer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

/**
 * Basic test cases for the GenerateResultsSummary feature.
 */
public class TestResultsSummary extends TestBase {

    private static final String BaseHeaderFields = "\"hitid\"\t\"hittypeid\"\t\"title\"\t\"description\"\t\"keywords\"\t\"reward\"\t\"creationtime\"\t\"assignments\"\t\"numavailable\"\t\"numpending\"\t\"numcomplete\"\t\"hitstatus\"\t\"reviewstatus\"\t\"annotation\"\t\"assignmentduration\"\t\"autoapprovaldelay\"\t\"hitlifetime\"\t\"assignmentid\"\t\"workerid\"\t\"assignmentstatus\"\t\"autoapprovaltime\"\t\"assignmentaccepttime\"\t\"assignmentsubmittime\"\t\"assignmentapprovaltime\"\t\"assignmentrejecttime\"\t\"deadline\"\t\"feedback\"";
    private static final String ImageCategoryHeaderLine = BaseHeaderFields + "\t\"Answer.category\"";
    private static final String SiteFilterHeader = BaseHeaderFields + "\t\"Answer.inappropriate\"";

    public TestResultsSummary() {
        super("TestResultsSummary");
    }

    public void testHappyAllSameAnswersImageCategory() throws Exception {

        String a1, a2, a3;
        a1 = a2 = a3 = "\"SameAnswer\"";

        // h1 has 3 assignments - all with same answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("3 of 3", h1Summary.get(4));
        assertEquals("100", h1Summary.get(5));
    }

    public void testNoAssignmentsSubmitted() throws Exception {

        // no assignments submitted
        List<String> lines = Arrays.asList(ImageCategoryHeaderLine);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        assertTrue(summary.isEmpty());
    }

    public void testPartialSubmissionAndNoWinner() throws Exception {

        // 7 max assignments; 5 answers submitted: 3(a1), 1(a2), 1(a3); 2 not submitted.
        // no resolution.
        String a1, a2, a3, a4, a5;
        a1 = a2 = "\"SameAnswer\"";
        a3 = "\"DifferentAnswer0\"";
        a4 = "\"DifferentAnswer1\"";
        a5 = "\"DifferentAnswer2\"";

        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;
        String h1a4 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a4;
        String h1a5 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a5;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3, h1a4, h1a5);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("[no agreement]", h1Summary.get(3));
        assertEquals("[n/a]", h1Summary.get(4));
        assertEquals("[n/a]", h1Summary.get(5));
    }

    public void testPartialSubmissionButWinnerPresent() throws Exception {

        // 7 max assignments; 6 answers submitted: 3(a1), 1(a2), 1(a3), 1(a4); 1 not submitted
        // a1 should still win (even though not majority)
        String a1, a2, a3, a4, a5, a6;
        a1 = a2 = a3 = "\"SameAnswer\"";
        a4 = "\"DifferentAnswer1\"";
        a5 = "\"DifferentAnswer2\"";
        a6 = "\"DifferentAnswer3\"";

        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;
        String h1a4 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a4;
        String h1a5 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a5;
        String h1a6 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"7\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a6;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3, h1a4, h1a5, h1a6);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);

        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("3 of 7", h1Summary.get(4));
        assertEquals("43", h1Summary.get(5));
    }

    public void test2Of3SameAnswersImageCategory() throws Exception {

        String a1, a2, a3;
        a1 = a2 = "\"SameAnswer\"";
        a3 = "\"DifferentAnswer\"";

        // h1 has 3 assignments - 2 with same answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("2 of 3", h1Summary.get(4));
        assertEquals("67", h1Summary.get(5));
    }

    public void testAllDifferentAnswersImageCategory() throws Exception {

        String a1, a2, a3;
        a1 = "\"Answer1\"";
        a2 = "\"Answer2\"";
        a3 = "\"Answer3\"";

        // h1 has 3 assignments - all with different answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("[no agreement]", h1Summary.get(3));
        assertEquals("[n/a]", h1Summary.get(4));
        assertEquals("[n/a]", h1Summary.get(5));
    }

    public void testAllCasesTogetherImageCategory() throws Exception {

        String a11, a12, a13;
        a11 = a12 = a13 = "\"SameAnswer\"";

        // h1 has 3 assignments - all with same answers
        String h1 = "DUMMY_HIT_ID1";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img1.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a11;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img1.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a12;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img1.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a13;

        // h2 has 3 assignments - 2 with same answers

        String a21, a22, a23;
        a21 = a22 = "\"SameAnswer\"";
        a23 = "\"DifferentAnswer\"";

        String h2 = "DUMMY_HIT_ID2";
        String h2a1 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img2.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a21;
        String h2a2 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img2.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a22;
        String h2a3 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img2.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a23;

        // h3 has 3 assignments - all with different answers
        String a31, a32, a33;
        a31 = "\"Answer1\"";
        a32 = "\"Answer2\"";
        a33 = "\"Answer3\"";

        String h3 = "DUMMY_HIT_ID3";
        String h3a1 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img3.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a31;
        String h3a2 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img3.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a32;
        String h3a3 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Categorize an Image\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/img3.jpg\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a33;

        List<String> lines = Arrays.asList(ImageCategoryHeaderLine, h1a1, h1a2, h1a3, h2a1, h2a2, h2a3, h3a1, h3a2, h3a3);

        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("image_category");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);

        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/img1.jpg", h1Summary.get(1));
        assertEquals("Category:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("3 of 3", h1Summary.get(4));
        assertEquals("100", h1Summary.get(5));

        List<String> h2Summary = summary.get(h2);
        assertEquals(h2, h2Summary.get(0));
        assertEquals("http://some.website.com/img2.jpg", h2Summary.get(1));
        assertEquals("Category:", h2Summary.get(2));
        assertEquals("SameAnswer", h2Summary.get(3));
        assertEquals("2 of 3", h2Summary.get(4));
        assertEquals("67", h2Summary.get(5));

        List<String> h3Summary = summary.get(h3);
        assertEquals(h3, h3Summary.get(0));
        assertEquals("http://some.website.com/img3.jpg", h3Summary.get(1));
        assertEquals("Category:", h3Summary.get(2));
        assertEquals("[no agreement]", h3Summary.get(3));
        assertEquals("[n/a]", h3Summary.get(4));
        assertEquals("[n/a]", h3Summary.get(5));
    }

    public void testHappyAllSameAnswersSiteFilter() throws Exception {

        String a1, a2, a3;
        a1 = a2 = a3 = "\"SameAnswer\"";

        // h1 has 3 assignments - all with same answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(SiteFilterHeader, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("site_filter");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/", h1Summary.get(1));
        assertEquals("Is inappropriate:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("3 of 3", h1Summary.get(4));
        assertEquals("100", h1Summary.get(5));
    }

    public void test2Of3SameAnswersSiteFilter() throws Exception {

        String a1, a2, a3;
        a1 = a2 = "\"SameAnswer\"";
        a3 = "\"DifferentAnswer\"";

        // h1 has 3 assignments - 2 with same answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(SiteFilterHeader, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("site_filter");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/", h1Summary.get(1));
        assertEquals("Is inappropriate:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("2 of 3", h1Summary.get(4));
        assertEquals("67", h1Summary.get(5));
    }

    public void testAllDifferentAnswersSiteFilter() throws Exception {

        String a1, a2, a3;
        a1 = "\"Answer1\"";
        a2 = "\"Answer2\"";
        a3 = "\"Answer3\"";

        // h1 has 3 assignments - all with different answers
        String h1 = "DUMMY_HIT_ID";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a1;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a2;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a3;

        List<String> lines = Arrays.asList(SiteFilterHeader, h1a1, h1a2, h1a3);
        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("site_filter");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);
        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some.website.com/", h1Summary.get(1));
        assertEquals("Is inappropriate:", h1Summary.get(2));
        assertEquals("[no agreement]", h1Summary.get(3));
        assertEquals("[n/a]", h1Summary.get(4));
        assertEquals("[n/a]", h1Summary.get(5));
    }

    public void testAllCasesTogetherSiteFilter() throws Exception {

        String a11, a12, a13;
        a11 = a12 = a13 = "\"SameAnswer\"";

        // h1 has 3 assignments - all with same answers
        String h1 = "DUMMY_HIT_ID1";
        String h1a1 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some1.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a11;
        String h1a2 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some1.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a12;
        String h1a3 = "\"" + h1 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some1.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a13;

        // h2 has 3 assignments - 2 with same answers

        String a21, a22, a23;
        a21 = a22 = "\"SameAnswer\"";
        a23 = "\"DifferentAnswer\"";

        String h2 = "DUMMY_HIT_ID2";
        String h2a1 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some2.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a21;
        String h2a2 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some2.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a22;
        String h2a3 = "\"" + h2 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some2.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a23;

        // h3 has 3 assignments - all with different answers
        String a31, a32, a33;
        a31 = "\"Answer1\"";
        a32 = "\"Answer2\"";
        a33 = "\"Answer3\"";

        String h3 = "DUMMY_HIT_ID3";
        String h3a1 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some3.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0R3YZ78ZDQ2D86RYJ2GGZ\"\t\"AZ58BX52DG5FJ\"\t\"Submitted\"\t\"Thu Mar 06 10:09:14 PST 2008\"\t\"Mon Mar 03 10:09:11 PST 2008\"\t\"Mon Mar 03 10:09:14 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a31;
        String h3a2 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some3.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX06S2ZWWYH8XEZSQY7CWVZ\"\t\"AHN23PP23PHZK\"\t\"Submitted\"\t\"Thu Mar 06 10:09:09 PST 2008\"\t\"Mon Mar 03 10:09:04 PST 2008\"\t\"Mon Mar 03 10:09:09 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a32;
        String h3a3 = "\"" + h3 + "\"\t\"DUMMY_HIT_TYPE_ID\"\t\"Adult Site?\"\t\"Problem description\"\t\"kw1, kw2\"\t\"$0.01\"\t\"Mon Mar 03 10:03:56 PST 2008\"\t\"3\"\t\"0\"\t\"0\"\t\"0\"\t\"Reviewable\"\t\"NotReviewed\"\t\"http://some3.website.com/\"\t\"3600\"\t\"259200\"\t\"Mon Mar 10 11:03:56 PDT 2008\"\t\"8BD4YQS0W3K0WK9AAGX0BWNRTAY5CWE2NZHWGYWZ\"\t\"A34LZPSJ3FXAUA\"\t\"Submitted\"\t\"Thu Mar 06 10:10:40 PST 2008\"\t\"Mon Mar 03 10:10:12 PST 2008\"\t\"Mon Mar 03 10:10:40 PST 2008\"\t\"\"\t\"\"\t\"\"\t\"\"\t" + a33;

        List<String> lines = Arrays.asList(SiteFilterHeader, h1a1, h1a2, h1a3, h2a1, h2a2, h2a3, h3a1, h3a2, h3a3);

        String resultsFilename = getResultsFile(lines);

        ResultsSummarizer rs = ResultsSummarizerFactory.getSummarizer("site_filter");
        Map<String,List<String>> summary = rs.getSummary(resultsFilename);

        List<String> h1Summary = summary.get(h1);
        assertEquals(h1, h1Summary.get(0));
        assertEquals("http://some1.website.com/", h1Summary.get(1));
        assertEquals("Is inappropriate:", h1Summary.get(2));
        assertEquals("SameAnswer", h1Summary.get(3));
        assertEquals("3 of 3", h1Summary.get(4));
        assertEquals("100", h1Summary.get(5));

        List<String> h2Summary = summary.get(h2);
        assertEquals(h2, h2Summary.get(0));
        assertEquals("http://some2.website.com/", h2Summary.get(1));
        assertEquals("Is inappropriate:", h2Summary.get(2));
        assertEquals("SameAnswer", h2Summary.get(3));
        assertEquals("2 of 3", h2Summary.get(4));
        assertEquals("67", h2Summary.get(5));

        List<String> h3Summary = summary.get(h3);
        assertEquals(h3, h3Summary.get(0));
        assertEquals("http://some3.website.com/", h3Summary.get(1));
        assertEquals("Is inappropriate:", h3Summary.get(2));
        assertEquals("[no agreement]", h3Summary.get(3));
        assertEquals("[n/a]", h3Summary.get(4));
        assertEquals("[n/a]", h3Summary.get(5));
    }

    private String getResultsFile(List<String> lines) throws IOException {
        File f = File.createTempFile("testImageCategorization", "results");
        f.deleteOnExit();

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
            for(String l : lines){
                pw.println(l);
            }
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        return f.getAbsolutePath();
    }

}
