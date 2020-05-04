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

function copyToClipborad(refLink,refLinkTxtDiv, msgtemplate, txtToCopy){
    el = document.createElement('textarea');
    el.value = txtToCopy;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
    
    $("#"+refLinkTxtDiv).html(msgtemplate.replace('%s',txtToCopy));
    $("#"+refLink).show().delay(2000).fadeOut();
    
    
    
    
    /*
    var tt = $("#"+refLink);
        tt.show().delay(2000).hide();
      
      */
   
   //alert ('Copied: ' + txtToCopy +" to clipboard");
    
}

function getAjaxActiveXObject() {
    var xmlHttp;
    try {  // Firefox, Opera 8.0+, Safari  
        xmlHttp = new XMLHttpRequest();
    }
    catch (e) {  // Internet Explorer  
        try {
            xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e) {
            try {
                xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e) {
                alert("Your browser does not support AJAX!");
                return false;
            }
        }
    }
    return xmlHttp;
}
function setLangCode(selectBoxId, targetLangId){
    var selectBox = document.getElementById(selectBoxId);
    if(selectBox && selectBox.selectedIndex>=0){
        var inputBox = document.getElementById(targetLangId);
        if(inputBox){
            inputBox.value = selectBox.options[selectBox.selectedIndex].value;
        }
        
        var currentLocation = window.location.href;
        var newLocation = '';
        var newValue = 'lang='+selectBox.options[selectBox.selectedIndex].value;
        if(currentLocation.indexOf('lang=')>0){
            
            newLocation  = currentLocation.replace(/lang=[a-z]+/mg, newValue)
        }
        else{
            if(currentLocation.indexOf('?')>0){
                newLocation = currentLocation+'&'+newValue;
            }
            else{
                newLocation = currentLocation+'?'+newValue;
            }        

        }
        
        window.location.href = newLocation;
    }
    
    
}

/*-----------------------------------------------------
 getObj()
 -------------------------------------------------------*/
function getObj(objId) {
    //alert(objId);
    return document.getElementById(objId)
}


/*-----------------------------------------------------
 submitFormTo()
 -------------------------------------------------------*/
function submitFormTo(formId, formAction) {
    var form = getObj(formId);
    form.action = formAction;
    //form.submit();
}

function trim(str, chars) {
    return ltrim(rtrim(str, chars), chars);
}

function ltrim(str, chars) {
    chars = chars || "\\s";


    return str.replace(new RegExp("^[" + chars + "]+", "g"), "");

}

function rtrim(str, chars) {
    chars = chars || "\\s";
    return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
}

function THEMAS_HiddenActionsDIV(currentDiv) {

    document.getElementById('THEMAS_HiddenActions_DIV').style.visibility = 'hidden';
    document.getElementById('THEMAS_HiddenTranslations_DIV').style.visibility = 'hidden';
    document.getElementById('THEMAS_HiddenFixData_DIV').style.visibility = 'hidden';
    document.getElementById('THEMAS_HiddenSystemConfigurations_DIV').style.visibility = 'hidden';
    document.getElementById(currentDiv).style.visibility = 'visible';

    var LinkId = currentDiv.replace('_DIV', '_LINK');
    document.getElementById('THEMAS_HiddenActions_LINK').className = "inactive";
    document.getElementById('THEMAS_HiddenTranslations_LINK').className = "inactive";
    document.getElementById('THEMAS_HiddenFixData_LINK').className = "inactive";
    document.getElementById('THEMAS_HiddenSystemConfigurations_LINK').className = "inactive";
    document.getElementById(LinkId).className = "active";

}

