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
package Utils;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tzortzak
 */
public class ConstantParameters {

    public static String copyrightAndLicenseForPropertiesFile = "# \n" +
"# Copyright 2015 Institute of Computer Science,\n" +
"#                Foundation for Research and Technology - Hellas.\n" +
"#\n" +
"# Licensed under the EUPL, Version 1.1 or - as soon they will be approved\n" +
"# by the European Commission - subsequent versions of the EUPL (the \"Licence\");\n" +
"# You may not use this work except in compliance with the Licence.\n" +
"# You may obtain a copy of the Licence at:\n" +
"#\n" +
"#      http://ec.europa.eu/idabc/eupl\n" +
"#\n" +
"# Unless required by applicable law or agreed to in writing, software distributed\n" +
"# under the Licence is distributed on an \"AS IS\" basis,\n" +
"# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
"# See the Licence for the specific language governing permissions and limitations\n" +
"# under the Licence.\n" +
"# \n" +
"# =============================================================================\n" +
"# Contact: \n" +
"# =============================================================================\n" +
"# Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece\n" +
"#     Tel: +30-2810-391632\n" +
"#     Fax: +30-2810-391638\n" +
"#  E-mail: isl@ics.forth.gr\n" +
"# WebSite: http://www.ics.forth.gr/isl/cci.html\n" +
"# \n" +
"# =============================================================================\n" +
"# Authors: \n" +
"# =============================================================================\n" +
"# Elias Tzortzakakis <tzortzak@ics.forth.gr>\n" +            
"# \n" +
"# This file is part of the THEMAS system.\n" +
"#\n";
    /*
    public static String copyrightAndLicenseForXML_XSLFile = "<!-- \n" +
" Copyright 2015 Institute of Computer Science,\n" +
"                Foundation for Research and Technology - Hellas.\n" +
"\n" +
" Licensed under the EUPL, Version 1.1 or - as soon they will be approved\n" +
" by the European Commission - subsequent versions of the EUPL (the \"Licence\");\n" +
" You may not use this work except in compliance with the Licence.\n" +
" You may obtain a copy of the Licence at:\n" +
"\n" +
"      http://ec.europa.eu/idabc/eupl\n" +
"\n" +
" Unless required by applicable law or agreed to in writing, software distributed\n" +
" under the Licence is distributed on an \"AS IS\" basis,\n" +
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
" See the Licence for the specific language governing permissions and limitations\n" +
" under the Licence.\n" +
" \n" +
" =============================================================================\n" +
" Contact: \n" +
" =============================================================================\n" +
" Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece\n" +
"     Tel: +30-2810-391632\n" +
"     Fax: +30-2810-391638\n" +
"  E-mail: isl@ics.forth.gr\n" +
" WebSite: http://www.ics.forth.gr/isl/cci.html\n" +
" \n" +
" =============================================================================\n" +
" Authors: \n" +
" =============================================================================\n" +
" Elias Tzortzakakis <tzortzak@ics.forth.gr>\n" +
" \n" +
" This file is part of the THEMAS system.\n" +
" -->\n";
    */
    
    
    
    public static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    public static boolean DEVELOPING = true;
    
    //Group_Reader --> i.e. READER. This user can see only the published part of the thesaurus without creation/modfication data
    public static final String Group_Reader = "READER";
    public static final String Group_Library = "LIBRARY";
    public static final String Group_ThesaurusTeam = "THESAURUS_TEAM";
    public static final String Group_ThesaurusCommittee = "THESAURUS_COMMITTEE";
    
    //Group_External_Reader --> i.e. EXTERNALREADER. This user can see everything (apart from other user data) in a specific thesarus but cannot change anything 
    public static final String Group_External_Reader = "EXTERNALREADER";
    
    public static final String Group_Administrator = "ADMINISTRATOR";
    
    public static final String hierarchytermClass = "HierarchyTerm";
    public static final String termClass = "Term";
    public static final String individualClass = "Individual";
    public static final String systemControlled = "system_controlled";
    public static final String translationType = "translation_type";
    public static final String garbageCollected = "garbage_collected";
    public static final String translationCategory = "Translation";
    public static final String uftranslationCategory = "UF_Translation";
    public static final String hasPrefix = "has_prefix";
    public static final String prefixClass = "Prefix";
    public static final String wordClass = "Word";
    public static final String thesaurusNotionTypeClass = "ThesaurusNotionType";

