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
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.SearchCriteria;

import Utils.NodeInfoSortItemContainer;
import Utils.Parameters;
import Utils.Utilities;
import Utils.SortItem;
import Utils.SortItemComparator;
import XMLHandling.WriteFileData;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;


public class SearchResults_Terms extends ApplicationBasicServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        
        request.setCharacterEncoding("UTF-8");
        String outputMode = request.getParameter("answerType");
        if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){
            response.setContentType("text/xml;charset=UTF-8");
        }
        else{
            response.setContentType("text/html;charset=UTF-8");
        }
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
       
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance); 
        

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

            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            //TOOLS
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();

            //Servlet needed Parameters
            
            String updateTermsCriteria = (String) request.getParameter("updateTermCriteria");
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            
            Locale targetLocale = new Locale(language, country);

            SearchCriteria searchCriteria;
            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            float elapsedTimeSec;


            //Data Storage
            HashMap<String, NodeInfoSortItemContainer> termsInfo = new HashMap<String, NodeInfoSortItemContainer>();
            SortItemComparator transliterationComparator = new SortItemComparator(Utils.SortItemComparator.SortItemComparatorField.TRANSLITERATION);
            ArrayList<Long> resultNodesIds = new ArrayList<Long>();


            //open connection
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }



            // -------------------- paging info And criteria retrieval--------------------------

            //initial values --> will change from the following code
            int termsPagingListStep = new Integer(ListStepStr).intValue();
            int termsPagingFirst = 1;
            int termsPagingQueryResultsCount = 0;

            if (updateTermsCriteria != null) { // detect if search was pressed or left menu option was triggered
                searchCriteria = SearchCriteria.createSearchCriteriaObject(SessionUserInfo, "SearchCriteria_Terms", updateTermsCriteria, request, u);

                if (searchCriteria.input.size() == searchCriteria.value.size()) {
                    /*
                    for (int k = 0; k < searchCriteria.input.size(); k++) {
                        String inputKwd = searchCriteria.input.get(k);
                        String value = searchCriteria.value.get(k);
                        byte[] valbytes = value.getBytes("UTF-8");
                        if (inputKwd.equals("name")
                                || inputKwd.equals(ConstantParameters.bt_kwd)
                                || inputKwd.equals(ConstantParameters.nt_kwd)
                                || inputKwd.equals(ConstantParameters.rt_kwd)
                                || inputKwd.equals(ConstantParameters.topterm_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } 
                        else if (inputKwd.equals(ConstantParameters.status_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForStatus(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        }
                        else if (inputKwd.equals(ConstantParameters.uf_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForUF(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.facet_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForFacet(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.primary_found_in_kwd)
                                || inputKwd.equals(ConstantParameters.translations_found_in_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.tc_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForTaxonomicalcode(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.dn_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForDewey(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.translation_kwd)
                                || inputKwd.equals(ConstantParameters.uf_translations_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForTranslation(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.created_by_kwd)
                                || inputKwd.equals(ConstantParameters.modified_by_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForUser(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.created_on_kwd)
                                || inputKwd.equals(ConstantParameters.modified_on_kwd)) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if (valbytes.length > dbtr.getMaxBytesForDate(SessionUserInfo.selectedThesaurus, Q, sis_session)) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }
                        } else if (inputKwd.equals(ConstantParameters.scope_note_kwd)
                                || inputKwd.equals(ConstantParameters.translations_scope_note_kwd)
                                || inputKwd.equals(ConstantParameters.historical_note_kwd)
                                || inputKwd.equals(ConstantParameters.comment_kwd)) {
                            if (valbytes.length > (dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session)/4) ) {
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                                response.sendRedirect("Links?tab=SearchCriteria&CheckLength=true");
                                return;
                            }

                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                        }
                    }
                    */
                } else {
                    Utils.StaticClass.webAppSystemOutPrintln("Search Input Error");
                }


                sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
            } else {  //else try to read criteria for this user
                searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Terms");
            }

            if (searchCriteria == null) {//tab pressed without any criteria previously set -- > default == list all with default output
                searchCriteria = SearchCriteria.createSearchCriteriaObject(SessionUserInfo, "SearchCriteria_Terms", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
            }

            if (startRecord == null) { //paging criteria were not passed directly through url so read them from Search criteria object
                int index = searchCriteria.pagingNames.indexOf("termsResults");
                termsPagingFirst = searchCriteria.pagingValues.get(index);
            }

            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {
                termsPagingFirst = Integer.parseInt(startRecord);
                if (termsPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("termsResults");
                    searchCriteria.pagingValues.set(index, termsPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);

                } else {
                    int index = searchCriteria.pagingNames.indexOf("termsResults");
                    termsPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }

            String[] input = new String[searchCriteria.input.size()];
            String[] ops = new String[searchCriteria.operator.size()];
            String[] inputValue = new String[searchCriteria.value.size()];
            String operator = searchCriteria.CombineOperator;

            searchCriteria.input.toArray(input);
            searchCriteria.operator.toArray(ops);
            searchCriteria.value.toArray(inputValue);

            
            ArrayList<String> output = new ArrayList<String>();
            output.addAll(searchCriteria.output);
            if (output.contains(ConstantParameters.id_kwd) == false) { //id will always be requested
                output.add(ConstantParameters.id_kwd);
            }
            if (output.contains("name")) { //name is also always requested but no special handling is needed
                output.remove("name");
            }
            //if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){                
            if (output.contains(ConstantParameters.system_transliteration_kwd) == false) {//transliteration should always be added for sorting 
                output.add(ConstantParameters.system_transliteration_kwd);
            }

            // handle search operators (not) starts / ends with and statuses (may be defined in different language)
            u.InformSearchOperatorsAndValuesWithSpecialCharacters(input,ops, inputValue,false);

            //--------------------end of paging info And criteria retrieval--------------------------


            // timer begin
            long startTime = Utilities.startTimer();

            //perform search
            int set_global_descriptor_results = dbGen.getSearchTermResultSet(SessionUserInfo, input, ops, inputValue, operator, Q, TA, sis_session);


            if (startRecord != null && startRecord.matches("SaveAll")) {

                //Extra parameters
                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = context.getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                //String webAppSaveResults_temporary_filesAbsolutePath = Parameters.BaseRealPath+File.separator+webAppSaveResults_Folder + File.separator + webAppSaveResults_temporary_files_Folder;
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Terms_" + time;
                //String webAppSaveResults_AbsolutePath = context.getRealPath("/"+webAppSaveResults_Folder);
                String webAppSaveResults_AbsolutePathString = Parameters.BaseRealPath+File.separator+webAppSaveResults_Folder;
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms.xsl").toString();
                //String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                String pathToSaveScriptingAndLocale = Parameters.BaseRealPath+File.separator+"translations"+File.separator+"SaveAll_Locale_And_Scripting.xml";
                //Read Term Ids
                
                ArrayList<String> allTerms = new ArrayList<String>();
                
                //READ RESULT SET'S REQUESTED OUTPUT AND WRITE RESULTS IN XML FILE
                dbGen.collectTermSetInfo(SessionUserInfo, Q, TA, sis_session, set_global_descriptor_results, output, termsInfo, allTerms, resultNodesIds);
                

                ArrayList<SortItem> allTermsInSortItems = Utilities.getSortItemVectorFromTermsInfoSortItemContainer(termsInfo, false);
                Collections.sort(allTermsInSortItems,transliterationComparator);
                allTerms.clear();
                allTerms.addAll(Utilities.getStringVectorFromSortItemVector(allTermsInSortItems));
                //Collections.sort(allTerms, new StringLocaleComparator(targetLocale));
                
                //Write XML file
                String startXML = "<page language=\"" + SessionUserInfo.UILang + "\""+
                                       " primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">"
                                        + "<title>" + time + "</title><query>" + searchCriteria.getQueryString(u) + "</query>";

                if (outputMode != null && ( outputMode.compareTo("XML") == 0 ||outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0 )) {
                    //nothing
                } else {
                    startXML += "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>";
                }

                if(outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0 ){
                    u.writeResultsInXMLFile(out,allTerms, startXML, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session, termsInfo, resultNodesIds, targetLocale,SessionUserInfo,true,false);
                }
                else{
                    u.writeResultsInXMLFile(null,allTerms, startXML, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q, sis_session, termsInfo, resultNodesIds, targetLocale,SessionUserInfo,false,false);
                }
                // timer end
                elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in terms --> time elapsed: " + elapsedTimeSec);

                
                //close connection
                Q.free_set(set_global_descriptor_results);
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                
                if (outputMode != null && outputMode.compareTo("XML") == 0 ) {
                    if (Parameters.FormatXML) {
                        WriteFileData.formatXMLFile(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml");
                    }
                    
                    out.println(Save_Results_file_name.concat(".xml"));
                    out.flush();
                    
                } 
                else if(outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
                    //nothing to do results already streamed
                    out.flush();
                }
                else{
                    //transform XML to HTML
                    u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", 
                                       XSL, 
                                       webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));
                    
                    //Send HTML url to output and return
                    out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                    out.flush();

                }
                //out.flush();
                return;

            }

            ArrayList<SortItem> allTerms = new ArrayList<SortItem>();
            Q.reset_set(set_global_descriptor_results);
            
            ArrayList<Return_Full_Nodes_Row> retVals = new ArrayList<Return_Full_Nodes_Row>();
            if(Q.bulk_return_full_nodes(set_global_descriptor_results, retVals)!=QClass.APIFail){
                for(Return_Full_Nodes_Row row: retVals){
                    
                    String termName = dbGen.removePrefix(row.get_v2_node_logicalname());
                    String transliteration = row.get_v5_node_transliteration();
                    if(transliteration==null|| transliteration.length()==0){
                        transliteration = Utilities.getTransliterationString(termName, false);
                    }
                    SortItem termSortItem = new SortItem(termName, row.get_v1_sysid(),transliteration,row.get_v4_long_referenceId());
                    allTerms.add(termSortItem);  
                }
            }
            
            Collections.sort(allTerms, transliterationComparator);

            //Get only those terms that will appear in next page
            termsPagingQueryResultsCount = allTerms.size();
            if (termsPagingFirst > termsPagingQueryResultsCount) {
                termsPagingFirst = 1;
            }

            int set_paging_results = Q.set_get_new();
            Q.reset_set(set_paging_results);
            for (int i = 0; i < termsPagingListStep; i++) {
                if (i + termsPagingFirst > termsPagingQueryResultsCount) {
                    break;
                }
                long sysIdL = allTerms.get(i + termsPagingFirst - 1).sysid;
                resultNodesIds.add(sysIdL);
                Q.set_current_node_id(sysIdL);
                Q.set_put(set_paging_results);

            }
            Q.reset_set(set_paging_results);

            
            ArrayList<String> resultsTerms = new ArrayList<String>();
            dbGen.collectTermSetInfo(SessionUserInfo, Q, TA, sis_session, set_paging_results, output, termsInfo, resultsTerms, resultNodesIds);
            //Collections.sort(resultsTerms, new StringLocaleComparator(targetLocale));
            ArrayList<SortItem> resultsTermsInSortItems = Utilities.getSortItemVectorFromTermsInfoSortItemContainer(termsInfo, false);
            Collections.sort(resultsTermsInSortItems,transliterationComparator);
            
            u.getResultsInXml_ForTableLayout(SessionUserInfo,resultsTermsInSortItems, termsInfo, output, xmlResults, Q, sis_session, targetLocale);

            
            Q.free_set(set_paging_results);
            Q.free_set(set_global_descriptor_results);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            // timer end
            elapsedTimeSec = Utilities.stopTimer(startTime);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Search results in terms --> time elapsed: " + elapsedTimeSec);

            
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
            xml.append("<results>");
            xml.append(u.writePagingInfoXML(termsPagingListStep, termsPagingFirst, termsPagingQueryResultsCount, elapsedTimeSec, "SearchResults_Terms"));
            xml.append("</results>");
            xml.append(u.getXMLMiddle(xmlResults.toString(), "SearchResults"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");


        } catch (IOException | NumberFormatException e) {
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

    private InputStream getInputStream() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
