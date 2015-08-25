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



package com.amazonaws.mturk.cmd.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptRunner {
    
    private static final boolean USE_CYGWIN;
    
    static {
        USE_CYGWIN = Boolean.parseBoolean(System.getProperty("USE_CYGWIN"));
    }

    public class ScriptResult {
        private String command;
        private String output;
        private int exitValue;
        
        public String getCommand() {
            return command;
        }
        
        public String getOutput() {
            return output;
        }
        
        public int getExitValue() {
            return exitValue;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("COMMAND: " + command + "\n");
            sb.append("OUTPUT: " + output + "\n");
            sb.append("EXITVALUE: " + exitValue + "\n");
            return sb.toString();
        }
    }
    
    public ScriptRunner() {
    }
    
    public ScriptResult runScript(File baseDir, String name, String... args) throws InterruptedException, IOException {
        return runScript(baseDir, name, Arrays.asList(args));
    }
    
    public ScriptResult runScript(File baseDir, String scriptName, List<String> args) throws InterruptedException, IOException {
        ScriptResult result = new ScriptResult();
        
        // Build the path to the script based on the OS
        String extension;
        if (System.getProperty("os.name").toLowerCase().contains("windows") && !USE_CYGWIN) {
            extension = ".cmd";
        } else {
            extension = ".sh";
        }
        scriptName = scriptName + extension;
        String scriptPath = new File(baseDir, scriptName).getAbsolutePath();

        // Stick the script to execute on the front of the args list,
        // and force everything to run against the sandbox
        List<String> fullArgs = new ArrayList<String>();
        fullArgs.add(scriptPath);
        fullArgs.add("--sandbox");
        fullArgs.addAll(args);
        
        // Convert to Cygwin if necessary
        if (USE_CYGWIN) {
            fullArgs = getCygwinArgs(fullArgs);
        }

        // Save the command
        StringBuffer sb = new StringBuffer();
        for (String arg: fullArgs) {
            sb.append(arg + " ");
        }
        result.command = sb.toString();
        
        // Start the process in the correct working directory with stderr redirected to stdout,
        // and force a hard exit so we can get back the return code
        ProcessBuilder pb = new ProcessBuilder(fullArgs);
        pb.directory(baseDir);
        pb.redirectErrorStream(true);
        pb.environment().put("MTURK_TERMINATE_CMD", "on");
        Process p = pb.start();
        
        // Save the output
        List<String> lines = drainInputStream(p.getInputStream());
        StringBuffer output = new StringBuffer();
        for (String line: lines) {
            output.append(line + "\n");
        }
        result.output = output.toString();
        
        result.exitValue = p.waitFor();

        return result;
    }
    
    /**
     * Converts the input args to something that works with Cygwin.
     * The first arg is the command and needs to be converted to a Unix path.
     * The rest aren't touched.
     */
    private List<String> getCygwinArgs(List<String> args) throws InterruptedException, IOException {
        // Shell scripts need to be run under sh since they're not executable by DOS
        List<String> newArgs = new ArrayList<String>();
        newArgs.add("sh");

        // Convert the script path to a Cygwin compatible version
        String script = args.get(0);
        ProcessBuilder pb = new ProcessBuilder("cygpath", script);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        List<String> lines = drainInputStream(p.getInputStream());
        int exitValue = p.waitFor();
        if (exitValue != 0 || lines.size() == 0) {
            throw new RuntimeException("Failed to convert '" + script + "' to Cygwin path");
        }
        newArgs.add(lines.get(0));
        
        // The rest are fine as-is
        newArgs.addAll(args.subList(1, args.size()));

        return newArgs;
    }
    
    /**
     * Returns the list of lines from the input stream.
     * It's important to drain the InputStream from a Process in
     * Windows to prevent it from blocking a process from completing.
     */
    private List<String> drainInputStream(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
}
