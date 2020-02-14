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
 * WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
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
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.SortItem;
import Utils.Utilities;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.ServletContext;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.StringObject;
import neo4j_sisapi.TMSAPIClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Elias
 */
public class DBImportDataTest {
    
    String basePath ="C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\_THEMAS_ProjectFolder\\Development\\THEMAS\\src\\main\\webapp"; 
    
    public DBImportDataTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of thesaurusImportActions method, of class DBImportData.
    
    @Test
    public void testThesaurusImportActions() throws Exception {
        System.out.println("thesaurusImportActions");
        Parameters.initParams(basePath);
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
        
        
        String xmlFilePath = "C:\\Users\\Elias\\BackupFiles\\Desktop\\External Uris Extension\\LoadTest\\Export_Thesaurus_PARTHENOS_2018-07-06_12-08-02-208.xml";
        String xmlSchemaType = ConstantParameters.xmlschematype_THEMAS;
        boolean initializeDb = true;
        String importThesaurusName = "PARTHENOS";
        
        String backUpDescription = "backup_before_import_data_to_thes_" + importThesaurusName;
        Locale targetLocale = new Locale("el", "GR");
        StringObject resultObj = new StringObject("");
        StringObject DBbackupFileNameCreated = new StringObject("");
        
        
        String pathToErrorsXML = Utilities.getXml_For_ConsistencyChecks();
        
        
        UserInfoClass refSessionUserInfo = new UserInfoClass();
        refSessionUserInfo.name="test";
        refSessionUserInfo.password="test";
        refSessionUserInfo.thesaurusGroups.add(ConstantParameters.Group_Administrator);
        refSessionUserInfo.UILang = Parameters.UILang;
        
        Utilities u = new Utilities();
        String logFileNamePath = basePath+"/" + ConstantParameters.LogFilesFolderName;
            
        OutputStreamWriter logFileWriter = null;
        long startTime = Utilities.startTimer();
        String time = Utilities.GetNow();
        String Filename = "Import_Thesaurus_" + importThesaurusName + "_" + time;
        logFileNamePath += "/" + Filename + ".xml";

        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);
            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"

