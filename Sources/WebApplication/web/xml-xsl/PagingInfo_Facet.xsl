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
    Document   : PagingInfo_Facet.xsl
    Created on : 11 Ιούλιος 2008, 11:29 πμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template  name="DisplayStatisticsAndPagingInfo_Facets">
        <xsl:param name="paginglocale" />
        <xsl:variable name="ServletName" select="//results/paging_info/ServletName"/>
        <xsl:variable name="query_results_time" select="//results/paging_info/query_results_time"/>
        <xsl:variable name="query_results_count" select="//results/paging_info/pagingQueryResultsCount"/>
        <xsl:variable name="pagingListStep" select="//results/paging_info/pagingListStep"/>
        <xsl:variable name="pagingFirst" select="//results/paging_info/pagingFirst"/>
        <xsl:variable name="pagingLast" select="//results/paging_info/pagingLast"/>
        <xsl:variable name="columncount" select="count(//output/node()) + 2"/>
        <tr width="100%">
            <!-- _____________ row with statistics _____________ -->
            <!-- <td class="resultRow" align="center" colspan="5"> -->
            <td class="PagingInfo">
                <xsl:attribute name="colspan"><xsl:value-of select="$columncount"/></xsl:attribute>
                <b>
                    <xsl:value-of select="$paginglocale/statisticspart1/option[@lang=$lang]"/>
                </b>
                <xsl:if test="$query_results_count = 0">
                    <xsl:value-of select="$paginglocale/statisticspart2/option[@lang=$lang]"/>
                </xsl:if>
                <xsl:if test="$query_results_count > 1">
                    <xsl:value-of select="$paginglocale/statisticspart3/option[@lang=$lang]"/>
                    <xsl:value-of select="$query_results_count"/>
                    <xsl:value-of select="$paginglocale/statisticspart4/option[@lang=$lang]"/>
                </xsl:if>
                <xsl:if test="$query_results_count = 1">
                    <xsl:value-of select="$paginglocale/statisticspart5/option[@lang=$lang]"/>
                    <xsl:value-of select="$query_results_count"/>
                    <xsl:value-of select="$paginglocale/statisticspart6/option[@lang=$lang]"/>
                </xsl:if>
                <b>
                    <xsl:value-of select="$paginglocale/statisticspart7/option[@lang=$lang]"/>
                </b>
                <xsl:value-of select="$pagingFirst"/> - <xsl:value-of select="$pagingLast"/>
                <b> 
                     <xsl:value-of select="$paginglocale/statisticspart8/option[@lang=$lang]"/>
                </b>
                <xsl:value-of select="ceiling($pagingLast div $pagingListStep)"/> /
                <xsl:value-of select="ceiling($query_results_count div $pagingListStep)"/>
                <xsl:text> </xsl:text>&#160;&#160;&#160;&#160;
                <xsl:call-template name="SearchResultsPaging_Facets">
                    <xsl:with-param name="ServletName" select="$ServletName"/>
                    <xsl:with-param name="pagingQueryResultsCount" select="$query_results_count"/>
                    <xsl:with-param name="pagingListStep" select="$pagingListStep"/>
                    <xsl:with-param name="pagingFirst" select="$pagingFirst"/>
                    <xsl:with-param name="pagingLast" select="$pagingLast"/>
                    <xsl:with-param name="paginglocale" select="$paginglocale" />
                </xsl:call-template>
                &#160;&#160;&#160;&#160; 
                <a href="#">
                    <xsl:attribute name="onClick">prepareResults('SearchResults_Facets','','','true');</xsl:attribute>
                    <img  height="16" width="16" border="0" class="img_link">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$paginglocale/saveimage/src/option[@lang=$lang]"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$paginglocale/saveimage/title/option[@lang=$lang]"/>
                                </xsl:attribute>
                    </img>
                </a>&#160;
                <a href="#">
                    <xsl:attribute name="onClick">downloadFile('SearchResults_Facets','','','true');</xsl:attribute>
                    <img  height="16" width="16" border="0" class="img_link" >
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$paginglocale/savexmlimage/src/option[@lang=$lang]"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$paginglocale/savexmlimage/title/option[@lang=$lang]"/>
                                </xsl:attribute>
                    </img>
                </a>
                    
                <hr></hr>
            </td>
        </tr>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: SearchResultsPaging
			FUNCTION: handles the paging mechanism for the query results
	      _____________________________________________________________________________ -->
    <xsl:template name="SearchResultsPaging_Facets">
        <xsl:param name="ServletName"/>
        <xsl:param name="pagingQueryResultsCount"/>
        <xsl:param name="pagingListStep"/>
        <xsl:param name="pagingFirst"/>
        <xsl:param name="pagingLast"/>
        <xsl:param name="paginglocale"/>
		
        <!-- previous ARROW alt message -->
        <xsl:variable name="pre_tip">
            <xsl:value-of select="$pagingFirst - $pagingListStep"/> - <xsl:value-of select="$pagingFirst - 1"/>
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
            <xsl:value-of select="$pagingLast + 1"/> - <xsl:value-of select="$next_tip_last_value"/>
        </xsl:variable>
        
	<!-- previous and start ARROW -->
        <xsl:if test="$pagingFirst > 1">
            <!-- start ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=1</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">DisplayPleaseWaitScreen(true);</xsl:attribute>
                <!--<xsl:attribute name="onclick">
                    getResults('19','SearchResults_Facets','1');
                </xsl:attribute>-->
                <img src="images/paging/start.gif" width="14" height="14" class="img_link" border="0">
                    <xsl:attribute name="title">
                        <xsl:value-of select="$paginglocale/statisticspart9/option[@lang=$lang]"/>
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
                <xsl:attribute name="onclick">DisplayPleaseWaitScreen(true);</xsl:attribute>
                <!--<xsl:attribute name="onclick">
                    getResults('19','SearchResults_Facets',<xsl:value-of select="$pre_start"/>);
                </xsl:attribute>-->
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
                <xsl:attribute name="onclick">DisplayPleaseWaitScreen(true);</xsl:attribute>
                <!--<xsl:attribute name="onclick">
                    getResults('19','SearchResults_Facets',<xsl:value-of select="$pagingLast + 1"/>);
                </xsl:attribute>-->
                <img src="images/paging/next.gif" width="14" height="14" title="{$next_tip}" alt="{$next_tip}" class="img_link" border="0"/>
            </a>
	    <!-- end ARROW -->
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$ServletName"/>
                    <xsl:text>?pageFirstResult=</xsl:text>
                    <xsl:value-of select="$end_arrow_parameter"/>
                </xsl:attribute>
                <xsl:attribute name="onclick">DisplayPleaseWaitScreen(true);</xsl:attribute>
                <!--<xsl:attribute name="onclick">
                    getResults('19','SearchResults_Facets',<xsl:value-of select="$end_arrow_parameter"/>);
                </xsl:attribute>-->
                <img src="images/paging/end.gif" width="14" height="14" class="img_link" border="0">
                    <xsl:attribute name="title">
                        <xsl:value-of select="$paginglocale/statisticspart10/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
        </xsl:if>
        
	<!-- go to specific page - part Display only if more than one pages exist -->
        <xsl:if test="$pagingQueryResultsCount &gt; $pagingListStep">
			&#160;&#160;<xsl:value-of select="$paginglocale/pageinputprompt/option[@lang=$lang]"/> 
            <!-- input for specific page number -->
            <input id="go_to_specific_page_input_facet" name="go_to_specific_page_input_facet" style="font-size: 8pt; width: 25pt">
                <xsl:attribute name="onKeyPress">
                    if(event.keyCode == 13) {
						DisplayPleaseWaitScreen(true);
                        checkPageNumber('SearchResults_Facets', '<xsl:value-of select="$pagingListStep"/>',document.getElementById('go_to_specific_page_input_facet').value);
                    }
                </xsl:attribute>
            </input>&#160;
            <input  class="button" type="button" style="font-size: 8pt;" >
                <xsl:attribute name="value">
                    <xsl:value-of select="$paginglocale/pageinput/option[@lang=$lang]"/>
                </xsl:attribute>
                <xsl:attribute name="onClick">
					DisplayPleaseWaitScreen(true);
                    checkPageNumber('SearchResults_Facets', '<xsl:value-of select="$pagingListStep"/>',document.getElementById('go_to_specific_page_input_facet').value);
               </xsl:attribute>
            </input>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
