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
    Document   : Edit_Source.xsl
    Created on : 29 Απρίλιος 2009, 11:33 πμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:output method="html"/>
    <xsl:variable name="targetEditField" select="//targetEditField"/>
    <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
    <xsl:template match="/">
        <xsl:variable name="sourcecardlocale" select="document('../../translations/translations.xml')/locale/popupcards/source"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <html>
            <head>
                
                <meta name="http-equiv" content="Content-type: text/html; charset=UTF-8"/>
                <script language="JavaScript">
                    <xsl:attribute name="src">Javascript/tabs.js
                    </xsl:attribute>
                </script>
                <title>Edit_Source.xsl</title>                
            </head>
            <body>                
                <div class="popUpCard2">
                    <xsl:choose>
                        <xsl:when test="$targetEditField ='delete_source'">
                            <div class="popUpEditCardSmall">
                                <xsl:choose>
                                    <xsl:when test="$targetEditField = 'delete_source' ">
                                        <xsl:call-template name="Edit_source_delete" >
                                            <xsl:with-param name="specificlocale" select="$sourcecardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                </xsl:choose>
                            </div>
                        </xsl:when>
                        <xsl:otherwise>
                            <div class="popUpEditCardLarge">
                            
                                <xsl:choose>
                                    <xsl:when test="$targetEditField = 'source_create' ">
                                        <xsl:call-template name="CreateNew_Source">
                                            <xsl:with-param name="specificlocale" select="$sourcecardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'move_source_references' ">
                                        <xsl:call-template name="Edit_source_move_refs">
                                            <xsl:with-param name="specificlocale" select="$sourcecardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'source_rename' ">                                                        
                                        <xsl:call-template name="Edit_source_name">
                                            <xsl:with-param name="specificlocale" select="$sourcecardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="$targetEditField = 'source_note' ">                             
                                        <xsl:call-template name="Edit_source_note">
                                            <xsl:with-param name="specificlocale" select="$sourcecardlocale/editactions"/>
                                            <xsl:with-param name="lang" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>Υπό κατασκευή</xsl:otherwise>
                                </xsl:choose>                            
                            </div>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>                
            </body>
        </html>
    </xsl:template>
    
    <!--NEW SOURCE-->
    <xsl:template name="CreateNew_Source">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_source_create">
            <legend>
                <xsl:value-of select="$specificlocale/create/title/option[@lang=$lang]"/>
            </legend>
            <table border="0" width="100%" align="center">
                <tr style="height:4px; font-size:5px;">
                    <td> </td>
                </tr>
                <tr>
                    <td>
                        <b>
                            <xsl:value-of select="$specificlocale/create/newname/option[@lang=$lang]"/>
                        </b>
                        <input id="newSourceName_Id" type="text" size="57" name="newName_Source"/>
                        <br/>
                    </td>
                </tr>
                <tr style="height:4px; font-size:5px;">
                    <td> </td>
                </tr>
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center">
                        <xsl:value-of select="$specificlocale/create/sn/option[@lang=$lang]"/>
                        <textarea id="modify_source_source_note_id" class="notetextarea" onkeydown="refreshCreateNew();" onkeyup="refreshCreateNew();"  onmouseover="refreshCreateNew();" onmouseout="refreshCreateNew();" onchange="refreshCreateNew();" onfocus="this.style.border='1px solid #000';refreshCreateNew();" onblur="this.style.border='1px solid #999966';refreshCreateNew();" name="source_note" >
                        </textarea>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetSource" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetSource"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/instructionsnote/option[@lang=$lang]"/>
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditActions_Source','edit_source_create', '','')">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--RENAME SOURCE-->
    <xsl:template name="Edit_source_name">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="renameFieldSet_Source">
            <legend>
                <xsl:value-of select="$specificlocale/rename/title/option[@lang=$lang]"/>
                <b>
                    <xsl:value-of select="//targetSource"/>
                </b>
            </legend>
            <br/>
            <div id="DynamicGenaration" align="center">
                <table cellspacing="0"  cellpadding="3">
                    <tr bgcolor="#F2F2F2" valign = "middle" cellspacing="0"  >
                        <td align="right"> <!--  style="color:#999966"-->
                            <xsl:value-of select="$specificlocale/rename/currentname/option[@lang=$lang]"/>
                        </td>
                        <td colspan="2">
                            <input id="oldSource" style="width:630px;" disabled="disabled" class="disabledbutton" name="oldsourcename" >
                                <xsl:attribute name="value">
                                    <xsl:value-of select="//targetSource"/>
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
                            <xsl:value-of select="$specificlocale/rename/newname/option[@lang=$lang]"/>
                        </td>
                        <!--EMPTY SEPERATOR LINE-->
                        <td>
                            <input type="text" style="width:630px;" id="newsourcename" name="source_rename">
                                <xsl:attribute name="onkeyup">
                                    <xsl:text>if(event.keyCode==13) {if( confirm('</xsl:text>
                                    <xsl:value-of select="$specificlocale/rename/jsconfirm/option[@lang=$lang]"/>
                                    <xsl:text>')){getServletResult('EditActions_Source','renameFieldSet_Source','ResultOf_Rename_Source','');} else { window.location.reload( true );}}</xsl:text>
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
                        <td align="right" valign="top">
                            <xsl:value-of select="$specificlocale/rename/result/option[@lang=$lang]"/>
                        </td>
                        <td>
                            <textarea id="ResultOf_Rename_Source" name="errorSourceMSG" class="renametextarea" 
                                      onfocus="this.style.border='1px solid #000'" 
                                      onblur="this.style.border='1px solid #999966'" readonly="readonly">
                                <xsl:value-of select="//currentRename/facetError/apotelesma"/>
                            </textarea>
                        </td>
                    </tr>
                </table>
                <input type="text" name="targetSource" style="visibility:hidden;">
                    <xsl:attribute name="value">
                        <xsl:value-of select="//targetSource"/>
                    </xsl:attribute>
                </input>
                <input type="text" name="targetEditField" style="visibility:hidden;">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$targetEditField"/>
                    </xsl:attribute>
                </input>
                <br/>
            </div>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:value-of disable-output-escaping="yes" select="$specificlocale/rename/instructionsnote/option[@lang=$lang]"/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="onclick">
                            <xsl:text>if (confirm('</xsl:text>
                            <xsl:value-of select="$specificlocale/rename/jsconfirm/option[@lang=$lang]"/>
                            <xsl:text>')) {getServletResult('EditActions_Source','renameFieldSet_Source','ResultOf_Rename_Source','');} else { window.location.reload( true );}</xsl:text>
                        </xsl:attribute>
                    </input> 
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--MOVE REFERENCES AND DELETE SOURCE-->
    <xsl:template name="Edit_source_move_refs">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_source_refs">
            <legend>
                <xsl:value-of select="$specificlocale/moverefs/title/option[@lang=$lang]"/>
                <b>
                    <xsl:value-of select="//source/name"/>
                </b>
            </legend>
            <br/>
            <table  border="0" style="position:relative; margin-left:auto; margin-right:auto;"  >
                <tr valign="top">
                    <td style="text-align:center;" bgcolor="#F2F2F2">
                        <xsl:value-of select="$specificlocale/moverefs/movethes/option[@lang=$lang]"/>
                        <b>
                            <xsl:value-of select="//selectedThesaurus"/>
                        </b>
                        <xsl:value-of select="$specificlocale/moverefs/tosource/option[@lang=$lang]"/>
                    </td>
                </tr>
                <tr>
                   
                    <td style="text-align:center;">
                        <select id="source_move_refs_to_id" name="move_source_references" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">
                            <xsl:for-each select="//source/possibleValue">
                                <option>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>
            </table>
            <br/>
            <table>
                <tr>
                    <td style="color:#898a5e; font-size:9px;">
                        <br/>
                        <xsl:value-of  disable-output-escaping="yes" select="$specificlocale/moverefs/instructionsnote1/option[@lang=$lang]"/>
                    </td>
                </tr>
                <tr>
                    <td style="color:#898a5e; font-size:9px;">
                        <xsl:value-of  disable-output-escaping="yes" select="$specificlocale/moverefs/instructionsnote2/option[@lang=$lang]"/>                       
                    </td>
                </tr>
            </table>
            <input type="text" name="targetSource" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="//source/name"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="visibility:hidden;">
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
                    <input type="button" class="button" >
                        <xsl:attribute name="value">
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/moverefs/moveAndDeletebtntext/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="onclick">
                            <xsl:text>getServletResult('EditActions_Source','edit_source_refs','','selectedIndexOnly');</xsl:text>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <!--EDIT SOURCE NOTE-->
    <xsl:template name="Edit_source_note">
        
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_source_source_note">
            <legend>
                <xsl:value-of select="$specificlocale/editsn/title/option[@lang=$lang]"/>
                <b>
                    <xsl:value-of select="//source/name"/>
                </b>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <tr valign="top">
                    <td bgcolor="#F2F2F2" align="center">
                        <xsl:value-of select="$specificlocale/editsn/sn/option[@lang=$lang]"/>
                    </td>
                </tr>
                <tr>                    
                    <td align="center">                                                                                           
                                            
                        <textarea id="modify_source_source_note_id" class="notetextarea" name="source_note" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'">                            
                            <xsl:value-of select="//source/source_note"/>
                        </textarea>   
                        
                    </td>
                </tr>
            </table>
            <input type="text" name="targetSource" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="//source/name"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="$targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <table width="100%">
            <tr>
                <td style="color:#898a5e; font-size:9px;">
                    <xsl:value-of disable-output-escaping="yes" select="$specificlocale/editsn/instructionsnote/option[@lang=$lang]"/>
                </td>
            </tr>
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" >
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="onclick">
                            <xsl:text>if (confirm('</xsl:text>
                            <xsl:value-of select="$specificlocale/editsn/jsconfirm/option[@lang=$lang]"/>
                            <xsl:text>')) {                                 
                                getServletResult('EditActions_Source','edit_source_source_note','','');
                                } else { window.location.reload( true ); } </xsl:text>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
	
    <!--DELETE SOURCE-->
    <xsl:template name="Edit_source_delete">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset id="edit_source_delete">
            <legend>
                <xsl:value-of  select="$specificlocale/delete/title/option[@lang=$lang]"/>
                <b>
                    <xsl:value-of select="//targetSource"/>
                </b>
            </legend>
            
            <table width="100%" >
                <tr>
                    <td style="text-align:center;">
                        <xsl:value-of  disable-output-escaping="yes" select="$specificlocale/delete/confirmmessage/option[@lang=$lang]"/>
                        <b>
                            <xsl:value-of select="//targetSource"/>
                        </b>
                        <xsl:value-of  select="$specificlocale/delete/qmark/option[@lang=$lang]"/>
                        <br/>
                        <input type="checkbox" name="deleteRefs" />
                        <xsl:value-of  select="$specificlocale/delete/deleterefs/option[@lang=$lang]"/>
                    </td>
                </tr>
                <tr align="center">
                    <td style="color:#898a5e; font-size:9px;" >
                        <br/>
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/delete/instructionsnote1/option[@lang=$lang]"/>
                        <xsl:value-of select="//targetSource"/>
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/delete/instructionsnote2/option[@lang=$lang]"/>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetSource" style="visibility:hidden;">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetSource"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="visibility:hidden;">
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
                    <input type="button" class="button" onclick="getDeleteResult('EditActions_Source','edit_source_delete', '','')">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$specificlocale/delete/deletebtntext/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload( true );">
                        <xsl:attribute name="value">
                            <xsl:value-of  select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>       
    </xsl:template>
</xsl:stylesheet>

