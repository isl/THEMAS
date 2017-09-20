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
//var targetThesaurus = 'AATDEMO';
var targetThesaurus = 'ANCIENT';



var config = {
  '.chosen-select'           : {width: '500px',search_contains: true,allow_single_deselect: true},
  '.chosen-select-deselect'  : { allow_single_deselect: true },
  '.chosen-select-no-single' : { disable_search_threshold: 10 },
  '.chosen-select-no-results': { no_results_text: 'Oops, nothing found!' },
  '.chosen-select-rtl'       : { rtl: true },
  '.chosen-select-width'     : { width: '95%' }
}

var starxsl = null;
var hierarchicalxsl = null;

function loadXMLDoc(filename)
{
    if (window.ActiveXObject)
    {
        xhttp = new ActiveXObject("Msxml2.XMLHTTP");
    }
    else 
    {
        xhttp = new XMLHttpRequest();
    }
    xhttp.open("GET", filename, false);
    try {xhttp.responseType = "msxml-document"} catch(err) {} // Helping IE11
    xhttp.send("");
    return xhttp.responseXML;
}

//document ready function
$(function() {    
    for (var selector in config) {
      $(selector).chosen(config[selector]);
    }
    
    $('#resultsSelect').on('change', function(evt, params) {
        var selText = $("#resultsSelect option:selected").val();
        if(selText==null || selText===null || selText.trim().length==0){
            
            $("#starViewBtn").hide();
            $("#hierarchicalViewBtn").hide();
            $("#starDiv").val('');
        }
        else{
            $("#starViewBtn").show();
            $("#hierarchicalViewBtn").show();
            $("#starDiv").val('');
        }
    });

    $("#SearchThemasParam").keyup(function(event){
        if(event.keyCode == 13){
            $("#searchBtn").click();
        }
    });
    
    starxsl = loadXMLDoc("starxsl.xsl");
    hierarchicalxsl = loadXMLDoc("Hierarchical_Term_Display.xsl");
    /*
    var accordeons = $(".accordion");
    accordeons.each(function(){
        $(this).onclick = function(){
            alert('edw');
            // Toggle between adding and removing the "active" class,
            //to highlight the button that controls the panel 
            this.classList.toggle("active");

            // Toggle between hiding and showing the active panel 
            var panel = this.nextElementSibling;
            if (panel.style.display === "block") {
                panel.style.display = "none";
            } else {
                panel.style.display = "block";
            }
        }
        
    });*/

} );

//display please wait window
$body = $("body");

$(document).on({
    ajaxStart: function() { $body.addClass("loading");    },
     ajaxStop: function() { $body.removeClass("loading"); }    
});


