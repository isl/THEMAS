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

import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.ServletContext;

import javax.xml.xpath.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import neo4j_sisapi.StringObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//import java.util.regex.Matcher;
//import java.io.OutputStreamWriter;

/*---------------------------------------------------------------------
                            Parameters
-----------------------------------------------------------------------
class responsible for the collection of the application's context 
Parameters defined in web.xml
----------------------------------------------------------------------*/
public class Parameters {
    
    public static final boolean OnlyTopTermsHoldReferenceId = true;
    public static boolean TransliterationAsAttribute = false;
    public static boolean ShowTransliterationInAllXMLStream = false;
    public static boolean ShowReferenceURIalso = false;
    public static String UnclassifiedTermsLogicalname = "" ; 
    public static String UnclassifiedTermsFacetLogicalname = "" ; 
    // SVG graphs configuration
    public static boolean DEBUG = false;
    public static String PrimaryLang =""; //Prefix Of main Language
    public static String UILang =""; // el/en
    
    public static String TargetLocaleLang = "";
    public static String TargetLocaleCountry = "";//Prefix Of main Language
    
    public static String SVG_temporary_filesPath;
    public static String SVG_navbar_enabled;
    public static String SVG_CategoriesDirections_for_traverse;
    public static String SVG_CategoriesLanguages_for_traverse;
    public static String SVG_CategoriesStyles_for_traverse;
    public static String SVG_Hierarchy_name_lang;
    public static String SVG_Hierarchy_name_style;
    public static String SVG_ISA_scope;
    public static String SVG_CategoriesNames_for_legend;
    public static String Save_Results_Temp_Folder;
    public static String Save_Results_Folder;
    
    public static boolean AtRenameSaveOldNameAsUf = false;
    
    public static boolean ThesTeamEditOnlyCreatedByTerms = false;
    public static boolean CreatorInAlphabeticalTermDisplay = false;

    public static String TRANSLATION_SEPERATOR;
    public static ArrayList<String> CLASS_SET;
    public static String DELIMITER1;
    public static String DELIMITER2;
    
    public static String[] alphabetical_mode;
    public static String[] alphabetical_ignored_nodes;
    public static ArrayList<String> alphabetical_mode_PAGING_COUNT_NODES;
    
    public static boolean TransliterationsToLowerCase = false;
    public static HashMap<String,String> TransliterationsReplacements = new HashMap<>();
    
    public static HashMap<String,String> SupportedUILangCodes = new HashMap<>();
    
    
    public static ArrayList<Integer> TermModificationChecks;
    
    //split in 2 
    //public static boolean SEARCH_MODE_TONE_INSENSITIVE = false;
    public static boolean SEARCH_MODE_CASE_INSENSITIVE = false; // true in case the system's searches are done in tone and case insensitive mode
    public static boolean ENABLE_AUTOMATIC_BACKUPS = true; // turning to false at first successfull login is reached.
    public static int AUTOMATIC_BACKUPS_START_HOUR;
    public static int AUTOMATIC_BACKUPS_START_MIN;
    public static int AUTOMATIC_BACKUPS_START_SEC;
    //public static int AUTOMATIC_BACKUPS_START_AM_PM;
    public static int AUTOMATIC_BACKUPS_HOURS_INTERVAL;
    public static String AUTOMATIC_BACKUPS_DESCRIPTION;

    
    public static String ApplicationName = "";
    public static String LogFilePrefix = "";
    //public static String[] mailList;
    //public static String mailHost ="";
    public static String TaxonomicalCodeFormat="";


//    public static int RestartTransactions = 20;
    public static boolean SkipAutomaticBackups = false;


    public static String Status_Under_Construction ="";
    public static String Status_For_Approval ="";
    public static String Status_For_Insertion ="";
    public static String Status_For_Reinspection =""; // not used
    public static String Status_Approved ="";
    
    //the following structures will hold the language variants of status in all languages defined in SaveAll_Locale_And_Scripting.xml
    //key will be the lang code while value will be the specified by key language of the status.
    private static HashMap<String,String> Lang_DefinitionsForStatus_Under_Construction = new HashMap<>();
    private static HashMap<String,String> Lang_DefinitionsForStatus_For_Approval = new HashMap<>();
    private static HashMap<String,String> Lang_DefinitionsForStatus_For_Insertion = new HashMap<>();
    private static HashMap<String,String> Lang_DefinitionsForStatus_For_Reinspection = new HashMap<>();
    private static HashMap<String,String> Lang_DefinitionsForStatus_Approved = new HashMap<>();
    
    

    public static boolean FormatXML = true;

    public static String BaseRealPath ="";
    public static String SVG_CategoriesFrom_for_traverse="";
    public static String SVG_CategoriesNames_for_traverse = "";
    public static String alphabetical_From_Class ="";
    public static String alphabetical_Links ="";
    public static String CLASS_SET_INCLUDE ="";

    //public static Timer timer = null;
    
    /*---------------------------------------------------------------------
                            initParams()
    -----------------------------------------------------------------------
    INPUT: - ServletContext context: the context of the current servlet
    CALLED BY: all servlets in order to get the application's context Parameters defined in web.xml
    ----------------------------------------------------------------------*/                    
    public static void initParams(ServletContext context) {

        BaseRealPath = context.getRealPath("");
        initParams(BaseRealPath);
    }
    
