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
		template: page_footer
	  _________________________________________________________________________________________________
		FUNCTION: template that builds the footer of each page (copyright text)
      _________________________________________________________________________________________________ -->
    <xsl:template name="page_footer">
        <table style="height:100%; width:100%;" cellspacing="0" cellpadding="0" border="0" >
            <tr valign="middle" style="height:5px; padding-bottom:0px; padding-top:0px;">
                <td  colspan="3" valign="middle" class="hrrow">
                   
                   
                </td>
            </tr>
            <tr valign="middle" style="height:26px;" >
                <td  width="316" valign="middle" style="height:12px; color:#898a5e; font-size:9px; text-align:left; padding-left:5px; padding-right:5px;">
                        <xsl:choose>
                            <xsl:when test="//THEMASUserInfo/name">
                                 <i>
                                     <xsl:choose>
                                            <xsl:when test="//THEMASUserInfo/userGroup = 'READER' ">
                                                    <xsl:value-of select="//THEMASUserInfo/name"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                    <a href="#" style="color:#898a5e; text-decoration:underline;" title="Αλλαγή κωδικού">
                                                            <xsl:attribute name="onClick"><xsl:text>showEditFieldCard('','change_password','EditDisplays_User');</xsl:text></xsl:attribute>
                                                            <xsl:value-of select="//THEMASUserInfo/name"/>
                                                    </a>
                                            </xsl:otherwise>
                                    </xsl:choose>					
                                   <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                                    <xsl:text>, </xsl:text><xsl:value-of select="$locale/usergroups/node()[name()=$THEMASUserInfo_userGroup]/option[@lang=$lang]"/><xsl:text> </xsl:text><xsl:value-of select="//THEMASUserInfo/selectedThesaurus"/>
                                </i>
                            </xsl:when>
                            
                        </xsl:choose>
                        
                </td>
                <td width="380"  valign="middle" style="color:#898B5E; font-size:10px; text-align:center; padding-left:5px; padding-right:5px;">
                    <xsl:value-of disable-output-escaping="yes" select="$locale/footer/owner/option[@lang=$lang]"/>
                </td>
                <td  width="316" valign="middle"  style="color:#898a5e; font-size:9px; text-align:right; padding-left:5px; padding-right:5px;">
                    <!--<b>target="_blank" href="http://www.ics.forth.gr/isl/cci-gr.html"-->
                    <i>
                        <xsl:choose>
                            <xsl:when test="$locale/footer/appurl/option[@lang=$lang]/text()!=''">
                                <a style="color:#898B5E; text-decoration:underline;" target="_blank">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="$locale/footer/appurl/option[@lang=$lang]"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="$locale/footer/appname/option[@lang=$lang]"/>
                                </a>
                                <a style="color:#898B5E;">
                                    <xsl:text> © 2015 </xsl:text>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <a style="color:#898B5E;">
                                    <xsl:value-of select="$locale/footer/appname/option[@lang=$lang]"/><xsl:text> © 2015 </xsl:text>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>                        
                    </i>
                    <i>
                        <u>
                            <a style="color:#898B5E; text-decoration:underline;" target="_blank" >
                                <xsl:attribute name="href">
                                    <xsl:value-of select="$locale/footer/tooltiphref/option[@lang=$lang]"/>
                                </xsl:attribute>
                                <xsl:attribute name="onmouseover"><!-- dark yellow of left menu = #e8e9be light yellow #feffd9 body grey = #eaead9-->
                                    <xsl:text>javascript:this.T_ABOVE = false;this.T_OFFSETY='20';this.T_BGCOLOR='#FFFFFF';this.T_FONTSIZE='8pt';this.T_FONTFACE='verdana';</xsl:text>
                                    <xsl:text>return escape('&lt;b&gt;</xsl:text>
                                    <xsl:value-of select="$locale/footer/tooltipappnameandversion/option[@lang=$lang]"/>
                                    <xsl:text> &lt;br/&gt;Copyright </xsl:text>
                                    <xsl:value-of select="$locale/footer/tooltipcreator/option[@lang=$lang]"/>
                                    <xsl:text>.&lt;/b&gt;');</xsl:text>
                                    <!--<xsl:text>. All rights reserved.&lt;/b&gt;');</xsl:text>-->
                                </xsl:attribute>
                                <xsl:value-of select="$locale/footer/creator/option[@lang=$lang]"/>
                            </a>
                        </u>
                    </i>
                    <i>
                    </i>
                    
                    <script language="JavaScript" type="text/javascript" src="third-party-javascript/wztooltip/wz_tooltip.js"></script>
                    <!--</b>-->
                </td>
            </tr>
            <tr valign="middle" style="height:5px;">
                <td  colspan="3" valign="middle" class="hrrow">
                    
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
