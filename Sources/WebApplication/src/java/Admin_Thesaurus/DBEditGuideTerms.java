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

import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Utils.ConstantParameters;


import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import neo4j_sisapi.*;

/**
 *
 * @author tzortzak
 */
public class DBEditGuideTerms {

    public DBEditGuideTerms() {
    }

    public boolean addGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session, String newGuideTerm, StringObject errorMsg, String pathToMessagesXML) {


        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Vector<String> errorArgs = new Vector<String>();

        Vector<String> guideTermLinks = new Vector<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(newGuideTerm)) {
            errorArgs.add(newGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/Exists", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Η ετικέτα δεσμού '" + newGuideTerm + "' χρησιμοποιείται ήδη στην βάση.");
            return false;
        }

        if (newGuideTerm != null && newGuideTerm.length() > 0) {
            /*
            try {
                byte[] byteArray = newGuideTerm.getBytes("UTF-8");

                
                int maxChars = dbtr.getMaxBytesForGuideTerm(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxChars) {
                    errorArgs.add("" + maxChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/LongName", errorArgs, pathToMessagesXML);                    
                    return false;
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOut(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
        }

        StringObject HierarchyTermObj = new StringObject();
        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();
        StringObject newLinkObj = new StringObject();

        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), HierarchyTermObj);
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);
        newLinkObj.setValue(BTLinkObj.getValue().concat(newGuideTerm));

        Q.reset_name_scope();
        long hierarchyTermIdL = Q.set_current_node(HierarchyTermObj);

        Q.reset_name_scope();
        long descriptorIdL = Q.set_current_node(BTClassObj);
        long bt_LinkIdL = Q.set_current_node(BTLinkObj);

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(TMSAPIClass.INDIVIDUAL));
        long dagIDL = Q.set_current_node(new StringObject(TMSAPIClass._DIRECTED_ACYCLIC_GRAPH));

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(TMSAPIClass.INDIVIDUAL));
        long necessaryIDL = Q.set_current_node(new StringObject(TMSAPIClass._NECESSARY));

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(TMSAPIClass.INDIVIDUAL));
        long interconnectedIDL = Q.set_current_node(new StringObject(TMSAPIClass._INTERCONNECTED));

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(TMSAPIClass.INDIVIDUAL));
        long backwards_sortedIDL = Q.set_current_node(new StringObject(TMSAPIClass._BACKWARDS_SORTED));

        Q.reset_name_scope();
        Q.set_current_node(new StringObject("ThesaurusNotionType"));
        long hierarchical_assosiation_typeIDL = Q.set_current_node(new StringObject("hierarchical_association_Type"));

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(selectedThesaurus.concat("ThesaurusNotionType")));
        long relationIDL = Q.set_current_node(new StringObject(selectedThesaurus.concat("_relation")));

        long[] newGuideTermInstanceOfL = {dagIDL, necessaryIDL, interconnectedIDL, backwards_sortedIDL, hierarchical_assosiation_typeIDL, relationIDL};

        //create new guide term
        Identifier fromClassIdent = new Identifier(descriptorIdL);
        Identifier newCategIdent = new Identifier(newLinkObj.getValue());
        CMValue toClassCMV = new CMValue();
        toClassCMV.assign_node(HierarchyTermObj.getValue(), hierarchyTermIdL);

        int ret = Q.CHECK_Add_Named_Attribute(newCategIdent, fromClassIdent, toClassCMV, QClass.SIS_API_S_CLASS, -1,true);

        if (ret == QClass.APIFail) {
            errorArgs.add(newGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/AdditionError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία δημιουργίας νέας ετικέτας δεσμού: " + newGuideTerm);
            return false;
        }

        //Q.reset_name_scope();
        //Q.set_current_node(BTClassObj);
        //long newLinkIdL = Q.set_current_node(newLinkObj);

        if (newCategIdent.getSysid() == QClass.APIFail) {
            errorArgs.add(newGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/AccessError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία πρόσβασης στην νέα ετικέτα δεσμού: " + newGuideTerm);
            return false;
        }
        

        ret = Q.CHECK_Add_IsA(newCategIdent, new Identifier(bt_LinkIdL));
        if (ret == QClass.APIFail) {
            errorArgs.add(newGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/InstatiationError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία υπαγωγής της νέας ετικέτας δεσμού κάτω από τον σύνδεσμο ΠΟ");
            return false;
        }

        for (int k = 0; k < newGuideTermInstanceOfL.length; k++) {
            //ret = Q.CHECK_Add_Instance(new Identifier(newLinkIdL), new Identifier(newGuideTermInstanceOfL[k]));
            ret = Q.CHECK_Add_Instance(newCategIdent, new Identifier(newGuideTermInstanceOfL[k]));
            if (ret == QClass.APIFail) {
                errorArgs.add(""+newGuideTermInstanceOfL[k]);
                dbGen.Translate(errorMsg, "root/EditGuideTerms/Creation/InstatiationIDError", errorArgs, pathToMessagesXML);
                //errorMsg.setValue("Αποτυχία ένταξης της νέας ετικέτας δεσμού κάτω από τον σύνδεσμο με sys id: " + newGuideTermInstanceOf[k]);
                return false;
            }
        }
        return true;
    }

    public boolean deleteGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session, String deleteGuideTerm, StringObject errorMsg, String pathToMessagesXML) {

        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Vector<String> errorArgs = new Vector<String>();
        Vector<String> guideTermLinks = new Vector<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(deleteGuideTerm) == false) {
            errorArgs.add(deleteGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Delete/NotFound", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Η ετικέτα δεσμού '" + deleteGuideTerm + "' δεν βρέθηκε στην βάση.");
            return false;
        }

        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();
        StringObject deleteGuideTermObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);
        deleteGuideTermObj.setValue(BTLinkObj.getValue().concat(deleteGuideTerm));

        Q.reset_name_scope();
        long fromclsidL = Q.set_current_node(BTClassObj);
        long fromIdL = Q.set_current_node(deleteGuideTermObj);
        int set_move_instances = Q.get_instances(0);
        Q.reset_set(set_move_instances);

        Q.reset_name_scope();

        Q.set_current_node(BTClassObj);
        long toIdL = Q.set_current_node(BTLinkObj);
        int ret = Q.CHECK_IMPROVE_Add_Instance_Set(set_move_instances, new Identifier(toIdL));
        if (ret == QClass.APIFail) {
            errorArgs.add(deleteGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Delete/BTLinksCopyError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία αντιγραφής των συνδέσμων ΠΟ της ετικέτας δεσμού: " + deleteGuideTerm);
            return false;
        }

        ret = Q.CHECK_IMPROVE_Delete_Instance_Set(set_move_instances, new Identifier(fromIdL));
        if (ret == QClass.APIFail) {
            errorArgs.add(deleteGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Delete/BTLinksDeletionError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία διαγραφής των συνδέσμων ΠΟ της ετικέτας δεσμού: " + deleteGuideTerm);
            return false;
        }

        Q.reset_name_scope();

        Identifier fromClsIdentifier = new Identifier(fromclsidL); //ALLGRETHEDescriptor
        Identifier fromIdIdentifier = new Identifier(fromIdL);     //ALLGRETHE_BTMyGuideTermLink
        ret = Q.CHECK_Delete_Named_Attribute(fromIdIdentifier, fromClsIdentifier);
        if (ret == QClass.APIFail) {
            errorArgs.add(deleteGuideTerm);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Delete/GeneralError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Αποτυχία διαγραφής της ετικέτας δεσμού: " + deleteGuideTerm);
            return false;
        }
        return true;
    }

    public boolean renameGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session,
            String renameGuideTermFrom, String renameGuideTermTo, StringObject errorMsg, String pathToMessagesXML) {
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Vector<String> errorArgs = new Vector<String>();

        if (renameGuideTermTo != null && renameGuideTermTo.length() > 0) {
            /*
            try {
                byte[] byteArray = renameGuideTermTo.getBytes("UTF-8");

                int maxChars = dbtr.getMaxBytesForGuideTerm(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxChars) {

                    errorArgs.add("" + maxChars);
                    errorArgs.add("" + byteArray.length);
                    dbGen.Translate(errorMsg, "root/EditGuideTerms/Rename/LongName", errorArgs, pathToMessagesXML);
                    return false;
                }
            } catch (UnsupportedEncodingException ex) {
                Utils.StaticClass.webAppSystemOut(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            */
        }

        Vector<String> guideTermLinks = new Vector<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(renameGuideTermFrom) == false) {

            errorArgs.add(renameGuideTermFrom);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Rename/NotFound", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Η ετικέτα δεσμού '" + renameGuideTermFrom + "' δεν βρέθηκε στην βάση.");
            return false;
        }

        if (guideTermLinks.contains(renameGuideTermTo)) {
            errorArgs.add(renameGuideTermFrom);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Rename/NewNameExists", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Η ετικέτα δεσμού '" + renameGuideTermTo + "' χρησιμοποιείται ήδη στην βάση.");
            return false;
        }

        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();
        StringObject renameGuideTermFromObj = new StringObject();
        StringObject renameGuideTermToObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);

        renameGuideTermFromObj.setValue(BTLinkObj.getValue().concat(renameGuideTermFrom));
        renameGuideTermToObj.setValue(BTLinkObj.getValue().concat(renameGuideTermTo));

        Q.reset_name_scope();
        long descriptorIdL = Q.set_current_node(BTClassObj);
        long tagetIdL = Q.set_current_node(renameGuideTermFromObj);
        Q.reset_name_scope();
        int ret = Q.CHECK_Rename_Named_Attribute(new Identifier(tagetIdL), new Identifier(descriptorIdL), new Identifier(renameGuideTermToObj.getValue()));
        if (ret == QClass.APIFail) {
            errorArgs.add(renameGuideTermFrom);
            errorArgs.add(renameGuideTermTo);
            dbGen.Translate(errorMsg, "root/EditGuideTerms/Rename/GeneralError", errorArgs, pathToMessagesXML);
            //errorMsg.setValue("Η μετονομασία της ετικέτας δεσμού: '"+renameGuideTermFrom+"'\n σε '"+renameGuideTermTo+"' απέτυχε.");
            return false;
        }

        return true;
    }
}
