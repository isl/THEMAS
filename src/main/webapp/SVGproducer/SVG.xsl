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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <!-- TargetKind = ('DESCRIPTOR' or 'HIERARCHY' or 'FACET') -->
    <xsl:import href="../xml-xsl/Configs.xsl"/>
    <xsl:include href="../xml-xsl/utilities.xsl"/>
    <xsl:variable name="TargetKind" select="//page/TargetKind"/>
    <xsl:variable name="TargetHasBTTerms" select="count(//page/targetBTterms/name_for_display) != 0"/>
    <xsl:variable name="locale" select="document('../translations/translations.xml')/locale/svgproducer"/>
    <xsl:variable name="lang" select="//page/@language"/>

    <!-- _____________________________________________________________________________
              TEMPLATE: starting template for transforming the XML code produced by GraphicalView servlet
    _____________________________________________________________________________ -->
    <xsl:template match="/">
        <xsl:variable name="Slash">\</xsl:variable>
        <xsl:variable name="SlashJS">\\</xsl:variable>
        <xsl:variable name="Apos">'</xsl:variable>
        <xsl:variable name="AposJS">\'</xsl:variable>
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
                <!-- page title -->
                <title>
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$locale/title/titlepart1/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'no'"/> 
                    </xsl:call-template>
                    <xsl:if test="$TargetKind = 'DESCRIPTOR' ">
                        <xsl:if test="$TargetHasBTTerms">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/title/titlepart2a/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:if>
                        <xsl:if test="not($TargetHasBTTerms)">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/title/titlepart2b/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="$TargetKind = 'HIERARCHY' ">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$locale/title/titlepart2c/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </xsl:if>
                    <xsl:if test="$TargetKind = 'FACET' ">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$locale/title/titlepart2d/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </xsl:if>
                    <xsl:value-of select="//page/TargetNameWithoutPrefix"/>
                </title>
                <!-- css -->
                <link rel="stylesheet" type="text/css" href="SVGproducer/SVG.css?v=@DeploymentTimestamp@"/>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"/>
                <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
                <script language="JavaScript">
                    <xsl:attribute name="src">Javascript/graphicalView.js?v=@DeploymentTimestamp@</xsl:attribute>
                </script>
            </head>
            <body>
                <!-- titles -->
                <xsl:if test="$TargetKind = 'HIERARCHY' or ($TargetKind = 'DESCRIPTOR' and not($TargetHasBTTerms) ) ">
                    <xsl:call-template name="DisplayTitleForHierarchy">
                        <xsl:with-param name="locale" select="$locale"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="$TargetKind = 'FACET' ">
                    <xsl:call-template name="DisplayTitleForFacet">
                        <xsl:with-param name="locale" select="$locale"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </xsl:if>				
                <!-- ευρύτεροι όροι του target -->
                <xsl:if test="$TargetKind = 'DESCRIPTOR' and $TargetHasBTTerms">
                    <table>
                        <tr>
                            <td class="CategName">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/btsofterm/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                                <b>
                                    <xsl:value-of select="//page/TargetNameWithoutPrefix"/>
                                </b>
                                <xsl:text>: </xsl:text>
                                <xsl:for-each select="//page/targetBTterms/name_for_display">
                                    <!--these chars must be replaced because they will cause javascript problems-->
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string">
                                            <xsl:with-param name="text" select="."/>
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
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>GraphicalViewIconPressed('GraphicalView', '</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>', "DESCRIPTOR","false")</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </a>
                                    <xsl:if test=". != ../name_for_display[last()] ">
                                        <xsl:text>, </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </table>
                </xsl:if>

                <!-- horizontal line-->
                <hr/>								
                <!-- SVG graph -->
                <embed name="DocSVG" type="image/svg+xml" style="height:82%;width:100%">
                    <xsl:attribute name="src">
                        <xsl:value-of select="//page/svgFileName"/>
                    </xsl:attribute>
                </embed>				
                <!-- horizontal line-->
                <hr/>	
                <!-- legend with colours -->
                <xsl:if test="$TargetKind = 'DESCRIPTOR' or $TargetKind = 'HIERARCHY' ">
                    <xsl:call-template name="DisplayLegend_WithColours">
                        <xsl:with-param name="locale" select="$locale"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </xsl:if>

            </body>
        </html>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: DisplayLegend_WithColours
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayLegend_WithColours">
        <xsl:param name="locale"/>
        <xsl:param name="lang"/>
        <table>
            <tr>
                <xsl:for-each select="//page/legend_data/categ">
                    <td>
                        <xsl:attribute name="class">
                            <xsl:value-of select="./style"/>
                        </xsl:attribute>
                    </td>
                    <td class="CategName">
                        <xsl:choose>
                            <xsl:when test="./name = 'NT' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/nt/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'UF' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/uf/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'ALT' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/alt/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'uf_translation' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/uf_translations/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'uk_alt' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/uk_alt/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'dewey' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/tc/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'translation' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/translations/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:when test="./name = 'RT' ">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/rt/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="./name"/>:
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </xsl:for-each>
            </tr>
        </table>
    </xsl:template>				
    <!-- _____________________________________________________________________________
              TEMPLATE: DisplayTitleForHierarchy
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayTitleForHierarchy">
        <xsl:param name="locale"/>
        <xsl:param name="lang"/>
        <table>
            <tr>
                <td class="CategName">
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$locale/title/displaytitleforhierarchy/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'no'"/> 
                    </xsl:call-template>    
                    <b>
                        <xsl:value-of select="//page/TargetNameWithoutPrefix"/>
                    </b>
                </td>
            </tr>
        </table>
    </xsl:template>						
    <!-- _____________________________________________________________________________
              TEMPLATE: DisplayTitleForFacet
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayTitleForFacet">
        <xsl:param name="locale"/>
        <xsl:param name="lang"/>
        <table>
            <tr>
                <td class="CategName">
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$locale/title/displaytitleforfacet/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'no'"/> 
                    </xsl:call-template>    
                    <b>
                        <xsl:value-of select="//page/TargetNameWithoutPrefix"/>
                    </b>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
