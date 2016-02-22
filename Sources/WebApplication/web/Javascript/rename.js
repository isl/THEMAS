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
/*****************************Start Ajax Rename***********************************/

/*-----------------------------------------------------
                    keyEvent()
-------------------------------------------------------*/      
function keyEvent(event) {
    switch(event.keyCode) {
        case 13:
            var newname = document.getElementById('newname').value;
            var oldname =document.getElementById('oldd').value;
            var NewInputname =document.getElementById('Newname').value;
            if (newname) {
                var RenameUrl ='RenameCheck_Term?newname='+ escape(encodeURIComponent(newname))+"&oldname="+escape(encodeURIComponent(oldname));
                makeHttpRequest(RenameUrl);	      
            }
            break;    
    }
}

// ------------------ GLOBAL variables ------------------
var terms =[];
var termss =[];
var tempterms=[];
var inputerms;
var temp;
var o;
var oo;
var flag=0;
var jId=0;
var templink;
var dinamo;
var secdinamo;
var thirddinamo;
var lastFieldValue;
var renameURL
var RenamexmlHttp;
var enablerename=0;
var gotorename=0;

var RenameChain = [];
var RenameId=0;
var fl2=0;
var fl3=0;

/*-----------------------------------------------------
                    makeHttpRequest()
-------------------------------------------------------*/      
function makeHttpRequest(RenameUrl) {
    var RenamexmlHttp;
    try
    {
        // Firefox, Opera 8.0+, Safari
        RenamexmlHttp=new XMLHttpRequest();
    }
    catch (e)
    {
        // Internet Explorer
        try
        {
            RenamexmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e)
        {
            try
            {
                RenamexmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e)
            {
                alert("Your browser does not support AJAX!");
                return false;
            }
        }
    }
  
    RenamexmlHttp.onreadystatechange=function() 
    {
	
	var response;
	
        if(RenamexmlHttp.readyState==4)
        {	
	
            //pernw to response apo to server. 
            //the respose informs the client of what is the term 
            //so that could manipulate the data

            var txt = RenamexmlHttp.responseText;

            response = txt.replace(/^\s+|\s+$/g,'');
            //for NewDescriptor
            if(response == 'newDescriptor')
            {
                //alert('NewDescriptor');
                var inputField2 = (document.getElementById('newname').value).replace(/^\s+|\s+$/g,'');
                //alert(' len '+inputField2.length);
                if(inputField2  == '')
		{
                    //alert('enter name');
                    alert(translate(21));
                     //alert('Παρακαλώ εισάγετε ένα νεό όνομα.');
		}
                else{
                    var mybutton = document.getElementById('rename');
                    mybutton.disabled = false;
		}		
            }
            if(response == 'Term exist in the database')
            {
                //alert(response);
                alert(translate(22));
                //alert('Ο όρος υπάρχει στη βάση δεδομένων.');
                //clear the input field
                var inputField2 = document.getElementById('newname');
                inputField2.value='';
		
            }
            if(response == 'Old Not Found')
            {
                var input1 = document.getElementById('oldd');
                //alert(response);
                alert(translate(23)+input1.value+translate(24));
                //alert('Ο όρος ' + input1.value + ' δεν βρέθηκε στην βαση δεδομένων.');
                //clear the input field
                var inputField2 = document.getElementById('newname');
                inputField2.value='';
		
            }
            if(response == 'Top Term')
            {
                //alert(response);
                alert(translate(25));
                //alert('Ο όρος αποτελεί Όρο Κορυφής και το όνομα του μπορεί \nνα αλλάχθει μόνο μέσω μετονομασίας της σχετικής ιεραρχίας.');
                //clear the input field
                var inputField2 = document.getElementById('newname');
                inputField2.value='';
		
            }
            
            //End for NewDescriptor

            //alert(response);
            if (response != 'ok' && response != 'sorry' && response !='newDescriptor' && response != 'Top Term'
                && response != 'Term exist in the database' && response != 'Old Not Found'
                && response.indexOf('only with a cyclic rename' ) == -1  // karam bug fix (6/8/08)
                // �������� ��� ��� ��������� ��� cyclic rename ���� �� ������ ������ �� ��� �����������
                // ����� ��� ���� � ������� ������� ������������ ��������
                ) {
                //alert("Error Message: "+response+" Please Try again. ");
                alert(translate(26)+response+translate(27));
                //alert("Μήνυμα λάθους: "+response+" Παρακαλώ προσπαθήστε ξανά.");
            }


	




            //to fl ginetai 1 otan o oros iparxei sto RenameChain 
            //etsi apogorevetai i eisodos sto (1)
            var fl=0;


            var newname2 = document.getElementById('newname').value;
            var oldname2 = document.getElementById('oldd').value;

            if(oldname2==newname2 && fl2 <=2)
            {
		
		var Input = getObj('Newname');
		var newInput;
		var InputForServlet = getObj('NewnameForServlet');
		var newInputForServlet ;
		
                var mytable = document.getElementById('myTable');
                if(mytable.rows.length != 1)
                {
		

                    if(fl2==0)
                    {
	
                        fl2++;
                        //cyclic rename
			if(fl3==1){
                            //alert("Message: "+newname2+" cyclic rename VALID");
			
                            var oldname = document.getElementById('oldd').value;
	  
                            var telos = document.getElementById('newname')
                            final = telos.value;
			
                            var mytable = document.getElementById('myTable');

                            var mytbody = document.getElementById('myTbody');

                            var myNewtbody = document.createElement('tbody');
                            myNewtbody.id = 'myTbody';							
			   
                            //last row of table
                            var lastRow = mytable.rows[mytable.rows.length-1];
                            var doc = document.createDocumentFragment();

                            var mytd = document.createElement('td');
	

                            //gia to servlet
                            //gia na parw tis parametrous sto servlet 
                            //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                            //use sti RenameRealesed Concept 
                            //edw einai oi dilwseis 
                            var Servlettable = document.getElementById('myTable2');
                            var Servlettbody = document.getElementById('myTbody2');
                            var ServletNewtbody = document.createElement('tbody');
                            ServletNewtbody.id = 'myTbody2';
                            var ServletdocFragment = document.createDocumentFragment();					      	           	     var ServlettrElem;
                            var ServlettdfirstElem;
                            ServlettrElem =  document.createElement('tr');
                            ServlettdfirstElem =  document.createElement('td');
                            //gia to servlet
			
			 
                            newInput = Input.cloneNode(true);
                            newInputForServlet = InputForServlet.cloneNode(true);
			
	
                            newInput.setAttribute('value',final);
                            newInput.setAttribute('name','Newname');
                            newInput.setAttribute('disabled','disabled');

                            newInputForServlet.setAttribute('value',final);
                            newInputForServlet.setAttribute('name','NewnameForServlet');
                            newInputForServlet.setAttribute('disabled','');
		
                            mytd.appendChild(newInput,'value');	
                            inputerms = final;
                            lastRow.appendChild(mytd);
                            doc.appendChild(lastRow);					
				
                            myNewtbody.appendChild(doc);
			
                            mytable.appendChild(myNewtbody, mytbody);

                            ServlettdfirstElem.appendChild(newInputForServlet,'value');
                            ServlettrElem.appendChild(ServlettdfirstElem);
                            ServletdocFragment.appendChild(ServlettrElem);
                            ServletNewtbody.appendChild(ServletdocFragment);
                            Servlettable.appendChild(ServletNewtbody, Servlettbody);

                            var mybutton = document.getElementById('rename');
                            mybutton.disabled = false;

                            //clear the input field
                            var inputField2 = document.getElementById('newname');
                            inputField2.value='';

			
			}

                    }
                    else
                    {
                        //alert("Message: "+newname2+" cyclic rename");
                        var oldname = document.getElementById('oldd').value;
	  
                        var telos = document.getElementById('newname')
                        final = telos.value;
			
                        var mytable = document.getElementById('myTable');

                        var mytbody = document.getElementById('myTbody');

                        var myNewtbody = document.createElement('tbody');
                        myNewtbody.id = 'myTbody';

                        //last row of table
                        var lastRow = mytable.rows[mytable.rows.length-1];				
			   
                        var doc = document.createDocumentFragment();

                        var mytd = document.createElement('td');
	

                        //gia to servlet
                        //gia na parw tis parametrous sto servlet 
                        //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                        //use sti RenameRealesed Concept 
                        //edw einai oi dilwseis 
                        var Servlettable = document.getElementById('myTable2');
                        var Servlettbody = document.getElementById('myTbody2');
                        var ServletNewtbody = document.createElement('tbody');
                        ServletNewtbody.id = 'myTbody2';
                        var ServletdocFragment = document.createDocumentFragment();					      	           	     var ServlettrElem;
                        var ServlettdfirstElem;
                        ServlettrElem =  document.createElement('tr');
                        ServlettdfirstElem =  document.createElement('td');
                        //gia to servlet
			
			 
                        newInput = Input.cloneNode(true);
                        newInputForServlet = InputForServlet.cloneNode(true);

                        newInput.setAttribute('value',final);
                        newInput.setAttribute('name','Newname');
                        newInput.setAttribute('disabled','disabled');

                        newInputForServlet.setAttribute('value',final);
                        newInputForServlet.setAttribute('name','NewnameForServlet');
                        newInputForServlet.setAttribute('disabled','');
		
                        mytd.appendChild(newInput,'value');	
                        inputerms = final;
                        lastRow.appendChild(mytd);
                        doc.appendChild(lastRow);					
				
                        myNewtbody.appendChild(doc);
			
                        mytable.appendChild(myNewtbody, mytbody);

                        ServlettdfirstElem.appendChild(newInputForServlet,'value');
                        ServlettrElem.appendChild(ServlettdfirstElem);
                        ServletdocFragment.appendChild(ServlettrElem);
                        ServletNewtbody.appendChild(ServletdocFragment);
                        Servlettable.appendChild(ServletNewtbody, Servlettbody);

                        var mybutton = document.getElementById('rename');
                        mybutton.disabled = false;

                        //clear the input field
                        var inputField2 = document.getElementById('newname');
                        inputField2.value='';
			 

                    }
		 
                }
            }




            //checks if the newname exist in the rename chain
            for (var r=0; r < RenameChain.length; r++)
            {
	
                var newname2 = document.getElementById('newname').value;
	 
	
                var name = RenameChain[r];
                var name2 =name;
	
	
	 
		 

                if(name2==newname2)
	 	{
                    fl=1;
                    //alert("Error Message: "+newname2+" has already been entered in the rename chain  Please Try again. ");
                    alert(translate(28)+newname2+translate(29));
                    //alert("Μήνυμα λάθους: το όνομα "+newname2+" έχει ήδη εισαχθεί στην αλυσίδα των μετονομασιών. Παρακαλώ προσπαθήστε ξανά.");
		 
		}
 
            }
	
            // (1)  if the term exist in TMS database
            if(response == 'ok' && fl==0  )
            { 
                var oldname = document.getElementById('oldd').value;
                var newname = document.getElementById('newname').value;
 
                var Input = getObj('Newname');
                var newInput ;

                var InputForServlet = getObj('NewnameForServlet');
                var newInputForServlet ;


                //if it is the first user's input 
         	if(flag < 1)	
                { 
				
					
                    var tdsecondElem;
                    var mytable = document.getElementById('myTable');
                    var mytbody = document.getElementById('myTbody');
                    var myNewtbody = document.createElement('tbody');
                    myNewtbody.id = 'myTbody';

				

                    //gia na parw tis parametrous sto servlet 
                    //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                    //use sti RenameRealesed Concept 
                    //edw einai oi dilwseis 
                    var Servlettable = document.getElementById('myTable2');
                    var Servlettbody = document.getElementById('myTbody2');
                    var ServletNewtbody = document.createElement('tbody');
                    ServletNewtbody.id = 'myTbody2';
                    var ServletdocFragment = document.createDocumentFragment();					      	           	     var ServlettrElem;
                    var ServlettdfirstElem;

                    ServlettrElem =  document.createElement('tr');
                    ServlettdfirstElem =  document.createElement('td');

                    //gia to servlet


                    var docFragment = document.createDocumentFragment();					      	           		     var trElem;
                    var trsecondElem;	
                    var tdfirstElem;
                    var tdsecondElem;
                    var tdthirdElem;
                    var txtfirstNode;
                    var txtsecondNode;
                    var txtthirdNode;

                    trElem =  document.createElement('tr');
                    trsecondElem =  document.createElement('tr');
				
			
				
			
                    tdfirstElem =  document.createElement('td');
                    tdsecondElem =  document.createElement('td');
                    tdthirdElem =  document.createElement('td');

                    txtfirstNode = document.createTextNode(oldname);
                    txtsecondNode = document.createTextNode(newname);
                    txtthirdNode = document.createTextNode(newname);

                    firstinput = document.createElement('input');
			
                    newInput = Input.cloneNode(true);
                    newInputForServlet = InputForServlet.cloneNode(true);
	
		

                    thirdinput = document.createElement('input');
                    firstinput.setAttribute('type','text');
                    firstinput.setAttribute('id','oldname');
                    firstinput.setAttribute('value',oldname);
                    firstinput.setAttribute('disabled','disabled');
			
				
                    dinamo = oldname;

			
                    newInput.setAttribute('value',newname);
                    newInput.setAttribute('name','Newname');
                    newInput.setAttribute('disabled','disabled');
		
                    newInputForServlet.setAttribute('value',newname);
                    newInputForServlet.setAttribute('name','NewnameForServlet');
                    newInputForServlet.setAttribute('disabled','');
				
                    RenameChain[RenameId++]=newname;

                    dinamo = newname;
				
                    thirdinput.setAttribute('type','text');		
                    thirdinput.setAttribute('value',newname);		
                    thirdinput.setAttribute('disabled','disabled');
							
                    tdfirstElem.appendChild(firstinput,'value');
                    tdsecondElem.appendChild(newInput,'value');
                    tdthirdElem.appendChild(thirdinput,'value');

                    trElem.appendChild(tdfirstElem);
                    trElem.appendChild(tdsecondElem);
								
                    trsecondElem.appendChild(tdthirdElem);
				
                    docFragment.appendChild(trElem);
                    docFragment.appendChild(trsecondElem);
				
                    myNewtbody.appendChild(docFragment);
			
                    mytable.appendChild(myNewtbody, mytbody);
			
                    //edw einai ta append pou kanw	
                    ServlettdfirstElem.appendChild(newInputForServlet,'value');
                    ServlettrElem.appendChild(ServlettdfirstElem);
                    ServletdocFragment.appendChild(ServlettrElem);
                    ServletNewtbody.appendChild(ServletdocFragment);
                    Servlettable.appendChild(ServletNewtbody, Servlettbody);
                    //edw einai ta append pou kanw
				
			
                    templink = newname;
                    fl=0;

                    //clear the input field
                    var inputField2 = document.getElementById('newname');
                    inputField2.value='';					

                    //gia to cyclic rename
                    fl3=1;
                }
			
                //if it is not the first user's input 
                if(flag++ >= 1)
                {
                    var mytable = document.getElementById('myTable');
                    var mytbody = document.getElementById('myTbody');
                    var myNewtbody = document.createElement('tbody');
                    myNewtbody.id = 'myTbody';							
			     
                    var mysectr= document.createElement('tr');
                    var mytd = document.createElement('td');
                    var mythirdtd = document.createElement('td');
                    var doc = document.createDocumentFragment();	
                    var mytxtfirstNode;
                    var mytxtthirdNode;
	
                    //last row of table
                    var lastRow = mytable.rows[mytable.rows.length-1];

                    //gia to servlet
                    //gia na parw tis parametrous sto servlet 
                    //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                    //use sti RenameRealesed Concept 
                    //edw einai oi dilwseis 
                    var Servlettable = document.getElementById('myTable2');
                    var Servlettbody = document.getElementById('myTbody2');
                    var ServletNewtbody = document.createElement('tbody');
                    ServletNewtbody.id = 'myTbody2';
                    var ServletdocFragment = document.createDocumentFragment();					      	           	     				     var ServlettrElem;
                    var ServlettdfirstElem;
                    ServlettrElem =  document.createElement('tr');
                    ServlettdfirstElem =  document.createElement('td');
                    //gia to servlet
			

                    newInput = Input.cloneNode(true);
                    newInputForServlet = InputForServlet.cloneNode(true);
                    third3input = document.createElement('input');

                    newInput.setAttribute('value',newname);
                    newInput.setAttribute('name','Newname');
                    newInput.setAttribute('disabled','disabled');

                    newInputForServlet.setAttribute('value',newname);
                    newInputForServlet.setAttribute('name','NewnameForServlet');
                    newInputForServlet.setAttribute('disabled','');
                    RenameChain[RenameId++]=newname;

                    third3input.setAttribute('type','text');
                    third3input.setAttribute('id','newname');
                    third3input.setAttribute('value',newname);
		
                    third3input.setAttribute('disabled','disabled');			
                    dinamo = newname;
			
                    mytd.appendChild(newInput,'value');										
                    mythirdtd.appendChild(third3input,'value');				
				
                    lastRow.appendChild(mytd);
                    doc.appendChild(lastRow);
                    var nextRow = document.createElement('tr');
                    nextRow.appendChild(mythirdtd)
                    doc.appendChild(nextRow);

                    myNewtbody.appendChild(doc);
			
                    mytable.appendChild(myNewtbody, mytbody);
				
				
                    fl=0;
                    //edw einai ta append pou kanw
                    ServlettdfirstElem.appendChild(newInputForServlet,'value');
                    ServlettrElem.appendChild(ServlettdfirstElem);
                    ServletdocFragment.appendChild(ServlettrElem);
                    ServletNewtbody.appendChild(ServletdocFragment);
                    Servlettable.appendChild(ServletNewtbody, Servlettbody);
                    //edw einai ta append pou kanw	

                    //clear the input field
                    var inputField2 = document.getElementById('newname');
                    inputField2.value='';
			
					
                }
						
            }
            //if the term does not exist in TMS database
            else if(response == 'sorry')
            { 	//if it is not the first user's input
                var Input = getObj('Newname');
                var newInput;

                var InputForServlet = getObj('NewnameForServlet');
                var newInputForServlet ;

		if(++flag>1){
                    var oldname = document.getElementById('oldd').value;
	  
                    var telos = document.getElementById('newname')
                    final = telos.value;
			
                    var mytable = document.getElementById('myTable');

                    var mytbody = document.getElementById('myTbody');

                    var myNewtbody = document.createElement('tbody');
                    myNewtbody.id = 'myTbody';							
			      
                    //last row of table
                    var lastRow = mytable.rows[mytable.rows.length-1];

		
                    var doc = document.createDocumentFragment();

                    var mytd = document.createElement('td');
				
		

                    //gia to servlet
                    //gia na parw tis parametrous sto servlet 
                    //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                    //use sti RenameRealesed Concept 
                    //edw einai oi dilwseis 
                    var Servlettable = document.getElementById('myTable2');
                    var Servlettbody = document.getElementById('myTbody2');
                    var ServletNewtbody = document.createElement('tbody');
                    ServletNewtbody.id = 'myTbody2';
                    var ServletdocFragment = document.createDocumentFragment();					      	           	     var ServlettrElem;
                    var ServlettdfirstElem;
                    ServlettrElem =  document.createElement('tr');
                    ServlettdfirstElem =  document.createElement('td');
                    //gia to servlet
			
			 
                    newInput = Input.cloneNode(true);
                    newInputForServlet = InputForServlet.cloneNode(true);

                    newInput.setAttribute('value',final);
                    newInput.setAttribute('name','Newname');
                    newInput.setAttribute('disabled','disabled');

                    newInputForServlet.setAttribute('value',final);
                    newInputForServlet.setAttribute('name','NewnameForServlet');
                    newInputForServlet.setAttribute('disabled','');
                    mytd.appendChild(newInput,'value');	
                    inputerms = final;
                    lastRow.appendChild(mytd);
                    doc.appendChild(lastRow);					
				
                    myNewtbody.appendChild(doc);
			
                    mytable.appendChild(myNewtbody, mytbody);

                    ServlettdfirstElem.appendChild(newInputForServlet,'value');
                    ServlettrElem.appendChild(ServlettdfirstElem);
                    ServletdocFragment.appendChild(ServlettrElem);
                    ServletNewtbody.appendChild(ServletdocFragment);
                    Servlettable.appendChild(ServletNewtbody, Servlettbody);

                    var mybutton = document.getElementById('rename');
                    mybutton.disabled = false;

                    //clear the input field
                    var inputField2 = document.getElementById('newname');
                    inputField2.value='';


		}
		//if it is  the first user's input
                else{
                    var oldname = document.getElementById('oldd').value;
	  
                    var telos = document.getElementById('newname')
                    final = telos.value;
			
                    var mytable = document.getElementById('myTable');

                    var mytbody = document.getElementById('myTbody');

                    var myNewtbody = document.createElement('tbody');
                    myNewtbody.id = 'myTbody';

			
                    var mytr= document.createElement('tr');
                    var doc = document.createDocumentFragment();
                    var myfirsttd = document.createElement('td');
                    var mytd = document.createElement('td');

                    //gia to servlet
                    //gia na parw tis parametrous sto servlet 
                    //xriazetai na dimioyrgisw allon ena pinaka me hiden ta input elements poy 
                    //use sti RenameRealesed Concept 
                    //edw einai oi dilwseis 
                    var Servlettable = document.getElementById('myTable2');
                    var Servlettbody = document.getElementById('myTbody2');
                    var ServletNewtbody = document.createElement('tbody');
                    ServletNewtbody.id = 'myTbody2';
                    var ServletdocFragment = document.createDocumentFragment();					      	           	     var ServlettrElem;
                    var ServlettdfirstElem;
                    ServlettrElem =  document.createElement('tr');
                    ServlettdfirstElem =  document.createElement('td');
                    //gia to servlet
			

		
                    newInput = Input.cloneNode(true);
                    newInputForServlet = InputForServlet.cloneNode(true);

		
                    first3input = document.createElement('input');
				
                    first3input.setAttribute('type','text');
                    first3input.setAttribute('id','oldname');
                    first3input.setAttribute('value',oldname);
                    first3input.setAttribute('disabled','disabled');
		

		
                    newInput.setAttribute('value',final);
                    newInput.setAttribute('name','Newname');
                    newInput.setAttribute('disabled','disabled');


                    newInputForServlet.setAttribute('value',final);
                    newInputForServlet.setAttribute('name','NewnameForServlet');
                    newInputForServlet.setAttribute('disabled','');
                    RenameChain[RenameId++]=newname;
		
                    myfirsttd.appendChild(first3input,'value');
                    mytd.appendChild(newInput,'value');	
                    inputerms = final;
                    mytr.appendChild(myfirsttd);
                    mytr.appendChild(mytd);				
                    doc.appendChild(mytr);					
				
                    myNewtbody.appendChild(doc);
			
                    mytable.appendChild(myNewtbody, mytbody);
			
                    //edw einai ta append pou kanw
                    ServlettdfirstElem.appendChild(newInputForServlet,'value');
                    ServlettrElem.appendChild(ServlettdfirstElem);
                    ServletdocFragment.appendChild(ServlettrElem);
                    ServletNewtbody.appendChild(ServletdocFragment);
                    Servlettable.appendChild(ServletNewtbody, Servlettbody);
                    //edw einai ta append pou kanw
			
                    var mybutton = document.getElementById('rename');
                    mybutton.disabled = false;

                    //clear the input field
                    var inputField2 = document.getElementById('newname');
                    inputField2.value='';
		

			
                }
            }

       	}

	var mytable = document.getElementById('myTable');
	if(mytable.rows.length != 1)
	{
            var UndoButton = document.getElementById('Undo');
            UndoButton.disabled=false;
	}

    }
   
 
    RenamexmlHttp.open('get',RenameUrl,true);
        
    // IMPORTANT: clean up the cache of ohttp
    // BUG fix (30/1/08). Problem solved: the above servlet wasn't called for second time with the same parameter
    RenamexmlHttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");      	
        
    RenamexmlHttp.send(null);
}

