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

<!--<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">-->
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <xsl:include href="PagingInfo_Term_ Alphabetical.xsl"/>
    <xsl:variable name="localeThes" select="document('../translations/labels.xml')/locale/messages"/>
    <xsl:variable name="labels" select="document('../translations/labels.xml')/locale/vars"/>
    <xsl:variable name="lang2" select="//page/@language"/>
    <xsl:variable name="nameLang" select="concat('name_',$lang2)"/>
    <xsl:variable name="legendDescrLang" select="concat('legend_descr_',$lang2)"/>

    
    <xsl:variable name="alphabeticalresultslocale" select="document('../translations/translations.xml')/locale/primarycontentarea/terms/alphabeticalresults"/>
    <xsl:variable name="Slash_Alpha">
        <xsl:text>\</xsl:text>
    </xsl:variable>
    <xsl:variable name="SlashJS_Alpha">
        <xsl:text>\\</xsl:text>
    </xsl:variable>
    <xsl:variable name="Apos_Alpha">
        <xsl:text>'</xsl:text>
    </xsl:variable>
    <xsl:variable name="AposJS_Alpha">
        <xsl:text>\'</xsl:text>
    </xsl:variable>

    <xsl:template match="/data" name="search_results_terms_alphabetical">
        <xsl:param name="paginglocale" />
        <!--<fieldset class="links">
            <legend>
                <xsl:value-of select="$alphabeticalresultslocale/legend/option[@lang=$lang2]" />
            </legend>
            -->
            <table width="100%" style="padding-left:5px;">
                <xsl:call-template name="DisplayStatisticsAndPagingInfo_Alphabetical">
                        <xsl:with-param name="paginglocale" select="$paginglocale" />
                        <xsl:with-param name="lang2" select="$lang2" />
                    </xsl:call-template>
            </table>
           <table width="100%" style="padding-left:5px;">
               <tr>
                   <td>
            <xsl:call-template name="alphabetical-display">
                <xsl:with-param name="prefferedWidth">100%</xsl:with-param>
                <xsl:with-param name="onHover">enable</xsl:with-param>
            </xsl:call-template>
           </td>
               </tr>
            </table>
        <!--</fieldset>-->
    </xsl:template>
    
    <!-- Copied with different name from utilities.xsl just because of reference problems of file utilities.xsl-->
    <xsl:template name="replace-string_alphabetical">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string_alphabetical">
                    <xsl:with-param name="text" select="substring-after($text,$replace)"/>
                    <xsl:with-param name="replace" select="$replace"/>
                    <xsl:with-param name="with" select="$with"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


        <!---->
    <xsl:template name="alphabetical-display">
        <xsl:param name="prefferedWidth"/>
        <xsl:param name="onHover"/>
        
        <xsl:variable name="trsSeperator" select="//data/@translationsSeperator"/>
       <!-- __________________ FOR EACH <term> or <ufterm> (BEGIN) ____________________ -->
        <xsl:variable name="allIds" select="//data/terms/term/descriptor/@id"/>
        
        <xsl:for-each select="//terms/term | //terms/ufterm">
            <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"-->
            <table  width="100%" cellspacing="0" cellpadding="0" >
                <xsl:if test="$onHover!=''">
                    <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>   
                        <!--
                    <xsl:attribute name="onMouseOut">
                        <xsl:text>this.bgColor = '#FFFFFF'</xsl:text>
                    </xsl:attribute> 
                    <xsl:attribute name="bgcolor">
                        <xsl:text>#FFFFFF</xsl:text>
                    </xsl:attribute> -->
                </xsl:if>
                                     
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
                                    <!--<xsl:text>background-color: #FFFFFF;</xsl:text>-->
                                </xsl:attribute>     
                                <xsl:attribute name="onMouseOut">
                                    <xsl:text>this.style.background = '</xsl:text>
                                    <xsl:value-of select="$alternateRowsColor2"/> 
                                    <xsl:text>'</xsl:text>
                                    <!--<xsl:text>this.style.background = '#FFFFFF'</xsl:text>-->
                                </xsl:attribute>                                           
                            </xsl:otherwise>
                        </xsl:choose>
                                <tr>
                                    <td>
                                        <!--<xsl:choose>
                                            <xsl:when test="position() mod 2 =0">
                                                <xsl:attribute name="bgcolor">
                                                    <xsl:text>#F2F2F2</xsl:text>
                                                </xsl:attribute>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="bgcolor">
                                                    <xsl:text>#FFFFFF</xsl:text>
                                                </xsl:attribute>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        -->
            <table cellspacing="0" cellpadding="0">
                <xsl:attribute name="width">
                    <xsl:value-of select="$prefferedWidth"/>
                </xsl:attribute>
                <tr class="rowThes">
                    <td colspan="2">
                        <xsl:choose>
                            <!-- in case the target is <term>, display it as BOLD -->
                            <xsl:when test="name() = 'term' ">
                                <span class="headerThes">
                                    <a class="aHeaderAnchorThes" name="{descriptor/@id}">
                                        <xsl:value-of select="descriptor"/>
                                    </a>
                                </span>
                            </xsl:when>
                            <!-- in case the target is <ufterm>, display it as NORMAL -->
                            <xsl:otherwise>
                                <span class="headerThes_normal" >
                                    <a class="aHeaderAnchorThes" name="{ufname/@id}">
                                        <xsl:value-of select="ufname"/>
                                    </a>
                                </span>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- taxonomic code -->
                        <xsl:if test="tc">
                            <xsl:for-each select="tc">
                                <span class="deweyHeaderThes">&#160;--&#160;(
                                    <xsl:value-of select="."/>)
                                </span>
                            </xsl:for-each>
                        </xsl:if>
                        <br/>
                    </td>
                </tr>
            </table>
            <!-- __________________ translations of target (<translations>) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="./translations"/>
                <xsl:with-param name="tagDisplayNameId" select="'TR'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'translations'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
                <xsl:with-param name="translationsSeparator" select="$trsSeperator"/>
                
            </xsl:call-template>
            <!-- __________________ ΔΣ of target (<scope_note> - ΔΣ) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="scope_note"/>
                <xsl:with-param name="tagDisplayNameId" select="'SN'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <!--<xsl:with-param name="displayFormat" select="'italics'"/>-->
                <xsl:with-param name="displayFormat" select="'none'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
            </xsl:call-template>
            <!-- __________________ SN of target (<translations_scope_note> - SN) ______________ -->
            <xsl:if test="count(translations_scope_note[text()!='']) >0">
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="translations_scope_note"/>
                <xsl:with-param name="tagDisplayNameId" select="'tr_SN'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <!--<xsl:with-param name="displayFormat" select="'italics'"/>-->
                <xsl:with-param name="displayFormat" select="'none'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
                <xsl:with-param name="translationsSeparator" select="$trsSeperator"/>
            </xsl:call-template>
            </xsl:if>
            <!--<xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="translations_scope_note"/>
                <xsl:with-param name="tagDisplayNameId" select="'tr_SN'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'italics'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
            </xsl:call-template>-->
            <!-- __________________ OK of target (<topterm> - OK) ____________________ DisplayTargetTagWithPopUp-->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="topterm"/>
                <xsl:with-param name="tagDisplayNameId" select="'TT'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'TERM'"/>
            </xsl:call-template>
            <!-- __________________UFs pointing of target (XA - <uf>) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="uf"/>
                <xsl:with-param name="tagDisplayNameId" select="'UF'"/>
                <xsl:with-param name="addAnchors" select="'true'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
                
            </xsl:call-template>            
            <!-- __________________UFs pointing of target (XA - <uf_translations>) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="uf_translations"/>
                <xsl:with-param name="tagDisplayNameId" select="'UF (Tra.)'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'translations'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
                <xsl:with-param name="translationsSeparator" select="$trsSeperator"/>
            </xsl:call-template>
            <!-- __________________BTs of target (<bt> - ΠΟ1 + ΠΟ2) (π.χ. αγροτική ανάπτυξη) ______________ -->
            <!-- __________________if BTs of target exist______________ -->
            <xsl:if test="bt">
                <table cellspacing="0" cellpadding="0" border="0">
                    <xsl:attribute name="width">
                        <xsl:value-of select="$prefferedWidth"/>
                    </xsl:attribute>							<!-- __________________for each BT of target BEGIN______________ -->
                    <xsl:for-each select="bt">
                        <tr class="rowThes">
                            <td class="typeColThes" valign="top">
                                <span class="typeThes">
                                    <xsl:value-of select="$localeThes/message[@id='BT1']/@*[name() = $nameLang]"/>
                                </span>
                            </td>
                            <td class="valueColThes">
                                <a >
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string_alphabetical">
                                            <xsl:with-param name="text" select="."/>
                                            <xsl:with-param name="replace" select="$Slash_Alpha"/>
                                            <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string_alphabetical">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos_Alpha"/>
                                            <xsl:with-param name="with" select="$AposJS_Alpha"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:attribute name="href">
                                        <xsl:text>#</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="onclick">
                                        <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                                    </xsl:attribute>
                                    <span class="valueThes">
                                        <xsl:value-of select="."/>
                                    </span>
                                </a>
                            </td>
                        </tr>
                        <tr class="rowThes">
                            <td valign="top" colspan="2">
                                <!-- 2nd level BTs -->
                                <!-- define the xsl:variable "currentBT1sysid" with value the sysid of the current BT1 term -->
                                <xsl:variable name="currentBT1sysid">
                                    <xsl:value-of select="./@id"/>
                                </xsl:variable>
                                <!-- get the BT term of xsl:variable "currentBT1sysid" -->
                                <table cellspacing="0" cellpadding="0" class="secondLevelTableThes" border="0">
                                    <xsl:attribute name="width">
                                        <xsl:value-of select="$prefferedWidth"/>
                                    </xsl:attribute>
                                    <xsl:for-each select="$allIds[.=$currentBT1sysid]/../../bt">
                                        <tr class="rowThes">
                                            <td class="emptyFirstCellThes" valign="top"/>
                                            <td class="typeColSecondThes" valign="top">
                                                <span class="typeThes">
                                                    <xsl:value-of select="$localeThes/message[@id='BT2']/@*[name() = $nameLang]"/>
                                                </span>
                                            </td>
                                            <td class="valueColSecondThes">
                                                <a>
                                                    <xsl:variable name="currentJS0">
                                                        <xsl:call-template name="replace-string_alphabetical">
                                                            <xsl:with-param name="text" select="."/>
                                                            <xsl:with-param name="replace" select="$Slash_Alpha"/>
                                                            <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="currentJS">
                                                        <xsl:call-template name="replace-string_alphabetical">
                                                            <xsl:with-param name="text" select="$currentJS0"/>
                                                            <xsl:with-param name="replace" select="$Apos_Alpha"/>
                                                            <xsl:with-param name="with" select="$AposJS_Alpha"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:attribute name="href">
                                                        <xsl:text>#</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                                                    </xsl:attribute>
                                                    <span class="valueThes">
                                                        <xsl:value-of select="."/>
                                                    </span>
                                                </a>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </table>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <!-- __________________for each BT of target END______________ -->
                </table>
            </xsl:if>
            <!-- __________________NTs of target (<bt> - EΟ1 + EΟ2) (π.χ. αγροτική παραγωγή - γεωργία) ______________ -->
            <!-- __________________if NTs of target exist______________ -->
            <xsl:if test="nt">
                <table cellspacing="0" cellpadding="0" border="0">
                    <xsl:attribute name="width">
                        <xsl:value-of select="$prefferedWidth"/>
                    </xsl:attribute>
                    <!-- __________________for each NT of target BEGIN______________ -->
                    <xsl:for-each select="nt">
                        <xsl:variable name="currentGuideTerm" select="./@linkClass"/>
                        <xsl:variable name="prevPos" select="position()-1"/>
                        <tr class="rowThes">
                            <td class="typeColThes" valign="bottom">
                                <span class="typeThes">
                                    <xsl:value-of select="$localeThes/message[@id='NT1']/@*[name() = $nameLang]"/>
                                </span>
                            </td>
                            <td class="valueColThes">
                                <!--<xsl:value-of select="preceding-sibling::*/../nt[$prevPos]/@linkClass"/>-->
                                <xsl:if test="(./@linkClass!='') and ((position()=1) or not(preceding-sibling::*/../nt[$prevPos]/@linkClass = ./@linkClass))">
                                    <span class="valueThes"><b><i><xsl:text>&lt;</xsl:text><xsl:value-of select="./@linkClass"/><xsl:text>&gt;</xsl:text></i></b></span>
                                    <br/>
                                </xsl:if>
                                
                                <a>
                                    <xsl:variable name="currentJS0">
                                        <xsl:call-template name="replace-string_alphabetical">
                                            <xsl:with-param name="text" select="."/>
                                            <xsl:with-param name="replace" select="$Slash_Alpha"/>
                                            <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:variable name="currentJS">
                                        <xsl:call-template name="replace-string_alphabetical">
                                            <xsl:with-param name="text" select="$currentJS0"/>
                                            <xsl:with-param name="replace" select="$Apos_Alpha"/>
                                            <xsl:with-param name="with" select="$AposJS_Alpha"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:attribute name="href">
                                        <xsl:text>#</xsl:text>
                                    </xsl:attribute>
                                    <xsl:attribute name="onclick">
                                        <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                                    </xsl:attribute>
                                    <span class="valueThes">
                                        <xsl:value-of select="."/>
                                    </span>
                                </a> 
                            </td>
                        </tr>
                        <tr class="rowThes">
                            <td valign="top" colspan="2">
                                <!-- 2nd level NTs (π.χ. αγροτική παραγωγή - γεωργία) -->
                                <!-- define the xsl:variable "currentNT1sysid" with value the sysid of the current NT1 term -->
                                <xsl:variable name="currentNT1sysid">
                                    <xsl:value-of select="./@id"/>
                                </xsl:variable>
                                <!-- get the NT term of xsl:variable "currentNT1sysid" -->
                                <table cellspacing="0" cellpadding="0" class="secondLevelTableThes" border="0">
                                    <xsl:attribute name="width">
                                        <xsl:value-of select="$prefferedWidth"/>
                                    </xsl:attribute>
                                    <xsl:for-each select="$allIds[.=$currentNT1sysid]/../../nt">
                                        <tr class="rowThes">
                                            <td class="emptyFirstCellThes" valign="top"/>
                                            <td class="typeColSecondThes" valign="top">
                                                <span class="typeThes">
                                                    <xsl:value-of select="$localeThes/message[@id='NT2']/@*[name() = $nameLang]"/>
                                                </span>
                                            </td>
                                            <td class="valueColSecondThes">
                                                <a>
                                                    <xsl:variable name="currentJS0">
                                                        <xsl:call-template name="replace-string_alphabetical">
                                                            <xsl:with-param name="text" select="."/>
                                                            <xsl:with-param name="replace" select="$Slash_Alpha"/>
                                                            <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="currentJS">
                                                        <xsl:call-template name="replace-string_alphabetical">
                                                            <xsl:with-param name="text" select="$currentJS0"/>
                                                            <xsl:with-param name="replace" select="$Apos_Alpha"/>
                                                            <xsl:with-param name="with" select="$AposJS_Alpha"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:attribute name="href">
                                                        <xsl:text>#</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="onclick">
                                                        <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                                                    </xsl:attribute>
                                                    <span class="valueThes">
                                                        <xsl:value-of select="."/>
                                                    </span>
                                                </a>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </table>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <!-- __________________for each NT of target END______________ -->
                </table>
            </xsl:if>
            <!-- __________________ RTs of target (<rt> - ΣΟ) - BOTH directions ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="rt"/>
                <xsl:with-param name="tagDisplayNameId" select="'RT'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'TERM'"/>
            </xsl:call-template>
            <!-- __________________Sources of target (<found_in> - Πηγή) ______________ 
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="found_in"/>
                <xsl:with-param name="tagDisplayNameId" select="'Source'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
            </xsl:call-template>
            -->
            <!-- __________________ Primary Sources of target (<primary_found_in> - Πηγή (ελλ.)) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="primary_found_in"/>
                <xsl:with-param name="tagDisplayNameId" select="'Source'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'SOURCE'"/>
            </xsl:call-template>

            <!-- __________________ Translation Sources of target (<translations_found_in> - Πηγή (αγγλ.)) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="translations_found_in"/>
                <xsl:with-param name="tagDisplayNameId" select="'Source_tr'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'SOURCE'"/>
            </xsl:call-template>
            <!-- __________________ <use> tags of target of type <ufterm> (ΧΡ) ______________ -->
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="use"/>
                <xsl:with-param name="tagDisplayNameId" select="'USE'"/>
                <xsl:with-param name="addAnchors" select="'true'"/>
                <xsl:with-param name="displayFormat" select="'bold'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'none'"/>
            </xsl:call-template>
            
            <xsl:call-template name="DisplayTargetTag">
                <xsl:with-param name="tagNode" select="created_by"/>
                <xsl:with-param name="tagDisplayNameId" select="'CREATED_BY'"/>
                <xsl:with-param name="addAnchors" select="'false'"/>
                <xsl:with-param name="displayFormat" select="'normal'"/>
                <xsl:with-param name="prefferedWidth" select="$prefferedWidth"/>
                <xsl:with-param name="popUpCardMode" select="'CREATED_BY'"/>
            </xsl:call-template>
            <!-- __________________ FOR EACH <term> or <ufterm> (END) ____________________ -->
            <br/>
                                    </td>
                                    </tr>
            </table>
        </xsl:for-each>
    </xsl:template>
    <!-- _____________________________________________________________________________
    TEMPLATE: DisplayTargetTag
    INPUT: - tagNode: the XML Node list of the xml tag to be displayed (g.e. translations, scope_note, translations_scope_note, topterm, uf, found_in, primary_found_in, translations_found_in, use)
    - tagDisplayNameId: the id (to be get from labels.xml <message>s) of the display name of the xml tag to be displayed (g.e. EN-TR, SN, tr_SN, TT, UF etc.)
    - addAnchors: true/false in case an anchor (<a>) is to be added (or not) to the displayed value
    - displayFormat: normal/bold/italics - the format of the displayed value
    FUNCTION: displays for the current <term> the values of the given XML Node list, tagNode
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayTargetTag">
		<!-- parameters -->
        <xsl:param name="tagNode"/>
        <xsl:param name="tagDisplayNameId"/>
        <xsl:param name="addAnchors"/>
        <xsl:param name="displayFormat"/>
        <xsl:param name="prefferedWidth"/>
        <xsl:param name="popUpCardMode"/>
        <xsl:param name="translationsSeparator"/>
        <!-- in case there is at least one tagNode -->
        <xsl:if test="$tagNode">
            <table cellspacing="0" cellpadding="0">
                <xsl:attribute name="width">
                    <xsl:value-of select="$prefferedWidth"/>
                </xsl:attribute>
                <tr class="rowThes">
                    <td class="typeColThes" valign="top">
                        <span class="typeThes">
                            <xsl:value-of select="$localeThes/message[@id=$tagDisplayNameId]/@*[name() = $nameLang]"/>
                        </span>
                    </td>
                    <td class="valueColThes">
                        <xsl:for-each select="$tagNode">
                            <xsl:choose>
                                <!-- __________________ with anchor __________________ -->
                                <xsl:when test="$addAnchors = 'true' ">
                                    <a href="#{./@id}">
                                        <xsl:call-template name="DisplayCurrentTagValue">
                                            <xsl:with-param name="displayFormat">
                                                <xsl:value-of select="$displayFormat"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="translationsSeparator">
                                                <xsl:value-of select="$translationsSeparator"/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </a>
                                </xsl:when>
                                <!-- __________________ without anchor __________________ -->
                                <xsl:otherwise>
                                    <a>
                                        <xsl:choose>          
                                        <xsl:when  test="$tagDisplayNameId = 'SN'">
                                            <span width="400" style="WORD-BREAK:BREAK-ALL;">
                                                
                                                <xsl:value-of disable-output-escaping="yes" select="."/>
                                                
                                            </span>
                                        </xsl:when>                
					<!--
                                        <xsl:when  test="$tagDisplayNameId = 'tr_SN'">
                                            <span width="400" style="WORD-BREAK:BREAK-ALL;">
                                                <xsl:value-of disable-output-escaping="yes" select="."/>
                                            </span>
                                        </xsl:when>                       
					-->                  
                                        <xsl:otherwise>                           
                                        <xsl:call-template name="DisplayCurrentTagValue">
                                            <xsl:with-param name="displayFormat" select="$displayFormat"/>
                                            <xsl:with-param name="popUpCardMode" select="$popUpCardMode"/>
                                            <xsl:with-param name="translationsSeparator">
                                                <xsl:value-of select="$translationsSeparator"/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                        </xsl:otherwise>
                                        </xsl:choose> 
                                    </a>
                                </xsl:otherwise>
                            </xsl:choose>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>
    <!-- _____________________________________________________________________________
    TEMPLATE: DisplayCurrentTagValue
    INPUT: - displayFormat: normal/bold/italics - the format of the displayed value
    FUNCTION: displays for the current tag's value with the given format (displayFormat)
    _____________________________________________________________________________ -->
    <xsl:template name="DisplayCurrentTagValue">
		<!-- parameters -->
        <xsl:param name="displayFormat"/>
        <xsl:param name="popUpCardMode"/>
        <xsl:param name="translationsSeparator"/>
        <xsl:choose>
            <xsl:when test="$popUpCardMode = 'TERM'">
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="."/>
                        <xsl:with-param name="replace" select="$Slash_Alpha"/>
                        <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos_Alpha"/>
                        <xsl:with-param name="with" select="$AposJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Term')</xsl:text>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$popUpCardMode = 'FACET'">
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="."/>
                        <xsl:with-param name="replace" select="$Slash_Alpha"/>
                        <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos_Alpha"/>
                        <xsl:with-param name="with" select="$AposJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Facet')</xsl:text>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$popUpCardMode = 'SOURCE'">
                <xsl:variable name="currentJS0">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="."/>
                        <xsl:with-param name="replace" select="$Slash_Alpha"/>
                        <xsl:with-param name="with" select="$SlashJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="currentJS">
                    <xsl:call-template name="replace-string_alphabetical">
                        <xsl:with-param name="text" select="$currentJS0"/>
                        <xsl:with-param name="replace" select="$Apos_Alpha"/>
                        <xsl:with-param name="with" select="$AposJS_Alpha"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="onclick">
                    <xsl:text>popUpCard('</xsl:text><xsl:value-of select="$currentJS"/><xsl:text>','CardOf_Source')</xsl:text>
                </xsl:attribute>
            </xsl:when>
        </xsl:choose>
        <span class="valueThes">
            <xsl:choose>
                <xsl:when test="$displayFormat = 'italics' ">
                    <i>
                    <!--<xsl:call-template name="insertBreaks">
                            <xsl:with-param name="pText" select="."/>
                        </xsl:call-template>-->
                        <xsl:if test="local-name(.)='translations_scope_note'">
                            <xsl:value-of select="@linkClass"/>
                            <xsl:value-of select="$translationsSeparator"/>
                            <xsl:text> </xsl:text>
                        </xsl:if>
                        <xsl:choose>
                            <xsl:when test="local-name(.)='scope_note' or local-name(.)='translations_scope_note' or local-name(.)='historical_note' or local-name(.)='source_note'" >
                                <xsl:value-of disable-output-escaping="yes" select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </i>
                </xsl:when>
                <xsl:when test="$displayFormat = 'bold' ">
                    <b>
                        <xsl:choose>
                            <xsl:when test="local-name(.)='scope_note' or local-name(.)='translations_scope_note' or local-name(.)='historical_note' or local-name(.)='source_note'" >
                                <xsl:value-of disable-output-escaping="yes" select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </b>
                </xsl:when>
                <xsl:when test="$displayFormat = 'translations' ">
                    <xsl:value-of select="@linkClass"/>
                    <xsl:value-of select="$translationsSeparator"/>
                    <xsl:text> </xsl:text>
                    
                    <xsl:value-of select="."/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- normal -->
                    <xsl:if test="local-name(.)='translations_scope_note'">
                        <xsl:value-of select="@linkClass"/>
                        <xsl:value-of select="$translationsSeparator"/>
                        <xsl:text> </xsl:text>
                    </xsl:if>  
                    <xsl:choose>                                          
                        <xsl:when test="local-name(.)='scope_note' or local-name(.)='translations_scope_note' or local-name(.)='historical_note' or local-name(.)='source_note'" >
                            <xsl:value-of disable-output-escaping="yes" select="."/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                        
                    
                </xsl:otherwise>
            </xsl:choose>
        </span>
    </xsl:template>
    <xsl:template match="text()" name="insertBreaks">
        <xsl:param name="pText" select="."/>
        <xsl:choose>
            <xsl:when test="not(contains($pText, '&#xA;'))">
                <xsl:copy-of select="$pText"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring-before($pText, '&#xA;')"/>
                <br />
                <xsl:call-template name="insertBreaks">
                    <xsl:with-param name="pText" select="substring-after($pText, '&#xA;')"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
