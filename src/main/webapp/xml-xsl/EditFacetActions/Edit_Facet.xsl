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
    Document   : Edit_Facet.xsl
    Created on : 23 Μάρτιος 2009, 7:34 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../Configs.xsl"/>
    <xsl:include href="../utilities.xsl"/>    
    <xsl:variable name="targetEditField" select="//targetEditField"/>
    <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
    <xsl:template match="/">
        <xsl:variable name="facetcardlocale" select="document('../../translations/translations.xml')/locale/popupcards/facet"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <html>
            <head>
                <meta name="http-equiv" content="Content-type: text/html; charset=UTF-8"/>
                <script language="JavaScript">
                    <xsl:attribute name="src">Javascript/tabs.js?v=@DeploymentTimestamp@</xsl:attribute>
                </script>
                <title>Edit_Facet.xsl</title>
            </head>
            <body>
                <xsl:choose>
                    <xsl:when test="$targetEditField ='delete_facet'">
                        <div class="popUpEditCardSmall">
                            <xsl:choose>
                                <xsl:when test="$targetEditField = 'delete_facet' ">
                                    <xsl:call-template name="Edit_delete">
                                        <xsl:with-param name="specificlocale" select="$facetcardlocale/editactions"/>
                                        <xsl:with-param name="lang" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:when>
                            </xsl:choose>
                        </div>
                    </xsl:when>
                    <xsl:otherwise>
                        <div class="popUpEditCardLarge">
                            <xsl:choose>
                                <xsl:when test="$targetEditField = 'facet_rename' ">
                                    <xsl:call-template name="rename_facet">
                                        <xsl:with-param name="specificlocale" select="$facetcardlocale/editactions"/>
                                        <xsl:with-param name="lang" select="$lang"/>
                                    </xsl:call-template>
                                    <table width="100%">
                                        <tr>
                                            <td style="color:#898a5e; font-size:9px;">
                                                 <!--<xsl:variable name="replaced">
                                                    <xsl:call-template name="replace-string">
                                                        <xsl:with-param name="text" select="$facetcardlocale/editactions/rename/instructionsnote/option[@lang=$lang]"/>                                                        <xsl:with-param name="replace" select="'%s'"/>
                                                        <xsl:with-param name="with" select="//maxFacetUTF8Length"/>
                                                    </xsl:call-template>
                                                </xsl:variable>
                                                <xsl:value-of disable-output-escaping="yes" select="$replaced"/>-->
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/rename/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                            </td>
                                        </tr>
                                        <tr>
                                            <td valign="bottom" align="right">
                                                <input type="button" class="button" onclick="getServletResult( 'Rename_Facet','renameFieldSet_Facet', 'ResultOf_Rename_Facet',''); ">
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </input>
                                                &#160;
                                                <input type="button" class="button" onclick="window.location.reload( true );">
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$facetcardlocale/editactions/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </input>
                                            </td>
                                        </tr>
                                    </table>
                                </xsl:when>
                                <xsl:when test="$targetEditField = 'facet_create' ">
                                    <xsl:call-template name="CreateNew_Facet">
                                        <xsl:with-param name="specificlocale" select="$facetcardlocale/editactions"/>
                                        <xsl:with-param name="lang" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>Under construction</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>
            </body>
        </html>
    </xsl:template>
    
    <!--CREATE NEW FACET-->
    <xsl:template name="CreateNew_Facet">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_facet_create">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr>
                    <td colspan="3">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/newname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                        <input id="newFacetName_Id" type="text" size="57" name="newName_Facet"/>
                        <br/>
                        <br/>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetFacet" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetFacet"/>
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

                    <!--<xsl:variable name="replaced">
                        <xsl:call-template name="replace-string">
                            <xsl:with-param name="text" select="$specificlocale/create/instructionsnote/option[@lang=$lang]"/>
                            <xsl:with-param name="replace" select="'%s'"/>
                            <xsl:with-param name="with" select="//maxFacetUTF8Length"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of disable-output-escaping="yes" select="$replaced"/>
                    -->
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
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Facet','edit_facet_create', '','')">
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
    
    <!--RENAME FACET-->
    <xsl:template name="rename_facet">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="renameFieldSet_Facet">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetFacet"/>
                </b>
            </legend>
            <br/>
            
            <div id="DynamicGenaration" align="center">
                <table cellspacing="0"  cellpadding="3">
                    <tr bgcolor="#F2F2F2" valign = "middle" cellspacing="0"  >
                        <td align="right" ><!--  style="color:#999966"-->
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/currentname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                        <td colspan="2">
                            <input id="oldFacet" style="width:630px;" disabled="disabled" class="disabledbutton" name="oldfacetname" >
                                <xsl:attribute name="value">
                                    <xsl:value-of select="//targetFacet"/>
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                        <!--EMPTY SEPERATOR LINE-->
                    <tr  style="height:3px; font-size:1pt;">
                        <td colspan="3">
                        </td>
                    </tr>
                    <tr bgcolor="#F2F2F2" valign="middle">
                        <td align="right" >
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/newname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                            <!--EMPTY SEPERATOR LINE-->
                        <td colspan="2">
                            <input type="text" style="width:630px;" id="newfacetname" name="newfacetname" onkeyup="if(event.keyCode==13) getServletResult( 'Rename_Facet','renameFieldSet_Facet', 'ResultOf_Rename_Facet','selectedIndexOnly');"/>
                        </td>
                    </tr>
                    <tr style="height:3px; font-size:1pt;">
                        <td colspan="3">
                        </td>
                    </tr>
                    <tr bgcolor="#F2F2F2" valign="middle">
                        <td align="right" valign="top">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
                        <td colspan="2">
                            <textarea id="ResultOf_Rename_Facet" name="errorFacetMSG" class="renametextarea"
                                      onfocus="this.style.border='1px solid #000'" 
                                      onblur="this.style.border='1px solid #999966'" readonly="readonly">
                                <xsl:value-of select="//currentRename/facetError/apotelesma"/>
                            </textarea>
                        </td>
                    </tr>
                </table>
                    
                <input type="text" name="targetFacet" class="hiddenInput">
                    <xsl:attribute name="value">
                        <xsl:value-of select="//targetFacet"/>
                    </xsl:attribute>
                </input>

                <input type="text" name="targetEditField" class="hiddenInput">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$targetEditField"/>
                    </xsl:attribute>
                </input>
                <br/>
            </div>
        </fieldset>
    </xsl:template>
    <!--DELETE FACET-->
    <xsl:template name="Edit_delete">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_facet_delete">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b>
                    <xsl:value-of select="//targetFacet"/>
                </b>
            </legend>
            <br/>
            <br/>
            <table width="100%">
                <tr>
                    <td style="text-align:center;">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/confirmmessage/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="//targetFacet"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/qmark/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <br/>
                        <br/>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetFacet" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetFacet"/>
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
                    <input type="button" class="button" onclick="getDeleteResult('EditActions_Facet','edit_facet_delete', '','')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/deletebtntext/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
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
</xsl:stylesheet>
