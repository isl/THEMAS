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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import Utils.ConstantParameters;
import java.util.*;
import neo4j_sisapi.*;


/*---------------------------------------------------------------------
                            DBThesaurusReferences
-----------------------------------------------------------------------
class with methods for refering to specific classes/categories/prefixes
of the current Thesaurus
----------------------------------------------------------------------*/
public class DBThesaurusReferences {


    


    /*----------------------------------------------------------------------
                         getThesaurusCategory_translation()
    ------------------------------------------------------------------------
    gets the name of the created category of current Thesaurus: g.e. EKT_translation
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_translation(QClass Q, int SISsessionID, StringObject targetThesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q, SISsessionID, retObject,
                new StringObject(ConstantParameters.termClass),
                new StringObject(ConstantParameters.translationCategory),
                new StringObject(targetThesaurus.getValue().concat(ConstantParameters.thesaurusNotionTypeClass)),
                new StringObject(targetThesaurus.getValue().concat(ConstantParameters.thesaurusDescriptionSuffix)), card);
        return ret;
    }

    public int getThesaurusCategory_uf_translation(QClass Q, int SISsessionID, StringObject targetThesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject(ConstantParameters.hierarchytermClass), new StringObject(ConstantParameters.uftranslationCategory),
                new StringObject(targetThesaurus.getValue().concat(ConstantParameters.thesaurusNotionTypeClass)),
                new StringObject(targetThesaurus.getValue().concat(ConstantParameters.thesaurusDescriptionSuffix)), card);
        return ret;
    }

    public int getThesaurusCategory_translation(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {

        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject(ConstantParameters.termClass), new StringObject(ConstantParameters.translationCategory),
                new StringObject(SelectedThesaurus.concat(ConstantParameters.thesaurusNotionTypeClass)),
                new StringObject(SelectedThesaurus.concat(ConstantParameters.thesaurusDescriptionSuffix)), card);
        return ret;
    }

    public int getThesaurusCategory_uf_translation(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {

        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject(ConstantParameters.hierarchytermClass), new StringObject(ConstantParameters.uftranslationCategory),
                new StringObject(SelectedThesaurus.concat(ConstantParameters.thesaurusNotionTypeClass)),
                new StringObject(SelectedThesaurus.concat(ConstantParameters.thesaurusDescriptionSuffix)), card);
        return ret;
    }
    //QClass Q = new QClass();
    //int SISsessionID;
    //final String thesaurus = "AAA";
    /*----------------------------------------------------------------------
                        Constructor of DBThesaurusReferences
    -----------------------------------------------------------------------*/
    public DBThesaurusReferences(/*IntegerObject sis_session*/) {
      //SISsessionID = sis_session.getValue();
    }


