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
package DB_Admin;

import Admin_Thesaurus.DBMergeThesauri;
import Admin_Thesaurus.DBexportData;
import Admin_Thesaurus.DBImportData;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import XMLHandling.WriteFileData;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Properties;
//import javax.mail.*;
//import javax.mail.internet.*;

import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import org.neo4j.io.fs.FileUtils;

/**
 *
 * @author tzortzak
 */
public class ScheduledBackups extends TimerTask {

    private DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private DateFormat FolderDateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private ConfigDBadmin config;
    private DBAdminUtilities dbAdminUtils;
    private CommonUtilsDBadmin common_utils;
    
    private SessionWrapperClass sessionInstance; 
    private UserInfoClass SessionUserInfo;
    private Locale targetLocale;
    private final String XML_ExportPrefix = "Export_Thesaurus_";
    private final String XML_ImportPrefix = "Import_Thesaurus_";
    private final String db_Backup_MainFolder ;
    private final String db_BackUp_SubFolderName = "Maintenance_BackUps";
    private String pathToErrorsXML;
    private ServletContext context;
    private String MaintananceStatusPath;

    public ScheduledBackups(HttpServletRequest request, SessionWrapperClass sessionInst, String ErrorsXML, String language, String country) {
        super();
        String basePath = request.getSession().getServletContext().getRealPath("");
        config = new ConfigDBadmin(basePath);
        dbAdminUtils = new DBAdminUtilities();
        common_utils = new CommonUtilsDBadmin(config);
        context = request.getSession().getServletContext();
        sessionInstance = new SessionWrapperClass(sessionInst);        
        SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        targetLocale = new Locale(language, country);
        pathToErrorsXML = ErrorsXML;
        MaintananceStatusPath = basePath + File.separator + "MonitorAutomaticBackups";
        db_Backup_MainFolder = common_utils.DB_BackupFolder.getAbsolutePath();
        Utils.StaticClass.webAppSystemOutPrintln("ScheduledBackups Constructor called for " + sessionInstance.path);
    }

    private void setDBBackupSubFolder(){
        
        
        common_utils.DB_BackupFolder = new File(db_Backup_MainFolder + File.separator + db_BackUp_SubFolderName);
        try {
            FileUtils.deleteRecursively(common_utils.DB_BackupFolder);
        } catch (IOException ex) {
            Logger.getLogger(ScheduledBackups.class.getName()).log(Level.SEVERE, null, ex);
        }
        common_utils.DB_BackupFolder.mkdir(); 
    }
    
    private void resetDBBackupFolder(){
        common_utils.DB_BackupFolder = new File(db_Backup_MainFolder);
        common_utils.DB_BackupFolder.mkdir(); 
    }