function callSystemConfigurationsServlet() {

    var i;

    var Delimeter = document.getElementById('DELIMETER_ID').value;
    //perform field valdation checks
    var ListStep = document.getElementById('ListStep_ID').value;
    var ListStepRegExp = /\b[1-9]{1}[0-9]*\b/;
    var firstMatch = ListStep.match(ListStepRegExp);
    if (firstMatch == null || firstMatch[0] != ListStep) {
        alert(translate(0));
        //alert('Το πλήθος αποτελεσμάτων ανά σελίδα πρέπει να είναι θετικός ακέραιος αριθμός.');
        return;
    }

    var AutoBackupStartTimeStr = document.getElementById('AutoBackupStartTime_ID').value;

    var AutoBackupStartTimeRegExpr = new RegExp('\\b([0-9]|1[0-9]|2[0-3])' + Delimeter + '([0-9]|[1-5][0-9])' + Delimeter + '([0-9]|[1-5][0-9])\\b');
    firstMatch = AutoBackupStartTimeStr.match(AutoBackupStartTimeRegExpr);
    if (firstMatch == null || firstMatch[0] != AutoBackupStartTimeStr) {
        alert(translate(1));
        //alert('Η ώρα έναρξης των αυτομάτων αντιγράφων ασφαλείας πρέπει να δίνεται στην μορφή: ΩΩ:::ΛΛ:::ΔΔ όπου\nΩΩ: Ωρα (ακέραιος αριθμός από 0 έως 23)\nΛΛ: Λεπτά (ακέραιος αριθμός από 0 έως 59)\nΔΔ: Δευτερόλεπτα (ακέραιος αριθμός από 0 έως 59)');
        return;
    }


    var AutoBackupInterval = document.getElementById('AutoBackupInterval_ID').value;
    firstMatch = AutoBackupInterval.match(ListStepRegExp);
    if (firstMatch == null || firstMatch[0] != AutoBackupInterval) {
        alert(translate(2));
        //alert('Η περιοδικότητα των αυτομάτων αντιγράφων ασφαλειας πρέπει να είναι θετικός ακέραιος αριθμός.');
        return;
    }
    /*
     var mailListStr = document.getElementById('mailList_ID').value.replace(/^\s+|\s+$/g, '');
     var mailHoststr = document.getElementById('mailHost_ID').value.replace(/^\s+|\s+$/g, '');
     
     if(mailHoststr!=null && mailHoststr!=''&& mailListStr!=null && mailListStr!=''){
     
     var mailList = mailListStr.split(Delimeter);
     var mailRegExp = /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}\b/;
     
     for(i=0; i<mailList.length; i++){
     firstMatch = mailList[i].match(mailRegExp);
     if(firstMatch==null || firstMatch[0]!=mailList[i]){
     alert(translate(3)+mailList[i]+translate(4));
     //alert('Η e-mail διεύθυνση: \''+mailList[i]+'\' δεν είναι έγκυρη.');
     return;
     }       
     }
     
     
     var mailHostRegExp = /\b[A-Za-z0-9._%+-]+\b/;
     firstMatch = mailHoststr.match(mailHostRegExp);
     
     if(firstMatch==null || firstMatch[0] != mailHoststr){
     alert(translate(5));
     //alert('O email server που δηλώθηκε δεν είναι έγκυρος.');
     return;
     }
     }
     else{
     if(mailHoststr!=null && mailHoststr!=''){
     alert(translate(6));
     //alert('Δεν έχει δηλωθεί λίστα email διευθύνσεων.');
     return;
     }
     else if(mailListStr && mailListStr!=''){
     alert(translate(7));
     //alert('Δεν έχει δηλωθεί mail server.');
     return;
     }
     //else no mailserver or mail list
     }
     */

    if (confirm(translate(8)) == false) {
        return;
    }

    var targetFieldSet = document.getElementById('SystemConfigurations_Fieldset_Id');
    var fieldSetParams = '?';

    var inputs = targetFieldSet.getElementsByTagName('input');
    for (i = 0; i < inputs.length; i++) {
        if (inputs[i].type == "text") {
            if (inputs[i].name != '') {
                fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&";
            }
        }
    }

    var xmlHttp = getAjaxActiveXObject();
    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4) {

            var txt = xmlHttp.responseText;
            var response = txt.replace(/^\s+|\s+$/g, '');
            if (response == 'Session Invalidate') {
                window.location = 'Index';
            }
            else {
                DisplayPleaseWaitScreen(false);
                window.location = 'LoginAdmin';
            }

        }
    }

    DisplayPleaseWaitScreen(true);
    xmlHttp.open("GET", 'SystemConfigurations' + fieldSetParams, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");

    xmlHttp.send(null);

}


