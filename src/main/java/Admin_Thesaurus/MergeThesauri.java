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

import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import DB_Admin.DBAdminUtilities;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;

import Utils.ConsistensyCheck;
import Utils.ConstantParameters;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import java.util.Vector;
import java.util.Locale;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/*
 * @author tzortzak
 */
public class MergeThesauri extends ApplicationBasicServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    //final String LogFilesFolderName = "LogFiles";
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }
        String basePath = request.getSession().getServletContext().getRealPath("");

        // ---------------------- LOCK SYSTEM ----------------------
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.LockSystemForAdministrativeJobs(config);

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();

        OutputStreamWriter logFileWriter = null;

        try {

            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }

            String thesaurusName1 = request.getParameter("thesaurus1");
            String thesaurusName2 = request.getParameter("thesaurus2");
            String mergedThesaurusName = request.getParameter("mergedThesaurusName");
            String pathToErrorsXML = context.getRealPath("/translations/Consistencies_Error_Codes.xml");
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
            String WebAppUsersFileName = request.getSession().getServletContext().getRealPath("/" + UsersClass.WebAppUsersXMLFilePath);

            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);

            //tools
            Utilities u = new Utilities();
            DBImportData dbImport = new DBImportData();
            StringBuffer xml = new StringBuffer();

            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);

            //data          
            StringObject CreateThesaurusResultMessage = new StringObject("");
            StringObject resultObj = new StringObject();
            String initiallySelectedThesaurus = SessionUserInfo.selectedThesaurus;

            //Format Name Of merged Thesauri
            mergedThesaurusName = mergedThesaurusName.trim();
            mergedThesaurusName = mergedThesaurusName.replaceAll(" ", "_");
            mergedThesaurusName = mergedThesaurusName.toUpperCase();
            //String mergedThesaurusNameDBformatted = mergedThesaurusName;

            StringObject DBbackupFileNameCreated = new StringObject("");

            //Statistics variables for logFile and duraion
            long startTime = Utilities.startTimer();

            String logPath = context.getRealPath("/" + ConstantParameters.LogFilesFolderName);
            String logFileNamePath = logPath;
            String time = Utilities.GetNow();
            String Filename = "Merge_Thesauri_" + thesaurusName1 + "_" + thesaurusName2 + "_in_" + mergedThesaurusName + "_" + time;
            logFileNamePath += "/" + Filename + ".xml";

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting thesaurus merge operation at: " + Utilities.GetNow());

            //thesaurusName1
            //thesaurusName2
            //mergedThesaurusName
            //IMPORT ACTIONS
            if (dbImport.thesaurusMergeActions(SessionUserInfo, common_utils, config,
                    pathToErrorsXML, thesaurusName1, thesaurusName2, mergedThesaurusName,
                    targetLocale, resultObj,
                    CreateThesaurusResultMessage, xml,
                    logFileNamePath, pathToSaveScriptingAndLocale, startTime, out)) {

                //sucess new thesaurus is created it should be set as current
                UsersClass wtmsUsers = new UsersClass();
                wtmsUsers.AddNewThesaurusForCurrentTMSUser(WebAppUsersFileName, sessionInstance, mergedThesaurusName);
            }

            //Now XSL should be found and java xsl transformation should be performed
            String XSL = context.getRealPath("/" + webAppSaveResults_Folder) + "/ImportCopyMergeThesaurus_Report.xsl";
            u.XmlFileTransform(logFileNamePath, XSL, logPath + "/" + Filename.concat(".html"));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Thesaurus merge operation of thesauri: " + thesaurusName1 + ", " + thesaurusName2 + " in thesaurus " + mergedThesaurusName + " was successfully completed in: " + ((Utilities.stopTimer(startTime)) / 60) + " minutes.");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
            if (logFileWriter != null) {
                logFileWriter.append("\r\n</page>");
                logFileWriter.flush();
                logFileWriter.close();
            }
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    /*---------------------------------------------------------------------
     getXMLMiddle()
     -----------------------------------------------------------------------
     OUTPUT: - String XMLMiddleStr: an XML string with the necessary data of this servlet
     ----------------------------------------------------------------------*/
    public String getXMLMiddle(Vector<String> thesaurusVector, String MergeThesaurusMessage) {
        String XMLMiddleStr = "<content_Admin_Thesaurus>";

        XMLMiddleStr += "<CurrentShownDIV>" + "CreateThesaurus_DIV" + "</CurrentShownDIV>";
        // in case there are other active sessions => write their number to XML, 
        // so as to warn user for their existence
        XMLMiddleStr += "<OtherActiveSessionsNO>0</OtherActiveSessionsNO>";
        // write the existing Thesaurus in DB
        int thesaurusVectorCount = thesaurusVector.size();
        XMLMiddleStr += "<existingThesaurus>";
        for (int i = 0; i < thesaurusVectorCount; i++) {
            XMLMiddleStr += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
        }
        XMLMiddleStr += "</existingThesaurus>";
        // write the results

        // write the NewThesaurusName given
        //XMLMiddleStr += "<NewThesaurusName>" + common_utils.ReplaceSpecialCharacters(NewThesaurusName) + "</NewThesaurusName>";            
        XMLMiddleStr += "<mergeThesauriResult>" + MergeThesaurusMessage + "</mergeThesauriResult>";

        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }

    public void abortActions(HttpServletRequest request, SessionWrapperClass sessionInstance, ServletContext context, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, Locale targetLocale, CommonUtilsDBadmin common_utils, String initiallySelectedThesaurus, String mergedThesaurusName, StringObject DBbackupFileNameCreated, StringObject resultObj, PrintWriter out) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ABORT MERGE");

        Q.TEST_abort_transaction();

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();
        Vector<String> thesauriNames = new Vector<String>();

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());

        StringObject result = new StringObject("");
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, initiallySelectedThesaurus, SessionUserInfo.userGroup);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + DBbackupFileNameCreated.getValue());

        boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);
        thesauriNames.remove(mergedThesaurusName);

        if (restored) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
            dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Did not manage to restore : " + DBbackupFileNameCreated.getValue());
        }

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));

        /*
         *
         *HARDCODED GREEKS
         *
         */
        String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
        StringObject resultMessageObj = new StringObject();
        Vector<String> errorArgs = new Vector<String>();

        dbGen.Translate(resultMessageObj, "root/abortActionsMergeThesauri/MergeFailure", null, pathToMessagesXML);
        xml.append(getXMLMiddle(thesauriNames, resultMessageObj.getValue() + resultObj.getValue()));
        
        //xml.append(getXMLMiddle(thesauriNames, "Merge of thesauri failure. " + resultObj.getValue()));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public void commitActions(HttpServletRequest request, SessionWrapperClass sessionInstance,
            ServletContext context, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Locale targetLocale, String mergedThesaurusName, PrintWriter out, StringBuffer mergeNotes, String Filename) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> thesauriNames = new Vector<String>();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();

        UserInfoClass refSessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);
        //wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, mergedThesaurusName, SessionUserInfo.userGroup);

        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        xml.append("<mergewarnings>");
        xml.append(mergeNotes);
        xml.append("</mergewarnings>");
        xml.append("<mergeReportFile>");
        xml.append(Filename);
        xml.append("</mergeReportFile>");
        /*
        *
        *HARDCODED GREEKS
        *
        */
        String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
        StringObject resultMessageObj = new StringObject();
        Vector<String> errorArgs = new Vector<String>();

        errorArgs.add(mergedThesaurusName);
        dbGen.Translate(resultMessageObj, "root/commitActionsMergeThesauri/MergeSucceeded", errorArgs, pathToMessagesXML);
        errorArgs.removeAllElements();

        xml.append(getXMLMiddle(thesauriNames, resultMessageObj.getValue()));        
        //xml.append(getXMLMiddle(thesauriNames, "Thesauri merge finished successfully. New thesaurus  " + mergedThesaurusName + " was set as current thesaurus."));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
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
