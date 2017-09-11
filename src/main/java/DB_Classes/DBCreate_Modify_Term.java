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
import Utils.SortItem;
import Utils.TaxonomicCodeItem;
import Utils.Utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.*;

/**
 *
 * @author tzortzak
 */
public class DBCreate_Modify_Term {

    public DBCreate_Modify_Term() {
    }

    public void createNewTerm(UserInfoClass SessionUserInfo, String newName, Vector<String> decodedValues, String user, StringObject errorMsg, QClass Q, IntegerObject sis_session, TMSAPIClass TA, IntegerObject tms_session, DBGeneral dbGen, String pathToErrorsXML, boolean updateModifiedFields, boolean resolveError, OutputStreamWriter logFileWriter, int ConsistencyChecksPolicy) {

        if (Parameters.DEBUG) {
            Utils.StaticClass.webAppSystemOutPrintln("Target NEW Term: " + newName + " Target bts: " + decodedValues.toString());
        }

        ConsistensyCheck consistencyChecks = new ConsistensyCheck();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBConnect_Term dbCon = new DBConnect_Term();

        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject newNameObj = new StringObject(prefix.concat(newName));

        if (newName != null && newName.length() > 0) {
            /*
            try {
                byte[] byteArray = newName.getBytes("UTF-8");

                int maxTermChars = dbtr.getMaxBytesForDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxTermChars) {
                    if (resolveError) {
                        StringObject warningMsg = new StringObject();
                        errorArgs.clear();
                        errorArgs.add(newName);
                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(warningMsg, "root/EditTerm/Creation/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                        Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                        try {
                            logFileWriter.append("\r\n<targetTerm>");
                            logFileWriter.append("<name>" + Utilities.escapeXML(newName) + "</name>");
                            logFileWriter.append("<errorType>" + "name" + "</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(newName) + "</errorValue>");
                            logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                        } catch (IOException ex) {
                            Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                            Utils.StaticClass.handleException(ex);
                        }
                        return;

                    } else {

                        errorArgs.add("" + maxTermChars);
                        errorArgs.add("" + byteArray.length);
                        dbGen.Translate(errorMsg, "root/EditTerm/Creation/LongName", errorArgs, pathToMessagesXML);
                        return;
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
        }


        if (consistencyChecks.create_modify_check_01(errorMsg, pathToErrorsXML, newName) == false) {
            return;
        }

        //Check if BTs declared exist in db and if these BTs are THES1HierarchyTerms 
        if (consistencyChecks.create_modify_check_15(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, prefix, 2) == false) {
            return;
        }

        if (consistencyChecks.create_modify_check_09(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, newName, prefix, "create") == false) {
            return;
        }

        if (consistencyChecks.create_modify_check_24(errorMsg, pathToErrorsXML, decodedValues, Parameters.UnclassifiedTermsLogicalname) == false) {
            return;
        }

        if (consistencyChecks.create_modify_check_25(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, newName, decodedValues, "create", prefix, resolveError, logFileWriter) == false) {
            return;
        }


        if (consistencyChecks.create_modify_check_27(SessionUserInfo, Q, sis_session, newName, decodedValues, errorMsg, pathToErrorsXML, resolveError, logFileWriter, ConsistencyChecksPolicy) == false) {
            return;
        }

        //TODO: Check if reference uri exists

        errorMsg.setValue(dbCon.connectDescriptor(SessionUserInfo.selectedThesaurus, newNameObj, decodedValues, Q, sis_session, dbGen, TA, tms_session));


        if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
            return;
        }



        if (updateModifiedFields == false) {

            // FILTER default status for term creation depending on user group
            DBFilters dbf = new DBFilters();
            dbCon.CreateModifyStatus(SessionUserInfo.selectedThesaurus, newNameObj, dbf.GetDefaultStatusForTermCreation(SessionUserInfo), Q,TA, sis_session, tms_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }

            StringObject createdOnClass = new StringObject();
            StringObject createdOnLink = new StringObject();
            StringObject createdByClass = new StringObject();
            StringObject createdByLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_by_kwd, createdByClass, createdByLink, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClass, createdOnLink, Q, sis_session);


            StringObject modifiedOnClass = new StringObject();
            StringObject modifiedOnLink = new StringObject();
            StringObject modifiedByClass = new StringObject();
            StringObject modifiedByLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClass, modifiedByLink, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClass, modifiedOnLink, Q, sis_session);


            String[] modifiedNodes = new String[decodedValues.size()];
            for (int i = 0; i < decodedValues.size(); i++) {
                modifiedNodes[i] = decodedValues.get(i).trim();
            }

            String UserName = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue()) + user;

            if (errorMsg.getValue().compareTo("") == 0) {

                errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, newNameObj, UserName, createdByClass.getValue(), createdByLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectTime(SessionUserInfo.selectedThesaurus, newNameObj, createdOnClass.getValue(), createdOnLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));


            }

            if (errorMsg.getValue().compareTo("") == 0 && modifiedNodes != null && modifiedNodes.length > 0) {
                for (int i = 0; i < modifiedNodes.length; i++) {


                    dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, modifiedNodes[i].trim(), ConstantParameters.FROM_Direction, modifiedOnClass.getValue(), modifiedOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                    dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, modifiedNodes[i].trim(), ConstantParameters.FROM_Direction, modifiedByClass.getValue(), modifiedByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

                    StringObject targetModifiedDescrObj = new StringObject(prefix.concat(modifiedNodes[i].trim()));
                    errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetModifiedDescrObj, UserName, modifiedByClass.getValue(), modifiedByLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectTime(SessionUserInfo.selectedThesaurus, targetModifiedDescrObj, modifiedOnClass.getValue(), modifiedOnLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                }
            }
        }
    }

    public boolean guideTermsMoveToHierarchyBugFixStep1Of2(String selectedThesaurus,
            QClass Q, IntegerObject sis_session, StringObject targetDescriptorObj, Vector<Long> guideTermBugFixLinkIdsL,
            Vector<Long> guideTermBugFixLinkCategIdsL, Vector<SortItem> guideTermBugFixBtsWithGuideTerms,
            Vector<String> old_bts, StringObject errorMsg) {

        //String pathToMessagesXML = Utilities.getMessagesXml();
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();


        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, fromClassObj, LinkObj, Q, sis_session);

        Q.reset_name_scope();
        Q.set_current_node(fromClassObj);
        long btLinkidL = Q.set_current_node(LinkObj);

        Identifier btLinkIdent = new Identifier(btLinkidL);

        Q.reset_name_scope();
        Q.set_current_node(targetDescriptorObj);
        int set = Q.get_link_from_by_category(0, fromClassObj, LinkObj);
        Q.reset_set(set);

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();

        Vector<Return_Full_Link_Id_Row> retFLIVals = new Vector<Return_Full_Link_Id_Row>();
        if(Q.bulk_return_full_link_id(set, retFLIVals)!=QClass.APIFail){
            for(Return_Full_Link_Id_Row row:retFLIVals){
                String bt = dbGen.removePrefix(row.get_v8_cmv().getString());
                long btIdL = row.get_v8_cmv().getSysid();
                String guideTerm = row.get_v5_categ().replaceFirst(fromClassObj.getValue(), "");

                SortItem newItem = new SortItem(bt, btIdL, guideTerm);

                if (guideTerm != null && guideTerm.length() > 0) {
                    guideTermBugFixLinkIdsL.add(row.get_v4_linkId());
                    guideTermBugFixLinkCategIdsL.add(row.get_v7_categid());
                    guideTermBugFixBtsWithGuideTerms.add(newItem);
                }
                old_bts.add(bt);
            }
        }
        /*
        while (Q.retur_full_link_id(set, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
            String bt = dbGen.removePrefix(cmv.getString());
            int btId = cmv.getSysid();
            String guideTerm = categ.getValue().replaceFirst(fromClassObj.getValue(), "");

            SortItem newItem = new SortItem(bt, btId, guideTerm);

            if (guideTerm != null && guideTerm.length() > 0) {
                guideTermBugFixLinkIds.add(linkID.getValue());
                guideTermBugFixLinkCategIds.add(categID.getValue());
                guideTermBugFixBtsWithGuideTerms.add(newItem);
            }
            old_bts.add(bt);
        }
        */

        Q.free_set(set);

        for (int i = 0; i < guideTermBugFixLinkIdsL.size(); i++) {
            Identifier currentLinkId = new Identifier(guideTermBugFixLinkIdsL.get(i).intValue());
            int currentGuideTermId = guideTermBugFixLinkCategIdsL.get(i).intValue();
            if (btLinkidL == currentGuideTermId) {
                continue;
            }
            Identifier currentLinkCategoryId = new Identifier(currentGuideTermId);

            Q.reset_name_scope();
            int ret = Q.CHECK_Add_Instance(currentLinkId, btLinkIdent);
            if (ret == QClass.APIFail) {
                SortItem zongItem = guideTermBugFixBtsWithGuideTerms.get(i);
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/ZeroAdditionFailure", new String[]{zongItem.getLogName()}));
                //errorMsg.setValue("Addition failure of term  to the zero guide term category.");
                return false;
            }
            ret = Q.CHECK_Delete_Instance(currentLinkId, currentLinkCategoryId);
            if (ret == QClass.APIFail) {
                SortItem zongItem = guideTermBugFixBtsWithGuideTerms.get(i);
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/DeletionFailure", new String[]{zongItem.getLogName(),zongItem.getLinkClass()}));
                //errorMsg.setValue("Deletion failure of BT relation between term " + zongItem.getLogName() + " and guide term: " + zongItem.getLinkClass());
                return false;
            }

        }
        return true;
    }

    public boolean guideTermsMoveToHierarchyBugFixStep2Of2(String selectedThesaurus,
            QClass Q, IntegerObject sis_session, StringObject targetDescriptorObj, Vector<Long> guideTermBugFixLinkCategIdsL,
            Vector<SortItem> guideTermBugFixBtsWithGuideTerms, StringObject errorMsg) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();


        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, fromClassObj, LinkObj, Q, sis_session);

