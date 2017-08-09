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

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <xsl:variable name="tableresultslocale" select="$locale/primarycontentarea/terms/tableresults"/>
    <xsl:include href="PagingInfo_Term.xsl"/>
    <!-- _________________ SearchResults TAB _________________ -->
    <xsl:template match="/page" name="search_results_terms">
        <xsl:param name="paginglocale" />
        <xsl:variable name="move" select="//content/move"/>
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        
        <!--<fieldset class="links">
            <legend>
                <xsl:value-of select="$tableresultslocale/legend/option[@lang=$lang]"/>
            </legend>-->
            <xsl:variable name="outputVar" select="//output"/>
            <table width="100%" style="padding-left:5px;">
                <xsl:if test="//results/paging_info">
                    <xsl:call-template name="DisplayStatisticsAndPagingInfo_Terms">
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
                        <xsl:when test="count(//data/terms/term)=0 or //data/terms/term[1]/descriptor = '' ">
                            <td align="left" valign="top" colspan="5">
                                <strong>
                                    <xsl:value-of select="$tableresultslocale/noresultsmsg/option[@lang=$lang]"/>
                                </strong>
                            </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td >
                                <strong>
                                    <xsl:value-of select="$tableresultslocale/columns/term/option[@lang=$lang]"/>
                                </strong>
                            </td>
			    <xsl:for-each select="$outputVar/node()">
                                <td height="5">
                                    <strong>
                                        <xsl:choose>
                                            <xsl:when test="name() = 'descriptor' ">
                                                <xsl:value-of select="$tableresultslocale/columns/term/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'translations' ">
                                                <xsl:value-of select="$tableresultslocale/columns/translations/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'bt' ">
                                                <xsl:value-of select="$tableresultslocale/columns/bt/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'nt' ">
                                                <xsl:value-of select="$tableresultslocale/columns/nt/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'topterm' ">
                                                <xsl:value-of select="$tableresultslocale/columns/topterm/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'rt' ">
                                                <xsl:value-of select="$tableresultslocale/columns/rt/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'uf' ">
                                                <xsl:value-of select="$tableresultslocale/columns/uf/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'uf_translations' ">
                                                <xsl:value-of select="$tableresultslocale/columns/uf_translations/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'tc' ">
                                                <xsl:value-of select="$tableresultslocale/columns/tc/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'scope_note' ">
                                                <xsl:value-of select="$tableresultslocale/columns/sn/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'translations_scope_note' ">
                                                <xsl:value-of select="$tableresultslocale/columns/sn_tr/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'facet' ">
                                                <xsl:value-of select="$tableresultslocale/columns/facet/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'primary_found_in' ">
                                                <xsl:value-of select="$tableresultslocale/columns/primarysource/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'translations_found_in' ">
                                                <xsl:value-of select="$tableresultslocale/columns/trsource/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'created_by' ">
                                                <xsl:value-of select="$tableresultslocale/columns/creator/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'modified_by' ">
                                                <xsl:value-of select="$tableresultslocale/columns/modificator/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'created_on' ">
                                                <xsl:value-of select="$tableresultslocale/columns/creationdate/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'modified_on' ">
                                                <xsl:value-of select="$tableresultslocale/columns/modificationdate/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:when test="name() = 'status' ">
                                                <xsl:value-of select="$tableresultslocale/columns/status/option[@lang=$lang]"/>
                                            </xsl:when>
                                            <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
                                        </xsl:choose>
                                    </strong>
                                </td>
                            </xsl:for-each>
                            <td>
                                <strong>
                                    <xsl:value-of select="$tableresultslocale/columns/actions/option[@lang=$lang]"/>
                                </strong>
			    </td>
                        </xsl:otherwise>
                    </xsl:choose>
                </tr>
                <xsl:variable name="trsSeparator" select="//data/@translationsSeperator"/>
                <xsl:for-each select="//data/terms/term[./descriptor != '' ]">
                    <xsl:variable name="currentJS0">
                        <xsl:call-template name="replace-string">
                            <xsl:with-param name="text" select="./descriptor"/>
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
                                    <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of  select="./descriptor"/>
                            </a>
                        </td>
                        <xsl:if test="count($outputVar/translations)!=0">
                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./translations"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>                                
                        </xsl:if>
                        <xsl:if test="count($outputVar/bt)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./bt"/>
                                    <xsl:with-param name="popUpCard"><xsl:text>TERM</xsl:text></xsl:with-param>
                                </xsl:call-template>                                
                        </xsl:if>
                        <xsl:if test="count($outputVar/nt)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./nt"/>
                                    <xsl:with-param name="popUpCard"><xsl:text>TERM</xsl:text></xsl:with-param>
                                </xsl:call-template>                                
                        </xsl:if>
                        <xsl:if test="count($outputVar/topterm)!=0">                                
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./topterm"/>
                                    <xsl:with-param name="popUpCard"><xsl:text>TERM</xsl:text></xsl:with-param>
                                </xsl:call-template>                                
                        </xsl:if>
                        
                        <xsl:if test="count($outputVar/rt)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./rt"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>TERM</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/uf)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./uf"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/uf_translations)!=0">
                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./uf_translations"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>
                                <!--
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./uf_translations"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                -->
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/tc)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./tc"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/scope_note)!=0">   
                                <!--<td width="400" style="WORD-BREAK:BREAK-ALL;">-->
                                <td>
                                    <xsl:value-of disable-output-escaping="yes" select="./scope_note"/>
                                </td>                             
                                   <!-- <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param disable-output-escaping="yes" name="nodeSet" select="./scope_note"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>   -->
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/translations_scope_note)!=0">                                
                                <xsl:call-template name="drawTranslationTd">
                                    <xsl:with-param name="nodeSet" select="./translations_scope_note"/>
                                    <xsl:with-param name="translationSeparator" select="$trsSeparator"/>
                                </xsl:call-template>
                                    <!--<xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./translations_scope_note"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                      -->
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/facet)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./facet"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>FACET</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/primary_found_in)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./primary_found_in"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>SOURCE</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/translations_found_in)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./translations_found_in"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>SOURCE</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                           
                            <xsl:if test="count($outputVar/created_by)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./created_by"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            <xsl:if test="count($outputVar/modified_by)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./modified_by"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/created_on)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./created_on"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/modified_on)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./modified_on"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/status)!=0">                                
                                    <xsl:call-template name="drawAttributeTd">
                                        <xsl:with-param name="nodeSet" select="./status"/>
                                        <xsl:with-param name="popUpCard"><xsl:text>false</xsl:text></xsl:with-param>
                                    </xsl:call-template> 
                            </xsl:if>
                                                  
                        <td align="center" width="10%">
				
                            <a href="#">
                                <img width="16" height="16" border="0">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$tableresultslocale/hierarchicalimage/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$tableresultslocale/hierarchicalimage/title/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="onClick">    
                                        <xsl:text>prepareResults('SearchResults_Terms_Hierarchical','</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>','','true');</xsl:text>
                                    </xsl:attribute>
                                </img>
                            </a>    
                            <a href="#">
                                <img width="16" height="16" border="0" >
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$tableresultslocale/graphicalimage/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$tableresultslocale/graphicalimage/title/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="onClick">
                                        <xsl:text>GraphicalViewIconPressed('GraphicalView', '</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>', "DESCRIPTOR","false")</xsl:text>
                                    </xsl:attribute>
                                </img>
                            </a>

                            <!-- DISABLE editing in case of user of group READER -->
                            <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'">
                                <a href="#">
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$tableresultslocale/editimage/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$tableresultslocale/editimage/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditCard_Term('</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>')</xsl:text>
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
