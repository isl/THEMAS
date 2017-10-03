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
package Utils;


import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Utils.StringLocaleComparator;
import Users.UserInfoClass;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import neo4j_sisapi.*;

import javax.xml.xpath.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.util.regex.Matcher;
import java.io.OutputStreamWriter;

/**
 *
 * @author tzortzak
 */
public class ConsistensyCheck {
    
  
    public final String Create_Modify_XML_STR = "CREATE_MODIFY";
    public final String MoveToHier_XML_STR = "MOVE_TO_HIERARCHY";
    
    //final static int FixData_POLICY = 0;
    public final static int IMPORT_COPY_MERGE_THESAURUS_POLICY = 1;
    public final static int EDIT_TERM_POLICY = 2;
    
 

    public ConsistensyCheck() {

    }


    /* 
     * moveAction: MOVE_NODE_ONLY   MOVE_NODE_AND_SUBTREE     CONNECT_NODE_AND_SUBTREE
     */
    
    public boolean check_Move_To_Hier_Consistencies(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session, DBGeneral dbGen, String prefix, StringObject errorMsg,String pathToErrorsXML, String descriptor, String moveAction, String newBT) {

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\nChecking : " + descriptor + " with " + newBT + " concering action : " + moveAction);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue());
        
        ArrayList<Integer> moveHierarchyChecks = new ArrayList<Integer>();
        /*
        case 1: {move_To_Hierarchy_Consistency_Test_1
        case 2: {move_To_Hierarchy_Consistency_Test_2
        case 3: {move_To_Hierarchy_Consistency_Test_3
        case 4: {move_To_Hierarchy_Consistency_Test_4
        case 5: {move_To_Hierarchy_Consistency_Test_5
        case 6: {move_To_Hierarchy_Consistency_Test_6
        case 7: {create_modify_check_27
        case 8: {create_modify_check_27
        */
        if (moveAction.matches("MOVE_NODE_ONLY")) {
            moveHierarchyChecks.add(9);
            moveHierarchyChecks.add(1);
            moveHierarchyChecks.add(2);
            moveHierarchyChecks.add(3);


        } else if (moveAction.matches("MOVE_NODE_AND_SUBTREE")) {
            moveHierarchyChecks.add(9);
            moveHierarchyChecks.add(1);
            moveHierarchyChecks.add(4);
            moveHierarchyChecks.add(7);

        } else if (moveAction.matches("CONNECT_NODE_AND_SUBTREE")) {
            moveHierarchyChecks.add(9);
            moveHierarchyChecks.add(1);
            moveHierarchyChecks.add(5);
            moveHierarchyChecks.add(6);
            moveHierarchyChecks.add(8);
        }

        for (int i = 0; i <moveHierarchyChecks.size(); i++) {

            switch (moveHierarchyChecks.get(i).intValue()) {

                case 1: {
                    //Applies to moveActions: MOVE_NODE_ONLY   MOVE_NODE_AND_SUBTREE     CONNECT_NODE_AND_SUBTREE
                    //Check if term is Top Term 
                    descriptor = descriptor;
                    newBT = newBT;
                    if(move_To_Hierarchy_Consistency_Test_1(SessionUserInfo.selectedThesaurus, Q,  sis_session,   dbGen, errorMsg, pathToErrorsXML, descriptor,  prefix)==false){
                        return false;
                    }
                    break;  
                }
                case 2: {
                    //Applies to moveAction: MOVE_NODE_ONLY  
                    //check if sourceObj has any RT relations with all targetObj's BTs recursively -- targetObj included                    
                    descriptor = descriptor;
                    newBT = newBT;
                    if(move_To_Hierarchy_Consistency_Test_2(SessionUserInfo.selectedThesaurus, Q,  sis_session, dbGen, errorMsg, pathToErrorsXML, descriptor,  newBT, prefix)==false){
                        return false;
                    }
                    break;  
                }
                case 3: {
                    // set_1 Get Direct BTs of sourceObj --> sourceObj not included 
                    // set_2 Get direct Nts of sourceObj --> sourceObj not included
                    // set_3 Get recursive BTs of set 2  --> sourceObj not included in every round of recursion
                    
                    // set_1 must have nothing in common with set_3
                    descriptor = descriptor;
                    newBT = newBT;
                    if(move_To_Hierarchy_Consistency_Test_3(SessionUserInfo.selectedThesaurus,Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML, descriptor, prefix)==false){
                        return false;
                    }
                    break; 
                }

                case 4: {
                    //Applies to moveActions: MOVE_NODE_AND_SUBTREE
                    //All target BTs recursively target included, and all their RTs nothing in common with all Nts of source and all their BTs recursively excluding E

                    // set_0: includes source
                    // set_1: must include NTs of sourceObj recursively without sourceObj 
                    // set_2: must include direct BTs of set_1 and set_1 itself --> thus it will also include source but not its other BTs as they will be lost
                    // set_3: must include all BTs recursively of targetObj recursively and targetObj itself
                    // set_2 and set_3 nothing in common 
                    // THUS Cycle avoided (nothing in set 3 meets with set_1 and sourceObj both included in set_2) 
                    // and no node under source node has direct BT links to any term above target or target itself

                    // set_4: must include all RTs of set_3
                    // set_4 and set_2 must have nothing in common
                    // Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML, descriptor,  newBT, prefix
                    descriptor = descriptor;
                    newBT = newBT;
                    if (move_To_Hierarchy_Consistency_Test_4(SessionUserInfo.selectedThesaurus, Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML, descriptor,  newBT, prefix) == false) {
                        return false;
                    }
                    break;
                }
                case 5: {
                    //Applies to moveActions: CONNECT_NODE_AND_SUBTREE

                    //set_0 includes targetObj only
                    //set_1 includes sourceObj
                    //set_2 must include all Bts of sourceObj recursively with sourceObj
                    //set_3 must include only Rts of set_2 without set_2

                    //set_2 must have nothing in common with set_0
                    //set_3 must have nothing in common with set_0
                    // 
                    descriptor = descriptor;
                    newBT = newBT;
                    if(move_To_Hierarchy_Consistency_Test_5(SessionUserInfo.selectedThesaurus,Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML, descriptor,  newBT, prefix)==false){
                        return false;
                    }
                    break; 
                }
                case 6: {
                    //Applies to moveAction: CONNECT_NODE_AND_SUBTREE  similar to check 4 

                    // set_0: includes source
                    // set_1: must include NTs of sourceObj recursively with sourceObj 
                    // set_2: must include direct BTs of set_1 and set_1 itself 
                    // set_3: must include all BTs recursively of targetObj recursively and targetObj itself

                    // set_2 and set_3 nothing in common 
                    // THUS Cycle avoided (nothing in set 3 meets with set_1 which is included in set_2) 
                    // and no node under source node has direct BT links to any term above target or target itself

                    // set_4: must include all RTs of set_3
                    // set_4 and set_2 must have nothing in common
                    descriptor = descriptor;
                    newBT = newBT;
                    if(move_To_Hierarchy_Consistency_Test_6(SessionUserInfo.selectedThesaurus, Q,  sis_session,  dbGen, errorMsg, pathToErrorsXML, descriptor,  newBT, prefix)==false){
                        return false;
                    }
                    break; 
                }
                case 7: {
                    
                    ArrayList<String> decodedValues = new ArrayList<String>();
                    decodedValues.add(newBT);
                    if (create_modify_check_27(SessionUserInfo, Q, sis_session,descriptor, decodedValues, errorMsg, pathToErrorsXML,false, null,EDIT_TERM_POLICY) == false) {
                        return false;
                    }
                                            
                    break;
                }
                
                case 8: {
                    
                    StringObject BTClassObj = new StringObject();
                    StringObject BTLinkObj = new StringObject();
                    dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);
                    StringObject descriptorObj = new StringObject(prefix.concat(descriptor));
                    ArrayList<String> decodedValues = new ArrayList<String>();
                    
                    
                    Q.reset_name_scope();
                    Q.set_current_node(descriptorObj);
                    
                    int set_bt_labels = Q.get_link_from_by_category(0, BTClassObj, BTLinkObj);
                    Q.reset_set(set_bt_labels);
                    
                    int set_bts = Q.get_to_value(set_bt_labels);
                    Q.reset_set(set_bts);
                    
                    
                    decodedValues.addAll(dbGen.get_Node_Names_Of_Set(set_bts, true, Q, sis_session));
                    
                    if(decodedValues.contains(newBT)==false){
                        decodedValues.add(newBT);
                    }
                    
                    
                    Q.free_set(set_bt_labels);
                    Q.free_set(set_bts);
                    
                    //must add rest btsof target Term
                    if (create_modify_check_27(SessionUserInfo, Q, sis_session,descriptor, decodedValues, errorMsg, pathToErrorsXML,false, null,EDIT_TERM_POLICY) == false) {
                        return false;
                    }
                                            
                    break;
                }
                case 9: {
                    //Applies to moveActions: MOVE_NODE_ONLY   MOVE_NODE_AND_SUBTREE     CONNECT_NODE_AND_SUBTREE
                    //Check if term is Top Term
                    descriptor = descriptor;
                    newBT = newBT;
                    if(descriptor.compareTo(newBT)==0){
                        ArrayList<String> errorArgs=new ArrayList<String>();
                        errorArgs.add(descriptor);
                        errorMsg.setValue(errorMsg.getValue().concat(translate(9,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));

                        return false;
                    }
                    break;
                }
                
                default: {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"No check code like " + moveHierarchyChecks.get(i) + " was found for move term to Hierarchy constraints check");
                    break;
                }
            }
        }
         
