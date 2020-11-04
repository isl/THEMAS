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

import DB_Classes.DBCreate_Modify_Term;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.ConsistensyCheck;
import Utils.ConstantParameters;
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
import neo4j_sisapi.TMSAPIClass;
import java.util.ArrayList;
/**
 *
 * @author tzortzak
 */
public class EditActions_Term extends ApplicationBasicServlet {
   
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
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        try {
            
            QClass Q = new QClass(); 
            TMSAPIClass TA = new TMSAPIClass();
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
            DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
            
                        
            //data
            String targetTerm = u.getDecodedParameterValue(request.getParameter("targetTerm"));           
            String targetField = u.getDecodedParameterValue(request.getParameter("targetEditField"));
            String translationSeperator = u.getDecodedParameterValue(request.getParameter("translationSeparator")); // case of translation
            String NewtargetField = targetField;
            String pathToErrorsXML = context.getRealPath("/translations/Consistencies_Error_Codes.xml");
            
            
            //result
            StringObject errorMsg = new StringObject("");
            ArrayList<String> ntsDecodedValues = new ArrayList<>();
            ArrayList<String> GuideTermsDecodedValues = new ArrayList<>();
                
            if(targetField.compareTo(ConstantParameters.guide_term_kwd)==0){
                
                String[] nts        = request.getParameterValues("NtName");
                String[] guideTerms = request.getParameterValues("GuideTerm");
                
                if(nts!=null){
                    for (String nt : nts) {
                        String temp = u.getDecodedParameterValue(nt);
                        if(temp!=null && temp.trim().length()>0 && ntsDecodedValues.contains(temp)==false){ 
                            ntsDecodedValues.add(temp);
                        }
                    }
                }
                
                if(guideTerms!=null){
                    for (String guideTerm : guideTerms) {
                        String temp = u.getDecodedParameterValue(guideTerm);
                        if(temp!=null && temp.trim().length()>0 ){ 
                            GuideTermsDecodedValues.add(temp);
                        }
                        else{
                            GuideTermsDecodedValues.add("");
                        }
                    }
                }                
            }
            
            //actions for create. bt values must be read apart from targetField
            if(targetField.compareTo(ConstantParameters.term_create_kwd)==0){
                NewtargetField = ConstantParameters.bt_kwd;
            }
            String[] values = request.getParameterValues(NewtargetField);
            
            //values are always read in decodedValues vector from values[] --> if term_create then values[] is filled with bts  
            ArrayList<String> decodedValues = new ArrayList<>();
            if(values!=null){
                for (String value : values) {
                    String temp = u.getDecodedParameterValue(value);
                    temp = temp.replaceAll(" +", " ");
                    temp = temp.replaceAll("\r", "\n");
                    temp = temp.replaceAll(" \n", "\n");
                    temp = temp.replaceAll("\n ", "\n");
                    while(temp.contains("\n\n")){
                        temp = temp.replaceAll("\n\n", "\n");
                    }
                    if(temp!=null && temp.trim().length()>0 && decodedValues.contains(temp)==false){ 
                        decodedValues.add(temp);
                    }
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+values[i]);
                }
            }
            
            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            //Try to perform Transaction
            if(targetField.compareTo(ConstantParameters.term_create_kwd)==0){
                String newName = u.getDecodedParameterValue(request.getParameter("newName_Term"));
                
                creation_modificationOfTerm.createNewTerm(SessionUserInfo,newName,decodedValues,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToErrorsXML,false,false,null,ConsistensyCheck.EDIT_TERM_POLICY);
            }
            else if(targetField.compareTo(ConstantParameters.guide_term_kwd)==0){
                
                creation_modificationOfTerm.performGuideTermEditing(SessionUserInfo.selectedThesaurus,Q,sis_session,errorMsg,targetTerm,ntsDecodedValues,GuideTermsDecodedValues, SessionUserInfo.UILang);
                
            }
            else{
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, targetTerm,targetField,decodedValues,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToErrorsXML,false,false,null,ConsistensyCheck.EDIT_TERM_POLICY);
            }
            
            //check result of transaction. Prepend with Success or Failure any message returned and wirte it to PrintWriter out for ajax handling
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
