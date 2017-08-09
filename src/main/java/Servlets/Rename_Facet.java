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
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
/**
 *
 * @author tzortzak
 * Commits the Facet rename.
 */
public class Rename_Facet extends ApplicationBasicServlet {

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
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }

            String pathToMessagesXML = getServletContext().getRealPath("/translations/Messages.xml");

            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

                        //StringBuffer xml = new StringBuffer();
            //String RenameResult = "Renamed succesfully!!";
            String RenameResult = "Η μετονομασία ολοκληρώθηκε με επιτυχία.";
            String Xmlresult = "";
            StringObject ob = new StringObject();
            Utilities u = new Utilities();

            String oldName = u.getDecodedParameterValue(request.getParameter("oldfacetname"));
            String newName = u.getDecodedParameterValue(request.getParameter("newfacetname"));

            int ret1 = 0;



            DBGeneral dbGen = new DBGeneral();
            //Parameters.initParams(this.getServletContext());

            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            /*
            try {
                Vector<String> errorArgs = new Vector<String>();
                //String pathToMessagesXML = Utilities.getMessagesXml();
                byte[] byteArray = newName.getBytes("UTF-8");
                int maxTermChars = dbtr.getMaxBytesForFacet(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {
                    //errorMsgObj.setValue("Δεν επιλέχθηκε όρος για μετονομασία. Ακύρωση μετονομασίας.");
                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(ob, "root/EditFacet/Edit/LongName", errorArgs, pathToMessagesXML);
                        //abort transaction and close connection
                        Q.free_all_sets();
                        Q.abort_transaction();
                        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                        out.println(ob.getValue());
                        out.flush();
                        return;

                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
            Q.reset_name_scope();
            DBGeneral dbG = new DBGeneral();

            
            String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            StringObject OldFacet = new StringObject(prefix.concat(oldName));
            String facet = prefix.concat(newName);

            //CODE IMPORTED FROM RenameCheck_Facet


            ret1 = TMSAPIClass.TMS_APISucc;
            /*
            try {
                byte[] byteArray = newName.getBytes("UTF-8");

                int maxFacetChars = dbtr.getMaxBytesForFacet(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxFacetChars) {
                    Vector<String> errorArgs = new Vector<String>();
                    errorArgs.add("" + maxFacetChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(ob, "root/EditFacet/Rename/LongName", errorArgs, pathToMessagesXML);
                    ret1 = TMSAPIClass.TMS_APIFail;

                    
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */

            if (ret1 == TMSAPIClass.TMS_APIFail) {

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                
            } else if ((OldFacet.toString().trim()).equals(prefix.toString().trim())) {

                //OLD NAME NULL?
                ob.setValue("Δεν επιλέχθηκε μικροθησαυρός για μετονομασία. Ακύρωση μετονομασίας.");
                ret1 = TMSAPIClass.TMS_APIFail;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            } else if (!dbG.check_exist(OldFacet.toString(), Q, sis_session)) {

                //OLD NAME EXISTS?
                ob.setValue("Ο μικροθησαυρός προς μετονομασία δεν είναι έγκυρος."
                        + " Ανανεώστε τα περιεχόμενα της σελίδας αποτελεσμάτων και προσπαθήστε ξανά. Ακύρωση μετονομασίας.");
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                ret1 = TMSAPIClass.TMS_APIFail;

            } else if ((facet.toString().trim()).equals(prefix.toString().trim())) {

                //NEW NAME ONY PREFIX?
                ob.setValue("Δεν δόθηκε νέο όνομα για τον μικροθησαυρό προς μετονομασία. Ακύρωση μετονομασίας.");
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                ret1 = TMSAPIClass.TMS_APIFail;
            } else if (dbG.check_exist(facet, Q, sis_session)) {
                //NEW NAME EXISTS?
                ob.setValue("Το νέο όνομα υπάρχει ήδη στη βάση δεδομένων. Ακύρωση μετονομασίας.");
                
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                ret1 = TMSAPIClass.TMS_APIFail;

            } else {




                StringObject newFacet = new StringObject(prefix.concat(newName));
                StringObject oldFacet = new StringObject(prefix.concat(oldName));

                ret1 = TA.CHECK_RenameFacet(oldFacet, newFacet);

                TA.ALMOST_DONE_GetTMS_APIErrorMessage(ob);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage: " + ob.toString());
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GetTMS_APIErrorMessage ret1 : " + ret1);

                
                if (ret1 == TMSAPIClass.TMS_APISucc) {
                    
                    //commit transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_end_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                } else {
                    
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                }

            }

            
            if (ret1 == 0) {

                Xmlresult = setRenameSuccessResultInXml(oldName + " " + RenameResult);
            }



            //xml.append(u.getXMLStart());

            if (ret1 != 0) {
                //xml.append(u.getXMLMiddle(Xmlresult,  "RenameFacet"));
                out.println(ob.toString());
            } else {
                //xml.append(u.getXMLMiddle(Xmlresult,  "RenameFacet"));
                /*synchronized (session) {
                session.setAttribute("currentFacet", newName);
                }*/
                out.print("Success");
                out.print("<newName>" + newName + "</newName>");
                out.println(RenameResult.trim());
            }

            //xml.append(u.getXMLEnd());
            //u.xslTransform(out, xml, path + "/xml-xsl/page_contents.xsl");

            out.flush();

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            sessionInstance.writeBackToSession(session);
            out.close();
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

    //prepare XML rename Facet success/error element
    public String setRenameSuccessResultInXml(String RenameResult) {

        StringBuffer sb = new StringBuffer();
        sb.append("<currentRename>");
        sb.append("<facetError>");
        sb.append("<apotelesma>");
        sb.append(RenameResult);
        sb.append("</apotelesma>");
        sb.append("</facetError>");
        sb.append("</currentRename>");

        return sb.toString();
    }
}
