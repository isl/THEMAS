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
import Utils.NodeInfoStringContainer;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBCreate_Modify_Term;
import DB_Classes.DBConnect_Term;
import Users.DBFilters;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;

import Utils.SortItem;
import Utils.ConsistensyCheck;
import Utils.ConstantParameters;

import XMLHandling.ParseFileData;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Locale;
import java.io.OutputStreamWriter;
import neo4j_sisapi.tmsapi.TMSAPIClass;

import neo4j_sisapi.*;

import javax.xml.xpath.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author tzortzak
 */
public class DBImportData {

    private final boolean PRINTXMLMESSAGES = false;

    public DBImportData() {
    }

    public boolean bulkImportActions(SessionWrapperClass sessionInstance, ServletContext context, CommonUtilsDBadmin common_utils,
            ConfigDBadmin config, Locale targetLocale, String pathToErrorsXML, String xmlFilePath, String importThesaurusName, String importHierarchyName, String backUpDescription, StringObject DBbackupFileNameCreated, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        UsersClass wtmsusers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();


        String importThesaurusNameDBformatted = importThesaurusName;

        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        wtmsusers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, importThesaurusNameDBformatted, SessionUserInfo.userGroup);

        //read terms from XML and import under targetHierarchy
        return importTermsUnderHierarchy(sessionInstance, importHierarchyName, xmlFilePath, pathToErrorsXML, logFileWriter, resultObj);

    }

    public boolean importTermsUnderHierarchy(SessionWrapperClass sessionInstance, String targetHierarchy, String xmlFilePath, String pathToErrorsXML, OutputStreamWriter logFileWriter, StringObject resultObj) throws IOException {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading terms from file: " + xmlFilePath + ".");
        Vector<String> parsedTermNames = new Vector<String>();

        try {

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(xmlFilePath));

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList termNames = (NodeList) xpath.evaluate("//data/terms/term/descriptor", document, XPathConstants.NODESET);

            int howManyTerms = termNames.getLength();
            for (int i = 0; i < howManyTerms; i++) {

                String targetTerm = termNames.item(i).getTextContent();
                targetTerm = readXMLTag(targetTerm);

                if (parsedTermNames.contains(targetTerm) == false) {
                    parsedTermNames.add(targetTerm);
                }
            }

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading terms. Found " + parsedTermNames.size() + " terms.");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }



        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        
        QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();

        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        boolean returnVal = true;
        //open connection and start Transaction
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ thesaurusImportActions ");
            return false;
        }
        try{
            



            String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            Vector<String> btOfTerms = new Vector<String>();
            btOfTerms.add(targetHierarchy);
            
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            StringObject resultMessageObj_2 = new StringObject();
            Vector<String> errorArgs = new Vector<String>();


            Q.reset_name_scope();
            if (Q.set_current_node(new StringObject(prefixTerm.concat(targetHierarchy))) == QClass.APIFail) {
                
                errorArgs.add(Utilities.escapeXML(targetHierarchy));
                dbGen.Translate(resultMessageObj, "root/importTermsUnderHierarchy/FailureFindHierarchy", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                
                resultObj.setValue(resultMessageObj.getValue());
                //resultObj.setValue("Hierarchy  '" + Utilities.escapeXML(targetHierarchy) + "' which was choosen for insertion of terms does not exist in database. Please choose a different name of Hierarchy.");
                
                returnVal = false;
                return false;
            }


            resultObj.setValue("");

            for (int i = 0; i < parsedTermNames.size(); i++) {                                                
                String targetUITerm = parsedTermNames.get(i);
                //Utils.StaticClass.webAppSystemOutPrintln(i+". Adding Term: "+ targetUITerm);
                StringObject targetTermObj = new StringObject(prefixTerm.concat(targetUITerm));
                Q.free_all_sets();
                Q.reset_name_scope();
                
                errorArgs.add(targetUITerm);
                dbGen.Translate(resultMessageObj_2, "root/importTermsUnderHierarchy/targetUITermAlreadyExists", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                
                if (Q.set_current_node(targetTermObj) != QClass.APIFail) {
                    logFileWriter.append("<targetTerm>");
                    logFileWriter.append("<name>" + Utilities.escapeXML(targetUITerm) + "</name>");
                    logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(targetHierarchy) + "</errorValue>");
                    logFileWriter.append("<reason>" + resultMessageObj_2.getValue() + resultMessageObj.getValue() + "').</reason>");
                    //logFileWriter.append("<reason>The Term '" + targetUITerm + "' already exists in database and it did not change (to be essentially NT of Term: '" + Utilities.escapeXML(targetHierarchy) + "'.</reason>");
                    logFileWriter.append("</targetTerm>");
                    //Utils.StaticClass.webAppSystemOutPrintln("\tTerm: "+ targetUITerm + " already found in thes " + SessionUserInfo.selectedThesaurus);
                    continue;
                } else {
                    creation_modificationOfTerm.createNewTerm(SessionUserInfo, targetUITerm, btOfTerms, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, false, true, logFileWriter, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);
                    if (resultObj.getValue().length() > 0) {
                        returnVal = false;
                        return false;
                    }
                }

            }
            Q.free_all_sets();

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading terms from file: " + xmlFilePath + ".");

        }
        finally{
            if(returnVal){
                Q.TEST_end_transaction();
                Utils.StaticClass.closeDb();
            }
            else{
                Q.TEST_abort_transaction();
            }
            return returnVal;
        }
        
    }

    public void checkLengths(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName,
            Vector<String> guideTerms, Hashtable<String, String> XMLsources,
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
            Hashtable<String, Vector<String>> hierarchyFacets,
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            OutputStreamWriter logFileWriter) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass webappusers = new UsersClass();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        /*
        //open connection and start Transaction
        if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData writeThesaurusData()");
            return;
        }

        */
        Vector<String> errorArgs = new Vector<String>();
        Vector<String> removeTerms = new Vector<String>();
        Hashtable<String, String> AllLengthRenames = new Hashtable<String, String>();
        int unlabeledCounter = 1;
        Vector<String> allTermsVec = new Vector<String>();
        allTermsVec.addAll(termsInfo.keySet());
        /*
        for (int k = 0; k < allTermsVec.size(); k++) {
            String targetTerm = allTermsVec.get(k);
            try {
                byte[] byteArray = targetTerm.getBytes("UTF-8");

                int maxTermChars = dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {
                    removeTerms.add(targetTerm);
                    String newName = XMLHandling.ParseFileData.UnlabeledPrefix + (unlabeledCounter++);
                    AllLengthRenames.put(targetTerm, newName);
                    StringObject warningMsg = new StringObject();
                    errorArgs.clear();
                    errorArgs.add(targetTerm);
                    errorArgs.add(newName);
                    errorArgs.add("" + maxTermChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                    try {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                        logFileWriter.append("<errorType>" + "termname" + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(newName) + "</errorValue>");
                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }



                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
        }
        */
        /*
        for (int k = 0; k < topTerms.size(); k++) {
        String targetTerm = topTerms.get(k);
        try {
        byte[] byteArray = targetTerm.getBytes("UTF-8");

        int maxTermChars = dbtr.getMaxBytesForDescriptor(refSessionUserInfo.selectedThesaurus, Q, sis_session);
        if (byteArray.length > maxTermChars) {
        if (removeTerms.contains(targetTerm) == false) {
        removeTerms.add(targetTerm);
        }
        StringObject warningMsg = new StringObject();
        errorArgs.clear();
        errorArgs.add(targetTerm);
        errorArgs.add("" + maxTermChars);
        errorArgs.add("" + byteArray.length);
        dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
        try {
        logFileWriter.append("\r\n<targetTerm>");
        logFileWriter.append("<name>" + targetTerm + "</name>");
        logFileWriter.append("<errorType>" + "termname" + "</errorType>");
        logFileWriter.append("<errorValue>" + targetTerm + "</errorValue>");
        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
        logFileWriter.append("</targetTerm>\r\n");
        } catch (IOException ex) {
        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
        Utils.StaticClass.handleException(ex);
        }



        }
        } catch (UnsupportedEncodingException ex) {
        Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
        Utils.StaticClass.handleException(ex);
        }
        }


         */

        if (removeTerms.size() > 0) {
            for (int k = 0; k < removeTerms.size(); k++) {
                String termToRemove = removeTerms.get(k);

                String newName = AllLengthRenames.get(termToRemove);
                if (termsInfo.containsKey(termToRemove)) {
                    NodeInfoStringContainer targetTermInfo = termsInfo.get(termToRemove);
                    termsInfo.remove(termToRemove);
                    termsInfo.put(newName, targetTermInfo);
                }

                Enumeration<String> termEnum = termsInfo.keys();
                while (termEnum.hasMoreElements()) {
                    String checkTerm = termEnum.nextElement();

                    Vector<String> bts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.bt_kwd);
                    Vector<String> nts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.nt_kwd);
                    Vector<String> rts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.rt_kwd);

                    if (bts.contains(termToRemove)) {
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.bt_kwd).remove(termToRemove);
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.bt_kwd).add(newName);
                    }
                    if (nts.contains(termToRemove)) {
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.nt_kwd).remove(termToRemove);
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.nt_kwd).add(newName);
                    }
                    if (rts.contains(termToRemove)) {
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.rt_kwd).remove(termToRemove);
                        termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.rt_kwd).add(newName);
                    }
                }

                /*
                if (topTerms.contains(termToRemove)) {
                topTerms.remove(termToRemove);
                }

                if (descriptorRts.containsKey(termToRemove)) {
                descriptorRts.remove(termToRemove);
                }

                if (descriptorUfs.containsKey(termToRemove)) {
                descriptorUfs.remove(termToRemove);
                }

                Enumeration<String> rtsEnum = descriptorRts.keys();
                while (rtsEnum.hasMoreElements()) {
                String targetTerm = rtsEnum.nextElement();
                Vector<String> rts = descriptorRts.get(targetTerm);
                if (rts.contains(termToRemove)) {
                descriptorRts.get(targetTerm).remove(termToRemove);
                }
                }
                 */
                /*
                for (int m = 0; m < allLevelsOfImportThes.size(); m++) {
                if (allLevelsOfImportThes.get(m).containsKey(termToRemove)) {
                allLevelsOfImportThes.get(m).remove(termToRemove);
                }

                Enumeration<String> levelTermsEnum = allLevelsOfImportThes.get(m).keys();
                while (levelTermsEnum.hasMoreElements()) {
                String targetTerm = levelTermsEnum.nextElement();
                Vector<String> bts = allLevelsOfImportThes.get(m).get(targetTerm);
                if (bts.contains(termToRemove)) {
                allLevelsOfImportThes.get(m).get(targetTerm).remove(termToRemove);
                }
                if (bts.size() == 1) {

                allLevelsOfImportThes.get(m).get(targetTerm).add(Parameters.UnclassifiedTermsLogicalname);
                }
                }
                }
                 */
                if (guideTerms.contains(termToRemove)) {

                    guideTerms.remove(termToRemove);
                    guideTerms.add(newName);
                }

                if (XMLguideTermsRelations.containsKey(termToRemove)) {
                    Vector<SortItem> existingRelations = XMLguideTermsRelations.get(termToRemove);
                    XMLguideTermsRelations.remove(termToRemove);
                    XMLguideTermsRelations.put(newName, existingRelations);
                }


                Enumeration<String> guideTermsEnum = XMLguideTermsRelations.keys();
                while (guideTermsEnum.hasMoreElements()) {
                    String targetTerm = guideTermsEnum.nextElement();
                    Vector<SortItem> gts = XMLguideTermsRelations.get(targetTerm);
                    for (int m = 0; m < gts.size(); m++) {
                        SortItem item = gts.get(m);
                        if (item.log_name.equals(termToRemove)) {
                            XMLguideTermsRelations.get(targetTerm).get(m).log_name = newName;
                        }
                    }
                    /*
                    Vector<SortItem> gtsToRemove = new Vector<SortItem>();
                    for (int m = 0; m < gts.size(); m++) {
                    SortItem item = gts.get(m);
                    if (item.log_name.equals(termToRemove)) {
                    gtsToRemove.add(item);
                    }
                    }

                    XMLguideTermsRelations.get(targetTerm).removeAll(gtsToRemove);
                     *
                     */

                }


            }
        }

        Enumeration<String> termsInfoEnumForSources = termsInfo.keys();
        while (termsInfoEnumForSources.hasMoreElements()) {
            String targetTerm = termsInfoEnumForSources.nextElement();
            Vector<String> primarySources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd);
            Vector<String> translationSources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd);
            if (primarySources != null) {
                for (int k = 0; k < primarySources.size(); k++) {
                    String checkSource = primarySources.get(k);
                    if (XMLsources.containsKey(checkSource) == false) {
                        XMLsources.put(checkSource, "");
                    }
                }

            }

            if (translationSources != null) {
                for (int k = 0; k < translationSources.size(); k++) {
                    String checkSource = translationSources.get(k);
                    if (XMLsources.containsKey(checkSource) == false) {
                        XMLsources.put(checkSource, "");
                    }
                }

            }

        }

        Vector<String> sourcesToRemove = new Vector<String>();
        Hashtable<String, String> sourcesToRename = new Hashtable<String, String>();
        Vector<String> allSources = new Vector<String>(XMLsources.keySet());
        /*
        for (int k = 0; k < allSources.size(); k++) {
            String targetSource = allSources.get(k);
            String newName = "";
            try {
                byte[] byteArray = targetSource.getBytes("UTF-8");

                int maxTermChars = dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {

                    if (sourcesToRemove.contains(targetSource) == false) {
                        sourcesToRemove.add(targetSource);
                    }
                    newName = XMLHandling.ParseFileData.UnlabeledPrefix + (unlabeledCounter++);
                    String oldSn = XMLsources.get(targetSource);
                    sourcesToRename.put(targetSource, newName);
                    XMLsources.remove(targetSource);
                    XMLsources.put(newName, oldSn);

                    StringObject warningMsg = new StringObject();
                    errorArgs.clear();
                    errorArgs.add(targetSource);
                    errorArgs.add(newName);
                    errorArgs.add("" + maxTermChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(warningMsg, "root/EditSource/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                    try {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(targetSource) + "</name>");
                        logFileWriter.append("<errorType>" + "sourcename" + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(newName) + "</errorValue>");
                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }



                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }

            //check source_note
            String srcname = "";
            String sourceNoteStr = "";
            if (sourcesToRemove.contains(targetSource) == false) {
                srcname = targetSource;
                sourceNoteStr = XMLsources.get(targetSource);
            } else {
                srcname = newName;
                sourceNoteStr = XMLsources.get(newName);
            }
            try {
                byte[] byteArray = sourceNoteStr.getBytes("UTF-8");

                int maxTermChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {

                    XMLsources.put(srcname, "");

                    StringObject warningMsg = new StringObject();
                    errorArgs.clear();
                    errorArgs.add(srcname);
                    errorArgs.add("" + maxTermChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(warningMsg, "root/EditSource/Creation/LongSourceNoteErrorResolve", errorArgs, pathToMessagesXML);
                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                    try {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(srcname) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.source_note_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(sourceNoteStr) + "</errorValue>");
                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }



                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }


        }
*/

        if (sourcesToRemove.size() > 0) {
            for (int k = 0; k < sourcesToRemove.size(); k++) {
                String sourceForRemoval = sourcesToRemove.get(k);
                String newName = sourcesToRename.get(sourceForRemoval);
                Enumeration<String> termsInfoEnum = termsInfo.keys();
                while (termsInfoEnum.hasMoreElements()) {
                    String targetTerm = termsInfoEnum.nextElement();
                    Vector<String> primarySources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd);
                    Vector<String> translationSources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd);
                    if (primarySources != null && primarySources.contains(sourceForRemoval)) {
                        termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd).remove(sourceForRemoval);
                        termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd).add(newName);
                    }
                    if (translationSources != null && translationSources.contains(sourceForRemoval)) {
                        termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd).remove(sourceForRemoval);
                        termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd).add(newName);
                    }
                }
            }
        }


        /*
        Vector<String> gtsToRemove = new Vector<String>();

        Enumeration<String> guideTermsEnum = XMLguideTermsRelations.keys();
        while (guideTermsEnum.hasMoreElements()) {
            String targetTerm = guideTermsEnum.nextElement();
            Vector<SortItem> gts = XMLguideTermsRelations.get(targetTerm);

            
            for (int m = 0; m < gts.size(); m++) {
                SortItem item = gts.get(m);
                String gtTerm = item.linkClass;
                try {
                    byte[] byteArray = gtTerm.getBytes("UTF-8");

                    int maxTermChars = dbtr.getMaxBytesForGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxTermChars) {

                        //resolve error
                        item.linkClass = "";
                        gtsToRemove.add(gtTerm);
                        XMLguideTermsRelations.get(targetTerm).set(m, item);
                        if (guideTerms.contains(gtTerm)) {
                            guideTerms.remove(gtTerm);
                        }

                        StringObject warningMsg = new StringObject();
                        errorArgs.clear();
                        errorArgs.add(gtTerm);
                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(warningMsg, "root/EditGuideTerms/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                        try {
                            logFileWriter.append("\r\n<targetTerm>");
                            logFileWriter.append("<name>" + Utilities.escapeXML(gtTerm) + "</name>");
                            logFileWriter.append("<errorType>" + ConstantParameters.guide_term_kwd + "</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(gtTerm) + "</errorValue>");
                            logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                        } catch (IOException ex) {
                            Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                            Utils.StaticClass.handleException(ex);
                        }



                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }

            }
            

        }
*/
        //dbGen.CloseDBConnection(Q, null, sis_session, null, false);

    }

    public boolean writeThesaurusData(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Vector<String> xmlFacets, Vector<String> guideTerms, Hashtable<String, String> XMLsources,
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
            Hashtable<String, Vector<String>> hierarchyFacets,
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            Vector<String> userSelectedTranslationWords,
            Vector<String> userSelectedTranslationIdentifiers,
            Hashtable<String, String> userSelections,
            Vector<String> topTerms, Hashtable<String, Vector<String>> descriptorRts,
            Hashtable<String, Vector<String>> descriptorUfs,
            Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes,
            String importThesaurusName,
            String pathToErrorsXML, Locale targetLocale,
            StringObject resultObj, OutputStreamWriter logFileWriter) {
        

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        UsersClass webappusers = new UsersClass();
        DBGeneral dbGen = new DBGeneral();
        String pathToMessagesXML = Utilities.getMessagesXml();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        //open connection and start Transaction
        //if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
          //  Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData writeThesaurusData()");
            //return false;
        //}








        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of new thesaurus creation: " + importThesaurusName + ".");

        Q.reset_name_scope();
        if (readAndSyncronizeTranslationCategories(importThesaurusName, resultObj, Q, TA, sis_session, tms_session,
                userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections) == false) {
            return false;
        }


        Q.reset_name_scope();
        // Step8 Get and put default Status per user for Unclassified terms
        if (termsInfo.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            specifyOrphansStatus(SessionUserInfo, Q, TA, sis_session, tms_session, resultObj);
        } else {
            if (termsInfo.get(Parameters.UnclassifiedTermsLogicalname).descriptorInfo.get(ConstantParameters.status_kwd).isEmpty()) {
                specifyOrphansStatus(SessionUserInfo, Q, TA, sis_session, tms_session, resultObj);
            }
        }

        Q.reset_name_scope();
        //common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        if (CreateSources(SessionUserInfo.selectedThesaurus, common_utils, importThesaurusName,
                Q, TA, sis_session, tms_session, XMLsources, resultObj, logFileWriter) == false) {
            return false;
        }
        
        // Step9 Create Facets specified by XML
        if (dbMerge.CreateFacets(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, xmlFacets, resultObj) == false) {
            return false;
        }

        String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, importThesaurusName);

        // Step10 Create Hierarchies specified by XML
        if (dbMerge.CreateHierarchies(SessionUserInfo, Q, TA, sis_session, tms_session,
                importThesaurusName, defaultFacet, targetLocale, resultObj, logFileWriter, hierarchyFacets) == false) {
            return false;
        }

        // Step11 Create Hierarchies specified by topterms of Step2
        if (importMoreHierarchiesFromTopTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, topTerms, targetLocale, logFileWriter, resultObj) == false) {
            return false;
        }

        // Step12 Create Terms
        if (importTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                pathToErrorsXML, importThesaurusName, termsInfo, resultObj, allLevelsOfImportThes,
                descriptorRts, descriptorUfs, logFileWriter) == false) {
            return false;
        }

        //Step 13 Guide Terms Addition Patch
        if (dbMerge.CreateGuideTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, guideTerms, XMLguideTermsRelations, importThesaurusName, resultObj) == false) {
            return false;
        }

        return true;
    }

    public boolean thesaurusMergeActions(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, ConfigDBadmin config,
            
            String pathToErrorsXML, String thesaurusName1, String thesaurusName2, String targetThesaurusName,
            Locale targetLocale, StringObject resultObj,
            StringObject CopyThesaurusResultMessage, StringBuffer xml,
            String logFileNamePath, String pathToSaveScriptingAndLocale,
            long startTime, PrintWriter out) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        StringObject trObj = new StringObject("");
        
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass webappusers = new UsersClass();

        String initiallySelectedThesaurus = refSessionUserInfo.selectedThesaurus;


        Vector<String> thesauriNames = new Vector<String>();

        OutputStreamWriter logFileWriter = null;


        String time = Utilities.GetNow();

        String logFileNamePathCopy = logFileNamePath.replace("\\","/");
        String Filename = logFileNamePathCopy.substring(logFileNamePathCopy.lastIndexOf('/'), logFileNamePathCopy.length() - ".xml".length());

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, thesaurusName1);

        // <editor-fold defaultstate="collapsed" desc="Xml Initialization">
        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);

            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            try {
                logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"
            } catch (IOException ex) {
                Logger.getLogger(DBImportData.class.getName()).log(Level.SEVERE, null, ex);
                Utils.StaticClass.handleException(ex);
            }
            //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../"+webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
            
            dbGen.Translate(trObj, "root/MergeThesauri/ReportTitle", Utilities.getMessagesXml(), new String[]{thesaurusName1,thesaurusName2,targetThesaurusName,time});
            logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
            logFileWriter.append("<title>"+trObj.getValue()+"</title>\r\n"
                    + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>\r\n");

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile of merge thesauri operation of thesauri: " + thesaurusName1 + " and " + thesaurusName2 + "  in new thesaurus: " + targetThesaurusName + ".");

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("FileNotFoundException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("UnsupportedEncodingException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("IOException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }

        // </editor-fold>

        StringObject DBbackupFileNameCreated = new StringObject("");

        // <editor-fold defaultstate="collapsed" desc="New Thesaurus Creation">
        /*************************Step1 CreateThesaurus***********************************/
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of new thesaurus: " + targetThesaurusName + ".");
        /*
         *boolean GivenThesaurusCanBeCreated = dbAdminUtils.GivenThesaurusCanBeCreated(config,common_utils, thesaurusVector,  NewThesaurusName,  NewThesaurusNameDBformatted,  CreateThesaurusResultMessage,  CreateThesaurusSucceded);
         *if (GivenThesaurusCanBeCreated == true) {
         *
         * AS in create thesaurus. Should have thesaurusVactor initialized
         */

        //Q.begin_query();
        //allHierarcies.addAll(dbGen.getDBAdminHierarchiesAndStatusesXML(sessionInstance, Q, sis_session));
        //Q.end_query();

        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(dbGen, config, common_utils, targetThesaurusName, targetThesaurusName, thesauriNames,
                CopyThesaurusResultMessage,
                "backup_before_merge_of_thes_" + thesaurusName1 + "_and_" + thesaurusName2 + "_to_" + targetThesaurusName,
                DBbackupFileNameCreated);


        if (CreateThesaurusSucceded == false) {
            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
            //xml.append(u.getDBAdminHierarchiesAndStatusesXML(allHierarcies, dbGen));
            xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, CopyThesaurusResultMessage, false));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            //out.println("DONE");
            if(out==null){
                Utils.StaticClass.webAppSystemOutPrintln(xml.toString());
            }
            else{
                u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Creation of new thesaurus: " + targetThesaurusName + " FAILED.");
            return false;
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Successful creation of new thesaurus: " + targetThesaurusName + ".");
        }


        /*************************Step2 Ensure Database is Running***********************/
        //ensure that sis_server is running before starting open connection procedure and merge transactions
        /*
        boolean serverStarted = common_utils.StartDatabase();
        if (serverStarted == false) {
            String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
            CopyThesaurusResultMessage.setValue(StartServerFailure + " " + common_utils.DatabaserBatFileDirectory + File.separator + common_utils.DatabaseBatFileName);
            common_utils.RestartDatabaseIfNeeded();
        }
        */
        // wait until server is finally started
        /*
        boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
        while (databaseIsRunning == false) {
            databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
        }*/

        // </editor-fold>
        
        

        /*************************Step3 begin transaction again***************************/
        QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();
        //open connection and start Query
        if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, thesaurusName1, false) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData thesaurusMergeActions()");
            return false;
        }


        //Structures to fill
        Vector<String> xmlFacets = new Vector<String>();
        Hashtable<String, Vector<String>> hierarchyFacets = new Hashtable<String, Vector<String>>();

        Vector<String> guideTerms = new Vector<String>();
        Hashtable<String, String> XMLsources = new Hashtable<String, String>();
        Hashtable<String, Vector<SortItem>> XMLguideTermsRelations = new Hashtable<String, Vector<SortItem>>();

        Vector<String> topTerms = new Vector<String>();
        Hashtable<String, Vector<String>> descriptorRts = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> descriptorUfs = new Hashtable<String, Vector<String>>();
        Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes = new Vector<Hashtable<String, Vector<String>>>();


        Hashtable<String, NodeInfoStringContainer> termsInfo = new Hashtable<String, NodeInfoStringContainer>();
        Vector<String> userSelectedTranslationWords = new Vector<String>();
        Vector<String> userSelectedTranslationIdentifiers = new Vector<String>();
        Hashtable<String, String> translationCategories = new Hashtable<String, String>();



        String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, targetThesaurusName);

        //read Translation Categories
        translationCategories = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, thesaurusName1, thesaurusName2, false, true);
        Enumeration<String> trEnum = translationCategories.keys();
        while (trEnum.hasMoreElements()) {
            String word = trEnum.nextElement();
            String identifier = translationCategories.get(word);

            userSelectedTranslationWords.add(word);
            userSelectedTranslationIdentifiers.add(identifier);
        }

        //read facets
        xmlFacets.addAll(dbMerge.ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, thesaurusName1, thesaurusName2));

        if (xmlFacets.contains(defaultFacet) == false) {
            xmlFacets.add(defaultFacet);
        }

        //read hierarchies
        hierarchyFacets = dbMerge.ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, thesaurusName1, thesaurusName2);
        if (hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            Vector<String> unclassifiedFacets = new Vector<String>();
            unclassifiedFacets.add(defaultFacet);
            hierarchyFacets.put(Parameters.UnclassifiedTermsLogicalname, unclassifiedFacets);
        }


        //readTermsInfo and guide terms
        dbMerge.ReadThesaurusTerms(refSessionUserInfo, Q,TA, sis_session, thesaurusName1, thesaurusName2,
                termsInfo, guideTerms, XMLguideTermsRelations);

        guideTerms.clear();

        //readGuideTerms

        guideTerms.addAll(dbGen.collectGuideLinks(thesaurusName1,Q, sis_session));
        Vector<String> guideTermsThes2 = dbGen.collectGuideLinks(thesaurusName2,Q, sis_session);
        if(guideTermsThes2!=null && guideTermsThes2.size()>0){
            for(int i=0; i < guideTermsThes2.size(); i++){
                if(guideTerms.contains(guideTermsThes2.get(i))==false){
                    guideTerms.add(guideTermsThes2.get(i));
                }
            }
        }
        //sources must exist - nothing to read or specifically create



        //processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);


        //end query// begin transaction and set thesaurus name to targetThesaurus

        //end query and close connection
        //Q.free_all_sets();
        //Q.TEST_end_query();
        //dbGen.CloseDBConnection(Q, null, sis_session, null, false);





        SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, targetThesaurusName);


        try {
            this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                    targetThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);

        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln("Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }
        // Step3 Read XML file in order to fill basic datastructures concerning terms
        // Step4 Process these data structures in order to define topterms and orphans
        //filling all structures passed as parameters except xmlFilePath parameter and then process data in order to classify terms in levels

        processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);


        try {
            if (writeThesaurusData(SessionUserInfo, common_utils,
                    Q, TA, sis_session, tms_session,
                    xmlFacets, guideTerms, XMLsources, XMLguideTermsRelations,
                    hierarchyFacets, termsInfo, userSelectedTranslationWords,
                    userSelectedTranslationIdentifiers, translationCategories,
                    topTerms, descriptorRts, descriptorUfs,
                    allLevelsOfImportThes, targetThesaurusName,
                    pathToErrorsXML, targetLocale, resultObj, logFileWriter) == false) {

                this.abortMergeActions(SessionUserInfo, Q, TA, sis_session, tms_session, targetLocale, common_utils,
                        initiallySelectedThesaurus, targetThesaurusName, DBbackupFileNameCreated, resultObj, out);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Merging thesauri operation FAILED.");
                return false;
            }

            //SUCESS
            commitMergeActions(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, targetLocale,
                    targetThesaurusName, out, Filename.concat(".html"));
            if (logFileWriter != null) {

                dbGen.Translate(trObj, "root/MergeThesauri/ReportSuccessMessage", Utilities.getMessagesXml(), new String[]{thesaurusName1,thesaurusName2,targetThesaurusName,((Utilities.stopTimer(startTime)) / 60)+"" });
                logFileWriter.append("\r\n<creationInfo>"+trObj.getValue()+"</creationInfo>\r\n");
                logFileWriter.append("</page>");
                logFileWriter.flush();
                logFileWriter.close();
            }

        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "IOException Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);

        }

        return true;
    }

    /* Abandoned code
    public boolean thesaurusExportActions(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, ConfigDBadmin config,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML, String sourceThesaurusName, String targetThesaurusName,
            Locale targetLocale, StringObject resultObj,
            StringObject CopyThesaurusResultMessage, StringBuffer xml,
            String logFileNamePath, String pathToSaveScriptingAndLocale,
            long startTime, PrintWriter out) {

        String initiallySelectedThesaurus = refSessionUserInfo.selectedThesaurus;
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass webappusers = new UsersClass();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, sourceThesaurusName);

        Vector<String> thesauriNames = new Vector<String>();

        OutputStreamWriter logFileWriter = null;


        String time = Utilities.GetNow();


        String Filename = logFileNamePath.substring(logFileNamePath.lastIndexOf('/'), logFileNamePath.length() - ".xml".length());

        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);

            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            try {
                logFileWriter.append(ConstantParameters.xmlHeader );//+ "\r\n"
            } catch (IOException ex) {
                Logger.getLogger(DBImportData.class.getName()).log(Level.SEVERE, null, ex);
                Utils.StaticClass.handleException(ex);
            }
            //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../"+webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
            logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
            logFileWriter.append("<title>report for the copy thesaurus operation from thesaurus: " + sourceThesaurusName + " to thesaurus " + targetThesaurusName + " " + time + "</title>\r\n"
                    + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>\r\n");

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile for the copy operation of thesaurus: " + sourceThesaurusName + " to the new thesarus: " + targetThesaurusName + ".");

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("FileNotFoundException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("UnsupportedEncodingException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("IOException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }


        //Structures to fill
        Vector<String> xmlFacets = new Vector<String>();
        Hashtable<String, Vector<String>> hierarchyFacets = new Hashtable<String, Vector<String>>();

        Vector<String> guideTerms = new Vector<String>();
        Hashtable<String, String> XMLsources = new Hashtable<String, String>();
        Hashtable<String, Vector<SortItem>> XMLguideTermsRelations = new Hashtable<String, Vector<SortItem>>();

        Vector<String> topTerms = new Vector<String>();
        Hashtable<String, Vector<String>> descriptorRts = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> descriptorUfs = new Hashtable<String, Vector<String>>();
        Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes = new Vector<Hashtable<String, Vector<String>>>();


        Hashtable<String, NodeInfoStringContainer> termsInfo = new Hashtable<String, NodeInfoStringContainer>();
        Vector<String> userSelectedTranslationWords = new Vector<String>();
        Vector<String> userSelectedTranslationIdentifiers = new Vector<String>();
        Hashtable<String, String> translationCategories = new Hashtable<String, String>();

        Vector<String> thesaurusVector = new Vector<String>();
        StringObject CreateThesaurusResultMessage = new StringObject("");

        String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, targetThesaurusName);

        //read Translation Categories
        translationCategories = dbGen.getThesaurusTranslationCategories(Q, TA, sis_session, sourceThesaurusName, null, false, true);
        Enumeration<String> trEnum = translationCategories.keys();
        while (trEnum.hasMoreElements()) {
            String word = trEnum.nextElement();
            String identifier = translationCategories.get(word);

            userSelectedTranslationWords.add(word);
            userSelectedTranslationIdentifiers.add(identifier);
        }

        //read facets
        xmlFacets.addAll(dbMerge.ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null));

        if (xmlFacets.contains(defaultFacet) == false) {
            xmlFacets.add(defaultFacet);
        }

        //read hierarchies
        hierarchyFacets = dbMerge.ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null);
        if (hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            Vector<String> unclassifiedFacets = new Vector<String>();
            unclassifiedFacets.add(defaultFacet);
            hierarchyFacets.put(Parameters.UnclassifiedTermsLogicalname, unclassifiedFacets);
        }


        //readTermsInfo and guide terms
        dbMerge.ReadThesaurusTerms(refSessionUserInfo, Q,TA, sis_session, sourceThesaurusName, null,
                termsInfo, guideTerms, XMLguideTermsRelations);


        return true;
    }
    */

    public boolean thesaurusCopyActions(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, ConfigDBadmin config,
            String pathToErrorsXML, String sourceThesaurusName, String targetThesaurusName,
            Locale targetLocale, StringObject resultObj,
            StringObject CopyThesaurusResultMessage, StringBuffer xml,
            String logFileNamePath, String pathToSaveScriptingAndLocale,
            long startTime, PrintWriter out) {

        String initiallySelectedThesaurus = refSessionUserInfo.selectedThesaurus;

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass webappusers = new UsersClass();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, sourceThesaurusName);

        Vector<String> thesauriNames = new Vector<String>();

        OutputStreamWriter logFileWriter = null;


        String time = Utilities.GetNow();


        String Filename = logFileNamePath.substring(logFileNamePath.lastIndexOf('/'), logFileNamePath.length() - ".xml".length());

        try {
            OutputStream fout = new FileOutputStream(logFileNamePath);

            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            try {
                logFileWriter.append(ConstantParameters.xmlHeader );//+ "\r\n"
            } catch (IOException ex) {
                Logger.getLogger(DBImportData.class.getName()).log(Level.SEVERE, null, ex);
                Utils.StaticClass.handleException(ex);
            }
            //logFileWriter.append("<?xml-stylesheet type=\"text/xsl\" href=\"../"+webAppSaveResults_Folder + "/ImportCopyMergeThesaurus_Report.xsl" + "\"?>\r\n");
            logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
            logFileWriter.append("<title>Αναφορά αντιγραφής δεδομένων από τον θησαυρό " + sourceThesaurusName + " στον θησαυρό " + targetThesaurusName + " " + time + "</title>\r\n"
                    + "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>\r\n");

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + time + " LogFile  LogFile for the copy operation of thesaurus: " + sourceThesaurusName + " to the new thesarus: " + targetThesaurusName + ".");

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("FileNotFoundException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("UnsupportedEncodingException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln("IOException Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }


        StringObject DBbackupFileNameCreated = new StringObject("");



        /*************************Step1 CreateThesaurus***********************************/
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of new thesaurus: " + targetThesaurusName + ".");
        /*
         *boolean GivenThesaurusCanBeCreated = dbAdminUtils.GivenThesaurusCanBeCreated(config,common_utils, thesaurusVector,  NewThesaurusName,  NewThesaurusNameDBformatted,  CreateThesaurusResultMessage,  CreateThesaurusSucceded);
         *if (GivenThesaurusCanBeCreated == true) {
         *
         * AS in create thesaurus. Should have thesaurusVactor initialized
         */

        //Q.begin_query();
        //allHierarcies.addAll(dbGen.getDBAdminHierarchiesAndStatusesXML(sessionInstance, Q, sis_session));
        //Q.end_query();

        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(dbGen, config, common_utils, targetThesaurusName, targetThesaurusName, thesauriNames,
                CopyThesaurusResultMessage,
                "backup_before_copy_thes_" + sourceThesaurusName + "_to_" + targetThesaurusName,
                DBbackupFileNameCreated);


        
        if (CreateThesaurusSucceded == false) {
            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
            //xml.append(u.getDBAdminHierarchiesAndStatusesXML(allHierarcies, dbGen));
            xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, CopyThesaurusResultMessage, false));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            //out.println("DONE");
            u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Creation of new thesaurus: " + targetThesaurusName + " failed.");
            return false;
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Successful creation of new thesaurus: " + targetThesaurusName + ".");
        }

        Utils.StaticClass.closeDb();
        
        QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();


        //open connection and start Transaction
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurusName, false)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData thesaurusCopyActions()");
            return false;
        }
        boolean ok = true;
        try {

            /*
             * Step1 CreateThesaurus
             * Step2 Ensure Database is Running
             * Step3 begin transaction again
             * Step4 get default facet
             * Step5 create facets
             * Step6 create hierarchies
             * Step7 create terms
             * Step8 Guide Terms Addition Patch
             */
            //Structures to fill
            Vector<String> xmlFacets = new Vector<String>();
            Hashtable<String, Vector<String>> hierarchyFacets = new Hashtable<String, Vector<String>>();

            Vector<String> guideTerms = new Vector<String>();
            Hashtable<String, String> XMLsources = new Hashtable<String, String>();
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations = new Hashtable<String, Vector<SortItem>>();

            Vector<String> topTerms = new Vector<String>();
            Hashtable<String, Vector<String>> descriptorRts = new Hashtable<String, Vector<String>>();
            Hashtable<String, Vector<String>> descriptorUfs = new Hashtable<String, Vector<String>>();
            Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes = new Vector<Hashtable<String, Vector<String>>>();

            Hashtable<String, NodeInfoStringContainer> termsInfo = new Hashtable<String, NodeInfoStringContainer>();
            Vector<String> userSelectedTranslationWords = new Vector<String>();
            Vector<String> userSelectedTranslationIdentifiers = new Vector<String>();
            Hashtable<String, String> translationCategories = new Hashtable<String, String>();

            Vector<String> thesaurusVector = new Vector<String>();
            StringObject CreateThesaurusResultMessage = new StringObject("");

            String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, targetThesaurusName);

            //read Translation Categories
            translationCategories = dbGen.getThesaurusTranslationCategories(Q, TA, sis_session, sourceThesaurusName, null, false, true);
            Enumeration<String> trEnum = translationCategories.keys();
            while (trEnum.hasMoreElements()) {
                String word = trEnum.nextElement();
                String identifier = translationCategories.get(word);

                userSelectedTranslationWords.add(word);
                userSelectedTranslationIdentifiers.add(identifier);
            }

            //read facets
            xmlFacets.addAll(dbMerge.ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null));

            if (xmlFacets.contains(defaultFacet) == false) {
                xmlFacets.add(defaultFacet);
            }

            //read hierarchies
            hierarchyFacets = dbMerge.ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null);
            if (hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
                Vector<String> unclassifiedFacets = new Vector<String>();
                unclassifiedFacets.add(defaultFacet);
                hierarchyFacets.put(Parameters.UnclassifiedTermsLogicalname, unclassifiedFacets);
            }

            //readTermsInfo and guide terms
            dbMerge.ReadThesaurusTerms(refSessionUserInfo, Q, TA, sis_session, sourceThesaurusName, null,
                    termsInfo, guideTerms, XMLguideTermsRelations);

            guideTerms.clear();
            //readGuideTerms

            guideTerms.addAll(dbGen.collectGuideLinks(sourceThesaurusName, Q, sis_session));

            SessionUserInfo = new UserInfoClass(refSessionUserInfo);
            webappusers.UpdateSessionUserSessionAttribute(refSessionUserInfo, targetThesaurusName);

            //try {
            this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                    targetThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);

        //} catch (Exception ex) {
            //  Utils.StaticClass.webAppSystemOutPrintln("Exception Caught: " + ex.getMessage());
