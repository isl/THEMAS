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
var servletParamsForTerm;
var servletParamsForHierarchy;
var servletParamsForFacet;

/*-----------------------------------------------------
                    CheckSearchUserInput()
  -----------------------------------------------------
  INPUT: - searchKind: "SearchTerms"/"SearchHierarchies"/"SearchFacets"/"SearchSources"
  OUTPUT: - true in case all user search input criteria have length less than the upper limit: 
              in case of LOGINAM - THEMASAPI_LOGINAM_SIZE (defined in page_head_html.xsl)
              in case of COMMENT - THEMASAPI_MAX_COM_LEN (defined in page_head_html.xsl)
              in case of STRING - THEMASAPI_MAX_STRING (defined in page_head_html.xsl)
          - false otherwise
  CALLED: any time "Search" button is pressed from any search card
  -------------------------------------------------------*/      
function CheckSearchUserInput(searchKind) {
    // ------------------------------- SearchTerms -------------------------------
    if (searchKind == "SearchTerms") {
        var targetFieldSet = document.getElementById("searchTermsForm");
        // get the <select> values currently selected by the user with name = "input_term" ("Πεδίο")
        var selectsArray = targetFieldSet.getElementsByTagName('select');
        var selectsArrayPedio = new Array();
        var index = 0;
        for ( i=0; i< selectsArray.length; i++) {
            if (selectsArray[i].name == "input_term") {
                selectsArrayPedio[index] = selectsArray[i].value;
                index++;
            }
        }
        // get the <input> values currently selected by the user with name = "inputvalue_term" ("Τιμή")
        var inputsArray = targetFieldSet.getElementsByTagName('input');
        var inputsArrayTimh = new Array();
        var index = 0;
        for ( i=0; i< inputsArray.length; i++) {
            if (inputsArray[i].name == "inputvalue_term") {
                inputsArrayTimh[index] = inputsArray[i].value;
                index++;
            }
        }


    }
    // ------------------------------- SearchHierarchies -------------------------------
    else if (searchKind == "SearchHierarchies") {
        var targetFieldSet = document.getElementById("searchHierarchiesForm");
        // get the <select> values currently selected by the user with name = "input_hierarchy" ("Πεδίο")
        var selectsArray = targetFieldSet.getElementsByTagName('select');
        var selectsArrayPedio = new Array();
        var index = 0;
        for ( i=0; i< selectsArray.length; i++) {
            if (selectsArray[i].name == "input_hierarchy") {
                selectsArrayPedio[index] = selectsArray[i].value;
                index++;
            }
        }
        // get the <input> values currently selected by the user with name = "inputvalue_hierarchy" ("Τιμή")
        var inputsArray = targetFieldSet.getElementsByTagName('input');
        var inputsArrayTimh = new Array();
        var index = 0;
        for ( i=0; i< inputsArray.length; i++) {
            if (inputsArray[i].name == "inputvalue_hierarchy") {
                inputsArrayTimh[index] = inputsArray[i].value;
                index++;
            }
        }
        /*
        for ( i=0; i< selectsArrayPedio.length; i++) {
            var pedio = selectsArrayPedio[i];
            var timh = inputsArrayTimh[i];
            if ((pedio == "name" || pedio == "term") && timh.length > THEMASAPI_LOGINAM_SIZE) {
                alert(translate(15) + THEMASAPI_LOGINAM_SIZE + translate(16));
                //alert('Το όνομα ενός στοιχείου της βάσης δεδομένων δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_LOGINAM_SIZE + ' χαρακτήρες.');
                return false;
            }
        }*/
    }
    // ------------------------------- SearchFacets -------------------------------
    else if (searchKind == "SearchFacets") {

        var targetFieldSet = document.getElementById("searchFacetsForm");
        // get the <select> values currently selected by the user with name = "input_facet" ("Πεδίο")
        var selectsArray = targetFieldSet.getElementsByTagName('select');
        var selectsArrayPedio = new Array();
        var index = 0;
        for ( i=0; i< selectsArray.length; i++) {
            if (selectsArray[i].name == "input_facet") {
                selectsArrayPedio[index] = selectsArray[i].value;
                index++;
            }
        }
        // get the <input> values currently selected by the user with name = "inputvalue_facet" ("Τιμή")
        var inputsArray = targetFieldSet.getElementsByTagName('input');
        var inputsArrayTimh = new Array();
        var index = 0;

        /*
        for ( i=0; i< inputsArray.length; i++) {
            if (inputsArray[i].name == "inputvalue_facet") {
                inputsArrayTimh[index] = inputsArray[i].value;
                index++;
            }
        }
        for ( i=0; i< selectsArrayPedio.length; i++) {
            var pedio = selectsArrayPedio[i];
            var timh = inputsArrayTimh[i];
            if ((pedio == "name" || pedio == "term") && timh.length > THEMASAPI_LOGINAM_SIZE) {
                alert(translate(15) + THEMASAPI_LOGINAM_SIZE + translate(16));
                //alert('Το όνομα ενός στοιχείου της βάσης δεδομένων δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_LOGINAM_SIZE + ' χαρακτήρες.');
                return false;
            }
        }

        */
    }
    // ------------------------------- SearchSources -------------------------------
    else if (searchKind == "SearchSources") {
        var targetFieldSet = document.getElementById("searchSourcesForm");
        // get the <select> values currently selected by the user with name = "input_source" ("Πεδίο")
        var selectsArray = targetFieldSet.getElementsByTagName('select');
        var selectsArrayPedio = new Array();
        var index = 0;
        for ( i=0; i< selectsArray.length; i++) {
            if (selectsArray[i].name == "input_source") {
                selectsArrayPedio[index] = selectsArray[i].value;
                index++;
            }
        }
        // get the <input> values currently selected by the user with name = "inputvalue_source" ("Τιμή")
        var inputsArray = targetFieldSet.getElementsByTagName('input');
        var inputsArrayTimh = new Array();
        var index = 0;
        for ( i=0; i< inputsArray.length; i++) {
            if (inputsArray[i].name == "inputvalue_source") {
                inputsArrayTimh[index] = inputsArray[i].value;
                index++;
            }
        }
        /*
        for ( i=0; i< selectsArrayPedio.length; i++) {
            var pedio = selectsArrayPedio[i];
            var timh = inputsArrayTimh[i];
            if ((pedio == "name" || pedio == "primary_found_in" || pedio == "translations_found_in") && timh.length > THEMASAPI_LOGINAM_SIZE) {
                alert(translate(15) + THEMASAPI_LOGINAM_SIZE + translate(16));
                //alert('Το όνομα ενός στοιχείου της βάσης δεδομένων δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_LOGINAM_SIZE + ' χαρακτήρες.');
                return false;
            }
            if (pedio == "source_note" && timh.length > THEMASAPI_MAX_COM_LEN) {
                alert(translate(17) + THEMASAPI_MAX_COM_LEN + translate(18));
                //alert('Το κείμενο ενός σχολίου δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_MAX_COM_LEN + ' χαρακτήρες.');
                return false;
            }
        }*/
    }

    return true;
}

