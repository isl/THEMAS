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
                    AddThesaurusGroupInput()
  -----------------------------------------------------
  CALLED: any time + icon is pressed ("Προσθήκη") in Create new User card
  -------------------------------------------------------*/      
function AddThesaurusGroupInput(tableBodyId, rowId) {
  var thesArray  = document.getElementsByName('selectThesaurus');
  var groupsArray  = document.getElementsByName('selectUserGroup');
  var MinusIconArray  = document.getElementsByName('MinusIcon');
  
    var tbody = getObj(tableBodyId);
    /*
    var table = tbody.parentNode;
    var row = getObj(rowId);
    // get the number of rows of the table (-1 because of header)
    len = table.rows.length - 1;
    //alert('Trying to add a row to table with ' + len + ' rows')
    if (len == 1 && thesArray[0].style.visibility == 'hidden') {
      row.style.visibility = 'visible';
      thesArray[0].style.visibility = 'visible';
      groupsArray[0].style.visibility = 'visible';
      MinusIconArray[0].style.visibility = 'visible';            
      //alert(row.name);
    }
    else {
      var newRow = row.cloneNode(true);
      tbody.appendChild(newRow);      
    }
    */
    
    
    var table = tbody.parentNode;
    var row = getObj(rowId);
    // get the number of rows of the table (-1 because of header)
    len = table.rows.length - 1;
    //alert('Trying to add a row to table with ' + len + ' rows')
    /*if (len == 1 && row.style.visibility == 'hidden') {
      row.style.visibility = 'visible';
      usersArray[0].style.visibility = 'visible';
      groupsArray[0].style.visibility = 'visible';
      MinusIconArray[0].style.visibility = 'visible';            
    }
    else {*/
    //first row will always be collapsed 
    var newRow = row.cloneNode(true);
    newRow.style.visibility = 'visible';
    var selects = newRow.getElementsByTagName('select');
    //enable the disabled selects
    for ( i=0; i< selects.length; i++) {
        selects[i].disabled = false;
    }
    tbody.appendChild(newRow);      
    //} 
}

/*-----------------------------------------------------
                    RemoveThesaurusGroupInput()
  -----------------------------------------------------
  CALLED: any time - icon is pressed in Create new User card
  -------------------------------------------------------*/      
function RemoveThesaurusGroupInput(tableId, rowObj) {
  var thesArray  = document.getElementsByName('selectThesaurus');
  var groupsArray  = document.getElementsByName('selectUserGroup');
  var MinusIconArray  = document.getElementsByName('MinusIcon');
  
  var table = getObj(tableId);
    
    // get the number of rows of the table (-1 because of header)
    len = table.rows.length - 1;
    //alert('Trying to delete a row from table with ' + len + ' rows')
    if (len > 1) {
      rowObj.parentNode.removeChild(rowObj);
    }
    else {
      rowObj.style.visibility = 'hidden';
      thesArray[0].style.visibility = 'hidden';
      groupsArray[0].style.visibility = 'hidden';
      MinusIconArray[0].style.visibility = 'hidden';      
    }    
}

/**
 * CALLED: any time + icon is pressed ("Add") in Share Thesaurus card
 * @param {type} tableBodyId
 * @param {type} rowId
 * @returns {undefined}
 */
function AddUserGroupInput(tableBodyId, rowId) {
  var usersArray  = document.getElementsByName('selectUser');
  var groupsArray  = document.getElementsByName('selectUserGroup');
  var MinusIconArray  = document.getElementsByName('MinusIcon');
  
    var tbody = getObj(tableBodyId);
    var table = tbody.parentNode;
    var row = getObj(rowId);
    // get the number of rows of the table (-1 because of header)
    len = table.rows.length - 1;
    //alert('Trying to add a row to table with ' + len + ' rows')
    /*if (len == 1 && row.style.visibility == 'hidden') {
      row.style.visibility = 'visible';
      usersArray[0].style.visibility = 'visible';
      groupsArray[0].style.visibility = 'visible';
      MinusIconArray[0].style.visibility = 'visible';            
    }
    else {*/
    //first row will always be collapsed 
    var newRow = row.cloneNode(true);
    newRow.style.visibility = 'visible';
    var selects = newRow.getElementsByTagName('select');
    //enable the disabled selects
    for ( i=0; i< selects.length; i++) {
        selects[i].disabled = false;
    }
    tbody.appendChild(newRow);      
    //}    
}

/**
 * CALLED: any time - icon is pressed in Share Thesaurus card
 * @param {type} tableId
 * @param {type} rowObj
 * @returns {undefined}
 */
