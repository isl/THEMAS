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
package DB_Classes;

import DB_Classes.DBGeneral;
import Users.UserInfoClass;

import Utils.SessionWrapperClass;
import Utils.Utilities;
import Utils.Parameters;
import java.util.*;
import javax.servlet.http.*;
import java.util.Vector;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/*---------------------------------------------------------------------
                            DBStatistics
-----------------------------------------------------------------------
  class for quering DB for statistics
----------------------------------------------------------------------*/
public class DBStatistics {

    /*----------------------------------------------------------------------
                        GetStatistics()
    -----------------------------------------------------------------------
    INPUT: - SessionWrapperClass sessionInstance: the current session info
           - String statisticsKind: StatisticsOfTerms_DIV/StatisticsOfHierarchies_DIV/StatisticsOfFacets_DIV/
                                    StatisticsOfSources_DIV/StatisticsOfUsers_DIV
    OUTPUT: - an XML string with the statistic results of the corresponding statisticsKind
    CALLED BY: servlet Statistics
    ------------------------------------------------------------------------*/
    public String GetStatistics(SessionWrapperClass sessionInstance, String statisticsKind, HttpServletRequest request, Locale targetLocale) {
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        // open SIS and TMS connection
        DBGeneral dbGen = new DBGeneral();
        QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();                
        IntegerObject sis_session = new IntegerObject();

        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBStatistics GetStatistics()");
            return "";
        }
        
        String XMLStr = "";
        // switch to the specific statistics kind
        if (statisticsKind.equals("StatisticsOfTerms_DIV")) {
            XMLStr = GetStatisticsOfTerms(Q, sis_session, SessionUserInfo);
        }
        if (statisticsKind.equals("StatisticsOfHierarchies_DIV")) {
            XMLStr = GetStatisticsOfHierarchies(Q, sis_session, SessionUserInfo, targetLocale);
        }        
        if (statisticsKind.equals("StatisticsOfFacets_DIV")) {
            XMLStr = GetStatisticsOfFacets(Q, sis_session, SessionUserInfo, targetLocale);
        }                
        if (statisticsKind.equals("StatisticsOfSources_DIV")) {
            XMLStr = GetStatisticsOfSources(Q, sis_session, SessionUserInfo, targetLocale);
        }                        
        if (statisticsKind.equals("StatisticsOfUsers_DIV")) {
            XMLStr = GetStatisticsOfUsers(Q, sis_session, SessionUserInfo, targetLocale);
        }                                
            
