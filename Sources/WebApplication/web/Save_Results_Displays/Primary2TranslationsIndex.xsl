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
    Document   : GreekEnglishIndex.xsl
    Created on : 21 Ιανουάριος 2009, 5:56 μμ
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
    <xsl:template match="/">
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
        <xsl:variable name="sortAttr" select="//sortAttr"/>
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/primary2translationsindex"/>
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="translationsSeperator" select="page/results/@translationsSeperator"/>
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
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <script type="text/javascript">
                    <xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/>
                </script>
            </head>
            <body style="background-color: #FFFFFF;" >
                <table width="100%">
                    <tr>
                        <td colspan="3">
                            <xsl:value-of disable-output-escaping="yes" select="$localecommon/searchcriteria/option[@lang=$lang]"/>
                            <br/>
                            <!-- <xsl:value-of select="//query/base" />-->
                            <xsl:value-of select="$localespecific/baselabel/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="//query/arg1"/>
                            </b>
                            <xsl:text>.</xsl:text>
                            <br/>
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
                    <tr>
                        <td colspan="4" align="left">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="//transaltions2primaryLocation"/>
                                </xsl:attribute>
                                <xsl:value-of select="$localespecific/linktotranslations2primaryindex/option[@lang=$lang]"/>
                            </a>
                            <br/>
                            <br/>
                        </td>
                    </tr>
                </table>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                                <xsl:text>background-color: </xsl:text>
                                <xsl:value-of select="$alternateRowsColor1"/> 
                                <xsl:text>;</xsl:text>
                                <!--<xsl:text>; text-align:center;</xsl:text>-->
                                <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                            </xsl:attribute>  
                        <xsl:choose>
                            <xsl:when test="count(//results/primary2transaltions/term)=0 or //results/primary2transaltions/term[1]/name = '' ">
                                <td align="left" valign="top" colspan="5">
                                    <strong>
                                        <xsl:value-of select="$localespecific/noresults/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td style=" font-weight:bold" width="10%">
                                    <xsl:value-of select="$localespecific/number/option[@lang=$lang]"/>
                                </td>
                                <td style=" font-weight:bold" width="45%">
                                    <xsl:value-of select="$localespecific/term/option[@lang=$lang]"/>
                                </td>
                                <td style=" font-weight:bold" width="45%">
                                    <xsl:value-of select="$localespecific/translationterm/option[@lang=$lang]"/>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <hr/>
                        </td>
                    </tr>
                    <xsl:for-each select="//results/primary2transaltions/term[./name != '' ]">
                        <!--  onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"-->
                        <tr valign="top">
                            <xsl:attribute name="onMouseOver">
                                <xsl:text>this.bgColor = '</xsl:text>
                                <xsl:value-of select="$onMouseOverColor"/> 
                                <xsl:text>';</xsl:text>
                            </xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="position() mod 2 =0">
                                    <xsl:attribute name="bgcolor">
                                        <xsl:value-of select="$alternateRowsColor1"/>                                         
                                    </xsl:attribute>   
                                    <xsl:attribute name="onMouseOut">
                                        <xsl:text>this.bgColor = '</xsl:text>
                                        <xsl:value-of select="$alternateRowsColor1"/> 
                                        <xsl:text>';</xsl:text>
                                    </xsl:attribute>                                                 
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="bgColor">
                                        <xsl:value-of select="$alternateRowsColor2"/>                                         
                                    </xsl:attribute>               
                                    <xsl:attribute name="onMouseOut">
                                        <xsl:text>this.bgColor = '</xsl:text>
                                        <xsl:value-of select="$alternateRowsColor2"/> 
                                        <xsl:text>';</xsl:text>
                                    </xsl:attribute>                                    
                                </xsl:otherwise>
                            </xsl:choose>
                            <td>
                                <xsl:value-of select="position()"/>
                            </td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="(not(./name)) or (./name='')">-
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:value-of select="./name"/>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:variable name="howmanytranslations" select="count(./translations/translation)"/>
                                <xsl:choose>
                                    <xsl:when test="$howmanytranslations=0">
                                        <xsl:text>-</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>

                                        <xsl:for-each select="./translations/translation">
                                            <a>
                                                <xsl:value-of select="./@linkClass"/>
                                                <xsl:value-of select="$translationsSeperator"/>
                                                <xsl:text> </xsl:text>
                                                <xsl:value-of select="."/>
                                                <xsl:if test="position() != $howmanytranslations">
                                                    <xsl:text>,</xsl:text>
                                                    <br/>
                                                </xsl:if>
                                            </a>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>

