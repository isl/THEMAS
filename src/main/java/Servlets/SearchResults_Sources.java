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
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.SearchCriteria;

import Utils.Parameters;
import Utils.Utilities;
import Utils.StringLocaleComparator;

import XMLHandling.WriteFileData;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Vector;
import java.util.Collections;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletContext;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class SearchResults_Sources extends ApplicationBasicServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance); 

        PrintWriter out = response.getWriter();
        String startRecord = (String) request.getParameter("pageFirstResult");
        try {

            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            if (sessionInstance.getAttribute("SessionUser") == null) {
                if(startRecord!=null && startRecord.matches("SaveAll")){
                    out.println("Session Invalidate");
                }
                else{
                    response.sendRedirect("Index");
                }
                return;
            }

            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            
            String updateSourceCriteria = (String) request.getParameter("updateSourceCriteria");
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            String answerType = request.getParameter("answerType");
            Locale targetLocale = new Locale(language, country);

            boolean usePreviousCriteria = true;
            if(request.getParameter("usePreviousCriteria")!=null && request.getParameter("usePreviousCriteria").compareTo("false")==0){
                usePreviousCriteria = false;
            }
            SearchCriteria searchCriteria = null;

            //initial values --> will change from the following code
            int sourcesPagingListStep = new Integer(ListStepStr).intValue();
            int sourcesPagingFirst = 1;
            int sourcesPagingQueryResultsCount = 0;

            StringBuffer xml = new StringBuffer("");

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            // -------------------- paging info And criteria retrieval--------------------------
            if (updateSourceCriteria != null) { // detect if search was pressed or left menu option was triggered
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Sources", updateSourceCriteria, request, u);
                if(searchCriteria.input.size()==searchCriteria.value.size()){
                    /*
                    for(int k=0; k<searchCriteria.input.size(); k++){
                        String inputKwd = searchCriteria.input.get(k);
                        String value = searchCriteria.value.get(k);
                        byte[] valbytes = value.getBytes("UTF-8");
                        if(inputKwd.equals("name")){
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if(valbytes.length > dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session)){

                                //end query and close connection
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                                response.sendRedirect("Links?tab=SourcesSearchCriteria&CheckLength=true");
                                return;
                            }
                        }
                        else if(inputKwd.equals(ConstantParameters.primary_found_in_kwd) || inputKwd.equals(ConstantParameters.translations_found_in_kwd)){
                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                            if(valbytes.length > dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session)){

                                //end query and close connection
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                                response.sendRedirect("Links?tab=SourcesSearchCriteria&CheckLength=true");
                                return;
                            }
                        }
                        else if(inputKwd.equals(ConstantParameters.source_note_kwd)){
                            if(valbytes.length > dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session)){

                                //end query and close connection
                                Q.free_all_sets();
                                Q.CHECK_end_query();
                                dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                                response.sendRedirect("Links?tab=SourcesSearchCriteria&CheckLength=true");
                                return;
                            }

                            //Utils.StaticClass.webAppSystemOutPrintln("Kwd: " + inputKwd + " value: " + value);
                        }
                    }*/
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Search Input Error");
                }
                sessionInstance.setAttribute("SearchCriteria_Sources", searchCriteria);
                
            } else {  //else try to read criteria for this user
                searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Sources");
            }

            if (searchCriteria == null) {//tab pressed without any criteria previously set -- > default == list all with default output
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Sources", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Sources", searchCriteria);
                
            }
            
            if(usePreviousCriteria==false){ // used for view alla sources from edit term's SN, SN_TR or HN in order not to modify previously defined criteria
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Sources", "*", request, u);
            }
            
            if (startRecord == null) { //read paging criteria
                int index = searchCriteria.pagingNames.indexOf("sourcesResults");
                sourcesPagingFirst = searchCriteria.pagingValues.get(index);
            }

            //if ALL then left_menu was triggered and criteria were updated with pageFirst = 1
            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {

                sourcesPagingFirst = Integer.parseInt(startRecord);
                if (sourcesPagingFirst != 0 && usePreviousCriteria) {
                    int index = searchCriteria.pagingNames.indexOf("sourcesResults");
                    searchCriteria.pagingValues.set(index, sourcesPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Sources", searchCriteria);
                    
                } else {
                    int index = searchCriteria.pagingNames.indexOf("sourcesResults");
                    sourcesPagingFirst = searchCriteria.pagingValues.get(index).intValue();
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
            u.InformSearchOperatorsAndValuesWithSpecialCharacters(ops, inputValue);
            //-------------------- paging info And criteria retrieval-------------------------- 
            long startTime = Utilities.startTimer();


            Vector<String> allResultsSources = dbGen.getAllSearchSources(SessionUserInfo, input, ops, inputValue, operator, Q, TA, sis_session);


            Collections.sort(allResultsSources, new StringLocaleComparator(targetLocale));

            //Get only those hierarchies that will appear in next page
            sourcesPagingQueryResultsCount = allResultsSources.size();
            if (sourcesPagingFirst > sourcesPagingQueryResultsCount) {
                sourcesPagingFirst = 1;
            }

            if (startRecord != null && startRecord.matches("SaveAll")) {

                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Sources_" + time;
                String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Sources.xsl").toString();

                if(answerType != null && answerType.compareTo("XML")==0){
                    pathToSaveScriptingAndLocale = null;
                }
                writeResultsInXMLFile(SessionUserInfo, allResultsSources, u, time, searchCriteria, output, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name, Q,TA, sis_session,pathToSaveScriptingAndLocale,targetLocale);

                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                
                if(answerType != null && answerType.compareTo("XML")==0){
                    if(Parameters.FormatXML){
                        WriteFileData.formatXMLFile(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml");
                    }
                    out.println(Save_Results_file_name.concat(".xml"));
                }
                else{
                    //create html and answer with html link for redirection --> download
                    u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath +  File.separator+ Save_Results_file_name + ".xml", 
                                       XSL, 
                                       webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));

                    //Send HTML relative url to output and return
                    out.println(webAppSaveResults_Folder + "/"+ webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                }
                
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in sources --> time elapsed: " + elapsedTimeSec);

                
                out.flush();
                return;
            }


            Vector<String> resultsSources = new Vector<String>();
            for (int i = 0; i < sourcesPagingListStep; i++) {
                if (i + sourcesPagingFirst > sourcesPagingQueryResultsCount) {
                    break;
                }
                String tmp = allResultsSources.get(i + sourcesPagingFirst - 1);
                resultsSources.addElement(tmp);
            }


            StringBuffer xmlResults = new StringBuffer();
            u.getResultsInXml_Source(SessionUserInfo, resultsSources, output, Q, TA, sis_session, targetLocale, xmlResults);

            float elapsedTimeSec = Utilities.stopTimer(startTime);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in hierarchies --> time elapsed: " + elapsedTimeSec);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            xmlResults.append("<results>");
            xmlResults.append(u.writePagingInfoXML(sourcesPagingListStep, sourcesPagingFirst, sourcesPagingQueryResultsCount, elapsedTimeSec, "SearchResults_Sources"));
            xmlResults.append("</results>");

           
            xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
            xml.append(u.getXMLMiddle(xmlResults.toString(), "SearchSourceResults"));
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

    public void writeResultsInXMLFile(UserInfoClass SessionUserInfo, Vector<String> allSources, Utilities u, String title, SearchCriteria sc, 
            String[] output, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name, QClass Q, TMSAPIClass TA, IntegerObject sis_session,String pathToSaveScriptingAndLocale, Locale targetLocale) {
    
        DBGeneral dbGen = new DBGeneral();
        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath +File.separator+ Save_Results_file_name + ".xml";
              
        OutputStreamWriter out = null;
        try {
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
            String temp ="";

            temp += ConstantParameters.xmlHeader+
                    "<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">" +
                    "<title>" + title + "</title>" +
                    "<query>" + sc.getQueryString(u) + "</query>";
            if(pathToSaveScriptingAndLocale!=null){
                temp+="<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>";

            }
           out.write(temp);
          
             
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        try {
        out.write("<data>");
        out.write("<output>");
        for (int m = 0; m < output.length; m++) {

            String category = output[m];
            if(category.compareTo("id")==0 ||category.compareTo("name")==0 ){
                continue;
            }
            else{
                out.write("<" + category +"/>");
            }
        }
        out.write("</output>");
        out.write("<sources>");

        int resultsLIMIT = allSources.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            //Q.reset_name_scope();
            Q.free_all_sets();
            StringBuffer temp= new StringBuffer();
            temp.append("<source index=\""+ (i+1) +"\">");
            
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentSource = allSources.get(i);
                    temp.append("<name>" + Utilities.escapeXML(currentSource) + "</name>");

                } else {
                    Vector<String> v = dbGen.returnResults_Source(SessionUserInfo, allSources.get(i), output[j],Q , TA,sis_session);
                    Collections.sort(v, new StringLocaleComparator(targetLocale));
                    
                    for (int k = 0; k < v.size(); k++) {
                        temp.append("<" + output[j] + ">");
                        temp.append(Utilities.escapeXML(v.get(k)));
                        temp.append("</" + output[j] + ">");
                    }
                    
                }
            }
            temp.append("</source>");
            out.write(temp.toString());
        }
        out.write("</sources>");
        out.write("</data>");
        } catch (Exception exc){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        
        try {
            out.write("</page>\n");
            out.flush();
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in closing file: " + exc.getMessage());
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
