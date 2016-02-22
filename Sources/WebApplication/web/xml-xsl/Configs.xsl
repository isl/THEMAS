<?xml version="1.0" encoding="UTF-8"?>
<!-- 
 Copyright 2015 Institute of Computer Science,
                Foundation for Research and Technology - Hellas.

 Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 by the European Commission - subsequent versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

      http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in writing, software distributed
 under the Licence is distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the Licence for the specific language governing permissions and limitations
 under the Licence.
 
 =============================================================================
 Contact: 
 =============================================================================
 Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
     Tel: +30-2810-391632
     Fax: +30-2810-391638
  E-mail: isl@ics.forth.gr
 WebSite: http://www.ics.forth.gr/isl/cci.html
 
 =============================================================================
 Authors: 
 =============================================================================
 Elias Tzortzakakis <tzortzak@ics.forth.gr>
 
 This file is part of the THEMAS system.
 -->
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:fo="http://www.w3.org/1999/XSL/Format" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema" 
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
                xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"
                exclude-result-prefixes="xsl fo xs fn xdt">
        
    
    <!-- <xsl:output method="html"  
                   encoding="UTF-8"  
                   indent="yes" 
                   doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                   doctype-system="http://www.w3.org/TR/html4/loose.dtd"
                   version="4.0" />	
    -->
    <!-- #E8E9BE --> 
    <!-- #feffd0 -->
    <!-- #D9EDFC -->
    <!-- #B6DCF9 -->
        <!-- #343839 -->
       
    <xsl:variable name="pageBgColor">        
        <xsl:text>#D9EDFC</xsl:text>
        <xsl:text/>
    </xsl:variable>
    <xsl:variable name="onMouseOverColor">
        <!-- #D9EDFC -->
        <xsl:text>#D9EDFC</xsl:text>
    </xsl:variable>
    <xsl:variable name="alternateRowsColor1">
        <!-- #E2E2E2 -->
        <xsl:text>#E2E2E2</xsl:text>
    </xsl:variable>
    <xsl:variable name="alternateRowsColor2">
        <!-- #FFFFFF -->
        <xsl:text>#FFFFFF</xsl:text>
    </xsl:variable>
        
    
</xsl:stylesheet>
