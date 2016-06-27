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
    Document   : search_results_sources.xsl
    Created on : 29 Απρίλιος 2009, 2:50 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:include href="PagingInfo_Source.xsl"/>
    <xsl:variable name="sourceslocale" select="$locale/primarycontentarea/sources"/>
    <xsl:template match="/page" name="search_results_sources">
        <xsl:param name="paginglocale" />
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        <xsl:variable name="outputVar" select="//output"/>
        <!--<fieldset class="links">
        <legend>
            <xsl:value-of select="$sourceslocale/tableresults/legend/option[@lang=$lang]"/>
        </legend>
        -->
            
        <table width="100%" style="padding-left:5px;">
                
            <xsl:if test="//results/paging_info">
                <xsl:call-template name="DisplayStatisticsAndPagingInfo_Sources">
                    <xsl:with-param name="paginglocale" select="$paginglocale" />
                </xsl:call-template>
            </xsl:if>
                
            <tr width="100%">
                <xsl:attribute name="style">
                        <xsl:text>background-color: </xsl:text>
                        <xsl:value-of select="$alternateRowsColor1"/> 
                        <xsl:text>;</xsl:text>
                        <!--<xsl:text>; text-align:center;</xsl:text>-->
                        <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                    </xsl:attribute>                 
                <xsl:choose>
                        
                    <xsl:when test="count(//data/sources/source)=0">
                        <td align="left" valign="top" colspan="5">
                            <strong>
                                <xsl:value-of select="$sourceslocale/tableresults/noresultsmsg/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </xsl:when>
                        
                    <xsl:otherwise>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$sourceslocale/tableresults/columns/source/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <xsl:for-each select="$outputVar/node()">
                            <td height="5">
                                <strong>
                                    <xsl:choose>
                                        <xsl:when test="name() = 'source_note' ">
                                            <xsl:value-of select="$sourceslocale/tableresults/columns/sourcenote/option[@lang=$lang]"/>
                                        </xsl:when>
                                        <xsl:when test="name() = 'primary_found_in' ">
                                            <xsl:value-of select="$sourceslocale/tableresults/columns/primarysource/option[@lang=$lang]"/>
                                        </xsl:when>
                                        <xsl:when test="name() = 'translations_found_in' ">
                                            <xsl:value-of select="$sourceslocale/tableresults/columns/trsource/option[@lang=$lang]"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="name()"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </strong>
                            </td>
                                
                        </xsl:for-each>
                            
                        <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'"> 
                            <td>
                                <strong>
                                    <xsl:value-of select="$sourceslocale/tableresults/columns/actions/legend/option[@lang=$lang]"/>
                                </strong>
                            </td>
                        </xsl:if>
                            
                    </xsl:otherwise>
                        
                </xsl:choose>
            </tr>
                
                
            <xsl:for-each select="//data/sources/source[./name!='']">
                    
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
                        
                    <td>
                        <a href="#">
                            <xsl:attribute name="onClick">
                                <xsl:text>popUpCard('</xsl:text>
                                <xsl:value-of select="$currentJS"/>
                                <xsl:text>','CardOf_Source')</xsl:text>
                            </xsl:attribute>
                            <xsl:value-of  select="./name"/>
                        </a>
                    </td>
                    <xsl:if test="count($outputVar/source_note)!=0">     
                        <xsl:choose>
                            <xsl:when test="count(./source_note)=0 or ./source_note = ''">
                                <td width="120">
                                    <xsl:text>-</xsl:text>
                                </td>
                            </xsl:when>
                            <!--<xsl:when test="$outputVar/primary_found_in and $outputVar/translations_found_in">
                                <td width="300"  style="WORD-BREAK:BREAK-ALL;">
                                    <xsl:value-of disable-output-escaping="yes" select="./source_note"/>
                                </td>
                            </xsl:when>-->
                            <xsl:otherwise>
                                <td width="400" style="WORD-BREAK:BREAK-ALL;">
                                    <xsl:value-of disable-output-escaping="yes" select="./source_note"/>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>                   
                    </xsl:if>
                    <xsl:if test="count($outputVar/primary_found_in)!=0">
                        <xsl:call-template name="drawAttributeTd">
                            <xsl:with-param name="nodeSet" select="./primary_found_in"/>
                            <xsl:with-param name="popUpCard">
                                <xsl:text>TERM</xsl:text>
                            </xsl:with-param>
                        </xsl:call-template>                                
                    </xsl:if>
                    <xsl:if test="count($outputVar/translations_found_in)!=0">                                
                        <xsl:call-template name="drawAttributeTd">
                            <xsl:with-param name="nodeSet" select="./translations_found_in"/>
                            <xsl:with-param name="popUpCard">
                                <xsl:text>TERM</xsl:text>
                            </xsl:with-param>
                        </xsl:call-template>                                
                    </xsl:if>

                    <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'"> 

                        <td align="center" width="10%">
                                
                            <a href="#">
                                <xsl:attribute name="onClick">showEditCard_Source('<xsl:value-of select="$currentJS"/>')</xsl:attribute>
                                <img width="16" height="16" border="0">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$sourceslocale/tableresults/columns/actions/edit/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$sourceslocale/tableresults/columns/actions/edit/title/option[@lang=$lang]"/>
                                    </xsl:attribute>                                        
                                </img>
                            </a>
                        </td>
                    </xsl:if>       

                </tr>

            </xsl:for-each>

        </table>	
        <!--</fieldset>-->
    </xsl:template>

</xsl:stylesheet>
