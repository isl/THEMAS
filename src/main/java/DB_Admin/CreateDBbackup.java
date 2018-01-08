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
package DB_Admin;




import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
CreateDBbackup
-----------------------------------------------------------------------
Servlet for the creation of a DB-backup
----------------------------------------------------------------------*/
public class CreateDBbackup extends ApplicationBasicServlet {

    ConfigDBadmin config;
    CommonUtilsDBadmin common_utils;
    boolean CreateDBbackupSucceded;
    StringObject CreateDBbackupResultMessage;
    // form parameters
    String backupDescription;

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
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
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);
        
        PrintWriter out = response.getWriter();

        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
            out.println("Session Invalidate");
            response.sendRedirect("Index");
            return;
        }
        
        try {
            StringBuffer xml = new StringBuffer();
            // get form parameters
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            HashMap params = u.getFormParams(request);
            backupDescription = params.get("Create_DB_backup_Description_NAME").toString();

            // create the configuration class
            config = new ConfigDBadmin(basePath);
            // create the common-utils class
            common_utils = new CommonUtilsDBadmin(config);

            // do the creation of the backup
            CreateDBbackupResultMessage = new StringObject("");
            StringObject DBbackupFileNameCreated = new StringObject("");
            CreateDBbackupSucceded = common_utils.CreateDBbackup(backupDescription, CreateDBbackupResultMessage, DBbackupFileNameCreated, SessionUserInfo.UILang);

            // write the XML results
            xml.append(u.getXMLStart(ConstantParameters.LMENU_DATABASE, SessionUserInfo.UILang));
            //xml.append(u.getDBAdminHierarchiesAndStatusesXML(allHierarcies, dbGen));  
            xml.append(getXMLMiddle());
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/page_contents.xsl");

            // ---------------------- UNLOCK SYSTEM ----------------------
            dbAdminUtils.UnlockSystemForAdministrativeJobs();
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
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
    public String getXMLMiddle() {
        String XMLMiddleStr = "<content_DBadmin>";
        XMLMiddleStr += "<CurrentShownDIV>" + "Create_Restore_DB_backup_DIV" + "</CurrentShownDIV>";
        // in case there are other active sessions => write their number to XML, 
        // so as to warn user for their existence
        // GetListOfDBbackups
        ArrayList<String> filesInDBBackupFolder = new ArrayList<String>();
        filesInDBBackupFolder = common_utils.GetListOfDBbackups();
        int filesInDBBackupFolderCount = filesInDBBackupFolder.size();
        XMLMiddleStr += "<filesInDBBackupFolder>";
        for (int i = 0; i < filesInDBBackupFolderCount; i++) {
            XMLMiddleStr += "<DBBackup>" + filesInDBBackupFolder.get(i) + "</DBBackup>";
        }
        XMLMiddleStr += "</filesInDBBackupFolder>";

        XMLMiddleStr += "<OtherActiveSessionsNO>0</OtherActiveSessionsNO>";
        // write the results
        XMLMiddleStr += "<CreateDBbackupResult>";
        XMLMiddleStr += "<CreateDBbackupSucceded>" + CreateDBbackupSucceded + "</CreateDBbackupSucceded>";
        XMLMiddleStr += "<CreateDBbackupResultMessage>" + CreateDBbackupResultMessage.getValue() + "</CreateDBbackupResultMessage>";
        XMLMiddleStr += "<backupDescription>" + common_utils.ReplaceSpecialCharacters(backupDescription) + "</backupDescription>";
        XMLMiddleStr += "</CreateDBbackupResult>";
        XMLMiddleStr += "</content_DBadmin>";

        return XMLMiddleStr;
    }

    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}