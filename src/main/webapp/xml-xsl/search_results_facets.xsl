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
	<!--
    Document   : search_facet_results.xsl
    Created on : 10 Ιούλιος 2008, 4:34 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
    <xsl:include href="PagingInfo_Facet.xsl"/>
    <xsl:variable name="facetslocale" select="$locale/primarycontentarea/facets"/>
    <xsl:template match="/page" name="search_results_facets">
        <xsl:param name="paginglocale" />
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        <xsl:variable name="outputVar" select="//output"/>
        <!--<fieldset class="links">
            <legend>
                <xsl:value-of select="$facetslocale/tableresults/legend/option[@lang=$lang]"/>
            </legend>-->
            <table width="100%" style="padding-left:5px;">
                <xsl:if test="//results/paging_info">
                    <xsl:call-template name="DisplayStatisticsAndPagingInfo_Facets">
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
                        
                        <xsl:when test="count(//data/facets/facet)=0">
                            <td align="left" valign="top" colspan="5">
                                <strong>
                                    <xsl:value-of select="$facetslocale/tableresults/noresultsmsg/option[@lang=$lang]"/>
                                </strong>
                            </td>
                        </xsl:when>
                        
                        <xsl:otherwise>
                            
                            <td height="5">
                               <strong>
                                   <xsl:value-of select="$facetslocale/tableresults/columns/facet/option[@lang=$lang]"/>
                               </strong>
                           </td>
                            <xsl:for-each select="$outputVar/node()">
                                <td height="5">
                                    <strong>
                                        <xsl:choose>
                                            <xsl:when test="name() = 'hierarchy' ">
                                                <xsl:value-of select="$facetslocale/tableresults/columns/hiers/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
                                        </xsl:choose>
                                    </strong>
                                </td>
                                
                            </xsl:for-each>
                            
                            <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'"> 
                                <td>
                                    <strong>
                                        <xsl:value-of select="$facetslocale/tableresults/columns/actions/legend/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </xsl:if>
                            
                        </xsl:otherwise>
                        
                    </xsl:choose>
                    
                </tr>
                
                
                <xsl:for-each select="//data/facets/facet[./name!='']">
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
                                    <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Facet')</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of  select="./name"/>
                            </a>
                        </td>
                        
                        <xsl:if test="count($outputVar/hierarchy)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./hierarchy"/>
                                    <xsl:with-param name="popUpCard"><xsl:text>HIERARCHY</xsl:text></xsl:with-param>
                                </xsl:call-template>                                
                        </xsl:if>
                    
                        <td align="center" width="10%">
                            
                            <xsl:variable name="currentJS0">
                                <xsl:call-template name="replace-string">
                                    <xsl:with-param name="text" select="./node()"/>
                                    <xsl:with-param name="replace" select="$Slash"/>
                                    <xsl:with-param name="with" select="$SlashJS"/>
                                </xsl:call-template>
                            </xsl:variable>
                            
                            <xsl:variable name="currentJS">
                                <xsl:call-template name="replace-string">
                                    <xsl:with-param name="text" select="$currentJS0"/>
                                    <xsl:with-param name="replace" select="$Apos"/>
                                    <xsl:with-param name="with" select="$AposJS"/>
                                </xsl:call-template>
                            </xsl:variable>
                            
                            <a href="#">
                                <img width="16" height="16" border="0"  style="margin-left:4px;">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$facetslocale/tableresults/columns/actions/hierarchical/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$facetslocale/tableresults/columns/actions/hierarchical/title/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="onClick">
                                        <xsl:text>prepareResults('hierarchysTermsShortcuts','</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','facethierarchical','true');</xsl:text>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a href="#">
                                <xsl:attribute name="onclick">
                                    <xsl:text>GraphicalViewIconPressed('GraphicalView','</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>', "FACET","false")</xsl:text>
                                </xsl:attribute>
                                <img width="16" height="16" border="0" style="margin-left:4px; margin-right:4px;">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$facetslocale/tableresults/columns/actions/graphical/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$facetslocale/tableresults/columns/actions/graphical/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    
                                </img>
                            </a>
			    <!-- Check for user of READER group (DISABLE editing) -->
                            <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER' and $THEMASUserInfo_userGroup != 'LIBRARY' ">
                                <a href="#">
                                    <xsl:attribute name="onClick">showEditCard_Facet('<xsl:value-of select="$currentJS"/>')</xsl:attribute>
                                    <img width="16" height="16" border="0"  style="margin-right:4px;">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$facetslocale/tableresults/columns/actions/edit/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$facetslocale/tableresults/columns/actions/edit/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </xsl:if>
                            
                        </td>
                        
                    </tr>
                    
                </xsl:for-each>                
            </table>            
        <!--</fieldset>-->
    </xsl:template>
</xsl:stylesheet>
