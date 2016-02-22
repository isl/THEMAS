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

<xsl:stylesheet xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" version="2.0">
	<xsl:template name="paging">
		<xsl:param name="step"/>
		<xsl:param name="start"/>
		<xsl:param name="max"/>
		<xsl:param name="letter"></xsl:param>
		<xsl:param name="count"></xsl:param>
		<xsl:param name="title"></xsl:param>
		<xsl:variable name="pre_tip">
			<xsl:choose>
				<xsl:when test="($step > $start) or ($step > $max)">1 - <!--<xsl:value-of select="$step"/>-->
				</xsl:when>
				<xsl:otherwise>
					<!--<xsl:value-of select="$start - $step "/> - <xsl:value-of select="$max - $step"/>-->
					<xsl:value-of select="$start - $step"/> - </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="pre_start">
			<xsl:choose>
				<xsl:when test="$start - $step > 1">
					<xsl:value-of select="$start - $step - 1"/>
				</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="next_tip">
			<xsl:value-of select="$max + 1"/> - <!--<xsl:value-of select="$max + $step"/>-->
		</xsl:variable>
		
			<xsl:if test="$start>1">
				<!--<a href="{$title}?start={$pre_start}&amp;letter={$letter}">-->
					<img src="images/previous.jpg" title="{$pre_tip}" alt="{$pre_tip}" class="img_link"  border="0" onClick="submitFormTo('search{$title}', '{$title}?start={$pre_start}&amp;letter={$letter}','href');return false;">
									
										 </img>&#160; <!--</a>-->
			</xsl:if>
			<xsl:if test="$max &lt; $count">
			<!--	<a href="{$title}?start={$max}&amp;letter={$letter}&amp;No_results=10">-->
					<img src="images/next.jpg" title="{$next_tip}" alt="{$next_tip}" class="img_link" border="0" onClick="submitFormTo('search{$title}', '{$title}?start={$max}&amp;letter={$letter}','href');return false;"/>
				<!--</a>-->
			</xsl:if>
		
	</xsl:template>
</xsl:stylesheet>
