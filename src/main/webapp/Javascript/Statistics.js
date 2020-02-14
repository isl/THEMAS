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
                    StatisticsDisplayDIV()
-------------------------------------------------------
  INPUT: - divID: the id of the TAB to be displayed
                  StatisticsOfTerms_DIV/StatisticsOfHierarchies_DIV/StatisticsOfFacets_DIV/StatisticsOfSources_DIV/StatisticsOfUsers_DIV
                  (DIV ids defined in Statistics.xsl)
  CALLED BY:  
-------------------------------------------------------*/           
function StatisticsDisplayDIV(divID) {	
  
  document.getElementById('StatisticsOfTerms_DIV').style.visibility = 'hidden';
  document.getElementById('StatisticsOfHierarchies_DIV').style.visibility = 'hidden';
  document.getElementById('StatisticsOfFacets_DIV').style.visibility = 'hidden';
  document.getElementById('StatisticsOfSources_DIV').style.visibility = 'hidden';
  document.getElementById('StatisticsOfUsers_DIV').style.visibility = 'hidden';
  document.getElementById(divID).style.visibility = 'visible';
  
  
  var LinkId = divID.replace('_DIV','_LINK');
  document.getElementById('StatisticsOfTerms_LINK').className ="inactive";
  document.getElementById('StatisticsOfHierarchies_LINK').className ="inactive";
  document.getElementById('StatisticsOfFacets_LINK').className ="inactive";
  document.getElementById('StatisticsOfSources_LINK').className ="inactive";
  document.getElementById('StatisticsOfUsers_LINK').className ="inactive";
  document.getElementById(LinkId).className ="active";
}

/*-----------------------------------------------------
                    openStatisticsDIV()
-------------------------------------------------------
  INPUT: - divID: the id of the TAB to be opened
                  StatisticsOfTerms_DIV/StatisticsOfHierarchies_DIV/StatisticsOfFacets_DIV/StatisticsOfSources_DIV/StatisticsOfUsers_DIV
                  (DIV ids defined in Statistics.xsl)
-------------------------------------------------------*/           
function openStatisticsDIV(divID) {	
  DisplayPleaseWaitScreen(true);
  var targetgetResultsServlet = 'Statistics?DIV=' + divID;
  
  window.location = targetgetResultsServlet;
}

