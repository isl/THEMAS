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
import DB_Classes.DBCreate_Modify_Hierarchy;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBCreate_Modify_Term;
import DB_Classes.DBCreate_Modify_Facet;
import DB_Classes.DBConnect_Term;
import Users.DBFilters;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.NodeInfoSortItemContainer;
import Utils.NodeInfoStringContainer;
import Utils.Utilities;
import Utils.Parameters;

import Utils.SortItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Locale;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import javax.servlet.ServletContext;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class DBMergeThesauri {

    public static final int restartInterval = 1000;

    public DBMergeThesauri() {
    }

    public boolean CreateThesaurus(UserInfoClass refSessionUserInfo, DBGeneral dbGen, ConfigDBadmin config, CommonUtilsDBadmin common_utils,
            String mergedThesaurusName, String mergedThesaurusNameDBformatted,
            ArrayList<String> thesauriNames, StringObject CreateThesaurusResultMessage,
            String backUpDescription, StringObject DBbackupFileNameCreated) {

        QClass Q = new QClass();
        IntegerObject sis_session = new IntegerObject();

        //open connection and start Query
        if (dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBMergeThesauri CreateThesaurus()");
            return false;
        }

        // check if data base is initialized //propably not needed in this servlet
        boolean DataBaseIsInitialized = common_utils.DataBaseIsInitialized(Q);

        dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        //end query and close connection
        Q.free_all_sets();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session, null, false);

        DBAdminUtilities dbAdminUtils = new DBAdminUtilities();

        // initialize DB if chekbox was selected or DB is not initialized
        StringObject InitializeDBResultMessage = new StringObject("");

        // initialize DB if chekbox was selected or DB is not initiali
        Boolean DBInitializationSucceded = true;
        if (DataBaseIsInitialized == false) {
            boolean DBCanBeInitialized = dbAdminUtils.DBCanBeInitialized(config, common_utils, mergedThesaurusNameDBformatted, InitializeDBResultMessage, DBInitializationSucceded);
            if (DBCanBeInitialized == true) {
                DBInitializationSucceded = dbAdminUtils.InitializeDB(common_utils, InitializeDBResultMessage);
                // clear the vector with the existing Thesaurus in DB after DB initialization
                thesauriNames.clear();
            }
        }
        

        // do the creation of the new thesaurus        
        Boolean CreateThesaurusSucceded = true;
        if (DBInitializationSucceded == true) {
            // check if the given NewThesaurusName exists
            boolean GivenThesaurusCanBeCreated = dbAdminUtils.GivenThesaurusCanBeCreated(config, common_utils, thesauriNames, mergedThesaurusName, mergedThesaurusNameDBformatted, CreateThesaurusResultMessage, CreateThesaurusSucceded);
            if (GivenThesaurusCanBeCreated == true) {
                CreateThesaurusSucceded = dbAdminUtils.CreateThesaurus(refSessionUserInfo, common_utils, mergedThesaurusNameDBformatted, CreateThesaurusResultMessage, backUpDescription, DBbackupFileNameCreated);
                // after finishing the job and in case SIS server is not running, restart it
                // ATTENTION!!! the following must be done so as to fix the SARUMAN bug
                // where after the creation of the Thesaurus, the SIS server was NOT restarted!
                // In my machine, this bug is NOT reproduced...
                //common_utils.RestartDatabaseIfNeeded();                
                // start server
                boolean serverStarted = common_utils.StartDatabase();
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 5");
                if (serverStarted == false) {

                    String StartServerFailure = common_utils.config.GetTranslation("StartServerFailure");
                    CreateThesaurusResultMessage.setValue(StartServerFailure);
                    common_utils.RestartDatabaseIfNeeded();
                }
                // wait until server is finally started
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 6");
                /*
                 boolean databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
                 while (databaseIsRunning == false) {
                 //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 7");
                 databaseIsRunning = common_utils.ProcessIsRunning(common_utils.MachineName, common_utils.DatabaseFullPath, common_utils.DatabaseName);
                 }*/
                // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 8");
            }
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 8");
            // Get the existing Thesaurus in DB (ALSO AFTER the creation of the new thesaurus, so as to be informed with the new one)
            if (CreateThesaurusSucceded == true) {
                thesauriNames.add(mergedThesaurusName);
                //disabled cause of unrepeatable 
                //dbAdminUtils.RefreshThesaurusVector(sessionInstance, Q, TA, sis_session, tms_session, dbGen, thesauriNames);
            }
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Reached Here 9");
        }

        return CreateThesaurusSucceded;
    }

    public boolean CopyGuideTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName, StringObject resultObj, String pathToMessagesXML, OutputStreamWriter logFileWriter) throws IOException {

        //Step2: collect Guide term Relations
        //Step3: perform creation        
        //data structures that need to be prepared
        ArrayList<String> allGuideTerms = new ArrayList<String>(); //Filled in step: 1
        HashMap<String, ArrayList<SortItem>> guideTermsRelations = new HashMap<String, ArrayList<SortItem>>();//filled in step: 2

        //tools
        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        /**
         * *****************************START OF STEP: 2**********************
         */
        //COLLECT GUIDE TERMED RELATION FROM THESAURUS 1 IN CASE OF MERGE, COPY, IMPORT thesaurus
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        //Parrallel Vectors that will hold all guide termed links data
        ArrayList<String> bts = new ArrayList<String>();
        ArrayList<SortItem> nts = new ArrayList<SortItem>();

        StringObject BTFromObj = new StringObject();
        StringObject BTLinkObj = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTFromObj, BTLinkObj, Q, sis_session);
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        Q.reset_name_scope();
        Q.set_current_node(BTFromObj);
        Q.set_current_node(BTLinkObj);
        int set_guideTerms1 = Q.get_subclasses(0);
        Q.reset_set(set_guideTerms1);

        int set_all_guide_termed_links_thes1 = Q.get_all_instances(set_guideTerms1);
        Q.reset_set(set_all_guide_termed_links_thes1);
        Q.free_set(set_guideTerms1);

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject traversed = new IntegerObject();
        //CMValue cmv = new CMValue();
        ArrayList<Return_Full_Link_Row> retFLVals = new ArrayList<Return_Full_Link_Row>();
        if (Q.bulk_return_full_link(set_all_guide_termed_links_thes1, retFLVals) != QClass.APIFail) {
            for (Return_Full_Link_Row row : retFLVals) {
                //while (Q.retur_full_link(set_all_guide_termed_links_thes1, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                String broderTerm = row.get_v5_cmv().getString();
                String nt = row.get_v1_cls();
                String linkClass = row.get_v3_categ();

                broderTerm = broderTerm.replaceFirst(termPrefix, "");
                linkClass = linkClass.replaceFirst(BTLinkObj.getValue(), "");
                nt = nt.replaceFirst(termPrefix, "");

                SortItem candidateEntry = new SortItem(nt, -1, linkClass);

                bts.add(broderTerm);
                nts.add(candidateEntry);
            }
        }

        /*while (Q.retur_full_link(set_all_guide_termed_links_thes1, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

         String broderTerm = cmv.getString();
         String nt = cls.getValue();
         String linkClass = categ.getValue();

         broderTerm = broderTerm.replaceFirst(termPrefix, "");
         linkClass = linkClass.replaceFirst(BTLinkObj.getValue(), "");
         nt = nt.replaceFirst(termPrefix, "");

         SortItem candidateEntry = new SortItem(nt, -1, linkClass);

         bts.add(broderTerm);
         nts.add(candidateEntry);
         }*/
        Q.free_set(set_all_guide_termed_links_thes1);

        //COLLECT GUIDE TERMED RELATION FROM THESAURUS 2 IN CASE OF MERGE
        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTFromObj, BTLinkObj, Q, sis_session);
            termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            Q.reset_name_scope();
            Q.set_current_node(BTFromObj);
            Q.set_current_node(BTLinkObj);
            int set_guideTerms2 = Q.get_subclasses(0);
            Q.reset_set(set_guideTerms2);

            int set_all_guide_termed_links_thes2 = Q.get_all_instances(set_guideTerms2);
            Q.reset_set(set_all_guide_termed_links_thes2);
            Q.free_set(set_guideTerms2);

            retFLVals.clear();
            if (Q.bulk_return_full_link(set_all_guide_termed_links_thes2, retFLVals) != QClass.APIFail) {
                for (Return_Full_Link_Row row : retFLVals) {
                    //while (Q.retur_full_link(set_all_guide_termed_links_thes2, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                    String broderTerm = row.get_v5_cmv().getString();
                    String nt = row.get_v1_cls();
                    String linkClass = row.get_v3_categ();

                    broderTerm = broderTerm.replaceFirst(termPrefix, "");
                    linkClass = linkClass.replaceFirst(BTLinkObj.getValue(), "");
                    nt = nt.replaceFirst(termPrefix, "");

                    SortItem candidateEntry = new SortItem(nt, -1, linkClass);
                    bts.add(broderTerm);
                    nts.add(candidateEntry);
                }
            }
            /*
             while (Q.retur_full_link(set_all_guide_termed_links_thes2, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
             String broderTerm = cmv.getString();
             String nt = cls.getValue();
             String linkClass = categ.getValue();

             broderTerm = broderTerm.replaceFirst(termPrefix, "");
             linkClass = linkClass.replaceFirst(BTLinkObj.getValue(), "");
             nt = nt.replaceFirst(termPrefix, "");

             SortItem candidateEntry = new SortItem(nt, -1, linkClass);
             bts.add(broderTerm);
             nts.add(candidateEntry);
             }*/
            Q.free_set(set_all_guide_termed_links_thes2);

        }

        //allGuideTerms
        //move link informationheld in vectors in 
        for (int i = 0; i < bts.size(); i++) {
            String targetTerm = bts.get(i);
            SortItem targetSortItem = nts.get(i);
            String guideTerm = targetSortItem.getLinkClass();
            if (guideTermsRelations.containsKey(targetTerm) == false) {
                ArrayList<SortItem> newEntry = new ArrayList<SortItem>();
                newEntry.add(targetSortItem);
                guideTermsRelations.put(targetTerm, newEntry);
                if (allGuideTerms.contains(guideTerm) == false) {
                    allGuideTerms.add(guideTerm);
                }
            } else {
                ArrayList<SortItem> currentValues = guideTermsRelations.get(targetTerm);
                int indexOfTestSortItem = indexOfSortItemLogNameInVector(currentValues, targetSortItem);
                if (indexOfTestSortItem == -1) {
                    //this nt value did not exist so add it
                    currentValues.add(targetSortItem);
                    guideTermsRelations.put(targetTerm, currentValues);
                    if (allGuideTerms.contains(guideTerm) == false) {
                        allGuideTerms.add(guideTerm);
                    }
                } else { //already existed but a bt and an nt can only be linked with one guide term
                    //Keep one and report possibility of error (from thesaurus 1) //no change will occur in guideTermsRelations entry
                    String valueThatWillBeKept = currentValues.get(indexOfTestSortItem).getLinkClass();
                    String valueThatWillBeIgnored = targetSortItem.getLinkClass();
                    if (valueThatWillBeKept.compareTo(valueThatWillBeIgnored) != 0) {
                        logFileWriter.append("\r\n"
                                + "<targetTerm>"
                                + "<name>" + Utilities.escapeXML(targetTerm) + "</name>"
                                + "<errorType>" + ConstantParameters.guide_term_kwd + "</errorType>"
                                + "<errorValue>" + Utilities.escapeXML(valueThatWillBeIgnored) + "</errorValue>"
                                
                                +"<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipNodeLabel", new String[] { Utilities.escapeXML(targetTerm),Utilities.escapeXML(targetSortItem.getLogName()),valueThatWillBeKept,Utilities.escapeXML(valueThatWillBeIgnored)})+"</reason>"
                                //+"<reason>Term: '" + Utilities.escapeXML(targetTerm) + "' has an already defined NT relationship with term: '" + Utilities.escapeXML(targetSortItem.getLogName())
                                //+ "' and node label: '" + valueThatWillBeKept + "'. There was also detected though another NT relationship among them with node label: '" + Utilities.escapeXML(valueThatWillBeIgnored)
                                //+ "' which was skipped.</reason>"
                                + "</targetTerm>/r/n");
                    }

                }
            }
        }

        return CreateGuideTerms(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                allGuideTerms, guideTermsRelations, mergedThesaurusName, resultObj);

    }

    public int indexOfSortItemLogNameInVector(ArrayList<SortItem> currentValues, SortItem testItem) {

        String testItemLogName = testItem.getLogName();
        int currentValuesSize = currentValues.size();
        for (int i = 0; i < currentValuesSize; i++) {
            String logName = currentValues.get(i).getLogName();
            if (testItemLogName.equals(logName)) {
                return i;
            }
        }
        return -1;
    }

    public boolean CreateGuideTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            ArrayList<String> allGuideTerms, HashMap<String, ArrayList<SortItem>> guideTermsRelations,
            String mergedThesaurusName, StringObject resultObj) {

        UsersClass wtmsUsers = new UsersClass();

        String pathToMessagesXML = Utilities.getXml_For_Messages();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        Q.free_all_sets();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting creation of Guide terms /Node Labels. Time: " + Utilities.GetNow());
        DBEditGuideTerms dbEdit_Guide_Terms = new DBEditGuideTerms();
        int howmanyGts = allGuideTerms.size();
        for (int i = 0; i < howmanyGts; i++) {

            if (i % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GT creation counter: " + (i + 1) + " of " + howmanyGts + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }

            if (dbEdit_Guide_Terms.addGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session, allGuideTerms.get(i), resultObj) == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create guide term / node lable: " + allGuideTerms.get(i) + ".\r\n" + resultObj.getValue());
                return false;
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of creation of guide terms / node labels.");

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Starting update of BT relations with guide terms/ node labels notation.");

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
        int linkRelations = 0;
        int homanyterms = guideTermsRelations.size();
        Iterator<String> XMLguideTermsEnum = guideTermsRelations.keySet().iterator();
        while (XMLguideTermsEnum.hasNext()) {
            if (linkRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Terms gt update counter: " + linkRelations + " of " + homanyterms + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            linkRelations++;
            String targetTerm = XMLguideTermsEnum.next();
            ArrayList<SortItem> targetTermNtsforChange = new ArrayList<SortItem>();
            targetTermNtsforChange.addAll(guideTermsRelations.get(targetTerm));

            //additional structures in order to reuse code for term editing
            ArrayList<String> ntsDecodedValues = new ArrayList<String>();
            ArrayList<String> GuideTermsDecodedValues = new ArrayList<String>();
            int howmanyGuideTermNts = targetTermNtsforChange.size();
            for (int i = 0; i < howmanyGuideTermNts; i++) {
                SortItem currentSortItem = targetTermNtsforChange.get(i);

                String ntName = currentSortItem.getLogName();
                String guideTermName = currentSortItem.getLinkClass();
                if (guideTermName != null && guideTermName.length() > 0) {
                    ntsDecodedValues.add(ntName);
                    GuideTermsDecodedValues.add(guideTermName);
                }
            }

            //edit guide term code reusage
            creation_modificationOfTerm.performGuideTermEditing(SessionUserInfo.selectedThesaurus, Q, sis_session, resultObj, targetTerm, ntsDecodedValues, GuideTermsDecodedValues);

            //error detection
            if (resultObj.getValue() != null && resultObj.getValue().length() > 0) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to add in term: " + targetTerm + " the node labels: " + GuideTermsDecodedValues.toString() + " for the NTs: " + ntsDecodedValues.toString() + ".\r\n" + resultObj.getValue());
                return false;
            }
            //links++;
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of update of BT relations with guide terms/ node labels notation.");
        return true;
    }

    public boolean CopyFacets(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName, DBGeneral dbGen,
            StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        //THEMASUserInfo SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        //Get Facets of Thesaurus 1
        ArrayList<String> facets_thes1 = new ArrayList<String>();
        facets_thes1.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, thesaurusName1, null));

        //Get Facets of Thesaurus 2
        ArrayList<String> facets_thes2 = new ArrayList<String>();
        if (thesaurusName2 != null) { // in oredr to support copy mode -> no second name is given
            facets_thes2.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, thesaurusName2, null));
        }

        //Get All Facets of New merged thesaurus --> UNCLASSFIED TERMS 
        ArrayList<String> mergedFacetNames = new ArrayList<String>();
        mergedFacetNames.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, mergedThesaurusName, null));

        //prepare return Vector which will contain only Facets not included in Merged Thesaurus -- duplicate elimination
        ArrayList<String> merged_thesaurus_NEW_facets = new ArrayList<String>();
        for (int i = 0; i < facets_thes1.size(); i++) {
            if (mergedFacetNames.contains(facets_thes1.get(i)) == false) {
                merged_thesaurus_NEW_facets.add(facets_thes1.get(i));
            }
        }
        for (int i = 0; i < facets_thes2.size(); i++) {
            if (mergedFacetNames.contains(facets_thes2.get(i)) == false && merged_thesaurus_NEW_facets.contains(facets_thes2.get(i)) == false) {
                merged_thesaurus_NEW_facets.add(facets_thes2.get(i));
            }
        }

        return CreateFacets(mergedThesaurusName, Q, TA, sis_session, tms_session,
                merged_thesaurus_NEW_facets, resultObj);

    }

    public boolean CreateFacets(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, ArrayList<String> merged_thesaurus_NEW_facets, StringObject resultObj) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of creating Facets. Time: " + Utilities.GetNow());
        String pathToMessagesXML = Utilities.getXml_For_Messages();
        DBGeneral dbGen = new DBGeneral();

        DBCreate_Modify_Facet creationModificationOfFacet = new DBCreate_Modify_Facet();

        boolean FacetAdditionSucceded = true;

        for (int i = 0; i < merged_thesaurus_NEW_facets.size(); i++) {
            Q.reset_name_scope();
            FacetAdditionSucceded = creationModificationOfFacet.Create_Or_ModifyFacet(selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, merged_thesaurus_NEW_facets.get(i), "create", null, resultObj, false);

            if (FacetAdditionSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create FACETS: " + resultObj.getValue() + ".");
                return false;
            }
            resultObj.setValue("");
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Facet creation.");
        return true;
    }

    public boolean CreateFacetsFromSortItemsVector(String selectedThesaurus, 
                                                   QClass Q, 
                                                   TMSAPIClass TA, 
                                                   IntegerObject sis_session, 
                                                   IntegerObject tms_session, 
                                                   ArrayList<SortItem> merged_thesaurus_NEW_facets, 
                                                   StringObject resultObj,
                                                   boolean resolveError,
                                                   OutputStreamWriter logFileWriter,
                                                   int ConsistencyPolicy) {
        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of creating Facets. Time: " + Utilities.GetNow());
        DBGeneral dbGen = new DBGeneral();

        DBCreate_Modify_Facet creationModificationOfFacet = new DBCreate_Modify_Facet();

        boolean FacetAdditionSucceded = true;

        for (SortItem newFacet: merged_thesaurus_NEW_facets) {
            Q.reset_name_scope();
            FacetAdditionSucceded = creationModificationOfFacet.Create_Or_ModifyFacetSortItem(selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, newFacet, "create", null, resultObj, false, resolveError,logFileWriter, ConsistencyPolicy);

            if (FacetAdditionSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create FACETS: " + resultObj.getValue() + ".");
                return false;
            }
            resultObj.setValue("");
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Facet creation.");
        return true;
    }

    public boolean CopyHierarchies(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergeThesaurus, DBGeneral dbGen,
            String defaultFacet, Locale targetLocale, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        //Start Hierarchies merging
        //if a hierarchy is not under at least one facet (in both thesauri) add it to defaultFacetObj
        //read hiers of thesaurus 1 and thesaurus 2
        HashMap<String, ArrayList<String>> pairsOfThesaurus1 = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, thesaurusName1, null);
        HashMap<String, ArrayList<String>> pairsOfThesaurus2 = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, thesaurusName2, null);

        //read hiers of  merged thesaurus
        HashMap<String, ArrayList<String>> pairsOfMergedThesaurus = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, mergeThesaurus, null);

        //add hiers of thesaurus 1 to merged thesaurus structure
        Iterator<String> pairsEnum1 = pairsOfThesaurus1.keySet().iterator();
        while (pairsEnum1.hasNext()) {
            String hierarchy = pairsEnum1.next();
            ArrayList<String> thes1Vals = pairsOfThesaurus1.get(hierarchy);

            if (pairsOfMergedThesaurus.containsKey(hierarchy) == false) {
                pairsOfMergedThesaurus.put(hierarchy, thes1Vals);
            } else {
                ArrayList<String> mergedVals = pairsOfMergedThesaurus.get(hierarchy);

                for (int i = 0; i < thes1Vals.size(); i++) {
                    if (mergedVals.contains(thes1Vals) == false) {
                        mergedVals.add(thes1Vals.get(i));
                    }
                }
                pairsOfMergedThesaurus.put(hierarchy, mergedVals);
            }
        }

        //add hiers of thesaurus 2 to merged thesaurus structure (containing hiers of thes1)
        if (thesaurusName2 != null) {
            Iterator<String> pairsEnum2 = pairsOfThesaurus2.keySet().iterator();
            while (pairsEnum2.hasNext()) {
                String hierarchy = pairsEnum2.next();
                ArrayList<String> thes2Vals = pairsOfThesaurus2.get(hierarchy);

                if (pairsOfMergedThesaurus.containsKey(hierarchy) == false) {
                    pairsOfMergedThesaurus.put(hierarchy, thes2Vals);
                } else {
                    ArrayList<String> mergedVals = pairsOfMergedThesaurus.get(hierarchy);

                    for (int i = 0; i < thes2Vals.size(); i++) {
                        if (mergedVals.contains(thes2Vals) == false) {
                            mergedVals.add(thes2Vals.get(i));
                        }
                    }
                    pairsOfMergedThesaurus.put(hierarchy, mergedVals);
                }
            }
        }

        /*
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nREADING HIERS OF MERGED THESAURUS : " + mergeThesaurus + "\n----------------------------\n\n");
         int i=1;
         Enumeration<String> pairsEnumMerged = pairsOfMergedThesaurus.keys();
         while(pairsEnumMerged.hasMoreElements()){
         String hierarchy = pairsEnumMerged.nextElement();
        
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+i+". " +hierarchy + " ----- " + pairsOfMergedThesaurus.get(hierarchy).toString());
         i++;
         }
         */
        return CreateHierarchies(refSessionUserInfo, Q, TA, sis_session, tms_session,
                mergeThesaurus, defaultFacet, targetLocale, resultObj, logFileWriter, pairsOfMergedThesaurus);

    }

    public boolean CreateHierarchies(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String targetThesaurus, String defaultFacet, Locale targetLocale, StringObject resultObj, OutputStreamWriter logFileWriter,
            HashMap<String, ArrayList<String>> pairsOfMergedThesaurus) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of Hierarchies creation. Time: " + Utilities.GetNow());
        boolean HierarchiesSucceeded = true;
        try {

            //String pathToMessagesXML = Utilities.getXml_For_Messages();
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            UsersClass wtmsUsers = new UsersClass();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            DBCreate_Modify_Hierarchy creationModificationOfHierarchy = new DBCreate_Modify_Hierarchy();

            //Set session user info to mergedThesaurus
            //THEMASUserInfo SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            //update all fields in order to keep it in consistent state
            UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, targetThesaurus);

            String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            StringObject resultMessageObj = new StringObject();
            //ArrayList<String> errorArgs = new ArrayList<String>();

            //Now for each pair of hierarchyplus facets create hierarchy and then change facets. If no facet is declared add hier under default facet
            Iterator<String> pairsEnumMerged = pairsOfMergedThesaurus.keySet().iterator();
            while (pairsEnumMerged.hasNext()) {
                String hierarchy = pairsEnumMerged.next();
                ArrayList<String> underFacets = pairsOfMergedThesaurus.get(hierarchy);
                if (underFacets.size() == 0) {

                    logFileWriter.append("\r\n<targetHierarchy><name>" + Utilities.escapeXML(hierarchy) + "</name><errorType>facet</errorType><errorValue>" + Utilities.escapeXML(defaultFacet) + "</errorValue>");
                    logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateHierarchies/WrongHierarchyPosition", new String[]{Utilities.escapeXML(hierarchy),Utilities.escapeXML(defaultFacet)}) + "</reason>");
                    //logFileWriter.append("<reason>Hierarchy: " + Utilities.escapeXML(hierarchy) + " was found without being classified under any Facet. It is therefore by default classified under the default Facet: " + Utilities.escapeXML(defaultFacet) + ".</reason>");
                    logFileWriter.append("</targetHierarchy>\r\n");
                    underFacets.add(defaultFacet);
                }

                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchy));
                Q.reset_name_scope();

                if (Q.set_current_node(hierarchyObj) == QClass.APIFail) {
                    //create hierarchy
                    Q.reset_name_scope();
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "create", null, SessionUserInfo.name, targetLocale, resultObj, false);
                    //logFileWriter.append(resultObj.getValue()+"\r\n");
                    if (HierarchiesSucceeded == true && underFacets.size() > 1) {

                        resultObj.setValue("");
                        HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false);

                        //logFileWriter.append(resultObj.getValue()+"\r\n");
                    }
                } else {
                    //modify hierarchy
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false);
                }

                if (HierarchiesSucceeded == false) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create Hierarchies: " + resultObj.getValue());
                    break;
                } else {
                    resultObj.setValue("");
                }
            }

            if (HierarchiesSucceeded) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Hierarchies creation.");
            }
        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass().toString());
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return HierarchiesSucceeded;
    }
    
    public boolean CreateHierarchiesFromSortItems(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
                                                 String targetThesaurus, 
                                                 SortItem defaultFacet, 
                                                 Locale targetLocale, 
                                                 StringObject resultObj, 
                                                 HashMap<SortItem, ArrayList<String>> pairsOfMergedThesaurus,
                                                 
                                                 boolean resolveError,
                                                 OutputStreamWriter logFileWriter, 
                                                 int ConsistencyChecksPolicy) {
        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start of Hierarchies creation. Time: " + Utilities.GetNow());
        boolean HierarchiesSucceeded = true;
        try {

            
            //String pathToMessagesXML = Utilities.getXml_For_Messages();
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            UsersClass wtmsUsers = new UsersClass();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            DBCreate_Modify_Hierarchy creationModificationOfHierarchy = new DBCreate_Modify_Hierarchy();

            //Set session user info to mergedThesaurus
            //THEMASUserInfo SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            //update all fields in order to keep it in consistent state
            UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, targetThesaurus);

            String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            String defaultFacetNameWithoutPrefix = defaultFacet.getLogName();
            if(defaultFacetNameWithoutPrefix.startsWith(prefix_class)){
                defaultFacetNameWithoutPrefix = dbGen.removePrefix(defaultFacetNameWithoutPrefix);
            }
            StringObject resultMessageObj = new StringObject();
            //ArrayList<String> errorArgs = new ArrayList<String>();

            //Now for each pair of hierarchyplus facets create hierarchy and then change facets. If no facet is declared add hier under default facet
            Iterator<SortItem> pairsEnumMerged = pairsOfMergedThesaurus.keySet().iterator();
            while (pairsEnumMerged.hasNext()) {
                SortItem hierarchy = pairsEnumMerged.next();
                ArrayList<String> underFacets = pairsOfMergedThesaurus.get(hierarchy);
                
                String hierarchyNameWithoutPrefix = hierarchy.getLogName();
                if(hierarchyNameWithoutPrefix.startsWith(prefix_class)){
                    hierarchyNameWithoutPrefix = dbGen.removePrefix(hierarchyNameWithoutPrefix);
                }                
                
                if (underFacets.size() == 0) {

                    logFileWriter.append("\r\n<targetHierarchy><name>" + Utilities.escapeXML(hierarchyNameWithoutPrefix) + "</name><errorType>facet</errorType><errorValue>" + Utilities.escapeXML(defaultFacetNameWithoutPrefix) + "</errorValue>");
                    logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateHierarchies/WrongHierarchyPosition", new String[]{Utilities.escapeXML(hierarchyNameWithoutPrefix),Utilities.escapeXML(defaultFacetNameWithoutPrefix)}) + "</reason>");
                    //logFileWriter.append("<reason>Hierarchy: " + Utilities.escapeXML(hierarchy) + " was found without being classified under any Facet. It is therefore by default classified under the default Facet: " + Utilities.escapeXML(defaultFacet) + ".</reason>");
                    logFileWriter.append("</targetHierarchy>\r\n");
                    underFacets.add(defaultFacet.getLogName());
                }

                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyNameWithoutPrefix));
                Q.reset_name_scope();

                
                if (Q.set_current_node(hierarchyObj) == QClass.APIFail) {
                    //create hierarchy
                    Q.reset_name_scope();
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchySortItem(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "create", null, SessionUserInfo.name, targetLocale, resultObj, false,resolveError,logFileWriter,ConsistencyChecksPolicy);
                    //logFileWriter.append(resultObj.getValue()+"\r\n");
                    if (HierarchiesSucceeded == true && underFacets.size() > 1) {

                        resultObj.setValue("");
                        HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchySortItem(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false,resolveError,logFileWriter,ConsistencyChecksPolicy);

                        //logFileWriter.append(resultObj.getValue()+"\r\n");
                    }
                } else {
                    //modify hierarchy
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchySortItem(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false,resolveError,logFileWriter,ConsistencyChecksPolicy);
                }

                if (HierarchiesSucceeded == false) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to create Hierarchies: " + resultObj.getValue());
                    break;
                } else {
                    resultObj.setValue("");
                }
            }

            if (HierarchiesSucceeded) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Hierarchies creation.");
            }
        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass().toString());
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return HierarchiesSucceeded;
    }

    /* ABANDONED CODE
    public boolean CopyTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String sourceThesaurusName, String mergedThesaurusName, String pathToErrorsXML,
            Locale targetLocale, StringBuffer warnignsBuffer, StringObject resultObj, OutputStreamWriter logFileWriter, int ConsistencyCheckPolicy) throws IOException {

        boolean keepCopying = true;
        long startTime;
        long elapsedTimeMillis;
        float elapsedTimeSec;
        //------------------TERMS OF THES1 START----------------------------

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }
        //<editor-fold defaultstate="collapsed" desc="Terms and BTs">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying terms (and BTs).");
        logFileWriter.flush();
        startTime = System.currentTimeMillis();
        keepCopying = CopyTermsLevelByLevel(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);
        //</editor-fold>

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }
        //<editor-fold defaultstate="collapsed" desc="RTs">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying terms (and BTs) in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying RTs.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            //keepCopying = CopyRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy, warnignsBuffer);
            keepCopying = CopyRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold>  

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Status">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying RTs in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart copying term Status.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyStatuses(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj);
        }
        //</editor-fold >  

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="CommentCategories">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying term status in " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart copying SN, tr_SN, HN.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold>  

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="translations">
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying SN, tr_SN, HN in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart copying translations.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.translation_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="UF..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying translations in: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            ArrayList<String> errorProneUFs = new ArrayList<String>();
            errorProneUFs.addAll(CollectErrorProneUfs(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null, logFileWriter));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying UFs.");

            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.uf_kwd, errorProneUFs, resultObj, ConsistencyCheckPolicy);

        }
        //</editor-fold>   

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="UF TRANSLATIONS...">
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying UFs in time: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            ArrayList<String> errorProneUFTranslations = new ArrayList<String>();

            if (Parameters.TermModificationChecks.contains(18)) {
                errorProneUFTranslations.addAll(CollectErrorProneUFTranslations(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null, logFileWriter));
            }

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying UF translations.");

            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.uf_translations_kwd, errorProneUFTranslations, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold >

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="TCs..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying UF translations in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart copying TCs.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.tc_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Primary Source...">
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying TCs in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart copying primary sources.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.primary_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Translations Source...">
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of coppying primary sources in:" + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying translation sources.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.translations_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Created By / ON ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying translation sources in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying Created BY / ON fileds.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.created_by_kwd, resultObj);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Modified By / ON ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying Created BY / ON fields in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of copying Modified BY /ON fields.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.modified_by_kwd, resultObj);
        }
        //</editor-fold>

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of copying Modified BY / ON fields in: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
        }

        return keepCopying;

    }
*/
    public boolean MergeTerms(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String sourceThesaurusName1, String sourceThesaurusName2, String mergedThesaurusName, String pathToErrorsXML,
            Locale targetLocale, StringBuffer warnignsBuffer, StringObject resultObj, OutputStreamWriter logFileWriter, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();

        boolean keepCopying = true;
        long startTime;
        long elapsedTimeMillis;
        float elapsedTimeSec;
        //------------------TERMS OF THES1 START----------------------------

        //<editor-fold defaultstate="collapsed" desc="Terms and BTs">
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging terms (and BTs).");
        logFileWriter.flush();
        startTime = System.currentTimeMillis();
        keepCopying = CopyTermsLevelByLevel(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="RTs">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging terms (and BTs) in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging RTs .");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy/*, warnignsBuffer*/);
        }
        //</editor-fold>  

        //<editor-fold defaultstate="collapsed" desc="Status">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging RTs in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging term statuses.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyStatuses(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj);
        }
        //</editor-fold >  

        //<editor-fold defaultstate="collapsed" desc="CommentCategories">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging term statuses in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging SN, SN (Tra.) and HN.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold>  

        //<editor-fold defaultstate="collapsed" desc="EN">   
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging SN, SN (Tra.) and HN in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging translations.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.translation_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="UF..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging translations in: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            ArrayList<String> errorProneUFs = new ArrayList<String>();
            errorProneUFs.addAll(CollectErrorProneUfs(refSessionUserInfo, Q, sis_session, sourceThesaurusName1, sourceThesaurusName2, logFileWriter));
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of mergis UFs.");
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.uf_kwd, errorProneUFs, resultObj, ConsistencyCheckPolicy);

        }
        //</editor-fold>   

        //<editor-fold defaultstate="collapsed" desc="UK_UF..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging UFs in: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            ArrayList<String> errorProneUFTranslations = new ArrayList<String>();

            if (Parameters.TermModificationChecks.contains(18)) {
                errorProneUFTranslations.addAll(CollectErrorProneUFTranslations(refSessionUserInfo, Q, sis_session, sourceThesaurusName1, sourceThesaurusName2, logFileWriter));
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging UF (Tra.).");
            logFileWriter.flush();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.uf_translations_kwd, errorProneUFTranslations, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold >

        //<editor-fold defaultstate="collapsed" desc="TCs..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging UF (Tra.) in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging TCs.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.tc_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Primary Source..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging TCs in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging primary sources.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.primary_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Translations Source..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging primary sources in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging tranlation sources.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.translations_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Created By / ON ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging translation sources in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging Created BY / ON fields.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.created_by_kwd, resultObj);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Modified BY / ON ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging Created BY / ON fields in: " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tStart of merging Modified BY / ON fields.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.modified_by_kwd, resultObj);
        }
        //</editor-fold>

        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tEnd of merging Modified BY / ON fields in: " + elapsedTimeSec + " min.");
            logFileWriter.flush();
        }

        return keepCopying;
    }

    public ArrayList<String> ReadThesaurusGuideTerms(String selectedThesaurus, QClass Q, IntegerObject sis_session, String thesaurusName) {
        ArrayList<String> thesaurus_guideTerms = new ArrayList<String>();

        DBGeneral dbGen = new DBGeneral();

        thesaurus_guideTerms.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        return thesaurus_guideTerms;
    }

    public ArrayList<String> ReadThesaurusFacets(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        ArrayList<String> returnResults = new ArrayList<String>();
        returnResults.addAll(Utilities.getStringVectorFromSortItemVector(ReadThesaurusFacetsInSortItems(refSessionUserInfo,Q,sis_session,thesaurusName1,thesaurusName2)));
        return returnResults;
    }

    public ArrayList<SortItem> ReadThesaurusFacetsInSortItems(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        DBGeneral dbGen = new DBGeneral();
        ArrayList<SortItem> thesaurus_facets = new ArrayList<SortItem>();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Q.reset_name_scope();
        //Find out facet Classes and get their instances
        int index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        int set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_facets);

        
        thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_facets, true, Q, sis_session));
        Q.free_set(set_facets);

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);
            Q.reset_name_scope();

            //Find out facet Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("FACET");
            FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
            set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
            Q.reset_set(set_facets);

            ArrayList<SortItem> thesaurus_facetsOfThes2 = new ArrayList<SortItem>();
            thesaurus_facetsOfThes2.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_facets, true, Q, sis_session));
            Q.free_set(set_facets);
            
            ArrayList<String> thesaurus_facetsOfThes1Strs = new ArrayList<String>();
            if(thesaurus_facets!=null){
                for(SortItem item: thesaurus_facets){
                    thesaurus_facetsOfThes1Strs.add(item.getLogName());
                }
            }

            for (int k = 0; k < thesaurus_facetsOfThes2.size(); k++) {
                SortItem checkFacet = thesaurus_facetsOfThes2.get(k);
                
            //skipping id a new one will be assigned
                String checkLogName = checkFacet.getLogName();
                String translit = checkFacet.getLogNameTransliteration();
                        
                if (thesaurus_facetsOfThes1Strs.contains(checkFacet.getLogName()) == false) {
                    
                    
                    thesaurus_facets.add(new SortItem(checkLogName,-1,translit,-1));
                }
            }
        }
        return thesaurus_facets;
    }
    
    public void ReadTheasaurusTermCommentCategories(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            String thesaurusName1, String thesaurusName2, HashMap<String, NodeInfoStringContainer> termsInfo) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        UsersClass wtmsUsers = new UsersClass();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        StringObject scopenoteFromClassObj = new StringObject();
        StringObject scopenoteLinkObj = new StringObject();
        StringObject scopenote_TR_FromClassObj = new StringObject();
        StringObject scopenote_TR_LinkObj = new StringObject();
        StringObject historicalnoteFromClassObj = new StringObject();
        StringObject historicalnoteLinkObj = new StringObject();
        
        StringObject commentFromClassObj = new StringObject();
        StringObject commentLinkObj = new StringObject();
        StringObject noteFromClassObj = new StringObject();
        StringObject noteLinkObj = new StringObject();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
        int set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
        Q.reset_set(set_terms);

        ArrayList<String> termsOfThes1 = new ArrayList<String>();
        termsOfThes1.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName1 + ".");

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.comment_kwd, commentFromClassObj, commentLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.note_kwd, noteFromClassObj, noteLinkObj, Q, sis_session);

        //Enumeration<String> termEnum = termsInfo.keys();
        //while(termEnum.hasMoreElements()){
        //String targetTerm = termEnum.nextElement();
        IntegerObject sysIdObj = new IntegerObject();
        StringObject nodeNameObj = new StringObject();
        StringObject classObj = new StringObject();

        //SCOPE NOTES 
        ArrayList<String> terms_with_sn_Vec = new ArrayList<String>();

        Q.reset_name_scope();

        Q.set_current_node(scopenoteFromClassObj);
        Q.set_current_node(scopenoteLinkObj);
        int set_all_links_sn = Q.get_all_instances(0);
        Q.reset_set(set_all_links_sn);
        int set_terms_with_sn = Q.get_from_value(set_all_links_sn);
        Q.reset_set(set_terms_with_sn);
        Q.free_set(set_all_links_sn);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_terms_with_sn, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String targetTerm = row.get_v1_cls_logicalname();
                terms_with_sn_Vec.add(targetTerm);
            }
        }
        /*
         //while (Q.retur_full_nodes(set_terms_with_sn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
         //if (resultNodesIds.contains(sysIdObj.getValue())) {
         String targetTerm = nodeNameObj.getValue();
         terms_with_sn_Vec.add(targetTerm);
         //}
         }*/
        Q.free_set(set_terms_with_sn);

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(thesaurusName1);
        }
        for (int i = 0; i < terms_with_sn_Vec.size(); i++) {

            String targetDBTerm = terms_with_sn_Vec.get(i);
            String targetUITerm = dbGen.removePrefix(targetDBTerm);
            StringObject commentObject = new StringObject("");

            TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, scopenoteFromClassObj, scopenoteLinkObj);

            if (commentObject.getValue().length() > 0) {
                termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.scope_note_kwd).add(commentObject.getValue());
            }
        }
        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }

        //Trnaslation SCOPE NOTES
        ArrayList<String> terms_with_sn_TR_Vec = new ArrayList<String>();
        Q.reset_name_scope();

        Q.set_current_node(scopenote_TR_FromClassObj);
        Q.set_current_node(scopenote_TR_LinkObj);
        int set_all_links_sn_tr = Q.get_all_instances(0);
        Q.reset_set(set_all_links_sn_tr);
        int set_terms_with_sn_tr = Q.get_from_value(set_all_links_sn_tr);
        Q.reset_set(set_terms_with_sn_tr);
        Q.free_set(set_all_links_sn_tr);

        retVals.clear();
        if (Q.bulk_return_nodes(set_terms_with_sn_tr, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String targetTerm = row.get_v1_cls_logicalname();
                terms_with_sn_TR_Vec.add(targetTerm);
            }
        }
        /*
         while (Q.retur_full_nodes(set_terms_with_sn_tr, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
         //if (resultNodesIds.contains(sysIdObj.getValue())) {
         String targetTerm = nodeNameObj.getValue();
         terms_with_sn_TR_Vec.add(targetTerm);
         //}

         }
         */
        Q.free_set(set_terms_with_sn_tr);

        prevThes = new StringObject("");
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(thesaurusName1);
        }
        for (int i = 0; i < terms_with_sn_TR_Vec.size(); i++) {

            String targetDBTerm = terms_with_sn_TR_Vec.get(i);
            String targetUITerm = dbGen.removePrefix(targetDBTerm);
            StringObject commentObject = new StringObject("");
            TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, scopenote_TR_FromClassObj, scopenote_TR_LinkObj);

            if (commentObject.getValue().length() > 0) {
                HashMap<String, String> trSns = u.getTranslationScopeNotes(commentObject.getValue());
                ArrayList<String> langCodes = new ArrayList<String>(trSns.keySet());
                Collections.sort(langCodes);
                for (int m = 0; m < langCodes.size(); m++) {
                    String lang = langCodes.get(m);
                    String val = trSns.get(lang);
                    if (val != null && val.trim().length() > 0) {
                        termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.translations_scope_note_kwd).add(lang + Parameters.TRANSLATION_SEPERATOR + val.trim());
                    }
                }
            }
        }

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }
        
         //Comment Notes
        ArrayList<String> terms_with_comment_Vec = new ArrayList<String>();
        Q.reset_name_scope();

        Q.set_current_node(commentFromClassObj);
        Q.set_current_node(commentLinkObj);
        int set_all_links_comm = Q.get_all_instances(0);
        Q.reset_set(set_all_links_comm);
        int set_terms_with_comment = Q.get_from_value(set_all_links_comm);
        Q.reset_set(set_terms_with_comment);
        Q.free_set(set_all_links_comm);

        retVals.clear();
        if (Q.bulk_return_nodes(set_terms_with_comment, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String targetTerm = row.get_v1_cls_logicalname();
                terms_with_comment_Vec.add(targetTerm);
            }
        }
        /*
         while (Q.retur_full_nodes(set_terms_with_hn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
         //    if (resultNodesIds.contains(sysIdObj.getValue())) {
         String targetTerm = nodeNameObj.getValue();
         terms_with_hn_Vec.add(targetTerm);
         //    }
         }*/
        Q.free_set(set_terms_with_comment);

        prevThes = new StringObject("");
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(thesaurusName1);
        }
        for (int i = 0; i < terms_with_comment_Vec.size(); i++) {

            String targetDBTerm = terms_with_comment_Vec.get(i);
            String targetUITerm = dbGen.removePrefix(targetDBTerm);
            StringObject commentObject = new StringObject("");
            TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, commentFromClassObj, commentLinkObj);

            if (commentObject.getValue().length() > 0) {
                termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.comment_kwd).add(commentObject.getValue());
            }
        }

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }
        

        //Note NOTES
        ArrayList<String> terms_with_notes_Vec = new ArrayList<String>();
        Q.reset_name_scope();

        Q.set_current_node(noteFromClassObj);
        Q.set_current_node(noteLinkObj);
        int set_all_links_notes = Q.get_all_instances(0);
        Q.reset_set(set_all_links_notes);
        int set_terms_with_note = Q.get_from_value(set_all_links_notes);
        Q.reset_set(set_terms_with_note);
        Q.free_set(set_all_links_notes);

        retVals.clear();
        if (Q.bulk_return_nodes(set_terms_with_note, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String targetTerm = row.get_v1_cls_logicalname();
                terms_with_notes_Vec.add(targetTerm);
            }
        }
        /*
         while (Q.retur_full_nodes(set_terms_with_hn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
         //    if (resultNodesIds.contains(sysIdObj.getValue())) {
         String targetTerm = nodeNameObj.getValue();
         terms_with_hn_Vec.add(targetTerm);
         //    }
         }*/
        Q.free_set(set_terms_with_note);

        prevThes = new StringObject("");
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(thesaurusName1);
        }
        for (int i = 0; i < terms_with_notes_Vec.size(); i++) {

            String targetDBTerm = terms_with_notes_Vec.get(i);
            String targetUITerm = dbGen.removePrefix(targetDBTerm);
            StringObject commentObject = new StringObject("");
            TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, noteFromClassObj, noteLinkObj);

            if (commentObject.getValue().length() > 0) {
                termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.note_kwd).add(commentObject.getValue());
            }
        }

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }
        
        
        //HISTORICAL NOTES
        ArrayList<String> terms_with_hn_Vec = new ArrayList<String>();
        Q.reset_name_scope();

        Q.set_current_node(historicalnoteFromClassObj);
        Q.set_current_node(historicalnoteLinkObj);
        int set_all_links_hn = Q.get_all_instances(0);
        Q.reset_set(set_all_links_hn);
        int set_terms_with_hn = Q.get_from_value(set_all_links_hn);
        Q.reset_set(set_terms_with_hn);
        Q.free_set(set_all_links_hn);

        retVals.clear();
        if (Q.bulk_return_nodes(set_terms_with_hn, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String targetTerm = row.get_v1_cls_logicalname();
                terms_with_hn_Vec.add(targetTerm);
            }
        }
        /*
         while (Q.retur_full_nodes(set_terms_with_hn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
         //    if (resultNodesIds.contains(sysIdObj.getValue())) {
         String targetTerm = nodeNameObj.getValue();
         terms_with_hn_Vec.add(targetTerm);
         //    }
         }*/
        Q.free_set(set_terms_with_hn);

        prevThes = new StringObject("");
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(thesaurusName1);
        }
        for (int i = 0; i < terms_with_hn_Vec.size(); i++) {

            String targetDBTerm = terms_with_hn_Vec.get(i);
            String targetUITerm = dbGen.removePrefix(targetDBTerm);
            StringObject commentObject = new StringObject("");
            TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, historicalnoteFromClassObj, historicalnoteLinkObj);

            if (commentObject.getValue().length() > 0) {
                termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.historical_note_kwd).add(commentObject.getValue());
            }
        }

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(thesaurusName1) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }
        
       
        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            index = Parameters.CLASS_SET.indexOf("TERM");
            termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
            set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
            Q.reset_set(set_terms);

            ArrayList<String> termsOfThes2 = new ArrayList<String>();
            termsOfThes2.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName2 + ".");

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.comment_kwd, commentFromClassObj, commentLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.note_kwd, noteFromClassObj, noteLinkObj, Q, sis_session);

            //Enumeration<String> termEnum = termsInfo.keys();
            //while(termEnum.hasMoreElements()){
            //String targetTerm = termEnum.nextElement();
            for (int i = 0; i < termsOfThes2.size(); i++) {
                String targetTerm = termsOfThes2.get(i);
                NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.scope_note_kwd)) {
                    Q.reset_name_scope();
                    ArrayList<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
                    ArrayList<String> scopeNote = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.scope_note_kwd, Q, TA, sis_session);
                    if (existing.size() == 1) {
                        String finalVal = "";

                        String snOld = "";
                        if (existing.size() == 1) {
                            snOld = existing.get(0);
                        }

                        String snNew = "";
                        if (scopeNote.size() == 1) {
                            snNew = scopeNote.get(0);
                        }

                        if (snOld.length() > 0) {
                            finalVal += snOld;
                        }

                        if (snNew.length() > 0 && snNew.equals(snOld) == false) {
                            finalVal = u.mergeStrings(snOld, snNew);
                            /*if (finalVal.length() > 0) {
                             finalVal += "; ";
                             }
                             finalVal += snNew;*/
                        }

                        scopeNote.clear();
                        if (finalVal.length() > 0) {
                            scopeNote.add(finalVal);
                        }
                        targetInfo.descriptorInfo.put(ConstantParameters.scope_note_kwd, scopeNote);
                    } else {
                        targetInfo.descriptorInfo.put(ConstantParameters.scope_note_kwd, scopeNote);
                    }
                }

                

                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.translations_scope_note_kwd)) {
                    Q.reset_name_scope();

                    ArrayList<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
                    ArrayList<String> trSn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
                    if (existing.size() == 1) {
                        String finalVal = "";

                        String trSNOld = "";
                        if (existing.size() == 1) {
                            trSNOld = existing.get(0);
                        }

                        String trSNNew = "";
                        if (trSn.size() == 1) {
                            trSNNew = trSn.get(0);
                        }

                        if (trSNOld.length() > 0) {
                            finalVal += trSNOld;
                        }

                        if (trSNNew.length() > 0 && trSNNew.equals(trSNOld) == false) {
                            finalVal = u.mergeStrings(trSNOld, trSNNew);
                            /*if (finalVal.length() > 0) {
                             finalVal += "; ";
                             }
                             finalVal += trSNNew;*/
                        }

                        trSn.clear();
                        if (finalVal.length() > 0) {
                            trSn.add(finalVal);
                        }
                        targetInfo.descriptorInfo.put(ConstantParameters.translations_scope_note_kwd, trSn);
                    } else {
                        targetInfo.descriptorInfo.put(ConstantParameters.translations_scope_note_kwd, trSn);
                    }

                }

                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.historical_note_kwd)) {
                    Q.reset_name_scope();
                    ArrayList<String> hn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.historical_note_kwd, Q, TA, sis_session);
                    ArrayList<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);

                    if (existing.size() == 1) {
                        String finalVal = "";

                        String hnOld = "";
                        if (existing.size() == 1) {
                            hnOld = existing.get(0);
                        }

                        String hnNew = "";
                        if (hn.size() == 1) {
                            hnNew = hn.get(0);
                        }

                        if (hnOld.length() > 0) {
                            finalVal += hnOld;
                        }

                        if (hnNew.length() > 0 && hnNew.equals(hnOld) == false) {
                            if (finalVal.length() > 0) {
                                finalVal += "; ";
                            }
                            finalVal += hnNew;
                        }

                        hn.clear();
                        if (finalVal.length() > 0) {
                            hn.add(finalVal);
                        }
                        targetInfo.descriptorInfo.put(ConstantParameters.historical_note_kwd, hn);
                    } else {
                        targetInfo.descriptorInfo.put(ConstantParameters.historical_note_kwd, hn);
                    }
                }
                
                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.comment_kwd)) {
                    Q.reset_name_scope();
                    ArrayList<String> comments = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.comment_kwd, Q, TA, sis_session);
                    ArrayList<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.comment_kwd);

                    if (existing.size() == 1) {
                        String finalVal = "";

                        String commentOld = "";
                        if (existing.size() == 1) {
                            commentOld = existing.get(0);
                        }

                        String commentNew = "";
                        if (comments.size() == 1) {
                            commentNew = comments.get(0);
                        }

                        if (commentOld.length() > 0) {
                            finalVal += commentOld;
                        }

                        if (commentNew.length() > 0 && commentNew.equals(commentOld) == false) {
                            if (finalVal.length() > 0) {
                                finalVal += "; ";
                            }
                            finalVal += commentNew;
                        }

                        comments.clear();
                        if (finalVal.length() > 0) {
                            comments.add(finalVal);
                        }
                        targetInfo.descriptorInfo.put(ConstantParameters.comment_kwd, comments);
                    } else {
                        targetInfo.descriptorInfo.put(ConstantParameters.comment_kwd, comments);
                    }
                }
                
                
                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.note_kwd)) {
                    Q.reset_name_scope();
                    ArrayList<String> notes = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.note_kwd, Q, TA, sis_session);
                    ArrayList<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.note_kwd);

                    if (existing.size() == 1) {
                        String finalVal = "";

                        String noteOld = "";
                        if (existing.size() == 1) {
                            noteOld = existing.get(0);
                        }

                        String noteNew = "";
                        if (notes.size() == 1) {
                            noteNew = notes.get(0);
                        }

                        if (noteOld.length() > 0) {
                            finalVal += noteOld;
                        }

                        if (noteNew.length() > 0 && noteNew.equals(noteOld) == false) {
                            if (finalVal.length() > 0) {
                                finalVal += "; ";
                            }
                            finalVal += noteNew;
                        }

                        notes.clear();
                        if (finalVal.length() > 0) {
                            notes.add(finalVal);
                        }
                        targetInfo.descriptorInfo.put(ConstantParameters.note_kwd, notes);
                    } else {
                        targetInfo.descriptorInfo.put(ConstantParameters.note_kwd, notes);
                    }
                }
            }
        }

    }

    public void ReadThesaursTermStatuses(QClass Q, IntegerObject sis_session,
            String thesaurusName1, String thesaurusName2, HashMap<String, NodeInfoStringContainer> termsInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();

        StringObject THESstatusForInsertion = new StringObject();
        StringObject THESstatusUnderConstruction = new StringObject();
        StringObject THESstatusForReinspection = new StringObject();
        StringObject THESstatusForApproval = new StringObject();
        StringObject THESstatusApproved = new StringObject();
        ArrayList<StringObject> allStatuses = new ArrayList<StringObject>();

        //READING FROM THES1
        //ALSO READ IDS OF STATUS CLASSES IN NEW MERGED THESAURUS --> KEEP THESE IDS IN HashMap merged_thesaurus_status_classIds
        /*UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
         wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);
         */
        dbtr.getThesaurusClass_StatusForInsertion(thesaurusName1, THESstatusForInsertion);
        dbtr.getThesaurusClass_StatusUnderConstruction(thesaurusName1, THESstatusUnderConstruction);
        dbtr.getThesaurusClass_StatusForReinspection(thesaurusName1, THESstatusForReinspection);
        dbtr.getThesaurusClass_StatusForApproval(thesaurusName1, THESstatusForApproval);
        dbtr.getThesaurusClass_StatusApproved(thesaurusName1, THESstatusApproved);

        allStatuses.add(THESstatusForInsertion);
        allStatuses.add(THESstatusUnderConstruction);
        allStatuses.add(THESstatusForApproval);
        allStatuses.add(THESstatusApproved);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING Statuses of terms in thesaurus: " + thesaurusName1 + ".");
        for (int i = 0; i < allStatuses.size(); i++) {

            String targetStatus = "";
            StringObject newStatusValue = allStatuses.get(i);//.getValue().replaceFirst(thesaurusName, mergedThesaurusName);
            if (newStatusValue.getValue().equals(THESstatusForInsertion.getValue())) {
                targetStatus = Parameters.Status_For_Insertion;
            } else if (newStatusValue.getValue().equals(THESstatusUnderConstruction.getValue())) {
                targetStatus = Parameters.Status_Under_Construction;
            } else if (newStatusValue.getValue().equals(THESstatusForReinspection.getValue())) {
                targetStatus = Parameters.Status_For_Reinspection;
            } else if (newStatusValue.getValue().equals(THESstatusForApproval.getValue())) {
                targetStatus = Parameters.Status_For_Approval;
            } else if (newStatusValue.getValue().equals(THESstatusApproved.getValue())) {
                targetStatus = Parameters.Status_Approved;
            }

            Q.reset_name_scope();
            long newIDL = Q.set_current_node(newStatusValue);
            if (newIDL == QClass.APIFail) {
                continue;
            }

            int set_all_such_terms = Q.get_instances(0);
            Q.reset_set(set_all_such_terms);
            //StringObject nodeName = new StringObject();

            ArrayList<String> termsWithThisStatus = new ArrayList<String>();

            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(set_all_such_terms, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    String UIName = dbGen.removePrefix(row.get_v1_cls_logicalname());
                    termsWithThisStatus.add(UIName);
                }
            }
            /*while (Q.retur_nodes(set_all_such_terms, nodeName) != QClass.APIFail) {
             String UIName = dbGen.removePrefix(nodeName.getValue());
             termsWithThisStatus.add(UIName);
             }*/

            Q.free_set(set_all_such_terms);

            for (int k = 0; k < termsWithThisStatus.size(); k++) {
                String targetTerm = termsWithThisStatus.get(k);
                if (termsInfo.containsKey(targetTerm)) {
                    NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
                    if (targetInfo.descriptorInfo.containsKey(ConstantParameters.status_kwd)) {
                        targetInfo.descriptorInfo.get(ConstantParameters.status_kwd).add(targetStatus);
                    }
                }
            }

        }

        Q.free_all_sets();
        Q.reset_name_scope();

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            dbtr.getThesaurusClass_StatusForInsertion(thesaurusName2, THESstatusForInsertion);
            dbtr.getThesaurusClass_StatusUnderConstruction(thesaurusName2, THESstatusUnderConstruction);
            dbtr.getThesaurusClass_StatusForReinspection(thesaurusName2, THESstatusForReinspection);
            dbtr.getThesaurusClass_StatusForApproval(thesaurusName2, THESstatusForApproval);
            dbtr.getThesaurusClass_StatusApproved(thesaurusName2, THESstatusApproved);

            allStatuses.add(THESstatusForInsertion);
            allStatuses.add(THESstatusUnderConstruction);
            allStatuses.add(THESstatusForApproval);
            allStatuses.add(THESstatusApproved);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING Statuses of terms in thesaurus: " + thesaurusName2 + ".");
            for (int i = 0; i < allStatuses.size(); i++) {

                String targetStatus = "";
                StringObject newStatusValue = allStatuses.get(i);//.getValue().replaceFirst(thesaurusName, mergedThesaurusName);
                if (newStatusValue.getValue().equals(THESstatusForInsertion.getValue())) {
                    targetStatus = Parameters.Status_For_Insertion;
                } else if (newStatusValue.getValue().equals(THESstatusUnderConstruction.getValue())) {
                    targetStatus = Parameters.Status_Under_Construction;
                } else if (newStatusValue.getValue().equals(THESstatusForReinspection.getValue())) {
                    targetStatus = Parameters.Status_For_Reinspection;
                } else if (newStatusValue.getValue().equals(THESstatusForApproval.getValue())) {
                    targetStatus = Parameters.Status_For_Approval;
                } else if (newStatusValue.getValue().equals(THESstatusApproved.getValue())) {
                    targetStatus = Parameters.Status_Approved;
                }

                Q.reset_name_scope();
                long newIDL = Q.set_current_node(newStatusValue);
                if (newIDL == QClass.APIFail) {
                    continue;
                }

                int set_all_such_terms = Q.get_instances(0);
                Q.reset_set(set_all_such_terms);
                //StringObject nodeName = new StringObject();

                ArrayList<String> termsWithThisStatus = new ArrayList<String>();

                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if (Q.bulk_return_nodes(set_all_such_terms, retVals) != QClass.APIFail) {
                    for (Return_Nodes_Row row : retVals) {
                        String UIName = dbGen.removePrefix(row.get_v1_cls_logicalname());
                        termsWithThisStatus.add(UIName);
                    }
                }
                /*while (Q.retur_nodes(set_all_such_terms, nodeName) != QClass.APIFail) {
                 String UIName = dbGen.removePrefix(nodeName.getValue());
                 termsWithThisStatus.add(UIName);
                 }*/

                Q.free_set(set_all_such_terms);

                for (int k = 0; k < termsWithThisStatus.size(); k++) {
                    String targetTerm = termsWithThisStatus.get(k);
                    if (termsInfo.containsKey(targetTerm)) {
                        NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
                        if (targetInfo.descriptorInfo.containsKey(ConstantParameters.status_kwd)) {
                            ArrayList<String> existingStatuses = targetInfo.descriptorInfo.get(ConstantParameters.status_kwd);
                            if (existingStatuses.size() == 1) {
                                String existingStatusStr = existingStatuses.get(0);
                                int existingStatusPriority = this.getStatusPriority(existingStatusStr, thesaurusName2);
                                int newstatusPriority = this.getStatusPriority(targetStatus, thesaurusName1);
                                if (existingStatusPriority < newstatusPriority) {
                                    existingStatuses.clear();
                                    existingStatuses.add(targetStatus);
                                    targetInfo.descriptorInfo.put(ConstantParameters.status_kwd, existingStatuses);
                                }
                            } else {
                                targetInfo.descriptorInfo.get(ConstantParameters.status_kwd).add(targetStatus);
                            }
                        }
                    }
                }

            }

            Q.free_all_sets();
            Q.reset_name_scope();

        }
    }

    public void ReadThesaurusSources(UserInfoClass refSessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            HashMap<String, String> XMLsources) {

        DBGeneral dbGen = new DBGeneral();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject SourceObj = new StringObject();
        StringObject SourceNoteObj = new StringObject();
        dbGen.getKeywordPair(refSessionUserInfo.selectedThesaurus, ConstantParameters.source_note_kwd, SourceObj, SourceNoteObj, Q, sis_session);

        Q.reset_name_scope();
        Q.set_current_node(SourceObj);
        int set_sources = Q.get_all_instances(0);
        Q.reset_set(set_sources);
        ArrayList<String> sourceNames = dbGen.get_Node_Names_Of_Set(set_sources, true, Q, sis_session);
        Q.free_set(set_sources);
        Collections.sort(sourceNames);

        Q.reset_name_scope();
        Q.set_current_node(SourceObj);
        Q.set_current_node(SourceNoteObj);

        int set_source_note_links = Q.get_all_instances(0);
        Q.reset_set(set_source_note_links);

        int set_sources_with_source_note = Q.get_from_value(set_source_note_links);
        Q.reset_set(set_sources_with_source_note);

        ArrayList<String> sourcesWithSourceNote = dbGen.get_Node_Names_Of_Set(set_sources_with_source_note, true, Q, sis_session);
        Q.free_set(set_sources_with_source_note);
        Q.free_set(set_source_note_links);

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(refSessionUserInfo.selectedThesaurus) == false) {
            TA.SetThesaurusName(refSessionUserInfo.selectedThesaurus);
        }
        for (int k = 0; k < sourceNames.size(); k++) {
            String source = sourceNames.get(k);
            String source_note = "";
            if (sourcesWithSourceNote.contains(source)) {

                String prefix_source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
                StringObject sourceObj = new StringObject(prefix_source.concat(source));
                StringObject sourceNoteObj = new StringObject("");

                TA.GetDescriptorComment(sourceObj, sourceNoteObj, SourceObj, SourceNoteObj);
                if (sourceNoteObj != null && sourceNoteObj.getValue().length() > 0) {
                    source_note = sourceNoteObj.getValue().trim();
                }
            }
            XMLsources.put(source, source_note);
        }
        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(refSessionUserInfo.selectedThesaurus) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }

    }

    public void ReadThesaurusTerms(UserInfoClass refSessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, String thesaurusName1, String thesaurusName2,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> guideTerms,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations) {

        ArrayList<String> allTerms = new ArrayList<>();

        String[] output = Utilities.getSortedTermAllOutputArray();

        ArrayList<String> outputVec = new ArrayList<>();
        outputVec.addAll(Arrays.asList(output));

        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        //step 1 collect all data around terms except status and scope notes
        /*
         StringObject fromcls = new StringObject();
         StringObject label = new StringObject();
         StringObject categ = new StringObject();
         StringObject cls = new StringObject();
         IntegerObject uniq_categ = new IntegerObject();
         IntegerObject traversed = new IntegerObject();
         CMValue cmv = new CMValue();
         IntegerObject clsID = new IntegerObject();
         IntegerObject linkID = new IntegerObject();
         IntegerObject categID = new IntegerObject();
         */
        StringObject BTLinkObj = new StringObject();
        HashMap<String, String> kewyWordsMappings = new HashMap<String, String>();
        dbGen.applyKeywordMappings(SessionUserInfo.selectedThesaurus, Q, sis_session, output, kewyWordsMappings);
        dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
        int set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
        Q.reset_set(set_terms);

        int set_from_links = Q.get_link_from(set_terms);

        Q.reset_set(set_from_links);

        ArrayList<Return_Full_Link_Id_Row> retFLIVals = new ArrayList<Return_Full_Link_Id_Row>();
        if (Q.bulk_return_full_link_id(set_from_links, retFLIVals) != QClass.APIFail) {

            for (Return_Full_Link_Id_Row row : retFLIVals) {
                //while (Q.retur_full_link_id(set_from_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                String debugcode = row.get_v1_cls();
                if (debugcode == null || debugcode.length() == 0) {
                    continue;
                }
                debugcode = row.get_v5_categ();
                if (debugcode == null || debugcode.length() == 0) {
                    continue;
                }

                debugcode = row.get_v8_cmv().getString();
                if (debugcode == null || debugcode.length() == 0) {
                    continue;
                }

                String targetTerm = dbGen.removePrefix(row.get_v1_cls());
                String targetTransliterationTerm = row.get_v10_clsTransliteration();
                Long targetTermReferenceId = row.get_v11_clsRefid();
                
                String category = row.get_v5_categ();
                String categoryKwd = kewyWordsMappings.get(row.get_v5_categ());
                String value = row.get_v8_cmv().getString();
                long valueRefIdL = row.get_v8_cmv().getRefid();
                String valueTranslit = row.get_v8_cmv().getTransliterationString();
                // Utils.StaticClass.webAppSystemOutPrintln((counter++) +" In " + targetTerm);
                long targetTermIdL = row.get_v2_clsid();
                long valueIdL = row.get_v8_cmv().getSysid();
                // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
                //String valueId = String.valueOf(linkID.getValue());
                //clsID = new IntegerObject();
                //linkID = new IntegerObject();
                //cmv = new CMValue();

                if (categoryKwd == null) {
                    if (category.startsWith(BTLinkObj.getValue())) {
                        categoryKwd = ConstantParameters.bt_kwd;
                    }
                }

                if (categoryKwd != null && categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                    category = category.replaceFirst(BTLinkObj.getValue(), "");
                    if (category.length() > 0 && guideTerms.contains(category) == false) {
                        guideTerms.add(category);
                    }
                }
                SortItem targetTermSortItem = new SortItem(targetTerm, targetTermIdL, category);

                if (termsInfo.containsKey(targetTerm) == false) {
                    NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                    //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId, category));
                    newContainer.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(targetTermReferenceId+"");
                    newContainer.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).add(targetTransliterationTerm);
                    
                    termsInfo.put(targetTerm, newContainer);
                    
                    
                    allTerms.add(targetTerm);
                }

                if (categoryKwd == null || 
                        categoryKwd.compareTo(ConstantParameters.scope_note_kwd) == 0 || 
                        categoryKwd.compareTo(ConstantParameters.translations_scope_note_kwd) == 0 || 
                        categoryKwd.compareTo(ConstantParameters.historical_note_kwd) == 0 || 
                        categoryKwd.compareTo(ConstantParameters.comment_kwd) == 0 || 
                        categoryKwd.compareTo(ConstantParameters.note_kwd) == 0) {
                    continue;
                }

                if (categoryKwd.compareTo(ConstantParameters.modified_on_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.created_on_kwd) == 0) {
                    //no value change needed
                } else {
                    value = dbGen.removePrefix(value);
                }

                SortItem valueSortItem = new SortItem(value, valueIdL, category);

                if (categoryKwd.compareTo(ConstantParameters.translation_kwd) == 0
                        || categoryKwd.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                    String langPrefix = category.substring(category.indexOf(ConstantParameters.toTranslationCategoryPrefix) + ConstantParameters.toTranslationCategoryPrefix.length());

                    valueSortItem = new SortItem(langPrefix + Parameters.TRANSLATION_SEPERATOR + value, valueIdL, langPrefix);
                }

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
                termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.log_name);

                if (categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                    if (termsInfo.containsKey(value) == false) {
                        NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                        
                        newContainer.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(valueRefIdL+"");
                        newContainer.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).add(valueTranslit);
                        //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
                        termsInfo.put(value, newContainer);
                        allTerms.add(value);
                    }
                    termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTerm);

                    //guide terms
                    if (category.length() > 0) {
                        if (XMLguideTermsRelations.containsKey(value) == false) {
                            XMLguideTermsRelations.put(value, new ArrayList<SortItem>());
                        }
                        ArrayList<SortItem> existingRelations = XMLguideTermsRelations.get(value);
                        existingRelations.add(targetTermSortItem);
                        XMLguideTermsRelations.put(value, existingRelations);
                    }

                } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
                    if (termsInfo.containsKey(value) == false) {
                        NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                        newContainer.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(valueRefIdL+"");
                        newContainer.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).add(valueTranslit);
                        //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
                        termsInfo.put(value, newContainer);
                        allTerms.add(value);
                    }

                    termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTerm);

                }
            }
        }

        /*
         while (Q.retur_full_link_id(set_from_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
         //while (Q.retur_full_link(set_from_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

         String debugcode = cls.getValue();
         if (debugcode == null || debugcode.length() == 0) {
         continue;
         }
         debugcode = categ.getValue();
         if (debugcode == null || debugcode.length() == 0) {
         continue;
         }

         debugcode = cmv.getString();
         if (debugcode == null || debugcode.length() == 0) {
         continue;
         }

         String targetTerm = dbGen.removePrefix(cls.getValue());
         String category = categ.getValue();
         String categoryKwd = kewyWordsMappings.get(categ.getValue());
         String value = cmv.getString();
         // Utils.StaticClass.webAppSystemOutPrintln((counter++) +" In " + targetTerm);
         int targetTermId = clsID.getValue();
         long valueIdL = cmv.getSysid();
         // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
         //String valueId = String.valueOf(linkID.getValue());
         clsID = new IntegerObject();
         linkID = new IntegerObject();
         cmv = new CMValue();

         if (categoryKwd == null) {
         if (category.startsWith(BTLinkObj.getValue())) {
         categoryKwd = ConstantParameters.bt_kwd;
         }
         }

         if (categoryKwd != null && categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
         category = category.replaceFirst(BTLinkObj.getValue(), "");
         if (category.length() > 0 && guideTerms.contains(category) == false) {
         guideTerms.add(category);
         }
         }
         SortItem targetTermSortItem = new SortItem(targetTerm, targetTermId, category);


         if (termsInfo.containsKey(targetTerm) == false) {
         NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
         //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId, category));
         termsInfo.put(targetTerm, newContainer);
         allTerms.add(targetTerm);
         }


         if (categoryKwd == null || categoryKwd.compareTo(ConstantParameters.scope_note_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.translations_scope_note_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.historical_note_kwd) == 0) {
         continue;
         }

         if (categoryKwd.compareTo(ConstantParameters.modified_on_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.created_on_kwd) == 0) {
         //no value change needed
         } else {
         value = dbGen.removePrefix(value);
         }

         SortItem valueSortItem = new SortItem(value, valueIdL, category);

         if (categoryKwd.compareTo(ConstantParameters.translation_kwd) == 0
         || categoryKwd.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
         String langPrefix = category.substring(category.indexOf(ConstantParameters.toTranslationCategoryPrefix) + ConstantParameters.toTranslationCategoryPrefix.length());

         valueSortItem = new SortItem(langPrefix + Parameters.TRANSLATION_SEPERATOR + value, valueIdL, langPrefix);
         }

         //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
         termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.log_name);

         if (categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
         if (termsInfo.containsKey(value) == false) {
         NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
         //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
         termsInfo.put(value, newContainer);
         allTerms.add(value);
         }
         termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTerm);

         //guide terms
         if (category.length() > 0) {
         if (XMLguideTermsRelations.containsKey(value) == false) {
         XMLguideTermsRelations.put(value, new ArrayList<SortItem>());
         }
         ArrayList<SortItem> existingRelations = XMLguideTermsRelations.get(value);
         existingRelations.add(targetTermSortItem);
         XMLguideTermsRelations.put(value, existingRelations);
         }



         } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
         if (termsInfo.containsKey(value) == false) {
         NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
         //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
         termsInfo.put(value, newContainer);
         allTerms.add(value);
         }

         termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTerm);

         }
         }
         */
        Q.free_set(set_from_links);
        Q.free_set(set_terms);

        //termNames.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));
        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            kewyWordsMappings.clear();
            dbGen.applyKeywordMappings(SessionUserInfo.selectedThesaurus, Q, sis_session, output, kewyWordsMappings);
            dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);

            index = Parameters.CLASS_SET.indexOf("TERM");
            termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
            set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
            Q.reset_set(set_terms);

            set_from_links = Q.get_link_from(set_terms);

            Q.reset_set(set_from_links);
            retFLIVals.clear();
            if (Q.bulk_return_full_link_id(set_from_links, retFLIVals) != QClass.APIFail) {
                for (Return_Full_Link_Id_Row row : retFLIVals) {
                    //while (Q.retur_full_link_id(set_from_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

                    String debugcode = row.get_v1_cls();
                    if (debugcode == null || debugcode.length() == 0) {
                        continue;
                    }
                    debugcode = row.get_v5_categ();
                    if (debugcode == null || debugcode.length() == 0) {
                        continue;
                    }

                    debugcode = row.get_v8_cmv().getString();
                    if (debugcode == null || debugcode.length() == 0) {
                        continue;
                    }

                    String targetTerm = dbGen.removePrefix(row.get_v1_cls());
                    String category = row.get_v5_categ();
                    String categoryKwd = kewyWordsMappings.get(row.get_v5_categ());
                    String value = row.get_v8_cmv().getString();
                    // Utils.StaticClass.webAppSystemOutPrintln((counter++) +" In " + targetTerm);
                    long targetTermIdL = row.get_v2_clsid();
                    long valueIdL = row.get_v8_cmv().getSysid();
                    // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
                    //String valueId = String.valueOf(linkID.getValue());
                    //clsID = new IntegerObject();
                    //linkID = new IntegerObject();
                    //cmv = new CMValue();

                    if (categoryKwd == null) {
                        if (category.startsWith(BTLinkObj.getValue())) {
                            categoryKwd = ConstantParameters.bt_kwd;
                        }
                    }

                    if (categoryKwd != null && categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                        category = category.replaceFirst(BTLinkObj.getValue(), "");
                        if (category.length() > 0 && guideTerms.contains(category) == false) {
                            guideTerms.add(category);
                        }
                    }
                    SortItem targetTermSortItem = new SortItem(targetTerm, targetTermIdL, category);

                    if (termsInfo.containsKey(targetTerm) == false) {
                        NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                        //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId, category));
                        termsInfo.put(targetTerm, newContainer);
                        allTerms.add(targetTerm);
                    }

                    if (categoryKwd == null || categoryKwd.compareTo(ConstantParameters.scope_note_kwd) == 0 ||
                            categoryKwd.compareTo(ConstantParameters.translations_scope_note_kwd) == 0 ||
                            categoryKwd.compareTo(ConstantParameters.historical_note_kwd) == 0 ||
                            categoryKwd.compareTo(ConstantParameters.comment_kwd)==0||
                            categoryKwd.compareTo(ConstantParameters.note_kwd)==0) {
                        continue;
                    }

                    if (categoryKwd.compareTo(ConstantParameters.modified_on_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.created_on_kwd) == 0) {
                        //no value change needed
                    } else {
                        value = dbGen.removePrefix(value);
                    }

                    SortItem valueSortItem = new SortItem(value, valueIdL, category);

                    if (categoryKwd.compareTo(ConstantParameters.translation_kwd) == 0
                            || categoryKwd.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
                        String langPrefix = category.substring(category.indexOf(ConstantParameters.toTranslationCategoryPrefix) + ConstantParameters.toTranslationCategoryPrefix.length());

                        valueSortItem = new SortItem(langPrefix + Parameters.TRANSLATION_SEPERATOR + value, valueIdL, langPrefix);
                    }

                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
                    termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.log_name);

                    if (categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                            //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }
                        ArrayList<String> nts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd);
                        if (nts.contains(targetTerm) == false) {
                            nts.add(targetTerm);
                            termsInfo.get(value).descriptorInfo.put(ConstantParameters.nt_kwd, nts);
                        }

                        //guide terms
                        if (category.length() > 0) {
                            if (XMLguideTermsRelations.containsKey(value) == false) {
                                XMLguideTermsRelations.put(value, new ArrayList<SortItem>());
                            }
                            ArrayList<SortItem> existingRelations = XMLguideTermsRelations.get(value);
                            existingRelations.add(targetTermSortItem);
                            XMLguideTermsRelations.put(value, existingRelations);
                        }

                    } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                            //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }

                        ArrayList<String> rts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd);
                        if (rts.contains(targetTerm) == false) {
                            rts.add(targetTerm);
                            termsInfo.get(value).descriptorInfo.put(ConstantParameters.rt_kwd, rts);
                        }
                        //termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTerm);

                    }
                }
            }

            /*
             while (Q.retur_full_link_id(set_from_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
             //while (Q.retur_full_link(set_from_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

             String debugcode = cls.getValue();
             if (debugcode == null || debugcode.length() == 0) {
             continue;
             }
             debugcode = categ.getValue();
             if (debugcode == null || debugcode.length() == 0) {
             continue;
             }

             debugcode = cmv.getString();
             if (debugcode == null || debugcode.length() == 0) {
             continue;
             }

             String targetTerm = dbGen.removePrefix(cls.getValue());
             String category = categ.getValue();
             String categoryKwd = kewyWordsMappings.get(categ.getValue());
             String value = cmv.getString();
             // Utils.StaticClass.webAppSystemOutPrintln((counter++) +" In " + targetTerm);
             int targetTermId = clsID.getValue();
             long valueIdL = cmv.getSysid();
             // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
             //String valueId = String.valueOf(linkID.getValue());
             clsID = new IntegerObject();
             linkID = new IntegerObject();
             cmv = new CMValue();

             if (categoryKwd == null) {
             if (category.startsWith(BTLinkObj.getValue())) {
             categoryKwd = ConstantParameters.bt_kwd;
             }
             }

             if (categoryKwd != null && categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
             category = category.replaceFirst(BTLinkObj.getValue(), "");
             if (category.length() > 0 && guideTerms.contains(category) == false) {
             guideTerms.add(category);
             }
             }
             SortItem targetTermSortItem = new SortItem(targetTerm, targetTermId, category);


             if (termsInfo.containsKey(targetTerm) == false) {
             NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
             //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId, category));
             termsInfo.put(targetTerm, newContainer);
             allTerms.add(targetTerm);
             }


             if (categoryKwd == null || categoryKwd.compareTo(ConstantParameters.scope_note_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.translations_scope_note_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.historical_note_kwd) == 0) {
             continue;
             }

             if (categoryKwd.compareTo(ConstantParameters.modified_on_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.created_on_kwd) == 0) {
             //no value change needed
             } else {
             value = dbGen.removePrefix(value);
             }

             SortItem valueSortItem = new SortItem(value, valueIdL, category);

             if (categoryKwd.compareTo(ConstantParameters.translation_kwd) == 0
             || categoryKwd.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
             String langPrefix = category.substring(category.indexOf(ConstantParameters.toTranslationCategoryPrefix) + ConstantParameters.toTranslationCategoryPrefix.length());

             valueSortItem = new SortItem(langPrefix + Parameters.TRANSLATION_SEPERATOR + value, valueIdL, langPrefix);
             }

             //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
             termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.log_name);

             if (categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
             if (termsInfo.containsKey(value) == false) {
             NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
             //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
             termsInfo.put(value, newContainer);
             allTerms.add(value);
             }
             ArrayList<String> nts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd);
             if (nts.contains(targetTerm) == false) {
             nts.add(targetTerm);
             termsInfo.get(value).descriptorInfo.put(ConstantParameters.nt_kwd, nts);
             }


             //guide terms
             if (category.length() > 0) {
             if (XMLguideTermsRelations.containsKey(value) == false) {
             XMLguideTermsRelations.put(value, new ArrayList<SortItem>());
             }
             ArrayList<SortItem> existingRelations = XMLguideTermsRelations.get(value);
             existingRelations.add(targetTermSortItem);
             XMLguideTermsRelations.put(value, existingRelations);
             }



             } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
             if (termsInfo.containsKey(value) == false) {
             NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
             //newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueId, valueId, category));
             termsInfo.put(value, newContainer);
             allTerms.add(value);
             }

             ArrayList<String> rts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd);
             if (rts.contains(targetTerm) == false) {
             rts.add(targetTerm);
             termsInfo.get(value).descriptorInfo.put(ConstantParameters.rt_kwd, rts);
             }
             //termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTerm);

             }
             }
             */
            Q.free_set(set_from_links);
            Q.free_set(set_terms);

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);
        }

        ReadThesaursTermStatuses(Q, sis_session, thesaurusName1, thesaurusName2, termsInfo);

        ReadTheasaurusTermCommentCategories(SessionUserInfo, Q, TA, sis_session, thesaurusName1, thesaurusName2, termsInfo);

        return;
    }

    public HashMap<String, ArrayList<String>> ReadThesaurusHierarchies(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        if (thesaurusName1 == null) { // supporting copy operation
            return new HashMap<String, ArrayList<String>>();
        }
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        Q.reset_name_scope();
        //Find out hierarchy Classes and get their instances
        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);
        int set_hierarchies = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
        Q.reset_set(set_hierarchies);

        ArrayList<String> hierarchyNames = new ArrayList<String>();
        hierarchyNames.addAll(dbGen.get_Node_Names_Of_Set(set_hierarchies, true, Q, sis_session));
        Q.free_set(set_hierarchies);

        //Create a set with all facets used as a filter to the get all classes call taht will follow
        Q.reset_name_scope();
        //Find out facet Classes and get their instances
        index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        int set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_facets);

        HashMap<String, ArrayList<String>> thesaurus_hierarchy_Facets_Relations = new HashMap<String, ArrayList<String>>();

        for (int i = 0; i < hierarchyNames.size(); i++) {
            StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyNames.get(i)));
            ArrayList<String> hierarchyFacets = new ArrayList<String>();
            Q.reset_name_scope();
            if (Q.set_current_node(hierarchyObj) != QClass.APIFail) {
                int set_hierarchy_classes = Q.get_superclasses(0);

                Q.reset_set(set_hierarchy_classes);
                Q.set_intersect(set_hierarchy_classes, set_facets);
                Q.reset_set(set_hierarchy_classes);
                hierarchyFacets.addAll(dbGen.get_Node_Names_Of_Set(set_hierarchy_classes, true, Q, sis_session));
                Q.free_set(set_hierarchy_classes);
            }

            thesaurus_hierarchy_Facets_Relations.put(hierarchyNames.get(i), hierarchyFacets);
        }
        //thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));

        Q.free_set(set_facets);

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            Q.reset_name_scope();
            //Find out hierarchy Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("HIERARCHY");
            HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);
            set_hierarchies = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
            Q.reset_set(set_hierarchies);

            ArrayList<String> hierarchyNamesOfThes2 = new ArrayList<String>();
            hierarchyNamesOfThes2.addAll(dbGen.get_Node_Names_Of_Set(set_hierarchies, true, Q, sis_session));
            Q.free_set(set_hierarchies);

            //Create a set with all facets used as a filter to the get all classes call taht will follow
            Q.reset_name_scope();
            //Find out facet Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("FACET");
            FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
            set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);

            Q.reset_set(set_facets);

            HashMap<String, ArrayList<String>> thesaurus_hierarchy_Facets_RelationsOfThes2 = new HashMap<String, ArrayList<String>>();

            for (int i = 0; i < hierarchyNamesOfThes2.size(); i++) {
                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyNamesOfThes2.get(i)));
                ArrayList<String> hierarchyFacets = new ArrayList<String>();
                Q.reset_name_scope();
                if (Q.set_current_node(hierarchyObj) != QClass.APIFail) {
                    int set_hierarchy_classes = Q.get_superclasses(0);

                    Q.reset_set(set_hierarchy_classes);
                    Q.set_intersect(set_hierarchy_classes, set_facets);
                    Q.reset_set(set_hierarchy_classes);
                    hierarchyFacets.addAll(dbGen.get_Node_Names_Of_Set(set_hierarchy_classes, true, Q, sis_session));

                    Q.free_set(set_hierarchy_classes);
                }

                thesaurus_hierarchy_Facets_RelationsOfThes2.put(hierarchyNamesOfThes2.get(i), hierarchyFacets);
            }
            //thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));

            Q.free_set(set_facets);

            //merge the two thesauri
            Iterator<String> secondThesHiers = thesaurus_hierarchy_Facets_RelationsOfThes2.keySet().iterator();
            while (secondThesHiers.hasNext()) {
                String targetHier = secondThesHiers.next();
                ArrayList<String> targetHierFacets = thesaurus_hierarchy_Facets_RelationsOfThes2.get(targetHier);

                if (thesaurus_hierarchy_Facets_Relations.containsKey(targetHier)) {

                    ArrayList<String> existingFacets = thesaurus_hierarchy_Facets_Relations.get(targetHier);

                    for (int k = 0; k < targetHierFacets.size(); k++) {
                        String checkFacet = targetHierFacets.get(k);
                        if (existingFacets.contains(checkFacet) == false) {
                            existingFacets.add(checkFacet);
                        }
                    }
                    thesaurus_hierarchy_Facets_Relations.put(targetHier, existingFacets);

                } else {
                    thesaurus_hierarchy_Facets_Relations.put(targetHier, targetHierFacets);
                }
            }
        }
        return thesaurus_hierarchy_Facets_Relations;
    }

    public HashMap<String, ArrayList<String>> ReadNextLevelSetTermsAndBts(QClass Q, IntegerObject sis_session, DBGeneral dbGen,
            int set_next_level_terms, StringObject btFromClassbj, StringObject btLinkObj) {

        HashMap<String, ArrayList<String>> nextLevelSet_Terms_and_Bts = new HashMap<String, ArrayList<String>>();

        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        Q.reset_name_scope();

        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_next_level_terms, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String termName = dbGen.removePrefix(row.get_v1_cls());
                String btName = dbGen.removePrefix(row.get_v3_cmv().getString());

                ArrayList<String> otherBts = nextLevelSet_Terms_and_Bts.get(termName);
                if (otherBts == null) {
                    otherBts = new ArrayList<String>();
                    otherBts.add(btName);
                    nextLevelSet_Terms_and_Bts.put(termName, otherBts);
                } else {
                    otherBts.add(btName);
                    nextLevelSet_Terms_and_Bts.put(termName, otherBts);
                }

            }
        }
        /*while (Q.retur_link(set_next_level_terms, cls, label, cmv) != QClass.APIFail) {
         String termName = dbGen.removePrefix(cls.getValue());
         String btName = dbGen.removePrefix(cmv.getString());

         ArrayList<String> otherBts = nextLevelSet_Terms_and_Bts.get(termName);
         if (otherBts == null) {
         otherBts = new ArrayList<String>();
         otherBts.add(btName);
         nextLevelSet_Terms_and_Bts.put(termName, otherBts);
         } else {
         otherBts.add(btName);
         nextLevelSet_Terms_and_Bts.put(termName, otherBts);
         }

         }*/

        return nextLevelSet_Terms_and_Bts;

    }

    public boolean CopyTermsLevelByLevel(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfThes1 = new ArrayList<HashMap<String, ArrayList<String>>>();
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfThes2 = new ArrayList<HashMap<String, ArrayList<String>>>();

        StringObject topTermObj = new StringObject();
        StringObject btFromClassObj = new StringObject();
        StringObject btLinkObj = new StringObject();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), topTermObj);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, btFromClassObj, btLinkObj, Q, sis_session);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tReading Process Started for thesaurus " + thesaurusName1 + ".");
        Q.reset_name_scope();
        if (Q.set_current_node(topTermObj) == QClass.APIFail) {
           
            //logFileWriter.append("<!--Failed to reference at TopTerm Class of Thesaurus: " + thesaurusName1 + ".-->\r\n");
            resultObj.setValue(u.translateFromMessagesXML("root/CopyTermsLevelByLevel/TopTermReferenceFailed", new String[]{thesaurusName1}));
            //resultObj.setValue("Failed to reference at TopTerm Class of Thesaurus: " + thesaurusName1 + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            
            return false;
        }

        int set_top_terms = Q.get_instances(0);
        Q.reset_set(set_top_terms);

        int set_next_level_links = Q.get_link_to_by_category(set_top_terms, btFromClassObj, btLinkObj);

        Q.reset_set(set_next_level_links);
        Q.free_set(set_top_terms);

        int counter = 1;
        int levelIndex = 0;

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + thesaurusName1 + ".");

        allLevelsOfThes1.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_links, btFromClassObj, btLinkObj));

        while (allLevelsOfThes1.get(levelIndex).size() > 0) {
            counter++;
            levelIndex++;

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + thesaurusName1 + ".");
            int set_next_level_loop = Q.get_from_value(set_next_level_links);
            Q.reset_set(set_next_level_loop);

            int set_next_level_loop_links = Q.get_link_to_by_category(set_next_level_loop, btFromClassObj, btLinkObj);
            Q.reset_set(set_next_level_loop_links);
            Q.free_set(set_next_level_loop);

            allLevelsOfThes1.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_loop_links, btFromClassObj, btLinkObj));

            Q.free_set(set_next_level_links);
            set_next_level_links = set_next_level_loop_links;

        }

        Q.free_set(set_next_level_links);
        Q.free_all_sets();

        if (thesaurusName2 != null) {
            //update all fields in order to keep it in consistent state
            SessionUserInfo = new UserInfoClass(refSessionUserInfo);
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), topTermObj);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, btFromClassObj, btLinkObj, Q, sis_session);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tReading Process Started for thesaurus " + thesaurusName2 + ".");
            Q.reset_name_scope();
            if (Q.set_current_node(topTermObj) == QClass.APIFail) {
                //logFileWriter.append("<!--Failed to reference at TopTerm Class of Thesaurus: " + thesaurusName1 + ".-->\r\n");
                resultObj.setValue(u.translateFromMessagesXML("root/CopyTermsLevelByLevel/TopTermReferenceFailed", new String[]{thesaurusName2}));
                //resultObj.setValue("Failed to reference at TopTerm Class of Thesaurus: " + thesaurusName2 + ".");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                return false;
            }

            set_top_terms = Q.get_instances(0);
            Q.reset_set(set_top_terms);

            set_next_level_links = Q.get_link_to_by_category(set_top_terms, btFromClassObj, btLinkObj);

            Q.reset_set(set_next_level_links);
            Q.free_set(set_top_terms);

            counter = 1;
            levelIndex = 0;

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + thesaurusName2 + ".");

            allLevelsOfThes2.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_links, btFromClassObj, btLinkObj));

            while (allLevelsOfThes2.get(levelIndex).size() > 0) {
                counter++;
                levelIndex++;

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + thesaurusName2 + ".");
                int set_next_level_loop = Q.get_from_value(set_next_level_links);
                Q.reset_set(set_next_level_loop);

                int set_next_level_loop_links = Q.get_link_to_by_category(set_next_level_loop, btFromClassObj, btLinkObj);
                Q.reset_set(set_next_level_loop_links);
                Q.free_set(set_next_level_loop);

                allLevelsOfThes2.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_loop_links, btFromClassObj, btLinkObj));

                Q.free_set(set_next_level_links);
                set_next_level_links = set_next_level_loop_links;

            }

            Q.free_set(set_next_level_links);
            Q.free_all_sets();
        }

        return CreateTermsLevelByLevel(SessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML,
                thesaurusName1, thesaurusName2, mergedThesaurusName, logFileWriter, resultObj,
                allLevelsOfThes1, allLevelsOfThes2, true, ConsistencyCheckPolicy);
    }

    public boolean CopyTermsLevelByLevel(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML,
            String sourceThesaurus,
            OutputStreamWriter logFileWriter, StringObject resultObj,
            ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfThes) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject topTermObj = new StringObject();
        StringObject btFromClassObj = new StringObject();
        StringObject btLinkObj = new StringObject();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, sourceThesaurus);

        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), topTermObj);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, btFromClassObj, btLinkObj, Q, sis_session);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tReading Process Started for thesaurus " + sourceThesaurus + ".");
        Q.reset_name_scope();
        if (Q.set_current_node(topTermObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyTermsLevelByLevel/TopTermReferenceFailed", new String[] {sourceThesaurus}));
            //resultObj.setValue("Failed to reference at TopTerm Class of Thesaurus: " + sourceThesaurus + ".");
            //logFileWriter.append("<!--Failed to reference at TopTerm Class of Thesaurus: " + thesaurusName1 + ".-->\r\n");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            
            return false;
        }

        int set_top_terms = Q.get_instances(0);
        Q.reset_set(set_top_terms);

        int set_next_level_links = Q.get_link_to_by_category(set_top_terms, btFromClassObj, btLinkObj);

        Q.reset_set(set_next_level_links);
        Q.free_set(set_top_terms);

        int counter = 1;
        int levelIndex = 0;

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + sourceThesaurus + ".");

        allLevelsOfThes.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_links, btFromClassObj, btLinkObj));

        while (allLevelsOfThes.get(levelIndex).size() > 0) {
            counter++;
            levelIndex++;

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING LEVEL: " + counter + " of thesaurus " + sourceThesaurus + ".");
            int set_next_level_loop = Q.get_from_value(set_next_level_links);
            Q.reset_set(set_next_level_loop);

            int set_next_level_loop_links = Q.get_link_to_by_category(set_next_level_loop, btFromClassObj, btLinkObj);
            Q.reset_set(set_next_level_loop_links);
            Q.free_set(set_next_level_loop);

            allLevelsOfThes.add(ReadNextLevelSetTermsAndBts(Q, sis_session, dbGen, set_next_level_loop_links, btFromClassObj, btLinkObj));

            Q.free_set(set_next_level_links);
            set_next_level_links = set_next_level_loop_links;

        }

        Q.free_set(set_next_level_links);
        Q.free_all_sets();

        return true;

    }

    public boolean CreateTermsLevelByLevel(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, 
            ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfThes1, ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfThes2, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfThes1SortItems = null;
        ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfThes2SortItems = null;
        if(allLevelsOfThes1!=null){
            allLevelsOfThes1SortItems = new ArrayList<HashMap<SortItem, ArrayList<SortItem>>> (); 
            
            
            for(HashMap<String, ArrayList<String>> item : allLevelsOfThes1){
                
                HashMap<SortItem, ArrayList<SortItem>> levelSortItems = new HashMap<SortItem,ArrayList<SortItem>>();
                
                Iterator<String> itemKeysEnum = item.keySet().iterator();
                while(itemKeysEnum.hasNext()){
                    String target = itemKeysEnum.next();
                    ArrayList<String> targetTermBts = item.get(target);
                    ArrayList<SortItem> targetTermBtSortItems = new ArrayList<SortItem>();
                    for(String str : targetTermBts){
                        targetTermBtSortItems.add(new SortItem(str,-1,Utilities.getTransliterationString(str, false),-1));
                    }
                    
                    levelSortItems.put(new SortItem(target,-1,Utilities.getTransliterationString(target, false),-1),targetTermBtSortItems );
                    
                }
                
               allLevelsOfThes1SortItems.add(levelSortItems);
            }
        }
        if(allLevelsOfThes2!=null){
            allLevelsOfThes2SortItems = new ArrayList<HashMap<SortItem, ArrayList<SortItem>>> (); 
            
            
            for(HashMap<String, ArrayList<String>> item : allLevelsOfThes2){
                
                HashMap<SortItem, ArrayList<SortItem>> levelSortItems = new HashMap<SortItem,ArrayList<SortItem>>();
                
                Iterator<String> itemKeysEnum = item.keySet().iterator();
                while(itemKeysEnum.hasNext()){
                    String target = itemKeysEnum.next();
                    ArrayList<String> targetTermBts = item.get(target);
                    ArrayList<SortItem> targetTermBtSortItems = new ArrayList<SortItem>();
                    for(String str : targetTermBts){
                        targetTermBtSortItems.add(new SortItem(str,-1,Utilities.getTransliterationString(str, false),-1));
                    }
                    
                    levelSortItems.put(new SortItem(target,-1,Utilities.getTransliterationString(target, false),-1),targetTermBtSortItems );
                    
                }
                
               allLevelsOfThes2SortItems.add(levelSortItems);
            }
        }
        
        return CreateTermsLevelByLevelInSortItems(refSessionUserInfo,common_utils,Q,TA,sis_session,tms_session,pathToErrorsXML,thesaurusName1,thesaurusName2,mergedThesaurusName,logFileWriter,resultObj,allLevelsOfThes1SortItems,allLevelsOfThes2SortItems,resolveError,ConsistencyCheckPolicy);
    }

    public boolean CreateTermsLevelByLevelInSortItems(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
                                                      QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML,
                                                      String thesaurusName1, String thesaurusName2, String mergedThesaurusName, 
                                                      OutputStreamWriter logFileWriter, StringObject resultObj, 
                                                      ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfThes1, 
                                                      ArrayList<HashMap<SortItem, ArrayList<SortItem>>> allLevelsOfThes2, 
                                                      boolean resolveError, 
                                                      int ConsistencyCheckPolicy) 
            throws IOException {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        
        //SortItem orphanTopTerm = getDefaultUnclassifiedTopTermSortItem(refSessionUserInfo,Q,sis_session,mergedThesaurusName);
        //orphanTopTerm.setLogName(dbGen.removePrefix(orphanTopTerm.getLogName()));
        

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
        UsersClass wtmsUsers = new UsersClass();

        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tCREATION Process Started for thesaurus " + mergedThesaurusName);
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);
        String prefix_term = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        int homanyTerms = 0;
        int howmanyLevels = allLevelsOfThes1.size();
        for (int i = 0; i < howmanyLevels; i++) {
            homanyTerms += allLevelsOfThes1.get(i).size();
        }

        if (allLevelsOfThes2 != null) {
            for (int i = 0; i < allLevelsOfThes2.size(); i++) {
                homanyTerms += allLevelsOfThes2.get(i).size();
            }
        }

        if (allLevelsOfThes2 != null && allLevelsOfThes2.size() > howmanyLevels) {
            howmanyLevels = allLevelsOfThes2.size();
        }
        int termsPerLevel = 0;

        for (int i = 0; i < howmanyLevels; i++) {

            int howmanyInthislevel = 0;
            if (allLevelsOfThes1.size() > i) {
                howmanyInthislevel += allLevelsOfThes1.get(i).keySet().size();
            }
            if (allLevelsOfThes2 != null && allLevelsOfThes2.size() > i) {
                howmanyInthislevel += allLevelsOfThes2.get(i).keySet().size();
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING LEVEL: " + (i + 1) + " of thesaurus " + mergedThesaurusName + " This Level Contains " + howmanyInthislevel + " terms");

            logFileWriter.flush();

            if (allLevelsOfThes1.size() > i) {

                Iterator<SortItem> pairsEnumMerged = allLevelsOfThes1.get(i).keySet().iterator();
                while (pairsEnumMerged.hasNext()) {

                    if (termsPerLevel % restartInterval == 0) {
                        if (common_utils != null) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TERM counter: " + termsPerLevel + " of " + homanyTerms + "  ");
                            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                        }
                    }
                    termsPerLevel++;

                    SortItem termSortItem = pairsEnumMerged.next();

                    Q.free_all_sets();

                    //createNewTerm(sessionInstance,newName,decodedValues,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToErrorsXML);
                    //NOW COPY TO NEW THESAURUS
                    boolean exists = false;
                    boolean lengthOK = true;

                    /*
                     try {
                     byte[] byteArray = term.getBytes("UTF-8");

                     int maxTermChars = dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session);
                     if (byteArray.length > maxTermChars) {
                     lengthOK = false;
                     ArrayList<String> errorArgs = new ArrayList<String>();
                     if (resolveError) {
                     StringObject warningMsg = new StringObject();

                     errorArgs.clear();
                     errorArgs.add(term);
                     errorArgs.add("" + maxTermChars);
                     errorArgs.add("" + byteArray.length);
                     dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                     Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                     try {
                     logFileWriter.append("\r\n<targetTerm>");
                     logFileWriter.append("<name>" + Utilities.escapeXML(term) + "</name>");
                     logFileWriter.append("<errorType>" + "name" + "</errorType>");
                     logFileWriter.append("<errorValue>" + Utilities.escapeXML(term) + "</errorValue>");
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

                     }
                     }
                     } catch (UnsupportedEncodingException ex) {
                     Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                     Utils.StaticClass.handleException(ex);
                     }
                     */
                    if (lengthOK) {
                        StringObject termobj = new StringObject(prefix_term.concat(termSortItem.getLogName()));
                        Q.reset_name_scope();

                        if (Q.set_current_node(termobj) != QClass.APIFail) {
                            exists = true;
                        }
                    }
                    
                    

                    Q.reset_name_scope();
                    ArrayList<String> allBts = new ArrayList<String>();
                    ArrayList<String> additionalBTs = new ArrayList<String>();
                    ArrayList<SortItem> additionalBTsSortItems = allLevelsOfThes1.get(i).get(termSortItem);
                    
                    if(additionalBTsSortItems!=null){
                        for(SortItem btitem : additionalBTsSortItems){
                            additionalBTs.add(btitem.getLogName());
                        }
                    }
                    

                    if (exists) {

                        allBts.addAll(dbGen.returnResults(SessionUserInfo, termSortItem.getLogName(), ConstantParameters.bt_kwd, Q, TA, sis_session));
                        

                        
                        if (allBts.isEmpty()) {//ALREADY EXISTS BUT WITHOUT BTS --> IT IS ALREADY DEFINED AS TOP TERM

                            logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termSortItem.getLogName()) + "</name>");
                            logFileWriter.append("<errorType>name</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(termSortItem.getLogName()) + "1</errorValue>");
                            logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateTermsLevelByLevel/TermFoundAsTopTerm", new String[] {Utilities.escapeXML(termSortItem.getLogName()),thesaurusName2,Utilities.escapeXML(termSortItem.getLogName())}) + "1.</reason>");
                            //logFileWriter.append("<reason>Term " + Utilities.escapeXML(term) + " found as a TT in Thesaurus '" + thesaurusName2 + "'. For the successful insertion renamed to " + Utilities.escapeXML(term) + "1.</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                            termSortItem.setLogName(termSortItem.getLogName()+ "1"); 
                            StringObject newtermobj = new StringObject(prefix_term.concat(termSortItem.getLogName()));
                            Q.reset_name_scope();
                            if (Q.set_current_node(newtermobj) != QClass.APIFail) {
                                exists = true;
                            } else {
                                exists = false;
                            }

                        }

                        for (String checkBT : additionalBTs) {
                            if (allBts.contains(checkBT) == false) {
                                allBts.add(checkBT);
                            }
                        }

                        if (allBts.size() > 1 && allBts.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            allBts.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                    }
                    if (exists) {
                        creation_modificationOfTerm.commitTermTransactionInSortItem(SessionUserInfo, termSortItem, ConstantParameters.bt_kwd, allBts, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                        //logFileWriter.append("Skipping Term : '" + term+"' under BTs : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        //continue;
                    } else {
                        if (additionalBTs.size() > 1 && additionalBTs.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            additionalBTs.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                        
                        
                        creation_modificationOfTerm.createNewTermSortItem(SessionUserInfo, termSortItem, additionalBTs, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                    }
                    //logFileWriter.append(termsPerLevel+". " +term + " ----- " + nextLevelSet_Terms_and_Bts.get(term).toString()+"\r\n");

                    if (resultObj.getValue().length() > 0) {
                        

                        resultObj.setValue(u.translateFromMessagesXML("root/CreateTermsLevelByLevel/CopyTermFailure", new String[] {thesaurusName1,Utilities.escapeXML(termSortItem.getLogName())}) + resultObj.getValue());
                        //resultObj.setValue("Term copy failure from Thesaurus: " + thesaurusName1 + ". " + resultObj.getValue());
                        //Q.free_set(set_next_level_links);
                        // Q.free_set(set_top_terms);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ERROR: " + (i + 1) + "." + termsPerLevel + ". Term: '" + termSortItem.getLogName() + " raised the following error message: " + resultObj.getValue());

                        return false;
                    } else {
                        if (exists) {
                            //  logFileWriter.append(i+1 +"." +termsPerLevel +". Term: '" + term+"' was successfully updated - modified. The following BTs were added: "+  allLevels.get(i).get(term).toString()+"\r\n");
                        } else {
                            //logFileWriter.append(i+1 +"." +termsPerLevel +".Term : '" + term+"' was successfully added under the following BTs: "+  allLevels.get(i).get(term).toString()+"\r\n");
                        }
                    }
                    //logFileWriter.flush();                    
                    resultObj.setValue("");

                }
            }

            if (allLevelsOfThes2 != null && allLevelsOfThes2.size() > i) {

                Iterator<SortItem> pairsEnumMerged = allLevelsOfThes2.get(i).keySet().iterator();
                while (pairsEnumMerged.hasNext()) {

                    if (termsPerLevel % restartInterval == 0) {
                        if (common_utils != null) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TERM counter: " + termsPerLevel + " of " + homanyTerms + "  ");
                            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                        }
                    }
                    termsPerLevel++;
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" i = " + i + " termsPerLevel = " + termsPerLevel);
                    SortItem termSortItem = pairsEnumMerged.next();

                    Q.free_all_sets();

                    //createNewTerm(sessionInstance,newName,decodedValues,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToErrorsXML);
                    //NOW COPY TO NEW THESAURUS
                    StringObject termobj = new StringObject(prefix_term.concat(termSortItem.getLogName()));
                    Q.reset_name_scope();
                    boolean exists = false;
                    if (Q.set_current_node(termobj) != QClass.APIFail) {
                        exists = true;
                    }
                    Q.reset_name_scope();
                    ArrayList<String> allBts = new ArrayList<String>();
                    ArrayList<String> additionalBTs = new ArrayList<String>();
                    ArrayList<SortItem> additionalBTSortItems = allLevelsOfThes2.get(i).get(termSortItem);
                    
                    if(additionalBTSortItems!=null){
                        for(SortItem btSortItem : additionalBTSortItems){
                            additionalBTs.add(btSortItem.getLogName());
                        }
                    }

                    //StringObject resultMessageObj_2 = new StringObject();
                    //ArrayList<String> errorArgs = new ArrayList<String>();

                    if (exists) {

                        allBts.addAll(dbGen.returnResults(SessionUserInfo, termSortItem.getLogName(), ConstantParameters.bt_kwd, Q, TA, sis_session));

                        if (allBts.size() == 0) {//ALREADY EXISTS BUT WITHOUT BTS --> IT IS ALREADY DEFINED AS TOP TERM

                            logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termSortItem.getLogName()) + "</name>");
                            logFileWriter.append("<errorType>name</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(termSortItem.getLogName()) + "2</errorValue>");
                            logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateTermsLevelByLevel/TermFoundAsTopTerm", new String[]{Utilities.escapeXML(termSortItem.getLogName()),thesaurusName1,Utilities.escapeXML(termSortItem.getLogName())}) + "2.</reason>");
                            //logFileWriter.append("<reason>Term ' " + Utilities.escapeXML(term) + "' found as a TT in Thesaurus '" + thesaurusName1 + "'. For the successful insertion renamed to " + Utilities.escapeXML(term) + "2.</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                            termSortItem.setLogName((termSortItem.getLogName() + "2"));
                            StringObject newtermobj = new StringObject(prefix_term.concat(termSortItem.getLogName()));
                            Q.reset_name_scope();
                            if (Q.set_current_node(newtermobj) != QClass.APIFail) {
                                exists = true;
                            } else {
                                exists = false;
                            }
                        }

                        for (int k = 0; k < additionalBTs.size(); k++) {
                            if (allBts.contains(additionalBTs.get(k)) == false) {
                                allBts.add(additionalBTs.get(k));
                            }
                        }

                        if (allBts.size() > 1 && allBts.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            allBts.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                    }

                    if (exists) {
                        creation_modificationOfTerm.commitTermTransactionInSortItem(SessionUserInfo, termSortItem, ConstantParameters.bt_kwd, allBts, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                        //logFileWriter.append("Skipping Term : '" + term+"' under BTs : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        //continue;
                    } else {
                        if (additionalBTs.size() > 1 && additionalBTs.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            additionalBTs.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                        creation_modificationOfTerm.createNewTermSortItem(SessionUserInfo, termSortItem, additionalBTs, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                    }
                    //logFileWriter.append(termsPerLevel+". " +term + " ----- " + nextLevelSet_Terms_and_Bts.get(term).toString()+"\r\n");

                    if (resultObj.getValue().length() > 0) {
                        
                        resultObj.setValue(u.translateFromMessagesXML("root/CreateTermsLevelByLevel/CopyTermFailure",new String[]{thesaurusName2,Utilities.escapeXML(termSortItem.getLogName())}) + resultObj.getValue());
                        //resultObj.setValue("Term copy failure from Thesaurus " + thesaurusName2 + ". " + resultObj.getValue());
                        //Q.free_set(set_next_level_links);
                        // Q.free_set(set_top_terms);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ERROR: " + (i + 1) + "." + termsPerLevel + ". Term: '" + termSortItem.getLogName() + " raised the following error message: " + resultObj.getValue());

                        return false;
                    } else {
                        if (exists) {
                            //  logFileWriter.append(i+1 +"." +termsPerLevel +". Term: '" + term+"' was successfully updated - modifgied. The following BTs were added: "+  allLevels.get(i).get(term).toString()+"\r\n");
                        } else {
                            //logFileWriter.append(i+1 +"." +termsPerLevel +".Term: '" + term+"' was successfully added in the database under BTs: "+  allLevels.get(i).get(term).toString()+"\r\n");
                        }
                    }

                    resultObj.setValue("");

                }
            }

        }

        logFileWriter.flush();
        return true;
    }

    
    public boolean CopyRTs(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, int ConsistencyCheckPolicy/*, StringBuffer warnignsBuffer*/) throws IOException {
        //reading both directions

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        HashMap<String, ArrayList<String>> allTermRts = new HashMap<String, ArrayList<String>>();
        ArrayList<String> rtsToThemSelves = new ArrayList<String>();

        StringObject rtFromClassObj = new StringObject();
        StringObject rtLinkObj = new StringObject();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.rt_kwd, rtFromClassObj, rtLinkObj, Q, sis_session);

        Q.reset_name_scope();
        if (Q.set_current_node(rtFromClassObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyRTs/CategoryReferenceFailed_2_Param", new String[]{rtFromClassObj.getValue(),thesaurusName1}));
            //resultObj.setValue("Failed to refer to Class: " + rtFromClassObj.getValue() + " of thesaurus : " + thesaurusName1 + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            return false;
        }

        if (Q.set_current_node(rtLinkObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyRTs/CategoryReferenceFailed", new String[]{rtFromClassObj.getValue(),rtLinkObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Category: " + rtFromClassObj.getValue() + "->" + rtLinkObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            return false;
        }

        int set_rt_links = Q.get_instances(0);
        Q.reset_set(set_rt_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING RTS from " + thesaurusName1 + "");

        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        Q.reset_name_scope();

        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_rt_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String term1Name = dbGen.removePrefix(row.get_v1_cls());
                String term2Name = dbGen.removePrefix(row.get_v3_cmv().getString());

                if (term1Name.compareTo(term2Name) == 0) {

                    rtsToThemSelves.add(term1Name);
                    logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
                    logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CopyRTs/TermLinkToRT", new String[]{Utilities.escapeXML(term1Name), thesaurusName1}) + "</reason>");
                    //logFileWriter.append("<reason>" + Utilities.escapeXML(term1Name) + " found to have Link RT with itself at Thesaurus: ' " + thesaurusName1 + "' . This Relationship will be bypassed.</reason>");
                    logFileWriter.append("</targetTerm>/r/n");
                    continue; //ignore from reading
                }

                ArrayList<String> otherRts1 = allTermRts.get(term1Name);
                ArrayList<String> otherRts2 = allTermRts.get(term2Name);

                if (otherRts1 == null) {

                    //check if this relation has already been declared in second term RTs
                    if (otherRts2 == null) {
                        otherRts1 = new ArrayList<String>();
                        otherRts1.add(term2Name);
                        allTermRts.put(term1Name, otherRts1);
                    } else {
                        if (otherRts2.contains(term1Name) == false) {
                            otherRts1 = new ArrayList<String>();
                            otherRts1.add(term2Name);
                            allTermRts.put(term1Name, otherRts1);
                        }
                    }

                } else {
                    if (otherRts1.contains(term2Name) == false) {
                        if (otherRts2 == null) {
                            otherRts1.add(term2Name);
                            allTermRts.put(term1Name, otherRts1);
                        } else {
                            if (otherRts2.contains(term1Name) == false) {
                                otherRts1.add(term2Name);
                                allTermRts.put(term1Name, otherRts1);
                            }

                        }
                    }

                }
            }
        }
        /*
         while (Q.retur_link(set_rt_links, cls, label, cmv) != QClass.APIFail) {
         String term1Name = dbGen.removePrefix(cls.getValue());
         String term2Name = dbGen.removePrefix(cmv.getString());

         if (term1Name.compareTo(term2Name) == 0) {
         rtsToThemSelves.add(term1Name);
         logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
         logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
         logFileWriter.append("<reason>Term: " + Utilities.escapeXML(term1Name) + " was found to have RT relationship with himself in thesaurus: " + thesaurusName1 + ". This relation will be ignored.</reason>");
         logFileWriter.append("</targetTerm>/r/n");
         continue; //ignore from reading
         }

         ArrayList<String> otherRts1 = allTermRts.get(term1Name);
         ArrayList<String> otherRts2 = allTermRts.get(term2Name);

         if (otherRts1 == null) {

         //check if this relation has already been declared in second term RTs
         if (otherRts2 == null) {
         otherRts1 = new ArrayList<String>();
         otherRts1.add(term2Name);
         allTermRts.put(term1Name, otherRts1);
         } else {
         if (otherRts2.contains(term1Name) == false) {
         otherRts1 = new ArrayList<String>();
         otherRts1.add(term2Name);
         allTermRts.put(term1Name, otherRts1);
         }
         }

         } else {
         if (otherRts1.contains(term2Name) == false) {
         if (otherRts2 == null) {
         otherRts1.add(term2Name);
         allTermRts.put(term1Name, otherRts1);
         } else {
         if (otherRts2.contains(term1Name) == false) {
         otherRts1.add(term2Name);
         allTermRts.put(term1Name, otherRts1);
         }

         }
         }

         }

         }
         */
        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.rt_kwd, rtFromClassObj, rtLinkObj, Q, sis_session);

            Q.reset_name_scope();
            if (Q.set_current_node(rtFromClassObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyRTs/CategoryReferenceFailed_2_Param", new String[]{rtFromClassObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Class: " + rtFromClassObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                return false;
            }

            if (Q.set_current_node(rtLinkObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyRTs/CategoryReferenceFailed", new String[]{rtFromClassObj.getValue(),rtLinkObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Category: " + rtFromClassObj.getValue() + "->" + rtLinkObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                return false;
            }

            set_rt_links = Q.get_instances(0);
            Q.reset_set(set_rt_links);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING RTS from " + thesaurusName2);

            //cls = new StringObject();
            //label = new StringObject();
            //cmv = new CMValue();
            Q.reset_name_scope();

            retVals.clear();
            if (Q.bulk_return_link(set_rt_links, retVals) != QClass.APIFail) {
                for (Return_Link_Row row : retVals) {

                    String term1Name = dbGen.removePrefix(row.get_v1_cls());
                    String term2Name = dbGen.removePrefix(row.get_v3_cmv().getString());


                    if (term1Name.compareTo(term2Name) == 0) {

                        rtsToThemSelves.add(term2Name);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
                        logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CopyRTs/TermLinkToRT", new String[]{Utilities.escapeXML(term1Name), thesaurusName2}) + "</reason>");
                        //logFileWriter.append("<reason>Term: " + Utilities.escapeXML(term1Name) + " was found to have RT relationship with himself in thesaurus: " + thesaurusName2 + ". This relation will be ignored.</reason>");
                        logFileWriter.append("</targetTerm>/r/n");
                        continue; //ignore from reading
                    }

                    ArrayList<String> otherRts1 = allTermRts.get(term1Name);
                    ArrayList<String> otherRts2 = allTermRts.get(term2Name);

                    if (otherRts1 == null) {

                        //check if this relation has already been declared in second term RTs
                        if (otherRts2 == null) {
                            otherRts1 = new ArrayList<String>();
                            otherRts1.add(term2Name);
                            allTermRts.put(term1Name, otherRts1);
                        } else {
                            if (otherRts2.contains(term1Name) == false) {
                                otherRts1 = new ArrayList<String>();
                                otherRts1.add(term2Name);
                                allTermRts.put(term1Name, otherRts1);
                            }
                        }

                    } else {
                        if (otherRts1.contains(term2Name) == false) {
                            if (otherRts2 == null) {
                                otherRts1.add(term2Name);
                                allTermRts.put(term1Name, otherRts1);
                            } else {
                                if (otherRts2.contains(term1Name) == false) {
                                    otherRts1.add(term2Name);
                                    allTermRts.put(term1Name, otherRts1);
                                }

                            }
                        }

                    }
                }
            }
            /*while (Q.retur_link(set_rt_links, cls, label, cmv) != QClass.APIFail) {
             String term1Name = dbGen.removePrefix(cls.getValue());
             String term2Name = dbGen.removePrefix(cmv.getString());

             if (term1Name.compareTo(term2Name) == 0) {
             rtsToThemSelves.add(term2Name);
             logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
             logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
             logFileWriter.append("<reason>Term: " + Utilities.escapeXML(term1Name) + " was found to have RT relationship with himself in thesaurus: " + thesaurusName2 + ". This relation will be ignored.</reason>");
             logFileWriter.append("</targetTerm>/r/n");
             continue; //ignore from reading
             }

             ArrayList<String> otherRts1 = allTermRts.get(term1Name);
             ArrayList<String> otherRts2 = allTermRts.get(term2Name);

             if (otherRts1 == null) {

             //check if this relation has already been declared in second term RTs
             if (otherRts2 == null) {
             otherRts1 = new ArrayList<String>();
             otherRts1.add(term2Name);
             allTermRts.put(term1Name, otherRts1);
             } else {
             if (otherRts2.contains(term1Name) == false) {
             otherRts1 = new ArrayList<String>();
             otherRts1.add(term2Name);
             allTermRts.put(term1Name, otherRts1);
             }
             }

             } else {
             if (otherRts1.contains(term2Name) == false) {
             if (otherRts2 == null) {
             otherRts1.add(term2Name);
             allTermRts.put(term1Name, otherRts1);
             } else {
             if (otherRts2.contains(term1Name) == false) {
             otherRts1.add(term2Name);
             allTermRts.put(term1Name, otherRts1);
             }

             }
             }

             }

             }
             */
        }
        Q.free_all_sets();

        return CreateRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML,
                mergedThesaurusName, logFileWriter, resultObj, allTermRts, true, ConsistencyCheckPolicy);
    }

    public boolean CreateRTs(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML,
            String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, HashMap<String, ArrayList<String>> allTermRts, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBGeneral dbGen = new DBGeneral();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        int rtRelations = 1;
        

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING RTS in " + mergedThesaurusName);

        rtRelations = 0;
        int howmany = allTermRts.size();
        Iterator<String> pairsEnumMerged = allTermRts.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {

            Q.free_all_sets();

            if (rtRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RT counter: " + rtRelations + " of " + howmany + "    ");
                    //Utils.StaticClass.webAppSystemOutPrintln("RESTARTING SERVER");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            rtRelations++;

            String term = pairsEnumMerged.next();

            ArrayList<String> allRTs = new ArrayList<String>();
            allRTs.addAll(allTermRts.get(term));

            if (allRTs.size() == 0) {
                continue;
            }

            creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.rt_kwd, allRTs, term, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);

            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + rtRelations + ". Failed to add the following RTs: " + allRTs.toString() + " in term: '" + term + "'." + resultObj.getValue());

                return false;
            } else {
                // logFileWriter.append(rtRelations +". Term: '" + term+"' was successfully updated - modified. The following RTs were added: "+  allRTs.toString()+"\r\n");
            }
            //logFileWriter.flush();
            //rtRelations++;
            resultObj.setValue("");

        }

        return true;
    }

    public boolean CopyStatuses(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName,
            OutputStreamWriter logFileWriter, StringObject resultObj) throws IOException {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        StringObject THESstatusForInsertion = new StringObject();
        StringObject THESstatusUnderConstruction = new StringObject();
        StringObject THESstatusForApproval = new StringObject();
        StringObject THESstatusApproved = new StringObject();
        ArrayList<StringObject> allStatuses = new ArrayList<StringObject>();

        HashMap<String, String> thesaurus1_statuses = new HashMap<String, String>();
        HashMap<String, String> thesaurus2_statuses = new HashMap<String, String>();
        HashMap<String, Long> merged_thesaurus_status_classIds = new HashMap<String, Long>();

        //READING FROM THES1 
        //ALSO READ IDS OF STATUS CLASSES IN NEW MERGED THESAURUS --> KEEP THESE IDS IN HashMap merged_thesaurus_status_classIds
        /*UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
         wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);
         */
        dbtr.getThesaurusClass_StatusForInsertion(thesaurusName1, THESstatusForInsertion);
        dbtr.getThesaurusClass_StatusUnderConstruction(thesaurusName1, THESstatusUnderConstruction);
        dbtr.getThesaurusClass_StatusForApproval(thesaurusName1, THESstatusForApproval);
        dbtr.getThesaurusClass_StatusApproved(thesaurusName1, THESstatusApproved);

        allStatuses.add(THESstatusForInsertion);
        allStatuses.add(THESstatusUnderConstruction);
        allStatuses.add(THESstatusForApproval);
        allStatuses.add(THESstatusApproved);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING Statuses of terms in thesaurus : " + thesaurusName1 + ".");
        for (int i = 0; i < allStatuses.size(); i++) {

            String newStatusValue = allStatuses.get(i).getValue().replaceFirst(thesaurusName1, mergedThesaurusName);
            Q.reset_name_scope();
            long newIDL = Q.set_current_node(new StringObject(newStatusValue));
            merged_thesaurus_status_classIds.put(newStatusValue, newIDL);

            Q.reset_name_scope();
            Q.set_current_node(allStatuses.get(i));
            int set_all_such_terms = Q.get_instances(0);
            Q.reset_set(set_all_such_terms);
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(set_all_such_terms, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    String UIName = dbGen.removePrefix(row.get_v1_cls_logicalname());
                    thesaurus1_statuses.put(UIName, newStatusValue);
                }
            }
            /*StringObject nodeName = new StringObject();

             while (Q.retur_nodes(set_all_such_terms, nodeName) != QClass.APIFail) {
             String UIName = dbGen.removePrefix(nodeName.getValue());
             thesaurus1_statuses.put(UIName, newStatusValue);
             }*/

            Q.free_set(set_all_such_terms);
        }

        Q.free_all_sets();
        Q.reset_name_scope();
        //READING FROM THES2 IN CASE OF MERGE INSTEAD OF JUST COPY
        if (thesaurusName2 != null) {

            //wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);
            dbtr.getThesaurusClass_StatusForInsertion(thesaurusName2, THESstatusForInsertion);
            dbtr.getThesaurusClass_StatusUnderConstruction(thesaurusName2, THESstatusUnderConstruction);
            dbtr.getThesaurusClass_StatusForApproval(thesaurusName2, THESstatusForApproval);
            dbtr.getThesaurusClass_StatusApproved(thesaurusName2, THESstatusApproved);

            allStatuses.clear();
            allStatuses.add(THESstatusForInsertion);
            allStatuses.add(THESstatusUnderConstruction);
            allStatuses.add(THESstatusForApproval);
            allStatuses.add(THESstatusApproved);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING Statuses of terms in thesaurus : " + thesaurusName2 + ".");
            logFileWriter.flush();
            for (int i = 0; i < allStatuses.size(); i++) {
                String newStatusValue = allStatuses.get(i).getValue().replaceFirst(thesaurusName2, mergedThesaurusName);
                Q.reset_name_scope();
                Q.set_current_node(allStatuses.get(i));
                int set_all_such_terms = Q.get_instances(0);
                Q.reset_set(set_all_such_terms);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if (Q.bulk_return_nodes(set_all_such_terms, retVals) != QClass.APIFail) {
                    for (Return_Nodes_Row row : retVals) {
                        String UIName = dbGen.removePrefix(row.get_v1_cls_logicalname());
                        thesaurus2_statuses.put(UIName, newStatusValue);
                    }
                }
                /*StringObject nodeName = new StringObject();

                 while (Q.retur_nodes(set_all_such_terms, nodeName) != QClass.APIFail) {
                 String UIName = dbGen.removePrefix(nodeName.getValue());
                 thesaurus2_statuses.put(UIName, newStatusValue);
                 }*/

                Q.free_set(set_all_such_terms);
            }

            Q.free_all_sets();
        }
        return CreateStatuses(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                mergedThesaurusName, logFileWriter, resultObj, thesaurus1_statuses, thesaurus2_statuses, merged_thesaurus_status_classIds);
    }

    public boolean CreateStatuses(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj,
            HashMap<String, String> thesaurus1_statuses, HashMap<String, String> thesaurus2_statuses,
            HashMap<String, Long> merged_thesaurus_status_classIds) throws IOException {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBFilters dbF = new DBFilters();
        //THEMASUsers wtmsUsers = new UsersClass();

        StringObject genericStatusObject = new StringObject();
        dbtr.getThesaurusClass_StatusOfTerm(mergedThesaurusName,genericStatusObject);
        
        
        
        
        String minorPriorityStatusKey = "";
        int minorPriority = 1000;
        Iterator<String> statusEnum = merged_thesaurus_status_classIds.keySet().iterator();
        while (statusEnum.hasNext()) {
            String checkStatus = statusEnum.next();
            int tempMinor = this.getStatusPriority(checkStatus, mergedThesaurusName);
            if (tempMinor < minorPriority) {
                minorPriority = tempMinor;
                minorPriorityStatusKey = checkStatus;
            }
        }
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        int howmany = thesaurus1_statuses.size();
        int counter = 0;
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCreating Statuses to terms in thesaurus : " + mergedThesaurusName + ".");
        logFileWriter.flush();
        
        
        Q.reset_name_scope();
        long checkGenericExists = Q.set_current_node(genericStatusObject);
        if (checkGenericExists == QClass.APIFail) {            
            resultObj.setValue("Failure: Could Not find class: " + genericStatusObject.toString() + ".");
            return false;
        }
        // Collect all statusues in statusesSet so that this set will be intersected with 
        //each term classes to see if any existing status valu has been defined in the database 
        //(so that it is removed)
        int statusesSet = Q.get_subclasses(0);
        Q.reset_set(statusesSet);
        
        String mergedTermPrefix = dbtr.getThesaurusPrefix_Descriptor(mergedThesaurusName, Q, sis_session.getValue());
        Iterator<String> pairsEnumMerged = thesaurus1_statuses.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {

            if (counter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Status counter: " + counter + " of " + howmany + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                    
                    //after restart the all statuses set must be calculated again because it was lost
                    Q.reset_name_scope();
                    checkGenericExists = Q.set_current_node(genericStatusObject);
                    if (checkGenericExists == QClass.APIFail) {            
                        resultObj.setValue("Failure: Could Not find class: " + genericStatusObject.toString() + ".");
                        return false;
                    }

                    statusesSet = Q.get_subclasses(0);
                    Q.reset_set(statusesSet);                    
                }
            }
            counter++;
            //term was found in thesaurus1 with under statusClass : status
            String term = pairsEnumMerged.next();
            String StatusThes1 = thesaurus1_statuses.get(term);
            String StatusThes2 = thesaurus2_statuses.get(term);   // Both statuses kept in HashMaps refer to mergedThesaurus Status Classes

            if (StatusThes1 != null && merged_thesaurus_status_classIds.containsKey(StatusThes1) == false) {
                Utils.StaticClass.webAppSystemOutPrintln("Status: " + StatusThes1 + " not found. Changing to minor priority Status.");
                StatusThes1 = minorPriorityStatusKey;//dbF.GetDefaultStatusForTermCreation(SessionUserInfo);
            }
            if (StatusThes2 != null && merged_thesaurus_status_classIds.containsKey(StatusThes2) == false) {
                Utils.StaticClass.webAppSystemOutPrintln("Status: " + StatusThes2 + " not found. Changing to minor priority Status.");
                StatusThes2 = minorPriorityStatusKey;//dbF.GetDefaultStatusForTermCreation(SessionUserInfo);

            }
            Q.reset_name_scope();
            long termIDL = Q.set_current_node(new StringObject(mergedTermPrefix.concat(term)));

            
            if (termIDL == QClass.APIFail) {

                resultObj.setValue("Failure: Could Not find term: " + term + " in order to update its status.");
                Q.free_set(statusesSet);
                return false;
            }
            int classesSet = Q.get_classes(0);
            
            ArrayList<Return_Nodes_Row> existingStatuses = new ArrayList<>();
            Q.set_intersect(classesSet, statusesSet);
            if(Q.set_get_card(classesSet)>0){
                //in this case instance value should be removed
                if(Q.bulk_return_nodes(classesSet, existingStatuses)!=QClass.APIFail){
                    /* DEBUG
                    existingStatuses.forEach((row) -> {
                        System.out.println("term: " + term +" belongs to "+row.get_v1_cls_logicalname()+" with id " + row.get_Neo4j_NodeId());
                    });                   
                    */
                }                
            }
            
            Q.free_set(classesSet);
            Q.reset_name_scope();
            int ret = QClass.APISucc;

            if (StatusThes1 == null && StatusThes2 != null) {
                Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                Identifier I_Term = new Identifier(termIDL);
                
                boolean addInstance = true;
                if(existingStatuses.size()>0){
                    for(Return_Nodes_Row row : existingStatuses)
                    {
                        if(row.get_Neo4j_NodeId() != I_Status.getSysid()){
                            ret = Q.CHECK_Delete_Instance(I_Term, new Identifier(row.get_Neo4j_NodeId()));
                            if(ret==QClass.APIFail){
                                System.out.println("Failed to remove status: " + row.get_v1_cls_logicalname() +" from term "+term);
                                Q.free_set(statusesSet);
                                return false;                                
                            }
                        }
                        else{
                            addInstance = false;
                        }
                    }
                }
                if(addInstance){
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }
            } else if (StatusThes1 != null && StatusThes2 == null) {
                Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes1).longValue());
                Identifier I_Term = new Identifier(termIDL);
                
                
                boolean addInstance = true;
                if(existingStatuses.size()>0){
                    for(Return_Nodes_Row row : existingStatuses)
                    {
                        if(row.get_Neo4j_NodeId() != I_Status.getSysid()){
                            ret = Q.CHECK_Delete_Instance(I_Term, new Identifier(row.get_Neo4j_NodeId()));
                            if(ret==QClass.APIFail){
                                System.out.println("Failed to remove status: " + row.get_v1_cls_logicalname() +" from term "+term);
                                Q.free_set(statusesSet);
                                return false;                                
                            }
                        }
                        else{
                            addInstance = false;
                        }
                    }
                }
                if(addInstance){
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }                
            } else { // select appropriate status
                String StatusThes1StrGR = "";
                String StatusThes2StrGR = "";
                if (StatusThes1.endsWith("StatusForInsertion")) {
                    StatusThes1StrGR = Parameters.Status_For_Insertion;
                } else if (StatusThes1.endsWith("StatusUnderConstruction")) {
                    StatusThes1StrGR = Parameters.Status_Under_Construction;
                } else if (StatusThes1.endsWith("StatusForApproval")) {
                    StatusThes1StrGR = Parameters.Status_For_Approval;
                } else if (StatusThes1.endsWith("StatusApproved")) {
                    StatusThes1StrGR = Parameters.Status_Approved;
                }

                if (StatusThes2.endsWith("StatusForInsertion")) {
                    StatusThes2StrGR = Parameters.Status_For_Insertion;
                } else if (StatusThes2.endsWith("StatusUnderConstruction")) {
                    StatusThes2StrGR = Parameters.Status_Under_Construction;
                } else if (StatusThes2.endsWith("StatusForApproval")) {
                    StatusThes2StrGR = Parameters.Status_For_Approval;
                } else if (StatusThes2.endsWith("StatusApproved")) {
                    StatusThes2StrGR = Parameters.Status_Approved;
                }

                StringObject resultMessageObj_2 = new StringObject();
                
                int priority = getStatusPriority(StatusThes2, mergedThesaurusName) - getStatusPriority(StatusThes1, mergedThesaurusName);
                if (priority > 0) {
                    
                    logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name><errorType>" + ConstantParameters.status_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + StatusThes2StrGR + "</errorValue>");
                    logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateStatuses/FoundInStatus", new String[]{Utilities.escapeXML(term), StatusThes1StrGR, StatusThes2StrGR, StatusThes1StrGR}) + "</reason>");
                    //logFileWriter.append("<reason>Term: " + Utilities.escapeXML(term) + " was found with statusues: '" + StatusThes1StrGR + "' and '" + StatusThes2StrGR + "'.\r\n\t\tStatus: '" + StatusThes1StrGR + "' was chosen.</reason>");
                    logFileWriter.append("</targetTerm>\r\n");
                    logFileWriter.flush();
                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes1).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    
                    boolean addInstance = true;
                    if(existingStatuses.size()>0){
                        for(Return_Nodes_Row row : existingStatuses)
                        {
                            if(row.get_Neo4j_NodeId() != I_Status.getSysid()){
                                ret = Q.CHECK_Delete_Instance(I_Term, new Identifier(row.get_Neo4j_NodeId()));
                                if(ret==QClass.APIFail){
                                    System.out.println("Failed to remove status: " + row.get_v1_cls_logicalname() +" from term "+term);
                                    Q.free_set(statusesSet);
                                    return false;                                
                                }
                            }
                            else{
                                addInstance = false;
                            }
                        }
                    }
                    if(addInstance){
                        ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                    }                    

                } else { //both status != null
                    if (priority < 0) {
                        
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name><errorType>" + ConstantParameters.status_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + StatusThes1StrGR + "</errorValue>");
                        logFileWriter.append("<reason>" + u.translateFromMessagesXML("root/CreateStatuses/FoundInStatus", new String[]{Utilities.escapeXML(term), StatusThes1StrGR, StatusThes2StrGR, StatusThes1StrGR}) + "</reason>");
                        //logFileWriter.append("<reason>Term: " + Utilities.escapeXML(term) + " was found with statusues: '" + StatusThes1StrGR + "' and '" + StatusThes2StrGR + "'.\r\n\t\tStatus: '" + StatusThes2StrGR + "' was chosen.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }
            }

            if (ret == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CreateStatuses/FailedToUpdateTermStatus", new String[]{term}));
                //resultObj.setValue("Failed to update status of term: " + term);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                Q.free_set(statusesSet);
                return false;
            }
        }

        if (thesaurus2_statuses != null) {
            Iterator<String> pairsEnumMerged2 = thesaurus2_statuses.keySet().iterator();
            while (pairsEnumMerged2.hasNext()) {
                //term was found in thesaurus2 with under statusClass : status
                String term = pairsEnumMerged2.next();
                String StatusThes1 = thesaurus1_statuses.get(term);
                String StatusThes2 = thesaurus2_statuses.get(term);   // Both statuses kept in HashMaps refer to mergedThesaurus Status Classes

                Q.reset_name_scope();
                long termIDL = Q.set_current_node(new StringObject(mergedTermPrefix.concat(term)));

                if (termIDL == QClass.APIFail) {
                    
                    resultObj.setValue(u.translateFromMessagesXML("root/CreateStatuses/TermReferenceFailed", new String[]{term,mergedThesaurusName}));
                    //resultObj.setValue("Failed to refer to " + term + " of thesaurus: " + mergedThesaurusName);
                    Q.free_set(statusesSet);
                    return false;
                }
                Q.reset_name_scope();
                int ret = QClass.APISucc;
                ArrayList<String> decodedValues = new ArrayList<String>();
                if (StatusThes1 == null && StatusThes2 != null) { // else decision has already been made
                    decodedValues.add(StatusThes2);

                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }

                if (ret == QClass.APIFail) {

                    resultObj.setValue(u.translateFromMessagesXML("root/CreateStatuses/FailedToUpdateTermStatus", new String[]{term}));
                    //resultObj.setValue("Failed to update status of term: " + term);
                    Q.free_set(statusesSet);
                    return false;
                }
            }
        }
        Q.free_set(statusesSet);
        return true;
    }

    public boolean CopyCommentCategories(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName,
            OutputStreamWriter logFileWriter, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();
        StringObject scopenoteFromClassObj = new StringObject();
        StringObject scopenoteLinkObj = new StringObject();
        StringObject scopenote_TR_FromClassObj = new StringObject();
        StringObject scopenote_TR_LinkObj = new StringObject();
        StringObject historicalnoteFromClassObj = new StringObject();
        StringObject historicalnoteLinkObj = new StringObject();
        StringObject commentNoteFromClassObj = new StringObject();
        StringObject commentNoteLinkObj = new StringObject();
        StringObject noteClassObj = new StringObject();
        StringObject noteLinkObj = new StringObject();
        Utilities u = new Utilities();

        HashMap<String, String> scope_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> scope_notes_EN_HASH = new HashMap<String, String>();
        HashMap<String, String> historical_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> comment_notes_HASH = new HashMap<String, String>();
        HashMap<String, String> note_notes_HASH = new HashMap<String, String>();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName1 + ".");
        logFileWriter.flush();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.comment_kwd, commentNoteFromClassObj, commentNoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.note_kwd, noteClassObj, noteLinkObj, Q, sis_session);

        

        Q.reset_name_scope();
        //Find out hierarchy Classes and get their instances
        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] TermClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses);
        int set_terms = dbGen.get_Instances_Set(TermClasses, Q, sis_session);
        Q.reset_set(set_terms);

        ArrayList<String> termNames = dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session);

        Q.free_set(set_terms);
        int commentCounter = 0;
        for (int i = 0; i < termNames.size(); i++) {
            Q.reset_name_scope();
            ArrayList<String> scopeNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.scope_note_kwd, Q, TA, sis_session);
            ArrayList<String> scopeNoteEN = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
            ArrayList<String> historicalNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.historical_note_kwd, Q, TA, sis_session);
            
            ArrayList<String> commentNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.comment_kwd, Q, TA, sis_session);
            ArrayList<String> noteNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.note_kwd, Q, TA, sis_session);

            if (scopeNote != null && scopeNote.size() > 0 && scopeNote.get(0).length() > 0) {
                commentCounter++;
                scope_notes_HASH.put(termNames.get(i), scopeNote.get(0));
                // logFileWriter.append(commentCounter + ". READING Scope Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
            }

            if (scopeNoteEN != null && scopeNoteEN.size() > 0 && scopeNoteEN.get(0).length() > 0) {
                commentCounter++;
                scope_notes_EN_HASH.put(termNames.get(i), scopeNoteEN.get(0));
                // logFileWriter.append(commentCounter + ". READING Scope Note EN from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
            }

            if (historicalNote != null && historicalNote.size() > 0 && historicalNote.get(0).length() > 0) {
                commentCounter++;
                historical_notes_HASH.put(termNames.get(i), historicalNote.get(0));
                // logFileWriter.append(commentCounter + ". READING Historical Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
            }
            
            if (commentNote != null && commentNote.size() > 0 && commentNote.get(0).length() > 0) {
                commentCounter++;
                comment_notes_HASH.put(termNames.get(i), commentNote.get(0));
                // logFileWriter.append(commentCounter + ". READING Historical Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
            }
            
                        
            if (noteNote != null && noteNote.size() > 0 && noteNote.get(0).length() > 0) {
                commentCounter++;
                note_notes_HASH.put(termNames.get(i), noteNote.get(0));
                // logFileWriter.append(commentCounter + ". READING Historical Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
            }
        }

        Q.free_all_sets();

        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName2 + ".");

            logFileWriter.flush();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.comment_kwd, commentNoteFromClassObj, commentNoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.note_kwd, noteClassObj, noteLinkObj, Q, sis_session);

            Q.reset_name_scope();
            //Find out hierarchy Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("TERM");
            String[] TermClasses2 = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses2);
            set_terms = dbGen.get_Instances_Set(TermClasses2, Q, sis_session);
            Q.reset_set(set_terms);

            termNames.clear();
            termNames = dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session);

            Q.free_set(set_terms);
            //commentCounter = 0;
            for (int i = 0; i < termNames.size(); i++) {
                Q.reset_name_scope();
                ArrayList<String> scopeNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.scope_note_kwd, Q, TA, sis_session);
                ArrayList<String> scopeNoteEN = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
                ArrayList<String> historicalNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.historical_note_kwd, Q, TA, sis_session);
                ArrayList<String> commentNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.comment_kwd, Q, TA, sis_session);
                ArrayList<String> noteNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.note_kwd, Q, TA, sis_session);


                if (scopeNote != null && scopeNote.size() > 0 && scopeNote.get(0).length() > 0) {
                    commentCounter++;
                    String firstScopeNote = scope_notes_HASH.get(termNames.get(i));
                    String secondScopenote = scopeNote.get(0);
                    if (firstScopeNote == null) {
                        firstScopeNote = secondScopenote;
                    } else if (firstScopeNote.equals(secondScopenote) == false) { //if not equal then keep both with a seperator
                        
                        
                        
                        
                        firstScopeNote = firstScopeNote.concat(" ### " + scopeNote.get(0));
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termNames.get(i)) + "</name><errorType>" + ConstantParameters.scope_note_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(firstScopeNote) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeTermSNs", new String[] { Utilities.escapeXML(termNames.get(i))})+"</reason>");
                        //logFileWriter.append("<reason>Two SNs were found for term: '" + Utilities.escapeXML(termNames.get(i)) + "'. Both will be kept with delimeter: ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    scope_notes_HASH.put(termNames.get(i), firstScopeNote);
                    // logFileWriter.append(commentCounter + ". READING Scope Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
                }

                if (scopeNoteEN != null && scopeNoteEN.size() > 0 && scopeNoteEN.get(0).length() > 0) {
                    commentCounter++;
                    String firstScopeNoteEN = scope_notes_EN_HASH.get(termNames.get(i));
                    String secondScopenoteEN = scopeNoteEN.get(0);
                    if (firstScopeNoteEN == null) {
                        firstScopeNoteEN = secondScopenoteEN;
                    } else if (firstScopeNoteEN.equals(secondScopenoteEN) == false) {
                        firstScopeNoteEN = firstScopeNoteEN.concat(" ### " + secondScopenoteEN);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termNames.get(i)) + "</name><errorType>" + ConstantParameters.translations_scope_note_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(firstScopeNoteEN) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeTerm_trSNs", new String[] { Utilities.escapeXML(termNames.get(i))})+"</reason>");
                        //logFileWriter.append("<reason>Two SNs (Tra.) were found for term: '" + Utilities.escapeXML(termNames.get(i)) + "'. Both will be kept with delimeter: ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();

                    }
                    scope_notes_EN_HASH.put(termNames.get(i), firstScopeNoteEN);
                    // logFileWriter.append(commentCounter + ". READING Scope Note EN from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
                }

                if (historicalNote != null && historicalNote.size() > 0 && historicalNote.get(0).length() > 0) {
                    commentCounter++;
                    String firstHistoricalNote = historical_notes_HASH.get(termNames.get(i));
                    String secondHistoricalNote = historicalNote.get(0);
                    if (firstHistoricalNote == null) {
                        firstHistoricalNote = secondHistoricalNote;
                    } else if (firstHistoricalNote.equals(secondHistoricalNote) == false) {

                        firstHistoricalNote = firstHistoricalNote.concat(" ### " + secondHistoricalNote);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termNames.get(i)) + "</name><errorType>" + ConstantParameters.historical_note_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(firstHistoricalNote) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeTermHNs", new String[] { Utilities.escapeXML(termNames.get(i))})+"</reason>");
                        //logFileWriter.append("<reason>Two HNs were found for term: '" + Utilities.escapeXML(termNames.get(i)) + "'. Both will be kept with delimeter: ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    historical_notes_HASH.put(termNames.get(i), firstHistoricalNote);
                    // logFileWriter.append(commentCounter + ". READING Historical Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
                }
                
                if (commentNote != null && commentNote.size() > 0 && commentNote.get(0).length() > 0) {
                    commentCounter++;
                    String firstCommentNote = comment_notes_HASH.get(termNames.get(i));
                    String secondCommentNote = commentNote.get(0);
                    if (firstCommentNote == null) {
                        firstCommentNote = secondCommentNote;
                    } else if (firstCommentNote.equals(secondCommentNote) == false) {

                        firstCommentNote = firstCommentNote.concat(" ### " + secondCommentNote);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termNames.get(i)) + "</name><errorType>" + ConstantParameters.comment_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(firstCommentNote) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeTermCNs", new String[] { Utilities.escapeXML(termNames.get(i))})+"</reason>");
                        //logFileWriter.append("<reason>Two Comments were found for term: '" + Utilities.escapeXML(termNames.get(i)) + "'. Both will be kept with delimeter: ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    comment_notes_HASH.put(termNames.get(i), firstCommentNote);                    
                }
                
                
                if (noteNote != null && noteNote.size() > 0 && noteNote.get(0).length() > 0) {
                    commentCounter++;
                    String firstNote = note_notes_HASH.get(termNames.get(i));
                    String secondNote = noteNote.get(0);
                    if (firstNote == null) {
                        firstNote = secondNote;
                    } else if (firstNote.equals(secondNote) == false) {

                        firstNote = firstNote.concat(" ### " + secondNote);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(termNames.get(i)) + "</name><errorType>" + ConstantParameters.note_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(firstNote) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/MergeTermNs", new String[] { Utilities.escapeXML(termNames.get(i))})+"</reason>");
                        //logFileWriter.append("<reason>Two Comments were found for term: '" + Utilities.escapeXML(termNames.get(i)) + "'. Both will be kept with delimeter: ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    note_notes_HASH.put(termNames.get(i), firstNote);                    
                }
            }

            Q.free_all_sets();

        }

        logFileWriter.flush();
        return CreateCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, mergedThesaurusName, 
                scope_notes_HASH, scope_notes_EN_HASH, historical_notes_HASH,comment_notes_HASH, note_notes_HASH,
                logFileWriter, pathToErrorsXML, resultObj, ConsistencyCheckPolicy);
    }

    public boolean CreateCommentCategories(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String mergedThesaurusName,
            HashMap<String, String> scope_notes_HASH,
            HashMap<String, String> scope_notes_EN_HASH,
            HashMap<String, String> historical_notes_HASH,
            HashMap<String, String> comment_notes_HASH,
            HashMap<String, String> note_notes_HASH,
            OutputStreamWriter logFileWriter, String pathToErrorsXML, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING SCOPE_NOTES in " + mergedThesaurusName + ".  Time: " + Utilities.GetNow());
        //common_utils.restartTransactionAndDatabase(Q,TA,sis_session,tms_session,mergedThesaurusName);
        logFileWriter.flush();
        int commentCounter = 0;

        int howmanyComments = scope_notes_HASH.size();
        howmanyComments += scope_notes_EN_HASH.size();
        howmanyComments += historical_notes_HASH.size();
        howmanyComments += comment_notes_HASH.size();
        howmanyComments += note_notes_HASH.size();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefixPerson = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        String user = prefixPerson.concat(SessionUserInfo.name);
        Iterator<String> pairsEnumMerged = scope_notes_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (SN) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.next();

            ArrayList<String> sns = new ArrayList<String>();
            sns.add(scope_notes_HASH.get(term));

            if (sns.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.scope_note_kwd, sns, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Failed to add SN in term: '" + term + "'. " + resultObj.getValue());
                return false;
            } else {
                //logFileWriter.append(commentCounter +". SN of term: '" + term+"' was successfully added.\r\n");
            }

            //commentCounter++;
            resultObj.setValue("");

        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING TRANSLATION SCOPE_NOTES in " + mergedThesaurusName + " Time: " + Utilities.GetNow());

        pairsEnumMerged = scope_notes_EN_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {

            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (SN-Tra.) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;
            String term = pairsEnumMerged.next();

            ArrayList<String> snsEN = new ArrayList<String>();
            snsEN.add(scope_notes_EN_HASH.get(term));

            if (snsEN.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.translations_scope_note_kwd, snsEN, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Failed to add SN (Tra.) in term: '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". SN (Tra.) was succesfully added in term: '" + term+"'.\r\n");
            }
            //logFileWriter.flush();
            //commentCounter++;
            resultObj.setValue("");

            //logFileWriter.append(commentCounter + ". " + term + " ----- " + scope_notes.get(term).toString() + "\r\n");
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING HISTORICAL_NOTES in " + mergedThesaurusName + ". Time: " + Utilities.GetNow());

        pairsEnumMerged = historical_notes_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (HN) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.next();
            ArrayList<String> hns = new ArrayList<String>();
            hns.add(historical_notes_HASH.get(term));

            if (hns.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.historical_note_kwd, hns, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Failed to add HN in term: '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". HN of term: '" + term+"' was successfully added.\r\n");
            }
            //logFileWriter.flush();
            //commentCounter++;
            resultObj.setValue("");

            //logFileWriter.append(commentCounter + ". " + term + " ----- " + scope_notes.get(term).toString() + "\r\n");
        }
        
        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING Comment Notes in " + mergedThesaurusName + ". Time: " + Utilities.GetNow());

        pairsEnumMerged = comment_notes_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.next();
            ArrayList<String> comments = new ArrayList<String>();
            comments.add(comment_notes_HASH.get(term));

            if (comments.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.comment_kwd, comments, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Failed to add Comment in term: '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". HN of term: '" + term+"' was successfully added.\r\n");
            }
            //logFileWriter.flush();
            //commentCounter++;
            resultObj.setValue("");

            //logFileWriter.append(commentCounter + ". " + term + " ----- " + scope_notes.get(term).toString() + "\r\n");
        }
        
        
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING NOTES in " + mergedThesaurusName + ". Time: " + Utilities.GetNow());

        pairsEnumMerged = note_notes_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (Notes) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.next();
            ArrayList<String> notes = new ArrayList<String>();
            notes.add(note_notes_HASH.get(term));

            if (notes.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.note_kwd, notes, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Failed to add Note in term: '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". HN of term: '" + term+"' was successfully added.\r\n");
            }
            //logFileWriter.flush();
            //commentCounter++;
            resultObj.setValue("");

            //logFileWriter.append(commentCounter + ". " + term + " ----- " + scope_notes.get(term).toString() + "\r\n");
        }
        return true;
    }

    public boolean CopyDatesAndEditors(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String pathToErrorsXML,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName,
            OutputStreamWriter logFileWriter, String editorKeyWordStr, StringObject resultObj) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        UsersClass wtmsUsers = new UsersClass();

        HashMap<String, ArrayList<String>> term_Editor_Links_THES1_HASH = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> term_Date_Links_THES1_HASH = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> term_Editor_Links_THES2_HASH = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> term_Date_Links_THES2_HASH = new HashMap<String, ArrayList<String>>();

        StringObject editorFromClassObj = new StringObject();
        StringObject editorLinkObj = new StringObject();
        StringObject dateFromClassObj = new StringObject();
        StringObject dateLinkObj = new StringObject();

        String dateKeywordStr = new String("");
        if (editorKeyWordStr.compareTo(ConstantParameters.created_by_kwd) == 0) {
            dateKeywordStr = ConstantParameters.created_on_kwd;
        } else if (editorKeyWordStr.compareTo(ConstantParameters.modified_by_kwd) == 0) {
            dateKeywordStr = ConstantParameters.modified_on_kwd;
        }

        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        //<editor-fold defaultstate="collapsed" desc="Read Thes1">  
        //READ FIRST THESAURUS LINKS.
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Q.reset_name_scope();
        Q.free_all_sets();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, editorKeyWordStr, editorFromClassObj, editorLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, dateKeywordStr, dateFromClassObj, dateLinkObj, Q, sis_session);

        //READ EDITOR LINKS OF THES1
        Q.reset_name_scope();
        if (Q.set_current_node(editorFromClassObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/ClassReferenceFailure", new String[] {editorFromClassObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Class: " + editorFromClassObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(editorLinkObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/CategoryReferenceFailure", new String[] {editorFromClassObj.getValue(),editorLinkObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Category: " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        int set_all_links = Q.get_instances(0);
        Q.reset_set(set_all_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " Links from " + thesaurusName1 + ".");

        Q.reset_name_scope();

        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String termName = dbGen.removePrefix(row.get_v1_cls());
                String editorValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                ArrayList<String> otherVals = term_Editor_Links_THES1_HASH.get(termName);

                if (otherVals == null) {
                    otherVals = new ArrayList<String>();
                    otherVals.add(editorValue);
                    term_Editor_Links_THES1_HASH.put(termName, otherVals);
                } else {
                    if (otherVals.contains(editorValue) == false) {
                        otherVals.add(editorValue);
                        term_Editor_Links_THES1_HASH.put(termName, otherVals);
                    }
                }
            }
        }
        /*
         while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

         String termName = dbGen.removePrefix(cls.getValue());
         String editorValue = dbGen.removePrefix(cmv.getString());
         ArrayList<String> otherVals = term_Editor_Links_THES1_HASH.get(termName);

         if (otherVals == null) {
         otherVals = new ArrayList<String>();
         otherVals.add(editorValue);
         term_Editor_Links_THES1_HASH.put(termName, otherVals);
         } else {
         if (otherVals.contains(editorValue) == false) {
         otherVals.add(editorValue);
         term_Editor_Links_THES1_HASH.put(termName, otherVals);
         }
         }
         }*/

        //READ DATE LINKS OF THES1
        Q.free_all_sets();
        Q.reset_name_scope();
        if (Q.set_current_node(dateFromClassObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/ClassReferenceFailure", new String[] {dateFromClassObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Class: " + dateFromClassObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(dateLinkObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/CategoryReferenceFailure", new String[] {dateFromClassObj.getValue(), dateLinkObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Category: " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        set_all_links = Q.get_instances(0);
        Q.reset_set(set_all_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " Links from " + thesaurusName1 + ".");

        Q.reset_name_scope();

        retVals.clear();
        if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String termName = dbGen.removePrefix(row.get_v1_cls());
                String dateValue = row.get_v3_cmv().getString();//Dates have no prefix
                ArrayList<String> otherVals = term_Date_Links_THES1_HASH.get(termName);

                if (otherVals == null) {
                    otherVals = new ArrayList<String>();
                    otherVals.add(dateValue);
                    term_Date_Links_THES1_HASH.put(termName, otherVals);
                } else {
                    if (otherVals.contains(dateValue) == false) {
                        otherVals.add(dateValue);
                        term_Date_Links_THES1_HASH.put(termName, otherVals);
                    }
                }
            }
        }
        /*while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

         String termName = dbGen.removePrefix(cls.getValue());
         String dateValue = cmv.getString();//Dates have no prefix
         ArrayList<String> otherVals = term_Date_Links_THES1_HASH.get(termName);

         if (otherVals == null) {
         otherVals = new ArrayList<String>();
         otherVals.add(dateValue);
         term_Date_Links_THES1_HASH.put(termName, otherVals);
         } else {
         if (otherVals.contains(dateValue) == false) {
         otherVals.add(dateValue);
         term_Date_Links_THES1_HASH.put(termName, otherVals);
         }
         }
         }*/
        //</editor-fold>

        Q.free_all_sets();
        Q.reset_name_scope();

        if (thesaurusName2 != null) {
            //<editor-fold defaultstate="collapsed" desc="Read Thes2">  
            //READ SECOND THESAURUS LINKS
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, editorKeyWordStr, editorFromClassObj, editorLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, dateKeywordStr, dateFromClassObj, dateLinkObj, Q, sis_session);

            //READ EDITOR LINKS OF THES2
            Q.reset_name_scope();
            if (Q.set_current_node(editorFromClassObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/ClassReferenceFailure", new String[] {editorFromClassObj.getValue(),thesaurusName2}));
                //resultObj.setValue("Failed to refer to Class: " + editorFromClassObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(editorLinkObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/CategoryReferenceFailure", new String[] {editorFromClassObj.getValue(),editorLinkObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Category: " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                return false;
            }

            set_all_links = Q.get_instances(0);
            Q.reset_set(set_all_links);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " Links from " + thesaurusName2 + ".");

            Q.reset_name_scope();

            retVals.clear();
            if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
                for (Return_Link_Row row : retVals) {

                    String termName = dbGen.removePrefix(row.get_v1_cls());
                    String editorValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                    ArrayList<String> otherVals = term_Editor_Links_THES2_HASH.get(termName);

                    if (otherVals == null) {
                        otherVals = new ArrayList<String>();
                        otherVals.add(editorValue);
                        term_Editor_Links_THES2_HASH.put(termName, otherVals);
                    } else {
                        if (otherVals.contains(editorValue) == false) {
                            otherVals.add(editorValue);
                            term_Editor_Links_THES2_HASH.put(termName, otherVals);
                        }
                    }
                }
            }
            /*while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

             String termName = dbGen.removePrefix(cls.getValue());
             String editorValue = dbGen.removePrefix(cmv.getString());
             ArrayList<String> otherVals = term_Editor_Links_THES2_HASH.get(termName);

             if (otherVals == null) {
             otherVals = new ArrayList<String>();
             otherVals.add(editorValue);
             term_Editor_Links_THES2_HASH.put(termName, otherVals);
             } else {
             if (otherVals.contains(editorValue) == false) {
             otherVals.add(editorValue);
             term_Editor_Links_THES2_HASH.put(termName, otherVals);
             }
             }
             }*/

            //READ DATE LINKS OF THES2
            Q.free_all_sets();
            Q.reset_name_scope();
            if (Q.set_current_node(dateFromClassObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/ClassReferenceFailure", new String[] {dateFromClassObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Class: " + dateFromClassObj.getValue() + " of thesaurus : " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(dateLinkObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopyDatesAndEditors/CategoryReferenceFailure", new String[] {dateFromClassObj.getValue(), dateLinkObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Category: " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                return false;
            }

            set_all_links = Q.get_instances(0);
            Q.reset_set(set_all_links);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " Links from " + thesaurusName2 + ".");

            Q.reset_name_scope();

            retVals.clear();
            if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
                for (Return_Link_Row row : retVals) {
                    String termName = dbGen.removePrefix(row.get_v1_cls());
                    String dateValue = row.get_v3_cmv().getString();//Dates have no prefix
                    ArrayList<String> otherVals = term_Date_Links_THES2_HASH.get(termName);

                    if (otherVals == null) {
                        otherVals = new ArrayList<String>();
                        otherVals.add(dateValue);
                        term_Date_Links_THES2_HASH.put(termName, otherVals);
                    } else {
                        if (otherVals.contains(dateValue) == false) {
                            otherVals.add(dateValue);
                            term_Date_Links_THES2_HASH.put(termName, otherVals);
                        }
                    }
                }
            }
            /*
             while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

             String termName = dbGen.removePrefix(cls.getValue());
             String dateValue = cmv.getString();//Dates have no prefix
             ArrayList<String> otherVals = term_Date_Links_THES2_HASH.get(termName);

             if (otherVals == null) {
             otherVals = new ArrayList<String>();
             otherVals.add(dateValue);
             term_Date_Links_THES2_HASH.put(termName, otherVals);
             } else {
             if (otherVals.contains(dateValue) == false) {
             otherVals.add(dateValue);
             term_Date_Links_THES2_HASH.put(termName, otherVals);
             }
             }
             }
             */

            Q.free_all_sets();
            Q.reset_name_scope();
            //</editor-fold>
        }

        return CreateDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, thesaurusName1, thesaurusName2, mergedThesaurusName, logFileWriter,
                term_Editor_Links_THES1_HASH, term_Date_Links_THES1_HASH, term_Editor_Links_THES2_HASH, term_Date_Links_THES2_HASH,
                editorKeyWordStr, resultObj);

    }

    public boolean CreateDatesAndEditors(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter,
            HashMap<String, ArrayList<String>> term_Editor_Links_THES1_HASH, HashMap<String, ArrayList<String>> term_Date_Links_THES1_HASH,
            HashMap<String, ArrayList<String>> term_Editor_Links_THES2_HASH, HashMap<String, ArrayList<String>> term_Date_Links_THES2_HASH,
            String editorKeyWordStr, StringObject resultObj) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();
        DBConnect_Term dbCon = new DBConnect_Term();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject editorFromClassObj = new StringObject();
        StringObject editorLinkObj = new StringObject();
        StringObject dateFromClassObj = new StringObject();
        StringObject dateLinkObj = new StringObject();

        String dateKeywordStr = new String("");
        if (editorKeyWordStr.compareTo(ConstantParameters.created_by_kwd) == 0) {
            dateKeywordStr = ConstantParameters.created_on_kwd;
        } else if (editorKeyWordStr.compareTo(ConstantParameters.modified_by_kwd) == 0) {
            dateKeywordStr = ConstantParameters.modified_on_kwd;
        }

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);
        //<editor-fold defaultstate="collapsed" desc="copy date-editor pairs from first thesaurus">

        String prefixPerson = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, editorKeyWordStr, editorFromClassObj, editorLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, dateKeywordStr, dateFromClassObj, dateLinkObj, Q, sis_session);

        int termsModified = 0;
        int totalSize = term_Date_Links_THES1_HASH.size() + term_Editor_Links_THES1_HASH.size();

        Iterator<String> pairsEnumMerged = term_Date_Links_THES1_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + dateKeywordStr + " (date) counter: " + termsModified + " of " + totalSize + "  ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;

            String term = pairsEnumMerged.next();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

            ArrayList<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            ArrayList<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
            //ArrayList<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            //ArrayList<String> datesThes2   = term_Date_Links_THES2_HASH.get(term);

            //Here datesThes1 could be compared in order to decide which thesaurus values to keep. 
            //In this case dates should only be of one value thus no vector needed just string
            //Bear in mind that Vectors might be null if a term is not defined in thes2 or editor is not defined in thes1
            //If all are coppied then just copy all values of thes1 and then when traversing thes2 values just check if 
            //its values are also declared in thes 1
            if (editorsThes1 != null && editorsThes1.size() > 0) {
                //create all editors for this term
                for (int i = 0; i < editorsThes1.size(); i++) {
                    resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes1.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    if (resultObj.getValue().length() > 0) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + editorLinkObj.getValue() + " : " + editorsThes1.get(i) + " of term: '" + term + "' from thesaurus: " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

            if (datesThes1 != null && datesThes1.size() > 0) {
                //create all dates from thes 1
                for (int i = 0; i < datesThes1.size(); i++) {
                    resultObj.setValue(resultObj.getValue().concat(dbCon.connectSpecificTime(SessionUserInfo.selectedThesaurus, targetTermObj, datesThes1.get(i), dateFromClassObj.getValue(), dateLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    if (resultObj.getValue().length() > 0) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + dateLinkObj.getValue() + " : " + datesThes1.get(i) + " of term: '" + term + "' from thesaurus: " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

            //termsModified++;
        }
        //</editor-fold>

        //<editor-fold desc="copy editors for term from thes1 with no DATE defined in thes 1">
        pairsEnumMerged = term_Editor_Links_THES1_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + dateKeywordStr + " (editor) counter: " + termsModified + " of " + totalSize + "  ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;
            String term = pairsEnumMerged.next();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));
            ArrayList<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            ArrayList<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
            //ArrayList<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            //ArrayList<String> datesThes2   = term_Date_Links_THES2_HASH.get(term);

            if (datesThes1 != null) {
                continue;//copird before
            }
            if (editorsThes1 != null && editorsThes1.size() > 0) {
                //create all editors for this term
                for (int i = 0; i < editorsThes1.size(); i++) {
                    resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes1.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    if (resultObj.getValue().length() > 0) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + editorLinkObj.getValue() + " : " + editorsThes1.get(i) + " of term: '" + term + "' from thesaurus: " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

        }
        //</editor-fold>

        //<editor-fold desc="copy dates and relevant editors for terms of thes 2. Done together because it may later be used to compare dates and write pairs editor/date.">
        if (thesaurusName2 != null) {

            pairsEnumMerged = term_Date_Links_THES2_HASH.keySet().iterator();
            while (pairsEnumMerged.hasNext()) {
                if (termsModified % restartInterval == 0) {
                    if (common_utils != null) {

                        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                    }
                }
                termsModified++;
                String term = pairsEnumMerged.next();
                StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

                ArrayList<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
                ArrayList<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
                ArrayList<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
                ArrayList<String> datesThes2 = term_Date_Links_THES2_HASH.get(term);

                if (editorsThes2 != null && editorsThes2.size() > 0) {

                    for (int i = 0; i < editorsThes2.size(); i++) {
                        if (editorsThes1 == null || editorsThes1.size() == 0 || editorsThes1.contains(editorsThes2.get(i)) == false) {
                            //create editor link
                            resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes2.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                            if (resultObj.getValue().length() > 0) {
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + editorLinkObj.getValue() + " : " + editorsThes2.get(i) + " of term: '" + term + "' from thesaurus:" + thesaurusName2 + "." + resultObj.getValue());
                                return false;
                            }
                        }//else value is already copied
                    }
                }

                if (datesThes2 != null && datesThes2.size() > 0) {

                    for (int i = 0; i < datesThes2.size(); i++) {
                        if (datesThes1 == null || datesThes1.size() == 0 || datesThes1.contains(datesThes2.get(i)) == false) {
                            //create date link
                            resultObj.setValue(resultObj.getValue().concat(dbCon.connectSpecificTime(SessionUserInfo.selectedThesaurus, targetTermObj, datesThes2.get(i), dateFromClassObj.getValue(), dateLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                            if (resultObj.getValue().length() > 0) {
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + dateLinkObj.getValue() + " : " + datesThes2.get(i) + " of term: '" + term + "' from thesaurus: " + thesaurusName2 + "." + resultObj.getValue());
                                return false;
                            }
                        }//else value is already copied

                    }
                }

            }
        }
        //</editor-fold>

        termsModified = 0;
        //<editor-fold desc="copy editors for term from thes 2 with no date defined in thes 2 and not already defined as editors from thes1.">
        pairsEnumMerged = term_Editor_Links_THES2_HASH.keySet().iterator();
        while (pairsEnumMerged.hasNext()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;
            String term = pairsEnumMerged.next();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

            ArrayList<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            //ArrayList<String> datesThes1   = term_Date_Links_THES1_HASH.get(term);
            ArrayList<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            ArrayList<String> datesThes2 = term_Date_Links_THES2_HASH.get(term);

            if (datesThes2 != null) {
                continue;//copird before
            }
            if (editorsThes2 != null && editorsThes2.size() > 0) {
                //create all editors for this term
                for (int i = 0; i < editorsThes2.size(); i++) {
                    if (editorsThes1 == null || editorsThes1.size() == 0 || editorsThes1.contains(editorsThes2.get(i)) == false) {
                        resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes2.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                        if (resultObj.getValue().length() > 0) {
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Failed to add values: " + editorLinkObj.getValue() + " : " + editorsThes2.get(i) + " of term: '" + term + "' from thesaurus: " + thesaurusName2 + "." + resultObj.getValue());
                            return false;
                        }
                    }

                }
            }

        }
        //</editor-fold>

        logFileWriter.flush();
        return true;
    }
    //SHOULD NOT BE USED FOR DATES --> NOT HAVING PREFIXES (DATES AND EDITORS ARE HANDLED BY DIFFERENT FUNCTION)

    public boolean CopySimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, String keyWordStr, ArrayList<String> skipNodes, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {
        //Copy Links to EnglishWords, Taxonomic Codes, Sources

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        UsersClass wtmsUsers = new UsersClass();

        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();

        HashMap<String, ArrayList<String>> term_Links_HASH = new HashMap<String, ArrayList<String>>();

        //READ FIRST THESAURUS LINKS.
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Q.reset_name_scope();
        Q.free_all_sets();

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, keyWordStr, fromClassObj, LinkObj, Q, sis_session);

        Q.reset_name_scope();
        if (Q.set_current_node(fromClassObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopySimpleLinks/ClassReferenceFailure", new String[] {fromClassObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Class: " + fromClassObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(LinkObj) == QClass.APIFail) {
            
            resultObj.setValue(u.translateFromMessagesXML("root/CopySimpleLinks/CategoryReferenceFailure", new String[] {fromClassObj.getValue(),LinkObj.getValue(), thesaurusName1}));
            //resultObj.setValue("Failed to refer to Category: " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " of thesaurus: " + thesaurusName1 + ".");
            return false;
        }

        int set_all_links = Q.get_instances(0);
        Q.reset_set(set_all_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " Links from " + thesaurusName1 + ".");

        Q.reset_name_scope();

        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String termName = dbGen.removePrefix(row.get_v1_cls());
                String linkValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                ArrayList<String> otherVals = term_Links_HASH.get(termName);
                if (skipNodes != null && skipNodes.contains(linkValue)) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Skipping copying of " + LinkObj.getValue() + " relation: " + linkValue + " for term: " + termName + " of thesaurus: " + thesaurusName1 + ".");
                    continue; // skip
                }
                if (otherVals == null) {
                    otherVals = new ArrayList<String>();
                    otherVals.add(linkValue);
                    term_Links_HASH.put(termName, otherVals);
                } else {
                    if (otherVals.contains(linkValue) == false) {
                        otherVals.add(linkValue);
                        term_Links_HASH.put(termName, otherVals);
                    }
                }
            }
        }
        /*while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

         String termName = dbGen.removePrefix(cls.getValue());
         String linkValue = dbGen.removePrefix(cmv.getString());
         ArrayList<String> otherVals = term_Links_HASH.get(termName);
         if (skipNodes != null && skipNodes.contains(linkValue)) {
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Skipping copying of " + LinkObj.getValue() + " relation: " + linkValue + " for term: " + termName + " of thesaurus: " + thesaurusName1 + ".");
         continue; // skip
         }
         if (otherVals == null) {
         otherVals = new ArrayList<String>();
         otherVals.add(linkValue);
         term_Links_HASH.put(termName, otherVals);
         } else {
         if (otherVals.contains(linkValue) == false) {
         otherVals.add(linkValue);
         term_Links_HASH.put(termName, otherVals);
         }
         }
         }*/

        if (thesaurusName2 != null) {

            //READ SECOND THESAURUS LINKS
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            Q.reset_name_scope();
            Q.free_all_sets();

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, keyWordStr, fromClassObj, LinkObj, Q, sis_session);

            Q.reset_name_scope();
            if (Q.set_current_node(fromClassObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopySimpleLinks/ClassReferenceFailure", new String[] {fromClassObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Class: " + fromClassObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(LinkObj) == QClass.APIFail) {
                
                resultObj.setValue(u.translateFromMessagesXML("root/CopySimpleLinks/CategoryReferenceFailure", new String[] {fromClassObj.getValue(), LinkObj.getValue(), thesaurusName2}));
                //resultObj.setValue("Failed to refer to Category: " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " of thesaurus: " + thesaurusName2 + ".");
                return false;
            }

            set_all_links = Q.get_instances(0);
            Q.reset_set(set_all_links);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " Links from " + thesaurusName2 + ".");

            Q.reset_name_scope();

            retVals.clear();
            if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
                for (Return_Link_Row row : retVals) {
                    String termName = dbGen.removePrefix(row.get_v1_cls());
                    String linkValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                    ArrayList<String> otherVals = term_Links_HASH.get(termName);
                    if (skipNodes != null && skipNodes.contains(linkValue)) {

                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Skipping copying of " + LinkObj.getValue() + " relation: " + linkValue + " for term: " + termName + " of thesaurus: " + thesaurusName2 + ".");
                        continue; // skip
                    }
                    if (otherVals == null) {
                        otherVals = new ArrayList<String>();
                        otherVals.add(linkValue);
                        term_Links_HASH.put(termName, otherVals);
                    } else {
                        if (otherVals.contains(linkValue) == false) {
                            otherVals.add(linkValue);
                            term_Links_HASH.put(termName, otherVals);
                        }
                    }
                }
            }
            /*while (Q.retur_link(set_all_links, cls, label, cmv) != QClass.APIFail) {

             String termName = dbGen.removePrefix(cls.getValue());
             String linkValue = dbGen.removePrefix(cmv.getString());
             ArrayList<String> otherVals = term_Links_HASH.get(termName);
             if (skipNodes != null && skipNodes.contains(linkValue)) {

             Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Skipping copying of " + LinkObj.getValue() + " relation: " + linkValue + " for term: " + termName + " of thesaurus: " + thesaurusName2 + ".");
             continue; // skip
             }
             if (otherVals == null) {
             otherVals = new ArrayList<String>();
             otherVals.add(linkValue);
             term_Links_HASH.put(termName, otherVals);
             } else {
             if (otherVals.contains(linkValue) == false) {
             otherVals.add(linkValue);
             term_Links_HASH.put(termName, otherVals);
             }
             }
             }*/

        }

        return CreateSimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, mergedThesaurusName, logFileWriter, keyWordStr, skipNodes, resultObj, term_Links_HASH, true, ConsistencyCheckPolicy);
    }

    public boolean CreateSimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String mergedThesaurusName, OutputStreamWriter logFileWriter, String keyWordStr, ArrayList<String> skipNodes, StringObject resultObj, HashMap<String, ArrayList<String>> term_Links_HASH, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();

        //START WRITING TO MERGED THESAURUS
        int linkRelations = 1;
        Q.free_all_sets();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        String prefixPerson = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        String user = prefixPerson.concat(SessionUserInfo.name);

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, keyWordStr, fromClassObj, LinkObj, Q, sis_session);

        //if (keyWordStr.equals(ConstantParameters.tc_kwd)) {
        //    Utils.StaticClass.webAppSystemOutPrintln("DEBUG");
        //}
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " Links in " + mergedThesaurusName);

        linkRelations = 0;
        int howmany = term_Links_HASH.size();
        Iterator<String> pairsEnumMerged = term_Links_HASH.keySet().iterator();

        while (pairsEnumMerged.hasNext()) {

            if (linkRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + keyWordStr + " counter: " + linkRelations + " of " + howmany + "   ");
                    //Utils.StaticClass.webAppSystemOutPrintln("RESTARTING SERVER");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            linkRelations++;

            String term = pairsEnumMerged.next();
            ArrayList<String> linkValues = new ArrayList<String>();
            ArrayList<String> tempVals = new ArrayList<String>();
            tempVals.addAll(term_Links_HASH.get(term));
            for (String tmp : tempVals) {
                if (linkValues.contains(tmp) == false) {
                    linkValues.add(tmp);
                }
            }

            if (linkValues != null && linkValues.size() > 0) {

                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, keyWordStr, linkValues, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                Q.free_all_sets();
            }

            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + linkRelations + ". Failed to add values: " + LinkObj.getValue() + " : " + linkValues.toString() + " in term: '" + term + "'." + resultObj.getValue() + ".");
                return false;
            } else {
                //logFileWriter.append(linkRelations + ". Term: '" + term + "' was successfully updated - modified. The following values were added: " + LinkObj.getValue() + " : " + linkValues.toString() + "\r\n");
            }
            //logFileWriter.flush();
            resultObj.setValue("");

        }

        logFileWriter.flush();
        return true;

    }

    
    public String getDefaultFacet(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String mergedThesaurusName) {
        
        return getDefaultFacetSortItem(refSessionUserInfo, Q, sis_session, mergedThesaurusName).getLogName();        
    }

    public SortItem getDefaultFacetSortItem(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String mergedThesaurusName) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject mergedUnclassifiedObj = new StringObject(prefix_class.concat(Parameters.UnclassifiedTermsLogicalname));
        Q.reset_name_scope();
        Q.set_current_node(mergedUnclassifiedObj);
        int set_classes = Q.get_superclasses(0);
        Q.reset_set(set_classes);

        int index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        int set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_facets);
        Q.set_intersect(set_classes, set_facets);
        Q.reset_set(set_classes);

        ArrayList<SortItem> defaultClassName = new ArrayList<SortItem>();
        defaultClassName.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_classes, true, Q, sis_session));
        Q.free_set(set_classes);
        Q.free_set(set_facets);

        return defaultClassName.get(0);
    }
    
    public int getStatusPriority(String status, String thesaurus) {

        String genericStatus = status.replaceFirst(thesaurus, "");
        if (genericStatus.compareTo("StatusForInsertion") == 0) {
            return 1;
        } else if (genericStatus.compareTo("StatusUnderConstruction") == 0) {
            return 2;
        } else if (genericStatus.compareTo("StatusForApproval") == 0) {
            return 3;
        }
        return 4; //released status

    }

    private ArrayList<String> CollectErrorProneUFTranslations(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2, OutputStreamWriter logFileWriter) throws IOException {
        /*
         * case 1: ens of thes 1 <-> uk_ufs of thes1
         * case 2: ens of thes 2 <-> uk_ufs of thes2
         * case 3: ens of thes 1 <-> uk_ufs of thes2
         * case 4: ens of thes 2 <-> uk_ufs of thes1
         */
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> ensThes1 = new ArrayList<String>();
        ArrayList<String> uk_ufsThes1 = new ArrayList<String>();
        ArrayList<String> ensThes2 = new ArrayList<String>();
        ArrayList<String> uk_ufsThes2 = new ArrayList<String>();

        Utilities u = new Utilities();
        UsersClass wtmsUsers = new UsersClass();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        StringObject uk_uffromClassObj = new StringObject();
        StringObject uk_ufLinkObj = new StringObject();

        StringObject enfromClassObj = new StringObject();
        StringObject enLinkObj = new StringObject();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, uk_uffromClassObj, uk_ufLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, enfromClassObj, enLinkObj, Q, sis_session);
        String prefixEN = dbtr.getThesaurusPrefix_EnglishWord(Q, sis_session.getValue());

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] TermClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses);
        int set_terms = dbGen.get_Instances_Set(TermClasses, Q, sis_session);
        Q.reset_set(set_terms);

        int set_en_links = Q.get_link_from_by_category(set_terms, enfromClassObj, enLinkObj);
        Q.reset_set(set_en_links);
        int set_ens = Q.get_to_value(set_en_links);
        Q.reset_set(set_ens);

        if (thesaurusName2 != null) {
            ensThes1.addAll(dbGen.get_Node_Names_Of_Set(set_ens, true, Q, sis_session));
        }

        int set_uk_ufs_links = Q.get_link_from_by_category(set_terms, uk_uffromClassObj, uk_ufLinkObj);
        Q.reset_set(set_uk_ufs_links);
        int set_uk_ufs = Q.get_to_value(set_uk_ufs_links);
        Q.reset_set(set_uk_ufs);

        if (thesaurusName2 != null) {
            uk_ufsThes1.addAll(dbGen.get_Node_Names_Of_Set(set_uk_ufs, true, Q, sis_session));
        }

        Q.set_intersect(set_uk_ufs, set_ens);
        Q.reset_set(set_uk_ufs);

        //case : 1
        results.addAll(dbGen.get_Node_Names_Of_Set(set_uk_ufs, true, Q, sis_session));

        for (int i = 0; i < results.size(); i++) {
            Q.reset_name_scope();
            StringObject errorUFTranslations = new StringObject(prefixEN.concat(results.get(i)));
            Q.set_current_node(errorUFTranslations);

            int set_WrongUFTranslationssOfThes1_labels = Q.get_link_to_by_category(0, uk_uffromClassObj, uk_ufLinkObj);
            Q.reset_set(set_WrongUFTranslationssOfThes1_labels);

            int set_WrongUFTranslationsOfThes1 = Q.get_from_value(set_WrongUFTranslationssOfThes1_labels);
            Q.reset_set(set_WrongUFTranslationsOfThes1);

            ArrayList<String> referencesToNode = new ArrayList<String>();
            referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes1, true, Q, sis_session));
            Q.free_set(set_WrongUFTranslationsOfThes1);
            Q.free_set(set_WrongUFTranslationssOfThes1_labels);

            for (int k = 0; k < referencesToNode.size(); k++) {
                logFileWriter.append("\r\n<targetTerm>");
                logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                logFileWriter.append("<errorValue>" + Utilities.escapeXML(results.get(i)) + "</errorValue>");
                
                logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFTrLink", new String[] { Utilities.escapeXML(results.get(i)),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName1, thesaurusName1})+"</reason>");                
                //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(results.get(i)) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + " from thesaurus: " + thesaurusName1 + ". This name is already used as a translation of thesaurus " + thesaurusName1 + " and cannot be used as a UF term in the new thesaurus.</reason>");                
                logFileWriter.append("</targetTerm>\r\n");
            }
        }

        Q.free_all_sets();
        Q.reset_name_scope();

        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, uk_uffromClassObj, uk_ufLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, enfromClassObj, enLinkObj, Q, sis_session);

            index = Parameters.CLASS_SET.indexOf("TERM");
            String[] TermClasses2 = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses2);
            set_terms = dbGen.get_Instances_Set(TermClasses2, Q, sis_session);
            Q.reset_set(set_terms);

            set_en_links = Q.get_link_from_by_category(set_terms, enfromClassObj, enLinkObj);
            Q.reset_set(set_en_links);
            set_ens = Q.get_to_value(set_en_links);
            Q.reset_set(set_ens);

            ensThes2.addAll(dbGen.get_Node_Names_Of_Set(set_ens, true, Q, sis_session));

            set_uk_ufs_links = Q.get_link_from_by_category(set_terms, uk_uffromClassObj, uk_ufLinkObj);
            Q.reset_set(set_uk_ufs_links);
            set_uk_ufs = Q.get_to_value(set_uk_ufs_links);
            Q.reset_set(set_uk_ufs);

            uk_ufsThes2.addAll(dbGen.get_Node_Names_Of_Set(set_uk_ufs, true, Q, sis_session));

            Q.set_intersect(set_uk_ufs, set_ens);
            Q.reset_set(set_uk_ufs);

            ArrayList<String> results2 = new ArrayList<String>();
            results2.addAll(dbGen.get_Node_Names_Of_Set(set_uk_ufs, true, Q, sis_session));

            //case : 2
            for (int i = 0; i < results2.size(); i++) {
                Q.reset_name_scope();
                StringObject errorUFTranslations = new StringObject(prefixEN.concat(results2.get(i)));
                Q.set_current_node(errorUFTranslations);

                int set_WrongUFTranslationsOfThes2_labels = Q.get_link_to_by_category(0, uk_uffromClassObj, uk_ufLinkObj);
                Q.reset_set(set_WrongUFTranslationsOfThes2_labels);

                int set_WrongUFTranslationsOfThes2 = Q.get_from_value(set_WrongUFTranslationsOfThes2_labels);
                Q.reset_set(set_WrongUFTranslationsOfThes2);

                ArrayList<String> referencesToNode = new ArrayList<String>();
                referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes2, true, Q, sis_session));
                Q.free_set(set_WrongUFTranslationsOfThes2);
                Q.free_set(set_WrongUFTranslationsOfThes2_labels);

                for (int k = 0; k < referencesToNode.size(); k++) {
                    logFileWriter.append("\r\n<targetTerm>");
                    logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                    logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(results2.get(i)) + "</errorValue>");
                    logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFTrLink", new String[] { Utilities.escapeXML(results2.get(i)),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName2, thesaurusName2})+"</reason>");                
                    //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(results2.get(i)) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + " from thesaurus: " + thesaurusName2 + ". This name is already used as a translation of thesaurus " + thesaurusName2 + " and cannot be used as a UF term in the new thesaurus.</reason>");                
                    logFileWriter.append("</targetTerm>\r\n");
                }

                if (results.contains(results2.get(i))) {
                    continue;
                } else {
                    results.add(results2.get(i));
                }
            }
            Q.free_all_sets();
            Q.reset_name_scope();

            //case : 3
            for (int i = 0; i < uk_ufsThes2.size(); i++) {
                String SearchKwd = uk_ufsThes2.get(i);
                if (ensThes1.contains(SearchKwd)) {

                    Q.reset_name_scope();
                    StringObject errorUFTranslations = new StringObject(prefixEN.concat(SearchKwd));
                    Q.set_current_node(errorUFTranslations);

                    int set_WrongUFTranslationsOfThes2_labels = Q.get_link_to_by_category(0, uk_uffromClassObj, uk_ufLinkObj);
                    Q.reset_set(set_WrongUFTranslationsOfThes2_labels);

                    int set_WrongUFTranslationsOfThes2 = Q.get_from_value(set_WrongUFTranslationsOfThes2_labels);
                    Q.reset_set(set_WrongUFTranslationsOfThes2);

                    ArrayList<String> referencesToNode = new ArrayList<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes2, true, Q, sis_session));
                    Q.free_set(set_WrongUFTranslationsOfThes2);
                    Q.free_set(set_WrongUFTranslationsOfThes2_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFTrLink", new String[] { Utilities.escapeXML(SearchKwd),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName2, thesaurusName1})+"</reason>");                
                        //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(SearchKwd) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + " from thesaurus: " + thesaurusName2 + ". This name is already used as a translation of thesaurus " + thesaurusName1 + " and cannot be used as a UF (Tra.) term in the new thesaurus.</reason>");                
                        logFileWriter.append("</targetTerm>\r\n");
                    }
                    if (results.contains(SearchKwd) == false) {
                        results.add(SearchKwd);
                    }
                }
            }

            //case : 4
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, uk_uffromClassObj, uk_ufLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, enfromClassObj, enLinkObj, Q, sis_session);

            for (int i = 0; i < uk_ufsThes1.size(); i++) {
                String SearchKwd = uk_ufsThes1.get(i);
                if (ensThes2.contains(SearchKwd)) {

                    Q.reset_name_scope();
                    StringObject errorUF = new StringObject(prefixEN.concat(SearchKwd));
                    Q.set_current_node(errorUF);

                    int set_WrongUFTranslationsOfThes1_labels = Q.get_link_to_by_category(0, uk_uffromClassObj, uk_ufLinkObj);
                    Q.reset_set(set_WrongUFTranslationsOfThes1_labels);

                    int set_WrongUFTranslationsOfThes1 = Q.get_from_value(set_WrongUFTranslationsOfThes1_labels);
                    Q.reset_set(set_WrongUFTranslationsOfThes1);

                    ArrayList<String> referencesToNode = new ArrayList<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes1, true, Q, sis_session));
                    Q.free_set(set_WrongUFTranslationsOfThes1);
                    Q.free_set(set_WrongUFTranslationsOfThes1_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFTrLink", new String[] { Utilities.escapeXML(SearchKwd),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName1, thesaurusName2})+"</reason>");                
                        //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(SearchKwd) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + " from thesaurus: " + thesaurusName1 + ". This name is already used as a translation of thesaurus " + thesaurusName2 + " and cannot be used as a UF (Tra.) term in the new thesaurus.</reason>");                
                        logFileWriter.append("</targetTerm>\r\n");
                    }

                    if (results.contains(SearchKwd) == false) {
                        results.add(SearchKwd);
                    }
                }
            }
        }

        return results;
    }

    private ArrayList<String> CollectErrorProneUfs(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2, OutputStreamWriter logFileWriter) throws IOException {

        /*
         * case 1: terms of thes 1 <-> ufs of thes1
         * case 2: terms of thes 2 <-> ufs of thes2
         * case 3: terms of thes 1 <-> ufs of thes2
         * case 4: terms of thes 2 <-> ufs of thes1
         */
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> termsThes1 = new ArrayList<String>();
        ArrayList<String> ufsThes1 = new ArrayList<String>();
        ArrayList<String> termsThes2 = new ArrayList<String>();
        ArrayList<String> ufsThes2 = new ArrayList<String>();

        UsersClass wtmsUsers = new UsersClass();
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        StringObject UFClass = new StringObject();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_kwd, fromClassObj, LinkObj, Q, sis_session);
        dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), UFClass);

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] TermClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses);
        int set_terms = dbGen.get_Instances_Set(TermClasses, Q, sis_session);
        Q.reset_set(set_terms);

        if (thesaurusName2 != null) {
            termsThes1.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));
        }

        Q.reset_name_scope();
        Q.set_current_node(UFClass);
        int set_report_errors = Q.get_instances(0);
        Q.reset_set(set_report_errors);

        if (thesaurusName2 != null) {
            ufsThes1.addAll(dbGen.get_Node_Names_Of_Set(set_report_errors, true, Q, sis_session));
        }

        Q.set_intersect(set_report_errors, set_terms);
        Q.reset_set(set_report_errors);

        //case : 1
        results.addAll(dbGen.get_Node_Names_Of_Set(set_report_errors, true, Q, sis_session));
        String prefixThes1 = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        for (int i = 0; i < results.size(); i++) {
            Q.reset_name_scope();
            StringObject errorUF = new StringObject(prefixThes1.concat(results.get(i)));
            Q.set_current_node(errorUF);

            int set_WrongUFsOfThes1_labels = Q.get_link_to_by_category(0, fromClassObj, LinkObj);
            Q.reset_set(set_WrongUFsOfThes1_labels);

            int set_WrongUFsOfThes1 = Q.get_from_value(set_WrongUFsOfThes1_labels);
            Q.reset_set(set_WrongUFsOfThes1);

            ArrayList<String> referencesToNode = new ArrayList<String>();
            referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes1, true, Q, sis_session));
            Q.free_set(set_WrongUFsOfThes1);
            Q.free_set(set_WrongUFsOfThes1_labels);

            for (int k = 0; k < referencesToNode.size(); k++) {
                logFileWriter.append("\r\n<targetTerm>");
                logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                logFileWriter.append("<errorValue>" + Utilities.escapeXML(results.get(i)) + "</errorValue>");
                logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFLink", new String[] { Utilities.escapeXML(results.get(i)),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName1})+"</reason>");                
                //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(results.get(i)) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + ". This name is already used as a term of thesaurus " + thesaurusName1 + " and cannot be used as a non-preferred term in the new thesaurus.</reason>");                
                logFileWriter.append("</targetTerm>\r\n");
            }
        }

        Q.free_all_sets();
        Q.reset_name_scope();

        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            String prefixThes2 = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_kwd, fromClassObj, LinkObj, Q, sis_session);
            dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), UFClass);

            index = Parameters.CLASS_SET.indexOf("TERM");
            String[] TermClasses2 = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses2);
            set_terms = dbGen.get_Instances_Set(TermClasses2, Q, sis_session);
            Q.reset_set(set_terms);

            termsThes2.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));

            Q.reset_name_scope();
            Q.set_current_node(UFClass);
            set_report_errors = Q.get_instances(0);
            Q.reset_set(set_report_errors);

            ufsThes2.addAll(dbGen.get_Node_Names_Of_Set(set_report_errors, true, Q, sis_session));

            Q.set_intersect(set_report_errors, set_terms);
            Q.reset_set(set_report_errors);

            ArrayList<String> results2 = new ArrayList<String>();
            results2.addAll(dbGen.get_Node_Names_Of_Set(set_report_errors, true, Q, sis_session));

            //case : 2
            for (int i = 0; i < results2.size(); i++) {

                Q.reset_name_scope();
                StringObject errorUF = new StringObject(prefixThes2.concat(results2.get(i)));
                Q.set_current_node(errorUF);

                int set_WrongUFsOfThes2_labels = Q.get_link_to_by_category(0, fromClassObj, LinkObj);
                Q.reset_set(set_WrongUFsOfThes2_labels);

                int set_WrongUFsOfThes2 = Q.get_from_value(set_WrongUFsOfThes2_labels);
                Q.reset_set(set_WrongUFsOfThes2);

                ArrayList<String> referencesToNode = new ArrayList<String>();
                referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes2, true, Q, sis_session));
                Q.free_set(set_WrongUFsOfThes2);
                Q.free_set(set_WrongUFsOfThes2_labels);

                for (int k = 0; k < referencesToNode.size(); k++) {
                    logFileWriter.append("\r\n<targetTerm>");
                    logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                    logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(results2.get(i)) + "</errorValue>");
                    logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFLink", new String[] { Utilities.escapeXML(results2.get(i)),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName2})+"</reason>");                
                    //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(results2.get(i)) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + ". This name is already used as a term of thesaurus " + thesaurusName2 + " and cannot be used as a non-preferred term in the new thesaurus.</reason>");                
                    logFileWriter.append("</targetTerm>\r\n");
                }

                if (results.contains(results2.get(i))) {
                    continue;
                } else {
                    results.add(results2.get(i));
                }
            }

            Q.free_all_sets();
            Q.reset_name_scope();

            //case : 3
            for (int i = 0; i < ufsThes2.size(); i++) {
                String SearchKwd = ufsThes2.get(i);

                if (termsThes1.contains(SearchKwd)) {

                    Q.reset_name_scope();
                    StringObject errorUF = new StringObject(prefixThes2.concat(SearchKwd));
                    Q.set_current_node(errorUF);

                    int set_WrongUFsOfThes2_labels = Q.get_link_to_by_category(0, fromClassObj, LinkObj);
                    Q.reset_set(set_WrongUFsOfThes2_labels);

                    int set_WrongUFsOfThes2 = Q.get_from_value(set_WrongUFsOfThes2_labels);
                    Q.reset_set(set_WrongUFsOfThes2);

                    ArrayList<String> referencesToNode = new ArrayList<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes2, true, Q, sis_session));
                    Q.free_set(set_WrongUFsOfThes2);
                    Q.free_set(set_WrongUFsOfThes2_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFLink", new String[] { Utilities.escapeXML(SearchKwd),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName1})+"</reason>");                
                        //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(SearchKwd) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + ". This name is already used as a term of thesaurus " + thesaurusName1 + " and cannot be used as a non-preferred term in the new thesaurus.</reason>");                
                        logFileWriter.append("</targetTerm>\r\n");
                    }
                    if (results.contains(SearchKwd) == false) {
                        results.add(SearchKwd);
                    }

                }
            }

            //case : 4
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_kwd, fromClassObj, LinkObj, Q, sis_session);

            for (int i = 0; i < ufsThes1.size(); i++) {
                String SearchKwd = ufsThes1.get(i);

                if (termsThes2.contains(SearchKwd)) {

                    Q.reset_name_scope();
                    StringObject errorUF = new StringObject(prefixThes1.concat(SearchKwd));
                    Q.set_current_node(errorUF);

                    int set_WrongUFsOfThes1_labels = Q.get_link_to_by_category(0, fromClassObj, LinkObj);
                    Q.reset_set(set_WrongUFsOfThes1_labels);

                    int set_WrongUFsOfThes1 = Q.get_from_value(set_WrongUFsOfThes1_labels);
                    Q.reset_set(set_WrongUFsOfThes1);

                    ArrayList<String> referencesToNode = new ArrayList<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes1, true, Q, sis_session));
                    Q.free_set(set_WrongUFsOfThes1);
                    Q.free_set(set_WrongUFsOfThes1_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>"+u.translateFromMessagesXML("root/MergeThesauri/SkipUFLink", new String[] { Utilities.escapeXML(SearchKwd),Utilities.escapeXML(referencesToNode.get(k)), thesaurusName2})+"</reason>");                
                        //logFileWriter.append("<reason>Skipping copying of UF link: '" + Utilities.escapeXML(SearchKwd) + "' for term: " + Utilities.escapeXML(referencesToNode.get(k)) + ". This name is already used as a term of thesaurus " + thesaurusName2 + " and cannot be used as a non-preferred term in the new thesaurus.</reason>");                
                        logFileWriter.append("</targetTerm>\r\n");
                    }

                    if (results.contains(SearchKwd) == false) {
                        results.add(SearchKwd);
                    }
                }
            }

        }

        return results;
    }
    
    public HashMap<SortItem, ArrayList<SortItem>> ReadThesaurusHierarchiesInSortItems(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        if (thesaurusName1 == null) { // supporting copy operation
            return new HashMap<SortItem, ArrayList<SortItem>>();
        }
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        Q.reset_name_scope();
        //Find out hierarchy Classes and get their instances
        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);
        int set_hierarchies = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
        Q.reset_set(set_hierarchies);

        ArrayList<SortItem> hierarchyNames = new ArrayList<SortItem>();
        
        // in order to get the the top terms (that contain the reference Id) one should follow the belongs_to_%thesaurus%_hierarchy link
        if(Parameters.OnlyTopTermsHoldReferenceId){
            
            StringObject belongsToHierarchyClass = new StringObject();
            StringObject belongsToHierarchyLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierarchyClass, belongsToHierarchyLink, Q, sis_session);
            set_hierarchies = Q.get_from_node_by_category(set_hierarchies, belongsToHierarchyClass, belongsToHierarchyLink);            
        }
        hierarchyNames.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_hierarchies, true, Q, sis_session));
        Q.free_set(set_hierarchies);

        //Create a set with all facets used as a filter to the get all classes call taht will follow
        Q.reset_name_scope();
        //Find out facet Classes and get their instances
        index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        int set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_facets);

        HashMap<SortItem, ArrayList<SortItem>> thesaurus_hierarchy_Facets_Relations = new HashMap<SortItem, ArrayList<SortItem>>();

        //System.out.println("set_facets count: " + Q.set_get_card(set_facets));
        for (SortItem hierarchySortItem :  hierarchyNames) {
            StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchySortItem.getLogName()));
            ArrayList<SortItem> hierarchyFacets = new ArrayList<SortItem>();
            Q.reset_name_scope();
            if (Q.set_current_node(hierarchyObj) != QClass.APIFail) {
                int set_hierarchy_classes = Q.get_superclasses(0);

                Q.reset_set(set_hierarchy_classes);
                Q.set_intersect(set_hierarchy_classes, set_facets);
                Q.reset_set(set_hierarchy_classes);
                hierarchyFacets.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_hierarchy_classes, true, Q, sis_session));
                Q.free_set(set_hierarchy_classes);
            }

            thesaurus_hierarchy_Facets_Relations.put(hierarchySortItem, hierarchyFacets);
        }
        //thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));

        Q.free_set(set_facets);

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            throw new UnsupportedOperationException("Merging is not yes supported - need to log URI changes");
            /*
            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

            Q.reset_name_scope();
            //Find out hierarchy Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("HIERARCHY");
            HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);
            set_hierarchies = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
            Q.reset_set(set_hierarchies);

            ArrayList<SortItem> hierarchyNamesOfThes2 = new ArrayList<SortItem>();
            
            // in order to get the the top terms (that contain the reference Id) one should follow the belongs_to_%thesaurus%_hierarchy link
            if(Parameters.OnlyTopTermsHoldReferenceId){

                StringObject belongsToHierarchyClass = new StringObject();
                StringObject belongsToHierarchyLink = new StringObject();
                dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierarchyClass, belongsToHierarchyLink, Q, sis_session);
                set_hierarchies = Q.get_from_node_by_category(set_hierarchies, belongsToHierarchyClass, belongsToHierarchyLink);            
            }
            hierarchyNamesOfThes2.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_hierarchies, true, Q, sis_session));
            Q.free_set(set_hierarchies);

            //Create a set with all facets used as a filter to the get all classes call taht will follow
            Q.reset_name_scope();
            //Find out facet Classes and get their instances
            index = Parameters.CLASS_SET.indexOf("FACET");
            FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
            set_facets = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);

            Q.reset_set(set_facets);

            HashMap<SortItem, ArrayList<SortItem>> thesaurus_hierarchy_Facets_RelationsOfThes2 = new HashMap<SortItem, ArrayList<SortItem>>();

            for (SortItem hierarchyObj2 : hierarchyNamesOfThes2) {
                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyObj2.getLogName()));
                ArrayList<SortItem> hierarchyFacets = new ArrayList<SortItem>();
                Q.reset_name_scope();
                if (Q.set_current_node(hierarchyObj) != QClass.APIFail) {
                    int set_hierarchy_classes = Q.get_superclasses(0);

                    Q.reset_set(set_hierarchy_classes);
                    Q.set_intersect(set_hierarchy_classes, set_facets);
                    Q.reset_set(set_hierarchy_classes);
                    hierarchyFacets.addAll(dbGen.get_Node_Names_Of_Set_In_SortItems(set_hierarchy_classes, true, Q, sis_session));

                    Q.free_set(set_hierarchy_classes);
                }

                thesaurus_hierarchy_Facets_RelationsOfThes2.put(hierarchyObj2, hierarchyFacets);
            }
            //thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));

            Q.free_set(set_facets);

            //merge the two thesauri
            Enumeration<SortItem> secondThesHiers = thesaurus_hierarchy_Facets_RelationsOfThes2.keys();
            while (secondThesHiers.hasMoreElements()) {
                SortItem targetHier = secondThesHiers.nextElement();
                ArrayList<SortItem> targetHierFacets = thesaurus_hierarchy_Facets_RelationsOfThes2.get(targetHier);

                if (thesaurus_hierarchy_Facets_Relations.containsKey(targetHier)) {

                    ArrayList<SortItem> existingFacets = thesaurus_hierarchy_Facets_Relations.get(targetHier);

                    for (SortItem  checkFacet : targetHierFacets) {
                        if (existingFacets.contains(checkFacet) == false) {
                            existingFacets.add(checkFacet);
                        }
                    }
                    thesaurus_hierarchy_Facets_Relations.put(targetHier, existingFacets);

                } else {
                    thesaurus_hierarchy_Facets_Relations.put(targetHier, targetHierFacets);
                }
            }
            */
        }
        return thesaurus_hierarchy_Facets_Relations;
    }
	
    
    public CMValue getDefaultUnclassifiedHierarchyCmv(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String mergedThesaurusName) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        UsersClass wtmsUsers = new UsersClass();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject mergedUnclassifiedObj = new StringObject(prefix_class.concat(Parameters.UnclassifiedTermsLogicalname));
        Q.reset_name_scope();
        CMValue returnCmv = new CMValue();
        Q.set_current_node_and_retrieve_Cmv(mergedUnclassifiedObj, returnCmv);
        Q.reset_name_scope();
        
        return returnCmv;
    }
    
    public SortItem getDefaultUnclassifiedTopTermSortItem(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String mergedThesaurusName) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        UsersClass wtmsUsers = new UsersClass();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        String prefix_descriptor = dbtr.getThesaurusPrefix_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject mergedUnclassifiedObj = new StringObject(prefix_descriptor.concat(Parameters.UnclassifiedTermsLogicalname));
        Q.reset_name_scope();
        CMValue returnCmv = new CMValue();
        Q.set_current_node_and_retrieve_Cmv(mergedUnclassifiedObj, returnCmv);
        Q.reset_name_scope();
        
        return new SortItem(returnCmv);
    }
    
    
}