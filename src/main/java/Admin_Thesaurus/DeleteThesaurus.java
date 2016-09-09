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
DeleteThesaurus
-----------------------------------------------------------------------
Servlet for the deletion of a Thesaurus
----------------------------------------------------------------------*/
public class DeleteThesaurus extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        if (SystemIsLockedForAdministrativeJobs(request, response)){
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
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        try {

            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            // create the common-utils class
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
            // get form parameters
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            Hashtable params = u.getFormParams(request);
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            String ThesaurusName = params.get("deleteThesaurus").toString();
            String ThesaurusNameDBformatted = ThesaurusName;

            // do the deletion of the thesaurus 
            StringObject DeleteThesaurusResultMessage = new StringObject("");
            
            // create a backup of the data base anyway
            StringObject DBbackupFileNameCreated = new StringObject("");
            String backUpDescrition = new String("backup_before_deletion_of_thesaurus_"+ThesaurusName);            
            common_utils.CreateDBbackup(backUpDescrition, DeleteThesaurusResultMessage, DBbackupFileNameCreated);
            DeleteThesaurusResultMessage.setValue("");
            boolean DeleteThesaurusSucceded = true;

            // open SIS and TMS connection
            QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();


            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, ThesaurusNameDBformatted, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            dbAdminUtils.DeleteThesaurus(request, Q, TA, sis_session, tms_session, dbGen, ThesaurusNameDBformatted, DeleteThesaurusResultMessage);

            // check the result of the transaction and END/ABORT transaction
            if (DeleteThesaurusResultMessage.getValue() == null || DeleteThesaurusResultMessage.getValue().length() == 0) {

                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            } else {
                DeleteThesaurusSucceded = false;

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }            

            /*************************Step2 Ensure Database is Running***********************/
            //ensure that sis_server is running before starting open connection procedure and merge transactions
            // if server runs, stop it WITHOUT asking (it is necessary to close it before creating backup)
            /*boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
            if (databaseIsRunning == true) {
                common_utils.StopDatabase();
                // wait until server is finally stopped
                databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
                while (databaseIsRunning == true) {
                    databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
                }
            }*/
            boolean serverStarted = common_utils.StartDatabase();

            if (serverStarted == false) {
                String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
                //CopyThesaurusResultMessage.setValue(StartServerFailure + " " + common_utils.DatabaserBatFileDirectory + "\\" + common_utils.DatabaseBatFileName);
                common_utils.RestartDatabaseIfNeeded();
            }
            // wait until server is finally started
            /*databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
            while (databaseIsRunning == false) {
                databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
            }*/

            // Refresh the list of the existing Thesaurus in DB
            Vector<String> thesaurusVector = new Vector<String>();
            Q = new neo4j_sisapi.QClass();
            sis_session = new IntegerObject();

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            //dbAdminUtils.RefreshThesaurusVector(sessionInstance, Q, TA, sis_session, tms_session, dbGen, thesaurusVector);
            // in case of succesful deletion of the thesaurus inform user's rights for the deletion of the thesaurus (todo)
            
            /*
            *
            *           
            *HARDCODED GREEKS NEED TRANSLATION
            *
            *
            */
            String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();
            
            if (DeleteThesaurusSucceded == true) {
                errorArgs.add(ThesaurusName);
                dbGen.Translate(resultMessageObj, "root/DeleteThesaurus/DeleteThesaurusSucceded", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                
                DeleteThesaurusResultMessage.setValue(resultMessageObj.getValue());
                //DeleteThesaurusResultMessage.setValue("Η διαγραφή του θησαυρού " + ThesaurusName + " ολοκληρώθηκε με επιτυχία.");
                UsersClass tmsUsers = new UsersClass();
                synchronized (sessionInstance) {
                    if (thesaurusVector.size() > 0) {
                        tmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, thesaurusVector.get(0).toString(), SessionUserInfo.userGroup);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + " Setting current thesaurus to " + thesaurusVector.get(0).toString());
                    }
                }
                tmsUsers.DeleteThesaurusFromTMSUsers(request, ThesaurusName);
            }

            // inform hierarchies statuses (needed by Fix DB)
            Vector<String> thesauriNames = new Vector<String>();
            Vector<String> allHierarchies = new Vector<String>();
            Vector<String> allGuideTerms = new Vector<String>();
            // open SIS and TMS connection
            if (thesaurusVector.size() > 0) {
                Q = new neo4j_sisapi.QClass();
                
                sis_session = new IntegerObject();


                //open connection and start Query
                if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true)==QClass.APIFail)
                {
                    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                    return;
                }

                dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies,allGuideTerms);
                //end query and close connection
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                
            } else {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "No remaining thesauri");
            }

            // write the XML results
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
            xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies,allGuideTerms,targetLocale));
            xml.append(getXMLMiddle(common_utils, thesaurusVector, ThesaurusName, DeleteThesaurusResultMessage, DeleteThesaurusSucceded));
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
    public String getXMLMiddle(CommonUtilsDBadmin common_utils, Vector thesaurusVector, String ThesaurusName, StringObject DeleteThesaurusResultMessage, boolean DeleteThesaurusSucceded) {
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
        XMLMiddleStr += "<DeleteThesaurusResult>";
        // write the ThesaurusName given
        XMLMiddleStr += "<ThesaurusName>" + common_utils.ReplaceSpecialCharacters(ThesaurusName) + "</ThesaurusName>";
        XMLMiddleStr += "<DeleteThesaurusResult>" + DeleteThesaurusSucceded + "</DeleteThesaurusResult>";
        XMLMiddleStr += "<DeleteThesaurusResultMessage>" + DeleteThesaurusResultMessage.getValue() + "</DeleteThesaurusResultMessage>";
        XMLMiddleStr += "</DeleteThesaurusResult>";
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