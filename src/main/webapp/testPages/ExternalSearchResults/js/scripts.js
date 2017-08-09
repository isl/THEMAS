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


var thesaurusBaseUrl = 'http://localhost:8084/THEMAS/';
var targetUserName = 'ExternalReader';
var targetThesaurus = 'AATDEMO';

var targetTable = null;

$(function() {
    targetTable = $('#resultsDiv').DataTable( {
        //data: dataSet,
        columns: [
            { title: "Term" },
            { title: "Non Preferred Term (UF)" },
            { title: "Non Preferred Translation Term (UF Tra.)" }
            
        ]
        //, iDisplayLength: -1
        ,bPaginate: false
    } );
});

function searchTHEMAS(){
    //alert($('#SearchThemasParam').val());
    $('#ShowResultsDiv').hide();
     
    var targetTerm = escape(encodeURIComponent($('#SearchThemasParam').val()));
    /*
     
    var dataSet =[[ "45 rpm records "+targetTerm, "seven-inch record " + $('#SearchThemasParam').val(), "NL: 45-toerenplaat"],
        [ "70mm", "seventy millimeter", "-"],
        [ "78 rpm records", "seventy-eights", "-"]];
    
     //$("#div1").html('');

    targetTable.clear();
    targetTable.row.add(dataSet[0]);
    targetTable.row.add(dataSet[1]);
    targetTable.row.add(dataSet[2]);
    targetTable.draw();
*/
    //exact match
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term=' + targetTerm + '&operator_term=and&output_term1=name&input_term=name&op_term=%253D';
    
    //contains in term name only
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term='+targetTerm+'&operator_term=and&output_term1=name&=deSelectAllOutputs&input_term=name&op_term=~&'
    
    //contains in term name only or in UF terms or uf translation terms
    var params = '?updateTermCriteria=parseCriteria' +'&answerType=XMLSTREAM'+'&pageFirstResult=SaveAll'+
                                                  '&input_term=name'+
                                                  '&op_term=~'+
                                                  '&inputvalue_term='+targetTerm+
                                                  '&input_term=uf'+
                                                  '&op_term=~'+
                                                  '&inputvalue_term='+targetTerm+
                                                  '&input_term=uf_translations'+
                                                  '&op_term=~&'+
                                                  '&inputvalue_term='+targetTerm+
                                                  '&operator_term=or'+
                                                  '&output_term1=name'+
                                                  '&=deSelectAllOutputs';

    /*
    $.ajax({url: thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus, success: function(result){
            alert('4');
            alert(result);
            //$("#div1").html(result);
        
    }, error: function(xhr,status,error){
        alert('3');
            alert(error);
            //$("#div1").html(result);
        
    }});
*/

    //alert('1');
    
    //alert(params);
    //alert(thesaurusBaseUrl);
    //alert(targetUserName);
    
    //alert(targetThesaurus);
    //Quick Search 
    //var params = '?updateTermCriteria=QuickSearch&QuickSearchInputValue='+targetTerm
    
    //window.open(thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus);
    
    $.ajax({url: thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus,
        type: "GET",
        dataType: "xml", success: function(xml){
            targetTable.clear();
             $(xml).find("descriptor").each(function(){
                    targetTable.row.add([$(this).text()+'','-','-']);
               });
             targetTable.draw();
            //alert(result);
            //$("#div1").html(result);
        
    }, error: function(xhr,status,error){
        //alert('3');
            //alert(error);
            //$("#div1").html(result);
        
    }});
    $('#ShowResultsDiv').show();
    
}