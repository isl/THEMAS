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
import Users.UserInfoClass;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.servlet.http.*;
/**
 *
 * @author tzortzak
 */
public class SearchCriteria {
    
    public String CombineOperator;
    
    public ArrayList<String> input;
    public ArrayList<String> operator;
    public ArrayList<String> value;
    
    public ArrayList<String> output;
    
    public ArrayList<String> pagingNames; // eg for terms 4 displays are supported termsAlphabetical, termsSystematic termsHierarchical and termsResults
    public ArrayList<Integer> pagingValues;

    //following fields are initialized with Parameters
    public static HashMap<String,HashMap<String,String>> inputStrs = null; 
    public static String[] termsDefaultOutput = null;
    public static String[] quickSearchInputStrs = null;
    public static String[] quickSearchOutput = null;
    public static String[] hierarchyDefaultOutput = null;
    public static String[] facetDefaultOutput = null;
    public static String[] sourcesDefaultOutput = null;
    public static String[] userssDefaultOutput = null;

    public static String showAllString = "";
    public static String andDisplayOperator = "";
    public static String orDisplayOperator = "";
    
    public static HashMap<String,String> Lang_DefinitionsFor_SearchCriteria_ShowAll = new HashMap<>();
    public static HashMap<String,String> Lang_DefinitionsFor_SearchCriteria_And = new HashMap<>();
    public static HashMap<String,String> Lang_DefinitionsFor_SearchCriteria_Or = new HashMap<>();
    
    //for each keyword (first level key) Hold all translations with lang as second level key
    public static HashMap<String,HashMap<String,String>> termSpecialInputs = null;
    public static HashMap<String,HashMap<String,String>> hierarchySpecialInputs = null;
    public static HashMap<String,HashMap<String,String>> facetSpecialInputs = null;
    public static HashMap<String,HashMap<String,String>> sourceSpecialInputs = null;

    String QueryString;
    
    SearchCriteria(){
        CombineOperator ="";   
        QueryString ="";
        
        input    = new ArrayList<String>();
        operator = new ArrayList<String>();
        value    = new ArrayList<String>();
        
        output   = new ArrayList<String>();
        
        pagingNames  =  new ArrayList<String>();
        pagingValues =  new ArrayList<Integer>();

    }
    
    private String getAndOperatroDisplayString(String targetLang){
        String retVal = "";
        if(Lang_DefinitionsFor_SearchCriteria_And.containsKey(targetLang)){
            retVal = Lang_DefinitionsFor_SearchCriteria_And.get(targetLang);
        }
        else{
            retVal = SearchCriteria.andDisplayOperator;
        }
        return retVal;
    }
    
    private String getOrOperatroDisplayString(String targetLang){
        String retVal = "";
        if(Lang_DefinitionsFor_SearchCriteria_Or.containsKey(targetLang)){
            retVal = Lang_DefinitionsFor_SearchCriteria_Or.get(targetLang);
        }
        else{
            retVal = SearchCriteria.orDisplayOperator;
        }
        return retVal;
    }
    
    
    private String getShowAllString(String targetLang){
        String retVal = "";
        if(Lang_DefinitionsFor_SearchCriteria_ShowAll.containsKey(targetLang)){
            retVal = Lang_DefinitionsFor_SearchCriteria_ShowAll.get(targetLang);
        }
        else{
            retVal = SearchCriteria.showAllString;
        }
        return retVal;
    }
    
