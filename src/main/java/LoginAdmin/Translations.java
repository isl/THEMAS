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
package LoginAdmin;

import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Utilities;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;
/**
 *
 * @author tzortzak
 */
public class Translations extends ApplicationBasicServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private String TranslationsActionResultXMLTag = "TranslationsActionResult";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String  basePath = request.getSession().getServletContext().getRealPath("");
        PrintWriter out = response.getWriter();
        /*if (Parameters.SYSTEM_IS_LOCKED) {
            out.println("SYSTEM LOCKED");
            out.close();
            request.getSession().invalidate();
            return;
        }
        */
        if (SystemIsLockedForAdministrativeJobs(request, response)) {
                return;
            }


        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);
        
        try {
            
            sessionInstance.readSession(session, request);
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName()) || SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator)==false) {
                out.println("Session Invalidate");
                return;
            }

            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            ConfigDBadmin config = new ConfigDBadmin(basePath);
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            //parameters
            String functionallity = u.getDecodedParameterValue(request.getParameter("functionallity"));
            String selectedThesaurus = u.getDecodedParameterValue(request.getParameter("selectedThesaurus"));
            ArrayList<String> userSelectedTranslationIdentifiers = u.getDecodedParameterValues(request.getParameterValues("LanguageIdentifier"));
            ArrayList<String> userSelectedTranslationWords = u.getDecodedParameterValues(request.getParameterValues("LanguageName"));

            String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
            HashMap<String, String> currentTranslationCategories = null;
            HashMap<String, String> userSelections = new HashMap<String, String>();
            StringObject resultMessageStrObj = new StringObject("");
            String XMLMiddleStr= "";

            //check user input - case of save
            for(int i=0; i<userSelectedTranslationWords.size(); i++ ){

                String word =userSelectedTranslationWords.get(i);
                if(word==null){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have empty Language Word values\r\n");
                    break;
                }
                word=word.trim();
                if(word.length()==0){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have empty Language Word values\r\n");
                    break;
                }

                String id ="";
                if(i<userSelectedTranslationIdentifiers.size()){
                    //if not an error message will be applied later on
                    id=userSelectedTranslationIdentifiers.get(i);                    
                }
                
                if(id==null){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have empty Language Identifier values\r\n");
                    break;
                }
                id=id.trim();
                if(id.length()==0){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have empty Language Identifier values\r\n");
                    break;
                }

                if(userSelections.containsKey(word)){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have dublicate Language Word values\r\n");
                    break;
                }
                if(userSelections.containsValue(id)){
                    resultMessageStrObj.setValue(resultMessageStrObj.getValue()+"Should not have dublicate Language Identifier values\r\n");
                    break;
                }
                userSelections.put(word, id);
            }
            

            //test if a query or a transaction is needed
            boolean queryInsteadOfTransaction = true;
            boolean commitTransaction = true;
            if(functionallity.equals("save")){
                queryInsteadOfTransaction = false;
            }


            if(resultMessageStrObj.getValue().length()>0){

                HashMap<String,String> originalUserSelection = new HashMap<String,String>();
                for(int i=0; i<userSelectedTranslationWords.size(); i++ ){
                    String word =userSelectedTranslationWords.get(i);
                    String id ="";
                    if(i<userSelectedTranslationIdentifiers.size()){
                        //if not an error message will be applied later on
                        id=userSelectedTranslationIdentifiers.get(i);
                    }
                    originalUserSelection.put(word, id);
                }

                XMLMiddleStr+= u.writeXMLTranslations(selectedThesaurus, originalUserSelection,  "<"+TranslationsActionResultXMLTag+">" + resultMessageStrObj.getValue() + "</"+TranslationsActionResultXMLTag+">");
            }
            else
            {            
                //open connection and query / transaction
                dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, selectedThesaurus, queryInsteadOfTransaction);

                //read current thesauri and current translation categories
                ArrayList<String> thesaurusVector = new ArrayList<String>();
                thesaurusVector = dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);
                if(thesaurusVector.contains(selectedThesaurus)){
                    currentTranslationCategories = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, selectedThesaurus, null, false, true);
                }
                else{
                    resultMessageStrObj.setValue("Thesaurus: "+ selectedThesaurus + " not found in the database.\r\n\r\nCheck thesaurus name and try again.");
                }


                if(functionallity != null && functionallity.equals("preview")){
                    // <editor-fold defaultstate="collapsed" desc="preview mode.">
                    XMLMiddleStr+= u.writeXMLTranslations(selectedThesaurus, currentTranslationCategories, "<"+TranslationsActionResultXMLTag+">" + resultMessageStrObj.getValue() + "</"+TranslationsActionResultXMLTag+">");

                    // </editor-fold>
                }
                else if (functionallity!=null && functionallity.equals("save")){
                    // <editor-fold defaultstate="collapsed" desc="save mode.">


                    dbGen.synchronizeTranslationCategories(currentTranslationCategories,
                            userSelections, userSelectedTranslationWords, userSelectedTranslationIdentifiers, selectedThesaurus,
                            resultMessageStrObj, pathToMessagesXML, Q, TA, sis_session,  tms_session);

                    if(resultMessageStrObj.getValue().length()>0)
                    {
                        HashMap<String,String> originalUserSelection = new HashMap<String,String>();
                        for(int i=0; i<userSelectedTranslationWords.size(); i++ ){
                            String word =userSelectedTranslationWords.get(i);
                            String id ="";
                            if(i<userSelectedTranslationIdentifiers.size()){
                                //if not an error message will be applied later on
                                id=userSelectedTranslationIdentifiers.get(i);
                            }
                            originalUserSelection.put(word, id);
                        }
                        XMLMiddleStr+= u.writeXMLTranslations(selectedThesaurus,userSelections,"<"+TranslationsActionResultXMLTag+">" + resultMessageStrObj.getValue() + "</"+TranslationsActionResultXMLTag+">");
                    }
                    else{
                        resultMessageStrObj.setValue("Changes were successfully submitted");
                        XMLMiddleStr+= u.writeXMLTranslations(selectedThesaurus,userSelections,"<"+TranslationsActionResultXMLTag+">" + resultMessageStrObj.getValue() + "</"+TranslationsActionResultXMLTag+">");
                    }
                    // </editor-fold>
                }

                //commit or abort transaction and close connection
                Q.free_all_sets();
                if(commitTransaction){
                    Q.TEST_end_transaction();
                }
                else{
                    Q.TEST_abort_transaction();
                }
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);                
            }
            
            HiddenActionsCommon HiddenActionsCommon = new HiddenActionsCommon();
            HiddenActionsCommon.writeXML(sessionInstance,context,"THEMAS_HiddenTranslations_DIV", out, common_utils,XMLMiddleStr);

        } finally {
            out.close();
        }
    } 
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