    public static String getXmlElementForConfigAtRenameSaveOldNameAsUf(){
        if(Parameters.AtRenameSaveOldNameAsUf){
            return "<SaveOldNameAsUF>yes</SaveOldNameAsUF>";
        }
        else{
            return "<SaveOldNameAsUF>no</SaveOldNameAsUF>";
        }
    }
    
    public static HashMap<String, String> getAvailableUICodes(ServletContext context, boolean displayOnly){
        String rootPathString = context.getRealPath("");
        Path rootPath = Paths.get(rootPathString);
        String pathToXMLForPrimaryLang = rootPath.resolve("DBadmin").resolve("tms_db_admin_config_files").resolve("config.xml").toString();
        HashMap<String, String> retVals = new HashMap<>();
        try {

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(new File(pathToXMLForPrimaryLang));

                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList SupportedUILangCodesList = (NodeList)xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/SupportedUILangCodes/langcode", document,XPathConstants.NODESET);
                if(SupportedUILangCodesList!=null){
                    int howmanyItems = SupportedUILangCodesList.getLength();
                    for(int i=0; i< howmanyItems; i++){
                        
                        String langcode = xpath.evaluate("./text()", SupportedUILangCodesList.item(i));
                        String mapcode = xpath.evaluate("./@mapcode", SupportedUILangCodesList.item(i));
                        String displayStr = xpath.evaluate("./@display", SupportedUILangCodesList.item(i));
                        
                        
                        if(displayOnly == false ){
                                                
                            if(mapcode!=null && mapcode.trim().length()>0){
                                mapcode = mapcode.trim().toLowerCase();
                            }

                            if(langcode!=null && langcode.trim().length()>0){
                                langcode = langcode.trim().toLowerCase();
                            }                        
                            retVals.put(langcode, mapcode);
                        }
                        else if(displayStr!=null && displayStr.trim().toLowerCase().equals("yes")){
                            String displayTextStr = xpath.evaluate("./@displayText", SupportedUILangCodesList.item(i));
                            retVals.put(displayTextStr, mapcode);
                        }
                    }
                }
        }catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        return retVals;
    }
    
