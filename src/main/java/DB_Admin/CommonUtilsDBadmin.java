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

import DB_Classes.DBGeneral;
import Utils.Parameters;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import org.neo4j.io.fs.FileUtils;

/*-----------------------------------------------------
class CommonUtilsDBadmin
-------------------------------------------------------
class with common utilities used by the application
-------------------------------------------------------*/
public class CommonUtilsDBadmin {

    public ConfigDBadmin config;
    public static boolean ExceptionCaught = false;
    // general configurations
    public String BasePath, DBPath; // g.e. SAMWISE 1244 C:\ICS-FORTH\TMS\Applications\TMS\db
    public String Neo4j_GenericTsvFile;
    public String Neo4j_SpecificTsvFile; // g.e. "C:\ICS-FORTH\TMS-EKT\telos_sources\model\specific_thes"
    //public int currentLanguage; // 0 for english, 1 for greek
    public File DB_Folder, DB_BackupFolder, XML_BackupFolder; // g.e. C:\ICS-FORTH\TMS\Applications\TMS\db C:\ICS-FORTH\TMS\Applications\TMS\db\db_BACKUP
    public String Neo4jExportTsvsFileDirectory; // g.e. "C:\\ICS-FORTH\\TMS"
    public String PrimaryLanguage;
    public String PrimaryLanguagePrefix;
    
    /*-----------------------------------------------------
    CommonUtils() - constructor
    -------------------------------------------------------
    INPUT: the base class of the application
    -------------------------------------------------------*/

    public CommonUtilsDBadmin(ConfigDBadmin c) {
        config = c;

        //MachineName = config.GetConfigurationValue("MACHINE_NAME");
        //PortNumber = config.GetConfigurationValue("PORT_NUMBER");
        BasePath = config.GetConfigurationValue("Neo4j_DB_FOLDER_PATH");
        DBPath = BasePath + config.GetConfigurationValue("Neo4j_DB_PATH");
        DB_Folder = new File(DBPath);
        if(DB_Folder.exists()==false){
            DB_Folder.mkdir();
        }
        // create directory db_backup next to DBPath (if it exists, it does nothing)
        String DB_BackupFolderPath = BasePath + config.GetConfigurationValue("Neo4j_DB_Backups_SubPATH");
        DB_BackupFolder = new File(DB_BackupFolderPath);
        if(DB_BackupFolder.exists()==false){
            DB_BackupFolder.mkdirs();
        }
        

        String XML_BackupFolderPath = BasePath + config.GetConfigurationValue("Neo4j_XML_Backups_SubPATH");
        XML_BackupFolder = new File(XML_BackupFolderPath);
        if(XML_BackupFolder.exists()==false){
            XML_BackupFolder.mkdirs();
        }        

        Neo4jExportTsvsFileDirectory = BasePath + config.GetConfigurationValue("Neo4j_ExportFileDirectory");
        Neo4j_GenericTsvFile = BasePath + config.GetConfigurationValue("Neo4j_GenericTSVFile");
        Neo4j_SpecificTsvFile= BasePath + config.GetConfigurationValue("Neo4j_SpecificTSVFile");
        
        
        //currentLanguage = new Integer(config.GetConfigurationValue("CURRENT_LANGUAGE")).intValue();
                
        PrimaryLanguage = config.GetConfigurationValue("PrimaryLanguage");
        PrimaryLanguagePrefix = config.GetConfigurationValue("PrimaryLanguagePrefix");
    }

    public boolean StartDatabase() {
        if(Utils.ConstantParameters.DEVELOPING){
            return true;
        }
    
        return false;
    }

    
    public boolean StartWithEmptyDataBase() {
        if(Utils.ConstantParameters.DEVELOPING){
            return true;
        }/*
        try {
            Process p = Runtime.getRuntime().exec("cmd /c " + StartWithEmptyDataBaseBatFileName, null, new File(DatabaseBatFileDirectory));
            return true;
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);
            return false;
        }*/
        return false;
    }
    

    /*----------------------------------------------------------------------
    RestartDatabaseIfNeeded()
    ------------------------------------------------------------------------
    FUNCTION: in case SIS server is not running, restarts it with:
    - StartDatabase() in case DB exists
    - StartWithEmptyDataBase() in case DB does not exist
    CALLED BY: any servlet after finishing its job
    ------------------------------------------------------------------------*/
    synchronized public void RestartDatabaseIfNeeded() {
        if(Utils.ConstantParameters.DEVELOPING){
            return;
        }
        /*
        boolean databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        if (databaseIsRunning == false) {
            if (DataBaseExists() == true) {
                StartDatabase();
            } else {
                StartWithEmptyDataBase();
            }
            // wait until server is finally started
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            while (databaseIsRunning == false) {
                databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            }
        }*/
    }

