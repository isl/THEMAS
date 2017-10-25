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
var translations = new Array();
var howmanytransaltions = 67;
for(var traverseindex =0 ; traverseindex <howmanytransaltions; traverseindex++){
    translations[traverseindex]=new Array();
}
//--------------------------------------------------------------
//file scripts.js function callSystemConfigurationsServlet
//--------------------------------------------------------------
translations[0][0]='The number of results per page must be a a positive integer.'; 
translations[0][1]='Το πλήθος αποτελεσμάτων ανά σελίδα πρέπει να είναι θετικός ακέραιος αριθμός.';
translations[1][0]='Automatic backups start time must be declared according to the format HH::MM::SS where\nHH: Hour (integer ranging from 0 to 23)\nMM: Minutes (integer ranging from 0 to 59)\nSS: Seconds (integer ranging from 0 to 59)';
translations[1][1]='Η ώρα έναρξης των αυτομάτων αντιγράφων ασφαλείας πρέπει να δίνεται στην μορφή: ΩΩ:::ΛΛ:::ΔΔ όπου\nΩΩ: Ωρα (ακέραιος αριθμός από 0 έως 23)\nΛΛ: Λεπτά (ακέραιος αριθμός από 0 έως 59)\nΔΔ: Δευτερόλεπτα (ακέραιος αριθμός από 0 έως 59)';
translations[2][0]='Automatic backups interval period must be declared as positive integer.'
translations[2][1]='Η περιοδικότητα των αυτομάτων αντιγράφων ασφαλειας πρέπει να είναι θετικός ακέραιος αριθμός.';
translations[3][0]='E-mail address: \'';
translations[3][1]='Η e-mail διεύθυνση: \'';
translations[4][0]='\' is not valid.';
translations[4][1]='\' δεν είναι έγκυρη.';
translations[5][0]='The e-mail notification server declared is not valid.';
translations[5][1]='O e-mail server που δηλώθηκε δεν είναι έγκυρος.';
translations[6][0]='E-mail notification addresses list is not declared.';
translations[6][1]='Δεν έχει δηλωθεί λίστα email διευθύνσεων.';
translations[7][0]='E-mail notification server is not declared.';
translations[7][1]='Δεν έχει δηλωθεί mail server.';
translations[8][0]= 'System configuration changes,\nrequire tomcat restart.\n\nIf you can not manually restart tomcat\n afterwards DO NOT procced.\n\nProceed?';
translations[8][1]='Η αλλαγή των ρυθμίσεων του συστήματος,\nαπαιτεί μη αυτόματη επανεκκίνηση του tomcat.\n\nΑν δεν μπορείτε να επανεκινήσετε χειροκίνητα\n τον tomcat μετά ΜΗΝ προχωρήσετε\n\nΘέλετε να προχωρήσετε?';
//--------------------------------------------------------------
//file scripts.js function callAdminFixServlet
//--------------------------------------------------------------
translations[9][0]='The name of the selected thesaurus must conatin 1 to 20 characters.';
translations[9][1]='Το όνομα του επιλεγμένου θησαυρού πρέπει να περιέχει από 1 έως και 20 χαρακτήρες.';
translations[10][0]='The name of the selected thesaurus must contain only capital latin characters and numbers.';
translations[10][1]='Το όνομα του επιλεγμένου θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.';
translations[11][0]='Thesaurus ';
translations[11][1]='Ο θησαυρός ';
translations[12][0]=' was not found in database.'
translations[12][1]=' δεν βρέθηκε στην βάση.';
translations[13][0]='Not all data were corrected.\n\nPlease repeat the \nlast correction action.';
translations[13][1]='Δεν διορθώθηκαν όλα τα δεδομένα.\n\nΠαρακαλώ επαναλάβετε την \nτελευταία ενέργεια διόρθωσης.';
//--------------------------------------------------------------
//file scripts.js function checkPageNumber
//--------------------------------------------------------------
translations[14][0]='Wrong page number.';
translations[14][1]='Εσφαλμένος αριθμός σελίδας.';
//--------------------------------------------------------------
//file tabs.js 
//--------------------------------------------------------------
translations[15][0]='The name of a database descriptor must not exceed the limit of ';
translations[15][1]='Το όνομα ενός στοιχείου της βάσης δεδομένων δεν πρέπει να ξεπερνάει τους ';
translations[16][0]=' characters.';
translations[16][1]=' χαρακτήρες.';
translations[17][0]='The text of a note  must not exceed the limit of ';
translations[17][1]='Το κείμενο ενός σχολίου δεν πρέπει να ξεπερνάει τους '
translations[18][0]=' characters.';
translations[18][1]=' χαρακτήρες.';
translations[19][0]='The content of a text value must not exceed the limit of ';
translations[19][1]='Το περιεχόμενο μιας τιμής κειμένου δεν πρέπει να ξεπερνάει τους '
translations[20][0]=' characters.';
translations[20][1]=' χαρακτήρες.';
//--------------------------------------------------------------
//file rename.js 
//--------------------------------------------------------------
translations[21][0]='Please insert a new name.';
translations[21][1]='Παρακαλώ εισάγετε ένα νεό όνομα.';
translations[22][0]='Term is already defined in the database.';
translations[22][1]='Ο όρος υπάρχει στη βάση δεδομένων.';
translations[23][0]='Term ';
translations[23][1]='Ο όρος ';
translations[24][0]=' was not found in the database.';
translations[24][1]=' δεν βρέθηκε στην βαση δεδομένων.';
translations[25][0]='This term is a Top Term and its name may\n only change by renaming its relative hierarchy.';
translations[25][1]='Ο όρος αποτελεί Όρο Κορυφής και το όνομα του μπορεί \nνα αλλάχθει μόνο μέσω μετονομασίας της σχετικής ιεραρχίας.';
translations[26][0]='Error message:\n';
translations[26][1]='Μήνυμα λάθους:\n';
translations[27][0]='\n\nPlease try again.';
translations[27][1]='\n\nΠαρακαλώ προσπαθήστε ξανά.';
translations[28][0]='Error message: the name ';
translations[28][1]='Μήνυμα λάθους: το όνομα ';
translations[29][0]=' has already been inserted in the rename chain. Please try again.';
translations[29][1]=' έχει ήδη εισαχθεί στην αλυσίδα των μετονομασιών. Παρακαλώ προσπαθήστε ξανά.';
//--------------------------------------------------------------
//file moveToHierarchy.js 
//--------------------------------------------------------------
translations[30][0]='Choose a Hierarchy towards which the movement will be performed.';
translations[30][1]='Επιλέξτε μία Ιεραρχία προς την οποία θα γίνει η μετακίνηση';
translations[31][0]='Choose a broader term of the Hierarchy towards which the movement will be performed';
translations[31][1]='Επιλέξτε έναν ευρύτερο όρο της Ιεραρχίας προς την οποία θα γίνει η μετακίνηση';
//--------------------------------------------------------------
//file htmlSelect_scripts.js 
//--------------------------------------------------------------
translations[32][0]='You must first choose a value for removal';
translations[32][1]='Πρέπει πρώτα να επιλέξετε μία τιμή για αφαίρεση.';
translations[33][0]='You must keep at least\none value for this field';
translations[33][1]='Πρέπει να διατηρήσετε τουλάχιστον\nμία τιμή για αυτό το πεδίο.';
translations[34][0]='This value is already chosen.';
translations[34][1]='Η τιμή αυτή έχει ήδη επιλεχθεί.';
//--------------------------------------------------------------
//file criteria.js 
//--------------------------------------------------------------
translations[35][0]='From';
translations[35][1]='Από';
translations[36][0]='To';
translations[36][1]='Έως';
//--------------------------------------------------------------
//file THEMASUsers.js 
//--------------------------------------------------------------
translations[37][0]='Are you sure you want to proceed to deletion of this user?';
translations[37][1]='Είστε σίγουρος για τη διαγραφή του χρήστη;';
//--------------------------------------------------------------
//file DBadmin.js 
//--------------------------------------------------------------
translations[38][0]='Are you sure you want to create a backup?\nThis function will temporarily stop every database communication.'; 
translations[38][1]='Είστε σίγουρος για την δημιουργία αντιγράφου ασφαλείας;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
translations[39][0]='Are you sure you want to delete this backup?';
translations[39][1]='Είστε σίγουρος για τη διαγραφή του αντιγράφου ασφαλείας;';
translations[40][0]='Do you confirm the restoration of backup: ';
translations[40][1]='Είστε σίγουρος για την επαναφορά αντιγράφου ασφαλείας: ';
translations[41][0]=' and the rewriting of the whole existing database;';
translations[41][1]=' και επανεγγραφή της υπάρχουσας βάσης δεδομένων;';
translations[42][0]='Please wait...';
translations[42][1]='Παρακαλώ περιμένετε...';
translations[43][0]='Do you confirm the database fix action.\nThis action will temporarily stop every database communication.';
translations[43][1]='Είστε σίγουρος για την επιδιόρθωση βάσης.\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
//--------------------------------------------------------------
//file Admin_Thesaurus.js 
//--------------------------------------------------------------
translations[44][0]='You have not chosen a .xml input file.';
translations[44][1]='Δεν έχετε επιλέξει .xml αρχείο εισόδου.';
translations[45][0]='Thesaurus name must contain 1 to 10 characters.';
translations[45][1]='Το όνομα του θησαυρού πρέπει να περιέχει από 1 έως και 20 χαρακτήρες.';
translations[46][0]='Thesaurus name must only contain latin capital characters and numbers.';
translations[46][1]='Το όνομα του θησαυρού πρέπει να περιέχει μόνο κεφαλαίους λατινικούς χαρακτήρες και αριθμούς.';
translations[47][0]='Are you sure for the thesaurus insertion to the database?\nThis action will stop temporarily every database communication.';
translations[47][1]='Είστε σίγουρος για την εισαγωγή θησαυρού στην βάση;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
translations[48][0]='You have not specified a hierarchy for insertion.';
translations[48][1]='Δεν έχει προσδιοριστεί ιεραρχία εισαγωγής.';
translations[49][0]='Do you confirm the insertion of terms in hierarchy \'';
translations[49][1]='Είστε σίγουρος για την εισαγωγή όρων στην ιεραρχία \'';
translations[50][0]='\'?\nThis action will temporarily stop every database communication.';
translations[50][1]='\';\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
translations[51][0]='Data insertion method to the thesaurus is not defined.';
translations[51][1]='Δεν έχει προσδιοριστεί μέθοδος εισαγωγής δεδομένων στον θησαυρό.';
translations[52][0]='Do you confirm the thesaurus export action?';
translations[52][1]='Είστε σίγουρος για την εξαγωγή θησαυρού από την βάση;';
translations[53][0]='Not all data were corrected.\n\nPlease repeat the \nlast correction action.';
translations[53][1]='Δεν διορθώθηκαν όλα τα δεδομένα.\n\nΠαρακαλώ επαναλάβετε την \nτελευταία ενέργεια διόρθωσης.';
translations[54][0]='Do you confrim the creation of new thesaurus?\nThis action will temporarily stop every database communication.';
translations[54][1]='Είστε σίγουρος για την δημιουργία νέου θησαυρού;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
translations[55][0]='Do you confirm the deletion of thesaurus ';
translations[55][1]='Είστε σίγουρος για τη διαγραφή του θησαυρού '
translations[56][0]='?';
translations[56][1]=';';
translations[57][0]='This action is time consuming. \rPlease wait...';
translations[57][1]='Η διαδικασία αυτή είναι χρονοβόρα. \rΠαρακαλώ περιμένετε...';
translations[58][0]='Do you confrim the thesaurus copy action?\nThis action will temporarily stop every database communication.';
translations[58][1]='Είστε σίγουρος για την αντιγραφή θησαυρού;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';
translations[59][0]='No thesaurus name was determined for the first thesaurus';
translations[59][1]='Δεν έχει προσδιοριστεί όνομα για τον πρώτο θησαυρό.';
translations[60][0]='No thesaurus name was determined for the second thesaurus';
translations[60][1]='Δεν έχει προσδιοριστεί όνομα για τον δεύτερο θησαυρό.';
translations[61][0]='No thesaurus name was determined for the new merged thesaurus.';
translations[61][1]='Δεν έχει προσδιοριστεί όνομα για τον νέο συγχωνευμένο θησαυρό.';
translations[62][0]='The thesauri that are to be merged must have different names.';
translations[62][1]='Οι δύο θησαυροί προς συγχώνευση πρέπει να έχουν διαφορετικό όνομα.';
translations[63][0]='The new thesaurus must have different name from the two thesauri that is composed of.';
translations[63][1]='Ο νέος θησαυρός πρέπει να έχει διαφορετικό όνομα από τους δύο επιμέρους.';
translations[64][0]='Do you confrim the merge thesauri action?\nThis action will temporarily stop every database communication.';
translations[64][1]='Είστε σίγουρος για την συγχώνευση θησαυρών;\nΗ λειτουργία αυτή θα σταματήσει προσωρινά κάθε επικοινωνία με την βάση.';


translations[65][0]='You have not inserted a value for addition.';
translations[65][1]='Δεν έχει εισαχθεί τιμή για προσθήκη.';

translations[66][0]=' is already defined.';
translations[66][1]=' έχει ήδη ορισθεί.';


function translate(code){
    if(code<howmanytransaltions){
        if(UILanguage=='el'){
            return translations[code][1];
        }
        else{//default behaviour in english bacause most people will undersatnd
            return translations[code][0];
        }
    }
    else{
        alert('NO SUCH TRANSLATION\n\nReport this error to ics.');
        return 'NO SUCH TRANSLATION';
    }   
}