    public static void initParams(String basePathString){
        Path basePath = Paths.get(basePathString);
        try {

            BaseRealPath = basePathString;

            String pathToXMLForPrimaryLang = basePath.resolve("DBadmin").resolve("tms_db_admin_config_files").resolve("config.xml").toString();
            try {

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(new File(pathToXMLForPrimaryLang));

                XPath xpath = XPathFactory.newInstance().newXPath();
                Parameters.PrimaryLang = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/PrimaryLanguagePrefix[1]", document);

                
                Parameters.ThesTeamEditOnlyCreatedByTerms = false;
                String boolValStr = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/UserRolesConfigs/ThesaurusTeam/EditOnlyCreatedByTerms[1]", document);
                
                if(boolValStr.toLowerCase().equals("true")||boolValStr.toLowerCase().equals("yes")){
                    Parameters.ThesTeamEditOnlyCreatedByTerms = true;
                }
                else{
                    Parameters.ThesTeamEditOnlyCreatedByTerms = false;
                }
                
                
                Parameters.CreatorInAlphabeticalTermDisplay = false;
                String boolValStr_2 = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/CreatorInAlphabeticalTerm/DisplayCreator[1]", document);
                
                if(boolValStr_2.toLowerCase().equals("true")||boolValStr_2.toLowerCase().equals("yes")){
                    Parameters.CreatorInAlphabeticalTermDisplay = true;
                }
                else{
                    Parameters.CreatorInAlphabeticalTermDisplay = false;
                }
                
                String boolValStr_3 = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/BehaviorConfigs/AtRenameStoreOldNameAsUf[1]", document);
                if(boolValStr_3.toLowerCase().equals("true")||boolValStr_3.toLowerCase().equals("yes")){
                    Parameters.AtRenameSaveOldNameAsUf = true;
                }
                else{
                    Parameters.AtRenameSaveOldNameAsUf = false;
                }
                
                UnclassifiedTermsLogicalname = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/UnclassifiedTermsHierarchyName[1]", document);
                
                UnclassifiedTermsFacetLogicalname = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/UnclassifiedTermsFacetName[1]", document);
                
                
                String boolValStr_4 = xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/BehaviorConfigs/Transliterations/@toLowerCase[1]", document);
                if(boolValStr_4.toLowerCase().equals("true")||boolValStr_4.toLowerCase().equals("yes")){
                    Parameters.TransliterationsToLowerCase = true;
                }
                else{
                    Parameters.TransliterationsToLowerCase = false;
                }
                
                
                NodeList TransltiterationGroups = (NodeList)xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/BehaviorConfigs/Transliterations/ReplacementGroup", document,XPathConstants.NODESET);
                if(TransltiterationGroups!=null){
                    int howmanyGroups = TransltiterationGroups.getLength();
                    for(int i=0; i< howmanyGroups; i++){
                        
                        
                        String delimeter = xpath.evaluate("./Source/@delimeterChar", TransltiterationGroups.item(i));
                        String replaceWhat = xpath.evaluate("./Source", TransltiterationGroups.item(i));
                        String replaceWith =xpath.evaluate("./Replacement", TransltiterationGroups.item(i));
                        
                        Parameters.TransliterationsReplacements.put(replaceWhat.replace(delimeter, ""), replaceWith==null?"":replaceWith);
                    }
                }
                
                NodeList SupportedUILangCodesList = (NodeList)xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/SupportedUILangCodes/langcode", document,XPathConstants.NODESET);
                if(SupportedUILangCodesList!=null){
                    int howmanyItems = SupportedUILangCodesList.getLength();
                    for(int i=0; i< howmanyItems; i++){
                        
                        String langcode = xpath.evaluate("./text()", SupportedUILangCodesList.item(i));
                        String mapcode = xpath.evaluate("./@mapcode", SupportedUILangCodesList.item(i));
                                                
                        if(mapcode!=null && mapcode.trim().length()>0){
                            mapcode = mapcode.trim().toLowerCase();
                        }
                        
                        if(langcode!=null && langcode.trim().length()>0){
                            langcode = langcode.trim().toLowerCase();
                        }
                        
                        Parameters.SupportedUILangCodes.put(langcode, mapcode);
                    }
                }
                
                
                ArrayList<String> permittedClassesFromXml = new ArrayList<>();
                NodeList classesPermitted = (NodeList)xpath.evaluate("TMS_DB_ADMIN_COFIGURATIONS/UserRolesConfigs/ReaderPermittedServlets/ClassName", document,XPathConstants.NODESET);
                if(classesPermitted!=null){
                    int howmanyClasses = classesPermitted.getLength();
                    for(int i=0; i< howmanyClasses; i++){
                        permittedClassesFromXml.add(classesPermitted.item(i).getTextContent());
                    }
                }
                Users.UserInfoClass.initializeAccessControlStructures(permittedClassesFromXml);
                
                
                

            } catch (Exception e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
                Utils.StaticClass.handleException(e);
            }

            String webXMLPath = basePath.resolve("WEB-INF").resolve("web.xml").toString();
            
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(webXMLPath));

            XPath xpath = XPathFactory.newInstance().newXPath();

            
            SVG_CategoriesFrom_for_traverse=xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesFrom_for_traverse']/param-value[1]", document);
            SVG_CategoriesNames_for_traverse = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesNames_for_traverse']/param-value[1]", document);
            alphabetical_From_Class =xpath.evaluate("web-app/context-param[param-name='alphabetical_From_Class']/param-value[1]", document);
            alphabetical_Links =xpath.evaluate("web-app/context-param[param-name='alphabetical_Links']/param-value[1]", document);
            CLASS_SET_INCLUDE =xpath.evaluate("web-app/context-param[param-name='CLASS_SET_INCLUDE']/param-value[1]", document);
            
            UILang = xpath.evaluate("web-app/context-param[param-name='UILanguage']/param-value[1]", document);

            
            TargetLocaleLang = xpath.evaluate("web-app/context-param[param-name='LocaleLanguage']/param-value[1]", document);
            TargetLocaleCountry = xpath.evaluate("web-app/context-param[param-name='LocaleCountry']/param-value[1]", document);
            //TargetLocale = new Locale(targetLocaleLang, targetLocaleCountry);
            //separated web-app/display-name from static variable ApplicationName 
            //because web-app/display-name may also be used in e.g. tomcat manager for short description
            //ApplicationName = xpath.evaluate("web-app/display-name", document);
            ApplicationName = xpath.evaluate("web-app/context-param[param-name='appname']/param-value[1]", document);
            LogFilePrefix = ApplicationName + " Logs: ";
            
            
            // SVG graphs configuration
            SVG_temporary_filesPath = xpath.evaluate("web-app/context-param[param-name='SVG_temporary_filesPath']/param-value[1]", document);
            SVG_navbar_enabled = xpath.evaluate("web-app/context-param[param-name='SVG_navbar_enabled']/param-value[1]", document);
            SVG_CategoriesDirections_for_traverse = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesDirections_for_traverse']/param-value[1]", document);
            SVG_CategoriesNames_for_traverse =  xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesNames_for_traverse']/param-value[1]", document);
            SVG_CategoriesFrom_for_traverse = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesFrom_for_traverse']/param-value[1]", document);
            SVG_CategoriesLanguages_for_traverse = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesLanguages_for_traverse']/param-value[1]", document);
            SVG_CategoriesStyles_for_traverse = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesStyles_for_traverse']/param-value[1]", document);
            SVG_Hierarchy_name_lang = xpath.evaluate("web-app/context-param[param-name='SVG_Hierarchy_name_lang']/param-value[1]", document);
            SVG_Hierarchy_name_style = xpath.evaluate("web-app/context-param[param-name='SVG_Hierarchy_name_style']/param-value[1]", document);
            SVG_ISA_scope = xpath.evaluate("web-app/context-param[param-name='SVG_ISA_scope']/param-value[1]", document);
            SVG_CategoriesNames_for_legend = xpath.evaluate("web-app/context-param[param-name='SVG_CategoriesNames_for_legend']/param-value[1]", document);


            Save_Results_Folder = xpath.evaluate("web-app/context-param[param-name='Save_Results_Folder']/param-value[1]", document);
            Save_Results_Temp_Folder = xpath.evaluate("web-app/context-param[param-name='Save_Results_Temp_Folder']/param-value[1]", document);
            TRANSLATION_SEPERATOR = xpath.evaluate("web-app/context-param[param-name='TRANSLATION_SEPERATOR']/param-value[1]", document);

            
            DELIMITER1 = xpath.evaluate("web-app/context-param[param-name='DELIMITER1']/param-value[1]", document);
            DELIMITER2 = xpath.evaluate("web-app/context-param[param-name='DELIMITER2']/param-value[1]", document);

            String TempREADSTR = xpath.evaluate("web-app/context-param[param-name='alphabetical_mode']/param-value[1]", document);
            if (TempREADSTR.split(DELIMITER1).length > 0) {
                String[] tempArray1 = TempREADSTR.split(DELIMITER1);
                alphabetical_mode = new String[tempArray1.length];
                for (int i = 0; i < tempArray1.length; i++) {
                    alphabetical_mode[i] = tempArray1[i];
                }
            }

            alphabetical_From_Class = xpath.evaluate("web-app/context-param[param-name='alphabetical_From_Class']/param-value[1]", document);
            alphabetical_Links = xpath.evaluate("web-app/context-param[param-name='alphabetical_Links']/param-value[1]", document);
            alphabetical_mode_PAGING_COUNT_NODES = new ArrayList<String>();
            TempREADSTR = xpath.evaluate("web-app/context-param[param-name='alphabetical_mode_PAGING_COUNT_NODES']/param-value[1]", document);
            if (TempREADSTR.split(DELIMITER1).length > 0) {
                String[] tempArray1 = TempREADSTR.split(DELIMITER1);
                for (int i = 0; i < tempArray1.length; i++) {
                    alphabetical_mode_PAGING_COUNT_NODES.add(tempArray1[i]);
                }
            }
            alphabetical_mode_PAGING_COUNT_NODES.trimToSize();

            TempREADSTR = xpath.evaluate("web-app/context-param[param-name='alphabetical_ignored_nodes']/param-value[1]", document);
            if (TempREADSTR.split(DELIMITER1).length > 0) {
                String[] tempArray1 = TempREADSTR.split(DELIMITER1);
                alphabetical_ignored_nodes = new String[tempArray1.length];
                for (int i = 0; i < tempArray1.length; i++) {
                    alphabetical_ignored_nodes[i] = tempArray1[i];
                }
            }

            CLASS_SET = new ArrayList<String>();
            TempREADSTR = xpath.evaluate("web-app/context-param[param-name='CLASS_SET']/param-value[1]", document);
            if (TempREADSTR.split(DELIMITER1).length > 0) {
                String[] tempArray1 = TempREADSTR.split(DELIMITER1);
                for (int i = 0; i < tempArray1.length; i++) {
                    if (!CLASS_SET.contains(tempArray1[i])) {
                        CLASS_SET.add(tempArray1[i]);
                    }
                }
            }
            CLASS_SET.trimToSize();
            String modifyTermChecks = xpath.evaluate("web-app/context-param[param-name='TermModificationChecks']/param-value[1]", document);
            TermModificationChecks = new ArrayList<Integer>();
            if (modifyTermChecks != null) {

                String[] termArray = modifyTermChecks.split(DELIMITER1);
                for (int i = 0; i < termArray.length; i++) {
                    TermModificationChecks.add(Integer.parseInt(termArray[i]));
                }
            }

            CLASS_SET_INCLUDE = xpath.evaluate("web-app/context-param[param-name='CLASS_SET_INCLUDE']/param-value[1]", document);

            //SEARCH_MODE_TONE_INSENSITIVE = xpath.evaluate("web-app/context-param[param-name='SEARCH_MODE_TONE_INSENSITIVE']/param-value[1]", document).equals("true");
            SEARCH_MODE_CASE_INSENSITIVE = xpath.evaluate("web-app/context-param[param-name='SEARCH_MODE_CASE_INSENSITIVE']/param-value[1]", document).equals("true");

            String automaticBackupsStartTimeStr = xpath.evaluate("web-app/context-param[param-name='Automatic_Backups_Next_Day_Start_Time']/param-value[1]", document);
            String[] automaticBackupsStartTimeParts = automaticBackupsStartTimeStr.split(DELIMITER1);
            AUTOMATIC_BACKUPS_START_HOUR = Integer.parseInt(automaticBackupsStartTimeParts[0]);
            AUTOMATIC_BACKUPS_START_MIN = Integer.parseInt(automaticBackupsStartTimeParts[1]);
            AUTOMATIC_BACKUPS_START_SEC = Integer.parseInt(automaticBackupsStartTimeParts[2]);
            /*if(automaticBackupsStartTimeParts[3].compareTo("AM")==0){
            AUTOMATIC_BACKUPS_START_AM_PM = Calendar.AM;
            }
            else{
            AUTOMATIC_BACKUPS_START_AM_PM = Calendar.PM;
            }
             */
            AUTOMATIC_BACKUPS_HOURS_INTERVAL = Integer.parseInt(xpath.evaluate("web-app/context-param[param-name='Automatic_Backups_Hours_Interval']/param-value[1]", document));
            AUTOMATIC_BACKUPS_DESCRIPTION = xpath.evaluate("web-app/context-param[param-name='Automatic_Backups_Description']/param-value[1]", document);


            String SkipAutomaticBackupsStr = xpath.evaluate("web-app/context-param[param-name='SkipAutomaticBackups']/param-value[1]", document);
            if (SkipAutomaticBackupsStr.compareTo("true") == 0) {
                SkipAutomaticBackups = true;
            }

            String doNotFormatXML = xpath.evaluate("web-app/context-param[param-name='FormatXML']/param-value[1]", document);
            if (doNotFormatXML != null && doNotFormatXML.compareTo("false") == 0) {
                FormatXML = false;
            }

            String DebugMode = xpath.evaluate("web-app/context-param[param-name='DebugMode']/param-value[1]", document);
            if (DebugMode.compareTo("true") == 0) {
                DEBUG = true;
            }

            /*
            String TempMailListREADSTR = xpath.evaluate("web-app/context-param[param-name='mailList']/param-value[1]", document);
            if (TempMailListREADSTR.split(DELIMITER1).length > 0) {
                String[] tempArray1 = TempMailListREADSTR.split(DELIMITER1);
                mailList = new String[tempArray1.length];
                for (int i = 0; i < tempArray1.length; i++) {
                    mailList[i] = tempArray1[i];
                }
            }
            mailHost = xpath.evaluate("web-app/context-param[param-name='mailHost']/param-value[1]", document);
             * 
             */
            TaxonomicalCodeFormat = xpath.evaluate("web-app/context-param[param-name='TaxonomicalCodeFormat']/param-value[1]", document);





        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }



