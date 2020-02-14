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

var SVGDocument = null;
      var SVGRoot = null;
      var TrueCoords = null;
      var GrabPoint = null;
      var BackDrop = null;
      var DragTarget = null;
      var Dragging = false;
      var SearchArea = null;
      var RawData = null;
      var previewSize = null;      
      var theDraggerWidth = 0;
      var thedraggerHeight = 0;
      var scaleFactor = 0.1; //default --updated on 17-03-2009

      
       
function followlink(){
    
}
      function Init(evt)
      {
         SVGDocument = evt.target.ownerDocument;
	 SVGRoot = SVGDocument.documentElement;
         // these svg points hold x and y values...
         //    very handy, but they do not display on the screen (just so you know)
	 TrueCoords = SVGRoot.createSVGPoint();
	 GrabPoint = SVGRoot.createSVGPoint();
         // this will serve as the canvas over which items are dragged.
         //    having the drag events occur on the mousemove over a backdrop
         //    (instead of the dragged element) prevents the dragged element
         //    from being inadvertantly dropped when the mouse is moved rapidly
	 BackDrop = SVGDocument.getElementById("group2");
	 DragTarget = SVGDocument.getElementById("group3");
	 SearchArea = SVGDocument.getElementById("group1");


	 // --updated on 17-03-2009
	 scaleFactor = getParentContainerHeight()/SearchArea.getBBox().height;
	 if (scaleFactor > 0.1)
	 {
		scaleFactor = 0.1;
	 }
	 BackDrop.setAttribute("transform","translate(0 0) scale("+scaleFactor+")");
	 DragTarget.setAttribute("transform","translate(0 0) scale("+scaleFactor+")")

         extractTermsfromSVG();

         //Create a minature of the Preview Panel
         createPreviewPanel();

         
         //Resize the frame that contains the preview panel in order to fit its size
         resizeContainingFrame();

         resizeDraggable();

	// --updated on 17-03-2009
	if (theDraggerWidth>0 && thedraggerHeight>0)
	{
	    set_PreviewPanel_functions();//call after resizing dragger
	}

         // addEventListeners();
         //parent.createAutoComplete();

      }

      
      function createPreviewPanel()
      {
         var previewThumbNail = SearchArea.cloneNode(true);
         //previewThumbNail.setAttribute ("x","20px");
         //previewThumbNail.setAttribute ("y","20px");

         BackDrop.appendChild(previewThumbNail);

      }

      // --updated on 17-03-2009
      function set_PreviewPanel_functions()
      {
	 var attr1 = SVGDocument.createAttribute("onmousedown");
	 var attr2 = SVGDocument.createAttribute("onmouseup");

	 attr1.value="setGrab(evt);Drag(evt)";
	 attr2.value="Drop(evt)";

	 BackDrop.setAttributeNode(attr1);
	 BackDrop.setAttributeNode(attr2);
      }


      function resizeContainingFrame()
      {
         previewSize = BackDrop.getBBox();
         var containigFrame = BackDrop.getElementsByTagName("rect").item(0);
         containigFrame.setAttribute ('width',previewSize.width+50);
         containigFrame.setAttribute ('height',previewSize.height+50);

      }


      // --updated on 17-03-2009
      function getParentContainerHeight()
      {
	/*
	var imageLayerId = activeFormElement.imglayer;
	var parentContainer = parent.document.getElementById(imageLayerId);
	return parentContainer.offsetHeight;
	*/
	var draggerHeight = 500;
	return draggerHeight;
 
      }


      function resizeDraggable()
      {
         // var parentContainer = parent.document.getElementById("photolayer");
         
         // var draggerWidth = parseInt(parentContainer.style.width);
         // var draggerHeight = parseInt(parentContainer.style.height);
         var draggerWidth = 500;
         var draggerHeight = 500;
          
         var previewWidth = previewSize.width;
         var previewHeight = previewSize.height;

         var dragTgtDimensions = DragTarget.getElementsByTagName("rect").item(0);
          


         if( (draggerWidth <= previewWidth) == true &&  (draggerHeight <= previewHeight) == true )
         {
         dragTgtDimensions.setAttribute("width",draggerWidth);
         dragTgtDimensions.setAttribute("height",draggerHeight);
         theDraggerWidth = draggerWidth;
         thedraggerHeight = draggerHeight;
      
         }

         else if( (draggerWidth <= previewWidth) == false &&  (draggerHeight <= previewHeight) == true )
         {
         dragTgtDimensions.setAttribute("width",previewWidth);
         dragTgtDimensions.setAttribute("height",draggerHeight);

         theDraggerWidth = previewWidth;
         thedraggerHeight = draggerHeight;
         }

         else if( (draggerWidth <= previewWidth) == true &&  (draggerHeight <= previewHeight) == false )
         {
         dragTgtDimensions.setAttribute("width",draggerWidth);
         dragTgtDimensions.setAttribute("height",previewHeight);

         theDraggerWidth = draggerWidth;
         thedraggerHeight = previewHeight;
         }

         else if( (draggerWidth <= previewWidth) == false &&  (draggerHeight <= previewHeight) == false )
         {
         dragTgtDimensions.setAttribute("width","0px");
         dragTgtDimensions.setAttribute("height","0px");
         }
         
      }


      function addEventListeners()
      {
        gelements = SearchArea.getElementsByTagName("g");

        for(i=0; i<gelements.length; i++)
        {
           //alert(gelements.item(i).childNodes.length)
           if(gelements.item(i).childNodes.length > 3)
           {
     
            gelements.item(i).addEventListener("mousedown",getThesaurusText, false);
            gelements.item(i).addEventListener("mouseover",onSelected, false);
            gelements.item(i).addEventListener("mouseout",onUnSelected, false);
           }
        }


      }


