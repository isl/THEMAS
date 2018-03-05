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

import Utils.Parameters;
import Utils.Utilities;
import Utils.TaxonomicCodeItem;
import Utils.TaxonomicCodeComparator;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class SearchResults_Terms_Systematic extends ApplicationBasicServlet {
   
    
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
        String startRecord = (String) request.getParameter("pageFirstResult");
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

            

            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            //Parameters
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            //Tools
            Utilities u = new Utilities();   
            DBGeneral dbGen = new DBGeneral();
            
            SearchCriteria searchCriteria;
            
            // -------------------- paging info And criteria retrieval-------------------------- 
            //initial values --> will change from the following code
            int systematicPagingListStep = new Integer(ListStepStr).intValue();
            int systematicPagingFirst = 1;
            int systematicPagingQueryResultsCount = 0;
             
            searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Terms");
            
            //tab pressed without any criteria set
            if(searchCriteria == null ){
                searchCriteria = SearchCriteria.createSearchCriteriaObject(SessionUserInfo, "SearchCriteria_Terms", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
                
            }
            
            if(startRecord == null){ //read paging criteria
                int index        = searchCriteria.pagingNames.indexOf("termsSystematic");
                systematicPagingFirst = searchCriteria.pagingValues.get(index);
            }
            
            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {
                
                systematicPagingFirst = Integer.parseInt(startRecord);
                if (systematicPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("termsSystematic");
                    searchCriteria.pagingValues.set(index, systematicPagingFirst);
                    
                    sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);
                    
                } else {
                    int index = searchCriteria.pagingNames.indexOf("termsSystematic");
                    systematicPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }            
            
            String[] input = new String[searchCriteria.input.size()];
            String[] ops = new String[searchCriteria.operator.size()];
            String[] inputValue = new String[searchCriteria.value.size()];
            String operator = searchCriteria.CombineOperator;
            
            //check if tc is included but if not do not add this 
            //to criteria just to systematic display's output
            int outputLength = searchCriteria.output.size();
            if(searchCriteria.output.indexOf("tc")==-1){
                outputLength++;
            }
            if(searchCriteria.output.indexOf("name")==-1){
                outputLength++;
            }
            String[] output = new String[outputLength];


            searchCriteria.input.toArray(input);
            searchCriteria.operator.toArray(ops);
            searchCriteria.value.toArray(inputValue);
            searchCriteria.output.toArray(output);
            
            if(searchCriteria.output.indexOf("tc")==-1){
                if(searchCriteria.output.size() == (outputLength-1)){
                    output[outputLength-1] = "tc";
                }
                else{
                    if(searchCriteria.output.size() == (outputLength-2)){
                        output[outputLength-2] = "tc";
                    }
                }
            }
            if(searchCriteria.output.indexOf("name")==-1){
                output[outputLength-1] = "name";
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
            
            ArrayList<TaxonomicCodeItem> descriptors = new ArrayList<TaxonomicCodeItem>();
            
            //Get All Results Set satisying Criteria
            int set_global_descriptor_results = dbGen.getSearchTermResultSet(SessionUserInfo, input, ops, inputValue, operator,Q,TA ,sis_session);
            
            //Get All dewey codes from set_global_descriptor_results set as TaxonomicCodeItems. Terms without
            //dewey or without a valid dewey xxx.yyy.zzz. .... will be considered as without having that dewey at all.
            dbGen.collectResultsTaxonomicCodes(SessionUserInfo.selectedThesaurus,Q, sis_session,set_global_descriptor_results, descriptors, ".");
            
            //Now these results must be sorted according to dewey number and  
            //then only the current page terms must be selected for display
            Collections.sort(descriptors, new TaxonomicCodeComparator(targetLocale));

            Q.free_set(set_global_descriptor_results);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                
            if(startRecord!=null && startRecord.matches("SaveAll")){
                
                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String time = Utilities.GetNow();
                String Save_Results_file_name = "SearchResults_Terms_Systematic_" + time;
                String webAppSaveResults_AbsolutePathStr = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathStr);
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Terms_Systematic.xsl").toString();
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                writeResultsInXMLFile(descriptors,u, time, searchCriteria, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name,pathToSaveScriptingAndLocale, SessionUserInfo.UILang);
            
            
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath +File.separator+ Save_Results_file_name + ".xml", 
                                   XSL, 
                                   webAppSaveResults_temporary_filesAbsolutePath +File.separator+Save_Results_file_name.concat(".html"));
                    
                float elapsedTimeSec = Utilities.stopTimer(startTime);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in terms Systematic View --> time elapsed: " + elapsedTimeSec);

                //Send HTML relative url to output and return
                out.println(webAppSaveResults_Folder + "/"+webAppSaveResults_temporary_files_Folder +"/"+ Save_Results_file_name.concat(".html"));
                out.flush();
                return;
            }
            
            //Get only those descriptors that will appear in next page
            systematicPagingQueryResultsCount = descriptors.size();
            ArrayList<TaxonomicCodeItem> pageDescriptors = new ArrayList<TaxonomicCodeItem>();
            for (int i = 0; i < systematicPagingListStep; i++) {
                if (i + systematicPagingFirst > systematicPagingQueryResultsCount) {
                    break;
                }
                TaxonomicCodeItem tmp = (TaxonomicCodeItem) descriptors.get(i + systematicPagingFirst - 1);
                pageDescriptors.add(tmp);
            }

            //Collect systematic display data for all nodes of next page
            String xmlResults = getResultsInXml(pageDescriptors, output);

            //timer end
            float elapsedTimeSec = Utilities.stopTimer(startTime);
            
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in terms Systematic View --> time elapsed: " + elapsedTimeSec);
            
            xmlResults+="<results>";
            xmlResults+=(u.writePagingInfoXML(systematicPagingListStep,
                                              systematicPagingFirst,
                                              systematicPagingQueryResultsCount, 
                                              elapsedTimeSec, 
                                              "SearchResults_Terms_Systematic"));
            xmlResults+="</results>";
              
            
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
            xml.append(u.getXMLMiddle(xmlResults, "Systematic"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");//sessionInstance.path: web path
            //u.XmlPrintWriterTransform(out, xml, path + "/xml-xsl/search_results_terms_systematic.xsl");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally { 
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    } 


    
    /*---------------------------------------------------------------------
    getResultsInXml_ForTableLayout()
    -----------------------------------------------------------------------
    this function is somewhat different from getResultsInXml_ForTableLayout() in Utilities.java since it must 
    display only the relevant dewey code --> Not all dewey codes declared for each 
    term. That's why it handles a ArrayList<TaxonomicCodeItem> parameter instead of a Vector
    filled with Strings which is used in Utilities.java file.
    ----------------------------------------------------------------------*/
    public String getResultsInXml(ArrayList<TaxonomicCodeItem> allTerms, String[] output) {
        //DBThesaurusReferences dbtr = new DBThesaurusReferences(sis_session);
        //String prefix_el = dbtr.getThesaurusPrefix_Descriptor();

        Utilities u = new Utilities();
        String XMLresults = "";
        XMLresults += "<results>";

        int resultsLIMIT = allTerms.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            XMLresults += "<term>";
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentTerm =  allTerms.get(i).nodeName;
                    XMLresults += "<name>" + Utilities.escapeXML(currentTerm) + "</name>";
                }else 
                if(output[j].equals("tc")) {
                    XMLresults += "<" + output[j] + ">";
                    XMLresults +=  allTerms.get(i).getTaxCode();
                    XMLresults += "</" + output[j] + ">";
                }
            }
            XMLresults += "</term>";
        }
        XMLresults += "</results>";

        return XMLresults;
    }
    
    
    public String writeResultsInXMLFile(ArrayList<TaxonomicCodeItem> allTerms, 
            Utilities u, 
            String title, 
            SearchCriteria sc,
            String webAppSaveResults_temporary_filesAbsolutePath,
            String Save_Results_file_name,
            String pathToSaveScriptingAndLocale,
            final String uiLang) {
        //DBThesaurusReferences dbtr = new DBThesaurusReferences(sis_session);
        //String prefix_el = dbtr.getThesaurusPrefix_Descriptor();

       
       String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath +"/"+ Save_Results_file_name + ".xml";
       
        
        OutputStreamWriter out = null;
        try {
            
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
            
            out.write(ConstantParameters.xmlHeader);
            //out.write(xslLink);
            out.write("<page title=\""+ title +"\" language=\""+uiLang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\" mode=\"insert\">");
                          
            out.write("<query>");
            out.write(sc.getQueryString(u ));
            out.write("</query>");
            
            out.write("<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>");
                       
            for(int i=0; i< allTerms.size(); i++){
                
                String dewey = "";
                
                for(int k=0 ; k< allTerms.get(i).codeParts.size(); k++ ){
                        
                    dewey += allTerms.get(i).codeParts.get(k);
                    if(k<allTerms.get(i).codeParts.size()-1 ){

                        dewey += ".";
                    }


                }
                
                out.write("<term position=\"");
                out.write(String.valueOf(i+1));
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
            
            /*
            
            Fw.write("<html>");
            Fw.write("<head>");
            Fw.write("<title>");
            Fw.write(this.windowName);
            Fw.write("</title>");
            Fw.write("</head>");
            Fw.write("<body>");
            Fw.write("<table border=\"1\">");
            Fw.write("<tr> <td width=\"40\"><b>A/A</b></td> <td width=\"100\"><b>Dewey</b></td> <td><b>Name</b></td> </tr>");
            for(int i=0; i< allTerms.size(); i++){
                
                String dewey = "&nbsp;&nbsp;";
                
                for(int k=0 ; k< allTerms.get(i).codeParts.size(); k++ ){
                        
                    dewey += allTerms.get(i).codeParts.get(k);
                    if(k<allTerms.get(i).codeParts.size()-1 ){

                        dewey += ".";
                    }


                }
                
                Fw.write("<tr><td align=\"center\">");
                Fw.write(String.valueOf(i+1));
                Fw.write("</td>");
                
                Fw.write("<td>");
                Fw.write(dewey);
                Fw.write("</td>");
                
                Fw.write("<td>");
                Fw.write(allTerms.get(i).nodeName);
                Fw.write("</td></tr>");
            }
            Fw.write("</table>");
            Fw.write("</body>");
            
            Fw.write("</html>");
          */

             
            out.close();

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        
        String XMLresults = "";
/*        XMLresults += "<results>";

        int resultsLIMIT = allTerms.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            XMLresults += "<term>";
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    String currentTerm =  allTerms.get(i).nodeName;
                    XMLresults += "<name>" + currentTerm + "</name>";
                }
                else if(output[j].equals("dn")) {
                    
                    String dewey = "";
                    for(int k=0 ; k< allTerms.get(i).codeParts.size(); k++ ){
                        
                        dewey += allTerms.get(i).codeParts.get(k);
                        if(k<allTerms.get(i).codeParts.size()-1 ){
                            
                            dewey += ".";
                        }
                            
                        
                    }
                    XMLresults += "<" + output[j] + ">";
                    XMLresults +=  dewey ;
                    XMLresults += "</" + output[j] + ">";
                }
                else ; /*ignore other cases Not needed{
                    Vector v = dbG.returnResults(allTerms.get(i).toString(), output[j]);
                    XMLresults += "<" + output[j] + ">";
                    for (int k = 0; k < v.size(); k++) {
                        XMLresults += v.get(k).toString();
                        if (v.size() > 1 && k < v.size() - 1) {
                            XMLresults += ",";
                        }
                    }
                    XMLresults += "</" + output[j] + ">";
                }*/
/*            }
            XMLresults += "</term>";
        }
        XMLresults += "</results>";
*/
        return XMLresults;
        
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
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
