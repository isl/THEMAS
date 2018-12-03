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
    Document   : PopUpInfo_Hierarchy.xsl
    Created on : 24 Μάρτιος 2009, 3:30 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:output method="html"/>
    <xsl:import href="../Configs.xsl"/>
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
                <title>PopUpInfo_Hierarchy.xsl</title>
            </head>
            <body>
                <div class="popUpCard">
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Hierarchy">
                        <xsl:with-param name="showClose">
                            <xsl:text>true</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                    <div class="marginfiller"/>
                </div>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="PopUp_Or_EditCard_Of_Hierarchy">
        <xsl:param name="showClose"/>
        <xsl:variable name="hierarchycardlocale" select="document('../../translations/translations.xml')/locale/popupcards/hierarchy"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <fieldset>
            <legend style="margin-bottom:5px;">
            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            <b><xsl:value-of select="//current/hierarchy/name"/></b></legend>
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
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                            <xsl:with-param name="text" select="//current/hierarchy/name"/>
                                            <xsl:with-param name="replace" select="$Slash"/>
                                            <xsl:with-param name="with" select="$SlashJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos"/>
                                            <xsl:with-param name="with" select="$AposJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditCard_Hierarchy('</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>')</xsl:text>
                                        </xsl:attribute>
                                    <i> 
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/edittext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </i>
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                        
                                    </img>
                                    </a>
                                                                         &#160;&#160;&#160;&#160;&#160;&#160;
                                </xsl:if>
								<!-- Kleisimo -->
                                <a href="#" onclick="document.getElementById('DisplayCardArea').innerHTML='';DisplayPleaseWaitScreen(false);">
                                    <i>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/closetext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </i>&#160;[x]
                                </a>
                            </td>
                        </tr>
                    </xsl:when>
                </xsl:choose>
                </table>
                <table style="padding-right:20px; padding-left:20px; ">
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
                       
                        <tr valign="top">
                            <td>
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/hierarchylabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </b>
                                </span>
                            </td>
                            <td>
                                <span class="headerThes_normal"><xsl:value-of select="//current/hierarchy/name"/></span>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <br/>
                                <span class="headerThes_normal"><b>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/showtoptermlabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </b></span>
                            </td>
                            <td>
                                <br/>
                                <xsl:variable name="Slash">\</xsl:variable>
                                <xsl:variable name="SlashJS">\\</xsl:variable>
                                <xsl:variable name="Apos">'</xsl:variable>
                                <xsl:variable name="AposJS">\'</xsl:variable>
                                <xsl:variable name="currentJS0">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                        <xsl:with-param name="text" select="//current/hierarchy/name"/>
                                        <xsl:with-param name="replace" select="$Slash" />
                                        <xsl:with-param name="with" select="$SlashJS" />
                                    </xsl:call-template>
                                </xsl:variable>

                                <xsl:variable name="currentJS">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                        <xsl:with-param name="text" select="$currentJS0"/>
                                        <xsl:with-param name="replace" select="$Apos" />
                                        <xsl:with-param name="with" select="$AposJS" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>','CardOf_Term')</xsl:text>
                                    </xsl:attribute> 

                                    <span class="headerThes_normal"><xsl:value-of select="//current/hierarchy/name"/></span>
                                </a>
                              
                            </td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <br/>
                                <span class="headerThes_normal"><b>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/facets/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </b></span>
                            </td>
                            <td>
                                <br/>
                                <xsl:choose>
                                    <xsl:when test="count(//facet/name)=0">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/nofacetsmsg/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="//facet/name">
                                           <xsl:sort select="."/>
                                           <xsl:variable name="Slash">\</xsl:variable>
                                            <xsl:variable name="SlashJS">\\</xsl:variable>
                                            <xsl:variable name="Apos">'</xsl:variable>
                                            <xsl:variable name="AposJS">\'</xsl:variable>
                                            <xsl:variable name="currentFacetJS0">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                                    <xsl:with-param name="text" select="."/>
                                                    <xsl:with-param name="replace" select="$Slash" />
                                                    <xsl:with-param name="with" select="$SlashJS" />
                                                </xsl:call-template>
                                            </xsl:variable>

                                            <xsl:variable name="currentFacetJS">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                                    <xsl:with-param name="text" select="$currentFacetJS0"/>
                                                    <xsl:with-param name="replace" select="$Apos" />
                                                    <xsl:with-param name="with" select="$AposJS" />
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <a href="#">
                                                <xsl:attribute name="onClick">
                                                    <xsl:text>popUpCard('</xsl:text>
                                                    <xsl:value-of select="$currentFacetJS"/>
                                                    <xsl:text>','CardOf_Facet')</xsl:text>
                                                </xsl:attribute> 

                                                <span class="headerThes_normal"><xsl:value-of select="."/></span>
                                            </a>
                                      
                                           <br/>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                              
                            </td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <br/>
                                <span class="headerThes_normal"><b>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </b></span>
                            </td>
                            <td valign="top">
                                <br/>
                                <xsl:variable name="Slash">\</xsl:variable>
                                <xsl:variable name="SlashJS">\\</xsl:variable>
                                <xsl:variable name="Apos">'</xsl:variable>
                                <xsl:variable name="AposJS">\'</xsl:variable>
                                <xsl:variable name="currentJS0">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                        <xsl:with-param name="text" select="//current/hierarchy/name"/>
                                        <xsl:with-param name="replace" select="$Slash" />
                                        <xsl:with-param name="with" select="$SlashJS" />
                                    </xsl:call-template>
                                </xsl:variable>

                                <xsl:variable name="currentJS">
                                    <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
                                        <xsl:with-param name="text" select="$currentJS0"/>
                                        <xsl:with-param name="replace" select="$Apos" />
                                        <xsl:with-param name="with" select="$AposJS" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <table border="0" cellpadding="0" cellspacing="0" >
                                    <tr>
                                        <td valign="middle">
                                            <a href="#">
                                                <xsl:attribute name="onClick">prepareResults('hierarchysTermsShortcuts','<xsl:value-of select="$currentJS"/>','alphabetical','true');</xsl:attribute>

                                                <img width="16" height="16" border="0" style="margin-left:2px; margin-right:4px;">
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/alphabetical/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/alphabetical/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                                <span class="headerThes_normal">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/alphabetical/displaytext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </span>
                                            </a> 
                                        </td>
                                    </tr>
                                    <tr >
                                        <td valign="middle">
                                            <a href="#">
                                                <xsl:attribute name="onClick">prepareResults('hierarchysTermsShortcuts','<xsl:value-of select="$currentJS"/>','hierarchical','true');</xsl:attribute>

                                                <img width="16" height="16" border="0" style="margin-left:2px; margin-right:4px;"  >
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/hierarchical/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/hierarchical/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                                <span class="headerThes_normal">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/hierarchical/displaytext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </span>
                                            </a>   
                                        </td>
                                    </tr>
                                    <tr >
                                        <td valign="middle">
                                            <a href="#">
                                                <xsl:attribute name="onClick">prepareResults('hierarchysTermsShortcuts','<xsl:value-of select="$currentJS"/>','systematic','true');</xsl:attribute>

                                                <img width="16" height="16" border="0" style="margin-left:2px; margin-right:4px;">
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/systematic/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/systematic/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                                <span class="headerThes_normal">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/systematic/displaytext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </span>
                                            </a> 
                                        </td>
                                    </tr>
                                    <tr >
                                        <td valign="middle">
                                            <a href="#">
                                                <xsl:attribute name="onClick">prepareResults('hierarchysTermsShortcuts','<xsl:value-of select="$currentJS"/>','primary2translations','true');</xsl:attribute>

                                                <img width="16" height="16" border="0" style="margin-left:2px; margin-right:4px;" >
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/index/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/index/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                                <span class="headerThes_normal">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/index/displaytext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </span>
                                            </a>
                                        </td>
                                    </tr>
                                    <tr >
                                        <td valign="middle">
                                            <a href="#">
                                                <xsl:attribute name="onClick">GraphicalViewIconPressed('GraphicalView','<xsl:value-of select="$currentJS"/>', 'HIERARCHY','false')</xsl:attribute>
                                                <img width="16" height="16" border="0" style="margin-left:2px; margin-right:4px;" >
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/graphical/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/graphical/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                                <span class="headerThes_normal">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/termspresentations/graphical/displaytext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </span>
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                                 
                            </td>
                        </tr>
                        
                    </xsl:otherwise>
                </xsl:choose>
            </table>
        </fieldset>
    </xsl:template>
    
    <xsl:template name="showEditActions_Hierarchy">
        <xsl:param name="hierarchyName"/>
        <xsl:variable name="hierarchycardlocale" select="document('../../translations/translations.xml')/locale/popupcards/hierarchy"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <div id="EditCardArea"/>
        <xsl:variable name="THEMASUserGroup" select="//page/THEMASUserInfo/userGroup "/>
        <!--<xsl:if test="$THEMASUserGroup = 'LIBRARY' ">-->
            <table cellspacing="15" width="100%" style="text-align:left; position:absolute; top:415px; left:0px;">
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/generalprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </b>
                    </td>
                </tr>
                <tr>
                    <td width="30%" valign="top">
                        <table cellpadding="2">
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$hierarchyName"/><xsl:text>','hierarchy_rename','EditDisplays_Hierarchy');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/rename/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$hierarchyName"/><xsl:text>','hierarchy_facets','EditDisplays_Hierarchy');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/editfacets/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditCard_Term('</xsl:text><xsl:value-of select="$hierarchyName"/><xsl:text>')</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/edittopterm/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$hierarchyName"/><xsl:text>','delete_hierarchy','EditDisplays_Hierarchy');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/delete/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            
                        </table>
                    </td>
                </tr>
            </table>
        <!--</xsl:if>-->
    </xsl:template>
    
    <xsl:template name="replace-string-for-PopUpInfo_Hierarchy">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string-for-PopUpInfo_Hierarchy">
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
