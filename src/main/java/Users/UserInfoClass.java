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

import java.util.*;


/*---------------------------------------------------------------------
                            UserInfoClass
-----------------------------------------------------------------------
Class for storing information 
1. for a specific THEMAS user keeped in 
\\WEB-INF\\WebAppUSERS.xml with syntax (g.e.):
	<user>
		<name>karam</name>
		<password>karam</password>
		<thesaurus group="READER">THES1</thesaurus>
		<thesaurus group="LIBRARY">THES2</thesaurus>
		<thesaurus group="THESAURUS_TEAM">THES3</thesaurus>
		<thesaurus group="THESAURUS_COMMITTEE">THES4</thesaurus>		
		<thesaurus group="ADMINISTRATOR"></thesaurus>				
		<description></description>
	</user>
2. for the authenticated THEMAS user of each session
----------------------------------------------------------------------*/
public class UserInfoClass {
    // <user> info found in WebAppUSERS.xml
    public String name;
    public String password;
    //String DBname;
    // parallel Vectors for tags (g.e.): 
    // <thesaurus group="READER">THES1</thesaurus>
    // <thesaurus group="LIBRARY">THES2</thesaurus>
    public Vector<String> thesaurusNames;
    public Vector<String> thesaurusGroups;
    public String description;
    
    // current session user info
    public String selectedThesaurus;
    public String userGroup; // "READER" or "LIBRARY" or "THESAURUS_TEAM" or "THESAURUS_COMMITTEE" or "ADMINISTRATOR"
    // SVG graphs configuration
    public String SVG_CategoriesFrom_for_traverse, SVG_CategoriesNames_for_traverse;
    // Alphabetical display configuration
    public String[] alphabetical_From_Class;
    public String[] alphabetical_Links;

    public Vector<Vector<String>> CLASS_SET_INCLUDE;

    /*---------------------------------------------------------------------
                                    UserInfoClass()
    ----------------------------------------------------------------------*/                
    public UserInfoClass() {
        name = "";
        password = "";
        thesaurusNames = new  Vector<String>();
        thesaurusGroups = new Vector<String>();
        description = "";
        selectedThesaurus = "";
        userGroup = "";
        SVG_CategoriesFrom_for_traverse = "";
        SVG_CategoriesNames_for_traverse = "";
        alphabetical_From_Class = null;
        alphabetical_Links = null;
        CLASS_SET_INCLUDE = null;
    }

    public UserInfoClass(UserInfoClass refUserInfo) {
        name = refUserInfo.name;
        password = refUserInfo.password;
        thesaurusNames = new Vector<String>();
        if(refUserInfo.thesaurusNames!=null){
            thesaurusNames.addAll(refUserInfo.thesaurusNames);
        }
        thesaurusGroups = new Vector<String>();
        if(refUserInfo.thesaurusGroups!=null){
            thesaurusGroups.addAll(refUserInfo.thesaurusGroups);
        }
        description = refUserInfo.description;
        selectedThesaurus = refUserInfo.selectedThesaurus;
        userGroup = refUserInfo.userGroup;
        SVG_CategoriesFrom_for_traverse = refUserInfo.SVG_CategoriesFrom_for_traverse;
        SVG_CategoriesNames_for_traverse = refUserInfo.SVG_CategoriesNames_for_traverse;

        if(refUserInfo.alphabetical_From_Class!=null){
            alphabetical_From_Class = new String[refUserInfo.alphabetical_From_Class.length];
            for(int i=0; i<alphabetical_From_Class.length; i++){
                alphabetical_From_Class[i]= refUserInfo.alphabetical_From_Class[i];
            }
        }
        
        if(refUserInfo.alphabetical_Links!=null){
            alphabetical_Links = new String[refUserInfo.alphabetical_Links.length];
            for(int i=0; i<alphabetical_Links.length; i++){
                alphabetical_Links[i]= refUserInfo.alphabetical_Links[i];
            }
        }

        CLASS_SET_INCLUDE = new Vector<Vector<String>>();

        if(refUserInfo.CLASS_SET_INCLUDE!=null){
            CLASS_SET_INCLUDE.addAll(refUserInfo.CLASS_SET_INCLUDE);
        }
    }
        
}


