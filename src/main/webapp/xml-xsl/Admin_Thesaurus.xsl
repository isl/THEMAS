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
    Document   : Admin_Thesaurus.xsl
    Created on : 2 Απρίλιος 2009, 5:20 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:variable name="CurrentAdminTHES_DIV" select="//page/content_Admin_Thesaurus/CurrentShownDIV"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template name="Admin_Thesaurus_content">
        <xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:variable name="tabup" select="//page/content_Admin_Thesaurus/CurrentShownDIV"/>
        
        <!-- _________________ TABs menu _________________ -->
        <div id="displaytabmenu">
            <table border="0" cellpadding="0" cellspacing="0" style="width:100%;">
                <tr valign="left">
                    <!-- _________________ TAB: Δημιουργία αντιγράφου ασφαλείας _________________ 
                    <xsl:if test="$THEMASUserInfo_userGroup != 'THESAURUS_TEAM' ">-->
                    <td id="ImportExport_Data_TAB">
                        <a href="Admin_Thesaurus?DIV=ImportExport_Data_DIV" onFocus="if(this.blur)this.blur()" id="ImportExport_Data_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='ImportExport_Data_DIV'">
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
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/thesauri/tabs/tab1/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </td>
                    <td id="Fix_Data_TAB">
                        <a href="Admin_Thesaurus?DIV=Fix_Data_DIV" onFocus="if(this.blur)this.blur()" id="Fix_Data_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='Fix_Data_DIV'">
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
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/thesauri/tabs/tab2/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </td>
                    <!--</xsl:if>
                    <xsl:if test="$THEMASUserInfo_userGroup != 'THESAURUS_TEAM' ">-->
                    <xsl:if test="$THEMASUserInfo_userGroup='ADMINISTRATOR'">
                        <td id="CreateThesaurus_TAB">



                            <a href="Admin_Thesaurus?DIV=CreateThesaurus_DIV" onFocus="if(this.blur)this.blur()" id="CreateThesaurus_LINK">
                                <xsl:choose>
                                    <xsl:when test="$tabup='CreateThesaurus_DIV'">
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
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/thesauri/tabs/tab3/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </a>
                        </td>
                    </xsl:if>
                    
                    <td id="EditGuideTerms_TAB">
                        <a href="Admin_Thesaurus?DIV=EditGuideTerms_DIV" onFocus="if(this.blur)this.blur()" id="EditGuideTerms_LINK">
                            <xsl:choose>
                                <xsl:when test="$tabup='EditGuideTerms_DIV'">
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
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/thesauri/tabs/tab4/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </td>
                    <!-- related to the check above about the manage thesauri tab -->
                    <xsl:if test="$THEMASUserInfo_userGroup!='ADMINISTRATOR'">
                        <td>
                            <a class="inactive" >&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</a>  
                        </td>
                    </xsl:if>
                    <td>
                        <a class="inactive" >&#160;</a>  
                    </td>
                    <!--<td><a class="inactive" >&#160;</a>  </td>-->
                        
                    <!--</xsl:if>-->
                </tr>
            </table>
        </div>
            
        <div id="content" style="text-align:left">
            <div id="DisplayCardArea"/>
            <div id="ImportExport_Data_DIV" class="tab-body" >
                <xsl:choose>
                    <xsl:when test="$tabup='ImportExport_Data_DIV'">
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
                <xsl:call-template name="ImportExportData">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/thesauri/importexport"/>
                    <xsl:with-param name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
            <div id="Fix_Data_DIV" class="tab-body" >
                <xsl:choose>
                    <xsl:when test="$tabup='Fix_Data_DIV'">
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
                
                <xsl:call-template name="FixData">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/thesauri/fixdata"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
            <div id="CreateThesaurus_DIV" class="tab-body" >
                <xsl:choose>
                    
                    <xsl:when test="$tabup='CreateThesaurus_DIV'">
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
                
                <!-- visibility: hidden; !!!!!!!!!!!!! -->
                <xsl:call-template name="CreateThesaurus">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/thesauri/createthesaurus"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
            <div id="EditGuideTerms_DIV" class="tab-body" >
                <xsl:choose>
                    <xsl:when test="$tabup='EditGuideTerms_DIV'">
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
                <!-- visibility: hidden; !!!!!!!!!!!!! -->
                <xsl:call-template name="EditGuideTerms">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/thesauri/editguideterms"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
            
            
            
        </div>
        <!-- _________________ display the current DIV_________________ -->
        <!--<script language="javascript">
            <xsl:text>Admin_Thesaurus_DisplayDIV('</xsl:text>
            <xsl:value-of select="$CurrentAdminTHES_DIV"/>
            <xsl:text>');</xsl:text>
        </script>-->
    </xsl:template>
    <xsl:template name="EditGuideTerms">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/createguideterm/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table width="100%">
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right" width="200" >
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/createprompt/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td width="350">
                        <input id="newGuideTerm" type="text" style="width:345px;"/>
                    </td>
                    <td align="left">
                        <input type="button" class="button" onclick="callEditGuideTermServlet( 'new' );" >
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/createbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
            </table>
        </fieldset>
        <br/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/deleteguideterm/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table width="100%">
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right" width="200" >
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/deleteprompt/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td width="350">
                        <select id="deleteGuideTerm" size="1" style="width:350px;">
                            <xsl:for-each select="//availableGuideTerms/GuideTerm">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                    <xsl:if test="./@card != '' ">
                                        <xsl:text> (</xsl:text>
                                        <xsl:value-of select="./@card"/>
                                        <xsl:text>)</xsl:text>
                                    </xsl:if>
                                </option>
                            </xsl:for-each>
                        </select>                        
                    </td>
                    <td align="left">
                        <input type="button" class="button" onclick="callEditGuideTermServlet( 'delete' ); ">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/deletebutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td align="right" width="200"/>                          
                    <td width="350">
                        <label for="idFordeleteEvenIfContainsTerms">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/deteteEvenIfInUse/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </label>
                        <input type="checkbox" name="deleteEvenIfContainsTerms" id="idFordeleteEvenIfContainsTerms"/>
                    </td>
                    <td align="left"/>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
            </table>
        </fieldset>
        <br/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/renameguideterm/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table width="100%">
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td align="right" width="200" >
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/renamefromprompt/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td width="350">
                        <select id="renameGuideTermFrom" size="1" style="width:350px;">
                            <xsl:for-each select="//availableGuideTerms/GuideTerm">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                    <xsl:if test="./@card != '' ">
                                        <xsl:text> (</xsl:text>
                                        <xsl:value-of select="./@card"/>
                                        <xsl:text>)</xsl:text>
                                    </xsl:if>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td align="left">
                    </td>
                </tr>
                <tr>
                    <td align="right" width="200" >
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/renametoprompt/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td width="350">
                        <input id="renameGuideTermTo" type="text" style="width:345px;"/>
                    </td>
                    <td align="left">
                        <input type="button" class="button" onclick="callEditGuideTermServlet( 'rename' );" >
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/renamebutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
            </table>
        </fieldset>
    </xsl:template>
    
    <!--IMPORT - EXPORT DATA-->
    <xsl:template name="ImportExportData">
        <xsl:param name="specificlocale" />
        <xsl:param name="THEMASUserInfo_userGroup"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/thesaurusimport/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <form method="post" style="margin-top:10px;" id="Import_DataForm" action="" ENCTYPE="multipart/form-data" >
                <table>
                    <tr style="width:800px;">
                        <td style="width:230px; text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selectxmlinputfile/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td style="width:550px;">
                            <input style="width:530px;" type="file" name="importXMLfilename" id="importXMLfilename_ID" />
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/chooseinputthesaurus/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td>
                            <input type="text" id="Import_Thesaurus_NewName_ID" name="Import_Thesaurus_NewName_NAME" class="thesaurusFieldWidth" size="17" onkeypress="if (event.keyCode == 13) return false;"/>
                            <xsl:text> </xsl:text>
                            <input class="button" type="submit" onClick="Import_DataButtonPressed('thesaurusImport');">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/importbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                            <xsl:if test="$THEMASUserInfo_userGroup = 'ADMINISTRATOR' ">
                                <xsl:text> </xsl:text>
                                <label for="importThesarusInitDb">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/initdb/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </label>
                                <input type="checkbox" name="InitDB" id="importThesarusInitDb"/>
                            </xsl:if>
                        </td>
                    </tr>
                    <!--<tr>
                        <td>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/selectxmlschematype/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>                                
                        </td>
                        <td>
                            <input type="radio" name="schematype" value="THEMAS" style="margin-left:5px;margin-right:5px;" checked="checked">
                                <xsl:text>THEMAS</xsl:text>
                            </input>
                            <input type="radio" name="schematype" value="skos" style="margin-left:10px;margin-right:5px;">
                                <xsl:text>Skos</xsl:text>
                             </input>                            
                        </td>
                    </tr>-->

                    <tr>
                        <td></td>
                        <td align="left">
                            <span style="color:#898a5e; font-size:9px;">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/newimportnamenote/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'yes'"/> 
                                </xsl:call-template>    
                            </span>
                        </td>
                    </tr>
                    <tr width="95%">
                        <td colspan="2">
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td width="780">
                                        <xsl:choose>
                                            <xsl:when test="//importReportFile or //importThesaurusMessage != ''">
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="style">
                                                    <xsl:text>visibility:hidden;</xsl:text>
                                                </xsl:attribute>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:value-of select="//importThesaurusMessage"/>
                                    </td>
                                    <td align="center" valign="bottom" style="width:150px;">
                                        <xsl:choose>
                                            <xsl:when test="//importReportFile != ''">
                                                <input style="width:125px;" class="button" type="button" >
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>previewCreateThesaurusResults('</xsl:text>
                                                        <xsl:value-of select="//importReportFile"/>
                                                        <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </input>
                                            </xsl:when>
                                            <xsl:otherwise></xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" style="text-align: right;">
                            
                            <a href="./help-files/THEMAS_XML_schema_v1.4.1.xsd" target="_blank" style="text-decoration: underline; font-style: italic; color:blue;">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/xmlschemalink/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'yes'"/> 
                                </xsl:call-template>    
                            </a>
                            
                            
                        </td>
                        
                    </tr>
                </table>
            </form>
        </fieldset>
        <br/>
        <br/>
        <!--IMPORT IN HIERARCHY-->
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/importinhierarchy/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <form method="post" style="margin-top:10px;" id="bulkImport_DataForm" action="" ENCTYPE="multipart/form-data" >
                <table>
                    <tr style="width:800px;">
                        <td style="width:230px; text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selectxmlinputfile/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td style="width:550px;">
                            <input style="width:530px;" type="file" name="bulkImportXMLfilename" id="bulkImportXMLfilename_ID" />
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selecthierarchy/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td>
                            <xsl:if test="//availableHierarchies/hierarchy">
                                <select id="bulkImportHierarchy_Id" name="bulkImportHierarchy">
                                    <xsl:for-each select="//availableHierarchies/hierarchy">
                                        <xsl:sort select="."/>
                                        <option>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                            <xsl:value-of select="."/>
                                        </option>
                                    </xsl:for-each>
                                </select>
                            </xsl:if>
                            <xsl:text> </xsl:text>
                            <input class="button" type="submit" onClick="Import_DataButtonPressed('bulkImport');" >
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/importbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr width="95%">
                        <td colspan="2">
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td width="780">
                                        <xsl:choose>
                                            <xsl:when test="//bulkImportReportFile or //bulkImportThesaurusMessage != ''">
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="style">
                                                    <xsl:text>visibility:hidden;</xsl:text>
                                                </xsl:attribute>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:value-of select="//bulkImportThesaurusMessage"/>
                                    </td>
                                    <td align="center" valign="bottom" style="width:150px;">
                                        <xsl:choose>
                                            <xsl:when test="//bulkImportReportFile and //bulkImportReportFile != ''">
                                                <input style="width:125px;" class="button" type="button" >
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> 
                                                            <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                                        </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>previewCreateThesaurusResults('</xsl:text>
                                                        <xsl:value-of select="//bulkImportReportFile"/>
                                                        <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </input>
                                            </xsl:when>
                                            <xsl:otherwise></xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </form>
        </fieldset>
        <br/>
        <br/>
        <!--EXPORT THESAURUS-->
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/exportdata/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <form style="margin-top:10px;" method="post" id="Export_DataForm" action="">
                <table>
                    <tr  width="95%">
                        <td style="width:250px; text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selectexportthesaurus/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td align="left">
                            <select id="exportThesaurus_ID" name="exportThesaurus" class="thesaurusFieldWidth" onchange="checkSkosConfiguration();">
                                <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                            </select>
                            <xsl:text> </xsl:text>
                            
                            <input class="button" type="submit" onClick="Export_DataButtonPressed();" >
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/exportbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                        </td>
                           
                        <td/>
                    </tr>
                    <tr width="95%">
                        <td style="text-align:right;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selectexportxmlschematype/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </td>
                        <td colspan="2">
                            <div style="display:inline-block; vertical-align:middle;"> 
                                <input type="radio" name="exportschematype" id="radioTHEMAS" value="THEMAS"  
                                       onclick="checkSkosConfiguration();" style="margin-left:0px;margin-right:5px;" checked="checked"/>
                                <label for="radioTHEMAS" style="cursor:pointer;">
                                    <xsl:text>THEMAS</xsl:text>
                                </label>
                            </div>
                            <div style="display:inline-block; vertical-align:middle;"> 
                            
                                <input type="radio" name="exportschematype" 
                                       id="radioSKOS" value="skos" 
                                       onclick="checkSkosConfiguration();" 
                                       style="margin-left:10px;margin-right:5px;"/>
                                <label for="radioSKOS" style="cursor:pointer;">
                                    <xsl:text>Skos</xsl:text>
                                </label>                            
                            
                            </div>
                           &#160;&#160;
                            <xsl:choose>
                                <xsl:when test="//exportFile != ''">
                                    <input type="button" class="button">
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/downloadexporfiletbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="onclick">
                                            <xsl:text>window.location ='DownloadFile?targetFile=LogFiles/'+'</xsl:text>
                                            <xsl:value-of select="//exportFile"/>
                                            <xsl:text>';</xsl:text>
                                        </xsl:attribute>
                                    </input>
                                    &#160;&#160;
                                    <input style="width:125px;" class="button" type="button" >
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/showexporfiletbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="onclick">
                                            <xsl:text>exportFilePreview('</xsl:text>
                                            <xsl:value-of select="//exportFile"/>
                                            <xsl:text>');</xsl:text>
                                        </xsl:attribute>
                                    </input>
                                </xsl:when>
                                <xsl:otherwise></xsl:otherwise>
                            </xsl:choose>
                        </td>
                        
                    </tr>
                    <tr width="95%" id="skosConceptSchemeConfigurationRowId" style="visibility:hidden;">
                        <td style="text-align:right;">
                            <label for="skosConceptSchemeId" style="cursor:pointer;">
                                <xsl:text>SKOS Concept Scheme:</xsl:text>
                            </label>
                        </td>
                        <td colspan="2">
                            <input type="text" id="skosConceptSchemeId" name="skosConceptScheme" value="" style="margin-left:10px; width:400px;"/>                            
                        </td>                       
                    </tr>
                    <tr width="95%" id="skosBaseNameSpaceConfigurationRowId" style="visibility:hidden;">
                        <td style="text-align:right;">
                            <label for="skosBaseNameSpaceId" style="cursor:pointer;">
                                <xsl:text>SKOS Namespace:</xsl:text>
                            </label>
                        </td>
                        <td colspan="2">
                            <input type="text" id="skosBaseNameSpaceId" name="skosBaseNameSpace" value="" style="margin-left:10px; width:400px;"/>                            
                        </td>                       
                    </tr>
                   
                </table>
            </form>
        </fieldset>
    </xsl:template>
    
    
    <!--FIX DATA-->
    <xsl:template name="FixData">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/title/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table width="100%">
                <tr>
                    <td>
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify" colspan="3">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/bulkstatuschange/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td align="left">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/choosehierarchy/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                        </xsl:call-template>    
                        <select id="targetHierarchy_Id" name="targetHierarchy">
                            <xsl:for-each select="//availableHierarchies/hierarchy">
                                <xsl:sort select="."/>
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td align="center">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/choosestatus/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'yes'"/> 
                        </xsl:call-template>    
                        <select id="targetStatus_Id" name="targetStatus">
                            <xsl:for-each select="//availableStatuses/status">
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
                        <input class="button" type="button" onClick="callFixServlet('FixCurrentData','Fix','HierarchyStatuses')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <br/>
                    </td>
                </tr>
            </table>
            <table width="100%" >
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/toptermerrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','TopTerm_Inconsistencies')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','TopTerm_Inconsistencies')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/nobterrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','No_BT_Terms')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','No_BT_Terms')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/wrongorphanerrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','Unclassified_Errors')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','Unclassified_Errors')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/btsvshierarchieserrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','Classes_and_BTs')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','Classes_and_BTs')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/checkorphanserrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','Check_Orphan_Hierarchy')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','Check_Orphan_Hierarchy')" >
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/rterrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','RTs_errors')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Fix','RTs_errors')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/prefferederrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','Multiple_Usage')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="disabledbutton" disabled="disabled">
                            <xsl:attribute name="onclick">
                                <xsl:text>alert('</xsl:text>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                                <xsl:text>');</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:justify">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/multiplevalueerrors/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="center">
                        <input type="button" class="button" onClick="callFixServlet('FixCurrentData','Preview','Multiple_Editors_And_Dates')">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                    <td align="center">
                        <input type="button" class="disabledbutton" disabled="disabled">
                            <xsl:attribute name="onclick">
                                <xsl:text>alert('</xsl:text>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/underconstruction/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                                <xsl:text>');</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/fixbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
        </fieldset>
    </xsl:template>
    
    
    <!-- _____________________________________________________________________________
   TEMPLATE: Create_Thesaurus
   FUNCTION: displays the contents of the Create_Thesaurus div
    _____________________________________________________________________________ -->
    <xsl:template name="CreateThesaurus">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang"/>
        <fieldset style="width:810px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/createlegend/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <br/>
            <table border="0" width="100%">
                <tr>
                    <td colspan="2">
                        <!-- List of Thesaurus -->
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/dbthesauri/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                        <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
                            <xsl:value-of select="."/>
                            <xsl:if test="name(./following-sibling::*[1]) = 'Thesaurus' ">
                                <xsl:text>, </xsl:text>
                            </xsl:if>
                        </xsl:for-each>                        
                    </td>
                </tr>
                <tr valign="top">
                    <td width="420">
                        <br/>
                        <form method="post" id="Create_ThesaurusForm" action="" >
                            
                            <!-- Thesaurus name - OK button -->
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/newthesname/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                            <input type="text" id="Create_Thesaurus_NewName_ID" name="Create_Thesaurus_NewName_NAME"
                                   class="thesaurusFieldWidth" onkeypress="if (event.keyCode == 13) return false;">
                                <!-- disable ENTER key-->
                                <xsl:attribute name="value">
                                    <xsl:value-of select="//page/content_Admin_Thesaurus/CreateThesaurusResult/NewThesaurusName"/>
                                </xsl:attribute>
                            </input>
                            <xsl:text> </xsl:text>
                            <input onClick="Create_ThesaurusOKButtonPressed()" class="button" type="submit">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                            <br/>
                            <br/>
                            <label for="initDB_id">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/initdb/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </label>
                            <input type="checkbox" name="InitDB" id="initDB_id"/>
                        </form>
                    </td>
                    <td valign="top">
                        <br/>
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/createresult/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                        
                        <textarea id="Create_Thesaurus_result_textarea_ID" name="Create_Thesaurus_result_textarea_NAME"
                                  class="thesaurustextarea" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:value-of select="//page/content_Admin_Thesaurus/CreateThesaurusResult/InitializeDBResultMessage"/>
                            <xsl:value-of select="//page/content_Admin_Thesaurus/CreateThesaurusResult/CreateThesaurusResultMessage"/>
                        </textarea>
                    </td>
                </tr>
            </table>
        </fieldset>
        <!-- ___________________________ Copy Thesaurus Action  ___________________________ -->
        <fieldset style="width:810px; margin-top:10px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/copythes/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <br/>
            <form  width="100%" method="post" id="Copy_ThesaurusForm" action="">
                <table  width="100%" cellspacing="0">
                    <tr valign="top">
                        <td style="widht:50%; text-align: center; vertical-alignment: top;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/copydbthesauri/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                            <!-- added this in order to align fields correctly -->
                            <xsl:text> </xsl:text>
                            <input class="button" type="button" style="visibility: hidden; height:5px;">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input> 
                        </td>
                        <td style="widht:50%; text-align: center; vertical-alignment: top;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/copynewname/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                            <!-- added this in order to align fields correctly -->
                            <xsl:text> </xsl:text>
                            <input class="button" type="button" style="visibility: hidden; height:5px;">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input> 
                        </td>                        
                    </tr>                    
                    <tr valign="top">
                        <td style="widht:50%; text-align: center; vertical-alignment: top;" >
                            <!-- List of Thesaurus -->
                            <select id="sourceThesaurus_ID" name="sourceThesaurus" class="thesaurusFieldWidth">
                                <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                            </select>        
                            <!-- added this in order to align fields correctly -->
                            <xsl:text> </xsl:text>
                            <input class="button" type="button" style="visibility: hidden;">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>                    
                        </td>
                        
                        <td style="widht:50%; text-align: center; vertical-alignment: top;" >
                            <input type="text" class="thesaurusFieldWidth" id="Copy_Thesaurus_NewName_ID" name="Copy_Thesaurus_NewName_NAME"  onkeypress="if (event.keyCode == 13) return false;">
                                <!-- disable ENTER key-->
                                <xsl:attribute name="value">
                                    <xsl:value-of select="//page/content_Admin_Thesaurus/CopyThesaurusResult/CopyThesaurusName"/>
                                </xsl:attribute>
                            </input>
                            <xsl:text> </xsl:text>
                            <input onClick="Copy_ThesaurusOKButtonPressed()" class="button" type="submit">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:545px;">
                            <xsl:choose>
                                <xsl:when test="//copyReportFile or //copyThesaurusResult != ''" >
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="style">
                                        <xsl:text>visibility:hidden;</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <br/><!--<br/>-->
                            <xsl:value-of select="//copyThesaurusResult" />
                        </td>
                        <td align="left" valign="bottom" >
                            <xsl:choose>
                                <xsl:when test="//copyReportFile or //copyThesaurusResult != ''">
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="style">
                                        <xsl:text>visibility:hidden;</xsl:text>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:choose>
                                <xsl:when test="//copyReportFile != ''">
                                &#160;
                                    <input style="width:125px;" class="button" type="button" >
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="onclick">
                                            <xsl:text>previewCreateThesaurusResults('</xsl:text>
                                            <xsl:value-of select="//copyReportFile"/>
                                            <xsl:text>');</xsl:text>
                                        </xsl:attribute>
                                    </input>
                                </xsl:when>
                                <xsl:otherwise></xsl:otherwise>
                            </xsl:choose>
                        </td>
                    </tr>
                </table>
            </form>
        </fieldset>
        <!-- ___________________________ Merge Thesauri Action ___________________________ -->
        <fieldset style="width:810px; margin-top:10px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/mergethesauri/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <br />
            <xsl:choose>
                <xsl:when  test="count(//content_Admin_Thesaurus/existingThesaurus/Thesaurus)&lt;=1">
                    <table width="100%">
                        <tr valign="top">
                            <td style="text-align:center; width:100%;" colspan="2">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$specificlocale/onethesmessage/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'yes'"/> 
                                </xsl:call-template>    
                            </td>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:otherwise>
                    
                    <form method="post" id="Merge_ThesauriForm" action="">
                        <table style="width:100%;">
                            <tr style="width:100%;">
                                <td style="text-align: center; vertical-alignment: top; width:30%;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/thesaurus1/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <!-- added this in order to align fields correctly -->
                                    <xsl:text> </xsl:text>
                                    <input class="button" type="button" style="visibility: hidden; height:5px;">
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </input> 
                                </td>
                                <td>
                                   
                                </td>
                                <td style="text-align: center; vertical-alignment: top; width:30%;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/thesaurus2/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <!-- added this in order to align fields correctly -->
                                    <xsl:text> </xsl:text>
                                    <input class="button" type="button" style="visibility: hidden; height:5px;">
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </input> 
                                </td>
                                <td>
                                   
                                </td>
                                <td style="text-align: center; vertical-alignment: top; ">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/mergenewthes/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <!-- added this in order to align fields correctly -->
                                    <xsl:text> </xsl:text>
                                    <input class="button" type="button" style="visibility: hidden; height:5px;">
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </input> 
                                </td>                                
                            </tr>
                            <tr style="vertical-alignment:middle;">
                                <td  style="text-align: center; vertical-alignment: top; width:30%;">
                                    <select name="thesaurus1" id="thesaurus_1_ID" class="thesaurusFieldWidth">
                                        <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
                                            <xsl:sort select="."/>
                                            <option>
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="."/>
                                                </xsl:attribute>
                                                <xsl:value-of select="."/>
                                            </option>
                                        </xsl:for-each>
                                    </select>
                                </td>
                                <td >
                                    <xsl:text> + </xsl:text>
                                </td>
                                <td  style="text-align: center; vertical-alignment: top; width:30%;">
                                    <select name="thesaurus2"  id="thesaurus_2_ID"  class="thesaurusFieldWidth">
                                        <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
                                            <xsl:sort select="."/>
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
                                    <xsl:text> = </xsl:text>
                                </td>
                                <td  style="text-align: center; vertical-alignment: top;">
                                    <input id="thesaurus_merged_ID" name="mergedThesaurusName" type="text"  class="thesaurusFieldWidth" />
                                    
                                    <xsl:text> </xsl:text>
                                    <input type="submit" class="button" onclick="Merge_ThesauriButtonPressed();" >
                                        <xsl:attribute name="value">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </input>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="5">
                                    <table border="0" >
                                        <tr>
                                            <td style="width:600px;">
                                                <xsl:choose>
                                                    <xsl:when test="//mergeReportFile or //mergeThesauriResult != ''">
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:attribute name="style">
                                                            <xsl:text>visibility:hidden;</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <br/>
                                                <xsl:value-of select="//mergeThesauriResult"/>
                                            </td>
                                            <td align="left" valign="bottom" style="width:170px;">
                                                <xsl:choose>
                                                    <xsl:when test="//mergeReportFile or //mergeThesauriResult != ''">
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:attribute name="style">
                                                            <xsl:text>visibility:hidden;</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:choose>
                                                    <xsl:when test="//mergeReportFile != ''">
                                                        &#160;
                                                        <input style="width:125px;" class="button" type="button" >
                                                            <xsl:attribute name="value">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$specificlocale/showreportbutton/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </xsl:attribute>
                                                            <xsl:attribute name="onclick">
                                                                <xsl:text>previewCreateThesaurusResults('</xsl:text>
                                                                <xsl:value-of select="//mergeReportFile"/>
                                                                <xsl:text>');</xsl:text>
                                                            </xsl:attribute>
                                                        </input>
                                                    </xsl:when>
                                                    <xsl:otherwise></xsl:otherwise>
                                                </xsl:choose>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </form>
                </xsl:otherwise>
            </xsl:choose>
        </fieldset>
        <!-- ___________________________ Delete Tehsaurus Action ___________________________ -->
        <fieldset style="width:810px; margin-top:10px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$specificlocale/deletethesaurus/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <br/>
            <table border="0" width="100%">
                <tr valign="top">
                    <td width="420">
                        <form method="post" id="Delete_ThesaurusForm" action="">
                            <!-- List of Thesaurus -->
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/selectdeletethesaurus/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                            <select id="deleteThesaurus_ID" name="deleteThesaurus"  class="thesaurusFieldWidth">
                                <xsl:for-each select="//content_Admin_Thesaurus/existingThesaurus/Thesaurus">
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
                            <!-- Thesaurus name - OK button -->
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$specificlocale/deleteprompt/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                            <input onClick="Delete_ThesaurusOKButtonPressed()" class="button" type="submit">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:choose >
                                    <xsl:when test="count(//content_Admin_Thesaurus/existingThesaurus/Thesaurus) = 1">
                                        <xsl:attribute name="disabled">
                                            <xsl:text>disabled</xsl:text>
                                        </xsl:attribute>
                                        <xsl:attribute name="class">
                                            <xsl:text>disabledbutton</xsl:text>
                                        </xsl:attribute>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="class">
                                            <xsl:text>button</xsl:text>
                                        </xsl:attribute>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </input>
                        </form>
                    </td>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$specificlocale/createresult/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </td>
                    <td align="left">
                        <textarea id="Delete_Thesaurus_result_textarea_ID" name="Delete_Thesaurus_result_textarea_NAME" class="thesaurustextarea" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:value-of select="//page/content_Admin_Thesaurus/DeleteThesaurusResult/DeleteThesaurusResultMessage"/>
                        </textarea>
                    </td>
                </tr>
            </table>
        </fieldset>
        <table >
            <tr >
                <td style="color:#898a5e; font-size:9px;" >
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$specificlocale/instructions/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                    </xsl:call-template>    
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
