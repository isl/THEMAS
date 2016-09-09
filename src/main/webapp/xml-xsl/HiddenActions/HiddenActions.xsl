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
                            <xsl:value-of select="$locale/loginadmin/tabs/adminsystem/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$locale/loginadmin/tabs/systemconfigs/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$locale/loginadmin/tabs/translations/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$locale/loginadmin/tabs/datacorrections/option[@lang=$lang]"/>
                        </a>
                    </td>
                    <td id="Exit_THEMAS_HiddenActions" style="width:10%;">
                        
                        <a href="Index" class="inactive" onFocus="if(this.blur)this.blur()" >
                            <xsl:value-of select="$locale/loginadmin/tabs/themasreturn/option[@lang=$lang]"/>
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
                <xsl:value-of select="$adminsystemlocale/startstopNeo4j/option[@lang=$lang]"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        <b>
                            <xsl:value-of select="$adminsystemlocale/Neo4jstatus/option[@lang=$lang]"/>
                        </b>
                    </td>
                    <xsl:if test="//Start_StopNeo4j/Neo4jStatus = 'ON' ">
                        <td class="CategName" style="color: green">
                            <b>
                                <xsl:value-of select="$adminsystemlocale/active/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:value-of select="$adminsystemlocale/promptstop/option[@lang=$lang]"/>
                                    <b>
                                        <a href="Start_StopNeo4j?action=STOP">
                                            <xsl:value-of select="$adminsystemlocale/stopNeo4jDb/option[@lang=$lang]"/>
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                    <xsl:if test="//Start_StopNeo4j/Neo4jStatus = 'OFF' ">
                        <td class="CategName" style="color: red">
                            <b>
                                <xsl:value-of select="$adminsystemlocale/inactive/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:value-of select="$adminsystemlocale/promptstart/option[@lang=$lang]"/>
                                    <b>
                                        <a href="Start_StopNeo4j?action=START">
                                            <xsl:value-of select="$adminsystemlocale/startneo4j/option[@lang=$lang]"/>
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
                <xsl:value-of select="$adminsystemlocale/lockunlocksystem/option[@lang=$lang]"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        <b>
                            <xsl:value-of select="$adminsystemlocale/systemstatus/option[@lang=$lang]"/>
                        </b>
                    </td>
                    <xsl:if test="//Lock_UnlockSystem/SystemStatus = 'ON' ">
                        <td class="CategName" style="color: green">
                            <b>
                                <xsl:value-of select="$adminsystemlocale/unlocked/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:value-of select="$adminsystemlocale/promptlock/option[@lang=$lang]"/>
                                    <b>
                                        <a href="Lock_UnlockSystem?action=LOCK">
                                            <xsl:value-of select="$adminsystemlocale/performlock/option[@lang=$lang]"/>
                                        </a>
                                    </b>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                    <xsl:if test="//Lock_UnlockSystem/SystemStatus = 'OFF' ">
                        <td class="CategName" style="color: red">
                            <b>
                                <xsl:value-of select="$adminsystemlocale/locked/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <table>
                            <tr>
                                <td class="CategName">
                                    <xsl:value-of select="$adminsystemlocale/promptunlock/option[@lang=$lang]"/>
                                    <b>
                                        <a href="Lock_UnlockSystem?action=UNLOCK">
                                            <xsl:value-of select="$adminsystemlocale/performunlock/option[@lang=$lang]"/>
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
                <xsl:value-of select="$adminsystemlocale/resetdb/option[@lang=$lang]"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td class="CategName">
                        
                            <a style="cursor: pointer;" onclick="getServletResult('StartExportImportToXML','resetDbFieldsetId','','');">
                                <xsl:value-of select="$adminsystemlocale/resetdbstart/option[@lang=$lang]"/>
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
                <xsl:value-of select="$admintranslationslocale/fieldsetlabel/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$admintranslationslocale/applyingthesaurus/option[@lang=$lang]"/>
                        </b>
                        <xsl:text>&#160;</xsl:text>
                        <input type="text" id="ConfigTranslationsCategoriesThesaurus_Admin_Id" name="ConfigTranslationsCategoriesThesaurus_Admin" size="20">
                            <xsl:if test="count(page/Translations[@ofthes!=''])>0">
                                <!--<xsl:attribute name="disabled">
                                    <xsl:text>disabled</xsl:text>
                                </xsl:attribute>-->
                                <xsl:attribute name="value">
                                    <xsl:value-of select="page/Translations/@ofthes"/>
                                </xsl:attribute>
                            </xsl:if>
                        </input>
                        <xsl:text>&#160;&#160;</xsl:text>
                        <input type="button" class="button" onClick="callAdminTranslationsServlet('Translations','preview'  )">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$admintranslationslocale/previewcategories/option[@lang=$lang]"/>
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
                            <xsl:if test="count(page/Translations[@ofthes!=''])>0">

                                <b>
                                    <xsl:value-of select="$admintranslationslocale/selectedthesaurus/option[@lang=$lang]"/>
                                </b>
                                <xsl:text>&#160;</xsl:text>
                                <xsl:value-of select="page/Translations/@ofthes"/>
                                <input type="hidden" name="targetThesaurus">
                                    <xsl:if test="count(page/Translations[@ofthes!=''])>0">
                                        <!--<xsl:attribute name="disabled">
                                            <xsl:text>disabled</xsl:text>
                                        </xsl:attribute>-->
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="page/Translations/@ofthes"/>
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
                                                                <xsl:value-of select="$admintranslationslocale/identifierlabel/option[@lang=$lang]"/>
                                                            </b>
                                                        </th>
                                                        <th class="translationstablecolumn">
                                                            <b>
                                                                <xsl:value-of select="$admintranslationslocale/languagelabel/option[@lang=$lang]"/>
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
                                                                        <xsl:value-of select="$admintranslationslocale/removeimage/src/option[@lang=$lang]"/>
                                                                    </xsl:attribute>
                                                                    <xsl:attribute name="title">
                                                                        <xsl:value-of select="$admintranslationslocale/removeimage/title/option[@lang=$lang]"/>
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
                                                    <xsl:value-of select="$admintranslationslocale/addimage/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$admintranslationslocale/addimage/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                            
                                            <!--<br/>
                                            <img width="20" height="20" border="0" id ="removeLanguageImg" onClick="removeLanguageRow(document.getElementById('translationstablebody'));">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$admintranslationslocale/removeimage/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$admintranslationslocale/removeimage/title/option[@lang=$lang]"/>
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
                                                        <xsl:value-of select="$admintranslationslocale/removeimage/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$admintranslationslocale/removeimage/title/option[@lang=$lang]"/>
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
                                                    <xsl:value-of select="$admintranslationslocale/savebuttonlabel/option[@lang=$lang]"/>
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
                <xsl:value-of select="$datafixeslocale/datacorrectionactions/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$datafixeslocale/applyingthesaurus/option[@lang=$lang]"/>
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
                        <xsl:value-of select="$datafixeslocale/prefixes/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Prefixes')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Prefixes')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--NAMING ERRORS-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/naming/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','RepairNames')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','RepairNames')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--DATES-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/dates/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Transform_Dates')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Transform_Dates')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--GARBADGE-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/garbadge/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Garbage_Collection')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Garbage_Collection')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED SOURCES-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/unclassifiedsources/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_Source_relations')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Unclassified_Source_relations')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED EDITORS-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/unclassifiededitors/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_Editor_relations')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Fix','Unclassified_Editor_relations')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr><!--UNCLASSIFIED TERMS-->
                    <td style="text-align:justify">
                        <xsl:value-of select="$datafixeslocale/unclassifiedterms/option[@lang=$lang]"/>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callAdminFixServlet('FixAdminData','Preview','Unclassified_HierarchyTerms')">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/showreport/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="disabledbutton" disabled="disabled">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$datafixeslocale/fixbutton/option[@lang=$lang]"/>
                            </xsl:attribute>
                            <xsl:attribute name="onclick">
                                <xsl:text>alert('</xsl:text>
                                <xsl:value-of select="$locale/generalmessages/underconstruction/option[@lang=$lang]"/>
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
                <xsl:value-of select="$systemconfigslocale/systemconfigurations/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/uilanguage/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/uilanguagenote/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/liststep/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/taxcodesformat/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/taxcodesformatnote/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/backupstarttime/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/backupinterval/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/backupintervalnote/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/backupdescription/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/emails/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$systemconfigslocale/mailserver/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$systemconfigslocale/savechanges/option[@lang=$lang]"/>
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
