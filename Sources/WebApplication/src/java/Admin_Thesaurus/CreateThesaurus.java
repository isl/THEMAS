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
import Utils.ConstantParameters;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/*---------------------------------------------------------------------
CreateThesaurus
-----------------------------------------------------------------------
Servlet for the creation of a new Thesaurus
----------------------------------------------------------------------*/
public class CreateThesaurus extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
		if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        // ---------------------- LOCK SYSTEM ----------------------
                String basePath = request.getSession().getServletContext().getRealPath("");
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.LockSystemForAdministrativeJobs(config);
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        		
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        
        try {
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String WebAppUsersFileName = request.getSession().getServletContext().getRealPath("/"+UsersClass.WebAppUsersXMLFilePath);

            DBGeneral dbGen = new DBGeneral();
            StringBuffer xml = new StringBuffer();

            // create the common-utils class
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);

            Utilities u = new Utilities();

            // get form parameters
            Hashtable params = u.getFormParams(request);
            String NewThesaurusName = params.get("Create_Thesaurus_NewName_NAME").toString();
            NewThesaurusName = NewThesaurusName.trim();
            NewThesaurusName = NewThesaurusName.replaceAll(" ", "_");
            NewThesaurusName = NewThesaurusName.toUpperCase();
            String NewThesaurusNameDBformatted = NewThesaurusName;
            // InitializeDB
            String InitializeDB = request.getParameter("InitDB"); // "on" or null
            StringObject DBbackupFileNameCreated = new StringObject("");


            // open SIS and TMS connection
            QClass Q = new neo4j_sisapi.QClass();            
            IntegerObject sis_session = new IntegerObject();

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            // Get the existing Thesaurus in DB
            Vector<String> thesaurusVector = new Vector<String>();
            thesaurusVector = dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);


            // check if data base is initialized
            boolean DataBaseIsInitialized = common_utils.DataBaseIsInitialized(Q);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            

            // initialize DB if chekbox was selected or DB is not initialized
            StringObject InitializeDBResultMessage = new StringObject("");
            Boolean DBInitializationSucceded = true;
            if (InitializeDB != null || DataBaseIsInitialized == false) {
                boolean DBCanBeInitialized = dbAdminUtils.DBCanBeInitialized(config, common_utils, NewThesaurusNameDBformatted, InitializeDBResultMessage, DBInitializationSucceded);
                if (DBCanBeInitialized == true) {
                    DBInitializationSucceded = dbAdminUtils.InitializeDB(common_utils, InitializeDBResultMessage);
                    // clear the vector with the existing Thesaurus in DB after DB initialization
                    thesaurusVector.clear();
                    
                    
                }
            }

            // do the creation of the new thesaurus        
            StringObject CreateThesaurusResultMessage = new StringObject("");

            Boolean CreateThesaurusSucceded = true;
            if (DBInitializationSucceded == true) {
                // check if the given NewThesaurusName exists
                boolean GivenThesaurusCanBeCreated = dbAdminUtils.GivenThesaurusCanBeCreated(config, common_utils, thesaurusVector, NewThesaurusName, NewThesaurusNameDBformatted, CreateThesaurusResultMessage, CreateThesaurusSucceded);
                if (GivenThesaurusCanBeCreated == true) {

                    CreateThesaurusSucceded = dbAdminUtils.CreateThesaurus(common_utils, NewThesaurusNameDBformatted,CreateThesaurusResultMessage, "backup_before_creating_new_thesaurus", DBbackupFileNameCreated);
                    // after finishing the job and in case SIS server is not running, restart it
                    // ATTENTION!!! the following must be done so as to fix the SARUMAN bug
                    // where after the creation of the Thesaurus, the SIS server was NOT restarted!
                    // In my machine, this bug is NOT reproduced...
                    //common_utils.RestartDatabaseIfNeeded();                
                    // start server
                    
                    // wait until server is finally started
                    
                }
                // Get the existing Thesaurus in DB (ALSO AFTER the creation of the new thesaurus, so as to be informed with the new one)
                if (CreateThesaurusSucceded == true) {
                    thesaurusVector.clear();
                    Q = new neo4j_sisapi.QClass();
                    sis_session = new IntegerObject();

                    //open connection and start Query
                    if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
                    {
                        Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                        return;
                    }
                    dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);
                    //dbAdminUtils.RefreshThesaurusVector(sessionInstance, Q, TA, sis_session, tms_session, dbGen, thesaurusVector);

                    //end query and close connection
                    Q.free_all_sets();
                    Q.TEST_end_query();
                    dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                    // inform current user's rights with the new thesaurus 
                    UsersClass tmsUsers = new UsersClass();
                    tmsUsers.AddNewThesaurusForCurrentTMSUser(WebAppUsersFileName, sessionInstance, NewThesaurusName);
                    tmsUsers.SetSessionAttributeSessionUser(sessionInstance,this.getServletContext(), SessionUserInfo.name, SessionUserInfo.password, NewThesaurusNameDBformatted, SessionUserInfo.userGroup);
                    tmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, NewThesaurusName);
                    
                }
            }

            Vector<String> allHierarchies = new Vector<String>();
            Vector<String> allGuideTerms = new Vector<String>();

            // open SIS and TMS connection
            Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
            sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            


            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies,allGuideTerms);

            Vector<String> status = new Vector<String>();
            status.addAll(dbGen.returnResults(SessionUserInfo, Parameters.UnclassifiedTermsLogicalname, ConstantParameters.status_kwd,Q, TA,sis_session));


            StringObject resultObj = new StringObject("");
            if(status.size()==0){
                DBImportData dbImport = new DBImportData();
                dbImport.specifyOrphansStatus(SessionUserInfo, Q, TA, sis_session, tms_session, resultObj);

            }

            if(resultObj.getValue().length()>0){
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }
            else{
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }
            
         
            // write the XML results
            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
            xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies,allGuideTerms,targetLocale));
            xml.append(getXMLMiddle(common_utils, thesaurusVector, NewThesaurusName, InitializeDBResultMessage, CreateThesaurusResultMessage, CreateThesaurusSucceded));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            //out.println("DONE");      
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");

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
    public String getXMLMiddle(CommonUtilsDBadmin common_utils, Vector thesaurusVector, String NewThesaurusName, StringObject InitializeDBResultMessage, StringObject CreateThesaurusResultMessage, Boolean CreateThesaurusSucceded) {
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
        XMLMiddleStr += "<CreateThesaurusResult>";
        // write the NewThesaurusName given
        XMLMiddleStr += "<NewThesaurusName>" + common_utils.ReplaceSpecialCharacters(NewThesaurusName) + "</NewThesaurusName>";
        XMLMiddleStr += "<CreateThesaurusResult>" + CreateThesaurusSucceded + "</CreateThesaurusResult>";
        XMLMiddleStr += "<InitializeDBResultMessage>" + InitializeDBResultMessage.getValue() + "</InitializeDBResultMessage>";
        XMLMiddleStr += "<CreateThesaurusResultMessage>" + CreateThesaurusResultMessage.getValue() + "</CreateThesaurusResultMessage>";
        XMLMiddleStr += "</CreateThesaurusResult>";
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }
    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}