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

import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.Utilities;


import java.io.UnsupportedEncodingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class DBCreate_Modify_Source {

    public static final String source_create_kwd = "source_create";
    public static final String source_delete_kwd = "delete_source";
    public static final String source_rename_kwd = "source_rename";
    public static final String source_note_kwd = ConstantParameters.source_note_kwd;
    public static final String source_move_references_kwd = "move_source_references";

    public DBCreate_Modify_Source() {
    }

    public boolean createNewSource(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String targetSource, String source_note, StringObject errorMsg) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        DBThesaurusReferences dbtr = new DBThesaurusReferences();


        if (targetSource != null && targetSource.length() > 0) {
            /*
            try {
                byte[] byteArray = targetSource.getBytes("UTF-8");

                int maxsourceNameChars = dbtr.getMaxBytesForSource(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxsourceNameChars) {
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(""+maxsourceNameChars);
                    errorArgs.add(""+byteArray.length);
                    dbGen.Translate(errorMsg, "root/EditSource/Creation/LongName", errorArgs, pathToMessagesXML);
                    
                    return false;
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOut(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
        }

        if (source_note != null && source_note.length() > 0) {
            /*
            try {
                byte[] byteArray = source_note.getBytes("UTF-8");

                int maxSourceNoteChars = dbtr.getMaxBytesForCommentCategory(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxSourceNoteChars) {
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(""+maxSourceNoteChars);
                    errorArgs.add(""+byteArray.length);
                    dbGen.Translate(errorMsg, "root/EditSource/Creation/LongSourceNote", errorArgs, pathToMessagesXML);
                    return false;
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOut(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
        }


        if (targetSource.length() == 0) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Creation/EmptyName", null));
            //errorMsg.setValue("A name must be specified for the new source.");
            return false;
        }
        String prefix_Source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
        StringObject sourceObj = new StringObject(prefix_Source.concat(targetSource));

        Q.reset_name_scope();
        if (Q.set_current_node(sourceObj) != QClass.APIFail) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Creation/AlreadyinDB", new String[] {targetSource}));
            //errorMsg.setValue("Source with name " + targetSource + " already exists in the database.");
            return false;
        }

        int ret = TA.CHECK_CreateSource(sourceObj);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg.setValue(dbGen.check_success(ret,TA,  null, tms_session));
            return false;
        }

        if (source_note != null) {
            source_note = source_note.replaceAll(" +", " ");
            source_note = source_note.trim();

            if (source_note.length() > 0) {

                StringObject prevThes = new StringObject();
                TA.GetThesaurusNameWithoutPrefix(prevThes);
                if(prevThes.getValue().equals(selectedThesaurus)==false){
                    TA.SetThesaurusName(selectedThesaurus);
                }
                ret = TA.SetDescriptorComment(sourceObj, new StringObject(source_note), new StringObject("Source"), new StringObject("source_note"));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    //errorMsg.setValue(WTA.errorMessage.getValue());
                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    return false;
                }
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
            }
        }

        return true;
    }

    public boolean commitSourceTransaction(UserInfoClass SessionUserInfo, ServletContext context, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, String targetSource, String targetField, String newValue, String deleteCurrentThesaurusReferences, StringObject errorMsg) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();


        String prefixSource = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
        String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject targetSourceObj = new StringObject(prefixSource.concat(targetSource));

        if (targetField.compareTo(source_delete_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Delete Source...">

            if (targetSource == null || targetSource.trim().length() == 0) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Deletion/EmptyName", null));
                //errorMsg.setValue("No name is specified for the deletion source. Deletion cancelled.");
                return false;
            }

            Q.reset_name_scope();
            long sourceIDL = Q.set_current_node(targetSourceObj);
            if (sourceIDL == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Deletion/NotFound", new String[] {targetSource}));
                //errorMsg.setValue("Source given for deletion: "+targetSource+" was not found in the database. Please refresh the page contents and try again. Deletion cancelled.");
                return false;
            }

            int set_links_to_source = Q.get_link_to(0);
            Q.reset_set(set_links_to_source);
            int howmanyRefs = Q.set_get_card(set_links_to_source);
            Q.free_set(set_links_to_source);
            if (howmanyRefs > 0 && deleteCurrentThesaurusReferences == null) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Deletion/HasReferences", new String[] {targetSource}));
                //errorMsg.setValue("Source " + targetSource + " is referenced from the current or other thesauri of the database and can not be deleted. Deletion cancelled.");
                return false;

            }

            if (howmanyRefs > 0) {// deleteCurrentThesaurusReferences is != null
                StringObject foundInClass = new StringObject();
                StringObject foundInLink = new StringObject();
                dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), foundInClass);
                dbtr.getThesaurusCategory_found_in(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), foundInLink);

                Q.reset_name_scope();
                Q.set_current_node(targetSourceObj);

                int set_source_links = Q.get_link_to_by_category(0, foundInClass, foundInLink);
                Q.reset_set(set_source_links);
                int howmanyfromCurrent = Q.set_get_card(set_source_links);

                ArrayList<String> targetTerms = new ArrayList<String>();
                ArrayList<Long> targetTermsLinkIdL = new ArrayList<Long>();
                //StringObject fromcls = new StringObject();
                //StringObject label = new StringObject();
                //StringObject categ = new StringObject();
                //StringObject cls = new StringObject();
                //IntegerObject uniq_categ = new IntegerObject();

                //IntegerObject clsID = new IntegerObject();
                //IntegerObject linkID = new IntegerObject();
                //IntegerObject categID = new IntegerObject();
                //CMValue cmv = new CMValue();
                Q.reset_name_scope();
                ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
                if(Q.bulk_return_link(set_source_links, retVals)!=QClass.APIFail){
                    for(Return_Link_Row row:retVals){
                        targetTerms.add(row.get_v1_cls());
                        targetTermsLinkIdL.add(row.get_Neo4j_NodeId());
                    }
                }
                /*
                while (Q.retur_full_link_id(set_source_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                    targetTerms.add(cls.getValue());
                    targetTermsLinkId.add(linkID.getValue());
                }
                */
                Q.free_set(set_source_links);
                //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

                int ret = QClass.APISucc;
                StringObject prevThes = new StringObject();
                TA.GetThesaurusNameWithoutPrefix(prevThes);
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                }

                for (int k = 0; k < targetTerms.size(); k++) {
                    ret = TA.CHECK_DeleteNewDescriptorAttribute(targetTermsLinkIdL.get(k), new StringObject(targetTerms.get(k)));
                    if (ret == QClass.APIFail) {
                        errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Deletion/ReferenceDeletionError", new String[] {targetTerms.get(k)}));
                        //errorMsg.setValue("Deletion error occurred while deleting source references for term "+ dbGen.removePrefix(targetTerms.get(k))+". Deletion cancelled.");
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                        return false;
                    }
                }
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }

                if (howmanyfromCurrent != howmanyRefs) {
                    errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Deletion/OtherThesauriReferences", null));
                    //errorMsg.setValue("Source references from current thesaurus were deleted successfully. The source could not be though deleted due to references from other thesauri of the database.");
                    Q.TEST_end_transaction();
                    return false;
                }
            }

            ArrayList<String> old_source_note = dbGen.returnResults_Source(SessionUserInfo, targetSource, targetField, Q, TA, sis_session);
            if (old_source_note.size() == 1 && old_source_note.get(0).compareTo(newValue) == 0) {
                return true;
            }

            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

            StringObject sourceClassObj = new StringObject(ConstantParameters.SourceClass);
            StringObject sourceNoteLinkObj = new StringObject(source_note_kwd);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }

            int ret = TA.DeleteDescriptorComment(targetSourceObj, sourceClassObj, sourceNoteLinkObj);
            if (ret ==TMSAPIClass.TMS_APIFail) {
                //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
                return false;
            }

            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            ret = TA.CHECK_DeleteSource(targetSourceObj);
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg.setValue(dbGen.check_success(ret, TA, null, tms_session));
                return false;
            }

            return true;

            //</editor-fold>
        } else if (targetField.compareTo(source_rename_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Source Rename...">
            if (targetSource == null || targetSource.trim().length() == 0) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/EmptyName", null));
                //errorMsg.setValue("No name is specified for the source to rename. Renaming operation cancelled.");
                return false;
            }


            if (newValue == null || newValue.trim().length() == 0) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/EmptyNewName", null));
                //errorMsg.setValue("No new name is specified for the source to rename. Renaming operation cancelled.");
                return false;
            }
            /*
            try {
                
                byte[] byteArray = newValue.getBytes("UTF-8");

                int maxSourceNameChars = dbtr.getMaxBytesForSource(SessionUserInfo.selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxSourceNameChars) {
                    errorArgs.add(""+maxSourceNameChars);
                    errorArgs.add(""+byteArray.length);
                    dbGen.Translate(errorMsg, "root/EditSource/Rename/LongName", errorArgs, pathToMessagesXML);
                    
                    return false;
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOut(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
                */

            Q.reset_name_scope();
            if (Q.set_current_node(targetSourceObj) == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/NotFound", new String[]{targetSource}));                
                //errorMsg.setValue("Source given for rename: %s was not found in the database. Please refresh the page contents and try again. Deletion cancelled.");
                return false;
            }

            newValue = newValue.replaceAll(" +", " ");
            newValue = newValue.trim();
            StringObject newSourceNameObj = new StringObject(prefixSource.concat(newValue));

            Q.reset_name_scope();
            if (Q.set_current_node(newSourceNameObj) != QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/AlreadyInDB", null));
                //errorMsg.setValue("New source name selected already exists in the database. Please select another source name.");
                return false;
            }

            //rename source node
            int ret = Q.CHECK_Rename_Node(new Identifier(targetSourceObj.getValue()), new Identifier(newSourceNameObj.getValue()));

            //check result
            if (ret == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/RenameFailure", null));
                //errorMsg.setValue("Rename Failure.");
                return false;
            }

            //check if this source had source note node
            Q.reset_name_scope();
            StringObject sourceNoteNodeObj = new StringObject(targetSourceObj.getValue().concat("`" + source_note_kwd));
            if (Q.set_current_node(sourceNoteNodeObj) != QClass.APIFail) {
                //if it had it must be renamed also
                Q.reset_name_scope();
                StringObject newSourceNoteNodeObj = new StringObject(newSourceNameObj.getValue().concat("`" + source_note_kwd));
                ret = Q.CHECK_Rename_Node(new Identifier(sourceNoteNodeObj.getValue()), new Identifier(newSourceNoteNodeObj.getValue()));
            }

            //check result of rename source note node if it existed
            if (ret == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/Rename/RenameFailure", null));
                //errorMsg.setValue("Rename Failure.");
                return false;
            } else {
                errorMsg.setValue("<newName>" + newValue + "</newName>");
                return true;
            }
            //</editor-fold>
        } else if (targetField.compareTo(source_note_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Source Note...">3

            if (newValue != null) {
                newValue = newValue.replaceAll(" +", " ");
                newValue = newValue.trim();
            }

            ArrayList<String> old_source_note = dbGen.returnResults_Source(SessionUserInfo, targetSource, targetField, Q,TA, sis_session);
            if (old_source_note.size() == 1 && old_source_note.get(0).compareTo(newValue) == 0) {
                return true;
            }

            if (newValue != null) {

                if (newValue.length() > 0) {

                    /*
                    try {
                        byte[] byteArray = newValue.getBytes("UTF-8");

                        int maxSourceNoteChars = dbtr.getMaxBytesForCommentCategory(SessionUserInfo.selectedThesaurus, Q, sis_session);
                        if (byteArray.length > maxSourceNoteChars) {
                            errorArgs.clear();
                            errorArgs.add(""+maxSourceNoteChars);
                            errorArgs.add(""+byteArray.length);
                            dbGen.Translate(errorMsg, "root/EditSource/Source_Note/LongName", errorArgs, pathToMessagesXML);
                            return false;
                        }
                    } catch (UnsupportedEncodingException ex) {
                        Utils.StaticClass.webAppSystemOut(ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }
                    */
                }
            }

            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

            StringObject sourceClassObj = new StringObject(ConstantParameters.SourceClass);
            StringObject sourceNoteLinkObj = new StringObject(source_note_kwd);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }

            int ret = TA.DeleteDescriptorComment(targetSourceObj, sourceClassObj, sourceNoteLinkObj);
            if (ret == TMSAPIClass.TMS_APIFail) {
                //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                //reset to previous thesaurus name if needed
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }
                return false;
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }

            if (newValue != null) {

                if (newValue.length() > 0) {

                    prevThes = new StringObject();
                    TA.GetThesaurusNameWithoutPrefix(prevThes);
                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                    }

                    ret = TA.SetDescriptorComment(targetSourceObj, new StringObject(newValue), sourceClassObj, sourceNoteLinkObj);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        //errorMsg.setValue(WTA.errorMessage.getValue());
                        TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                        return false;
                    }
                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                }
            }


            //</editor-fold>
        } else if (targetField.compareTo(source_move_references_kwd) == 0) {
            //<editor-fold defaultstate="collapsed" desc="Move References to source and delete...">

            DBConnect_Term dbCon = new DBConnect_Term();
            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

            //merge source note 
            String oldTargetSourceNote = null;
            String newTargetSourceNote = null;
            ArrayList<String> sourceNotes = new ArrayList<String>();
            StringObject sourceClassObj = new StringObject(ConstantParameters.SourceClass);
            StringObject sourceNoteLinkObj = new StringObject(source_note_kwd);
            StringObject newTargetSourceObj = new StringObject(prefixSource.concat(newValue));
            StringObject translations_found_in_Class = new StringObject();
            StringObject translations_found_in_Link = new StringObject();
            StringObject primary_found_in_Class = new StringObject();
            StringObject primary_found_in_Link = new StringObject();

            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translations_found_in_kwd, translations_found_in_Class, translations_found_in_Link, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.primary_found_in_kwd, primary_found_in_Class, primary_found_in_Link, Q, sis_session);


            sourceNotes.addAll(dbGen.returnResults_Source(SessionUserInfo, targetSource, ConstantParameters.source_note_kwd, Q, TA, sis_session));
            if (sourceNotes.size() > 0 && sourceNotes.get(0) != null && sourceNotes.get(0).trim().length() > 0) {
                oldTargetSourceNote = sourceNotes.get(0);
            }
            sourceNotes.clear();

            sourceNotes.addAll(dbGen.returnResults_Source(SessionUserInfo, newValue, ConstantParameters.source_note_kwd, Q,TA,  sis_session));
            if (sourceNotes.size() > 0 && sourceNotes.get(0) != null && sourceNotes.get(0).trim().length() > 0) {
                newTargetSourceNote = sourceNotes.get(0);
            }

            if (oldTargetSourceNote != null) { //Data that must not be lost and source note of newValue source must be updated

                if (newTargetSourceNote != null) {

                    if (newTargetSourceNote.compareTo(oldTargetSourceNote) != 0 && newTargetSourceNote.endsWith("### " + oldTargetSourceNote) == false) {

                        newTargetSourceNote = newTargetSourceNote + " ### " + oldTargetSourceNote;

                        StringObject prevThes = new StringObject();
                        TA.GetThesaurusNameWithoutPrefix(prevThes);
                        if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                            TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                        }
                        int ret = TA.DeleteDescriptorComment(newTargetSourceObj, sourceClassObj, sourceNoteLinkObj);
                        if (ret == TMSAPIClass.TMS_APIFail) {
                            //errorMsg.setValue(" " + WTA.errorMessage.getValue());
                            TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                            //reset to previous thesaurus name if needed
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                            return false;
                        }
                        if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                    }
                } else {
                    newTargetSourceNote = oldTargetSourceNote;
                }

                newTargetSourceNote = newTargetSourceNote.replaceAll(" +", " ");
                newTargetSourceNote = newTargetSourceNote.trim();

                StringObject prevThes = new StringObject();
                TA.GetThesaurusNameWithoutPrefix(prevThes);
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                }
                int ret = TA.SetDescriptorComment(newTargetSourceObj, new StringObject(newTargetSourceNote), sourceClassObj, sourceNoteLinkObj);
                if (ret == TMSAPIClass.TMS_APIFail) {
                    //errorMsg.setValue(WTA.errorMessage.getValue());
                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    return false;
                }
                if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(prevThes.getValue());
                }

            }


            ArrayList<Long> deleteIDsL = new ArrayList<Long>();
            ArrayList<String> targetTerms = new ArrayList<String>();
            //StringObject label = new StringObject();
            //StringObject sclass = new StringObject();
            //IntegerObject sysid = new IntegerObject();


            Q.reset_name_scope();
            if (Q.set_current_node(targetSourceObj) == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditSource/MoveReferences/NotFound", new String[]{targetSource}));
                //errorMsg.setValue("Source " + targetSource + " was not found in the database. Please refresh the source's search results.");
                return false;
            }

            //these links must be deleted from their from value terms
            int deleteEtLinks = Q.get_link_to_by_category(0, translations_found_in_Class, translations_found_in_Link);
            Q.reset_set(deleteEtLinks);
            int etLinks = Q.set_get_card(deleteEtLinks);

            int deleteGtLinks = Q.get_link_to_by_category(0, primary_found_in_Class, primary_found_in_Link);
            Q.reset_set(deleteGtLinks);
            int gtLinks = Q.set_get_card(deleteGtLinks);

            if (etLinks == 0 && gtLinks == 0) {
                Q.free_set(deleteEtLinks);
                Q.free_set(deleteGtLinks);
                return true;
            }

            //collect link ids for deletion
            if (etLinks > 0) {
                Q.reset_name_scope();
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(deleteEtLinks, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                            deleteIDsL.add(row.get_Neo4j_NodeId());
                        }
                    }
                }
