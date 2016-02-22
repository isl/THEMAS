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
                    DBadminDisplayDIV()
-------------------------------------------------------
  INPUT: - divID: the id of the TAB to be displayed
                  Create_DB_backup_DIV / Restore_DB_backup_DIV / Fix_DB_DIV 
                  (DIV ids defined in DBadmin_contents.xsl)
  CALLED BY:  
-------------------------------------------------------*/           
/*
 *function DBadminDisplayDIV(divID) {	
  document.getElementById('Create_Restore_DB_backup_DIV').style.visibility = 'hidden';
  document.getElementById('Fix_DB_DIV').style.visibility = 'hidden';
  document.getElementById(divID).style.visibility = 'visible';
  
  var LinkId = divID.replace('_DIV','_LINK');
  document.getElementById('Create_Restore_DB_backup_LINK').className ="inactive";
  document.getElementById('Fix_DB_LINK').className ="inactive";
  document.getElementById(LinkId).className ="active";
}
*/
/*-----------------------------------------------------
              Create_DB_backupOKButtonPressed()
-------------------------------------------------------*/      
function Create_DB_backupOKButtonPressed() {
  // replace special characters of Create_DB_backup_DescriptionInput
  var Create_DB_backup_DescriptionInput = document.getElementById('Create_DB_backup_Description_ID');
  var Create_DB_backup_Description = Create_DB_backup_DescriptionInput.value;

  Create_DB_backup_Description = StringReplaceSpecialCharacters(Create_DB_backup_Description);
  Create_DB_backup_DescriptionInput.value = Create_DB_backup_Description;


  if (confirm(translate(38))) {
  //if (confirm('Είστε σίγουρος για την δημιουργία αντιγράφου ασφαλείας;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')) {
      DisplayPleaseWaitScreen(true);
      submitFormTo('Create_DB_backupForm' , 'CreateDBbackup');  
  }
  else{
      submitFormTo('Create_DB_backupForm' , 'DBadmin?DIV=Create_Restore_DB_backup_DIV');  
  }
}

/*-----------------------------------------------------
              Restore_DB_backupDeleteButtonPressed()
-------------------------------------------------------*/      
function Restore_DB_backupDeleteButtonPressed() {
  
  if (confirm(translate(39))) {
  //if (confirm('Είστε σίγουρος για τη διαγραφή του αντιγράφου ασφαλείας;')) {
    DisplayPleaseWaitScreen(true);
    submitFormTo('Restore_DB_backupForm' , 'RestoreDBbackup?action=DELETE');  
  }
  else { 
    // ATTENTION!!!! in case of No/Cancel selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Restore_DB_backupForm' , 'DBadmin?DIV=Create_Restore_DB_backup_DIV');  
  }
}

/*-----------------------------------------------------
              Restore_DB_backupRestoreButtonPressed()
-------------------------------------------------------*/      
function Restore_DB_backupRestoreButtonPressed() {
  
  var DB_backupsList = document.getElementById('DB_backupsListID');
  var DB_backupsListValue = DB_backupsList.value;
  // get the selected backup file
  if (confirm(translate(40) + DB_backupsListValue + translate(41))) {
    //if (confirm('Είστε σίγουρος για την επαναφορά αντιγράφου ασφαλείας:' + DB_backupsListValue + ' και επανεγγραφή της υπάρχουσας βάσης δεδομένων;')) {
    DisplayPleaseWaitScreen(true);
    document.getElementById('Restore_DB_backup_result_textarea_ID').value = translate(42);
    //document.getElementById('Restore_DB_backup_result_textarea_ID').value = 'Παρακαλώ περιμένετε...';
    submitFormTo('Restore_DB_backupForm' , 'RestoreDBbackup?action=RESTORE');  
  }
  else { 
    // ATTENTION!!!! in case of No/Cancel selected, set the form action to the following
    // otherwise, the last form's action is executed!!
    submitFormTo('Restore_DB_backupForm' , 'DBadmin?DIV=Create_Restore_DB_backup_DIV');  
  }
}

/*-----------------------------------------------------
              Fix_DBOKButtonPressed()
-------------------------------------------------------*/      
function Fix_DBOKButtonPressed() {
  
  if(confirm(translate(43))){
     //if(confirm('Είστε σίγουρος για την επιδιόρθωση βάσης.\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.')){
     DisplayPleaseWaitScreen(true); 
     document.getElementById('Fix_DB_result_textarea_ID').value = translate(42);
     //document.getElementById('Fix_DB_result_textarea_ID').value = 'Παρακαλώ περιμένετε...';
     submitFormTo('Fix_DBForm' , 'FixDB');  
  }
  else{
      submitFormTo('Fix_DBForm' , 'DBadmin?DIV=Fix_DB_DIV');
  }
}


/*-----------------------------------------------------
              StringReplaceSpecialCharacters()
-------------------------------------------------------*/      
function StringReplaceSpecialCharacters(str) {
  str = str.replace(/</g, '&lt;');
  str = str.replace(/>/g, '&gt;');
  str = str.replace(/&/g, '&amp;');
  
  
  return str;
}

/*-----------------------------------------------------
              StringContainsSpecialCharacters()
-------------------------------------------------------*/      
function StringContainsSpecialCharacters(str) {
  var iChars = "*|,\":<>[]{}`\';()@&$#%";
  for (var i = 0; i < str.length; i++) {
    if (iChars.indexOf(str.charAt(i)) != -1) {
      return true;
    }
  }
  
  return false;
}
    