/*
 * This function is triggered either by a search button click in a search criteria form (term/ hierarchy/ facet)
 * or by view All (term/ hierarchy/ facet) on page_leftMenu. 
 * 
 * targetCriteria indicates the session Attribute to be updated. Search criteria are preserved 
 * throughout tab changes in three session Varables (terms, hierarchies and facet). Thus
 * targetCriteria indicates which SearchCriteria session variable should be updated.
 * 
 * sourceCriteria should be either "*" indicating that viewAll was pressed 
 * or "criteriaTab" indicating that a search button was pressed so that search 
 * and output field parameters must be collected from the form specified in targetForm variable during 
 * variable during targetCriteria parsing.
 * 
 */
function updateCriteria(targetCriteria , sourceCriteria){
    DisplayPleaseWaitScreen(true);

    //alert('targetCriteria= ' +targetCriteria+'\nsourceCriteria= ' + sourceCriteria)
    var paramsStr ='';
    //var targetgetResultsRowsID = '';
    var targetgetResultsServlet ='';
    
    var targetFieldSet ="";
    

    if(targetCriteria == "SearchCriteria_Terms"){
        targetFieldSet = "searchTermsForm";
        //targetgetResultsRowsID = '15'; //indicates the default display for term results --> table rows
        targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=';
    }
    
    if(targetCriteria == "SearchCriteria_Hierarchies"){
        targetFieldSet = "searchHierarchiesForm";
        //targetgetResultsRowsID = '18';//indicates the default display for hierarchy results --> table rows
        targetgetResultsServlet = 'SearchResults_Hierarchies?updateTermCriteria=';
    }

    if(targetCriteria == "SearchCriteria_Facets"){
        targetFieldSet = "searchFacetsForm";
        //targetgetResultsRowsID = '19';//indicates the default display for facet results --> table rows
        targetgetResultsServlet = 'SearchResults_Facets?updateTermCriteria=';
    }
    
    if(targetCriteria == "SearchCriteria_Sources"){
        targetFieldSet = "searchSourcesForm";
        targetgetResultsServlet = 'SearchResults_Sources?updateSourceCriteria=';
    }
    
    if(targetCriteria == "SearchCriteria_Users"){
        targetgetResultsServlet = 'SearchResults_Users?updateUserCriteria=';
    }    
    
    //Collect Target Form parameters
    if(sourceCriteria == 'criteriaTab'){
        var i;
        paramsStr += 'parseCriteria&';
        
        var targForm = document.getElementById(targetFieldSet);
        
        var inputs = targForm.getElementsByTagName('input');
        for ( i=0; i< inputs.length; i++) {

            if (inputs[i].getAttribute("type") == "text") {
                paramsStr += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }
            if (inputs[i].type == "checkbox") {
                if (inputs[i].checked) {
                    paramsStr += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
                }
            }
            if (inputs[i].type == "radio") {
                if (inputs[i].checked) {
                    paramsStr += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
                }
            }

        }
      
        var selects = targForm.getElementsByTagName('select');
        for ( i=0; i< selects.length; i++) {
            paramsStr += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[selects[i].selectedIndex].value)) + "&";
        }
        
        // alert(targetgetResultsServlet+paramsStr)
        window.location = targetgetResultsServlet+paramsStr;
    //alert('paramsStr =  ' + paramsStr);
    //ajaxFunction_SetSessionAttribute(targetCriteria , paramsStr);
        
    }
    else 
    if(sourceCriteria == '*'){
        //ajaxFunction_SetSessionAttribute(targetCriteria , sourceCriteria);            
        window.location = targetgetResultsServlet + sourceCriteria;
    }
    else 
    if(sourceCriteria == 'QuickSearch'){
        var QuickSearchInputValue = document.getElementById('QuickSearchInputValue');
        window.location = targetgetResultsServlet + sourceCriteria + '&QuickSearchInputValue=' + escape(encodeURIComponent(QuickSearchInputValue.value));
    }    

    
//call get results with pagingFirst parameter == 1
//getResults(targetgetResultsRowsID, targetgetResultsServlet, '1');
    
    
}

function clearFieldsetTexts(targetFieldset){
    //alert('here');
    var targetFieldSet = document.getElementById(targetFieldset);
    var i;
    var inputs = targetFieldSet.getElementsByTagName('input');
    for (i=0; i< inputs.length; i++) {

        if (inputs[i].getAttribute("type") == "text") {
            inputs[i].value = '';
        }
    }
      
    var textareas = targetFieldSet.getElementsByTagName('textarea');
    for ( i=0; i< textareas.length; i++) {
        textareas[i].value='';
    }
 
    
}

/*-----------------------------------------------------
                    CheckUserInput()
  -----------------------------------------------------
  INPUT: - userInputID: the "id" value of the <input> item to be checked
         - userInputKind: "LOGINAM" in case of logical name user input
                          "COMMENT" in case of comment user input
                          "STRING" in case of comment user input
  OUTPUT: - true in case: "LOGINAM" and user input length is <= THEMASAPI_LOGINAM_SIZE (defined in page_head_html.xsl)
                          "COMMENT" and user input length is <= THEMASAPI_MAX_COM_LEN (defined in page_head_html.xsl)
                          "STRING" and user input length is <= THEMASAPI_MAX_STRING (defined in page_head_html.xsl)
          - false otherwise
  CALLED: any time "Save" button is pressed from any card
  -------------------------------------------------------*/      