        //end query and close connection
        Q.free_all_sets();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        return XMLStr;
    }    
    
    /*----------------------------------------------------------------------
                        GetStatisticsOfTerms()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the statistic results for the terms of current thesaurus
    CALLED BY: GetStatistics()
    ------------------------------------------------------------------------*/
    private String GetStatisticsOfTerms(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBStatisticsTerms DBST = new DBStatisticsTerms();
        
        String XMLStr = "<StatisticsOfTerms>";
        XMLStr += "<total>" + DBST.GetTotalNumber(Q, sis_session, SessionUserInfo) + "</total>";
        XMLStr += "<total_XA>" + DBST.GetTotalNumberXA(Q, sis_session, SessionUserInfo) + "</total_XA>";
        XMLStr += "<total_PrefferedTranslations>" + DBST.GetTotalNumberPrefferedTranslations(Q, sis_session, SessionUserInfo) + "</total_PrefferedTranslations>";
        XMLStr += "<total_NonPrefferedTranslations>" + DBST.GetTotalNumberNonPrefferedTranslations(Q, sis_session, SessionUserInfo) + "</total_NonPrefferedTranslations>";
        XMLStr += "</StatisticsOfTerms>";

        return XMLStr;
    }        
    /*----------------------------------------------------------------------
                        GetStatisticsOfHierarchies()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the statistic results for the hierarcies of current thesaurus
    CALLED BY: GetStatistics()
    ------------------------------------------------------------------------*/
    private String GetStatisticsOfHierarchies(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBStatisticsHierarchies DBSH = new DBStatisticsHierarchies();
        
        String XMLStr = "<StatisticsOfHierarchies>";
        // <total>
        XMLStr += "<total>" + DBSH.GetTotalNumber(Q, sis_session, SessionUserInfo) + "</total>";

        
        Vector<String> HierarchiesVector = new Vector<String>();
        Vector<IntegerObject> HierarchiesTermsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> HierarchiesNonPrefferedTermsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> HierarchiesTranslationsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> HierarchiesNonPrefferedTranslationsCountVector = new Vector<IntegerObject>();
        DBSH.GetNumberOfTermsAndTranslationsPerHierarchy(Q, sis_session, SessionUserInfo, targetLocale,
             HierarchiesVector, HierarchiesTermsCountVector, HierarchiesNonPrefferedTermsCountVector, 
             HierarchiesTranslationsCountVector, HierarchiesNonPrefferedTranslationsCountVector);
        // copy HierarchiesVector to HierarchiesVectorSortedUI (UI encoded without prefix)
        Vector<String> HierarchiesVectorSortedUI = new Vector<String>();      
        
        int HierarchiesVectorSize = HierarchiesVector.size();
        for (int i = 0; i < HierarchiesVectorSize; i++) {
            String currentHierarchy = HierarchiesVector.get(i);
            HierarchiesVectorSortedUI.add(dbGen.removePrefix(currentHierarchy));
        }
        
        XMLStr += "<NumberOfTermsAndTranslationsPerHierarchy>";
        // for each hierarchy
        int HierarchiesVectorSortedUISize = HierarchiesVectorSortedUI.size();
        for (int i = 0; i < HierarchiesVectorSortedUISize; i++) {
            String currentHierarchy = HierarchiesVectorSortedUI.get(i);
            IntegerObject currentHierarchyTermsCount = HierarchiesTermsCountVector.get(i);
            IntegerObject currentHierarchyNonPrefferedTermsCount = HierarchiesNonPrefferedTermsCountVector.get(i);
            IntegerObject currentHierarchyEnglishWordsCount = HierarchiesTranslationsCountVector.get(i);
            IntegerObject currentHierarchyNonPrefferedEnglishWordsCount = HierarchiesNonPrefferedTranslationsCountVector.get(i);
            XMLStr += "<hierarchy>";
            XMLStr += "<name>" + Utilities.escapeXML(currentHierarchy) + "</name>";
            XMLStr += "<NumberOfTerms>" + currentHierarchyTermsCount.getValue() + "</NumberOfTerms>";
            XMLStr += "<NumberOfNonPrefferedTerms>" + currentHierarchyNonPrefferedTermsCount.getValue() + "</NumberOfNonPrefferedTerms>";
            XMLStr += "<NumberOfTranslations>" + currentHierarchyEnglishWordsCount.getValue() + "</NumberOfTranslations>";
            XMLStr += "<NumberOfNonPrefferedTranslations>" + currentHierarchyNonPrefferedEnglishWordsCount.getValue() + "</NumberOfNonPrefferedTranslations>";
            XMLStr += "</hierarchy>";            
        }        
        XMLStr += "</NumberOfTermsAndTranslationsPerHierarchy>";

        XMLStr += "</StatisticsOfHierarchies>";

        return XMLStr;
    }            
    /*----------------------------------------------------------------------
                        GetStatisticsOfFacets()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the statistic results for the facets of current thesaurus
    CALLED BY: GetStatistics()
    ------------------------------------------------------------------------*/
    private String GetStatisticsOfFacets(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBStatisticsFacets DBSF = new DBStatisticsFacets();
        
        String XMLStr = "<StatisticsOfFacets>";
        // <total>
        XMLStr += "<total>" + DBSF.GetTotalNumber(Q, sis_session, SessionUserInfo) + "</total>";
        
        // <NumberOfHierarchiesAndTermsAndEnglishWordsPerFacet>
        Vector<String> FacetsVector = new Vector<String>();
        Vector<IntegerObject> FacetsHierarciesCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> FacetsTermsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> FacetsNonPrefferedTermsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> FacetsTranslationsCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> FacetsNonPrefferedTranslationsCountVector = new Vector<IntegerObject>();
        DBSF.GetNumberOfHierarchiesAndTermsAndTranslationsPerFacet(Q, sis_session, SessionUserInfo, targetLocale,
                FacetsVector, FacetsHierarciesCountVector, 
                FacetsTermsCountVector, FacetsNonPrefferedTermsCountVector, 
                FacetsTranslationsCountVector, FacetsNonPrefferedTranslationsCountVector);
        // copy FacetsVector to FacetsVectorSortedUI (UI encoded without prefix)
        Vector<String> FacetsVectorSortedUI = new Vector<String>();      
        
        int FacetsVectorSize = FacetsVector.size();
        for (int i = 0; i < FacetsVectorSize; i++) {
            String currentFacet = FacetsVector.get(i);
            FacetsVectorSortedUI.add(dbGen.removePrefix(currentFacet));
        }
        
        XMLStr += "<NumberOfHierarchiesAndTermsAndTranslationsPerFacet>";
        // for each facet
        int FacetsVectorSortedUISize = FacetsVectorSortedUI.size();
        for (int i = 0; i < FacetsVectorSortedUISize; i++) {
            String currentFacet = FacetsVectorSortedUI.get(i);
            IntegerObject currentFacetHierarciesCount = FacetsHierarciesCountVector.get(i);
            IntegerObject currentFacetTermsCount = FacetsTermsCountVector.get(i);
            IntegerObject currentFacetNonPrefferedTermsCount = FacetsNonPrefferedTermsCountVector.get(i);
            IntegerObject currentFacetEnglishWordsCount = FacetsTranslationsCountVector.get(i);
            IntegerObject currentFacetNonPrefferedEnglishWordsCount = FacetsNonPrefferedTranslationsCountVector.get(i);
            XMLStr += "<facet>";
            XMLStr += "<name>" + Utilities.escapeXML(currentFacet) + "</name>";
            XMLStr += "<NumberOfHierarchies>" + currentFacetHierarciesCount.getValue() + "</NumberOfHierarchies>";
            XMLStr += "<NumberOfTerms>" + currentFacetTermsCount.getValue() + "</NumberOfTerms>";
            XMLStr += "<NumberOfNonPrefferedTerms>" + currentFacetNonPrefferedTermsCount.getValue() + "</NumberOfNonPrefferedTerms>";
            XMLStr += "<NumberOfTranslations>" + currentFacetEnglishWordsCount.getValue() + "</NumberOfTranslations>";
            XMLStr += "<NumberOfNonPrefferedTranslations>" + currentFacetNonPrefferedEnglishWordsCount.getValue() + "</NumberOfNonPrefferedTranslations>";
            XMLStr += "</facet>";
            //Utils.StaticClass.webAppSystemOutPrintln("Facet: " + dbGen.removePrefix(currentFacet) + " with " + currentFacetHierarciesCount.getValue() + " hierarchies and " + currentFacetTermsCount.getValue() + " terms");
        }        
        XMLStr += "</NumberOfHierarchiesAndTermsAndTranslationsPerFacet>";

        XMLStr += "</StatisticsOfFacets>";
        return XMLStr;
    }    
    /*----------------------------------------------------------------------
                        GetStatisticsOfSources()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the statistic results for the sources of current thesaurus
    CALLED BY: GetStatistics()
    ------------------------------------------------------------------------*/
    private String GetStatisticsOfSources(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBStatisticsSources DBSS = new DBStatisticsSources();
        
        String XMLStr = "<StatisticsOfSources>";
        XMLStr += "<total>" + DBSS.GetTotalNumber(Q, sis_session, SessionUserInfo) + "</total>";
        
        // <NumberOfTermsPerSourceGrEn>
        Vector<String> SourcesVector = new Vector<String>();
        Vector<IntegerObject> TermsSourceGrCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> TermsSourceEnCountVector = new Vector<IntegerObject>();
        DBSS.GetNumberOfTermsPerSourceGrEn(Q, sis_session, SessionUserInfo, targetLocale, SourcesVector, TermsSourceGrCountVector, TermsSourceEnCountVector);
        // copy SourcesVector to SourcesVectorSortedUI (UI encoded without prefix)
        Vector<String> SourcesVectorSortedUI = new Vector<String>();      
        
        int SourcesVectorSize = SourcesVector.size();
        for (int i = 0; i < SourcesVectorSize; i++) {
            String currentSource = SourcesVector.get(i);
            SourcesVectorSortedUI.add(dbGen.removePrefix(currentSource));
        }
        
        XMLStr += "<NumberOfTermsPerTranslationsSource>";
        // for each source
        int SourcesVectorSortedUISize = SourcesVectorSortedUI.size();
        for (int i = 0; i < SourcesVectorSortedUISize; i++) {
            String currentSource = SourcesVectorSortedUI.get(i);
            IntegerObject currentSourceGrTermsCount = TermsSourceGrCountVector.get(i);
            IntegerObject currentSourceEnTermsCount = TermsSourceEnCountVector.get(i);
            XMLStr += "<source>";
            XMLStr += "<name>" + Utilities.escapeXML(currentSource) + "</name>";
            XMLStr += "<NumberOfTermsPerTermSource>" + currentSourceGrTermsCount.getValue() + "</NumberOfTermsPerTermSource>";
            XMLStr += "<NumberOfTermsPerTranslationSource>" + currentSourceEnTermsCount.getValue() + "</NumberOfTermsPerTranslationSource>";
            XMLStr += "</source>";
            //Utils.StaticClass.webAppSystemOutPrintln("Source: " + dbGen.removePrefix(currentSource) + " with " + currentSourceGrTermsCount.getValue() + " terms (GR) " + currentSourceEnTermsCount.getValue() + " terms (EN)");
        }        
        XMLStr += "</NumberOfTermsPerTranslationsSource>";
        
        XMLStr += "</StatisticsOfSources>";

        return XMLStr;
    }       
    /*----------------------------------------------------------------------
                        GetStatisticsOfUsers()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the statistic results for the users of current thesaurus
    CALLED BY: GetStatistics()
    ------------------------------------------------------------------------*/
    private String GetStatisticsOfUsers(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBStatisticsUsers DBSU = new DBStatisticsUsers();
        
        String XMLStr = "<StatisticsOfUsers>";
        XMLStr += "<total>" + DBSU.GetTotalNumber(Q, sis_session, SessionUserInfo) + "</total>";
        
        // <NumberOfTermsCreatedAndLastModifiedPerUser>
        Vector<String> UsersVector = new Vector<String>();
        Vector<IntegerObject> TermsCreatedCountVector = new Vector<IntegerObject>();
        Vector<IntegerObject> TermsLastModifiedCountVector = new Vector<IntegerObject>();
        DBSU.GetNumberOfTermsCreatedAndLastModifiedPerUser(Q, sis_session, SessionUserInfo, targetLocale, UsersVector, TermsCreatedCountVector, TermsLastModifiedCountVector);
        // copy UsersVector to UsersVectorSortedUI (UI encoded without prefix)
        Vector<String> UsersVectorSortedUI = new Vector<String>();      
        
        int UsersVectorSize = UsersVector.size();
        for (int i = 0; i < UsersVectorSize; i++) {
            String currentUser = UsersVector.get(i);
            UsersVectorSortedUI.add(dbGen.removePrefix(currentUser));
        }
       
        XMLStr += "<NumberOfTermsCreatedAndLastModifiedPerUser>";
        // for each user
        int UsersVectorSortedUISize = UsersVectorSortedUI.size();        
        for (int i = 0; i < UsersVectorSortedUISize; i++) {
            String currentUser = UsersVectorSortedUI.get(i);
            IntegerObject currentUserTermsCreatedCount = TermsCreatedCountVector.get(i);
            IntegerObject currentUserTermsLastModifiedCount = TermsLastModifiedCountVector.get(i);
            XMLStr += "<user>";
            XMLStr += "<name>" + Utilities.escapeXML(currentUser) + "</name>";
            XMLStr += "<NumberOfTermsCreated>" + currentUserTermsCreatedCount.getValue() + "</NumberOfTermsCreated>";
            XMLStr += "<NumberOfTermsLastModified>" + currentUserTermsLastModifiedCount.getValue() + "</NumberOfTermsLastModified>";
            XMLStr += "</user>";
            //Utils.StaticClass.webAppSystemOutPrintln("User: " + dbGen.removePrefix(currentUser) + " with " + currentUserTermsCreatedCount.getValue() + " terms created and " + currentUserTermsLastModifiedCount.getValue() + " terms last modified");
        }        
        XMLStr += "</NumberOfTermsCreatedAndLastModifiedPerUser>";        
        
        XMLStr += "</StatisticsOfUsers>";

        return XMLStr;
    }                   
    
}