        Q.reset_name_scope();
        Q.set_current_node(fromClassObj);
        long btLinkidL = Q.set_current_node(LinkObj);

        Identifier btLinkIdent = new Identifier(btLinkidL);

        //link ids have changed. thus they must be read again in order to restore prior GuideTerm relations
        Vector<Long> newGuideTermBugFixLinkIdsL = new Vector<Long>();
        Vector<String> new_current_Bts = new Vector<String>();


        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();

        Q.reset_name_scope();
        Q.set_current_node(targetDescriptorObj);
        int set_new_bts = Q.get_link_from_by_category(0, fromClassObj, LinkObj);
        Q.reset_set(set_new_bts);

        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if(Q.bulk_return_link(set_new_bts, retVals)!=QClass.APIFail){
            for(Return_Link_Row row:retVals){
                String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                new_current_Bts.add(bt);
                newGuideTermBugFixLinkIdsL.add(row.get_Neo4j_NodeId());
            }
        }

        /*while (Q.retur_full_link_id(set_new_bts, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
            String bt = dbGen.removePrefix(cmv.getString());
            new_current_Bts.add(bt);
            newGuideTermBugFixLinkIds.add(linkID.getValue());
        }*/
        Q.free_set(set_new_bts);

        for (int i = 0; i < new_current_Bts.size(); i++) {
            String targetBt = new_current_Bts.get(i);
            int index = findLogNameIndexInSortItemVector(guideTermBugFixBtsWithGuideTerms, targetBt);
            if (index == -1) {
                continue;
            }
            long currentGuideTermIdL = guideTermBugFixLinkCategIdsL.get(index).intValue();
            if (btLinkidL == currentGuideTermIdL) {
                continue;
            }
            Identifier currentLinkId = new Identifier(newGuideTermBugFixLinkIdsL.get(i).intValue());
            Identifier currentLinkCategoryId = new Identifier(currentGuideTermIdL);


            Q.reset_name_scope();
            long retL = Q.set_current_node_id(currentLinkId.getSysid());
            retL = Q.set_current_node_id(currentLinkCategoryId.getSysid());
            retL = Q.CHECK_Add_Instance(currentLinkId, currentLinkCategoryId);
            if (retL == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/GeneralAdditionFailure", null));
                //errorMsg.setValue("Failure during addition of term to guide term category. " );
                return false;
            }
            retL = Q.CHECK_Delete_Instance(currentLinkId, btLinkIdent);
            if (retL == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/GeneralDeletionFailure", null));
                //errorMsg.setValue("Failure during deletion of term from guide term category.");
                return false;
            }

        }

