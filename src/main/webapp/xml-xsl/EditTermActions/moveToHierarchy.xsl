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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    
    <!-- _________________ SearchResults TAB _________________ -->
    <xsl:variable name="TargetTermName" select="//MoveToHierarchyData/Target/name"/>
    <xsl:variable name="targetTermCanBeMovedToHierarchy" select="//MoveToHierarchyData/Target/targetTermCanBeMovedToHierarchy"/>
    <xsl:variable name="reasonTargetTermCannotBeMovedToHierarchy" select="//MoveToHierarchyData/Target/reasonTargetTermCannotBeMovedToHierarchy"/>
    <xsl:variable name="MoveToHierarchyResultsMessage" select="//MoveToHierarchyData/MoveToHierarchyResultsMessage"/>
    <xsl:variable name="BLANK_VALUE_for_empty_select_item">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:variable>
    <xsl:variable name="TDwidth1">15%</xsl:variable>
    <xsl:variable name="TDwidth2">35%</xsl:variable>
    <xsl:variable name="TDwidth3">15%</xsl:variable>
    <xsl:variable name="SelectSize1">3</xsl:variable>
    <xsl:variable name="SelectSize2">9</xsl:variable>
    
    <!-- _____________________________________________________________________________
            TEMPLATE: MoveToHierarchy
            FUNCTION: displays the down part tab for the operation MoveToHierarchy
      _____________________________________________________________________________ -->
    <xsl:template match="/page" name="moveToHierarchy">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
	
        <fieldset id="moveTo_HierarchyFieldSetID" style="padding-left:3px;">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <b> 
                    <xsl:value-of select="//targetTerm"/>
                </b>
            </legend>
			
            <xsl:if test="count($MoveToHierarchyResultsMessage) = 0 "> <!-- XSL called by MoveToHierarchy servlet -->
                <xsl:call-template name="MoveToHierarchyDisplayForm">
                    <xsl:with-param name="specificlocale" select="$specificlocale"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </xsl:if>
            
            <xsl:if test="count($MoveToHierarchyResultsMessage) != 0 "> <!-- XSL called by MoveToHierarchyResults servlet -->
                <br></br>
                <!-- display the results message -->
                <xsl:value-of  disable-output-escaping="yes" select="//MoveToHierarchyData/MoveToHierarchyResultsMessage"/>
                &#160;&#160;&#160;
                <!-- εικονίδιο για την "Επιστροφή στη φόρμα Μετακίνησης του Όρου σε Ιεραρχία" -->
                <a href="#" onClick="window.location.reload(true);">						
                    <img width="16" height="16" border="0">
                        <xsl:attribute name="src">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/moveimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>                                                
                        <xsl:attribute name="title">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/moveimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>                                                
                    </img>
                </a>
                <br/>&#160;
            </xsl:if>				
			
            <xsl:if test="count($TargetTermName) = 0 "> <!-- XSL called by TAB switch -->
                <br></br>
            </xsl:if>
            
        </fieldset>
            
    </xsl:template>

    <!-- _____________________________________________________________________________
            TEMPLATE: MoveToHierarchyDisplayForm
            FUNCTION: displays the form of the down part tab for the operation MoveToHierarchy only
                      in case TargetTermName is != ''
      _____________________________________________________________________________ -->
    <xsl:template name="MoveToHierarchyDisplayForm">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <xsl:choose>
            <xsl:when test="$targetTermCanBeMovedToHierarchy = 'true' ">
                <xsl:call-template name="MoveToHierarchyDisplayFormWithData">
                    <xsl:with-param name="specificlocale" select="$specificlocale"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="MoveToHierarchyDisplayFormWithErrorMessage">
                    <xsl:with-param name="specificlocale" select="$specificlocale"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>		
	
    <!-- _____________________________________________________________________________
            TEMPLATE: MoveToHierarchyDisplayFormWithData
            FUNCTION: displays the form of the down part tab for the operation MoveToHierarchy only
                      in case TargetTermName is allowed to be moved to Hierarchy
          _____________________________________________________________________________ -->
    <xsl:template name="MoveToHierarchyDisplayFormWithData">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
		
        <table width="100%" border="0" >
            <tr>
                <!-- Όρος: -->
                <td bgcolor="#F2F2F2" align="right">
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth1"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/term/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth2"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <xsl:value-of select="$TargetTermName"/>
						<!-- Term Name: HIDDEN (so as to be able to be taken as input parameter by the action-servlet of this form) -->
                    <input type="hidden" name="TargetTermName" style="width:1px; height:1px;">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$TargetTermName"/>
                        </xsl:attribute>
                    </input>
                </td>
                <!-- Μετακίνηση από την Ιεραρχία: -->
                <td bgcolor="#F2F2F2" align="right">
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth3"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/fromhier/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <select id="MoveFromHierarchy" name="MoveFromHierarchy">
                        <xsl:attribute name="size">
                            <xsl:value-of select="$SelectSize1"/>
                        </xsl:attribute>
                        <xsl:for-each select="//MoveToHierarchyData/TargetHierarchies/name">
                            <option>
                                <xsl:if test=". = ../name[1] ">
                                    <xsl:attribute name="selected">true</xsl:attribute>
                                </xsl:if>
                                <xsl:attribute name="value">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                                <xsl:value-of select="."/>
                            </option>
                        </xsl:for-each>
                        <xsl:if test="count(//MoveToHierarchyData/TargetHierarchies/name) = 0 "> <!-- in case target does not belong to any Hierarchy -->
                            <option>
                                <xsl:value-of select="$BLANK_VALUE_for_empty_select_item"/>
                            </option>
                        </xsl:if>
                    </select>
                </td>
            </tr>
            
            <!-- Μετακίνηση προς την Ιεραρχία: -->
            <tr>
                <td bgcolor="#F2F2F2" align="right">
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth1"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/tohier/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth2"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <select id="DestinationHierList" name="DestinationHierList" onchange="MTH_ToHierListChanged('DestinationHierList');"  style="width:100%;">
                        <xsl:attribute name="size">
                            <xsl:value-of select="$SelectSize2"/>
                        </xsl:attribute>
                        <xsl:for-each select="//MoveToHierarchyData/DBHierarchies/name">
                            <option>
                                <xsl:attribute name="value">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                                <!--<xsl:value-of select="position()"/>. -->
                                <xsl:value-of select="."/>
                            </option>
                        </xsl:for-each>
                        <xsl:if test="count(//MoveToHierarchyData/DBHierarchies/name) = 0 "> <!-- in case XSL is used by MoveToHierarchyResults servlet -->
                            <option>
                                <xsl:value-of select="$BLANK_VALUE_for_empty_select_item"/>
                            </option>
                        </xsl:if>
                    </select>
                </td>
                
                <!-- Πλατύτερος  όρος: -->
                <td bgcolor="#F2F2F2" align="right">
                    <xsl:attribute name="style">
                        <xsl:text>width: </xsl:text>
                        <xsl:value-of select="$TDwidth3"/>
                        <xsl:text>;</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/targetbt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <select id="MoveBTterm" name="MoveBTterm" style="width:100%;">
                        <xsl:attribute name="size">
                            <xsl:value-of select="$SelectSize2"/>
                        </xsl:attribute>
                        <!-- options to be filled dynamically with javascript and AJAX onchange="MTH_ToHierListChanged(this);" -->
                        <option>
                            <xsl:value-of select="$BLANK_VALUE_for_empty_select_item"/>
                        </option>
                    </select>
                </td>
            </tr>
        </table>
        <!-- Επιλογές -->
        <table border="0" cellspacing="0" cellpadding="0"  style="width:800px; margin:0px;">
            <tr>
                <td align="center">
                    <!--<fieldset style="width:780px;">
                        <legend>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/mode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </legend>-->
                        <table border="0" cellspacing="2" style="width:780px;" >
                            <tr align="center">
                                <td colspan="3">
                                    <b>
                                        <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/mode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    -->
                                        <br/>
                                    </b>
                                </td>
                            </tr>
                            <tr align="center">
                                <td >
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/movenode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                <td>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/movesubtree/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                <td>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/connectsubtree/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                            </tr>
                            <tr>
                                <td align="center">
                                    <input type="radio" name="MoveToHierarchyOption" value="MOVE_NODE_ONLY"  style="border: 0px;" checked="checked"/>
                                </td>
                                <td align="center">
                                    <input type="radio" name="MoveToHierarchyOption" value="MOVE_NODE_AND_SUBTREE"  style="border:0px;"/>
                                </td>
                                <td align="center">
                                    <input type="radio" name="MoveToHierarchyOption" value="CONNECT_NODE_AND_SUBTREE"  style="border:0px;"/>
                                </td>
                            </tr>
                        </table>
                    <!--</fieldset-->
                </td>
            </tr>
        </table>						
			
			<!-- ABANDONED detete bts Πλατύτεροι όροι - button Διαγραφή
        <table border="0" cellspacing="0" cellpadding="0"  style="width:800px; margin:0px;">
            <tr>
                <td align="center">
                    <fieldset style="text-align:center; width:780px;" >
                        <legend style="text-align:left;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/bts/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </legend>
                         
							in case target has at least 1 BT 
                        <xsl:if test="count(//MoveToHierarchyData/TargetBTs/name) &gt; 0">
                            <select id="TargetBTforDeletion" name="TargetBTforDeletion" size="1">
                                <xsl:for-each select="//MoveToHierarchyData/TargetBTs/name">
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                            </select>
                            &#160;											
                            <input type="button">
                                <xsl:attribute name="value">                                                                     
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/baseform/deletebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="onClick">                                                                     
                                    <xsl:text>MTH_DeleteBTButtonPressed();</xsl:text>
                                </xsl:attribute>
                                <xsl:if test="count(//MoveToHierarchyData/TargetBTs/name) = 1">  in case target has only 1 => disable button 
                                    <xsl:attribute name="disabled">
                                        <xsl:text>disabled</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="class">
                                        <xsl:text>disabledbutton</xsl:text>
                                    </xsl:attribute>
                                </xsl:if>
                                <xsl:if test="count(//MoveToHierarchyData/TargetBTs/name) > 1">
                                    <xsl:attribute name="class">
                                        <xsl:text>button</xsl:text>
                                    </xsl:attribute>
                                </xsl:if>
                            </input>
                        </xsl:if>
                         in case target has no BTs 
                        <xsl:if test="count(//MoveToHierarchyData/TargetBTs/name) = 0">
                            <br></br>
                        </xsl:if>
                    </fieldset>
                </td>
            </tr>
        </table>									
			-->			
        <!-- Αποτέλεσμα - Αποθήκευση -->
        <table width="100%">
            <tr align="center">
                <td></td>
            </tr>
            
            <tr>
                <td></td>
            </tr>
        </table>
        
    </xsl:template>	
	
    <!-- _____________________________________________________________________________
                TEMPLATE: MoveToHierarchyDisplayFormWithErrorMessage
                FUNCTION: displays the reason why TargetTermName is NOT allowed to be moved to Hierarchy
      _____________________________________________________________________________ -->
    <xsl:template name="MoveToHierarchyDisplayFormWithErrorMessage">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        
        <table width="100%" height="100%" border="0">
            <tr>
                <td bgcolor="#F2F2F2">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/formwitherror/termname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <xsl:value-of select="$TargetTermName"/>
                </td>
            </tr>
            <tr>
                <td bgcolor="#F2F2F2">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/formwitherror/msg/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/formwitherror/nomove/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
            </tr>
            <tr>
                <td bgcolor="#F2F2F2">
                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/moveterm/formwitherror/reason/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                </td>
                <td>
                    <xsl:value-of select="$reasonTargetTermCannotBeMovedToHierarchy"/>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
