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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <xsl:variable name="criteriafacetslocale" select="$locale/primarycontentarea/facets/criteria"/>
    <xsl:template name="criteria_facets">
        <fieldset id="searchFacetsForm">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <table width="100%" height="100%" border="0" valign="top">
                <tr>
                    <td/>
                </tr>
                <tr>
                    <td>
                        <xsl:call-template name="getTranslationMessage">
                            <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/title/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                        
                        <xsl:text> </xsl:text>
                        <span style="margin-left:20px;">
                        <!-- Logical combine operator selection -->
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/title/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                        
                        <ul class="checkboxes checkboxes-horizontal" style="vertical-align: text-bottom; display:inline-block;">
                            <li>
                                <label for="operator_facet_and" style="margin:0px 5px;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/and/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>
                                </label>
                                <input id="operator_facet_and" name="operator_facet" type="radio" value="and" checked="checked" style="vertical-align: text-bottom;"/>
                            </li>
                            <li>
                                <label for="operator_facet_or" style="margin:0px 5px;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/or/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template> 
                                </label>
                                
                                <input id="operator_facet_or" name="operator_facet" type="radio" value="or" style="vertical-align: text-bottom;"/>
                            </li>
                        </ul> 
                        
                        </span>
                        <!--<tr>
                                            <td align="left" id="criteria_facets_and_or">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/title/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/and/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                                <input name="operator_facet" type="radio" value="and" checked="checked"/>
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/or/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                                <input name="operator_facet" type="radio" value="or"/>
                                            </td>
                                        </tr>-->
                    </td>
                </tr>
                <tr>
                    <td align="left" valign="top" cellspacing="5">
                        <table border="0">
                            <tr>
                                <td valign="top" width="380">
                                    <table id="criteria_facets" align="left" border="1" valign="top">
                                        <tbody id="criteriaBody_facet">
                                            <tr class="contentHeadText">
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </td>
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </td>
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/inputstitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </td>
                                            </tr>
                                            <tr id="criterion_facet">
                                                <td>
                                                    <select color="#FFFFFF" name="input_facet" style="width:119px;">
                                                        <option value="name">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/criteriaarea/searchfields/facet/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="term">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/criteriaarea/searchfields/containsterm/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <select name="op_facet">
                                                        <option value="=">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/equal/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="~">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/similar/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="~*">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/starts/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="*~">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/ends/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="!">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notequal/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="!~">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notsimilar/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="!~*">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notstarts/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                        <option value="!*~">
                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notends/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                        </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <input value="" name="inputvalue_facet" id="inputvalue_facet" type="text" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'" style="width:159px;"/>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                                
                                <td width="25" valign="top" align="left" id="criteria_facets_add_remove">
                                    <img width="20" height="20" border="0" onClick="addOutput('criteriaBody_facet', 'criterion_facet','inputvalue_facet');">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                    <img width="20" height="20" border="0" onClick="removeRow(document.getElementById('criteria_facets'));">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                </td>
                                
                                <td align="left" valign="top">
                                    <table border="0">
                                        <tr>
                                            <td>
                                                <ul class="checkboxes checkboxes-horizontal"> 
                                                    <li>
                                                        <label for="showAllFacetscbx">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/criteriaarea/selectall/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </label>
                                                        <input type="checkbox" id="showAllFacetscbx" name="showAll" value="all">                                            
                                                            <xsl:attribute name="onclick">
                                                                <xsl:text>if(checked) {showallpressed('facets');} else{showallreleased('facets');}</xsl:text>
                                                            </xsl:attribute>
                                                        </input>
                                                    </li> 
                                                </ul>
                                            <!--
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/criteriaarea/selectall/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                                <input name="showAll" type="checkbox" value="all">
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>if(checked) {showallpressed('facets');} else{showallreleased('facets');}</xsl:text>
                                                    </xsl:attribute>
                                                </input>
                                                             -->
                                            </td>
                                        </tr>
                                        
                                    </table>
                                    
                                </td>
                                
                            </tr>
                       
                            <tr>
                                <td colspan="2">
                                    <br/>
                                    <br/>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <input type="checkbox" name="output_facet" value="name" checked="checked"  class="hiddenInput" disabled="disabled"/>
                                </td>
                                <td align="right" valign="bottom">
                                   <!-- <xsl:text>Επιλογή όλων: </xsl:text>
                                    <input type="checkbox" id="alloutputs_facet" value="selectAllOutputs_facet">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)selectAllOutputs('customOutputs_facet');document.getElementById('nooutputs_facet').checked=false;</xsl:text>
                                       </xsl:attribute>
                                    </input>
                                    &#160;&#160;<xsl:text>Αποεπιλογή όλων: </xsl:text>
                                    <input type="checkbox" id="nooutputs_facet" value="deSelectAllOutputs_facet">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)deselectAllOutputs('customOutputs_facet');document.getElementById('alloutputs_facet').checked=false;</xsl:text>
                                       </xsl:attribute>
                                    </input>&#160;-->
                                </td>
                                
                            </tr>
                            
                        </table>
                        
                        <hr/>
                        
                        <table border="0" id="customOutputs_facet">
                            <tr><!--ROW 1-->
                                <td>
                                    <ul class="checkboxes"> 
                                        <li>
                                            <input type="checkbox" id="out1" name="output_facet" value="hierarchy" checked="checked"/>
                                            <label for="out1">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/outputarea/hiers/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>   
                                            </label>
                                        </li> 
                                    </ul>
                                    <!--
                                    <input type="checkbox" checked="checked" name="output_facet" value="hierarchy"/>
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/outputarea/hiers/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>  
                                    -->
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                
                <tr >
                    <td align="right">
                        <input name="submit4facetsearch" class="button" type="button">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/searchbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="onClick">
                                <xsl:text>if (CheckSearchUserInput("SearchFacets") == true) updateCriteria("SearchCriteria_Facets","criteriaTab");</xsl:text>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                
                <tr>
                    <td valign="top">
                    </td>
                </tr>
            </table>
			
        </fieldset>
        
        <table align="left">
            <tr>
                <xsl:choose>
                    <xsl:when test="//results!=''">
                        <td style="color:#FF0000; font-size:9px;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td style="color:#898a5e; font-size:9px;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriafacetslocale/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