            //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../" + webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
            logFileWriter.append("<page language=\"" + refSessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
            logFileWriter.append("<title>" + u.translateFromMessagesXML("root/ImportData/ReportTitle", new String[]{importThesaurusName, time}, refSessionUserInfo.UILang) + "</title>\r\n"
                    + "<pathToSaveScriptingAndLocale>" + Utilities.getXml_For_SaveAll_Locale_And_Scripting() + "</pathToSaveScriptingAndLocale>\r\n");
            //logFileWriter.append("<!--"+time + " LogFile for data import in thesaurus: " + importThesaurusName +".-->\r\n");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile for data import in thesaurus: " + importThesaurusName + ".");

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        
        
        
        
        try{
        
        
        
        DBImportData instance = new DBImportData();
        boolean expResult = true;
        boolean result = instance.thesaurusImportActions(refSessionUserInfo, common_utils, initializeDb, config, targetLocale, pathToErrorsXML, xmlFilePath, xmlSchemaType, importThesaurusName, backUpDescription, DBbackupFileNameCreated, resultObj, logFileWriter);
        System.out.println(resultObj.getValue());
        assertEquals(expResult, result);
        }
        catch(Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    **/
    
    /**
     * Test of bulkImportActions method, of class DBImportData.
     
    @Test
    public void testBulkImportActions() throws Exception {
        System.out.println("bulkImportActions");
        SessionWrapperClass sessionInstance = null;
        ServletContext context = null;
        CommonUtilsDBadmin common_utils = null;
        ConfigDBadmin config = null;
        Locale targetLocale = null;
        String pathToErrorsXML = "";
        String xmlFilePath = "";
        String importThesaurusName = "";
        String importHierarchyName = "";
        String backUpDescription = "";
        StringObject DBbackupFileNameCreated = null;
        StringObject resultObj = null;
        OutputStreamWriter logFileWriter = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.bulkImportActions(sessionInstance, context, common_utils, config, targetLocale, pathToErrorsXML, xmlFilePath, importThesaurusName, importHierarchyName, backUpDescription, DBbackupFileNameCreated, resultObj, logFileWriter);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of importTermsUnderHierarchy method, of class DBImportData.
     
    @Test
    public void testImportTermsUnderHierarchy() throws Exception {
        System.out.println("importTermsUnderHierarchy");
        SessionWrapperClass sessionInstance = null;
        String targetHierarchy = "";
        String xmlFilePath = "";
        String pathToErrorsXML = "";
        OutputStreamWriter logFileWriter = null;
        StringObject resultObj = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.importTermsUnderHierarchy(sessionInstance, targetHierarchy, xmlFilePath, pathToErrorsXML, logFileWriter, resultObj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of writeThesaurusDataFromSortItems method, of class DBImportData.
     
    @Test
    public void testWriteThesaurusDataFromSortItems() {
        System.out.println("writeThesaurusDataFromSortItems");
        UserInfoClass refSessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        ArrayList<SortItem> xmlFacets = null;
        ArrayList<String> guideTerms = null;
        HashMap<String, String> XMLsources = null;
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        HashMap<String, NodeInfoStringContainer> termsInfo = null;
        ArrayList<String> userSelectedTranslationWords = null;
        ArrayList<String> userSelectedTranslationIdentifiers = null;
        HashMap<String, String> userSelections = null;
        ArrayList<SortItem> topTerms = null;
        HashMap<String, ArrayList<String>> descriptorRts = null;
        HashMap<String, ArrayList<SortItem>> descriptorUfs = null;
        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes = null;
        String importThesaurusName = "";
        String pathToErrorsXML = "";
        Locale targetLocale = null;
        StringObject resultObj = null;
        OutputStreamWriter logFileWriter = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.writeThesaurusDataFromSortItems(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, xmlFacets, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections, topTerms, descriptorRts, descriptorUfs, allLevelsOfImportThes, importThesaurusName, pathToErrorsXML, targetLocale, resultObj, logFileWriter);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of writeThesaurusData method, of class DBImportData.
     
    @Test
    public void testWriteThesaurusData() {
        System.out.println("writeThesaurusData");
        UserInfoClass refSessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        ArrayList<SortItem> xmlFacetsInSortItems = null;
        ArrayList<String> guideTerms = null;
        HashMap<String, String> XMLsources = null;
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        HashMap<String, NodeInfoStringContainer> termsInfo = null;
        ArrayList<String> userSelectedTranslationWords = null;
        ArrayList<String> userSelectedTranslationIdentifiers = null;
        HashMap<String, String> userSelections = null;
        ArrayList<String> topTerms = null;
        HashMap<String, ArrayList<String>> descriptorRts = null;
        HashMap<String, ArrayList<String>> descriptorUfs = null;
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = null;
        String importThesaurusName = "";
        String pathToErrorsXML = "";
        Locale targetLocale = null;
        StringObject resultObj = null;
        OutputStreamWriter logFileWriter = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.writeThesaurusData(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, xmlFacetsInSortItems, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections, topTerms, descriptorRts, descriptorUfs, allLevelsOfImportThes, importThesaurusName, pathToErrorsXML, targetLocale, resultObj, logFileWriter);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */

    /**
     * Test of thesaurusMergeActions method, of class DBImportData.
     
    @Test
    public void testThesaurusMergeActions() {
        System.out.println("thesaurusMergeActions");
        UserInfoClass refSessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        ConfigDBadmin config = null;
        String pathToErrorsXML = "";
        String thesaurusName1 = "";
        String thesaurusName2 = "";
        String targetThesaurusName = "";
        Locale targetLocale = null;
        StringObject resultObj = null;
        StringObject CopyThesaurusResultMessage = null;
        StringBuffer xml = null;
        String logFileNamePath = "";
        String pathToSaveScriptingAndLocale = "";
        long startTime = 0L;
        PrintWriter out = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.thesaurusMergeActions(refSessionUserInfo, common_utils, config, pathToErrorsXML, thesaurusName1, thesaurusName2, targetThesaurusName, targetLocale, resultObj, CopyThesaurusResultMessage, xml, logFileNamePath, pathToSaveScriptingAndLocale, startTime, out);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of thesaurusCopyActions method, of class DBImportData.
     
    @Test
    public void testThesaurusCopyActions() {
        System.out.println("thesaurusCopyActions");
        UserInfoClass refSessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        ConfigDBadmin config = null;
        String pathToErrorsXML = "";
        String sourceThesaurusName = "";
        String targetThesaurusName = "";
        Locale targetLocale = null;
        StringObject resultObj = null;
        StringObject CopyThesaurusResultMessage = null;
        StringBuffer xml = null;
        String logFileNamePath = "";
        String pathToSaveScriptingAndLocale = "";
        long startTime = 0L;
        PrintWriter out = null;
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.thesaurusCopyActions(refSessionUserInfo, common_utils, config, pathToErrorsXML, sourceThesaurusName, targetThesaurusName, targetLocale, resultObj, CopyThesaurusResultMessage, xml, logFileNamePath, pathToSaveScriptingAndLocale, startTime, out);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getXMLMiddleForCopyThesaurus method, of class DBImportData.
     
    @Test
    public void testGetXMLMiddleForCopyThesaurus() {
        System.out.println("getXMLMiddleForCopyThesaurus");
        CommonUtilsDBadmin common_utils = null;
        ArrayList thesaurusVector = null;
        StringObject CopyThesaurusResultMessage = null;
        Boolean CopyThesaurusSucceded = null;
        DBImportData instance = new DBImportData();
        String expResult = "";
        String result = instance.getXMLMiddleForCopyThesaurus(common_utils, thesaurusVector, CopyThesaurusResultMessage, CopyThesaurusSucceded);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getXMLMiddleForMergeThesaurus method, of class DBImportData.
     
    @Test
    public void testGetXMLMiddleForMergeThesaurus() {
        System.out.println("getXMLMiddleForMergeThesaurus");
        CommonUtilsDBadmin common_utils = null;
        ArrayList thesaurusVector = null;
        String MergeThesaurusMessage = "";
        DBImportData instance = new DBImportData();
        String expResult = "";
        String result = instance.getXMLMiddleForMergeThesaurus(common_utils, thesaurusVector, MergeThesaurusMessage);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of commitMergeActions method, of class DBImportData.
     
    @Test
    public void testCommitMergeActions() {
        System.out.println("commitMergeActions");
        UserInfoClass SessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        Locale targetLocale = null;
        String mergedThesaurusName = "";
        PrintWriter out = null;
        String reportFile = "";
        DBImportData instance = new DBImportData();
        instance.commitMergeActions(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, targetLocale, mergedThesaurusName, out, reportFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of commitCopyActions method, of class DBImportData.
     
    @Test
    public void testCommitCopyActions() {
        System.out.println("commitCopyActions");
        UserInfoClass SessionUserInfo = null;
        CommonUtilsDBadmin common_utils = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        Locale targetLocale = null;
        String mergedThesaurusName = "";
        PrintWriter out = null;
        String reportFile = "";
        DBImportData instance = new DBImportData();
        instance.commitCopyActions(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, targetLocale, mergedThesaurusName, out, reportFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of abortMergeActions method, of class DBImportData.
     
    @Test
    public void testAbortMergeActions() {
        System.out.println("abortMergeActions");
        UserInfoClass SessionUserInfo = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        Locale targetLocale = null;
        CommonUtilsDBadmin common_utils = null;
        String initiallySelectedThesaurus = "";
        String mergedThesaurusName = "";
        StringObject DBbackupFileNameCreated = null;
        StringObject resultObj = null;
        PrintWriter out = null;
        DBImportData instance = new DBImportData();
        instance.abortMergeActions(SessionUserInfo, Q, TA, sis_session, tms_session, targetLocale, common_utils, initiallySelectedThesaurus, mergedThesaurusName, DBbackupFileNameCreated, resultObj, out);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of abortCopyActions method, of class DBImportData.
     
    @Test
    public void testAbortCopyActions() {
        System.out.println("abortCopyActions");
        UserInfoClass SessionUserInfo = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        Locale targetLocale = null;
        CommonUtilsDBadmin common_utils = null;
        String initiallySelectedThesaurus = "";
        String mergedThesaurusName = "";
        StringObject DBbackupFileNameCreated = null;
        StringObject resultObj = null;
        PrintWriter out = null;
        DBImportData instance = new DBImportData();
        instance.abortCopyActions(SessionUserInfo, Q, TA, sis_session, tms_session, targetLocale, common_utils, initiallySelectedThesaurus, mergedThesaurusName, DBbackupFileNameCreated, resultObj, out);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    

    /**
     * Test of CreateSources method, of class DBImportData.
     
    @Test
    public void testCreateSources() {
        System.out.println("CreateSources");
        String selectedThesaurus = "";
        CommonUtilsDBadmin common_utils = null;
        String importThesaurusName = "";
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        HashMap<String, String> XMLsources = null;
        StringObject resultObj = null;
        OutputStreamWriter logFileWriter = null;
        String uiLang = "";
        DBImportData instance = new DBImportData();
        boolean expResult = false;
        boolean result = instance.CreateSources(selectedThesaurus, common_utils, importThesaurusName, Q, TA, sis_session, tms_session, XMLsources, resultObj, logFileWriter, uiLang);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of processXMLTerms method, of class DBImportData.
     
    @Test
    public void testProcessXMLTerms() {
        System.out.println("processXMLTerms");
        HashMap<String, NodeInfoStringContainer> termsInfo = null;
        HashMap<String, ArrayList<String>> descriptorRts = null;
        HashMap<String, ArrayList<String>> descriptorUfs = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        ArrayList<String> topTerms = null;
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = null;
        DBImportData instance = new DBImportData();
        instance.processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */

    /**
     * Test of processXMLTermsInSortItems method, of class DBImportData.
     
    @Test
    public void testProcessXMLTermsInSortItems() {
        System.out.println("processXMLTermsInSortItems");
        HashMap<String, NodeInfoStringContainer> termsInfo = null;
        HashMap<String, ArrayList<String>> descriptorRts = null;
        HashMap<String, ArrayList<SortItem>> descriptorUfs = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        ArrayList<SortItem> topTerms = null;
        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes = null;
        DBImportData instance = new DBImportData();
        instance.processXMLTermsInSortItems(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of findOutTermLevelsInSortItems method, of class DBImportData.
     
    @Test
    public void testFindOutTermLevelsInSortItems() {
        System.out.println("findOutTermLevelsInSortItems");
        HashMap<SortItem, ArrayList<SortItem>> descriptorNts = null;
        ArrayList<SortItem> allTermsHavingBTs = null;
        ArrayList<SortItem> allTermsHavingNTs = null;
        ArrayList<SortItem> allTermsWithoutBTsorNTs = null;
        ArrayList<SortItem> topTerms = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes = null;
        DBImportData instance = new DBImportData();
        instance.findOutTermLevelsInSortItems(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets, allLevelsOfImportThes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */

    /**
     * Test of findOutTopTermsAndOrphansUsingSortItems method, of class DBImportData.
     
    @Test
    public void testFindOutTopTermsAndOrphansUsingSortItems() {
        System.out.println("findOutTopTermsAndOrphansUsingSortItems");
        HashMap<SortItem, ArrayList<SortItem>> descriptorNts = null;
        ArrayList<SortItem> allTermsHavingBTs = null;
        ArrayList<SortItem> allTermsHavingNTs = null;
        ArrayList<SortItem> allTermsWithoutBTsorNTs = null;
        ArrayList<SortItem> topTerms = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        DBImportData instance = new DBImportData();
        instance.findOutTopTermsAndOrphansUsingSortItems(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of findOutTermLevels method, of class DBImportData.
     
    @Test
    public void testFindOutTermLevels() {
        System.out.println("findOutTermLevels");
        HashMap<String, ArrayList<String>> descriptorNts = null;
        ArrayList<String> allTermsHavingBTs = null;
        ArrayList<String> allTermsHavingNTs = null;
        ArrayList<String> allTermsWithoutBTsorNTs = null;
        ArrayList<String> topTerms = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = null;
        DBImportData instance = new DBImportData();
        instance.findOutTermLevels(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets, allLevelsOfImportThes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of findOutTopTermsAndOrphans method, of class DBImportData.
     
    @Test
    public void testFindOutTopTermsAndOrphans() {
        System.out.println("findOutTopTermsAndOrphans");
        HashMap<String, ArrayList<String>> descriptorNts = null;
        ArrayList<String> allTermsHavingBTs = null;
        ArrayList<String> allTermsHavingNTs = null;
        ArrayList<String> allTermsWithoutBTsorNTs = null;
        ArrayList<String> topTerms = null;
        HashMap<String, ArrayList<String>> hierarchyFacets = null;
        DBImportData instance = new DBImportData();
        instance.findOutTopTermsAndOrphans(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of readNextLevelSetTermsAndBts method, of class DBImportData.
     
    @Test
    public void testReadNextLevelSetTermsAndBts() {
        System.out.println("readNextLevelSetTermsAndBts");
        ArrayList<String> currentLevel = null;
        HashMap<String, ArrayList<String>> allNts = null;
        ArrayList<String> parsedTerms = null;
        DBImportData instance = new DBImportData();
        HashMap<String, ArrayList<String>> expResult = null;
        HashMap<String, ArrayList<String>> result = instance.readNextLevelSetTermsAndBts(currentLevel, allNts, parsedTerms);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of readNextLevelSetTermsAndBtsInSortItems method, of class DBImportData.
     
    @Test
    public void testReadNextLevelSetTermsAndBtsInSortItems() {
        System.out.println("readNextLevelSetTermsAndBtsInSortItems");
        ArrayList<String> currentLevel = null;
        HashMap<SortItem, ArrayList<SortItem>> allNts = null;
        ArrayList<String> parsedTerms = null;
        DBImportData instance = new DBImportData();
        HashMap<SortItem, ArrayList<SortItem>> expResult = null;
        HashMap<SortItem, ArrayList<SortItem>> result = instance.readNextLevelSetTermsAndBtsInSortItems(currentLevel, allNts, parsedTerms);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of specifyOrphansStatus method, of class DBImportData.
     
    @Test
    public void testSpecifyOrphansStatus() {
        System.out.println("specifyOrphansStatus");
        UserInfoClass SessionUserInfo = null;
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        IntegerObject tms_session = null;
        StringObject resultObj = null;
        DBImportData instance = new DBImportData();
        instance.specifyOrphansStatus(SessionUserInfo, Q, TA, sis_session, tms_session, resultObj);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */

    /**
     * Test of readXMLTag method, of class DBImportData.
     
    @Test
    public void testReadXMLTag() {
        System.out.println("readXMLTag");
        String test = "";
        DBImportData instance = new DBImportData();
        String expResult = "";
        String result = instance.readXMLTag(test);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */
    
}
