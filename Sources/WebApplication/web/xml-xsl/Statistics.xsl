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
    <xsl:variable name="THEMASUserInfo_selectedThesaurus" select="//THEMASUserInfo/selectedThesaurus"/>
    
	<!-- _____________________________________________________________________________
			TEMPLATE: DBadmin_content
			FUNCTION: displays the contents of the DBadmin operations
	      _____________________________________________________________________________ -->
    <xsl:template name="Statistics_content">
		<!-- general VARIABLES -->
        <xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
        <xsl:variable name="lang" select="//page/@language"/>
        <xsl:variable name="CurrentStatisticsDIV" select="//page/content_Statistics/CurrentShownDIV"/>
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        		<!-- _________________ TABs menu _________________ -->
            <div id="displaytabmenu">
                <table border="0" cellpadding="0" cellspacing="0" style="width:100%;">
                    <tr valign="left">
						<!-- _________________ TAB: Στατιστικά Όρων _________________ -->
                        <td id="StatisticsOfTerms_ΤΑΒ" style="width:18%;">
                            <a onFocus="if(this.blur)this.blur()" id="StatisticsOfTerms_LINK">
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>openStatisticsDIV("StatisticsOfTerms_DIV");</xsl:text>
                                </xsl:attribute>
                                <xsl:choose>
                                    <xsl:when test="$CurrentStatisticsDIV='StatisticsOfTerms_DIV'">
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
                                <xsl:value-of select="$locale/primarycontentarea/statistics/tabs/tab1/option[@lang=$lang]"/>
                            </a>
                        </td>
						<!-- _________________ TAB: Στατιστικά Ιεραρχιών _________________ -->
                        <td id="StatisticsOfTerms_ΤΑΒ" style="width:18%;">
                            <a onFocus="if(this.blur)this.blur()" id="StatisticsOfHierarchies_LINK">
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>openStatisticsDIV("StatisticsOfHierarchies_DIV");</xsl:text>
                                </xsl:attribute>
                                <xsl:choose>
                                    <xsl:when test="$CurrentStatisticsDIV='StatisticsOfHierarchies_DIV'">
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
                                <xsl:value-of select="$locale/primarycontentarea/statistics/tabs/tab2/option[@lang=$lang]"/>
                            </a>
                        </td>
						<!-- _________________ TAB: Στατιστικά Μικροθησαυρών _________________ -->
                        <td id="StatisticsOfFacets_ΤΑΒ" style="width:18%;">
                            <a onFocus="if(this.blur)this.blur()" id="StatisticsOfFacets_LINK">
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>openStatisticsDIV("StatisticsOfFacets_DIV");</xsl:text>
                                </xsl:attribute>
                                <xsl:choose>
                                    <xsl:when test="$CurrentStatisticsDIV='StatisticsOfFacets_DIV'">
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
                                <xsl:value-of select="$locale/primarycontentarea/statistics/tabs/tab3/option[@lang=$lang]"/>
                            </a>
                        </td>
						<!-- _________________ TAB: Στατιστικά Πηγών _________________ -->
                        <td id="StatisticsOfSources_ΤΑΒ" style="width:18%;">
                            <a onFocus="if(this.blur)this.blur()" id="StatisticsOfSources_LINK">
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>openStatisticsDIV("StatisticsOfSources_DIV");</xsl:text>                                    
                                </xsl:attribute>
                                <xsl:choose>
                                    <xsl:when test="$CurrentStatisticsDIV='StatisticsOfSources_DIV'">
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
                                <xsl:value-of select="$locale/primarycontentarea/statistics/tabs/tab4/option[@lang=$lang]"/>
                            </a>
                        </td>
						<!-- _________________ TAB: Στατιστικά Χρηστών _________________ -->
                        <td id="StatisticsOfUsers_ΤΑΒ" style="width:18%;">
                            <a onFocus="if(this.blur)this.blur()" id="StatisticsOfUsers_LINK">
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>openStatisticsDIV("StatisticsOfUsers_DIV");</xsl:text>
                                </xsl:attribute>
                                <xsl:choose>
                                    <xsl:when test="$CurrentStatisticsDIV='StatisticsOfUsers_DIV'">
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
                                <xsl:value-of select="$locale/primarycontentarea/statistics/tabs/tab5/option[@lang=$lang]"/>
                            </a>
                        </td>
                        
                        <td style="width:10%; "><a class="inactive" >&#160;</a>  </td>
                        <!--<td style="width:20%; "><a class="inactive" >&#160;&#160;&#160;</a>  </td>-->
                    </tr>
                </table>
            </div>
        <div id="content" style="text-align:left">
            <div id="DisplayCardArea"/>
			<!-- _________________ DIV: Στατιστικά Όρων _________________ -->
            <div id="StatisticsOfTerms_DIV" class="tab-body">
                <xsl:call-template name="StatisticsOfTerms">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/statistics/termstats"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
			<!-- _________________ DIV: Στατιστικά Ιεραρχιών _________________ -->
            <div id="StatisticsOfHierarchies_DIV" class="tab-body">
                <xsl:call-template name="StatisticsOfHierarchies">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/statistics/hierstats"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
			<!-- _________________ DIV: Στατιστικά Μικροθησαυρών _________________ -->
            <div id="StatisticsOfFacets_DIV" class="tab-body">
                <xsl:call-template name="StatisticsOfFacets">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/statistics/facetstats"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
			<!-- _________________ DIV: Στατιστικά Πηγών _________________ -->
            <div id="StatisticsOfSources_DIV" class="tab-body">
                <xsl:call-template name="StatisticsOfSources">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/statistics/sourcestats"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
			<!-- _________________ DIV: Στατιστικά Χρηστών _________________ -->
            <div id="StatisticsOfUsers_DIV" class="tab-body">
                <xsl:call-template name="StatisticsOfUsers">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/statistics/userstats"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
	
        </div>
		<!-- _________________ display the current DIV_________________ -->
        <script language="javascript">
            <xsl:text>StatisticsDisplayDIV('</xsl:text>
            <xsl:value-of select="$CurrentStatisticsDIV"/>
            <xsl:text>');</xsl:text>
        </script>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: StatisticsOfTerms
			FUNCTION: displays the statistics for terms
	      _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfTerms">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset>
            <legend>
                <xsl:value-of select="$specificlocale/title/option[@lang=$lang]"/>
                <xsl:value-of select="$THEMASUserInfo_selectedThesaurus"/>
            </legend>
            <br/>
            <xsl:value-of select="$specificlocale/saveall/prompt/option[@lang=$lang]"/>
            
            <!-- SAVE ALL -->
            &#160;&#160;&#160;&#160;
            <a href="#">
                <xsl:attribute name="onClick">
                    <xsl:text>SaveStatisticResults('Statistics?DIV=StatisticsOfTerms_DIV&amp;Save=yes');</xsl:text>
                </xsl:attribute>
                <img height="16" width="16" border="0" >
                    <xsl:attribute name="src">
                        <xsl:value-of select="$specificlocale/saveall/image/src/option[@lang=$lang]"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$specificlocale/saveall/image/title/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
            <br/>
            <br/>
            <fieldset>
                <legend>
                    <xsl:value-of select="$specificlocale/description/option[@lang=$lang]"/>
                </legend>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                        <xsl:text>background-color: </xsl:text>
                        <xsl:value-of select="$alternateRowsColor1"/> 
                        <xsl:text>;</xsl:text>
                        <!--<xsl:text>; text-align:center;</xsl:text>-->
                        <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                    </xsl:attribute>                 
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/descriptioncolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/cardinalitycolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        
                    </tr>
                    
                    <tr class="resultRow" valign="top">
                        <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor2"/> 
                            <xsl:text>;</xsl:text>                            
                        </xsl:attribute>                        
                        <xsl:attribute name="onMouseOut">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$alternateRowsColor2"/> 
                            <xsl:text>'</xsl:text>                            
                        </xsl:attribute>                        
                        <td class="resultRow">
                            <xsl:value-of select="$specificlocale/totaltermsnumber/option[@lang=$lang]"/>
                        </td>
                        <td class="resultRow">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>updateCriteria("SearchCriteria_Terms","*");</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="//page/content_Statistics/StatisticsOfTerms/total"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="resultRow" valign="top">
                        <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>                            
                        </xsl:attribute>                        
                        <xsl:attribute name="onMouseOut">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>'</xsl:text>                            
                        </xsl:attribute>          
                        <td class="resultRow">
                            <xsl:value-of select="$specificlocale/totaluftermsnumber/option[@lang=$lang]"/>
                        </td>
                        <td class="resultRow">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>viewStatisticsResults_TermsWithXA();</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="//page/content_Statistics/StatisticsOfTerms/total_XA"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="resultRow" valign="top">
                        <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor2"/> 
                            <xsl:text>;</xsl:text>                            
                        </xsl:attribute>                        
                        <xsl:attribute name="onMouseOut">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$alternateRowsColor2"/> 
                            <xsl:text>'</xsl:text>                            
                        </xsl:attribute>  
                        <td class="resultRow">
                            <xsl:value-of select="$specificlocale/totaltranslationsnumber/option[@lang=$lang]"/>
                        </td>
                        <td class="resultRow">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>viewStatisticsResults_TermsWithTranslations();</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="//page/content_Statistics/StatisticsOfTerms/total_PrefferedTranslations"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="resultRow" valign="top">
                        <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>                            
                        </xsl:attribute>                        
                        <xsl:attribute name="onMouseOut">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>'</xsl:text>                            
                        </xsl:attribute>  
                        <td class="resultRow">
                            <xsl:value-of select="$specificlocale/totalenuftermsnumber/option[@lang=$lang]"/>
                        </td>
                        <td class="resultRow">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:text>#</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="onclick">
                                    <xsl:text>viewStatisticsResults_TermsWithUF();</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="//page/content_Statistics/StatisticsOfTerms/total_NonPrefferedTranslations"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: StatisticsOfHierarchies
			FUNCTION: displays the statistics for hierarchies
	      _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfHierarchies">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset>
            <legend>
                <xsl:value-of select="$specificlocale/title/option[@lang=$lang]"/>
                <xsl:value-of select="$THEMASUserInfo_selectedThesaurus"/>
            </legend>
            <br/>
            <xsl:value-of select="$specificlocale/saveall/prompt/option[@lang=$lang]"/>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">updateCriteria("SearchCriteria_Hierarchies","*");
                </xsl:attribute>
                <xsl:value-of select="//page/content_Statistics/StatisticsOfHierarchies/total"/>
            </a>
            <!-- SAVE ALL -->
            &#160;&#160;&#160;&#160;
            <a href="#">
                <xsl:attribute name="onClick">SaveStatisticResults('Statistics?DIV=StatisticsOfHierarchies_DIV&amp;Save=yes');
                </xsl:attribute>
                <img height="16" width="16" border="0">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$specificlocale/saveall/image/src/option[@lang=$lang]"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$specificlocale/saveall/image/title/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
            <br/>
            <br/>
			<!-- Αριθμός όρων και ΑΟ ανά ιεραρχία -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$specificlocale/description/option[@lang=$lang]"/>
                </legend>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>
                            <!--<xsl:text>; text-align:center;</xsl:text>-->
                            <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                        </xsl:attribute>                 
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/hierarchycolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/termcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/uftermcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/translationscolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/uftranslationstermcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//page/content_Statistics/StatisticsOfHierarchies/NumberOfTermsAndTranslationsPerHierarchy/hierarchy">
                        <tr class="resultRow" valign="top">
                            <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:choose>
                            <xsl:when test="position() mod 2 =0">
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>;</xsl:text>
                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                </xsl:attribute>                        
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>'</xsl:text>
                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                </xsl:attribute>                        
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>;</xsl:text>
                                </xsl:attribute>     
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>'</xsl:text>                                    
                                </xsl:attribute>                                           
                            </xsl:otherwise>
                        </xsl:choose>
                            <td class="resultRow">
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="./name"/>
                                        <xsl:text>','CardOf_Hierarchy')</xsl:text>
                                    </xsl:attribute>
                                    <xsl:value-of select="./name"/>
                                </a>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTerms != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfHierarchy("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTerms"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTerms"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfNonPrefferedTerms != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_NonPrefferedTermsOfHierarchy("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTranslations != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TranslationsOfHierarchy("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTranslations"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTranslations"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfNonPrefferedTranslations != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_NonPrefferedTranslationsOfHierarchy("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: StatisticsOfFacets
			FUNCTION: displays the statistics for facets
	      _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfFacets">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset>
            <legend>
                <xsl:value-of select="$specificlocale/title/option[@lang=$lang]"/>
                <xsl:value-of select="$THEMASUserInfo_selectedThesaurus"/>
            </legend>
            <br/>
			<!-- Συνολικός αριθμός μικροθησαυρών -->
            <xsl:value-of select="$specificlocale/saveall/prompt/option[@lang=$lang]"/>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">updateCriteria("SearchCriteria_Facets","*");
                </xsl:attribute>
                <xsl:value-of select="//page/content_Statistics/StatisticsOfFacets/total"/>
            </a>
            <!-- SAVE ALL -->
            &#160;&#160;&#160;&#160;
            <a href="#">
                <xsl:attribute name="onClick">SaveStatisticResults('Statistics?DIV=StatisticsOfFacets_DIV&amp;Save=yes');
                </xsl:attribute>
                <img height="16" width="16" border="0" >
                    <xsl:attribute name="src">
                        <xsl:value-of select="$specificlocale/saveall/image/src/option[@lang=$lang]"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$specificlocale/saveall/image/title/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
            <br/>
            <br/>
            <!-- Αριθμός ιεραρχιών, όρων και ΑΟ ανά μικροθησαυρό -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$specificlocale/description/option[@lang=$lang]"/>
                </legend>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>
                            <!--<xsl:text>; text-align:center;</xsl:text>-->
                            <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                        </xsl:attribute>                 
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/facetcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/hierarchycolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/termcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/uftermcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/translationscolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/uftranslationstermcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//page/content_Statistics/StatisticsOfFacets/NumberOfHierarchiesAndTermsAndTranslationsPerFacet/facet">
                        <tr class="resultRow" valign="top">
                            <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:choose>
                            <xsl:when test="position() mod 2 =0">
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>;</xsl:text>
                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                </xsl:attribute>                        
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>'</xsl:text>
                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                </xsl:attribute>                        
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>;</xsl:text>
                                </xsl:attribute>     
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>'</xsl:text>                                    
                                </xsl:attribute>                                           
                            </xsl:otherwise>
                        </xsl:choose>
                            <td class="resultRow">
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="./name"/>
                                        <xsl:text>','CardOf_Facet')</xsl:text>
                                    </xsl:attribute>
                                    <xsl:value-of select="./name"/>
                                </a>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfHierarchies != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_HierarchiesOfFacet("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfHierarchies"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfHierarchies"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTerms != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfFacet("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTerms"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTerms"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfNonPrefferedTerms != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_NonPrefferedTermsOfFacet("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTranslations != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TranslationsOfFacet("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTranslations"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTranslations"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfNonPrefferedTranslations != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_NonPrefferedTranslationsOfFacet("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: StatisticsOfSources
			FUNCTION: displays the statistics for sources
	      _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfSources">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset>
            <legend>
                <xsl:value-of select="$specificlocale/title/option[@lang=$lang]"/>
                <xsl:value-of select="$THEMASUserInfo_selectedThesaurus"/>
            </legend>
            <br/>
            <xsl:value-of select="$specificlocale/saveall/prompt/option[@lang=$lang]"/>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>updateCriteria("SearchCriteria_Sources","*");</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="//page/content_Statistics/StatisticsOfSources/total"/>
            </a>
            <!-- SAVE ALL -->
            &#160;&#160;&#160;&#160;
            <a href="#">
                <xsl:attribute name="onClick">
                    <xsl:text>SaveStatisticResults('Statistics?DIV=StatisticsOfSources_DIV&amp;Save=yes');</xsl:text>
                </xsl:attribute>
                <img height="16" width="16" border="0">
                    <xsl:attribute name="src">
                        <xsl:value-of select="$specificlocale/saveall/image/src/option[@lang=$lang]"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$specificlocale/saveall/image/title/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
            <br/>
            <br/>
			<!-- Αριθμός όρων ανά πηγή (ελληνική/αγγλική) -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$specificlocale/description/option[@lang=$lang]"/>
                </legend>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>
                            <!--<xsl:text>; text-align:center;</xsl:text>-->
                            <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                        </xsl:attribute>                 
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/sourcecolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/primarysourcecolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/translationssourcecolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//page/content_Statistics/StatisticsOfSources/NumberOfTermsPerTranslationsSource/source">
                        <xsl:variable name="currentJS0">
                            <xsl:call-template name="replace-string">
                                <xsl:with-param name="text" select="./name"/>
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

                        <tr class="resultRow" valign="top">
                            <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:choose>
                            <xsl:when test="position() mod 2 =0">
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>;</xsl:text>
                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                </xsl:attribute>                        
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>'</xsl:text>
                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                </xsl:attribute>                        
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>;</xsl:text>
                                </xsl:attribute>     
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>'</xsl:text>                                    
                                </xsl:attribute>                                           
                            </xsl:otherwise>
                        </xsl:choose>
                            <td class="resultRow">
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>popUpCard('</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>','CardOf_Source')</xsl:text>
                                    </xsl:attribute>
                                    <xsl:value-of select="./name"/>
                                </a>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTermsPerTermSource != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfPrimarySource("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTermsPerTermSource"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTermsPerTermSource"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTermsPerTranslationSource != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfTranslationsSource("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTermsPerTranslationSource"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTermsPerTranslationSource"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
	<!-- _____________________________________________________________________________
			TEMPLATE: StatisticsOfUsers
			FUNCTION: displays the statistics for users
	      _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfUsers">
        <xsl:param name="specificlocale" />
        <xsl:param name="lang" />
        <fieldset>
            <legend>
                <xsl:value-of select="$specificlocale/title/option[@lang=$lang]"/>
                <xsl:value-of select="$THEMASUserInfo_selectedThesaurus"/>
            </legend>
            <br/>
            <xsl:value-of select="$specificlocale/saveall/prompt/option[@lang=$lang]"/>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>viewStatisticsResults_AllTermsWithOutputsCreatorAndModificator();</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="//page/content_Statistics/StatisticsOfUsers/total"/>
            </a>
            <!-- SAVE ALL -->
            &#160;&#160;&#160;&#160;
            <a href="#">
                <xsl:attribute name="onClick">SaveStatisticResults('Statistics?DIV=StatisticsOfUsers_DIV&amp;Save=yes');
                </xsl:attribute>
                <img height="16" width="16" border="0" >
                    <xsl:attribute name="src">
                        <xsl:value-of select="$specificlocale/saveall/image/src/option[@lang=$lang]"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$specificlocale/saveall/image/title/option[@lang=$lang]"/>
                    </xsl:attribute>
                </img>
            </a>
            <br/>
            <br/>
			<!-- Αριθμός όρων δημιουργία και τελευταίας τροποποίησης ανά χρήστη -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$specificlocale/description/option[@lang=$lang]"/>
                </legend>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                            <xsl:text>background-color: </xsl:text>
                            <xsl:value-of select="$alternateRowsColor1"/> 
                            <xsl:text>;</xsl:text>
                            <!--<xsl:text>; text-align:center;</xsl:text>-->
                            <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                        </xsl:attribute>                 
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/usercolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/creationcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$specificlocale/modificationcolumn/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//page/content_Statistics/StatisticsOfUsers/NumberOfTermsCreatedAndLastModifiedPerUser/user">
                        <tr class="resultRow" valign="top">
                            <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
                        <xsl:choose>
                            <xsl:when test="position() mod 2 =0">
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>;</xsl:text>
                                    <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                                </xsl:attribute>                        
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor1"/> 
                                    <xsl:text>'</xsl:text>
                                    <!--<xsl:text>this.style.background = '#E2E2E2'</xsl:text>-->
                                </xsl:attribute>                        
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="style">
                                    <xsl:text>background-color: </xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>;</xsl:text>
                                </xsl:attribute>     
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>'</xsl:text>                                    
                                </xsl:attribute>                                           
                            </xsl:otherwise>
                        </xsl:choose>
                            <td class="resultRow">
                                <xsl:value-of select="./name"/>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTermsCreated != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfCreator("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTermsCreated"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTermsCreated"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="./NumberOfTermsLastModified != 0">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:text>#</xsl:text>
                                            </xsl:attribute>
                                            <xsl:attribute name="onclick">
                                                <xsl:text>viewStatisticsResults_TermsOfModificator("</xsl:text>
                                                <xsl:value-of select="./name"/>
                                                <xsl:text>");</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="./NumberOfTermsLastModified"/>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./NumberOfTermsLastModified"/>
                                    </xsl:otherwise>
                                </xsl:choose>
								<!-- <xsl:value-of select="./NumberOfTermsLastModified"/> -->
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
</xsl:stylesheet>
