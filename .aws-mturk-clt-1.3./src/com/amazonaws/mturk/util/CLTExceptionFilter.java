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


package com.amazonaws.mturk.util;

import com.amazonaws.mturk.filter.Filter;
import com.amazonaws.mturk.filter.Message;
import com.amazonaws.mturk.filter.Reply;
import com.amazonaws.mturk.service.exception.InsufficientFundsException;
import com.amazonaws.mturk.service.exception.InternalServiceException;
import com.amazonaws.mturk.service.exception.ServiceException;

/**
 * Filter used to postprocess exceptions thrown by the Requester service for CLT usage.
 */
public class CLTExceptionFilter extends Filter {

  private static boolean isInThrottleMode = false;
  private ClientConfig config = null;
  
  public CLTExceptionFilter(ClientConfig config) {
    this.config = config;
  }

  /**
   * Calls the next filter and catches ServiceException. If it is a exception, 
   * it wraps the error in a more userfriendly error message to be displayed to the CLT user.
   * 
   * Also the filter implements a guaranteed message delivery, ie if a transient throttling
   * error has occured after the retry filter has exhausted its retries, the message is
   * sent down the filter chain again until it succeeds or a non transient error occurs
   */ 
  @Override
  public Reply execute(Message m) throws ServiceException {

    try {
      if (isInThrottleMode) {
        try {
          // avoid new messages to be processed immediately so
          // that the waiting threads aren't starved and have a 
          // chance to get a token from the D-Throttle bucket
          Thread.sleep(config.getRetryDelayMillis());
        }
        catch (InterruptedException ex) {
          // do nothing
        }
      }
      
      return sendMessage(m);
    }
    catch (InsufficientFundsException fundsEx) {
      throw new InsufficientFundsException("You do not have sufficient funds in your Mechanical Turk account to execute this operation", fundsEx);
    } 
  }    
  
  /**
   * Sends the message until it is successfully delivered or 
   * a non-transient error was received
   */
  private Reply sendMessage(Message m) {
    Reply ret = null;
    while (ret == null) {
      try {
        ret = passMessage(m);
      }
      catch (InternalServiceException e) {
        boolean isTransientError = false;
        for (String errorCode : e.getErrorCodes()) {
          if (config.getRetriableErrors().contains(errorCode)) {
            isTransientError = true;
            break;
          }            
        }
        
        if (isTransientError) {
          isInThrottleMode = true; // start throttling for all threads until command finished
        }
        else {
          throw e;
        }
      }
    }
    return ret;
  }
}
