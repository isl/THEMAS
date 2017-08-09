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
package Utils;

import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.StringLocaleComparator;
import Utils.GuideTermSortItemComparator;
import Utils.SortItemLocaleComparator;
import java.io.*;
import java.util.*;
import neo4j_sisapi.*;
import javax.servlet.http.*;
//import isl.dms.DMSException;
//import isl.dms.xml.XMLTransform;
import java.text.SimpleDateFormat;
import java.net.URLDecoder;

import javax.xml.transform.*;

import javax.xml.transform.Result;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.xpath.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.util.regex.Matcher;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import org.w3c.dom.Element;

/*---------------------------------------------------------------------
Utilities
-----------------------------------------------------------------------
general class with utility methods
----------------------------------------------------------------------*/
public class Utilities {

    /*----------------------------------------------------------------------
    Constructor of Utilities
    -----------------------------------------------------------------------*/
    public Utilities() {
    }

    public String mergeStrings(String value1possiblyContainingValue2, String value2){
        String returnVal = value2;
        if(value1possiblyContainingValue2==null || value1possiblyContainingValue2.trim().length()==0){
                return returnVal;
        }
        if(value1possiblyContainingValue2.contains(" ### ")){
            String[] parts = value1possiblyContainingValue2.split(" ### ");
            boolean found = false;
            for(String str: parts){
                if(str!=null && str.trim().equals(value2.trim())){
                    found = true;
                }
                
            }
            if(found){
                returnVal = value1possiblyContainingValue2;                 
            }
            else{
                returnVal = value1possiblyContainingValue2 + " ### " + value2;
            }
        }
        else{
            
            if(value1possiblyContainingValue2.trim().equals(value2.trim())){
                returnVal = value1possiblyContainingValue2;
            }
            else{
                returnVal = value1possiblyContainingValue2 + " ### " + value2;
            }
        }
        return returnVal;
    }
    /*---------------------------------------------------------------------
    getXMLStart()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStart: an XML string
    CALLED BY: all servlets in order to get the first part of their XML representation
    ----------------------------------------------------------------------*/
    public String getXMLStart(String LeftMenuMode) {
        String XMLStart =
                //"<?xml version=\"1.0\" encoding=\"windows-1253\"?>" +
                // changed by karam (6/2/2008) and also the xslTransform() 
                // method of this class so as to work properly.
                // the xslTransform_OLD() was working ONLY for encoding "windows-1253" and NOT for UTF-8 !!!
                ConstantParameters.xmlHeader
                + // "<?xml-stylesheet href=\"" + xsl + "\" type=\"text/xsl\"?>" +
                // (canceled by karam - 7/2/2008): it has NO sense because each servlet calls xslTransform()
                // method of this class with the corresponding XSL file as parameter and writes the final HTML code to writer output
                "\r\n<page title=\"Δημιουργός Θησαυρού\" language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\" mode=\"insert\">"
                + "\r\n<header>"
                + "\r\n<name>Δημιουργός Θησαυρού</name>"
                + //"\r\n<logo>images/thesaurusheader.jpg</logo>" +
                "\r\n</header>"
                + "\r\n<leftmenu>"
                + "\r\n<activemode>" + LeftMenuMode + "</activemode>"
                + /*"\r\n<option>" +
                "\r\n<name>Term</name>" +
                "\r\n<new alt=\"Δημιουργία\" kind=\"Term\">images/newdoc.gif</new>" +
                "<search alt=\"Αναζήτηση\">images/searchdoc.gif</search>" +
                "<viewAll alt=\"Εμφάνιση όλων των όρων\" url=\"/ViewAll_Terms\">images/showalldocs.gif</viewAll>" +
                "</option>" +
                "<option>" +
                "<name>Hierarchy</name>" +
                "<new alt=\"Δημιουργία\" kind=\"Hierarchy\" >images/newdoc.gif</new>" +
                "<search alt=\"Αναζήτηση\">images/searchdoc.gif</search>" +
                "<viewAll alt=\"Εμφάνιση όλων των ιεραρχιών\" url=\"/ViewAll_Hierarchies\">images/showalldocs.gif</viewAll>" +
                "</option>" +
                "<option>" +
                "<name>Facet</name>" +
                "<new alt=\"Δημιουργία\" kind=\"Facet\">images/newdoc.gif</new>" +
                "<search alt=\"Αναζήτηση\">images/searchdoc.gif</search>" +
                "<viewAll alt=\"Εμφάνιση όλων των μικροθησαυρών\" url=\"/ViewAll_Facets\" >images/showalldocs.gif</viewAll>" +
                "</option>" +
                "<option>" +
                "<name>Bibliography</name>" +
                "</option>" +
                "<option>" +
                "<name>Sources</name>" +
                "<new alt=\"Δημιουργία\" kind=\"Source\">images/newdoc.gif</new>" +
                "<search alt=\"Αναζήτηση\">images/searchdoc.gif</search>" +
                "<viewAll alt=\"Εμφάνιση όλων των πηγών\" url=\"/ViewAll_Sources\" >images/showalldocs.gif</viewAll>" +
                "</option>" +
                "<option>" +
                "<name>Statistics</name>" +
                "<statistics alt=\"Εμφάνιση Στατιστικών Θησαυρού\" href=\"Statistics?DIV=StatisticsOfTerms_DIV\">images/showalldocs.gif</statistics>" +
                "</option>" +
                "<option>" +
                "<name>THES_Admin</name>" +
                "<ThesOperations alt=\"Διαχείριση Θησαυρών\" href=\"ImportExportData?DIV=Import_Data_DIV\">images/db.gif</ThesOperations>" + 
                "<ThesChange alt=\"Αλλαγή Τρέχοντος Θησαυρού\" href=\"#\">images/editdoc.gif</ThesChange>" + 
                "</option>" +                
                "<option>" +
                "<name>DB Admin</name>" +
                "<anchor alt=\"Διαχείριση Βάσης Δεδομένων\" href=\"DBadmin?DIV=Create_Restore_DB_backup_DIV\">images/db.gif</anchor>" +
                "</option>" +
                
                "<option>" +
                "<name>ReleaseThes</name>" +
                "<anchor alt=\"Έκδοση Θησαυρού\" href=\"ReleaseThesaurus?DIV=ReleaseThesaurus_DIV\">images/ReleaseThesaurus.gif</anchor>" +
                "</option>" +
                "<option>" +
                "<name>Users</name>" +
                "<new alt=\"Δημιουργία\" kind=\"Users\">images/newdoc.gif</new>" +
                // temporarily NO search for users
                // "<search>images/searchdoc.gif</search>" +
                "<viewAll alt=\"Εμφάνιση όλων των χρηστών\" url=\"/ViewAll_Users\">images/showalldocs.gif</viewAll>" +
                "<shareThesaurus alt=\"Διαχείριση ιδιοτήτων χρηστών θησαυρού\">images/db.gif</shareThesaurus>" +
                "</option>" +*//*
                "<save>images/disk.gif</save>"+
                "<save-xml>images/disk_xml.gif</save-xml>"+*/ "</leftmenu>";

        return XMLStart;
    }

    /*---------------------------------------------------------------------
    getXMLMiddle()
    -----------------------------------------------------------------------
    INPUT: - String resultsString: the <results> contents
    - String upActiveTab: the up-part tab name to be active (see tabsetArray1)
    - String downActiveTab: the up-part tab name to be active (see tabsetArray1)
    OUTPUT: - String XMLMiddleStr: an XML string
    CALLED BY: all servlets in order to get the <content> part of their XML representation
    ----------------------------------------------------------------------*/
    public String getXMLMiddle(String resultsString, String upActiveTab/*, String downActiveTab*/) {
        // <tabset1>
        // the following strings must be the SAME with the strings defined in tabs.js!!!
        //String[] tabsetArray1 = {"Αλφαβητικά", "Συστηματικά", "Ιεραρχικά", "Γραφικά", "Αποτ.Αναζήτησης", "Κριτ.Αναζήτησης", "Ρυθμίσεις","Αποτ.Αναζήτ.Ιεραρχιών", "Αποτ.Αναζήτ.Θεμάτων"};
        String[] tabsetArray1 = {"Alphabetical", "Systematic", "Hierarchical", "Graphical", "SearchResults", "SearchCriteria", "Settings",
            "SearchHierarchyResults", "SearchFacetResults", "SearchSourceResults", "SearchUsersResults", "HierarchiesSearchCriteria", "FacetsSearchCriteria", "SourcesSearchCriteria", "New",
            "NewHierarchy", "NewFacet", "Move2Hier", "HistoricalInfo_Term", "HistoricalInfo_Hierarchy", "HistoricalInfo_Facet", "Rename", "RenameHierarchy", "RenameFacet",
            "Details", "HierarchyDetails", "FacetDetails", "SourceDetails"};
        String upTabsetXML = "";
        for (int i = 0; i < tabsetArray1.length; i++) {
            upTabsetXML += "<tab active=\"no\">" + tabsetArray1[i] + "</tab>";
        }
        upActiveTab = upActiveTab.replaceAll("&", "&amp;");
        upTabsetXML = upTabsetXML.replaceAll("<tab active=\"no\">" + upActiveTab + "</tab>", "<tab active=\"yes\">" + upActiveTab + "</tab>");

        String XMLMiddleStr =
                "<content>"
                + "<results>" + resultsString + "</results>"
                + "<part1>"
                + "<tabset1>" + upTabsetXML + "</tabset1>"
                + "<window name=\"up\"></window>"
                + "</part1>"
                + /*"<part2>" +
                "<tabset2>" + downTabsetXML + "</tabset2>" +
                "<window name=\"down\"></window>" +
                "</part2>" +*/ //"<menuOn>images/menuOn.png</menuOn>" +
                //"<menuOff>images/menuOff.png</menuOff>" +
                //"<edit>images/editdoc.gif</edit>" +
                //"<viewAll>images/showalldocs.gif</viewAll>" +
                //"<graph_view>images/SVGgraph.gif</graph_view>" +
                //"<greekEnglishIndex>images/book_open.gif</greekEnglishIndex>" +
                //"<alphabetical>images/alphabetical.gif</alphabetical>" +
                //"<systematic>images/systematic.gif</systematic>" +
                //"<hierarchical>images/hierarchical.gif</hierarchical>" +
                //"<rename>images/rename.gif</rename>" +
                "<move>images/move.png</move>"
                + //"<delete>images/deletedoc.gif</delete>" +
                //"<plus>images/treeplus.gif</plus>" +
                //"<minus>images/treeminus.gif</minus>" +
                "</content>";

        return XMLMiddleStr;
    }

    /*---------------------------------------------------------------------
    getXMLEnd()
    -----------------------------------------------------------------------
    OUTPUT: an XML string
    CALLED BY: most of the servlets for ending their XML representation
    ----------------------------------------------------------------------*/
    public String getXMLEnd() {
        String XMLEnd = "</page>";
        return XMLEnd;
    }

