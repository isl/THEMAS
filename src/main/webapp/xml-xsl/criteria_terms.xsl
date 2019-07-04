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
    <xsl:variable name="criteriatermslocale" select="$locale/primarycontentarea/terms/criteria"/>
    <xsl:template name="criteria">
        <xsl:param name="expandWithRntsOption" />
        <!-- Quick Search -->
        <xsl:call-template name="DisplayQuickSearchForm"/>
        <br/>
        <xsl:variable name="UserGroup" select="//THEMASUserInfo/userGroup"/>
            
        <!-- Search Terms -->
        <fieldset id="searchTermsForm" >
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/legend/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table style="height:100%; width:100%;" border="0" valign="top" >
                <tr>
                    <td/>
                </tr>
                <tr>
                    <td style="vertical-align:middle;">
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
                                <label for="operator_term_and" style="margin:0px 5px;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/and/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>
                                </label>
                                <input id="operator_term_and" name="operator_term" type="radio" value="and" checked="checked" style="vertical-align: text-bottom;"/>
                            </li>
                            <li>
                                <label for="operator_term_or" style="margin:0px 5px;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/or/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template> 
                                </label>
                                
                                <input id="operator_term_or" name="operator_term" type="radio" value="or" style="vertical-align: text-bottom;"/>
                            </li>
                        </ul>
                        
                           
                        
                           
                        
                    </span>
                    </td>                    
                </tr>
                
                <tr>
                    <td align="left" valign="top" cellspacing="5">
                        <table border="0">
                            <tr>
                                <td valign="top" width="380">
                                    <table id="criteria" align="left" border="1" valign="top" width="380">
                                        <tbody id="criteriaBody">
                                            <tr class="contentHeadText">
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/title/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>    
                                                </td>
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/title/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>    
                                                </td>
                                                <td>
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/inputstitle/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>    
                                                </td>
                                            </tr>
                                            <tr id="criterion">
                                                <td>
                                                    <select color="#FFFFFF" name="input_term" onchange="InputSearchFieldOnChange(this);">
                                                        <option value="name">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/term/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="translations">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/translations/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="bt">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/bt/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="nt">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/nt/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="rt">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/rt/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="uf">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/uf/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="uf_translations">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/uf_translations/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="tc">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/tc/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="primary_found_in">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/primarysource/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="translations_found_in">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/trsource/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="facet">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/facet/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="topterm">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/topterm/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <xsl:if test="$UserGroup!='READER' ">
                                                            <option value="created_by">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/creator/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </option>
                                                            <option value="modified_by">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/modificator/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </option>
                                                            <option value="created_on">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/creationdate/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </option>
                                                            <option value="modified_on">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/modificationdate/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </option>
                                                            <option value="status">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/status/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>    
                                                            </option>
                                                        </xsl:if>
                                                        <option value="scope_note">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/sn/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="translations_scope_note">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/sn_tr/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    
                                                    <select name="op_term" onclick="OperatorSearchFieldOnClick(this);">
                                                        <option value="=">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/equal/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="~">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/similar/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="~*">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/starts/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="*~">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/ends/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="!">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notequal/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="!~">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notsimilar/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="!~*">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notstarts/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                        <option value="!*~">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notends/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <input value="" name="inputvalue_term" id="inputvalue_term" type="text" style="width:155px;"/>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>                                
                                <!-- Add - remove criteria options -->
                                <td width="25" valign="top" align="left" id="criteria_add_remove">
                                    
                                    <img width="20" height="20" border="0" onClick="addOutput('criteriaBody', 'criterion','inputvalue_term');">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/src/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/title/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                    <img width="20" height="20" border="0" onClick="removeRow(document.getElementById('criteria'));">
                                        <xsl:attribute name="src">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/src/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:call-template name="getTranslationMessage"> 
                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/title/option"/> 
                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                            </xsl:call-template>    
                                        </xsl:attribute>
                                    </img>
                                </td>
                                <!-- Options select all / combine operator (AND/OR) / Expand To RNTs  -->
                                <td valign="top" >
                                    <table border="0">
                                        <!-- select or -->
                                        <tr>
                                            <td>
                                               <ul class="checkboxes checkboxes-horizontal"> 
                                                    <li>
                                                        <label for="showAllTermscbx">
                                                            <xsl:call-template name="getTranslationMessage"> 
                                                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/selectall/option"/> 
                                                                <xsl:with-param name="targetLang" select="$lang"/> 
                                                                <xsl:with-param name="disableEscape" select="'no'"/> 
                                                            </xsl:call-template>    
                                                        </label>
                                                        <input type="checkbox" id="showAllTermscbx" name="showAll" value="all">                                            
                                                            <xsl:attribute name="onclick">
                                                                <xsl:text>if(checked) {showallpressed('terms');} else{showallreleased('terms');}</xsl:text>
                                                            </xsl:attribute>
                                                        </input>
                                                    </li> 
                                                </ul>
                                            </td>
                                        </tr>                                        
                                        
                                        <!-- Optional Configuration option to expand results with rnts -->
                                        <xsl:if test="$expandWithRntsOption='yes'">
                                            <tr>
                                                <td>
                                                    <ul class="checkboxes checkboxes-horizontal"> 
                                                        <li>
                                                            <label for="extendWithRntscbxId">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/expandwithrnts/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>   
                                                            </label>
                                                            <input type="checkbox" id="extendWithRntscbxId" name="extendWithRntsName" value="extendWithRnts"/>                                            
                                                        </li> 
                                                    </ul>                                            
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <ul class="checkboxes checkboxes-horizontal"> 
                                                        <li>
                                                            <label for="restrictToApprovedcbxId">
                                                                <xsl:call-template name="getTranslationMessage"> 
                                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/criteriaarea/restrictToApproved/option"/> 
                                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                                </xsl:call-template>   
                                                            </label>
                                                            <input type="checkbox" id="restrictToApprovedcbxId" name="restrictToApprovedTermsname" value="restrictToApprovedTerms"/>                                            
                                                        </li> 
                                                    </ul>                                            
                                                </td>
                                            </tr>
                                        </xsl:if>
                                    </table>
                                </td>
                            </tr>
                       
                            <!-- select all or none of the output fields -->
                            <tr>
                                <td colspan="2">
                                    
                                    <br/>
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/title/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <input type="checkbox" name="output_term1" value="name" checked="checked" class="hiddenInput" disabled="disabled"/>                                    
                                </td>
                                <td align="right" valign="bottom">
                                    
                                    <!--
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectall/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <input type="checkbox" id="alloutputs" value="selectAllOutputs">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)selectAllOutputs('customOutputs');document.getElementById('nooutputs').checked=false;</xsl:text>
                                        </xsl:attribute>
                                    </input>
                                    &#160;&#160;
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectnone/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                    <input type="checkbox" id="nooutputs" value="deSelectAllOutputs">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)deselectAllOutputs('customOutputs');document.getElementById('alloutputs').checked=false;</xsl:text>
                                        </xsl:attribute>
                                    </input>&#160;
                                    -->
                                    
                                    <ul class="checkboxes checkboxes-horizontal"> 
                                        <li>
                                            <label for="alloutputs">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectall/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                            <input type="checkbox" id="alloutputs" value="selectAllOutputs" onclick="if(this.checked)selectAllOutputs('customOutputs');document.getElementById('nooutputs').checked=false;"/>
                                            
                                        </li> 
                                        <li>
                                            <label for="nooutputs">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/selectnone/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>  
                                            </label>
                                            <input type="checkbox" id="nooutputs" value="deSelectAllOutputs" onclick="if(this.checked)deselectAllOutputs('customOutputs');document.getElementById('alloutputs').checked=false;"/>                                            
                                        </li> 
                                    </ul>
                                </td>
                            </tr>
                        </table>
                        <hr/>
                        <!-- table containing the selection of output fields --> 
                        <table border="0" id="customOutputs">
                            <tr>
                                <!-- column 1 with output options for translations / bts / nts / tt / rts -->
                                <td style="vertical-align: top;">
                                    
                                    <ul class="checkboxes"> 
                                        <li>
                                            <input type="checkbox" id="out1" checked="checked" name="output_term1" value="translations" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out1">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/translations/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>    
                                            </label>
                                        </li> 
                                        <li>
                                            <input type="checkbox" id="out2" checked="checked" name="output_term1" value="bt" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out2">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/bt/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>   
                                            </label>
                                        </li> 
                                        <li>
                                            <input type="checkbox" id="out3" checked="checked" name="output_term1" value="nt" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out3">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/nt/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li> 
                                        <li>
                                            <input type="checkbox" id="out4" checked="checked" name="output_term1" value="topterm" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out4">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/topterm/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li> 
                                        
                                        <li>
                                            <input type="checkbox" id="out5" name="output_term1" value="rt" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out5">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/rt/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li> 
                                    </ul> 
                                    
                                </td>                                                               
                                <!-- column 2 with output options for ufs / uf (tra.) / tc / sn / sn (tra.) --> 
                                <td style="vertical-align: top;">                                                               
                                    <ul class="checkboxes"> 
                                        <li>
                                            <input type="checkbox" id="out6" name="output_term2" value="uf" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out6">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/uf/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>   
                                            </label>
                                        </li> 
                                        <li>
                                            <input type="checkbox" id="out7" name="output_term2" value="uf_translations" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out7">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/uf_translations/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>   
                                            </label>
                                        </li> 
                                        
                                        <li>
                                            <input type="checkbox" id="out8" name="output_term2" value="tc" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out8">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/tc/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li> 
                                        
                                        <li>
                                            <input type="checkbox" id="out9" name="output_term2" value="scope_note" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out9">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/sn/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li> 
                                        <li>
                                            <input type="checkbox" id="out10" name="output_term2" value="translations_scope_note" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out10">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/sn_tr/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li>                                        
                                    </ul> 
                                </td>
                                <!-- column 3 with output options for facets / sources / sources (tra.) / created by / modified by --> 
                                <td style="vertical-align: top;">
                                    <ul class="checkboxes"> 
                                        <li>
                                            <input type="checkbox" id="out11" name="output_term3" value="facet" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out11">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/facet/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li>
                                    
                                    
                                        <li>
                                            <input type="checkbox" id="out12" name="output_term3" value="primary_found_in" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out12">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/primarysource/option"/>
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li>
                                        <li>
                                            <input type="checkbox" id="out13" name="output_term3" value="translations_found_in" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                            <label for="out13">
                                                <xsl:call-template name="getTranslationMessage"> 
                                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/trsource/option"/> 
                                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                                </xsl:call-template>      
                                            </label>
                                        </li>
                                        <xsl:if test="$UserGroup!='READER' ">
                                            <li>
                                                <input type="checkbox" id="out14" name="output_term3" value="created_by" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                                <label for="out14">
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/creator/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>      
                                                </label>
                                            </li>


                                            <li>
                                                <input type="checkbox" id="out15" name="output_term3" value="modified_by" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                                <label for="out15">
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/modificator/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>      
                                                </label>
                                            </li>
                                        </xsl:if>
                                    </ul>
                                </td>
                                <!-- column 4 with output options for creation date/ modified date / status --> 
                                <td style="vertical-align: top;">
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <ul class="checkboxes"> 
                                            <li>
                                                <input type="checkbox" id="out16" name="output_term4" value="created_on" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                                <label for="out16" style="width: 170px;">
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/creationdate/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>      
                                                </label>
                                            </li>


                                            <li>
                                                <input type="checkbox" id="out17" name="output_term4" value="modified_on" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                                <label for="out17" style="width: 170px;">
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/modificationdate/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>      
                                                </label>
                                            </li>                                           
                                            <li>
                                                <input type="checkbox" id="out18" name="output_term4" value="status" onclick="uncheckSelectAllOrNone(this.checked,'');"/>
                                                <label for="out18"  style="width: 170px;">
                                                    <xsl:call-template name="getTranslationMessage"> 
                                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/outputarea/status/option"/> 
                                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                                    </xsl:call-template>      
                                                </label>
                                            </li>   
                                        </ul>
                                    </xsl:if>
                                </td>                                
                            </tr>                            
                            
                        </table>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <input name="submit4search" class="button" type="button">
                            <xsl:attribute name="value">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/searchbutton/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="onClick">
                                <xsl:text>if (CheckSearchUserInput("SearchTerms") == true) updateCriteria("SearchCriteria_Terms","criteriaTab");</xsl:text>
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
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                            </xsl:call-template>    
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td style="color:#898a5e; font-size:9px;">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'yes'"/> 
                            </xsl:call-template>    
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
        </table>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: DisplayQuickSearchForm
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayQuickSearchForm">
        <fieldset id="QuickSearchTermsForm">
            <legend>
                <xsl:call-template name="getTranslationMessage"> 
                    <xsl:with-param name="targetLangElements" select="$criteriatermslocale/quicksearchfieldset/legend/option"/> 
                    <xsl:with-param name="targetLang" select="$lang"/> 
                    <xsl:with-param name="disableEscape" select="'no'"/> 
                </xsl:call-template>    
            </legend>
            <table height="100%" border="0" valign="top">
                <tr>
                    <td/>
                </tr>
                <tr>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$criteriatermslocale/quicksearchfieldset/inputprompt/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                        <input value="" name="QuickSearchInputValue" id="QuickSearchInputValue" type="text">
                            <xsl:attribute name="onKeyPress">
                                <xsl:text>if(event.keyCode == 13) { if (CheckUserInput('QuickSearchInputValue', 'LOGINAM') == true) updateCriteria("SearchCriteria_Terms","QuickSearch");}</xsl:text>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td>
                        <a href="#">
                            <xsl:attribute name="onClick">
                                <xsl:text>if (CheckUserInput('QuickSearchInputValue', 'LOGINAM') == true) updateCriteria("SearchCriteria_Terms","QuickSearch");</xsl:text>
                            </xsl:attribute>
                            <img width="16" height="16" border="0">
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/quicksearchfieldset/quicksearchimage/src/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>                                        
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$criteriatermslocale/quicksearchfieldset/quicksearchimage/title/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </xsl:attribute>
                            </img>
                        </a>
                    </td>
                    <!--
                    <td>Ανεξάρτητα τόνων/πεζών/κεφαλαίων
                            <input type="checkbox" id="tonosAndCaseInsensitiveCheckBoxID"/>
                    </td>
                    -->
                </tr>
            </table>
        </fieldset>
    </xsl:template>
</xsl:stylesheet>