/*-----------------------------------------------------
                    Undo_Rename()
-------------------------------------------------------*/      
function Undo_Rename() {

    var inputField = document.getElementById('newname');
    var Table = document.getElementById('myTable');
    var ServletTable = document.getElementById('myTable2');
    var TableRows = document.getElementById('myTable').rows.length;
    var ServletTableRows = document.getElementById('myTable2').rows.length;
    var Renamebutton = document.getElementById('rename');

    RenameChain.splice(RenameChain.length-1,1);
			  
    //case of disable Button		
    if(Renamebutton.disabled == true)
    {
		
		
	Table.deleteRow(TableRows-1);			
	TableRows = Table.rows.length;
	Table.rows[TableRows-1].deleteCell(1);
	ServletTableRows = document.getElementById('myTable2').rows.length;
		
	if(ServletTableRows != 2){		
            ServletTable.deleteRow(ServletTableRows-1);
	}
	//clears the input field
 	inputField.value='';			
    }
    //case of enable Button
    else
    {
         
	Table.rows[TableRows-1].deleteCell(1);
	ServletTableRows = document.getElementById('myTable2').rows.length;

	if(ServletTableRows != 2){		
            ServletTable.deleteRow(ServletTableRows-1);
	}
	//clears the input field
 	inputField.value='';
	Renamebutton.disabled = true;
    }

    TableRows = document.getElementById('myTable').rows.length;
	
    if(TableRows==2)
    {	
        Table.deleteRow(1);
        ServletTable.deleteRow(1);
        flag=0;
        Renamebutton.disabled = true; 
    }

    //disable the Undo Button
    var mytable = document.getElementById('myTable');
    if(mytable.rows.length == 1)
    {
        var UndoButton = document.getElementById('Undo');
	//clears the input field
        inputField.value='';
        UndoButton.disabled=true;
    }
}

function clearRenameChain(){
    
    var mytable = document.getElementById('myTable');
    if(mytable != null)
    while(mytable.rows.length > 1)
    {
        Undo_Rename();
    }
    
}