    /*-------------------------------------------------------------------
                        getThesaurusObject()
    ---------------------------------------------------------------------
    INPUT: - retObject an allocated string which will be filled with
             the searching node/link name
           - givenSuperClassFrom (in case of searching a link name):
             the from value of the category which is superclass of
             the searching link name, (in case of searching a node name): NULL
           - givenSuperClass (in case of searching a link name):
             the category name which is superclass of the searching link name,
             (in case of searching a node name): the superclass of the searching node name
           - givenClassFrom (in case of searching a link name):
             the from value of the category which is class of
             the searching link name, (in case of searching a node name): NULL
           - givenClass (in case of searching a link name):
             the category name which is class of the searching link name,
             (in case of searching a node name): the class of the searching node name
           - card the number of found node/link names. Normally, it must be ONLY ONE.
    OUTPUT: - APISucc in case no error query execution happens
            - APIFail in case an error query execution happens
    FUNCTION: returns a node (case of testSuperClassFrom, testClassFrom = NULL)
              or a link (testSuperClassFrom, testClassFrom != NULL) of current thesaurus,
              applying the following rules:
              - for <thes>Descriptor returns:
                intersection(instances(<thes>ThesaurusNotionType), subclasses(givenSuperClass))
              - for <thes>Facet, <thes>Hierarchy returns:
                intersection(instances(<thes>ThesaurusClassType), subclasses(givenSuperClass))
              - for <thes>category returns:
                intersection(instances(<thes>ThesaurusNotionType, <thes>_relation), subclasses(givenSuperClassFrom, givenSuperClass))
                This function is called any time we want to refer to a node or
                a link of current thesaurus, which can be translated to any
                language. So, in this way, the code is independent by any translation
    ATTENTION: - this function must be called inside a query session
    -------------------------------------------------------------------*/
    private int getThesaurusObject(QClass Q, int API_sessionID, StringObject retObject, StringObject givenSuperClassFrom, StringObject givenSuperClass, StringObject givenClassFrom, StringObject givenClass, IntegerObject card) {
        int sub_class_set, class_set;
        int APIFail = QClass.APIFail;
        int APISucc = QClass.APISucc;

        // get the direct subclasses of givenSuperClass
        if (Q.reset_name_scope() == APIFail) {
            return APIFail;
        }
        if (givenSuperClassFrom != null) { // case of link
            if (Q.set_current_node( givenSuperClassFrom) == APIFail) {
                return APIFail;
            }
        }
	if (Q.set_current_node( givenSuperClass) == APIFail) {
            return APIFail;
	}
	if ((sub_class_set = Q.get_subclasses( 0)) == APIFail ) {
            return APIFail;
        }
        // get the direct instances of givenClass
        if (Q.reset_name_scope() == APIFail) {
            return APIFail;
        }
        if (givenClassFrom != null) { // case of link
            if (Q.set_current_node( givenClassFrom) == APIFail) {
                return APIFail;
            }
        }
	if (Q.set_current_node( givenClass) == APIFail) {
            return APIFail;
	}
	if ((class_set = Q.get_instances( 0)) == APIFail ) {
            return APIFail;
        }
        // get the intersection of sub_class_set and class_set
	if (Q.set_intersect( sub_class_set, class_set) == APIFail ) {
            return APIFail;
        }
        // get the item of the intersection (it MUST be always one)
        Q.reset_set( sub_class_set);
        int int_card = Q.set_get_card( sub_class_set);
        card.setValue(int_card);
        Q.return_nodes( sub_class_set, retObject);

        Q.free_set( sub_class_set);
        Q.free_set( class_set);
        return APISucc;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_AlternativeTerm()
    ------------------------------------------------------------------------
    gets the name of the AlternativeTerm class of current Thesaurus: g.e. EKTAlternativeTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_AlternativeTerm(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q, SISsessionID, retObject, null, new StringObject("AlternativeTerm"),
                null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_Descriptor()
    ------------------------------------------------------------------------
    gets the name of the Descriptor class of current Thesaurus: g.e. EKTDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Descriptor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Descriptor"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_Descriptor()
    ------------------------------------------------------------------------
    gets the name of the Descriptor class of current Thesaurus: g.e. EKTDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Descriptor(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Descriptor"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_Editor()
    ------------------------------------------------------------------------
    gets the name of the Editor class of current Thesaurus: g.e. EKTEditor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Editor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {

        if(SelectedThesaurus!=null ){
            retObject.setValue(SelectedThesaurus.concat("Editor"));
            return QClass.APISucc;
        }
        return QClass.APIFail;
        /*
        int ret = Q.reset_name_scope();
        if(ret == QClass.APIFail)
            return ret;

        ret = Q.set_current_node(new StringObject("Editor"));
        if(ret == QClass.APIFail)
            return ret;

        int set_sbc = Q.get_subclasses( 0);
        if(set_sbc == QClass.APIFail)
            return ret;
        Q.reset_set( set_sbc);
        //int card = Q.set_get_card( set_sbc);
        Q.retur_nodes( set_sbc, retObject);


        Q.free_set( set_sbc);
        return QClass.APISucc;
         */
    }


    /*----------------------------------------------------------------------
                         getThesaurusClass_Facet()
    ------------------------------------------------------------------------
    gets the name of the Facet class of current Thesaurus: g.e. EKTFacet
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Facet(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Facet"),
                null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_Hierarchy()
    ------------------------------------------------------------------------
    gets the name of the Hierarchy class of current Thesaurus: g.e. EKTHierarchy
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Hierarchy(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Hierarchy"),
                null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_HierarchyTerm()
    ------------------------------------------------------------------------
    gets the name of the HierarchyTerm class of current Thesaurus: g.e. EKTHierarchyTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_HierarchyTerm(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("HierarchyTerm"),
                null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_HierarchyTerm()
    ------------------------------------------------------------------------
    gets the name of the HierarchyTerm class of current Thesaurus: g.e. EKTHierarchyTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_HierarchyTerm(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("HierarchyTerm"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_NewThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the NewThesaurusClass class of current Thesaurus: g.e. EKTNewThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_NewThesaurusClass(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("NewThesaurusClass"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_NewThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the NewThesaurusClass class of current Thesaurus: g.e. EKTNewThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_NewThesaurusClass(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("NewThesaurusClass"), null, new StringObject(thesaurus.getValue().concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                 getThesaurusClass_JustBecameObsoleteDescriptor()
    ------------------------------------------------------------------------
    gets the name of the JustBecameObsoleteDescriptor class of current Thesaurus: g.e. EKTJustBecameObsoleteDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_JustBecameObsoleteDescriptor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("JustBecameObsoleteDescriptor"),
                null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                 getThesaurusClass_JustBecameObsoleteDescriptor()
    ------------------------------------------------------------------------
    gets the name of the JustBecameObsoleteDescriptor class of current Thesaurus: g.e. EKTJustBecameObsoleteDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_JustBecameObsoleteDescriptor(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("JustBecameObsoleteDescriptor"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                 getThesaurusClass_JustBecameObsoleteThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the JustBecameObsoleteThesaurusClass class of current Thesaurus: g.e. EKTJustBecameObsoleteThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_JustBecameObsoleteThesaurusClass(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("JustBecameObsoleteThesaurusClass"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                 getThesaurusClass_JustBecameObsoleteThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the JustBecameObsoleteThesaurusClass class of current Thesaurus: g.e. EKTJustBecameObsoleteThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_JustBecameObsoleteThesaurusClass(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("JustBecameObsoleteThesaurusClass"), null, new StringObject(thesaurus.getValue().concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_NewDescriptor()
    ------------------------------------------------------------------------
    gets the name of the NewDescriptor class of current Thesaurus: g.e. EKTNewDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_NewDescriptor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("NewDescriptor"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_NewDescriptor()
    ------------------------------------------------------------------------
    gets the name of the NewDescriptor class of current Thesaurus: g.e. EKTNewDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_NewDescriptor(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("NewDescriptor"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteDescriptor()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteDescriptor class of current Thesaurus: g.e. EKTObsoleteDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteDescriptor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteDescriptor"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteDescriptor()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteDescriptor class of current Thesaurus: g.e. EKTObsoleteDescriptor
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteDescriptor(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteDescriptor"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*HLIAS-----------------------------------------------------------------
                         getThesaurusClass_ObsoleteFacet()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteFacet class of current Thesaurus: g.e. EKTObsoleteFacet
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteFacet(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteFacet"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }

    /*HLIAS----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteHierarchy()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteHierarchy class of current Thesaurus: g.e. EKTObsoleteHierarchy
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteHierarchy(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteHierarchy"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteTerm()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteTerm class of current Thesaurus: g.e. EKTObsoleteTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteTerm(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteTerm"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteTerm()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteTerm class of current Thesaurus: g.e. EKTObsoleteTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteTerm(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteTerm"), null, new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteThesaurusClass class of current Thesaurus: g.e. EKTObsoleteThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteThesaurusClass(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteThesaurusClass"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_ObsoleteThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the ObsoleteThesaurusClass class of current Thesaurus: g.e. EKTObsoleteThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ObsoleteThesaurusClass(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ObsoleteThesaurusClass"), null, new StringObject(thesaurus.getValue().concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusOfTerm()
    ------------------------------------------------------------------------
    gets the name of the StatusOfTerm class of current Thesaurus: g.e. EKTStatusOfTerm
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusOfTerm(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusOfTerm"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusApproved()
    ------------------------------------------------------------------------
    gets the name of the StatusApproved class of current Thesaurus: g.e. EKTStatusApproved
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusApproved(String SelectedThesaurus,  StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusApproved"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusForInsertion()
    ------------------------------------------------------------------------
    gets the name of the StatusForInsertion class of current Thesaurus: g.e. EKTStatusForInsertion
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusForInsertion(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusForInsertion"));
    }


    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusForApproval()
    ------------------------------------------------------------------------
    gets the name of the StatusForApproval class of current Thesaurus: g.e. EKTStatusForApproval
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusForApproval(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusForApproval"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusTermAccepted()
    ------------------------------------------------------------------------
    gets the name of the StatusTermAccepted class of current Thesaurus: g.e. EKTStatusTermAccepted
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusTermAccepted(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusTermAccepted"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusForReinspection()
    ------------------------------------------------------------------------
    gets the name of the StatusForReinspection class of current Thesaurus: g.e. EKTStatusForReinspection
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusForReinspection(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusForReinspection"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusTermNotAccepted()
    ------------------------------------------------------------------------
    gets the name of the StatusTermNotAccepted class of current Thesaurus: g.e. EKTStatusTermNotAccepted
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusTermNotAccepted(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusTermNotAccepted"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_StatusUnderConstruction()
    ------------------------------------------------------------------------
    gets the name of the StatusUnderConstruction class of current Thesaurus: g.e. EKTStatusUnderConstruction
    ------------------------------------------------------------------------*/
    public void getThesaurusClass_StatusUnderConstruction(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.concat("StatusUnderConstruction"));
    }




    /*----------------------------------------------------------------------
                         getThesaurusClass_Term()
    ------------------------------------------------------------------------
    gets the name of the Term class of current Thesaurus: g.e. EKTTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Term(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Term"), null, new StringObject(SelectedThesaurus.concat("ThesaurusExpressionType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_Term()
    ------------------------------------------------------------------------
    gets the name of the Term class of current Thesaurus: g.e. EKTTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_Term(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("Term"), null, new StringObject(thesaurus.getValue().concat("ThesaurusExpressionType")), card);
        return ret;
    }


    /*----------------------------------------------------------------------
                         getThesaurusClass_ThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the ThesaurusClass class of current Thesaurus: g.e. EKTThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ThesaurusClass(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ThesaurusClass"), null, new StringObject(SelectedThesaurus.concat("ThesaurusClassType")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusClass_ThesaurusClass()
    ------------------------------------------------------------------------
    gets the name of the ThesaurusClass class of current Thesaurus: g.e. EKTThesaurusClass
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ThesaurusClass(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ThesaurusClass"), null, new StringObject(thesaurus.getValue().concat("ThesaurusClassType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_ThesaurusConcept()
    ------------------------------------------------------------------------
    gets the name of the ThesaurusConcept class of current Thesaurus: g.e. EKTThesaurusConcept
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ThesaurusConcept(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ThesaurusConcept"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_ThesaurusNotion()
    ------------------------------------------------------------------------
    gets the name of the ThesaurusNotion class of current Thesaurus: g.e. EKTThesaurusNotion
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_ThesaurusNotion(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("ThesaurusNotion"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_TopTerm()
    ------------------------------------------------------------------------
    gets the name of the TopTerm class of current Thesaurus: g.e. EKTTopTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_TopTerm(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("TopTerm"), null, new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusClass_UsedForTerm()
    ------------------------------------------------------------------------
    gets the name of the UsedForTerm class of current Thesaurus: g.e. EKTUsedForTerm
    ------------------------------------------------------------------------*/
    public int getThesaurusClass_UsedForTerm(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, null, new StringObject("UsedForTerm"), null, new StringObject(SelectedThesaurus.concat("ThesaurusExpressionType")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_ALT()
    ------------------------------------------------------------------------
    gets the name of the ALT category of current Thesaurus: g.e. EKT_ALT
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_ALT(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("ALT"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_relation")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_belongs_to_hierarchy()
    ------------------------------------------------------------------------
    gets the name of the BT category of current Thesaurus: g.e. belongs_to_thes1_hierarchy
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_belongs_to_hierarchy(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        retObject.setValue("belongs_to_"+SelectedThesaurus.toLowerCase()+"_hierarchy");

        //int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("TopTerm"), new StringObject("belongs_to_hierarchy"), new StringObject("Individual"), new StringObject("system_controlled"), card);
        //return ret;
        return QClass.APISucc;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_BT()
    ------------------------------------------------------------------------
    gets the name of the BT category of current Thesaurus: g.e. EKT_BT
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_BT(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("Descriptor"), new StringObject("BT"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_relation")), card);
        return ret;
    }
    
    public int getThesaurusCategory_externalLink(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        
        Q.reset_name_scope();
        long retL = Q.set_current_node(new StringObject(SelectedThesaurus.toUpperCase().concat("HierarchyTerm")));
        if(retL<=0){
            return QClass.APIFail;
        }
        StringObject tempVal = new StringObject(SelectedThesaurus.toUpperCase().concat("has_external_link"));
        retL = Q.set_current_node(tempVal);
        if(retL<=0){
            return QClass.APIFail;
        }
        Q.reset_name_scope();
        retObject.setValue(tempVal.getValue());
        
        return QClass.APISucc;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_created()
    ------------------------------------------------------------------------
    gets the name of the created category of current Thesaurus: g.e. ekt_created
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_created(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("created"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_comment()
    ------------------------------------------------------------------------
    gets the name of the comment category of current Thesaurus: g.e. ekt_comment
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_comment(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("ThesaurusConcept"), new StringObject("comment"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_dewey()
    ------------------------------------------------------------------------
    gets the name of the dewey category of current Thesaurus: g.e. ekt_dewey
    ------------------------------------------------------------------------*/
    public void getThesaurusCategory_dewey(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.toLowerCase().concat("_dewey"));
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_created()
    ------------------------------------------------------------------------
    gets the name of the created category of current Thesaurus: g.e. ekt_created
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_editor(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("editor"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }



    /*----------------------------------------------------------------------
                         getThesaurusCategory_found_in()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_found_in
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_found_in(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("found_in"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }



    /*----------------------------------------------------------------------
                         getThesaurusCategory_gave_name_to()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_gave_name_to
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_gave_name_to(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("gave_name_to"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_relation")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusCategory_gave_name_to()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_gave_name_to
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_gave_name_to(QClass Q, int SISsessionID, StringObject thesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("gave_name_to"), new StringObject(thesaurus.getValue().concat("ThesaurusNotionType")), new StringObject(thesaurus.getValue().concat("_relation")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_historical_note()
    ------------------------------------------------------------------------
    gets the name of the historical_note category of current Thesaurus: g.e. ekt_historical_note
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_historical_note(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("ThesaurusConcept"), new StringObject("historical_note"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_modified()
    ------------------------------------------------------------------------
    gets the name of the modified category of current Thesaurus: g.e. ekt_modified
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_modified(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("modified"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }




    /*----------------------------------------------------------------------
                         getThesaurusCategory_not_found_in()
    ------------------------------------------------------------------------
    gets the name of the not_found_in category of current Thesaurus: g.e. ekt_not_found_in
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_not_found_in(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("not_found_in"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_RT()
    ------------------------------------------------------------------------
    gets the name of the RT category of current Thesaurus: g.e. EKT_RT
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_RT(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("RT"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_relation")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_scope_note()
    ------------------------------------------------------------------------
    gets the name of the scope_note category of current Thesaurus: g.e. ekt_scope_note
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_scope_note(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("ThesaurusConcept"), new StringObject("scope_note"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_scope_note()
    ------------------------------------------------------------------------
    gets the name of the scope_note category of current Thesaurus: g.e. ekt_scope_note
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_translations_scope_note(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("ThesaurusConcept"), new StringObject("translations_scope_note"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }
    
    /*----------------------------------------------------------------------
                         getThesaurusCategory_note()
    ------------------------------------------------------------------------
    gets the name of the scope_note category of current Thesaurus: g.e. ekt_note
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_note(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();

        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("ThesaurusConcept"), new StringObject("note"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }
    /*----------------------------------------------------------------------
                         getThesaurusCategory_taxonomic_code()
    ------------------------------------------------------------------------
    gets the name of the taxonomic_code category of current Thesaurus: g.e. ekt_taxonomic_code
    ------------------------------------------------------------------------*/
    public void getThesaurusCategory_taxonomic_code(String SelectedThesaurus, StringObject retObject) {

        retObject.setValue(SelectedThesaurus.toLowerCase().concat("_taxonomic_code"));
    }

    public int getThesaurusCategoryTranslation(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("Term"), new StringObject("Translation"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }

    public int getThesaurusCategoryTranslation(QClass Q, int SISsessionID,String selectedThesaurus, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("Term"), new StringObject("Translation"), new StringObject(selectedThesaurus.concat("ThesaurusNotionType")), new StringObject(selectedThesaurus.concat("_description")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_to_EN() ABANDONED since now we support multiple translations
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_found_in
    ------------------------------------------------------------------------*/

    /*
    int getThesaurusCategory_to_EN(QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        THEMASUserInfo SessionUserInfo = (THEMASUserInfo)sessionInstance.getAttribute("SessionUser");
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("Term"), new StringObject("to_EN"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_description")), card);
        return ret;
    }
    */

    /*----------------------------------------------------------------------
                         getThesaurusCategory_UF()
    ------------------------------------------------------------------------
    gets the name of the UF category of current Thesaurus: g.e. EKT_UF
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_UF(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        IntegerObject card = new IntegerObject();
        int ret = getThesaurusObject(Q,SISsessionID, retObject, new StringObject("HierarchyTerm"), new StringObject("UF"), new StringObject(SelectedThesaurus.concat("ThesaurusNotionType")), new StringObject(SelectedThesaurus.concat("_relation")), card);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_AlternativeTerm()
    ------------------------------------------------------------------------
    gets the prefix of the AlternativeTerms of current Thesaurus: g.e. EL`
    (to-value of EKTThesaurusNotionType->EKTNotion`UsesAsPrefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_AlternativeTerm(String SelectedThesaurus, QClass Q, int SISsessionID) {
        // looking for EKTThesaurusNotion
        StringObject thesThesaurusNotion = new StringObject();
        getThesaurusClass_ThesaurusNotion(SelectedThesaurus,Q,SISsessionID,thesThesaurusNotion);

        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusNotion);
        int set = Q.get_link_from_by_category( 0, new StringObject("ThesaurusNotionType"), new StringObject("Notion`UsesAsPrefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link(set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_Class()
    ------------------------------------------------------------------------
    gets the prefix of the Classes of current Thesaurus: g.e. EKTClass`
    (to-value of EKTThesaurusClass->EKT`Class`UsesAsPrefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_Class(String SelectedThesaurus, QClass Q, int SISsessionID) {
        // looking for ThesaurusClass
        StringObject thesThesaurusClass = new StringObject();
        getThesaurusClass_ThesaurusClass(SelectedThesaurus,Q,SISsessionID,thesThesaurusClass);

        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusClass);
        int set = Q.get_link_from_by_category( 0, new StringObject("ThesaurusClassType"), new StringObject("Class`UsesAsPrefix"));
        Q.reset_set(set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);
        //Q.reset_name_scope();

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_DeweyNumber()
    ------------------------------------------------------------------------
    gets the prefix of the DeweyNumbers of current Thesaurus: g.e. Dewey`
    (to-value of DeweyNumber->has_prefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_DeweyNumber(QClass Q, int SISsessionID) {
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("DeweyNumber"));
        int set = Q.get_link_from_by_category( 0, new StringObject("Individual"), new StringObject("has_prefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }



    /*----------------------------------------------------------------------
                         getThesaurusPrefix_Editor()
    ------------------------------------------------------------------------
    gets the prefix of the Editors of current Thesaurus: g.e. Person`
    (to-value of Editor->has_prefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_Editor(QClass Q, int SISsessionID) {
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Editor"));
        int set = Q.get_link_from_by_category( 0, new StringObject("Individual"), new StringObject("has_prefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_EnglishWord()
    ------------------------------------------------------------------------
    gets the prefix of the EnglishWords of current Thesaurus: g.e. EN`
    (to-value of EnglishWord->has_prefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_EnglishWord(QClass Q, int SISsessionID) {
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("EnglishWord"));
        int set = Q.get_link_from_by_category( 0, new StringObject("Individual"), new StringObject("has_prefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set(set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_Descriptor(Q,sis_session.getValue())
    ------------------------------------------------------------------------
    gets the prefix of the Descriptors of current Thesaurus: g.e. EL`
    (to-value of EKTThesaurusNotionType->EKTNotion`UsesAsPrefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_Descriptor(String SelectedThesaurus,  QClass Q, int SISsessionID) {
        // looking for EKTThesaurusNotion

        StringObject thesThesaurusNotion = new StringObject();
        getThesaurusClass_ThesaurusNotion(SelectedThesaurus, Q,SISsessionID,thesThesaurusNotion);

        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusNotion);
        int set = Q.get_link_from_by_category( 0, new StringObject("ThesaurusNotionType"), new StringObject("Notion`UsesAsPrefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_Source()
    ------------------------------------------------------------------------
    gets the prefix of the Sources of current Thesaurus: g.e. Literature`
    (to-value of Source->has_prefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_Source(QClass Q, int SISsessionID) {
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Source"));
        int set = Q.get_link_from_by_category( 0, new StringObject("Individual"), new StringObject("has_prefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_TaxonomicCode()
    ------------------------------------------------------------------------
    gets the prefix of the TaxonomiCodes of current Thesaurus: g.e. TaxCode`
    (to-value of TaxonomicCode->has_prefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_TaxonomicCode(QClass Q, int SISsessionID) {
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("TaxonomicCode"));
        int set = Q.get_link_from_by_category( 0, new StringObject("Individual"), new StringObject("has_prefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_TopTerm()
    ------------------------------------------------------------------------
    gets the prefix of the TopTerms of current Thesaurus: g.e. EL`
    (to-value of EKTThesaurusNotionType->EKTNotion`UsesAsPrefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_TopTerm(String SelectedThesaurus,  QClass Q, int SISsessionID) {
        // looking for EKTThesaurusNotion
        StringObject thesThesaurusNotion = new StringObject();
        getThesaurusClass_ThesaurusNotion(SelectedThesaurus, Q,SISsessionID,thesThesaurusNotion);

        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusNotion);
        int set = Q.get_link_from_by_category( 0, new StringObject("ThesaurusNotionType"), new StringObject("Notion`UsesAsPrefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*----------------------------------------------------------------------
                         getThesaurusPrefix_UsedForTerm()
    ------------------------------------------------------------------------
    gets the prefix of the UsedForTerms of current Thesaurus: g.e. EL`
    (to-value of EKTThesaurusNotionType->EKTNotion`UsesAsPrefix)
    ------------------------------------------------------------------------*/
    public String getThesaurusPrefix_UsedForTerm(String SelectedThesaurus,  QClass Q, int SISsessionID) {
        // looking for EKTThesaurusNotion
        StringObject thesThesaurusNotion = new StringObject();
        getThesaurusClass_ThesaurusNotion(SelectedThesaurus, Q,SISsessionID,thesThesaurusNotion);

        Q.reset_name_scope();
        Q.set_current_node(thesThesaurusNotion);
        int set = Q.get_link_from_by_category( 0, new StringObject("ThesaurusNotionType"), new StringObject("Notion`UsesAsPrefix"));
        Q.reset_set( set);

        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link( set, cls, label, cmv);
        Q.free_set( set);

        String prefix = cmv.getString();
        return prefix;
    }

    /*NOT GOOD IMPLEMENTATION*/
    /*----------------------------------------------------------------------
                         getThesaurusCategory_created_by()
    ------------------------------------------------------------------------
    gets the name of the modified category of current Thesaurus: g.e. ekt_modified
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_created_by(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        int ret =getThesaurusCategory_created(SelectedThesaurus,Q,SISsessionID,retObject);
        String temp = retObject.getValue().concat("_by");
        retObject.setValue(temp);
        return ret;
    }


    /*----------------------------------------------------------------------
                         getThesaurusCategory_modified_by()
    ------------------------------------------------------------------------
    gets the name of the modified category of current Thesaurus: g.e. ekt_modified
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_modified_by(String SelectedThesaurus, QClass Q, int SISsessionID, StringObject retObject) {
        int ret =getThesaurusCategory_modified(SelectedThesaurus,Q,SISsessionID,retObject);
        String temp = retObject.getValue().concat("_by");
        retObject.setValue(temp);
        return ret;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_bt_found_in()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_found_in
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_bt_found_in(String SelectedThesaurus, StringObject retObject) {

        String temp = SelectedThesaurus.toLowerCase().concat("_bt_found_in");
        retObject.setValue(temp);
        return QClass.APISucc;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_primary_found_in()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_found_in
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_primary_found_in(String SelectedThesaurus, StringObject retObject) {

        String temp = SelectedThesaurus.toLowerCase().concat("_primary_found_in");
        retObject.setValue(temp);
        return QClass.APISucc;
    }




    /*----------------------------------------------------------------------
                         getThesaurusCategory_translations_found_in()
    ------------------------------------------------------------------------
    gets the name of the found_in category of current Thesaurus: g.e. ekt_found_in
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_translations_found_in(String SelectedThesaurus, StringObject retObject) {

        String temp = SelectedThesaurus.toLowerCase().concat("_translations_found_in");
        retObject.setValue(temp);
        return QClass.APISucc;
    }

    /*----------------------------------------------------------------------
                         getThesaurusCategory_uf_translations()
    ------------------------------------------------------------------------
    gets the name of the uk_uf category of current Thesaurus: g.e. ekt_uf_translations
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_uf_translations(String SelectedThesaurus, StringObject retObject) {

        String temp = SelectedThesaurus.concat("_uf_translation");
        retObject.setValue(temp);
        return QClass.APISucc;
    }
    /*----------------------------------------------------------------------
                         getThesaurusCategory_uk_alt()
    ------------------------------------------------------------------------
    gets the name of the uk_uf category of current Thesaurus: g.e. ekt_uk_alt
    ------------------------------------------------------------------------*/
    public int getThesaurusCategory_uk_alt(String SelectedThesaurus, StringObject retObject) {

        String temp = SelectedThesaurus.concat("_uk_alt");
        retObject.setValue(temp);
        return QClass.APISucc;
    }

    /*
    public int getMaxBytesForUF(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;

        String prefix = this.getThesaurusPrefix_UsedForTerm(selectedThesaurus, Q, sis_session.getValue());


        returnVal -= prefix.length();//2 for lang identifier 1 for `

        return returnVal;
    }

    public int getMaxBytesForTranslation(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;


        returnVal -= 3;//2 for lang identifier 1 for `
        
        return returnVal;
    }

    public int getMaxBytesForDewey(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;

        String prefixcode = getThesaurusPrefix_DeweyNumber(Q, sis_session.getValue());
        returnVal -= prefixcode.length();
        return returnVal;
    }

    public int getMaxBytesForTaxonomicalcode(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        

        String prefixcode = getThesaurusPrefix_TaxonomicCode(Q, sis_session.getValue());
        returnVal -= prefixcode.length();
        return returnVal;
    }

    public int getMaxBytesForDate(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        return returnVal;
    }

    public int getMaxBytesForUser(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        
        String prefixPerson = getThesaurusPrefix_Editor(Q, sis_session.getValue());

        returnVal -= prefixPerson.length();

        return returnVal;
    }

    public int getMaxBytesForFacet(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        

        String prefixClass = getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        returnVal -= prefixClass.length();

        return returnVal;
    }

    public int getMaxBytesForHierarchy(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        

        String prefixTerm = getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        String prefixClass = getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());
        if(prefixClass.length()>prefixTerm.length()){
            returnVal -= prefixClass.length();
        }
        else{
            returnVal -= prefixTerm.length();
        }

        StringObject trSn = new StringObject();
        getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), trSn);
        returnVal -= (trSn.getValue().length()+1); // -1 stands for `that prefixes the scope note category

        return returnVal;
    }

    public int getMaxBytesForSource(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;
        
        String prefix = getThesaurusPrefix_Source(Q, sis_session.getValue());
        returnVal -= prefix.length();

        //dbtr.g(selectedThesaurus, Q, sis_session.getValue(), trSn);
        returnVal -= (ConstantParameters.source_note_kwd.length()+1); // -1 stands for `that prefixes the scope note category
        //
        return returnVal;
    }

    public int getMaxBytesForCommentCategory(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        int returnVal = THEMASAPIClass.MAX_COM_LEN - (THEMASAPIClass.MAX_COM_LEN / THEMASAPIClass.MAX_STRING);

        if((THEMASAPIClass.MAX_COM_LEN % THEMASAPIClass.MAX_STRING)!=0){
            returnVal --;
        }
        
        return returnVal;
    }

    public int getMaxBytesForStatus(String selectedThesaurus, QClass Q, IntegerObject sis_session){
        return THEMASAPIClass.LOGINAM_SIZE-1;
    }
    
    public int getMaxBytesForDescriptor(String selectedThesaurus, QClass Q, IntegerObject sis_session){
            int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;


        String prefixTerm =  getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        //String prefixClass =  getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());
        //if(prefixClass.length()>prefixTerm.length()){
        //    returnVal -= prefixClass.length(); //NULLClass`
        //}
        //else{
            returnVal -= prefixTerm.length();
        //}
        
        StringObject trSn = new StringObject();
        getThesaurusCategory_translations_scope_note(selectedThesaurus, Q, sis_session.getValue(), trSn);
        returnVal -= (trSn.getValue().length()+1); // -1 stands for `that prefixes the scope note category

        return returnVal;
    }

    public int getMaxBytesForGuideTerm(String selectedThesaurus, QClass Q, IntegerObject sis_session){

        int returnVal = THEMASAPIClass.LOGINAM_SIZE-1;

        StringObject btObj = new StringObject();

        this.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), btObj);
        returnVal-=btObj.getValue().length();

        return returnVal;
    }
    */
}
