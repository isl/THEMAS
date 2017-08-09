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

import DB_Classes.DBCreate_Modify_Hierarchy;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.Locale;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
public class EditActions_Hierarchy extends ApplicationBasicServlet {
   
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
            DBCreate_Modify_Hierarchy creation_modificationOfHierarchy = new DBCreate_Modify_Hierarchy();
            
            
            //data
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String pathToMessagesXML = getServletContext().getRealPath("/translations/Messages.xml");
            String targetHierarchy  = u.getDecodedParameterValue(request.getParameter("targetHierarchy"));           
            String targetField = u .getDecodedParameterValue(request.getParameter("targetEditField"));
            String NewtargetField = targetField;
            
            //result tracking 
            StringObject errorMsg = new StringObject("");
            
            
            //actions for create. bt values must be read apart from targetField
            if(targetField.compareTo("hierarchy_create")==0){
                NewtargetField = "facets";
            }
            
            String[] values = request.getParameterValues(NewtargetField);
            //values are always read in decodedValues vector from values[] --> if hierarchy_create then values[] are filled with facets  
            Vector<String> decodedValues = new Vector<String>();
            if(values!=null){
                for(int i=0; i< values.length ;i++){
                    String temp =u.getDecodedParameterValue(values[i]);

                    if(temp!=null && temp.trim().length()>0 && decodedValues.contains(temp)==false){ 
                        decodedValues.add(temp);
                    }
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+values[i]);
                }
            }
            
            if(targetField.compareTo("hierarchy_create")==0){
                targetHierarchy = u.getDecodedParameterValue(request.getParameter("newName_Hierarchy"));           
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"targetHierarchy = " + targetHierarchy + "  targetField= "+targetField + " values = " + decodedValues.toString());
            }
            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            if(targetField.compareTo("hierarchy_create")==0){
                //Perform Checks
                boolean succeeded = creation_modificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA,  sis_session,  tms_session,
                          dbGen,  targetHierarchy,  decodedValues,"create", null,SessionUserInfo.name, targetLocale,  errorMsg,true,pathToMessagesXML);

                if(succeeded && decodedValues.size()>1){
                   
                    succeeded = creation_modificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA,  sis_session,  tms_session,
                          dbGen,  targetHierarchy,  decodedValues,"modify", null,SessionUserInfo.name, targetLocale,  errorMsg,true,pathToMessagesXML);
                }
            }
            else if(targetField.compareTo("hierarchy_facets")==0){
                
                 creation_modificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA,  sis_session,  tms_session,
                           dbGen,  targetHierarchy,  decodedValues,"modify", null,SessionUserInfo.name, targetLocale,  errorMsg,true,pathToMessagesXML);
            }
            else if(targetField.compareTo(DBCreate_Modify_Hierarchy.hierarchy_delete_kwd)==0){
                //creation_modificationOfHierarchy.Create_Or_ModifyHierarchy(session, Q, TA,  sis_session,  tms_session,  dbGen,  targetHierarchy,  decodedValues,"modify", "delete",SessionUserInfo.name, targetLocale,  errorMsg,true);                
                // replaced by karam with new method: DeleteHierarchy()
                creation_modificationOfHierarchy.DeleteHierarchy(SessionUserInfo, Q, TA,  sis_session,  tms_session,  dbGen,  targetHierarchy,  errorMsg);
            }
            
            //check result of transaction. Prepend with Success or Failure any message returned and write it to PrintWriter out for ajax handling
            if(errorMsg.getValue() == null || errorMsg.getValue().length()==0){

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
            
        } catch (Exception e) {
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