        return true;
    }

    public void commitTermTransaction(UserInfoClass SessionUserInfo, String targetTerm,
            String targetField, Vector<String> decodedValues,
            String user, StringObject errorMsg,
            QClass Q, IntegerObject sis_session, TMSAPIClass TA, IntegerObject tms_session,
            DBGeneral dbGen, String pathToErrorsXML, boolean updateModifiedFields, boolean resolveError, OutputStreamWriter logFileWriter, int ConsistencyChecksPolicy) {

        if (Parameters.DEBUG) {
            Utils.StaticClass.webAppSystemOutPrintln("Target Term: " + targetTerm + " Target relation: " + targetField + " Target Value: " + decodedValues.toString());
        }

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Utilities u = new Utilities();

        DBConnect_Term dbCon = new DBConnect_Term();
        ConsistensyCheck consistencyChecks = new ConsistensyCheck();

        StringObject modifiedOnClass = new StringObject();
        StringObject modifiedOnLink = new StringObject();
        StringObject modifiedByClass = new StringObject();
        StringObject modifiedByLink = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClass, modifiedByLink, Q, sis_session);
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClass, modifiedOnLink, Q, sis_session);

        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, targetField, fromClassObj, LinkObj, Q, sis_session);

        String UserName = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue()) + user;
        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject targetDescriptorObj = new StringObject(prefix.concat(targetTerm));

        String[] modifiedNodes = null;

        if (consistencyChecks.create_modify_check_01(errorMsg, pathToErrorsXML, targetTerm) == false) {
            return;
        }

        if (targetField.compareTo(ConstantParameters.delete_term_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Delete Term...">  
            String descr = targetTerm;

            if (consistencyChecks.move_To_Hierarchy_Consistency_Test_3(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, descr, prefix) == false) {
                return;
            }


            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"checks succeded for " + targetTerm);
            //Find out which nodes where modified
            Vector<String> modifiedNodesVector = new Vector<String>();
            modifiedNodesVector.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.bt_kwd, Q,TA, sis_session));
            modifiedNodesVector.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.nt_kwd, Q,TA, sis_session));
            modifiedNodesVector.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.rt_kwd, Q,TA, sis_session));

            if (modifiedNodesVector.size() > 0) {
                modifiedNodes = new String[modifiedNodesVector.size()];
                for (int i = 0; i < modifiedNodesVector.size(); i++) {
                    modifiedNodes[i] = modifiedNodesVector.get(i).trim();
                }
            }

            Vector<String> old_top_terms = new Vector<String>();
            old_top_terms = dbGen.returnResults(SessionUserInfo, targetTerm, "topterm", Q,TA, sis_session);
            if (deleteDescriptor(SessionUserInfo.selectedThesaurus, Q, sis_session, TA, tms_session,
                    dbGen, dbCon, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, targetDescriptorObj, targetTerm, errorMsg, old_top_terms) == false) {
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"deletion cancelled");
                return;
            }

            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"deletion performed");
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.status_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Status...">  
            //No consistency check

            Vector<String> status = new Vector<String>();
            status.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.status_kwd, Q,TA, sis_session));
            if (status.size() > 0 && decodedValues.get(0).compareTo(status.get(0)) == 0) {
                return;
            }

            modifiedNodes = new String[1];
            modifiedNodes[0] = targetTerm.trim();

            dbCon.CreateModifyStatus(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues.get(0), Q, TA, sis_session, tms_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.bt_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Bt...">
            //check consistency of at least one bt preserved
            if (decodedValues == null || decodedValues.size() == 0) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/BTs/AtLeastOne", null));
                //errorMsg.setValue("One at least BT must be maintained.");
                return;
            }



            if (consistencyChecks.create_modify_check_07(decodedValues, errorMsg, pathToErrorsXML, targetTerm) == false) {
                return;
            }
            //Check if RTs declared exist in db and if these RTs are THES1HierarchyTerms 
            if (consistencyChecks.create_modify_check_15(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, prefix, 2) == false) {
                return;
            }

            if (consistencyChecks.create_modify_check_24(errorMsg, pathToErrorsXML, decodedValues, Parameters.UnclassifiedTermsLogicalname) == false) {
                return;
            }

            if (consistencyChecks.create_modify_check_25(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, decodedValues, "create", prefix, resolveError, logFileWriter) == false) {
                return;
            }

            if (consistencyChecks.create_modify_check_27(SessionUserInfo, Q, sis_session, targetTerm, decodedValues, errorMsg, pathToErrorsXML, resolveError, logFileWriter, ConsistencyChecksPolicy) == false) {
                return;
            }

            if (decodedValues.size() == 0) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/BTs/AtLeastOne", null));
                //errorMsg.setValue("One at least BT must be maintained.");
                return;
            }


            Vector<String> old_bts = new Vector<String>();
            Vector<Long> guideTermBugFixLinkIdsL = new Vector<Long>();
            Vector<Long> guideTermBugFixLinkCategIdsL = new Vector<Long>();
            Vector<SortItem> guideTermBugFixBtsWithGuideTerms = new Vector<SortItem>();

            //GuideTerms Bug Fix - Keep GuideTerms information related to this term , delete guide terms with its bts and then perform Move to hierarchy actions
            if (guideTermsMoveToHierarchyBugFixStep1Of2(SessionUserInfo.selectedThesaurus, Q, sis_session, targetDescriptorObj, 
                    guideTermBugFixLinkIdsL, guideTermBugFixLinkCategIdsL, guideTermBugFixBtsWithGuideTerms, old_bts, errorMsg) == false) {
                return;
            }



            Vector<String> add_bts = new Vector<String>();
            Vector<String> delete_bts = new Vector<String>();

            for (int i = 0; i < decodedValues.size(); i++) {
                int index = old_bts.indexOf(decodedValues.get(i));
                if (index < 0) {
                    add_bts.add(decodedValues.get(i));
                }

            }

            for (int i = 0; i < old_bts.size(); i++) {
                int index = decodedValues.indexOf(old_bts.get(i));
                if (index < 0) {
                    delete_bts.add(old_bts.get(i));
                }

            }

            //No modification occured just update screen
            if (delete_bts.size() == 0 && add_bts.size() == 0) {
                return;
            }

            //Find out nodes that will be modified modified
            if (delete_bts.size() > 0 || add_bts.size() > 0) {
                add_bts.addAll(delete_bts);
                add_bts.add(targetTerm.trim());
                modifiedNodes = new String[add_bts.size()];
                for (int i = 0; i < add_bts.size(); i++) {
                    modifiedNodes[i] = add_bts.get(i).trim();
                }
            } else {
                modifiedNodes = null;
            }

            //Consistency checks
            if (consistencyChecks.move_To_Hierarchy_Consistency_Test_1(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, prefix) == false) {
                return;
            }

            //perform consistency checks for all new values            
            Q.reset_name_scope();
            if (consistencyChecks.move_To_Hierarchy_Consistency_Test_7(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, decodedValues, prefix, resolveError, logFileWriter) == false) {
                return;
            }
            Q.reset_name_scope();
            if (consistencyChecks.move_To_Hierarchy_Consistency_Test_8(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, decodedValues, prefix, resolveError, logFileWriter) == false) {
                return;
            }

            for (int i = 0; i < decodedValues.size(); i++) {

                if (consistencyChecks.move_To_Hierarchy_Consistency_Test_4(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, decodedValues.get(i), prefix) == false) {
                    return;
                }

            }

            //first move Node and subtree with first bt declared and then connect rest bts of decodedValues Vector if any more exist
            Vector<String> fromhiers = new Vector<String>();
            fromhiers.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, "topterm", Q,TA, sis_session));

            Vector<String> tohiers = new Vector<String>();
            tohiers.addAll(dbGen.returnResults(SessionUserInfo, decodedValues.get(0), "topterm", Q,TA, sis_session));

            boolean performmovement = true;
            if (fromhiers.size() == 0) {
                performmovement = false;
                if (Parameters.DEBUG) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Term " + targetTerm + " was found with no Top Terms.");
                }
            }
            if (tohiers.size() == 0) {
                performmovement = false;
                if (Parameters.DEBUG) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Term " + targetTerm + " was found without new Top Terms to be moved to.");
                }
            }
            if (performmovement && MoveToHierarchyAction(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, targetTerm, fromhiers.get(0), tohiers.get(0), decodedValues.get(0), ConstantParameters.MOVE_NODE_AND_SUBTREE, user, errorMsg) == false) {
                return;
            } else {

                Q.reset_name_scope();

                if (decodedValues.size() >= 1) {

                    for (int i = 1; i < decodedValues.size(); i++) {

                        Vector<String> FromNewBThiers = new Vector<String>();
                        FromNewBThiers.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, "topterm", Q,TA, sis_session));
                        Vector<String> ToNewBThiers = new Vector<String>();
                        ToNewBThiers.addAll(dbGen.returnResults(SessionUserInfo, decodedValues.get(i), "topterm", Q,TA, sis_session));
                        if (MoveToHierarchyAction(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, tms_session, dbGen, targetTerm, FromNewBThiers.get(0), ToNewBThiers.get(0), decodedValues.get(i), ConstantParameters.CONNECT_NODE_AND_SUBTREE, user, errorMsg) == false) {
                            return;
                        }

                    }
                }

                Q.reset_name_scope();
                //Collect in a check_nodes_set all nodes that should be checked for - TopTerm - Classes Consistensy

                //StringObject TargetTermNameUTF8WithPrefix = new StringObject(prefix.concat(TargetTermName));
                Q.reset_name_scope();

                if (Q.set_current_node(targetDescriptorObj) != QClass.APIFail) {

                    int check_nodes_set = Q.set_get_new();
                    Q.set_put(check_nodes_set);
                    Q.reset_set(check_nodes_set);

                    //if(MoveToHierarchyOption.compareTo("MOVE_NODE_AND_SUBTREE") == 0){ // this is the case here --> just do it!
                    dbGen.collect_Recurcively_ALL_NTs_Of_Set(SessionUserInfo.selectedThesaurus, check_nodes_set, check_nodes_set, true, Q, sis_session);
                    Q.reset_set(check_nodes_set);
                    //}

                    StringObject errorMsgPrefixObj = new StringObject();

                    if (MoveToHierBugFix(SessionUserInfo.selectedThesaurus, check_nodes_set, Q, sis_session, dbGen, errorMsg) == false) {
                        errorMsgPrefixObj.setValue(u.translateFromMessagesXML("root/EditTerm/BTs/GeneralHierarchyUpdateMsg", null));

                        errorMsg.setValue(errorMsgPrefixObj.getValue() + errorMsg.getValue());
                        Q.free_set(check_nodes_set);
                        return;
                    }
                    Q.free_set(check_nodes_set);
                } else {
                    
                    errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/BTs/GeneralHierarchyUpdateMsg", null) +u.translateFromMessagesXML("root/EditTerm/BTs/NotFound", new String[]{targetTerm}));
                    return;
                }

            }

            //restore bt realtions guide terms info
            if (guideTermsMoveToHierarchyBugFixStep2Of2(SessionUserInfo.selectedThesaurus, Q, sis_session, targetDescriptorObj, guideTermBugFixLinkCategIdsL, guideTermBugFixBtsWithGuideTerms, errorMsg) == false) {
                return;
            }

            if (updateModifiedFields == false) {
                Q.TEST_end_transaction();
                Q.TEST_begin_transaction();
            }
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.nt_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Nt...">
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.rt_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Rt...">
            //Rts may not contain target descriptor
            if (consistencyChecks.create_modify_check_07(decodedValues, errorMsg, pathToErrorsXML, targetTerm) == false) {
                return;
            }


            //Check if RTs declared exist in db and if these RTs are THES1HierarchyTerms 
            if (consistencyChecks.create_modify_check_15(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, prefix, 0) == false) {
                return;
            }

            //Check if RTs delared exist in set that includes all BTs and all NTs recursively of target Node
            if (consistencyChecks.create_modify_check_20(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, targetTerm, decodedValues, prefix, "modify", resolveError, logFileWriter) == false) {
                return;
            }

            //prepare modified nodes
            Vector<String> modified_rts = new Vector<String>();
            modified_rts.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.rt_kwd, Q,TA,  sis_session));

            for (int i = 0; i < decodedValues.size(); i++) {

                int index = modified_rts.indexOf(decodedValues.get(i));
                if (index >= 0) {
                    modified_rts.removeElementAt(index);
                } else {
                    modified_rts.add(decodedValues.get(i));
                }

            }
            if (modified_rts.size() > 0) {
                modified_rts.add(targetTerm.trim());
                modifiedNodes = new String[modified_rts.size()];
                for (int i = 0; i < modified_rts.size(); i++) {
                    modifiedNodes[i] = modified_rts.get(i).trim();
                }
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation

            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.TO_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectRTs(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues, Q, sis_session, dbGen, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }
            //</editor-fold >
        } else if (targetField.compareTo(ConstantParameters.translation_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Translations...">
            //create_modify_check_03 //Check if more than one english names are inserted  
            //create_modify_check_07 //Check if the same values with term name are used for another link declaration
            //create_modify_check_10 //Check if new term translation name already exists in db
            //also check if it exists and is used as used for term

            //String prefixEN = dbtr.getThesaurusPrefix_EnglishWord(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue());

            Vector<String> valsToRemove = new Vector<String>();
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);
                String langPrefix = "";
                if (val.indexOf(Parameters.TRANSLATION_SEPERATOR) > 0) {
                    langPrefix = val.substring(0, val.indexOf(Parameters.TRANSLATION_SEPERATOR) + 1).trim();
                    val = val.substring(val.indexOf(Parameters.TRANSLATION_SEPERATOR) + 1).trim();
                }
                /*
                try {
                    
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForTranslation(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(langPrefix + val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/Translations/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.translation_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(langPrefix + val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/Translations/LongName", errorArgs, pathToMessagesXML);

                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                */
            }

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            //Check if more than one english names are inserted      
            if (consistencyChecks.create_modify_check_03(decodedValues, errorMsg, pathToErrorsXML) == false) {
                return;
            }

            //abandoned
            if (consistencyChecks.create_modify_check_26(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, "") == false) {
                return;
            }


            //prepare modified nodes
            Vector<SortItem> modified_translations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, true, targetTerm, Q, sis_session);

            Vector<String> normalizedModeifiedNodes = new Vector<String>();

            for (int i = 0; i < modified_translations.size(); i++) {
                SortItem targetSort = modified_translations.get(i);
                normalizedModeifiedNodes.add(targetSort.linkClass + Parameters.TRANSLATION_SEPERATOR + targetSort.log_name);
            }


            Vector<String> normalizedDecodedValues = new Vector<String>();
            for (int i = 0; i < decodedValues.size(); i++) {
                String targetValue = decodedValues.get(i); //eg EN: some en term

                if (Parameters.DEBUG) {
                    Utils.StaticClass.webAppSystemOutPrintln(targetValue);
                }
                String prefixLang = targetValue.substring(0, targetValue.indexOf(Parameters.TRANSLATION_SEPERATOR)); //eg EN:
                targetValue = targetValue.substring(targetValue.indexOf(Parameters.TRANSLATION_SEPERATOR) + Parameters.TRANSLATION_SEPERATOR.length()); //eg ' some en term'
                targetValue = prefixLang.trim() + Parameters.TRANSLATION_SEPERATOR + targetValue.trim(); //eg EN:some en term;
                normalizedDecodedValues.add(targetValue);
            }

            for (int i = 0; i < normalizedDecodedValues.size(); i++) {


                int index = normalizedModeifiedNodes.indexOf(normalizedDecodedValues.get(i));
                if (index >= 0) {
                    normalizedModeifiedNodes.removeElementAt(index);
                } else {
                    normalizedModeifiedNodes.add(normalizedDecodedValues.get(i));
                }
            }

            if (normalizedModeifiedNodes.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation


            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectTranslation(SessionUserInfo.selectedThesaurus, targetDescriptorObj, normalizedDecodedValues, Q, sis_session, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }

            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.uf_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit UF...">
            //create_modify_check_07 //Check if the same values with term name are used for another link declaration
            //create_modify_check_16 //Check if UFs declared already exist in db but not as uf links
            //UFs may not contain target descriptor

            Vector<String> valsToRemove = new Vector<String>();

            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                
                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForUF(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {

                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/UF/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/UF/LongName", errorArgs, pathToMessagesXML);

                            return;
                        }
                    }

                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                
            }
