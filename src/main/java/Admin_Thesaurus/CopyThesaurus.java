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
import Users.UsersClass;
import Servlets.ApplicationBasicServlet;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;

import Users.UserInfoClass;
import Utils.ConstantParameters;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import neo4j_sisapi.*;

/**
 *
 * @author tzortzak
 */
public class CopyThesaurus extends ApplicationBasicServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    //final String LogFilesFolderName = "LogFiles";
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }
        // ---------------------- LOCK SYSTEM ----------------------
        String basePath = request.getSession().getServletContext().getRealPath("");
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.LockSystemForAdministrativeJobs(config);

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");



        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);


        try (PrintWriter out = response.getWriter()) {


            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }


            String sourceThesaurusName = request.getParameter("sourceThesaurus");
            String targetThesaurusName = request.getParameter("Copy_Thesaurus_NewName_NAME");
            String pathToErrorsXML = Utilities.getXml_For_ConsistencyChecks();
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String pathToSaveScriptingAndLocale = Utilities.getXml_For_SaveAll_Locale_And_Scripting();
            String XSL = context.getRealPath("/"+webAppSaveResults_Folder) + "/ImportCopyMergeThesaurus_Report.xsl";
            String logPath = context.getRealPath("/"+ConstantParameters.LogFilesFolderName);
            String WebAppUsersFileName = request.getSession().getServletContext().getRealPath("/"+UsersClass.WebAppUsersXMLFilePath);

            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);


            //tools
            Utilities u = new Utilities();
            
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
            DBImportData dbImport = new DBImportData();
            
            //data
            StringBuffer xml = new StringBuffer();
            StringObject CopyThesaurusResultMessage = new StringObject("");

            StringObject resultObj = new StringObject();
            String initiallySelectedThesaurus = SessionUserInfo.selectedThesaurus;

            //Format Name Of merged Thesauri
            targetThesaurusName = targetThesaurusName.trim();
            targetThesaurusName = targetThesaurusName.replaceAll(" ", "_");
            targetThesaurusName = targetThesaurusName.toUpperCase();
            
            
            //Statistics variables for logFile and duraion
            long startTime = Utilities.startTimer();

            String logFileNamePath = logPath;
            String time = Utilities.GetNow();
            String Filename = "Copy_Thesaurus_" + sourceThesaurusName + "_in_" + targetThesaurusName + "_" + time;
            logFileNamePath += "/" + Filename + ".xml";
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting Thesaurus copy operation at: " + Utilities.GetNow());
            
            //IMPORT ACTIONS
            if(dbImport.thesaurusCopyActions(SessionUserInfo, common_utils, config,
                    pathToErrorsXML, sourceThesaurusName, targetThesaurusName,
                    targetLocale, resultObj,
                    CopyThesaurusResultMessage, xml,
                    logFileNamePath, pathToSaveScriptingAndLocale, startTime, out)){
                //sucess new thesaurus is created it should be set as current
                UsersClass wtmsUsers = new UsersClass();
                wtmsUsers.AddNewThesaurusForCurrentTMSUser(WebAppUsersFileName, sessionInstance, targetThesaurusName);
            }

            
            /*
            if (== false) {

                //abortActions(context, sessionInstance, Q, TA, sis_session, tms_session, targetLocale, common_utils, initiallySelectedThesaurus, targetThesaurusName, DBbackupFileNameCreated, resultObj, out);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Thesaurus copy operation failed.");
            } 
            else {
                //SUCESS
                // inform current user's rights with the new thesaurus
                UsersClass wtmsUsers = new UsersClass();
                wtmsUsers.AddNewThesaurusForCurrentTMSUser(request, sessionInstance, targetThesaurusName);


                commitActions(context, sessionInstance, common_utils, Q, TA, sis_session, tms_session, targetLocale, targetThesaurusName, out, Filename.concat(".html"));
                logFileWriter.append("\r\n<creationInfo>Thesaurus copy operation was successfully completed in: " + ((Utilities.stopTimer(startTime)) / 60) + " minutes.</creationInfo>\r\n");
                if (logFileWriter != null) {
                    logFileWriter.append("</page>");
                    logFileWriter.flush();
                    logFileWriter.close();
                }

            }
             * 
             */

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of thesaurus copy operation at: " + Utilities.GetNow() +". Time passed: "+((Utilities.stopTimer(startTime)) / 60) + " minutes.");

            //Now XSL should be found and java xsl transformation should be performed
            u.XmlFileTransform(logFileNamePath, XSL, logPath + "/" + Filename.concat(".html"));


            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Thesaurus copy operation was sucessfully completed in: " + ((Utilities.stopTimer(startTime)) / 60) + " minutes.");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());            
            Utils.StaticClass.handleException(e);            
        } finally {
            sessionInstance.writeBackToSession(session);
        }
    }
/*
    public String getXMLMiddle(CommonUtilsDBadmin common_utils, Vector thesaurusVector,
            String NewThesaurusName, StringObject CopyThesaurusResultMessage, Boolean CopyThesaurusSucceded) {
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
        XMLMiddleStr += "<copyThesaurusResult>";
        // write the NewThesaurusName given
                
        XMLMiddleStr +=  CopyThesaurusResultMessage.getValue() ;
        XMLMiddleStr += "</copyThesaurusResult>";
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }
*/
   

    

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
