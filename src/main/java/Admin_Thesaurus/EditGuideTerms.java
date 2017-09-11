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
package Admin_Thesaurus;




import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import java.util.Vector;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class EditGuideTerms extends ApplicationBasicServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }
        
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();

        try {

            //check if user is valid
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }

            //Connection structures
            QClass Q = new QClass(); 
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            //tools
            Utilities u = new Utilities();
            
            DBEditGuideTerms dbEdit_Guide_Terms = new DBEditGuideTerms();
            

            //data
            String mode = u.getDecodedParameterValue(request.getParameter("mode"));
            String newGuideTerm = u.getDecodedParameterValue(request.getParameter("newGuideTerm"));
            String deleteGuideTerm = u.getDecodedParameterValue(request.getParameter("deleteGuideTerm"));
            String renameGuideTermFrom = u.getDecodedParameterValue(request.getParameter("renameGuideTermFrom"));
            String renameGuideTermTo = u.getDecodedParameterValue(request.getParameter("renameGuideTermTo"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            


            DBGeneral dbGen = new DBGeneral();

            //result
            boolean operationSucceded = false;
            StringObject errorMsg = new StringObject("");
            Vector<String> GuideTermsDecodedValues = new Vector<String>();

            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            if (mode.compareTo("new") == 0) {
                operationSucceded = dbEdit_Guide_Terms.addGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session,newGuideTerm,errorMsg);
            }
            
            if (mode.compareTo("delete") == 0) {
                operationSucceded = dbEdit_Guide_Terms.deleteGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session,deleteGuideTerm,errorMsg);
            }
            
            if (mode.compareTo("rename") == 0) {
                operationSucceded = dbEdit_Guide_Terms.renameGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session, renameGuideTermFrom, renameGuideTermTo, errorMsg);
            }
            
            //check result of transaction. 
            //Any message returned should be prepended with 'Success' or 'Failure' 
            //in order to be handled by the client via ajax.
            if(operationSucceded || errorMsg.getValue() == null || errorMsg.getValue().length()==0){
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, null, sis_session, tms_session, true);
                out.println("Success");
            }else{        

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, null, sis_session, tms_session, true);

                out.println("Failure" + errorMsg.getValue());                
            }
            

        } 
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }
        finally {
            out.close();
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