function callAdminFixServlet(servlet, mode, functionallity) {

    var setThesaurus_NewName_Input = document.getElementById('FixThesData_Admin_Id');
    var setThesaurus_NewName = setThesaurus_NewName_Input.value;
    setThesaurus_NewName_Input.value = setThesaurus_NewName;

    var trimmed = setThesaurus_NewName.replace(/^\s+|\s+$/g, '');
    trimmed = trimmed.toUpperCase();
    if (trimmed.length == 0 || trimmed.length > 10) {
        alert(translate(9));
        //alert('Το όνομα του επιλεγμένου θησαυρού πρέπει να περιέχει από 1 έως και 10 χαρακτήρες.');
        return;
        //submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');
    }
    else if (trimmed != trimmed.match('[A-Z0-9]+')) {
        alert(translate(10));
        //alert('Το όνομα του επιλεγμένου θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.');
        return;
    }


    // ATTENTION!!!! in case of no thesaurus selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    //submitFormTo('Create_ThesaurusForm' , 'Admin_Thesaurus?DIV=CreateThesaurus_DIV');      
    //}
    //alert('Προσωρινά μη διαθέσιμη λειτουργία.');
    //return;
    var xmlHttp = getAjaxActiveXObject();
    var params = "?mode=" + mode + "&functionallity=" + functionallity + "&selectedThesaurus=" + escape(encodeURIComponent(trimmed));

    //alert(params)
    var windowName = ConstructWindowName('', '');

    var pleaseWaitWindow;
    if (mode == "Preview") {
        var x = window.screenLeft + 50;
        var y = window.screenTop + 20;
        // todo: location=yes (ONLY for debugging) - to be: location=no
        var windowConfiguration = 'left=' + x + ',top=' + y + ',width=' + 668 + ',height=' + 400 + ',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
        pleaseWaitWindow = window.open('WaitForDownload', windowName, windowConfiguration);
        pleaseWaitWindow.focus();
    }

    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4) {

            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            var response = txt.replace(/^\s+|\s+$/g, '');
            if (response == "SYSTEM LOCKED") {
                window.location = 'SystemIsUnderMaintenance';
            }
            else
            if (response == 'Session Invalidate') {
                window.location = 'Index';
            }
            else {

                //alert(response);
                var checkSuccess = response.substring(0, 7);

                if (checkSuccess == 'Failure') {
                    var msg = response.slice(7);
                    if (msg == 'THESAURUS_NOT_FOUND') {
                        alert(translate(11) + trimmed + translate(12));
                        //msg = 'Ο θησαυρός ' + trimmed + ' δεν βρέθηκε στην βάση.';
                    }
                    //window.location.reload(true);
                    alert(msg);
                }
                else
                if (mode == "Preview") {


                    pleaseWaitWindow.location = response;
                    pleaseWaitWindow.focus();
                }
                else
                if (mode == "Fix") {
                    if (response == "Retry") {
                        alert(translate(13));
                        //alert("Δεν διορθώθηκαν όλα τα δεδομένα.\n\nΠαρακαλώ επαναλάβετε την \nτελευταία ενέργεια διόρθωσης.");
                    }
                }
            }
        }
    }

    DisplayPleaseWaitScreen(true);
    xmlHttp.open("GET", servlet + params, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");

    xmlHttp.send(null);

}

