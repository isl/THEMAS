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
    Document   : search_results_terms_systematic.xsl
    Created on : 11 Σεπτέμβριος 2008, 7:11 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <xsl:variable name="systematicresultslocale" select="$locale/primarycontentarea/terms/systematicresults"/>
    <xsl:include href="PagingInfo_Term_Systematic.xsl"/>
    <!-- _________________ SearchResults TAB _________________ -->
    <xsl:template match="/page" name="search_results_terms_systematic"  >
        <xsl:param name="paginglocale" />
        <!--<fieldset class="links">
        <legend>
            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systematicresultslocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
        </legend>
        -->
        <table width="100%" style="padding-left:5px;">
            <tbody>
                <!-- statistics and paging info -->
                <xsl:if test="//results/paging_info">
                    <xsl:call-template name="DisplayStatisticsAndPagingInfo_Terms_Systematic">
                        <xsl:with-param name="paginglocale" select="$paginglocale" />
                    </xsl:call-template>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="count(//results/term)=0">
                        <tr width="100%" height="10%">
                            <td align="left" valign="top" colspan="5">
                                <strong>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systematicresultslocale/noresultsmsg/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </strong>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <xsl:attribute name="style">
                                <xsl:text>background-color: </xsl:text>
                                <xsl:value-of select="$alternateRowsColor1"/> 
                                <xsl:text>;</xsl:text>
                                <!--<xsl:text>; text-align:center;</xsl:text>-->
                                <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                            </xsl:attribute>                 
                            <!-- style="text-align:right;"-->
                            <td width="30%"> 
                                <strong>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systematicresultslocale/columns/tc/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </strong>
                            </td>
                            <td width="70%">
                                <strong>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systematicresultslocale/columns/term/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </strong>
                            </td>
                        </tr>                            
                        <xsl:for-each select="//results/term">
                            <xsl:variable name="currentJS0">
                                <xsl:call-template name="replace-string">
                                    <xsl:with-param name="text" select="./name"/>
                                    <xsl:with-param name="replace" select="$Slash" />
                                    <xsl:with-param name="with" select="$SlashJS" />
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:variable name="currentJS">
                                <xsl:call-template name="replace-string">
                                    <xsl:with-param name="text" select="$currentJS0"/>
                                    <xsl:with-param name="replace" select="$Apos" />
                                    <xsl:with-param name="with" select="$AposJS" />
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:choose>
                                <xsl:when test="./tc != ''">  
                                    <tr class="resultRow" valign="top">
                                        <xsl:attribute name="onMouseOver">
                                            <xsl:text>this.style.background = '</xsl:text>
                                            <xsl:value-of select="$onMouseOverColor"/> 
                                            <xsl:text>'</xsl:text>
                                        </xsl:attribute>                        
                                        <xsl:choose>
                                            <xsl:when test="position() mod 2 =0">
                                                <xsl:attribute name="style">
                                                    <xsl:text>background-color: </xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                                    <xsl:text>;</xsl:text>
                                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                                </xsl:attribute>                        
                                                <xsl:attribute name="onMouseOut">
                                                    <xsl:text>this.style.background = '</xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                                    <xsl:text>'</xsl:text>
                                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                                </xsl:attribute>                        
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="style">
                                                    <xsl:text>background-color: </xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                                    <xsl:text>;</xsl:text>
                                                    <!--<xsl:text>background-color: #FFFFFF;</xsl:text>-->
                                                </xsl:attribute>     
                                                <xsl:attribute name="onMouseOut">
                                                    <xsl:text>this.style.background = '</xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                                    <xsl:text>'</xsl:text>
                                                    <!--<xsl:text>this.style.background = '#FFFFFF'</xsl:text>-->
                                                </xsl:attribute>                                           
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:for-each select="./tc">
                                            <!-- style="text-align:right;"-->
                                            <td width="30%" height="10%">
                                                <xsl:value-of select="."/>
                                            </td>
                                        </xsl:for-each>
                                        <xsl:for-each select="./name">
                                            <td width="70%" height="10%">
                                                <a href="#"> 
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>popUpCard('</xsl:text>
                                                        <xsl:value-of select="$currentJS"/>
                                                        <xsl:text>','CardOf_Term')</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:value-of  select="."/>
                                                </a>
                                            </td>
                                        </xsl:for-each>
                                    </tr>
                                </xsl:when>
                                <xsl:otherwise >
                                    <tr class="resultRow" valign="top">
                                        <xsl:attribute name="onMouseOver">
                                            <xsl:text>this.style.background = '</xsl:text>
                                            <xsl:value-of select="$onMouseOverColor"/> 
                                            <xsl:text>'</xsl:text>
                                        </xsl:attribute>                        
                                        <xsl:choose>
                                            <xsl:when test="position() mod 2 =0">
                                                <xsl:attribute name="style">
                                                    <xsl:text>background-color: </xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                                    <xsl:text>;</xsl:text>
                                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                                </xsl:attribute>                        
                                                <xsl:attribute name="onMouseOut">
                                                    <xsl:text>this.style.background = '</xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                                    <xsl:text>'</xsl:text>
                                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                                </xsl:attribute>                        
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="style">
                                                    <xsl:text>background-color: </xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                                    <xsl:text>;</xsl:text>
                                                    <!--<xsl:text>background-color: #FFFFFF;</xsl:text>-->
                                                </xsl:attribute>     
                                                <xsl:attribute name="onMouseOut">
                                                    <xsl:text>this.style.background = '</xsl:text>
                                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                                    <xsl:text>'</xsl:text>
                                                    <!--<xsl:text>this.style.background = '#FFFFFF'</xsl:text>-->
                                                </xsl:attribute>                                           
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:for-each select="./name">
                                            <td width="30%"  height="10%">-</td>
                                            <td  width="70%" height="10%">
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>popUpCard('</xsl:text>
                                                        <xsl:value-of select="$currentJS"/>
                                                        <xsl:text>','CardOf_Term')</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:value-of  select="."/>
                                                </a>
                                            </td>
                                        </xsl:for-each>
                                    </tr>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </tbody>
        </table>
        <!--</fieldset>-->
    </xsl:template>
</xsl:stylesheet>