        String pathToXMLForStatuses = basePath.resolve("translations").resolve("SaveAll_Locale_And_Scripting.xml").toString();
        try {

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(pathToXMLForStatuses));

            XPath xpath = XPathFactory.newInstance().newXPath();
            String lang = Parameters.UILang.toLowerCase();

            Parameters.Status_For_Insertion = xpath.evaluate("root/common/statuses/forinsertion/option[@lang='" + lang + "']", document);
            Parameters.Status_Under_Construction = xpath.evaluate("root/common/statuses/underconstruction/option[@lang='" + lang + "']", document);
            Parameters.Status_For_Approval = xpath.evaluate("root/common/statuses/underapproval/option[@lang='" + lang + "']", document);
            Parameters.Status_For_Reinspection = xpath.evaluate("root/common/statuses/forreinspection/option[@lang='" + lang + "']", document);
            Parameters.Status_Approved = xpath.evaluate("root/common/statuses/approved/option[@lang='" + lang + "']", document);


            NodeList LangsFor_StautsFI = (NodeList)xpath.evaluate("root/common/statuses/forinsertion/option", document,XPathConstants.NODESET);
            if(LangsFor_StautsFI!=null){
                int howmanyItems = LangsFor_StautsFI.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_StautsFI.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_StautsFI.item(i));

