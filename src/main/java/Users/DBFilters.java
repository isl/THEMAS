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

import Utils.Parameters;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import java.io.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBFilters
-----------------------------------------------------------------------
  Class for handling the DB filtering for users of ALL groups
----------------------------------------------------------------------*/
public class DBFilters {
    
    /*---------------------------------------------------------------------
                            DBFilters()
    ----------------------------------------------------------------------*/                
    public DBFilters() {
    }    
    
    /*---------------------------------------------------------------------
                    FilterTermsResults()
    ----------------------------------------------------------------------*/
    public int FilterTermsResults(UserInfoClass SessionUserInfo, int termsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            termsSet = dbrf.FilterTermsResults(SessionUserInfo, termsSet, Q, sis_session);
        }        
        
        return termsSet;
    }    
    
    /*---------------------------------------------------------------------
                    FilterTermsResults()
    ----------------------------------------------------------------------*/
    public void FilterTermsResultsLinks(UserInfoClass SessionUserInfo, int termsLinksSetFrom, int termsLinksSetTo, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            dbrf.FilterTermsResultsLinks(SessionUserInfo.selectedThesaurus, termsLinksSetFrom, termsLinksSetTo, Q, sis_session);
        }        
        
        return;
    }   
    
    /*---------------------------------------------------------------------
                    FilterHierResults()
    ----------------------------------------------------------------------*/
    public int FilterHierResults(UserInfoClass SessionUserInfo, int hiersSet, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            hiersSet = dbrf.FilterHierResults(SessionUserInfo, hiersSet, Q, sis_session);
        }        
        
        return hiersSet;
    }        
    
    /*---------------------------------------------------------------------
                    FilterHierarchy()
    ----------------------------------------------------------------------*/
    public boolean FilterHierarchy(UserInfoClass SessionUserInfo, StringObject hierarchy, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            return dbrf.FilterHierarchy(SessionUserInfo, hierarchy, Q, sis_session);
        }
        return true;
    }            
    
    /*---------------------------------------------------------------------
                    FilterFacetResults()
    ----------------------------------------------------------------------*/
    public int FilterFacetResults(UserInfoClass SessionUserInfo, int facetsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            facetsSet = dbrf.FilterFacetResults(SessionUserInfo, facetsSet, Q, sis_session);
        }
        
        return facetsSet;
    }            
                    
    /*---------------------------------------------------------------------
                    FilterToValuesOfTerms()
    ----------------------------------------------------------------------*/
    public int FilterToValuesOfTerms(UserInfoClass SessionUserInfo, int ToValuesOfTermsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            ToValuesOfTermsSet = dbrf.FilterToValuesOfTerms(SessionUserInfo, ToValuesOfTermsSet, Q, sis_session);
        }
        
        return ToValuesOfTermsSet;
    }                            
                                  
    
    /*---------------------------------------------------------------------
                    FilterBTLinksSet()
    ----------------------------------------------------------------------*/
    public int FilterBTLinksSet(UserInfoClass SessionUserInfo, int BTLinksSet, QClass Q, IntegerObject sis_session) {
        
        // in case of reader user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true) {
            DBReaderFilters dbrf = new DBReaderFilters();
            BTLinksSet = dbrf.FilterBTLinksSet(SessionUserInfo, BTLinksSet, Q, sis_session);
        }
        
        return BTLinksSet;
    }                                
    
    /*---------------------------------------------------------------------
                    TermIsEditable()
    ----------------------------------------------------------------------*/
    public boolean TermIsEditable(UserInfoClass SessionUserInfo, StringObject targetTerm, StringObject userLogicalName, QClass Q, IntegerObject sis_session) {
        
        boolean termIsEditable = true;
        // in case of LIBRARY user
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Library) == true) {
            DBLibraryFilters dblf = new DBLibraryFilters();
            termIsEditable = dblf.TermIsEditable(SessionUserInfo.selectedThesaurus, targetTerm, userLogicalName, Q, sis_session);
        }
        
        if(Parameters.ThesTeamEditOnlyCreatedByTerms){
            //ThesTeamEditOnlyCreatedByTerms
            if(SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_ThesaurusTeam)==true){
                DBThesaurusTeamFilters dbteamf = new DBThesaurusTeamFilters();
                termIsEditable = dbteamf.TermIsEditable(SessionUserInfo.selectedThesaurus, targetTerm, userLogicalName, Q, sis_session);
            }
        }
        
        return termIsEditable;
    }                                    
    
    /*---------------------------------------------------------------------
                    GetDefaultStatusForTermCreation()
    ----------------------------------------------------------------------*/
    public String GetDefaultStatusForTermCreation(UserInfoClass SessionUserInfo) {
        
            
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_ThesaurusTeam) == true) {
            return Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction,SessionUserInfo);
        }
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_ThesaurusCommittee) == true) {
            return Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction,SessionUserInfo);
        }        
                
        return Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion,SessionUserInfo);
    }                                        
}


