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
    Document   : PopUpInfo_Facet.xsl
    Created on : 31 Μάρτιος 2009, 6:12 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../Configs.xsl"/>
    <xsl:output method="html"/>
    
    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="CSS/xml_thes.css?v=@DeploymentTimestamp@"/>
                <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>
				<!--<xsl:attribute name="href"><xsl:value-of select="'CSS/xml_thes.css?v=@DeploymentTimestamp@'"/></xsl:attribute>
			</link>-->
                <title>PopUpInfo_Facet.xsl</title>
            </head>
            <body>
                <div class="popUpCard">
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Facet">
                        <xsl:with-param name="showClose">
                            <xsl:text>true</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                    <div class="marginfiller"/>
                </div>
            </body>
        </html>
    </xsl:template>

    
    <xsl:template name="PopUp_Or_EditCard_Of_Facet">
        <xsl:param name="showClose"/>
        <xsl:variable name="facetcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/facet"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:variable name="showReferenceUri" select="/page/@showReferenceURI"/>
        <fieldset >
            <legend style="margin-bottom:5px;">
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b><xsl:value-of select="//facetName"/></b> </legend>
            <table width="100%">
                <xsl:choose>
                    <xsl:when test="$showClose = 'true'">
                        <tr width="100%">
                            <td colspan="2" align="right" width="100%">
                                <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                                <!-- Tropopoihsh -->
                                <xsl:if test="$THEMASUserInfo_userGroup != 'READER' and $THEMASUserInfo_userGroup != 'EXTERNALREADER' and $THEMASUserInfo_userGroup != 'LIBRARY'">
                                    <xsl:variable name="Slash">\</xsl:variable>
                                    <xsl:variable name="SlashJS">\\</xsl:variable>
                                    <xsl:variable name="Apos">'</xsl:variable>
                                    <xsl:variable name="AposJS">\'</xsl:variable>
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                            <xsl:with-param name="text" select="//facetName"/>
                                            <xsl:with-param name="replace" select="$Slash"/>
                                            <xsl:with-param name="with" select="$SlashJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos"/>
                                            <xsl:with-param name="with" select="$AposJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditCard_Facet('</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>')</xsl:text>
                                        </xsl:attribute>
                                    <i>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/edittext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </i>
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                        
                                    </img>
                                    </a>
                                    &#160;&#160;&#160;&#160;&#160;&#160;
                                </xsl:if>
                                <!-- Kleisimo -->
                                <a href="#" onclick="document.getElementById('DisplayCardArea').innerHTML='';DisplayPleaseWaitScreen(false);">
                                    <i>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/closetext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </i>&#160;[x]
                                </a>
                            </td>
                        </tr>
                    </xsl:when>
                </xsl:choose>
                <!--</table>
                <table style="padding-right:20px; padding-left:20px; ">-->
                <xsl:choose>
                    <xsl:when test="//errorMsg ">
                        <tr>
                            <td colspan="2">
                                <xsl:value-of select="//errorMsg"/>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- Display info of hierarchy-->
                        <xsl:variable name="Slash">\</xsl:variable>
                        <xsl:variable name="SlashJS">\\</xsl:variable>
                        <xsl:variable name="Apos">'</xsl:variable>
                        <xsl:variable name="AposJS">\'</xsl:variable>
                        
                        <tr>
                            <td class="displayFacetFirstCol">
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/facetlabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </b>
                                </span>
                            </td>
                            <td class="displayFacetSecondCol">
                                <span class="headerThes_normal"><xsl:value-of select="//facetName"/></span>
                            </td>
                        </tr>
                        <xsl:if test="$showReferenceUri='yes' and //facets/facet/name/@referenceId[.!='']">
                        <tr valign="top">
                            <td class="displayFacetFirstCol">
                                <br/>                       
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$facetcardlocale/refId/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>       
                                    </b>
                                </span>
                            </td>
                            <td class="displayFacetSecondCol">
                                <br/>                       
                               <span class="headerThes_normal">
                                                        <xsl:value-of select="//facets/facet/name/@referenceId"/>
                                                    </span> 
                                                    &#160;&#160;&#160;
                                                    
                                                    <a id="refIdLink" style="cursor: pointer;">
                                                        <!--
                                                        <xsl:attribute name="href">
                                                            <xsl:value-of select="$currentNode/ReferenceUri/text()"/>
                                                        </xsl:attribute>-->
                                                        <xsl:attribute name="onclick">
                                                            <xsl:text>copyToClipborad('refIdTooltipLink','refIdTooltipTxt', '</xsl:text>
                                                             <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$facetcardlocale/refIdToolTipPrefix/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                            </xsl:call-template>
                                                            <xsl:text>','</xsl:text>
                                                            <xsl:value-of select="//facets/facet/ReferenceUri/text()"/>
                                                            <xsl:text>');</xsl:text>
                                                        </xsl:attribute>
                                                        <!--<i>Copy Link</i>-->
                                                        <img src="images/link32.png" width="14" height="14" border="0" style="margin-left:5px;"/>
                                                    </a> 
                                                    <!-- display: none; --> 
                                                    <div id="refIdTooltipLink" class="referenceUriTooltip">                                                    
                                                        <div style="margin:5px" id="refIdTooltipTxt">
                                                            
                                                        </div>
                                                    </div>
                                                    
                            </td>
                        </tr>
                        </xsl:if>
                        <tr valign="top">
                            <td class="displayFacetFirstCol">
                                <br/>
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/hierarchieslabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </b>
                                </span>
                            </td>
                            <td class="displayFacetSecondCol">
                                
                                <xsl:choose>
                                    <xsl:when test="count(//hierarchy)=0">
                                        <xsl:text> - </xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="//hierarchy">
                                            <xsl:variable name="currentHierJS0">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                                    <xsl:with-param name="text" select="."/>
                                                    <xsl:with-param name="replace" select="$Slash"/>
                                                    <xsl:with-param name="with" select="$SlashJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:variable name="currentHierJS">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                                    <xsl:with-param name="text" select="$currentHierJS0"/>
                                                    <xsl:with-param name="replace" select="$Apos"/>
                                                    <xsl:with-param name="with" select="$AposJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                           <a href="#">
                                                <xsl:attribute name="onClick">
                                                    <xsl:text>popUpCard('</xsl:text>
                                                    <xsl:value-of select="$currentHierJS"/>
                                                    <xsl:text>','CardOf_Hierarchy')</xsl:text>                                                            
                                                </xsl:attribute>
                                                <span class="headerThes_normal"><xsl:value-of select="."/></span>
                                            </a>
                                           <br/>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                              
                            </td>
                        </tr>
                        <tr valign="middle">
                            <td class="displayFacetFirstCol">
                                <br/>
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/showhierslabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </b>
                                </span>
                            </td>
                            <td class="displayFacetSecondCol">
                                <br/>
                                <xsl:variable name="currentJS0">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                        <xsl:with-param name="text" select="//facetName"/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
                                        <xsl:with-param name="text" select="$currentJS0"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    
                                    <xsl:attribute name="onclick">
                                        <xsl:text>GraphicalViewIconPressed('GraphicalView','</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>', "FACET","false")</xsl:text>
                                    </xsl:attribute>
                                    
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/graphicalimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/graphicalimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                    <span class="headerThes_normal" style="vertical-align:top;">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/graphicalimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </span>
                                    
                                </a>
                                <br/>
                                 <a href="#">
                                     <xsl:attribute name="onClick">
                                        <xsl:text>prepareResults('hierarchysTermsShortcuts','</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','facethierarchical','true');</xsl:text>
                                    </xsl:attribute>
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/hierarchical/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/hierarchical/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        
                                    </img>
                                    <span class="headerThes_normal" style="vertical-align:top;">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/hierarchical/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </span>
                                </a>
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </table>
        </fieldset>
    </xsl:template>
    
    <xsl:template name="showEditActions_Facet">
        <xsl:variable name="facetcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/facet"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:param name="facetName"/>
        <div id="EditCardArea"/>
        <xsl:variable name="THEMASUserGroup" select="//page/THEMASUserInfo/userGroup "/>
        <!--<xsl:if test="$THEMASUserGroup = 'LIBRARY' ">-->
            <table cellspacing="15" width="100%" style="text-align:left; position:absolute; top:415px; left:0px;">
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/generalprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            <!--<xsl:value-of select="$facetName"/>-->
                        </b>
                    </td>
                </tr>
                <tr>
                    <td width="30%" valign="top">
                        <table cellpadding="2">
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$facetName"/><xsl:text>','facet_rename','EditDisplays_Facet');</xsl:text>
                                        </xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/rename/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$facetName"/><xsl:text>','delete_facet','EditDisplays_Facet');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/delete/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        <!--</xsl:if>-->
    </xsl:template>
    
    
    <xsl:template name="replace-string-for-PopUpInfo_Facet">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string-for-PopUpInfo_Facet">
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
    
    
</xsl:stylesheet>
