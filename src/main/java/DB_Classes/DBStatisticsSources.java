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
import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Utils.ConstantParameters;

import Utils.StringLocaleComparator;
import java.util.*;
import java.util.Vector;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBStatisticsSources
-----------------------------------------------------------------------
  class for quering DB for statistics of sources
----------------------------------------------------------------------*/
public class DBStatisticsSources {

    /*----------------------------------------------------------------------
                        GetTotalNumber()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of sources of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumber(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        int set_s = GetAllSources(Q, sis_session, SessionUserInfo);
        int totalSourcesSetCard = Q.set_get_card(set_s);
        Q.free_set(set_s);
        
        return totalSourcesSetCard;                
    } 
    
    /*----------------------------------------------------------------------
                        GetNumberOfTermsPerSourceGrEn()
    -----------------------------------------------------------------------
    OUTPUT: - Vector<String> SourcesVector: a Vector with the existing Sources (DB encoded)
            - Vector<IntegerObject> TermsSourceGrCountVector: parallel Vector with the 
                             cardinality of the terms having as Greek Source the corresponding source
            - Vector<IntegerObject> TermsSourceEnCountVector: parallel Vector with the 
                             cardinality of the terms having as English Source the corresponding source
    ------------------------------------------------------------------------*/
    public void GetNumberOfTermsPerSourceGrEn(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale, Vector<String> SourcesVector, Vector<IntegerObject> TermsSourceGrCountVector, Vector<IntegerObject> TermsSourceEnCountVector) {
        DBFilters dbf = new DBFilters();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        // get all Sources
        int set_s = GetAllSources(Q, sis_session, SessionUserInfo);
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
	if(Q.bulk_return_nodes(set_s, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                SourcesVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*StringObject l_name = new StringObject();
        while ((Q.retur_nodes(set_s, l_name)) != QClass.APIFail) {
            SourcesVector.add(l_name.getValue());
            l_name = new StringObject();
        } */       
        Q.free_set(set_s);
        
        // copy SourcesVector to SourcesVectorSortedUI (sorted and UI encoded)
        DBGeneral dbGen = new DBGeneral();
        Vector<String> SourcesVectorSortedUI = new Vector<String>();      
        
        int SourcesVectorSize = SourcesVector.size();
        for (int i = 0; i < SourcesVectorSize; i++) {
            String currentSource = SourcesVector.get(i);
            SourcesVectorSortedUI.add(currentSource);
        }
        Collections.sort(SourcesVectorSortedUI, new StringLocaleComparator(targetLocale)); 
        // copy back SourcesVectorSortedUI to SourcesVector (DB encoded)
        SourcesVector.clear();
        int SourcesVectorSortedUISize = SourcesVectorSortedUI.size();
        for (int i = 0; i < SourcesVectorSortedUISize; i++) {
            String currentSource = SourcesVectorSortedUI.get(i);
            SourcesVector.add(currentSource);
        }                            
                
        // looking for AAAHierarchyTerm->aaa_primary_found_in and aaa_translations_found_in
        // looking for AAAHierarchyTerm
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(Q,sis_session.getValue(),new StringObject(SessionUserInfo.selectedThesaurus), thesHierarchyTerm);          
        // looking for aaa_primary_found_in
        StringObject thes_primary_found_in = new StringObject();
        dbtr.getThesaurusCategory_primary_found_in(SessionUserInfo.selectedThesaurus, thes_primary_found_in);
        // looking for aaa_translations_found_in
        StringObject thes_translations_found_in = new StringObject();
        dbtr.getThesaurusCategory_translations_found_in(SessionUserInfo.selectedThesaurus, thes_translations_found_in);
        
        SourcesVectorSize = SourcesVector.size();
        // for each source
        for (int i = 0; i < SourcesVectorSize; i++) {
            String currentSource = SourcesVector.get(i);
            // ------------------- get current sources's terms count having as Greek Source the corresponding source -------------------
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentSource));            
            int linksSet = Q.get_link_to_by_category(0, thesHierarchyTerm, thes_primary_found_in);
            Q.reset_set(linksSet);            
            int TermsSourceGrSet = Q.get_from_value(linksSet);
            // FILTER terms depending on user group
            TermsSourceGrSet = dbf.FilterTermsResults(SessionUserInfo, TermsSourceGrSet, Q, sis_session);
            Q.reset_set(TermsSourceGrSet);            
            int TermsSourceGrSetCard = Q.set_get_card(TermsSourceGrSet);
            Q.free_set(linksSet);
            Q.free_set(TermsSourceGrSet);            
            TermsSourceGrCountVector.add(new IntegerObject(TermsSourceGrSetCard));
            // ------------------- get current user's last modified terms count -------------------
            // set_current_node(currentSource) AGAIN because DBFilters methods change the name scope!
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentSource));                        
            int linksSet2 = Q.get_link_to_by_category(0, thesHierarchyTerm, thes_translations_found_in);
            Q.reset_set(linksSet2);            
            int TermsSourceEnSet = Q.get_from_value(linksSet2);
            // FILTER terms depending on user group
            TermsSourceEnSet = dbf.FilterTermsResults(SessionUserInfo, TermsSourceEnSet, Q, sis_session);
            Q.reset_set(TermsSourceEnSet);            
            int TermsSourceEnSetCard = Q.set_get_card(TermsSourceEnSet);
            Q.free_set(linksSet2);
            Q.free_set(TermsSourceEnSet);            
            TermsSourceEnCountVector.add(new IntegerObject(TermsSourceEnSetCard));            
        }
    }                    
    
    /*----------------------------------------------------------------------
                        GetAllSources()
    -----------------------------------------------------------------------
    OUTPUT: - a set with all Source instances
    ------------------------------------------------------------------------*/
    public int GetAllSources(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        
        StringObject SourceClassObj = new StringObject(ConstantParameters.SourceClass);
        
        Q.reset_name_scope();
        Q.set_current_node(SourceClassObj);
        int set_s = Q.get_all_instances(0);
        Q.reset_set(set_s);        
        
        return set_s;
    }                    
    
}
