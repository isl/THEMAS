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
/***********TYPE ahead functionality*////////////////
function loadTypeAheadActions() {
    if(document.getElementById('inputvalue_term')!=null){
        
        var oTextbox_t = new AutoSuggestControl(document.getElementById('inputvalue_term'), new RemoteStateSuggestions(),'inputvalue_term');
    }
    if(document.getElementById('inputvalue_hierarchy')!=null){
        
        oTextbox_h = new AutoSuggestControl(document.getElementById('inputvalue_hierarchy'), new RemoteStateSuggestions(),'inputvalue_hierarchy');
    }
    if(document.getElementById('inputvalue_facet')!=null){
        
        var oTextbox_f = new AutoSuggestControl(document.getElementById('inputvalue_facet'), new RemoteStateSuggestions(),'inputvalue_facet');
    }
    if(document.getElementById('inputvalue_source')!=null){
        
        var oTextbox_s = new AutoSuggestControl(document.getElementById('inputvalue_source'), new RemoteStateSuggestions(),'inputvalue_source');
    }
}
var aSuggestions='';

/**
 * Provides suggestions for state names (USA).
 * @class
 * @scope public
 */
function RemoteStateSuggestions() {
    if (typeof XMLHttpRequest != "undefined") { // Firefox, Opera 8.0+, Safari
        this.http = new XMLHttpRequest();
    } else if (typeof ActiveXObject != "undefined") { // Internet Explorer 
        this.http = new ActiveXObject("MSXML2.XmlHttp");
    } else {
        alert("No XMLHttpRequest object available. This functionality will not work.");
    }
    
}
var TypeAheadOpts = [];
/**
 * Request suggestions for the given autosuggest control. 
 * @scope protected
 * @param oAutoSuggestControl The autosuggest control to provide suggestions for.
 */
