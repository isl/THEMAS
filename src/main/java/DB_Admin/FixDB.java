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
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            FixDB
-----------------------------------------------------------------------
  Servlet for the fixing of the DB
----------------------------------------------------------------------*/
public class FixDB extends ApplicationBasicServlet {
    ConfigDBadmin config_global;
    CommonUtilsDBadmin common_utils_global;
    boolean  FixDBSucceded;
    StringObject FixDBResultMessage_Global;

    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        String basePath = request.getSession().getServletContext().getRealPath("");
        // ---------------------- LOCK SYSTEM ----------------------
        config_global = new ConfigDBadmin(basePath);
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.LockSystemForAdministrativeJobs(config_global);
        
        response.setContentType("text/html;charset=UTF-8");
	request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        try{

            StringBuffer xml = new StringBuffer();

            Utilities u = new Utilities();

            // create the configuration class
            config_global = new ConfigDBadmin(basePath);
            // create the common-utils class
            common_utils_global = new CommonUtilsDBadmin(config_global);

            FixDBResultMessage_Global = new StringObject("");
            FixDBSucceded = true;
            // do the fixinf of the DB
            FixDBSucceded = common_utils_global.FixDB(true, FixDBResultMessage_Global);

            // write the XML results
            xml.append(u.getXMLStart(ConstantParameters.LMENU_DATABASE));  
            //xml.append(u.getDBAdminHierarchiesAndStatusesXML(allHierarcies, dbGen));  
            xml.append(getXMLMiddle());
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
    public String getXMLMiddle() {
        String XMLMiddleStr = "<content_DBadmin>";
            XMLMiddleStr += "<CurrentShownDIV>" + "Fix_DB_DIV" + "</CurrentShownDIV>";
            
            // GetListOfDBbackups
            Vector<String> filesInDBBackupFolder = new Vector<String>();
            filesInDBBackupFolder = common_utils_global.GetListOfDBbackups();
            int filesInDBBackupFolderCount = filesInDBBackupFolder.size();
            XMLMiddleStr += "<filesInDBBackupFolder>";
            for(int i=0; i< filesInDBBackupFolderCount; i++) {
                XMLMiddleStr += "<DBBackup>" + filesInDBBackupFolder.get(i) + "</DBBackup>";
            }
            XMLMiddleStr += "</filesInDBBackupFolder>";   
            // in case there are other active sessions => write their number to XML, 
            // so as to warn user for their existence
            XMLMiddleStr += "<OtherActiveSessionsNO>0</OtherActiveSessionsNO>";
            // write the results
            XMLMiddleStr += "<FixDBResult>";
                XMLMiddleStr += "<FixDBResult>" + FixDBSucceded + "</FixDBResult>";
                XMLMiddleStr += "<FixDBResultMessage>" + FixDBResultMessage_Global.getValue() + "</FixDBResultMessage>";
            XMLMiddleStr += "</FixDBResult>";
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