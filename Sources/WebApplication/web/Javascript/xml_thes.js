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
var menu;
var theTop = 50;
var old = theTop;
var legend_showing = false;

function init() {
	doThes();
}

function doThes() {
	this.obj = document.getElementById('legendDiv');
	if (this.obj != null)
		this.style = this.obj.style;
	menu = this.obj;
	if (this.obj != null)
		movemenu();
}

function movemenu() {
	if (window.innerHeight)
	{
		  pos = window.pageYOffset
	}
	else if (document.documentElement && document.documentElement.scrollTop)
	{
		pos = document.documentElement.scrollTop
	}
	else if (document.body)
	{
		  pos = document.body.scrollTop
	}
	if (pos < theTop) pos = theTop;
	else pos += 50;
	if (pos == old)
	{
		menu.style.top = pos;
		if (pos!=menu.style.top) {
			menu.style.top = pos + 'px';
		}
	}
	old = pos;
	temp = setTimeout('movemenu()',50);
}

function toggleLegend() {
	if (!legend_showing)
		document.getElementById('legendDiv').style.display='block';
	else
		document.getElementById('legendDiv').style.display='none';
	legend_showing = !legend_showing;
}