    /*----------------------------------------------------------------------
    DataBaseExists()
    ------------------------------------------------------------------------
    FUNCTION: checks if SIS data base exists
    CALLED BY: RestartDatabaseIfNeeded() so as to call StartDatabase() or StartWithEmptyDataBase() 
    OUTPUT: true in case DB exists, false otherwise
    ------------------------------------------------------------------------*/
    public boolean DataBaseExists() {
        File telos_db_obj = new File(DBPath + "\\telos_db.obj");
        if (telos_db_obj.isFile() == true) {
            return true;
        }
        return false;
    }

    /*----------------------------------------------------------------------
    DataBaseIsInitialized()
    ------------------------------------------------------------------------
    FUNCTION: checks if SIS data base is initialized (model exists)
    CALLED BY: CreateThesaurus servlet so as to initialize data base anyway, 
    in case it is not initialized
    OUTPUT: true in case DB is initialized, false otherwise
    ------------------------------------------------------------------------*/
    public boolean DataBaseIsInitialized(QClass Q) {
        
        
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(new StringObject("Thesaurus"));
        if (sysidL == Q.APIFail) {
            return false;
        }
        return true;
    }

    public boolean StopDatabase() {
        if(Utils.ConstantParameters.DEVELOPING){
            return true;
        }
        /*
        String taskKillOutput = "";
        
        // the filter "Windowtitle eq ..." must NOT be used
        //String taskKillCommand = "taskkill /S \"" + MachineName + "\" /FI \"Windowtitle eq " + DatabaseFullPath + "\"";
        String taskKillCommand = "taskkill /F /S \"" + MachineName + "\" /IM \"" + DatabaseName + "\"";

        try {
            Runtime Rt = Runtime.getRuntime();
            // Get available Process 
            Process p = Rt.exec(taskKillCommand);
            InputStream ip = p.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(ip));
            while ((taskKillOutput = in.readLine()) != null) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + taskKillOutput);
                if (taskKillOutput.indexOf("SUCCESS") >= 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            Utils.StaticClass.handleException(e);
        }
*/
        return false;
        
    }

    /*----------------------------------------------------------------------
    StartSISexport()
    ------------------------------------------------------------------------
    FUNCTION: executes <SISExportBatFileName> of config.xml found at 
    <SISExportBatFileDirectory> g.e. C:\ICS-FORTH\TMS\export.exe
    OUTPUT: true in case of succesfull execution, false otherwise
    ------------------------------------------------------------------------*/
    public boolean StartSISexport() {
        if(Utils.ConstantParameters.DEVELOPING){
            return true;
        }
        /*
        try {
            Process p = Runtime.getRuntime().exec("cmd /c " + SISExportBatFileName, null, new File(SISExportBatFileDirectory));
            return true;
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);
            return false;
        }*/
        return false;
    }
    /*-----------------------------------------------------
                      FixDB()
    -------------------------------------------------------*/
    public boolean FixDB(boolean resetDb, StringObject FixDBResultMessage) {
    
        
        // check if server runs (close it before fixing DB)
        /*
        boolean databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        if (databaseIsRunning == true) {
            StopDatabase();
            // wait until server is finally stopped
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            while (databaseIsRunning == true) {
                databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            }
        }
*/
        /*
        // check if given DB folder is empty
        boolean DBfolderIsEmpty = FolderIsEmpty(DBPath);
        if (DBfolderIsEmpty == true) {
            String EmptyDataBaseFolder1 = config.GetTranslation("EmptyDataBaseFolder1");
            String EmptyDataBaseFolder2 = config.GetTranslation("EmptyDataBaseFolder2");
            FixDBResultMessage.setValue(EmptyDataBaseFolder1 + " " + DBPath + " " + EmptyDataBaseFolder2);
            RestartDatabaseIfNeeded();
            return false;
        }*/

        // create a backup of the data base anyway
        StringObject DBbackupFileNameCreated = new StringObject("");
        if(resetDb){
            CreateDBbackup("backup_before_fixing_data_base", FixDBResultMessage, DBbackupFileNameCreated);
        }

        /*
        // start server
        boolean serverStarted = StartDatabase();
        if (serverStarted == false) {
            String StartServerFailure = config.GetTranslation("StartServerFailure");
            FixDBResultMessage.setValue(StartServerFailure + " " + DatabaseBatFileDirectory + "\\" + DatabaseBatFileName);
            RestartDatabaseIfNeeded();
            return false;
        }*/
        /*
        // wait until server is finally started
        databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        while (databaseIsRunning == false) {
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        }

        */
        
        
        // remove previously found DB_Admin_ExportOutputTelosFile.tls (if any)
        String exportFileName =Neo4jExportTsvsFileDirectory + "\\TSV_Global_Export_"+GetCurrentDateAndTime()+".tsv";
        //String TelosFileName = Neo4jExportBatFileDirectory + "\\" + exportFileName;
        //File TelosFile = new File(TelosFileName);
        //TelosFile.delete();
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+TelosFileName + " deleted");
        Utils.StaticClass.closeDb();
        
