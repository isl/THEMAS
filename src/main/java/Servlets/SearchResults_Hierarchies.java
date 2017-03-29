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
import Utils.SearchCriteria;

import Utils.Parameters;
import Utils.Utilities;
import Utils.StringLocaleComparator;

import XMLHandling.WriteFileData;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class SearchResults_Hierarchies extends ApplicationBasicServlet {

    final String xml_header = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        String startRecord = (String) request.getParameter("pageFirstResult");
        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            if (sessionInstance.getAttribute("SessionUser") == null) {
                if (startRecord != null && startRecord.matches("SaveAll")) {
                    out.println("Session Invalidate");
                } else {
                    response.sendRedirect("Index");
                }
                return;
            }

            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

            String updateTermsCirteria = (String) request.getParameter("updateTermCriteria");
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            String answerType = request.getParameter("answerType");
            Locale targetLocale = new Locale(language, country);

            Utilities u = new Utilities();

            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            SearchCriteria searchCriteria;


            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            // -------------------- paging info And criteria retrieval-------------------------- 
            //initial values --> will change from the following code
            int hierarchiesPagingListStep = new Integer(ListStepStr).intValue();
            int hierarchiesPagingFirst = 1;
            int hierarchiesPagingQueryResultsCount = 0;

            if (updateTermsCirteria != null) { // detect if search was pressed or left menu option was triggered
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Hierarchies", updateTermsCirteria, request, u);
                if(searchCriteria.input.size()==searchCriteria.value.size()){
                    /*
                    for(int k=0; k<searchCriteria.input.size(); k++){
                        String inputKwd = searchCriteria.input.get(k);
                        String value = searchCriteria.value.get(k);
                        byte[] valbytes = value.getBytes("UTF-8");
                        if(inputKwd.equals("name") ||inputKwd.equals("term")){
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if(valbytes.length > dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session)){
                                
                                //end query and close connection
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                
                                response.sendRedirect("Links?tab=HierarchiesSearchCriteria&CheckLength=true");
                                return;
                            }
                        }                        
                    }
                    */
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Search Input Error");
                }
                
                sessionInstance.setAttribute("SearchCriteria_Hierarchies", searchCriteria);

            } else {  //else try to read criteria for this user
                searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Hierarchies");
            }

            if (searchCriteria == null) {//tab pressed without any criteria previously set -- > default == list all with default output
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Hierarchies", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Hierarchies", searchCriteria);

            }

            if (startRecord == null) { //read paging criteria
                int index = searchCriteria.pagingNames.indexOf("hierarchiesResults");
                hierarchiesPagingFirst = searchCriteria.pagingValues.get(index);
            }

            //if ALL then left_menu was triggered and criteria were updated with pageFirst = 1
            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {

                hierarchiesPagingFirst = Integer.parseInt(startRecord);
                if (hierarchiesPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("hierarchiesResults");
                    searchCriteria.pagingValues.set(index, hierarchiesPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Hierarchies", searchCriteria);

                } else {
                    int index = searchCriteria.pagingNames.indexOf("hierarchiesResults");
                    hierarchiesPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }

            String[] input = new String[searchCriteria.input.size()];
            String[] ops = new String[searchCriteria.operator.size()];
            String[] inputValue = new String[searchCriteria.value.size()];
            String operator = searchCriteria.CombineOperator;
            String[] output = new String[searchCriteria.output.size()];

            searchCriteria.input.toArray(input);
            searchCriteria.operator.toArray(ops);
            searchCriteria.value.toArray(inputValue);
            searchCriteria.output.toArray(output);

            // handle search operators (not) starts / ends with
            u.InformSearchOperatorsAndValuesWithSpecialCharacters(ops, inputValue);
            //-------------------- paging info And criteria retrieval-------------------------- 

            StringBuffer xml = new StringBuffer();

            
            long startTime = Utilities.startTimer();

            Vector<String> allResultsHierarchies = getAllSearchHierarchies(SessionUserInfo, input, ops, inputValue, operator, Q, TA, sis_session);
            Collections.sort(allResultsHierarchies, new StringLocaleComparator(targetLocale));

            //Get only those hierarchies that will appear in next page
            hierarchiesPagingQueryResultsCount = allResultsHierarchies.size();
            if (hierarchiesPagingFirst > hierarchiesPagingQueryResultsCount) {
                hierarchiesPagingFirst = 1;
            }

            if (startRecord != null && startRecord.matches("SaveAll")) {

                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Hierarchies_" + time;
                String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Hierarchies.xsl").toString();

                if(answerType != null && answerType.compareTo("XML")==0){
                    pathToSaveScriptingAndLocale = null;
                }
                writeResultsInXMLFile(SessionUserInfo, allResultsHierarchies, u, time, searchCriteria, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session,pathToSaveScriptingAndLocale, targetLocale);

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                if (answerType != null && answerType.compareTo("XML") == 0) {
                    if(Parameters.FormatXML){

                        WriteFileData.formatXMLFile(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml");

                    }
                    out.println(Save_Results_file_name.concat(".xml"));
                } else {
                    //create html and answer with html link for redirection --> download
                    u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", 
                                       XSL, 
                                       webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));
                    
//Send HTML relative url to output and return
                    out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                }

                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in hierarchies --> time elapsed: " + elapsedTimeSec);

                out.flush();
                return;
            }

            Vector<String> resultsHierarchies = new Vector<String>();
            for (int i = 0; i < hierarchiesPagingListStep; i++) {
                if (i + hierarchiesPagingFirst > hierarchiesPagingQueryResultsCount) {
                    break;
                }
                String tmp = allResultsHierarchies.get(i + hierarchiesPagingFirst - 1).toString();
                resultsHierarchies.addElement(tmp);
            }
            String xmlResults = u.getResultsinXml_Hierarchy(SessionUserInfo, resultsHierarchies, output, Q, sis_session, targetLocale);

            float elapsedTimeSec = Utilities.stopTimer(startTime);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in hierarchies --> time elapsed: " + elapsedTimeSec);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            xmlResults += "<results>";
            xmlResults += (u.writePagingInfoXML(hierarchiesPagingListStep, hierarchiesPagingFirst, hierarchiesPagingQueryResultsCount, elapsedTimeSec, "SearchResults_Hierarchies"));
            xmlResults += "</results>";

            xml.append(u.getXMLStart(ConstantParameters.LMENU_HIERARCHIES));
            xml.append(u.getXMLMiddle(xmlResults, "SearchHierarchyResults"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
        

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    /**Returns a Vector with the hierarchies which match with criteria*/
    public Vector<String> getAllSearchHierarchies(UserInfoClass SessionUserInfo, String[] input, String[] operators, String[] inputValues, 
            String globalOperator, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Vector<String> globalHierachyResults = new Vector<String>();

        int sisSessionId = sis_session.getValue();
        int set_global_hierarchy_results = -1;
        String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        Q.reset_name_scope();


        // looking for EKTHierarchyClass
        StringObject Hierarchies = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), Hierarchies);

        // get instances of EKTHierarchyClass 
        Q.reset_name_scope();
        Q.set_current_node( Hierarchies);

        int set_h = Q.get_instances( 0);

        // get instances of EKTObsoleteHierarchy 
        StringObject ObsoleteHierarchies = new StringObject();
        dbtr.getThesaurusClass_ObsoleteHierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), ObsoleteHierarchies);

        Q.reset_name_scope();
        Q.set_current_node( ObsoleteHierarchies);
        int set_oh = Q.get_instances( 0);

        Q.set_union( set_h, set_oh);

        Q.reset_name_scope();

        if (globalOperator.equals("*") || globalOperator.equals("all")) {
            set_global_hierarchy_results = Q.set_get_new();
            Q.reset_set( set_h);
            Q.set_copy( set_global_hierarchy_results, set_h);
            Q.reset_set( set_global_hierarchy_results);
        } else {
            for (int i = 0; i < input.length; i++) {

                Q.reset_set( set_h);
                int set_partial_hierarchy_results = Q.set_get_new();

                String searchVal = inputValues[i];

                /*
                String searchVal = new String("");
                try{
                searchVal =  new String(inputValues[i].getBytes("ISO-8859-1"), "UTF-8");
                }
                catch(IOException e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+e.getMessage());
                }*/


                Q.reset_name_scope();

                //Case Of Hierarchy "term" criteria
                if (input[i].toString().equalsIgnoreCase("term")) {
                    // get the terms with the given criteria
                    String[] term_field = {"name"};
                    String[] term_operator = new String[1];
                    term_operator[0] = operators[i];
                    String[] term_inputValue = new String[1];
                    term_inputValue[0] = searchVal;
                    int descriptor_results_set = dbGen.getSearchTermResultSet(SessionUserInfo, term_field, term_operator, term_inputValue, globalOperator, Q, TA, sis_session);
                    Q.reset_set( descriptor_results_set);
                    // get the hierarchies of the found terms
                    set_partial_hierarchy_results = Q.get_classes( descriptor_results_set);
                    Q.free_set( descriptor_results_set);
                    Q.reset_set( set_partial_hierarchy_results);
                    Q.set_intersect( set_partial_hierarchy_results, set_h);
                    Q.reset_set( set_partial_hierarchy_results);
                }

                //Case Of Hierarchy Name criteria
                if (input[i].toString().equalsIgnoreCase("name")) {

                    if (operators[i].toString().equals("=")) {

                        if (searchVal != null && searchVal.trim().length() > 0) {
                            if (Q.set_current_node( new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                                /*
                                CMValue prm_val = new CMValue();
                                prm_val.assign_string(searchVal);
                                int ptrn_set = Q.set_get_new();
                                Q.set_put_prm( ptrn_set, prm_val);
                                
                                Q.reset_set( set_h);
                                Q.reset_set( ptrn_set);
                                set_partial_hierarchy_results = Q.get_matched( set_h, ptrn_set);
                                Q.reset_set( set_partial_hierarchy_results);*/
                                Q.set_put( set_partial_hierarchy_results);
                                Q.reset_set( set_partial_hierarchy_results);
                            }
                        }
                    } else if (operators[i].toString().equals("~")) {

                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm( ptrn_set, prm_val);

                        Q.reset_set( set_h);
                        //Q.reset_set( ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_partial_hierarchy_results = Q.get_matched_case_insensitive( set_h, ptrn_set,1);
                        //set_partial_hierarchy_results = Q.get_matched( set_h, ptrn_set);
                        set_partial_hierarchy_results = Q.get_matched_ToneAndCaseInsensitive( set_h, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);

                        Q.reset_set( set_partial_hierarchy_results);

                    } else if (operators[i].toString().equals("!")) {

                        int set_exclude_hierarchies = Q.set_get_new();

                        if (Q.set_current_node( new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                            /*
                            CMValue prm_val = new CMValue();
                            prm_val.assign_string(searchVal);
                            int ptrn_set = Q.set_get_new();
                            Q.set_put_prm( ptrn_set, prm_val);
                            
                            Q.reset_set( set_h);
                            Q.reset_set( ptrn_set);
                            set_exclude_facets = Q.get_matched( set_h, ptrn_set);
                            Q.reset_set( set_exclude_facets);
                             */
                            Q.set_put( set_exclude_hierarchies);
                            Q.reset_set( set_exclude_hierarchies);
                        }

                        Q.reset_set( set_h);
                        Q.set_copy( set_partial_hierarchy_results, set_h);

                        Q.reset_set( set_partial_hierarchy_results);
                        Q.reset_set( set_exclude_hierarchies);
                        Q.set_difference( set_partial_hierarchy_results, set_exclude_hierarchies);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.reset_set( set_exclude_hierarchies);

                    } else if (operators[i].toString().equals("!~")) {

                        //int set_exclude_hierarchies = Q.set_get_new();

                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm( ptrn_set, prm_val);

                        Q.reset_set( set_h);
                        //Q.reset_set( ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_exclude_hierarchies = Q.get_matched_case_insensitive( set_h, ptrn_set,1);
                        //set_exclude_hierarchies = Q.get_matched( set_h, ptrn_set);
                        int set_exclude_hierarchies = Q.get_matched_ToneAndCaseInsensitive( set_h, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                        Q.reset_set( set_exclude_hierarchies);

                        Q.reset_set( set_h);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.set_copy( set_partial_hierarchy_results, set_h);

                        Q.reset_set( set_partial_hierarchy_results);
                        Q.reset_set( set_exclude_hierarchies);
                        Q.set_difference( set_partial_hierarchy_results, set_exclude_hierarchies);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.reset_set( set_exclude_hierarchies);
                    }
                } //Case Of letter_code's value criteria // NOT USED BECAUSE OF CRITERIA HIERARCHIES XSL 
                else if (input[i].toString().equalsIgnoreCase("letter_code")) {

                    if (operators[i].toString().equals("=")) {

                        //get all hierarchies that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set( set_h);
                        linkFromSet = Q.get_inher_link_from( set_h);
                        Q.reset_set( linkFromSet);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        Vector<Return_Full_Link_Id_Row> retVals = new Vector<Return_Full_Link_Id_Row>();
                        if(Q.bulk_return_full_link_id(linkFromSet, retVals)!=QClass.APIFail){
                            for(Return_Full_Link_Id_Row row:retVals){
                                if (row.get_v5_categ().equals("letter_code") == false) {
                                continue;
                                }
                                String temp = row.get_v8_cmv().getString();
                                if (temp.equals(searchVal)) {

                                    if (Q.set_current_node_id( row.get_v4_linkId()) != QClass.APIFail) {

                                        Q.set_put( filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                        StringObject cls = new StringObject();
                        IntegerObject clsID = new IntegerObject();
                        StringObject label = new StringObject();
                        IntegerObject linkID = new IntegerObject();
                        StringObject categ = new StringObject();
                        StringObject fromcls = new StringObject();
                        IntegerObject categID = new IntegerObject();
                        CMValue cmv = new CMValue();
                        IntegerObject uniq_categ = new IntegerObject();

                        //Delete Letter codes
                        //For each link originating from current hierarchy ignore non letter_code links
                        while (Q.retur_full_link_id(linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                            if (categ.getValue().equals("letter_code") == false) {
                                continue;
                            }
                            String temp = cmv.getString();
                            if (temp.equals(searchVal)) {

                                if (Q.set_current_node_id( linkID.getValue()) != QClass.APIFail) {

                                    Q.set_put( filteredSet);
                                }
                            }
                        }
                        */
                        
                        Q.reset_set( filteredSet);

                        //get hierarchy nodes that correspond to the filtered letter code values
                        set_partial_hierarchy_results = Q.get_from_value( filteredSet);
                        Q.reset_set( set_partial_hierarchy_results);

                        //Find Facets that inherit their letter Codes to their sub hierarchy classes -- stored in additional_hiers_set
                        int facetLCSet = Q.set_get_new();
                        Q.set_copy( facetLCSet, set_partial_hierarchy_results);

                        Q.reset_set( facetLCSet);
                        Q.reset_set( set_h);
                        Q.set_difference( facetLCSet, set_h);
                        Q.reset_set( facetLCSet);

                        int additional_hiers_set = Q.set_get_new();
                        additional_hiers_set = Q.get_subclasses( facetLCSet);

                        Q.reset_set( additional_hiers_set);
                        Q.reset_set( set_h);
                        Q.set_intersect( additional_hiers_set, set_h);
                        Q.reset_set( additional_hiers_set);

                        //Filter out letter code from nodes that do not belong to set_h -- are not hierarchies
                        Q.reset_set( set_h);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.set_intersect( set_partial_hierarchy_results, set_h);
                        Q.reset_set( set_partial_hierarchy_results);

                        //Union of these 2 partial results (hierarchyletter codes and inherieted letter codes)
                        Q.set_union( set_partial_hierarchy_results, additional_hiers_set);
                        Q.reset_set( set_partial_hierarchy_results);


                    } else if (operators[i].toString().equals("~")) {

                        //get all hierarchies that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set( set_h);
                        linkFromSet = Q.get_inher_link_from( set_h);
                        Q.reset_set( linkFromSet);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        Vector<Return_Full_Link_Id_Row> retVals = new Vector<Return_Full_Link_Id_Row>();
                        if(Q.bulk_return_full_link_id(linkFromSet, retVals)!=QClass.APIFail){
                            for(Return_Full_Link_Id_Row row: retVals){
                                if (row.get_v5_categ().equals("letter_code") == false) {
                                    continue;
                                }
                                String temp = row.get_v8_cmv().getString();
                                if (temp.contains(searchVal)) {

                                    if (Q.set_current_node_id( row.get_v4_linkId()) != QClass.APIFail) {

                                        Q.set_put( filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                        StringObject cls = new StringObject();
                        IntegerObject clsID = new IntegerObject();
                        StringObject label = new StringObject();
                        IntegerObject linkID = new IntegerObject();
                        StringObject categ = new StringObject();
                        StringObject fromcls = new StringObject();
                        IntegerObject categID = new IntegerObject();
                        CMValue cmv = new CMValue();
                        IntegerObject uniq_categ = new IntegerObject();

                        //Delete Letter codes
                        //For each link originating from current hierarchy ignore non letter_code links
                        while (Q.retur_full_link_id( linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                            if (categ.getValue().equals("letter_code") == false) {
                                continue;
                            }
                            String temp = cmv.getString();
                            if (temp.contains(searchVal)) {

                                if (Q.set_current_node_id( linkID.getValue()) != QClass.APIFail) {

                                    Q.set_put( filteredSet);
                                }
                            }
                        }
                        */
                        Q.reset_set( filteredSet);

                        //get hierarchy nodes that correspond to the filtered letter code values
                        set_partial_hierarchy_results = Q.get_from_value( filteredSet);
                        Q.reset_set( set_partial_hierarchy_results);

                        //Find Facets that inherit their letter Codes to their sub hierarchy classes -- stored in additional_hiers_set
                        int facetLCSet = Q.set_get_new();
                        Q.set_copy( facetLCSet, set_partial_hierarchy_results);

                        Q.reset_set( facetLCSet);
                        Q.reset_set( set_h);
                        Q.set_difference( facetLCSet, set_h);
                        Q.reset_set( facetLCSet);

                        int additional_hiers_set = Q.set_get_new();
                        additional_hiers_set = Q.get_subclasses( facetLCSet);

                        Q.reset_set( additional_hiers_set);
                        Q.reset_set( set_h);
                        Q.set_intersect( additional_hiers_set, set_h);
                        Q.reset_set( additional_hiers_set);

                        //Filter out letter code from nodes that do not belong to set_h -- are not hierarchies
                        Q.reset_set( set_h);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.set_intersect( set_partial_hierarchy_results, set_h);
                        Q.reset_set( set_partial_hierarchy_results);

                        //Union of these 2 partial results (hierarchyletter codes and inherieted letter codes)
                        Q.set_union( set_partial_hierarchy_results, additional_hiers_set);
                        Q.reset_set( set_partial_hierarchy_results);


                    } else if (operators[i].toString().equals("!")) {

                        //get all hierarchies that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set( set_h);
                        linkFromSet = Q.get_inher_link_from( set_h);
                        Q.reset_set( linkFromSet);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        Vector<Return_Full_Link_Id_Row> retVals = new Vector<Return_Full_Link_Id_Row>();
                        if(Q.bulk_return_full_link_id(linkFromSet, retVals)!=QClass.APIFail){
                            for(Return_Full_Link_Id_Row row: retVals){
                                if (row.get_v5_categ().equals("letter_code") == false) {
                                    continue;
                                }
                                String temp = row.get_v8_cmv().getString();
                                if (temp.equals(searchVal)) {

                                    if (Q.set_current_node_id( row.get_v4_linkId()) != QClass.APIFail) {

                                        Q.set_put( filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                        StringObject cls = new StringObject();
                        IntegerObject clsID = new IntegerObject();
                        StringObject label = new StringObject();
                        IntegerObject linkID = new IntegerObject();
                        StringObject categ = new StringObject();
                        StringObject fromcls = new StringObject();
                        IntegerObject categID = new IntegerObject();
                        CMValue cmv = new CMValue();
                        IntegerObject uniq_categ = new IntegerObject();
        
                        //Delete Letter codes
                        //For each link originating from current hierarchy ignore non letter_code links
                        while (Q.retur_full_link_id( linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                            if (categ.getValue().equals("letter_code") == false) {
                                continue;
                            }
                            String temp = cmv.getString();
                            if (temp.equals(searchVal)) {

                                if (Q.set_current_node_id( linkID.getValue()) != QClass.APIFail) {

                                    Q.set_put( filteredSet);
                                }
                            }
                        }
                        */
                        Q.reset_set( filteredSet);

                        //get hierarchy nodes that correspond to the filtered letter code values
                        int set_exclude_facets = Q.set_get_new();
                        set_exclude_facets = Q.get_from_value( filteredSet);
                        Q.reset_set( set_exclude_facets);

                        //Find Facets that inherit their letter Codes to their sub hierarchy classes -- stored in additional_hiers_set
                        int facetLCSet = Q.set_get_new();
                        Q.set_copy( facetLCSet, set_exclude_facets);

                        Q.reset_set( facetLCSet);
                        Q.reset_set( set_h);
                        Q.set_difference( facetLCSet, set_h);
                        Q.reset_set( facetLCSet);

                        int additional_hiers_set = Q.set_get_new();
                        additional_hiers_set = Q.get_subclasses( facetLCSet);

                        Q.reset_set( additional_hiers_set);
                        Q.reset_set( set_h);
                        Q.set_intersect( additional_hiers_set, set_h);
                        Q.reset_set( additional_hiers_set);

                        //Filter out letter code from nodes that do not belong to set_h -- are not hierarchies
                        Q.reset_set( set_h);
                        Q.reset_set( set_exclude_facets);
                        Q.set_intersect( set_exclude_facets, set_h);
                        Q.reset_set( set_exclude_facets);

                        //Union of these 2 partial results (hierarchyletter codes and inherieted letter codes)
                        Q.set_union( set_exclude_facets, additional_hiers_set);
                        Q.reset_set( set_exclude_facets);

                        //get facet nodes that correspond to the filtered letter code values and put them 
                        //in an exclude list. This is done because one facet may have multiple letter codes 
                        //but if one letter code equals to searchVal and all the others don't then the whole
                        //Facet must be excluded.

                        //this loop's partial results are calculated from the set difference between all facets and exluded facets
                        Q.reset_set( set_h);
                        Q.set_copy( set_partial_hierarchy_results, set_h);
                        Q.reset_set( set_partial_hierarchy_results);

                        Q.set_difference( set_partial_hierarchy_results, set_exclude_facets);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.free_set( set_exclude_facets);

                    } else if (operators[i].toString().equals("!~")) {

                        //get all hierarchies that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set( set_h);
                        linkFromSet = Q.get_inher_link_from( set_h);
                        Q.reset_set( linkFromSet);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        Vector<Return_Full_Link_Id_Row> retVals = new Vector<Return_Full_Link_Id_Row>();
                        if(Q.bulk_return_full_link_id(linkFromSet, retVals)!=QClass.APIFail){
                            for(Return_Full_Link_Id_Row row: retVals){
                                if (row.get_v5_categ().equals("letter_code") == false) {
                                    continue;
                                }
                                String temp = row.get_v8_cmv().getString();
                                if (temp.contains(searchVal)) {

                                    if (Q.set_current_node_id( row.get_v4_linkId()) != QClass.APIFail) {

                                        Q.set_put( filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                        StringObject cls = new StringObject();
                        IntegerObject clsID = new IntegerObject();
                        StringObject label = new StringObject();
                        IntegerObject linkID = new IntegerObject();
                        StringObject categ = new StringObject();
                        StringObject fromcls = new StringObject();
                        IntegerObject categID = new IntegerObject();
                        CMValue cmv = new CMValue();
                        IntegerObject uniq_categ = new IntegerObject();
                        
                        //Delete Letter codes
                        //For each link originating from current hierarchy ignore non letter_code links
                        while (Q.retur_full_link_id( linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                            if (categ.getValue().equals("letter_code") == false) {
                                continue;
                            }
                            String temp = cmv.getString();
                            if (temp.contains(searchVal)) {

                                if (Q.set_current_node_id( linkID.getValue()) != QClass.APIFail) {

                                    Q.set_put( filteredSet);
                                }
                            }
                        }
                        */
                        Q.reset_set( filteredSet);

                        //get hierarchy nodes that correspond to the filtered letter code values
                        int set_exclude_facets = Q.set_get_new();
                        set_exclude_facets = Q.get_from_value( filteredSet);
                        Q.reset_set( set_exclude_facets);

                        //Find Facets that inherit their letter Codes to their sub hierarchy classes -- stored in additional_hiers_set
                        int facetLCSet = Q.set_get_new();
                        Q.set_copy( facetLCSet, set_exclude_facets);

                        Q.reset_set( set_h);
                        Q.reset_set( facetLCSet);
                        Q.set_difference( facetLCSet, set_h);
                        Q.reset_set( facetLCSet);

                        int additional_hiers_set = Q.set_get_new();
                        additional_hiers_set = Q.get_subclasses( facetLCSet);

                        Q.reset_set( set_h);
                        Q.reset_set( additional_hiers_set);
                        Q.set_intersect( additional_hiers_set, set_h);
                        Q.reset_set( additional_hiers_set);

                        //Filter out letter code from nodes that do not belong to set_h -- are not hierarchies
                        Q.reset_set( set_h);
                        Q.reset_set( set_exclude_facets);
                        Q.set_intersect( set_exclude_facets, set_h);
                        Q.reset_set( set_exclude_facets);

                        //Union of these 2 partial results (hierarchyletter codes and inherieted letter codes)
                        Q.set_union( set_exclude_facets, additional_hiers_set);
                        Q.reset_set( set_exclude_facets);

                        //get facet nodes that correspond to the filtered letter code values and put them 
                        //in an exclude list. This is done because one facet may have multiple letter codes 
                        //but if one letter code equals to searchVal and all the others don't then the whole
                        //Facet must be excluded.

                        //this loop's partial results are calculated from the set difference between all facets and exluded facets
                        Q.reset_set( set_h);
                        Q.set_copy( set_partial_hierarchy_results, set_h);
                        Q.reset_set( set_partial_hierarchy_results);

                        Q.set_difference( set_partial_hierarchy_results, set_exclude_facets);
                        Q.reset_set( set_partial_hierarchy_results);
                        Q.free_set( set_exclude_facets);

                    }
                }

                //merge results of each loop. All first loop's results are included
                if (i == 0) {

                    //Q.set_copy( set_global_hierarchy_results, set_partial_hierarchy_results);
                    set_global_hierarchy_results = Q.set_get_new();
                    Q.reset_set( set_partial_hierarchy_results);
                    Q.set_copy( set_global_hierarchy_results, set_partial_hierarchy_results);
                    Q.reset_set( set_global_hierarchy_results);
                    //Q.set_union( set_global_facet_results, set_partial_facet_results);
                    continue;
                }
                //If conjuction operator == AND then set_intersect
                if (globalOperator.equalsIgnoreCase("AND")) {

                    Q.reset_set( set_global_hierarchy_results);
                    Q.reset_set( set_partial_hierarchy_results);

                    Q.set_intersect( set_global_hierarchy_results, set_partial_hierarchy_results);
                    Q.reset_set( set_global_hierarchy_results);
                    continue;
                }
                //If conjuction operator == OR then set_union
                if (globalOperator.equalsIgnoreCase("OR")) {

                    Q.reset_set( set_global_hierarchy_results);
                    Q.reset_set( set_partial_hierarchy_results);

                    Q.set_union( set_global_hierarchy_results, set_partial_hierarchy_results);
                    Q.reset_set( set_global_hierarchy_results);
                    continue;
                }

            }
        }
        Q.reset_set( set_global_hierarchy_results);

        // FILTER hierarchies depending on user group
        DBFilters dbf = new DBFilters();
        set_global_hierarchy_results = dbf.FilterHierResults(SessionUserInfo, set_global_hierarchy_results, Q, sis_session);

        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_global_hierarchy_results, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                globalHierachyResults.addElement(row.get_v1_cls_logicalname());
            }
        }
        /*StringObject c_name = new StringObject();
        while (Q.retur_nodes( set_global_hierarchy_results, c_name) != QClass.APIFail) {
            globalHierachyResults.addElement(c_name.getValue());
        }*/

        globalHierachyResults = dbGen.removePrefix(globalHierachyResults);
        Collections.sort(globalHierachyResults);

        Q.free_all_sets();

        return globalHierachyResults;

    }

    public void writeResultsInXMLFile(UserInfoClass SessionUserInfo, Vector<String> allHierarchies, Utilities u, String title, SearchCriteria sc, String[] output, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, QClass Q, IntegerObject sis_session,String pathToSaveScriptingAndLocale, Locale targetLocale) {

        DBGeneral dbGen = new DBGeneral();
        
        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";

        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
            String temp = "";

            temp += xml_header /*+xslLink*/ +
                    "<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">" +
                    "<title>" + title + "</title>" +
                    "<query>" + sc.getQueryString(u) + "</query>";
            if(pathToSaveScriptingAndLocale!=null){
                temp+="<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>";
            }
            out.write(temp);


        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("<data>");
            out.write("<output>");
            for (int m = 0; m < output.length; m++) {

                String category = output[m];
                if (category.compareTo("id") == 0 || category.compareTo("name") == 0) {
                    continue;
                } else {
                    out.write("<" + category + "/>");
                }
            }
            out.write("</output>");

            out.write("<hierarchies>");

            int resultsLIMIT = allHierarchies.size();
            for (int i = 0; i < resultsLIMIT; i++) {
                //Q.reset_name_scope();
                Q.free_all_sets();
                StringBuffer temp = new StringBuffer();
                temp.append("<hierarchy index=\"" + (i + 1) + "\">");

                for (int j = 0; j < output.length; j++) {
                    if (output[j].equals("name")) {
                        String currentHierarchy = (String) (allHierarchies.get(i));
                        temp.append("<name>" + Utilities.escapeXML(currentHierarchy) + "</name>");

                    } else {
                        Vector<String> v = dbGen.returnResults_Hierarchy(SessionUserInfo, allHierarchies.get(i).toString(), output[j], Q, sis_session, targetLocale);
                        Collections.sort(v, new StringLocaleComparator(targetLocale));

                        for (int k = 0; k < v.size(); k++) {
                            temp.append("<" + output[j] + ">");
                            temp.append(v.get(k));
                            temp.append("</" + output[j] + ">");
                        }
                    }
                }
                temp.append("</hierarchy>");
                out.write(temp.toString());
            }
            out.write("</hierarchies>");
            out.write("</data>");
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            out.write("</page>\n");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        //Q.reset_name_scope();
        Q.free_all_sets();

    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
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
