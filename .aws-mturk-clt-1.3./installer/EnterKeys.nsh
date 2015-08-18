/*
 * Copyright 2012 Amazon Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */ 
 
/*
 * EnterKeysPage related functionality
 */
 
!ifndef _EnterKeys_nsh
!define _EnterKeys_nsh

!include nsDialogs.nsh
!include LogicLib.nsh
!include WinMessages.nsh
!include TextReplace.nsh
!include StrFunc.nsh

# Initialize/define String Functions
${StrLoc} 
${StrTok}

# Variables
Var MTurkPropertiesFilePath
Var OldPropertiesFilePath
Var FileAccessKey
Var FileSecretKey
Var LinkFont
Var EnterKeysDialog
Var TopLabel
Var Num1Label
Var Num2Label
Var Num3Label
Var NewRequesterLabel
Var RegisterHereLink
Var NewToAWSLabel
Var CreateAWSAccountLink
Var ViewKeysLink
Var AccessKeyLabel
Var AccessKeyText
Var SecretKeyLabel
Var SecretKeyText

# Custom Page Strings
LangString ENTER_KEYS_PAGE_TITLE ${LANG_ENGLISH} "Enter Credentials"
LangString ENTER_KEYS_PAGE_SUBTITLE ${LANG_ENGLISH} "Please enter your Amazon Web Services access key and secret key.  These are your user name and password for using the Mechanical Turk web service."

Function EnterKeysPage
  !insertmacro MUI_HEADER_TEXT $(ENTER_KEYS_PAGE_TITLE) $(ENTER_KEYS_PAGE_SUBTITLE)

  Call ReadExistingKeys      
  
  nsDialogs::Create /NOUNLOAD 1018
  Pop $EnterKeysDialog

  ${If} $EnterKeysDialog == error
      Abort
  ${EndIf}

  ${NSD_CreateLabel} 30u 8u -10u 12u "Don't know your access key and secret keys?  Follow these steps:"
  Pop $TopLabel

  ${NSD_CreateLabel} 30u 24u 10u 12u "1."
  Pop $Num1Label

  ${NSD_CreateLabel} 40u 24u 55u 12u "New Requester?"
  Pop $NewRequesterLabel
  ${NSD_CreateLink} 96u 24u -10u 12u "register for a new Requester account"
  Pop $RegisterHereLink
  ${NSD_OnClick} $RegisterHereLink RegisterHereLink_OnClick

  ${NSD_CreateLabel} 30u 40u 10u 12u "2."
  Pop $Num2Label

  ${NSD_CreateLabel} 40u 40u 49u 12u "New to AWS?"
  Pop $NewToAWSLabel

  ${NSD_CreateLink} 89u 40u -10u 12u "create an AWS account"
  Pop $CreateAWSAccountLink
  ${NSD_OnClick} $CreateAWSAccountLink CreateAWSAccountLink_OnClick

  ${NSD_CreateLabel} 30u 56u 10u 12u "3."
  Pop $Num3Label

  ${NSD_CreateLink} 40u 56u -10u 12u "View your AWS keys here"
  Pop $ViewKeysLink
  ${NSD_OnClick} $ViewKeysLink ViewKeysLink_OnClick

  ${NSD_CreateLabel} 55u 86u 40u 12u "Access Key"
  Pop $AccessKeyLabel

  ${If} $FileAccessKey == ""
  ${OrIf} $FileAccessKey == "[insert your access key here]"
    ${NSD_CreateText} 96u 84u 150u 13u "Enter your access key here"
  ${Else}
    ${NSD_CreateText} 96u 84u 150u 13u $FileAccessKey
  ${EndIf}
  Pop $AccessKeyText

  ${NSD_CreateLabel} 55u 112u 40u 12u "Secret Key"
  Pop $SecretKeyLabel

  ${If} $FileSecretKey == ""
  ${OrIf} $FileSecretKey == "[insert your secret key here]"
    ${NSD_CreateText} 96u 110u 150u 13u "Enter your secret key here"
  ${Else}
    ${NSD_CreateText} 96u 110u 150u 13u $FileSecretKey
  ${EndIf}
  Pop $SecretKeyText
  
  # Create Link Font
  CreateFont $LinkFont "$(^Font)" "$(^FontSize)" "" /UNDERLINE
  # Set links to use underline font
  SendMessage $RegisterHereLink ${WM_SETFONT} $LinkFont 1
  SendMessage $CreateAWSAccountLink ${WM_SETFONT} $LinkFont 1
  SendMessage $ViewKeysLink ${WM_SETFONT} $LinkFont 1
  
  ${NSD_SetFocus} $AccessKeyText
  
  nsDialogs::Show
  
FunctionEnd

