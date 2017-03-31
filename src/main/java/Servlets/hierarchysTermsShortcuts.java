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
import Utils.GuideTermSortItemComparator;
import Utils.NodeInfoSortItemContainer;
import java.io.*;
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

    final String xml_header = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        
        
        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null) {
                out.println("Session Invalidate");
                //response.sendRedirect("Index");
                return;
            }

            

            DBGeneral dbGen = new DBGeneral();
            
            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            // timer begin
            long startTime = Utilities.startTimer();  
            
            Utilities u = new Utilities();
            
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            String hierarchy = u.getDecodedParameterValue(request.getParameter("hierarchy"));
            String action = u.getDecodedParameterValue(request.getParameter("action"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);            
            String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());


            //StringObject cls = new StringObject();
            //StringObject label = new StringObject();
            //CMValue cmv = new CMValue();


            StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchy));

            //save ALL code                
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;

            String webAppSaveResults_AbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);
            String webAppSaveResults_temporary_filesAbsolutePath = webAppSaveResults_AbsolutePath + File.separator + webAppSaveResults_temporary_files_Folder;
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


                Hashtable<String, Vector<SortItem>> term_translationsOfHierDesciptor = new Hashtable<String, Vector<SortItem>>();
                Hashtable<SortItem, Vector<String>> translations_termsOfHierDesciptor = new Hashtable<SortItem, Vector<String>>();
                
                //NOW SET_HIERS_TERMS CONTAINS ALL HIERARCHY TERMS FILTERED
                Vector<String> all_hier_terms_vec = dbGen.get_Node_Names_Of_Set(set_hiers_terms, true, Q, sis_session);
                Collections.sort(all_hier_terms_vec, new StringLocaleComparator(targetLocale));

                for (int i = 0; i < all_hier_terms_vec.size(); i++) {
                    Vector<SortItem> translationsVec = new Vector<SortItem>();
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
                Vector<String> all_terms_without_translations = dbGen.get_Node_Names_Of_Set(set_hiers_terms, true, Q, sis_session);
                Collections.sort(all_terms_without_translations, new StringLocaleComparator(targetLocale));
                translations_termsOfHierDesciptor.put(new SortItem("-",-1,""), all_terms_without_translations);

                Vector<SortItem> linkValue = new Vector<SortItem>();
                

                //retrieve translations distinct
                //StringObject fromcls = new StringObject();
                //StringObject categ = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();
                //IntegerObject traversed = new IntegerObject();

                

                Q.reset_set(set_translations_links);
                int translationSubStringLength = ConstantParameters.thesaursTranslationCategorysubString.length();
                Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
                if(Q.bulk_return_full_link(set_translations_links, retFLVals)!=QClass.APIFail){
                    for(Return_Full_Link_Row row: retFLVals){
                        //while (Q.retur_full_link(set_translations_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                        String term = dbGen.removePrefix(row.get_v1_cls());
                        String translation = dbGen.removePrefix(row.get_v5_cmv().getString());
                        String subCategory = row.get_v3_categ().substring(row.get_v3_categ().indexOf(ConstantParameters.thesaursTranslationCategorysubString)+ translationSubStringLength);
                        SortItem translationSortItem = new SortItem(translation,-1,subCategory);

                        if(linkValue.contains(translationSortItem)==false){
                            linkValue.add(translationSortItem);
                        }
                        Vector<SortItem> othertranslations = term_translationsOfHierDesciptor.get(term);

                        if (othertranslations != null && othertranslations.contains(translationSortItem) == false) {
                            term_translationsOfHierDesciptor.get(term).add(translationSortItem);
                            //ensOfHierDesciptor.put(term, otherEns);
                        }

                        Vector<String> otherTerms = translations_termsOfHierDesciptor.get(translationSortItem);
                        if (otherTerms != null) {
                            if(otherTerms.contains(term) == false){
                                translations_termsOfHierDesciptor.get(translationSortItem).add(term);
                            }
                        }
                        else{
                            Vector<String> newTermsVector = new Vector<String>();
                            newTermsVector.add(term);
                            translations_termsOfHierDesciptor.put(translationSortItem,newTermsVector);
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
                    Vector<SortItem> othertranslations = term_translationsOfHierDesciptor.get(term);

                    if (othertranslations != null && othertranslations.contains(translationSortItem) == false) {
                        term_translationsOfHierDesciptor.get(term).add(translationSortItem);
                        //ensOfHierDesciptor.put(term, otherEns);
                    }

                    Vector<String> otherTerms = translations_termsOfHierDesciptor.get(translationSortItem);
                    if (otherTerms != null) {
                        if(otherTerms.contains(term) == false){
                            translations_termsOfHierDesciptor.get(translationSortItem).add(term);
                        }
                    }
                    else{
                        Vector<String> newTermsVector = new Vector<String>();
                        newTermsVector.add(term);
                        translations_termsOfHierDesciptor.put(translationSortItem,newTermsVector);
                    }
                }
                */

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);



                String title = "<base>Ευρετήριο όρων της ιεραρχίας: </base><arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>";
                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Index_" + time;
                String baseURL =sessionInstance.path +  "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/";
                writePrimary2TranslationsIndexResultsInXMLFile(all_hier_terms_vec,linkValue, term_translationsOfHierDesciptor,translations_termsOfHierDesciptor, time, title, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml", sis_session, Q, baseURL, webAppSaveResults_AbsolutePath, pathToSaveScriptingAndLocale,targetLocale);
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in Hierarchy terms PrimarytoTranslations and TranslationstoPrimary: " + elapsedTimeSec);
                

                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat("_primary2translations.html"));
                out.flush();
                return;
            //</editor-fold>

            } else if (action.compareTo("alphabetical") == 0) {

                //<editor-fold defaultstate="collapsed" desc="alphabetical display computations..."> 
                
                
                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Alphabetical_" + time;
                String XML = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";
                String pathToLabels = context.getRealPath("/translations/labels.xml");
                
                
                String XSL = webAppSaveResults_AbsolutePath.concat("/SaveAll_Terms_Alphabetical.xsl");
                String resultsInfo = "<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\"><title>" + time +"</title><query>"
                        + "<base>Αλφαβητική παρουσίαση όρων της ιεραρχίας: </base><arg1>" + Utilities.escapeXML(hierarchy)
                        + "</arg1>" +"</query>"+ u.getXMLUserInfo(SessionUserInfo) + "<pathToLabels>" + pathToLabels
                        +"</pathToLabels>" + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale
                        +"</pathToSaveScriptingAndLocale>" ;
                //Output required for alphabetical
                Vector<String> output = new Vector<String>();
                output.add("id");
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
                if(Parameters.CreatorInAlphabeticalTermDisplay == true){
                    output.add(ConstantParameters.created_by_kwd);
                }
                
                //Storage Structures
                Hashtable<String, NodeInfoSortItemContainer> termsInfo = new Hashtable<String, NodeInfoSortItemContainer>();              
                Vector<Long> resultNodesIdsL = new Vector<Long>();
                Vector<String> allTerms = new Vector<String>();                

                
                Q.reset_name_scope();
                Q.set_current_node(hierarchyObj);
                int set_hiers_terms = Q.get_all_instances(0);
                Q.reset_set(set_hiers_terms);
                DBFilters dbf = new DBFilters();
                set_hiers_terms = dbf.FilterTermsResults(SessionUserInfo, set_hiers_terms, Q, sis_session);
                
                //READ RESULT SET'S REQUESTED OUTPUT AND WRITE RESULTS IN XML FILE
                dbGen.collectTermSetInfo(SessionUserInfo,Q, TA, sis_session, set_hiers_terms, output, termsInfo, allTerms, resultNodesIdsL);
                dbGen.collectUsedForTermSetInfo(SessionUserInfo, Q, sis_session, set_hiers_terms, termsInfo, allTerms , resultNodesIdsL);
                Collections.sort(allTerms, new StringLocaleComparator(targetLocale));         
                
                //Write XML file
                u.writeResultsInXMLFile(allTerms, resultsInfo, output, webAppSaveResults_temporary_filesAbsolutePath,  Save_Results_file_name, Q, sis_session ,termsInfo,resultNodesIdsL,targetLocale);
                
                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                //make html transformation
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml", XSL, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name.concat(".html"));

                //respond with html file name in order to be downloaded from DownloadFile.java Servlet
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in Hierarchy terms Alphabetical: " + elapsedTimeSec);
                
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

                Vector<TaxonomicCodeItem> descriptors = new Vector<TaxonomicCodeItem>();
                //Get All dewey codes from set_global_descriptor_results set as TaxonomicCodeItems. Terms without
                //dewey or without a valid dewey xxx.yyy.zzz. .... will be considered as without having that dewey at all.
                dbGen.collectResultsTaxonomicCodes(SessionUserInfo.selectedThesaurus,Q, sis_session,set_hiers_terms, descriptors, ".");

                Collections.sort(descriptors, new TaxonomicCodeComparator(targetLocale));

                String Save_Results_file_name = "SearchResults_Terms_Of_Hierarchy_Systematic_" + time;
                String XSL = webAppSaveResults_AbsolutePath.concat("/SaveAll_Terms_Systematic.xsl");
                //String XSL = "<?xml-stylesheet type=\"text/xsl\" href=\"" + path + "/" + webAppSaveResults_Folder + "/SaveAll_Terms_Systematic.xsl" + "\"?>" ;
                //String webAppSaveResults_AbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);


                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                writeSystematicResultsInXMLFile(descriptors, u, time, "<base>Συστηματική παρουσίαση όρων της ιεραρχίας: </base><arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>", webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml",pathToSaveScriptingAndLocale);
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name + ".xml", XSL, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name.concat(".html"));

                
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in Hierarchy terms Systematic: " + elapsedTimeSec);
                
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

                Hashtable<String, Vector<String>> ntsOfHierDesciptor = new Hashtable<String, Vector<String>>();

                Vector<String> newTopTermNtsVec = new Vector<String>();
                String topterm = dbGen.removePrefix(hierarchyObj.getValue());
                ntsOfHierDesciptor.put(topterm, newTopTermNtsVec);

                Vector<String> allHierTermsVec = new Vector<String>();
                Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                Q.reset_set(set_bt_labels_from);
                if(Q.bulk_return_link(set_bt_labels_from, retVals)!=QClass.APIFail){
                    for(Return_Link_Row row:retVals){
                        //while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {
                        
                        String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                        String nt = dbGen.removePrefix(row.get_v1_cls());
                        if (allHierTermsVec.contains(bt) == false) {
                            allHierTermsVec.add(bt);
                        }
                        if (allHierTermsVec.contains(nt) == false) {
                            allHierTermsVec.add(nt);
                        }                    
                        Vector<String> btNts = ntsOfHierDesciptor.get(bt);
                        if (btNts == null) {
                            Vector<String> newNtsVec = new Vector<String>();
                            newNtsVec.add(nt);
                            ntsOfHierDesciptor.put(bt, newNtsVec);
                        } else {
                            if (btNts.contains(nt) == false) {

                                btNts.add(nt);
                                ntsOfHierDesciptor.put(bt, btNts);
                            }
                        }

                        if (ntsOfHierDesciptor.containsKey(nt) == false) {
                            Vector<String> newNtsVec = new Vector<String>();
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
                    Vector<String> btNts = ntsOfHierDesciptor.get(bt);
                    if (btNts == null) {
                        Vector<String> newNtsVec = new Vector<String>();
                        newNtsVec.add(nt);
                        ntsOfHierDesciptor.put(bt, newNtsVec);
                    } else {
                        if (btNts.contains(nt) == false) {

                            btNts.add(nt);
                            ntsOfHierDesciptor.put(bt, btNts);
                        }
                    }

                    if (ntsOfHierDesciptor.containsKey(nt) == false) {
                        Vector<String> newNtsVec = new Vector<String>();
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
                String XSL1 = webAppSaveResults_AbsolutePath.concat("/SaveAll_Terms_Of_Hierarchy_Hierarchical.xsl");
                String XSL2 = webAppSaveResults_AbsolutePath.concat("/SaveAll_Terms_Of_Hierarchy_Hierarchical_tree_view.xsl");

                String typicalLocation = Save_Results_file_name1.concat(".html");
                String treeLocation = Save_Results_file_name2.concat(".html");
                writeHierarchicalResultsInXMLFile(allHierTermsVec, ntsOfHierDesciptor, hierarchy, u,
                        time, "<typicalHierarchicalLocation>"+typicalLocation+"</typicalHierarchicalLocation><treeHierarchicalLocation>"+treeLocation+"</treeHierarchicalLocation><base>Ιεραρχική παρουσίαση όρων της ιεραρχίας: </base><arg1>" + Utilities.escapeXML(hierarchy) + "</arg1>",/* xslLink,*/ webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml",pathToSaveScriptingAndLocale, targetLocale);

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml", XSL1, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1.concat(".html"));
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml", XSL2, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name2.concat(".html"));
                

                //response.sendRedirect("DownloadFile?targetFile="+ Save_Results_file_name1.concat(".html"));
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name1.concat(".html"));

                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in Hierarchy terms Hierarchical: " + elapsedTimeSec);
                //out.println(path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name );
                out.flush();
                return;
            //</editor-fold>    
            }
            
            else if (action.compareTo("GlobalThesarusHierarchical")==0){
                //DISPLAY FACETS AND HIERARCHIES
                 //<editor-fold defaultstate="collapsed" desc="GlobalThesarusHierarchical display computations..."> 

                Vector<String> facetNames = dbGen.getFacets(SessionUserInfo.selectedThesaurus, Q, sis_session, targetLocale);
                
                Hashtable<String,Vector<String>> facetToHierarhchies = new Hashtable<String,Vector<String>>();
                Vector<String> hierarchyNames = new Vector<String>();
                Vector<String> allTermsVec = new Vector<String>();
                Hashtable<String, Vector<String>> ntsOfHierDesciptor = new Hashtable<String, Vector<String>>();
                
                for(int k=0; k< facetNames.size(); k++){
                    String fName = facetNames.get(k);
                    Vector<String> facetHiers = dbGen.returnResults_Facet(SessionUserInfo,fName,"hierarchy",Q,sis_session, targetLocale);
                    facetToHierarhchies.put(fName, facetHiers);
                    
                    for(int n=0;n<facetHiers.size();n++){
                        String hierName = facetHiers.get(n);
                        if(hierarchyNames.contains(hierName)==false){
                            hierarchyNames.add(hierName);
                        }
                    }
                }
                
                StringObject BT_NTClassObj = new StringObject();
                StringObject BT_NTLinkObj = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.nt_kwd, BT_NTClassObj, BT_NTLinkObj, Q, sis_session);
                
                
                
                for(int k=0; k< hierarchyNames.size(); k++){
                
                    StringObject currentHierarchyObj = new StringObject(prefix_class.concat(hierarchyNames.get(k)));
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

                    

                    Vector<String> newTopTermNtsVec = new Vector<String>();
                    String topterm = dbGen.removePrefix(currentHierarchyObj.getValue());
                    ntsOfHierDesciptor.put(topterm, newTopTermNtsVec);

                    
                    Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
                    Q.reset_set(set_bt_labels_from);
                    if(Q.bulk_return_link(set_bt_labels_from, retVals)!=QClass.APIFail){
                        for(Return_Link_Row row:retVals){
                            //while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {

                            String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                            String nt = dbGen.removePrefix(row.get_v1_cls());
                            if (allTermsVec.contains(bt) == false) {
                                allTermsVec.add(bt);
                            }
                            if (allTermsVec.contains(nt) == false) {
                                allTermsVec.add(nt);
                            }                    
                            Vector<String> btNts = ntsOfHierDesciptor.get(bt);
                            if (btNts == null) {
                                Vector<String> newNtsVec = new Vector<String>();
                                newNtsVec.add(nt);
                                ntsOfHierDesciptor.put(bt, newNtsVec);
                            } else {
                                if (btNts.contains(nt) == false) {

                                    btNts.add(nt);
                                    ntsOfHierDesciptor.put(bt, btNts);
                                }
                            }

                            if (ntsOfHierDesciptor.containsKey(nt) == false) {
                                Vector<String> newNtsVec = new Vector<String>();
                                ntsOfHierDesciptor.put(nt, newNtsVec);
                            }
                        }
                    }
                
                    
                }
                Collections.sort(allTermsVec, new StringLocaleComparator(targetLocale));

                    

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                String Save_Results_file_name1 = "Thesaurus_Global_Hierarchical_View_" + time;
                String Save_Results_file_name2 = "Thesaurus_Global_Tree_View_" + time;
                String XSL1 = webAppSaveResults_AbsolutePath.concat("/Thesaurus_Global_Hierarchical_View.xsl");
                String XSL2 = webAppSaveResults_AbsolutePath.concat("/Thesaurus_Global_Tree_View.xsl");

                
                

                String typicalLocation = Save_Results_file_name1.concat(".html");
                String treeLocation    = Save_Results_file_name2.concat(".html");
                //Hashtable<String,Vector<String>> facetToHierarhchies = new Hashtable<String,Vector<String>>();
                //Vector<String> hierarchyNames = new Vector<String>();
                //Vector<String> allHierTermsVec = new Vector<String>();
                writeThesarusGlobalViewResultsInXMLFile(facetToHierarhchies, allTermsVec, ntsOfHierDesciptor,SessionUserInfo.selectedThesaurus, u,
                        time, 
                        "<typicalHierarchicalLocation>"+typicalLocation+"</typicalHierarchicalLocation><treeHierarchicalLocation>"+treeLocation+"</treeHierarchicalLocation>"
                        , webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml",pathToSaveScriptingAndLocale, targetLocale);

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml", XSL1, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1.concat(".html"));
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name1 + ".xml", XSL2, webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name2.concat(".html"));
                
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name1.concat(".html"));
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in Hierarchy terms GlobalThesarus View: " + elapsedTimeSec);
                //out.println(path + "/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name );
                out.flush();
                return;
            //</editor-fold>    
            }
            else{
                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            }
            

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    public void writeThesarusGlobalViewResultsInXMLFile(
            Hashtable<String,Vector<String>> facetHiers, 
            Vector<String> all_hier_terms_vec,  
            Hashtable<String, Vector<String>> ntsOfHierDesciptor, 
            String thesaurus, 
            Utilities u, 
            String title, 
            String query,
            String fileName,
            String pathToSaveScriptingAndLocale, 
            Locale targetLocale) {
        
        
        
        String Full_Save_Results_file_name = fileName;
        
        OutputStreamWriter out = null;
        try {
            //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            out.write(xml_header);
            //out.write(xslLink);
            // Fw = new FileWriter(Full_Save_Results_file_name);
            out.write("<page language=\""+Parameters.UILang+"\">\n" +
                    "\t<targetThesaurus>" + thesaurus + "</targetThesaurus>\n" +
                    "\t<title>" + title + "</title>\n" +
                    "\t<query>\n" + query + "\t</query>\n" +
                    "\t<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>\n");


        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
        out.write("\r\n<results>\r\n");
        Vector<String> facets = new Vector<String>(facetHiers.keySet());
        Collections.sort(facets, new StringLocaleComparator(targetLocale));
        for(int p=0;p<facets.size();p++){
            String fname = facets.get(p);
            out.write("\t<topterm>\r\n");

            out.write("\t\t<name>");
            out.write(Utilities.escapeXML(fname));
            out.write("</name>\r\n");
            
            Vector<String>hierarhies = facetHiers.get(fname);
            Collections.sort(hierarhies, new StringLocaleComparator(targetLocale));
            for(int q = 0; q< hierarhies.size(); q++){
                String hierName = hierarhies.get(q);
                out.write("\t\t<nt>");
                out.write(Utilities.escapeXML(hierName));
                out.write("</nt>\r\n");
            }
            out.write("\t</topterm>\r\n");
        }
        
         int all_hier_terms_vecSize = all_hier_terms_vec.size();
        for (int i=0; i<all_hier_terms_vecSize; i++) {
            String term = all_hier_terms_vec.get(i);
            Vector<String> termNts = new Vector<String>();
            termNts.addAll(ntsOfHierDesciptor.get(term));  
            if (termNts.size() > 0) {
                Collections.sort(termNts, new StringLocaleComparator(targetLocale));
            }

            out.write("\t<term>\r\n");

            out.write("\t\t<name>");
            out.write(Utilities.escapeXML(term));
            out.write("</name>\r\n");

            for (int k = 0; k < termNts.size(); k++) {

                out.write("\t\t<nt>");
                out.write(Utilities.escapeXML(termNts.get(k)));
                out.write("</nt>\r\n");

            }

            out.write("\t</term>\r\n");
        }
            
            
        /*
            //out.write("<results>");
            out.write("<topterm>");

            out.write("<name>");
            out.write(Utilities.escapeXML(hierarchy));
            out.write("</name>");

            Vector<String> toptermNts = ntsOfHierDesciptor.get(hierarchy);
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
            for (int i=0; i<all_hier_terms_vecSize; i++) {
                String term = all_hier_terms_vec.get(i);
                Vector<String> termNts = new Vector<String>();
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

*/


            out.write("</results>");
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("</page>");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        
    }
    
    public String writePrimary2TranslationsIndexResultsInXMLFile(Vector<String> all_hier_terms_vec,Vector<SortItem> all_translations_vec,
            Hashtable<String, Vector<SortItem>> term_translationsOfHierDesciptor, Hashtable<SortItem, Vector<String>> translations_termsOfHierDesciptor, String title, String query/*, String xslLink*/, String fileName, IntegerObject sis_session, QClass Q, String baseURL, String xslBasePath,String pathToSaveScriptingAndLocale, Locale targetLocale) {

        String Full_Save_Results_file_name = fileName; //webAppSaveResults_temporary_filesAbsolutePath +"/"+ Save_Results_file_name + ".xml";
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        String Primary2TranslationsHtmlOutputPath = Full_Save_Results_file_name.split("\\.xml")[0].concat("_primary2translations.html");
        String Translations2PrimaryHtmlOutputPath = Full_Save_Results_file_name.split("\\.xml")[0].concat("_translations2primary.html");

        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            //out.write(xml_header);
            //out.write(xslLink);
            // Fw = new FileWriter(Full_Save_Results_file_name);
            out.write("<page language='"+Parameters.UILang+"'>\n" +
                    "<title>" + title + "</title>" +
                    "<query>" + query + "</query>" +
                    "<primary2transaltionsLocation>" + baseURL.concat(Primary2TranslationsHtmlOutputPath.split("/")[Primary2TranslationsHtmlOutputPath.split("/").length - 1]) + "</primary2transaltionsLocation>" +
                    "<transaltions2primaryLocation>" + baseURL.concat(Translations2PrimaryHtmlOutputPath.split("/")[Translations2PrimaryHtmlOutputPath.split("/").length - 1]) + "</transaltions2primaryLocation>" +
                    "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>");
                    

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("<results translationsSeperator=\""+Parameters.TRANSLATION_SEPERATOR+"\">");
            
                out.write("<primary2transaltions>");
                int all_hier_terms_vecSize = all_hier_terms_vec.size();
                
                Collections.sort(all_hier_terms_vec, new StringLocaleComparator(targetLocale));

                for (int i=0; i<all_hier_terms_vecSize; i++) {

                    String term = all_hier_terms_vec.get(i);
                    Vector<SortItem> termTranslations = new Vector<SortItem>();
                    termTranslations.addAll(term_translationsOfHierDesciptor.get(term));
                    Collections.sort(termTranslations, new GuideTermSortItemComparator(targetLocale));
                    
                    out.write("<term>");

                    out.write("<name>");
                    out.write(Utilities.escapeXML(term));
                    out.write("</name>");


                    out.write("<" + ConstantParameters.translation_kwd+">");
                    for (int j = 0; j < termTranslations.size(); j++) {
                        out.write("<translation linkClass=\""+termTranslations.get(j).linkClass+"\">");
                        out.write(Utilities.escapeXML(termTranslations.get(j).log_name));
                        out.write("</translation>");
                    }
                    out.write("</" + ConstantParameters.translation_kwd + ">");

                    out.write("</term>");

                }
                out.write("</primary2transaltions>");
                
                out.write("<translations2primary>");

                Vector<String> noTranslationTerms = new Vector<String>();
                noTranslationTerms.addAll(translations_termsOfHierDesciptor.get(new SortItem("-",-1,"")));
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
                Collections.sort(all_translations_vec,new GuideTermSortItemComparator(targetLocale));

                for (int i=0; i<all_translations_vecSize; i++) {

                    SortItem translation = all_translations_vec.get(i);
                    Vector<String> primaryTerms= new Vector<String>();
                    primaryTerms.addAll(translations_termsOfHierDesciptor.get(translation));
                    Collections.sort(primaryTerms, new StringLocaleComparator(targetLocale));

                    out.write("<" + ConstantParameters.translation_kwd + ">");

                    out.write("<name linkClass=\""+translation.linkClass+"\">");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("</page>");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        String primary2translationsXSLPath = xslBasePath + File.separator + "Primary2TranslationsIndex.xsl";
        String translations2primaryXSLPath = xslBasePath + File.separator + "Translations2PrimaryIndex.xsl";

        //XML STORED IN Full_Save_Results_file_name
        //XSL STORED IN xslLink
        u.XmlFileTransform(Full_Save_Results_file_name, primary2translationsXSLPath, Primary2TranslationsHtmlOutputPath);
        u.XmlFileTransform(Full_Save_Results_file_name, translations2primaryXSLPath, Translations2PrimaryHtmlOutputPath);

        //default index
        return Primary2TranslationsHtmlOutputPath;
    }

    public void writeSystematicResultsInXMLFile(Vector<TaxonomicCodeItem> allTerms, Utilities u, String title, String query,/*String xslLink,*/ String fileName,String pathToSaveScriptingAndLocale) {

        String Full_Save_Results_file_name = fileName;

        
        OutputStreamWriter out = null;
        try {

            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            out.write(ConstantParameters.xmlHeader);
            //out.write(xslLink);
            out.write("<page title=\"" + title + "\" language='"+Parameters.UILang+"' mode=\"insert\">");

            out.write("<query>");
            out.write(query);
            out.write("</query>");

            out.write("<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>");

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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }



    }

    public void writeHierarchicalResultsInXMLFile(Vector<String> all_hier_terms_vec, Hashtable<String, Vector<String>> ntsOfHierDesciptor, String hierarchy, Utilities u, String title, String query,/*String xslLink,*/ String fileName,String pathToSaveScriptingAndLocale, Locale targetLocale) {

        String Full_Save_Results_file_name = fileName;
        
        OutputStreamWriter out = null;
        try {
            //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");

            out.write(xml_header);
            //out.write(xslLink);
            // Fw = new FileWriter(Full_Save_Results_file_name);
            out.write("<page language=\""+Parameters.UILang+"\">\n" +
                    "<title>" + title + "</title>" +
                    //"<query>" + "Ιεραρχική παρουσίαση όρων της ιεραρχίας " + hierarchy + "</query>" +
                    "<query>" + query + "</query>" +
                    "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>");


        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }


        try {
            out.write("<results>");
            out.write("<topterm>");

            out.write("<name>");
            out.write(Utilities.escapeXML(hierarchy));
            out.write("</name>");

            Vector<String> toptermNts = ntsOfHierDesciptor.get(hierarchy);
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
            for (int i=0; i<all_hier_terms_vecSize; i++) {
                String term = all_hier_terms_vec.get(i);
                Vector<String> termNts = new Vector<String>();
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("</page>");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }



    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
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
