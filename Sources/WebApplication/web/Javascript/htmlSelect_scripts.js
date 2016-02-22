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

/* used in order to copy the selected option from an html select field (sourceSel) to 
 * another html select field (destSel). Finally the selected option (if any) is deleted 
 * from sourceSel using remove option function.
 */
function copyOption(sourceSel_ID, destSel_ID)
{
    var sourceSel = document.getElementById(sourceSel_ID);
    var destSel = document.getElementById(destSel_ID);
    if(sourceSel.selectedIndex < 0)
        return;
    if(sourceSel.options[sourceSel.selectedIndex].disabled == true)
        return;

    if (destSel.length == 0) {
        
        /*Create First Option To Destination select Element*/
        var newOpt1 = new Option(sourceSel.options[sourceSel.selectedIndex].value, sourceSel.options[sourceSel.selectedIndex].value);
        destSel.options[0] = newOpt1;
        destSel.selectedIndex = 0;
        
        /*remove option from source select element*/
        sourceSel.options[sourceSel.selectedIndex] = null;
        
        if (sourceSel.length > 0) {
            sourceSel.selectedIndex == 0 ? sourceSel.selectedIndex = 0 : sourceSel.selectedIndex = sourceSel.selectedIndex - 1;
        }
               
    }
    else{
        
        /*Create First Option To Destination select Element*/
        var newOpt2 = new Option(sourceSel.options[sourceSel.selectedIndex].value, sourceSel.options[sourceSel.selectedIndex].value);
        destSel.options[destSel.length] = newOpt2;
        destSel.selectedIndex = 0;
        
        sourceSel.options[sourceSel.selectedIndex] = null;
        
        if (sourceSel.length > 0) {
            sourceSel.selectedIndex == 0 ? sourceSel.selectedIndex = 0 : sourceSel.selectedIndex = sourceSel.selectedIndex - 1;
        }
    } 
    
    if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
        document.getElementById('DisplayCardArea').style.visibility = 'hidden';
        document.getElementById('DisplayCardArea').style.visibility = 'visible';
    }

}


/* 
 * This Function is used in order to remove an option (opt) from an html select list (sourceSel).
 * Since options texts are all numbered a visual reordering must be performed.
 */
function removeOption(sourceSel, opt){
    
    if (opt != -1) {
        sourceSel.options[opt] = null;
        
        if (sourceSel.length > 0) {
            sourceSel.selectedIndex = opt == 0 ? 0 : opt - 1;
        }
    }
    // * Commented Because it does not work well with deisabled options
    //sortlist(sourceSel);
}

/* 
 * Alphabetical Sort of select elements' (whichselect) options
 * Commented Because it does not work well with deisabled options
 
function sortlist(whichselect) {
    
    var arrTexts = new Array();

    for(i=0; i<whichselect.length; i++)  {
        arrTexts[i] = whichselect.options[i].text;
    }

    arrTexts.sort();

    for(i=0; i<whichselect.length; i++)  {
        whichselect.options[i].text = arrTexts[i];
        whichselect.options[i].value = arrTexts[i];
    }
}
*/
    

function readSelectFields(fromID, toInputName){

    //var targetSelectList = document.getElementById('edit_Sel_Letter_Codes_HierID');
    var targetSelectList = document.getElementById(fromID);
    var targetText = '';
    var i=0;
    
    if(targetSelectList.length > 0){
        
       targetText = '';

        for(i=0; i< targetSelectList.length ; i++ ){
            targetText += targetSelectList.options[i].value;

            if(i != targetSelectList.length -1 ){
                targetText += ',';
            }

        }
        //document.getElementsByName('letterCodesHierarchyInput')[0].value = targetText;
        document.getElementsByName(toInputName)[0].value = targetText;
    }
        
    /*
    targetSelectList = document.getElementById('edit_Sel_Hier_FacetID');
    
    if(targetSelectList.length > 0){
        
        targetText = '';

        for( i=0; i< targetSelectList.length ; i++ ){
            targetText += targetSelectList.options[i].value;

            if(i != targetSelectList.length -1 ){
                targetText += ',';
            }

        }
        document.getElementsByName('selectedFacetsHierarchyInput')[0].value = targetText;
    }    
    */
   


} 
var indexOfUTF8Strings=0;
var a_after_b = 1;
 var a_equal_to_b = 0;
 var a_before_b = -1;
 