Function CopyOldPropertiesFile

  # Look for previous version of the CLT
  # If found, offer to copy over the old keys

  StrCpy $OldPropertiesFilePath ""
  StrCpy $MTurkPropertiesFilePath $INSTDIR\bin\mturk.properties
 
  # Check if MTURK_CMD_HOME points to a previous version
  ReadEnvStr $0 MTURK_CMD_HOME
  #MessageBox MB_OK "MTURK_CMD_HOME=$0$\nINSTDIR=$INSTDIR"
  ${If} $0 != $INSTDIR
    StrCpy $1 $0\bin\mturk.properties
    ${If} ${FileExists} $1
      StrCpy $OldPropertiesFilePath $1
    ${EndIf}
  ${EndIf}
  
  # Check for reinstall into a different dir
  ${If} $INSTDIR != "C:\mech-turk-tools-${VERSION}"
    StrCpy $1 "C:\aws-mturk-clt-${VERSION}\bin\mturk.properties"
    ${If} $OldPropertiesFilePath == ""
    ${AndIf} ${FileExists} $1
      StrCpy $OldPropertiesFilePath $1
    ${EndIf}
  ${EndIf}
  
  # Check likely previous install dirs
  # Note: We don't need to add new version to this list as
  #       anything after 1.2.1 should have MTURK_CMD_HOME set
  #       and will be handled by the first case above
  StrCpy $1 "C:\aws-mturk-clt-1.2.1\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt-1.2.0\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt-1.2\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt-1.1.0\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt-1.1\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt-1.0\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\aws-mturk-clt\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\mech-turk-tools\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\mturk-clt\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}
  StrCpy $1 "C:\MTurkCLT\bin\mturk.properties"
  ${If} $OldPropertiesFilePath == ""
  ${AndIf} ${FileExists} $1
    StrCpy $OldPropertiesFilePath $1
  ${EndIf}

  # If we found a previous version
  ${If} $OldPropertiesFilePath != ""
  ${AndIf} $OldPropertiesFilePath != "$INSTDIR\bin\mturk.properties"
  
    # Get directory name of previous install without bin\mturk.properties
    StrLen $1 $OldPropertiesFilePath
    IntOp $2 $1 - 21
    StrCpy $3 $OldPropertiesFilePath $2 0
    MessageBox MB_YESNO \
               "You have a previous version of the tools installed in $3 $\nWould you like to copy over your access keys from this previous installation?" \
               IDNO SkipPropertiesFileCopy

    StrCpy $MTurkPropertiesFilePath $OldPropertiesFilePath
    
    SkipPropertiesFileCopy:    
  ${EndIf}

FunctionEnd

Function ReadExistingKeys
  # Read in the access key and secret key from existing mturk.properties file

  ${If} $MTurkPropertiesFilePath == ""
    StrCpy $MTurkPropertiesFilePath $INSTDIR\bin\mturk.properties
  ${EndIf}
  
  ClearErrors
  FileOpen $0 "$MTurkPropertiesFilePath" "r"
  ${Unless} ${Errors}
    # Loop through each line in the file
    ${Do}
        # Read a line
        FileRead $0 $1
        
        # Check for EOF
        ${If} $1 == ""
          ${ExitDo}
        ${EndIf}
        
        # Ignore comments
        ${StrLoc} $2 $1 "#" ">"
        ${If} $2 < 1
          # Find property name
          ${StrTok} $3 $1 " =" "0" "1"
          
          ${If} $3 == "access_key"
            # Take everything past the equals sign
            ${StrLoc} $4 $1 "=" ">"
            IntOp $4 $4 + 1
            StrCpy $FileAccessKey $1 "" $4
            
            # Trim leading and trailing spaces
            Push $FileAccessKey
            Call Trim
            Pop $FileAccessKey
          ${EndIf}
          ${If} $3 == "secret_key"
            # Take everything past the equals sign
            ${StrLoc} $4 $1 "=" ">"
            IntOp $4 $4 + 1
            StrCpy $FileSecretKey $1 "" $4

            # Trim leading and trailing spaces
            Push $FileSecretKey
            Call Trim
            Pop $FileSecretKey
          ${EndIf}
        ${EndIf}
    ${Loop}

    # Close the file
    FileClose $0
    
  ${EndUnless}
    
FunctionEnd