function openCustomStatisticsDIV(divID){
    DisplayPleaseWaitScreen(true);
    var targetgetResultsServlet = 'CustomStatistics?DIV=' + divID;
  
    window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
                    SaveStatisticResults()
-------------------------------------------------------*/           
function SaveStatisticResults(servletName) {
    
    DisplayPleaseWaitScreen(true);
    var windowName = ConstructWindowName('', '');
    var x = window.screenLeft + 50;
    var y = window.screenTop +20;
    // todo: location=yes (ONLY for debugging) - to be: location=no
    var windowConfiguration = 'left='+x+',top='+y+',width='+668+',height='+400 +',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
    var pleaseWaitWindow = window.open('WaitForDownload', windowName, windowConfiguration);

    pleaseWaitWindow.focus();
    
    var params='';
    /*
    var params='?usePreviousCriteria='+usePreviousCriteria;
    if(hierName!=''){
        params +='&hierarchy=' + escape(encodeURIComponent(hierName)) + '&action='+display;
    }
    else{
        params +='&pageFirstResult=SaveAll';
    }
    */
    
    var xmlHttp;
    try  {  // Firefox, Opera 8.0+, Safari  
        xmlHttp=new XMLHttpRequest();  
    }
    catch (e) {  // Internet Explorer  
        try {    
            xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");    
        }
        catch (e) {    
            try {
                xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");      
            }
            catch (e) {      
                alert("Your browser does not support AJAX!");      
                return false;      
            }    
        }  
    }  
    
    xmlHttp.onreadystatechange=function() {
        
        if(xmlHttp.readyState==4)
        {	
            DisplayPleaseWaitScreen(false);
            var txt = xmlHttp.responseText;
            txt = txt.replace(/^\s+|\s+$/g,'');
            
            if(txt  ==  'Session Invalidate'){
                pleaseWaitWindow.close();
                window.location = 'Index';
            }
            else{
                //alert(txt);
                //window.open('DownloadFile?targetFile='+txt, windowName, windowConfiguration);
                //pleaseWaitWindow.location= 'Save_Results_Displays\\Save_Results_temporary_files\\SearchResults_Sources_2009-06-29_15-45-59-819.html';
                pleaseWaitWindow.location= txt;
                //pleaseWaitWindow.location= 'Save_Results_Displays\\Save_Results_temporary_files\\Statistics_2009-06-30_13-58-17-66.html';
                pleaseWaitWindow.focus();
            }
            
                
        }
    }    
    
    //DisplayPleaseWaitScreen(true);
    //alert(servletName + params);
    //xmlHttp.open("GET",servletName + '?pageFirstResult=SaveAll', false);
    xmlHttp.open("GET",servletName + params, true);  
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    xmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");    
    
    xmlHttp.send(null);   
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsWithXA()
-------------------------------------------------------
  FUNCTION: calls SearchResults_Terms servlet with criteria uf ! '' and outputs=����, uf
-------------------------------------------------------*/           
function viewStatisticsResults_TermsWithXA() {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + '';
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=uf'; // and/or + outputs
  targetgetResultsServlet += '&input_term=uf'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=uf'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsWithTranslations()
-------------------------------------------------------
  FUNCTION: calls SearchResults_Terms servlet with criteria translations ! '' and outputs=����, uf
-------------------------------------------------------*/           
function viewStatisticsResults_TermsWithTranslations() {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + '';
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=translations'; // and/or + outputs
  targetgetResultsServlet += '&input_term=translations'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term='; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsWithUF()
-------------------------------------------------------
  FUNCTION: calls SearchResults_Terms servlet with criteria uk_uf ! '' and outputs=����, uf
-------------------------------------------------------*/           
function viewStatisticsResults_TermsWithUF() {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + '';
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=uf_translations'; // and/or + outputs
  targetgetResultsServlet += '&input_term=uf_translations'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=uf_translations'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfHierarchy()
-------------------------------------------------------
  INPUT: - hierarchy = g.e. '�������� �������� - �������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria OK=hierarchy and outputs=����, AO, ��, OK
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfHierarchy(hierarchy) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(hierarchy)); // g.e. &inputvalue_term = �������� ...
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=topterm';//translations&output_term=bt&output_term=nt&output_term=topterm'; // and/or + outputs
  targetgetResultsServlet += '&input_term=topterm'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term='; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

function viewStatisticsResults_TranslationsOfHierarchy(hierarchy) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';  
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(hierarchy));
  targetgetResultsServlet += '&inputvalue_term=' + ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=translations&output_term1=topterm';
  targetgetResultsServlet += '&input_term=topterm'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_term (=, ! ...)
  targetgetResultsServlet += '&input_term=translations'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term='; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_NonPrefferedTermsOfHierarchy()
-------------------------------------------------------
  INPUT: - hierarchy = g.e. '�������� �������� - �������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria (OK=hierarchy and XA !='') and outputs=XA
-------------------------------------------------------*/           
function viewStatisticsResults_NonPrefferedTermsOfHierarchy(hierarchy) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(hierarchy)); // g.e. &inputvalue_term = �������� ...
  targetgetResultsServlet += '&inputvalue_term='; // ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=topterm&output_term1=uf'; // and/or + outputs
  targetgetResultsServlet += '&input_term=topterm'; // OK
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_term (=, ! ...)
  targetgetResultsServlet += '&input_term=uf'; // XA
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=uf'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_NonPrefferedTranslationsOfHierarchy()
-------------------------------------------------------
  INPUT: - hierarchy = g.e. '�������� �������� - �������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria (OK=hierarchy and UF !='') and outputs=UF
  -------------------------------------------------------*/           
function viewStatisticsResults_NonPrefferedTranslationsOfHierarchy(hierarchy) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(hierarchy)); // g.e. &inputvalue_term = �������� ...
  targetgetResultsServlet += '&inputvalue_term='; // ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=topterm&output_term1=uf_translations'; // and/or + outputs
  targetgetResultsServlet += '&input_term=topterm'; // OK
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_term (=, ! ...)
  targetgetResultsServlet += '&input_term=uf_translations'; // UF
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_term (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=uf_translations'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_HierarchiesOfFacet()
-------------------------------------------------------
  INPUT: - facet = g.e. '������������ - �����������'         
  FUNCTION: calls SearchResults_Facets servlet with criteria facet=facet and outputs=�������������, ���������
-------------------------------------------------------*/           
function viewStatisticsResults_HierarchiesOfFacet(facet) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Facets?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_facet=' + escape(encodeURIComponent(facet)); // g.e. &inputvalue_facet = ������������ - ����������� ...
  targetgetResultsServlet += '&operator_facet=and&output_facet=name&output_facet=hierarchy'; // and/or + outputs
  targetgetResultsServlet += '&input_facet=name'; // input_facet
  targetgetResultsServlet += '&op_facet=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfFacet()
-------------------------------------------------------
  INPUT: - facet = g.e. '������������ - �����������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria facet=facet and outputs=����, AO, ��, OK
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfFacet(facet) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(facet)); // g.e. &inputvalue_term = ������������ - ����������� ...
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=facet'; // and/or + outputs
  targetgetResultsServlet += '&input_term=facet'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_NonPrefferedTermsOfFacet()
-------------------------------------------------------
  INPUT: - facet = g.e. '������������ - �����������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria (facet=facet and XA !='') and outputs=����, XA
-------------------------------------------------------*/           
function viewStatisticsResults_NonPrefferedTermsOfFacet(facet) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(facet)); // g.e. &inputvalue_term = ������������ - ����������� ...
  targetgetResultsServlet += '&inputvalue_term='; // ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=uf&output_term1=facet'; // and/or + outputs
  targetgetResultsServlet += '&input_term=facet'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&input_term=uf'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_facet (=, ! ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TranslationsOfFacet()