*/
            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            if (consistencyChecks.create_modify_check_07(decodedValues, errorMsg, pathToErrorsXML, targetTerm) == false) {
                return;
            }

            if (consistencyChecks.create_modify_check_16(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, prefix, targetTerm, resolveError, logFileWriter) == false) {
                return;
            }

            //prepare modified nodes
            Vector<String> modified_ufs = new Vector<String>();
            modified_ufs.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.uf_kwd, Q,TA, sis_session));

            for (int i = 0; i < decodedValues.size(); i++) {

                int index = modified_ufs.indexOf(decodedValues.get(i));
                if (index >= 0) {
                    modified_ufs.removeElementAt(index);
                } else {
                    modified_ufs.add(decodedValues.get(i));
                }

            }
            if (modified_ufs.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation
            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.TO_Direction,
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectUFs(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues, Q, sis_session, dbGen, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }

            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.uf_translations_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit UF_Translations...">
            //create_modify_check_18 //Check if uk_ufs declared already exist in db but not as uk_uf links
            //String prefixEN = dbtr.getThesaurusPrefix_EnglishWord(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue());

            /*
            if(consistencyChecks.create_modify_check_18(SessionUserInfo.selectedThesaurus, Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML,decodedValues, prefixEN,targetTerm,resolveError,logFileWriter)==false){
            return;
            }
             */
            //prepare modified nodes
            Vector<String> valsToRemove = new Vector<String>();
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);
                String langPrefix = "";
                if (val.indexOf(Parameters.TRANSLATION_SEPERATOR) > 0) {
                    langPrefix = val.substring(0, val.indexOf(Parameters.TRANSLATION_SEPERATOR) + 1).trim();
                    val = val.substring(val.indexOf(Parameters.TRANSLATION_SEPERATOR) + 1).trim();
                
                }
                /*
                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForTranslation(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(langPrefix + val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/TR_UF/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(langPrefix + val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/TR_UF/LongName", errorArgs, pathToMessagesXML);

                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                    */
            }

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            Vector<SortItem> modified_translations = dbGen.getTranslationLinkValues(SessionUserInfo.selectedThesaurus, false, targetTerm, Q, sis_session);

            Vector<String> normalizedModeifiedNodes = new Vector<String>();

            for (int i = 0; i < modified_translations.size(); i++) {
                SortItem targetSort = modified_translations.get(i);
                normalizedModeifiedNodes.add(targetSort.linkClass + Parameters.TRANSLATION_SEPERATOR + targetSort.log_name);
            }


            Vector<String> normalizedDecodedValues = new Vector<String>();
            for (int i = 0; i < decodedValues.size(); i++) {
                String targetValue = decodedValues.get(i); //eg EN: some en term

                String prefixLang = targetValue.substring(0, targetValue.indexOf(Parameters.TRANSLATION_SEPERATOR)); //eg EN:
                targetValue = targetValue.substring(targetValue.indexOf(Parameters.TRANSLATION_SEPERATOR) + Parameters.TRANSLATION_SEPERATOR.length()); //eg ' some en term'
                targetValue = prefixLang.trim() + Parameters.TRANSLATION_SEPERATOR + targetValue.trim(); //eg EN:some en term;
                normalizedDecodedValues.add(targetValue);
            }

            for (int i = 0; i < normalizedDecodedValues.size(); i++) {


                int index = normalizedModeifiedNodes.indexOf(normalizedDecodedValues.get(i));
                if (index >= 0) {
                    normalizedModeifiedNodes.removeElementAt(index);
                } else {
                    normalizedModeifiedNodes.add(normalizedDecodedValues.get(i));
                }
            }

            if (normalizedModeifiedNodes.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation

            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectUFTranslation(SessionUserInfo.selectedThesaurus, targetDescriptorObj, normalizedDecodedValues, Q, sis_session, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.primary_found_in_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit primary Sources...">

            String prefix_Source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());

            Vector<String> valsToRemove = new Vector<String>();
            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/PR_source/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.primary_found_in_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/PR_source/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            */

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);


                //WRITE TO LOG FILE A MESSAGE
                //LongNameErrorResolve
            }

            if (consistencyChecks.create_modify_check_13(Q, sis_session, errorMsg, pathToErrorsXML, decodedValues, prefix_Source) == false) {
                return;
            }

            //prepare modified nodes
            Vector<String> modified_gts = new Vector<String>();
            modified_gts.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.primary_found_in_kwd, Q,TA, sis_session));

            for (int i = 0; i < decodedValues.size(); i++) {

                int index = modified_gts.indexOf(decodedValues.get(i));
                if (index >= 0) {
                    modified_gts.removeElementAt(index);
                } else {
                    modified_gts.add(decodedValues.get(i));
                }

            }
            if (modified_gts.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation

            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectSources(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues, DBConnect_Term.CATEGORY_PRIMARY_FOUND_IN, Q, sis_session, dbGen, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.translations_found_in_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Translations Sources...">
            //create_modify_check_07 //Check if the same values with term name are used for another link declaration
            //create_modify_check_18 //Check if uk_ufs declared already exist in db but not as uk_uf links

            Vector<String> valsToRemove = new Vector<String>();
            String prefix_Source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/TR_source/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {

                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.translations_found_in_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/TR_source/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            */

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            if (consistencyChecks.create_modify_check_13(Q, sis_session, errorMsg, pathToErrorsXML, decodedValues, prefix_Source) == false) {
                return;
            }

            //prepare modified nodes
            Vector<String> modified_ets = new Vector<String>();
            modified_ets.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.translations_found_in_kwd, Q,TA, sis_session));

            for (int i = 0; i < decodedValues.size(); i++) {

                int index = modified_ets.indexOf(decodedValues.get(i));
                if (index >= 0) {
                    modified_ets.removeElementAt(index);
                } else {
                    modified_ets.add(decodedValues.get(i));
                }

            }
            if (modified_ets.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation

            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectSources(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues, DBConnect_Term.CATEGORY_translations_found_in, Q, sis_session, dbGen, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }
            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.tc_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit TC...">
            //prepare values --> 12.35 --> transform to --> 012.35
            Vector<String> valsToRemove = new Vector<String>();
            String prefix_TC = dbtr.getThesaurusPrefix_TaxonomicCode(Q, sis_session.getValue());
            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForTaxonomicalcode(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/TC/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.tc_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/TC/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            */

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            /*
            for(int i=0; i< decodedValues.size();i++){
            TaxonomicCodeItem tempItem = new TaxonomicCodeItem(decodedValues.get(i),"");
            Vector<String> codeParts = new Vector<String>();
            codeParts.addAll(tempItem.codeParts);
            String codeStr = "";
            for(int j=0; j< codeParts.size(); j++){
            codeStr = codeStr.concat(codeParts.get(j));
            if(j< codeParts.size()-1){
            codeStr = codeStr.concat(".");
            }
            }
            decodedValues.set(i, codeStr);
            }
             */

            if (consistencyChecks.create_modify_check_11(errorMsg, pathToErrorsXML, decodedValues) == false) {
                return;
            }
            if (consistencyChecks.create_modify_check_12(SessionUserInfo.selectedThesaurus, Q, sis_session, dbGen, errorMsg, pathToErrorsXML, decodedValues, prefix_TC, prefix.concat(targetTerm)) == false) {
                return;
            }


            //prepare modified nodes
            Vector<String> modified_tcs = new Vector<String>();
            modified_tcs.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.tc_kwd, Q,TA, sis_session));

            for (int i = 0; i < decodedValues.size(); i++) {

                int index = modified_tcs.indexOf(decodedValues.get(i));
                if (index >= 0) {
                    modified_tcs.removeElementAt(index);
                } else {
                    modified_tcs.add(decodedValues.get(i));
                }

            }
            if (modified_tcs.size() > 0) {
                modifiedNodes = new String[1];
                modifiedNodes[0] = targetTerm.trim();
            } else {
                modifiedNodes = null;
            }
            //end of modified nodes preparation

            dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, targetTerm, ConstantParameters.FROM_Direction, 
                    fromClassObj.getValue(), LinkObj.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                return;
            }
            if (decodedValues.size() > 0) {
                errorMsg.setValue(dbCon.connectTaxonomicCodes(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues, Q, sis_session, dbGen, TA, tms_session));
                if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                    return;
                }
            }
            //</editor-fold >
        } else if (targetField.compareTo(ConstantParameters.scope_note_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit SN...">

            Vector<String> valsToRemove = new Vector<String>();
            Vector<String> scopeNote = new Vector<String>();
            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {

                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/ScopeNote/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.scope_note_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {

                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/ScopeNote/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            */

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }

            scopeNote.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.scope_note_kwd, Q,TA, sis_session));
            if (decodedValues.size() > 0 && scopeNote.size() > 0 && decodedValues.get(0).compareTo(scopeNote.get(0)) == 0) {
                return;
            }

            modifiedNodes = new String[1];
            modifiedNodes[0] = targetTerm.trim();

            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            int ret = TA.DeleteDescriptorComment(targetDescriptorObj, fromClassObj, LinkObj);
            if (ret == TMSAPIClass.TMS_APIFail) {
                //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
                return;
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            
            if (decodedValues.size() > 0 && decodedValues.get(0).trim().length() > 0) {

                errorMsg.setValue(dbCon.AddComment(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues.get(0), DBConnect_Term.HYPERTEXT_CATEGORY_SCOPENOTE, Q,TA, sis_session));
            }

            //</editor-fold >
        } else if (targetField.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit SN_TR...">

            String checkStr = "";
            if(decodedValues.size()>0){
                checkStr = decodedValues.get(0);
            }
            
            boolean valueLengthOk = true;

            
            if(checkStr.length()>0){
                /*
                try {
                    byte[] byteArray = checkStr.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        valueLengthOk = false;
                        if (resolveError) {

                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(checkStr);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/TranslationsScopeNote/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.translations_scope_note_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(checkStr) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/TranslationsScopeNote/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                */
            }


            if(valueLengthOk == false)
            {
                //no change
                return;
            }
            
            Vector<String> scopeNoteEN = new Vector<String>();
            scopeNoteEN.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.translations_scope_note_kwd, Q, TA, sis_session));
            if (checkStr.length() > 0 && scopeNoteEN.size() > 0 && checkStr.compareTo(scopeNoteEN.get(0)) == 0) {
                return;
            }
            

            
            
            Hashtable<String, String> trSns = u.getTranslationScopeNotes(checkStr);
            Vector<String> langCodes = new Vector<String>();
            langCodes.addAll(trSns.keySet());
            Collections.sort(langCodes);

            String finalTrSNStr = "";
            for (int k = 0; k < langCodes.size(); k++) {
                String targetLangCode = langCodes.get(k);
                String val = trSns.get(targetLangCode);
                if (val != null && val.trim().length() > 0) {
                    val = val.trim();
                    finalTrSNStr += "\n" + targetLangCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR + " " + val;
                }

            }
            finalTrSNStr = finalTrSNStr.trim();
             
            modifiedNodes = new String[1];
            modifiedNodes[0] = targetTerm.trim();

            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            int ret = TA.DeleteDescriptorComment(targetDescriptorObj, fromClassObj, LinkObj);
            if (ret == TMSAPIClass.TMS_APIFail) {
                //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
                return;
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            if (finalTrSNStr.length() > 0) {

                errorMsg.setValue(dbCon.AddComment(SessionUserInfo.selectedThesaurus, targetDescriptorObj, finalTrSNStr, DBConnect_Term.HYPERTEXT_CATEGORY_SCOPENOTE_TR, Q,TA, sis_session));
            }

            //</editor-fold>
        } else if (targetField.compareTo(ConstantParameters.historical_note_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Edit Historical Note...">

            Vector<String> valsToRemove = new Vector<String>();
            /*
            for (int k = 0; k < decodedValues.size(); k++) {
                String val = decodedValues.get(k);

                try {
                    byte[] byteArray = val.getBytes("UTF-8");

                    int maxChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
                    if (byteArray.length > maxChars) {
                        if (resolveError) {
                            valsToRemove.add(val);
                            StringObject warningMsg = new StringObject();
                            errorArgs.clear();
                            errorArgs.add(val);
                            errorArgs.add(targetTerm);
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(warningMsg, "root/EditTerm/HistoricalNote/LongNameErrorResolve", errorArgs, pathToMessagesXML);
                            Utils.StaticClass.webAppSystemOutPrintln(warningMsg.getValue());
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.historical_note_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(val) + "</errorValue>");
                                logFileWriter.append("<reason>" + warningMsg.getValue() + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Utils.StaticClass.webAppSystemOutPrintln("IOException caught: " + ex.getMessage());
                                Utils.StaticClass.handleException(ex);
                            }

                        } else {
                            errorArgs.add("" + maxChars);
                            errorArgs.add("" + byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditTerm/HistoricalNote/LongName", errorArgs, pathToMessagesXML);
                            return;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            */

            if (valsToRemove.size() > 0) {
                decodedValues.removeAll(valsToRemove);
            }


            Vector<String> historicalNote = new Vector<String>();
            historicalNote.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, ConstantParameters.historical_note_kwd, Q, TA, sis_session));
            if (decodedValues.size() > 0 && historicalNote.size() > 0 && decodedValues.get(0).compareTo(historicalNote.get(0)) == 0) {
                return;
            }

            modifiedNodes = new String[1];
            modifiedNodes[0] = targetTerm.trim();

            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            int ret = TA.DeleteDescriptorComment(targetDescriptorObj, fromClassObj, LinkObj);
            if (ret == TMSAPIClass.TMS_APIFail) {
                //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
                return;
            }
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
            if (decodedValues.size() > 0 && decodedValues.get(0).trim().length() > 0) {

                errorMsg.setValue(dbCon.AddComment(SessionUserInfo.selectedThesaurus, targetDescriptorObj, decodedValues.get(0), DBConnect_Term.HYPERTEXT_CATEGORY_HISTORICALNOTE, Q,TA, sis_session));
            }

            //</editor-fold >
        }
        if (updateModifiedFields == false) {
            //IF NO ERROR OCCURED CURRENT NODE MUST UPDATE IT's MODOFICATION FIELDS
            if (errorMsg.getValue().compareTo("") == 0 && modifiedNodes != null && modifiedNodes.length > 0) {
                for (int i = 0; i < modifiedNodes.length; i++) {

                    dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, modifiedNodes[i].trim(), ConstantParameters.FROM_Direction, 
                            modifiedOnClass.getValue(), modifiedOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
                    dbCon.delete_term_links_by_category(SessionUserInfo.selectedThesaurus, modifiedNodes[i].trim(), ConstantParameters.FROM_Direction, 
                            modifiedByClass.getValue(), modifiedByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

                    StringObject targetModifiedDescrObj = new StringObject(prefix.concat(modifiedNodes[i].trim()));
                    errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectEditor(SessionUserInfo.selectedThesaurus, targetModifiedDescrObj, UserName, modifiedByClass.getValue(), modifiedByLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                    errorMsg.setValue(errorMsg.getValue().concat(dbCon.connectTime(SessionUserInfo.selectedThesaurus, targetModifiedDescrObj, modifiedOnClass.getValue(), modifiedOnLink.getValue(), Q, sis_session, dbGen, TA, tms_session)));
                }
            }
        }

    }

    public int findLogNameIndexInSortItemVector(Vector<SortItem> containerVector, String targetLogName) {
        int index = -1;
        for (int i = 0; i < containerVector.size(); i++) {
            if (targetLogName.compareTo(containerVector.get(i).log_name) == 0) {
                return i;
            }
        }
        return index;
    }

    public boolean deleteDescriptor(String selectedThesaurus, QClass Q, IntegerObject sis_session, 
            TMSAPIClass TA, IntegerObject tms_session, DBGeneral dbGen, DBConnect_Term dbCon, int KindOfDescriptor,
            StringObject targetDescriptorObj, String targetDescriptorUTF8, StringObject errorMsg, Vector<String> old_top_terms) {
        
        String initialVal = errorMsg.getValue()==null?"":errorMsg.getValue();
        if (KindOfDescriptor == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {
            //1)Delete all terms links except BTs and NTs.
            //2)MOVE NODE ONLY TO Unclassified Class and delete it.
            //  Consistencies Check ensures that these actions are allowed to be performed
            //3)Delete Node
            //4)Update Nodes affected modification data, status etc

            StringObject BTFromClassObj = new StringObject();
            StringObject BTLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTFromClassObj, BTLinkObj, Q, sis_session);

            StringObject scopenoteFromClassObj = new StringObject();
            StringObject scopenoteLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);

            StringObject scopenoteENFromClassObj = new StringObject();
            StringObject scopenoteENLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenoteENFromClassObj, scopenoteENLinkObj, Q, sis_session);

            StringObject commentFromClassObj = new StringObject();
            StringObject commentLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.comment_kwd, commentFromClassObj, commentLinkObj, Q, sis_session);

            StringObject historicalnoteFromClassObj = new StringObject();
            StringObject historicalnoteLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
            //DELETE ALL TERM RELATIONS EXCEPT BT RELATIONS
            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            TA.DeleteDescriptorComment(targetDescriptorObj, scopenoteFromClassObj, scopenoteLinkObj);
            TA.DeleteDescriptorComment(targetDescriptorObj, scopenoteENFromClassObj, scopenoteENLinkObj);
            TA.DeleteDescriptorComment(targetDescriptorObj, commentFromClassObj, commentLinkObj);
            TA.DeleteDescriptorComment(targetDescriptorObj, historicalnoteFromClassObj, historicalnoteLinkObj);
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }


            //collect all links from targetNote that are not BT links in order to delete them
            Q.reset_name_scope();
            Q.set_current_node(targetDescriptorObj);
            int set_BTs_Relations_From = Q.get_link_from_by_category(0, BTFromClassObj, BTLinkObj);
            Q.reset_set(set_BTs_Relations_From);
            int set_link_from = Q.get_link_from(0);
            Q.reset_set(set_link_from);
            Q.set_difference(set_link_from, set_BTs_Relations_From);
            Q.reset_set(set_link_from);

            dbCon.delete_term_links_by_set(selectedThesaurus, targetDescriptorUTF8, ConstantParameters.FROM_Direction, set_link_from, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q,TA, sis_session, dbGen, errorMsg);
            Q.reset_name_scope();
            Q.set_current_node(targetDescriptorObj);
            //collect all links to targetNode that are not BT links
            int set_BTs_Relations_To = Q.get_link_to_by_category(0, BTFromClassObj, BTLinkObj);
            Q.reset_set(set_BTs_Relations_To);
            int set_link_to = Q.get_link_to(0);
            Q.reset_set(set_link_to);
            Q.set_difference(set_link_to, set_BTs_Relations_To);
            Q.reset_set(set_link_to);

            dbCon.delete_term_links_by_set(selectedThesaurus, targetDescriptorUTF8, ConstantParameters.TO_Direction, set_link_to, ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q,TA, sis_session, dbGen, errorMsg);

            Q.free_set(set_BTs_Relations_From);
            Q.free_set(set_BTs_Relations_To);
            Q.free_set(set_link_from);
            Q.free_set(set_link_to);

            for (int i = 0; i < old_top_terms.size(); i++) {
                // karam: do NOT call MoveToHierarchyNodeOnly in case target and destination are the same
                if (targetDescriptorUTF8.equals(old_top_terms.get(i).toString()) == false) {
                    int ret = MoveToHierarchyNodeOnly(selectedThesaurus, targetDescriptorUTF8, old_top_terms.get(i).toString(), Q, sis_session, TA, tms_session, errorMsg);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        continue;
                    }
					/* BUGFIX???
                    else{
                        break;
                    }*/
                }
            }

            int ret = TA.CHECK_DeleteNewDescriptor( targetDescriptorObj);
            // karam: free all sets after this, because in case of repetitive call of this function
            // => SIS server: out of answer sets
            Q.free_all_sets();

            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null, tms_session)));
                return false;
            }
            /*
            else {
            Q.end_transaction();
            Q.begin_transaction();
            //for modifed fields updates
            }
             */

        }
        //ELIAS BugFix
        errorMsg.setValue(initialVal);
        return true;
    }

    int MoveToHierarchyNodeOnly(String selectedThesaurus, String TargetTermName, String MoveFromHierarchy, QClass Q, IntegerObject sis_session, TMSAPIClass TA, IntegerObject tms_session, StringObject errorMsg) {

        //String pathToMessagesXML = Utilities.getMessagesXml();
        //Vector<String> errorArgs = new Vector<String>();
        Utilities u = new Utilities();

        // looking for Term prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        // looking for Class prefix
        String classPrefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        // prepare input parameters: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));
        StringObject MoveFromHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(MoveFromHierarchy));
        StringObject MoveToHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(Parameters.UnclassifiedTermsLogicalname));
        StringObject MoveBTtermUTF8WithPrefix = new StringObject(termPrefix.concat(Parameters.UnclassifiedTermsLogicalname));
        int MTHoption = TMSAPIClass.MOVE_NODE_ONLY;

        int ret = TA.CHECK_MoveToHierarchy( TargetTermNameUTF8WithPrefix, MoveFromHierarchyUTF8WithPrefix,
                MoveToHierarchyUTF8WithPrefix, MoveBTtermUTF8WithPrefix, MTHoption);

        StringObject MoveToHierarchyResultsMessage = new StringObject();
        if (ret == TMSAPIClass.TMS_APISucc) { // SUCCESS
            
            MoveToHierarchyResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/SuccessMsg", new String[] {TargetTermName}));
            //MoveToHierarchyResultsMessage.setValue("Movement of term \"" + TargetTermName + "\" to Hierarchy waw successfully performed.");

        } else { // FAIL
            TA.ALMOST_DONE_GetTMS_APIErrorMessage( MoveToHierarchyResultsMessage);
            MoveToHierarchyResultsMessage.setValue(MoveToHierarchyResultsMessage.getValue());
            errorMsg.setValue(errorMsg.getValue().concat(MoveToHierarchyResultsMessage.getValue()));
            return TMSAPIClass.TMS_APIFail;
        }



        return TMSAPIClass.TMS_APISucc;
    }

    /*---------------------------------------------------------------------
    MoveToHierarchyAction()
    ----------------------------------------------------------------------*/
    boolean MoveToHierarchyAction(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, DBGeneral dbGen, String TargetTermName, String MoveFromHierarchy, String MoveToHierarchy, String MoveBTterm, String MoveToHierarchyOption, String user, StringObject MoveToHierarchyResultsMessage) {

        Utilities u = new Utilities();



        // looking for Term prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        // looking for Class prefix
        String classPrefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        // prepare input parameters: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));
        StringObject MoveFromHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(MoveFromHierarchy));
        StringObject MoveToHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(MoveToHierarchy));
        StringObject MoveBTtermUTF8WithPrefix = new StringObject(termPrefix.concat(MoveBTterm));
        int MTHoption = 0;

        if (MoveToHierarchyOption.compareTo("MOVE_NODE_ONLY") == 0) {
            MTHoption = TMSAPIClass.MOVE_NODE_ONLY;

        } else if (MoveToHierarchyOption.compareTo("MOVE_NODE_AND_SUBTREE") == 0) {
            MTHoption = TMSAPIClass.MOVE_NODE_AND_SUBTREE;
        } else {
            MTHoption = TMSAPIClass.CONNECT_NODE_AND_SUBTREE;
        }

        int ret = TA.CHECK_MoveToHierarchy( TargetTermNameUTF8WithPrefix, MoveFromHierarchyUTF8WithPrefix, MoveToHierarchyUTF8WithPrefix, MoveBTtermUTF8WithPrefix, MTHoption);

        if (ret == TMSAPIClass.TMS_APISucc) { // SUCCESS
            //Q.free_all_sets();
            return true;
        } else { // FAIL
            TA.ALMOST_DONE_GetTMS_APIErrorMessage( MoveToHierarchyResultsMessage);

            MoveToHierarchyResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/BTs/ErrorPrefix", null) + MoveToHierarchyResultsMessage.getValue());
            //Q.free_all_sets();
            return false;
        }

    }


    /*---------------------------------------------------------------------
    DeleteBTAction()
    ----------------------------------------------------------------------*/
    boolean DeleteBTAction(String selectedThesaurus, QClass Q, IntegerObject sis_session, TMSAPIClass TA, IntegerObject tms_session, DBGeneral dbGen, String TargetTermName, String TargetBTforDeletion, String user, StringObject MoveToHierarchyResultsMessage) {


        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // 1. get the BT links of TargetTermName (see DBGeneral::getBT_NT()) - BT_links_set
        // looking for Term prefix
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        // looking for EKTDescriptor
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thesDescriptor);
        // looking for EKT_BT
        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), thesBT);
        // prepare TargetTermName and TargetBTforDeletion: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));
        StringObject TargetBTforDeletionUTF8WithPrefix = new StringObject(termPrefix.concat(TargetBTforDeletion));
        Q.reset_name_scope();
        long TargetTermSysidL = Q.set_current_node(TargetTermNameUTF8WithPrefix);
        int BT_links_set = Q.get_link_from_by_category(0, thesDescriptor, thesBT);

        // 2. parse set1 and get the sysid of the BT-link with to-value = TargetBTforDeletionUTF8WithPrefix - BTlink_sysid
        Q.reset_set(BT_links_set);
        //StringObject node_name = new StringObject();
        //IntegerObject BTlink_sysid = new IntegerObject();
        //CMValue cmv = new CMValue();
        long BTlink_sysidL =-1;
        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if(Q.bulk_return_link(BT_links_set, retVals)!=QClass.APIFail){
            for(Return_Link_Row row:retVals){
                String BTlink_toValue = row.get_v3_cmv().getString();
                if (BTlink_toValue.compareTo(TargetBTforDeletionUTF8WithPrefix.getValue()) == 0) {
                    BTlink_sysidL = row.get_Neo4j_NodeId();
                    break;
                }
            }
        }

        /*
        while (Q.retur_link_id(BT_links_set, node_name, new IntegerObject(), BTlink_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
            String BTlink_toValue = cmv.getString();
            if (BTlink_toValue.compareTo(TargetBTforDeletionUTF8WithPrefix.getValue()) == 0) {
                break;
            }
        }
        */
        Q.free_set(BT_links_set);

        // 3. check if BTlink_sysid is Named or Unnamed link (see DBGeneral::isNamedLink()) 
        // and call Delete_Named_Attribute() or Delete_Unnamed_Attribute()
        int ret;
        Identifier BTlinkID = new Identifier(BTlink_sysidL);
        // named link
        //if (dbGen.isNamedLink(BTlink_sysidL) == true) {
        if (Q.CHECK_isUnNamedLink(BTlink_sysidL) == false) {
            Identifier TargetTermID = new Identifier(TargetTermSysidL);
            ret = Q.CHECK_Delete_Named_Attribute(BTlinkID, TargetTermID);
        } // unnamed link
        else {
            ret = Q.CHECK_Delete_Unnamed_Attribute(BTlinkID);
        }

        Q.reset_name_scope();

        if (ret == TMSAPIClass.TMS_APISucc) { // SUCCESS            
            Q.free_all_sets();
            return true;
        } else { // FAIL
            TA.ALMOST_DONE_GetTMS_APIErrorMessage( MoveToHierarchyResultsMessage);
            Q.free_all_sets();
            return false;
        }


    }

    boolean MoveToHierBugFix(String selectedThesaurus, int set_check_nodes, QClass Q, IntegerObject sis_session, DBGeneral dbGen, StringObject errorMsg) {


        int SisSessionId = sis_session.getValue();
        Q.reset_name_scope();

        Vector<String> checkNodes = dbGen.get_Node_Names_Of_Set(set_check_nodes, false, Q, sis_session);
        StringObject TopTermObjClass = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("TopTerm"));
        StringObject HierarchyObj = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("ThesaurusNotionType"));
        StringObject TopTermHierRelationObj = new StringObject();//("belongs_to_" + SessionUserInfo.selectedThesaurus.toLowerCase().concat("_hierarchy"));

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Utilities u = new Utilities();

        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTermObjClass);
        dbtr.getThesaurusCategory_belongs_to_hierarchy(selectedThesaurus, Q, sis_session.getValue(), TopTermHierRelationObj);
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), HierarchyObj);
        String prefixClass = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        for (int i = 0; i < checkNodes.size(); i++) {

            Q.reset_name_scope();

            if (Q.set_current_node( new StringObject(checkNodes.get(i))) != QClass.APIFail) {

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"MoveToHierBugFix Checking Node :" + checkNodes.get(i) +"\n");
                //COLLECT ALL TARGET NODE'S BTS UNTIL TOP TERMS
                int target_set = Q.set_get_new();
                Q.set_put( target_set);
                Q.reset_set( target_set);

                int set_recursive_bts = Q.set_get_new();
                dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus, target_set, set_recursive_bts, false, Q, sis_session);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_recursive_bts:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n",Q,sis_session));

                //COLLECT TARGET TERM'S CLASSES And get their TOP Terms
                Q.reset_set( target_set);
                int set_classes = Q.get_classes( target_set);
                Q.reset_set( set_classes);

                Q.reset_name_scope();
                Q.set_current_node( HierarchyObj);
                int set_class_filter = Q.get_all_instances( 0);//will include instances of THES1ObsoleteHierarchy
                Q.reset_set( set_class_filter);

                Q.set_intersect( set_classes, set_class_filter);
                Q.reset_set( set_classes);

                int set_classes_topterms = Q.get_from_node_by_category( set_classes, TopTermObjClass, TopTermHierRelationObj);
                Q.reset_set( set_classes_topterms);
                //int set_classes_topterms = Q.get_from_node( set_classes);
                //Q.reset_set(set_classes_topterms);

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_classes_topterms:\n" +dbGen.getStringList_Of_Set(set_classes_topterms, ",\n",Q,sis_session));
                Q.reset_name_scope();

                //GET TOP TERM INSTANCES in order to filter recursive BTs of target
                Q.reset_name_scope();
                Q.set_current_node( TopTermObjClass);
                int set_top_terms = Q.get_instances( 0);
                Q.reset_set( set_top_terms);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_top_terms:\n" +dbGen.getStringList_Of_Set(set_top_terms, ",\n",Q,sis_session));

                /*
                //FILTER TARGETTERM'S Classes instances WITH TOPTERM instansces
                Q.set_intersect(set_classes_topterms, set_top_terms);
                Q.reset_set( set_classes_topterms);
                 */

                //FILTER TARGETTERM'S RECURSIVE BTS WITH TOPTERM INSATNCES
                Q.set_intersect( set_recursive_bts, set_top_terms);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nFiltered set_recursive_bts:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n",Q,sis_session));

                //MAKE A COPY OF TARGET TERM'S FILTERED CLASSES TopTerms
                int set_classes_topterms_copy = Q.set_get_new();
                Q.set_copy( set_classes_topterms_copy, set_classes_topterms);

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_classes_topterms_copy:\n" +dbGen.getStringList_Of_Set(set_classes_topterms_copy, ",\n",Q,sis_session));

                //FIND OUT WHICH CLASSES OF TARGET NODE SHOULD BE DELETED (ALL RECURSIVE BTS OF TARGET DO NOT INCLUDE THEIR RELEVANT TOP TERMS)
                Q.set_difference( set_classes_topterms, set_recursive_bts);
                Q.reset_set( set_classes_topterms);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nDelete Classes Instances:\n" +dbGen.getStringList_Of_Set(set_classes_topterms, ",\n",Q,sis_session));
                if (Q.set_get_card( set_classes_topterms) > 0) {

                    Vector<String> deleteInstancesClasses = dbGen.get_Node_Names_Of_Set(set_classes_topterms, true, Q, sis_session);

                    String tempStr = checkNodes.get(i);
                    tempStr = dbGen.removePrefix(tempStr);
                    int ret;
                    for (int k = 0; k < deleteInstancesClasses.size(); k++) {

                        //ensure that no top term relation will be deleted. Top Terms should not reach at this point of code but just in case
                        if (tempStr.compareTo(deleteInstancesClasses.get(k)) == 0) {
                            continue;
                        }

                        Identifier from = new Identifier(checkNodes.get(i));
                        Identifier to = new Identifier(prefixClass.concat(deleteInstancesClasses.get(k)));

                        ret = Q.CHECK_Delete_Instance( from, to);
                        if (ret == QClass.APIFail) {
                            
                            errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/GeneralUpdateNodeError", new String[] {tempStr}));
                            //errorMsg.setValue("An error occurred during update of node " + tempStr+".");
                            return false;
                        }
                    }

                }

                //FIND OUT WHICH CLASSES OF TARGET NODE SHOULD BE ADDED(ALL RECURSIVE BTS OF TARGET DO NOT INCLUDE THEIR RELEVANT TOP TERMS)
                Q.set_difference( set_recursive_bts, set_classes_topterms_copy);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nAdd class Instances:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n",Q,sis_session));
                if (Q.set_get_card( set_recursive_bts) > 0) {

                    Vector<String> addInstancesClasses = dbGen.get_Node_Names_Of_Set(set_recursive_bts, true, Q, sis_session);
                    int ret;
                    for (int k = 0; k < addInstancesClasses.size(); k++) {

                        Identifier from = new Identifier(checkNodes.get(i));
                        Identifier to = new Identifier(prefixClass.concat(addInstancesClasses.get(k)));
                        ret = Q.CHECK_Add_Instance( from, to);
                        if (ret == QClass.APIFail) {

                            String tempStr = checkNodes.get(i);
                            tempStr = dbGen.removePrefix(tempStr);
                            errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/GeneralUpdateNodeError", new String[] {tempStr}));
                            ///errorMsg.setValue("An error occurred during update of node " + tempStr+".");
                            return false;
                        }
                    }
                }

            } else {
                Q.free_all_sets();
                return false;
            }
            Q.free_all_sets();

        }
        Q.reset_name_scope();
        //dbGen.collect_Direct_Links_Of_Set( set_check_nodes, includeTarget, user, user, SisSessionId);
        Q.free_all_sets();

        return true;
    }

    public void performGuideTermEditing(String selectedThesaurus, QClass Q, IntegerObject sis_session, StringObject errorMsg, String targetTerm, Vector<String> decodedNtsVec, Vector<String> decodedGuideTermsVec) {

        Utilities u = new Utilities();

        //targetTerm
        //ntsVec
        //guideTermsVec
        DBGeneral dbGen = new DBGeneral();
        StringObject targetTermObj = new StringObject();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();


        StringObject NTClassObj = new StringObject();
        StringObject NTLinkObj = new StringObject();
        StringObject HierarchyTermObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.nt_kwd, NTClassObj, NTLinkObj, Q, sis_session);
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), HierarchyTermObj);

        Vector<String> existingGuideTerms = new Vector<String>();
        existingGuideTerms.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        targetTermObj.setValue(termPrefix.concat(targetTerm));

        Q.reset_name_scope();
        Q.set_current_node(targetTermObj);
        int set_nts = Q.get_link_to_by_category(0, NTClassObj, NTLinkObj);
        Q.reset_set(set_nts);


        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();