Function EnterKeysPageLeave

  # Get keys from the dialog
  ${NSD_GetText} $AccessKeyText $0
  ${NSD_GetText} $SecretKeyText $1

  # Trim leading and trailing spaces
  Push $0
  Call Trim
  Pop $0
  Push $1
  Call Trim
  Pop $1
    
  # Verify length of AccessKey
  # Example is 20 chars long
  StrLen $2 $0
  ${If} $2 < 20 
      MessageBox MB_OK|MB_ICONEXCLAMATION "The access key you entered does not appear to be the correct length.$\nPlease enter it again."
      Abort
  ${EndIf}
  ${If} $2 > 22 
      MessageBox MB_OK|MB_ICONEXCLAMATION "The access key you entered does not appear to be the correct length.$\nPlease enter it again."
      Abort
  ${EndIf}

  # Verify length of SecretKey
  # Example is 40 chars long
  StrLen $3 $1
  ${If} $3 < 30 
      MessageBox MB_OK|MB_ICONEXCLAMATION "The secret key you entered does not appear to be the correct length.$\nPlease enter it again."
      Abort
  ${EndIf}
  ${If} $3 > 50 
      MessageBox MB_OK|MB_ICONEXCLAMATION "The secret key you entered does not appear to be the correct length.$\nPlease enter it again."
      Abort
  ${EndIf}

  #MessageBox MB_OK "FileAccessKey=$FileAccessKey$\nFileSecretKey=$FileSecretKey"
  
  # Write the access key to the mturk.properties file
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\naccess_key=$FileAccessKey" "$\naccess_key=$0" "" $6
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\naccess_key = $FileAccessKey" "$\naccess_key=$0" "" $4
  IntOp $6 $6 + $4
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\naccess_key= $FileAccessKey" "$\naccess_key=$0" "" $4
  IntOp $6 $6 + $4
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\naccess_key =$FileAccessKey" "$\naccess_key=$0" "" $4
  IntOp $6 $6 + $4
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "[insert your access key here]" $0 "" $4
  IntOp $6 $6 + $4
  
  # Write the secret key to the mturk.properties file
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\nsecret_key=$FileSecretKey" "$\nsecret_key=$1" "" $7
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\nsecret_key = $FileSecretKey" "$\nsecret_key=$1" "" $5
  IntOp $7 $7 + $5
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\nsecret_key= $FileSecretKey" "$\nsecret_key=$1" "" $5
  IntOp $7 $7 + $5
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "$\nsecret_key =$FileSecretKey" "$\nsecret_key=$1" "" $5
  IntOp $7 $7 + $5
  ${textreplace::ReplaceInFile} $INSTDIR\bin\mturk.properties $INSTDIR\bin\mturk.properties "[insert your secret key here]" $1 "" $5
  IntOp $7 $7 + $5

  ${If} $6 < 1
  ${OrIf} $7 < 1
      MessageBox MB_OK|MB_ICONEXCLAMATION "Unable to write access key and secret key to mturk.properties file.$\nYou will need to manually update the file at $INSTDIR\bin\mturk.properties"
  ${EndIf}
FunctionEnd

Function RegisterHereLink_OnClick
  Pop $1
  # Launch broswer to requester.mturk.com signin page
  ExecShell "" "http://requester.mturk.com/mturk/beginsignin"
FunctionEnd

Function CreateAWSAccountLink_OnClick
  Pop $1 
  # Launch broswer to AWS resource center
  ExecShell "" "https://aws-portal.amazon.com/gp/aws/developer/account/index.html/105-9852631-3500420?ie=UTF8&action=access-key"
FunctionEnd

Function ViewKeysLink_OnClick
  Pop $1 
  # Launch broswer to AWS resource center
  ExecShell "" "https://aws-portal.amazon.com/gp/aws/developer/account/index.html/105-9852631-3500420?ie=UTF8&action=access-key"
FunctionEnd


# Utility functions
Function isEmptyDir
  # Stack ->                    # Stack: <directory>
  Exch $0                       # Stack: $0
  Push $1                       # Stack: $1, $0
  FindFirst $0 $1 "$0\*.*"
  strcmp $1 "." 0 _notempty
    FindNext $0 $1
    strcmp $1 ".." 0 _notempty
      ClearErrors
      FindNext $0 $1
      IfErrors 0 _notempty
        FindClose $0
        Pop $1                  # Stack: $0
        StrCpy $0 1
        Exch $0                 # Stack: 1 (true)
        goto _end
     _notempty:
       FindClose $0
       Pop $1                   # Stack: $0
       StrCpy $0 0
       Exch $0                  # Stack: 0 (false)
  _end:
FunctionEnd

; Trim
;   Removes leading & trailing whitespace from a string
; Usage:
;   Push 
;   Call Trim
;   Pop 
Function Trim
    Exch $R1 ; Original string
    Push $R2
 
Loop:
    StrCpy $R2 "$R1" 1
    StrCmp "$R2" " " TrimLeft
    StrCmp "$R2" "$\r" TrimLeft
    StrCmp "$R2" "$\n" TrimLeft
    StrCmp "$R2" "$\t" TrimLeft
    GoTo Loop2
TrimLeft:   
    StrCpy $R1 "$R1" "" 1
    Goto Loop
 
Loop2:
    StrCpy $R2 "$R1" 1 -1
    StrCmp "$R2" " " TrimRight
    StrCmp "$R2" "$\r" TrimRight
    StrCmp "$R2" "$\n" TrimRight
    StrCmp "$R2" "$\t" TrimRight
    GoTo Done
TrimRight:  
    StrCpy $R1 "$R1" -1
    Goto Loop2
 
Done:
    Pop $R2
    Exch $R1
FunctionEnd
!endif ; _EnterKeys_nsh