function innerPrepareResultsCommon(servletName, hierName, display, usePreviousCriteria, removeBlackScreen) {

    DisplayPleaseWaitScreen(true);
    var windowName = ConstructWindowName('', '');
    var x = window.screenLeft + 50;
    var y = window.screenTop + 20;
    // todo: location=yes (ONLY for debugging) - to be: location=no
    var windowConfiguration = 'left=' + x + ',top=' + y + ',width=' + 668 + ',height=' + 400 + ',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
    var pleaseWaitWindow = window.open('WaitForDownload', windowName, windowConfiguration);

    pleaseWaitWindow.focus();

    var params = '?usePreviousCriteria=' + usePreviousCriteria;
    if (hierName != '') {
        params += '&hierarchy=' + escape(encodeURIComponent(hierName)) + '&action=' + display;
    }
    else {
        params += '&pageFirstResult=SaveAll';
    }

    var xmlHttp = getAjaxActiveXObject();

    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4)
        {
            if (removeBlackScreen) {
                DisplayPleaseWaitScreen(false);
            }
            else {
                JustRemovePleasWait();
            }
            //

            var txt = xmlHttp.responseText;
            txt = txt.replace(/^\s+|\s+$/g, '');

            if (txt == 'Session Invalidate') {
                pleaseWaitWindow.close();
                window.location = 'Index';
            }
            else {
                //alert(txt);
                //window.open('DownloadFile?targetFile='+txt, windowName, windowConfiguration);
                pleaseWaitWindow.location = /*'Save_Results_Displays/Save_Results_temporary_files/' +*/ txt;  // 'DownloadFile?targetFile='+txt ;
                pleaseWaitWindow.focus();
            }


        }
    }
    //DisplayPleaseWaitScreen(true);

    //xmlHttp.open("GET",servletName + '?pageFirstResult=SaveAll', false);
    xmlHttp.open("GET", servletName + params, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");

    xmlHttp.send(null);

}
function prepareResultsWithoutRemoveBackGroundBlackScreen(servletName, hierName, display, usePreviousCriteria) {

    innerPrepareResultsCommon(servletName, hierName, display, usePreviousCriteria, false);
}
function prepareResults(servletName, hierName, display, usePreviousCriteria) {

    innerPrepareResultsCommon(servletName, hierName, display, usePreviousCriteria, true);
}

function downloadFile(servletName, hierName, display, usePreviousCriteria) {

    
    downloadFileType(servletName, hierName, display, usePreviousCriteria, 'XML');
}

function downloadFileType(servletName, hierName, display, usePreviousCriteria, extraParams){
    
    DisplayPleaseWaitScreen(true);
    //alert('edw');
    var params = '?usePreviousCriteria=' + usePreviousCriteria;
    if (hierName != '') {
        params += '&hierarchy=' + escape(encodeURIComponent(hierName)) + '&action=' + display;
    }
    else {
        params += '&pageFirstResult=SaveAll';
    }
    if(extraParams =='XML'){
        params += '&answerType=XML';
    }
    if(extraParams =='RDF'){
        params += '&answerType=RDF';
    }
    var xmlHttp = getAjaxActiveXObject();

    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4)
        {
            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            txt = txt.replace(/^\s+|\s+$/g, '');

            if (txt == 'Session Invalidate') {
                window.location = 'Index';
            }
            else {

                window.location = 'DownloadFile?targetFile=' + txt;
            }


        }
    }
    xmlHttp.open("GET", servletName + params, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");

    xmlHttp.send(null);
}



function checkPageNumber(servlet, step, pageNum) {

    var patternNum = /\d+/g;
    var result = pageNum.match(patternNum);
    if (result != null && result.length == 1 && result[0] == pageNum && parseInt(pageNum) > 0) {

        window.location = servlet + '?pageFirstResult=' + parseInt((parseInt(step) * (parseInt(pageNum) - 1)) + 1)
    }
    else {
        alert(translate(14));
        DisplayPleaseWaitScreen(false);
        //alert('Εσφαλμένος αριθμός σελίδας.');
    }
}

function popUpCard(objectName, servlet) {


    DisplayPleaseWaitScreen(true);
    var params = '';
    if (servlet.toLowerCase().indexOf('term') != -1) {
        params = 'term=' + escape(encodeURIComponent(objectName));
    }
    if (servlet.toLowerCase().indexOf('hierarchy') != -1) {
        params = 'hierarchy=' + escape(encodeURIComponent(objectName));
    }
    if (servlet.toLowerCase().indexOf('facet') != -1) {
        params = 'facet=' + escape(encodeURIComponent(objectName));
    }
    if (servlet.toLowerCase().indexOf('source') != -1) {
        params = 'source=' + escape(encodeURIComponent(objectName));
    }

    var xmlHttp = getAjaxActiveXObject();

    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4) {

            var txt = xmlHttp.responseText;

            if (txt.length < 30) { //Avoid the following replacement in case of a lengthy answer
                txt = txt.replace(/^\s+|\s+$/g, '');
            }

            if (txt == 'Session Invalidate') {
                window.location = 'Index';
            }
            else {
                var content = xmlHttp.responseText.substring(txt.indexOf('<body>') + 6, txt.indexOf('</body>'));

                //DisplayPleaseWaitScreen(false);
                JustRemovePleasWait();
                // alert('termName Done ' + termName);

                var promptbox = document.createElement('div');
                promptbox.setAttribute('id', 'prompt');
                document.getElementById('DisplayCardArea').appendChild(promptbox);
                document.getElementById('DisplayCardArea').style.zIndex = 400;
                document.getElementById('prompt').innerHTML = content;
                promptbox.style.visibility = 'visible';
                promptbox.focus();
            }
        }
    }


    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST", servlet, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    xmlHttp.send(params);


}

