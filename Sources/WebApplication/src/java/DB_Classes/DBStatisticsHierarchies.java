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
                            DBStatisticsHierarchies
-----------------------------------------------------------------------
  class for quering DB for statistics of hierarchies
----------------------------------------------------------------------*/
public class DBStatisticsHierarchies {

    /*----------------------------------------------------------------------
                        GetTotalNumber()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of hierarchies of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumber(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        int set_h = GetAllHierarchies(Q, sis_session, SessionUserInfo);
        int totalHierarchiesSetCard = Q.set_get_card(set_h);
        Q.free_set(set_h);
        
        return totalHierarchiesSetCard;        
    }    
    
    /*----------------------------------------------------------------------
                        GetNumberOfTermsAndTranslationsPerHierarchy()
    -----------------------------------------------------------------------
    OUTPUT: - Vector<String> HierarchiesVector: a sorted Vector with the existing hierarchies (DB encoded)
            - Vector<IntegerObject> HierarchiesTermsCountVector: parallel Vector with the 
                             cardinality of the terms of the corresponding hierarchy
            - Vector<IntegerObject> HierarchiesNonPrefferedTermsCountVector: parallel Vector with the 
                             cardinality of the NonPreffered terms of the corresponding hierarchy (XA)
            - Vector<IntegerObject> HierarchiesEnglishWordsCountVector: parallel Vector with the 
                             cardinality of the EnglishWords of the corresponding hierarchy
            - Vector<IntegerObject> HierarchiesNonPrefferedEnglishWordsCountVector: parallel Vector with the 
                             cardinality of the NonPreffered EnglishWords of the corresponding hierarchy (UF)
    ------------------------------------------------------------------------*/
    public void GetNumberOfTermsAndTranslationsPerHierarchy(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo,
            Locale targetLocale, Vector<String> HierarchiesVector, 
            Vector<IntegerObject> HierarchiesTermsCountVector, 
            Vector<IntegerObject> HierarchiesNonPrefferedTermsCountVector, 
            Vector<IntegerObject> HierarchiesTranslationsCountVector,
            Vector<IntegerObject> HierarchiesNonPrefferedEnglishWordsCountVector) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        // get all Hierarchies
        int set_h = GetAllHierarchies(Q, sis_session, SessionUserInfo);
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_h, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                HierarchiesVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*StringObject l_name = new StringObject();
        while ((Q.retur_nodes(set_h, l_name)) != QClass.APIFail) {
            HierarchiesVector.add(l_name.getValue());
            l_name = new StringObject();
        } */       
        Q.free_set(set_h);
        
        // copy HierarchiesVector to HierarchiesVectorSortedUI (sorted and UI encoded)
        DBGeneral dbGen = new DBGeneral();
        Vector<String> HierarchiesVectorSortedUI = new Vector<String>();      
        
        int HierarchiesVectorSize = HierarchiesVector.size();
        for (int i = 0; i < HierarchiesVectorSize; i++) {
            String currentHierarchy = HierarchiesVector.get(i);
            HierarchiesVectorSortedUI.add(currentHierarchy);
        }
        Collections.sort(HierarchiesVectorSortedUI, new StringLocaleComparator(targetLocale)); 
        // copy back HierarchiesVectorSortedUI to HierarchiesVector (DB encoded)
        HierarchiesVector.clear();
        int HierarchiesVectorSortedUISize = HierarchiesVectorSortedUI.size();
        for (int i = 0; i < HierarchiesVectorSortedUISize; i++) {
            String currentHierarchy = HierarchiesVectorSortedUI.get(i);
            HierarchiesVector.add(currentHierarchy);
        }            

