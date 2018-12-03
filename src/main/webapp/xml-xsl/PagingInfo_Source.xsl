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
    Document   : PagingInfo_Source.xsl
    Created on : 29 Απρίλιος 2009, 2:56 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    
    <xsl:template  name="DisplayStatisticsAndPagingInfo_Sources">
        <xsl:param name="paginglocale"/>
        <xsl:variable name="ServletName" select="//results/paging_info/ServletName"/>
        <xsl:variable name="query_results_time" select="//results/paging_info/query_results_time"/>
        <xsl:variable name="query_results_count" select="//results/paging_info/pagingQueryResultsCount"/>
        <xsl:variable name="pagingListStep" select="//results/paging_info/pagingListStep"/>
        <xsl:variable name="pagingFirst" select="//results/paging_info/pagingFirst"/>
        <xsl:variable name="pagingLast" select="//results/paging_info/pagingLast"/>
        <xsl:variable name="columncount" select="count(//output/node()) + 2"/>
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        
        <tr width="100%">
            <!-- _____________ row with statistics _____________ -->
            <!-- <td class="resultRow" align="center" colspan="5"> -->
            <td class="PagingInfo">
                <xsl:attribute name="colspan"><xsl:value-of select="$columncount"/></xsl:attribute>
                <b>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </b>
                <xsl:if test="$query_results_count = 0"> 
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </xsl:if>
                <xsl:if test="$query_results_count > 1">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart3/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    <xsl:value-of select="$query_results_count"/>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart4/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </xsl:if>
                <xsl:if test="$query_results_count = 1">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart5/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    <xsl:value-of select="$query_results_count"/>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart6/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </xsl:if>

                <b>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart7/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </b>
                <xsl:value-of select="$pagingFirst"/> - <xsl:value-of select="$pagingLast"/>
                <b> 
                     <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart8/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </b>
                <xsl:value-of select="ceiling($pagingLast div $pagingListStep)"/> /
                <xsl:value-of select="ceiling($query_results_count div $pagingListStep)"/>
                <xsl:text> </xsl:text>&#160;&#160;&#160;&#160;
                <xsl:call-template name="SearchResultsPaging_Sources">
                    <xsl:with-param name="ServletName" select="$ServletName"/>
                    <xsl:with-param name="pagingQueryResultsCount" select="$query_results_count"/>
                    <xsl:with-param name="pagingListStep" select="$pagingListStep"/>
                    <xsl:with-param name="pagingFirst" select="$pagingFirst"/>
                    <xsl:with-param name="pagingLast" select="$pagingLast"/>
                    <xsl:with-param name="paginglocale" select="$paginglocale" />
                </xsl:call-template>
                &#160;&#160;&#160;&#160; 
                <a href="#">
                    <xsl:attribute name="onClick">prepareResults('SearchResults_Sources','','','true');</xsl:attribute>
                    <img  height="16" width="16" border="0" class="img_link">
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/saveimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/saveimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                    </img>
                </a>&#160;
                <a href="#">
                    <xsl:attribute name="onClick">downloadFile('SearchResults_Sources','','','true');</xsl:attribute>
                    <img  height="16" width="16" border="0"  class="img_link" >
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/savexmlimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/savexmlimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                    </img>
                </a>
                <hr></hr>
            </td>
        </tr>
    </xsl:template>


    <xsl:template name="SearchResultsPaging_Sources">
        <xsl:param name="ServletName"/>
        <xsl:param name="pagingQueryResultsCount"/>
        <xsl:param name="pagingListStep"/>
        <xsl:param name="pagingFirst"/>
        <xsl:param name="pagingLast"/>
        <xsl:param name="paginglocale"/>
        
        
        <!-- previous ARROW alt message -->
        <xsl:variable name="pre_tip">
            <xsl:value-of select="$pagingFirst - $pagingListStep"/><xsl:text> - </xsl:text><xsl:value-of select="$pagingFirst - 1"/>
        </xsl:variable>
        
	<!-- previous ARROW servlet calling parameter -->
        <xsl:variable name="pre_start">
            <xsl:value-of select="$pagingFirst - $pagingListStep"/>
        </xsl:variable>
        <xsl:variable name="next_tip_last_value">
            <xsl:choose>
                <xsl:when test=" $pagingQueryResultsCount >= ($pagingLast + $pagingListStep)">
                    <xsl:value-of select="$pagingLast + $pagingListStep"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$pagingQueryResultsCount"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
	<!-- end ARROW servlet calling parameter -->
        <xsl:variable name="end_arrow_parameter">
            <xsl:value-of select="(ceiling($pagingQueryResultsCount div $pagingListStep) - 1) * $pagingListStep + 1"/>
        </xsl:variable>	
        
	<!-- next ARROW alt message -->
        <xsl:variable name="next_tip">
            <xsl:value-of select="$pagingLast + 1"/><xsl:text> - </xsl:text><xsl:value-of select="$next_tip_last_value"/>
        </xsl:variable>
        
	<!-- previous and start ARROW -->
        <xsl:if test="$pagingFirst > 1">
            <!-- start ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=1</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>DisplayPleaseWaitScreen(true);</xsl:text>
                </xsl:attribute>
                <img src="images/paging/start.gif" width="14" height="14" class="img_link" border="0">
                    <xsl:attribute name="title">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart9/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </xsl:attribute>
                </img>
            </a>			
            <!-- previous ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=</xsl:text>
                    <xsl:value-of select="$pre_start"/>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>DisplayPleaseWaitScreen(true);</xsl:text>
                </xsl:attribute>
                <img src="images/paging/prev.gif" width="14" height="14" title="{$pre_tip}" alt="{$pre_tip}" class="img_link" border="0"/>
            </a>
        </xsl:if>
         
        <!-- next and end ARROW -->
        <xsl:if test="$pagingLast &lt; $pagingQueryResultsCount">
            <!-- next ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=</xsl:text>
                    <xsl:value-of select="$pagingLast + 1"/>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>DisplayPleaseWaitScreen(true);</xsl:text>
                </xsl:attribute>
                <img src="images/paging/next.gif" width="14" height="14" title="{$next_tip}" alt="{$next_tip}" class="img_link" border="0"/>
            </a>
            <!-- end ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=</xsl:text>
                    <xsl:value-of select="$end_arrow_parameter"/>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>DisplayPleaseWaitScreen(true);</xsl:text>
                </xsl:attribute>
                <img src="images/paging/end.gif" width="14" height="14" class="img_link" border="0">
                    <xsl:attribute name="title">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/statisticspart10/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </xsl:attribute>
                </img>
            </a>
        </xsl:if>
        
	<!-- go to specific page - part -->
        <xsl:if test="$pagingQueryResultsCount &gt; $pagingListStep">
            &#160;&#160;<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/pageinputprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            
            <!-- input for specific page number -->
            <input id="go_to_specific_page_input_source" name="go_to_specific_page_input_source" style="font-size: 8pt; width: 25pt">
                <xsl:attribute name="onKeyPress">
                    <xsl:text>if(event.keyCode == 13) {	DisplayPleaseWaitScreen(true); checkPageNumber('SearchResults_Sources', '</xsl:text>
                    <xsl:value-of select="$pagingListStep"/>
                    <xsl:text>',document.getElementById('go_to_specific_page_input_source').value);}</xsl:text>
                </xsl:attribute>
            </input>&#160;
            <input class="button" type="button" style="font-size: 8pt;" >
                <xsl:attribute name="value">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$paginglocale/pageinput/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </xsl:attribute>
                <xsl:attribute name="onClick">
                    <xsl:text>DisplayPleaseWaitScreen(true);checkPageNumber('SearchResults_Sources', '</xsl:text>
                    <xsl:value-of select="$pagingListStep"/>
                    <xsl:text>',document.getElementById('go_to_specific_page_input_source').value);</xsl:text>
               </xsl:attribute>
            </input>
        </xsl:if>
    </xsl:template>
    
    
</xsl:stylesheet>
