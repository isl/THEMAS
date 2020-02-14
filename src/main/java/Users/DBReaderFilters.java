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
import Utils.Parameters;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import java.io.*;

import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            DBReaderFilters
-----------------------------------------------------------------------
  Class for handling the DB filtering for users of group "READER"
----------------------------------------------------------------------*/
public class DBReaderFilters {
    
    /*---------------------------------------------------------------------
                            DBReaderFilters()
    ----------------------------------------------------------------------*/                
    public DBReaderFilters() {
    }    
    
    /*---------------------------------------------------------------------
                    FilterTermsResults()
    -----------------------------------------------------------------------
    INPUT: - termsSet: the given set of terms to be filtered
    OUTPUT: - termsSet: a subset of the given set of terms containing only 
              those that are instances of class AAAStatusApproved
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, the initial set is returned
    ----------------------------------------------------------------------*/
    public int FilterTermsResults(UserInfoClass SessionUserInfo, int termsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return termsSet;
        }        
        // in case of reader user
        int SISApiSession = sis_session.getValue();
        // get all the instances of class AAAStatusApproved
        StringObject thesStatusApproved = new StringObject(SessionUserInfo.selectedThesaurus.concat("StatusApproved"));
        Q.reset_name_scope();
        Q.set_current_node(thesStatusApproved);
        int TermsOfStatusApproved = Q.get_all_instances(0);
        Q.reset_set(TermsOfStatusApproved);
        Q.reset_set(termsSet); 
        // intersect the termsSet with the instances of class AAAStatusApproved
        Q.set_intersect(termsSet, TermsOfStatusApproved);
        Q.free_set(TermsOfStatusApproved);
        Q.reset_set(termsSet);        
        
