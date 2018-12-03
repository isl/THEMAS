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
	<xsl:include href="../page_header.xsl"/>
	<xsl:include href="../page_footer.xsl"/>
        <xsl:include href="../Configs.xsl"/>
        
	<xsl:variable name="locale" select="document('../../translations/translations.xml')/locale"/>
	<xsl:variable name="lang" select="//page/@language"/>
	<xsl:variable name="ReleaseThesaurus_BLANK_VALUE_for_empty_select_item">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:variable>    	
	<xsl:output method="html"  
            encoding="UTF-8"  
            indent="yes" 
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            version="4.0" />
	<!-- _________________________________________________________________________________________________
		START template
      _________________________________________________________________________________________________ -->
	<xsl:template match="/">
		<xsl:call-template name="Login_Page"/>
	</xsl:template>
	<!-- _________________________________________________________________________________________________
		template: Login_Page
	  _________________________________________________________________________________________________
		FUNCTION: template that builds the structure of the Login page
      _________________________________________________________________________________________________ -->
	<xsl:template name="Login_Page">
		<html>
                    
                    <head>
			<title>
				<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/header/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
			</title>
			<link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>
			<script language="JavaScript" src="Javascript/scripts.js?v=@DeploymentTimestamp@"/>		
                        <xsl:if test="$locale/header/favicon/text()!=''">
                            <link rel='shortcut icon' type='image/x-icon'>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="$locale/header/favicon/text()"/>
                                    <xsl:text>?v=@DeploymentTimestamp@</xsl:text>
                                </xsl:attribute>
                            </link>
                        </xsl:if>		
		</head>
		<body onload="document.loginForm.username.focus();">
                    <div id="all">
                        <xsl:if test="$pageBgColor!=''">
                            <xsl:attribute name="style">
                                <xsl:text>background-color:</xsl:text>
                                <xsl:value-of select="$pageBgColor"/>
                                <xsl:text>;</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                        <div id="loginwrapper">
                
                            <div id="header">
                                <xsl:call-template name="page_header"/>                                    
                            </div>
                            
                            <div id="logindiv">
                                <xsl:call-template name="Login_Contents"/>
                            </div>
                            
                            <div id="footer" align="center">
                                <xsl:call-template name="page_footer"/>
                            </div>
                            
                        </div>
                    </div>
                
                </body>
        </html>
</xsl:template>

	<!-- _________________________________________________________________________________________________
		template: Login_Contents
	  _________________________________________________________________________________________________
		FUNCTION: template that fills the contents of the Login page
      _________________________________________________________________________________________________ -->
	<xsl:template name="Login_Contents">
             <table border="0" style="width:460px; height:100%; margin-left:auto; margin-right:auto;">
                <tr style="width:100%; height:63px;">
                    <td>
                        <xsl:text> </xsl:text>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fieldset class="index">
                            <legend>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/loginfieldset/legend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </legend>
                            <br/>
                            <form name="loginForm" method="post" action="HiddenActions">
                                <table width="295" cellspacing="0" cellpadding="5" align="center">
                                                    <!-- Όνομα Χρήστη -->
                                    <tr style="width:100%; background-color:#F2F2F2;">
                                        <td align="right">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/loginfieldset/adminname/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </td>
                                        <td>
                                            <input type="text" name="username" style="width:130px;" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'"/>
                                        </td>
                                    </tr>
                                                    <!-- Κωδικός -->
                                    <tr style="width:100%; background-color:#F2F2F2;">
                                        <td align="right">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/loginfieldset/password/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </td>
                                        <td>
                                            <input type="password" name="password" style="width:130px;" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'"/>
                                        </td>
                                    </tr>
                                    
                                    <tr style="width:100%;">
                                        <td colspan="2" align="right" style="padding-top:10px;">
                                            <input type="submit" name="Submit" class="button">
                                                <xsl:attribute name="value">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/loginfieldset/submitbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                            </input>&#160;
                                            <input type="reset" name="Reset" class="button">
                                                <xsl:attribute name="value">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginadmin/loginfieldset/cancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                            </input>
                                        </td>
                                    </tr>
                                </table>
                            </form>
                        </fieldset>
                    </td>
                </tr>
                <tr>
                    <td style="color:#898a5e; font-size:9px;" align="center">
                        <!-- insert some text with instructions for login-->
                    </td>
                </tr>
                <tr valign="bottom" align="center" style="height:100%;">
                    <td><!-- get the rest of space. Image area-->
                    </td>
                </tr>
            </table>
		
			
                
	</xsl:template>
</xsl:stylesheet>