function CheckUserInput(userInputID, userInputKind) {
    var userInputLength = document.getElementById(userInputID).value.length;
    /*if (userInputKind == 'LOGINAM' && userInputLength > THEMASAPI_LOGINAM_SIZE) {
        alert(translate(15) + THEMASAPI_LOGINAM_SIZE + translate(16));
        //alert('Το όνομα ενός στοιχείου της βάσης δεδομένων δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_LOGINAM_SIZE + ' χαρακτήρες.');
        return false;
    }
    else if (userInputKind == 'COMMENT' && userInputLength > THEMASAPI_MAX_COM_LEN) {
        alert(translate(17) + THEMASAPI_MAX_COM_LEN + translate(18));
        //alert('Το κείμενο ενός σχολίου δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_MAX_COM_LEN + ' χαρακτήρες.');
        return false;
    }
    else if (userInputKind == 'STRING' && userInputLength > THEMASAPI_MAX_STRING) {
        alert(translate(19) + THEMASAPI_MAX_STRING + translate(20));
        //alert('Το περιεχόμενο μιας τιμής κειμένου δεν πρέπει να ξεπερνάει τους ' + THEMASAPI_MAX_STRING + ' χαρακτήρες.');
        return false;
    }*/
    return true;
}
function cancelAction(){
    
    var CurrentLocation = new String(); 
    CurrentLocation += window.location;
    var CurLocationLength = CurrentLocation.length;
    //alert('CurrentLocation = "' + CurrentLocation +'"' + '\nCurLocationLength = "' + CurLocationLength +'"' );
    
    
    if(CurrentLocation.indexOf('Admin_Thesaurus')!=-1 || 
        CurrentLocation.indexOf('ImportData')!=-1 ||
        CurrentLocation.indexOf('ExportData')!=-1 ||
        CurrentLocation.indexOf('CreateThesaurus')!=-1 ||
        CurrentLocation.indexOf('CopyThesaurus')!=-1 ||
        CurrentLocation.indexOf('MergeThesauri')!=-1 ||
        CurrentLocation.indexOf('DeleteThesaurus')!=-1 ||
        CurrentLocation.indexOf('DBadmin')!=-1 || 
        CurrentLocation.indexOf('RestoreDBbackup')!=-1 || 
        CurrentLocation.indexOf('FixDB')!=-1 ){
        
        document.getElementById('DisplayCardArea').innerHTML='';
        DisplayPleaseWaitScreen(false);
    }
    else
    if(CurrentLocation.charAt(CurLocationLength-1) == "#"){
        //alert('try');
        window.location = CurrentLocation.substring(0,CurLocationLength-1);
    }     
    else
    {
        window.location.reload( true );
    }
}
/*
 *Function used in order to collect input fields (contained in targetFieldSetID)
 *commit an edit action (servletName) and 
 *display the result of the action to the specified textarea (resultArea --> id)
 */