        // call export program
        TSVExportsImports expimp = new TSVExportsImports();
        boolean exportCompleted = expimp.globalExportToFile(exportFileName,false,false);//StartSISexport();
        if(exportCompleted==false){
            //TODO: Translate message
            //FixDBResultMessage.setValue("Error occured while exporting");
            String ExportFailed = config.GetTranslation("ExportFailed");
            FixDBResultMessage.setValue(ExportFailed);
            return false;
        }
        
        /*
        if (exportStarted == false) {
            String StartExportFailure = config.GetTranslation("StartExportFailure");
            FixDBResultMessage.setValue(StartExportFailure + " " + SISExportBatFileDirectory + "\\" + SISExportBatFileName);
            return false;
        }*/

        // wait until export is started
        // ATTENTION: do NOT pass window-title value for filtering (it is NOT "C:\\ICS-FORTH\\TMS\\bin\\export.exe")
        /*
        boolean sisExportIsRunning = ProcessIsRunning(MachineName, null, SISExportName);
        while (sisExportIsRunning == false) {
            sisExportIsRunning = ProcessIsRunning(MachineName, null, SISExportName);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"sisExportIs NOT Running");
        }
        */
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- sisExportIs STARTED ---------------------");
        // wait until export is finished
        /*
        sisExportIsRunning = ProcessIsRunning(MachineName, null, SISExportName);
        while (sisExportIsRunning == true) {
            sisExportIsRunning = ProcessIsRunning(MachineName, null, SISExportName);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"sisExportIsRunning");
        }
        */
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- sisExportIs FINISHED ---------------------");

        // check if DB_Admin_ExportOutputTelosFile.tls is created
        /*
        TelosFile = new File(TelosFileName);
        if (TelosFile.isFile() == false) {
            String ExportFailed = config.GetTranslation("ExportFailed");
            FixDBResultMessage.setValue(ExportFailed);
            return false;
        }*/

        if(resetDb==false){
            return true;
        }

        // stop server
        /*boolean serverStopped = StopDatabase();
        if (serverStopped == false) {
            String StopServerFailure = config.GetTranslation("StopServerFailure");
            FixDBResultMessage.setValue(StopServerFailure + " " + DatabaseBatFileDirectory + "\\" + DatabaseBatFileName);
            return false;
        }
        */
        // wait until server is finally stopped
        /*
        databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        while (databaseIsRunning == true) {
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        }*/

        // clear db folder contents
        Utils.StaticClass.closeDb();
        File srcDir = new File(Utils.StaticClass.getGraphDbFolderPath());
        try {
            Thread.sleep(500);
            FileUtils.deleteRecursively(srcDir);
            Thread.sleep(500);
            srcDir.mkdir();
        } catch (IOException ex) {
            Logger.getLogger(CommonUtilsDBadmin.class.getName()).log(Level.SEVERE, null, ex);
            String ClearDBFolderFailure = config.GetTranslation("ClearDBFolderFailure");
            FixDBResultMessage.setValue(ClearDBFolderFailure + " " + DBPath);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(CommonUtilsDBadmin.class.getName()).log(Level.SEVERE, null, ex);
            String ClearDBFolderFailure = config.GetTranslation("ClearDBFolderFailure");
            FixDBResultMessage.setValue(ClearDBFolderFailure + " " + DBPath);
            return false;
        }
        /*boolean dbIsCleared = DeleteFolderContents(DBPath);
        if (dbIsCleared == false) {
            String ClearDBFolderFailure = config.GetTranslation("ClearDBFolderFailure");
            FixDBResultMessage.setValue(ClearDBFolderFailure + " " + DBPath);
            RestartDatabaseIfNeeded();
            return false;
        }*/

        
        // start server with empty data base folder
        Utils.StaticClass.getDBService();
        
        boolean importSucceeded = expimp.globalImportFromFile(exportFileName);
        if(importSucceeded==false){
            String TelosFailed = config.GetTranslation("TelosFailed");
            FixDBResultMessage.setValue(TelosFailed);
            return false;
        }
        
        //shutdown db in order to commit transactions and openit again
        Utils.StaticClass.closeDb();
        Utils.StaticClass.getDBService();

        
        
        // inform user for success
        String FixDBSuccess = config.GetTranslation("FixDBSuccess");
        FixDBResultMessage.setValue(FixDBSuccess);
        return true;
        /*
        serverStarted = StartWithEmptyDataBase();
        if (serverStarted == false) {
            String StartServerFailure = config.GetTranslation("StartServerFailure");
            FixDBResultMessage.setValue(StartServerFailure + " " + DatabaseBatFileDirectory + "\\" + StartWithEmptyDataBaseBatFileName);
            RestartDatabaseIfNeeded();
            return false;
        }*/
        // wait until server is finally started
        /*databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        while (databaseIsRunning == false) {
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        }*/

