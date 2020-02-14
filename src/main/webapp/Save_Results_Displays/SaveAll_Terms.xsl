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
 WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
 
 =============================================================================
 Authors: 
 =============================================================================
 Elias Tzortzakakis <tzortzak@ics.forth.gr>
 
This file is part of the THEMAS system.
-->

<!--
    Document   : SaveAll_Terms.xsl
    Created on : 10 Οκτώβριος 2008, 1:22 μμ
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
    
    <xsl:import href="../xml-xsl/Configs.xsl"/>
    
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

        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/saveallterms"/>        
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="pageTitle">
            <xsl:call-template name="getTranslationMessage"> 
                <xsl:with-param name="targetLangElements" select="$localespecific/titleprefix/option"/> 
                <xsl:with-param name="targetLang" select="$lang"/> 
                <xsl:with-param name="disableEscape" select="'no'"/> 
            </xsl:call-template>    
            <xsl:value-of select="//title"/>
        </xsl:variable>
        <xsl:variable name="sortAttr" select="//sortAttr"/>
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
                
                <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}                    
                    a {text-decoration: none; color:black;}
                    .showDecorations a{color: #0000EE; text-decoration: underline; color: -webkit-link; color: -moz-hyperlinktext;}
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                </style>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"/>
                <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
                <script type="text/javascript">
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$localecommon/browserdetectionsaveasscript/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'no'"/> 
                    </xsl:call-template>    
                </script>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
               
            </head>
            <body style="background-color: #FFFFFF;" >
                <table width="100%">
                    <tr>
                        <td  class="criteriaInSaves">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$localecommon/searchcriteria/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                            </xsl:call-template>    
                            <br/>
                            <xsl:value-of select="//query" />
                        </td>
                        <td align="right">

                            <a href="#" class="SaveAsAndPrintLinks">
                                <xsl:attribute name="onclick">
                                    <xsl:text>saveAscode('SaveAs',null, '</xsl:text>
                                    <xsl:value-of select="$pageTitle"/>
                                    <xsl:text>');</xsl:text>
                                </xsl:attribute>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$localecommon/saveas/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </a>
                            &#160;
                            <a href="#" class="SaveAsAndPrintLinks" onclick="print()">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$localecommon/print/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </a>
                        </td>
                    </tr>
                </table>
                <xsl:variable name="outputVar" select="//output"/>
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
                            <xsl:when test="count(//data/terms/term)=0 or //data/terms/term[1]/descriptor = '' ">
                                <td align="left" valign="middle" colspan="5">
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$localespecific/noterms/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </strong>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- karam: a/a -->
                                <td >
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$localespecific/number/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </strong>
                                </td>
                                <td >
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$localespecific/term/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </strong>
                                </td>
                                <xsl:for-each select="$outputVar/node()">
                                    <xsl:if test="string-length(name()) != 0 ">
                                        <td >
                                            <strong>
                                                <xsl:choose>
                                                    <xsl:when test="name() = 'descriptor' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/term/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'translations' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/translations/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'bt' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/bt/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'nt' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/nt/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'topterm' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/topterm/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'rt' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/rt/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'uf' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/uf/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'uf_translations' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/uf_translations/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'tc' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/tc/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'scope_note' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/sn/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'translations_scope_note' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/sn_tr/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'facet' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/facet/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'primary_found_in' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/primarysource/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'translations_found_in' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/trsource/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'created_by' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/creator/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'modified_by' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/modificator/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'created_on' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/creationdate/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'modified_on' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/modificationdate/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'status' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/status/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:when test="name() = 'historical_note' ">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$localespecific/hn/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="name()"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </strong>
                                        </td>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>
                    <tr>
                        <td valign="middle">
                            <xsl:attribute name="colspan">
                                <xsl:value-of select="count($outputVar/node())+2"/>
                            </xsl:attribute>
                            <hr/>
                        </td>
                    </tr>
                    <xsl:variable name="trsSeparator" select="//data/@translationsSeperator"/>
                    <xsl:for-each select="//data/terms/term[./descriptor != '' ]">
                        <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF" -->
                        <tr  valign="top">
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
                                <xsl:attribute name="id">
                                    <xsl:value-of select="./descriptor/@id"/>
                                </xsl:attribute>
                                <xsl:value-of select="./descriptor"/>
                            </td>
                            <!-- ATTENTION: Followinf ifs must be in the same order as output element-->
                            <xsl:if test="count($outputVar/translations)!=0">
                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./translations"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/bt)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./bt"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/nt)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./nt"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/topterm)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./topterm"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/rt)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./rt"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/uf)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./uf"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/uf_translations)!=0">
                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./uf_translations"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/tc)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./tc"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/scope_note)!=0">  
                                <!--<td width="400" style="WORD-BREAK:BREAK-ALL;">-->
                                <td>
                                    <xsl:choose>
                                        <xsl:when test="./scope_note/text()!=''">
                                            <span class="showDecorations">
                                                <xsl:value-of disable-output-escaping="yes" select="./scope_note"/>
                                            </span>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <a>-</a>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    
                                </td>                              
                                <!--<xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./scope_note"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>   -->
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/translations_scope_note)!=0">

                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./translations_scope_note"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>
                                <!--
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./translations_scope_note"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>-->
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/facet)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./facet"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/primary_found_in)!=0">
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./primary_found_in"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/translations_found_in)!=0">
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./translations_found_in"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                           
                            <xsl:if test="count($outputVar/created_by)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./created_by"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/modified_by)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./modified_by"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/created_on)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./created_on"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/modified_on)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./modified_on"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/status)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./status"/>
                                    <xsl:with-param name="localespecific" select="$localespecific"/>
                                    <xsl:with-param name="lang" select="$lang"/>
                                </xsl:call-template> 
                            </xsl:if>
                            
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="drawTranslationTd">
        <xsl:param name="nodeSet"/>
        <xsl:param name="localespecific"/>
        <xsl:param name="lang" />
        <xsl:param name="translationSeparator" />
        <td>
            <xsl:variable name="howmanyFields" select="count($nodeSet[node()!=''])"/>
            
            <xsl:choose>
                <xsl:when test="$howmanyFields=0">-</xsl:when>
                <xsl:otherwise>
                    
                    <xsl:for-each select="$nodeSet/node()">
                        <xsl:choose>
                            <xsl:when test="local-name(..)='translations_scope_note'">
                                <span class="showDecorations">
                                    <xsl:value-of select="../@linkClass"/>
                                    <xsl:value-of select="$translationSeparator"/>
                                    <xsl:text> </xsl:text>

                                    <xsl:value-of disable-output-escaping="yes" select="."/>
                                    <br/>
                                </span>
                            </xsl:when>
                            <xsl:otherwise>
                                <a>
                                    <xsl:value-of select="../@linkClass"/>
                                    <xsl:value-of select="$translationSeparator"/>
                                    <xsl:text> </xsl:text>
                                    
                                    <xsl:value-of select="."/>
                                </a>
                                
                                <xsl:if test="position()!= $howmanyFields">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                                <br/>
                            </xsl:otherwise>
                        </xsl:choose>
                    
                    </xsl:for-each>                    
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>

    <xsl:template name="drawAttributeTd">
        <xsl:param name="nodeSet"/>
        <xsl:param name="localespecific"/>        
        <xsl:param name="lang" />
        <td>
            <xsl:variable name="howmanyFields" select="count($nodeSet[node()!=''])"/>
            <!--<xsl:value-of select="$howmanyFields"/>-->
            <xsl:choose>
                <xsl:when test="$howmanyFields=0">-</xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="$nodeSet/node()">  
                        <xsl:choose>
                            <xsl:when test="../@id ">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:text>#</xsl:text>
                                        <xsl:value-of select="../@id"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:when test="name(..)='status'">                                
                                <xsl:value-of select="."/>
                            </xsl:when>

                            <xsl:otherwise>
                                <a>
                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="position()!= $howmanyFields">
                                <!-- <xsl:text>, </xsl:text> -->
                                <br/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template> 
</xsl:stylesheet>
