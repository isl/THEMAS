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
package LoginAdmin;

import Users.UsersClass;
import DB_Admin.DBAdminUtilities;
import DB_Admin.ConfigDBadmin;
import Admin_Thesaurus.DBFixCurrentData;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ArrayList;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class FixAdminData extends ApplicationBasicServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String  basePath = request.getSession().getServletContext().getRealPath("");
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
            
            
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName()) || SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator)==false ) {
                out.println("Session Invalidate");
                return;
            }
            Boolean fixed = true;
            
            QClass Q = new QClass(); 
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            
            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            DBFixCurrentData dbFixData= new DBFixCurrentData();
            
            //parameters
            String mode = (String) request.getParameter("mode");
            
            ConfigDBadmin config = new ConfigDBadmin(basePath);
            DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
            if (mode.compareTo("Fix") == 0) {
                // ---------------------- LOCK SYSTEM ----------------------
                dbAdminUtils.LockSystemForAdministrativeJobs(config);                
            }                        
            String functionallity = (String)  request.getParameter("functionallity");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            String selectedThesaurus = u.getDecodedParameterValue(request.getParameter("selectedThesaurus"));Locale targetLocale = new Locale(language, country);
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
            String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
            String time = Utilities.GetNow();
            StringObject Save_Results_file_name = new StringObject("functionality_not_supported_" + time);
            StringObject XSL_fileNameObject = new StringObject("ERROR### XSL Not Determined");
            ArrayList<String> thesaurusVector = new ArrayList<String>();
            
            //open sis connection
            Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService()/*Parameters.server_host, Integer.parseInt(Parameters.server_port),Parameters.db_username, Parameters.db_password*/);
            Q.TEST_open_connection();
            
           /* int ret = TA.ALMOST_DONE_create_TMS_API_Session(Q,null);
            if (ret == QClass.APIFail) {
                dbGen.CloseDBConnection(Q, null, sis_session,null, false);
                return;
            } */           
            
            dbGen.GetExistingThesaurus(true, thesaurusVector, Q, sis_session);
            if(thesaurusVector.contains(selectedThesaurus)){
                UsersClass wtmsUsers = new UsersClass();
                String targetLang = (SessionUserInfo ==null || SessionUserInfo.UILang==null) ? Parameters.UILang : SessionUserInfo.UILang;
                wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, getServletContext(), SessionUserInfo.name, SessionUserInfo.password, selectedThesaurus, SessionUserInfo.userGroup, targetLang);
                SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            }
            else{
                out.println("Failure" + "THESAURUS_NOT_FOUND" );
                if (mode.compareTo("Fix") == 0) {
                    // ---------------------- LOCK SYSTEM ----------------------
                    dbAdminUtils.UnlockSystemForAdministrativeJobs();                
                }
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                return;
            }
            
            TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            //begin query or transaction will be performed inside handler functions

            fixed = dbFixData.previewOrfix(SessionUserInfo,context, Q, TA, sis_session, tms_session, targetLocale, functionallity, mode, null, null, time, webAppSaveResults_temporary_filesAbsolutePath, webAppSaveResults_Folder, Save_Results_file_name,XSL_fileNameObject);
            
            if (mode.compareTo("Fix") == 0) {
                // ---------------------- LOCK SYSTEM ----------------------
                dbAdminUtils.UnlockSystemForAdministrativeJobs();                
            }                        
            
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            //String resopnseStr= "FixCurrentData2.java Reached with params : mode = " + mode + " functionallity = " +  functionallity;
            if (mode.compareTo("Preview") == 0) {
                
                String XML_file =webAppSaveResults_temporary_filesAbsolutePath + "/" + Save_Results_file_name.getValue() + ".xml";
                String XSL_file =XSL_fileNameObject.getValue();
                String HTML_file=webAppSaveResults_temporary_filesAbsolutePath+ "/" + Save_Results_file_name.getValue() + ".html";
                u.XmlFileTransform(XML_file, XSL_file, HTML_file, sessionInstance.path+"/");
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.getValue().concat(".html"));
                
            } else {
                if (fixed) {
                    out.println("OK");
                } else {
                    out.println("Retry");
                }
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
