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
import Utils.SortItem;
import Utils.SortItemComparator;
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

//import org.apache.xalan.xslt.Process;
/**
 *
 * @author tzortzak
 */
public class SearchResults_Facets extends ApplicationBasicServlet {

    //final String xml_header = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";
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
        String startRecord = (String) request.getParameter("pageFirstResult");
        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                if (startRecord != null && startRecord.matches("SaveAll")) {
                    out.println("Session Invalidate");
                } else {
                    response.sendRedirect("Index");
                }
                return;
            }

            // open SIS and TMS connection
            QClass Q = new QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();

            SearchCriteria searchCriteria;

            String updateTermsCirteria = (String) request.getParameter("updateTermCriteria");
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");

            Locale targetLocale = new Locale(language, country);

            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            // -------------------- paging info And criteria retrieval--------------------------
            //initial values --> will change from the following code
            int facetsPagingListStep = new Integer(ListStepStr).intValue();
            int facetsPagingFirst = 1;
            int facetsPagingQueryResultsCount = 0;

            if (updateTermsCirteria != null) { // detect if search was pressed or left menu option was triggered
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Facets", updateTermsCirteria, request, u);

                if (searchCriteria.input.size() == searchCriteria.value.size()) {
                    /*
                     for(int k=0; k<searchCriteria.input.size(); k++){
                     String inputKwd = searchCriteria.input.get(k);
                     String value = searchCriteria.value.get(k);
                     byte[] valbytes = value.getBytes("UTF-8");
                     if(inputKwd.equals("name")){
                     //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                     if(valbytes.length > dbtr.getMaxBytesForFacet(SessionUserInfo.selectedThesaurus, Q, sis_session)){

                     //end query and close connection
                     Q.free_all_sets();
                     Q.CHECK_end_query();
                     dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                     response.sendRedirect("Links?tab=FacetsSearchCriteria&CheckLength=true");
                     return;
                     }
                     }
                     else if(inputKwd.equals("term")){
                            
                     //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                     if(valbytes.length > dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session)){

                     //end query and close connection
                     Q.free_all_sets();
                     Q.CHECK_end_query();
                     dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                     response.sendRedirect("Links?tab=FacetsSearchCriteria&CheckLength=true");
                     return;
                     }
                     }
                     }
                     */
                } else {
                    Utils.StaticClass.webAppSystemOutPrintln("Search Input Error");
                }
                sessionInstance.setAttribute("SearchCriteria_Facets", searchCriteria);

            } else {  //else try to read criteria for this user
                searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Facets");
            }

            if (searchCriteria == null) {//tab pressed without any criteria previously set -- > default == list all with default output
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Facets", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Facets", searchCriteria);
            }

            if (startRecord == null) { //read paging criteria
                int index = searchCriteria.pagingNames.indexOf("facetsResults");
                facetsPagingFirst = searchCriteria.pagingValues.get(index);
            }

            //if ALL then left_menu was triggered and criteria were updated with pageFirst = 1
            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {

                facetsPagingFirst = Integer.parseInt(startRecord);
                if (facetsPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("facetsResults");
                    searchCriteria.pagingValues.set(index, facetsPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Facets", searchCriteria);

                } else {
                    int index = searchCriteria.pagingNames.indexOf("facetsResults");
                    facetsPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }

            String[] input = new String[searchCriteria.input.size()];
            searchCriteria.input.toArray(input);
            String[] ops = new String[searchCriteria.operator.size()];
            searchCriteria.operator.toArray(ops);
            String[] inputValue = new String[searchCriteria.value.size()];
            searchCriteria.value.toArray(inputValue);
            String operator = searchCriteria.CombineOperator;
            String[] output = new String[searchCriteria.output.size()];
            searchCriteria.output.toArray(output);

            // handle search operators (not) starts / ends with
            u.InformSearchOperatorsAndValuesWithSpecialCharacters(input, ops, inputValue,false);
            //-------------------- paging info And criteria retrieval-------------------------- 

            StringBuffer xml = new StringBuffer();

            // timer begin
            long startTime = Utilities.startTimer();

            ArrayList<SortItem> allResultsFacets = getAllSearchFacets(SessionUserInfo, input, ops, inputValue, operator, Q, TA, sis_session);
            Collections.sort(allResultsFacets, new SortItemComparator(Utils.SortItemComparator.SortItemComparatorField.TRANSLITERATION));

            if (startRecord != null && startRecord.matches("SaveAll")) {

                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Facets_" + time;
                String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/" + webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Facets.xsl").toString();

                if (outputMode != null && outputMode.compareTo("XML") == 0) {
                    pathToSaveScriptingAndLocale = null;
                }

                if(outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0 ){
                    writeResultsInXMLFile(out, SessionUserInfo, allResultsFacets, u, time, searchCriteria, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session, pathToSaveScriptingAndLocale, targetLocale);
                }
                else{
                    writeResultsInXMLFile(null, SessionUserInfo, allResultsFacets, u, time, searchCriteria, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session, pathToSaveScriptingAndLocale, targetLocale);
                }

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                
                if (outputMode != null && outputMode.compareTo("XML") == 0) {
                    if (Parameters.FormatXML) {

                        WriteFileData.formatXMLFile(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml");

                    }
                    out.println(Save_Results_file_name.concat(".xml"));
                } 
                else if(outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
                    //nothing to do results already streamed
                    out.flush();
                }
                else {
                    String arg1 = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";
                    String arg2 = XSL;
                    String arg3 = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html");
                    u.XmlFileTransform(arg1, arg2, arg3);

                    //Send HTML relative url to output and return
                    out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));

                }

                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in facets  --> time elapsed: " + elapsedTimeSec);

                out.flush();
                return;
            }

            //Get only those facets that will appear in next page
            facetsPagingQueryResultsCount = allResultsFacets.size();
            if (facetsPagingFirst > facetsPagingQueryResultsCount) {
                facetsPagingFirst = 1;
            }
            ArrayList<String> resultsFacets = new ArrayList<String>();
            for (int i = 0; i < facetsPagingListStep; i++) {
                if (i + facetsPagingFirst > facetsPagingQueryResultsCount) {
                    break;
                }
                String tmp = allResultsFacets.get(i + facetsPagingFirst - 1).getLogName();
                resultsFacets.add(tmp);
            }

            String xmlResults = u.getResultsInXml_Facet(SessionUserInfo, resultsFacets, output, Q, sis_session, targetLocale, dbGen);

            // timer end
            float elapsedTimeSec = Utilities.stopTimer(startTime);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in facets  --> time elapsed: " + elapsedTimeSec);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            xmlResults += "<results>";
            xmlResults += (u.writePagingInfoXML(facetsPagingListStep, facetsPagingFirst, facetsPagingQueryResultsCount, elapsedTimeSec, "SearchResults_Facets"));
            xmlResults += "</results>";

            xml.append(u.getXMLStart(ConstantParameters.LMENU_FACETS));
            xml.append(u.getXMLMiddle(xmlResults, "SearchFacetResults"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0){
                if(session!=null) {session.invalidate();}
            }
            else{
                sessionInstance.writeBackToSession(session);
            }
        }
    }

    /**
     * Returns a Vector with the facets which match with criteria
     */
    public ArrayList<SortItem> getAllSearchFacets(UserInfoClass SessionUserInfo, String[] input, String[] operators, String[] inputValues, String globalOperator,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        ArrayList<SortItem> globalFacetResults = new ArrayList<SortItem>();
        DBGeneral dbG = new DBGeneral();

        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int set_global_facet_results = -1;

        String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        // looking for EKTFacet
        StringObject Facets = new StringObject();
        dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), Facets);

        // get instances of EKTFacet 
        Q.reset_name_scope();
        Q.set_current_node(Facets);

        int set_f = Q.get_instances(0);
        Q.reset_set(set_f);

        // get instances of EKTObsoleteFacet 
        StringObject ObsoleteFacets = new StringObject();
        dbtr.getThesaurusClass_ObsoleteFacet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), ObsoleteFacets);

        Q.reset_name_scope();
        Q.set_current_node(ObsoleteFacets);
        int set_of = Q.get_instances(0);
        Q.reset_set(set_of);

        Q.set_union(set_f, set_of);
        Q.reset_set(set_f);

        Q.reset_name_scope();

        if (globalOperator.equals("*") || globalOperator.equals("all")) {
            set_global_facet_results = Q.set_get_new();
            Q.reset_set(set_f);
            Q.set_copy(set_global_facet_results, set_f);
            Q.reset_set(set_global_facet_results);
        } else {
            for (int i = 0; i < input.length; i++) {
                String currentInput = input[i];
                String currentOperator = operators[i];
                Q.reset_set(set_f);
                int set_partial_facet_results = Q.set_get_new();
                String searchVal = inputValues[i];

                Q.reset_name_scope();

                //Case Of Facet "term" criteria
                if (currentInput.equalsIgnoreCase("term")) {
                    // get the terms with the given criteria
                    String[] term_field = {"name"};
                    String[] term_operator = new String[1];
                    term_operator[0] = currentOperator;
                    String[] term_inputValue = new String[1];
                    term_inputValue[0] = searchVal;
                    int descriptor_results_set = dbG.getSearchTermResultSet(SessionUserInfo, term_field, term_operator, term_inputValue, globalOperator, Q, TA, sis_session);
                    Q.reset_set(descriptor_results_set);
                    // get the classes of the found terms
                    int descriptor_results_Classes_set = Q.get_classes(descriptor_results_set);
                    Q.reset_set(descriptor_results_Classes_set);
                    // get the superclasses of the above classes
                    set_partial_facet_results = Q.get_superclasses(descriptor_results_Classes_set);
                    Q.free_set(descriptor_results_Classes_set);
                    // intersect them with the facets set
                    Q.set_intersect(set_partial_facet_results, set_f);
                    Q.reset_set(set_partial_facet_results);
                }

                //Case Of Facet Name criteria
                if (currentInput.equalsIgnoreCase("name")) {

                    if (currentOperator.equals(ConstantParameters.searchOperatorEquals)) {

                        if (searchVal != null && searchVal.trim().length() > 0) {
                            if (Q.set_current_node(new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {

                                /*
                                 CMValue prm_val = new CMValue();
                                 prm_val.assign_string(searchVal);
                                 int ptrn_set = Q.set_get_new();
                                 Q.set_put_prm( ptrn_set, prm_val);
                                
                                 Q.reset_set( set_f);
                                 Q.reset_set( ptrn_set);
                                 set_partial_facet_results = Q.get_matched( set_f, ptrn_set);
                                 Q.reset_set( set_partial_facet_results);
                                 */
                                Q.set_put(set_partial_facet_results);
                                Q.reset_set(set_partial_facet_results);

                            }
                        }
                    } 
                    else if (currentOperator.equals(ConstantParameters.searchOperatorTransliterationContains)) {
                        Q.reset_set(set_f);
                        set_partial_facet_results = Q.get_matched_OnTransliteration(set_f, Utilities.getTransliterationString(searchVal,false),false);
                        Q.reset_set(set_partial_facet_results);

                    }
                    else if (currentOperator.equals(ConstantParameters.searchOperatorContains)) {
                        // <editor-fold defaultstate="collapsed" desc="Code for Contains">
                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm( ptrn_set, prm_val);
                        Q.reset_set(set_f);
                        //Q.reset_set( ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_partial_facet_results = Q.get_matched_case_insensitive( set_f, ptrn_set,1);
                        //set_partial_facet_results = Q.get_matched( set_f, ptrn_set);
                        if(Parameters.SEARCH_MODE_CASE_INSENSITIVE ){
                            set_partial_facet_results = Q.get_matched_CaseInsensitive(set_f, searchVal, true);                            
                        }
                        else{
                            set_partial_facet_results = Q.get_matched_ToneAndCaseInsensitive(set_f, searchVal, false);
                        }
                        
                        
                        Q.reset_set(set_partial_facet_results);
                        //</editor-fold>
                    } else if (currentOperator.equals("!")) {

                        int set_exclude_facets = Q.set_get_new();

                        if (Q.set_current_node(new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                            /*
                             CMValue prm_val = new CMValue();
                             prm_val.assign_string(searchVal);
                             int ptrn_set = Q.set_get_new();
                             Q.set_put_prm( ptrn_set, prm_val);
                            
                             Q.reset_set( set_f);
                             Q.reset_set( ptrn_set);
                             set_exclude_facets = Q.get_matched( set_f, ptrn_set);
                             Q.reset_set( set_partial_facet_results);
                             */
                            Q.set_put(set_exclude_facets);
                            Q.reset_set(set_exclude_facets);
                        }

                        Q.reset_set(set_f);
                        Q.reset_set(set_partial_facet_results);
                        Q.set_copy(set_partial_facet_results, set_f);

                        Q.reset_set(set_partial_facet_results);
                        Q.reset_set(set_exclude_facets);
                        Q.set_difference(set_partial_facet_results, set_exclude_facets);
                        Q.reset_set(set_partial_facet_results);

                    } else if (currentOperator.equals(ConstantParameters.searchOperatorNotContains)) {

                        //int set_exclude_facets = Q.set_get_new();
                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm( ptrn_set, prm_val);
                        Q.reset_set(set_f);
                        //Q.reset_set( ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_exclude_facets = Q.get_matched_case_insensitive( set_f, ptrn_set,1);
                        //set_exclude_facets = Q.get_matched( set_f, ptrn_set);
                        int set_exclude_facets = -1;
                        if(Parameters.SEARCH_MODE_CASE_INSENSITIVE ){
                            set_exclude_facets = Q.get_matched_CaseInsensitive(set_f, searchVal, true);                            
                        }
                        else{
                            set_exclude_facets = Q.get_matched_ToneAndCaseInsensitive(set_f, searchVal, false);
                        }

                        Q.reset_set(set_f);
                        Q.reset_set(set_partial_facet_results);
                        Q.set_copy(set_partial_facet_results, set_f);

                        Q.reset_set(set_partial_facet_results);
                        Q.reset_set(set_exclude_facets);
                        Q.set_difference(set_partial_facet_results, set_exclude_facets);
                        Q.reset_set(set_partial_facet_results);

                    }
                    else if (currentOperator.equals(ConstantParameters.searchOperatorNotTransliterationContains)) {

                        Q.reset_set(set_f);
                        int set_exclude_facets = Q.get_matched_OnTransliteration(set_f, Utilities.getTransliterationString(searchVal,false),false);
                        
                        Q.reset_set(set_f);
                        Q.reset_set(set_partial_facet_results);
                        Q.set_copy(set_partial_facet_results, set_f);

                        Q.reset_set(set_partial_facet_results);
                        Q.reset_set(set_exclude_facets);
                        Q.set_difference(set_partial_facet_results, set_exclude_facets);
                        Q.reset_set(set_partial_facet_results);

                    }
                } //Case Of letter_code's value criteria //NOT USED BEACUSE OF CRITERIA FACETS XSL
                else if (currentInput.equalsIgnoreCase("letter_code")) {

                    if (currentOperator.equals(ConstantParameters.searchOperatorEquals)) {

                        //get all facets that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set(set_f);
                        linkFromSet = Q.get_link_from_by_category(set_f, new StringObject("Facet"), new StringObject("letter_code"));
                        Q.reset_set(set_partial_facet_results);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        ArrayList<Return_Link_Id_Row> retVals = new ArrayList<>();
                        if (Q.bulk_return_link_id(linkFromSet, retVals) != QClass.APIFail) {
                            for (Return_Link_Id_Row row : retVals) {
                                String temp = row.get_v4_cmv().getString();
                                if (temp.equals(searchVal)) {
                                    if (Q.set_current_node_id(row.get_v3_sysid()) != QClass.APIFail) {
                                        Q.set_put(filteredSet);
                                    }
                                }
                            }
                        }

                        /*
                         StringObject c_name = new StringObject();
                         IntegerObject fId = new IntegerObject();
                         IntegerObject sysId = new IntegerObject();
                         IntegerObject traversed = new IntegerObject();
                         CMValue c_val = new CMValue();
                         while (Q.retur_link_id( linkFromSet, c_name, fId, sysId, c_val, traversed) != QClass.APIFail) {

                         String temp = c_val.getString();
                         if (temp.equals(searchVal)) {

                         if (Q.set_current_node_id( sysId.getValue()) != QClass.APIFail) {

                         Q.set_put( filteredSet);
                         }
                         }
                         }
                         */
                        Q.reset_set(filteredSet);

                        //get facet nodes that correspond to the filtered letter code values
                        set_partial_facet_results = Q.get_from_value(filteredSet);
                        Q.reset_set(set_partial_facet_results);

                    } else if (currentOperator.equals(ConstantParameters.searchOperatorContains)) {
                        // <editor-fold defaultstate="collapsed" desc="Code for Contains letter code">

                        //get all facets that have letter codes
                        Q.reset_set(set_f);
                        int linkFromSet = Q.get_link_from_by_category(set_f, new StringObject("Facet"), new StringObject("letter_code"));
                        Q.reset_set(set_partial_facet_results);

                        //select only those that have one letter code value containing searchVal
                        int filteredSet = Q.set_get_new();

                        ArrayList<Return_Link_Id_Row> retVals = new ArrayList<>();
                        if (Q.bulk_return_link_id(linkFromSet, retVals) != QClass.APIFail) {
                            for (Return_Link_Id_Row row : retVals) {
                                String temp = row.get_v4_cmv().getString();
                                if (temp.contains(searchVal)) {

                                    if (Q.set_current_node_id(row.get_v3_sysid()) != QClass.APIFail) {

                                        Q.set_put(filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                         StringObject c_name = new StringObject();
                         IntegerObject fId = new IntegerObject();
                         IntegerObject sysId = new IntegerObject();
                         IntegerObject traversed = new IntegerObject();
                         CMValue c_val = new CMValue();
                         while (Q.retur_link_id( linkFromSet, c_name, fId, sysId, c_val, traversed) != QClass.APIFail) {

                         String temp = c_val.getString();
                         if (temp.contains(searchVal)) {

                         if (Q.set_current_node_id( sysId.getValue()) != QClass.APIFail) {

                         Q.set_put( filteredSet);
                         }
                         }
                         }
                         */
                        Q.reset_set(filteredSet);

                        //get facet nodes that correspond to the filtered letter code values
                        set_partial_facet_results = Q.get_from_value(filteredSet);
                        Q.reset_set(set_partial_facet_results);
                        //</editor-fold>
                    } else if (currentOperator.equals("!")) {

                        //get all facets that have letter codes
                        int linkFromSet = Q.set_get_new();
                        Q.reset_set(set_f);
                        linkFromSet = Q.get_link_from_by_category(set_f, new StringObject("Facet"), new StringObject("letter_code"));
                        Q.reset_set(set_partial_facet_results);

                        //select only those that have one letter code value equal to searchVal
                        int filteredSet = Q.set_get_new();

                        ArrayList<Return_Link_Id_Row> retVals = new ArrayList<>();
                        if (Q.bulk_return_link_id(linkFromSet, retVals) != QClass.APIFail) {
                            for (Return_Link_Id_Row row : retVals) {
                                String temp = row.get_v4_cmv().getString();
                                if (temp.equals(searchVal)) {

                                    if (Q.set_current_node_id(row.get_v3_sysid()) != QClass.APIFail) {

                                        Q.set_put(filteredSet);
                                    }
                                }
                            }
                        }
                        /*
                         StringObject c_name = new StringObject();
                         IntegerObject fId = new IntegerObject();
                         IntegerObject sysId = new IntegerObject();
                         IntegerObject traversed = new IntegerObject();
                         CMValue c_val = new CMValue();
                         while (Q.retur_link_id( linkFromSet, c_name, fId, sysId, c_val, traversed) != QClass.APIFail) {

                         String temp = c_val.getString();
                         if (temp.equals(searchVal)) {

                         if (Q.set_current_node_id( sysId.getValue()) != QClass.APIFail) {

                         Q.set_put( filteredSet);
                         }
                         }
                         }
                         */
                        Q.reset_set(filteredSet);

                        //get facet nodes that correspond to the filtered letter code values and put them 
                        //in an exclude list. This is done because one facet may have multiple letter codes 
                        //but if one letter code equals to searchVal and all the others don't then the whole
                        //Facet must be excluded.
                        int set_exclude_facets = Q.get_from_value(filteredSet);

                        //this loop's partial results are calculated from the set difference between all facets and exluded facets
                        Q.reset_set(set_f);
                        Q.set_copy(set_partial_facet_results, set_f);

                        Q.reset_set(set_partial_facet_results);
                        Q.reset_set(set_exclude_facets);
                        Q.set_difference(set_partial_facet_results, set_exclude_facets);
                        Q.reset_set(set_partial_facet_results);

                    } else if (currentOperator.equals(ConstantParameters.searchOperatorNotContains)) {

                        //get all facets that have letter codes
                        Q.reset_set(set_f);
                        int linkFromSet = Q.get_link_from_by_category(set_f, new StringObject("Facet"), new StringObject("letter_code"));
                        Q.reset_set(set_partial_facet_results);

                        int filteredSet = Q.set_get_new();

                        ArrayList<Return_Link_Id_Row> retVals = new ArrayList<>();
                        if (Q.bulk_return_link_id(linkFromSet, retVals) != QClass.APIFail) {
                            retVals.forEach((row) -> {
                                String temp = row.get_v4_cmv().getString();
                                if (temp.contains(searchVal)) {
                                    if (Q.set_current_node_id(row.get_v3_sysid()) != QClass.APIFail) {
                                        Q.set_put(filteredSet);
                                    }
                                }
                            });
                        }
                        /*
                         StringObject c_name = new StringObject();
                         IntegerObject fId = new IntegerObject();
                         IntegerObject sysId = new IntegerObject();
                         IntegerObject traversed = new IntegerObject();
                         CMValue c_val = new CMValue();

                        
                         while (Q.retur_link_id( linkFromSet, c_name, fId, sysId, c_val, traversed) != QClass.APIFail) {

                         String temp = c_val.getString();
                         if (temp.contains(searchVal)) {

                         if (Q.set_current_node_id( sysId.getValue()) != QClass.APIFail) {

                         Q.set_put( filteredSet);
                         }
                         }
                         }
                         */
                        Q.reset_set(filteredSet);

                        //get facet nodes that correspond to the filtered letter code values and put them 
                        //in an exclude list. This is done because one facet may have multiple letter codes 
                        //but if one letter code equals to searchVal and all the others don't then the whole
                        //Facet must be excluded.
                        int set_exclude_facets = Q.set_get_new();
                        set_exclude_facets = Q.get_from_value(filteredSet);

                        //this loop's partial results are calculated from the set difference between all facets and exluded facets
                        Q.reset_set(set_f);
                        Q.set_copy(set_partial_facet_results, set_f);

                        Q.reset_set(set_partial_facet_results);
                        Q.reset_set(set_exclude_facets);
                        Q.set_difference(set_partial_facet_results, set_exclude_facets);
                        Q.reset_set(set_partial_facet_results);
                    }
                }

                //merge results of each loop. All first loop's results are included
                if (i == 0) {

                    set_global_facet_results = Q.set_get_new();
                    Q.reset_set(set_partial_facet_results);
                    Q.set_copy(set_global_facet_results, set_partial_facet_results);
                    Q.reset_set(set_global_facet_results);
                    continue;
                }
                //If conjuction operator == AND then set_intersect
                if (globalOperator.equalsIgnoreCase("AND")) {

                    Q.reset_set(set_global_facet_results);
                    Q.reset_set(set_partial_facet_results);
                    Q.set_intersect(set_global_facet_results, set_partial_facet_results);
                    Q.reset_set(set_global_facet_results);
                    continue;
                }
                //If conjuction operator == OR then set_union
                if (globalOperator.equalsIgnoreCase("OR")) {

                    Q.reset_set(set_global_facet_results);
                    Q.reset_set(set_partial_facet_results);
                    Q.set_union(set_global_facet_results, set_partial_facet_results);
                    Q.reset_set(set_global_facet_results);
                    continue;
                }

            }
        }
        Q.reset_set(set_global_facet_results);

        // FILTER facets depending on user group
        DBFilters dbf = new DBFilters();
        set_global_facet_results = dbf.FilterFacetResults(SessionUserInfo, set_global_facet_results, Q, sis_session);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<>();
        if (Q.bulk_return_nodes(set_global_facet_results, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                globalFacetResults.add(new SortItem(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId(), row.get_v3_cls_transliteration(), row.get_v2_long_referenceId()));
            }
        }
        /*StringObject c_name = new StringObject();
         while (Q.retur_nodes( set_global_facet_results, c_name) != QClass.APIFail) {
         globalFacetResults.addElement(c_name.getValue());
         }*/

        globalFacetResults = dbG.removeSortItemArrayListPrefix(globalFacetResults);
        Collections.sort(globalFacetResults, new SortItemComparator(Utils.SortItemComparator.SortItemComparatorField.TRANSLITERATION));

        Q.free_all_sets();

        return globalFacetResults;

    }

    public void writeResultsInXMLFile(PrintWriter outStream, UserInfoClass SessionUserInfo, ArrayList<SortItem> allFacets, Utilities u, String title, SearchCriteria sc, String[] output, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, QClass Q, IntegerObject sis_session, String pathToSaveScriptingAndLocale, Locale targetLocale) {

        boolean streamOutput = false;
        if (outStream != null) {
            streamOutput = true;
        }

        OutputStreamWriter out = null;

        DBGeneral dbGen = new DBGeneral();

        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";

        String appendVal = ConstantParameters.xmlHeader
                + "\n<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">";
        if (streamOutput) {
            outStream.append(appendVal);
        } else {
            try {
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");
                //out = new OutputStreamWriter(bout, "UTF8");
                appendVal += "<title>" + title + "</title>"
                        + "<query>" + sc.getQueryString(u) + "</query>";
                if (pathToSaveScriptingAndLocale != null) {
                    appendVal += "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>";
                }
                out.append(appendVal);
            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }

        try {
            appendVal = "<data thesaurus=\"" + SessionUserInfo.selectedThesaurus.toUpperCase() + "\" translationsSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">";
            if (!streamOutput) {
                //not of interest in xmlstream
                appendVal += "<output>";
                for (String category : output) {

                    if (category.compareTo(ConstantParameters.id_kwd) != 0 && category.compareTo("name") != 0) {
                        appendVal += "<" + category + "/>";
                    }
                }
                appendVal += "</output>";
            }
            appendVal += "<facets count=\""+ allFacets.size()+"\">";

            if (streamOutput) {
                outStream.append(appendVal);
            } else {
                out.append(appendVal);
            }
            SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
            StringBuilder temp;
            int resultsLIMIT = allFacets.size();
            for (int i = 0; i < resultsLIMIT; i++) {

                SortItem currentFacetSortItem = allFacets.get(i);
                Q.free_all_sets();

                temp = new StringBuilder();
                temp.append("<facet index=\"" + (i + 1) + "\">");

                for (String currentOutput : output) {

                    if (currentOutput.equals("name")) {
                        String currentFacet = currentFacetSortItem.getLogName();
                        String transliteration = currentFacetSortItem.getLogNameTransliteration();
                        long refId = currentFacetSortItem.getThesaurusReferenceId();
                        temp.append("<name");
                        if (refId > 0) {
                            temp.append(" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + refId + "\"");
                        }
                        temp.append(">" + Utilities.escapeXML(currentFacet) + "</name>");
                        temp.append("<" + ConstantParameters.system_transliteration_kwd + ">" + transliteration + "</" + ConstantParameters.system_transliteration_kwd + ">");

                    } else if (currentOutput.equals("hierarchy")){

                        ArrayList<SortItem> v = dbGen.returnResults_FacetInSortItems(SessionUserInfo, currentFacetSortItem.getLogName(), currentOutput, Q, sis_session, targetLocale);
                        Collections.sort(v, transliterationComparator);

                        for (SortItem hier : v ) {
                            temp.append("<" + currentOutput);
                            if(hier.getThesaurusReferenceId()>0){
                               temp.append(" " + ConstantParameters.system_referenceIdAttribute_kwd+"=\""+ hier.getThesaurusReferenceId()+"\"");
                            }
                            temp.append(">"+hier.getLogName());
                            temp.append("</" + currentOutput + ">");
                        }

                    }
                    else{

                        ArrayList<String> v = dbGen.returnResults_Facet(SessionUserInfo, currentFacetSortItem.getLogName(), currentOutput, Q, sis_session, targetLocale);
                        Collections.sort(v, new StringLocaleComparator(targetLocale));

                        for (String val : v ) {
                            temp.append("<" + currentOutput + ">");
                            temp.append(val);
                            temp.append("</" + currentOutput + ">");
                        }

                    }
                }
                temp.append("</facet>");

                if (streamOutput) {
                    outStream.append(temp.toString());
                } else {
                    out.append(temp.toString());
                }

            }
            if (streamOutput) {
                outStream.append("</facets></data></page>");
            } else {
                out.append("</facets></data></page>");
            }
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        if (!streamOutput) {
            try {
                out.close();
            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }

        Q.free_all_sets();

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
