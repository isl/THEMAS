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
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.ConsistensyCheck;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.Locale;

/**
 *
 * @author tzortzak
 */
public class EditActions_Facet extends ApplicationBasicServlet {
   
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
            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }
            
            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBCreate_Modify_Facet creation_modificationOfFacet = new DBCreate_Modify_Facet();
            
            
            //data
            ServletContext context = getServletContext();
            String language = context.getInitParameter("LocaleLanguage");
            String country  = context.getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String targetFacet  = u.getDecodedParameterValue(request.getParameter("targetFacet"));           
            String targetField  = u.getDecodedParameterValue(request.getParameter("targetEditField"));
            String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");

            
            if(targetField.compareTo("facet_create")==0){
                targetFacet = u.getDecodedParameterValue(request.getParameter("newName_Facet"));           
            }
            
            //result tracking 
            StringObject errorMsg = new StringObject("");
            boolean succeded = false;
            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            
            
            if(targetField.compareTo("facet_create")==0){
                    
                succeded = creation_modificationOfFacet.Create_Or_ModifyFacet(SessionUserInfo.selectedThesaurus, Q, TA,  sis_session, tms_session,
                         dbGen,  targetFacet, "create",  null, errorMsg,true,pathToMessagesXML);
            }
            if(targetField.compareTo("delete_facet")==0){
                    
                ConsistensyCheck consistencies = new ConsistensyCheck();
                boolean continueDeletion = consistencies.check_facet_deletion(SessionUserInfo, Q, sis_session, dbGen, targetFacet,errorMsg, targetLocale,pathToMessagesXML);
                
                succeded = creation_modificationOfFacet.Create_Or_ModifyFacet(SessionUserInfo.selectedThesaurus, Q, TA,  sis_session, tms_session,
                         dbGen,  targetFacet, "modify",  "delete", errorMsg,true,pathToMessagesXML);
            }
            
            //check result of transaction. Prepend with Success or Failure any message returned and write it to PrintWriter out for ajax handling
            if(succeded){

                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                
                out.println("Success");
            }else{

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                out.println("Failure" + errorMsg.getValue());
                
            }
            
            
            
          
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
        return "Short description";
    }// </editor-fold>

}
