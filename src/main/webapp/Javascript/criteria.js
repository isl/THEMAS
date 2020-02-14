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
/*-----------------------------------------------------
                  InputSearchFieldOnChange()
-------------------------------------------------------
  INPUT: - targetPedioDropDown: the <select> Pedio DropDown item changed in Search Terms card
  FUNCTION: - in case of <select> item changed being 'created_on'/'modified_on',
              adds 2 date operators (>= and <=) in the corresponding 
              'op_term' <select> item (in case it has not them already)
            - in case of <select> item changed NOT being 'created_on'/'modified_on',
              removes 2 date operators (>= and <=) from the corresponding 
              'op_term' <select> item (in case it has them already)  
-------------------------------------------------------*/      
function InputSearchFieldOnChange(targetPedioDropDown) {
  // get all existing PedioDropDowns and TelesthsDropDowns
  var PedioDropDowns = document.getElementsByName("input_term");
  var TelesthsDropDowns = document.getElementsByName("op_term");
  // check if targetPedioDropDown is related to Dates
  var targetPedioDropDownIsDate = false;
  if (targetPedioDropDown.value == 'created_on' || targetPedioDropDown.value == 'modified_on') {
    targetPedioDropDownIsDate = true;
  }
  // get the corresponding to targetPedioDropDown 'op_term' <select> item (targetTelesthsDropDown)
  var targetTelesthsDropDown = null;
  for (var i=0; i < PedioDropDowns.length; i++) {
    if (PedioDropDowns[i] == targetPedioDropDown) {
      targetTelesthsDropDown = TelesthsDropDowns[i];
    }
  }
  // check if targetTelesthsDropDown has already the 2 date operators
  var targetTelesthsDropDownHasAlreadyDateOperators = false;
  var targetTelesthsDropDownOptions = targetTelesthsDropDown.options;
  //alert(targetTelesthsDropDownOptions.length);
  for (var i=0; i < targetTelesthsDropDownOptions.length; i++) {
    if (targetTelesthsDropDownOptions[i].value == '>=' || targetTelesthsDropDownOptions[i].value == '<=') {
      targetTelesthsDropDownHasAlreadyDateOperators = true;
    }
  }  

  if (targetPedioDropDownIsDate == true && targetTelesthsDropDownHasAlreadyDateOperators == false) { // => add 2 date operators
    var optionDateFrom = new Option(translate(35), '>='); // text - value
    var optionDateTo = new Option(translate(36), '<='); // text - value
    //var optionDateFrom = new Option('Από', '>='); // text - value
    //var optionDateTo = new Option('Έως', '<='); // text - value
    targetTelesthsDropDown.options[targetTelesthsDropDownOptions.length] = optionDateFrom;
    targetTelesthsDropDown.options[targetTelesthsDropDownOptions.length] = optionDateTo;
  }
  if (targetPedioDropDownIsDate == false && targetTelesthsDropDownHasAlreadyDateOperators == true) { // => remove 2 date operators
    targetTelesthsDropDownOptions = targetTelesthsDropDown.options;
    for (var i=0; i < targetTelesthsDropDownOptions.length; i++) {
      if (targetTelesthsDropDownOptions[i].value == '>=') {
        targetTelesthsDropDown.options[i] = null;
      }
    }    
    targetTelesthsDropDownOptions = targetTelesthsDropDown.options;
    for (var i=0; i < targetTelesthsDropDownOptions.length; i++) {
      if (targetTelesthsDropDownOptions[i].value == '<=') {
        targetTelesthsDropDown.options[i] = null;
      }
    }        
  }  

}

/*-----------------------------------------------------
                  OperatorSearchFieldOnClick()
-------------------------------------------------------
  INPUT: - targetOperatorDropDown: the <select> Operator DropDown item changed in Search Terms card
  FUNCTION: calls InputSearchFieldOnChange() with parameter the corresponding to targetOperatorDropDown 'input_term' <select> item
-------------------------------------------------------*/      
function OperatorSearchFieldOnClick(targetOperatorDropDown) {
  // get all existing PedioDropDowns and TelesthsDropDowns
  var PedioDropDowns = document.getElementsByName("input_term");
  var TelesthsDropDowns = document.getElementsByName("op_term");
  // get the corresponding to targetOperatorDropDown 'input_term' <select> item (targetPedioDropDown)
  var targetPedioDropDown = null;
  for (var i=0; i < TelesthsDropDowns.length; i++) {
    if (TelesthsDropDowns[i] == targetOperatorDropDown) {
      targetPedioDropDown = PedioDropDowns[i];
    }
  }
  InputSearchFieldOnChange(targetPedioDropDown);
}

