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

import Admin_Thesaurus.DBEditGuideTerms;
import DB_Classes.DBCreate_Modify_Hierarchy;
import DB_Classes.DBCreate_Modify_Facet;
import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBGeneral;
import DB_Classes.DBConnect_Term;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;

import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import neo4j_sisapi.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.neo4j.io.fs.FileUtils;

/**
 *
 * @author tzortzak
 */
public class DBAdminUtilities {

    private static final String LockFileName = "SystemLocked.txt";
    private static boolean SYSTEM_IS_LOCKED = false; // true in case the system is locked by an administrator for doing administrative jobs

    File Neo4jInitializeDBTsvFile; // g.e. "C:\Projects\THEMAS_DB_Folder\TSVs\System\Generic.tsv"
    File Neo4jCreateNewThesaurusSpecificTsv; // g.e. "C:\Projects\THEMAS_DB_Folder\TSVs\System\Specific_el.tsv"

    public static boolean isSystemLocked() {
        return SYSTEM_IS_LOCKED || lockFileExists();
    }

    private static boolean lockFileExists() {
        String fullLockFilenamePath = Parameters.BaseRealPath + File.separator + "DBadmin" + File.separator + DB_Admin.DBAdminUtilities.LockFileName;
        //Utils.StaticClass.webAppSystemOutPrintln(fullLockFilenamePath);
        File f = new File(fullLockFilenamePath);
        if (f.exists() && !f.isDirectory()) {
            SYSTEM_IS_LOCKED = true;
            f = null;
            //Utils.StaticClass.webAppSystemOutPrintln("System lock file found " +fullLockFilenamePath );

            return true;
        }
        //Utils.StaticClass.webAppSystemOutPrintln("System lock file NOT found ");
        /*else{

         }
         //DB_Admin.DBAdminUtilities.LockFileName;
         //Parameters.SYSTEM_IS_LOCKED = true;
         * 
         */
        return false;
    }

