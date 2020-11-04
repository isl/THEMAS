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
package DB_Classes;

import Admin_Thesaurus.DBexportData;
import Users.DBFilters;

import Utils.StrLenComparator;
import Utils.Parameters;
import Utils.Utilities;
import Utils.SortItem;
import Utils.TaxonomicCodeItem;
import Utils.StringLocaleComparator;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.NodeInfoSortItemContainer;
import Utils.SortItemComparator;

import java.io.IOException;
import java.util.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
 DBGeneral
 -----------------------------------------------------------------------
 class with methods handling the SIS data base
 ----------------------------------------------------------------------*/
public class DBGeneral {

    /*----------------------------------------------------------------------
     Constructor of DBConnect
     -----------------------------------------------------------------------*/
    public DBGeneral() {

    }

    /* getPrefixes()
     * Function used in order to pull all prefixes from thesaurus DB
     * Called by SearchResults_Terms_Alphabetical Servlet.
     */
    public ArrayList<String> getPrefixes(QClass Q, IntegerObject sis_session) {

        ArrayList<String> vPrefixes = new ArrayList<String>();
        int sisSessionID = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Prefix"));
        int set_Pr = Q.get_instances(0);
        Q.reset_set(set_Pr);

        int numOfPrefixes = Q.set_get_card(set_Pr);

        if (numOfPrefixes > 0) {
            //StringObject c_name = new StringObject();
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(set_Pr, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    if (!row.get_v1_cls_logicalname().trim().matches("")) {
                        vPrefixes.add(row.get_v1_cls_logicalname());
                    }
                }
            }
            /*
             while (Q.retur_nodes(set_Pr, c_name) != QClass.APIFail) {
             if(!c_name.getValue().trim().matches(""))
             vPrefixes.add(c_name.getValue());
             }*/
        }
        //Sort is needed for alphabetical sort bug fix when prefixes like EL` and THES1EL` are defined
        StrLenComparator strLen = new StrLenComparator(StrLenComparator.Descending);
        Collections.sort(vPrefixes, strLen);

        Q.free_set(set_Pr);

