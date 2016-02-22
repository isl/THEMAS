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
    Document   : FixData_Check_Orphan_Hierarchy.xsl
    Created on : 22 Ιούνιος 2009, 3:53 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
    xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes" 
    exclude-result-prefixes="xsl fo xs fn xdt">
    <xsl:output method="html"  
            encoding="UTF-8"  
            indent="yes" 
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            version="4.0" />
    <xsl:template match="/" >
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/fixdata/checkorphan"/>
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="pageTitle">
            <xsl:value-of select="$localespecific/titleprefix/option[@lang=$lang]"/>
            <xsl:value-of select="//title"/>
        </xsl:variable>
        <html>
            <head>
                <style rel="stylesheet" type="text/css">
                td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                </style>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <script type="text/javascript">
                    <xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/>
                </script>
            </head>
            <body style="background-color: #FFFFFF;"> <!--  -->
                <table width="100%">
                    <tr>
                        <td>
                            <b>
                                <xsl:value-of select="$localespecific/intro/option[@lang=$lang]"/>
                            </b>
                            <br/>
                        </td>
                        <td align="right">
                            <a href="#" class="SaveAsAndPrintLinks">
                                <xsl:attribute name="onclick">
                                    <xsl:text>saveAscode('SaveAs',null, '</xsl:text>
                                    <xsl:value-of select="$pageTitle"/>
                                    <xsl:text>');</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="$localecommon/saveas/option[@lang=$lang]"/>
                            </a>                          
                            &#160;
                            <a href="#" class="SaveAsAndPrintLinks" onclick="print()">
                                <xsl:value-of select="$localecommon/print/option[@lang=$lang]"/>
                            </a>
                        </td>
                    </tr>
                </table>
                <br/>
                <xsl:choose>
                    <xsl:when test="count(//concept)=0 ">
                        <table>
                            <tr>
                                <td align="left" valign="top" colspan="4">
                                    <strong>
                                        <xsl:value-of select="$localespecific/noerrors/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </tr>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <table width="100%" >
                            <tr>
                                <td align="center">
                                    <strong>
                                        <xsl:value-of select="$localespecific/number/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                                <td align="center">
                                    <strong>
                                        <xsl:value-of select="$localespecific/term/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                                <td align="center">
                                    <strong>
                                        <xsl:value-of select="$localespecific/orphanbts/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                                <td align="center">
                                    <strong>
                                        <xsl:value-of select="$localespecific/nonorphanbts/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="4">
                                    <hr/>
                                </td>
                            </tr>
                            <xsl:for-each select="//concept">
                                <xsl:sort select="./name"/>
                                <tr onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF">
                                    <td>
                                        <xsl:value-of select="position()"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./name"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./orphanBts"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./correctBts"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </table>
                    </xsl:otherwise>
                </xsl:choose>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