function getServletResult(servletName ,targetFieldSetID , resultArea, selectsSelectionMode){
    //alert(targetFieldSetID);
    //alert( '\n'+servletName+'\n\n'+targetFieldSetID)  ;
    var targetFieldSet = document.getElementById(targetFieldSetID);
/*
    var CheckNameForComma ='Προσωρινά το νέο όνομα ';
    if(servletName=='EditActions_Term'){
        CheckNameForComma +='του όρου: '
        if(targetFieldSetID=='edit_term_create'){
            CheckNameForComma += document.getElementById('newTermName_Id').value;
            if(CheckNameForComma.indexOf(',')>=0){
                CheckNameForComma+=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
                alert(CheckNameForComma)  ;
                return;
            }
        }        
    }
    else if(servletName=='Rename_Term') {
        CheckNameForComma +='του όρου: '
        CheckNameForComma += document.getElementById('newname').value;
        if(CheckNameForComma.indexOf(',')>=0){
            CheckNameForComma+=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
            alert(CheckNameForComma)  ;
            return;
        }
    }
    else if(servletName=='EditActions_Hierarchy'){
        CheckNameForComma +='της ιεραρχίας: '
        if(targetFieldSetID=='edit_hierarchy_create'){
            CheckNameForComma += document.getElementById('newHierarchyName_Id').value;
            if(CheckNameForComma.indexOf(',')>=0){
                CheckNameForComma +=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
                alert(CheckNameForComma)  ;
                return;
            }
        }
    }
    else if(servletName=='Rename_Hierarchy') {
        CheckNameForComma +='της ιεραρχίας: '
        CheckNameForComma += document.getElementById('newhierarchyname').value;
        if(CheckNameForComma.indexOf(',')>=0){
            CheckNameForComma +=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
            alert(CheckNameForComma)  ;
            return;
        }
    }
    else if(servletName=='EditActions_Facet'){
        CheckNameForComma +='του θέματος: '
        if(targetFieldSetID=='edit_facet_create'){
            CheckNameForComma += document.getElementById('newFacetName_Id').value;
            if(CheckNameForComma.indexOf(',')>=0){
                CheckNameForComma +=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
                alert(CheckNameForComma)  ;
                return;
            }
        }
    }
    else if(servletName=='Rename_Facet') {
        CheckNameForComma +='του θέματος: '
        CheckNameForComma += document.getElementById('newfacetname').value;
        if(CheckNameForComma.indexOf(',')>=0){
            CheckNameForComma +=' δεν πρέπει να περιέχει τον χαρακτήρα κόμμα \',\'.';
            alert(CheckNameForComma)  ;
            return;
        }
    }
   */
    
    var fieldSetParams ='';
    var i;
    var inputs = targetFieldSet.getElementsByTagName('input');
        
    for (i=0; i< inputs.length; i++) {

          
        if (inputs[i].type == "checkbox") {
            if (inputs[i].checked) {
                if(inputs[i].name!= '')
                    fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }
        }
        else
        if (inputs[i].type == "radio") {

            if (inputs[i].checked) {
                if(inputs[i].name != '')
                    fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }

        }
        else
        {
            
            if(inputs[i].name!= '')
                fieldSetParams += inputs[i].name + '=' + escape(encodeURIComponent(inputs[i].value)) + '&' ;
        }
          
    }
    if(targetFieldSetID=='edit_term_translations_scope_note'){

        var trsep = document.getElementById('translationSeperator').value;
        
        var table = document.getElementById('trsnstable');
        var rowCount = table.rows.length;

        var value='';
        for(var i=0; i< rowCount; i++){
            //var checkRow = table.rows[i];
            var checkLangCode = table.rows[i].cells[0].innerHTML;
            var id = 'modify_term_translations_scope_note_id_'+checkLangCode;
            //var textAreaObj = table.rows[i].cells[1].getElementsByTagName('textarea')[0];
            
           if(CKEDITOR.instances[id]){            
                var tempval ='';

                if(value.length>0){
                    value+='\n';
                }

                tempval = checkLangCode+trsep+' '+CKEDITOR.instances[id].getData();
                //tempval = tempval.replace("\r\n","");

                while(tempval.indexOf("\r\n")>0){
                    tempval = tempval.replace("\r\n","");
                }
                while(tempval.indexOf("\r")>0){
                    tempval = tempval.replace("\r","");
                }
                while(tempval.indexOf("\n")>0){
                    tempval = tempval.replace("\n","");
                }

                value += tempval;
            }
            else{
                var textAreaObj = table.rows[i].cells[1].getElementsByTagName('textarea')[0];
                
                var tempval ='';

                if(value.length>0){
                    value+='\n';
                }

                tempval = checkLangCode+trsep+' '+textAreaObj.value;

                while(tempval.indexOf("\r\n")>0){
                    tempval = tempval.replace("\r\n","");
                }
                while(tempval.indexOf("\r")>0){
                    tempval = tempval.replace("\r","");
                }
                while(tempval.indexOf("\n")>0){
                    tempval = tempval.replace("\n","");
                }

                value += tempval;                 
            }
        }
        fieldSetParams += 'translations_scope_note=' + escape(encodeURIComponent(value)) + '&' ;
    }   
    else if(targetFieldSetID=='edit_source_source_note' || targetFieldSetID=='edit_source_create'){
        var textareas = targetFieldSet.getElementsByTagName('textarea');
        for ( i=0; i< textareas.length; i++) {

            var tempval = CKEDITOR.instances['modify_source_source_note_id'].getData();
            //tempval = tempval.replace("\r\n","");

            while(tempval.indexOf("\r\n")>0){
                tempval = tempval.replace("\r\n","");
            }
            while(tempval.indexOf("\r")>0){
                tempval = tempval.replace("\r","");
            }
            while(tempval.indexOf("\n")>0){
                tempval = tempval.replace("\n","");
            }


            if(textareas[i].name!= '')
                fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(tempval)) + '&' ;
        }
    }    
    else if(targetFieldSetID=='edit_term_scope_note'){
        var textareas = targetFieldSet.getElementsByTagName('textarea');
        for ( i=0; i< textareas.length; i++) {

            var tempval = CKEDITOR.instances['modify_term_scope_note_id'].getData();
            //tempval = tempval.replace("\r\n","");

            while(tempval.indexOf("\r\n")>0){
                tempval = tempval.replace("\r\n","");
            }
            while(tempval.indexOf("\r")>0){
                tempval = tempval.replace("\r","");
            }
            while(tempval.indexOf("\n")>0){
                tempval = tempval.replace("\n","");
            }

            if(textareas[i].name!= '')
                fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(tempval)) + '&' ;
        }
    }
    else if(targetFieldSetID=='edit_term_historical_note'){
        var textareas = targetFieldSet.getElementsByTagName('textarea');
        for ( i=0; i< textareas.length; i++) {

            var tempval = CKEDITOR.instances['modify_term_historical_note_id'].getData();
            //tempval = tempval.replace("\r\n","");

            while(tempval.indexOf("\r\n")>0){
                tempval = tempval.replace("\r\n","");
            }
            while(tempval.indexOf("\r")>0){
                tempval = tempval.replace("\r","");
            }
            while(tempval.indexOf("\n")>0){
                tempval = tempval.replace("\n","");
            }


            if(textareas[i].name!= '')
                fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(tempval)) + '&' ;
        }
    }    
    else {
        var textareas = targetFieldSet.getElementsByTagName('textarea');
        for ( i=0; i< textareas.length; i++) {

            var tempval = textareas[i].value;
            //tempval = tempval.replace("\r\n","");

            while(tempval.indexOf("\r\n")>0){
                tempval = tempval.replace("\r\n","");
            }
            while(tempval.indexOf("\r")>0){
                tempval = tempval.replace("\r","");
            }
            while(tempval.indexOf("\n")>0){
                tempval = tempval.replace("\n","");
            }


            if(textareas[i].name!= '')
                fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(tempval)) + '&' ;
        }
    }
    
    var selects = targetFieldSet.getElementsByTagName('select');
    
    for ( i=0; i< selects.length; i++) {
        //if edit user servlet then should check if parent row is disabled
        
        
        //if(servletName != 'Create_Modify_Hierarchy' && servletName !='EditActions_Term'){
        if(selectsSelectionMode==='selectedIndexOnly'){
            //added "&& !selects[i].disabled" for share Thesaurus functionality
            //so that global thesaurus (* All thesauri) editing rights of users, 
            //that are not editable via share thesaurus card are not also collected
            if(selects[i].name!== '' && selects[i].selectedIndex >=0 && selects[i].style.visibility !== 'hidden' && !selects[i].disabled)
                fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[selects[i].selectedIndex].value)) + "&";
        }
        else{
            if(selects[i].name!== ''){
                
                if(selects[i].multiple){
                    //introduced in order to include only selected options of select
                    //(chosen plugin)
                
                    for(var k =0; k<selects[i].options.length; k++ ){
                        if(selects[i].options[k].selected){
                            fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[k].value)) + "&";
                        }
                    }
                } 
                else{
                    for(var k =0; k<selects[i].options.length; k++ ){
                        fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[k].value)) + "&";
                    }
                }
            }

        }
    }

    // alert(servletName+'?'+fieldSetParams);
      
    var xmlHttp;
    
    try  {  // Firefox, Opera 8.0+, Safari  
        xmlHttp=new XMLHttpRequest();  
    }
    catch (e) {  // Internet Explorer  
        try {    
            xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");    
        }
        catch (e) {    
            try {
                xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");      
            }
            catch (e) {      
                alert("Your browser does not support AJAX!");      
                return false;      
            }    
        }  
    }  

    xmlHttp.onreadystatechange=function() {
        
        var response;

        //responseText MUST be:
        //
        //Session Invalidate --> user is redirected to index while previous session has already been set invalid
        //Success + message
        //ErrorMessage
        
        if(xmlHttp.readyState==4)
        {	
            //edit_user_create ? possibly? what about the resubscription of old user
            var removePleaseWaitAndPeploServletValues = ["resetDbFieldsetId","edit_thesaurus","edit_user_create"];
            var targetResultArea;
            if(removePleaseWaitAndPeploServletValues.indexOf(targetFieldSetID)>-1){
                DisplayPleaseWaitScreen(false);
            }
            else{
            //DisplayPleaseWaitScreen(false);
                JustRemovePleasWait();
            }
            var txt = xmlHttp.responseText;
      
            var check = false;
            if(txt.length < 30){ //Avoid the following replacement in case of a lengthy answer

                response = txt.replace(/^\s+|\s+$/g,'');
                if(response  ==  'Session Invalidate'){
                    window.location = 'Index';
                }
                else{ 
                    check=true;
                }
               
            }
            else {
                check=true;
            }
            
            if (check){
                //alert("resultArea = \'" + resultArea + "\'");
                if(resultArea==''){
                    response = txt.replace(/^\s+|\s+$/g,'');
                    var moveToSource   = document.getElementById('source_move_refs_to_id');                        
                    if(response == 'Success'){
                        var newTermField = document.getElementById('newTermName_Id');
                        var newHierarchyField = document.getElementById('newHierarchyName_Id');
                        var newFacetField = document.getElementById('newFacetName_Id');
                        var newSourceField = document.getElementById('newSourceName_Id');
                        var newUserField = document.getElementById('newUserName_Id');
                        if(newTermField && newTermField.value !==''){
                      
                            showEditCard_Term(newTermField.value);
                        }
                        else
                        if(newHierarchyField && newHierarchyField.value !==''){
                            
                            showEditCard_Hierarchy(newHierarchyField.value);
                        }
                        else
                        if(newFacetField && newFacetField.value !==''){                            
                            showEditCard_Facet(newFacetField.value);
                        }
                        else
                        if(newSourceField && newSourceField.value !==''){                            
                            showEditCard_Source(newSourceField.value);
                        }
                        else
                        if(moveToSource){                            
                            showEditCard_Source(moveToSource.options[moveToSource.selectedIndex].value);
                        }
                        else
                        if(newUserField && newUserField.value !==''){
                            document.getElementById('DisplayCardArea').innerHTML = '';
                            //window.location.reload( true );
                            cancelAction();
                        }                        
                        else{
                            
                            //window.location.reload( true );
                            cancelAction();
                        }                            
                    }
                    else{
                        response = response.slice(7);
                        var resultOf_Edit = document.getElementById('resultOf_Edit');
                        //alert(response);
                        
                        if(moveToSource){                            
                            alert(response);
                            showEditCard_Source(moveToSource.options[moveToSource.selectedIndex].value);
                        }
                        else if(fieldSetParams.indexOf('save_user_edit')!=-1 ){ //case of user edit only
                        
                            if(response == 'NEW_USER_NAME_ALREADY_EXISTS_IN_DB'){
                                /*var prepareparams = 'user='+escape(encodeURIComponent(document.getElementById('targetUserID').value));
                                prepareparams += '&olderUser='+escape(encodeURIComponent(document.getElementById('newUserName_Id').value));
                                prepareparams += '&description=' + escape(encodeURIComponent(document.getElementById('newUserDescription_Id').value));
                                prepareparams += '&targetEditField=rename_target_and_older_user_edit';
                                */
                                fieldSetParams = fieldSetParams.replace(/save_user_edit/g,'rename_target_and_older_user_edit');
                                //fieldSetParams = fieldSetParams.replace(/save_user_create/g,'rename_target_and_older_user_edit');
                                showEditFieldCard(fieldSetParams,'rename_target_and_older_user_edit', 'EditDisplays_User');
                                return;
                            }
                            else{                                
                                resultOf_Edit.innerHTML = "<b>" + response + "</b>";                                
                            }
                        }
                        else if(fieldSetParams.indexOf('save_user_create')!=-1 ){ 
                            
                            if(response == 'NEW_USER_NAME_ALREADY_EXISTS_IN_DB'){ //case of user creation in which user name exists in dB but does not exist in XML
                                
                                fieldSetParams = fieldSetParams.replace(/save_user_create/g,'user_create_and_merge_with_older');
                                showEditFieldCard(fieldSetParams,'rename_target_and_older_user_edit', 'EditDisplays_User');
                                
                                return;
                            }
                            else{
                                resultOf_Edit.innerHTML = "<b>" + response + "</b>";
                            }
                        }
                        else{
                            resultOf_Edit.innerHTML = "<b>" + response + "</b>";
                        }
                    }
                    
                /*
                    //MoveHierarchyCode 
                    //Should pass rowsIdex as parameter even if others do not need it
                    var content = txt.substring(txt.indexOf('<body>')+6,txt.indexOf('</body>'));
                    //alert(content);
                    var targetDiv = document.getElementById('T27');
                    targetDiv.innerHTML = content;
                    
                    changeCont2('27', 'tab27');
                    DrawTabs(2,true);*/
                }
                else
                if(servletName ==  'Preview_Available_Facets'){
                    
                    targetResultArea = document.getElementById(resultArea);
                    //targetResultArea.style.visibility = "visible"; 
                    targetResultArea.innerHTML = txt;
                    
                }
                else {
                    
                
                    //alert( txt.substring(0, 7)) ;
                    targetResultArea = document.getElementById(resultArea);
                    if(txt.substring(0, 7) == "Success"){
                        
                        txt = txt.slice(7);
                        if(servletName == 'Rename_Term'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Term(newName);
                        //targetResultArea.
                            
                        }else
                        if(servletName == 'UndoRenameResults'){
                            servletParamsForTerm = null;
                        }else
                        if(servletName == 'Rename_Hierarchy'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Hierarchy(newName);
                            
                        }
                        else
                        if(servletName == 'Rename_Facet'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Facet(newName);
                            
                        }
                        else
                        if(servletName == 'EditActions_Source' && txt.indexOf('<newName>')!= -1 && txt.indexOf('</newName>')!=-1){
                            //case of source rename
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Source(newName);
                        }
                        else{
                            
                            targetResultArea.value = txt;
                        }
                    }
                    else{ //No flag such as Success contained in answer
                        // alert('in here');
                        
                        if(txt.indexOf("Failure")==0 || txt.indexOf("Success")==0 ){
                            txt = txt.slice(7);
                        }
                        targetResultArea.value = txt;
                    }
                }

            }
        }
    }    
  
    DisplayPleaseWaitScreen(true);
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST",servletName, true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    
    xmlHttp.send(fieldSetParams); 
