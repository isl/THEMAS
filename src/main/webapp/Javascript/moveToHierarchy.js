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
/*-----------------------------------------------------
              MTH_ToHierListChanged()
-------------------------------------------------------*/      
function MTH_ToHierListChanged(DestinationHierList_ID) {	
    var DestinationHierList= document.getElementById(DestinationHierList_ID);
    // get the selected Hierarchy to move to
    var currentSelectedHierarchy = escape(encodeURIComponent(DestinationHierList.options[DestinationHierList.selectedIndex].value));
    //alert('searching for the terms of hierarchy: ' + currentSelectedHierarchy);
    
    var MoveBTtermList = document.getElementById('MoveBTterm');
    //alert(MoveBTtermList.options[0].value);
    
    // todo: get with Ajax the terms of the currentSelectedHierarchy
    var parametersStr = 'DBqueryID=GET_TERMS_OF_HIERARCHY' + '&targetHierarchy=' + currentSelectedHierarchy;
    var xmlHttp = getAjaxActiveXObject();//new ActiveXObject("Msxml2.XMLHTTP");
    /*-----------------------------------------------------
                onreadystatechange()
    informs the MoveBTtermList options
  -------------------------------------------------------*/        
    xmlHttp.onreadystatechange = function() {
        
        if (xmlHttp.readyState == 4||xmlHttp.readyState == "complete")  {
            // ENABLE and FOCUS DestinationHierList when Ajax servlet is finished 
            // (bug fix of server crash after repeating presses of down arrow in the list)    
            DestinationHierList.disabled = "";
            DestinationHierList.focus();
            
            // alert(xmlHttp.responseText);
            var termsString = xmlHttp.responseText;
            
            if(termsString.length < 30){ //Avoid the following replacement in case of a lengthy answer
                termsString = termsString.replace(/^\s+|\s+$/g,'');
            }
            
            if(termsString  ==  'Session Invalidate'){
                window.location = 'Index';
            }
            else{ 
                //alert('>>>>>>>>>>>' + termsString);
                var termsArray = [];
                termsArray = termsString.split("###");
                var termsArrayLength = termsArray.length;
                // clear the MoveBTtermList
                MoveBTtermList.options.length = 0;      
                for (var i = 0; i < termsArrayLength; i++) {
                    var displayValue = /*/i + 1 + '. ' +*/ termsArray[i];
                    //var displayValue = termsArray[i];
                    // cut the displayValue with suffix '...' in case its length is > 44
                    var displayValueLength = displayValue.length; 
                    if (displayValueLength > 44) {
                        displayValue = displayValue.substring(0, 44) + '...';
                    }
                    MoveBTtermList.options[i] = new Option(displayValue, termsArray[i]); // 1st parameter: display value, 2nd: option value
                }      
            }
        }
    }    
    
    // DISABLE DestinationHierList until Ajax servlet is finished 
    // (bug fix of server crash after repeating presses of down arrow in the list)
    DestinationHierList.disabled = "disabled";

    //var parametersStr = '?' + attributeName + '=' + attributeValue;
    xmlHttp.open("POST","AjaxDBQuery", true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");  
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded"); 
    xmlHttp.setRequestHeader("Content-length", parametersStr.length); 
    xmlHttp.setRequestHeader("Connection", "close");             
    xmlHttp.send(parametersStr);    	
}

/*-----------------------------------------------------
              MTH_DeleteBTButtonPressed()
-------------------------------------------------------*/      
function MTH_DeleteBTButtonPressed() {	
  //submitFormTo('moveToHierarchyForm' , 'MoveToHierarchyResults?action=deleteBT');
  //alert('hrer');
  
  //var actionField = document.getElementById('moveToHierarchyActionInput');
  //actionField.value= 'deleteBT';
  //getServletResult('MoveToHierarchyResults' ,'moveTo_HierarchyFieldSetID' , 'RefreshPage');
  //collect Parameters
  
  var fieldSetParams = collectInputsOfFieldSet('moveTo_HierarchyFieldSetID');
  
  var xmlHttp = getAjaxActiveXObject();
    DisplayPleaseWaitScreen(true);

    xmlHttp.onreadystatechange=function() {
        
        if(xmlHttp.readyState==4){
            
            var txt = xmlHttp.responseText;
            /*if(txt== '      '){//instead of Success one it appeared in the screen so return something invisible
                window.location.reload( true );
                return;
            }*/
            if(txt.length < 30){ //Avoid the following replacement in case of a lengthy answer
                txt = txt.replace(/^\s+|\s+$/g,'');
            }
            
            if(txt  ==  'Session Invalidate'){
                window.location = 'Index';
            }
            else{ 
                
                var content = xmlHttp.responseText.substring(txt.indexOf('<body>')+6,txt.indexOf('</body>'));
                //alert(content);
                //DisplayPleaseWaitScreen(false);
                JustRemovePleasWait();
                
                // alert('termName Done ' + termName);
                
                var promptbox = document.createElement('div'); 
                promptbox.setAttribute ('id' , 'Editprompt'); 
                document.getElementById('EditCardArea').appendChild(promptbox);
                document.getElementById('EditCardArea').style.zIndex = 400;
                //promptbox = eval("document.getElementById('prompt').style") ;
                
                
                
                document.getElementById('Editprompt').innerHTML = content;
                promptbox.style.visibility='visible' ;
                promptbox.focus() ;
                //alert(content);
            }
            
        }
    }    
  
    
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("GET",'MoveToHierarchyResults'+fieldSetParams+'action=deleteBT' , true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    
    xmlHttp.send(null);    
    
  //window.location = 'MoveToHierarchyResults'+fieldSetParams+'action=deleteBT';
  
  
}

/*-----------------------------------------------------
              MTH_SaveButtonPressed()
-------------------------------------------------------*/      
function MTH_SaveButtonPressed() {	
  // check if user has selected the necessary input values (MoveΤοHierarchy, MoveBTterm)
  var DestinationHierList = document.getElementById('DestinationHierList');
  var DestinationHierListSelectedIndex = DestinationHierList.selectedIndex;
  var MoveBTtermList = document.getElementById('MoveBTterm');
  var MoveBTtermListSelectedIndex = MoveBTtermList.selectedIndex;  
  
  if (DestinationHierListSelectedIndex < 0) {
    alert(translate(30));
    //alert('Επιλέξτε μία Ιεραρχία προς την οποία θα γίνει η μετακίνηση');
    return;
    //submitFormTo('moveToHierarchyForm' , '');
  }
  else if (MoveBTtermListSelectedIndex < 0) {
    alert(translate(31));
    //alert('Επιλέξτε έναν ευρύτερο όρο της Ιεραρχίας προς την οποία θα γίνει η μετακίνηση');
    return;
    //submitFormTo('moveToHierarchyForm' , '');  
  }
  else {
    //submitFormTo('moveToHierarchyForm' , 'MoveToHierarchyResults?action=moveToHier');  
    

  
      //var actionField = document.getElementById('moveToHierarchyActionInput');
      //actionField.value= 'moveToHier';
      
      //collect Parameters
      var fieldSetParams = collectInputsOfFieldSet('moveTo_HierarchyFieldSetID');
        
var xmlHttp = getAjaxActiveXObject();

    DisplayPleaseWaitScreen(true);
    xmlHttp.onreadystatechange=function() {
        
        if(xmlHttp.readyState==4){
            
            var txt = xmlHttp.responseText;
            
            if(txt.length < 30){ //Avoid the following replacement in case of a lengthy answer
                txt = txt.replace(/^\s+|\s+$/g,'');
            }
            
            if(txt  ==  'Session Invalidate'){
                window.location = 'Index';
            }
            else{ 
                var content = xmlHttp.responseText.substring(txt.indexOf('<body>')+6,txt.indexOf('</body>'));
                //alert(content);
                //DisplayPleaseWaitScreen(false);
                JustRemovePleasWait();
                // alert('termName Done ' + termName);
                
                var promptbox = document.createElement('div'); 
                promptbox.setAttribute ('id' , 'Editprompt'); 
                document.getElementById('EditCardArea').appendChild(promptbox);
                document.getElementById('EditCardArea').style.zIndex = 400;
                document.getElementById('Editprompt').innerHTML = content;
                
                promptbox.style.visibility='visible' ;
                promptbox.focus() ;
                
                //alert(content);
            }
        }
    }    
  
    
    //xmlHttp.open("POST",servletName, false);
    xmlHttp.open("GET",'MoveToHierarchyResults'+fieldSetParams+'action=moveToHier' , true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    
    xmlHttp.send(null);    
            
      //window.location =   'MoveToHierarchyResults' + fieldSetParams+'action=moveToHier';
      //getServletResult('MoveToHierarchyResults' ,'moveTo_HierarchyFieldSetID' , 'RefreshPage');
      
  }
}
  
  function collectInputsOfFieldSet(fieldSetID){
      
      var targetFieldSet = document.getElementById(fieldSetID);            
      var fieldSetParams ="?";
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
                  fieldSetParams += inputs[i].name + "=" + escape(encodeURIComponent(inputs[i].value)) + "&" ;
          }
          
      }
      var textareas = targetFieldSet.getElementsByTagName('textarea');    
      for ( i=0; i< textareas.length; i++) {

              if(textareas[i].name!= '')
                  fieldSetParams += textareas[i].name + "=" + escape(encodeURIComponent(textareas[i].value)) + "&" ;
      }
      
      /* difference from getServletResult() function
      for ( i=0; i< selects.length; i++) {

          if(servletName != 'Create_Modify_Hierarchy'){
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
      */
      var selects = targetFieldSet.getElementsByTagName("select");

      for ( i=0; i< selects.length; i++) {
        if(selects[i].name!= '' && selects[i].selectedIndex >=0 )
          fieldSetParams += selects[i].name + "=" + escape(encodeURIComponent(selects[i].options[selects[i].selectedIndex].value)) + "&";
         
      }
      return fieldSetParams;
  
}
