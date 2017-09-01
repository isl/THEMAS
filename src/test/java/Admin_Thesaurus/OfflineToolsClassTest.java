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
        
        String mode = "ExportToXML";
        //String basePath = "/home/elias/Desktop/Installations/DevThemas/THEMAS/target/THEMAS-1.1-SNAPSHOT";
        String basePath = "C:\\Users\\Elias\\BackupFiles\\Projects\\THEMAS_RELATED\\THEMAS\\target\\THEMAS-1.1-SNAPSHOT";
        String thesaurusName = "XXXXXX";//export all thesauri
        String exportPath = "C:\\Users\\Elias\\BackupFiles\\Desktop"; //linux testing "/home/elias/Projects/Neo4jDbs/Tools/input-output";
        
        String[] args = {mode,basePath, thesaurusName, exportPath};
        
        //fail("The test case is a prototype.");
        // TODO review the generated test code and remove the default call to fail.
        
        //OfflineToolsClass.main(args);
        
    }
    
}
