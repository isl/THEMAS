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
<xsl:stylesheet version="2.0" 
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
        xmlns:fo="http://www.w3.org/1999/XSL/Format" 
        xmlns:xs="http://www.w3.org/2001/XMLSchema" 
        xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
        xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"
        exclude-result-prefixes="xsl fo xs fn xdt">
    	<xsl:output method="html"  
            encoding="UTF-8"  
            indent="yes" 
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            version="4.0" />
	<xsl:import href="./Configs.xsl"/>
	<xsl:include href="page_header.xsl"/>
	<xsl:include href="page_footer.xsl"/>
	<xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
	<xsl:variable name="lang" select="//page/@language"/>
	<xsl:variable name="ReleaseThesaurus_BLANK_VALUE_for_empty_select_item">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:variable>    	
	
	<!-- _________________________________________________________________________________________________
		START template
      _________________________________________________________________________________________________ -->
	<xsl:template match="/">
		<xsl:call-template name="SystemIsUnderMaintenance_Page"/>
	</xsl:template>
	<!-- _________________________________________________________________________________________________
		template: Login_Page
	  _________________________________________________________________________________________________
		FUNCTION: template that builds the structure of the SystemIsUnderMaintenance page
      _________________________________________________________________________________________________ -->
	<xsl:template name="SystemIsUnderMaintenance_Page">
		<html>
			<xsl:call-template name="SystemIsUnderMaintenance_HTML_head">
				<xsl:with-param name="javascript" select="'Javascript/scripts.js?v=@DeploymentTimestamp@'"/>
				<xsl:with-param name="css" select="'CSS/page.css?v=@DeploymentTimestamp@'"/>
			</xsl:call-template>
			<!-- karam (22/1/08) set focus to username input at page loading -->
			<body>
				<div id="maintenancepage">
					<div id="header">
						<xsl:call-template name="page_header"/>
					</div>
					<div id="logindiv">
						<xsl:call-template name="SystemIsUnderMaintenance_Contents"/>
					</div>
					<div id="footer" align="center">
						<xsl:call-template name="page_footer"/>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	<!-- _________________________________________________________________________________________________
		template: SystemIsUnderMaintenance_HTML_head
	  _________________________________________________________________________________________________
		FUNCTION: template that fills the header of the SystemIsUnderMaintenance page
      _________________________________________________________________________________________________ -->
	<xsl:template name="SystemIsUnderMaintenance_HTML_head">
		<xsl:param name="javascript"/>
		<xsl:param name="css"/>
		<head>
			<title>
				<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/header/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
			</title>
			<link rel="stylesheet" type="text/css">
				<xsl:attribute name="href"><xsl:value-of select="$css"/></xsl:attribute>
			</link>
                        <script language="JavaScript">
				<xsl:attribute name="src"><xsl:value-of select="$javascript"/></xsl:attribute>
			</script>
		</head>
	</xsl:template>
	<!-- _________________________________________________________________________________________________
		template: SystemIsUnderMaintenance_Contents
	  _________________________________________________________________________________________________
		FUNCTION: template that fills the contents of the SystemIsUnderMaintenance page
      _________________________________________________________________________________________________ -->
	<xsl:template name="SystemIsUnderMaintenance_Contents">
            
		<table border="0" style="width:460px; height:100%; margin-left:auto; margin-right:auto;">
                <tr style="width:100%; height:63px;">
                    <td>
                        <xsl:text> </xsl:text>
                    </td>
                </tr>
                <tr valign="top">
                    <td>
			<fieldset class="index" >
                            
				<legend>
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </legend>
                                <table style="margin-left:auto; margin-right:auto;">
                                    <tr>
                                        <td style="text-align:center;">
                                            <br/>
					<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/operationsperformed/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
					<br/>
					<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/please/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        <a href="Index">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/tryagain/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </a>
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/later/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
					<br/><br/>
					<img border="0">
                                            <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/generalmessages/undermaintenance/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                        </img>
                                        <br/><br/>
                                        </td>
                                    </tr>
                                </table>
					
			</fieldset>
		</td>
                </tr>
                </table>
	</xsl:template>
</xsl:stylesheet>
