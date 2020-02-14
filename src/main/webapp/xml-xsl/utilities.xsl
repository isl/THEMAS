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
    Document   : Utilities.xsl
    Created on : November 25, 2008, 6:11 PM
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">

   
    
    <xsl:template name="replace-string">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string">
                    <xsl:with-param name="text" select="substring-after($text,$replace)"/>
                    <xsl:with-param name="replace" select="$replace"/>
                    <xsl:with-param name="with" select="$with"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="drawTranslationTd">
        <xsl:param name="nodeSet"/>
        <xsl:param name="translationSeparator"/>
    
        <xsl:variable name="howmanyFields" select="count($nodeSet[text()!=''])"/>
        <xsl:choose>
            <xsl:when test="$howmanyFields=0">
                <td>-</td>
            </xsl:when>    
            <xsl:otherwise>
                <td>
                    <xsl:for-each select="$nodeSet[text()!='']/node()">
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
                </td>
            </xsl:otherwise>

        </xsl:choose>
   
    </xsl:template>

    <xsl:template name="drawAttributeTd">
        <xsl:param name="nodeSet"/>
        <xsl:param name="popUpCard"/>
        
        <xsl:variable name="howmanyFields" select="count($nodeSet)"/>

        <xsl:variable name="Slash">\</xsl:variable>
        <xsl:variable name="SlashJS">\\</xsl:variable>
        <xsl:variable name="Apos">'</xsl:variable>
        <xsl:variable name="AposJS">\'</xsl:variable>
        <!--<xsl:value-of select="$howmanyFields"/>-->
        <td>
            <xsl:choose>
                <xsl:when test="$howmanyFields=0">-</xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="$nodeSet/node()"> 
                        <xsl:choose>
                            <xsl:when test="$popUpCard = 'TERM'">
                                <xsl:variable name="currentJS1">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="."/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS2">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="$currentJS1"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS2"/>
                                        <xsl:text>','CardOf_Term')</xsl:text>
                                    </xsl:attribute> 
                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:when test="$popUpCard = 'HIERARCHY'">
                                <xsl:variable name="currentJS1">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="."/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS2">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="$currentJS1"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS2"/>
                                        <xsl:text>','CardOf_Hierarchy')</xsl:text>
                                    </xsl:attribute> 

                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:when test="$popUpCard = 'FACET'">
                                <xsl:variable name="currentJS1">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="."/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS2">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="$currentJS1"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS2"/>
                                        <xsl:text>','CardOf_Facet')</xsl:text>
                                    </xsl:attribute> 

                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:when test="$popUpCard = 'SOURCE'">
                                <xsl:variable name="currentJS1">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="."/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS2">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="$currentJS1"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS2"/>
                                        <xsl:text>','CardOf_Source')</xsl:text>
                                    </xsl:attribute> 

                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="name(..)='status'">
                                        <xsl:value-of select="."/>                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:value-of select="."/>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="position()!= $howmanyFields">
                                <xsl:text>,</xsl:text>
                                <br/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template> 
</xsl:stylesheet>
