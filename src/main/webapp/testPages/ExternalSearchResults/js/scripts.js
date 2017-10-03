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


//var thesaurusBaseUrl = 'http://139.91.183.97:8080/THEMAS/';
var thesaurusBaseUrl = 'http://localhost:8087/THEMAS/';
var targetUserName = 'ExternalReader';
//var targetThesaurus = 'AATDEMO';
var targetThesaurus = 'ANCIENT';



var config = {
    '.chosen-select': {width: '500px', search_contains: true, allow_single_deselect: true},
    '.chosen-select-deselect': {allow_single_deselect: true},
    '.chosen-select-no-single': {disable_search_threshold: 10},
    '.chosen-select-no-results': {no_results_text: 'Oops, nothing found!'},
    '.chosen-select-rtl': {rtl: true},
    '.chosen-select-width': {width: '95%'}
}

//variables that will be asssigned the 3 different xsl documents that are used for the HTML display of each XML
var starxsl = null;
var hierarchicalxsl = null;
var facetOrThesaurusHierarchicalXsl = null;

//function used in order to preload the 3 diffenrent xsl in the above 3 variables
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
    try {
        xhttp.responseType = "msxml-document"
    } catch (err) {
    } // Helping IE11
    xhttp.send("");
    return xhttp.responseXML;
}

//document ready function
$(function () {
    for (var selector in config) {
        $(selector).chosen(config[selector]);
    }

    $('#resultsSelect').on('change', function (evt, params) {
        var selText = $("#resultsSelect option:selected").val();
        if (selText == null || selText === null || selText.trim().length == 0) {

            $("#starViewBtn").hide();
            //$("#hierarchicalViewBtn").hide();
            $("#starDiv").val('');
        }
        else {
            $("#starViewBtn").show();
            $("#hierarchicalViewBtn").show();
            $("#starDiv").val('');
        }
    });

    $("#SearchThemasParam").keyup(function (event) {
        if (event.keyCode == 13) {
            $("#searchBtn").click();
        }
    });

    starxsl = loadXMLDoc("starxsl.xsl");
    hierarchicalxsl = loadXMLDoc("Hierarchical_Term_Display.xsl");
    facetOrThesaurusHierarchicalXsl = loadXMLDoc("Thesaurus_Global_Hierarchical_View.xsl");
    retrieveFacets();

});


//display please wait window
$body = $("body");
$(document).on({
    ajaxStart: function () {
        $body.addClass("loading");
    },
    ajaxStop: function () {
        $body.removeClass("loading");
    }
});

//load the URL request input field with the url used for the 
//ajax xml retrival. This url may refer to
// - listing of thesarus facets
// - custom search that will be performed depending on the 
//   selection of Facet and output fields
// - star view of selected term
// - hierarchical view of selected term  or selected Facet 
//   if no term is selected or selected Thesaurus if neither term 
//   nor Facet is selected  
function ShowUrlRequest(urlVal){
    if (urlVal != null && urlVal !== null && urlVal.trim().length > 0) {
        $("#showURLRequest").val(urlVal);
    }
    else{
        $("#showURLRequest").val('');
    }
}

function getUrlForRetrieveFacets() {
    var params = '?updateTermCriteria=parseCriteria'
            + '&inputvalue_facet='
            + '&showAll=all'
            + '&operator_facet=and'
            + '&output_facet=name'
            + '&answerType=XMLSTREAM'
            + '&pageFirstResult=SaveAll'
            ;//+ '&output_facet=hierarchy';

    return thesaurusBaseUrl + 'SearchResults_Facets' + params + '&external_user=' + targetUserName + '&external_thesaurus=' + targetThesaurus
}

function getUrlForCustomSearch() {
    
    var selFacetIdText = $("#facetsResultSelect option:selected").val();
    
    if (selFacetIdText === "") {
        selFacetIdText = '&showAll=all';
    }
    
    var $boxes = $('fieldset.group input[type=checkbox]:checked');
    
    var params = '?updateTermCriteria=parseCriteria' + '&answerType=XMLSTREAM' + '&pageFirstResult=SaveAll' +
            //facet name contains predicate
            '&input_term=facet' +
            '&op_term=refid=' +
            '&inputvalue_term=' + selFacetIdText +
            //combine operator OR (instead of and)
            '&operator_term=or' +
            //output field: onty the term name
            '&output_term1=name' +
            /*
            '&output_term1=uf' +
            '&output_term1=rnt' +
            '&output_term1=rbt' +
            */
            //'&=deSelectAllOutputs'
            '';
    if($boxes!==null){
        $boxes.each(function(){
            params += '&output_term1='+$(this).val();
        });
    }
    return thesaurusBaseUrl + 'SearchResults_Terms' + params + '&external_user=' + targetUserName + '&external_thesaurus=' + targetThesaurus;
}

