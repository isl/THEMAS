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

import DB_Classes.DBCreate_Modify_Facet;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Users.UserInfoClass;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;
/**
 *
 * @author tzortzak
 * 
 * class used either by new Facet Functionality either by edit/delete/abandon/undo abandon facet Functionalities
 */
public class Create_Modify_Facet extends ApplicationBasicServlet {

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
           

            // check for previous logon
            
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (sessionInstance.getAttribute("SessionUser") == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                
                response.sendRedirect("Index");
                return;
            }

            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBCreate_Modify_Facet creation = new DBCreate_Modify_Facet();
            
            //String targetFacetTmp = params.get("facet").toString().trim();
            String targetFacet      = u.getDecodedParameterValue(request.getParameter("facet"));
            String createORmodify   = u.getDecodedParameterValue(request.getParameter("createORmodify_Facet"));
            String deletionOperator = u.getDecodedParameterValue(request.getParameter("delete"));
            String pathToMessagesXML = getServletContext().getRealPath("/translations/Messages.xml");

            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();            
            IntegerObject tms_session = new IntegerObject();

            StringObject resultObj = new StringObject("");
            //Parameters.initParams(this.getServletContext());
            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            boolean succeeded =  creation.Create_Or_ModifyFacet(SessionUserInfo, Q, TA,  sis_session, tms_session,   dbGen,  targetFacet, "", createORmodify,  deletionOperator, resultObj,true);

            Q.free_all_sets();
            if(succeeded){
                Q.TEST_end_transaction();
            }
            else{
                Q.TEST_abort_transaction();
            }
            // send the result/error message to XML
            
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
        return "Create_Modify_Facet Servlet used either by new Facet Functionality either by edit/delete/abandon/undo abandon facet Functionalities";
    }

    // </editor-fold>
    

}
