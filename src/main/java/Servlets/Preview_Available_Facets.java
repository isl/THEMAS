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
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Utils.StringLocaleComparator;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import neo4j_sisapi.*;

/**
 *
 * @author tzortzak
 * Lists available Facets For New Hierarchy Creation
 */
public class Preview_Available_Facets extends ApplicationBasicServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        try {

            DBGeneral dbGen = new DBGeneral();
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

            // check for correct user login
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                response.sendRedirect("Index");
                
                return;
            }


            QClass Q = new neo4j_sisapi.QClass();
            IntegerObject sis_session = new IntegerObject();


            Utilities u = new Utilities();
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            

            ArrayList<String> availableFacets = dbGen.getAvailableFacets(SessionUserInfo.selectedThesaurus, Q,sis_session,targetLocale);

           // String xmlResults = getResultsInXml_ForTableLayout(currentHierName_utf8, currentFacetName_utf8, availableFacets);
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
  
            
            Collections.sort(availableFacets, new StringLocaleComparator(targetLocale));
            
            
            
            // get the previously selected tab of the DOWN part
            String currentTABup = (String) (sessionInstance.getAttribute("currentTABup"));
            if (currentTABup == null) {
                currentTABup = "SearchResults";
            }
                  
            
            String outPutAnswer = u.translateFromMessagesXML("root/EditHierarchy/Edit/AvailableFacets", null)+
                    " <select id=\"new_Hier_FacetID\" name=\"new_Hier_Facet\" size=\"5\" style=\"min-width:100px;\" onchange=\"hierarchyFacetName.value=new_Hier_Facet.options[new_Hier_Facet.selectedIndex].value;\"  >";
            
            for(int i=0;i<availableFacets.size();i++){
                outPutAnswer += "<option value=\"" + escapeHTML(availableFacets.get(i).toString().trim()) + "\">";
                outPutAnswer += escapeHTML(availableFacets.get(i).toString().trim());
                outPutAnswer += "</option>";
            }

           
           outPutAnswer+="</select>";
           out.write(outPutAnswer);
           out.flush(); 
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }
    private String escapeHTML(String target){
        if(target==null)
            return "";
        String returnVal = target;
        returnVal = returnVal.replaceAll("\\\\", "\\\\");
        returnVal = returnVal.replaceAll("&", "&amp;");
        returnVal = returnVal.replaceAll("<", "&lt;");
        returnVal = returnVal.replaceAll(">", "&gt;");

        returnVal = returnVal.replaceAll("\"", "&quot;");
        
        
        return returnVal;
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

    /*
     * Prepare XML so that available Facets (new and released) are approprietly Listed
     */
    public String getResultsInXml(String currentHierName, String currentFacetName, Vector availableFacets) {
        
        String XMLresults = "";
        XMLresults += "<currentcreatehierarchy>";
        XMLresults += "<hierarchy>";

        if(currentHierName.trim().length() != 0){
            
            XMLresults += "<name>";
            XMLresults += currentHierName.trim();
            XMLresults += "</name>";
        }
        
        if(currentFacetName.trim().length() != 0){
            
            XMLresults += "<facetName>";
            XMLresults += currentFacetName.trim();
            XMLresults += "</facetName>";
        }
        
        XMLresults += "</hierarchy>";


        for (int i = 0; i < availableFacets.size(); i++) {
            XMLresults += "<newHierFacetOption>";

            String currentHierarchy = (String) (availableFacets.get(i));
            XMLresults += "<name>" + currentHierarchy + "</name>";

            XMLresults += "</newHierFacetOption>";
        }
        XMLresults += "</currentcreatehierarchy>";

        return XMLresults;
    }
    
}