RemoteStateSuggestions.prototype.requestSuggestions = function (oAutoSuggestControl /*:AutoSuggestControl*/,bTypeAhead /*:boolean*/) {
    //var oHttp = this.http;
    
    var sTextboxValue = oAutoSuggestControl.textbox.value;  
    // alert("sTextboxValue = " + sTextboxValue + " inputType = " + oAutoSuggestControl.inputField)
    
    // karam: disable the type-ahead mechanism depending on current PEDIO (BEGIN)
    // ----------------------------------------------------------------------------
    var inputTypeForURL = oAutoSuggestControl.inputField;
    
    // 1. case of inputvalue_term
    if (oAutoSuggestControl.inputField == "inputvalue_term") {
        var PedioDropDowns = document.getElementsByName("input_term");
        var TelesthsDropDowns = document.getElementsByName("op_term");
        var TimhInputs = document.getElementsByName("inputvalue_term");
        var currentPEDIO_value = "";
        for (var i=0; i < TimhInputs.length; i++) {
            if (TimhInputs[i] == oAutoSuggestControl.textbox) {
                currentPEDIO_value = PedioDropDowns[i].value;
                currentTELESTHS_value = TelesthsDropDowns[i].value;
                break;
            }
        }   
        // cancel type-ahead mechanism in case of selected operator different than =, !, >=, <=
        if (currentTELESTHS_value != "=" && currentTELESTHS_value != "!" && currentTELESTHS_value != ">=" && currentTELESTHS_value != "<=") {
            return;
        }      
        // cancel type-ahead mechanism in case of selected pedio 
        //  "scope_note" / "translations_scope_note" / "historical_note"
        if (currentPEDIO_value == "scope_note" || currentPEDIO_value == "translations_scope_note" || currentPEDIO_value == "historical_note") {
            return;
        }
        if(currentPEDIO_value == "translations"){
            inputTypeForURL = "inputvalue_Translations";
        }
        if (currentPEDIO_value == "uf_translations" /*|| currentPEDIO_value == "uk_alt"*/) {
            inputTypeForURL = "inputvalue_UF_Translations";
        }      
        if (currentPEDIO_value == "uf") {
            inputTypeForURL = "inputvalue_UF";
        }            
        if (currentPEDIO_value == "dn") {
            inputTypeForURL = "inputvalue_DeweyNumber";
        }    
        if (currentPEDIO_value == "tc") {
            inputTypeForURL = "inputvalue_TaxonomicCode";
        } 
        if (currentPEDIO_value == "alt") {
            inputTypeForURL = "inputvalue_AlternativeTerm";
        }                        
        if (currentPEDIO_value == "primary_found_in" || currentPEDIO_value == "translations_found_in") {
            inputTypeForURL = "inputvalue_Source";
        }                              
        if (currentPEDIO_value == "facet") {
            inputTypeForURL = "inputvalue_facet";
        }                              
        if (currentPEDIO_value == "topterm") {
            inputTypeForURL = "inputvalue_TopTerm";
        }                                    
        if (currentPEDIO_value == "created_by" || currentPEDIO_value == "modified_by") {
            inputTypeForURL = "inputvalue_Editor";
        }  
        if (currentPEDIO_value == "created_on" || currentPEDIO_value == "modified_on") {
            inputTypeForURL = "inputvalue_Date";
        }                  
        if (currentPEDIO_value == "status" ) {
            inputTypeForURL = "inputvalue_Status";
        }   
    }
    // 2. case of inputvalue_hierarchy
    if (oAutoSuggestControl.inputField == "inputvalue_hierarchy") {
        var PedioDropDowns = document.getElementsByName("input_hierarchy");
        var TelesthsDropDowns = document.getElementsByName("op_hierarchy");
        var TimhInputs = document.getElementsByName("inputvalue_hierarchy");
        var currentPEDIO_value = "";
        for (var i=0; i < TimhInputs.length; i++) {
            if (TimhInputs[i] == oAutoSuggestControl.textbox) {
                currentPEDIO_value = PedioDropDowns[i].value;
                currentTELESTHS_value = TelesthsDropDowns[i].value;
                break;
            }
        }   
        // cancel type-ahead mechanism in case of selected operator different than =, !=
        if (currentTELESTHS_value != "=" && currentTELESTHS_value != "!") {
            return;
        }      
        /*
      // cancel type-ahead mechanism in case of selected pedio different than "name"
      if (currentPEDIO_value != "name") {
        return;
      }
         */
        if (currentPEDIO_value == "term") {
            inputTypeForURL = "inputvalue_term";
        }      
    }    
    // 3. case of inputvalue_facet
    if (oAutoSuggestControl.inputField == "inputvalue_facet") {
        var PedioDropDowns = document.getElementsByName("input_facet");
        var TelesthsDropDowns = document.getElementsByName("op_facet");
        var TimhInputs = document.getElementsByName("inputvalue_facet");
        var currentPEDIO_value = "";
        for (var i=0; i < TimhInputs.length; i++) {
            if (TimhInputs[i] == oAutoSuggestControl.textbox) {
                currentPEDIO_value = PedioDropDowns[i].value;
                currentTELESTHS_value = TelesthsDropDowns[i].value;
                break;
            }
        }   
        // cancel type-ahead mechanism in case of selected operator different than =, !=
        if (currentTELESTHS_value != "=" && currentTELESTHS_value != "!") {
            return;
        }      
        /*
      // cancel type-ahead mechanism in case of selected pedio different than "name"
      if (currentPEDIO_value != "name") {
        return;
      }
         */
        if (currentPEDIO_value == "term") {
            inputTypeForURL = "inputvalue_term";
        }            
    }        
      // 4. case of inputvalue_source
    if (oAutoSuggestControl.inputField == "inputvalue_source") {
        var PedioDropDowns = document.getElementsByName("input_source");
        var TelesthsDropDowns = document.getElementsByName("op_source");
        var TimhInputs = document.getElementsByName("inputvalue_source");
        var currentPEDIO_value = "";
        for (var i=0; i < TimhInputs.length; i++) {
            if (TimhInputs[i] == oAutoSuggestControl.textbox) {
                currentPEDIO_value = PedioDropDowns[i].value;
                currentTELESTHS_value = TelesthsDropDowns[i].value;
                break;
            }
        }   
        // cancel type-ahead mechanism in case of selected operator different than =, !=
        if (currentTELESTHS_value !== "=" && currentTELESTHS_value !== "!") {
            return;
        }      
        /*
      // cancel type-ahead mechanism in case of selected pedio different than "name"
      if (currentPEDIO_value != "name") {
        return;
      }
         */
        
        if (currentPEDIO_value === "primary_found_in" || currentPEDIO_value === "translations_found_in") {
            inputTypeForURL = "inputvalue_term";
        }          
        if (currentPEDIO_value === "source_note" ) {
             return;
            //inputTypeForURL = "inputvalue_source_note";
        }
    }        
    // karam: disable the type-ahead mechanism depending on current PEDIO (END)
    // ----------------------------------------------------------------------------    
    
    
    $.ajax({ 
            method: "POST",
            url: "nicajax",
            data: { inputvalue: sTextboxValue, inputType: inputTypeForURL } ,
            beforeSend: function() {
                    if(sTextboxValue.length===1){
                       DisplayPleaseWaitScreen(true);
                    }
                }
            }).done(function( msg ) {
                //alert( "Data Saved: " + msg );
                
                var neos = [];
                if(sTextboxValue.length===1) {
                    TypeAheadOpts = [];
                    //$xmlDoc = $.parseXML( msg ),
                    ///$xml = $( xmlDoc ),
                    $options = $(msg).find( "option" ).each(function () {

                        TypeAheadOpts.push($(this).text());
                    });
                }
                
                var neos = [];
                if (sTextboxValue.length >1){ 

                    for (var i=0; i < TypeAheadOpts.length-1; i++) { 

                        var len = sTextboxValue.length;
                        var str = TypeAheadOpts[i]; 
                        var  a   = str.substr(0,len);

                        if (a===sTextboxValue){			
                            neos.push(TypeAheadOpts[i]);
                        }
                    } 
                    oAutoSuggestControl.autosuggest(neos, bTypeAhead);
                }

                if(sTextboxValue.length===1){
                    oAutoSuggestControl.autosuggest(TypeAheadOpts, bTypeAhead);
                }
                
                
                
            }).always(function() {
                //if(sTextboxValue.length===1){
                   DisplayPleaseWaitScreen(false);
                //}
              });

    /* Previous Code 
    //if there is already a live request, cancel it
    if (oHttp.readyState != 0) {
        oHttp.abort();
    }                 
    
    //build the URL
    //var sURL = "nicajax?inputvalue="+(sTextboxValue) + "&inputType=" + (oAutoSuggestControl.inputField);
    //Hlias
    var params = "inputvalue="+escape(encodeURIComponent(sTextboxValue)) + "&inputType=" + escape(encodeURIComponent(inputTypeForURL));
    
    //open connection to states.txt file
    oHttp.open("post", 'nicajax' , true);
    
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    oHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
    oHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded"); 
    oHttp.setRequestHeader("Content-length", params.length); 
    oHttp.setRequestHeader("Connection", "close");     
    oHttp.send(params);
    oHttp.onreadystatechange = function () {
        
        if (oHttp.readyState == 4 ) 
        {
            //evaluate the returned text Javacript (an array)
            var Options = [];
            
            if(sTextboxValue.length==1) {
                aSuggestions =oHttp.responseText;
            }
            
            Options=aSuggestions.split("###");
            
            var neos = [];
            if (sTextboxValue.length >1){ 
                
                for (var i=0; i < Options.length-1; i++) { 
                
                    var len = sTextboxValue.length;
                    var str = Options[i]; 
                    var  a   = str.substr(0,len);
                    
                    if (a==sTextboxValue){			
                        neos.push(Options[i]);
                    }
                } 
		oAutoSuggestControl.autosuggest(neos, bTypeAhead);
            }
            
            if(sTextboxValue.length==1){
		oAutoSuggestControl.autosuggest(Options, bTypeAhead);
            }
        }
        
        
    };
    */
	
    


};
/***********TYPE ahead functionality*////////////////



