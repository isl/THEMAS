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
import Utils.SessionListener;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBadmin
-----------------------------------------------------------------------
  Servlet for displaying the forms for DB-backup: create, restore or DB fix
----------------------------------------------------------------------*/
public class DBadmin extends ApplicationBasicServlet {
   
    ConfigDBadmin config;
    CommonUtilsDBadmin common_utils;  
    Vector<String> thesaurusVector; // the existing Thesaurus in DB
    // servlet parameters
    String CurrentShownDIV; // Create_DB_backup_DIV / Restore_DB_backup_DIV / Fix_DB_DIV / Create_Thesaurus_DIV (DIV ids defined in DBadmin_contents.xsl)

    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        String basePath = request.getSession().getServletContext().getRealPath("");

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        try{
            config = new ConfigDBadmin(basePath);// create the configuration class
            common_utils = new CommonUtilsDBadmin(config);// create the common-utils class        
            CurrentShownDIV = request.getParameter("DIV");// get servlet parameters    
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }
            StringBuffer xml = new StringBuffer();

            QClass Q = new QClass();            
            IntegerObject sis_session = new IntegerObject();
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            // Get the existing Thesaurus in DB
            thesaurusVector = new Vector<String>();
            thesaurusVector = dbGen.GetExistingThesaurus(false, thesaurusVector,Q,sis_session);        


            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            

            xml.append(u.getXMLStart(ConstantParameters.LMENU_DATABASE));  
            xml.append(getXMLMiddle());
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
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
        // get the active sessions
        SessionListener SL = new SessionListener();
        int OtherActiveSessionsNO = SL.activesessionsNO - 1;
        
        String XMLMiddleStr = "<content_DBadmin>";
            if(CurrentShownDIV.compareTo(XMLMiddleStr)==0){
                //add a drop down of hierarchies and statuses
                
            }
            else{
                XMLMiddleStr += "<CurrentShownDIV>" + CurrentShownDIV + "</CurrentShownDIV>";
            }
            // in case there are other active sessions => write their number to XML, 
            // so as to warn user for their existence
            XMLMiddleStr += "<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>";
            // write the existing Thesaurus in DB
            int thesaurusVectorCount = thesaurusVector.size();
            XMLMiddleStr += "<existingThesaurus>";
            for(int i=0; i< thesaurusVectorCount; i++) {
                XMLMiddleStr += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
            }
            XMLMiddleStr += "</existingThesaurus>";                        
            // GetListOfDBbackups
            Vector<String> filesInDBBackupFolder = new Vector<String>();
            filesInDBBackupFolder = common_utils.GetListOfDBbackups();
            int filesInDBBackupFolderCount = filesInDBBackupFolder.size();
            XMLMiddleStr += "<filesInDBBackupFolder>";
            for(int i=0; i< filesInDBBackupFolderCount; i++) {
                XMLMiddleStr += "<DBBackup>" + filesInDBBackupFolder.get(i) + "</DBBackup>";
            }
            XMLMiddleStr += "</filesInDBBackupFolder>";
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