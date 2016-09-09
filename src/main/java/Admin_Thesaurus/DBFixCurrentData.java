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

import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBConnect_Term;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.Utilities;
import Utils.Parameters;

import Utils.StringLocaleComparator;

import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.io.*;
import java.util.*;
import javax.servlet.ServletContext;

/**
 *
 * @author tzortzak
 */
public class DBFixCurrentData {

    final int MAXPARSETIMES = 10;

    public DBFixCurrentData() {
    }

    public boolean previewOrfix(UserInfoClass SessionUserInfo, ServletContext context,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Locale targetLocale, String functionallity, String mode, String targetHierarchy,
            String targetStatus, String time, String webAppSaveResults_temporary_filesAbsolutePath,
            String webAppSaveResults_Folder, StringObject Save_Results_file_name,
            StringObject XSL_fileNameObject) {

        Boolean fixed = new Boolean(true);
        String webAppSaveResults_AbsolutePath = context.getRealPath("/" + webAppSaveResults_Folder);
        String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");

        //<editor-fold defaultstate="collapsed" desc="HierarchyStatuses...">  
        if (functionallity.compareTo("HierarchyStatuses") == 0 && mode.compareTo("Fix") == 0) { // no preview mode supported

            ChangeStatus(SessionUserInfo.selectedThesaurus, targetHierarchy, targetStatus, fixed);
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Prefixes...">      
        if (functionallity.compareTo("Prefixes") == 0) {

            Save_Results_file_name.setValue("Prefix_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_WrongNames_Report.xsl");

            RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);

        } else //</editor-fold>       
        //<editor-fold defaultstate="collapsed" desc="RepairNames...">  
        if (functionallity.compareTo("RepairNames") == 0) {

            Save_Results_file_name.setValue("Name_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_WrongNames_Report.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;

                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }

            if (fixed == true) {

                RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Transform_Dates...">      
        if (functionallity.compareTo("Transform_Dates") == 0) {

            //Preview or fix Incorrect Top Terms. Which either do not follow the name of their hierarchy or they have one or more BT declared
            Save_Results_file_name.setValue("TransformDates_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Transform_Dates.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;

                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }
            }

            if (fixed == true) {
                TransformDates(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="TopTerm_Inconsistencies..."> 
        if (functionallity.compareTo("TopTerm_Inconsistencies") == 0) {

            //Preview or fix Incorrect Top Terms. Which either do not follow the name of their hierarchy or they have one or more BT declared
            Save_Results_file_name.setValue("TopTermsWithBts_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_TopTermsWith_Bts_Report.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }

            if (fixed == true) {
                TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="No_BT_Terms...">  
        if (functionallity.compareTo("No_BT_Terms") == 0) {

            Save_Results_file_name.setValue("Terms_Without_BT_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Descriptors_With_No_BT.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }
            }

            if (fixed == true) {

                Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Unclassified_Errors...">  
        if (functionallity.compareTo("Unclassified_Errors") == 0) {

            Save_Results_file_name.setValue("Terms_Wrong_Unclassified" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Unclassified_BT_Errors.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }
            }

            if (fixed == true) {
                Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>       
        //<editor-fold defaultstate="collapsed" desc="Classes_and_BTs...">  
        if (functionallity.compareTo("Classes_and_BTs") == 0) {

            Save_Results_file_name.setValue("Classes_BTs_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Classes_BTs_Inconsistencies_Report.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }
            }

            if (fixed == true) {
                Classes_and_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Check_Orphan_Hierarchy...">
        if (functionallity.compareTo("Check_Orphan_Hierarchy") == 0) {
            Save_Results_file_name.setValue("Check_Orphan_Hierarchy_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Check_Orphan_Hierarchy.xsl");

            Check_Orphan_Hierarchy(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="RTs_errors...">      
        if (functionallity.compareTo("RTs_errors") == 0) {

            Save_Results_file_name.setValue("RTs_Inconsistencies_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_RTs_Errors_Report.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Check_Orphan_Hierarchy(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Check_Orphan_Hierarchy(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {

                RTs_Errors(SessionUserInfo, targetLocale, mode, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>  
        //<editor-fold defaultstate="collapsed" desc="Garbage_Collection...">  
        if (functionallity.compareTo("Garbage_Collection") == 0) {

            Save_Results_file_name.setValue("Garbage_Collection_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Garbage_Collection.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {
                Collect_Garbage(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Unclassified_Source_relations...">      
        if (functionallity.compareTo("Unclassified_Source_relations") == 0) {

            Save_Results_file_name.setValue("Unclassified_Source_relations_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Unclassified_Sources.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }

            if (fixed == true) {
                Unclassified_Sources(SessionUserInfo.selectedThesaurus, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Unclassified_Editor_relations..."> 
        if (functionallity.compareTo("Unclassified_Editor_relations") == 0) {

            Save_Results_file_name.setValue("Unclassified_Editor_relations_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Unclassified_Editors.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {
                Unclassified_Editors(SessionUserInfo.selectedThesaurus, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Unclassified_HierarchyTerms...">      
        if (functionallity.compareTo("Unclassified_HierarchyTerms") == 0) {

            Save_Results_file_name.setValue("Unclassified_HierarchyTerms_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Unclassified_HierarchyTerms.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {
                Unclassified_Hierarchy_Terms(SessionUserInfo.selectedThesaurus, mode, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Multiple_Usage...">      
        if (functionallity.compareTo("Multiple_Usage") == 0) {

            Save_Results_file_name.setValue("Multiple_Usage_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Multiple_Usage.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {

                Multiple_Usage(SessionUserInfo, mode, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        } else //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Multiple_Editors_And_Dates...">     
        if (functionallity.compareTo("Multiple_Editors_And_Dates") == 0) {

            Save_Results_file_name.setValue("Multiple_Editors_And_Dates_" + time);
            XSL_fileNameObject.setValue(webAppSaveResults_AbsolutePath + "/FixData_Multiple_Editors_And_Dates.xsl");

            if (mode.compareTo("Fix") == 0) {

                int parseTimes = 0;
                RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairPrefixes(SessionUserInfo.selectedThesaurus, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    //RepairPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session, time, mode, fixed, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    RepairNames(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    TopTerm_Inconsistencies(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Without_BTs(SessionUserInfo, targetLocale, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

                parseTimes = 0;
                if (fixed == true) {
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                }
                while (fixed == false && parseTimes < MAXPARSETIMES) {
                    fixed = true;
                    Terms_Wrong_Unclassified(SessionUserInfo.selectedThesaurus, mode, fixed, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
                    parseTimes++;
                }

            }
            if (fixed == true) {
                Multiple_Editors_And_Dates(SessionUserInfo.selectedThesaurus, mode, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name.getValue(), pathToSaveScriptingAndLocale);
            }
        }
        //</editor-fold>   

        return fixed;

    }

    void Check_Orphan_Hierarchy(UserInfoClass SessionUserInfo, Locale targetLocale, String mode,
            Boolean fixed, String title, String webAppSaveResults_temporary_filesAbsolutePath,
            String Save_Results_file_name, String pathToSaveScriptingAndLocale) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        try {

            OutputStreamWriter out = null;

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + title + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Check_Orphan_Hierarchy ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Check_Orphan_Hierarchy ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            if (Parameters.TermModificationChecks.contains(27)) {
                //get all terms
                int index = Parameters.CLASS_SET.indexOf("TERM");

                String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
                SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

                StringObject BTClass = new StringObject();
                StringObject BTLink = new StringObject();
                StringObject fromcls = new StringObject();
                StringObject label = new StringObject();
                StringObject categ = new StringObject();
                StringObject cls = new StringObject();
                IntegerObject uniq_categ = new IntegerObject();
                IntegerObject traversed = new IntegerObject();
                CMValue cmv = new CMValue();
                IntegerObject clsID = new IntegerObject();
                IntegerObject linkID = new IntegerObject();
                IntegerObject categID = new IntegerObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTClass, BTLink, Q, sis_session);
                String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                String prefixClass = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                StringObject orphanClassObj = new StringObject(prefixClass + Parameters.UnclassifiedTermsLogicalname);

                //get all orphan instances
                Q.reset_name_scope();
                Q.set_current_node(orphanClassObj);
                int set_orphans = Q.get_all_instances(0);
                Q.reset_set(set_orphans);

                //collect all descriptors 
                Q.reset_name_scope();
                int set_all_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
                Q.reset_set(set_all_terms);
                Vector<String> checkTerms = new Vector<String>();
                checkTerms.addAll(dbGen.get_Node_Names_Of_Set(set_all_terms, true, Q, sis_session));
                Collections.sort(checkTerms, new StringLocaleComparator(targetLocale));
                //for each term collect all its bts 
                for (int i = 0; i < checkTerms.size(); i++) {

                    String targetTerm = checkTerms.get(i);
                    StringObject targetTermObj = new StringObject(prefixTerm + targetTerm);

                    //get tergetTerm's bt links
                    Q.reset_name_scope();
                    Q.set_current_node(targetTermObj);
                    int set_bt_labels = Q.get_link_from_by_category(0, BTClass, BTLink);
                    Q.reset_set(set_bt_labels);
                    //if term does not have more than 1 bt then ok -->continue
                    if (Q.set_get_card(set_bt_labels) < 2) {
                        Q.free_set(set_bt_labels);
                        continue;
                    }

                    //if links exist get to bt nodes 
                    int set_bts = Q.get_to_value(set_bt_labels);
                    Q.reset_set(set_bts);

                    //create a set that will hold targetTerm's not orphan bts
                    int set_not_orphans = Q.set_get_new();
                    Q.reset_set(set_not_orphans);
                    Q.set_copy(set_not_orphans, set_bts);
                    Q.reset_set(set_not_orphans);
                    Q.set_difference(set_not_orphans, set_orphans);
                    Q.reset_set(set_not_orphans);

                    //if no not orphan exists then ok --> continue
                    if (Q.set_get_card(set_not_orphans) == 0) {
                        Q.free_set(set_not_orphans);
                        Q.free_set(set_bts);
                        continue;
                    }

                    //now hold in set_bts all bts of targetTerm that are orphans
                    Q.set_intersect(set_bts, set_orphans);
                    Q.reset_set(set_bts);

                    if (Q.set_get_card(set_bts) > 0) {
                        //error case because not orphans cardinality is also > 0 

                        if (mode.compareTo("Preview") == 0) {
                            Vector<String> correctBts_Vector = new Vector<String>();
                            Vector<String> errorBts_Vector = new Vector<String>();
                            correctBts_Vector.addAll(dbGen.get_Node_Names_Of_Set(set_not_orphans, true, Q, sis_session));
                            errorBts_Vector.addAll(dbGen.get_Node_Names_Of_Set(set_bts, true, Q, sis_session));
                            try {

                                out.write("<concept>");

                                out.write("<name>");
                                out.write(Utilities.escapeXML(targetTerm));
                                out.write("</name>");

                                out.write("<orphanBts>");
                                for (int m = 0; m < errorBts_Vector.size(); m++) {

                                    out.write(Utilities.escapeXML("'" + Utilities.escapeXML(errorBts_Vector.get(m)) + "'"));
                                    if (m < errorBts_Vector.size() - 1) {
                                        out.write(", ");
                                    }

                                }
                                out.write("</orphanBts>");

                                out.write("<correctBts>");
                                for (int m = 0; m < correctBts_Vector.size(); m++) {

                                    out.write(Utilities.escapeXML("'" + Utilities.escapeXML(correctBts_Vector.get(m)) + "'"));
                                    if (m < correctBts_Vector.size() - 1) {
                                        out.write(", ");
                                    }

                                }
                                out.write("</correctBts>");

                                out.write("</concept>");
                                out.flush();

                            } catch (java.io.IOException exc) {
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Check_Orphan_Hierarchy Error in writing Errors in Names file : " + exc.getMessage());
                                Utils.StaticClass.handleException(exc);
                            }
                        } else {
                            //FIX CODE
                            Vector<String> correctBts_Vector = new Vector<String>();
                            Vector<String> errorBts_Vector = new Vector<String>();
                            correctBts_Vector.addAll(dbGen.get_Node_Names_Of_Set(set_not_orphans, false, Q, sis_session));
                            errorBts_Vector.addAll(dbGen.get_Node_Names_Of_Set(set_bts, false, Q, sis_session));
                            Vector<Long> deleteLinkIds = new Vector<Long>();
                            Q.reset_name_scope();
                            Q.reset_set(set_bt_labels);

                            //why call return_full_link_id and not call bulk_return_link
                            Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                            if (Q.bulk_return_link(set_bt_labels, retVals) != QClass.APIFail) {
                                for (Return_Link_Row row : retVals) {
                                    String targetBt = row.get_v3_cmv().getString();

                                    if (errorBts_Vector.contains(targetBt)) {
                                        deleteLinkIds.add(row.get_Neo4j_NodeId());
                                    }
                                }
                            }
                            /*
                             //while(Q.retur_full_link_id(set_bt_labels, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                             String targetBt = cmv.getString();

                             if(errorBts_Vector.contains(targetBt)){
                             deleteLinkIds.add(linkID.getValue());
                             }
                             }
                             */

                            //keep previous thesaurus value
                            StringObject prevThesName = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThesName);
                            if (SessionUserInfo.selectedThesaurus.equals(prevThesName.getValue()) == false) {
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }

                            for (int k = 0; k < deleteLinkIds.size(); k++) {
                                int ret = TA.CHECK_DeleteNewDescriptorAttribute(deleteLinkIds.get(k), targetTermObj);
                                //int ret = WTA.DeleteNewDescriptorAttribute(SessionUserInfo.selectedThesaurus, deleteLinkIds.get(k), targetTermObj);
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    fixed = false;
                                }
                            }
                            //restore previous thesarus name value
                            if (SessionUserInfo.selectedThesaurus.equals(prevThesName) == false) {
                                TA.SetThesaurusName(prevThesName.getValue());
                            }

                            /*
                             //if targetTerm in set_orphans terms then delete it from instance
                             //not ENOUGH SINCE ITS NTS MAY REMAIN IN THIS HIERARCHY
                             Q.free_set(set_orphans);
                             //get all orphan instances
                             Q.reset_name_scope();
                             int orphcanClassId = Q.set_current_node(orphanClassObj);
                             set_orphans = Q.get_all_instances(0);
                             Q.reset_set(set_orphans);

                             int test_set = Q.set_get_new();
                             Q.reset_set(test_set);


                             Q.reset_name_scope();
                             int targetTermId = Q.set_current_node(targetTermObj);
                             Q.set_put(test_set);
                             Q.reset_set(test_set);

                             Q.set_intersect(test_set, set_orphans);
                             Q.reset_set(test_set);

                             if(Q.set_get_card(test_set)>0){
                             Identifier termIdent = new Identifier(targetTermId);
                             Identifier classIdent = new Identifier(orphcanClassId);

                             int ret = Q.Delete_Instance(termIdent, classIdent);
                             if(ret==QClass.APIFail){
                             fixed = false;
                             }
                             }
                             */
                        }

                    }

                    Q.free_set(set_not_orphans);
                    Q.free_set(set_bt_labels);
                    Q.free_set(set_bts);
                }

                Q.free_set(set_all_terms);
                Q.free_set(set_orphans);
            }

            Q.free_all_sets();
            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                if (fixed && Parameters.TermModificationChecks.contains(27)) {
                    //USE THIS CODE IN ORDER TO DECLASSIFY TERMS CORRCTED AND THEIR NTS FROM ORPHAN TERMS CLASS IF NEEDED
                    Classes_and_BTs(SessionUserInfo, null, mode, fixed, title, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, pathToSaveScriptingAndLocale);
                }
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RepairPrefixes: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

    }

    void ChangeStatus(String selectedThesaurus, String targetHierarchy, String targetStatus, Boolean fixed) {

        DBGeneral dbGen = new DBGeneral();
        DBConnect_Term dbCon = new DBConnect_Term();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        //open connection and start Transaction
        if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ ChangeStatus ");
            return;
        }

        Q.TEST_begin_transaction();

        StringObject errorMsg = new StringObject("");
        String prefixClass = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        StringObject targetHier = new StringObject(prefixClass + targetHierarchy);
        Q.reset_name_scope();

        String pathToMessagesXML = Parameters.BaseRealPath + "\\translations\\Messages.xml";
        StringObject resultMessageObj = new StringObject();
        Vector<String> errorArgs = new Vector<String>();

        if (Q.set_current_node(targetHier) == QClass.APIFail) {
            errorArgs.add(targetHierarchy);
            dbGen.Translate(resultMessageObj, "root/FixCurrentData/ChangeStatusFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();

            errorMsg.setValue(resultMessageObj.getValue());
            //errorMsg.setValue("Αποτυχία εύρεσης της ιεραρχίας " + targetHierarchy + " στην βάση.");
            fixed = false;
            return;
        }

        int set_hierarchy_nodes = Q.get_all_instances(0);
        Q.reset_set(set_hierarchy_nodes);

        Vector<String> hierarchyNodes = new Vector<String>();
        hierarchyNodes.addAll(dbGen.get_Node_Names_Of_Set(set_hierarchy_nodes, false, Q, sis_session));
        Q.free_set(set_hierarchy_nodes);
        fixed = false;
        for (int i = 0; i < hierarchyNodes.size(); i++) {
            StringObject targetTermObj = new StringObject(hierarchyNodes.get(i));
            dbCon.CreateModifyStatus(selectedThesaurus, targetTermObj, targetStatus, Q, TA, sis_session, tms_session, dbGen, errorMsg);
            if (errorMsg != null && errorMsg.getValue().length() > 0) {
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                return;
            }
        }
        fixed = true;

        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
    }

    void RepairPrefixes(String selectedThesaurus, String title, String mode, Boolean fixed, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        //Fix or repair names that are not normalized and trimmed and nodes that do not have the right prefix

        //tools
        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        try {

            OutputStreamWriter out = null;

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                //out.write(XSL);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + title + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RepairPrefixes ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RepairPrefixes ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            int SisSessionId = sis_session.getValue();
            Q.reset_name_scope();

            Vector<StringObject> classes = new Vector<StringObject>();
            Vector<String> prefixes = new Vector<String>();
            Vector<String> writenTerms = new Vector<String>();

            prefixes.add(dbtr.getThesaurusPrefix_AlternativeTerm(selectedThesaurus, Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_DeweyNumber(Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_Editor(Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_Source(Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_TaxonomicCode(Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_TopTerm(selectedThesaurus, Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_UsedForTerm(selectedThesaurus, Q, SisSessionId));
            prefixes.add(dbtr.getThesaurusPrefix_EnglishWord(Q, SisSessionId));

            StringObject AltClassObj = new StringObject();
            dbtr.getThesaurusClass_AlternativeTerm(selectedThesaurus, Q, SisSessionId, AltClassObj);
            classes.add(AltClassObj);

            StringObject thesClassObj = new StringObject();
            dbtr.getThesaurusClass_ThesaurusClass(selectedThesaurus, Q, SisSessionId, thesClassObj);
            classes.add(thesClassObj);

            StringObject termClassObj = new StringObject();
            dbtr.getThesaurusClass_Term(selectedThesaurus, Q, SisSessionId, termClassObj);
            classes.add(termClassObj);

            classes.add(new StringObject("DeweyNumber"));

            StringObject editorClassObj = new StringObject();
            dbtr.getThesaurusClass_Editor(selectedThesaurus, Q, SisSessionId, editorClassObj);
            classes.add(editorClassObj);

            classes.add(new StringObject("Source"));
            classes.add(new StringObject("TaxonomicCode"));

            StringObject topTermClassObj = new StringObject();
            dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, SisSessionId, topTermClassObj);
            classes.add(topTermClassObj);

            StringObject ufClassObj = new StringObject();
            dbtr.getThesaurusClass_UsedForTerm(selectedThesaurus, Q, SisSessionId, ufClassObj);
            classes.add(ufClassObj);

            classes.add(new StringObject("EnglishWord"));

            for (int i = 0; i < classes.size(); i++) {

                String currentPrefix = prefixes.get(i);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Προσπάθεια αναφοράς στην κλάση " + classes.get(i).getValue() +" με πρόθεμα "+ currentPrefix);
                Q.reset_name_scope();
                if (Q.set_current_node(classes.get(i)) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αποτυχία αναφοράς στην κλάση " + classes.get(i).getValue());
                    continue;
                }
                int set_curr_instances = Q.get_all_instances(0);
                Q.reset_set(set_curr_instances);

                Vector<String> instanceNames = dbGen.get_Node_Names_Of_Set(set_curr_instances, false, Q, sis_session);
                if (instanceNames != null && instanceNames.size() > 0) {

                    for (int k = 0; k < instanceNames.size(); k++) {

                        Q.reset_name_scope();

                        String initial = instanceNames.get(k);
                        String comparison = instanceNames.get(k);

                        Identifier I_old = new Identifier(initial);
                        Identifier I_new = null;
                        boolean exists = false;
                        StringObject newNameStrObj = new StringObject();

                        //fix Prefix
                        if (currentPrefix != null && initial.startsWith(currentPrefix)) {

                            continue;

                        } else {

                            comparison = currentPrefix + comparison;
                            newNameStrObj.setValue(comparison);
                            I_new = new Identifier(newNameStrObj.getValue());
                            Q.reset_name_scope();

                            if (Q.set_current_node(newNameStrObj) != QClass.APIFail) {
                                exists = true;
                            }
                        }

                        if (initial.compareTo(comparison) == 0) {
                            continue;
                        }

                        if (mode.compareTo("Fix") == 0) {

                            //if rename fails then new node name already exists.
                            //Default fix behavior will be to copy old node name's links to new node name and delete old
                            Q.reset_name_scope();
                            if (Q.CHECK_Rename_Node(I_old, I_new) == QClass.APIFail) {
                                Q.reset_name_scope();
                                if (Q.set_current_node(new StringObject(I_old.getLogicalName())) != QClass.APIFail) {
                                    fixed = false;
                                }
                            }

                        } else if (mode.compareTo("Preview") == 0) {
                            try {

                                if (!writenTerms.contains(initial)) {
                                    out.write("<concept>");

                                    out.write("<kind>");
                                    out.write(classes.get(i).getValue());
                                    out.write("</kind>");
                                    out.write("<oldname>");
                                    out.write(Utilities.escapeXML("'" + initial.replaceAll(" ", "&#160;") + "'"));
                                    out.write("</oldname>");
                                    out.write("<newname>");
                                    out.write(Utilities.escapeXML("'" + comparison + "'"));
                                    out.write("</newname>");

                                    out.write("<newnameexists>");

                                    if (exists) {
                                        out.write("NAI");
                                    } else {
                                        out.write(" - ");
                                    }
                                    out.write("</newnameexists>");

                                    out.write("</concept>");
                                    out.flush();
                                    writenTerms.add(initial);
                                }

                            } catch (java.io.IOException exc) {
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RepairPrefixes Error in writing Errors in Names file : " + exc.getMessage());
                                Utils.StaticClass.handleException(exc);
                            }
                        }
                    }
                }

            }

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RepairPrefixes: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void RepairNames(UserInfoClass SessionUserInfo, Locale targetLocale, String mode, Boolean fixed, String title, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        //Fix or repair names that are not normalized and trimmed and nodes that do not have the right prefix
        try {

            //tools
            OutputStreamWriter out = null;
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                //out.write(XSL);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + title + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RepairNames ");
                    return;
                }
                Q.TEST_begin_query();

            } else {
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RepairNames ");
                    return;
                }
                Q.TEST_begin_transaction();
            }

            StringObject toENClassObj = new StringObject();
            StringObject toENLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, toENClassObj, toENLinkObj, Q, sis_session);

            StringObject usedForClassObj = new StringObject();
            dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), usedForClassObj);

            StringObject altClassObj = new StringObject();
            dbtr.getThesaurusClass_AlternativeTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), altClassObj);

            StringObject uk_ufClassObj = new StringObject();
            StringObject uk_ufLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, uk_ufClassObj, uk_ufLinkObj, Q, sis_session);

            /*
             StringObject uk_altClassObj = new StringObject();
             StringObject uk_altLinkObj = new StringObject();
             dbGen.getKeywordPair(selectedThesaurus, dbGen.uk_alt_kwd, uk_altClassObj, uk_altLinkObj, Q, sis_session);
             *
             */
            StringObject editorClassObj = new StringObject();
            dbtr.getThesaurusClass_Editor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), editorClassObj);

            StringObject scope_noteClassObj = new StringObject();
            StringObject scope_noteLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scope_noteClassObj, scope_noteLinkObj, Q, sis_session);

            StringObject scope_noteENClassObj = new StringObject();
            StringObject scope_noteENLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scope_noteENClassObj, scope_noteENLinkObj, Q, sis_session);

            StringObject commentClassObj = new StringObject();
            StringObject commentLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.comment_kwd, commentClassObj, commentLinkObj, Q, sis_session);

            StringObject createdOnClassObj = new StringObject();
            StringObject createdOnLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClassObj, createdOnLinkObj, Q, sis_session);

            StringObject modifiedOnClassObj = new StringObject();
            StringObject modifiedOnLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClassObj, modifiedOnLinkObj, Q, sis_session);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            index = Parameters.CLASS_SET.indexOf("HIERARCHY");

            String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);

            index = Parameters.CLASS_SET.indexOf("FACET");
            String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);

            Q.reset_name_scope();
            int set_1 = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
            Vector<String> set_1Names = dbGen.get_Node_Names_Of_Set(set_1, false, Q, sis_session);
            Q.free_set(set_1);

            Q.reset_name_scope();
            int set_2 = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
            Vector<String> set_2Names = dbGen.get_Node_Names_Of_Set(set_2, false, Q, sis_session);
            Q.free_set(set_2);

            Q.reset_name_scope();
            int set_3 = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Vector<String> set_3Names = dbGen.get_Node_Names_Of_Set(set_3, false, Q, sis_session);
            Q.free_set(set_3);

            Q.reset_name_scope();
            Q.set_current_node(usedForClassObj);
            int set_4 = Q.get_all_instances(0);
            Vector<String> set_4Names = dbGen.get_Node_Names_Of_Set(set_4, false, Q, sis_session);
            Q.free_set(set_4);

            Q.reset_name_scope();
            Q.set_current_node(altClassObj);
            int set_5 = Q.get_all_instances(0);
            Vector<String> set_5Names = dbGen.get_Node_Names_Of_Set(set_5, false, Q, sis_session);
            Q.free_set(set_5);

            Q.reset_name_scope();
            Q.set_current_node(toENClassObj);
            Q.set_current_node(toENLinkObj);
            int set_6_labels = Q.get_all_instances(0);
            int set_6 = Q.get_to_value(set_6_labels);
            Vector<String> set_6Names = dbGen.get_Node_Names_Of_Set(set_6, false, Q, sis_session);
            Q.free_set(set_6_labels);
            Q.free_set(set_6);

            Q.reset_name_scope();
            Q.set_current_node(uk_ufClassObj);
            Q.set_current_node(uk_ufLinkObj);
            int set_7_labels = Q.get_all_instances(0);
            int set_7 = Q.get_to_value(set_7_labels);
            Vector<String> set_7Names = dbGen.get_Node_Names_Of_Set(set_7, false, Q, sis_session);
            Q.free_set(set_7_labels);
            Q.free_set(set_7);

            /*
             Q.reset_name_scope();
             Q.set_current_node(uk_altClassObj);
             Q.set_current_node(uk_altLinkObj);
             int set_8_labels = Q.get_all_instances(0);
             int set_8 = Q.get_to_value(set_8_labels);
             Vector<String> set_8Names = dbGen.get_Node_Names_Of_Set(set_8, false, Q, sis_session);
             Q.free_set(set_8_labels);
             Q.free_set(set_8);
             *
             */
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.SourceClass));
            int set_9 = Q.get_all_instances(0);
            Vector<String> set_9Names = dbGen.get_Node_Names_Of_Set(set_9, false, Q, sis_session);
            Q.free_set(set_9);

            Q.reset_name_scope();
            Q.set_current_node(editorClassObj);
            int set_10 = Q.get_all_instances(0);
            Vector<String> set_10Names = dbGen.get_Node_Names_Of_Set(set_10, false, Q, sis_session);
            Q.free_set(set_10);

            Q.reset_name_scope();
            Q.set_current_node(scope_noteClassObj);
            Q.set_current_node(scope_noteLinkObj);
            int set_11_0 = Q.get_instances(0);
            Q.reset_set(set_11_0);
            int set_11 = Q.get_to_value(set_11_0);
            Vector<String> set_11Names = dbGen.get_Node_Names_Of_Set(set_11, false, Q, sis_session);
            Q.free_set(set_11_0);
            Q.free_set(set_11);

            //handle comment nodes
            Q.reset_name_scope();
            StringObject thesaurusCommentObj = new StringObject();
            dbtr.getThesaurusCategory_comment(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), thesaurusCommentObj);
            Q.reset_name_scope();
            Q.set_current_node(commentClassObj);
            Q.set_current_node(commentLinkObj);
            int set_12_0 = Q.get_instances(0);
            Q.reset_set(set_12_0);
            int set_12 = Q.get_to_value(set_12_0);
            Vector<String> set_12Names = dbGen.get_Node_Names_Of_Set(set_12, false, Q, sis_session);
            Q.free_set(set_12_0);
            Q.free_set(set_12);

            //handle dates
            Q.reset_name_scope();
            Q.set_current_node(createdOnClassObj);
            Q.set_current_node(createdOnLinkObj);
            int set_created_labels = Q.get_all_instances(0);
            Q.reset_set(set_created_labels);
            int set_created = Q.get_to_value(set_created_labels);
            Q.reset_set(set_created);
            Vector<String> createdDates = dbGen.get_Node_Names_Of_Set(set_created, false, Q, sis_session);
            Q.free_set(set_created);
            Q.free_set(set_created_labels);

            Q.reset_name_scope();
            Q.set_current_node(modifiedOnClassObj);
            Q.set_current_node(modifiedOnLinkObj);
            int set_modified_labels = Q.get_all_instances(0);
            Q.reset_set(set_modified_labels);
            int set_modified = Q.get_to_value(set_modified_labels);
            Q.reset_set(set_created);
            Vector<String> modifiedDates = dbGen.get_Node_Names_Of_Set(set_modified, false, Q, sis_session);
            Q.free_set(set_modified);
            Q.free_set(set_modified_labels);

            Q.reset_name_scope();
            Q.set_current_node(scope_noteENClassObj);
            Q.set_current_node(scope_noteENLinkObj);
            int set_13_0 = Q.get_instances(0);
            Q.reset_set(set_13_0);
            int set_13 = Q.get_to_value(set_13_0);
            Vector<String> set_13Names = dbGen.get_Node_Names_Of_Set(set_13, false, Q, sis_session);
            Q.free_set(set_13_0);
            Q.free_set(set_13);

            String prefix1_2 = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            String prefix3_5 = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            String prefix6_8 = dbtr.getThesaurusPrefix_EnglishWord(Q, sis_session.getValue());
            String prefix9 = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
            String prefix10 = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());

            Q.reset_name_scope();

            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_1Names, prefix1_2, "", "Μικροθησαυρός", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_2Names, prefix1_2, "", "Ιεραρχία", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_3Names, prefix3_5, "", "Όρος", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_4Names, prefix3_5, "", "ΧΑ", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_5Names, prefix3_5, "", "Εναλλ.Όρος", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_6Names, prefix6_8, "", "ΑΟ", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_7Names, prefix6_8, "", "UF", out);
            /*RepairNames_Preview_or_Fix(selectedThesaurus, Q, sis_session, targetLocale, mode, fixed, set_8Names, prefix6_8, "", "ALT", out);*/
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_9Names, prefix9, "", "Πηγές", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_10Names, prefix10, "", "Συντάκτης", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_11Names, prefix3_5, "`" + scope_noteLinkObj.getValue(), "ΔΣ", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_12Names, prefix3_5, "`" + commentLinkObj.getValue(), "IΣ", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, set_13Names, prefix3_5, "`" + scope_noteENLinkObj.getValue(), "SN", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, createdDates, "", "", "Ημερομην.Δημιουργίας", out);
            RepairNames_Preview_or_Fix(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, targetLocale, mode, fixed, modifiedDates, "", "", "Ημερομην.Τροποποίησης", out);

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RepairNames: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void RepairNames_Preview_or_Fix(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale, String mode, Boolean fixed, Vector<String> allNamesWithPrefix, String prefix, String suffix, String kind, OutputStreamWriter out) {

        Utilities u = new Utilities();

        allNamesWithPrefix.trimToSize();
        Collections.sort(allNamesWithPrefix, new StringLocaleComparator(targetLocale));

        for (int i = 0; i < allNamesWithPrefix.size(); i++) {

            Q.reset_name_scope();

            String initial = allNamesWithPrefix.get(i);
            String comparison = allNamesWithPrefix.get(i).replaceAll(" +", " ").trim();

            Identifier I_old = new Identifier(initial);
            Identifier I_new = null;
            boolean exists = false;
            StringObject oldNameStrObj = new StringObject(allNamesWithPrefix.get(i));
            StringObject newNameStrObj = new StringObject();

            //fix Prefix
            if (prefix != null && initial.startsWith(prefix)) {

                if (prefix.length() > 0) {

                    initial = initial.substring(prefix.length());
                    comparison = comparison.substring(prefix.length());

                    if (suffix != null && suffix.length() > 0 && comparison.endsWith(suffix)) {

                        int length = comparison.length() - suffix.length();
                        comparison = comparison.substring(0, length);
                        comparison = comparison.trim();
                        comparison = comparison.concat(suffix);
                    } else {
                        comparison = comparison.trim();
                    }
                }

                newNameStrObj.setValue(prefix.concat(comparison));
                I_new = new Identifier(newNameStrObj.getValue());

                Q.reset_name_scope();

                if (Q.set_current_node(newNameStrObj) != QClass.APIFail) {
                    exists = true;
                }
            } else {
                //also check scope notes and comments node names

                if (suffix != null && suffix.length() > 0 && comparison.endsWith(suffix)) {

                    int length = comparison.length() - suffix.length();
                    comparison = comparison.substring(0, length);
                    comparison = comparison.trim();
                    comparison = comparison.concat(suffix);
                } else {
                    comparison = comparison.trim();
                }
                comparison = prefix.concat(comparison);
                newNameStrObj.setValue(comparison);
                I_new = new Identifier(newNameStrObj.getValue());
                Q.reset_name_scope();

                if (Q.set_current_node(newNameStrObj) != QClass.APIFail) {
                    exists = true;
                }
            }

            // System.out.print("'" +initial + "' vs '"+ comparison +"'" );
            if (initial.compareTo(comparison) == 0) {
                //  Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" EQUALS" );
                continue;
            }

            if (mode.compareTo("Fix") == 0) {

                //if rename fails then new node name already exists.
                //Default fix behavior will be to copy old node name's links to new node name and delete old
                Q.reset_name_scope();
                if (Q.CHECK_Rename_Node(I_old, I_new) == QClass.APIFail) {

                    //No default error handling for scope_note and comment nodes that already exist
                    if (suffix != null && suffix.length() > 0 && comparison.endsWith(suffix)) {
                        continue;
                    }
                    Q.reset_name_scope();
                    long oldNodeL = Q.set_current_node(oldNameStrObj);
                    if (oldNodeL == QClass.APIFail)//node did not exist
                    {
                        continue;
                    }
                    Q.set_current_node_id(oldNodeL);//Unknown bug without this cuurent node is not set and oldNode_links gets -1

                    int oldNode_links_from = Q.get_link_from(0);
                    Q.reset_set(oldNode_links_from);

                    //StringObject fromcls = new StringObject();
                    //StringObject label = new StringObject();
                    //StringObject categ = new StringObject();
                    //StringObject cls = new StringObject();
                    //IntegerObject uniq_categ = new IntegerObject();
                    //IntegerObject clsID = new IntegerObject();
                    //IntegerObject linkID = new IntegerObject();
                    //IntegerObject categID = new IntegerObject();
                    //CMValue cmv = new CMValue();
                    //Storage of values in order to copy them as attributes to the new node
                    Vector<Long> links_Ids = new Vector<Long>();
                    Vector<CMValue> vals = new Vector<CMValue>();
                    Vector<String> fromClasses = new Vector<String>();
                    Vector<String> link_names = new Vector<String>();

                    Vector<Return_Full_Link_Id_Row> retFLIVals = new Vector<Return_Full_Link_Id_Row>();
                    if (Q.bulk_return_full_link_id(oldNode_links_from, retFLIVals) != QClass.APIFail) {
                        for (Return_Full_Link_Id_Row row : retFLIVals) {
                            //while (Q.retur_full_link_id(oldNode_links_from, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                            links_Ids.add(row.get_v4_linkId());
                            fromClasses.add(row.get_v6_fromCls());
                            link_names.add(row.get_v5_categ());
                            vals.add(row.get_v8_cmv());
                        }
                    }
                    /*
                     //while (Q.retur_full_link_id(oldNode_links_from, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                     links_Ids.add(linkID.getValue());
                     fromClasses.add(fromcls.getValue());
                     link_names.add(categ.getValue());
                     CMValue copyCmv = new CMValue();

                     switch (cmv.getType()) {

                     case CMValue.TYPE_EMPTY: {
                     copyCmv.assign_empty();
                     break;
                     }

                     case CMValue.TYPE_FLOAT: {
                     copyCmv.assign_float(cmv.getFloat());
                     break;
                     }

                     case CMValue.TYPE_INT: {
                     copyCmv.assign_int(cmv.getInt());
                     break;
                     }

                     case CMValue.TYPE_NODE: {

                     copyCmv.assign_node(cmv.getString(), cmv.getSysid());
                     break;
                     }
                     case CMValue.TYPE_STRING: {
                     copyCmv.assign_string(cmv.getString());
                     break;
                     }

                     case CMValue.TYPE_SYSID: {
                     copyCmv.assign_int(cmv.getSysid());
                     break;
                     }
                     case CMValue.TYPE_TIME: {
                     copyCmv.assign_time(cmv.getTime());
                     break;
                     }
                      
                     default:
                     break;
                     }
                     vals.add(copyCmv);

                     }
                     */

                    links_Ids.trimToSize();
                    vals.trimToSize();
                    fromClasses.trimToSize();
                    link_names.trimToSize();
                    Q.free_set(oldNode_links_from);

                    boolean allAttributesDeleted = true;
                    //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

                    StringObject prevThesObj = new StringObject();
                    if (prevThesObj.getValue().equals(selectedThesaurus) == false) {
                        TA.SetThesaurusName(selectedThesaurus);
                    }
                    for (int k = 0; k < links_Ids.size(); k++) {
                        //all nodes are new --> use CreateNewDescriptorAttribute and DeleteNewDescriptorAttribute
                        Q.reset_name_scope();

                        int catSet = Q.set_get_new();
                        Q.reset_name_scope();
                        Q.set_current_node(new StringObject(fromClasses.get(k)));
                        Q.set_current_node(new StringObject(link_names.get(k)));
                        Q.set_put(catSet);
                        Q.reset_set(catSet);
                        //int ret = WTA.CreateNewDescriptorAttribute(selectedThesaurus, new StringObject(), newNameStrObj, vals.get(k), catSet);
                        int ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), newNameStrObj, vals.get(k), catSet);
                        Q.free_set(catSet);

                        if (ret == TMSAPIClass.TMS_APISucc) {
                            //WTA.DeleteNewDescriptorAttribute(selectedThesaurus, links_Ids.get(k), oldNameStrObj);
                            TA.CHECK_DeleteNewDescriptorAttribute(links_Ids.get(k), oldNameStrObj);
                        } else {
                            allAttributesDeleted = false;
                            fixed = false;
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deletion Failed from '" + newNameStrObj.getValue() + "' to '" + vals.get(k).getString() + "'");
                        }
                    }

                    if (prevThesObj.getValue().equals(selectedThesaurus) == false) {
                        TA.SetThesaurusName(prevThesObj.getValue());
                    }

                    //Get To Nodes AND LINKS
                    //Node name from is not known and must be stored also
                    Vector<String> names = new Vector<String>();
                    links_Ids.clear();
                    vals.clear();
                    fromClasses.clear();
                    link_names.clear();

                    Q.reset_name_scope();
                    //Q.set_current_node(oldNameStrObj);
                    Q.set_current_node_id(oldNodeL);//Unknown bug without this cuurent node is not set and oldNode_links_to gets -1

                    int oldNode_links_to = Q.get_link_to(0);
                    Q.reset_set(oldNode_links_to);

                    retFLIVals.clear();
                    if (Q.bulk_return_full_link_id(oldNode_links_to, retFLIVals) != QClass.APIFail) {
                        for (Return_Full_Link_Id_Row row : retFLIVals) {
                            //while (Q.retur_full_link_id(oldNode_links_to, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                            links_Ids.add(row.get_v4_linkId());
                            names.add(row.get_v1_cls());
                            fromClasses.add(row.get_v6_fromCls());
                            link_names.add(row.get_v5_categ());
                            vals.add(row.get_v8_cmv());
                        }
                    }
                    /*
                     //while (Q.retur_full_link_id(oldNode_links_to, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                     links_Ids.add(linkID.getValue());
                     names.add(cls.getValue());
                     fromClasses.add(fromcls.getValue());
                     link_names.add(categ.getValue());
                     CMValue copyCmv = new CMValue();

                     switch (cmv.getType()) {

                     case CMValue.TYPE_EMPTY: {
                     copyCmv.assign_empty();
                     break;
                     }

                            
                     case CMValue.TYPE_FLOAT: {
                     copyCmv.assign_float(cmv.getFloat());
                     break;
                     }
                            

                     case CMValue.TYPE_INT: {
                     copyCmv.assign_int(cmv.getInt());
                     break;
                     }

                     case CMValue.TYPE_NODE: {
                     String temp = cmv.getString().replaceAll(" +", " ").trim();

                     copyCmv.assign_node(temp, cmv.getSysid());
                     break;
                     }
                     case CMValue.TYPE_STRING: {
                     String temp = cmv.getString().replaceAll(" +", " ").trim();
                     copyCmv.assign_string(temp);
                     break;
                     }
                            
                     case CMValue.TYPE_SYSID: {
                     copyCmv.assign_int(cmv.getSysid());
                     break;
                     }
                     case CMValue.TYPE_TIME: {
                     copyCmv.assign_time(cmv.getTime());
                     break;
                     }
                            
                     default:
                     break;
                     }
                     vals.add(copyCmv);

                     }
                     */

                    links_Ids.trimToSize();
                    Q.free_set(oldNode_links_to);

                    TA.GetThesaurusNameWithoutPrefix(prevThesObj);
                    if (prevThesObj.getValue().equals(selectedThesaurus) == false) {
                        TA.SetThesaurusName(selectedThesaurus);
                    }
                    for (int k = 0; k < links_Ids.size(); k++) {

                        //check if a same relation exists before create
                        //all nodes are new --> use CreateNewDescriptorAttribute and DeleteNewDescriptorAttribute
                        Q.reset_name_scope();

                        int catSet = Q.set_get_new();
                        Q.reset_name_scope();
                        Q.set_current_node(new StringObject(fromClasses.get(k)));
                        Q.set_current_node(new StringObject(link_names.get(k)));
                        Q.set_put(catSet);
                        Q.reset_set(catSet);
                        //int ret = WTA.CreateNewDescriptorAttribute(selectedThesaurus, new StringObject(), new StringObject(names.get(k)), vals.get(k), catSet);
                        int ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), new StringObject(names.get(k)), vals.get(k), catSet);
                        Q.free_set(catSet);

                        if (ret == TMSAPIClass.TMS_APISucc) {
                            //WTA.DeleteNewDescriptorAttribute(selectedThesaurus, links_Ids.get(k), new StringObject(names.get(k)));
                            TA.CHECK_DeleteNewDescriptorAttribute(links_Ids.get(k), new StringObject(names.get(k)));
                        } else {
                            allAttributesDeleted = false;
                            fixed = false;
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Deletion Failed from '" + names.get(k) + "' to '" + vals.get(k).getString() + "'");
                        }
                    }
                    if (prevThesObj.getValue().equals(selectedThesaurus) == false) {
                        TA.SetThesaurusName(prevThesObj.getValue());
                    }
                    if (allAttributesDeleted) {
                        Q.CHECK_Delete_Node(new Identifier(oldNameStrObj.getValue()));
                    } else {
                        fixed = false;
                    }
                }

            } else if (mode.compareTo("Preview") == 0) {
                try {

                    out.write("<concept>");

                    out.write("<kind>");
                    out.write(kind);
                    out.write("</kind>");
                    out.write("<oldname>");
                    out.write(Utilities.escapeXML("'" + initial.replaceAll(" ", "&#160;") + "'"));
                    out.write("</oldname>");
                    out.write("<newname>");
                    out.write(Utilities.escapeXML("'" + comparison + "'"));
                    out.write("</newname>");

                    out.write("<newnameexists>");

                    if (exists) {
                        out.write("NAI");
                    } else {
                        out.write(" - ");
                    }
                    out.write("</newnameexists>");

                    out.write("</concept>");
                    out.flush();

                } catch (java.io.IOException exc) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RepairNames_Preview_or_Fix Error in writing Errors in Names file : " + exc.getMessage());
                    Utils.StaticClass.handleException(exc);
                }
            }

        }

    }

    void TopTerm_Inconsistencies(UserInfoClass SessionUserInfo, Locale targetLocale, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        //Fix or repair 
        //Top Terms that have BT
        //Top Terms that have different name from their relative hierarchy
        //Top Terms that belong to some other hierarchy than their relative one
        //Top Terms without any relative hierarchy
        DBGeneral dbGen = new DBGeneral();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        try {

            OutputStreamWriter out = null;

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                //out.write(XSL);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");
                out.flush();

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ TopTerm_Inconsistencies ");
                    return;
                }
                Q.TEST_begin_query();

            } else if (mode.compareTo("Fix") == 0) {
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ TopTerm_Inconsistencies ");
                    return;
                }
                Q.TEST_begin_transaction();
            }

            Q.reset_name_scope();

            Q.set_current_node(new StringObject(SessionUserInfo.selectedThesaurus.concat("TopTerm")));
            int set_1 = Q.get_instances(0);
            Q.reset_set(set_1);
            Vector<String> tts = new Vector<String>();
            tts.addAll(dbGen.get_Node_Names_Of_Set(set_1, true, Q, sis_session));

            if (mode.compareTo("Preview") == 0) {

                Q.free_set(set_1);

                TopTerm_Inconsistencies_Preview(SessionUserInfo.selectedThesaurus, Q, sis_session, targetLocale, tts, out);
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.flush();
                out.close();

            } else if (mode.compareTo("Fix") == 0) {

                Q.free_set(set_1);
                TopTerm_Inconsistencies_Fix(SessionUserInfo.selectedThesaurus, Q, sis_session, fixed, tts);

                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TopTerm_Inconsistencies: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

    }

    void TopTerm_Inconsistencies_Preview(String selectedThesaurus, QClass Q, IntegerObject sis_session, Locale targetLocale, Vector<String> topTerms, OutputStreamWriter out) {

        //tools
        Utilities u = new Utilities();

        topTerms.trimToSize();
        Collections.sort(topTerms, new StringLocaleComparator(targetLocale));

        for (int i = 0; i < topTerms.size(); i++) {

            Vector<String> BTsFordeletion = new Vector<String>();
            Vector<String> HiersFordeletion = new Vector<String>();
            Vector<String> shouldBeRenamedTo = new Vector<String>();

            TopTermsBugDetection(selectedThesaurus, Q, sis_session, topTerms.get(i), BTsFordeletion, HiersFordeletion, shouldBeRenamedTo);

            if (BTsFordeletion.size() > 0 || HiersFordeletion.size() > 0 || (shouldBeRenamedTo.size() > 0 && shouldBeRenamedTo.get(0).compareTo(topTerms.get(i)) != 0)) {
                try {
                    out.write("<topterm>");

                    out.write("<name>");
                    out.write(Utilities.escapeXML(topTerms.get(i)));
                    out.write("</name>");

                    out.write("<delete_bt_relations>");

                    if (BTsFordeletion.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < BTsFordeletion.size(); k++) {

                            out.write(Utilities.escapeXML(BTsFordeletion.get(k)));
                            if (k < BTsFordeletion.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }
                    out.write("</delete_bt_relations>");

                    out.write("<delete_from_classes>");

                    if (HiersFordeletion.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < HiersFordeletion.size(); k++) {

                            out.write(Utilities.escapeXML(HiersFordeletion.get(k)));
                            if (k < HiersFordeletion.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }
                    out.write("</delete_from_classes>");

                    out.write("<RelativeHier>");

                    if (shouldBeRenamedTo.size() == 0) {
                        out.write("-");
                    } else {

                        if (shouldBeRenamedTo.get(0).compareTo(topTerms.get(i)) == 0) {
                            out.write("-");
                        } else {
                            out.write(Utilities.escapeXML(shouldBeRenamedTo.get(0)));
                        }
                    }

                    out.write("</RelativeHier>");

                    out.write("</topterm>");

                } catch (java.io.IOException exc) {

                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TopTerm_Inconsistencies_Preview: Error in opening/writing file: " + exc.getMessage());
                    Utils.StaticClass.handleException(exc);
                }
            }//If error top term was reported
        }//end of for loop

    }

    void TopTerm_Inconsistencies_Fix(String selectedThesaurus, QClass Q, IntegerObject sis_session, Boolean fixed, Vector<String> topTerms) {

        //tools
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        topTerms.trimToSize();
        Q.reset_name_scope();
        String prefix_Descr = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        String prefix_Class = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        for (int i = 0; i < topTerms.size(); i++) {

            Q.reset_name_scope();
            String termName = new String(topTerms.get(i)); //might change

            Vector<String> BTsFordeletion = new Vector<String>();
            Vector<String> HiersFordeletion = new Vector<String>();
            Vector<String> shouldBeRenamedTo = new Vector<String>();

            TopTermsBugDetection(selectedThesaurus, Q, sis_session, topTerms.get(i), BTsFordeletion, HiersFordeletion, shouldBeRenamedTo);

            if (BTsFordeletion.size() > 0 || HiersFordeletion.size() > 0 || (shouldBeRenamedTo.size() > 0 && shouldBeRenamedTo.get(0).compareTo(topTerms.get(i)) != 0)) {

                //Rename TopTerm or its relative hierarchy so that they hava common names - different prefixes
                if (shouldBeRenamedTo.size() == 1 && shouldBeRenamedTo.get(0).compareTo(termName) != 0) {

                    Identifier I_top_term_old = new Identifier(prefix_Descr.concat(termName));
                    Identifier I_top_term_new = new Identifier(prefix_Descr.concat(shouldBeRenamedTo.get(0)));
                    //try to rename top term to hierarchy name
                    if (Q.CHECK_Rename_Node(I_top_term_old, I_top_term_new) == QClass.APIFail) {

                        //if fail try to rename hierarchy to its relative top term
                        Identifier I_hier_old = new Identifier(prefix_Class.concat(shouldBeRenamedTo.get(0)));
                        Identifier I_hier_new = new Identifier(prefix_Class.concat(termName));
                        if (Q.CHECK_Rename_Node(I_hier_old, I_hier_new) == QClass.APISucc) {
                            //If new name exists in the deletion from hierarchy vector skip this deletion
                            //Term name remains the same
                            //Hier must change

                            int index = HiersFordeletion.indexOf(termName);
                            if (index >= 0) {
                                HiersFordeletion.removeElementAt(index);
                                HiersFordeletion.trimToSize();
                            }
                        } else {//Else no default fix action is performed
                            fixed = false;
                        }

                    } else { //If new name exists in the deletion from hierarchy vector skip this deletion
                        //Also Update term name
                        termName = shouldBeRenamedTo.get(0);
                        int index = HiersFordeletion.indexOf(shouldBeRenamedTo.get(0));

                        if (index >= 0) {
                            HiersFordeletion.removeElementAt(index);
                            HiersFordeletion.trimToSize();
                        }
                    }

                }

                //Renames have been performed. Deletions of BTs and Hiers must also be applied
                //Delete hierarchies
                for (int m = 0; m < HiersFordeletion.size(); m++) {
                    Q.reset_name_scope();
                    Identifier from = new Identifier(prefix_Descr.concat(termName));
                    Identifier to = new Identifier(prefix_Class.concat(HiersFordeletion.get(m)));

                    int ret = Q.CHECK_Delete_Instance(from, to);
                    if (ret == QClass.APIFail) {
                        fixed = false;
                        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+ret);
                    }
                }

                //Delete BTs
                if (BTsFordeletion.size() > 0) {

                    DeleteTopTermBts(selectedThesaurus, Q, sis_session, fixed, prefix_Descr.concat(termName));

                }
            }//If error top term was reported

        }//end of for loop

        //Patch for top terms declassification from THES1Descriptor
        Q.reset_name_scope();
        StringObject topTermObj = new StringObject();
        StringObject thes1_DescrObj = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), topTermObj);
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thes1_DescrObj);

        Q.reset_name_scope();
        Q.set_current_node(topTermObj);
        int set_tts = Q.get_all_instances(0);

        Q.reset_name_scope();
        Q.set_current_node(thes1_DescrObj);
        int set_descr = Q.get_all_instances(0);

        Q.reset_set(set_descr);
        Q.reset_set(set_tts);
        Q.set_intersect(set_tts, set_descr);

        Vector<String> declassifyTTs = dbGen.get_Node_Names_Of_Set(set_tts, false, Q, sis_session);

        for (int i = 0; i < declassifyTTs.size(); i++) {
            Q.reset_name_scope();
            long fromIdL = Q.set_current_node(new StringObject(declassifyTTs.get(i)));
            Q.reset_name_scope();
            long toIdL = Q.set_current_node(thes1_DescrObj);
            Identifier from = new Identifier(fromIdL);
            Identifier to = new Identifier(toIdL);
            Q.reset_name_scope();
            if (Q.CHECK_Delete_Instance(from, to) == QClass.APIFail) {
                fixed = false;
            }

        }

        Q.free_all_sets();

    }

    boolean TopTermsBugDetection(String selectedThesaurus, QClass Q, IntegerObject sis_session, String checkNode, Vector<String> BTsFordeletion, Vector<String> HiersFordeletion, Vector<String> RelatedHier) {

        //tools
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        int SisSessionId = sis_session.getValue();
        Q.reset_name_scope();

        String prefixterm = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());

        StringObject checkNodeObj = new StringObject(prefixterm.concat(checkNode));
        StringObject HierarchyObj = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("ThesaurusNotionType"));
        StringObject TopTermClassObj = new StringObject();
        StringObject belongsToHierObj = new StringObject();
        StringObject btClassObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btClassObj, btLinkObj, Q, sis_session);

        dbtr.getThesaurusCategory_belongs_to_hierarchy(selectedThesaurus, Q, sis_session.getValue(), belongsToHierObj);
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTermClassObj);
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), HierarchyObj);

        Q.reset_name_scope();

        if (Q.set_current_node(checkNodeObj) != QClass.APIFail) {

            int target_top_term = Q.set_get_new();
            Q.set_put(target_top_term);
            Q.reset_set(target_top_term);

            int set_bts = Q.set_get_new();
            dbGen.collect_Direct_Links_Of_Set(target_top_term, set_bts, false, btClassObj.getValue(), btLinkObj.getValue(), ConstantParameters.FROM_Direction, Q, sis_session);
            Q.reset_set(set_bts);

            /*
             if(Q.set_get_card(set_bts)>0){                
             //error detected
             }
             */
            //COLLECT TARGET TERM'S CLASSES And get their TOP Terms
            Q.reset_set(target_top_term);
            int set_classes = Q.get_classes(target_top_term);
            Q.reset_set(set_classes);

            Q.reset_name_scope();
            Q.set_current_node(HierarchyObj);
            int set_class_filter = Q.get_all_instances(0);
            Q.reset_set(set_class_filter);

            Q.set_intersect(set_classes, set_class_filter);
            Q.reset_set(set_classes);

            int set_class_relation = Q.get_to_node_by_category(target_top_term, TopTermClassObj, belongsToHierObj);
            Q.reset_set(set_class_relation);

            /*
             if(Q.set_get_card(set_class_relation)==0){
             //error detected
             }
             */
            Q.set_difference(set_classes, set_class_relation);
            Q.reset_set(set_classes);

            /*
             if(Q.set_get_card(set_classes)>0){
             //error detected belonging to more classes than it should or should be renamed error
             }
             */
            HiersFordeletion.addAll(dbGen.get_Node_Names_Of_Set(set_classes, true, Q, sis_session));
            BTsFordeletion.addAll(dbGen.get_Node_Names_Of_Set(set_bts, true, Q, sis_session));
            RelatedHier.addAll(dbGen.get_Node_Names_Of_Set(set_class_relation, true, Q, sis_session));

            Q.free_all_sets();
        } else {
            return false;
        }
        return true;
    }

    void DeleteTopTermBts(String selectedThesaurus, QClass Q, IntegerObject sis_session, Boolean fixed, String topTerm) {
        //Delete All Tops Term's BTs

        //tools
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thesDescriptor);

        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), thesBT);

