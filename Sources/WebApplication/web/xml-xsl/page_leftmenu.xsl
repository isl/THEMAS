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
	<!-- _________________________________________________________________________________________________
		template: page_leftmenu
	  _________________________________________________________________________________________________
		FUNCTION: template that builds the left menu of each page, with options: Term, Hierarchy, Facet, Bibliography etc.
		INPUT: - option: the XPath node: <leftmenu>/<option> which for example has the following structure (one for each of the above options):
					<option>
						<name>Term</name>
						<new alt="New">images/newdoc.gif</new>
						<search alt="Search">images/searchdoc.gif</search>
						<viewAll alt="Show All">images/showalldocs.gif</viewAll>
					</option>
      _________________________________________________________________________________________________ -->
    <xsl:template name="page_leftmenu">
        <xsl:variable name="localeThes" select="document('../translations/labels.xml')/locale/messages"/>
        <xsl:variable name="nameLang" select="concat('name_',$lang)"/>
        <xsl:variable name="legendDescrLang" select="concat('legend_descr_',$lang)"/>
        <xsl:variable name="leftmenumode" select="//page/leftmenu/activemode "/>
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        <xsl:variable name="THEMASUserInfo_selectedThesaurus" select="//THEMASUserInfo/selectedThesaurus"/>
        
        <table cellspacing="0" cellpadding="2" align="right" class="leftMenuTable">
            
            <tr id="termsleftmenu"><!--TERMS-->
                <xsl:if test="$leftmenumode = 'LMENU_TERMS'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/terms/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <a href="#">
                                                <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                    <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/terms/actions/new/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/terms/actions/new/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                    </xsl:when>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'EXTERNALREADER'">
                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('','term_create','EditDisplays_Term');</xsl:text>
                                            </xsl:attribute>
                                            
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/terms/actions/new/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/terms/actions/new/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <a href="Links?tab=SearchCriteria">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/terms/actions/search/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/terms/actions/search/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                            <td>
                                <a href="#" onClick="updateCriteria('SearchCriteria_Terms','*');">                                        
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/terms/actions/viewall/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/terms/actions/viewall/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                 </a>
                            </td>
                        </tr>
                    </table>
                </td>   
            </tr>   
            
            <tr id="hierarchiesleftmenu"><!--HIERARCHIES-->
                <xsl:if test="$leftmenumode = 'LMENU_HIERARCHIES'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/hierarchies/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <a href="#">
                                                <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                     <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/hierarchies/actions/new/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/hierarchies/actions/new/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                    </xsl:when>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'EXTERNALREADER'">
                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="#">
                                            <xsl:choose>
                                                <xsl:when test="$THEMASUserInfo_userGroup = 'LIBRARY'">
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('','hierarchy_create','EditDisplays_Hierarchy');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/hierarchies/actions/new/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/hierarchies/actions/new/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <a href="Links?tab=HierarchiesSearchCriteria">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/hierarchies/actions/search/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/hierarchies/actions/search/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                            <td>
                                <a href="#" onClick="updateCriteria('SearchCriteria_Hierarchies','*');">                                        
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/hierarchies/actions/viewall/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/hierarchies/actions/viewall/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                 </a>
                            </td>
                        </tr>
                    </table>
                </td> 
            </tr>
            
            <tr id="facetsleftmenu"><!--FACETS-->
                <xsl:if test="$leftmenumode = 'LMENU_FACETS'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/facets/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <a href="#">
                                                <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                     <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/facets/actions/new/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/facets/actions/new/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                    </xsl:when>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'EXTERNALREADER'">
                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="#">
                                            <xsl:choose>
                                                <xsl:when test="$THEMASUserInfo_userGroup = 'LIBRARY'">
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('','facet_create','EditDisplays_Facet');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/facets/actions/new/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/facets/actions/new/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <a href="Links?tab=FacetsSearchCriteria">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/facets/actions/search/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/facets/actions/search/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                            <td>
                                <a href="#" onClick="updateCriteria('SearchCriteria_Facets','*');">                                        
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/facets/actions/viewall/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/facets/actions/viewall/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                 </a>
                            </td>
                        </tr>
                    </table>
                </td> 
            </tr> 
            
            <tr><!--SEPERATOR-->
                <td colspan="2" style="padding-left:7px;">
                    <hr/>
                </td>
            </tr> 
            
            <tr id="sourcesleftmenu"><!--SOURCES-->
                <xsl:if test="$leftmenumode = 'LMENU_SOURCES'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/sources/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <a href="#">
                                                <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                     <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/sources/actions/new/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/sources/actions/new/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                    </xsl:when>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'EXTERNALREADER'">
                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="#">
                                            <xsl:choose>
                                                <xsl:when test="$THEMASUserInfo_userGroup = 'LIBRARY'">
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('','source_create','EditDisplays_Source');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/sources/actions/new/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/sources/actions/new/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <a href="Links?tab=SourcesSearchCriteria">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/sources/actions/search/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/sources/actions/search/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                            <td>
                                <a href="#" onClick="updateCriteria('SearchCriteria_Sources','*');">                                        
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/sources/actions/viewall/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/sources/actions/viewall/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                 </a>
                            </td>
                        </tr>
                    </table>
                </td> 
            </tr>   
            
            <tr id="statisticsleftmenu"><!--STATISTICS-->
                <xsl:if test="$leftmenumode = 'LMENU_STATISTICS'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="$THEMASUserInfo_userGroup = 'READER'">
                    <xsl:attribute name="disabled">
                        <xsl:text>disabled</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="class">
                        <xsl:text>disabled</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/statistics/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER' ">
                                        <a href="#" >
                                            <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                     <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/statistics/actions/showstatistics/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/statistics/actions/showstatistics/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>openStatisticsDIV("StatisticsOfTerms_DIV");</xsl:text>
                                            </xsl:attribute>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/statistics/actions/showstatistics/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/statistics/actions/showstatistics/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </table>
                </td> 
               
            </tr> 
            
            
            <tr><!--SEPERATOR-->
                <td colspan="2" style="padding-left:7px;">
                    <hr/>
                </td>
            </tr> 
            
            <tr id="thesaurileftmenu"><!--THESAURI MANAGEMENT-->
                <xsl:if test="$leftmenumode = 'LMENU_THESAURI'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/thesauri/label/option[@lang=$lang]"/>
                    </b>
                </td>
                
                <td align="right">
                    <table>
                        <tr>
                            <xsl:if test="$THEMASUserInfo_userGroup != 'EXTERNALREADER'">
                                <td align="right">
                                    <a href="#">
                                        <xsl:attribute name="onClick">
                                            <xsl:text>showEditFieldCard('','change_thesaurus','EditDisplays_User');</xsl:text>
                                        </xsl:attribute>
                                        <img width="16" height="16" border="0">
                                            <xsl:attribute name="src">
                                                <xsl:value-of select="$locale/leftmenu/thesauri/actions/changethesaurus/image/src/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:value-of select="$locale/leftmenu/thesauri/actions/changethesaurus/image/title/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </img>
                                    </a>
                                </td>

                                <xsl:choose>
                                    <!-- only admin and thesaurus commitee have access to these operations-->
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER' or $THEMASUserInfo_userGroup = 'LIBRARY' or $THEMASUserInfo_userGroup = 'THESAURUS_TEAM'">
                                        <td>
                                            <a href="#" >
                                                <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/thesauri/actions/thesaurimanagement/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/thesauri/actions/thesaurimanagement/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td>
                                            <a href="Admin_Thesaurus?DIV=ImportExport_Data_DIV">
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/thesauri/actions/thesaurimanagement/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/thesauri/actions/thesaurimanagement/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                            <!-- Thesaurus global View -->
                            <td>
                                <a href="#">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$hierarchieslocale/tableresults/columns/actions/hierarchical/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$hierarchieslocale/tableresults/columns/actions/hierarchical/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="onClick">
                                            <!--<xsl:text>prepareResults('hierarchysTermsShortcuts','</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','GlobalThesarusHierarchical','true');</xsl:text>-->
                                                <xsl:text>prepareResults('hierarchysTermsShortcuts','GlobalThesarusHierarchical','GlobalThesarusHierarchical','true');</xsl:text>
                                            
                                        </xsl:attribute>
                                    </img>
                                </a>                                
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>   
            
            <xsl:if test="$THEMASUserInfo_userGroup != 'EXTERNALREADER'">
                <tr id="dbleftmenu"><!--DATABASE MANAGEMENT-->
                    <xsl:if test="$leftmenumode = 'LMENU_DATABASE'">
                        <xsl:attribute name="style">
                            <xsl:text> background-color: #FFFFFF; </xsl:text>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$THEMASUserInfo_userGroup = 'READER' or $THEMASUserInfo_userGroup = 'LIBRARY'">
                        <xsl:attribute name="disabled">
                            <xsl:text>disabled</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="class">
                            <xsl:text>disabled</xsl:text>
                        </xsl:attribute>                        
                    </xsl:if>
                    <td style="padding-left:7px;">
                        <b>
                            <xsl:value-of select="$locale/leftmenu/db/label/option[@lang=$lang]"/>
                        </b>
                    </td>
                    <td align="right">
                        <table>
                            <tr>
                                <td>
                                    <xsl:choose>
                                        <xsl:when test="$THEMASUserInfo_userGroup = 'READER' or $THEMASUserInfo_userGroup = 'LIBRARY' ">
                                            <a href="#" >
                                                <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/db/actions/managedb/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/db/actions/managedb/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <a>
                                                <!-- Η Ομάδα Θησαυρού (THESAURUS_TEAM) από τις διαχειριστικές λειτουργίες μπορεί να κάνει μόνο Επιδιόρθωση Βάσης -->
                                                <xsl:choose>
                                                    <xsl:when test="$THEMASUserInfo_userGroup != 'THESAURUS_TEAM'">
                                                        <xsl:attribute name="href">
                                                            <xsl:text>DBadmin?DIV=Create_Restore_DB_backup_DIV</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:attribute name="href">
                                                            <xsl:text>DBadmin?DIV=Fix_DB_DIV</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/db/actions/managedb/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/db/actions/managedb/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr> 
            </xsl:if>
            
            <xsl:if test="$THEMASUserInfo_userGroup != 'EXTERNALREADER'">
            <tr id="usersleftmenu"><!--USERS-->
                <xsl:if test="$leftmenumode = 'LMENU_USERS'">
                    <xsl:attribute name="style">
                        <xsl:text> background-color: #FFFFFF; </xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="$THEMASUserInfo_userGroup != 'THESAURUS_COMMITTEE' and $THEMASUserInfo_userGroup != 'ADMINISTRATOR'">
                    <xsl:attribute name="disabled">
                        <xsl:text>disabled</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="class">
                        <xsl:text>disabled</xsl:text>
                    </xsl:attribute>  
                </xsl:if>
                
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/users/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right" >
                    <table>
                        <tr>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <a href="#">
                                                <xsl:attribute name="onclick">
                                                    <xsl:text>alert('</xsl:text>
                                                    <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                     <xsl:text>');</xsl:text>
                                                </xsl:attribute>
                                                <img width="16" height="16" border="0">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$locale/leftmenu/users/actions/new/image/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$locale/leftmenu/users/actions/new/image/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
                                            </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="#">
                                            <xsl:choose>
                                                <xsl:when test="$THEMASUserInfo_userGroup != 'THESAURUS_COMMITTEE' and $THEMASUserInfo_userGroup != 'ADMINISTRATOR' ">
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>alert('</xsl:text>
                                                        <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                         <xsl:text>');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="onClick">
                                                        <xsl:text>showEditFieldCard('','user_create','EditDisplays_User');</xsl:text>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <img width="16" height="16" border="0">
                                                <xsl:attribute name="src">
                                                    <xsl:value-of select="$locale/leftmenu/users/actions/new/image/src/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:value-of select="$locale/leftmenu/users/actions/new/image/title/option[@lang=$lang]"/>
                                                </xsl:attribute>
                                            </img>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <a href="#" >
                                    <xsl:choose>
                                        <xsl:when test="$THEMASUserInfo_userGroup != 'THESAURUS_COMMITTEE' and $THEMASUserInfo_userGroup != 'ADMINISTRATOR' ">
                                            <xsl:attribute name="onclick">
                                                <xsl:text>alert('</xsl:text>
                                                <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                 <xsl:text>');</xsl:text>
                                            </xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>updateCriteria("SearchCriteria_Users","*");</xsl:text>
                                            </xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/users/actions/viewall/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/users/actions/viewall/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                 </a>
                            </td>
                            <td>
                                <a href="#">
                                    <xsl:choose>
                                        <xsl:when test="$THEMASUserInfo_userGroup != 'THESAURUS_COMMITTEE' and $THEMASUserInfo_userGroup != 'ADMINISTRATOR' ">
                                            <xsl:attribute name="onclick">
                                                <xsl:text>alert('</xsl:text>
                                                <xsl:value-of select="$locale/generalmessages/disabledfunction/option[@lang=$lang]"/>
                                                 <xsl:text>');</xsl:text>
                                            </xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('','share_thesaurus','EditDisplays_User');</xsl:text>
                                            </xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/users/actions/sharethesaurus/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/users/actions/sharethesaurus/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr> 
            </xsl:if>
            <tr><!--SEPERATOR-->
                <td colspan="2" style="padding-left:7px;">
                    <hr/>
                </td>
            </tr> 
            
            <tr><!--HELP-->
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/help/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <a target="_blank">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="$locale/leftmenu/help/href/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/help/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/help/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>   
            
            <tr id="legendDiv2" name="legendDiv2"><!--YPOMNIMA - LEGEND-->
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/ypomnima/image/title/option[@lang=$lang]"/>
                        <!--<xsl:value-of select="$localeThes/message[@id='showLegend']/@*[name() = $nameLang]"/>-->
                    </b>
                </td>
                <td align="right" >
                    <table>
                        <tr>
                            <td>
                                <a href="#">
                                    <img width="16" height="16" border="0" onclick="javascript:toggleLegend();">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/ypomnima/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/ypomnima/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            
            <tr><!--SEPERATOR-->
                <td colspan="2" style="padding-left:7px;">
                    <hr/>
                </td>
            </tr> 
            
            <tr><!--LOGOUT-->
                <td style="padding-left:7px;">
                    <b>
                        <xsl:value-of select="$locale/leftmenu/logout/label/option[@lang=$lang]"/>
                    </b>
                </td>
                <td align="right">
                    <table>
                        <tr>
                            <td>
                                <a href="Index?logout=true">
                                    <img width="16" height="16" border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/leftmenu/logout/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/leftmenu/logout/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>  
            <!--
            <tr height="100%">
                <td valign="bottom" colspan="2" align="right">
                    
                </td>
            </tr>
            -->
            
        </table>
    
        
        <div id="bottomImg">
            <!-- image for down right side of left_menu-->
            <xsl:if test="$locale/leftmenu/imageatbottom/src/option[@lang=$lang]!=''">
                <a>
                    <xsl:if test="$locale/leftmenu/imageatbottom/href/option[@lang=$lang]!=''">
                        <xsl:attribute name="href">
                            <xsl:value-of select="$locale/leftmenu/imageatbottom/href/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="target">
                            <xsl:text>_blank</xsl:text>
                        </xsl:attribute>
                    </xsl:if>
                    <img border="0">
                        <xsl:attribute name="src">
                            <xsl:value-of select="$locale/leftmenu/imageatbottom/src/option[@lang=$lang]"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="$locale/leftmenu/imageatbottom/title/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </img>
                </a>
            </xsl:if>
        </div>
        
    </xsl:template>
</xsl:stylesheet>
