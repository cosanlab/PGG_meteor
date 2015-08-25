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


package com.amazonaws.mturk.cmd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

import com.amazonaws.mturk.util.CreateScriptUtil;

public class MakeTemplate extends AbstractCmd {
  private final static String ARG_TEMPLATE = "template"; 
  private final static String ARG_TARGET = "target";
  private final static String ARG_TYPE = "type";
  private final static String ARG_OS = "os";
  private final static String ARG_TEMPLATE_ROOT_DIR = "templateRootDir";
  private final static String ARG_TARGET_ROOT_DIR = "targetRootDir";
  private final static String ARG_SCRIPT_TEMPLATE_DIR = "scriptTemplateDir";
  
  public enum TemplateType {
    Hit,
    Qual
  }
  
  public static void main(String[] args) {
    MakeTemplate lh = new MakeTemplate();
    lh.run(args);
  }

  protected void initOptions() {
    opt.addOption(ARG_TEMPLATE, true,
    "(required) The name of one of the sample directories (eg. helloworld).");
    opt.addOption(ARG_TARGET, true,
    "(required) The name of the target directory (eg. new_application)");
    opt.addOption(ARG_TYPE, true,
    "(required) The type of the template (eg. hit)");
    opt.addOption(ARG_OS, true,
    "(required) The type of the operating system (eg. dos)");
    opt.addOption(ARG_TEMPLATE_ROOT_DIR, true,
    "(required) The relative path to the root of the template directory (eg. ../samples)");
    opt.addOption(ARG_TARGET_ROOT_DIR, true,
    "(required) The relative path to the root of the target directory (eg. ../hits)");
    opt.addOption(ARG_SCRIPT_TEMPLATE_DIR, true,
    "(required) The relative path to the script directory (eg. ../etc/templates/hits)");
  }