    /*---------------------------------------------------------------------
    getXMLUserInfo()
    -----------------------------------------------------------------------
    OUTPUT: an XML string with the current session user info
    CALLED BY: most of the servlets for ending their XML representation
    ----------------------------------------------------------------------*/
    public String getXMLUserInfo(UserInfoClass SessionUserInfo) {
        UsersClass tmsUsers = new UsersClass();


        String XMLTHEMASUserInfo = "<THEMASUserInfo>";
        XMLTHEMASUserInfo += "<name>" + SessionUserInfo.name + "</name>";
        XMLTHEMASUserInfo += "<userGroup tra=\"" + tmsUsers.translateGroup(SessionUserInfo.userGroup) + "\">" + SessionUserInfo.userGroup + "</userGroup>";
        XMLTHEMASUserInfo += "<selectedThesaurus>" + SessionUserInfo.selectedThesaurus + "</selectedThesaurus>";
        XMLTHEMASUserInfo += "<selectedThesaurusLowerCase>" + SessionUserInfo.selectedThesaurus.toLowerCase() + "</selectedThesaurusLowerCase>";
        XMLTHEMASUserInfo += "</THEMASUserInfo>";

        return XMLTHEMASUserInfo;
    }


    /*----------------------------------------------------------------------
    writePagingInfoXML()
    ------------------------------------------------------------------------
    OUTPUT : - an xml representation with the paging info
    FUNCTION: - constructs an xml representation with paging info 
    ------------------------------------------------------------------------*/
    public String writePagingInfoXML(int pagingListStep, int pagingFirst, int pagingQueryResultsCount, float elapsedTimeSec, String ServletName) {
        String pagingInfoXML = "<paging_info>";

        pagingInfoXML += "<ServletName>" + ServletName + "</ServletName>";
        pagingInfoXML += "<query_results_time>" + elapsedTimeSec + "</query_results_time>";
        pagingInfoXML += "<pagingQueryResultsCount>" + pagingQueryResultsCount + "</pagingQueryResultsCount>";
        pagingInfoXML += "<pagingListStep>" + pagingListStep + "</pagingListStep>";
        pagingInfoXML += "<pagingFirst>" + pagingFirst + "</pagingFirst>";

        int pagingLast = 0;

        if (pagingFirst + pagingListStep - 1 >= pagingQueryResultsCount) {
            pagingLast = pagingQueryResultsCount;
        } else {
            pagingLast = pagingFirst + pagingListStep - 1;
        }
        pagingInfoXML += "<pagingLast>" + pagingLast + "</pagingLast>";

        pagingInfoXML += "</paging_info>";

        return pagingInfoXML;
    }

    /**
     * Starts the timer
     * @return Start of timer as <CODE>long</CODE>
     */
    public static long startTimer() {
        long startTime = System.currentTimeMillis();
        return startTime;

    }

    /**
     * Stops the timer and counts time elapsed
     * @param startTime Start of timer as <CODE>long</CODE>
     * @return Elapsed time as <CODE>float</CODE>
     */
    public static float stopTimer(long startTime) {
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        return elapsedTimeSec;
    }

    //ALMOST IDENTICAL TO getResultsInXml
    public void getResultsInXmlGuideTermSorting(Vector<String> allTerms, Hashtable<String, NodeInfoSortItemContainer> termsInfo, Vector<String> output, StringBuffer xmlResults, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);
        SortItemLocaleComparator sortComparator = new SortItemLocaleComparator(targetLocale);


        xmlResults.append("<data translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");
        xmlResults.append("<output>");
        for (int m = 0; m < output.size(); m++) {

            String category = output.get(m);
            if (category.compareTo("id") == 0) {
                continue;
            } else {
                xmlResults.append("<" + category + "/>");
            }
        }
        xmlResults.append("</output>");

        xmlResults.append("<terms>");
        for (int i = 0; i < allTerms.size(); i++) {

            String targetTerm = allTerms.get(i);
            NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
            String type = targetTermInfo.containerType;

            if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                xmlResults.append("<ufterm index=\"" + (i + 1) + "\">");
                xmlResults.append("<ufname id=\"" + targetTermInfo.descriptorInfo.get("id").get(0).getSysId() + "\">");
                xmlResults.append(escapeXML(targetTerm));
                xmlResults.append("</ufname>");

                Vector<SortItem> values = new Vector<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get("use"));
                Collections.sort(values, sortComparator);