        Q.reset_name_scope();

        long TopTermIdL = Q.set_current_node(new StringObject(topTerm));
        int BT_links_set = Q.get_link_from_by_category(0, thesDescriptor, thesBT);

        Q.reset_set(BT_links_set);

        long BTlink_sysidL = QClass.APIFail;
        Vector<Long> BTlinks_Ids = new Vector<Long>();

        Vector<Return_Link_Id_Row> retVals = new Vector<Return_Link_Id_Row>();
        if (Q.bulk_return_link_id(BT_links_set, retVals) != QClass.APIFail) {
            for (Return_Link_Id_Row row : retVals) {
                BTlink_sysidL = row.get_v3_sysid();
                BTlinks_Ids.add(BTlink_sysidL);
            }
        }


        /*
         StringObject node_name = new StringObject();
         IntegerObject BTlink_sysid = new IntegerObject();
         Vector<Long> BTlinks_Ids = new Vector<Long>();
         CMValue cmv = new CMValue();
         //while (Q.retur_link_id(BT_links_set, node_name, new IntegerObject(), BTlink_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
         BTlinks_Ids.add(BTlink_sysid.getValue());
         }
         */
        BTlinks_Ids.trimToSize();
        Q.free_set(BT_links_set);
        int ret;
        for (int i = 0; i < BTlinks_Ids.size(); i++) {
            Identifier BTlinkID = new Identifier(BTlinks_Ids.get(i).intValue());
            // named link

            //if (dbGen.isNamedLink(BTlink_sysidL) == true) {
            if (Q.CHECK_isUnNamedLink(BTlink_sysidL) == false) {
                Identifier TargetTermID = new Identifier(TopTermIdL);
                ret = Q.CHECK_Delete_Named_Attribute(BTlinkID, TargetTermID);
            } // unnamed link
            else {
                ret = Q.CHECK_Delete_Unnamed_Attribute(BTlinkID);
            }
            if (ret == QClass.APIFail) {
                fixed = false;
            }
        }
    }

    void Terms_Without_BTs(UserInfoClass SessionUserInfo, Locale targetLocale, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Terms_Without_BTs ");
                    return;
                }

                Q.TEST_begin_query();

            } else {
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Terms_Without_BTs ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            StringObject thesaurusTopTermClassObj = new StringObject();
            StringObject HierarchyObj = new StringObject();
            StringObject belongsToHierarchyObj = new StringObject();
            StringObject btFromClassObj = new StringObject();
            StringObject btLinkNameObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, btFromClassObj, btLinkNameObj, Q, sis_session);

            String prefix_term = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            String prefix_Class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), thesaurusTopTermClassObj);
            dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), HierarchyObj);
            dbtr.getThesaurusCategory_belongs_to_hierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), belongsToHierarchyObj);

            Q.reset_name_scope();
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node(btFromClassObj);
            Q.set_current_node(btLinkNameObj);
            Q.set_put(catSet);
            Q.reset_set(catSet);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            //collect all descriptors apart from top terms
            Q.reset_name_scope();
            int set_all_descr = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session); // Q.get_all_instances(0);

            Q.reset_name_scope();
            Q.set_current_node(thesaurusTopTermClassObj);
            int set_all_tt = Q.get_all_instances(0);

            Q.reset_set(set_all_descr);
            Q.reset_set(set_all_tt);
            Q.set_difference(set_all_descr, set_all_tt);
            Q.reset_set(set_all_descr);

            int set_temp_bts = Q.set_get_new();
            dbGen.collect_Direct_Links_Of_Set(set_all_descr, set_temp_bts, false, btFromClassObj.getValue(), btLinkNameObj.getValue(), ConstantParameters.FROM_Direction, Q, sis_session);

            Q.reset_set(set_temp_bts);

            int set_temp_descr = Q.set_get_new();
            dbGen.collect_Direct_Links_Of_Set(set_temp_bts, set_temp_descr, false, btFromClassObj.getValue(), btLinkNameObj.getValue(), ConstantParameters.TO_Direction, Q, sis_session);

            Q.reset_set(set_temp_descr);

            Q.set_difference(set_all_descr, set_temp_descr);
            Q.reset_set(set_all_descr);

            Q.reset_name_scope();
            Q.set_current_node(HierarchyObj);
            int set_class_filter = Q.get_all_instances(0);
            Q.reset_set(set_class_filter);

            Vector<String> set_DescrNames = dbGen.get_Node_Names_Of_Set(set_all_descr, false, Q, sis_session);
            set_DescrNames.trimToSize();

            Collections.sort(set_DescrNames, new StringLocaleComparator(targetLocale));

            Q.free_set(set_all_descr);
            Q.free_set(set_temp_descr);
            Q.free_set(set_temp_bts);
            Q.free_set(set_all_tt);

            for (int k = 0; k < set_DescrNames.size(); k++) {

                Q.reset_name_scope();
                Q.set_current_node(new StringObject(set_DescrNames.get(k)));
                int set_current_node_classes = Q.get_classes(0);
                Q.reset_set(set_current_node_classes);
                Q.set_intersect(set_current_node_classes, set_class_filter);
                Q.reset_set(set_current_node_classes);

                //get the hierarchies relative Top terms (perhaps not in the same name though wrong)
                int set_add_BTs = Q.get_from_node_by_category(set_current_node_classes, thesaurusTopTermClassObj, belongsToHierarchyObj);
                Q.reset_set(set_add_BTs);

                if (mode.compareTo("Preview") == 0) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(set_DescrNames.get(k))));
                    out.write("</name>");
                    out.write("<belongs_to_Hiers>");
                    if (Q.set_get_card(set_current_node_classes) > 0) {

                        Vector<String> belongs_to_Hiers = dbGen.get_Node_Names_Of_Set(set_current_node_classes, true, Q, sis_session);

                        for (int m = 0; m < belongs_to_Hiers.size(); m++) {
                            out.write(Utilities.escapeXML(belongs_to_Hiers.get(m)));

                            if (m < belongs_to_Hiers.size() - 1) {
                                out.write(", ");
                            }
                        }

                    } else {
                        out.write("-");
                    }

                    out.write("</belongs_to_Hiers>");
                    out.write("<addBt>");

                    if (Q.set_get_card(set_add_BTs) > 0) {

                        Vector<String> add_BTs = dbGen.get_Node_Names_Of_Set(set_add_BTs, true, Q, sis_session);

                        for (int m = 0; m < add_BTs.size(); m++) {
                            out.write(Utilities.escapeXML(add_BTs.get(m)));

                            if (m < add_BTs.size() - 1) {
                                out.write(", ");
                            }
                        }

                    } else {
                        out.write(Parameters.UnclassifiedTermsLogicalname);
                    }
                    out.write("</addBt>");
                    out.write("</concept>");
                } else if (mode.compareTo("Fix") == 0) {

                    //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
                    Q.reset_name_scope();

                    Vector<String> add_BTs = null;
                    StringObject term = new StringObject(set_DescrNames.get(k));
                    if (Q.set_get_card(set_add_BTs) > 0) {
                        add_BTs = new Vector<String>();
                        add_BTs.addAll(dbGen.get_Node_Names_Of_Set(set_add_BTs, false, Q, sis_session));

                    } else {
                        add_BTs = new Vector<String>();
                        add_BTs.add(prefix_term.concat(Parameters.UnclassifiedTermsLogicalname));
                        //Add hierarchy instance
                        Q.reset_name_scope();
                        Identifier from = new Identifier(term.getValue());
                        Identifier to = new Identifier(prefix_Class.concat(Parameters.UnclassifiedTermsLogicalname));
                        int ret = Q.CHECK_Add_Instance(from, to);
                        if (ret == QClass.APIFail) {
                            fixed = false;                        //Add Unclassified as BT
                            //out.write(Parameters.UnclassifiedTermsLogicalname);
                        }
                    }

                    StringObject prevThes = new StringObject();
                    TA.GetThesaurusNameWithoutPrefix(prevThes);
                    if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                        TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                    }
                    //Crete BT Connections towrards its relevant Hierarchies Top Terms
                    for (int m = 0; m < add_BTs.size(); m++) {
                        //All descriptors are new
                        Q.reset_name_scope();
                        String CMValString = add_BTs.get(m);
                        long CMValIDL = Q.set_current_node(new StringObject(CMValString));
                        CMValue cmv = new CMValue();
                        cmv.assign_node(CMValString, CMValIDL);

                        Q.reset_name_scope();
                        int ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), term, cmv, catSet);
                        if (ret == TMSAPIClass.TMS_APIFail) {
                            fixed = false;
                        }
                    }
                    //reset to previous thesaurus name if needed
                    if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                        TA.SetThesaurusName(prevThes.getValue());
                    }

                }

                Q.free_set(set_current_node_classes);
                Q.free_set(set_add_BTs);

            }//For loop ended
            Q.free_set(catSet);
            Q.free_set(set_class_filter);

            if (mode.compareTo("Preview") == 0) {

                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Terms_Without_BTs: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Terms_Wrong_Unclassified(String selectedThesaurus, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            //tools
            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Terms_Wrong_Unclassified ");
                    return;
                }
                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Terms_Wrong_Unclassified ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            StringObject btFromClassObj = new StringObject();
            StringObject btLinkNameObj = new StringObject();

            String prefix_term = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
            dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), btFromClassObj);
            dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), btLinkNameObj);

            Q.reset_name_scope();
            Q.set_current_node(new StringObject(prefix_term.concat(Parameters.UnclassifiedTermsLogicalname)));
            int set_unclassified_nts_labels = Q.get_link_to_by_category(0, btFromClassObj, btLinkNameObj);
            Q.reset_set(set_unclassified_nts_labels);
            int set_unclassified_nts = Q.get_from_value(set_unclassified_nts_labels);
            Q.reset_set(set_unclassified_nts);

            Vector<String> unclassified_nts_Names = dbGen.get_Node_Names_Of_Set(set_unclassified_nts, false, Q, sis_session);
            Vector<String> WrongTerms = new Vector<String>();

            //Detect / fix Wrong Unclassified Bts
            for (int k = 0; k < unclassified_nts_Names.size(); k++) {

                Q.reset_name_scope();
                Q.set_current_node(new StringObject(unclassified_nts_Names.get(k)));
                int set_direct_bts_labels = Q.get_link_from_by_category(0, btFromClassObj, btLinkNameObj);
                Q.reset_set(set_direct_bts_labels);
                int set_direct_bts = Q.get_to_value(set_direct_bts_labels);
                Q.reset_set(set_direct_bts);

                if (Q.set_get_card(set_direct_bts) > 1) {

                    if (mode.compareTo("Fix") == 0) {

                        //IntegerObject link_sysid = new IntegerObject();
                        //CMValue cmv = new CMValue();
                        Q.reset_name_scope();
                        Q.reset_set(set_direct_bts_labels);
                        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                        if (Q.bulk_return_link(set_direct_bts_labels, retVals) != QClass.APIFail) {
                            for (Return_Link_Row row : retVals) {
                                if (row.get_v3_cmv().getType() != CMValue.TYPE_NODE) {
                                    continue;
                                }
                                String temp = dbGen.removePrefix(row.get_v3_cmv().getString());

                                if (temp.compareTo(Parameters.UnclassifiedTermsLogicalname) == 0) {

                                    //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
                                    //int ret = WTA.DeleteNewDescriptorAttribute(selectedThesaurus, row.get_Neo4j_NodeId(), new StringObject(unclassified_nts_Names.get(k)));
                                    StringObject prevThes = new StringObject();
                                    TA.GetThesaurusNameWithoutPrefix(prevThes);
                                    if (prevThes.getValue().equals(selectedThesaurus) == false) {
                                        TA.SetThesaurusName(selectedThesaurus);
                                    }
                                    int ret = TA.CHECK_DeleteNewDescriptorAttribute(row.get_Neo4j_NodeId(), new StringObject(unclassified_nts_Names.get(k)));
                                    if (ret == TMSAPIClass.TMS_APIFail) {
                                        fixed = false;
                                    }
                                    if (prevThes.getValue().equals(selectedThesaurus) == false) {
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    break;
                                }
                            }
                        }

                        /*
                         //while (Q.retur_link_id(set_direct_bts_labels, new StringObject(), new IntegerObject(), link_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
                         if (cmv.getType() != CMValue.TYPE_NODE) {
                         continue;
                         }
                         String temp = dbGen.removePrefix(cmv.getString());

                         if (temp.compareTo(Parameters.UnclassifiedTermsLogicalname) == 0) {

                         //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
                         int ret = WTA.DeleteNewDescriptorAttribute(selectedThesaurus, link_sysid.getValue(), new StringObject(unclassified_nts_Names.get(k)));
                         if (ret == TMSAPIClass.TMS_APIFail) {
                         fixed = false;
                         }
                         break;
                         }
                         }
                         */
                    }

                    WrongTerms.add(dbGen.removePrefix(unclassified_nts_Names.get(k)));
                }

                Q.free_set(set_direct_bts_labels);
                Q.free_set(set_direct_bts);
            }

            Q.free_set(set_unclassified_nts_labels);
            Q.free_set(set_unclassified_nts);

            //PERFORM CHECKS AND XML WRITING IF PREVIEW
            if (mode.compareTo("Preview") == 0) {

                for (int k = 0; k < WrongTerms.size(); k++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(WrongTerms.get(k)));
                    out.write("</name>");
                    out.write("</concept>");
                }
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();

            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Terms_Wrong_Unclassified: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Classes_and_BTs(UserInfoClass SessionUserInfo, Locale targetLocale, String mode, Boolean fixed, String title, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        int index = Parameters.CLASS_SET.indexOf("TERM");

        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        if (mode.compareTo("Fix") == 0) {

            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Classes_and_BTs ");
                return;
            }

            Q.TEST_begin_transaction();
            int set_1 = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Vector<String> decr = dbGen.get_Node_Names_Of_Set(set_1, true, Q, sis_session);
            decr.trimToSize();
            Q.free_set(set_1);

            for (int i = 0; i < decr.size(); i++) {
                MoveToHierBugFix(SessionUserInfo.selectedThesaurus, Q, sis_session, mode, fixed, decr.get(i), new Vector<String>(), new Vector<String>());
            }

            Q.TEST_end_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            return;
        }

        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";
        try {
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter out = new OutputStreamWriter(bout, "UTF-8");

            out.write(ConstantParameters.xmlHeader);

            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Classes_and_BTs ");
                return;
            }

            Q.TEST_begin_query();

            int set_1 = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);

            Vector<String> decr = dbGen.get_Node_Names_Of_Set(set_1, true, Q, sis_session);
            Q.free_set(set_1);
            decr.trimToSize();

            Collections.sort(decr, new StringLocaleComparator(targetLocale));

            out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
            out.write("<title>" + title + "</title>");
            out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");
            for (int i = 0; i < decr.size(); i++) {

                Vector<String> deletion = new Vector<String>();
                Vector<String> addtion = new Vector<String>();
                MoveToHierBugFix(SessionUserInfo.selectedThesaurus, Q, sis_session, mode, fixed, decr.get(i), deletion, addtion);
                deletion.trimToSize();
                addtion.trimToSize();
                if (deletion.size() > 0 || addtion.size() > 0) {
                    out.write("<term>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(decr.get(i)));
                    out.write("</name>");

                    out.write("<delete_from_classes>");

                    if (deletion.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < deletion.size(); k++) {

                            out.write(Utilities.escapeXML(deletion.get(k)));
                            if (k < deletion.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }
                    out.write("</delete_from_classes>");

                    out.write("<add_to_classes>");
                    if (addtion.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < addtion.size(); k++) {

                            out.write(Utilities.escapeXML(addtion.get(k)));
                            if (k < addtion.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }
                    out.write("</add_to_classes>");

                    out.write("</term>");
                }
                out.flush();
            }

            out.write("</page>");
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            out.close();

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Classes_and_BTs Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    boolean MoveToHierBugFix(String selectedThesaurus, QClass Q, IntegerObject sis_session, String mode, Boolean fixed, String checkNode, Vector<String> deleteClassInstances, Vector<String> addClassInstances) {

        //tools
        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        int SisSessionId = sis_session.getValue();
        Q.reset_name_scope();

        String prefixClass = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());
        String prefixterm = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject HierarchyObj = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("ThesaurusNotionType"));
        StringObject TopTermObjClass = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("TopTerm"));
        StringObject checkNodeObj = new StringObject(prefixterm.concat(checkNode));
        StringObject TopTermHierRelationObj = new StringObject();//("belongs_to_" + SessionUserInfo.selectedThesaurus.toLowerCase().concat("_hierarchy"));

        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), HierarchyObj);
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTermObjClass);
        dbtr.getThesaurusCategory_belongs_to_hierarchy(selectedThesaurus, Q, sis_session.getValue(), TopTermHierRelationObj);
        Q.reset_name_scope();

        if (Q.set_current_node(checkNodeObj) != QClass.APIFail) {

            //COLLECT ALL TARGET NODE'S BTS UNTIL TOP TERMS
            int target_set = Q.set_get_new();
            Q.set_put(target_set);
            Q.reset_set(target_set);

            int set_recursive_bts = Q.set_get_new();
            dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus, target_set, set_recursive_bts, false, Q, sis_session);
            Q.reset_set(set_recursive_bts);

            //COLLECT TARGET TERM'S CLASSES And get their TOP Terms
            Q.reset_set(target_set);
            int set_classes = Q.get_classes(target_set);
            Q.reset_set(set_classes);

            Q.reset_name_scope();
            Q.set_current_node(HierarchyObj);
            int set_class_filter = Q.get_all_instances(0);
            Q.reset_set(set_class_filter);

            Q.set_intersect(set_classes, set_class_filter);
            Q.reset_set(set_classes);

            int set_classes_topterms = Q.get_from_node_by_category(set_classes, TopTermObjClass, TopTermHierRelationObj);
            Q.reset_set(set_classes_topterms);

            Q.reset_name_scope();

            //GET TOP TERM INSTANCES in order to filter recursive BTs of target
            Q.reset_name_scope();
            Q.set_current_node(TopTermObjClass);
            int set_top_terms = Q.get_instances(0);
            Q.reset_set(set_top_terms);

            //FILTER TARGETTERM'S RECURSIVE BTS WITH TOPTERM INSATNCES
            Q.set_intersect(set_recursive_bts, set_top_terms);
            Q.reset_set(set_recursive_bts);

            //MAKE A COPY OF TARGET TERM'S FILTERED CLASSES TopTerms
            int set_classes_topterms_copy = Q.set_get_new();
            Q.set_copy(set_classes_topterms_copy, set_classes_topterms);

            //FIND OUT WHICH CLASSES OF TARGET NODE SHOULD BE DELETED (ALL RECURSIVE BTS OF TARGET DO NOT INCLUDE THEIR RELEVANT TOP TERMS)
            Q.set_difference(set_classes_topterms, set_recursive_bts);
            Q.reset_set(set_classes_topterms);

            if (Q.set_get_card(set_classes_topterms) > 0) {

                if (mode.compareTo("Preview") == 0) {
                    //TopTermCase
                    deleteClassInstances.addAll(dbGen.get_Node_Names_Of_Set(set_classes_topterms, true, Q, sis_session));
                    int index = deleteClassInstances.indexOf(checkNode);
                    if (index >= 0) {
                        deleteClassInstances.removeElementAt(index);
                    }

                } else {
                    Vector<String> deletions = dbGen.get_Node_Names_Of_Set(set_classes_topterms, true, Q, sis_session);
                    deletions.trimToSize();

                    int ret;
                    for (int k = 0; k < deletions.size(); k++) {

                        if (checkNode.compareTo(deletions.get(k)) == 0) {
                            continue;
                        }
                        Identifier from = new Identifier(checkNodeObj.getValue());
                        Identifier to = new Identifier(prefixClass.concat(deletions.get(k)));

                        ret = Q.CHECK_Delete_Instance(from, to);
                        if (ret == QClass.APIFail) {
                            fixed = false;
                        }
                    }
                }
            }

            Q.set_difference(set_recursive_bts, set_classes_topterms_copy);
            Q.reset_set(set_recursive_bts);

            if (Q.set_get_card(set_recursive_bts) > 0) {

                if (mode.compareTo("Preview") == 0) {
                    addClassInstances.addAll(dbGen.get_Node_Names_Of_Set(set_recursive_bts, true, Q, sis_session));
                } else {
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nAdd class Instances:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n"));  
                    Vector<String> addInstancesClasses = dbGen.get_Node_Names_Of_Set(set_recursive_bts, true, Q, sis_session);
                    addInstancesClasses.trimToSize();
                    int ret;
                    for (int k = 0; k < addInstancesClasses.size(); k++) {

                        Identifier from = new Identifier(checkNodeObj.getValue());
                        Identifier to = new Identifier(prefixClass.concat(addInstancesClasses.get(k)));
                        ret = Q.CHECK_Add_Instance(from, to);
                        if (ret == QClass.APIFail) {
                            fixed = false;
                        }
                    }

                }

            }

        } else {
            Q.free_all_sets();
            return false;
        }

        Q.reset_name_scope();
        Q.free_all_sets();
        return true;

    }

    void RTs_Errors(UserInfoClass SessionUserInfo, Locale targetLocale, String mode, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RTs_Errors ");
                    return;
                }

                Q.TEST_begin_query();

            } else {
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ RTs_Errors ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            //handle scope note
            Q.reset_name_scope();
            StringObject rtFromClassObj = new StringObject();
            StringObject rtLinkObj = new StringObject();
            StringObject btFromClassObj = new StringObject();
            StringObject btLinkObj = new StringObject();
            dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), rtFromClassObj);
            dbtr.getThesaurusCategory_RT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), rtLinkObj);
            dbtr.getThesaurusClass_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), btFromClassObj);
            dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), btLinkObj);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_1 = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Vector<String> set_1Names = dbGen.get_Node_Names_Of_Set(set_1, false, Q, sis_session);
            Q.free_set(set_1);

            for (int i = 0; i < set_1Names.size(); i++) {

                Q.reset_name_scope();
                //Create set_1
                StringObject currentNodeObj = new StringObject(set_1Names.get(i));
                if (Q.set_current_node(currentNodeObj) == QClass.APIFail) {
                    continue;
                }
                int set_target_node = Q.set_get_new();
                Q.set_put(set_target_node);
                Q.reset_set(set_target_node);

                int set_rts = Q.set_get_new();
                dbGen.collect_Direct_Links_Of_Set(set_target_node, set_rts, false, rtFromClassObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction, Q, sis_session);                
                Q.reset_set(set_rts);
                //int card1 = Q.set_get_card(set_rts);

                int set_all_bts = Q.set_get_new();
                dbGen.collect_Recurcively_ALL_BTs_Of_Set(SessionUserInfo.selectedThesaurus, set_target_node, set_all_bts, false, Q, sis_session);
                Q.reset_set(set_all_bts);
                //int card2 = Q.set_get_card(set_all_bts);

                int set_all_nts = Q.set_get_new();
                dbGen.collect_Recurcively_ALL_NTs_Of_Set(SessionUserInfo.selectedThesaurus, set_target_node, set_all_nts, false, Q, sis_session);
                Q.reset_set(set_all_nts);
                //int card3 = Q.set_get_card(set_all_nts);

                Q.set_union(set_all_bts, set_all_nts);
                Q.reset_set(set_all_bts);
                Q.set_intersect(set_rts, set_all_bts);
                Q.reset_set(set_rts);

                Q.free_set(set_target_node);
                Q.free_set(set_all_bts);
                Q.free_set(set_all_nts);

                if (Q.set_get_card(set_rts) == 0) {

                    Q.free_all_sets();

                    continue;
                }

                if (mode.compareTo("Preview") == 0) {

                    Vector<String> deleteRts = dbGen.get_Node_Names_Of_Set(set_rts, true, Q, sis_session);
                    Q.free_all_sets();

                    deleteRts.trimToSize();

                    Collections.sort(deleteRts, new StringLocaleComparator(targetLocale));

                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(set_1Names.get(i))));
                    out.write("</name>");
                    out.write("<delete_rt>");
                    for (int k = 0; k < deleteRts.size(); k++) {
                        out.write(Utilities.escapeXML(deleteRts.get(k)));
                        if (k < deleteRts.size() - 1) {
                            out.write(", ");
                        }
                    }
                    out.write("</delete_rt>");
                    out.write("</concept>");
                } else if (mode.compareTo("Fix") == 0) {

                    Vector<String> deleteRts = dbGen.get_Node_Names_Of_Set(set_rts, false, Q, sis_session);
                    Q.free_all_sets();
                    deleteRts.trimToSize();

                    for (int k = 0; k < deleteRts.size(); k++) {

                        Q.reset_name_scope();
                        Q.set_current_node(currentNodeObj);
                        int set_from_rts = Q.get_link_from_by_category(0, rtFromClassObj, rtLinkObj);
                        Q.reset_set(set_from_rts);

                        Vector<Long> deleteIDs = new Vector<Long>();

                        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
                        //IntegerObject linkID = new IntegerObject();
                        //CMValue cmv = new CMValue();
                        String checkRT = deleteRts.get(k);

                        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                        if (Q.bulk_return_link(set_from_rts, retVals) != QClass.APIFail) {
                            for (Return_Link_Row row : retVals) {
                                if (row.get_v3_cmv().getString() != null && row.get_v3_cmv().getString().compareTo(checkRT) == 0) {
                                    deleteIDs.add(row.get_Neo4j_NodeId());
                                }
                            }
                        }
                        /*
                         //while (Q.retur_link_id(set_from_rts, new StringObject(), new IntegerObject(), linkID, cmv, new IntegerObject()) != QClass.APIFail) {
                         if (cmv.getString() != null && cmv.getString().compareTo(checkRT) == 0) {
                         deleteIDs.add(linkID.getValue());
                         }
                         }
                         */
                        StringObject prevThes = new StringObject();
                        TA.GetThesaurusNameWithoutPrefix(prevThes);
                        if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                            TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                        }

                        for (int m = 0; m < deleteIDs.size(); m++) {

                            TA.CHECK_DeleteNewDescriptorAttribute(deleteIDs.get(m).intValue(), currentNodeObj);
                        }
                        //reset to previous thesaurus name if needed
                        if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                        Q.free_set(set_from_rts);

                    }
                }

            }

            if (mode.compareTo("Preview") == 0) {

                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RTs_Errors: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Unclassified_Sources(String selectedThesaurus, Locale targetLocale, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Sources ");
                    return;
                }

                Q.TEST_begin_query();

            } else {
                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Sources ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            //collect all thesaurus references that will be needed
            StringObject foundInClassObj = new StringObject();
            StringObject foundInLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.foundIn_kwd, foundInClassObj, foundInLinkObj, Q, sis_session);
            StringObject primaryfoundInClassObj = new StringObject();
            StringObject primaryfoundInLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.primary_found_in_kwd, primaryfoundInClassObj, primaryfoundInLinkObj, Q, sis_session);
            StringObject trfoundInClassObj = new StringObject();
            StringObject trfoundInLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translations_found_in_kwd, trfoundInClassObj, trfoundInLinkObj, Q, sis_session);
            StringObject toENClassObj = new StringObject();
            StringObject toENLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translation_kwd, toENClassObj, toENLinkObj, Q, sis_session);
            String prefixSource = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());

            //Collect unclassified Link labels of found in
            Q.reset_name_scope();
            Q.set_current_node(foundInClassObj);
            Q.set_current_node(foundInLinkObj);
            int set_unclassified_sources_labels = Q.get_instances(0);
            Q.reset_set(set_unclassified_sources_labels);

            if (Q.set_get_card(set_unclassified_sources_labels) > 0) {

                //Objects used for traversing of sets
                //StringObject fromcls = new StringObject();
                //StringObject label = new StringObject();
                //StringObject categ = new StringObject();
                //StringObject cls = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();
                //IntegerObject clsID = new IntegerObject();
                //IntegerObject sysID = new IntegerObject();
                //IntegerObject traversed = new IntegerObject();
                //IntegerObject categID = new IntegerObject();
                //CMValue cmv = new CMValue();
                if (mode.compareTo("Fix") == 0) {

                    //Collect ids of links that must be reclassified
                    Vector<Long> reClassifyIDs = new Vector<Long>();
                    Q.reset_name_scope();
                    Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                    if (Q.bulk_return_link(set_unclassified_sources_labels, retVals) != QClass.APIFail) {
                        for (Return_Link_Row row : retVals) {
                            reClassifyIDs.add(row.get_Neo4j_NodeId());
                        }
                    }
                    /*
                     //while (Q.retur_link_id(set_unclassified_sources_labels, cls, clsID, sysID, cmv, traversed) != QClass.APIFail) {
                     reClassifyIDs.add(sysID.getValue());
                     }
                     */

                    //get sysid of aaa_found_in
                    Q.reset_name_scope();
                    Q.set_current_node(foundInClassObj);
                    long foundInIDL = Q.set_current_node(foundInLinkObj);

                    //get sysid of aaa_primary_found_in
                    Q.reset_name_scope();
                    Q.set_current_node(primaryfoundInClassObj);
                    long GtFoundInIDL = Q.set_current_node(primaryfoundInLinkObj);

                    Identifier I_foundIn = new Identifier(foundInIDL);
                    Identifier I_GTFoundIn = new Identifier(GtFoundInIDL);

                    //classify each link to aaa_primary_found_in and declassify from aaa_found_in
                    for (int m = 0; m < reClassifyIDs.size(); m++) {
                        Q.reset_name_scope();
                        Identifier I_link = new Identifier(reClassifyIDs.get(m));

                        if (Q.CHECK_Add_Instance(I_link, I_GTFoundIn) == QClass.APIFail) {
                            fixed = false;
                            //do not perform deletion if addition failed
                        } else if (Q.CHECK_Delete_Instance(I_link, I_foundIn) == QClass.APIFail) {
                            fixed = false;
                        }
                    }

                } else if (mode.compareTo("Preview") == 0) {

                    int set_sources = Q.get_to_value(set_unclassified_sources_labels);
                    Q.reset_set(set_sources);

                    Vector<String> sources = dbGen.get_Node_Names_Of_Set(set_sources, true, Q, sis_session);
                    sources.trimToSize();

                    for (int m = 0; m < sources.size(); m++) {

                        StringObject currentSourceObj = new StringObject(prefixSource.concat(sources.get(m)));

                        //Get terms related to unclassified found_in labels
                        Q.reset_name_scope();
                        Q.set_current_node(currentSourceObj);
                        int set_related_terms_labels = Q.get_link_to_by_category(0, foundInClassObj, foundInLinkObj);
                        Q.reset_set(set_related_terms_labels);

                        //collect term names tha have unclassified source links
                        Vector<String> related_terms = new Vector<String>();
                        Vector<Return_Full_Link_Row> retFLIVals = new Vector<Return_Full_Link_Row>();
                        if (Q.bulk_return_full_link(set_related_terms_labels, retFLIVals) != QClass.APIFail) {
                            for (Return_Full_Link_Row row : retFLIVals) {
                                if (row.get_v3_categ().compareTo(foundInLinkObj.getValue()) == 0) {
                                    related_terms.add(row.get_v1_cls());
                                }
                            }
                        }

                        /*
                         //while (Q.retur_full_link_id(set_related_terms_labels, cls, clsID, label, sysID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                         if (categ.getValue().compareTo(foundInLinkObj.getValue()) == 0) {
                         related_terms.add(cls.getValue());
                         }
                         }
                         */
                        //Sort
                        Collections.sort(related_terms, new StringLocaleComparator(targetLocale));

                        for (int n = 0; n < related_terms.size(); n++) {

                            Q.reset_name_scope();
                            Q.set_current_node(new StringObject(related_terms.get(n)));
                            int set_greek_source = Q.get_link_from_by_category(0, primaryfoundInClassObj, primaryfoundInLinkObj);
                            Q.reset_set(set_greek_source);

                            int set_english_source = Q.get_link_from_by_category(0, trfoundInClassObj, trfoundInLinkObj);
                            Q.reset_set(set_english_source);

                            int set_to_en = Q.get_link_from_by_category(0, toENClassObj, toENLinkObj);
                            Q.reset_set(set_to_en);

                            out.write("<error>");

                            out.write("<source>");
                            out.write(Utilities.escapeXML(sources.get(m)));
                            out.write("</source>");

                            out.write("<term>");
                            out.write(Utilities.escapeXML(dbGen.removePrefix(related_terms.get(n))));
                            out.write("</term>");

                            out.write("<primary_src>");
                            if (Q.set_get_card(set_greek_source) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</primary_src>");

                            out.write("<translations_src>");
                            if (Q.set_get_card(set_english_source) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</translations_src>");

                            out.write("<translation>");
                            if (Q.set_get_card(set_to_en) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</translation>");

                            out.write("</error>");

                            Q.free_set(set_greek_source);
                            Q.free_set(set_english_source);
                            Q.free_set(set_to_en);

                        }// end of writer for loop

                        Q.free_set(set_related_terms_labels);
                    }//end of sources traverse for loop

                    Q.free_set(set_sources);
                }//end of preview case
            }

            Q.free_set(set_unclassified_sources_labels);

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Unclassified_Sources: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Unclassified_Editors(String selectedThesaurus, Locale targetLocale, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Editors ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Editors ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            //Collect all thesaurus references that will be needed
            StringObject editorClassObj = new StringObject();
            StringObject editorLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.editor_kwd, editorClassObj, editorLinkObj, Q, sis_session);
            StringObject createdByClassObj = new StringObject();
            StringObject createdByLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.created_by_kwd, createdByClassObj, createdByLinkObj, Q, sis_session);
            StringObject createdOnClassObj = new StringObject();
            StringObject createdOnLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClassObj, createdOnLinkObj, Q, sis_session);
            StringObject modifiedByClassObj = new StringObject();
            StringObject modifiedByLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClassObj, modifiedByLinkObj, Q, sis_session);
            StringObject modifiedOnClassObj = new StringObject();
            StringObject modifiedOnLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClassObj, modifiedOnLinkObj, Q, sis_session);
            String prefixEditor = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());

            //Collect all unclassifed editor link labels
            Q.reset_name_scope();
            Q.set_current_node(editorClassObj);
            Q.set_current_node(editorLinkObj);
            int set_unclassified_editors_labels = Q.get_instances(0);
            Q.reset_set(set_unclassified_editors_labels);

            if (Q.set_get_card(set_unclassified_editors_labels) > 0) {

                //Objects used for traversing of sets
                //StringObject fromcls = new StringObject();
                //StringObject label = new StringObject();
                //StringObject categ = new StringObject();
                //StringObject cls = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();
                //IntegerObject clsID = new IntegerObject();
                //IntegerObject sysID = new IntegerObject();
                //IntegerObject traversed = new IntegerObject();
                //IntegerObject categID = new IntegerObject();
                //CMValue cmv = new CMValue();
                if (mode.compareTo("Fix") == 0) {

                    //Collect ids of links that must be reclassified
                    Vector<Long> reClassifyIDs = new Vector<Long>();
                    Q.reset_name_scope();
                    Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                    if (Q.bulk_return_link(set_unclassified_editors_labels, retVals) != QClass.APIFail) {
                        for (Return_Link_Row row : retVals) {
                            reClassifyIDs.add(row.get_Neo4j_NodeId());
                        }
                    }

                    /*
                     //while (Q.retur_link_id(set_unclassified_editors_labels, cls, clsID, sysID, cmv, traversed) != QClass.APIFail) {
                     reClassifyIDs.add(sysID.getValue());
                     }
                     */
                    //get sysid of aaa_editor
                    Q.reset_name_scope();
                    Q.set_current_node(editorClassObj);
                    long editorIDL = Q.set_current_node(editorLinkObj);

                    //get sysid of aaa_created_by
                    Q.reset_name_scope();
                    Q.set_current_node(createdByClassObj);
                    long creatorIDL = Q.set_current_node(createdByLinkObj);

                    Identifier I_editor = new Identifier(editorIDL);
                    Identifier I_creator = new Identifier(creatorIDL);

                    //classify each link to aaa_created_by and declassify from aaa_editor
                    for (int m = 0; m < reClassifyIDs.size(); m++) {
                        Q.reset_name_scope();
                        Identifier I_link = new Identifier(reClassifyIDs.get(m));

                        if (Q.CHECK_Add_Instance(I_link, I_creator) == QClass.APIFail) {
                            fixed = false;
                            //do not perform deletion if addition failed
                        } else if (Q.CHECK_Delete_Instance(I_link, I_editor) == QClass.APIFail) {
                            fixed = false;
                        }
                    }

                } else if (mode.compareTo("Preview") == 0) {

                    int set_editors = Q.get_to_value(set_unclassified_editors_labels);
                    Q.reset_set(set_editors);

                    Vector<String> editors = dbGen.get_Node_Names_Of_Set(set_editors, true, Q, sis_session);

                    for (int m = 0; m < editors.size(); m++) {

                        StringObject currentEditorObj = new StringObject(prefixEditor.concat(editors.get(m)));

                        //Get terms related to unclassified editor labels
                        Q.reset_name_scope();
                        Q.set_current_node(currentEditorObj);
                        int set_related_terms_labels = Q.get_link_to_by_category(0, editorClassObj, editorLinkObj);
                        Q.reset_set(set_related_terms_labels);

                        //Store term names
                        Vector<String> related_terms = new Vector<String>();

                        Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
                        if (Q.bulk_return_full_link(set_related_terms_labels, retFLVals) != QClass.APIFail) {
                            for (Return_Full_Link_Row row : retFLVals) {
                                if (row.get_v3_categ().compareTo(editorLinkObj.getValue()) == 0) {
                                    related_terms.add(row.get_v1_cls());
                                }
                            }
                        }
                        /*
                         //while (Q.retur_full_link_id(set_related_terms_labels, cls, clsID, label, sysID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                         if (categ.getValue().compareTo(editorLinkObj.getValue()) == 0) {
                         related_terms.add(cls.getValue());
                         }
                         }*/

                        //Sort
                        Collections.sort(related_terms, new StringLocaleComparator(targetLocale));

                        //For each term get info about its created/modified links -- May be helpfull in order to decide where each link will be classified
                        for (int n = 0; n < related_terms.size(); n++) {

                            Q.reset_name_scope();
                            Q.set_current_node(new StringObject(related_terms.get(n)));
                            int set_created_by = Q.get_link_from_by_category(0, createdByClassObj, createdByLinkObj);
                            Q.reset_set(set_created_by);

                            int set_created_on = Q.get_link_from_by_category(0, createdOnClassObj, createdOnLinkObj);
                            Q.reset_set(set_created_on);

                            int set_modified_by = Q.get_link_from_by_category(0, modifiedByClassObj, modifiedByLinkObj);
                            Q.reset_set(set_modified_by);

                            int set_modified_on = Q.get_link_from_by_category(0, modifiedOnClassObj, modifiedOnLinkObj);
                            Q.reset_set(set_modified_on);

                            out.write("<error>");
                            out.write("<editor>");
                            out.write(Utilities.escapeXML(editors.get(m)));
                            out.write("</editor>");

                            out.write("<term>");
                            out.write(Utilities.escapeXML(dbGen.removePrefix(related_terms.get(n))));
                            out.write("</term>");

                            out.write("<created_by>");
                            if (Q.set_get_card(set_created_by) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</created_by>");

                            out.write("<created_on>");
                            if (Q.set_get_card(set_created_on) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</created_on>");

                            out.write("<modified_by>");
                            if (Q.set_get_card(set_modified_by) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</modified_by>");

                            out.write("<modified_on>");
                            if (Q.set_get_card(set_modified_on) > 0) {
                                out.write("ΥΠΑΡΧΕΙ");
                            } else {
                                out.write("-");
                            }
                            out.write("</modified_on>");

                            out.write("</error>");

                            Q.free_set(set_created_by);
                            Q.free_set(set_created_on);
                            Q.free_set(set_modified_by);
                            Q.free_set(set_modified_on);

                        }//writer for loop ending

                        Q.free_set(set_related_terms_labels);
                    }//editors traverse ending
                    Q.free_set(set_editors);
                }//Preview case ending
            }

            Q.free_set(set_unclassified_editors_labels);

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Unclassified_Editors: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Unclassified_Hierarchy_Terms(String selectedThesaurus, String mode, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {

        OutputStreamWriter out = null;
        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        QClass Q = new neo4j_sisapi.QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        try {

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Hierarchy_Terms ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Unclassified_Hierarchy_Terms ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            StringObject hierarchyTermObj = new StringObject();
            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), hierarchyTermObj);

            if (mode.compareTo("Preview") == 0) {

                String title = hierarchyTermObj.getValue().concat(" " + time);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + title + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");
            }
            Q.reset_name_scope();
            Q.set_current_node(hierarchyTermObj);
            int set_unclassified_terms = Q.get_instances(0);
            Q.reset_set(set_unclassified_terms);

            if (Q.set_get_card(set_unclassified_terms) > 0) {

                Vector<String> unclassified_terms = dbGen.get_Node_Names_Of_Set(set_unclassified_terms, true, Q, sis_session);
                unclassified_terms.trimToSize();

                for (int m = 0; m < unclassified_terms.size(); m++) {

                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(unclassified_terms.get(m)));
                    out.write("</name>");
                    out.write("</concept>");
                }
            }

            Q.free_set(set_unclassified_terms);

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Unclassified_Editors: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Collect_Garbage(String selectedThesaurus, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {

        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Collect_Garbage ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Collect_Garbage ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            StringObject hierarchyTermObj = new StringObject();
            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), hierarchyTermObj);

            if (mode.compareTo("Preview") == 0) {

                String title = hierarchyTermObj.getValue().concat(" " + time);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + title + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");
            }

            Q.reset_name_scope();
            Q.set_current_node(new StringObject(TMSAPIClass.INDIVIDUAL));
            Q.set_current_node(new StringObject(TMSAPIClass._GARBAGE_COLLECTED));

            int set_link_names = Q.get_all_instances(0);
            Q.reset_set(set_link_names);

            //EnglishWord THES1UsedForTerm etc.
            int set_to_categories = Q.get_to_value(set_link_names);
            Q.reset_set(set_to_categories);

            //All nodes to be examined for deletion
            int set_all_nodes = Q.get_all_instances(set_to_categories);
            Q.reset_set(set_all_nodes);

            //get all of these nodes that have links starting from them
            int set_from_links = Q.get_link_from(set_all_nodes);
            Q.reset_set(set_from_links);

            int set_nodes_having_from_links = Q.get_from_value(set_from_links);
            Q.reset_set(set_nodes_having_from_links);

            //get all of these nodes that have links pointing to them             
            int set_to_links = Q.get_link_to(set_all_nodes);
            Q.reset_set(set_to_links);

            int set_nodes_having_to_links = Q.get_to_value(set_to_links);
            Q.reset_set(set_nodes_having_to_links);

            //Exlude nodes with links from/to them from examined set of nodes. Remaining nodes should be deleted
            Q.set_difference(set_all_nodes, set_nodes_having_from_links);
            Q.reset_set(set_all_nodes);
            Q.set_difference(set_all_nodes, set_nodes_having_to_links);
            Q.reset_set(set_all_nodes);

            Q.free_set(set_link_names);
            Q.free_set(set_to_categories);
            Q.free_set(set_from_links);
            Q.free_set(set_nodes_having_from_links);
            Q.free_set(set_to_links);
            Q.free_set(set_nodes_having_to_links);

            if (mode.compareTo("Preview") == 0) {

                if (Q.set_get_card(set_all_nodes) > 0) {

                    Vector<String> nodes = dbGen.get_Node_Names_Of_Set(set_all_nodes, false, Q, sis_session);
                    Q.free_set(set_all_nodes);
                    for (int i = 0; i < nodes.size(); i++) {

                        out.write("<concept>");

                        out.write("<name>");
                        out.write(Utilities.escapeXML(nodes.get(i)));
                        out.write("</name>");
                        Q.reset_name_scope();
                        Q.set_current_node(new StringObject(nodes.get(i)));
                        int set_classes = Q.get_classes(0);
                        Q.reset_set(set_classes);
                        out.write("<category>");
                        Vector<String> classes = dbGen.get_Node_Names_Of_Set(set_classes, false, Q, sis_session);
                        for (int k = 0; k < classes.size(); k++) {

                            out.write(classes.get(k));
                            if (k < classes.size() - 1) {
                                out.write(", ");
                            }
                        }
                        Q.free_set(set_classes);
                        out.write("</category>");

                        out.write("</concept>");
                    }

                } else {
                    Q.free_set(set_all_nodes);
                }
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else if (mode.compareTo("Fix") == 0) {

                if (Q.set_get_card(set_all_nodes) > 0) {

                    Vector<String> nodes = dbGen.get_Node_Names_Of_Set(set_all_nodes, false, Q, sis_session);
                    Q.free_set(set_all_nodes);
                    for (int k = 0; k < nodes.size(); k++) {

                        Q.reset_name_scope();
                        Identifier I_delete = new Identifier(nodes.get(k));
                        if (Q.CHECK_Delete_Node(I_delete) == QClass.APIFail) {
                            fixed = false;
                        }

                    }
                } else {
                    Q.free_set(set_all_nodes);
                }
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Collect_Garbage: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Multiple_Usage(UserInfoClass SessionUserInfo, String mode, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Multiple_Usage ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Multiple_Usage ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            Q.reset_name_scope();

            StringObject hierarchyTermObj = new StringObject();
            StringObject usedForTermObj = new StringObject();
            StringObject alternativeTermObj = new StringObject();
            StringObject UFtranslationsLinkObj = new StringObject();
            //StringObject AltLinkObj = new StringObject();
            StringObject TranslationsClassObj = new StringObject();
            StringObject TranslationsLinkObj = new StringObject();

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, TranslationsClassObj, TranslationsLinkObj, Q, sis_session);
            dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), usedForTermObj);
            dbtr.getThesaurusClass_AlternativeTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), alternativeTermObj);
            dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), hierarchyTermObj);
            dbtr.getThesaurusCategory_uf_translations(SessionUserInfo.selectedThesaurus, UFtranslationsLinkObj);
            //dbtr.getThesaurusCategory_uk_alt(selectedThesaurus, ukAltLinkObj);

            //CHECK TO_EN/UK_UF/UK_ALT USAGES
            StringObject englishWordObj = new StringObject(ConstantParameters.EnglishWordClass);
            Q.reset_name_scope();
            Q.set_current_node(englishWordObj);

            int set_all_english_words = Q.get_all_instances(0);
            Q.reset_set(set_all_english_words);

            int set_translations_labels = Q.get_link_to_by_category(set_all_english_words, TranslationsClassObj, TranslationsLinkObj);
            Q.reset_set(set_translations_labels);
            //int card1= Q.set_get_card(set_translations_labels);

            int set_uk_ufs_labels = Q.get_link_to_by_category(set_all_english_words, hierarchyTermObj, UFtranslationsLinkObj);
            Q.reset_set(set_uk_ufs_labels);
            //int card2= Q.set_get_card(set_uk_ufs_labels);

            /*
             int set_uk_alts_labels = Q.get_link_to_by_category(set_all_english_words, hierarchyTermObj, ukAltLinkObj);
             Q.reset_set(set_uk_alts_labels);*/
            //int card3= Q.set_get_card(set_uk_alts_labels);
            int set_translations = Q.get_to_value(set_translations_labels);
            Q.reset_set(set_translations);
            //int card4= Q.set_get_card(set_translations);

            int set_uf_translations = Q.get_to_value(set_uk_ufs_labels);
            Q.reset_set(set_uf_translations);
            //int card5= Q.set_get_card(set_uk_ufs);

            /*
             int set_uk_alts = Q.get_to_value(set_uk_alts_labels);
             Q.reset_set(set_uk_alts);*/
            //int card6= Q.set_get_card(set_uk_alts);
            int set_translations_copy = Q.set_get_new();
            Q.set_copy(set_translations_copy, set_translations);
            Q.reset_set(set_translations_copy);

            Vector<String> TUvec = new Vector<String>();//translation and uk_uf
            Vector<String> TAvec = new Vector<String>();//translation and uk_alt
            Vector<String> UAvec = new Vector<String>();//uk_uf and uk_alt
            Vector<String> TUAvec = new Vector<String>();//translation and uk_uf and uk_alt

            //keep in set_translations all terms that are both translations and ufs
            Q.set_intersect(set_translations, set_uf_translations);
            Q.reset_set(set_translations);
            if (Q.set_get_card(set_translations) > 0) {
                TUvec.addAll(dbGen.get_Node_Names_Of_Set(set_translations, false, Q, sis_session));
                TUvec.trimToSize();
            }

            //keep in set_translations all terms that are both translations and alts
            Q.set_copy(set_translations, set_translations_copy);
            Q.reset_set(set_translations);

            /*
             Q.set_intersect(set_translations, set_uk_alts);
             Q.reset_set(set_translations);
             if (Q.set_get_card(set_translations) > 0) {
             TAvec.addAll(dbGen.get_Node_Names_Of_Set(set_translations, false, Q, sis_session));
             TAvec.trimToSize();
             }
           
             */
            //prepare set_translations that will now keep all terms that are translations and uk_alts and uk_ufs 
            Q.set_intersect(set_translations, set_uf_translations);
            Q.reset_set(set_translations);

            /*
             //keep in set_uk_ufs all terms that are both uk_ufs and uk_alts
             Q.set_intersect(set_uk_ufs, set_uk_alts);
             Q.reset_set(set_uk_ufs);
             *
             */
            if (Q.set_get_card(set_uf_translations) > 0) {
                UAvec.addAll(dbGen.get_Node_Names_Of_Set(set_uf_translations, false, Q, sis_session));
                UAvec.trimToSize();
            }

            if (Q.set_get_card(set_translations) > 0) {
                TUAvec.addAll(dbGen.get_Node_Names_Of_Set(set_translations, false, Q, sis_session));
                TUAvec.trimToSize();
            }

            Q.free_set(set_all_english_words);
            Q.free_set(set_translations_labels);
            Q.free_set(set_uk_ufs_labels);
            //Q.free_set(set_uk_alts_labels);
            Q.free_set(set_translations);
            Q.free_set(set_uf_translations);
            //Q.free_set(set_uk_alts);
            Q.free_set(set_translations_copy);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            //CHECK TERM/UF/ALT USAGES
            Q.reset_name_scope();
            int set_descriptors = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_descriptors);

            Q.reset_name_scope();
            Q.set_current_node(usedForTermObj);
            int set_ufs = Q.get_all_instances(0);
            Q.reset_set(set_ufs);

            Q.reset_name_scope();
            Q.set_current_node(alternativeTermObj);
            int set_alts = Q.get_all_instances(0);
            Q.reset_set(set_alts);

            int set_descriptors_copy = Q.set_get_new();
            Q.set_copy(set_descriptors_copy, set_descriptors);
            Q.reset_set(set_descriptors_copy);

            Vector<String> DXvec = new Vector<String>();//descriptor and uf
            Vector<String> DAvec = new Vector<String>();//descriptor and alt
            Vector<String> XAvec = new Vector<String>();//uf and alt
            Vector<String> DXAvec = new Vector<String>();//descriptor and uf and alt

            Q.set_intersect(set_descriptors, set_ufs);
            Q.reset_set(set_descriptors);
            if (Q.set_get_card(set_descriptors) > 0) {
                DXvec.addAll(dbGen.get_Node_Names_Of_Set(set_descriptors, false, Q, sis_session));
                DXvec.trimToSize();
            }

            Q.set_intersect(set_descriptors, set_alts);
            Q.reset_set(set_descriptors);
            if (Q.set_get_card(set_descriptors) > 0) {
                DXAvec.addAll(dbGen.get_Node_Names_Of_Set(set_descriptors, false, Q, sis_session));
                DXAvec.trimToSize();
            }

            Q.set_intersect(set_ufs, set_alts);
            Q.reset_set(set_ufs);
            if (Q.set_get_card(set_ufs) > 0) {
                XAvec.addAll(dbGen.get_Node_Names_Of_Set(set_ufs, false, Q, sis_session));
                XAvec.trimToSize();
            }

            Q.set_copy(set_descriptors, set_descriptors_copy);
            Q.reset_set(set_descriptors);

            Q.set_intersect(set_descriptors, set_alts);
            Q.reset_set(set_descriptors);
            if (Q.set_get_card(set_descriptors) > 0) {
                DAvec.addAll(dbGen.get_Node_Names_Of_Set(set_descriptors, false, Q, sis_session));
                DAvec.trimToSize();
            }

            Q.free_set(set_alts);
            Q.free_set(set_ufs);
            Q.free_set(set_descriptors);
            Q.free_set(set_descriptors_copy);

            if (mode.compareTo("Preview") == 0) {

                for (int i = 0; i < DXvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(DXvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("DX");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
                for (int i = 0; i < DAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(DAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("DA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
                for (int i = 0; i < XAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(XAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("XA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }

                for (int i = 0; i < DXAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(DXAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("DXA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }

                for (int i = 0; i < TUvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(TUvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("TU");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
                for (int i = 0; i < TAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(TAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("TA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
                for (int i = 0; i < UAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(UAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("UA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
                for (int i = 0; i < TUAvec.size(); i++) {
                    out.write("<concept>");
                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(TUAvec.get(i))));
                    out.write("</name>");
                    out.write("<usedas>");
                    out.write("TUA");
                    out.write("</usedas>");
                    out.write("</concept>");
                }
            } else if (mode.compareTo("Fix") == 0) {
                //Follow the rule that Descriptor or translations will be kept
                //otherwise used For Terms will be kept
                //alternative Terms will be deleted in every case
            }

            Q.reset_name_scope();

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Multiple_Usage: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void Multiple_Editors_And_Dates(String selectedThesaurus, String mode, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);

                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Multiple_Editors_And_Dates ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ Multiple_Editors_And_Dates ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            //PERFORM CHECKS AND XML WRITING IF PREVIEW
            Q.reset_name_scope();

            StringObject newDescrObj = new StringObject();
            StringObject hierarchyTermObj = new StringObject();
            StringObject editorObj = new StringObject();
            StringObject createdByObj = new StringObject();
            StringObject modifiedByObj = new StringObject();
            StringObject createdObj = new StringObject();
            StringObject modifiedObj = new StringObject();

            dbtr.getThesaurusClass_NewDescriptor(selectedThesaurus, Q, sis_session.getValue(), newDescrObj);
            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), hierarchyTermObj);
            dbtr.getThesaurusCategory_editor(selectedThesaurus, Q, sis_session.getValue(), editorObj);
            dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, sis_session.getValue(), createdByObj);
            dbtr.getThesaurusCategory_modified_by(selectedThesaurus, Q, sis_session.getValue(), modifiedByObj);
            dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), createdObj);
            dbtr.getThesaurusCategory_modified(selectedThesaurus, Q, sis_session.getValue(), modifiedObj);

            Q.reset_name_scope();
            Q.set_current_node(hierarchyTermObj);
            int set_descriptors = Q.get_all_instances(0);

            Q.reset_name_scope();
            Q.set_current_node(newDescrObj);
            int set_new_descriptors = Q.get_all_instances(0);

            Q.reset_set(set_new_descriptors);
            Q.reset_set(set_descriptors);
            Q.set_union(set_descriptors, set_new_descriptors);
            Q.reset_set(set_descriptors);

            Vector<String> allDescr = dbGen.get_Node_Names_Of_Set(set_descriptors, false, Q, sis_session);

            Q.free_set(set_new_descriptors);
            Q.free_set(set_descriptors);

            //String errorString = allDescr.get(2260);
            //allDescr.add(0, errorString);
            for (int i = 0; i < allDescr.size(); i++) {
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"i= " + i + " of " +allDescr.size()+" terms = " + allDescr.get(i));

                Q.reset_name_scope();

                Q.set_current_node(new StringObject(allDescr.get(i)));

                int set_from_labels = Q.get_link_from(0);
                Q.reset_set(set_from_labels);

                Vector<String> creators = new Vector<String>();
                Vector<String> modificators = new Vector<String>();
                Vector<String> created_on = new Vector<String>();
                Vector<String> modified_on = new Vector<String>();

                ///StringObject fromcls = new StringObject();
                //StringObject label = new StringObject();
                //StringObject categ = new StringObject();
                //StringObject cls = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();
                //IntegerObject clsID = new IntegerObject();
                //IntegerObject linkID = new IntegerObject();
                //IntegerObject categID = new IntegerObject();
                //CMValue cmv = new CMValue();
                Q.reset_name_scope();
                Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
                if (Q.bulk_return_full_link(set_from_labels, retFLVals) != QClass.APIFail) {
                    for (Return_Full_Link_Row row : retFLVals) {

                        String categ = row.get_v3_categ();
                        CMValue cmv = row.get_v5_cmv();
                        if (categ.compareTo(editorObj.getValue()) == 0) {

                            if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                                creators.add(cmv.getString());
                                modificators.add(cmv.getString());
                            }
                        } else if (categ.compareTo(createdByObj.getValue()) == 0) {
                            if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                                creators.add(cmv.getString());
                            }
                        } else if (categ.compareTo(modifiedByObj.getValue()) == 0) {
                            if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                                modificators.add(cmv.getString());
                            }
                        } else if (categ.compareTo(createdObj.getValue()) == 0) {
                            if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                                created_on.add(cmv.getString());
                            }
                        } else if (categ.compareTo(modifiedObj.getValue()) == 0) {
                            if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                                modified_on.add(cmv.getString());
                            }
                        }
                    }
                }

                /*
                 //while (Q.retur_full_link_id(set_from_labels, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                 //while (Q.retur_link(set_editor_labels, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                 if (categ.getValue().compareTo(editorObj.getValue()) == 0) {

                 if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                 creators.add(cmv.getString());
                 modificators.add(cmv.getString());
                 }
                 } else if (categ.getValue().compareTo(createdByObj.getValue()) == 0) {
                 if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                 creators.add(cmv.getString());
                 }
                 } else if (categ.getValue().compareTo(modifiedByObj.getValue()) == 0) {
                 if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                 modificators.add(cmv.getString());
                 }
                 } else if (categ.getValue().compareTo(createdObj.getValue()) == 0) {
                 if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                 created_on.add(cmv.getString());
                 }
                 } else if (categ.getValue().compareTo(modifiedObj.getValue()) == 0) {
                 if (cmv.getType() == CMValue.TYPE_NODE || cmv.getType() == CMValue.TYPE_STRING) {
                 modified_on.add(cmv.getString());
                 }
                 }

                 }
                 */
                Q.free_set(set_from_labels);

                if (creators.size() > 1 || modificators.size() > 1 || created_on.size() > 1 || modified_on.size() > 1) {

                    out.write("<concept>");

                    out.write("<name>");
                    out.write(Utilities.escapeXML(dbGen.removePrefix(allDescr.get(i))));
                    out.write("</name>");

                    out.write("<creator>");

                    if (creators.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < creators.size(); k++) {
                            out.write(Utilities.escapeXML(dbGen.removePrefix(creators.get(k))));

                            if (k < creators.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }

                    out.write("</creator>");

                    out.write("<modificator>");

                    if (modificators.size() == 0) {
                        out.write("-");
                    } else {
                        for (int k = 0; k < modificators.size(); k++) {
                            out.write(Utilities.escapeXML(dbGen.removePrefix(modificators.get(k))));
                            if (k < modificators.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }

                    out.write("</modificator>");

                    out.write("<createdOn>");
                    if (created_on.size() == 0) {
                        out.write("-");
                    } else {

                        for (int k = 0; k < created_on.size(); k++) {
                            out.write(Utilities.escapeXML(created_on.get(k)));
                            if (k < created_on.size() - 1) {
                                out.write(", ");
                            }
                        }

                    }

                    out.write("</createdOn>");

                    out.write("<modifiedOn>");

                    if (modified_on.size() == 0) {
                        out.write("-");
                    } else {

                        for (int k = 0; k < modified_on.size(); k++) {
                            out.write(Utilities.escapeXML(modified_on.get(k)));
                            if (k < modified_on.size() - 1) {
                                out.write(", ");
                            }
                        }
                    }

                    out.write("</modifiedOn>");
                    out.write("</concept>");
                }

            }

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Multiple_Editors_And_Dates: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    void TransformDates(String selectedThesaurus, String mode, Boolean fixed, String time, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, String pathToSaveScriptingAndLocale) {
        try {

            OutputStreamWriter out = null;
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            if (mode.compareTo("Preview") == 0) {

                String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                out.write(ConstantParameters.xmlHeader);
                out.write("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                out.write("<title>" + time + "</title>");
                out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, true) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ TransformDates ");
                    return;
                }

                Q.TEST_begin_query();

            } else {

                if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, false) == QClass.APIFail) {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ TransformDates ");
                    return;
                }

                Q.TEST_begin_transaction();
            }

            //handle scope note
            Q.reset_name_scope();
            StringObject hierarchyTermObj = new StringObject();
            StringObject createdObj = new StringObject();
            StringObject modifiedObj = new StringObject();

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), hierarchyTermObj);
            dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), createdObj);
            dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), modifiedObj);

            Q.reset_name_scope();
            Q.set_current_node(hierarchyTermObj);
            Q.set_current_node(createdObj);
            int set_created_labels = Q.get_all_instances(0);
            Q.reset_set(set_created_labels);

            int set_dates = Q.get_to_value(set_created_labels);
            Q.reset_set(set_dates);

            Q.reset_name_scope();
            Q.set_current_node(hierarchyTermObj);
            Q.set_current_node(modifiedObj);
            int set_modified_labels = Q.get_all_instances(0);
            Q.reset_set(set_modified_labels);

            int set_dates_modified = Q.get_to_value(set_modified_labels);
            Q.reset_set(set_dates_modified);

            Q.set_union(set_dates, set_dates_modified);
            Q.reset_set(set_dates);

            Q.free_set(set_dates_modified);
            Q.free_set(set_modified_labels);
            Q.free_set(set_created_labels);

            Vector<String> dates = dbGen.get_Node_Names_Of_Set(set_dates, false, Q, sis_session);
            Q.free_set(set_dates);

            for (int i = 0; i < dates.size(); i++) {

                String newName = getNumericDate(dates.get(i));
                if (newName.compareTo(dates.get(i)) == 0) {
                    continue;
                }

                if (mode.compareTo("Fix") == 0) {

                    Identifier I_old = new Identifier(dates.get(i));
                    Identifier I_new = new Identifier(newName);
                    if (Q.CHECK_Rename_Node(I_old, I_new) == QClass.APIFail) {
                        Q.reset_name_scope();
                        if (Q.set_current_node(new StringObject(dates.get(i))) != QClass.APIFail) {
                            fixed = false;
                        }
                    }

                    /*Q.reset_name_scope();
                     if(Q.set_current_node(new StringObject(u.dates.get(i)))==Q.APIFail){
                     Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Could not set current Node Date : "+dates.get(i));
                     }*/
                } else {

                    out.write("<concept>");

                    out.write("<oldname>");
                    out.write(Utilities.escapeXML(dates.get(i)));
                    out.write("</oldname>");

                    out.write("<newname>");
                    out.write(Utilities.escapeXML(newName));
                    out.write("</newname>");

                    out.write("</concept>");
                }

            }

            if (mode.compareTo("Preview") == 0) {
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                out.write("</page>");
                out.close();
            } else {
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

        } catch (java.io.IOException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TransformDates: Error in opening/writing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    String getNumericDate(String oldFormat) {
        String newFormat = oldFormat;

        oldFormat = oldFormat.replaceAll(" +", " ").trim();
        if (oldFormat.matches("[1-2][0-9]{3} [a-zA-Zα-ωΑ-Ωάέήίόύώ]+ (([0][1-9])|[12][0-9]|([3][0-1]))")) {
            String[] parts = oldFormat.split(" ");
            newFormat = parts[0];
            newFormat += "-";
            newFormat += GetMonthInt(parts[1]);
            newFormat += "-";
            newFormat += parts[2];
        } else if (oldFormat.matches("[1-2][0-9]{3}-[0-9]+-[0-9]+")) {
            String[] parts = oldFormat.split("-");
            newFormat = parts[0];
            newFormat += "-";
            if (parts[1].length() == 1) {
                newFormat += "0";
            }

            newFormat += parts[1];
            newFormat += "-";
            if (parts[2].length() == 1) {
                newFormat += "0";
            }

            newFormat += parts[2];

        }

        return newFormat;
    }

    String GetMonthInt(String monthStr) {
        String monthInt = "00";

        String Month = monthStr.toLowerCase();
        if (Month.startsWith("jan") || Month.startsWith("ιαν")) {
            return "01";
        }
        if (Month.startsWith("feb") || Month.startsWith("φεβ")) {
            return "02";
        }
        if (Month.startsWith("mar") || Month.startsWith("μαρ") || Month.startsWith("μάρ")) {
            return "03";
        }
        if (Month.startsWith("apr") || Month.startsWith("απρ")) {
            return "04";
        }
        if (Month.startsWith("may") || Month.startsWith("μάι") || Month.startsWith("μαι")) {
            return "05";
        }
        if (Month.startsWith("jun") || Month.startsWith("ιούν") || Month.startsWith("ιουν")) {
            return "06";
        }
        if (Month.startsWith("jul") || Month.startsWith("ιούλ") || Month.startsWith("ιουλ")) {
            return "07";
        }
        if (Month.startsWith("aug") || Month.startsWith("αύγ") || Month.startsWith("αυγ")) {
            return "08";
        }
        if (Month.startsWith("sep") || Month.startsWith("σεπ")) {
            return "09";
        }
        if (Month.startsWith("oct") || Month.startsWith("οκτ")) {
            return "10";
        }
        if (Month.startsWith("nov") || Month.startsWith("νοέ") || Month.startsWith("νοε")) {
            return "11";
        }
        if (Month.startsWith("dec") || Month.startsWith("δεκ")) {
            return "12";
        }
        return monthInt;

    }
    /*
     //not used replaced by a more generic implementation
     void RepairPrefixes_Preview_or_Fix(QClass Q, IntegerObject sis_session, Locale targetLocale, String mode, Boolean fixed, Vector<String> allNamesWithPrefix, String prefix, String suffix, String kind, OutputStreamWriter out) {

     //tools 
     Utilities u = new Utilities();
        

     allNamesWithPrefix.trimToSize();
     Collections.sort(allNamesWithPrefix, new StringLocaleComparator(targetLocale));


     for (int i = 0; i < allNamesWithPrefix.size(); i++) {

     Q.reset_name_scope();

     String initial = allNamesWithPrefix.get(i);
     String comparison = allNamesWithPrefix.get(i);


     Identifier I_old = new Identifier(initial);
     Identifier I_new = null;
     boolean exists = false;
     StringObject newNameStrObj = new StringObject();

     //fix Prefix
     if (prefix != null && initial.startsWith(prefix)) {

     continue;

     } else {

     comparison = prefix + comparison;
     newNameStrObj.setValue(comparison);
     I_new = new Identifier(newNameStrObj.getValue());
     Q.reset_name_scope();

     if (Q.set_current_node(newNameStrObj) != QClass.APIFail) {
     exists = true;
     }
     }

     if (initial.compareTo(comparison) == 0) {
     continue;
     }

     if (mode.compareTo("Fix") == 0) {

     //if rename fails then new node name already exists.
     //Default fix behavior will be to copy old node name's links to new node name and delete old
     Q.reset_name_scope();
     if (Q.Rename_Node(I_old, I_new) == QClass.APIFail) {
     Q.reset_name_scope();
     if (Q.set_current_node(new StringObject(I_old.getLogicalName())) != QClass.APIFail) {
     fixed = false;
     }
     }

     } else if (mode.compareTo("Preview") == 0) {
     try {

     out.write("<concept>");

     out.write("<kind>");
     out.write(kind);
     out.write("</kind>");
     out.write("<oldname>");
     out.write(Utilities.escapeXML("'" + initial.replaceAll(" ", "&#160;") + "'"));
     out.write("</oldname>");
     out.write("<newname>");
     out.write(Utilities.escapeXML("'" + comparison + "'"));
     out.write("</newname>");

     out.write("<newnameexists>");

     if (exists) {
     out.write("NAI");
     } else {
     out.write(" - ");
     }
     out.write("</newnameexists>");

     out.write("</concept>");
     out.flush();

     } catch (java.io.IOException exc) {
     Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"RepairPrefixes_Preview_or_Fix Error in writing Errors in Names file : " + exc.getMessage());
     }
     }
     }
     }
     */
    /* TEMPLATE FOR MORE CHECKS
     void Terms_Without_BTs(String time) {
     try {
    
     OutputStreamWriter out = null;
    
     if (mode.compareTo("Preview") == 0) {
    
     String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath +"/"+ Save_Results_file_name + ".xml";
    
     OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
     OutputStream bout = new BufferedOutputStream(fout);
     out = new OutputStreamWriter(bout, "UTF-8");
     out.write(ConstantParameters.xmlHeader);
     out.write(XSL);
     out.write("<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">");
     out.write("<title>" + time + "</title>");
     out.write("<save>../../images/save.gif</save><print>../../images/print.gif</print>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>" );
    
     Q.begin_query();
    
     } else {
     Q.begin_transaction();
     }
    
     //PERFORM CHECKS AND XML WRITING IF PREVIEW
    
     if (mode.compareTo("Preview") == 0) {
     Q.end_query();
     out.write("</page>");
     out.close();
     }
     else {
     Q.end_transaction();
     }
    
     } catch (java.io.IOException exc) {
     Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Terms_Without_BTs: Error in opening/writing file: " + exc.getMessage());
     }    
     }*/
}