  protected void printHelp() {   
    StringBuffer templTypesStr = new StringBuffer("");
    TemplateType[] templTypes = TemplateType.values();
    for(int i=0; i<templTypes.length; i++) {
      TemplateType type = templTypes[i];
      
      if (i > 0)
        templTypesStr.append(", ");

      templTypesStr.append(type);
    }

    StringBuffer scriptTypesStr = new StringBuffer("");   
    CreateScriptUtil.ScriptType[] scriptTypes = CreateScriptUtil.ScriptType.values();
    for(int i=0; i<scriptTypes.length; i++) {
      CreateScriptUtil.ScriptType type = scriptTypes[i];
      
      if (i > 0)
        scriptTypesStr.append(", ");

      scriptTypesStr.append(type);
    }
    
    formatter.printHelp(MakeTemplate.class.getName()
        + " -" + ARG_TEMPLATE + " [template name]"
        + " -" + ARG_TARGET + " [target directory]"
        + " -" + ARG_TYPE + " [" + templTypesStr + "]"
        + " -" + ARG_OS + " [" + scriptTypesStr + "]"
        + " -" + ARG_TEMPLATE_ROOT_DIR + " [root of the template directory]"
        + " -" + ARG_TARGET_ROOT_DIR + " [root of the target directory]"
        + " -" + ARG_SCRIPT_TEMPLATE_DIR + " [script template directory]", opt);
  }
  protected void runCommand(CommandLine cmdLine) throws Exception {

    if (!cmdLine.hasOption(ARG_TEMPLATE)) {
      log.error("Missing: -" + ARG_TEMPLATE);
      printHelp();
      System.exit(-1);
    } else if (!cmdLine.hasOption(ARG_TARGET)) {
      log.error("Missing: -" + ARG_TARGET); 
      printHelp();
      System.exit(-1); 
    } else if (!cmdLine.hasOption(ARG_TYPE)) {
      log.error("Missing: -" + ARG_TYPE); 
      printHelp();
      System.exit(-1); 
    } else if (!cmdLine.hasOption(ARG_OS)) {
      log.error("Missing: -" + ARG_OS); 
      printHelp();
      System.exit(-1); 
    } else if (!cmdLine.hasOption(ARG_TEMPLATE_ROOT_DIR)) {
      log.error("Missing: -" + ARG_TEMPLATE_ROOT_DIR); 
      printHelp();
      System.exit(-1); 
    } else if (!cmdLine.hasOption(ARG_TARGET_ROOT_DIR)) {
      log.error("Missing: -" + ARG_TARGET_ROOT_DIR); 
      printHelp();
      System.exit(-1); 
    } else if (!cmdLine.hasOption(ARG_SCRIPT_TEMPLATE_DIR)) {
      log.error("Missing: -" + ARG_SCRIPT_TEMPLATE_DIR); 
      printHelp();
      System.exit(-1); 
    }

    try {
      makeTemplate(cmdLine.getOptionValue(ARG_TEMPLATE),
          cmdLine.getOptionValue(ARG_TARGET),
          cmdLine.getOptionValue(ARG_TYPE),
          cmdLine.getOptionValue(ARG_OS),
          cmdLine.getOptionValue(ARG_TEMPLATE_ROOT_DIR),
          cmdLine.getOptionValue(ARG_TARGET_ROOT_DIR),
          cmdLine.getOptionValue(ARG_SCRIPT_TEMPLATE_DIR));
    } catch (Exception e) {
      log.error("Error making a template: " + e.getLocalizedMessage(), e);
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  private void makeTemplate(String template, String target, String type, String os,
      String templateRootDirPath, String targetRootDirPath, String scriptTemplateDir) throws Exception {

    String targetDirPath = targetRootDirPath + File.separator + target;
    File targetDir = new File(targetDirPath);
    
    // the target directory should not exist already
    if (targetDir.exists()) {
      throw new Exception("A file or directory named " + target + " already exists.");
    }

    String templateDirPath = templateRootDirPath + File.separator + template;
    
    // the template directory should exist already
    File templateDir = new File(templateDirPath);
    if (!templateDir.exists() || !templateDir.canRead() || templateDir.isFile()) {
      throw new Exception("Could not read from the directory " + template);
    }
    
    try {
      // create the target directory
      if (!targetDir.mkdir())
        throw new Exception("Could not create the directory " + target);
      
      TemplateType templType = TemplateType.valueOf(type);
      CreateScriptUtil.ScriptType scriptType = CreateScriptUtil.ScriptType.valueOf(os);
      
      String templateFileRoot = templateDirPath + File.separator + ((templType == TemplateType.Hit) ? template : "qualification");
      
      String targetFileName = targetDirPath + File.separator + ((templType == TemplateType.Hit) ? target : "qualification");
      
      // copy the resource files from the template directory to the target directory
      copyTemplateFile(templateFileRoot, targetFileName, ".question");
      copyTemplateFile(templateFileRoot, targetFileName, ".properties");
      
      switch (templType) {
        case Hit:
          copyTemplateFile(templateFileRoot, targetFileName, ".input");
          
          // generate the scripts for HIT operations
          generateScript(scriptTemplateDir, target, targetDirPath, "run", scriptType);
          generateScript(scriptTemplateDir, target, targetDirPath, "getResults", scriptType);
          generateScript(scriptTemplateDir, target, targetDirPath, "approveAndDeleteResults", scriptType); break;
        case Qual:
          copyTemplateFile(templateFileRoot, targetFileName, ".answer");
          
          // generate the scripts for Qual operations
          generateScript(scriptTemplateDir, target, targetDirPath, "createQualification", scriptType);
          generateScript(scriptTemplateDir, target, targetDirPath, "updateQualification", scriptType);
          generateScript(scriptTemplateDir, target, targetDirPath, "deactivateQualification", scriptType); break;
        default:
          throw new Exception("Unrecognized TemplateType: " + type);
      }     
    } catch (Exception e) {
      if (targetDir.exists())
        targetDir.delete(); // clean up the directory created
      
      throw e;
    }
  }
  
  private void copyTemplateFile(String sourceRoot, String targetRoot, String extension) throws Exception {
    String inputFileName = sourceRoot + extension;
    String outputFileName = targetRoot + extension;
    
    System.out.println("Copying resource file: " + outputFileName);
    
    File inputFile = new File(inputFileName);
    if (!inputFile.exists() || !inputFile.canRead()) {
      throw new Exception("Could not read from the file " + inputFileName);
    }

    File outputFile = new File(outputFileName);
    if (!outputFile.exists()) {
      if (!outputFile.createNewFile() || !outputFile.canWrite())
        throw new Exception("Could not write to the file " + outputFileName);
    }
    
    // copy file
    FileReader in = new FileReader(inputFile);
    FileWriter out = new FileWriter(outputFile);
    
    try {
      char[] buffer = new char[1024];
      int nread = 0;
      while ((nread = in.read(buffer)) != -1) {
          out.write(buffer, 0, nread);
      }
    }
    finally {
      in.close();
      out.close();
    }
  }
  
  private static void generateScript(String scriptTemplateDir, String target, 
      String targetDirPath, String command, CreateScriptUtil.ScriptType type) throws Exception {
    
    String scriptName = targetDirPath + File.separator + command + type.getExtension();

    System.out.println("Generating script: " + scriptName);
    
    Map<String, String> input = new HashMap<String, String>(1);
    input.put("${target}", target);
    
    String templateFileName = scriptTemplateDir + File.separator + command + "." + type + ".template";
    String source = CreateScriptUtil.generateScriptSource(type, input, templateFileName.toString());
    
    FileWriter out = new FileWriter(scriptName);
    out.write(source);
    out.close();
  }
}
