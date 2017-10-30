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


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;


import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;

import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SortItem;
import Utils.NodeInfoSortItemContainer;
import Utils.NodeInfoStringContainer;

import XMLHandling.WriteFileData;

import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.Return_Nodes_Row;
import neo4j_sisapi.StringObject;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class DBexportData {

    public DBexportData() {
    }

    public void exportThesaurusActions(UserInfoClass SessionUserInfo, /*CommonUtilsDBadmin common_utils,*/
            String exprortThesaurus,
            String exportSchemaName,
            OutputStreamWriter logFileWriter,
            ArrayList<String> thesauriNames,
            ArrayList<String> allHierarchies,
            ArrayList<String> allGuideTerms){

        DBGeneral dbGen = new DBGeneral();

        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();

        WriteFileData writer = new WriteFileData();

        DBMergeThesauri dbMerge = new DBMergeThesauri();
        DBImportData dbImport = new DBImportData();

        HashMap<String, String> translationCategories = new HashMap<String, String>();
        
        ArrayList<SortItem> xmlFacetsInSortItem = new ArrayList<SortItem>();
        HashMap<SortItem, ArrayList<SortItem>> hierarchyFacets = new HashMap<SortItem, ArrayList<SortItem>>();

        
        ArrayList<String> guideTerms = new ArrayList<String>();
        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        ArrayList<String> topTerms = new ArrayList<String>();
        HashMap<String, ArrayList<String>> descriptorRts = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> descriptorUfs = new HashMap<String, ArrayList<String>>();
        ArrayList<HashMap<String, ArrayList<String>>> allLevelsOfImportThes = new ArrayList<HashMap<String, ArrayList<String>>>();


        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();


            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, null, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ DBexportData exportThesaurusActions()");
                return;
            }

            thesauriNames = dbGen.GetExistingThesaurus(false, thesauriNames, Q, sis_session);
            if(thesauriNames.contains(exprortThesaurus)==false){
                
                Utils.StaticClass.webAppSystemOutPrintln("Export thesaurus with name: " + exprortThesaurus + "was not found in the database");
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                return;
            }


            translationCategories = dbGen.getThesaurusTranslationCategories(Q, TA, sis_session, exprortThesaurus, null, false, true);
            
            SortItem defaultFacetSortItem = dbMerge.getDefaultFacetSortItem(SessionUserInfo, Q, sis_session, exprortThesaurus);
            SortItem defaultHierarchySortItem = dbMerge.getDefaultUnclassifiedTopTermSortItem(SessionUserInfo, Q, sis_session, exprortThesaurus);
            defaultHierarchySortItem.setLogName(dbGen.removePrefix(defaultHierarchySortItem.getLogName()));
            
            xmlFacetsInSortItem.addAll(dbMerge.ReadThesaurusFacetsInSortItems(SessionUserInfo, Q, sis_session, exprortThesaurus, null));

            if (xmlFacetsInSortItem.contains(defaultFacetSortItem) == false) {
                xmlFacetsInSortItem.add(defaultFacetSortItem);
            }

            //read hierarchies
            hierarchyFacets = dbMerge.ReadThesaurusHierarchiesInSortItems(SessionUserInfo, Q, sis_session, exprortThesaurus, null);
            
            if (hierarchyFacets.containsKey(defaultHierarchySortItem) == false) {
                ArrayList<SortItem> unclassifiedFacets = new ArrayList<>();
                unclassifiedFacets.add(defaultFacetSortItem);
                hierarchyFacets.put(defaultHierarchySortItem, unclassifiedFacets);
            }


            //readTermsInfo and guide terms
            dbMerge.ReadThesaurusTerms(SessionUserInfo, Q,TA, sis_session, exprortThesaurus, null,
                    termsInfo, guideTerms, XMLguideTermsRelations);

            guideTerms.clear();
            guideTerms.addAll(dbGen.collectGuideLinks(exprortThesaurus, Q, sis_session));
            HashMap<String, ArrayList<String>> hierarchyFacetsStrFormat = new HashMap<>();
            hierarchyFacets.forEach( (key,value) -> {
                hierarchyFacetsStrFormat.put(key.getLogName(), new ArrayList<String>(value.stream().map(item -> item.getLogName()).collect(Collectors.toList())));
            });
            
            //read terms
            dbImport.processXMLTerms(termsInfo, descriptorRts, descriptorUfs, hierarchyFacetsStrFormat, topTerms, allLevelsOfImportThes);

            //read sources
            dbMerge.ReadThesaurusSources(SessionUserInfo, Q,TA, sis_session, XMLsources);

            
            dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo, Q, sis_session, allHierarchies, allGuideTerms);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

             //check if tcs are defined for skos case
            if (exportSchemaName.equals(ConstantParameters.xmlschematype_skos)) {
                ArrayList<String> termNames = new ArrayList<String>(termsInfo.keySet());
                Collections.sort(termNames);
                int howmanyWithoutTc = 0;
                String homanytermNames = "000" + termNames.size();
                homanytermNames = homanytermNames.replaceAll("[1-9]", "0");
                NumberFormat formatter = new DecimalFormat(homanytermNames);
                for (int k = 0; k < termNames.size(); k++) {
                    String targetTerm = termNames.get(k);
                    NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
                    ArrayList<String> tcs = targetInfo.descriptorInfo.get(ConstantParameters.tc_kwd);
                    if (tcs == null || tcs.size() == 0) {
                        ArrayList<String> newTcs = new ArrayList<String>();
                        howmanyWithoutTc++;

                        String newTCValue = exprortThesaurus+formatter.format(howmanyWithoutTc);
                        newTcs.add(newTCValue);
                        targetInfo.descriptorInfo.put(ConstantParameters.tc_kwd, newTcs);
                    }
                }
            }


            try{

                writer.WriteFileStart(logFileWriter, exportSchemaName, exprortThesaurus);
                writer.WriteTranslationCategories(logFileWriter, exportSchemaName, translationCategories);

                writer.WriteFacetsFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, xmlFacetsInSortItem, hierarchyFacets, termsInfo, null, null);
                writer.WriteHierarchiesFromSortItems(logFileWriter, exportSchemaName, exprortThesaurus, hierarchyFacets, termsInfo, XMLguideTermsRelations, null, null);
                writer.WriteTerms(logFileWriter, exportSchemaName, exprortThesaurus, hierarchyFacetsStrFormat, termsInfo, XMLguideTermsRelations, null);
                writer.WriteGuideTerms(logFileWriter, exportSchemaName, guideTerms);
                writer.WriteSources(logFileWriter, exportSchemaName, XMLsources);
                writer.WriteFileEnd(logFileWriter, exportSchemaName);
            }catch (Exception e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in DBexportData exportThesaurusActions() Message:" + e.getMessage());
                Utils.StaticClass.handleException(e);

            }


    }
    
    public void ReadTermStatuses(String selectedThesaurus, QClass Q, IntegerObject sis_session, 
            ArrayList<String> output, ArrayList<String> allTerms, HashMap<String, NodeInfoSortItemContainer> termsInfo,
            ArrayList<Long> resultNodesIds) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        if (output.contains(ConstantParameters.status_kwd) == false) {
            return;
        }

        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        

        StringObject THESstatusForInsertion = new StringObject();
        StringObject THESstatusUnderConstruction = new StringObject();
        StringObject THESstatusForApproval = new StringObject();
        StringObject THESstatusApproved = new StringObject();

        ArrayList<StringObject> allStatuses = new ArrayList<StringObject>();

        //READING FROM THES1 
        dbtr.getThesaurusClass_StatusForInsertion(selectedThesaurus, THESstatusForInsertion);
        dbtr.getThesaurusClass_StatusUnderConstruction(selectedThesaurus, THESstatusUnderConstruction);
        dbtr.getThesaurusClass_StatusForApproval(selectedThesaurus, THESstatusForApproval);
        dbtr.getThesaurusClass_StatusApproved(selectedThesaurus, THESstatusApproved);

        allStatuses.add(THESstatusForInsertion);
        allStatuses.add(THESstatusUnderConstruction);
        allStatuses.add(THESstatusForApproval);
        allStatuses.add(THESstatusApproved);

        for (int i = 0; i < allStatuses.size(); i++) {

            String writeStatus = statusDBtoUImapping(selectedThesaurus, allStatuses.get(i).getValue());
            Q.reset_name_scope();
            long statusIdL = Q.set_current_node(allStatuses.get(i));
            int set_all_such_terms = Q.get_instances(0);

            Q.reset_set(set_all_such_terms);
            
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_all_such_terms, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    if (resultNodesIds.contains(row.get_Neo4j_NodeId())) {
                        String targetTermName = dbGen.removePrefix(row.get_v1_cls_logicalname());

                        if (termsInfo.containsKey(targetTermName) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            termsInfo.put(targetTermName, newContainer);
                            allTerms.add(targetTermName);
                        }
                        termsInfo.get(targetTermName).descriptorInfo.get(ConstantParameters.status_kwd).add(new SortItem(writeStatus, statusIdL));
                    }
                }
            }
            /*
            IntegerObject sysIdObj = new IntegerObject();
            StringObject nodeNameObj = new StringObject();
            StringObject classObj = new StringObject();
            while (Q.retur_full_nodes(set_all_such_terms, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
                if (resultNodesIds.contains(sysIdObj.getValue())) {
                    String targetTermName = dbGen.removePrefix(nodeNameObj.getValue());

                    if (termsInfo.containsKey(targetTermName) == false) {
                        NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                        termsInfo.put(targetTermName, newContainer);
                        allTerms.add(targetTermName);
                    }
                    termsInfo.get(targetTermName).descriptorInfo.get(ConstantParameters.status_kwd).add(new SortItem(writeStatus, statusIdL));
                }
            }
            */

            Q.free_set(set_all_such_terms);
        }

        Q.reset_name_scope();
    }

    public String statusDBtoUImapping(String selectedThesaurus, String dbStatus) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject statusForInsertionObj = new StringObject();
        StringObject statusUnderConstructionObj = new StringObject();
        StringObject statusForApprovalObj = new StringObject();
        StringObject statusApprovedObj = new StringObject();


        dbtr.getThesaurusClass_StatusForInsertion(selectedThesaurus, statusForInsertionObj);
        dbtr.getThesaurusClass_StatusUnderConstruction(selectedThesaurus, statusUnderConstructionObj);
        dbtr.getThesaurusClass_StatusForApproval(selectedThesaurus, statusForApprovalObj);
        dbtr.getThesaurusClass_StatusApproved(selectedThesaurus, statusApprovedObj);

        if (dbStatus.compareTo(statusForInsertionObj.getValue()) == 0) {

            return Parameters.Status_For_Insertion;
        }
        if (dbStatus.compareTo(statusUnderConstructionObj.getValue()) == 0) {

            return Parameters.Status_Under_Construction;
        }
        if (dbStatus.compareTo(statusForApprovalObj.getValue()) == 0) {

            return Parameters.Status_For_Approval;
        }
        if (dbStatus.compareTo(statusApprovedObj.getValue()) == 0) {

            return Parameters.Status_Approved;
        }
        return "";
    }

    public void ReadTermCommentCategories(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, 
            ArrayList<String> output, ArrayList<String> allTerms, HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<Long> resultNodesIds) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        

        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        //IntegerObject sysIdObj = new IntegerObject();
        //StringObject nodeNameObj = new StringObject();
        //StringObject classObj = new StringObject();

        if (output.contains(ConstantParameters.scope_note_kwd)) {
            //SCOPE NOTES 
            ArrayList<String> terms_with_sn_Vec = new ArrayList<String>();
            Q.reset_name_scope();
            StringObject scopenoteFromClassObj = new StringObject();
            StringObject scopenoteLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.scope_note_kwd, scopenoteFromClassObj, scopenoteLinkObj, Q, sis_session);
            Q.reset_name_scope();


            Q.set_current_node(scopenoteFromClassObj);
            Q.set_current_node(scopenoteLinkObj);
            int set_all_links_sn = Q.get_all_instances(0);
            Q.reset_set(set_all_links_sn);
            int set_terms_with_sn = Q.get_from_value(set_all_links_sn);
            Q.reset_set(set_terms_with_sn);
            Q.free_set(set_all_links_sn);


            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_terms_with_sn, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    if (resultNodesIds.contains(row.get_Neo4j_NodeId())) {
                        String targetTerm = row.get_v1_cls_logicalname();
                        terms_with_sn_Vec.add(targetTerm);
                    }
                }
            }
            /*
            while (Q.retur_full_nodes(set_terms_with_sn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
                if (resultNodesIds.contains(sysIdObj.getValue())) {
                    String targetTerm = nodeNameObj.getValue();
                    terms_with_sn_Vec.add(targetTerm);
                }
            }
            */
            Q.free_set(set_terms_with_sn);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            for (int i = 0; i < terms_with_sn_Vec.size(); i++) {

                String targetDBTerm = terms_with_sn_Vec.get(i);
                String targetUITerm = dbGen.removePrefix(targetDBTerm);
                StringObject commentObject = new StringObject("");
                TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, scopenoteFromClassObj, scopenoteLinkObj);
                if (termsInfo.containsKey(targetUITerm) == false) {
                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                    termsInfo.put(targetUITerm, newContainer);
                    allTerms.add(targetUITerm);
                }
                if (commentObject.getValue().length() > 0) {
                    termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.scope_note_kwd).add(new SortItem(commentObject.getValue(), -1));
                }
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
        }


        if (output.contains(ConstantParameters.translations_scope_note_kwd)) {
            //SCOPE NOTES EN
            ArrayList<String> terms_with_sn_TR_Vec = new ArrayList<String>();
            Q.reset_name_scope();
            StringObject scopenote_TR_FromClassObj = new StringObject();
            StringObject scopenote_TR_LinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.translations_scope_note_kwd, scopenote_TR_FromClassObj, scopenote_TR_LinkObj, Q, sis_session);
            Q.reset_name_scope();


            Q.set_current_node(scopenote_TR_FromClassObj);
            Q.set_current_node(scopenote_TR_LinkObj);
            int set_all_links_sn_tr = Q.get_all_instances(0);
            Q.reset_set(set_all_links_sn_tr);
            int set_terms_with_sn_tr = Q.get_from_value(set_all_links_sn_tr);
            Q.reset_set(set_terms_with_sn_tr);
            Q.free_set(set_all_links_sn_tr);

            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_terms_with_sn_tr, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    if (resultNodesIds.contains(row.get_Neo4j_NodeId())) {
                        String targetTerm = row.get_v1_cls_logicalname();
                        terms_with_sn_TR_Vec.add(targetTerm);
                    }
                }
            }
            /*
            while (Q.retur_full_nodes(set_terms_with_sn_tr, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
                if (resultNodesIds.contains(sysIdObj.getValue())) {
                    String targetTerm = nodeNameObj.getValue();
                    terms_with_sn_TR_Vec.add(targetTerm);
                }
            }
            */
            Q.free_set(set_terms_with_sn_tr);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            for (int i = 0; i < terms_with_sn_TR_Vec.size(); i++) {

                String targetDBTerm = terms_with_sn_TR_Vec.get(i);
                String targetUITerm = dbGen.removePrefix(targetDBTerm);
                StringObject commentObject = new StringObject("");
                TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, scopenote_TR_FromClassObj, scopenote_TR_LinkObj);
                if (termsInfo.containsKey(targetUITerm) == false) {
                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                    termsInfo.put(targetUITerm, newContainer);
                    allTerms.add(targetUITerm);
                }
                if (commentObject.getValue().length() > 0) {

                    //break in parts
                    HashMap<String,String> trSns = u.getTranslationScopeNotes(commentObject.getValue());
                    ArrayList<String> langCodes = new ArrayList<String>(trSns.keySet());
                    Collections.sort(langCodes);
                    for(int m=0;m<langCodes.size();m++){
                        String lang = langCodes.get(m);
                        String val = trSns.get(lang);
                        if(val!=null && val.trim().length()>0){
                            termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.translations_scope_note_kwd).add(new SortItem(lang+Parameters.TRANSLATION_SEPERATOR+" " +val.trim(),-1));
                        }
                    }
                    //termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.translations_scope_note_kwd).add(new SortItem(commentObject.getValue(), -1));
                }
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
        }

        if (output.contains(ConstantParameters.historical_note_kwd)) {
            //HISTORICAL NOTES 
            ArrayList<String> terms_with_hn_Vec = new ArrayList<String>();
            Q.reset_name_scope();
            StringObject historicalnoteFromClassObj = new StringObject();
            StringObject historicalnoteLinkObj = new StringObject();
            dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.historical_note_kwd, historicalnoteFromClassObj, historicalnoteLinkObj, Q, sis_session);
            Q.reset_name_scope();

            Q.set_current_node(historicalnoteFromClassObj);
            Q.set_current_node(historicalnoteLinkObj);
            int set_all_links_hn = Q.get_all_instances(0);
            Q.reset_set(set_all_links_hn);
            int set_terms_with_hn = Q.get_from_value(set_all_links_hn);
            Q.reset_set(set_terms_with_hn);
            Q.free_set(set_all_links_hn);

            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_terms_with_hn, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    if (resultNodesIds.contains(row.get_Neo4j_NodeId())) {
                        String targetTerm = row.get_v1_cls_logicalname();
                        terms_with_hn_Vec.add(targetTerm);
                    }
                }
            }
            /*
            while (Q.retur_full_nodes(set_terms_with_hn, sysIdObj, nodeNameObj, classObj) != QClass.APIFail) {
                if (resultNodesIds.contains(sysIdObj.getValue())) {
                    String targetTerm = nodeNameObj.getValue();
                    terms_with_hn_Vec.add(targetTerm);
                }
            }
            */
            Q.free_set(set_terms_with_hn);

            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(selectedThesaurus);
            }
            for (int i = 0; i < terms_with_hn_Vec.size(); i++) {

                String targetDBTerm = terms_with_hn_Vec.get(i);
                String targetUITerm = dbGen.removePrefix(targetDBTerm);
                StringObject commentObject = new StringObject("");
                TA.GetDescriptorComment(new StringObject(targetDBTerm), commentObject, historicalnoteFromClassObj, historicalnoteLinkObj);
                if (termsInfo.containsKey(targetUITerm) == false) {
                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                    termsInfo.put(targetUITerm, newContainer);
                    allTerms.add(targetUITerm);
                }

                if (commentObject.getValue().length() > 0) {
                    termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.historical_note_kwd).add(new SortItem(commentObject.getValue(), -1));
                }
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
        }
    }

    public void ReadTermFacetAndHierarchies(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session,
            int set_terms, ArrayList<String> output, ArrayList<String> allTerms, 
            HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<Long> resultNodesIds) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        
        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        int cardinalityOfTerms = Q.set_get_card(set_terms);

        IntegerObject sysIdObj = new IntegerObject();
        //StringObject nodeName = new StringObject();
        StringObject cls = new StringObject();

        if (output.contains(ConstantParameters.facet_kwd)) {

            int facetIndex = Parameters.CLASS_SET.indexOf("FACET");
            String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(facetIndex).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(facetIndex).toArray(FacetClasses);

            int set_f = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
            Q.reset_set(set_f);

            int cardinalityOfFacets = Q.set_get_card(set_f);

            if (cardinalityOfFacets > cardinalityOfTerms) {

                //set current node for each term
                ArrayList<String> terms = new ArrayList<String>();
                Q.reset_set(set_terms);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_terms, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        terms.add(row.get_v1_cls_logicalname());
                    }
                }
                /*
                while (Q.retur_nodes(set_terms, nodeName) != QClass.APIFail) {
                    terms.add(nodeName.getValue());
                }*/

                for (int i = 0; i < terms.size(); i++) {

                    StringObject targetTerm = new StringObject(terms.get(i));
                    String targetUITerm = dbGen.removePrefix(targetTerm.getValue());

                    Q.reset_name_scope();
                    long termIdL = Q.set_current_node(targetTerm);
                    int set_all_classes = Q.get_all_classes(0);
                    Q.reset_set(set_all_classes);
                    Q.reset_set(set_f);
                    Q.set_intersect(set_all_classes, set_f);
                    Q.reset_set(set_all_classes);

                    retVals.clear();
                    if(Q.bulk_return_nodes(set_all_classes, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            String targetFacet = dbGen.removePrefix(row.get_v1_cls_logicalname());
                            long targetFacetIdL = row.get_Neo4j_NodeId();

                            if (termsInfo.containsKey(targetUITerm) == false) {
                                NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL));
                                termsInfo.put(targetUITerm, newContainer);
                                allTerms.add(targetUITerm);
                            }
                            termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetFacet, targetFacetIdL));
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(set_all_classes, sysIdObj, nodeName, cls) != QClass.APIFail) {
                        String targetFacet = dbGen.removePrefix(nodeName.getValue());
                        int targetFacetId = sysIdObj.getValue();

                        if (termsInfo.containsKey(targetUITerm) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL));
                            termsInfo.put(targetUITerm, newContainer);
                            allTerms.add(targetUITerm);
                        }
                        termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetFacet, targetFacetId));

                    }
                    */
                    Q.free_set(set_all_classes);
                }

            } else { //set current node for each facet


                ArrayList<String> facets = new ArrayList<String>();
                Q.reset_set(set_f);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_f, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        facets.add(row.get_v1_cls_logicalname());
                    }
                }
                /*while (Q.retur_nodes(set_f, nodeName) != QClass.APIFail) {
                    facets.add(nodeName.getValue());
                }*/
                for (int i = 0; i < facets.size(); i++) {
                    StringObject targetFacet = new StringObject(facets.get(i));
                    String targetUIFacet = dbGen.removePrefix(targetFacet.getValue());

                    Q.reset_name_scope();
                    long facetIdL = Q.set_current_node(targetFacet);
                    int set_all_such_terms = Q.get_all_instances(0);
                    Q.reset_set(set_all_such_terms);

                    Q.reset_set(set_terms);
                    Q.set_intersect(set_all_such_terms, set_terms);
                    Q.reset_set(set_all_such_terms);
                    retVals.clear();
                    if(Q.bulk_return_nodes(set_all_such_terms, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            String targetTerm = dbGen.removePrefix(row.get_v1_cls_logicalname());
                            long targetTermIdL = row.get_Neo4j_NodeId();
                            if (resultNodesIds.contains(targetTermIdL)) {
                                if (termsInfo.containsKey(targetTerm) == false) {
                                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                    newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermIdL, targetTermIdL));
                                    termsInfo.put(targetTerm, newContainer);
                                    allTerms.add(targetTerm);
                                }
                                termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetUIFacet, facetIdL));
                            }
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(set_all_such_terms, sysIdObj, nodeName, cls) != QClass.APIFail) {
                        String targetTerm = dbGen.removePrefix(nodeName.getValue());
                        int targetTermId = sysIdObj.getValue();
                        if (resultNodesIds.contains(targetTermId)) {
                            if (termsInfo.containsKey(targetTerm) == false) {
                                NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId));
                                termsInfo.put(targetTerm, newContainer);
                                allTerms.add(targetTerm);
                            }
                            termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetUIFacet, facetIdL));
                        }
                    }
                    */
                    Q.free_set(set_all_such_terms);
                }
            }

            Q.free_set(set_f);
        }

        if (output.contains(ConstantParameters.topterm_kwd)) {

            StringObject belongsToHierarchyClass = new StringObject();
            StringObject belongsToHierarchyLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierarchyClass, belongsToHierarchyLink, Q, sis_session);

            int hierIndex = Parameters.CLASS_SET.indexOf("HIERARCHY");
            String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(hierIndex).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(hierIndex).toArray(HierarchyClasses);


            int set_h = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
            Q.reset_set(set_h);

            int cardinalityOfHierarchies = Q.set_get_card(set_h);

            if (cardinalityOfHierarchies > cardinalityOfTerms) { //set current node for each term

                //set current node for each term
                ArrayList<String> terms = new ArrayList<String>();
                Q.reset_set(set_terms);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_terms, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        terms.add(row.get_v1_cls_logicalname());
                    }
                }
                /*while (Q.retur_nodes(set_terms, nodeName) != QClass.APIFail) {
                    terms.add(nodeName.getValue());
                }*/

                for (int i = 0; i < terms.size(); i++) {

                    StringObject targetTerm = new StringObject(terms.get(i));
                    String targetUITerm = dbGen.removePrefix(targetTerm.getValue());

                    Q.reset_name_scope();
                    long termIdL = Q.set_current_node(targetTerm);
                    int set_all_classes = Q.get_classes(0);
                    Q.reset_set(set_all_classes);

                    Q.reset_set(set_h);
                    Q.set_intersect(set_all_classes, set_h);
                    Q.reset_set(set_all_classes);

                    retVals.clear();
                    if(Q.bulk_return_nodes(set_all_classes, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            String targetHierarchy = dbGen.removePrefix(row.get_v1_cls_logicalname());
                            long targetHierarchyIdL = row.get_Neo4j_NodeId();

                            if (termsInfo.containsKey(targetUITerm) == false) {
                                NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL));
                                termsInfo.put(targetUITerm, newContainer);
                                allTerms.add(targetUITerm);
                            }
                            termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.topterm_kwd).add(new SortItem(targetHierarchy, targetHierarchyIdL));

                        }
                    }
                    /*
                    while (Q.retur_full_nodes(set_all_classes, sysIdObj, nodeName, cls) != QClass.APIFail) {
                        String targetHierarchy = dbGen.removePrefix(nodeName.getValue());
                        int targetHierarchyId = sysIdObj.getValue();

                        if (termsInfo.containsKey(targetUITerm) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termId, termId));
                            termsInfo.put(targetUITerm, newContainer);
                            allTerms.add(targetUITerm);
                        }
                        termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.topterm_kwd).add(new SortItem(targetHierarchy, targetHierarchyId));

                    }
                    */
                    Q.free_set(set_all_classes);
                }

            } else { //set current node for each hierarchy

                ArrayList<String> hierarchies = new ArrayList<String>();
                Q.reset_set(set_h);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if(Q.bulk_return_nodes(set_h, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        hierarchies.add(row.get_v1_cls_logicalname());
                    }
                }
                /*while (Q.retur_nodes(set_h, nodeName) != QClass.APIFail) {
                    hierarchies.add(nodeName.getValue());
                }*/
                for (int i = 0; i < hierarchies.size(); i++) {
                    StringObject targetHierarchy = new StringObject(hierarchies.get(i));
                    String targetUIHierarchy = dbGen.removePrefix(targetHierarchy.getValue());

                    Q.reset_name_scope();
                    long hierarchyIdL = Q.set_current_node(targetHierarchy);
                    int set_all_such_terms = Q.get_all_instances(0);
                    Q.reset_set(set_all_such_terms);

                    //writeDown TOPTERM ID NOT HIERARCHY ID
                    int set_hiers_topterm_labels = Q.get_link_to_by_category(0, belongsToHierarchyClass, belongsToHierarchyLink);
                    Q.reset_set(set_hiers_topterm_labels);
                    int set_hiers_topterm = Q.get_from_value(set_hiers_topterm_labels);
                    Q.reset_set(set_hiers_topterm);
                    
                    retVals.clear();
                    if(Q.bulk_return_nodes(set_hiers_topterm, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            hierarchyIdL = row.get_Neo4j_NodeId();
                            targetUIHierarchy = dbGen.removePrefix(row.get_v1_cls_logicalname());
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(set_hiers_topterm, sysIdObj, nodeName, cls) != QClass.APIFail) {
                        hierarchyIdL = sysIdObj.getValue();
                        targetUIHierarchy = dbGen.removePrefix(nodeName.getValue());
                    }
                    */
                    Q.free_set(set_hiers_topterm_labels);
                    Q.free_set(set_hiers_topterm);

                    Q.reset_set(set_terms);
                    Q.set_intersect(set_all_such_terms, set_terms);
                    Q.reset_set(set_all_such_terms);
                    retVals.clear();
                    if(Q.bulk_return_nodes(set_all_such_terms, retVals)!=QClass.APIFail){
                        for(Return_Nodes_Row row:retVals){
                            String targetTerm = dbGen.removePrefix(row.get_v1_cls_logicalname());
                            long targetTermIdL = row.get_Neo4j_NodeId();
                            if (resultNodesIds.contains(targetTermIdL)) {
                                if (termsInfo.containsKey(targetTerm) == false) {
                                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                    newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermIdL, targetTermIdL));
                                    termsInfo.put(targetTerm, newContainer);
                                    allTerms.add(targetTerm);
                                }
                                termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.topterm_kwd).add(new SortItem(targetUIHierarchy, hierarchyIdL));
                            }
                        }
                    }
                    /*
                    while (Q.retur_full_nodes(set_all_such_terms, sysIdObj, nodeName, cls) != QClass.APIFail) {
                        String targetTerm = dbGen.removePrefix(nodeName.getValue());
                        int targetTermId = sysIdObj.getValue();
                        if (resultNodesIds.contains(targetTermId)) {
                            if (termsInfo.containsKey(targetTerm) == false) {
                                NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                                newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermId, targetTermId));
                                termsInfo.put(targetTerm, newContainer);
                                allTerms.add(targetTerm);
                            }
                            termsInfo.get(targetTerm).descriptorInfo.get(ConstantParameters.topterm_kwd).add(new SortItem(targetUIHierarchy, hierarchyIdL));
                        }
                    }
                    */
                    Q.free_set(set_all_such_terms);
                }
            }
            Q.free_set(set_h);
        }



    }
    
    public void ReadTermFacetAndHierarchiesInSortItems(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session,
            int set_terms, ArrayList<String> output, ArrayList<String> allTerms, 
            HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<Long> resultNodesIds) throws IOException {

        DBGeneral dbGen = new DBGeneral();
        
        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        int cardinalityOfTerms = Q.set_get_card(set_terms);

        IntegerObject sysIdObj = new IntegerObject();
        //StringObject nodeName = new StringObject();
        StringObject cls = new StringObject();

        if (output.contains(ConstantParameters.facet_kwd)) {

            int facetIndex = Parameters.CLASS_SET.indexOf("FACET");
            String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(facetIndex).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(facetIndex).toArray(FacetClasses);

            int set_f = dbGen.get_Instances_Set(FacetClasses, Q, sis_session);
            Q.reset_set(set_f);

            //int cardinalityOfFacets = Q.set_get_card(set_f);


            //set current node for each term
            ArrayList<SortItem> terms = new ArrayList<SortItem>();
            Q.reset_set(set_terms);
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_terms, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    SortItem newTermSortItem = new SortItem(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId(),row.get_v3_cls_transliteration(),row.get_v2_long_referenceId());
                    terms.add(newTermSortItem);
                }
            }
            /*
            while (Q.retur_nodes(set_terms, nodeName) != QClass.APIFail) {
                terms.add(nodeName.getValue());
            }*/

            for (SortItem targetTermSortItem : terms) {

                StringObject targetTerm = new StringObject(targetTermSortItem.getLogName());
                String targetUITerm = dbGen.removePrefix(targetTermSortItem.getLogName());

                Q.reset_name_scope();
                long termIdL = Q.set_current_node_id(targetTermSortItem.getSysId());
                int set_all_classes = Q.get_all_classes(0);
                Q.reset_set(set_all_classes);
                Q.reset_set(set_f);
                Q.set_intersect(set_all_classes, set_f);
                Q.reset_set(set_all_classes);

                retVals.clear();
                if(Q.bulk_return_nodes(set_all_classes, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        String targetFacet = dbGen.removePrefix(row.get_v1_cls_logicalname());
                        long targetFacetIdL = row.get_Neo4j_NodeId();
                        long targetFacetRefIdL = row.get_v2_long_referenceId();
                        String targetFacetTransliteration = row.get_v3_cls_transliteration();

                        if (termsInfo.containsKey(targetUITerm) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL,targetTermSortItem.getLogNameTransliteration(),targetTermSortItem.getThesaurusReferenceId()));
                            termsInfo.put(targetUITerm, newContainer);
                            allTerms.add(targetUITerm);
                        }
                        termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetFacet, targetFacetIdL,targetFacetTransliteration,targetFacetRefIdL));
                    }
                }
                /*
                while (Q.retur_full_nodes(set_all_classes, sysIdObj, nodeName, cls) != QClass.APIFail) {
                    String targetFacet = dbGen.removePrefix(nodeName.getValue());
                    int targetFacetId = sysIdObj.getValue();

                    if (termsInfo.containsKey(targetUITerm) == false) {
                        NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                        newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL));
                        termsInfo.put(targetUITerm, newContainer);
                        allTerms.add(targetUITerm);
                    }
                    termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.facet_kwd).add(new SortItem(targetFacet, targetFacetId));

                }
                */
                Q.free_set(set_all_classes);
            }
            Q.free_set(set_f);
        }

        if (output.contains(ConstantParameters.topterm_kwd)) {

            StringObject belongsToHierarchyClass = new StringObject();
            StringObject belongsToHierarchyLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierarchyClass, belongsToHierarchyLink, Q, sis_session);

            int hierIndex = Parameters.CLASS_SET.indexOf("HIERARCHY");
            String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(hierIndex).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(hierIndex).toArray(HierarchyClasses);


            int set_h = dbGen.get_Instances_Set(HierarchyClasses, Q, sis_session);
            Q.reset_set(set_h);

            int cardinalityOfHierarchies = Q.set_get_card(set_h);

             //set current node for each term

            //set current node for each term
            ArrayList<SortItem> terms = new ArrayList<SortItem>();
            Q.reset_set(set_terms);
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_terms, retVals)!=QClass.APIFail){
                for(Return_Nodes_Row row:retVals){
                    SortItem newTermSortItem = new SortItem(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId(),row.get_v3_cls_transliteration(),row.get_v2_long_referenceId());
                    terms.add(newTermSortItem);
                }
            }
            /*while (Q.retur_nodes(set_terms, nodeName) != QClass.APIFail) {
                terms.add(nodeName.getValue());
            }*/

            /*
            if(Parameters.OnlyTopTermsHoldReferenceId){
                int set_topterms = Q.get_from_node_by_category(set_sub_classes, belongsToHierClass, belongsToHierarchyLink);
                results.addAll(get_Node_Names_Of_Set_In_SortItems(set_topterms, true, Q, sis_session));
            }
            else{
                results.addAll(get_Node_Names_Of_Set_In_SortItems(set_sub_classes, true, Q, sis_session));
            }*/
            for (SortItem targetTerm: terms) {

                String targetUITerm = dbGen.removePrefix(targetTerm.getLogName());

                Q.reset_name_scope();
                long termIdL = Q.set_current_node_id(targetTerm.getSysId());
                int set_all_classes = Q.get_classes(0);
                Q.reset_set(set_all_classes);

                Q.reset_set(set_h);
                Q.set_intersect(set_all_classes, set_h);
                Q.reset_set(set_all_classes);


                if(Parameters.OnlyTopTermsHoldReferenceId){
                    set_all_classes = Q.get_from_node_by_category(set_all_classes, belongsToHierarchyClass, belongsToHierarchyLink);
                }

                retVals.clear();
                if(Q.bulk_return_nodes(set_all_classes, retVals)!=QClass.APIFail){
                    for(Return_Nodes_Row row:retVals){
                        String targetHierarchy = dbGen.removePrefix(row.get_v1_cls_logicalname());
                        long targetHierarchyIdL = row.get_Neo4j_NodeId();
                        long targetHierarchyRefIdL = row.get_v2_long_referenceId();
                        String targetHierarchyTransliteration = row.get_v3_cls_transliteration();

                        if (termsInfo.containsKey(targetUITerm) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + termIdL, termIdL,targetTerm.getLogNameTransliteration(),targetTerm.getThesaurusReferenceId()));
                            termsInfo.put(targetUITerm, newContainer);
                            allTerms.add(targetUITerm);
                        }
                        termsInfo.get(targetUITerm).descriptorInfo.get(ConstantParameters.topterm_kwd).add(new SortItem(targetHierarchy, targetHierarchyIdL,targetHierarchyTransliteration,targetHierarchyRefIdL));

                    }
                }                   
                Q.free_set(set_all_classes);
            }

            Q.free_set(set_h);
        }



    }

    public void ReadRelatedSources(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            int set_terms, HashMap<String, NodeInfoSortItemContainer> sourcesInfo, ArrayList<String> allSources) {
        //Abandoned Code that filtered only these sources that were referenced
        //It is not expected to see less sources after a scheduled backup (export 2 XML - import from XML)
        /*
        DBGeneral dbGen = new  DBGeneral();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        
        
        StringObject gtFromClassObj = new StringObject();
        StringObject gtLinkObj      = new StringObject();
        StringObject etFromClassObj = new StringObject();
        StringObject etLinkObj      = new StringObject();
        StringObject sourceNoteFromObj = new StringObject();
        StringObject sourceNoteLinkObj = new StringObject();      
        ArrayList<SortItem> referencedSources = new ArrayList<SortItem>(); 
        
        dbGen.getKeywordPair(sessionInstance, ConstantParameters.primary_found_in_kwd, gtFromClassObj, gtLinkObj, Q, sis_session);
        dbGen.getKeywordPair(sessionInstance, ConstantParameters.translations_found_in_kwd, etFromClassObj, etLinkObj, Q, sis_session);
        dbGen.getKeywordPair(sessionInstance, ConstantParameters.source_note_kwd, sourceNoteFromObj, sourceNoteLinkObj, Q, sis_session);
        
        String[] output = {ConstantParameters.id_kwd,ConstantParameters.source_note_kwd}; 
        
        Q.reset_set(set_terms);
        
        int set_gt_source_labels = Q.get_link_from_by_category(set_terms, gtFromClassObj, gtLinkObj);
        Q.reset_set(set_gt_source_labels);
        
        int set_all_referenced_sources = Q.get_to_value(set_gt_source_labels);
        Q.reset_set(set_all_referenced_sources);
        Q.free_set(set_gt_source_labels);
        
        int set_et_source_labels = Q.get_link_from_by_category(set_terms, etFromClassObj, etLinkObj);
        Q.reset_set(set_et_source_labels);
        
        int set_temp = Q.get_to_value(set_et_source_labels);
        Q.reset_set(set_temp);
        Q.free_set(set_et_source_labels);
        
        Q.set_union(set_all_referenced_sources,set_temp);
        Q.reset_set(set_all_referenced_sources);        
        Q.free_set(set_temp);
        
        
        StringObject label = new StringObject();
        IntegerObject sysid = new IntegerObject();
        StringObject sclass = new StringObject(); 
        
        while (Q.retur_full_nodes(set_all_referenced_sources, sysid, label, sclass) != QClass.APIFail) {
        SortItem sourceItem = new SortItem(label.getValue(),sysid.getValue());
        referencedSources.add(sourceItem);
        //sourcesInfo.put(nodeNameObj.getValue(), null);
        }
        
        Q.free_set(set_all_referenced_sources);
        
        for(int i=0 ; i < referencedSources.size(); i++){

        int sourceItemId = referencedSources.get(i).getSysId();
        StringObject targetSourceDBFormatObj = new StringObject(referencedSources.get(i).getLogName());

        String targetSourceUIFormat = dbGen.removePrefix(targetSourceDBFormatObj.getValue());

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" i = " + i + " source =  " +targetSourceUIFormat);

        StringObject commentObject = new StringObject("");
        WTA.GetDescriptorComment(sessionInstance, targetSourceDBFormatObj, commentObject, sourceNoteFromObj, sourceNoteLinkObj);

        NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_SOURCE, output);
        if (commentObject.getValue().length() > 0) {

        newContainer.descriptorInfo.get(ConstantParameters.source_note_kwd).add(new SortItem(commentObject.getValue(),-1));

        }
        //else{
        //    sourcesInfo.put(targetSourceUIFormat, new String(""));
        //}
        newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem(""+sourceItemId,sourceItemId));
        sourcesInfo.put(targetSourceUIFormat,newContainer);

        allSources.add(targetSourceUIFormat);
        }
         */


        ArrayList<SortItem> targetSources = new ArrayList<SortItem>();
        DBGeneral dbGen = new DBGeneral();
        //DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.SourceClass));
        int set_sources = Q.get_instances(0);
        Q.reset_set(set_sources);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_sources, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                SortItem sourceItem = new SortItem(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId());
                targetSources.add(sourceItem);
                //sourcesInfo.put(nodeNameObj.getValue(), null);
            }
        }
        /*
        StringObject label = new StringObject();
        IntegerObject sysid = new IntegerObject();
        StringObject sclass = new StringObject();

        while (Q.retur_full_nodes(set_sources, sysid, label, sclass) != QClass.APIFail) {
            SortItem sourceItem = new SortItem(label.getValue(), sysid.getValue());
            targetSources.add(sourceItem);
            //sourcesInfo.put(nodeNameObj.getValue(), null);
        }
        */
        Q.free_set(set_sources);


        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        StringObject sourceNoteFromObj = new StringObject();
        StringObject sourceNoteLinkObj = new StringObject();
        dbGen.getKeywordPair(selectedThesaurus, ConstantParameters.source_note_kwd, sourceNoteFromObj, sourceNoteLinkObj, Q, sis_session);
        String[] output = {ConstantParameters.id_kwd, ConstantParameters.source_note_kwd};

        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(selectedThesaurus);
        }

        for (int i = 0; i < targetSources.size(); i++) {

            long sourceItemIdL = targetSources.get(i).getSysId();
            StringObject targetSourceDBFormatObj = new StringObject(targetSources.get(i).getLogName());

            String targetSourceUIFormat = dbGen.removePrefix(targetSourceDBFormatObj.getValue());

            StringObject commentObject = new StringObject("");
            TA.GetDescriptorComment(targetSourceDBFormatObj, commentObject, sourceNoteFromObj, sourceNoteLinkObj);

            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_SOURCE, output);
            if (commentObject.getValue().length() > 0) {

                newContainer.descriptorInfo.get(ConstantParameters.source_note_kwd).add(new SortItem(commentObject.getValue(), -1));

            }
            //else{
            //    sourcesInfo.put(targetSourceUIFormat, new String(""));
            //}
            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + sourceItemIdL, sourceItemIdL));
            sourcesInfo.put(targetSourceUIFormat, newContainer);

            allSources.add(targetSourceUIFormat);
        }
        //reset to previous thesaurus name if needed
        if(prevThes.getValue().equals(selectedThesaurus)==false){
            TA.SetThesaurusName(prevThes.getValue());
        }





    }
}
