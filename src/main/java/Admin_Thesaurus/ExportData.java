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
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.NodeInfoStringContainer;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.SessionListener;
import Utils.SortItem;
import XMLHandling.WriteFileData;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.xml.transform.dom.DOMSource;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class ExportData extends ApplicationBasicServlet {

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

        DBexportData exp = new DBexportData();
        
        Utilities u = new Utilities();
        
        UsersClass WTMSUsers = new UsersClass();




        int port = request.getLocalPort();
        
        
        String logFileNamePath = session.getServletContext().getRealPath("/"+ConstantParameters.LogFilesFolderName);
        //String logFileNamePath = Parameters.BaseRealPath+"/"+ConstantParameters.LogFilesFolderName+"/";
        String time = Utilities.GetNow();


        // check for previous logon but because of ajax usage respond with Session Invalidate str

        UserInfoClass refSessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        if (refSessionUserInfo == null || !refSessionUserInfo.servletAccessControl(this.getClass().getName())) {
            out.println("Session Invalidate");
            response.sendRedirect("Index");
            return;
        }



        String oldSchemePrefix = ConstantParameters.SchemePrefix;
        String oldThesaurusReference = ConstantParameters.referenceThesaurusSchemeName;

        //parameters

        String exportXMLfilename = u.getDecodedParameterValue(request.getParameter("exportXMLfilename"));
        String exprortThesaurus = u.getDecodedParameterValue(request.getParameter("exportThesaurus"));
        String exportSchemaName = request.getParameter("exportschematype");
        //String exportSchemaName    =  ConstantParameters.xmlschematype_THEMAS;
        String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        try {


            
            UsersClass webappusers = new UsersClass();



            String initiallySelectedThes = refSessionUserInfo.selectedThesaurus;


            UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
            webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, exprortThesaurus);

            //data storage
            StringBuffer xml = new StringBuffer();


            //Statistics variables for logFile and duraion
            long startTime = Utilities.startTimer();






            String Filename = "Export_Thesaurus_" + exprortThesaurus + "_" + time;


            if (exportSchemaName.equals(ConstantParameters.xmlschematype_skos)) {
                Filename += ".rdf";
                ConstantParameters.referenceThesaurusSchemeName = "http://localhost:" + port + "/" + Parameters.ApplicationName + "#" + exprortThesaurus;
                ConstantParameters.SchemePrefix = "http://localhost:" + port + "/" + Parameters.ApplicationName + "/" + exprortThesaurus;
                ConstantParameters.SchemePrefix = ConstantParameters.SchemePrefix.toLowerCase();

            } else if (exportSchemaName.equals(ConstantParameters.xmlschematype_THEMAS)) {
                Filename += ".xml";
            }

            logFileNamePath += "/" + Filename;



            try {
                OutputStream fout = new FileOutputStream(logFileNamePath);
                OutputStream bout = new BufferedOutputStream(fout);
                logFileWriter = new OutputStreamWriter(bout, "UTF-8");
                

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile εξαγωγής δεδομένων του θησαυρού : " + exprortThesaurus + " στο αρχείο: " + logFileNamePath + ".");

            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }

            Vector<String> thesauriNames = new Vector<String>();
            Vector<String> allHierarchies = new Vector<String>();
            Vector<String> allGuideTerms = new Vector<String>();
            
            exp.exportThesaurusActions(SessionUserInfo, exprortThesaurus, exportSchemaName, logFileWriter,thesauriNames,allHierarchies,allGuideTerms);


            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "exported in time " + Utilities.stopTimer(startTime) + " sec.");



            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
            xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
            xml.append(getXMLMiddle(thesauriNames, Filename));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            // ---------------------- UNLOCK SYSTEM ----------------------
            ConstantParameters.SchemePrefix = oldSchemePrefix;
            ConstantParameters.referenceThesaurusSchemeName = oldThesaurusReference;

            dbAdminUtils.UnlockSystemForAdministrativeJobs();
            out.close();
            sessionInstance.writeBackToSession(session);


        }


    }

    public String getXMLMiddle(Vector<String> thesaurusVector, String filePath) {
        // get the active sessions
        int OtherActiveSessionsNO = SessionListener.activesessionsNO - 1;

        StringBuffer XMLMiddleStr = new StringBuffer("");

        XMLMiddleStr.append("<content_Admin_Thesaurus>");


        XMLMiddleStr.append("<CurrentShownDIV>ImportExport_Data_DIV</CurrentShownDIV>");


        XMLMiddleStr.append("<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>");
        // write the existing Thesaurus in DB
        int thesaurusVectorCount = thesaurusVector.size();
        XMLMiddleStr.append("<existingThesaurus>");
        for (int i = 0; i < thesaurusVectorCount; i++) {
            XMLMiddleStr.append("<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>");
        }
        XMLMiddleStr.append("</existingThesaurus>");
        XMLMiddleStr.append("<exportFile>" + filePath + "</exportFile>");
        XMLMiddleStr.append("</content_Admin_Thesaurus>");

        return XMLMiddleStr.toString();
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