-------------------------------------------------------
  INPUT: - facet = g.e. '������������ - �����������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria (facet=facet and AO !='') and outputs=����, AO
-------------------------------------------------------*/           
function viewStatisticsResults_TranslationsOfFacet(facet) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(facet)); // g.e. &inputvalue_term = ������������ - ����������� ...
  targetgetResultsServlet += '&inputvalue_term='; // ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=translations&output_term1=facet'; // and/or + outputs
  targetgetResultsServlet += '&input_term=facet'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&input_term=translations'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&outputSel_term='; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_NonPrefferedTranslationsOfFacet()
-------------------------------------------------------
  INPUT: - facet = g.e. '������������ - �����������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria (facet=facet and UF !='') and outputs=����, UF
-------------------------------------------------------*/           
function viewStatisticsResults_NonPrefferedTranslationsOfFacet(facet) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(facet)); // g.e. &inputvalue_term = ������������ - ����������� ...
  targetgetResultsServlet += '&inputvalue_term='; // ''
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=uf_translations&output_term1=facet'; // and/or + outputs
  targetgetResultsServlet += '&input_term=facet'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&input_term=uf_translations'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('!')); // op_facet (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=uf_translations'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfSourceGr()
-------------------------------------------------------
  INPUT: - source = g.e. 'AAT'         
  FUNCTION: calls SearchResults_Terms servlet with criteria primary_found_in=source and outputs=����, AO, ��, OK, primary_found_in
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfPrimarySource(source) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(source)); // g.e. &inputvalue_term = AAT ...
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=primary_found_in'; // and/or + outputs
  targetgetResultsServlet += '&input_term=primary_found_in'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=primary_found_in'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfSourceEn()
-------------------------------------------------------
  INPUT: - source = g.e. 'AAT'         
  FUNCTION: calls SearchResults_Terms servlet with criteria translations_found_in=source and outputs=����, AO, ��, OK, translations_found_in
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfTranslationsSource(source) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(source)); // g.e. &inputvalue_term = AAT ...
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=translations_found_in';//translations&output_term=bt&output_term=nt&output_term=topterm'; // and/or + outputs
  targetgetResultsServlet += '&input_term=translations_found_in'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=translations_found_in'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_AllTermsWithOutputsCreatorAndModificator()
-------------------------------------------------------
  FUNCTION: calls SearchResults_Terms servlet with criteria showAll=all and outputs=����, AO, ��, OK, created_by, modified_by
-------------------------------------------------------*/           
function viewStatisticsResults_AllTermsWithOutputsCreatorAndModificator() {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria&showAll=all';
  targetgetResultsServlet += '&inputvalue_term=';
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=translations&output_term1=bt&output_term1=nt&output_term1=topterm'; // and/or + outputs
  targetgetResultsServlet += '&input_term=name'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&output_term1=created_by&output_term1=modified_by'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfCreator()
-------------------------------------------------------
  INPUT: - creator = g.e. '��������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria translations_found_in=source and outputs=����, AO, ��, OK, created_by
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfCreator(creator) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(creator)); // g.e. &inputvalue_term = �������� ...
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=created_by'; // and/or + outputs
  targetgetResultsServlet += '&input_term=created_by'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  //targetgetResultsServlet += '&outputSel_term=created_by'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}

/*-----------------------------------------------------
      viewStatisticsResults_TermsOfModificator()
-------------------------------------------------------
  INPUT: - modificator = g.e. '��������'         
  FUNCTION: calls SearchResults_Terms servlet with criteria translations_found_in=source and outputs=����, AO, ��, OK, modified_by
-------------------------------------------------------*/           
function viewStatisticsResults_TermsOfModificator(modificator) {
  DisplayPleaseWaitScreen(true);
  
  var targetgetResultsServlet = 'SearchResults_Terms?updateTermCriteria=parseCriteria';
  targetgetResultsServlet += '&inputvalue_term=' + escape(encodeURIComponent(modificator)); // g.e. &inputvalue_term = �������� ...
  //targetgetResultsServlet += '&operator_term=and&output_term=name&output_term=translations&output_term=bt&output_term=nt&output_term=topterm'; // and/or + outputs
  targetgetResultsServlet += '&operator_term=and&output_term1=name&output_term1=modified_by'; // and/or + outputs
  targetgetResultsServlet += '&input_term=modified_by'; // input_term
  targetgetResultsServlet += '&op_term=' + escape(encodeURIComponent('=')); // op_facet (=, ! ...)
  targetgetResultsServlet += '&outputSel_term=modified_by'; // extra outputs (nt, rt, ...)
  window.location = targetgetResultsServlet;
}


