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
    <xsl:include href="../Configs.xsl"/>
    <xsl:variable name="TermIsEditable" select="//page/termName/@editable "/>
    

    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="CSS/xml_thes.css"/>
                <link rel="stylesheet" type="text/css" href="CSS/page.css"/>
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
        <xsl:variable name="termName" select="//data/terms/term[1]/descriptor"/>
        <xsl:variable name="termcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/term"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <fieldset style="width:790px;" >
            <legend  style="margin-bottom:5px;">
                <xsl:value-of select="$termcardlocale/legend/option[@lang=$lang]"/>
                <b>
                    <xsl:value-of select="$termName"/>                    
                </b>
            </legend>
            <table width="100%">
                <xsl:choose>
                    <xsl:when test="$showClose = 'true'">
                        
                        <tr width="100%">
                            <td colspan="2" align="right" width="100%" >
                                <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                                <!-- Tropopoihsh -->
                                <xsl:if test="$THEMASUserInfo_userGroup != 'READER' ">
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
                                            <xsl:value-of select="$termcardlocale/edittext/option[@lang=$lang]"/>
                                        </i>
                                        <img width="16" height="16" border="0">
                                            <xsl:attribute name="src">
                                                <xsl:value-of select="$termcardlocale/editimage/src/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:value-of select="$termcardlocale/editimage/title/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </img>
                                    </a>
                                                                         &#160;&#160;&#160;&#160;&#160;&#160;
                                </xsl:if>
                                
                                <a href="#" onclick="document.getElementById('prompt').innerHTML='';DisplayPleaseWaitScreen(false);">
                                    <i>
                                        <xsl:value-of select="$termcardlocale/closetext/option[@lang=$lang]"/>
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
                                </xsl:call-template>
                            </td>
                            <xsl:if test="//THEMASUserInfo/userGroup != 'READER' ">
                                <td width="362" valign="top" style="padding-right:20px; padding-left:20px; border-style:solid; border-left-width:thin; border-left-color:#CCCCCC; border-bottom:0; border-top:0; border-right:0;">
                                
                                    <table >
                                        <xsl:variable name="currentNode" select="//data/terms/term[1]"/>
                                        <xsl:if test="$currentNode/created_by[.!='']">
                                            <xsl:for-each select="$currentNode/created_by[.!='']">
                                                <tr width="350" valign="top">
                                                    <td valign="top" width="100" >
                                                        <span class="headerThes_normal">
                                                            <b>
                                                                <xsl:choose>
                                                                    <xsl:when test="position()!=1"></xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:value-of select="$termcardlocale/creator/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$termcardlocale/modificator/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$termcardlocale/creationdate/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$termcardlocale/modificationdate/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$termcardlocale/status/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$termcardlocale/hn/option[@lang=$lang]"/>
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </b>
                                                        </span>
                                                    </td>
                                                    <td valign="top" width="240" style="text-align:justify;">
                                                        <span class="headerThes_normal">
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
                                                                        <xsl:value-of select="$termcardlocale/acceptterm/option[@lang=$lang]"/>
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
                                                            <xsl:value-of select="$termcardlocale/showhierarchy/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$termcardlocale/noeditaction/option[@lang=$lang]"/>
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
                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/toptermprompt/option[@lang=$lang]"/>
                                    </b>
                                </xsl:when>
                                <xsl:otherwise>
                                    <b>
                                        <xsl:value-of select="$termcardlocale/generalprompt/option[@lang=$lang]"/>
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
                                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/rename/prompttitle/option[@lang=$lang]"/>
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
                                                    <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/rename/prompttitle/option[@lang=$lang]"/>
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
                                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/bt/prompttitle/option[@lang=$lang]"/>
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
                                                    <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/bt/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/rt/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/translations/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/status/prompttitle/option[@lang=$lang]"/>
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td >
                                        <xsl:choose>
                                            <xsl:when test="count(//term/nt) = 0">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/guideterms/prompttitle/option[@lang=$lang]"/>
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
                                                    <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/guideterms/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/uf/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/uf_translations/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/primarysource/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/trsource/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/tc/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/sn/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/sn_tr/prompttitle/option[@lang=$lang]"/>
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
                                            <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/hn/prompttitle/option[@lang=$lang]"/>
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="$disableTopTermEdit = 'true'">
                                                <xsl:text>
                                                    <a class="disabled">
                                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/moveterm/prompttitle/option[@lang=$lang]"/>
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
                                                    <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/moveterm/prompttitle/option[@lang=$lang]"/>
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
                                                        <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/delete/prompttitle/option[@lang=$lang]"/>
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
                                                    <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/delete/prompttitle/option[@lang=$lang]"/>
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
                                <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/generalprompt/option[@lang=$lang]"/>
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
                                <xsl:value-of disable-output-escaping="yes" select="$termcardlocale/status/prompttitle/option[@lang=$lang]"/>
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