                for (int k = 0; k < values.size(); k++) {
                    xmlResults.append("<use id=\"" + values.get(k).getSysId() + "\" >");
                    xmlResults.append(escapeXML(values.get(k).getLogName()));
                    xmlResults.append("</use>");
                }
                xmlResults.append("</ufterm>");
                continue;
            }

            xmlResults.append("<term index=\"" + (i + 1) + "\">");

            long targetSysIdL = targetTermInfo.descriptorInfo.get("id").get(0).getSysId();
            xmlResults.append("<descriptor id=\"" + targetSysIdL + "\" >");
            //xmlResults.append("<descriptor>");
            xmlResults.append(escapeXML(targetTerm));
            xmlResults.append("</descriptor>");

            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo("id") == 0) {
                    continue;
                }
                Vector<SortItem> values = new Vector<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get(category));
                if (category.compareTo(ConstantParameters.nt_kwd) == 0 || category.compareTo(ConstantParameters.translation_kwd) == 0 || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                    Collections.sort(values, guideTermComparator);
                } else {
                    Collections.sort(values, sortComparator);
                }
                if (category.compareTo(ConstantParameters.uf_kwd) == 0
                        || category.compareTo(ConstantParameters.bt_kwd) == 0
                        || category.compareTo(ConstantParameters.nt_kwd) == 0
                        || category.compareTo(ConstantParameters.translation_kwd) == 0
                        || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) { // add id info in order to add anchors in paging
                    for (int k = 0; k < values.size(); k++) {
                        xmlResults.append("<" + category + " id=\"" + values.get(k).getSysId() + "\" linkClass=\"" + values.get(k).getLinkClass() + "\" >");
                        xmlResults.append(escapeXML(values.get(k).getLogName()));
                        xmlResults.append("</" + category + ">");
                    }
                } else if (category.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {
                    String checkStr = "";
                    for (int k = 0; k < values.size(); k++) {
                        if (k > 0) {
                            checkStr += "\n";
                        }
                        checkStr += values.get(k).log_name;
                    }
                    Hashtable<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                    Vector<String> langcodes = new Vector<String>(trSns.keySet());
                    Collections.sort(langcodes);
                    for (int k = 0; k < langcodes.size(); k++) {
                        String linkClass = langcodes.get(k);
                        String val = trSns.get(linkClass);
                        xmlResults.append("<" + category + " linkClass=\"" + linkClass + "\">");
                        xmlResults.append(escapeXML(val));
                        xmlResults.append("</" + category + ">");
                    }

                } else {
                    for (int k = 0; k < values.size(); k++) {
                        xmlResults.append("<" + category + ">");
                        xmlResults.append(escapeXML(values.get(k).getLogName()));
                        xmlResults.append("</" + category + ">");
                    }
                }

            }

            xmlResults.append("</term>");
        }
        xmlResults.append("</terms>");
        xmlResults.append("</data>");

    }

    //ALMOST IDENTICAL TO getResultsInXmlGuideTermSorting
    /*---------------------------------------------------------------------
    getResultsInXml()
    -----------------------------------------------------------------------
    INPUT: - Vector allTerms: the Vector with the terms to be parsed
    - String[] output: the properties of each term to be collected
    OUTPUT: a String with the XML representation of the results
    CALLED BY: servlets: ViewAll with output = {"name", ConstantParameters.dn_kwd}
    ----------------------------------------------------------------------*/
    public void getResultsInXml(Vector<String> allTerms, Hashtable<String, NodeInfoSortItemContainer> termsInfo, Vector<String> output, StringBuffer xmlResults, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        SortItemLocaleComparator sortComparator = new SortItemLocaleComparator(targetLocale);
        GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);

        xmlResults.append("<data translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");
        xmlResults.append("<output>");
        for (int m = 0; m < output.size(); m++) {

            String category = output.get(m);
            if (category.compareTo("id") == 0) {
                continue;
            } else {
                xmlResults.append("<" + category + "/>");
            }
        }
        xmlResults.append("</output>");

        xmlResults.append("<terms>");
        for (int i = 0; i < allTerms.size(); i++) {

            String targetTerm = allTerms.get(i);
            NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
            String type = targetTermInfo.containerType;

            if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                xmlResults.append("<ufterm index=\"" + (i + 1) + "\">");
                xmlResults.append("<ufname id=\"" + targetTermInfo.descriptorInfo.get("id").get(0).getSysId() + "\">");
                xmlResults.append(escapeXML(targetTerm));
                xmlResults.append("</ufname>");

                Vector<SortItem> values = new Vector<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get("use"));


                for (int k = 0; k < values.size(); k++) {
                    xmlResults.append("<use id=\"" + values.get(k).getSysId() + "\" >");
                    xmlResults.append(escapeXML(values.get(k).getLogName()));
                    xmlResults.append("</use>");
                }
                xmlResults.append("</ufterm>");
                continue;
            }

            xmlResults.append("<term index=\"" + (i + 1) + "\">");

            long targetSysIdL = targetTermInfo.descriptorInfo.get("id").get(0).getSysId();
            xmlResults.append("<descriptor id=\"" + targetSysIdL + "\" >");
            //xmlResults.append("<descriptor>");
            xmlResults.append(escapeXML(targetTerm));
            xmlResults.append("</descriptor>");

            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo("id") == 0) {
                    continue;
                }
                Vector<SortItem> values = new Vector<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get(category));
                if (category.compareTo(ConstantParameters.nt_kwd) == 0 || category.compareTo(ConstantParameters.translation_kwd) == 0 || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                    Collections.sort(values, guideTermComparator);
                } else {
                    Collections.sort(values, sortComparator);
                }

                if (category.compareTo(ConstantParameters.uf_kwd) == 0
                        || category.compareTo(ConstantParameters.bt_kwd) == 0
                        || category.compareTo(ConstantParameters.nt_kwd) == 0
                        || category.compareTo(ConstantParameters.translation_kwd) == 0
                        || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) { // add id info in order to add anchors in paging
                    for (int k = 0; k < values.size(); k++) {
                        xmlResults.append("<" + category + " id=\"" + values.get(k).getSysId() + "\" linkClass=\"" + values.get(k).getLinkClass() + "\" >");
                        xmlResults.append(escapeXML(values.get(k).getLogName()));
                        xmlResults.append("</" + category + ">");
                    }
                } else if (category.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {

                    String checkStr = "";
                    for (int k = 0; k < values.size(); k++) {
                        if (k > 0) {
                            checkStr += "\n";
                        }
                        checkStr += values.get(k).log_name;
                    }
                    Hashtable<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                    Vector<String> langcodes = new Vector<String>(trSns.keySet());
                    Collections.sort(langcodes);
                    for (int k = 0; k < langcodes.size(); k++) {
                        String linkClass = langcodes.get(k);
                        String val = trSns.get(linkClass);
                        xmlResults.append("<" + category + " linkClass=\"" + linkClass + "\">");
                        xmlResults.append(escapeXML(val));
                        xmlResults.append("</" + category + ">");
                    }

                } else {
                    for (int k = 0; k < values.size(); k++) {
                        xmlResults.append("<" + category + ">");
                        xmlResults.append(escapeXML(values.get(k).getLogName()));
                        xmlResults.append("</" + category + ">");
                    }
                }

            }

            xmlResults.append("</term>");
        }
        xmlResults.append("</terms>");
        xmlResults.append("</data>");
    }

    public void getResultsInXml(UserInfoClass SessionUserInfo, Vector allTerms, String[] output, StringBuffer XMLresults, QClass Q,TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale, DBGeneral dbGen) {
        XMLresults.append("<results>");

        int resultsLIMIT = allTerms.size();
        for (int i = 0; i < resultsLIMIT; i++) {

            XMLresults.append("<term>");

            for (int j = 0; j < output.length; j++) {
                String currentTerm = (String) (allTerms.get(i));
                if (output[j].equals("name")) {

                    XMLresults.append("<name>" + escapeXML(currentTerm) + "</name>");

                    // get the kind of the descriptor (new/released/obsolete) -- DISABLED AS RELEASED DESCRIPTORS ARE NOT SUPPORTED
                    /*StringObject targetTerm_el = new StringObject(prefix_el.concat(currentTerm));
                    int descriptorKind = dbGen.GetKindOfDescriptor(targetTerm_el);
                    XMLresults += "<kind>";
                    if (descriptorKind == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {
                    XMLresults += "new";
                    }
                    if (descriptorKind == ConstantParameters.DESCRIPTOR_OF_KIND_RELEASED) {
                    XMLresults += "released";
                    }
                    if (descriptorKind == ConstantParameters.DESCRIPTOR_OF_KIND_OBSOLETE) {
                    XMLresults += "obsolete";
                    }
                    XMLresults += "</kind>";
                     */
                } else if (output[j].equals(ConstantParameters.translation_kwd) || output[j].equals(ConstantParameters.uf_translations_kwd)) {
                    Vector<SortItem> vtranslations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, output[j].equals(ConstantParameters.translation_kwd), currentTerm, Q, sis_session);
                    Collections.sort(vtranslations, new GuideTermSortItemComparator(targetLocale));
                    XMLresults.append("<" + output[j] + ">");
                    for (int k = 0; k < vtranslations.size(); k++) {
                        XMLresults.append("<name linkClass=\"" + vtranslations.get(k).linkClass + "\">");
                        XMLresults.append(escapeXML(vtranslations.get(k).log_name));
                        XMLresults.append("</name>");
                    }
                    XMLresults.append("</" + output[j] + ">");
                } else {

                    Vector v = dbGen.returnResults(SessionUserInfo, currentTerm, output[j], Q,TA, sis_session);
                    Collections.sort(v, new StringLocaleComparator(targetLocale));
                    XMLresults.append("<" + output[j] + ">");
                    for (int k = 0; k < v.size(); k++) {
                        XMLresults.append("<name>");
                        XMLresults.append(escapeXML(v.get(k).toString()));
                        /*if (v.size() > 1 && k < v.size() - 1) {
                        XMLresults.append(", ");
                        }
                        
                         */
                        XMLresults.append("</name>");
                    }
                    XMLresults.append("</" + output[j] + ">");
                }
            }
            XMLresults.append("</term>");
        }
        XMLresults.append("</results>");


        return;
    }

    /*---------------------------------------------------------------------
    getResultsInXml()
    -----------------------------------------------------------------------
    INPUT:  Vector availableFacets: contains all available facets (either new or released)
    OUTPUT: a String with the XML representation of the available facets
    CALLED BY: Create_Modify_Hierarchy servlet
    ----------------------------------------------------------------------
    public String getResultsInXml_Hierarchy(Vector<String> availableFacets) {

    String XMLresults = "";
    XMLresults += "<currentcreatehierarchy>";
    for (int i = 0; i < availableFacets.size(); i++) {
    XMLresults += "<facetoption>";

    String currentHierarchy = (String) (availableFacets.get(i));
    XMLresults += "<name>" + escapeXML(currentHierarchy) + "</name>";

    XMLresults += "</facetoption>";
    }
    XMLresults += "</currentcreatehierarchy>";

    return XMLresults;
    }
     */
    /*getResultsinXml_Hierarchy()*/
    public String getResultsinXml_Hierarchy(UserInfoClass SessionUserInfo, Vector displayHierarchies, String[] output, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        DBGeneral dbGen = new DBGeneral();

        StringBuffer XMLresults = new StringBuffer();
        XMLresults.append("<data>");
        XMLresults.append("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if (category.compareTo("id") == 0 || category.compareTo("name") == 0) {
                continue;
            } else {
                XMLresults.append("<" + category + "/>");
            }
        }
        XMLresults.append("</output>");
        XMLresults.append("<hierarchies>");

        int resultsLIMIT = displayHierarchies.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            XMLresults.append("<hierarchy>");
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentHier = (String) (displayHierarchies.get(i));
                    XMLresults.append("<name>");
                    XMLresults.append(escapeXML(currentHier));
                    XMLresults.append("</name>");

                } else {
                    Vector<String> v = dbGen.returnResults_Hierarchy(SessionUserInfo, displayHierarchies.get(i).toString(), output[j], Q, sis_session, targetLocale);
                    if (v != null && v.size() > 0) {
                        Collections.sort(v, new StringLocaleComparator(targetLocale));
                    }
                    for (int k = 0; k < v.size(); k++) {
                        XMLresults.append("<" + output[j] + ">");
                        XMLresults.append(escapeXML(v.get(k)));
                        XMLresults.append("</" + output[j] + ">");
                    }
                }
            }
            XMLresults.append("</hierarchy>");
        }

        XMLresults.append("</hierarchies>");
        XMLresults.append("</data>");
        return XMLresults.toString();
    }

    public void getResultsInXml_Source(UserInfoClass SessionUserInfo, Vector<String> displaySources, String[] output, QClass Q, TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale, StringBuffer xmlResults) {

        DBGeneral dbGen = new DBGeneral();

        xmlResults.append("<data>");

        xmlResults.append("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if (category.compareTo("id") == 0 || category.compareTo("name") == 0) {
                continue;
            } else {
                xmlResults.append("<" + category + "/>");
            }
        }
        xmlResults.append("</output>");

        xmlResults.append("<sources>");
        int resultsLIMIT = displaySources.size();
        for (int i = 0; i < resultsLIMIT; i++) {

            xmlResults.append("<source index=\"" + (i + 1) + "\">");

            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentSource = (displaySources.get(i));
                    xmlResults.append("<name>" + escapeXML(currentSource) + "</name>");

                } else {
                    Vector<String> v = dbGen.returnResults_Source(SessionUserInfo, displaySources.get(i), output[j], Q,TA, sis_session);
                    Collections.sort(v, new StringLocaleComparator(targetLocale));

                    for (int k = 0; k < v.size(); k++) {
                        xmlResults.append("<" + output[j] + ">");
                        xmlResults.append(escapeXML(v.get(k)));
                        xmlResults.append("</" + output[j] + ">");
                    }

                }
            }
            xmlResults.append("</source>");
        }
        xmlResults.append("</sources>");
        xmlResults.append("</data>");

    }

    /*---------------------------------------------------------------------
    getResultsInXml_Facet()
    -----------------------------------------------------------------------
    INPUT: - Vector allTerms: the Vector with the terms to be parsed
    - String[] output: the properties of each term to be collected
    OUTPUT: a String with the XML representation of the results
    CALLED BY: servlets: ViewAll with output = {"name", ConstantParameters.dn_kwd}
    ----------------------------------------------------------------------*/
    public String getResultsInXml_Facet(UserInfoClass SessionUserInfo, Vector displayFacets, String[] output, QClass Q, IntegerObject sis_session, Locale targetLocale, DBGeneral dbGen) {

        StringBuffer XMLresults = new StringBuffer();
        ;
        XMLresults.append("<data>");
        XMLresults.append("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if (category.compareTo("id") == 0 || category.compareTo("name") == 0) {
                continue;
            } else {
                XMLresults.append("<" + category + "/>");
            }
        }
        XMLresults.append("</output>");

        XMLresults.append("<facets>");
        int resultsLIMIT = displayFacets.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            XMLresults.append("<facet>");
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentFacet = (String) (displayFacets.get(i));
                    XMLresults.append("<name>");
                    XMLresults.append(escapeXML(currentFacet));
                    XMLresults.append("</name>");

                } else {

                    Vector<String> v = dbGen.returnResults_Facet(SessionUserInfo, displayFacets.get(i).toString(), output[j], Q, sis_session, targetLocale);
                    if (v != null && v.size() > 0) {
                        Collections.sort(v, new StringLocaleComparator(targetLocale));
                    }

                    for (int k = 0; k < v.size(); k++) {
                        XMLresults.append("<" + output[j] + ">");
                        XMLresults.append(escapeXML(v.get(k)));
                        XMLresults.append("</" + output[j] + ">");
                    }

                }
            }
            XMLresults.append("</facet>");
        }
        XMLresults.append("</facets>");
        XMLresults.append("</data>");

        return XMLresults.toString();
    }

    /*----------------------------------------------------------------------
    getVectorInXml()
    ------------------------------------------------------------------------
    INPUT: - Vector vectorList: the Vector to be parsed
    - String xmlTagName: the name of the XML tag to be written (g.e. "facet", "topterm")
    OUTPUT : a String with the XML representation of the given Vector contents
    FUNCTION: constructs an XML representation of the given Vector contents
    CALLED BY: ViewInfo servlet
    ------------------------------------------------------------------------*/
    public String getVectorInXml(Vector vectorList, String xmlTagName) {
        StringBuffer sb = new StringBuffer();
        sb.append("<current>");
        sb.append("<term>");
        sb.append("<" + xmlTagName + ">");
        int size = vectorList.size();
        for (int k = 0; k < size; k++) {
            sb.append(escapeXML(vectorList.get(k).toString()));
            if (size > 1 && k < size - 1) {
                sb.append(",");
            }
        }
        sb.append("</" + xmlTagName + ">");
        sb.append("</term>");
        sb.append("</current>");

        return sb.toString();
    }

    public String setRenameSuccessResultInXml(String RenameResult) {

        StringBuffer sb = new StringBuffer();
        sb.append("<currentRename>");
        sb.append("<termError>");
        sb.append("<apotelesma>");
        sb.append(RenameResult);
        sb.append("</apotelesma>");
        sb.append("</termError>");
        sb.append("</currentRename>");

        return sb.toString();
    }

    public String RenameXml(String term) {

        StringBuffer sb = new StringBuffer();
        sb.append("<currentRename>");
        sb.append("<term>");
        sb.append("<name>");
        sb.append(escapeXML(term));
        sb.append("</name>");
        sb.append("</term>");
        sb.append("</currentRename>");

        return sb.toString();
    }
    /* RenameXml()
     * Puts Rename facet parameters in XML representation
     * to be used from rename_facet.xsl*/

    public String RenameXml_Facet(String facet) {

        StringBuffer sb = new StringBuffer();
        sb.append("<currentRename>");
        sb.append("<facet>");
        sb.append("<name>");
        sb.append(escapeXML(facet));
        sb.append("</name>");
        sb.append("</facet>");
        sb.append("</currentRename>");

        return sb.toString();
    }

    /*---------------------------------------------------------------------
    getCreateOrModifyERRORInXml()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String ErrorResult: the message to be written
    OUTPUT: a String with the XML representation of the result of the
    creation or modification of a Descriptor
    CALLED BY: Create_Modify servlet
    ----------------------------------------------------------------------*/
    public String getCreateOrModifyERRORInXml(String createORmodify, String ErrorResult) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "currentcreate";
        } else {
            startTag = "currentmodify";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<termError>";
        xmlString += "<apotelesma>" + ErrorResult + "</apotelesma>";
        xmlString += "</termError>";
        xmlString += "</" + startTag + ">";

        return xmlString;
    }

    /*---------------------------------------------------------------------
    getCreateOrModifySameValueField()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String targetDescriptor etc: the data to be written in XML
    OUTPUT: a String with the XML representation of the given data
    CALLED BY: Create_Modify servlet, in case of error happened, in order
    to refill the input items with given values
    ----------------------------------------------------------------------*/
    public String getCreateOrModifySameValueField(String createORmodify, String targetDescriptor, String bt, String uf,
            String uk_alt, String uk_uf, String tc, String historical_note, String et) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "createcurrent";
        } else {
            startTag = "current";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<term>";
        xmlString += "<name>" + escapeXML(targetDescriptor) + "</name>";
        xmlString += "<bt>" + escapeXML(bt) + "</bt>";
        xmlString += "<uf>" + escapeXML(uf) + "</uf>";
        xmlString += "<uk_alt>" + escapeXML(uk_alt) + "</uk_alt>";
        xmlString += "<uk_uf>" + escapeXML(uk_uf) + "</uk_uf>";
        xmlString += "<tc>" + escapeXML(tc) + "</tc>";
        xmlString += "<historical_note>" + escapeXML(historical_note) + "</historical_note>";
        xmlString += "<et>" + escapeXML(et) + "</et>";
        xmlString += "</term>";
        xmlString += "</" + startTag + ">";

        return xmlString;
    }

    /*---------------------------------------------------------------------
    getCreateOrModifySameValueField()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String targetFacet: the data to be written in XML
    OUTPUT: a String with the XML representation of the given data
    CALLED BY: Create_Modify_Hierarchy servlet, in case of error happened, in order
    to refill the input items with given values
    ----------------------------------------------------------------------*/
    public String getCreateOrModifySameValueField_Hierarchy(String createORmodify, String targetHierarchy, Vector targetFacets) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "currentcreatehierarchy";
        } else {
            startTag = "current";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<hierarchyName>";
        xmlString += "<name>" + escapeXML(targetHierarchy) + "</name>";
        xmlString += "</hierarchyName>";

        for (int i = 0; i < targetFacets.size(); i++) {
            xmlString += "<hierarchyFacetName>";
            xmlString += "<name>" + escapeXML(targetFacets.get(i).toString()) + "</name>";
            xmlString += "</hierarchyFacetName>";
        }


        xmlString += "</" + startTag + ">";

        return xmlString;
    }
    /*---------------------------------------------------------------------
    getCreateOrModifySameValueField()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String targetFacet: the data to be written in XML
    OUTPUT: a String with the XML representation of the given data
    CALLED BY: Create_Modify_Facet servlet, in case of error happened, in order
    to refill the input items with given values
    ----------------------------------------------------------------------*/

    public String getCreateOrModifySameValueField_Facet(String createORmodify, String targetFacet) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "currentcreatefacet";
        } else {
            startTag = "current";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<facet>";
        xmlString += "<name>" + escapeXML(targetFacet) + "</name>";
        xmlString += "</facet>";
        xmlString += "</" + startTag + ">";

        return xmlString;
    }

    /**Returns the terms which criterion has as value, String value*/
    public Vector<String> getEquals(UserInfoClass SessionUserInfo, String criterion, String value, String[] links, QClass Q, IntegerObject sis_session, DBGeneral dbGen) {
        Vector<String> v = new Vector<String>();

        if (criterion.equals("name")) {
            v.add(value);
        } else if (criterion.equals(ConstantParameters.bt_kwd)) {
            v.addAll(dbGen.getBT_NT(SessionUserInfo, value, ConstantParameters.NT_DIRECTION, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.nt_kwd)) {
            v.addAll(dbGen.getBT_NT(SessionUserInfo, value, ConstantParameters.BT_DIRECTION, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.rt_kwd)) {
            v.addAll(dbGen.getRTlinksBothDirections(SessionUserInfo, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.uf_kwd)) {
            v.addAll(dbGen.getLink(SessionUserInfo.selectedThesaurus, value, ConstantParameters.uf_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.dn_kwd)) {
            v.addAll(dbGen.getLink(SessionUserInfo.selectedThesaurus, value, ConstantParameters.dn_kwd, Q, sis_session));
            /*} else if (criterion.equals(ConstantParameters.uk_alt_kwd)) {
            v.addAll(dbGen.getLink(sessionInstance, value, ConstantParameters.uk_alt_kwd, Q, sis_session));*/
        } else if (criterion.equals(ConstantParameters.translations_found_in_kwd)) {
            v.addAll(dbGen.getLink(SessionUserInfo.selectedThesaurus, value, ConstantParameters.translations_found_in_kwd, Q, sis_session));
            /*} else if (criterion.equals(ConstantParameters.bt_found_in_kwd)) {
            v.addAll(dbGen.getLink(sessionInstance, value, ConstantParameters.bt_found_in_kwd, Q, sis_session));*/
        } else if (criterion.equals(ConstantParameters.uf_translations_kwd)) {
            v.addAll(dbGen.getLink(SessionUserInfo.selectedThesaurus, value, ConstantParameters.uf_translations_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.primary_found_in_kwd)) {
            v.addAll(dbGen.getLink(SessionUserInfo.selectedThesaurus, value, ConstantParameters.primary_found_in_kwd, Q, sis_session));
        }
        return v;
    }

    /**Returns the terms which criterion contains String value (*value*)
    public Vector<String> getContains(THEMASUserInfo SessionUserInfo, String criterion, String value, String[] links, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen) {
        Vector<String> v = new Vector<String>();

        if (criterion.equals("name")) {
            v.addAll(dbGen.getMatchedByName(SessionUserInfo.selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.bt_kwd)) {
            v.addAll(dbGen.getMatchedByBT(SessionUserInfo.selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.nt_kwd)) {
            v.addAll(dbGen.getMatchedByNT(SessionUserInfo.selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.rt_kwd)) {
            v.addAll(dbGen.getMatchedByRT(SessionUserInfo, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.uf_kwd)) {
            v.addAll(dbGen.getMatched(SessionUserInfo.selectedThesaurus, value, ConstantParameters.uf_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.dn_kwd)) {
            v.addAll(dbGen.getMatched(SessionUserInfo.selectedThesaurus, value, links[5], Q, sis_session));
            //} else if (criterion.equals("uk_alt")) {
            //v.addAll(dbGen.getMatched(sessionInstance, value, "uk_alt", Q, sis_session));
        } else if (criterion.equals(ConstantParameters.translations_found_in_kwd)) {
            v.addAll(dbGen.getMatched(SessionUserInfo.selectedThesaurus, value, ConstantParameters.translations_found_in_kwd, Q, sis_session));
            //} else if (criterion.equals("bt_found_in")) {
            //v.addAll(dbGen.getMatched(sessionInstance, value, "bt_found_in", Q, sis_session));
        } else if (criterion.equals(ConstantParameters.uf_translations_kwd)) {
            v.addAll(dbGen.getMatched(SessionUserInfo.selectedThesaurus, value, ConstantParameters.uf_translations_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.primary_found_in_kwd)) {
            v.addAll(dbGen.getMatched(SessionUserInfo.selectedThesaurus, value, ConstantParameters.primary_found_in_kwd, Q, sis_session));
        }
        return v;
    }
    */

    /**Returns the terms which criterion isn't equal with value*/
    public Vector<String> getNotEquals(String selectedThesaurus, String criterion, String value, String[] links, QClass Q, IntegerObject sis_session, DBGeneral dbGen) {
        /*HINT: Prwta 8a elegxoume an autes oi times uparxoun sto sunolo ws exei ws twra k 8a ta bgazoume apo kei.. *-) */
        Vector<String> v = new Vector<String>();

        if (criterion.equals("name")) {
            v.addAll(dbGen.getNotEqualsByName(selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.bt_kwd)) {
            v.addAll(dbGen.getNotEqualsByBT(selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.nt_kwd)) {
            v.addAll(dbGen.getNotEqualsByNT(selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.rt_kwd)) {
            v.addAll(dbGen.getNotEqualsByRT(selectedThesaurus, value, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.uf_kwd)) {
            v.addAll(dbGen.getNotEquals(selectedThesaurus, value, ConstantParameters.uf_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.dn_kwd)) {
            v.addAll(dbGen.getNotEquals(selectedThesaurus, value, links[5], Q, sis_session));
            /*} else if (criterion.equals("uk_alt")) {
            v.addAll(dbGen.getNotEquals(sessionInstance, value, "uk_alt", Q, sis_session));*/
        } else if (criterion.equals(ConstantParameters.translations_found_in_kwd)) {
            v.addAll(dbGen.getNotEquals(selectedThesaurus, value, ConstantParameters.translations_found_in_kwd, Q, sis_session));
            /*} else if (criterion.equals("bt_found_in")) {
            v.addAll(dbGen.getNotEquals(sessionInstance, value, "bt_found_in", Q, sis_session));*/
        } else if (criterion.equals(ConstantParameters.uf_translations_kwd)) {
            v.addAll(dbGen.getNotEquals(selectedThesaurus, value, ConstantParameters.uf_translations_kwd, Q, sis_session));
        } else if (criterion.equals(ConstantParameters.primary_found_in_kwd)) {
            v.addAll(dbGen.getNotEquals(selectedThesaurus, value, ConstantParameters.primary_found_in_kwd, Q, sis_session));
        }
        return v;
    }

    //flag = checkTerm(criteria, operators, values, sis_session, terms.get(i));
    /*
    public boolean checkTerm(UserInfoClass SessionUserInfo, String[] criteria, String[] operators, String[] values, String term, String[] links, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen) {

        Vector<String> v = new Vector<String>();

        for (int i = 0; i < criteria.length; i++) {
            if (operators[i].equals("=")) {
                v = getEquals(SessionUserInfo, criteria[i], values[i], links, Q, sis_session, dbGen);
                if (!v.contains(term)) {
                    return false;
                }
                v.removeAllElements();
            } else if (operators[i].equals("~")) {
                v = getContains(SessionUserInfo, criteria[i], values[i], links, Q, TA, sis_session, dbGen);
                if (!v.contains(term)) {
                    return false;
                }
                v.removeAllElements();
            } else {
                //public Vector getNotEquals(String criterion, String value, IntegerObject sis_session, String[] links){
                v = getNotEquals(SessionUserInfo.selectedThesaurus, criteria[i], values[i], links, Q, sis_session, dbGen);
                if (!v.contains(term)) {
                    return false;
                }
                v.removeAllElements();
            }
        }

        return true;
    }
    

    public Vector checkTerms(UserInfoClass SessionUserInfo, String[] criteria, String[] operators, String[] values,
            Vector terms, String[] links, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen) {

        boolean flag = false;

        for (int i = 0; i < terms.size(); i++) {
            flag = checkTerm(SessionUserInfo, criteria, operators, values, terms.get(i).toString(), links, Q, TA, sis_session, dbGen);
            if (flag == false) {
                terms.removeElementAt(i);
            }
        }

        return terms;
    }
*/
    /**Returns a Vector with the terms which match with criteria
    public Vector getAllTerms(UserInfoClass SessionUserInfo, String[] criteria, String[] operators, String[] values, String op, String[] links,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale, DBGeneral dbGen) {

        Vector<String> terms = new Vector<String>();

        //retainAll -> AND periptwsh -> lambanoume up'opsh mono ta koina

        
        // Case Where All are requested        
        if (op.equalsIgnoreCase("*")) {
            terms.addAll(dbGen.getDescriptors(SessionUserInfo.selectedThesaurus, Q, sis_session, targetLocale));
            return terms;
        }
        if (op.equalsIgnoreCase("and")) {
            
            //if(operators[0].equals("="))
            //terms.addAll(getEquals(criteria[0], values[0], sis_session, links));
            //else if(operators[0].equals("~"))
            //terms.addAll(getContains(criteria[0], values[0], sis_session, links));
            //else
            //terms.addAll(getNotEquals(criteria[0], values[0], sis_session, links));
            
            //int i=1;
            //while(!terms.isEmpty()){
            //TODO
            //pare to sunolo terms kai elegkse an o ka8e oros plhrei ta 1-size() krithria
            //}			
            //return terms;
             

            boolean start = false;

            //case: =
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("=")) {
                    if (start == false) {
                        terms.addAll(getEquals(SessionUserInfo, criteria[i], values[i], links, Q, sis_session, dbGen));
                        start = true;
                    } else//start == true
                    if (terms.isEmpty()) {
                        return terms;
                    } else {
                        return checkTerms(SessionUserInfo, criteria, operators, values, terms, links, Q, TA, sis_session, dbGen);//checkSet & return
                    }
                }
            }
            //case: ~
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("~")) {
                    if (start == false) {
                        terms.addAll(getContains(SessionUserInfo, criteria[i], values[i], links, Q, TA, sis_session, dbGen));
                        start = true;
                    } else if (terms.isEmpty()) {
                        return terms;
                    } else {
                        return checkTerms(SessionUserInfo, criteria, operators, values, terms, links, Q, TA, sis_session, dbGen);//checkSet & return
                    }
                }
            }
            //case:!=
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("!")) {
                    if (start == false) {
                        terms.addAll(getNotEquals(SessionUserInfo.selectedThesaurus, criteria[i], values[i], links, Q, sis_session, dbGen));
                        start = true;
                    } else if (terms.isEmpty()) {
                        return terms;
                    } else {
                        return checkTerms(SessionUserInfo, criteria, operators, values, terms, links, Q, TA, sis_session, dbGen);//checkSet & return
                    }
                }
            }

            return terms;
            
            // AND
            //
            // Diabazw tous operators kai psaxnw opou einai =
            // 		An einai to prwto crithrio pou eksetazw (start = false)
            // 			Set = makeNewSet();
            // 		Alliws
            // 			Gia ka8e oro pou uparxei sto Set
            // 				checkSet(...);
            // Diabazw tous operators kai psaxnw opou einai ~
            // 		An einai to prwto crithrio pou eksetazw (start = false)
            // 			Set = makeNewSet();
            // 		Alliws
            // 			Gia ka8e oro pou uparxei sto Set
            // 				checkSet(...);
            // Diabazw tous operators kai psaxnw opou einai !=
            // 		An einai to prwto crithrio pou eksetazw (start = false)
            // 			Set = makeNewSet();
            // 		Alliws
            // 			Gia ka8e oro pou uparxei sto Set
            // 				checkSet(...);
            //
        }//if op == AND
        //addAll -> OR periptwsh -> ta lambanoume ola up'opsh
        else {
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("=")) {
                    terms.addAll(getEquals(SessionUserInfo, criteria[i], values[i], links, Q, sis_session, dbGen));
                }
            }
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("~")) {
                    terms.addAll(getContains(SessionUserInfo, criteria[i], values[i], links, Q, TA, sis_session, dbGen));
                }
            }
            for (int i = 0; i < operators.length; i++) {
                if (operators[i].equals("!")) {
                    terms.addAll(getNotEquals(SessionUserInfo.selectedThesaurus, criteria[i], values[i], links, Q, sis_session, dbGen));
                }
            }
            Collections.sort(terms);
            //terms.add("GETALLTERMS: OR");
            return terms;
        }//else if op == OR

    }
*/
    /*---------------------------------------------------------------------
    xslTransform()
    -----------------------------------------------------------------------
    INPUT: - PrintWriter out: the writer to be used
    - StringBuffer xml: the XML StringBuffer to be parsed
    - String xslFileName: the full path of the XSL file to be used for the transormation
    FUNCTION: writes to the given writer the given XML StringBuffer tranformed 
    with the given XSL file
    ----------------------------------------------------------------------*/
    public void abandoned_xslTransform(PrintWriter out, StringBuffer xml, String xslFileName) {
        /*
        
        try {
            XMLTransform xmlD = new XMLTransform(xml.toString());
            xmlD.transform(out, xslFileName);


        } catch (DMSException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "xslTransform Failed for " + xslFileName + " : " + ex.getMessage());
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Input Xml had as follows: " + xml.toString());
            Utils.StaticClass.handleException(ex);
            if (Parameters.DEBUG) {
                Utils.StaticClass.webAppSystemOutPrintln(xml.toString());
            }
        }
        if (Parameters.DEBUG) {
            Utils.StaticClass.webAppSystemOutPrintln(xml.toString());
        }
        */
        XmlPrintWriterTransform(out, xml,xslFileName);
        Utils.StaticClass.webAppSystemOutPrintln("xslTransform WITHOUT DMS");
        out.flush();
    }

    /*---------------------------------------------------------------------
    xslTransform_OLD() - FREEZED
    -----------------------------------------------------------------------
    FREEZED by karam (6/2/2008) because it was working ONLY for encoding "windows-1253" and NOT for UTF-8 !!!
    REPLACED by the above, taken from isl.dms.xml.XMLTransform package of DMS1.3.jar
    ----------------------------------------------------------------------*/
    public void xslTransform_OLD(PrintWriter out, String xml, String xslFileName) {
        try {
            StreamSource xslSource = new StreamSource(xslFileName);
            StreamSource xmlSource = new StreamSource(new ByteArrayInputStream(xml.getBytes()));

            TransformerFactory tFactory = TransformerFactory.newInstance();
            //added for faster results
            Templates cachedXSLT = tFactory.newTemplates(xslSource);
            Transformer transformer = cachedXSLT.newTransformer();
            transformer.transform(xmlSource, new StreamResult(out));
        } catch (TransformerException e) {
            System.err.println("Error: " + e);
            Utils.StaticClass.handleException(e);
        }
    }

    /*---------------------------------------------------------------------
    getFormParams()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the request of the current servlet
    OUTPUT: - Hashtable paramsHashtable: a Hashtable with the parameters of the current servlet
    FUNCTION: gets to a Hashtable the parameters of the current servlet
    CALLED BY: all servlets in order to get their parameters
    ----------------------------------------------------------------------*/
    public Hashtable<String, Object> getFormParams(HttpServletRequest request) {

        Hashtable<String, Object> paramsHashtable = new Hashtable<String, Object>();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                paramsHashtable.put(paramName, paramValue);

            } else {
                //params.put(paramName, paramValues);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < paramValues.length; i++) {
                    sb.append(paramValues[i]);
                    if (i < paramValues.length - 1) {
                        sb.append(",");
                    }
                }
                paramsHashtable.put(paramName, sb.toString());
            }
        }
        return paramsHashtable;
    }

    /*---------------------------------------------------------------------
    getCreateOrModifyERRORInXml_Hierarchy()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String ErrorResult: the message to be written
    OUTPUT: a String with the XML representation of the result of the
    creation or modification of a Hierarchy
    CALLED BY: Create_Modify_Hierarchy servlet
    ----------------------------------------------------------------------*/
    public String getCreateOrModifyERRORInXml_Hierarchy(String createORmodify, String ErrorResult) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "currentcreatehierarchy";
        } else {
            startTag = "currentmodifyhierarchy";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<hierarchyError>";
        xmlString += "<apotelesma>" + ErrorResult + "</apotelesma>";
        xmlString += "</hierarchyError>";
        xmlString += "</" + startTag + ">";

        return xmlString;
    }

    /*---------------------------------------------------------------------
    getCreateOrModifyERRORInXml()
    -----------------------------------------------------------------------
    INPUT: - String createORmodify: "create" or "modify"
    - String ErrorResult: the message to be written
    OUTPUT: a String with the XML representation of the result of the
    creation or modification of a Facet
    CALLED BY: Create_Modify_Facet servlet
    ----------------------------------------------------------------------*/
    public String getCreateOrModifyERRORInXml_Facet(String createORmodify, String ErrorResult) {
        String startTag = "";
        if (createORmodify.equals("create")) {
            startTag = "currentcreatefacet";
        } else {
            startTag = "currentmodifyfacet";
        }
        String xmlString = "";
        xmlString += "<" + startTag + ">";
        xmlString += "<facetError>";
        xmlString += "<apotelesma>" + ErrorResult + "</apotelesma>";
        xmlString += "</facetError>";
        xmlString += "</" + startTag + ">";

        return xmlString;
    }

    public static String GetNow() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss:S";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);


        String temp = sdf.format(cal.getTime());
        temp = temp.replaceAll(":", "-");
        temp = temp.replaceAll(" ", "_");
        return temp;
    }

    public String getDecodedParameterValue(String encodedParam) throws java.io.UnsupportedEncodingException {

        String param_decoded = null;
        if (encodedParam != null) {
            param_decoded = URLDecoder.decode(encodedParam, "UTF-8").trim();
            //\r\n are needed for translations scope notes
            //param_decoded = param_decoded.replaceAll("\r\n", " ");
            //param_decoded = param_decoded.replaceAll("\n", " ");
            //param_decoded = param_decoded.replaceAll("\r", " ");
            param_decoded = param_decoded.replaceAll(" +", " ");
            
            param_decoded = param_decoded.trim();
        }

        return param_decoded;
    }

    public Vector<String> getDecodedParameterValues(String[] encodedParams) throws java.io.UnsupportedEncodingException {

        Vector<String> returnParams_decoded = new Vector<String>();
        if (encodedParams != null) {
            for (int i = 0; i < encodedParams.length; i++) {
                String param_decoded = URLDecoder.decode(encodedParams[i], "UTF-8").trim();
                //\r\n are needed for translations scope notes
                //param_decoded = param_decoded.replaceAll("\r\n", " ");
                //param_decoded = param_decoded.replaceAll("\n", " ");
                //param_decoded = param_decoded.replaceAll("\r", " ");
                param_decoded = param_decoded.replaceAll(" +", " ");
                param_decoded = param_decoded.trim();
                returnParams_decoded.add(param_decoded);
            }
        }

        return returnParams_decoded;
    }

    public String GetNowNodeName() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd";
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

        Date d = cal.getTime();
        String dateStr = sdf.format(d);
        return dateStr;
        /*
        String[] dateParts = dateStr.split("-");
        String Day = dateParts[2];
        String Month = dateParts[1];
        int monthInt = Integer.parseInt(Month);
        
        switch(monthInt){
        case 1: {

        Month = "Ιανουάριος";
        break;
        }
        case 2: {

        Month = "Φεβρουάριος";
        break;
        }
        case 3: {

        Month = "Μάρτιος";
        break;
        }
        case 4: {

        Month = "Απρίλιος";
        break;
        }
        case 5: {

        Month = "Μάιος";
        break;
        }
        case 6: {

        Month = "Ιούνιος";
        break;
        }
        case 7: {

        Month = "Ιούλιος";
        break;
        }
        case 8: {
        Month = "Αύγουστος";
        break;
        }
        case 9: {

        Month = "Σεπτέμβριος";
        break;
        }
        case 10: {

        Month = "Οκτώβριος";
        break;
        }
        case 11: {

        Month = "Νοέμβριος";
        break;
        }
        case 12: {

        Month = "Δεκέμβριος";
        break;
        }
        default: {
        break;
        }
        }

        
        String Year = dateParts[0];
        String temp = Year + " " + Month + " " + Day;
        return temp;*/
    }

    //Turn a comma seperated String to Vector --> No dublicates
    public Vector<String> get_Vector_from_String(String str, String delimeter) {

        Vector<String> result = new Vector<String>();

        if (str == null) {
            return result;
        }

        String[] strArray = str.split(delimeter);

        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i].trim().length() == 0) {
                continue;
            }

            if (!result.contains(strArray[i].trim())) {
                result.add(strArray[i].trim());
            }

        }

        result.trimToSize();

        return result;

    }

    public static String escapeXML(String target) {
        /*
        &lt; < less than 
        &gt; > greater than 
        &amp; & ampersand  
        &apos; ' apostrophe 
        &quot; " quotation mark 
         */
        if (target == null) {
            return "";
        }
        String returnVal = target;
        returnVal = returnVal.replaceAll("\\\\", "\\\\");
        returnVal = returnVal.replaceAll("\u0013", "-");
        returnVal = returnVal.replaceAll("&", "&amp;");
        returnVal = returnVal.replaceAll("<", "&lt;");
        returnVal = returnVal.replaceAll(">", "&gt;");
        returnVal = returnVal.replaceAll("'", "&apos;");
        returnVal = returnVal.replaceAll("\"", "&quot;");
        returnVal = returnVal.replaceAll(" +", " ");


        /*
        Me to parakato xalasan ola
        try {
            returnVal = new String(returnVal.getBytes("UTF8"));
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return returnVal;
    }

    /*---------------------------------------------------------------------
    InformSearchOperatorsAndValuesWithSpecialCharacters()
    -----------------------------------------------------------------------
    INPUT: - String[] searchOperators with values for example "=", "~", "~*", "*~", "!", "!~", "!~*", "!*~"
    - String[] searchInputValues with the search patterns
    FUNCTION: - replaces each element of searchOperators[] having value "~*", "*~", "!~*", "!*~" ===> with value "~" (contains)
    - and adds to the corresponding searchInputValues[] elements the special character "*" appropriately
    This is done so as the user don't have to type this special character
    CALLED BY: SearchResults_Terms, SearchResults_Hierarchies and SearchResults_Facets servlets
    ----------------------------------------------------------------------*/
    public void InformSearchOperatorsAndValuesWithSpecialCharacters(String[] searchOperators, String[] searchInputValues) {
        int len = searchOperators.length;
        for (int i = 0; i < len; i++) {
            if (searchOperators[i].compareTo("~*") == 0) { // starts with
                searchOperators[i] = "~";
                searchInputValues[i] = searchInputValues[i] + "*";
            } else if (searchOperators[i].compareTo("*~") == 0) { // ends with
                searchOperators[i] = "~";
                searchInputValues[i] = "*" + searchInputValues[i];
            } else if (searchOperators[i].compareTo("!~*") == 0) { // not starts with
                searchOperators[i] = "!~";
                searchInputValues[i] = searchInputValues[i] + "*";
            } else if (searchOperators[i].compareTo("!*~") == 0) { // not ends with
                searchOperators[i] = "!~";
                searchInputValues[i] = "*" + searchInputValues[i];
            }
        }
    }
    
    public void XmlPrintWriterTransform(PrintWriter outputStream, StringBuffer xml, String xslPath) {


        /*
        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(outputFullPath);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        */
        
        //DocumentBuilderFactory docBuilderF = DocumentBuilderFactory.newInstance();
        //DocumentBuilder docBuilder = docBuilderF.newDocumentBuilder();
        
        //Element XMLElem = docBuilder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml.toString()))).getDocumentElement();
        
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file

            //Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslPath)));
            Templates template = factory.newTemplates(new StreamSource(xslPath));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files

            //File xmlFile = new File(xmlInputPath);

            StreamSource xmlSource = new StreamSource(new StringReader(xml.toString()));
            
            //Source source = new StreamSource(xmlFile);
            Result result = new StreamResult(outputStream);

            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, result);

            outputStream.flush();

        //} catch (FileNotFoundException e) {
          //  Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "1=" + e.getMessage());
            //Utils.StaticClass.handleException(e);
        } catch (TransformerConfigurationException e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "2=" + e.getMessage());
            Utils.StaticClass.handleException(e);
            // An error occurred in the XSL file
        } catch (TransformerException e) {
            
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "3=" + e.getMessage());
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessageAndLocation());
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getLocationAsString());

            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();

            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "LINE=" + line + " COL=" + col);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);

        }catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "1=" + ex.getClass() +" " +ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }

        /*
        try {
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        */
    }

    public void XmlFileTransform(String xmlInputPath, String xslPath, String outputFullPath) {


        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(outputFullPath);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }


        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file

            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslPath)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files

            File xmlFile = new File(xmlInputPath);

            Source source = new StreamSource(xmlFile);
            Result result = new StreamResult(out);

            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(source, result);


        } catch (FileNotFoundException e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "1=" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } catch (TransformerConfigurationException e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "2=" + e.getMessage());
            Utils.StaticClass.handleException(e);
            // An error occurred in the XSL file
        } catch (TransformerException e) {
            
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "3=" + e.getMessage());
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessageAndLocation());
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getLocationAsString());

            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();

            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "LINE=" + line + " COL=" + col);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);

        }

        try {
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }

    public String getBTNTWithGuideTermsResultsInXml(String term, String attribute, String GuideTermPrefix, Vector<SortItem> btsORnts, Vector<String> existingGuideTermsVec, Locale targetLocale) {

        StringBuffer sb = new StringBuffer();
        sb.append("<current>");
        sb.append("<term>");
        sb.append("<name>" + escapeXML(term) + "</name>");


        if (!btsORnts.isEmpty()) {
            Collections.sort(btsORnts, new SortItemLocaleComparator(targetLocale));
            sb.append("<" + attribute + ">");
            for (int k = 0; k < btsORnts.size(); k++) {
                sb.append("<name linkClass=\"" + btsORnts.get(k).getLinkClass().replaceFirst(GuideTermPrefix, "") + "\">");
                sb.append(escapeXML(btsORnts.get(k).getLogName()));
                sb.append("</name>");
            }
            sb.append("</" + attribute + ">");
        }
        sb.append("</term>");
        sb.append("</current>");


        sb.append("<GuideTerms>");
        for (int i = 0; i < existingGuideTermsVec.size(); i++) {
            sb.append("<GuideTerm>" + existingGuideTermsVec.get(i) + "</GuideTerm>");
        }
        sb.append("</GuideTerms>");

        return sb.toString();
    }
    /*---------------------------------------------------------------------
    getTermResultsInXml()
    -----------------------------------------------------------------------
    INPUT: - String term: the the term to be queried
    - String[] output: the properties of each term to be collected
    - String for_deletion: "true" in case the term is to be deleted 
    (delete icon was pressed from Search results TAB), null otherwise
    OUTPUT: a String with the XML representation of the results
    CALLED BY: servlets: ViewInfo with output = {"name", ConstantParameters.bt_kwd, ConstantParameters.nt_kwd, ConstantParameters.rt_kwd, ConstantParameters.uf_kwd,  "uk_alt", "uf_translations", ConstantParameters.dn_kwd, "comment", "et"}
    ----------------------------------------------------------------------*/

    public String getTermResultsInXml(UserInfoClass SessionUserInfo, String term, String[] output, QClass Q, TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();

        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);

        Vector<String> v = new Vector<String>();
        StringBuffer sb = new StringBuffer();
        sb.append("<current translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");
        sb.append("<term>");

        // karam - add the information about the term if it is to be deleted
        // (delete icon was pressed from Search results TAB)
        /*sb.append("<for_deletion>");
        if (for_deletion != null) {
        sb.append("true");
        }
        sb.append("</for_deletion>");
         */
        // karam - add the information about the term if it is a new or a released descriptor
        /*sb.append("<is_released>");
        // looking for Descriptor prefix (EL`)
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Descriptor(Q,sis_session.getValue());
        // convert target Descriptor to DB encoding with prefix
        StringObject targetDescriptorObj = new StringObject(prefix.concat(term));
        boolean isReleased = dbGen.IsReleasedDescriptor(targetDescriptorObj,Q,sis_session);
        if (isReleased == true) {
        sb.append("true");
        } else {
        sb.append("false");
        }
        sb.append("</is_released>");
        // karam - in case of released descriptor, add the information about the term if it is obsolete or not
        sb.append("<is_obsolete>");
        if (isReleased == true) {
        // looking for EKTObsoleteDescriptor
        StringObject thesObsoleteDescriptor = new StringObject();
        dbtr.getThesaurusClass_ObsoleteDescriptor(Q,sis_session.getValue(),thesObsoleteDescriptor);
        boolean isObsolete = dbGen.NodeBelongsToClass(targetDescriptorObj, thesObsoleteDescriptor, false,Q,sis_session);
        if (isObsolete == true) {
        sb.append("true");
        } else {
        sb.append("false");
        }
        }
        sb.append("</is_obsolete>");
         */
        // for each value of output : e.g. some of  {"name", "translations",  ConstantParameters.dn_kwd, ConstantParameters.bt_kwd, ConstantParameters.nt_kwd, ConstantParameters.rt_kwd, ConstantParameters.uf_kwd, "uf_translations", "alt",
        //"uk_alt",  "gt",  "et", "created", "created_by", "modified",  "modified_by", "scope_note"}*/

        for (int j = 0; j < output.length; j++) {

            if (output[j].equals("name")) {
                sb.append("<name>" + escapeXML(term) + "</name>");
            } else if (output[j].equals(ConstantParameters.translation_kwd) || output[j].equals(ConstantParameters.uf_translations_kwd)) {

                Vector<SortItem> vtranslations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, output[j].equals(ConstantParameters.translation_kwd), term, Q, sis_session);
                Collections.sort(vtranslations, new GuideTermSortItemComparator(targetLocale));
                sb.append("<" + output[j] + ">");
                for (int k = 0; k < vtranslations.size(); k++) {
                    sb.append("<name linkClass=\"" + vtranslations.get(k).linkClass + "\">");
                    sb.append(escapeXML(vtranslations.get(k).log_name));
                    sb.append("</name>");
                }
                sb.append("</" + output[j] + ">");

            } else if (output[j].equals(ConstantParameters.translations_scope_note_kwd)) {

                v.clear();
                v.addAll(dbGen.returnResults(SessionUserInfo, term, output[j], Q, TA, sis_session));
                if (v.size() > 0) {
                    String translationsScopeNoteStr = v.get(0).toString();

                    if (translationsScopeNoteStr != null && translationsScopeNoteStr.length() > 0) {
                        Hashtable<String, String> trs = getTranslationScopeNotes(translationsScopeNoteStr);
                        Vector<String> trSns = new Vector<String>(trs.keySet());
                        Collections.sort(trSns);
                        for (int k = 0; k < trSns.size(); k++) {
                            String writeVal = trSns.get(k);
                            String val = trs.get(writeVal);

                            if (val != null && val.trim().length() > 0) {
                                sb.append("<" + output[j] + ">");
                                sb.append("<name lang=\"" + writeVal + "\">");
                                sb.append(escapeXML(val));
                                sb.append("</name>");
                                sb.append("</" + output[j] + ">");
                            }
                        }
                    }
                }


            } else {
                v.clear();
                v.addAll(dbGen.returnResults(SessionUserInfo, term, output[j], Q, TA, sis_session));

                Collections.sort(v, strCompar);

                if (!v.isEmpty()) {
                    //Collections.sort(v, strCompar);
                    sb.append("<" + output[j] + ">");
                    for (int k = 0; k < v.size(); k++) {

                        sb.append("<name>");
                        sb.append(escapeXML(v.get(k).toString()));
                        sb.append("</name>");

                        /*if (v.size() > 1 && k < v.size() - 1) {
                        sb.append(",");
                        }*/
                    }
                    sb.append("</" + output[j] + ">");
                }
            }
            v.clear();
        }
        sb.append("</term>");
        sb.append("</current>");

        return sb.toString();
    }

    public Hashtable<String, String> getTranslationScopeNotes(String scopeNoteValStr) {
        String scopeNoteVal = scopeNoteValStr;
        Hashtable<String, String> returnVals = new Hashtable<String, String>();

        scopeNoteVal = scopeNoteVal.replaceAll("\t", " ");
        scopeNoteVal = scopeNoteVal.replaceAll(" +", " ");
        scopeNoteVal = scopeNoteVal.replaceAll("\r\n", "\n");
        scopeNoteVal = scopeNoteVal.replaceAll(" \n", "\n");
        scopeNoteVal = scopeNoteVal.replaceAll("\n ", "\n");


        String[] parts = scopeNoteVal.split("\n");

        if (parts != null) {
            String langCode = "";
            String value = "";
            for (int p = 0; p < parts.length; p++) {
                String partStr = parts[p];
                if (partStr.indexOf(Parameters.TRANSLATION_SEPERATOR) == 2) {
                    langCode = partStr.substring(0, 2);
                    value = partStr.substring(3).trim();
                }
                if (returnVals.containsKey(langCode) == false) {
                    returnVals.put(langCode, value);
                } else {
                    String existingVal = returnVals.get(langCode);
                    if (value.length() > 0 && existingVal.equals(value) == false) {
                        existingVal += value;
                        returnVals.put(langCode, existingVal);
                    }
                }
                /*
                if (partStr.matches("[A-Z]{2}" + Parameters.TRANSLATION_SEPERATOR)) {

                //check if langcode and value are not empty.
                if (langCode != null && langCode.length() > 0 && value != null && value.length() > 0) {
                if (returnVals.containsKey(langCode) == false) {
                returnVals.put(langCode, value);
                } else {
                String existingVal = returnVals.get(langCode);
                if (value.length() > 0 && existingVal.equals(value) == false) {
                existingVal += value;
                returnVals.put(langCode, existingVal);
                }
                }
                }
                langCode = partStr.replaceFirst(Parameters.TRANSLATION_SEPERATOR, "").toUpperCase();
                value = "";
                } else {
                if (value != null && value.length() > 0) {
                value += "\n" + partStr;
                } else {
                value = partStr;
                }
                }*/
            }

            //check if langcode and value are not empty.
            if (langCode != null && langCode.length() > 0 && value != null && value.length() > 0) {
                if (returnVals.containsKey(langCode) == false) {
                    returnVals.put(langCode, value);
                } else {
                    String existingVal = returnVals.get(langCode);
                    if (value.length() > 0 && existingVal.equals(value) == false) {
                        existingVal += value;
                        returnVals.put(langCode, existingVal);
                    }
                }
            }
        }

        return returnVals;
    }

    public String getHierarchyResultsInXml(UserInfoClass SessionUserInfo, String hierarchy, String[] output, String for_deletion, QClass Q, IntegerObject sis_session, DBGeneral dbGen, Locale targetLocale) {

        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);

        Vector<String> v = new Vector<String>();
        StringBuffer sb = new StringBuffer();
        sb.append("<current>");
        sb.append("<hierarchy>");

        // (delete icon was pressed from Search results TAB)
        sb.append("<for_deletion>");
        if (for_deletion != null) {
            sb.append("true");
        }
        sb.append("</for_deletion>");

        // for each value of output = {"name", "facets", "letter_code", "created", "created_by", "modified", "modified_by"}
        for (int j = 0; j < output.length; j++) {
            if (output[j].equals("name")) {
                sb.append("<name>" + Utilities.escapeXML(hierarchy) + "</name>");
            } else {

                if (output[j].equals("letter_code")) {
                    v.addAll(dbGen.returnResults_Hierarchy(SessionUserInfo, hierarchy, output[j], Q, sis_session, targetLocale));
                    if (!v.isEmpty()) {
                        Collections.sort(v, strCompar);
                        for (int k = 0; k < v.size(); k++) {
                            Vector<String> temp = new Vector<String>();
                            //temp.addAll((Vector) v.get(k)); //BUG??
                            temp.add(v.get(k));

                            sb.append("<" + output[j] + ">");

                            sb.append("<name>");
                            sb.append(Utilities.escapeXML(temp.get(0).toString()));
                            sb.append("</name>");
                            
                            sb.append("<editable>");
                            if(temp.size()>1){ //BUG?? this check did not exist
                                sb.append(Utilities.escapeXML(temp.get(1).toString()));
                            }
                            sb.append("</editable>");
                            

                            sb.append("</" + output[j] + ">");
                        }

                    }

                } else {
                    v.addAll(dbGen.returnResults_Hierarchy(SessionUserInfo, hierarchy, output[j], Q, sis_session, targetLocale));
                    if (!v.isEmpty()) {
                        Collections.sort(v, strCompar);
                        sb.append("<" + output[j] + ">");
                        for (int k = 0; k < v.size(); k++) {
                            sb.append("<name>");
                            sb.append(Utilities.escapeXML(v.get(k).toString()));
                            sb.append("</name>");
                        }
                        sb.append("</" + output[j] + ">");
                    }
                }
            }
            v.clear();
        }
        sb.append("</hierarchy>");
        sb.append("</current>");

        return sb.toString();
    }

    public void getAvailableValues(UserInfoClass SessionUserInfo, String output, QClass Q, IntegerObject sis_session, StringBuffer xml, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        if (output.matches(ConstantParameters.bt_kwd) || output.matches(ConstantParameters.nt_kwd) || output.matches(ConstantParameters.rt_kwd) || output.matches(ConstantParameters.term_create_kwd)) {
            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            Vector<String> termNames = dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session);
            Q.free_set(set_terms);

            Collections.sort(termNames, new StringLocaleComparator(targetLocale));
            xml.append("<availableTerms>");
            for (int i = 0; i < termNames.size(); i++) {
                xml.append("<name>");
                xml.append(Utilities.escapeXML(termNames.get(i)));
                xml.append("</name>");
            }
            xml.append("</availableTerms>");

        }

        if (output.matches(ConstantParameters.primary_found_in_kwd) || output.matches(ConstantParameters.translations_found_in_kwd)) {

            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.SourceClass));
            int set_sources = Q.get_all_instances(0);
            Q.reset_set(set_sources);
            Vector<String> sourceNames = dbGen.get_Node_Names_Of_Set(set_sources, true, Q, sis_session);
            Q.free_set(set_sources);
            Collections.sort(sourceNames, new StringLocaleComparator(targetLocale));
            xml.append("<availableSources>");
            for (int i = 0; i < sourceNames.size(); i++) {
                xml.append("<name>");
                xml.append(Utilities.escapeXML(sourceNames.get(i)));
                xml.append("</name>");
            }
            xml.append("</availableSources>");
        }

        if (output.matches(ConstantParameters.translation_kwd)) {

            /*
            Q.reset_name_scope();
            StringObject toTranslationsFromClass = new StringObject();
            StringObject toTranslationsLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, toTranslationsFromClass, toTranslationsLink, Q, sis_session);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            int set_translation_term_links = Q.get_link_from_by_category(set_terms, toTranslationsFromClass, toTranslationsLink);
            Q.reset_set(set_translation_term_links);


            Vector<SortItem> translationNames = dbGen.get_To_SortItems_Of_LinkSet(set_translation_term_links, true, true, true, Q, sis_session);

            Q.free_set(set_terms);
            Q.free_set(set_translation_term_links);


            Collections.sort(translationNames, new GuideTermSortItemComparator(targetLocale));
            xml.append("<availableTranslations>");
            for (int i = 0; i < translationNames.size(); i++) {
            SortItem currentSortItem = translationNames.get(i);
            xml.append("<name linkClass=\"" + currentSortItem.linkClass + "\">");
            xml.append(Utilities.escapeXML(currentSortItem.log_name));
            xml.append("</name>");
            }
            xml.append("</availableTranslations>");*/
        }

        if (output.matches(ConstantParameters.uf_translations_kwd)) {

            /*
            Q.reset_name_scope();
            StringObject ufTranslationsFromClass = new StringObject();
            StringObject ufTranslationsLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, ufTranslationsFromClass, ufTranslationsLink, Q, sis_session);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            int set_uftranslations_labels = Q.get_link_from_by_category(set_terms, ufTranslationsFromClass, ufTranslationsLink);
            Q.reset_set(set_uftranslations_labels);
            //
            //int set_uftranslations = Q.get_to_value(set_uftranslations_labels);
            //Q.reset_set(set_uftranslations);
            //
            //Vector<String> uftranslationsNames = dbGen.get_Node_Names_Of_Set(set_uftranslations, true, Q, sis_session);

            Vector<SortItem> uftranslationsNames = dbGen.get_To_SortItems_Of_LinkSet(set_uftranslations_labels, true, true, false, Q, sis_session);


            Q.free_set(set_terms);
            Q.free_set(set_uftranslations_labels);
            //Q.free_set(set_uftranslations);

            
            Collections.sort(uftranslationsNames, new GuideTermSortItemComparator(targetLocale));
            xml.append("<availableUfTranslations>");
            for (int i = 0; i < uftranslationsNames.size(); i++) {
            SortItem currentSortItem = uftranslationsNames.get(i);
            xml.append("<name linkClass=\"" + currentSortItem.linkClass + "\">");
            xml.append(Utilities.escapeXML(currentSortItem.log_name));
            xml.append("</name>");
            }
            xml.append("</availableUfTranslations>");
             * 
             */
        }

        if (output.matches(ConstantParameters.uf_kwd)) {

            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            StringObject UsedForTermClass = new StringObject();
            dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), UsedForTermClass);
            Q.reset_name_scope();

            Q.set_current_node(UsedForTermClass);
            int set_ufs = Q.get_all_instances(0);
            Q.reset_set(set_ufs);
            Vector<String> ufNames = dbGen.get_Node_Names_Of_Set(set_ufs, true, Q, sis_session);
            Q.free_set(set_ufs);
            Collections.sort(ufNames, new StringLocaleComparator(targetLocale));
            xml.append("<availableUfs>");
            for (int i = 0; i < ufNames.size(); i++) {
                xml.append("<name>");
                xml.append(Utilities.escapeXML(ufNames.get(i)));
                xml.append("</name>");
            }
            xml.append("</availableUfs>");
        }



    }

    public StringBuffer getDBAdminHierarchiesStatusesAndGuideTermsXML(Vector<String> allHierarcies, Vector<String> allGuideTerms, Locale targetLocale) {

        StringBuffer dataNeeded = new StringBuffer();
        Collections.sort(allHierarcies, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableHierarchies>");
        for (int i = 0; i < allHierarcies.size(); i++) {
            dataNeeded.append("<hierarchy>" + allHierarcies.get(i) + "</hierarchy>");
        }
        dataNeeded.append("</availableHierarchies>");

        dataNeeded.append("<availableStatuses>");
        dataNeeded.append("<status>" + Parameters.Status_Under_Construction + "</status>");
        dataNeeded.append("<status>" + Parameters.Status_For_Approval + "</status>");
        dataNeeded.append("<status>" + Parameters.Status_For_Insertion + "</status>");
        //dataNeeded.append("<status>" + dbGen.Status_For_Reinspection + "</status>");
        dataNeeded.append("<status>" + Parameters.Status_Approved + "</status>");
        dataNeeded.append("</availableStatuses>");

        Collections.sort(allGuideTerms, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableGuideTerms>");
        for (int i = 0; i < allGuideTerms.size(); i++) {
            dataNeeded.append("<GuideTerm>" + allGuideTerms.get(i) + "</GuideTerm>");
        }
        dataNeeded.append("</availableGuideTerms>");

        return dataNeeded;
    }

    public void writeResultsInXMLFile(Vector<String> allTerms, String startXML, Vector<String> output, 
            String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, 
            QClass Q, IntegerObject sis_session, Hashtable<String, NodeInfoSortItemContainer> termsInfo, 
            Vector<Long> resultNodesIds, Locale targetLocale) {



        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

        GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);
        SortItemLocaleComparator sortComparator = new SortItemLocaleComparator(targetLocale);

        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
            //out = new OutputStreamWriter(bout, "UTF8");

            out.write(ConstantParameters.xmlHeader);
            out.write(startXML);


        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {

            out.append("<data translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");

            out.append("<output>");
            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo("id") == 0) {
                    continue;
                } else {
                    out.append("<" + category + "/>");
                }
            }
            out.append("</output>");

            out.append("<terms>");

            for (int i = 0; i < allTerms.size(); i++) {
                String targetTerm = allTerms.get(i);
                //Utils.StaticClass.webAppSystemOutPrintln(targetTerm);
                NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
                String type = targetTermInfo.containerType;

                if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                    out.append("<ufterm index=\"" + (i + 1) + "\">");
                    out.append("<ufname id=\"" + targetTermInfo.descriptorInfo.get("id").get(0).getSysId() + "\">");
                    out.append(escapeXML(targetTerm));
                    out.append("</ufname>");

                    Vector<SortItem> values = new Vector<SortItem>();
                    values.addAll(targetTermInfo.descriptorInfo.get("use"));
                    Collections.sort(values, sortComparator);

                    for (int k = 0; k < values.size(); k++) {
                        long valueIDL = values.get(k).getSysId();
                        if (resultNodesIds.contains(valueIDL)) {
                            out.append("<use id=\"" + valueIDL + "\">");
                        } else {
                            out.append("<use>");
                        }
                        out.append(escapeXML(values.get(k).getLogName()));
                        out.append("</use>");
                    }


                    out.append("</ufterm>");
                    continue;
                }
                out.append("<term index=\"" + (i + 1) + "\">");

                long targetSysIdL = targetTermInfo.descriptorInfo.get("id").get(0).getSysId();

                out.append("<descriptor id=\"" + targetSysIdL + "\" >");
                out.append(escapeXML(targetTerm));
                out.append("</descriptor>");


                for (int m = 0; m < output.size(); m++) {

                    String category = output.get(m);
                    if (category.compareTo("id") == 0) {
                        continue;
                    }
                    Vector<SortItem> values = new Vector<SortItem>();
                    values.addAll(targetTermInfo.descriptorInfo.get(category));
                    if (category.compareTo(ConstantParameters.nt_kwd) == 0 || category.compareTo(ConstantParameters.translation_kwd) == 0 || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                        Collections.sort(values, guideTermComparator);
                    } else {
                        Collections.sort(values, sortComparator);
                    }

                    if (category.compareTo(ConstantParameters.bt_kwd) == 0
                            || category.compareTo(ConstantParameters.nt_kwd) == 0
                            || category.compareTo(ConstantParameters.topterm_kwd) == 0
                            || category.compareTo(ConstantParameters.rt_kwd) == 0
                            || category.compareTo(ConstantParameters.uf_kwd) == 0
                            || category.compareTo(ConstantParameters.translation_kwd) == 0
                            || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {

                        if(values.size()>0){
                        for (int k = 0; k < values.size(); k++) {

                            SortItem currentItem = values.get(k);
                            long valueIDL = currentItem.getSysId();
                            String linkClass = currentItem.getLinkClass();
                            if (resultNodesIds.contains(valueIDL)) {
                                if (linkClass == null || linkClass.length() == 0) {
                                    out.append("<" + category + " id=\"" + valueIDL + "\" linkClass=\"\" >");
                                } else {
                                    out.append("<" + category + " id=\"" + valueIDL + "\" linkClass=\"" + linkClass + "\">");
                                }
                            } else {
                                //Error in xslt transofrmation detected if linkClass attribute is missing (even if empty) in nt guide terms definitions
                                if (linkClass == null || linkClass.length() == 0) {
                                    out.append("<" + category + " linkClass=\"\">");
                                } else {
                                    out.append("<" + category + " linkClass=\"" + linkClass + "\">");
                                }
                            }
                            out.append(escapeXML(values.get(k).getLogName()));
                            out.append("</" + category + ">");
                        }
                        }
                        else{
                            out.append("<" + category + "/>");
                        }
                    } else if (category.equals(ConstantParameters.translations_scope_note_kwd)) {
                        String checkStr = "";
                        for (int k = 0; k < values.size(); k++) {
                            if (k > 0) {
                                checkStr += "\n";
                            }
                            checkStr += values.get(k).log_name;
                        }
                        Hashtable<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                        Vector<String> langcodes = new Vector<String>(trSns.keySet());
                        Collections.sort(langcodes);
                        for (int k = 0; k < langcodes.size(); k++) {
                            String linkClass = langcodes.get(k);
                            String val = trSns.get(linkClass);
                            out.append("<" + category + " linkClass=\"" + linkClass + "\">");
                            out.append(escapeXML(val));
                            out.append("</" + category + ">");
                        }


                    } else {

                        for (int k = 0; k < values.size(); k++) {
                            out.append("<" + category + ">");
                            out.append(escapeXML(values.get(k).getLogName()));
                            out.append("</" + category + ">");
                        }

                    }
                }


                out.append("</term>");
            }
            out.append("</terms>");
            out.append("</data>");

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("</page>");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        //Q.reset_name_scope();
        Q.free_all_sets();

    }

    public String getSupportedTranslationsXML(QClass Q, TMSAPIClass TA, IntegerObject sis_session, String targetThesaurus, boolean startQueryAndConnection, String extraTranslationXML) {

        String returnXML = "";
        DBGeneral dbGen = new DBGeneral();
        Hashtable<String, String> translationHash = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, targetThesaurus, null, startQueryAndConnection, true);
        returnXML = writeXMLTranslations(targetThesaurus, translationHash, extraTranslationXML);
        return returnXML;
    }

    public String writeXMLTranslations(String targetThesaurus, Hashtable<String, String> translationHash, String extraTranslationXML) {
        String returnXML = "";
        returnXML += "<Translations ofthes=\"" + targetThesaurus + "\" translationSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">";

        if (translationHash != null) {
            Enumeration<String> languagesEnumeration = translationHash.keys();

            while (languagesEnumeration.hasMoreElements()) {
                String languageWord = languagesEnumeration.nextElement();
                String languageId = translationHash.get(languageWord);

                returnXML += "<TranslationPair>";
                returnXML += "<TranslationWord>";
                returnXML += languageWord;
                returnXML += "</TranslationWord>";
                returnXML += "<TranslationIdentifier>";
                returnXML += languageId;
                returnXML += "</TranslationIdentifier>";
                returnXML += "</TranslationPair>";
            }
        }
        if (extraTranslationXML != null && extraTranslationXML.trim().length() > 0) {
            returnXML += extraTranslationXML.trim();
        }

        returnXML += "</Translations>";

        return returnXML;
    }

    public void CheckMaintenanceCompleted(ConfigDBadmin config, String restoreBackupTxtFilePath, String SystemOutPrefix) {

        File restoreBackUpFile = new File(restoreBackupTxtFilePath);

        if (restoreBackUpFile.exists()) {

            Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "Restore Backup Nedded. Creating Backup before restoration.");
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
            if (common_utils.CreateDBbackup("BackUp_before_Automatic_BackUp_Restore", new StringObject(), new StringObject()) == false) {
                Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "Error in creating Backup before Automatic BackUp Restore.");
                return;
            }

            try {
                InputStream fin = new FileInputStream(restoreBackupTxtFilePath);
                InputStream bin = new BufferedInputStream(fin);
                InputStreamReader logFileReader = new InputStreamReader(bin, "UTF-8");
                BufferedReader br = new BufferedReader(logFileReader);
                String BackUpName = br.readLine().trim();
                br.close();
                logFileReader.close();

                if (BackUpName != null && BackUpName.length() > 0) {
                    Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "Trying to restore " + BackUpName);
                    boolean restored = common_utils.RestoreDBbackup(BackUpName, new StringObject());
                    if (restored) {
                        Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "BackUp Restoration of '" + BackUpName + "' Successfully completed.");
                    } else {
                        Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "BackUp Restoration of '" + BackUpName + "' failed.");
                        //lock();
                    }
                } else {
                    //lock
                }


            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }

            restoreBackUpFile.delete();
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(SystemOutPrefix + "No Restoration Needed");
        }

    }

    public String translate(String messageXPath, Vector<String> args, String pathToErrorsXML) {

        String widgetNodeStr = "";

        try {

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(pathToErrorsXML));

            XPath xpath = XPathFactory.newInstance().newXPath();

            widgetNodeStr = xpath.evaluate(messageXPath, document);

            //widgetNodeStr = widgetNode.getTextContent();
            int counter = 1;

            while (widgetNodeStr.contains("%s")) {
                if (args != null && args.size() >= counter) {
                    String temp = this.escapeXML(args.get(counter - 1).toString());
                    //Need For Matcher quotereplacement because of the possibility of chars like /
                    widgetNodeStr = widgetNodeStr.replaceFirst("%s", Matcher.quoteReplacement(temp));
                    counter++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        return widgetNodeStr;

    }

    public static String[] getSortedTermAllOutputArray(){
        String[] output = {ConstantParameters.bt_kwd, ConstantParameters.nt_kwd, ConstantParameters.rt_kwd, ConstantParameters.uf_kwd, ConstantParameters.tc_kwd,
            ConstantParameters.translation_kwd, ConstantParameters.status_kwd, ConstantParameters.uf_translations_kwd,
            ConstantParameters.translations_found_in_kwd, ConstantParameters.primary_found_in_kwd,
            ConstantParameters.created_by_kwd, ConstantParameters.created_on_kwd, ConstantParameters.modified_by_kwd,
            ConstantParameters.modified_on_kwd, ConstantParameters.scope_note_kwd, ConstantParameters.translations_scope_note_kwd, ConstantParameters.historical_note_kwd
        };
        return output;
    }

    public static String[] getSortedTermAllOutputArray(String[] selectedOutput){


        Vector<String> initiallySelected = new Vector<String>();
        for(int k=0;k<selectedOutput.length;k++){
            initiallySelected.add(selectedOutput[k]);
        }

        String[] output = getSortedTermAllOutputArray();
        Vector<String> filteredOutput = new Vector<String>();
        for(int k=0;k<output.length;k++){
            String targetOutput = output[k];
            if(targetOutput!=null && targetOutput.length()>0 &&
                    initiallySelected.contains(targetOutput) &&
                    filteredOutput.contains(targetOutput) == false){
                filteredOutput.add(targetOutput);
            }
        }

        String[] returnArray = new String[filteredOutput.size()];
        for(int k=0;k<filteredOutput.size();k++){
            returnArray[k] = filteredOutput.get(k);
        }


        return returnArray;
    }

    public static String getMessagesXml() {
        return Utilities.getTranslationsXml("Messages.xml");
    }

    public static String getTranslationsXml(String fileName) {
        return Parameters.BaseRealPath + File.separator + "translations" + File.separator + fileName;
    }
}


