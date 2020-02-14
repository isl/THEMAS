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
 * WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
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

import Utils.Parameters;
import Utils.Utilities;

import java.util.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
DBConnect_Term
-----------------------------------------------------------------------
class with methods updating (basically for creation) the SIS data base
----------------------------------------------------------------------*/
public class DBConnect_Term {

    /*
    
    QClass Q = new QClass();
    DBGeneral dbGen;
    Utilities u;
    
    HttpServlet ServletCaller;
    //
    IntegerObject sis_session;
    IntegerObject tms_session;
     */
    public static final int CATEGORY_THES_UK_ALT = 0;
    public static final int CATEGORY_THES_UF_TRANSLATIONS = 1;    // constatns used by connectSources()
    public static final int CATEGORY_BT_FOUND_IN = 0;
    
    public static final int CATEGORY_translations_found_in = 1;
    public static final int CATEGORY_PRIMARY_FOUND_IN = 2;
    
    public final int DB_CREATE = 0;
    public final int DB_MODIFY = 1;
    public final int DB_DELETE = 2;

        // constats used by AddComment()
    public static final int COMMENT_CATEGORY_COMMENT = 0;
    public static final int COMMENT_CATEGORY_SCOPENOTE = 1;    
    public static final int COMMENT_CATEGORY_HISTORICALNOTE = 2;
    public static final int COMMENT_CATEGORY_SCOPENOTE_TR = 3;
    public static final int COMMENT_CATEGORY_NOTE = 4;
    
    /*----------------------------------------------------------------------
    Constructor of DBConnect_Term
    -----------------------------------------------------------------------*/
    public DBConnect_Term(/*HttpServlet caller, IntegerObject sisSession, IntegerObject tmsSession*/) {
        /*ServletCaller = caller;
        g = new DBGeneral();
        sis_session = sisSession;
        tms_session = tmsSession;
        u = new Utilities();
        */

    }

    /*---------------------------------------------------------------------
    connectDescriptor()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the Descriptor to be created
    - String bts: the BT values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates a new Descriptor and associates it with the given BTs.
    It creates relations of the type BT from the Descriptor given as 
    parameter with the BTs that the user has specified.
    The BTs must exist in the database otherwise the function returns error.
    Besides creating the BTs relations, the Descriptor is also added under 
    all the hierarchies of the BTs.
    CALLED BY: createDescriptorAndBT()-Create_Or_ModifyDescriptor() ONLY in case of creation!
    ----------------------------------------------------------------------*/
    public String connectDescriptor(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> bts,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
       CMValue cmv = new CMValue();
       cmv.assign_node(targetDescriptor.getValue(), -1, Utilities.getTransliterationString(targetDescriptor.getValue(), true),-1);
       
       return connectDescriptorCMValue(selectedThesaurus,cmv,bts,Q,sis_session,dbGen,TA,tms_session,uiLang);
    }

