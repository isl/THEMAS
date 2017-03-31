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
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.*;

/**
 *
 * @author tzortzak
 * 
 * Commits the Hierarchy rename.
 */
public class Rename_Hierarchy extends ApplicationBasicServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
            if (SessionUserInfo == null) {
                out.println("Session Invalidate");
                return;
            }

            DBGeneral dbGen = new DBGeneral();
            
            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            String pathToMessagesXML = getServletContext().getRealPath("/translations/Messages.xml");
            Utilities u = new Utilities();

            DBConnect_Term dbCon = new DBConnect_Term();

            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            String oldName = u.getDecodedParameterValue(request.getParameter("oldhierarchyname"));
            String newName = u.getDecodedParameterValue(request.getParameter("newhierarchyname"));

            int ret1 = TMSAPIClass.TMS_APISucc; //Will store the result of rename
            int retAllowContinue = TMSAPIClass.TMS_APISucc;
            String RenameResult = "Η μετονομασία ολοκληρώθηκε με επιτυχία.";
            StringObject errorMsgObj = new StringObject("");


            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            /*
            try {
                Vector<String> errorArgs = new Vector<String>();
                //String pathToMessagesXML = Utilities.getMessagesXml();
                byte[] byteArray = newName.getBytes("UTF-8");
                int maxTermChars = dbtr.getMaxBytesForHierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {
                    //errorMsgObj.setValue("Δεν επιλέχθηκε όρος για μετονομασία. Ακύρωση μετονομασίας.");
                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(errorMsgObj, "root/EditHierarchy/Edit/LongName", errorArgs, pathToMessagesXML);
                        //abort transaction and close connection
                        Q.free_all_sets();
                        Q.abort_transaction();
                        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                        out.println(errorMsgObj.getValue());
                        out.flush();
                        return;

                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */

            Q.reset_name_scope();

            String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            StringObject OldHierarchy = new StringObject(prefix.concat(oldName));
            String hierarchy = prefix.concat(newName);
            //CODE IMPORTED FROM RenameCheck_Hierarchy

            if (newName != null && newName.length() > 0) {
                /*
                try {
                    byte[] byteArray = newName.getBytes("UTF-8");

                    int maxHierarchyChars = dbtr.getMaxBytesForHierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxHierarchyChars) {
                        Vector<String> errorArgs = new Vector<String>();
                        errorArgs.add("" + maxHierarchyChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(errorMsgObj, "root/EditHierarchy/Rename/LongName", errorArgs, pathToMessagesXML);                        
                        ret1 = TMSAPIClass.TMS_APIFail;
                        
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                */
            }

            if(ret1==TMSAPIClass.TMS_APIFail){

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }
            else
            if ((OldHierarchy.toString().trim()).equals(prefix.toString().trim())) {
                //OLD NAME NULL?
                errorMsgObj.setValue("Δεν επιλέχθηκε ιεραρχία για μετονομασία. Ακύρωση μετονομασίας.");
                ret1 = TMSAPIClass.TMS_APIFail;
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (!dbGen.check_exist(OldHierarchy.toString(), Q, sis_session)) {
                //OLD NAME EXISTS?
                errorMsgObj.setValue("Η ιεραρχία προς μετονομασία δεν είναι έγκυρη. Ανανεώστε τα περιεχόμενα της σελίδας αποτελεσμάτων και προσπαθήστε ξανά. Ακύρωση μετονομασίας.");
                
                ret1 = TMSAPIClass.TMS_APIFail;
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if ((hierarchy.toString().trim()).equals(prefix.toString().trim())) {
                //NEW NAME ONY PREFIX?
                errorMsgObj.setValue("Δεν δόθηκε νέο όνομα για την ιεραρχία προς μετονομασία. Ακύρωση μετονομασίας.");
                ret1 = TMSAPIClass.TMS_APIFail;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (dbGen.check_exist(hierarchy, Q, sis_session)) {
                //NEW NAME EXISTS?
                errorMsgObj.setValue("Το νέο όνομα υπάρχει ήδη στη βάση δεδομένων. Ακύρωση μετονομασίας.");
                ret1 = TMSAPIClass.TMS_APIFail;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else {




                StringObject newHierarchy = new StringObject(prefix.concat(newName));
                StringObject oldHierarchy = new StringObject(prefix.concat(oldName));

                String prefix_term = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());


                //Commit rename
                //check if Unclassified is declared --> No rename
                //Find out which nodes must also update their modified fields due to top term rename
                if (oldName.compareTo(Parameters.UnclassifiedTermsLogicalname) == 0) {
                    retAllowContinue = TMSAPIClass.TMS_APIFail;
                    errorMsgObj.setValue("Η συγκεκριμένη ιεραρχία δεν μπορεί να μετονομαστεί. Σε αυτήν κατατάσσονται όλοι οι νέοι όροι που δημιουργούνται.");
                } else {

                    StringObject newTopTermName = new StringObject(prefix_term.concat(newName));
                    Q.reset_name_scope();
                    if (Q.set_current_node(newTopTermName) != QClass.APIFail) {
                        retAllowContinue = TMSAPIClass.TMS_APIFail;
                        errorMsgObj.setValue("Το νέο όνομα που δώθηκε δεν μπορεί να χρησιμοποιηθεί για τον Όρο κορυφής της ιεραρχίας.");
                    }
                }

                if (retAllowContinue == TMSAPIClass.TMS_APISucc) {

                    Vector<String> modifiedNodes = new Vector<String>();

                    Vector<String> bts_vec = new Vector<String>();
                    Vector<String> nts_vec = new Vector<String>();
                    Vector<String> rts_vec = new Vector<String>();
                    bts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.bt_kwd, Q,TA, sis_session);
                    nts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.nt_kwd, Q,TA, sis_session);
                    rts_vec = dbGen.returnResults(SessionUserInfo, oldName, ConstantParameters.rt_kwd, Q,TA, sis_session);

                    bts_vec.trimToSize();
                    nts_vec.trimToSize();
                    rts_vec.trimToSize();

                    for (int m = 0; m < bts_vec.size(); m++) {
                        if(modifiedNodes.contains(bts_vec.get(m))==false){
                            modifiedNodes.add(bts_vec.get(m));
                        }
                        //modifiedNodes += bts_vec.get(m) + ",";
                    }
                    for (int m = 0; m < nts_vec.size(); m++) {
                        if(modifiedNodes.contains(nts_vec.get(m))==false){
                            modifiedNodes.add(nts_vec.get(m));
                        }
                        //modifiedNodes += nts_vec.get(m) + ",";
                    }
                    for (int m = 0; m < rts_vec.size(); m++) {
                        if(modifiedNodes.contains(rts_vec.get(m))==false){
                            modifiedNodes.add(rts_vec.get(m));
                        }
                        //modifiedNodes += rts_vec.get(m) + ",";
                    }

                    //if (modifiedNodes.length() > 0) {
                      //  modifiedNodes = modifiedNodes.substring(0, modifiedNodes.lastIndexOf(','));
                    //}
                    ret1 = TA.CHECK_RenameHierarchy(oldHierarchy, newHierarchy);

                    if (ret1 != TMSAPIClass.TMS_APIFail) {
                        ret1 = dbGen.renameCommentNodes(SessionUserInfo.selectedThesaurus, new StringObject(prefix_term.concat(oldName)), new StringObject(prefix_term.concat(newName)), Q, sis_session);
                    }

                    if (ret1 != TMSAPIClass.TMS_APIFail) {

                        Q.free_all_sets();
                        Q.TEST_end_transaction();
                        Q.TEST_begin_transaction();
                        dbCon.CreateModify_Finalization(SessionUserInfo.selectedThesaurus, new StringObject(prefix_term.concat(newName)), SessionUserInfo.name, modifiedNodes, dbCon.DB_MODIFY, Q, sis_session, TA, tms_session, dbGen, errorMsgObj);

                        if (errorMsgObj.getValue().compareTo("") != 0) {
                            retAllowContinue = TMSAPIClass.TMS_APIFail;
                            //errorMsgObj.setValue(modifiications.errorMsg);
                        }
                    }
                }
            }

            if (ret1 == TMSAPIClass.TMS_APISucc && retAllowContinue == TMSAPIClass.TMS_APISucc) {

                out.print("Success");
                out.print("<newName>" + newName + "</newName>");
                out.println(RenameResult);
                
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (ret1 == TMSAPIClass.TMS_APISucc && retAllowContinue == TMSAPIClass.TMS_APIFail) {

                out.println(errorMsgObj.toString());

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else if (ret1 != TMSAPIClass.TMS_APISucc) {
                // in case errorMsgObj has not been set by the servlet's checkings
                if (errorMsgObj.getValue().length() == 0) {
                    // set it by TMS_API mechanism
                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsgObj);
                }
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage: " + errorMsgObj.toString());
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage ret1 : " + ret1);
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

    //prepare XML rename Hierarchy success/error element
    public String setRenameSuccessResultInXml(String RenameResult) {

        StringBuffer sb = new StringBuffer();
        sb.append("<currentRename>");
        sb.append("<hierarchyError>");
        sb.append("<apotelesma>");
        sb.append(RenameResult);
        sb.append("</apotelesma>");
        sb.append("</hierarchyError>");
        sb.append("</currentRename>");

        return sb.toString();
    }
}
