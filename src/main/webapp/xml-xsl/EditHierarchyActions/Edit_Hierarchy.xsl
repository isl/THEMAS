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
    Document   : Edit_Hierarchy.xsl
    Created on : 13 Φεβρουάριος 2009, 5:09 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../Configs.xsl"/>
	<xsl:variable name="targetEditField" select="//targetEditField"/>
	<xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
	<xsl:template match="/">
            <xsl:variable name="hierarchycardlocale" select="document('../../translations/translations.xml')/locale/popupcards/hierarchy"/>
            <xsl:variable name="lang" select="//page/@language"/>
		<html>
                    <head>
                        <meta name="http-equiv" content="Content-type: text/html; charset=UTF-8"/>
			<title>Edit_Hierarchy.xsl</title>
                    </head>
                    <body>
                            <xsl:choose>
                                <xsl:when test="$targetEditField ='delete_hierarchy'">
                                    
                                    <div class="popUpEditCardSmall">
                                        <xsl:choose>
                                            <xsl:when test="$targetEditField = 'delete_hierarchy' ">
                                                <xsl:call-template name="Edit_delete" >
                                                    <xsl:with-param name="specificlocale" select="$hierarchycardlocale/editactions"/>
                                                    <xsl:with-param name="lang" select="$lang"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                        </xsl:choose>
                                    </div>
                                    
                                </xsl:when>
                                <xsl:otherwise>
                                    
				<div class="popUpEditCardLarge">
					<xsl:choose>
                                                <xsl:when test="$targetEditField = 'hierarchy_rename' ">
                                                
                                                    <xsl:call-template name="rename_hierarchy">
                                                        <xsl:with-param name="specificlocale" select="$hierarchycardlocale/editactions"/>
                                                        <xsl:with-param name="lang" select="$lang"/>
                                                    </xsl:call-template>
                                                    
                                                    <table width="100%">
			<tr>
				<td style="color:#898a5e; font-size:9px;">
					<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/rename/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
				</td>
			</tr>			
                                                        
                                                        <tr>
                                                            <td valign="bottom" align="right">
                                                                <input type="button" class="button" onclick="getServletResult( 'Rename_Hierarchy','renameFieldSet_Hierarchy', 'ResultOf_Rename_Hierarchy',''); ">
                                                                    <xsl:attribute name="value">
                                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                    </xsl:attribute>
                                                                </input>
                                                                &#160;
                                                                <input type="button" class="button" onclick="window.location.reload( true );">
                                                                <xsl:attribute name="value">
                                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$hierarchycardlocale/editactions/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                    </xsl:attribute>
                                                                </input>    
                                                            </td>
                                                        </tr>
                                                        
                                                    </table>

                                                </xsl:when>
						<xsl:when test="$targetEditField = 'hierarchy_create' ">
                                                    <xsl:call-template name="CreateNew_Hierarchy">
                                                        <xsl:with-param name="specificlocale" select="$hierarchycardlocale/editactions"/>
                                                        <xsl:with-param name="lang" select="$lang"/>
                                                    </xsl:call-template>
                                                </xsl:when>
						<xsl:when test="$targetEditField = 'hierarchy_facets' ">
                                                    <xsl:call-template name="Edit_Facets" >
                                                        <xsl:with-param name="specificlocale" select="$hierarchycardlocale/editactions"/>
                                                        <xsl:with-param name="lang" select="$lang"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                
                                                <!--TODO<xsl:when test="$targetEditField = 'delete_hierarchy_and_terms' "><xsl:call-template name="Edit_delete_hierarchy_and_terms" /></xsl:when>-->
						<xsl:otherwise>
                                                    <xsl:text>UNDER CONSTRUCTION</xsl:text>
                                                    <!--<fieldset height="100">
								<legend>Επεξεργασία <xsl:value-of select="$targetEditField"/>
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
										
										&#160;<input type="button" class="button" value="Άκυρο" onclick="window.location.reload( true );"/>
									</td>
								</tr>
							</table>						-->
						</xsl:otherwise>
					</xsl:choose>
				</div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </body>
		</html>
	</xsl:template>
        
        <!--NEW HIERARCHY-->
	<xsl:template name="CreateNew_Hierarchy">
            <xsl:param name="specificlocale" />
            <xsl:param name="lang" />
		<!--similar to edit templates defined in Edit_Term.xsl-->
		<fieldset id="edit_hierarchy_create">
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
						<input id="newHierarchyName_Id" type="text" size="57" name="newName_Hierarchy"/>
						<br/>
						<br/>
					</td>
				</tr>
				<tr valign="top">
					<td bgcolor="#F2F2F2" align="center" colspan="3">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/selectedfacets/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
					</td>
                                        <!--
					<td rowspan="2" width="10%" valign="middle" align="center">
						<input type="button" onclick="copyOption('edit_Avail_Hier_FacetID','edit_Sel_Hier_FacetID');">
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </input>
                                                <br/>
                                                <br/>
                                                <input type="button" onclick="copyOption('edit_Sel_Hier_FacetID', 'edit_Avail_Hier_FacetID');">
                                                    <xsl:attribute name="value">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </input>
					</td>
					<td bgcolor="#F2F2F2" align="center" width="45%">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/existingfacets/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
					</td>-->
				</tr>
                                <tr valign="top">
                                    <td colspan="3"  class="chosenContainerClass">
                                        <select id="edit_Sel_Hier_FacetID" name="facets" multiple="true" data-placeholder="-------------" class="chosen-select">
                                                <!--<xsl:for-each select="//current/term/bt/name">
                                                    <xsl:sort select="."/> leave sorting as it was
                                                    <option selected="selected">
                                                        <xsl:attribute name="value">
                                                            <xsl:value-of select="."/>
                                                        </xsl:attribute>
                                                        <xsl:value-of select="."/>
                                                    </option>
                                                </xsl:for-each>-->
                                                 <xsl:for-each select="//availableFacets/name">
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
						<select id="edit_Sel_Hier_FacetID" onmouseover="refreshCreateNew();" onchange="refreshCreateNew();" onmouseout="refreshCreateNew();" onfocus="refreshCreateNew();" onblur="refreshCreateNew();" name="facets" size="10" style="width:100%;" ondblclick="copyOption('edit_Sel_Hier_FacetID','edit_Avail_Hier_FacetID');">
							
							<xsl:for-each select="//current/hierarchy/facet/name">
								<xsl:sort select="."/>
								<option>
									<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
					<td>
						<select id="edit_Avail_Hier_FacetID" onfocus="refreshCreateNew();" onblur="refreshCreateNew();" size="10" style="width:100%;" ondblclick="copyOption('edit_Avail_Hier_FacetID','edit_Sel_Hier_FacetID');">
							<xsl:for-each select="//availableFacets/name">
								<xsl:sort select="."/>
								<option>
									<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>-->
			</table>
                        <input type="text" name="targetHierarchy" class="hiddenInput">
                                <xsl:attribute name="value"><xsl:value-of select="//targetHierarchy"/></xsl:attribute>
                        </input>
                        <input type="text" name="targetEditField" class="hiddenInput">
                                <xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
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
                                                <input type="button" class="button" onclick="getServletResult( 'EditActions_Hierarchy','edit_hierarchy_create', '','')">
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
                
        <!--RENAME HIERARCHY-->        
        <xsl:template name="rename_hierarchy">
            <xsl:param name="specificlocale"/>
            <xsl:param name="lang"/>
            <fieldset id="renameFieldSet_Hierarchy">
                <legend>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    <b><xsl:value-of select="//targetHierarchy"/></b></legend>
                <br/><br/>
                <!--<form method="post" id="renameHierarchyForm" action="/THEMAS/Rename_Hierarchy">-->
                    <div id="DynamicGenaration" align="center">
                        
                        <table cellspacing="0"  cellpadding="3"  >
                            
                            <tr bgcolor="#F2F2F2" valign = "middle"  >
                                
                                <td  align="right" > <!--  style="color:#999966"-->
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/currentname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                
                                <td colspan="2">
                                    <input id="oldHierarchy" style="width:630px;" disabled="disabled" class="disabledbutton"  name="oldhierarchyname" >
                                        <xsl:attribute name="value"><xsl:value-of select="//targetHierarchy"/></xsl:attribute>
                                    </input>
                                </td>
                                
                            </tr>
                        
                        <!--EMPTY SEPERATOR LINE-->
                        <tr  style="height:3px; font-size:1pt;">
                            <td colspan="3">
                            </td>
                        </tr>
                            
                            <tr bgcolor="#F2F2F2" valign="middle">
                                
                                <td  align="right">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/newname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                
                                <td colspan="2">
                                    <input type="text" style="width:630px;" id="newhierarchyname" name="newhierarchyname" onkeyup="if(event.keyCode==13) getServletResult( 'Rename_Hierarchy','renameFieldSet_Hierarchy', 'ResultOf_Rename_Hierarchy','selectedIndexOnly');" />
                                </td>
                                
                            </tr>
                        
                           <!--EMPTY SEPERATOR LINE-->
                        <tr  style="height:3px; font-size:1pt;">
                            <td colspan="3">
                            </td>
                        </tr>
                            
                            <tr bgcolor="#F2F2F2" valign="middle">
                                <td  align="right"  valign="top" >
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/rename/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                <td colspan="2">
                                    <textarea id="ResultOf_Rename_Hierarchy" name="errorHierarchyMSG" class="renametextarea"
                                      onfocus="this.style.border='1px solid #000'" 
                                      onblur="this.style.border='1px solid #999966'" readonly="readonly">
                                        <xsl:value-of select="//currentRename/hierarchyError/apotelesma"/>
                                    </textarea>
                                    
                                </td>
                            </tr>
                            
                        </table>
                        <br/>
                        <br/>
                        <br/>
                        <input type="text" name="targetHierarchy" class="hiddenInput">
                            <xsl:attribute name="value"><xsl:value-of select="//targetHierarchy"/></xsl:attribute>
                        </input>

                        <input type="text" name="targetEditField" class="hiddenInput">
                            <xsl:attribute name="value"><xsl:value-of select="$targetEditField"/></xsl:attribute>
                        </input>
                        <!--<table>
                            <tr align="center" >
                                <td>
                                    <input id="renameHierarchy" value="Μετονομασία" class="button" type="button" > disabled="disabled">
                                         <xsl:attribute name="onClick">
                                             getServletResult( 'Rename_Hierarchy','renameFieldSet_Hierarchy', 'ResultOf_Rename_Hierarchy','selectedIndexOnly');
                                        </xsl:attribute>
                                    </input>&#160;
                                    <input type="button" name="Reset" value="Άκυρο" class="button" >
                                        <xsl:attribute name="onClick">
                                           window.location.reload(true);
                                        </xsl:attribute>                                       
                                    </input>                                    
                                </td>
                            </tr>
                        </table>-->
                    </div>
                <!--</form>-->
            </fieldset>
       <!-- </body>
    </html>-->
