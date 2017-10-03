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

import DB_Classes.DBConnect_Term;
import DB_Classes.DBCreate_Modify_Term;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConsistensyCheck;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

public class Rename_Term extends ApplicationBasicServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        try {


            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }

            DBGeneral dbGen = new DBGeneral();
            Utilities u = new Utilities();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            DBConnect_Term dbCon = new DBConnect_Term();
            
            StringObject errorMsgObj = new StringObject();
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            
            String pathToErrorsXML = getServletContext().getRealPath("/translations/Consistencies_Error_Codes.xml");            
            //String pathToMessagesXml = Utilities.getXml_For_Messages();
            
            
            //"Renamed succesfully!!" ;
            
            String RenameResult = u.translateFromMessagesXML("root/EditTerm/Rename/Success", null);
            
            String oldName = u.getDecodedParameterValue(request.getParameter("target"));            
            String saveAsUf = u.getDecodedParameterValue(request.getParameter("saveasuf")); 
            String newName = u.getDecodedParameterValue(request.getParameter("newname"));
            newName = newName.replaceAll(" +", " ").trim();

            
            int ret1 = TMSAPIClass.TMS_APISucc;
            int ret_modify_other_nodes = TMSAPIClass.TMS_APISucc;
            

            //open connection and start Transaction
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            
            
            String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            StringObject newtermobj = new StringObject(prefix.concat(newName));
            StringObject oldtermobj = new StringObject(prefix.concat(oldName));


            if ((oldtermobj.getValue().trim()).equals(prefix.toString().trim())) {
                //OLD NAME NULL?
                //No term selected for rename. Operation cancelled.
                ret1 = TMSAPIClass.TMS_APIFail;
                errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/NoTermSelected", null));
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                out.println(errorMsgObj.getValue());
                out.flush();
                return;
            } else if (!dbGen.check_exist(oldtermobj.getValue(), Q, sis_session)) {
                //OLD NAME EXISTS?
                errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/OldNameDoesNotExist", null));
                //errorMsgObj.setValue("Term selected for rename does not exist anymore. Please search again for this term and try again. Operation cancelled.");
                ret1 = TMSAPIClass.TMS_APIFail;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                out.println(errorMsgObj.getValue());
                out.flush();
                return;
            } else if ((newtermobj.getValue().trim()).equals(prefix.toString().trim())) {

                //NEW NAME ONY PREFIX?
                //errorMsgObj.setValue("A new term name was not provided. Operation cancelled.");
                errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/EmptyNewName", null));
                ret1 = TMSAPIClass.TMS_APIFail;
                
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                out.println(errorMsgObj.getValue());
                out.flush();
                return;
            } else if (dbGen.check_exist(newtermobj.getValue(), Q, sis_session)) {
                //NEW NAME EXISTS?
                //errorMsgObj.setValue("New term name already exists in the database. Operation cancelled.");
                errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/NewNameExists", null));                
                ret1 = TMSAPIClass.TMS_APIFail;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                out.println(errorMsgObj.getValue());
                out.flush();
                return;
            } else {

                //in case the option to save old name as uf is enabled
                ArrayList<String> existingUFs = new ArrayList<String>();

                if(Parameters.AtRenameSaveOldNameAsUf && saveAsUf!=null && saveAsUf.equalsIgnoreCase("yes")){

                    
                    existingUFs.addAll(dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.uf_kwd, Q,TA, sis_session));
                    
                    
                }
                
                //==========Start of Rename_Term New Descriptor===================================================================
                ArrayList<String> modifiedNodes = new ArrayList<String>();

                ArrayList<String> bts_vec = new ArrayList<String>();
                ArrayList<String> nts_vec = new ArrayList<String>();
                ArrayList<String> rts_vec = new ArrayList<String>();
                bts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.bt_kwd, Q, TA, sis_session);
                nts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.nt_kwd, Q, TA, sis_session);
                rts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.rt_kwd, Q, TA, sis_session);

                bts_vec.trimToSize();
                nts_vec.trimToSize();
                rts_vec.trimToSize();

                for (String btTerm : bts_vec) {
                    if(modifiedNodes.contains(btTerm)==false){
                        modifiedNodes.add(btTerm);
                    }
                }
                for (String ntTerm : nts_vec) {
                    if(modifiedNodes.contains(ntTerm)==false){
                        modifiedNodes.add(ntTerm);
                    }
                }
                for (String rtTerm : rts_vec) {
                    if(modifiedNodes.contains(rtTerm)==false){
                        modifiedNodes.add(rtTerm);
                    }
                }

                
                //StringObject newtermCmv = new StringObject(prefix.concat(newName));
                //StringObject oldtermCmv = new StringObject(prefix.concat(oldName));
                CMValue newtermCmv = new CMValue();
                newtermCmv.assign_node(newtermobj.getValue(), -1, Utilities.getTransliterationString(newName,false), -1);
                
                Q.reset_name_scope();
                CMValue oldtermCmv = new CMValue();                
                long ret = Q.set_current_node_and_retrieve_Cmv(new StringObject(prefix.concat(oldName)), oldtermCmv);
                if(ret<=0){
                    errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/OldNameDoesNotExist", null));
                    //errorMsgObj.setValue("Term selected for rename does not exist anymore. Please search again for this term and try again. Operation cancelled.");
                    ret1 = TMSAPIClass.TMS_APIFail;

                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                    out.println(errorMsgObj.getValue());
                    out.flush();
                }
                
            
                ret1 = TA.CHECK_RenameNewDescriptorCMValue(oldtermCmv, newtermCmv);

                if (ret1 != TMSAPIClass.TMS_APIFail) {

                    //rename scope note historical note and comment if exist
                    ret1 = dbGen.renameCommentNodes(SessionUserInfo.selectedThesaurus, oldtermobj, newtermobj, Q, sis_session);
                }
                

                if (ret1 == TMSAPIClass.TMS_APISucc) {

                    if(Parameters.AtRenameSaveOldNameAsUf && saveAsUf!=null && saveAsUf.equalsIgnoreCase("yes")){
                        existingUFs.add(oldName);
                        StringObject errorMsg = new StringObject("");
                        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
                        creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, newName, ConstantParameters.uf_kwd, existingUFs, SessionUserInfo.name, errorMsg, Q, sis_session,TA,tms_session,dbGen, pathToErrorsXML,false,false,null,ConsistensyCheck.EDIT_TERM_POLICY);
                        
                        if(errorMsg.getValue().trim().length()>0){
                            errorMsgObj.setValue(errorMsg.getValue());
                            ret1 = TMSAPIClass.TMS_APIFail;

                            //abort transaction and close connection
                            Q.free_all_sets();
                            Q.TEST_abort_transaction();
                            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                            out.println(errorMsgObj.getValue());
                            out.flush();
                            return;
                        }
                    }
                    Q.free_all_sets();
                    Q.TEST_end_transaction();
                    Q.TEST_begin_transaction();
                    dbCon.CreateModify_Finalization(SessionUserInfo.selectedThesaurus, newtermobj, SessionUserInfo.name, modifiedNodes, dbCon.DB_MODIFY, Q, sis_session, TA, tms_session, dbGen, errorMsgObj);

                    if (errorMsgObj.getValue().compareTo("") != 0) {
                        ret_modify_other_nodes = TMSAPIClass.TMS_APIFail;
                        //errorMsgObj.setValue(modifiications.errorMsg);
                    }
                }


                //==========End of Rename_Term New Descriptor===================================================================
            }

            if (ret1 == TMSAPIClass.TMS_APISucc && ret_modify_other_nodes == TMSAPIClass.TMS_APISucc) {

                out.print("Success");
                out.print("<newName>" + newName + "</newName>");
                out.println(RenameResult);
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (ret1 == TMSAPIClass.TMS_APISucc && ret_modify_other_nodes == TMSAPIClass.TMS_APIFail) {

                out.println(errorMsgObj.toString());
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (ret1 != TMSAPIClass.TMS_APISucc) {

                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsgObj);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage: " + errorMsgObj.getValue());
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage ret1 : " + ret1);
                // translate the case "EL`xxx does not exist in data base"
                String errorMessageStr = errorMsgObj.toString();
                if (errorMessageStr.indexOf("does not exist in data base") != -1) {
                    // remove prefix if any
                    if (errorMessageStr.indexOf("`") != -1) {
                        errorMessageStr = errorMessageStr.substring(errorMessageStr.indexOf("`") + 1);
                    }
                    
                    errorMsgObj.setValue(u.translateFromMessagesXML("root/EditTerm/Edit/TermDoesNotExist", new String[]{errorMessageStr}));                    
                    
                }
                out.println(errorMsgObj.toString());
                
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }


            out.flush();
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }



    }

    /* Handles the HTTP <code>GET</code> method.
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
}
/*StringObject str1 = new StringObject();
str1.setValue(term);
StringObject str2 = new StringObject();
str2.setValue(term);
StringObject str3 = new StringObject();
str3.setValue("Editor");				
StringObject str4 = new StringObject();
str4.setValue(term.toString());				
StringObject str5 = new StringObject();
str5.setValue(term.toString());
Q.reset_name_scope();
int ret333 = Q.set_current_node(tms_session.getValue(), str1);
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_current_node:" + term + " ret = " + ret333);
Q.reset_name_scope();
ret333 = Q.set_current_node(tms_session.getValue(), str2);
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_current_node:" + term + " ret = " + ret333);
Q.reset_name_scope();
ret333 = Q.set_current_node(tms_session.getValue(), str3);
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_current_node:" + str3 + " ret = " + ret333);			 
Q.reset_name_scope();
ret333 = Q.set_current_node(tms_session.getValue(), str4);
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_current_node:" + str4 + " ret = " + ret333);
Q.reset_name_scope();
ret333 = Q.set_current_node(tms_session.getValue(), str5);
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_current_node:" + str5 + " ret = " + ret333);			 

boolean b1 = dbG.check_exist(sis_session,term.toString());
Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"check_exist:" + term.toString() + " ret = " + b1);
 */
