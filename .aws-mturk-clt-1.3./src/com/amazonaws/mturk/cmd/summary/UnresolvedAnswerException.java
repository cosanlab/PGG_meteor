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
 * Thrown when a quorum-based technique (where answer with max votes wins) cannot
 * be used to determine the correct answer.
 */
public class UnresolvedAnswerException extends Exception {

    
    private ErrorReason reason;
    /**
     * Constructor with error message.
     * @param message error message.
     * @param reason error reason
     */
    
    public UnresolvedAnswerException(String message, ErrorReason reason) {
        super(message);
        this.reason = reason;
    }
    
    public ErrorReason getErrorReason() {
        return this.reason;
    }
    
    public static enum ErrorReason {
        InProgress("in progress"),
        NoAgreement("no agreement");
        
        private String reason;
        private ErrorReason(String reason) {
            this.reason = reason;
        }
        public String getReason() {
            return this.reason;
        }
    }
}