//alert(fieldSetParams);   
    
}

function getDeleteResult(servletName ,targetFieldSetID , resultArea, selectsSelectionMode){
      
    var targetFieldSet = document.getElementById(targetFieldSetID);
    var fieldSetParams ='';
    var i;
    var inputs = targetFieldSet.getElementsByTagName('input');
    for (i=0; i< inputs.length; i++) {

          
        if (inputs[i].type == "checkbox") {
            if (inputs[i].checked) {
                if(inputs[i].name!= '')
                    fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }
        }
        else
        if (inputs[i].type == "radio") {

            if (inputs[i].checked) {
                if(inputs[i].name != '')
                    fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }

        }
        else
        {
            if(inputs[i].name!= '')
                fieldSetParams += inputs[i].name + '=' + escape(encodeURIComponent(inputs[i].value)) + '&' ;
        }
          
    }
    var textareas = targetFieldSet.getElementsByTagName('textarea');
    for ( i=0; i< textareas.length; i++) {

        if(textareas[i].name!= '')
            fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(textareas[i].value)) + '&' ;
    }
      
    var selects = targetFieldSet.getElementsByTagName('select');

    for ( i=0; i< selects.length; i++) {

        //if(servletName != 'Create_Modify_Hierarchy' && servletName !='EditActions_Term'){
        if(selectsSelectionMode=='selectedIndexOnly'){
            if(selects[i].name!= '' && selects[i].selectedIndex >=0 )
                fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[selects[i].selectedIndex].value)) + "&";
        }
        else{
            if(selects[i].name!= '')
                for(var k =0; k<selects[i].options.length; k++ ){
                    fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[k].value)) + "&";
                }

        }
    }
     
    //alert(servletName+'?'+fieldSetParams);
      
    var xmlHttp;
    
    try  {  // Firefox, Opera 8.0+, Safari  
        xmlHttp=new XMLHttpRequest();  
    }
    catch (e) {  // Internet Explorer  
        try {    
            xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");    
        }
        catch (e) {    
            try {
                xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");      
            }
            catch (e) {      
                alert("Your browser does not support AJAX!");      
                return false;      
            }    
        }  
    }  

    xmlHttp.onreadystatechange=function() {
        
        var response;

        //responseText MUST be:
        //
        //Session Invalidate --> user is redirected to index while previous session has already been set invalid
        //Success + message
        //ErrorMessage
        
        if(xmlHttp.readyState==4)
        {	
            var targetResultArea;
            DisplayPleaseWaitScreen(false);
            
            var txt = xmlHttp.responseText;

            var check = false;
            if(txt.length < 30){ //Avoid the following replacement in case of a lengthy answer

                response = txt.replace(/^\s+|\s+$/g,'');
                if(response  ==  'Session Invalidate')
                    window.location = 'Index';
                else
                    check=true;
               
            }
            else 
                check=true;
            
            if (check){
                //alert("resultArea = \'" + resultArea + "\'");
                if(resultArea==''){
                    
                    response = txt.replace(/^\s+|\s+$/g,'');
                    
                    if(response == 'Success'){
                     
                        if(targetFieldSetID.toLowerCase().indexOf("term")!=-1){
                            //var newTermField = document.getElementById('newTermName_Id');
                            window.location = 'SearchResults_Terms';
                        }
                        else if(targetFieldSetID.toLowerCase().indexOf("hierarchy")!=-1){
                            window.location = 'SearchResults_Hierarchies';
                        }
                        else if(targetFieldSetID.toLowerCase().indexOf("facet")!=-1){
                            window.location = 'SearchResults_Facets';
                        }
                        else if(targetFieldSetID.toLowerCase().indexOf("source")!=-1){
                            window.location = 'SearchResults_Sources';
                        }
                                                    
                    }
                    else{
                        
                        //alert(response);
                        response = response.slice(7);
                        //window.location = 'SearchResults_Terms';
                        var resultOf_EditTerm = document.getElementById('resultOf_Edit');
                        resultOf_EditTerm.innerHTML = "<b>" + response + "</b>";
                        
                    }
                    
                /*
                    //MoveHierarchyCode 
                    //Should pass rowsIdex as parameter even if others do not need it
                    var content = txt.substring(txt.indexOf('<body>')+6,txt.indexOf('</body>'));
                    //alert(content);
                    var targetDiv = document.getElementById('T27');
                    targetDiv.innerHTML = content;
                    
                    changeCont2('27', 'tab27');
                    DrawTabs(2,true);*/
                }
                else
                if(servletName ==  'Preview_Available_Facets'){
                    
                    targetResultArea = document.getElementById(resultArea);
                    //targetResultArea.style.visibility = "visible"; 
                    targetResultArea.innerHTML = txt;
                    
                }
                else {
                    
                
                    //alert( txt.substring(0, 7)) ;
                    targetResultArea = document.getElementById(resultArea);
                    if(txt.substring(0, 7) == "Success"){
                        
                        txt = txt.slice(7);
                        if(servletName == 'Rename_Term'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Term(newName);
                        //targetResultArea.
                            
                        }else
                        if(servletName == 'UndoRenameResults'){
                            servletParamsForTerm = null;
                        }else{
                            targetResultArea.value = txt;
                        }
                    }
                    else{ //No flag such as Success contained in answer
                        // alert('in here');
                        targetResultArea.value = txt;
                    }
                }

            }
        }
    }    
  
    DisplayPleaseWaitScreen(true);
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST",servletName, true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    
    xmlHttp.send(fieldSetParams);    
    
}

