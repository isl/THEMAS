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
import java.util.Locale;
import java.io.OutputStreamWriter;
import neo4j_sisapi.TMSAPIClass;

import neo4j_sisapi.*;

import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
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
        ArrayList<String> parsedTermNames = new ArrayList<>();

        ParseFileData parser = new ParseFileData();
        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();
        
        try {
            String importSchemaName = ConstantParameters.xmlschematype_THEMAS;
            if (parser.readXMLTerms(xmlFilePath, importSchemaName, termsInfo, null)) {
                termsInfo.keySet().stream().forEach((targetTerm) -> {
                    if (parsedTermNames.contains(targetTerm) == false) {
                        parsedTermNames.add(targetTerm);
                    }
                });
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
            ArrayList<String> btOfTerms = new ArrayList<>();
            btOfTerms.add(targetHierarchy);
            
            StringObject resultMessageObj = new StringObject();
            StringObject resultMessageObj_2 = new StringObject();
            
            Q.reset_name_scope();
            if (Q.set_current_node(new StringObject(prefixTerm.concat(targetHierarchy))) == QClass.APIFail) {
                
                resultMessageObj.setValue(u.translateFromMessagesXML("root/importTermsUnderHierarchy/FailureFindHierarchy", new String[]{Utilities.escapeXML(targetHierarchy)}));
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
                
                resultMessageObj_2.setValue(u.translateFromMessagesXML("root/importTermsUnderHierarchy/targetUITermAlreadyExists", new String[]{targetUITerm,targetHierarchy}));
                resultObj.setValue(resultMessageObj_2.getValue());
                
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
                } 
                    
                long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(termsInfo.get(targetUITerm));

                if(refId >0){
                    //check if it exists
                    String existingTerm = Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus, refId);
                    if(existingTerm!=null && existingTerm.trim().length()>0) {
                        existingTerm = dbGen.removePrefix(existingTerm);
                        if(!existingTerm.equals(targetUITerm)){
                           
                           logFileWriter.append("<targetTerm>");
                           logFileWriter.append("<name>" + Utilities.escapeXML(targetUITerm) + "</name>");
                           logFileWriter.append("<errorType>" + ConstantParameters.system_referenceIdAttribute_kwd + "</errorType>");
                           logFileWriter.append("<errorValue>" + refId + "</errorValue>");
                           logFileWriter.append("<reason>" +u.translateFromMessagesXML("root/importTermsUnderHierarchy/NewThesaurusReferenceId", new String[]{""+refId,targetUITerm,existingTerm,targetUITerm,SessionUserInfo.selectedThesaurus})+ "</reason>");
                           //logFileWriter.append("<reason>The Term '" + targetUITerm + "' already exists in database and it did not change (to be essentially NT of Term: '" + Utilities.escapeXML(targetHierarchy) + "'.</reason>");
                           logFileWriter.append("</targetTerm>");
                           refId = -1; 
                        }
                        else{
                            continue;
                        }
                    }
                    
                }
                
                SortItem newNameObj = new SortItem(targetUITerm,-1,Utilities.getTransliterationString(targetUITerm, false),refId);

                creation_modificationOfTerm.createNewTermSortItem(SessionUserInfo, 
                        newNameObj, btOfTerms, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, false, true, logFileWriter, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);

                if (resultObj.getValue().length() > 0) {
                    returnVal = false;
                    return false;
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
        }
        return returnVal;
    }

    //this function will find out all the thesaurusReferenceIds that have been used by searching in facets and terms
    private long findThesaurusMaxRefernceId(ArrayList<SortItem> xmlFacets, HashMap<String, NodeInfoStringContainer> termsInfo){
        long retVal = -1;
        
        for(SortItem facetSortItem : xmlFacets){
            if(facetSortItem.getThesaurusReferenceId()>retVal){
                retVal = facetSortItem.getThesaurusReferenceId();
            }
        }
        Iterator<String> termEnum = termsInfo.keySet().iterator();
        
            
        while (termEnum.hasNext()) {
            
            String targetTerm = termEnum.next();
            long compareVal = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(termsInfo.get(targetTerm));
            if(compareVal>retVal){
                retVal = compareVal;
            } 
        }

        
        return retVal;
    }
    
    public boolean writeThesaurusDataFromSortItems(UserInfoClass refSessionUserInfo, 
                                                    CommonUtilsDBadmin common_utils,
                                                    QClass Q, 
                                                    TMSAPIClass TA, 
                                                    IntegerObject sis_session, 
                                                    IntegerObject tms_session,
                                                    ArrayList<SortItem> xmlFacets, 
                                                    ArrayList<String> guideTerms, 
                                                    HashMap<String, String> XMLsources,
                                                    HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
                                                    HashMap<String, ArrayList<String>> hierarchyFacets,
                                                    HashMap<String, NodeInfoStringContainer> termsInfo,
                                                    ArrayList<String> userSelectedTranslationWords,
                                                    ArrayList<String> userSelectedTranslationIdentifiers,
                                                    HashMap<String, String> userSelections,
                                                    ArrayList<SortItem> topTerms,
                                                    HashMap<String, ArrayList<String>> descriptorRts,
                                                    HashMap<String, ArrayList<SortItem>> descriptorUfs,
                                                    ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes,
                                                    String importThesaurusName,
                                                    String pathToErrorsXML, 
                                                    Locale targetLocale,
                                                    StringObject resultObj, 
                                                    OutputStreamWriter logFileWriter) {
        

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        UsersClass webappusers = new UsersClass();
        
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of new thesaurus creation: " + importThesaurusName + ".");

        Q.reset_name_scope();
        if (readAndSyncronizeTranslationCategories(importThesaurusName, resultObj, Q, TA, sis_session, tms_session,
                userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections) == false) {
            return false;
        }

        long maxExistingRefId = findThesaurusMaxRefernceId(xmlFacets, termsInfo);
        if(maxExistingRefId>0){
            if(TA.resetCounter_For_ThesarusReferenceId(importThesaurusName,maxExistingRefId)==QClass.APIFail){
                Utils.StaticClass.webAppSystemOutPrintln("Setting Max Thesaurus reference Id Failed for thesaurus: " + importThesaurusName);
                return false;
            }
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
        //since Unclassified terms is affected by the create thesaurus functionality 
        //concerning its status its creator and its creation date
        //one should check if termsInfo contains both creator and creation date. 
        //If this is the case then current creation links created by Created Thesaurus should be removed
        if(termsInfo.containsKey(Parameters.UnclassifiedTermsLogicalname)){
            HashMap<String,ArrayList<String>> container  = termsInfo.get(Parameters.UnclassifiedTermsLogicalname).descriptorInfo;
            if(container!=null &&
                    container.containsKey(ConstantParameters.created_by_kwd) && 
                    container.get(ConstantParameters.created_by_kwd).size()>0 && 
                    
                    container.containsKey(ConstantParameters.created_on_kwd) && 
                    container.get(ConstantParameters.created_on_kwd).size()>0 ){
                
                DBGeneral dbGen = new DBGeneral();          
                DBConnect_Term dbCon = new DBConnect_Term();
                
                StringObject createdOnClass = new StringObject();
                StringObject createdOnLink = new StringObject();
                StringObject createdByClass = new StringObject();
                StringObject createdByLink = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_by_kwd, createdByClass, createdByLink, Q, sis_session);
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClass, createdOnLink, Q, sis_session);
                
                StringObject errorMsg = new StringObject("");
                
                dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, Parameters.UnclassifiedTermsLogicalname, ConstantParameters.FROM_Direction, createdByClass.getValue(), createdByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                if(errorMsg.getValue().length()>0){
                     Utils.StaticClass.webAppSystemOutPrintln("Failed to remove Previous created By links of: " + Parameters.UnclassifiedTermsLogicalname);
                     return false;
                }
                dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, Parameters.UnclassifiedTermsLogicalname, ConstantParameters.FROM_Direction, createdOnClass.getValue(), createdOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                if(errorMsg.getValue().length()>0){
                     Utils.StaticClass.webAppSystemOutPrintln("Failed to remove Previous created On links of: " + Parameters.UnclassifiedTermsLogicalname);
                     return false;
                }
            }
        }

        Q.reset_name_scope();
        //common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, importThesaurusName);

        if (CreateSources(SessionUserInfo.selectedThesaurus, common_utils, importThesaurusName,
                Q, TA, sis_session, tms_session, XMLsources, resultObj, logFileWriter) == false) {
            return false;
        }
        
        // Step9 Create Facets specified by XML
        if (dbMerge.CreateFacetsFromSortItemsVector(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, xmlFacets, resultObj,true,logFileWriter,ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            return false;
        }

        SortItem defaultFacetSortItem = dbMerge.getDefaultFacetSortItem(SessionUserInfo, Q, sis_session, importThesaurusName);
        
        //retrieve from termInfo the TopTerm Reference id and the transliteration values used
        HashMap<SortItem,ArrayList<String>> hierarchyFacetSortItems = new HashMap<SortItem,ArrayList<String>>();
        for(String hierarchy : hierarchyFacets.keySet()){
            String transliteration = "";
            long refId = -1;
            NodeInfoStringContainer targetTopTermInfo = null;
            
            if(termsInfo.containsKey(hierarchy)){
                 targetTopTermInfo= termsInfo.get(hierarchy);                
            }
            transliteration = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(targetTopTermInfo, hierarchy, false);
            refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(targetTopTermInfo);
            hierarchyFacetSortItems.put(new SortItem(hierarchy,-1,transliteration,refId), hierarchyFacets.get(hierarchy));
        }
        
        // Step10 Create Hierarchies specified by XML
        if (dbMerge.CreateHierarchiesFromSortItems(SessionUserInfo, Q, TA, sis_session, tms_session,
                importThesaurusName, defaultFacetSortItem, targetLocale, resultObj, hierarchyFacetSortItems,true,logFileWriter,ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            return false;
        }

        // Step11 Create Hierarchies specified by topterms of Step2
        if (importMoreHierarchiesFromTopTermsInsortItems(SessionUserInfo, Q, TA, sis_session, tms_session, importThesaurusName, topTerms, targetLocale, resultObj,true,logFileWriter,ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
            return false;
        }

        // Step12 Create Terms
        if (importTermsInSortItems(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                        pathToErrorsXML, 
                        importThesaurusName, 
                        termsInfo, 
                        resultObj, 
                        allLevelsOfImportThes,
                        descriptorRts, 
                        descriptorUfs, 
                        logFileWriter) == false) {
            return false;
        }

        //Step 13 Guide Terms Addition Patch
        if (dbMerge.CreateGuideTerms(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, guideTerms, XMLguideTermsRelations, importThesaurusName, resultObj) == false) {
            return false;
        }

        return true;
    }

    
    public boolean writeThesaurusData(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            ArrayList<SortItem> xmlFacetsInSortItems, ArrayList<String> guideTerms, HashMap<String, String> XMLsources,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            HashMap<String, ArrayList<String>> hierarchyFacets,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> userSelectedTranslationWords,
            ArrayList<String> userSelectedTranslationIdentifiers,
            HashMap<String, String> userSelections,
            ArrayList<String> topTerms, HashMap<String, ArrayList<String>> descriptorRts,
            HashMap<String, ArrayList<String>> descriptorUfs,
            ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes,
            String importThesaurusName,
            String pathToErrorsXML, Locale targetLocale,
            StringObject resultObj, OutputStreamWriter logFileWriter) {
        

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        UsersClass webappusers = new UsersClass();
        DBGeneral dbGen = new DBGeneral();
        String pathToMessagesXML = Utilities.getXml_For_Messages();
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
        if (dbMerge.CreateFacetsFromSortItemsVector(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, xmlFacetsInSortItems, resultObj,true,logFileWriter,ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
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
        
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        UsersClass webappusers = new UsersClass();

        String initiallySelectedThesaurus = refSessionUserInfo.selectedThesaurus;


        ArrayList<String> thesauriNames = new ArrayList<String>();

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
            
            
            logFileWriter.append("<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\r\n");
            logFileWriter.append("<title>"+u.translateFromMessagesXML("root/MergeThesauri/ReportTitle", new String[]{thesaurusName1,thesaurusName2,targetThesaurusName,time})+"</title>\r\n"
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

        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(SessionUserInfo, dbGen, config, common_utils, targetThesaurusName, targetThesaurusName, thesauriNames,
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
        ArrayList<String> xmlFacets = new ArrayList<String>();
        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();

        ArrayList<String> guideTerms = new ArrayList<String>();
        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        ArrayList<String> topTerms = new ArrayList<String>();
        HashMap<String, ArrayList<String>> descriptorRts = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> descriptorUfs = new HashMap<String, ArrayList<String>>();
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = new ArrayList<HashMap<String, ArrayList<String>>>();


        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();
        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> translationCategories = new HashMap<String, String>();



        String defaultFacet = dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, targetThesaurusName);

        //read Translation Categories
        translationCategories = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, thesaurusName1, thesaurusName2, false, true);
        Iterator<String> trEnum = translationCategories.keySet().iterator();
        while (trEnum.hasNext()) {
            String word = trEnum.next();
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
            ArrayList<String> unclassifiedFacets = new ArrayList<String>();
            unclassifiedFacets.add(defaultFacet);
            hierarchyFacets.put(Parameters.UnclassifiedTermsLogicalname, unclassifiedFacets);
        }


        //readTermsInfo and guide terms
        dbMerge.ReadThesaurusTerms(refSessionUserInfo, Q,TA, sis_session, thesaurusName1, thesaurusName2,
                termsInfo, guideTerms, XMLguideTermsRelations);

        guideTerms.clear();

        //readGuideTerms

        guideTerms.addAll(dbGen.collectGuideLinks(thesaurusName1,Q, sis_session));
        ArrayList<String> guideTermsThes2 = dbGen.collectGuideLinks(thesaurusName2,Q, sis_session);
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

        //No need to check lengths any more
        /*
		try {
            this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                    targetThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);

        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln("Exception Caught: " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }*/
        // Step3 Read XML file in order to fill basic datastructures concerning terms
        // Step4 Process these data structures in order to define topterms and orphans
        //filling all structures passed as parameters except xmlFilePath parameter and then process data in order to classify terms in levels

        processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);

        ArrayList<SortItem> xmlFacetSortItems = Utilities.getSortItemVectorFromStringVector(xmlFacets,false);
        try {
            if (writeThesaurusData(SessionUserInfo, common_utils,
                    Q, TA, sis_session, tms_session,
                    xmlFacetSortItems, guideTerms, XMLsources, XMLguideTermsRelations,
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

                logFileWriter.append("\r\n<creationInfo>"+u.translateFromMessagesXML("root/MergeThesauri/ReportSuccessMessage", new String[]{thesaurusName1,thesaurusName2,targetThesaurusName,((Utilities.stopTimer(startTime)) / 60)+"" })+"</creationInfo>\r\n");
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

        ArrayList<String> thesauriNames = new ArrayList<String>();

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
        ArrayList<String> xmlFacets = new ArrayList<String>();
        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();

        ArrayList<String> guideTerms = new ArrayList<String>();
        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        ArrayList<String> topTerms = new ArrayList<String>();
        HashMap<String, ArrayList<String>> descriptorRts = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> descriptorUfs = new HashMap<String, ArrayList<String>>();
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = new ArrayList<HashMap<String, ArrayList<String>>>();


        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();
        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> translationCategories = new HashMap<String, String>();

        ArrayList<String> thesaurusVector = new ArrayList<String>();
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
            ArrayList<String> unclassifiedFacets = new ArrayList<String>();
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

        ArrayList<String> thesauriNames = new ArrayList<String>();

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
            logFileWriter.append("<title>"+u.translateFromMessagesXML("root/CopyThesauri/ReportTitle", new String[]{sourceThesaurusName, targetThesaurusName, time}) + "</title>\r\n"
            //logFileWriter.append("<title>Report of copy thesaurus operation of thesaurus " + sourceThesaurusName + " to thesaurus " + targetThesaurusName + ". Time: " + time + "</title>\r\n"
            
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

        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(SessionUserInfo, dbGen, config, common_utils, targetThesaurusName, targetThesaurusName, thesauriNames,
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
        
        QClass Q = new neo4j_sisapi.QClass(); 
        TMSAPIClass TA = new TMSAPIClass();
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
            ArrayList<SortItem> xmlFacetSortItems = new ArrayList<>();
            HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<>();

            ArrayList<String> guideTerms = new ArrayList<>();
            HashMap<String, String> XMLsources = new HashMap<>();
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<>();

            ArrayList<String> topTerms = new ArrayList<>();
            HashMap<String, ArrayList<String>> descriptorRts = new HashMap<>();
            HashMap<String, ArrayList<String>> descriptorUfs = new HashMap<>();
            ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = new ArrayList<>();

            HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<>();
            ArrayList<String> userSelectedTranslationWords = new ArrayList<>();
            ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<>();
            HashMap<String, String> translationCategories = new HashMap<>();

            StringObject CreateThesaurusResultMessage = new StringObject("");

            SortItem defaultFacetSortItem = dbMerge.getDefaultFacetSortItem(SessionUserInfo, Q, sis_session, targetThesaurusName);

            //read Translation Categories
            translationCategories = dbGen.getThesaurusTranslationCategories(Q, TA, sis_session, sourceThesaurusName, null, false, true);
            Iterator<String> trEnum = translationCategories.keySet().iterator();
            while (trEnum.hasNext()) {
                String word = trEnum.next();
                String identifier = translationCategories.get(word);
            
                userSelectedTranslationWords.add(word);
                userSelectedTranslationIdentifiers.add(identifier);
            }            

            //read facets
            xmlFacetSortItems.addAll(dbMerge.ReadThesaurusFacetsInSortItems(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null));

            /*
            ArrayList<String> xmlFacets = Utilities.getStringVectorFromSortItemVector(xmlFacetSortItems);
            
            if (xmlFacets.contains(defaultFacetSortItem.getLogName()) == false) {
                xmlFacets.add(defaultFacetSortItem.getLogName());
                xmlFacetSortItems.add(defaultFacetSortItem);
            }*/

            //read hierarchies
            hierarchyFacets = dbMerge.ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null);
            if (hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
                ArrayList<String> unclassifiedFacets = new ArrayList<String>();
                unclassifiedFacets.add(defaultFacetSortItem.getLogName());
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

            //No need to check lengths any more 
            //this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
            //        targetThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);

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
                    xmlFacetSortItems, guideTerms, XMLsources, XMLguideTermsRelations,
                    hierarchyFacets, termsInfo, userSelectedTranslationWords,
                    userSelectedTranslationIdentifiers, translationCategories,
                    topTerms, descriptorRts, descriptorUfs,
                    allLevelsOfImportThes, targetThesaurusName,
                    pathToErrorsXML, targetLocale, resultObj, logFileWriter);

            
                if(ok){
                    //SUCESS
                    commitCopyActions(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, targetLocale,targetThesaurusName, out, Filename.concat(".html"));
                    if (logFileWriter != null) {
                        
                        logFileWriter.append("\r\n<creationInfo>"+u.translateFromMessagesXML("root/CopyThesauri/ReportSuccessMessage", new String[]{ (""+((Utilities.stopTimer(startTime)) / 60))}) + "</creationInfo>\r\n");
                        //logFileWriter.append("\r\n<creationInfo>Copy thesaurus operation completed successfully in: " + ((Utilities.stopTimer(startTime)) / 60) + " minutes.</creationInfo>\r\n");
                        
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

    public String getXMLMiddleForCopyThesaurus(CommonUtilsDBadmin common_utils, ArrayList thesaurusVector,
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

    public String getXMLMiddleForMergeThesaurus(CommonUtilsDBadmin common_utils, ArrayList thesaurusVector,
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

        UsersClass wtmsUsers = new UsersClass();
        StringBuffer xml = new StringBuffer();
        ArrayList<String> thesauriNames = new ArrayList<String>();
        ArrayList<String> allHierarchies = new ArrayList<String>();
        ArrayList<String> allGuideTerms = new ArrayList<String>();



        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);


        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        
        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        //xml.append("<mergewarnings>");
        //xml.append(mergeNotes);
        //xml.append("</mergewarnings>");
        xml.append("<mergeReportFile>");
        xml.append(reportFile);
        xml.append("</mergeReportFile>");
        xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, u.translateFromMessagesXML("root/commitMergeActions/MergeThesaurusSucceed", new String[]{mergedThesaurusName})));
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

        ArrayList<String> thesauriNames = new ArrayList<String>();
        ArrayList<String> allHierarchies = new ArrayList<String>();
        ArrayList<String> allGuideTerms = new ArrayList<String>();

        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);


        dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);
        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);     
        
        xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));
        xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies, allGuideTerms, targetLocale));
        xml.append("<copyReportFile>");
        xml.append(reportFile);
        xml.append("</copyReportFile>");
        xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames,new StringObject(u.translateFromMessagesXML("root/commitCopyActions/CopySucceed",null)), true));
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
        ArrayList<String> allHierarchies = new ArrayList<String>();
        ArrayList<String> allGuideTerms = new ArrayList<String>();
        ArrayList<String> thesauriNames = new ArrayList<String>();

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
        xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, u.translateFromMessagesXML("root/MergeThesauri/FailureMessage", null)+" " + resultObj.getValue()));
        //xml.append(getXMLMiddleForMergeThesaurus(common_utils, thesauriNames, "Failure of merge thesauri operation: " + resultObj.getValue()));
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

        ArrayList<String> thesauriNames = new ArrayList<String>();
        ArrayList<String> allHierarchies = new ArrayList<String>();
        ArrayList<String> allGuideTerms = new ArrayList<String>();


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
        xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, new StringObject(u.translateFromMessagesXML("root/CopyThesauri/FailureMessage",null)+" " + resultObj.getValue()), false));
        //xml.append(getXMLMiddleForCopyThesaurus(common_utils, thesauriNames, new StringObject("Failure of copy thesaurus operation: " + resultObj.getValue()), false));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());

        u.XmlPrintWriterTransform(out, xml, getPageContentsXsl());

        // ---------------------- UNLOCK SYSTEM ----------------------
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        dbAdminUtils.UnlockSystemForAdministrativeJobs();
    }

    public boolean thesaurusImportActions(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,boolean initializeDb,
            ConfigDBadmin config, Locale targetLocale, String pathToErrorsXML, String xmlFilePath,
            String xmlSchemaType, String importThesaurusName,
            String backUpDescription, StringObject DBbackupFileNameCreated,
            StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        boolean returnVal = false;
        
        DBGeneral dbGen = new DBGeneral();
        DBMergeThesauri dbMerge = new DBMergeThesauri();

        ParseFileData parser = new ParseFileData();

        //Structures to fill
        HashMap<String, SortItem> xmlFacetSortItems = new HashMap<String, SortItem>();
        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();
        
        ArrayList<String> guideTerms = new ArrayList<String>();
        HashMap<String, String> XMLsources = new HashMap<String, String>();        
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();
        
        //key should only be string (instead of SortItem) in case we do not have all information about uri and transliteration in every reference in the XML       
        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();
        
        
        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> userLanguageSelections = new HashMap<String, String>();

        ArrayList<String> thesaurusVector = new ArrayList<String>();
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
        if (parser.readXMLFacetsInSortItems(importThesaurusName, xmlFilePath, inputScheme, xmlFacetSortItems) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read FACETS.");
            processSucceded = false;
        }


        
        ArrayList<String> xmlFacetsInStrs = new ArrayList<>();
        if(!xmlFacetSortItems.isEmpty()){
            xmlFacetsInStrs.addAll(xmlFacetSortItems.keySet());
        }
            
        /* Step2 Read Hierarchies specified by XML************************************************/
        if (processSucceded && parser.readXMLHierarchies(importThesaurusName, xmlFilePath, inputScheme, hierarchyFacets, xmlFacetsInStrs) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read HIERARCHIES.");
            processSucceded = false;
        }
        
        //might not be defined any
        if (processSucceded) {
            parser.readTranslationCategories(xmlFilePath, inputScheme, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userLanguageSelections);
        }


        if (processSucceded && parser.readXMLTerms(xmlFilePath, inputScheme, termsInfo, userLanguageSelections) == false) {
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
        ArrayList<String> guideTerms, HashMap<String, String> XMLsources,
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
        HashMap<String, ArrayList<String>> hierarchyFacets,
        HashMap<String, NodeInfoStringContainer> termsInfo,
        OutputStreamWriter logFileWriter
         */

        //common_utils.RestartDatabaseIfNeeded();

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of creation of new thesaurus: " + importThesaurusName + ". Time: " + Utilities.GetNow());

        
        Utils.StaticClass.closeDb();
        
        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();
        if(initializeDb){
            
            
            dbAdminUtils.LockSystemForAdministrativeJobs(config);
            // initialize DB if chekbox was selected or DB is not initialized
            StringObject InitializeDBResultMessage = new StringObject("");

            // initialize DB if chekbox was selected or DB is not initiali
            Boolean DBInitializationSucceded = true;
            
            boolean DBCanBeInitialized = dbAdminUtils.DBCanBeInitialized(config, common_utils, importThesaurusName, InitializeDBResultMessage, DBInitializationSucceded);
            if (DBCanBeInitialized == true) {
                DBInitializationSucceded = dbAdminUtils.InitializeDB(common_utils, InitializeDBResultMessage);                
            }
            if(DBInitializationSucceded ==false){
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Initialization of database failed during import operation of thesaurus: " + importThesaurusName + " failed.");
                return false;
            }
        }
        
        //Step5 thesaurus creation
        boolean CreateThesaurusSucceded = dbMerge.CreateThesaurus(refSessionUserInfo, dbGen, config, common_utils, importThesaurusName, importThesaurusName, thesaurusVector, CreateThesaurusResultMessage, backUpDescription, DBbackupFileNameCreated);
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


        //No need to check lengths any more
        //this.checkLengths(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, importThesaurusName, guideTerms, XMLsources, XMLguideTermsRelations, hierarchyFacets, termsInfo, logFileWriter);
        
        ArrayList<SortItem> topTerms = new ArrayList<SortItem>();
        HashMap<String, ArrayList<String>> descriptorRts = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<SortItem>> descriptorUfs = new HashMap<String, ArrayList<SortItem>>();
        
        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes = new ArrayList<HashMap<SortItem, ArrayList<SortItem>>>();

        // Step3 Read XML file in order to fill basic datastructures concerning terms
        // Step4 Process these data structures in order to define topterms and orphans
        //filling all structures passed as parameters except xmlFilePath parameter and then process data in order to classify terms in levels

        processXMLTermsInSortItems(termsInfo, descriptorRts, descriptorUfs, hierarchyFacets, topTerms, allLevelsOfImportThes);

        ArrayList<SortItem> facetSortItems = new ArrayList<SortItem>();
        facetSortItems.addAll(xmlFacetSortItems.values());
        

        returnVal= writeThesaurusDataFromSortItems(refSessionUserInfo, common_utils,
                Q, TA, sis_session, tms_session,
                facetSortItems, guideTerms, XMLsources, XMLguideTermsRelations,
                hierarchyFacets, termsInfo, userSelectedTranslationWords,
                userSelectedTranslationIdentifiers, userLanguageSelections,
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
        }
        if(returnVal && initializeDb){
            dbAdminUtils.UnlockSystemForAdministrativeJobs();
        }
        
        
        return returnVal;
    }
    
    

    public boolean CreateSources(String selectedThesaurus, CommonUtilsDBadmin common_utils, String importThesaurusName,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, 
            HashMap<String, String> XMLsources, StringObject resultObj, OutputStreamWriter logFileWriter){
        
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
            for(String nameStr : XMLsources.keySet()) {
                Q.free_all_sets();
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
            Iterator<String> sourcesIterator2 = XMLsources.keySet().iterator();
            while (sourcesIterator2.hasNext()) {
                Q.free_all_sets();
                String nameStr = sourcesIterator2.next();
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
                    //root/MergeThesauri/MergeSourceSNsSourcePrefix --> 'Source: '
                    logFileWriter.append("\r\n<targetTerm><name>"+u.translateFromMessagesXML("root/MergeThesauri/MergeSourceSNsSourcePrefix",null) + Utilities.escapeXML(nameStr) + "</name><errorType>" + ConstantParameters.source_note_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(oldSourceNoteStr + " ### " + sourceNoteStr) + "</errorValue>");
                    logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeSourceSNs", new String[]{Utilities.escapeXML(nameStr)})+"</reason>");
                    //logFileWriter.append("<reason>Two Source Notes were found for source: '" + Utilities.escapeXML(nameStr) + "'. In order to keep both they will be concatenated with ' ### ' as delimeter</reason>");
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

    public void processXMLTerms(HashMap<String, NodeInfoStringContainer> termsInfo, HashMap<String, ArrayList<String>> descriptorRts, HashMap<String, ArrayList<String>> descriptorUfs,/* ArrayList<String> LinkingToSelf,*/ HashMap<String, ArrayList<String>> hierarchyFacets, ArrayList<String> topTerms, ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting Xml TERMS processing.");
        DBGeneral dbGen = new DBGeneral();

        ArrayList<String> allTermsHavingBTs = new ArrayList<String>();
        ArrayList<String> allTermsHavingNTs = new ArrayList<String>();
        ArrayList<String> allTermsWithoutBTsorNTs = new ArrayList<String>();
        HashMap<String, ArrayList<String>> descriptorNts = new HashMap<String, ArrayList<String>>();


        ArrayList<String> nodesOfInterest = new ArrayList<String>();
        nodesOfInterest.add("descriptor");
        nodesOfInterest.add(ConstantParameters.bt_kwd);
        nodesOfInterest.add(ConstantParameters.nt_kwd);
        nodesOfInterest.add(ConstantParameters.rt_kwd);
        nodesOfInterest.add(ConstantParameters.uf_kwd);

        //DEBUG int counter=1;
        Iterator<String> termInfoIterator = termsInfo.keySet().iterator();

        while (termInfoIterator.hasNext()) {


            String targetNode = termInfoIterator.next();


            NodeInfoStringContainer targetNodeInfo = termsInfo.get(targetNode);

            ArrayList<String> BTnodes = new ArrayList<>();
            ArrayList<String> NTnodes = new ArrayList<>();
            ArrayList<String> RTnodes = new ArrayList<>();
            ArrayList<String> UFnodes = new ArrayList<>();

            BTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.bt_kwd));
            NTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.nt_kwd));
            RTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.rt_kwd));
            UFnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.uf_kwd));

            int howmanyBTs = BTnodes.size();
            int howmanyNTs = NTnodes.size();
            int howmanyRTs = RTnodes.size();
            int howmanyUFs = UFnodes.size();


            if (descriptorNts.containsKey(targetNode) == false) {
                descriptorNts.put(targetNode, new ArrayList<String>());
            }
            if (descriptorRts.containsKey(targetNode) == false) {
                descriptorRts.put(targetNode, new ArrayList<String>());
            }


            boolean validValueDetected = false;
            //Bts relations may define a new Descriptor. in this case HashMaps
            //should be updated with parsedBt as new key and no values.
            //target node should be added to nts of each bt child Node
            for (int k = 0; k < howmanyBTs; k++) {
                String parsedBt = BTnodes.get(k);

                if (parsedBt == null || parsedBt.length() == 0) {
                    continue;
                }

                if (descriptorNts.containsKey(parsedBt) == false) {
                    descriptorNts.put(parsedBt, new ArrayList<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedBt) == false) {
                    descriptorRts.put(parsedBt, new ArrayList<String>());//add it as a key because it may be later encountered
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
            //Nts relations may also define a new Descriptor. in this case HashMaps
            //should be updated with parsedNt as new key and no values.
            //each child nt element should be added to targetNode's nts
            for (int k = 0; k < howmanyNTs; k++) {
                String parsedNt = NTnodes.get(k);

                if (parsedNt == null || parsedNt.length() == 0) {
                    continue;
                }
                if (descriptorNts.containsKey(parsedNt) == false) {
                    descriptorNts.put(parsedNt, new ArrayList<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedNt) == false) {
                    descriptorRts.put(parsedNt, new ArrayList<String>());//add it as a key because it may be later encountered
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

            //Rts relations may also define a new Descriptor. in this case HashMaps
            //should be updated with parsedRt as new key and no values.
            //each rt content should be added to targetNode's rts
            for (int k = 0; k < howmanyRTs; k++) {
                String parsedRt = RTnodes.get(k);

                if (parsedRt == null || parsedRt.length() == 0) {
                    continue;
                }
                if (descriptorNts.containsKey(parsedRt) == false) {
                    descriptorNts.put(parsedRt, new ArrayList<String>());//add it as a key because it may be later encountered
                }
                if (descriptorRts.containsKey(parsedRt) == false) {
                    descriptorRts.put(parsedRt, new ArrayList<String>());//add it as a key because it may be later encountered
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
                    descriptorUfs.put(targetNode, new ArrayList<String>());//add it as a key because it may be later encountered
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

    public void processXMLTermsInSortItems(HashMap<String, NodeInfoStringContainer> termsInfo, 
                                           HashMap<String, ArrayList<String>> descriptorRts,
                                           HashMap<String, ArrayList<SortItem>> descriptorUfs,
                                           HashMap<String, ArrayList<String>> hierarchyFacets, 
                                           ArrayList<SortItem> topTerms, 
                                           ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting Xml TERMS processing.");
        
        ArrayList<SortItem> allTermsHavingBTs = new ArrayList<SortItem>();
        ArrayList<SortItem> allTermsHavingNTs = new ArrayList<SortItem>();
        ArrayList<SortItem> allTermsWithoutBTsorNTs = new ArrayList<SortItem>();
        HashMap<SortItem, ArrayList<SortItem>> descriptorNts = new HashMap<SortItem, ArrayList<SortItem>>();

        
        HashMap<SortItem, ArrayList<SortItem>> tempDescriptorRts  = new HashMap<SortItem, ArrayList<SortItem>>();

        ArrayList<String> nodesOfInterest = new ArrayList<String>();
        nodesOfInterest.add("descriptor");
        nodesOfInterest.add(ConstantParameters.bt_kwd);
        nodesOfInterest.add(ConstantParameters.nt_kwd);
        nodesOfInterest.add(ConstantParameters.rt_kwd);
        nodesOfInterest.add(ConstantParameters.uf_kwd);

        //DEBUG int counter=1;
        String targetNode = "";
        String targetNodeTranslit = "";
        long targetNodeRef = -1;
        //DEBUG int counter=1;
        
        Iterator<String> termInfoIterator = termsInfo.keySet().iterator();

        while (termInfoIterator.hasNext()) {


            targetNode = termInfoIterator.next();
            targetNodeTranslit = "";
            targetNodeRef = -1;

            NodeInfoStringContainer targetNodeInfo = termsInfo.get(targetNode);
            
            targetNodeRef = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(targetNodeInfo);
            targetNodeTranslit = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(targetNodeInfo, targetNode, false);   
            
            SortItem targetSortItem = new SortItem(targetNode, -1, targetNodeTranslit, targetNodeRef);

            ArrayList<String> BTnodes = new ArrayList<String>();
            ArrayList<String> NTnodes = new ArrayList<String>();
            ArrayList<String> RTnodes = new ArrayList<String>();
            ArrayList<String> UFnodes = new ArrayList<String>();

            BTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.bt_kwd));
            NTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.nt_kwd));
            RTnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.rt_kwd));
            UFnodes.addAll(targetNodeInfo.descriptorInfo.get(ConstantParameters.uf_kwd));

            int howmanyBTs = BTnodes.size();
            int howmanyNTs = NTnodes.size();
            int howmanyRTs = RTnodes.size();
            int howmanyUFs = UFnodes.size();


            if (descriptorNts.containsKey(targetSortItem) == false) {
                descriptorNts.put(targetSortItem, new ArrayList<SortItem>());
            }
            if (tempDescriptorRts.containsKey(targetSortItem) == false) {
                tempDescriptorRts.put(targetSortItem, new ArrayList<SortItem>());
                descriptorRts.put(targetSortItem.getLogName(), new ArrayList<String>());
            }


            boolean validValueDetected = false;
            //Bts relations may define a new Descriptor. in this case HashMaps
            //should be updated with parsedBt as new key and no values.
            //target node should be added to nts of each bt child Node
            String parsedBt = "";
            String parsedBtTranslit = "";
            long parsedBtRef = -1;
            for (int k = 0; k < howmanyBTs; k++) {
                parsedBt = BTnodes.get(k);
                parsedBtTranslit = "";
                parsedBtRef = -1;

                if (parsedBt == null || parsedBt.length() == 0) {
                    continue;
                }
                
                NodeInfoStringContainer parsedBtInfo = termsInfo.get(parsedBt);

                parsedBtRef = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(parsedBtInfo);
                parsedBtTranslit = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(parsedBtInfo, parsedBt, false);

                SortItem parsedBtSortItem = new SortItem(parsedBt,-1,parsedBtTranslit,parsedBtRef);
                
                if (descriptorNts.containsKey(parsedBtSortItem) == false) {
                    descriptorNts.put(parsedBtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                }
                if (tempDescriptorRts.containsKey(parsedBtSortItem) == false) {
                    tempDescriptorRts.put(parsedBtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                    descriptorRts.put(parsedBtSortItem.getLogName(), new ArrayList<String>());
                }

                if (targetNode.compareTo(parsedBt) == 0) {
                    ///LinkingToSelf.add(targetNode);
                } else {
                    validValueDetected = true;
                    if (descriptorNts.get(parsedBtSortItem).contains(targetSortItem) == false) {
                        descriptorNts.get(parsedBtSortItem).add(targetSortItem);//targetNode is nt of parsedBt
                    }

                    if (allTermsHavingNTs.contains(parsedBtSortItem) == false) {
                        allTermsHavingNTs.add(parsedBtSortItem);
                    }
                }
            }

            if (validValueDetected && allTermsHavingBTs.contains(targetSortItem) == false) {
                allTermsHavingBTs.add(targetSortItem);
            }

            validValueDetected = false;
            //Nts relations may also define a new Descriptor. in this case HashMaps
            //should be updated with parsedNt as new key and no values.
            //each child nt element should be added to targetNode's nts
            
            String parsedNt = "";
            String parsedNtTranslit = "";
            long parsedNtRef = -1;
            
            for (int k = 0; k < howmanyNTs; k++) {
                parsedNt = NTnodes.get(k);
                parsedNtTranslit = "";
                parsedNtRef = -1;

                if (parsedNt == null || parsedNt.length() == 0) {
                    continue;
                }
                
                NodeInfoStringContainer parsedNtInfo = termsInfo.get(parsedNt);
                
                parsedNtRef = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(parsedNtInfo);
                parsedNtTranslit = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(parsedNtInfo, parsedNt, false);
                SortItem parsedNtSortItem = new SortItem(parsedNt,-1,parsedNtTranslit,parsedNtRef);
                
                if (descriptorNts.containsKey(parsedNtSortItem) == false) {
                    descriptorNts.put(parsedNtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                }
                if (tempDescriptorRts.containsKey(parsedNtSortItem) == false) {
                    tempDescriptorRts.put(parsedNtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                    descriptorRts.put(parsedNtSortItem.getLogName(), new ArrayList<String>());
                }

                if (targetNode.compareTo(parsedNt) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    validValueDetected = true;
                    if (descriptorNts.get(targetSortItem).contains(parsedNtSortItem) == false) {
                        descriptorNts.get(targetSortItem).add(parsedNtSortItem);//parsedNt is nt of targetNode
                    }

                    if (allTermsHavingBTs.contains(parsedNtSortItem) == false) {
                        allTermsHavingBTs.add(parsedNtSortItem);
                    }
                }
            }

            if (validValueDetected && allTermsHavingNTs.contains(targetSortItem) == false) {
                allTermsHavingNTs.add(targetSortItem);
            }

            //Rts relations may also define a new Descriptor. in this case HashMaps
            //should be updated with parsedRt as new key and no values.
            //each rt content should be added to targetNode's rts
            String parsedRt = "";
            String parsedRtTranslit = "";
            long parsedRtRef = -1;
            for (int k = 0; k < howmanyRTs; k++) {
                parsedRt = RTnodes.get(k);
                parsedRtTranslit = "";
                parsedRtRef = -1;

                if (parsedRt == null || parsedRt.length() == 0) {
                    continue;
                }
                NodeInfoStringContainer parsedRtInfo = termsInfo.get(parsedRt);
                
                parsedRtRef = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(parsedRtInfo);
                parsedRtTranslit = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(parsedRtInfo, parsedRt, false);
                
                SortItem parsedRtSortItem = new SortItem(parsedRt,-1,parsedRtTranslit,parsedRtRef);
                
                if (descriptorNts.containsKey(parsedRtSortItem) == false) {
                    descriptorNts.put(parsedRtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                }
                if (tempDescriptorRts.containsKey(parsedRtSortItem) == false) {
                    tempDescriptorRts.put(parsedRtSortItem, new ArrayList<SortItem>());//add it as a key because it may be later encountered
                    descriptorRts.put(parsedRtSortItem.getLogName(), new ArrayList<String>());
                }

                if (targetNode.compareTo(parsedRt) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    if (tempDescriptorRts.get(targetSortItem).contains(parsedRtSortItem) == false) {
                        tempDescriptorRts.get(targetSortItem).add(parsedRtSortItem);
                        descriptorRts.get(targetSortItem.getLogName()).add(parsedRtSortItem.getLogName());
                    }
                }
            }

            //READ UF Links
            String parsedUF = "";
            String parsedUFTranslit = "";
            long parsedUFRef = -1;
            for (int k = 0; k < howmanyUFs; k++) {
                parsedUF = UFnodes.get(k);
                parsedUFTranslit = "";
                parsedUFRef = -1;

                if (parsedUF == null || parsedUF.length() == 0) {
                    continue;
                }

                parsedUFTranslit = Utilities.getTransliterationString(parsedUF, false);
                
                SortItem ufSortItem = new SortItem(parsedUF,-1,parsedUFTranslit,-1);
                
                if (descriptorUfs.containsKey(targetSortItem.getLogName()) == false) {
                    descriptorUfs.put(targetSortItem.getLogName(), new ArrayList<SortItem>());//add it as a key because it may be later encountered
                }

                if (targetNode.compareTo(parsedUF) == 0) {
                    //LinkingToSelf.add(targetNode);
                } else {
                    if (descriptorUfs.get(targetSortItem.getLogName()).contains(ufSortItem) == false) {
                        descriptorUfs.get(targetSortItem.getLogName()).add(ufSortItem);
                    }
                }
            }
        }

        findOutTermLevelsInSortItems(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets, allLevelsOfImportThes);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Xml TERMS processing.");
    }

    public void findOutTermLevelsInSortItems(HashMap<SortItem, ArrayList<SortItem>> descriptorNts, 
                                             ArrayList<SortItem> allTermsHavingBTs, 
                                             ArrayList<SortItem> allTermsHavingNTs, 
                                             ArrayList<SortItem> allTermsWithoutBTsorNTs, 
                                             ArrayList<SortItem> topTerms, 
                                             HashMap<String, ArrayList<String>> hierarchyFacets, 
                                             ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of classification of terms in hierarchical levels.");

        
        //filling structures allTermsWithoutBTsorNTs, topTerms and find out hierarchies and top terms
        findOutTopTermsAndOrphansUsingSortItems(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets);
        
        //classify terms in levels of creation kept level by level in allLevelsOfImportThes


        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with bt " + allTermsHavingBTs.size() + ".");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with nt " + allTermsHavingNTs.size() + ".");

        ArrayList<String> currentLevel = new ArrayList<String>();
        for(SortItem item :topTerms){
            currentLevel.add(item.getLogName());
        }
        

        /*
        if(Parameters.DEBUG){
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nList of terms promoted to Top Terms");
        for(int i=0; i< topTerms.size(); i++){
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+i + "\t" + topTerms.get(i));
        }
        }
         */
        //Now add all hierarchies created in current level
        for(String targetHierarchy : hierarchyFacets.keySet()){
            if (currentLevel.contains(targetHierarchy) == false) {
                currentLevel.add(targetHierarchy);
            }
        }

        ArrayList<String> parsedTerms = new ArrayList<String>();
        int levelIndex = 0;
        while (currentLevel.size() > 0) {

            int termsperlevel = 0;
            //logFileWriter.append("\r\nTerm Level No: " + (levelIndex+2) +"\r\n");

            allLevelsOfImportThes.add(readNextLevelSetTermsAndBtsInSortItems(currentLevel, descriptorNts, parsedTerms));
            ArrayList<String> nextLevel = new ArrayList<String>();
            
            
            Iterator<SortItem> parseLevel = allLevelsOfImportThes.get(levelIndex).keySet().iterator();
            
            while (parseLevel.hasNext()) {
                termsperlevel++;
                SortItem term = parseLevel.next();

                if (nextLevel.contains(term.getLogName()) == false) {
                    nextLevel.add(term.getLogName());
                }
            }

            levelIndex++;
            if (nextLevel.isEmpty()) {
                break;
            } else {
                currentLevel.clear();
                currentLevel.addAll(nextLevel);
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of classification of terms in hierarchical levels.");

    }

    
    public void findOutTopTermsAndOrphansUsingSortItems(HashMap<SortItem, ArrayList<SortItem>> descriptorNts, 
                                                        ArrayList<SortItem> allTermsHavingBTs, 
                                                        ArrayList<SortItem> allTermsHavingNTs, 
                                                        ArrayList<SortItem> allTermsWithoutBTsorNTs, 
                                                        ArrayList<SortItem> topTerms, 
                                                        HashMap<String, ArrayList<String>> hierarchyFacets) {

        //find out topTerms and unclassifed terms
        Iterator<SortItem> parseAllTerms = descriptorNts.keySet().iterator();
        while (parseAllTerms.hasNext()) {

            SortItem term = parseAllTerms.next();

            if (allTermsHavingBTs.contains(term) == false) {

                if (allTermsHavingNTs.contains(term) == false) {

                    if (hierarchyFacets.containsKey(term.getLogName()) == false) {
                        allTermsWithoutBTsorNTs.add(term);
                    }
                } else {
                    if (hierarchyFacets.containsKey(term.getLogName()) == false) {
                        topTerms.add(term);
                    }
                }
            }

        }
        boolean foundUnclassifiedInTopTerms = false;
        for(SortItem item : topTerms){
            if(item.getLogName().endsWith(Parameters.UnclassifiedTermsLogicalname))
            {
                foundUnclassifiedInTopTerms = true;
                break;
            }
        }
        
        //Update read structures affected
        if (!foundUnclassifiedInTopTerms && hierarchyFacets.containsKey(Parameters.UnclassifiedTermsLogicalname) == false) {
            topTerms.add(new SortItem(Parameters.UnclassifiedTermsLogicalname,-1,Utilities.getTransliterationString(Parameters.UnclassifiedTermsLogicalname, false),-1));
        }

        boolean foundUnclassifiedInTermsHavingNts = false;
        for(SortItem item : allTermsHavingNTs){
            if(item.getLogName().endsWith(Parameters.UnclassifiedTermsLogicalname))
            {
                foundUnclassifiedInTermsHavingNts = true;
                break;
            }
        }
        
        if (!foundUnclassifiedInTermsHavingNts) {
            allTermsHavingNTs.add(new SortItem(Parameters.UnclassifiedTermsLogicalname,-1,Utilities.getTransliterationString(Parameters.UnclassifiedTermsLogicalname, false),-1));
        }

        //boolean foundUnclassifiedInDescriptorNts = false;
        SortItem keyfound = null;
        for(SortItem item : descriptorNts.keySet()){
            if(item.getLogName().endsWith(Parameters.UnclassifiedTermsLogicalname))
            {
                //foundUnclassifiedInDescriptorNts = true;
                keyfound = item.getACopy();
                break;
            }
        }
        
        if (keyfound==null) {
            descriptorNts.put(new SortItem(Parameters.UnclassifiedTermsLogicalname,-1,Utilities.getTransliterationString(Parameters.UnclassifiedTermsLogicalname, false),-1), allTermsWithoutBTsorNTs);
        } else {
            //check one by one no dublicates should exist
            descriptorNts.get(keyfound).addAll(allTermsWithoutBTsorNTs);
        }

        allTermsHavingBTs.addAll(allTermsWithoutBTsorNTs);
    }

    
    public void findOutTermLevels(HashMap<String, ArrayList<String>> descriptorNts, ArrayList<String> allTermsHavingBTs, ArrayList<String> allTermsHavingNTs, ArrayList<String> allTermsWithoutBTsorNTs, ArrayList<String> topTerms, HashMap<String, ArrayList<String>> hierarchyFacets, ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of classification of terms in hierarchical levels.");

        //filling structures allTermsWithoutBTsorNTs, topTerms and find out hierarchies and top terms
        findOutTopTermsAndOrphans(descriptorNts, allTermsHavingBTs, allTermsHavingNTs, allTermsWithoutBTsorNTs, topTerms, hierarchyFacets);
        
        //classify terms in levels of creation kept level by level in allLevelsOfImportThes


        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with bt " + allTermsHavingBTs.size() + ".");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Num of terms with nt " + allTermsHavingNTs.size() + ".");

        ArrayList<String> currentLevel = new ArrayList<String>();
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
        Iterator<String> parsedHierarchies = hierarchyFacets.keySet().iterator();
        while (parsedHierarchies.hasNext()) {
            String targetHierarchy = parsedHierarchies.next();
            if (currentLevel.contains(targetHierarchy) == false) {
                currentLevel.add(targetHierarchy);
            }
        }

        ArrayList<String> parsedTerms = new ArrayList<String>();
        int levelIndex = 0;
        while (currentLevel.size() > 0) {

            int termsperlevel = 0;
            //logFileWriter.append("\r\nTerm Level No: " + (levelIndex+2) +"\r\n");


            allLevelsOfImportThes.add(readNextLevelSetTermsAndBts(currentLevel, descriptorNts, parsedTerms));
            ArrayList<String> nextLevel = new ArrayList<String>();
            Iterator<String> parseLevel = allLevelsOfImportThes.get(levelIndex).keySet().iterator();
            while (parseLevel.hasNext()) {
                termsperlevel++;
                String term = parseLevel.next();

                if (nextLevel.contains(term) == false) {
                    nextLevel.add(term);
                }
            }

            levelIndex++;
            if (nextLevel.size() == 0) {
                break;
            } else {
                currentLevel.clear();
                currentLevel.addAll(nextLevel);
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of classification of terms in hierarchical levels.");

    }

    public void findOutTopTermsAndOrphans(HashMap<String, ArrayList<String>> descriptorNts, ArrayList<String> allTermsHavingBTs, ArrayList<String> allTermsHavingNTs, ArrayList<String> allTermsWithoutBTsorNTs, ArrayList<String> topTerms, HashMap<String, ArrayList<String>> hierarchyFacets) {

        //find out topTerms and unclassifed terms
        Iterator<String> parseAllTerms = descriptorNts.keySet().iterator();
        while (parseAllTerms.hasNext()) {

            String term = parseAllTerms.next();

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

    public HashMap<String, ArrayList<String>> readNextLevelSetTermsAndBts(ArrayList<String> currentLevel, HashMap<String, ArrayList<String>> allNts, ArrayList<String> parsedTerms) {

        HashMap<String, ArrayList<String>> nextLevelSet_Terms_and_Bts = new HashMap<String, ArrayList<String>>();

        for (int i = 0; i < currentLevel.size(); i++) {

            String currentTerm = currentLevel.get(i);
            //System.out.print("currentTerm: "+currentTerm + " with nts: ");
            if (parsedTerms.contains(currentTerm)) {
                continue;
            } else {
                parsedTerms.add(currentTerm);
            }
            ArrayList<String> nts = allNts.get(currentTerm);
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
                    ArrayList<String> bts = new ArrayList<String>();
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

    public HashMap<SortItem, ArrayList<SortItem>> readNextLevelSetTermsAndBtsInSortItems(ArrayList<String> currentLevel, HashMap<SortItem, ArrayList<SortItem>> allNts, ArrayList<String> parsedTerms) {

        HashMap<String, SortItem> allNtsKeysDictionary = new HashMap<String,SortItem>();
        //HashMap<String, ArrayList<SortItem>> allNtsWithLogicalnameAsKey = new HashMap<String,ArrayList<SortItem>>();
        
        Iterator<SortItem> allNtsEnum = allNts.keySet().iterator();
        while(allNtsEnum.hasNext()){
            SortItem targetItem = allNtsEnum.next();
            allNtsKeysDictionary.put(targetItem.getLogName(), targetItem);
            //allNtsWithLogicalnameAsKey.put(targetItem.getLogName(),allNts.get(targetItem));
        }
        HashMap<SortItem, ArrayList<SortItem>> nextLevelSet_Terms_and_Bts = new HashMap<SortItem, ArrayList<SortItem>>();

        for (int i = 0; i < currentLevel.size(); i++) {

            String currentTerm = currentLevel.get(i);
            //System.out.print("currentTerm: "+currentTerm + " with nts: ");
            if (parsedTerms.contains(currentTerm)) {
                continue;
            } else {
                parsedTerms.add(currentTerm);
            }
            SortItem currentTermSortItem = allNtsKeysDictionary.get(currentTerm);
            ArrayList<SortItem> nts = allNts.get(currentTermSortItem);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"i= " + i + " currentTerm = " + currentTerm );
            if (nts == null) {
                continue;
            }

            while (nts.contains(currentTermSortItem)) {
                nts.remove(currentTermSortItem);
            }

            //Utils.StaticClass.webAppSystemOutPrintln(nts.toString());
            for (int k = 0; k < nts.size(); k++) {

                SortItem currentNt = nts.get(k);


                /* BUG Not NEEDED A->B and C->D->B 
                A,C top terms diffenrent 
                In this case B would not be created as a child of D parsed terms works well in case of cyrcles
                if (currentLevel.contains(currentNt)) {
                    continue;
                }*/

                if (nextLevelSet_Terms_and_Bts.containsKey(currentNt) == false) {
                    ArrayList<SortItem> bts = new ArrayList<SortItem>();
                    bts.add(currentTermSortItem);
                    nextLevelSet_Terms_and_Bts.put(currentNt, bts);

                } else {
                    if (nextLevelSet_Terms_and_Bts.get(currentNt).contains(currentTermSortItem) == false) {
                        nextLevelSet_Terms_and_Bts.get(currentNt).add(currentTermSortItem);
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
    
    private boolean importMoreHierarchiesFromTopTermsInsortItems(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, 
            String importThesaurusName, ArrayList<SortItem> topTerms, Locale targetLocale, StringObject resultObj,boolean resolveError, OutputStreamWriter logFileWriter, int ConsistencyPolicy) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "String creation of additional hierarchies - specified by terms without bt but not declared as hierarchies. Time: " + Utilities.GetNow());
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        //Before any addition to merged thesaurus find out which facet is used for Unclassified terms --> UNCLASSIFIED TERMS
        //in order to instanciate possible hierarchies that do not belong in any Facet
        String defaultFacet = new String(dbMerge.getDefaultFacet(SessionUserInfo, Q, sis_session, importThesaurusName));
        SortItem defaultFacetSortItem = dbMerge.getDefaultFacetSortItem(SessionUserInfo, Q, sis_session, importThesaurusName);
        
        HashMap<SortItem, ArrayList<String>> hierFacetsPairsOfNewThesaurus = new HashMap<SortItem, ArrayList<String>>();
        
        ArrayList<String> facets = new ArrayList<String>();
        facets.add(defaultFacet);
        for (int i = 0; i < topTerms.size(); i++) {
            hierFacetsPairsOfNewThesaurus.put(topTerms.get(i), facets);
        }
        //try {
            if (dbMerge.CreateHierarchiesFromSortItems(SessionUserInfo, Q, TA, sis_session, tms_session, importThesaurusName, defaultFacetSortItem, targetLocale, resultObj, hierFacetsPairsOfNewThesaurus, resolveError,logFileWriter,ConsistencyPolicy) == false) {
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

    private boolean importMoreHierarchiesFromTopTerms(UserInfoClass SessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, 
            TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String importThesaurusName,
            ArrayList<String> topTerms, Locale targetLocale, OutputStreamWriter logFileWriter, StringObject resultObj) {
        
        ArrayList<SortItem> topTermSortItems = new ArrayList<SortItem>();
        if(topTerms!=null){
            for(String toptermStr :topTerms){
                topTermSortItems.add(new SortItem(toptermStr,-1,Utilities.getTransliterationString(toptermStr, false),-1));
            }
        }

        boolean returnVal = importMoreHierarchiesFromTopTermsInsortItems(SessionUserInfo, Q, TA, sis_session, tms_session, importThesaurusName, topTermSortItems, targetLocale, resultObj,false,logFileWriter, ConsistensyCheck.EDIT_TERM_POLICY);
        
        if(returnVal){
            topTerms.clear();
            return true;
        }
        else{
            return false;
        }        
    }

    private boolean importTermsInSortItems(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML, String importThesaurusName,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            StringObject resultObj,
            ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfImportThes,
            HashMap<String, ArrayList<String>> descriptorRts,
            HashMap<String, ArrayList<SortItem>> descriptorUfs,
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
        if (dbMerge.CreateTermsLevelByLevelInSortItems(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, " " + importThesaurusName, null, importThesaurusName, logFileWriter, resultObj, allLevelsOfImportThes, null, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
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
        
        HashMap<String,ArrayList<String>> descUFs = new HashMap<String,ArrayList<String>> ();
        if(descriptorUfs!=null){
            Iterator<String> ufsEnum = descriptorUfs.keySet().iterator();
            while(ufsEnum.hasNext()){
                String item = ufsEnum.next();
                ArrayList<SortItem> ufsSortItems =    descriptorUfs.get(item);
                ArrayList<String> newUFValues = new ArrayList<String>();
                if(ufsSortItems!=null){
                    for(SortItem rtSi : ufsSortItems){
                        newUFValues.add(rtSi.getLogName());
                    }
                }
                descUFs.put(item, newUFValues);
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of UFs. Time: " + Utilities.GetNow());
        startTime = System.currentTimeMillis();
        if (dbMerge.CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, ConstantParameters.uf_kwd, new ArrayList<String>(), resultObj, descUFs, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
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

        Iterator<String> termsEnum = termsInfo.keySet().iterator();
        while (termsEnum.hasNext()) {
            String targetTerm = termsEnum.next();
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
        //<editor-fold defaultstate="collapsed" desc="Simple term links --> Translations, UF_Translataions,primary_found_in, translations_found_in ...">
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
    
    private boolean importTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML, String importThesaurusName,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            StringObject resultObj,
            ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes,
            HashMap<String, ArrayList<String>> descriptorRts,
            HashMap<String, ArrayList<String>> descriptorUfs,
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
        if (dbMerge.CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, ConstantParameters.uf_kwd, new ArrayList<String>(), resultObj, descriptorUfs, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY) == false) {
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

        Iterator<String> termsEnum = termsInfo.keySet().iterator();
        while (termsEnum.hasNext()) {
            String targetTerm = termsEnum.next();
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
            String importThesaurusName, HashMap<String, NodeInfoStringContainer> termsInfo,
            StringObject resultObj, OutputStreamWriter logFileWriter, boolean resolveError) throws IOException {

        DBFilters dbF = new DBFilters();
        DBGeneral dbGen = new DBGeneral();

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        HashMap<String, String> thesaurus1_statuses = new HashMap<String, String>();
        HashMap<String, Long> merged_thesaurus_status_classIds = new HashMap<String, Long>();

        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);


        //Read Statuses defined in XML
        Iterator<String> termEnum = termsInfo.keySet().iterator();
        while (termEnum.hasNext()) {
            String targetTerm = termEnum.next();
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
            ArrayList<String> targetStatuses = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.status_kwd);
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
        ArrayList<String> readStatuses = new ArrayList<String>();
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

        ArrayList<String> allDBTerms = dbGen.get_Node_Names_Of_Set(set_allTerms, true, Q, sis_session);
        Q.free_all_sets();

        for (int i = 0; i < allDBTerms.size(); i++) {
            String testTerm = allDBTerms.get(i);

            if (thesaurus1_statuses.containsKey(testTerm) == false) {
                thesaurus1_statuses.put(testTerm, defaultStatusForUser);
            }
        }

        return dbMerge.CreateStatuses(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                importThesaurusName, logFileWriter, resultObj, thesaurus1_statuses,
                new HashMap<String, String>(), merged_thesaurus_status_classIds);

    }

    private boolean importSimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName, String pathToErrorsXML, HashMap<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter, String keyWord) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        HashMap<String, ArrayList<String>> term_Links_HASH = new HashMap<String, ArrayList<String>>();

        //Read Statuses defined in XML
        Iterator<String> termEnum = termsInfo.keySet().iterator();
        while (termEnum.hasNext()) {
            String targetTerm = termEnum.next();
            ArrayList<String> targettermLinks = termsInfo.get(targetTerm).descriptorInfo.get(keyWord);
            term_Links_HASH.put(targetTerm, targettermLinks);
        }

        return dbMerge.CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, importThesaurusName, logFileWriter, keyWord, new ArrayList<String>(), resultObj, term_Links_HASH, true, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);

    }

    private boolean importCommentCategories(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName, String pathToErrorsXML, HashMap<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        HashMap<String, String> scope_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> scope_notes_EN_HASH = new HashMap<String, String>();
        HashMap<String, String> historical_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> comment_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> note_notes_HASH = new HashMap<String, String>();


        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        Iterator<String> termsIterator = termsInfo.keySet().iterator();
        while (termsIterator.hasNext()) {

            String targetDescriptor = termsIterator.next();
            NodeInfoStringContainer targetDescriptorInfo = termsInfo.get(targetDescriptor);

            String targetScopeNote = new String("");
            String targetScopeNoteEn = new String("");
            String targetHistoricalNote = new String("");
            String targetCommentNote = new String("");
            String targetNote = new String("");
            ArrayList<String> snNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
            ArrayList<String> snEnNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
            ArrayList<String> hnNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);
            ArrayList<String> comNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.comment_kwd);
            ArrayList<String> noteNodes = targetDescriptorInfo.descriptorInfo.get(ConstantParameters.note_kwd);

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
            
            if (comNodes.size() > 0) {
                targetCommentNote = comNodes.get(0);
                if (targetCommentNote != null && targetCommentNote.length() > 0) {
                    comment_notes_HASH.put(targetDescriptor, targetCommentNote);
                }
            }
            
            if (noteNodes.size() > 0) {
                targetNote = noteNodes.get(0);
                if (targetNote != null && targetNote.length() > 0) {
                    note_notes_HASH.put(targetDescriptor, targetNote);
                }
            }

        }

        return dbMerge.CreateCommentCategories(refSessionUserInfo, common_utils, Q, TA,
                sis_session, tms_session, importThesaurusName, scope_notes_HASH,
                scope_notes_EN_HASH, historical_notes_HASH, comment_notes_HASH, note_notes_HASH, logFileWriter,
                pathToErrorsXML, resultObj, ConsistensyCheck.IMPORT_COPY_MERGE_THESAURUS_POLICY);
    }

    private boolean importDatesAndEditors(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String editorKeyWordStr, String importThesaurusName,
            HashMap<String, NodeInfoStringContainer> termsInfo, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBMergeThesauri dbMerge = new DBMergeThesauri();
        HashMap<String, ArrayList<String>> term_Editor_Links_THES1_HASH = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> term_Date_Links_THES1_HASH = new HashMap<String, ArrayList<String>>();


        String dateKeyWordStr = new String("");
        if (editorKeyWordStr.compareTo(ConstantParameters.created_by_kwd) == 0) {
            dateKeyWordStr = ConstantParameters.created_on_kwd;
        } else if (editorKeyWordStr.compareTo(ConstantParameters.modified_by_kwd) == 0) {
            dateKeyWordStr = ConstantParameters.modified_on_kwd;
        }

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        Iterator<String> parseAllTerms = termsInfo.keySet().iterator();
        while (parseAllTerms.hasNext()) {
            String targetDescriptor = parseAllTerms.next();
            NodeInfoStringContainer targetDescriptorInfo = termsInfo.get(targetDescriptor);

            ArrayList<String> EditorVec = new ArrayList<String>();
            EditorVec.addAll(targetDescriptorInfo.descriptorInfo.get(editorKeyWordStr));
            ArrayList<String> DatesVec = new ArrayList<String>();
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
                ArrayList<String> currentLinks = term_Editor_Links_THES1_HASH.get(targetDescriptor);

                if (currentLinks == null) {
                    ArrayList<String> newLinksVec = new ArrayList<String>();
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
                ArrayList<String> currentLinks = term_Date_Links_THES1_HASH.get(targetDescriptor);

                if (currentLinks == null) {
                    ArrayList<String> newLinksVec = new ArrayList<String>();
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
                term_Editor_Links_THES1_HASH, term_Date_Links_THES1_HASH, new HashMap<String, ArrayList<String>>(), new HashMap<String, ArrayList<String>>(), editorKeyWordStr, resultObj);
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
            ArrayList<String> userSelectedTranslationWords,
            ArrayList<String> userSelectedTranslationIdentifiers,
            HashMap<String, String> userSelections) {

        DBGeneral dbGen = new DBGeneral();
        String pathToMessagesXML = Utilities.getXml_For_Messages();


        HashMap<String, String> currentTranslationCategories = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, selectedThesaurus, null, false, true);

        return dbGen.synchronizeTranslationCategories(currentTranslationCategories,
                userSelections, userSelectedTranslationWords, userSelectedTranslationIdentifiers, selectedThesaurus,
                resultMessageStrObj, pathToMessagesXML, Q, TA, sis_session, tms_session);
    }

    private String getPageContentsXsl() {
        return Parameters.BaseRealPath + File.separator + "xml-xsl" + File.separator + "page_contents.xsl";
    }
}


/*
    public void checkLengths(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String importThesaurusName,
            ArrayList<String> guideTerms, HashMap<String, String> XMLsources,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            HashMap<String, ArrayList<String>> hierarchyFacets,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            OutputStreamWriter logFileWriter) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass webappusers = new UsersClass();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        webappusers.UpdateSessionUserSessionAttribute(SessionUserInfo, importThesaurusName);

        
        //open connection and start Transaction
        //if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
        //    Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBImportData writeThesaurusData()");
        //    return;
        //}

        
        ArrayList<String> errorArgs = new ArrayList<String>();
        ArrayList<String> removeTerms = new ArrayList<String>();
        HashMap<String, String> AllLengthRenames = new HashMap<String, String>();
        int unlabeledCounter = 1;
        ArrayList<String> allTermsVec = new ArrayList<String>();
        allTermsVec.addAll(termsInfo.keySet());
        
//        for (int k = 0; k < allTermsVec.size(); k++) {
//            String targetTerm = allTermsVec.get(k);
//            try {
//                byte[] byteArray = targetTerm.getBytes("UTF-8");
//
//                int maxTermChars = dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session);
//              if (byteArray.length > maxTermChars) {
//                    removeTerms.add(targetTerm);
//                    String newName = XMLHandling.ParseFileData.UnlabeledPrefix + (unlabeledCounter++);
//                    AllLengthRenames.put(targetTerm, newName);
//                    StringObject warningMsg = new StringObject();
//                    errorArgs.clear();
//                    errorArgs.add(targetTerm);
//                    errorArgs.add(newName);
//                    errorArgs.add("" + maxTermChars);
//                    errorArgs.add("" + byteArray.length);
//                    dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
//                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
//                    try {
//                        logFileWriter.append("\r\n<targetTerm>");
//                        logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
//                        logFileWriter.append("<errorType>" + "termname" + "</errorType>");
//                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(newName) + "</errorValue>");
//                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
//                        logFileWriter.append("</targetTerm>\r\n");
//                    } catch (IOException ex) {
//                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
//                        Utils.StaticClass.handleException(ex);
//                    }
//
//
//
//                }
//            } catch (UnsupportedEncodingException ex) {
//                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
//                Utils.StaticClass.handleException(ex);
//            }
//        }
//        
//        
//        for (int k = 0; k < topTerms.size(); k++) {
//        String targetTerm = topTerms.get(k);
//        try {
//        byte[] byteArray = targetTerm.getBytes("UTF-8");
//
//        int maxTermChars = dbtr.getMaxBytesForDescriptor(refSessionUserInfo.selectedThesaurus, Q, sis_session);
//        if (byteArray.length > maxTermChars) {
//        if (removeTerms.contains(targetTerm) == false) {
//        removeTerms.add(targetTerm);
//        }
//        StringObject warningMsg = new StringObject();
//        errorArgs.clear();
//        errorArgs.add(targetTerm);
//        errorArgs.add("" + maxTermChars);
//        errorArgs.add("" + byteArray.length);
//        dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
//        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
//        try {
//        logFileWriter.append("\r\n<targetTerm>");
//        logFileWriter.append("<name>" + targetTerm + "</name>");
//        logFileWriter.append("<errorType>" + "termname" + "</errorType>");
//        logFileWriter.append("<errorValue>" + targetTerm + "</errorValue>");
//        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
//        logFileWriter.append("</targetTerm>\r\n");
//        } catch (IOException ex) {
//        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
//        Utils.StaticClass.handleException(ex);
//        }
//
//
//
//        }
//        } catch (UnsupportedEncodingException ex) {
//        Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
//        Utils.StaticClass.handleException(ex);
//        }
//        }
//
//
//         

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

                    ArrayList<String> bts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.bt_kwd);
                    ArrayList<String> nts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.nt_kwd);
                    ArrayList<String> rts = termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.rt_kwd);

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

//
//                if (topTerms.contains(termToRemove)) {
//                topTerms.remove(termToRemove);
//                }
//
//                if (descriptorRts.containsKey(termToRemove)) {
//                descriptorRts.remove(termToRemove);
//                }
//
//                if (descriptorUfs.containsKey(termToRemove)) {
//                descriptorUfs.remove(termToRemove);
//                }
//
//                Enumeration<String> rtsEnum = descriptorRts.keys();
//                while (rtsEnum.hasMoreElements()) {
//                String targetTerm = rtsEnum.nextElement();
//                ArrayList<String> rts = descriptorRts.get(targetTerm);
//                if (rts.contains(termToRemove)) {
//                descriptorRts.get(targetTerm).remove(termToRemove);
//                }
//                }
//                 
//                
//                for (int m = 0; m < allLevelsOfImportThes.size(); m++) {
//                if (allLevelsOfImportThes.get(m).containsKey(termToRemove)) {
//                allLevelsOfImportThes.get(m).remove(termToRemove);
//                }
//
//                Enumeration<String> levelTermsEnum = allLevelsOfImportThes.get(m).keys();
//                while (levelTermsEnum.hasMoreElements()) {
//                String targetTerm = levelTermsEnum.nextElement();
//                ArrayList<String> bts = allLevelsOfImportThes.get(m).get(targetTerm);
//                if (bts.contains(termToRemove)) {
//                allLevelsOfImportThes.get(m).get(targetTerm).remove(termToRemove);
//                }
//                if (bts.size() == 1) {
//
//                allLevelsOfImportThes.get(m).get(targetTerm).add(Parameters.UnclassifiedTermsLogicalname);
//                }
//                }
//                }
//                 
                if (guideTerms.contains(termToRemove)) {

                    guideTerms.remove(termToRemove);
                    guideTerms.add(newName);
                }

                if (XMLguideTermsRelations.containsKey(termToRemove)) {
                    ArrayList<SortItem> existingRelations = XMLguideTermsRelations.get(termToRemove);
                    XMLguideTermsRelations.remove(termToRemove);
                    XMLguideTermsRelations.put(newName, existingRelations);
                }


                Enumeration<String> guideTermsEnum = XMLguideTermsRelations.keys();
                while (guideTermsEnum.hasMoreElements()) {
                    String targetTerm = guideTermsEnum.nextElement();
                    ArrayList<SortItem> gts = XMLguideTermsRelations.get(targetTerm);
                    for (int m = 0; m < gts.size(); m++) {
                        SortItem item = gts.get(m);
                        if (item.log_name.equals(termToRemove)) {
                            XMLguideTermsRelations.get(targetTerm).get(m).log_name = newName;
                        }
                    }
//
//                    ArrayList<SortItem> gtsToRemove = new ArrayList<SortItem>();
//                    for (int m = 0; m < gts.size(); m++) {
//                    SortItem item = gts.get(m);
//                    if (item.log_name.equals(termToRemove)) {
//                    gtsToRemove.add(item);
//                    }
//                    }
//
//                    XMLguideTermsRelations.get(targetTerm).removeAll(gtsToRemove);
//                     
//                     

                }


            }
        }

        Enumeration<String> termsInfoEnumForSources = termsInfo.keys();
        while (termsInfoEnumForSources.hasMoreElements()) {
            String targetTerm = termsInfoEnumForSources.nextElement();
            ArrayList<String> primarySources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd);
            ArrayList<String> translationSources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd);
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

        ArrayList<String> sourcesToRemove = new ArrayList<String>();
        HashMap<String, String> sourcesToRename = new HashMap<String, String>();
        ArrayList<String> allSources = new ArrayList<String>(XMLsources.keySet());

//        for (int k = 0; k < allSources.size(); k++) {
//            String targetSource = allSources.get(k);
//            String newName = "";
//            try {
//                byte[] byteArray = targetSource.getBytes("UTF-8");
//
//                int maxTermChars = dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session);
//                if (byteArray.length > maxTermChars) {
//
//                    if (sourcesToRemove.contains(targetSource) == false) {
//                        sourcesToRemove.add(targetSource);
//                    }
//                    newName = XMLHandling.ParseFileData.UnlabeledPrefix + (unlabeledCounter++);
//                    String oldSn = XMLsources.get(targetSource);
//                    sourcesToRename.put(targetSource, newName);
//                    XMLsources.remove(targetSource);
//                    XMLsources.put(newName, oldSn);
//
//                    StringObject warningMsg = new StringObject();
//                    errorArgs.clear();
//                    errorArgs.add(targetSource);
//                    errorArgs.add(newName);
//                    errorArgs.add("" + maxTermChars);
//                    errorArgs.add("" + byteArray.length);
//                    dbGen.Translate(warningMsg, "root/EditSource/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
//                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
//                    try {
//                        logFileWriter.append("\r\n<targetTerm>");
//                        logFileWriter.append("<name>" + Utilities.escapeXML(targetSource) + "</name>");
//                        logFileWriter.append("<errorType>" + "sourcename" + "</errorType>");
//                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(newName) + "</errorValue>");
//                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
//                        logFileWriter.append("</targetTerm>\r\n");
//                    } catch (IOException ex) {
//                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
//                        Utils.StaticClass.handleException(ex);
//                    }
//
//
//
//                }
//            } catch (UnsupportedEncodingException ex) {
//                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
//                Utils.StaticClass.handleException(ex);
//            }
//
//            //check source_note
//            String srcname = "";
//            String sourceNoteStr = "";
//            if (sourcesToRemove.contains(targetSource) == false) {
//                srcname = targetSource;
//                sourceNoteStr = XMLsources.get(targetSource);
//            } else {
//                srcname = newName;
//                sourceNoteStr = XMLsources.get(newName);
//            }
//            try {
//                byte[] byteArray = sourceNoteStr.getBytes("UTF-8");
//
//                int maxTermChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
//                if (byteArray.length > maxTermChars) {
//
//                    XMLsources.put(srcname, "");
//
//                    StringObject warningMsg = new StringObject();
//                    errorArgs.clear();
//                    errorArgs.add(srcname);
//                    errorArgs.add("" + maxTermChars);
//                    errorArgs.add("" + byteArray.length);
//                    dbGen.Translate(warningMsg, "root/EditSource/Creation/LongSourceNoteErrorResolve", errorArgs, pathToMessagesXML);
//                    Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
//                    try {
//                        logFileWriter.append("\r\n<targetTerm>");
//                        logFileWriter.append("<name>" + Utilities.escapeXML(srcname) + "</name>");
//                        logFileWriter.append("<errorType>" + ConstantParameters.source_note_kwd + "</errorType>");
//                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(sourceNoteStr) + "</errorValue>");
//                        logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
//                        logFileWriter.append("</targetTerm>\r\n");
//                    } catch (IOException ex) {
//                        Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
//                        Utils.StaticClass.handleException(ex);
//                    }
//
//
//
//                }
//            } catch (UnsupportedEncodingException ex) {
//                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
//                Utils.StaticClass.handleException(ex);
//            }
//
//
//        }
//

        if (sourcesToRemove.size() > 0) {
            for (int k = 0; k < sourcesToRemove.size(); k++) {
                String sourceForRemoval = sourcesToRemove.get(k);
                String newName = sourcesToRename.get(sourceForRemoval);
                Enumeration<String> termsInfoEnum = termsInfo.keys();
                while (termsInfoEnum.hasMoreElements()) {
                    String targetTerm = termsInfoEnum.nextElement();
                    ArrayList<String> primarySources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.primary_found_in_kwd);
                    ArrayList<String> translationSources = termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.translations_found_in_kwd);
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


//        ArrayList<String> gtsToRemove = new ArrayList<String>();
//
//        Enumeration<String> guideTermsEnum = XMLguideTermsRelations.keys();
//        while (guideTermsEnum.hasMoreElements()) {
//            String targetTerm = guideTermsEnum.nextElement();
//            ArrayList<SortItem> gts = XMLguideTermsRelations.get(targetTerm);
//
//            
//            for (int m = 0; m < gts.size(); m++) {
//                SortItem item = gts.get(m);
//                String gtTerm = item.linkClass;
//                try {
//                    byte[] byteArray = gtTerm.getBytes("UTF-8");
//
//                    int maxTermChars = dbtr.getMaxBytesForGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session);
//                    if (byteArray.length > maxTermChars) {
//
//                        //resolve error
//                        item.linkClass = "";
//                        gtsToRemove.add(gtTerm);
//                        XMLguideTermsRelations.get(targetTerm).set(m, item);
//                        if (guideTerms.contains(gtTerm)) {
//                            guideTerms.remove(gtTerm);
//                        }
//
//                        StringObject warningMsg = new StringObject();
//                        errorArgs.clear();
//                        errorArgs.add(gtTerm);
//                        errorArgs.add("" + maxTermChars);
//                        errorArgs.add("" + byteArray.length);
//                        dbGen.Translate(warningMsg, "root/EditGuideTerms/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
//                        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
//                        try {
//                            logFileWriter.append("\r\n<targetTerm>");
//                            logFileWriter.append("<name>" + Utilities.escapeXML(gtTerm) + "</name>");
//                            logFileWriter.append("<errorType>" + ConstantParameters.guide_term_kwd + "</errorType>");
//                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(gtTerm) + "</errorValue>");
//                            logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
//                            logFileWriter.append("</targetTerm>\r\n");
//                        } catch (IOException ex) {
//                            Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
//                            Utils.StaticClass.handleException(ex);
//                        }
//
//
//
//                    }
//                } catch (UnsupportedEncodingException ex) {
//                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
//                    Utils.StaticClass.handleException(ex);
//                }
//
//            }
//            
//
//        }
//
        //dbGen.CloseDBConnection(Q, null, sis_session, null, false);

    }
*/