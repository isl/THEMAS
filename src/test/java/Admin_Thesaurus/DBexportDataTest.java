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
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.NodeInfoSortItemContainer;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.Utilities;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
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
public class DBexportDataTest {
    
    public DBexportDataTest() {
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

    String basePath ="C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\_THEMAS_ProjectFolder\\Development\\THEMAS\\src\\main\\webapp"; 
    
    /**
     * Test of exportThesaurusActions method, of class DBexportData.
     
    @Test
    public void testExportThesaurusActions() {
        System.out.println("exportThesaurusActions");
        SessionWrapperClass sessionInstance = new SessionWrapperClass();

        Parameters.initParams(basePath);
        //these 2 initialize the database path
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
        
        String exprortThesaurus = "PARTHENOS";
        String exportSchemaName = ConstantParameters.xmlschematype_skos;
        
        
        UserInfoClass SessionUserInfo = new UserInfoClass();
        SessionUserInfo.name="test";
        SessionUserInfo.password="test";
        SessionUserInfo.thesaurusGroups.add(ConstantParameters.Group_Administrator);
        
        SessionUserInfo.UILang = Parameters.UILang;
        UsersClass webusers = new UsersClass();
        webusers.UpdateSessionUserSessionAttribute(SessionUserInfo,exprortThesaurus);
        
        OutputStreamWriter logFileWriter = null;
        String logFileNamePath = "C:\\tests\\";
        String time = Utilities.GetNow();
        
        String Filename = "Export_Thesaurus_" + exprortThesaurus + "_" + time;
        
        
        String hostName = "localhost";
        String port = "8084";
        
        if (exportSchemaName.equals(ConstantParameters.xmlschematype_skos)) {
                Filename += ".rdf";
                ConstantParameters.referenceThesaurusSchemeName = "http://"+hostName+":" + port + "/" + Parameters.ApplicationName + "#" + exprortThesaurus;
                ConstantParameters.SchemePrefix = "http://"+hostName+":" + port + "/" + Parameters.ApplicationName +"/"+ exprortThesaurus;
                
                //ConstantParameters.SchemePrefix = ConstantParameters.SchemePrefix.toLowerCase();

            } else if (exportSchemaName.equals(ConstantParameters.xmlschematype_THEMAS)) {
                Filename += ".xml";
            }

            logFileNamePath += Filename;
        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);
            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");


            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile of export data from thesaurus: " + exprortThesaurus + " in file: " + logFileNamePath + ".");

        } catch (FileNotFoundException | UnsupportedEncodingException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        
        ArrayList<String> thesauriNames = new ArrayList<>();
        ArrayList<String> allHierarchies = new ArrayList<>();
        ArrayList<String> allGuideTerms = new ArrayList<>();
        
        
        DBexportData instance = new DBexportData();
        
        try {
            instance.exportThesaurusActions(SessionUserInfo, exprortThesaurus, exportSchemaName, logFileWriter, thesauriNames, allHierarchies, allGuideTerms);
        }
        catch(Exception ex){
            Utils.StaticClass.handleException(ex);
        }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    */

    /**
     * Test of ReadTermStatuses method, of class DBexportData.
     
    @Test
    public void testReadTermStatuses() throws Exception {
        System.out.println("ReadTermStatuses");
        String selectedThesaurus = "";
        QClass Q = null;
        IntegerObject sis_session = null;
        ArrayList<String> output = null;
        ArrayList<String> allTerms = null;
        HashMap<String, NodeInfoSortItemContainer> termsInfo = null;
        ArrayList<Long> resultNodesIds = null;
        DBexportData instance = new DBexportData();
        instance.ReadTermStatuses(selectedThesaurus, Q, sis_session, output, allTerms, termsInfo, resultNodesIds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of statusDBtoUImapping method, of class DBexportData.
     
    @Test
    public void testStatusDBtoUImapping() {
        System.out.println("statusDBtoUImapping");
        String selectedThesaurus = "";
        String dbStatus = "";
        DBexportData instance = new DBexportData();
        String expResult = "";
        String result = instance.statusDBtoUImapping(selectedThesaurus, dbStatus);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    **/

    /**
     * Test of ReadTermCommentCategories method, of class DBexportData.
     
    @Test
    public void testReadTermCommentCategories() throws Exception {
        System.out.println("ReadTermCommentCategories");
        String selectedThesaurus = "";
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        ArrayList<String> output = null;
        ArrayList<String> allTerms = null;
        HashMap<String, NodeInfoSortItemContainer> termsInfo = null;
        ArrayList<Long> resultNodesIds = null;
        DBexportData instance = new DBexportData();
        instance.ReadTermCommentCategories(selectedThesaurus, Q, TA, sis_session, output, allTerms, termsInfo, resultNodesIds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of ReadTermFacetAndHierarchies method, of class DBexportData.
     
    @Test
    public void testReadTermFacetAndHierarchies() throws Exception {
        System.out.println("ReadTermFacetAndHierarchies");
        UserInfoClass SessionUserInfo = null;
        QClass Q = null;
        IntegerObject sis_session = null;
        int set_terms = 0;
        ArrayList<String> output = null;
        ArrayList<String> allTerms = null;
        HashMap<String, NodeInfoSortItemContainer> termsInfo = null;
        ArrayList<Long> resultNodesIds = null;
        DBexportData instance = new DBexportData();
        instance.ReadTermFacetAndHierarchies(SessionUserInfo, Q, sis_session, set_terms, output, allTerms, termsInfo, resultNodesIds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of ReadTermFacetAndHierarchiesInSortItems method, of class DBexportData.
     
    @Test
    public void testReadTermFacetAndHierarchiesInSortItems() throws Exception {
        System.out.println("ReadTermFacetAndHierarchiesInSortItems");
        UserInfoClass SessionUserInfo = null;
        QClass Q = null;
        IntegerObject sis_session = null;
        int set_terms = 0;
        ArrayList<String> output = null;
        ArrayList<String> allTerms = null;
        HashMap<String, NodeInfoSortItemContainer> termsInfo = null;
        ArrayList<Long> resultNodesIds = null;
        DBexportData instance = new DBexportData();
        instance.ReadTermFacetAndHierarchiesInSortItems(SessionUserInfo, Q, sis_session, set_terms, output, allTerms, termsInfo, resultNodesIds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of ReadRelatedSources method, of class DBexportData.
     
    @Test
    public void testReadRelatedSources() {
        System.out.println("ReadRelatedSources");
        String selectedThesaurus = "";
        QClass Q = null;
        TMSAPIClass TA = null;
        IntegerObject sis_session = null;
        int set_terms = 0;
        HashMap<String, NodeInfoSortItemContainer> sourcesInfo = null;
        ArrayList<String> allSources = null;
        DBexportData instance = new DBexportData();
        instance.ReadRelatedSources(selectedThesaurus, Q, TA, sis_session, set_terms, sourcesInfo, allSources);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/
    
}