                    Parameters.Lang_DefinitionsForStatus_For_Insertion.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_StautsUC = (NodeList)xpath.evaluate("root/common/statuses/underconstruction/option", document,XPathConstants.NODESET);
            if(LangsFor_StautsUC!=null){
                int howmanyItems = LangsFor_StautsUC.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_StautsUC.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_StautsUC.item(i));

                    Parameters.Lang_DefinitionsForStatus_Under_Construction.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_StautsFA = (NodeList)xpath.evaluate("root/common/statuses/underapproval/option", document,XPathConstants.NODESET);
            if(LangsFor_StautsFA!=null){
                int howmanyItems = LangsFor_StautsFA.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_StautsFA.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_StautsFA.item(i));

                    Parameters.Lang_DefinitionsForStatus_For_Approval.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_StautsFR = (NodeList)xpath.evaluate("root/common/statuses/forreinspection/option", document,XPathConstants.NODESET);
            if(LangsFor_StautsFR!=null){
                int howmanyItems = LangsFor_StautsFR.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_StautsFR.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_StautsFR.item(i));

                    Parameters.Lang_DefinitionsForStatus_For_Reinspection.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_StautsApproved = (NodeList)xpath.evaluate("root/common/statuses/approved/option", document,XPathConstants.NODESET);
            if(LangsFor_StautsApproved!=null){
                int howmanyItems = LangsFor_StautsApproved.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_StautsApproved.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_StautsApproved.item(i));