//searching THEMAS in term names, ufs, uf translations etc?
function searchTHEMAS(){
    //alert($('#SearchThemasParam').val());
    $('#panel1').hide();
    $('#panel2').hide();
    $("#starDiv").val('');
    $("#starDivXSLT").html('');
    
    
    var targetTerm = escape(encodeURIComponent($('#SearchThemasParam').val()));
    //$('#resultsSelectElement').options.clear();
    $('#resultsSelect option').remove();
    $("#resultsSelect").trigger("chosen:updated");

    //exact match
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term=' + targetTerm + '&operator_term=and&output_term1=name&input_term=name&op_term=%253D';
    
    //contains in term name only
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term='+targetTerm+'&operator_term=and&output_term1=name&=deSelectAllOutputs&input_term=name&op_term=~&'
    
    //contains in term name only or in UF terms or uf translation terms
    var params = '?updateTermCriteria=parseCriteria' +'&answerType=XMLSTREAM'+'&pageFirstResult=SaveAll'+
                                                  //term name contains predicate
                                                  '&input_term=name'+
                                                  //'&op_term=~'+
                                                  '&op_term=transliteration~'+
                                                  '&inputvalue_term='+targetTerm+
                                                  //or used for term (non-preferred term) contains predicate
                                                  '&input_term=uf'+
                                                  '&op_term=~'+
                                                  '&inputvalue_term='+targetTerm+
                                                  //or translation used for term (translation non-preferred term) contains predicate
                                                  '&input_term=uf_translations'+
                                                  '&op_term=~&'+
                                                  '&inputvalue_term='+targetTerm+
                                                  
                                                  //combine operator OR (instead of and)
                                                  '&operator_term=or'+
                                                          
                                                  //output field: onty the term name
                                                  '&output_term1=name'+
                                                  '&output_term1=uf'+        
                                                  '&output_term1=rnt'+     
                                                  '&output_term1=rbt'+   
                                                  //'&=deSelectAllOutputs'
                                                  '';

    //Quick Search 
    //var params = '?updateTermCriteria=QuickSearch&QuickSearchInputValue='+targetTerm
    
    //window.open(thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus);
    
    $.ajax({url: thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus,
            type: "GET",
            dataType: "xml", success: function(xml){
                
                //$("#div1").html(xml);
                //alert('edw');
                $('#resultsSelect').append('<option value="" selected>&nbsp;</option>');
                var s = new XMLSerializer();
                if($(xml).find("data").length>0){
                    var d = $(xml).find("data")[0];

                    var str = s.serializeToString(d);

                    $("#searchDiv").val(vkbeautify.xml(str, 4));
                }
                
                $(xml).find("term").each(function(){
                    var descriptorName = '';
                    var transliteration = '';
                    var referenceUrlText='';
                    var ufs ='';
                    
                    descriptorName = $(this).find("descriptor").text();
                    transliteration = $(this).find("descriptor").attr('transliteration');
                    referenceUrlText = $(this).find("descriptor").attr('referenceId');
                    $(this).find("uf").each(function(){
                    	if(ufs!=''){
                            ufs+=', ';
                          }
                          ufs+= $(this).text();
                    });
                    
                    if(descriptorName!=''){
                        
                        //var optionValue = '<option value="'+idText+'" id="'+idText+'"';
                        if(ufs!=''){
                            $('#resultsSelect').append('<option value="'+referenceUrlText+'">'+descriptorName+ ' (UFs: '+ufs+')</option>');                    
                        }
                        else{
                            $('#resultsSelect').append('<option value="'+referenceUrlText+'">'+descriptorName+'</option>');                    
                        }
                    }
                    
                });
                
                $("#resultsSelect").trigger("chosen:updated");
                $("#starViewBtn").hide();
                $("#hierarchicalViewBtn").hide();
                
        }
    });
    
    
    $('#ShowResultsDiv').show();
    
}

function tooglePanel1(){
    
    if($("#panel1").is(":visible")){
        $("#panel1").hide();
    }
    else{
        $("#panel1").show();
    }
}

function tooglePanel2(){
    
    if($("#panel2").is(":visible")){
        $("#panel2").hide();
    }
    else{
        $("#panel2").show();
    }
}

function ShowHideStarXML(){
    if($("#starDiv").is(":visible")){
        $("#starDiv").hide();
    }
    else{
        $("#starDiv").show();
    }
}

function ShowHideSearchResultsXML(){
    if($("#searchDiv").is(":visible")){
        $("#searchDiv").hide();
    }
    else{
        $("#searchDiv").show();
    }
}

function ShowHideHierXML(){
    if($("#hierDiv").is(":visible")){
        $("#hierDiv").hide();
    }
    else{
        $("#hierDiv").show();
    }
}