        return termsSet;
    }    
    
    //links from result_terms to terms
    //links to result_terms or result_ufs from terms
    public void FilterTermsResultsLinks(String selectedThesaurus, int termsLinksSetFrom, int termsLinksSetTo, QClass Q, IntegerObject sis_session) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject ApprovedClassObj = new StringObject();
        StringObject StatusClassObj = new StringObject();
        dbtr.getThesaurusClass_StatusOfTerm(selectedThesaurus, StatusClassObj);
        dbtr.getThesaurusClass_StatusApproved(selectedThesaurus, ApprovedClassObj);
        
        Q.reset_name_scope();
        Q.set_current_node(ApprovedClassObj);
        int set_approved = Q.get_all_instances(0);
        Q.reset_set(set_approved);
        
        Q.reset_name_scope();
        Q.set_current_node(StatusClassObj);
        int set_filter_status = Q.get_all_instances(0);
        Q.reset_set(set_filter_status);
        
        Q.set_difference(set_filter_status, set_approved);
        Q.reset_set(set_filter_status);
        
        Q.free_set(set_approved);
        
        if(termsLinksSetFrom!=-1){
            int set_links_to_filter_set = Q.get_link_to(set_filter_status);
            Q.reset_set(set_links_to_filter_set);
            Q.reset_set(termsLinksSetFrom);
            Q.set_difference(termsLinksSetFrom, set_links_to_filter_set);
            Q.reset_set(termsLinksSetFrom);
            Q.free_set(set_links_to_filter_set);
        }
        
        if(termsLinksSetTo!=-1){
            int set_links_from_filter_set = Q.get_link_from(set_filter_status);
            Q.reset_set(set_links_from_filter_set);
            Q.reset_set(termsLinksSetTo);
            Q.set_difference(termsLinksSetTo, set_links_from_filter_set);
            Q.reset_set(termsLinksSetTo);
            Q.free_set(set_links_from_filter_set);
        }
        
        Q.free_set(set_filter_status);
    }
    /*---------------------------------------------------------------------
                    FilterHierResults()
    -----------------------------------------------------------------------
    INPUT: - hiersSet: the given set of hierarchies to be filtered
    OUTPUT: - hiersSetFiltered: a subset of the given set of hierarchies containing only 
              the hierarchies with approved TopTerms (instances of class AAAStatusApproved)
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, the initial set is returned
    ----------------------------------------------------------------------*/
    public int FilterHierResults(UserInfoClass SessionUserInfo, int hiersSet, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return hiersSet;
        }
        // in case of reader user
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        // show ONLY the hierarchies with approved TopTerms (instances of class AAAStatusApproved)
        // looking for AAATopTerm 
        StringObject TopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTerm);
        // looking for belongs_to_aaa_hierarchy 
        StringObject TopTermHierRelationObj = new StringObject();
        dbtr.getThesaurusCategory_belongs_to_hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTermHierRelationObj);
        // get the TopTerms of hiersSet
        Q.reset_set( hiersSet);
        int TopTermsLinksSet1 = Q.get_link_to_by_category( hiersSet, TopTerm, TopTermHierRelationObj);
        Q.reset_set( TopTermsLinksSet1);
        int TopTermsSet = Q.get_from_value( TopTermsLinksSet1);
        Q.free_set( TopTermsLinksSet1);
        Q.reset_set( TopTermsSet);
        // get all the instances of class AAAStatusApproved
        StringObject thesStatusApproved = new StringObject(SessionUserInfo.selectedThesaurus.concat("StatusApproved"));
        Q.reset_name_scope();
        Q.set_current_node( thesStatusApproved);
        int TermsOfStatusApproved = Q.get_all_instances( 0);
        Q.reset_set( TermsOfStatusApproved);
        // intersect the TopTerms of hiersSet with the instances of class AAAStatusApproved
        Q.set_intersect( TopTermsSet, TermsOfStatusApproved);
        Q.free_set( TermsOfStatusApproved);
        Q.reset_set( TopTermsSet);
        // get the links of category AAATopTerm->belongs_to_aaa_hierarchy pointing from TopTermsSet
        int TopTermsLinksSet2 = Q.get_link_from_by_category( TopTermsSet, TopTerm, TopTermHierRelationObj);
        Q.reset_set( TopTermsLinksSet2);
        // get the hierarchies of the accepted TopTerms
        int hiersSetFiltered = Q.get_to_value( TopTermsLinksSet2);
        Q.free_set( TopTermsLinksSet2);
        Q.free_set( TopTermsSet);
        Q.free_set( hiersSet);
        Q.reset_set( hiersSetFiltered);
        
        return hiersSetFiltered;
    }        
    
    /*---------------------------------------------------------------------
                    FilterHierarchy()
    -----------------------------------------------------------------------
    INPUT: - hierarchy: the given hierarchy to be checked
    OUTPUT: - true: in case the given hierarchy is approved (with approved TopTerm)
            - false, otherwise
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, true is returned
    ----------------------------------------------------------------------*/
    public boolean FilterHierarchy(UserInfoClass SessionUserInfo, StringObject hierarchy, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return true;
        }
        // in case of reader user
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        // show ONLY the hierarchies with approved TopTerms (instances of class AAAStatusApproved)
        // looking for AAATopTerm 
        StringObject TopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTerm);
        // looking for belongs_to_aaa_hierarchy 
        StringObject TopTermHierRelationObj = new StringObject();
        dbtr.getThesaurusCategory_belongs_to_hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTermHierRelationObj);
        // get the TopTerm of hierarchy
        Q.reset_name_scope();
        Q.set_current_node( hierarchy);
        int TopTermLinkSet = Q.get_link_to_by_category( 0, TopTerm, TopTermHierRelationObj);
        Q.reset_set( TopTermLinkSet);
        int TopTermSet = Q.get_from_value( TopTermLinkSet);
        Q.free_set( TopTermLinkSet);
        Q.reset_set( TopTermSet);
        // get all the instances of class AAAStatusApproved
        StringObject thesStatusApproved = new StringObject(SessionUserInfo.selectedThesaurus.concat("StatusApproved"));
        Q.reset_name_scope();
        Q.set_current_node( thesStatusApproved);
        int TermsOfStatusApproved = Q.get_all_instances( 0);
        Q.reset_set( TermsOfStatusApproved);
        // intersect the TopTerm of hierarchy with the instances of class AAAStatusApproved
        Q.set_intersect( TopTermSet, TermsOfStatusApproved);
        Q.free_set( TermsOfStatusApproved);
        Q.reset_set( TopTermSet);
        int x = Q.set_get_card( TopTermSet);
        Q.free_set( TopTermSet);
        if (x > 0) {
            return true;
        }
        else {
            return false;
        }
    }            
    
    /*---------------------------------------------------------------------
                    FilterFacetResults()
    -----------------------------------------------------------------------
    INPUT: - facetsSet: the given set of facets to be filtered
    OUTPUT: - facetsSetFiltered: a subset of the given set of facets containing only 
              the facets with at least 1 approved hierarchy (with approved TopTerm)
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, the initial set is returned
    ----------------------------------------------------------------------*/
    public int FilterFacetResults(UserInfoClass SessionUserInfo, int facetsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return facetsSet;
        }
        // in case of reader user
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        
        // get ALL hierarchies of the DB (union of AAAHierarchyClass and AAAObsoleteHierarchy instances)
        // looking for AAAHierarchyClass
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesHierarchy);
        // get instances of AAAHierarchyClass 
        Q.reset_name_scope();
        Q.set_current_node( thesHierarchy);
        int set_with_ALL_hierarchies = Q.get_instances( 0);
        // get instances of AAAObsoleteHierarchy 
        StringObject thesObsoleteHierarchy = new StringObject();
        dbtr.getThesaurusClass_ObsoleteHierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesObsoleteHierarchy);
        Q.reset_name_scope();
        Q.set_current_node( thesObsoleteHierarchy);
        int set_oh = Q.get_instances( 0);
        Q.set_union( set_with_ALL_hierarchies, set_oh);
        Q.free_set( set_oh);
        Q.reset_set( set_with_ALL_hierarchies);
        // get ONLY the approved hierarchies
        set_with_ALL_hierarchies = FilterHierResults(SessionUserInfo, set_with_ALL_hierarchies, Q, sis_session);
        // get their facets
        int facetsSetFiltered = Q.get_superclasses( set_with_ALL_hierarchies) ;
        Q.free_set( set_with_ALL_hierarchies);
        // intersect them with the given set
        Q.set_intersect( facetsSetFiltered, facetsSet);
        Q.free_set( facetsSet);
        Q.reset_set( facetsSetFiltered);                
        
        return facetsSetFiltered;
    }            
                    
    /*---------------------------------------------------------------------
                    FilterToValuesOfTerms()
    -----------------------------------------------------------------------
    INPUT: - ToValuesOfTermsSet: the given set of ToValuesOfTerms to be filtered
    OUTPUT: - ToValuesOfTermsFiltered: a subset of the given set of ToValuesOfTerms containing only 
              the ToValuesOfTerms of approved terms
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, the initial set is returned
    ----------------------------------------------------------------------*/
    public int FilterToValuesOfTerms(UserInfoClass SessionUserInfo, int ToValuesOfTermsSet, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return ToValuesOfTermsSet;
        }
        // in case of reader user
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        
        // get the terms pointing to ToValuesOfTerms
        int linkSet = Q.get_link_to( ToValuesOfTermsSet);
        Q.reset_set( linkSet);
        int termsSet = Q.get_from_value( linkSet);
        Q.free_set( linkSet);
        Q.reset_set( termsSet);
        // keep ONLY the approved terms
        termsSet = FilterTermsResults(SessionUserInfo, termsSet, Q, sis_session);
        Q.reset_set( termsSet);
        // get their ToValues
        linkSet = Q.get_link_from( termsSet);
        int ToValuesOfTermsFiltered = Q.get_to_value( linkSet);
        Q.free_set( linkSet);
        Q.set_intersect( ToValuesOfTermsFiltered, ToValuesOfTermsSet);
        Q.free_set( ToValuesOfTermsSet);
        Q.reset_set( ToValuesOfTermsFiltered);                
        
        return ToValuesOfTermsFiltered;
    }                            
    
    /*---------------------------------------------------------------------
                    IsHierarchyHiddenFromReader()
    -----------------------------------------------------------------------
    FUNCTION: returns true in case target belongs to AAAHierarchy and not to AAAStatusApproved
    ----------------------------------------------------------------------*/
    public boolean IsHierarchyHiddenFromReader(UserInfoClass SessionUserInfo, StringObject target, QClass Q, IntegerObject sis_session) {
        int SISApiSession = sis_session.getValue();
        DBGeneral dbg = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for AAAHierarchy
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesHierarchy);
        boolean isHierarchy = dbg.NodeBelongsToClass(target, thesHierarchy, false, Q, sis_session);
        boolean isApproved = FilterHierarchy(SessionUserInfo, target, Q, sis_session);
        if (isHierarchy == true && isApproved == false) {
            return true;
        }
        
        return false;
    }    
    
    /*---------------------------------------------------------------------
                    IsDescriptorHiddenFromReader()
    -----------------------------------------------------------------------
    FUNCTION: returns true in case target is a Descriptor/ObsoleteDescriptor/TopTerm and not Approved
    ----------------------------------------------------------------------*/
    public boolean IsDescriptorHiddenFromReader(UserInfoClass SessionUserInfo, StringObject target, QClass Q, IntegerObject sis_session) {
        DBGeneral dbg = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        // check if target is Descriptor/ObsoleteDescriptor/TopTerm
        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);
        int DescriptorClassesSize = DescriptorClasses.length;
        boolean isDescriptor = false;
        for (int i = 0; i < DescriptorClassesSize; i++) {
            //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"-----> " +  DescriptorClasses[i]);
            isDescriptor = dbg.NodeBelongsToClass(target, new StringObject(DescriptorClasses[i]), false, Q, sis_session);
            if (isDescriptor) break;
        }
        // in case target is not Descriptor/ObsoleteDescriptor/TopTerm, do not hide it
        if (isDescriptor == false) {
            return false;
        }
        // looking for AAAStatusApproved
        StringObject thesStatusApproved = new StringObject();
        dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, thesStatusApproved);
        boolean isApproved = dbg.NodeBelongsToClass(target, thesStatusApproved, false, Q, sis_session);
        // in case target is not Approved, hide it
        if (isApproved == false) {
            return true;
        }        
        
        return false;
    }        
    
    /*---------------------------------------------------------------------
                    FilterBTLinksSet()
    -----------------------------------------------------------------------
    INPUT: - BTLinksSet: the given link set containing BT links (and maybe of other category) to be filtered
    OUTPUT: - 
    FUNCTION: the above filtering is done only in case of a user of group "READER"
              Otherwise, the initial set is returned
    ----------------------------------------------------------------------*/
    public int FilterBTLinksSet(UserInfoClass SessionUserInfo, int BTLinksSet, QClass Q, IntegerObject sis_session) {
        
        // in case of NOT reader user, return the same set
        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == false) {
            return BTLinksSet;
        }
        // in case of reader user
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        // fill FROM_Vector and TO_Vector with the pairs of BT links,
        // BTlink_sysid_Vector with the sysids of BT links and
        // LinkIsHidden_Vector with true in case the link must be hidden (full of false initially)
        ArrayList<String> FROM_Vector = new ArrayList();
        ArrayList<String> TO_Vector = new ArrayList();
        ArrayList<Long> BTlink_sysid_Vector = new ArrayList();
        ArrayList<Boolean> LinkIsHidden_Vector = new ArrayList();
        
        
        ArrayList<Return_Link_Id_Row> retVals = new ArrayList();
        if(Q.bulk_return_link_id(BTLinksSet, retVals)!=QClass.APIFail){
            for(Return_Link_Id_Row row: retVals){
                FROM_Vector.add(row.get_v4_cmv().getString());
                TO_Vector.add(row.get_v1_cls());
                BTlink_sysid_Vector.add(row.get_v3_sysid());
                // ATTENTION: reinitialize BTlink_sysid, otherwise the same int value is added to LinkIsHidden_Vector!!
                //BTlink_sysid = new IntegerObject();
                LinkIsHidden_Vector.add(false);
            }
        }
        /*
        StringObject cls = new StringObject();
        IntegerObject BTlink_sysid = new IntegerObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        
        while (Q.retur_link_id( BTLinksSet, cls, new IntegerObject(), BTlink_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
            FROM_Vector.add(cmv.getString());
            TO_Vector.add(cls.getValue());
            BTlink_sysid_Vector.add(BTlink_sysid);
            // ATTENTION: reinitialize BTlink_sysid, otherwise the same int value is added to LinkIsHidden_Vector!!
            BTlink_sysid = new IntegerObject();
            LinkIsHidden_Vector.add(new Boolean(false));
        }
        */
        // for testing
        /*
        Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"---------------- BEFORE FILTERING ----------------");
        for (int i = 0; i < FROM_Vector.size(); i++) {
            Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+i+1 + ". BT link: " + GreekConverter.ISO72UniString((String)FROM_Vector.get(i)) + " -> " + GreekConverter.ISO72UniString((String)TO_Vector.get(i)) + " with sysid: " + BTlink_sysid_Vector.get(i));
        }
        */
        
        // for each to-value of BT links
        int TO_VectorSize = TO_Vector.size();
        for (int i = 0; i < TO_VectorSize; i++) {
            // in case link has already been marked as hidden, ignore it
            if ((Boolean)LinkIsHidden_Vector.get(i)) {
                continue;
            }
            StringObject toValue = new StringObject((String)TO_Vector.get(i));
            boolean IsDescriptorHiddenFromReader = IsDescriptorHiddenFromReader(SessionUserInfo, toValue, Q, sis_session);
            // in case it must be hidden from READER
            if (IsDescriptorHiddenFromReader == true) {
                //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"----------- HIDE BT link: " + GreekConverter.ISO72UniString((String)FROM_Vector.get(i)) + " -> " + GreekConverter.ISO72UniString((String)TO_Vector.get(i)) + " with sysid: " + BTlink_sysid_Vector.get(i));
                // mark link as hidden
                LinkIsHidden_Vector.set(i, true);
                // hide the subbtree with root the to-value of this hidden link
                HideBTSubTree(toValue.getValue(), FROM_Vector, TO_Vector, LinkIsHidden_Vector, SessionUserInfo, Q, sis_session);
            }
        }
        
        // for testing
        //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"---------------- AFTER FILTERING ----------------");

        // construct the filtered link set
        Q.free_set( BTLinksSet);
        int BTLinksSetFiltered = Q.set_get_new();
        int FROM_VectorSize = FROM_Vector.size();
        int index = 1;
        for (int i = 0; i < FROM_VectorSize; i++) {
            if ((Boolean)LinkIsHidden_Vector.get(i) == false) {
                //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+index + ". BT link: " + GreekConverter.ISO72UniString((String)FROM_Vector.get(i)) + " -> " + GreekConverter.ISO72UniString((String)TO_Vector.get(i)) + " with sysid: " + BTlink_sysid_Vector.get(i));
                index++;
                long linkSysidL = BTlink_sysid_Vector.get(i);
                Q.reset_name_scope();
                Q.set_current_node_id(linkSysidL);
                Q.set_put( BTLinksSetFiltered);
            }
        }               
           
        Q.reset_set( BTLinksSetFiltered);
        return BTLinksSetFiltered;
    }                                
    
    /*---------------------------------------------------------------------
                    HideBTSubTree()
    -----------------------------------------------------------------------
    INPUT: - String root: the root of the BT-links subtree to be marked as hidden
           - FROM_Vector, TO_Vector: the pairs of BT links
           - LinkIsHidden_Vector: parallel Vector with true in case the link must be hidden
    FUNCTION: recursively marks as hidden the subtree of BT-links with root the given one
    ----------------------------------------------------------------------*/
    public void HideBTSubTree(String root, ArrayList FROM_Vector, ArrayList TO_Vector, ArrayList LinkIsHidden_Vector, UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session) {
        // for each child of the root (links with from-values == root)
        int FROM_VectorSize = FROM_Vector.size();
        for (int i = 0; i < FROM_VectorSize; i++) {
            String fromValue = (String)FROM_Vector.get(i);
            if (root.equals(fromValue) == true) { // root found
                //Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"----------- HIDE BT link: " + GreekConverter.ISO72UniString((String)FROM_Vector.get(i)) + " -> " + GreekConverter.ISO72UniString((String)TO_Vector.get(i)));
                // mark link as hidden
                LinkIsHidden_Vector.set(i, true);
                
                // recursively mark as hidden the subtree of BT-links with root the to-value of this link
                String toValue = (String)TO_Vector.get(i);
                HideBTSubTree(toValue, FROM_Vector, TO_Vector, LinkIsHidden_Vector, SessionUserInfo, Q, sis_session);
            }
        }
    }    
}


