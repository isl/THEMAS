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
import Utils.Utilities;


import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import neo4j_sisapi.*;

/**
 *
 * @author tzortzak
 */
public class DBEditGuideTerms {

    public DBEditGuideTerms() {
    }

    public boolean addGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session, String newGuideTerm, StringObject errorMsg) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        ArrayList<String> guideTermLinks = new ArrayList<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(newGuideTerm)) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Creation/Exists", new String[]{newGuideTerm}));
            //errorMsg.setValue("Guide Term '" + newGuideTerm + "' is already defined in the database.");
            return false;
        }

        if (newGuideTerm != null && newGuideTerm.length() > 0) {
            /*
            try {
                byte[] byteArray = newGuideTerm.getBytes("UTF-8");

                
                int maxChars = dbtr.getMaxBytesForGuideTerm(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxChars) {
                    errorArgs.clear();
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
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Creation/AdditionError", new String[]{newGuideTerm}));
            //errorMsg.setValue("Creation failure of new Guide Term: " + newGuideTerm);
            return false;
        }

        //Q.reset_name_scope();
        //Q.set_current_node(BTClassObj);
        //long newLinkIdL = Q.set_current_node(newLinkObj);

        if (newCategIdent.getSysid() == QClass.APIFail) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Creation/AccessError", new String[]{newGuideTerm}));
            //errorMsg.setValue("Failed to refer to the new Guide Term: " + newGuideTerm);
            return false;
        }
        

        ret = Q.CHECK_Add_IsA(newCategIdent, new Identifier(bt_LinkIdL));
        if (ret == QClass.APIFail) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Creation/InstatiationError", new String[]{newGuideTerm}));
            //errorMsg.setValue("Failed to classify new guide term %s under BT category.");
            return false;
        }

        for (int k = 0; k < newGuideTermInstanceOfL.length; k++) {
            //ret = Q.CHECK_Add_Instance(new Identifier(newLinkIdL), new Identifier(newGuideTermInstanceOfL[k]));
            ret = Q.CHECK_Add_Instance(newCategIdent, new Identifier(newGuideTermInstanceOfL[k]));
            if (ret == QClass.APIFail) {
                errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Creation/InstatiationIDError", new String[]{newGuideTerm,""+newGuideTermInstanceOfL[k]}));
                //errorMsg.setValue("Failed to classify new guide term %s under link-node with id: " + newGuideTermInstanceOf[k]);
                return false;
            }
        }
        return true;
    }

    public boolean deleteGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session, String deleteGuideTerm, StringObject errorMsg) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        ArrayList<String> guideTermLinks = new ArrayList<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(deleteGuideTerm) == false) {
            
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Delete/NotFound", new String[]{deleteGuideTerm}));
            //errorMsg.setValue(Guide Term '" + deleteGuideTerm + "' was not found in the database.");
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
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Delete/BTLinksCopyError", new String[]{deleteGuideTerm}));
            //errorMsg.setValue("Copying failure of BT links of guide term: " + deleteGuideTerm);
            return false;
        }

        ret = Q.CHECK_IMPROVE_Delete_Instance_Set(set_move_instances, new Identifier(fromIdL));
        if (ret == QClass.APIFail) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Delete/BTLinksDeletionError", new String[]{deleteGuideTerm}));
            //errorMsg.setValue("Deletion failure of BT links of guide term: " + deleteGuideTerm);
            return false;
        }

        Q.reset_name_scope();

        Identifier fromClsIdentifier = new Identifier(fromclsidL); //ALLGRETHEDescriptor
        Identifier fromIdIdentifier = new Identifier(fromIdL);     //ALLGRETHE_BTMyGuideTermLink
        ret = Q.CHECK_Delete_Named_Attribute(fromIdIdentifier, fromClsIdentifier);
        if (ret == QClass.APIFail) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Delete/GeneralError", new String[]{deleteGuideTerm}));
            //errorMsg.setValue("Deletion failure of guide term: " + deleteGuideTerm);
            return false;
        }
        return true;
    }

    public boolean renameGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session,
            String renameGuideTermFrom, String renameGuideTermTo, StringObject errorMsg) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        
        if (renameGuideTermTo != null && renameGuideTermTo.length() > 0) {
            /*
            try {
                byte[] byteArray = renameGuideTermTo.getBytes("UTF-8");

                int maxChars = dbtr.getMaxBytesForGuideTerm(selectedThesaurus, Q, sis_session);
                if (byteArray.length > maxChars) {
                    errorArgs.clear();
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

        ArrayList<String> guideTermLinks = new ArrayList<String>();
        guideTermLinks.addAll(dbGen.collectGuideLinks(selectedThesaurus, Q, sis_session));

        if (guideTermLinks.contains(renameGuideTermFrom) == false) {

            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Rename/NotFound", new String[]{renameGuideTermFrom}));
            //errorMsg.setValue("Guide Term '" + renameGuideTermFrom + "' was not found in the database.");
            return false;
        }

        if (guideTermLinks.contains(renameGuideTermTo)) {
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Rename/NewNameExists", new String[]{renameGuideTermFrom}));
            //errorMsg.setValue("New Guide Term name '" + renameGuideTermTo + "' is already defined in the database.");
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
            
            errorMsg.setValue(u.translateFromMessagesXML("root/EditGuideTerms/Rename/GeneralError", new String[]{renameGuideTermFrom,renameGuideTermTo}));
            //errorMsg.setValue("Renaming action of Guide Term  '"+renameGuideTermFrom+"'\n to '"+renameGuideTermTo+"' failed.");
            return false;
        }

        return true;
    }
}
