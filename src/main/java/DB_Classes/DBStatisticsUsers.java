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

import Utils.StringLocaleComparator;
import java.util.*;
import javax.servlet.http.*;
import java.util.Vector;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBStatisticsUsers
-----------------------------------------------------------------------
  class for quering DB for statistics of users
----------------------------------------------------------------------*/
public class DBStatisticsUsers {

    /*----------------------------------------------------------------------
                        GetTotalNumber() - instances of AAAEditor
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of DBStatisticsUsers of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumber(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        int set_e = GetAllUsers(Q, sis_session, SessionUserInfo);
        int totalUsersSetCard = Q.set_get_card(set_e);
        Q.free_set(set_e);
        
        return totalUsersSetCard;                
        
    }    
    
    /*----------------------------------------------------------------------
                        GetNumberOfTermsCreatedAndLastModifiedPerUser()
    -----------------------------------------------------------------------
    OUTPUT: - Vector<String> UsersVector: a Vector with the existing Editors of current thesaurus (DB encoded)
            - Vector<IntegerObject> TermsCreatedCountVector: parallel Vector with the 
                             cardinality of the terms created by the corresponding user
            - Vector<IntegerObject> TermsLastModifiedCountVector: parallel Vector with the 
                             cardinality of the terms being last modified by the corresponding user
    ------------------------------------------------------------------------*/
    public void GetNumberOfTermsCreatedAndLastModifiedPerUser(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo, Locale targetLocale, Vector<String> UsersVector, Vector<IntegerObject> TermsCreatedCountVector, Vector<IntegerObject> TermsLastModifiedCountVector) {
        DBFilters dbf = new DBFilters();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        // get all Users
        int set_e = GetAllUsers(Q, sis_session, SessionUserInfo);
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_e, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                UsersVector.add(row.get_v1_cls_logicalname());            
            }
        }
        /*StringObject l_name = new StringObject();
        while ((Q.retur_nodes(set_e, l_name)) != QClass.APIFail) {
            UsersVector.add(l_name.getValue());
            l_name = new StringObject();
        } */       
        Q.free_set(set_e);
        
        // copy UsersVector to UsersVectorSortedUI (sorted and UI encoded)
        DBGeneral dbGen = new DBGeneral();
        Vector<String> UsersVectorSortedUI = new Vector<String>();      
        
        int UsersVectorSize = UsersVector.size();
        for (int i = 0; i < UsersVectorSize; i++) {
            String currentUser = UsersVector.get(i);
            UsersVectorSortedUI.add(currentUser);
        }
        Collections.sort(UsersVectorSortedUI, new StringLocaleComparator(targetLocale)); 
        // copy back UsersVectorSortedUI to UsersVector (DB encoded)
        UsersVector.clear();
        int UsersVectorSortedUISize = UsersVectorSortedUI.size();
        for (int i = 0; i < UsersVectorSortedUISize; i++) {
            String currentUser = UsersVectorSortedUI.get(i);
            UsersVector.add(currentUser);
        }                                    
        
        // looking for AAAHierarchyTerm->aaa_created_by and aaa_modified_by
        // looking for AAAHierarchyTerm
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(Q,sis_session.getValue(),new StringObject(SessionUserInfo.selectedThesaurus), thesHierarchyTerm);          
        // looking for aaa_created_by
        StringObject thes_created_by = new StringObject();
        dbtr.getThesaurusCategory_created_by(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue(), thes_created_by);
        // looking for aaa_modified_by
        StringObject thes_modified_by = new StringObject();
        dbtr.getThesaurusCategory_modified_by(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue(), thes_modified_by);
        
        UsersVectorSize = UsersVector.size();
        // for each user
        for (int i = 0; i < UsersVectorSize; i++) {
            String currentUser = UsersVector.get(i);
            // ------------------- get current user's created terms count -------------------
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentUser));            
            int linksSet = Q.get_link_to_by_category(0, thesHierarchyTerm, thes_created_by);
            Q.reset_set(linksSet);            
            int TermsCreatedSet = Q.get_from_value(linksSet);
            // FILTER terms depending on user group
            TermsCreatedSet = dbf.FilterTermsResults(SessionUserInfo, TermsCreatedSet, Q, sis_session);
            Q.reset_set(TermsCreatedSet);            
            int TermsCreatedSetCard = Q.set_get_card(TermsCreatedSet);
            Q.free_set(linksSet);
            Q.free_set(TermsCreatedSet);            
            TermsCreatedCountVector.add(new IntegerObject(TermsCreatedSetCard));
            // ------------------- get current user's last modified terms count -------------------
            // set_current_node(currentUser) AGAIN because DBFilters methods change the name scope!
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentUser));                        
            int linksSet2 = Q.get_link_to_by_category(0, thesHierarchyTerm, thes_modified_by);
            Q.reset_set(linksSet2);            
            int TermsLastModifiedSet = Q.get_from_value(linksSet2);
            // FILTER terms depending on user group
            TermsLastModifiedSet = dbf.FilterTermsResults(SessionUserInfo, TermsLastModifiedSet, Q, sis_session);
            Q.reset_set(TermsLastModifiedSet);            
            int TermsLastModifiedSetCard = Q.set_get_card(TermsLastModifiedSet);
            Q.free_set(linksSet2);
            Q.free_set(TermsLastModifiedSet);            
            TermsLastModifiedCountVector.add(new IntegerObject(TermsLastModifiedSetCard));            
        }
    }                
    
    /*----------------------------------------------------------------------
                        GetAllUsers()
    -----------------------------------------------------------------------
    OUTPUT: - a set with all <thes>Editor instances of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetAllUsers(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();        
        Q.reset_name_scope();
        // looking for EKTEditor
        StringObject Editor = new StringObject();
        dbtr.getThesaurusClass_Editor(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),Editor);
        // get instances of EKTEditor 
        Q.reset_name_scope();
        Q.set_current_node(Editor);
        int set_e = Q.get_instances(0);
        
        return set_e;
    }                
    
}
