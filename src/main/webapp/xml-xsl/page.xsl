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

<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
    xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"
    exclude-result-prefixes="xsl fo xs fn xdt">
    <xsl:import href="Configs.xsl"/>
    <xsl:import href="page_head_html.xsl"/>
    <xsl:import href="page_header.xsl"/>
    <xsl:import href="page_footer.xsl"/>    
    <xsl:import href="page_leftmenu.xsl"/>
    <xsl:import href="Admin_DB.xsl"/>
    <xsl:import href="Statistics.xsl"/>
    <xsl:import href="Admin_Thesaurus.xsl"/>
    <xsl:import href="HiddenActions/HiddenActions.xsl"/>
    <xsl:output method="html"  
            encoding="UTF-8"  
            indent="yes" 
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            version="4.0" />	
        <!-- _________________________________________________________________________________________________
		template: page
	  _________________________________________________________________________________________________
		FUNCTION: template that builds the structure of each page: header - leftmenu - content - footer
      _________________________________________________________________________________________________ -->
          
     
    <xsl:template name="page">
            <!--        <html xmlns="http://www.w3.org/1999/xhtml">-->
        <html>

            <!-- html_head -->
            <xsl:call-template name="page_head_html"/>
                
            
            <!-- BODY: javascript code for AJAX type-ahead mechanism -->
            <!-- <body onload="loadTypeAheadActions();" onmouseup="direction='Released'" onmousemove=" SetDivPosition(event)"> -->
            <body>
                <!-- display none but with this bugfix we avoid flickering of image at the bottom
                <table style="width:100%; margin-left:auto; margin-right:auto; display:none;" >
                    
                    <tr>
                        <td style="text-align:center;">
                      <img border="0" style="margin-top:10px; margin-left:auto; margin-right:auto;">
                          <xsl:attribute name="src">
                              <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                          </xsl:attribute>
                          <xsl:attribute name="title">
                              <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                          </xsl:attribute>
                      </img>
                      </td>
                    </tr>
                </table>
                -->
                <xsl:if test="count(//content) != 0">
                    <!-- only in case of usual servlets (and not for example for DBadmin) -->
                    <xsl:attribute name="onload">
                        <xsl:text>loadTypeAheadActions();</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="onmouseup">
                        <xsl:text>direction='Released'</xsl:text>
                    </xsl:attribute>
                    <!--<xsl:attribute name="onmousemove">SetDivPosition(event)</xsl:attribute>                                       -->
                    <!--<xsl:attribute name="onunload">checkUnloadAction();</xsl:attribute>-->
                </xsl:if>
                <div id="all">
                    <xsl:if test="$pageBgColor!=''">
                        <xsl:attribute name="style">
                            <xsl:text>background-color:</xsl:text>
                            <xsl:value-of select="$pageBgColor"/>
                            <xsl:text>;</xsl:text>
                        </xsl:attribute>
                    </xsl:if>
                    <!-- peplo screen -->
                    <xsl:call-template name="peploScreen"/>
                    <div id="page">
                        
                        <!--<script type="text/javascript" language="javascript">finishSession=true;</script>-->
                        <!-- header -->
                        <div id="header">
                            <xsl:call-template name="page_header"/>
                        </div>
                        
                        <!-- leftmenu -->
                        <xsl:if test="count(//content) != 0 or count(//content_Statistics) != 0 or count(//content_Admin_Thesaurus) != 0 or count(//content_DBadmin) != 0">
                            <!-- only in case of usual servlets (and not for example for THEMAS_HiddenActions) -->
                            <div id="content-secondary">
                                <xsl:call-template name="page_leftmenu"/>
                            </div>
                        </xsl:if>
                        
                        <!-- content -->
                        <xsl:if test="count(//content) != 0">
                            <!-- only in case of usual servlets (and not for example for DBadmin) -->
                            <div id="content-primary">
                                <xsl:call-template name="content"/>
                            </div>
                        </xsl:if>
                        <xsl:if test="count(//content_DBadmin) != 0">
                            <!-- only in case of DBadmin servlet -->
                            <div id="content-primary">
                                <xsl:call-template name="DBadmin_content"/>
                            </div>
                        </xsl:if>
                        <xsl:if test="count(//content_Statistics) != 0">
                            <!-- only in case of Statistics servlet -->
                            <div id="content-primary">
                                <xsl:call-template name="Statistics_content"/>
                            </div>
                        </xsl:if>
                        <xsl:if test="count(//content_Admin_Thesaurus) != 0">
                            <!-- only in case of DBadmin servlet -->
                            <div id="content-primary">
                                <xsl:call-template name="Admin_Thesaurus_content"/>
                            </div>
                        </xsl:if>
                        <xsl:if test="count(//content_THEMAS_HiddenActions) != 0">
                            <!-- only in case of THEMAS_HiddenActions servlet -->
                            <div id="content-primary">
                                <xsl:call-template name="THEMAS_HiddenActions_content"/>
                            </div>
                        </xsl:if>					
                        <!-- footer -->
                        <!--<div id="footer">-->
                            <xsl:call-template name="page_footer"/>
                        <!--</div>-->
                        <!-- LEGEND DIV CODE-->
                        <div id="legendDiv" name="legendDiv" class="legendThes">
                            <table cellspacing="0" cellpadding="0" border="0">
                                <tbody>
                                    <tr>
                                        <td class="closeLegendThes" onclick="javascript:toggleLegend();" colspan="3">
                                            <i>
                                                <xsl:value-of select="$localeThes/message[@id='closeLegend']/@*[name() = $nameLang]"/>
                                            </i>&#160;[x]
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="legendEntryThes" colspan="3">
                                            <span class="headerLegendThes">
                                                <xsl:value-of select="$localeThes/message[@id='legendText']/@*[name() = $nameLang]"/>
                                            </span>
                                        </td>
                                    </tr>
                                    <xsl:for-each select="$localeThes/message[@order]">
                                        <xsl:sort data-type="number" select="./@order"/>
                                        <tr>
                                            <!-- <td class="style1Thes"><xsl:value-of select="./@id"/></td> -->
                                            
                                            <td class="style3Thes">
                                                <xsl:value-of select="./@*[name() = $nameLang]"/>
                                            </td>
                                            <td class="style2Thes">
                                                <xsl:value-of select="./@*[name() = $legendDescrLang]"/>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <xsl:call-template name="page_logos"/>
                    <!--<table style="width:100%; margin-left:auto; margin-right:auto;" >
                        <tr>
                            <td style="text-align:center;">
                                <img border="0" style="margin-top:10px; margin-left:auto; margin-right:auto;">
                                    <xsl:attribute name="src">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </xsl:attribute>
                                </img>
                            </td>
                        </tr>
                    </table>   -->     
                </div>
        
            </body>
        </html>
    </xsl:template>
	
    <!-- _________________________________________________________________________________________________
              template: peploScreen
        _________________________________________________________________________________________________
              FUNCTION: template that defines the peplo screen (hidden initially)
    _________________________________________________________________________________________________ -->
    <xsl:template name="peploScreen">
        <div id="peploScreen" style="z-index:300; visibility:hidden; vertical-align:middle; position:absolute; display:block; top:0px; left:0px; width:100%; height:100%;  background:url(images/peplo.png) repeat; ">
        </div>
    </xsl:template>
</xsl:stylesheet>