        //Q.free_all_sets(sisSessionID);
        return vPrefixes;
    }

    /*HLIAS---------------------------------------------------------------------
     IsReleasedHierarchy()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetHierarchy: the Descriptor to be checked
     OUTPUT: - true in case targetFacet is released, false otherwise
     FUNCTION: gets the classes of targetFacet and checks if class
     "NewThesaurusClass" of current thesaurus is one of them (=> new descriptor)
     ----------------------------------------------------------------------*/
    public boolean IsReleasedHierarchy(String selectedThesaurus, StringObject targetHierarchy, QClass Q, IntegerObject sis_session) {
        int SISapiSession = sis_session.getValue();
        //THEMASUserInfo SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

        //String thesNewClass = new String(SessionUserInfo.selectedThesaurus.concat("NewThesaurusClass"));
        String thesNewClass = new String(selectedThesaurus.concat("NewThesaurusClass"));

        Q.reset_name_scope();
        long retL = Q.set_current_node(targetHierarchy);

        if (retL == QClass.APIFail) {
            return false;
        }

        int set = Q.get_all_classes(0);
        Q.reset_set(set);

        boolean isReleased = true;
        //StringObject l_name = new StringObject();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                if (row.get_v1_cls_logicalname().equals(thesNewClass)) {
                    isReleased = false;
                    break;
                }
            }
        }
        /*while ((Q.retur_nodes( set, l_name)) != QClass.APIFail) {
         if (l_name.getValue().equals(thesNewClass)) {
         isReleased = false;
         break;
         }
         }*/
        Q.free_set(set);
        return isReleased;

    }

    /*HLIAS---------------------------------------------------------------------
     IsReleasedFacet()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetFacet: the Descriptor to be checked
     OUTPUT: - true in case targetFacet is released, false otherwise
     FUNCTION: gets the classes of targetFacet and checks if class
     "NewThesaurusClass" of current thesaurus is one of them (=> new descriptor)
     ----------------------------------------------------------------------*/

    public boolean IsReleasedFacet(String selectedThesaurus, StringObject targetFacet, QClass Q, IntegerObject sis_session) {
        int SISapiSession = sis_session.getValue();
        //THEMASUserInfo SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        String thesNewClass = new String(selectedThesaurus.concat("NewThesaurusClass"));

        Q.reset_name_scope();
        long retL = Q.set_current_node(targetFacet);

        if (retL == QClass.APIFail) {
            return false;
        }

        int set = Q.get_all_classes(0);
        Q.reset_set(set);

        boolean isReleased = true;
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                if (row.get_v1_cls_logicalname().equals(thesNewClass)) {
                    isReleased = false;
                    break;
                }
            }
        }
        /*
         StringObject l_name = new StringObject();
         while ((Q.retur_nodes( set, l_name)) != QClass.APIFail) {
         if (l_name.getValue().equals(thesNewClass)) {
         isReleased = false;
         break;
         }
         }*/
        Q.free_set(set);
        return isReleased;

    }

    /*HLIAS---------------------------------------------------------------------
     GetKindOfHierarchy()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetFacet: the facet to be checked
     OUTPUT: - one of the above defines
     FUNCTION: returns the kind of the given facet (new/released/obsolete)
     ----------------------------------------------------------------------*/
    public int GetKindOfHierarchy(String selectedThesaurus, StringObject targetHierarchy, QClass Q, IntegerObject sis_session) {

        boolean isReleased = IsReleasedHierarchy(selectedThesaurus, targetHierarchy, Q, sis_session);
        if (isReleased == false) {
            return ConstantParameters.HIERARCHY_OF_KIND_NEW;
        }
        // looking for THES1ObsoleteHierarchy
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject thesObsoleteHierarchy = new StringObject();

        dbtr.getThesaurusClass_ObsoleteHierarchy(selectedThesaurus, Q, sis_session.getValue(), thesObsoleteHierarchy);

        boolean isObsolete = NodeBelongsToClass(targetHierarchy, thesObsoleteHierarchy, false, Q, sis_session);
        if (isObsolete == true) {
            return ConstantParameters.HIERARCHY_OF_KIND_OBSOLETE;
        }
        return ConstantParameters.HIERARCHY_OF_KIND_RELEASED;
    }

    /*---------------------------------------------------------------------
     GetKindOfFacet()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetFacet: the facet to be checked
     OUTPUT: - one of the above defines
     FUNCTION: returns the kind of the given facet (new/released/obsolete)
     ----------------------------------------------------------------------*/

    public int GetKindOfFacet(String selectedThesaurus, StringObject targetFacet, QClass Q, IntegerObject sis_session) {
        boolean isReleased = IsReleasedFacet(selectedThesaurus, targetFacet, Q, sis_session);
        if (isReleased == false) {
            return ConstantParameters.FACET_OF_KIND_NEW;
        }
        // looking for THES1ObsoleteDescriptor 
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject thesObsoleteFacet = new StringObject();

        dbtr.getThesaurusClass_ObsoleteFacet(selectedThesaurus, Q, sis_session.getValue(), thesObsoleteFacet);

        boolean isObsolete = NodeBelongsToClass(targetFacet, thesObsoleteFacet, false, Q, sis_session);
        if (isObsolete == true) {
            return ConstantParameters.FACET_OF_KIND_OBSOLETE;
        }
        return ConstantParameters.FACET_OF_KIND_RELEASED;
    }

    /*---------------------------------------------------------------------
     IsReleasedDescriptor()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetDescriptor: the Descriptor to be checked
     OUTPUT: - true in case targetDescriptor is released, false otherwise
     FUNCTION: gets the classes of targetDescriptor and checks if class
     "NewDescriptor" of current thesaurus is one of them (=> new descriptor)
     ----------------------------------------------------------------------*/

    public boolean IsReleasedDescriptor(String selectedThesaurus, StringObject targetDescriptor, QClass Q, IntegerObject sis_session) {
        int SISapiSession = sis_session.getValue();

        String thesNewDescriptor = new String(selectedThesaurus.concat("NewDescriptor"));

        Q.reset_name_scope();
        long retL = Q.set_current_node(targetDescriptor);
        if (retL != QClass.APIFail) {
            int set = Q.get_classes(0);
            Q.reset_set(set);

            boolean isReleased = true;
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    if (row.get_v1_cls_logicalname().equals(thesNewDescriptor)) {
                        isReleased = false;
                        break;
                    }
                }
            }
            /*StringObject l_name = new StringObject();
             while ((Q.retur_nodes( set, l_name)) != QClass.APIFail) {
             if (l_name.getValue().equals(thesNewDescriptor)) {
             isReleased = false;
             break;
             }
             }*/

            Q.free_set(set);

            return isReleased;
        }
        return false;
    }

    /*---------------------------------------------------------------------
     GetKindOfDescriptor()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetDescriptor: the Descriptor to be checked
     OUTPUT: - one of the above defines
     FUNCTION: returns the kind of the given descriptor (new/released/obsolete)
     ----------------------------------------------------------------------*/
    public int GetKindOfDescriptor(String selectedThesaurus, StringObject targetDescriptor, QClass Q, IntegerObject sis_session) {
        boolean isReleased = IsReleasedDescriptor(selectedThesaurus, targetDescriptor, Q, sis_session);
        if (isReleased == false) {
            return ConstantParameters.DESCRIPTOR_OF_KIND_NEW;
        }
        // looking for THES1ObsoleteDescriptor 
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject thesObsoleteDescriptor = new StringObject();
        dbtr.getThesaurusClass_ObsoleteDescriptor(selectedThesaurus, Q, sis_session.getValue(), thesObsoleteDescriptor);
        boolean isObsolete = NodeBelongsToClass(targetDescriptor, thesObsoleteDescriptor, false, Q, sis_session);
        if (isObsolete == true) {
            return ConstantParameters.DESCRIPTOR_OF_KIND_OBSOLETE;
        }
        return ConstantParameters.DESCRIPTOR_OF_KIND_RELEASED;
    }

    /* Start TYPING AHEAD  */
    public ArrayList<String> Search(UserInfoClass SessionUserInfo, String str, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        ArrayList<String> hint = new ArrayList<String>();

        int set_descriptor, set_term, set_obsolete_descriptor, set_match, set_result;

        //StringObject results_name = new StringObject();
        ArrayList<String> v_results = new ArrayList<String>();
        StringObject Descriptor = new StringObject();
        StringObject TopTerm = new StringObject();
        StringObject ObsoleteDescriptor = new StringObject();

        CMValue ptrn = new CMValue();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        //ptrn.assign_string(prefix.concat(str) + "*");
        //ptrn.assign_string(prefix.concat(str));
        ptrn.assign_string(str+"*");

        String term_split[];
        ArrayList<String> vec = new ArrayList<String>();
        ArrayList<String> removed = new ArrayList<String>();
        ArrayList<String> options = new ArrayList<String>();

        //out.println("ptrn :"+ptrn.getString());
        Descriptor.setValue(SessionUserInfo.selectedThesaurus + "Descriptor");
        TopTerm.setValue(SessionUserInfo.selectedThesaurus + "TopTerm");
        ObsoleteDescriptor.setValue(SessionUserInfo.selectedThesaurus + "ObsoleteDescriptor");

        Q.reset_name_scope();
        Q.set_current_node(Descriptor);
        set_descriptor = Q.get_all_instances(0);
        Q.reset_set(set_descriptor);

        Q.reset_name_scope();
        Q.set_current_node(TopTerm);
        set_term = Q.get_all_instances(0);
        Q.reset_set(set_term);

        // karam
        Q.reset_name_scope();
        Q.set_current_node(ObsoleteDescriptor);
        set_obsolete_descriptor = Q.get_all_instances(0);
        Q.reset_set(set_obsolete_descriptor);

        Q.set_union(set_descriptor, set_term);
        // karam
        Q.set_union(set_descriptor, set_obsolete_descriptor);
        Q.reset_set(set_descriptor);

        Q.reset_name_scope();

        set_match = Q.set_get_new();
        Q.set_put_prm(set_match, ptrn);

        set_result = Q.get_matched(set_descriptor, set_match);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set_result = dbf.FilterTermsResults(SessionUserInfo, set_result, Q, sis_session);

        Q.reset_set(set_result);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                v_results.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes(set_result, results_name)) != QClass.APIFail) {
         v_results.add(results_name.getValue());
         }*/

        removed = removePrefix(v_results);
        

        Collections.sort(removed, new StringLocaleComparator(targetLocale));

        for (int i = 0; i < removed.size(); i++) {
            //options = options.concat((String)removed.get(i) + "###");
            options.add((String) removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }

        Q.free_set(set_descriptor);
        Q.free_set(set_term);
        Q.free_set(set_obsolete_descriptor);
        Q.free_set(set_match);
        Q.free_set(set_result);

        //Q.free_all_sets();
        return (options);
    }

    /* Start TYPING AHEAD  */
    public ArrayList<String> Search_Facets(UserInfoClass SessionUserInfo, String str, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        int set_f, set_of, set_match, set_result;
        //StringObject results_name = new StringObject();
        ArrayList<String> v_results = new ArrayList<>();
        StringObject Facet = new StringObject();
        StringObject ObsoleteFacet = new StringObject();

        CMValue ptrn = new CMValue();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        //ptrn.assign_string(prefix.concat(str));
        ptrn.assign_string(str+"*");

        ArrayList<String> removed = new ArrayList<>();
        ArrayList<String> options = new ArrayList<>();

        dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), Facet);
        dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), ObsoleteFacet);

        Q.reset_name_scope();
        Q.set_current_node(Facet);
        set_f = Q.get_instances(0);
        Q.reset_set(set_f);

        Q.reset_name_scope();
        Q.set_current_node(ObsoleteFacet);
        set_of = Q.get_instances(0);
        Q.reset_set(set_of);

        Q.set_union(set_f, set_of);
        Q.reset_set(set_f);

        Q.reset_name_scope();

        set_match = Q.set_get_new();
        Q.set_put_prm(set_match, ptrn);

        set_result = Q.get_matched(set_f, set_match);

        Q.reset_set(set_result);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set_result = dbf.FilterFacetResults(SessionUserInfo, set_result, Q, sis_session);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                v_results.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes(set_result, results_name)) != QClass.APIFail) {
         v_results.add(results_name.getValue());
         }*/

        removed = removePrefix(v_results);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(removed, new StringLocaleComparator(targetLocale));
        for (int i = 0; i < removed.size(); i++) {
            options.add((String) removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }

        Q.free_set(set_f);
        Q.free_set(set_of);
        Q.free_set(set_match);
        Q.free_set(set_result);

        //Q.free_all_sets();
        return (options);
    }

    /* Start TYPING AHEAD  */
    public ArrayList<String> Search_Hierarchies(UserInfoClass SessionUserInfo, String str, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        int set_h, set_oh, set_match, set_result;
        //StringObject results_name = new StringObject();
        ArrayList<String> v_results = new ArrayList<String>();
        StringObject Hierarchy = new StringObject();
        StringObject ObsoleteHierarchy = new StringObject();

        CMValue ptrn = new CMValue();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //String prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        //ptrn.assign_string(prefix.concat(str));
        ptrn.assign_string(str+"*");

        ArrayList<String> removed = new ArrayList<String>();
        ArrayList<String> options = new ArrayList<String>();

        dbtr.getThesaurusClass_Hierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), Hierarchy);
        dbtr.getThesaurusClass_ObsoleteHierarchy(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), ObsoleteHierarchy);

        Q.reset_name_scope();
        Q.set_current_node(Hierarchy);
        set_h = Q.get_instances(0);
        Q.reset_set(set_h);

        Q.reset_name_scope();
        Q.set_current_node(ObsoleteHierarchy);
        set_oh = Q.get_instances(0);
        Q.reset_set(set_oh);

        Q.set_union(set_h, set_oh);
        Q.reset_set(set_h);

        Q.reset_name_scope();

        set_match = Q.set_get_new();
        Q.set_put_prm(set_match, ptrn);

        set_result = Q.get_matched(set_h, set_match);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set_result = dbf.FilterHierResults(SessionUserInfo, set_result, Q, sis_session);

        Q.reset_set(set_result);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                v_results.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes(set_result, results_name)) != QClass.APIFail) {
         v_results.add(results_name.getValue());
         }*/

        removed = removePrefix(v_results);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(removed, new StringLocaleComparator(targetLocale));
        for (int i = 0; i < removed.size(); i++) {
            options.add((String) removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }

        Q.free_set(set_h);
        Q.free_set(set_oh);
        Q.free_set(set_match);
        Q.free_set(set_result);
        //Q.free_all_sets();
        return (options);
    }

    /*----------------------------------------------------------------------
     Search_TYPE_AHEAD_ForClass()
     ------------------------------------------------------------------------
     INPUT: - String prefix: the prefix to be added to the given string for match 
     (null in case it doesn't exist)
     - String str: the string pattern to be matched with the queried set
     - String className: the name of the class to be used so as to get its instances
     OUTPUT : - Vector options: filled with the instances of the given class that match 
     the given string pattern
     ------------------------------------------------------------------------*/
    public ArrayList<String> Search_TYPE_AHEAD_ForClass(UserInfoClass SessionUserInfo, String prefix, String str, String className, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();
        // get all instances of the given class
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(className));
        int instancesSet = Q.get_all_instances(0);
        Q.reset_set(instancesSet);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        StringObject thesUF = new StringObject();
        dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesUF);
        if (className.equals(ConstantParameters.EnglishWordClass) || className.equals(thesUF.getValue())
                || className.equals(ConstantParameters.TaxonomicCodeClass) || className.equals(ConstantParameters.SourceClass)) {
            // filter the EnglishWords
            instancesSet = dbf.FilterToValuesOfTerms(SessionUserInfo, instancesSet, Q, sis_session);
        }
        StringObject thesTopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), thesTopTerm);
        if (className.equals(thesTopTerm.getValue())) {
            // filter the TopTerms
            instancesSet = dbf.FilterTermsResults(SessionUserInfo, instancesSet, Q, sis_session);
        }

        ArrayList<String> v_results = new ArrayList<String>();
        //StringObject results_name = new StringObject();
        // in case of prefix given != "", use the get_matched() mechanism
        if (prefix != null) {
            CMValue ptrn = new CMValue();
            //ptrn.assign_string(prefix.concat(str));
            ptrn.assign_string(str+"*");
            int set_match = Q.set_get_new();
            Q.set_put_prm(set_match, ptrn);
            int set_result = Q.get_matched(instancesSet, set_match);
            Q.reset_set(set_result);
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    v_results.add(row.get_v1_cls_logicalname());
                }
            }
            /*while ((Q.retur_nodes( set_result, results_name)) != QClass.APIFail) {
             v_results.add(results_name.getValue());
             }*/
            Q.free_set(set_match);
            Q.free_set(set_result);
        } else { // in case of prefix given == "", collect the instances starting with the given string for match
            String patternForMatch = str;
            ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
            if (Q.bulk_return_nodes(instancesSet, retVals) != QClass.APIFail) {
                for (Return_Nodes_Row row : retVals) {
                    if (row.get_v1_cls_logicalname().startsWith(patternForMatch) == true) {
                        v_results.add(row.get_v1_cls_logicalname());
                    }
                }
            }
            /*while ((Q.retur_nodes( instancesSet, results_name)) != QClass.APIFail) {
             if (results_name.getValue().startsWith(patternForMatch) == true) {
             v_results.add(results_name.getValue());
             }
             } */
        }

        ArrayList<String> removed = new ArrayList<String>();
        removed = removePrefix(v_results);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(removed, new StringLocaleComparator(targetLocale));
        ArrayList<String> options = new ArrayList<String>();
        for (int i = 0; i < removed.size(); i++) {
            options.add((String) removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }
        Q.free_set(instancesSet);

        //Q.free_all_sets();
        return (options);
    }

    public ArrayList<String> Search_TYPE_AHEAD_ForTranslations(UserInfoClass SessionUserInfo, String str, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();

        StringObject thesaurusTermObj = new StringObject();
        StringObject translationTermObj = new StringObject();
        dbtr.getThesaurusClass_Term(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesaurusTermObj);
        dbtr.getThesaurusCategory_translation(SessionUserInfo.selectedThesaurus, Q, SISApiSession, translationTermObj);

        // get all instances of the given class
        Q.reset_name_scope();
        Q.set_current_node(thesaurusTermObj);
        Q.set_current_node(translationTermObj);
        int set_translation_classes = Q.get_subclasses(0); //set1
        Q.reset_set(set_translation_classes);

        int set_instances_links = Q.get_instances(set_translation_classes);//set2
        Q.reset_set(set_instances_links);

        int instancesSet = Q.get_to_value(set_instances_links);//set3
        Q.reset_set(instancesSet);

        //get all translation prefixes
        Q.reset_set(set_translation_classes);
        int set_translation_words = Q.get_to_value(set_translation_classes);//set4
        Q.reset_set(set_translation_words);

        int set_prefixes_links = Q.get_link_from_by_category(set_translation_words,
                new StringObject(ConstantParameters.individualClass), new StringObject(ConstantParameters.hasPrefix));//set5
        Q.reset_set(set_prefixes_links);

        //StringObject label = new StringObject();
        //StringObject cls = new StringObject();
        //CMValue cmv = new CMValue();
        ArrayList<String> validPrefixes = new ArrayList<String>();
        ArrayList<Return_Link_Row> retLVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_prefixes_links, retLVals) != QClass.APIFail) {
            for (Return_Link_Row row : retLVals) {
                validPrefixes.add(row.get_v3_cmv().getString());
            }
        }
        /*while(Q.retur_link( set_prefixes_links, cls, label, cmv)!=QClass.APIFail){
         validPrefixes.add(cmv.getString());
         }*/
        Q.free_set(set_prefixes_links);//free set5
        Q.free_set(set_translation_words);//free set4
        Q.free_set(set_translation_classes);//free set2
        Q.free_set(set_translation_classes);//free set1

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        instancesSet = dbf.FilterToValuesOfTerms(SessionUserInfo, instancesSet, Q, sis_session);

        //ArrayList<String> targetSet = this.get_Node_Names_Of_Set(instancesSet, false, Q, sis_session);
        ArrayList<String> v_results = new ArrayList<String>();
        int set_match = Q.set_get_new();//set6
        for (int i = 0; i < validPrefixes.size(); i++) {
            //prepare the set for get_matched()

            Q.reset_name_scope();
            String prefix = validPrefixes.get(i);
            CMValue ptrn = new CMValue();
            //ptrn.assign_string(prefix.concat(str));
            ptrn.assign_string(str+"*");
            Q.set_put_prm(set_match, ptrn);
        }

        //StringObject results_name = new StringObject();
        Q.reset_name_scope();
        Q.reset_set(set_match);
        Q.reset_set(instancesSet);

        //int card1 = Q.set_get_card(set_match);
        //int card2 = Q.set_get_card(instancesSet);
        int set_result = Q.get_matched(instancesSet, set_match);//set7
        Q.reset_set(set_result);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                v_results.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes( set_result, results_name)) != QClass.APIFail) {
         v_results.add(results_name.getValue());
         }*/

        Q.free_set(set_match);//free set6
        Q.free_set(set_result);//free set7

        ArrayList<String> removed = new ArrayList<String>();
        removed = removePrefix(v_results);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(removed, new StringLocaleComparator(targetLocale));
        ArrayList<String> options = new ArrayList<String>();
        for (int i = 0; i < removed.size(); i++) {
            options.add(removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }
        Q.free_set(instancesSet);//free set2

        //Q.free_all_sets();
        return (options);
    }

    public ArrayList<String> Search_TYPE_AHEAD_ForUFTranslations(UserInfoClass SessionUserInfo, String str, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int SISApiSession = sis_session.getValue();

        StringObject thesaurusHierarchyTermObj = new StringObject();
        StringObject ufTranslationTermObj = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, thesaurusHierarchyTermObj);
        dbtr.getThesaurusCategory_uf_translation(SessionUserInfo.selectedThesaurus, Q, SISApiSession, ufTranslationTermObj);

        // get all instances of the given class
        Q.reset_name_scope();
        Q.set_current_node(thesaurusHierarchyTermObj);
        Q.set_current_node(ufTranslationTermObj);
        int set_uf_translation_classes = Q.get_subclasses(0); //set1
        Q.reset_set(set_uf_translation_classes);

        int set_instances_links = Q.get_instances(set_uf_translation_classes);//set2
        Q.reset_set(set_instances_links);

        int instancesSet = Q.get_to_value(set_instances_links);//set3
        Q.reset_set(instancesSet);

        //get all uf_translation prefixes
        Q.reset_set(set_uf_translation_classes);
        int set_uf_translation_words = Q.get_to_value(set_uf_translation_classes);//set4
        Q.reset_set(set_uf_translation_words);

        int set_prefixes_links = Q.get_link_from_by_category(set_uf_translation_words,
                new StringObject(ConstantParameters.individualClass), new StringObject(ConstantParameters.hasPrefix));//set5
        Q.reset_set(set_prefixes_links);

        //StringObject label = new StringObject();
        //StringObject cls = new StringObject();
        //CMValue cmv = new CMValue();
        ArrayList<String> validPrefixes = new ArrayList<String>();
        ArrayList<Return_Link_Row> retLVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_prefixes_links, retLVals) != QClass.APIFail) {
            for (Return_Link_Row row : retLVals) {
                validPrefixes.add(row.get_v3_cmv().getString());
            }
        }
        /*while(Q.retur_link( set_prefixes_links, cls, label, cmv)!=QClass.APIFail){
         validPrefixes.add(cmv.getString());
         }*/
        Q.free_set(set_prefixes_links);//free set5
        Q.free_set(set_uf_translation_words);//free set4
        Q.free_set(set_uf_translation_classes);//free set2
        Q.free_set(set_uf_translation_classes);//free set1

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        instancesSet = dbf.FilterToValuesOfTerms(SessionUserInfo, instancesSet, Q, sis_session);

        //ArrayList<String> targetSet = this.get_Node_Names_Of_Set(instancesSet, false, Q, sis_session);
        ArrayList<String> v_results = new ArrayList<String>();
        int set_match = Q.set_get_new();//set6
        for (int i = 0; i < validPrefixes.size(); i++) {
            //prepare the set for get_matched()

            Q.reset_name_scope();
            String prefix = validPrefixes.get(i);
            CMValue ptrn = new CMValue();
            //ptrn.assign_string(prefix.concat(str));
            ptrn.assign_string(str+"*");
            Q.set_put_prm(set_match, ptrn);
        }

        //StringObject results_name = new StringObject();
        Q.reset_name_scope();
        Q.reset_set(set_match);
        Q.reset_set(instancesSet);

        //int card1 = Q.set_get_card(set_match);
        //int card2 = Q.set_get_card(instancesSet);
        int set_result = Q.get_matched(instancesSet, set_match);//set7
        Q.reset_set(set_result);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_result, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                v_results.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes( set_result, results_name)) != QClass.APIFail) {
         v_results.add(results_name.getValue());
         }*/

        Q.free_set(set_match);//free set6
        Q.free_set(set_result);//free set7

        ArrayList<String> removed = new ArrayList<String>();
        removed = removePrefix(v_results);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(removed, new StringLocaleComparator(targetLocale));
        ArrayList<String> options = new ArrayList<String>();
        for (int i = 0; i < removed.size(); i++) {
            options.add(removed.get(i) + Utils.ConstantParameters.TypeAheadSeparator);
        }
        Q.free_set(instancesSet);//free set2

        //Q.free_all_sets();
        return (options);
    }

    /*----------------------------------------------------------------------
     NodeBelongsToClass()
     ------------------------------------------------------------------------
     INPUT: - StringObject nodeObj: the node to be checked
     - StringObject classObj: the name of the class to be checked
     - boolean BQ: if true, begins query session
     if false, it doesn't begin query session
     OUTPUT : - TRUE, in case ALL the given nodeObj belongs to class classObj
     - FALSE, otherwise
     FUNCTION: checks if the given nodeObj belongs to class classObj
     ------------------------------------------------------------------------*/
    public boolean NodeBelongsToClass(StringObject nodeObj, StringObject classObj, boolean BQ, QClass Q, IntegerObject sis_session) {
        int API_sessionID = sis_session.getValue();
        // begin query
        if (BQ == true) {
            Q.TEST_begin_query();

            // check if classObj exists
        }
        Q.reset_name_scope();
        long classSysidL = Q.set_current_node(classObj);
        if (classSysidL < 0) {
            if (BQ == true) {
                Q.TEST_end_query();
            }
            return false;
        }
        // check if nodeObj exists
        Q.reset_name_scope();
        long nodeSysidL = Q.set_current_node(nodeObj);
        if (nodeSysidL < 0) {
            if (BQ == true) {
                Q.TEST_end_query();
            }
            return false;
        }
        // check if nodeObj belongs to classObj
        int nodeClassesSet = Q.get_all_classes(0);
        Q.reset_set(nodeClassesSet);
        //StringObject nodeClassObj = new StringObject();
        boolean belongs = false;
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(nodeClassesSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                if (row.get_v1_cls_logicalname().compareTo(classObj.getValue()) == 0) {
                    belongs = true;
                    break;
                }
            }
        }
        /*
         while (Q.retur_nodes( nodeClassesSet, nodeClassObj) != QClass.APIFail) {
         if (nodeClassObj.getValue().compareTo(classObj.getValue()) == 0) {
         belongs = true;
         break;
         }
         }*/
        Q.free_set(nodeClassesSet);

        // end query
        if (BQ == true) {
            Q.TEST_end_query();
        }
        return belongs;
    }

    /*---------------------------------------------------------------------
     traverseByCategory()
     -----------------------------------------------------------------------
     FUNCTION: returns all the ancestor of the given concept.
     The function get_traverse_by_category from the QClass Api is used.
     ----------------------------------------------------------------------
     public Vecto traverseByCategory(String selectedThesaurus, Vecto begin, StringObject from, StringObject link, QClass Q, IntegerObject sis_session) {
     Vector bt = new Vecto();
     DBThesaurusReferences dbtr = new DBThesaurusReferences();
     String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
     Vector terms = new Vecto();
     terms.addAll(begin);

     int SISapiSession = sis_session.getValue();
     Q.reset_name_scope();
     CategorySet[] categs = new CategorySet[2];
     categs[0] = new CategorySet(from.getValue(), link.getValue(), QClass.Traversal_Direction.FORWARD);
     //categs[1] = new CategorySet("end", "end", QClass.NOISA);
     Q.set_categories( categs);

     for (int i = 0; i < terms.size(); i++) {
     Q.reset_name_scope();
     Q.set_current_node( new StringObject(prefix.concat(terms.get(i).toString())));
     int set;
     if ((set = Q.get_traverse_by_category( 0, QClass.Traversal_Isa.NOISA)) != QClass.APIFail) {
     int set2 = Q.get_to_value( set);
     Q.reset_set( set2);
     StringObject name = new StringObject();
     while ((Q.retur_nodes( set2, name)) != QClass.APIFail) {
     name.setValue(removePrefix(name.getValue()));
     bt.add(name.getValue());
     }
     Q.free_set( set2);
     Q.free_set( set);
     }
     }
     //Q.free_all_sets(SISapiSession);

     return bt;
     }
     */

 /* Checks if the given rt belongs to the same hierarchy as the concept.
     * Returns true it is, else it returns false.
     * If the rt doesn't exist, false is returned.
     
     public boolean hasSameHierarchy(String selectedThesaurus,String concept, String rt,QClass Q, IntegerObject sis_session,IntegerObject tms_session) {
     DBThesaurusReferences dbtr = new DBThesaurusReferences();
     String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
     StringObject concept_obj = new StringObject(prefix_el.concat(concept));
     StringObject rt_obj = new StringObject(prefix_el.concat(rt));
     if (check_exist(rt_obj.getValue(),Q,sis_session) == false) {
     Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Rt does NOT exist");
     return (false);
     }
     Vector conceptHiers = getDescriptorHierarchies(selectedThesaurus,concept_obj,Q,sis_session);
     Vector rtHiers = getDescriptorHierarchies(selectedThesaurus,rt_obj,Q,sis_session);
     Vector same = new Vecto();

     for (int i = 0; i < rtHiers.size(); i++) {
     for (int j = 0; j < conceptHiers.size(); j++) {
     if (((StringObject) rtHiers.get(i)).getValue().compareTo(((StringObject) conceptHiers.get(j)).getValue()) == 0) {
     same.add(((StringObject) rtHiers.get(i)).getValue());
     }
     }
     }
     Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"same.length = " + same.size());
     if (same.size() >= 1) {
     return (true);
     }
     return (false);
     }
     */

 /*---------------------------------------------------------------------
     isConcept()
     -----------------------------------------------------------------------
     FUNCTION: checks if the given term is a Descriptor or TopTerm
     ----------------------------------------------------------------------*/
    public boolean isConcept(String selectedThesaurus, String term, QClass Q, IntegerObject sis_session) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for THES1TopTerm 
        StringObject TopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTerm);
        // looking for THES1Descriptor 
        StringObject Desc = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), Desc);

        boolean isTopTerm = NodeBelongsToClass(new StringObject(term), TopTerm, false, Q, sis_session);
        boolean isDescriptor = NodeBelongsToClass(new StringObject(term), Desc, false, Q, sis_session);

        boolean ret = isTopTerm || isDescriptor;
        return ret;
    }

    /*---------------------------------------------------------------------
     getFacets()
     -----------------------------------------------------------------------
     FUNCTION: returns in a Vector all Concepts that are instances of the 
     THES1Facet,  sorted alphabetically and without their prefixes
     ----------------------------------------------------------------------*/
    public ArrayList<String> getFacets(String selectedThesaurus, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1Facet
        StringObject Facets = new StringObject();
        dbtr.getThesaurusClass_Facet(selectedThesaurus, Q, sis_session.getValue(), Facets);

        // get instances of THES1Facet 
        Q.reset_name_scope();
        Q.set_current_node(Facets);

        int set_f = Q.get_instances(0);

        // get instances of THES1ObsoleteFacet 
        StringObject ObsoleteFacets = new StringObject();
        dbtr.getThesaurusClass_ObsoleteFacet(selectedThesaurus, Q, sis_session.getValue(), ObsoleteFacets);

        Q.reset_name_scope();
        Q.set_current_node(ObsoleteFacets);
        int set_of = Q.get_instances(0);

        Q.set_union(set_f, set_of);

        ArrayList<String> conceptsVector = new ArrayList<String>();

        Q.reset_set(set_f);
        //StringObject c_name = new StringObject();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_f, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                conceptsVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*
         while ((Q.retur_nodes( set_f, c_name)) != QClass.APIFail) {
         conceptsVector.add(c_name.getValue());
         }*/
        conceptsVector = removePrefix(conceptsVector);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(conceptsVector, new StringLocaleComparator(targetLocale));
        Q.free_set(set_f);
        Q.free_set(set_of);
        //Q.free_all_sets();

        return conceptsVector;
    }

    /*HLIAS---------------------------------------------------------------------
     getAvailableFacets()
     -----------------------------------------------------------------------
     FUNCTION: returns in a Vector all Concepts that are instances of 
     THES1Facet which are either new or released (not obsolete) in order 
     to be used at new /edit hierarchy functionalities.
     ----------------------------------------------------------------------*/
    public ArrayList<String> getAvailableFacets(String selectedThesaurus, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1Facet
        StringObject Facets = new StringObject();
        dbtr.getThesaurusClass_Facet(selectedThesaurus, Q, sis_session.getValue(), Facets);

        // get instances of THES1Facet 
        Q.reset_name_scope();
        Q.set_current_node(Facets);

        int set_f = Q.get_instances(0);

        ArrayList<String> conceptsVector = new ArrayList<String>();

        Q.reset_set(set_f);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_f, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                conceptsVector.add(row.get_v1_cls_logicalname());
            }
        }
        //StringObject c_name = new StringObject();
        //while ((Q.retur_nodes( set_f, c_name)) != QClass.APIFail) {
        //    conceptsVector.add(c_name.getValue());
        //}
        conceptsVector = removePrefix(conceptsVector);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(conceptsVector, new StringLocaleComparator(targetLocale));
        Q.free_set(set_f);
        //Q.free_all_sets();

        return conceptsVector;
    }

    /*HLIAS---------------------------------------------------------------------
     getSelectedFacets()
     -----------------------------------------------------------------------
     FUNCTION: returns in a Vector all Facets that targetHierarchy belongs to 
     Used in Hierarchy edit/delete page.
     ----------------------------------------------------------------------*/
    public ArrayList<String> getSelectedFacets(String selectedThesaurus, String targetHierarchy, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        ArrayList<String> returnVector = new ArrayList<String>();
        ArrayList<String> tempVector = new ArrayList<String>();

        if (targetHierarchy == null || targetHierarchy.length() == 0) {
            return returnVector;
        }

        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        StringObject targetHierarchyObj = new StringObject(prefix.concat(targetHierarchy));

        Q.reset_name_scope();
        Q.set_current_node(targetHierarchyObj);

        //Get SuperClasses of hierarchy and then filter out these that are not Facets
        int set_super = Q.get_superclasses(0);
        Q.reset_set(set_super);
        //StringObject c_name = new StringObject();

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_super, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                tempVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes( set_super, c_name)) != QClass.APIFail) {
         tempVector.add(c_name.getValue());
         }*/

        ArrayList<String> allfacets = getFacets(selectedThesaurus, Q, sis_session, targetLocale);
        tempVector = removePrefix(tempVector);

        for (int i = 0; i < tempVector.size(); i++) {
            if (allfacets.contains(tempVector.get(i))) {
                returnVector.add(tempVector.get(i));
            }
        }
        Q.free_set(set_super);
        //Q.free_all_sets();

        return returnVector;

    }

    /*---------------------------------------------------------------------
     getHierarchies()
     -----------------------------------------------------------------------
     FUNCTION: returns in a Vector all Concepts that are instances of the 
     THES1Hierarchy,  sorted alphabetically and without their prefixes
     ----------------------------------------------------------------------*/
    public ArrayList<String> getHierarchies(String selectedThesaurus, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1Hierarchy
        StringObject Hierarchies = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), Hierarchies);

        // get only the instances of THES1Hierarchy
        Q.reset_name_scope();
        Q.set_current_node(Hierarchies);
        int set_h = Q.get_instances(0);

        // get instances of THES1ObsoleteHierarchy
        StringObject ObsoleteHierarchies = new StringObject();
        dbtr.getThesaurusClass_ObsoleteHierarchy(selectedThesaurus, Q, sis_session.getValue(), ObsoleteHierarchies);

        Q.reset_name_scope();
        Q.set_current_node(ObsoleteHierarchies);
        int set_oh = Q.get_instances(0);

        Q.set_union(set_h, set_oh);

        ArrayList<String> conceptsVector = new ArrayList<String>();
        Q.reset_set(set_h);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_h, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                conceptsVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*
         StringObject c_name = new StringObject();
         while ((Q.retur_nodes( set_h, c_name)) != QClass.APIFail) {
         conceptsVector.add(c_name.getValue());
         }*/
        conceptsVector = removePrefix(conceptsVector);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(conceptsVector, new StringLocaleComparator(targetLocale));
        Q.free_set(set_h);
        Q.free_set(set_oh);
        //Q.free_all_sets();

        return conceptsVector;
    }

    /*---------------------------------------------------------------------
     getDescriptors()
     -----------------------------------------------------------------------
     FUNCTION: returns in a Vector all Concepts that are the union of the 
     instances of the THES1opTerm and THES1Descriptor, sorted alphabetically
     and without their prefixes
     ----------------------------------------------------------------------*/
    public ArrayList<String> getDescriptors(String selectedThesaurus, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        int sisSessionId = sis_session.getValue();
        int set_d = getDescriptorsSet(selectedThesaurus, Q, sis_session);
        ArrayList<String> conceptsVector = new ArrayList<String>();
        Q.reset_set(set_d);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_d, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                conceptsVector.add(row.get_v1_cls_logicalname());
            }
        }
        //StringObject c_name = new StringObject();
        //while ((Q.retur_nodes( set_d, c_name)) != QClass.APIFail) {
        //conceptsVector.add(c_name.getValue());
        //}
        conceptsVector = removePrefix(conceptsVector);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(conceptsVector, new StringLocaleComparator(targetLocale));
        Q.free_set(set_d);
        //Q.free_all_sets();

        return conceptsVector;
    }

    /*---------------------------------------------------------------------
     getDescriptorsSet()
     -----------------------------------------------------------------------
     FUNCTION: returns in a set all the Descriptors that are the union of the 
     instances of the THES1opTerm and THES1Descriptor, sorted alphabetically
     and without their prefixes
     ----------------------------------------------------------------------*/
    public int getDescriptorsSet(String selectedThesaurus, QClass Q, IntegerObject sis_session) {
        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1Descriptor 
        StringObject Desc = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), Desc);

        // get all instances of THES1Descriptor
        Q.reset_name_scope();
        Q.set_current_node(Desc);
        int set_d = Q.get_all_instances(0);

        // looking for THES1TopTerm 
        StringObject TopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTerm);

        // get all instances of THES1TopTerm
        Q.reset_name_scope();
        Q.set_current_node(TopTerm);
        int set_t = Q.get_all_instances(0);

        // looking for THES1ObsoleteDescriptor 
        StringObject ObsoleteDescriptor = new StringObject();
        dbtr.getThesaurusClass_ObsoleteDescriptor(selectedThesaurus, Q, sis_session.getValue(), ObsoleteDescriptor);

        // get all instances of THES1ObsoleteDescriptor
        Q.reset_name_scope();
        Q.set_current_node(ObsoleteDescriptor);
        int set_o = Q.get_all_instances(0);

        // get the union of the two sets
        Q.reset_set(set_d);
        Q.reset_set(set_t);
        Q.reset_set(set_o);
        Q.set_union(set_d, set_t);
        Q.reset_set(set_d);
        Q.set_union(set_d, set_o);
        Q.reset_set(set_d);
        Q.free_set(set_t);
        Q.free_set(set_o);

        return set_d;
    }

    /* Removes the prefix aka the string before the '`' from all the string that the vector contains.
     * Returns a new vector that contains all thesame string without the prefix.
     */
    public ArrayList removePrefix(ArrayList<String> v) {
        String term_split[];
        ArrayList<String> vec = new ArrayList<String>();
        String result;

        for (int i = 0; i < v.size(); i++) {
            /*term_split = ( v.get(i)).split("`");
             if (term_split.length == 1) {
             vec.add(term_split[0]);
             continue;
             }
             result = new String(term_split[1]);*/
            if (v.get(i).contains("`")) {
                result = v.get(i).substring(v.get(i).indexOf("`") + 1);
            } else {
                result = v.get(i);
            }
            vec.add(result);
        }
        return (vec);
    }

    public ArrayList removeCmvVectorPrefix(ArrayList<CMValue> v) {
        String term_split[];
        ArrayList<CMValue> vec = new ArrayList<CMValue>();
        String result;

        CMValue newCmv = new CMValue();
        for (int i = 0; i < v.size(); i++) {

            CMValue itemI = v.get(i);

            if (itemI.getString().contains("`")) {
                result = itemI.getString().substring(itemI.getString().indexOf("`") + 1);
            } else {
                result = itemI.getString();
            }
            newCmv.assign_node(result, itemI.getSysid(), itemI.getTransliterationString(), itemI.getRefid());
            vec.add(newCmv.getCmvCopy());
        }
        return (vec);
    }

    public ArrayList<SortItem> removeSortItemArrayListPrefix(ArrayList<SortItem> v) {
        String term_split[];
        ArrayList<SortItem> vec = new ArrayList<SortItem>();
        String result;

        for (int i = 0; i < v.size(); i++) {

            SortItem itemI = v.get(i);

            if (itemI.getLogName().contains("`")) {
                result = itemI.getLogName().substring(itemI.getLogName().indexOf("`") + 1);
            } else {
                result = itemI.getLogName();
            }
            vec.add(new SortItem(result, itemI.getSysId(), itemI.getLinkClass(), itemI.getLogNameTransliteration(), itemI.getThesaurusReferenceId()));
        }
        return (vec);
    }

    /*---------------------------------------------------------------------
     removePrefix()
     -----------------------------------------------------------------------
     INPUT: - String s: the string to be parsed
     OUTPUT: a string without the prefix (after character "`")
     ----------------------------------------------------------------------*/
    public String removePrefix(String s) {
        /*String[] res = s.split("`");
         if (res.length == 1) {
         //return s;
         return "";
         }
         if (res[1] != null) {
         return res[1];
         }
         /*if(s!=null && s.length()>0){
         s = s.replaceAll(" +", " ").trim();
         }*/
        if (s.contains("`")) {
            return s.substring(s.indexOf("`") + 1);
        } else {
            return s;
        }
        //return result;
    }

    /*---------------------------------------------------------------------
     CMVtoString()
     -----------------------------------------------------------------------
     INPUT: - CMValue cmv: the given CMValue
     OUTPUT: the value of the given CMValue in a String
     ----------------------------------------------------------------------*/
    public String CMVtoString(CMValue cmv) {
        String CMVStringValue = "";
        if (cmv.getType() == CMValue.TYPE_NODE) {
            CMVStringValue = removePrefix(cmv.getString());
        } else if (cmv.getType() == CMValue.TYPE_STRING) {
            CMVStringValue = cmv.getString();
        } else if (cmv.getType() == CMValue.TYPE_INT) {
            IntegerObject intObj = new IntegerObject(cmv.getInt());
            CMVStringValue = intObj.toString();
        }
        /*/else if (cmv.getType() == CMValue.TYPE_FLOAT) {
         Float floatObj = new Float(cmv.getFloat());
         CMVStringValue = floatObj.toString();
         } else if (cmv.getType() == CMValue.TYPE_TIME) {
         CMVStringValue = cmv.getTime().toString();
         } else if (cmv.getType() == CMValue.TYPE_SYSID) {
         IntegerObject intObj = new IntegerObject(cmv.getSysid());
         CMVStringValue = intObj.toString();
         }*/

        return CMVStringValue;
    }

    public ArrayList<SortItem> getTranslationLinkValues(String selectedThesaurus, boolean preffered, String target, QClass Q, IntegerObject sis_session) {
        int API_sessionID = sis_session.getValue();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject fromClassObj = new StringObject();
        StringObject linkObj = new StringObject();
        if (preffered) {
            getKeywordPair(selectedThesaurus, ConstantParameters.translation_kwd, fromClassObj, linkObj, Q, sis_session);
        } else {
            getKeywordPair(selectedThesaurus, ConstantParameters.uf_translations_kwd, fromClassObj, linkObj, Q, sis_session);
        }

        Q.reset_name_scope();
        StringObject name = new StringObject();
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject targetTerm = new StringObject(prefix.concat(target));

        Q.reset_name_scope();
        Q.set_current_node(targetTerm);
        int linkSet = Q.get_link_from_by_category(0, fromClassObj, linkObj);

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject traversed = new IntegerObject();
        //CMValue cmv = new CMValue();
        ArrayList<SortItem> linkValue = new ArrayList<SortItem>();

        Q.reset_set(linkSet);
        int translationSubStringLength = ConstantParameters.thesaursTranslationCategorysubString.length();
        ArrayList<Return_Full_Link_Row> retFLVals = new ArrayList<Return_Full_Link_Row>();
        if (Q.bulk_return_full_link(linkSet, retFLVals) != QClass.APIFail) {
            for (Return_Full_Link_Row row : retFLVals) {
                String subCategory = row.get_v3_categ().substring(row.get_v3_categ().indexOf(ConstantParameters.thesaursTranslationCategorysubString) + translationSubStringLength);
                linkValue.add(new SortItem(this.CMVtoString(row.get_v5_cmv()), -1, subCategory));
            }
        }
        /*
         while (Q.retur_full_link( linkSet, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

         String subCategory = categ.getValue().substring(categ.getValue().indexOf(ConstantParameters.thesaursTranslationCategorysubString)+ translationSubStringLength);
         linkValue.add(new SortItem(this.CMVtoString(cmv),-1,subCategory));
         }*/
        Q.reset_set(linkSet);
        Q.free_set(linkSet);

        return linkValue;
    }

    /*---------------------------------------------------------------------
     getLinkValue()
     -----------------------------------------------------------------------
     INPUT: - String target: the target term
     - String link_name: the name a specific category (g.e. "THES1_RT")
     OUTPUT: a Vector with the to-values of the links under the specified category of the target term
     ----------------------------------------------------------------------*/

    public ArrayList<String> getLinkValue(String selectedThesaurus, String target, String fromClass, String link_name, QClass Q, IntegerObject sis_session) {

        int API_sessionID = sis_session.getValue();
        // looking for THES1HierarchyTerm 
        StringObject from = new StringObject(fromClass);
        StringObject createdObj = new StringObject();
        StringObject modifiedObj = new StringObject();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), createdObj);
        dbtr.getThesaurusCategory_modified(selectedThesaurus, Q, sis_session.getValue(), modifiedObj);
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());

        StringObject name = new StringObject();

        StringObject target_el = new StringObject(prefix_el.concat(target));

        Q.reset_name_scope();
        Q.set_current_node(target_el);
        int linkSet = Q.get_link_from_by_category(0, from, new StringObject(link_name));

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject traversed = new IntegerObject();
        //CMValue cmv = new CMValue();
        ArrayList<String> linkValue = new ArrayList<String>();
        Q.reset_set(linkSet);
        int card1 = Q.set_get_card(linkSet);

        ArrayList<Return_Full_Link_Row> retFLVals = new ArrayList<Return_Full_Link_Row>();
        if (Q.bulk_return_full_link(linkSet, retFLVals) != QClass.APIFail) {
            for (Return_Full_Link_Row row : retFLVals) {
                //while (Q.retur_full_link( linkSet, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
                // do not collect the instances of the subclasses of the given category (see the case of thes1_found_in)
                if (row.get_v3_categ().equals(link_name) == false) {
                    continue;
                }
                //bug fix for times because it deos not return TelosTime Primitives as expected
                if (row.get_v3_categ().compareTo(createdObj.getValue()) == 0
                        || row.get_v3_categ().compareTo(modifiedObj.getValue()) == 0) {
                    name.setValue(row.get_v5_cmv().getString());
                } else {
                    name.setValue(CMVtoString(row.get_v5_cmv()));
                }

                linkValue.add(name.getValue());
            }
        }
        /*
         while (Q.retur_full_link( linkSet, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
         //while (Q.retur_link( linkSet, cls, label, cmv) != QClass.APIFail) {
         // do not collect the instances of the subclasses of the given category (see the case of thes1_found_in)
         if (categ.getValue().equals(link_name) == false) {
         continue;
         }
         //bug fix for times because it deos not return TelosTime Primitives as expected
         if(categ.getValue().compareTo(createdObj.getValue()) ==0 || 
         categ.getValue().compareTo(modifiedObj.getValue()) ==0  ){
         name.setValue(cmv.getString());
         }
         else{
         name.setValue(CMVtoString(cmv));
         }

         linkValue.add(name.getValue());
         }
         */

        Q.free_set(linkSet);
        return linkValue;
    }

    //gia na kanoume search me krithrio get_from_node...
    /* Returns the value of the link given as a parameter.
     * The link is pointing from the concept given as parameter.
     * If the concept has multiple links with the same name, 
     * the values are returned in a string with the delimeter '###' between them.
     */
    public ArrayList<String> getLink(String selectedThesaurus, String concept, String link_name, QClass Q, IntegerObject sis_session) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject dn_link = new StringObject();
        StringObject tc_link = new StringObject();
        StringObject primary_link = new StringObject();
        StringObject tr_link = new StringObject();
        StringObject uf_link = new StringObject();

        dbtr.getThesaurusCategory_dewey(selectedThesaurus, dn_link);
        dbtr.getThesaurusCategory_taxonomic_code(selectedThesaurus, tc_link);
        dbtr.getThesaurusCategory_primary_found_in(selectedThesaurus, primary_link);
        dbtr.getThesaurusCategory_translations_found_in(selectedThesaurus, tr_link);
        dbtr.getThesaurusCategory_UF(selectedThesaurus, Q, sis_session.getValue(), uf_link);
        Q.reset_name_scope();

        ArrayList<String> linkValue = new ArrayList<String>();
        int set;
        StringObject link;

        StringObject from = new StringObject(selectedThesaurus.concat("HierarchyTerm"));

        //TODO: repair inconsistency in database prefix.. eg. THES1_UF, thes1_dewey 
        if (link_name.equals(ConstantParameters.dn_kwd)) {
            link = new StringObject(dn_link.getValue());
        } else if (link_name.equals(ConstantParameters.tc_kwd)) {
            link = new StringObject(tc_link.getValue());
        } else if (link_name.equals(ConstantParameters.primary_found_in_kwd)) {
            link = new StringObject(primary_link.getValue());
        } else if (link_name.equals(ConstantParameters.translations_found_in_kwd)) {
            link = new StringObject(tr_link.getValue());
            /*} else if (link_name.equals(bt_found_in_kwd)) {
             link = new StringObject(SessionUserInfo.selectedThesaurus.toLowerCase().concat("_bt_found_in"));*/
        } else if (link_name.equals(ConstantParameters.uf_kwd)) {
            link = new StringObject(uf_link.getValue());
        } else if (/*link_name.equals(uk_alt_kwd) ||*/link_name.equals(ConstantParameters.uf_translations_kwd)) {
            //linkValue.add("LINK NAME = " + link_name + " value = " + concept);
            link = new StringObject(selectedThesaurus.concat("_" + link_name));
        } else {
            link = new StringObject(selectedThesaurus.concat("_" + link_name.toUpperCase()));
        }
        //StringObject name = new StringObject();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        String prefix_en = dbtr.getThesaurusPrefix_EnglishWord(Q, sis_session.getValue());
        String prefix_dn = dbtr.getThesaurusPrefix_DeweyNumber(Q, sis_session.getValue());
        String prefix_src = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
        StringObject concept_el;
        if (/*link_name.equals(uk_alt_kwd) || */link_name.equals(ConstantParameters.uf_translations_kwd)) {
            concept_el = new StringObject(prefix_en.concat(concept));
        } else if (link_name.equals(ConstantParameters.dn_kwd)) {
            concept_el = new StringObject(prefix_dn.concat(concept));
        } else if (link_name.equals(ConstantParameters.primary_found_in_kwd) || link_name.equals(ConstantParameters.translations_found_in_kwd) /*|| link_name.equals(bt_found_in_kwd)*/) {
            concept_el = new StringObject(prefix_src.concat(concept));
        } else {
            concept_el = new StringObject(prefix_el.concat(concept));
        }
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        set = Q.get_from_node_by_category(0, from, link);
        Q.reset_set(set);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                //link_value = link_value.concat(name.getValue() + "###");
                linkValue.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*
         while ((Q.retur_nodes(set, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         //link_value = link_value.concat(name.getValue() + "###");
         linkValue.add(name.getValue());
         }*/
        Q.free_set(set);

        //linkValue.add("link = "+ link + " concept = " + concept_el);
        return linkValue;
    }

    /*----------------------------------------------------------------------
     getFromRTLinks()
     ------------------------------------------------------------------------
     INPUT: - String target: the target to be queried
     OUTPUT : a Vector with the from values of the given
     target under category RT (g.e. THES1HierarchyTerm->THES1_RT)
     FUNCTION: collects in a Vector the from values of the given 
     target under category RT (g.e. THES1HierarchyTerm->THES1_RT)
     ------------------------------------------------------------------------*/
    public ArrayList<String> getFromRTLinks(String selectedThesaurus, String target, QClass Q, IntegerObject sis_session) {
        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), thesHierarchyTerm);
        // looking for category THES1_RT
        StringObject thes_RT = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), thes_RT);
        // looking for Descriptor prefix	
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        // construct target with prefix and SIS encoding
        StringObject concept_el = new StringObject(prefix_el.concat(target));

        // get the from RT values
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        int set = Q.get_from_node_by_category(0, thesHierarchyTerm, thes_RT);
        Q.reset_set(set);
        // fill the vector	
        ArrayList<String> res = new ArrayList<String>();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                res.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        //StringObject name = new StringObject();
        /*while ((Q.retur_nodes( set, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         res.add(name.getValue());
         }*/
        Q.free_set(set);
        return res;
    }

    /*----------------------------------------------------------------------
     getRTlinksBothDirections()
     ------------------------------------------------------------------------
     INPUT: - String target: the target to be queried
     OUTPUT : a Vector with the union of the from and to values of the given
     target under category RT (g.e. THES1HierarchyTerm->THES1_RT)
     FUNCTION: collects in a Vector the union of the from and to values of the given 
     target under category RT (g.e. THES1HierarchyTerm->THES1_RT)
     ------------------------------------------------------------------------*/
    public ArrayList<String> getRTlinksBothDirections(UserInfoClass SessionUserInfo, String target, QClass Q, IntegerObject sis_session) {
        int sisSessionId = sis_session.getValue();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1HierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), thesHierarchyTerm);
        // looking for THES1_RT 
        StringObject thesRT = new StringObject();
        dbtr.getThesaurusCategory_RT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), thesRT);
        // looking for prefix (EL`)
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        // construct target with prefix and SIS encoding
        StringObject targetWithPrefix = new StringObject(prefix_el.concat(target));

        // get the from and to RT values
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(targetWithPrefix);
        int setFrom = Q.get_from_node_by_category(0, thesHierarchyTerm, thesRT);
        Q.reset_set(setFrom);
        int setTo = Q.get_to_node_by_category(0, thesHierarchyTerm, thesRT);
        Q.reset_set(setTo);
        // get their union (result to setFrom)
        Q.set_union(setFrom, setTo);
        // fill the vector
        ArrayList<String> RTsVector = new ArrayList<String>();
        Q.reset_set(setFrom);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        setFrom = dbf.FilterTermsResults(SessionUserInfo, setFrom, Q, sis_session);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(setFrom, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                RTsVector.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        //StringObject name = new StringObject();
        /*while (Q.retur_nodes( setFrom, name) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         RTsVector.add(name.getValue());
         }*/
        Q.free_set(setFrom);
        Q.free_set(setTo);

        return RTsVector;
    }

    public ArrayList<SortItem> getBT_NTwithGuideTerms(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session, String descriptor, int direction) {

        DBFilters dbf = new DBFilters();// FILTER DB results depending on user group
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();

        getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);

        // add the prefix to the descriptor
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject descriptor_el = new StringObject(prefix_el.concat(descriptor));

        ArrayList<SortItem> results = new ArrayList<SortItem>();

        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();
        Q.reset_name_scope();
        Q.set_current_node(descriptor_el);

        if (direction == ConstantParameters.BT_DIRECTION) {
            int set = Q.get_link_from_by_category(0, BTClassObj, BTLinkObj);
            Q.reset_set(set);
            int set_filtered = dbf.FilterBTLinksSet(SessionUserInfo, set, Q, sis_session);
            Q.reset_set(set_filtered);

            ArrayList<Return_Full_Link_Row> retFLVals = new ArrayList<Return_Full_Link_Row>();
            if (Q.bulk_return_full_link(set_filtered, retFLVals) != QClass.APIFail) {
                for (Return_Full_Link_Row row : retFLVals) {
                    SortItem newItem = new SortItem(removePrefix(row.get_v5_cmv().getString()), row.get_v5_cmv().getSysid(), row.get_v3_categ());
                    results.add(newItem);
                }
            }
            /*while (Q.retur_full_link_id(set_filtered, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                
             SortItem newItem = new SortItem(removePrefix(cmv.getString()),cmv.getSysid(),categ.getValue());
             results.add(newItem);
             }*/
            Q.free_set(set_filtered);
            Q.free_set(set);

        } else {
            int set = Q.get_link_to_by_category(0, BTClassObj, BTLinkObj);
            Q.reset_set(set);
            int set_filtered = dbf.FilterBTLinksSet(SessionUserInfo, set, Q, sis_session);
            Q.reset_set(set);
            ArrayList<Return_Full_Link_Id_Row> retFLIVals = new ArrayList<Return_Full_Link_Id_Row>();
            if (Q.bulk_return_full_link_id(set_filtered, retFLIVals) != QClass.APIFail) {
                for (Return_Full_Link_Id_Row row : retFLIVals) {
                    SortItem newItem = new SortItem(removePrefix(row.get_v1_cls()), row.get_v2_clsid(), row.get_v5_categ());
                    results.add(newItem);
                }
            }
            /*while (Q.retur_full_link_id(set_filtered, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                
             SortItem newItem = new SortItem(removePrefix(cls.getValue()),clsID.getValue(),categ.getValue());
             results.add(newItem);
             }*/
            Q.free_set(set_filtered);
            Q.free_set(set);
        }

        return results;

    }

    /*---------------------------------------------------------------------
     getBT_NT()
     -----------------------------------------------------------------------
     INPUT: - String descriptor: the target descriptor
     - int direction: BT_DIRECTION / NT_DIRECTION
     OUTPUT: - Vector resultVector: filled with the broader / narrower terms of the given descriptor 
     FUNCTION: gets the broader / narrower terms of the given descriptor
     ----------------------------------------------------------------------*/

    public ArrayList<String> getBT_NT(UserInfoClass SessionUserInfo, String descriptor, int direction, QClass Q, IntegerObject sis_session) {

        ArrayList<String> resultVector = new ArrayList<String>();

        ArrayList<SortItem> resultVectorInSortItems = getBT_NTInSortItems(SessionUserInfo, descriptor, direction, Q, sis_session);
        if (resultVectorInSortItems != null) {
            for (SortItem item : resultVectorInSortItems) {
                resultVector.add(item.getLogName());
            }
        }
        return resultVector;
    }

    public ArrayList<SortItem> getBT_NTInSortItems(UserInfoClass SessionUserInfo, String descriptor, int direction, QClass Q, IntegerObject sis_session) {
        int SISsessionID = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1Descriptor
        StringObject from = new StringObject();
        dbtr.getThesaurusClass_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), from);
        // looking for THES1_BT
        StringObject link = new StringObject();
        dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), link);
        // add the prefix to the descriptor
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject descriptor_el = new StringObject(prefix_el.concat(descriptor));

        Q.reset_name_scope();
        long sysidL = Q.set_current_node(descriptor_el);
        int set;
        if (direction == ConstantParameters.BT_DIRECTION) {
            set = Q.get_to_node_by_category(0, from, link);
        } else {
            set = Q.get_from_node_by_category(0, from, link);
        }

        ArrayList<SortItem> resultVector = new ArrayList<SortItem>();
        Q.reset_set(set);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set = dbf.FilterTermsResults(SessionUserInfo, set, Q, sis_session);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                String logname = removePrefix(row.get_v1_cls_logicalname());
                String translit = row.get_v3_cls_transliteration();
                long refId = row.get_v2_long_referenceId();
                resultVector.add(new SortItem(logname, -1, translit, refId));
            }
        }
        //StringObject name = new StringObject();
        /*while ((Q.retur_nodes(set, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         resultVector.add(name.getValue());
         }*/
        Q.free_set(set);
        return resultVector;
    }

    public ArrayList<SortItem> returnResultsInSortItems(UserInfoClass SessionUserInfo, String term, String output, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        if (output.equals(ConstantParameters.bt_kwd)) {
            return getBT_NTInSortItems(SessionUserInfo, term, ConstantParameters.BT_DIRECTION, Q, sis_session);
        } else if (output.equals(ConstantParameters.nt_kwd)) {
            return getBT_NTInSortItems(SessionUserInfo, term, ConstantParameters.NT_DIRECTION, Q, sis_session);
        } else if (output.equals(ConstantParameters.facet_kwd)) {
            return get_term_facets_or_topterms_InSortItemVector(SessionUserInfo, term, ConstantParameters.get_Term_Facets, Q, sis_session);
        } else if (output.equals(ConstantParameters.topterm_kwd)) {
            return get_term_facets_or_topterms_InSortItemVector(SessionUserInfo, term, ConstantParameters.get_Term_Top_Terms, Q, sis_session);
        }
        return new ArrayList<SortItem>();
    }

    /*---------------------------------------------------------------------
     returnResults()
     -----------------------------------------------------------------------
     INPUT: - String output: a keyword with the name of a category (g.e. ConstantParameters.bt_kwd, ConstantParameters.nt_kwd, ConstantParameters.rt_kwd)
     - IntegerObject sis_session: current SIS session
     - String term: the target term
     OUTPUT: - a Vector filled with the to-values of the links of the target 
     term under the specified category 
     ----------------------------------------------------------------------*/
    public ArrayList<String> returnResults(UserInfoClass SessionUserInfo, String term, String output, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        //term is in ui encoding and without prefix
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+term + " " +output );      
        if (output.equals(ConstantParameters.bt_kwd)) {
            return getBT_NT(SessionUserInfo, term, ConstantParameters.BT_DIRECTION, Q, sis_session);
        } else if (output.equals(ConstantParameters.nt_kwd)) {
            return getBT_NT(SessionUserInfo, term, ConstantParameters.NT_DIRECTION, Q, sis_session);
        } else if (output.equals(ConstantParameters.rt_kwd)) {
            return getRTlinksBothDirections(SessionUserInfo, term, Q, sis_session);
        } else if (output.equals(ConstantParameters.facet_kwd)) {
            return get_term_facets_or_topterms(SessionUserInfo, term, ConstantParameters.get_Term_Facets, Q, sis_session);
        } else if (output.equals(ConstantParameters.topterm_kwd)) {
            return get_term_facets_or_topterms(SessionUserInfo, term, ConstantParameters.get_Term_Top_Terms, Q, sis_session);
        } else if (output.equals("accepted")) {
            return get_accepted_status(SessionUserInfo.selectedThesaurus, term, Q, sis_session);
        } else if (output.equals("status")) {
            return get_term_status(SessionUserInfo, term, Q, sis_session);
        } else if (output.equals(ConstantParameters.comment_kwd) ||
                output.equals(ConstantParameters.scope_note_kwd) || 
                output.equals(ConstantParameters.translations_scope_note_kwd) ||
                output.equals(ConstantParameters.historical_note_kwd)||
                output.equals(ConstantParameters.note_kwd)) {
            //else if (output.equals("comment")) {
            ArrayList<String> temp = getTermComment(SessionUserInfo.selectedThesaurus, term, output, Q, TA, sis_session);
            //if(output.equals(ConstantParameters.translations_scope_note_kwd)){

            //}
            //else{
            return temp;
            //}
        } else if (output.equals(ConstantParameters.translation_kwd) || output.equals(ConstantParameters.uf_translations_kwd)) {
            ArrayList<SortItem> tempVector = getTranslationLinkValues(SessionUserInfo.selectedThesaurus, output.equals(ConstantParameters.translation_kwd), term, Q, sis_session);
            ArrayList<String> retrunVec = new ArrayList<String>();
            for (int i = 0; i < tempVector.size(); i++) {
                String newtranslation = tempVector.get(i).log_name;
                if (retrunVec.contains(newtranslation) == false) {
                    retrunVec.add(newtranslation);
                }
            }
            return retrunVec;
        } else {
            StringObject fromClassObj = new StringObject();
            StringObject linkObj = new StringObject();
            getKeywordPair(SessionUserInfo.selectedThesaurus, output, fromClassObj, linkObj, Q, sis_session);
            return getLinkValue(SessionUserInfo.selectedThesaurus, term, fromClassObj.getValue(), linkObj.getValue(), Q, sis_session);
        }
    }

    ArrayList<String> get_accepted_status(String selectedThesaurus, String term, QClass Q, IntegerObject sis_session) {

        ArrayList<String> acceptedOrNot = new ArrayList<String>();

        StringObject acceptedObj = new StringObject();
        StringObject notAcceptedObj = new StringObject();
        StringObject termObj = new StringObject();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        dbtr.getThesaurusClass_StatusTermAccepted(selectedThesaurus, acceptedObj);
        dbtr.getThesaurusClass_StatusTermNotAccepted(selectedThesaurus, notAcceptedObj);
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());

        termObj.setValue(prefix.concat(term));

        Q.reset_name_scope();
        Q.set_current_node(termObj);
        int set_classes = Q.get_all_classes(0);
        Q.reset_set(set_classes);

        ArrayList<String> ClassNames = get_Node_Names_Of_Set(set_classes, false, Q, sis_session);
        Q.free_set(set_classes);

        if (ClassNames.contains(acceptedObj.getValue())) {
            acceptedOrNot.add("yes");
        } else if (ClassNames.contains(notAcceptedObj.getValue())) {
            acceptedOrNot.add("no");
        }

        return acceptedOrNot;
    }

    ArrayList<String> get_term_status(Users.UserInfoClass SessionUserInfo, String term, QClass Q, IntegerObject sis_session) {

        ArrayList<String> termStatus = new ArrayList<String>();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject statusUnderConstructionObj = new StringObject();
        StringObject statusForApprovalObj = new StringObject();
        StringObject statusForInsertionObj = new StringObject();
        StringObject statusForReinspectionObj = new StringObject();
        StringObject statusApprovedObj = new StringObject();
        StringObject termObj = new StringObject();

        dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus, statusUnderConstructionObj);
        dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus, statusForApprovalObj);
        dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus, statusForInsertionObj);
        dbtr.getThesaurusClass_StatusForReinspection(SessionUserInfo.selectedThesaurus, statusForReinspectionObj);
        dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, statusApprovedObj);

        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        termObj.setValue(prefix.concat(term));

        Q.reset_name_scope();
        Q.set_current_node(termObj);
        int set_classes = Q.get_all_classes(0);
        Q.reset_set(set_classes);

        ArrayList<String> ClassNames = get_Node_Names_Of_Set(set_classes, false, Q, sis_session);
        Q.free_set(set_classes);

        if (ClassNames.contains(statusUnderConstructionObj.getValue())) {
            termStatus.add(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction, SessionUserInfo));
        } else if (ClassNames.contains(statusForApprovalObj.getValue())) {
            termStatus.add(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Approval, SessionUserInfo));
        } else if (ClassNames.contains(statusForInsertionObj.getValue())) {
            termStatus.add(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion, SessionUserInfo));
        } else if (ClassNames.contains(statusForReinspectionObj.getValue())) {
            termStatus.add(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Reinspection, SessionUserInfo));
        } else if (ClassNames.contains(statusApprovedObj.getValue())) {
            termStatus.add(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Approved, SessionUserInfo));
        }

        return termStatus;
    }

    /*---------------------------------------------------------------------
     returnResults_Facet()
     -----------------------------------------------------------------------
     INPUT: - String output: a keyword with the name of a category (e.g. letter_code)
     - IntegerObject sis_session: current SIS session
     - String facet: the target facet
     OUTPUT: - a Vector filled with the to-values of the links of the target 
     facet under the specified category 
     ----------------------------------------------------------------------*/
    public ArrayList<String> returnResults_Facet(UserInfoClass SessionUserInfo, String facet, String output, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        ArrayList<String> abortVctr = new ArrayList<String>();
        if (output.equals("hierarchy")) {
            return getFacetHierarchies(SessionUserInfo, facet, Q, sis_session, targetLocale);
        } else if (output.equals("letter_code")) {
            return getLinkValue_Facet(SessionUserInfo.selectedThesaurus, facet, "letter_code", Q, sis_session, targetLocale);
        } else {
            return abortVctr;
        }
    }

    public ArrayList<SortItem> returnResults_FacetInSortItems(UserInfoClass SessionUserInfo, String facet, String output, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        ArrayList<SortItem> returnVec = new ArrayList<SortItem>();
        if (output.equals("hierarchy")) {
            return getFacetHierarchiesInSortItems(SessionUserInfo, facet, Q, sis_session, targetLocale);
        }
        return returnVec;
    }

    /*---------------------------------------------------------------------
     returnResults_Hierarchy()
     -----------------------------------------------------------------------
     INPUT: - String output: a keyword with the name of a category (e.g. letter_code)
     - IntegerObject sis_session: current SIS session
     - String facet: the target facet
     OUTPUT: - a Vector filled with the to-values of the links of the target 
     facet under the specified category 
     ----------------------------------------------------------------------*/
    public ArrayList<String> returnResults_Hierarchy(UserInfoClass SessionUserInfo, String hierarchy, String output, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        ArrayList<String> abortVctr = new ArrayList<String>();
        if (output.equals(ConstantParameters.facet_kwd)) {
            return getHierarchyFacets(SessionUserInfo, hierarchy, Q, sis_session, targetLocale);
        }
        /*
         if (output.equals("letter_code")) {
         return getLink1Value_Hierarchy(SessionUserInfo.selectedThesaurus,hierarchy, "letter_code",Q,sis_session,targetLocale);
         } else {*/
        return abortVctr;
        //}
    }
    
    public ArrayList<SortItem> returnResults_HierarchyInSortItems(UserInfoClass SessionUserInfo, String hierarchy, String output, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        ArrayList<SortItem> returnVec = new ArrayList<>();
        
        if (output.equals(ConstantParameters.facet_kwd)) {
            return getHierarchyFacetsInSortItems(SessionUserInfo, hierarchy, Q, sis_session, targetLocale);
        }
        return returnVec;
    }


    /*---------------------------------------------------------------------
     getLinkValue_Hierarchy()
     -----------------------------------------------------------------------
     INPUT: - String target: the target hierarchy
     - String link_name: the name a specific category (g.e. "THES1_RT")
     OUTPUT: a Vector with the to-values of the links under the specified category of the target term
     ----------------------------------------------------------------------
     public ArrayList<String> getLinkValue_Hierarchy(String selectedThesaurus,String target, String link_name,QClass Q, IntegerObject sis_session,Locale targetLocale) {

     if(target==null || target.length()==0)
     return new ArrayList<String>();
        
     ArrayList<String> linkValue = new ArrayList<String>();
     int sisSessionId = sis_session.getValue();
     DBThesaurusReferences dbtr = new DBThesaurusReferences();

     String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus,Q,sis_session.getValue());

     StringObject targetHierarchy = new StringObject(prefix.concat(target));

     // get instances of THES1Hierarchy
     Q.reset_name_scope();
     Q.set_current_node( targetHierarchy);

     int linkFromSet = Q.get_inher_link_from( 0);

     Q.reset_set( linkFromSet);

     StringObject fromcls = new StringObject();
     StringObject label = new StringObject();
     StringObject categ = new StringObject();
     StringObject cls = new StringObject();
     IntegerObject uniq_categ = new IntegerObject();
     IntegerObject traversed = new IntegerObject();
     CMValue cmv = new CMValue();


     while (Q.retur_full_link( linkFromSet, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {

     if (categ.getValue().equals(link_name) == false) {
     continue;
     }
            
     //            if (link_name.equals("letter_code")) {
     //                ArrayList<String> tempVec = new ArrayList<String>();
     //                if (targetHierarchy.getValue().equals(cls.getValue())) {
     //                    tempVec.add(cmv.getString());
     //                    tempVec.add("enabled");
     //                } else {
     //                    tempVec.add(cmv.getString());
     //                    tempVec.add("disabled");
     //                }
     //
     //                linkValue.add(tempVec);
     //            }
     //            else
     //                linkValue.add(cmv.getString());
             
     }

     if (!link_name.equals("letter_code")) {
     linkValue = removePrefix(linkValue);
     //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
     Collections.sort(linkValue,new StringLocaleComparator(targetLocale));

     }
     Q.free_set(linkFromSet);
     //Q.free_all_sets();

     return linkValue;
     }
     */
 /*---------------------------------------------------------------------
     getLinkValue_Facet()
     -----------------------------------------------------------------------
     INPUT: - String target: the target facet
     - String link_name: the name a specific category (g.e. "THES1_RT")
     OUTPUT: a Vector with the to-values of the links under the specified category of the target term
     ----------------------------------------------------------------------*/
    public ArrayList getLinkValue_Facet(String selectedThesaurus, String facet, String link_name, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        ArrayList<String> linkValue = new ArrayList<String>();
        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        StringObject targetFacet = new StringObject(prefix.concat(facet));

        // get instances of THES1Facet 
        Q.reset_name_scope();
        Q.set_current_node(targetFacet);

        String targetFromClass = "";
        if (link_name.equals("letter_code")) {
            targetFromClass = ConstantParameters.facet_kwd;
        }
        int linkFromSet = Q.get_link_from_by_category(0, new StringObject(targetFromClass), new StringObject(link_name));

        Q.reset_set(linkFromSet);

        //StringObject label = new StringObject();
        //StringObject cls = new StringObject();
        //CMValue cmv = new CMValue();
        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(linkFromSet, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                linkValue.add(row.get_v3_cmv().getString());
            }
        }
        /*while (Q.retur_link( linkFromSet, cls, label, cmv) != QClass.APIFail) {

         linkValue.add(cmv.getString());
         }
         */

        linkValue = removePrefix(linkValue);
        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(linkValue, new StringLocaleComparator(targetLocale));
        Q.free_set(linkFromSet);
        //Q.free_all_sets();

        return linkValue;
    }

    /*---------------------------------------------------------------------
     getTermComment()
     -----------------------------------------------------------------------
     INPUT:- String targetTerm: the target term
     - String commentKind: "comment" (for THES1ThesaurusConcept->thes1_comment),
     "scope_note" (for THES1ThesaurusConcept->thes1_scope_note) or "historical_note" (for THES1ThesaurusConcept->thes1_historical_note)
     OUTPUT: - a Vector filled with the comment string found
     ----------------------------------------------------------------------*/
    public ArrayList<String> getTermComment(String selectedThesaurus, String targetTerm, String commentKind, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        ArrayList<String> result = new ArrayList<String>();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        // looking for THES1ThesaurusConcept
        StringObject from = new StringObject();
        dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), from);

        StringObject to = new StringObject();
        if (commentKind.equals(ConstantParameters.comment_kwd)) {
            // looking for thes1_comment
            dbtr.getThesaurusCategory_comment(selectedThesaurus, Q, sis_session.getValue(), to);
        } else if (commentKind.equals(ConstantParameters.scope_note_kwd)) {
            // looking for thes1_scope_note
            dbtr.getThesaurusCategory_scope_note(selectedThesaurus, Q, sis_session.getValue(), to);
        } else if (commentKind.equals(ConstantParameters.translations_scope_note_kwd)) {
            // looking for thes1_translations_scope_note
            dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), to);
        } else if (commentKind.equals(ConstantParameters.historical_note_kwd)) {
            // looking for thes1_historical_note
            dbtr.getThesaurusCategory_historical_note(selectedThesaurus, Q, sis_session.getValue(), to);
        } else if (commentKind.equals(ConstantParameters.note_kwd)) {
            // looking for thes1_note
            dbtr.getThesaurusCategory_note(selectedThesaurus, Q, sis_session.getValue(), to);
        }

        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject targetTerm_el = new StringObject(prefix_el.concat(targetTerm));

        // in case of ObsoleteDescriptor, return because TMS-API function GetDescriptorComment() returns error message
        // looking for THES1ObsoleteDescriptor 
        StringObject thesObsoleteDescriptor = new StringObject();
        dbtr.getThesaurusClass_ObsoleteDescriptor(selectedThesaurus, Q, sis_session.getValue(), thesObsoleteDescriptor);
        boolean isObsolete = NodeBelongsToClass(targetTerm_el, thesObsoleteDescriptor, false, Q, sis_session);
        if (isObsolete == true) {
            result.add("");
            return result;
        }

        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        StringObject comment = new StringObject("");
        //int ret = TA.GetDescriptorComment( new StringObject(targetTerm_el.getValue()), comment, from, to);
        Q.reset_name_scope();
        StringObject prevThes = new StringObject();
        TA.GetThesaurusNameWithoutPrefix(prevThes);
        if (prevThes.getValue().equals(selectedThesaurus) == false) {
            TA.SetThesaurusName(selectedThesaurus);
        }
        int ret = TA.GetDescriptorComment(new StringObject(targetTerm_el.getValue()), comment, from, to);

        // ATTENTION: begin query session AFTER TMS GetDescriptorComment() so as to overcome TMS GetDescriptorComment() BUG:
        // after calling it Q class looses the previously opened query session
        //int test = Q.begin_query(); //IMPLEMENTED THROUGH THEMASAPI WHERE THIS BUG IN FUNCTION EDITCOMMENT IS REMOVED
        StringObject errorMsg = new StringObject("");
        if (ret == TMSAPIClass.TMS_APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in getComment");
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(errorMsg);
            //errorMsg = /*"<tr><td>" +*/ errorMsg.concat(check_success(ret, null)) /*+ "</td></tr>"*/;
            result.add(errorMsg.getValue());
        }

        //reset to previous thesaurus name if needed
        if (prevThes.getValue().equals(selectedThesaurus) == false) {
            TA.SetThesaurusName(prevThes.getValue());
        }

        result.add(comment.getValue());
        return result;
    }

    /* Checks if the given link is named or unnamed.
     * Returns true if it is named else it returns false.
     */
 /*---------------------------------------------------------------------
     isNamedLink()
     -----------------------------------------------------------------------
     INPUT: - int linkSysid: the sysid of the link to be checked
     OUTPUT: - true if it is named, false otherwise
     FUNCTION: checkes if the given link is named or unnamed.
     ----------------------------------------------------------------------*/
 /*
     public boolean isNamedLink(long linkSysid) {
     //TMSAPIClass TA = new TMSAPIClass();
     if(TA.IS_UNNAMED(linkSysid)==QClass.APISucc){
     return false;
     }
     return true;
        
     int sysid_end = 0x01000000;
     int unNamed_bit = (sysid_end >> 1);
     if (((linkSysid) & (unNamed_bit)) != 0) {
     return false;
     }
     return true;
        
     }
     */

 /*---------------------------------------------------------------------
     equalStringArrays()
     -----------------------------------------------------------------------
     INPUT: two String arrays
     FUNCTION: checkes the given arrays contain the same values
     ----------------------------------------------------------------------*/
    public boolean equalStringArrays(String[] array1, String[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        if (length1 != length2) {
            return false;
        }
        for (int i = 0; i < length2; i++) {
            boolean found = false;
            for (int j = 0; j < length1; j++) {
                if (array2[i].compareTo(array1[j]) == 0) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        return true;
    }

    public boolean compareRTLinks(String rt, String[] links) {

        int i;

        for (i = 0; i < links.length; i++) {
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"link = " + links[i] + "rt = " + rt);
            if (rt.compareTo(links[i]) == 0) {
                return (true);
            }
        }
        return (false);
    }

    /*---------------------------------------------------------------------
     DescriptorCanBeMovedToHierarchy()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetDescriptor: the Descriptor to be checked
     - StringObject reasonOfFalse: a description of the reason in case of false
     OUTPUT: - false in case the given descriptor:
     > does not belong to DB
     > is TopTerm or ObsoleteDescriptor
     > is not a descriptor
     - true, otherwise
     FUNCTION: checks if the given descriptor can be moved to Hierarchy 
     ----------------------------------------------------------------------*/
    public boolean DescriptorCanBeMovedToHierarchy(String selectedThesaurus, StringObject targetDescriptor, StringObject reasonOfFalse, QClass Q, IntegerObject sis_session, final String uiLang) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        Utilities u = new Utilities();

        // check the case the given descriptor does not belong to DB
        if (check_exist(targetDescriptor.getValue(), Q, sis_session) == false) {
            //The given term does not exist in data base.
            reasonOfFalse.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/CurrentTermNotFound", null, uiLang));
            return false;
        }
        // check the case the given descriptor is TopTerm        
        // looking for THES1TopTerm 
        StringObject thesTopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), thesTopTerm);
        boolean isTopTerm = NodeBelongsToClass(targetDescriptor, thesTopTerm, false, Q, sis_session);
        if (isTopTerm == true) {
            //reasonOfFalse.setValue("The given term is a Top Term");
            reasonOfFalse.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/CurrentTermIsTopTerm", null, uiLang));
            return false;
        }
        // check the case the given descriptor is ObsoleteDescriptor
        // looking for THES1ObsoleteDescriptor 
        StringObject thesObsoleteDescriptor = new StringObject();
        dbtr.getThesaurusClass_ObsoleteDescriptor(selectedThesaurus, Q, sis_session.getValue(), thesObsoleteDescriptor);
        boolean isObsolete = NodeBelongsToClass(targetDescriptor, thesObsoleteDescriptor, false, Q, sis_session);
        if (isObsolete == true) {
            //reasonOfFalse.setValue("The given term is Obsolete");
            reasonOfFalse.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/CurrentTermIsObsolete", null, uiLang));
            return false;
        }
        // check the case the given descriptor is not a descriptor
        // looking for THES1Descriptor 
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thesDescriptor);

        boolean isDescriptor = NodeBelongsToClass(targetDescriptor, thesDescriptor, false, Q, sis_session);
        if (isDescriptor == false) {
            //reasonOfFalse.setValue("The given term is not a Descriptor");
            reasonOfFalse.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/CurrentTermNotDescriptor", null, uiLang));
            return false;
        }

        return true;
    }

    /*---------------------------------------------------------------------
     getDescriptorHierarchies()
     -----------------------------------------------------------------------
     INPUT: - String targetDescriptor: the Descriptor to be queried
     OUTPUT: - Vector hierarchiesVector: a Vector with all the hierarchies of the given Descriptor
     or
     - Vector error: filled with an error message (!!??!!) in case of
     the given Descriptor does not belong to a Hierarchy and is not a TopTerm
     FUNCTION: collects in a Vector all the hierarchies that the given Descriptor is instance of
     ----------------------------------------------------------------------*/
    public ArrayList<String> getDescriptorHierarchies(String selectedThesaurus, StringObject targetDescriptor, QClass Q, IntegerObject sis_session, final String uiLang) {
        int SISapiSession = sis_session.getValue();
        Utilities u = new Utilities();

        // looking for Descriptor THES1Hierarchy
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), thesHierarchy);

        // get all instances of THES1Hierarchy (set_h)
        Q.reset_name_scope();
        Q.set_current_node(thesHierarchy);
        int set_h = Q.get_all_instances(0);
        Q.reset_set(set_h);

        // get all classes of targetDescriptor (set_c)
        Q.reset_name_scope();
        Q.set_current_node(targetDescriptor);
        int set_c = Q.get_classes(0);
        Q.reset_set(set_c);

        // set_intersect(set_h, set_c)        
        ArrayList<String> hierarchiesVector = new ArrayList<String>();
        String errorMsg = new String("");
        ArrayList<String> error = new ArrayList<String>();

        // in case targetDescriptor belongs to a Hierarchy
        if (Q.set_disjoint(set_h, set_c) == QClass.FALSEval) {
            // set_disjoint returns true in case the 2 sets have empty intersection

            if (Q.set_intersect(set_h, set_c) != QClass.APIFail) {
                Q.reset_set(set_h);
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if (Q.bulk_return_nodes(set_h, retVals) != QClass.APIFail) {
                    for (Return_Nodes_Row row : retVals) {
                        hierarchiesVector.add(row.get_v1_cls_logicalname());
                    }
                }
                //StringObject lname = new StringObject();
                /*while ((Q.retur_nodes( set_h, lname)) != QClass.APIFail) {
                 hierarchiesVector.add(lname.getValue());
                 lname = new StringObject();
                 }*/
                Q.free_set(set_h);
                Q.free_set(set_c);
            } else {
                errorMsg = errorMsg.concat("<tr><td>" + u.translateFromMessagesXML("root/GeneralMessages/DBConnectionError", null, uiLang) + "</td></tr>");
                //errorMsg = errorMsg.concat("<tr><td>" + "A database connection error occurred. Please try again later." + "</td></tr>");
                error.add(errorMsg);
                return error;
            }
        } else { // mporei o oros na mhn anhkei se ierarxia apo la8os h mporei na einai top term		

            StringObject nodeObj = new StringObject(targetDescriptor.getValue());
            // looking for THES1TopTerm 
            StringObject TopTerm = new StringObject();
            dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTerm);
            boolean isTopTerm = NodeBelongsToClass(nodeObj, TopTerm, false, Q, sis_session);
            // in case targetDescriptor is a TopTerm, return the Hierarchy with the same name and prefix: "THES1Class`"
            if (isTopTerm == true) {
                // looking for prefix "THES1Class`"
                String classPrefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());
                hierarchiesVector.add(classPrefix.concat(removePrefix(targetDescriptor.getValue())));
                return hierarchiesVector;
            }
            errorMsg = errorMsg.concat("<tr><td>" + u.translateFromMessagesXML("root/GeneralMessages/TermDoesNotBelongInAnyHierarchy", new String[]{removePrefix(targetDescriptor.getValue())}, uiLang) + "</td></tr>");
            //errorMsg = errorMsg.concat("<tr><td>Term: '" + removePrefix(targetDescriptor.getValue()) + "' is not classified under any hierarchy.</td></tr>");
            error.add(errorMsg);
            return error;
        }

        return hierarchiesVector;
    }

    /*---------------------------------------------------------------------
     getClassesOfTerm()
     -----------------------------------------------------------------------
     INPUT: - String concept: the target term
     OUTPUT: a Vector with the classes of the given term
     ----------------------------------------------------------------------*/
    public ArrayList<String> getClassesOfTerm(String selectedThesaurus, String term, QClass Q, IntegerObject sis_session) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());

        StringObject concept_el = new StringObject(prefix.concat(term));
        int SISsession = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(concept_el);
        int set_c = Q.get_classes(0);

        ArrayList<String> classes = new ArrayList<String>();
        Q.reset_set(set_c);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_c, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //Cname.setValue(removePrefix(Cname.getValue()));
                classes.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        //StringObject Cname = new StringObject();
        /*while ((Q.retur_nodes( set_c, Cname)) != QClass.APIFail) {
         Cname.setValue(removePrefix(Cname.getValue()));
         classes.add(Cname.getValue());
         }*/
        Q.free_set(set_c);
        return classes;
    }

    /*---------------------------------------------------------------------
     GetTermsOfHierarchy()
     -----------------------------------------------------------------------
     INPUT: - String targetHierarchy: the target Hierarchy (in UI encoding and without prefix)
     OUTPUT: a Vector with the terms of the given Hierarchy (in UI encoding and without prefix) - sorted
     ----------------------------------------------------------------------*/
    public ArrayList<String> GetTermsOfHierarchy(String selectedThesaurus, String targetHierarchy, QClass Q, IntegerObject sis_session, Locale targetLocale) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        StringObject targetHierarchy_el = new StringObject(prefix.concat(targetHierarchy));
        int SISsession = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(targetHierarchy_el);
        int set_c = Q.get_instances(0);

        ArrayList<String> terms = new ArrayList<String>();
        Q.reset_set(set_c);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_c, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //Cname.setValue(removePrefix(Cname.getValue()));
                terms.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        //StringObject Cname = new StringObject();
        /*while ((Q.retur_nodes( set_c, Cname)) != QClass.APIFail) {
         Cname.setValue(removePrefix(Cname.getValue()));
         terms.add(Cname.getValue());
         }*/
        Q.free_set(set_c);

        //Locale targetLocale = new Locale(LocaleLanguage, LocaleCountry);
        Collections.sort(terms, new StringLocaleComparator(targetLocale));
        return terms;
    }

    /*---------------------------------------------------------------------
     check_exist()
     -----------------------------------------------------------------------
     INPUT: - String term: the term to be checked
     OUTPUT: - true, in case the given term exists in DB, false otherwise
     FUNCTION: checks if the term given as parameter exist in the database
     ----------------------------------------------------------------------*/
    public boolean check_exist(String term, QClass Q, IntegerObject sis_session) {
        if (term == null) {
            return false;
        }
        int SISapiSession = sis_session.getValue();
        StringObject str = new StringObject(term);
        Q.reset_name_scope();
        if (Q.set_current_node(str) != QClass.APIFail) {
            return true;
        }
        return false;
    }

    /*---------------------------------------------------------------------
     check_exist()
     -----------------------------------------------------------------------
     INPUT: - String term: the term to be checked
     OUTPUT: - true, in case the given term exists in DB, false otherwise
     FUNCTION: checks if the term given as parameter exist in the database
     ----------------------------------------------------------------------*/
    public boolean checkCMV_exist(CMValue termCmv, QClass Q, IntegerObject sis_session) {
        if (termCmv == null) {
            return false;
        }
        int SISapiSession = sis_session.getValue();
        StringObject str = new StringObject(termCmv.getString());
        Q.reset_name_scope();
        if (Q.set_current_node(str) != QClass.APIFail) {
            return true;
        }
        return false;
    }

    /* If the er_buf string is null, the function returns the error message that applies to the code ret.
     * Else it returns the string er_buf.
     * Besides returning a string, the functions also aborts a transaction.
     */
 /*---------------------------------------------------------------------
     check_success()
     -----------------------------------------------------------------------
     INPUT: - int ret: TMSAPIClass.TMS_APISucc or TMSAPIClass.TMS_APIFail 
     - String er_buf: an error message or null
     OUTPUT: - in case of 
     ret == TMS_APIFail, the given error message (if not null) or the TMS error message
     null, otherwise
     ----------------------------------------------------------------------*/
    public String check_success(int ret, TMSAPIClass TA, String er_buf, IntegerObject tms_session) {
        String result = new String();
        if (ret == TMSAPIClass.TMS_APIFail) {
            StringObject buf = new StringObject();
            if (er_buf == null) {

                TA.ALMOST_DONE_GetTMS_APIErrorMessage(buf);
            } else {
                buf.setValue(er_buf);
            }
            result = buf.getValue();
            //return removePrefix(result); 
            return result;  // karam

        }
        return null;
    }

    public ArrayList<String> getMatchedByName(String selectedThesaurus, String value, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        int tmp_set, ptrn_set;

        StringObject name = new StringObject();

        //CMValue prm = new CMValue();
        //prm.assign_string(prefix.concat(value));
        Q.set_current_node(name);//BUG???
        tmp_set = getDescriptorsSet(selectedThesaurus, Q, sis_session);
        //ptrn_set = Q.set_get_new();
        //Q.set_put_prm(ptrn_set, prm);
        //int ans_set = Q.get_matched(tmp_set, ptrn_set);
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        int ans_set = -1;// Q.get_matched_ToneAndCaseInsensitive(tmp_set, prefix.concat(value), Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
        if (Parameters.SEARCH_MODE_CASE_INSENSITIVE) {
            ans_set = Q.get_matched_CaseInsensitive(tmp_set, prefix.concat(value), true);
        } else {
            ans_set = Q.get_matched_ToneAndCaseInsensitive(tmp_set, prefix.concat(value), false);
        }

        Q.reset_set(ans_set);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(ans_set, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(ans_set, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/

        Q.free_set(tmp_set);
        //Q.free_set(ptrn_set);
        Q.free_set(ans_set);
        return v;
    }

    public ArrayList<String> getNotEqualsByName(String selectedThesaurus, String value, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        int conceptsSet, s;
        //StringObject name = new StringObject();
        StringObject term = new StringObject(value);

        conceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);

        Q.set_current_node(term);
        s = Q.get_all_instances(0);
        Q.reset_set(s);
        Q.set_difference(conceptsSet, s);
        Q.reset_set(conceptsSet);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(conceptsSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(conceptsSet, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/
        Q.free_set(conceptsSet);
        Q.free_set(s);
        //Q.free_all_sets();

        return v;
    }

    public ArrayList<String> getNotEqualsByBT(String selectedThesaurus, String value, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        int allConceptsSet, setBT;

        //StringObject name = new StringObject();
        StringObject from = new StringObject(selectedThesaurus.concat("Descriptor"));
        StringObject link = new StringObject(selectedThesaurus.concat("_BT"));
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject concept_el = new StringObject(prefix_el.concat(value));

        allConceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);

        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        setBT = Q.get_to_node_by_category(0, from, link);
        Q.reset_set(setBT);

        Q.set_difference(allConceptsSet, setBT);
        Q.reset_set(allConceptsSet);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(allConceptsSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(allConceptsSet, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/
        Q.free_set(allConceptsSet);
        Q.free_set(setBT);
        //Q.free_all_sets();

        return v;
    }

    public ArrayList<String> getNotEqualsByNT(String selectedThesaurus, String value, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        int allConceptsSet, setNT;
        //StringObject name = new StringObject();

        StringObject from = new StringObject(selectedThesaurus.concat("Descriptor"));
        StringObject link = new StringObject(selectedThesaurus.concat("_NT"));
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject concept_el = new StringObject(prefix_el.concat(value));

        allConceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);

        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        setNT = Q.get_to_node_by_category(0, from, link);
        Q.reset_set(setNT);

        Q.set_difference(allConceptsSet, setNT);
        Q.reset_set(allConceptsSet);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(allConceptsSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(allConceptsSet, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/
        Q.free_set(allConceptsSet);
        Q.free_set(setNT);
        //Q.free_all_sets();

        return v;
    }

    public ArrayList<String> getNotEqualsByRT(String selectedThesaurus, String value, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        int allConceptsSet;
        allConceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);
        //StringObject name = new StringObject();

        /*TODO
         * allConceptsSet	 <- olous tous orous
         * set_with_rt
         set1 = getLinkValue(sis_session,concept,rt_link);
         set2 = getFromRTLinks(sis_session,concept);
         set1 = set_union  --->  set_with_rt
         * set2 			 <- set_difference(allConceptsSet, set_with_rt)
         * set3 			 <- oloi oi oroi pou exoun rt to set2
         * */
        //allTerms
        allConceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);

        //rtTerms - getLinkValue
        int set1, set2;
        StringObject link;
        StringObject from = new StringObject(selectedThesaurus.concat("HierarchyTerm"));
        //TODO: repair inconsistency in database prefix.. eg. THES1_UF, thes1_dewey 
        link = new StringObject(selectedThesaurus.concat("_RT"));

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        StringObject concept_el = new StringObject(prefix_el.concat(value));
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        set1 = Q.get_from_node_by_category(0, from, link);
        Q.reset_set(set1);

        //rtTerms - getFromRTLinks
        Q.reset_name_scope();
        sysidL = Q.set_current_node(concept_el);
        set2 = Q.get_from_node_by_category(0, from, link);
        Q.reset_set(set2);

        //set_union
        Q.reset_name_scope();
        sysidL = Q.set_current_node(concept_el);
        Q.set_union(set1, set2);
        Q.reset_set(set1);

        //set difference
        Q.reset_name_scope();
        sysidL = Q.set_current_node(concept_el);
        Q.set_difference(allConceptsSet, set1);
        Q.reset_set(set1);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(allConceptsSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(allConceptsSet, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/
        Q.free_set(set1);
        Q.free_set(set2);
        //Q.free_all_sets();

        return v;
    }

    public ArrayList<String> getNotEquals(String selectedThesaurus, String value, String l, QClass Q, IntegerObject sis_session) {
        ArrayList<String> v = new ArrayList<String>();
        int allConceptsSet, set;

        StringObject from = new StringObject(selectedThesaurus.concat("HierarchyTerm"));
        StringObject link;
        //StringObject name = new StringObject();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        String prefix_en = "EN`";
        String prefix_src = "Source`";
        StringObject concept_el;

        if (/*l.equals(uk_alt_kwd) || */l.equals(ConstantParameters.uf_kwd)) {
            link = new StringObject(selectedThesaurus.concat("_" + l));
        } else if (l.equals(ConstantParameters.primary_found_in_kwd)) {
            link = new StringObject(selectedThesaurus.toLowerCase().concat("_" + ConstantParameters.primary_found_in_kwd));
        } else if (l.equals(ConstantParameters.translations_found_in_kwd)) {
            link = new StringObject(selectedThesaurus.toLowerCase().concat("_" + ConstantParameters.translations_found_in_kwd));
            /*} else if (l.equals(bt_found_in_kwd)) {
             link = new StringObject(SessionUserInfo.selectedThesaurus.toLowerCase().concat("_bt_found_in"));*/
        } else {
            link = new StringObject(selectedThesaurus.concat("_" + l.toUpperCase()));
        }
        if (/*l.equals(uk_alt_kwd) ||*/l.equals(ConstantParameters.uf_kwd)) {
            concept_el = new StringObject(prefix_en.concat(value));
        } else if (l.equals(ConstantParameters.primary_found_in_kwd) || l.equals(ConstantParameters.translations_found_in_kwd) /*|| (l.equals(bt_found_in_kwd))*/) {
            concept_el = new StringObject(prefix_src.concat(value));
        } else {
            concept_el = new StringObject(prefix_el.concat(value));
            //ok
        }
        allConceptsSet = getDescriptorsSet(selectedThesaurus, Q, sis_session);

        Q.reset_name_scope();
        long sysidL = Q.set_current_node(concept_el);
        set = Q.get_from_node_by_category(0, from, link);
        Q.reset_set(set);

        Q.set_difference(allConceptsSet, set);
        Q.reset_set(allConceptsSet);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(allConceptsSet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //name.setValue(removePrefix(name.getValue()));
                v.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        /*while ((Q.retur_nodes(allConceptsSet, name)) != QClass.APIFail) {
         name.setValue(removePrefix(name.getValue()));
         v.add(name.getValue());
         }*/

        Q.free_set(allConceptsSet);
        Q.free_set(set);
        //Q.free_all_sets();

        return v;
    }

    /*
     public ArrayList<String> getMatchedByBT(String selectedThesaurus, String value,QClass Q, IntegerObject sis_session) {
     ArrayList<String> v = new ArrayList<String>();
     DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
     String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
     int conceptsSet, matchSet, ptrn_set, set;
     CMValue prm = new CMValue();
     StringObject name = new StringObject();
        
     StringObject from = new StringObject(selectedThesaurus.concat("Descriptor"));
     StringObject link = new StringObject(selectedThesaurus.concat("_BT"));
     StringObject Desc = new StringObject();
     Desc.setValue(selectedThesaurus + "Descriptor");
         
     //get all concepts
     conceptsSet = getDescriptorsSet(selectedThesaurus,Q,sis_session);
     //match
     prm.assign_string(prefix.concat(value));
     ptrn_set = Q.set_get_new();
     Q.set_put_prm(ptrn_set, prm);
     matchSet = Q.get_matched_string( conceptsSet, prm, QClass.MatchStringTypes.STRING_MATCHED);
     Q.reset_set(matchSet);

     //getNTs of matchSet
     Q.reset_name_scope();
     long sysidL = Q.set_current_node(Desc);
     set = Q.CHECK_get_to_node_by_category( matchSet, from, link);
     Q.reset_set(set);

     while ((Q.retur_nodes(set, name)) != QClass.APIFail) {
     name.setValue(removePrefix(name.getValue()));
     v.add(name.getValue());
     }
     Q.free_set(conceptsSet);
     Q.free_set(matchSet);
     Q.free_set(ptrn_set);
     Q.free_set(set);
     //Q.free_all_sets();
     return v;
     }
     */
 /*
     public ArrayList<String> getMatchedByNT(String selectedThesaurus, String value,QClass Q, IntegerObject sis_session) {
     ArrayList<String> v = new ArrayList<String>();
     DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
     String prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
     int conceptsSet, matchSet, ptrn_set, set;
     CMValue prm = new CMValue();
     StringObject name = new StringObject();
        
     StringObject from = new StringObject(selectedThesaurus.concat("Descriptor"));
     StringObject link = new StringObject(selectedThesaurus.concat("_BT"));
     StringObject Desc = new StringObject();
     Desc.setValue(selectedThesaurus + "Descriptor");
     //get all concepts
     conceptsSet = getDescriptorsSet(selectedThesaurus,Q,sis_session);
     //match
     prm.assign_string(prefix.concat(value));
     ptrn_set = Q.set_get_new();
     Q.set_put_prm(ptrn_set, prm);
     matchSet = Q.get_matched_string( conceptsSet, prm, QClass.MatchStringTypes.STRING_MATCHED);
     Q.reset_set(matchSet);

     //getBTs of matchSet
     Q.reset_name_scope();
     long sysidL = Q.set_current_node(Desc);
     set = Q.CHECK_get_from_node_by_category( matchSet, from, link);
     Q.reset_set(set);

     Q.reset_name_scope();
     //int sysid = Q.set_current_node(concept_el);
     //set = Q.get_from_node_by_category(0,from,link);
     //Q.reset_set(set);

     while ((Q.retur_nodes(set, name)) != QClass.APIFail) {
     name.setValue(removePrefix(name.getValue()));
     v.add(name.getValue());
     }

     Q.free_set(conceptsSet);
     Q.free_set(matchSet);
     Q.free_set(ptrn_set);
     Q.free_set(set);
     //Q.free_all_sets();
     return v;
     }
     */
 /*
     //TODO: 8elei kapoia beltiwsh...
     public ArrayList<String> getMatchedByRT(UserInfoClass SessionUserInfo, String value,QClass Q, IntegerObject sis_session) {
     ArrayList<String> v = new ArrayList<String>();
     ArrayList<String> k = new ArrayList<String>();

     DBThesaurusReferences dbtr = new DBThesaurusReferences();
     String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue());
     int conceptsSet, matchSet, ptrn_set, set;
     CMValue prm = new CMValue();
     StringObject name = new StringObject();
        
     StringObject from = new StringObject(SessionUserInfo.selectedThesaurus.concat("Descriptor"));
     StringObject link = new StringObject(SessionUserInfo.selectedThesaurus.concat("_RT"));
     StringObject Desc = new StringObject();
     Desc.setValue(SessionUserInfo.selectedThesaurus + "Descriptor");
        
        
     //get all concepts
     conceptsSet = getDescriptorsSet(SessionUserInfo.selectedThesaurus,Q,sis_session);
     //match
     prm.assign_string(prefix.concat(value));
     ptrn_set = Q.set_get_new();
     Q.set_put_prm(ptrn_set, prm);
     matchSet = Q.get_matched_string( conceptsSet, prm, QClass.MatchStringTypes.STRING_MATCHED);
     Q.reset_set(matchSet);

     //getNTs of matchSet
     Q.reset_name_scope();
     long sysidL = Q.set_current_node(Desc);
     set = Q.CHECK_get_from_node_by_category( matchSet, from, link);
     Q.reset_set(set);

     //Q.reset_name_scope();
     //int sysid = Q.set_current_node(concept_el);
     //set = Q.get_from_node_by_category(0,from,link);
     //Q.reset_set(set);

     while ((Q.retur_nodes(matchSet, name)) != QClass.APIFail) {
     name.setValue(removePrefix(name.getValue()));
     v.add(name.getValue());
     }

     Q.free_set(conceptsSet);
     Q.free_set(matchSet);
     Q.free_set(ptrn_set);
     Q.free_set(set);
     //Q.free_all_sets();

     for (int i = 0; i < v.size(); i++) {
     k.addAll(getRTlinksBothDirections(SessionUserInfo,v.get(i).toString(),Q,sis_session));
     }
     return k;
     }
     */
 /*
     public ArrayList<String> getMatched(String selectedThesaurus,String value, String l,QClass Q, IntegerObject sis_session) {

     ArrayList<String> v = new ArrayList<String>();
     int conceptsSet, matchSet, ptrn_set, set;
     CMValue prm = new CMValue();
     StringObject name = new StringObject();
        
     StringObject from = new StringObject(selectedThesaurus.concat("HierarchyTerm"));
     StringObject Desc = new StringObject();
     Desc.setValue(selectedThesaurus + "Descriptor");

     StringObject link;
     if (l.equals(ConstantParameters.uf_kwd)) {//l.equals(uk_alt_kwd) ||
     link = new StringObject(selectedThesaurus.concat("_" + l));
     Desc.setValue(selectedThesaurus + "EnglishWord");
     }//+++++
     else if (l.equals(ConstantParameters.translations_found_in_kwd) || l.equals(ConstantParameters.primary_found_in_kwd)) {
     link = new StringObject(selectedThesaurus.toLowerCase().concat("_" + l));
     } else {
     link = new StringObject(selectedThesaurus.concat("_" + l.toUpperCase()));
     //get all concepts
     }
        
     conceptsSet = getDescriptorsSet(selectedThesaurus,Q,sis_session);
     //match
     //prm.assign_string(prefix.concat(value));
     prm.assign_string(value);
     ptrn_set = Q.set_get_new();
     Q.set_put_prm(ptrn_set, prm);
     matchSet = Q.get_matched_string( conceptsSet, prm, QClass.MatchStringTypes.STRING_MATCHED);
     Q.reset_set(matchSet);

     Q.reset_name_scope();
     long sysidL = Q.set_current_node(Desc);
     set = Q.CHECK_get_from_node_by_category( matchSet, from, link);
     Q.reset_set(set);

     //Q.reset_name_scope();
     //int sysid = Q.set_current_node(concept_el);
     //set = Q.get_from_node_by_category(0,from,link);
     //Q.reset_set(set);

     while ((Q.retur_nodes(set, name)) != QClass.APIFail) {
     name.setValue(removePrefix(name.getValue()));
     v.add(name.getValue());
     }

     Q.free_set(conceptsSet);
     Q.free_set(matchSet);
     Q.free_set(ptrn_set);
     Q.free_set(set);
     //Q.free_all_sets();

     return v;
     }
     */
    public ArrayList<String> getThesaurusTranslationPrefixes(String selectedThesaurus, QClass Q, IntegerObject sis_session) {
        ArrayList<String> returnVec = new ArrayList<String>();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        int SISsession = sis_session.getValue();

        StringObject thesaurusTermStrObj = new StringObject();
        StringObject thesaurusTranslationStrObj = new StringObject();
        StringObject IndividualStrObj = new StringObject(ConstantParameters.individualClass);
        StringObject HasPrefixStrObj = new StringObject(ConstantParameters.hasPrefix);

        dbtr.getThesaurusClass_Term(selectedThesaurus, Q, SISsession, thesaurusTermStrObj);
        dbtr.getThesaurusCategory_translation(selectedThesaurus, Q, SISsession, thesaurusTranslationStrObj);

        Q.reset_name_scope();
        Q.set_current_node(thesaurusTermStrObj);
        Q.set_current_node(thesaurusTranslationStrObj);
        int set_translation_categories = Q.get_subclasses(0);
        Q.reset_set(set_translation_categories);

        int set_translationWords = Q.get_to_value(set_translation_categories);
        Q.reset_set(set_translationWords);

        int set_has_prefix_links = Q.get_link_from_by_category(set_translationWords, IndividualStrObj, HasPrefixStrObj);
        Q.reset_set(set_has_prefix_links);

        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_has_prefix_links, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String newPrefix = row.get_v3_cmv().getString();
                if (returnVec.contains(newPrefix) == false) {
                    returnVec.add(newPrefix);
                }
            }
        }
        /*while(Q.retur_link( set_has_prefix_links, cls, label, cmv)!=QClass.APIFail){
         String newPrefix = cmv.getString();
         if(returnVec.contains(newPrefix)==false){
         returnVec.add(newPrefix);
         }
         }*/
        Q.free_set(set_has_prefix_links);
        Q.free_set(set_translationWords);
        Q.free_set(set_translation_categories);

        return returnVec;
    }

    public int getSearchTermResultSet(UserInfoClass SessionUserInfo, String[] input, String[] operators, String[] values, String globalOperator, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        //globalOperator == *       -->View All Icon with default output pressed from left menu
        //globalOperator == all     -->Proboli olwn pressed from criteria but custo9mized output was selected
        //globalOperator == AND |OR -->customized criteria and output

        int index = Parameters.CLASS_SET.indexOf("TERM");

        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"getSearchTermResultSet STARTS");
        //No need to include classes like THES1JustBecameObsoleteDescriptor / THES1NewDescriptor, THES1Descriptor_V1 etc
        int sisSessionId = sis_session.getValue();
        int set_global_descriptor_results = -1;

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        String[] prefixes = null;
        Q.reset_name_scope();
        int set_d = get_Instances_Set(DescriptorClasses, Q, sis_session);

        Q.reset_name_scope();
        if (globalOperator.equalsIgnoreCase("*") || globalOperator.equalsIgnoreCase("all")) {

            set_global_descriptor_results = Q.set_get_new();
            Q.reset_set(set_d);
            Q.set_copy(set_global_descriptor_results, set_d);
            Q.reset_set(set_global_descriptor_results);

        } else {

            for (int i = 0; i < input.length; i++) {

                Q.reset_set(set_d);
                int set_partial_descriptor_results = -1;// = Q.set_get_new();
                String searchVal = values[i];

                Q.reset_name_scope();

                // looking for THES1Descriptor
                StringObject from = new StringObject();
                // looking for THES1_BT
                StringObject link = new StringObject();
                getKeywordPair(SessionUserInfo.selectedThesaurus, input[i].toString(), from, link, Q, sis_session);

                if (input[i].toString().equalsIgnoreCase("name")) {

                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByName(prefixes, set_d,
                            operators[i].toString(), searchVal, Q, sis_session, SessionUserInfo.selectedThesaurus);
                    Q.reset_set(set_partial_descriptor_results);
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.translation_kwd)) {

                    set_partial_descriptor_results = filterTransLationLinks(SessionUserInfo, true, sisSessionId,
                            operators[i].toString(), searchVal, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.bt_kwd)) {
                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes,
                            operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.nt_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes,
                            operators[i].toString(), searchVal, ConstantParameters.TO_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.rt_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes,
                            operators[i].toString(), searchVal, ConstantParameters.BOTH_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.uf_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes,
                            operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.uf_translations_kwd)) {
                    /*
                     prefix = dbtr.getThesaurusPrefix_EnglishWord(SessionUserInfo,Q,sis_session.getValue());
                     prefixes = new String[1];
                     prefixes[0] = prefix;
                     set_partial_descriptor_results =  filterLinksByRelation(SessionUserInfo, sisSessionId,  from, link, prefixes,  operators[i].toString(), searchVal, FROM_Direction,Q,sis_session);
                     * */
                    set_partial_descriptor_results = filterTransLationLinks(SessionUserInfo, false, sisSessionId, operators[i].toString(), searchVal, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.dn_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_DeweyNumber(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.tc_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_TaxonomicCode(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.alt_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                    /*} else if (input[i].toString().equalsIgnoreCase(uk_alt_kwd)) {
                    
                     prefix = dbtr.getThesaurusPrefix_EnglishWord(sessionInstance,Q,sis_session.getValue());
                     prefixes = new String[1];
                     prefixes[0] = prefix;
                     set_partial_descriptor_results =  filterLinksByRelation(sessionInstance, sisSessionId,  from, link, prefixes,  operators[i].toString(), searchVal, FROM_Direction,Q,sis_session);
                     Q.reset_set( set_partial_descriptor_results);

                     } else if (input[i].toString().equalsIgnoreCase(bt_found_in_kwd)) {
                    
                     prefix = dbtr.getThesaurusPrefix_Source(Q,sis_session.getValue());
                     prefixes = new String[1];
                     prefixes[0] = prefix;
                     set_partial_descriptor_results =  filterLinksByRelation(sessionInstance, sisSessionId,  from, link, prefixes,  operators[i].toString(), searchVal, FROM_Direction,Q,sis_session);
                     Q.reset_set( set_partial_descriptor_results);
                     */
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.primary_found_in_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.translations_found_in_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.facet_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    set_partial_descriptor_results = filterLinksByParentFacet(SessionUserInfo, sisSessionId, prefix, operators[i].toString(), searchVal, Q, sis_session);
                    //PENDING
                    //set_partial_descriptor_results =  filterLinksByRelation(  from, link, prefix,  operators[i].toString(), searchVal, FROM_Direction);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.topterm_kwd)) {

                    //PENDING
                    // looking for EKTTopTerm                     
                    prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    set_partial_descriptor_results = filterLinksByTopTerm(SessionUserInfo.selectedThesaurus, sisSessionId, prefix, operators[i].toString(), searchVal, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.scope_note_kwd)) {
                    set_partial_descriptor_results = filterLinksByComment(SessionUserInfo, Q, TA, sis_session, ConstantParameters.scope_note_kwd, operators[i].toString(), searchVal);
                    Q.reset_set(set_partial_descriptor_results);
                    //Utils.StaticClass.webAppSystemOutPrintln(Q.set_get_card(set_partial_descriptor_results));
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.translations_scope_note_kwd)) {
                    set_partial_descriptor_results = filterLinksByComment(SessionUserInfo, Q, TA, sis_session, ConstantParameters.translations_scope_note_kwd, operators[i].toString(), searchVal);
                    Q.reset_set(set_partial_descriptor_results);
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.historical_note_kwd)) {
                    set_partial_descriptor_results = filterLinksByComment(SessionUserInfo, Q, TA, sis_session, ConstantParameters.historical_note_kwd, operators[i].toString(), searchVal);
                    Q.reset_set(set_partial_descriptor_results);
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.comment_kwd)) {
                    set_partial_descriptor_results = filterLinksByComment(SessionUserInfo, Q, TA, sis_session, ConstantParameters.comment_kwd, operators[i].toString(), searchVal);
                    Q.reset_set(set_partial_descriptor_results);
                }
                else if (input[i].toString().equalsIgnoreCase(ConstantParameters.note_kwd)) {
                    set_partial_descriptor_results = filterLinksByComment(SessionUserInfo, Q, TA, sis_session, ConstantParameters.note_kwd, operators[i].toString(), searchVal);
                    Q.reset_set(set_partial_descriptor_results);
                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.created_by_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.modified_by_kwd)) {

                    prefix = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.created_on_kwd)) {

                    //PENDING THES1HierarchyTerm - thes1_created
                    prefix = "";
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.modified_on_kwd)) {

                    //PENDING THES1HierarchyTerm - thes1_modified
                    prefix = "";
                    prefixes = new String[1];
                    prefixes[0] = prefix;
                    set_partial_descriptor_results = filterLinksByRelation(SessionUserInfo, sisSessionId, from, link, prefixes, operators[i].toString(), searchVal, ConstantParameters.FROM_Direction, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                } else if (input[i].toString().equalsIgnoreCase(ConstantParameters.status_kwd)) {

                    
                    set_partial_descriptor_results = filterLinksByStatus(SessionUserInfo, sisSessionId, operators[i].toString(), searchVal, Q, sis_session);
                    Q.reset_set(set_partial_descriptor_results);

                }

                //merge results of each loop. All first loop's results are included
                if (i == 0) {
                    Q.free_set(set_global_descriptor_results);
                    set_global_descriptor_results = Q.set_get_new();
                    Q.reset_set(set_partial_descriptor_results);
                    Q.set_copy(set_global_descriptor_results, set_partial_descriptor_results);
                    Q.reset_set(set_global_descriptor_results);

                    Q.free_set(set_partial_descriptor_results);

                    if (globalOperator.equalsIgnoreCase("AND") && Q.set_get_card(set_global_descriptor_results) == 0) {
                        break;
                    } else {
                        continue;
                    }
                }
                //If conjuction operator == AND then set_intersect
                if (globalOperator.equalsIgnoreCase("AND")) {

                    Q.reset_set(set_global_descriptor_results);
                    Q.reset_set(set_partial_descriptor_results);
                    Q.set_intersect(set_global_descriptor_results, set_partial_descriptor_results);
                    Q.reset_set(set_global_descriptor_results);

                    Q.free_set(set_partial_descriptor_results);

                    if (Q.set_get_card(set_global_descriptor_results) == 0) {
                        break;
                    } else {
                        continue;
                    }
                }
                //If conjuction operator == OR then set_union
                if (globalOperator.equalsIgnoreCase("OR")) {

                    Q.reset_set(set_global_descriptor_results);
                    Q.reset_set(set_partial_descriptor_results);
                    Q.set_union(set_global_descriptor_results, set_partial_descriptor_results);
                    Q.reset_set(set_global_descriptor_results);

                    Q.free_set(set_partial_descriptor_results);
                    continue;
                }

            }
        }

        Q.reset_set(set_global_descriptor_results);
        Q.free_set(set_d);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"getSearchTermResultSet ENDS");

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set_global_descriptor_results = dbf.FilterTermsResults(SessionUserInfo, set_global_descriptor_results, Q, sis_session);
        Q.reset_set(set_global_descriptor_results);

        return set_global_descriptor_results;
    }

    public int filterLinksByTopTerm(String selectedThesaurus, int sisSessionId, String prefix, String operator, String searchVal, QClass Q, IntegerObject sis_session) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject TopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTerm);
        String topTermClass = TopTerm.getValue();

        int set_results = Q.set_get_new();
        Q.reset_set(set_results);

        Q.reset_name_scope();
        Q.set_current_node(new StringObject(topTermClass));

        int set_tt_nodes = Q.get_instances(0);
        Q.reset_set(set_tt_nodes);

        int cardtt = Q.set_get_card(set_tt_nodes);
        //Filter  Names contained in set_tt_nodes according to searchVal
        String[] prefixes = new String[1];
        prefixes[0] = prefix;
        int set_tt_criteria_names = filterLinksByName(prefixes, set_tt_nodes, operator, searchVal, Q, sis_session, selectedThesaurus);
        Q.reset_set(set_tt_criteria_names);

        int cardttcr = Q.set_get_card(set_tt_criteria_names);
        collect_Recurcively_ALL_NTs_Of_Set(selectedThesaurus, set_tt_criteria_names, set_results, true, -1, Q, sis_session);
        Q.reset_set(set_results);

        int cardr = Q.set_get_card(set_results);
        //Get All terms for display
        /* No need for this
         int set_terms = this.get_Instances_Set(DescriptorClasses);
         Q.reset_set( set_terms);
         
         //ensure that no non display terms will appear in the result set
         Q.set_intersect( set_results, set_terms);
         Q.reset_set( set_results);
         
         */
        return set_results;
    }

    public int filterLinksByParentFacet(UserInfoClass SessionUserInfo, int sisSessionId, String prefix, String operator, String searchVal, QClass Q, IntegerObject sis_session) {

        String[] prefixes = {prefix};
        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);

        // set_results = Q.set_get_new();
        int set_f_nodes = get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_f_nodes);

        //Filter Facet Names contained in set_f according to searchVal
        int set_f_criteria_names = filterLinksByName(prefixes, set_f_nodes, operator, searchVal, Q, sis_session, SessionUserInfo.selectedThesaurus);
        Q.reset_set(set_f_criteria_names);

        int set_results = Q.get_all_instances(set_f_criteria_names);
        Q.reset_set(set_results);

        //Get All terms for display
        int set_terms = get_Instances_Set(DescriptorClasses, Q, sis_session);
        Q.reset_set(set_terms);

        //ensure that no non display terms will appear in the result set
        Q.set_intersect(set_results, set_terms);
        Q.reset_set(set_results);

        return set_results;
    }

    /* getAllInstancesSet()
     * 
     * Reads global variable static String[] DescriptorClasses and returns a set  
     * that contains all instances of the classes declared in DescriptorClasses
     */
    public int get_Instances_Set(String[] targetClasses, QClass Q, IntegerObject sis_session) {

        int sisSessionId = sis_session.getValue();

        int set_d = Q.set_get_new();

        for (int i = 0; i < targetClasses.length; i++) {

            Q.reset_name_scope();

            StringObject partClass = new StringObject(targetClasses[i]);
            Q.set_current_node(partClass);
            int set_part = Q.get_instances(0);

            Q.reset_set(set_d);
            Q.reset_set(set_part);
            Q.set_union(set_d, set_part);

            Q.free_set(set_part);
        }
        Q.reset_set(set_d);

        Q.reset_name_scope();
        return set_d;
    }

    // constant values used by filterLinksByComment()
    /*---------------------------------------------------------------------
     filterLinksByComment()
     -----------------------------------------------------------------------
     INPUT: - commentKind = "scope_note" or "historical_note" or "comment"
     OUTPUT: a set of terms with comments:
     a. under the given comment category (commentKind)
     b. with the given search criteria (operator, searchVal)
     ----------------------------------------------------------------------*/
    public int filterLinksByComment(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, String commentKind, String operator, String searchVal) {
        int sisSessionId = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for AAAThesaurusConcept
        StringObject thesThesaurusConcept = new StringObject();
        dbtr.getThesaurusClass_ThesaurusConcept(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesThesaurusConcept);
        // looking for aaa_scope_note or aaa_historical_note or aaa_comment
        StringObject thesCommentKind = new StringObject();
        if (commentKind.compareTo(ConstantParameters.scope_note_kwd) == 0) {
            dbtr.getThesaurusCategory_scope_note(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesCommentKind);
        } else if (commentKind.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {
            dbtr.getThesaurusCategory_translations_scope_note(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesCommentKind);
        } else if (commentKind.compareTo(ConstantParameters.historical_note_kwd) == 0) {
            dbtr.getThesaurusCategory_historical_note(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesCommentKind);
        } else if (commentKind.compareTo(ConstantParameters.comment_kwd) == 0) {
            dbtr.getThesaurusCategory_comment(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesCommentKind);
        } else if (commentKind.compareTo(ConstantParameters.note_kwd) == 0) {
            dbtr.getThesaurusCategory_note(SessionUserInfo.selectedThesaurus, Q, sisSessionId, thesCommentKind);
        }

        // get the set with ALL terms
        int index = Parameters.CLASS_SET.indexOf("TERM");

        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);
        int set_terms = get_Instances_Set(DescriptorClasses, Q, sis_session);
        Q.reset_set(set_terms);

        // get the set with ALL terms with comment
        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusConcept);
        Q.set_current_node(thesCommentKind);
        int commentLinksSet = Q.get_all_instances(0);
        Q.reset_set(commentLinksSet);
        int set_terms_with_comment = Q.get_from_value(commentLinksSet);
        Q.free_set(commentLinksSet);
        Q.reset_set(set_terms_with_comment);

        // optimization of special search cases
        // 1. terms without comment
        if (operator.equals(ConstantParameters.searchOperatorEquals) && searchVal.equals("")) {
            Q.set_difference(set_terms, set_terms_with_comment);
            Q.free_set(set_terms_with_comment);
            return set_terms;
        }
        // 2. terms with comment
        if (operator.equals("!") && searchVal.equals("")) {
            Q.free_set(set_terms);
            return set_terms_with_comment;
        }
        // special handling for searching modes: ~ and !~
        int SearchMode = -1;
        if (operator.equals(ConstantParameters.searchOperatorContains) && searchVal.startsWith("*") == false && searchVal.endsWith("*") == false) { // contains
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_CONTAINS;
        } else if (operator.equals(ConstantParameters.searchOperatorContains) && searchVal.endsWith("*")) { // starts with
            // remove special character "*"
            searchVal = searchVal.substring(0, searchVal.length() - 1);
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_STARTS_WITH;
        } else if (operator.equals(ConstantParameters.searchOperatorContains) && searchVal.startsWith("*")) { // ends with
            // remove special character "*"
            searchVal = searchVal.substring(1);
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_ENDS_WITH;
        } else if (operator.equals(ConstantParameters.searchOperatorNotContains) && searchVal.startsWith("*") == false && searchVal.endsWith("*") == false) { // contains
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_CONTAINS;
        } else if (operator.equals(ConstantParameters.searchOperatorNotContains) && searchVal.endsWith("*")) { // not starts with
            // remove special character "*"
            searchVal = searchVal.substring(0, searchVal.length() - 1);
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_STARTS_WITH;
        } else if (operator.equals(ConstantParameters.searchOperatorNotContains) && searchVal.startsWith("*")) { // not ends with
            // remove special character "*"
            searchVal = searchVal.substring(1);
            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_ENDS_WITH;
        }

        // collect all terms with comment in a Vector
        //StringObject termName = new StringObject();
        ArrayList<String> termNamesVector = new ArrayList<String>();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_terms_with_comment, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                termNamesVector.add(row.get_v1_cls_logicalname());
            }
        }
        /*while ((Q.retur_nodes( set_terms_with_comment, termName)) != QClass.APIFail) {
         termNamesVector.add(termName.getValue());
         }*/
        //Utils.StaticClass.webAppSystemOutPrintln(Q.set_get_card(set_terms_with_comment));
        Q.free_set(set_terms_with_comment);

        int set_results = Q.set_get_new();
        // for each term
        int termNamesVectorSize = termNamesVector.size();
        ArrayList<String> termComment = new ArrayList<String>();
        for (int i = 0; i < termNamesVectorSize; i++) {
            String termNameStr = (String) (termNamesVector.get(i));
            String targetTerm_UI = removePrefix(termNameStr);
            termComment = getTermComment(SessionUserInfo.selectedThesaurus, targetTerm_UI, commentKind, Q, TA, sis_session);
            String termCommentStr = (String) (termComment.get(0));
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+i + " . Comment of: " + targetTerm_UI + " = " + termCommentStr);

            // check the current comment depending on the search operator
            boolean termBelongsToResults = false;
            if (operator.equals(ConstantParameters.searchOperatorEquals)) { // equals with (g.e. The biotopes located in areas where agriculure is practiced)
                if (termCommentStr.equals(searchVal)) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_CONTAINS) { // contains (g.e. οικονομία)
                if (termCommentStr.indexOf(searchVal) != -1) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_STARTS_WITH) { // starts with (g.e. The*)
                if (termCommentStr.startsWith(searchVal)) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_ENDS_WITH) { // ends with (g.e. *περιβάλλον)
                if (termCommentStr.endsWith(searchVal)) {
                    termBelongsToResults = true;
                }
            } else if (operator.equals("!")) { // not equals with (g.e. Πνεύμονες πρασίνου μεσαίου μεγέθους)
                if (termCommentStr.equals(searchVal) == false) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_CONTAINS) { // not contains (g.e. οικονομία)
                if (termCommentStr.indexOf(searchVal) == -1) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_STARTS_WITH) { // not starts with (g.e. The*)
                if (termCommentStr.startsWith(searchVal) == false) {
                    termBelongsToResults = true;
                }
            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_ENDS_WITH) { // not ends with (g.e. *περιβάλλον)
                if (termCommentStr.endsWith(searchVal) == false) {
                    termBelongsToResults = true;
                }
            }

            // inform results in case the current comment satisfies the search criterion
            if (termBelongsToResults == true) {
                Q.reset_name_scope();
                Q.set_current_node(new StringObject(termNameStr));
                Q.set_put(set_results);
            }
        }
        //Utils.StaticClass.webAppSystemOutPrintln(Q.set_get_card(set_results));
        //Q.reset_name_scope();
        //int set_labels_to = Q.CHECK_get_link_to_by_category(set_results, thesThesaurusConcept,thesCommentKind);
        //Q.reset_set(set_labels_to);
        //Utils.StaticClass.webAppSystemOutPrintln(Q.set_get_card(set_labels_to));
        //int set_final_results = Q.get_from_value(set_labels_to);

        //Q.reset_set(set_final_results);
        //Utils.StaticClass.webAppSystemOutPrintln(Q.set_get_card(set_final_results));
        //Q.free_set( set_labels_to);
        //Q.free_set( set_results);
        //return set_final_results;
        return set_results;
    }

    public int filterLinksByRelation(UserInfoClass SessionUserInfo, int sisSessionId, StringObject from, StringObject link, String[] prefixes, String operator, String searchVal, int direction, QClass Q, IntegerObject sis_session) {

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        //int set_results = Q.set_get_new();
        int set_results = -1;

        if (direction == ConstantParameters.FROM_Direction || direction == ConstantParameters.BOTH_Direction) {
            int set_from_results = -1;
            //Get All terms for display
            int set_terms = get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);

            //Get The labes set of Their Bt relations originating from them
            int set_from_labels = Q.get_link_from_by_category(set_terms, from, link);
            Q.reset_set(set_from_labels);

            //Get To value of these labels in order to get the set of their bt names
            int set_from_names = Q.get_to_value(set_from_labels);
            Q.reset_set(set_from_names);

            //Filter BT Names contained in set_bt_names according to searchVal
            int set_from_criteria_names = filterLinksByName(prefixes, set_from_names, operator, searchVal, Q, sis_session, SessionUserInfo.selectedThesaurus);
            Q.reset_set(set_from_criteria_names);

            //get all BT Labels from BT Names (From NT --> LABEL --> TO BT)
            int set_from_criteria_labels = Q.get_link_to_by_category(set_from_criteria_names, from, link);
            Q.reset_set(set_from_criteria_labels);

            //get the from values of these labels (From --> LABEL --> TO BT)
            set_from_results = Q.get_from_value(set_from_criteria_labels);
            Q.reset_set(set_from_results);

            //ensure that no non display terms will appear in the result set
            Q.set_intersect(set_from_results, set_terms);
            Q.reset_set(set_from_results);

            Q.free_set(set_terms);
            Q.free_set(set_from_labels);
            Q.free_set(set_from_names);
            Q.free_set(set_from_criteria_names);
            Q.free_set(set_from_criteria_labels);

            if (direction == ConstantParameters.FROM_Direction) {
                return set_from_results;
            } else {
                Q.reset_set(set_from_results);
                set_results = Q.set_get_new();
                Q.set_copy(set_results, set_from_results);
                Q.free_set(set_from_results);
            }

        }
        if (direction == ConstantParameters.TO_Direction || direction == ConstantParameters.BOTH_Direction) {

            int set_to_results = -1;

            //HAVING NARROW TERM AS SEARCH VALUE
            //Get All terms for display
            int set_terms = get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);

            //Get The labes set of Their BT relations ending to them
            int set_to_labels = Q.get_link_to_by_category(set_terms, from, link);
            Q.reset_set(set_to_labels);

            //Get From value of these labels in order to get the set of their nt names
            int set_to_names = Q.get_from_value(set_to_labels);
            Q.reset_set(set_to_names);

            //Filter NT Names contained in set_nt_names according to searchVal
            int set_to_criteria_names = filterLinksByName(prefixes, set_to_names, operator, searchVal, Q, sis_session, SessionUserInfo.selectedThesaurus);
            Q.reset_set(set_to_criteria_names);

            //get all NT Labels originating from NT Names (From NT --> LABEL --> TO BT)
            int set_to_criteria_labels = Q.get_link_from_by_category(set_to_criteria_names, from, link);
            Q.reset_set(set_to_criteria_labels);

            //get the to values of these labels (From NT --> LABEL --> TO BT)
            set_to_results = Q.get_to_value(set_to_criteria_labels);
            Q.reset_set(set_to_results);

            //ensure that no non display terms will appear in the result set
            Q.set_intersect(set_to_results, set_terms);
            Q.reset_set(set_to_results);

            Q.free_set(set_terms);
            Q.free_set(set_to_labels);
            Q.free_set(set_to_names);
            Q.free_set(set_to_criteria_names);
            Q.free_set(set_to_criteria_labels);

            if (direction == ConstantParameters.TO_Direction) {
                return set_to_results;
            } else {
                Q.reset_set(set_to_results);
                set_results = Q.set_get_new();
                Q.set_union(set_results, set_to_results);
                Q.free_set(set_to_results);
            }
        }

        Q.reset_set(set_results);

        return set_results;
    }

    //similar to filterLinksByRelation but customized in order to support the multiple prefixes of translations
    public int filterTransLationLinks(UserInfoClass SessionUserInfo, boolean prefferredTranslations, int sisSessionId, String operator, String searchVal, QClass Q, IntegerObject sis_session) {

        ArrayList<String> prefixesVector = getThesaurusTranslationPrefixes(SessionUserInfo.selectedThesaurus, Q, sis_session);
        String[] prefixes = new String[prefixesVector.size()];
        prefixesVector.toArray(prefixes);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        //get translation links that are valid
        StringObject from = new StringObject();
        StringObject link = new StringObject();
        if (prefferredTranslations) {
            dbtr.getThesaurusClass_Term(SessionUserInfo.selectedThesaurus, Q, sisSessionId, from);
            dbtr.getThesaurusCategory_translation(SessionUserInfo.selectedThesaurus, Q, sisSessionId, link);
        } else {
            dbtr.getThesaurusClass_HierarchyTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), from);
            dbtr.getThesaurusCategory_uf_translation(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), link);
        }

        int index = Parameters.CLASS_SET.indexOf("TERM");
        String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

        // get all instances of translation links
        Q.reset_name_scope();
        Q.set_current_node(from);
        Q.set_current_node(link);
        int set_translation_classes = Q.get_subclasses(0); //set1
        Q.reset_set(set_translation_classes);
        int set_instances_links = Q.get_all_instances(set_translation_classes);//set2
        Q.reset_set(set_instances_links);

        //Get All terms for display
        int set_terms = get_Instances_Set(DescriptorClasses, Q, sis_session);//set3
        Q.reset_set(set_terms);

        //Get all links from the above terms. tThen intersect with the set of all translation links
        int set_from_labels = Q.get_link_from_by_category(set_terms, from, link);//set4
        Q.reset_set(set_from_labels);
        Q.reset_set(set_instances_links);
        Q.set_intersect(set_from_labels, set_instances_links);
        Q.reset_set(set_from_labels);

        //Get To value of these labels in order to get the set of their bt names
        int set_from_names = Q.get_to_value(set_from_labels);//set5
        Q.reset_set(set_from_names);

        //Filter BT Names contained in set_bt_names according to searchVal
        int set_from_criteria_names = filterLinksByName(prefixes, set_from_names, operator, searchVal, Q, sis_session, SessionUserInfo.selectedThesaurus);//set6
        Q.reset_set(set_from_criteria_names);

        //get all BT Labels from BT Names (From NT --> LABEL --> TO BT)
        int set_from_criteria_labels = Q.get_link_to_by_category(set_from_criteria_names, from, link);//set7
        Q.reset_set(set_from_criteria_labels);
        Q.reset_set(set_instances_links);
        Q.set_intersect(set_from_criteria_labels, set_instances_links);

        //get the from values of these labels (From --> LABEL --> TO BT)
        int set_from_results = Q.get_from_value(set_from_criteria_labels);//set8
        Q.reset_set(set_from_results);

        //ensure that no non display terms will appear in the result set
        Q.set_intersect(set_from_results, set_terms);
        Q.reset_set(set_from_results);

        Q.free_set(set_translation_classes);//free set1
        Q.free_set(set_instances_links);//free set2
        Q.free_set(set_terms);//free set3
        Q.free_set(set_from_labels);//free set4
        Q.free_set(set_from_names);//free set5
        Q.free_set(set_from_criteria_names);//free set6
        Q.free_set(set_from_criteria_labels);//free set7

        Q.reset_set(set_from_results);

        return set_from_results;
    }

    public int filterLinksByStatus(UserInfoClass SessionUserInfo, int sisSessionId, String operator, String searchVal, QClass Q, IntegerObject sis_session) {

        int set_results = Q.set_get_new();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject statusUnderConstructionObj = new StringObject();
        StringObject statusForApprovalObj = new StringObject();
        StringObject statusForInsertionObj = new StringObject();
        StringObject statusForReinspectionObj = new StringObject();
        StringObject statusApprovedObj = new StringObject();

        dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus, statusUnderConstructionObj);
        dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus, statusForApprovalObj);
        dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus, statusForInsertionObj);
        dbtr.getThesaurusClass_StatusForReinspection(SessionUserInfo.selectedThesaurus, statusForReinspectionObj);
        dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, statusApprovedObj);

        HashMap<String, StringObject> all_statuses = new HashMap<String, StringObject>();

        all_statuses.put(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Under_Construction, SessionUserInfo), statusUnderConstructionObj);
        all_statuses.put(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Approval, SessionUserInfo), statusForApprovalObj);
        all_statuses.put(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Insertion, SessionUserInfo), statusForInsertionObj);
        all_statuses.put(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_For_Reinspection, SessionUserInfo), statusForReinspectionObj);
        all_statuses.put(Parameters.getStatusRepresentation_ForDisplay(Parameters.Status_Approved, SessionUserInfo), statusApprovedObj);

        ArrayList<StringObject> filtered_Status_Vec = new ArrayList<>();

        Iterator<String> keysEnum = all_statuses.keySet().iterator();
        String targetKey;
        while (keysEnum.hasNext()) {

            targetKey = keysEnum.next();

            if (operator.equals(ConstantParameters.searchOperatorEquals)) {
                if (targetKey.compareTo(searchVal) != 0) {
                    continue;
                }
                //if (targetKey.compareTo(Status_Approved) == 0) {                    
                filtered_Status_Vec.add(all_statuses.get(targetKey));
                //}

            } else if (operator.equals(ConstantParameters.searchOperatorContains)) {

                if (searchVal.startsWith("*", 0)) {
                    searchVal = searchVal.substring(1, searchVal.length());
                }
                if (searchVal.endsWith("*")) {
                    searchVal = searchVal.substring(0, searchVal.length() - 1);
                }
                if (targetKey.toLowerCase().contains(searchVal.toLowerCase()) == true) {
                    filtered_Status_Vec.add(all_statuses.get(targetKey));
                }

            } else if (operator.equals("!")) {

                if (targetKey.compareTo(searchVal) != 0) {
                    filtered_Status_Vec.add(all_statuses.get(targetKey));
                }

            } else if (operator.equals(ConstantParameters.searchOperatorNotContains)) {

                if (searchVal.startsWith("*", 0)) {
                    searchVal = searchVal.substring(1, searchVal.length());
                }
                if (searchVal.endsWith("*")) {
                    searchVal = searchVal.substring(0, searchVal.length() - 1);
                }
                if (targetKey.toLowerCase().contains(searchVal.toLowerCase()) == false) {
                    filtered_Status_Vec.add(all_statuses.get(targetKey));
                }
            }

        }

        if (filtered_Status_Vec.isEmpty()) {
            Q.reset_set(set_results);
            return set_results;
        }

        for (int i = 0; i < filtered_Status_Vec.size(); i++) {
            Q.reset_name_scope();
            Q.set_current_node(filtered_Status_Vec.get(i));
            int set_partial = Q.get_instances(0);
            Q.reset_set(set_partial);
            Q.reset_set(set_results);
            Q.set_union(set_results, set_partial);
            Q.reset_set(set_results);
            Q.free_set(set_partial);
        }

        Q.reset_name_scope();
        Q.reset_set(set_results);

        return set_results;
    }

    public int filterLinksByName(String[] prefixes, int set_target, String operator, String searchVal,
            QClass Q, IntegerObject sis_session, String selectedThesaurus) {

        //int set_results = Q.set_get_new();
        int set_results = -1;
        Q.reset_name_scope();
        TMSAPIClass TA = new TMSAPIClass();
        TA.ALMOST_DONE_create_TMS_API_Session(Q, selectedThesaurus);

        try {
            if (operator.equals(ConstantParameters.searchOperatorEquals)) {

                if (searchVal != null && searchVal.trim().length() > 0) {
                    for (int i = 0; i < prefixes.length; i++) {
                        String prefix = prefixes[i];
                        if (Q.set_current_node(new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                            set_results = Q.set_get_new();
                            Q.set_put(set_results);
                            Q.reset_set(set_results);

                        }
                    }
                }

            } //else if (operator.equals("transliteration=")) {
            //    Q.reset_set(set_target);
            //    set_results = Q.get_matched_OnTransliteration(set_target, Utilities.getTransliterationString(searchVal,false),true);
            //    Q.reset_set(set_results);
            //}
            else if (operator.equals("refid=")) {
                if (searchVal != null && searchVal.trim().length() > 0) {

                    long refId = -1;
                    try {
                        refId = Long.parseLong(searchVal);
                    } catch (NumberFormatException ex) {

                    }

                    if (refId > 0) {
                        if (TA.set_current_node_by_referenceId(refId, selectedThesaurus) != QClass.APIFail) {
                            set_results = Q.set_get_new();
                            Q.set_put(set_results);
                            Q.reset_set(set_results);
                        }
                    }

                    for (int i = 0; i < prefixes.length; i++) {
                        String prefix = prefixes[i];
                        if (Q.set_current_node(new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                            set_results = Q.set_get_new();
                            Q.set_put(set_results);
                            Q.reset_set(set_results);

                        }
                    }
                }
            } else if (operator.equals(ConstantParameters.searchOperatorTransliterationEquals)) {
                Q.reset_set(set_target);
                set_results = TA.get_matched_OnTransliteration(set_target, Utilities.getTransliterationString(searchVal, false), true);
                Q.reset_set(set_results);
            } else if (operator.equals(ConstantParameters.searchOperatorNotTransliterationEquals)) {

                Q.reset_set(set_target);

                int set_exclude = TA.get_matched_OnTransliteration(set_target, Utilities.getTransliterationString(searchVal, false), true);

                Q.reset_set(set_target);
                set_results = Q.set_get_new();
                Q.set_copy(set_results, set_target);

                Q.reset_set(set_results);
                Q.reset_set(set_exclude);
                Q.set_difference(set_results, set_exclude);
                Q.reset_set(set_results);

                Q.free_set(set_exclude);
            } else if (operator.equals(ConstantParameters.searchOperatorTransliterationContains)) {
                Q.reset_set(set_target);
                set_results = TA.get_matched_OnTransliteration(set_target, Utilities.getTransliterationString(searchVal, false), false);
                Q.reset_set(set_results);
            } else if (operator.equals(ConstantParameters.searchOperatorNotTransliterationContains)) {

                Q.reset_set(set_target);

                int set_exclude = TA.get_matched_OnTransliteration(set_target, Utilities.getTransliterationString(searchVal, false), false);

                Q.reset_set(set_target);
                set_results = Q.set_get_new();
                Q.set_copy(set_results, set_target);

                Q.reset_set(set_results);
                Q.reset_set(set_exclude);
                Q.set_difference(set_results, set_exclude);
                Q.reset_set(set_results);

                Q.free_set(set_exclude);
            } else if (operator.equals(ConstantParameters.searchOperatorContains)) {
                Q.reset_set(set_target);

                //set_results = Q.get_matched_ToneAndCaseInsensitive(set_target, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                //set_results = Q.get_matched_CaseInsensitive(set_target, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                if (Parameters.SEARCH_MODE_CASE_INSENSITIVE) {
                    set_results = Q.get_matched_CaseInsensitive(set_target, searchVal, true);
                } else {
                    set_results = Q.get_matched_ToneAndCaseInsensitive(set_target, searchVal, false);
                }
                Q.reset_set(set_results);
            } else if (operator.equals("!")) {

                int set_exclude_facets = -1;

                for (String prefix : prefixes) {
                    if (Q.set_current_node(new StringObject(prefix.concat(searchVal))) != QClass.APIFail) {
                        set_exclude_facets = Q.set_get_new();
                        Q.set_put(set_exclude_facets);
                        Q.reset_set(set_exclude_facets);
                    }
                }
                Q.reset_set(set_target);
                set_results = Q.set_get_new();
                Q.set_copy(set_results, set_target);

                Q.reset_set(set_results);
                Q.reset_set(set_exclude_facets);
                Q.set_difference(set_results, set_exclude_facets);
                Q.reset_set(set_results);

                Q.free_set(set_exclude_facets);
            } else if (operator.equals(ConstantParameters.searchOperatorNotContains)) {

                int set_exclude_facets;

                Q.reset_set(set_target);
                //set_exclude_facets = WTA.get_matched_ToneAndCaseInsensitive( set_target, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                //set_exclude_facets = Q.get_matched_ToneAndCaseInsensitive(set_target, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);

                if (Parameters.SEARCH_MODE_CASE_INSENSITIVE) {
                    set_exclude_facets = Q.get_matched_CaseInsensitive(set_target, searchVal, true);
                } else {
                    set_exclude_facets = Q.get_matched_ToneAndCaseInsensitive(set_target, searchVal, false);
                }
                Q.reset_set(set_target);
                set_results = Q.set_get_new();
                Q.set_copy(set_results, set_target);

                Q.reset_set(set_results);
                Q.reset_set(set_exclude_facets);
                Q.set_difference(set_results, set_exclude_facets);
                Q.reset_set(set_results);

                Q.free_set(set_exclude_facets);
            } else if (operator.equals(">=") || operator.equals("<=")) {
                Q.reset_set(set_target);
                //StringObject l_name = new StringObject();
                ArrayList<String> termsVector = new ArrayList<String>();
                ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
                if (Q.bulk_return_nodes(set_target, retVals) != QClass.APIFail) {
                    for (Return_Nodes_Row row : retVals) {
                        if (operator.equals(">=")) {
                            if (row.get_v1_cls_logicalname().compareTo(searchVal) >= 0) {
                                termsVector.add(row.get_v1_cls_logicalname());
                            }
                        } else {
                            if (row.get_v1_cls_logicalname().compareTo(searchVal) <= 0) {
                                termsVector.add(row.get_v1_cls_logicalname());
                            }
                        }
                    }
                }
                /*
             while ((Q.retur_nodes( set_target, l_name)) != QClass.APIFail) {
             if (operator.equals(">=")) {
             if (l_name.getValue().compareTo(searchVal) >= 0) {
             termsVector.add(l_name.getValue());
             }
             }
             else {
             if (l_name.getValue().compareTo(searchVal) <= 0) {
             termsVector.add(l_name.getValue());
             }                    
             }
             }*/
                set_results = Q.set_get_new();
                int termsVectorSize = termsVector.size();
                for (int i = 0; i < termsVectorSize; i++) {
                    //Utils.StaticClass.webAppSystemOutPrintln("DATE = " + termsVector.get(i));
                    Q.reset_name_scope();
                    Q.set_current_node(new StringObject(termsVector.get(i)));
                    Q.set_put(set_results);
                }
                Q.reset_set(set_results);
            }

        } finally {
            TA.ALMOST_DONE_release_TMS_API_Session();
        }
        return set_results;

    }

    /*-----------------------------------------------------------------
     ThesaurusVersion()
     -------------------------------------------------------------------
     INPUT: - thesaurus, the name of the given thesaurus (g.e. Thesaurus`THES1)
     OUTPUT: - version_number, the version number of the given thesaurus
     FUNCTION: gets the version number of the given thesaurus. This is done by:
     - getting the link pointing from the given thesaurus and under category ("Thesaurus","version-number")
     - getting the to_value of the above link
     -----------------------------------------------------------------*/
    public int ThesaurusVersion(StringObject thesaurus, QClass Q, IntegerObject sis_session) {

        int version_number;
        // get the link pointing from the given thesaurus and under category ("Thesaurus","version-number")
        Q.reset_name_scope();
        long thesaurusSySIdL = Q.set_current_node(thesaurus);
        if (thesaurusSySIdL <= 0) {
            return -1;
        }
        int ret_set1 = Q.get_link_from_by_category(0, new StringObject("Thesaurus"), new StringObject("version-number"));
        if ((ret_set1 == -1) || (Q.set_get_card(ret_set1) != 1)) {
            Q.free_set(ret_set1);
            return -1;
        }

        // get the to_value of the above link
        Q.reset_set(ret_set1);
        int ret_set2 = Q.get_to_value(ret_set1);
        Q.free_set(ret_set1);
        if ((ret_set2 == -1) || (Q.set_get_card(ret_set2) != 1)) {
            Q.free_set(ret_set2);
            return -1;
        }
        Q.reset_set(ret_set2);
        CMValue cmv = new CMValue();
        int ret_val = Q.return_prm(ret_set2, cmv);
        if (ret_val == -1) {
            Q.free_set(ret_set2);
            return -1;
        }
        version_number = cmv.getInt();

        Q.free_set(ret_set2);
        return version_number;
    }

    /*---------------------------------------------------------------------
     GetExistingThesaurus()
     -----------------------------------------------------------------------
     OUTPUT: a Vector with the existing Thesaurus in DB
     ----------------------------------------------------------------------*/
    public ArrayList<String> GetExistingThesaurus(boolean startQuerySession, ArrayList<String> thesaurusVector, QClass Q, IntegerObject sis_session) {

        if (startQuerySession == true) {
            // START query
            Q.TEST_begin_query();
        }

        //DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // String prefix = dbtr.getThesaurusPrefix_Class();
        // StringObject targetHierarchy_el = new StringObject(prefix.concat(targetHierarchy));
        int SISsession = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Thesaurus"));
        int set_c = Q.get_instances(0);

        //Vector thesaurusVector = new Vecto();
        Q.reset_set(set_c);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_c, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                //Cname.setValue(removePrefix(Cname.getValue()));
                thesaurusVector.add(removePrefix(row.get_v1_cls_logicalname()));
            }
        }
        //StringObject Cname = new StringObject();
        /*while ((Q.retur_nodes( set_c, Cname)) != QClass.APIFail) {
         Cname.setValue(removePrefix(Cname.getValue()));
         thesaurusVector.add(Cname.getValue());
         }*/
        Q.free_set(set_c);

        Collections.sort(thesaurusVector);

        if (startQuerySession == true) {
            // END query
            Q.TEST_end_query();
        } else {
            Q.reset_name_scope();
        }
        return thesaurusVector;
    }

    /*
     public String getPrefix(String targetClass) {
     String prefix_source = "";
     Q.reset_name_scope();
     Q.set_current_node(new StringObject(targetClass));
     int labels = Q.get_link_from_by_category(0, new StringObject("Individual"), new StringObject("has_prefix"));
     Q.reset_set(labels);

     if (Q.set_get_card(labels) != 0) {
     int values = Q.get_to_value(labels);
     Q.reset_set(values);

     StringObject name = new StringObject();
     //should only have one object 
     if (Q.retur_nodes(values, name) != Q.APIFail) {
     prefix_source = name.getValue();
     }

     Q.free_set(values);
     }

     Q.free_set(labels);
     Q.reset_name_scope();

     return prefix_source;
     }
     */

    public ArrayList<SortItem> get_term_facets_or_topterms_InSortItemVector(UserInfoClass SessionUserInfo, String term, int facet_or_topterm, QClass Q, IntegerObject sis_session) {

        int SISApiSession = sis_session.getValue();
        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");

        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);

        index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_el = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, SISApiSession);

        StringObject descrObj = new StringObject(prefix_el.concat(term));

        Q.reset_name_scope();

        if (Q.set_current_node(descrObj) != QClass.APIFail) {

            int set_all_classes = Q.get_all_classes(0);
            Q.reset_set(set_all_classes);

            if (facet_or_topterm == ConstantParameters.get_Term_Facets) {

                int set_facets = get_Instances_Set(FacetClasses, Q, sis_session);
                Q.reset_set(set_facets);
                Q.reset_set(set_all_classes);

                Q.set_intersect(set_all_classes, set_facets);
                Q.reset_set(set_all_classes);
                /*
                 Q.reset_name_scope();
                 StringObject label = new StringObject();
                 while (Q.retur_nodes( set_all_classes, label) != QClass.APIFail) {
                 results.add(label.getValue());
                 }

                 results = removePrefix(results);

                 */
                Q.free_set(set_facets);
            } else //each hierarchy is supposed to be related with a TopTerm with the same name and different prefix
            if (facet_or_topterm == ConstantParameters.get_Term_Top_Terms) {

                int set_hierarchies = get_Instances_Set(HierarchyClasses, Q, sis_session);

                Q.reset_set(set_hierarchies);
                Q.reset_set(set_all_classes);

                Q.set_intersect(set_all_classes, set_hierarchies);
                Q.reset_set(set_all_classes);

                // FILTER Top terms depending on user group
                // get the TopTerms of hiersSet
                // looking for AAATopTerm 
                StringObject TopTerm = new StringObject();
                dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTerm);
                // looking for belongs_to_aaa_hierarchy 
                StringObject TopTermHierRelationObj = new StringObject();
                dbtr.getThesaurusCategory_belongs_to_hierarchy(SessionUserInfo.selectedThesaurus, Q, SISApiSession, TopTermHierRelationObj);
                int TopTermsLinksSet1 = Q.get_link_to_by_category(set_all_classes, TopTerm, TopTermHierRelationObj);
                Q.free_set(set_all_classes);
                Q.reset_set(TopTermsLinksSet1);
                int TopTermsSet = Q.get_from_value(TopTermsLinksSet1);
                Q.reset_set(TopTermsSet);
                Q.free_set(TopTermsLinksSet1);

                set_all_classes = TopTermsSet;

                DBFilters dbf = new DBFilters();
                set_all_classes = dbf.FilterTermsResults(SessionUserInfo, set_all_classes, Q, sis_session);

                Q.free_set(set_hierarchies);

            }

            ArrayList<SortItem> results = get_Node_Names_Of_Set_In_SortItems(set_all_classes, true, Q, sis_session);
            Q.free_set(set_all_classes);
            return results;
        }

        ArrayList<SortItem> results = new ArrayList<SortItem>();
        return results;
    }

    //facet_or_topterm: == get_Term_Facets   == 0   --> mode = facet
    //facet_or_topterm: ==get_Term_Top_Terms == 1 --> mode = topterm     
    public ArrayList<String> get_term_facets_or_topterms(UserInfoClass SessionUserInfo, String term, int facet_or_topterm, QClass Q, IntegerObject sis_session) {

        ArrayList<String> results = new ArrayList<String>();

        ArrayList<SortItem> resultsInSortItem = get_term_facets_or_topterms_InSortItemVector(SessionUserInfo, term, facet_or_topterm, Q, sis_session);
        if (resultsInSortItem != null) {
            for (SortItem item : resultsInSortItem) {
                results.add(item.getLogName());
            }
        }
        return results;
    }

    public ArrayList<CMValue> get_Node_Cmvalues_Of_Set(int set_target, boolean removePrefixes, QClass Q, IntegerObject sis_session) {
        ArrayList<CMValue> names = new ArrayList<CMValue>();

        Q.reset_name_scope();
        Q.reset_set(set_target);
        //StringObject label = new StringObject();

        CMValue newItem = new CMValue();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_target, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                newItem.assign_node(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId(), row.get_v3_cls_transliteration(), row.get_v2_long_referenceId());
                names.add(newItem.getCmvCopy());
            }
        }
        /*while (Q.retur_nodes(set_target, label) != QClass.APIFail) {

         names.add(label.getValue());
         }*/

        if (removePrefixes) {
            names = removeCmvVectorPrefix(names);
        }

        names.trimToSize();

        return names;
    }

    public ArrayList<SortItem> get_Node_Names_Of_Set_In_SortItems(int set_target, boolean removePrefixes, QClass Q, IntegerObject sis_session) {
        ArrayList<SortItem> names = new ArrayList<SortItem>();

        Q.reset_name_scope();
        Q.reset_set(set_target);
        //StringObject label = new StringObject();

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_target, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                SortItem newItem = new SortItem(row.get_v1_cls_logicalname(), row.get_Neo4j_NodeId(), "", row.get_v3_cls_transliteration(), row.get_v2_long_referenceId());
                names.add(newItem);
            }
        }
        /*while (Q.retur_nodes(set_target, label) != QClass.APIFail) {

         names.add(label.getValue());
         }*/

        if (removePrefixes) {
            names = removeSortItemArrayListPrefix(names);
        }

        names.trimToSize();

        return names;
    }

    public ArrayList<String> get_Node_Names_Of_Set(int set_target, boolean removePrefixes, QClass Q, IntegerObject sis_session) {
        ArrayList<String> names = new ArrayList<String>();

        Q.reset_name_scope();
        Q.reset_set(set_target);
        //StringObject label = new StringObject();

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_target, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                names.add(row.get_v1_cls_logicalname());
            }
        }
        /*while (Q.retur_nodes(set_target, label) != QClass.APIFail) {

         names.add(label.getValue());
         }*/

        if (removePrefixes) {
            names = removePrefix(names);
        }

        names.trimToSize();

        return names;
    }

    public boolean collect_Recurcively_ALL_BTs(String selectedThesaurus, StringObject targetDescriptorObj, int set_result, StringObject resultMessage, boolean includeTarget, QClass Q, IntegerObject sis_session, final String uiLang) {

        int sisSessionId = sis_session.getValue();
        Utilities u = new Utilities();
        StringObject btFromObj = new StringObject();
        StringObject btCategObj = new StringObject();
        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btCategObj, Q, sis_session);

        Q.reset_name_scope();

        /*Check if target node exists*/
        Q.reset_name_scope();
        if (Q.set_current_node(targetDescriptorObj) == QClass.APIFail) {
            resultMessage.setValue(resultMessage.getValue().concat(u.translateFromMessagesXML("root/GeneralMessages/TermNotfoundInTheDatabase", new String[]{targetDescriptorObj.getValue()}, uiLang)));
            //resultMessage.setValue(resultMessage.getValue().concat("Term: " + targetDescriptorObj.getValue() + " was not found in the database"));
            return false;
        }

        /*Bts colletion process Starts*/
        int partial = Q.set_get_new();
        Q.set_put(partial);
        Q.reset_set(partial);

        if (includeTarget) {
            Q.reset_set(set_result);
            Q.set_union(set_result, partial);
            Q.reset_set(set_result);
        }

        while (Q.set_get_card(partial) != 0) {

            int partial_labels = Q.get_link_from_by_category(partial, btFromObj, btCategObj);
            Q.reset_set(partial_labels);

            if (Q.set_get_card(partial_labels) == 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.free_set(partial);
            partial = Q.get_to_value(partial_labels);
            Q.reset_set(partial);

            if (Q.set_get_card(partial) == 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);

            Q.free_set(partial_labels);
        }

        Q.free_set(partial);
        Q.reset_set(set_result);

        return true;
    }

    public boolean collect_Recurcively_ALL_NTs(String selectedThesaurus, StringObject targetDescriptorObj, int set_result, StringObject resultMessage, boolean includeTarget, QClass Q, IntegerObject sis_session, final String uiLang) {

        int sisSessionId = sis_session.getValue();
        Utilities u = new Utilities();
        StringObject btFromObj = new StringObject();
        StringObject btCategObj = new StringObject();
        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btFromObj, btCategObj, Q, sis_session);
        Q.reset_name_scope();

        /*Check if target node still exists*/
        if (Q.set_current_node(targetDescriptorObj) == QClass.APIFail) {
            resultMessage.setValue(resultMessage.getValue().concat(u.translateFromMessagesXML("root/EditTerm/Edit/TermDoesNotExist", new String[]{targetDescriptorObj.getValue()}, uiLang)));
            //resultMessage.setValue(resultMessage.getValue().concat("Term " + targetDescriptorObj.getValue() + " was not found in the database"));
            return false;
        }

        /*Nts colletion process Starts*/
        int partial = Q.set_get_new();
        Q.set_put(partial);
        Q.reset_set(partial);

        if (includeTarget) {
            Q.reset_set(set_result);
            Q.set_union(set_result, partial);
            Q.reset_set(set_result);
        }

        // Q.reset_name_scope();
        while (Q.set_get_card(partial) != 0) {

            int partial_labels = Q.get_link_to_by_category(partial, btFromObj, btCategObj);
            Q.reset_set(partial_labels);

            if (Q.set_get_card(partial_labels) == 0) {
                Q.free_set(partial_labels);
                break;

            }
            Q.free_set(partial);

            partial = Q.get_from_value(partial_labels);
            Q.reset_set(partial);

            if (Q.set_get_card(partial) == 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);

            Q.free_set(partial_labels);

        }

        Q.free_set(partial);
        Q.reset_set(set_result);

        /*Nts colletion process Completed*/
        return true;
    }

    /*Function collect_Direct_Links_Of_Set()
     *Starting from "target_set" gets all links specified from "from" and "link" and the given "direction"
     *Results are stored in "set_result" which includes "target_set" if "includeTarget" is set to true.
     *Used for links like Bts, Nts, Rts, UF, Alt, Dewey etc.
     */

    public void collect_Direct_Links_Of_Set(int target_set, int set_result, boolean includeTarget, String from, String link, int direction, QClass Q, IntegerObject sis_session) {

        int sisSessionId = sis_session.getValue();
        Q.reset_name_scope();
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Function \"collect_Direct_Links_Of_Set\" called with parameters:\n");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"target_set = " + target_set + " set_result = " + set_result + " includeTarget = " + String.valueOf(includeTarget) + " fromClass = " + from + " link = " + link + " direction = " + direction);
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nTarget Set included : \n'" + getStringList_Of_Set(target_set, "'\n'") + "'");

        if (includeTarget) {

            Q.reset_set(target_set);
            Q.reset_set(set_result);
            Q.set_union(set_result, target_set);
            Q.reset_set(set_result);
        }

        StringObject fromObj = new StringObject(from);
        StringObject linkObj = new StringObject(link);

        if (direction == ConstantParameters.TO_Direction || direction == ConstantParameters.BOTH_Direction) {

            Q.reset_name_scope();

            //Collect toClass-link category relations ending TO TARGET SET
            Q.reset_set(target_set);
            int partial_labels = Q.get_link_to_by_category(target_set, fromObj, linkObj);
            Q.reset_set(partial_labels);

            //Collect from values of labels retrieved
            int partial = Q.get_from_value(partial_labels);
            Q.reset_set(partial);

            /*add results to set result*/
            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);
            Q.reset_set(set_result);

            Q.free_set(partial_labels);
            Q.free_set(partial);

        }
        if (direction == ConstantParameters.FROM_Direction || direction == ConstantParameters.BOTH_Direction) {

            //Collect fromClass-link categroy relations starting FROM TARGET SET
            int partial_labels = Q.get_link_from_by_category(target_set, fromObj, linkObj);
            Q.reset_set(partial_labels);

            //Collect to values of labels retrieved
            int partial = Q.get_to_value(partial_labels);
            Q.reset_set(partial);

            /*add results to set result*/
            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);
            Q.reset_set(set_result);

            Q.free_set(partial_labels);
            Q.free_set(partial);

        }

        Q.reset_set(set_result);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nResult Set includes : \n'" + getStringList_Of_Set(set_result, "'\n'") + "'");
        return;

    }

    public void collect_Recurcively_ALL_BTs_Of_Set(String selectedThesaurus, int set_target, int set_result, boolean includeTarget, QClass Q, IntegerObject sis_session) {

        int sisSessionId = sis_session.getValue();

        StringObject btClassFromObj = new StringObject();
        StringObject btCategObj = new StringObject();

        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btClassFromObj, btCategObj, Q, sis_session);
        Q.reset_name_scope();
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Function \"collect_Recurcively_ALL_BTs_Of_Set\" called with parameters:\n");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"target_set = " + set_target + " set_result = " + set_result + " includeTarget = " + String.valueOf(includeTarget));
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nTarget Set included : \n'" + getStringList_Of_Set(set_target, "'\n'") + "'");

        //Bts colletion process Starts
        Q.reset_set(set_target);
        int partial = Q.set_get_new();
        Q.set_copy(partial, set_target);
        Q.reset_set(partial);

        if (includeTarget) {

            Q.reset_set(set_result);
            Q.reset_set(set_target);
            Q.set_union(set_result, set_target);
            Q.reset_set(set_result);
        }

        while (Q.set_get_card(partial) > 0) {

            int partial_labels = Q.get_link_from_by_category(partial, btClassFromObj, btCategObj);
            Q.reset_set(partial_labels);

            if (Q.set_get_card(partial_labels) <= 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.free_set(partial);
            partial = Q.get_to_value(partial_labels);
            Q.reset_set(partial);
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+(getStringList_Of_Set(partial, " , ", Q, sis_session));
            if (Q.set_get_card(partial) <= 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);

            Q.free_set(partial_labels);

        }

        Q.free_set(partial);
        Q.reset_set(set_result);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nResult Set includes : \n'" + getStringList_Of_Set(set_result, "'\n'") + "'");
        return;

    }

    /***
     * 
     * @param selectedThesaurus
     * @param set_target
     * @param set_result
     * @param includeTarget
     * @param filteringSet: a set that may be used for intersection filtering in every step of recursion. 
     * if no such functionality is needed then provide -1 as value
     * @param Q
     * @param sis_session 
     */
    public void collect_Recurcively_ALL_NTs_Of_Set(String selectedThesaurus, int set_target, int set_result, boolean includeTarget, int filteringSet, QClass Q, IntegerObject sis_session) {

        int sisSessionId = sis_session.getValue();
        StringObject btClassFromObj = new StringObject();
        StringObject btCategObj = new StringObject();

        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, btClassFromObj, btCategObj, Q, sis_session);
        Q.reset_name_scope();
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Function \"collect_Recurcively_ALL_NTs_Of_Set\" called with parameters:\n");
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"target_set = " + set_target + " set_result = " + set_result + " includeTarget = " + String.valueOf(includeTarget));
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nTarget Set included : \n'" + getStringList_Of_Set(set_target, "'\n'") + "'");

        //Bts colletion process Starts
        Q.reset_set(set_target);
        int partial = Q.set_get_new();
        Q.set_copy(partial, set_target);
        Q.reset_set(partial);

        if(filteringSet>0){
            Q.reset_set(set_result);
            Q.reset_set(filteringSet);
            Q.set_intersect(set_result, filteringSet);
            Q.reset_set(set_result);
        }
        if (includeTarget) {
            Q.reset_set(set_result);
            Q.reset_set(set_target);
            Q.set_union(set_result, set_target);
            Q.reset_set(set_result);
            
            if(filteringSet>0){
                Q.reset_set(set_result);
                Q.reset_set(filteringSet);
                Q.set_intersect(set_result, filteringSet);
                Q.reset_set(set_result);
            }
        }

        // Q.reset_name_scope();
        while (Q.set_get_card(partial) > 0) {

            int partial_labels = Q.get_link_to_by_category(partial, btClassFromObj, btCategObj);
            Q.reset_set(partial_labels);

            if (Q.set_get_card(partial_labels) <= 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.free_set(partial);
            partial = Q.get_from_value(partial_labels);
            Q.reset_set(partial);

            if (Q.set_get_card(partial) <= 0) {
                Q.free_set(partial_labels);
                break;

            }

            Q.reset_set(set_result);
            Q.reset_set(partial);
            Q.set_union(set_result, partial);

            
            Q.free_set(partial_labels);
            
            if(filteringSet>0){
                Q.reset_set(set_result);
                Q.reset_set(filteringSet);
                Q.set_intersect(set_result, filteringSet);
                Q.reset_set(set_result);
            }

        }

        Q.free_set(partial);
        Q.reset_set(set_result);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nResult Set includes : \n'" + getStringList_Of_Set(set_result, "'\n'") + "'");        
    }

    public String getStringList_Of_Set(int set_print, String delimiter, QClass Q, IntegerObject sis_session) {
        String result = new String("");

        Q.reset_name_scope();
        Q.reset_set(set_print);
        //StringObject label = new StringObject();
        int howmany = Q.set_get_card(set_print);
        int index = 0;
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_print, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                index++;
                String temp = row.get_v1_cls_logicalname();
                result += removePrefix(temp);
                if (index != howmany) {
                    result += delimiter;
                }
            }
        }
        /*
         while (Q.retur_nodes(set_print, label) != QClass.APIFail) {
         index++;
         String temp = label.getValue();
         result += removePrefix(temp);
         if (index != howmany) {
         result += delimiter;
         }

         }*/
        return result;
    }

    public void getKeywordPair(String selectedThesaurus, String keyWord, StringObject retFrom, StringObject retLink, QClass Q, IntegerObject sis_session) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Q.reset_name_scope();
        if (keyWord.equalsIgnoreCase("name")) {
            //nothing to do
        } else if (keyWord.equalsIgnoreCase(ConstantParameters.translation_kwd)) {
            dbtr.getThesaurusClass_Term(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_translation(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.bt_kwd)) {

            dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.nt_kwd)) {

            dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.rt_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_RT(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.uf_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_UF(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.uf_translations_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_uf_translations(selectedThesaurus, retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.dn_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_dewey(selectedThesaurus, retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.tc_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_taxonomic_code(selectedThesaurus, retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.alt_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_ALT(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.primary_found_in_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_primary_found_in(selectedThesaurus, retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.translations_found_in_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_translations_found_in(selectedThesaurus, retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.scope_note_kwd)) {

            dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_scope_note(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.translations_scope_note_kwd)) {

            dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), retLink);

        }  else if (keyWord.equalsIgnoreCase(ConstantParameters.historical_note_kwd)) {

            dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_historical_note(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.comment_kwd)) {

            dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_comment(selectedThesaurus, Q, sis_session.getValue(), retLink);

        }
        else if (keyWord.equalsIgnoreCase(ConstantParameters.note_kwd)) {

            dbtr.getThesaurusClass_ThesaurusConcept(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_note(selectedThesaurus, Q, sis_session.getValue(), retLink);

        }else if (keyWord.equalsIgnoreCase(ConstantParameters.created_by_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.modified_by_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_modified_by(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.created_on_kwd)) {

            //PENDING THES1HierarchyTerm - thes1_created
            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), retLink);

        } else if (keyWord.equalsIgnoreCase(ConstantParameters.modified_on_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_modified(selectedThesaurus, Q, sis_session.getValue(), retLink);
        } else if (keyWord.equalsIgnoreCase(ConstantParameters.editor_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_editor(selectedThesaurus, Q, sis_session.getValue(), retLink);
        } else if (keyWord.equalsIgnoreCase(ConstantParameters.foundIn_kwd)) {

            dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_found_in(selectedThesaurus, Q, sis_session.getValue(), retLink);
        } else if (keyWord.equalsIgnoreCase(ConstantParameters.source_note_kwd)) {

            retFrom.setValue(ConstantParameters.SourceClass);
            retLink.setValue(ConstantParameters.source_note_kwd);
        } else if (keyWord.equalsIgnoreCase(ConstantParameters.belongs_to_hier_kwd)) {

            dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), retFrom);
            dbtr.getThesaurusCategory_belongs_to_hierarchy(selectedThesaurus, Q, sis_session.getValue(), retLink);
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "No Keyword : " + keyWord + " supported");
        }
    }

    /*---------------------------------------------------------------------
     ExistingTermSortDescription()
     -----------------------------------------------------------------------
     INPUT: - StringObject targetTerm: the existing term to be described
     OUTPUT: - a StringObject with a sort description of the existing term
     FUNCTION: - 1. in case targetTerm has BT links pointing from it to terms x1, x2, ..., xn =>
     "as narrower term of terms: x1, x2, ..., xn"
     - 2. in case targetTerm belongs to EKTTopTerm => "as top term"
     - 3. in case targetTerm belongs to EKTUsedForTerm with UF links pointing to it from terms x1, x2, ..., xn =>
     "as non preferred term of terms: x1, x2, ..., xn"        
     ----------------------------------------------------------------------*/
    public StringObject ExistingTermSortDescription(String selectedThesaurus, StringObject targetTerm, QClass Q, IntegerObject sis_session, final String uiLang) {
        String description = "";
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Utilities u = new Utilities();

        // looking for Descriptor EKTDescriptor->EKT_BT
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thesDescriptor);
        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), thesBT);

        int sisSessionID = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(targetTerm);
        // 1. in case targetTerm has BT links pointing from it to terms x1, x2, ..., xn =>
        // "as narrower term of terms: x1, x2, ..., xn"
        int BTlinksSet = Q.get_link_from_by_category(0, thesDescriptor, thesBT);
        Q.reset_set(BTlinksSet);
        int BTlinksSetCard = Q.set_get_card(BTlinksSet);
        if (BTlinksSetCard >= 1) {
            description += "\r\n" + u.translateFromMessagesXML("root/GeneralMessages/ConsistencyChecks/AsNTtermOfTerms", null, uiLang);
        }
        //StringObject label = new StringObject();
        //StringObject cls = new StringObject();
        //CMValue cmv = new CMValue();
        int counter = 0;
        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(BTlinksSet, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String BTterm = row.get_v3_cmv().getString();
                String BTtermWithoutPrefix = BTterm.substring(BTterm.indexOf("`") + 1);
                counter++;
                if (counter > 1) {
                    description += ", ";
                }
                description += BTtermWithoutPrefix;
            }
        }
        /*while (Q.retur_link(BTlinksSet, cls, label, cmv) != QClass.APIFail) {
         String BTterm = cmv.getString();
         String BTtermWithoutPrefix = BTterm.substring(BTterm.indexOf("`") + 1);
         counter++;
         if (counter > 1) description += ", ";
         description += BTtermWithoutPrefix;
         } */

        Q.free_set(BTlinksSet);
        // 2. in case targetTerm belongs to EKTTopTerm =>
        // "as top term"
        // looking for EKTTopTerm 
        StringObject thesTopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), thesTopTerm);
        boolean isTopTerm = NodeBelongsToClass(targetTerm, thesTopTerm, false, Q, sis_session);
        if (isTopTerm == true) {
            description += "\r\n" + u.translateFromMessagesXML("root/GeneralMessages/ConsistencyChecks/AsTopTerm", null, uiLang);
        }
        // 3. in case targetTerm belongs to EKTUsedForTerm with UF links pointing to it from terms x1, x2, ..., xn =>
        // "as non preferred term of terms: x1, x2, ..., xn"        
        // looking for EKTUsedForTerm 
        StringObject thesUsedForTerm = new StringObject();
        dbtr.getThesaurusClass_UsedForTerm(selectedThesaurus, Q, sis_session.getValue(), thesUsedForTerm);
        boolean isUsedForTerm = NodeBelongsToClass(targetTerm, thesUsedForTerm, false, Q, sis_session);
        if (isUsedForTerm == true) {
            description += "\r\n" + u.translateFromMessagesXML("root/GeneralMessages/ConsistencyChecks/AsNonPreferredTerm", null, uiLang);
        }
        // looking for EKTHierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q, sis_session.getValue(), thesHierarchyTerm);
        // looking for EKT_UF 
        StringObject thes_UF = new StringObject();
        dbtr.getThesaurusCategory_UF(selectedThesaurus, Q, sis_session.getValue(), thes_UF);
        Q.reset_name_scope();
        Q.set_current_node(targetTerm);
        int UFlinksSet = Q.get_link_to_by_category(0, thesHierarchyTerm, thes_UF);
        Q.reset_set(UFlinksSet);
        int UFlinksSetCard = Q.set_get_card(UFlinksSet);
        if (UFlinksSetCard >= 1) {
            description += "\r\n" + u.translateFromMessagesXML("root/GeneralMessages/ConsistencyChecks/OfTermOrTerms", null, uiLang);
        }
        counter = 0;
        retVals.clear();
        if (Q.bulk_return_link(UFlinksSet, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String clsterm = row.get_v1_cls();
                String clstermWithoutPrefix = clsterm.substring(clsterm.indexOf("`") + 1);
                counter++;
                if (counter > 1) {
                    description += ", ";
                }
                description += clstermWithoutPrefix;
            }
        }
        /*while (Q.retur_link(UFlinksSet, cls, label, cmv) != QClass.APIFail) {
         String clsterm = cls.getValue();
         String clstermWithoutPrefix = clsterm.substring(clsterm.indexOf("`") + 1);
         counter++;
         if (counter > 1) description += ", ";
         description += clstermWithoutPrefix;
         } */
        Q.free_set(UFlinksSet);

        StringObject descriptionObj = new StringObject(description);
        return descriptionObj;
    }

    // though of the same value QClass.APIFail and TMSAPIClass.TMS_APIFail. 
    // code that uses this function checks about TMSAPIClass.TMS_APIFail. thus
    // if QClass.APIFail is reached TMSAPIClass.TMS_APIFail is returned
    // 
    public int renameCommentNodes(String selectedThesaurus, StringObject oldTermObj, StringObject newTermObj, QClass Q, IntegerObject sis_session) {

        int ret = QClass.APISucc;
        String oldName = oldTermObj.getValue().concat("`");
        String newName = newTermObj.getValue().concat("`");

        ret = Q.reset_name_scope();
        if (ret == QClass.APIFail) {
            return TMSAPIClass.TMS_APIFail;
        }
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject scopeNoteLinkObj = new StringObject();
        StringObject scopeNoteENLinkObj = new StringObject();
        StringObject historicalNoteLinkObj = new StringObject();
        StringObject commentLinkObj = new StringObject();

        dbtr.getThesaurusCategory_scope_note(selectedThesaurus, Q, sis_session.getValue(), scopeNoteLinkObj);
        dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), scopeNoteENLinkObj);
        dbtr.getThesaurusCategory_historical_note(selectedThesaurus, Q, sis_session.getValue(), historicalNoteLinkObj);
        dbtr.getThesaurusCategory_comment(selectedThesaurus, Q, sis_session.getValue(), commentLinkObj);

        ret = Q.reset_name_scope();

        if (ret == QClass.APIFail) {
            return TMSAPIClass.TMS_APIFail;
        }

        if (Q.set_current_node(new StringObject(oldName.concat(scopeNoteLinkObj.getValue()))) != QClass.APIFail) {

            ret = Q.CHECK_Rename_Node(new Identifier(oldName.concat(scopeNoteLinkObj.getValue())), new Identifier(newName.concat(scopeNoteLinkObj.getValue())));
            if (ret == QClass.APIFail) {
                return TMSAPIClass.TMS_APIFail;
            }

        }
        Q.reset_name_scope();
        if (Q.set_current_node(new StringObject(oldName.concat(scopeNoteENLinkObj.getValue()))) != QClass.APIFail) {

            ret = Q.CHECK_Rename_Node(new Identifier(oldName.concat(scopeNoteENLinkObj.getValue())), new Identifier(newName.concat(scopeNoteENLinkObj.getValue())));
            if (ret == QClass.APIFail) {
                return TMSAPIClass.TMS_APIFail;
            }

        }
        Q.reset_name_scope();
        if (Q.set_current_node(new StringObject(oldName.concat(historicalNoteLinkObj.getValue()))) != QClass.APIFail) {

            ret = Q.CHECK_Rename_Node(new Identifier(oldName.concat(historicalNoteLinkObj.getValue())), new Identifier(newName.concat(historicalNoteLinkObj.getValue())));
            if (ret == QClass.APIFail) {
                return TMSAPIClass.TMS_APIFail;
            }
        }
        Q.reset_name_scope();
        if (Q.set_current_node(new StringObject(oldName.concat(commentLinkObj.getValue()))) != QClass.APIFail) {

            ret = Q.CHECK_Rename_Node(new Identifier(oldName.concat(commentLinkObj.getValue())), new Identifier(newName.concat(commentLinkObj.getValue())));
            if (ret == QClass.APIFail) {
                return TMSAPIClass.TMS_APIFail;
            }
        }

        return TMSAPIClass.TMS_APISucc;
    }

    ArrayList<String> getHierarchyFacets(UserInfoClass SessionUserInfo, String hierarchy, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        if (hierarchy == null || hierarchy.length() == 0) {
            return new ArrayList<String>();
        }
        int index = Parameters.CLASS_SET.indexOf("FACET");

        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);

        //String language = ServletCaller.getServletContext().getInitParameter("LocaleLanguage");
        //String country = ServletCaller.getServletContext().getInitParameter("LocaleCountry");
        //Locale targetLocale = new Locale(language, country);
        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);

        ArrayList<String> results = new ArrayList<>();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject hierObj = new StringObject(prefix_class.concat(hierarchy));

        Q.reset_name_scope();
        Q.set_current_node(hierObj);
        int set_super_classes = Q.get_superclasses(0);
        Q.reset_set(set_super_classes);

        Q.reset_name_scope();
        int set_facets = get_Instances_Set(FacetClasses, Q, sis_session);

        Q.reset_set(set_facets);
        //int card = Q.set_get_card(set_facets);

        Q.set_intersect(set_super_classes, set_facets);
        Q.reset_set(set_super_classes);

        results.addAll(get_Node_Names_Of_Set(set_super_classes, true, Q, sis_session));
        Q.free_set(set_super_classes);
        Q.free_set(set_facets);
        Q.reset_name_scope();

        Collections.sort(results, strCompar);
        return results;

    }
    
    ArrayList<SortItem> getHierarchyFacetsInSortItems(UserInfoClass SessionUserInfo, String hierarchy, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        ArrayList<SortItem> results = new ArrayList<SortItem>();

        int index = Parameters.CLASS_SET.indexOf("FACET");
        String[] FacetClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(FacetClasses);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject hierObj = new StringObject(prefix_class.concat(hierarchy));

        StringObject belongsToHierClass = new StringObject();
        StringObject belongsToHierarchyLink = new StringObject();
        getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierClass, belongsToHierarchyLink, Q, sis_session);

        Q.reset_name_scope();
        Q.set_current_node(hierObj);
        int set_super_classes = Q.get_superclasses(0);
        Q.reset_set(set_super_classes);


        Q.reset_name_scope();
        int set_facets = get_Instances_Set(FacetClasses, Q, sis_session);
        Q.reset_set(set_facets);
        //int card = Q.set_get_card(set_facets);

        Q.set_intersect(set_super_classes, set_facets);
        Q.reset_set(set_super_classes);

        // FILTER hierarchies depending on user group
        //DBFilters dbf = new DBFilters();
        //set_sub_classes = dbf.FilterHierResults(SessionUserInfo, set_sub_classes, Q, sis_session);

        /*
        if (Parameters.OnlyTopTermsHoldReferenceId) {
            //int card1 = Q.set_get_card(set_sub_classes);

            int set_topterms = Q.get_from_node_by_category(set_super_classes, belongsToHierClass, belongsToHierarchyLink);

            //int card = Q.set_get_card(set_topterms);
            //String s1 = ""+card1+" " + card2 + " " + card3;
            results.addAll(get_Node_Names_Of_Set_In_SortItems(set_topterms, true, Q, sis_session));
        } else {
            results.addAll(get_Node_Names_Of_Set_In_SortItems(set_super_classes, true, Q, sis_session));
        }*/
        results.addAll(get_Node_Names_Of_Set_In_SortItems(set_super_classes, true, Q, sis_session));
        Q.free_set(set_super_classes);
        Q.free_set(set_facets);
        Q.reset_name_scope();

        Collections.sort(results, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));
        return results;

    }

    ArrayList<String> getFacetHierarchies(UserInfoClass SessionUserInfo, String facet, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);

        //String language = ServletCaller.getServletContext().getInitParameter("LocaleLanguage");
        //String country = ServletCaller.getServletContext().getInitParameter("LocaleCountry");
        //Locale targetLocale = new Locale(language, country);
        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);

        ArrayList<String> results = new ArrayList<String>();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject hierObj = new StringObject(prefix_class.concat(facet));

        Q.reset_name_scope();
        Q.set_current_node(hierObj);
        int set_sub_classes = Q.get_subclasses(0);
        Q.reset_set(set_sub_classes);

        Q.reset_name_scope();
        int set_hiers = get_Instances_Set(HierarchyClasses, Q, sis_session);

        Q.reset_set(set_hiers);
        //int card = Q.set_get_card(set_facets);

        Q.set_intersect(set_sub_classes, set_hiers);
        Q.reset_set(set_sub_classes);

        // FILTER hierarchies depending on user group
        DBFilters dbf = new DBFilters();
        set_sub_classes = dbf.FilterHierResults(SessionUserInfo, set_sub_classes, Q, sis_session);

        results.addAll(get_Node_Names_Of_Set(set_sub_classes, true, Q, sis_session));
        Q.free_set(set_sub_classes);
        Q.free_set(set_hiers);
        Q.reset_name_scope();

        Collections.sort(results, strCompar);
        return results;

    }

    //getFacetHierarchiesInSortItems differs significantly from getFacetHierarchies as
    //it searches for the hierarchy reference Id in the TopTerm
    ArrayList<SortItem> getFacetHierarchiesInSortItems(UserInfoClass SessionUserInfo, String facet, QClass Q, IntegerObject sis_session, Locale targetLocale) {

        ArrayList<SortItem> results = new ArrayList<SortItem>();

        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchyClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchyClasses);

        //String language = ServletCaller.getServletContext().getInitParameter("LocaleLanguage");
        //String country = ServletCaller.getServletContext().getInitParameter("LocaleCountry");
        //Locale targetLocale = new Locale(language, country);
        StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject hierObj = new StringObject(prefix_class.concat(facet));

        StringObject belongsToHierClass = new StringObject();
        StringObject belongsToHierarchyLink = new StringObject();
        getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierClass, belongsToHierarchyLink, Q, sis_session);

        Q.reset_name_scope();
        Q.set_current_node(hierObj);
        int set_sub_classes = Q.get_subclasses(0);
        Q.reset_set(set_sub_classes);

        Q.reset_name_scope();
        int set_hiers = get_Instances_Set(HierarchyClasses, Q, sis_session);
        Q.reset_set(set_hiers);
        //int card = Q.set_get_card(set_facets);

        Q.set_intersect(set_sub_classes, set_hiers);
        Q.reset_set(set_sub_classes);

        // FILTER hierarchies depending on user group
        DBFilters dbf = new DBFilters();
        set_sub_classes = dbf.FilterHierResults(SessionUserInfo, set_sub_classes, Q, sis_session);

        if (Parameters.OnlyTopTermsHoldReferenceId) {
            //int card1 = Q.set_get_card(set_sub_classes);

            int set_topterms = Q.get_from_node_by_category(set_sub_classes, belongsToHierClass, belongsToHierarchyLink);

            //int card = Q.set_get_card(set_topterms);
            //String s1 = ""+card1+" " + card2 + " " + card3;
            results.addAll(get_Node_Names_Of_Set_In_SortItems(set_topterms, true, Q, sis_session));
        } else {
            results.addAll(get_Node_Names_Of_Set_In_SortItems(set_sub_classes, true, Q, sis_session));
        }
        Q.free_set(set_sub_classes);
        Q.free_set(set_hiers);
        Q.reset_name_scope();

        Collections.sort(results, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));
        return results;

    }

    //Nodes without dewey are read first. Such nodes should not exist though
    public void collectResultsTaxonomicCodes(String selectedThesaurus, QClass Q, IntegerObject sis_session, int targetSet, ArrayList<TaxonomicCodeItem> descriptors, String separator) {

        StringObject from = new StringObject();
        StringObject link = new StringObject();
        getKeywordPair(selectedThesaurus, ConstantParameters.tc_kwd, from, link, Q, sis_session);

        Q.reset_name_scope();
        int deweySetLabels = Q.get_link_from_by_category(targetSet, from, link);
        Q.reset_set(deweySetLabels);

        int deweyNodesSet = Q.get_from_value(deweySetLabels);
        Q.reset_set(deweyNodesSet);

        int noDeweySet = Q.set_get_new();
        Q.set_copy(noDeweySet, targetSet);
        Q.reset_set(noDeweySet);

        Q.set_difference(noDeweySet, deweyNodesSet);
        Q.reset_set(noDeweySet);

        //Add every node in noDeweySet to descriptors with code = -1 so that they will be ordered first
        //StringObject label = new StringObject();
        //IntegerObject sysid = new IntegerObject();
        //StringObject sclass = new StringObject();   // dummy
        Q.reset_set(noDeweySet);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(noDeweySet, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                String tempName = row.get_v1_cls_logicalname();
                String transliteration = row.get_v3_cls_transliteration();
                if (transliteration == null || transliteration.length() == 0) {
                    transliteration = Utilities.getTransliterationString(tempName, true);
                }
                descriptors.add(new TaxonomicCodeItem(tempName, transliteration));
            }
        }
        /*while (Q.retur_full_nodes(noDeweySet, sysid, label, sclass) != QClass.APIFail) {

         String tempName = label.getValue();
         descriptors.add(new TaxonomicCodeItem(tempName) );
         }*/
        Q.reset_name_scope();

        //Now add nodes that already have dewey declared --> more than one dewey numbers may have been assigned to one node
        //This node will appear according to ISO 2788 multiple times in systematic display
        Q.reset_set(deweySetLabels);

        //StringObject fromcls = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();
        //CMValue cmv = new CMValue();
        ArrayList<Return_Link_Row> retLVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(deweySetLabels, retLVals) != QClass.APIFail) {
            for (Return_Link_Row row : retLVals) {
                String tempName = row.get_v1_cls();
                String tempCode = removePrefix(row.get_v3_cmv().getString());
                String tempTranslit = row.get_v4_clsTransliteration();
                descriptors.add(new TaxonomicCodeItem(tempCode, tempName, tempTranslit));
            }
        }

        /*while (Q.retur_full_link_id(deweySetLabels, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
            
         String tempName = cls.getValue();
         String tempCode = removePrefix(cmv.getString());
         descriptors.add(new TaxonomicCodeItem(tempCode, tempName) );
         }*/
        Q.free_set(deweySetLabels);
        Q.free_set(noDeweySet);
        Q.free_set(deweyNodesSet);

    }

    public void getDBAdminHierarchiesStatusesAndGuideTermsXML(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session, ArrayList<String> allHierarcies, ArrayList<String> allGuideTerms) {

        Q.reset_name_scope();

        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchiesClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchiesClasses);

        int set_allhiers = get_Instances_Set(HierarchiesClasses, Q, sis_session);
        Q.reset_set(set_allhiers);
        allHierarcies.addAll(get_Node_Names_Of_Set(set_allhiers, true, Q, sis_session));
        Q.free_set(set_allhiers);
        allGuideTerms.addAll(collectGuideLinks(SessionUserInfo.selectedThesaurus, Q, sis_session));

    }
    
    public void getDBAdminHierarchiesStatusesAndGuideTermsWithStatisticsXML(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session, ArrayList<String> allHierarcies, HashMap<String,Integer> allGuideTerms) {

        Q.reset_name_scope();

        int index = Parameters.CLASS_SET.indexOf("HIERARCHY");
        String[] HierarchiesClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
        SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(HierarchiesClasses);

        int set_allhiers = get_Instances_Set(HierarchiesClasses, Q, sis_session);
        Q.reset_set(set_allhiers);
        allHierarcies.addAll(get_Node_Names_Of_Set(set_allhiers, true, Q, sis_session));
        Q.free_set(set_allhiers);
        allGuideTerms.putAll(collectGuideLinksWithUsage(SessionUserInfo.selectedThesaurus, Q, sis_session));        

    }

    public ArrayList<String> getAllSearchSources(UserInfoClass SessionUserInfo, String[] input, String[] operators, String[] inputValues, String globalOperator, QClass Q,
            TMSAPIClass TA, IntegerObject sis_session) {
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        int set_global_source_results = -1;
        String prefixSource = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
        String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        StringObject SourceClassObj = new StringObject(ConstantParameters.SourceClass);

        ArrayList<String> globalSourceResults = new ArrayList<>();

        Q.reset_name_scope();
        Q.set_current_node(SourceClassObj);
        int set_s = Q.get_all_instances(0);
        Q.reset_set(set_s);

        if (globalOperator.equals("*") || globalOperator.equals("all")) {
            set_global_source_results = Q.set_get_new();
            Q.set_copy(set_global_source_results, set_s);
            Q.reset_set(set_global_source_results);
        } else {
            for (int i = 0; i < input.length; i++) {

                Q.reset_set(set_s);
                int set_partial_source_results = Q.set_get_new();

                String searchVal = inputValues[i];

                Q.reset_name_scope();

                
                if (input[i].equalsIgnoreCase("name")) {

                    if (operators[i].equals(ConstantParameters.searchOperatorEquals)) {

                        if (searchVal != null && searchVal.trim().length() > 0) {
                            if (Q.set_current_node(new StringObject(prefixSource.concat(searchVal))) != QClass.APIFail) {

                                Q.set_put(set_partial_source_results);
                                Q.reset_set(set_partial_source_results);
                            }
                        }

                    } else if (operators[i].equals(ConstantParameters.searchOperatorContains)) {
                        
                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm(ptrn_set, prm_val);
                        Q.reset_set(set_s);
                        //Q.reset_set(ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_partial_hierarchy_results = Q.get_matched_case_insensitive( set_h, ptrn_set,1);
                        Q.free_set(set_partial_source_results);

                        //set_partial_source_results = Q.get_matched(set_s, ptrn_set);
                        //set_partial_source_results = Q.get_matched_ToneAndCaseInsensitive(set_s, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                        if (Parameters.SEARCH_MODE_CASE_INSENSITIVE) {
                            set_partial_source_results = Q.get_matched_CaseInsensitive(set_s, searchVal, true);
                        } else {
                            set_partial_source_results = Q.get_matched_ToneAndCaseInsensitive(set_s, searchVal, false);
                        }
                        Q.reset_set(set_partial_source_results);
                        //Q.free_set(ptrn_set);
                        
                    } else if (operators[i].equals(ConstantParameters.searchOperatorTransliterationContains)) {
                        
                        Q.reset_set(set_s);
                        Q.free_set(set_partial_source_results);

                        set_partial_source_results = TA.get_matched_OnTransliteration(set_s, Utilities.getTransliterationString(searchVal, false), false);

                        Q.reset_set(set_partial_source_results);
                        //Q.free_set(ptrn_set);
                        

                    } else if (operators[i].equals("!")) {
                        
                        int set_exclude_sources = Q.set_get_new();

                        if (Q.set_current_node(new StringObject(prefixSource.concat(searchVal))) != QClass.APIFail) {

                            Q.set_put(set_exclude_sources);
                            Q.reset_set(set_exclude_sources);
                        }

                        Q.reset_set(set_s);
                        Q.set_copy(set_partial_source_results, set_s);

                        Q.reset_set(set_partial_source_results);
                        Q.reset_set(set_exclude_sources);
                        Q.set_difference(set_partial_source_results, set_exclude_sources);
                        Q.reset_set(set_partial_source_results);
                        Q.free_set(set_exclude_sources);
                        
                    } else if (operators[i].equals(ConstantParameters.searchOperatorNotContains)) {
                        
                        //int set_exclude_sources = Q.set_get_new();
                        //CMValue prm_val = new CMValue();
                        //prm_val.assign_string(searchVal);
                        //int ptrn_set = Q.set_get_new();
                        //Q.set_put_prm(ptrn_set, prm_val);
                        Q.reset_set(set_s);
                        //Q.reset_set(ptrn_set);
                        //Decided Not case insensitive logo problimatow me ta tonoumena
                        //set_exclude_hierarchies = Q.get_matched_case_insensitive( set_h, ptrn_set,1);
                        //int set_exclude_sources = Q.get_matched(set_s, ptrn_set);
                        int set_exclude_sources = -1;//Q.get_matched_ToneAndCaseInsensitive(set_s, searchVal, Parameters.SEARCH_MODE_CASE_TONE_INSENSITIVE);
                        if (Parameters.SEARCH_MODE_CASE_INSENSITIVE) {
                            set_exclude_sources = Q.get_matched_CaseInsensitive(set_s, searchVal, true);
                        } else {
                            set_exclude_sources = Q.get_matched_ToneAndCaseInsensitive(set_s, searchVal, false);
                        }

                        Q.reset_set(set_exclude_sources);
                        //Q.free_set(ptrn_set);

                        Q.reset_set(set_s);
                        Q.reset_set(set_partial_source_results);
                        Q.set_copy(set_partial_source_results, set_s);

                        Q.reset_set(set_partial_source_results);
                        Q.reset_set(set_exclude_sources);
                        Q.set_difference(set_partial_source_results, set_exclude_sources);
                        Q.reset_set(set_partial_source_results);
                        Q.free_set(set_exclude_sources);
                        
                    }                    
                    else if (operators[i].equals(ConstantParameters.searchOperatorNotTransliterationContains)) {
                        
                        
                        Q.reset_set(set_s);
                        
                        int set_exclude_sources = TA.get_matched_OnTransliteration(set_s, Utilities.getTransliterationString(searchVal,false),false);
                        Q.reset_set(set_exclude_sources);
                        //Q.free_set(ptrn_set);

                        Q.reset_set(set_s);
                        Q.reset_set(set_partial_source_results);
                        Q.set_copy(set_partial_source_results, set_s);

                        Q.reset_set(set_partial_source_results);
                        Q.reset_set(set_exclude_sources);
                        Q.set_difference(set_partial_source_results, set_exclude_sources);
                        Q.reset_set(set_partial_source_results);
                        Q.free_set(set_exclude_sources);
                        
                    }                    
                }
                

                
                if (input[i].equalsIgnoreCase(ConstantParameters.primary_found_in_kwd) || input[i].equalsIgnoreCase(ConstantParameters.translations_found_in_kwd)) {
                    String[] term_field = {"name"};
                    String[] term_operator = new String[1];
                    term_operator[0] = operators[i];
                    String[] term_inputValue = new String[1];
                    term_inputValue[0] = searchVal;
                    int descriptor_results_set = getSearchTermResultSet(SessionUserInfo, term_field, term_operator, term_inputValue, globalOperator, Q, TA, sis_session);
                    Q.reset_set(descriptor_results_set);
                    Q.reset_name_scope();

                    StringObject fromClassObj = new StringObject();
                    StringObject linkObj = new StringObject();
                    getKeywordPair(SessionUserInfo.selectedThesaurus, input[i], fromClassObj, linkObj, Q, sis_session);

                    int set_source_labels = Q.get_link_from_by_category(descriptor_results_set, fromClassObj, linkObj);
                    Q.reset_set(set_source_labels);

                    Q.free_set(set_partial_source_results);
                    Q.reset_name_scope();
                    set_partial_source_results = Q.get_to_value(set_source_labels);
                    Q.reset_set(set_partial_source_results);

                    Q.free_set(descriptor_results_set);

                    Q.set_intersect(set_partial_source_results, set_s);
                    Q.reset_set(set_partial_source_results);

                }
                

                
                if (input[i].equalsIgnoreCase(ConstantParameters.source_note_kwd)) {
                    //SourceClassObj
                    StringObject sourceNoteLinkObj = new StringObject(ConstantParameters.source_note_kwd);
                    Q.set_current_node(SourceClassObj);
                    Q.set_current_node(sourceNoteLinkObj);

                    int set_all_source_note_nodes = Q.get_all_instances(0);
                    Q.reset_set(set_all_source_note_nodes);

                    int set_sources_with_source_note = Q.get_from_value(set_all_source_note_nodes);
                    Q.reset_set(set_sources_with_source_note);

                    if (operators[i].equals(ConstantParameters.searchOperatorEquals) && searchVal.equals("")) {
                        // optimization of special search cases
                        // 1. sources without source_note

                        Q.set_difference(set_all_source_note_nodes, set_sources_with_source_note);

                        Q.reset_set(set_partial_source_results);
                        Q.reset_set(set_all_source_note_nodes);
                        Q.set_copy(set_partial_source_results, set_all_source_note_nodes);

                        Q.reset_set(set_partial_source_results);
                        Q.free_set(set_sources_with_source_note);
                        Q.free_set(set_all_source_note_nodes);

                    } else if (operators[i].equals("!") && searchVal.equals("")) {
                        // optimization of special search cases
                        // 2. sources with source_note
                        Q.reset_set(set_partial_source_results);
                        Q.reset_set(set_sources_with_source_note);
                        Q.set_copy(set_partial_source_results, set_sources_with_source_note);

                        Q.reset_set(set_partial_source_results);
                        Q.free_set(set_sources_with_source_note);
                        Q.free_set(set_all_source_note_nodes);
                    } else {

                        //special handling for searching modes: ~ and !~
                        int SearchMode = -1;
                        if (operators[i].equals(ConstantParameters.searchOperatorContains) && searchVal.startsWith("*") == false && searchVal.endsWith("*") == false) { // contains
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_CONTAINS;
                        } else if (operators[i].equals(ConstantParameters.searchOperatorContains) && searchVal.endsWith("*")) { // starts with
                            // remove special character "*"
                            searchVal = searchVal.substring(0, searchVal.length() - 1);
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_STARTS_WITH;
                        } else if (operators[i].equals(ConstantParameters.searchOperatorContains) && searchVal.startsWith("*")) { // ends with
                            // remove special character "*"
                            searchVal = searchVal.substring(1);
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_ENDS_WITH;
                        } else if (operators[i].equals(ConstantParameters.searchOperatorNotContains) && searchVal.startsWith("*") == false && searchVal.endsWith("*") == false) { // contains
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_CONTAINS;
                        } else if (operators[i].equals(ConstantParameters.searchOperatorNotContains) && searchVal.endsWith("*")) { // not starts with
                            // remove special character "*"
                            searchVal = searchVal.substring(0, searchVal.length() - 1);
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_STARTS_WITH;
                        } else if (operators[i].equals(ConstantParameters.searchOperatorNotContains) && searchVal.startsWith("*")) { // not ends with
                            // remove special character "*"
                            searchVal = searchVal.substring(1);
                            SearchMode = ConstantParameters.SEARCH_COMMENTS_MODE_NOT_ENDS_WITH;
                        }

                        // collect all sources with source_note in a Vector
                        ArrayList<String> Search_in_sources_Vec = new ArrayList<String>();
                        Search_in_sources_Vec.addAll(get_Node_Names_Of_Set(set_sources_with_source_note, false, Q, sis_session));

                        Q.free_set(set_all_source_note_nodes);
                        Q.free_set(set_sources_with_source_note);

                        for (int k = 0; k < Search_in_sources_Vec.size(); k++) {

                            //Get Source note in a string
                            StringObject sourceObj = new StringObject(Search_in_sources_Vec.get(k));
                            StringObject sourceNoteObj = new StringObject("");

                            StringObject prevThes = new StringObject();
                            TA.GetThesaurusNameWithoutPrefix(prevThes);
                            if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
                            }

                            TA.GetDescriptorComment(sourceObj, sourceNoteObj,
                                    new StringObject("Source"), new StringObject(ConstantParameters.source_note_kwd));

                            //reset to previous thesaurus name if needed
                            if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                                TA.SetThesaurusName(prevThes.getValue());
                            }
                            String source_noteStr = sourceNoteObj.getValue();

                            // check the current source_note depending on the search operator
                            boolean sourceBelongsToResults = false;
                            if (operators[i].equals(ConstantParameters.searchOperatorEquals)) { // equals with 
                                if (source_noteStr.equals(searchVal)) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_CONTAINS) { // contains (g.e. οικονομία)
                                if (source_noteStr.indexOf(searchVal) != -1) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_STARTS_WITH) { // starts with (g.e. The*)
                                if (source_noteStr.startsWith(searchVal)) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_ENDS_WITH) { // ends with (g.e. *περιβάλλον)
                                if (source_noteStr.endsWith(searchVal)) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (operators[i].equals("!")) { // not equals with (g.e. Πνεύμονες πρασίνου μεσαίου μεγέθους)
                                if (source_noteStr.equals(searchVal) == false) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_CONTAINS) { // not contains (g.e. οικονομία)
                                if (source_noteStr.indexOf(searchVal) == -1) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_STARTS_WITH) { // not starts with (g.e. The*)
                                if (source_noteStr.startsWith(searchVal) == false) {
                                    sourceBelongsToResults = true;
                                }
                            } else if (SearchMode == ConstantParameters.SEARCH_COMMENTS_MODE_NOT_ENDS_WITH) { // not ends with (g.e. *περιβάλλον)
                                if (source_noteStr.endsWith(searchVal) == false) {
                                    sourceBelongsToResults = true;
                                }
                            }

                            // inform results in case the current source_note satisfies the search criterion
                            if (sourceBelongsToResults == true) {
                                Q.reset_name_scope();
                                Q.set_current_node(sourceObj);
                                Q.set_put(set_partial_source_results);
                                Q.reset_set(set_partial_source_results);
                            }
                        }
                    }
                }
                

                //merge results of each loop. All first loop's results are included
                if (i == 0) {

                    set_global_source_results = Q.set_get_new();
                    Q.reset_set(set_partial_source_results);
                    Q.set_copy(set_global_source_results, set_partial_source_results);
                    Q.reset_set(set_global_source_results);
                    continue;
                }
                //If conjuction operator == AND then set_intersect
                if (globalOperator.equalsIgnoreCase("AND")) {

                    Q.reset_set(set_global_source_results);
                    Q.reset_set(set_partial_source_results);

                    Q.set_intersect(set_global_source_results, set_partial_source_results);
                    Q.reset_set(set_global_source_results);
                    continue;
                }
                //If conjuction operator == OR then set_union
                if (globalOperator.equalsIgnoreCase("OR")) {

                    Q.reset_set(set_global_source_results);
                    Q.reset_set(set_partial_source_results);

                    Q.set_union(set_global_source_results, set_partial_source_results);
                    Q.reset_set(set_global_source_results);
                    continue;
                }

            }
        }

        Q.reset_set(set_global_source_results);

        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if (Q.bulk_return_nodes(set_global_source_results, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                globalSourceResults.add(row.get_v1_cls_logicalname());
            }
        }
        //StringObject c_name = new StringObject();
        /*while (Q.retur_nodes(set_global_source_results, c_name) != QClass.APIFail) {
         globalSourceResults.add(c_name.getValue());
         }*/

        globalSourceResults = removePrefix(globalSourceResults);
        Collections.sort(globalSourceResults);

        Q.free_all_sets();

        return globalSourceResults;
    }

    public ArrayList<String> returnResults_Source(UserInfoClass SessionUserInfo, String targetSource, String output, QClass Q, TMSAPIClass TA, IntegerObject sis_session) {
        //term is in ui encoding and without prefix
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+term + " " +output );      
        if (output.equals(ConstantParameters.primary_found_in_kwd)) {
            return getGT_or_ET_SourceTerms(SessionUserInfo, targetSource, ConstantParameters.primary_found_in_kwd, Q, sis_session);
        } else if (output.equals(ConstantParameters.translations_found_in_kwd)) {
            return getGT_or_ET_SourceTerms(SessionUserInfo, targetSource, ConstantParameters.translations_found_in_kwd, Q, sis_session);
        } else if (output.equals(ConstantParameters.source_note_kwd)) {

            //Q.reset_name_scope();
            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            String prefix_source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
            StringObject sourceObj = new StringObject(prefix_source.concat(targetSource));
            StringObject sourceNoteObj = new StringObject("");
            StringObject prevThes = new StringObject();

            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }

            TA.GetDescriptorComment(sourceObj, sourceNoteObj, new StringObject("Source"), new StringObject(ConstantParameters.source_note_kwd));
            //reset to previous thesaurus name if needed
            if (prevThes.getValue().equals(SessionUserInfo.selectedThesaurus) == false) {
                TA.SetThesaurusName(prevThes.getValue());
            }
            ArrayList<String> returnVec = new ArrayList<String>();
            returnVec.add(sourceNoteObj.getValue());
            return returnVec;
        } else {

            ArrayList<String> noSupportedKwd = new ArrayList<String>();
            if (output.compareTo("name") != 0) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "returnResults_Source() called with output keyword " + output + ". No search function supported for this keyword");
            }
            return noSupportedKwd;
        }

    }

    public ArrayList<String> getGT_or_ET_SourceTerms(UserInfoClass SessionUserInfo, String targetSource, String mode_kwd, QClass Q, IntegerObject sis_session) {

        ArrayList<String> termsWithThisSource = new ArrayList<String>();

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        String prefix_source = dbtr.getThesaurusPrefix_Source(Q, sis_session.getValue());
        StringObject sourceObj = new StringObject(prefix_source.concat(targetSource));
        StringObject fromClassObj = new StringObject();
        StringObject LinkObj = new StringObject();

        getKeywordPair(SessionUserInfo.selectedThesaurus, mode_kwd, fromClassObj, LinkObj, Q, sis_session);
        Q.reset_name_scope();
        if (Q.set_current_node(sourceObj) == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to reference source: " + targetSource);
            return termsWithThisSource;
        }

        int set_references_labels = Q.get_link_to_by_category(0, fromClassObj, LinkObj);
        Q.reset_set(set_references_labels);

        int set_referenced_terms = Q.get_from_value(set_references_labels);
        Q.reset_set(set_referenced_terms);
        Q.free_set(set_references_labels);

        // FILTER DB results depending on user group
        DBFilters dbf = new DBFilters();
        set_referenced_terms = dbf.FilterTermsResults(SessionUserInfo, set_referenced_terms, Q, sis_session);

        termsWithThisSource.addAll(get_Node_Names_Of_Set(set_referenced_terms, true, Q, sis_session));
        Q.free_set(set_referenced_terms);

        return termsWithThisSource;
    }

    public void applyKeywordMappings(String selectedThesaurus, QClass Q, IntegerObject sis_session, String[] output, HashMap<String, String> kewyWordsMappings) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject categoryStr = new StringObject();

        for (int i = 0; i < output.length; i++) {
            String targetOutput = output[i];

            if (targetOutput.compareTo(ConstantParameters.bt_kwd) == 0) {
                dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.bt_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.rt_kwd) == 0) {
                dbtr.getThesaurusCategory_RT(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.rt_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.translation_kwd) == 0) {

                StringObject thesTermObj = new StringObject();
                dbtr.getThesaurusClass_Term(Q, sis_session.getValue(), new StringObject(selectedThesaurus), thesTermObj);
                dbtr.getThesaurusCategory_translation(Q, sis_session.getValue(), new StringObject(selectedThesaurus), categoryStr);

                Q.reset_name_scope();
                Q.set_current_node(thesTermObj);
                Q.set_current_node(categoryStr);

                int set_thes_translation_categories = Q.get_subclasses(0);
                Q.reset_set(set_thes_translation_categories);

                ArrayList<String> trCats = get_Node_Names_Of_Set(set_thes_translation_categories, false, Q, sis_session);
                Q.free_set(set_thes_translation_categories);

                for (int k = 0; k < trCats.size(); k++) {
                    kewyWordsMappings.put(trCats.get(k), ConstantParameters.translation_kwd);
                }

            } else if (targetOutput.compareTo(ConstantParameters.tc_kwd) == 0) {

                dbtr.getThesaurusCategory_taxonomic_code(selectedThesaurus, categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.tc_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.uf_kwd) == 0) {
                dbtr.getThesaurusCategory_UF(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.uf_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.uf_translations_kwd) == 0) {

                StringObject thesTermObj = new StringObject();
                dbtr.getThesaurusClass_HierarchyTerm(Q, sis_session.getValue(), new StringObject(selectedThesaurus), thesTermObj);
                dbtr.getThesaurusCategory_uf_translation(Q, sis_session.getValue(), new StringObject(selectedThesaurus), categoryStr);

                Q.reset_name_scope();
                Q.set_current_node(thesTermObj);
                Q.set_current_node(categoryStr);

                int set_thes_uf_translation_categories = Q.get_subclasses(0);
                Q.reset_set(set_thes_uf_translation_categories);

                ArrayList<String> ufTrCats = get_Node_Names_Of_Set(set_thes_uf_translation_categories, false, Q, sis_session);
                Q.free_set(set_thes_uf_translation_categories);

                for (int k = 0; k < ufTrCats.size(); k++) {
                    kewyWordsMappings.put(ufTrCats.get(k), ConstantParameters.uf_translations_kwd);
                }

            } else if (targetOutput.compareTo(ConstantParameters.scope_note_kwd) == 0) {
                dbtr.getThesaurusCategory_scope_note(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.scope_note_kwd);

            } else if (targetOutput.compareTo(ConstantParameters.translations_scope_note_kwd) == 0) {
                dbtr.getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.translations_scope_note_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.historical_note_kwd) == 0) {
                dbtr.getThesaurusCategory_historical_note(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.historical_note_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.comment_kwd) == 0) {
                dbtr.getThesaurusCategory_comment(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.comment_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.note_kwd) == 0) {
                dbtr.getThesaurusCategory_note(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.note_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.created_by_kwd) == 0) {
                dbtr.getThesaurusCategory_created_by(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.created_by_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.created_on_kwd) == 0) {
                dbtr.getThesaurusCategory_created(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.created_on_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.modified_by_kwd) == 0) {
                dbtr.getThesaurusCategory_modified_by(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.modified_by_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.modified_on_kwd) == 0) {
                dbtr.getThesaurusCategory_modified(selectedThesaurus, Q, sis_session.getValue(), categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.modified_on_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.translations_found_in_kwd) == 0) {
                dbtr.getThesaurusCategory_translations_found_in(selectedThesaurus, categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.translations_found_in_kwd);
            } else if (targetOutput.compareTo(ConstantParameters.primary_found_in_kwd) == 0) {
                dbtr.getThesaurusCategory_primary_found_in(selectedThesaurus, categoryStr);
                kewyWordsMappings.put(categoryStr.getValue(), ConstantParameters.primary_found_in_kwd);
            } else {
                kewyWordsMappings.put(targetOutput, targetOutput);
            }

        }
    }

    //used for alphabetical display
    public void collectUsedForTermSetInfo(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session, int set_results,
            HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<String> allTerms, ArrayList<Long> resultNodesIdsL) {
        //must get all ufs that are linked to the result set and get all their nodes back to terms of current thes.
        DBFilters dbF = new DBFilters();

        Q.reset_name_scope();
        StringObject UFFromClassObj = new StringObject();
        StringObject UFLinkClassObj = new StringObject();
        getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_kwd, UFFromClassObj, UFLinkClassObj, Q, sis_session);

        Q.reset_name_scope();

        Q.reset_set(set_results);
        int set_uf_links = Q.get_link_from_by_category(set_results, UFFromClassObj, UFLinkClassObj);
        Q.reset_set(set_uf_links);

        int set_uf_nodes = Q.get_to_value(set_uf_links);
        Q.reset_set(set_uf_nodes);

        int set_uf_nodes_refs = Q.get_link_to_by_category(set_uf_nodes, UFFromClassObj, UFLinkClassObj);
        Q.reset_set(set_uf_nodes_refs);

        dbF.FilterTermsResultsLinks(SessionUserInfo, -1, set_uf_nodes_refs, Q, sis_session);
        //StringObject fromcls = new StringObject();
        //StringObject label = new StringObject();
        //StringObject categ = new StringObject();
        //StringObject cls = new StringObject();
        //IntegerObject uniq_categ = new IntegerObject();
        //CMValue cmv = new CMValue();
        //IntegerObject clsID = new IntegerObject();
        //IntegerObject linkID = new IntegerObject();
        //IntegerObject categID = new IntegerObject();

        ArrayList<Return_Full_Link_Id_Row> retFLIVals = new ArrayList<Return_Full_Link_Id_Row>();
        if (Q.bulk_return_full_link_id(set_uf_nodes_refs, retFLIVals) != QClass.APIFail) {
            for (Return_Full_Link_Id_Row row : retFLIVals) {

                String targetTerm = removePrefix(row.get_v1_cls());
                String UFvalue = removePrefix(row.get_v8_cmv().getString());
                long targetTermIdL = row.get_v2_clsid();
                long UFvalueIdL = row.get_v8_cmv().getSysid();

                SortItem targetTermSortItem = new SortItem(targetTerm, targetTermIdL);

                if (resultNodesIdsL.contains(UFvalueIdL) == false) {
                    resultNodesIdsL.add(UFvalueIdL);
                }
                if (termsInfo.containsKey(UFvalue) == false) {
                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_UF, null);
                    newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + UFvalueIdL, UFvalueIdL));
                    termsInfo.put(UFvalue, newContainer);
                    allTerms.add(UFvalue);
                }

                termsInfo.get(UFvalue).descriptorInfo.get("use").add(targetTermSortItem);
            }
        }
        /*
         while (Q.retur_full_link_id(set_uf_nodes_refs, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
        
         String targetTerm   = removePrefix(cls.getValue());
         String UFvalue      = removePrefix((cmv.getString()));
         int targetTermId = clsID.getValue();
         int  UFvalueId = cmv.getSysid();
            
         SortItem targetTermSortItem = new SortItem(targetTerm,targetTermId);
            
         if (resultNodesIds.contains(UFvalueId) == false) {
         resultNodesIds.add(UFvalueId);
         }
         if (termsInfo.containsKey(UFvalue) == false) {
         NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_UF, null);
         newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + UFvalueId, UFvalueId));
         termsInfo.put(UFvalue, newContainer);
         allTerms.add(UFvalue);
         }

         termsInfo.get(UFvalue).descriptorInfo.get("use").add(targetTermSortItem);
            
         }
         */

        Q.free_set(set_uf_nodes_refs);
        Q.free_set(set_uf_nodes);
        Q.free_set(set_uf_links);

    }

    public void constructReferenceURIs(String prefix, HashMap<String, NodeInfoSortItemContainer> termsInfo){
        
        if(termsInfo!=null){
            
            Utilities u = new Utilities();
            HashMap<String, Long> collectIdsInfo = new HashMap<String,Long>();
            
            for (Map.Entry<String, NodeInfoSortItemContainer> entry : termsInfo.entrySet()) {
                String termName = entry.getKey();
                long referenceId = entry.getValue().descriptorInfo.get(ConstantParameters.id_kwd).get(0).getThesaurusReferenceId();
                collectIdsInfo.put(termName, referenceId);
            }
            for (Map.Entry<String, Long> entry : collectIdsInfo.entrySet()) {
                String termName = entry.getKey();
                long referenceId = entry.getValue();
                //termsInfo.get(termName).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(new SortItem(XMLHandling.WriteFileData.getSkosUri(false, prefix, referenceId)));
                termsInfo.get(termName).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(new SortItem(prefix+u.getExternalReaderReferenceUriSuffix(false, referenceId)));
            }
        }
    }
    
    public void collectTermSetInfo(UserInfoClass SessionUserInfo,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            int set_results,            
            ArrayList<String> output,
            HashMap<String, NodeInfoSortItemContainer> termsInfo,
            ArrayList<String> allTerms,
            ArrayList<Long> resultNodesIds,
            boolean restrictOutputInSearchResultsOnly, 
            /*boolean restrictExpansionToApprovedTermsOnly, */
            ArrayList<String> completeSetNames
    ) throws IOException {
        DBexportData dbExport = new DBexportData();

        
        boolean ntOutputSelected = output.contains(ConstantParameters.nt_kwd);
        boolean btOutputSelected = output.contains(ConstantParameters.bt_kwd);
        if (ntOutputSelected && !btOutputSelected) {
            output.add(ConstantParameters.bt_kwd);
        }
        if (btOutputSelected && !ntOutputSelected) {
            output.add(ConstantParameters.nt_kwd);
        }
        
        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        
        ArrayList<String> resultTermNamesWithPrefixes = new ArrayList<>();
        Q.reset_set(set_results);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<>();
        if (Q.bulk_return_nodes(set_results, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {

                long targetTermIdL = row.get_Neo4j_NodeId();
                if(!resultNodesIds.contains(targetTermIdL)){
                    resultNodesIds.add(targetTermIdL);
                }
                

                String targetTerm = removePrefix(row.get_v1_cls_logicalname());
                resultTermNamesWithPrefixes.add(row.get_v1_cls_logicalname());

                long referenceId = row.get_v2_long_referenceId();
                String transliteration = row.get_v3_cls_transliteration();
                if (termsInfo.containsKey(targetTerm) == false) {
                    NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                    newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + targetTermIdL, targetTermIdL, transliteration, referenceId));
                    //newContainer.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).add(new SortItem("" + targetTermIdL, targetTermIdL,transliteration,referenceId));                         
                    
                    termsInfo.put(targetTerm, newContainer);
                    allTerms.add(targetTerm);
                }
            }
        }
        /*
        while(Q.retur_full_nodes(set_results, resultIdObj, resultNodeObj, resultClassObj)!=QClass.APIFail){
         int targetTermId = resultIdObj.getValue();
         resultNodesIds.add(targetTermId);
         String targetTerm = removePrefix(resultNodeObj.getValue());

         if (termsInfo.containsKey(targetTerm) == false) {
         NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
         newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem(""+targetTermId,targetTermId));
         termsInfo.put(targetTerm, newContainer);
         allTerms.add(targetTerm);
         }
         }*/

        int set_from_links = Q.get_link_from(set_results);
        Q.reset_set(set_from_links);

        int set_to_links = Q.get_link_to(set_results);
        Q.reset_set(set_to_links);

        if(!restrictOutputInSearchResultsOnly){
            Q.set_difference(set_to_links, set_from_links);
            Q.reset_set(set_to_links);
        }

        DBFilters dbF = new DBFilters();
        dbF.FilterTermsResultsLinks(SessionUserInfo, set_from_links, set_to_links, Q, sis_session);

        collectTermSetInfoFrom(SessionUserInfo.selectedThesaurus, Q, sis_session, set_from_links, output, termsInfo, allTerms, resultNodesIds,restrictOutputInSearchResultsOnly, completeSetNames);
        collectTermSetInfoTo(SessionUserInfo.selectedThesaurus, Q, sis_session, set_to_links, output, termsInfo, allTerms, resultNodesIds,restrictOutputInSearchResultsOnly, completeSetNames);

        if (output.contains(ConstantParameters.rnt_kwd)) {

            StringObject errorMsg = new StringObject("");

            for (int i = 0; i < resultTermNamesWithPrefixes.size(); i++) {
                String targetTerm = resultTermNamesWithPrefixes.get(i);
                String termNameWithoutPrefix = removePrefix(targetTerm);

                int set_recursive_nts = Q.set_get_new();
                this.collect_Recurcively_ALL_NTs(SessionUserInfo.selectedThesaurus, new StringObject(targetTerm), set_recursive_nts, errorMsg, false, Q, sis_session,SessionUserInfo.UILang);
                if (Q.set_get_card(set_recursive_nts) > 0) {

                    retVals.clear();
                    ArrayList<SortItem> recNts = new ArrayList<>();
                    if (Q.bulk_return_nodes(set_recursive_nts, retVals) != QClass.APIFail) {
                        for (Return_Nodes_Row row : retVals) {

                            long targetTermIdL = row.get_Neo4j_NodeId();
                            long refId = row.get_v2_long_referenceId();
                            String transliteration = row.get_v3_cls_transliteration();
                            recNts.add(new SortItem(removePrefix(row.get_v1_cls_logicalname()), targetTermIdL, "", transliteration, refId));
                        }
                    }

                    termsInfo.get(termNameWithoutPrefix).descriptorInfo.get(ConstantParameters.rnt_kwd).addAll(recNts);
                }
            }
        }
        if (output.contains(ConstantParameters.rbt_kwd)) {

            StringObject errorMsg = new StringObject("");

            for (int i = 0; i < resultTermNamesWithPrefixes.size(); i++) {
                String targetTerm = resultTermNamesWithPrefixes.get(i);
                String termNameWithoutPrefix = removePrefix(targetTerm);

                int set_recursive_nts = Q.set_get_new();
                this.collect_Recurcively_ALL_BTs(SessionUserInfo.selectedThesaurus, new StringObject(targetTerm), set_recursive_nts, errorMsg, false, Q, sis_session, SessionUserInfo.UILang);
                if (Q.set_get_card(set_recursive_nts) > 0) {

                    retVals.clear();
                    ArrayList<SortItem> recBts = new ArrayList<>();
                    if (Q.bulk_return_nodes(set_recursive_nts, retVals) != QClass.APIFail) {
                        for (Return_Nodes_Row row : retVals) {

                            long targetTermIdL = row.get_Neo4j_NodeId();
                            long refId = row.get_v2_long_referenceId();
                            String transliteration = row.get_v3_cls_transliteration();

                            recBts.add(new SortItem(removePrefix(row.get_v1_cls_logicalname()), targetTermIdL, "", transliteration, refId));
                        }
                    }

                    termsInfo.get(termNameWithoutPrefix).descriptorInfo.get(ConstantParameters.rbt_kwd).addAll(recBts);
                }
            }

        }
        dbExport.ReadTermStatuses(SessionUserInfo.selectedThesaurus, Q, sis_session, output, allTerms, termsInfo, resultNodesIds);
        dbExport.ReadTermCommentCategories(SessionUserInfo.selectedThesaurus, Q, TA, sis_session, output, allTerms, termsInfo, resultNodesIds);
        dbExport.ReadTermFacetAndHierarchiesInSortItems(SessionUserInfo, Q, sis_session, set_results, output, allTerms, termsInfo, resultNodesIds);

        //one safe option is to add extendSearcResultsWithRnts cotnrol here 
        
        Q.free_set(set_to_links);
        Q.free_set(set_from_links);

        if (ntOutputSelected && !btOutputSelected) {
            output.remove(ConstantParameters.bt_kwd);
        }
        if (btOutputSelected && !ntOutputSelected) {
            output.remove(ConstantParameters.nt_kwd);
        }
    }

    private void collectTermSetInfoFrom(String selectedThesaurus, QClass Q, IntegerObject sis_session, int set_from_links,
            ArrayList<String> output, HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<String> allTerms, ArrayList<Long> resultNodesIdsL,
            boolean restrictOutputInSearchResultsOnly, ArrayList<String> completeSetNames) {
        //step 1 collect all data around terms except status and scope notes

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        boolean readXrs = false;
        if (output.contains(ConstantParameters.uf_kwd) && output.contains("use")) {
            readXrs = true;
        }

        StringObject BTLinkObj = new StringObject();
        StringObject RTLinkObj = new StringObject();
        HashMap<String, String> keyWordsMappings = new HashMap<String, String>();

        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        applyKeywordMappings(selectedThesaurus, Q, sis_session, outputTable, keyWordsMappings);
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);
        dbtr.getThesaurusCategory_RT(selectedThesaurus, Q, sis_session.getValue(), RTLinkObj);

        /*
         StringObject fromcls = new StringObject();
         StringObject label = new StringObject();
         StringObject categ = new StringObject();
         StringObject cls = new StringObject();
         IntegerObject uniq_categ = new IntegerObject();
         CMValue cmv = new CMValue();
         IntegerObject clsID = new IntegerObject();
         IntegerObject linkID = new IntegerObject();
         IntegerObject categID = new IntegerObject();
         */
        
        ArrayList<String> filteringLinks = new ArrayList<>();
        if(restrictOutputInSearchResultsOnly){
            filteringLinks.add(ConstantParameters.nt_kwd);
            filteringLinks.add(ConstantParameters.rt_kwd);
            filteringLinks.add(ConstantParameters.bt_kwd);
        }
        
        int translationCategorySubStringLength = ConstantParameters.thesaursTranslationCategorysubString.length();
        int translationUFCategorySubStringLength = ConstantParameters.thesaursUFTranslationCategorysubString.length();
        ArrayList<Return_Full_Link_Id_Row> retFLIVals = new ArrayList<>();
        if (Q.bulk_return_full_link_id(set_from_links, retFLIVals) != QClass.APIFail) {
            for (Return_Full_Link_Id_Row row : retFLIVals) {

                String targetTerm = removePrefix(row.get_v1_cls());
                long targetTermIdL = row.get_v2_clsid();
                long targetTermRefIdL = row.get_v11_clsRefid();
                String targetTermTransiteration = row.get_v10_clsTransliteration();

                String category = row.get_v5_categ();
                String categoryKwd = keyWordsMappings.get(category);

                String value = row.get_v8_cmv().getString();
                
                
                long valueIdL = row.get_v8_cmv().getSysid();
                long valueRefIdL = row.get_v8_cmv().getRefid();
                String valueTransliterationStr = row.get_v8_cmv().getTransliterationString();
                
                
                if (categoryKwd == null) {
                    if (category.startsWith(BTLinkObj.getValue())) {
                        categoryKwd = ConstantParameters.bt_kwd;
                    } else {
                        continue;
                    }
                }
                
                if (categoryKwd.equals(ConstantParameters.modified_on_kwd) || categoryKwd.equals(ConstantParameters.created_on_kwd)) {
                    //no value change needed
                } else /*if (categoryKwd.compareTo(translation_kwd)==0) {
                 value = value.replaceFirst(DBThesaurusReferences.languageIdentifierSuffix, ": ");
                 }
                 else
                 */ {
                    value = removePrefix(value);
                }
                if(restrictOutputInSearchResultsOnly && filteringLinks.contains(categoryKwd)){
                    if(!completeSetNames.contains(value) || !completeSetNames.contains(targetTerm)){
                        if(Parameters.DEBUG){
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +"Skipping from collectTermSetInfoFrom values: " + value + "   "+targetTerm);
                        }
                        continue;
                        
                    }                    
                }
                
                
                // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
                //String valueId = String.valueOf(linkID.getValue());
                //clsID = new IntegerObject();
                //linkID = new IntegerObject();
                //cmv = new CMValue();

                

                if (categoryKwd.equals(ConstantParameters.bt_kwd)) {
                    category = category.replaceFirst(BTLinkObj.getValue(), "");
                }
                else if(categoryKwd.equals(ConstantParameters.rt_kwd)){
                    category = category.replaceFirst(RTLinkObj.getValue(), "");
                }
                
                SortItem targetTermSortItem = new SortItem(targetTerm, targetTermIdL, category, targetTermTransiteration, targetTermRefIdL);

                if (categoryKwd.equals(ConstantParameters.scope_note_kwd) 
                        || categoryKwd.equals(ConstantParameters.translations_scope_note_kwd) 
                        || categoryKwd.equals(ConstantParameters.historical_note_kwd)
                        || categoryKwd.equals(ConstantParameters.comment_kwd) 
                        || categoryKwd.equals(ConstantParameters.note_kwd) ) {

                    continue;
                }

                

                SortItem valueSortItem = new SortItem(value, valueIdL, category, valueTransliterationStr, valueRefIdL);

                //the translation category will be as follows --> to_EN, to_IT etc
                if (categoryKwd.equals(ConstantParameters.translation_kwd)) {
                    valueSortItem = new SortItem(value, valueIdL, category.substring(category.indexOf(ConstantParameters.thesaursTranslationCategorysubString) + translationCategorySubStringLength));
                }
                if (categoryKwd.equals(ConstantParameters.uf_translations_kwd)) {
                    valueSortItem = new SortItem(value, valueIdL, category.substring(category.indexOf(ConstantParameters.thesaursUFTranslationCategorysubString) + translationUFCategorySubStringLength));
                }
                if (categoryKwd.equals(ConstantParameters.uf_kwd) || categoryKwd.equals(ConstantParameters.rt_kwd)) {
                    valueSortItem.linkClass = "";
                }

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
                if (output.contains(categoryKwd) ) {
                    if(!termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).contains(valueSortItem)){
                        termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.getACopy());
                    }
                } else {
                    continue;
                }

                if (categoryKwd.equals(ConstantParameters.bt_kwd)) {

                    if (resultNodesIdsL.contains(valueIdL) ) {

                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueIdL, valueIdL, category, valueTransliterationStr, valueRefIdL));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }

                        if(!termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).contains(targetTermSortItem)){
                            termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTermSortItem.getACopy());
                        }                        
                    }
                } else if (categoryKwd.equals(ConstantParameters.rt_kwd)) {
                    if (resultNodesIdsL.contains(valueIdL)) {
                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueIdL, valueIdL, "", valueTransliterationStr, valueRefIdL));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }

                        SortItem newRt = new SortItem(targetTermSortItem.getLogName(), targetTermSortItem.getSysId(), "", targetTermSortItem.getLogNameTransliteration(), targetTermSortItem.getThesaurusReferenceId());
                        if(!termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).contains(newRt)){
                            termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(newRt);
                        }
                    }
                }
            }
        }

        /*
         while (Q.retur_full_link_id(set_from_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
         //while (Q.retur_full_link(set_from_links, cls, label, categ, fromcls, cmv, uniq_categ, traversed) != QClass.APIFail) {
         String targetTerm = removePrefix(cls.getValue());
         String category = categ.getValue();
         String categoryKwd = keyWordsMappings.get(category);

         String value = cmv.getString();
         // System.out.print((counter++) +" In " + targetTerm);
         int targetTermId = clsID.getValue();
         int  valueId = cmv.getSysid();
         // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
         //String valueId = String.valueOf(linkID.getValue());
         clsID = new IntegerObject();
         linkID = new IntegerObject();
         cmv = new CMValue();
            
         if (categoryKwd == null) {
         if(category.startsWith(BTLinkObj.getValue())){
         categoryKwd = ConstantParameters.bt_kwd;
         }
         else{
         continue;
         }
         } 

            

         if(categoryKwd.compareTo(ConstantParameters.bt_kwd)==0){
         category = category.replaceFirst(BTLinkObj.getValue(), "");
         }
         SortItem targetTermSortItem = new SortItem(targetTerm,targetTermId,category);
            
         if(categoryKwd.compareTo(ConstantParameters.scope_note_kwd) == 0
         || categoryKwd.compareTo(ConstantParameters.translations_scope_note_kwd) == 0
         || categoryKwd.compareTo(ConstantParameters.historical_note_kwd) == 0) {

         continue;
         }

         if (categoryKwd.compareTo(ConstantParameters.modified_on_kwd) == 0 || categoryKwd.compareTo(ConstantParameters.created_on_kwd) == 0) {
         //no value change needed
         } else
         //if (categoryKwd.compareTo(translation_kwd)==0) {
         //    value = value.replaceFirst(DBThesaurusReferences.languageIdentifierSuffix, ": ");
         //}
         //else
         {
         value = removePrefix(value);
         }
            
         SortItem valueSortItem  = new SortItem(value,valueId,category);

         //the translation category will be as follows --> to_EN, to_IT etc
         if (categoryKwd.compareTo(ConstantParameters.translation_kwd)==0) {
         valueSortItem  = new SortItem(value,valueId,category.substring(category.indexOf(ConstantParameters.thesaursTranslationCategorysubString)+translationCategorySubStringLength));
         }
         if (categoryKwd.compareTo(ConstantParameters.uf_translations_kwd)==0) {
         valueSortItem  = new SortItem(value,valueId,category.substring(category.indexOf(ConstantParameters.thesaursUFTranslationCategorysubString)+translationUFCategorySubStringLength));
         }
         if (categoryKwd.compareTo(ConstantParameters.uf_kwd)==0 || categoryKwd.compareTo(ConstantParameters.rt_kwd)==0 ) {
         valueSortItem.linkClass="";
         }

         //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
         if(output.contains(categoryKwd)){
         termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem);
         }
         else {
         continue;
         }
            
         if (categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                
         if(resultNodesIds.contains(valueId)){
                
         if (termsInfo.containsKey(value) == false) {
         NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
         newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem(""+valueId,valueId,category));
         termsInfo.put(value, newContainer);
         allTerms.add(value);
         }

         termsInfo.get(value).descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTermSortItem);
         }
         }
         else if(categoryKwd.compareTo(ConstantParameters.rt_kwd) == 0) {
         if(resultNodesIds.contains(valueId)){
         if (termsInfo.containsKey(value) == false) {
         NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
         newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem(""+valueId,valueId,""));
         termsInfo.put(value, newContainer);
         allTerms.add(value);
         }

         termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(new SortItem(targetTermSortItem.log_name , targetTermSortItem.sysid,""));
         }
         }
         }
         */
    }

    private void collectTermSetInfoTo(String selectedThesaurus, QClass Q, IntegerObject sis_session, int set_to_links,
            ArrayList<String> output, HashMap<String, NodeInfoSortItemContainer> termsInfo, ArrayList<String> allTerms, 
            ArrayList<Long> resultNodesIdsL, boolean restrictOutputInSearchResultsOnly, ArrayList<String> completeSetNames) {
        //step 1 collect all data around terms except status and scope notes

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        StringObject BTLinkObj = new StringObject();
        StringObject RTLinkObj = new StringObject();
        HashMap<String, String> keyWordsMappings = new HashMap<String, String>();

        String[] outputTable = new String[output.size()];
        output.toArray(outputTable);
        applyKeywordMappings(selectedThesaurus, Q, sis_session, outputTable, keyWordsMappings);
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);
        dbtr.getThesaurusCategory_RT(selectedThesaurus, Q, sis_session.getValue(), RTLinkObj);

        
        /*
         StringObject fromcls = new StringObject();
         StringObject label = new StringObject();
         StringObject categ = new StringObject();
         StringObject cls = new StringObject();
         IntegerObject uniq_categ = new IntegerObject();
         CMValue cmv = new CMValue();
         IntegerObject clsID = new IntegerObject();
         IntegerObject linkID = new IntegerObject();
         IntegerObject categID = new IntegerObject();
         */
        ArrayList<String> filteringLinks = new ArrayList<>();
        if(restrictOutputInSearchResultsOnly){
            filteringLinks.add(ConstantParameters.nt_kwd);
            filteringLinks.add(ConstantParameters.rt_kwd);
            filteringLinks.add(ConstantParameters.bt_kwd);
        }
        ArrayList<Return_Full_Link_Id_Row> retFLIVals = new ArrayList<>();
        if (Q.bulk_return_full_link_id(set_to_links, retFLIVals) != QClass.APIFail) {
            for (Return_Full_Link_Id_Row row : retFLIVals) {

                String targetTerm = removePrefix(row.get_v8_cmv().getString());
                
                long targetTermIdL = row.get_v8_cmv().getSysid();
                long targetTermRefIdL = row.get_v8_cmv().getRefid();
                String targetTermTransiteration = row.get_v8_cmv().getTransliterationString();

                String category = row.get_v5_categ();
                String categoryKwd = keyWordsMappings.get(category); // LINKS TO TERMS ARE EITHER BT LINKS OR RT LINKS
                if (category.startsWith(BTLinkObj.getValue())) {
                    categoryKwd = ConstantParameters.nt_kwd;
                }
                if (categoryKwd != null && categoryKwd.compareTo(ConstantParameters.bt_kwd) == 0) {
                    categoryKwd = ConstantParameters.nt_kwd;
                }
                
                
                String value = removePrefix(row.get_v1_cls());
                long valueIdL = row.get_v2_clsid();
                long valueRefIdL = row.get_v11_clsRefid();
                String valueTransliterationStr = row.get_v10_clsTransliteration();
                
                
                if(restrictOutputInSearchResultsOnly && filteringLinks.contains(categoryKwd)){
                    if(!completeSetNames.contains(value) || !completeSetNames.contains(targetTerm)){
                        if(Parameters.DEBUG){
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +"Skipping from collectTermSetInfoFrom values: " + value + "   "+targetTerm);
                        }
                        continue;
                    }                    
                }
                
                
                // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\t ID = " + targetTermId + " valueId = " +valueId);
                //String valueId = String.valueOf(linkID.getValue());
                //clsID = new IntegerObject();
                //linkID = new IntegerObject();
                //cmv = new CMValue();

                // LINKS TO TERMS ARE EITHER BT LINKS OR RT LINKS
                //if (category == null || category.compareTo(ConstantParameters.scope_note_kwd) == 0 || category.compareTo(ConstantParameters.translations_scope_note_kwd) == 0 || category.compareTo(ConstantParameters.historical_note_kwd) == 0) {
                //    continue;
                //}
                if (categoryKwd == null) {
                    if (row.get_v5_categ().startsWith(BTLinkObj.getValue())) {
                        categoryKwd = ConstantParameters.nt_kwd;
                    } else {
                        continue;
                    }
                }
                if (categoryKwd.equals(ConstantParameters.nt_kwd)) {
                    category = category.replaceFirst(BTLinkObj.getValue(), "");
                }
                else if(categoryKwd.equals(ConstantParameters.rt_kwd)){
                    category = category.replaceFirst(RTLinkObj.getValue(), "");
                }

                SortItem targetTermSortItem = new SortItem(targetTerm, targetTermIdL, category, targetTermTransiteration, targetTermRefIdL);
                /*
                 if (category.compareTo(ConstantParameters.modified_on_kwd) == 0 || category.compareTo(ConstantParameters.created_on_kwd) == 0) {
                 //no value change needed
                 } else {
                 value = dbGen.removePrefix(value);
                 }
                 */
                SortItem valueSortItem = new SortItem(value, valueIdL, category, valueTransliterationStr, valueRefIdL);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nInserting: targetTerm=\t" + targetTerm+"\ncategory=\t" + category+"\nvalue=\t" + value);
                if (output.contains(categoryKwd)) {
                    if(!termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).contains(valueSortItem)){
                        termsInfo.get(targetTerm).descriptorInfo.get(categoryKwd).add(valueSortItem.getACopy());
                    }
                } else {
                    continue;
                }

                if (categoryKwd.equals(ConstantParameters.nt_kwd)) {

                    if (resultNodesIdsL.contains(valueIdL)) {

                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueIdL, valueIdL, category, valueTransliterationStr, valueRefIdL));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }
                        if(!termsInfo.get(value).descriptorInfo.get(ConstantParameters.bt_kwd).contains(targetTermSortItem)){
                            termsInfo.get(value).descriptorInfo.get(ConstantParameters.bt_kwd).add(targetTermSortItem.getACopy());
                        }
                        
                    }
                } else if (categoryKwd.equals(ConstantParameters.rt_kwd)) {
                    if (resultNodesIdsL.contains(valueIdL)) {
                        if (termsInfo.containsKey(value) == false) {
                            NodeInfoSortItemContainer newContainer = new NodeInfoSortItemContainer(NodeInfoSortItemContainer.CONTAINER_TYPE_TERM, outputTable);
                            newContainer.descriptorInfo.get(ConstantParameters.id_kwd).add(new SortItem("" + valueIdL, valueIdL, category, valueTransliterationStr, valueRefIdL));
                            termsInfo.put(value, newContainer);
                            allTerms.add(value);
                        }

                        //termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTermSortItem.getACopy());
                        
                        if(!termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).contains(targetTermSortItem)){
                            termsInfo.get(value).descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTermSortItem.getACopy());
                        }
                    }

                }
            }
        }

    }

    public HashMap<String,Integer> collectGuideLinksWithUsage(String selectedThesaurus, QClass Q, IntegerObject sis_session) {

        HashMap<String,Integer>  resultVec = new HashMap();
        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();

        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);

        long retL = Q.reset_name_scope();
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        retL = Q.set_current_node(BTClassObj);
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        retL = Q.set_current_node(BTLinkObj);
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        int set_bts = Q.get_subclasses(0);
        Q.reset_set(set_bts);

        //StringObject label = new StringObject();
        ArrayList<String> tempNames = new ArrayList();

        ArrayList<Return_Nodes_Row> retVals = new ArrayList();
        if (Q.bulk_return_nodes(set_bts, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                tempNames.add(row.get_v1_cls_logicalname());
            }
        }
        /*while (Q.retur_nodes(set_bts, label) != QClass.APIFail) {
         tempNames.add(label.getValue());
         }*/

        Q.free_set(set_bts);

        for(String str : tempNames) {
            int cardinality =0;
            
            Q.reset_name_scope();
            Q.set_current_node(BTClassObj);
            Q.set_current_node(new StringObject(str));
            int setInstance = Q.get_all_instances(0);
            Q.reset_set(setInstance);
            cardinality = Q.set_get_card(setInstance);
            Q.free_set(setInstance);
            
            resultVec.put(str.replaceFirst(BTLinkObj.getValue(), ""),cardinality);
        }
        
        

        //Utils.StaticClass.webAppSystemOutPrintln(resultVec.toString());
        return resultVec;
    }
    
    public ArrayList<String> collectGuideLinks(String selectedThesaurus, QClass Q, IntegerObject sis_session) {

        ArrayList<String> resultVec = new ArrayList<String>();
        StringObject BTClassObj = new StringObject();
        StringObject BTLinkObj = new StringObject();

        getKeywordPair(selectedThesaurus, ConstantParameters.bt_kwd, BTClassObj, BTLinkObj, Q, sis_session);

        long retL = Q.reset_name_scope();
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        retL = Q.set_current_node(BTClassObj);
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        retL = Q.set_current_node(BTLinkObj);
        if (retL == QClass.APIFail) {
            return resultVec;
        }

        int set_bts = Q.get_subclasses(0);
        Q.reset_set(set_bts);

        //StringObject label = new StringObject();
        ArrayList<String> tempNames = new ArrayList();

        ArrayList<Return_Nodes_Row> retVals = new ArrayList();
        if (Q.bulk_return_nodes(set_bts, retVals) != QClass.APIFail) {
            for (Return_Nodes_Row row : retVals) {
                tempNames.add(row.get_v1_cls_logicalname());
            }
        }
        /*while (Q.retur_nodes(set_bts, label) != QClass.APIFail) {
         tempNames.add(label.getValue());
         }*/

        Q.free_set(set_bts);

        for (int i = 0; i < tempNames.size(); i++) {
            resultVec.add(tempNames.get(i).replaceFirst(BTLinkObj.getValue(), ""));
        }

        //Utils.StaticClass.webAppSystemOutPrintln(resultVec.toString());
        return resultVec;
    }

    public HashMap<String, String> getThesaurusTranslationCategories(QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            String thesaurusName1, String thesaurusName2, boolean startQueryAndConnection, boolean languageWordAsKey) {
        HashMap<String, String> LanguageWordsAndIds = new HashMap<String, String>();

        if (startQueryAndConnection) {
            //open connection and start Query
            if (openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral getThesaurusTranslationCategories()");
                return LanguageWordsAndIds;
            }

        }

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //get an AAATerm identifier
        StringObject thesaurusTermStrObj = new StringObject();
        dbtr.getThesaurusClass_Term(Q, sis_session.getValue(), new StringObject(thesaurusName1), thesaurusTermStrObj);

        //if(thesaurusTermStrObj.getValue().equals("")){
        //  return LanguageWordsAndIds;
        //}
        //Get Identifier for AAATerm->AAA_translation
        StringObject thesaurusTranslationStrObj = new StringObject();
        dbtr.getThesaurusCategory_translation(Q, sis_session.getValue(), new StringObject(thesaurusName1), thesaurusTranslationStrObj);
        Q.reset_name_scope();
        Q.set_current_node(thesaurusTermStrObj);
        long thesaurusTranslation_IdL = Q.set_current_node(thesaurusTranslationStrObj);

        int set_translationClasses = Q.get_subclasses(0);
        Q.reset_set(set_translationClasses);

        int set_translationWords = Q.get_to_value(set_translationClasses);
        Q.reset_set(set_translationWords);

        int set_prefixes = Q.get_link_from_by_category(set_translationWords, new StringObject(ConstantParameters.individualClass), new StringObject(ConstantParameters.hasPrefix));
        Q.reset_set(set_prefixes);


        /*
         StringObject fromcls = new StringObject();
         StringObject label = new StringObject();
         StringObject categ = new StringObject();
         StringObject cls = new StringObject();
         IntegerObject uniq_categ = new IntegerObject();

         IntegerObject clsID = new IntegerObject();
         IntegerObject linkID = new IntegerObject();
         IntegerObject categID = new IntegerObject();
         CMValue cmv = new CMValue();
         */
        ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
        if (Q.bulk_return_link(set_prefixes, retVals) != QClass.APIFail) {
            for (Return_Link_Row row : retVals) {
                String langWord = row.get_v1_cls();
                String langId = row.get_v3_cmv().getString();
                langWord = langWord.substring(0, langWord.length() - ConstantParameters.wordClass.length());
                langId = langId.substring(0, langId.length() - ConstantParameters.languageIdentifierSuffix.length());
                if (languageWordAsKey) {
                    LanguageWordsAndIds.put(langWord, langId);
                } else {
                    LanguageWordsAndIds.put(langId, langWord);
                }
            }
        }

        /*while (Q.retur_full_link_id(set_prefixes, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
         String langWord  = cls.getValue();
         String langId    = cmv.getString();
         langWord = langWord.substring(0,langWord.length()-ConstantParameters.wordClass.length());
         langId = langId.substring(0,langId.length()-ConstantParameters.languageIdentifierSuffix.length());
         if(languageWordAsKey){
         LanguageWordsAndIds.put(langWord,langId);
         }
         else{
         LanguageWordsAndIds.put(langId,langWord);
         }
         }*/
        Q.free_set(set_translationClasses);
        Q.free_set(set_translationWords);
        Q.free_set(set_prefixes);

        if (thesaurusName2 != null && thesaurusName2.length() > 0 && thesaurusName2.equals(thesaurusName1) == false) {

            //Q.TEST_end_query();
            //CloseDBConnection(Q, null, sis_session,null, false);
            //this.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, thesaurusName2, true);
            //TA.SetThesaurusName( new StringObject(thesaurusName2));
            //Q.TEST_begin_query();
            //get an AAATerm identifier
            thesaurusTermStrObj = new StringObject();
            dbtr.getThesaurusClass_Term(Q, sis_session.getValue(), new StringObject(thesaurusName2), thesaurusTermStrObj);

            //Get Identifier for AAATerm->AAA_translation
            thesaurusTranslationStrObj = new StringObject();
            dbtr.getThesaurusCategory_translation(Q, sis_session.getValue(), new StringObject(thesaurusName2), thesaurusTranslationStrObj);

            Q.reset_name_scope();
            Q.set_current_node(thesaurusTermStrObj);
            thesaurusTranslation_IdL = Q.set_current_node(thesaurusTranslationStrObj);

            set_translationClasses = Q.get_subclasses(0);
            Q.reset_set(set_translationClasses);

            set_translationWords = Q.get_to_value(set_translationClasses);
            Q.reset_set(set_translationWords);

            set_prefixes = Q.get_link_from_by_category(set_translationWords, new StringObject(ConstantParameters.individualClass), new StringObject(ConstantParameters.hasPrefix));
            Q.reset_set(set_prefixes);


            /*
             fromcls = new StringObject();
             label = new StringObject();
             categ = new StringObject();
             cls = new StringObject();
             uniq_categ = new IntegerObject();

             clsID = new IntegerObject();
             linkID = new IntegerObject();
             categID = new IntegerObject();
             cmv = new CMValue();
             */
            retVals.clear();
            if (Q.bulk_return_link(set_prefixes, retVals) != QClass.APIFail) {
                for (Return_Link_Row row : retVals) {
                    String langWord = row.get_v1_cls();
                    String langId = row.get_v3_cmv().getString();
                    langWord = langWord.substring(0, langWord.length() - ConstantParameters.wordClass.length());
                    langId = langId.substring(0, langId.length() - ConstantParameters.languageIdentifierSuffix.length());
                    if (languageWordAsKey) {
                        LanguageWordsAndIds.put(langWord, langId);
                    } else {
                        LanguageWordsAndIds.put(langId, langWord);
                    }
                }
            }

            /*while (Q.retur_full_link_id(set_prefixes, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
             String langWord  = cls.getValue();
             String langId    = cmv.getString();
             langWord = langWord.substring(0,langWord.length()-ConstantParameters.wordClass.length());
             langId = langId.substring(0,langId.length()-ConstantParameters.languageIdentifierSuffix.length());
             if(languageWordAsKey){
             LanguageWordsAndIds.put(langWord,langId);
             }
             else{
             LanguageWordsAndIds.put(langId,langWord);
             }
             }*/
            Q.free_set(set_translationClasses);
            Q.free_set(set_translationWords);
            Q.free_set(set_prefixes);

        }

        if (startQueryAndConnection) {
            Q.free_all_sets();
            Q.TEST_end_query();
            CloseDBConnection(Q, null, sis_session, null, false);
        }

        return LanguageWordsAndIds;
    }

    public boolean createTranslationCategories(QClass Q, TMSAPIClass TA,
            IntegerObject sis_session, IntegerObject tms_session, String targetThesaurus,
            HashMap<String, String> LanguageWordsAndIds,
            boolean startTransactionAndConnection, StringObject errorMessage, String pathToTranslationsXML) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        int ret = -1;
        boolean OneTransaction = true;

        
        if (startTransactionAndConnection) {

            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral createTranslationCategories");
                return false;
            }

        }

        //Create an Identifier for ThesaurusNotionType
        StringObject thesaurusNotionTypeObj = new StringObject(ConstantParameters.thesaurusNotionTypeClass);
        Q.reset_name_scope();
        long thesaurusNotionTypeIdL = Q.set_current_node(thesaurusNotionTypeObj);
        Identifier ThesaursNotionTypeIdentifier = new Identifier(thesaurusNotionTypeIdL);

        //Create an Identifier for Word
        StringObject wordObj = new StringObject(ConstantParameters.wordClass);
        Q.reset_name_scope();
        long wordIdL = Q.set_current_node(wordObj);
        Identifier WordIdentifier = new Identifier(wordIdL);

        //get a Prefix Class  identifier
        Q.reset_name_scope();
        long prefixClassIdL = Q.set_current_node(new StringObject(ConstantParameters.prefixClass));
        Identifier PrefixClassIdentifier = new Identifier(prefixClassIdL);

        //get Individual identifier
        StringObject individualStrObj = new StringObject(ConstantParameters.individualClass);

        //get has_prefix identifier
        StringObject hasPrefixStrObj = new StringObject(ConstantParameters.hasPrefix);

        //get an identifier for Individual->system_controlled
        StringObject systemControlledStrObj = new StringObject(ConstantParameters.systemControlled);
        Q.reset_name_scope();
        Q.set_current_node(individualStrObj);
        long syscontrolIdL = Q.set_current_node(systemControlledStrObj);
        Identifier systemControlledIdentifier = new Identifier(syscontrolIdL);

        //get an identifier for ThesaurusNotionType->translation_type
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.thesaurusNotionTypeClass));
        long translationTypeIdL = Q.set_current_node(new StringObject(ConstantParameters.translationType));
        Identifier translationTypeIdentifier = new Identifier(translationTypeIdL);

        //get an identifier for Term->Translation
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.termClass));
        long termTranslationIdL = Q.set_current_node(new StringObject(ConstantParameters.translationCategory));
        Identifier termTranslationIdentifier = new Identifier(termTranslationIdL);

        Q.reset_name_scope();
        long termClassIdL = Q.set_current_node(new StringObject(ConstantParameters.termClass));
        Identifier TermIdentifier = new Identifier(termClassIdL);

        //get an AAATerm identifier
        StringObject thesaurusTermStrObj = new StringObject();
        dbtr.getThesaurusClass_Term(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusTermStrObj);
        Q.reset_name_scope();
        long thesaurusTermIdL = Q.set_current_node(thesaurusTermStrObj);
        Identifier thesurusTermIdentifier = new Identifier(thesaurusTermIdL);

        //get an Identifier for Individual->garbage_collected
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.individualClass));
        long garbageCollected_IdL = Q.set_current_node(new StringObject(ConstantParameters.garbageCollected));
        Identifier garbageCollectedIdentifier = new Identifier(garbageCollected_IdL);

        //Get Identifier for AAAThesaurusNotionType->AAA_description
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(targetThesaurus.concat(ConstantParameters.thesaurusNotionTypeClass)));
        long thesaurusDescription_IdL = Q.set_current_node(new StringObject(targetThesaurus.concat(ConstantParameters.thesaurusDescriptionSuffix)));
        Identifier thesaurusDescriptionIdentifier = new Identifier(thesaurusDescription_IdL);

        //Get Identifier for AAATerm->AAA_translation
        StringObject thesaurusTranslationStrObj = new StringObject();
        dbtr.getThesaurusCategory_translation(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusTranslationStrObj);
        Q.reset_name_scope();
        Q.set_current_node(thesaurusTermStrObj);
        long thesaurusTranslation_IdL = Q.set_current_node(thesaurusTranslationStrObj);
        Identifier thesaurusTranslationIdentifier = new Identifier(thesaurusTranslation_IdL);

        //get an AAAHierarchyTerm identifier
        StringObject thesaurusHierarchyTermStrObj = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusHierarchyTermStrObj);
        Q.reset_name_scope();
        long thesaurusHierarchyTermIdL = Q.set_current_node(thesaurusHierarchyTermStrObj);
        Identifier thesurusHierarchyTermIdentifier = new Identifier(thesaurusHierarchyTermIdL);

        //Get Identifier for AAAHierarchyTerm->AAA_uf_translation
        StringObject thesaurusUFTranslationStrObj = new StringObject();
        dbtr.getThesaurusCategory_uf_translation(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusUFTranslationStrObj);
        Q.reset_name_scope();
        Q.set_current_node(thesaurusHierarchyTermStrObj);
        long thesaurusUFTranslation_IdL = Q.set_current_node(thesaurusUFTranslationStrObj);
        Identifier thesaurusUFTranslationIdentifier = new Identifier(thesaurusUFTranslation_IdL);
        

        //STEP 1 Create New Language Words and New Language Prefixes i.e. ItalianWord and IT`
        
        Iterator<String> languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            //Create an Identifier for the new Language.
            //It will be suffixed with string "Word".
            //i.e. if Language == English Identifer will be EnglishWord
            Identifier LanguageWordIdentifier = new Identifier(languageWord + ConstantParameters.wordClass);
            /*
             * RETELL Individual (ItalianWord) in S_Class
             * end
             */
            Q.reset_name_scope();
            //check if it exists
            if (Q.set_current_node(new StringObject(languageWord + ConstantParameters.wordClass)) == QClass.APIFail) {
                Q.reset_name_scope();
                ret = Q.CHECK_Add_Node(LanguageWordIdentifier, QClass.SIS_API_S_CLASS, true);
            }

            //update the Identifier object so that it references by sysid
            Q.reset_name_scope();
            //long newLanguageWordIdL = Q.set_current_node(new StringObject(LanguageWordIdentifier.getLogicalName()));
            //Q.reset_name_scope();
            //LanguageWordIdentifier = new Identifier(newLanguageWordIdL);

            /*
             * RETELL (ItalianWord) in (ThesaurusNotionType)
             * end
             */
            ret = Q.CHECK_Add_Instance(LanguageWordIdentifier, ThesaursNotionTypeIdentifier);
            /*
             * RETELL (ItalianWord) isA (Word)
             * end
             */
            ret = Q.CHECK_Add_IsA(LanguageWordIdentifier, WordIdentifier);

            /*
             * RETELL Individual (IT`) in Token
             * end
             */
            Identifier LanguagePrefixIdentifier = new Identifier(languageId + ConstantParameters.languageIdentifierSuffix);

            Q.reset_name_scope();
            if (Q.set_current_node(new StringObject(languageId + ConstantParameters.languageIdentifierSuffix)) == QClass.APIFail) {
                Q.reset_name_scope();
                ret = Q.CHECK_Add_Node(LanguagePrefixIdentifier, QClass.SIS_API_TOKEN_CLASS, true);
            }

            //update the Identifier object so that it references by sysid
            Q.reset_name_scope();
            //long newLanguageIdentifierIdL = Q.set_current_node(new StringObject(LanguagePrefixIdentifier.getLogicalName()));
            //LanguagePrefixIdentifier = new Identifier(newLanguageIdentifierIdL);

            /*
             * RETELL (IT`) in (Prefix)
             * end
             */
            Q.CHECK_Add_Instance(LanguagePrefixIdentifier, PrefixClassIdentifier);
        }

        if (OneTransaction == false) {
            //end transaction
            Q.free_all_sets();
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        // STEP 2 Connect New Language Words with New Language Prefixes i.e. ItalianWord -> has_prefix --> IT`
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral createTranslationCategories");
                return false;
            }
        }
        ret = Q.reset_name_scope();

        long retL = QClass.APIFail;
        int set_c = Q.set_get_new();
        ret = Q.reset_set(set_c);
        ret = Q.reset_name_scope();
        retL = Q.set_current_node(individualStrObj);
        retL = Q.set_current_node(hasPrefixStrObj);
        ret = Q.set_put(set_c);
        ret = Q.reset_set(set_c);

        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            /*
             *  RETELL (ItalianWord)
             *      with (has_prefix)
             *      :	(IT`)
             *  end
             */
            // create a word identifier
            ret = Q.reset_name_scope();
            long currentWordIdL = Q.set_current_node(new StringObject(languageWord.concat(ConstantParameters.wordClass)));
            Identifier currentWordIdentifier = new Identifier(currentWordIdL);

            ret = Q.reset_name_scope();
            long currentWordPrefixIdL = Q.set_current_node(new StringObject(languageId.concat(ConstantParameters.languageIdentifierSuffix)));
            //Identifier currentWordPrefixIdentifier = new Identifier(currentWordPrefixId);
            CMValue cmv = new CMValue();
            cmv.assign_node(languageId.concat(ConstantParameters.languageIdentifierSuffix), currentWordPrefixIdL);

            //reset categories set
            ret = Q.reset_name_scope();
            ret = Q.reset_set(set_c);

            ret = Q.CHECK_Add_Unnamed_Attribute(currentWordIdentifier, cmv, set_c);
        }

        Q.free_set(set_c);

        if (OneTransaction == false) {
            //end transaction

            Q.free_all_sets();
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        // STEP 3 Create the new translation Categories i.e. to_IT
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral createTranslationCategories");
                return false;
            }
        }
        //traverse all words
        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            /*
             * RETELL Attribute (to_IT)
             *   from	:	(Term)
             *   to	:	(ItalianWord) in S_Class
             * end
             */
            //Create an identifier for the new translation Category i.e. to_IT
            Identifier toNewLanguageIdentifier = new Identifier(ConstantParameters.toTranslationCategoryPrefix.concat(languageId));

            //Create a word CMValue
            Q.reset_name_scope();
            long currentWordIdL = Q.set_current_node(new StringObject(languageWord.concat(ConstantParameters.wordClass)));
            CMValue toWordVal = new CMValue();
            toWordVal.assign_node(languageWord.concat(ConstantParameters.wordClass), currentWordIdL);

            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.termClass));
            long categIdL = Q.set_current_node(new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId)));
            if (categIdL == QClass.APIFail) {
                ret = Q.CHECK_Add_Named_Attribute(toNewLanguageIdentifier, TermIdentifier, toWordVal, QClass.SIS_API_S_CLASS, -1, true);
            } else {
                toNewLanguageIdentifier = new Identifier(categIdL);
            }
            /*
             *  RETELL (to_IT)
             *     from	:	(Term)
             *     to	:	(ItalianWord) in (system_controlled), (translation_type) from (ThesaurusNotionType)
             *  end
             */

            //get an identifier with id for the new language category i.e. to_IT
            //Q.reset_name_scope();
            //Q.set_current_node(new StringObject(ConstantParameters.termClass));
            //long newLanguageCategoryIdL = Q.set_current_node(new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId)));
            //toNewLanguageIdentifier = new Identifier(newLanguageCategoryIdL);
            ret = Q.CHECK_Add_Instance(toNewLanguageIdentifier, systemControlledIdentifier);
            Q.reset_name_scope();
            ret = Q.CHECK_Add_Instance(toNewLanguageIdentifier, translationTypeIdentifier);

            /*
             * RETELL (to_IT)
             *	from	:	(Term)
             *	to	:	(ItalianWord) isA (Translation) from (Term)
             * end
             */
            ret = Q.CHECK_Add_IsA(toNewLanguageIdentifier, termTranslationIdentifier);
        }

        if (OneTransaction == false) {
            Q.free_all_sets();
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        // STEP 4 Create thesauric translation categories i.e AAA_translation, to_IT and uf translation categories i.e AAA_uf_translation, to_IT
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral createTranslationCategories");
                return false;
            }
        }

        //traverse all words
        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            
            /*
             *  RETELL Attribute (AAA_translation, to_IT)
             *      from  :  (AAATerm)
             *      to    :  (ItalianWord) in S_Class
             *  end
             */
            //create a string object and an identifer for the new Translation Category i.e. AAA_translation, to_IT
            StringObject thesaurusTranslationCategoryStrObj = new StringObject(targetThesaurus.concat(ConstantParameters.thesaursTranslationCategorysubString).concat(languageId));
            Identifier thesaurusTranslationCategoryIdentifier = new Identifier(thesaurusTranslationCategoryStrObj.getValue());

            StringObject currentWordStrObj = new StringObject(languageWord.concat(ConstantParameters.wordClass));
            Q.reset_name_scope();
            long currentWordIdL = Q.set_current_node(currentWordStrObj);
            CMValue cmvForCurrentWord = new CMValue();
            cmvForCurrentWord.assign_node(currentWordStrObj.getValue(), currentWordIdL);

            Q.reset_name_scope();
            Q.set_current_node(thesaurusTermStrObj);
            if (Q.set_current_node(thesaurusTranslationCategoryStrObj) == QClass.APIFail) {
                Q.reset_name_scope();
                ret = Q.CHECK_Add_Named_Attribute(thesaurusTranslationCategoryIdentifier, thesurusTermIdentifier, cmvForCurrentWord, QClass.SIS_API_S_CLASS, -1, true);
            }

            /*  RETELL (AAA_translation, to_IT)
             *      from  :  (AAATerm)
             *      to    :  (ItalianWord) in (garbage_collected),
             *                               (translation_type)
             *                              from (ThesaurusNotionType), (AAA_description) from (AAAThesaurusNotionType)
             *  end
             */
            //update the identifer so that it is based on system identifier i.e. AAA_translation, to_IT
            //Q.reset_name_scope();
            //Q.set_current_node(thesaurusTermStrObj);
            //long thesaurusTranslationCategoryIdL = Q.set_current_node(thesaurusTranslationCategoryStrObj);
            //thesaurusTranslationCategoryIdentifier = new Identifier(thesaurusTranslationCategoryIdL);
            Q.CHECK_Add_Instance(thesaurusTranslationCategoryIdentifier, garbageCollectedIdentifier);
            Q.CHECK_Add_Instance(thesaurusTranslationCategoryIdentifier, translationTypeIdentifier);
            Q.CHECK_Add_Instance(thesaurusTranslationCategoryIdentifier, thesaurusDescriptionIdentifier);

            /* RETELL (AAA_translation, to_IT)
             *     from  :  (AAATerm)
             *     to    :  (ItalianWord) isA (to_IT) from (Term), (AAA_translation) from (AAATerm)
             * end
             */
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.termClass));
            long currentToLanguageCategoryIdL = Q.set_current_node(new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId)));

            Identifier currentToLanguageCategoryIdentifier = new Identifier(currentToLanguageCategoryIdL);

            Q.CHECK_Add_IsA(thesaurusTranslationCategoryIdentifier, currentToLanguageCategoryIdentifier);
            Q.CHECK_Add_IsA(thesaurusTranslationCategoryIdentifier, thesaurusTranslationIdentifier);
            

            

            /*
             *  RETELL Attribute (AAA_uf_translation, to_IT)
             *      from  :  (AAAHierarchyTerm)
             *      to    :  (ItalianWord) in S_Class
             *  end
             */
            //create a string object and an identifer for the new Translation Category i.e. AAA_translation, to_IT
            StringObject thesaurusUFTranslationCategoryStrObj = new StringObject(targetThesaurus.concat(ConstantParameters.thesaursUFTranslationCategorysubString).concat(languageId));
            Identifier thesaurusUFTranslationCategoryIdentifier = new Identifier(thesaurusUFTranslationCategoryStrObj.getValue());

            Q.reset_name_scope();
            Q.set_current_node(thesaurusHierarchyTermStrObj);
            if (Q.set_current_node(thesaurusUFTranslationCategoryStrObj) == QClass.APIFail) {
                Q.reset_name_scope();
                ret = Q.CHECK_Add_Named_Attribute(thesaurusUFTranslationCategoryIdentifier, thesurusHierarchyTermIdentifier, cmvForCurrentWord, QClass.SIS_API_S_CLASS, -1, true);
            }

            /*  RETELL (AAA_uf_translation, to_IT)
             *      from  :  (AAAHierarchyTerm)
             *      to    :  (ItalianWord) in (garbage_collected),
             *                               (translation_type)
             *                              from (ThesaurusNotionType), (AAA_description) from (AAAThesaurusNotionType)
             *  end
             */
            //update the identifer so that it is based on system identifier i.e. AAA_uf_translation, to_IT
            //Q.reset_name_scope();
            ///Q.set_current_node(thesaurusHierarchyTermStrObj);
            //long thesaurusUFTranslationCategoryIdL = Q.set_current_node(thesaurusUFTranslationCategoryStrObj);
            //thesaurusUFTranslationCategoryIdentifier = new Identifier(thesaurusUFTranslationCategoryIdL);
            Q.CHECK_Add_Instance(thesaurusUFTranslationCategoryIdentifier, garbageCollectedIdentifier);
            Q.CHECK_Add_Instance(thesaurusUFTranslationCategoryIdentifier, translationTypeIdentifier);
            Q.CHECK_Add_Instance(thesaurusUFTranslationCategoryIdentifier, thesaurusDescriptionIdentifier);

            /* RETELL (AAA_uf_translation, to_IT)
             *     from  :  (AAAHierarchyTerm)
             *     to    :  (ItalianWord) isA (to_IT) from (Term), (AAA_uf_translation) from (AAAHierarchyTerm)
             * end
             */
            Q.CHECK_Add_IsA(thesaurusUFTranslationCategoryIdentifier, currentToLanguageCategoryIdentifier);
            Q.CHECK_Add_IsA(thesaurusUFTranslationCategoryIdentifier, thesaurusUFTranslationIdentifier);
            
        }

        if (startTransactionAndConnection) {
            Q.free_all_sets();
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        return true;
    }

    /*
    Usage is not any more recommended. Use the specific functions for each xml file
     * instead from the Utilities class i.e. 
     * translateFromMessagesXML / translateFromTranslationsXML / translateFromSaveAllLocaleAndScriptingXML
     * that do not require the path to the xml parameter and the args variable can be initialized in-line
     * @param resultObj
     * @param targetMessageBasePath
     * @param args
     * @param pathToErrorXMLFile 
     
    public void Translate(StringObject resultObj, String targetMessageBasePath, ArrayList<String> args, String pathToErrorXMLFile) {
        //String CurrentValue = "";
        //resultObj.getValue();
        //if (CurrentValue == null) {
       //            CurrentValue = "";
        //      }
        Utilities u = new Utilities();
        String tagetMessageFullXPath = targetMessageBasePath + "/option[@lang=\"" + Parameters.UILang + "\"]";
        resultObj.setValue(u.translate(tagetMessageFullXPath, args, pathToErrorXMLFile));
    }
     */
    public boolean deleteTranslationCategories(QClass Q, TMSAPIClass TA, IntegerObject sis_session,
            IntegerObject tms_session, String targetThesaurus, HashMap<String, String> LanguageWordsAndIds,
            boolean startTransactionAndConnection, StringObject resultMessageObj, String pathToTranslationsXML, final String uiLang) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        Utilities u = new Utilities();
        int ret = -1;
        boolean OneTransaction = true;
        
        //begin transaction
        //open connection and begin transaction
        if (startTransactionAndConnection) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral deleteTranslationCategories");
                return false;
            }
        }

        //Create an Identifier for ThesaurusNotionType
        StringObject thesaurusNotionTypeObj = new StringObject(ConstantParameters.thesaurusNotionTypeClass);
        Q.reset_name_scope();
        long thesaurusNotionTypeIdL = Q.set_current_node(thesaurusNotionTypeObj);
        Identifier ThesaursNotionTypeIdentifier = new Identifier(thesaurusNotionTypeIdL);

        //Create an Identifier for Word
        StringObject wordObj = new StringObject(ConstantParameters.wordClass);
        Q.reset_name_scope();
        long wordIdL = Q.set_current_node(wordObj);
        Identifier WordIdentifier = new Identifier(wordIdL);

        //get a Prefix Class  identifier
        Q.reset_name_scope();
        long prefixClassIdL = Q.set_current_node(new StringObject(ConstantParameters.prefixClass));
        Identifier PrefixClassIdentifier = new Identifier(prefixClassIdL);

        //get Individual identifier
        StringObject individualStrObj = new StringObject(ConstantParameters.individualClass);

        //get has_prefix identifier
        StringObject hasPrefixStrObj = new StringObject(ConstantParameters.hasPrefix);

        //get an identifier for Individual->system_controlled
        StringObject systemControlledStrObj = new StringObject(ConstantParameters.systemControlled);
        Q.reset_name_scope();
        Q.set_current_node(individualStrObj);
        long syscontrolIdL = Q.set_current_node(systemControlledStrObj);
        Identifier systemControlledIdentifier = new Identifier(syscontrolIdL);

        //get an identifier for ThesaurusNotionType->translation_type
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.thesaurusNotionTypeClass));
        long translationTypeIdL = Q.set_current_node(new StringObject(ConstantParameters.translationType));
        Identifier translationTypeIdentifier = new Identifier(translationTypeIdL);

        //get an identifier for Term->Translation
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.termClass));
        long termTranslationIdL = Q.set_current_node(new StringObject(ConstantParameters.translationCategory));
        Identifier termTranslationIdentifier = new Identifier(termTranslationIdL);

        Q.reset_name_scope();
        long termClassIdL = Q.set_current_node(new StringObject(ConstantParameters.termClass));
        Identifier TermIdentifier = new Identifier(termClassIdL);

        //get an AAATerm identifier
        StringObject thesaurusTermStrObj = new StringObject();
        dbtr.getThesaurusClass_Term(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusTermStrObj);
        Q.reset_name_scope();
        long thesaurusTermIdL = Q.set_current_node(thesaurusTermStrObj);
        Identifier thesurusTermIdentifier = new Identifier(thesaurusTermIdL);

        //get an AAAHierarchyTerm identifier
        StringObject thesaurusHierarchyTermStrObj = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusHierarchyTermStrObj);
        Q.reset_name_scope();
        long thesaurusHierarchyTermIdL = Q.set_current_node(thesaurusHierarchyTermStrObj);
        Identifier thesurusHierarchyTermIdentifier = new Identifier(thesaurusHierarchyTermIdL);

        //get an Identifier for Individual->garbage_collected
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(ConstantParameters.individualClass));
        long garbageCollected_IdL = Q.set_current_node(new StringObject(ConstantParameters.garbageCollected));
        Identifier garbageCollectedIdentifier = new Identifier(garbageCollected_IdL);

        //Get Identifier for AAAThesaurusNotionType->AAA_description
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(targetThesaurus.concat(ConstantParameters.thesaurusNotionTypeClass)));
        long thesaurusDescription_IdL = Q.set_current_node(new StringObject(targetThesaurus.concat(ConstantParameters.thesaurusDescriptionSuffix)));
        Identifier thesaurusDescriptionIdentifier = new Identifier(thesaurusDescription_IdL);

        //Get Identifier for AAATerm->AAA_translation
        StringObject thesaurusTranslationStrObj = new StringObject();
        dbtr.getThesaurusCategory_translation(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusTranslationStrObj);
        Q.reset_name_scope();
        Q.set_current_node(thesaurusTermStrObj);
        long thesaurusTranslation_IdL = Q.set_current_node(thesaurusTranslationStrObj);
        Identifier thesaurusTranslationIdentifier = new Identifier(thesaurusTranslation_IdL);

        //Get Identifier for AAAHierarchyTerm->AAA_uf_translation
        StringObject thesaurusUFTranslationStrObj = new StringObject();
        dbtr.getThesaurusCategory_uf_translation(Q, sis_session.getValue(), new StringObject(targetThesaurus), thesaurusUFTranslationStrObj);
        Q.reset_name_scope();
        Q.set_current_node(thesaurusHierarchyTermStrObj);
        long thesaurusUFTranslation_IdL = Q.set_current_node(thesaurusUFTranslationStrObj);
        Identifier thesaurusUFTranslationIdentifier = new Identifier(thesaurusUFTranslation_IdL);

        
        // Reverse STEP 4 Delete thesauric translation categories i.e AAA_translation, to_IT and AAA_uf_translation, to_IT
        
        //traverse all words
        Iterator<String> languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            
            //create a string object and an identifer for the new Translation Category i.e. AAA_translation, to_IT
            StringObject thesaurusTranslationCategoryStrObj = new StringObject(targetThesaurus.concat(ConstantParameters.thesaursTranslationCategorysubString).concat(languageId));

            String[] errorArgs = {languageWord, languageId, thesaurusTranslationCategoryStrObj.getValue()};
            String errorMessage1 = u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionError", errorArgs, uiLang);
            //update the identifer so that it is based on system identifier i.e. AAA_translation, to_IT
            Q.reset_name_scope();
            Q.set_current_node(thesaurusTermStrObj);
            long thesaurusTranslationCategoryIdL = Q.set_current_node(thesaurusTranslationCategoryStrObj);

            if (thesaurusTranslationCategoryIdL != QClass.APIFail) { // theere is a case that these thesauric concepts are not defined in the database

                //these thesauricconcepts should not have instances
                int set_instances = Q.get_instances(0);
                Q.reset_set(set_instances);
                if (Q.set_get_card(set_instances) <= 0) {

                    Identifier thesaurusTranslationCategoryIdentifier = new Identifier(thesaurusTranslationCategoryIdL);

                    StringObject currentWordStrObj = new StringObject(languageWord.concat(ConstantParameters.wordClass));
                    Q.reset_name_scope();
                    long currentWordIdL = Q.set_current_node(currentWordStrObj);
                    CMValue cmvForCurrentWord = new CMValue();
                    cmvForCurrentWord.assign_node(currentWordStrObj.getValue(), currentWordIdL);

                    // RETELL (AAA_translation, to_IT)
                    //     from  :  (AAATerm)
                    //     to    :  (ItalianWord) isA (to_IT) from (Term), (AAA_translation) from (AAATerm)
                    // end
                    //
                    Q.reset_name_scope();
                    Q.set_current_node(new StringObject(ConstantParameters.termClass));
                    long currentToLanguageCategoryIdL = Q.set_current_node(new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId)));
                    Identifier currentToLanguageCategoryIdentifier = new Identifier(currentToLanguageCategoryIdL);

                    ret = Q.CHECK_Delete_IsA(thesaurusTranslationCategoryIdentifier, currentToLanguageCategoryIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                    ret = Q.CHECK_Delete_IsA(thesaurusTranslationCategoryIdentifier, thesaurusTranslationIdentifier);;
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                    //  RETELL (AAA_translation, to_IT)
                    //      from  :  (AAATerm)
                    //      to    :  (ItalianWord) in (garbage_collected),
                    //                               (translation_type)
                    //                              from (ThesaurusNotionType), (AAA_description) from (AAAThesaurusNotionType)
                    //  end
                    //
                    ret = Q.CHECK_Delete_Instance(thesaurusTranslationCategoryIdentifier, garbageCollectedIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                    ret = Q.CHECK_Delete_Instance(thesaurusTranslationCategoryIdentifier, translationTypeIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                    ret = Q.CHECK_Delete_Instance(thesaurusTranslationCategoryIdentifier, thesaurusDescriptionIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                    //
                    //  RETELL Attribute (AAA_translation, to_IT)
                    //      from  :  (AAATerm)
                    //      to    :  (ItalianWord) in S_Class
                    //  end
                    //
                    ret = Q.CHECK_Delete_Named_Attribute(thesaurusTranslationCategoryIdentifier, thesurusTermIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage1);
                        return false;
                    }

                } else {

                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/DeletionErrorRemainingInstances", errorArgs, uiLang));
                    return false;

                }

                Q.reset_set(set_instances);
                Q.free_set(set_instances);
            }
            

            
            //create a string object and an identifer for the new Translation Category i.e. AAA_uf_translation, to_IT
            StringObject thesaurusUFTranslationCategoryStrObj = new StringObject(targetThesaurus.concat(ConstantParameters.thesaursUFTranslationCategorysubString).concat(languageId));
            errorArgs[2] = thesaurusUFTranslationCategoryStrObj.getValue();

            String errorMessage2 = u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionError", errorArgs, uiLang);
            //update the identifer so that it is based on system identifier i.e. AAA_translation, to_IT
            Q.reset_name_scope();
            Q.set_current_node(thesaurusHierarchyTermStrObj);
            long thesaurusUFTranslationCategoryIdL = Q.set_current_node(thesaurusUFTranslationCategoryStrObj);

            if (thesaurusUFTranslationCategoryIdL != QClass.APIFail) { // there is a case that these thesauric concepts are not defined in the database

                //these thesauricconcepts should not have instances
                int set_instances = Q.get_instances(0);
                Q.reset_set(set_instances);
                if (Q.set_get_card(set_instances) <= 0) {

                    Identifier thesaurusUFTranslationCategoryIdentifier = new Identifier(thesaurusUFTranslationCategoryIdL);

                    StringObject currentWordStrObj = new StringObject(languageWord.concat(ConstantParameters.wordClass));
                    Q.reset_name_scope();
                    long currentWordIdL = Q.set_current_node(currentWordStrObj);
                    CMValue cmvForCurrentWord = new CMValue();
                    cmvForCurrentWord.assign_node(currentWordStrObj.getValue(), currentWordIdL);

                    // RETELL (AAA_uf_translation, to_IT)
                    //     from  :  (AAAHierarchyTerm)
                    //     to    :  (ItalianWord) isA (to_IT) from (Term), (AAA_uf_translation) from (AAAHierarchyTerm)
                    // end
                    //
                    Q.reset_name_scope();
                    //ERROR CORRECTED it was HierarchyTerm while it should be Term 
                    Q.set_current_node(new StringObject(ConstantParameters.termClass));

                    long currentToLanguageCategoryIdL = Q.set_current_node(new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId)));
                    Identifier currentToLanguageCategoryIdentifier = new Identifier(currentToLanguageCategoryIdL);

                    ret = Q.CHECK_Delete_IsA(thesaurusUFTranslationCategoryIdentifier, currentToLanguageCategoryIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }

                    ret = Q.CHECK_Delete_IsA(thesaurusUFTranslationCategoryIdentifier, thesaurusUFTranslationIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }

                    //  RETELL (AAA_uf_translation, to_IT)
                    //      from  :  (AAAHierarchyTerm)
                    //      to    :  (ItalianWord) in (garbage_collected),
                    //                               (translation_type)
                    //                              from (ThesaurusNotionType), (AAA_description) from (AAAThesaurusNotionType)
                    //  end
                    //
                    ret = Q.CHECK_Delete_Instance(thesaurusUFTranslationCategoryIdentifier, garbageCollectedIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }

                    ret = Q.CHECK_Delete_Instance(thesaurusUFTranslationCategoryIdentifier, translationTypeIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }

                    ret = Q.CHECK_Delete_Instance(thesaurusUFTranslationCategoryIdentifier, thesaurusDescriptionIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }

                    //
                    //  RETELL Attribute (AAA_uf_translation, to_IT)
                    //      from  :  (AAAHierarchyTerm)
                    //      to    :  (ItalianWord) in S_Class
                    //  end
                    //
                    ret = Q.CHECK_Delete_Named_Attribute(thesaurusUFTranslationCategoryIdentifier, thesurusHierarchyTermIdentifier);
                    if (ret == QClass.APIFail) {
                        resultMessageObj.setValue(errorMessage2);
                        return false;
                    }
                }
                Q.reset_set(set_instances);
                Q.free_set(set_instances);
            }
            

        }

        if (OneTransaction == false) {
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        //check if other thesauri use this translation category
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral deleteTranslationCategories");
                return false;
            }
        }

        ArrayList<String> otherLanguageReferences = new ArrayList<String>();

        ArrayList<String> thesauriNames = new ArrayList<String>();
        this.GetExistingThesaurus(false, thesauriNames, Q, sis_session);

        for (int k = 0; k < thesauriNames.size(); k++) {
            String targetThes = thesauriNames.get(k);
            if (targetThes.equals(targetThesaurus)) {
                continue;
            }

            StringObject TermClassObj = new StringObject();
            StringObject TranslationsClassObj = new StringObject();
            dbtr.getThesaurusClass_Term(targetThes, Q, sis_session.getValue(), TermClassObj);
            dbtr.getThesaurusCategoryTranslation(targetThes, Q, sis_session.getValue(), TranslationsClassObj);
            Q.reset_name_scope();
            Q.set_current_node(TermClassObj);
            Q.set_current_node(TranslationsClassObj);
            int set_subClasses = Q.get_subclasses(0);
            Q.reset_set(set_subClasses);
            if (Q.set_get_card(set_subClasses) == 0) {

                Q.free_set(set_subClasses);
                continue;
            }

            int set_words = Q.get_to_value(set_subClasses);
            Q.reset_set(set_words);

            ArrayList<String> checkWords = this.get_Node_Names_Of_Set(set_words, false, Q, sis_session);

            Q.free_set(set_words);
            Q.free_set(set_subClasses);

            for (int m = 0; m < checkWords.size(); m++) {
                String tempWord = checkWords.get(m).replace("Word", "");

                if (otherLanguageReferences.contains(tempWord) == false) {
                    otherLanguageReferences.add(tempWord);
                }
            }
        }

        if (OneTransaction == false) {
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }

        // Reverse STEP 3 Delete the translation Categories i.e. to_IT
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral deleteTranslationCategories");
                return false;
            }
        }
        //traverse all words
        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            if (otherLanguageReferences.contains(languageWord)) {
                continue;
            }

            StringObject toTranslationCategoryStrObj = new StringObject(ConstantParameters.toTranslationCategoryPrefix.concat(languageId));

            String[] errorArgs = {languageWord, languageId, toTranslationCategoryStrObj.getValue()};
            String errorMessage = u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionError", errorArgs, uiLang);

            //get an identifier with id for the new language category i.e. to_IT
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.termClass));
            long newLanguageCategoryIdL = Q.set_current_node(toTranslationCategoryStrObj);

            //these to_IT should not have subclasses in order to be deleted. e.g. from other thesauri
            int set_subclasses = Q.get_subclasses(0);
            Q.reset_set(set_subclasses);
            if (Q.set_get_card(set_subclasses) <= 0) {

                Identifier toNewLanguageIdentifier = new Identifier(newLanguageCategoryIdL);
                //
                // RETELL (to_IT)
                //	from	:	(Term)
                //	to	:	(ItalianWord) isA (Translation) from (Term)
                // end
                //
                ret = Q.CHECK_Delete_IsA(toNewLanguageIdentifier, termTranslationIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(errorMessage);
                    return false;
                }

                //
                //  RETELL (to_IT)
                //     from	:	(Term)
                //     to	:	(ItalianWord) in (system_controlled), (translation_type) from (ThesaurusNotionType)
                //  end
                //
                ret = Q.CHECK_Delete_Instance(toNewLanguageIdentifier, systemControlledIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(errorMessage);
                    return false;
                }

                ret = Q.CHECK_Delete_Instance(toNewLanguageIdentifier, translationTypeIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(errorMessage);
                    return false;
                }

                //
                // RETELL Attribute (to_IT)
                //   from	:	(Term)
                //   to	:	(ItalianWord) in S_Class
                // end
                //
                //Delete a word CMValue
                ret = Q.CHECK_Delete_Named_Attribute(toNewLanguageIdentifier, TermIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(errorMessage);
                    return false;
                }
            }
            Q.reset_set(set_subclasses);
            Q.free_set(set_subclasses);
        }
        Q.free_all_sets();
        if (OneTransaction == false) {
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        // Reverse STEP 2 Delete Connections of Language Words with Language Prefixes i.e. ItalianWord -> has_prefix --> IT`
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral deleteTranslationCategories");
                return false;
            }
        }

        ret = Q.reset_name_scope();
        long retL = QClass.APIFail;
        int set_c = Q.set_get_new();
        ret = Q.reset_set(set_c);
        ret = Q.reset_name_scope();
        retL = Q.set_current_node(individualStrObj);
        retL = Q.set_current_node(hasPrefixStrObj);
        ret = Q.set_put(set_c);
        ret = Q.reset_set(set_c);

        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            if (otherLanguageReferences.contains(languageWord)) {
                continue;
            }

            String[] errorArgs = {languageWord, languageId, ConstantParameters.hasPrefix};
            String errorMsgStr = u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionError", errorArgs, uiLang);

            //
            //  RETELL (ItalianWord)
            //      with (has_prefix)
            //      :	(IT`)
            //  end
            //
            // create a word identifier
            ret = Q.reset_name_scope();
            Q.set_current_node(new StringObject(languageWord.concat(ConstantParameters.wordClass)));

            //howmany word instances
            int set_instances = Q.get_instances(0);
            Q.reset_set(set_instances);
            int instancesCardinallity = Q.set_get_card(set_instances);
            Q.free_set(set_instances);

            //howmany links from
            int set_links_from = Q.get_link_from(0);
            Q.reset_set(set_links_from);
            int allLinksFromNumber = Q.set_get_card(set_links_from);
            Q.free_set(set_links_from);

            //howmany links to
            int set_links_to = Q.get_link_to(0);
            Q.reset_set(set_links_to);
            int allLinksToNumber = Q.set_get_card(set_links_to);
            Q.free_set(set_links_to);

            //howmany Individual->has_prefix links from
            int set_has_prefix_links = Q.get_link_from_by_category(0, individualStrObj, hasPrefixStrObj);
            Q.reset_set(set_has_prefix_links);
            int hasPrefixLinksFromNumber = Q.set_get_card(set_has_prefix_links);

            //Language Word for deletion should have no instances
            //and the only links remaining from/to this word should be the has_prefix category
            if (instancesCardinallity == 0 && allLinksToNumber == 0 && hasPrefixLinksFromNumber == allLinksFromNumber) {

                StringObject fname = new StringObject();
                PrimitiveObject_Long fid = new PrimitiveObject_Long();
                PrimitiveObject_Long tid = new PrimitiveObject_Long();
                CMValue tname = new CMValue();
                IntegerObject traversed = new IntegerObject();

                Q.return_link_id(set_has_prefix_links, fname, fid, tid, tname, traversed);

                ret = Q.reset_name_scope();
                Q.free_set(set_has_prefix_links);
                ret = Q.reset_name_scope();

                ret = Q.CHECK_Delete_Unnamed_Attribute(new Identifier(tid.getValue()));
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(errorMsgStr);
                    return false;
                }
            } else {
                String[] errorArgs2 = {languageWord, languageId, ConstantParameters.hasPrefix, languageWord};
                if (instancesCardinallity != 0) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordInstances", errorArgs2, uiLang));

                } else if (allLinksToNumber != 0) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordRelations", errorArgs2, uiLang));

                } else if (hasPrefixLinksFromNumber != allLinksFromNumber) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordRelations", errorArgs2, uiLang));
                }
                return false;
            }

            Q.reset_set(set_has_prefix_links);
            Q.free_set(set_has_prefix_links);

        }

        Q.free_all_sets();
        if (OneTransaction == false) {
            //end transaction

            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        

        // Reverse STEP 1 Delete Language Words and Language Prefixes i.e. ItalianWord and IT`
        
        if (OneTransaction == false) {
            //open connection and start Transaction
            if (openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, targetThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class DBGeneral deleteTranslationCategories");
                return false;
            }
        }

        languagesEnumeration = LanguageWordsAndIds.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = LanguageWordsAndIds.get(languageWord);

            if (otherLanguageReferences.contains(languageWord)) {
                continue;
            }

            ret = Q.reset_name_scope();
            Q.set_current_node(new StringObject(languageWord.concat(ConstantParameters.wordClass)));

            //howmany word instances
            int set_instances = Q.get_instances(0);
            Q.reset_set(set_instances);
            int instancesCardinallity = Q.set_get_card(set_instances);
            Q.free_set(set_instances);

            //howmany links from
            int set_links_from = Q.get_link_from(0);
            Q.reset_set(set_links_from);
            int allLinksFromNumber = Q.set_get_card(set_links_from);
            Q.free_set(set_links_from);

            //howmany links to
            int set_links_to = Q.get_link_to(0);
            Q.reset_set(set_links_to);
            int allLinksToNumber = Q.set_get_card(set_links_to);
            Q.free_set(set_links_to);

            //also prefix for deletion should not now have any other links to
            Q.reset_name_scope();
            Q.set_current_node(new StringObject(languageId + ConstantParameters.languageIdentifierSuffix));
            int set_prefix_links_to = Q.get_link_to(0);
            Q.reset_set(set_prefix_links_to);

            int remainingLinksToPrefix = Q.set_get_card(set_prefix_links_to);
            Q.free_set(set_prefix_links_to);

            //Language Word for deletion should have no instances
            //It should also not have any links from / to as the has_prefix category was deleted previously
            //The prefix should also have no other links pointing to it
            if (instancesCardinallity == 0 && allLinksToNumber == 0 && allLinksFromNumber == 0 && remainingLinksToPrefix == 0) {

                String[] errorArgs1 = {languageWord, languageId, languageWord, languageId};

                //
                // RETELL (IT`) in (Prefix)
                // end
                //
                //update the Identifier object so that it references by sysid
                Q.reset_name_scope();
                long newLanguageIdentifierIdL = Q.set_current_node(new StringObject(languageId + ConstantParameters.languageIdentifierSuffix));
                Identifier LanguagePrefixIdentifier = new Identifier(newLanguageIdentifierIdL);

                ret = Q.CHECK_Delete_Instance(LanguagePrefixIdentifier, PrefixClassIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionErrorFinalStep", errorArgs1, uiLang));
                    return false;
                }

                //
                // RETELL Individual (IT`) in Token
                // end
                //
                ret = Q.CHECK_Delete_Node(LanguagePrefixIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionErrorFinalStep", errorArgs1, uiLang));
                    return false;
                }

                //update the Identifier object so that it references by sysid
                Q.reset_name_scope();
                long newLanguageWordIdL = Q.set_current_node(new StringObject(languageWord + ConstantParameters.wordClass));
                Q.reset_name_scope();
                Identifier LanguageWordIdentifier = new Identifier(newLanguageWordIdL);

                //
                // RETELL (ItalianWord) isA (Word)
                // end
                //
                ret = Q.CHECK_Delete_IsA(LanguageWordIdentifier, WordIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionErrorFinalStep", errorArgs1, uiLang));
                    return false;
                }

                //
                // RETELL (ItalianWord) in (ThesaurusNotionType)
                // end
                //
                ret = Q.CHECK_Delete_Instance(LanguageWordIdentifier, ThesaursNotionTypeIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionErrorFinalStep", errorArgs1, uiLang));
                    return false;
                }

                //
                // RETELL Individual (ItalianWord) in S_Class
                // end
                //
                ret = Q.CHECK_Delete_Node(LanguageWordIdentifier);
                if (ret == QClass.APIFail) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/CategoryDeletionErrorFinalStep", errorArgs1, uiLang));
                    return false;
                }
            } else {
                String[] errorArgs2 = {languageWord, languageId, languageWord};
                if (instancesCardinallity != 0) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordInstances", errorArgs2, uiLang));
                } else if (allLinksToNumber != 0) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordRelations", errorArgs2, uiLang));
                } else if (allLinksFromNumber != 0) {
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingWordRelations", errorArgs2, uiLang));
                } else if (remainingLinksToPrefix != 0) {
                    errorArgs2[2] = languageId;
                    resultMessageObj.setValue(u.translateFromMessagesXML("root/TranslationsSynchronization/remainingPrefixRelations", errorArgs2, uiLang));
                }
                return false;
            }

        }
        Q.free_all_sets();
        //end transaction
        if (startTransactionAndConnection) {
            Q.TEST_end_transaction();
            CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }

        
        return true;
    }

    public boolean synchronizeTranslationCategories(HashMap<String, String> currentTranslationCategories,
            HashMap<String, String> userSelections,
            ArrayList<String> userSelectedTranslationWords,
            ArrayList<String> userSelectedTranslationIdentifiers,
            String selectedThesaurus,
            StringObject resultMessageStrObj,
            String pathToMessagesXML,
            QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            final String uiLang) {

        boolean commitTransaction = true;
        //FIND OUT WHICH TRANSLATION CATEGORIES TO KEEP/ADD/DELETE
        HashMap<String, String> languagesForDeletion = new HashMap<String, String>();
        HashMap<String, String> languagesForAddition = new HashMap<String, String>();

        //check current languages in order to find which ones to delete
        Iterator<String> languagesEnumeration = currentTranslationCategories.keySet().iterator();
        while (languagesEnumeration.hasNext()) {
            String languageWord = languagesEnumeration.next();
            String languageId = currentTranslationCategories.get(languageWord);

            //if language word does not exist in user selection then delete it
            int indexOfCurrentWord = userSelectedTranslationWords.indexOf(languageWord);
            if (indexOfCurrentWord < 0) {
                languagesForDeletion.put(languageWord, languageId);
            } else {
                //if it does exist the check if user defined the same identifier.
                //if not the same then delete this
                int indexOfCurrentIdentifier = userSelectedTranslationIdentifiers.indexOf(languageId);
                if (indexOfCurrentWord != indexOfCurrentIdentifier) {
                    languagesForDeletion.put(languageWord, languageId);
                }
            }
        }

        //check user selection in order to find out which languages to add
        Iterator<String> userLanguagesEnumeration = userSelections.keySet().iterator();
        while (userLanguagesEnumeration.hasNext()) {

            String languageWord = userLanguagesEnumeration.next();
            String languageId = userSelections.get(languageWord);

            //if db contains the user selected word then check also if it has the same prefix.
            //if it does contain it and does have the same prefix then no action is needed
            //if it does contain it but it does not have the same prefix then it will be deleted
            //from the prior step and then it must be added again
            if (currentTranslationCategories.containsKey(languageWord)) {

                String currentTranslationPrefix = currentTranslationCategories.get(languageWord);

                if (currentTranslationPrefix.equals(languageId) == false) {
                    languagesForAddition.put(languageWord, languageId);
                }
            } else // here is the case where the database did not contain the current word
            {
                //in this case we must check that the prefix is not used (or will be not used since some deletions will occur first
                if (currentTranslationCategories.containsValue(languageId) == false || languagesForDeletion.containsValue(languageId)) {
                    languagesForAddition.put(languageWord, languageId);
                }
            }
        }

        if (resultMessageStrObj.getValue().length() <= 0) {
            commitTransaction = deleteTranslationCategories(Q, TA, sis_session, tms_session, selectedThesaurus, languagesForDeletion, false, resultMessageStrObj, pathToMessagesXML, uiLang);
            if (commitTransaction) {
                commitTransaction = createTranslationCategories(Q, TA, sis_session, tms_session, selectedThesaurus, languagesForAddition, false, resultMessageStrObj, pathToMessagesXML);
            } else {
                return false;
            }
        }

        if (commitTransaction) {
            Utils.StaticClass.webAppSystemOutPrintln("synchronizeTranslationCategories ended sucessfully");
            return true;
        } else {
            return false;
        }

    }

    /**
     * startQueryInsteadOfTransaction = true to begin query
     * startQueryInsteadOfTransaction = false to begin transaction
     *
     * @param Q
     * @param TA
     * @param sis_session
     * @param tms_session
     * @param targetThesaurus
     * @param startQueryInsteadOfTransaction
     * @return
     */
    public int openConnectionAndStartQueryOrTransaction(QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String targetThesaurus, boolean startQueryInsteadOfTransaction) {

        Utils.StaticClass.webAppSystemOutPrintln("openConnectionAndStartQueryOrTransaction");
        //create_SIS_CS_Session
        if (Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService()/*Parameters.server_host, Integer.parseInt(Parameters.server_port),Parameters.db_username, Parameters.db_password*/) == QClass.APIFail) {
            //Q.reset_name_scope();
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "OPEN CONNECTION ERROR @ create_SIS_CS_Session");
            return QClass.APIFail;
        }

        //open_connection
        if (Q.TEST_open_connection() == QClass.APIFail) {
            //Q.reset_name_scope();
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "OPEN CONNECTION ERROR @ open_connection");
            Q.TEST_release_SIS_Session();
            return QClass.APIFail;
        }

        if (startQueryInsteadOfTransaction) {//if query
            //begin_query
            if (Q.TEST_begin_query() == QClass.APIFail) {
                //Q.reset_name_scope();
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "OPEN CONNECTION ERROR @ begin_query");
                CloseDBConnection(Q, null, sis_session, null, false);
                return QClass.APIFail;
            }
        } else {

            //begin_transaction
            if (Q.TEST_begin_transaction() == QClass.APIFail) {
                //Q.reset_name_scope();
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "OPEN CONNECTION ERROR @ begin_transaction");
                CloseDBConnection(Q, TA, sis_session, tms_session, true);
                return QClass.APIFail;
            }
        }

        if (TA != null) {
            //create_TMS_API_Session
            if (TA.ALMOST_DONE_create_TMS_API_Session(Q, targetThesaurus) == TMSAPIClass.TMS_APIFail) {
                //Q.reset_name_scope();
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "OPEN CONNECTION ERROR @ create_TMS_API_Session");
                CloseDBConnection(Q, null, sis_session, null, false);
                return QClass.APIFail;
            }
            //SetThesaurusName is set in CHECK_create_TMS_API_Session
            /*
             if(targetThesaurus!=null){
             Utils.StaticClass.webAppSystemOutPrintln("setting thesaurus Name to: " + targetThesaurus);
             if(TA.SetThesaurusName(targetThesaurus)==TMSAPIClass.TMS_APIFail){
             Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"OPEN CONNECTION ERROR @ SetThesaurusName");
             CloseDBConnection(Q, TA, sis_session, tms_session, true);
             return QClass.APIFail;
             }
             }*/
        }

        //Q.reset_name_scope();
        return QClass.APISucc;
    }

    public int CloseDBConnection(QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, boolean TMSSessionRelease) {
        int ret = QClass.APISucc;

        Utils.StaticClass.webAppSystemOutPrintln("CloseDBConnection");

        if (TMSSessionRelease && TA != null) {
            ret = TA.ALMOST_DONE_release_TMS_API_Session();
            if (ret == TMSAPIClass.TMS_APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "CLOSE CONNECTION ERROR @ release_TMS_API_Session");
                Q.TEST_close_connection();
                Q.TEST_release_SIS_Session();
                return ret;
            }
        }

        ret = Q.TEST_close_connection();
        if (ret == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "CLOSE CONNECTION ERROR @ close_connection");
            Q.TEST_release_SIS_Session();
            return ret;
        }

        ret = Q.TEST_release_SIS_Session();
        if (ret == QClass.APIFail) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "CLOSE CONNECTION ERROR @ release_SIS_Session");
        }
        return ret;
    }

    public SortItem getSortItemFromCMValue(CMValue cmv, boolean removePrefix) {
        SortItem retSortItem = new SortItem(cmv);
        if (removePrefix) {
            retSortItem.setLogName(removePrefix(retSortItem.getLogName()));
        }
        return retSortItem;
    }

}