/****AUTOSUGGEST****//////////////////
/**
 * An autosuggest textbox control.
 * @class
 * @scope public
 */
function AutoSuggestControl(oTextbox /*:HTMLInputElement*/,  oProvider /*:SuggestionProvider*/ , targetInput /*String*/) {
    
    /**
     * The currently selected suggestions.
     * @scope private
     */   
    this.cur /*:int*/ = -1;
    this.inputField = targetInput;

    /**
     * The dropdown list layer.
     * @scope private
     */
    this.layer = null;
    
    /**
     * Suggestion provider for the autosuggest feature.
     * @scope private.
     */
    this.provider /*:SuggestionProvider*/ = oProvider;
    
    /**
     * The textbox to capture.
     * @scope private
     */
    this.textbox /*:HTMLInputElement*/ = oTextbox;
    
    //initialize the control
    this.init();
    
}

/**
 * Autosuggests one or more suggestions for what the user has typed.
 * If no suggestions are passed in, then no autosuggest occurs.
 * @scope private
 * @param aSuggestions An array of suggestion strings.
 * @param bTypeAhead If the control should provide a type ahead suggestion.
 */
AutoSuggestControl.prototype.autosuggest = function (aSuggestions /*:Array*/,bTypeAhead /*:boolean*/  ) {    
    //make sure there's at least one suggestion
    if (aSuggestions.length > 0) {
        if (bTypeAhead) {
            this.typeAhead(aSuggestions[0]);
        }
        
        this.showSuggestions(aSuggestions);
    } else {
        this.hideSuggestions();
    }
};