    public static final String thesaurusDescriptionSuffix = "_description";
    public static final String languageIdentifierSuffix = "`";
    public static final String toTranslationCategoryPrefix = "to_";
    public static final String thesaursTranslationCategorysubString = "_translation, to_";
    public static final String thesaursUFTranslationCategorysubString = "_uf_translation, to_";
    
    public final static int SEARCH_COMMENTS_MODE_CONTAINS          = 0;
    public final static int SEARCH_COMMENTS_MODE_STARTS_WITH       = 1;
    public final static int SEARCH_COMMENTS_MODE_ENDS_WITH         = 2;
    public final static int SEARCH_COMMENTS_MODE_NOT_CONTAINS      = 3;
    public final static int SEARCH_COMMENTS_MODE_NOT_STARTS_WITH   = 4;
    public final static int SEARCH_COMMENTS_MODE_NOT_ENDS_WITH     = 5;

    public static final String DeweyClass = "DeweyNumber";
    public static final String TaxonomicCodeClass = "TaxonomicCode";
    public static final String validBTSandRTsClass = "HierarchyTerm";
    public static final String EnglishWordClass = "EnglishWord";
    public static final String SourceClass = "Source";
    public static final String PersonClass = "Editor";
    public static final int FROM_Direction = 0; //result nodes have relations that originate from them
    public static final int TO_Direction = 1;   //result nodes have relations that end to them
    public static final int BOTH_Direction = 2;

    
    public static final String XMLTermsWrapperElementName = "terms";
    public static final String XMLSourcesWrapperElementName = "sources";
    public static final String XMLTermElementName = "term";
    public static final String XMLNodeLabelsWrapperElementName = "nodelabels";
    public static final String XMLNodeLabelElementName = "nodelabel";
    public static final String XMLDescriptorElementName = "descriptor";

    public static final String XMLLinkClassAttributeName = "linkClass";

    public static final String id_kwd = "id";
    public static final String facet_kwd = "facet";
    public static final String topterm_kwd = "topterm";
    public static final String translation_kwd = "translations";
    public static final String bt_kwd = "bt";
    public static final String nt_kwd = "nt";
    //addition standing for output of recursive bts
    public static final String rbt_kwd = "rbt";
    //addition standing for output of recursive nts
    public static final String rnt_kwd = "rnt";
    public static final String rt_kwd = "rt";
    public static final String uf_kwd = "uf";
    public static final String uf_translations_kwd ="uf_translations";
    public static final String dn_kwd = "dn";
    public static final String tc_kwd = "tc";
    public static final String alt_kwd = "alt";    
    public static final String primary_found_in_kwd = "primary_found_in";
    public static final String translations_found_in_kwd = "translations_found_in";
    public static final String scope_note_kwd = "scope_note";
    public static final String translations_scope_note_kwd = "translations_scope_note";
    public static final String historical_note_kwd = "historical_note";
    public static final String comment_kwd= "comment";
    public static final String created_by_kwd = "created_by";
    public static final String modified_by_kwd ="modified_by";
    public static final String created_on_kwd ="created_on";
    public static final String modified_on_kwd ="modified_on";
    public static final String delete_term_kwd ="delete_term";
    //public static final String move2Hier_kwd ="move2Hier";
    public static final String term_create_kwd ="term_create";
    public static final String source_note_kwd ="source_note";
    public static final String belongs_to_hier_kwd = "belongs_to_hierarchy";
    public static final String guide_term_kwd = "guide_terms";

    public static final String accepted_kwd ="accepted";
    public static final String status_kwd   ="status";
    
    public static final String system_referenceUri_kwd   ="ReferenceUri";
    public static final String system_referenceIdAttribute_kwd   ="referenceId";
    public static final String system_transliteration_kwd   ="transliteration";


    public static final String editor_kwd ="editor";
    public static final String foundIn_kwd = "found_in";

    public static final int get_Term_Top_Terms = 1;
    public static final int get_Term_Facets    = 0;

    public static final String Status_Accepted     = "Yes";
    public static final String Status_Not_Accepted = "No";



