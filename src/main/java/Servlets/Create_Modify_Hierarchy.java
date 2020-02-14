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
package Servlets;

import DB_Classes.DBCreate_Modify_Hierarchy;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 * 
 * Servlet used either by new Hierarchy Functionality either by edit/delete/abandon/undo abandon Hierarchy Functionalities
 */
public class Create_Modify_Hierarchy extends ApplicationBasicServlet {
    
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
            
            // check for previous logon
            
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                
                response.sendRedirect("Index"); 
                return;
            }


            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            
            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBCreate_Modify_Hierarchy creation = new DBCreate_Modify_Hierarchy();
                     
            // get servlet parameters
            
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            String pathToMessagesXML = getServletContext().getRealPath("/translations/Messages.xml");
            Locale targetLocale = new Locale(language, country);
            
            String targetHierarchy  = u.getDecodedParameterValue(request.getParameter("hierarchyName"));
            String createORmodify   = u.getDecodedParameterValue(request.getParameter("createORmodify_Hierarchy"));
            String deletionOperator = u.getDecodedParameterValue(request.getParameter("delete"));
            String targetHierFacetName =  u.getDecodedParameterValue(request.getParameter("hierarchyFacetName"));
            String facetsStrArr[] = request.getParameterValues("edit_Sel_Hier_Facet");
            
            ArrayList<String> targetHierarchyFacets = new ArrayList<String>();
            StringObject resultObj = new StringObject("");
            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            if (createORmodify.equals("create")) {
                
                targetHierarchyFacets.add(targetHierFacetName);
                
            } else { //modify

                if (facetsStrArr!=null && facetsStrArr.length >= 1 && facetsStrArr[0].trim().length() != 0) {
                    
                    for (int i = 0; i < facetsStrArr.length; i++) {

                        targetHierarchyFacets.add(u.getDecodedParameterValue(facetsStrArr[i]));
                    }
                }
            }

            //Perform Checks and commit/Abort modification
            boolean succeeded = creation.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA,  sis_session,  tms_session,
                      dbGen,  targetHierarchy,  targetHierarchyFacets,createORmodify, deletionOperator,
                    SessionUserInfo.name, targetLocale, resultObj, true);

            Q.free_all_sets();
            if(succeeded){
                Q.TEST_end_transaction();
            }else{
                Q.TEST_abort_transaction();
            }
            // close SIS and TMS connection        
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

       
            out.println(resultObj.getValue());
            out.flush();
                   

        }catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
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
        return "Create_Modify_Hierarchy Servlet used either by new Hierarchy Functionality either by edit/delete/abandon/undo abandon Hierarchy Functionalities";
    }// </editor-fold>


    
}