var charArrays = new Array("*","A","a","B","b","C","c","D","d","E","e","F","f","G","g","H","h","I","i","J","j","K","k","L","l","M","m","N","n","O","o","P","p","Q","q","R","r","S","s","T","t","U","u","V","v","W","w","X","x","Y","y","Z","z",
                           "Α","Ά","α","ά","Β","β","Γ","γ","Δ","δ","Ε","Έ","ε","έ","Ζ","ζ","Η","Ή","η","ή","Θ","Θ","Ι","Ί","Ϊ","ι","ί","ϊ","Κ","κ",
                           "Λ","λ","Μ","μ","Ν","ν","Ξ","ξ","Ο","Ό","ο","ό","Π","π","Ρ","ρ","Σ","σ","Τ","τ","Υ","Ύ","Ϋ","υ","ύ","ϋ","Φ","φ","Χ","χ","Ψ","ψ","Ω","Ώ","ω","ώ");

                    
var charPriorities = new Array(0,10,10,20,20,30,30,40,40,50,50,60,60,70,70,80,80,90,90,100,100,110,110,120,120,130,130,140,140,150,150,160,160,170,170,180,180,190,190,200,200,210,210,220,220,230,230,240,240,250,250,260,270,
                           300,300,300,300,310,310,320,320,330,330,340,340,340,340,350,350,360,360,360,360,370,370,380,380,380,380,380,380,390,390,
                           400,400,410,410,420,420,430,430,440,440,440,440,450,450,460,460,470,470,480,480,490,490,490,490,490,490,500,500,510,510,520,520,530,530,530,530);



   function removeSelectedOption(targetSelectField_Id,obligatory){
       
       var targetSelectField = document.getElementById(targetSelectField_Id);
       if(targetSelectField.selectedIndex <0){
           refreshCreateNew();
           alert(translate(32));
           //alert('Πρέπει πρώτα να επιλέξετε μία τιμή για αφαίρεση.');
           refreshCreateNew();
           return;
       }
       
       if(obligatory == 'true' && targetSelectField.length ==1){
           refreshCreateNew();
           alert(translate(33));
           //alert('Πρέπει να διατηρείσετε τουλάχιστον\nμία τιμή για αυτό το πεδίο.')
           refreshCreateNew();
           return;
       }
           
       targetSelectField.options[targetSelectField.selectedIndex] = null;
        
        if (targetSelectField.length > 0) {
            targetSelectField.selectedIndex == 0 ? targetSelectField.selectedIndex = 0 : targetSelectField.selectedIndex = targetSelectField.selectedIndex - 1;
        }
        if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
            document.getElementById('DisplayCardArea').style.visibility = 'hidden';
            document.getElementById('DisplayCardArea').style.visibility = 'visible';
        }
        
       
   }
   
   function addOption(sourceSelectFieldId, targetSelectFieldId){
       
      var destSel = document.getElementById(targetSelectFieldId);
      var sourceSel = document.getElementById(sourceSelectFieldId);
       
       if(sourceSel.selectedIndex < 0)
           return;
       
       // if(sourceSel.options[sourceSel.selectedIndex].disabled == true)
       //     return;
       
       if (destSel.length == 0) {
           
           //Create First Option To Destination select Element
           var newOpt1 = new Option(sourceSel.options[sourceSel.selectedIndex].value, sourceSel.options[sourceSel.selectedIndex].value);
           destSel.options[0] = newOpt1;
           destSel.selectedIndex = 0;
           
           
           
       }
       else{
           var i=0;
           var newOpt2 = new Option(sourceSel.options[sourceSel.selectedIndex].value, sourceSel.options[sourceSel.selectedIndex].value);
           
           for(i=0; i< destSel.length; i++){
               
               if(destSel.options[i].value == sourceSel.options[sourceSel.selectedIndex].value ){
                   refreshCreateNew();
                   alert(translate(34));
                   //alert('Η τιμή αυτή έχει ήδη επιλεχθεί.');
                   refreshCreateNew();
                   return;
               }
           }
           //Create First Option To Destination select Element
           destSel.options[destSel.length] = newOpt2;
           destSel.selectedIndex = 0;
           
           
       } 
       if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
            document.getElementById('DisplayCardArea').style.visibility = 'hidden';
            document.getElementById('DisplayCardArea').style.visibility = 'visible';
        }
    // sortlist(destSel);
   }

   function addNewTranslationValue(destSel_id,langId,seperator, inputID){
       var targetLanguageIdInput = document.getElementById(langId);
       var seperatorInput = document.getElementById(seperator);
       var targetInput = document.getElementById(inputID);
       var destSel = document.getElementById(destSel_id);

       var lanfuageIdentifier =targetLanguageIdInput.options[targetLanguageIdInput.selectedIndex].value;

       
       
       if(trim(targetInput.value).length>0){

           var newVal = lanfuageIdentifier +seperatorInput.value+ trim(targetInput.value);
           if (destSel.length == 0) {
               //Create First Option To Destination select Element
               var newOpt1 = new Option(newVal, newVal);
               destSel.options[0] = newOpt1;
               destSel.selectedIndex = 0;
           }
           else{
               var i=0;
               var newOpt2 = new Option(newVal, newVal);

               for(i=0; i< destSel.length; i++){

                   if(destSel.options[i].value == newVal){
                       alert(translate(34));                       
                       return;
                   }
               }
               //Create First Option To Destination select Element
               destSel.options[destSel.length] = newOpt2;
               destSel.selectedIndex = destSel.length-1;

           }
       }
       else{
           alert(translate(65));
       }
       
       targetInput.value='';
       if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
            document.getElementById('DisplayCardArea').style.visibility = 'hidden';
            document.getElementById('DisplayCardArea').style.visibility = 'visible';
        }
   }
   
   function addNewValue(destSel_id, inputID){
       var targetInput = document.getElementById(inputID);
       var destSel = document.getElementById(destSel_id);
       
       if (destSel.length == 0) {
           
           //Create First Option To Destination select Element
           var newOpt1 = new Option(targetInput.value, targetInput.value);
           destSel.options[0] = newOpt1;
           destSel.selectedIndex = 0;
           
           
           
       }
       else{
           var i=0;
           var newOpt2 = new Option(targetInput.value, targetInput.value);
           
           for(i=0; i< destSel.length; i++){
               
               if(destSel.options[i].value == targetInput.value){
                   alert(translate(34));
                   //alert('Η τιμή αυτή έχει ήδη επιλεχθεί.')
                   return;
               }
           }
           //Create First Option To Destination select Element
           destSel.options[destSel.length] = newOpt2;
           destSel.selectedIndex = 0;
                      
       } 
       targetInput.value='';
       if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
            document.getElementById('DisplayCardArea').style.visibility = 'hidden';
            document.getElementById('DisplayCardArea').style.visibility = 'visible';
        }
   }
   
   
   function refreshCreateNew(){
   // if(document.getElementById('DisplayCardArea').style.visibility == 'visible'){
            document.getElementById('DisplayCardArea').style.visibility = 'hidden';
            document.getElementById('DisplayCardArea').style.visibility = 'visible';
   //     }
   }
   
   

 
 function greekCompare(a,b){
 
     //a is shorter than b - >a remains first
     if(indexOfUTF8Strings==a.length){
         if(a.length<b.length){
             return a_before_b;
         }
         else
         if(a.length==b.length){
             return a_equal_to_b;
         }         
         else{
             return a_after_b;
         }             
     } 
     
     //a is shorter than b - >a sould be first first
     if(indexOfUTF8Strings==b.length){
         if(b.length<a.length){
             return a_after_b;
         }
         else
         if(b.length==a.length){
             return a_equal_to_b;
         }         
         else{
             return a_before_b;
         }             
     }
     
     
     var aComp = getLetterCode(a.charAt(indexOfUTF8Strings));
     var bComp = getLetterCode(b.charAt(indexOfUTF8Strings));
     if(aComp == bComp){
         indexOfUTF8Strings++;
         return greekCompare(a,b);
     }
     else{
         if(aComp>bComp){
             return a_after_b;
         }
         else{
             return a_before_b;
         }
     
     }
      
 }
function getLetterCode(GreekChar){

    var index = -1; 
    var i;
    for(i=0;i<charArrays.length;i++){
        if(charArrays[i] == GreekChar ){
            index=i;
            break;
        }
    }
  
    if(index>=0)
        return charPriorities[index];
    else
        return 0;
}
function sortlist(destSel) {
      
       var arrTexts = new Array();
       var i=0;
       for(i=0; i<destSel.length; i++)  {
           arrTexts[i] = destSel.options[i].value;
       }
       indexOfUTF8Strings=0;
       arrTexts.sort(greekCompare);
       
       for(i=0; i<destSel.length; i++)  {
           destSel.options[i].text = arrTexts[i];
           destSel.options[i].value = arrTexts[i];
       }
   }
