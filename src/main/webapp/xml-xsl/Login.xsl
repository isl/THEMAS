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
        
    <xsl:include href="page_header.xsl"/>
    <xsl:include href="page_footer.xsl"/>
    <xsl:include href="Configs.xsl"/>
    <xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
    <xsl:variable name="lang" select="//page/@language"/>
    <xsl:variable name="ReleaseThesaurus_BLANK_VALUE_for_empty_select_item">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
    </xsl:variable>    	
    <xsl:output method="html"  
                       encoding="UTF-8"  
                       indent="yes" 
                       doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                       doctype-system="http://www.w3.org/TR/html4/loose.dtd"
                       version="4.0" />	
	
    <xsl:template match="/">
        <html>
        
            <head>
                <meta http-equiv="Content-Language" content="el" />

                <title>
                    <xsl:value-of select="$locale/header/title/option[@lang=$lang]"/>
                </title>
                <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@" />
                <script language="JavaScript" type="text/javascript" src="Javascript/scripts.js?v=@DeploymentTimestamp@"/>           
                <xsl:if test="$locale/header/favicon/text()!=''">
                    <link rel='shortcut icon' type='image/x-icon'>
                        <xsl:attribute name="href"><xsl:value-of select="$locale/header/favicon/text()"/><xsl:text>?v=@DeploymentTimestamp@</xsl:text></xsl:attribute>
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
                    
                        <div id="headerLogin">
                            <!-- just like <xsl:template name="page_header"> but with different image -->
                            <div>
                                <img width="1024">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$locale/loginpage/loginheader/image/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$locale/header/name/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="alt">
                                        <xsl:value-of select="$locale/header/name/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                </img>
                                
                                <select id="langSelectionControl" style="z-index: 100; float: left; position: absolute;right: 5px; top: 35px;" onchange="setLangCode('langSelectionControl','langID');">
                                    <xsl:for-each select="//availableUILangs/langcode">
                                        <option>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="./@code"/>
                                            </xsl:attribute>
                                            <xsl:if test="$lang = ./@code">
                                                <xsl:attribute name="selected">
                                                    <xsl:text>selected</xsl:text>
                                                </xsl:attribute>
                                            </xsl:if>
                                            <xsl:value-of select="./text()"/>
                                        </option>                                    
                                    </xsl:for-each>
                                </select>                                
                            </div>                          
                        </div>
                    
                        <div id="logindiv" >
                            <xsl:call-template name="Login_Contents"/>
                        </div>
                    
                        <div id="footer" align="center">
                            <xsl:call-template name="page_footer"/>
                        </div>
                    </div>
                    <table style="width:100%; margin-left:auto; margin-right:auto;" >
                        <tr>
                            <td style="text-align:center;">
                                <img border="0" style="margin-top:10px; margin-left:auto; margin-right:auto;">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$locale/loginpage/image/src/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$locale/loginpage/image/title/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                </img>
                            </td>
                        </tr>
                    </table>
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
        <table border="0" style="width:460px; height:350px; margin-left:auto; margin-right:auto;">
            <tr style="width:100%; height:40px;">
                <td>
                    <xsl:text> </xsl:text>
                </td>
            </tr>
            <tr>
                <td>
                    <fieldset class="index">
                        <legend>
                            <xsl:value-of select="$locale/loginpage/loginfieldset/legend/option[@lang=$lang]"/>
                        </legend>
                        <br/>
                        <form name="loginForm" method="post" action="Links">
                            <table width="295" cellspacing="0" cellpadding="5" align="center">
                                <!-- User name -->
                                <tr style="width:100%; background-color:#FFFFFF;">
                                    <td align="right">
                                        <xsl:value-of select="$locale/loginpage/username/option[@lang=$lang]"/>
                                    </td>
                                    <td>
                                        <input type="text" name="username" style="width:160px;" onfocus="this.style.border='1px solid #000000'" onblur="this.style.border='1px solid #999966'"/>
                                    </td>
                                </tr>
                                <!-- Password -->
                                <tr style="width:100%; background-color:#FFFFFF;">
                                    <td align="right">
                                        <xsl:value-of select="$locale/loginpage/password/option[@lang=$lang]"/>
                                    </td>
                                    <td>
                                        <input type="password" name="password" style="width:160px;" onfocus="this.style.border='1px solid #000000'" onblur="this.style.border='1px solid #999966'"/>
                                    </td>
                                </tr>
                                <!-- Thesaurus Selection -->
                                <tr style="width:100%; background-color:#FFFFFF;">
                                    <td align="right">
                                        <xsl:value-of select="$locale/loginpage/thesaurus/option[@lang=$lang]"/>                                            
                                    </td>
                                    <td>
                                        <select id="selectedThesaurusID" name="selectedThesaurusNAME" size="1" style="width:160px;" onkeypress="if (event.keyCode == 13) document.forms['loginForm'].submit();">
                                            <xsl:for-each select="//existingThesaurus/Thesaurus">
                                                <option>
                                                    <xsl:if test=". = ../Thesaurus[1] ">
                                                        <xsl:attribute name="selected">
                                                            <xsl:text>selected</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                    <xsl:value-of select="."/>
                                                </option>
                                            </xsl:for-each>
                                            <xsl:if test="count(//existingThesaurus/Thesaurus) = 0 "> <!-- in case of no existing Thesaurus -->
                                                <option>
                                                    <xsl:value-of select="$ReleaseThesaurus_BLANK_VALUE_for_empty_select_item"/>
                                                </option>
                                            </xsl:if>
                                        </select>
                                    </td>
                                </tr>
                                <tr style="width:100%;">
                                    <td colspan="2" align="right" style="padding-top:10px; padding-right:18px;">
                                        <input id="langID" type="hidden" name="lang" class="button">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$lang"/>
                                            </xsl:attribute>
                                        </input>&#160;
                                        <input type="submit" name="Submit" class="button">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$locale/loginpage/submitbutton/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </input>&#160;
                                        <input type="reset" name="Reset" class="button">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$locale/loginpage/cancelbutton/option[@lang=$lang]"/>
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
                        
                    <xsl:value-of disable-output-escaping="yes" select="$locale/loginpage/instructions/option[@lang=$lang]"/>
                        
                    <!-- insert some text with instructions for login-->
                </td>
            </tr>
            <tr>
                <td>
                    <!--
                    <xsl:if test="$locale/loginpage/image/src/option[@lang=$lang] != ''">
                        <xsl:choose>
                            <xsl:when test="$locale/loginpage/image/href/option[@lang=$lang]!=''">
                                <a target="_blank" style="margin-top:10px; margin-bottom:10px; text-align:center;">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="$locale/loginpage/image/href/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    
                                    <img border="0">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/loginpage/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/loginpage/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                 <img border="0" style="margin-top:10px; margin-bottom:10px; text-align:center;">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$locale/loginpage/image/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$locale/loginpage/image/title/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                    </img>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                    </xsl:if>
                    -->
                </td>
            </tr>
            <tr>
                <td/>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