        /*
        // get the modification date of db folder contents before telos parsing
        long DBmodificationBeforeTelos = GetFolderContentsModificationDate(DBPath);
        // call telos for TelosFileName x N times
        StringObject telosOutputObj = new StringObject("");
        Vector<String> tlsFiles = new Vector<String>();
        //tlsFiles.add(TelosFileName);
        for(int i=0; i< SISTelosParseTimes; i++) {
            // start telos
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- sisTelosIs STARTED  "+i+"--------------------");
            //boolean telosStarted = common_utils.StartSIStelos(common_utils.SISTelosBatFileName,new Vector<String>(), common_utils.SISTelosBatFileDirectory, telosOutputObj);
            boolean telosStarted = StartSIStelos(tlsFiles, telosOutputObj);
            if (telosStarted == false) {
                String StartTelosFailure = config.GetTranslation("StartTelosFailure");
                FixDBResultMessage.setValue(StartTelosFailure + " " + SISTelosBatFileDirectory + "\\" + SISTelosBatFileName);
                return false;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- sisTelosIs FINISHED "+i+"--------------------");
        }

        // get the modification date of db folder contents after telos parsing
        long DBmodificationAfterTelos = GetFolderContentsModificationDate(DBPath);

        //checking last tls parsing result
        boolean telosSucceded = SIStelosSucceded(telosOutputObj);

        if (DBmodificationAfterTelos == DBmodificationBeforeTelos || telosSucceded == false) {
            String TelosFailed = config.GetTranslation("TelosFailed");
            FixDBResultMessage.setValue(TelosFailed + " " + telosOutputObj.getValue());
            // stop server
            //serverStopped = StopDatabase();
            RestartDatabaseIfNeeded();
            return false;
        }
*/
        /*
        if(ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName)){
            // stop server
            serverStopped = StopDatabase();
            if (serverStopped == false) {
                String StopServerFailure = config.GetTranslation("StopServerFailure");
                FixDBResultMessage.setValue(StopServerFailure + " " + DatabaseBatFileDirectory + "\\" + DatabaseBatFileName);
                RestartDatabaseIfNeeded();
                return false;
            }
        }*/

/*
        // after finishing the job and in case SIS server is not running, restart it
        RestartDatabaseIfNeeded();

        // inform user for success
        String FixDBSuccess = config.GetTranslation("FixDBSuccess");
        FixDBResultMessage.setValue(FixDBSuccess);
        return true;
        */
    }


    /*----------------------------------------------------------------------
    StartSIStelos()
    ------------------------------------------------------------------------
    FUNCTION: executes <SISTelosBatFileName> of config.xml found at 
    <SISTelosBatFileDirectory> g.e. C:\ICS-FORTH\TMS\telos.exe
    OUTPUT: true in case of succesfull execution, false otherwise
    ------------------------------------------------------------------------*/
    public boolean StartSIStelos(Vector<String> tlsFiles, StringObject telosOutputObj) {

        return true;
        /*
        String envVar = DBPath;
        //Utils.StaticClass.webAppSystemOutPrintln(envVar);
        StringBuffer telosOutputSb = new StringBuffer("");
        try {

            for (int i = 0; i < tlsFiles.size(); i++) {

                ProcessBuilder pb = new ProcessBuilder(SISTelosFullPath, "-S" + MachineName, "-P" + PortNumber, tlsFiles.elementAt(i));
                Map<String, String> env = pb.environment();
                env.put("DB_DIR", envVar);//enviromental variable necessary for telosPORT.exe to run
                pb.redirectErrorStream(false);
                //Utils.StaticClass.webAppSystemOutPrintln("STARTING " + pb.command().toString());                        
                InputStream ip = pb.start().getErrorStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(ip));
                String telosOutput = "";

                while ((telosOutput = in.readLine()) != null) {
                    telosOutputSb.append(telosOutput);
                }
            }
            return true;

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);
            return false;
        } finally {
            telosOutputObj.setValue(telosOutputSb.toString());
        }
        */
    }

    /*----------------------------------------------------------------------
    SIStelosSucceded()
    ------------------------------------------------------------------------*/
    public boolean SIStelosSucceded(StringObject telosLogStr) {
        String TELOS_ERROR_KEYWORDS[] = {"ERROR", "abort"};
        for (int i = 0; i < TELOS_ERROR_KEYWORDS.length; i++) {
            String keyword = TELOS_ERROR_KEYWORDS[i];
            if (telosLogStr.getValue().indexOf(keyword) >= 0) {
                return false;
            }
        }
        return true;
    }

    /*----------------------------------------------------------------------
    GetCurrentDateAndTime()
    ------------------------------------------------------------------------
    OUTPUT : - a string with the current date and time separated with "-"
    FUNCTION: - gets in a string the current date and time separated with "-"
    ------------------------------------------------------------------------*/
    public String GetCurrentDateAndTime() {
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

    /*----------------------------------------------------------------------
    ProcessIsRunning()
    ------------------------------------------------------------------------
    FUNCTION: - uses DOS command "tasklist"     
    to check if a specific process is currently running.
    It returns true in case:
    - Windowtitle != null
    a process with the given window title (full path) is running and
    the string output of the tasklist command contains the given ProcessName
    - Windowtitle == null
    the string output of the tasklist command contains the given ProcessName
    ------------------------------------------------------------------------*/
    public boolean ProcessIsRunning(String MachineName, String Windowtitle, String ProcessName) {
        if(Utils.ConstantParameters.DEVELOPING){
            return true;
        }
        /*
        String taskListCommand = "";
        DBGeneral dbGen = new DBGeneral();
        // the filter "Windowtitle eq ..." must NOT be used                
        
        //if (Windowtitle != null) {
        //taskListCommand = "tasklist /S \"" + MachineName + "\" /FI \"Windowtitle eq " + Windowtitle + "\" /FO \"CSV\" /NH";
        //}
        //else {
        //taskListCommand = "tasklist /S \"" + MachineName + "\" /FO \"CSV\" /NH";
        //}
         
        taskListCommand = "tasklist /S \"" + MachineName + "\" /FO \"CSV\" /NH";
        // AppendFileContents("C:\\test.txt", "ProcessIsRunning(" + ProcessName + ") - taskListCommand: " + taskListCommand + "\r\n");        
        String taskListOutput = "";
        try {

            ProcessBuilder pb = new ProcessBuilder("tasklist", "/S", "\"" + MachineName + "\"", "/FO", "\"CSV\"", "/NH");
            //Runtime Rt = Runtime.getRuntime();
            // Get available Process 
            InputStream ip = pb.start().getInputStream();//Rt.exec(taskListCommand).getInputStream(); 

            BufferedReader in = new BufferedReader(new InputStreamReader(ip));
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"------------------------------------------------------------------------");
            while ((taskListOutput = in.readLine()) != null) {
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+taskListOutput);
                if (taskListOutput.indexOf(ProcessName) >= 0) {
                    // AppendFileContents("C:\\test.txt", "ProcessIsRunning() = TRUE" + "\r\n");
                    //return true;
                    if (ProcessName.equals(DatabaseName)) {

                        // open connection
                        // NO begin query it might stall due to a running transaction
                        QClass Q = new neo4j_sisapi.QClass();
                        IntegerObject sis_session = new IntegerObject();
                        int ret = 0;
                        ret = Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService());
                        if (ret == Q.APIFail) {
                            //Utils.StaticClass.webAppSystemOutPrintln("-------------------------------------- Database is not ready yet");
                            return false;
                        }
                        ret = Q.TEST_open_connection();
                        if (ret == Q.APIFail) {
                            //Utils.StaticClass.webAppSystemOutPrintln("-------------------------------------- Database is not ready yet");
                            return false;
                        }

                        // close SIS connection
                        Q.TEST_close_connection();
                        Q.TEST_release_SIS_Session();
                        return true;
                    } else {
                        return true;
                    }
                }
            }
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"------------------------------------------------------------------------");
        } catch (IOException e) {
            if (ExceptionCaught == false) {
                ExceptionCaught = true;
                Utils.StaticClass.handleException(e);
            }
            // AppendFileContents("C:\\test.txt", "ProcessIsRunning() ERROR: " + e.toString() + "\r\n");                                

        }

        // AppendFileContents("C:\\test.txt", "ProcessIsRunning() = FALSE" + "\r\n");
*/
        
        return false;
    }

    /*----------------------------------------------------------------------
    FileNameContainsInvalidCharacters()
    ------------------------------------------------------------------------*/
    public boolean FileNameContainsInvalidCharacters(String fileName) {
        String[] InvalidCharacters = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        int len = InvalidCharacters.length;
        for (int i = 0; i < len; i++) {
            if (fileName.indexOf(InvalidCharacters[i]) >= 0) {
                return true;
            }
        }
        return false;
    }

    /*----------------------------------------------------------------------
    ReplaceSpecialCharacters()
    ------------------------------------------------------------------------*/
    public String ReplaceSpecialCharacters(String str) {
        String[] InvalidCharacters = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        str.replaceAll("<", "&lt;");
        str.replaceAll(">", "&gt;");
        str.replaceAll("&", "&amp;");
        return str;
    }

    /*----------------------------------------------------------------------
    GetFileContents()
    ------------------------------------------------------------------------*/
    public String GetFileContents(String fileName) {
        String str = "";
        try {
            InputStream in = new FileInputStream(new File(fileName));

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                str += new String(buf, 0, len);
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Unable to read from file: " + fileName);
            Utils.StaticClass.handleException(e);
        }
        return str;
    }

    /*----------------------------------------------------------------------
    SetFileContents()
    ------------------------------------------------------------------------
    INPUT : - fileName: the full path of a file
    - contents: a string to be written in the file
    FUNCTION : writes the given file with the given String contents
    ------------------------------------------------------------------------*/
    public void SetFileContents(String fileName, String contents) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(contents);
            out.close();
        } catch (IOException e) {
            Utils.StaticClass.handleException(e);
        }
    }

    /*----------------------------------------------------------------------
    AppendFileContents()
    ------------------------------------------------------------------------
    INPUT : - fileName: the full path of a file
    - contents: a string to be appended in the file
    FUNCTION : appends the given file with the given String contents
    ------------------------------------------------------------------------*/
    public void AppendFileContents(String fileName, String contents) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(contents);
            out.close();
        } catch (IOException e) {
            Utils.StaticClass.handleException(e);
        }
    }

    /*----------------------------------------------------------------------
    GetListOfDBbackups()
    ------------------------------------------------------------------------*/
    public Vector<String> GetListOfDBbackups() {
        // fill list with available backup files
        String filesFoundInDBBackupFolder[] = DB_BackupFolder.list();
        int filesFoundInDBBackupFolderCount = filesFoundInDBBackupFolder.length;
        Vector<String> filesInDBBackupFolderToBeDisplayed = new Vector<String>();
        for (int i = 0; i < filesFoundInDBBackupFolderCount; i++) {
            if (filesFoundInDBBackupFolder[i].startsWith("#") && filesFoundInDBBackupFolder[i].endsWith("@.zip")) {
                filesInDBBackupFolderToBeDisplayed.add(filesFoundInDBBackupFolder[i]);
            }
        }
        return filesInDBBackupFolderToBeDisplayed;
    }

    /*-----------------------------------------------------
    CreateDBbackup()
    -------------------------------------------------------*/
    public boolean CreateDBbackup(String backupDescription, StringObject CreateDBbackupResultMessage, StringObject DBbackupFileNameCreated/*, String folderPath*/) {
        // check backupDescription for invalid characters
        boolean fileNameContainsInvalidCharacters = FileNameContainsInvalidCharacters(backupDescription);
        if (fileNameContainsInvalidCharacters == true) {
            String FileNameContainsInvalidCharacters = config.GetTranslation("fileNameContainsInvalidCharacters");
            String Error = config.GetTranslation("Error");
            CreateDBbackupResultMessage.setValue(Error + ": " + FileNameContainsInvalidCharacters + "  \\ / : * ? \" &lt; &gt; |");
            return false;
        }

        /*
        // if server runs, stop it WITHOUT asking (it is necessary to close it before creating backup)
        boolean databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        if (databaseIsRunning == true) {
            StopDatabase();
            // wait until server is finally stopped
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            while (databaseIsRunning == true) {
                databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            }
        }
        */

        // construct the name of the zip file <backupDescription> + currentDate + currentTime
        String zipFileDeate = GetCurrentDateAndTime();
        String zipFileName = "#" + zipFileDeate + "#";
        zipFileName += "_@" + backupDescription + "@.zip";
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Zip file to be made: " + zipFileName);

        Utils.StaticClass.closeDb();
        
        File srcDir = new File(Utils.StaticClass.getGraphDbFolderPath());
        File destDir = new File(srcDir.getParent()+"/"+zipFileDeate + "/");
        
        try {
            destDir.mkdir();
            FileUtils.copyRecursively(srcDir, destDir);
            String dbZippedBackup = "";
			/*
            if(folderPath!=null && folderPath.length()>0){
                File folder = new File(folderPath);
                if(folder.exists()&&folder.isDirectory()){
                    dbZippedBackup = folder.getPath() + "\\" + zipFileName;
                }
                else{
                    dbZippedBackup = DB_BackupFolder.getPath() + "\\" + zipFileName;
                }
            }
            else{*/
                dbZippedBackup = DB_BackupFolder.getPath() + "\\" + zipFileName;
            //}
            
            ZipUtilityDBadmin zu = new ZipUtilityDBadmin(destDir.getAbsolutePath(), dbZippedBackup);
            
            
            FileUtils.deleteRecursively(destDir);
            // inform user for success
            String CreateDBbackupSuccess = config.GetTranslation("CreateDBbackupSuccess");
            CreateDBbackupResultMessage.setValue(CreateDBbackupSuccess + " " + zipFileName);
            DBbackupFileNameCreated.setValue(zipFileName);
        } catch (IOException ex) {
            Logger.getLogger(CommonUtilsDBadmin.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        // zip argSisPath/db with name file inside backup directory
        String dbFolderForZip = DB_Folder.getPath();
        String dbZippedBackup = DB_BackupFolder.getPath() + "\\" + zipFileName;
        ZipUtilityDBadmin zu = new ZipUtilityDBadmin(dbFolderForZip, dbZippedBackup);
*/
        

        // after finishing the job and in case SIS server is not running, restart it
        //RestartDatabaseIfNeeded();

        return true;
    }

    /*-----------------------------------------------------
    RestoreDBbackup()
    -------------------------------------------------------*/
    public boolean RestoreDBbackup(String selectedDBbackupFileName, StringObject result) {
        
        /*
        // check if server runs (close it before restoring backup)
        boolean databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
        if (databaseIsRunning == true) {
            StopDatabase();
            // wait until server is finally stopped
            databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            while (databaseIsRunning == true) {
                databaseIsRunning = ProcessIsRunning(MachineName, DatabaseFullPath, DatabaseName);
            }
        }
        */
        File fileForRestore = new File(DB_BackupFolder.getPath() + "\\" + selectedDBbackupFileName);
        if(fileForRestore.exists()==false){
            result.setValue("Could Not find file for restore: " + DB_BackupFolder.getPath() + "\\" + selectedDBbackupFileName);
            return false;
        }
        

        Utils.StaticClass.closeDb();
        File srcDir = new File(Utils.StaticClass.getGraphDbFolderPath());
        try {
            
            Thread.sleep(500);
            
            FileUtils.deleteRecursively(srcDir);
            Thread.sleep(500);
            srcDir.mkdir();
        } catch (IOException ex) {
            Logger.getLogger(CommonUtilsDBadmin.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(CommonUtilsDBadmin.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        // restore file
        
        String file_to_be_extracted = DB_BackupFolder.getPath() + "\\" + selectedDBbackupFileName;
        ExtractUtility eu = new ExtractUtility(file_to_be_extracted, Utils.StaticClass.getGraphDbFolderPath());
        // inform user for success
        String RestoreDBbackupSuccess = config.GetTranslation("RestoreDBbackupSuccess");
        result.setValue(RestoreDBbackupSuccess + selectedDBbackupFileName);

        // after finishing the job and in case SIS server is not running, restart it
        RestartDatabaseIfNeeded();

        //AppendFileContents("C:\\test.txt", "RestoreDBbackup() SUCCEDED for: " + file_to_be_extracted + "\r\n");
        return true;
    }

    /*----------------------------------------------------------------------
    CopyFolder()
    ------------------------------------------------------------------------
    INPUT : - File fromFile: the source folder
    - File toFile: the target folder
    FUNCTION: - copies source folder to target folder
    OUTPUT: true in case of succesfull execution, false otherwise
    ------------------------------------------------------------------------*/
    public boolean CopyFolder(File source, File destination) {
        destination.mkdir();
        File[] list = source.listFiles();
        for (int i = 0; i < list.length; i++) {
            File dest = new File(destination, list[i].getName());
            if (list[i].isDirectory()) {
                dest.mkdir();
                CopyFolder(list[i], dest);
            } else {
                if (!list[i].isHidden()) {
                    CopyFile(list[i], dest);
                }
            }
        }
        return true;
    }

    /*----------------------------------------------------------------------
    ReplaceKeywordsInFolder()
    ------------------------------------------------------------------------
    INPUT: - rootFolder: the folder to be parsed
    - TELOS_Keywords[] = {"SIS_XXXXX_SIS", "SIS_Xxxxx_SIS", "SIS_xxxxx_SIS", "SIS_zzzzz_SIS", "SIS_ZZZZZ_SIS"}
    - THESAURUS_Keywords[] = (g.e.) {"EKT", "Ekt", "ekt", "EL", "Greek"}
    FUNCTION: parses all the files of the given folder and replaces TELOS_Keywords[]
    in each tls file found in it with the strings of the parallel array THESAURUS_Keywords[]
    ------------------------------------------------------------------------*/
    public void ReplaceTelosKeywordsInFolder(File rootFolder, String TELOS_Keywords[], String THESAURUS_Keywords[]) {
        File[] rootFolderList = rootFolder.listFiles();
        for (int i = 0; i < rootFolderList.length; i++) {
            File childFile = rootFolderList[i];
            if (childFile.isDirectory()) {
                ReplaceTelosKeywordsInFolder(childFile, TELOS_Keywords, THESAURUS_Keywords);
            } else {
                String childFileFullName = "";
                try {
                    childFileFullName = childFile.getCanonicalPath();
                } catch (Exception e) {
                    Utils.StaticClass.handleException(e);
                }
                String childFileName = childFile.getName();
                // ignore not tls files
                if (childFileName.endsWith(".tls") == false) {
                    continue;
                }
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Telos file to be parsed: " + childFileFullName);
                // get the contents of the file in a string
                String childFileContents = GetFileContents(childFileFullName);
                // do the replacements
                int keywordsSize = TELOS_Keywords.length;
                for (int j = 0; j < keywordsSize; j++) {
                    childFileContents = childFileContents.replaceAll(TELOS_Keywords[j], THESAURUS_Keywords[j]);
                }
                // write back the string to the file
                SetFileContents(childFileFullName, childFileContents);
            }
        }
    }

    /*----------------------------------------------------------------------
    CopyFile()
    ------------------------------------------------------------------------
    INPUT : - File fromFile: the source FILE
    - File toFile: the target FILE
    FUNCTION: - copies source FILE to target FILE
    ------------------------------------------------------------------------*/
    void CopyFile(File fromFile, File toFile) {
        try {
            FileInputStream fis = new FileInputStream(fromFile);
            FileOutputStream fos = new FileOutputStream(toFile);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------
    DeleteFolder()
    ------------------------------------------------------------------------
    FUNCTION: deletes all files and subdirectories under dir.
    Returns true if all deletions were successful.
    If a deletion fails, the method stops attempting to delete and returns false.
    OUTPUT: true in case of succesfull execution, false otherwise
    ------------------------------------------------------------------------*/
    public static boolean DeleteFolder(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = DeleteFolder(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /*----------------------------------------------------------------------
    FolderIsEmpty()
    ------------------------------------------------------------------------
    INPUT: the full path of a folder for check
    OUTPUT: true in case folder is empty, false otherwise
    FUNCTION: checks if the given folder is empty
    CALLED BY: FixDB class for checking if the db folder has contents to fix
    ------------------------------------------------------------------------*/
    public boolean FolderIsEmpty(String folderFullPath) {
        File folder = new File(folderFullPath);
        File[] folderFiles = folder.listFiles();
        int folderFilesCount = folderFiles.length;
        if (folderFilesCount == 0) {
            return true;
        } else {
            return false;
        }
    }

    /*----------------------------------------------------------------------
    DeleteFolderContents()
    ------------------------------------------------------------------------
    INPUT: the full path of a folder for contents deletion
    OUTPUT: true in case folder is finally cleared, false otherwise (in case of locked contents)
    FUNCTION: deletes the contents of the given folder (ONLY for file contents)
    CALLED BY: FixDB class for clearing the db folder after export and before 
    parsing the export output with telos
    ------------------------------------------------------------------------*/
    public boolean DeleteFolderContents(String folderFullPath) {
        File f = new File(folderFullPath);
        if(f.exists()==false || f.isDirectory() == false){
            return false;
        }
        
        if(folderFullPath.equals(DBPath)){
            Utils.StaticClass.closeDb();
            
        }
        try {
            if(folderFullPath.equals(DBPath)){
                Thread.sleep(500);
            }
            FileUtils.deleteRecursively(f);
            if(folderFullPath.equals(DBPath)){
                Thread.sleep(500);
            }
            f.mkdir();
        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (InterruptedException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
        /*
        File folder = new File(folderFullPath);
        File[] folderFiles = folder.listFiles();
        int folderFilesCount = folderFiles.length;
        for (int i = 0; i < folderFilesCount; i++) {
            folderFiles[i].delete();
        }
        // return true;
        // check if folder is finally empty (in case SIS server is running, DB-folder contents are NOT deleted!)
        folderFilesCount = folder.listFiles().length;
        if (folderFilesCount == 0) {
            return true;
        } else {
            return false;
        }*/
    }

    /*----------------------------------------------------------------------
    GetFolderContentsModificationDate()
    ------------------------------------------------------------------------
    INPUT: the full path of a folder for check
    OUTPUT: a long value with the most recent (max) date of modification of 
    a file inside the folder, or -1 in case the folder is empty
    FUNCTION: calculates the most recent (max) date of modification of 
    a file inside the folder
    CALLED BY: FixDB class for db folder before and after parsing 
    the export output with telos, so as to check if parsing was succesfull
    ------------------------------------------------------------------------*/
    public long GetFolderContentsModificationDate(String folderFullPath) {
        long modDate = -1;
        File folder = new File(folderFullPath);
        File[] folderFiles = folder.listFiles();
        int folderFilesCount = folderFiles.length;
        for (int i = 0; i < folderFilesCount; i++) {
            long md = folderFiles[i].lastModified();
            if (md > modDate) {
                modDate = md;
            }
        }
        return modDate;
    }

    /*----------------------------------------------------------------------
    FileExists()
    ------------------------------------------------------------------------
    INPUT: - folderFullPath, the full path of a folder for check
    - fileName, the name of a file for check
    OUTPUT: true in case the file is found in the folder, false otherwise
    FUNCTION: checks if a file with the given name is found in the given folder
    ------------------------------------------------------------------------*/
    public boolean FileExists(String folderFullPath, String fileName) {
        File folder = new File(folderFullPath);
        File[] folderFiles = folder.listFiles();
        int folderFilesCount = folderFiles.length;
        for (int i = 0; i < folderFilesCount; i++) {
            if (folderFiles[i].getName().compareTo(fileName) == 0) {
                return true;
            }
        }
        return false;
    }

    //restart transaction and sis server
    public boolean restartTransactionAndDatabase(QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String targetThesaurusName) {
DBGeneral dbGen = new DBGeneral();
        if(Utils.ConstantParameters.DEVELOPING){
            
                        Q.TEST_end_transaction();
                        Utils.StaticClass.closeDb();
                        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, targetThesaurusName, false)==QClass.APIFail){
                            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ ");
                            return false;
                        }
            return true;
        }
        if(!Utils.ConstantParameters.DEVELOPING){
            Q.TEST_end_transaction();
                        Utils.StaticClass.closeDb();
                        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, targetThesaurusName, false)==QClass.APIFail){
                            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ ");
                            return false;
                        }
            return true;
        }
        
        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "restartTransactionAndDatabase");


        //start it and wait until it is started
        boolean serverStarted = StartDatabase();
        if (serverStarted == false) {
            String StartServerFailure = config.GetTranslation("StartServerFailure");
            if (Parameters.DEBUG) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + StartServerFailure);
            }
            //CreateThesaurusResultMessage.setValue(StartServerFailure + " " + DatabaseBatFileDirectory + "\\" + DatabaseBatFileName);
            RestartDatabaseIfNeeded();
            return false;
        }

        
        

        dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurusName, false);
        return true;
    }
}