        // looking for AAATerm and AAA_translation
        StringObject thes_term = new StringObject();
        StringObject thes_translation = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, thes_term, thes_translation, Q, sis_session);

         // looking for AAAHierarchyTerm and AAA_UF
        StringObject thes_HierarchyTerm = new StringObject();
        StringObject thes_UF = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_kwd, thes_HierarchyTerm, thes_UF, Q, sis_session);
        
        // looking for AAA_uk_uf
        StringObject thes_uf_translations = new StringObject();
        dbtr.getThesaurusCategory_uf_translations(SessionUserInfo.selectedThesaurus, thes_uf_translations);
        
        HierarchiesVectorSize = HierarchiesVector.size();
        // for each hierarchy
        for (int i = 0; i < HierarchiesVectorSize; i++) {
            String currentHierarchy = HierarchiesVector.get(i);
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentHierarchy));            
            // get current hierarchy's terms count
            int hierarchyTerms = Q.get_all_instances(0);
            Q.reset_set(hierarchyTerms);
            int hierarchyTermsCard = Q.set_get_card(hierarchyTerms);
            //if (hierarchyTermsCard > 1) hierarchyTermsCard--; // -1 so as to exclude Top Term of current hierarchy
            HierarchiesTermsCountVector.add(new IntegerObject(hierarchyTermsCard)); 
            // get current hierarchy's NonPreffered terms count
            int linksSet = Q.get_link_from_by_category(hierarchyTerms, thes_HierarchyTerm, thes_UF);
            Q.reset_set(linksSet);
            int NonPrefferedTermsSet = Q.get_to_value(linksSet);
            Q.reset_set(NonPrefferedTermsSet);
            int NonPrefferedTermsSetSetCard = Q.set_get_card(NonPrefferedTermsSet);
            //if (NonPrefferedTermsSetSetCard > 1) NonPrefferedTermsSetSetCard--; // -1 so as to exclude Top Term of current hierarchy
            Q.free_set(linksSet);
            Q.free_set(NonPrefferedTermsSet);
            HierarchiesNonPrefferedTermsCountVector.add(new IntegerObject(NonPrefferedTermsSetSetCard));
            // get current hierarchy's EnglishWords count
            linksSet = Q.get_link_from_by_category(hierarchyTerms, thes_term, thes_translation);
            Q.reset_set(linksSet);
            int TranslationsSet = Q.get_to_value(linksSet);
            Q.reset_set(TranslationsSet);
            int TranslationsSetCard = Q.set_get_card(TranslationsSet);
            //if (TranslationsSetCard > 1) TranslationsSetCard--; // -1 so as to exclude Top Term of current hierarchy
            Q.free_set(linksSet);
            Q.free_set(TranslationsSet);
            HierarchiesTranslationsCountVector.add(new IntegerObject(TranslationsSetCard));
            // get current hierarchy's NonPreffered EnglishWords count
            linksSet = Q.get_link_from_by_category(hierarchyTerms, thes_HierarchyTerm, thes_uf_translations);
            Q.reset_set(linksSet);
            int NonPrefferedTranslationWordsSet = Q.get_to_value(linksSet);
            Q.reset_set(NonPrefferedTranslationWordsSet);
            int NonPrefferedTranslationsSetCard = Q.set_get_card(NonPrefferedTranslationWordsSet);
            //if (NonPrefferedEnglishWordsSetCard > 1) NonPrefferedEnglishWordsSetCard--; // -1 so as to exclude Top Term of current hierarchy
            Q.free_set(linksSet);
            Q.free_set(NonPrefferedTranslationsSetCard);
            HierarchiesNonPrefferedEnglishWordsCountVector.add(new IntegerObject(NonPrefferedTranslationsSetCard));
        
            Q.free_set(hierarchyTerms);
        }
    }            
    
    /*----------------------------------------------------------------------
                        GetAllHierarchies()
    -----------------------------------------------------------------------
    OUTPUT: - a set with all hierarchies of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetAllHierarchies(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();        
        Q.reset_name_scope();
        // looking for EKTHierarchyClass
        StringObject Hierarchies = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),Hierarchies);
        // get instances of EKTHierarchyClass 
        Q.reset_name_scope();
        Q.set_current_node(Hierarchies);
        int set_h = Q.get_instances(0);
        // get instances of EKTObsoleteHierarchy 
        StringObject ObsoleteHierarchies = new StringObject();
        dbtr.getThesaurusClass_ObsoleteHierarchy(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),ObsoleteHierarchies);
        Q.reset_name_scope();
        Q.set_current_node(ObsoleteHierarchies);
        int set_oh = Q.get_instances(0);
        Q.set_union(set_h, set_oh);
        Q.free_set(set_oh);
        
        // FILTER hierarchies depending on user group
        DBFilters dbf = new DBFilters();
        set_h = dbf.FilterHierResults(SessionUserInfo, set_h, Q, sis_session);
        
        return set_h;
    }            
}