        return true;
    }
  
    
    public boolean create_modify_check_01(StringObject errorMsg,String pathToErrorsXML, String targetDescriptor){
        if(Parameters.TermModificationChecks.contains(1)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_1");
        //Check if name declared is null or empty
        if (targetDescriptor == null || targetDescriptor.trim().length() == 0) {
            errorMsg.setValue(errorMsg.getValue().concat(translate(1,1,Create_Modify_XML_STR,null,pathToErrorsXML)));
            return false;
        }        
       
        return true;
    }
   
    public boolean create_modify_check_03(ArrayList<String> translations_Vector,StringObject errorMsg,String pathToErrorsXML){
        if(Parameters.TermModificationChecks.contains(3)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_3");
        //Check if more than one english names are inserted   

        translations_Vector.trimToSize();
        if (translations_Vector.size() > 1) {
            
            int count = 0;
            for(int i=0; i< translations_Vector.size(); i++){
                if(translations_Vector.get(i)!=null && translations_Vector.get(i).trim().length()>0){
                    count++;
                }
            }
            if(count!=1){
                errorMsg.setValue(errorMsg.getValue().concat(translate(3,1,Create_Modify_XML_STR,null,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Declaration of one and only translation for each term is obligatory.;
                return false;
            }
        }
        return true;
    }
   /*
    public boolean create_modify_check_04(ArrayList<String> tcs_Vector,StringObject errorMsg,String pathToErrorsXML){
        if(Parameters.TermModificationChecks.contains(4)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_4");
        //Check if at least one dn is declared
        if (tcs_Vector.size() == 0) {
            errorMsg.setValue(errorMsg.getValue().concat(translate(4,1,Create_Modify_XML_STR,null,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("No taxonomical code has been declared.";
            return false;
        }
        return true;
    }
    */
    /*
    public boolean create_modify_check_05(StringObject errorMsg,String pathToErrorsXML, int greekSources){
        if(Parameters.TermModificationChecks.contains(5)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_5");
        //Check if at least one greek source is declared
        if (greekSources == 0) {
            errorMsg.setValue(errorMsg.getValue().concat(translate(5,1,Create_Modify_XML_STR,null,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("No term source has been declared.";
            return false;
        }
                    
        return true;
    }
    */
    /*
    public boolean create_modify_check_06(StringObject errorMsg,String pathToErrorsXML,int enSources){
        if(Parameters.TermModificationChecks.contains(6)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_6");
        //Check if at least one english source is declared
        if (enSources == 0) {
            errorMsg.setValue(errorMsg.getValue().concat(translate(6,1,Create_Modify_XML_STR,null,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("No term translation source has been declared.";
            return false;
        }

        return true;
    }
    */
    public boolean create_modify_check_07(ArrayList<String> invalidNames_Vector, StringObject errorMsg,String pathToErrorsXML, String targetDescriptor){
        if(Parameters.TermModificationChecks.contains(7)==false)
            return true;
        
        invalidNames_Vector.trimToSize();


        if (invalidNames_Vector.contains(targetDescriptor)) {

            errorMsg.setValue(errorMsg.getValue().concat(translate(7,1,Create_Modify_XML_STR,null,pathToErrorsXML)));            
            return false;
        }
        
        return true;
    }
    /*
    public boolean create_modify_check_08(QClass Q, IntegerObject sis_session,StringObject errorMsg,String pathToErrorsXML,ArrayList<String> bts_Vector, String prefix){
        if(Parameters.TermModificationChecks.contains(8)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_8");
        int SisSessionId = sis_session.getValue();
        //Check if bts or RTs declared exist
        for(int i =0; i< bts_Vector.size(); i++){

            Q.reset_name_scope();
            StringObject targetBt = new StringObject(prefix.concat(bts_Vector.get(i)));

            if (Q.set_current_node( targetBt) == QClass.APIFail) {

                ArrayList<String> errorArgs= new ArrayList<String>();
                errorArgs.add(bts_Vector.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(8,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("BT: '" + UnclassifiedClass + "' declared for the creation of the new term was not found in the database.";
                Q.reset_name_scope();
                return false;
            }

        }
        Q.reset_name_scope();
           
        return true;
    }
    */
    public boolean create_modify_check_09(String selectedThesaurus,QClass Q, IntegerObject sis_session,DBGeneral dbGen, StringObject errorMsg,String pathToErrorsXML,String targetDescriptor,String prefix, String create_modify){
        if(Parameters.TermModificationChecks.contains(9)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_9");
        int SisSessionId = sis_session.getValue();
        //Check if new term name already exists in db
        if (create_modify.matches("create")) {

            Q.reset_name_scope();
            StringObject targetDescr = new StringObject(prefix.concat(targetDescriptor));

            if (Q.set_current_node( targetDescr) != QClass.APIFail) {

                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(targetDescriptor);
                errorMsg.setValue(errorMsg.getValue().concat(translate(9,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)));//"Term '" + targetDescriptor + "' already exists in the database.";
                
                // append a sort description of the existing term
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.ExistingTermSortDescription(selectedThesaurus,targetDescr,Q,sis_session).getValue()));
                
                Q.reset_name_scope();
                return false;
            }
            Q.reset_name_scope();
        }
        
        return true;
    }
    
    public boolean create_modify_check_11(StringObject errorMsg,String pathToErrorsXML,ArrayList<String> tcs_Vector){
        if(Parameters.TermModificationChecks.contains(11)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_11");
        //Check if tcs declared follow a predifined pattern xxx.y.zz.w etc
        for (int i = 0; i < tcs_Vector.size(); i++) {

            String testTC = tcs_Vector.get(i);
            if(!testTC.matches(Parameters.TaxonomicalCodeFormat)){
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(testTC);
                errorMsg.setValue(errorMsg.getValue().concat(translate(11,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Non approved taxinomical code : '" + testDN + "'.";
                return false;
            }
            /*
            String testTC = tcs_Vector.get(i);
            boolean passedDigit = false;
            boolean digits_and_dots = true;

            for (int j = 0; j < testTC.length(); j++) {

                if (Character.isDigit(testTC.charAt(j))) {
                    passedDigit = true;
                    continue;
                } else if (passedDigit && testTC.charAt(j) == '.') {
                    passedDigit = false;
                    continue;
                }

                digits_and_dots = false;
                break;
            }

            if (digits_and_dots == false || testTC.endsWith(".")) {
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(testTC);
                errorMsg.setValue(errorMsg.getValue().concat(translate(11,1,Create_Modify_XML_STR,errorArgs);
                //errorMsg.setValue(errorMsg.getValue().concat("Non approved taxinomical code : '" + testDN + "'.";
                return false;
            }
            */
        }
        return true;
    }
    
    public boolean create_modify_check_12(String selectedThesaurus,QClass Q, IntegerObject sis_session,  DBGeneral dbGen, StringObject errorMsg, String pathToErrorsXML, ArrayList<String> tcs_Vector, String prefix_TC, String targetDescriptor/*,String create_modify*/) {

        if(Parameters.TermModificationChecks.contains(12)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_12");
        int SisSessionId = sis_session.getValue();
        //Check if declared deweys are already used
        StringObject targetDescriptorObj = new StringObject(targetDescriptor);
        Q.reset_name_scope();
        int set_targetNode = Q.set_get_new();
        Q.reset_set(set_targetNode);
        Q.set_current_node( targetDescriptorObj);
        Q.set_put(set_targetNode);
        Q.reset_name_scope();
        //if (create_modify.matches("create")) {
        int set_of_existing_tcs = Q.set_get_new();
        Q.reset_set(set_of_existing_tcs);

        //check if english words declared are already used  
        //is this an error that should be detected?
        for (int i = 0; i < tcs_Vector.size(); i++) {

            StringObject term_tc = new StringObject(prefix_TC.concat(tcs_Vector.get(i)));
            if (Q.set_current_node( term_tc) != QClass.APIFail) {
                Q.set_put(set_of_existing_tcs);
                Q.reset_set(set_of_existing_tcs);
            

            }
            Q.reset_name_scope();
        }

        if (Q.set_get_card( set_of_existing_tcs) > 0) {

            StringObject fromTCClassObj = new StringObject();
            StringObject LinkTCClassObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus,ConstantParameters.tc_kwd, fromTCClassObj, LinkTCClassObj, Q, sis_session);
            int set_terms_labels = Q.get_link_to_by_category( set_of_existing_tcs, fromTCClassObj, LinkTCClassObj);
            Q.reset_set(set_terms_labels);
            int set_terms = Q.get_from_value( set_terms_labels);
            Q.reset_set(set_terms);

            Q.set_difference(set_terms, set_targetNode);
            Q.reset_set(set_terms);

            if (Q.set_get_card(set_terms) > 0) {

                int set_problematic_labels = Q.get_link_from_by_category(set_terms, fromTCClassObj, LinkTCClassObj);
                Q.reset_set(set_problematic_labels);
                int set_problematic_tcs = Q.get_to_value(set_problematic_labels);
                Q.reset_set(set_problematic_tcs);

                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(getStringList_Of_Set(Q, sis_session, dbGen, set_problematic_tcs, "', '") + "\n");
                errorMsg.setValue(errorMsg.getValue().concat(translate(12, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Taxinomical codes : '" + dns_Vector.get(i) + "' already exist in database and are used for other terms.";


                Q.free_set(set_problematic_tcs);
                Q.free_set(set_problematic_labels);

                Q.free_set(set_terms);
                Q.free_set(set_terms_labels);

                Q.free_set(set_of_existing_tcs);
                Q.free_set(set_targetNode);

                Q.reset_name_scope();
                return false;
            }
            
            Q.free_set(set_terms);
            Q.free_set(set_terms_labels);

            Q.free_set(set_of_existing_tcs);
            Q.free_set(set_targetNode);

        }
        Q.free_set(set_of_existing_tcs);
        Q.free_set(set_targetNode);

        // }
        Q.reset_name_scope();

        return true;
    }
    
    public boolean create_modify_check_13(QClass Q, IntegerObject sis_session,StringObject errorMsg,String pathToErrorsXML, ArrayList<String> sources_Vector,String prefix_Source){
        if(Parameters.TermModificationChecks.contains(13)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_13");
        int SisSessionId = sis_session.getValue();
        //Check if declared sources exist  
        Q.reset_name_scope();

          
        //if (create_modify.matches("create")) {

            //check if english words declared are already used  
            for (int i = 0; i < sources_Vector.size(); i++) {

                StringObject term_source = new StringObject(prefix_Source.concat(sources_Vector.get(i)));
                if (Q.set_current_node( term_source) == QClass.APIFail) {
                    
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(sources_Vector.get(i));
                    errorMsg.setValue(errorMsg.getValue().concat(translate(13, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("Source : '" + sources_Vector.get(i) + "' does not exist in database.";
                    Q.reset_name_scope();
                    return false;
                }
                Q.reset_name_scope();
            }
       // }

        Q.reset_name_scope();
        return true;
    }
    
    /*
    public boolean create_modify_check_14(StringObject errorMsg,String pathToErrorsXML,String targetDescriptor,ArrayList<String> bts_Vector, ArrayList<String> rts_Vector,String create_modify){
        if(Parameters.TermModificationChecks.contains(14)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_14");
        //Check if BTs declared are also declared as new Terms' RTs
        if (create_modify.matches("create")) {

            for(int i=0 ; i < bts_Vector.size(); i++){
                
                if (rts_Vector.size() > 0 && rts_Vector.contains(bts_Vector.get(i))) {
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(targetDescriptor);
                    errorArgs.add(bts_Vector.get(i));
                    errorMsg.setValue(errorMsg.getValue().concat(translate(14, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("Relationship RT between the '" + targetDescriptor + "' and '" + UnclassifiedClass + "' cannot be implemented because they belong to the same hierarchical branch.";
                    return false;
                }
            }
            
        }
        
        return true;
    }
    */
    
    public boolean create_modify_check_15(String selectedThesaurus, QClass Q,IntegerObject sis_session,DBGeneral dbGen, StringObject errorMsg,String pathToErrorsXML,ArrayList<String> bts_or_rts_Vector,String prefix,int errorMsgOffset){
        //if bts check --> errorMsgOffset = 2 else if rts check errorMsgOffset = 0
        if(Parameters.TermModificationChecks.contains(15)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_15");
        int SisSessionId = sis_session.getValue();
        //Check if RTs declared exist in db and if these RTs or BTS are THES1HierarchyTerms 
        if (bts_or_rts_Vector.size() > 0) {
            //Check if RTs Declared already exist in DB

            int set_bts_or_rts = Q.set_get_new();
            Q.reset_set( set_bts_or_rts);
            Q.reset_name_scope();

            for (int i = 0; i < bts_or_rts_Vector.size(); i++) {

                //create a set containg rt names
                StringObject tempObj = new StringObject(prefix.concat(bts_or_rts_Vector.get(i)));
                if (Q.set_current_node( tempObj) != QClass.APIFail) {
                    Q.set_put( set_bts_or_rts);
                    Q.reset_set( set_bts_or_rts);
                } else {
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(bts_or_rts_Vector.get(i));
                    errorMsg.setValue(errorMsg.getValue().concat(translate(15, (errorMsgOffset+1), Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("RT '" + rts_Vector.get(i) + "' does not exist in database.";
                    Q.free_set( set_bts_or_rts);
                    Q.reset_name_scope();
                    return false;
                }
                Q.reset_name_scope();

            }

            //create a set containing valid BT or RT Names which translates to XXXHierarchyTerm class instances
            Q.reset_name_scope();
            

            Q.set_current_node( new StringObject(selectedThesaurus + ConstantParameters.validBTSandRTsClass));
            int valid_bts_or_rts = Q.get_all_instances( 0);//This might include unclassified nodes to HierarchyTerm ISA classes

            Q.reset_set( set_bts_or_rts);
            Q.reset_set( valid_bts_or_rts);
            Q.set_difference( set_bts_or_rts, valid_bts_or_rts);
            Q.reset_set( set_bts_or_rts);

            Q.free_set( valid_bts_or_rts);
            if (Q.set_get_card( set_bts_or_rts) != 0) {

                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_bts_or_rts, "', '")+"\n");
                errorMsg.setValue(errorMsg.getValue().concat(translate(15, (errorMsgOffset+2), Create_Modify_XML_STR, errorArgs, pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Terms: '" + getStringList_Of_Set(set_rts, "', '") + "' do not belong in related terms in condition to be used in the field of RT.";
                Q.free_set( set_bts_or_rts);
                Q.reset_name_scope();
                return false;
            }
            Q.free_set( set_bts_or_rts);


            Q.reset_name_scope();

        }
        
        return true;
    }
    
    public boolean create_modify_check_16(String selectedThesaurus,QClass Q, IntegerObject sis_session,DBGeneral dbGen, StringObject errorMsg,String pathToErrorsXML,ArrayList<String> ufs_Vector,String prefix,String targetTerm, boolean resolveError, OutputStreamWriter logFileWriter){
        if(Parameters.TermModificationChecks.contains(16)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_16");
        int SisSessionId = sis_session.getValue();
        //Check if UFs declared already exist in db but not as uf links
        
        if (ufs_Vector.size() > 0) {

            Q.reset_name_scope();
            int set_check_ufs = Q.set_get_new();

            //create a set with existing ufs
            for (int i = 0; i < ufs_Vector.size(); i++) {

                StringObject term_uf = new StringObject(prefix.concat(ufs_Vector.get(i)));
                if (Q.set_current_node( term_uf) != QClass.APIFail) {
                    Q.set_put( set_check_ufs);
                    Q.reset_set( set_check_ufs);
                }
                Q.reset_name_scope();
            }

            if (Q.set_get_card( set_check_ufs) != 0) {

                ArrayList<String> removeUfs = new ArrayList<String>();
                
                DBThesaurusReferences dbtr = new DBThesaurusReferences();
                StringObject usedForClassObj = new StringObject();
                dbtr.getThesaurusClass_UsedForTerm(selectedThesaurus,Q,sis_session.getValue(),usedForClassObj);
                Q.reset_name_scope();
                
                Q.set_current_node( usedForClassObj);
                int set_existing_ufs = Q.get_instances( 0);
                Q.reset_set( set_existing_ufs);

                Q.set_difference( set_check_ufs, set_existing_ufs);
                Q.reset_set( set_check_ufs);

                int invalid_card = Q.set_get_card( set_check_ufs);
                //check if other names that are not UsedForTerms exist in DB. if yes then error
                String non_valid_ufs_str = getStringList_Of_Set(Q,sis_session,dbGen,set_check_ufs, "', '");
                removeUfs.addAll(dbGen.get_Node_Names_Of_Set(set_check_ufs, true, Q, sis_session));

                Q.free_set( set_existing_ufs);
                Q.free_set( set_check_ufs);
                Q.reset_name_scope();

                if (invalid_card > 0) {

                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_ufs_str);
                    try {
                        if (resolveError) {
                            //logFileWriter.append("\r\n" + translate(20, 3, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "\r\n");
                            
                            
                            for (int k = 0; k < removeUfs.size(); k++) {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.uf_kwd + "</errorType>");
                                ufs_Vector.remove(removeUfs.get(k));
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(removeUfs.get(k)) + "</errorValue>");
                                logFileWriter.append("<reason>" + translate(16, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            }
                            


                        } else {
                            errorMsg.setValue(errorMsg.getValue().concat(translate(16, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                            return false;
                        }
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(16, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "\n" + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }
                 
                    
                }

            } else {
                Q.free_set( set_check_ufs);
                Q.reset_name_scope();
            }
        }

        return true;
    }
    /*
    public boolean create_modify_check_17(String selectedThesaurus,QClass Q, IntegerObject sis_session,DBGeneral dbGen, StringObject errorMsg,String pathToErrorsXML,ArrayList<String> alts_Vector,String prefix){
        if(Parameters.TermModificationChecks.contains(17)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_17");
        int SisSessionId = sis_session.getValue();
        //Check if ALTs declared already exist in db but not as alt links
        if (alts_Vector.size() > 0) {

            Q.reset_name_scope();
            int set_check_alts = Q.set_get_new();

            //create a set with existing alts
            for (int i = 0; i < alts_Vector.size(); i++) {

                StringObject term_alt = new StringObject(prefix.concat(alts_Vector.get(i)));
                if (Q.set_current_node( term_alt) != QClass.APIFail) {
                    Q.set_put( set_check_alts);
                    Q.reset_set( set_check_alts);
                }
                Q.reset_name_scope();
            }

            if (Q.set_get_card( set_check_alts) != 0) {

                DBThesaurusReferences dbtr = new DBThesaurusReferences();
                StringObject alternativeTermClassObj = new StringObject();
                dbtr.getThesaurusClass_AlternativeTerm(sessionInstance,Q,sis_session.getValue(),alternativeTermClassObj);
                Q.reset_name_scope();
                
                Q.set_current_node( alternativeTermClassObj);
                int set_existing_alts = Q.get_instances( 0);
                Q.reset_set( set_existing_alts);

                Q.set_difference( set_check_alts, set_existing_alts);
                Q.reset_set( set_check_alts);
                int invalid_card = Q.set_get_card( set_check_alts);

                //check if other names that are not AlternativeTerms exist in DB. if yes then error
                String non_valid_uk_alts = getStringList_Of_Set(Q,sis_session,dbGen,set_check_alts, "', '");

                Q.free_set( set_existing_alts);
                Q.free_set( set_check_alts);
                Q.reset_name_scope();

                if (invalid_card > 0) {
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_uk_alts);
                    errorMsg.setValue(errorMsg.getValue().concat(translate(17, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("The Alternative terms : '" + non_valid_uk_alts + "', are used in database beyond of the set of the alternative terms.";
                    return false;
                }

            } else {
                Q.free_set( set_check_alts);
                Q.reset_name_scope();
            }
        }

        return true;
    }
    */
    public boolean create_modify_check_18(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,ArrayList<String> uk_ufs_Vector,String prefixEN,String targetTerm, boolean resolveError, OutputStreamWriter logFileWriter){
        if(Parameters.TermModificationChecks.contains(18)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_18");
        StringObject uk_uf_fromClass = new StringObject();
        StringObject uk_uf_link = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.uf_translations_kwd, uk_uf_fromClass, uk_uf_link,Q,sis_session);
        int SisSessionId = sis_session.getValue();
        //Check if uk_ufs declared already exist in db but not used (or only used) as uk_ufs links
        if (uk_ufs_Vector.size() > 0) {

            //Collect names that exist in db
            Q.reset_name_scope();
            int set_check_uk_ufs = Q.set_get_new();

            //create a set with existing en`words
            for (int i = 0; i < uk_ufs_Vector.size(); i++) {

                StringObject term_uk_uf = new StringObject(prefixEN.concat(uk_ufs_Vector.get(i)));
                if (Q.set_current_node( term_uk_uf) != QClass.APIFail) {
                    Q.set_put( set_check_uk_ufs);
                    Q.reset_set( set_check_uk_ufs);
                }
                Q.reset_name_scope();
            }

            //if none exist then all are new and nothing needs to be checked
            if (Q.set_get_card( set_check_uk_ufs) != 0) {
                
                StringObject enFromClass = new StringObject();
                StringObject enLinkObj   = new StringObject();
                /*StringObject uk_alt_fromClass = new StringObject();
                StringObject uk_alt_link = new StringObject();*/
                dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translation_kwd, enFromClass, enLinkObj, Q, sis_session);
                //dbGen.getKeywordPair(sessionInstance,  dbGen.uk_alt_kwd, uk_alt_fromClass, uk_alt_link,Q,sis_session);
      
                Q.reset_name_scope();
                               
                int set_ens = Q.get_link_to_by_category(set_check_uk_ufs, enFromClass,enLinkObj);
                Q.reset_set(set_ens);

                if (Q.set_get_card(set_ens) > 0) {
                    int wrong_ens = Q.get_to_value(set_ens);
                    String non_valid_ens = getStringList_Of_Set(Q,sis_session,dbGen,wrong_ens, "', '");
                    ArrayList<String> errorUFENValues = new ArrayList<String>();
                    errorUFENValues.addAll(dbGen.get_Node_Names_Of_Set(wrong_ens, true, Q, sis_session));
                    
                    Q.free_set(wrong_ens);
                    Q.free_set(set_ens);
                    Q.free_set( set_check_uk_ufs);
                    
                    Q.reset_name_scope();
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_ens);
                    
                    try {
                        if (resolveError) {
                            
                            for (int k = 0; k < errorUFENValues.size(); k++) {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.uf_translations_kwd + "</errorType>");
                                uk_ufs_Vector.remove(errorUFENValues.get(k));
                                logFileWriter.append("<errorValue>" + Utilities.escapeXML(errorUFENValues.get(k)) + "</errorValue>");
                                logFileWriter.append("<reason>" + translate(18, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            }
                            
                        } else {
                            errorMsg.setValue(errorMsg.getValue().concat(translate(18, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                            return false;
                        }
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(18, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "\n" + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }
                    
                }
                
                
                Q.reset_name_scope();
                               
                /*int set_uk_alts = Q.get_link_to_by_category(set_check_uk_ufs, uk_alt_fromClass,uk_alt_link);
                Q.reset_set(set_uk_alts);*/
                /*NO ALTS USED
                if (Q.set_get_card(set_uk_alts) > 0) {
                    int wrong_ens = Q.get_to_value(set_uk_alts);
                    String non_valid_ens = getStringList_Of_Set(Q,sis_session,dbGen,wrong_ens, "', '");
                    
                    Q.free_set(wrong_ens);
                    Q.free_set(set_uk_alts);
                    Q.free_set( set_check_uk_ufs);
                    
                    Q.reset_name_scope();
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_ens);
                    errorMsg.setValue(errorMsg.getValue().concat(translate(18, 2, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    return false;
                    
                }
                */
                
               // Q.free_set(set_uk_alts);
                Q.free_set(set_ens);
                Q.free_set( set_check_uk_ufs);



            } else {

                Q.free_set( set_check_uk_ufs);
                Q.reset_name_scope();
            }
        }

        return true;
    }
    /*
    public boolean create_modify_check_19(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen, StringObject errorMsg,String pathToErrorsXML,ArrayList<String> uk_alts_Vector, String prefixEN){
        if(Parameters.TermModificationChecks.contains(19)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_19");
        StringObject fromClass = new StringObject();
        StringObject link = new StringObject();
        dbGen.getKeywordPair(sessionInstance, dbGen.uk_alt_kwd, fromClass, link,Q,sis_session);
        int SisSessionId = sis_session.getValue();
        //Check if uk_alts declared already exist in db but not used (or only used) as uk_alts links
        if (uk_alts_Vector.size() > 0) {

            //Collect names that exist in db
            Q.reset_name_scope();
            int set_check_uk_alts = Q.set_get_new();

            //create a set with existing en`words
            for (int i = 0; i < uk_alts_Vector.size(); i++) {

                StringObject term_uk_alt = new StringObject(prefixEN.concat(uk_alts_Vector.get(i)));
                if (Q.set_current_node( term_uk_alt) != QClass.APIFail) {
                    Q.set_put( set_check_uk_alts);
                    Q.reset_set( set_check_uk_alts);
                }
                Q.reset_name_scope();
            }

            //if none exist then all are new and nothing needs to be checked
            if (Q.set_get_card( set_check_uk_alts) != 0) {
                int set_all_links_to = Q.get_link_to(set_check_uk_alts);
                Q.reset_set(set_all_links_to);
                
                int set_uk_alt_links_to = Q.get_link_to_by_category(set_check_uk_alts,fromClass,link);
                Q.reset_set(set_uk_alt_links_to);
                
                Q.set_difference(set_all_links_to,set_uk_alt_links_to);
                Q.reset_set(set_all_links_to);
                
                int invalid_card = Q.set_get_card( set_all_links_to);
           
                if (invalid_card > 0) {
                    int set_wrong_uk_alts  = Q.get_to_value(set_all_links_to);
                    Q.reset_set( set_wrong_uk_alts);
                    String non_valid_uk_alts = getStringList_Of_Set(Q,sis_session,dbGen,set_wrong_uk_alts, "', '");
                    
                    Q.free_set(set_uk_alt_links_to);
                    Q.free_set( set_all_links_to);
                    Q.free_set( set_uk_alt_links_to);
                    Q.free_set( set_check_uk_alts);
                    Q.reset_name_scope();
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_uk_alts);
                    errorMsg.setValue(errorMsg.getValue().concat(translate(18, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("UF terms:  '" + non_valid_uk_ufs + "' declared are already used in the database beyond the set of Used For Terms.";
                    return false;
                }


                

                Q.free_set( set_all_links_to);
                Q.free_set( set_uk_alt_links_to);
                Q.free_set( set_check_uk_alts);
                Q.reset_name_scope();



            } else {

                Q.free_set( set_check_uk_alts);
                Q.reset_name_scope();
            }
        }

        return true;
    }
    */
    //Function "check_Rts_Modification_Consistency" 
    //checks if "targetDescriptor" has BTs or NTs that are also declared as "targetDescriptor"'s RTs
    public boolean create_modify_check_20(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral  dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, ArrayList<String> rts_Vector, String prefix, String create_modify, boolean resolveError, OutputStreamWriter logFileWriter) {
        
        //Check if RTs delared exist in set that includes all BTs and all NTs recursively of target Node
        //DEBUG NOTE: propably some set is lost in this code
        if(Parameters.TermModificationChecks.contains(20)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_20");
        if(create_modify.matches("create")){ //rtS ARE NOT DECLARED SIMULTANEOUSLY WITH BTS THUS NO NEED TO CHECK RTS IF TERM IS NOW CREATED
            return true;
        }
        
        int SisSessionId = sis_session.getValue();
        Q.reset_name_scope();

        StringObject descriptorObj = new StringObject(prefix.concat(descriptor));

        if (Q.set_current_node(descriptorObj) == QClass.APIFail) {
            ArrayList<String> errorArgs = new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(20, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + "  not found in database.";
            return false;
        }

        //Create set_1
        int set_1 = Q.set_get_new();
        Q.set_put( set_1);
        Q.reset_set( set_1);


        //Collect declared Rts from rt parameter --> comma seperated list
        if (rts_Vector.size()==0) {

            Q.free_set( set_1);
            return true;
        }

        int set_rts = Q.set_get_new();

        for (int i = 0; i < rts_Vector.size(); i++) {

            Q.reset_name_scope();
            if (Q.set_current_node(new StringObject(prefix.concat(rts_Vector.get(i)))) == QClass.APIFail) {
                
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(rts_Vector.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(20, 2, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Term '" + rts_Vector.get(i) + "'  was not found in database.";
                
                Q.free_set( set_1);
                Q.free_set( set_rts);
                return false;

            } else {
                Q.set_put( set_rts);
                Q.reset_set( set_rts);
            }

        }
        
        //set_1 will include only descriptorObj
        //set_2 must include all BTs and all NTs of descriptorObj recursively 
        //set_3 must include set_1's RTs Both directions

        //set_1 and set_2 must not have any node in common
        Q.reset_name_scope();
        //Check if target node exists


        int set_2 = Q.set_get_new();
        dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_1, set_2, true,Q,sis_session);
        Q.reset_set( set_2);

        dbGen.collect_Recurcively_ALL_NTs_Of_Set(selectedThesaurus,set_1, set_2, false,Q,sis_session);
        Q.reset_set( set_2);


        //Check Condition
        Q.reset_set( set_rts);
        Q.reset_set( set_2);
        Q.set_intersect( set_2, set_rts);
        Q.reset_set( set_2);


        Q.free_set( set_rts);
        Q.free_set( set_1);
        
        boolean rtsRemoved = false;
        ArrayList<String> removeRts = new ArrayList<String>();
        
        if (Q.set_get_card( set_2) != 0) {

            ArrayList<String> errorArgs = new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_2, "', '"));
            
            try {
                if (resolveError) {
                    //logFileWriter.append("\r\n" + translate(20, 3, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "\r\n");
                    
                    rtsRemoved = true;
                    removeRts.addAll(dbGen.get_Node_Names_Of_Set(set_2, true, Q, sis_session));
                    
                    
                    for(int k =0 ; k< removeRts.size();k++){
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>"+Utilities.escapeXML(errorArgs.get(0))+"</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.rt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>"+Utilities.escapeXML(removeRts.get(k))+"</errorValue>");
                        logFileWriter.append("<reason>"+translate(20, 3, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)+"</reason>");
                        logFileWriter.append("</targetTerm>\r\n");
                    }
                    
                    
                    Q.free_set( set_2);
                   
                } else {
                    errorMsg.setValue(errorMsg.getValue().concat(translate(20, 3, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    //errorMsg.setValue(errorMsg.getValue().concat("Term '" + descriptor + "' cannot has as RT the '" + getStringList_Of_Set(set_2, "'\n'") + "' due to the existance of recursive relationships with BT and NT.'";
                    Q.free_set( set_2);
                    return false;
                }
            } catch (IOException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " +  translate(20, 3, Create_Modify_XML_STR, errorArgs,pathToErrorsXML) + "\n" + ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
            
            
        }
        
        if (resolveError && rtsRemoved) {
            for (int i = 0; i < removeRts.size(); i++) {
                if (rts_Vector.contains(removeRts.get(i))) {
                    rts_Vector.remove(removeRts.get(i));

                }
            }
        }

        return true;
    }
   
    public boolean create_modify_check_24(StringObject errorMsg,String pathToErrorsXML,ArrayList<String> bts_Vector,String UnclassifiedClass){
        if(Parameters.TermModificationChecks.contains(24)==false)
            return true;
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_24");  
        //in case of 2+ bts declared check if unclassifed hierarchy's top term is included --> it should not

        if(bts_Vector.size() > 1){
            if(bts_Vector.contains(UnclassifiedClass)){
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(UnclassifiedClass);
                errorMsg.setValue(errorMsg.getValue().concat(translate(24,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)));
                return false;
            }
        }
                    
        return true;
    }
    
    public boolean create_modify_check_25(String selectedThesaurus,QClass Q,IntegerObject sis_session,  DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String targetTerm, ArrayList<String> bts_Vector, String create_modify,String prefix, boolean resolveError, OutputStreamWriter logFileWriter){
        if (Parameters.TermModificationChecks.contains(25) == false) {
            return true;
        }
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_25");
        int SisSessionId = sis_session.getValue();
        //in case of 2+ bts declared check if hierarchical dependencies exist among them.

        //if bts num >1 
        //for each bt 
        //collect its bts recursively in tempVec
        //check if tempVec and bts_Vector have anything if common. they should not
        boolean btsRemoved = false;
        ArrayList<String> removeBts = new ArrayList<String>();

        if (bts_Vector.size() == 1) {
            return true;
        }
        StringObject tmpMessage = new StringObject();
        for (int i = 0; i < bts_Vector.size(); i++) {

            Q.reset_name_scope();
            StringObject targetDescriptorObj = new StringObject(prefix.concat(bts_Vector.get(i)));
            int set_all_bts = Q.set_get_new();
            Q.reset_set(set_all_bts);

            if (dbGen.collect_Recurcively_ALL_BTs(selectedThesaurus, targetDescriptorObj, set_all_bts, tmpMessage, false, Q, sis_session) == false) {
                errorMsg.setValue(errorMsg.getValue().concat(tmpMessage.getValue()));
                return false;
            }
            Q.reset_set(set_all_bts);

            ArrayList<String> target_Rec_BTs_Names_Vec = dbGen.get_Node_Names_Of_Set(set_all_bts, true, Q, sis_session);
            Q.free_set(set_all_bts);

            for (int k = 0; k < target_Rec_BTs_Names_Vec.size(); k++) {

                if (bts_Vector.contains(target_Rec_BTs_Names_Vec.get(k))) {

                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(bts_Vector.get(i));
                    errorArgs.add(target_Rec_BTs_Names_Vec.get(k));
                    errorArgs.add(targetTerm);
                    //errorMsg.setValue(errorMsg.getValue().concat(translate(25,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)));
                    //return false;

                    try {
                        if (resolveError) {

                            //logFileWriter.append("\r\n" + translate(25,1,Create_Modify_XML_STR,errorArgs,pathToErrorsXML)+ "\r\n");
                            btsRemoved = true;
                            removeBts.add(bts_Vector.get(i));

                            logFileWriter.append("\r\n<targetTerm>");
                            logFileWriter.append("<name>" + Utilities.escapeXML(targetTerm) + "</name>");
                            logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(target_Rec_BTs_Names_Vec.get(k)) + "</errorValue>");

                            logFileWriter.append("<reason>" + translate(25, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                            logFileWriter.append("</targetTerm>\r\n");

                            continue;
                        } else {
                            errorMsg.setValue(errorMsg.getValue().concat(translate(25, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML)));
                            return false;
                        }
                    } catch (IOException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed in writing: " + translate(25, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) + "\n" + ex.getMessage());
                        Utils.StaticClass.handleException(ex);
                    }

                }
            }
        }
        Q.reset_name_scope();

        if (resolveError && btsRemoved) {
            for (int i = 0; i < removeBts.size(); i++) {
                if (bts_Vector.contains(removeBts.get(i))) {
                    bts_Vector.remove(removeBts.get(i));

                }
            }
        }

        if (bts_Vector.size() <= 0) {
            return false;
        } else {
            return true;
        }

    }
    
    public boolean create_modify_check_26(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,ArrayList<String> translations_Vector,String prefixEN){
        if(Parameters.TermModificationChecks.contains(26)==false)
            return true;
        //abandoned due to multiplicity of translations 
        // no motivation to update this consistency check in order to support multiple translations

        /*
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"create_modify_check_26");
        StringObject enfromClass = new StringObject();
        StringObject enlink = new StringObject();
        dbGen.getKeywordPair(sessionInstance, ConstantParameters.translation_kwd, enfromClass, enlink,Q,sis_session);

        int SisSessionId = sis_session.getValue();
        //Check if ens declared already exist in db but not used (or only used) as en links
        if (translations_Vector.size() > 0) {

            //Collect names that exist in db
            Q.reset_name_scope();
            int set_check_ens = Q.set_get_new();

            //create a set with existing en`words
            for (int i = 0; i < translations_Vector.size(); i++) {

                StringObject term_en = new StringObject(prefixEN.concat(translations_Vector.get(i)));
                if (Q.set_current_node( term_en) != QClass.APIFail) {
                    Q.set_put( set_check_ens);
                    Q.reset_set( set_check_ens);
                }
                Q.reset_name_scope();
            }

            //if none exist then all are new and nothing needs to be checked
            if (Q.set_get_card( set_check_ens) > 0) {
                

                StringObject uk_uf_fromClass = new StringObject();
                StringObject uk_uf_link = new StringObject();
                StringObject uk_alt_fromClass = new StringObject();
                StringObject uk_alt_link = new StringObject();
                dbGen.getKeywordPair(sessionInstance,  ConstantParameters.uf_translations_kwd, uk_uf_fromClass, uk_uf_link,Q,sis_session);
                dbGen.getKeywordPair(sessionInstance,  dbGen.uk_alt_kwd, uk_alt_fromClass, uk_alt_link,Q,sis_session);
      
                Q.reset_name_scope();
                               
                int set_uk_ufs = Q.get_link_to_by_category(set_check_ens, uk_uf_fromClass,uk_uf_link);
                Q.reset_set(set_uk_ufs);

                if (Q.set_get_card(set_uk_ufs) > 0) {
                    int wrong_ens = Q.get_to_value(set_uk_ufs);
                    String non_valid_ens = getStringList_Of_Set(Q,sis_session,dbGen,wrong_ens, "', '");
                    Q.free_set(wrong_ens);
                    Q.free_set(set_uk_ufs);
                    Q.free_set( set_check_ens);
                    
                    Q.reset_name_scope();
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_ens);
                    errorMsg.setValue(errorMsg.getValue().concat(translate(26, 1, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    return false;
                    
                }
                
                
                Q.reset_name_scope();
                               
                int set_uk_alts = Q.get_link_to_by_category(set_check_ens, uk_alt_fromClass,uk_alt_link);
                Q.reset_set(set_uk_alts);

                if (Q.set_get_card(set_uk_alts) > 0) {
                    int wrong_ens = Q.get_to_value(set_uk_alts);
                    String non_valid_ens = getStringList_Of_Set(Q,sis_session,dbGen,wrong_ens, "', '");
                    
                    Q.free_set(wrong_ens);
                    Q.free_set(set_uk_alts);
                    Q.free_set( set_check_ens);
                    
                    Q.reset_name_scope();
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(non_valid_ens);
                    errorMsg.setValue(errorMsg.getValue().concat(translate(26, 2, Create_Modify_XML_STR, errorArgs,pathToErrorsXML)));
                    return false;
                    
                }
                
                
                Q.free_set(set_uk_alts);
                Q.free_set(set_uk_ufs);
                Q.free_set( set_check_ens);
                

            } else {

                Q.free_set( set_check_ens);
                Q.reset_name_scope();
            }
        }

         *
         */
        return true;
    }
    
    public boolean create_modify_check_27(UserInfoClass SessionUserInfo,QClass Q, IntegerObject sis_session,String targetTerm, ArrayList<String> bts_Vector,StringObject errorMsg,String pathToErrorsXML, boolean resolveError, OutputStreamWriter logFileWriter, int policy){
        //test if new BT values of target term will break the rule that a term may not
        //participate both in orphans hierarchy and another one
        //this test must be performed to targetTerm and all its subtree regarding newBts values
        if(Parameters.TermModificationChecks.contains(27)==false)
            return true;

        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        
        
        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");        
        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);
        int set_all_hierarchies = dbGen.get_Instances_Set(HierarchyClasses,Q,sis_session);
        Q.reset_set(set_all_hierarchies);
        
        String prefixClass = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        String prefixTerm  = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject orphanClassObj = new StringObject(prefixClass + Parameters.UnclassifiedTermsLogicalname);
        ArrayList<StringObject> DB_bts_Vector = new ArrayList<StringObject>();
        for(int i=0; i< bts_Vector.size();i++){
            StringObject newStrObj = new StringObject(prefixTerm + bts_Vector.get(i));
            DB_bts_Vector.add(newStrObj);
        }
        
        Q.reset_name_scope();
        Q.set_current_node(orphanClassObj);
        int set_orphans = Q.get_all_instances(0);
        Q.reset_set(set_orphans);
        
        
        int set_candidate_Bts = Q.set_get_new();
        Q.reset_set(set_candidate_Bts);
        for(int i=0; i< DB_bts_Vector.size();i++){
            Q.reset_name_scope();
            Q.set_current_node(DB_bts_Vector.get(i));
            Q.set_put(set_candidate_Bts);
            Q.reset_set(set_candidate_Bts);
        }
                
        int set_non_orphans= Q.set_get_new();
        Q.reset_set(set_non_orphans);
        
        Q.set_copy(set_non_orphans, set_candidate_Bts);
        Q.reset_set(set_non_orphans);
        
        //which candidate bts do not belong in orphan class
        Q.set_difference(set_non_orphans, set_orphans);
        Q.reset_set(set_non_orphans);
        
        //which candidate bts belong in orphan class
        Q.set_intersect(set_orphans,set_candidate_Bts);
        Q.reset_set(set_orphans);
        
        int howmanyOrphans = Q.set_get_card(set_orphans);
        int howmanyNonOrphans = Q.set_get_card(set_non_orphans);
        String OrphannsStringList = dbGen.getStringList_Of_Set(set_orphans, ", ", Q, sis_session);
        String NonOrphannsStringList = dbGen.getStringList_Of_Set(set_non_orphans, ", ", Q, sis_session);
        /*
        ArrayList<String> orphansVec = new ArrayList<String>();
        ArrayList<String> nonOrphansVec = new ArrayList<String>();
        orphansVec.addAll(dbGen.get_Node_Names_Of_Set(set_orphans, true, Q, sis_session));
        nonOrphansVec.addAll(dbGen.get_Node_Names_Of_Set(set_non_orhans, true, Q, sis_session));
        */
        if(howmanyOrphans>0 && howmanyNonOrphans>0){
    
            ArrayList<String> errorArgs = new ArrayList<String>();
                    
            switch(policy){
                
                case IMPORT_COPY_MERGE_THESAURUS_POLICY:{
                    
                    errorArgs.add(targetTerm);
                    errorArgs.add(OrphannsStringList);
                    errorArgs.add(targetTerm);
                    errorArgs.add(NonOrphannsStringList);
                    
                    ArrayList<String> removeBts = new ArrayList<String>();
                    removeBts.addAll(dbGen.get_Node_Names_Of_Set(set_orphans, true, Q, sis_session)); 
                    
                    if(resolveError){
                        //code for copy/merge thesaurus and fix data
                        for (int i = 0; i < removeBts.size(); i++) {
                            if (bts_Vector.size() > 1) {
                                bts_Vector.remove(removeBts.get(i));
                            }
                        }
                        
                        try {
                            logFileWriter.append("\r\n<targetTerm>");
                            logFileWriter.append("<name>" + Utilities.escapeXML(errorArgs.get(0)) + "</name>");
                            logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                            logFileWriter.append("<errorValue>" + Utilities.escapeXML(errorArgs.get(3)) + "</errorValue>");
                            logFileWriter.append("<reason>" + translate(27, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                            logFileWriter.append("</targetTerm>\r\n");
                        } catch (IOException ex) {
                            Logger.getLogger(ConsistensyCheck.class.getName()).log(Level.SEVERE, null, ex);
                            Utils.StaticClass.handleException(ex);
                        }
                        
                        Q.free_set(set_orphans);
                        Q.free_set(set_non_orphans);
                        Q.free_set(set_candidate_Bts);
                        Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS
                        return true; 
                    }
                    
                }
                case EDIT_TERM_POLICY:{
                    errorArgs.add(targetTerm);
                    errorArgs.add(NonOrphannsStringList);
                    errorArgs.add(OrphannsStringList);
                    
                    errorMsg.setValue(translate(27, 2, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) );
                    Q.free_set(set_orphans);
                    Q.free_set(set_non_orphans);
                    Q.free_set(set_candidate_Bts);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS
                    return false; 
                    
                }
                default:
                    Q.free_set(set_orphans);
                    Q.free_set(set_non_orphans);
                    Q.free_set(set_candidate_Bts);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS
                    return false;
            }
            
        }
        
        Q.free_set(set_orphans);
        Q.free_set(set_non_orphans);
        Q.free_set(set_candidate_Bts);

        if(policy==EDIT_TERM_POLICY){//in this case subtree should also be checked considering new targetTerms classes
            
            //scenario:
            //targetTerm: prior not in orphans --> after only in orphans
            //one of its recursive nts --> participates in another hierarchy apart from those inherited by prior targetTerm's classes
            StringObject BTClassObj = new StringObject();
            StringObject BTLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);
            StringObject targetTermObj = new StringObject(prefixTerm.concat(targetTerm));
        
            //create a set With target Term only
            Q.reset_name_scope();
            int set_exclude_target =  Q.set_get_new();
            Q.reset_set(set_exclude_target);
            Q.reset_name_scope();
            long retL = Q.set_current_node(targetTermObj);
            if(retL==QClass.APIFail){
                Q.free_set(set_exclude_target);
                if(errorMsg.getValue().length()>0){
                    return false;
                }
                return true; // no further check
            }
            Q.set_put(set_exclude_target);
            Q.reset_set(set_exclude_target);

            //FIND out all leaf nodes of targetTerm classes
            int set_targetTermClasses = Q.get_classes(0);
            Q.reset_set(set_targetTermClasses);
            Q.set_intersect(set_targetTermClasses, set_all_hierarchies);
            Q.reset_set(set_targetTermClasses);
            
            
            Q.reset_name_scope();
            int set_leafTerms = Q.set_get_new();
            Q.reset_set(set_leafTerms);
            dbGen.collect_Recurcively_ALL_NTs_Of_Set(SessionUserInfo.selectedThesaurus, set_exclude_target, set_leafTerms, false, Q, sis_session);
            Q.reset_set(set_leafTerms);
            
            //if no nts then no further check is needed
            if(Q.set_get_card(set_leafTerms)==0){
                Q.free_set(set_leafTerms);
                Q.free_set(set_targetTermClasses);
                Q.free_set(set_exclude_target);
                Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS
                return true;  
            }
            
            int set_all_targetTerm_classes_instances = Q.get_all_instances(set_targetTermClasses);
            Q.reset_set(set_all_targetTerm_classes_instances);
            //get all Bt links and manipulate them in order to find out leaf nodes of candidate Bts' classes
            Q.reset_name_scope();
            Q.set_current_node(BTClassObj);
            Q.set_current_node(BTLinkObj);            
            int set_all_bt_links = Q.get_all_instances(0);
            Q.reset_set(set_all_bt_links);
            int set_terms_having_bt = Q.get_from_value(set_all_bt_links);
            Q.reset_set(set_terms_having_bt);
            //filter them in order to contain olny terms of candidate hierarchies
            Q.set_intersect(set_terms_having_bt, set_all_targetTerm_classes_instances);
            Q.reset_set(set_terms_having_bt);
            
            int set_terms_having_nt = Q.get_to_value(set_all_bt_links);
            Q.reset_set(set_terms_having_nt);
            //filter them in order to contain olny terms of candidate hierarchies
            Q.set_intersect(set_terms_having_nt, set_all_targetTerm_classes_instances);
            Q.reset_set(set_terms_having_nt);
            
            //find out leafs in set_terms_having_bt
            Q.set_difference(set_terms_having_bt,set_terms_having_nt);
            Q.reset_set(set_terms_having_bt);
            
            
            //NOW SET set_terms_having_bt CONTAINS ONLY LEAFS OF targetTerm's Candidate hierarchies.
            
            Q.reset_name_scope();
            Q.set_intersect(set_leafTerms, set_terms_having_bt);
            Q.reset_set(set_leafTerms);
            
            //Starting from this set of nts we must get all their bts recursively exluding target term in each round
            //and then intersect with top terms in order to find out classes
            
            
            
            
            
            //collect recursively all bts of leaf nodes defined above, excluding in each round target term
            //this is done in order to find out the hierarchies that targetTerm's subtree participates
            int set_partial = Q.set_get_new();
            Q.reset_set(set_partial);
            Q.set_copy(set_partial,set_leafTerms);
            Q.reset_set(set_partial);
            Q.set_difference(set_partial,set_exclude_target);
            Q.reset_set(set_partial);
            
            int set_leaf_topterms = Q.set_get_new();
            Q.reset_set(set_leaf_topterms);
            /*
            ArrayList<String> test_vec1 = new ArrayList<String>();
            ArrayList<String> test_vec2 = new ArrayList<String>();
            test_vec1.addAll(dbGen.get_Node_Names_Of_Set(set_leaf_topterms, true, Q, sis_session));
            test_vec2.addAll(dbGen.get_Node_Names_Of_Set(set_partial, true, Q, sis_session));
            */
            
            
            //free some sets not needed
            Q.free_set(set_terms_having_nt);
            Q.free_set(set_terms_having_bt);
            Q.free_set(set_all_bt_links);
            Q.free_set(set_all_targetTerm_classes_instances);
            Q.free_set(set_leafTerms);
            Q.free_set(set_targetTermClasses);            
            
            
            while (Q.set_get_card(set_partial) > 0) {
                /*test_vec2.clear();
                test_vec2.addAll(dbGen.get_Node_Names_Of_Set(set_partial, true, Q, sis_session));
                */
                int partial_labels = Q.get_link_from_by_category(set_partial, BTClassObj, BTLinkObj);
                Q.reset_set(partial_labels);

                if (Q.set_get_card(partial_labels) <= 0) {
                    Q.free_set(partial_labels);
                    break;

                }

                Q.free_set(set_partial);
                set_partial = Q.get_to_value(partial_labels);
                Q.reset_set(set_partial);
                Q.set_difference(set_partial,set_exclude_target);
                Q.reset_set(set_partial);
                
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+(getStringList_Of_Set(partial, " , ", Q, sis_session));
                if (Q.set_get_card(set_partial) <= 0) {
                    Q.free_set(partial_labels);
                    break;
                }


                Q.reset_set(set_leaf_topterms);
                Q.reset_set(set_partial);
                Q.set_union(set_leaf_topterms, set_partial);

                Q.free_set(partial_labels);

            }

            Q.free_set(set_partial);
            Q.reset_set(set_leaf_topterms);
            
            //set_leaf_top_terms should now contain all insatnces of targetTerm's nt classes
            StringObject topTermObj = new StringObject();
            dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), topTermObj);
            
            Q.reset_name_scope();
            Q.set_current_node(topTermObj);
            int set_top_terms = Q.get_instances(0);
            Q.reset_set(set_top_terms);
            
            Q.set_intersect(set_leaf_topterms, set_top_terms);
            Q.reset_set(set_leaf_topterms);
            
            ArrayList<String> ntsClasses = new ArrayList<String>();
            ntsClasses.addAll(dbGen.get_Node_Names_Of_Set(set_leaf_topterms, true, Q, sis_session));
            
            Q.free_set(set_top_terms);
            Q.free_set(set_leaf_topterms);
            //both howmanyOrphans and howmanyNonOrphans shoud never be both > 0
            if(howmanyOrphans>0){ //targetTerm will belong to orphans. all its nts must not belong to any other hierarchy without considering targetTerms inheritance
                
                int howManyHiers = ntsClasses.size();
                
                if(howManyHiers==0){
                    
                    Q.free_set(set_exclude_target);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                    
                    return true;
                }
                else
                if(howManyHiers==1){
                    if(ntsClasses.contains(Parameters.UnclassifiedTermsLogicalname)){
                        
                        Q.free_set(set_exclude_target);
                        Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                        
                        return true;
                    }
                    else{
                        ArrayList<String> errorArgs = new ArrayList<String>();
                        errorArgs.add(OrphannsStringList);
                        errorArgs.add(targetTerm);
                        errorArgs.add(targetTerm);
                        errorMsg.setValue(translate(27, 3, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) );
                        
                        Q.free_set(set_exclude_target);
                        Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                        
                        return false;
                    }
                }
                else
                if(howManyHiers>1){
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(OrphannsStringList);
                    errorArgs.add(targetTerm);
                    errorArgs.add(targetTerm);
                    errorMsg.setValue(translate(27, 3, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) );
                    
                    Q.free_set(set_exclude_target);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                    
                    return false;
                }
            }
            
            
            if(howmanyNonOrphans>0){
                if(ntsClasses.contains(Parameters.UnclassifiedTermsLogicalname)){
                    ArrayList<String> errorArgs = new ArrayList<String>();
                    errorArgs.add(NonOrphannsStringList);
                    errorArgs.add(targetTerm);
                    errorArgs.add(targetTerm);
                    errorMsg.setValue(translate(27, 4, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) );
                    
                    Q.free_set(set_exclude_target);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                    
                    return false;
                }
                else{
                    Q.free_set(set_exclude_target);
                    Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS                    
                    return true;
                }
                    
            }
        }
        Q.free_set(set_all_hierarchies);
//EXIT POINT FREE SETS        
        return true;        
    }
    
    /**
     * Checking if new Thesaurus Reference Id is already assigned to another term.
     * 
     * @param SessionUserInfo
     * @param Q
     * @param sis_session
     * @param targetTerm
     * @param bts_Vector
     * @param errorMsg
     * @param pathToErrorsXML
     * @param resolveError
     * @param logFileWriter
     * @param policy
     * @return 
     */
    public boolean create_modify_check_28_alwaysOn(UserInfoClass SessionUserInfo,QClass Q, IntegerObject sis_session,SortItem targetTermSortItem, ArrayList<String> bts_Vector,StringObject errorMsg,String pathToErrorsXML, boolean resolveError, OutputStreamWriter logFileWriter, int policy){
        
        boolean suchATermExists = Q.IsThesaurusReferenceIdAssigned(SessionUserInfo.selectedThesaurus,targetTermSortItem.getThesaurusReferenceId());
        
        if(suchATermExists){
            
            DBGeneral dbGen = new DBGeneral();
            
            String termUsingThisReferenceId = dbGen.removePrefix(Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus, targetTermSortItem.getThesaurusReferenceId()));
            
            if(termUsingThisReferenceId.equals(targetTermSortItem.getLogName())==false){
                ArrayList<String> errorArgs = new ArrayList<String>();

                switch(policy){

                    case IMPORT_COPY_MERGE_THESAURUS_POLICY:{

                        errorArgs.add(""+targetTermSortItem.getThesaurusReferenceId());
                        errorArgs.add(targetTermSortItem.getLogName());                    
                        errorArgs.add(termUsingThisReferenceId);
                        errorArgs.add(targetTermSortItem.getLogName());
                        errorArgs.add(SessionUserInfo.selectedThesaurus);

                        if(resolveError){
                            long refIdCausingProblem = targetTermSortItem.getThesaurusReferenceId();
                            targetTermSortItem.setThesaurusReferenceId(-1);
                            try {
                                logFileWriter.append("\r\n<targetTerm>");
                                logFileWriter.append("<name>" + Utilities.escapeXML(targetTermSortItem.getLogName()) + "</name>");
                                logFileWriter.append("<errorType>" + ConstantParameters.system_referenceIdAttribute_kwd + "</errorType>");
                                logFileWriter.append("<errorValue>" + refIdCausingProblem + "</errorValue>");
                                logFileWriter.append("<reason>" + translate(28, 1, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                                logFileWriter.append("</targetTerm>\r\n");
                            } catch (IOException ex) {
                                Logger.getLogger(ConsistensyCheck.class.getName()).log(Level.SEVERE, null, ex);
                                Utils.StaticClass.handleException(ex);
                            }                        
                            return true; 
                        }

                    }
                    case EDIT_TERM_POLICY:{
                        errorArgs.add(""+targetTermSortItem.getThesaurusReferenceId());
                        errorArgs.add(targetTermSortItem.getLogName());                    
                        errorArgs.add(termUsingThisReferenceId);

                        errorMsg.setValue(translate(28, 2, Create_Modify_XML_STR, errorArgs, pathToErrorsXML) );

                        return false; 

                    }
                    default:

                        return false;
                }
            }
            
        }
        
        return true;        
    }
    
    
    public boolean move_To_Hierarchy_Consistency_Test_1(String selectedThesaurus, QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor,String prefix){
        //Applies to moveActions: MOVE_NODE_ONLY   MOVE_NODE_AND_SUBTREE     CONNECT_NODE_AND_SUBTREE
        //Check if term is Top Term 
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_1");
        
        StringObject sourceObj = new StringObject(prefix + descriptor);

        if (dbGen.NodeBelongsToClass(sourceObj, new StringObject(selectedThesaurus + "TopTerm"), false,Q,sis_session)) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(1,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " is declared as Top Term and cannot be moved from the hierarchy."; //Do not Translate
            return false;
        }
        return true;        
    }
    
    public boolean move_To_Hierarchy_Consistency_Test_2(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, String newBT,String prefix){

        //Applies to moveAction: MOVE_NODE_ONLY  
        //check if sourceObj has any RT relations with all targetObj's BTs recursively -- targetObj included 
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_2");
        int SisSessionId = sis_session.getValue();
        
        //String prefix = dbtr.getThesaurusPrefix_Descriptor(Q,sis_session.getValue());
        StringObject sourceObj = new StringObject(prefix + descriptor);
        StringObject targetObj = new StringObject(prefix + newBT);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);
        

        StringObject tmpMsgObj = new StringObject();
        int set_All_Target_BTs = Q.set_get_new();
        if(dbGen.collect_Recurcively_ALL_BTs(selectedThesaurus,targetObj, set_All_Target_BTs, tmpMsgObj, true,Q,sis_session)==false){
            errorMsg.setValue(errorMsg.getValue().concat(tmpMsgObj.getValue()));
            return false;
        }
        Q.reset_set( set_All_Target_BTs);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"The following BT relationships of term " + newBT + " were collected:\n" + getStringList_Of_Set(set_All_Target_BTs,"\n"));
        /*Check if target node exists*/
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(2,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.";
            return false;
        }

        /*Rts colletion process Starts*/
        int set_Source_Node = Q.set_get_new();
        Q.set_put( set_Source_Node);
        Q.reset_set( set_Source_Node);
        int set_RTs = Q.set_get_new();

        dbGen.collect_Direct_Links_Of_Set(set_Source_Node, set_RTs, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"The following RT relationships of term " + descriptor + " were collected:\n" + getStringList_Of_Set(set_RTs,"\n") );

        //Check Condition
        Q.reset_set( set_RTs);
        Q.reset_set( set_All_Target_BTs);
        Q.set_intersect( set_RTs, set_All_Target_BTs);
        Q.reset_set( set_RTs);

        Q.free_set( set_Source_Node);
        Q.free_set( set_All_Target_BTs);

        if (Q.set_get_card( set_RTs) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_RTs, "', '"));
            errorArgs.add(newBT);

            errorMsg.setValue(errorMsg.getValue().concat(translate(2,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("The following RT of term '" + descriptor + "' : '" + getStringList_Of_Set(set_RTs, "', '") + "' prevent the creation of relationship BT with the term: '" + newBT + "'.";
            Q.free_set( set_RTs);
            return false;
        }
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"card of " + this.targetObj.getValue() + " BTS : " + Q.set_get_card(set_All_Terget_BTs));
        Q.free_set( set_RTs);


        return true;
    }
            
    public boolean move_To_Hierarchy_Consistency_Test_3(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor,String prefix){
        //Applies to moveAction: MOVE_NODE_ONLY

        // set_1 Get Direct BTs of sourceObj --> sourceObj not included 
        // set_2 Get direct Nts of sourceObj --> sourceObj not included
        // set_3 Get recursive BTs of set 2  --> sourceObj not included in every round of recursion

        // set_1 must have nothing in common with set_3
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_3");
        int SisSessionId = sis_session.getValue();
        
        StringObject sourceObj = new StringObject(prefix + descriptor);
        StringObject btFromObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btLinkObj,Q,sis_session);
        
        /*Create set_1*/
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(3,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.";
            return false;
        }

        int set_0 = Q.set_get_new();
        Q.set_put( set_0);
        Q.reset_set( set_0);

        int set_1 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_0, set_1, false, btFromObj.getValue(),btLinkObj.getValue(), ConstantParameters.FROM_Direction,Q,sis_session);
        Q.reset_set( set_1);

        /*Create Set 2*/
        Q.reset_name_scope();
        int set_2 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_0, set_2, false, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.TO_Direction,Q,sis_session);
        Q.reset_set( set_2);
/*
        if(Q.set_get_card(set_2)==0){
            Q.free_set(set_0);
            Q.free_set(set_1);
            Q.free_set(set_2);
            return true;
        }
*/      /*create set_3*/
        //collect all dirrect Bts of set_2
        int set_3 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_2, set_3, false, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.FROM_Direction,Q,sis_session);
        Q.reset_set( set_3);

        //exlude source obj from set_3 
        Q.reset_set( set_0);
        Q.set_difference( set_3, set_0);
        Q.reset_set( set_3);

        //continue with recursive collection of set_2 remaining BTs
        dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_3, set_3, false,Q,sis_session);

        //Check Condition
        Q.reset_set( set_3);
        Q.reset_set( set_1);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\nThe Recursive BT of the immediate NTs of term: '" + descriptor + "', excluding the hierarchical brancehs that include term: '" + descriptor + "' are the following: \n'" + getStringList_Of_Set(set_3, "'\n'") + "'\n");

        Q.set_intersect( set_1, set_3);
        Q.reset_set( set_1);

        Q.free_set( set_2);
        Q.free_set( set_3);
        if (Q.set_get_card( set_1) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_1, "'\n'"));
            errorMsg.setValue(errorMsg.getValue().concat(translate(3,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term '" + descriptor +
            //        "' cannot be moved due to the relationships that will be created from the NTs of the terms: '" +
            //        getStringList_Of_Set(set_1, "'\n'") + "'.";
            Q.free_set( set_1);
            return false;
        }

        Q.free_set( set_1);

        return true;        
    }
    
    public boolean move_To_Hierarchy_Consistency_Test_4(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, String newBT,String prefix){
        //Applies to moveActions: MOVE_NODE_AND_SUBTREE 
        //All target BTs recursively target included, and all their RTs nothing in common with all Nts of source and all their BTs recursively excluding E
        
        // set_0: includes source
        // set_1: must include NTs of sourceObj recursively without sourceObj 
        // set_2: must include direct BTs of set_1 and set_1 itself --> thus it will also include source but not its other BTs as they will be lost
        // set_3: must include all BTs recursively of targetObj recursively and targetObj itself
        // set_2 and set_3 nothing in common 
        // THUS Cycle avoided (nothing in set 3 meets with set_1 and sourceObj both included in set_2) 
        // and no node under source node has direct BT links to any term above target or target itself
        
        // set_4: must include all RTs of set_3
        // set_4 and set_2 must have nothing in common

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_4");
        int SisSessionId = sis_session.getValue();

        StringObject sourceObj = new StringObject(prefix + descriptor);
        StringObject targetObj = new StringObject(prefix + newBT);
        StringObject btFromObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btLinkObj,Q,sis_session);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);


        //Create set_1 with all NTS recursively of source - source not included
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(4,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.";
            return false;
        }
        
        int set_0 = Q.set_get_new();
        Q.set_put( set_0);
        Q.reset_set( set_0);

        //ArrayList<String> test = dbGen.get_Node_Names_Of_Set(set_0, false, Q, sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(test.toString());
        int set_1 = Q.set_get_new();
        dbGen.collect_Recurcively_ALL_NTs_Of_Set(selectedThesaurus,set_0, set_1, false,Q,sis_session);
        Q.reset_set( set_1);
        

        //ArrayList<String> test1 = dbGen.get_Node_Names_Of_Set(set_1, false, Q, sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(test1.toString());
        //Create set_2
        int set_2 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_1, set_2, true, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.FROM_Direction,Q,sis_session);
        Q.reset_set( set_2);
        Q.reset_set( set_0);
        Q.set_union(set_2,set_0);
        Q.reset_set( set_2);
        Q.free_set(set_0);
        
        //ArrayList<String> test2 = dbGen.get_Node_Names_Of_Set(set_2, false, Q, sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(test2.toString());
        Q.reset_name_scope();
        if (Q.set_current_node( targetObj) == QClass.APIFail) {
            
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(newBT);
            errorMsg.setValue(errorMsg.getValue().concat(translate(4,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + newBT + " was not found in the database.";
            Q.free_set( set_1);
            Q.free_set( set_2);
            return false;
        }

        //Create set_3
        int set_3 = Q.set_get_new();
        Q.set_put( set_3);
        Q.reset_set( set_3);

        dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_3, set_3, true,Q,sis_session);
        Q.reset_set( set_3);
        //ArrayList<String> test3 = dbGen.get_Node_Names_Of_Set(set_3, false, Q, sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(test3.toString());

        //Create set_4
        int set_4 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_3, set_4, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
        Q.reset_set( set_4);

        //ArrayList<String> test4 = dbGen.get_Node_Names_Of_Set(set_4, false, Q, sis_session);
        //Utils.StaticClass.webAppSystemOutPrintln(test4.toString());
        //Check Condition 1
        Q.reset_set( set_2);
        Q.reset_set( set_3);
        Q.set_intersect( set_3, set_2);
        Q.reset_set( set_3);
        
        if (Q.set_get_card( set_3) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_3, "'\n'")+"\n");
            errorMsg.setValue(errorMsg.getValue().concat(translate(4,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to the preexistance of hierarchical relationships of '" + descriptor + "' and its NTS from/to terms: '" +
            //        getStringList_Of_Set(set_3, "'\n'") + "'";
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            Q.free_set( set_4);
            return false;
        }    
        
        
        //Check Condition 1
        Q.reset_set( set_2);
        Q.reset_set( set_4);
        Q.set_intersect( set_4, set_2);
        Q.reset_set( set_4);
        
        if (Q.set_get_card( set_4) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(newBT);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_4, "'\n'")+"\n");
            errorMsg.setValue(errorMsg.getValue().concat(translate(4,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to the preexistance of associative relationships of ''" + descriptor + "' and its NTS from/to terms: '" +
            //        getStringList_Of_Set(set_4, "'\n'") + "'";
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            Q.free_set( set_4);
            return false;
        }    
        
        Q.free_set( set_1);
        Q.free_set( set_2);
        Q.free_set( set_3);
        Q.free_set( set_4);
      
        return true;        
    }
    
    public boolean move_To_Hierarchy_Consistency_Test_5(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, String newBT,String prefix){
        //Applies to moveActions: CONNECT_NODE_AND_SUBTREE

        //set_0 includes targetObj only
        //set_1 includes sourceObj
        //set_2 must include all Bts of sourceObj recursively with sourceObj
        //set_3 must include only Rts of set_2 without set_2

        //set_2 must have nothing in common with set_0
        //set_3 must have nothing in common with set_0
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_5");
        int SisSessionId = sis_session.getValue();

        StringObject sourceObj = new StringObject(prefix + descriptor);
        StringObject targetObj = new StringObject(prefix + newBT);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);
        
        
        //Create set_0
        Q.reset_name_scope();
        if (Q.set_current_node( targetObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(newBT);
            errorMsg.setValue(errorMsg.getValue().concat(translate(5,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + newBT + " was not found in the database.";
            return false;
        }
        
        int set_0 = Q.set_get_new();
        Q.set_put( set_0);
        Q.reset_set( set_0);
               
        
        //Create set_1
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(5,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.";
            Q.free_set( set_0);
            return false;
        }

        int set_1 = Q.set_get_new();
        Q.set_put( set_1);
        Q.reset_set( set_1);

        
        //Create set_2
        int set_2 = Q.set_get_new();
        dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_1, set_2, true,Q,sis_session);
        Q.reset_set( set_2);
        
        //Create set_3
        int set_3 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_2, set_3, false,rtFromObj.getValue(),rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
        Q.reset_set( set_3);
        
        
        //Check Hierarchical condition of set_2 with set_0
        Q.reset_set( set_2);
        Q.reset_set( set_0);
        Q.set_intersect( set_2, set_0);
        Q.reset_set( set_2);
        
        
        if (Q.set_get_card( set_2) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_2, "'\n'")+"\n");
            errorMsg.setValue(errorMsg.getValue().concat(translate(5,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting relationships of '" + newBT + "' and its NTS, From/to terms: '" +
            //        getStringList_Of_Set(set_3, "'\n'") + "'.";
            Q.free_set( set_0);
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            return false;
        }
        
        //Check Hierarchical condition of set_3 with set_0
        Q.reset_set( set_3);
        Q.reset_set( set_0);
        Q.set_intersect( set_3, set_0);
        Q.reset_set( set_3);
        


        if (Q.set_get_card( set_3) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_3, "'\n'")+"\n");
            errorMsg.setValue(errorMsg.getValue().concat(translate(5,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting relationships of '" + newBT + "' and its NTS, From/to terms: '" +
            //        getStringList_Of_Set(set_3, "'\n'") + "'.";
            Q.free_set( set_0);
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            return false;
        }

        Q.free_set( set_0);
        Q.free_set( set_1);
        Q.free_set( set_2);
        Q.free_set( set_3);
                
        
        return true;        
    }
    
    public boolean move_To_Hierarchy_Consistency_Test_6(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, String newBT,String prefix){
        //Applies to moveAction: CONNECT_NODE_AND_SUBTREE  similar to check 4 

        // set_0: includes source
        // set_1: must include NTs of sourceObj recursively with sourceObj 
        // set_2: must include direct BTs of set_1 and set_1 itself 
        // set_3: must include all BTs recursively of targetObj recursively and targetObj itself

        // set_2 and set_3 nothing in common 
        // THUS Cycle avoided (nothing in set 3 meets with set_1 which is included in set_2) 
        // and no node under source node has direct BT links to any term above target or target itself

        // set_4: must include all RTs of set_3
        // set_4 and set_2 must have nothing in common

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_6");
        int SisSessionId = sis_session.getValue();

        //String prefix = dbtr.getThesaurusPrefix_Descriptor(Q,sis_session.getValue());
        StringObject sourceObj = new StringObject(prefix + descriptor);
        StringObject targetObj = new StringObject(prefix + newBT);
        StringObject btFromObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btLinkObj,Q,sis_session);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);


        //Create set_1 with all NTS recursively of source - source not included
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(6,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.");
            return false;
        }
        
        int set_0 = Q.set_get_new();
        Q.set_put( set_0);
        Q.reset_set( set_0);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_0 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_0, "'\n'"));

        int set_1 = Q.set_get_new();
        dbGen.collect_Recurcively_ALL_NTs_Of_Set(selectedThesaurus,set_0, set_1, true,Q,sis_session);
        Q.reset_set( set_1);
        Q.free_set(set_0);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_1 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_1, "'\n'"));

        //Create set_2
        int set_2 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_1, set_2, true, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.FROM_Direction,Q,sis_session);
        Q.reset_set( set_2);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_2 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_2, "'\n'"));
        
        Q.reset_name_scope();
        if (Q.set_current_node( targetObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(newBT);
            errorMsg.setValue(errorMsg.getValue().concat(translate(6,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + newBT + " was not found in the database."));
            Q.free_set( set_1);
            Q.free_set( set_2);
            return false;
        }

        //Create set_3
        int set_3 = Q.set_get_new();
        Q.set_put( set_3);
        Q.reset_set( set_3);

        dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_3, set_3, true,Q,sis_session);
        Q.reset_set( set_3);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_3 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_3, "'\n'"));
        //Create set_4
        int set_4 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_3, set_4, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
        Q.reset_set( set_4);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_4 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_4, "'\n'"));
        
        
        //Check Condition 1
        Q.reset_set( set_2);
        Q.reset_set( set_3);
        Q.set_intersect( set_3, set_2);
        Q.reset_set( set_3);
        
        if (Q.set_get_card( set_3) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_4, "'\n'")+"\n");
            errorMsg.setValue(errorMsg.getValue().concat(translate(6,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting hierarchical relationships of '" + descriptor + "' and its NTS, From/to terms:  '" +
            //        getStringList_Of_Set(set_3, "'\n'") + "'";
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            Q.free_set( set_4);
            return false;
        }    
        
        
        //Check Condition 1
        Q.reset_set( set_2);
        Q.reset_set( set_4);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_2 has:\n" + getStringList_Of_Set(Q,sis_session,dbGen,set_2, "', '"));
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_4 has:\n" + getStringList_Of_Set(Q,sis_session,dbGen,set_4, "', '"));
        Q.set_intersect( set_4, set_2);
        Q.reset_set( set_4);
        
        if (Q.set_get_card( set_4) != 0) {

            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorArgs.add(newBT);
            errorArgs.add(descriptor);
            errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_4, "', '"));
            errorMsg.setValue(errorMsg.getValue().concat(translate(6,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
            //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting associative relationships of '" + descriptor + "' and its NTS, From/to terms: '" +
            //        getStringList_Of_Set(set_4, "'\n'") + "'";
            Q.free_set( set_1);
            Q.free_set( set_2);
            Q.free_set( set_3);
            Q.free_set( set_4);
            return false;
        }    
        
        Q.free_set( set_1);
        Q.free_set( set_2);
        Q.free_set( set_3);
        Q.free_set( set_4);
      
        return true;        
    }
    
    
    public boolean move_To_Hierarchy_Consistency_Test_7(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, ArrayList<String> decodedValues,String prefix, boolean resolveError, OutputStreamWriter logFileWriter){
        //Applies to moveActions: CONNECT_NODE_AND_SUBTREE similar to cc 5 except that source node's bts are now passed as parameters

        //in each loop set_0 includes targetObj  only
        //set_1 includes sourceObj
        //set_all_Bts includes all new declared BTs
        //set_2patch must include all new Bts(except the one tested) of sourceObj recursively plus sourceObj itself
        //set_3 must include only Rts of set_2patch without set_2patch

        //In each loop:
            //set_0 includes new BT tested only
            //set_2patch must have nothing in common with set_0
            //set_3 must have nothing in common with set_0
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_7");
        int SisSessionId = sis_session.getValue();

        StringObject sourceObj = new StringObject(prefix + descriptor);
        //StringObject targetObj = new StringObject(prefix + newBT);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);
        StringObject btFromObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btLinkObj,Q,sis_session);
        
        boolean btsRemoved = false;
        ArrayList<String> removeBts = new ArrayList<String>();
        //Create set_1
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(7,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.");
            return false;
        }

        int set_1 = Q.set_get_new();
        Q.set_put( set_1);
        Q.reset_set( set_1);
        
        //Create set_set_all_Bts
        int set_all_Bts = Q.set_get_new();
        for(int i=0; i<decodedValues.size();i++){
            
            StringObject targetObj = new StringObject(prefix + decodedValues.get(i));
            Q.reset_name_scope();
            if (Q.set_current_node( targetObj) == QClass.APIFail) {
                ArrayList<String> errorArgs=new ArrayList<String>();
                errorArgs.add(decodedValues.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(7,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Term " + newBT + " was not found in the database."));
                Q.free_set(set_1);
                return false;
            }
            else{
                Q.set_put( set_all_Bts);
                Q.reset_set( set_all_Bts);
            }
        }
        Q.reset_set( set_all_Bts);
        
        //set2patch contains all new BTs of source Term except for the BT tested which is contained is set_0
        //set2patch also contains recursively all Bts of its new bts
        //set2patch is finally extended to include source node it self in set_1 without any of its older bts
        
        //bt_tested is the only one included in  set_0
        for(int i=0; i<decodedValues.size();i++){
            
            StringObject targetObj = new StringObject(prefix + decodedValues.get(i));
            int set_2patch = Q.set_get_new();
            Q.reset_set(set_2patch);
            
            int set_0 = Q.set_get_new();
            Q.reset_set(set_0);
            Q.reset_name_scope();
            if (Q.set_current_node( targetObj) == QClass.APIFail) {
                ArrayList<String> errorArgs=new ArrayList<String>();
                errorArgs.add(decodedValues.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(7,2,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                //errorMsg.setValue(errorMsg.getValue().concat("Term " + newBT + " was not found in the database."));
                Q.free_set(set_0);
                Q.free_set(set_1);
                Q.free_set(set_2patch);
                Q.free_set(set_all_Bts);
                return false;
            }
            else{
                Q.set_put( set_0);
                Q.reset_set( set_0);                
                Q.set_union(set_2patch, set_all_Bts);
                Q.reset_set( set_2patch);
                Q.set_difference(set_2patch, set_0);
                Q.reset_set( set_2patch);                
            }            
            
            int set_2patchCopy = Q.set_get_new();
            Q.reset_set(set_2patchCopy);
            Q.set_union(set_2patchCopy,set_2patch);
            Q.reset_set(set_2patchCopy);
            
            dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_2patch, set_2patch, false,Q,sis_session);
            Q.reset_set(set_2patch);
            
            Q.reset_set(set_2patch);
            Q.reset_set(set_1);
            Q.set_union(set_2patch, set_1); // include sourceObjet
            Q.reset_set(set_2patch);
            
            
            int set_2patchCopy2 = Q.set_get_new();
            Q.reset_set(set_2patchCopy2);
            Q.set_union(set_2patchCopy2,set_2patch);
            Q.reset_set(set_2patchCopy2);
                       
            //Create set_3
            int set_3 = Q.set_get_new();
            dbGen.collect_Direct_Links_Of_Set(set_2patch, set_3, false,rtFromObj.getValue(),rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
            Q.reset_set( set_3);
            
            
            
            //Check Hierarchical condition of set_2 with set_0
            
            Q.reset_set( set_2patch);
            Q.reset_set( set_0);
            Q.set_intersect( set_2patch, set_0);
            Q.reset_set( set_2patch);
            
            if (Q.set_get_card( set_2patch) != 0) {
                                
                int set_Find_out_nodes = Q.set_get_new();
                dbGen.collect_Direct_Links_Of_Set(set_2patch, set_Find_out_nodes, false, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.TO_Direction, Q, sis_session);
                Q.reset_set( set_Find_out_nodes);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_Find_out_nodes\n" +getStringList_Of_Set(Q,sis_session,dbGen,set_Find_out_nodes, "', '"));
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_2patchCopy\n" +getStringList_Of_Set(Q,sis_session,dbGen,set_2patchCopy, "', '"));
                Q.set_intersect(set_Find_out_nodes, set_2patchCopy);
                Q.reset_set( set_Find_out_nodes);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_Find_out_nodes\n" +getStringList_Of_Set(Q,sis_session,dbGen,set_Find_out_nodes, "', '"));
                
                
                ArrayList<String> errorArgs=new ArrayList<String>();
                errorArgs.add(descriptor);
                errorArgs.add(decodedValues.get(i));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_Find_out_nodes, "', '"));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_2patch, "', '"));
                Q.free_set(set_1);
                Q.free_set(set_all_Bts);
                
                Q.free_set(set_2patch);
                Q.free_set(set_2patchCopy);
                Q.free_set(set_2patchCopy2);
                Q.free_set(set_Find_out_nodes);
                Q.free_set(set_0);
                Q.free_set( set_3);
                
                try {
                    if (resolveError) {
                        
                        //logFileWriter.append("\r\n" + translate(7, 3, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "\r\n");
                        btsRemoved = true;
                        removeBts.add(decodedValues.get(i));
                        
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(errorArgs.get(0)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(decodedValues.get(i)) + "</errorValue>");
                        if(errorArgs.get(2)!=null && errorArgs.get(2).length()>0){
                            logFileWriter.append("<reason>" +translate(7, 3, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        else{
                            logFileWriter.append("<reason>" +translate(7, 5, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        logFileWriter.append("</targetTerm>\r\n");
                        continue;
                    } else {
                        errorMsg.setValue(errorMsg.getValue().concat(translate(7, 3, MoveToHier_XML_STR, errorArgs, pathToErrorsXML)));
                        return false;
                    }
                } catch (IOException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(7, 3, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "\n" + ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                
                //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
                //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting relationships of '" + newBT + "' and its NTS, From/to terms: '" +
                //        getStringList_Of_Set(set_3, "'\n'") + "'.";
                
               
            }
            
            //Check Hierarchical condition of set_3 with set_0
            Q.reset_set( set_3);
            Q.reset_set( set_0);
            Q.set_intersect( set_3, set_0);
            Q.reset_set( set_3);



            if (Q.set_get_card( set_3) != 0) {

                                 
                int set_Find_out_nodes = Q.set_get_new();
                dbGen.collect_Direct_Links_Of_Set(set_3, set_Find_out_nodes, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction, Q, sis_session);
                Q.reset_set( set_Find_out_nodes);
                Q.set_intersect(set_Find_out_nodes, set_2patchCopy2);
                Q.reset_set( set_Find_out_nodes);
                
                ArrayList<String> errorArgs=new ArrayList<String>();
                errorArgs.add(descriptor);
                errorArgs.add(decodedValues.get(i));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_Find_out_nodes, "', '"));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_3, "', '"));
                //errorMsg.setValue(errorMsg.getValue().concat(translate(7,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+);
                //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
                //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting relationships of '" + newBT + "' and its NTS, From/to terms: '" +
                //        getStringList_Of_Set(set_3, "'\n'") + "'.";
                Q.free_set(set_1);
                Q.free_set(set_all_Bts);
                
                Q.free_set(set_2patch);
                Q.free_set(set_2patchCopy);
                Q.free_set(set_2patchCopy2);
                Q.free_set(set_0);
                Q.free_set( set_3);
                
                Q.free_set( set_Find_out_nodes);
                
                 
                try {
                    if (resolveError) {                        
                        //logFileWriter.append("\r\n" + translate(7,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "\r\n");
                        btsRemoved = true;
                        removeBts.add(decodedValues.get(i));
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(errorArgs.get(0)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(decodedValues.get(i)) + "</errorValue>");

                        if(errorArgs.get(2)!=null && errorArgs.get(2).length()>0){
                            logFileWriter.append("<reason>" +translate(7, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        else{
                            logFileWriter.append("<reason>" +translate(7, 6, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        
                        logFileWriter.append("</targetTerm>\r\n");
                        continue;
                    } else {
                        errorMsg.setValue(errorMsg.getValue().concat(translate(7,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                        return false;
                    }
                } catch (IOException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(7,4,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "\n" + ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
            
            Q.free_set(set_2patch);
            Q.free_set(set_2patchCopy);
            Q.free_set(set_2patchCopy2);
            Q.free_set(set_0);
            Q.free_set( set_3);
        }
     
        Q.free_set( set_1);
        Q.free_set( set_all_Bts);
        
        if(resolveError && btsRemoved){
            for(int i =0; i< removeBts.size(); i ++){
                if(decodedValues.contains(removeBts.get(i))){
                    decodedValues.remove(removeBts.get(i));
                    
                }
            }
        }
        
        if(decodedValues.size()<=0){
            return false;
        }
        else {
            return true;
        }        
    }
    
    
    public boolean move_To_Hierarchy_Consistency_Test_8(String selectedThesaurus,QClass Q, IntegerObject sis_session, DBGeneral dbGen,StringObject errorMsg,String pathToErrorsXML,String descriptor, ArrayList<String> decodedValues,String prefix, boolean resolveError, OutputStreamWriter logFileWriter){
        //Applies to moveAction: CONNECT_NODE_AND_SUBTREE  similar to check 6 but decodedValues will declare source Nodes bts
        // thus set_1 should change and so that it does not include sourceObj and set 2 should be appended with nodes defined from decoded values 

        // set_0:includes only sourceObj
        // set_1: must include NTs of sourceObj recursively without sourceObj 
        // set_2: must include direct BTs of set_1 and set_1 itself plus set_0 (case of not nts)
        // set_allBts: includes all new Bts passed in ArrayList<String> parameter decodedValues
        // set_2patch will initially contain all contents of set_all_Bts, except for the new BT that is tested in each loop independently of its prior source Node's prior/current BTs
        // set_3: initially includes targetObj and is used in a set difference action for set_2patch in order to 
        //        exclude the bt that is tested in each loop.
        //        It then includes all BTs recursively of targetObj and targetObj itself

        //In each loop:
            // set_2patch and set_3 nothing in common 
            // THUS Cycle avoided (nothing in set 3 meets with set_1 which is included in set_2) 
            // and no node under source node has direct BT links to any term above target or target itself

            // set_4: must include all RTs of set_3
            // set_4 and set_2 must have nothing in common

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"move_ToHierarchy_Consistency_Test_8");
        int SisSessionId = sis_session.getValue();

        //String prefix = dbtr.getThesaurusPrefix_Descriptor(Q,sis_session.getValue());
        StringObject sourceObj = new StringObject(prefix + descriptor);
        //StringObject targetObj = new StringObject(prefix + newBT);
        StringObject btFromObj = new StringObject();
        StringObject btLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btLinkObj,Q,sis_session);
        StringObject rtFromObj = new StringObject();
        StringObject rtLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.rt_kwd, rtFromObj, rtLinkObj,Q,sis_session);

        boolean btsRemoved = false;
        ArrayList<String> removeBts = new ArrayList<String>();

        //Create set_1 with all NTS recursively of source - source not included
        Q.reset_name_scope();
        if (Q.set_current_node( sourceObj) == QClass.APIFail) {
            ArrayList<String> errorArgs=new ArrayList<String>();
            errorArgs.add(descriptor);
            errorMsg.setValue(errorMsg.getValue().concat(translate(8,1,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
            
            //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.");
            return false;
        }
        
        
        int set_0 = Q.set_get_new();
        Q.set_put( set_0);
        Q.reset_set( set_0);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_0 has:\n" + getStringList_Of_Set(Q,sis_session,dbGen,set_0, "'\n'"));

        int set_1 = Q.set_get_new();
        //dbGen.collect_Recurcively_ALL_NTs_Of_Set(sessionInstance,set_0, set_1, true,Q,sis_session);
        dbGen.collect_Recurcively_ALL_NTs_Of_Set(selectedThesaurus,set_0, set_1, false,Q,sis_session);
        Q.reset_set( set_1);        
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_1 has:\n" + getStringList_Of_Set(Q,sis_session,dbGen,set_1, "'\n'"));

        //Create set_2
        int set_2 = Q.set_get_new();
        dbGen.collect_Direct_Links_Of_Set(set_1, set_2, true, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.FROM_Direction,Q,sis_session);
        Q.reset_set( set_2);
        
        //in case set_2 had no nts then include source node in results
        Q.set_union(set_2,set_0);
        Q.reset_set( set_2);
        
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_2 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_2, "'\n'"));
        
        //collect all new bts in a set which will represent its new condition above this term
        int set_allBts = Q.set_get_new();
        for(int i=0; i<decodedValues.size(); i++){
            
            Q.reset_name_scope();
            StringObject newBTObj = new StringObject(prefix + decodedValues.get(i));
            if (Q.set_current_node( newBTObj) == QClass.APIFail) {
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(decodedValues.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(8, 2, MoveToHier_XML_STR, errorArgs, pathToErrorsXML)));
                Q.free_set(set_1);
                Q.free_set(set_2);
                Q.free_set(set_allBts);
                //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.");
                return false;
                
            } else {
                Q.set_put(set_allBts);
                Q.reset_set(set_allBts);
            }
        }
        
        Q.reset_set(set_allBts);
        
        //set_2patch will initially contain contents of set_all_Bts, except for the new BT that is tested in each loop
        //After that set_2patch will be unioned with set_2
        //
        for(int i=0; i<decodedValues.size(); i++){
            
            int set_2patch = Q.set_get_new();
            
            int set_3 = Q.set_get_new();
            Q.reset_name_scope();
            StringObject newBTObj = new StringObject(prefix + decodedValues.get(i));
            if (Q.set_current_node( newBTObj) == QClass.APIFail) {
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(decodedValues.get(i));
                errorMsg.setValue(errorMsg.getValue().concat(translate(8, 2, MoveToHier_XML_STR, errorArgs, pathToErrorsXML)));
                Q.free_set(set_1);
                Q.free_set(set_2);
                Q.free_set(set_3);
                //errorMsg.setValue(errorMsg.getValue().concat("Term " + descriptor + " was not found in the database.");
                return false;
                
            } else {
                Q.set_put(set_3);
                Q.reset_set(set_3);
                
                Q.reset_set(set_allBts);
                Q.reset_set(set_2patch);
                Q.set_union(set_2patch,set_allBts);
                
                Q.reset_set(set_2patch);
                Q.set_difference(set_2patch, set_3);
                Q.reset_set(set_2patch);
                Q.reset_set(set_3);
                
                
                Q.reset_set(set_2);
                Q.reset_set(set_2patch);
                Q.set_union(set_2patch,set_2);
            }
            
            dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus,set_3, set_3, true,Q,sis_session);
            Q.reset_set( set_3);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_3 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_3, "'\n'"));
            
            
            //Create set_4
            int set_4 = Q.set_get_new();
            dbGen.collect_Direct_Links_Of_Set(set_3, set_4, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction,Q,sis_session);
            Q.reset_set( set_4);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_4 has" + getStringList_Of_Set(Q,sis_session,dbGen,set_4, "'\n'"));

            int set_3Copy = Q.set_get_new();
            Q.reset_set(set_3Copy);
            Q.set_union(set_3Copy,set_3);
            Q.reset_set(set_3Copy);
            
            int set_4Copy = Q.set_get_new();
            Q.reset_set(set_4Copy);
            Q.set_union(set_4Copy,set_4);
            Q.reset_set(set_4Copy);
            
            
            //Check Condition 1
            Q.reset_set( set_2patch);
            Q.reset_set( set_3);
            Q.set_intersect( set_3, set_2patch);
            Q.reset_set( set_3);

            if (Q.set_get_card( set_3) > 0) {

                int set_Find_out_nodes = Q.set_get_new();
                dbGen.collect_Direct_Links_Of_Set(set_3Copy, set_Find_out_nodes, false, btFromObj.getValue(), btLinkObj.getValue(), ConstantParameters.TO_Direction, Q, sis_session);
                Q.reset_set( set_Find_out_nodes);
                Q.set_intersect(set_Find_out_nodes, set_3Copy);
                Q.reset_set( set_Find_out_nodes);
                
                ArrayList<String> errorArgs=new ArrayList<String>();
                errorArgs.add(descriptor);
                errorArgs.add(decodedValues.get(i));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_Find_out_nodes, "', '"));
                errorArgs.add(getStringList_Of_Set(Q,sis_session,dbGen,set_3, "', '"));
                //errorMsg.setValue(errorMsg.getValue().concat(translate(8,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+);
                //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
                //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting hierarchical relationships of '" + descriptor + "' and its NTS, From/to terms: '" +
                //        getStringList_Of_Set(set_3, "'\n'") + "'";
                Q.free_set( set_0);
                Q.free_set( set_1);
                Q.free_set( set_2);
                Q.free_set( set_allBts);
                
                Q.free_set( set_2patch);                
                Q.free_set( set_3);
                Q.free_set( set_4);
                Q.free_set(set_3Copy);
                Q.free_set(set_4Copy);
                
                Q.free_set(set_Find_out_nodes);
                
                try {
                    if (resolveError) {
                        
                        //logFileWriter.append("\r\n" + translate(8,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "\r\n");
                        btsRemoved = true;
                        removeBts.add(decodedValues.get(i));
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(errorArgs.get(0)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(decodedValues.get(i)) + "</errorValue>");
                        if(errorArgs.get(2)!=null && errorArgs.get(2).length()>0){
                            logFileWriter.append("<reason>" + translate(8,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "</reason>");
                        }
                        else{
                            logFileWriter.append("<reason>" + translate(8,5,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "</reason>");
                        }
                        
                        logFileWriter.append("</targetTerm>\r\n");
                        continue;
                    } else {
                        errorMsg.setValue(errorMsg.getValue().concat(translate(8,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML)));
                        return false;
                    }
                } catch (IOException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(8,3,MoveToHier_XML_STR,errorArgs,pathToErrorsXML) + "\n" + ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
                
            }    
            
            
            //Check Condition 2
            Q.reset_set( set_2patch);
            Q.reset_set( set_4);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_2 has:\n" + getStringList_Of_Set(Q, sis_session, dbGen, set_2patch, "'\n'"));
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"set_4 has:\n" + getStringList_Of_Set(Q, sis_session, dbGen, set_4, "'\n'"));
            Q.set_intersect( set_4, set_2patch);
            Q.reset_set( set_4);

            if (Q.set_get_card( set_4) > 0) {
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error : set_4 has:\n" + getStringList_Of_Set(Q, sis_session, dbGen, set_4, "'\n'"));
                int set_Find_out_nodes = Q.set_get_new();
                dbGen.collect_Direct_Links_Of_Set(set_4, set_Find_out_nodes, false, rtFromObj.getValue(), rtLinkObj.getValue(), ConstantParameters.BOTH_Direction, Q, sis_session);
                Q.reset_set( set_Find_out_nodes);
                Q.set_intersect(set_Find_out_nodes, set_3Copy);
                Q.reset_set( set_Find_out_nodes);
                
                
                ArrayList<String> errorArgs = new ArrayList<String>();
                errorArgs.add(descriptor);
                errorArgs.add(decodedValues.get(i));
                errorArgs.add(getStringList_Of_Set(Q, sis_session, dbGen, set_Find_out_nodes, "', '") );
                errorArgs.add(getStringList_Of_Set(Q, sis_session, dbGen, set_4, "', '") );
                //errorMsg.setValue(errorMsg.getValue().concat(translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML)));
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML));
                //errorMsg.setValue(errorMsg.getValue().concat("Term: '" + descriptor +
                //        "' cannot be moved under the BT: '" + newBT + "'\ndue to preexisting associative relationships of '" + descriptor + "' and its NTS, From/to terms: '" +
                //        getStringList_Of_Set(set_4, "'\n'") + "'";
                Q.free_set( set_0);
                Q.free_set( set_1);
                Q.free_set( set_2);
                Q.free_set( set_allBts);
                
                Q.free_set( set_2patch);                
                Q.free_set( set_3);
                Q.free_set( set_4);
                Q.free_set(set_3Copy);
                Q.free_set(set_4Copy);
                
                Q.free_set(set_Find_out_nodes);
                try {
                    if (resolveError) {
                        
                        //logFileWriter.append("\r\n" + translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "\r\n");
                        btsRemoved = true;
                        removeBts.add(decodedValues.get(i));
                        logFileWriter.append("\r\n<targetTerm>");
                        logFileWriter.append("<name>" + Utilities.escapeXML(errorArgs.get(0)) + "</name>");
                        logFileWriter.append("<errorType>" + ConstantParameters.bt_kwd + "</errorType>");
                        logFileWriter.append("<errorValue>" + Utilities.escapeXML(decodedValues.get(i)) + "</errorValue>");
                        if(errorArgs.get(2)!=null && errorArgs.get(2).length()>0){
                            logFileWriter.append("<reason>" + translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        else{
                            logFileWriter.append("<reason>" + translate(8, 6, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "</reason>");
                        }
                        
                        logFileWriter.append("</targetTerm>\r\n");
                        continue;
                    } else {
                        errorMsg.setValue(errorMsg.getValue().concat(translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML)));
                        return false;
                    }
                } catch (IOException ex) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Failed in writing: " + translate(8, 4, MoveToHier_XML_STR, errorArgs, pathToErrorsXML) + "\n" + ex.getMessage());
                    Utils.StaticClass.handleException(ex);
                }
            }
        
                
            Q.free_set( set_2patch);                
            Q.free_set( set_3);
            Q.free_set( set_4);
            Q.free_set(set_3Copy);
            Q.free_set(set_4Copy);
                
  
        }
        Q.free_set(set_0);
        Q.free_set( set_1);
        Q.free_set( set_2);
        Q.free_set( set_allBts); 
       
      
        if(resolveError && btsRemoved){
            for(int i =0; i< removeBts.size(); i ++){
                if(decodedValues.contains(removeBts.get(i))){
                    decodedValues.remove(removeBts.get(i));                    
                }
            }
        }
        
        if(decodedValues.size()<=0){
            return false;
        }
        else {
            return true;
        }               
    }
     

    public boolean check_facet_deletion(UserInfoClass SessionUserInfo,QClass Q, IntegerObject sis_session,DBGeneral dbGen, String targetFacet, StringObject errorMsg, Locale targetLocale){
       
        int index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);
        
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject targetFacetObj = new StringObject();
        ArrayList<String> subHierarchies = new ArrayList<String>();
        int set_sub_hiers, set_hier_facets, set_f_nodes;
        
        Q.reset_name_scope();
        String prefixClass = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        Q.reset_name_scope();
        
        targetFacetObj.setValue(prefixClass+targetFacet);
        long retL = Q.set_current_node(targetFacetObj);
        if(retL==QClass.APIFail){
            Utilities u = new Utilities();
            errorMsg.setValue(u.translateFromMessagesXML("root/EditFacet/Deletion/FacetNotFound", new String[]{targetFacet}));
            //errorMsg.setValue("Facet " + targetFacet + " was not found.");
            return false;
        }
        set_sub_hiers = Q.get_all_subclasses( 0);
        Q.reset_set(set_sub_hiers);
        
        subHierarchies.addAll(dbGen.get_Node_Names_Of_Set(set_sub_hiers, false, Q, sis_session));
        Q.free_set(set_sub_hiers);
        if(subHierarchies.size()==0){
            return true;
        }
        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);
        Collections.sort(subHierarchies, strCompar);
        set_f_nodes = dbGen.get_Instances_Set(FacetClasses,Q,sis_session);
        Q.reset_set(set_f_nodes);
        
        boolean allHiersMoreThanOneFacet = true;
        String errorHierNames = new String("");
        for(int i=0; i<subHierarchies.size();i++){
            StringObject targetHierObj = new StringObject(subHierarchies.get(i));
            Q.reset_name_scope();
            
            if(Q.set_current_node(targetHierObj)!=QClass.APIFail){
                set_hier_facets = Q.get_superclasses(0);
                Q.reset_set(set_hier_facets);
                Q.set_intersect(set_hier_facets, set_f_nodes);
                Q.reset_set(set_hier_facets);
                
                ArrayList<String> hierFacets = dbGen.get_Node_Names_Of_Set(set_hier_facets, false, Q, sis_session);
                Q.free_set(set_hier_facets);
                if(hierFacets.size()==1){
                    allHiersMoreThanOneFacet = false;
                    errorHierNames = errorHierNames + dbGen.removePrefix(subHierarchies.get(i)) + ", ";
                }
                
            }
        }
        Q.free_set(set_f_nodes);
        if(allHiersMoreThanOneFacet==false){
            errorHierNames = errorHierNames.substring(0, errorHierNames.length()-2);
            
            Utilities u = new Utilities();
            errorMsg.setValue(u.translateFromMessagesXML("root/EditFacet/Deletion/LastFacetForHierarchies", new String[]{targetFacet,errorHierNames}));
            //errorMsg.setValue("Facet " + targetFacet +" is the only one under which heirarchies: " + errorHierNames + " are classifiesd. Facet deletion action was cancelled in order to maintain these hierarchies under at least one facet.");
            return false;
        }
        
        return true;
    }
    
    public String getStringList_Of_Set(QClass Q, IntegerObject sis_session,DBGeneral dbGen,int set_print, String delimiter) {
        String result = new String("");

        Q.reset_name_scope();
        Q.reset_set(set_print);
        //StringObject label = new StringObject();
        int howmany = Q.set_get_card(set_print);
        int index = 0;
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
	if(Q.bulk_return_nodes(set_print, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                index++;
                String temp = row.get_v1_cls_logicalname();
                result += dbGen.removePrefix(temp);
                if (index != howmany) {
                    result += delimiter;
                }
            }
        }
        /*while (Q.retur_nodes(set_print, label) != QClass.APIFail) {
            index++;
            String temp = label.getValue();
            result += dbGen.removePrefix(temp);
            if (index != howmany) {
                result += delimiter;
            }

        }*/


        return result;
    }

    //Turn a comma seperated String to Vector --> No dublicates
    public ArrayList<String> get_Vector_from_String(String str, String delimeter) {

        ArrayList<String> result = new ArrayList<String>();

        if (str == null) {
            return result;
        }

        String[] strArray = str.split(delimeter);

        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i].trim().length() == 0) {
                continue;
            }

            if (!result.contains(strArray[i].trim())) {
                result.add(strArray[i].trim());
            }

        }

        result.trimToSize();

        return result;

    }

    public String translate(int errCode, int errorCase, String groupMode, ArrayList<String> args,String pathToErrorsXML){

        Utilities u = new Utilities();
        
        String lang = Parameters.UILang.toLowerCase();                        

        String expressionBasePath = "CONSISTENCIES_CHECKS/"+groupMode+"/TEST[@id='"+ errCode + "' ]/errorcase[@id='"+errorCase+"']/option[@lang='"+lang+"']";        
        //String expressionBasePath = "CONSISTENCIES_CHECKS/"+groupMode+"/TEST[@id='"+ errCode + "' ]/errorcase[@id='"+errorCase+"']";

        return u.translate(expressionBasePath, args, pathToErrorsXML);
    }
    
    
}
