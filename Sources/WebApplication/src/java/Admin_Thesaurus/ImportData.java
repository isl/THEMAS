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
import Utils.UpDownFiles;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.SessionListener;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author tzortzak
 */
public class ImportData extends ApplicationBasicServlet {

    //final String LogFilesFolderName = "LogFiles";
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

        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }

            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBImportData dbImport = new DBImportData();
            DBMergeThesauri dbMerge = new DBMergeThesauri();

            Vector<String> thesauriNames = new Vector<String>();

            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);
            StringObject resultObj = new StringObject("");

            String initiallySelectedThesaurus = SessionUserInfo.selectedThesaurus;

            //Parameters
            String xmlFilePath = request.getParameter("importXMLfilename");

            //String importSchemaName    = request.getParameter("schematype");
            String importSchemaName = ConstantParameters.xmlschematype_THEMAS;
            String importThesaurusName = request.getParameter("Import_Thesaurus_NewName_NAME");
            String importMethodChoice = request.getParameter("ImportThesaurusMode");//thesaurusImport or bulkImport
            String importHierarchyName = u.getDecodedParameterValue(request.getParameter("Import_Thesaurus_HierarchyName"));
            String pathToErrorsXML = context.getRealPath("/translations/Consistencies_Error_Codes.xml");
            String language = context.getInitParameter("LocaleLanguage");
            String country = context.getInitParameter("LocaleCountry");
            String WebAppUsersFileName = request.getSession().getServletContext().getRealPath("/" + UsersClass.WebAppUsersXMLFilePath);

            String logPath = context.getRealPath("/" + ConstantParameters.LogFilesFolderName);
            String logFileNamePath = logPath;
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
            Locale targetLocale = new Locale(language, country);

            if ((importMethodChoice.equals("thesaurusImport") && (importThesaurusName != null))
                    || (importMethodChoice.equals("bulkImport") && importHierarchyName != null)) {
                UpDownFiles fup = new UpDownFiles();
                String[] formData = new String[10];
                FileItem[] dom = fup.prepareToUpBinary(request, formData);
                //Hashtable initParams = UpDownFiles.uploadParams;

                if (dom[0] != null) {

                    String filename = xmlFilePath;
                    ///String caption = (String) initParams.get("caption");

                    filename = filename.substring(filename.lastIndexOf("\\") + 1);

                    String fileType = filename.substring(filename.lastIndexOf(".") + 1);
                    String userFileName = filename.substring(0, filename.lastIndexOf("."));

                    filename = userFileName + "(" + getDate() + " " + getTime() + ")." + fileType;

                    String fullPath = getServletContext().getRealPath("/Uploads") + "/" + filename;
                    xmlFilePath = fullPath;
                    if (fup.writeBinary(dom[0], fullPath)) {
                        //mode = 1;
                    } else {
                        //mode = -1;
                    }
                } else {
                    //mode = -1;
                }
            }

            QClass Q = new QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();

            //open connection and start transaction
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

            if (importMethodChoice.equals("thesaurusImport")) {

                //Format Name Of import Thesaurus
                importThesaurusName = importThesaurusName.trim();
                importThesaurusName = importThesaurusName.replaceAll(" ", "_");
                importThesaurusName = importThesaurusName.toUpperCase();

                if (thesauriNames.contains(importThesaurusName)) {
                    /*
                     *
                     *
                     * HARDCODED GREEKS NEED TRANSALTION
                     *
                     *
                     */
                    String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
                    StringObject resultMessageObj = new StringObject();
                    StringObject resultMessageObj_2 = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    errorArgs.add(importThesaurusName);
                    dbGen.Translate(resultMessageObj, "root/ImportData/importThesaurusNameFailure", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();

                    resultObj.setValue(resultMessageObj.getValue());
                    //resultObj.setValue("Ο θησαυρός '" + importThesaurusName + "' έχει ήδη ορισθεί στην βάση. Παρακαλώ επιλέξτε διαφορετικό όνομα θησαυρού");

                    Vector<String> allHierarchies = new Vector<String>();
                    Vector<String> allGuideTerms = new Vector<String>();
                    dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);

                    //end query and close connection
                    Q.free_all_sets();
                    Q.TEST_end_query();
                    //Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                    StringBuffer xml = new StringBuffer();
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
                    xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
                    dbGen.Translate(resultMessageObj_2, "root/ImportData/ImportFunctionFailure", null, pathToMessagesXML);
                    xml.append(getXMLMiddle(thesauriNames, resultMessageObj_2.getValue() + resultObj.getValue(), importMethodChoice));
                    xml.append(u.getXMLUserInfo(SessionUserInfo));
                    xml.append(u.getXMLEnd());
                    u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

                    // ---------------------- UNLOCK SYSTEM ----------------------
                    dbAdminUtils.UnlockSystemForAdministrativeJobs();
                    return;
                }
            } else if (importMethodChoice.equals("bulkImport")) {
                importThesaurusName = SessionUserInfo.selectedThesaurus;
                if (thesauriNames.contains(importThesaurusName) == false) {

                    String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
                    StringObject resultMessageObj = new StringObject();
                    StringObject resultMessageObj_2 = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    errorArgs.add(importThesaurusName);
                    dbGen.Translate(resultMessageObj, "root/ImportData/ThesaurusDoesNotExist", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();
                    resultObj.setValue(resultMessageObj.getValue());

                    //resultObj.setValue("Ο θησαυρός '" + importThesaurusName + "' δεν βρέθηκε στην βάση. Παρακαλώ ελέγξτε αν ο θησαυρός αυτός εξακολουθεί να υπάρχει.");
                    Vector<String> allHierarchies = new Vector<String>();
                    Vector<String> allGuideTerms = new Vector<String>();
                    dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);

                    //end query and close connection
                    Q.free_all_sets();
                    Q.TEST_end_query();
                    //Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, null, sis_session, null, false);

                    StringBuffer xml = new StringBuffer();
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
                    xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));

                    /*
                     *
                     *HARDCODED GREEKS
                     *
                     */
                    dbGen.Translate(resultMessageObj_2, "root/ImportData/InsertionFailure", null, pathToMessagesXML);
                    xml.append(getXMLMiddle(thesauriNames, resultMessageObj_2.getValue() + resultObj.getValue(), importMethodChoice));
                    //xml.append(getXMLMiddle(thesauriNames, "Αποτυχία λειτουργίας εισαγωγής δεδομένων. " + resultObj.getValue(),importMethodChoice));
                    xml.append(u.getXMLUserInfo(SessionUserInfo));
                    xml.append(u.getXMLEnd());
                    u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

                    // ---------------------- UNLOCK SYSTEM ----------------------
                    dbAdminUtils.UnlockSystemForAdministrativeJobs();
                    return;
                }
            }

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            Utils.StaticClass.closeDb();

            StringObject DBbackupFileNameCreated = new StringObject("");

            long startTime = Utilities.startTimer();
            String time = Utilities.GetNow();
            String Filename = "Import_Thesaurus_" + importThesaurusName + "_" + time;
            logFileNamePath += "/" + Filename + ".xml";

            try {
                OutputStream fout = new FileOutputStream(logFileNamePath);
                OutputStream bout = new BufferedOutputStream(fout);
                logFileWriter = new OutputStreamWriter(bout, "UTF-8");
                logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"
                //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../" + webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
                logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
                logFileWriter.append("<title>Αναφορά εισαγωγής δεδομένων στον θησαυρό " + importThesaurusName + " " + time + "</title>\r\n"
                        + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>\r\n");
                //logFileWriter.append("<!--"+time + " LogFile λειτουργίας εισαγωγής δεδομένων στον θησαυρό: " + importThesaurusName +".-->\r\n");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile λειτουργίας εισαγωγής δεδομένων στον θησαυρό: " + importThesaurusName + ".");

            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }

            if (importMethodChoice.equals("thesaurusImport")) {

                if (dbImport.thesaurusImportActions(SessionUserInfo, common_utils,
                        config, targetLocale, pathToErrorsXML,
                        xmlFilePath, importSchemaName, importThesaurusName, "backup_before_import_data_to_thes_" + importThesaurusName, DBbackupFileNameCreated, resultObj, logFileWriter) == false) {
                    abortActions(request, sessionInstance, context, targetLocale, common_utils, initiallySelectedThesaurus, importThesaurusName, DBbackupFileNameCreated, resultObj, out);
                    return;
                }
            } else if (importMethodChoice.equals("bulkImport")) {
                /*
                 //open connection and start Transaction
                 if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
                 {
                 Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                 return;
                 }
                 */
                if (dbImport.bulkImportActions(sessionInstance, context, common_utils, config, targetLocale, pathToErrorsXML, xmlFilePath, importThesaurusName, importHierarchyName, "backup_before_import_data_to_thes_" + importThesaurusName, DBbackupFileNameCreated, resultObj, logFileWriter) == false) {
                    abortActions(request, sessionInstance, context, targetLocale, common_utils, initiallySelectedThesaurus, importThesaurusName, DBbackupFileNameCreated, resultObj, out);
                    return;
                }

            }

            commitActions(request, WebAppUsersFileName, sessionInstance, context, targetLocale, importThesaurusName, out, Filename.concat(".html"));

            logFileWriter.append("\r\n<creationInfo>Η διαδικασία εισαγωγής δεδομένων στον θησαυρό " + importThesaurusName + " από το αρχείο " + xmlFilePath + " ολοκληρώθηκε με επιτυχία σε χρόνο : " + ((Utilities.stopTimer(startTime)) / 60) + " λεπτά.</creationInfo>\r\n");

            if (logFileWriter != null) {
                logFileWriter.append("</page>");
                logFileWriter.flush();
                logFileWriter.close();
            }

            //Now XSL should be found and java xsl transformation should be performed
            String XSL = context.getRealPath("/" + webAppSaveResults_Folder) + "/ImportCopyMergeThesaurus_Report.xsl";

            u.XmlFileTransform(logFileNamePath, XSL, logPath + "/" + Filename.concat(".html"));

        } catch (Exception e) {

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
            if (logFileWriter != null) {
                logFileWriter.append("</page>");
                logFileWriter.flush();
                logFileWriter.close();
            }
        } finally {
            out.flush();
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }
    /*
     protected Hashtable<String, Object> getFormParams(HttpServletRequest request) {
     Hashtable<String, Object> params = new Hashtable<String, Object>();
     Enumeration paramNames = request.getParameterNames();
     while (paramNames.hasMoreElements()) {
     String paramName = (String)paramNames.nextElement();
     //    Utils.StaticClass.webAppSystemOutPrintln("PARAMNAME="+paramName);
     String [] paramValues = request.getParameterValues(paramName);
     if (paramValues.length == 1) {
     String paramValue = paramValues[0];
     //    Utils.StaticClass.webAppSystemOutPrintln("PARAMVALUE="+paramValue);
     params.put(paramName, paramValue);
                
     } else {
     params.put(paramName, paramValues);
     for (int i=0;i<paramValues.length;i++) {
     //      Utils.StaticClass.webAppSystemOutPrintln("PARAMVALUES="+paramValues[i]);
     }
     }
     }
     return params;
     }
     */

    private static String getDate() {

        Calendar cal = new GregorianCalendar(Locale.getDefault());
        DecimalFormat myformat = new DecimalFormat("00");

        // Get the components of the date
        // int era = cal.get(Calendar.ERA);               // 0=BC, 1=AD
        int year = cal.get(Calendar.YEAR);             // 2002
        int month = cal.get(Calendar.MONTH) + 1;           // 0=Jan, 1=Feb, ...
        int day = cal.get(Calendar.DAY_OF_MONTH);      // 1...

        //   int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday
        return new String(myformat.format(day) + "-" + myformat.format(month) + "-" + year);
    }

    /**
     * Time method
     *
     * @return Current time as <CODE>String</CODE> in hh:mm:ss format
     */
    private static String getTime() {
        Calendar cal = new GregorianCalendar(Locale.getDefault());

        // Get the components of the time
        //    int hour12 = cal.get(Calendar.HOUR);            // 0..11
        // Create the DecimalFormat object only one time.
        DecimalFormat myformat = new DecimalFormat("00");

        int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
        int min = cal.get(Calendar.MINUTE);             // 0..59
        //   int sec = cal.get(Calendar.SECOND);        // 0..59
        return new String(myformat.format(hour24) + myformat.format(min));
//        return new String(myformat.format(hour24)+":"+myformat.format(min)+":"+myformat.format(sec));
    }

    public void commitActions(HttpServletRequest request, String WebAppUsersFileName,
            SessionWrapperClass sessionInstance, ServletContext context, Locale targetLocale,
            String importThesaurusName, PrintWriter out, String reportFile) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();
        Vector<String> thesauriNames = new Vector<String>();

        QClass Q = new QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        //open connection and start transaction
        if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
            return;
        }
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String importMethodChoice = request.getParameter("ImportThesaurusMode");
        String resultFileTagName = "importReportFile";
        if (importMethodChoice.compareTo("bulkImport") == 0) {
            resultFileTagName = "bulkImportReportFile";
        }
        wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, importThesaurusName, SessionUserInfo.userGroup);

        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_query();

        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, false);

        // inform current user's rights with the new thesaurus 
        wtmsUsers.AddNewThesaurusForCurrentTMSUser(WebAppUsersFileName, sessionInstance, importThesaurusName);

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        xml.append("<" + resultFileTagName + ">");
        xml.append(reportFile);
        xml.append("</" + resultFileTagName + ">");

        /*
         *
         *HARDCODED GREEKS
         *
         */
        String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
        StringObject resultMessageObj = new StringObject();
        StringObject resultMessageObj_2 = new StringObject();
        Vector<String> errorArgs = new Vector<String>();

        dbGen.Translate(resultMessageObj, "root/ImportData/SuccessfulInsertion", null, pathToMessagesXML);

        xml.append(getXMLMiddle(thesauriNames, resultMessageObj.getValue() + importThesaurusName + ".", importMethodChoice));
        //xml.append(getXMLMiddle(thesauriNames, "Η λειτουργία εισαγωγής δεδομένων ολοκληρώθηκε με επιτυχία. Τρέχοντας θησαυρός τέθηκε ο: " + importThesaurusName + ".", importMethodChoice));

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public void abortActions(HttpServletRequest request, SessionWrapperClass sessionInstance, ServletContext context, Locale targetLocale, CommonUtilsDBadmin common_utils, String initiallySelectedThesaurus, String mergedThesaurusName, StringObject DBbackupFileNameCreated, StringObject resultObj, PrintWriter out) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ABORT IMPORT");
        //abort transaction and close connection
        //Q.free_all_sets();
        //Q.TEST_abort_transaction();
        //dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> thesauriNames = new Vector<String>();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();
        String importMethodChoice = request.getParameter("ImportThesaurusMode");
        StringObject result = new StringObject("");

        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, initiallySelectedThesaurus, SessionUserInfo.userGroup);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + DBbackupFileNameCreated.getValue());

        boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);
        thesauriNames.remove(mergedThesaurusName);

        QClass Q = new QClass();
        TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        if (restored) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
            dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Did not manage to restore : " + DBbackupFileNameCreated.getValue());
        }

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        /*
         *
         *HARDCODED GREEKS
         *
         */
        String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
        StringObject resultMessageObj = new StringObject();
        StringObject resultMessageObj_2 = new StringObject();
        Vector<String> errorArgs = new Vector<String>();

        dbGen.Translate(resultMessageObj, "root/abortActions/InsertionFailure", null, pathToMessagesXML);

        xml.append(getXMLMiddle(thesauriNames, resultMessageObj.getValue() + resultObj.getValue(), importMethodChoice));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public String getXMLMiddle(Vector<String> thesaurusVector, String importThesaurusMessage, String thesaurusOrBulkMode) {
        // get the active sessions
        String resultTagName = "importThesaurusMessage";
        if (thesaurusOrBulkMode.compareTo("bulkImport") == 0) {
            resultTagName = "bulkImportThesaurusMessage";
        }

        int OtherActiveSessionsNO = SessionListener.activesessionsNO - 1;

        String XMLMiddleStr = "<content_Admin_Thesaurus>";

        XMLMiddleStr += "<CurrentShownDIV>ImportExport_Data_DIV</CurrentShownDIV>";

        XMLMiddleStr += "<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>";
        // write the existing Thesaurus in DB
        int thesaurusVectorCount = thesaurusVector.size();
        XMLMiddleStr += "<existingThesaurus>";
        for (int i = 0; i < thesaurusVectorCount; i++) {
            XMLMiddleStr += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
        }
        XMLMiddleStr += "</existingThesaurus>";

        XMLMiddleStr += "<" + resultTagName + ">" + importThesaurusMessage + "</" + resultTagName + ">";
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
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