function getUrlForStarView(){
    
    var selIdText = $("#resultsSelect option:selected").val();
    if (selIdText !== null && selIdText.trim().length > 0) {
        var targetTermReferenceUrl = 'CardOf_Term?referenceId=' + selIdText.trim();//escape(encodeURIComponent(selIdText.trim()));
        //var params = '?mode=XMLSTREAM'+'&term='+targetTermId;
        var params = '&mode=XMLSTREAM&external_user=' + targetUserName + '&external_thesaurus=' + targetThesaurus;
        return thesaurusBaseUrl + targetTermReferenceUrl + params;
    }    
    return '';
}

function getUrlForTermHierarchicalView(){
    var selIdText = $("#resultsSelect option:selected").val();
    
    if (selIdText !== null && selIdText.trim().length > 0) {
        var targetTermReferenceUrl = 'SearchResults_Terms_Hierarchical?referenceId=' +selIdText.trim(); /*escape(encodeURIComponent(selIdText.trim()));*/
        var params = '&answerType=XMLSTREAM' + '&external_user=' + targetUserName + '&external_thesaurus=' + targetThesaurus;
        
        return thesaurusBaseUrl + targetTermReferenceUrl + params;
    }
    return '';
}

function getUrlForFacetOrGlobalThesaurusHierarchicalView(){
    
    //var selFacetNameText = $("#facetsResultSelect option:selected").text();
    var selFacetId = $("#facetsResultSelect option:selected").val();
    var targetFacetReferenceUrl = 'hierarchysTermsShortcuts?';
    var action = '';
    if ($("#facetsResultSelect").val() === "") {
        action = "GlobalThesarusHierarchical";//"facethierarchical";//"GlobalThesarusHierarchical";
    }
    else {
        action = 'facethierarchical';
        //targetFacetReferenceUrl += 'hierarchy=' + escape(encodeURIComponent(selFacetNameText)) + '&';
        targetFacetReferenceUrl += 'hierarchyRefId=' + selFacetId + '&';
    }
    
    var params = 'action=' + action + '&answerType=XMLSTREAM' + '&external_user=' + targetUserName + '&external_thesaurus=' + targetThesaurus;
    
    return thesaurusBaseUrl + targetFacetReferenceUrl + params;
}

function retrieveFacets() {
    $('#facetsResultSelect option').remove();
    $("#facetsResultSelect").trigger("chosen:updated");

    var targetUrl = getUrlForRetrieveFacets();
    ShowUrlRequest(targetUrl)
    $.ajax({url: targetUrl,
        type: "GET",
        dataType: "xml", success: function (xml) {

            //$("#div1").html(xml);
            //alert('edw');
            $('#facetsResultSelect').append('<option value="" selected>&nbsp;</option>');
            var s = new XMLSerializer();
            if ($(xml).find("data").length > 0) {
                var d = $(xml).find("data")[0];

                var str = s.serializeToString(d);

                $("#allFacetsDiv").val(vkbeautify.xml(str, 4));
            }

            $(xml).find("facet").each(function () {
                var facetName = '';
                var referenceIdText = '';

                facetName = $(this).find("name").text();
                referenceIdText = $(this).find("name").attr('referenceId');


                if (facetName !== '') {
                    $('#facetsResultSelect').append('<option value="' + referenceIdText + '">' + facetName + '</option>');
                }

            });

            $("#facetsResultSelect").trigger("chosen:updated");
        }
    });
}

//searching THEMAS
function searchTHEMAS() {
    //alert($('#SearchThemasParam').val());
    $('#panel1').hide();
    $('#panel2').hide();
    $("#starDiv").val('');
    $("#starDivXSLT").html('');

    $('#resultsSelect option').remove();
    $("#resultsSelect").trigger("chosen:updated");

    var targetUrl = getUrlForCustomSearch();
    ShowUrlRequest(targetUrl)
    //exact match
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term=' + targetTerm + '&operator_term=and&output_term1=name&input_term=name&op_term=%253D';

    //contains in term name only
    //var params = '?updateTermCriteria=parseCriteria&inputvalue_term='+targetTerm+'&operator_term=and&output_term1=name&=deSelectAllOutputs&input_term=name&op_term=~&'

    //Quick Search 
    //var params = '?updateTermCriteria=QuickSearch&QuickSearchInputValue='+targetTerm

    //window.open(thesaurusBaseUrl + 'SearchResults_Terms' + params +'&external_user=' +targetUserName+ '&external_thesaurus='+ targetThesaurus);

    $.ajax({url: targetUrl,
        type: "GET",
        dataType: "xml", success: function (xml) {

            //$("#div1").html(xml);
            //alert('edw');
            $('#resultsSelect').append('<option value="" selected>&nbsp;</option>');
            var s = new XMLSerializer();
            if ($(xml).find("data").length > 0) {
                var d = $(xml).find("data")[0];

                var str = s.serializeToString(d);

                $("#searchDiv").val(vkbeautify.xml(str, 4));
            }

            $(xml).find("term").each(function () {
                var descriptorName = '';
                var referenceUrlText = '';
                var ufs = '';

                descriptorName = $(this).find("descriptor").text();
                referenceUrlText = $(this).find("descriptor").attr('referenceId');
                $(this).find("uf").each(function () {
                    if (ufs != '') {
                        ufs += ', ';
                    }
                    ufs += $(this).text();
                });

                if (descriptorName != '') {

                    //var optionValue = '<option value="'+idText+'" id="'+idText+'"';
                    if (ufs != '') {
                        $('#resultsSelect').append('<option value="' + referenceUrlText + '">' + descriptorName + ' (UFs: ' + ufs + ')</option>');
                    }
                    else {
                        $('#resultsSelect').append('<option value="' + referenceUrlText + '">' + descriptorName + '</option>');
                    }
                }

            });

            $("#resultsSelect").trigger("chosen:updated");
            $("#starViewBtn").hide();
            //$("#hierarchicalViewBtn").hide();

        }
    });


    $('#ShowResultsDiv').show();

}