function DisplayBlackBackground(showInsteadOfHide){
    if(showInsteadOfHide){
        document.getElementById('peploScreen').style.visibility = 'visible';
        document.getElementById('peploScreen').style.zIndex = '500';
    }
    else{
        document.getElementById('peploScreen').style.visibility = 'hidden';
        document.getElementById('peploScreen').style.zIndex = '300';
    }
}

function JustRemovePleasWait(){
    document.getElementById('pleasewaitScreen').style.visibility = 'hidden';
    document.getElementById('peploScreen').style.zIndex = '300';
}

/**
 * Toggles the please wait gif icon and calls the DisplayBlackBackground function
 * to open or hide 
 * 
 * @param {type} display
 * @returns {undefined}
 */
function DisplayPleaseWaitScreen(display) {
    if (display === true) {
        document.getElementById('pleasewaitScreen').style.visibility = 'visible';        
    }
    else {
        document.getElementById('pleasewaitScreen').style.visibility = 'hidden';
        
    }
    DisplayBlackBackground(display);
}

function addTranslationScopeNote(targetTableId,langSelectId){

    var newlangcode = document.getElementById(langSelectId).options[document.getElementById(langSelectId).selectedIndex].value;

    //  alert(newlangcode);
    var table = document.getElementById(targetTableId);
    var rowCount = table.rows.length;

    for(var i=0; i< rowCount; i++){
        //var checkRow = table.rows[i];
        var checkLangCodeCell = table.rows[i].cells[0];
        //alert('ok here');

        var checkLangCode = checkLangCodeCell.innerHTML;
        if(checkLangCode == newlangcode){
            alert(newlangcode + translate(66));
            //alert(newlangcode + ' is already defined.');
            return false;
        }
    }

    
    
    var row = table.insertRow(rowCount);
    //row.setAttribute("class", "translationstablerow");

    var cell1 = row.insertCell(0);
    //cell1.setAttribute("class", "translationstablecolumn");
    //var element1 = document.createElement("input");
    //element1.type = "text";
    //element1.name = "LanguageIdentifier";
    
    cell1.innerHTML=newlangcode;
    cell1.vAlign='top';


    var cell2 = row.insertCell(1);
    cell2.setAttribute("width","98%");
    //cell2.setAttribute("class", "trscopenotesclass");
    var element2 = document.createElement("textarea");
    element2.rows=5;
    element2.setAttribute("class", "trscopenotesclass");
    element2.setAttribute("id", "modify_term_translations_scope_note_id_"+newlangcode);
    //element2.
    cell2.appendChild(element2);
        

    var cell3 = row.insertCell(2);
    var element3 = document.createElement("input");
    element3.type = "button";
    element3.setAttribute("class", "button");
    element3.value='-';
    element3.onclick = function () {
        deleteTableRow(targetTableId, this)
        };
    cell3.vAlign='top';

    cell3.appendChild(element3);
    
    CKEDITOR.replace( "modify_term_translations_scope_note_id_"+newlangcode);
}

