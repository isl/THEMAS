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
    Document   : SaveAll_Terms_Systematic.xsl
    Created on : 8 Οκτώβριος 2008, 1:35 πμ
    Author     : Hlias Tzortz
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
    <xsl:import href="../xml-xsl/Configs.xsl"/>
    <xsl:output method="html"  
                encoding="UTF-8"  
                indent="yes" 
                doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                doctype-system="http://www.w3.org/TR/html4/loose.dtd"
                version="4.0" />
    
    <!-- _________________ SearchResults TAB _________________ -->
    <xsl:template match="/" >      
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
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/savealltermssystematic"/>        
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="pageTitle">
            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/titleprefix/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            <xsl:value-of select="//page/@title"/>
        </xsl:variable>
        <html>
            <xsl:if test="$lang='ar'">
                <!--<xsl:attribute name="dir">
                    <xsl:text>rtl</xsl:text>                    
                </xsl:attribute>-->
                <xsl:attribute name="class">
                    <xsl:text>rtl</xsl:text>                    
                </xsl:attribute>
            </xsl:if>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; 
                    text-decoration:underline; color:black; }
                </style>
                <script type="text/javascript">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localecommon/browserdetectionsaveasscript/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </script>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"/>
                <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
            </head>
            <body style="background-color: #FFFFFF;" >
             
                <table width="100%"> 
                    <tr>
                        <td class="criteriaInSaves">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localecommon/searchcriteria/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            <br/>
                            <xsl:choose>
                                <xsl:when test="//query/base">
                                    <!--Συστηματική παρουσίαση όρων της ιεραρχίας:-->
                                    <!-- <xsl:value-of select="//query/base" /> -->
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/baselabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <b>
                                        <xsl:value-of select="//query/arg1"/>
                                    </b>.</xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="//query" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        
                       
                        <td align="right">
                            <a href="#" class="SaveAsAndPrintLinks">
                                <xsl:attribute name="onclick">
                                    <xsl:text>saveAscode('SaveAs',null, '</xsl:text>
                                    <xsl:value-of select="$pageTitle"/>
                                    <xsl:text>');</xsl:text>
                                </xsl:attribute>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localecommon/saveas/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </a>                          
                            &#160;                           
                            <a href="#" class="SaveAsAndPrintLinks" onclick="print()">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localecommon/print/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </a>
                        </td>
                    
                    </tr>
                </table>
                <xsl:choose>
                    <xsl:when test="count(//term)=0 ">
                        <table>
                            <tr>
                                <td align="left" valign="top" colspan="5">
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/noterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                            </tr>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <table width="100%" >

                            <tr> 
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>;</xsl:text>
                                </xsl:attribute>
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/number/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                  
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/tc/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                  
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/term/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                  
                            </tr>
                            <tr>
                                <td colspan="3"> 
                                    <hr/>
                                </td>
                            </tr>
                            <xsl:for-each select="//term">
                                <!--<xsl:sort select="./@position" data-type="number" />-->
                                <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"-->
                                <tr > 
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
                                    <td  >
                                        <xsl:value-of select="./@position"/>
                                    </td>

                                    <td >
                                        <xsl:choose>
                                            <xsl:when test="( not (./tc) or (./tc = '' ) ) ">-</xsl:when>
                                            <xsl:otherwise >
                                                <xsl:value-of select="./tc"/> 
                                            </xsl:otherwise>
                                        </xsl:choose>
                              
                                    </td>

                                    <td >
                                        <xsl:value-of select="./name"/>
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
