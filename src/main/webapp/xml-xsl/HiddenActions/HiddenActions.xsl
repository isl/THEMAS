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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:template name="THEMAS_HiddenActions_content">
        <xsl:variable name="tabup" select="//page/leftmenu/activemode "/>
        <!-- _________________ TABs menu _________________ -->
        <div id="displaytabmenu" style="background: #E2E2E2; width:100%;" >
            
            <table border="0" cellpadding="0" cellspacing="0" style="background: #E2E2E2; width:100%;">
                <tr valign="left" style="background: #E2E2E2;">
                    <td id="THEMAS_HiddenActions_TAB" onclick="THEMAS_HiddenActionsDIV('THEMAS_HiddenActions_DIV');" style="width:10%;">
                        <a href="#" onFocus="if(this.blur)this.blur()" id="THEMAS_HiddenActions_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='THEMAS_HiddenActions_DIV'">
                                    <xsl:attribute name="class">
                                        <xsl:text>active</xsl:text>
                                    </xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">
                                        <xsl:text>inactive</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/tabs/adminsystem/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </a>
                    </td>
                    <td id="THEMAS_SystemConfigurations_TAB" onclick="THEMAS_HiddenActionsDIV('THEMAS_HiddenSystemConfigurations_DIV');" style="width:10%;">
                        <a href="#" onFocus="if(this.blur)this.blur()" id="THEMAS_HiddenSystemConfigurations_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='THEMAS_HiddenSystemConfigurations_DIV'">
                                    <xsl:attribute name="class">
                                        <xsl:text>active</xsl:text>
                                    </xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">
                                        <xsl:text>inactive</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/tabs/systemconfigs/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </a>
                    </td>
                    <td id="THEMAS_HiddenTranslations_TAB" onclick="THEMAS_HiddenActionsDIV('THEMAS_HiddenTranslations_DIV');" style="width:10%;">
                        <a href="#" onFocus="if(this.blur)this.blur()" id="THEMAS_HiddenTranslations_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='THEMAS_HiddenTranslations_DIV'">
                                    <xsl:attribute name="class">
                                        <xsl:text>active</xsl:text>
                                    </xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">
                                        <xsl:text>inactive</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/tabs/translations/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </a>
                    </td>
                    <td id="THEMAS_HiddenFixData_TAB" onclick="THEMAS_HiddenActionsDIV('THEMAS_HiddenFixData_DIV');" style="width:10%;">
                        <a href="#" onFocus="if(this.blur)this.blur()" id="THEMAS_HiddenFixData_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='THEMAS_HiddenFixData_DIV'">
                                    <xsl:attribute name="class">
                                        <xsl:text>active</xsl:text>
                                    </xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">
                                        <xsl:text>inactive</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/tabs/datacorrections/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </a>
                    </td>
                    <td id="Exit_THEMAS_HiddenActions" style="width:10%;">
                        
                        <a href="Index" class="inactive" onFocus="if(this.blur)this.blur()" >
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/tabs/themasreturn/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </a>
                    </td>
                    
                    <!--<td style="width:20%;"><a class="inactive" >&#160;&#160;&#160;</a>  </td>-->
                    <!--<td style="width:20%;"><a class="inactive" >&#160;&#160;&#160;</a>  </td>-->
                    <td style="width:20%;">
                        <a class="inactive" >&#160;&#160;&#160;</a>
                    </td>
                </tr>
            </table>
        </div>
        <div id="content" style="height:600px; width:1024px; text-align:left">
            <div id="THEMAS_HiddenSystemConfigurations_DIV" class="admintab-body">
                <xsl:choose>
                    <xsl:when test="$tabup='THEMAS_HiddenSystemConfigurations_DIV'">
                        <xsl:attribute name="style">
                            <xsl:text>visibility:visible;</xsl:text>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">
                            <xsl:text>visibility:hidden;</xsl:text>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="SystemConfigurations"/>
            </div>
            <div id="THEMAS_HiddenTranslations_DIV" class="admintab-body">
                <xsl:choose>
                    <xsl:when test="$tabup='THEMAS_HiddenTranslations_DIV'">
                        <xsl:attribute name="style">
                            <xsl:text>visibility:visible;</xsl:text>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">
                            <xsl:text>visibility:hidden;</xsl:text>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="AdminTranslations"/>
            </div>
            <div id="THEMAS_HiddenFixData_DIV" class="admintab-body" >
                <xsl:choose>
                    <xsl:when test="$tabup='THEMAS_HiddenFixData_DIV'">
                        <xsl:attribute name="style">
                            <xsl:text>visibility:visible;</xsl:text>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">
                            <xsl:text>visibility:hidden;</xsl:text>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="AdminFixData"/>
            </div>
            <div id="THEMAS_HiddenActions_DIV" class="admintab-body">
                <xsl:choose>
                    <xsl:when test="$tabup='THEMAS_HiddenActions_DIV'">
                        <xsl:attribute name="style">
                            <xsl:text>visibility:visible;</xsl:text>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">
                            <xsl:text>visibility:hidden;</xsl:text>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="THEMAS_HiddenActions"/>
            </div>
        </div>
    </xsl:template>
	<!-- _____________________________________________________________________________
    TEMPLATE: THEMAS_HiddenActions
     _____________________________________________________________________________ -->
    <xsl:template name="THEMAS_HiddenActions">
        <xsl:variable name="adminsystemlocale" select="$locale/loginadmin/adminsystem"/>
                <!-- _______________________  Έναρξη / Τερματισμός SIS server _______________________ -->
        <fieldset style="width:930px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/startstopNeo4j/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/Neo4jstatus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <xsl:if test="//Start_StopNeo4j/Neo4jStatus = 'ON' ">
                        <td class="CategName" style="color: green">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/active/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/promptstop/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <b>
                                        <a href="Start_StopNeo4j?action=STOP">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/stopNeo4jDb/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                    <xsl:if test="//Start_StopNeo4j/Neo4jStatus = 'OFF' ">
                        <td class="CategName" style="color: red">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/inactive/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/promptstart/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <b>
                                        <a href="Start_StopNeo4j?action=START">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/startneo4j/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                </tr>
            </table>
        </fieldset>
        <br/>
        <br/>
		<!-- _______________________  (Ξε)Κλείδωμα συστήματος _______________________ -->
        <fieldset style="width:930px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/lockunlocksystem/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/systemstatus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <xsl:if test="//Lock_UnlockSystem/SystemStatus = 'ON' ">
                        <td class="CategName" style="color: green">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/unlocked/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/promptlock/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <b>
                                        <a href="Lock_UnlockSystem?action=LOCK">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/performlock/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                    <xsl:if test="//Lock_UnlockSystem/SystemStatus = 'OFF' ">
                        <td class="CategName" style="color: red">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/locked/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/promptunlock/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <b>
                                        <a href="Lock_UnlockSystem?action=UNLOCK">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/performunlock/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                </tr>
            </table>
