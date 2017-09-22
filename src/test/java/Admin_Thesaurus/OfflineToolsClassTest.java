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
import neo4j_sisapi.StringObject;
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
public class OfflineToolsClassTest {
    
    public OfflineToolsClassTest() {
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
     * Test of main method, of class OfflineToolsClass.
     */
    @org.junit.Test
    public void testMain() {
        System.out.println("main");
        
        /*
        Modes:
        ======================================================
        private static String shutDownDb = "ShutDownDatabase";
        private static String importXMLMode = "ImportFromXML";
        private static String exportXMLMode = "ExportToXML";
        private static String mergeMode = "MergeThesauri";
        private static String lockSystemMode = "LockSystem";
        private static String unlockSystemMode =  "UnLockSystem";
        private static String fixDbMode = "FixDB";    
        private static String importFromTsvMode = "ImportFromTSV";
        private static String exportToTsvMode = "ExportToTSV";
        
        */
        
        //String mode = "ExportToTSV";
        String mode = "";
        OfflineToolsClass.printExpectedParametersAccordingToMode(mode);
        
        // <editor-fold defaultstate="collapsed" desc="Test for ImportFromXML">	
        if(mode.equals("ImportFromXML")){
            /*
            For mode: ImportFromXML the expected arguments are:
            1) ImportFromXML
            2) Web Application Base Path
            3) ThesaurusName (No spaces just latin chars)
            4) Input XML Full File Path
            5) Issues report xml file full path (an html will also be produeced with the same name but different extension)            
            */
            //String basePath = "/home/elias/Desktop/Installations/DevThemas/THEMAS/target/THEMAS-1.1-SNAPSHOT";
            String basePath = "C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\THEMAS\\target\\THEMAS-1.1-SNAPSHOT";
            String thesaurusName = "ANCIENT";
            //String inputXML = "C:\\Users\\Elias\\BackupFiles\\Desktop\\Ancient Theatres\\Export_Thesaurus_ANCIENT_2017-08-11_12-56-53-962.xml";
            //String inputXML = "C:\\Users\\Elias\\BackupFiles\\Desktop\\Ancient Theatres\\Export_Thesaurus_ANCIENT_2017-08-11_12-56-53-962.xml";
            //String inputXML = "C:\\\\Users\\\\Elias\\\\BackupFiles\\\\Desktop\\Export_Thesaurus_ANCIENT_2017-09-22_13-33-54-493.xml";
            String inputXML =  "C:\\\\Users\\\\Elias\\\\BackupFiles\\\\Desktop\\Export_Thesaurus_ANCIENT_2017-09-22_17-20-17-628.xml";
            String exportPath = "C:\\Users\\Elias\\BackupFiles\\Desktop\\temp\\testImport.xml"; //linux testing "/home/elias/Projects/Neo4jDbs/Tools/input-output";

            String[] args = {mode,basePath, thesaurusName, inputXML, exportPath};
            OfflineToolsClass.main(args);
        }
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Test for ExportToXML">	
        else if(mode.equals("ExportToXML")){
            /*
            For mode: ExportToXML the expected arguments are:
            1) ExportToXML
            2) Web Application Base Path
            3) ThesaurusName (Thesaurus must exist or use value: XXXXXX in order to export all existing thesauri)
            4) Export XML Full Folder Path 
            
            */
            //String basePath = "/home/elias/Desktop/Installations/DevThemas/THEMAS/target/THEMAS-1.1-SNAPSHOT";
            String basePath = "C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\THEMAS\\target\\THEMAS-1.1-SNAPSHOT";
            String thesaurusName = "ANCIENT";//export all thesauri
            String exportPath = "C:\\Users\\Elias\\BackupFiles\\Desktop\\temp"; //linux testing "/home/elias/Projects/Neo4jDbs/Tools/input-output";

            String[] args = {mode,basePath, thesaurusName, exportPath};
            OfflineToolsClass.main(args);
        }
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Test for ImportFromTSV">	
        else if (mode.equals("ImportFromTSV")){
            /*
             For mode: "ImportFromTSV" the expected arguments are:
            ==========================================================================
            1) ImportFromTSV
            2) Web Application Base Path
            3) Full path to the TSV to be loaded.
            4) Boolean value true or false that determines if the TSV contains Generic Definitions or Not.
            5) Boolean value true or false that determines if the transliteration properties should be recomputed or Not (keeping only the ones defined in the tsv file).
            */
            String basePath = "C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\THEMAS\\target\\THEMAS-1.1-SNAPSHOT";
            String importTsvPath = "C:\\Projects\\THEMAS_DB_Folder\\TSVs\\Exporter_Output_at_2017-09-08-15-59-58.tsv";
            String containsgeneric = "true";
            String recomputeTransliterations = "true";
            
            String[] args = {mode,basePath, importTsvPath,containsgeneric,recomputeTransliterations};
            OfflineToolsClass.main(args);
        }
        //</editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Test for ExportToTSV">	
        else if (mode.equals("ExportToTSV")){
            /*            
            For mode: "ExportToTSV" the expected arguments are:
            1) ExportToTSV
            2) Web Application Base Path
            3) Boolean value true or false that determines if the TSV will ONLY contain Generic data or Not.
            4) Boolean value true or false that determines if the TSV will skip Generic data or Not.
               If previous value was set tot true then this argument is just ignored.
            5) Full Path To TSV outPutFolder (optional variable if not is used then TSVs folder will be selected.
            */
            String basePath = "C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\THEMAS\\target\\THEMAS-1.1-SNAPSHOT";
            String onlygeneric = "false";
            String skipgeneric = "false";
            String exportPath = "C:\\Projects\\THEMAS_DB_Folder\\TSVs\\"; //linux testing "/home/elias/Projects/Neo4jDbs/Tools/input-output";

            String[] args = {mode,basePath, onlygeneric,skipgeneric, exportPath};
            OfflineToolsClass.main(args);
        }
        
        // </editor-fold>
        //fail("The test case is a prototype.");
    }
    
}
