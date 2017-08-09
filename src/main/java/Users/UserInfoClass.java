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
    public String userGroup; // "READER" or "LIBRARY" or "THESAURUS_TEAM" or "THESAURUS_COMMITTEE" or "ADMINISTRATOR" or "EXTERNALREADER"
    // SVG graphs configuration
    public String SVG_CategoriesFrom_for_traverse, SVG_CategoriesNames_for_traverse;
    // Alphabetical display configuration
    public String[] alphabetical_From_Class;
    public String[] alphabetical_Links;

    public Vector<Vector<String>> CLASS_SET_INCLUDE;
    
    private static ArrayList<String> readerPermittedServletNames;
    
    public static void initializeAccessControlStructures(Vector<String> configValues){
        
        //comment out not existing servlets and servelts where access is allowed
        //leave only servlets where access is not allowed
        readerPermittedServletNames = new ArrayList<String>(configValues);
        /*
        readerPermittedServletNames = new ArrayList<>(Arrays.asList(new String[]
            {
//Admin_Thesaurus.Admin_Thesaurus.class.getName(),
//Admin_Thesaurus.CopyThesaurus.class.getName(),
//Admin_Thesaurus.CreateThesaurus.class.getName(),
//Admin_Thesaurus.DeleteThesaurus.class.getName(),
//Admin_Thesaurus.EditGuideTerms.class.getName(),
//Admin_Thesaurus.ExportData.class.getName(),
//Admin_Thesaurus.FixCurrentData.class.getName(),
//Admin_Thesaurus.ImportData.class.getName(),
//Admin_Thesaurus.MergeThesauri.class.getName(),
//DB_Admin.CreateDBbackup.class.getName(),
//DB_Admin.DBadmin.class.getName(),
//DB_Admin.FixDB.class.getName(),
//DB_Admin.RestoreDBbackup.class.getName(),
            
//not existing DisplayGraph.class.getName(),
//not existing ExportFacets.class.getName(),
            
//LoginAdmin.FixAdminData.class.getName(),
//LoginAdmin.HiddenActions.class.getName(),
//LoginAdmin.Lock_UnlockSystem.class.getName(),
LoginAdmin.LoginAdmin.class.getName(),
//LoginAdmin.Start_StopNeo4j.class.getName(),
//LoginAdmin.StartExportImportToXML.class.getName(),
//LoginAdmin.SystemConfigurations.class.getName(),
//LoginAdmin.Translations.class.getName(),
            
//not existing Logout.class.getName(),
//not existing ProduceSVGGraphServlet.class.getName(),
            
Servlets.AjaxDBQuery.class.getName(),
Servlets.CardOf_Facet.class.getName(),
Servlets.CardOf_Hierarchy.class.getName(),
Servlets.CardOf_Source.class.getName(),
Servlets.CardOf_Term.class.getName(),
Servlets.DownloadFile.class.getName(),
//Servlets.EditActions_Facet.class.getName(),
//Servlets.EditActions_Hierarchy.class.getName(),
//Servlets.EditActions_Source.class.getName(),
//Servlets.EditActions_Term.class.getName(),
//Servlets.EditDisplays_Facet.class.getName(),
//Servlets.EditDisplays_Hierarchy.class.getName(),
//Servlets.EditDisplays_Source.class.getName(),
//Servlets.EditDisplays_Term.class.getName(),
//Servlets.EditDisplays_User.class.getName(),
Servlets.GraphicalView.class.getName(),
Servlets.hierarchysTermsShortcuts.class.getName(),
Servlets.Index.class.getName(),
Servlets.Links.class.getName(),
//Servlets.MoveToHierarchy.class.getName(),
//Servlets.MoveToHierarchyResults.class.getName(),

//not existing Servlets.NewServlet.class.getName(),

Servlets.nicajax.class.getName(),
//Servlets.Preview_Available_Facets.class.getName(),
//Servlets.Rename_Facet.class.getName(),
//Servlets.Rename_Hierarchy.class.getName(),
//Servlets.Rename_Term.class.getName(),
//Servlets.RenameInfo_Term.class.getName(),
Servlets.SearchResults_Facets.class.getName(),
Servlets.SearchResults_Hierarchies.class.getName(),
Servlets.SearchResults_Sources.class.getName(),
Servlets.SearchResults_Terms.class.getName(),
Servlets.SearchResults_Terms_Alphabetical.class.getName(),
Servlets.SearchResults_Terms_Hierarchical.class.getName(),
Servlets.SearchResults_Terms_Systematic.class.getName(),
//Servlets.SearchResults_Users.class.getName(),
Servlets.Statistics.class.getName(),
Servlets.SystemIsUnderMaintenance.class.getName(),
//Servlets.UndoRenameResults.class.getName(),
Servlets.WaitForDownload.class.getName(),

//not existing test.class.getName(),


            }));
        */
        
    }

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
    
    private String getThesaurusGroupForSpecificThesaurus(String thesName){
        
        if(this.thesaurusNames.contains(thesName)){
            int index = this.thesaurusNames.indexOf(thesName);
            if(index >=0 && index < this.thesaurusGroups.size()){
                return this.thesaurusGroups.get(index);
            }
        }
        else if (this.thesaurusGroups.contains(Utils.ConstantParameters.Group_Administrator)){
            return Utils.ConstantParameters.Group_Administrator;
        }
        return "";
    }
    
    
    public boolean servletAccessControl(String className){
        boolean returnVal = true;
        
        String currentUserGroup = this.userGroup;//getThesaurusGroupForSpecificThesaurus(this.selectedThesaurus);
        
        if(currentUserGroup!=null){
            if(currentUserGroup.equals(Utils.ConstantParameters.Group_Reader)){
                if(readerPermittedServletNames.contains(className)){
                    return true;
                }
                else{
                    return false;
                }
            }
            else if(currentUserGroup.equals(Utils.ConstantParameters.Group_External_Reader)){
                if(readerPermittedServletNames.contains(className)){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        
        return returnVal;
    }
}