                    Parameters.Lang_DefinitionsForStatus_Approved.put(langcode, displayVal);
                }
            }
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }



        String pathToXMLForSearchCriteria = basePath.resolve("translations").resolve("searchcriteria.xml").toString();
        try{

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(pathToXMLForSearchCriteria));

            XPath xpath = XPathFactory.newInstance().newXPath();
            String lang = Parameters.UILang;

            //XPathExpression expr = xpath.compile("/locale/searchcriteria/inputstrs/*");
            //Object result = expr.evaluate(document, XPathConstants.NODESET);


            
            SearchCriteria.inputStrs = new HashMap<>();
            
            NodeList LangsFor_Inputs = (NodeList)xpath.evaluate("/locale/searchcriteria/inputstrs/*", document,XPathConstants.NODESET);
            if(LangsFor_Inputs!=null){
                int howmanyItems = LangsFor_Inputs.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String keyword = LangsFor_Inputs.item(i).getNodeName().toLowerCase();
                    SearchCriteria.inputStrs.put(keyword, new HashMap<>());
                    NodeList LangsFor_CurrentKeyword = (NodeList)xpath.evaluate("./option", LangsFor_Inputs.item(i),XPathConstants.NODESET);
                    int howmanyLangs = LangsFor_CurrentKeyword.getLength();
                    
                    for(int k=0; k< howmanyLangs; k++){
                        String langcode = xpath.evaluate("./@lang", LangsFor_CurrentKeyword.item(k));
                        String displayVal = xpath.evaluate("./text()", LangsFor_CurrentKeyword.item(k));
                        SearchCriteria.inputStrs.get(keyword).put(langcode,displayVal);
                    }                    
                }
            }
            
            int howmany= 0;
            
            XPathExpression expr = xpath.compile("/locale/searchcriteria/termDefaultOutputs/keyword");
            Object result = expr.evaluate(document, XPathConstants.NODESET);

            NodeList nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.termsDefaultOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.termsDefaultOutput[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/quicksearch/inputStrs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.quickSearchInputStrs = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.quickSearchInputStrs[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/quicksearch/outputs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.quickSearchOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.quickSearchOutput[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/hierarchyDefaultOutputs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.hierarchyDefaultOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.hierarchyDefaultOutput[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/facetDefaultOutputs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.facetDefaultOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.facetDefaultOutput[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/sourcesDefaultOutputs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.sourcesDefaultOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.sourcesDefaultOutput[i] = nodes.item(i).getTextContent();
            }

            expr=null;
            result = null;
            nodes = null;
            howmany= 0;
            expr = xpath.compile("/locale/searchcriteria/usersDefaultOutputs/keyword");
            result = expr.evaluate(document, XPathConstants.NODESET);

            nodes = (NodeList) result;
            howmany = nodes.getLength();
            SearchCriteria.userssDefaultOutput = new String[howmany];
            for (int i = 0; i < howmany; i++) {
                SearchCriteria.userssDefaultOutput[i] = nodes.item(i).getTextContent();
            }

            
            SearchCriteria.termSpecialInputs = new HashMap<>();
            NodeList LangsFor_TermSpecialInputs = (NodeList)xpath.evaluate("/locale/searchreport/inputUIDifferentiations/mode/term/input", document,XPathConstants.NODESET);
            if(LangsFor_TermSpecialInputs!=null){
                int howmanyItems = LangsFor_TermSpecialInputs.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String keyword = xpath.evaluate("./@keyword", LangsFor_TermSpecialInputs.item(i));
                    SearchCriteria.termSpecialInputs.put(keyword,new HashMap<>());
                    NodeList LangsFor_CurrentKeyword = (NodeList)xpath.evaluate("./option", LangsFor_TermSpecialInputs.item(i),XPathConstants.NODESET);
                    int howmanyLangs = LangsFor_CurrentKeyword.getLength();
                    
                    for(int k=0; k< howmanyLangs; k++){
                        String langcode = xpath.evaluate("./@lang", LangsFor_CurrentKeyword.item(k));
                        String displayVal = xpath.evaluate("./text()", LangsFor_CurrentKeyword.item(k));
                        SearchCriteria.termSpecialInputs.get(keyword).put(langcode,displayVal);
                    }                    
                }
            }
            
            SearchCriteria.hierarchySpecialInputs = new HashMap<>();
            NodeList LangsFor_HierarchySpecialInputs = (NodeList)xpath.evaluate("/locale/searchreport/inputUIDifferentiations/mode/hierarchy/input", document,XPathConstants.NODESET);
            if(LangsFor_HierarchySpecialInputs!=null){
                int howmanyItems = LangsFor_HierarchySpecialInputs.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String keyword = xpath.evaluate("./@keyword", LangsFor_HierarchySpecialInputs.item(i));
                    SearchCriteria.hierarchySpecialInputs.put(keyword,new HashMap<>());
                    NodeList LangsFor_CurrentKeyword = (NodeList)xpath.evaluate("./option", LangsFor_HierarchySpecialInputs.item(i),XPathConstants.NODESET);
                    int howmanyLangs = LangsFor_CurrentKeyword.getLength();
                    
                    for(int k=0; k< howmanyLangs; k++){
                        String langcode = xpath.evaluate("./@lang", LangsFor_CurrentKeyword.item(k));
                        String displayVal = xpath.evaluate("./text()", LangsFor_CurrentKeyword.item(k));
                        SearchCriteria.hierarchySpecialInputs.get(keyword).put(langcode,displayVal);
                    }                    
                }
            }
            
            SearchCriteria.facetSpecialInputs = new HashMap<>();
            NodeList LangsFor_FacetSpecialInputs = (NodeList)xpath.evaluate("/locale/searchreport/inputUIDifferentiations/mode/facet/input", document,XPathConstants.NODESET);
            if(LangsFor_FacetSpecialInputs!=null){
                int howmanyItems = LangsFor_FacetSpecialInputs.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String keyword = xpath.evaluate("./@keyword", LangsFor_FacetSpecialInputs.item(i));
                    SearchCriteria.facetSpecialInputs.put(keyword,new HashMap<>());
                    NodeList LangsFor_CurrentKeyword = (NodeList)xpath.evaluate("./option", LangsFor_FacetSpecialInputs.item(i),XPathConstants.NODESET);
                    int howmanyLangs = LangsFor_CurrentKeyword.getLength();
                    
                    for(int k=0; k< howmanyLangs; k++){
                        String langcode = xpath.evaluate("./@lang", LangsFor_CurrentKeyword.item(k));
                        String displayVal = xpath.evaluate("./text()", LangsFor_CurrentKeyword.item(k));
                        SearchCriteria.facetSpecialInputs.get(keyword).put(langcode,displayVal);
                    }                    
                }
            }

            SearchCriteria.sourceSpecialInputs = new HashMap<>();
            NodeList LangsFor_SourceSpecialInputs = (NodeList)xpath.evaluate("/locale/searchreport/inputUIDifferentiations/mode/source/input", document,XPathConstants.NODESET);
            if(LangsFor_SourceSpecialInputs!=null){
                int howmanyItems = LangsFor_SourceSpecialInputs.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String keyword = xpath.evaluate("./@keyword", LangsFor_SourceSpecialInputs.item(i));
                    SearchCriteria.sourceSpecialInputs.put(keyword,new HashMap<>());
                    NodeList LangsFor_CurrentKeyword = (NodeList)xpath.evaluate("./option", LangsFor_SourceSpecialInputs.item(i),XPathConstants.NODESET);
                    int howmanyLangs = LangsFor_CurrentKeyword.getLength();
                    
                    for(int k=0; k< howmanyLangs; k++){
                        String langcode = xpath.evaluate("./@lang", LangsFor_CurrentKeyword.item(k));
                        String displayVal = xpath.evaluate("./text()", LangsFor_CurrentKeyword.item(k));
                        SearchCriteria.sourceSpecialInputs.get(keyword).put(langcode,displayVal);
                    }                    
                }
            }
            
            SearchCriteria.showAllString =  xpath.evaluate("locale/searchreport/showall/option[@lang='"+lang+"']", document);

            SearchCriteria.andDisplayOperator =  xpath.evaluate("locale/searchreport/andDisplayOperator/option[@lang='"+lang+"']", document);
            SearchCriteria.orDisplayOperator  =  xpath.evaluate("locale/searchreport/orDisplayOperator/option[@lang='"+lang+"']", document);
            
            
            NodeList LangsFor_ShowAllString = (NodeList)xpath.evaluate("locale/searchreport/showall/option", document,XPathConstants.NODESET);
            if(LangsFor_ShowAllString!=null){
                int howmanyItems = LangsFor_ShowAllString.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_ShowAllString.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_ShowAllString.item(i));

                    SearchCriteria.Lang_DefinitionsFor_SearchCriteria_ShowAll.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_AndOperator = (NodeList)xpath.evaluate("locale/searchreport/andDisplayOperator/option", document,XPathConstants.NODESET);
            if(LangsFor_AndOperator!=null){
                int howmanyItems = LangsFor_AndOperator.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_AndOperator.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_AndOperator.item(i));

                    SearchCriteria.Lang_DefinitionsFor_SearchCriteria_And.put(langcode, displayVal);
                }
            }
            
            NodeList LangsFor_OROperator = (NodeList)xpath.evaluate("locale/searchreport/orDisplayOperator/option", document,XPathConstants.NODESET);
            if(LangsFor_OROperator!=null){
                int howmanyItems = LangsFor_OROperator.getLength();
                for(int i=0; i< howmanyItems; i++){

                    String langcode = xpath.evaluate("./@lang", LangsFor_OROperator.item(i));
                    String displayVal = xpath.evaluate("./text()", LangsFor_OROperator.item(i));

                    SearchCriteria.Lang_DefinitionsFor_SearchCriteria_Or.put(langcode, displayVal);
                }
            }
        }
        catch(Exception e){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Translate Error: " + e.getMessage());
            Utils.StaticClass.handleException(e);
        }
    }
    
    public static String getStatusRepresentation_ForDisplay(String targetStatus, UserInfoClass SessionUserInfo){
     
        String targetLang = (SessionUserInfo!=null && SessionUserInfo.UILang!=null)?SessionUserInfo.UILang : Parameters.UILang;
        String retVal = "";
        
        if(targetStatus.equals(Parameters.Status_Under_Construction)){
            retVal = Parameters.Lang_DefinitionsForStatus_Under_Construction.containsKey(targetLang)? Parameters.Lang_DefinitionsForStatus_Under_Construction.get(targetLang) :  Parameters.Status_Under_Construction;
        }
        else if(targetStatus.equals(Parameters.Status_Approved)){         
            retVal = Parameters.Lang_DefinitionsForStatus_Approved.containsKey(targetLang)? Parameters.Lang_DefinitionsForStatus_Approved.get(targetLang) :  Parameters.Status_Approved;
        }
        else if(targetStatus.equals(Parameters.Status_For_Approval)){
            retVal = Parameters.Lang_DefinitionsForStatus_For_Approval.containsKey(targetLang)? Parameters.Lang_DefinitionsForStatus_For_Approval.get(targetLang) :  Parameters.Status_For_Approval;
        }
        else if(targetStatus.equals(Parameters.Status_For_Insertion)){
            retVal = Parameters.Lang_DefinitionsForStatus_For_Insertion.containsKey(targetLang)? Parameters.Lang_DefinitionsForStatus_For_Insertion.get(targetLang) :  Parameters.Status_For_Insertion;
        }
        else if(targetStatus.equals(Parameters.Status_For_Reinspection)){
            retVal = Parameters.Lang_DefinitionsForStatus_For_Reinspection.containsKey(targetLang)? Parameters.Lang_DefinitionsForStatus_For_Reinspection.get(targetLang) :  Parameters.Status_For_Reinspection;
        }
        
        return retVal;
                
    }
    
    public static String getStatusRepresentation_ForDB(String targetDisplayStatus, UserInfoClass SessionUserInfo){
     
        String targetLang = (SessionUserInfo!=null && SessionUserInfo.UILang!=null)?SessionUserInfo.UILang : Parameters.UILang;
        String retVal = "";
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject statusObj = new StringObject();
        if(targetDisplayStatus!=null && targetDisplayStatus.length()>0){
            if(Parameters.Lang_DefinitionsForStatus_Under_Construction.containsKey(targetLang)  && Parameters.Lang_DefinitionsForStatus_Under_Construction.get(targetLang).equals(targetDisplayStatus)){
                //retVal = Parameters.Status_Under_Construction;
                dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus, statusObj);
                return statusObj.getValue();
            }
            else if(Parameters.Lang_DefinitionsForStatus_Approved.containsKey(targetLang)  && Parameters.Lang_DefinitionsForStatus_Approved.get(targetLang).equals(targetDisplayStatus)){     
                dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, statusObj);
                return statusObj.getValue();
            }
            else if(Parameters.Lang_DefinitionsForStatus_For_Approval.containsKey(targetLang)  && Parameters.Lang_DefinitionsForStatus_For_Approval.get(targetLang).equals(targetDisplayStatus)){     
                dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus, statusObj);
                return statusObj.getValue();
            }
            else if(Parameters.Lang_DefinitionsForStatus_For_Insertion.containsKey(targetLang)  && Parameters.Lang_DefinitionsForStatus_For_Insertion.get(targetLang).equals(targetDisplayStatus)){     
                dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus, statusObj);
                return statusObj.getValue();
            }
            else if(Parameters.Lang_DefinitionsForStatus_For_Reinspection.containsKey(targetLang)  && Parameters.Lang_DefinitionsForStatus_For_Reinspection.get(targetLang).equals(targetDisplayStatus)){     
                dbtr.getThesaurusClass_StatusForReinspection(SessionUserInfo.selectedThesaurus, statusObj);
                return statusObj.getValue();
            }
            else {
                //check other languages                
                if(Parameters.Lang_DefinitionsForStatus_Under_Construction.containsValue(targetDisplayStatus)){
                    //retVal = Parameters.Status_Under_Construction;
                    dbtr.getThesaurusClass_StatusUnderConstruction(SessionUserInfo.selectedThesaurus, statusObj);
                    return statusObj.getValue();
                }
                else if(Parameters.Lang_DefinitionsForStatus_Approved.containsValue(targetDisplayStatus)){     
                    dbtr.getThesaurusClass_StatusApproved(SessionUserInfo.selectedThesaurus, statusObj);
                    return statusObj.getValue();
                }
                else if(Parameters.Lang_DefinitionsForStatus_For_Approval.containsValue(targetDisplayStatus)){     
                    dbtr.getThesaurusClass_StatusForApproval(SessionUserInfo.selectedThesaurus, statusObj);
                    return statusObj.getValue();
                }
                else if(Parameters.Lang_DefinitionsForStatus_For_Insertion.containsValue(targetDisplayStatus)){     
                    dbtr.getThesaurusClass_StatusForInsertion(SessionUserInfo.selectedThesaurus, statusObj);
                    return statusObj.getValue();
                }
                else if(Parameters.Lang_DefinitionsForStatus_For_Reinspection.containsValue(targetDisplayStatus)){     
                    dbtr.getThesaurusClass_StatusForReinspection(SessionUserInfo.selectedThesaurus, statusObj);
                    return statusObj.getValue();
                }
                else{
                    //nothing find return the same value
                    retVal = targetDisplayStatus;
                }
            }            
        }
        
        return retVal;                
    }

}