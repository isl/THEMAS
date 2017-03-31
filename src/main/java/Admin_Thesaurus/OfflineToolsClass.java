/* 
 * Copyright 2015 Institute of Computer Science,
 *                Foundation for Research and Technology - Hellas.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 * 
 * =============================================================================
 * Contact: 
 * =============================================================================
 * Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
 *     Tel: +30-2810-391632
 *     Fax: +30-2810-391638
 *  E-mail: isl@ics.forth.gr
 * WebSite: http://www.ics.forth.gr/isl/cci.html
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package Admin_Thesaurus;

import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import DB_Admin.DBAdminUtilities;
import DB_Admin.FixDB;
import DB_Admin.TSVExportsImports;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SessionListener;
import Utils.SortItem;
import Utils.Utilities;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.StringObject;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class OfflineToolsClass {

    private static String shutDownDb = "ShutDownDatabase";
    private static String importXMLMode = "ImportFromXML";
    private static String exportXMLMode = "ExportToXML";
    private static String mergeMode = "MergeThesauri";
    private static String lockSystemMode = "LockSystem";
    private static String unlockSystemMode =  "UnLockSystem";
    private static String fixDbMode = "FixDB";    
    private static String importFromTsvMode = "ImportFromTSV";
    private static String exportToTsvMode = "ExportToTSV";
    

    static void printAvailableModes(){
        Utils.StaticClass.webAppSystemOutPrintln("Available Modes of OfflineToolsClass (1st argument) are:");
        Utils.StaticClass.webAppSystemOutPrintln(importXMLMode);
        Utils.StaticClass.webAppSystemOutPrintln(exportXMLMode);
        Utils.StaticClass.webAppSystemOutPrintln(mergeMode);
        Utils.StaticClass.webAppSystemOutPrintln(lockSystemMode);
        Utils.StaticClass.webAppSystemOutPrintln(unlockSystemMode);
        Utils.StaticClass.webAppSystemOutPrintln(fixDbMode);
        Utils.StaticClass.webAppSystemOutPrintln(importFromTsvMode);
        Utils.StaticClass.webAppSystemOutPrintln(exportToTsvMode);
    }
    static void printExpectedParametersAccordingToMode(String mode){
        if(mode.equals(importXMLMode)){
            
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ importXMLMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+importXMLMode);
            Utils.StaticClass.webAppSystemOutPrintln("2) ThesaurusName (No spaces just latin chars)");
            Utils.StaticClass.webAppSystemOutPrintln("3) Input XML Full File Path ");
            Utils.StaticClass.webAppSystemOutPrintln("4) Issues report xml file full path (an html will also be produeced with the same name but different extension)");            
                
            return;
        }    
        if(mode.equals(exportXMLMode)){
            
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ exportXMLMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+exportXMLMode);
            Utils.StaticClass.webAppSystemOutPrintln("2) ThesaurusName (Thesaurus must exist or use value: XXXXXX in order to export all existing thesauri)");
            Utils.StaticClass.webAppSystemOutPrintln("3) Export XML Full Folder Path ");
            
            return;
        }
        if(mode.equals(mergeMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ mergeMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+mergeMode);
            Utils.StaticClass.webAppSystemOutPrintln("2) ThesaurusName 1");
            Utils.StaticClass.webAppSystemOutPrintln("3) ThesaurusName 2");
            Utils.StaticClass.webAppSystemOutPrintln("4) MERGED ThesaurusName ");
            Utils.StaticClass.webAppSystemOutPrintln("5) Issues report xml file full path (an html will also be produeced with the same name but different extension)"); 
            return;
        }    
        if(mode.equals(lockSystemMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ lockSystemMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+lockSystemMode);
            return;
        }    
        if(mode.equals(unlockSystemMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ unlockSystemMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+unlockSystemMode);
            return;
        }    
        if(mode.equals(fixDbMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ fixDbMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+fixDbMode);
            return;
        }    
        if(mode.equals(shutDownDb)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ shutDownDb+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+shutDownDb);
            return;
        }
        if(mode.equals(importFromTsvMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ importFromTsvMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+importFromTsvMode);
            Utils.StaticClass.webAppSystemOutPrintln("2) Full path to the TSV to be loaded.");
            Utils.StaticClass.webAppSystemOutPrintln("3) Boolean value true or false that determines if the TSV contains Generic Definitions or Not.");
                    
            return;
        }    
        if(mode.equals(exportToTsvMode)){
            Utils.StaticClass.webAppSystemOutPrintln("For mode: "+ exportToTsvMode+" the expected arguments are:");
            Utils.StaticClass.webAppSystemOutPrintln("1) "+exportToTsvMode);
            Utils.StaticClass.webAppSystemOutPrintln("2) Boolean value true or false that determines if the TSV will ONLY contain Generic data or Not.");
            Utils.StaticClass.webAppSystemOutPrintln("3) Boolean value true or false that determines if the TSV will skip Generic data or Not.\r\n"
                                                   + "   If previous value was set tot true then this argument is just ignored.");
            Utils.StaticClass.webAppSystemOutPrintln("4) Full Path To TSV outPutFolder (optional variable if noe is used then TSVs folder will be selected.");
            return;
        }   
        
    }
    
    static String getFolderPathOrExit(String rawPath, boolean checkFolderExists){
        String returnVal = "";
        if(rawPath==null || rawPath.trim().length()==0){
            Utils.StaticClass.webAppSystemOutPrintln("getFolderPathOrExit found empty path");
            Utils.StaticClass.setJustSimpleSystemOut(false);
            DBAdminUtilities dbadmin = new DBAdminUtilities();
            dbadmin.UnlockSystemForAdministrativeJobs();
            
            System.exit(-1);
        }
        returnVal = rawPath.replace("\\", "/");
        if(returnVal.endsWith("/")==false){
            returnVal+="/";
        }        
        if(checkFolderExists){
            File checkFolderExistense = new File(returnVal);
            if(checkFolderExistense.exists()==false || checkFolderExistense.isDirectory()==false){
                Utils.StaticClass.webAppSystemOutPrintln("getFolderPathOrExit folder not found: " + rawPath);
                Utils.StaticClass.setJustSimpleSystemOut(false);
                DBAdminUtilities dbadmin = new DBAdminUtilities();
                dbadmin.UnlockSystemForAdministrativeJobs();
                System.exit(-1);
            }
        }
        return returnVal;
    }
    private static String GetCurrentDateAndTime() {
        Calendar rightNow = Calendar.getInstance();
        int current_year = rightNow.get(Calendar.YEAR);
        int current_month = rightNow.get(Calendar.MONTH) + 1;
        int current_day = rightNow.get(Calendar.DAY_OF_MONTH);
        int current_hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int current_minute = rightNow.get(Calendar.MINUTE);
        int current_sec = rightNow.get(Calendar.SECOND);
        // in case any value is 1 digit number => start it with zero (0)
        String current_monthStr = new Integer(current_month).toString();
        if (current_month <= 9) {
            current_monthStr = "0" + current_monthStr;
        }
        String current_dayStr = new Integer(current_day).toString();
        if (current_day <= 9) {
            current_dayStr = "0" + current_dayStr;
        }
        String current_hourStr = new Integer(current_hour).toString();
        if (current_hour <= 9) {
            current_hourStr = "0" + current_hourStr;
        }
        String current_minuteStr = new Integer(current_minute).toString();
        if (current_minute <= 9) {
            current_minuteStr = "0" + current_minuteStr;
        }
        String current_secStr = new Integer(current_sec).toString();
        if (current_sec <= 9) {
            current_secStr = "0" + current_secStr;
        }

        String str = current_year + "-" + current_monthStr + "-" + current_dayStr + "-";
        str += current_hourStr + "-" + current_minuteStr + "-" + current_secStr;

        return str;
    }
    
    public static void main(String[] args) {


        Utils.StaticClass.setJustSimpleSystemOut(true);
        Utils.StaticClass.webAppSystemOutPrintln("OfflineToolsClass called at: " + Utilities.GetNow());
        Vector<String> arguements = new Vector<String>();
        
        boolean unlockSystem = true;
        boolean oldFilterVal = ConstantParameters.filterBts_Nts_Rts;
        DBAdminUtilities dbadmin = new DBAdminUtilities();
        
        try{
            String mode = "";
            if(args!=null && args.length>=1){
                mode = args[0];
                Utils.StaticClass.webAppSystemOutPrintln("Arguement: 1\t" + args[0]);
                if(args.length>1){
                    for(int i =1; i< args.length; i++){
                       arguements.add(args[i]);
                       Utils.StaticClass.webAppSystemOutPrintln("Arguement: "+(i+1) +"\t" + args[i]);
                    }
                }
            }
            else{
                //set default mode for test
                mode = fixDbMode;
                //arguements.add("EMPTY");
                //arguements.add("TEST");
                //arguements.add("MERGED");
                //arguements.add("C:\\Users\\tzortzak\\Desktop\\import-output\\MergeLog.xml");
                //arguements.add("C:\\Users\\tzortzak\\Desktop\\import-output\\TESTIMPORT.xml");
                //arguements.add("C:\\Users\\tzortzak\\Desktop\\import-output\\importFromXMLLog.xml");
                
                //add default arguments if needed
                //arguements.add()
            }
            
            String basePath = null;
            try {
                basePath = URLDecoder.decode(OfflineToolsClass.class.getResource("OfflineToolsClass.class").getFile(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            if(basePath!=null){

                basePath = basePath.substring(0,basePath.indexOf("WEB-INF")-1).toLowerCase().replaceAll("\\\\", "/");
                if(basePath.startsWith("file:/")){
                    basePath = basePath.replaceFirst("file:/", "");
                }
                if(basePath.startsWith("/")){
                    basePath = basePath.replaceFirst("/", "");
                }

            }
            else{

                basePath = System.getProperty("user.dir").replace("WEB-INF\\classes", "").replace("WEB-INF/classes", "");

            }
            Parameters.initParams(basePath);
            //Utils.StaticClass.webAppSystemOutPrintln(basePath);

            ConfigDBadmin config = new ConfigDBadmin(Parameters.BaseRealPath);
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);

            //Parameters.initParams(System.getProperty("user.dir").replace("WEB-INF\\classes", "").replace("WEB-INF/classes", ""));
            Utils.StaticClass.webAppSystemOutPrintln("Application Base Path Set to: " + Parameters.BaseRealPath);
            String baseApplicationFilePath = Parameters.BaseRealPath;
            
            
            DBGeneral dbGen = new DBGeneral();
            UsersClass webusers = new UsersClass();
            UserInfoClass refSessionUserInfo = new UserInfoClass();
            refSessionUserInfo.selectedThesaurus = "";
            refSessionUserInfo.name = "admin";
            refSessionUserInfo.userGroup = "ADMINISTRATOR";
            Utilities u = new Utilities();
            
            

            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            
            if(mode.equals(shutDownDb)){
                Utils.StaticClass.closeDb();
                return;
            }
            
            //restart database in case it was not closed correctly
            try {
                Utils.StaticClass.getDBService();
                Thread.sleep(500);
                Utils.StaticClass.closeDb();
                Thread.sleep(500);
            } catch (Exception ex) {
                Utils.StaticClass.webAppSystemOutPrintln("Exception Caught while trying to restart neo4j database ");
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            
            if(dbadmin.isSystemLocked()){
                unlockSystem = false;
            }
            else{
                dbadmin.LockSystemForAdministrativeJobs(config);
            }
            
            // <editor-fold defaultstate="collapsed" desc="importXMLMode">	
            if(mode.equals(importXMLMode)){
                String importThesaurusName = "";
                String inputFilePath = "";
                String logFileNamePath = "";
                
                if(arguements.size()!=3){
                    printExpectedParametersAccordingToMode(mode);
                    return;
                }
                
                importThesaurusName = arguements.get(0);
                inputFilePath = arguements.get(1);
                logFileNamePath = arguements.get(2);
                

                Locale targetLocale = new Locale("el", "GR");
                String pathToErrorsXML = baseApplicationFilePath.concat("/translations/Consistencies_Error_Codes.xml");
                //String pathToSaveScriptingAndLocale = baseApplicationFilePath.concat("\\translations\\SaveAll_Locale_And_Scripting.xml");
                String pathToMessagesXML = Parameters.BaseRealPath.concat("/translations/SaveAll_Locale_And_Scripting.xml");

                
                

                
                
                DBImportData imp = new DBImportData();
        
                
                //webusers.UpdateSessionUserSessionAttribute(refSessionUserInfo,importThesaurusName);
                String initiallySelectedThesaurus="";
                

                String xmlSchemaType = ConstantParameters.xmlschematype_THEMAS;

                webusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, importThesaurusName);
                String backUpDescription = "BackUpBeforeTestImport" + Utilities.GetNow();
                StringObject DBbackupFileNameCreated = new StringObject();
                StringObject resultObj = new StringObject("");
                OutputStreamWriter logFileWriter = null;

                try {

                    String time = Utilities.GetNow();
                    String Filename = "Import_Thesaurus_" + importThesaurusName + "_" + time;
                    
                    logFileNamePath = logFileNamePath.replace("/", "\\");
                    
                    
                    Vector<String> msgArgs = new Vector<String>();
                    StringObject trMsg = new StringObject();
                    msgArgs.add(importThesaurusName);
                    msgArgs.add(time);
                    dbGen.Translate(trMsg, "root/importcopymerge/importreporttitle", msgArgs, pathToMessagesXML);


                    OutputStream fout = new FileOutputStream(logFileNamePath);
                    OutputStream bout = new BufferedOutputStream(fout);
                    logFileWriter = new OutputStreamWriter(bout, "UTF-8");
                    logFileWriter.append(ConstantParameters.xmlHeader );//+ "\r\n"


                    logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
                    logFileWriter.append("<title>"+trMsg.getValue()+"</title>\r\n");

                    logFileWriter.append("<pathToSaveScriptingAndLocale>" + pathToMessagesXML +"</pathToSaveScriptingAndLocale>\r\n");

                    if (imp.thesaurusImportActions(refSessionUserInfo, common_utils, config, targetLocale, pathToErrorsXML, inputFilePath, xmlSchemaType, importThesaurusName, backUpDescription, DBbackupFileNameCreated, resultObj, logFileWriter) == false) {
                        Utils.StaticClass.webAppSystemOutPrintln("Failure");
                        abortActions( common_utils, initiallySelectedThesaurus, importThesaurusName, DBbackupFileNameCreated, resultObj);
                        return;
                    }

                    Utils.StaticClass.closeDb();
                    commitActions(importThesaurusName, Filename.concat(".html"));

                    Utils.StaticClass.webAppSystemOutPrintln("IMPORT PROCESS FINISHED SUCCESSFULLY at time: " + Utilities.GetNow());
                    msgArgs = new Vector<String>();
                    trMsg = new StringObject();
                    msgArgs.add(importThesaurusName);
                    msgArgs.add(inputFilePath);
                    dbGen.Translate(trMsg, "root/importcopymerge/creationinfomsg", msgArgs, pathToMessagesXML);


                    logFileWriter.append("\r\n<creationInfo>"+trMsg.getValue()+"</creationInfo>\r\n");

                    if(logFileWriter!=null){
                        logFileWriter.append("</page>");
                        logFileWriter.flush();
                        logFileWriter.close();
                    }

                    //Now XSL should be found and java xsl transformation should be performed
                    String XSL = Parameters.BaseRealPath + File.separator + Parameters.Save_Results_Folder + File.separator + "ImportCopyMergeThesaurus_Report.xsl";

                    u.XmlFileTransform(logFileNamePath, XSL, logFileNamePath.replace(".xml",".html"));


                } catch (IOException ex) {
                    unlockSystem=false;
                    Utils.StaticClass.webAppSystemOutPrintln("Failure");
                    Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                return;
            }
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="exportXMLMode">
            if(mode.equals(exportXMLMode)){
                String exportThesaurusName = "";
                String exportFolderPath = "";
                //String logFileNamePath = "";
                
                if(arguements.size()!=2){
                    printExpectedParametersAccordingToMode(mode);
                    return;
                }
                exportThesaurusName = arguements.get(0);
                exportFolderPath = getFolderPathOrExit(arguements.get(1),true);
                
                //open connection and start Query
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ TestImportClass (export choice)");
                    return;
                }

                Vector<String> allthesauriNames = new Vector<String>();
                dbGen.GetExistingThesaurus(false, allthesauriNames, Q, sis_session);
                
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                if(exportThesaurusName.equals("XXXXXX")==false){
                    allthesauriNames.clear();
                    allthesauriNames.add(exportThesaurusName);
                }
                for(String exportThesarus: allthesauriNames){

                    long startTime = Utilities.startTimer();
                    DBexportData exp = new DBexportData();
                    String exportSchemaName = ConstantParameters.xmlschematype_THEMAS;
                    String time = Utilities.GetNow();
                    String exportFilePath = exportFolderPath+exportThesarus+"_"+time+".xml";
                    //String exportThesarus = importThesaurusName;
                    

                    webusers.UpdateSessionUserSessionAttribute(refSessionUserInfo,exportThesarus);
                    //StringObject resultObj = new StringObject("");
                    OutputStreamWriter logFileWriter = null;

                    try {

                        OutputStream fout = new FileOutputStream(exportFilePath);
                        OutputStream bout = new BufferedOutputStream(fout);
                        logFileWriter = new OutputStreamWriter(bout, "UTF-8");

                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile εξαγωγής δεδομένων του θησαυρού : " + exportThesarus + " στο αρχείο: " + exportFilePath + ".");


                    }
                     catch (IOException ex) {
                        unlockSystem=false;
                        Utils.StaticClass.webAppSystemOutPrintln("Failure");
                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }

                    Vector<String> thesauriNames = new Vector<String>();
                    Vector<String> allHierarchies = new Vector<String>();
                    Vector<String> allGuideTerms = new Vector<String>();

                    exp.exportThesaurusActions(refSessionUserInfo, exportThesarus, exportSchemaName, logFileWriter,thesauriNames,allHierarchies,allGuideTerms);

                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "exported in time " + Utilities.stopTimer(startTime) + " sec.");                    
                }
                return;
            }
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="mergeMode">    
            if(mode.equals(mergeMode)){
                
                String thesaurusName1 = "";
                String thesaurusName2 = "";
                String mergedThesaurusName = "";
                String logFileNamePath = "";
                
                if(arguements.size()!=4){
                    printExpectedParametersAccordingToMode(mode);
                    return;
                }
                
                thesaurusName1 = arguements.get(0);
                thesaurusName2 = arguements.get(1);
                mergedThesaurusName = arguements.get(2);                
                logFileNamePath = arguements.get(3);
                
                Locale targetLocale = new Locale("el", "GR");
                String pathToErrorsXML = baseApplicationFilePath.concat("/translations/Consistencies_Error_Codes.xml");
                String pathToMessagesXML = Parameters.BaseRealPath.concat("/translations/SaveAll_Locale_And_Scripting.xml");

                
                DBImportData dbImport = new DBImportData();
                
                //String time = Utilities.GetNow();
                //String Filename = "Merge_Thesauri_" + thesaurusName1 + "_" + thesaurusName2 + "_in_" + mergedThesaurusName + "_" + time ;
                //logFileNamePath += "/" + Filename + ".xml";

                long startTime = Utilities.startTimer();
                StringObject CreateThesaurusResultMessage = new StringObject("");
                StringObject resultObj = new StringObject();
                StringBuffer xml = new StringBuffer(); 
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αρχή διαδικασίας συγχώνευσης Θησαυρών στις: " + Utilities.GetNow());
                
                //IMPORT ACTIONS
                if(dbImport.thesaurusMergeActions(refSessionUserInfo, common_utils, config,
                        pathToErrorsXML, thesaurusName1, thesaurusName2, mergedThesaurusName,
                        targetLocale, resultObj,
                        CreateThesaurusResultMessage, xml,
                        logFileNamePath, pathToMessagesXML, startTime, null)){

                    //sucess new thesaurus is created it should be set as current
                    //UsersClass wtmsUsers = new UsersClass();
                    //wtmsUsers.AddNewThesaurusForCurrentTMSUser(WebAppUsersFileName, sessionInstance, mergedThesaurusName);
                }
                else{
                    unlockSystem=false;
                    System.out.println("Merging Failed");
                    return;
                }
                
                //Now XSL should be found and java xsl transformation should be performed
                String XSL = baseApplicationFilePath+  "/Save_Results_Displays/ImportCopyMergeThesaurus_Report.xsl";
                u.XmlFileTransform(logFileNamePath, XSL, logFileNamePath.replace(".xml", ".html"));
                
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Η διαδικασία συγχώνευσης των θησαυρών: "+
                        thesaurusName1+", " + thesaurusName2 + " στον θησαυρό "+ mergedThesaurusName
                        +" ολοκληρώθηκε με επιτυχία σε χρόνο : " + ((Utilities.stopTimer(startTime)) / 60) + " λεπτά.");
                
                
                
                
                return;
            }    
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="lockSystemMode">
            if(mode.equals(lockSystemMode)){
                unlockSystem = false;
                dbadmin.LockSystemForAdministrativeJobs(config);
            }
            
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="unlockSystemMode">
            if(mode.equals(unlockSystemMode)){
                unlockSystem = true;
                dbadmin.UnlockSystemForAdministrativeJobs();
                return;
            }    
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="fixDbMode">
            if(mode.equals(fixDbMode)){
                StringObject FixDBResultMessage_Global = new StringObject("");
                boolean dbFixed = common_utils.FixDB(true, FixDBResultMessage_Global);
                if(!dbFixed){
                    unlockSystem = false;
                    Utils.StaticClass.webAppSystemOutPrintln("Fix DB failed");
                    Utils.StaticClass.webAppSystemOutPrintln("Error Message: " + FixDBResultMessage_Global.getValue());
                }   
                Utils.StaticClass.webAppSystemOutPrintln("Fix DB finished Successfully!!!");
                return;
            }    
            // </editor-fold> 
                        
            // <editor-fold defaultstate="collapsed" desc="tsvImport">
            if(mode.equals(importFromTsvMode)){
                
                if(arguements.size()!=2){
                    printExpectedParametersAccordingToMode(mode);
                    return;
                }
                String import_export_file = arguements.get(0);
                boolean genericImport =false;
                if(arguements.get(1).toLowerCase().trim().equals("true")|| arguements.get(1).toLowerCase().trim().equals("yes")){
                    genericImport = true;
                }
                
                TSVExportsImports expimp = new TSVExportsImports();
            
                File f2 = new File(import_export_file);
                if(f2.exists()==false){
                    Utils.StaticClass.webAppSystemOutPrintln("Import file does not exist: " + import_export_file);
                    return;
                }
                boolean importCompleted = false;

                if(genericImport){
                    importCompleted = expimp.importGenericFromFile(import_export_file);
                }
                else{
                    importCompleted = expimp.importSpecificFromFile(import_export_file);
                }

                if(importCompleted==false){
                    //TODO: Translate message
                    //FixDBResultMessage.setValue("Error occured while exporting");
                    Utils.StaticClass.webAppSystemOutPrintln("Import Failed");
                    unlockSystem = false;
                    return;
                }
                Utils.StaticClass.closeDb();
                Utils.StaticClass.webAppSystemOutPrintln("Import Succedded!!!");
                return;
            }    
            // </editor-fold> 
            
            // <editor-fold defaultstate="collapsed" desc="tsvExport">
            if(mode.equals(exportToTsvMode)){
                
                if(arguements.size()!=2 && arguements.size()!=3){
                    printExpectedParametersAccordingToMode(mode);
                    return;
                }
                boolean exportOnlyGeneric =false;
                if(arguements.get(0).toLowerCase().trim().equals("true")|| arguements.get(0).toLowerCase().trim().equals("yes")){
                    exportOnlyGeneric = true;
                }
                boolean skipGeneric =false;
                if(arguements.get(1).toLowerCase().trim().equals("true")|| arguements.get(1).toLowerCase().trim().equals("yes")){
                    skipGeneric = true;
                }
                
                
                //String baseDatabaseFolderPath = getFolderPathOrExit(basePath, true);
                String dataBaseBasePath = config.GetConfigurationValue("Neo4j_DB_FOLDER_PATH");
                String dbSubPath = config.GetConfigurationValue("Neo4j_DB_PATH");
                String tsvExportFolder = "";
                if(arguements.size()==3){
                    tsvExportFolder = getFolderPathOrExit(arguements.get(2), true);
                }
                else{
                    tsvExportFolder = dataBaseBasePath + config.GetConfigurationValue("Neo4j_ExportFileDirectory");
                }
                
                TSVExportsImports expimp = new TSVExportsImports();
                
                
                File f2 = new File(dataBaseBasePath+dbSubPath);
                if(f2.exists()==false || f2.isDirectory()==false){
                    
                    Utils.StaticClass.webAppSystemOutPrintln("Database Folder path does not exist: " + dataBaseBasePath+dbSubPath);
                    return;
                }                
                File f3 = new File(tsvExportFolder);
                if(f3.exists()==false || f3.isDirectory()==false){

                    Utils.StaticClass.webAppSystemOutPrintln("Export Folder path does not exist: " + tsvExportFolder);
                    return;
                }
                
                String exportFileName =f3.getAbsolutePath()+ File.separator + "Exporter_Output_at_"+GetCurrentDateAndTime()+".tsv";
                boolean exportCompleted = expimp.globalExportToFile(exportFileName,exportOnlyGeneric,skipGeneric);//StartSISexport();
                if(exportCompleted==false){
                    //TODO: Translate message
                    //FixDBResultMessage.setValue("Error occured while exporting");
                    Utils.StaticClass.webAppSystemOutPrintln("Export Failed");
                    return;
                }
                Utils.StaticClass.webAppSystemOutPrintln("Export Succedded!");          
                return;
            }    
            // </editor-fold> 
            
            
            
            
            
        }
        catch (Exception Ex) {
            Utils.StaticClass.webAppSystemOutPrintln("Exception Caught: " + Ex.getMessage());
            Utils.StaticClass.handleException(Ex);
            unlockSystem = false;

        }
        finally{
            Utils.StaticClass.setJustSimpleSystemOut(false);
            if(unlockSystem){
                dbadmin.UnlockSystemForAdministrativeJobs();
            }
            ConstantParameters.filterBts_Nts_Rts = oldFilterVal;
        }
        

    }

    public static void commitActions(String importThesaurusName, String reportFile) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        UsersClass webappusers = new UsersClass();


        
        StringBuffer xml = new StringBuffer();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();
        Vector<String> thesauriNames = new Vector<String>();


        String importMethodChoice  = "thesaurusImport";
        String resultFileTagName = "importReportFile";
        if(importMethodChoice.compareTo("bulkImport")==0){
            resultFileTagName = "bulkImportReportFile";
        }
        

        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();            

        //open connection and start transaction
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true)==QClass.APIFail){
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ in commit actions " );
            return;
        }
        //dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies,allGuideTerms);
        //dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

        

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append("<"+resultFileTagName+">");
        xml.append(reportFile);
        xml.append("</"+resultFileTagName+">");

        String pathToMessagesXML = Utilities.getTranslationsXml("SaveAll_Locale_And_Scripting.xml");
        StringObject errorMsg = new StringObject();

        dbGen.Translate(errorMsg, "root/importcopymerge/sucessresultmsg", null, pathToMessagesXML);

        xml.append(getXMLMiddle(errorMsg.getValue(),importMethodChoice));


        //xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        //u.xslTransform(out, xml,sessionInstance.path +  "/xml-xsl/page_contents.xsl");


        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public static void abortActions(
            CommonUtilsDBadmin common_utils, String initiallySelectedThesaurus,
            String mergedThesaurusName, StringObject DBbackupFileNameCreated, StringObject resultObj) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        
        
        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();  
        
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true)==QClass.APIFail){
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ in abort actions ");
            return;
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"ABORT IMPORT");
        //abort transaction and close connection
        Q.free_all_sets();
        Q.TEST_abort_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);



        
        StringBuffer xml = new StringBuffer();
        //Vector<String> thesauriNames = new Vector<String>();
        //Vector<String> allHierarchies = new Vector<String>();
        //Vector<String> allGuideTerms = new Vector<String>();
        String importMethodChoice  = "thesaurusImport";
        StringObject result = new StringObject("");


        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+DBbackupFileNameCreated.getValue());

        boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);
        //thesauriNames.remove(mergedThesaurusName);

        if (restored) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
            //open connection and start Query
            /*
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR ");
                return;
            }

            //dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies,allGuideTerms);
            //dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

            //end query and close connection
            Q.free_all_sets();
            Q.end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
*/
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Did not manage to restore : " + DBbackupFileNameCreated.getValue());
        }

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        //xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies,allGuideTerms,targetLocale));
        String pathToMessagesXML = Utilities.getTranslationsXml("SaveAll_Locale_And_Scripting.xml");
        Vector<String> errorArgs = new Vector<String>();
        StringObject errorMsg = new StringObject();
        errorArgs.add(resultObj.getValue());
        dbGen.Translate(errorMsg, "root/importcopymerge/abortresultmsg", errorArgs, pathToMessagesXML);
        xml.append(getXMLMiddle(errorMsg.getValue(),importMethodChoice));
        //xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        //u.xslTransform(out, xml,Parameters.BaseRealPath + "\\xml-xsl\\page_contents.xsl");

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public static String getXMLMiddle(String importThesaurusMessage, String thesaurusOrBulkMode) {
        // get the active sessions
        String resultTagName = "importThesaurusMessage";
        if(thesaurusOrBulkMode.compareTo("bulkImport")==0){
            resultTagName = "bulkImportThesaurusMessage";
        }

        //int OtherActiveSessionsNO = SessionListener.activesessionsNO - 1;

        String XMLMiddleStr = "<content_Admin_Thesaurus>";


        XMLMiddleStr += "<CurrentShownDIV>ImportExport_Data_DIV</CurrentShownDIV>";


        //XMLMiddleStr += "<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>";
        // write the existing Thesaurus in DB
        /*
        int thesaurusVectorCount = thesaurusVector.size();
        XMLMiddleStr += "<existingThesaurus>";
        for (int i = 0; i < thesaurusVectorCount; i++) {
            XMLMiddleStr += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
        }
        XMLMiddleStr += "</existingThesaurus>";
*/
        XMLMiddleStr += "<"+resultTagName+">" + importThesaurusMessage + "</"+ resultTagName+">";
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }
}
