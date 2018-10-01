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
        <!-- Quick Search -->
        <xsl:call-template name="DisplayQuickSearchForm"/>
        <br/>
        <xsl:variable name="UserGroup" select="//THEMASUserInfo/userGroup"/>
            
        <!-- Search Terms -->
        <fieldset id="searchTermsForm" >
            <legend>
                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/legend/option[@lang=$lang]"/>
            </legend>
            <table style="height:100%; width:100%;" border="0" valign="top" >
                <tr>
                    <td/>
                </tr>
                <tr>
                    <td>
                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/title/option[@lang=$lang]"/>
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
                                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/title/option[@lang=$lang]"/>
                                                </td>
                                                <td>
                                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/title/option[@lang=$lang]"/>
                                                </td>
                                                <td>
                                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/inputstitle/option[@lang=$lang]"/>
                                                </td>
                                            </tr>
                                            <tr id="criterion">
                                                <td>
                                                    <select color="#FFFFFF" name="input_term" onchange="InputSearchFieldOnChange(this);">
                                                        <option value="name">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/term/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="translations">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/translations/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="bt">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/bt/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="nt">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/nt/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="rt">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/rt/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="uf">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/uf/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="uf_translations">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/uf_translations/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="tc">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/tc/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="primary_found_in">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/primarysource/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="translations_found_in">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/trsource/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="facet">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/facet/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="topterm">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/topterm/option[@lang=$lang]"/>
                                                        </option>
                                                        <xsl:if test="$UserGroup!='READER' ">
                                                            <option value="created_by">
                                                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/creator/option[@lang=$lang]"/>
                                                            </option>
                                                            <option value="modified_by">
                                                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/modificator/option[@lang=$lang]"/>
                                                            </option>
                                                            <option value="created_on">
                                                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/creationdate/option[@lang=$lang]"/>
                                                            </option>
                                                            <option value="modified_on">
                                                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/modificationdate/option[@lang=$lang]"/>
                                                            </option>
                                                            <option value="status">
                                                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/status/option[@lang=$lang]"/>
                                                            </option>
                                                        </xsl:if>
                                                        <option value="scope_note">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/sn/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="translations_scope_note">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/searchfields/sn_tr/option[@lang=$lang]"/>
                                                        </option>
                                                    </select>
                                                </td>
                                                <td>
                                                    
                                                    <select name="op_term" onclick="OperatorSearchFieldOnClick(this);">
                                                        <option value="=">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/equal/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="~">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/similar/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="~*">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/starts/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="*~">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/ends/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="!">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notequal/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="!~">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notsimilar/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="!~*">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notstarts/option[@lang=$lang]"/>
                                                        </option>
                                                        <option value="!*~">
                                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/fieldoperators/notends/option[@lang=$lang]"/>
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
                                <td width="25" valign="top" align="left" id="criteria_add_remove">
                                    <img width="20" height="20" border="0" onClick="addOutput('criteriaBody', 'criterion','inputvalue_term');">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/plusimage/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                    <img width="20" height="20" border="0" onClick="removeRow(document.getElementById('criteria'));">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/minusimage/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </td>
                                <td valign="top" >
                                    <table border="0">
                                    <tr>
                                        <td >
                                            <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/selectall/option[@lang=$lang]"/>
                                            <input id="showAllTermscbx" name="showAll" type="checkbox" value="all" >
                                                <xsl:attribute name="onclick">
                                                     <xsl:text>if(checked) {showallpressed('terms');} else{showallreleased('terms');}</xsl:text>
                                                </xsl:attribute>
                                            </input>
                                        </td>
                                        </tr>
                                        <tr>
                                            <td align="left" id="criteria_and_or">
                                               <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/title/option[@lang=$lang]"/>
                                               <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/and/option[@lang=$lang]"/><input name="operator_term" type="radio" value="and" checked="checked"/>
                                               <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/criteriaarea/combineoperators/or/option[@lang=$lang]"/><input name="operator_term" type="radio" value="or"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                       
                            <tr>
                                <td colspan="2">
                                    <br/><br/>
                                     <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/title/option[@lang=$lang]"/><input type="checkbox" name="output_term1" value="name" checked="checked" class="hiddenInput" disabled="disabled"/>
                                </td>
                                <td align="right" valign="bottom">
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/selectall/option[@lang=$lang]"/>
                                    <input type="checkbox" id="alloutputs" value="selectAllOutputs">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)selectAllOutputs('customOutputs');document.getElementById('nooutputs').checked=false;</xsl:text>
                                       </xsl:attribute>
                                    </input>
                                    &#160;&#160;
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/selectnone/option[@lang=$lang]"/>
                                    <input type="checkbox" id="nooutputs" value="deSelectAllOutputs">
                                        <xsl:attribute name="onclick">
                                            <xsl:text>if(this.checked)deselectAllOutputs('customOutputs');document.getElementById('alloutputs').checked=false;</xsl:text>
                                       </xsl:attribute>
                                    </input>&#160;
                                </td>
                            </tr>
                        </table>
                            <hr/>
                            <table border="0" id="customOutputs">
                            <tr><!--ROW 1-->
                                <td width="140">
                                    <input type="checkbox" checked="checked" name="output_term1" value="translations" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/translations/option[@lang=$lang]"/>
                                </td>
                                <td width="140">
                                    <input type="checkbox" name="output_term2" value="uf" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/uf/option[@lang=$lang]"/>
                                </td>
                                <td width="140">
                                    <input type="checkbox" name="output_term3" value="facet" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/facet/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <input type="checkbox" name="output_term4" value="created_on" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/creationdate/option[@lang=$lang]"/>
                                    </xsl:if>
                                </td>                                
                            </tr>
                            <tr><!--ROW 2-->
                                <td>
                                    <input type="checkbox" checked="checked" name="output_term1" value="bt" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/bt/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term2" value="uf_translations" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/uf_translations/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term3" value="primary_found_in" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/primarysource/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <input type="checkbox" name="output_term4" value="modified_on" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/modificationdate/option[@lang=$lang]"/>
                                    </xsl:if>
                                </td>                                
                            </tr>
                            <tr><!--ROW 3-->
                                <td>
                                    <input type="checkbox" checked="checked" name="output_term1" value="nt" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/nt/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term2" value="tc" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/tc/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term3" value="translations_found_in" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/trsource/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <input type="checkbox" name="output_term4" value="status" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/status/option[@lang=$lang]"/>
                                    </xsl:if>
                                </td>                                
                            </tr>
                                                                
                            <tr><!--ROW 4-->
                                <td>
                                    <input type="checkbox" checked="checked" name="output_term1" value="topterm" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/topterm/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term2" value="scope_note" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/sn/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <input type="checkbox" name="output_term3" value="created_by" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/creator/option[@lang=$lang]"/>
                                    </xsl:if>
                                </td>
                                <td></td>                                
                            </tr>
                            <tr><!--ROW 5-->
                                <td>
                                    <input type="checkbox" name="output_term1" value="rt" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/rt/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <input type="checkbox" name="output_term2" value="translations_scope_note" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                    <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/sn_tr/option[@lang=$lang]"/>
                                </td>
                                <td>
                                    <xsl:if test="$UserGroup!='READER' ">
                                        <input type="checkbox" name="output_term3" value="modified_by" onclick="if(this.checked)document.getElementById('nooutputs').checked=false; else document.getElementById('alloutputs').checked=false;"/>
                                        <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/outputarea/modificator/option[@lang=$lang]"/>
                                    </xsl:if>
                                </td>
                                <td></td>                                
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr >
                    <td align="right">
                        <input name="submit4search" class="button" type="button">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$criteriatermslocale/simplesearchfieldset/searchbutton/option[@lang=$lang]"/>
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
                            <xsl:value-of disable-output-escaping="yes" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option[@lang=$lang]"/>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td style="color:#898a5e; font-size:9px;">
                            <xsl:value-of disable-output-escaping="yes" select="$criteriatermslocale/simplesearchfieldset/instructionsnote/option[@lang=$lang]"/>
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
                <xsl:value-of select="$criteriatermslocale/quicksearchfieldset/legend/option[@lang=$lang]"/>
            </legend>
            <table height="100%" border="0" valign="top">
                <tr>
                    <td/>
                </tr>
                <tr>
                    <td>
                        <xsl:value-of select="$criteriatermslocale/quicksearchfieldset/inputprompt/option[@lang=$lang]"/>
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
                                    <xsl:value-of select="$criteriatermslocale/quicksearchfieldset/quicksearchimage/src/option[@lang=$lang]"/>                                    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$criteriatermslocale/quicksearchfieldset/quicksearchimage/title/option[@lang=$lang]"/>
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