    public void performScheduledBackupActions(String backupDescription){
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Backups Starting on:" + formatter.format(new Date()));
        
        dbAdminUtils.LockSystemForAdministrativeJobs(config);
        Parameters.initParams(context);
        boolean maintenanceSucceded = true;
        StringObject resultObj = new StringObject("");
        StringObject DBbackupFileNameCreated = new StringObject("");
        Utilities u = new Utilities();

        String NewRestoreBackupTxtFilePath = MaintananceStatusPath + File.separator + "RestorationNeeded.txt";
        File MonitorMaintananceFile = new File(NewRestoreBackupTxtFilePath);
        try{
            //Data Storage
            Vector<String> thesaurusVector = new Vector<String>();
            StringObject CreateDBbackupResultMessage = new StringObject("");


            String testName = FolderDateformatter.format(new Date());
            testName = testName.replaceAll(":", "-");
            testName = testName.replaceAll(" ", "_");


            
            
            maintenanceSucceded = common_utils.CreateDBbackup(backupDescription, CreateDBbackupResultMessage, DBbackupFileNameCreated);
            if (maintenanceSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Create System BackUp Failed on:" + formatter.format(new Date()) + ". ErrorMsg: " + CreateDBbackupResultMessage.getValue());
                return;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Create System BackUp Completed Successfully on:" + formatter.format(new Date()));

            u.CheckMaintenanceCompleted(config, NewRestoreBackupTxtFilePath, Parameters.LogFilePrefix);

            OutputStream fout = new FileOutputStream(MonitorMaintananceFile);
            OutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            logFileWriter.append(DBbackupFileNameCreated.getValue());
            logFileWriter.close();

            setDBBackupSubFolder();

            maintenanceSucceded = exportAllTHesauri(thesaurusVector, testName);
            if (maintenanceSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Export Failed on:" + formatter.format(new Date()) + ".");
                return;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Export Completed Successfully on:" + formatter.format(new Date()));


            maintenanceSucceded = initalizeDB();
            if (maintenanceSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Initialize DB Failed on:" + formatter.format(new Date()) + ".");
                return;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Initialize DB Completed Successfully on:" + formatter.format(new Date()));


            // boolean DBInitializationSucceded = dbAdminUtils.InitializeDB(common_utils, resultObj);
            // get the modification date of db folder contents before telos parsing
            long DBmodificationBeforeImports = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);

            maintenanceSucceded = importAllThesauri(thesaurusVector, testName);
            if (maintenanceSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Import Thesauri Failed on:" + formatter.format(new Date()) + ".");
                return;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Import Thesauri Completed Successfully on:" + formatter.format(new Date()));

            long DBmodificationafterImports = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);

            //check if db folder is modified at all
            if(DBmodificationBeforeImports==DBmodificationafterImports){
                Utils.StaticClass.webAppSystemOutPrintln("No MODIFICATION");
                maintenanceSucceded =false;
                return;
            }

            resetDBBackupFolder();

            //unlock System only in case no error has occured
            if(maintenanceSucceded){
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Maintenance Completed Sucessfully on:" + formatter.format(new Date()) + ".");
                // check modification date of db folder afer imports succeded
                MonitorMaintananceFile.delete();
                dbAdminUtils.UnlockSystemForAdministrativeJobs();
            }

        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Maintenance Throwed Exception on:" + formatter.format(new Date()) + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exception Message:" + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            //sendmail();
            resetDBBackupFolder();
            boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), resultObj);
            if (restored) {
                MonitorMaintananceFile.delete();
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
            } else {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Did not manage to restore : " + DBbackupFileNameCreated.getValue() + ". Result:" + resultObj.getValue());
            }
            cancel();
            //Parameters.timer.cancel();
            Parameters.ENABLE_AUTOMATIC_BACKUPS = true;
        }
        finally{
            if (maintenanceSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Maintenance Failed on:" + formatter.format(new Date()) + ".");
                //sendmail();
                resetDBBackupFolder();
                boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), resultObj);
                if (restored) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
                } else {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Did not manage to restore : " + DBbackupFileNameCreated.getValue() + ". Result:" + resultObj.getValue());
                }
                cancel();
                //Parameters.timer.cancel();
                Parameters.ENABLE_AUTOMATIC_BACKUPS = true;

            }

        }

    }
    /** 
     * This method is the implementation of a contract defined in the TimerTask 
     * class. This in the entry point of the task execution. 
     */
    public void run() {
        if(Parameters.SkipAutomaticBackups){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Skipping Automatic Backups on:" + formatter.format(new Date()));
            return;
        }
        this.performScheduledBackupActions(Parameters.AUTOMATIC_BACKUPS_DESCRIPTION);
    }

    private boolean exportAllTHesauri(Vector<String> thesaurusVector, String testName) {

        DBGeneral dbGen = new DBGeneral();
        UsersClass WTMSUsers = new UsersClass();
        DBexportData dbExport = new DBexportData();
        Utilities u = new Utilities();



        File currentDateFolder = new File(common_utils.XML_BackupFolder, testName);
        currentDateFolder.mkdir();

        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();



        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class ScheduledBackups exportAllTHesauri()");
            return false;
        }
        dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);
        int ret = Q.free_all_sets();
        if (ret == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to free_all_sets");
            return false;
        }

        ret = Q.reset_name_scope();
        if (ret == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to reset_name_scope");
            return false;
        }


        
        Q.free_all_sets();
        Q.reset_name_scope();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session,null, false);

        //Start Exporting each thesaurus
        boolean exportFailure = false;
        for (int i = 0; i < thesaurusVector.size(); i++) {

            //Creating filename and initializing each thesaurus export file
            String exprortThesaurus = thesaurusVector.get(i);
            String Filename = XML_ExportPrefix + exprortThesaurus + ".xml";
            String logFileNamePath = currentDateFolder.getPath() + File.separator + Filename;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Attempting export " + exprortThesaurus + " in file " + logFileNamePath);
            OutputStreamWriter logFileWriter = null;
            OutputStream fout = null;
            OutputStream bout = null;
            try {
                fout = new FileOutputStream(logFileNamePath);

                bout = new BufferedOutputStream(fout);
                logFileWriter = new OutputStreamWriter(bout, "UTF-8");
                //logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"
                //logFileWriter.append("<data ofThes=\"" + exprortThesaurus + "\"  >\r\n");

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "LogFile of data export of thesaurus: " + exprortThesaurus + " in file: " + logFileNamePath);

            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + logFileNamePath + " " + exc.getMessage());
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to export thesaurus " + exprortThesaurus);
                exportFailure = true;
                Utils.StaticClass.handleException(exc);
                cancel();
                //Parameters.timer.cancel();
                Parameters.ENABLE_AUTOMATIC_BACKUPS = true;
                break;
            }

            //set thesauric values to Parameters
            WTMSUsers.SetSessionAttributeSessionUser(sessionInstance,context, SessionUserInfo.name, SessionUserInfo.password, exprortThesaurus, SessionUserInfo.userGroup);

            //exporting Data

            Vector<String> thesauriNames = new Vector<String>();
            Vector<String> allHierarchies = new Vector<String>();
            Vector<String> allGuideTerms = new Vector<String>();

            dbExport.exportThesaurusActions(SessionUserInfo, exprortThesaurus, ConstantParameters.xmlschematype_THEMAS, 
                    logFileWriter,thesauriNames,allHierarchies,allGuideTerms);
            //dbExport.FOR_DELETE_exportTranslationCategories(sessionInstance,ConstantParameters.xmlschematype_THEMAS, Q,TA, sis_session, logFileWriter, targetLocale);
            //dbExport.FOR_DELETE_exportFacetsAndHierarchies(sessionInstance,ConstantParameters.xmlschematype_THEMAS, Q, sis_session, logFileWriter, targetLocale);
            //dbExport.FOR_DELETE_exportTerms(sessionInstance,ConstantParameters.xmlschematype_THEMAS, Q, TA, sis_session, logFileWriter, targetLocale);
            //if(Parameters.FormatXML){

        
            WriteFileData.formatXMLFile(logFileNamePath);
        


            
        }





        //check if export exception occured
        if (exportFailure) {
            return false;
        }

        return true;
    }
    
    private boolean initalizeDB(){
        
        //Initialize server with emty db
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deleting DB folder and Starting Server With Empty DB");
        // stop server
        boolean serverStopped = common_utils.StopDatabase();
        if (serverStopped == false) {
            String StopServerFailure = common_utils.config.GetTranslation("StopServerFailure");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + StopServerFailure );
            return false;
        }
        // wait until server is finally stopped
        Utils.StaticClass.closeDb();
        // clear db folder contents
        boolean dbIsCleared = common_utils.DeleteFolderContents(common_utils.DBPath);
        if (dbIsCleared == false) {
            String ClearDBFolderFailure = common_utils.config.GetTranslation("ClearDBFolderFailure");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ClearDBFolderFailure + " " + common_utils.DBPath);
            common_utils.RestartDatabaseIfNeeded();
            return false;
        }
        // start server with empty data base folder
        boolean serverStarted = common_utils.StartWithEmptyDataBase();
        if (serverStarted == false) {
            String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + StartServerFailure );
            common_utils.RestartDatabaseIfNeeded();
            return false;
        }
        // wait until server is finally started
        Utils.StaticClass.getDBService();
        return true;
    }
    
    private boolean importAllThesauri(Vector<String> thesaurusVector, String testName) {
        //QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        //IntegerObject sis_session = new IntegerObject();
        //IntegerObject tms_session = new IntegerObject();
        
        
        StringObject CreateDBbackupResultMessage = new StringObject("");
        StringObject DBbackupFileNameCreated = new StringObject("");
        StringObject resultObj = new StringObject("");

        //tools
        DBGeneral dbGen = new DBGeneral();
        //UsersClass WTMSUsers = new UsersClass();
        //DBexportData dbExport = new DBexportData();
        DBImportData dbImport = new DBImportData();
        //DBMergeThesauri dbMerge = new DBMergeThesauri();


        //Start importing
        for (int i = 0; i < thesaurusVector.size(); i++) {
            
            String importThesaurus = thesaurusVector.get(i);
            String Filename = XML_ImportPrefix + importThesaurus + ".xml";
            String logFileNamePath = common_utils.XML_BackupFolder + File.separator + testName + File.separator + Filename;
            String XMLFileToImport = common_utils.XML_BackupFolder + File.separator + testName + File.separator + XML_ExportPrefix + importThesaurus + ".xml";
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting Import from file: " + logFileNamePath);

            OutputStreamWriter logFileWriter = null;

            try {
                OutputStream fout = new FileOutputStream(logFileNamePath);
                OutputStream bout = new BufferedOutputStream(fout);
                logFileWriter = new OutputStreamWriter(bout, "UTF-8");
                
                StringObject translatedMsgObj = new StringObject("");
                Vector<String> translationArgs = new Vector<String>();
                translationArgs.add(importThesaurus);
                translationArgs.add( Utilities.GetNow());
                
                
                dbGen.Translate(translatedMsgObj, "root/ImportData/ReportTitle", translationArgs, Utilities.getMessagesXml());                
                
                logFileWriter.append(ConstantParameters.xmlHeader );//+ "\r\n"
                //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../" + webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
                logFileWriter.append("<importActions>\r\n");
                logFileWriter.append("<title>"+translatedMsgObj.getValue()+"</title>\r\n");
                //logFileWriter.append("<!--"+time + " LogFile  of import data in thesaurus: " + importThesaurusName +".-->\r\n");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "LogFile of import data in thesaurus: " + importThesaurus + ".");


            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }

            try {
                //connection opens in here. Check result and if ok commit actions if not ok abort actions
                if (dbImport.thesaurusImportActions(SessionUserInfo,common_utils, config, targetLocale, pathToErrorsXML, XMLFileToImport,
                        ConstantParameters.xmlschematype_THEMAS, importThesaurus, "backup_before_import_data_to_thes_" + importThesaurus, DBbackupFileNameCreated, resultObj, logFileWriter) == false) {
                    //abort transaction and close connection
                    //Q.free_all_sets();
                    //Q.TEST_abort_transaction();
                    //dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return false;
                }
                logFileWriter.append("</importActions>");
                logFileWriter.flush();
                logFileWriter.close();
                
                StringObject FixDBResultMessage_Global = new StringObject("");
                boolean dbFixed = common_utils.FixDB(true, FixDBResultMessage_Global);
                if(!dbFixed){
                   
                    Utils.StaticClass.webAppSystemOutPrintln("Fix DB failed");
                    Utils.StaticClass.webAppSystemOutPrintln("Error Message: " + FixDBResultMessage_Global.getValue());
                }   
                Utils.StaticClass.webAppSystemOutPrintln("Fix DB finished Successfully!!!");

            } catch (IOException ex) {
                Logger.getLogger(ScheduledBackups.class.getName()).log(Level.SEVERE, null, ex);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to Import thesaurus " + importThesaurus);
                Utils.StaticClass.handleException(ex);
                break;
            }
            
            //commit transaction and close connection
            //Q.free_all_sets();
            //Q.TEST_end_transaction();
            //dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }

        return true;
    }

    /*
    public void sendmail() {
        
        String mailContent  = "Automatic Maintenance of " + sessionInstance.path +" Failed.\n\nException occured on " + formatter.format(new Date()) +
                        ".\nCheck Tomcat Logs and restore Database in case automatic restoration failed.";
        
        String subject = "THEMASMaintenanceError";
        try {
            // Create a mail session
            Properties propers = new java.util.Properties();
            propers.put("mail.smtp.host", Parameters.mailHost);
            //propers.put("mail.smtp.host", "mailhost.ics.forth.gr");
            propers.put("mail.smtp.port", "" + 25);
            Session sessionn = Session.getDefaultInstance(propers, null);

            // Construct the message
            Message msg = new MimeMessage(sessionn);
            msg.setFrom(new InternetAddress(Parameters.ApplicationName + "@ics.forth.gr"));
            for (int i = 0; i < Parameters.mailList.length; i++) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(Parameters.mailList[i]));
            }

            //msg.setRecipients(Message.RecipientType.TO, addresses);
            msg.setSubject(subject);
            msg.setText(mailContent);

            Transport.send(msg);
        } catch (MessagingException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("Failed to send e-mail");
            Logger.getLogger(ScheduledBackups.class.getName()).log(Level.SEVERE, null, ex);
            Utils.StaticClass.handleException(ex);
            cancel();
            //Parameters.timer.cancel();
            Parameters.ENABLE_AUTOMATIC_BACKUPS = true;
        }
    }
    */
}

