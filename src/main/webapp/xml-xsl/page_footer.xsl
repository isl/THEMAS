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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <!-- _________________________________________________________________________________________________
              template: page_footer
        _________________________________________________________________________________________________
              FUNCTION: template that builds the footer of each page (copyright text)
    _________________________________________________________________________________________________ -->
    <xsl:template name="page_footer">
        <div id="footer" align="center">
        <table style="height:100%; width:100%;" cellspacing="0" cellpadding="0" border="0" >
            <tr valign="middle" style="height:5px; padding-bottom:0px; padding-top:0px;">
                <td  colspan="3" valign="middle" class="hrrow">
                   
                   
                </td>
            </tr>
            <tr valign="middle" style="height:26px;" >
                <!--  border:2px solid #999999; -->
                <td  width="200" valign="middle" style="height:12px; color:#898a5e; font-size:9px; text-align:left; padding-left:5px; padding-right:5px;">
                    <xsl:choose>
                        <xsl:when test="//THEMASUserInfo/name">
                            <i>
                                <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/usergroups/node()[name()=$THEMASUserInfo_userGroup]/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template> 
                                
                                <xsl:choose>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'READER'">
                                        <xsl:text>: </xsl:text>
                                        <xsl:value-of select="//THEMASUserInfo/name"/>
                                        <br/>
                                    </xsl:when>
                                    <xsl:when test="$THEMASUserInfo_userGroup = 'EXTERNALREADER'">
                                        <br/>    
                                    </xsl:when>
                                    <xsl:otherwise>
                                           
                                        <xsl:text>: </xsl:text>
                                        
                                          
                                        <a href="#" style="color:#898a5e; text-decoration:underline;" title="Αλλαγή κωδικού">
                                            <xsl:attribute name="onClick">
                                                <xsl:text>showEditFieldCard('','change_password','EditDisplays_User');</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="//THEMASUserInfo/name"/>
                                        </a>
                                        <br/>
                                    </xsl:otherwise>
                                </xsl:choose>					
                                 
                                <!--<xsl:value-of select="$locale/usergroups/node()[name()=$THEMASUserInfo_userGroup]/option[@lang=$lang]"/>-->
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="//THEMASUserInfo/selectedThesaurus"/>
                            </i>
                        </xsl:when>
                            
                    </xsl:choose>
                        
                </td>
                <!-- border:2px solid #999999; -->
                <td width="612"  valign="middle" style="color:#898B5E; font-size:10px; text-align:center; padding-left:5px; padding-right:5px;  ">
                    <u>
                        <a style="color:#898B5E; text-decoration:underline;" target="_blank" >
                            <xsl:attribute name="href">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/usagetermsurl/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>                                
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/usageterms/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </u>
                    
                    <xsl:text> | </xsl:text>
                    <u>
                        <a style="color:#898B5E; text-decoration:underline;" target="_blank" >
                            <xsl:attribute name="href">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/privacypolicyurl/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>                                
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/privacypolicy/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </u>                        
                    
                    <xsl:text> | </xsl:text>
                    
                    <xsl:text>© 2015</xsl:text>
                    <span id="untilYear"> - 2018 </span>
                    <script language="javascript" type="text/javascript"> var d = new Date(); document.getElementById("untilYear").innerHTML = ('- ' + d.getFullYear()+' ');</script>
                    <u>
                        <a style="color:#898B5E; text-decoration:underline;" target="_blank" >
                            <xsl:attribute name="href">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/tooltiphref/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/creator/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </u>
                    <xsl:text> | </xsl:text>
                    
                    <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$locale/footer/licencePrefixText/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'yes'"/> 
                    </xsl:call-template>    
                    <u>
                        <a style="color:#898B5E; text-decoration:underline;" target="_blank" >
                            
                            <xsl:attribute name="href">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/licenceHrefUrl/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>                                
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/licenceHrefText/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </a>
                    </u>
                    
                </td>
                <!--  border:2px solid #999999; -->
                <td  width="200" valign="middle"  style="color:#898a5e; font-size:9px; text-align:right; padding-left:5px; padding-right:5px;">
                    <!--<b>target="_blank" href="https://www.ics.forth.gr/isl/centre-cultural-informatics?lang=el"-->
                    <i>
                        <xsl:choose>
                            <xsl:when test="$locale/footer/appurl/option[@lang=$lang]/text()!=''">
                                <a style="color:#898B5E; text-decoration:underline;" target="_blank">
                                    <xsl:attribute name="onmouseover"><!-- dark yellow of left menu = #e8e9be light yellow #feffd9 body grey = #eaead9-->
                                        <xsl:text>javascript:this.T_ABOVE = false;this.T_OFFSETY='20';this.T_BGCOLOR='#FFFFFF';this.T_FONTSIZE='8pt';this.T_FONTFACE='verdana';</xsl:text>
                                        <xsl:text>return escape('&lt;b&gt;</xsl:text>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$locale/footer/tooltipappnameandversion/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                        <xsl:text> </xsl:text>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$locale/version/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                        <xsl:text> &lt;br/&gt;Copyright </xsl:text>
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$locale/footer/tooltipcreator/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                        <xsl:text>.&lt;/b&gt;');</xsl:text>
                                        <!--<xsl:text>. All rights reserved.&lt;/b&gt;');</xsl:text>-->
                                    </xsl:attribute>
                                    <xsl:attribute name="href">
                                        <xsl:call-template name="getTranslationMessage"> 
                                            <xsl:with-param name="targetLangElements" select="$locale/footer/appurl/option"/> 
                                            <xsl:with-param name="targetLang" select="$lang"/> 
                                            <xsl:with-param name="disableEscape" select="'no'"/> 
                                        </xsl:call-template>    
                                    </xsl:attribute>
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$locale/footer/appname/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </a>
                                <!--<a style="color:#898B5E;">
                                    <xsl:text> © 2015 </xsl:text>
                                </a>-->
                            </xsl:when>
                            <xsl:otherwise>
                                <a style="color:#898B5E;">
                                    <xsl:call-template name="getTranslationMessage"> 
                                        <xsl:with-param name="targetLangElements" select="$locale/footer/appname/option"/> 
                                        <xsl:with-param name="targetLang" select="$lang"/> 
                                        <xsl:with-param name="disableEscape" select="'no'"/> 
                                    </xsl:call-template>    
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>                        
                    </i>
                    
                    <script language="JavaScript" type="text/javascript" src="third-party-javascript/wztooltip/wz_tooltip.js?v=@DeploymentTimestamp@"></script>
                    <!--</b>-->
                </td>
            </tr>
            <tr valign="middle" style="height:5px;">
                <td  colspan="3" valign="middle" class="hrrow">
                    
                </td>
            </tr>
        </table>
        </div>
        <!--<table style="width:100%; margin-left:auto; margin-right:auto;" >
                        <tr>
                            <td style="text-align:center;">-->
        
        <!--                    </td>
                        </tr>
                    </table>-->
    </xsl:template>
    
    <xsl:template name="page_logos">
        <div id="page-logos">
            <div id="logos-line-1" class="w3-row">
                <div class="w3-col m3">
                    <a target="_blank">
                        <xsl:attribute name="href">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogoimage/href/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>
                        <img border="0" id="project-logo-img" class="img-responsive">
                            <xsl:attribute name="style">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogoimage/stylevalue/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="src">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogoimage/src/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="getTranslationMessage">
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogoimage/title/option"/>                            
                                    <xsl:with-param name="targetLang" select="$lang"/>
                                    <xsl:with-param name="disableEscape" select="'no'"/>
                                </xsl:call-template>
                                <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    -->
                            </xsl:attribute>
                        </img>
                    </a>

                </div>
                <div class="w3-col m6" align="middle">
                    <img border="0" id="acknowledgement-logo-img" class="img-responsive">
                        <!--<xsl:attribute name="style">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/stylevalue/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>-->
                        <xsl:attribute name="src">
                            <xsl:call-template name="getTranslationMessage">
                                <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/src/option"/>                            
                                <xsl:with-param name="targetLang" select="$lang"/>
                                <xsl:with-param name="disableEscape" select="'no'"/>
                            </xsl:call-template>
                            <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    -->
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="getTranslationMessage">
                                <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/>                            
                                <xsl:with-param name="targetLang" select="$lang"/>
                                <xsl:with-param name="disableEscape" select="'no'"/>
                            </xsl:call-template>
                            <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    -->
                        </xsl:attribute>
                    </img>
                </div>

                
                
                <div class="w3-col m3">
                    
                    <a target="_blank">
                        <xsl:attribute name="href">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogorightsideimage/href/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>
                        <img border="0" id="project-logo-right-side-img" class="img-responsive">
                            <xsl:attribute name="style">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogorightsideimage/stylevalue/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="src">
                                <xsl:call-template name="getTranslationMessage"> 
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogorightsideimage/src/option"/> 
                                    <xsl:with-param name="targetLang" select="$lang"/> 
                                    <xsl:with-param name="disableEscape" select="'no'"/> 
                                </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="getTranslationMessage">
                                    <xsl:with-param name="targetLangElements" select="$locale/footer/projectlogorightsideimage/title/option"/>                            
                                    <xsl:with-param name="targetLang" select="$lang"/>
                                    <xsl:with-param name="disableEscape" select="'no'"/>
                                </xsl:call-template>
                                
                            </xsl:attribute>
                        </img>
                    </a>
                </div>
            </div>
            <div id="logos-line-2">
                
                <a target="_blank">
                    <xsl:attribute name="href">
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$locale/footer/developedbyimage/href/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>    
                    </xsl:attribute>
                    <img border="0">
                        <!--<xsl:attribute name="style">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/developedbyimage/stylevalue/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>-->
                        <xsl:attribute name="src">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/developedbyimage/src/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="getTranslationMessage">
                                <xsl:with-param name="targetLangElements" select="$locale/footer/developedbyimage/title/option"/>                            
                                <xsl:with-param name="targetLang" select="$lang"/>
                                <xsl:with-param name="disableEscape" select="'no'"/>
                            </xsl:call-template>
                            <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/loginpage/image/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    -->
                        </xsl:attribute>
                    </img>
                </a>
            </div>
            <!--
            <div id="logos-line-3">
                <span class="developedtext">
                     <xsl:call-template name="getTranslationMessage"> 
                        <xsl:with-param name="targetLangElements" select="$locale/footer/developedbytext/prefixText/option"/> 
                        <xsl:with-param name="targetLang" select="$lang"/> 
                        <xsl:with-param name="disableEscape" select="'no'"/> 
                    </xsl:call-template>
                </span>
                <span class="teamtext">
                    <a target="_blank">
                        <xsl:attribute name="href">
                            <xsl:call-template name="getTranslationMessage"> 
                                <xsl:with-param name="targetLangElements" select="$locale/footer/developedbytext/linkhref/option"/> 
                                <xsl:with-param name="targetLang" select="$lang"/> 
                                <xsl:with-param name="disableEscape" select="'no'"/> 
                            </xsl:call-template>    
                        </xsl:attribute>
                    
                    
                        <xsl:call-template name="getTranslationMessage"> 
                            <xsl:with-param name="targetLangElements" select="$locale/footer/developedbytext/linktext/option"/> 
                            <xsl:with-param name="targetLang" select="$lang"/> 
                            <xsl:with-param name="disableEscape" select="'no'"/> 
                        </xsl:call-template>  
                    </a>
                </span>
            </div>
            -->
        </div>
    </xsl:template>
</xsl:stylesheet>
