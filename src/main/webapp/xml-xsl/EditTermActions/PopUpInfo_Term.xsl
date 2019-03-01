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
    Document   : PopUpInfo_Term.xsl
    Created on : 10 Φεβρουάριος 2009, 1:00 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../search_results_terms_alphabetical.xsl"/>
    <xsl:import href="../Configs.xsl"/>
    <xsl:variable name="TermIsEditable" select="//page/termName/@editable "/>
    

    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="CSS/xml_thes.css?v=@DeploymentTimestamp@"/>
                <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>
                <title>PopUpInfo_Term.xsl</title>
            </head>
            <body>
                <div class="popUpCard">
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Term">
                        <xsl:with-param name="showClose">
                            <xsl:text>true</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                </div>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="PopUp_Or_EditCard_Of_Term">
        <xsl:param name="showClose"/>
        <xsl:variable name="showCreatorInAlphabeticalDisplay" select="//data/@displayCreatorInAlphabetical"/>
        <xsl:variable name="termName" select="//data/terms/term[1]/descriptor"/>
        <xsl:variable name="termcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/term"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <fieldset style="width:790px;" >
            <legend  style="margin-bottom:5px;">
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$termcardlocale/legend/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
                <b>
                    <xsl:value-of select="$termName"/>                    
                </b>
            </legend>
            <table width="100%">
                <xsl:choose>
                    <xsl:when test="$showClose = 'true'">                        
                        <tr width="100%">
                            <xsl:variable name="currentLinkNode" select="//data/terms/term[1]"/>
                            <td colspan="2" align="right" width="100%" >
                                <!--<a target="_blank">
                                    <xsl:attribute name="href">
                                                                <xsl:value-of select="$currentLinkNode/ReferenceUri/text()"/>
                                                            </xsl:attribute>
                                    <i>link</i>
                                    
                                    <img src="images/link32.png" width="16" height="16" border="0" style="margin-left:5px;"/>
                                    
                                </a>
                            &#160;&#160;&#160;&#160;&#160;&#160;-->
                                <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                                <!-- Tropopoihsh -->
                                <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER' ">
                                    <xsl:variable name="Slash">
                                        <xsl:text>\</xsl:text>
                                    </xsl:variable>
                                    <xsl:variable name="SlashJS">
                                        <xsl:text>\\</xsl:text>
                                    </xsl:variable>
                                    <xsl:variable name="Apos">
                                        <xsl:text>'</xsl:text>
                                    </xsl:variable>
                                    <xsl:variable name="AposJS">
                                        <xsl:text>\'</xsl:text>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Term">
                                            <xsl:with-param name="text" select="$termName"/>
                                            <xsl:with-param name="replace" select="$Slash"/>
                                            <xsl:with-param name="with" select="$SlashJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>                                    
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string-for-PopUpInfo_Term">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos"/>
                                            <xsl:with-param name="with" select="$AposJS"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="edit" select="//edit"/>
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditCard_Term('</xsl:text>
                                            <xsl:value-of select="$currentJS"/>
                                            <xsl:text>')</xsl:text>
                                        </xsl:attribute>
                                        <i>                                         
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/edittext/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </i>
                                        <img width="16" height="16" border="0">
                                            <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$termcardlocale/editimage/src/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$termcardlocale/editimage/title/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                            </xsl:attribute>
                                        </img>
                                    </a>
                                    &#160;&#160;&#160;&#160;&#160;&#160;
                                </xsl:if>
                                
                                <a href="#" onclick="document.getElementById('prompt').innerHTML='';DisplayPleaseWaitScreen(false);">
                                    <i>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/closetext/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </i>[x]
                                </a>
                            </td>
                        </tr>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="//errorMsg ">
                        <tr>
                            <td colspan="2">
                                <xsl:value-of select="//errorMsg"/>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td valign="top" width="448" style="padding-right:20px; padding-left:20px;  ">
                                <xsl:call-template name="alphabetical-display">
                                    <xsl:with-param name="prefferedWidth" select="'440'"/>
                                    <xsl:with-param name="showCreator" select="$showCreatorInAlphabeticalDisplay"/>
                                </xsl:call-template>
                            </td>
                            <xsl:if test="//THEMASUserInfo/userGroup != 'READER'">
                                <td width="362" valign="top" style="padding-right:20px; padding-left:20px; border-style:solid; border-left-width:thin; border-left-color:#CCCCCC; border-bottom:0; border-top:0; border-right:0;">
                                
                                    <table>
                                        <xsl:variable name="currentNode" select="//data/terms/term[1]"/>
                                        <xsl:if test="$currentNode/descriptor/@referenceId[.!='']">
                                            <tr width="350" valign="top">
                                                <td valign="top" width="100" >
                                                    <span class="headerThes_normal">
                                                        <b>
                                                            <xsl:choose>
                                                                <xsl:when test="position()!=1"></xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:call-template name="getTranslationMessage"> 
                                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/refId/option"/> 
                                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                    </xsl:call-template>    
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </b>
                                                    </span>
                                                </td>                                                
                                                <td valign="middle" width="240" style="text-align:justify;">
                                                    <span class="headerThes_normal">
                                                        <xsl:value-of select="$currentNode/descriptor/@referenceId"/>
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
                                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/refIdToolTipPrefix/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                            </xsl:call-template>
                                                            <xsl:text>','</xsl:text>
                                                            <xsl:value-of select="$currentNode/ReferenceUri/text()"/>
                                                            <xsl:text>');</xsl:text>
                                                        </xsl:attribute>
                                                        <!--<i>Copy Link</i>-->
                                                        <img src="images/link32.png" width="14" height="14" border="0" style="margin-left:5px;"/>
                                                    </a> 
                                                    <!-- display: none; --> 
                                                    <div id="refIdTooltipLink" style="position: absolute; z-index: 1000; display: none;  top: 70px; background: white; text-align: left; right:10px;    border: 2px solid black;">                                                    
                                                        <div style="margin:5px" id="refIdTooltipTxt">
                                                            
                                                        </div>
                                                    </div>
                                                    
                                                    <br/>
                                                </td>
                                            </tr>
                                        </xsl:if>
                                        
                                        <xsl:if test="$currentNode/created_by[.!='']">
                                            <xsl:for-each select="$currentNode/created_by[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/creator/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                        
                                        <xsl:if test="$currentNode/modified_by[.!='']">
                                            <xsl:for-each select="$currentNode/modified_by[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/modificator/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                        
                                        <xsl:if test="$currentNode/created_on[.!='']">
                                            <xsl:for-each select="$currentNode/created_on[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/creationdate/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                        
                                        <xsl:if test="$currentNode/modified_on[.!='']">
                                            <xsl:for-each select="$currentNode/modified_on[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/modificationdate/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                        
                                        <xsl:if test="$currentNode/status[.!='']">
                                            <xsl:for-each select="$currentNode/status[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/status/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">                                                            
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                                
                                                
                                        <xsl:if test="$currentNode/historical_note[.!='']">
                                            <xsl:for-each select="$currentNode/historical_note[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>                                                                
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>                                                                         
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/hn/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal showDecorations">
                                                            <xsl:value-of disable-output-escaping="yes" select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                            
                                        <xsl:if test="$currentNode/accepted[.!='']">
                                            <xsl:for-each select="$currentNode/accepted[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:call-template name="getTranslationMessage"> 
                                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/acceptterm/option"/> 
                                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                        </xsl:call-template>    
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="."/>
                                                        </span>
                                                        <br/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </xsl:if>
                                                
                                        <xsl:if test="//isTopTerm ='true'">
                                            <tr width="350">
                                                <td valign="top" width="100" >
                                                    <span class="headerThes_normal">
                                                        <b>
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/showhierarchy/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </b>
                                                    </span>
                                                </td>
                                                <td>
                                                    <xsl:variable name="Slash">\</xsl:variable>
                                                    <xsl:variable name="SlashJS">\\</xsl:variable>
                                                    <xsl:variable name="Apos">'</xsl:variable>
                                                    <xsl:variable name="AposJS">\'</xsl:variable>
                                                    <xsl:variable name="currentHierJS0">
                                                        <xsl:call-template name="replace-string-for-PopUpInfo_Term">
                                                            <xsl:with-param name="text" select="//data/terms/term[1]/topterm"/>
                                                            <xsl:with-param name="replace" select="$Slash"/>
                                                            <xsl:with-param name="with" select="$SlashJS"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="currentHierJS">
                                                        <xsl:call-template name="replace-string-for-PopUpInfo_Term">
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
                                                        <span class="headerThes_normal">
                                                            <xsl:value-of select="//page/termName/node()"/>
                                                        </span>
                                                    </a>
                                                </td>
                                            </tr>
                                        </xsl:if>
                                    </table>
                                
                                </td>
                            </xsl:if>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </table>
        </fieldset>
    </xsl:template>
    
    
    <xsl:template name="showEditActions_Term">
        <xsl:param name="termName"/>
        <xsl:param name="disableTopTermEdit"/>
        <xsl:variable name="termcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/term/editactions"/>
        <xsl:variable name="statuseslocale" select="document('../../translations/SaveAll_Locale_And_Scripting.xml')/root/common/statuses"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:variable name="primarylang" select="//page/@primarylanguage"/>
        <div id="EditCardArea"/>
        
        
        <xsl:if test="$TermIsEditable = 'false' ">
            <table cellspacing="20" width="100%">
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$termcardlocale/noeditaction/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </b>
                    </td>
                </tr>
            </table>
        </xsl:if>
        <xsl:if test="$TermIsEditable = 'true' ">
            <!-- Εμφάνιση των λειτουργιών επεξεργασίας μόνο στην περίπτωση που το status του όρου είναι "Υπό επεξεργασία" ή πρόκειται για editable term από κάποιον user of group LIBRARY -->
            <xsl:variable name="TermStatus" select="//data/terms/term[1]/status "/>
            <xsl:variable name="THEMASUserGroup" select="//page/THEMASUserInfo/userGroup "/>
            
            <xsl:if test="$TermStatus = $statuseslocale/underconstruction/option[@lang=$lang] or $THEMASUserGroup = 'LIBRARY' ">
                <table cellspacing="15" width="100%" style="text-align:left; position:absolute; top:415px; left:0px;">
                    <tr>
                        <td colspan="3">
                            <xsl:choose>
                                <xsl:when test="$disableTopTermEdit = 'true'">
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/toptermprompt/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                        </xsl:call-template>    
                                    </b>
                                </xsl:when>
                                <xsl:otherwise>
                                    <b>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/generalprompt/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </b>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                    </tr>
                    <tr>
                        <td width="33%" valign="top">
                            <table cellpadding="2">
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$disableTopTermEdit = 'true'">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/rename/prompttitle/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                        </xsl:call-template>    
                                                    </a>
                                                </xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('</xsl:text>
                                                        <xsl:value-of select="$termName"/>
                                                        <xsl:text>','name','RenameInfo_Term');</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/rename/prompttitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                    </xsl:call-template>    
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$disableTopTermEdit = 'true'">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/bt/prompttitle/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                        </xsl:call-template>    
                                                    </a>
                                                </xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('</xsl:text>
                                                        <xsl:value-of select="$termName"/>
                                                        <xsl:text>','bt','EditDisplays_Term');</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/bt/prompttitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                    </xsl:call-template>    
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','rt','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/rt/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','translations','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/translations/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','status','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/status/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td >
                                        <xsl:choose>
                                            <xsl:when test="count(//term/nt) = 0">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/guideterms/prompttitle/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                        </xsl:call-template>    
                                                    </a>
                                                </xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('</xsl:text>
                                                        <xsl:value-of select="$termName"/>
                                                        <xsl:text>','guide_terms','EditDisplays_Term');</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/guideterms/prompttitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                    </xsl:call-template>    
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="33%" valign="top">
                            <table cellpadding="2">
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','uf','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/uf/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','uf_translations','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/uf_translations/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','primary_found_in','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/primarysource/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','translations_found_in','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/trsource/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','tc','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/tc/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="33%" valign="top">
                            <table cellpadding="2">                                
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','scope_note','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/sn/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','translations_scope_note','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/sn_tr/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="#">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('</xsl:text>
                                                <xsl:value-of select="$termName"/>
                                                <xsl:text>','historical_note','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$termcardlocale/hn/prompttitle/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                                            </xsl:call-template>    
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$disableTopTermEdit = 'true'">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/moveterm/prompttitle/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                        </xsl:call-template>    
                                                    </a>
                                                </xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('</xsl:text>
                                                        <xsl:value-of select="$termName"/>
                                                        <xsl:text>','move2Hier','MoveToHierarchy');</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/moveterm/prompttitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                    </xsl:call-template>    
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$disableTopTermEdit = 'true'">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$termcardlocale/delete/prompttitle/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                        </xsl:call-template>    
                                                    </a>
                                                </xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="#">
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('</xsl:text>
                                                        <xsl:value-of select="$termName"/>
                                                        <xsl:text>','delete_term','EditDisplays_Term');</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$termcardlocale/delete/prompttitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                                                    </xsl:call-template>    
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>                    
                </table>
            </xsl:if>
            <!-- Απόκρυψη των λειτουργιών επεξεργασίας και εμφάνιση ΜΟΝΟ της δυνατότητας αλλαγής του status στην περίπτωση -->
            <!--  που (το status του όρου είναι "Υπό επεξεργασία" ή δεν έχει status) και ο user δεν είναι LIBRARY -->
            <xsl:if test="($TermStatus != $statuseslocale/underconstruction/option[@lang=$lang] or count($TermStatus) = 0) and $THEMASUserGroup != 'LIBRARY' ">
                <table cellspacing="20" width="100%">
                    <tr>
                        <td colspan="3">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$termcardlocale/generalprompt/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'yes'"/> 
                                </xsl:call-template>    
                            </b>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a href="#">
                                <xsl:attribute name="onClick">
                                    <xsl:text>showEditFieldCard('</xsl:text>
                                    <xsl:value-of select="$termName"/>
                                    <xsl:text>','status','EditDisplays_Term');</xsl:text>
                                </xsl:attribute>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$termcardlocale/status/prompttitle/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'yes'"/> 
                                </xsl:call-template>    
                            </a>
                        </td>
                    </tr>
                </table>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="replace-string-for-PopUpInfo_Term">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string-for-PopUpInfo_Term">
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