</fieldset>
            <br/>
        <br/>
	<!-- _______________________  Επαναρχικοποίηση βάσης δεδομένων μέσω εξαγωγής/εισαγωγής δεδομένων σε XML _______________________ -->
        <fieldset style="width:930px;" id="resetDbFieldsetId">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/resetdb/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        
                            <a style="cursor: pointer;" onclick="getServletResult('StartExportImportToXML','resetDbFieldsetId','','');">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$adminsystemlocale/resetdbstart/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </a>
                        
                    </td>                    
                </tr>
            </table>
            <br/>
        </fieldset>
        
    </xsl:template>

    <xsl:template name="AdminTranslations">
        <xsl:variable name="admintranslationslocale" select="$locale/loginadmin/translations"/>
                <!-- _______________________  Translations _______________________ -->
        <fieldset style="width:930px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/fieldsetlabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            
            <br/>
            <table width="100%">
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                    
                </tr>
                <tr>
                    <td>
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/applyingthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                        <xsl:text>&#160;</xsl:text>
                        <input type="text" id="ConfigTranslationsCategoriesThesaurus_Admin_Id" name="ConfigTranslationsCategoriesThesaurus_Admin" size="20">
                            <xsl:if test="count(page/Translations[@thesaurus!=''])>0">
                                <!--<xsl:attribute name="disabled">
                                    <xsl:text>disabled</xsl:text>
                                </xsl:attribute>-->
                                <xsl:attribute name="value">
                                    <xsl:value-of select="page/Translations/@thesaurus"/>
                                </xsl:attribute>
                            </xsl:if>
                        </input>
                        <xsl:text>&#160;&#160;</xsl:text>
                        <input type="button" class="button" onClick="callAdminTranslationsServlet('Translations','preview'  )">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/previewcategories/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td colspan="2"/>
                    
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                    
                </tr>
                <tr>
                    <td>
                        <div id="previewtranslationsarea">
                            <xsl:if test="count(page/Translations[@thesaurus!=''])>0">

                                <b>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/selectedthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </b>
                                <xsl:text>&#160;</xsl:text>
                                <xsl:value-of select="page/Translations/@thesaurus"/>
                                <input type="hidden" name="targetThesaurus">
                                    <xsl:if test="count(page/Translations[@thesaurus!=''])>0">
                                        <!--<xsl:attribute name="disabled">
                                            <xsl:text>disabled</xsl:text>
                                        </xsl:attribute>-->
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="page/Translations/@thesaurus"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <br/>
                                <br/>
                                <table border="0">
                                    <tr>
                                        <td valign="top">
                                            <table class="translationstable" id="trsnstable">
                                                <thead>
                                                    <tr><!--bgcolor="#E8E9BE"-->
                                                        <th class="translationstablecolumn">
                                                            <b>
                                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/identifierlabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                            </b>
                                                        </th>
                                                        <th class="translationstablecolumn">
                                                            <b>
                                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/languagelabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                            </b>
                                                        </th>
                                                    </tr>
                                                </thead>
                                                <tbody id="translationstablebody">
                                                    <xsl:for-each select="/page/Translations/TranslationPair">
                                                        <xsl:sort select="./TranslationIdentifier"/>

                                                        <tr class="translationstablerow">
                                                            <td class="translationstablecolumn">
                                                                <input type="text" name="LanguageIdentifier"><!-- disabled="disabled"-->
                                                                    <xsl:attribute name="value">
                                                                        <xsl:value-of select="./TranslationIdentifier"/>
                                                                    </xsl:attribute>
                                                                </input>
                                                            </td>
                                                            <td class="translationstablecolumn">
                                                                <input type="text" name="LanguageName"><!-- disabled="disabled"-->
                                                                    <xsl:attribute name="value">
                                                                        <xsl:value-of select="./TranslationWord"/>
                                                                    </xsl:attribute>
                                                                </input>
                                                            </td>
                                                            <td>
                                                                <img width="20" height="20" border="0" id ="removeLanguageImg" onClick="deleteTableRow('translationstablebody', this);">
                                                                    <xsl:attribute name="src">
                                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                    </xsl:attribute>
                                                                    <xsl:attribute name="title">
                                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                    </xsl:attribute>                                                                    
                                                                </img>
                                                            </td>
                                                        </tr>
                                                    </xsl:for-each>
                                                </tbody>
                                            </table>
                                        </td>
                                        <td valign="top">
                                            <img width="20" height="20" border="0" onClick="addLanguageRow('translationstablebody','inputvalue_term');">
                                                <xsl:attribute name="src">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/addimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/addimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                            </img>
                                            
                                            <!--<br/>
                                            <img width="20" height="20" border="0" id ="removeLanguageImg" onClick="removeLanguageRow(document.getElementById('translationstablebody'));">
                                                <xsl:attribute name="src">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                                <xsl:choose>
                                                    <xsl:when test="count(/page/Translations/TranslationPair)>0">
                                                        <xsl:attribute name="display">
                                                            <xsl:value-of select="'inline'"/>
                                                        </xsl:attribute>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:attribute name="display">
                                                            <xsl:value-of select="'none'"/>
                                                        </xsl:attribute>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </img>-->
                            
                                        </td>
                                        <td style="display:none">
                                            <img width="20" height="20" border="0" id ="removeLanguageImgTemplate" onClick="deleteTableRow('translationstablebody', this);">
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                         </td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <br/>
                                        </td>
                                   </tr>
                                    <tr>
                                        <td style="text-align:right;">
                                            <input type="button" class="button" onclick="callAdminTranslationsServlet('Translations','save');">
                                                <xsl:attribute name="value">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$admintranslationslocale/savebuttonlabel/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                            </input>
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td>
                                            <br/>
                                        </td>
                                   </tr>
                                    
                                </table>
                                <br/>
                
                            </xsl:if>
                        </div>
                    </td>
                    <td colspan="2"/>
                    
                </tr>
                <tr>
                    <td colspan="3">
                        <xsl:value-of select="//Translations/TranslationsActionResult"/>
                    </td>
                </tr>
            </table>
           
        </fieldset>

    </xsl:template>
    <xsl:template name="AdminFixData">
        <xsl:variable name="datafixeslocale" select="$locale/loginadmin/datacorrections"/>
        <fieldset style="width:930px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/datacorrectionactions/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <table width="100%">
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/applyingthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                        <input type="text" id="FixThesData_Admin_Id" name="FixThesData_Admin" size="20" onkeypress="if (event.keyCode == 13) return false;"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr><!--PREFIXES ERRORS-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/prefixes/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Prefixes')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Prefixes')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--NAMING ERRORS-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/naming/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','RepairNames')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','RepairNames')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--DATES-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/dates/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Transform_Dates')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Transform_Dates')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--GARBADGE-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/garbadge/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Garbage_Collection')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Garbage_Collection')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED SOURCES-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/unclassifiedsources/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_Source_relations')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Unclassified_Source_relations')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED EDITORS-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/unclassifiededitors/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_Editor_relations')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Unclassified_Editor_relations')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED TERMS-->
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/unclassifiedterms/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_HierarchyTerms')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/showreport/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="disabledbutton" disabled="disabled">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$datafixeslocale/fixbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="onclick">
                                <xsl:text>alert('</xsl:text>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/underconstruction/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                <xsl:text>');</xsl:text>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
        </fieldset>
    </xsl:template>
    <xsl:template name="SystemConfigurations">
        <xsl:variable name="systemconfigslocale" select="$locale/loginadmin/systemconfigs"/>
        <fieldset style="width:930px;" id="SystemConfigurations_Fieldset_Id">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/systemconfigurations/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <table >
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/uilanguage/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="Language_ID" name="Language" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/UILanguage"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                        <i>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/uilanguagenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </i>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/liststep/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="ListStep_ID" name="ListStep" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/ListStep"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/taxcodesformat/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input name="TaxonomicalCodeFormat" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/TaxonomicalCodeFormat"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                        <i>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/taxcodesformatnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </i>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr style="display:none;">
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/backupstarttime/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="AutoBackupStartTime_ID" name="Automatic_Backups_Next_Day_Start_Time" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/Automatic_Backups_Next_Day_Start_Time"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>
                <tr style="display:none;">
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr style="display:none;">
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/backupinterval/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="AutoBackupInterval_ID" name="Automatic_Backups_Hours_Interval" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/Automatic_Backups_Hours_Interval"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                        <i>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/backupintervalnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </i>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr style="display:none;">
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/backupdescription/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="AutoBackupDescr_ID" name="Automatic_Backups_Description" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/Automatic_Backups_Description"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <!--<tr>
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/emails/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="mailList_ID" name="mailList" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/mailList"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/mailserver/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="mailHost_ID" name="mailHost" type="text" size="30">
                            <xsl:attribute name="value">
                                <xsl:value-of select="page/SystemConfigurations/mailHost"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>-->
                <tr>
                    <td colspan="2" align="right">
                        <br/>
                        <input type="hidden" name="Delimeter" id="DELIMETER_ID">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//DELIMETER"/>
                            </xsl:attribute>
                        </input>
                        <input type="button" class="button" onclick="callSystemConfigurationsServlet();">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$systemconfigslocale/savechanges/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td style="color:#898a5e; font-size:9px;">
                    </td>
                </tr>
            </table>
        </fieldset>
    </xsl:template>
</xsl:stylesheet>
