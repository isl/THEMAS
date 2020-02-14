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
/*/function Admin_Thesaurus_DisplayDIV(divID) {	
  //alert('DBadminDisplayTAB():' + divID);
  
  document.getElementById('ImportExport_Data_DIV').style.visibility = 'hidden';
  document.getElementById('Fix_Data_DIV').style.visibility = 'hidden';  
  document.getElementById('CreateThesaurus_DIV').style.visibility = 'hidden';
  document.getElementById('EditGuideTerms_DIV').style.visibility = 'hidden'; 
  document.getElementById(divID).style.visibility = 'visible';
  
  var LinkId = divID.replace('_DIV','_LINK');
  document.getElementById('ImportExport_Data_LINK').className ="inactive";
  document.getElementById('Fix_Data_LINK').className ="inactive";
  document.getElementById('CreateThesaurus_LINK').className ="inactive";
  document.getElementById('EditGuideTerms_LINK').className ="inactive";
  document.getElementById(LinkId).className ="active";
}
*/

/*-----------------------------------------------------
              Import_DataButtonPressed()
-------------------------------------------------------*/      
function Import_DataButtonPressed(importMode){
    var choiceSelected = '';
    choiceSelected="&ImportThesaurusMode="+importMode;
    
    if(importMode == 'thesaurusImport'){
        var importXMLfilename = encodeURIComponent(document.getElementById('importXMLfilename_ID').value);
        //alert(importXMLfilename);
        if (importXMLfilename.length == 0) {
            alert(translate(44));
            //alert('Δεν έχετε επιλέξει .xml αρχείο εισόδου.');
            // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following// otherwise, the last form's action is executed!!
            submitFormTo('Import_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
            return;
        }    
        
        // replace special characters of Create_Thesaurus_NewName_Input
        var Import_Thesaurus_NewName_Input = document.getElementById('Import_Thesaurus_NewName_ID');
        var Import_Thesaurus_NewName = Import_Thesaurus_NewName_Input.value;
        Import_Thesaurus_NewName = StringReplaceSpecialCharacters(Import_Thesaurus_NewName);
        Import_Thesaurus_NewName_Input.value = Import_Thesaurus_NewName;
        
        var trimmed = ThesaurusNameNormalizedString(Import_Thesaurus_NewName);
        
        if (!ThesaurusNameIsLengthOk(trimmed)) {
            alert(translate(45));
            //alert('Το όνομα του θησαυρού πρέπει να περιέχει από 1 έως και 10 χαρακτήρες.');
            // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
            // otherwise, the last form's action is executed!!
            submitFormTo('Import_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
        }
        else if (!ThesaurusNameAreCharsOk(trimmed)) {
            alert(translate(46));
            //alert('Το όνομα του θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.');
            // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
            // otherwise, the last form's action is executed!!
            submitFormTo('Import_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
        }
        else{
            
            //var selection = document.getElementsByName('schematype');
            var schemaTypeSelection = '';
            /*
            for (var k=0; k<selection.length; k++)
            {
              if (selection[k].checked){
                  schemaTypeSelection = selection[k].value;
              }
            }
            */

            

            


             if(confirm(translate(47))){
             //if(confirm('Είστε σίγουρος για την εισαγωγή θησαυρού στην βάση;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
                DisplayPleaseWaitScreen(true);
                //alert('ImportData?Import_Thesaurus_NewName_NAME='+trimmed+"&importXMLfilename="+importXMLfilename + choiceSelected);
                var intiDBCheckBox = document.getElementById('importThesarusInitDb');
                if(intiDBCheckBox && intiDBCheckBox.checked){
                    submitFormTo('Import_DataForm' , 'ImportData?Import_Thesaurus_NewName_NAME='+trimmed+"&importXMLfilename="+importXMLfilename + choiceSelected+'&schematype='+schemaTypeSelection+'&InitDB=true');
                }
                else{
                    submitFormTo('Import_DataForm' , 'ImportData?Import_Thesaurus_NewName_NAME='+trimmed+"&importXMLfilename="+importXMLfilename + choiceSelected+'&schematype='+schemaTypeSelection);
                }
            }
            else{
                submitFormTo('Import_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
            }
        }
    }
    else
    if(importMode == 'bulkImport'){
        

        var bulkImportXMLfilename = encodeURIComponent(document.getElementById('bulkImportXMLfilename_ID').value);
        if (bulkImportXMLfilename.length == 0) {
            alert(translate(44));
            //alert('Δεν έχετε επιλέξει .xml αρχείο εισόδου.');
            // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following// otherwise, the last form's action is executed!!
            submitFormTo('bulkImport_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
            return;
        }

        // replace special characters of Create_Thesaurus_NewName_Input
        var Import_Thesaurus_Hierarchy_Input = document.getElementById('bulkImportHierarchy_Id');
        var Import_Thesaurus_HierarchyName = Import_Thesaurus_Hierarchy_Input.options[Import_Thesaurus_Hierarchy_Input.selectedIndex].value;
        Import_Thesaurus_HierarchyName = StringReplaceSpecialCharacters(Import_Thesaurus_HierarchyName);

        var trimmed = '' + Import_Thesaurus_HierarchyName.replace(/^\s+|\s+$/g, '') ;
        if (trimmed.length == 0) {
            alert(translate(48));
            //alert('Δεν έχει προσδιοριστεί ιεραρχία εισαγωγής.');
            // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following// otherwise, the last form's action is executed!!
            submitFormTo('bulkImport_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
        }
        else{
            if(confirm(translate(49) +trimmed +translate(50))){
            //if(confirm('Είστε σίγουρος για την εισαγωγή όρων στην ιεραρχία \'' +trimmed +'\';\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
                DisplayPleaseWaitScreen(true);
                trimmed = escape(encodeURIComponent(trimmed));
                //alert('ImportData?Import_Thesaurus_HierarchyName='+trimmed+"&importXMLfilename="+bulkImportXMLfilename + choiceSelected);
                submitFormTo('bulkImport_DataForm' , 'ImportData?Import_Thesaurus_HierarchyName='+trimmed+"&importXMLfilename="+bulkImportXMLfilename + choiceSelected);  
            }
            else{
                submitFormTo('bulkImport_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
            }
        }

    }
    else{
        alert(translate(51));        
        //alert('Δεν έχει προσδιοριστεί μέθοδος εισαγωγής δεδομένων στον θησαυρό.');
        submitFormTo('Import_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
        return;
    }
    return;
}

function Export_DataButtonPressed(){
    //alert('Προσωρινά μη διαθέσιμη λειτουργία.');
    //return;
    //alert('exporting');
     if(confirm(translate(52))){
     //if(confirm('Είστε σίγουρος για την εξαγωγή θησαυρού από την βάση;')){
         DisplayPleaseWaitScreen(true);
         submitFormTo('Export_DataForm' , 'ExportData');  
     }
     else{
         submitFormTo('Export_DataForm' , 'Admin_Thesaurus?DIV=ImportExport_Data_DIV');  
     }
    
}

function previewCreateThesaurusResults(logfilename){
    //DisplayPleaseWaitScreen(true);
    var windowName = ConstructWindowName('', '');
    var x = window.screenLeft;
    var y = window.screenTop;
    // todo: location=yes (ONLY for debugging) - to be: location=no
    var windowConfiguration = 'left='+x+',top='+y+',width='+800+',height='+600 +',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
    window.open('LogFiles/'+logfilename, windowName, windowConfiguration);
}

function exportFilePreview(logfilename){
    var windowName = ConstructWindowName('', '');
    var x = window.screenLeft;
    var y = window.screenTop;
    // todo: location=yes (ONLY for debugging) - to be: location=no
    var windowConfiguration = 'left='+x+',top='+y+',width='+800+',height='+600 +',resizable=yes,toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes';
    window.open('LogFiles/'+logfilename, windowName, windowConfiguration);
}

function callFixServlet( servlet,mode,functionallity){

    var xmlHttp;
    var params = "?mode=" + mode+"&functionallity="+functionallity;
    if(functionallity=='HierarchyStatuses'){
        var selectHier = document.getElementById('targetHierarchy_Id');
        params+='&'+selectHier.name + '=';
        params+=escape(encodeURIComponent(selectHier.options[selectHier.selectedIndex].value));
        
        var selectStatus = document.getElementById('targetStatus_Id');
        params+='&'+selectStatus.name + '=';
        params+=escape(encodeURIComponent(selectStatus.options[selectStatus.selectedIndex].value));
    }
    //alert(params);
    var windowName = ConstructWindowName('', '');
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
    
    
    var pleaseWaitWindow;
    if(mode == "Preview"){
        var x = window.screenLeft + 50;
        var y = window.screenTop +20;
        // todo: location=yes (ONLY for debugging) - to be: location=no
        var windowConfiguration = 'left='+x+',top='+y+',width='+668+',height='+400 +',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
        pleaseWaitWindow = window.open('WaitForDownload', windowName, windowConfiguration);
        pleaseWaitWindow.focus();
    }
    
    xmlHttp.onreadystatechange=function() {

        if(xmlHttp.readyState==4){
            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            var response = txt.replace(/^\s+|\s+$/g,'');
            if (response == "SYSTEM LOCKED") {
              window.location = 'SystemIsUnderMaintenance';
            }
            //alert(response);
            if(mode == "Preview"){
                
                pleaseWaitWindow.location=response;
                pleaseWaitWindow.focus();
            }
            if(mode == "Fix"){
                if(response=="Retry")
                    alert(translate(53))
                    //alert("Δεν διορθώθηκαν όλα τα δεδομένα.\n\nΠαρακαλώ επαναλάβετε την \nτελευταία ενέργεια διόρθωσης.");
                
            }
            
        }
    }    
  
    DisplayPleaseWaitScreen(true);
    xmlHttp.open("GET",servlet + params, true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    
    xmlHttp.send(null);    

}


function callAdminTranslationsServlet(servlet,functionallity){


    var params = "?functionallity="+functionallity;
    var okToContinue=true;
    
    if(functionallity=='preview'){
        var selectedThes = document.getElementById('ConfigTranslationsCategoriesThesaurus_Admin_Id');
        params+='&selectedThesaurus='+   escape(encodeURIComponent(selectedThes.value));
    }



    if(functionallity =='save'){

        var selectedThes = document.getElementById('ConfigTranslationsCategoriesThesaurus_Admin_Id');
        params+='&selectedThesaurus='+   escape(encodeURIComponent(selectedThes.value));
        var table = document.getElementById('translationstablebody');
        var rowCount = table.rows.length;
        
        for(var i=0; i< rowCount ; i++){
            var row = table.rows.item(i);
            params +='&LanguageIdentifier='+ row.getElementsByTagName('input')[0].value + " ";
            params +='&LanguageName='+ row.getElementsByTagName('input')[1].value; + " ";
        }

        //alert(retrunVal);
    }

    if(okToContinue == true){
        DisplayPleaseWaitScreen(true);
        window.location = 'Translations'+params;
    }
    else{
        
    }
    
}

function ThesaurusNameNormalizedString(thesName){
    var trimmed = thesName.replace(/^\s+|\s+$/g, '') ;
    trimmed= trimmed.toUpperCase();
    return trimmed;
}
function ThesaurusNameIsLengthOk(thesName){
    if(thesName.length == 0 || thesName.length > 20){
        return false;
    }
    return true;
}

function ThesaurusNameAreCharsOk(thesName){
    if(thesName != thesName.match('[A-Z0-9_-]+')){
        return false;
    }
    return true;
}
/*-----------------------------------------------------
              Create_ThesaurusOKButtonPressed()
-------------------------------------------------------*/      
function Create_ThesaurusOKButtonPressed() {
  //TEMPORARILY DISABLED
  //alert('Η λειτουργία αυτή είναι προσωρινά απενεργοποιημένη');
  //submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV'); 
  //DisplayPleaseWaitScreen(false); 
  //return;
    
  //alert('Create_ThesaurusOKButtonPressed');
  // replace special characters of Create_Thesaurus_NewName_Input
  var Create_Thesaurus_NewName_Input = document.getElementById('Create_Thesaurus_NewName_ID');
  var Create_Thesaurus_NewName = Create_Thesaurus_NewName_Input.value;
  Create_Thesaurus_NewName = StringReplaceSpecialCharacters(Create_Thesaurus_NewName);
  Create_Thesaurus_NewName_Input.value = Create_Thesaurus_NewName;
    
  var trimmed = ThesaurusNameNormalizedString(Create_Thesaurus_NewName);
  if (!ThesaurusNameIsLengthOk(trimmed)) {
    alert(translate(45));  
    //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει από 1 έως και 10 χαρακτήρες.');
    // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');    
  }
  else if (!ThesaurusNameAreCharsOk(trimmed)) {
    alert(translate(46));
    //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.');
    // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');      
  }
  else {
      if(confirm(translate(54))){
      //if(confirm('Είστε σίγουρος για την δημιουργία νέου θησαυρού;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
          DisplayPleaseWaitScreen(true);
          document.getElementById('Create_Thesaurus_result_textarea_ID').value = translate(42);
          //document.getElementById('Create_Thesaurus_result_textarea_ID').value = 'Παρακαλώ περιμένετε...';
          submitFormTo('Create_ThesaurusForm' , 'CreateThesaurus');  


/*
 *
 *var params = "?mode=" + mode;  
    
    params +=    "&newGuideTerm=" + escape(encodeURIComponent(newGuideTerm.value));
    
    if(deleteGuideTerm.selectedIndex >-1){
        params +=    "&deleteGuideTerm=" + escape(encodeURIComponent(deleteGuideTerm.options[deleteGuideTerm.selectedIndex].value));
    }
    if(renameGuideTermFrom.selectedIndex >-1){
        params +=    "&renameGuideTermFrom=" + escape(encodeURIComponent(renameGuideTermFrom.options[renameGuideTermFrom.selectedIndex].value));
    }
    
    params +=    "&renameGuideTermTo=" + escape(encodeURIComponent(renameGuideTermTo.value ));    
    
    var xmlHttp = GetActiveXObject();
    
    xmlHttp.onreadystatechange=function() {

        if(xmlHttp.readyState==4){
            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            var response = txt.replace(/^\s+|\s+$/g,'');
            if (response == "SYSTEM LOCKED") {
              window.location = 'SystemIsUnderMaintenance';
            }
            else if(response =="Session Invalidate"){
                window.location = 'Index?logout=true';
            }
            else if(response =='Success'){
                window.location='Admin_Thesaurus?DIV=EditGuideTerms_DIV'
            } else {
                response = response.slice(7);
                alert(response);
            }            
        }
    }    
  
    DisplayPleaseWaitScreen(true);
    xmlHttp.open("GET",'EditGuideTerms' + params, true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    
    xmlHttp.send(null);    
 */

      }
      else{
          
          return false;
    
    //form.submit();
      }
      /*else{
          submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');      
      }*/
    
  }
}

/*-----------------------------------------------------
              Delete_ThesaurusOKButtonPressed()
-------------------------------------------------------*/      
function Delete_ThesaurusOKButtonPressed() {
  //TEMPORARILY DISABLED
  /*
  alert('Η λειτουργία αυτή είναι προσωρινά απενεργοποιημένη');
  submitFormTo('Delete_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
  return;
  */
    
  var Delete_Thesaurus_Name_Input = document.getElementById('deleteThesaurus_ID');
  var Delete_Thesaurus_Name = Delete_Thesaurus_Name_Input.options[Delete_Thesaurus_Name_Input.selectedIndex].value;
  Delete_Thesaurus_Name = StringReplaceSpecialCharacters(Delete_Thesaurus_Name);  

  if (confirm(translate(55) + Delete_Thesaurus_Name + translate(56))) {
  //if (confirm('Είστε σίγουρος για τη διαγραφή του θησαυρού ' + Delete_Thesaurus_Name + ';')) {
      DisplayPleaseWaitScreen(true);
    document.getElementById('Delete_Thesaurus_result_textarea_ID').value = translate(57);
    //document.getElementById('Delete_Thesaurus_result_textarea_ID').value = 'Η διαδικασία αυτή είναι χρονοβόρα. \rΠαρακαλώ περιμένετε...';
    submitFormTo('Delete_ThesaurusForm' , 'DeleteThesaurus');    
  }
  else { 
    // ATTENTION!!!! in case of No/Cancel selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Delete_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
  }
}

/*-----------------------------------------------------
              Copy_ThesaurusOKButtonPressed()
-------------------------------------------------------*/      
function Copy_ThesaurusOKButtonPressed(){
    var Copy_Thesaurus_1_Name_Input = document.getElementById('sourceThesaurus_ID');
    var Copy_Thesaurus_1_Name = Copy_Thesaurus_1_Name_Input.options[Copy_Thesaurus_1_Name_Input.selectedIndex].value;
    Copy_Thesaurus_1_Name = StringReplaceSpecialCharacters(Copy_Thesaurus_1_Name);
    
    var Copy_Thesaurus_NewName_Input = document.getElementById('Copy_Thesaurus_NewName_ID');
    var Copy_Thesaurus_NewName = Copy_Thesaurus_NewName_Input.value;
    Copy_Thesaurus_NewName = StringReplaceSpecialCharacters(Copy_Thesaurus_NewName);
    Copy_Thesaurus_NewName.value = Copy_Thesaurus_NewName; 
    
    
    var trimmed = ThesaurusNameNormalizedString(Copy_Thesaurus_NewName) ;
    
  if (!ThesaurusNameIsLengthOk(trimmed)) {
    alert(translate(45));
    //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει από 1 έως και 10 χαρακτήρες.');
    // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Copy_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
  }
  else if (!ThesaurusNameAreCharsOk(trimmed)) {
    alert(translate(46));
    //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.');
    // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Copy_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
  }
  else {
    //document.all['Copy_Thesaurus_result_textarea_ID'].value = 'Παρακαλώ περιμένετε...';
    if(confirm(translate(58))){
    //if(confirm('Είστε σίγουρος για την αντιγραφή θησαυρού;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
        DisplayPleaseWaitScreen(true);
        submitFormTo('Copy_ThesaurusForm' , 'CopyThesaurus');    
    }
    else{
        submitFormTo('Copy_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
    }
  }    
}

/*-----------------------------------------------------
              Merge_ThesauriButtonPressed()
-------------------------------------------------------*/      
function Merge_ThesauriButtonPressed() {
  //alert('Προσωρινά μη διαθέσιμη λειτουργία.');
  //callServletOK = false;
  //return false;
  var Create_Thesaurus_1_Name_Input = document.getElementById('thesaurus_1_ID');
  var Create_Thesaurus_2_Name_Input = document.getElementById('thesaurus_2_ID');
  var Create_Thesaurus_Merged_Name_Input = document.getElementById('thesaurus_merged_ID');
  
  var Create_Thesaurus_1_Name = '';
  var Create_Thesaurus_2_Name = '';
  var Create_Thesaurus_Merged_Name = '';
  
  if(Create_Thesaurus_1_Name_Input && Create_Thesaurus_2_Name_Input && Create_Thesaurus_Merged_Name_Input){
      Create_Thesaurus_1_Name = Create_Thesaurus_1_Name_Input.options[Create_Thesaurus_1_Name_Input.selectedIndex].value;
      Create_Thesaurus_2_Name = Create_Thesaurus_2_Name_Input.options[Create_Thesaurus_2_Name_Input.selectedIndex].value;
      Create_Thesaurus_Merged_Name = Create_Thesaurus_Merged_Name_Input.value;
      
      
  }
  var callServletOK = true;
  if(Create_Thesaurus_1_Name== '' ){
      alert(translate(59));
      //alert('Δεν έχει προσδιοριστεί όνομα για τον πρώτο θησαυρό.');
      callServletOK = false;
  }
  else
  if(Create_Thesaurus_2_Name== '' ){
      alert(translate(60));
      //alert('Δεν έχει προσδιοριστεί όνομα για τον δεύτερο θησαυρό.');
      callServletOK = false;
  }
  else
  if(Create_Thesaurus_Merged_Name ==  '' ){
      alert(translate(61));
      //alert('Δεν έχει προσδιοριστεί όνομα για τον νέο συγχωνευμένο θησαυρό.');
      callServletOK = false;
  }
  else
  if(Create_Thesaurus_1_Name==Create_Thesaurus_2_Name){
      alert(translate(62));
      //alert('Οι δύο θησαυροί προς συγχώνευση πρέπει να έχουν διαφορετικό όνομα.');
      callServletOK = false;
  }
  else
  if( Create_Thesaurus_1_Name==Create_Thesaurus_Merged_Name || Create_Thesaurus_2_Name==Create_Thesaurus_Merged_Name){
      alert(translate(63));
      //alert('Ο νέος θησαυρός πρέπει να έχει διαφορετικό όνομα από τους δύο επιμέρους.');
      callServletOK = false;
  }
  
  
 // alert('Create_Thesaurus_1_Name = ' + Create_Thesaurus_1_Name + '\n\nCreate_Thesaurus_2_Name = ' + Create_Thesaurus_2_Name +'\n\nCreate_Thesaurus_Merged_Name = ' + Create_Thesaurus_Merged_Name );
   
  var trimmedNewName = ThesaurusNameNormalizedString(Create_Thesaurus_Merged_Name);
  if (callServletOK == true) {
    if (!ThesaurusNameIsLengthOk(trimmedNewName)) {
      alert(translate(45));
      //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει από 1 έως και 10 χαρακτήρες.');
      // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
      // otherwise, the last form's action is executed!!
      submitFormTo('Merge_ThesauriForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
    }
    else if (!ThesaurusNameAreCharsOk(trimmedNewName)) {
      alert(translate(46));
      //alert('Το όνομα του νέου θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.');
      // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
      // otherwise, the last form's action is executed!!
      submitFormTo('Merge_ThesauriForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
    }
    else {
      
      //document.all['Merge_Thesaurus_result_textarea_ID'].innerHTML = 'Παρακαλώ περιμένετε...';
      if(confirm(translate(64))){
      //if(confirm('Είστε σίγουρος για την συγχώνευση θησαυρών;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
          DisplayPleaseWaitScreen(true);
          submitFormTo('Merge_ThesauriForm' , 'MergeThesauri');    
      }
      else{
          submitFormTo('Merge_ThesauriForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');  
      }
    }      
  }
  
}
function GetActiveXObject(){
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
    return xmlHttp;
}

function callEditGuideTermServlet( mode ){
    
    var newGuideTerm        = document.getElementById("newGuideTerm");
    var deleteGuideTerm     = document.getElementById("deleteGuideTerm");
    var renameGuideTermFrom = document.getElementById("renameGuideTermFrom");
    var renameGuideTermTo   = document.getElementById("renameGuideTermTo");
    
    var params = "?mode=" + mode;  
    
    params +=    "&newGuideTerm=" + escape(encodeURIComponent(newGuideTerm.value));
    
    if(deleteGuideTerm.selectedIndex >-1){
        params +=    "&deleteGuideTerm=" + escape(encodeURIComponent(deleteGuideTerm.options[deleteGuideTerm.selectedIndex].value));
    }
    if(renameGuideTermFrom.selectedIndex >-1){
        params +=    "&renameGuideTermFrom=" + escape(encodeURIComponent(renameGuideTermFrom.options[renameGuideTermFrom.selectedIndex].value));
    }
    
    params +=    "&renameGuideTermTo=" + escape(encodeURIComponent(renameGuideTermTo.value ));    
    
    var xmlHttp = GetActiveXObject();
    
    xmlHttp.onreadystatechange=function() {

        if(xmlHttp.readyState==4){
            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            var response = txt.replace(/^\s+|\s+$/g,'');
            if (response == "SYSTEM LOCKED") {
              window.location = 'SystemIsUnderMaintenance';
            }
            else if(response =="Session Invalidate"){
                window.location = 'Index?logout=true';
            }
            else if(response =='Success'){
                window.location='Admin_Thesaurus?DIV=EditGuideTerms_DIV'
            } else {
                response = response.slice(7);
                alert(response);
            }            
        }
    }    
  
    DisplayPleaseWaitScreen(true);
    xmlHttp.open("GET",'EditGuideTerms' + params, true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    
    xmlHttp.send(null);    

}



function saveButtonClicked(tableBodyId){
    var table = document.getElementById(tableBodyId);
    var rowCount = table.rows.length;
    var retrunVal ='';
    for(var i=0; i< rowCount ; i++){
        var row = table.rows.item(i);
        retrunVal += row.getElementsByTagName('input')[0].value + " ";
        retrunVal += row.getElementsByTagName('input')[1].value; + " ";
    }

    alert(retrunVal);
}
function addLanguageRow(tableBodyId, targetInput) {
    //var tbody = getObj(tableBodyId);
    var removeLangButton = document.getElementById('removeLanguageImgTemplate').cloneNode(true);
;

    var table = document.getElementById(tableBodyId);
    var rowCount = table.rows.length;
    var row = table.insertRow(rowCount);
    row.setAttribute("class", "translationstablerow");

    var cell1 = row.insertCell(0);
    cell1.setAttribute("class", "translationstablecolumn");
    var element1 = document.createElement("input");
    element1.type = "text";
    element1.name = "LanguageIdentifier";
    cell1.appendChild(element1);
    

    var cell2 = row.insertCell(1);
    cell2.setAttribute("class", "translationstablecolumn");
    var element2 = document.createElement("input");
    element2.name = "LanguageName";
    element2.type = "text";
    cell2.appendChild(element2);

    var cell3 = row.insertCell(2);

    cell3.appendChild(removeLangButton);

    //document.getElementById('removeLanguageImg').style.display='inline';
    
        /*'<tr onMouseOver="this.style.background = \'#feffd0\'" onMouseOut="this.style.background = \'#E8E9BE\'" bgcolor="#E8E9BE" class="translationstablerow">' +
                                '<td class="translationstablecolumn">'+
                                    '<input type="text"/>'+
                                '</td>'+
                                '<td class="translationstablecolumn">'+
                                    '<input type="text"/>'+
                                '</td>'+
                            '</tr>';*/
    //var newRow = row.cloneNode(true);
    
    /*
    // BUG fixed by karam (21/1/2008) - START
    // set type ahead mechanism for the new created inputvalue
    // and set its value to blank ('')
    var inputvaluesList = document.getElementsByName(targetInput);
    var inputvaluesLength = inputvaluesList.length;
    if (inputvaluesLength != 0) {
        var lastCreatedInput = inputvaluesList[inputvaluesLength-1];
        lastCreatedInput.value = '';

        //disable type ahead in case of facets and hierarchies untill it is fully supported.
        //if(targetInput == 'inputvalue'){

        // the following two lines are copied from <body onload= ...> declaration of page.xsl
          var oTextbox = new AutoSuggestControl(lastCreatedInput, new RemoteStateSuggestions(),targetInput);
        

      // check if the last added-cloned PedioDropDown contains 2 date operators (>= and <=) => remove them
      var PedioDropDowns = document.getElementsByName("input_term");
      if(this.InputSearchFieldChanged!=null){
          InputSearchFieldChanged(PedioDropDowns[PedioDropDowns.length-1]);
      }
    }
    */
    // BUG fixed by karam (21/1/2008) - END
}


function removeLanguageRow(table) {
    len = table.rows.length;
    if(len > 0) {
        table.deleteRow(len-1);
    }

    if(table.rows.length==0){
        document.getElementById('removeLanguageImg').style.display='none';
    }
}


function checkSkosConfiguration(){
    if($('#radioSKOS').is(':checked')) { 
        $('#skosConceptSchemeConfigurationRowId').css('visibility', 'visible'); 
        $('#skosBaseNameSpaceConfigurationRowId').css('visibility', 'visible'); 
        
        //var currentVal = $('#skosBaseNameSpaceId').val();
        //if(!currentVal || currentVal===""){
        $('#skosConceptSchemeId').val(computeSkosDefaultConceptScheme());
        $('#skosBaseNameSpaceId').val(computeSkosDefaultBaseNameSpace());
        //}
    }else{  
        $('#skosConceptSchemeConfigurationRowId').css('visibility', 'hidden'); 
        $('#skosBaseNameSpaceConfigurationRowId').css('visibility', 'hidden'); 
    }
}

function computeSkosDefaultConceptScheme(){
    
    
    var retVal = window.location.origin;
    var baseStr = window.location.pathname;
    var parts = baseStr.split("/");
    if(parts && parts.length>0)
    {        
        for(var partIndex=0;  partIndex < parts.length ; partIndex++){
            var part = parts[partIndex];
            if(part && part.length>0){
                retVal +="/"+part;
                break;
            }
        }        
    }    
    var selectedThes = $('#exportThesaurus_ID :selected').text();
    if(selectedThes && selectedThes.length>0){
        retVal+="#"+selectedThes;
    }
    
    return retVal;
}

function computeSkosDefaultBaseNameSpace(){
    var retVal = window.location.origin;
    var baseStr = window.location.pathname;
    var parts = baseStr.split("/");
    if(parts && parts.length>0)
    {        
        for(var partIndex=0;  partIndex < parts.length ; partIndex++){
            var part = parts[partIndex];
            if(part && part.length>0){
                retVal +="/"+part;
                break;
            }
        }        
    }    
    var selectedThes = $('#exportThesaurus_ID :selected').text();
    if(selectedThes && selectedThes.length>0){
        retVal+="/"+selectedThes;
    }
    
    return retVal;
    
    
}