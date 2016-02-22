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
package SVGproducer;

import Users.UserInfoClass;
import Utils.Parameters;
import javax.servlet.http.*;

/*-------------------------------------------------------------------
Class ReadConfig:
Used by all clients to get configuration information...
--------------------------------------------------------------------*/
public class ReadSVGConfig {
    private String DELIMITER;
    
    public String hierarchy_scope; // used ONLY by ProduceISAHierarchy - supernodes => get_all_superclasses - subnodes => get_all_subclasses
    public String navbar_enabled;
    public String[] sis_class_name;
    public String[] sis_category_name;
    public String[] sis_category_direction;
    public String[] sis_category_lang;
    public String[] sis_category_style;
    public String[] hierarchy_names;
    public String[] hierarchy_name_lang;
    public String[] hierarchy_name_style;

    /*-----------------------------------------------------------------------
                            InitParms()
    -------------------------------------------------------------------------
    FUNCTION: initializes local members
    CALLED_BY: ReadConfig()
    -------------------------------------------------------------------------*/
    private void InitParms() {
        DELIMITER = new String(":::");
        
        hierarchy_scope = new String();
        navbar_enabled = new String();
        sis_class_name = new String[50];
        sis_category_name = new String[50];
        sis_category_direction = new String[50];
        sis_category_lang  = new String[50];
        sis_category_style  = new String[50];
        hierarchy_names = new String[50];
        hierarchy_name_lang = new String[50];
        hierarchy_name_style = new String[50];
    }

    /*-----------------------------------------------------------------------
                                ReadConfig()
    -------------------------------------------------------------------------
    FUNCTION: basic CONSTRUCTOR of this class
    -------------------------------------------------------------------------*/
    ReadSVGConfig(UserInfoClass SessionUserInfo) {
        InitParms();
        
        navbar_enabled = Parameters.SVG_navbar_enabled;
        // get the SVG_CategoriesFrom_for_traverse and SVG_CategoriesNames_for_traverse
        // from the session attribute "SessionUser"
        
        sis_class_name = SessionUserInfo.SVG_CategoriesFrom_for_traverse.split(DELIMITER);
        sis_category_name = SessionUserInfo.SVG_CategoriesNames_for_traverse.split(DELIMITER);         
        
        sis_category_direction = Parameters.SVG_CategoriesDirections_for_traverse.split(DELIMITER); 
        sis_category_lang = Parameters.SVG_CategoriesLanguages_for_traverse.split(DELIMITER); 
        sis_category_style = Parameters.SVG_CategoriesStyles_for_traverse.split(DELIMITER); 
        hierarchy_name_lang = Parameters.SVG_Hierarchy_name_lang.split(DELIMITER); 
        hierarchy_name_style = Parameters.SVG_Hierarchy_name_style.split(DELIMITER); 
        hierarchy_scope = Parameters.SVG_ISA_scope;  
    }
}
