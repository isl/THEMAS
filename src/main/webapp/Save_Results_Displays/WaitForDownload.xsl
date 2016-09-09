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

<!--
    Document   : WaitForDownload.xsl
    Created on : 26 Μάϊος 2010, 1:50 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"
            encoding="UTF-8"  
            indent="yes" 
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            version="4.0" />	

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <xsl:variable name="locale" select="document('../translations/SaveAll_Locale_And_Scripting.xml')/root/waitfordownload"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <!-- 
    Document   : WaitForDownload
    Created on : 27 Ιαν 2009, 11:57:40 πμ
    Author     : tzortzak
-->
        <html>
            <head>
                <title>
                    <xsl:value-of select="$locale/pagetitle/option[@lang=$lang]"/>
                </title>
            </head>
            <body bgcolor="#FFFFFF">
                <div align="left" style="position:absolute; top:0px; left:0px; width:680px; overflow:hidden" >
                    <img  border="0" >
                        <xsl:attribute name="src">
                            <xsl:value-of select="$locale/headerimage/src/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="$locale/headerimage/title/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </img>
                </div>

                <div id="pleasewaitScreen" style="position:absolute;z-index:5;top:30%;left:35%;">
                    <table bgcolor="#000000" border="1" bordercolor="#000000" cellpadding="0" cellspacing="0" height="100" width="200">
                        <tr>
                            <td width="100%" height="100%" bgcolor="#FFFFFF" align="center" valign="middle">
                                <br/>                                                
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$locale/pleasewaitimage/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$locale/pleasewaitimage/title/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                </img>
                                <br/>   
                                    <font face="Helvetica,verdana,Arial" size="2" color="#000066">
                                        <xsl:value-of disable-output-escaping="yes" select="$locale/pleasewaitmessage/option[@lang=$lang]"/>
                                    </font>
                                <br/>
                                <br/>
                            </td>
                        </tr>
                    </table>
                </div>


            </body>
        </html>

    </xsl:template>

</xsl:stylesheet>
