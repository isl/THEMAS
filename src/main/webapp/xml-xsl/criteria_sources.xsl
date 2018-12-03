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
    Document   : criteria_sources.xsl
    Created on : 29 Απρίλιος 2009, 2:54 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:variable name="criteriasourceslocale" select="$locale/primarycontentarea/sources/criteria"/>
    <xsl:template name="criteria_sources">
        <fieldset id="searchSourcesForm">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <table style="width:100%;" height="100%" border="0" valign="top">                
                <tr>
                    <td/>
                </tr>                
                <tr>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                </tr>                
                <tr>
                    <td align="left" valign="top" cellspacing="5" >
                        <table border="0" >
                            <tr> 
                                <td  valign="top" width="380" >
                                    <table id="criteria_sources" name="criteria_sources" align="left" border="1" valign="top" width="380">
                                        <tbody id="criteriaBody_source">
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
                                            <tr id="criterion_source">
                                                <td>
                                                    <select color="#FFFFFF" name="input_source" style="width:119px;">
                                                        <option value="name"><xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/criteriaarea/searchfields/source/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    </option>
                                                        <option value="primary_found_in"><xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/criteriaarea/searchfields/primarysource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    </option>
                                                        <option value="translations_found_in"><xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/criteriaarea/searchfields/trsource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    </option>
                                                        <option value="source_note"><xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/criteriaarea/searchfields/sourcenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <select name="op_source">
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
                                                    <input value="" name="inputvalue_source" id="inputvalue_source" type="text" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'" style="width:155px;"/>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td style="width:25px;" valign="top" align="left" id="criteria_sources_add_remove">
                                    <!--<br/>-->
                                    <img width="20" height="20" border="0" onClick="addOutput('criteriaBody_source', 'criterion_source','inputvalue_source');">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                    <!--<br/>-->
                                    <img width="20" height="20" border="0" onClick="removeRow(document.getElementById('criteria_sources'));">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                </td>
                                <td valign="top" >
                                    <table border="0">
                                        <tr>
                                            <td>
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/criteriaarea/selectall/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                <input name="showAll" type="checkbox" value="all" >
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>if(checked) {showallpressed('sources');} else{showallreleased('sources');}</xsl:text>
                                                    </xsl:attribute>
                                                </input>

                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="right" id="criteria_sources_and_or">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                               <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/and/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    <input name="operator_source" type="radio" value="and" checked="checked"/>
                                               <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/or/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    <input name="operator_source" type="radio" value="or"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <br/><br/>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <input type="checkbox" name="output_source" value="name" checked="checked" class="hiddenInput" disabled="disabled"/>
                                </td>
                                <td align="right" valign="bottom">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectall/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <input type="checkbox" id="alloutputs_source" value="selectAllOutputs_source">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)selectAllOutputs('customOutputs_source');document.getElementById('nooutputs_source').checked=false;</xsl:text>
                                       </xsl:attribute>
                                        </input>
                                
                                    &#160;&#160;
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectnone/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    <input type="checkbox" id="nooutputs_source" value="deSelectAllOutputs_source">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)deselectAllOutputs('customOutputs_source');document.getElementById('alloutputs_source').checked=false;</xsl:text>
                                       </xsl:attribute>
                                        </input>&#160;
                                </td>
                            </tr>
                        </table>
                        
                        <hr/>
                        
                        <table border="0" id="customOutputs_source">
                            <tr><!--ROW 1-->
                                <td style="width:180px;">
                                    <input type="checkbox" name="output_source" value="source_note" checked="checked" onclick="if(this.checked)document.getElementById('nooutputs_source').checked=false; else document.getElementById('alloutputs_source').checked=false;"/>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/outputarea/sourcenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                <td style="width:180px;">
                                    <input type="checkbox" name="output_source" value="primary_found_in" onclick="if(this.checked)document.getElementById('nooutputs_source').checked=false; else document.getElementById('alloutputs_source').checked=false;"/>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/outputarea/primarysource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                                <td style="width:180px;">
                                    <input type="checkbox" name="output_source" value="translations_found_in" onclick="if(this.checked)document.getElementById('nooutputs_source').checked=false; else document.getElementById('alloutputs_source').checked=false;" />
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriasourceslocale/outputarea/trsource/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <input name="submit4sourcesearch" class="button" type="button">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/searchbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="onClick">
                                <xsl:text>if (CheckSearchUserInput("SearchSources") == true) updateCriteria("SearchCriteria_Sources","criteriaTab");</xsl:text>
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
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td style="color:#898a5e; font-size:9px;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </xsl:otherwise>
                </xsl:choose>

            </tr>
        </table>        
    </xsl:template>

</xsl:stylesheet>