    public static final String MOVE_NODE_ONLY           = "MOVE_NODE_ONLY";
    public static final String MOVE_NODE_AND_SUBTREE    = "MOVE_NODE_AND_SUBTREE";
    public static final String CONNECT_NODE_AND_SUBTREE = "CONNECT_NODE_AND_SUBTREE";

    public static final int DESCRIPTOR_OF_KIND_NEW = 0;
    public static final int DESCRIPTOR_OF_KIND_RELEASED = 1;
    public static final int DESCRIPTOR_OF_KIND_OBSOLETE = 2;

    //HLIAS constats used by GetKindOfhierarchy()
    public static final int HIERARCHY_OF_KIND_NEW = 0;
    public static final int HIERARCHY_OF_KIND_RELEASED = 1;
    public static final int HIERARCHY_OF_KIND_OBSOLETE = 2;

    // constats used by GetKindOfFacet()
    public static final int FACET_OF_KIND_NEW = 0;
    public static final int FACET_OF_KIND_RELEASED = 1;
    public static final int FACET_OF_KIND_OBSOLETE = 2;

    // constats used by getBT_NT()
    public static final int BT_DIRECTION = 0;
    public static final int NT_DIRECTION = 1;

   public static final String xmlschematype_aat = "aat";
    public static final String xmlschematype_skos = "skos";
    public static final String xmlschematype_THEMAS = "THEMAS";

    public static final String aat_subject_tag = "Subject";

    public static final String aat_subject_id_attr = "Subject_ID";
    public static final String aat_record_type_tag = "Record_Type";
    public static final String aat_record_type_Facet_val = "Facet";
    public static final String aat_record_type_Hierarchy_Name_val = "Hierarchy Name";
    public static final String aat_record_type_Guide_Term_val = "Guide Term";
    public static final String aat_record_type_Concept_val = "Concept";


    public static final String aat_Hierarchy_tag = "Hierarchy";
    public static final String aat_Preferred_Term_tag = "Preferred_Term";
    public static final String aat_Non_Preferred_Term_tag = "Non-Preferred_Term";
    public static final String aat_Term_Text_tag = "Term_Text";

    public static final String aat_Parent_Relationships_Wrapper = "Parent_Relationships";
    public static final String aat_Preffered_Parent_tag = "Preferred_Parent";
    public static final String aat_Non_Preffered_Parent_tag = "Non-Preferred_Parent";
    public static final String aat_Parent_Subject_ID_tag = "Parent_Subject_ID";

    public static final String aat_Associative_Relationships_tag = "Associative_Relationships";
    public static final String aat_Associative_Relationships_Subject_ID_tag =  "VP_Subject_ID";

    public static final String aat_Revision_History_Wrapper_tag = "Revision_History";
    public static final String aat_Revision_tag = "Revision";
    public static final String aat_Revision_Action_tag = "Action";
    public static final String aat_Revision_Action_created_val = "created";
    public static final String aat_Revision_User_Name_tag = "User_Name";
    public static final String aat_Revision_Date_tag = "Date";

    public static final String aat_Term_LanguagesWrapper_tag = "Term_Languages";
    public static final String aat_Term_Language_tag = "Term_Language";
    public static final String aat_Term_Language_Language_tag = "Language";
    public static final String aat_Term_Language_Preferred_tag = "Preferred";
    public static final String aat_Term_Language_Preferred_Undetermined_val = "Undetermined";
    public static final String aat_Term_Language_Preferred_Preferred_val = "Preferred";
    public static final String aat_Term_Language_Preferred_Non_Preferred_val = "Non Preferred";

    public static final String aat_Term_Language_Term_Type_tag = "Term_Type";
    public static final String aat_Term_Language_Term_Type_Descriptor_val = "Descriptor";
    public static final String aat_Term_Language_Term_Type_Used_For_Term_val = "Used For Term";
    public static final String aat_Term_Language_Term_Type_Alternate_Descriptor_val = "Alternate Descriptor";

    public static final String aat_Term_Source_ID_tag = "Source_ID";

    public static final String aat_Descriptive_NotesWrapper_tag = "Descriptive_Notes";
    public static final String aat_Descriptive_Note_tag = "Descriptive_Note";
    public static final String aat_Descriptive_Note_Note_Text_tag = "Note_Text";
    public static final String aat_Note_Language_tag = "Note_Language";
    public static final String aat_Note_Source_ID_tag = "Source_ID";




