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
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Locale;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import javax.servlet.ServletContext;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class DBMergeThesauri {

    public static final int restartInterval = 1000;

    public DBMergeThesauri() {
    }

    public boolean CreateThesaurus(DBGeneral dbGen, ConfigDBadmin config, CommonUtilsDBadmin common_utils,
            String mergedThesaurusName, String mergedThesaurusNameDBformatted,
            Vector<String> thesauriNames, StringObject CreateThesaurusResultMessage,
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
                CreateThesaurusSucceded = dbAdminUtils.CreateThesaurus(common_utils, mergedThesaurusNameDBformatted, CreateThesaurusResultMessage, backUpDescription, DBbackupFileNameCreated);
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
        Vector<String> allGuideTerms = new Vector<String>(); //Filled in step: 1
        Hashtable<String, Vector<SortItem>> guideTermsRelations = new Hashtable<String, Vector<SortItem>>();//filled in step: 2

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
        Vector<String> bts = new Vector<String>();
        Vector<SortItem> nts = new Vector<SortItem>();

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
        Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
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
                Vector<SortItem> newEntry = new Vector<SortItem>();
                newEntry.add(targetSortItem);
                guideTermsRelations.put(targetTerm, newEntry);
                if (allGuideTerms.contains(guideTerm) == false) {
                    allGuideTerms.add(guideTerm);
                }
            } else {
                Vector<SortItem> currentValues = guideTermsRelations.get(targetTerm);
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
                                + "<reason>Για τον όρο: '" + Utilities.escapeXML(targetTerm) + "' έχει ήδη ορισθεί σχέση ΕΟ με τον όρο '" + Utilities.escapeXML(targetSortItem.getLogName())
                                + "' και ετικέτα δεσμού '" + valueThatWillBeKept + "'. Ανιχνεύθηκε ωστόσο μεταξύ τους και σχέση ΕΟ με ετικέτα δεσμού '" + Utilities.escapeXML(valueThatWillBeIgnored)
                                + "' η οποία παρακάμφθηκε.</reason>"
                                + "</targetTerm>/r/n");
                    }

                }
            }
        }

        return CreateGuideTerms(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session,
                allGuideTerms, guideTermsRelations, mergedThesaurusName, resultObj);

    }

    public int indexOfSortItemLogNameInVector(Vector<SortItem> currentValues, SortItem testItem) {

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
            Vector<String> allGuideTerms, Hashtable<String, Vector<SortItem>> guideTermsRelations,
            String mergedThesaurusName, StringObject resultObj) {

        UsersClass wtmsUsers = new UsersClass();

        String pathToMessagesXML = Utilities.getMessagesXml();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        Q.free_all_sets();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αρχή δημιουργίας ετικετών δεσμού. Ώρα: " + Utilities.GetNow());
        DBEditGuideTerms dbEdit_Guide_Terms = new DBEditGuideTerms();
        int howmanyGts = allGuideTerms.size();
        for (int i = 0; i < howmanyGts; i++) {

            if (i % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "GT creation counter: " + (i + 1) + " of " + howmanyGts + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }

            if (dbEdit_Guide_Terms.addGuideTerm(SessionUserInfo.selectedThesaurus, Q, sis_session, allGuideTerms.get(i), resultObj, pathToMessagesXML) == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αποτυχία δημιουργίας της ετικέτας δεσμού: " + allGuideTerms.get(i) + ".\r\n" + resultObj.getValue());
                return false;
            }
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Τέλος δημιουργίας ετικετών δεσμού.");

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αρχή ενημέρωσης σχέσεων με ετικέτες δεσμού.");

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
        int linkRelations = 0;
        int homanyterms = guideTermsRelations.size();
        Enumeration<String> XMLguideTermsEnum = guideTermsRelations.keys();
        while (XMLguideTermsEnum.hasMoreElements()) {
            if (linkRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Terms gt update counter: " + linkRelations + " of " + homanyterms + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            linkRelations++;
            String targetTerm = XMLguideTermsEnum.nextElement();
            Vector<SortItem> targetTermNtsforChange = new Vector<SortItem>();
            targetTermNtsforChange.addAll(guideTermsRelations.get(targetTerm));

            //additional structures in order to reuse code for term editing
            Vector<String> ntsDecodedValues = new Vector<String>();
            Vector<String> GuideTermsDecodedValues = new Vector<String>();
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
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αποτυχία προσθήκης στον όρο" + targetTerm + " των ετικετών δεσμού " + GuideTermsDecodedValues.toString() + " για τους ΕΟ " + ntsDecodedValues.toString() + ".\r\n" + resultObj.getValue());
                return false;
            }
            //links++;
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Τέλος ενημέρωσης σχέσεων με ετικέτες δεσμού.");
        return true;
    }

    public boolean CopyFacets(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName, DBGeneral dbGen,
            StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        //THEMASUserInfo SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        //Get Facets of Thesaurus 1
        Vector<String> facets_thes1 = new Vector<String>();
        facets_thes1.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, thesaurusName1, null));

        //Get Facets of Thesaurus 2
        Vector<String> facets_thes2 = new Vector<String>();
        if (thesaurusName2 != null) { // in oredr to support copy mode -> no second name is given
            facets_thes2.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, thesaurusName2, null));
        }

        //Get All Facets of New merged thesaurus --> ΘΈΜΑ ΚΟΡΥΦΗΣ 
        Vector<String> mergedFacetNames = new Vector<String>();
        mergedFacetNames.addAll(ReadThesaurusFacets(refSessionUserInfo, Q, sis_session, mergedThesaurusName, null));

        //prepare return Vector which will contain only Facets not included in Merged Thesaurus -- duplicate elimination
        Vector<String> merged_thesaurus_NEW_facets = new Vector<String>();
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

    public boolean CreateFacets(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, Vector<String> merged_thesaurus_NEW_facets, StringObject resultObj) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αρχή δημιουργίας μικροθησαυρών. Ώρα: " + Utilities.GetNow());
        String pathToMessagesXML = Utilities.getMessagesXml();
        DBGeneral dbGen = new DBGeneral();

        DBCreate_Modify_Facet creationModificationOfFacet = new DBCreate_Modify_Facet();

        boolean FacetAdditionSucceded = true;

        for (int i = 0; i < merged_thesaurus_NEW_facets.size(); i++) {
            Q.reset_name_scope();
            FacetAdditionSucceded = creationModificationOfFacet.Create_Or_ModifyFacet(selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, merged_thesaurus_NEW_facets.get(i), "create", null, resultObj, false, pathToMessagesXML);

            if (FacetAdditionSucceded == false) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αποτυχία δημιουργίας μικροθησαυρών: " + resultObj.getValue() + ".");
                return false;
            }
            resultObj.setValue("");
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Τέλος δημιουργίας μικροθησαυρών.");
        return true;
    }

    public boolean CopyHierarchies(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String thesaurusName1, String thesaurusName2, String mergeThesaurus, DBGeneral dbGen,
            String defaultFacet, Locale targetLocale, StringObject resultObj, OutputStreamWriter logFileWriter) throws IOException {

        //Start Hierarchies merging
        //if a hierarchy is not under at least one facet (in both thesauri) add it to defaultFacetObj
        //read hiers of thesaurus 1 and thesaurus 2
        Hashtable<String, Vector<String>> pairsOfThesaurus1 = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, thesaurusName1, null);
        Hashtable<String, Vector<String>> pairsOfThesaurus2 = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, thesaurusName2, null);

        //read hiers of  merged thesaurus
        Hashtable<String, Vector<String>> pairsOfMergedThesaurus = ReadThesaurusHierarchies(refSessionUserInfo, Q, sis_session, mergeThesaurus, null);

        //add hiers of thesaurus 1 to merged thesaurus structure
        Enumeration<String> pairsEnum1 = pairsOfThesaurus1.keys();
        while (pairsEnum1.hasMoreElements()) {
            String hierarchy = pairsEnum1.nextElement();
            Vector<String> thes1Vals = pairsOfThesaurus1.get(hierarchy);

            if (pairsOfMergedThesaurus.containsKey(hierarchy) == false) {
                pairsOfMergedThesaurus.put(hierarchy, thes1Vals);
            } else {
                Vector<String> mergedVals = pairsOfMergedThesaurus.get(hierarchy);

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
            Enumeration<String> pairsEnum2 = pairsOfThesaurus2.keys();
            while (pairsEnum2.hasMoreElements()) {
                String hierarchy = pairsEnum2.nextElement();
                Vector<String> thes2Vals = pairsOfThesaurus2.get(hierarchy);

                if (pairsOfMergedThesaurus.containsKey(hierarchy) == false) {
                    pairsOfMergedThesaurus.put(hierarchy, thes2Vals);
                } else {
                    Vector<String> mergedVals = pairsOfMergedThesaurus.get(hierarchy);

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
            Hashtable<String, Vector<String>> pairsOfMergedThesaurus) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αρχή δημιουργίας ιεραρχιών. Ώρα: " + Utilities.GetNow());
        boolean HierarchiesSucceeded = true;
        try {

            String pathToMessagesXML = Utilities.getMessagesXml();
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
            Vector<String> errorArgs = new Vector<String>();

            //Now for each pair of hierarchyplus facets create hierarchy and then change facets. If no facet is declared add hier under default facet
            Enumeration<String> pairsEnumMerged = pairsOfMergedThesaurus.keys();
            while (pairsEnumMerged.hasMoreElements()) {
                String hierarchy = pairsEnumMerged.nextElement();
                Vector<String> underFacets = pairsOfMergedThesaurus.get(hierarchy);
                if (underFacets.size() == 0) {

                    errorArgs.add(Utilities.escapeXML(hierarchy));
                    errorArgs.add(Utilities.escapeXML(defaultFacet));
                    dbGen.Translate(resultMessageObj, "root/CreateHierarchies/WrongHierarchyPosition", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();

                    logFileWriter.append("\r\n<targetHierarchy><name>" + Utilities.escapeXML(hierarchy) + "</name><errorType>facet</errorType><errorValue>" + Utilities.escapeXML(defaultFacet) + "</errorValue>");
                    logFileWriter.append("<reason>" + resultMessageObj.getValue() + "</reason>");
                    //logFileWriter.append("<reason>Η ιεραρχία " + Utilities.escapeXML(hierarchy) + " βρέθηκε εσφαλμένα χωρίς να υπάγεται σε κανένα μικροθησαυρό. Υπαγωγή της στον μικροθησαυρό " + Utilities.escapeXML(defaultFacet) + ".</reason>");
                    logFileWriter.append("</targetHierarchy>\r\n");
                    underFacets.add(defaultFacet);
                }

                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchy));
                Q.reset_name_scope();

                if (Q.set_current_node(hierarchyObj) == QClass.APIFail) {
                    //create hierarchy
                    Q.reset_name_scope();
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "create", null, SessionUserInfo.name, targetLocale, resultObj, false, pathToMessagesXML);
                    //logFileWriter.append(resultObj.getValue()+"\r\n");
                    if (HierarchiesSucceeded == true && underFacets.size() > 1) {

                        resultObj.setValue("");
                        HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false, pathToMessagesXML);

                        //logFileWriter.append(resultObj.getValue()+"\r\n");
                    }
                } else {
                    //modify hierarchy
                    HierarchiesSucceeded = creationModificationOfHierarchy.Create_Or_ModifyHierarchy(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, hierarchy, underFacets, "modify", null, SessionUserInfo.name, targetLocale, resultObj, false, pathToMessagesXML);
                }

                if (HierarchiesSucceeded == false) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Αποτυχία δημιουργίας ιεραρχιών: " + resultObj.getValue());
                    break;
                } else {
                    resultObj.setValue("");
                }
            }

            if (HierarchiesSucceeded) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Τέλος δημιουργίας ιεραρχιών.");
            }
        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass().toString());
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return HierarchiesSucceeded;
    }

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
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής Όρων (και ΠΟ ).");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής Όρων (και ΠΟ ) σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής ΣΟ.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy/*, warnignsBuffer*/);
        }
        //</editor-fold>  

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Status">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής ΣΟ σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής κατάστασης όρων.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής κατάστασης όρων σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής ΔΣ, SN και ΙΣ.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής ΔΣ, SN και ΙΣ σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής ΑΟ.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής ΑΟ σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            Vector<String> errorProneUFs = new Vector<String>();
            errorProneUFs.addAll(CollectErrorProneUfs(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null, logFileWriter));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής ΧΑ.");

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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής ΧΑ σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            Vector<String> errorProneUFTranslations = new Vector<String>();

            if (Parameters.TermModificationChecks.contains(18)) {
                errorProneUFTranslations.addAll(CollectErrorProneUFTranslations(refSessionUserInfo, Q, sis_session, sourceThesaurusName, null, logFileWriter));
            }

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής UF.");

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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής UF σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής TK.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής TK σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής GS.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής GS σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής ES.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.translations_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Created By / ΟΝ ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής ES σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής Created BY / ON πεδίων.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName, null, mergedThesaurusName, logFileWriter, ConstantParameters.created_by_kwd, resultObj);
        }
        //</editor-fold> 

        if (common_utils != null) {
            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
        }

        //<editor-fold defaultstate="collapsed" desc="Modified By / ΟΝ ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής Created BY/ ON σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή αντιγραφής Modified BY / ON πεδίων.");
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
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος αντιγραφής Modified ΒΥ / ON σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
        }

        return keepCopying;

    }

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
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης Όρων (και ΠΟ ).");
        logFileWriter.flush();
        startTime = System.currentTimeMillis();
        keepCopying = CopyTermsLevelByLevel(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="RTs">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης Όρων (και ΠΟ ) σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης ΣΟ.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyRTs(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy/*, warnignsBuffer*/);
        }
        //</editor-fold>  

        //<editor-fold defaultstate="collapsed" desc="Status">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης ΣΟ σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης κατάστασης όρων.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyStatuses(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj);
        }
        //</editor-fold >  

        //<editor-fold defaultstate="collapsed" desc="CommentCategories">  
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης κατάστασης όρων σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης ΔΣ, SN και ΙΣ.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold>  

        //<editor-fold defaultstate="collapsed" desc="EN">   
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης ΔΣ, SN και ΙΣ σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης ΑΟ.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.translation_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="UF..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης ΑΟ σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            Vector<String> errorProneUFs = new Vector<String>();
            errorProneUFs.addAll(CollectErrorProneUfs(refSessionUserInfo, Q, sis_session, sourceThesaurusName1, sourceThesaurusName2, logFileWriter));
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης ΧΑ.");
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.uf_kwd, errorProneUFs, resultObj, ConsistencyCheckPolicy);

        }
        //</editor-fold>   

        //<editor-fold defaultstate="collapsed" desc="UK_UF..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης ΧΑ σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            Vector<String> errorProneUFTranslations = new Vector<String>();

            if (Parameters.TermModificationChecks.contains(18)) {
                errorProneUFTranslations.addAll(CollectErrorProneUFTranslations(refSessionUserInfo, Q, sis_session, sourceThesaurusName1, sourceThesaurusName2, logFileWriter));
            }
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης UF.");
            logFileWriter.flush();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.uf_translations_kwd, errorProneUFTranslations, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold >

        //<editor-fold defaultstate="collapsed" desc="TCs..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης UF σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης TK.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.tc_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Greek Source..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης TK σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης GS.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.primary_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="English Source..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης GS σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης ES");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopySimpleLinks(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.translations_found_in_kwd, null, resultObj, ConsistencyCheckPolicy);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Created By / ΟΝ ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης ES σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης Created BY / ON πεδίων.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.created_by_kwd, resultObj);
        }
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Modified By / ΟΝ ..."> 
        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης Created BY/ ON σε χρόνο " + elapsedTimeSec + " min.");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΑρχή συγχώνευσης Modified BY / ON πεδίων.");
            logFileWriter.flush();
            startTime = System.currentTimeMillis();
            keepCopying = CopyDatesAndEditors(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, pathToErrorsXML, sourceThesaurusName1, sourceThesaurusName2, mergedThesaurusName, logFileWriter, ConstantParameters.modified_by_kwd, resultObj);
        }
        //</editor-fold>

        if (keepCopying) {
            elapsedTimeMillis = System.currentTimeMillis() - startTime;
            elapsedTimeSec = (elapsedTimeMillis / 1000F) / 60;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\tΤέλος συγχώνευσης Modified ΒΥ / ON σε χρόνο " + elapsedTimeSec + " min.");
            logFileWriter.flush();
        }

        return keepCopying;
    }

    public Vector<String> ReadThesaurusGuideTerms(String selectedThesaurus, QClass Q, IntegerObject sis_session, String thesaurusName) {
        Vector<String> thesaurus_guideTerms = new Vector<String>();

        DBGeneral dbGen = new DBGeneral();

        thesaurus_guideTerms.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        return thesaurus_guideTerms;
    }

    public Vector<String> ReadThesaurusFacets(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        DBGeneral dbGen = new DBGeneral();

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

        Vector<String> thesaurus_facets = new Vector<String>();
        thesaurus_facets.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));
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

            Vector<String> thesaurus_facetsOfThes2 = new Vector<String>();
            thesaurus_facetsOfThes2.addAll(dbGen.get_Node_Names_Of_Set(set_facets, true, Q, sis_session));
            Q.free_set(set_facets);

            for (int k = 0; k < thesaurus_facetsOfThes2.size(); k++) {
                String checkFacet = thesaurus_facetsOfThes2.get(k);
                if (thesaurus_facets.contains(checkFacet) == false) {
                    thesaurus_facets.add(checkFacet);
                }
            }
        }
        return thesaurus_facets;
    }

    public void ReadTheasaurusTermCommentCategories(UserInfoClass refSessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            String thesaurusName1, String thesaurusName2, Hashtable<String, NodeInfoStringContainer> termsInfo) {
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

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
        int set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
        Q.reset_set(set_terms);

        Vector<String> termsOfThes1 = new Vector<String>();
        termsOfThes1.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName1 + ".");

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);

        //Enumeration<String> termEnum = termsInfo.keys();
        //while(termEnum.hasMoreElements()){
        //String targetTerm = termEnum.nextElement();
        IntegerObject sysIdObj = new IntegerObject();
        StringObject nodeNameObj = new StringObject();
        StringObject classObj = new StringObject();

        //SCOPE NOTES 
        Vector<String> terms_with_sn_Vec = new Vector<String>();

        Q.reset_name_scope();

        Q.set_current_node(scopenoteFromClassObj);
        Q.set_current_node(scopenoteLinkObj);
        int set_all_links_sn = Q.get_all_instances(0);
        Q.reset_set(set_all_links_sn);
        int set_terms_with_sn = Q.get_from_value(set_all_links_sn);
        Q.reset_set(set_terms_with_sn);
        Q.free_set(set_all_links_sn);

        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
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
        Vector<String> terms_with_sn_TR_Vec = new Vector<String>();
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
                Hashtable<String, String> trSns = u.getTranslationScopeNotes(commentObject.getValue());
                Vector<String> langCodes = new Vector<String>(trSns.keySet());
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

        //HISTORICAL NOTES
        Vector<String> terms_with_hn_Vec = new Vector<String>();
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
        /*
         for(int i=0; i<termsOfThes1.size();i++){
         String targetTerm = termsOfThes1.get(i);
         NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
         if(targetInfo.descriptorInfo.containsKey(ConstantParameters.scope_note_kwd)){
         Q.reset_name_scope();
         Vector<String> scopeNote = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.scope_note_kwd, Q, sis_session);
         targetInfo.descriptorInfo.put(ConstantParameters.scope_note_kwd, scopeNote);
         }

         if(targetInfo.descriptorInfo.containsKey(ConstantParameters.comment_kwd)){
         Q.reset_name_scope();
         Vector<String> comment = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.comment_kwd, Q, sis_session);
         targetInfo.descriptorInfo.put(ConstantParameters.comment_kwd, comment);

         }

         if(targetInfo.descriptorInfo.containsKey(ConstantParameters.translations_scope_note_kwd)){
         Q.reset_name_scope();
         Vector<String> trSn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.translations_scope_note_kwd, Q, sis_session);
         targetInfo.descriptorInfo.put(ConstantParameters.translations_scope_note_kwd, trSn);
         }

         if(targetInfo.descriptorInfo.containsKey(ConstantParameters.historical_note_kwd)){
         Q.reset_name_scope();
         Vector<String> hn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.historical_note_kwd, Q, sis_session);
         targetInfo.descriptorInfo.put(ConstantParameters.historical_note_kwd, hn);
         }
         }
         */

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            index = Parameters.CLASS_SET.indexOf("TERM");
            termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
            set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
            Q.reset_set(set_terms);

            Vector<String> termsOfThes2 = new Vector<String>();
            termsOfThes2.addAll(dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName2 + ".");

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);

            //Enumeration<String> termEnum = termsInfo.keys();
            //while(termEnum.hasMoreElements()){
            //String targetTerm = termEnum.nextElement();
            for (int i = 0; i < termsOfThes2.size(); i++) {
                String targetTerm = termsOfThes2.get(i);
                NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.scope_note_kwd)) {
                    Q.reset_name_scope();
                    Vector<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
                    Vector<String> scopeNote = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.scope_note_kwd, Q, TA, sis_session);
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

                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.comment_kwd)) {
                    Q.reset_name_scope();
                    Vector<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.comment_kwd);
                    Vector<String> comments = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.comment_kwd, Q, TA, sis_session);
                    if (existing.size() == 1) {
                        String finalVal = "";

                        String cmOld = "";
                        if (existing.size() == 1) {
                            cmOld = existing.get(0);
                        }

                        String cmNew = "";
                        if (comments.size() == 1) {
                            cmNew = comments.get(0);
                        }

                        if (cmOld.length() > 0) {
                            finalVal += cmOld;
                        }

                        if (cmNew.length() > 0 && cmNew.equals(cmOld) == false) {
                            finalVal = u.mergeStrings(cmOld, cmNew);
                            /*if (finalVal.length() > 0) {
                             finalVal += "; ";
                             }*/
                            finalVal += cmNew;
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

                if (targetInfo.descriptorInfo.containsKey(ConstantParameters.translations_scope_note_kwd)) {
                    Q.reset_name_scope();

                    Vector<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
                    Vector<String> trSn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
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
                    Vector<String> hn = dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.historical_note_kwd, Q, TA, sis_session);
                    Vector<String> existing = targetInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);

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
            }
        }

    }

    public void ReadThesaursTermStatuses(QClass Q, IntegerObject sis_session,
            String thesaurusName1, String thesaurusName2, Hashtable<String, NodeInfoStringContainer> termsInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();

        StringObject THESstatusForInsertion = new StringObject();
        StringObject THESstatusUnderConstruction = new StringObject();
        StringObject THESstatusForReinspection = new StringObject();
        StringObject THESstatusForApproval = new StringObject();
        StringObject THESstatusApproved = new StringObject();
        Vector<StringObject> allStatuses = new Vector<StringObject>();

        //READING FROM THES1
        //ALSO READ IDS OF STATUS CLASSES IN NEW MERGED THESAURUS --> KEEP THESE IDS IN HASHTABLE merged_thesaurus_status_classIds
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

            Vector<String> termsWithThisStatus = new Vector<String>();

            Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
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

                Vector<String> termsWithThisStatus = new Vector<String>();

                Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
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
                            Vector<String> existingStatuses = targetInfo.descriptorInfo.get(ConstantParameters.status_kwd);
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
            Hashtable<String, String> XMLsources) {

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
        Vector<String> sourceNames = dbGen.get_Node_Names_Of_Set(set_sources, true, Q, sis_session);
        Q.free_set(set_sources);
        Collections.sort(sourceNames);

        Q.reset_name_scope();
        Q.set_current_node(SourceObj);
        Q.set_current_node(SourceNoteObj);

        int set_source_note_links = Q.get_all_instances(0);
        Q.reset_set(set_source_note_links);

        int set_sources_with_source_note = Q.get_from_value(set_source_note_links);
        Q.reset_set(set_sources_with_source_note);

        Vector<String> sourcesWithSourceNote = dbGen.get_Node_Names_Of_Set(set_sources_with_source_note, true, Q, sis_session);
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
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            Vector<String> guideTerms,
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations) {

        Vector<String> allTerms = new Vector<String>();

        String[] output = Utilities.getSortedTermAllOutputArray();

        Vector<String> outputVec = new Vector<String>();
        for (int k = 0; k < output.length; k++) {
            outputVec.add(output[k]);
        }

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
        Hashtable<String, String> kewyWordsMappings = new Hashtable<String, String>();
        dbGen.applyKeywordMappings(SessionUserInfo.selectedThesaurus, Q, sis_session, output, kewyWordsMappings);
        dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] termClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(termClasses);
        int set_terms = dbGen.get_Instances_Set(termClasses, Q, sis_session);
        Q.reset_set(set_terms);

        int set_from_links = Q.get_link_from(set_terms);

        Q.reset_set(set_from_links);

        Vector<Return_Full_Link_Id_Row> retFLIVals = new Vector<Return_Full_Link_Id_Row>();
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
                    //newContainer.descriptorInfo.get("id").add(new SortItem("" + targetTermId, targetTermId, category));
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
                        //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
                        termsInfo.put(value, newContainer);
                        allTerms.add(value);
                    }
                    termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTerm);

                    //guide terms
                    if (category.length() > 0) {
                        if (XMLguideTermsRelations.containsKey(value) == false) {
                            XMLguideTermsRelations.put(value, new Vector<SortItem>());
                        }
                        Vector<SortItem> existingRelations = XMLguideTermsRelations.get(value);
                        existingRelations.add(targetTermSortItem);
                        XMLguideTermsRelations.put(value, existingRelations);
                    }

                } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
                    if (termsInfo.containsKey(value) == false) {
                        NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                        //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
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
         //newContainer.descriptorInfo.get("id").add(new SortItem("" + targetTermId, targetTermId, category));
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
         //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
         termsInfo.put(value, newContainer);
         allTerms.add(value);
         }
         termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTerm);

         //guide terms
         if (category.length() > 0) {
         if (XMLguideTermsRelations.containsKey(value) == false) {
         XMLguideTermsRelations.put(value, new Vector<SortItem>());
         }
         Vector<SortItem> existingRelations = XMLguideTermsRelations.get(value);
         existingRelations.add(targetTermSortItem);
         XMLguideTermsRelations.put(value, existingRelations);
         }



         } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
         if (termsInfo.containsKey(value) == false) {
         NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
         //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
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
                        //newContainer.descriptorInfo.get("id").add(new SortItem("" + targetTermId, targetTermId, category));
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
                            //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }
                        Vector<String> nts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd);
                        if (nts.contains(targetTerm) == false) {
                            nts.add(targetTerm);
                            termsInfo.get(value).descriptorInfo.put(ConstantParameters.nt_kwd, nts);
                        }

                        //guide terms
                        if (category.length() > 0) {
                            if (XMLguideTermsRelations.containsKey(value) == false) {
                                XMLguideTermsRelations.put(value, new Vector<SortItem>());
                            }
                            Vector<SortItem> existingRelations = XMLguideTermsRelations.get(value);
                            existingRelations.add(targetTermSortItem);
                            XMLguideTermsRelations.put(value, existingRelations);
                        }

                    } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
                            //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }

                        Vector<String> rts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd);
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
             //newContainer.descriptorInfo.get("id").add(new SortItem("" + targetTermId, targetTermId, category));
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
             //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
             termsInfo.put(value, newContainer);
             allTerms.add(value);
             }
             Vector<String> nts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd);
             if (nts.contains(targetTerm) == false) {
             nts.add(targetTerm);
             termsInfo.get(value).descriptorInfo.put(ConstantParameters.nt_kwd, nts);
             }


             //guide terms
             if (category.length() > 0) {
             if (XMLguideTermsRelations.containsKey(value) == false) {
             XMLguideTermsRelations.put(value, new Vector<SortItem>());
             }
             Vector<SortItem> existingRelations = XMLguideTermsRelations.get(value);
             existingRelations.add(targetTermSortItem);
             XMLguideTermsRelations.put(value, existingRelations);
             }



             } else if (categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
             if (termsInfo.containsKey(value) == false) {
             NodeInfoStringContainer newContainer = new NodeInfoStringContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, output);
             //newContainer.descriptorInfo.get("id").add(new SortItem("" + valueId, valueId, category));
             termsInfo.put(value, newContainer);
             allTerms.add(value);
             }

             Vector<String> rts = termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd);
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

    public Hashtable<String, Vector<String>> ReadThesaurusHierarchies(UserInfoClass refSessionUserInfo,
            QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2) {

        if (thesaurusName1 == null) { // supporting copy operation
            return new Hashtable<String, Vector<String>>();
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

        Vector<String> hierarchyNames = new Vector<String>();
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

        Hashtable<String, Vector<String>> thesaurus_hierarchy_Facets_Relations = new Hashtable<String, Vector<String>>();

        for (int i = 0; i < hierarchyNames.size(); i++) {
            StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyNames.get(i)));
            Vector<String> hierarchyFacets = new Vector<String>();
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

            Vector<String> hierarchyNamesOfThes2 = new Vector<String>();
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

            Hashtable<String, Vector<String>> thesaurus_hierarchy_Facets_RelationsOfThes2 = new Hashtable<String, Vector<String>>();

            for (int i = 0; i < hierarchyNamesOfThes2.size(); i++) {
                StringObject hierarchyObj = new StringObject(prefix_class.concat(hierarchyNamesOfThes2.get(i)));
                Vector<String> hierarchyFacets = new Vector<String>();
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
            Enumeration<String> secondThesHiers = thesaurus_hierarchy_Facets_RelationsOfThes2.keys();
            while (secondThesHiers.hasMoreElements()) {
                String targetHier = secondThesHiers.nextElement();
                Vector<String> targetHierFacets = thesaurus_hierarchy_Facets_RelationsOfThes2.get(targetHier);

                if (thesaurus_hierarchy_Facets_Relations.containsKey(targetHier)) {

                    Vector<String> existingFacets = thesaurus_hierarchy_Facets_Relations.get(targetHier);

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

    public Hashtable<String, Vector<String>> ReadNextLevelSetTermsAndBts(QClass Q, IntegerObject sis_session, DBGeneral dbGen,
            int set_next_level_terms, StringObject btFromClassbj, StringObject btLinkObj) {

        Hashtable<String, Vector<String>> nextLevelSet_Terms_and_Bts = new Hashtable<String, Vector<String>>();

        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        Q.reset_name_scope();

        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if (Q.bulk_return_link(set_next_level_terms, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String termName = dbGen.removePrefix(row.get_v1_cls());
                String btName = dbGen.removePrefix(row.get_v3_cmv().getString());

                Vector<String> otherBts = nextLevelSet_Terms_and_Bts.get(termName);
                if (otherBts == null) {
                    otherBts = new Vector<String>();
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

         Vector<String> otherBts = nextLevelSet_Terms_and_Bts.get(termName);
         if (otherBts == null) {
         otherBts = new Vector<String>();
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

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Vector<Hashtable<String, Vector<String>>> allLevelsOfThes1 = new Vector<Hashtable<String, Vector<String>>>();
        Vector<Hashtable<String, Vector<String>>> allLevelsOfThes2 = new Vector<Hashtable<String, Vector<String>>>();

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
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */

            String pathToMessagesXML = pathToErrorsXML;
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            //logFileWriter.append("<!--Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName1 + ".-->\r\n");
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyTermsLevelByLevel/TopTermReferenceFailed", errorArgs, pathToErrorsXML);
            resultObj.setValue(resultMessageObj.getValue());
            //resultObj.setValue("Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName1 + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            errorArgs.removeAllElements();
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
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = pathToErrorsXML;
                StringObject resultMessageObj_2 = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                //logFileWriter.append("<!--Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName1 + ".-->\r\n");
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj_2, "root/CopyTermsLevelByLevel/TopTermReferenceFailed", errorArgs, pathToErrorsXML);
                resultObj.setValue(resultMessageObj_2.getValue());
                //resultObj.setValue("Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName2 + ".");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                errorArgs.removeAllElements();
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
            Vector<Hashtable<String, Vector<String>>> allLevelsOfThes) {

        DBGeneral dbGen = new DBGeneral();

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
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = pathToErrorsXML;
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            //logFileWriter.append("<!--Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName1 + ".-->\r\n");
            errorArgs.add(sourceThesaurus);
            dbGen.Translate(resultMessageObj, "root/CopyTermsLevelByLevel/TopTermReferenceFailed", errorArgs, pathToErrorsXML);
            resultObj.setValue(resultMessageObj.getValue());

            //logFileWriter.append("<!--Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + thesaurusName1 + ".-->\r\n");
            resultObj.setValue("Αποτυχία αναφοράς στην κλάση TopTerm του θησαυρού : " + sourceThesaurus + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            errorArgs.removeAllElements();
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
            String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, Vector<Hashtable<String, Vector<String>>> allLevelsOfThes1, Vector<Hashtable<String, Vector<String>>> allLevelsOfThes2, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();
        UsersClass wtmsUsers = new UsersClass();

        String pathToMessagesXML = Utilities.getMessagesXml();

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

                Enumeration<String> pairsEnumMerged = allLevelsOfThes1.get(i).keys();
                while (pairsEnumMerged.hasMoreElements()) {

                    if (termsPerLevel % restartInterval == 0) {
                        if (common_utils != null) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TERM counter: " + termsPerLevel + " of " + homanyTerms + "  ");
                            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                        }
                    }
                    termsPerLevel++;

                    String term = pairsEnumMerged.nextElement();

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
                     Vector<String> errorArgs = new Vector<String>();
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
                        StringObject termobj = new StringObject(prefix_term.concat(term));
                        Q.reset_name_scope();

                        if (Q.set_current_node(termobj) != QClass.APIFail) {
                            exists = true;
                        }
                    }

                    Q.reset_name_scope();
                    Vector<String> allBts = new Vector<String>();
                    Vector<String> additionalBTs = new Vector<String>();
                    additionalBTs.addAll(allLevelsOfThes1.get(i).get(term));

                    if (exists) {

                        allBts.addAll(dbGen.returnResults(SessionUserInfo, term, ConstantParameters.bt_kwd, Q, TA, sis_session));
                        /*
                         *
                         *
                         *HARDCODED GREEKS TRANSLATE
                         *
                         *
                         */

                        StringObject resultMessageObj = new StringObject();
                        StringObject resultMessageObj_2 = new StringObject();
                        Vector<String> errorArgs = new Vector<String>();

                        if (allBts.size() == 0) {//ALREADY EXISTS BUT WITHOUT BTS --> IT IS ALREADY DEFINED AS TOP TERM

                            errorArgs.add(Utilities.escapeXML(term));
                            errorArgs.add(thesaurusName2);
                            errorArgs.add(Utilities.escapeXML(term));
                            dbGen.Translate(resultMessageObj, "root/CreateTermsLevelByLevel/TermFoundAsTopTerm", errorArgs, pathToMessagesXML);
                            errorArgs.removeAllElements();

                            logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name>");
                            logFileWriter.append("<errorType>name</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(term) + "1</errorValue>");
                            logFileWriter.append("<reason>" + resultMessageObj + "1.</reason>");
                            //logFileWriter.append("<reason>Ο όρος " + Utilities.escapeXML(term) + " βρέθηκε σαν OK στον θησαυρό " + thesaurusName2 + ". Για την επιτυχή προσθήκη του μετονομάζεται σε " + Utilities.escapeXML(term) + "1.</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                            term += "1";
                            StringObject newtermobj = new StringObject(prefix_term.concat(term));
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
                        creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.bt_kwd, allBts, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                        //logFileWriter.append("Skipping Term : '" + term+"' under BTs : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        //continue;
                    } else {
                        if (additionalBTs.size() > 1 && additionalBTs.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            additionalBTs.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                        creation_modificationOfTerm.createNewTerm(SessionUserInfo, term, additionalBTs, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                    }
                    //logFileWriter.append(termsPerLevel+". " +term + " ----- " + nextLevelSet_Terms_and_Bts.get(term).toString()+"\r\n");

                    if (resultObj.getValue().length() > 0) {
                        /*
                         * 
                         *
                         *
                         *
                         * HARDCODED, CHANGE IT THROUGH TRANSLATION
                         *
                         *
                         *
                         *
                         */

                        StringObject resultMessageObj = new StringObject();
                        Vector<String> errorArgs = new Vector<String>();

                        errorArgs.add(thesaurusName1);
                        errorArgs.add(Utilities.escapeXML(term));
                        dbGen.Translate(resultMessageObj, "root/CreateTermsLevelByLevel/CopyTermFailure", errorArgs, pathToMessagesXML);
                        errorArgs.removeAllElements();

                        resultObj.setValue(resultMessageObj.getValue() + resultObj.getValue());
                        //resultObj.setValue("Σφάλμα κατά την αντιγραφή όρου από τον θησαυρό " + thesaurusName1 + ". " + resultObj.getValue());
                        //Q.free_set(set_next_level_links);
                        // Q.free_set(set_top_terms);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ERROR: " + (i + 1) + "." + termsPerLevel + ". Ο όρος : '" + term + " εμφάνισε το εξής λάθος : " + resultObj.getValue());

                        return false;
                    } else {
                        if (exists) {
                            //  logFileWriter.append(i+1 +"." +termsPerLevel +". Ο όρος : '" + term+"' τροποποιήθηκε με επιτυχία. Προστέθηκαν οι ΠΟ : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        } else {
                            //logFileWriter.append(i+1 +"." +termsPerLevel +".Ο όρος : '" + term+"' προστέθηκε στην βάση με επιτυχία κάτω από τους ΠΟ : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        }
                    }
                    //logFileWriter.flush();                    
                    resultObj.setValue("");

                }
            }

            if (allLevelsOfThes2 != null && allLevelsOfThes2.size() > i) {

                Enumeration<String> pairsEnumMerged = allLevelsOfThes2.get(i).keys();
                while (pairsEnumMerged.hasMoreElements()) {

                    if (termsPerLevel % restartInterval == 0) {
                        if (common_utils != null) {
                            //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "TERM counter: " + termsPerLevel + " of " + homanyTerms + "  ");
                            common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                        }
                    }
                    termsPerLevel++;
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" i = " + i + " termsPerLevel = " + termsPerLevel);
                    String term = pairsEnumMerged.nextElement();

                    Q.free_all_sets();

                    //createNewTerm(sessionInstance,newName,decodedValues,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToErrorsXML);
                    //NOW COPY TO NEW THESAURUS
                    StringObject termobj = new StringObject(prefix_term.concat(term));
                    Q.reset_name_scope();
                    boolean exists = false;
                    if (Q.set_current_node(termobj) != QClass.APIFail) {
                        exists = true;
                    }
                    Q.reset_name_scope();
                    Vector<String> allBts = new Vector<String>();
                    Vector<String> additionalBTs = new Vector<String>();
                    additionalBTs.addAll(allLevelsOfThes2.get(i).get(term));

                    StringObject resultMessageObj_2 = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    if (exists) {

                        allBts.addAll(dbGen.returnResults(SessionUserInfo, term, ConstantParameters.bt_kwd, Q, TA, sis_session));

                        if (allBts.size() == 0) {//ALREADY EXISTS BUT WITHOUT BTS --> IT IS ALREADY DEFINED AS TOP TERM

                            errorArgs.add(Utilities.escapeXML(term));
                            errorArgs.add(thesaurusName1);
                            errorArgs.add(Utilities.escapeXML(term));
                            dbGen.Translate(resultMessageObj_2, "root/CreateTermsLevelByLevel/TermFoundAsTopTerm", errorArgs, pathToMessagesXML);
                            errorArgs.removeAllElements();

                            logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name>");
                            logFileWriter.append("<errorType>name</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(term) + "2</errorValue>");
                            logFileWriter.append("<reason>" + resultMessageObj_2.getValue() + "2.</reason>");
                            //logFileWriter.append("<reason> όρος " + Utilities.escapeXML(term) + " βρέθηκε σαν OK στον θησαυρό " + thesaurusName1 + ". Για την επιτυχή προσθήκη του μετονομάζεται σε " + Utilities.escapeXML(term) + "2.</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                            term += "2";
                            StringObject newtermobj = new StringObject(prefix_term.concat(term));
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
                        creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.bt_kwd, allBts, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                        //logFileWriter.append("Skipping Term : '" + term+"' under BTs : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        //continue;
                    } else {
                        if (additionalBTs.size() > 1 && additionalBTs.contains(Parameters.UnclassifiedTermsLogicalname)) {
                            additionalBTs.remove(Parameters.UnclassifiedTermsLogicalname);
                        }
                        creation_modificationOfTerm.createNewTerm(SessionUserInfo, term, additionalBTs, SessionUserInfo.name, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);
                    }
                    //logFileWriter.append(termsPerLevel+". " +term + " ----- " + nextLevelSet_Terms_and_Bts.get(term).toString()+"\r\n");

                    if (resultObj.getValue().length() > 0) {
                        /*
                         * 
                         *
                         *
                         *
                         * HARDCODED, CHANGE IT THROUGH TRANSLATION
                         *
                         *
                         *
                         *
                         */

                        StringObject resultMessageObj_3 = new StringObject();

                        errorArgs.add(thesaurusName2);
                        errorArgs.add(Utilities.escapeXML(term));
                        dbGen.Translate(resultMessageObj_3, "root/CreateTermsLevelByLevel/CopyTermFailure", errorArgs, pathToMessagesXML);
                        errorArgs.removeAllElements();

                        resultObj.setValue(resultMessageObj_2.getValue() + resultObj.getValue());

                        //resultObj.setValue("Σφάλμα κατά την αντιγραφή όρου από τον θησαυρό " + thesaurusName2 + ". " + resultObj.getValue());
                        //Q.free_set(set_next_level_links);
                        // Q.free_set(set_top_terms);
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "ERROR: " + (i + 1) + "." + termsPerLevel + ". Ο όρος : '" + term + " εμφάνισε το εξής λάθος : " + resultObj.getValue());

                        return false;
                    } else {
                        if (exists) {
                            //  logFileWriter.append(i+1 +"." +termsPerLevel +". Ο όρος : '" + term+"' τροποποιήθηκε με επιτυχία. Προστέθηκαν οι ΠΟ : "+  allLevels.get(i).get(term).toString()+"\r\n");
                        } else {
                            //logFileWriter.append(i+1 +"." +termsPerLevel +".Ο όρος : '" + term+"' προστέθηκε στην βάση με επιτυχία κάτω από τους ΠΟ : "+  allLevels.get(i).get(term).toString()+"\r\n");
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
        Hashtable<String, Vector<String>> allTermRts = new Hashtable<String, Vector<String>>();
        Vector<String> rtsToThemSelves = new Vector<String>();

        StringObject rtFromClassObj = new StringObject();
        StringObject rtLinkObj = new StringObject();

        //update all fields in order to keep it in consistent state
        UsersClass wtmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.rt_kwd, rtFromClassObj, rtLinkObj, Q, sis_session);

        Q.reset_name_scope();
        if (Q.set_current_node(rtFromClassObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(rtFromClassObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyRTs/CategoryReferenceFailed_2_Param", errorArgs, pathToErrorsXML);
            errorArgs.removeAllElements();

            //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + rtFromClassObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
            return false;
        }

        if (Q.set_current_node(rtLinkObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(rtFromClassObj.getValue());
            errorArgs.add(rtLinkObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyRTs/CategoryReferenceFailed", errorArgs, pathToErrorsXML);
            errorArgs.removeAllElements();

            resultObj.setValue(resultMessageObj.getValue());
            //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + rtFromClassObj.getValue() + "->" + rtLinkObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
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

        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if (Q.bulk_return_link(set_rt_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String term1Name = dbGen.removePrefix(row.get_v1_cls());
                String term2Name = dbGen.removePrefix(row.get_v3_cmv().getString());

                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                StringObject resultMessageObj_2 = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                if (term1Name.compareTo(term2Name) == 0) {

                    errorArgs.add(Utilities.escapeXML(term1Name));
                    errorArgs.add(thesaurusName1);
                    dbGen.Translate(resultMessageObj, "root/CopyRTs/TermLinkToRT", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();

                    rtsToThemSelves.add(term1Name);
                    logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
                    logFileWriter.append("<reason>" + resultMessageObj.getValue() + "</reason>");
                    //logFileWriter.append("<reason>" + Utilities.escapeXML(term1Name) + " βρέθηκε να έχει σχέση ΣΟ με τον εαυτό του στον θησαυρό: " + thesaurusName1 + ". Η σχέση αυτή παρακάμπτεται.</reason>");
                    logFileWriter.append("</targetTerm>/r/n");
                    continue; //ignore from reading
                }

                Vector<String> otherRts1 = allTermRts.get(term1Name);
                Vector<String> otherRts2 = allTermRts.get(term2Name);

                if (otherRts1 == null) {

                    //check if this relation has already been declared in second term RTs
                    if (otherRts2 == null) {
                        otherRts1 = new Vector<String>();
                        otherRts1.add(term2Name);
                        allTermRts.put(term1Name, otherRts1);
                    } else {
                        if (otherRts2.contains(term1Name) == false) {
                            otherRts1 = new Vector<String>();
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
         logFileWriter.append("<reason>O Όρος : " + Utilities.escapeXML(term1Name) + " βρέθηκε να έχει σχέση ΣΟ με τον εαυτό του στον θησαυρό: " + thesaurusName1 + ". Η σχέση αυτή παρακάμπτεται.</reason>");
         logFileWriter.append("</targetTerm>/r/n");
         continue; //ignore from reading
         }

         Vector<String> otherRts1 = allTermRts.get(term1Name);
         Vector<String> otherRts2 = allTermRts.get(term2Name);

         if (otherRts1 == null) {

         //check if this relation has already been declared in second term RTs
         if (otherRts2 == null) {
         otherRts1 = new Vector<String>();
         otherRts1.add(term2Name);
         allTermRts.put(term1Name, otherRts1);
         } else {
         if (otherRts2.contains(term1Name) == false) {
         otherRts1 = new Vector<String>();
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
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */

                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(rtFromClassObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyRTs/CategoryReferenceFailed_2_Param", errorArgs, pathToErrorsXML);
                errorArgs.removeAllElements();

                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Aποτυχία αναφοράς στην κλάση " + rtFromClassObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                return false;
            }

            if (Q.set_current_node(rtLinkObj) == QClass.APIFail) {
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */

                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(rtFromClassObj.getValue());
                errorArgs.add(rtLinkObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyRTs/CategoryReferenceFailed", errorArgs, pathToErrorsXML);
                errorArgs.removeAllElements();

                //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + rtFromClassObj.getValue() + "->" + rtLinkObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
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

                    String pathToMessagesXML = Utilities.getMessagesXml();
                    StringObject resultMessageObj = new StringObject();
                    StringObject resultMessageObj_2 = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    if (term1Name.compareTo(term2Name) == 0) {

                        errorArgs.add(Utilities.escapeXML(term1Name));
                        errorArgs.add(thesaurusName2);
                        dbGen.Translate(resultMessageObj_2, "root/CopyRTs/TermLinkToRT", errorArgs, pathToMessagesXML);
                        errorArgs.removeAllElements();

                        rtsToThemSelves.add(term2Name);
                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term1Name) + "</name><errorType>" + ConstantParameters.rt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(term1Name) + "</errorValue>");
                        logFileWriter.append("<reason>" + resultMessageObj_2.getValue() + "</reason>");
                        //logFileWriter.append("<reason>O Όρος : " + Utilities.escapeXML(term1Name) + " βρέθηκε να έχει σχέση ΣΟ με τον εαυτό του στον θησαυρό: " + thesaurusName2 + ". Η σχέση αυτή παρακάμπτεται.</reason>");
                        logFileWriter.append("</targetTerm>/r/n");
                        continue; //ignore from reading
                    }

                    Vector<String> otherRts1 = allTermRts.get(term1Name);
                    Vector<String> otherRts2 = allTermRts.get(term2Name);

                    if (otherRts1 == null) {

                        //check if this relation has already been declared in second term RTs
                        if (otherRts2 == null) {
                            otherRts1 = new Vector<String>();
                            otherRts1.add(term2Name);
                            allTermRts.put(term1Name, otherRts1);
                        } else {
                            if (otherRts2.contains(term1Name) == false) {
                                otherRts1 = new Vector<String>();
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
             logFileWriter.append("<reason>O Όρος : " + Utilities.escapeXML(term1Name) + " βρέθηκε να έχει σχέση ΣΟ με τον εαυτό του στον θησαυρό: " + thesaurusName2 + ". Η σχέση αυτή παρακάμπτεται.</reason>");
             logFileWriter.append("</targetTerm>/r/n");
             continue; //ignore from reading
             }

             Vector<String> otherRts1 = allTermRts.get(term1Name);
             Vector<String> otherRts2 = allTermRts.get(term2Name);

             if (otherRts1 == null) {

             //check if this relation has already been declared in second term RTs
             if (otherRts2 == null) {
             otherRts1 = new Vector<String>();
             otherRts1.add(term2Name);
             allTermRts.put(term1Name, otherRts1);
             } else {
             if (otherRts2.contains(term1Name) == false) {
             otherRts1 = new Vector<String>();
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
            String mergedThesaurusName, OutputStreamWriter logFileWriter, StringObject resultObj, Hashtable<String, Vector<String>> allTermRts, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

        UsersClass wtmsUsers = new UsersClass();
        DBGeneral dbGen = new DBGeneral();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        int rtRelations = 1;
        Enumeration<String> pairsEnumMerged = allTermRts.keys();

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING RTS in " + mergedThesaurusName);

        rtRelations = 0;
        int howmany = allTermRts.size();
        pairsEnumMerged = allTermRts.keys();
        while (pairsEnumMerged.hasMoreElements()) {

            Q.free_all_sets();

            if (rtRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "RT counter: " + rtRelations + " of " + howmany + "    ");
                    //Utils.StaticClass.webAppSystemOutPrintln("RESTARTING SERVER");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            rtRelations++;

            String term = pairsEnumMerged.nextElement();

            Vector<String> allRTs = new Vector<String>();
            allRTs.addAll(allTermRts.get(term));

            if (allRTs.size() == 0) {
                continue;
            }

            creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.rt_kwd, allRTs, term, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, resolveError, logFileWriter, ConsistencyCheckPolicy);

            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + rtRelations + ". Αποτυχία κατά την  προσθήκη των ΣΟ : " + allRTs.toString() + " του όρου : '" + term + "'." + resultObj.getValue());

                return false;
            } else {
                // logFileWriter.append(rtRelations +". Ο όρος : '" + term+"' τροποποιήθηκε με επιτυχία. Προστέθηκαν οι ΣΟ : "+  allRTs.toString()+"\r\n");
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
        Vector<StringObject> allStatuses = new Vector<StringObject>();

        Hashtable<String, String> thesaurus1_statuses = new Hashtable<String, String>();
        Hashtable<String, String> thesaurus2_statuses = new Hashtable<String, String>();
        Hashtable<String, Long> merged_thesaurus_status_classIds = new Hashtable<String, Long>();

        //READING FROM THES1 
        //ALSO READ IDS OF STATUS CLASSES IN NEW MERGED THESAURUS --> KEEP THESE IDS IN HASHTABLE merged_thesaurus_status_classIds
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
            Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
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
                Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
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
            Hashtable<String, String> thesaurus1_statuses, Hashtable<String, String> thesaurus2_statuses,
            Hashtable<String, Long> merged_thesaurus_status_classIds) throws IOException {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBFilters dbF = new DBFilters();
        //THEMASUsers wtmsUsers = new UsersClass();

        String minorPriorityStatusKey = "";
        int minorPriority = 1000;
        Enumeration<String> statusEnum = merged_thesaurus_status_classIds.keys();
        while (statusEnum.hasMoreElements()) {
            String checkStatus = statusEnum.nextElement();
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
        String mergedTermPrefix = dbtr.getThesaurusPrefix_Descriptor(mergedThesaurusName, Q, sis_session.getValue());
        Enumeration<String> pairsEnumMerged = thesaurus1_statuses.keys();
        while (pairsEnumMerged.hasMoreElements()) {

            if (counter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Status counter: " + counter + " of " + howmany + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            counter++;
            //term was found in thesaurus1 with under statusClass : status
            String term = pairsEnumMerged.nextElement();
            String StatusThes1 = thesaurus1_statuses.get(term);
            String StatusThes2 = thesaurus2_statuses.get(term);   // Both statuses kept in Hashtables refer to mergedThesaurus Status Classes

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
                return false;
            }
            Q.reset_name_scope();
            int ret = QClass.APISucc;

            if (StatusThes1 == null && StatusThes2 != null) {
                Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                Identifier I_Term = new Identifier(termIDL);
                ret = Q.CHECK_Add_Instance(I_Term, I_Status);
            } else if (StatusThes1 != null && StatusThes2 == null) {
                Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes1).longValue());
                Identifier I_Term = new Identifier(termIDL);
                ret = Q.CHECK_Add_Instance(I_Term, I_Status);
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

                //HARDCODED GREEKS
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                StringObject resultMessageObj_2 = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                int priority = getStatusPriority(StatusThes2, mergedThesaurusName) - getStatusPriority(StatusThes1, mergedThesaurusName);
                if (priority > 0) {
                    //change             
                    errorArgs.add(Utilities.escapeXML(term));
                    errorArgs.add(StatusThes1StrGR);
                    errorArgs.add(StatusThes2StrGR);
                    errorArgs.add(StatusThes1StrGR);
                    dbGen.Translate(resultMessageObj, "root/CreateStatuses/FoundInStatus", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();

                    logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name><errorType>" + ConstantParameters.status_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + StatusThes2StrGR + "</errorValue>");
                    logFileWriter.append("<reason>" + resultMessageObj.getValue() + "</reason>");
                    //logFileWriter.append("<reason>O όρος : " + Utilities.escapeXML(term) + " βρέθηκε με κατάσταση '" + StatusThes1StrGR + "' και '" + StatusThes2StrGR + "'.\r\n\t\tΕπιλέχθηκε με σειρά προτεραιότητας η κατάσταση '" + StatusThes1StrGR + "'.</reason>");
                    logFileWriter.append("</targetTerm>\r\n");
                    logFileWriter.flush();
                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes1).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                    /* Not needed as merged thesaurus has no statuses defined yet
                     if (ret == QClass.APIFail) {
                     return false;
                     }

                     Identifier I_DelelteStatus = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                     ret = Q.Delete_Instance(I_Term, I_DelelteStatus);*/

                } else { //both status != null
                    if (priority < 0) {
                        errorArgs.add(Utilities.escapeXML(term));
                        errorArgs.add(StatusThes1StrGR);
                        errorArgs.add(StatusThes2StrGR);
                        errorArgs.add(StatusThes1StrGR);
                        dbGen.Translate(resultMessageObj_2, "root/CreateStatuses/FoundInStatus", errorArgs, pathToMessagesXML);
                        errorArgs.removeAllElements();

                        logFileWriter.append("\r\n<targetTerm><name>" + Utilities.escapeXML(term) + "</name><errorType>" + ConstantParameters.status_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + StatusThes1StrGR + "</errorValue>");
                        logFileWriter.append("<reason>" + resultMessageObj_2.getValue() + "</reason>");
                        //logFileWriter.append("<reason>O όρος : " + Utilities.escapeXML(term) + " βρέθηκε με κατάσταση '" + StatusThes1StrGR + "' και '" + StatusThes2StrGR + "'.\r\n\t\tΕπιλέχθηκε με σειρά προτεραιότητας η κατάσταση '" + StatusThes2StrGR + "'.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }
            }

            if (ret == QClass.APIFail) {
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */

                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(term);
                dbGen.Translate(resultMessageObj, "root/CreateStatuses/FailedToUpdateTermStatus", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                //resultObj.setValue("Αποτυχία ενημέρρωσης της κατάστασης του όρου : " + term);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + resultObj.getValue());
                return false;
            }
        }

        if (thesaurus2_statuses != null) {
            Enumeration<String> pairsEnumMerged2 = thesaurus2_statuses.keys();
            while (pairsEnumMerged2.hasMoreElements()) {
                //term was found in thesaurus2 with under statusClass : status
                String term = pairsEnumMerged2.nextElement();
                String StatusThes1 = thesaurus1_statuses.get(term);
                String StatusThes2 = thesaurus2_statuses.get(term);   // Both statuses kept in Hashtables refer to mergedThesaurus Status Classes

                Q.reset_name_scope();
                long termIDL = Q.set_current_node(new StringObject(mergedTermPrefix.concat(term)));

                if (termIDL == QClass.APIFail) {
                    /*
                     * 
                     *
                     *
                     *
                     * HARDCODED, CHANGE IT THROUGH TRANSLATION
                     *
                     *
                     *
                     *
                     */
                    String pathToMessagesXML = Utilities.getMessagesXml();
                    StringObject resultMessageObj = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    errorArgs.add(term);
                    errorArgs.add(mergedThesaurusName);
                    dbGen.Translate(resultMessageObj, "root/CreateStatuses/TermReferenceFailed", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();
                    resultObj.setValue(resultMessageObj.getValue());
                    //resultObj.setValue("Αποτυχία αναφοράς στον όρο " + term + " του θησαυρού: " + mergedThesaurusName);
                    return false;
                }
                Q.reset_name_scope();
                int ret = QClass.APISucc;
                Vector<String> decodedValues = new Vector<String>();
                if (StatusThes1 == null && StatusThes2 != null) { // else decision has already been made
                    decodedValues.add(StatusThes2);

                    Identifier I_Status = new Identifier(merged_thesaurus_status_classIds.get(StatusThes2).longValue());
                    Identifier I_Term = new Identifier(termIDL);
                    ret = Q.CHECK_Add_Instance(I_Term, I_Status);
                }

                if (ret == QClass.APIFail) {
                    /*
                     * 
                     *
                     *
                     *
                     * HARDCODED, CHANGE IT THROUGH TRANSLATION
                     *
                     *
                     *
                     *
                     */
                    String pathToMessagesXML = Utilities.getMessagesXml();
                    StringObject resultMessageObj = new StringObject();
                    Vector<String> errorArgs = new Vector<String>();

                    errorArgs.add(term);
                    dbGen.Translate(resultMessageObj, "root/CreateStatuses/FailedToUpdateTermStatus", errorArgs, pathToMessagesXML);
                    errorArgs.removeAllElements();

                    resultObj.setValue(resultMessageObj.getValue());
                    //resultObj.setValue("Αποτυχία ενημέρρωσης της κατάστασης του όρου : " + term);
                    return false;
                }
            }
        }
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
        Utilities u = new Utilities();

        Hashtable<String, String> scope_notes_HASH = new Hashtable<String, String>();
        Hashtable<String, String> scope_notes_EN_HASH = new Hashtable<String, String>();
        Hashtable<String, String> historical_notes_HASH = new Hashtable<String, String>();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName1 + ".");
        logFileWriter.flush();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);

        Q.reset_name_scope();
        //Find out hierarchy Classes and get their instances
        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] TermClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(TermClasses);
        int set_terms = dbGen.get_Instances_Set(TermClasses, Q, sis_session);
        Q.reset_set(set_terms);

        Vector<String> termNames = dbGen.get_Node_Names_Of_Set(set_terms, true, Q, sis_session);

        Q.free_set(set_terms);
        int commentCounter = 0;
        for (int i = 0; i < termNames.size(); i++) {
            Q.reset_name_scope();
            Vector<String> scopeNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.scope_note_kwd, Q, TA, sis_session);
            Vector<String> scopeNoteEN = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
            Vector<String> historicalNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.historical_note_kwd, Q, TA, sis_session);

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
        }

        Q.free_all_sets();

        if (thesaurusName2 != null) {

            wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName2);

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING COMMENT CATEGORIES FROM " + thesaurusName2 + ".");

            logFileWriter.flush();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);

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
                Vector<String> scopeNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.scope_note_kwd, Q, TA, sis_session);
                Vector<String> scopeNoteEN = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session);
                Vector<String> historicalNote = dbGen.returnResults(SessionUserInfo, termNames.get(i), ConstantParameters.historical_note_kwd, Q, TA, sis_session);

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
                        logFileWriter.append("<reason>Βρέθηκαν 2 ΔΣ για τον όρο : '" + Utilities.escapeXML(termNames.get(i)) + "'. Θα διατηρηθούν και οι δύο με το διαχωριστικό ' ### '.</reason>");
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
                        logFileWriter.append("<reason>Βρέθηκαν 2 SN για τον όρο : '" + Utilities.escapeXML(termNames.get(i)) + "'. Θα διατηρηθούν και οι δύο με το διαχωριστικό ' ### '.</reason>");
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
                        logFileWriter.append("<reason>Βρέθηκαν 2 ΙΣ για τον όρο : '" + Utilities.escapeXML(termNames.get(i)) + "'. Θα διατηρηθούν και οι δύο με το διαχωριστικό ' ### '.</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                        logFileWriter.flush();
                    }
                    historical_notes_HASH.put(termNames.get(i), firstHistoricalNote);
                    // logFileWriter.append(commentCounter + ". READING Historical Note from term " +termNames.get(i)+ " of thesaurus "+thesaurusName+".\r\n");
                }
            }

            Q.free_all_sets();

        }

        logFileWriter.flush();
        return CreateCommentCategories(refSessionUserInfo, common_utils, Q, TA, sis_session, tms_session, mergedThesaurusName, scope_notes_HASH, scope_notes_EN_HASH, historical_notes_HASH, logFileWriter, pathToErrorsXML, resultObj, ConsistencyCheckPolicy);
    }

    public boolean CreateCommentCategories(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String mergedThesaurusName,
            Hashtable<String, String> scope_notes_HASH,
            Hashtable<String, String> scope_notes_EN_HASH,
            Hashtable<String, String> historical_notes_HASH,
            OutputStreamWriter logFileWriter, String pathToErrorsXML, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        DBCreate_Modify_Term creation_modificationOfTerm = new DBCreate_Modify_Term();

        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, mergedThesaurusName);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING SCOPE_NOTES in " + mergedThesaurusName + ".  Ώρα: " + Utilities.GetNow());
        //common_utils.restartTransactionAndDatabase(Q,TA,sis_session,tms_session,mergedThesaurusName);
        logFileWriter.flush();
        int commentCounter = 0;

        int howmanyComments = scope_notes_HASH.size();
        howmanyComments += scope_notes_EN_HASH.size();
        howmanyComments += historical_notes_HASH.size();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefixPerson = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        String user = prefixPerson.concat(SessionUserInfo.name);
        Enumeration<String> pairsEnumMerged = scope_notes_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (SN) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.nextElement();

            Vector<String> sns = new Vector<String>();
            sns.add(scope_notes_HASH.get(term));

            if (sns.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.scope_note_kwd, sns, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Αποτυχία κατά την προσθήκη ΔΣ στον όρο : '" + term + "'." + resultObj.getValue());
                return false;
            } else {
                //logFileWriter.append(commentCounter +". Η ΔΣ του όρου : '" + term+"' προστέθηκε με επιτυχία.\r\n");
            }

            //commentCounter++;
            resultObj.setValue("");

        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING TRANSLATION SCOPE_NOTES in " + mergedThesaurusName + " Ώρα: " + Utilities.GetNow());

        pairsEnumMerged = scope_notes_EN_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {

            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (SN-Tra.) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;
            String term = pairsEnumMerged.nextElement();

            Vector<String> snsEN = new Vector<String>();
            snsEN.add(scope_notes_EN_HASH.get(term));

            if (snsEN.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.translations_scope_note_kwd, snsEN, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Αποτυχία κατά την προσθήκη SN στον όρο : '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". Η SN του όρου : '" + term+"' προστέθηκε με επιτυχία.\r\n");
            }
            //logFileWriter.flush();
            //commentCounter++;
            resultObj.setValue("");

            //logFileWriter.append(commentCounter + ". " + term + " ----- " + scope_notes.get(term).toString() + "\r\n");
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tCREATING HISTORICAL_NOTES in " + mergedThesaurusName + ". Ώρα: " + Utilities.GetNow());

        pairsEnumMerged = historical_notes_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {
            if (commentCounter % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Comment (HN) counter: " + commentCounter + " of " + howmanyComments + "   ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            commentCounter++;

            String term = pairsEnumMerged.nextElement();
            Vector<String> hns = new Vector<String>();
            hns.add(historical_notes_HASH.get(term));

            if (hns.size() > 0) {
                creation_modificationOfTerm.commitTermTransaction(SessionUserInfo, term, ConstantParameters.historical_note_kwd, hns, user, resultObj, Q, sis_session, TA, tms_session, dbGen, pathToErrorsXML, true, true, logFileWriter, ConsistencyCheckPolicy);
            }
            if (resultObj.getValue().length() > 0) {

                // Q.free_set(set_top_terms);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + commentCounter + ". Αποτυχία κατά την προσθήκη ΙΣ στον όρο : '" + term + "'." + resultObj.getValue());
                return false;
            } else {                //logFileWriter.append(commentCounter +". Η IΣ του όρου : '" + term+"' προστέθηκε με επιτυχία.\r\n");
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
        UsersClass wtmsUsers = new UsersClass();

        Hashtable<String, Vector<String>> term_Editor_Links_THES1_HASH = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> term_Date_Links_THES1_HASH = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> term_Editor_Links_THES2_HASH = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> term_Date_Links_THES2_HASH = new Hashtable<String, Vector<String>>();

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
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */

            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(editorFromClassObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/ClassReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + editorFromClassObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(editorLinkObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(editorFromClassObj.getValue());
            errorArgs.add(editorLinkObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            return false;
        }

        int set_all_links = Q.get_instances(0);
        Q.reset_set(set_all_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " Links from " + thesaurusName1 + ".");

        Q.reset_name_scope();

        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String termName = dbGen.removePrefix(row.get_v1_cls());
                String editorValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                Vector<String> otherVals = term_Editor_Links_THES1_HASH.get(termName);

                if (otherVals == null) {
                    otherVals = new Vector<String>();
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
         Vector<String> otherVals = term_Editor_Links_THES1_HASH.get(termName);

         if (otherVals == null) {
         otherVals = new Vector<String>();
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
            /*
             * 
             *
             *
             *
             * HARDCODED GREEKS, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(dateFromClassObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/ClassReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + dateFromClassObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(dateLinkObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED GREEKS, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(dateFromClassObj.getValue());
            errorArgs.add(dateLinkObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
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
                Vector<String> otherVals = term_Date_Links_THES1_HASH.get(termName);

                if (otherVals == null) {
                    otherVals = new Vector<String>();
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
         Vector<String> otherVals = term_Date_Links_THES1_HASH.get(termName);

         if (otherVals == null) {
         otherVals = new Vector<String>();
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
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */

                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(editorFromClassObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/ClassReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + editorFromClassObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(editorLinkObj) == QClass.APIFail) {
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(editorFromClassObj.getValue());
                errorArgs.add(editorLinkObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + editorFromClassObj.getValue() + "->" + editorLinkObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
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
                    Vector<String> otherVals = term_Editor_Links_THES2_HASH.get(termName);

                    if (otherVals == null) {
                        otherVals = new Vector<String>();
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
             Vector<String> otherVals = term_Editor_Links_THES2_HASH.get(termName);

             if (otherVals == null) {
             otherVals = new Vector<String>();
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
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(dateFromClassObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/ClassReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + dateFromClassObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(dateLinkObj) == QClass.APIFail) {
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(dateFromClassObj.getValue());
                errorArgs.add(dateLinkObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopyDatesAndEditors/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + dateFromClassObj.getValue() + "->" + dateLinkObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
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
                    Vector<String> otherVals = term_Date_Links_THES2_HASH.get(termName);

                    if (otherVals == null) {
                        otherVals = new Vector<String>();
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
             Vector<String> otherVals = term_Date_Links_THES2_HASH.get(termName);

             if (otherVals == null) {
             otherVals = new Vector<String>();
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
            Hashtable<String, Vector<String>> term_Editor_Links_THES1_HASH, Hashtable<String, Vector<String>> term_Date_Links_THES1_HASH,
            Hashtable<String, Vector<String>> term_Editor_Links_THES2_HASH, Hashtable<String, Vector<String>> term_Date_Links_THES2_HASH,
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

        Enumeration<String> pairsEnumMerged = term_Date_Links_THES1_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + dateKeywordStr + " (date) counter: " + termsModified + " of " + totalSize + "  ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;

            String term = pairsEnumMerged.nextElement();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

            Vector<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            Vector<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
            //Vector<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            //Vector<String> datesThes2   = term_Date_Links_THES2_HASH.get(term);

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
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + editorLinkObj.getValue() + " : " + editorsThes1.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

            if (datesThes1 != null && datesThes1.size() > 0) {
                //create all dates from thes 1
                for (int i = 0; i < datesThes1.size(); i++) {
                    resultObj.setValue(resultObj.getValue().concat(dbCon.connectSpecificTime(SessionUserInfo.selectedThesaurus, targetTermObj, datesThes1.get(i), dateFromClassObj.getValue(), dateLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    if (resultObj.getValue().length() > 0) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + dateLinkObj.getValue() + " : " + datesThes1.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

            //termsModified++;
        }
        //</editor-fold>

        //<editor-fold desc="copy editors for term from thes1 with no DATE defined in thes 1">
        pairsEnumMerged = term_Editor_Links_THES1_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + dateKeywordStr + " (editor) counter: " + termsModified + " of " + totalSize + "  ");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;
            String term = pairsEnumMerged.nextElement();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));
            Vector<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            Vector<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
            //Vector<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            //Vector<String> datesThes2   = term_Date_Links_THES2_HASH.get(term);

            if (datesThes1 != null) {
                continue;//copird before
            }
            if (editorsThes1 != null && editorsThes1.size() > 0) {
                //create all editors for this term
                for (int i = 0; i < editorsThes1.size(); i++) {
                    resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes1.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    if (resultObj.getValue().length() > 0) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + editorLinkObj.getValue() + " : " + editorsThes1.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName1 + "." + resultObj.getValue());
                        return false;
                    }
                }
            }

        }
        //</editor-fold>

        //<editor-fold desc="copy dates and relevant editors for terms of thes 2. Done together because it may later be used to compare dates and write pairs editor/date.">
        if (thesaurusName2 != null) {

            pairsEnumMerged = term_Date_Links_THES2_HASH.keys();
            while (pairsEnumMerged.hasMoreElements()) {
                if (termsModified % restartInterval == 0) {
                    if (common_utils != null) {

                        common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                    }
                }
                termsModified++;
                String term = pairsEnumMerged.nextElement();
                StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

                Vector<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
                Vector<String> datesThes1 = term_Date_Links_THES1_HASH.get(term);
                Vector<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
                Vector<String> datesThes2 = term_Date_Links_THES2_HASH.get(term);

                if (editorsThes2 != null && editorsThes2.size() > 0) {

                    for (int i = 0; i < editorsThes2.size(); i++) {
                        if (editorsThes1 == null || editorsThes1.size() == 0 || editorsThes1.contains(editorsThes2.get(i)) == false) {
                            //create editor link
                            resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes2.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                            if (resultObj.getValue().length() > 0) {
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + editorLinkObj.getValue() + " : " + editorsThes2.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName2 + "." + resultObj.getValue());
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
                                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + dateLinkObj.getValue() + " : " + datesThes2.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName2 + "." + resultObj.getValue());
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
        pairsEnumMerged = term_Editor_Links_THES2_HASH.keys();
        while (pairsEnumMerged.hasMoreElements()) {
            if (termsModified % restartInterval == 0) {
                if (common_utils != null) {
                    //Utils.StaticClass.webAppSystemOutPrintln("Restarting Server");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            termsModified++;
            String term = pairsEnumMerged.nextElement();
            StringObject targetTermObj = new StringObject(prefixTerm.concat(term));

            Vector<String> editorsThes1 = term_Editor_Links_THES1_HASH.get(term);
            //Vector<String> datesThes1   = term_Date_Links_THES1_HASH.get(term);
            Vector<String> editorsThes2 = term_Editor_Links_THES2_HASH.get(term);
            Vector<String> datesThes2 = term_Date_Links_THES2_HASH.get(term);

            if (datesThes2 != null) {
                continue;//copird before
            }
            if (editorsThes2 != null && editorsThes2.size() > 0) {
                //create all editors for this term
                for (int i = 0; i < editorsThes2.size(); i++) {
                    if (editorsThes1 == null || editorsThes1.size() == 0 || editorsThes1.contains(editorsThes2.get(i)) == false) {
                        resultObj.setValue(resultObj.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTermObj, prefixPerson.concat(editorsThes2.get(i)), editorFromClassObj.getValue(), editorLinkObj.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                        if (resultObj.getValue().length() > 0) {
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + termsModified + ". Αποτυχία κατά την  προσθήκη των τιμών " + editorLinkObj.getValue() + " : " + editorsThes2.get(i) + " του όρου : '" + term + "' από τον θησαυρό " + thesaurusName2 + "." + resultObj.getValue());
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

    public boolean CopySimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String thesaurusName1, String thesaurusName2, String mergedThesaurusName, OutputStreamWriter logFileWriter, String keyWordStr, Vector<String> skipNodes, StringObject resultObj, int ConsistencyCheckPolicy) throws IOException {
        //Copy Links to EnglishWords, Taxonomic Codes, Sources

        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();

        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();

        Hashtable<String, Vector<String>> term_Links_HASH = new Hashtable<String, Vector<String>>();

        //READ FIRST THESAURUS LINKS.
        UserInfoClass SessionUserInfo = new UserInfoClass(refSessionUserInfo);
        wtmsUsers.UpdateSessionUserSessionAttribute(SessionUserInfo, thesaurusName1);

        Q.reset_name_scope();
        Q.free_all_sets();

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, keyWordStr, fromClassObj, LinkObj, Q, sis_session);

        Q.reset_name_scope();
        if (Q.set_current_node(fromClassObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(fromClassObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopySimpleLinks/ClassReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + fromClassObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            return false;
        }

        if (Q.set_current_node(LinkObj) == QClass.APIFail) {
            /*
             * 
             *
             *
             *
             * HARDCODED, CHANGE IT THROUGH TRANSLATION
             *
             *
             *
             *
             */
            String pathToMessagesXML = Utilities.getMessagesXml();
            StringObject resultMessageObj = new StringObject();
            Vector<String> errorArgs = new Vector<String>();

            errorArgs.add(fromClassObj.getValue());
            errorArgs.add(LinkObj.getValue());
            errorArgs.add(thesaurusName1);
            dbGen.Translate(resultMessageObj, "root/CopySimpleLinks/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
            errorArgs.removeAllElements();
            resultObj.setValue(resultMessageObj.getValue());

            //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " του θησαυρού : " + thesaurusName1 + ".");
            return false;
        }

        int set_all_links = Q.get_instances(0);
        Q.reset_set(set_all_links);

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\t\tREADING " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " Links from " + thesaurusName1 + ".");

        Q.reset_name_scope();

        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if (Q.bulk_return_link(set_all_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {

                String termName = dbGen.removePrefix(row.get_v1_cls());
                String linkValue = dbGen.removePrefix(row.get_v3_cmv().getString());
                Vector<String> otherVals = term_Links_HASH.get(termName);
                if (skipNodes != null && skipNodes.contains(linkValue)) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Παρακάμπτεται η αντιγραφή του " + LinkObj.getValue() + " συνδέσμου : " + linkValue + " για τον όρο : " + termName + " του θησαυρού " + thesaurusName1 + ".");
                    continue; // skip
                }
                if (otherVals == null) {
                    otherVals = new Vector<String>();
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
         Vector<String> otherVals = term_Links_HASH.get(termName);
         if (skipNodes != null && skipNodes.contains(linkValue)) {
         Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Παρακάμπτεται η αντιγραφή του " + LinkObj.getValue() + " συνδέσμου : " + linkValue + " για τον όρο : " + termName + " του θησαυρού " + thesaurusName1 + ".");
         continue; // skip
         }
         if (otherVals == null) {
         otherVals = new Vector<String>();
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
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(fromClassObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopySimpleLinks/ClassReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κλάση " + fromClassObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
                return false;
            }

            if (Q.set_current_node(LinkObj) == QClass.APIFail) {
                /*
                 * 
                 *
                 *
                 *
                 * HARDCODED, CHANGE IT THROUGH TRANSLATION
                 *
                 *
                 *
                 *
                 */
                String pathToMessagesXML = Utilities.getMessagesXml();
                StringObject resultMessageObj = new StringObject();
                Vector<String> errorArgs = new Vector<String>();

                errorArgs.add(fromClassObj.getValue());
                errorArgs.add(LinkObj.getValue());
                errorArgs.add(thesaurusName2);
                dbGen.Translate(resultMessageObj, "root/CopySimpleLinks/CategoryReferenceFailure", errorArgs, pathToMessagesXML);
                errorArgs.removeAllElements();
                resultObj.setValue(resultMessageObj.getValue());

                //resultObj.setValue("Αποτυχία αναφοράς στην κατηγορία " + fromClassObj.getValue() + "->" + LinkObj.getValue() + " του θησαυρού : " + thesaurusName2 + ".");
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
                    Vector<String> otherVals = term_Links_HASH.get(termName);
                    if (skipNodes != null && skipNodes.contains(linkValue)) {

                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Παρακάμπτεται η αντιγραφή του " + LinkObj.getValue() + " συνδέσμου : " + linkValue + " για τον όρο : " + termName + " του θησαυρού " + thesaurusName2 + ".");
                        continue; // skip
                    }
                    if (otherVals == null) {
                        otherVals = new Vector<String>();
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
             Vector<String> otherVals = term_Links_HASH.get(termName);
             if (skipNodes != null && skipNodes.contains(linkValue)) {

             Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Παρακάμπτεται η αντιγραφή του " + LinkObj.getValue() + " συνδέσμου : " + linkValue + " για τον όρο : " + termName + " του θησαυρού " + thesaurusName2 + ".");
             continue; // skip
             }
             if (otherVals == null) {
             otherVals = new Vector<String>();
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

    public boolean CreateSimpleLinks(UserInfoClass refSessionUserInfo, CommonUtilsDBadmin common_utils, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String pathToErrorsXML, String mergedThesaurusName, OutputStreamWriter logFileWriter, String keyWordStr, Vector<String> skipNodes, StringObject resultObj, Hashtable<String, Vector<String>> term_Links_HASH, boolean resolveError, int ConsistencyCheckPolicy) throws IOException {

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
        Enumeration<String> pairsEnumMerged = term_Links_HASH.keys();

        while (pairsEnumMerged.hasMoreElements()) {

            if (linkRelations % restartInterval == 0) {
                if (common_utils != null) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + keyWordStr + " counter: " + linkRelations + " of " + howmany + "   ");
                    //Utils.StaticClass.webAppSystemOutPrintln("RESTARTING SERVER");
                    common_utils.restartTransactionAndDatabase(Q, TA, sis_session, tms_session, mergedThesaurusName);
                }
            }
            linkRelations++;

            String term = pairsEnumMerged.nextElement();
            Vector<String> linkValues = new Vector<String>();
            Vector<String> tempVals = new Vector<String>();
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
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + linkRelations + ". Αποτυχία κατά την  προσθήκη των τιμών " + LinkObj.getValue() + " : " + linkValues.toString() + " του όρου : '" + term + "'." + resultObj.getValue() + ".");
                return false;
            } else {
                //logFileWriter.append(linkRelations + ". Ο όρος : '" + term + "' τροποποιήθηκε με επιτυχία. Προστέθηκαν οι τιμές " + LinkObj.getValue() + " : " + linkValues.toString() + "\r\n");
            }
            //logFileWriter.flush();
            resultObj.setValue("");

        }

        logFileWriter.flush();
        return true;

    }

    public String getDefaultFacet(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String mergedThesaurusName) {

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

        Vector<String> defaultClassName = new Vector<String>();
        defaultClassName.addAll(dbGen.get_Node_Names_Of_Set(set_classes, true, Q, sis_session));
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

    private Vector<String> CollectErrorProneUFTranslations(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2, OutputStreamWriter logFileWriter) throws IOException {
        /*
         * case 1: ens of thes 1 <-> uk_ufs of thes1
         * case 2: ens of thes 2 <-> uk_ufs of thes2
         * case 3: ens of thes 1 <-> uk_ufs of thes2
         * case 4: ens of thes 2 <-> uk_ufs of thes1
         */
        Vector<String> results = new Vector<String>();
        Vector<String> ensThes1 = new Vector<String>();
        Vector<String> uk_ufsThes1 = new Vector<String>();
        Vector<String> ensThes2 = new Vector<String>();
        Vector<String> uk_ufsThes2 = new Vector<String>();

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

            Vector<String> referencesToNode = new Vector<String>();
            referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes1, true, Q, sis_session));
            Q.free_set(set_WrongUFTranslationsOfThes1);
            Q.free_set(set_WrongUFTranslationssOfThes1_labels);

            for (int k = 0; k < referencesToNode.size(); k++) {
                logFileWriter.append("\r\n<targetTerm>");
                logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                logFileWriter.append("<errorValue>" + Utilities.escapeXML(results.get(i)) + "</errorValue>");
                logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του UF συνδέσμου : " + Utilities.escapeXML(results.get(i)) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό " + thesaurusName1 + ". Το όνομα αυτό χρησιμοποιείται για ΑΟ του θησαυρού " + thesaurusName1 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή UF συνδέσμου στον νέο θησαυρό.</reason>");
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

            Vector<String> results2 = new Vector<String>();
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

                Vector<String> referencesToNode = new Vector<String>();
                referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes2, true, Q, sis_session));
                Q.free_set(set_WrongUFTranslationsOfThes2);
                Q.free_set(set_WrongUFTranslationsOfThes2_labels);

                for (int k = 0; k < referencesToNode.size(); k++) {
                    logFileWriter.append("\r\n<targetTerm>");
                    logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                    logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(results2.get(i)) + "</errorValue>");
                    logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του UF συνδέσμου : " + Utilities.escapeXML(results2.get(i)) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό " + thesaurusName2 + ". Το όνομα αυτό χρησιμοποιείται για ΑΟ του θησαυρού " + thesaurusName2 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή UF συνδέσμου στον νέο θησαυρό.</reason>");
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

                    Vector<String> referencesToNode = new Vector<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes2, true, Q, sis_session));
                    Q.free_set(set_WrongUFTranslationsOfThes2);
                    Q.free_set(set_WrongUFTranslationsOfThes2_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του UF συνδέσμου : " + Utilities.escapeXML(SearchKwd) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό: " + thesaurusName2 + ". Το όνομα αυτό χρησιμοποιείται για AO του θησαυρού " + thesaurusName1 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή UF συνδέσμου στον νέο θησαυρό.</reason>");
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

                    Vector<String> referencesToNode = new Vector<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFTranslationsOfThes1, true, Q, sis_session));
                    Q.free_set(set_WrongUFTranslationsOfThes1);
                    Q.free_set(set_WrongUFTranslationsOfThes1_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του UF συνδέσμου : " + Utilities.escapeXML(SearchKwd) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό: " + thesaurusName1 + ". Το όνομα αυτό χρησιμοποιείται για AO του θησαυρού " + thesaurusName2 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για την περιγραφή UF συνδέσμου στον νέο θησαυρό.</reason>");
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

    private Vector<String> CollectErrorProneUfs(UserInfoClass refSessionUserInfo, QClass Q, IntegerObject sis_session, String thesaurusName1, String thesaurusName2, OutputStreamWriter logFileWriter) throws IOException {

        /*
         * case 1: terms of thes 1 <-> ufs of thes1
         * case 2: terms of thes 2 <-> ufs of thes2
         * case 3: terms of thes 1 <-> ufs of thes2
         * case 4: terms of thes 2 <-> ufs of thes1
         */
        Vector<String> results = new Vector<String>();
        Vector<String> termsThes1 = new Vector<String>();
        Vector<String> ufsThes1 = new Vector<String>();
        Vector<String> termsThes2 = new Vector<String>();
        Vector<String> ufsThes2 = new Vector<String>();

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

            Vector<String> referencesToNode = new Vector<String>();
            referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes1, true, Q, sis_session));
            Q.free_set(set_WrongUFsOfThes1);
            Q.free_set(set_WrongUFsOfThes1_labels);

            for (int k = 0; k < referencesToNode.size(); k++) {
                logFileWriter.append("\r\n<targetTerm>");
                logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                logFileWriter.append("<errorValue>" + Utilities.escapeXML(results.get(i)) + "</errorValue>");
                logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του XA συνδέσμου : " + Utilities.escapeXML(results.get(i)) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + ". Το όνομα αυτό χρησιμοποιείται για όρο του θησαυρού " + thesaurusName1 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή αδόκιμου όρου στον νέο θησαυρό.</reason>");
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

            Vector<String> results2 = new Vector<String>();
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

                Vector<String> referencesToNode = new Vector<String>();
                referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes2, true, Q, sis_session));
                Q.free_set(set_WrongUFsOfThes2);
                Q.free_set(set_WrongUFsOfThes2_labels);

                for (int k = 0; k < referencesToNode.size(); k++) {
                    logFileWriter.append("\r\n<targetTerm>");
                    logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                    logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                    logFileWriter.append("<errorValue>" + Utilities.escapeXML(results2.get(i)) + "</errorValue>");
                    logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του XA συνδέσμου : " + Utilities.escapeXML(results2.get(i)) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + ". Το όνομα αυτό χρησιμοποιείται για όρο του θησαυρού " + thesaurusName2 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή αδόκιμου όρου στον νέο θησαυρό.</reason>");
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

                    Vector<String> referencesToNode = new Vector<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes2, true, Q, sis_session));
                    Q.free_set(set_WrongUFsOfThes2);
                    Q.free_set(set_WrongUFsOfThes2_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του XA συνδέσμου : " + Utilities.escapeXML(SearchKwd) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό: " + thesaurusName2 + ". Το όνομα αυτό χρησιμοποιείται για όρο του θησαυρού " + thesaurusName1 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή αδόκιμου όρου στον νέο θησαυρό.</reason>");
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

                    Vector<String> referencesToNode = new Vector<String>();
                    referencesToNode.addAll(dbGen.get_Node_Names_Of_Set(set_WrongUFsOfThes1, true, Q, sis_session));
                    Q.free_set(set_WrongUFsOfThes1);
                    Q.free_set(set_WrongUFsOfThes1_labels);

                    for (int k = 0; k < referencesToNode.size(); k++) {
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(referencesToNode.get(k)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(SearchKwd) + "</errorValue>");
                        logFileWriter.append("<reason>Παρακάμφθηκε η αντιγραφή του XA συνδέσμου : " + Utilities.escapeXML(SearchKwd) + " για τον όρο : " + Utilities.escapeXML(referencesToNode.get(k)) + " από τον θησαυρό: " + thesaurusName1 + ". Το όνομα αυτό χρησιμοποιείται για όρο του θησαυρού " + thesaurusName2 + " και δεν μπορεί ταυτόχρονα να χρησιμοποιηθεί για τη περιγραφή αδόκιμου όρου στον νέο θησαυρό.</reason>");
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
}
