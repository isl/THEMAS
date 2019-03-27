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
    Document   : PopUpInfo_Source.xsl
    Created on : 29 Απρίλιος 2009, 1:53 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../Configs.xsl"/>
    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <head>
                <link rel="stylesheet" type="text/css" href="CSS/xml_thes.css?v=@DeploymentTimestamp@"/>
                <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>                
				<!--<xsl:attribute name="href"><xsl:value-of select="'CSS/xml_thes.css?v=@DeploymentTimestamp@'"/></xsl:attribute>
			</link>-->
                <title>PopUpInfo_Source.xsl</title>
            </head>
            <body>                
                <div class="popUpCard">
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Source">
                        
                        <xsl:with-param name="showClose">
                            <xsl:text>true</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
               
                </div>
            </body>
    </xsl:template>

    
    <xsl:template name="PopUp_Or_EditCard_Of_Source">
        <xsl:param name="showClose"/>
        <xsl:variable name="sourcecardlocale" select="document('../../translations/translations.xml')/locale/popupcards/source"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <fieldset >
            <legend style="margin-bottom:5px;">
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            <b><xsl:value-of select="//source/name"/></b></legend>
            <table width="100%">
                <xsl:choose>
                    <xsl:when test="$showClose = 'true'">
                        <tr width="100%">
                            <td colspan="2" align="right" width="100%">
                                <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
								<!-- Tropopoihsh -->
                                <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER' ">
                                    <xsl:variable name="Slash">\</xsl:variable>
                                    <xsl:variable name="SlashJS">\\</xsl:variable>
                                    <xsl:variable name="Apos">'</xsl:variable>
                                    <xsl:variable name="AposJS">\'</xsl:variable>
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                            <xsl:with-param name="text" select="//source/name"/>
                                            <xsl:with-param name="replace" select="$Slash"/>
                                            <xsl:with-param name="with" select="$SlashJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos"/>
                                            <xsl:with-param name="with" select="$AposJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditCard_Source('</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>')</xsl:text>
                                        </xsl:attribute>
                                    <i> 
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/edittext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </i>
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/editimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/editimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                        
                                    </img>
                                    </a>&#160;&#160;&#160;&#160;&#160;&#160;
                                </xsl:if>
								<!-- Kleisimo -->
                                <a href="#" onclick="document.getElementById('DisplayCardArea').innerHTML='';DisplayPleaseWaitScreen(false);">
                                    <i>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/closetext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
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
                        <!-- Display info of source-->    
                        <xsl:variable name="currentSource" select="//data/sources/source[1]"/>
                        <tr valign="top">
                            <td>
                                <span class="headerThes_normal">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/source/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </b>
                                </span>
                            </td>
                            <td>
                                <span class="headerThes_normal"><xsl:value-of select="$currentSource/name"/></span>
                            </td>
                        </tr>
                        <xsl:if test="$currentSource/source_note and $currentSource/source_note!=''">
                            <tr valign="top">
                                <td>
                                    <br/>
                                    <span class="headerThes_normal">
                                        <b>
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/sourcenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </b>
                                    </span>

                                </td>
                                <td>
                                    <br/>
                                    <span class="headerThes_normal showDecorations"><xsl:value-of disable-output-escaping="yes" select="$currentSource/source_note"/></span>
                                </td>
                            </tr>
                        </xsl:if>
                        
                        <xsl:variable name="howmanyGTs" select="count($currentSource/primary_found_in)"/>
                        <xsl:if test="$currentSource/primary_found_in and $currentSource/primary_found_in !=''">
                            <tr valign="top">
                                <td>
                                    <br/>
                                    <span class="headerThes_normal">
                                        <b>
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/primarysource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </b>
                                    </span>
                                </td>
                                <td>
                                    <br/>
                                    <span class="headerThes_normal">
                                        
                                        <xsl:variable name="Slash">\</xsl:variable>
                                        <xsl:variable name="SlashJS">\\</xsl:variable>
                                        <xsl:variable name="Apos">'</xsl:variable>
                                        <xsl:variable name="AposJS">\'</xsl:variable>
                                        <xsl:for-each select="$currentSource/primary_found_in">
                                            <xsl:variable name="currentTermJS0">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                                    <xsl:with-param name="text" select="."/>
                                                    <xsl:with-param name="replace" select="$Slash"/>
                                                    <xsl:with-param name="with" select="$SlashJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:variable name="currentTermJS">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                                    <xsl:with-param name="text" select="$currentTermJS0"/>
                                                    <xsl:with-param name="replace" select="$Apos"/>
                                                    <xsl:with-param name="with" select="$AposJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            
                                            <a href="#">
                                                <xsl:attribute name="onClick">
                                                    <xsl:text>popUpCard('</xsl:text>
                                                    <xsl:value-of select="$currentTermJS"/>
                                                    <xsl:text>','CardOf_Term')</xsl:text>
                                                </xsl:attribute>
                                                <xsl:value-of select="."/>
                                            </a>
                                            <xsl:if test="position()!= $howmanyGTs">
                                                <xsl:text>, </xsl:text>                                            
                                            </xsl:if>
                                        </xsl:for-each>
                                    </span>
                                </td>
                            </tr>
                        </xsl:if>
                        
                        <xsl:variable name="howmanyETs" select="count($currentSource/translations_found_in)"/>
                        <xsl:if test="$currentSource/translations_found_in and $currentSource/translations_found_in !=''">
                            <tr valign="top">
                                <td>
                                    <br/>
                                    <span class="headerThes_normal">
                                        <b>
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/trsource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </b>
                                    </span>

                                </td>
                                <td>
                                    <br/>
                                    <span class="headerThes_normal">
                                        
                                        <xsl:variable name="Slash">\</xsl:variable>
                                        <xsl:variable name="SlashJS">\\</xsl:variable>
                                        <xsl:variable name="Apos">'</xsl:variable>
                                        <xsl:variable name="AposJS">\'</xsl:variable>
                                        <xsl:for-each select="$currentSource/translations_found_in">
                                            
                                            <xsl:variable name="currentTermJS0">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                                    <xsl:with-param name="text" select="."/>
                                                    <xsl:with-param name="replace" select="$Slash"/>
                                                    <xsl:with-param name="with" select="$SlashJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:variable name="currentTermJS">
                                                <xsl:call-template name="replace-string-for-PopUpInfo_Source">
                                                    <xsl:with-param name="text" select="$currentTermJS0"/>
                                                    <xsl:with-param name="replace" select="$Apos"/>
                                                    <xsl:with-param name="with" select="$AposJS"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            
                                            <a href="#">
                                                <xsl:attribute name="onClick">
                                                    <xsl:text>popUpCard('</xsl:text>
                                                    <xsl:value-of select="$currentTermJS"/>
                                                    <xsl:text>','CardOf_Term')</xsl:text>
                                                </xsl:attribute>
                                                <xsl:value-of select="."/>
                                            </a>
                                            <xsl:if test="position()!=$howmanyETs">
                                                <xsl:text>, </xsl:text>                                            
                                            </xsl:if>
                                        </xsl:for-each>
                                    </span>
                                </td>
                            </tr>
                        </xsl:if>
                        
                        
                    </xsl:otherwise>
                </xsl:choose>
            </table>
        </fieldset>
    </xsl:template>
    
    <xsl:template name="showEditActions_Source">
        <xsl:param name="sourceName"/>
        <xsl:variable name="sourcecardlocale" select="document('../../translations/translations.xml')/locale/popupcards/source/editactions"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <div id="EditCardArea"/>
        <xsl:variable name="THEMASUserGroup" select="//page/THEMASUserInfo/userGroup "/>
        <!--<xsl:if test="$THEMASUserGroup = 'LIBRARY' ">-->
            <table cellspacing="15" width="100%" style="text-align:left; position:absolute; top:415px; left:0px;">
                <tr>
                    <td >
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/generalprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                </tr>
                <tr>
                    <td valign="top">
                        <table cellpadding="2">
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$sourceName"/><xsl:text>','source_rename','EditDisplays_Source');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/rename/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$sourceName"/><xsl:text>','source_note','EditDisplays_Source');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/editsn/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$sourceName"/><xsl:text>','move_source_references','EditDisplays_Source');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/moverefs/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <a href="#">
                                        <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('</xsl:text><xsl:value-of select="$sourceName"/><xsl:text>','delete_source','EditDisplays_Source');</xsl:text></xsl:attribute>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$sourcecardlocale/delete/prompttitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                    </a>
                                </td>
                            </tr>
                            
                        </table>
                    </td>
                </tr>
            </table>
        <!--</xsl:if>-->
    </xsl:template>
    
    
    <xsl:template name="replace-string-for-PopUpInfo_Source">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string-for-PopUpInfo_Source">
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
