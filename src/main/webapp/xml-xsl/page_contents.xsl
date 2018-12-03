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
    <xsl:import href="page.xsl"/>
    
    <!-- general VARIABLES -->
    <xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
    <xsl:variable name="lang" select="//page/@language"/>
    <!--<xsl:variable name="bannerImagePath" select="$locale/loginpage/image/title/option[@lang=$lang]/text()"/>-->
    <xsl:variable name="primarylang" select="//page/@primarylanguage"/>
    <xsl:variable name="paginglocale" select="$locale/primarycontentarea/paging"/>
    
    <xsl:variable name="Slash">\</xsl:variable>
    <xsl:variable name="SlashJS">\\</xsl:variable>
    <xsl:variable name="Apos">'</xsl:variable>
    <xsl:variable name="AposJS">\'</xsl:variable>
	<!-- used by hierarchical.xsl -->
    <xsl:variable name="showEmpty" select="1"/>
    <xsl:variable name="adminTag" select="'admin'"/>
    <xsl:variable name="paging_infoTag" select="'paging_info'"/>
    <xsl:variable name="type" select="'AP'"/>
    <xsl:variable name="xpathBackLimit" select="0"/>
    <xsl:variable name="activeD" select="//part2/tabset2/tab/@active='yes'"/>
    <!-- XSL INCLUDES for divs -->
    <xsl:include href="criteria_terms.xsl"/>
    <xsl:include href="criteria_facets.xsl"/>
    <xsl:include href="criteria_hierarchies.xsl"/>
    <xsl:include href="criteria_sources.xsl"/>
    <xsl:include href="search_results_terms.xsl"/>
    <xsl:include href="search_results_hierarchies.xsl"/>
    <xsl:include href="search_results_facets.xsl"/>
    <xsl:include href="search_results_sources.xsl"/>
    <xsl:include href="search_results_users.xsl"/>
    <xsl:include href="search_results_terms_systematic.xsl"/>
    <xsl:include href="EditTermActions/PopUpInfo_Term.xsl"/>
    <xsl:include href="EditHierarchyActions/PopUpInfo_Hierarchy.xsl"/>
    <xsl:include href="EditFacetActions/PopUpInfo_Facet.xsl"/>
    <xsl:include href="EditSourceActions/PopUpInfo_Source.xsl"/>
    <xsl:include href="utilities.xsl"/>
    <xsl:template match="/">
        <xsl:call-template name="page"/>
    </xsl:template>
    <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
    
	<!-- _____________________________________________________________________________
			TEMPLATE: content
			FUNCTION: 
	      _____________________________________________________________________________ -->
    <xsl:template name="content">
		<!-- THEMASUserInfo_userGroup: HIDDEN (so as to be able to be taken by DrawTabs() javascript function) -->
        <input type="hidden" id="THEMASUserInfo_userGroup">
            <xsl:attribute name="value">
                <xsl:value-of select="$THEMASUserInfo_userGroup"/>
            </xsl:attribute>
        </input>
					    
		<!-- _________________ UP part TABs _________________  260 -->
        <xsl:variable name="up" select="//part1/tabset1/tab[@active='yes']/text()"/>
        <xsl:choose>
            <xsl:when test="$up='Alphabetical' ">
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div id="content" >
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="search_results_terms_alphabetical">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='Systematic' ">
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div  id="content" >
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="search_results_terms_systematic">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='Hierarchical' ">
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div id="content" >
                    <div id="DisplayCardArea"/>
                    
                    <!--<script language="javascript"> DrawTabs('display','term');</script>-->
                    <div id="centralDiv" class="tab-body">
                        Υπό κατασκευή
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='Graphical' ">
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div id="content" >
                    <div id="DisplayCardArea"/>
                    
                    <!--<script language="javascript"> DrawTabs('display','term');</script>-->
                    <div id="centralDiv" class="tab-body">
                        Υπό κατασκευή
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='SearchResults' ">
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div id="content">
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="search_results_terms">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='SearchCriteria' ">
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'term'"/>
                </xsl:call-template>
                <div id="content" >
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="criteria"/>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='Settings' ">
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'hierarchy'"/>
                </xsl:call-template>
                <div id="content" >
                    <div id="DisplayCardArea"/>
                    
                    <!--<script language="javascript"> DrawTabs('display','hierarchy');</script>-->
                    <div id="centralDiv" class="tab-body">
                        Υπό κατασκευή
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='SearchHierarchyResults'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'hierarchy'"/>
                </xsl:call-template>
                <div id="content" >
                    
                    <!--<script language="javascript"> DrawTabs('display','hierarchy');</script>-->
                    <div id="centralDiv" class="tab-body">
                        
                        <xsl:call-template name="search_results_hierarchies">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='SearchFacetResults'" >
                <div id="DisplayCardArea"/>  
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'facet'"/>
                </xsl:call-template>
                <div id="content" >                                       
                    <!--<script language="javascript"> DrawTabs('display','facet');</script>-->
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="search_results_facets">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='SearchSourceResults'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'source'"/>
                </xsl:call-template>
                <div id="content" >
                 
                    
                    <!--<script language="javascript"> DrawTabs('display','source');</script>-->
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="search_results_sources">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <!-- SearchUsersResults -->
            <xsl:when test="$up='SearchUsersResults'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'user'"/>
                </xsl:call-template>
                <div id="content" >
                    
                    
                    <!--<script language="javascript"> DrawTabs('display','user');</script>-->
                    <div id="centralDiv" class="tab-body">
						<!-- todo -->
                        <xsl:call-template name="search_results_users">
                            <xsl:with-param name="paginglocale" select="$paginglocale" />
                        </xsl:call-template>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='FacetsSearchCriteria'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'facet'"/>
                </xsl:call-template>
                <div id="content" >
                    
                    
                    <!--<script language="javascript"> DrawTabs('display','facet');</script>-->
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="criteria_facets"/>
                        
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="$up='HierarchiesSearchCriteria'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'hierarchy'"/>
                </xsl:call-template>
                <div id="content" >                                        
                    <!--<script language="javascript"> DrawTabs('display','hierarchy');</script>-->
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="criteria_hierarchies"/>
                    </div>
                </div>
            </xsl:when>
            
            <xsl:when test="$up='SourcesSearchCriteria'" >
                <div id="DisplayCardArea"/>
                <xsl:call-template name="drawtabs">
                    <xsl:with-param name="currenttabup" select="$up"/>
                    <xsl:with-param name="mode" select="'source'"/>
                </xsl:call-template>
                <div id="content" >
                    
                    
                    
                    <!--<script language="javascript"> DrawTabs('display','source');</script>-->
                    <div id="centralDiv" class="tab-body">
                        <xsl:call-template name="criteria_sources"/>
                    </div>
                </div>
            </xsl:when>
            
       
            <xsl:when test="$up='Details'" >
                <!--<div id="content" >-->
                <div id="DisplayCardArea"/>
                    <!--<script language="javascript"> DrawTabs('edit', 'term');  </script>    -->
                <div id="centralDiv" class="popUpCard editmodecard" >
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Term" >
                        <xsl:with-param name="showClose">
                            <xsl:text>false</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                </div>
                    
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="//termName"/>
                        <xsl:with-param name="replace" select="$Slash" />
                        <xsl:with-param name="with" select="$SlashJS" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos" />
                        <xsl:with-param name="with" select="$AposJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:variable name="isTopTerm">
                    <xsl:value-of select="//current/term/isTopTerm"/>
                </xsl:variable>
                    
                <xsl:call-template name="showEditActions_Term">
                    <xsl:with-param name="termName">
                        <xsl:value-of select="$currentJS"/>
                    </xsl:with-param>
                    <xsl:with-param name="disableTopTermEdit">
                        <xsl:value-of select="$isTopTerm"/>
                    </xsl:with-param>
                </xsl:call-template>
                    
                <!--</div>-->
            </xsl:when>
            <xsl:when test="$up='HierarchyDetails'" >
                <!--<div id="content" >-->
                <div id="DisplayCardArea"/>
                    
                <div id="centralDiv" class="popUpCard editmodecard" >
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Hierarchy" >
                        <xsl:with-param name="showClose">
                            <xsl:text>false</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                </div>
                    
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="//hierarchyName"/>
                        <xsl:with-param name="replace" select="$Slash" />
                        <xsl:with-param name="with" select="$SlashJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos" />
                        <xsl:with-param name="with" select="$AposJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:call-template name="showEditActions_Hierarchy">
                    <xsl:with-param name="hierarchyName">
                        <xsl:value-of select="$currentJS"/>
                    </xsl:with-param>
                </xsl:call-template>
                <!--</div>-->
            </xsl:when>
            <xsl:when test="$up='FacetDetails'" >
                <!--<div id="content" >-->
                <div id="DisplayCardArea"/>
                    
                <div id="centralDiv" class="popUpCard editmodecard" >
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Facet" >
                        <xsl:with-param name="showClose">
                            <xsl:text>false</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                </div>
                    
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="//facetName"/>
                        <xsl:with-param name="replace" select="$Slash" />
                        <xsl:with-param name="with" select="$SlashJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos" />
                        <xsl:with-param name="with" select="$AposJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:call-template name="showEditActions_Facet">
                    <xsl:with-param name="facetName">
                        <xsl:value-of select="$currentJS"/>
                    </xsl:with-param>
                </xsl:call-template>
                <!--</div>-->
            </xsl:when>
            <xsl:when test="$up='SourceDetails'" >
           <!--<div id="content" >-->
                <div id="DisplayCardArea"/> 
                                 
                <div id="centralDiv" class="popUpCard editmodecard" >
                    <xsl:call-template name="PopUp_Or_EditCard_Of_Source" >
                        <xsl:with-param name="showClose">
                            <xsl:text>false</xsl:text>
                        </xsl:with-param>
                    </xsl:call-template>
                </div>
                    
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="//source/name"/>
                        <xsl:with-param name="replace" select="$Slash" />
                        <xsl:with-param name="with" select="$SlashJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos" />
                        <xsl:with-param name="with" select="$AposJS" />
                    </xsl:call-template>
                </xsl:variable>
                    
                <xsl:call-template name="showEditActions_Source">
                    <xsl:with-param name="sourceName">
                        <xsl:value-of select="$currentJS"/>
                    </xsl:with-param>
                </xsl:call-template>
             <!--</div>-->
            </xsl:when>
	    
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="drawtabs">
        
        <xsl:param name="currenttabup"/>
        <xsl:param name="mode"/>
        <xsl:choose>
            <xsl:when test="$mode='term'">
                <xsl:variable name="tablist" select="$locale/primarycontentarea/terms/tabs"/>
                <div id="displaytabmenu">
                    <table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
                        <tr valign="left">
                            
                            <td >
                                <a href="SearchResults_Terms_Alphabetical" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='Alphabetical'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="SearchResults_Terms_Systematic" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='Systematic'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="SearchResults_Terms" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SearchResults'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab3/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="Links?tab=SearchCriteria" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SearchCriteria'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab8/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                        </tr>
                    </table>
                </div>
            </xsl:when>
            
            <xsl:when test="$mode='hierarchy'">
                <xsl:variable name="tablist" select="$locale/primarycontentarea/hierarchies/tabs"/>
                <div id="displaytabmenu">
                    <table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
                        <tr valign="left">
                            
                            <td class="tabstop">
                                <a href="SearchResults_Hierarchies" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose><!--  SearchFacetResults SourcesSearchCriteria SearchSourceResults SearchUsersResults FacetsSearchCriteria -->
                                        <xsl:when test="$currenttabup='SearchHierarchyResults'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="Links?tab=HierarchiesSearchCriteria" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='HierarchiesSearchCriteria'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab8/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                        </tr>
                    </table>
                </div>
            </xsl:when>
            <xsl:when test="$mode='facet'">
                <xsl:variable name="tablist" select="$locale/primarycontentarea/facets/tabs"/>
                <div id="displaytabmenu">
                    <table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
                        <tr valign="left">
                            
                            <td class="tabstop">
                                <a href="SearchResults_Facets" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SearchFacetResults'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="Links?tab=FacetsSearchCriteria" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='FacetsSearchCriteria'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab8/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                        </tr>
                    </table>
                </div>
            </xsl:when>
            <xsl:when test="$mode='source'">
                <xsl:variable name="tablist" select="$locale/primarycontentarea/sources/tabs"/>
                <div id="displaytabmenu">
                    <table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
                        <tr valign="left">
                            
                            <td class="tabstop">
                                <a href="SearchResults_Sources" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SearchSourceResults'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:12%;">
                                <a class="inactive" style="width:100%; display:block;">&#160;&#160;&#160;</a>
                            </td>
                            
                            <td class="tabstop">
                                <a href="Links?tab=SourcesSearchCriteria" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SourcesSearchCriteria'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab8/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                        </tr>
                    </table>
                </div>
            </xsl:when>
            <xsl:when test="$mode='user'">
                <xsl:variable name="tablist" select="$locale/primarycontentarea/users/tabs"/>
                <div id="displaytabmenu">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" style="width:100%;">
                        <tr valign="left" >
                            <td style="width:14%;">
                                <a href="SearchResults_Users" onFocus="if(this.blur)this.blur()" onClick="DisplayPleaseWaitScreen(true);">
                                    <xsl:choose>
                                        <xsl:when test="$currenttabup='SearchUsersResults'">
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
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$tablist/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </a>
                            </td>
                            <td style="width:26%; ">
                                <a class="inactive" >&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:20%; ">
                                <a class="inactive" >&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:20%; ">
                                <a class="inactive" >&#160;&#160;&#160;</a>
                            </td>
                            <td style="width:20%; ">
                                <a class="inactive" >&#160;&#160;&#160;</a>
                            </td>

                        </tr>
                    </table>
                </div>
            </xsl:when>
        </xsl:choose>
        
    </xsl:template>
    <!--
    <xsl:call-template name="drawtabs">
                        <xsl:with-param name="currenttabup" select="$up"/>
                        <xsl:with-param name="mode" select="'term'"/>
                    </xsl:call-template>-->
                    <!--<script language="javascript"> DrawTabs('display','term');-->
</xsl:stylesheet>
