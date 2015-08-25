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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class TestStream extends AppenderSkeleton 
{   
  StringBuilder runningOutput = new StringBuilder();

  public TestStream() 
  {
    super();
  }

  private static TestStream instance = null;
  public static TestStream getInstance() {
    if (instance == null) {
      instance = new TestStream();
    }
    return instance;
  } 


  public String getRunningOutput()
  {
    return runningOutput.toString();
  }

  public void resetRunningOutput()
  {
    this.runningOutput = new StringBuilder();
  }

  @Override
  protected void append( LoggingEvent event ) {
    runningOutput.append( event.getLevel().toString() );
    runningOutput.append( " -- " );
    runningOutput.append( event.getRenderedMessage() );
    runningOutput.append( "\n" );
  }

  public void close() {
    // TODO Auto-generated method stub

  }

  public boolean requiresLayout() {
    // TODO Auto-generated method stub
    return false;
  }
} ///:~