    public static final String XML_rdf_RDF = "rdf:RDF";
    public static final String XML_skos_collection = "skos:Collection";

    public static final String XML_xml_lang = "xml:lang";
    public static final String XML_skos_concept = "skos:Concept";
    public static final String XML_rdf_about = "rdf:about";
    public static final String XML_skos_topConceptOf ="skos:topConceptOf";

    public static final String XML_skos_member = "skos:member";
    public static final String XML_rdf_resource = "rdf:resource";


    public static final String XML_skos_prefLabel = "skos:prefLabel";//text //xml:lang
    public static final String XML_dc_creator = "dc:creator";//text
    public static final String XML_dc_date = "dc:date";//text
    public static final String XML_skos_altLabel = "skos:altLabel";//text //xml:lang
    public static final String XML_skos_broader = "skos:broader";//rdf:resource
    public static final String XML_skos_inScheme = "skos:inScheme";//rdf:resource
    public static final String XML_skos_narrower = "skos:narrower";//rdf:resource
    public static final String XML_skos_Note = "skos:Note";//text
    public static final String XML_skos_related = "skos:related";//rdf:resource
    public static final String XML_skos_scopeNote = "skos:scopeNote"; //xml:lang


    
    public static final String LogFilesFolderName = "LogFiles";
    public static final String LMENU_TERMS = "LMENU_TERMS";
    public static final String LMENU_HIERARCHIES = "LMENU_HIERARCHIES";
    public static final String LMENU_FACETS = "LMENU_FACETS";
    public static final String LMENU_SOURCES = "LMENU_SOURCES";
    public static final String LMENU_STATISTICS = "LMENU_STATISTICS";
    public static final String LMENU_THESAURI = "LMENU_THESAURI";
    public static final String LMENU_DATABASE = "LMENU_DATABASE";
    public static final String LMENU_USERS = "LMENU_USERS";
    public static final String LMENU_HiddenActions_DIV = "THEMAS_HiddenActions_DIV";
    public static final String LMENU_HiddenSystemConfigurations_DIV = "THEMAS_HiddenSystemConfigurations_DIV";
    public static final String LMENU_HiddenFixData_DIV = "THEMAS_HiddenFixData_DIV";
    public static final String LMENU_NONE = "LMENU_NONE";

    //public static final int MAX_LOGINAM_LENGTH = 96;
    //public static final int MAX_COMMENT_LENGTH = 20000;

    public static final int CONCEPT_SCHEME = 0;
    public static final int FACET = 1;
    public static final int TOPCONCEPT = 2;
    public static final int HIERARCHY = 2;
    public static final int CONCEPT = 3;
    public static final int COLLECTION = 4;
    public static final int GUIDE_TERM = 4;
    public static final int SOURCE = 5;

    public static boolean DEBUG_MESSAGES_ENABLED = false;


    public static String referenceThesaurusSchemeName = "http://www.3d-coform.eu/PhysicalObjectTypes";
    public static boolean includeThesaurusNameInScheme = false;
    public static String SchemePrefix ="http://www.getty.edu/research/tools/vocabularies/aat";
    public static boolean filterBts_Nts_Rts  = false;
    //public static String THEMASStatusLang = "el";

    //public static final int MAX_LOGINAM_SIZE = 100;
    //public static final int MAX_COMMENT_SIZE = 19000;

    //output option supported for specific servlets in order to provide the output as xml stream
    //be carefull at the end of each servlet that supports it, not to update current session
    //because this request usually will have been submitted from a different application.
    //Also there is no meaning in keeping alive this session after the XMLStream has been produced
    //so it will be good practice to invalidate the session.
    public static final String XMLSTREAM = "XMLSTREAM";
    
    public static final String searchOperatorEquals = "=";
    public static final String searchOperatorTransliterationEquals = "transliteration=";
    public static final String searchOperatorNotTransliterationEquals = "!transliteration=";
    public static final String searchOperatorTransliterationContains = "transliteration~";
    public static final String searchOperatorNotTransliterationContains = "!transliteration~";
    public static final String searchOperatorContains = "~";
    public static final String searchOperatorNotContains = "!~";
    
}