//
  //      CMValue cmv = new CMValue();

        Vector<String> currentNts = new Vector<String>();
        Vector<Long> linkIdsL = new Vector<Long>();
        Vector<String> currentNtsGuideTerms = new Vector<String>();

        Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
        if(Q.bulk_return_full_link(set_nts, retFLVals)!=QClass.APIFail){
            for(Return_Full_Link_Row row:retFLVals){
                currentNts.add(row.get_v1_cls());
                linkIdsL.add(row.get_Neo4j_NodeId());
                currentNtsGuideTerms.add(row.get_v3_categ());
            }
        }
        /*//while(Q.retur_full_link(set_nts, cls, label, categ, fromcls, cmv, uniq_categ, traversed)!=QClass.APIFail){
        while (Q.retur_full_link_id(set_nts, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
            currentNts.add(cls.getValue());
            linkIds.add(linkID.getValue());
            currentNtsGuideTerms.add(categ.getValue());
        }*/
        Q.free_set(set_nts);

        //ntsVec
        //guideTermsVec
        for (int i = 0; i < currentNts.size(); i++) {

            String currentNt = currentNts.get(i).replaceFirst(termPrefix, "");
            String currentGuideTerm = currentNtsGuideTerms.get(i);
            Identifier currentLinkId = new Identifier(linkIdsL.get(i));

            int index = decodedNtsVec.indexOf(currentNt);
            //check if dedoded nts plus gulideterms that were collected from servlet request
            //apply changes only to nts currently associated with term and altered by user
            if (index >= 0) { //No new Nts will be created in this step
                String decodedGuideTerm = NTLinkObj.getValue();

                if (decodedGuideTermsVec.get(index) != null) {
                    decodedGuideTerm = decodedGuideTerm.concat(decodedGuideTermsVec.get(index));
                }

                if (decodedGuideTerm.compareTo(currentGuideTerm) != 0) {

                    Q.reset_name_scope();
                    Q.set_current_node(NTClassObj);
                    long oldGuideTermIDL = Q.set_current_node(new StringObject(currentGuideTerm));
                    //Utils.StaticClass.webAppSystemOutPrintln(currentGuideTerm + " for nt: "+ currentNt + " must change to: " + newLink);

                    StringObject decodedGuideTermLinkObj = new StringObject(decodedGuideTerm);
                    Q.reset_name_scope();
                    Q.set_current_node(NTClassObj);
                    long decodedGuideTermLinkIdL = Q.set_current_node(decodedGuideTermLinkObj);

                    if (decodedGuideTermLinkIdL == QClass.APIFail) {
                        errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/NotFoundGuideTerm", new String[] {decodedGuideTerm,currentNt}));
                        //errorMsg.setValue("Guide term '" + decodedGuideTerm+"' selected for term '" +currentNt+ "' was not found in the database. Please refresh screen and repeat the action.");
                        return;
                    }

                    //add new relation
                    Q.reset_name_scope();
                    int ret = Q.CHECK_Add_Instance(currentLinkId, new Identifier(decodedGuideTermLinkIdL));
                    if (ret == QClass.APIFail) {
                        errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/AdditionFailureOfGuideTerm", new String[] {decodedGuideTerm}));
                        //errorMsg.setValue("Failure during addition of term to guide term category: "+ decodedGuideTerm+".");
                        return;
                    }

                    //remove previous relation
                    Q.reset_name_scope();
                    ret = Q.CHECK_Delete_Instance(currentLinkId, new Identifier(oldGuideTermIDL));
                    if (ret == QClass.APIFail) {
                        errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/GuideTerms/DeletionFailureOfGuideTerm", new String[] {currentGuideTerm}));
                        //errorMsg.setValue("Failure during deletion of term from guide term category: "+currentGuideTerm+".");
                        return;
                    }

                }
            }
        }

    }
}
