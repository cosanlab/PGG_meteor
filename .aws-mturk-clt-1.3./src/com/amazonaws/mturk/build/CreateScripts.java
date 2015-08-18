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

package com.amazonaws.mturk.build;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.amazonaws.mturk.util.CreateScriptUtil;

/**
 * Utility for generating convenience scripts.
 *
 */
public class CreateScripts {
    
    private final static Pattern NON_COMMANDS = Pattern.compile("^Abstract");
    
    private static List<String> getCommandNames(String sourceDir) {
        sourceDir = sourceDir + "/com/amazonaws/mturk/cmd";
        List<String> commands = new ArrayList<String>();
        File dir = new File(sourceDir);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".java") &&
                    !NON_COMMANDS.matcher(file.getName()).find())
            {
                commands.add(file.getName().replaceAll("\\.java$", ""));
            }
        }
        return commands;
    }
    
    private static String getScriptName(String command) {
        if (command.equals("GrantQualificationRequests")) {
            return "approveQualificationRequests";
        }
        else {
            return command.substring(0, 1).toLowerCase() + command.substring(1);
        }
    }
    
    private static void generateScript(String command, String scriptDir, CreateScriptUtil.ScriptType type) throws Exception {
        String scriptName = scriptDir + File.separator + getScriptName(command) + type.getExtension();
        System.out.println("Generating script: " + scriptName);
        
        Map<String, String> input = new HashMap<String, String>(2);
        input.put("${command}", command);
        input.put("\\r?\\n", type.getEol());
        
        String source = CreateScriptUtil.generateScriptSource(type, input, "com/amazonaws/mturk/build/" + type + "Script.template");
        FileWriter out = new FileWriter(scriptName);
        out.write(source);
        out.close();
    }
    
    private static void generateScripts(List<String> commands, String scriptDir, CreateScriptUtil.ScriptType type)
        throws Exception
    {
        for (String command : commands) {
            if (type == null) { // All
                for (CreateScriptUtil.ScriptType scriptType : CreateScriptUtil.ScriptType.values()) {
                    generateScript(command, scriptDir, scriptType);
                }
            }
            else {
                generateScript(command, scriptDir, type);
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: <java source directory> <script output directory>");
            System.exit(1);
        }
        String src = args[0];
        String dest = args[1];
        CreateScriptUtil.ScriptType type = (args.length > 2) ? CreateScriptUtil.ScriptType.valueOf(args[2]) : null;
        List<String> commands = getCommandNames(src);
        generateScripts(commands, dest, type);
    }

}