    public static SearchCriteria createSearchCriteriaObject(UserInfoClass SessionUserInfo, String targetSearchCriteriaMode, String targetSearchCriteriaValue,HttpServletRequest request,Utilities u) {

        SearchCriteria sc = new SearchCriteria();               
        
        String targetLang = (SessionUserInfo!=null && SessionUserInfo.UILang!=null)?SessionUserInfo.UILang : Parameters.UILang;

        try{
            String showAll = u.getDecodedParameterValue(request.getParameter("showAll")); 
            //String showAll = null;
            if (targetSearchCriteriaMode.equals("SearchCriteria_Terms")) {

               
                if (targetSearchCriteriaValue.equals("*")) {

                    //In this case View All icon was pressed from the left menu
                    sc.CombineOperator = "*";

                    sc.input.add("*");
                    sc.operator.add("*");
                    sc.value.add("*");

                    for(int i=0; i< termsDefaultOutput.length; i++){
                        sc.output.add(termsDefaultOutput[i]);
                    }
                    /*
                        sc.output.add("name");
                        sc.output.add("translations");
                        sc.output.add(ConstantParameters.bt_kwd);
                        sc.output.add(ConstantParameters.nt_kwd);
                        sc.output.add("topterm");
                    */

                } 
                else if (targetSearchCriteriaValue.equals("QuickSearch")) { // in case of Quick Search
                    // This case is like executing the following search:
                    // INPUT criteria:
                    // name ~ (contains) QuickSearchInputValue or
                    // translations ~ (contains) QuickSearchInputValue or
                    // rt ~ (contains) QuickSearchInputValue or
                    // uf ~ (contains) QuickSearchInputValue or
                    // OUTPUT fields: name - TR - BT - TT - RT - UF
                    
                    // get the QuickSearchInputValue
                    String QuickSearchInputValue = request.getParameter("QuickSearchInputValue");
                    QuickSearchInputValue = u.getDecodedParameterValue(QuickSearchInputValue);                    
                    // CombineOperator - always "or"
                    sc.CombineOperator = "or";                    
                    // INPUTs: name - TR - RT - UF

                    //String[] inputsArray = {"name", "translations", ConstantParameters.rt_kwd, "uf"};
                    String operator = ConstantParameters.searchOperatorContains;
                    for (int i=0; i < quickSearchInputStrs.length; i++) {
                        sc.input.add(quickSearchInputStrs[i]);
                        sc.operator.add(operator);
                        sc.value.add(QuickSearchInputValue);                        
                    }
                    // OUTPUTs: name - TR - BT - TT - RT - UF
                    //String[] outputsArray = {"name", "translations", ConstantParameters.bt_kwd, ConstantParameters.nt_kwd, "topterm", ConstantParameters.rt_kwd, "uf"};
                    sc.output = new ArrayList(Arrays.asList(quickSearchOutput));
                }                 
                else 
                if(showAll!= null && showAll.compareTo("all")==0){ // user selected Proboli olwn apo to interface twn kritiriwn kai meta epelekse output

                    sc.CombineOperator="all";
                    //output_term was spli in 1 - 2 - 3 - 4 in order to parse outputs column by column instead of row by row which is default behaviour
                    String[] temp4 = request.getParameterValues("output_term1");
                    String[] temp5 = request.getParameterValues("output_term2");
                    String[] temp6 = request.getParameterValues("output_term3");
                    String[] temp7 = request.getParameterValues("output_term4");
                    //String[] temp5 = request.getParameterValues("outputSel_term");
                    if(temp4!=null){
                        for(int i =0; i<temp4.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp4[i]));    
                        }
                    }
                    if(temp5!=null){
                        for(int i =0; i<temp5.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp5[i]));    
                        }
                    }
                    if(temp6!=null){
                        for(int i =0; i<temp6.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp6[i]));    
                        }
                    }
                    if(temp7!=null){
                        for(int i =0; i<temp7.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp7[i]));    
                        }
                    }
                    
                    /*
                    for(int i =0; i<temp5.length ;i++){
                        String tempVal = u.getDecodedParameterValue(temp5[i]);
                        if(!sc.output.contains(tempVal))
                            if(tempVal.trim().length() != 0)
                                sc.output.add(tempVal);    

                    }*/
                }
                else
                if (targetSearchCriteriaValue.equals("parseCriteria")) { 

                    sc.CombineOperator = request.getParameter("operator_term");

                    String[] temp1 = request.getParameterValues("input_term");
                    String[] temp2 = request.getParameterValues("op_term");
                    String[] temp3 = request.getParameterValues("inputvalue_term");

                    String[] temp4 = request.getParameterValues("output_term1");
                    String[] temp5 = request.getParameterValues("output_term2");
                    String[] temp6 = request.getParameterValues("output_term3");
                    String[] temp7 = request.getParameterValues("output_term4");
                    //String[] temp5 = request.getParameterValues("outputSel_term");

                    
                    for(int i =0; i< temp1.length;i++){
                        sc.input.add(u.getDecodedParameterValue(temp1[i]));
                        sc.operator.add(u.getDecodedParameterValue(temp2[i]));
                        sc.value.add(u.getDecodedParameterValue(temp3[i]));                                        
                    }

                    if(temp4!=null){
                        for(int i =0; i<temp4.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp4[i]));    
                        }
                    }
                    if(temp5!=null){
                        for(int i =0; i<temp5.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp5[i]));    
                        }
                    }
                    if(temp6!=null){
                        for(int i =0; i<temp6.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp6[i]));    
                        }
                    }
                    if(temp7!=null){
                        for(int i =0; i<temp7.length ;i++){
                            sc.output.add(u.getDecodedParameterValue(temp7[i]));    
                        }
                    }
                                       /*
                    for(int i =0; i<temp5.length ;i++){
                        String tempVal = u.getDecodedParameterValue(temp5[i]);
                        if(!sc.output.contains(tempVal))
                            if(tempVal.trim().length() != 0)
                                sc.output.add(tempVal);    

                    }
                    */
                }

                sc.pagingNames.add("termsAlphabetical");
                sc.pagingValues.add(1);
                sc.pagingNames.add("termsSystematic");
                sc.pagingValues.add(1);
                sc.pagingNames.add("termsHierarchical");
                sc.pagingValues.add(1);
                sc.pagingNames.add("termsResults");
                sc.pagingValues.add(1);
                
                sc.setQueryString("term", targetLang);

            } 
            else 
            if (targetSearchCriteriaMode.equals("SearchCriteria_Hierarchies")) {

                if (targetSearchCriteriaValue.equals("*")) {
                    sc.CombineOperator = "*";
                    sc.input.add("*");
                    sc.operator.add("*");
                    sc.value.add("*");

                    for(int i=0; i<hierarchyDefaultOutput.length; i ++){
                        sc.output.add(hierarchyDefaultOutput[i]);
                    }
                    //sc.output.add("letter_code");


                } 
                else
                if(showAll!= null && showAll.compareTo("all")==0){ // user selected Proboli olwn apo to interface twn kritiriwn kai meta epelekse output

                    sc.CombineOperator="all";
                    
                    String[] temp4 = request.getParameterValues("output_hierarchy");
                    
                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }
                }
                else
                if (targetSearchCriteriaValue.equals("parseCriteria")) {

                    sc.CombineOperator = request.getParameter("operator_hierarchy");

                    String[] temp1 = request.getParameterValues("input_hierarchy");
                    String[] temp2 = request.getParameterValues("op_hierarchy");
                    String[] temp3 = request.getParameterValues("inputvalue_hierarchy");
                    String[] temp4 = request.getParameterValues("output_hierarchy");

                    for(int i =0; i< temp1.length;i++){
                        sc.input.add(u.getDecodedParameterValue(temp1[i]));
                        sc.operator.add(u.getDecodedParameterValue(temp2[i]));
                        sc.value.add(u.getDecodedParameterValue(temp3[i]));                                        
                    }

                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }

                }

                sc.pagingNames.add("hierarchiesResults");
                sc.pagingValues.add(1);
                sc.setQueryString("hierarchy", targetLang);
            } 
            else 
            if (targetSearchCriteriaMode.equals("SearchCriteria_Facets")) {

                if (targetSearchCriteriaValue.equals("*")) {

                    sc.CombineOperator = "*";

                    sc.input.add("name");
                    sc.operator.add("*"); //NOT USED
                    sc.value.add("*"); //NOT USED

                    for(int i=0; i<facetDefaultOutput.length; i ++ ){
                        sc.output.add(facetDefaultOutput[i]);
                    }
                    
                }                 
                else
                if(showAll!= null && showAll.compareTo("all")==0){ // user selected Proboli olwn apo to interface twn kritiriwn kai meta epelekse output

                    sc.CombineOperator="all";
                    
                    String[] temp4 = request.getParameterValues("output_facet");
                    
                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }
                }
                else 
                if (targetSearchCriteriaValue.equals("parseCriteria")) {

                    sc.CombineOperator = request.getParameter("operator_facet");

                    String[] temp1 = request.getParameterValues("input_facet");
                    String[] temp2 = request.getParameterValues("op_facet");
                    String[] temp3 = request.getParameterValues("inputvalue_facet");
                    String[] temp4 = request.getParameterValues("output_facet");

                    for(int i =0; i< temp1.length;i++){
                        sc.input.add(u.getDecodedParameterValue(temp1[i]));
                        sc.operator.add(u.getDecodedParameterValue(temp2[i]));
                        sc.value.add(u.getDecodedParameterValue(temp3[i]));
                    }

                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }

                }

                sc.pagingNames.add("facetsResults");
                sc.pagingValues.add(1);

                sc.setQueryString("facet", targetLang);
            }
            else 
            if (targetSearchCriteriaMode.equals("SearchCriteria_Sources")) {

                
                if (targetSearchCriteriaValue.equals("*")) {

                    //In this case View All icon was pressed from the left menu
                    sc.CombineOperator = "*";

                    sc.input.add("*");
                    sc.operator.add("*");
                    sc.value.add("*");

                    for(int i=0; i<sourcesDefaultOutput.length; i ++ ){
                        sc.output.add(sourcesDefaultOutput[i]);
                    }
                }                                
                else 
                if(showAll!= null && showAll.compareTo("all")==0){ // user selected Proboli olwn apo to interface twn kritiriwn kai meta epelekse output

                    sc.CombineOperator="all";
                    
                    String[] temp4 = request.getParameterValues("output_source");
                    //String[] temp5 = request.getParameterValues("outputSel_source");
                    
                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }
                    /*for(int i =0; i<temp5.length ;i++){
                        String tempVal = u.getDecodedParameterValue(temp5[i]);
                        if(!sc.output.contains(tempVal))
                            if(tempVal.trim().length() != 0)
                                sc.output.add(tempVal);    

                    }*/
                }
                else
                if (targetSearchCriteriaValue.equals("parseCriteria")) { 

                    sc.CombineOperator = request.getParameter("operator_source");

                    String[] temp1 = request.getParameterValues("input_source");
                    String[] temp2 = request.getParameterValues("op_source");
                    String[] temp3 = request.getParameterValues("inputvalue_source");

                    String[] temp4 = request.getParameterValues("output_source");
                    //String[] temp5 = request.getParameterValues("outputSel_source");

                    for(int i =0; i< temp1.length;i++){
                        sc.input.add(u.getDecodedParameterValue(temp1[i]));
                        sc.operator.add(u.getDecodedParameterValue(temp2[i]));
                        sc.value.add(u.getDecodedParameterValue(temp3[i]));                                        
                    }

                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }
                    /*for(int i =0; i<temp5.length ;i++){
                        String tempVal = u.getDecodedParameterValue(temp5[i]);
                        if(!sc.output.contains(tempVal))
                            if(tempVal.trim().length() != 0)
                                sc.output.add(tempVal);    

                    }*/
                }

                sc.pagingNames.add("sourcesResults");
                sc.pagingValues.add(1);
                
                sc.setQueryString("source", targetLang);

            } 
            else 
            if (targetSearchCriteriaMode.equals("SearchCriteria_Users")) {      
                
               
                if (targetSearchCriteriaValue.equals("*")) {

                    sc.CombineOperator = "*";

                    sc.input.add("name");
                    sc.operator.add("*"); //NOT USED
                    sc.value.add("*"); //NOT USED


                    for(int i=0; i<userssDefaultOutput.length; i ++ ){
                        sc.output.add(userssDefaultOutput[i]);
                    }
                    //sc.output.add("name");
                    //sc.output.add("DBname");
                    //sc.output.add("thesaurusName");
                    //sc.output.add("description");
                }                 
                else if(showAll!= null && showAll.compareTo("all")==0){ // user selected Proboli olwn apo to interface twn kritiriwn kai meta epelekse output

                    sc.CombineOperator="all";
                    
                    String[] temp4 = request.getParameterValues("output_user");
                    
                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }
                }
                else 
                if (targetSearchCriteriaValue.equals("parseCriteria")) {

                    sc.CombineOperator = request.getParameter("operator_user");

                    String[] temp1 = request.getParameterValues("input_user");
                    String[] temp2 = request.getParameterValues("op_user");
                    String[] temp3 = request.getParameterValues("inputvalue_user");
                    String[] temp4 = request.getParameterValues("output_user");

                    for(int i =0; i< temp1.length;i++){
                        sc.input.add(u.getDecodedParameterValue(temp1[i]));
                        sc.operator.add(u.getDecodedParameterValue(temp2[i]));
                        sc.value.add(u.getDecodedParameterValue(temp3[i]));                                        
                    }

                    for(int i =0; i<temp4.length ;i++){
                        sc.output.add(u.getDecodedParameterValue(temp4[i]));    

                    }

                }

                sc.pagingNames.add("usersResults");
                sc.pagingValues.add(1);

                sc.setQueryString("user", targetLang);
            }    
        }
        catch(java.io.UnsupportedEncodingException ex){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Update Search Criteria Error : " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }

        
        return sc;

    }
    
    public String getQueryString(Utilities u){
                     
       return Utilities.escapeXML(QueryString);
    }
    

    private String getInputStrLangVariant(String targetInput, String targetLang){
        String retVal = "";
        if(inputStrs.containsKey(targetInput.toLowerCase()) && inputStrs.get(targetInput.toLowerCase()).containsKey(targetLang)){
            retVal = inputStrs.get(targetInput.toLowerCase()).get(targetLang);
        }
        if(retVal==null || retVal.length()==0){
            retVal = targetInput;
        }
        return retVal;
    }
    
    public void setQueryString(String mode, String targetLang) {
        
        String Query = "";

        if (CombineOperator.equals("*")||CombineOperator.equals("all")) {

            //Query = "Show all";
            Query = getShowAllString(targetLang);// SearchCriteria.showAllString;

        } else {

            for (int i = 0; i < input.size(); i++) {

                if (input.size() > 1) {
                    Query += " (";
                }
                String inputStr =  input.get(i);
                String inputStr_UI = input.get(i);
                //Query += " " + input.get(i) + " ";
                
                //for(int j=0;j< inputStrs.size();j++){

                    if(inputStrs.containsKey(inputStr.toLowerCase())){
                        if(mode.compareTo("term")==0){
                            if(termSpecialInputs.containsKey(inputStr) && termSpecialInputs.get(inputStr).containsKey(targetLang)){
                                inputStr_UI = " " + termSpecialInputs.get(inputStr).get(targetLang);
                            }
                            else{
                                inputStr_UI = " " + getInputStrLangVariant(inputStr,targetLang);
                            }

                        }
                        else if(mode.compareTo("hierarchy")==0){
                            if(hierarchySpecialInputs.containsKey(inputStr) && hierarchySpecialInputs.get(inputStr).containsKey(targetLang)){
                                inputStr_UI = " " + hierarchySpecialInputs.get(inputStr).get(targetLang);
                            }
                            else{
                                inputStr_UI = " " + getInputStrLangVariant(inputStr,targetLang);
                            }

                        }
                        else if(mode.compareTo("facet")==0){
                            if(facetSpecialInputs.containsKey(inputStr)&& facetSpecialInputs.get(inputStr).containsKey(targetLang)){
                                inputStr_UI = " " + facetSpecialInputs.get(inputStr).get(targetLang);
                            }
                            else{
                                inputStr_UI = " " + getInputStrLangVariant(inputStr,targetLang);
                            }
                        }
                        else if(mode.compareTo("source")==0){

                            if(sourceSpecialInputs.containsKey(inputStr)&& sourceSpecialInputs.get(inputStr).containsKey(targetLang)){
                                inputStr_UI = " " + sourceSpecialInputs.get(inputStr).get(targetLang);
                            }
                            else{
                                inputStr_UI = " " + getInputStrLangVariant(inputStr,targetLang);
                            }
                        }
                        else{
                            inputStr_UI = " " + getInputStrLangVariant(inputStr,targetLang);
                        }

                        //break;
                    }

                    /*
                    if(inputStrs[j][0].equalsIgnoreCase(inputStr)){
                        if(inputStr.compareTo("name")!=0 && inputStr.compareTo("primary_found_in")!=0 && inputStr.compareTo("translations_found_in")!=0) {
                            inputStr_UI = inputStrs[j][1];
                        }
                        else{//If name then we should write term/hier/or facet
                            if(mode.compareTo("term")==0){
                                if(inputStr.compareTo("name")==0){
                                    inputStr_UI = " Term";
                                }
                                else if(inputStr.compareTo("primary_found_in")==0){
                                    inputStr_UI = " Gr.Source";
                                }
                                else if(inputStr.compareTo("translations_found_in")==0){
                                    inputStr_UI = " Eng.Source";
                                }
                                    
                            }
                            else
                            if(mode.compareTo("hierarchy")==0 && inputStr.compareTo("name")==0){
                                inputStr_UI = "Hierarchy";
                            }
                            else
                            if(mode.compareTo("facet")==0 && inputStr.compareTo("name")==0){
                                inputStr_UI = "Facet";
                            }
                            else if(mode.compareTo("source")==0){
                                if(inputStr.compareTo("name")==0){
                                    inputStr_UI = "Source";
                                }
                                else if(inputStr.compareTo("primary_found_in")==0){
                                    inputStr_UI = "Gr. Term Source";
                                }
                                else if(inputStr.compareTo("translations_found_in")==0){
                                    inputStr_UI = "Eng. Term Source";
                                }
                                    
                            }                            
                        }
                        
                        break;
                    }*/
                //}
                
                Query += " "+inputStr_UI+" ";
                
                if(operator.get(i).matches(ConstantParameters.searchOperatorEquals)){
                    Query += " = = ";
                }
                else{
                if(operator.get(i).matches("!"))
                {
                    Query += " != ";
                }
                else{
                     Query += " "+ operator.get(i) +" ";
                }
                }
                
                Query += " " + value.get(i) + " ";
                
                
                
                if (input.size() > 1) {
                    Query += " ) ";
                }
                
                
                if (i < input.size() - 1) {
                    if(CombineOperator.toUpperCase().matches("AND"))
                        Query += " "+getAndOperatroDisplayString(targetLang)+" " ;
                    if(CombineOperator.toUpperCase().matches("OR"))
                        Query += " "+getOrOperatroDisplayString(targetLang)+" "  ;
                }
            }

        }
        
        QueryString = Query;
    }


}