function RemoveUserGroupInput(tableId, rowObj) {
    
  var usersArray  = document.getElementsByName('selectUser');
  var groupsArray  = document.getElementsByName('selectUserGroup');
  var MinusIconArray  = document.getElementsByName('MinusIcon');
  
    var table = getObj(tableId);
    
    // get the number of rows of the table (-1 because of header)
    len = table.rows.length - 1;
    //alert('Trying to delete a row from table with ' + len + ' rows')
    if (len > 1) {
      rowObj.parentNode.removeChild(rowObj);
    }
    else {
      rowObj.style.visibility = 'hidden';
      usersArray[0].style.visibility = 'hidden';
      groupsArray[0].style.visibility = 'hidden';
      MinusIconArray[0].style.visibility = 'hidden';      
    }
}

/*-----------------------------------------------------
                    administratorCheckBoxClick() 
  -------------------------------------------------------*/      
function administratorCheckBoxClick(adminCheckBox) {
  /*var thesArray  = document.getElementsByName('selectThesaurus');
  var groupsArray  = document.getElementsByName('selectUserGroup');
  var MinusIconArray  = document.getElementsByName('MinusIcon');*/
    
  var userRolesTable = document.getElementById('thesaurusGroupTable');
  var roleAdditionTable = document.getElementById('TableWithPlusIconId');
  var editUserRolesTableRow = document.getElementById('rolesDefinitionRow');
  
  if(adminCheckBox.checked) {
    /*for(var i=0; i < thesArray.length; i++){
      thesArray[i].style.visibility = 'hidden';
      groupsArray[i].style.visibility = 'hidden';
      MinusIconArray[i].style.visibility = 'hidden';
    }    */  
    if(userRolesTable){userRolesTable.style.display = 'none';}
    if(roleAdditionTable){roleAdditionTable.style.display = 'none';}
    if(editUserRolesTableRow){editUserRolesTableRow.style.display = 'none';}
  }
  else {
      /*
    for(var i=0; i < thesArray.length; i++){
      thesArray[i].style.visibility = 'visible';
      groupsArray[i].style.visibility = 'visible';
      MinusIconArray[i].style.visibility = 'visible';
    }   */     
        
    if(userRolesTable){userRolesTable.style.display = 'table';}
    if(roleAdditionTable){roleAdditionTable.style.display = 'table';}
    if(editUserRolesTableRow){editUserRolesTableRow.style.display = 'table-row';}    
  }
}

/*-----------------------------------------------------
                    deleteUserCheckBoxClick() 
  -------------------------------------------------------*/      
function deleteUserCheckBoxClick(deleteUserCheckBox_id) {
    var usernameElement = document.getElementById('newUserName_Id');
    var userDescriptionElement = document.getElementById('newUserDescription_Id');
    var deletePasswordCheckBoxElement=document.getElementById('deletePasswordCheckBoxId');
  if(document.getElementById(deleteUserCheckBox_id).checked) {
    usernameElement.disabled = "disabled";
    userDescriptionElement.disabled = "disabled";
    deletePasswordCheckBoxElement.disabled = "disabled";
    
    var attr1 = document.createAttribute ("class");
    attr1.value = "disabledbutton";
    usernameElement.setAttributeNode (attr1);
    
    var attr2 = document.createAttribute ("class");
    attr2.value = "disabledbutton";
    userDescriptionElement.setAttributeNode (attr2);
            
    var attr3 = document.createAttribute ("class");
    attr3.value = "disabledbutton";
    deletePasswordCheckBoxElement.setAttributeNode (attr3);
  }
  else {
      
    usernameElement.disabled = "";
    usernameElement.removeAttribute("class");
    
    userDescriptionElement.disabled = "";
    userDescriptionElement.removeAttribute("class");
    
    deletePasswordCheckBoxElement.disabled = "";
    deletePasswordCheckBoxElement.removeAttribute("class");
    
  }
}

/*-----------------------------------------------------
                    Edit_User_Save_Button_Pressed() 
  -------------------------------------------------------*/      
function Edit_User_Save_Button_Pressed(servletName ,targetFieldSetID , resultArea, selectsSelectionMode) {
  var deleteUserCheckBox = document.getElementById('deleteUserCheckBoxId');
  if (deleteUserCheckBox != null) {
    if (deleteUserCheckBox.checked && confirm(translate(37)) == false) {
    //if (deleteUserCheckBox.checked && confirm('Είστε σίγουρος για τη διαγραφή του χρήστη;') == false) {    
      return;
    }
  }
  getServletResult( 'EditDisplays_User','edit_user_create', '','selectedIndexOnly');
}