    /*---------------------------------------------------------------------
    connectDescriptorCMValue()
    -----------------------------------------------------------------------
    INPUT: - CMValue targetDescriptor: the Descriptor to be created
    - String bts: the BT values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates a new Descriptor and associates it with the given BTs.
    It creates relations of the type BT from the Descriptor given as 
    parameter with the BTs that the user has specified.
    The BTs must exist in the database otherwise the function returns error.
    Besides creating the BTs relations, the Descriptor is also added under 
    all the hierarchies of the BTs.
    CALLED BY: createDescriptorAndBT()-Create_Or_ModifyDescriptor() ONLY in case of creation!
    ----------------------------------------------------------------------*/
    public String connectDescriptorCMValue(String selectedThesaurus,CMValue targetDescriptorCmv, ArrayList<String> bts,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        
        // initialize output
        String errorMsg = new String("");
        Utilities u = new Utilities();
        // looking for Descriptor prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String b_prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());

        //Utils.StaticClass.webAppSystemOutPrintln("targetDescriptor: "+ targetDescriptor + "  transliteration: " + transliterationString);
        
        // in case of empty targetDescriptor
        // PERFORMED BY CONSISTENCYCHECK.JAVA
        /*
        if (targetDescriptor.getValue().trim().equals(b_prefix)) {		
        errorMsg = errorMsg.concat("you cannot create a term without a name.");
        return errorMsg;
        }
         */
        // check for the existence of each BT given and collect them in vec_bt Vector
        ArrayList<StringObject> vec_bt = new ArrayList<StringObject>();
        //String[] bt_split = bts.split("###");
        StringObject bt_obj;
        for (int i = 0; i < bts.size(); i++) {
            String tempName = b_prefix.concat(bts.get(i));
            bt_obj = new StringObject(tempName);

            if (dbGen.check_exist(bt_obj.getValue(),Q,sis_session) == false) {
                //Bts declared should already exist in the database.
                errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Creation/BTMustExist", null, uiLang),tms_session) + "");
                continue;
            }
            vec_bt.add(bt_obj);
        }

        // create targetDescriptor if it doesn't exist
        if (dbGen.checkCMV_exist(targetDescriptorCmv, Q,sis_session) == false) {
            int ret = TA.CHECK_CreateDescriptorCMValue(targetDescriptorCmv, (StringObject) vec_bt.get(0));
            if (ret == TMSAPIClass.TMS_APIFail) {
                if(Q.get_error_code().equals(Messages.ErrorCode_For_ThesaurusReferenceIdAlreadyAssigned)){
                    if(targetDescriptorCmv.getRefid()>0){
                        errorMsg = errorMsg.concat(u.translateFromMessagesXML("root/EditTerm/Creation/AlreadyAssignedSpecificTermReferenceId", new String[] { ""+targetDescriptorCmv.getRefid(), dbGen.removePrefix(targetDescriptorCmv.getString()),selectedThesaurus}, uiLang));
                    }
                    else{
                        errorMsg = errorMsg.concat(u.translateFromMessagesXML("root/EditTerm/Creation/ErrorDuringTermReferenceIdAssignement", new String[] { dbGen.removePrefix(targetDescriptorCmv.getString()),selectedThesaurus}, uiLang));
                    }                 
                }
                else{
                    String reasonMessage =dbGen.check_success(ret, TA,null,tms_session);                
                    errorMsg = errorMsg.concat(reasonMessage);
                }
                return errorMsg;
            }
        } else {
            
            //Term dbGen.removePrefix(targetDescriptor.getValue()) already exists in the database.
            errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,
                                       TA, 
                                       u.translateFromMessagesXML("root/EditTerm/Creation/TermAlreadyExists", new String[] { dbGen.removePrefix(targetDescriptorCmv.getString())}, uiLang),
                                       tms_session) 
                                    + "");
            return errorMsg;
        }

        ArrayList<String> hiers = new ArrayList<String>();
        ArrayList<String> prevHiers = new ArrayList<String>();
        for (int i = 0; i < vec_bt.size(); i++) {
            hiers = dbGen.getDescriptorHierarchies(selectedThesaurus, (StringObject) vec_bt.get(i),Q,sis_session, uiLang);
            if (i == 0) {
                prevHiers = dbGen.getDescriptorHierarchies(selectedThesaurus,
                                                          new StringObject(targetDescriptorCmv.getString()),
                                                           Q,
                                                           sis_session, uiLang);
            } else {
                prevHiers = dbGen.getDescriptorHierarchies(selectedThesaurus, (StringObject) vec_bt.get(i - 1),Q,sis_session, uiLang);
            }
            
            for (int j = 0; j < hiers.size(); j++) {
                
                int ret = TA.CHECK_MoveToHierarchy(new StringObject(targetDescriptorCmv.getString()), 
                                                   new StringObject(prevHiers.get(0)),
                                                   new StringObject(hiers.get(j)), 
                                                   vec_bt.get(i), 
                                                   TMSAPIClass.CONNECT_NODE_AND_SUBTREE);
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("" + dbGen.check_success(ret,TA, null,tms_session) + "");
                }
            }
        }
            
        return errorMsg;
    }

    /*---------------------------------------------------------------------
    connectNTs()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String nts: the NT values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type NT from the given Descriptor with 
    the given NTs. If any of the NTs doesn't exist in the database, 
    it is created with the function CreateDescriptor with BT the Descriptor.
    Besides creating the BT relations that the NT have with the Descriptor, 
    the NT is also added to all the hierarchies of the Descriptor.
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectNTs(String selectedThesaurus, StringObject targetDescriptor, String nts,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        String errorMsg = new String("");
        Utilities u = new Utilities();
        
        // looking for Descriptor prefix (EL`)
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //THEMASUserInfo SessionUserInfo = (THEMASUserInfo)sessionInstance.getAttribute("SessionUser");
        String n_prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        // looking for THES1Class`Unclassified
        String orphan = selectedThesaurus.toUpperCase().concat("Class`"+Parameters.UnclassifiedTermsLogicalname);

        // in case of empty NTs list, return
        if (nts.compareTo("") == 0) {
            return errorMsg;
        }
        // fill Vector vec_nt with the given NTs to DB format
        String[] nts_split = nts.split("###");
        ArrayList<StringObject> vec_nt = new ArrayList<StringObject>();
        for (int i = 0; i < nts_split.length; i++) {
            nts_split[i] = n_prefix.concat(nts_split[i]);
            StringObject nt_obj = new StringObject(nts_split[i]);
            vec_nt.add(nt_obj);
        }

        // get the Hierarchies of the targetDescriptor
        ArrayList<String> hiers = dbGen.getDescriptorHierarchies(selectedThesaurus, targetDescriptor,Q,sis_session, uiLang);
        /*if (!hiers.get(0).getClass().getName().equals("neo4j_sisapi.StringObject")) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"connectNTs:: errorMsg = " + (String) hiers.get(0));
            errorMsg = errorMsg.concat((String) hiers.get(0));
            return errorMsg;
        }*/

        // for each NT
        for (int i = 0; i < vec_nt.size(); i++) {
            // NT exists in DB
            if (dbGen.check_exist(((StringObject) vec_nt.get(i)).getValue(),Q,sis_session) == true) {
                if (dbGen.isConcept(selectedThesaurus, ((StringObject) vec_nt.get(i)).getValue(),Q,sis_session) == false) {
                    String str = dbGen.removePrefix(((StringObject) vec_nt.get(i)).getValue());
                    
                    errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,u.translateFromMessagesXML("root/EditTerm/Edit/NtNotInDescriptors", new String[]{str}, uiLang),tms_session) + "");        
                    //errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,"Term " + str + " does not belong in Descriptors and therefore cannot be defined as NT.",tms_session) + "");
                    continue;
                }
                // get the Hierarchies of the current NT
                ArrayList<String> prevHiers = dbGen.getDescriptorHierarchies(selectedThesaurus, (StringObject) vec_nt.get(i),Q,sis_session, uiLang);
                /*if (!prevHiers.get(0).getClass().getName().equals("neo4j_sisapi.StringObject")) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"connectNTs:: errorMsg = " + (String) prevHiers.get(0));
                    errorMsg = errorMsg.concat((String) prevHiers.get(0));
                    return errorMsg;
                }*/
                // MOVE / CONNECT currentNT to all the hierarchies of the Descriptor
                // for each Hierarchy of the targetDescriptor
                for (int j = 0; j < hiers.size(); j++) {
                    int ret;
                    // in case the currentNT is ORPHAN and for ONCE time (the last Hierarchy of the targetDescriptor)
                    // MOVE it to this Hierarchy
                    if (prevHiers.get(0).compareTo(orphan) == 0 && j == (hiers.size() - 1)) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Unclassified");
                        ret = TA.CHECK_MoveToHierarchy( (StringObject) vec_nt.get(i), new StringObject(prevHiers.get(0)),
                                new StringObject(hiers.get(j)), targetDescriptor, TMSAPIClass.MOVE_NODE_AND_SUBTREE);
                    } // for each other, CONNECT it to current Hierarchy of the targetDescriptor
                    else {
                        ret = TA.CHECK_MoveToHierarchy( (StringObject) vec_nt.get(i), new StringObject(prevHiers.get(0)),
                                new StringObject(hiers.get(j)), targetDescriptor, TMSAPIClass.CONNECT_NODE_AND_SUBTREE);
                    }
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        errorMsg = errorMsg.concat("" + dbGen.check_success(ret,TA, null,tms_session) + "");
                    }
                }
            } else { // NT does NOT exist in DB
                // create it with BT the targetDescriptor.
                int ret = TA.CHECK_CreateDescriptor( (StringObject) vec_nt.get(i), targetDescriptor);
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("" + dbGen.check_success(ret, TA,null,tms_session) + "");
                    continue;
                }
                // get the Hierarchies of the current NT
                ArrayList<String> hier0 = dbGen.getDescriptorHierarchies(selectedThesaurus, (StringObject) vec_nt.get(i),Q,sis_session, uiLang);
                /*if (!hier0.get(0).getClass().getName().equals("neo4j_sisapi.StringObject")) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"connectNTs:: errorMsg = " + (String) hier0.get(0));
                    errorMsg = errorMsg.concat((String) hier0.get(0));
                    return errorMsg;
                }*/
                // CONNECT currentNT to all the hierarchies of the Descriptor
                // for each Hierarchy of the targetDescriptor
                for (int j = 0; j < hiers.size(); j++) {
                    ret = TA.CHECK_MoveToHierarchy( (StringObject) vec_nt.get(i), new StringObject(hier0.get(0)),
                            new StringObject(hiers.get(j)), targetDescriptor, TMSAPIClass.CONNECT_NODE_AND_SUBTREE);
                    if (ret == TMSAPIClass.TMS_APIFail) {
                        errorMsg = errorMsg.concat("" + dbGen.check_success(ret,TA, null,tms_session) + "");
                    }
                }

            }
        }

        return errorMsg;
    }

    /*---------------------------------------------------------------------
    connectRTs()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String rts: the RT values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type RT from the given Descriptor with 
    the given RTs. If any of the RTs doesn't exist in the database, 
    it is created with the TMS function CreateDescriptor
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectRTs(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> rts ,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        Utilities u = new Utilities();
        String errorMsg = new String("");
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus,Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for THES1_RT 
        StringObject thes_RT = new StringObject();
        dbtr.getThesaurusCategory_RT(selectedThesaurus,Q,sis_session.getValue(),thes_RT);
        // looking for Descriptor prefix
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        String orphans = prefix.concat(Parameters.UnclassifiedTermsLogicalname);

        /*// in case of empty RTs list, return
        if (rts.compareTo("") == 0) {
            return errorMsg;
        }
        // get the RT values	
        String[] rt_split = rts.split("###");
        // fill a Vector with the RTs with prefix and DB encoding	
        Vector rtsVector = new Vecto();
        for (int i = 0; i < rt_split.length; i++) {
            rt_split[i] = prefix.concat(rt_split[i]);
            rtsVector.addElement(new StringObject(rt_split[i]));
        }
        */
        ArrayList<StringObject> rtsVector = new ArrayList<StringObject>();
        for (int i = 0; i < rts.size(); i++) {            
            rtsVector.add(new StringObject(prefix.concat(rts.get(i))));
        }
        
        // for each RT
        for (int i = 0; i < rtsVector.size(); i++) {
            // in case it doesn't exist
            if (dbGen.check_exist(((StringObject) rtsVector.get(i)).getValue(),Q,sis_session) == false) {
                // create it as orphan
                int ret = TA.CHECK_CreateDescriptor( (StringObject) rtsVector.get(i), new StringObject(orphans));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("" + dbGen.check_success(ret,TA, null,tms_session) + "");
                    continue;
                }
            } else { // RT exists                
                // in case it is not a HierarchyTerm, fill error message
                if (dbGen.NodeBelongsToClass((StringObject) rtsVector.get(i), new StringObject(selectedThesaurus + "HierarchyTerm"), false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(((StringObject) rtsVector.get(i)).getValue());
                    errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,u.translateFromMessagesXML("root/EditTerm/Edit/RTNotInPreferredTerms", new String[]{str}, uiLang),tms_session) + "");
                    //errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,"Term " + str + " does not belong in the preferred terms set, therefore cannot be declared as RT value.",tms_session) + "");
                    continue;
                }
            }
            // create the RT link
            Q.reset_name_scope();
            long sysid1L = Q.set_current_node( (StringObject) rtsVector.get(i));
            CMValue to = new CMValue();
            to.assign_node(((StringObject) rtsVector.get(i)).getValue(), sysid1L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( thesHierarchyTerm);
            Q.set_current_node( thes_RT);
            Q.set_put( catSet);

            int ret;
            if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
                ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            } else {
                ret = TA.CHECK_CreateDescriptorAttribute(new StringObject(), targetDescriptor, to, catSet);
            }
            Q.free_set( catSet);
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat("" + dbGen.check_success(ret, TA,null,tms_session) + "");
            }
        }
        return errorMsg;
    }

    /*---------------------------------------------------------------------
    connectUFs()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String ufs: the UF values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type UF from the given Descriptor with 
    the given UFs. If any of the UFs doesn't exist in the database, 
    it is created with the TMS function CreateUsedForTerm
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectUFs(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> ufs,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        Utilities u = new Utilities();
        
        String errorMsg = new String("");
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus,Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for THES1_UF
        StringObject thes_UF = new StringObject();
        dbtr.getThesaurusCategory_UF(selectedThesaurus,Q,sis_session.getValue(),thes_UF);
        // looking for Descriptor prefix
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        /*
        // in case of empty UFs list, return
        if (ufs.compareTo("") == 0) {
            return errorMsg;
        }
        // get the RT values		
        String[] uf_split = ufs.split("###");
        // fill a Vector with the UFs with prefix and DB encoding		
        */
        ArrayList<StringObject> ufsVector = new ArrayList<>();
        for (int i = 0; i < ufs.size(); i++) {           
            ufsVector.add(new StringObject(prefix.concat(ufs.get(i))));
        }
        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }
        // for each UF
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        for (StringObject ufTermObj : ufsVector) {
            
            CMValue targetUfTermCmv = new CMValue();
            targetUfTermCmv.assign_node(ufTermObj.getValue(), -1, Utilities.getTransliterationString(ufTermObj.getValue(), true), TMSAPIClass.Do_Not_Assign_ReferenceId);
            
            
            // in case it doesn't exist
            if (dbGen.check_exist(ufTermObj.getValue(),Q,sis_session) == false) {
                // create it
                //StringObject dummy = new StringObject();
                //TA.GetThesaurusName( dummy);
                int ret = TA.CHECK_CreateUsedForTermCMValue(targetUfTermCmv);
                //TA.GetThesaurusName( dummy);
                //TA.GetTMS_APIErrorMessage( dummy);TA.
                if (ret == TMSAPIClass.TMS_APIFail) {
                    
                    StringObject tmpErr = new StringObject();
                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                    errorMsg = errorMsg.concat(tmpErr.getValue());
                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    return errorMsg;
                }
            } else { // UF exists		                
                // in case it is not a UsedForTerm, fill error message
                if (dbGen.NodeBelongsToClass(ufTermObj, new StringObject(selectedThesaurus + "UsedForTerm"), false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(ufTermObj.getValue());
                    
                     errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInUsedForTerms", new String[]{str}, uiLang),tms_session) + "");
                    //errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Term: " + str + " does not belong in non-preferred terms set and therefore cannot be defined as UF.",tms_session) + "");
                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    return errorMsg;
                }
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            // create the UF link
            Q.reset_name_scope();
            long sysid1L = Q.set_current_node( ufTermObj);
            CMValue to = new CMValue();
            to.assign_node(ufTermObj.getValue(), sysid1L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( thesHierarchyTerm);
            Q.set_current_node( thes_UF);
            Q.set_put( catSet);
            Q.reset_set( catSet);

            int ret;
            prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
                ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), targetDescriptor, to, catSet);
            } else {
                ret = TA.CHECK_CreateDescriptorAttribute(new StringObject(), targetDescriptor, to, catSet);
            }
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            Q.free_set( catSet);
            if (ret == TMSAPIClass.TMS_APIFail) {
                StringObject tmpErr = new StringObject();
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                errorMsg = errorMsg.concat(tmpErr.getValue());
                //errorMsg = errorMsg.concat(WTA.errorMessage.getValue());
            }
        }
        return errorMsg;
    }

    /*---------------------------------------------------------------------
    connectDewey()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String dewey: the Dewey values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type THES1_dewey from the given Descriptor with 
    the given Deweys. If any of the Deweys doesn't exist in the database, 
    it is created as instance of class DeweyNumber
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectDewey(String selectedThesaurus,StringObject targetDescriptor, String dewey,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        Utilities u = new Utilities();
        String errorMsg = new String("");
        DBThesaurusReferences dbtr = new DBThesaurusReferences();  
        String DeweyNumber = ConstantParameters.DeweyClass;
        String DeweyPrefix = dbtr.getThesaurusPrefix_DeweyNumber(Q,sis_session.getValue());
                
        StringObject dnFromClassObj = new StringObject();
        StringObject dnLinkObj      = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.dn_kwd, dnFromClassObj, dnLinkObj,Q,sis_session);
        

        // in case of empty Deweys list, return
        if (dewey.compareTo("") == 0) {
            return errorMsg;
        }

        // get the Dewey values
        String[] dewey_split = dewey.split("###");
        // fill a Vector with the Deweys with prefix and DB encoding
        ArrayList<StringObject> deweys = new ArrayList<StringObject>();
        for (int i = 0; i < dewey_split.length; i++) {
            dewey_split[i] = DeweyPrefix.concat(dewey_split[i]);
            deweys.add(new StringObject(dewey_split[i]));
        }
        // for each Dewey	
        for (int i = 0; i < deweys.size(); i++) {
            // in case it doesn't exist
            if (dbGen.check_exist(((StringObject) deweys.get(i)).getValue(),Q,sis_session) == false) {
                // create it as instance of class DeweyNumber
                Identifier id_d = new Identifier(((StringObject) deweys.get(i)).getValue());
                if (Q.CHECK_Add_Node( id_d, QClass.SIS_API_TOKEN_CLASS,true) == QClass.APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/DeweyAdditionError", new String[]{dbGen.removePrefix(((StringObject) deweys.get(i)).getValue())}, uiLang),tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, "Addition of Dewey Number: " + dbGen.removePrefix(((StringObject) deweys.get(i)).getValue()) + " failed.",tms_session) + " ");
                    continue;
                }
                Q.reset_name_scope();
                //long fromL = Q.set_current_node( (StringObject) deweys.get(i));
                //Identifier id_from = new Identifier(fromL);
                //Q.reset_name_scope();
                long toL = Q.set_current_node( new StringObject(DeweyNumber));
                Identifier id_to = new Identifier(toL);
                Q.reset_name_scope();
                //if (Q.CHECK_Add_Instance( id_from, id_to) == QClass.APIFail) {
                if (Q.CHECK_Add_Instance( id_d, id_to) == QClass.APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/DeweyAdditionError", new String[]{dbGen.removePrefix(((StringObject) deweys.get(i)).getValue())}, uiLang),tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, "Addition of Dewey Number: " + dbGen.removePrefix(((StringObject) deweys.get(i)).getValue()) + " failed.",tms_session) + " ");
                     
                    continue;
                }
            } else { // Dewey exists
                // in case it is not a DeweyNumber, fill error message
                if (dbGen.NodeBelongsToClass((StringObject) deweys.get(i), new StringObject("DeweyNumber"), false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(((StringObject) deweys.get(i)).getValue());
                    errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInDeweyNumbers", new String[]{str}, uiLang),tms_session) + "");
                    //errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Value: " + str + " does not belong in the set of Dewey Numbers and therefore cannot be defined as DN.",tms_session) + "");
                    continue;
                }
            }
            // create the THES1_dewey link
            Q.reset_name_scope();
            long sysidL = Q.set_current_node( targetDescriptor);
            Identifier from = new Identifier(sysidL);
            Q.reset_name_scope();
            long sysid2L = Q.set_current_node( (StringObject) deweys.get(i));
            CMValue to = new CMValue();
            to.assign_node(((StringObject) deweys.get(i)).getValue(), sysid2L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( dnFromClassObj);
            Q.set_current_node( dnLinkObj);
            Q.set_put( catSet);
            int ret = Q.CHECK_Add_Unnamed_Attribute( from, to, catSet);
            Q.free_set( catSet);
            if (ret == QClass.APIFail) {
                errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ConnectionError", new String[]{dbGen.removePrefix(((StringObject) deweys.get(i)).getValue())}, uiLang) ,tms_session) + " ");
                //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Error occurred while creationg link: " + dbGen.removePrefix(((StringObject) deweys.get(i)).getValue())+ " .",tms_session) + " ");
            }
        }
        return errorMsg;
    }

    /*---------------------------------------------------------------------
    connectTaxonomicCode()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String tc: the Taxonomic Code values to be added separated with "###"
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type THES1_taxonomic_code from the given Descriptor with 
    the given tcs. If any of the tc doesn't exist in the database, 
    it is created as instance of class TaxonomicCode
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectTaxonomicCodes(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> tc,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
               
        int SISapiSession = sis_session.getValue();
        String errorMsg = new String("");
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();  
        String taxCodeClass = ConstantParameters.TaxonomicCodeClass;
        String tcPrefix = dbtr.getThesaurusPrefix_TaxonomicCode(Q,sis_session.getValue());
                
        StringObject tcFromClassObj = new StringObject();
        StringObject tcLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.tc_kwd, tcFromClassObj, tcLinkObj,Q,sis_session);
        
        if (tc.isEmpty()) {
        //if (sourcesList.compareTo("") == 0) {
            return errorMsg;
        }
        // fill a Vector with the Source values (with prefix and DB format)
        //String[] src_split = sourcesList.split("###");
        ArrayList<StringObject> taxCodes = new ArrayList<>();
        
        for (int i = 0; i < tc.size(); i++) {
            String tempTaxCode = tc.get(i);
            if(tempTaxCode!=null && tempTaxCode.trim().length()>0){
                tempTaxCode = tcPrefix.concat(tempTaxCode.trim());
                taxCodes.add(new StringObject(tempTaxCode));
            }
        }

      
        // for each Dewey	
        for (int i = 0; i < taxCodes.size(); i++) {
            // in case it doesn't exist
            if (dbGen.check_exist(((StringObject) taxCodes.get(i)).getValue(),Q,sis_session) == false) {
                // create it as instance of class DeweyNumber
                Identifier id_tc = new Identifier(((StringObject) taxCodes.get(i)).getValue());
                if (Q.CHECK_Add_Node( id_tc, QClass.SIS_API_TOKEN_CLASS,true) == QClass.APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/TaxonomicalCodeAdditionError", new String[]{dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue())}, uiLang),tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, "Addition of Taxonomical Code: " + dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue())  + " failed.",tms_session) + " ");
                    
                    continue;
                }
                Q.reset_name_scope();
                //long fromL = Q.set_current_node( (StringObject) taxCodes.get(i));
                //Identifier id_from = new Identifier(fromL);
                //Q.reset_name_scope();
                long toL = Q.set_current_node( new StringObject(taxCodeClass));
                Identifier id_to = new Identifier(toL);
                Q.reset_name_scope();
                //if (Q.CHECK_Add_Instance( id_from, id_to) == QClass.APIFail) {
                if (Q.CHECK_Add_Instance( id_tc, id_to) == QClass.APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/TaxonomicalCodeAdditionError", new String[]{dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue())}, uiLang),tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, "Addition of Taxonomical Code: " + dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue())  + " failed.",tms_session) + " ");
                    continue;
                }
            } else { // Dewey exists
                // in case it is not a DeweyNumber, fill error message
                if (dbGen.NodeBelongsToClass((StringObject) taxCodes.get(i), new StringObject(taxCodeClass), false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue());
                    errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInTaxonomicalCodes", new String[]{str}, uiLang),tms_session) + "");
                    //errorMsg = errorMsg.concat("" + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Value: " + str + " does not belong in the set of Taxonomical Codes and therefore cannot be defined as TC.",tms_session) + "");                    
                    continue;
                }
            }
            // create the THES1_dewey link
            Q.reset_name_scope();
            long sysidL = Q.set_current_node( targetDescriptor);
            Identifier from = new Identifier(sysidL);
            Q.reset_name_scope();
            long sysid2L = Q.set_current_node( (StringObject) taxCodes.get(i));
            CMValue to = new CMValue();
            to.assign_node(((StringObject) taxCodes.get(i)).getValue(), sysid2L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( tcFromClassObj);
            Q.set_current_node( tcLinkObj);
            Q.set_put( catSet);
            int ret = Q.CHECK_Add_Unnamed_Attribute( from, to, catSet);
            Q.free_set( catSet);
            if (ret == QClass.APIFail) {
                errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/ConnectionError", new String[]{ dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue())}, uiLang ),tms_session) + " ");
                //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,"Error occurred while creationg link: " + dbGen.removePrefix(((StringObject) taxCodes.get(i)).getValue()) + " .",tms_session) + " ");
            }
        }
        return errorMsg;
    }
    
    
    
    /*---------------------------------------------------------------------
    connectSources()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String sourcesList: the Source values to be added separated with "###"
    - int sourceCategory: CATEGORY_BT_FOUND_IN / CATEGORY_translations_found_in / CATEGORY_PRIMARY_FOUND_IN
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type:
    sourceCategory == CATEGORY_BT_FOUND_IN => thes1_bt_found_in
    sourceCategory == CATEGORY_translations_found_in => thes1_translations_found_in
    sourceCategory == CATEGORY_PRIMARY_FOUND_IN => thes1_primary_found_in
    from the given Descriptor with the given Sources. If any of the Sources
    doesn't exist in the database, it is created with the TMS function CreateSource()
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectSources(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> sourcesList, int sourceCategory,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        String errorMsg = new String("");
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Utilities u = new Utilities();
        // looking for Source prefix ("Literature`")
        String prefix = dbtr.getThesaurusPrefix_Source(Q,sis_session.getValue());
        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus,Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for thes1_bt_found_in, thes1_translations_found_in, thes1_primary_found_in
        StringObject bt_found_in = new StringObject();
        StringObject translations_found_in = new StringObject();
        StringObject primary_found_in = new StringObject();
        dbtr.getThesaurusCategory_bt_found_in(selectedThesaurus,bt_found_in);
        dbtr.getThesaurusCategory_primary_found_in(selectedThesaurus,primary_found_in);
        dbtr.getThesaurusCategory_translations_found_in(selectedThesaurus,translations_found_in);
        
        // in case of empty Sources list, return
        if (sourcesList.size() == 0) {
        //if (sourcesList.compareTo("") == 0) {
            return errorMsg;
        }
        // fill a Vector with the Source values (with prefix and DB format)
        //String[] src_split = sourcesList.split("###");
        ArrayList<StringObject> sources = new ArrayList<StringObject>();
        
        for (int i = 0; i < sourcesList.size(); i++) {
            String tempSource = sourcesList.get(i);
            if(tempSource!=null && tempSource.trim().length()>0){
                tempSource = prefix.concat(tempSource.trim());
                sources.add(new StringObject(tempSource));
            }
        }
        // for each Source value	
        for (StringObject sourceObj : sources) {
            
            CMValue targetSourceCmv = new CMValue();
            targetSourceCmv.assign_node(sourceObj.getValue(), -1, Utilities.getTransliterationString(sourceObj.getValue(), true), TMSAPIClass.Do_Not_Assign_ReferenceId);
                    
            // if it doesn't exist with TMSAPI
            if (dbGen.check_exist(sourceObj.getValue(),Q,sis_session) == false) {
                // create it
                int ret = TA.CHECK_CreateSourceCMValue(targetSourceCmv);
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(ret, TA,null,tms_session) + " ");
                    continue;
                }
            } else { // already exists
                // check it is an instance of class Source
                if (dbGen.NodeBelongsToClass(sourceObj, new StringObject("Source"), false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(sourceObj.getValue());
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInSources", new String[]{str}, uiLang),tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail, TA,"Value: " + str + " does not belong in the set Sources and therefore cannot be used as primary or translations source.",tms_session) + " ");
                    continue;
                }
            }

            // create from targetDescriptor to current Source the relation of type:
            // sourceCategory == CATEGORY_BT_FOUND_IN => thes1_bt_found_in
            // sourceCategory == CATEGORY_translations_found_in => thes1_translations_found_in
            // sourceCategory == CATEGORY_PRIMARY_FOUND_IN => thes1_primary_found_in
            Q.reset_name_scope();
            long sysidL = Q.set_current_node( targetDescriptor);
            Identifier from = new Identifier(sysidL);
            Q.reset_name_scope();
            long sysid2L = Q.set_current_node( sourceObj);
            CMValue to = new CMValue();
            to.assign_node(sourceObj.getValue(), sysid2L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            long sysid3L = Q.set_current_node( thesHierarchyTerm);
            long sysid4L;
            if (sourceCategory == CATEGORY_BT_FOUND_IN) {
                sysid4L = Q.set_current_node( bt_found_in);
            } else if (sourceCategory == CATEGORY_translations_found_in) {
                sysid4L = Q.set_current_node( translations_found_in);
            } else { // CATEGORY_PRIMARY_FOUND_IN
                sysid4L = Q.set_current_node( primary_found_in);
            }
            Q.set_put( catSet);
            int ret = Q.CHECK_Add_Unnamed_Attribute( from, to, catSet);
            Q.free_set( catSet);
            if (ret == QClass.APIFail) {
                errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ConnectionError", new String[]{ dbGen.removePrefix(sourceObj.getValue())}, uiLang) ,tms_session) + " ");
                //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Error occurred while creationg link: " + dbGen.removePrefix(((StringObject) sources.get(i)).getValue()) + " .",tms_session) + " ");
            }
        } // for each Source value

        return errorMsg;
    }


    /*---------------------------------------------------------------------
    connectEnglishWords()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String engWordList: the EnglishWord values to be added separated with "###"
    - int englishWordCategory: CATEGORY_THES_UK_ALT / CATEGORY_THES_UK_UF
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: creates relations of the type:
    englishWordCategory == CATEGORY_THES_UK_ALT => THES1_uk_alt
    englishWordCategory == CATEGORY_THES_UK_UF => THES1_uk_uf
    from the given Descriptor with the given EnglishWords. If any of the EnglishWords
    doesn't exist in the database, it is created with the TMS function CreateEnglishWord()
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/
    public String connectEnglishWords(String selectedThesaurus,StringObject targetDescriptor, ArrayList<String> engWordList, int englishWordCategory,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        int TMSapiSession = tms_session.getValue();
        String errorMsg = new String("");
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Utilities u = new Utilities();

        // in case of empty engWordList, return	
        if(engWordList.size()==0){
        //if (engWordList.compareTo("") == 0) {
            return ("");
        }
        // fill a Vector with the EnglishWord values (with prefix and DB format)	
        //String[] eng_split = engWordList.split("###");
        ArrayList<StringObject> words = new ArrayList<StringObject>();
        String e_prefix = dbtr.getThesaurusPrefix_EnglishWord(Q,sis_session.getValue());
        for (int i = 0; i < engWordList.size(); i++) {
            String tempWord = engWordList.get(i);
            if(tempWord!=null && tempWord.trim().length()>0){
                tempWord=e_prefix.concat(tempWord.trim());
                words.add(new StringObject(tempWord));
            }
        }

        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus,Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for category THES1_uk_alt/uf
        StringObject thes_uk_category = new StringObject();
        if (englishWordCategory == CATEGORY_THES_UK_ALT) {
            thes_uk_category.setValue(selectedThesaurus.concat("_uk_alt"));
        } else { // CATEGORY_THES_UK_UF
            thes_uk_category.setValue(selectedThesaurus.concat("_uk_uf"));
        }
        // for each EnglishWord value	                
        for (int i = 0; i < words.size(); i++) {
            // check if current EnglishWord is the same with target descriptor
            if (targetDescriptor.getValue().compareTo(dbGen.removePrefix(((StringObject) words.get(i)).getValue())) == 0) {
                errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/CannotCreateRelationFromAndToTheSameTerm",null, uiLang),tms_session) + " ");
                //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "The creation of relation from and to the same term is prohibitied.",tms_session) + " ");
                return errorMsg;
            }
            // if it doesn't exist with TMSAPI
            if (dbGen.check_exist(((StringObject) words.get(i)).getValue(),Q,sis_session) == false) {
                int ret = TA.CHECK_CreateTranslationWord((StringObject) words.get(i), new StringObject("EnglishWord"));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(ret, TA, null,tms_session) + " ");
                    return errorMsg;
                }
            } else { // already exists
                // check it is an instance of class EnglishWord //already checked by consistency check 18
                // EnglishWord class is not thesauric and is the same as uk_uf
                //String str = dbGen.removePrefix(((StringObject) words.get(i)).getValue());
                if (dbGen.NodeBelongsToClass((StringObject) words.get(i), new StringObject("EnglishWord"), false,Q,sis_session) == false) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInTranslations", new String[]{((StringObject) words.get(i)).getValue()}, uiLang) ,tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Value: " + targetWord+" does not belong in the set of Translation terms and therefore cannot be defined as Translation.",tms_session) + " ");
                    return errorMsg;
                }
            }
            // create from targetDescriptor to current EnglishWord the relation of type:
            // THES1HierarchyTerm->THES1_uk_alt
            Q.reset_name_scope();
            long sysidL = Q.set_current_node( targetDescriptor);
            Identifier from = new Identifier(sysidL);
            Q.reset_name_scope();
            long sysid2L = Q.set_current_node( (StringObject) words.get(i));
            CMValue to = new CMValue();
            to.assign_node(((StringObject) words.get(i)).getValue(), sysid2L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            long sysid3L = Q.set_current_node( thesHierarchyTerm);
            long sysid4L = Q.set_current_node( thes_uk_category);
            Q.set_put( catSet);
            int ret = Q.CHECK_Add_Unnamed_Attribute( from, to, catSet);
            Q.free_set( catSet);
            if (ret == QClass.APIFail) {
                errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ConnectionError", new String[]{dbGen.removePrefix(((StringObject) words.get(i)).getValue())}, uiLang) ,tms_session) + " ");
                //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA,"Error occurred while creationg link: " + dbGen.removePrefix(((StringObject) words.get(i)).getValue()),tms_session) + " ." + " ");
            }
        } // for each EnglishWord value

        return errorMsg;
    }    

    /*---------------------------------------------------------------------
    AddComment()
    -----------------------------------------------------------------------
    INPUT: - String targetDescriptor: the target Descriptor
    - String commentString: the commentString value to be added
    - int commentCategory: CATEGORY_THES_COMMENT / CATEGORY_THES_SCOPENOTE
    OUTPUT: - String errorMsg: an error description (if any), "" otherwise
    FUNCTION: adds a comment of the type:
    commentCategory == CATEGORY_THES_COMMENT => THES1ThesaurusConcept->thes1_comment
    commentCategory == CATEGORY_THES_SCOPENOTE => THES1ThesaurusConcept->thes1_scope_note
    commentCategory == COMMENT_CATEGORY_HISTORICALNOTE => THES1ThesaurusConcept->thes1_historical_note
    to the given Descriptor 
    CALLED BY: the creation / modification of a Descriptor
    ----------------------------------------------------------------------*/

    public String AddComment(String selectedThesaurus,
            StringObject targetDescriptor, 
            String commentString, 
            int commentCategory,
            QClass Q, 
            TMSAPIClass TA, 
            IntegerObject sis_session) {

        String errorMsg = new String("");
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // in case of empty engWordList, return	
        if (commentString.compareTo("") == 0) {
            return ("");
        }
        // looking for THES1ThesaurusConcept 
        StringObject thesThesaurusConcept = new StringObject();
        dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus,Q,sis_session.getValue(),thesThesaurusConcept);

        StringObject commentCategoryStrObj = new StringObject();
        switch (commentCategory) {
            case COMMENT_CATEGORY_COMMENT:
                dbtr.getThesaurusCategory_comment(selectedThesaurus,Q,sis_session.getValue(),commentCategoryStrObj);
                break;
            case COMMENT_CATEGORY_SCOPENOTE:
                dbtr.getThesaurusCategory_scope_note(selectedThesaurus,Q,sis_session.getValue(),commentCategoryStrObj);
                break;
            case COMMENT_CATEGORY_HISTORICALNOTE:
                dbtr.getThesaurusCategory_historical_note(selectedThesaurus,Q,sis_session.getValue(),commentCategoryStrObj);
                break;
            case COMMENT_CATEGORY_SCOPENOTE_TR:
                dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus,Q,sis_session.getValue(),commentCategoryStrObj);
                break;                
            case COMMENT_CATEGORY_NOTE:
                dbtr.getThesaurusCategory_note(selectedThesaurus,Q,sis_session.getValue(),commentCategoryStrObj);
                break;
            default:
                break;
        }
        
        
         //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }
        
        int ret = TA.SetDescriptorComment(targetDescriptor, new StringObject(commentString), thesThesaurusConcept, commentCategoryStrObj);
         //int ret = TA.SetDescriptorComment(TMSapiSession, targetDescriptor, new StringObject(commentString), thesThesaurusConcept, commentCategoryStrObj);
         //reset to previous thesaurus name if needed
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(prevThes.getValue());
        }

        if (ret == TMSAPIClass.TMS_APIFail) {
            StringObject tmpErr = new StringObject();
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
            errorMsg = errorMsg.concat(tmpErr.getValue());
            //errorMsg += dbGen.check_success(ret, null);
        }

        return errorMsg;
    }

    public String connectAlts(String selectedThesaurus,StringObject targetDescriptor, String alts, String prefix,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {

        String errorMsg = new String("");
        int SisSessionId = sis_session.getValue();
        Utilities u = new Utilities();

        StringObject thesHierarchyTerm = new StringObject(selectedThesaurus + "HierarchyTerm");
        StringObject thes_ALT = new StringObject(selectedThesaurus + "_ALT");

        // in case of empty alts list, return
        if (alts.compareTo("") == 0) {
            return errorMsg;
        }
        // get the alt values	
        String[] alts_split = alts.split(",");
        // fill a Vector with the alt with prefix and DB encoding	
        ArrayList<StringObject> alts_Vector = new ArrayList<StringObject>();

        for (int i = 0; i < alts_split.length; i++) {
            String tempSTR1 = new String(alts_split[i].trim());
            if (tempSTR1.length() > 0) {

                StringObject tempSTR1Obj = new StringObject(prefix.concat(tempSTR1));
                if (alts_Vector.contains(tempSTR1Obj) == false) {

                    alts_Vector.add(tempSTR1Obj);
                }
            }
        }
        // for each ALT check 

        for (int i = 0; i < alts_Vector.size(); i++) {
            // in case it doesn't exist
            if (dbGen.check_exist(((StringObject) alts_Vector.get(i)).getValue(),Q,sis_session) == false) {
                // create Alternative Term
                int ret = TA.CreateAlternativeTerm( (StringObject) alts_Vector.get(i));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("\n" + dbGen.check_success(ret,TA, null,tms_session) + "\n");
                    continue;
                }
            } else { // Name exists   
                //Check if it is used as an Alternative Term 
                DBThesaurusReferences dbtr = new DBThesaurusReferences(); 
                StringObject altClassObj = new StringObject();
                dbtr.getThesaurusClass_AlternativeTerm(selectedThesaurus,Q,sis_session.getValue(),altClassObj);
                
                if (dbGen.NodeBelongsToClass((StringObject) alts_Vector.get(i), altClassObj , false,Q,sis_session) == false) {
                    String str = dbGen.removePrefix(((StringObject) alts_Vector.get(i)).getValue());
                    //ValueNotInAlternativeTerms
                    errorMsg = errorMsg.concat(u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInAlternativeTerms", new String[]{str}, uiLang));
                    //errorMsg = errorMsg.concat("Value: '" + str + "' does not belong in the set of Alternative Terms and therefore cannot be defined as ALT.");
                    continue;
                }
            }

            // create the ALT link
            Q.reset_name_scope();
            long sysid1L = Q.set_current_node((StringObject) alts_Vector.get(i));
            CMValue to = new CMValue();
            to.assign_node(((StringObject) alts_Vector.get(i)).getValue(), sysid1L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( thesHierarchyTerm);
            Q.set_current_node( thes_ALT);
            Q.set_put( catSet);

            int ret;
            if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
                ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            } else {
                ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            }
            Q.free_set( catSet);
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat(dbGen.check_success(ret, TA, null,tms_session));
            }
        }
        return errorMsg;

    }

    public String connectTranslation(String selectedThesaurus, StringObject targetDescriptor, ArrayList<String> normalizedTranslations,QClass Q, IntegerObject sis_session,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        StringObject fromClass = new StringObject();
        StringObject link = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translation_kwd, fromClass, link,Q,sis_session);

        Q.reset_name_scope();
        int ret = TMSAPIClass.TMS_APISucc;
        String errorMsg = new String("");
        int SISapiSession = sis_session.getValue();
        
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);



        
        if(normalizedTranslations.size()==0){
            return errorMsg;
        }

        ArrayList<String> translationsVector = new ArrayList<String>();
        for(int i=0 ; i<normalizedTranslations.size(); i++ ){

            String normalizedValue = normalizedTranslations.get(i); //i.e EN:some val
            String wordPrefix = normalizedValue.substring(0, normalizedValue.indexOf(Parameters.TRANSLATION_SEPERATOR)) + ConstantParameters.languageIdentifierSuffix;
            String word       = normalizedValue.substring(normalizedValue.indexOf(Parameters.TRANSLATION_SEPERATOR) + Parameters.TRANSLATION_SEPERATOR.length());
            translationsVector.add(wordPrefix+word);
        }


        
        HashMap<String,String> languagesIDs2Words = dbGen.getThesaurusTranslationCategories(Q, TA, sis_session, selectedThesaurus, null, false,false);

        // for each translation
        for (int i = 0; i < translationsVector.size(); i++) {
            
            CMValue targetWordCmv = new CMValue();
            targetWordCmv.assign_node(translationsVector.get(i), -1, Utilities.getTransliterationString(translationsVector.get(i), true), TMSAPIClass.Do_Not_Assign_ReferenceId);
            
            String prefix = targetWordCmv.getString().substring(0,targetWordCmv.getString().indexOf(ConstantParameters.languageIdentifierSuffix));
            String targetWordClass =languagesIDs2Words.get(prefix)+ConstantParameters.wordClass;
            String targetSubTranslationCategory =selectedThesaurus + ConstantParameters.thesaursTranslationCategorysubString + prefix;
            

            // in case it doesn't exist
            if (dbGen.check_exist(targetWordCmv.getString(),Q,sis_session) == false) {
                // create it as orphan
                ret = TA.CHECK_CreateTranslationWordCMValue(targetWordCmv, new StringObject(targetWordClass));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("\n" + dbGen.check_success(ret,TA, null,tms_session) + "\n");
                    return errorMsg;
                }
            } else { 
                
                //consistency check 26 is supposed to be applied prior to this call
                if (dbGen.NodeBelongsToClass(new StringObject(targetWordCmv.getString()), new StringObject(targetWordClass), false,Q,sis_session) == false) {
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInTranslations", new String[]{targetWordCmv.getString()}, uiLang) ,tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Value: " + targetWord+" does not belong in the set of Translation terms and therefore cannot be defined as Translation.",tms_session) + " ");
                    return errorMsg;
                }
            }


            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat(dbGen.check_success(ret,TA, null,tms_session));
            }
            // create the translation links
            Q.reset_name_scope();
            long sysid1L = Q.set_current_node(new StringObject(targetWordCmv.getString()));
            CMValue to = new CMValue();
            to.assign_node(targetWordCmv.getString(), sysid1L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( fromClass);
            Q.set_current_node( new StringObject(targetSubTranslationCategory));
            Q.set_put( catSet);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }

            //RELEASED DESCRIPTORS NOT SUPPORTED YET
            if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
                ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            } else {
                ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            }
            
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            if (ret == TMSAPIClass.TMS_APIFail) {
                StringObject tmpErr = new StringObject();
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);                        
                return tmpErr.getValue();
            }
            Q.free_set( catSet);
            
        }           

        return "";

    }

    public String connectUFTranslation(String selectedThesaurus, StringObject targetDescriptor, ArrayList<String> normalizedTranslations,QClass Q, IntegerObject sis_session,TMSAPIClass TA, IntegerObject tms_session, final String uiLang) {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        StringObject fromClass = new StringObject();
        StringObject link = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.uf_translations_kwd, fromClass, link,Q,sis_session);

        Q.reset_name_scope();
        int ret = TMSAPIClass.TMS_APISucc;
        String errorMsg = new String("");
        int SISapiSession = sis_session.getValue();

        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

        if(normalizedTranslations.size()==0){
            return errorMsg;
        }

        ArrayList<String> translationsVector = new ArrayList<String>();
        for(int i=0 ; i<normalizedTranslations.size(); i++ ){

            String normalizedValue = normalizedTranslations.get(i); //i.e EN:some val
            String wordPrefix = normalizedValue.substring(0, normalizedValue.indexOf(Parameters.TRANSLATION_SEPERATOR)) + ConstantParameters.languageIdentifierSuffix;
            String word       = normalizedValue.substring(normalizedValue.indexOf(Parameters.TRANSLATION_SEPERATOR) + Parameters.TRANSLATION_SEPERATOR.length());
            translationsVector.add(wordPrefix+word);
        }



        HashMap<String,String> languagesIDs2Words = dbGen.getThesaurusTranslationCategories(Q,TA, sis_session, selectedThesaurus, null, false,false);

        // for each translation
        for (int i = 0; i < translationsVector.size(); i++) {

            CMValue targetWordCmv = new CMValue();
            targetWordCmv.assign_node(translationsVector.get(i), -1, Utilities.getTransliterationString(translationsVector.get(i), true), TMSAPIClass.Do_Not_Assign_ReferenceId);
            String prefix = targetWordCmv.getString().substring(0,targetWordCmv.getString().indexOf(ConstantParameters.languageIdentifierSuffix));
            String targetWordClass =languagesIDs2Words.get(prefix)+ConstantParameters.wordClass;
            String targetSubTranslationCategory =selectedThesaurus + ConstantParameters.thesaursUFTranslationCategorysubString + prefix;


            // in case it doesn't exist
            if (dbGen.check_exist(targetWordCmv.getString(),Q,sis_session) == false) {
                // create it as orphan
                ret = TA.CHECK_CreateTranslationWordCMValue(targetWordCmv, new StringObject(targetWordClass));
                if (ret == TMSAPIClass.TMS_APIFail) {
                    errorMsg = errorMsg.concat("\n" + dbGen.check_success(ret,TA, null,tms_session) + "\n");
                    return errorMsg;
                }
            } else {

                //consistency check 26 is supposed to be applied prior to this call
                if (dbGen.NodeBelongsToClass(new StringObject(targetWordCmv.getString()), new StringObject(targetWordClass), false,Q,sis_session) == false) {
                    
                    errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, u.translateFromMessagesXML("root/EditTerm/Edit/ValueNotInUFTranslations", new String[]{targetWordCmv.getString()}, uiLang) ,tms_session) + " ");
                    //errorMsg = errorMsg.concat(" " + dbGen.check_success(TMSAPIClass.TMS_APIFail,TA, "Value: " + targetWord+"  does not belong in the set of non preferred Translation terms and therefore cannot be defined as translation UF.",tms_session) + " ");
                    return errorMsg;
                }
            }


            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat(dbGen.check_success(ret, TA, null,tms_session));
            }
            // create the translation links
            Q.reset_name_scope();
            long sysid1L = Q.set_current_node(new StringObject(targetWordCmv.getString()));
            CMValue to = new CMValue();
            to.assign_node(targetWordCmv.getString(), sysid1L);
            int catSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node( fromClass);
            Q.set_current_node( new StringObject(targetSubTranslationCategory));
            Q.set_put( catSet);


            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            //RELEASED DESCRIPTORS NOT SUPPORTED YET
            if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
                ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            } else {
                ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            if (ret == TMSAPIClass.TMS_APIFail) {
                StringObject tmpErr = new StringObject();
                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                return tmpErr.getValue();
            }
            Q.free_set( catSet);

        }

        return "";

    }


    public String connectEditor(String selectedThesaurus,StringObject targetDescriptor, String user, String FromClass, String Link,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session) {
        
        int ret = TMSAPIClass.TMS_APISucc;
        String errorMsg = new String("");
        int SisSessionId = sis_session.getValue();
 
        // looking for THES1Term 
        StringObject fromClass = new StringObject(FromClass);
        StringObject link = new StringObject(Link);

        //if empty return
        if (user.compareTo("") == 0) {
            return errorMsg;
        }
        
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);

        // in case it doesn't exist
        if (dbGen.check_exist(user,Q,sis_session) == false) {
            // create it as orphan
            ret = TA.CreateEditor( new StringObject(user));
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat("\n" + dbGen.check_success(ret,TA, null,tms_session) + "\n");
                return errorMsg;
            }
        }
        //if(not instance of dbtr.getEditor then instansiate)
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject EditorClass = new StringObject();
        StringObject userObj = new StringObject(user);
        dbtr.getThesaurusClass_Editor(selectedThesaurus, Q, SisSessionId, EditorClass);
        
        if(dbGen.NodeBelongsToClass(userObj, EditorClass, false, Q, sis_session)==false){
            Q.reset_name_scope();
            //Q.set_current_node(EditorClass);
            Identifier fromIdentifier =new Identifier(userObj.getValue());
            Identifier toIdentifier =  new Identifier(EditorClass.getValue());
            ret = Q.CHECK_Add_Instance(fromIdentifier, toIdentifier);
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMsg = errorMsg.concat("\n" + dbGen.check_success(ret, TA, null,tms_session) + "\n");
                return errorMsg;
            }
        }

        
        

        ret = TMSAPIClass.TMS_APISucc;
        // create the ALT link
        Q.reset_name_scope();
        long sysid1L = Q.set_current_node( userObj );
        CMValue to = new CMValue();
        to.assign_node(user, sysid1L);
        int catSet = Q.set_get_new();
        Q.reset_name_scope();
        Q.set_current_node( fromClass);
        Q.set_current_node( link);
        Q.set_put( catSet);

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }
        if (dbGen.IsReleasedDescriptor(selectedThesaurus, targetDescriptor,Q,sis_session) == false) {
            ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
        } else {
            ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
        }
        //reset to previous thesaurus name if needed
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(prevThes.getValue());
        }
        
        Q.free_set( catSet);
        if (ret == TMSAPIClass.TMS_APIFail) {
            StringObject tmpErr = new StringObject();
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
            errorMsg = errorMsg.concat(tmpErr.getValue());
        }

        return errorMsg;
    }
    
    public String connectTime(String selectedThesaurus,StringObject targetDescriptor, String FromClass, String Link,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session) {
          
        int ret = TMSAPIClass.TMS_APISucc;
        Utilities u = new Utilities();
        String errorMsg = new String("");
        int SisSessionId = sis_session.getValue();

        StringObject fromClass = new StringObject(FromClass);
        StringObject link = new StringObject(Link);

        Q.reset_name_scope();

        String currDate = u.GetNowNodeName();

     
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
      
        // in case it doesn't exist
        if (dbGen.check_exist(currDate,Q,sis_session) == false) {
            // create it as orphan

            Identifier I_c = new Identifier(currDate);
            //I_c.setValue();
            ret = Q.CHECK_Add_Node(I_c, QClass.SIS_API_TOKEN_CLASS,true);// tms_session.getValue(), new StringObject(user));
            if (ret == QClass.APIFail) {
                StringObject message = new StringObject("");
                Q.get_error_message(message);
                errorMsg = errorMsg.concat(message.getValue());
                return errorMsg;
            }
            // instantiate it under class Date
            Identifier I_Date = new Identifier("Date");
            ret = Q.CHECK_Add_Instance(I_c, I_Date);
            if (ret == QClass.APIFail) {
                StringObject message = new StringObject("");
                Q.get_error_message(message);
                errorMsg = errorMsg.concat(message.getValue());
                return errorMsg;
            }
        }
        
        ret = TMSAPIClass.TMS_APISucc;
        Q.reset_name_scope();
        long sysid1L = Q.set_current_node( new StringObject(currDate));
        CMValue to = new CMValue();

        to.assign_node(currDate, sysid1L);
        int catSet = Q.set_get_new();
        Q.reset_name_scope();
        Q.set_current_node( fromClass);
        Q.set_current_node( link);
        Q.set_put( catSet);

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }
        if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
            ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), targetDescriptor, to, catSet);
        } else {
            ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
        }
        //reset to previous thesaurus name if needed
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(prevThes.getValue());
        }
        Q.free_set( catSet);
        if (ret == TMSAPIClass.TMS_APIFail) {
            StringObject tmpErr = new StringObject();
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
            errorMsg = errorMsg.concat(tmpErr.getValue());
        }

        return errorMsg;
    }
      
    public String connectSpecificTime(String selectedThesaurus,StringObject targetDescriptor,String timeValue, String FromClass, String Link,QClass Q, IntegerObject sis_session,DBGeneral dbGen,TMSAPIClass TA, IntegerObject tms_session) {
          
        int ret = TMSAPIClass.TMS_APISucc;
        Utilities u = new Utilities();
        String errorMsg = new String("");
        int SisSessionId = sis_session.getValue();

        StringObject fromClass = new StringObject(FromClass);
        StringObject link = new StringObject(Link);

        Q.reset_name_scope();

        String currDate = timeValue;

     
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
      
        // in case it doesn't exist
        if (dbGen.check_exist(currDate,Q,sis_session) == false) {
            // create it as orphan

            Identifier I_c = new Identifier(currDate);
            //I_c.setValue();
            ret = Q.CHECK_Add_Node(I_c, QClass.SIS_API_TOKEN_CLASS,true);// tms_session.getValue(), new StringObject(user));
            if (ret == QClass.APIFail) {
                StringObject message = new StringObject("");
                Q.get_error_message(message);
                errorMsg = errorMsg.concat(message.getValue());
                return errorMsg;
            }
            // instantiate it under class Date
            Identifier I_Date = new Identifier("Date");
            ret = Q.CHECK_Add_Instance(I_c, I_Date);
            if (ret == QClass.APIFail) {
                StringObject message = new StringObject("");
                Q.get_error_message(message);
                errorMsg = errorMsg.concat(message.getValue());
                return errorMsg;
            }
        }
        
        ret = TMSAPIClass.TMS_APISucc;
        Q.reset_name_scope();
        long sysid1L = Q.set_current_node( new StringObject(currDate));
        CMValue to = new CMValue();

        to.assign_node(currDate, sysid1L);
        int catSet = Q.set_get_new();
        Q.reset_name_scope();
        Q.set_current_node( fromClass);
        Q.set_current_node( link);
        Q.set_put( catSet);

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }
        if (dbGen.IsReleasedDescriptor(selectedThesaurus,targetDescriptor,Q,sis_session) == false) {
            ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
        } else {
            ret = TA.CHECK_CreateDescriptorAttribute( new StringObject(), targetDescriptor, to, catSet);
        }
        //reset to previous thesaurus name if needed
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(prevThes.getValue());
        }
        Q.free_set( catSet);
        if (ret == TMSAPIClass.TMS_APIFail) {
            StringObject tmpErr = new StringObject();
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
             errorMsg = errorMsg.concat(tmpErr.getValue());
        }

        return errorMsg;
    }
    
    public int delete_term_links_by_category(String selectedThesaurus, 
            String targetDescriptor, 
            int direction, 
            String fromClass, 
            String link, 
            int KindOfDescriptor, 
            QClass Q, 
            TMSAPIClass TA, 
            IntegerObject sis_session,
            DBGeneral dbGen,
            StringObject errorMsg) {

        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        int ret = TMSAPIClass.TMS_APISucc;
        //Q.reset_error_message(sis_session.getValue());
        Q.reset_name_scope();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        if (termPrefix == null) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"errorDetected in term " + targetDescriptor);
        }
        Q.reset_name_scope();
        // prepare input parameters: add prefix and convert to DB encoding
        StringObject targetDescriptorObj = new StringObject(termPrefix.concat(targetDescriptor));


        StringObject fromClassObj = new StringObject(fromClass);
        StringObject linkObj = new StringObject(link);
        Q.reset_name_scope();

        ArrayList<Long> deleteIDsL = new ArrayList<Long>();

        //StringObject label = new StringObject();
        //IntegerObject sysid = new IntegerObject();
        //StringObject sclass = new StringObject();   // dummy


        if (Q.set_current_node(targetDescriptorObj) != QClass.APIFail) {
            if (direction == ConstantParameters.TO_Direction) {

                //must collect all Nodes Names with relation RT pointing to targetDescriptorObj in newTargets
                //during this session we also collect the RTs labels sysids pointing to targetDescriptorObj in deleteDatesIDs
                ArrayList<String> newTargets = new ArrayList<String>();
                int selected_category_nodes = Q.get_link_to_by_category(0, fromClassObj, linkObj);
                Q.reset_set(selected_category_nodes);

                if (Q.set_get_card(selected_category_nodes) > 0) {
                    
                    ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                    if(Q.bulk_return_nodes(selected_category_nodes, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                deleteIDsL.add(row.get_Neo4j_NodeId());
                            }
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(selected_category_nodes, sysid, label, sclass) != QClass.APIFail) {
                        if (!deleteIDs.contains(sysid.getValue())) {
                            deleteIDs.add(sysid.getValue());
                        }
                    }
                    */
                }

                Q.reset_name_scope();
                int select_category_values = Q.get_from_value(selected_category_nodes);
                Q.reset_set(select_category_values);


                if (Q.set_get_card(select_category_values) > 0) {

                    ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                    if(Q.bulk_return_nodes(select_category_values, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!newTargets.contains(row.get_v1_cls_logicalname())) {
                                newTargets.add(row.get_v1_cls_logicalname());
                            }
                        }
                    }
                    /*
                    while (Q.retur_nodes(select_category_values, label) != QClass.APIFail) {
                        if (!newTargets.contains(label.getValue())) {
                            newTargets.add(label.getValue());
                        }
                    }*/
                }

                Q.free_set(select_category_values);
                Q.free_set(selected_category_nodes);

                newTargets.trimToSize();
                deleteIDsL.trimToSize();

                //for each of these nodes pointing through RT to targetDescriptorObj 
                //gather their from links to targetDescriptorObj in deletenewTargetIDs
                for (int k = 0; k < newTargets.size(); k++) {
                    /*
                    if(filterDelete){
                    String convertedName = dbGen.removePrefix(newTargets.get(k)).trim() ;
                    if(escapeDeletion.contains(convertedName))
                    continue;                    
                    }*/

                    Q.reset_name_scope();
                    Q.set_current_node(new StringObject(newTargets.get(k)));

                    ArrayList<Long> deletenewTargetIDsL = new ArrayList<Long>();
                    int selected_new_target_category_nodes = Q.get_link_from_by_category(0, fromClassObj, linkObj);
                    Q.reset_set(selected_new_target_category_nodes);
                    if (Q.set_get_card(selected_new_target_category_nodes) > 0) {

                        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                        if(Q.bulk_return_nodes(selected_new_target_category_nodes, retVals)!=QClass.APIFail){
                            for(Return_Nodes_Row row:retVals){
                                if (!deletenewTargetIDsL.contains(row.get_Neo4j_NodeId()) && deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                    deletenewTargetIDsL.add(row.get_Neo4j_NodeId());
                                }
                            }
                        }
                        /*
                        while (Q.retur_full_nodes(selected_new_target_category_nodes, sysid, label, sclass) != QClass.APIFail) {
                            if (!deletenewTargetIDsL.contains(sysid.getValue()) && deleteIDs.contains(sysid.getValue())) {
                                deletenewTargetIDsL.add(sysid.getValue());
                            }
                        }
                        */
                    }
                    Q.free_set(selected_new_target_category_nodes);
                    deletenewTargetIDsL.trimToSize();
                    if (deletenewTargetIDsL.size() > 0) {

                        int KindOfDescr = dbGen.GetKindOfDescriptor(selectedThesaurus,new StringObject(newTargets.get(k)),Q,sis_session);

                        if (KindOfDescr == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {


                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(selectedThesaurus)==false){
                                TA.SetThesaurusName(selectedThesaurus);
                            }
                            
                            for (int i = 0; i < deletenewTargetIDsL.size(); i++) {
                                //ret = WTA.DeleteNewDescriptorAttribute(selectedThesaurus,deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                                ret = TA.CHECK_DeleteNewDescriptorAttribute(deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                                if (ret == TMSAPIClass.TMS_APIFail) {
                                    
                                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    return TMSAPIClass.TMS_APIFail;
                                }
                            }
                            if(prevThes.getValue().equals(selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }


                        } else {

                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if(prevThes.getValue().equals(selectedThesaurus)==false){
                                TA.SetThesaurusName(selectedThesaurus);
                            }
                            for (int i = 0; i < deletenewTargetIDsL.size(); i++) {
                                //ret = WTA.DeleteDescriptorAttribute(selectedThesaurus,deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                                ret = TA.CHECK_DeleteDescriptorAttribute(deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                                if (ret == TMSAPIClass.TMS_APIFail) {

                                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                                        TA.SetThesaurusName(prevThes.getValue());
                                    }
                                    StringObject tmpErr = new StringObject();
                                    TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                    errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                    return TMSAPIClass.TMS_APIFail;
                                }
                            }
                            if(prevThes.getValue().equals(selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                        }

                    }
                }

            }

            if (direction == ConstantParameters.FROM_Direction) {

                int selected_category_nodes = Q.get_link_from_by_category(0, fromClassObj, linkObj);
                Q.reset_set(selected_category_nodes);
                if (Q.set_get_card(selected_category_nodes) > 0) {

                    ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                    if(Q.bulk_return_nodes(selected_category_nodes, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                deleteIDsL.add(row.get_Neo4j_NodeId());
                            }
                        }
                    }
                }

                Q.free_set(selected_category_nodes);
                deleteIDsL.trimToSize();

                if (KindOfDescriptor == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {
                    if (deleteIDsL.size() > 0) {
                        StringObject prevThes = new StringObject();
                        TA.GetThesaurusNameWithoutPrefix(prevThes);
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(selectedThesaurus);
                        }
                        for (int i = 0; i < deleteIDsL.size(); i++) {
                            ret = TA.CHECK_DeleteNewDescriptorAttribute(deleteIDsL.get(i).intValue(), targetDescriptorObj);
                            if (ret == TMSAPIClass.TMS_APIFail) {

                                StringObject tmpErr = new StringObject();
                                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                errorMsg.setValue(errorMsg.getValue().concat(tmpErr.getValue()));
                                //reset to previous thesaurus name if needed
                                if(prevThes.getValue().equals(selectedThesaurus)==false){
                                    TA.SetThesaurusName(prevThes.getValue());
                                }
                                return TMSAPIClass.TMS_APIFail;
                            }
                        }
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                    }

                } else {
                }

            }
            return ret;

        } else {
            return QClass.APIFail;
        }
    }

    public int delete_term_links_by_set(String selectedThesaurus,String targetDescriptor, int direction, int set_links, int KindOfDescriptor,
            QClass Q,TMSAPIClass TA, IntegerObject sis_session,  DBGeneral dbGen,StringObject errorMsg) {

        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        int ret = TMSAPIClass.TMS_APISucc;
        Q.reset_name_scope();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        if (termPrefix == null) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"errorDetected");
        }
        Q.reset_name_scope();
        // prepare input parameters: add prefix and convert to DB encoding
        StringObject targetDescriptorObj = new StringObject(termPrefix.concat(targetDescriptor));
        
        ArrayList<Long> deleteIDsL = new ArrayList<Long>();

        //StringObject label  = new StringObject();
        //IntegerObject sysid = new IntegerObject();
        //StringObject sclass = new StringObject();

        if (direction == ConstantParameters.TO_Direction) {

            ArrayList<String> newTargets = new ArrayList<String>();

            //collect link ids for deletion
            if (Q.set_get_card(set_links) > 0) {
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_links, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                            deleteIDsL.add(row.get_Neo4j_NodeId());
                        }
                    }
                }
                /*
                while (Q.retur_full_nodes(set_links, sysid, label, sclass) != QClass.APIFail) {
                    if (!deleteIDs.contains(sysid.getValue())) {
                        deleteIDs.add(sysid.getValue());
                    }
                }
                */
            }
            
            //find out which nodes have these links as from attributes
            int select_new_target_terms = Q.get_from_value(set_links);
            Q.reset_set(select_new_target_terms);

            //collect their names in newTargets Vector
            if (Q.set_get_card(select_new_target_terms) > 0) {

                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(select_new_target_terms, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        if (!newTargets.contains(row.get_v1_cls_logicalname())) {
                            newTargets.add(row.get_v1_cls_logicalname());
                        }
                    }
                }
                /*
                while (Q.retur_nodes(select_new_target_terms, label) != QClass.APIFail) {

                    if (!newTargets.contains(label.getValue())) {
                        newTargets.add(label.getValue());
                    }
                }*/
            }
            Q.free_set(select_new_target_terms);


            for (int k = 0; k < newTargets.size(); k++) {

                //each newtarget is set as current node and all its links from are traversed. 
                //deletenewTargetIDs will hold the ids of each current node that should be deleted according to deleteIDs
                Q.reset_name_scope();
                Q.set_current_node(new StringObject(newTargets.get(k)));

                ArrayList<Long> deletenewTargetIDsL = new ArrayList<Long>();
                int selected_new_target_nodes = Q.get_link_from(0);
                Q.reset_set(selected_new_target_nodes);
                if (Q.set_get_card(selected_new_target_nodes) > 0) {
                    
                    ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                    if(Q.bulk_return_nodes(selected_new_target_nodes, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            if (!deletenewTargetIDsL.contains(row.get_Neo4j_NodeId()) && deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                                deletenewTargetIDsL.add(row.get_Neo4j_NodeId());
                            }
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(selected_new_target_nodes, sysid, label, sclass) != QClass.APIFail) {
                        if (!deletenewTargetIDs.contains(sysid.getValue()) && deleteIDs.contains(sysid.getValue())) {
                            deletenewTargetIDs.add(sysid.getValue());
                        }
                    }*/
                }
                Q.free_set(selected_new_target_nodes);
                deletenewTargetIDsL.trimToSize();
                if (deletenewTargetIDsL.size() > 0) {

                    int KindOfDescr = dbGen.GetKindOfDescriptor(selectedThesaurus,new StringObject(newTargets.get(k)),Q,sis_session);

                    if (KindOfDescr == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {


                        StringObject prevThes = new StringObject();
                        TA.GetThesaurusNameWithoutPrefix(prevThes);
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(selectedThesaurus);
                        }
                        for (int i = 0; i < deletenewTargetIDsL.size(); i++) {
                            ret = TA.CHECK_DeleteNewDescriptorAttribute(deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                            if (ret == TMSAPIClass.TMS_APIFail) {

                                StringObject tmpErr = new StringObject();
                                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                
                                errorMsg.setValue(tmpErr.getValue());
                                //reset to previous thesaurus name if needed
                                if(prevThes.getValue().equals(selectedThesaurus)==false){
                                    TA.SetThesaurusName(prevThes.getValue());
                                }
                                return TMSAPIClass.TMS_APIFail;
                            }
                        }
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }


                    } else {

                        StringObject prevThes = new StringObject();
                        TA.GetThesaurusNameWithoutPrefix(prevThes);
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(selectedThesaurus);
                        }
                        for (int i = 0; i < deletenewTargetIDsL.size(); i++) {
                            ret = TA.CHECK_DeleteDescriptorAttribute(deletenewTargetIDsL.get(i).intValue(), new StringObject(newTargets.get(k)));
                            if (ret == TMSAPIClass.TMS_APIFail) {

                                StringObject tmpErr = new StringObject();
                                TA.ALMOST_DONE_GetTMS_APIErrorMessage(tmpErr);
                                
                                errorMsg.setValue(tmpErr.getValue());
                                //reset to previous thesaurus name if needed
                                if(prevThes.getValue().equals(selectedThesaurus)==false){
                                    TA.SetThesaurusName(prevThes.getValue());
                                }
                                return TMSAPIClass.TMS_APIFail;
                            }
                        }
                        //reset to previous thesaurus name if needed
                        if(prevThes.getValue().equals(selectedThesaurus)==false){
                            TA.SetThesaurusName(prevThes.getValue());
                        }
                    }

                }
            }

        }
        else if (direction == ConstantParameters.FROM_Direction) {

            if (Q.set_get_card(set_links) > 0) {

                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_links, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        if (!deleteIDsL.contains(row.get_Neo4j_NodeId())) {
                            deleteIDsL.add(row.get_Neo4j_NodeId());
                        }
                    }
                }
                /*
                while (Q.retur_full_nodes(set_links, sysid, label, sclass) != QClass.APIFail) {

                    if (!deleteIDs.contains(sysid.getValue())) {
                        deleteIDs.add(sysid.getValue());
                    }
                }*/
            }
            deleteIDsL.trimToSize();

            if (KindOfDescriptor == ConstantParameters.DESCRIPTOR_OF_KIND_NEW) {
                if (deleteIDsL.size() > 0) {
                    StringObject prevThes = new StringObject();
                    TA.GetThesaurusNameWithoutPrefix(prevThes);
                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                        TA.SetThesaurusName(selectedThesaurus);
                    }
                    for (int i = 0; i < deleteIDsL.size(); i++) {
                        ret = TA.CHECK_DeleteNewDescriptorAttribute(deleteIDsL.get(i).intValue(), targetDescriptorObj);
                        if (ret == TMSAPIClass.TMS_APIFail) {

                            //errorMsg.setValue(WTA.errorMessage.getValue());
                            TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
                            //reset to previous thesaurus name if needed
                            if(prevThes.getValue().equals(selectedThesaurus)==false){
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                            return TMSAPIClass.TMS_APIFail;
                        }
                    }
                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                }

            } else {
            }

        }
        return ret;
    }
    
    void CreateModifyStatusTermAccepted(String selectedThesaurus,StringObject targetDescriptorObj, String term_accepted,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session,IntegerObject tms_session,  DBGeneral dbGen,StringObject errorMsg){
       
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject acceptedObj = new StringObject();
        StringObject notAcceptedObj = new StringObject();
        dbtr.getThesaurusClass_StatusTermAccepted(selectedThesaurus,acceptedObj);
        dbtr.getThesaurusClass_StatusTermNotAccepted(selectedThesaurus,notAcceptedObj);
        
        Q.reset_name_scope();
        long accepted_IDL = Q.set_current_node(acceptedObj);
        
        Q.reset_name_scope();
        long not_Accepted_IDL = Q.set_current_node(notAcceptedObj);
        
        Q.reset_name_scope();
        long termIdL = Q.set_current_node(targetDescriptorObj);
        
        int set_classes = Q.get_all_classes(0);
        Q.reset_set(set_classes);
        
        ArrayList<String> termClassesNames = dbGen.get_Node_Names_Of_Set(set_classes, false,Q,sis_session);
        Q.free_set(set_classes);
        
        Identifier I_Accepted = new Identifier(accepted_IDL);
        Identifier I_Not_Accepted = new Identifier(not_Accepted_IDL);
        Identifier I_Term = new Identifier(termIdL);
        int ret = QClass.APISucc;
        
        if(term_accepted.toLowerCase().compareTo("yes")==0){
                        
            if(termClassesNames.contains(notAcceptedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Not_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
            if(!termClassesNames.contains(acceptedObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
            
        }
        else
        if(term_accepted.toLowerCase().compareTo("no")==0){
            
            if(termClassesNames.contains(acceptedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
            if(!termClassesNames.contains(notAcceptedObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_Not_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
        }
        else{
            
            if(termClassesNames.contains(notAcceptedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Not_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            if(termClassesNames.contains(acceptedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Accepted);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
        }
    }
    
    public void CreateModifyStatus(UserInfoClass SessionUserInfo,StringObject targetDescriptorObj, String status,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session,IntegerObject tms_session,  DBGeneral dbGen,StringObject errorMsg){
       
        String displayStatusFor_UC = Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction, SessionUserInfo);
        String displayStatusFor_FI = Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion, SessionUserInfo);
        String displayStatusFor_FA = Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Approval, SessionUserInfo);
        String displayStatusFor_FR = Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Reinspection, SessionUserInfo);
        String displayStatus_Approved = Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Approved, SessionUserInfo);
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject statusUnderConstructionObj = new StringObject();
        StringObject statusForApprovalObj       = new StringObject();
        StringObject statusForInsertionObj      = new StringObject();
        StringObject statusForReinspectionObj   = new StringObject();
        StringObject statusApprovedObj          = new StringObject();
        
        dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus,statusUnderConstructionObj);
        dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus,statusForApprovalObj);
        dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus,statusForInsertionObj);
        dbtr.getThesaurusClass_StatusForReinspection(SessionUserInfo.selectedThesaurus,statusForReinspectionObj);
        dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus,statusApprovedObj);
        
        //Get Sys_IDs and create Identifiers for all above Retrieved classes and target term
        Q.reset_name_scope();
        long under_construction_IDL = Q.set_current_node(statusUnderConstructionObj);
        
        Q.reset_name_scope();
        long for_Approval_IDL = Q.set_current_node(statusForApprovalObj);
       
        Q.reset_name_scope();
        long for_Insertion_IDL = Q.set_current_node(statusForInsertionObj);
        
        Q.reset_name_scope();
        long for_ReInspection_IDL = Q.set_current_node(statusForReinspectionObj);
        
        Q.reset_name_scope();
        long approved_IDL = Q.set_current_node(statusApprovedObj);
        
        Q.reset_name_scope();
        long termIdL = Q.set_current_node(targetDescriptorObj);
                
        int set_classes = Q.get_all_classes(0);
        Q.reset_set(set_classes);
        
        ArrayList<String> termClassesNames = dbGen.get_Node_Names_Of_Set(set_classes, false,Q,sis_session);
        Q.free_set(set_classes);
        
        Identifier I_Under_Construction = new Identifier(under_construction_IDL);
        Identifier I_For_Approval = new Identifier(for_Approval_IDL);
        Identifier I_For_Insertion = new Identifier(for_Insertion_IDL);
        Identifier I_For_Reinspection = new Identifier(for_ReInspection_IDL);
        Identifier I_Approved = new Identifier(approved_IDL);        
        Identifier I_Term = new Identifier(termIdL);
        int ret = QClass.APISucc;
            
        if(status.compareTo(displayStatusFor_UC)==0){
            
            //Delete previous status
            if(termClassesNames.contains(statusForApprovalObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Approval);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            if(termClassesNames.contains(statusForInsertionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Insertion);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusForReinspectionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Reinspection);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusApprovedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Approved);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
                                    
            //Add new status if needed
            if(!termClassesNames.contains(statusUnderConstructionObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_Under_Construction);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            
        }
        else
        if(status.compareTo(displayStatusFor_FA)==0){
            
            //Delete previous status
            if(termClassesNames.contains(statusUnderConstructionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term,I_Under_Construction );
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            if(termClassesNames.contains(statusForInsertionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Insertion);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusForReinspectionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Reinspection);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusApprovedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Approved);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
                                    
            //Add new status if needed
            if(!termClassesNames.contains(statusForApprovalObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_For_Approval);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
        }
        else
        if(status.compareTo(displayStatusFor_FI)==0){
            
            //Delete previous status
            if(termClassesNames.contains(statusUnderConstructionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term,I_Under_Construction );
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret,TA, null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusForApprovalObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Approval);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusForReinspectionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Reinspection);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA,null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusApprovedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Approved);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
                                    
            //Add new status if needed
            if(!termClassesNames.contains(statusForInsertionObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_For_Insertion);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
        }
        else
        if(status.compareTo(displayStatusFor_FR)==0){
            
            //Delete previous status
            if(termClassesNames.contains(statusUnderConstructionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term,I_Under_Construction );
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusForApprovalObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Approval);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            if(termClassesNames.contains(statusForInsertionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Insertion);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            
            if(termClassesNames.contains(statusApprovedObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_Approved);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
                                    
            //Add new status if needed
            if(!termClassesNames.contains(statusForReinspectionObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_For_Reinspection);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
        }
        else
        if(status.compareTo(displayStatus_Approved)==0){
            
            //Delete previous status
            if(termClassesNames.contains(statusUnderConstructionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term,I_Under_Construction );
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
                        
            if(termClassesNames.contains(statusForApprovalObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Approval);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            if(termClassesNames.contains(statusForInsertionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term, I_For_Insertion);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
            
            if(termClassesNames.contains(statusForReinspectionObj.getValue()))
                ret = Q.CHECK_Delete_Instance(I_Term,I_For_Reinspection);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
                                    
            //Add new status if needed
            if(!termClassesNames.contains(statusApprovedObj.getValue()))
                ret = Q.CHECK_Add_Instance(I_Term, I_Approved);
            
            if(ret == QClass.APIFail){
                errorMsg.setValue(errorMsg.getValue().concat(dbGen.check_success(ret, TA, null,tms_session)));
            }
        }
    }
    /* CreateModify_Finalization()
     * Handles created/modified info of each node affected.
     * Affected Nodes are target Node and all nodes in String otherModifiedNodes in a comma seperated list
     */
    public void CreateModify_Finalization(String selectedThesaurus,StringObject targetDescriptor, String user, 
            ArrayList<String> otherModifiedNodes, int createORmodify,
            QClass Q, IntegerObject sis_session,TMSAPIClass TA, IntegerObject tms_session,
            DBGeneral dbGen, StringObject errorMsg) {

        Q.reset_name_scope();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject modifiedOnClass = new StringObject();
        StringObject modifiedOnLink = new StringObject();
        StringObject modifiedByClass = new StringObject();
        StringObject modifiedByLink = new StringObject();

        StringObject createdOnClass = new StringObject();
        StringObject createdOnLink = new StringObject();
        StringObject createdByClass = new StringObject();
        StringObject createdByLink = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClass, modifiedByLink,Q,sis_session);
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.modified_on_kwd, modifiedOnClass, modifiedOnLink,Q,sis_session);
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.created_by_kwd, createdByClass, createdByLink,Q,sis_session);
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.created_on_kwd, createdOnClass, createdOnLink,Q,sis_session);


        String UserName = dbtr.getThesaurusPrefix_Editor(Q,sis_session.getValue()) + user;
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        Q.reset_name_scope();

        if (otherModifiedNodes != null && otherModifiedNodes.size() > 0) {

            //String[] otherModifiedNodeArray = otherModifiedNodes.split(",");
            for (int i = 0; i < otherModifiedNodes.size(); i++) {

                if (otherModifiedNodes.get(i).trim().length() > 0) {
                    StringObject targetObjModified = new StringObject(prefix.concat(otherModifiedNodes.get(i).trim()));

                    delete_term_links_by_category(selectedThesaurus,otherModifiedNodes.get(i).trim(), ConstantParameters.FROM_Direction, 
                            modifiedOnClass.getValue(), modifiedOnLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW,
                            Q, TA, sis_session, dbGen, errorMsg);
                    delete_term_links_by_category(selectedThesaurus,otherModifiedNodes.get(i).trim(), ConstantParameters.FROM_Direction, 
                            modifiedByClass.getValue(), modifiedByLink.getValue(), ConstantParameters.DESCRIPTOR_OF_KIND_NEW, 
                            Q, TA, sis_session, dbGen, errorMsg);

                    errorMsg.setValue(errorMsg.getValue().concat(connectEditor(selectedThesaurus,targetObjModified, UserName, modifiedByClass.getValue(), modifiedByLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));
                    errorMsg.setValue(errorMsg.getValue().concat(connectTime(selectedThesaurus,targetObjModified, modifiedOnClass.getValue(), modifiedOnLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));
                }
            }
        }

        if (createORmodify == DB_CREATE) {

            errorMsg.setValue(errorMsg.getValue().concat(connectEditor(selectedThesaurus,targetDescriptor, UserName, createdByClass.getValue(), createdByLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));
            errorMsg.setValue(errorMsg.getValue().concat(connectTime(selectedThesaurus,targetDescriptor, createdOnClass.getValue(), createdOnLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));

        } else if (createORmodify == DB_MODIFY) {

            delete_term_links_by_category(selectedThesaurus,dbGen.removePrefix(targetDescriptor.getValue()),
                    ConstantParameters.FROM_Direction, modifiedOnClass.getValue(), modifiedOnLink.getValue(),
                    ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);
            delete_term_links_by_category(selectedThesaurus,dbGen.removePrefix(targetDescriptor.getValue()),
                    ConstantParameters.FROM_Direction, modifiedByClass.getValue(), modifiedByLink.getValue(),
                    ConstantParameters.DESCRIPTOR_OF_KIND_NEW, Q, TA, sis_session, dbGen, errorMsg);

            errorMsg.setValue(errorMsg.getValue().concat(connectEditor(selectedThesaurus,targetDescriptor,
                    UserName, modifiedByClass.getValue(), modifiedByLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));
            errorMsg.setValue(errorMsg.getValue().concat(connectTime(selectedThesaurus,targetDescriptor,
                    modifiedOnClass.getValue(), modifiedOnLink.getValue(), Q,  sis_session, dbGen, TA,  tms_session)));
        }
        
    }

}