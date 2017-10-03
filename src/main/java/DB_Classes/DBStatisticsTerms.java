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
import Utils.Parameters;
import java.util.ArrayList;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBStatisticsTerms
-----------------------------------------------------------------------
  class for quering DB for statistics of terms
----------------------------------------------------------------------*/
public class DBStatisticsTerms {

    /*----------------------------------------------------------------------
                        GetTotalNumber()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of terms of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumber(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        int totalTermsSet = GetAllTerms(Q, sis_session, SessionUserInfo);
        Q.reset_set(totalTermsSet);
        int totalTermsSetCard = Q.set_get_card(totalTermsSet);
        Q.free_set(totalTermsSet);
        
        return totalTermsSetCard;
    }    
    
    /*----------------------------------------------------------------------
                        GetTotalNumberXA()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of instances of class UsedForTerm of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumberXA(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();        
        // looking for AAAUsedForTerm
        StringObject thes_UsedForTerm = new StringObject();
        dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue(), thes_UsedForTerm);
        
        Q.reset_name_scope();
        Q.set_current_node(thes_UsedForTerm); 
        int XA_set = Q.get_all_instances(0);
        Q.reset_set(XA_set);
        int totalTermsXASetCard = Q.set_get_card(XA_set);
        Q.free_set(XA_set);
        
        return totalTermsXASetCard;
    }        
    
    /*----------------------------------------------------------------------
                        GetTotalNumberPrefferedAO()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of the to-values of the links pointing from all 
              terms of current thesaurus under category AAATerm->AAA_translation
    ------------------------------------------------------------------------*/
    public int GetTotalNumberPrefferedTranslations(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        
        
        int totalTermsSet = GetAllTerms(Q, sis_session, SessionUserInfo);

        // looking for AAATerm and AAA_translation
        StringObject thes_term = new StringObject();
        StringObject thes_translation = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, thes_term, thes_translation, Q, sis_session);

        int linksSet = Q.get_link_from_by_category(totalTermsSet, thes_term, thes_translation);
        Q.free_set(totalTermsSet);
        Q.reset_set(linksSet);
        int toValues = Q.get_to_value(linksSet);
        Q.free_set(linksSet);
        Q.reset_set(toValues);
        int totalPrefferedTranslations = Q.set_get_card(toValues);
        Q.free_set(toValues);
        
        return totalPrefferedTranslations;
    }            
    
    /*----------------------------------------------------------------------
                        GetTotalNumberNonPrefferedAO()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of the to-values of the links pointing from all 
              terms of current thesaurus under category AAAHierarchyTerm->AAA_uk_uf
    ------------------------------------------------------------------------*/
    public int GetTotalNumberNonPrefferedTranslations(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        
        
        int totalTermsSet = GetAllTerms(Q, sis_session, SessionUserInfo);
        // looking for AAAHierarchyTerm --> uf_translations
        StringObject thes_HierarchyTerm = new StringObject();
        StringObject thes_uf_translations = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, thes_HierarchyTerm, thes_uf_translations, Q, sis_session);
        


        int ret = Q.reset_name_scope();
        Q.reset_set(totalTermsSet);
        int card1= Q.set_get_card(totalTermsSet);
        int linksSet = Q.get_link_from_by_category(totalTermsSet, thes_HierarchyTerm, thes_uf_translations);



        Q.reset_set(linksSet);
        int toValues = Q.get_to_value(linksSet);

        Q.reset_set(toValues);
        int totalNonPrefferedTranslations = Q.set_get_card(toValues);

        Q.free_set(linksSet);
        Q.free_set(totalTermsSet);
        Q.free_set(toValues);
        
        return totalNonPrefferedTranslations;
    }                
    
    /*----------------------------------------------------------------------
                        GetAllTerms()
    -----------------------------------------------------------------------
    OUTPUT: - a set with all terms of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetAllTerms(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBGeneral dbGen = new DBGeneral();
        int index = Parameters.CLASS_SET.indexOf("TERM");
              
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);
        
        Q.reset_name_scope();
        int totalTermsSet = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
        
        // FILTER terms depending on user group
        DBFilters dbf = new DBFilters();
        totalTermsSet = dbf.FilterTermsResults(SessionUserInfo, totalTermsSet, Q, sis_session);
        
        Q.reset_set(totalTermsSet);        
        return totalTermsSet;
    }                
    
}
