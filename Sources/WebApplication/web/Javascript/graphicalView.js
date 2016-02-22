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
                    GraphicalViewIconPressed()
-------------------------------------------------------
  INPUT: - target: the name of the node which SVG graph will be displayed
         - targetKind: the kind of the node ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
  FUNCTION: displays the SVG graph of the target node
  CALLED BY:  in case targetKind = 'DESCRIPTOR', from search_results.xsl (TODO)
              in case targetKind = 'HIERARCHY', from search_results_hierarchies.xsl
              in case targetKind = 'FACET', from search_results_facets.xsl (TODO)
-------------------------------------------------------*/           
function GraphicalViewIconPressed(servlet, target, targetKind, CalledBySVG, userGroup, selectedThesaurus, SVG_CategoriesFrom_for_traverse, SVG_CategoriesNames_for_traverse) {	
  //alert('GraphicalViewIconPressed for target: ' + target + ' of kind: ' + targetKind + '\r\rUNDER CONSTRUCTION');
  // open a new IE window
  var windowName = ConstructWindowName(target, targetKind);

 
 var url='';
 if(CalledBySVG=='true'){
     //alert(userGroup);
     var str =  window.location.toString();
     var indexOfQuestionMark = str.indexOf('SVGproducer');
     if(indexOfQuestionMark==-1){
        indexOfQuestionMark = str.indexOf('GraphicalView');
     }
     url += str.substring(0,indexOfQuestionMark);
     url += 'GraphicalView?TargetName=' + escape(target) + 
                    '&TargetKind=' +  escape(targetKind) +
                    '&CalledBySVGgraph=true' +
                    '&userGroup='+escape(encodeURIComponent(userGroup)) +
                    '&selectedThesaurus='+escape(encodeURIComponent(selectedThesaurus)) +
                    '&SVG_CategoriesFrom_for_traverse=' +escape(encodeURIComponent(SVG_CategoriesFrom_for_traverse)) +
                    '&SVG_CategoriesNames_for_traverse=' +escape(encodeURIComponent(SVG_CategoriesNames_for_traverse));
 }
 else{
     url += servlet + '?TargetName=' + escape(encodeURIComponent(target)) + '&TargetKind=' +  escape(encodeURIComponent(targetKind));
     //  alert('url = ' + url);

 }
  //alert('url = ' + url);
     PopUpNewWindow(url, windowName, 800, 650);


}
function alert2(){
    alert('file ok')
}

/*-----------------------------------------------------
                    ConstructWindowName()
-------------------------------------------------------
  FUNCTION: returns a unique name for the pop-up window
            o be opened with current date and time.
            The name must be unique so as to open multiple
            windows any time graph view icon is pressed.
            Do NOT use the target name because in case of
            containing special characters, window.open() does NOT work
-------------------------------------------------------*/           
function ConstructWindowName(target, targetKind) {
  var currentTime = new Date();
  winName = currentTime.getFullYear() + '_' + currentTime.getMonth() + '_' + currentTime.getDate() + '_';
  winName += currentTime.getHours() + '_' + currentTime.getMinutes() + '_' + currentTime.getSeconds();
  //alert(winName);
  return winName;	
}

/*-----------------------------------------------------
                    PopUpNewWindow()
-------------------------------------------------------
  INPUT: - url: the web address of the page you wish to appear in the new window
         - winName: the name of the new window, in case we need 
                    to make a reference to it later (ATTENTION: no blanks!!!! so as window.open() works properly)
         - w, h: width and height of the new window
  FUNCTION: opens a new browser window for the viewer to use
-------------------------------------------------------*/           
function PopUpNewWindow(url, winName, w, h) {
  x = window.screenLeft;
  y = window.screenTop;
  // todo: location=yes (ONLY for debugging) - to be: location=no
  var windowConfiguration = 'left='+x+',top='+y+',width='+w+',height='+h+',resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes';
  var w = open(url, winName, windowConfiguration);
  w.focus();
}