/**
 * Creates the dropdown layer to display multiple suggestions.
 * @scope private
 */
AutoSuggestControl.prototype.createDropDown = function () {

    var oThis = this;

    //create the layer and assign styles
    this.layer = document.createElement("div");
    this.layer.className = "suggestions";
    this.layer.style.visibility = "hidden";
    this.layer.style.width = '300px';//this.textbox.offsetWidth * 2;
    
    //when the user clicks on the a suggestion, get the text (innerHTML)
    //and place it into a textbox
    this.layer.onmousedown = 
        this.layer.onmouseup = 
        this.layer.onmouseover = function (oEvent) {
        oEvent = oEvent || window.event;
        oTarget = oEvent.target || oEvent.srcElement;

        if (oEvent.type == "mousedown") {
            // karam
            var nodeValue = oTarget.firstChild.nodeValue;
            if (nodeValue != null) {
                oThis.textbox.value = nodeValue;
                oThis.hideSuggestions();
            }
        } else if (oEvent.type == "mouseover") {
            oThis.highlightSuggestion(oTarget);
        } else {
            oThis.textbox.focus();
        }
    };

    // when dropdown loses focus, hide suggestions
    this.layer.onblur = function (oEvent) {
        oThis.hideSuggestions();
    };
    
    
    document.body.appendChild(this.layer);
};

/**
 * Gets the left coordinate of the textbox.
 * @scope private
 * @return The left coordinate of the textbox in pixels.
 */
AutoSuggestControl.prototype.getLeft = function () /*:int*/ {
    var oNode = this.textbox;
    var iLeft = 4;
    
    while(oNode.tagName != "BODY") {
        iLeft += oNode.offsetLeft;
        oNode = oNode.offsetParent;        
    }
    
    return iLeft;
};

/**
 * Gets the top coordinate of the textbox.
 * @scope private
 * @return The top coordinate of the textbox in pixels.
 */
AutoSuggestControl.prototype.getTop = function () /*:int*/ {
    var oNode = this.textbox;
    var iTop = 3;
    
    while(oNode.tagName != "BODY") {
        iTop += oNode.offsetTop;
        oNode = oNode.offsetParent;
    }
    
    return iTop;
};

/**
 * Handles three keydown events.
 * @scope private
 * @param oEvent The event object for the keydown event.
 */
AutoSuggestControl.prototype.handleKeyDown = function (oEvent /*:Event*/) {
    switch(oEvent.keyCode) {
        case 38: //up arrow
            this.previousSuggestion();
            break;
        case 40: //down arrow 
            this.nextSuggestion();
            break;
        case 13: //enter
            this.hideSuggestions();
            break;
    }

};

/**
 * Handles keyup events.
 * @scope private
 * @param oEvent The event object for the keyup event.
 */
AutoSuggestControl.prototype.handleKeyUp = function (oEvent /*:Event*/) {
    var iKeyCode = oEvent.keyCode;

    //for backspace (8) and delete (46), shows suggestions without typeahead
    if (iKeyCode == 8 || iKeyCode == 46) {
        this.provider.requestSuggestions(this, false);
        
        //make sure not to interfere with non-character keys
    } else if (iKeyCode < 32 || (iKeyCode >= 33 && iKeyCode < 46) || (iKeyCode >= 112 && iKeyCode <= 123)) {
        //ignore
    } else {
        //request suggestions from the suggestion provider with typeahead
        this.provider.requestSuggestions(this, true);
    }
};

/**
 * Hides the suggestion dropdown.
 * @scope private
 */
AutoSuggestControl.prototype.hideSuggestions = function () {
    this.layer.style.visibility = "hidden";

    // karam: Tooltips and other layers appear behind select lists in Internet Explorer for Windows
    // BUG FIX: display the previously hidden "Extra Output" select list
    // document.getElementById('outputSel').style.visibility = "visible";  
    // NOT needed for IE7+
};

/**
 * Highlights the given node in the suggestions dropdown.
 * @scope private
 * @param oSuggestionNode The node representing a suggestion in the dropdown.
 */
AutoSuggestControl.prototype.highlightSuggestion = function (oSuggestionNode) {
    for (var i=0; i < this.layer.childNodes.length; i++) {
        var oNode = this.layer.childNodes[i];
        if (oNode == oSuggestionNode) {
            oNode.className = "current"
        } else if (oNode.className == "current") {
            oNode.className = "";
        }
    }
};