function getThesaurusText(evt)
{
  evt.preventDefault();
  var SVGNode = evt.getCurrentNode();
  var text = SVGNode.getElementsByTagName("text").item(0).firstChild.getData();

  parent.getTermFromSVG(text);
  //alert(text);
} 



function extractTermsfromSVG()
    {
      var theTerms = new Array();	    

      var theNodes = SVGRoot.getElementsByTagName("text");
      var buffString;
      
      
      for(i=0;i<theNodes.length;i++)
      {
          if(theNodes.item(i).firstChild.getData){
              buffString = theNodes.item(i).firstChild.getData();
              theTerms[i] = buffString;
          }
        
      }
        theTerms.sort();
      

         //setTimeout("parent.setTermsarray(theTerms)",1250);
        
        //parent.setTermsarray(theTerms)

	
        //parent.svgTermsAsArray = theTerms;

    }



function onSelected(evt)
{
  evt.preventDefault();
  var SVGNode = evt.getCurrentNode();
  var rect = SVGNode.getElementsByTagName("rect").item(0);

  var style = rect.getStyle();
  style.setProperty('fill','red');

}

function onUnSelected(evt)
{
  evt.preventDefault();
  var SVGNode = evt.getCurrentNode();
  var rect = SVGNode.getElementsByTagName("rect").item(0);

  var style = rect.getStyle();
  style.setProperty('fill','#E9E9C6');
}

      function AutoFocus(evt)
      {
         var Clickable = evt.target;         
         var SVGDoc = evt.target.ownerDocument;
	 var SVGRt = SVGDoc.documentElement;
         var Zoomable = SVGRt.getElementById("group1");

         Zoomable.getAttribute("transform");
  

         var parent = evt.getCurrentNode();
 
         var itemlist = parent.getElementsByTagName("rect"); 
               
         for(i=0; i<itemlist.length; i++)
         {   
         var style = itemlist.item(i).getStyle();
         style.setProperty('fill','blue');

         var x = itemlist.item(i).getAttribute ('x') * 0.1 + 10;
         var y = itemlist.item(i).getAttribute ('y') * 0.1 + 10;
         var w = itemlist.item(i).getAttribute ('width');
         var h = itemlist.item(i).getAttribute ('height');
         }

         var focusat = "translate(-" + x + ",-" + y + ")";

         Zoomable.setAttribute("transform",focusat);
         
      }

       // --updated on 17-03-2009
       function setGrab(evt)
       {
	GetTrueCoords(evt);
	//Set GrabPoint in the middle of dragger
	GrabPoint.x = theDraggerWidth*0.5*scaleFactor;
	GrabPoint.y = thedraggerHeight*0.5*scaleFactor;  
	
	Dragging = true
      }

 function Grab(evt)
 {
         
	 evt.preventDefault(); //for FireFox
	 evt.returnValue=false; //for IE

         // find out which element we moused down on
	 //var targetElement = evt.target;
         // you cannot drag the background itself, so ignore any attempts to mouse down on it
	 if (BackDrop)
	 {

            // we need to find the current position and translation of the grabbed element,
            //    so that we only apply the differential between the current location
	    //    and the new location
	    
	    var transMatrix = DragTarget.getCTM();
	    
	    GrabPoint.x = TrueCoords.x - Number(transMatrix.e);
	    GrabPoint.y = TrueCoords.y - Number(transMatrix.f);

            Dragging = true;

	    }

}



      function Drag(evt)
      {
	 GetTrueCoords(evt);

	// if we don't currently have an element in tow, don't do anything
	if (Dragging == false) {return;}


	 evt.preventDefault(); //for FireFox
	 evt.returnValue=false; //for IE


	// account for zooming and panning
           
            var curCTM = DragTarget.getCTM();
            var curCTM2 = SearchArea.getCTM();


            var oldX = curCTM.e;
            var oldY = curCTM.f;


            var oldX2 = curCTM2.e;
            var oldY2 = curCTM2.f;

            //alert(oldX+" "+oldY);          

            // account for the offset between the element's origin and the
            //    exact place we grabbed it... this way, the drag will look more natural
            var newX = TrueCoords.x - GrabPoint.x;
	    var newY = TrueCoords.y - GrabPoint.y;

	    var newX2 = (GrabPoint.x - TrueCoords.x)/scaleFactor;// --updated on 17-03-2009
	    var newY2 = (GrabPoint.y - TrueCoords.y)/scaleFactor;// --updated on 17-03-2009

            //alert(previewSize.width + " sds" + theDraggerWidth)

            if( newX < 0)
            {
                var newX = 0;
                var newX2 = 0;
            }

            if( newX > ((previewSize.width - theDraggerWidth + 30)*scaleFactor))// --updated on 17-03-2009
            {
                newX = (previewSize.width - theDraggerWidth + 30)*scaleFactor;// --updated on 17-03-2009
                newX2 = -(previewSize.width - theDraggerWidth + 30);
            }

            if( newY > (previewSize.height - thedraggerHeight + 30)*scaleFactor )// --updated on 17-03-2009
            {
                 newY = (previewSize.height - thedraggerHeight + 30)*scaleFactor;// --updated on 17-03-2009
                 newY2 = -(previewSize.height - thedraggerHeight + 30);
            }

            if( newY < 0)
            {
                 
                 var newY = 0;
                 var newY2 = 0;
            }	    

            // apply a new tranform translation to the dragged element, to display
	    // it in its new location

            
	    DragTarget.setAttributeNS(null, 'transform', 'translate(' + newX + ',' + newY + ')' + ',scale('+scaleFactor+')');// --updated on 17-03-2009
	    SearchArea.setAttributeNS(null, 'transform', 'translate(' + newX2 + ',' + newY2 + ')' + ',scale(1)');
	    
         
	}


     
      function Drop(evt)
      {
         //evt.preventDefault();
      // if we aren't currently dragging an element, don't do anything

	 if ( Dragging != false )
         {
	   Dragging = false;
         }
      }



      function GetTrueCoords(evt)
      {
         // evt.preventDefault();
         // find the current zoom level and pan setting, and adjust the reported
         //    mouse position accordingly
	 var newScale = SVGRoot.currentScale;

         //var newScale = 0.1;
	 var translation = SVGRoot.currentTranslate;

         TrueCoords.x = (evt.clientX - translation.x)/newScale;
         TrueCoords.y = (evt.clientY - translation.y)/newScale;
     }




