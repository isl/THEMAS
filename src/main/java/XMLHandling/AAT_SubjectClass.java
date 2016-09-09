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
package XMLHandling;

import java.util.Vector;
import java.util.Hashtable;

/**
 *
 * @author tzortzak
 */


public class AAT_SubjectClass {

    public enum AAT_Subject_Kind_Enum {
        UNDEFINED, KIND_FACET, KIND_HIERARCHY, KIND_CONCEPT, KIND_GUIDE_TERM
    }

    public AAT_Subject_Kind_Enum SubjectKind;
    public String SubjectId = "";

    public AAT_SubjectTermClass SubjectPreferredTermName = new AAT_SubjectTermClass();
    public Vector<AAT_SubjectTermClass> nonPreferredTermNames = new Vector<AAT_SubjectTermClass>();

    //public String PreferredParentSubjectId = "";
    public Vector<String> parentSubjectIds = new Vector<String>();
    public Vector<String> associatedSubjectIds = new Vector<String>();
    public Vector<AAT_SubjectTermClass> descriptiveNotes = new Vector<AAT_SubjectTermClass>();

    public Vector<AAT_RevisionClass> contributors = new Vector<AAT_RevisionClass>();

    public AAT_SubjectClass(){
        this.SubjectKind = AAT_Subject_Kind_Enum.UNDEFINED;
    }
}
