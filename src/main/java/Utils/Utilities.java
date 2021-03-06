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
package Utils;

import Admin_Thesaurus.DBMergeThesauri;
import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Users.UsersClass;
import XMLHandling.WriteFileData;
import java.io.*;
import java.util.*;
import neo4j_sisapi.*;
import javax.servlet.http.*;
//import isl.dms.DMSException;
//import isl.dms.xml.XMLTransform;
import java.text.SimpleDateFormat;
import java.net.URLDecoder;


import javax.xml.transform.Result;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.util.regex.Matcher;
import java.util.stream.Collectors;
import neo4j_sisapi.TMSAPIClass;

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

    

    /**
     * Constructs the uri that will be used in order to directly access a concept or Facet
     * @param req
     * @param targetThesaurus
     * @param isFacet
     * @param refId
     * @return 
     */    
    public String getExternalReaderReferenceUri(HttpServletRequest req, String targetThesaurus, boolean isFacet,long refId){
        //tried getServername etc but behind a proxy server could not determine the port 
        // server port return 443 while local port returned 8443 
        // so did not use getScheme, getServerName, getContextPath etc but applied 
        // string handling instead.
        
        String lastPart = "/"+targetThesaurus+(isFacet ? "/Facet/":"/Concept/")+refId;
        String firstPart = getExternalReaderReferenceUriPrefix(req, targetThesaurus);
        return firstPart + lastPart;
    }
    
    public String getExternalReaderReferenceUriSuffix(boolean isFacet,long refId){
        
        String lastPart = (isFacet ? "/Facet/":"/Concept/")+refId;
        return lastPart;
    }
    
    String guessBaseHref(HttpServletRequest request){
        String retUrl = "";
        
        String url = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        retUrl = url.substring(0, url.indexOf(contextPath))+contextPath;
        
        return retUrl;
    }
    
    /**
     * 
     * @param req
     * @param targetThesaurus
     * @return 
     */
    public String getExternalReaderReferenceUriPrefix(HttpServletRequest req, String targetThesaurus){
         //String lastPart = "/"+targetThesaurus+(isFacet ? "/Facet/":"/Concept/")+refId;
        String firstPart = req.getRequestURL().toString();
        String servletPath = req.getServletPath();        
        
        if(!firstPart.isEmpty() && !servletPath.isEmpty() && firstPart.endsWith(servletPath)){
            firstPart = firstPart.substring(0, firstPart.length() - servletPath.length());
        }
        if(!firstPart.endsWith("/")){
            firstPart+="/";
        }
        firstPart+=targetThesaurus;
        
        if(Parameters.ExternalURIsReplacements.size()>0){
            for(Map.Entry<String, String> entry : Parameters.ExternalURIsReplacements.entrySet()){
                if(firstPart.startsWith(entry.getKey())){
                    firstPart = firstPart.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        return firstPart;
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
    
    public String getXMLStart(String LeftMenuMode,final String uiLang) {
        return getXMLStart(LeftMenuMode, false, uiLang);
    }
    /*---------------------------------------------------------------------
    getXMLStart()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStart: an XML string
    CALLED BY: all servlets in order to get the first part of their XML representation
    ----------------------------------------------------------------------*/
    public String getXMLStart(String LeftMenuMode, boolean skipLeftMenu,final String uiLang) {
        String XMLStart =
                //"<?xml version=\"1.0\" encoding=\"windows-1253\"?>" +
                // changed by karam (6/2/2008) and also the xslTransform() 
                // method of this class so as to work properly.
                // the xslTransform_OLD() was working ONLY for encoding "windows-1253" and NOT for UTF-8 !!!
                ConstantParameters.xmlHeader+
                 // "<?xml-stylesheet href=\"" + xsl + "\" type=\"text/xsl\"?>" +
                // (canceled by karam - 7/2/2008): it has NO sense because each servlet calls xslTransform()
                // method of this class with the corresponding XSL file as parameter and writes the final HTML code to writer output
                "\r\n<page" +/*( (targetThesaurus!=null && targetThesaurus.length()>0) ? (" thesaurus=\""+targetThesaurus.toUpperCase()+"\"") :"") + */" language=\"" + uiLang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() +"\" showReferenceURI=\""+(Parameters.DisplayExternalLinkUrls?"yes":"no")  +"\"" +  ">";
                if(!skipLeftMenu){
                    XMLStart+= "\r\n<leftmenu>"
                    + "\r\n<activemode>" + LeftMenuMode + "</activemode>"
                    + "</leftmenu>";
                }

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
        String[] tabsetArray1 = {"Alphabetical", "Systematic", "Hierarchical", "Graphical", "SearchResults", "SearchCriteria", "Settings",
            "SearchHierarchyResults", "SearchFacetResults", "SearchSourceResults", "SearchUsersResults", "HierarchiesSearchCriteria", "FacetsSearchCriteria", "SourcesSearchCriteria", "New",
            "NewHierarchy", "NewFacet", "Move2Hier", "HistoricalInfo_Term", "HistoricalInfo_Hierarchy", "HistoricalInfo_Facet", "Rename", "RenameHierarchy", "RenameFacet",
            "Details", "HierarchyDetails", "FacetDetails", "SourceDetails"};
        String upTabsetXML = "";
        for (int i = 0; i < tabsetArray1.length; i++) {
            upTabsetXML += "<tab active=\"no\">" + tabsetArray1[i] + "</tab>";
        }
        upActiveTab = upActiveTab.replaceAll("&", "&amp;");
        upTabsetXML = upTabsetXML.replaceAll("<tab active=\"no\">" + upActiveTab + "</tab>",
                "<tab active=\"yes\" expand_search_results_with_rnts=\""+(Parameters.AbilityToExpandSearchResultsWithRNTs ? "yes" : "no") + "\">" + upActiveTab + "</tab>");

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
        // tra=\"" + tmsUsers.translateGroup(SessionUserInfo.userGroup) + "\"
        XMLTHEMASUserInfo += "<userGroup"+(Parameters.ThesTeamEditOnlyCreatedByTerms? " thesteamcaneditforinsertions='true'>":">") + SessionUserInfo.userGroup + "</userGroup>";
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

    //ALMOST IDENTICAL TO getResultsInXml_ForTableLayout
    public void getResultsInXmlGuideTermSorting(ArrayList<String> allTerms, 
                      HashMap<String, NodeInfoSortItemContainer> termsInfo, 
                      ArrayList<String> output, StringBuffer xmlResults, 
                      QClass Q, IntegerObject sis_session, 
                      Locale targetLocale, 
                      UserInfoClass SessionUserInfo, boolean skipOutput, boolean skipIds) {
        //GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);
        SortItemComparator guideTermComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);
        SortItemComparator sortComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);


        
        xmlResults.append("<data thesaurus=\"").append(SessionUserInfo.selectedThesaurus.toUpperCase()).append("\" translationsSeperator=\"").append(Parameters.TRANSLATION_SEPERATOR).append("\" displayCreatorInAlphabetical=\"").append(Parameters.CreatorInAlphabeticalTermDisplay).append("\">");
        if(!skipOutput){
            xmlResults.append("<output>");
            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo(ConstantParameters.id_kwd) != 0) {
                    xmlResults.append("<" + category + "/>");
                }
            }
            xmlResults.append("</output>");
        }
        xmlResults.append("<terms>");
        for (int i = 0; i < allTerms.size(); i++) {

            String targetTerm = allTerms.get(i);
            NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
            String type = targetTermInfo.containerType;

            if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                xmlResults.append("<ufterm index=\"" + (i + 1) + "\">");
                xmlResults.append("<ufname"+(skipIds?"":(" id=\"" + targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId() + "\"")) +">");
                xmlResults.append(escapeXML(targetTerm));
                xmlResults.append("</ufname>");

                ArrayList<SortItem> values = new ArrayList<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get("use"));
                Collections.sort(values, sortComparator);

                for (int k = 0; k < values.size(); k++) {
                    xmlResults.append("<use"+(skipIds?"":(" id=\"" + values.get(k).getSysId() + "\""))+">");
                    xmlResults.append(escapeXML(values.get(k).getLogName()));
                    xmlResults.append("</use>");
                }
                xmlResults.append("</ufterm>");
                continue;
            }

            xmlResults.append("<term index=\"" + (i + 1) + "\">");

            long targetSysIdL = targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId();
            long refIdL = targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getThesaurusReferenceId();
            String transliteration = targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getLogNameTransliteration();
            
            xmlResults.append("<descriptor").append(skipIds?"":(" id=\"" + targetSysIdL + "\""));
            
            if(refIdL>0){
                xmlResults.append(" "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+refIdL+"\"");
                    if(Parameters.ShowReferenceURIalso){
                    xmlResults.append(" "+ConstantParameters.system_referenceUri_kwd+"=\""+consrtuctReferenceUri(SessionUserInfo.selectedThesaurus.toUpperCase(), Utilities.ReferenceUriKind.TERM, refIdL)+"\""); 
                }
            }
            /*
            if(transliteration!=null && transliteration.length()>0){
                xmlResults.append(" "+ConstantParameters.system_transliteration_kwd+"=\""+transliteration+"\""); 
            }*/
            //xmlResults.append("<descriptor>");
            xmlResults.append(">");
            xmlResults.append(escapeXML(targetTerm));
            xmlResults.append("</descriptor>");

            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo(ConstantParameters.id_kwd) == 0) {
                    continue;
                }
                else if (category.compareTo(ConstantParameters.system_transliteration_kwd) == 0) {
                    xmlResults.append("<" + category + ">");
                    xmlResults.append(escapeXML(transliteration));
                    xmlResults.append("</" + category + ">");
                    continue;
                }
                ArrayList<SortItem> values = new ArrayList<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get(category));
                if (category.compareTo(ConstantParameters.nt_kwd) == 0 || 
                        category.compareTo(ConstantParameters.translation_kwd) == 0 || 
                        category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                    Collections.sort(values, guideTermComparator);
                } else {
                    Collections.sort(values, sortComparator);
                }
                
                if (category.compareTo(ConstantParameters.facet_kwd) == 0
                        || category.compareTo(ConstantParameters.uf_kwd) == 0
                        || category.compareTo(ConstantParameters.topterm_kwd) == 0
                        || category.compareTo(ConstantParameters.bt_kwd) == 0
                        || category.compareTo(ConstantParameters.rt_kwd) == 0
                        || category.compareTo(ConstantParameters.nt_kwd) == 0
                        || category.compareTo(ConstantParameters.translation_kwd) == 0
                        || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) { // add id info in order to add anchors in paging
                    for (SortItem valueSortItem : values) {
                        
                        long linkRefIdL = valueSortItem.getThesaurusReferenceId();
                        String linkTransliteration =valueSortItem.getLogNameTransliteration();
            
                        ReferenceUriKind whatKind = (category.compareTo(ConstantParameters.facet_kwd) == 0)? ReferenceUriKind.FACET: ReferenceUriKind.TERM;
                        xmlResults.append("<" + category + (skipIds?"":(" id=\"" + valueSortItem.getSysId() + "\"")));
                        
                        if(category.compareTo(ConstantParameters.nt_kwd) == 0 || 
                                category.compareTo(ConstantParameters.translation_kwd) == 0 ||
                                category.compareTo(ConstantParameters.uf_translations_kwd)==0  ){
                            if(valueSortItem.getLinkClass()!=null && valueSortItem.getLinkClass().length()>0){
                                xmlResults.append(" linkClass=\"" + escapeXML(valueSortItem.getLinkClass()) + "\"");
                            }
                        }
                        if(linkRefIdL>0){
                          xmlResults.append(" "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+linkRefIdL+"\"");
                          if(Parameters.ShowReferenceURIalso){
                              xmlResults.append(" "+ConstantParameters.system_referenceUri_kwd+"=\""+consrtuctReferenceUri(SessionUserInfo.selectedThesaurus.toUpperCase(), whatKind, linkRefIdL)+"\"");
                          } 
                        }
                        if(Parameters.ShowTransliterationInAllXMLStream){
                            if(linkTransliteration!=null && linkTransliteration.length()>0){
                                xmlResults.append(" "+ConstantParameters.system_transliteration_kwd+"=\""+linkTransliteration+"\""); 
                            }
                        }
                        
                        xmlResults.append(">");
                        xmlResults.append(escapeXML(valueSortItem.getLogName()));
                        xmlResults.append("</" + category + ">");
                    }
                } else if (category.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {
                    String checkStr = "";
                    for (SortItem valueSortItem : values) {
                        if (checkStr.length() > 0) {
                            checkStr += "\n";
                        }
                        checkStr += valueSortItem.getLogName();
                    }
                    if(checkStr.length()>0){
                        HashMap<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                        ArrayList<String> langcodes = new ArrayList<String>(trSns.keySet());
                        Collections.sort(langcodes);

                        for (String linkClass : langcodes) {
                            String val = trSns.get(linkClass);
                            xmlResults.append("<" + category + " linkClass=\"" + escapeXML(linkClass) + "\">");
                            xmlResults.append(escapeXML(val));
                            xmlResults.append("</" + category + ">");
                        }
                    }

                } else if (category.compareTo(ConstantParameters.status_kwd) == 0) {
                    for (SortItem valueSortItem : values) {
                        xmlResults.append("<" + category + ">");
                        xmlResults.append(escapeXML(Parameters.getStatusRepresentation_ForDisplay(valueSortItem.getLogName(), SessionUserInfo)));
                        xmlResults.append("</" + category + ">");
                    }
                }
                else {
                    for (SortItem valueSortItem : values) {
                        xmlResults.append("<" + category + ">");
                        xmlResults.append(escapeXML(valueSortItem.getLogName()));
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
    getResultsInXml_ForTableLayout()
    -----------------------------------------------------------------------
    INPUT: - Vector allTerms: the Vector with the terms to be parsed
    - String[] output: the properties of each term to be collected
    OUTPUT: a String with the XML representation of the results
    CALLED BY: servlets: ViewAll with output = {"name", ConstantParameters.dn_kwd}
    ----------------------------------------------------------------------*/
    public void getResultsInXml_ForTableLayout(UserInfoClass SessionUserInfo, ArrayList<SortItem> allTerms, HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<String> output, StringBuffer xmlResults, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        
        //SortItemLocaleComparator sortComparator = new SortItemLocaleComparator(targetLocale);
        SortItemComparator sortComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
        //GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);
        SortItemComparator guideTermComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);

        xmlResults.append("<data translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");
        xmlResults.append("<output>");
        for (int m = 0; m < output.size(); m++) {

            String category = output.get(m);
            if (category.compareTo(ConstantParameters.id_kwd) == 0 
                    || category.compareTo(ConstantParameters.system_transliteration_kwd) == 0
                    || category.compareTo(ConstantParameters.system_referenceIdAttribute_kwd) == 0
                    || category.compareTo(ConstantParameters.system_referenceUri_kwd) == 0) {
                continue;
            } else {
                xmlResults.append("<" + category + "/>");
            }
        }
        xmlResults.append("</output>");

        xmlResults.append("<terms>");
        for (int i = 0; i < allTerms.size(); i++) {
            SortItem targetSortItem = allTerms.get(i);                    
            String targetTerm = targetSortItem.getLogName();
            NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
            String type = targetTermInfo.containerType;

            if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                xmlResults.append("<ufterm index=\"" + (i + 1) + "\">");
                xmlResults.append("<ufname id=\"" + targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId() + "\">");
                xmlResults.append(escapeXML(targetTerm));
                xmlResults.append("</ufname>");

                ArrayList<SortItem> values = new ArrayList<SortItem>();
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

            long targetSysIdL = targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId();
            xmlResults.append("<descriptor id=\"" + targetSysIdL + "\" >");
            //xmlResults.append("<descriptor>");
            xmlResults.append(escapeXML(targetTerm));
            xmlResults.append("</descriptor>");

            for (int m = 0; m < output.size(); m++) {

                String category = output.get(m);
                if (category.compareTo(ConstantParameters.id_kwd) == 0) {
                    continue;
                }
                ArrayList<SortItem> values = new ArrayList<SortItem>();
                values.addAll(targetTermInfo.descriptorInfo.get(category));
                if ( //category.compareTo(ConstantParameters.nt_kwd) == 0 || no need to sort nts with sortitem comparator
                        category.compareTo(ConstantParameters.translation_kwd) == 0 || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
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
                        xmlResults.append("<" + category + " id=\"" + values.get(k).getSysId() + "\" linkClass=\"" + escapeXML(values.get(k).getLinkClass()) + "\" >");
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
                    HashMap<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                    ArrayList<String> langcodes = new ArrayList<String>(trSns.keySet());
                    Collections.sort(langcodes);
                    for (int k = 0; k < langcodes.size(); k++) {
                        String linkClass = langcodes.get(k);
                        String val = trSns.get(linkClass);
                        xmlResults.append("<" + category + " linkClass=\"" + escapeXML(linkClass) + "\">");
                        xmlResults.append(escapeXML(val));
                        xmlResults.append("</" + category + ">");
                    }

                }else if (category.compareTo(ConstantParameters.status_kwd) == 0) {
                    for (int k = 0; k < values.size(); k++) {
                        xmlResults.append("<" + category + ">");
                        xmlResults.append(escapeXML(Parameters.getStatusRepresentation_ForDisplay(values.get(k).getLogName(), SessionUserInfo)));
                        xmlResults.append("</" + category + ">");
                    }
                }else {
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
        
        SortItemComparator guideTermComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);
        
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
                    ArrayList<SortItem> vtranslations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, output[j].equals(ConstantParameters.translation_kwd), currentTerm, Q, sis_session);
                    //Collections.sort(vtranslations, new GuideTermSortItemComparator(targetLocale));
                    Collections.sort(vtranslations, guideTermComparator);
                    XMLresults.append("<" + output[j] + ">");
                    for (int k = 0; k < vtranslations.size(); k++) {
                        XMLresults.append("<name linkClass=\"" + escapeXML(vtranslations.get(k).linkClass) + "\">");
                        XMLresults.append(escapeXML(vtranslations.get(k).log_name));
                        XMLresults.append("</name>");
                    }
                    XMLresults.append("</" + output[j] + ">");
                } else {

                    ArrayList v = dbGen.returnResults(SessionUserInfo, currentTerm, output[j], Q,TA, sis_session);
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
    getResultsInXml_ForTableLayout()
    -----------------------------------------------------------------------
    INPUT:  Vector availableFacets: contains all available facets (either new or released)
    OUTPUT: a String with the XML representation of the available facets
    CALLED BY: Create_Modify_Hierarchy servlet
    ----------------------------------------------------------------------
    public String getResultsInXml_Hierarchy(ArrayList<String> availableFacets) {

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
    public String getResultsinXml_Hierarchy(UserInfoClass SessionUserInfo, ArrayList displayHierarchies, String[] output, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        DBGeneral dbGen = new DBGeneral();

        StringBuffer XMLresults = new StringBuffer();
        XMLresults.append("<data>");
        XMLresults.append("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if (category.compareTo(ConstantParameters.id_kwd) == 0 || category.compareTo("name") == 0) {
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
                    ArrayList<String> v = dbGen.returnResults_Hierarchy(SessionUserInfo, displayHierarchies.get(i).toString(), output[j], Q, sis_session, targetLocale);
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

    public void getResultsInXml_Source(UserInfoClass SessionUserInfo, ArrayList<String> displaySources, String[] output, QClass Q, TMSAPIClass TA, IntegerObject sis_session, Locale targetLocale, StringBuffer xmlResults) {

        DBGeneral dbGen = new DBGeneral();

        xmlResults.append("<data>");

        xmlResults.append("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if (category.compareTo(ConstantParameters.id_kwd) == 0 || category.compareTo("name") == 0) {
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
                    ArrayList<String> v = dbGen.returnResults_Source(SessionUserInfo, displaySources.get(i), output[j], Q,TA, sis_session);
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
    public ArrayList<String> getEquals(UserInfoClass SessionUserInfo, String criterion, String value, String[] links, QClass Q, IntegerObject sis_session, DBGeneral dbGen) {
        ArrayList<String> v = new ArrayList<String>();

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
    public ArrayList<String> getContains(THEMASUserInfo SessionUserInfo, String criterion, String value, String[] links, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen) {
        ArrayList<String> v = new ArrayList<String>();

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
    public ArrayList<String> getNotEquals(String selectedThesaurus, String criterion, String value, String[] links, QClass Q, IntegerObject sis_session, DBGeneral dbGen) {
        /*HINT: Prwta 8a elegxoume an autes oi times uparxoun sto sunolo ws exei ws twra k 8a ta bgazoume apo kei.. *-) */
        ArrayList<String> v = new ArrayList<String>();

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

        ArrayList<String> v = new ArrayList<String>();

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

        ArrayList<String> terms = new ArrayList<String>();

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
    OUTPUT: - HashMap paramsHashMap: a HashMap with the parameters of the current servlet
    FUNCTION: gets to a HashMap the parameters of the current servlet
    CALLED BY: all servlets in order to get their parameters
    ----------------------------------------------------------------------*/
    public HashMap<String, Object> getFormParams(HttpServletRequest request) {

        HashMap<String, Object> paramsHashMap = new HashMap<String, Object>();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                paramsHashMap.put(paramName, paramValue);

            } else {
                //params.put(paramName, paramValues);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < paramValues.length; i++) {
                    sb.append(paramValues[i]);
                    if (i < paramValues.length - 1) {
                        sb.append(",");
                    }
                }
                paramsHashMap.put(paramName, sb.toString());
            }
        }
        return paramsHashMap;
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

    public ArrayList<String> getDecodedParameterValues(String[] encodedParams) throws java.io.UnsupportedEncodingException {

        ArrayList<String> returnParams_decoded = new ArrayList<String>();
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

        Month = "January";
        break;
        }
        case 2: {

        Month = "February";
        break;
        }
        case 3: {

        Month = "March";
        break;
        }
        case 4: {

        Month = "April";
        break;
        }
        case 5: {

        Month = "May";
        break;
        }
        case 6: {

        Month = "June";
        break;
        }
        case 7: {

        Month = "July";
        break;
        }
        case 8: {
        Month = "August";
        break;
        }
        case 9: {

        Month = "September";
        break;
        }
        case 10: {

        Month = "October";
        break;
        }
        case 11: {

        Month = "November";
        break;
        }
        case 12: {

        Month = "December";
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
    public ArrayList<String> get_Vector_from_String(String str, String delimeter) {

        ArrayList<String> result = new ArrayList<String>();

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
        //escaping double hyphen that may cause an error in comments
        


        /*
        Me to parakato xalasan ola
        try {
            returnVal = new String(returnVal.getBytes("UTF8"));
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return returnVal;
    }
    
    public static String escapeXMLComment(String target) {
        if (target == null) {
            return "";
        }
        String returnVal = escapeXML(target);
        returnVal = returnVal.replaceAll("--", "&#x002D;&#x002D;");
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
    public void InformSearchOperatorsAndValuesWithSpecialCharacters(String[] input,String[] searchOperators, String[] searchInputValues, boolean nameRefersToSource) {
        int len = searchOperators.length;
        for (int i = 0; i < len; i++) {
            
            if (searchOperators[i].compareTo("~*") == 0) { // starts with
                searchOperators[i] = ConstantParameters.searchOperatorContains;
                searchInputValues[i] = searchInputValues[i] + "*";
            } else if (searchOperators[i].compareTo("*~") == 0) { // ends with
                searchOperators[i] = ConstantParameters.searchOperatorContains;
                searchInputValues[i] = "*" + searchInputValues[i];
            } else if (searchOperators[i].compareTo("!~*") == 0) { // not starts with
                searchOperators[i] = ConstantParameters.searchOperatorNotContains;
                searchInputValues[i] = searchInputValues[i] + "*";
            } else if (searchOperators[i].compareTo("!*~") == 0) { // not ends with
                searchOperators[i] = ConstantParameters.searchOperatorNotContains;
                searchInputValues[i] = "*" + searchInputValues[i];
            }
            else if (searchOperators[i].compareTo(ConstantParameters.searchOperatorContains)==0){
                String relevantInput = input[i];
                ArrayList<String> keywordsThatShouldBeSearchedWithTransliteration = new ArrayList<>();
                //if(!nameRefersToSource)// sources can also be serarched via transliteration
                    keywordsThatShouldBeSearchedWithTransliteration.add("name");
                //}
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.bt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.nt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rbt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rnt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.topterm_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.facet_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add("term");//hierarchy or facet case
                //if(nameRefersToSource){ sources can also be serarched via transliteration
                    keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.primary_found_in_kwd);
                    keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.translations_found_in_kwd);
                //}
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.translation_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.uf_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.uf_translations_kwd);
                
                if(keywordsThatShouldBeSearchedWithTransliteration.contains(relevantInput)){
                    searchOperators[i] = ConstantParameters.searchOperatorTransliterationContains;
                }
            }
            else if (searchOperators[i].compareTo(ConstantParameters.searchOperatorNotContains)==0){
                String relevantInput = input[i];
                ArrayList<String> keywordsThatShouldBeSearchedWithTransliteration = new ArrayList<>();
                //if(!nameRefersToSource){ // sources can also be serarched via transliteration
                    keywordsThatShouldBeSearchedWithTransliteration.add("name");
                //}
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.bt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.nt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rbt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rnt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.rt_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.topterm_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.facet_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add("term");//hierarchy or facet case
                //if(nameRefersToSource){ // sources can also be serarched via transliteration
                    keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.primary_found_in_kwd);
                    keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.translations_found_in_kwd);
                //}
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.translation_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.uf_kwd);
                keywordsThatShouldBeSearchedWithTransliteration.add(ConstantParameters.uf_translations_kwd);
                
                if(keywordsThatShouldBeSearchedWithTransliteration.contains(relevantInput)){
                    searchOperators[i] = ConstantParameters.searchOperatorNotTransliterationContains;
                }
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

    /**
     * Enabling relative import - include uris in xslts
     * @param xmlInputPath
     * @param xslPath
     * @param outputFullPath
     * @param rootUri 
     */
    public void XmlFileTransform(String xmlInputPath, String xslPath, String outputFullPath, String rootUri) {


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

            String xslPathUri = xslPath.replace(Parameters.BaseRealPath,rootUri).replaceAll("\\\\","/");
            StreamSource sSource = new StreamSource(new FileInputStream(xslPath));
            sSource.setSystemId(xslPathUri);
            //StreamSource sSource = new StreamSource(xslPathUri);
            Templates template = factory.newTemplates(sSource);
            
            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files

            File xmlFile = new File(xmlInputPath);

            Source source = new StreamSource(xmlFile);
            //source.setSystemId(rootUri);
            Result result = new StreamResult(out);
            //result.setSystemId(rootUri);
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

    /*
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

            StreamSource sSource = new StreamSource(new FileInputStream(xslPath));
            
            Templates template = factory.newTemplates(sSource);
            
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
    }*/

    public String getBTNTWithGuideTermsResultsInXml(String term, String attribute, String GuideTermPrefix, ArrayList<SortItem> btsORnts, ArrayList<String> existingGuideTermsVec, Locale targetLocale) {

        SortItemComparator sortComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
        
        StringBuffer sb = new StringBuffer();
        sb.append("<current>");
        sb.append("<term>");
        sb.append("<name>" + escapeXML(term) + "</name>");


        if (!btsORnts.isEmpty()) {
            Collections.sort(btsORnts, sortComparator);
            sb.append("<" + attribute + ">");
            for (SortItem btOrNtSortItem : btsORnts) {
                sb.append("<name linkClass=\"" + escapeXML(btOrNtSortItem.getLinkClass().replaceFirst(GuideTermPrefix, "")) + "\">");
                sb.append(escapeXML(btOrNtSortItem.getLogName()));
                sb.append("</name>");
            }
            sb.append("</" + attribute + ">");
        }
        sb.append("</term>");
        sb.append("</current>");


        sb.append("<GuideTerms>");
        for (int i = 0; i < existingGuideTermsVec.size(); i++) {
            sb.append("<GuideTerm>" + escapeXML(existingGuideTermsVec.get(i)) + "</GuideTerm>");
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
        SortItemComparator guideTermComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);

        ArrayList<String> v = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        sb.append("<current translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");
        sb.append("<term>");

        for (int j = 0; j < output.length; j++) {

            if (output[j].equals("name")) {
                sb.append("<name>" + escapeXML(term) + "</name>");
            } else if (output[j].equals(ConstantParameters.translation_kwd) || output[j].equals(ConstantParameters.uf_translations_kwd)) {

                ArrayList<SortItem> vtranslations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, output[j].equals(ConstantParameters.translation_kwd), term, Q, sis_session);
                //Collections.sort(vtranslations, new GuideTermSortItemComparator(targetLocale));
                Collections.sort(vtranslations, guideTermComparator);
                sb.append("<" + output[j] + ">");
                for (int k = 0; k < vtranslations.size(); k++) {
                    sb.append("<name linkClass=\"" + escapeXML(vtranslations.get(k).linkClass) + "\">");
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
                        HashMap<String, String> trs = getTranslationScopeNotes(translationsScopeNoteStr);
                        ArrayList<String> trSns = new ArrayList<String>(trs.keySet());
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
                        /*if (output[j].equals(ConstantParameters.status_kwd)){
                           sb.append(escapeXML(Parameters.getStatusRepresentation_ForDisplay(v.get(k).toString(), SessionUserInfo)));
                        }
                        else{*/
                                sb.append(escapeXML(v.get(k).toString()));
                        //}
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

    public HashMap<String, String> getTranslationScopeNotes(String scopeNoteValStr) {
        String scopeNoteVal = scopeNoteValStr;
        HashMap<String, String> returnVals = new HashMap<String, String>();

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

    
    
    public StringBuffer getDBAdminHierarchiesStatusesAndGuideTermsXML(UserInfoClass SessionUserInfo, ArrayList<String> allHierarcies, 
            ArrayList<String> allGuideTerms, Locale targetLocale) {

        StringBuffer dataNeeded = new StringBuffer();
        Collections.sort(allHierarcies, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableHierarchies>");
        for (int i = 0; i < allHierarcies.size(); i++) {
            dataNeeded.append("<hierarchy>" + escapeXML(allHierarcies.get(i)) + "</hierarchy>");
        }
        dataNeeded.append("</availableHierarchies>");

        dataNeeded.append("<availableStatuses>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction, SessionUserInfo) + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Approval, SessionUserInfo) + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion, SessionUserInfo) + "</status>");
        //dataNeeded.append("<status>" + dbGen.Status_For_Reinspection + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Approved, SessionUserInfo) + "</status>");
        dataNeeded.append("</availableStatuses>");

        Collections.sort(allGuideTerms, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableGuideTerms>");
        for (int i = 0; i < allGuideTerms.size(); i++) {
            dataNeeded.append("<GuideTerm>" + escapeXML(allGuideTerms.get(i)) + "</GuideTerm>");
        }
        dataNeeded.append("</availableGuideTerms>");

        return dataNeeded;
    }
    
    public StringBuffer getDBAdminHierarchiesStatusesAndGuideTermsWithStatisticsXML(UserInfoClass SessionUserInfo, ArrayList<String> allHierarcies, 
            HashMap<String,Integer> allGuideTerms, Locale targetLocale) {

        StringBuffer dataNeeded = new StringBuffer();
        Collections.sort(allHierarcies, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableHierarchies>");
        for (int i = 0; i < allHierarcies.size(); i++) {
            dataNeeded.append("<hierarchy>" + escapeXML(allHierarcies.get(i)) + "</hierarchy>");
        }
        dataNeeded.append("</availableHierarchies>");

        dataNeeded.append("<availableStatuses>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction, SessionUserInfo) + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Approval, SessionUserInfo) + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion, SessionUserInfo) + "</status>");
        //dataNeeded.append("<status>" + dbGen.Status_For_Reinspection + "</status>");
        dataNeeded.append("<status>" + Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Approved, SessionUserInfo) + "</status>");
        dataNeeded.append("</availableStatuses>");

        ArrayList<String> gTerms = new ArrayList();
        gTerms.addAll(allGuideTerms.keySet());
        Collections.sort(gTerms, new StringLocaleComparator(targetLocale));
        dataNeeded.append("<availableGuideTerms>");
        for (String gTerm : gTerms) {
            dataNeeded.append("<GuideTerm card=\""+allGuideTerms.get(gTerm)+"\">" + escapeXML(gTerm) + "</GuideTerm>");
        }
        dataNeeded.append("</availableGuideTerms>");

        return dataNeeded;
    }

    /*
    public void writeResultsInOutputStream(){
        
    }
    */
    
    public void writeResultsInRDFFile(UserInfoClass SessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            String SkosExportConceptScheme, 
            String SkosExportBaseNameSpace,
            String webAppSaveResults_temporary_filesAbsolutePath, 
            String Save_Results_file_name,
            HashMap<String, NodeInfoSortItemContainer> termsInfo) {
        
        WriteFileData writer = new WriteFileData();
        
        ConstantParameters.referenceThesaurusSchemeName = SkosExportConceptScheme;
        ConstantParameters.SchemePrefix = SkosExportBaseNameSpace;
        String logFileNamePath = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".rdf";
        
        OutputStreamWriter logFileWriter = null;
        
        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);
            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");


            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile of export data from thesaurus: " + exprortThesaurus + " in file: " + logFileNamePath + ".");

        } catch (FileNotFoundException | UnsupportedEncodingException exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        String exportSchemaName = ConstantParameters.xmlschematype_skos;
        String exprortThesaurus = SessionUserInfo.selectedThesaurus;
        
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        
        HashMap<String, ArrayList<String>> hierarchyFacetsStrFormat = new HashMap<>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<>();
        HashMap<String,ArrayList<ExternalLink>> termExtLinks = new HashMap<>();
        ArrayList<ExternalVocabulary> vocabularyIdentifiers = new ArrayList<>();
        /*
            hierarchyFacets.forEach( (key,value) -> {
                hierarchyFacetsStrFormat.put(key.getLogName(), new ArrayList<String>(value.stream().map(item -> item.getLogName()).collect(Collectors.toList())));
            });
        */    
        
        try{

                writer.WriteFileStart(logFileWriter, exportSchemaName, exprortThesaurus, SessionUserInfo.UILang);
                /* WriteTranslationCategories for SKOS export format it does not produce anything
                writer.WriteTranslationCategories(logFileWriter, exportSchemaName, translationCategories);
                */

                dbMerge.ReadThesaurusExternalLinksAndVocabularies(SessionUserInfo, Q,TA, sis_session, exprortThesaurus,null, termExtLinks,vocabularyIdentifiers);
                
                
                
                
                //writer.WriteFacetsFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, xmlFacetsInSortItem, hierarchyFacets, termsInfo, null, null);
                //writer.WriteHierarchiesFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, hierarchyFacets, termsInfo, XMLguideTermsRelations, termExtLinks, null, null);
                
                //this causes refIds of bts or nts to be lost not suitbale for rdf export
                HashMap<String, NodeInfoStringContainer> termsInfoStr = this.getStringContainerFromSortItemContainer(termsInfo);
                
                HashMap<String,Long> addtionalTermIds = null;
                if (exportSchemaName.equals(ConstantParameters.xmlschematype_skos)){
                    final ArrayList<String> targetKeywords = new ArrayList(Arrays.asList(ConstantParameters.bt_kwd,ConstantParameters.nt_kwd,ConstantParameters.topterm_kwd, ConstantParameters.rt_kwd));
                    addtionalTermIds = new HashMap();
                    for(NodeInfoSortItemContainer sItem : termsInfo.values()){
                        
                        for(String targetKeyword : targetKeywords){
                            if(sItem.descriptorInfo.containsKey(targetKeyword)){
                                ArrayList<SortItem> vals = sItem.descriptorInfo.get(targetKeyword);
                                
                                for(SortItem val : vals){
                                    if(!termsInfoStr.containsKey(val.log_name) && !addtionalTermIds.containsKey(val.log_name)){
                                        addtionalTermIds.put(val.getLogName(), val.getThesaurusReferenceId());
                                    }
                                }
                                
                            }
                        }
                        
                    }
                }
                writer.WriteTerms(logFileWriter, 
                        exportSchemaName, exprortThesaurus, hierarchyFacetsStrFormat,
                        termsInfoStr, XMLguideTermsRelations,termExtLinks, null, addtionalTermIds);
                
                
                /* WriteGuideTerms for SKOS export format it only writes in comments
                writer.WriteGuideTerms(logFileWriter, exportSchemaName, guideTerms);
                */
                
                /*
                WriteSources for SKOS export format it does not produce anything
                writer.WriteSources(logFileWriter, exportSchemaName, XMLsources);
                */
                
                /*
                WriteExtVocabularies for SKOS export format it does not produce anything
                writer.WriteExtVocabularies(logFileWriter, exportSchemaName, vocabularyIdentifiers);
                */
                writer.WriteFileEnd(logFileWriter, exportSchemaName);
            }catch (IOException e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in Utilities writeResultsInRDFFile() Message:" + e.getMessage());
                Utils.StaticClass.handleException(e);

            }
        //produce rdf/skos file
                    //String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";
                    /*
                    String SkosExportConceptScheme = request.getParameter("skosConceptScheme");
                    String SkosExportBaseNameSpace = request.getParameter("skosBaseNameSpace");
                    
                    Filename += ".rdf";
                    if(SkosExportBaseNameSpace!=null && SkosExportBaseNameSpace.length()>0 &&  SkosExportConceptScheme!=null && SkosExportConceptScheme.length()>0){
                        ConstantParameters.referenceThesaurusSchemeName = SkosExportConceptScheme;
                        ConstantParameters.SchemePrefix = SkosExportBaseNameSpace;
                    }
                    else{

                        ConstantParameters.referenceThesaurusSchemeName = "http://"+hostName+":" + port + "/" + Parameters.ApplicationName + "#" + exprortThesaurus;
                        ConstantParameters.SchemePrefix = "http://"+hostName+":" + port + "/" + Parameters.ApplicationName +"/"+ exprortThesaurus;
                    }
                    
                    logFileNamePath += "/" + Filename;





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

                    exp.exportThesaurusActions(SessionUserInfo, exprortThesaurus, exportSchemaName, logFileWriter,thesauriNames,allHierarchies,allGuideTerms);
                    
                    
                    try{

                writer.WriteFileStart(logFileWriter, exportSchemaName, exprortThesaurus, SessionUserInfo.UILang);
                writer.WriteTranslationCategories(logFileWriter, exportSchemaName, translationCategories);

                writer.WriteFacetsFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, xmlFacetsInSortItem, hierarchyFacets, termsInfo, null, null);
                writer.WriteHierarchiesFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, hierarchyFacets, termsInfo, XMLguideTermsRelations, termExtLinks, null, null);
                writer.WriteTerms(logFileWriter, exportSchemaName, exprortThesaurus, hierarchyFacetsStrFormat, termsInfo, XMLguideTermsRelations,termExtLinks, null);
                writer.WriteGuideTerms(logFileWriter, exportSchemaName, guideTerms);
                writer.WriteSources(logFileWriter, exportSchemaName, XMLsources);
                writer.WriteExtVocabularies(logFileWriter, exportSchemaName, vocabularyIdentifiers);
                writer.WriteFileEnd(logFileWriter, exportSchemaName);
            }catch (Exception e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in DBexportData exportThesaurusActions() Message:" + e.getMessage());
                Utils.StaticClass.handleException(e);

            }

                    */
    }
    
    public void writeResultsInXMLFile(PrintWriter outStream, ArrayList<String> allTerms, String startXML, ArrayList<String> output, 
            String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, 
            QClass Q, IntegerObject sis_session, HashMap<String, NodeInfoSortItemContainer> termsInfo, 
            ArrayList<Long> resultNodesIds, Locale targetLocale, UserInfoClass SessionUserInfo, boolean skipIds,boolean sortNtsViaLinkClassFirst) {



        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml";

        //GuideTermSortItemComparator guideTermComparator = new GuideTermSortItemComparator(targetLocale);
        SortItemComparator guideTermComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);
        SortItemComparator sortComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);

        boolean streamOutput = false;
        if(outStream!=null){
            streamOutput = true;
        }
        
        OutputStreamWriter out = null;
        
        String appendVal = ConstantParameters.xmlHeader+ startXML;
        if(streamOutput){
            outStream.append(appendVal);
        }
        else{
            try {
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                //out = new OutputStreamWriter(bout, "UTF8");

                out.append(appendVal);
            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }
        
        try {
            
            appendVal = "<data thesaurus=\"" +SessionUserInfo.selectedThesaurus.toUpperCase()+"\" translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\" displayCreatorInAlphabetical=\""+Parameters.CreatorInAlphabeticalTermDisplay+"\">";
            if(!streamOutput){
                //not of interest in xmlstream
                appendVal+="<output>";
                for (String category : output) {

                    if (category.compareTo(ConstantParameters.id_kwd) == 0
                            || category.compareTo(ConstantParameters.system_transliteration_kwd) == 0
                            || category.compareTo(ConstantParameters.system_referenceUri_kwd) == 0
                            || category.compareTo(ConstantParameters.system_referenceIdAttribute_kwd) == 0
                            ) {
                        continue;
                    } else {
                        appendVal+="<" + category + "/>";
                    }
                }
                appendVal+="</output>";
            }
            appendVal+="<terms count=\""+allTerms.size()+"\">";
            
            if(streamOutput){
                outStream.append(appendVal);
            }
            else{
                out.append(appendVal);
            }
            
            for (int i = 0; i < allTerms.size(); i++) {
                String targetTerm = allTerms.get(i);
                //Utils.StaticClass.webAppSystemOutPrintln(targetTerm);
                NodeInfoSortItemContainer targetTermInfo = termsInfo.get(targetTerm);
                String type = targetTermInfo.containerType;

                if (type.compareTo(NodeInfoSortItemContainer.CONTAINER_TYPE_UF) == 0) {//Perhaps it is uf link
                    
                    
                    //out.append("<ufterm index=\"" + (i + 1) + "\">");
                    //out.append("<ufname id=\"" + targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId() + "\">");
                    //out.append(escapeXML(targetTerm));
                    //out.append("</ufname>");

                    appendVal = "<ufterm index=\"" + (i + 1) + "\">"+"<ufname";
                    if(!skipIds){
                        appendVal += " id=\"" + targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId() + "\"";
                    }
                    appendVal +=">"+escapeXML(targetTerm)+"</ufname>";
                    
                    if(streamOutput){
                        outStream.append(appendVal);
                    }
                    else{
                        out.append(appendVal);
                    }
                    
                    ArrayList<SortItem> values = new ArrayList<SortItem>();
                    values.addAll(targetTermInfo.descriptorInfo.get("use"));
                    Collections.sort(values, sortComparator);

                    for (int k = 0; k < values.size(); k++) {
                        long valueIDL = values.get(k).getSysId();
                        
                        if (resultNodesIds.contains(valueIDL) && !skipIds) {
                            appendVal = "<use id=\"" + valueIDL + "\">";
                        } else {
                            appendVal = "<use>";
                        }
                        appendVal+=escapeXML(values.get(k).getLogName())+"</use>";
                        if(streamOutput){
                            outStream.append(appendVal);
                        }
                        else{
                            out.append(appendVal);
                        }
                    }

                    if(streamOutput){
                        outStream.append("</ufterm>");
                    }
                    else{
                        out.append("</ufterm>");
                    }
                    //out.append("</ufterm>");
                    continue;
                }
                
                long targetSysIdL = targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getSysId();
                long refIdL =  targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getThesaurusReferenceId();
                String transliteration =  targetTermInfo.descriptorInfo.get(ConstantParameters.id_kwd).get(0).getLogNameTransliteration();
                //out.append("<term index=\"" + (i + 1) + "\">");
                appendVal = "<term index=\"" + (i + 1) + "\">" +
                        "<descriptor";
                if(!skipIds){
                    appendVal+=" " + ConstantParameters.id_kwd+"=\"" + targetSysIdL + "\"";
                }
                /*
                if(refIdL>0){
                    appendVal+="\t\t\t<" + ConstantParameters.system_referenceUri_kwd + " "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+refIdL+"\">";
                    appendVal+=Utilities.escapeXML(consrtuctReferenceUri(selectedThesaurus.toUpperCase(), Utilities.ReferenceUriKind.TERM, refIdL));
                    appendVal+="</" + ConstantParameters.system_referenceUri_kwd + ">\r\n";
                }
                if(transliteration!=null && transliteration.length()>0){
                    appendVal+="\t\t\t<" + ConstantParameters.system_transliteration_kwd+">";
                    appendVal+=Utilities.escapeXML(transliteration);
                    appendVal+="</" + ConstantParameters.system_transliteration_kwd + ">\r\n";
                }
                */
                if(refIdL>0){
                    appendVal +=" "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+refIdL+"\"";
                    if(Parameters.ShowReferenceURIalso){
                        appendVal+=" "+ConstantParameters.system_referenceUri_kwd+"=\""+consrtuctReferenceUri(SessionUserInfo.selectedThesaurus.toUpperCase(), Utilities.ReferenceUriKind.TERM, refIdL)+"\"";
                    }
                 }
                /*
                 if(transliteration!=null && transliteration.length()>0){
                     appendVal +=" "+ConstantParameters.system_transliteration_kwd+"=\""+transliteration+"\""; 
                 }
                  */              
                                

                appendVal+=">"+escapeXML(targetTerm)+"</descriptor>";
                if(transliteration!=null && transliteration.length()>0){
                    appendVal+="<" + ConstantParameters.system_transliteration_kwd+">";
                    appendVal+=Utilities.escapeXML(transliteration);
                    appendVal+="</" + ConstantParameters.system_transliteration_kwd + ">";
                }

                //out.append("<descriptor id=\"" + targetSysIdL + "\" >");
                //out.append(escapeXML(targetTerm));
                //out.append("</descriptor>");
                if(streamOutput){
                    outStream.append(appendVal);
                }
                else{
                    out.append(appendVal);
                }

                for (int m = 0; m < output.size(); m++) {

                    String category = output.get(m);
                    if (category.compareTo(ConstantParameters.id_kwd) == 0 || category.compareTo(ConstantParameters.system_transliteration_kwd) == 0) {
                        continue;
                    }
                    /*
                    else if (category.compareTo(ConstantParameters.system_transliteration_kwd) == 0) {
                        if(streamOutput){
                            outStream.append("<" + category + ">"+escapeXML(transliteration)+"</" + category + ">");                            
                        }
                        else{
                            out.append("<" + category + ">"+escapeXML(transliteration)+"</" + category + ">");                            
                        }    
                        continue;
                    }
                    */
                    ArrayList<SortItem> values = new ArrayList<SortItem>();
                    values.addAll(targetTermInfo.descriptorInfo.get(category));
                    if ( (category.compareTo(ConstantParameters.nt_kwd) == 0  && sortNtsViaLinkClassFirst) || //no need to sort nts by link class as this xml will only be shown in table - simple list format
                            category.compareTo(ConstantParameters.translation_kwd) == 0 || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                        Collections.sort(values, guideTermComparator);
                    } else {
                        Collections.sort(values, sortComparator);
                    }

                    if (category.compareTo(ConstantParameters.facet_kwd) == 0
                            || category.compareTo(ConstantParameters.bt_kwd) == 0
                            || category.compareTo(ConstantParameters.nt_kwd) == 0
                            || category.compareTo(ConstantParameters.rbt_kwd) == 0
                            || category.compareTo(ConstantParameters.rnt_kwd) == 0
                            || category.compareTo(ConstantParameters.topterm_kwd) == 0
                            || category.compareTo(ConstantParameters.rt_kwd) == 0
                            || category.compareTo(ConstantParameters.uf_kwd) == 0
                            || category.compareTo(ConstantParameters.translation_kwd) == 0
                            || category.compareTo(ConstantParameters.uf_translations_kwd) == 0) {

                        if(values.size()>0){
                            for (int k = 0; k < values.size(); k++) {
                                ReferenceUriKind whatKind = (category.compareTo(ConstantParameters.facet_kwd) == 0) ? ReferenceUriKind.FACET : ReferenceUriKind.TERM;
                                
                                appendVal ="";
                                SortItem currentItem = values.get(k);
                                long valueIDL = currentItem.getSysId();
                                String linkClass = currentItem.getLinkClass();
                                
                                long linkRefId = currentItem.getThesaurusReferenceId();
                                String linkTransliteration = currentItem.getLogNameTransliteration();
                                
                                if (resultNodesIds.contains(valueIDL)) {
                                    if (linkClass == null || linkClass.length() == 0) {
                                        //out.append("<" + category + " id=\"" + valueIDL + "\" linkClass=\"\" >");
                                        appendVal += "<" + category + (skipIds?"":" id=\"" + valueIDL + "\"");// linkClass=\"\"";
                                    } else {
                                        //out.append("<" + category + " id=\"" + valueIDL + "\" linkClass=\"" + linkClass + "\">");
                                        appendVal += "<" + category + (skipIds?"":" id=\"" + valueIDL + "\"")+" linkClass=\"" + escapeXML(linkClass) + "\"";
                                    }                                    
                                    
                                } else {
                                    //Error in xslt transofrmation detected if linkClass attribute is missing (even if empty) in nt guide terms definitions
                                    if (linkClass == null || linkClass.length() == 0) {
                                        //out.append("<" + category + " linkClass=\"\">");
                                        appendVal += "<" + category + "";// linkClass=\"\"";
                                    } else {
                                        //out.append("<" + category + " linkClass=\"" + linkClass + "\">");
                                        appendVal += "<" + category + " linkClass=\"" + escapeXML(linkClass) + "\"";
                                    }
                                }
                                
                                if(linkRefId>0){
                                       appendVal +=" "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+linkRefId+"\"";
                                       if(Parameters.ShowReferenceURIalso){
                                            appendVal +=" " +ConstantParameters.system_referenceUri_kwd+"=\""+consrtuctReferenceUri(SessionUserInfo.selectedThesaurus.toUpperCase(), whatKind, linkRefId)+"\""; 
                                       }
                                }
                                if(Parameters.ShowTransliterationInAllXMLStream)
                                {
                                    if(linkTransliteration!=null && linkTransliteration.length()>0){
                                        appendVal +=" "+ConstantParameters.system_transliteration_kwd+"=\""+linkTransliteration+"\""; 
                                    }
                                }
                                
                                appendVal+=">";

                                //out.append(escapeXML(values.get(k).getLogName()));
                                //out.append("</" + category + ">");
                                appendVal +=escapeXML(currentItem.getLogName())+"</" + category + ">";
                                if(streamOutput){
                                    outStream.append(appendVal);
                                }
                                else{
                                    out.append(appendVal);
                                }
                            }
                        }
                        else{
                            //out.append("<" + category + "/>");
                            if(streamOutput){
                                outStream.append("<" + category + "/>");
                            }
                            else{
                                out.append("<" + category + "/>");
                            }
                        }
                    } else if (category.equals(ConstantParameters.translations_scope_note_kwd)) {
                        String checkStr = "";
                        for (SortItem valueSortItem :values) {
                            if (checkStr.length()>0) {
                                checkStr += "\n";
                            }
                            checkStr += valueSortItem.getLogName();
                        }
                        HashMap<String, String> trSns = this.getTranslationScopeNotes(checkStr);
                        ArrayList<String> langcodes = new ArrayList<String>(trSns.keySet());
                        Collections.sort(langcodes);
                        for (int k = 0; k < langcodes.size(); k++) {
                            String linkClass = langcodes.get(k);
                            String val = trSns.get(linkClass);
                            appendVal = "<" + category + " linkClass=\"" + escapeXML(linkClass) + "\">"+escapeXML(val)+"</" + category + ">";
                            if(streamOutput){
                                 outStream.append(appendVal);
                            }
                            else{
                                out.append(appendVal);
                            }
                        }


                    } else if(category.compareTo(ConstantParameters.status_kwd)==0){

                        for (SortItem valueSortItem : values) {
                            if(streamOutput){
                                outStream.append("<" + category + ">"+ escapeXML(Parameters.getStatusRepresentation_ForDisplay(valueSortItem.getLogName(), SessionUserInfo)) +"</" + category + ">");                            
                            }
                            else{
                                out.append("<" + category + ">"+escapeXML(Parameters.getStatusRepresentation_ForDisplay(valueSortItem.getLogName(), SessionUserInfo))+"</" + category + ">");                            
                            }                            
                        }

                    }
                    else {

                        for (SortItem valueSortItem : values) {
                            if(streamOutput){
                                outStream.append("<" + category + ">"+escapeXML(valueSortItem.getLogName())+"</" + category + ">");                            
                            }
                            else{
                                out.append("<" + category + ">"+escapeXML(valueSortItem.getLogName())+"</" + category + ">");                            
                            }                            
                        }

                    }
                }

                if(streamOutput){
                    outStream.append("</term>");
                }
                else{
                    out.append("</term>");
                }
                
            }
            if(streamOutput){
                outStream.append("</terms></data></page>");                
            }
            else{
                out.append("</terms></data></page>");
            }

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        if(!streamOutput){
            try {                
                out.close();
            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }
        //Q.reset_name_scope();
        Q.free_all_sets();

    }

    public String getSupportedTranslationsXML(QClass Q, TMSAPIClass TA, IntegerObject sis_session, String targetThesaurus, boolean startQueryAndConnection, String extraTranslationXML) {

        String returnXML = "";
        DBGeneral dbGen = new DBGeneral();
        HashMap<String, String> translationHash = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, targetThesaurus, null, startQueryAndConnection, true);
        returnXML = writeXMLTranslations(targetThesaurus, translationHash, extraTranslationXML);
        return returnXML;
    }

    public String writeXMLTranslations(String targetThesaurus, HashMap<String, String> translationHash, String extraTranslationXML) {
        String returnXML = "";
        returnXML += "<Translations thesaurus=\"" + targetThesaurus + "\" translationSeparator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">";

        if (translationHash != null) {
            Iterator<String> languagesEnumeration = translationHash.keySet().iterator();

            while (languagesEnumeration.hasNext()) {
                String languageWord = languagesEnumeration.next();
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
            if (common_utils.CreateDBbackup("BackUp_before_Automatic_BackUp_Restore", new StringObject(), new StringObject(), Parameters.UILang) == false) {
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
                    boolean restored = common_utils.RestoreDBbackup(BackUpName, new StringObject(), Parameters.UILang);
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

    public String translateFromMessagesXML(String messageXPath, String[] args, final String uiLang){
        //String uiLang = Parameters.UILang;
        ArrayList<String> argsVector = null;
        if(args!=null){            
            argsVector = new ArrayList<String>();
            int howmany = args.length;
            for(int i=0; i< howmany; i++){
                argsVector.add(args[i]);
            }
            
        }       
        String tagetMessageFullXPath = messageXPath + "/option[@lang=\"" + uiLang + "\"]";
        String str = translate(tagetMessageFullXPath, argsVector, Utilities.getXml_For_Messages());
        //if not supported display the english label
        if(str==null || str.trim().isEmpty()){
            tagetMessageFullXPath = messageXPath + "/option[@lang=\"en\"]";
            str = translate(tagetMessageFullXPath, argsVector, Utilities.getXml_For_Messages());
        }

        return str;
    }
    
    public String translateFromTranslationsXML(String messageXPath, String[] args, final String uiLang){
        
        ArrayList<String> argsVector = null;
        if(args!=null){            
            argsVector = new ArrayList<String>();
            int howmany = args.length;
            for(int i=0; i< howmany; i++){
                argsVector.add(args[i]);
            }
            
        }        
        String tagetMessageFullXPath = messageXPath + "/option[@lang=\"" + uiLang + "\"]";
        
        String str = translate(tagetMessageFullXPath, argsVector, Utilities.getTranslationsXml("translations.xml"));
        //if not supported display the english label
        if(str==null || str.trim().isEmpty()){
            tagetMessageFullXPath = messageXPath + "/option[@lang=\"en\"]";
            str = translate(tagetMessageFullXPath, argsVector, Utilities.getTranslationsXml("translations.xml"));
        }
        return str;
    }
    
    public String translateFromSaveAllLocaleAndScriptingXML(String messageXPath, String[] args, final String uiLang){
        
        ArrayList<String> argsVector = null;
        if(args!=null){            
            argsVector = new ArrayList<String>();
            int howmany = args.length;
            for(int i=0; i< howmany; i++){
                argsVector.add(args[i]);
            }            
        }        
        String tagetMessageFullXPath = messageXPath + "/option[@lang=\"" + uiLang + "\"]";
        String str = translate(tagetMessageFullXPath, argsVector, Utilities.getTranslationsXml("SaveAll_Locale_And_Scripting.xml"));
        //if not supported display the english label
        if(str==null || str.trim().isEmpty()){
            tagetMessageFullXPath = messageXPath + "/option[@lang=\"en\"]";
            str = translate(tagetMessageFullXPath, argsVector, Utilities.getTranslationsXml("SaveAll_Locale_And_Scripting.xml"));
        }
        return str;
    }
    
    
    public String translate(String messageXPath, ArrayList<String> args, String pathToErrorsXML) {

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
        String[] commonOutput = {
                ConstantParameters.bt_kwd, 
                ConstantParameters.nt_kwd, 
                ConstantParameters.rt_kwd,
                ConstantParameters.uf_kwd, 
                ConstantParameters.tc_kwd,
                ConstantParameters.translation_kwd, 
                ConstantParameters.status_kwd, 
                ConstantParameters.uf_translations_kwd,
                ConstantParameters.primary_found_in_kwd,
                ConstantParameters.translations_found_in_kwd,             
                ConstantParameters.created_by_kwd, 
                ConstantParameters.created_on_kwd, 
                ConstantParameters.modified_by_kwd,
                ConstantParameters.modified_on_kwd, 
                ConstantParameters.scope_note_kwd,
                ConstantParameters.translations_scope_note_kwd, 
                ConstantParameters.historical_note_kwd, 
                ConstantParameters.comment_kwd, 
                ConstantParameters.note_kwd, 
                ConstantParameters.system_referenceUri_kwd, 
                ConstantParameters.system_allHierarchicalUris_kwd, 
                ConstantParameters.system_transliteration_kwd,
                ConstantParameters.externalLink_kwd
            };
        
        if(Parameters.createSKOSHierarchicalUris){
            String[] output = new String[commonOutput.length+1];
            System.arraycopy(commonOutput, 0, output, 0, commonOutput.length);            
            output[commonOutput.length] = ConstantParameters.system_allHierarchicalUris_kwd;
                
            return output;
        }
        else{
            return commonOutput;
        }
    }


    public enum ReferenceUriKind {NONE,FACET,HIERARCHY,TOPTERM,TERM /*,SOURCE*/};

    public String consrtuctReferenceUri(String thesaurusName,ReferenceUriKind kind,long referenceId){
        String retVal ="";
        retVal="/"+thesaurusName+"/"+((kind == ReferenceUriKind.FACET) ? XMLHandling.WriteFileData.Skos_Facet :  XMLHandling.WriteFileData.Skos_Concept)+"/"+referenceId;
        
        
        /*
        switch(kind){
            case FACET:{
                retVal = "/"+Servlets.CardOf_Facet.class.getSimpleName()+"?"+ConstantParameters.system_referenceIdAttribute_kwd+"="+referenceId;//+"&external_user=ExternalReader&external_thesaurus="+thesaurusName.toUpperCase();//+"&mode=XMLSTREAM";
                break;
            }
            case HIERARCHY:{
                retVal = "/"+Servlets.CardOf_Term.class.getSimpleName()+"?"+ConstantParameters.system_referenceIdAttribute_kwd+"="+referenceId;//+"&external_user=ExternalReader&external_thesaurus="+thesaurusName.toUpperCase();//+"&mode=XMLSTREAM";
                break;
            }
            case TOPTERM:{
                retVal = "/"+Servlets.CardOf_Term.class.getSimpleName()+"?"+ConstantParameters.system_referenceIdAttribute_kwd+"="+referenceId;//+"&external_user=ExternalReader&external_thesaurus="+thesaurusName.toUpperCase();//+"&mode=XMLSTREAM";
                break;
            }
            case TERM:{
                retVal = "/"+Servlets.CardOf_Term.class.getSimpleName()+"?"+ConstantParameters.system_referenceIdAttribute_kwd+"="+referenceId;//+"&external_user=ExternalReader&external_thesaurus="+thesaurusName.toUpperCase();//+"&mode=XMLSTREAM";
                break;
            }
            default:{
                break;
            }
        }*/
        /*
        if(thesaurusName!=null && thesaurusName.length()>0){
            retVal += thesaurusName+"/";
        }
        
        switch(kind){
            
            case FACET:{
                
                retVal += "Facet/";
                break;
            }
            case HIERARCHY:{
                if(thesaurusName!=null && thesaurusName.length()>0){
                    retVal += thesaurusName+"/";
                }
                retVal += "Hierarchy/";
                break;
            }
            case TERM:{
                if(thesaurusName!=null && thesaurusName.length()>0){
                    retVal += thesaurusName+"/";
                }
                retVal += "Term/";
                break;
            }
//            
//            Source might raise issues during merge thesauri operations.
//            It is not unique per thesaurus -> Thus logicalname may have 
//            different 
//            case SOURCE:{
//                retVal += "Source/";
//                break;
//            }
//            
            default:{
                break;
            }
            
            
        }
        */
        //retVal += String.format("%07d", referenceId);
        
        if(Parameters.ExternalURIsReplacements.size()>0){
            for(Map.Entry<String, String> entry : Parameters.ExternalURIsReplacements.entrySet()){
                if(retVal.startsWith(entry.getKey())){
                    retVal = retVal.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        return escapeXML(retVal);
    }
    
    public static String getXml_For_Messages() {
        return Utilities.getTranslationsXml("Messages.xml");
    }

    public static String getXml_For_ConsistencyChecks() {
        return Utilities.getTranslationsXml("Consistencies_Error_Codes.xml");
    }
    
    public static String getXml_For_SaveAll_Locale_And_Scripting() {
        return Utilities.getTranslationsXml("SaveAll_Locale_And_Scripting.xml");
    }
    
    public static String getTranslationsXml(String fileName) {
        return Parameters.BaseRealPath + File.separator + "translations" + File.separator + fileName;
    }
    
    public static String getTransliterationString(String originalLogicalNameWithoutPrefix, boolean removePrefix){
        String transliterationString = "";
        if(removePrefix){
            if (originalLogicalNameWithoutPrefix.contains("`")) {
                transliterationString = originalLogicalNameWithoutPrefix.substring(originalLogicalNameWithoutPrefix.indexOf("`") + 1);
            } else {
                transliterationString = originalLogicalNameWithoutPrefix;
            }
        }
        else{
            transliterationString = originalLogicalNameWithoutPrefix;
        }
        
        if(Parameters.TransliterationsToLowerCase){
            transliterationString = transliterationString.toLowerCase();
        }
        //if apache remove accents needs some changes these can be applies by useing these filters before it is applies 
        for (String key : Parameters.TransliterationsReplacements.keySet()) {
            String replaceRegEx = "["+key+"]";
            String replaceWith = Parameters.TransliterationsReplacements.get(key);

            transliterationString = transliterationString.replaceAll(replaceRegEx, replaceWith);
        }
        
        //apache remove accents 
        transliterationString = org.apache.commons.lang3.StringUtils.stripAccents(transliterationString);
        //System.out.println(transliterationString +" --> "+ org.apache.commons.lang3.StringUtils.stripAccents(transliterationString));
        
        //normalize gaps keep just one if multiple defined
        transliterationString = transliterationString.replaceAll(" +", " ");
        
        
        return transliterationString;
    }
    
    public static long retrieveDatabaseIdFromNodeInfoStringContainer(NodeInfoStringContainer container){
        long retVal = -1;
        
        if(container!=null && container.descriptorInfo.containsKey(ConstantParameters.id_kwd)){
            ArrayList<String> vals = container.descriptorInfo.get(ConstantParameters.id_kwd);
            if(vals!=null && vals.size()>0){
                String valStr = vals.get(0);
                if(valStr!=null && valStr.trim().length()>0 && valStr.trim().compareTo("-1")!=0 && valStr.trim().compareTo("0")!=0 ){
                    try{
                        retVal = Long.parseLong(valStr);
                    }
                    catch(Exception ex){
                        Utils.StaticClass.handleException(ex);
                    }
                }
            }
        }
        
        return retVal>0 ? retVal:-1;
    }
    
    public static long retrieveThesaurusReferenceFromNodeInfoStringContainer(NodeInfoStringContainer container){
        long retVal = -1;
        
        if(container!=null && container.descriptorInfo.containsKey(ConstantParameters.system_referenceUri_kwd)){
            ArrayList<String> vals = container.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd);
            if(vals!=null && vals.size()>0){
                String valStr = vals.get(0);
                if(valStr!=null && valStr.trim().length()>0 && valStr.trim().compareTo("-1")!=0 && valStr.trim().compareTo("0")!=0 ){
                    try{
                        retVal = Long.parseLong(valStr);
                    }
                    catch(Exception ex){
                        Utils.StaticClass.handleException(ex);
                    }
                }
            }
        }
        
        return retVal>0 ? retVal:-1;
    }
    
    public static long retrieveThesaurusReferenceFromNodeInfoSortItemContainer(NodeInfoSortItemContainer container){
        long retVal = -1;
        
        if(container!=null && container.descriptorInfo.containsKey(ConstantParameters.id_kwd)){
            ArrayList<SortItem> vals = container.descriptorInfo.get(ConstantParameters.id_kwd);
            if(vals!=null && vals.size()>0 && vals.get(0)!=null){
                SortItem valSortItem = vals.get(0);
                retVal = valSortItem.getThesaurusReferenceId();                
            }
        }
        
        return retVal>0 ? retVal:-1;
    }
    
    public static String retrieveTransliterationStringFromNodeInfoStringContainer(NodeInfoStringContainer container, String originalName, boolean removePrefix){
        String retVal = "";
        
        if(container!=null && container.descriptorInfo.containsKey(ConstantParameters.system_transliteration_kwd)){
            ArrayList<String> vals = container.descriptorInfo.get(ConstantParameters.system_transliteration_kwd);
            if(vals!=null && vals.size()>0){
                String valStr = vals.get(0);
                if(valStr!=null && valStr.trim().length()>0 ){
                    retVal = valStr.trim();
                }
                else{
                    retVal = Utilities.getTransliterationString(originalName, removePrefix);
                }
            }
        }
        
        return retVal;
    }
    
    public static String retrieveTransliterationStringFromNodeInfoSortItemContainer(NodeInfoSortItemContainer container, String originalName, boolean removePrefix){
        String retVal = "";
        
        if(container!=null && container.descriptorInfo.containsKey(ConstantParameters.id_kwd)){
            ArrayList<SortItem> vals = container.descriptorInfo.get(ConstantParameters.id_kwd);
            if(vals!=null && vals.size()>0 && vals.get(0)!=null){
                
                SortItem valSortItem = vals.get(0);
                String valStr = valSortItem.getLogNameTransliteration();
                if(valStr!=null && valStr.trim().length()>0 ){
                    retVal = valStr.trim();
                }
                else{
                    retVal = Utilities.getTransliterationString(originalName, removePrefix);
                }
            }
        }
        
        return retVal;
    }
    
    public static ArrayList<String> getStringVectorFromSortItemVector(ArrayList<SortItem> v){
        
        ArrayList<String>  returnResults = new ArrayList<String>();
        if(v!=null){
            for(SortItem item : v){
                returnResults.add(item.getLogName());
            }
        }
        return returnResults;
    }
    
    public static ArrayList<SortItem> getSortItemVectorFromStringVector(ArrayList<String> v, boolean removeTransliterationPrefix){
        
        ArrayList<SortItem>  returnResults = new ArrayList<SortItem>();
        if(v!=null){
            for(String item : v){
                returnResults.add(new SortItem( item,-1,Utilities.getTransliterationString(item, removeTransliterationPrefix),-1));
            }
        }
        return returnResults;
    }
    
    
    public static ArrayList<SortItem> getSortItemVectorFromTermsInfo(HashMap<String, NodeInfoStringContainer> termsInfo, boolean removeTransliterationPrefix){
    
        ArrayList<SortItem>  returnResults = new ArrayList<SortItem>();
        if(termsInfo!=null){
            Iterator<String> termEnum = termsInfo.keySet().iterator();
            while(termEnum.hasNext()){
                String termName = termEnum.next();
                NodeInfoStringContainer targetInfo = termsInfo.get(termName);
                long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(targetInfo);
                String transliteration = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(targetInfo, termName, removeTransliterationPrefix);
                long id = -1;//retrieveDatabaseIdFromNodeInfoStringContainer(targetInfo);
                returnResults.add(new SortItem(termName,id,transliteration,refId));
            }
        }        
        
        return returnResults;
    }
    
    public static ArrayList<SortItem> getSortItemVectorFromTermsInfoSortItemContainer(HashMap<String, NodeInfoSortItemContainer> termsInfo, boolean removeTransliterationPrefix){
    
        ArrayList<SortItem>  returnResults = new ArrayList<>();
        if(termsInfo!=null){
            Iterator<String> termEnum = termsInfo.keySet().iterator();
            while(termEnum.hasNext()){
                String termName = termEnum.next();
                NodeInfoSortItemContainer targetInfo = termsInfo.get(termName);
                long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoSortItemContainer(targetInfo);
                String transliteration = Utilities.retrieveTransliterationStringFromNodeInfoSortItemContainer(targetInfo, termName, removeTransliterationPrefix);
                long id = -1;//retrieveDatabaseIdFromNodeInfoStringContainer(targetInfo);
                returnResults.add(new SortItem(termName,id,transliteration,refId));
            }
        }        
        
        return returnResults;
    }
    
    
    public static ArrayList<SortItem> getSortItemVectorFromStringVectorAndTermsInfo(ArrayList<String> filterVector, HashMap<String, NodeInfoStringContainer> termsInfo, boolean removeTransliterationPrefix){
    
        ArrayList<SortItem>  returnResults = new ArrayList<SortItem>();
        if(termsInfo!=null && filterVector!=null){
            Iterator<String> termEnum = termsInfo.keySet().iterator();
            while(termEnum.hasNext()){
                String termName = termEnum.next();
                if(filterVector.contains(termName)){
                    NodeInfoStringContainer targetInfo = termsInfo.get(termName);
                    long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(targetInfo);
                    String transliteration = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(targetInfo, termName, removeTransliterationPrefix);
                    long id = -1;//retrieveDatabaseIdFromNodeInfoStringContainer(targetInfo);
                    returnResults.add(new SortItem(termName,id,transliteration,refId));
                }
            }
        }        
        
        return returnResults;
    }
    
    public HashMap<String, NodeInfoStringContainer> getStringContainerFromSortItemContainer(HashMap<String, NodeInfoSortItemContainer> sortItemContainer){
        HashMap<String, NodeInfoStringContainer> retVal = new HashMap<>();
        
        if(sortItemContainer!=null){
            
            Iterator<String> termEnum = sortItemContainer.keySet().iterator();
            
            while(termEnum.hasNext()){
                String termName = termEnum.next();
                NodeInfoSortItemContainer targetInfo = sortItemContainer.get(termName);
                
                String[] output = targetInfo.descriptorInfo.keySet().toArray(new String[targetInfo.descriptorInfo.keySet().size()]);
                int index = -1;
                for(int i=0; i<output.length; i++){
                    if(output[i].compareTo(ConstantParameters.id_kwd)==0){
                        index = i;
                        break;
                    }
                }
                
                if(index>=0){
                    output[index] = ConstantParameters.system_referenceUri_kwd;
                }
                
                NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                for(String str : output){
                    
                    ArrayList<SortItem> vals = null; 
                    if(str.compareTo(ConstantParameters.system_referenceUri_kwd)==0){
                        vals = targetInfo.descriptorInfo.get(ConstantParameters.id_kwd);
                        newContainer.descriptorInfo.get(str).add(""+vals.get(0).thesarurusReferenceId);
                    }
                    else{
                        vals = targetInfo.descriptorInfo.get(str);
                        if(str.compareTo(ConstantParameters.translation_kwd)==0 || str.compareTo(ConstantParameters.uf_translations_kwd)==0){
                            for(SortItem si : vals){
                                newContainer.descriptorInfo.get(str).add(si.linkClass +Parameters.TRANSLATION_SEPERATOR + si.log_name);
                            }
                        }
                        else{
                            for(SortItem si : vals){
                                newContainer.descriptorInfo.get(str).add(si.log_name);
                            }
                        }
                    }
                    
                    
                }
                retVal.put(termName, newContainer);
            }
        }
        return retVal;
    }
    
    public String getSkosExportConceptScheme(HttpServletRequest request, String thesaurus){
        String baseWebAppLocation = guessBaseHref(request)+"#"+thesaurus;
        if(Parameters.ExternalURIsReplacements.size()>0){
            for(Map.Entry<String, String> entry : Parameters.ExternalURIsReplacements.entrySet()){
                if(baseWebAppLocation.startsWith(entry.getKey())){
                    baseWebAppLocation = baseWebAppLocation.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        return baseWebAppLocation;
        
    }
    
    public String getSkosBaseName(HttpServletRequest request, String thesaurus){
        String baseWebAppLocation = guessBaseHref(request) +"/"+thesaurus;
        if(Parameters.ExternalURIsReplacements.size()>0){
            for(Map.Entry<String, String> entry : Parameters.ExternalURIsReplacements.entrySet()){
                if(baseWebAppLocation.startsWith(entry.getKey())){
                    baseWebAppLocation = baseWebAppLocation.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        return baseWebAppLocation;
        
    }
    
    public void updateUserNamesWithDescription(HttpServletRequest request, 
            ArrayList<String> output, 
            String exportScheme,
            HashMap<String, NodeInfoStringContainer> termsStrInfo,
            HashMap<String, NodeInfoSortItemContainer> termsInfo){
        
        if(exportScheme.equals(ConstantParameters.xmlschematype_skos) && Parameters.SkosReplaceLoginNamesWithDescription){
            if(output.contains(ConstantParameters.created_by_kwd) || 
                    output.contains(ConstantParameters.modified_by_kwd)){

                HashMap<String,String> knownUserNames = new HashMap<>();

                Iterator<String> termsEnumaration = termsInfo!=null ? termsInfo.keySet().iterator() : termsStrInfo.keySet().iterator();
                while (termsEnumaration.hasNext()) {
                    String termName = termsEnumaration.next();
                    if(termsInfo!=null){
                        if(termsInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.created_by_kwd)){
                            for(SortItem si : termsInfo.get(termName).descriptorInfo.get(ConstantParameters.created_by_kwd)){
                                if(!knownUserNames.containsKey(si.log_name)){
                                    knownUserNames.put(si.log_name, si.log_name);
                                }
                            }
                        }

                        if(termsInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.modified_by_kwd)){
                            for(SortItem si : termsInfo.get(termName).descriptorInfo.get(ConstantParameters.modified_by_kwd)){
                                if(!knownUserNames.containsKey(si.log_name)){
                                    knownUserNames.put(si.log_name, si.log_name);
                                }
                            }
                        }     
                    }
                    else{
                        if(termsStrInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.created_by_kwd)){
                            for(String si : termsStrInfo.get(termName).descriptorInfo.get(ConstantParameters.created_by_kwd)){
                                if(!knownUserNames.containsKey(si)){
                                    knownUserNames.put(si, si);
                                }
                            }
                        }

                        if(termsStrInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.modified_by_kwd)){
                            for(String si : termsStrInfo.get(termName).descriptorInfo.get(ConstantParameters.modified_by_kwd)){
                                if(!knownUserNames.containsKey(si)){
                                    knownUserNames.put(si, si);
                                }
                            }
                        } 
                    }

                }

                //check if user description has been defined
                UsersClass wtmsUsers = new UsersClass();
                Set<String> userKeys = knownUserNames.keySet();
                for(String str : userKeys){
                    UserInfoClass userClass = wtmsUsers.SearchTMSUser(request, str);
                    if(userClass.description!=null && userClass.description.trim().length()>0){
                        knownUserNames.put(str, userClass.description.trim());
                    }
                }

                Iterator<String> termsEnum = termsInfo!=null ? termsInfo.keySet().iterator() : termsStrInfo.keySet().iterator();
                while (termsEnum.hasNext()) {
                    String termName = termsEnum.next();
                    if(termsInfo!=null){
                        if(termsInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.created_by_kwd)){
                            for(SortItem si : termsInfo.get(termName).descriptorInfo.get(ConstantParameters.created_by_kwd)){
                                if(knownUserNames.containsKey(si.log_name)){
                                    si.log_name = knownUserNames.get(si.log_name);
                                }
                            }
                        }
                        if(termsInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.modified_by_kwd)){
                            for(SortItem si : termsInfo.get(termName).descriptorInfo.get(ConstantParameters.modified_by_kwd)){
                                if(knownUserNames.containsKey(si.log_name)){
                                    si.log_name = knownUserNames.get(si.log_name);
                                }
                            }
                        }
                    }
                    else{
                        if(termsStrInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.created_by_kwd)){
                            for(String str : knownUserNames.keySet()){                            
                                Collections.replaceAll(termsStrInfo.get(termName).descriptorInfo.get(ConstantParameters.created_by_kwd), str, knownUserNames.get(str));                            
                            }
                            /*
                            for(SortItem si : termsStrInfo.get(termName).descriptorInfo.get(ConstantParameters.created_by_kwd)){
                                if(knownUserNames.containsKey(si.log_name)){
                                    si.log_name = knownUserNames.get(si.log_name);
                                }
                            }*/
                        }
                        if(termsInfo.get(termName).descriptorInfo.containsKey(ConstantParameters.modified_by_kwd)){
                            for(String str : knownUserNames.keySet()){                            
                                Collections.replaceAll(termsStrInfo.get(termName).descriptorInfo.get(ConstantParameters.modified_by_kwd), str, knownUserNames.get(str));                            
                            }
                            /*for(SortItem si : termsInfo.get(termName).descriptorInfo.get(ConstantParameters.modified_by_kwd)){
                                if(knownUserNames.containsKey(si.log_name)){
                                    si.log_name = knownUserNames.get(si.log_name);
                                }
                            }*/
                        }
                    }
                }
            }
        }
    }
    
}