function starView(){
    var selIdText = $("#resultsSelect option:selected").val();
    if(selIdText!=null && selIdText!==null && selIdText.trim().length>0){
        //alert('startView');
        
        var targetTermReferenceUrl = 'CardOf_Term?referenceId='+escape(encodeURIComponent(selIdText.trim()));
        //var params = '?mode=XMLSTREAM'+'&term='+targetTermId;
        var params = '&mode=XMLSTREAM&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus;
    
        //window.open(thesaurusBaseUrl + 'CardOf_Term' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus);

        $("#starDivXSLT").html('');
        
        $.ajax({url: thesaurusBaseUrl + targetTermReferenceUrl +params,
                type: "GET",
                dataType: "xml", success: function(xml){
                    
                    var s = new XMLSerializer();
                    
                    if($(xml).find("data").length>0){
                        var d = $(xml).find("data")[0];
                        
                        var str = s.serializeToString(d);
                        
                        $("#starDiv").val(vkbeautify.xml(str, 4));
                        
                        
                        if (window.ActiveXObject || xhttp.responseType == "msxml-document")
                        {
                            ex = xml.transformNode(starxsl);
                            document.getElementById("starDivXSLT").innerHTML = ex;
                        }
                        // code for Chrome, Firefox, Opera, etc.
                        else if (document.implementation && document.implementation.createDocument)
                        {
                            xsltProcessor = new XSLTProcessor();
                            xsltProcessor.importStylesheet(starxsl);
                            resultDocument = xsltProcessor.transformToFragment(xml, document);
                            document.getElementById("starDivXSLT").appendChild(resultDocument);                            
                        }
                    }
                    else{
                        $("#starDiv").val('nothing returned');
                        $("#starDivXSLT").html('nothing returned');
                    }
                    
                    //#starDivXSLT
                    
                    $("#panel1").show();
                    //$('#myIframe').src = $(xml);
                    
                    
                    /*$('#resultsSelect').append('<option value="" selected>&nbsp;</option>');
                    $(xml).find("descriptor").each(function(){
                        $('#resultsSelect').append('<option value="'+$(this).text()+'">'+$(this).text()+'</option>');                    
                    });

                    $("#resultsSelect").trigger("chosen:updated");
                    $("#starViewBtn").hide();
                    $("#hierarchicalViewBtn").hide();
                    */

            }
        });
        
    }    
}

function hierarchicalView(){
    
    var selIdText = $("#resultsSelect option:selected").val();
    
    if(selIdText!=null && selIdText!==null && selIdText.trim().length>0){
        //alert('startView');
        
        
       var targetTermReferenceUrl = 'SearchResults_Terms_Hierarchical?referenceId='+ escape(encodeURIComponent(selIdText.trim()));
       //alert('hierarchicalView');
       ////alert('startView');
       //var targetTerm = escape(encodeURIComponent(selText.trim()));
       //var params = '?answerType=XMLSTREAM'+'&hierarchy='+targetTerm;
       var params = '&answerType=XMLSTREAM'+'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus;
       //window.open(thesaurusBaseUrl + 'SearchResults_Terms_Hierarchical' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus);       
       
       
       $("#hierDivXSLT").html('');
        
        $.ajax({url: thesaurusBaseUrl + targetTermReferenceUrl + params,
                type: "GET",
                dataType: "xml", success: function(xml){
                    
                    var s = new XMLSerializer();
                    if($(xml).find("page").length>0){
                        var d = $(xml).find("page")[0];
                        
                        var str = s.serializeToString(d);
                        
                        $("#hierDiv").val(vkbeautify.xml(str, 4));
                        
                        
                        if (window.ActiveXObject || xhttp.responseType == "msxml-document")
                        {
                            ex = xml.transformNode(hierarchicalxsl);
                            document.getElementById("hierDivXSLT").innerHTML = ex;
                        }
                        // code for Chrome, Firefox, Opera, etc.
                        else if (document.implementation && document.implementation.createDocument)
                        {
                            xsltProcessor = new XSLTProcessor();
                            xsltProcessor.importStylesheet(hierarchicalxsl);
                            resultDocument = xsltProcessor.transformToFragment(xml, document);
                            document.getElementById("hierDivXSLT").appendChild(resultDocument);                            
                        }
                    }
                    else{
                        $("#hierDiv").val('nothing returned');
                        $("#hierDivXSLT").html('nothing returned');
                    }
                    
                    //#starDivXSLT
                    
                    $("#panel2").show();                   

            }
        });
    }
}