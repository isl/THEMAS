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
import java.util.ArrayList;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBStatisticsFacets
-----------------------------------------------------------------------
  class for quering DB for statistics of facets
----------------------------------------------------------------------*/
public class DBStatisticsFacets {

    /*----------------------------------------------------------------------
                        GetTotalNumber()
    -----------------------------------------------------------------------
    OUTPUT: - total cardinality of facets of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetTotalNumber(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        int set_f = GetAllFacets(Q, sis_session, SessionUserInfo);
        int totalFacetsSetCard = Q.set_get_card(set_f);
        Q.free_set(set_f);
        
        return totalFacetsSetCard;
    }    
    
    /*----------------------------------------------------------------------
              GetNumberOfHierarchiesAndTermsAndEnglishWordsPerFacet()
    -----------------------------------------------------------------------
    OUTPUT: - ArrayList<String> FacetsVector: a sorted Vector with the existing facets (DB encoded)
            - ArrayList<IntegerObject> FacetsHierarciesCountVector: parallel Vector with the 
                             cardinality of the hierarcies of the corresponding facet
            - ArrayList<IntegerObject> FacetsTermsCountVector: parallel Vector with the 
                             cardinality of the terms of the corresponding facet
            - ArrayList<IntegerObject> FacetsNonPrefferedTermsCountVector: parallel Vector with the 
                             cardinality of the NonPreffered terms of the corresponding facet (XA)
            - ArrayList<IntegerObject> FacetsEnglishWordsCountVector: parallel Vector with the 
                             cardinality of the EnglishWords of the corresponding facet
            - ArrayList<IntegerObject> FacetsNonPrefferedEnglishWordsCountVector: parallel Vector with the 
                             cardinality of the EnglishWords of the corresponding facet (UF)
    ------------------------------------------------------------------------*/
    public void GetNumberOfHierarchiesAndTermsAndTranslationsPerFacet(QClass Q, IntegerObject sis_session,
            UserInfoClass SessionUserInfo, Locale targetLocale,
            ArrayList<String> FacetsVector, ArrayList<IntegerObject> FacetsHierarciesCountVector, 
            ArrayList<IntegerObject> FacetsTermsCountVector, ArrayList<IntegerObject> FacetsNonPrefferedTermsCountVector, 
            ArrayList<IntegerObject> FacetsEnglishWordsCountVector, ArrayList<IntegerObject> FacetsNonPrefferedEnglishWordsCountVector) {
        DBFilters dbf = new DBFilters();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //THEMASUserInfo SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        // get all Facets
        int set_f = GetAllFacets(Q, sis_session, SessionUserInfo);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
	if(Q.bulk_return_nodes(set_f, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                FacetsVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*
        StringObject l_name = new StringObject();
        while ((Q.retur_nodes(set_f, l_name)) != QClass.APIFail) {
            FacetsVector.add(l_name.getValue());
            l_name = new StringObject();
        } */       
        Q.free_set(set_f);
        
        // copy FacetsVector to FacetsVectorSortedUI (sorted and UI encoded)
        DBGeneral dbGen = new DBGeneral();
        ArrayList<String> FacetsVectorSortedUI = new ArrayList<String>();      
        
        int FacetsVectorSize = FacetsVector.size();
        for (int i = 0; i < FacetsVectorSize; i++) {
            String currentFacet = FacetsVector.get(i);
            FacetsVectorSortedUI.add(currentFacet);
        }
        Collections.sort(FacetsVectorSortedUI, new StringLocaleComparator(targetLocale)); 
        // copy back FacetsVectorSortedUI to FacetsVector (DB encoded)
        FacetsVector.clear();
        int FacetsVectorSortedUISize = FacetsVectorSortedUI.size();
        for (int i = 0; i < FacetsVectorSortedUISize; i++) {
            String currentFacet = FacetsVectorSortedUI.get(i);
            FacetsVector.add(currentFacet);
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
        
        FacetsVectorSize = FacetsVector.size();
        // for each facet       
        for (int i = 0; i < FacetsVectorSize; i++) {
            String currentFacet = FacetsVector.get(i);
            // ------------------- get current facet's hierarcies count -------------------
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentFacet));                        
            int facetHierarchies = Q.get_all_subclasses( 0);
            // FILTER hierarchies depending on user group        
            facetHierarchies = dbf.FilterHierResults(SessionUserInfo, facetHierarchies, Q, sis_session);
            Q.reset_set(facetHierarchies);
            int facetHierarchiesCard = Q.set_get_card(facetHierarchies);
            Q.free_set(facetHierarchies);
            FacetsHierarciesCountVector.add(new IntegerObject(facetHierarchiesCard));
            // ------------------- get current facet's terms count -------------------
            // set_current_node(currentFacet) AGAIN because DBFilters methods change the name scope!
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(currentFacet));                        
            int facetTerms = Q.get_all_instances(0);
            // FILTER terms depending on user group
            facetTerms = dbf.FilterTermsResults(SessionUserInfo, facetTerms, Q, sis_session);
            Q.reset_set(facetTerms);
            int facetTermsCard = Q.set_get_card(facetTerms);
            FacetsTermsCountVector.add(new IntegerObject(facetTermsCard));
            // ------------------- get current facet's NonPreffered terms count (XA) -------------------
            Q.reset_set(facetTerms);
            int linksSet = Q.get_link_from_by_category(facetTerms, thes_HierarchyTerm, thes_UF);
            Q.reset_set(linksSet);
            int NonPrefferedTermsSet = Q.get_to_value(linksSet);
            Q.reset_set(NonPrefferedTermsSet);
            int NonPrefferedTermsSetSetCard = Q.set_get_card(NonPrefferedTermsSet);
            Q.free_set(linksSet);
            Q.free_set(NonPrefferedTermsSet);
            FacetsNonPrefferedTermsCountVector.add(new IntegerObject(NonPrefferedTermsSetSetCard));            
            // ------------------- get current facet's translations count -------------------
            Q.reset_set(facetTerms);
            linksSet = Q.get_link_from_by_category(facetTerms, thes_term, thes_translation);
            Q.reset_set(linksSet);
            int TranslationsSet = Q.get_to_value(linksSet);
            Q.reset_set(TranslationsSet);
            int TranslationsSetCard = Q.set_get_card(TranslationsSet);
            Q.free_set(linksSet);
            Q.free_set(TranslationsSet);
            FacetsEnglishWordsCountVector.add(new IntegerObject(TranslationsSetCard));
            // ------------------- get current facet's NonPreffered EnglishWords count (UF) -------------------
            Q.reset_set(facetTerms);
            linksSet = Q.get_link_from_by_category(facetTerms, thes_HierarchyTerm, thes_uf_translations);
            Q.reset_set(linksSet);
            int NonPrefferedEnglishWordsSet = Q.get_to_value(linksSet);
            Q.reset_set(NonPrefferedEnglishWordsSet);
            int NonPrefferedEnglishWordsSetCard = Q.set_get_card(NonPrefferedEnglishWordsSet);
            Q.free_set(linksSet);
            Q.free_set(NonPrefferedEnglishWordsSet);
            FacetsNonPrefferedEnglishWordsCountVector.add(new IntegerObject(NonPrefferedEnglishWordsSetCard));                        
            
            Q.free_set(facetTerms);
        }
    }        
    
    /*----------------------------------------------------------------------
                        GetAllFacets()
    -----------------------------------------------------------------------
    OUTPUT: - a set with all facets of current thesaurus
    ------------------------------------------------------------------------*/
    public int GetAllFacets(QClass Q, IntegerObject sis_session, UserInfoClass SessionUserInfo) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for EKTFacet
        StringObject Facets = new StringObject();
        dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), Facets);
        // get instances of EKTFacet 
        Q.reset_name_scope();
        Q.set_current_node(Facets);
        int set_f = Q.get_instances(0);
        Q.reset_set(set_f);
        // get instances of EKTObsoleteFacet 
        StringObject ObsoleteFacets = new StringObject();
        dbtr.getThesaurusClass_ObsoleteFacet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), ObsoleteFacets);
        Q.reset_name_scope();
        Q.set_current_node(ObsoleteFacets);
        int set_of = Q.get_instances(0);
        Q.reset_set(set_of);
        Q.set_union(set_f, set_of);
        Q.reset_set(set_f);
        Q.free_set(set_of);
        
        // FILTER facets depending on user group
        DBFilters dbf = new DBFilters();
        set_f = dbf.FilterFacetResults(SessionUserInfo, set_f, Q, sis_session);
        
        return set_f;
    }        
    
}
