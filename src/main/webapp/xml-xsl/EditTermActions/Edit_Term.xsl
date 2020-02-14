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
    Document   : Edit_Term.xsl
    Created on : 13 Φεβρουάριος 2009, 5:09 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../Configs.xsl"/>
    <xsl:output method="html"/>
    
    <xsl:include href="moveToHierarchy.xsl"/>
	
    <xsl:variable name="targetEditField" select="//targetEditField"/>
    <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
    <xsl:variable name="THEMASUserGroupTeamCanEdit" select="//THEMASUserInfo/userGroup/@thesteamcaneditforinsertions"/>
    
    <xsl:template match="/">
        <xsl:variable name="termcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/term"/>
        <xsl:variable name="statuseslocale" select="document('../../translations/SaveAll_Locale_And_Scripting.xml')/root/common/statuses"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:variable name="primarylanguage" select="//page/@primarylanguage"/>
        <html>
            <head>
                <title>Edit_Term.xsl</title>
                <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>
            </head>
            <body>
                <div class="popUpCard2">
                    <xsl:choose>
                        <xsl:when test="$targetEditField='status' or $targetEditField ='delete_term'">
                            <div class="popUpEditCardSmall">
                                <xsl:choose>
                                    <xsl:when test="$targetEditField = 'delete_term' ">
                                        <xsl:call-template name="Edit_delete">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'status' ">
                                        <xsl:call-template name="Edit_status">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="statuseslocale" select="$statuseslocale"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                            <xsl:with-param name="primarylang" select="$lang"/>
                                            <!--<xsl:with-param name="primarylang" select="$primarylang"/>-->
                                        </xsl:call-template>
                                    </xsl:when>
                                </xsl:choose>
                            </div>
                        </xsl:when>
                        <xsl:otherwise>
                            <div class="popUpEditCardLarge">
                                <xsl:choose>
                                    <xsl:when test="$targetEditField = 'name'">
                                        <xsl:call-template name="rename_term">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="showSaveOldNameAsUF" select="//SaveOldNameAsUF"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                        <table width="100%">
                                            <tr>
                                                <td style="color:#898a5e; font-size:9px;">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/rename/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                                </td>
                                            </tr>
                                            <tr>
                                                <td valign="bottom" align="right">
                                                    <input type="button" class="button" onclick="if (CheckUserInput('newname', 'LOGINAM') == true) getServletResult( 'Rename_Term','renameFieldSet_Term', 'ResultOf_Rename_Term',''); ">
                                                        <xsl:attribute name="value">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </xsl:attribute>
                                                    </input>
                                                    &#160;
                                                    <input type="button" class="button" onclick="window.location.reload( true );">
                                                        <xsl:attribute name="value">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </xsl:attribute>
                                                    </input>
                                                </td>
                                            </tr>
                                        </table>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'move2Hier'">
                                        <xsl:call-template name="moveToHierarchy">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                        <table width="100%">
                                            <tr>
                                                <td>
                                                    <br/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td valign="bottom" align="right">
                                                    <input type="button" class="button" onclick="MTH_SaveButtonPressed();">
                                                        <xsl:attribute name="value">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </xsl:attribute>
                                                    </input>
                                                    &#160;
                                                    <input type="button" class="button" onclick="window.location.reload( true );">
                                                        <xsl:attribute name="value">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </xsl:attribute>
                                                    </input>
                                                </td>
                                            </tr>
                                        </table>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'move2HierResults'">
                                        <xsl:call-template name="moveToHierarchy">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>

                                        <table width="100%">
                                            <tr>
                                                <td>
                                                    <br/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td valign="bottom" align="right">
                                                    <xsl:choose>
                                                        <xsl:when test="//MoveToHierarchyData/succeded/text()='true'">
                                                            <input type="button" class="button" onclick="window.location.reload( true );">
                                                                <xsl:attribute name="value">
                                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalreloadtermbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                </xsl:attribute>
                                                            </input>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <input type="button" class="button" onclick="window.location.reload( true );">
                                                                <xsl:attribute name="value">
                                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                </xsl:attribute>
                                                            </input>
                                                            &#160;
                                                            <input type="button" class="button" onclick="window.location.reload( true );">
                                                                <xsl:attribute name="value">
                                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$termcardlocale/editactions/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                </xsl:attribute>
                                                            </input>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </td>
                                            </tr>
                                        </table>
                                    </xsl:when>
                                    <!-- see keywords used in DBGeneral-->
                                    <xsl:when test="$targetEditField = 'term_create' ">
                                        <xsl:call-template name="CreateNew_Term">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <!--<xsl:when test="$targetEditField = 'delete_term' "><xsl:call-template name="Edit_delete"/></xsl:when>-->
                                    <!--<xsl:when test="$targetEditField = 'status' "><xsl:call-template name="Edit_status"/></xsl:when>-->
                                    <xsl:when test="$targetEditField = 'bt' ">
                                        <xsl:call-template name="Edit_bt">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'guide_terms' ">
                                        <xsl:call-template name="Edit_guide_terms">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'rt' ">
                                        <xsl:call-template name="Edit_rt">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'translations' ">
                                        <xsl:call-template name="Edit_translations">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'uf' ">
                                        <xsl:call-template name="Edit_uf">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'uf_translations' ">
                                        <xsl:call-template name="Edit_uf_translations">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'primary_found_in' ">
                                        <xsl:call-template name="Edit_primary_found_in">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'translations_found_in' ">
                                        <xsl:call-template name="Edit_translations_found_in">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'tc' ">
                                        <xsl:call-template name="Edit_tc">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'scope_note' ">
                                        <xsl:call-template name="Edit_scope_note">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'translations_scope_note' ">
                                        <xsl:call-template name="Edit_translations_scope_note">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'historical_note' ">
                                        <xsl:call-template name="Edit_historical_note">
                                            <xsl:with-param name="specificlocale" select="$termcardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:text>UNDER CONSTRUCTION</xsl:text>
                                        <!--<fieldset height="100">
                                            <legend><xsl:text>Επεξεργασία </xsl:text>
                                                <xsl:value-of select="$targetEditField"/>
                                            </legend>
                                            <xsl:value-of select="//resultText"/>
                                            <xsl:value-of select="$targetEditField"/>
                                        </fieldset>
                                        <table width="100%">
                                            <tr>
                                                <td>
                                                    <br/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td valign="bottom" align="right">
                                                    <input type="button" class="button" value="Επιβεβαίωση" onclick="alert('under construction')"/>
                                                    &#160;<input type="button" class="button" value="Άκυρο" onclick="window.location.reload( true );"/>
                                                </td>
                                            </tr>
                                        </table>-->
                                    </xsl:otherwise>
                                </xsl:choose>
                            </div>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="CreateNew_Term">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <!--similar to edit templates defined in Edit_Term.xsl-->
        <fieldset id="edit_term_create">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <!--<legend>Επεξεργασία Πλατύτερων Όρων</legend>-->
            <br/>
            <table border="0" width="100%" align="center">
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/newname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                        <input id="newTermName_Id" type="text" size="57" name="newName_Term"/>
                        <br/>
                        <br/>
                    </td>
                </tr>
                <tr valign="top">
                    <td bgcolor="#F2F2F2" colspan="3" align="center">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/selectedterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    
                </tr>
                <!--
                <tr valign="top"  style="display:none;">
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/selectedterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','true');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/existingterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>-->
                <tr valign="top">
                    <td colspan="3"  class="chosenContainerClass">
                        <select id="newValue_Id" name="bt" multiple="true" data-placeholder="-------------" class="chosen-select">
                                <!--<xsl:for-each select="//current/term/bt/name">
                                    <xsl:sort select="."/> leave sorting as it was
                                    <option selected="selected">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>-->
                                 <xsl:for-each select="//availableTerms/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                
            
            <!--
                <tr valign="top" style="display:none;">
                    <td>
                        <select id="newValue_Id1" onmouseover="refreshCreateNew();" onchange="refreshCreateNew();"
                                onmouseout="refreshCreateNew();" onfocus="refreshCreateNew();" onblur="refreshCreateNew();"
                                name="bt1" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','true');">
                            <xsl:for-each select="//current/term/bt/name">
                                
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td>
                        <select id="available_values_id" onfocus="refreshCreateNew();" onblur="refreshCreateNew();" size="10" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableTerms/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>-->
            </table>
		
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
            
        </fieldset>
        
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult('EditActions_Term','edit_term_create', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: Edit_status
    _____________________________________________________________________________ -->
    <xsl:template name="Edit_status">
        <xsl:param name="specificlocale" />
        <xsl:param name="statuseslocale" />
        <xsl:param name="lang" />
        <xsl:param name="primarylang" />
        <fieldset id="edit_term_status">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/status/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table  border="0" style="position:relative; margin-left:auto; margin-right:auto;"  >
                <tr valign="top">
                
                    <td style="text-align:center;" bgcolor="#F2F2F2">
                    
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/status/promptvalue/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <xsl:variable name="termStatus" select="//current/term/status"/>
                        <select name="status">
                            <!-- user of group LIBRARY cannot change the status => DISABLE drop-down -->
                            <xsl:if test="$THEMASUserInfo_userGroup = 'LIBRARY' ">
                                <xsl:attribute name="disabled">disabled</xsl:attribute>
                                <xsl:attribute name="class">disabled</xsl:attribute>
                            </xsl:if>
                            <!-- Φιλτράρισμα των status ανάλογα με το user group και το τρέχων status του όρου -->
                            <xsl:choose>
                                <!-- _____________________ΟΜΑΔΑ ΘΗΣΑΥΡΟΥ_____________________-->
                                <!-- ομάδα θησαυρού: από «Για εισαγωγή» σε «Υπό επεξεργασία» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_TEAM' and $termStatus= $statuseslocale/forinsertion/option[@lang=$primarylang] ">
                                    
                                    <xsl:choose>
                                        <xsl:when test="$THEMASUserGroupTeamCanEdit ='true'">
                                            <xsl:call-template name="StatusOptions">
                                                <xsl:with-param name="GiaEisagwgh">true</xsl:with-param>
                                                <xsl:with-param name="YpoEpeksergasia">false</xsl:with-param>
                                                <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                                <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                                <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                                <xsl:with-param name="lang" select="$primarylang"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:call-template name="StatusOptions">
                                                <xsl:with-param name="GiaEisagwgh">true</xsl:with-param>
                                                <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                                <xsl:with-param name="YpoEgkrish">false</xsl:with-param>
                                                <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                                <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                                <xsl:with-param name="lang" select="$primarylang"/>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    
                                </xsl:when>                                
                                <!-- ομάδα θησαυρού: από «Υπό επεξεργασία» σε «Υπό έγκριση» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_TEAM' and $termStatus=$statuseslocale/underconstruction/option[@lang=$primarylang] ">
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- ομάδα θησαυρού: από «Υπό έγκριση» σε «Υπό επεξεργασία» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_TEAM' and $termStatus=$statuseslocale/underapproval/option[@lang=$primarylang] ">
                                    <xsl:choose>
                                        <xsl:when test="$THEMASUserGroupTeamCanEdit ='true'">                                        
                                            <xsl:call-template name="StatusOptions">
                                                    <xsl:with-param name="GiaEisagwgh">true</xsl:with-param>
                                                    <xsl:with-param name="YpoEpeksergasia">false</xsl:with-param>
                                                    <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                                    <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                                    <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                                    <xsl:with-param name="lang" select="$primarylang"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="StatusOptions">
                                                    <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                                    <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                                    <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                                    <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                                    <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                                    <xsl:with-param name="lang" select="$primarylang"/>
                                                </xsl:call-template>
                                            </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                    
                                <!-- ομάδα θησαυρού: από «Εκδοθείς» σε τίποτα -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_TEAM' and $termStatus=$statuseslocale/approved/option[@lang=$primarylang] ">
                                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">false</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">false</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">true</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- _____________________ΕΠΙΤΡΟΠΗ ΘΗΣΑΥΡΟΥ_____________________-->
                                <!-- επιτροπή θησαυρού: από «Για εισαγωγή» σε «Υπό επεξεργασία» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_COMMITTEE' and $termStatus=$statuseslocale/forinsertion/option[@lang=$primarylang] ">
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">true</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">false</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">false</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- επιτροπή θησαυρού: από «Υπό επεξεργασία» σε «Υπό έγκριση» ή «Εκδοθείς» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_COMMITTEE' and $termStatus=$statuseslocale/underconstruction/option[@lang=$primarylang] ">
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">true</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- επιτροπή θησαυρού: από «Υπό έγκριση» σε «Υπό επεξεργασία» ή «Εκδοθείς» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_COMMITTEE' and $termStatus=$statuseslocale/underapproval/option[@lang=$primarylang] ">
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">true</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- επιτροπή θησαυρού: από «Εκδοθείς» σε «Υπό επεξεργασία» -->
                                <xsl:when test="$THEMASUserInfo_userGroup = 'THESAURUS_COMMITTEE' and $termStatus=$statuseslocale/approved/option[@lang=$primarylang] ">
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">false</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">false</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">true</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <!-- _____________________OTHERWISE_____________________-->
                                <!-- σε κάθε άλλη περίπτωση, εμφάνισε όλα τα status -->
                                <xsl:otherwise>
                                    <xsl:call-template name="StatusOptions">
                                        <xsl:with-param name="GiaEisagwgh">true</xsl:with-param>
                                        <xsl:with-param name="YpoEpeksergasia">true</xsl:with-param>
                                        <xsl:with-param name="YpoEgkrish">true</xsl:with-param>
                                        <xsl:with-param name="Ekdothis">true</xsl:with-param>
                                        <xsl:with-param name="specificlocale" select="$statuseslocale"/>
                                        <xsl:with-param name="lang" select="$primarylang"/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </select>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td>
                    <br/>
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_status', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: StatusOptions
    _____________________________________________________________________________ -->
    <xsl:template name="StatusOptions">
        <xsl:param name="GiaEisagwgh"/>
        <xsl:param name="YpoEpeksergasia"/>
        <xsl:param name="YpoEgkrish"/>
        <xsl:param name="Ekdothis"/>
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <xsl:variable name="termStatus" select="//current/term/status"/>
        <!-- Για εισαγωγή -->
        <xsl:if test="$GiaEisagwgh = 'true' ">
            <xsl:choose>
                <xsl:when test="$termStatus=$specificlocale/forinsertion/option[@lang=$lang]">
                    <option selected="selected">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/forinsertion/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/forinsertion/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option>
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/forinsertion/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/forinsertion/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <!-- Υπό επεξεργασία -->
        <xsl:if test="$YpoEpeksergasia = 'true' ">
            <xsl:choose>
                <xsl:when test="$termStatus=$specificlocale/underconstruction/option[@lang=$lang]">
                    <option selected="selected">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option>
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <!-- Υπό έγκριση -->
        <xsl:if test="$YpoEgkrish = 'true' ">
            <xsl:choose>
                <xsl:when test="$termStatus=$specificlocale/underapproval/option[@lang=$lang]">

                    <option selected="selected">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underapproval/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underapproval/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option>
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underapproval/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/underapproval/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <!-- Εκδοθείς -->
        <xsl:if test="$Ekdothis = 'true' ">
            <xsl:choose>
                <xsl:when test="$termStatus=$specificlocale/approved/option[@lang=$lang]">
                    <option selected="selected">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/approved/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/approved/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option>
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/approved/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/approved/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
        
    <!-- EDIT BTS-->
    <xsl:template name="Edit_bt">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_bt">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/bt/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            
            <br/>
            
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" clospan="3">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/bt/selectedterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--
                    <td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','true');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/bt/existingterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td colspan="3" class="chosenContainerClass">
                        <select id="newValue_Id" name="bt" multiple="true" data-placeholder="-------------" class="chosen-select">
                                <!--<xsl:for-each select="//current/term/bt/name">
                                    <xsl:sort select="."/> leave sorting as it was
                                    <option selected="selected">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>-->
                                 <xsl:for-each select="//availableTerms/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                <!--
                <tr valign="top">
                    <td>
                        <select id="newValue_Id" name="bt" size="12" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','true');">
                            <xsl:for-each select="//current/term/bt/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableTerms/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>-->
            </table>
		
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td>
                    <br/>
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_bt', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!-- EDIT GUIDE TERMS-->
    <xsl:template name="Edit_guide_terms">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_guide_terms">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/guideterms/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="780" align="center">
                <tr valign="middle">
                    <td bgcolor="#F2F2F2" align="center" width="50%" >
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/guideterms/nt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="50%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/guideterms/guideterm/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>
                    
                <!-- for each nt define a row for its GuideLink-->
                <xsl:variable name="allGuideTerms" select="//GuideTerms/GuideTerm"/>
                
                <xsl:for-each select="//current/term/nt/name">
                    <xsl:variable name="posit" select="position()"/>
                    <xsl:variable name="GuideTermValId" select="concat('GuideTermValId', $posit)"/>
                    <xsl:variable name="currentClass" select="./@linkClass"/>
                    <tr valign="middle" style="height:24px;">
                        <td align="center"  >
                            <input type="text" class="disabled" name="NtName" disabled="disabled" style="width:100%;">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                            </input>
                        </td>
                        <td align="center">
                            <select name="GuideTerm" size="1" style="width:100%; border:1px solid; border-color:#999999; background-color:#FFFFFF;">
                                <xsl:for-each select="$allGuideTerms">
                                    <option>
                                        <xsl:if test=".=$currentClass">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="." />
                                        </xsl:attribute>
                                        <xsl:value-of select="." />
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
            <input type="text" name="targetTerm" id="tagetTerm-GuideTermId" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" id="targetEditField-GuideTermId" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td>
                    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="GuideTermsCollectFunction();">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT RTS-->
    <xsl:template name="Edit_rt">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_rt">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rt/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" colspan="3">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rt/relatedterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--
                    <td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rt/existingterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td colspan="3" class="chosenContainerClass">
                        <select id="newValue_Id" name="rt" multiple="true" data-placeholder="-------------" class="chosen-select">
                                 <xsl:for-each select="//availableTerms/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                <!--<tr valign="top">
                    <td>
                        <select id="newValue_Id" name="rt" size="12" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/rt/name">
                                
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableTerms/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>-->
            </table>
		
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td>
                    <br/>
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_rt', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
	
    <!-- Edit Translations -->
    <xsl:template name="Edit_translations">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_translations">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/translations/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/translations/translationterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--<td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>-->                    
                    <!--<td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/translations/existingenterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td>
                        <select id="newValue_Id" name="translations" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/translations/name">
                                <!--<xsl:sort select="."/>-->
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@linkClass"/>
                                        <xsl:value-of select="//Translations/@translationSeperator"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="@linkClass"/>
                                    <xsl:value-of select="//Translations/@translationSeperator"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>
                        <br/>
                        <select id="language_identifier_field">
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()='EN']">
                                <!--<xsl:sort select="TranslationIdentifier"/>-->
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()!='EN']">
                                <xsl:sort select="TranslationIdentifier"/>
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                        </select>
                        &#160;
                        <input type="text" style="width:500px;" id="additionalInput"/>&#160;&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewTranslationValue('newValue_Id','language_identifier_field','translationSeperator', 'additionalInput')"/>&#160;&#160;&#160;
                        <input type="button" class="button" value="-" onclick="removeSelectedOption('newValue_Id','false');"/>
                    </td>
                    <!--<td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableTranslations/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@linkClass"/>
                                        <xsl:value-of select="//Translations/@translationSeperator"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="@linkClass"/>
                                    <xsl:value-of select="//Translations/@translationSeperator"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>-->
                </tr>
            </table>


            <input type="text" id="translationSeperator" name="translationSeperator" class="hiddenInput" >
                <xsl:attribute name="value">
                    <xsl:value-of select="//Translations/@translationSeperator"/>
                    <xsl:text> </xsl:text>
                </xsl:attribute>
            </input>
            
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/translations/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_translations', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload(true);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT UF-->
    <xsl:template name="Edit_uf">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_uf">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
		
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" colspan="3">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf/ufterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--<td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf/existingufterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                 <tr valign="top">
                    <td colspan="3" class="chosenContainerClass">
                        <select id="newValue_Id" name="uf" multiple="true" data-placeholder="-------------" class="chosen-select">
                                 <xsl:for-each select="//availableUfs/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                <tr valign="top">
                    <td colspan="3">
                        <!--<select id="newValue_Id" name="uf" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/uf/name">
                                <xsl:sort select="."/>  leave sorting as it was
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>-->
                        <br/>
                        <input type="text" style="width:500px;" id="additionalInput"/>&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewValueInChosen('newValue_Id', 'additionalInput')"/>
                    </td>
                    <!--<td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableUfs/name">
                                <xsl:sort select="."/> leave sorting as it was
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    -->
                </tr>
            </table>
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_uf', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--Edit UF TRANSLATIONS-->
    <xsl:template name="Edit_uf_translations">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_uf_translations">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf_translations/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
		
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf_translations/uftranslationsterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--<td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf_translations/existinguftranslationsterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td>
                        <select id="newValue_Id" name="uf_translations" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/uf_translations/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@linkClass"/>
                                        <xsl:value-of select="//Translations/@translationSeperator"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="@linkClass"/>
                                    <xsl:value-of select="//Translations/@translationSeperator"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>
                        <br/>
                        <select id="language_identifier_field">
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()='EN']">
                                <!--<xsl:sort select="TranslationIdentifier"/>-->
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()!='EN']">
                                <xsl:sort select="TranslationIdentifier"/>
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                        </select>
                        &#160;
                        <!--                        <input type="text" style="width:250px;" id="additionalInput"/>
                                                <input type="button" class="button" value="+" onclick="if (CheckUserInput('additionalInput', 'LOGINAM') == true) addNewTranslationValue('newValue_Id','language_identifier_field','translationSeperator', 'additionalInput')"/>
                        -->
                        <input type="text" style="width:550px;" id="additionalInput"/>&#160;&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewTranslationValue('newValue_Id','language_identifier_field','translationSeperator', 'additionalInput')"/>&#160;&#160;&#160;
                        <input type="button" class="button" value="-" onclick="removeSelectedOption('newValue_Id','false');"/>
                    </td>
                    <!--<td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableUfTranslations/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@linkClass"/>
                                        <xsl:value-of select="//Translations/@translationSeperator"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="@linkClass"/>
                                    <xsl:value-of select="//Translations/@translationSeperator"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>-->
                </tr>
            </table>

            <input type="text" id="translationSeperator" name="translationSeperator" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//Translations/@translationSeperator"/>
                    <xsl:text> </xsl:text>
                </xsl:attribute>
            </input>
            
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/uf_translations/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_uf_translations', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT PRIMARY SOURCES-->
    <xsl:template name="Edit_primary_found_in">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_primary_found_in">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/primarysource/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" colspan="3">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/primarysource/selected/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--
                    <td rowspan="2" width="10%" valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/primarysource/existing/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td colspan="3"  class="chosenContainerClass">
                        <select id="newValue_Id" name="primary_found_in" multiple="true" data-placeholder="-------------" class="chosen-select">
                                 <xsl:for-each select="//availableSources/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                <tr valign="top">
                    <td colspan="3">
                        <!--<select id="newValue_Id" name="primary_found_in" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/primary_found_in/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>
                        <br/>-->
                        <input type="text" style="width:500px;" id="additionalInput"/>&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewValueInChosen('newValue_Id', 'additionalInput')"/>
                    </td>
                    <!--
                    <td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableSources/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>-->
                </tr>
            </table>
		
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/primarysource/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_primary_found_in', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT ENGLISH SOURCES-->
    <xsl:template name="Edit_translations_found_in">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_translations_found_in">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/trsource/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" colspna="3">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/trsource/selected/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <!--
                    <td rowspan="2"  valign="middle" align="center">
                        <input type="button" onclick="addOption('available_values_id','newValue_Id');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                        <input type="button" onclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                        <br/>
                        <br/>
                    </td>
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/trsource/existing/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>-->
                </tr>
                <tr valign="top">
                    <td colspan="3"  class="chosenContainerClass">
                        <select id="newValue_Id" name="translations_found_in" multiple="true" data-placeholder="-------------" class="chosen-select">
                                 <xsl:for-each select="//availableSources/name">
                                    <!-- <xsl:sort select="."/>  leave sorting as it was-->
                                    <option>
                                        <xsl:if test="./@selected='yes'">
                                            <xsl:attribute name="selected">
                                                <xsl:text>selected</xsl:text>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                        </select>
                    </td>
                </tr>
                <tr valign="top">
                    <td colspan="3">
                        <!--
                        <select id="newValue_Id" name="translations_found_in" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/translations_found_in/name">
                                
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>
                        <br/>-->
                        <input type="text" style="width:500px;" id="additionalInput"/>&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewValueInChosen('newValue_Id', 'additionalInput')"/>
                    </td>
                    <!--<td>
                        <select id="available_values_id" size="12" style="width:100%;" ondblclick="addOption('available_values_id','newValue_Id');">
                            <xsl:for-each select="//availableSources/name">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>-->
                </tr>
            </table>
		
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/trsource/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_translations_found_in', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT TCs-->
    <xsl:template name="Edit_tc">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_tc">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/tc/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center" width="45%">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/tc/selected/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td width="10%">
                    </td>
                    <td align="center" width="45%">
                    </td>
                </tr>
                <tr valign="top">
                    <td>
                        <select id="newValue_Id" name="tc" size="10" style="width:100%;" ondblclick="removeSelectedOption('newValue_Id','false');">
                            <xsl:for-each select="//current/term/tc/name">
                                <xsl:sort select="."/>
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                        <br/>
                        <br/>
                        <input type="text" style="width:255px;" id="additionalInput"/>&#160;&#160;
                        <input type="button" class="button" value="+" onclick="addNewValue('newValue_Id', 'additionalInput')"/>&#160;&#160;&#160;
                        <input type="button" class="button" value="-" onclick="removeSelectedOption('newValue_Id','false');"/>
                    </td>
                    <td>
                    </td>
                    <td>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/tc/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_tc', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT SCOPE NOTE-->
    <xsl:template name="Edit_scope_note">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_scope_note">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn/value/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <textarea id="modify_term_scope_note_id" class="notetextarea" name="scope_note" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:value-of select="//current/term/scope_note"/>
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <input type="text" name="targetTerm" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//targetTerm"/>
                            </xsl:attribute>
                        </input>
                        <input type="text" name="targetEditField" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$targetEditField"/>
                            </xsl:attribute>
                        </input>
                        <input type="button" class="button" onclick="prepareResultsWithoutRemoveBackGroundBlackScreen('SearchResults_Sources','','','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn/sources/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult('EditActions_Term','edit_term_scope_note','','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT TRANSLATIONS SCOPE NOTE-->
    <xsl:template name="Edit_translations_scope_note">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_translations_scope_note">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn_tr/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <!--<legend>Επεξεργασία Scope Note</legend>-->
            <br/>
            <table border="0" width="90%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center"  colspan="2">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn_tr/value/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td align="center"  colspan="2">
                        <div style="width:100%; display:block; overflow:auto;">
                            <table id="trsnstable">
                                <xsl:for-each select="//current/term/translations_scope_note/name">
                                    <tr>
                                        <td valign="top">
                                            <xsl:value-of select="@lang"/>
                                        </td>
                                        <td valing="top" width="98%">
                                            <!--<textarea id="modify_term_translations_scope_note_id" name="textarea" rows="5" class="trscopenotesclass">
                                                <xsl:value-of select="."/>
                                            </textarea>-->
                                            <textarea name="textarea" rows="5" class="trscopenotesclass">
                                                <xsl:attribute name="id"> 
                                                    <xsl:text>modify_term_translations_scope_note_id_</xsl:text>
                                                    <xsl:value-of select="@lang"/>
                                                </xsl:attribute>
                                                <xsl:value-of select="."/>                                                
                                            </textarea>
                                        </td>
                                        <td valign="top">
                                            <input type="button" class="button" value="-" onclick="deleteTableRow('trsnstable', this);"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </table>
                        </div>
                    </td>
                </tr>
                
                <!--<tr>
                    <td align="center">
                        <textarea id="modify_term_translations_scope_note_id" class="notetextarea" name="translations_scope_note" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:value-of select="//current/term/translations_scope_note"/>
                        </textarea>
                    </td>
                </tr>-->
                
                <tr>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn_tr/newlang/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        
                        <select id="language_identifier_field">
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()='EN']">
                                <!--<xsl:sort select="TranslationIdentifier"/>-->
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                            <xsl:for-each select="//Translations/TranslationPair[./TranslationIdentifier/text()!='EN']">
                                <xsl:sort select="TranslationIdentifier"/>
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="TranslationIdentifier"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="TranslationWord"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="TranslationIdentifier"/>
                                </option>
                            </xsl:for-each>
                        </select>
                        &#160;
                        <input type="button" class="button" value="+" onclick="addTranslationScopeNote('trsnstable', 'language_identifier_field')"/>
                        <input type="text" id="translationSeperator" name="translationSeperator" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//Translations/@translationSeperator"/>
                                <xsl:text></xsl:text>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="right" width="190px;">
                        
                        <input type="text" name="targetTerm" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//targetTerm"/>
                            </xsl:attribute>
                        </input>
                        <input type="text" name="targetEditField" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$targetEditField"/>
                            </xsl:attribute>
                        </input>
                        <input type="button" class="button" onclick="prepareResultsWithoutRemoveBackGroundBlackScreen('SearchResults_Sources','','','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn_tr/sources/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sn_tr/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_translations_scope_note', '','');">
                        <!--<input type="button" class="button" onclick="saveTranslationsScopeNotes('trsnstable');">-->
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT HISTORICAL NOTE-->
    <xsl:template name="Edit_historical_note">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_historical_note">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/hn/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/hn/value/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <textarea id="modify_term_historical_note_id" class="notetextarea" name="historical_note" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:value-of select="//current/term/historical_note"/>
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <input type="text" name="targetTerm" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//targetTerm"/>
                            </xsl:attribute>
                        </input>
                        <input type="text" name="targetEditField" class="hiddenInput">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$targetEditField"/>
                            </xsl:attribute>
                        </input>
                        <input type="button" class="button" onclick="prepareResultsWithoutRemoveBackGroundBlackScreen('SearchResults_Sources','','','false');">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/hn/sources/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/hn/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Term','edit_term_historical_note', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--DELETE TERM-->
    <xsl:template name="Edit_delete">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_term_delete">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            <br/>
            <br/>
            <table width="100%">
                <tr>
                    <td style="text-align:center;">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/confirmmessage/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="//targetTerm"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/qmark/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <br/>
                        <br/>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetTerm" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetTerm"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getDeleteResult( 'EditActions_Term','edit_term_delete', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/deletebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--RENAME TERM-->
    <xsl:template name="rename_term">
        <xsl:param name="specificlocale" />
        <xsl:param name="showSaveOldNameAsUF" />
        <xsl:param name="lang" />
   
        <fieldset id="renameFieldSet_Term">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
            
            <br/>
            
            <div id="DynamicGenaration" align="center">
                <table cellspacing="0" cellpadding="3">
                    <tr>
                        <td colspan="3">
                            <input id="oldd" style="visibility: hidden; height:1px;">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="//currentRename/term/name"/>
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    
                    <tr bgcolor="#F2F2F2" valign="middle">
                        <td  align="right" > <!-- style="color:#999966" -->
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/currentname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                        
                        
                        <xsl:choose>
                            <xsl:when test="$showSaveOldNameAsUF='yes'">
                                <td>                            
                                    <input id="oldTerm" disabled="disabled" class="disabledbutton" style="width:100%" name="target" >
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="//currentRename/term/name"/>
                                        </xsl:attribute>
                                    </input>
                                </td>
                                <td style="width:200px;">
                                    <label>
                                        <!-- removing the checked="checked"  it should only be done when intended -->
                                    <input type="checkbox" name="saveasuf" value="yes" style="vertical-align: middle;"/>
                                        
                                    </label>
                                    <xsl:text> <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/saveasufCheckboxLabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>     </xsl:text>                                    
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td colspan="2">                            
                                    <input id="oldTerm" disabled="disabled" class="disabledbutton" style="width:630px;" name="target" >
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="//currentRename/term/name"/>
                                        </xsl:attribute>
                                    </input>
                                    <input type="checkbox" name="saveasuf" value="no" style="display:none;"/> 
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                        
                        
                    </tr>
                
                    <!--EMPTY SEPERATOR LINE-->
                    <tr style="height:3px; font-size:1pt;">
                        <td colspan="3"></td>
                    </tr>
                        
                    <tr bgcolor="#F2F2F2" valign="middle">
                        <td  align="right">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/newname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                        <td colspan="2">
                            <input type="text" id="newname" style="width:630px;" name="newname" onkeyup="if(event.keyCode==13) getServletResult( 'Rename_Term','renameFieldSet_Term', 'ResultOf_Rename_Term','selectedIndexOnly');"/>
                        </td>
                    </tr>
                    
                    <!--EMPTY SEPERATOR LINE-->
                    <tr style="height:3px; font-size:1pt;">
                        <td colspan="3"></td>
                    </tr>
                        
                    <tr bgcolor="#F2F2F2" valign="middle">
                        <td align="right" valign="top">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                        <td colspan="2">
                            <xsl:choose>
                                <xsl:when test="(not (//currentRename/term/name ) )or (//currentRename/term/name = '')">
                                    <textarea id="ResultOf_Rename_Term" name="errorMSG"  class="renametextarea" readonly="readonly" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/notargetterm/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </textarea>
                                </xsl:when>
                                <xsl:otherwise>
                                    <textarea id="ResultOf_Rename_Term" name="errorMSG"  class="renametextarea" readonly="readonly" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                                        <xsl:value-of select="//currentRename/termError/apotelesma"/>
                                    </textarea>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                    </tr>
                    
                </table>
            </div>
            
            <input id="clear" value="first" style="visibility: hidden; height:1px;"/>
            
            <table id="myTable2" align="center">
                <tbody id="myTbody2">
                    <tr style="visibility: hidden;  height:1px;">
                    </tr>
                </tbody>
            </table>
            
            <input width="20" type="text" name="Newname" style="visibility: hidden;  height:1px;"/>
            <input width="20" type="text" name="NewnameForServlet" style="visibility: hidden;  height:1px;" disabled="disabled" class="disabledbutton" />
            
        </fieldset>
        
        <!-- Undo Rename code -->
        <xsl:if test="//UndoRenameData/Target/targetTermCanBeUndoRenamed = 'true'">
            <xsl:call-template name="UndoRenameCode"/>
        </xsl:if>
        
    </xsl:template>
	
    <!-- _____________________________________________________________________________
              TEMPLATE: UndoRenameCode
              FUNCTION: displays the form or the result of the Undo Rename operation
    _____________________________________________________________________________ -->
    <xsl:template name="UndoRenameCode">
        <xsl:variable name="TargetTermName" select="//UndoRenameData/Target/name"/>
        <xsl:variable name="UndoRenameResultsMessage" select="//UndoRenameData/UndoRenameResultsMessage"/>
        <br></br>
        <fieldset id="UndoRenameFieldSet_Term">
            <legend>
                <xsl:text>Αναίρεση Μετονομασίας Όρου</xsl:text>
            </legend>
            
            <xsl:if test="count($UndoRenameResultsMessage) = 0 "> <!-- XSL called by RenameInfo_Term servlet -->
                <xsl:call-template name="UndoRenameDisplayForm"/>
            </xsl:if>
            
            <xsl:if test="count($UndoRenameResultsMessage) != 0 "> <!-- XSL called by UndoRenameResults servlet -->
                <br></br>
                <!-- display the result message -->
                <xsl:value-of select="$UndoRenameResultsMessage"/>
                <br/>&#160;
            </xsl:if>
        </fieldset>
    </xsl:template>
	
    <!-- _____________________________________________________________________________
              TEMPLATE: UndoRenameDisplayForm
              FUNCTION: displays the form of the Undo Rename operation
    _____________________________________________________________________________ -->
    <xsl:template name="UndoRenameDisplayForm">
        <xsl:variable name="TargetTermName" select="//UndoRenameData/Target/name"/>
        <br/>
	    
        <!-- Εμφάνιση του μονοπατιού / κύκλου μετονομασιών προς αναίρεση -->
        <xsl:if test="//UndoRenameData/UndoRenameChain/@rename_chain_kind = 'PATH' ">
            <b>
                <xsl:text>Μονοπάτι </xsl:text>
            </b>
        </xsl:if>
        <xsl:if test="//UndoRenameData/UndoRenameChain/@rename_chain_kind = 'CYCLE' ">
            <b>
                <xsl:text>Κύκλος </xsl:text>
            </b>
        </xsl:if>
        <b>
            <xsl:text>μετονομασιών προς αναίρεση: </xsl:text>
        </b>
            
        <xsl:for-each select="//UndoRenameData/UndoRenameChain/couple">
            <xsl:value-of select="part1"/>
            <xsl:text> </xsl:text>&#8594;<!-- &#8594; = HTML right arrow character -->
            <xsl:text> </xsl:text>
            <!-- case of last couple => display part2 also -->
            <xsl:if test="name(./following-sibling::*[1]) != 'couple' ">
                <xsl:value-of select="part2"/>
            </xsl:if>
        </xsl:for-each>
            
        <!-- Αποτέλεσμα - Αποθήκευση -->
        <table width="100%">
                
            <tr>
                <td></td>
            </tr>
            <tr>
                <td></td>
            </tr>
              
            <tr  align="center">
                <td>
                    <input value="Αποθήκευση" class="button" type="button">
                        <xsl:attribute name="onClick">
                            <xsl:text>getServletResult( 'UndoRenameResults','renameFieldSet_Term', 'ResultOf_Rename_Term','selectedIndexOnly');</xsl:text>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