function showEditCard_Source(targetSource) {
    DisplayPleaseWaitScreen(true);
    var params = 'source=' + escape(encodeURIComponent(targetSource));
    document.getElementById('DisplayCardArea').innerHTML = '';
    window.location = 'CardOf_Source?' + params + '&mode=edit';
}

function showEditCard_Term(targetTerm) {
    DisplayPleaseWaitScreen(true);
    var params = 'term=' + escape(encodeURIComponent(targetTerm));
    document.getElementById('DisplayCardArea').innerHTML = '';
    window.location = 'CardOf_Term?' + params + '&mode=edit';
}

function showEditCard_Hierarchy(targetHierarchy) {
    DisplayPleaseWaitScreen(true);
    var params = 'hierarchy=' + escape(encodeURIComponent(targetHierarchy));
    document.getElementById('DisplayCardArea').innerHTML = '';
    window.location = 'CardOf_Hierarchy?' + params + '&mode=edit';
}

function showEditCard_Facet(targetFacet) {
    DisplayPleaseWaitScreen(true);
    var params = 'facet=' + escape(encodeURIComponent(targetFacet));
    document.getElementById('DisplayCardArea').innerHTML = '';
    window.location = 'CardOf_Facet?' + params + '&mode=edit';
}

function showEditFieldCard(objectName, targetObjectField, servlet) {
    DisplayPleaseWaitScreen(true);
    var params = '';
    if (servlet.toLowerCase().indexOf("term") != -1 || servlet == 'MoveToHierarchy') {
        params += 'targetTerm=' + escape(encodeURIComponent(objectName)) + "&targetField=" + escape(encodeURIComponent(targetObjectField));
    }
    else if (servlet.toLowerCase().indexOf("hierarchy") != -1) {
        params += 'targetHierarchy=' + escape(encodeURIComponent(objectName)) + "&targetField=" + escape(encodeURIComponent(targetObjectField));
    }
    else if (servlet.toLowerCase().indexOf("facet") != -1) {
        params += 'targetFacet=' + escape(encodeURIComponent(objectName)) + "&targetField=" + escape(encodeURIComponent(targetObjectField));
    }
    else if (servlet.toLowerCase().indexOf("source") != -1) {
        params += 'targetSource=' + escape(encodeURIComponent(objectName)) + "&targetField=" + escape(encodeURIComponent(targetObjectField));
    }
    else if (servlet.toLowerCase().indexOf("user") != -1) {
        if (targetObjectField == 'rename_target_and_older_user_edit' || targetObjectField == 'user_create_and_merge_with_older') {
            params += objectName;
        }
        else {
            params += 'user=' + escape(encodeURIComponent(objectName)) + "&targetEditField=" + escape(encodeURIComponent(targetObjectField));
        }

    }


    var xmlHttp = getAjaxActiveXObject();

    xmlHttp.onreadystatechange = function () {

        if (xmlHttp.readyState == 4) {

            var txt = xmlHttp.responseText;
            if (txt.length < 30) { //Avoid the following replacement in case of a lengthy answer
                txt = txt.replace(/^\s+|\s+$/g, '');
            }

            if (txt == 'Session Invalidate') {

                window.location = 'Index';

            }
            else {
                var content = xmlHttp.responseText.substring(txt.indexOf('<body>') + 6, txt.indexOf('</body>'));
                //alert(content);
                //DisplayPleaseWaitScreen(false);
                JustRemovePleasWait();
                //DisplayBlackBackground(true);
                // alert('termName Done ' + termName);


                var promptbox = document.createElement('div');
                promptbox.setAttribute('id', 'Editprompt');

                if (targetObjectField.indexOf('create') != -1 || (servlet.toLowerCase().indexOf("user") != -1)) {
                    //setAttribute("z-index", 2005);
                    //eg: term_create, hierarchy_create, facet_create, user_create
                    document.getElementById('DisplayCardArea').appendChild(promptbox);
                    document.getElementById('DisplayCardArea').style.visibility = 'hidden';
                    document.getElementById('DisplayCardArea').style.visibility = 'visible';
                    document.getElementById('DisplayCardArea').style.zIndex = 400;

                }
                else {
                    document.getElementById('EditCardArea').appendChild(promptbox);
                    document.getElementById('EditCardArea').style.zIndex = 400;
                }
                //promptbox = eval("document.getElementById('prompt').style") ;
                
                document.getElementById('Editprompt').innerHTML = content;                                

               if(content.indexOf('modify_term_scope_note_id') != -1)
               {
                   CKEDITOR.replace( 'modify_term_scope_note_id' ); 
               }
               else if(content.indexOf('modify_source_source_note_id') != -1)
               {
                   CKEDITOR.replace( 'modify_source_source_note_id' );
               }
               else if(content.indexOf('modify_term_historical_note_id') != -1)
               {
                   CKEDITOR.replace( 'modify_term_historical_note_id' );
               }    
               
               var textareaList = document.getElementsByTagName('textarea');
                for(var i=0; i<textareaList.length; i++){
                    //trims the "modify_term_translations_scope_note_id_XX" to get the suffix that is the languange (EN,DE,GRC...)
                    var lang_suffix = textareaList[i].id.toString().substring(39); 
                    CKEDITOR.replace( 'modify_term_translations_scope_note_id_'+ lang_suffix); 
                }
               
                promptbox.style.visibility = 'visible';
                promptbox.focus();
                
                updateSelectDropDowns();
            }
        }
    }

    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST", servlet, true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    xmlHttp.send(params);

}



