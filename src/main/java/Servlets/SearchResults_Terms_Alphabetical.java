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
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

//import isl.dms.DMSException;
//import isl.dms.xml.XMLTransform;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;
//import org.apache.xalan.xslt.Process;

/**
 *
 * @author tzortzak
 */
public class SearchResults_Terms_Alphabetical extends ApplicationBasicServlet {

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
        String startRecord = (String) request.getParameter("pageFirstResult") ;
        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                if(startRecord!=null && startRecord.matches("SaveAll")){
                    out.println("Session Invalidate");
                }
                else{
                    response.sendRedirect("Index");
                }
                return;
            }

            //Connection classes
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            //Parameters
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
            SearchCriteria searchCriteria;
            
            // -------------------- paging info And criteria retrieval-------------------------- 
            //initial values --> will change from the following code
            int alphabeticalPagingFirst = 1;
            int alphabeticalPagingListStep = Integer.valueOf(ListStepStr).intValue();
            int alphabeticalPagingQueryResultsCount = 0;
                        
            searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Terms");
            
            //tab pressed without any criteria set
            if(searchCriteria == null ){
                searchCriteria = SearchCriteria.createSearchCriteriaObject(SessionUserInfo, "SearchCriteria_Terms", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
                
            }
            
            if(startRecord == null){ //read paging criteria
                int index        = searchCriteria.pagingNames.indexOf("termsAlphabetical");
                alphabeticalPagingFirst = searchCriteria.pagingValues.get(index);
            }
            
            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {
                
                alphabeticalPagingFirst = Integer.parseInt(startRecord);
                if (alphabeticalPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("termsAlphabetical");
                    searchCriteria.pagingValues.set(index, alphabeticalPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
                    
                } else {
                    int index = searchCriteria.pagingNames.indexOf("termsAlphabetical");
                    alphabeticalPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }            
            
            String[] input = new String[searchCriteria.input.size()];
            String[] ops = new String[searchCriteria.operator.size()];
            String[] inputValue = new String[searchCriteria.value.size()];
            
            String operator = searchCriteria.CombineOperator;
            searchCriteria.input.toArray(input);
            searchCriteria.operator.toArray(ops);
            searchCriteria.value.toArray(inputValue);
            
            //Output required for alphabetical
            ArrayList<String> output = new ArrayList<String>();
            output.add(ConstantParameters.id_kwd);
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
                
            // handle search operators (not) starts / ends with
            u.InformSearchOperatorsAndValuesWithSpecialCharacters(input,ops, inputValue,false);
            //-------------------- paging info And criteria retrieval-------------------------- 
            
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            // timer begin
            long startTime = Utilities.startTimer();    
            
            HashMap<String, NodeInfoSortItemContainer> termsInfo = new HashMap<String, NodeInfoSortItemContainer>();              
            ArrayList<Long> resultNodesIdsL = new ArrayList<Long>();
                
            int set_global_descriptor_results = dbGen.getSearchTermResultSet(SessionUserInfo, input, ops, inputValue, operator,Q,TA,sis_session);
            boolean extendSearcResultsWithRnts = false;//searchCriteria.expandWithRecusiveNts;
            
            if (startRecord!=null && startRecord.matches("SaveAll")) {

                //Extra parameters 
                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Terms_Alphabetical_" + time;
                String XML = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml";
                String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Alphabetical.xsl").toString();
                String pathToLabels = context.getRealPath("/translations/labels.xml");
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                String resultsInfo = "<page language=\""+SessionUserInfo.UILang+"\" " +
                        "primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">" + 
                        "<title>" + time +"</title>" + 
                        "<query>" + searchCriteria.getQueryString(u) +"</query>" + 
                        u.getXMLUserInfo(SessionUserInfo) + 
                        "<pathToLabels>" + pathToLabels +"</pathToLabels>" + 
                        "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>" ;
                
                //Storage Structures
                
                ArrayList<String> allTerms = new ArrayList<String>();                
                //READ RESULT SET'S REQUESTED OUTPUT AND WRITE RESULTS IN XML FILE
                dbGen.collectTermSetInfo(SessionUserInfo, Q, TA, sis_session, set_global_descriptor_results, output, termsInfo, allTerms, resultNodesIdsL,extendSearcResultsWithRnts, null);
                dbGen.collectUsedForTermSetInfo(SessionUserInfo, Q, sis_session, set_global_descriptor_results, termsInfo, allTerms , resultNodesIdsL);
                

                //Collections.sort(allTerms, new StringLocaleComparator(targetLocale));         
                ArrayList<SortItem> resultsTermsInSortItems = Utilities.getSortItemVectorFromTermsInfoSortItemContainer(termsInfo, false);
                Collections.sort(resultsTermsInSortItems,transliterationComparator);
                allTerms.clear();
                allTerms.addAll(Utilities.getStringVectorFromSortItemVector(resultsTermsInSortItems));
            
                //Collections.sort(resultUFNodes, new SortItemLocaleComparator(targetLocale)); 
                
                //Write XML file
                u.writeResultsInXMLFile(null, allTerms, resultsInfo, output, webAppSaveResults_temporary_filesAbsolutePath,  Save_Results_file_name, Q, sis_session ,termsInfo,resultNodesIdsL,targetLocale,SessionUserInfo,false,true);
                
                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath +File.separator+ Save_Results_file_name + ".xml",
                                   XSL, 
                                   webAppSaveResults_temporary_filesAbsolutePath +File.separator+Save_Results_file_name.concat(".html"), sessionInstance.path +"/");
				
                //Send HTML relative url to output and return
                out.println(webAppSaveResults_Folder + "/"+webAppSaveResults_temporary_files_Folder +"/"+  Save_Results_file_name.concat(".html"));
               // out.println(fullName);
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in terms Alphabetical View : " + elapsedTimeSec);
                out.flush();
                return;
            }
            
            
            ArrayList<SortItem> allTerms = new ArrayList<SortItem>();
            ArrayList<String> resultsTerms = new ArrayList<String>();
            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            
            Q.reset_set(set_global_descriptor_results);
            ArrayList<Return_Full_Nodes_Row> retVals = new ArrayList<Return_Full_Nodes_Row>();
            if(Q.bulk_return_full_nodes(set_global_descriptor_results, retVals)!=QClass.APIFail){
                for(Return_Full_Nodes_Row row: retVals){
                    
                    String termName = dbGen.removePrefix(row.get_v2_node_logicalname());
                    SortItem termSortItem = new SortItem(termName,row.get_v1_sysid(),row.get_v5_node_transliteration(),row.get_v4_long_referenceId());
                    allTerms.add(termSortItem); 
                }
            }
            
            Collections.sort(allTerms, transliterationComparator);
            
            alphabeticalPagingQueryResultsCount = allTerms.size();
            if (alphabeticalPagingFirst > alphabeticalPagingQueryResultsCount) {
                alphabeticalPagingFirst = 1;
            }
            
            int set_paging_results = Q.set_get_new();
            Q.reset_set(set_paging_results);
            for (int i = 0; i < alphabeticalPagingListStep; i++) {
                if (i + alphabeticalPagingFirst > alphabeticalPagingQueryResultsCount) {
                    break;
                }
                long sysIdL= allTerms.get(i + alphabeticalPagingFirst - 1).sysid;
                resultNodesIdsL.add(sysIdL);
                Q.set_current_node_id(sysIdL);
                Q.set_put(set_paging_results);
                
            }
            Q.reset_set(set_paging_results);
            
            
            
            dbGen.collectTermSetInfo(SessionUserInfo, Q, TA, sis_session, set_paging_results, output, termsInfo, resultsTerms, resultNodesIdsL, extendSearcResultsWithRnts, null);
            dbGen.collectUsedForTermSetInfo(SessionUserInfo, Q, sis_session, set_paging_results, termsInfo, resultsTerms , resultNodesIdsL);
            
            //Collections.sort(resultsTerms, new StringLocaleComparator(targetLocale));     
            ArrayList<SortItem> resultsTermsInSortItems = Utilities.getSortItemVectorFromTermsInfoSortItemContainer(termsInfo, false);
            Collections.sort(resultsTermsInSortItems,transliterationComparator);
            resultsTerms.clear();
            resultsTerms.addAll(Utilities.getStringVectorFromSortItemVector(resultsTermsInSortItems));
            
            
            u.getResultsInXmlGuideTermSorting(resultsTerms, termsInfo, output, xmlResults, Q, sis_session, targetLocale,SessionUserInfo,false,false);
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
            // timer end
            float elapsedTimeSec = Utilities.stopTimer(startTime);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in terms Alphabetical View : " + elapsedTimeSec);
            
            
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
            xml.append(createPagingInfo(alphabeticalPagingListStep,alphabeticalPagingFirst,alphabeticalPagingQueryResultsCount));
            xml.append(u.getXMLMiddle(xmlResults.toString(), "Alphabetical"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/page_contents.xsl");
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    public String createPagingInfo(int PagingListStep, int pagingFirst, int alphabeticalPagingQueryResultsCount) {
        
        String pagingInfo = "";
        pagingInfo += "<alphabeticalresults>";
        pagingInfo += "<paging_info>";

        //This Servlet
        pagingInfo += "<ServletName>";
        pagingInfo += "SearchResults_Terms_Alphabetical";
        pagingInfo += "</ServletName>";

        //Time used to query db
        pagingInfo += "<query_results_time>";
        //pagingInfo += resultsTime;
        pagingInfo += "</query_results_time>";

        //How many query results 
        pagingInfo += "<pagingQueryResultsCount>";
        pagingInfo += alphabeticalPagingQueryResultsCount;
        pagingInfo += "</pagingQueryResultsCount>";

        //How many per page
        pagingInfo += "<pagingListStep>";
        pagingInfo += PagingListStep;
        pagingInfo += "</pagingListStep>";

        int pagingLast=0;
        //Prepare Last record index of global results for the next page
        if (pagingFirst + PagingListStep - 1 >= alphabeticalPagingQueryResultsCount) {
            pagingLast = alphabeticalPagingQueryResultsCount;
        } else {
            pagingLast = pagingFirst + PagingListStep - 1;
        }

        //The First record index of global results for the next page
        pagingInfo += "<pagingFirst>";
        pagingInfo += pagingFirst;
        pagingInfo += "</pagingFirst>";

        //The Last record index of global results for the next page
        pagingInfo += "<pagingLast>";
        pagingInfo += pagingLast;
        pagingInfo += "</pagingLast>";

        pagingInfo += "</paging_info>";
        
        pagingInfo += "</alphabeticalresults>";


        return pagingInfo;
    }

    
    public void xslTransform(PrintWriter out, StringBuffer xml, String xslFileName) {
        Utilities u = new Utilities();
        u.XmlPrintWriterTransform(out, xml, xslFileName);
        /*
        try {
            XMLTransform xmlD = new XMLTransform(xml.toString());
            xmlD.transform(out, xslFileName);
        } catch (DMSException ex) {
            Utils.StaticClass.handleException(ex);
        }
        out.flush();*/
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
