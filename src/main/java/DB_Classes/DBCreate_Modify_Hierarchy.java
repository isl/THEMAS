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
package DB_Classes;

import Users.DBFilters;
import Users.UserInfoClass;
import Utils.ConsistensyCheck;
import Utils.ConstantParameters;

import Utils.Parameters;
import Utils.StaticClass;
import Utils.Utilities;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tzortzak
 */
public class DBCreate_Modify_Hierarchy {

    /*---------------------------------------------------------------------
    DBCreate_Modify_Hierarchy
    -----------------------------------------------------------------------
    general class with methods used by Create_Modify_Hierarchy servlet
    ----------------------------------------------------------------------*/
    /*
    QClass Q = new QClass();
    DBGeneral dbGen;
    HttpServlet ServletCaller;
    
    DBConnect_Hierarchy dbConH;
    DBRemove_Hierarchy dbRemH;
    StringObject errorMsg;
    IntegerObject sis_session;
    IntegerObject tms_session;
    boolean CreationOrModificationSucceded;*/
    // constats used by this class methods
    public static final int DB_CREATE = 0;
    public static final int DB_MODIFY = 1;
    public static final String hierarchy_delete_kwd = "delete_hierarchy";

    /*----------------------------------------------------------------------
    Constructor of DBCreate_Modify_Hierarchy
    -----------------------------------------------------------------------*/
    public DBCreate_Modify_Hierarchy() {
        /*
        ServletCaller = caller;
        dbConH = new DBConnect_Hierarchy(selectedThesaurus,ServletCaller, sisSession, tmsSession);
        dbGen = new DBGeneral();
        dbRemH = new DBRemove_Hierarchy(selectedThesaurus,ServletCaller, sisSession, tmsSession);
        errorMsg = new StringObject("");
        sis_session = sisSession;
        tms_session = tmsSession;
        
        CreationOrModificationSucceded = true;*/
    }

    /*---------------------------------------------------------------------
    Create_Or_ModifyHierarchy()
    -----------------------------------------------------------------------
    INPUT: 
    - String targetHierarchy: the name of the Hierarchy to be created / modified (no prefix)
    - String targetHierarchyFacet: the name of the Facet(s) that Hierarchy to be created / modified 
    (no prefix) belongs to. Should become a vector or something like a comma seperated list
    - String createORmodify: "create" or "modify" determines if the 
    targetHierarchy is going to be created or modified
    - String deletionOperator: "delete" or null in case of modification, determines 
    if the targetHierarchy is going to be deleted/(undo)abandoned
    
    OUTPUT: a String with the result of the creation / modification
    FUNCTION: creates / modifies the given Hierarchy
    CALLED BY: Create_Modify_Hierarchy servlet
    ----------------------------------------------------------------------*/
    public boolean Create_Or_ModifyHierarchy(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            DBGeneral dbGen, String targetHierarchy, Vector<String> targetHierarchyFacets,/* Vector targetHierarchyLetterCodes,*/
            String createORmodify, String deletionOperator, String userName, Locale targetLocale, StringObject errorMsg, boolean updateHistoricalData, String pathToMessagesXML) {


        int SISApiSession = sis_session.getValue();

        DBConnect_Hierarchy dbConH = new DBConnect_Hierarchy();
        DBRemove_Hierarchy dbRemH = new DBRemove_Hierarchy();
        DBConnect_Term dbCon = new DBConnect_Term();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject modifiedOnClass = new StringObject();
        StringObject modifiedOnLink = new StringObject();
        StringObject modifiedByClass = new StringObject();
        StringObject modifiedByLink = new StringObject();

        StringObject createdOnClass = new StringObject();
        StringObject createdOnLink = new StringObject();
        StringObject createdByClass = new StringObject();
        StringObject createdByLink = new StringObject();

        Q.reset_name_scope();

        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClass, modifiedByLink, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClass, modifiedOnLink, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_by_kwd, createdByClass, createdByLink, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClass, createdOnLink, Q, sis_session);