/*
                while (Q.retur_full_nodes(deleteEtLinks, sysid, label, sclass) != QClass.APIFail) {
                    if (!deleteIDsL.contains(sysid.getValue())) {
                        deleteIDsL.add(sysid.getValue());
                    }
                }
*/
                //find out from values -- terms of links to be deleted 
                int set_affected_terms = Q.get_from_value(deleteEtLinks);
                Q.reset_set(set_affected_terms);
                Q.free_set(deleteEtLinks);

                if (Q.set_get_card(set_affected_terms) > 0) {

                    retVals.clear();
                    if(Q.bulk_return_nodes(set_affected_terms, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!targetTerms.contains(row.get_v1_cls_logicalname())) {
                                targetTerms.add(row.get_v1_cls_logicalname());
                            }
                        }
                    }
                    /*
                    while (Q.retur_nodes(set_affected_terms, label) != QClass.APIFail) {
                        if (!targetTerms.contains(label.getValue())) {
                            targetTerms.add(label.getValue());
                        }
                    }*/
                }

                for (int i = 0; i < targetTerms.size(); i++) {

                    boolean addNewET = false;

                    StringObject targetTermObj = new StringObject(targetTerms.get(i));
                    Q.reset_name_scope();
                    Q.set_current_node(targetTermObj);

                    ArrayList<Long> deletenewTargetIDsL = new ArrayList<Long>();
                    int selected_new_target_category_nodes = Q.get_link_from_by_category(0, translations_found_in_Class, translations_found_in_Link);
                    Q.reset_set(selected_new_target_category_nodes);

                    if (Q.set_get_card(selected_new_target_category_nodes) > 0) {

                        retVals.clear();
                        if(Q.bulk_return_nodes(selected_new_target_category_nodes, retVals)!=QClass.APIFail){
                            for(Return_Nodes_Row row:retVals){
                                if (!deletenewTargetIDsL.contains(row.get_Neo4j_NodeId()) && deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                    deletenewTargetIDsL.add(row.get_Neo4j_NodeId());
                                }
                            }
                        }
                        /*while (Q.retur_full_nodes(selected_new_target_category_nodes, sysid, label, sclass) != QClass.APIFail) {
                            if (!deletenewTargetIDs.contains(sysid.getValue()) && deleteIDs.contains(sysid.getValue())) {
                                deletenewTargetIDs.add(sysid.getValue());
                            }
                        }*/

                        int set_existing_Et_links = Q.get_to_value(selected_new_target_category_nodes);
                        Q.reset_set(set_existing_Et_links);
                        Q.reset_name_scope();
                        Q.set_current_node(newTargetSourceObj);
                        if (Q.set_member_of(set_existing_Et_links) == QClass.APIFail) {
                            addNewET = true;
                        }
                        Q.free_set(set_existing_Et_links);

                    } else {
                        addNewET = true;
                    }
                    Q.free_set(selected_new_target_category_nodes);


                    //Deletions + additions of ETs
                    if (deletenewTargetIDsL.size() > 0) {

                        int KindOfDescr = dbGen.GetKindOfDescriptor(SessionUserInfo.selectedThesaurus, targetTermObj, Q, sis_session);

                        if (KindOfDescr == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {

                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }
                            for (int k = 0; k < deletenewTargetIDsL.size(); k++) {
                                int ret = TA.CHECK_DeleteNewDescriptorAttribute(deletenewTargetIDsL.get(k).intValue(), targetTermObj);
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    //reset to previous thesaurus name if needed
                                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    return false;
                                }
                            }

                        } else {

                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }
                            for (int k = 0; k < deletenewTargetIDsL.size(); k++) {
                                int ret = TA.CHECK_DeleteDescriptorAttribute(deletenewTargetIDsL.get(k).intValue(), targetTermObj);
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    //reset to previous thesaurus name if needed
                                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    return false;
                                }
                            }
                            //reset to previous thesaurus name if needed
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                        }
                        deletenewTargetIDsL.clear();
                    }

                    if (addNewET) {
                        ArrayList<String> newValues = new ArrayList<String>();
                        newValues.add(newValue);
                        errorMsg.setValue(dbCon.connectSources(SessionUserInfo.selectedThesaurus, targetTermObj, newValues, DBConnect_Term.CATEGORY_translations_found_in, Q, sis_session, dbGen, TA, tms_session));
                        if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                            return false;
                        }
                    }
                }

                targetTerms.clear();
                deleteIDsL.clear();
                Q.free_set(set_affected_terms);

            }



            if (gtLinks > 0) {
                Q.reset_name_scope();
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(deleteGtLinks, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                            deleteIDsL.add(row.get_Neo4j_NodeId());
                        }
                    }
                }
                /*while (Q.retur_full_nodes(deleteGtLinks, sysid, label, sclass) != QClass.APIFail) {
                    if (!deleteIDs.contains(sysid.getValue())) {
                        deleteIDs.add(sysid.getValue());
                    }
                }*/

                //find out from values -- terms of links to be deleted 
                int set_affected_terms = Q.get_from_value(deleteGtLinks);
                Q.reset_set(set_affected_terms);
                Q.free_set(deleteGtLinks);

                if (Q.set_get_card(set_affected_terms) > 0) {
                    retVals.clear();
                    if(Q.bulk_return_nodes(set_affected_terms, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!targetTerms.contains(row.get_v1_cls_logicalname())) {
                                targetTerms.add(row.get_v1_cls_logicalname());
                            }
                        }
                    }
                    /*while (Q.retur_nodes(set_affected_terms, label) != QClass.APIFail) {
                        if (!targetTerms.contains(label.getValue())) {
                            targetTerms.add(label.getValue());
                        }
                    }*/
                }

                for (int i = 0; i < targetTerms.size(); i++) {

                    boolean addNewGT = false;

                    StringObject targetTermObj = new StringObject(targetTerms.get(i));
                    Q.reset_name_scope();
                    Q.set_current_node(targetTermObj);

                    ArrayList<Long> deletenewTargetIDsL = new ArrayList<Long>();
                    int selected_new_target_category_nodes = Q.get_link_from_by_category(0, primary_found_in_Class, primary_found_in_Link);
                    Q.reset_set(selected_new_target_category_nodes);

                    if (Q.set_get_card(selected_new_target_category_nodes) > 0) {
                        
                        retVals.clear();
                        if(Q.bulk_return_nodes(selected_new_target_category_nodes, retVals)!=QClass.APIFail){
                            for(Return_Nodes_Row row:retVals){
                                if (!deletenewTargetIDsL.contains(row.get_Neo4j_NodeId()) && deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                    deletenewTargetIDsL.add(row.get_Neo4j_NodeId());
                                }
                            }
                        }
                        /*while (Q.retur_full_nodes(selected_new_target_category_nodes, sysid, label, sclass) != QClass.APIFail) {
                            if (!deletenewTargetIDs.contains(sysid.getValue()) && deleteIDs.contains(sysid.getValue())) {
                                deletenewTargetIDs.add(sysid.getValue());
                            }
                        }*/

                        int set_existing_Gt_links = Q.get_to_value(selected_new_target_category_nodes);
                        Q.reset_set(set_existing_Gt_links);
                        Q.reset_name_scope();
                        Q.set_current_node(newTargetSourceObj);
                        if (Q.set_member_of(set_existing_Gt_links) == QClass.APIFail) {
                            addNewGT = true;
                        }
                        Q.free_set(set_existing_Gt_links);

                    } else {
                        addNewGT = true;
                    }
                    Q.free_set(selected_new_target_category_nodes);


                    //Deletions + additions of ETs
                    if (deletenewTargetIDsL.size() > 0) {

                        int KindOfDescr = dbGen.GetKindOfDescriptor(SessionUserInfo.selectedThesaurus, targetTermObj, Q, sis_session);

                        if (KindOfDescr == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {

                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }
                            for (int k = 0; k < deletenewTargetIDsL.size(); k++) {
                                int ret = TA.CHECK_DeleteNewDescriptorAttribute(deletenewTargetIDsL.get(k).intValue(), targetTermObj);
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    //reset to previous thesaurus name if needed
                                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    return false;
                                }
                            }

                        } else {
                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }

                            for (int k = 0; k < deletenewTargetIDsL.size(); k++) {
                                int ret = TA.CHECK_DeleteDescriptorAttribute(deletenewTargetIDsL.get(k).intValue(), targetTermObj);
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    //reset to previous thesaurus name if needed
                                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    return false;
                                }
                            }
                            //reset to previous thesaurus name if needed
                            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                        }
                        deletenewTargetIDsL.clear();
                    }

                    if (addNewGT) {
                        ArrayList<String> newValues = new ArrayList<String>();
                        newValues.add(newValue);
                        errorMsg.setValue(dbCon.connectSources(SessionUserInfo.selectedThesaurus, targetTermObj, newValues, DBConnect_Term.CATEGORY_PRIMARY_FOUND_IN, Q, sis_session, dbGen, TA, tms_session));
                        if (errorMsg.getValue() != null && errorMsg.getValue().length() > 0) {
                            return false;
                        }
                    }
                }

                Q.free_set(set_affected_terms);

            }
            return true;
            //</editor-fold>
        }

        return true;
    }
}