/*-----------------------------------------------------
                  removeRow()
-------------------------------------------------------*/      
function removeRow(table) {
    len = table.rows.length;
    if(len > 2) {
        table.deleteRow(len-1);
    }
}

/*-----------------------------------------------------
                  addOutput2()
-------------------------------------------------------*/      
function addOutput2(tableBodyId, rowId) {
    var tbody = getObj(tableBodyId);
    var row = getObj(rowId);
    var newRow = row.cloneNode(true);
    tbody.appendChild(newRow);
}

/*-----------------------------------------------------
                  addOutput()
-------------------------------------------------------*/      
function addOutput(tableBodyId, rowId, targetInput) {
    var tbody = getObj(tableBodyId);
    var row = getObj(rowId);
    var newRow = row.cloneNode(true);
    tbody.appendChild(newRow);
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
        /*}
        else if(targetInput == 'inputvalue_facet'){
            
        }
        else if(targetInput == 'inputvalue_hierarchy'){
                
        }*/
        
      // check if the last added-cloned PedioDropDown contains 2 date operators (>= and <=) => remove them
      var PedioDropDowns = document.getElementsByName("input_term");
      if(this.InputSearchFieldChanged!=null){
          InputSearchFieldChanged(PedioDropDowns[PedioDropDowns.length-1]);
      }
    }
    // BUG fixed by karam (21/1/2008) - END
}

/*-----------------------------------------------------
                  onChangeField()
-------------------------------------------------------*/      
function onChangeField(sel, id, oper) {
    // for (ii=0; ii<sel.parentNode.parentNode.cells[2].childNodes.length; ii++)
    //	sel.parentNode.parentNode.cells[2].childNodes[ii].style.display = 'none';	
    // sel.parentNode.parentNode.cells[2].childNodes[id+1].style.display = 'block';
    oper.selectedIndex = id;
}

function selectAllOutputs(tableId){
    var targTable = document.getElementById(tableId);
    var inputs = targTable.getElementsByTagName('input');
    for (var i=0; i< inputs.length; i++) {
        if(inputs[i].type == "checkbox"){
            inputs[i].checked = true;
        }
	
    }
}
function deselectAllOutputs(tableId){
    var targTable = document.getElementById(tableId);
    var inputs = targTable.getElementsByTagName('input');
    for (var i=0; i< inputs.length; i++) {
        if(inputs[i].type == "checkbox"){
            inputs[i].checked = false;
        }
	
    }

}
function uncheckSelectAllOrNone(checkedVal, suffix){
    if(checkedVal){
        document.getElementById('nooutputs'+suffix).checked=false;
    } else {
        document.getElementById('alloutputs'+suffix).checked=false;
    }
}

function showallpressed(moderator){
    if(moderator=='terms'){
       moderator=''; 
    }
    else{
        moderator = '_' + moderator;
    }

    var targetTable = document.getElementById('criteria'+moderator);
    addClass(targetTable,'disabled');
    
    var inputs = targetTable.getElementsByTagName('input');
    for (var i=0; i< inputs.length; i++) {
        inputs[i].setAttribute("disabled","disabled");        
    }
    var selects = targetTable.getElementsByTagName('select'); 
    for (var i=0; i< selects.length; i++) {
        selects[i].setAttribute("disabled","disabled");        
    }
    
    document.getElementById('criteria'+moderator+'_and_or').style.visibility='hidden'; 
    document.getElementById('criteria'+moderator+'_add_remove').style.visibility='hidden';


}

function addClass(element, value) {
    if(!element.className) {
        element.className = value;
    } else {
        var newClassName = element.className;
        newClassName+= " ";
        newClassName+= value;
        element.className = newClassName;
    }
}

function removeClass(element, value) { //not so safe as it will remove both value and valuewhatever
    if(!element.className) {
        
         
    } else {
        var newClassName = element.className;
        if(newClassName.indexOf(" " + value)>=0){
            element.className = newClassName.replace(" "+value, "");
        }
        else if(newClassName.indexOf(value)>=0){
            element.className = newClassName.replace(value, "");
        }
        
    }
}

function showallreleased(moderator){
    if(moderator=='terms'){
       moderator=''; 
    }
    else{
        moderator = '_' + moderator;
    }

    var targetTable = document.getElementById('criteria'+moderator);
    removeClass(targetTable,'disabled');
    
    var inputs = targetTable.getElementsByTagName('input');
    for (var i=0; i< inputs.length; i++) {
        inputs[i].removeAttribute("disabled");        
    }
    var selects = targetTable.getElementsByTagName('select'); 
    for (var i=0; i< selects.length; i++) {
        selects[i].removeAttribute("disabled");        
    }
    
    document.getElementById('criteria'+moderator+'_and_or').style.visibility='visible'; 
    document.getElementById('criteria'+moderator+'_add_remove').style.visibility='visible';
}