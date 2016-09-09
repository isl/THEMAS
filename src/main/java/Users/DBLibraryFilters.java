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
package Users;



import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBGeneral;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import java.io.*;

import neo4j_sisapi.*;


/*---------------------------------------------------------------------
                            DBLibraryFilters
-----------------------------------------------------------------------
  Class for handling the DB filtering for users of group "LIBRARY"
----------------------------------------------------------------------*/
public class DBLibraryFilters {
    
    /*---------------------------------------------------------------------
                            DBLibraryFilters()
    ----------------------------------------------------------------------*/                
    public DBLibraryFilters() {
    }    
    
    /*---------------------------------------------------------------------
                    TermIsEditable()
    -----------------------------------------------------------------------
    INPUT: - targetTerm: the term to be checked
           - userLogicalName: the logical name of the current LIBRARY user (Person`xxx)
    OUTPUT: - true in case the targetTerm is 
                a. of status AAAStatusForInsertion
                b. created by current LIBRARY user
              otherwise, it returns false
    ----------------------------------------------------------------------*/
    public boolean TermIsEditable(String selectedThesaurus, StringObject targetTerm, StringObject userLogicalName, QClass Q, IntegerObject sis_session) {
        int SISApiSession = sis_session.getValue();
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbg = new DBGeneral();
        
        // looking for AAAStatusForInsertion
        StringObject thesStatusForInsertion = new StringObject();
        dbtr.getThesaurusClass_StatusForInsertion(selectedThesaurus, thesStatusForInsertion);
        
        // check if targetTerm is of status AAAStatusForInsertion
        boolean isOfStatusForInsertion = dbg.NodeBelongsToClass(targetTerm, thesStatusForInsertion, false, Q, sis_session);
        if (isOfStatusForInsertion == false) {
            return false;
        }
        
        // check if targetTerm is created by current LIBRARY user (userLogicalName - Person`xxx)
        // looking for AAAHierarchyTerm
        StringObject thes_HierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, SISApiSession, thes_HierarchyTerm);
        // looking for aaa_created_by
        StringObject thes_created_by = new StringObject();
        dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, SISApiSession, thes_created_by);
        Q.reset_name_scope();
        Q.set_current_node( targetTerm);
        int linkSet = Q.get_link_from_by_category(0, thes_HierarchyTerm, thes_created_by);
        Q.reset_set(linkSet);
        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        boolean termIsEditable = false;
        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if(Q.bulk_return_link(linkSet, retVals)!=QClass.APIFail){
            for(Return_Link_Row row:retVals){
                if (userLogicalName.getValue().equals(row.get_v3_cmv().getString())) {
                    termIsEditable = true;
                    break;
                }
            }
        }
        /*
        while (Q.retur_link(linkSet, cls, label, cmv) != QClass.APIFail) {
            if (userLogicalName.getValue().equals(cmv.getString())) {
                termIsEditable = true;
                break;
            }
        }*/
        Q.free_set(linkSet);
        
        return termIsEditable;
    }                                        
}