function GuideTermsCollectFunction() {
    var fieldSetParams = '';

    var Nts = document.getElementsByName('NtName');
    for (var i = 0; i < Nts.length; i++) {
        fieldSetParams += 'NtName=' + escape(encodeURIComponent(Nts[i].value)) + '&';
    }
    var GuideTerms = document.getElementsByName('GuideTerm');
    for (var i = 0; i < GuideTerms.length; i++) {
        fieldSetParams += 'GuideTerm=' + escape(encodeURIComponent(GuideTerms[i].value)) + '&';
    }



    fieldSetParams += 'targetTerm=' + escape(encodeURIComponent(document.getElementById('tagetTerm-GuideTermId').value));
    fieldSetParams += '&targetEditField=' + escape(encodeURIComponent(document.getElementById('targetEditField-GuideTermId').value));

    var xmlHttp = getAjaxActiveXObject();

    xmlHttp.onreadystatechange = function () {

        var response;

        //responseText MUST be:
        //
        //Session Invalidate --> user is redirected to index while previous session has already been set invalid
        //Success + message
        //ErrorMessage

        if (xmlHttp.readyState == 4) {

            //DisplayPleaseWaitScreen(false);

            var txt = xmlHttp.responseText;
            var check = false;

            if (txt.length < 30) {
                response = txt.replace(/^\s+|\s+$/g, '');
                if (response == 'Session Invalidate') {
                    window.location = 'Index';
                }
                else {
                    check = true;
                }
            }
            else {
                check = true;
            }

            if (check) {
                response = txt.replace(/^\s+|\s+$/g, '');
                //    var moveToSource   = document.getElementById('source_move_refs_to_id');                        
                if (response == 'Success') {
                    cancelAction();//just updates view
                }

                else {
                    response = response.slice(7);
                    document.getElementById('resultOf_Edit').innerHTML = response;
                    //DisplayPleaseWaitScreen(false);
                    cancelAction();
                }
            }
        }
    }

    DisplayPleaseWaitScreen(true);
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("POST", 'EditActions_Term', true);

    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    xmlHttp.send(fieldSetParams);

}

 