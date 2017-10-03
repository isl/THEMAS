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
package Servlets;

import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.DBFilters;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Utils.SortItem;
import Utils.TaxonomicCodeItem;
import Utils.TaxonomicCodeComparator;
import Utils.StringLocaleComparator;
import Utils.NodeInfoSortItemContainer;
import Utils.SortItemComparator;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import java.util.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class hierarchysTermsShortcuts extends ApplicationBasicServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String outputMode = request.getParameter("answerType");
        if (outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
            response.setContentType("text/xml;charset=UTF-8");
        } else {
            response.setContentType("text/html;charset=UTF-8");
        }

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();

        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                //response.sendRedirect("Index");
                return;
            }

            DBGeneral dbGen = new DBGeneral();

            // open SIS and TMS connection
            QClass Q = new QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            // timer begin
            long startTime = Utilities.startTimer();

            Utilities u = new Utilities();

            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            
            String hierarchy = u.getDecodedParameterValue(request.getParameter("hierarchy"));
            String refIdVal = request.getParameter("referenceId");
            String action = u.getDecodedParameterValue(request.getParameter("action"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            
            if(hierarchy==null || hierarchy.trim().length()==0){
                        
                if(refIdVal!=null && refIdVal.length()>0){
                    long refId = -1;
                    try{
                        refId = Long.parseLong(refIdVal);
                    }
                    catch(NumberFormatException ex){
                        Utils.StaticClass.handleException(ex);
                    }

                    if(refId>0){
                        Q.reset_name_scope();
                        hierarchy = dbGen.removePrefix(Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus, refId));
                    }
                }
            }
            
            String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            //StringObject cls = new StringObject();
            //StringObject label = new StringObject();
            //CMValue cmv = new CMValue();
            StringObject hierarchyObj = null;
            if (hierarchy != null && hierarchy.length() > 0) {
                hierarchyObj = new StringObject(prefix_class.concat(hierarchy));
            }

            //save ALL code                
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;

            String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/" + webAppSaveResults_Folder);
            Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
            String webAppSaveResults_temporary_filesAbsolutePath = webAppSaveResults_AbsolutePath.resolve(webAppSaveResults_temporary_files_Folder).toString();
            String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
            String time = Utilities.GetNow();

            if (action.compareTo("translations2primary") == 0 || action.compareTo("primary2translations") == 0) {

                //<editor-fold defaultstate="collapsed" desc="Primary 2 Translations Index display computations...">
                StringObject TranslationsClassObj = new StringObject();
                StringObject TranslationsLinkObj = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, TranslationsClassObj, TranslationsLinkObj, Q, sis_session);
                Q.reset_name_scope();


                /*
                 int index = Parameters.CLASS_SET.indexOf("TERM");
                 String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
                 SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);
                 */
                //int set_hiers_terms = dbGen.get_Instances_Set(DescriptorClasses,Q,sis_session);
                Q.set_current_node(hierarchyObj);
                int set_hiers_terms = Q.get_all_instances(0);
                Q.reset_set(set_hiers_terms);
                //int card0 = Q.set_get_card(set_hiers_terms);

                // FILTER terms set depending on user group
                DBFilters dbf = new DBFilters();
                set_hiers_terms = dbf.FilterTermsResults(SessionUserInfo, set_hiers_terms, Q, sis_session);

                HashMap<String, ArrayList<SortItem>> term_translationsOfHierDesciptor = new HashMap<String, ArrayList<SortItem>>();
                HashMap<SortItem, ArrayList<String>> translations_termsOfHierDesciptor = new HashMap<SortItem, ArrayList<String>>();

                //NOW SET_HIERS_TERMS CONTAINS ALL HIERARCHY TERMS FILTERED
                ArrayList<String> all_hier_terms_vec = dbGen.get_Node_Names_Of_Set(set_hiers_terms, true, Q, sis_session);
                Collections.sort(all_hier_terms_vec, new StringLocaleComparator(targetLocale));

                for (int i = 0; i < all_hier_terms_vec.size(); i++) {
                    ArrayList<SortItem> translationsVec = new ArrayList<SortItem>();
                    term_translationsOfHierDesciptor.put(all_hier_terms_vec.get(i), translationsVec);
                }

                int set_translations_links = Q.get_link_from_by_category(set_hiers_terms, TranslationsClassObj, TranslationsLinkObj);
                Q.reset_set(set_translations_links);

                Q.reset_set(set_translations_links);
                int set_terms_with_translation = Q.get_from_value(set_translations_links);
                Q.reset_set(set_terms_with_translation);

                /*
                 Q.reset_set(set_translations_links);
                 int set_translations = Q.get_to_value(set_translations_links);
                 Q.reset_set(set_translations);
                 */
                //findout terms without translation
                Q.set_difference(set_hiers_terms, set_terms_with_translation);
                Q.reset_set(set_hiers_terms);
                ArrayList<String> all_terms_without_translations = dbGen.get_Node_Names_Of_Set(set_hiers_terms, true, Q, sis_session);
                Collections.sort(all_terms_without_translations, new StringLocaleComparator(targetLocale));
                translations_termsOfHierDesciptor.put(new SortItem("-", -1, ""), all_terms_without_translations);

                ArrayList<SortItem> linkValue = new ArrayList<SortItem>();

                //retrieve translations distinct
                //StringObject fromcls = new StringObject();
                //StringObject categ = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();
                //IntegerObject traversed = new IntegerObject();
                Q.reset_set(set_translations_links);
                int translationSubStringLength = ConstantParameters.thesaursTranslationCategorysubString.length();
                ArrayList<Return_Full_Link_Row> retFLVals = new ArrayList<Return_Full_Link_Row>();
                if (Q.bulk_return_full_link(set_translations_links, retFLVals) != QClass.APIFail) {
                    for (Return_Full_Link_Row row : retFLVals) {
                        //while (Q.retur_full_link(set_translations_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                        String term = dbGen.removePrefix(row.get_v1_cls());
                        String translation = dbGen.removePrefix(row.get_v5_cmv().getString());
                        String subCategory = row.get_v3_categ().substring(row.get_v3_categ().indexOf(ConstantParameters.thesaursTranslationCategorysubString) + translationSubStringLength);
                        SortItem translationSortItem = new SortItem(translation, -1, subCategory);

                        if (linkValue.contains(translationSortItem) == false) {
                            linkValue.add(translationSortItem);
                        }
                        ArrayList<SortItem> othertranslations = term_translationsOfHierDesciptor.get(term);

                        if (othertranslations != null && othertranslations.contains(translationSortItem) == false) {
                            term_translationsOfHierDesciptor.get(term).add(translationSortItem);
                            //ensOfHierDesciptor.put(term, otherEns);
                        }

                        ArrayList<String> otherTerms = translations_termsOfHierDesciptor.get(translationSortItem);
                        if (otherTerms != null) {
                            if (otherTerms.contains(term) == false) {
                                translations_termsOfHierDesciptor.get(translationSortItem).add(term);
                            }
                        } else {
                            ArrayList<String> newTermsVector = new ArrayList<String>();
                            newTermsVector.add(term);
                            translations_termsOfHierDesciptor.put(translationSortItem, newTermsVector);
                        }
                    }
                }
                /*
                 while (Q.retur_full_link(set_translations_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

                 String term = dbGen.removePrefix(cls.getValue());
                 String translation = dbGen.removePrefix(cmv.getString());
                 String subCategory = categ.getValue().substring(categ.getValue().indexOf(ConstantParameters.thesaursTranslationCategorysubString)+ translationSubStringLength);
                 SortItem translationSortItem = new SortItem(translation,-1,subCategory);

                 if(linkValue.contains(translationSortItem)==false){
                 linkValue.add(translationSortItem);
                 }
                 ArrayList<SortItem> othertranslations = term_translationsOfHierDesciptor.get(term);

                 if (othertranslations != null && othertranslations.contains(translationSortItem) == false) {
                 term_translationsOfHierDesciptor.get(term).add(translationSortItem);
                 //ensOfHierDesciptor.put(term, otherEns);
                 }

                 ArrayList<String> otherTerms = translations_termsOfHierDesciptor.get(translationSortItem);
                 if (otherTerms != null) {
                 if(otherTerms.contains(term) == false){
                 translations_termsOfHierDesciptor.get(translationSortItem).add(term);
                 }
                 }
                 else{
                 ArrayList<String> newTermsVector = new ArrayList<String>();
                 newTermsVector.add(term);
                 translations_termsOfHierDesciptor.put(translationSortItem,newTermsVector);
                 }
                 }
                 */

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                String title = /*"<base>Index of terms belonging to hierarchy: </base>"+*/ "<arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>";
                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Index_" + time;
                String baseURL = sessionInstance.path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/";

                writePrimary2TranslationsIndexResultsInXMLFile(all_hier_terms_vec,
                        linkValue,
                        term_translationsOfHierDesciptor,
                        translations_termsOfHierDesciptor,
                        time,
                        title,
                        //webAppSaveResults_temporary_filesAbsolutePath + File.separator + 
                        Save_Results_file_name //                + ".xml"
                        ,
                        sis_session,
                        Q,
                        baseURL,
                        webAppSaveResults_AbsolutePath.toString(),
                        webAppSaveResults_temporary_filesAbsolutePath,
                        pathToSaveScriptingAndLocale, targetLocale);
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in Hierarchy terms PrimarytoTranslations and TranslationstoPrimary: " + elapsedTimeSec);

                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat("_primary2translations.html"));
                out.flush();
                return;
                //</editor-fold>

            } else if (action.compareTo("alphabetical") == 0) {

                //<editor-fold defaultstate="collapsed" desc="alphabetical display computations..."> 
                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Alphabetical_" + time;
                String XML = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";
                String pathToLabels = context.getRealPath("/translations/labels.xml");

                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Alphabetical.xsl").toString();
                String resultsInfo = "<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\"><title>" + time + "</title><query>"
                        //+ "<base>Alphabetical display of terms of Hierarchy: </base>"
                        + "<arg1>" + Utilities.escapeXML(hierarchy)
                        + "</arg1>" + "</query>" + u.getXMLUserInfo(SessionUserInfo) + "<pathToLabels>" + pathToLabels
                        + "</pathToLabels>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale
                        + "</pathToSaveScriptingAndLocale>";
                //Output required for alphabetical
                ArrayList<String> output = new ArrayList<String>();
                output.add(ConstantParameters.id_kwd);
                output.add(ConstantParameters.system_referenceUri_kwd);
                output.add(ConstantParameters.system_transliteration_kwd);
                output.add(ConstantParameters.tc_kwd);
                output.add(ConstantParameters.translation_kwd);
                output.add(ConstantParameters.scope_note_kwd);
                output.add(ConstantParameters.translations_scope_note_kwd);
                output.add(ConstantParameters.topterm_kwd);
                output.add(ConstantParameters.bt_kwd);
                output.add(ConstantParameters.nt_kwd);
                output.add(ConstantParameters.rt_kwd);
                output.add(ConstantParameters.uf_kwd);
                output.add(ConstantParameters.uf_translations_kwd);
                output.add(ConstantParameters.primary_found_in_kwd);
                output.add(ConstantParameters.translations_found_in_kwd);
                output.add("use");
                if (Parameters.CreatorInAlphabeticalTermDisplay == true) {
                    output.add(ConstantParameters.created_by_kwd);
                }

                //Storage Structures
                HashMap<String, NodeInfoSortItemContainer> termsInfo = new HashMap<String, NodeInfoSortItemContainer>();
                ArrayList<Long> resultNodesIdsL = new ArrayList<Long>();
                ArrayList<String> allTerms = new ArrayList<String>();

                Q.reset_name_scope();
                Q.set_current_node(hierarchyObj);
                int set_hiers_terms = Q.get_all_instances(0);
                Q.reset_set(set_hiers_terms);
                DBFilters dbf = new DBFilters();
                set_hiers_terms = dbf.FilterTermsResults(SessionUserInfo, set_hiers_terms, Q, sis_session);

                //READ RESULT SET'S REQUESTED OUTPUT AND WRITE RESULTS IN XML FILE
                dbGen.collectTermSetInfo(SessionUserInfo, Q, TA, sis_session, set_hiers_terms, output, termsInfo, allTerms, resultNodesIdsL);
                dbGen.collectUsedForTermSetInfo(SessionUserInfo, Q, sis_session, set_hiers_terms, termsInfo, allTerms, resultNodesIdsL);
                Collections.sort(allTerms, new StringLocaleComparator(targetLocale));

                //Write XML file
                u.writeResultsInXMLFile(null, allTerms, resultsInfo, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session, termsInfo, resultNodesIdsL, targetLocale, SessionUserInfo.selectedThesaurus, false, false);

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                //make html transformation
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml",
                        XSL,
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));

                //respond with html file name in order to be downloaded from DownloadFile.java Servlet
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in Hierarchy terms Alphabetical: " + elapsedTimeSec);

                out.flush();
                return;
                //</editor-fold>
            } else if (action.compareTo("systematic") == 0) {

                //<editor-fold defaultstate="collapsed" desc="systematic display computations..."> 
                Q.reset_name_scope();
                Q.set_current_node(hierarchyObj);
                int set_hiers_terms = Q.get_all_instances(0);
                Q.reset_set(set_hiers_terms);

                // FILTER terms set depending on user group
                DBFilters dbf = new DBFilters();
                set_hiers_terms = dbf.FilterTermsResults(SessionUserInfo, set_hiers_terms, Q, sis_session);

                ArrayList<TaxonomicCodeItem> descriptors = new ArrayList<TaxonomicCodeItem>();
                //Get All dewey codes from set_global_descriptor_results set as TaxonomicCodeItems. Terms without
                //dewey or without a valid dewey xxx.yyy.zzz. .... will be considered as without having that dewey at all.
                dbGen.collectResultsTaxonomicCodes(SessionUserInfo.selectedThesaurus, Q, sis_session, set_hiers_terms, descriptors, ".");

                Collections.sort(descriptors, new TaxonomicCodeComparator(targetLocale));

                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Systematic_" + time;
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Systematic.xsl").toString();
                //String XSL = "<?xml-stylesheet type=\"text/xsl\" href=\"" + path + "/" + webAppSaveResults_Folder + "/SaveAll_Terms_Systematic.xsl" + "\"?>" ;
                //String webAppSaveResults_AbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                writeSystematicResultsInXMLFile(descriptors,
                        u,
                        time,
                        //"<base>Systematic display of terms belonging to hierarchy: </base>"+
                        "<arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>",
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml",
                        pathToSaveScriptingAndLocale);

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml",
                        XSL,
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));

                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));

                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in Hierarchy terms Systematic: " + elapsedTimeSec);

                //out.println(path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name + ".xml");
                out.flush();
                return;
                //</editor-fold>

            } else if (action.compareTo("hierarchical") == 0) {
                //<editor-fold defaultstate="collapsed" desc="hierarchical display computations..."> 

                StringObject BT_NTClassObj = new StringObject();
                StringObject BT_NTLinkObj = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.nt_kwd, BT_NTClassObj, BT_NTLinkObj, Q, sis_session);
                Q.reset_name_scope();

                Q.set_current_node(hierarchyObj);
                int set_all_hier_terms = Q.get_all_instances(0);
                Q.reset_set(set_all_hier_terms);

                //find all bt/nt relations among terms of current hierarchy
                int set_bt_labels_from = Q.get_link_from_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_from);

                int set_bt_labels_to = Q.get_link_to_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_to);

                //this should preserve all bt links established between terms of the selected hierarchy
                Q.set_intersect(set_bt_labels_from, set_bt_labels_to);
                Q.reset_set(set_bt_labels_from);

                // FILTER bt links set depending on user group
                DBFilters dbf = new DBFilters();
                set_bt_labels_from = dbf.FilterBTLinksSet(SessionUserInfo, set_bt_labels_from, Q, sis_session);

                HashMap<String, ArrayList<String>> ntsOfHierDesciptor = new HashMap<String, ArrayList<String>>();

                ArrayList<String> newTopTermNtsVec = new ArrayList<String>();
                String topterm = dbGen.removePrefix(hierarchyObj.getValue());
                ntsOfHierDesciptor.put(topterm, newTopTermNtsVec);

                ArrayList<String> allHierTermsVec = new ArrayList<String>();
                ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
                Q.reset_set(set_bt_labels_from);
                if (Q.bulk_return_link(set_bt_labels_from, retVals) != QClass.APIFail) {
                    for (Return_Link_Row row : retVals) {
                        //while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {

                        String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                        String nt = dbGen.removePrefix(row.get_v1_cls());
                        if (allHierTermsVec.contains(bt) == false) {
                            allHierTermsVec.add(bt);
                        }
                        if (allHierTermsVec.contains(nt) == false) {
                            allHierTermsVec.add(nt);
                        }
                        ArrayList<String> btNts = ntsOfHierDesciptor.get(bt);
                        if (btNts == null) {
                            ArrayList<String> newNtsVec = new ArrayList<String>();
                            newNtsVec.add(nt);
                            ntsOfHierDesciptor.put(bt, newNtsVec);
                        } else {
                            if (btNts.contains(nt) == false) {

                                btNts.add(nt);
                                ntsOfHierDesciptor.put(bt, btNts);
                            }
                        }

                        if (ntsOfHierDesciptor.containsKey(nt) == false) {
                            ArrayList<String> newNtsVec = new ArrayList<String>();
                            ntsOfHierDesciptor.put(nt, newNtsVec);
                        }
                    }
                }
                /*
                 while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {

                 String bt = dbGen.removePrefix(cmv.getString());
                 String nt = dbGen.removePrefix(cls.getValue());
                 if (allHierTermsVec.contains(bt) == false) {
                 allHierTermsVec.add(bt);
                 }
                 if (allHierTermsVec.contains(nt) == false) {
                 allHierTermsVec.add(nt);
                 }                    
                 ArrayList<String> btNts = ntsOfHierDesciptor.get(bt);
                 if (btNts == null) {
                 ArrayList<String> newNtsVec = new ArrayList<String>();
                 newNtsVec.add(nt);
                 ntsOfHierDesciptor.put(bt, newNtsVec);
                 } else {
                 if (btNts.contains(nt) == false) {

                 btNts.add(nt);
                 ntsOfHierDesciptor.put(bt, btNts);
                 }
                 }

                 if (ntsOfHierDesciptor.containsKey(nt) == false) {
                 ArrayList<String> newNtsVec = new ArrayList<String>();
                 ntsOfHierDesciptor.put(nt, newNtsVec);
                 }


                 }
                 */
                Collections.sort(allHierTermsVec, new StringLocaleComparator(targetLocale));

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                String Save_Results_file_name1 = "SearchResults_Terms_Of_Hierarchy_Hierarchical_Typical_" + time;
                String Save_Results_file_name2 = "SearchResults_Terms_Of_Hierarchy_Hierarchical_Tree_" + time;
                //String xslLink = "<?xml-stylesheet type=\"text/xsl\" href=\"" + path + "/" + webAppSaveResults_Folder + "/SaveAll_Terms_Of_Hierarchy_Hierarchical.xsl" + "\"?>" ;
                String XSL1 = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Of_Hierarchy_Hierarchical.xsl").toString();
                String XSL2 = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Of_Hierarchy_Hierarchical_tree_view.xsl").toString();

                String typicalLocation = Save_Results_file_name1.concat(".html");
                String treeLocation = Save_Results_file_name2.concat(".html");
                writeHierarchicalResultsInXMLFile(allHierTermsVec, ntsOfHierDesciptor, hierarchy, u,
                        time, "<typicalHierarchicalLocation>" + typicalLocation + "</typicalHierarchicalLocation>"
                        + "<treeHierarchicalLocation>" + treeLocation + "</treeHierarchicalLocation>"
                        //+ "<base>Herarchical Presentation of terms of hierarchy: </base>"
                        + "<arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>",
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml", pathToSaveScriptingAndLocale, targetLocale);

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml",
                        XSL1,
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1.concat(".html"));

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml",
                        XSL2,
                        webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name2.concat(".html"));

                //response.sendRedirect("DownloadFile?targetFile="+ Save_Results_file_name1.concat(".html"));
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name1.concat(".html"));

                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in Hierarchy terms Hierarchical: " + elapsedTimeSec);
                //out.println(path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name );
                out.flush();
                return;
                //</editor-fold>    
            } else if (action.compareTo("GlobalThesarusHierarchical") == 0 || action.compareTo("facethierarchical") == 0) {
                //DISPLAY FACETS AND HIERARCHIES
                //<editor-fold defaultstate="collapsed" desc="GlobalThesarusHierarchical display computations..."> 

                ArrayList<String> facetNames = new ArrayList<>();
                boolean facetViewInsteadOfThesaurus = false;
                if (action.compareTo("facethierarchical") == 0) {
                    facetNames.add(hierarchy);
                    facetViewInsteadOfThesaurus = true;
                } else {
                    facetNames.addAll(dbGen.getFacets(SessionUserInfo.selectedThesaurus, Q, sis_session, targetLocale));
                }

                HashMap<SortItem, ArrayList<SortItem>> facetToHierarhchies = new HashMap<SortItem, ArrayList<SortItem>>();
                ArrayList<SortItem> hierarchyNames = new ArrayList<SortItem>();
                ArrayList<SortItem> allTermsVec = new ArrayList<SortItem>();
                HashMap<SortItem, ArrayList<SortItem>> ntsOfHierDesciptor = new HashMap<SortItem, ArrayList<SortItem>>();

                String classPrefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

                CMValue facetCmv = new CMValue();
                for (String fName : facetNames) {

                    Q.reset_name_scope();
                    //retrieve fname
                    if (Q.set_current_node_and_retrieve_Cmv(new StringObject(classPrefix.concat(fName)), facetCmv) != QClass.APIFail) {

                        SortItem fsortItem = new SortItem(facetCmv);
                        fsortItem.setLogName(dbGen.removePrefix(fsortItem.getLogName()));
                        fsortItem.sysid = -1;

                        ArrayList<SortItem> facetHiers = dbGen.returnResults_FacetInSortItems(SessionUserInfo, fName, "hierarchy", Q, sis_session, targetLocale);
                        for (SortItem item : facetHiers) {
                            item.sysid = -1;
                        }
                        facetToHierarhchies.put(fsortItem, facetHiers);

                        for (SortItem hierName : facetHiers) {
                            if (hierarchyNames.contains(hierName) == false) {
                                hierarchyNames.add(hierName);
                            }
                        }
                    }
                }

                StringObject BT_NTClassObj = new StringObject();
                StringObject BT_NTLinkObj = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.nt_kwd, BT_NTClassObj, BT_NTLinkObj, Q, sis_session);

                for (SortItem hierarchySortItem : hierarchyNames) {

                    StringObject currentHierarchyObj = new StringObject(prefix_class.concat(hierarchySortItem.getLogName()));
                    Q.reset_name_scope();
                    Q.set_current_node(currentHierarchyObj);
                    int set_all_hier_terms = Q.get_all_instances(0);
                    Q.reset_set(set_all_hier_terms);

                    //find all bt/nt relations among terms of current hierarchy
                    int set_bt_labels_from = Q.get_link_from_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                    Q.reset_set(set_bt_labels_from);

                    int set_bt_labels_to = Q.get_link_to_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                    Q.reset_set(set_bt_labels_to);

                    //this should preserve all bt links established between terms of the selected hierarchy
                    Q.set_intersect(set_bt_labels_from, set_bt_labels_to);
                    Q.reset_set(set_bt_labels_from);

                    // FILTER bt links set depending on user group
                    DBFilters dbf = new DBFilters();
                    set_bt_labels_from = dbf.FilterBTLinksSet(SessionUserInfo, set_bt_labels_from, Q, sis_session);

                    ArrayList<SortItem> newTopTermNtsVec = new ArrayList<>();
                    //String topterm = dbGen.removePrefix(currentHierarchyObj.getValue());
                    ntsOfHierDesciptor.put(hierarchySortItem, newTopTermNtsVec);

                    ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
                    Q.reset_set(set_bt_labels_from);
                    if (Q.bulk_return_link(set_bt_labels_from, retVals) != QClass.APIFail) {
                        for (Return_Link_Row row : retVals) {
                            //while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {

                            //String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                            SortItem btSortItem = new SortItem(row.get_v3_cmv());
                            btSortItem.setLogName(dbGen.removePrefix(btSortItem.getLogName()));
                            btSortItem.sysid = -1;

                            SortItem ntSortItem = new SortItem(dbGen.removePrefix(row.get_v1_cls()), -1, row.get_v4_clsTransliteration(), row.get_v5_clsRefid());
                            //String nt = dbGen.removePrefix(row.get_v1_cls());
                            if (allTermsVec.contains(btSortItem) == false) {
                                allTermsVec.add(btSortItem);
                            }
                            if (allTermsVec.contains(ntSortItem) == false) {
                                allTermsVec.add(ntSortItem);
                            }

                            ArrayList<SortItem> btNts = ntsOfHierDesciptor.get(btSortItem);
                            if (btNts == null) {
                                ArrayList<SortItem> newNtsVec = new ArrayList<SortItem>();
                                newNtsVec.add(ntSortItem);
                                ntsOfHierDesciptor.put(btSortItem, newNtsVec);
                            } else {
                                if (btNts.contains(ntSortItem) == false) {

                                    btNts.add(ntSortItem);
                                    ntsOfHierDesciptor.put(btSortItem, btNts);
                                }
                            }

                            if (ntsOfHierDesciptor.containsKey(ntSortItem) == false) {
                                ArrayList<SortItem> newNtsVec = new ArrayList<>();
                                ntsOfHierDesciptor.put(ntSortItem, newNtsVec);
                            }
                        }
                    }

                }
                Collections.sort(allTermsVec, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                String Save_Results_file_name1 = (facetViewInsteadOfThesaurus ? "Facet_Hierarchical_View_" : "Thesaurus_Global_Hierarchical_View_") + time;
                String Save_Results_file_name2 = (facetViewInsteadOfThesaurus ? "Facet_Tree_View_" : "Thesaurus_Global_Tree_View_") + time;
                String XSL1 = webAppSaveResults_AbsolutePath.resolve("Thesaurus_Global_Hierarchical_View.xsl").toString();
                String XSL2 = webAppSaveResults_AbsolutePath.resolve("Thesaurus_Global_Tree_View.xsl").toString();

                String typicalLocation = Save_Results_file_name1.concat(".html");
                String treeLocation = Save_Results_file_name2.concat(".html");
                //HashMap<String,ArrayList<String>> facetToHierarhchies = new HashMap<String,ArrayList<String>>();
                //ArrayList<String> hierarchyNames = new ArrayList<String>();
                //ArrayList<String> allHierTermsVec = new ArrayList<String>();
                if (outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
                    writeThesarusGlobalViewResultsInXMLFile(out, facetToHierarhchies,
                            allTermsVec,
                            ntsOfHierDesciptor,
                            facetViewInsteadOfThesaurus,
                            SessionUserInfo.selectedThesaurus,
                            hierarchy,
                            u,
                            time,
                            "<typicalHierarchicalLocation>" + typicalLocation + "</typicalHierarchicalLocation>"
                            + "<treeHierarchicalLocation>" + treeLocation + "</treeHierarchicalLocation>",
                            webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml", pathToSaveScriptingAndLocale, targetLocale);

                } else {
                    writeThesarusGlobalViewResultsInXMLFile(null, facetToHierarhchies,
                            allTermsVec,
                            ntsOfHierDesciptor,
                            facetViewInsteadOfThesaurus,
                            SessionUserInfo.selectedThesaurus,
                            hierarchy,
                            u,
                            time,
                            "<typicalHierarchicalLocation>" + typicalLocation + "</typicalHierarchicalLocation>"
                            + "<treeHierarchicalLocation>" + treeLocation + "</treeHierarchicalLocation>",
                            webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml", pathToSaveScriptingAndLocale, targetLocale);

                    u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml",
                            XSL1,
                            webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1.concat(".html"));

                    u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name1 + ".xml",
                            XSL2,
                            webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name2.concat(".html"));
                    
                    out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name1.concat(".html"));
                }
                
                
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in Hierarchy terms GlobalThesarus View: " + elapsedTimeSec);
                //out.println(path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name );
                out.flush();
            } else {
                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            }

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            if (outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) != 0) {
                out.close();
            }
            
            if (outputMode == null || outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) != 0) {
                sessionInstance.writeBackToSession(session);
            }
        }
    }

    public void writeThesarusGlobalViewResultsInXMLFile(PrintWriter outStream,
            HashMap<SortItem, ArrayList<SortItem>> facetHiers,
            ArrayList<SortItem> all_hier_terms_vec,
            HashMap<SortItem, ArrayList<SortItem>> ntsOfHierDesciptor,
            boolean facetInstradOfThesaurus,
            String targetThesaurus,
            String targetFacet,
            Utilities u,
            String title,
            String query,
            String fileName,
            String pathToSaveScriptingAndLocale,
            Locale targetLocale) {

        boolean streamOutput = false;
        if (outStream != null) {
            streamOutput = true;
        }

        String Full_Save_Results_file_name = fileName;
        OutputStreamWriter out = null;

        String appendVal = ConstantParameters.xmlHeader
                + "<page thesaurus=\""+targetThesaurus+"\" language=\"" + Parameters.UILang + "\">\n"
                + (facetInstradOfThesaurus ? ("\t<targetFacet>" + targetFacet + "</targetFacet>\n") : ("\t<targetThesaurus>" + targetThesaurus + "</targetThesaurus>\n"))
                + "\t<title>" + title + "</title>\n" +
                (streamOutput? "": ("\t<query>\n" + query + "\t</query>\n\t<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>\n") )+
                "<results>\n";
        if (streamOutput) {
            outStream.append(appendVal);
        } else {

            try {
                //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");

                out.write(appendVal);
            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }

        try {

            ArrayList<SortItem> facets = new ArrayList<SortItem>(facetHiers.keySet());
            SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
            Collections.sort(facets, transliterationComparator);
            for (SortItem facet : facets) {
                appendVal = "\t<topterm>\r\n\t\t<name";
                
                if (facet.getThesaurusReferenceId() > 0) {
                    appendVal+=" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + facet.getThesaurusReferenceId() + "\"";
                }
                appendVal+=">" + Utilities.escapeXML(facet.getLogName());
                appendVal+="</name>\r\n";
                
                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }

                ArrayList<SortItem> hierarhies = facetHiers.get(facet);
                Collections.sort(hierarhies, transliterationComparator);
                for (SortItem hierarchy : hierarhies) {
                    appendVal = "\t\t<nt";
                    if (hierarchy.getThesaurusReferenceId() > 0) {
                        appendVal+=" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + hierarchy.getThesaurusReferenceId() + "\"";
                    }
                    appendVal+=">" + Utilities.escapeXML(hierarchy.getLogName());
                    appendVal+="</nt>\r\n";
                    if (streamOutput) {
                        outStream.append(appendVal);
                    } else {
                        out.append(appendVal);
                    }
                }
                
                if (streamOutput) {
                    outStream.append("\t</topterm>\r\n");
                } else {
                    out.append("\t</topterm>\r\n");
                }
            }

            for (SortItem termItem : all_hier_terms_vec) {

                ArrayList<SortItem> termNts = new ArrayList<>();
                termNts.addAll(ntsOfHierDesciptor.get(termItem));
                if (!termNts.isEmpty()) {
                    Collections.sort(termNts, transliterationComparator);
                }

                appendVal = "\t<term>\r\n\t\t<name";
                if (termItem.getThesaurusReferenceId() > 0) {
                    appendVal +=" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + termItem.getThesaurusReferenceId() + "\"";
                }
                appendVal +=">" + Utilities.escapeXML(termItem.getLogName());
                appendVal +="</name>\r\n";

                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }

                for (SortItem ntItem : termNts) {
                    appendVal = "\t\t<nt";
                    
                    if (ntItem.getThesaurusReferenceId() > 0) {
                        appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + ntItem.getThesaurusReferenceId() + "\"";
                    }
                    appendVal += ">" + Utilities.escapeXML(ntItem.getLogName());
                    appendVal += "</nt>\r\n";
                     if (streamOutput) {
                        outStream.append(appendVal);
                    } else {
                        out.append(appendVal);
                    }

                }

                 if (streamOutput) {
                    outStream.append("\t</term>\r\n");
                } else {
                    out.append("\t</term>\r\n");
                }
            }

             if (streamOutput) {
                outStream.append("\t</results>\r\n</page>");
            } else {
                out.append("\t</results>\r\n</page>");
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

    }

    public String writePrimary2TranslationsIndexResultsInXMLFile(ArrayList<String> all_hier_terms_vec,
            ArrayList<SortItem> all_translations_vec,
            HashMap<String, ArrayList<SortItem>> term_translationsOfHierDesciptor,
            HashMap<SortItem, ArrayList<String>> translations_termsOfHierDesciptor,
            String title,
            String query,
            String fileNameWithoutExtension,
            IntegerObject sis_session,
            QClass Q,
            String baseURL,
            String xslBasePath,
            String temporaryfolderFullPath,
            String pathToSaveScriptingAndLocale, Locale targetLocale) {

        SortItemComparator linkClassTransliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME);

        String Save_Results_file_nameWithoutExtension = fileNameWithoutExtension; //webAppSaveResults_temporary_filesAbsolutePath +"/"+ Save_Results_file_name + ".xml";
        Utilities u = new Utilities();

        // \\ stands for regex escape not for windows path separator
        String Primary2TranslationsHtmlOutputPath = Save_Results_file_nameWithoutExtension.concat("_primary2translations.html");
        String Translations2PrimaryHtmlOutputPath = Save_Results_file_nameWithoutExtension.concat("_translations2primary.html");

        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(temporaryfolderFullPath + File.separator + Save_Results_file_nameWithoutExtension.concat(".xml"));
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            //out.write(xml_header);
            //out.write(xslLink);
            // Fw = new FileWriter(Save_Results_file_nameWithoutExtension);
            out.write("<page language='" + Parameters.UILang + "'>\n"
                    + "<title>" + title + "</title>"
                    + "<query>" + query + "</query>"
                    + "<primary2transaltionsLocation>" + baseURL.concat(Primary2TranslationsHtmlOutputPath) + "</primary2transaltionsLocation>"
                    + "<transaltions2primaryLocation>" + baseURL.concat(Translations2PrimaryHtmlOutputPath) + "</transaltions2primaryLocation>"
                    + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("<results translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">");

            out.write("<primary2transaltions>");
            int all_hier_terms_vecSize = all_hier_terms_vec.size();

            Collections.sort(all_hier_terms_vec, new StringLocaleComparator(targetLocale));

            for (int i = 0; i < all_hier_terms_vecSize; i++) {

                String term = all_hier_terms_vec.get(i);
                ArrayList<SortItem> termTranslations = new ArrayList<SortItem>();
                termTranslations.addAll(term_translationsOfHierDesciptor.get(term));
                //Collections.sort(termTranslations, new GuideTermSortItemComparator(targetLocale));
                Collections.sort(termTranslations, linkClassTransliterationComparator);

                out.write("<term>");

                out.write("<name>");
                out.write(Utilities.escapeXML(term));
                out.write("</name>");

                out.write("<" + ConstantParameters.translation_kwd + ">");
                for (int j = 0; j < termTranslations.size(); j++) {
                    out.write("<translation linkClass=\"" + termTranslations.get(j).linkClass + "\">");
                    out.write(Utilities.escapeXML(termTranslations.get(j).log_name));
                    out.write("</translation>");
                }
                out.write("</" + ConstantParameters.translation_kwd + ">");

                out.write("</term>");

            }
            out.write("</primary2transaltions>");

            out.write("<translations2primary>");

            ArrayList<String> noTranslationTerms = new ArrayList<String>();
            noTranslationTerms.addAll(translations_termsOfHierDesciptor.get(new SortItem("-", -1, "")));
            Collections.sort(noTranslationTerms, new StringLocaleComparator(targetLocale));

            out.write("<" + ConstantParameters.translation_kwd + ">");
            out.write("<name>-</name>");
            for (int j = 0; j < noTranslationTerms.size(); j++) {
                out.write("<term>");
                out.write(Utilities.escapeXML(noTranslationTerms.get(j)));

                out.write("</term>");

            }
            out.write("</" + ConstantParameters.translation_kwd + ">");

            int all_translations_vecSize = all_translations_vec.size();
            //Collections.sort(all_translations_vec,new GuideTermSortItemComparator(targetLocale));
            Collections.sort(all_translations_vec, linkClassTransliterationComparator);

            for (int i = 0; i < all_translations_vecSize; i++) {

                SortItem translation = all_translations_vec.get(i);
                ArrayList<String> primaryTerms = new ArrayList<String>();
                primaryTerms.addAll(translations_termsOfHierDesciptor.get(translation));
                Collections.sort(primaryTerms, new StringLocaleComparator(targetLocale));

                out.write("<" + ConstantParameters.translation_kwd + ">");

                out.write("<name linkClass=\"" + translation.linkClass + "\">");
                out.write(Utilities.escapeXML(translation.log_name));
                out.write("</name>");

                for (int j = 0; j < primaryTerms.size(); j++) {
                    out.write("<term>");
                    out.write(Utilities.escapeXML(primaryTerms.get(j)));
                    out.write("</term>");
                }

                out.write("</" + ConstantParameters.translation_kwd + ">");

            }
            out.write("</translations2primary>");

            out.write("</results>");

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

        String primary2translationsXSLPath = xslBasePath.concat(File.separator + "Primary2TranslationsIndex.xsl");
        String translations2primaryXSLPath = xslBasePath.concat(File.separator + "Translations2PrimaryIndex.xsl");

        //XML STORED IN Save_Results_file_nameWithoutExtension
        //XSL STORED IN xslLink
        u.XmlFileTransform(temporaryfolderFullPath + File.separator + Save_Results_file_nameWithoutExtension.concat(".xml"),
                primary2translationsXSLPath,
                temporaryfolderFullPath + File.separator + Primary2TranslationsHtmlOutputPath);
        u.XmlFileTransform(temporaryfolderFullPath + File.separator + Save_Results_file_nameWithoutExtension.concat(".xml"),
                translations2primaryXSLPath,
                temporaryfolderFullPath + File.separator + Translations2PrimaryHtmlOutputPath);

        //default index
        return temporaryfolderFullPath + File.separator + Primary2TranslationsHtmlOutputPath;
    }

    public void writeSystematicResultsInXMLFile(ArrayList<TaxonomicCodeItem> allTerms, Utilities u, String title, String query,/*String xslLink,*/ String fileName, String pathToSaveScriptingAndLocale) {

        String Full_Save_Results_file_name = fileName;

        OutputStreamWriter out = null;
        try {

            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            out.write(ConstantParameters.xmlHeader);
            //out.write(xslLink);
            out.write("<page title=\"" + title + "\" language='" + Parameters.UILang + "' mode=\"insert\">");

            out.write("<query>");
            out.write(query);
            out.write("</query>");

            out.write("<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

            for (int i = 0; i < allTerms.size(); i++) {

                String dewey = "";

                for (int k = 0; k < allTerms.get(i).codeParts.size(); k++) {

                    dewey += allTerms.get(i).codeParts.get(k);
                    if (k < allTerms.get(i).codeParts.size() - 1) {

                        dewey += ".";
                    }

                }

                out.write("<term position=\"");
                out.write(String.valueOf(i + 1));
                out.write("\">\n");

                out.write("<tc>");
                out.write(dewey);
                out.write("</tc>\n");

                out.write("<name>");

                out.write(Utilities.escapeXML(allTerms.get(i).nodeName));
                out.write("</name>\n");

                out.write("</term>\n");

            }

            out.write("</page>\n");

            out.close();

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

    }

    public void writeHierarchicalResultsInXMLFile(ArrayList<String> all_hier_terms_vec, HashMap<String, ArrayList<String>> ntsOfHierDesciptor, String hierarchy, Utilities u, String title, String query,/*String xslLink,*/ String fileName, String pathToSaveScriptingAndLocale, Locale targetLocale) {

        String Full_Save_Results_file_name = fileName;

        OutputStreamWriter out = null;
        try {
            //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            out.write(ConstantParameters.xmlHeader);
            //out.write(xslLink);
            // Fw = new FileWriter(Save_Results_file_nameWithoutExtension);
            out.write("<page language=\"" + Parameters.UILang + "\">\n"
                    + "<title>" + title + "</title>"
                    + //"<query>" + "Hierarchical presentation of terms of hierarchy: " + hierarchy + "</query>" +
                    "<query>" + query + "</query>"
                    + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("<results>");
            out.write("<topterm>");

            out.write("<name>");
            out.write(Utilities.escapeXML(hierarchy));
            out.write("</name>");

            ArrayList<String> toptermNts = ntsOfHierDesciptor.get(hierarchy);
            Collections.sort(toptermNts, new StringLocaleComparator(targetLocale));

            for (int i = 0; i < toptermNts.size(); i++) {

                out.write("<nt>");
                out.write(Utilities.escapeXML(toptermNts.get(i)));
                out.write("</nt>");

            }
            out.write("</topterm>");

            ntsOfHierDesciptor.remove(hierarchy);
            all_hier_terms_vec.remove(hierarchy);

            int all_hier_terms_vecSize = all_hier_terms_vec.size();
            for (int i = 0; i < all_hier_terms_vecSize; i++) {
                String term = all_hier_terms_vec.get(i);
                ArrayList<String> termNts = new ArrayList<String>();
                termNts.addAll(ntsOfHierDesciptor.get(term));
                if (termNts.size() > 0) {
                    Collections.sort(termNts, new StringLocaleComparator(targetLocale));
                }

                out.write("<term>");

                out.write("<name>");
                out.write(Utilities.escapeXML(term));
                out.write("</name>");

                for (int k = 0; k < termNts.size(); k++) {

                    out.write("<nt>");
                    out.write(Utilities.escapeXML(termNts.get(k)));
                    out.write("</nt>");

                }

                out.write("</term>");
            }

            out.write("</results>");
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

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
