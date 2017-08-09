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

import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            RestoreDBbackup
-----------------------------------------------------------------------
  Servlet for the deletion/restoration of a DB-backup
----------------------------------------------------------------------*/
public class RestoreDBbackup extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
        //ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();

        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
            out.println("Session Invalidate");
            response.sendRedirect("Index");
            return;
        }
        try{

            StringBuffer xml = new StringBuffer();
            
            // get servlet parameters
            String action = request.getParameter("action"); 

            // get form parameters
            Utilities u = new Utilities();
            Hashtable params = u.getFormParams(request); 
            String selectedDBbackupFileName = params.get("DB_backupsListNAME").toString();

            // create the common-utils class
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);

            StringObject RestoreDBbackupResultMessage = new StringObject("");
            Boolean RestoreDBbackupSucceded = true;
            if (action.compareTo("DELETE") == 0) {
                // do the deletion of the backup
                RestoreDBbackupSucceded = DeleteDBbackup(common_utils,selectedDBbackupFileName, RestoreDBbackupResultMessage);            
            }
            else { // RESTORE
                StringObject result = new StringObject("");
                // create a backup of the data base anyway
                StringObject DBbackupFileNameCreated = new StringObject("");
                common_utils.CreateDBbackup("backup_before_restoring_data_base_backup", result, DBbackupFileNameCreated);             

                // do the restoration of the backup
                RestoreDBbackupSucceded = common_utils.RestoreDBbackup(selectedDBbackupFileName, result);
                RestoreDBbackupResultMessage.setValue(result.getValue());
            }

            // write the XML results
            xml.append(u.getXMLStart(ConstantParameters.LMENU_DATABASE));  
            //xml.append(u.getDBAdminHierarchiesAndStatusesXML(allHierarcies, dbGen));
            xml.append(getXMLMiddle(common_utils,action,RestoreDBbackupResultMessage,RestoreDBbackupSucceded));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/page_contents.xsl");
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
            out.close();
            sessionInstance.writeBackToSession(session);
        }
        
	
        
        // ---------------------- UNLOCK SYSTEM ----------------------
        dbAdminUtils.UnlockSystemForAdministrativeJobs();                        
    }
    
    /*---------------------------------------------------------------------
                            getXMLMiddle()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLMiddleStr: an XML string with the necessary data of this servlet
    ----------------------------------------------------------------------*/
    public String getXMLMiddle(CommonUtilsDBadmin common_utils, String action, StringObject RestoreDBbackupResultMessage, Boolean RestoreDBbackupSucceded) {
        String XMLMiddleStr = "<content_DBadmin>";
            XMLMiddleStr += "<CurrentShownDIV>" + "Create_Restore_DB_backup_DIV" + "</CurrentShownDIV>";
            // in case there are other active sessions => write their number to XML, 
            // so as to warn user for their existence
            XMLMiddleStr += "<OtherActiveSessionsNO>0</OtherActiveSessionsNO>";
            // GetListOfDBbackups
            Vector<String> filesInDBBackupFolder = new Vector<String>();
            filesInDBBackupFolder = common_utils.GetListOfDBbackups();
            int filesInDBBackupFolderCount = filesInDBBackupFolder.size();
            XMLMiddleStr += "<filesInDBBackupFolder>";
            for(int i=0; i< filesInDBBackupFolderCount; i++) {
                XMLMiddleStr += "<DBBackup>" + filesInDBBackupFolder.get(i) + "</DBBackup>";
            }
            XMLMiddleStr += "</filesInDBBackupFolder>";            
            // write the results
            XMLMiddleStr += "<RestoreDBbackupResult>";
                XMLMiddleStr += "<action>" + action + "</action>";
                XMLMiddleStr += "<RestoreDBbackupSucceded>" + RestoreDBbackupSucceded + "</RestoreDBbackupSucceded>";
                XMLMiddleStr += "<RestoreDBbackupResultMessage>" + RestoreDBbackupResultMessage.getValue() + "</RestoreDBbackupResultMessage>";
            XMLMiddleStr += "</RestoreDBbackupResult>";
        XMLMiddleStr += "</content_DBadmin>";

        return XMLMiddleStr;
    }    
    
    /*-----------------------------------------------------
                      DeleteDBbackup()
    -------------------------------------------------------*/
    public boolean DeleteDBbackup(CommonUtilsDBadmin common_utils,String selectedDBbackupFileName, StringObject RestoreDBbackupResultMessage) {
        File fileForDeletion = new File(common_utils.DB_BackupFolder.getPath() + File.separator + selectedDBbackupFileName);
        boolean deletionSucceded = fileForDeletion.delete();        
        if (deletionSucceded == false) {
            RestoreDBbackupResultMessage.setValue("Δεν βρέθηκε το αρχείο " + selectedDBbackupFileName);
            return false;
        }
        RestoreDBbackupResultMessage.setValue("Το αρχείο " + selectedDBbackupFileName + " διαγράφηκε");
        return true;
    }    
    

    /*---------------------------------------------------------------------
                                    doPost()
    ----------------------------------------------------------------------*/                
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	doGet(request, response);
    }
}