function deleteTableRow(targetTableId,obj){
    var table=document.getElementById(targetTableId);
    var delRow = obj.parentNode.parentNode;
    var rIndex = delRow.sectionRowIndex;
    
    table.deleteRow(rIndex);
}

function saveTranslationsScopeNotes(targetTableId){
    var table = document.getElementById(targetTableId);
    var rowCount = table.rows.length;

    var value='';
    for(var i=0; i< rowCount; i++){
        //var checkRow = table.rows[i];
        var checkLangCode = table.rows[i].cells[0].innerHTML;
        var textAreaObj = table.rows[i].cells[1].getElementsByTagName('textarea')[0];


        if(value.length>0){
            value+='\n';
        }
        value+=checkLangCode+':\n'+textAreaObj.value;
    }
    alert(value);

/*
    for(var i=0; i< rowCount; i++){
    var targetFieldSet = document.getElementById(targetFieldSetID);
      var fieldSetParams ='';
      var i;
      var inputs = targetFieldSet.getElementsByTagName('input');

      for (i=0; i< inputs.length; i++) {


          if (inputs[i].type == "checkbox") {
             if (inputs[i].checked) {
                 if(inputs[i].name!= '')
                    fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
             }
          }
          else
          if (inputs[i].type == "radio") {

             if (inputs[i].checked) {
                 if(inputs[i].name != '')
                     fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
             }

          }
          else
          {
              if(inputs[i].name!= '')
                  fieldSetParams += inputs[i].name + '=' + escape(encodeURIComponent(inputs[i].value)) + '&' ;
          }

      }
      var textareas = targetFieldSet.getElementsByTagName('textarea');
      for ( i=0; i< textareas.length; i++) {

              if(textareas[i].name!= '')
                  fieldSetParams += textareas[i].name + '=' + escape(encodeURIComponent(textareas[i].value)) + '&' ;
      }

      var selects = targetFieldSet.getElementsByTagName('select');

      for ( i=0; i< selects.length; i++) {

          //if(servletName != 'Create_Modify_Hierarchy' && servletName !='EditActions_Term'){
          if(selectsSelectionMode=='selectedIndexOnly'){
            if(selects[i].name!= '' && selects[i].selectedIndex >=0 && selects[i].style.visibility != 'hidden')
              fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[selects[i].selectedIndex].value)) + "&";
          }
          else{
              if(selects[i].name!= '')
              for(var k =0; k<selects[i].options.length; k++ ){
                  fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[k].value)) + "&";
              }

          }
      }

     // alert(servletName+'?'+fieldSetParams);

    var xmlHttp;

    try  {  // Firefox, Opera 8.0+, Safari
        xmlHttp=new XMLHttpRequest();
    }
    catch (e) {  // Internet Explorer
        try {
            xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e) {
            try {
                xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e) {
                alert("Your browser does not support AJAX!");
                return false;
            }
        }
    }

    xmlHttp.onreadystatechange=function() {

        var response;

        //responseText MUST be:
        //
        //Session Invalidate --> user is redirected to index while previous session has already been set invalid
        //Success + message
        //ErrorMessage

        if(xmlHttp.readyState==4)
        {
            var targetResultArea;
            DisplayPleaseWaitScreen(false);

            var txt = xmlHttp.responseText;

            var check = false;
            if(txt.length < 30){ //Avoid the following replacement in case of a lengthy answer

                response = txt.replace(/^\s+|\s+$/g,'');
                if(response  ==  'Session Invalidate'){
                    window.location = 'Index';
                }
                else{
                    check=true;
                }

            }
            else {
                check=true;
            }

            if (check){
                //alert("resultArea = \'" + resultArea + "\'");
                if(resultArea==''){
                    response = txt.replace(/^\s+|\s+$/g,'');
                    var moveToSource   = document.getElementById('source_move_refs_to_id');
                    if(response == 'Success'){
                        var newTermField = document.getElementById('newTermName_Id');
                        var newHierarchyField = document.getElementById('newHierarchyName_Id');
                        var newFacetField = document.getElementById('newFacetName_Id');
                        var newSourceField = document.getElementById('newSourceName_Id');
                        var newUserField = document.getElementById('newUserName_Id');
                        if(newTermField && newTermField.value !=''){

                            showEditCard_Term(newTermField.value);
                        }
                        else
                        if(newHierarchyField && newHierarchyField.value !=''){

                            showEditCard_Hierarchy(newHierarchyField.value);
                        }
                        else
                        if(newFacetField && newFacetField.value !=''){
                            showEditCard_Facet(newFacetField.value);
                        }
                        else
                        if(newSourceField && newSourceField.value !=''){
                            showEditCard_Source(newSourceField.value);
                        }
                        else
                        if(moveToSource){
                            showEditCard_Source(moveToSource.options[moveToSource.selectedIndex].value);
                        }
                        else
                        if(newUserField && newUserField.value !=''){
                            document.getElementById('DisplayCardArea').innerHTML = '';
                            //window.location.reload( true );
                            cancelAction();
                        }
                        else{

                            //window.location.reload( true );
                            cancelAction();
                        }
                    }
                    else{
                        response = response.slice(7);
                        var resultOf_Edit = document.getElementById('resultOf_Edit');
                        //alert(response);

                        if(moveToSource){
                            alert(response);
                            showEditCard_Source(moveToSource.options[moveToSource.selectedIndex].value);
                        }
                        else if(fieldSetParams.indexOf('save_user_edit')!=-1 ){ //case of user edit only

                            if(response == 'NEW_USER_NAME_ALREADY_EXISTS_IN_DB'){
                                //var prepareparams = 'user='+escape(encodeURIComponent(document.getElementById('targetUserID').value));
                                //prepareparams += '&olderUser='+escape(encodeURIComponent(document.getElementById('newUserName_Id').value));
                                //prepareparams += '&description=' + escape(encodeURIComponent(document.getElementById('newUserDescription_Id').value));
                                //prepareparams += '&targetEditField=rename_target_and_older_user_edit';
                                
                                fieldSetParams = fieldSetParams.replace(/save_user_edit/g,'rename_target_and_older_user_edit');
                                //fieldSetParams = fieldSetParams.replace(/save_user_create/g,'rename_target_and_older_user_edit');
                                showEditFieldCard(fieldSetParams,'rename_target_and_older_user_edit', 'EditDisplays_User');
                                return;
                            }
                            else{
                                resultOf_Edit.innerHTML = "<b>" + response + "</b>";
                            }
                        }
                        else if(fieldSetParams.indexOf('save_user_create')!=-1 ){
                            if(response == 'NEW_USER_NAME_ALREADY_EXISTS_IN_DB'){ //case of user creation in which user name exists in dB but does not exist in XML

                                fieldSetParams = fieldSetParams.replace(/save_user_create/g,'user_create_and_merge_with_older');
                                showEditFieldCard(fieldSetParams,'rename_target_and_older_user_edit', 'EditDisplays_User');

                                return;
                            }
                            else{
                                resultOf_Edit.innerHTML = "<b>" + response + "</b>";
                            }
                        }
                        else{
                            resultOf_Edit.innerHTML = "<b>" + response + "</b>";
                        }
                    }

                    //
                    //MoveHierarchyCode
                    //Should pass rowsIdex as parameter even if others do not need it
                    //var content = txt.substring(txt.indexOf('<body>')+6,txt.indexOf('</body>'));
                    //alert(content);
                    //var targetDiv = document.getElementById('T27');
                    //targetDiv.innerHTML = content;

                    //changeCont2('27', 'tab27');
                    //DrawTabs(2,true);
                }
                else
                if(servletName ==  'Preview_Available_Facets'){

                    targetResultArea = document.getElementById(resultArea);
                    //targetResultArea.style.visibility = "visible";
                    targetResultArea.innerHTML = txt;

                }
                else {


                    //alert( txt.substring(0, 7)) ;
                    targetResultArea = document.getElementById(resultArea);
                    if(txt.substring(0, 7) == "Success"){

                        txt = txt.slice(7);
                        if(servletName == 'Rename_Term'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Term(newName);
                            //targetResultArea.

                        }else
                        if(servletName == 'UndoRenameResults'){
                            servletParamsForTerm = null;
                        }else
                        if(servletName == 'Rename_Hierarchy'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Hierarchy(newName);

                        }
                        else
                        if(servletName == 'Rename_Facet'){
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Facet(newName);

                        }
                        else
                        if(servletName == 'EditActions_Source' && txt.indexOf('<newName>')!= -1 && txt.indexOf('</newName>')!=-1){
                            //case of source rename
                            servletParamsForTerm = null;
                            var newName = txt.substring(txt.indexOf('<newName>')+9,txt.indexOf('</newName>'));
                            showEditCard_Source(newName);
                        }
                        else{

                            targetResultArea.value = txt;
                        }
                    }
                    else{ //No flag such as Success contained in answer
                       // alert('in here');
                       if(txt.indexOf("Failure")==0 || txt.indexOf("Success")==0 ){
                           txt = txt.slice(7);
                       }
                       targetResultArea.value = txt;
                    }
                }

            }
        }
    }

    DisplayPleaseWaitScreen(true);
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST",servletName, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    xmlHttp.send(fieldSetParams);
    */
}
