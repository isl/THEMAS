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

import DB_Admin.ConfigDBadmin;
import DB_Admin.DBAdminUtilities;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;
import java.io.*;
import java.util.*;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tzortzak
 */
public class FixCurrentData extends ApplicationBasicServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        if (DB_Admin.DBAdminUtilities.isSystemLocked()) {
            out.println("SYSTEM LOCKED");
            out.close();
            request.getSession().invalidate();
            return;
        }
        String basePath = request.getSession().getServletContext().getRealPath("");

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        try {

            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }
            Boolean fixed = new Boolean(true);

            QClass Q = new QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBFixCurrentData dbFixData = new DBFixCurrentData();

            //parameters
            String mode = (String) request.getParameter("mode");
            ConfigDBadmin config = new ConfigDBadmin(basePath);
            DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
            if (mode.compareTo("Fix") == 0) {
                // ---------------------- LOCK SYSTEM ----------------------
                dbAdminUtils.LockSystemForAdministrativeJobs(config);
            }
            String functionallity = (String) request.getParameter("functionallity");
            String targetHierarchy = u.getDecodedParameterValue(request.getParameter("targetHierarchy"));
            String targetStatus = u.getDecodedParameterValue(request.getParameter("targetStatus"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
            String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/" + webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
            String time = Utilities.GetNow();
            StringObject Save_Results_file_name = new StringObject("functionality_not_supported_" + time);//if functionallity supported Save_Results_file_name must change value
            StringObject XSL_fileNameObject = new StringObject("ERROR### XSL Not Determined");

            //open sis connection
            //Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService()/*Parameters.server_host, Integer.parseInt(Parameters.server_port),Parameters.db_username, Parameters.db_password*/);
            // Q.TEST_open_connection();
            //int ret = TA.ALMOST_DONE_create_TMS_API_Session(Q,SessionUserInfo.selectedThesaurus);
            /*if (ret == QClass.APIFail) {
             dbGen.CloseDBConnection(Q, null, sis_session,null, false);
             return;
             }        */
            //TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            //begin query or transaction will be performed inside handler functions
            fixed = dbFixData.previewOrfix(SessionUserInfo, context, Q, TA, sis_session, tms_session, targetLocale, functionallity, mode, targetHierarchy, targetStatus, time, webAppSaveResults_temporary_filesAbsolutePath, webAppSaveResults_Folder, Save_Results_file_name, XSL_fileNameObject);

            if (mode.compareTo("Fix") == 0) {
                // ---------------------- LOCK SYSTEM ----------------------
                dbAdminUtils.UnlockSystemForAdministrativeJobs();
            }

            if (mode.compareTo("Preview") == 0) {

                String XML_file = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name.getValue() + ".xml";
                String XSL_file = XSL_fileNameObject.getValue();
                String HTML_file = webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name.getValue() + ".html";
                u.XmlFileTransform(XML_file, XSL_file, HTML_file);
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.getValue().concat(".html"));

            } else {
                if (fixed) {
                    out.println("OK");
                } else {
                    out.println("Retry");
                }
            }
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
     *
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
     *
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
}