        //Get Prefixes that will be needed
        String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        String topTermPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        String editor_Prefix = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());

        // convert target Hierarchy to DB encoding with prefix
        StringObject targetHierarchyObj = new StringObject(prefix.concat(targetHierarchy));
        StringObject targetTopTermObj = new StringObject(topTermPrefix.concat(targetHierarchy));

        StringObject errorMsgPrefix = new StringObject();
        if (createORmodify.equals("create")) {
            dbGen.Translate(errorMsgPrefix, "root/EditHierarchy/Creation/ErrorPrefix", null, pathToMessagesXML);
        } else {
            dbGen.Translate(errorMsgPrefix, "root/EditHierarchy/Edit/ErrorPrefix", null, pathToMessagesXML);
        }


        /*
        try {
            byte[] byteArray = targetHierarchy.getBytes("UTF-8");

            int maxHierarchyChars = dbtr.getMaxBytesForHierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session);
            if (byteArray.length > maxHierarchyChars) {
                
                Vector<String> errorArgs = new Vector<String>();
                errorArgs.add("" + maxHierarchyChars);
                errorArgs.add("" + byteArray.length);
                dbGen.Translate(errorMsg, "root/EditHierarchy/Edit/LongName", errorArgs, pathToMessagesXML);

                return false;
            }
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOut(ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }*/


        Q.reset_name_scope();
        Vector<String> errorArgs = new Vector<String>();

        if (createORmodify.equals("create") == false && Q.set_current_node(targetHierarchyObj) == QClass.APIFail) {

            errorArgs.add(targetHierarchy);
            dbGen.Translate(errorMsg, "root/EditHierarchy/Edit/HierarchyNotFound", errorArgs, pathToMessagesXML);
            errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());

            //errorMsg.setValue(errorMsg.getValue().concat(errorMsgPrefix.getValue() + "Η ιεραρχία " + targetHierarchy +" δεν βρέθηκε στην βάση."));
            return false;
        }

        int KindOfHierarchy = dbGen.GetKindOfHierarchy(SessionUserInfo.selectedThesaurus, targetHierarchyObj, Q, sis_session);

        if (createORmodify.equals("create")) { // create	 

            //During creation of new hierarchy one and only one parent facet may be declared
            if (targetHierarchy == null || targetHierarchy.trim().length() == 0) {
                dbGen.Translate(errorMsg, "root/EditHierarchy/Creation/EmptyName", null, pathToMessagesXML);
                errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
                //errorMsg.setValue("Δεν έχει δηλωθεί όνομα για την ιεραρχία.");
                return false;
            }
            if (targetHierarchyFacets == null || targetHierarchyFacets.size() == 0) {
                dbGen.Translate(errorMsg, "root/EditHierarchy/Creation/NoFacet", null, pathToMessagesXML);
                errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
                //errorMsg.setValue("Η δήλωση ενός τουλάχιστον μικροθησαυρού είναι υποχρεωτική για την δημιουργία της ιεραρχίας.");
                return false;
            }
            Q.reset_name_scope();
            if (Q.set_current_node(targetHierarchyObj) != QClass.APIFail) {

                errorArgs.add(targetHierarchy);
                dbGen.Translate(errorMsg, "root/EditHierarchy/Creation/AlreadyinDB", errorArgs, pathToMessagesXML);
                errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
                //errorMsg.setValue("Το όνομα αυτό υπάρχει ήδη στην βάση είτε σαν ιεραρχία είτε σαν μικροθησαυρός.");
                return false;
            }

            Q.reset_name_scope();
            if (Q.set_current_node(targetTopTermObj) != QClass.APIFail) {

                errorArgs.add(targetHierarchy);
                dbGen.Translate(errorMsg, "root/EditHierarchy/Creation/TermAlreadyinDB", errorArgs, pathToMessagesXML);
                errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());

                //errorMsg.setValue("Το όνομα αυτό υπάρχει ήδη στην βάση σαν όρος με αποτέλεσμα να μην μπορεί να δημιουργηθεί ο αντίστοιχος ΟΚ.");
                return false;
            }

            Q.reset_name_scope();

            StringObject targetHierarchyFacetObj = new StringObject(prefix.concat(targetHierarchyFacets.get(0).toString()));
            errorMsg.setValue(errorMsg.getValue().concat(dbConH.ConnectHierarchy(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, targetHierarchyObj, targetHierarchyFacetObj, pathToMessagesXML)));


            if (updateHistoricalData) {

                // FILTER default status for term creation depending on user group
                DBFilters dbf = new DBFilters();
                dbCon.CreateModifyStatus(SessionUserInfo.selectedThesaurus, targetTopTermObj, dbf.GetDefaultStatusForTermCreation(SessionUserInfo), Q, TA, sis_session, tms_session, dbGen, errorMsg);


                //Also update creation info of top terms
                errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetTopTermObj, editor_Prefix.concat(userName), createdByClass.getValue(), createdByLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectTime(SessionUserInfo.selectedThesaurus, targetTopTermObj, createdOnClass.getValue(), createdOnLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
            }

        } else { // modify	

            if (deletionOperator != null) { // delete / (undo) abandon descriptor

                if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_NEW) { // new descriptor => delete 

                    if (checkTopTermDependencies(SessionUserInfo.selectedThesaurus, Q, sis_session, errorMsg, targetHierarchy, pathToMessagesXML) == true) {

                        StringObject taxonomicCodeFromClass = new StringObject();
                        StringObject taxonomicCodeLink = new StringObject();
                        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.tc_kwd, taxonomicCodeFromClass, taxonomicCodeLink, Q, sis_session);

                        /*Code should be enabled if we want hiers to be deleted even though top terms may hava scope notes historical notes or comments
                         *Code should also be enabled in function checkTopTermDependencies 
                         */
                        /*
                        StringObject scopenoteFromClassObj = new StringObject();
                        StringObject scopenoteLinkObj = new StringObject();
                        dbGen.getKeywordPair(ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj);
                        StringObject commentFromClassObj = new StringObject();
                        StringObject commentLinkObj = new StringObject();
                        dbGen.getKeywordPair(ConstantParameters.comment_kwd, commentFromClassObj, commentLinkObj);
                        StringObject historicalnoteFromClassObj = new StringObject();
                        StringObject historicalnoteLinkObj = new StringObject();
                        dbGen.getKeywordPair(ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj);

                        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session,tms_session);
                        WTA.DeleteDescriptorComment(targetTopTermObj, scopenoteFromClassObj, scopenoteLinkObj);
                        WTA.DeleteDescriptorComment(targetTopTermObj, commentFromClassObj, commentLinkObj);
                        WTA.DeleteDescriptorComment(targetTopTermObj, historicalnoteFromClassObj, historicalnoteLinkObj);
                         */
                        dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetHierarchy, ConstantParameters.FROM_Direction, taxonomicCodeFromClass.getValue(), taxonomicCodeLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

                        dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetHierarchy, ConstantParameters.FROM_Direction, modifiedByClass.getValue(), modifiedByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                        dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetHierarchy, ConstantParameters.FROM_Direction, modifiedOnClass.getValue(), modifiedOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

                        dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetHierarchy, ConstantParameters.FROM_Direction, createdByClass.getValue(), createdByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                        dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetHierarchy, ConstantParameters.FROM_Direction, createdOnClass.getValue(), createdOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

                        errorMsg.setValue(errorMsg.getValue().concat(dbRemH.DeleteHierarchy(Q, TA, sis_session, tms_session, dbGen, targetHierarchyObj)));

                    }

                } else { // released / obsolete descriptor => (undo) abandon
                    //CreationOrModificationSucceded = true;
                    if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_OBSOLETE) { // obsolete descriptor => undo abandon
                        // convert BT_for_undo_abandon to DB encoding with prefix
                        errorMsg.setValue(dbRemH.UndoAbandonHierarchy(TA, tms_session, dbGen, targetHierarchyObj));
                    }
                    if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_RELEASED) { // released descriptor => abandon
                        errorMsg.setValue(dbRemH.AbandonHierarchy(TA, tms_session, dbGen, targetHierarchyObj));
                    }

                }

            } else { // NO deletion - detect modifications

                if (targetHierarchyFacets.size() == 0) {
                    //errorMsg = " Every hierarchy should have at least one parent Facet. Modification Aborted";
                    dbGen.Translate(errorMsg, "root/EditHierarchy/Edit/NoFacet", null, pathToMessagesXML);
                    errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
                    //errorMsg.setValue(" Κάθε ιεραρχία πρέπει να υπάγεται σε έναν τουλάχιστον μικροθησαυρό. Ακύρωση τροποποίησης.");
                    return false;
                } else {

                    //letter code modification and parent facets modifications should be handled 
                    // modifyLetterCodes(targetHierarchy, targetHierarchyLetterCodes);

                    modifyFacets(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, targetHierarchy, targetHierarchyFacets, targetLocale, errorMsg);
                }

            }

        }

        if (errorMsg.getValue().equals("") == false) { // case of error

            errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
            return false;

        } else { // case of NO error
            //CreationOrModificationSucceded = true;
            // end transaction
            //Q.end_transaction();
            if (createORmodify.equals("create")) {
                //errorMsg.setValue("Η ιεραρχία : '" + targetHierarchy + "' δημιουργήθηκε με επιτυχία.");
                return true;
            } else { // modify
                if (deletionOperator != null) { // delete / (undo) abandon descriptor


                    String message = "";

                    if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_NEW) {
                        //message = "Η ιεραρχία : '" + targetHierarchy + "' διαγράφηκε με επιτυχία.";
                    }
                    if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_OBSOLETE) {
                        //message = "Για την ιεραρχία : '" + targetHierarchy + "' ολοκληρώθηκε η αναίρεση της κατάργησής της.";
                    }
                    if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_RELEASED) {
                        //message = "Η ιεραρχία : '" + targetHierarchy + "' καταργήθηκε με επιτυχία.";
                    }
                    errorMsg.setValue(message);
                    return true;
                } else {
                    //errorMsg.setValue("Η ιεραρχία : '" + targetHierarchy + "' τροποποιήθηκε με επιτυχία.");
                    return true;
                }
            }
        }

    }

    /*---------------------------------------------------------------------
    DeleteHierarchy()
    -----------------------------------------------------------------------
    INPUT: - String targetHierarchy: the name of the Hierarchy to be created / modified (no prefix)
    OUTPUT: StringObject errorMsg: with the result of the deletion
    FUNCTION: in case of:
    - HIERARCHY_OF_KIND_NEW: deletes the target hierarchy and all of its terms
    - HIERARCHY_OF_KIND_RELEASED: abandons the target hierarchy
    - HIERARCHY_OF_KIND_OBSOLETE: undo abandons the target hierarchy
    CALLED BY: EditActions_Hierarchy servlet
    ----------------------------------------------------------------------*/
    public void DeleteHierarchy(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            IntegerObject tms_session, DBGeneral dbGen,
            String targetHierarchy, StringObject errorMsg) {

        String pathToConsistencyErrorsXML = Utilities.getTranslationsXml("Consistencies_Error_Codes.xml");
        String pathToMessagesXML = Utilities.getMessagesXml();
        StringObject errorMsgPrefixObj = new StringObject();

        dbGen.Translate(errorMsgPrefixObj, "root/EditHierarchy/Edit/ErrorPrefix", null, pathToMessagesXML);
        Vector<String> errorArgs = new Vector<String>();

        int ret = 0;
        int SISApiSession = sis_session.getValue();
        DBRemove_Hierarchy dbRemH = new DBRemove_Hierarchy();
        DBConnect_Term dbCon = new DBConnect_Term();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        // looking for prefix AAAClass`
        String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, SISApiSession);
        // looking for prefix AAAEL`
        String topTermPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, SISApiSession);
        // convert target Hierarchy to DB encoding with prefix
        StringObject targetHierarchyObj = new StringObject(prefix.concat(targetHierarchy));
        StringObject targetTopTermObj = new StringObject(topTermPrefix.concat(targetHierarchy));
        // looking for AAAHierarchy
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesHierarchy);
        // looking for AAATopTerm
        StringObject thesTopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesTopTerm);
        // looking for AAADescriptor
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesDescriptor);
        // looking for AAAClass`Ορφανοί όροι
        StringObject orphansHierarchyObj = new StringObject(prefix.concat(Parameters.UnclassifiedTermsLogicalname));
        // looking for AAAEL`Ορφανοί όροι
        StringObject orphansHierarchyTopTermObj = new StringObject(orphansHierarchyObj.getValue().replaceFirst(prefix, topTermPrefix));
        // looking for AAA_BT
        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesBT);

        // check if target hierarchy exists
        Q.reset_name_scope();
        if (Q.set_current_node(targetHierarchyObj) == QClass.APIFail) {

            errorArgs.add(targetHierarchy);
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/HierarchyNotFound", errorArgs, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Η ιεραρχία " + targetHierarchy +" δεν βρέθηκε στην βάση."));
            return;
        }
        // check if target hierarchy to be deleted is the AAAEL`Ορφανοί όροι
        if (targetHierarchyObj.getValue().compareTo(orphansHierarchyObj.getValue()) == 0) {
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/UnclassifiedTermsHierarchy", null, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Η συγκεκριμένη ιεραρχία δεν μπορεί να διαγραφεί. Σε αυτήν κατατάσσονται όλοι οι νέοι όροι που δημιουργούνται."));
            return;
        }
        // get the kind of the target hierarchy
        int KindOfHierarchy = dbGen.GetKindOfHierarchy(SessionUserInfo.selectedThesaurus, targetHierarchyObj, Q, sis_session);
        // in case of released hierarchy => abandon it
        if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_RELEASED) {
            errorMsg.setValue(dbRemH.AbandonHierarchy(TA, tms_session, dbGen, targetHierarchyObj));
            // check the case of error
            if (errorMsg.getValue().equals("") == false) {
                errorMsg.setValue(errorMsgPrefixObj.getValue() + errorMsg.getValue());
            }
            return;
        }
        // in case of obsolete hierarchy => undo abandon it
        if (KindOfHierarchy == ConstantParameters.HIERARCHY_OF_KIND_OBSOLETE) {
            errorMsg.setValue(dbRemH.UndoAbandonHierarchy(TA, tms_session, dbGen, targetHierarchyObj));
            // check the case of error
            if (errorMsg.getValue().equals("") == false) {
                errorMsg.setValue(errorMsgPrefixObj.getValue() + errorMsg.getValue());
            }
            return;
        }

        // case of new hierarchy => delete it  
        // ------------------------------------------------------------------------------------------
        // 0. get the the TopTerm of target hierarchy
        // ------------------------------------------------------------------------------------------ 
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        int termsOfHierarchy = Q.get_all_instances(0);
        
        Vector<String> allHierarchyTermNames = new Vector<String>();
        Vector<Return_Nodes_Row> retAllHierarchyTermRows = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(termsOfHierarchy, retAllHierarchyTermRows)==QClass.APIFail){
            errorMsg.setValue("Deletion Failed");
            Utils.StaticClass.webAppSystemOutPrintln("Deletion failed in DeleteHierarchy() of DB_Create_Modify_Hierarchy while trying to READ all nodes belonging to hierarchy: " + targetHierarchyObj.getValue());
            return;
        }
        for(Return_Nodes_Row row: retAllHierarchyTermRows){
            allHierarchyTermNames.add(row.get_v1_cls_logicalname());
        }
        
        Q.reset_name_scope();
        Q.set_current_node( thesTopTerm);
        int TopTermsSet = Q.get_all_instances( 0);
        Q.reset_set(TopTermsSet);
        Vector<String> allTopTerms = new Vector<String>();
        Vector<Return_Nodes_Row> retTopTermRows = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(TopTermsSet, retTopTermRows)==QClass.APIFail){
            errorMsg.setValue("Deletion Failed");
            Utils.StaticClass.webAppSystemOutPrintln("Deletion failed in DeleteHierarchy() of DB_Create_Modify_Hierarchy while trying to READ Top term Nodes");
            return;
        }
        for(Return_Nodes_Row row: retTopTermRows){
            allTopTerms.add(row.get_v1_cls_logicalname());
        }
        
        Q.set_intersect(TopTermsSet, termsOfHierarchy);
        Q.free_set(termsOfHierarchy);
        Q.reset_set(TopTermsSet);
        StringObject topTermOfTargetHierarchy = new StringObject();
        Q.return_nodes(TopTermsSet, topTermOfTargetHierarchy);
        Q.free_set(TopTermsSet);
        // ------------------------------------------------------------------------------------------
        // 1. get the terms of target hierarchy and for those that belong to other hierachies also,
        //    de-instantiate them from target hierarchy
        // ------------------------------------------------------------------------------------------        
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 1. ------------------------------------------------------------------------------------------");
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        termsOfHierarchy = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"termsOfHierarchy card = " + Q.set_get_card(termsOfHierarchy));
        // get a set with ALL Hierarchies except the target
        int setWithTargetHierarchy = Q.set_get_new();
        Q.set_put(setWithTargetHierarchy);
        Q.reset_name_scope();
        Q.set_current_node(thesHierarchy);
        int hierarchiesSet = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"hierarchiesSet card = " + Q.set_get_card(hierarchiesSet));
        Q.set_difference(hierarchiesSet, setWithTargetHierarchy);
        int hierarchiesSetWithoutTarget = hierarchiesSet;
        Q.free_set(setWithTargetHierarchy);
        Q.reset_set(hierarchiesSetWithoutTarget);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"hierarchiesSetWithoutTarget card = " + Q.set_get_card(hierarchiesSetWithoutTarget));
        // get the terms belonging to ALL Hierarchies except the target
        int termsOfHierarchiesSetWithoutTarget = Q.get_all_instances(hierarchiesSetWithoutTarget);
        Q.free_set(hierarchiesSetWithoutTarget);
        // terms of target hierarchy belonging to other hierachies also = 
        // intersection(termsOfHierarchy, termsOfHierarchiesSetWithoutTarget)
        Q.set_intersect(termsOfHierarchy, termsOfHierarchiesSetWithoutTarget);
        // Q.free_set(termsOfHierarchiesSetWithoutTarget); - do NOT free this set here. It is used at step 2 also
        int termsOfHierarchyBelongingToOtherHierarchiesAlso = termsOfHierarchy;
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"termsOfHierarchyBelongingToOtherHierarchiesAlso card = " + Q.set_get_card(termsOfHierarchyBelongingToOtherHierarchiesAlso));
        // de-instantiate them from target hierarchy
        
        DBCreate_Modify_Term DBCMT = new DBCreate_Modify_Term();
        if(Q.set_get_card(termsOfHierarchyBelongingToOtherHierarchiesAlso)>0){
            //ELIAS BugFix 2015-09-24 delete instance Set is not enough. The Bts that justified this should also be deleted
            //ret = Q.CHECK_IMPROVE_Delete_Instance_Set(termsOfHierarchyBelongingToOtherHierarchiesAlso, new Identifier(targetHierarchyObj.getValue()));
            Vector<String> termsBelongingToOtherHierarchyAlso = new Vector<String>();
            Vector<Return_Nodes_Row> retRows = new Vector<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(termsOfHierarchyBelongingToOtherHierarchiesAlso, retRows)==QClass.APIFail){
                errorMsg.setValue("Deletion Failed");
                Utils.StaticClass.webAppSystemOutPrintln("Deletion failed in DeleteHierarchy() of DB_Create_Modify_Hierarchy while trying to READ terms belonging to other hierarchies also");
                return;
            }
            for(Return_Nodes_Row row: retRows){
                String termBelongingToOtherHierarchiesAlso = row.get_v1_cls_logicalname();
                Vector<String> bts = dbGen.returnResults(SessionUserInfo, dbGen.removePrefix(termBelongingToOtherHierarchiesAlso), 
                        ConstantParameters.bt_kwd, Q, TA, sis_session);
                bts.removeAll(allHierarchyTermNames);
                
                if(bts.size()>0){
                    DBCMT.commitTermTransaction(SessionUserInfo, dbGen.removePrefix(termBelongingToOtherHierarchiesAlso),ConstantParameters.bt_kwd,
                            bts,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToConsistencyErrorsXML,false,false,null,ConsistensyCheck.EDIT_TERM_POLICY);
                    
                     if(errorMsg.getValue() != null && errorMsg.getValue().length()>0){
                         Utils.StaticClass.webAppSystemOutPrintln(errorMsg.getValue());
                         errorMsg.setValue("Deletion Failed");
                        
                        return;
                     }
                }
                
                //set                
            }
            
        }
        
        
        Q.free_set(termsOfHierarchyBelongingToOtherHierarchiesAlso);
        // ------------------------------------------------------------------------------------------                
        // 2. get the rest terms of target hierarchy and for those that have links from/to terms 
        //    that belong to other hierachies, de-instantiate them from target hierarchy and
        //    instantiate them to UnclassifiedTermsLogicalname (AAAClass`Ορφανοί όροι)
        // ------------------------------------------------------------------------------------------                
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 2. ------------------------------------------------------------------------------------------");
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        termsOfHierarchy = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"termsOfHierarchy card = " + Q.set_get_card(termsOfHierarchy));         
        Q.reset_set(termsOfHierarchiesSetWithoutTarget);
        int termsPointedByTermsOfHierarchiesSetWithoutTarget = Q.get_to_node(termsOfHierarchiesSetWithoutTarget);
        int termsPointingToTermsOfHierarchiesSetWithoutTarget = Q.get_from_node(termsOfHierarchiesSetWithoutTarget);
        Q.set_union(termsPointedByTermsOfHierarchiesSetWithoutTarget, termsPointingToTermsOfHierarchiesSetWithoutTarget);
        Q.free_set(termsPointingToTermsOfHierarchiesSetWithoutTarget);
        int termsReferredByTermsOfHierarchiesSetWithoutTarget = termsPointedByTermsOfHierarchiesSetWithoutTarget;
        Q.set_intersect(termsReferredByTermsOfHierarchiesSetWithoutTarget, termsOfHierarchy);
        int termsOfHierarchyReferredByTermsOfOtherHierarchies = termsReferredByTermsOfHierarchiesSetWithoutTarget;
        
        
        if(Q.set_get_card(termsOfHierarchyReferredByTermsOfOtherHierarchies)>0){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"termsOfHierarchyReferredByTermsOfOtherHierarchies card = " + Q.set_get_card(termsOfHierarchyReferredByTermsOfOtherHierarchies));
            
            //ELIAS BUGFIX: 2015-09-24
            // instead of just add,delete instances move to hierarchy should be performed in
            // in order to also redirect the BT links that justify the new instance of link
            /*
            ret = Q.CHECK_IMPROVE_Add_Instance_Set(termsOfHierarchyReferredByTermsOfOtherHierarchies, new Identifier(orphansHierarchyObj.getValue()));
            // de-instantiate them from target hierarchy
            ret = Q.CHECK_IMPROVE_Delete_Instance_Set(termsOfHierarchyReferredByTermsOfOtherHierarchies, new Identifier(targetHierarchyObj.getValue()));
            */
            Vector<Return_Nodes_Row> retNodes = new Vector<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(termsOfHierarchyReferredByTermsOfOtherHierarchies, retNodes)==QClass.APIFail){
                errorMsg.setValue("Deletion Failed");
                Utils.StaticClass.webAppSystemOutPrintln("Deletion failed in DeleteHierarchy() of DB_Create_Modify_Hierarchy while trying to READ the nodes  that will be moved under Unclassified terms");
                return;
            }
            
            for(Return_Nodes_Row row : retNodes){
                StringObject targetNode = new StringObject(row.get_v1_cls_logicalname());
                if(row.get_v1_cls_logicalname().equals("ALLMERGEDEL`μεταλλουργικός εξοπλισμός")){
                    System.out.println("DEBUG");
                }
                if(allTopTerms.contains(targetNode.getValue())==false){
                /*
                    
                    Vector<String> bts = new Vector<String>();
                    bts.add(dbGen.removePrefix(orphansHierarchyTopTermObj.getValue()));
                    
                    DBCMT.commitTermTransaction(SessionUserInfo, dbGen.removePrefix(targetNode.getValue()),ConstantParameters.bt_kwd,
                            bts,SessionUserInfo.name,errorMsg, Q,sis_session,TA,tms_session,dbGen, pathToConmsistensyErrorsXML,false,false,null,ConsistensyCheck.EDIT_TERM_POLICY);
                    
                     if(errorMsg.getValue() != null && errorMsg.getValue().length()>0){
                         Utils.StaticClass.webAppSystemOutPrintln(errorMsg.getValue());
                         errorMsg.setValue("Deletion Failed");
                        
                        return;
                     }
                */
                    
                    ret = TA.CHECK_MoveToHierarchy(targetNode, targetHierarchyObj, orphansHierarchyObj, orphansHierarchyTopTermObj, TMSAPIClass.MOVE_NODE_ONLY);
                    if(ret==TMSAPIClass.TMS_APIFail){
                        Utils.StaticClass.webAppSystemOutPrintln(errorMsg.getValue());
                        errorMsg.setValue("Deletion Failed");
                        
                        Utils.StaticClass.webAppSystemOutPrintln("Deletion failed in DeleteHierarchy() of DB_Create_Modify_Hierarchy while trying to move node "+targetNode+" under Unclassified terms");
                        
                        return;
                    }
                }
            }
            
            

        }
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"termsOfHierarchyReferredByTermsOfOtherHierarchies card = " + Q.set_get_card(termsOfHierarchyReferredByTermsOfOtherHierarchies));
        // instantiate them to UnclassifiedTermsLogicalname (AAAClass`Ορφανοί όροι)
        
        

        Q.free_set(termsOfHierarchyReferredByTermsOfOtherHierarchies);
        Q.free_set(termsOfHierarchy);
        // ------------------------------------------------------------------------------------------                
        // 3. get the rest terms of target hierarchy and delete them 
        // ------------------------------------------------------------------------------------------                
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 3. ------------------------------------------------------------------------------------------");        
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        termsOfHierarchy = Q.get_all_instances(0);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"termsOfHierarchy card = " + Q.set_get_card(termsOfHierarchy));
        // delete them
        Vector<String> termsToBeDeleted = new Vector<String>();
        Q.reset_set(termsOfHierarchy);
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
	if(Q.bulk_return_nodes(termsOfHierarchy, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                termsToBeDeleted.add(row.get_v1_cls_logicalname());
            }
        }
        /*StringObject name = new StringObject();
        while (Q.retur_nodes(termsOfHierarchy, name) != QClass.APIFail) {
            termsToBeDeleted.add(name.getValue());
        }*/
        Q.free_set(termsOfHierarchy);
        termsToBeDeleted.remove(topTermOfTargetHierarchy.getValue());//delete top term last
        int termsToBeDeletedSize = termsToBeDeleted.size();
        for (int i = 0; i < termsToBeDeletedSize; i++) {
            
            String termToBeDeleted = (String) termsToBeDeleted.get(i);
            String termToBeDeletedUIWithoutPrefix = dbGen.removePrefix(termToBeDeleted);
            //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+i+1 + ". Delete term: " + termToBeDeletedUIWithoutPrefix);
            Vector<String> old_top_terms = new Vector<String>();
            old_top_terms = dbGen.returnResults(SessionUserInfo, termToBeDeletedUIWithoutPrefix, "topterm", Q, TA, sis_session);
            //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"old_top_terms size = " + old_top_terms.size());
            if (DBCMT.deleteDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session, TA, tms_session,
                    dbGen, dbCon, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, new StringObject(termToBeDeleted),
                    termToBeDeletedUIWithoutPrefix, errorMsg, old_top_terms) == false) {
                //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"deletion cancelled");
                Logger.getLogger(DBCreate_Modify_Hierarchy.class.getName()).log(Level.INFO, "Failed To delete: "+termToBeDeleted);
                return;
            }
        }
        //delete top term last
        String termToBeDeleted = topTermOfTargetHierarchy.getValue();
        String termToBeDeletedUIWithoutPrefix = dbGen.removePrefix(termToBeDeleted);
        Vector<String> old_top_terms = new Vector<String>();
        //old_top_terms = dbGen.returnResults(SessionUserInfo, termToBeDeletedUIWithoutPrefix, "topterm", Q, TA, sis_session);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"old_top_terms size = " + old_top_terms.size());
        if (DBCMT.deleteDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session, TA, tms_session,
                dbGen, dbCon, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, new StringObject(termToBeDeleted),
                termToBeDeletedUIWithoutPrefix, errorMsg, old_top_terms) == false) {
            //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"deletion cancelled");
            Logger.getLogger(DBCreate_Modify_Hierarchy.class.getName()).log(Level.INFO, "Failed To delete: "+termToBeDeleted);
            return;
        }
        
        
        // ------------------------------------------------------------------------------------------                
        // 4. delete ALL links pointed to/from target hierarchy and delete target hierarchy
        // ------------------------------------------------------------------------------------------                
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 4. ------------------------------------------------------------------------------------------");                
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        termsOfHierarchy = Q.get_all_instances(0);
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"termsOfHierarchy card = " + Q.set_get_card(termsOfHierarchy)); 
        // delete ALL links pointed to/from target hierarchy
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);
        int linksFrom = Q.get_link_from(0);
        int linksTo = Q.get_link_to(0);
        Q.set_union(linksFrom, linksTo);
        Q.free_set(linksTo);
        int linksSet = linksFrom;
        Q.reset_set(linksSet);
        //StringObject cls = new StringObject();
        //IntegerObject fromid = new IntegerObject();
        //IntegerObject link_sysid = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject flag = new IntegerObject();
                
        Vector<Return_Link_Id_Row> retLIVals = new Vector<Return_Link_Id_Row>();
        if(Q.bulk_return_link_id(linksSet, retLIVals)!=QClass.APIFail){
            for(Return_Link_Id_Row row:retLIVals){
                Identifier I_from = new Identifier(row.get_v2_fcid());
                Identifier I_link = new Identifier(row.get_v3_sysid());
                //if (TA.IS_UNNAMED(row.get_v3_sysid()) != 0) { // unnamed attribute
                if(Q.CHECK_isUnNamedLink(row.get_v3_sysid())){
                    ret = Q.CHECK_Delete_Unnamed_Attribute(I_link);
                } else { // named attribute
                    ret = Q.CHECK_Delete_Named_Attribute(I_link, I_from);
                }
            }
        }

        /*
        while (Q.retur_link_id(linksSet, cls, fromid, link_sysid, cmv, flag) != QClass.APIFail) {
            Identifier I_from = new Identifier(fromid.getValue());
            Identifier I_link = new Identifier(link_sysid.getValue());
            if (WTA.IS_UNNAMED(link_sysid.getValue()) != 0) { // unnamed attribute
                ret = Q.Delete_Unnamed_Attribute(I_link);
            } else { // named attribute
                ret = Q.Delete_Named_Attribute(I_link, I_from);
            }
        }
        */
        Q.free_set(linksSet);
        // ------------------------------------------------------------------------------------------
        // 5. delete target hierarchy
        // ------------------------------------------------------------------------------------------
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 5. ------------------------------------------------------------------------------------------");        
        ret = Q.CHECK_Delete_Node(new Identifier(targetHierarchyObj.getValue()));
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"Delete_Node(target hierarchy) = " + ret); 
        // ------------------------------------------------------------------------------------------
        // 6. get the the TopTerm of target hierarchy and de-instantiate it from AAATopTerm
        // and connect it with BT link with AAAEL`Ορφανοί όροι (orphansHierarchyTopTermObj)
        // ------------------------------------------------------------------------------------------
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"step 6. ------------------------------------------------------------------------------------------");
        Q.reset_name_scope();
        long TopTermSysidL = Q.set_current_node(topTermOfTargetHierarchy);
        // in case TopTerm of target hierarchy still exists
        if (TopTermSysidL != QClass.APIFail) {
            ret = Q.CHECK_Add_Instance(new Identifier(topTermOfTargetHierarchy.getValue()), new Identifier(thesDescriptor.getValue()));
            ret = Q.CHECK_Delete_Instance(new Identifier(topTermOfTargetHierarchy.getValue()), new Identifier(thesTopTerm.getValue()));
            //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"Delete_Instance() = " + ret); 
            if (ret == QClass.APIFail) {
                errorMsg.setValue(dbGen.check_success(ret,TA,  null, tms_session));
            }
            // connect it with BT link with AAAEL`Ορφανοί όροι
            Q.reset_name_scope();
            long TopTermOfOrphansL = Q.set_current_node(orphansHierarchyTopTermObj);
            Identifier fromID = new Identifier(TopTermSysidL);
            CMValue to = new CMValue();
            to.assign_node(orphansHierarchyTopTermObj.getValue(), TopTermOfOrphansL);
            // construct category set with: AAADescriptor->AAA_BT (thesDescriptor, thesBT)
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node(thesDescriptor);
            Q.set_current_node(thesBT);
            Q.set_put(catSet);
            ret = Q.CHECK_Add_Unnamed_Attribute(fromID, to, catSet);
            Q.free_set(catSet);
            if (ret == QClass.APIFail) {
                errorMsg.setValue(dbGen.check_success(ret,TA,  null, tms_session));
            }
        }

        // check the case of error
        if (errorMsg.getValue().equals("") == false || ret == QClass.APIFail) {
            errorMsg.setValue(errorMsgPrefixObj.getValue() + errorMsg.getValue());
        }
        /*
        else {
        Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"############ DELETION of hierarchy: " + targetHierarchy + " SUCCEDED ############");
        }
         */
    }

    /*---------------------------------------------------------------------
    checkTopTermDependencies()
    ----------------------------------------------------------------------*/
    public boolean checkTopTermDependencies(String selectedThesaurus, QClass Q, IntegerObject sis_session, StringObject errorMsg, String oldName, String pathToMessagesXML) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());

        StringObject createdByLink = new StringObject();
        StringObject createdOnLink = new StringObject();
        StringObject modifiedByLink = new StringObject();
        StringObject modifiedOnLink = new StringObject();
        StringObject scopeNoteLink = new StringObject();
        StringObject scopeNoteENLink = new StringObject();
        StringObject commentLink = new StringObject();
        StringObject historicalNoteLink = new StringObject();
        StringObject taxonomicCodeLink = new StringObject();


        dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, sis_session.getValue(), createdByLink);
        dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), createdOnLink);
        dbtr.getThesaurusCategory_modified_by(selectedThesaurus, Q, sis_session.getValue(), modifiedByLink);
        dbtr.getThesaurusCategory_modified(selectedThesaurus, Q, sis_session.getValue(), modifiedOnLink);

        dbtr.getThesaurusCategory_scope_note(selectedThesaurus, Q, sis_session.getValue(), scopeNoteLink);
        dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), scopeNoteENLink);
        dbtr.getThesaurusCategory_comment(selectedThesaurus, Q, sis_session.getValue(), commentLink);
        dbtr.getThesaurusCategory_historical_note(selectedThesaurus, Q, sis_session.getValue(), historicalNoteLink);
        dbtr.getThesaurusCategory_taxonomic_code(selectedThesaurus, taxonomicCodeLink);

        if (oldName.compareTo(Parameters.UnclassifiedTermsLogicalname) == 0) {
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/UnclassifiedTermsHierarchy", null, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Η συγκεκριμένη ιεραρχία δεν μπορεί να διαγραφεί. Σε αυτήν κατατάσσονται όλοι οι νέοι όροι που δημιουργούνται."));
            return false;
        }

        Q.reset_name_scope();
        if (Q.set_current_node(new StringObject(prefix_el.concat(oldName))) == QClass.APIFail) {
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/TopTermNotFound", null, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Ο αντίστοιχος Όρος κορυφής δεν βρέθηκε στην βάση. Η διαδικασία διαγραφής της ιεραρχίας απέτυχε."));
            return false;
        }

        int set_to_links = Q.get_link_to(0);
        Q.reset_set(set_to_links);
        if (Q.set_get_card(set_to_links) > 0) {
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/TopTermHasLinksTo", null, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Ο αντίστοιχος Όρος κορυφής διαθέτει αναφορές προς αυτόν. Η διαδικασία διαγραφής της ιεραρχίας απέτυχε."));
            return false;
        }

        Q.reset_name_scope();
        if (Q.set_current_node(new StringObject(prefix_el.concat(oldName))) == QClass.APIFail) {
            dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/TopTermNotFound", null, pathToMessagesXML);
            //errorMsg.setValue(errorMsg.getValue().concat("Ο αντίστοιχος Όρος κορυφής δεν βρέθηκε στην βάση. Η διαδικασία διαγραφής της ιεραρχίας απέτυχε."));
            return false;
        }
        int set_from_links = Q.get_link_from(0);
        Q.reset_set(set_from_links);

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject traversed = new IntegerObject();
        //CMValue cmv = new CMValue();
        int count = 0; //Top Term must only have its THES1TopTerm-->belongs_to_thes1_hierarchy relation and any modified /created etc links
        Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
        if(Q.bulk_return_full_link(set_from_links, retFLVals)!=QClass.APIFail){
            for(Return_Full_Link_Row row:retFLVals){
                //while (Q.retur_full_link(set_from_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                String categ = row.get_v3_categ();
                if (categ.compareTo(modifiedByLink.getValue()) == 0) {
                continue;
                }
                if (categ.compareTo(modifiedOnLink.getValue()) == 0) {
                    continue;
                }
                if (categ.compareTo(createdByLink.getValue()) == 0) {
                    continue;
                }
                if (categ.compareTo(createdOnLink.getValue()) == 0) {
                    continue;
                }

                if (categ.compareTo(taxonomicCodeLink.getValue()) == 0) {
                    continue;
                }
                count++;
                if (count > 1) {
                    dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/TopTermHasLinksFrom", null, pathToMessagesXML);
                    //errorMsg.setValue(errorMsg.getValue().concat("Ο αντίστοιχος Όρος κορυφής διαθέτει συνδέσμους προς άλλους κόμβους. Η διαδικασία διαγραφής της ιεραρχίας απέτυχε."));
                    return false;
                }
            }
        }
        /*
        while (Q.retur_full_link(set_from_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

            if (categ.getValue().compareTo(modifiedByLink.getValue()) == 0) {
                continue;
            }
            if (categ.getValue().compareTo(modifiedOnLink.getValue()) == 0) {
                continue;
            }
            if (categ.getValue().compareTo(createdByLink.getValue()) == 0) {
                continue;
            }
            if (categ.getValue().compareTo(createdOnLink.getValue()) == 0) {
                continue;
            }

            if (categ.getValue().compareTo(taxonomicCodeLink.getValue()) == 0) {
                continue;
            }
            count++;
            if (count > 1) {
                dbGen.Translate(errorMsg, "root/EditHierarchy/Deletion/TopTermHasLinksFrom", null, pathToMessagesXML);
                //errorMsg.setValue(errorMsg.getValue().concat("Ο αντίστοιχος Όρος κορυφής διαθέτει συνδέσμους προς άλλους κόμβους. Η διαδικασία διαγραφής της ιεραρχίας απέτυχε."));
                return false;
            }
        }
        */

        return true;
    }

    /*
    private void modifyLetterCodes(String selectedThesaurus,TMSAPIClass TA,  String targetHierarchy, Vector targetHierarchyLetterCodes,Locale targetLocale) {

    //Get existing letter codes.
    Vector v = new Vecto();
    Vector currentLC = new Vecto();
    Vector addLC = new Vecto();
    Vector removeLC = new Vecto();

    v.addAll(dbGen.returnResults_Hierarchy(selectedThesaurus, targetHierarchy, "letter_code",Q,sis_session,targetLocale));

    if (!v.isEmpty()) {

    for (int k = 0; k < v.size(); k++) {

    Vector temp = new Vecto();
    temp.addAll((Vector) v.get(k));

    //get All current letter codes
    currentLC.addElement(temp.get(0).toString());

    //if one current letter code does not exist in targetHierarchyLetterCodes then it should be deleted
    if (!targetHierarchyLetterCodes.contains(temp.get(0).toString())) {
    removeLC.addElement(temp.get(0).toString());
    }
    }
    }

    //Now find out which are the new letter codes need that must be added
    for (int l = 0; l < targetHierarchyLetterCodes.size(); l++) {

    if (!currentLC.contains(targetHierarchyLetterCodes.get(l).toString())) {

    addLC.addElement(targetHierarchyLetterCodes.get(l).toString());
    }
    }

    //Preparing Deletion And Addition
    // looking for Hierarchy prefix (EKT`)
    DBThesaurusReferences dbtr = new DBThesaurusReferences();
    String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus,Q,sis_session.getValue());
    StringObject facetClassObj = new StringObject();
    dbtr.getThesaurusClass_Facet(selectedThesaurus,Q,sis_session.getValue(),facetClassObj);

    // convert target Hierarchy to DB encoding with prefix
    StringObject targetHierarchyObj = new StringObject(prefix.concat(targetHierarchy));

    int sisSessionId = sis_session.getValue();
    // get instances of EKTHierarchy
    Q.reset_name_scope();
    Q.set_current_node(sisSessionId, targetHierarchyObj);

    //Get all links originating from current node
    int linkFromSet = Q.get_inher_link_from(sisSessionId, 0);

    Q.reset_set(sisSessionId, linkFromSet);

    StringObject fromcls = new StringObject();
    StringObject label = new StringObject();
    StringObject categ = new StringObject();
    StringObject cls = new StringObject();
    IntegerObject uniq_categ = new IntegerObject();

    IntegerObject clsID = new IntegerObject();
    IntegerObject linkID = new IntegerObject();
    IntegerObject categID = new IntegerObject();
    CMValue cmv = new CMValue();
    int ret;

    //Delete Letter codes
    //For each link originating from current hierarchy ignore non letter_code links
    while (Q.retur_full_link_id(sisSessionId, linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {

    if (categ.getValue().equals("letter_code") == false) {
    continue;
    } else {

    //If letter_code value exists in removeLC Vector then perform delete and get possible error message
    String temp = cmv.getString();
    if (removeLC.contains(temp)) {

    ret = TA.DeleteHierarchyAttribute(this.tms_session.getValue(), linkID.getValue(), targetHierarchyObj);

    if (ret == TMSAPIClass.TMS_APIFail) {
    errorMsg.setValue(errorMsg.getValue().concat("<tr><td>" + dbGen.check_success(ret, null,tms_session) + "</td></tr>"));
    return;
    }
    }
    }
    }
    //We might want here to check if every removeLC elemnt has been deleted and inform user.


    //Add Letter Codes
    //Commit Facet Additions
    for (int m = 0; m < addLC.size(); m++) {

    StringObject nullLinkName = new StringObject();
    cmv.assign_string(addLC.get(m).toString());
    int catSet = Q.set_get_new();
    Q.reset_name_scope();
    Q.set_current_node(new StringObject("Facet"));
    Q.set_current_node(new StringObject("letter_code"));
    Q.set_put(catSet);

    ret = TA.CreateHierarchyAttribute(tms_session.getValue(),nullLinkName, targetHierarchyObj, cmv, catSet);

    if (ret == TMSAPIClass.TMS_APIFail) {
    errorMsg.setValue(errorMsg.getValue().concat("<tr><td>" + dbGen.check_success(ret, null,tms_session) + "</td></tr>"));
    return;
    }

    }



    Q.free_all_sets();

    }
     */
    private void modifyFacets(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            DBGeneral dbGen, String targetHierarchy, Vector<String> targetHierarchyFacets, Locale targetLocale, StringObject errorMsg) {

        Vector<String> currentFacets = dbGen.getSelectedFacets(selectedThesaurus, targetHierarchy, Q, sis_session, targetLocale);
        Vector<String> addFacets = new Vector<String>();
        Vector<String> removeFacets = new Vector<String>();


        for (int k = 0; k < currentFacets.size(); k++) {

            //if one current letter code does not exist in targetHierarchyLetterCodes then it should be deleted
            if (!targetHierarchyFacets.contains(currentFacets.get(k))) {
                removeFacets.addElement(currentFacets.get(k));
            }
        }


        //Now find out which are the new letter codes need that must be added
        for (int l = 0; l < targetHierarchyFacets.size(); l++) {

            if (!currentFacets.contains(targetHierarchyFacets.get(l))) {

                addFacets.addElement(targetHierarchyFacets.get(l));
            }
        }

        // looking for Hierarchy prefix (EKT`)
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        // convert target Hierarchy to DB encoding with prefix
        StringObject targetHierarchyObj = new StringObject(prefix.concat(targetHierarchy));
        int ret;


        //Commit Facet Additions
        for (int m = 0; m < addFacets.size(); m++) {

            StringObject targetFacetObj = new StringObject(prefix.concat(addFacets.get(m).toString()));
            ret = TA.CHECK_ClassifyHierarchyInFacet(targetHierarchyObj, targetFacetObj);

            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg.setValue(errorMsg.getValue().concat("<tr><td>" + dbGen.check_success(ret,TA,  null, tms_session) + "</td></tr>"));
                return;
            }

        }

        //Commit Facet DeClassifications
        for (int n = 0; n < removeFacets.size(); n++) {

            StringObject targetFacetObj = new StringObject(prefix.concat(removeFacets.get(n).toString()));
            ret = TA.CHECK_DeClassifyHierarchyFromFacet(targetHierarchyObj, targetFacetObj);

            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg.setValue(errorMsg.getValue().concat("<tr><td>" + dbGen.check_success(ret,TA,  null, tms_session) + "</td></tr>"));
                return;
            }
        }

    }
}
