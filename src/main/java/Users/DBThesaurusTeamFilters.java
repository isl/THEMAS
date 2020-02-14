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
package Users;

import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Utils.Parameters;
import java.util.ArrayList;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.Return_Link_Row;
import neo4j_sisapi.StringObject;

/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class DBThesaurusTeamFilters {
    
    public DBThesaurusTeamFilters(){
        
    }
    
    public boolean TermIsEditable(String selectedThesaurus, StringObject targetTerm, StringObject userLogicalName, QClass Q, IntegerObject sis_session) {
        int SISApiSession = sis_session.getValue();
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbg = new DBGeneral();
        
        // looking for AAAStatusForApproved
        StringObject thesStatusForApproved = new StringObject();
        dbtr.getThesaurusClass_StatusApproved(selectedThesaurus, thesStatusForApproved);
        
        StringObject thesStatusForInsertion = new StringObject();
        dbtr.getThesaurusClass_StatusForInsertion(selectedThesaurus, thesStatusForInsertion);
        
        // check if targetTerm is of status AAAStatusForApproved
        boolean isOfStatusForApproved = dbg.NodeBelongsToClass(targetTerm, thesStatusForApproved, false, Q, sis_session);
        if (isOfStatusForApproved) {
            return false;
        }
        //in case the term is of status for insertion then (as this function is 
        //supposed to be called only for thesaurus team users) editing should be 
        //permitted
        boolean isOfStatusForInsertion = dbg.NodeBelongsToClass(targetTerm, thesStatusForInsertion, false, Q, sis_session);
        if (isOfStatusForInsertion) {
            return true;
        }
        
        boolean termIsEditable = false;
        
        
            termIsEditable = false;
            // check if targetTerm is created by current LIBRARY user (userLogicalName - Person`xxx)
            // looking for AAAHierarchyTerm
            StringObject thes_HierarchyTerm = new StringObject();
            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, SISApiSession, thes_HierarchyTerm);
            // looking for aaa_created_by
            StringObject thes_created_by = new StringObject();
            dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, SISApiSession, thes_created_by);
            Q.reset_name_scope();
            Q.set_current_node(targetTerm);
            int linkSet = Q.get_link_from_by_category(0, thes_HierarchyTerm, thes_created_by);
            Q.reset_set(linkSet);

            ArrayList<Return_Link_Row> retVals = new ArrayList<>();
            if(Q.bulk_return_link(linkSet, retVals)!=QClass.APIFail){
                for(Return_Link_Row row:retVals){
                    if (userLogicalName.getValue().equals(row.get_v3_cmv().getString())) {
                        termIsEditable = true;
                        break;
                    }
                }
            }            
            Q.free_set(linkSet);
            /* If it is for insertion but user changes the status to under construction 
            in order to actually perform this editing then he looses the rigth to edit
            because it is under construction and he is not the same user who created it.
            but last editing user has changed.
            
            But this also produces other problems since actual editing is performed
            only when under construction. and when a user changes this then 
            
            For this reason 
            */
            /*
            if(!termIsEditable){
                dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, SISApiSession, thes_HierarchyTerm);
                // looking for aaa_created_by
                StringObject thes_last_edited_by = new StringObject();
                dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, SISApiSession, thes_last_edited_by);
                Q.reset_name_scope();
                Q.set_current_node(targetTerm);
                linkSet = Q.get_link_from_by_category(0, thes_HierarchyTerm, thes_created_by);
                Q.reset_set(linkSet);

                retVals.clear();
                if(Q.bulk_return_link(linkSet, retVals)!=QClass.APIFail){
                    for(Return_Link_Row row:retVals){
                        if (userLogicalName.getValue().equals(row.get_v3_cmv().getString())) {
                            termIsEditable = true;
                            break;
                        }
                    }
                }            
                Q.free_set(linkSet);
            }*/
        
        return termIsEditable;
    }     
}