/**
 * Initializes the textbox with event handlers for
 * auto suggest functionality.
 * @scope private
 */
AutoSuggestControl.prototype.init = function () {
    //save a reference to this object
    var oThis = this;
    
    //assign the onkeyup event handler
    this.textbox.onkeyup = function (oEvent) {
    
        //check for the proper location of the event object
        if (!oEvent) {
            oEvent = window.event;
        }    
        
        //call the handleKeyUp() method with the event object
        oThis.handleKeyUp(oEvent);
    };

    //assign onkeydown event handler
    this.textbox.onkeydown = function (oEvent) {
    
        //check for the proper location of the event object
        if (!oEvent) {
            oEvent = window.event;
        }    
        
        //call the handleKeyDown() method with the event object
        oThis.handleKeyDown(oEvent);
    };
    
    //assign onblur event handler (hides suggestions)    
    this.textbox.onblur = function () {
        // changed by karam because when clicking on vertical scrollbar, the suggestions were hiden!
        if (document.activeElement.className != 'suggestions') {
            oThis.hideSuggestions();
        }
    };
    
    //create the suggestions dropdown
    this.createDropDown();
};

/**
 * Highlights the next suggestion in the dropdown and
 * places the suggestion into the textbox.
 * @scope private
 */
AutoSuggestControl.prototype.nextSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;

    if (cSuggestionNodes.length > 0 && this.cur < cSuggestionNodes.length-1) {
        var oNode = cSuggestionNodes[++this.cur];
        this.highlightSuggestion(oNode);
        this.textbox.value = oNode.firstChild.nodeValue; 
    }
};

/**
 * Highlights the previous suggestion in the dropdown and
 * places the suggestion into the textbox.
 * @scope private
 */
AutoSuggestControl.prototype.previousSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;

    if (cSuggestionNodes.length > 0 && this.cur > 0) {
        var oNode = cSuggestionNodes[--this.cur];
        this.highlightSuggestion(oNode);
        this.textbox.value = oNode.firstChild.nodeValue;   
    }
};

/**
 * Selects a range of text in the textbox.
 * @scope public
 * @param iStart The start index (base 0) of the selection.
 * @param iLength The number of characters to select.
 */
AutoSuggestControl.prototype.selectRange = function (iStart /*:int*/, iLength /*:int*/) {
    //use text ranges for Internet Explorer
    if (this.textbox.createTextRange) {
        var oRange = this.textbox.createTextRange(); 
        oRange.moveStart("character", iStart); 
        oRange.moveEnd("character", iLength - this.textbox.value.length);      
        oRange.select();
        
        //use setSelectionRange() for Mozilla
    } else if (this.textbox.setSelectionRange) {
        this.textbox.setSelectionRange(iStart, iLength);
    }     

    //set focus back to the textbox
    this.textbox.focus();      
}; 

/**
 * Builds the suggestion layer contents, moves it into position,
 * and displays the layer.
 * @scope private
 * @param aSuggestions An array of suggestions for the control.
 */
AutoSuggestControl.prototype.showSuggestions = function (aSuggestions /*:Array*/) {
    
    var oDiv = null;
    this.layer.innerHTML = "";  //clear contents of the layer
    
    for (var i=0; i < aSuggestions.length; i++) {
        oDiv = document.createElement("div");
        oDiv.appendChild(document.createTextNode(aSuggestions[i]));
        this.layer.appendChild(oDiv);
    }
    
    this.layer.style.left = this.getLeft() + "px";
    this.layer.style.top = (this.getTop()+this.textbox.offsetHeight) + "px";
    this.layer.style.visibility = "visible";

    // karam: Tooltips and other layers appear behind select lists in Internet Explorer for Windows
    // BUG FIX: hide the "Extra Output" select list
    // document.getElementById('outputSel').style.visibility = "hidden";
    // NOT needed for IE7+
};

/**
 * Inserts a suggestion into the textbox, highlighting the 
 * suggested part of the text.
 * @scope private
 * @param sSuggestion The suggestion for the textbox.
 */
AutoSuggestControl.prototype.typeAhead = function (sSuggestion /*:String*/) {     
    //check for support of typeahead functionality
    if (this.textbox.createTextRange || this.textbox.setSelectionRange){
        var iLen = this.textbox.value.length; 
        this.textbox.value = sSuggestion; 
        this.selectRange(iLen, sSuggestion.length);
    }
};

/****AUTOSUGGEST****//////////////////