</xsl:template>

        <!--EDIT FACETS-->
        <xsl:template name="Edit_Facets" >
            <xsl:param name="specificlocale" />
            <xsl:param name="lang" />
            <fieldset id="edit_hierarchy_facets">
                <legend>
                    
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editfacets/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    <b><xsl:value-of select="//targetHierarchy"/></b>
                </legend>
		<br/>
		
                <table border="0" width="100%" align="center">
                    <tr valign="top">
                        
                        <td bgcolor="#F2F2F2" align="center" colspan="3">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editfacets/selectedfacets/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
			
                        <!--
                        <td rowspan="2" width="10%" valign="middle" align="center">
                            <input type="button" onclick="copyOption('edit_Avail_Hier_FacetID','edit_Sel_Hier_FacetID');">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generaladdbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                            <br/>
                            <br/>
                            <input type="button" onclick="copyOption('edit_Sel_Hier_FacetID', 'edit_Avail_Hier_FacetID');">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalremovebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                       </td>
                       
                       <td bgcolor="#F2F2F2" align="center" width="45%">
                           <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editfacets/existingfacets/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                       </td>-->
                    </tr>
                    <tr valign="top">
                        <td colspan="3"  class="chosenContainerClass">
                            <select id="edit_Sel_Hier_FacetID" name="hierarchy_facets" multiple="true" data-placeholder="-------------" class="chosen-select">
                                    <!--<xsl:for-each select="//current/term/bt/name">
                                        <xsl:sort select="."/> leave sorting as it was
                                        <option selected="selected">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                            <xsl:value-of select="."/>
                                        </option>
                                    </xsl:for-each>-->
                                     <xsl:for-each select="//availableFacets/name">
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
                            <select id="edit_Sel_Hier_FacetID" name="hierarchy_facets" size="12" style="width:100%;" ondblclick="copyOption('edit_Sel_Hier_FacetID', 'edit_Avail_Hier_FacetID');">
				<xsl:for-each select="//currentFacets/name">
                                    <xsl:sort select="."/>
                                    <option>
					<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
					<xsl:value-of select="."/>
                                    </option>
				</xsl:for-each>
                            </select>
                            
                            <br/>
                        </td>
			
                        <td>
                            <select id="edit_Avail_Hier_FacetID" size="12" style="width:100%;" ondblclick="copyOption('edit_Avail_Hier_FacetID','edit_Sel_Hier_FacetID');" >
                                <xsl:for-each select="//availableFacets/name">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
				</xsl:for-each>
                            </select>
			</td>
                        
                    </tr>-->
		
                </table>
                
                <input type="text" name="targetHierarchy" class="hiddenInput">
                    <xsl:attribute name="value"><xsl:value-of select="//targetHierarchy"/></xsl:attribute>
                </input>
                
		<input type="text" name="targetEditField" class="hiddenInput">
                    <xsl:attribute name="value"><xsl:value-of select="$targetEditField"/></xsl:attribute>
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
                        <input type="button" class="button" onclick="getServletResult( 'EditActions_Hierarchy','edit_hierarchy_facets', '','')">
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
        
        <!--DELETE HIERARCHY-->
        <xsl:template name="Edit_delete">
            <xsl:param name="specificlocale" />
            <xsl:param name="lang" /> 
           <fieldset id="edit_hierarchy_delete">
		<legend>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    <b><xsl:value-of select="//targetHierarchy"/></b>
                </legend>
                
                <br/>
                <br/>
                <table width="100%">
                <tr>
                        <td style="text-align:center;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/confirmmessage/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            <b>
                                <xsl:value-of select="//targetHierarchy"/>
                            </b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/qmark/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            <br/>
                            <br/>
                            <span style="color:#898a5e; font-size:9px;">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/delete/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            </span>
                            <br/>
                        </td>
                    </tr>
                </table>
                                
                
                <input type="text" name="targetHierarchy" class="hiddenInput">
                    <xsl:attribute name="value"><xsl:value-of select="//targetHierarchy"/></xsl:attribute>
                </input>
                
		<input type="text" name="targetEditField" class="hiddenInput">
                    <xsl:attribute name="value"><xsl:value-of select="$targetEditField"/></xsl:attribute>
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
                        <input type="button" class="button" onclick="getDeleteResult('EditActions_Hierarchy','edit_hierarchy_delete', '','')">
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
        
        <xsl:template name="Edit_delete_hierarchy_and_terms">
            <xsl:param name="specificlocale" />
            <xsl:param name="lang" />
            <fieldset id="edit_hierarchy_and_terms_delete">
		<legend>
                    <xsl:text>Διαγραφή όλων των όρων της Ιεραρχίας: </xsl:text>
                    <b><xsl:value-of select="//targetHierarchy"/></b>
                </legend>
          </fieldset>
        </xsl:template>
        
</xsl:stylesheet>