    public static void setLockVariable(boolean value) {
        SYSTEM_IS_LOCKED = value;

        String fullLockFilenamePath = Parameters.BaseRealPath + File.separator + "DBadmin" + File.separator + DBAdminUtilities.LockFileName;
        if (value) {

            try {
                File f = new File(fullLockFilenamePath);
                if (!f.exists()) {
                    OutputStreamWriter logFileWriter = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fullLockFilenamePath)), "UTF-8");
                    logFileWriter.append(Parameters.LogFilePrefix + "LOCKED for the first time at: " + Utilities.GetNow());
                    logFileWriter.flush();
                    logFileWriter.close();
                } else {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Lock File already Found");
                }
            } catch (Exception ex) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }

            //create file if does not exist
        } else {
            File f = new File(fullLockFilenamePath);
            if (f.exists()) {
                f.delete();
                f = null;
            }
            //delete file if exists
        }
    }
    /*----------------------------------------------------------------------
     LockSystemForAdministrativeJobs()
     ------------------------------------------------------------------------*/

    public void LockSystemForAdministrativeJobs(ConfigDBadmin config) {
        // create the common-utils class
        /*
         CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
         DBGeneral dbGen = new DBGeneral();
         // restart server if it is stopped
         common_utils.RestartDatabaseIfNeeded();

         QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
         IntegerObject sis_session = new IntegerObject();
         IntegerObject tms_session = new IntegerObject();

        
         //open connection and start Transaction
         if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, null, false)==QClass.APIFail)
         {
         Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBAdminUtilities LockSystemForAdministrativeJobs");
         return;
         }
         // BEGIN TRANSACTION so as to wait all pending transactions by other sessions to be finished!
         Q.TEST_begin_transaction();
         */
        // wait until an other administrative job being started before this, is finished
        /*
         while (DB_Admin.DBAdminUtilities.isSystemLocked() == true) {
         //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"An other administrative job is being executed. Please wait...");
         }
         */
        // lock the system
        setLockVariable(true);

        //commit transaction and close connection
        /*
         Q.free_all_sets();
         Q.TEST_end_transaction();
         dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

         */
        /*
         // check if server runs (close it before creating new thesaurus)
         boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         if (databaseIsRunning == true) {
         common_utils.StopDatabase();
         // wait until server is finally stopped
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         while (databaseIsRunning == true) {
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         }
         }

         // stop server
         boolean serverStopped = common_utils.StopDatabase();
         if (serverStopped == false) {
         common_utils.RestartDatabaseIfNeeded();
         }

         // after locking the system and in case SIS server is not running, restart it
         common_utils.RestartDatabaseIfNeeded();
         */
    }

    /*----------------------------------------------------------------------
     UnlockSystemForAdministrativeJobs()
     ------------------------------------------------------------------------*/
    public void UnlockSystemForAdministrativeJobs() {
        setLockVariable(false);
    }


    /*----------------------------------------------------------------------
     DBCanBeInitialized()
     ------------------------------------------------------------------------*/
    public boolean DBCanBeInitialized(ConfigDBadmin config, CommonUtilsDBadmin common_utils, String NewThesaurusNameDBformatted, StringObject InitializeDBResultMessage, Boolean DBInitializationSucceded) {

        // check if the SISInitializeDBTelosSourcesDirectory configuration directory
        // (g.e. "C:\ICS-FORTH\TMS-EKT\telos_sources\model\generic"), exists
        Neo4jInitializeDBTsvFile = new File(common_utils.Neo4j_GenericTsvFile);
        boolean fileExists = (Neo4jInitializeDBTsvFile.exists() && !Neo4jInitializeDBTsvFile.isDirectory());
        //boolean SISInitializeDBTelosSourcesDirectoryExists = SISInitializeDBTelosSourcesDirectory.isDirectory();
        if (fileExists == false) {
            String InvalidConfigValue = common_utils.config.GetTranslation("InvalidConfigValue");
            InitializeDBResultMessage.setValue(InvalidConfigValue + ": " + common_utils.Neo4j_GenericTsvFile);
            DBInitializationSucceded = false;
            return false;
        }

        // construct specific bat file
        //CreateBatFile(config, common_utils, NewThesaurusNameDBformatted, CREATE_BAT_FILE_FOR_INIT_DB);
        return true;
    }

    /*----------------------------------------------------------------------
     GivenThesaurusCanBeCreated()
     ------------------------------------------------------------------------*/
    public boolean GivenThesaurusCanBeCreated(ConfigDBadmin config, CommonUtilsDBadmin common_utils,
            Vector thesaurusVector, String NewThesaurusName, String NewThesaurusNameDBformatted,
            StringObject CreateThesaurusResultMessage, Boolean CreateThesaurusSucceded) {
        // check if the given NewThesaurusName exists
        boolean exists = thesaurusVector.contains(NewThesaurusName);
        if (exists == true) {
            String ThesaurusExists = common_utils.config.GetTranslation("ThesaurusExists");
            CreateThesaurusResultMessage.setValue(ThesaurusExists);
            CreateThesaurusSucceded = false;
            return false;
        }
        // check if the given NewThesaurusName has only 1 character
        boolean OneCharacter = (NewThesaurusName.length() == 1);
        if (OneCharacter == true) {
            String OneCharacterMessage = common_utils.config.GetTranslation("OneCharacterMessage");
            CreateThesaurusResultMessage.setValue(OneCharacterMessage);
            CreateThesaurusSucceded = false;
            return false;
        }
        // check if the given NewThesaurusName contains invalid characters
        boolean ContainsInvalidCharacters = common_utils.FileNameContainsInvalidCharacters(NewThesaurusNameDBformatted);
        if (ContainsInvalidCharacters == true) {
            String ThesaurusContainsInvalidCharacters = common_utils.config.GetTranslation("ThesaurusContainsInvalidCharacters");
            CreateThesaurusResultMessage.setValue(ThesaurusContainsInvalidCharacters);
            CreateThesaurusSucceded = false;
            return false;
        }

        File Neo4jSpecificTsvFile = new File(common_utils.Neo4j_SpecificTsvFile);
        boolean fileExists = (Neo4jSpecificTsvFile.exists() && !Neo4jSpecificTsvFile.isDirectory());
        //boolean SISInitializeDBTelosSourcesDirectoryExists = SISInitializeDBTelosSourcesDirectory.isDirectory();
        if (fileExists == false) {
            String InavalidConfigValue = common_utils.config.GetTranslation("InvalidConfigValue");
            CreateThesaurusResultMessage.setValue(InavalidConfigValue + ": " + common_utils.Neo4j_SpecificTsvFile);
            CreateThesaurusSucceded = false;
            return false;
        }
        //copy File / delete first if exists
        String parentDirectoryPath = common_utils.Neo4jExportTsvsFileDirectory;
        if (parentDirectoryPath.endsWith("/") == false) {
            parentDirectoryPath += "/";
        }

        String copySpecificFilePath = parentDirectoryPath + "Specific_" + NewThesaurusNameDBformatted + ".tsv";
        File specificForParse = new File(copySpecificFilePath);
        if (specificForParse.exists()) {
            specificForParse.delete();
        }
        specificForParse = null;
        //copy and replace in new file path
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(common_utils.Neo4j_SpecificTsvFile), "UTF-8"));
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(copySpecificFilePath)), "UTF-8");

            String linestr;

            while ((linestr = in.readLine()) != null) {
                String newLineStr = linestr;
                newLineStr = newLineStr.replace("SIS_XXXXX_SIS", NewThesaurusNameDBformatted);
                newLineStr = newLineStr.replace("SIS_Xxxxx_SIS", NewThesaurusNameDBformatted.substring(0, 1) + NewThesaurusNameDBformatted.substring(1).toLowerCase());
                newLineStr = newLineStr.replace("SIS_xxxxx_SIS", NewThesaurusNameDBformatted.toLowerCase());
                newLineStr = newLineStr.replace("SIS_zzzzz_SIS", common_utils.PrimaryLanguagePrefix);
                newLineStr = newLineStr.replace("SIS_ZZZZZ_SIS", common_utils.PrimaryLanguage);

                out.append(newLineStr + "\r\n");
                out.flush();
            }
            out.close();
            in.close();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DBAdminUtilities.class.getName()).log(Level.SEVERE, null, ex);
            CreateThesaurusResultMessage.setValue("Can non create file : " + copySpecificFilePath);
            CreateThesaurusSucceded = false;
            return false;
        } catch (IOException ex) {
            Logger.getLogger(DBAdminUtilities.class.getName()).log(Level.SEVERE, null, ex);
            CreateThesaurusResultMessage.setValue("Can non create file : " + copySpecificFilePath);
            CreateThesaurusSucceded = false;
            return false;
        }

        Neo4jCreateNewThesaurusSpecificTsv = new File(copySpecificFilePath);

        /*
         // copy SISCreateThesaurusTelosSourcesDirectory to SISCreateThesaurusTelosSourcesDirectoryNEW
         // g.e. "C:\ICS-FORTH\TMS-EKT\telos_sources\model\specific_thes_EKT"
         String sourceDir = common_utils.SISCreateThesaurusTelosSourcesDirectory;
         String destinationDir = common_utils.SISCreateThesaurusTelosSourcesDirectory + "_" + NewThesaurusNameDBformatted;
         boolean copySucceded = common_utils.CopyFolder(new File(sourceDir), new File(destinationDir));
         if (copySucceded == false) {
            
         CreateThesaurusResultMessage.setValue(CannotCreateDirectory + ": " + destinationDir);
         CreateThesaurusSucceded = false;
         return false;
         }
         SISCreateThesaurusTelosSourcesDirectoryNEW = new File(destinationDir);

         // construct specific bat file
         CreateBatFile(config, common_utils, NewThesaurusNameDBformatted, CREATE_BAT_FILE_FOR_CREATE_THESAURUS);
         */
        // replace TELOS keywords in each tls file of folder: SISCreateThesaurusTelosSourcesDirectoryNEW
        // g.e. "C:\ICS-FORTH\TMS-EKT\telos_sources\model\specific_thes_EKT"
        /* Replacements:
         SIS_XXXXX_SIS -> g.e. EKT
         SIS_Xxxxx_SIS -> g.e. Ekt
         SIS_xxxxx_SIS -> g.e. ekt
         SIS_zzzzz_SIS -> g.e. EL
         SIS_ZZZZZ_SIS -> g.e. Greek
         */
        /*
         String TELOS_Keywords[] = {"SIS_XXXXX_SIS", "SIS_Xxxxx_SIS", "SIS_xxxxx_SIS", "SIS_zzzzz_SIS", "SIS_ZZZZZ_SIS"};
         String THESAURUS_Keywords[] = new String[5];
         THESAURUS_Keywords[0] = NewThesaurusNameDBformatted; // g.e. EKT
         THESAURUS_Keywords[1] = NewThesaurusNameDBformatted.substring(0, 1) + NewThesaurusNameDBformatted.substring(1).toLowerCase();
         THESAURUS_Keywords[2] = NewThesaurusNameDBformatted.toLowerCase();
         THESAURUS_Keywords[3] = common_utils.TELOSPrimaryLanguagePrefix;//"EL"; // future todo: to be input by the user
         THESAURUS_Keywords[4] = common_utils.TELOSPrimaryLanguage; // future todo: to be related with the above one
         common_utils.ReplaceTelosKeywordsInFolder(SISCreateThesaurusTelosSourcesDirectoryNEW, TELOS_Keywords, THESAURUS_Keywords);
         */
        return true;
    }

    /*-----------------------------------------------------
     CreateThesaurus()
     * DBbackupFileNameCreated: Used in order to store back Up file name. If not null its value will be used as a backup description
     -------------------------------------------------------*/
    public boolean CreateThesaurus(CommonUtilsDBadmin common_utils, String NewThesaurusNameDBformatted, StringObject CreateThesaurusResultMessage, String backUpDescrition, StringObject DBbackupFileNameCreated) {
        // check if server runs (close it before creating new thesaurus)
        /*
         boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         if (databaseIsRunning == true) {
         common_utils.StopDatabase();
         // wait until server is finally stopped
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         while (databaseIsRunning == true) {
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         }
         }
         */

        // create a backup of the data base anyway
        common_utils.CreateDBbackup(backUpDescrition, CreateThesaurusResultMessage, DBbackupFileNameCreated);

        /*// start server
         boolean serverStarted = common_utils.StartDatabase();
         if (serverStarted == false) {
         String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
         CreateThesaurusResultMessage.setValue(StartServerFailure + " " + common_utils.DatabaserBatFileDirectory + File.separator + common_utils.DatabaseBatFileName);
         common_utils.RestartDatabaseIfNeeded();
         return false;
         }
         // wait until server is finally started
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         while (databaseIsRunning == false) {
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         }
         */
        TSVExportsImports expimps = new TSVExportsImports();
        if (expimps.importSpecificFromFile(Neo4jCreateNewThesaurusSpecificTsv.getAbsolutePath(),false) == false) {

            StringObject result = new StringObject("");
            boolean RestoreDBbackupSucceded = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);
            CreateThesaurusResultMessage.setValue("Can not create file : " + Neo4jCreateNewThesaurusSpecificTsv.getAbsolutePath() + "\r\n" + result.getValue());
            return false;
        }
        /*
         // get the modification date of db folder contents before telos parsing
         long DBmodificationBeforeTelos = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);
         // call telos for TelosFileName x N times
         Vector<String> tlsFiles = new Vector<String>();
         StringObject telosOutputObj = new StringObject("");
         fillTlsFilesVector(tlsFiles, common_utils, NewThesaurusNameDBformatted, CREATE_BAT_FILE_FOR_CREATE_THESAURUS);

         for (int i = 0; i < common_utils.SISCreateThesaurusTelosParseTimes; i++) {
         //initialize error msg
         telosOutputObj.setValue("");

         // start telos
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "--------------------- sisTelosIs STARTED ---------------------" + (i + 1));
         boolean telosStarted = common_utils.StartSIStelos(tlsFiles, telosOutputObj);
         if (telosStarted == false) {
         String StartTelosFailure = common_utils.config.GetTranslation("StartTelosFailure");
         CreateThesaurusResultMessage.setValue(StartTelosFailure + " " + common_utils.SISTelosBatFileDirectory + File.separator + common_utils.SISTelosBatFileName);
         return false;
         }
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "--------------------- sisTelosIs FINISHED --------------------" + (i + 1));
         }*/

        /*
         boolean serverStopped = false;
         // get the modification date of db folder contents after telos parsing
         long DBmodificationAfterTelos = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);
         //checking last tls parsing result
         //Utils.StaticClass.webAppSystemOutPrintln(telosOutputObj.getValue());            
         boolean telosSucceded = common_utils.SIStelosSucceded(telosOutputObj);

         // in case of error
         if (DBmodificationAfterTelos == DBmodificationBeforeTelos || telosSucceded == false) {

         String TelosFailed = common_utils.config.GetTranslation("TelosFailed");
         String DBRestoration = common_utils.config.GetTranslation("DBRestoration");
         CreateThesaurusResultMessage.setValue(TelosFailed + " " + telosOutputObj.getValue() + ". " + DBRestoration);

         // do the restoration of the initial backup
         StringObject result = new StringObject("");
         boolean RestoreDBbackupSucceded = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);

         // delete the new created file: SISCreateThesaurusBatFileNameNEW 
         // g.e. C:\ICS-FORTH\TMS-EKT\WEB_TMS_script_for_CreateThesaurus_EKT.bat
         SISCreateThesaurusBatFileNameNEW.delete();
         // delete the new created folder: SISCreateThesaurusTelosSourcesDirectoryNEW
         // g.e. "C:\ICS-FORTH\TMS-EKT\telos_sources\model\specific_thes_EKT"
         CommonUtilsDBadmin.DeleteFolder(SISCreateThesaurusTelosSourcesDirectoryNEW);

         // stop server
         serverStopped = common_utils.StopDatabase();
         common_utils.RestartDatabaseIfNeeded();
         return false;
         }
         */
        // inform user for success
        String CreateThesaurusSuccess = common_utils.config.GetTranslation("CreateThesaurusSuccess");
        CreateThesaurusResultMessage.setValue(CreateThesaurusSuccess);
        return true;
    }

    /*-----------------------------------------------------
     InitializeDB()
     -------------------------------------------------------*/
    public boolean InitializeDB(CommonUtilsDBadmin common_utils, StringObject InitializeDBResultMessage) {

        //Vector<String> tlsFiles = new Vector<String>();
        //fillTlsFilesVector(tlsFiles, common_utils, null, CREATE_BAT_FILE_FOR_INIT_DB);
        // check if server runs (close it before creating DB initialization)
        /*
         boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         if (databaseIsRunning == true) {
         common_utils.StopDatabase();
         // wait until server is finally stopped
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         while (databaseIsRunning == true) {
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         }
         }*/
        // create a backup of the data base anyway
        StringObject DBbackupFileNameCreated = new StringObject("");
        common_utils.CreateDBbackup("backup_before_DB_initialization", InitializeDBResultMessage, DBbackupFileNameCreated);

        Utils.StaticClass.closeDb();
        // check if server runs (close it before deleting DB folder contents)
        /*databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         if (databaseIsRunning == true) {
         common_utils.StopDatabase();
         // wait until server is finally stopped
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         while (databaseIsRunning == true) {
         databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
         }
         }*/

        // clear db folder contents
        boolean dbIsCleared = common_utils.DeleteFolderContents(common_utils.DBPath);
        if (dbIsCleared == false) {
            String ClearDBFolderFailure = common_utils.config.GetTranslation("ClearDBFolderFailure");
            InitializeDBResultMessage.setValue(ClearDBFolderFailure + " " + common_utils.DBPath);
            //common_utils.RestartDatabaseIfNeeded();
            boolean RestoreDBbackupSucceded = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), new StringObject());
            return false;
        }

        // start server with empty data base folder
        Utils.StaticClass.getDBService();

        //import generic file
        String genericTSVFile = common_utils.Neo4j_GenericTsvFile;
        TSVExportsImports expimps = new TSVExportsImports();
        if (expimps.importGenericFromFile(genericTSVFile,false) == false) {
            InitializeDBResultMessage.setValue("Generic Import Failed");
            boolean RestoreDBbackupSucceded = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), new StringObject());

            return false;
        }
        /*
         // get the modification date of db folder contents before telos parsing
         long DBmodificationBeforeTelos = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);
         // call telos for TelosFileName x N times
         StringObject telosOutputObj = new StringObject("");
         for (int i = 0; i < common_utils.SISInitializeDBTelosParseTimes; i++) {

         // start telos    
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "--------------------- sisTelosIs STARTED ---------------------" + (i + 1));
         boolean telosStarted = common_utils.StartSIStelos(tlsFiles, telosOutputObj);

         if (telosStarted == false) {
         String StartTelosFailure = common_utils.config.GetTranslation("StartTelosFailure");
         InitializeDBResultMessage.setValue(StartTelosFailure + " " + common_utils.SISTelosBatFileDirectory + File.separator + common_utils.SISTelosBatFileName);
         return false;
         }

         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "--------------------- sisTelosIs FINISHED --------------------" + (i + 1));
         }
         */

        /*
         boolean serverStopped = false;
         // get the modification date of db folder contents after telos parsing
         long DBmodificationAfterTelos = common_utils.GetFolderContentsModificationDate(common_utils.DBPath);
         //checking last tls parsing result
         boolean telosSucceded = common_utils.SIStelosSucceded(telosOutputObj);
        
         // in case of error
         if (DBmodificationAfterTelos == DBmodificationBeforeTelos || telosSucceded == false) {
         String TelosFailed = common_utils.config.GetTranslation("TelosFailed");
         String DBRestoration = common_utils.config.GetTranslation("DBRestoration");
         InitializeDBResultMessage.setValue(TelosFailed + " " + telosOutputObj.getValue() + ". " + DBRestoration);

         // do the restoration of the initial backup
         StringObject result = new StringObject("");
            
         // stop server
         serverStopped = common_utils.StopDatabase();
         common_utils.RestartDatabaseIfNeeded();
         return false;
         }

        
        
        

         // after finishing the job and in case SIS server is not running, restart it
         common_utils.RestartDatabaseIfNeeded();

        
        
         */
        // start stop server in order to commit changes
        Utils.StaticClass.closeDb();
        Utils.StaticClass.getDBService();

        // inform user for success
        String InitializeDBSuccess = common_utils.config.GetTranslation("InitializeDBSuccess");
        InitializeDBResultMessage.setValue(InitializeDBSuccess + " ");
        return true;
    }

    public void step2ofDeletion(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, StringObject targetDescriptorObj, String targetDescriptorUTF8, StringObject errorMsg) {
        DBGeneral dbGen = new DBGeneral();
        DBConnect_Term dbCon = new DBConnect_Term();

        StringObject scopenoteFromClassObj = new StringObject();
        StringObject scopenoteLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);

        StringObject scopenoteENFromClassObj = new StringObject();
        StringObject scopenoteENLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenoteENFromClassObj, scopenoteENLinkObj, Q, sis_session);

        StringObject commentFromClassObj = new StringObject();
        StringObject commentLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.comment_kwd, commentFromClassObj, commentLinkObj, Q, sis_session);

        StringObject historicalnoteFromClassObj = new StringObject();
        StringObject historicalnoteLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
        //DELETE ALL TERM RELATIONS EXCEPT BT RELATIONS
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(selectedThesaurus) == false) {
            TA.SetThesaurusName(selectedThesaurus);
        }
        TA.DeleteDescriptorComment(targetDescriptorObj, scopenoteFromClassObj, scopenoteLinkObj);
        TA.DeleteDescriptorComment(targetDescriptorObj, scopenoteENFromClassObj, scopenoteENLinkObj);
        TA.DeleteDescriptorComment(targetDescriptorObj, commentFromClassObj, commentLinkObj);
        TA.DeleteDescriptorComment(targetDescriptorObj, historicalnoteFromClassObj, historicalnoteLinkObj);

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(selectedThesaurus) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }

        //collect all links from targetNote that are not BT links in order to delete them
        Q.reset_name_scope();
        Q.set_current_node(targetDescriptorObj);
        int set_link_from = Q.get_link_from(0);
        Q.reset_set(set_link_from);

        dbCon.delete_term_links_by_set(selectedThesaurus, targetDescriptorUTF8, ConstantParameters.FROM_Direction, set_link_from, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

        Q.reset_name_scope();
        long idL = Q.set_current_node(targetDescriptorObj);
        //collect all links to targetNode that are not BT links
        int set_link_to = Q.get_link_to(0);
        Q.reset_set(set_link_to);

        dbCon.delete_term_links_by_set(selectedThesaurus, targetDescriptorUTF8, ConstantParameters.TO_Direction, set_link_to, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

        Q.free_set(set_link_from);
        Q.free_set(set_link_to);

        Q.CHECK_Delete_Node(new Identifier(idL));

    }

    /*---------------------------------------------------------------------
     DeleteThesaurus()
     -----------------------------------------------------------------------
     INPUT: - String targetThesaurus: the name of the thesaurus to be deleted (DB formatted and without prefix)
     OUTPUT: StringObject errorMsg: with the result of the deletion
     FUNCTION: deletes the target thesaurus
     CALLED BY: DeleteThesaurus servlet
     ----------------------------------------------------------------------*/
    public void DeleteThesaurus(HttpServletRequest request, QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            IntegerObject tms_session, DBGeneral dbGen,
            String targetThesaurus, StringObject errorMsg) {

        Utilities u = new Utilities();
        // timer begin
        long startTime = Utilities.startTimer();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "############ DELETION of thesaurus: " + targetThesaurus + " STARTED ############");

        int ret = 0;
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session, request);
        int SISApiSession = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        UsersClass tmsUsers = new UsersClass();
        // construct targetThesaurusObj StringObject with prefix        
        StringObject targetThesaurusObj = new StringObject("Thesaurus`" + targetThesaurus);
        // ATTENTION!!!: all queries must be done AFTER the call to tmsUsers.EditUserThesaurus()
        // so as the selected thesaurus for deletion is set as selectedThesaurus of current SessionUserInfo
        // and the DB thesaurus references are associated with this thesaurus

        // check if current user has access to the given thesaurus
        UserInfoClass refSessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String targetUser = refSessionUserInfo.name;
        ret = tmsUsers.EditUserThesaurus(request, session, sessionInstance, targetUser, targetThesaurus);
        switch (ret) {
            case UsersClass.USER_NAME_DOES_NOT_EXIST:
                
                errorMsg.setValue(u.translateFromMessagesXML("root/DBAdminUtilities/DeleteThesaurus/USER_NAME_DOES_NOT_EXIST", new String[]{targetUser}));
                //errorMsg.setValue("The renaming user: '" + targetUser + "' could not be found as a user of the system.");
                return;
            case UsersClass.AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED:
                
                errorMsg.setValue(u.translateFromMessagesXML("root/DBAdminUtilities/DeleteThesaurus/AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED", new String[]{targetUser,targetThesaurus}));
                //errorMsg.setValue(User '" + targetUser + "' does not have permission to delete the thesaurus '" + targetThesaurus+ "'.");
                // ATTENTION: the following is necessary so as to restore the old valid state of the "SessionUser" session attribute
                sessionInstance.setAttribute("SessionUser", refSessionUserInfo);
                return;
        }
        // check if target thesaurus exists
        Q.reset_name_scope();
        if (Q.set_current_node(targetThesaurusObj) == QClass.APIFail) {
            
            errorMsg.setValue(u.translateFromMessagesXML("root/DBAdminUtilities/DeleteThesaurus/ThesaurusNotFound", new String[]{targetThesaurus}));
            //errorMsg.setValue(errorMsg.getValue().concat("Thesaurus '" + targetThesaurus + "' does not exist in database."));
            return;
        }

        //upda
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        tmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, targetThesaurus);
        // ATTENTION!!!: all queries must be done AFTER the call to tmsUsers.EditUserThesaurus()
        // so as the selected thesaurus for deletion is set as selectedThesaurus of current SessionUserInfo
        // and the DB thesaurus references are associated with this thesaurus
        // looking for AAAHierarchy
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesHierarchy);
        // looking for AAAFacet
        StringObject thesFacet = new StringObject();
        dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesFacet);
        // looking for hierarchy prefix AAAClass`
        String hierarchyPrefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, SISApiSession);
        // looking for descriptor prefix AAAEL`
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, SISApiSession);
        // looking for AAAClass`Unclassified terms
        StringObject orphansHierarchyObj = new StringObject(hierarchyPrefix.concat(Parameters.UnclassifiedTermsLogicalname));
        // looking for AAAEN`Unclassified terms
        StringObject orphansHierarchyTopTermObj = new StringObject(orphansHierarchyObj.getValue().replaceFirst(hierarchyPrefix, termPrefix));
        // looking for AAAClass`UNCLASSIFIED TERMS
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyTopTermObj);

        //Find out Orphan Terms facet --> default facet
        //StringObject orphansHierarchyFacet = new StringObject(hierarchyPrefix + "UNCLASSIFIED TERMS");
        StringObject orphansHierarchyFacet = new StringObject();
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyObj);
        int set_classes = Q.get_superclasses(0);
        Q.reset_set(set_classes);

        int index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        int set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);

        Q.set_intersect(set_classes, set_facets);
        Q.reset_set(set_classes);

        Q.free_set(set_facets);

        //StringObject nodeName = new StringObject();
        Q.reset_set(set_classes);
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_classes, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                orphansHierarchyFacet.setValue(row.get_v1_cls_logicalname());
            }
        }
        /*while(Q.retur_nodes(set_classes, nodeName)!=QClass.APIFail){
         orphansHierarchyFacet.setValue(nodeName.getValue());
         }*/
        Q.free_set(set_classes);
        String testFacet = orphansHierarchyFacet.getValue();
        //SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"DeleteThesaurus(" + targetThesaurus + ") and current SessionUserInfo selectedThesaurus = " + SessionUserInfo.selectedThesaurus);

        //0 DELETE GUIDE TERMS FROM THESAURUS
        DBEditGuideTerms editGuideTerms = new DBEditGuideTerms();
        Vector<String> guideTerms = dbGen.collectGuideLinks(SessionUserInfo.selectedThesaurus, Q, sis_session);
        for (int i = 0; i < guideTerms.size(); i++) {
            String GuideTermForDeletion = guideTerms.get(i);
            editGuideTerms.deleteGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session, GuideTermForDeletion, errorMsg);
            if (errorMsg.getValue().equals("") == false) {
                return;
            }
        }

        // 1. --- delete all hierarchies of thesaurus AAA EXCEPT{AAAClass`Unclassfied terms} ---
        // a. get all hierarchies of thesaurus AAA (ALL instances of AAAHierarchy) EXCEPT{AAAClass`Unclassfied terms}
        Q.reset_name_scope();
        Q.set_current_node(thesHierarchy);
        int hierarchiesSet = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"hierarchiesSet card = " + Q.set_get_card( hierarchiesSet));
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyObj);
        int setWithOrphansHierarchy = Q.set_get_new();
        Q.set_put(setWithOrphansHierarchy);
        Q.set_difference(hierarchiesSet, setWithOrphansHierarchy);
        Q.free_set(setWithOrphansHierarchy);
        int hierarchiesSetWithoutOrphansHierarchy = hierarchiesSet;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"hierarchiesSetWithoutOrphansHierarchy card = " + Q.set_get_card( hierarchiesSetWithoutOrphansHierarchy));
        Vector<String> hierarchiesToBeDeleted = new Vector<String>();
        Q.reset_set(hierarchiesSetWithoutOrphansHierarchy);
        retVals.clear();
        if (Q.bulk_return_nodes(hierarchiesSetWithoutOrphansHierarchy, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                hierarchiesToBeDeleted.add(row.get_v1_cls_logicalname());
            }
        }
        //StringObject name = new StringObject();
        //while (Q.retur_nodes(hierarchiesSetWithoutOrphansHierarchy, name) != QClass.APIFail) {
        //    hierarchiesToBeDeleted.add(name.getValue());
        //}
        Q.free_set(hierarchiesSetWithoutOrphansHierarchy);
        // b. delete all hierarchies of thesaurus AAA EXCEPT{AAAClass`Unclassfied terms}
        DBCreate_Modify_Hierarchy creation_modificationOfHierarchy = new DBCreate_Modify_Hierarchy();
        int hierarchiesToBeDeletedSize = hierarchiesToBeDeleted.size();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deleting " + hierarchiesToBeDeletedSize + " hierarchies...");
        for (int i = 0; i < hierarchiesToBeDeletedSize; i++) {
            String hierarchyToBeDeleted = (String) hierarchiesToBeDeleted.get(i);
            String hierarchyToBeDeletedUIWithoutPrefix = dbGen.removePrefix(hierarchyToBeDeleted);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + (i + 1) + ". Delete hierarchy: " + hierarchyToBeDeletedUIWithoutPrefix);
            /*if(i==5){
             System.out.println("DEBUG");
             }*/
            creation_modificationOfHierarchy.DeleteHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchyToBeDeletedUIWithoutPrefix, errorMsg);
            if (errorMsg.getValue().equals("") == false) {
                return;
            }
        }

        /*
         // 2. --- clean the terms of hierarchy AAAClass`Unclassfied terms EXCEPT{AAAEN`Unclassfied terms} without deleting this hieracrchy itself
        
         boolean continueOrphanDeletion = true;
         Q.reset_name_scope();
        
         StringObject btFromClassObj = new StringObject();
         StringObject btLinkObj = new StringObject();
         dbGen.getKeywordPair(sessionInstance, ConstantParameters.bt_kwd, btFromClassObj, btLinkObj, Q, sis_session);
        
         int howmanyOrphanDeletionCounter =0;
         DBConnect_Term dbCon = new DBConnect_Term();
         DBCreate_Modify_Term DBCMT = new DBCreate_Modify_Term();
         String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(sessionInstance, Q, sis_session.getValue());
        
         Vector<String> oldTopTerms = new Vector<String>();
         oldTopTerms.add(Parameters.UnclassifiedTermsLogicalname);
        
         Q.reset_name_scope();
         int set_orphan_top_term_only = Q.set_get_new();
         Q.reset_set(set_orphan_top_term_only);
        
        
         Q.set_current_node(orphansHierarchyTopTermObj);
         Q.set_put(set_orphan_top_term_only);
         Q.reset_set(set_orphan_top_term_only);
        
         while(continueOrphanDeletion){
         Q.reset_name_scope();
         Q.set_current_node(orphansHierarchyObj);
         int set_all_orphans = Q.get_instances(0);
         Q.reset_set(set_all_orphans);
         if(Q.set_get_card(set_all_orphans)==1){
         Q.free_set(set_all_orphans);
         continueOrphanDeletion=false;
         continue;
         }
         Q.set_difference(set_all_orphans,set_orphan_top_term_only);
         Q.reset_set(set_all_orphans);
         if(howmanyOrphanDeletionCounter==0){
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Starting deletion of " + Q.set_get_card(set_all_orphans) + " orphan terms...");
         }
         int set_non_leaf_nodes_labels = Q.get_link_from_by_category(set_all_orphans, btFromClassObj, btLinkObj);
         Q.reset_set(set_non_leaf_nodes_labels);
        
         int set_non_leaf_nodes = Q.get_to_value(set_non_leaf_nodes_labels);
         Q.reset_set(set_non_leaf_nodes);
        
         int set_leaf_nodes = Q.set_get_new();
         Q.reset_set(set_leaf_nodes);
        
        
         Q.set_copy(set_leaf_nodes, set_all_orphans);
         Q.reset_set(set_leaf_nodes);
        
         Q.set_difference(set_leaf_nodes,set_non_leaf_nodes);
         Q.reset_set(set_leaf_nodes);
        
         Vector<String> termsForDeletion = new Vector<String>();
         termsForDeletion.addAll(dbGen.get_Node_Names_Of_Set(set_leaf_nodes, true, Q, sis_session));
        
         Q.free_set(set_leaf_nodes);
         Q.free_set(set_non_leaf_nodes);
         Q.free_set(set_non_leaf_nodes_labels);
         Q.free_set(set_all_orphans);
         for (int k = 0; k < termsForDeletion.size(); k++) {
         howmanyOrphanDeletionCounter++;
         String LeafTermForDeletionUTF8 = termsForDeletion.get(k);
         StringObject LeafTermForDeletionObj = new StringObject(prefixTerm.concat(LeafTermForDeletionUTF8));
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+howmanyOrphanDeletionCounter  + ". Delete orphanTerm: " + LeafTermForDeletionUTF8);
         if (DBCMT.deleteDescriptor(sessionInstance, Q, sis_session, TA, tms_session,
         dbGen, dbCon, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, LeafTermForDeletionObj,
         LeafTermForDeletionUTF8, errorMsg, oldTopTerms) == false) {
         Q.free_set(set_orphan_top_term_only);
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- orphan term deletion failed");
         return;
         }
         }
        
         }
         Q.free_set(set_orphan_top_term_only);
        
         */
        // 2. --- clean the terms of hierarchy AAAClass`Unclassfied terms EXCEPT{AAAEN`Unclassfied terms} without deleting this hieracrchy itself
        // a. get all terms of (hierarchy AAAClass`Unclassfied terms) EXCEPT{AAAEN`Unclassfied terms}
        /*
         Q.end_transaction();
         //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"DEBUG PURPOSE");
         if(errorMsg.getValue().length()==0){
         return;
         }
         Q.begin_transaction();
         */
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyObj);
        int orphanTermsSet = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"orphanTermsSet card = " + Q.set_get_card( orphanTermsSet));        
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyTopTermObj);
        int setWithOrphansTopTerm = Q.set_get_new();
        Q.set_put(setWithOrphansTopTerm);
        Q.set_difference(orphanTermsSet, setWithOrphansTopTerm);
        Q.free_set(setWithOrphansTopTerm);
        int orphanTermsSetWithoutOrphansTopTerm = orphanTermsSet;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"orphanTermsSetWithoutOrphansTopTerm card = " + Q.set_get_card( orphanTermsSetWithoutOrphansTopTerm));        
        Vector<String> orphanTermsToBeDeleted = new Vector<String>();
        Q.reset_set(orphanTermsSetWithoutOrphansTopTerm);
        retVals.clear();
        if (Q.bulk_return_nodes(orphanTermsSetWithoutOrphansTopTerm, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                orphanTermsToBeDeleted.add(row.get_v1_cls_logicalname());
            }
        }
        /*name = new StringObject();
         while (Q.retur_nodes(orphanTermsSetWithoutOrphansTopTerm, name) != QClass.APIFail) {
         orphanTermsToBeDeleted.add(name.getValue());
         }*/
        Q.free_set(orphanTermsSetWithoutOrphansTopTerm);
        // b. delete all terms of (hierarchy AAAClass`Unclassfied terms) EXCEPT{AAAEN`Unclassfied terms}
        int orphanTermsToBeDeletedSize = orphanTermsToBeDeleted.size();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deleting " + orphanTermsToBeDeletedSize + " orphan terms...");
        for (int i = 0; i < orphanTermsToBeDeletedSize; i++) {
            String orphanTermToBeDeleted = (String) orphanTermsToBeDeleted.get(i);

            String orphanTermToBeDeletedUIWithoutPrefix = dbGen.removePrefix(orphanTermToBeDeleted);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + (i + 1) + ". Delete orphanTerm: " + orphanTermToBeDeletedUIWithoutPrefix);

            step2ofDeletion(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, new StringObject(orphanTermToBeDeleted), orphanTermToBeDeletedUIWithoutPrefix, errorMsg);
        }

        // 3. --- delete all facets of thesaurus AAA
        // get all facets of thesaurus AAA (ALL instances of AAAFacet EXCEPT{AAAClass`UNCLASSIFIED TERMS})
        Q.reset_name_scope();
        Q.set_current_node(thesFacet);
        int facetsOfThesaurusSet = Q.get_instances(0); // do NOT get ALL instances
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"facetsOfThesaurusSet card = " + Q.set_get_card( facetsOfThesaurusSet)); 
        Q.reset_name_scope();
        Q.set_current_node(orphansHierarchyFacet);
        int setWithOrphansFacet = Q.set_get_new();
        Q.set_put(setWithOrphansFacet);
        Q.set_difference(facetsOfThesaurusSet, setWithOrphansFacet);
        Q.free_set(setWithOrphansFacet);
        int facetsOfThesaurusSetWithoutOrphansFacet = facetsOfThesaurusSet;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"facetsOfThesaurusSetWithoutOrphansFacet card = " + Q.set_get_card( facetsOfThesaurusSetWithoutOrphansFacet));                
        Vector<String> facetsToBeDeleted = new Vector<String>();
        Q.reset_set(facetsOfThesaurusSetWithoutOrphansFacet);
        retVals.clear();
        if (Q.bulk_return_nodes(facetsOfThesaurusSetWithoutOrphansFacet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                facetsToBeDeleted.add(row.get_v1_cls_logicalname());
            }
        }
        /*name = new StringObject();
         while (Q.retur_nodes( facetsOfThesaurusSetWithoutOrphansFacet, name) != QClass.APIFail) {
         facetsToBeDeleted.add(name.getValue());
         }*/
        Q.free_set(facetsOfThesaurusSetWithoutOrphansFacet);
        // b. delete all facets thesaurus AAA (ALL instances of AAAFacet EXCEPT{AAAClass`UNCLASSIFIED TERMS})
        DBCreate_Modify_Facet creation_modificationOfFacet = new DBCreate_Modify_Facet();
        int facetsToBeDeletedSize = facetsToBeDeleted.size();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deleting " + facetsToBeDeletedSize + " facets...");
        for (int i = 0; i < facetsToBeDeletedSize; i++) {
            String facetToBeDeleted = (String) facetsToBeDeleted.get(i);
            String facetToBeDeletedUIWithoutPrefix = dbGen.removePrefix(facetToBeDeleted);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + i + 1 + ". Delete facet: " + facetToBeDeletedUIWithoutPrefix);
            if (creation_modificationOfFacet.Create_Or_ModifyFacet(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, facetToBeDeletedUIWithoutPrefix, "modify", "delete", errorMsg, true) == false) {
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------- facet deletion cancelled");
                return;
            } else {
                // ATTENTION: reset error message after every successful call to Create_Or_ModifyFacet() 
                // because it is set to "Facet: xxx was successfully deleted." and any next call to it
                // fails because it checks if error message is empty
                errorMsg.setValue("");
            }
        }

        // 4. --- Delete Instance Thesaurus`AAA from class Thesaurus 
        // a. delete ALL links pointed to/from target thesaurus
        Q.reset_name_scope();
        Q.set_current_node(targetThesaurusObj);
        int linksFrom = Q.get_link_from(0);
        int linksTo = Q.get_link_to(0);
        Q.set_union(linksFrom, linksTo);
        Q.free_set(linksTo);
        int linksSet = linksFrom;
        Q.reset_set(linksSet);
        //StringObject cls = new StringObject();
        //IntegerObject fromid = new IntegerObject();
        //IntegerObject link_sysid = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject flag = new IntegerObject();

        Vector<Return_Link_Id_Row> retLIVals = new Vector<Return_Link_Id_Row>();
        if (Q.bulk_return_link_id(linksSet, retLIVals) != QClass.APIFail) {
            //while (Q.retur_link_id( linksSet, cls, fromid, link_sysid, cmv, flag) != QClass.APIFail) {
            for (Return_Link_Id_Row row : retLIVals) {
                Identifier I_from = new Identifier(row.get_v2_fcid());
                Identifier I_link = new Identifier(row.get_v3_sysid());
                if (Q.CHECK_isUnNamedLink(row.get_v3_sysid())) {// unnamed attribute
                    //if (TA.IS_UNNAMED(row.get_v3_sysid()) != 0) { // unnamed attribute
                    ret = Q.CHECK_Delete_Unnamed_Attribute(I_link);
                } else { // named attribute
                    ret = Q.CHECK_Delete_Named_Attribute(I_link, I_from);
                }
            }
        }
        /*
         //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
         while (Q.retur_link_id( linksSet, cls, fromid, link_sysid, cmv, flag) != QClass.APIFail) {
         Identifier I_from = new Identifier(fromid.getValue());
         Identifier I_link = new Identifier(link_sysid.getValue());
         if (WTA.IS_UNNAMED(link_sysid.getValue()) != 0) { // unnamed attribute
         ret = Q.Delete_Unnamed_Attribute( I_link);
         } else { // named attribute
         ret = Q.Delete_Named_Attribute( I_link, I_from);
         }
         }
         */
        Q.free_set(linksSet);

        // b. Delete Instance Thesaurus`AAA from class Thesaurus
        ret = Q.CHECK_Delete_Instance(new Identifier(targetThesaurusObj.getValue()), new Identifier("Thesaurus"));
        if (ret == QClass.APIFail) {
            errorMsg.setValue(dbGen.check_success(ret, TA, null, tms_session));
            return;
        }

        // timer end
        float elapsedTimeSec = Utilities.stopTimer(startTime);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "############ DELETION of thesaurus: " + targetThesaurus + " SUCCEDED ############ (Time elapsed: " + elapsedTimeSec + " sec)");
    }
}
