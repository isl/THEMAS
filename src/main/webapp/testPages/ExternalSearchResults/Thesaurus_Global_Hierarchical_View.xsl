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
    Document   : SaveAll_Terms_Of_Hierarchy_Hierarchical.xsl
    Created on : 23 Ιανουάριος 2009, 10:30 πμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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
		<xsl:variable name="numOfIndent">0</xsl:variable> 
		<xsl:variable name="lang" select="/page/@language"/>
		<xsl:variable name="pageTitle">
			<xsl:choose>
				<xsl:when test="count(/page/targetFacet[./text()!='']) != 0 ">
							<xsl:choose>
								<xsl:when test="$lang='el'">
									<xsl:text>Τυπική Ιεραρχική Παρουσίαση του Μικροθησαυρού: </xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>Hierarchical Presentation of Facet: </xsl:text>
								</xsl:otherwise>
							</xsl:choose>                            
							
                            <xsl:value-of select="/page/targetFacet"/>
                            <!--<xsl:text> </xsl:text>
                            <xsl:value-of select="/page/title"/>-->
                         </xsl:when>
                         <xsl:otherwise>
                              <xsl:choose>
								<xsl:when test="$lang='el'">
									<xsl:text>Τυπική Ιεραρχική Παρουσίαση του Θησαυρού: </xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>Hierarchical Presentation of Thesaurus: </xsl:text>
								</xsl:otherwise>
							</xsl:choose>                            

                            <xsl:value-of select="/page/targetThesaurus"/>
                            <!--<xsl:text> </xsl:text>
                            <xsl:value-of select="/page/title"/>-->
                         </xsl:otherwise>                         
                     </xsl:choose>
    </xsl:variable>
             
            <xsl:choose>
                <xsl:when test="count(//topterm)=0 ">
                    <table><tr>
                    <td align="left" valign="top" colspan="5">
                        <strong>
                            <xsl:value-of select="$localespecific/noterms/option[@lang=$lang]"/>  
                        </strong>
                    </td>
                    </tr></table>
            </xsl:when>
            <xsl:otherwise>
                <h2>
                    <xsl:value-of select="$pageTitle"/>
                </h2>
                <br/>
                <xsl:for-each select="//topterm">
				
				<br/>
				<br/>
                <strong ><span class="row"><xsl:value-of select="./name"/></span></strong>
				
                <xsl:call-template name="list-nts">
                    <xsl:with-param name="node" select="."/>
                    <xsl:with-param name="howmany" select="$numOfIndent+1" ></xsl:with-param>
                </xsl:call-template>
				</xsl:for-each>
            </xsl:otherwise>
            </xsl:choose>
            
    </xsl:template>
    
    <xsl:template name="list-nts">
        <xsl:param name="node"/>
        <xsl:param name="howmany"/>
        
        <xsl:for-each select="$node[1]/nt">
            <!-- <xsl:sort select="."/> -->
            <span class="row"><br/>
            
                <xsl:call-template name="draw-indent">
                    <xsl:with-param name="i" >0</xsl:with-param>
                    <xsl:with-param name="maxTimes" select="$howmany"/>
                 </xsl:call-template>
                 <xsl:variable name="currentNode" select="."/>
                 <xsl:choose>
                     <xsl:when test="$howmany=1">
                         
                             <xsl:value-of select="."/>
                         
                     </xsl:when>
                     <xsl:otherwise>
                         <span style="font-style:italic;">
                            <xsl:value-of select="."/>
                        </span>
                     </xsl:otherwise>
                 </xsl:choose>
                 
                 <xsl:if test="count(//term[./name=$currentNode]/nt) >0 ">
                     <xsl:call-template name="list-nts">
                        <xsl:with-param name="node" select="//term[./name=$currentNode]" />
                        <xsl:with-param name="howmany" select="$howmany+1" />
                     </xsl:call-template>

                 </xsl:if>
             </span>
        </xsl:for-each>      
    </xsl:template>
    
    <xsl:template name="draw-indent">
        <xsl:param name="i"/>
        <xsl:param name="maxTimes"/>
        <xsl:variable name="tabModule">--&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:variable>
        <xsl:if test="$i &lt; $maxTimes">
            <xsl:value-of select="$tabModule"/>            
            <xsl:call-template name="draw-indent">
                <xsl:with-param name="i" select="$i + 1"/>
                <xsl:with-param name="maxTimes" select="$maxTimes"/>
            </xsl:call-template>     
        </xsl:if>
            
    </xsl:template>
        
</xsl:stylesheet>

