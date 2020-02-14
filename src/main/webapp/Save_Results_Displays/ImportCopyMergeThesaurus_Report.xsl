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
    Document   : ImportCopyMergeThesaurus_Report.xsl
    Created on : 23 Απρίλιος 2009, 4:11 μμ
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
    
    <xsl:template match="/" >
        <xsl:variable name="pageTitle" select="//title"/>
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/importcopymerge"/>
        <xsl:variable name="lang" select="page/@language"/>
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
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                </style>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <script type="text/javascript">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localecommon/browserdetectionsaveasscript/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </script>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"/>
                <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
            </head>
            <body style="background-color: #FFFFFF;" >
                <table width="100%">
                    <tr>
                        <td>
                            <xsl:value-of select="//creationInfo"/>
                            <br/>
                            <br/>
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/reporttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                            <br/>
                            <br/>
                        </td>
                        <td align="right" valign="top">
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
                    <xsl:when test="count(//targetTerm)=0 and count(//targetHierarchy)=0 and count(//targetFacet)=0 ">
                        <table>
                            <tr>
                                <td align="left" valign="top" colspan="5">
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/nodiffs/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                            </tr>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <table width="100%" >
                            <tr align="center">
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/number/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/termhier/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/relation/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/value/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                                <td>
                                    <strong>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/reason/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </strong>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="5">
                                    <hr/>
                                </td>
                            </tr>
                            <xsl:for-each select="//targetFacet | //targetHierarchy | //targetTerm">
                                <tr valign="top" onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF">
                                    <td>
                                        <xsl:value-of select="position()"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./name"/>
                                    </td>
                                    <td align="center">
                                        <xsl:variable name="relation" select="./errorType"/>
                                        <xsl:choose>
                                            <xsl:when test="$relation='termname'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/termname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='sourcename'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/sourcename/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='tc'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/tc/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='source_note'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/source_note/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='name'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/rename/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='facet'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/facet/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='bt'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/bt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='rt'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/rt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='referenceId'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/referenceId/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='translations'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/translations/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='uf'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/uf/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='uf_translations'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/uf_translations/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='primary_found_in'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/primary_found_in/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='translations_found_in'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/translations_found_in/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='status'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/status/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='scope_note'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/sn/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='translations_scope_note'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/translations_scope_note/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='historical_note'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/hn/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='source_note'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/sourcenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:when test="$relation='guide_terms'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$localespecific/guide_term/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$relation"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./errorValue"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./reason"/>
                                        <br/>
                                        <br/>
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
