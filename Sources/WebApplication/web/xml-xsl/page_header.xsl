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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <!-- _________________________________________________________________________________________________
            template: page_header
      _________________________________________________________________________________________________
            FUNCTION: template that builds the header of each page, with the logo image and the title
    _________________________________________________________________________________________________ -->
    <xsl:template name="page_header">
        <div>
            <img width="1024">
                <xsl:attribute name="src">
                    <xsl:value-of select="$locale/header/imagepath/option[@lang=$lang]"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="$locale/header/name/option[@lang=$lang]"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="$locale/header/name/option[@lang=$lang]"/>
                </xsl:attribute>
            </img>
        </div>
        <!-- please wait screen -->
        <xsl:call-template name="pleasewaitScreen"/>
    </xsl:template>
    <!-- _________________________________________________________________________________________________
            template: pleasewaitScreen
      _________________________________________________________________________________________________
            FUNCTION: template that defines the please wait screen (hidden initially)
  _________________________________________________________________________________________________ -->
    <xsl:template name="pleasewaitScreen">
        <div id="pleasewaitScreen" bgcolor="#FFFFFF" >
            <table bgcolor="#000000" cellpadding="0" cellspacing="0" style="border:1px solid #000000; height:100px; width:200px;" >
                <tr>
                    <td width="100%" height="100%" bgcolor="#FFFFFF" align="center" valign="middle">
                        <br/>
                        <img alt="" src="images/pleasewait.gif"/>
                        <br/>
                        <font face="Helvetica,verdana,Arial" size="2" color="#000066">
                            <b>
                                <xsl:value-of select="$locale/generalmessages/pleasewait/text/option[@lang=$lang]"/>
                            </b>
                        </font>
                        <br/>
                        <br/>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>
</xsl:stylesheet>