function starView() {
    var targetUrl = getUrlForStarView();
    ShowUrlRequest(targetUrl);
    if (targetUrl != null && targetUrl !== null && targetUrl.trim().length > 0) {
        //alert('startView');

        
        $("#starDivXSLT").html('');

        $.ajax({url: targetUrl,
            type: "GET",
            dataType: "xml", success: function (xml) {

                var s = new XMLSerializer();

                if ($(xml).find("data").length > 0) {
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
                else {
                    $("#starDiv").val('nothing returned');
                    $("#starDivXSLT").html('nothing returned');
                }

                //#starDivXSLT

                $("#panel1").show();
                //$('#myIframe').src = $(xml);

            }
        });

    }
}

function hierarchicalView() {

    var targetUrl = getUrlForTermHierarchicalView();
    
    if (targetUrl !== null && targetUrl.trim().length > 0) {
        
        ShowUrlRequest(targetUrl);
        
        //hierarchical view of term
        $("#hierDivXSLT").html('');

        $.ajax({url: targetUrl,
            type: "GET",
            dataType: "xml", success: function (xml) {

                var s = new XMLSerializer();
                if ($(xml).find("page").length > 0) {
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
                else {
                    $("#hierDiv").val('nothing returned');
                    $("#hierDivXSLT").html('nothing returned');
                }

                //#starDivXSLT

                $("#panel2").show();

            }
        });
    }
    else{
        targetUrl = getUrlForFacetOrGlobalThesaurusHierarchicalView();
        ShowUrlRequest(targetUrl);
        
        $("#hierDivXSLT").html('');

        $.ajax({url: targetUrl,
            type: "GET",
            dataType: "xml", success: function (xml) {

                var s = new XMLSerializer();
                if ($(xml).find("page").length > 0) {
                    var d = $(xml).find("page")[0];

                    var str = s.serializeToString(d);

                    $("#hierDiv").val(vkbeautify.xml(str, 4));


                    if (window.ActiveXObject || xhttp.responseType == "msxml-document")
                    {
                        ex = xml.transformNode(facetOrThesaurusHierarchicalXsl);
                        document.getElementById("hierDivXSLT").innerHTML = ex;
                    }
                    // code for Chrome, Firefox, Opera, etc.
                    else if (document.implementation && document.implementation.createDocument)
                    {
                        xsltProcessor = new XSLTProcessor();
                        xsltProcessor.importStylesheet(facetOrThesaurusHierarchicalXsl);
                        resultDocument = xsltProcessor.transformToFragment(xml, document);
                        document.getElementById("hierDivXSLT").appendChild(resultDocument);
                    }
                }
                else {
                    $("#hierDiv").val('nothing returned');
                    $("#hierDivXSLT").html('nothing returned');
                }

                //#starDivXSLT

                $("#panel2").show();

            }
        });
    }    
}


function tooglePanel1() {

    if ($("#panel1").is(":visible")) {
        $("#panel1").hide();
    }
    else {
        $("#panel1").show();
    }
}

function tooglePanel2() {

    if ($("#panel2").is(":visible")) {
        $("#panel2").hide();
    }
    else {
        $("#panel2").show();
    }
}

function ShowHideStarXML() {
    if ($("#starDiv").is(":visible")) {
        $("#starDiv").hide();
    }
    else {
        $("#starDiv").show();
    }
}

function ShowHideSearchResultsXML() {
    if ($("#searchDiv").is(":visible")) {
        $("#searchDiv").hide();
    }
    else {
        $("#searchDiv").show();
    }
}

function ShowHideHierXML() {
    if ($("#hierDiv").is(":visible")) {
        $("#hierDiv").hide();
    }
    else {
        $("#hierDiv").show();
    }
}

function ShowHideListFacetsXML() {
    if ($("#allFacetsDiv").is(":visible")) {
        $("#allFacetsDiv").hide();
    }
    else {
        $("#allFacetsDiv").show();
    }
}