//            Utils.StaticClass.handleException(ex);
            //      }
            // Step3 Read XML file in order to fill basic datastructures concerning terms
            // Step4 Process these data structures in order to define topterms and orphans
            //filling all structures passed as parameters except xmlFilePath parameter and then process data in order to classify terms in levels
            processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);

            ok = writeThesaurusData(SessionUserInfo, common_utils,
                    Q, TA, sis_session, tms_session,
                    xmlFacets, guideTerms, XMLsources, XMLguideTermsRelations,
                    hierarchyFacets, termsInfo, userSelectedTranslationWords,
                    userSelectedTranslationIdentifiers, translationCategories,
                    topTerms, descriptorRts, descriptorUfs,
                    allLevelsOfImportThes, targetThesaurusName,
                    pathToErrorsXML, targetLocale, resultObj, logFileWriter);

            
                if(ok){
                    //SUCESS
                    commitCopyActions(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, targetLocale,targetThesaurusName, out, Filename.concat(".html"));
                    if (logFileWriter != null) {

                            logFileWriter.append("\r\n<creationInfo>Η διαδικασία αντιγραφής ολοκληρώθηκε με επιτυχία σε χρόνο : " + ((Utilities.stopTimer(startTime)) / 60) + " λεπτά.</creationInfo>\r\n");
                            logFileWriter.append("</page>");
                            logFileWriter.flush();
                            logFileWriter.close();

                    }
                }
                

        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "IOException Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            ok = false;
            return false;
        } finally {
            if(!ok){
                this.abortCopyActions(SessionUserInfo, Q, TA, sis_session, tms_session, targetLocale, common_utils,
                        initiallySelectedThesaurus, targetThesaurusName, DBbackupFileNameCreated, resultObj, out);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Thesaurus copy operation failed.");
                return false;
            }
        }
        return true;



    }

    public String getXMLMiddleForCopyThesaurus(CommonUtilsDBadmin common_utils, Vector thesaurusVector,
            StringObject CopyThesaurusResultMessage, Boolean CopyThesaurusSucceded) {
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
        XMLMiddleStr += "<copyThesaurusResult>";
        // write the NewThesaurusName given
                /*XMLMiddleStr += "<NewThesaurusName>" + common_utils.ReplaceSpecialCharacters(NewThesaurusName) + "</NewThesaurusName>";
        XMLMiddleStr += "<CopyThesaurusSucceded>" + CopyThesaurusSucceded + "</CopyThesaurusSucceded>";*/
        XMLMiddleStr += /*"<CopyThesaurusResultMessage>" +*/ CopyThesaurusResultMessage.getValue() /*+ "</CopyThesaurusResultMessage>"*/;
        XMLMiddleStr += "</copyThesaurusResult>";
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }

    public String getXMLMiddleForMergeThesaurus(CommonUtilsDBadmin common_utils, Vector thesaurusVector,
            String MergeThesaurusMessage) {
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

        // write the NewThesaurusName given
        //XMLMiddleStr += "<NewThesaurusName>" + common_utils.ReplaceSpecialCharacters(NewThesaurusName) + "</NewThesaurusName>";
        XMLMiddleStr += "<mergeThesauriResult>" + MergeThesaurusMessage + "</mergeThesauriResult>";

        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
    }

    public void commitMergeActions(UserInfoClass SessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Locale targetLocale, String mergedThesaurusName, PrintWriter out, String reportFile) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> thesauriNames = new Vector<String>();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();



        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);


        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        
        String pathToMessagesXML = Utilities.getMessagesXml();
        StringObject resultMessageObj = new StringObject();
        Vector<String> errorArgs = new Vector<String>();
        
        errorArgs.add(mergedThesaurusName);        
        dbGen.Translate(resultMessageObj, "root/commitMergeActions/MergeThesaurusSucceed", errorArgs, pathToMessagesXML);
        errorArgs.removeAllElements();
        
        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        //xml.append("<mergewarnings>");
        //xml.append(mergeNotes);
        //xml.append("</mergewarnings>");
        xml.append("<mergeReportFile>");
        xml.append(reportFile);
        xml.append("</mergeReportFile>");
        xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, resultMessageObj.getValue()));
        //xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, "The merge thesauri procedure was successfully completed. New thesaurus: " + mergedThesaurusName + " was set as the current one."));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        if(out==null){
            //Utils.StaticClass.webAppSystemOutPrintln(xml.toString());
        }
        else{
            u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());
        }
        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public void commitCopyActions(UserInfoClass SessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, Locale targetLocale, String mergedThesaurusName, PrintWriter out, String reportFile) {

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();

        Vector<String> thesauriNames = new Vector<String>();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();

        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);


        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);     
        
        String pathToMessagesXML = Utilities.getMessagesXml();
        StringObject resultMessageObj = new StringObject();
        Vector<String> errorArgs = new Vector<String>();
        
        dbGen.Translate(resultMessageObj, "root/commitCopyActions/CopySucceed", null, pathToMessagesXML);

        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        xml.append("<copyReportFile>");
        xml.append(reportFile);
        xml.append("</copyReportFile>");
        xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames,resultMessageObj, true));
        //xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, new StringObject("The copy thesuarus procedure was successfully completed.\n\n"), true));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public void abortMergeActions(UserInfoClass SessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Locale targetLocale, CommonUtilsDBadmin common_utils,
            String initiallySelectedThesaurus, String mergedThesaurusName,
            StringObject DBbackupFileNameCreated, StringObject resultObj, PrintWriter out) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ABORT MERGE");

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ABORT MERGE");

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();
        Vector<String> thesauriNames = new Vector<String>();

        //abort transaction and close connection
        Q.free_all_sets();
        Q.TEST_abort_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);



        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());

        StringObject result = new StringObject("");
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, initiallySelectedThesaurus);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + DBbackupFileNameCreated.getValue());

        boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);
        thesauriNames.remove(mergedThesaurusName);

        if (restored) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of : " + DBbackupFileNameCreated.getValue() + " succeeded.");
            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, initiallySelectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData abortMergeActions()");
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
        xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, "Αποτυχία λειτουργίας συγχώνευσης. " + resultObj.getValue()));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        if(out==null){
            Utils.StaticClass.webAppSystemOutPrintln(xml.toString());
        }
        else{
            u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());
        }

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();

    }

    public void abortCopyActions(UserInfoClass SessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Locale targetLocale, CommonUtilsDBadmin common_utils,
            String initiallySelectedThesaurus, String mergedThesaurusName,
            StringObject DBbackupFileNameCreated, StringObject resultObj, PrintWriter out) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ABORT COPY");
        Q.TEST_abort_transaction();

        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();

        Vector<String> thesauriNames = new Vector<String>();
        Vector<String> allHierarchies = new Vector<String>();
        Vector<String> allGuideTerms = new Vector<String>();


        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());

        StringObject result = new StringObject("");

        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, initiallySelectedThesaurus);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + DBbackupFileNameCreated.getValue());

        boolean restored = common_utils.RestoreDBbackup(DBbackupFileNameCreated.getValue(), result);

        if (restored) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Restoration of :" + DBbackupFileNameCreated.getValue() + " succeeded.");
            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, initiallySelectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData abortCopyActions()");
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
        xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, new StringObject("Αποτυχία λειτουργίας αντιγραφής: " + resultObj.getValue()), false));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public boolean thesaurusImportActions(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            ConfigDBadmin config, Locale targetLocale, String pathToErrorsXML, String xmlFilePath,
            String xmlSchemaType, String importThesaurusName,
            String backUpDescription, StringObject DBbackupFileNameCreated,
            StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        boolean returnVal = false;
        
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        ParseFileData parser = new ParseFileData();

        //Structures to fill
        Vector<String> xmlFacets = new Vector<String>();
        Vector<String> guideTerms = new Vector<String>();
        Hashtable<String, String> XMLsources = new Hashtable<String, String>();
        Hashtable<String, Vector<SortItem>> XMLguideTermsRelations = new Hashtable<String, Vector<SortItem>>();
        Hashtable<String, Vector<String>> hierarchyFacets = new Hashtable<String, Vector<String>>();
        Hashtable<String, NodeInfoStringContainer> termsInfo = new Hashtable<String, NodeInfoStringContainer>();
        Vector<String> userSelectedTranslationWords = new Vector<String>();
        Vector<String> userSelectedTranslationIdentifiers = new Vector<String>();
        Hashtable<String, String> userSelections = new Hashtable<String, String>();

        Vector<String> thesaurusVector = new Vector<String>();
        StringObject CreateThesaurusResultMessage = new StringObject("");

        //
        // Step1  Read Facets defined in xml
        // Step2  Read Hierarchies defined in xml
        // Step3  Read XML file in order to fill basic datastructures concerning terms
        // Step4  Process these data structures in order to define topterms and orphans
        // Step5  Create thesaurus or parse model over an existing one
        // Step6  Ensure server is running
        // Step7  Start connection and transaction since server was restarted
        // Step8  Get and put default Status per user for Unclassified terms
        // createSources
        // Step9  Create Facets defined in xml
        // Step10 Create Hierarchies defined in xml
        // Step11 Create Hierarchies specified by topterms of Step2
        // Step12 Create Terms
        // Step13 Create GuideTerms - tried to apply this step in createTerms but many things needed to change. It is an addition and it is treaded as an addition
        //

        boolean processSucceded = true;
        String inputScheme = ConstantParameters.xmlschematype_THEMAS;

        // Step1 Read Facets specified by XML
        if (parser.readXMLFacets(importThesaurusName, xmlFilePath, inputScheme, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read FACETS.");
            processSucceded = false;
        }


        /* Step2 Read Hierarchies specified by XML************************************************/
        if (processSucceded && parser.readXMLHierarchies(importThesaurusName, xmlFilePath, inputScheme, hierarchyFacets, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read HIERARCHIES.");
            processSucceded = false;
        }

        if (processSucceded) {
            parser.readTranslationCategories(xmlFilePath, inputScheme, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections);
        }


        if (processSucceded && parser.readXMLTerms(xmlFilePath, inputScheme, termsInfo, userSelections) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read TERMS.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLSources(xmlFilePath, inputScheme, XMLsources) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read SOURCES.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLGuideTerms(xmlFilePath, inputScheme, guideTerms, XMLguideTermsRelations) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Guide Terms / Node Labels.");
            processSucceded = false;
        }

        if (processSucceded == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read XML file.");
            return false;
        }


        /*
         * UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
        QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
        String importThesaurusName,
        Vector<String> guideTerms, Hashtable<String, String> XMLsources,
        Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
        Hashtable<String, Vector<String>> hierarchyFacets,
        Hashtable<String, NodeInfoStringContainer> termsInfo,
        OutputStreamWriter logFileWriter
         */

        //common_utils.RestartDatabaseIfNeeded();

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of creation of new thesaurus: " + importThesaurusName + ". Time: " + Utilities.GetNow());

        Utils.StaticClass.closeDb();
        //Step5 thesaurus creation
        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(dbGen, config, common_utils, importThesaurusName, importThesaurusName, thesaurusVector, CreateThesaurusResultMessage, backUpDescription, DBbackupFileNameCreated);
        if (CreateThesaurusSucceded == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Creation operation of new thesaurus: " + importThesaurusName + " failed.");
            return false;
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Successful creation of new thesaurus: " + importThesaurusName + ".");
        }

        Utils.StaticClass.closeDb();
        
        QClass Q = new neo4j_sisapi.QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();


        //open connection and start Transaction
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, importThesaurusName, false)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ thesaurusImportActions ");
            return false;
        }
        try{
        /*
        // Step6 Ensure server is running
        boolean serverStarted = common_utils.StartDatabase();
        if (serverStarted == false) {
            String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
            CreateThesaurusResultMessage.setValue(StartServerFailure + " " + common_utils.DatabaserBatFileDirectory + File.separator + common_utils.DatabaseBatFileName);
            common_utils.RestartDatabaseIfNeeded();
        }*/

        //wait until server is finally started
        /*
        boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
        while (databaseIsRunning == false) {
            databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
        }*/


        this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                importThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);
        Vector<String> topTerms = new Vector<String>();
        Hashtable<String, Vector<String>> descriptorRts = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> descriptorUfs = new Hashtable<String, Vector<String>>();
        Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes = new Vector<Hashtable<String, Vector<String>>>();

        // Step3 Read XML file in order to fill basic datastructures concerning terms
        // Step4 Process these data structures in order to define topterms and orphans
        //filling all structures passed as parameters except xmlFilePath parameter and then process data in order to classify terms in levels

        processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);


        returnVal= writeThesaurusData(refSessionUserInfo, common_utils,
                Q, TA, sis_session, tms_session,
                xmlFacets, guideTerms, XMLsources, XMLguideTermsRelations,
                hierarchyFacets, termsInfo, userSelectedTranslationWords,
                userSelectedTranslationIdentifiers, userSelections,
                topTerms, descriptorRts, descriptorUfs,
                allLevelsOfImportThes, importThesaurusName,
                pathToErrorsXML, targetLocale, resultObj, logFileWriter);
        }
        finally{
            if(returnVal){
                Q.TEST_end_transaction();
                Utils.StaticClass.closeDb();
            }
            else{
                Q.TEST_abort_transaction();
            }
            return returnVal;
        }

        /*
        // Step7 Start connection and transaction since server was restarted
        dbMerge.openConnection(Q, TA, sis_session, tms_session, false);
        //wtmsusers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, importThesaurusName, SessionUserInfo.userGroup);
        TA.SetThesaurusName(tms_session.getValue(), new StringObject(importThesaurusName));
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of new thesaurus: " + importThesaurusName + ".");
        Q.begin_transaction();

        if (readAndSyncronizeTranslationCategories(importThesaurusName, resultObj, Q, TA, sis_session, tms_session,
        userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections) == false) {
        return false;
        }


        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);
        // Step8 Get and put default Status per user for Unclassified terms
        if(termsInfo.containsKey(Parameters.UnclassifiedTermsLogicalname) ==false ){
        specifyOrphansStatus(SessionUserInfo, Q, sis_session, tms_session, resultObj);
        }
        else{
        if(termsInfo.get(Parameters.UnclassifiedTermsLogicalname).descriptorInfo.get(ConstantParameters.status_kwd).isEmpty()){
        specifyOrphansStatus(SessionUserInfo, Q, sis_session, tms_session, resultObj);
        }
        }

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        if (CreateSources(SessionUserInfo.selectedThesaurus, common_utils, importThesaurusName,
        Q, TA, sis_session, tms_session, XMLsources, resultObj, logFileWriter) == false) {
        return false;
        }

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        // Step9 Create Facets specified by XML
        if (dbMerge.CreateFacets(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, xmlFacets, resultObj) == false) {
        return false;
        }

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, importThesaurusName);

        // Step10 Create Hierarchies specified by XML
        if (dbMerge.CreateHierarchies(SessionUserInfo, Q, TA, sis_session, tms_session,
        importThesaurusName, defaultFacet, targetLocale, resultObj, logFileWriter, hierarchyFacets) == false) {
        return false;
        }

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        // Step11 Create Hierarchies specified by topterms of Step2
        if (importMoreHierarchiesFromTopTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, topTerms, targetLocale, logFileWriter, resultObj) == false) {
        return false;
        }

        common_utils.restartTransactionAndDatabase(Q,TA,sis_session,tms_session,importThesaurusName);

        // Step12 Create Terms
        if (importTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
        pathToErrorsXML, importThesaurusName, termsInfo, resultObj, allLevelsOfImportThes,
        descriptorRts, descriptorUfs, logFileWriter) == false) {
        return false;
        }

        //Step 13 Guide Terms Addition Patch
        if (dbMerge.CreateGuideTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, guideTerms, XMLguideTermsRelations, importThesaurusName, resultObj) == false) {
        return false;
        }

        common_utils.restartTransactionAndDatabase(Q,TA,sis_session,tms_session,importThesaurusName);

        return true;
         *
         */
    }
    
    

    public boolean CreateSources(String selectedThesaurus, CommonUtilsDBadmin common_utils, String importThesaurusName,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, 
            Hashtable<String, String> XMLsources, StringObject resultObj, OutputStreamWriter logFileWriter){
        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Startin creation of SOURCES. Time: " + Utilities.GetNow());

        try{
            DBGeneral dbGen = new DBGeneral();
            Utilities u = new Utilities();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();



            String prefixSource = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());

            StringObject sourceClassObj = new StringObject(ConstantParameters.SourceClass);
            StringObject sourceNoteLinkObj = new StringObject(ConstantParameters.source_note_kwd);

            int counter = 0;

            //step1 create sources
            int total = XMLsources.size();
            Enumeration<String> sourcesIterator = XMLsources.keys();
            while (sourcesIterator.hasMoreElements()) {
                Q.free_all_sets();
                String nameStr = sourcesIterator.nextElement();
                //String sourceNoteStr = XMLsources.get(nameStr);

                /*
                if(counter>=2800 && counter <=3000){
                    Utils.StaticClass.webAppSystemOutPrintln(counter+".\t" + nameStr);
                }*/
                if (counter % DBMergeThesauri.restartInterval == 0) {
                    if (common_utils != null) {
                        Utils.StaticClass.webAppSystemOutPrintln("Sources counter: " + counter + " of " + total + "  ");
                        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
                        //Q.TEST_end_transaction();
                        //Utils.StaticClass.closeDb();
                        //if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, importThesaurusName, false)==QClass.APIFail){
                          //  Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ ");
                            //return false;
                        //}
                        
                    }
                }
                counter++;

                /*
                try {
                    byte[] byteArray = nameStr.getBytes("UTF-8");


                    int maxChars = dbtr.getMaxBytesForSource(selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {

                        Utils.StaticClass.webAppSystemOutPrintln("By passed creation of source " + nameStr + " due to length limitation.");
                        continue;
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                */

                StringObject nameDBObj = new StringObject(prefixSource.concat(nameStr));
                Q.reset_name_scope();
                if (Q.set_current_node(nameDBObj) != QClass.APIFail) {
                    //source exists check source note
                    continue;

                } else {
                    //source does not exist Create it with its source note
                    Q.reset_name_scope();
                    int ret = TA.CHECK_CreateSource(nameDBObj);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        resultObj.setValue(dbGen.check_success(ret, TA, null, tms_session));
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create source: " + nameStr);
                        Q.free_all_sets();
                        return false;
                    }

                }

            }
            //step2 create source notes
            counter = 0;
            Q.free_all_sets();
            Enumeration<String> sourcesIterator2 = XMLsources.keys();
            while (sourcesIterator2.hasMoreElements()) {
                Q.free_all_sets();
                String nameStr = sourcesIterator2.nextElement();
                String sourceNoteStr = XMLsources.get(nameStr);
                if(sourceNoteStr==null || sourceNoteStr.trim().length()==0){
                    counter++;
                    continue;
                }
                sourceNoteStr = sourceNoteStr.trim();
                if (counter % DBMergeThesauri.restartInterval == 0) {
                    if (common_utils != null) {
                        Utils.StaticClass.webAppSystemOutPrintln("Source notes counter: " + counter + " of " + total + "  ");
                        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
                    }
                }
                counter++;

                /*
                try {
                    byte[] byteArray = nameStr.getBytes("UTF-8");


                    int maxChars = dbtr.getMaxBytesForSource(selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {

                        Utils.StaticClass.webAppSystemOutPrintln("By passed creation of source " + nameStr + " due to length limitation.");
                        continue;
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                */


                StringObject nameDBObj = new StringObject(prefixSource.concat(nameStr));

                //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
                StringObject prevThes = new StringObject();
                TA.GetThesaurusNameWithoutPrefix(prevThes);
                if(prevThes.getValue().equals(selectedThesaurus)==false){
                    TA.SetThesaurusName(selectedThesaurus);
                }
                StringObject sourceNoteObj = new StringObject("");
                TA.GetDescriptorComment(nameDBObj, sourceNoteObj, new StringObject("Source"), new StringObject(ConstantParameters.source_note_kwd));
                String oldSourceNoteStr = sourceNoteObj.getValue().trim();
                if (oldSourceNoteStr.length() == 0) {
                    if (sourceNoteStr.length() > 0) {
                        int ret = TA.SetDescriptorComment(nameDBObj, new StringObject(sourceNoteStr), sourceClassObj, sourceNoteLinkObj);
                        if (ret == TMSAPIClass.TMS_APIFail) {
                            //resultObj.setValue(WTA.errorMessage.getValue());
                            TA.ALMOST_DONE_GetTMS_APIErrorMessage(resultObj);
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to add source Note in source: " + nameStr + ". sourceNote = " + sourceNoteStr);
                            Q.free_all_sets();
                            return false;
                        }
                    }
                } else if (oldSourceNoteStr.compareTo(sourceNoteStr) != 0) {

                    int ret = TA.DeleteDescriptorComment(nameDBObj, sourceClassObj, sourceNoteLinkObj);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to delete previous source Note of source: " + nameStr + " while trying to create the merged source note: " + oldSourceNoteStr + " ### " + sourceNoteStr + ".");
                        //resultObj.setValue(" " + WTA.errorMessage.getValue());
                        TA.ALMOST_DONE_GetTMS_APIErrorMessage(resultObj);
                        Q.free_all_sets();
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                        return false;
                    }

                    String newSourceNote = u.mergeStrings(oldSourceNoteStr,sourceNoteStr);
                    
                    ret = TA.SetDescriptorComment(nameDBObj, new StringObject(newSourceNote), sourceClassObj, sourceNoteLinkObj);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        //resultObj.setValue(WTA.errorMessage.getValue());
                        TA.ALMOST_DONE_GetTMS_APIErrorMessage(resultObj);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create source Note in source: " + nameStr + ". sourceNote = " + newSourceNote);
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                        return false;
                    }
                    logFileWriter.append("\r\n<targetTerm><name>Πηγή: " + Utilities.escapeXML(nameStr) + "</name><errorType>" + ConstantParameters.source_note_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(oldSourceNoteStr + " ### " + sourceNoteStr) + "</errorValue>");
                    logFileWriter.append("<reason>Βρέθηκαν 2 σημειώσεις για την πηγή: '" + Utilities.escapeXML(nameStr) + "'. Διατηρηθηκαν και οι δύο με το διαχωριστικό ' ### '.</reason>");
                    logFileWriter.append("</targetTerm>\r\n");
                    logFileWriter.flush();

                }
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }

            }

            Q.free_all_sets();
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of SOURCES creation.");
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass().toString());
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);                    
            return false;
        }
        return true;
    }

    public void readCurrentNodesAttributes(Node currentNode, String currentNodeValue, Vector<String> validAttrKeywords, Hashtable<String, NodeInfoStringContainer> termsInfo, String[] output) {

        int numOfCurrentNodeAttributes = currentNode.getAttributes().getLength();

        NodeInfoStringContainer targetTermRestInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);

        for (int k = 0; k < numOfCurrentNodeAttributes; k++) {
            Node attrNode = currentNode.getAttributes().item(k);
            String nodeType = attrNode.getNodeName();

            if (validAttrKeywords.contains(nodeType) == false) {
                continue;
            }

            String nodeValue = attrNode.getTextContent();
            nodeValue = readXMLTag(nodeValue);

            if (targetTermRestInfo.descriptorInfo.containsKey(nodeType) && targetTermRestInfo.descriptorInfo.get(nodeType).contains(nodeValue) == false) {
                targetTermRestInfo.descriptorInfo.get(nodeType).add(nodeValue);
            }

        }

        if (termsInfo.containsKey(currentNodeValue) == false) {
            termsInfo.put(currentNodeValue, targetTermRestInfo);
        } else {
            NodeInfoStringContainer olderTermRestInfo = termsInfo.get(currentNodeValue);

            if (olderTermRestInfo == null) {
                termsInfo.put(currentNodeValue, targetTermRestInfo);
            } else {
                for (int p = 0; p < output.length; p++) {
                    String mergeAttribute = output[p];
                    Vector<String> part1 = olderTermRestInfo.descriptorInfo.get(mergeAttribute);
                    Vector<String> part2 = targetTermRestInfo.descriptorInfo.get(mergeAttribute);
                    int initialSize = part1.size();

                    for (int r = 0; r < part2.size(); r++) {
                        if (part1.contains(part2.get(r)) == false) {
                            part1.add(part2.get(r));
                        }
                    }

                    if (part1.size() > initialSize) {
                        olderTermRestInfo.descriptorInfo.put(mergeAttribute, part1);
                        termsInfo.put(currentNodeValue, olderTermRestInfo);
                    }
                }
            }
        }
    }

    public void processXMLTerms(Hashtable<String, NodeInfoStringContainer> termsInfo, Hashtable<String, Vector<String>> descriptorRts, Hashtable<String, Vector<String>> descriptorUfs,/* Vector<String> LinkingToSelf,*/ Hashtable<String, Vector<String>> hierarchyFacets, Vector<String> topTerms, Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting Xml TERMS processing.");
        DBGeneral dbGen = new DBGeneral();

        Vector<String> allTermsHavingBTs = new Vector<String>();
        Vector<String> allTermsHavingNTs = new Vector<String>();
        Vector<String> allTermsWithoutBTsorNTs = new Vector<String>();
        Hashtable<String, Vector<String>> descriptorNts = new Hashtable<String, Vector<String>>();


        Vector<String> nodesOfInterest = new Vector<String>();
        nodesOfInterest.add("descriptor");
        nodesOfInterest.add(ConstantParameters.bt_kwd);
        nodesOfInterest.add(ConstantParameters.nt_kwd);
        nodesOfInterest.add(ConstantParameters.rt_kwd);
        nodesOfInterest.add(ConstantParameters.uf_kwd);

        //DEBUG int counter=1;
        Enumeration<String> termInfoIterator = termsInfo.keys();

        while (termInfoIterator.hasMoreElements()) {


            String targetNode = termInfoIterator.nextElement();


            NodeInfoStringContainer targetNodeInfo = termsInfo.get(targetNode);

            Vector<String> BTnodes = new Vector<String>();
            Vector<String> NTnodes = new Vector<String>();
            Vector<String> RTnodes = new Vector<String>();
            Vector<String> UFnodes = new Vector<String>();

            BTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.bt_kwd));
            NTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.nt_kwd));
            RTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.rt_kwd));
            UFnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.uf_kwd));

            int howmanyBTs = BTnodes.size();
            int howmanyNTs = NTnodes.size();
            int howmanyRTs = RTnodes.size();
            int howmanyUFs = UFnodes.size();


            if (descriptorNts.containsKey(targetNode) == false) {
                descriptorNts.put(targetNode, new Vector<String>());
            }
            if (descriptorRts.containsKey(targetNode) == false) {
                descriptorRts.put(targetNode, new Vector<String>());
            }


            boolean validValueDetected = false;
            //Bts relations may define a new Descriptor. in this case hashTables
            //should be updated with parsedBt as new key and no values.
            //target node should be added to nts of each bt child Node
            for (int k = 0; k < howmanyBTs; k++) {
                String parsedBt = BTnodes.get(k);

                if (parsedBt == null || parsedBt.length() == 0) {
                    continue;
                }

                if (descriptorNts.containsKey(parsedBt) == false) {
                    descriptorNts.put(parsedBt, new Vector<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedBt) == false) {
                    descriptorRts.put(parsedBt, new Vector<String>());//add it as a key because it may be later encountered
                }

                if (targetNode.compareTo(parsedBt) == 0) {
                    ///LinkingToSelf.add(targetNode);
                } else {
                    validValueDetected = true;
                    if (descriptorNts.get(parsedBt).contains(targetNode) == false) {
                        descriptorNts.get(parsedBt).add(targetNode);//targetNode is nt of parsedBt
                    }

                    if (allTermsHavingNTs.contains(parsedBt) == false) {
                        allTermsHavingNTs.add(parsedBt);
                    }
                }
            }

            if (validValueDetected && allTermsHavingBTs.contains(targetNode) == false) {
                allTermsHavingBTs.add(targetNode);
            }

            validValueDetected = false;
            //Nts relations may also define a new Descriptor. in this case hashTables
            //should be updated with parsedNt as new key and no values.
            //each child nt element should be added to targetNode's nts
            for (int k = 0; k < howmanyNTs; k++) {
                String parsedNt = NTnodes.get(k);

                if (parsedNt == null || parsedNt.length() == 0) {
                    continue;
                }
                if (descriptorNts.containsKey(parsedNt) == false) {
                    descriptorNts.put(parsedNt, new Vector<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedNt) == false) {
                    descriptorRts.put(parsedNt, new Vector<String>());//add it as a key because it may be later encountered
                }

                if (targetNode.compareTo(parsedNt) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    validValueDetected = true;
                    if (descriptorNts.get(targetNode).contains(parsedNt) == false) {
                        descriptorNts.get(targetNode).add(parsedNt);//parsedNt is nt of targetNode
                    }

                    if (allTermsHavingBTs.contains(parsedNt) == false) {
                        allTermsHavingBTs.add(parsedNt);
                    }
                }
            }

            if (validValueDetected && allTermsHavingNTs.contains(targetNode) == false) {
                allTermsHavingNTs.add(targetNode);
            }

            //Rts relations may also define a new Descriptor. in this case hashTables
            //should be updated with parsedRt as new key and no values.
            //each rt content should be added to targetNode's rts
            for (int k = 0; k < howmanyRTs; k++) {
                String parsedRt = RTnodes.get(k);

                if (parsedRt == null || parsedRt.length() == 0) {
                    continue;
                }
                if (descriptorNts.containsKey(parsedRt) == false) {
                    descriptorNts.put(parsedRt, new Vector<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedRt) == false) {
                    descriptorRts.put(parsedRt, new Vector<String>());//add it as a key because it may be later encountered
                }

                if (targetNode.compareTo(parsedRt) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    if (descriptorRts.get(targetNode).contains(parsedRt) == false) {
                        descriptorRts.get(targetNode).add(parsedRt);
                    }
                }
            }

            //READ UF Links
            for (int k = 0; k < howmanyUFs; k++) {
                String parsedUF = UFnodes.get(k);

                if (parsedUF == null || parsedUF.length() == 0) {
                    continue;
                }

                if (descriptorUfs.containsKey(targetNode) == false) {
                    descriptorUfs.put(targetNode, new Vector<String>());//add it as a key because it may be later encountered
                }

                if (targetNode.compareTo(parsedUF) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    if (descriptorUfs.get(targetNode).contains(parsedUF) == false) {
                        descriptorUfs.get(targetNode).add(parsedUF);
                    }
                }
            }
        }

        findOutTermLevels(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets, allLevelsOfImportThes);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Xml TERMS processing.");
    }

    public void findOutTermLevels(Hashtable<String, Vector<String>> descriptorNts, Vector<String> allTermsHavingBTs, Vector<String> allTermsHavingNTs, Vector<String> allTermsWithoutBTsorNTs, Vector<String> topTerms, Hashtable<String, Vector<String>> hierarchyFacets, Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of classification of terms in hierarchical levels.");

        //filling structures allTermsWithoutBTsorNTs, topTerms and find out hierarchies and top terms
        findOutTopTermsAndOrphans(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets);
        
        //classify terms in levels of creation kept level by level in allLevelsOfImportThes


        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with bt " + allTermsHavingBTs.size() + ".");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with nt " + allTermsHavingNTs.size() + ".");

        Vector<String> currentLevel = new Vector<String>();
        currentLevel.addAll(topTerms);


        /*
        if(Parameters.DEBUG){
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nList of terms promoted to Top Terms");
        for(int i=0; i< topTerms.size(); i++){
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+i + "\t" + topTerms.get(i));
        }
        }
         */
        //Now add all hierarchies created in current level
        Enumeration<String> parsedHierarchies = hierarchyFacets.keys();
        while (parsedHierarchies.hasMoreElements()) {
            String targetHierarchy = parsedHierarchies.nextElement();
            if (currentLevel.contains(targetHierarchy) == false) {
                currentLevel.add(targetHierarchy);
            }
        }

        Vector<String> parsedTerms = new Vector<String>();
        int levelIndex = 0;
        while (currentLevel.size() > 0) {

            int termsperlevel = 0;
            //logFileWriter.append("\r\nΕπίπεδο όρων Νο: " + (levelIndex+2) +"\r\n");


            allLevelsOfImportThes.add(readNextLevelSetTermsAndBts(currentLevel, descriptorNts, parsedTerms));
            Vector<String> nextLevel = new Vector<String>();
            Enumeration<String> parseLevel = allLevelsOfImportThes.get(levelIndex).keys();
            while (parseLevel.hasMoreElements()) {
                termsperlevel++;
                String term = parseLevel.nextElement();

                if (nextLevel.contains(term) == false) {
                    nextLevel.add(term);
                }
            }

            levelIndex++;
            if (nextLevel.size() == 0) {
                break;
            } else {
                currentLevel.removeAllElements();
                currentLevel.addAll(nextLevel);
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of classification of terms in hierarchical levels.");

    }

    public void findOutTopTermsAndOrphans(Hashtable<String, Vector<String>> descriptorNts, Vector<String> allTermsHavingBTs, Vector<String> allTermsHavingNTs, Vector<String> allTermsWithoutBTsorNTs, Vector<String> topTerms, Hashtable<String, Vector<String>> hierarchyFacets) {

        //find out topTerms and unclassifed terms
        Enumeration<String> parseAllTerms = descriptorNts.keys();
        while (parseAllTerms.hasMoreElements()) {

            String term = parseAllTerms.nextElement();

            if (allTermsHavingBTs.contains(term) == false) {

                if (allTermsHavingNTs.contains(term) == false) {

                    if (hierarchyFacets.containsKey(term) == false) {
                        allTermsWithoutBTsorNTs.add(term);
                    }
                } else {
                    if (hierarchyFacets.containsKey(term) == false) {
                        topTerms.add(term);
                    }
                }
            }

        }
        //Update read structures affected
        if (topTerms.contains(Parameters.UnclassifiedTermsLogicalname) == false && hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            topTerms.add(Parameters.UnclassifiedTermsLogicalname);
        }

        if (allTermsHavingNTs.contains(Parameters.UnclassifiedTermsLogicalname) == false) {
            allTermsHavingNTs.add(Parameters.UnclassifiedTermsLogicalname);
        }

        if (descriptorNts.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            descriptorNts.put(Parameters.UnclassifiedTermsLogicalname, allTermsWithoutBTsorNTs);
        } else {
            //check one by one no dublicates should exist
            descriptorNts.get(Parameters.UnclassifiedTermsLogicalname).addAll(allTermsWithoutBTsorNTs);
        }

        allTermsHavingBTs.addAll(allTermsWithoutBTsorNTs);
    }

    public Hashtable<String, Vector<String>> readNextLevelSetTermsAndBts(Vector<String> currentLevel, Hashtable<String, Vector<String>> allNts, Vector<String> parsedTerms) {

        Hashtable<String, Vector<String>> nextLevelSet_Terms_and_Bts = new Hashtable<String, Vector<String>>();

        for (int i = 0; i < currentLevel.size(); i++) {

            String currentTerm = currentLevel.get(i);
            //System.out.print("currentTerm: "+currentTerm + " with nts: ");
            if (parsedTerms.contains(currentTerm)) {
                continue;
            } else {
                parsedTerms.add(currentTerm);
            }
            Vector<String> nts = allNts.get(currentTerm);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"i= " + i + " currentTerm = " + currentTerm );
            if (nts == null) {
                continue;
            }

            while (nts.contains(currentTerm)) {
                nts.remove(currentTerm);
            }

            //Utils.StaticClass.webAppSystemOutPrintln(nts.toString());
            for (int k = 0; k < nts.size(); k++) {

                String currentNt = nts.get(k);


                /* BUG Not NEEDED A->B and C->D->B 
                A,C top terms diffenrent 
                In this case B would not be created as a child of D parsed terms works well in case of cyrcles
                if (currentLevel.contains(currentNt)) {
                    continue;
                }*/

                if (nextLevelSet_Terms_and_Bts.containsKey(currentNt) == false) {
                    Vector<String> bts = new Vector<String>();
                    bts.add(currentTerm);
                    nextLevelSet_Terms_and_Bts.put(currentNt, bts);

                } else {
                    if (nextLevelSet_Terms_and_Bts.get(currentNt).contains(currentTerm) == false) {
                        nextLevelSet_Terms_and_Bts.get(currentNt).add(currentTerm);
                    }
                }

            }
        }



        return nextLevelSet_Terms_and_Bts;

    }

    public void specifyOrphansStatus(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, StringObject resultObj) {

        DBFilters dbf = new DBFilters();
        DBGeneral dbGen = new DBGeneral();
        DBConnect_Term dbCon = new DBConnect_Term();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();


        // FILTER default status for term creation depending on user group
        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject orphanTermObj = new StringObject(prefix.concat(Parameters.UnclassifiedTermsLogicalname));
        dbCon.CreateModifyStatus(SessionUserInfo.selectedThesaurus, orphanTermObj, dbf.GetDefaultStatusForTermCreation(SessionUserInfo), Q, TA, sis_session, tms_session, dbGen, resultObj);

    }

    private boolean importMoreHierarchiesFromTopTerms(UserInfoClass SessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String importThesaurusName, Vector<String> topTerms, Locale targetLocale, OutputStreamWriter logFileWriter, StringObject resultObj) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "String creation of additional hierarchies - specified by terms without bt but not declared as hierarchies. Time: " + Utilities.GetNow());
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        //Before any addition to merged thesaurus find out which facet is used for Unclassified terms --> UNCLASSIFIED TERMS
        //in order to instanciate possible hierarchies that do not belong in any Facet
        String defaultFacet = new String(dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, importThesaurusName));

        Hashtable<String, Vector<String>> hierFacetsPairsOfNewThesaurus = new Hashtable<String, Vector<String>>();
        Vector<String> facets = new Vector<String>();
        facets.add(defaultFacet);
        for (int i = 0; i < topTerms.size(); i++) {
            hierFacetsPairsOfNewThesaurus.put(topTerms.get(i), facets);
        }
        //try {
            if (dbMerge.CreateHierarchies(SessionUserInfo, Q, TA, sis_session, tms_session, importThesaurusName, defaultFacet, targetLocale, resultObj, logFileWriter, hierFacetsPairsOfNewThesaurus) == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "The creation of additional hierarchies failed.");
                return false;
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of additional hierarchies.");

        //} catch (IOException ex) {
            //Logger.getLogger(DBImportData.class.getName()).log(Level.SEVERE, null, ex);
          //  Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "import hierararchies error:" + ex.getMessage());
            //Utils.StaticClass.handleException(ex);
        //}
        topTerms.clear();
        return true;
    }

    private boolean importTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML, String importThesaurusName,
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            StringObject resultObj,
            Vector<Hashtable<String, Vector<String>>> allLevelsOfImportThes,
            Hashtable<String, Vector<String>> descriptorRts,
            Hashtable<String, Vector<String>> descriptorUfs,
            OutputStreamWriter logFileWriter) {


        DBMergeThesauri dbMerge = new DBMergeThesauri();



        long startTime;
        long elapsedTimeMillis;
        float elapsedTimeSec;


        try{
        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Descriptors ...">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of TERMS. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (dbMerge.CreateTermsLevelByLevel(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, " " + importThesaurusName, null, importThesaurusName, logFileWriter, resultObj, allLevelsOfImportThes, null, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Term creation operation failed.");
            return false;
        }
        allLevelsOfImportThes.clear();
        allLevelsOfImportThes = null;
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\nEnd of terms creation in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="rt ...">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of RTs. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (dbMerge.CreateRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, resultObj, descriptorRts, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Creation of RTS failed.");
            return false;
        }
        descriptorRts.clear();
        descriptorRts = null;
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\nEnd of RTs creation in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="uf ...">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of UFs. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (dbMerge.CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, ConstantParameters.uf_kwd, new Vector<String>(), resultObj, descriptorUfs, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Creation of UFs FAILED.");
            return false;
        }
        descriptorUfs.clear();
        descriptorUfs = null;
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of UFS in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Statuses ...">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting updating the status of terms. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (importStatuses(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, termsInfo, resultObj, logFileWriter, true) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "The Status update procedure FAILED.");
            return false;
        }
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of updating the status of terms in: " + elapsedTimeSec + " min.");
        //</editor-fold>


        //clear some memory

        Enumeration<String> termsEnum = termsInfo.keys();
        while (termsEnum.hasMoreElements()) {
            String targetTerm = termsEnum.nextElement();
            NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
            if (targetInfo.descriptorInfo.containsKey(ConstantParameters.bt_kwd)) {
                targetInfo.descriptorInfo.remove(ConstantParameters.bt_kwd);
            }
            if (targetInfo.descriptorInfo.containsKey(ConstantParameters.nt_kwd)) {
                targetInfo.descriptorInfo.remove(ConstantParameters.nt_kwd);
            }
            if (targetInfo.descriptorInfo.containsKey(ConstantParameters.rt_kwd)) {
                targetInfo.descriptorInfo.remove(ConstantParameters.rt_kwd);
            }
            if (targetInfo.descriptorInfo.containsKey(ConstantParameters.uf_kwd)) {
                targetInfo.descriptorInfo.remove(ConstantParameters.uf_kwd);
            }
            if (targetInfo.descriptorInfo.containsKey(ConstantParameters.status_kwd)) {
                targetInfo.descriptorInfo.remove(ConstantParameters.status_kwd);
            }
        }

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Simple term links --> en, uk_uf,primary_found_in, translations_found_in ...">
        String[] readNodes = {ConstantParameters.translation_kwd,
            ConstantParameters.uf_translations_kwd,
            ConstantParameters.tc_kwd,
            ConstantParameters.primary_found_in_kwd,
            ConstantParameters.translations_found_in_kwd};
        //uf not in this list as we already had read uf links
        for (int i = 0; i < readNodes.length; i++) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of: " + readNodes[i] + " Time: " + Utilities.GetNow()); //data may also be found as attributes of nodes
            startTime = System.currentTimeMillis();
            if (importSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, pathToErrorsXML, termsInfo, resultObj, logFileWriter, readNodes[i]) == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Insert opearion of node: " + readNodes[i] + " failed.");
                return false;
            }
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of: " + readNodes[i] + " in: " + elapsedTimeSec + " min.");
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        }
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Comment categories SN, tr_SN, HN ...">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of comment categories SN, tr_SN, HN. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (importCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, pathToErrorsXML, termsInfo, resultObj, logFileWriter) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "The creation of comment categories  SN, tr_SN, HN failed.");
            return false;
        }
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of comment categories  SN, tr_SN, HN in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Created By / ON ...">
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of Created BY / ON fields. Time: " + Utilities.GetNow());
        logFileWriter.flush();
        startTime = System.currentTimeMillis();
        if (importDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, ConstantParameters.created_by_kwd, importThesaurusName, termsInfo, resultObj, logFileWriter) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "The creation of Created BY / ON fields FAILED.");
            return false;
        }
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\"End of creation of Created BY / ON fields in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="Modified By / ON ...">
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of Modified BY / ON fields. Time: " + Utilities.GetNow());
        logFileWriter.flush();
        startTime = System.currentTimeMillis();
        if (importDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, ConstantParameters.modified_by_kwd, importThesaurusName, termsInfo, resultObj, logFileWriter) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "The creation of Modified BY / ON fields FAILED.");
            return false;
        }
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of Modified BY / ON fields in: " + elapsedTimeSec + " min.");
        //</editor-fold>

        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass().toString());
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
    }

    private boolean importStatuses(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName, Hashtable<String, NodeInfoStringContainer> termsInfo,
            StringObject resultObj, OutputStreamWriter logFileWriter, boolean resolveError) throws IOException {

        DBFilters dbF = new DBFilters();
        DBGeneral dbGen = new DBGeneral();

        String pathToMessagesXML = Utilities.getMessagesXml();

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Hashtable<String, String> thesaurus1_statuses = new Hashtable<String, String>();
        Hashtable<String, Long> merged_thesaurus_status_classIds = new Hashtable<String, Long>();

        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);


        Vector<String> errorArgs = new Vector<String>();
        //Read Statuses defined in XML
        Enumeration<String> termEnum = termsInfo.keys();
        while (termEnum.hasMoreElements()) {
            String targetTerm = termEnum.nextElement();
            /*
            try {
                byte[] byteArray = targetTerm.getBytes("UTF-8");

                int maxTermChars = dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {
                    if (resolveError) {
                        StringObject warningMsg = new StringObject();
                        errorArgs.clear();
                        errorArgs.add(targetTerm);
                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                        try {
                            logFileWriter.append("\r\n<targetTerm>");
                            logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                            logFileWriter.append("<errorType>" + "name" + "</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(targetTerm) + "</errorValue>");
                            logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                        } catch (IOException ex) {
                            Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                            Utils.StaticClass.handleException(ex);
                        }
                        continue;

                    } else {

                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(resultObj, "root/EditTerm/Creation/LongName", errorArgs, pathToMessagesXML);
                        return false;
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
*/
            Vector<String> targetStatuses = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.status_kwd);
            if (targetStatuses.size() > 0) {
                String targetStatus = statusGRtoDBmapping(SessionUserInfo, targetStatuses.get(0));
                thesaurus1_statuses.put(targetTerm, targetStatus);
            }

        }

        //for all other terms just set status to its default values for user
        String defaultStatusForUser = dbF.GetDefaultStatusForTermCreation(SessionUserInfo);
        defaultStatusForUser = statusGRtoDBmapping(SessionUserInfo, defaultStatusForUser);


        Q.reset_name_scope();
        StringObject parentThesaurusStatusObj = new StringObject();
        dbtr.getThesaurusClass_StatusOfTerm(SessionUserInfo.selectedThesaurus, parentThesaurusStatusObj);

        Q.set_current_node(parentThesaurusStatusObj);
        int set_allstatuses = Q.get_subclasses(0);
        Q.reset_set(set_allstatuses);
        Vector<String> readStatuses = new Vector<String>();
        readStatuses.addAll(dbGen.get_Node_Names_Of_Set(set_allstatuses, false, Q, sis_session));
        Q.free_set(set_allstatuses);

        for (int i = 0; i < readStatuses.size(); i++) {
            Q.reset_name_scope();
            long statusIDL = Q.set_current_node(new StringObject(readStatuses.get(i)));
            if (statusIDL != QClass.APIFail) {
                merged_thesaurus_status_classIds.put(readStatuses.get(i), statusIDL);
            } else {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Could not access status class with name " + readStatuses.get(i));
            }
        }

        //Now for all terms that status is not declared in file declare default status
        Q.reset_name_scope();
        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        int set_allTerms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
        Q.reset_set(set_allTerms);

        Vector<String> allDBTerms = dbGen.get_Node_Names_Of_Set(set_allTerms, true, Q, sis_session);
        Q.free_all_sets();

        for (int i = 0; i < allDBTerms.size(); i++) {
            String testTerm = allDBTerms.get(i);

            if (thesaurus1_statuses.containsKey(testTerm) == false) {
                thesaurus1_statuses.put(testTerm, defaultStatusForUser);
            }
        }

        return dbMerge.CreateStatuses(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                importThesaurusName, logFileWriter, resultObj, thesaurus1_statuses,
                new Hashtable<String, String>(), merged_thesaurus_status_classIds);

    }

    private boolean importSimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName, String pathToErrorsXML, Hashtable<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter, String keyWord) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        Hashtable<String, Vector<String>> term_Links_HASH = new Hashtable<String, Vector<String>>();

        //Read Statuses defined in XML
        Enumeration<String> termEnum = termsInfo.keys();
        while (termEnum.hasMoreElements()) {
            String targetTerm = termEnum.nextElement();
            Vector<String> targettermLinks = termsInfo.get(targetTerm).descriptorInfo.get(keyWord);
            term_Links_HASH.put(targetTerm, targettermLinks);
        }

        return dbMerge.CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, keyWord, new Vector<String>(), resultObj, term_Links_HASH, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);

    }

    private boolean importCommentCategories(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName, String pathToErrorsXML, Hashtable<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        Hashtable<String, String> scope_notes_HASH = new Hashtable<String, String>();
        Hashtable<String, String> scope_notes_EN_HASH = new Hashtable<String, String>();
        Hashtable<String, String> historical_notes_HASH = new Hashtable<String, String>();


        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        Enumeration<String> termsIterator = termsInfo.keys();
        while (termsIterator.hasMoreElements()) {

            String targetDescriptor = termsIterator.nextElement();
            NodeInfoStringContainer targetDescriptorInfo = termsInfo.get(targetDescriptor);

            String targetScopeNote = new String("");
            String targetScopeNoteEn = new String("");
            String targetHistoricalNote = new String("");
            Vector<String> snNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
            Vector<String> snEnNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
            Vector<String> hnNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);

            if (snNodes.size() > 0) {
                targetScopeNote = snNodes.get(0);
                if (targetScopeNote != null && targetScopeNote.length() > 0) {
                    scope_notes_HASH.put(targetDescriptor, targetScopeNote);
                }
            }

            if (snEnNodes.size() > 0) {
                for (int k = 0; k < snEnNodes.size(); k++) {
                    if (k > 0) {
                        targetScopeNoteEn += "\n";
                    }
                    targetScopeNoteEn += snEnNodes.get(k);
                }
                scope_notes_EN_HASH.put(targetDescriptor, targetScopeNoteEn);
            }

            if (hnNodes.size() > 0) {
                targetHistoricalNote = hnNodes.get(0);
                if (targetHistoricalNote != null && targetHistoricalNote.length() > 0) {
                    historical_notes_HASH.put(targetDescriptor, targetHistoricalNote);
                }
            }

        }

        return dbMerge.CreateCommentCategories(refSessionUserInfo, common_utils, Q, TA,
                sis_session, tms_session, importThesaurusName, scope_notes_HASH,
                scope_notes_EN_HASH, historical_notes_HASH, logFileWriter,
                pathToErrorsXML, resultObj, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);
    }

    private boolean importDatesAndEditors(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String editorKeyWordStr, String importThesaurusName,
            Hashtable<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        Hashtable<String, Vector<String>> term_Editor_Links_THES1_HASH = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> term_Date_Links_THES1_HASH = new Hashtable<String, Vector<String>>();


        String dateKeyWordStr = new String("");
        if (editorKeyWordStr.compareTo(ConstantParameters.created_by_kwd) == 0) {
            dateKeyWordStr = ConstantParameters.created_on_kwd;
        } else if (editorKeyWordStr.compareTo(ConstantParameters.modified_by_kwd) == 0) {
            dateKeyWordStr = ConstantParameters.modified_on_kwd;
        }

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        Enumeration<String> parseAllTerms = termsInfo.keys();
        while (parseAllTerms.hasMoreElements()) {
            String targetDescriptor = parseAllTerms.nextElement();
            NodeInfoStringContainer targetDescriptorInfo = termsInfo.get(targetDescriptor);

            Vector<String> EditorVec = new Vector<String>();
            EditorVec.addAll(targetDescriptorInfo.descriptorInfo.get(editorKeyWordStr));
            Vector<String> DatesVec = new Vector<String>();
            DatesVec.addAll(targetDescriptorInfo.descriptorInfo.get(dateKeyWordStr));

            int howmanyDirectEditorLinks = EditorVec.size();
            int howmanyDirectDateLinks = DatesVec.size();
            /*
            //Fill term_Date_Links_THES1_HASH and term_Editor_Links_THES1_HASH with all values found. No correlation is supported
            //and no gurantee that dates and editor will always come in pairs. Thus just copy what you see and just check
            //if same values are declared in order to skip them
             */
            //<editor-fold defaultstate="collapsed" desc="read editors ...">
            for (int k = 0; k < howmanyDirectEditorLinks; k++) {
                String targetLink = EditorVec.get(k);
                targetLink = readXMLTag(targetLink);
                if (targetLink == null || targetLink.length() == 0) {
                    continue;
                }
                Vector<String> currentLinks = term_Editor_Links_THES1_HASH.get(targetDescriptor);

                if (currentLinks == null) {
                    Vector<String> newLinksVec = new Vector<String>();
                    newLinksVec.add(targetLink);
                    term_Editor_Links_THES1_HASH.put(targetDescriptor, newLinksVec);
                } else {
                    if (currentLinks.contains(targetLink) == false) {
                        currentLinks.add(targetLink);
                        term_Editor_Links_THES1_HASH.put(targetDescriptor, currentLinks);
                    }
                }
            }

            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="read dates ...">
            for (int k = 0; k < howmanyDirectDateLinks; k++) {
                String targetLink = DatesVec.get(k);
                targetLink = readXMLTag(targetLink);
                if (targetLink == null || targetLink.length() == 0) {
                    continue;
                }
                Vector<String> currentLinks = term_Date_Links_THES1_HASH.get(targetDescriptor);

                if (currentLinks == null) {
                    Vector<String> newLinksVec = new Vector<String>();
                    newLinksVec.add(targetLink);
                    term_Date_Links_THES1_HASH.put(targetDescriptor, newLinksVec);
                } else {
                    if (currentLinks.contains(targetLink) == false) {
                        currentLinks.add(targetLink);
                        term_Date_Links_THES1_HASH.put(targetDescriptor, currentLinks);
                    }
                }
            }

            //</editor-fold>

        }

        //Utils.StaticClass.webAppSystemOutPrintln("term_Editor_Links_THES1_HASH size: " + term_Editor_Links_THES1_HASH.size());
        //Utils.StaticClass.webAppSystemOutPrintln("term_Date_Links_THES1_HASH size: " + term_Date_Links_THES1_HASH.size());

        return dbMerge.CreateDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, " ", null, importThesaurusName, logFileWriter,
                term_Editor_Links_THES1_HASH, term_Date_Links_THES1_HASH, new Hashtable<String, Vector<String>>(), new Hashtable<String, Vector<String>>(), editorKeyWordStr, resultObj);
    }

    public String statusGRtoDBmapping(UserInfoClass SessionUserInfo, String greekStatus) {

        DBFilters dbF = new DBFilters();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject statusObj = new StringObject();

        if (greekStatus.compareTo(Parameters.Status_For_Insertion) == 0) {

            dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus, statusObj);
            return statusObj.getValue();
        } else if (greekStatus.compareTo(Parameters.Status_Under_Construction) == 0) {

            dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus, statusObj);
            return statusObj.getValue();
        } else if (greekStatus.compareTo(Parameters.Status_For_Approval) == 0) {

            dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus, statusObj);
            return statusObj.getValue();
        } else if (greekStatus.compareTo(Parameters.Status_Approved) == 0) {

            dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, statusObj);
            return statusObj.getValue();
        } else {
            return dbF.GetDefaultStatusForTermCreation(SessionUserInfo);
        }
    }

    public String readXMLTag(String test) {
        if (test == null) {
            return null;
        }
        String newStr = test.replaceAll("\u00A0", " ");
        newStr = newStr.replaceAll(" +", " ");
        newStr = newStr.trim();
        return newStr;

    }

    private boolean readAndSyncronizeTranslationCategories(String selectedThesaurus, StringObject resultMessageStrObj, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            Vector<String> userSelectedTranslationWords,
            Vector<String> userSelectedTranslationIdentifiers,
            Hashtable<String, String> userSelections) {

        DBGeneral dbGen = new DBGeneral();
        String pathToMessagesXML = Utilities.getMessagesXml();


        Hashtable<String, String> currentTranslationCategories = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, selectedThesaurus, null, false, true);

        return dbGen.synchronizeTranslationCategories(currentTranslationCategories,
                userSelections, userSelectedTranslationWords, userSelectedTranslationIdentifiers, selectedThesaurus,
                resultMessageStrObj, pathToMessagesXML, Q, TA, sis_session, tms_session);
    }

    private String getPageContentsXsl() {
        return Parameters.BaseRealPath + File.separator + "xml-xsl" + File.separator + "page_contents.xsl";
    }
}
