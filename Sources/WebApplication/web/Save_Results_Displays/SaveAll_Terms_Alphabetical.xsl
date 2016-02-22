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
    <xsl:variable name="lang" select="//page/@language"/>
    <xsl:variable name="pathToLabels" select="//pathToLabels"/>
    <xsl:variable name="localeThes" select="document($pathToLabels)/locale/messages"/>
    <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
    <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
    <xsl:variable name="nameLang" select="concat('name_',$lang)"/>
    <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/savealltermsalphabetical"/>
    <xsl:variable name="pageTitle">
        <xsl:value-of select="$localespecific/titleprefix/option[@lang=$lang]"/>
        <xsl:value-of select="//title"/>
    </xsl:variable>
    <xsl:variable name="trSeperator" select="//data/@translationsSeperator"/>
    <xsl:variable name="onMouseOverColor">
            <!-- #D9EDFC -->
            <xsl:text>#D9EDFC</xsl:text>
        </xsl:variable>
        <xsl:variable name="alternateRowsColor1">
            <!-- #E2E2E2 -->
            <xsl:text>#E2E2E2</xsl:text>
        </xsl:variable>
        <xsl:variable name="alternateRowsColor2">
            <!-- #FFFFFF -->
            <xsl:text>#FFFFFF</xsl:text>
        </xsl:variable>
    <!-- _____________________________________________________________________________
              MAIN TEMPLATE
              FUNCTION: displays the data given in Alphabetical format for the tags <term>
    _____________________________________________________________________________ -->
    <xsl:template match="/page">
        <html>
            <head>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <!-- __________________ STYLES __________________ -->
                <style rel="stylesheet" type="text/css">
                    body { font-size:12px; font-family:  verdana, arial, helvetica, sans-serif;}
                    table {margin:0; padding:0;}
                    tr.rowThes {padding:0; }
                    a {text-decoration:none; color:black;}
                    a.aHeaderAnchorThes { text-decoration:none; color:black;}
                    span.headerThes { font-size: 12px; font-weight:bold;font-family:  verdana, arial, helvetica, sans-serif;}
                    span.headerThes_normal { font-size: 12px; font-family:  verdana, arial, helvetica, sans-serif;}
                    span.deweyHeaderThes {font-size: 12px; font-weight:bold; font-style:normal;}
                    span.typeThes {font-size: 12px; font-weight:normal; padding:0; }
                    span.valueThes {font-size: 12px; font-weight:normal;font-family: verdana, arial, helvetica, sans-serif;}
                    td.typeColThes {width:50px;padding:0;}
                    td.valueColThes {width:650px;padding:0;}
                    td.emptyFirstCellThes {width:20px;}
                    td.typeColSecondThes {width:60px;padding:0;}
                    td.valueColSecondThes {width:620px;padding:0;}
                    td.criteriaInSaves{font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                </style>
                <script type="text/javascript">
                    <xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/>
                </script>
            </head>
            <body style="background-color: #FFFFFF;" lang="el">
                <table width="100%">
                    <tr>
                        <td class="criteriaInSaves">
                            <xsl:value-of disable-output-escaping="yes" select="$localecommon/searchcriteria/option[@lang=$lang]"/>
                            <br/>
                            <xsl:choose>
                                <xsl:when test="//query/base">
                                    <!-- <xsl:value-of select="//query/base"/> --> <!-- Αλφαβητική παρουσίαση όρων της ιεραρχίας: -->
                                    <xsl:value-of select="$localespecific/baselabel/option[@lang=$lang]"/>
                                    <b>
                                        <xsl:value-of select="//query/arg1"/>
                                    </b>.</xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="//query"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td align="right" class="criteriaPlusLinks">
                            <a href="#" class="SaveAsAndPrintLinks">
                                <xsl:attribute name="onclick">
                                    <xsl:text>saveAscode('SaveAs',null,'</xsl:text>
                                    <xsl:value-of select="$pageTitle"/>
                                    <xsl:text>');</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="$localecommon/saveas/option[@lang=$lang]"/>
                            </a>
                                                        &#160;
                            <a href="#" class="SaveAsAndPrintLinks" onclick="print()">
                                <xsl:value-of select="$localecommon/print/option[@lang=$lang]"/>
                            </a>
                        </td>
                    </tr>
                </table>
                <xsl:choose>
                    <xsl:when test="count(//term) + count(//ufterm) =0">
                        <table>
                            <tr>
                                <td align="left" valign="top" colspan="5">
                                    <strong>
                                        <xsl:value-of select="$localespecific/noterms/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </tr>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- __________________ FOR EACH <term> or <ufterm> (BEGIN) ____________________ -->
                        <xsl:variable name="allIds" select="//data/terms/term/descriptor/@id"/>

                        <br/>

                        <xsl:for-each select="//terms/term | //terms/ufterm">
                            
                            <table  width="100%" cellspacing="0" cellpadding="0">
                                <tr>
                                    <xsl:attribute name="onMouseOver">
                                        <xsl:text>this.bgColor = '</xsl:text>
                                        <xsl:value-of select="$onMouseOverColor"/> 
                                        <xsl:text>';</xsl:text>
                                    </xsl:attribute>
                                    <xsl:choose>
                                        <xsl:when test="position() mod 2 =0">
                                            <xsl:attribute name="bgcolor">
                                                <xsl:value-of select="$alternateRowsColor1"/>                                         
                                            </xsl:attribute>   
                                            <xsl:attribute name="onMouseOut">
                                                <xsl:text>this.bgColor = '</xsl:text>
                                                <xsl:value-of select="$alternateRowsColor1"/> 
                                                <xsl:text>';</xsl:text>
                                            </xsl:attribute>                                                 
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="bgColor">
                                                <xsl:value-of select="$alternateRowsColor2"/>                                         
                                            </xsl:attribute>               
                                            <xsl:attribute name="onMouseOut">
                                                <xsl:text>this.bgColor = '</xsl:text>
                                                <xsl:value-of select="$alternateRowsColor2"/> 
                                                <xsl:text>';</xsl:text>
                                            </xsl:attribute>                                    
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <td>
                                      
                                   
                            <table width="800" cellspacing="0" cellpadding="0">
                                
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
                                                <span class="deweyHeaderThes">&#160;--&#160;(<xsl:value-of select="."/>)
                                                </span>
                                            </xsl:for-each>
                                        </xsl:if>
                                        <br/>
                                    </td>
                                </tr>
                            </table>
                            <!-- __________________ translations of target (<translations>) ______________ -->
                            <xsl:if test="count(./translations[text()!='']) !=0">
                                <xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="./translations"/>
                                    <xsl:with-param name="tagDisplayNameId">TR</xsl:with-param>
                                    <xsl:with-param name="addAnchors">false</xsl:with-param>
                                    <xsl:with-param name="displayFormat">translations</xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                            <!-- __________________ ΔΣ of target (<scope_note> - ΔΣ) ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="scope_note"/>
                                <xsl:with-param name="tagDisplayNameId">SN</xsl:with-param>
                                <xsl:with-param name="addAnchors">false</xsl:with-param>
                                <!--<xsl:with-param name="displayFormat">italics</xsl:with-param>-->
                                <xsl:with-param name="displayFormat">none</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________ SN of target (<translations_scope_note> - SN) ______________ -->
                            <xsl:if test="count(translations_scope_note[text()!='']) >0">
                                <xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="translations_scope_note"/>
                                    <xsl:with-param name="tagDisplayNameId" select="'tr_SN'"/>
                                    <xsl:with-param name="addAnchors" select="'false'"/>
                                    <!--<xsl:with-param name="displayFormat">italics</xsl:with-param>-->
                                <xsl:with-param name="displayFormat">none</xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                            <!--<xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="translations_scope_note"/>
                                    <xsl:with-param name="tagDisplayNameId">tr_SN</xsl:with-param>
                                    <xsl:with-param name="addAnchors">false</xsl:with-param>
                                    <xsl:with-param name="displayFormat">italics</xsl:with-param>
                            </xsl:call-template>-->
                            <!-- __________________ OK of target (<topterm> - OK) ____________________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="topterm"/>
                                <xsl:with-param name="tagDisplayNameId">TT</xsl:with-param>
                                <xsl:with-param name="addAnchors">true</xsl:with-param>
                                <xsl:with-param name="displayFormat">normal</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________UFs pointing of target (XA - <uf>) ______________ -->
                            <xsl:if test="count(./uf[text()!='']) !=0">
                                <xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="uf"/>
                                    <xsl:with-param name="tagDisplayNameId">UF</xsl:with-param>
                                    <xsl:with-param name="addAnchors">true</xsl:with-param>
                                    <xsl:with-param name="displayFormat">normal</xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                            <!-- __________________uf_translations pointing of target (UF - <uf_translations>) ______________ -->
                            <xsl:if test="count(./uf_translations[text()!='']) !=0">
                                <xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="./uf_translations"/>
                                    <xsl:with-param name="tagDisplayNameId">UF (Tra.)</xsl:with-param>
                                    <xsl:with-param name="addAnchors">false</xsl:with-param>
                                    <xsl:with-param name="displayFormat">translations</xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                            <!-- __________________BTs of target (<bt> - ΠΟ1 + ΠΟ2) (π.χ. αγροτική ανάπτυξη) ______________ -->
                            <!-- __________________if BTs of target exist______________ -->
                            <xsl:if test="bt[text()!='']">
                                <table width="800" cellspacing="0" cellpadding="0" border="0">
                                    <!-- __________________for each BT of target BEGIN______________ -->
                                    <xsl:for-each select="bt">

                                        <tr class="rowThes">
                                            <td class="typeColThes" valign="top">
                                                <span class="typeThes">
                                                    <xsl:value-of select="$localeThes/message[@id='BT1']/@*[name() = $nameLang]"/>
                                                </span>
                                            </td>
                                            <td class="valueColThes">
                                                <a>
                                                    <xsl:if test="./@id">
                                                        <xsl:attribute name="href">
                                                            <xsl:value-of select="concat('#',./@id)"/>
                                                        </xsl:attribute>
                                                    </xsl:if>
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
                                                <!-- <xsl:for-each select="//terms/term/descriptor[./@id=$currentBT1sysid]"> -->
                                                <table width="800" cellspacing="0" cellpadding="0" class="secondLevelTableThes" border="0">
                                                    <!-- <xsl:for-each select="../bt">-->
                                                    <!--    <xsl:for-each select="$allIds">
                                                        <xsl:value-of select="."/>
                                                        <xsl:value-of select=".."/>
                                                    </xsl:for-each>
                                                    -->
                                                    <xsl:for-each select="$allIds[.=$currentBT1sysid]/../../bt">
                                                        <xsl:if test=".!=''">
                                                            <tr class="rowThes">
                                                                <td class="emptyFirstCellThes" valign="top"/>
                                                                <td class="typeColSecondThes" valign="top">
                                                                    <span class="typeThes">
                                                                        <xsl:value-of select="$localeThes/message[@id='BT2']/@*[name() = $nameLang]"/>
                                                                    </span>
                                                                </td>
                                                                <td class="valueColSecondThes">
                                                                    <a>
                                                                        <xsl:if test="./@id">
                                                                            <xsl:attribute name="href">
                                                                                <xsl:value-of select="concat('#',./@id)"/>
                                                                            </xsl:attribute>
                                                                        </xsl:if>
                                                                        <span class="valueThes">
                                                                            <xsl:value-of select="."/>
                                                                        </span>
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                </table>
                                                <!--</xsl:for-each>-->
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                    <!-- __________________for each BT of target END______________ -->
                                </table>
                            </xsl:if>
                            <!-- __________________NTs of target (<bt> - EΟ1 + EΟ2) (π.χ. αγροτική παραγωγή - γεωργία) ______________ -->
                            <!-- __________________if NTs of target exist______________ -->
                            <xsl:if test="nt[text()!='']">
                                <table width="800" cellspacing="0" cellpadding="0" border="0">
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
                                                <xsl:if test="(./@linkClass!='') and ((position()=1) or (preceding-sibling::*/../nt[$prevPos]/@linkClass!=./@linkClass))">
                                                    <span class="valueThes">
                                                        <b>
                                                            <i>
                                                                <xsl:text>&lt;</xsl:text>
                                                                <xsl:value-of select="./@linkClass"/>
                                                                <xsl:text>&gt;</xsl:text>
                                                            </i>
                                                        </b>
                                                    </span>
                                                    <br/>
                                                </xsl:if>
                                                <a>
                                                    <xsl:if test="./@id">
                                                        <xsl:attribute name="href">
                                                            <xsl:value-of select="concat('#',./@id)"/>
                                                        </xsl:attribute>
                                                    </xsl:if>
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
                                                <!-- get the NT term of xsl:variable "currentNT1sysid"
                                                <xsl:for-each select="//terms/term[./descriptor/@id=$currentNT1sysid]">-->
                                                <table width="800" cellspacing="0" cellpadding="0" class="secondLevelTableThes" border="0">
                                                    <xsl:for-each select="$allIds[.=$currentNT1sysid]/../../nt">
                                                        <!--<xsl:for-each select="nt">-->
                                                        <xsl:if test=".!=''">
                                                            <tr class="rowThes">
                                                                <td class="emptyFirstCellThes" valign="top"/>
                                                                <td class="typeColSecondThes" valign="top">
                                                                    <span class="typeThes">
                                                                        <xsl:value-of select="$localeThes/message[@id='NT2']/@*[name() = $nameLang]"/>
                                                                    </span>
                                                                </td>
                                                                <td class="valueColSecondThes">
                                                                    <a>
                                                                        <xsl:if test="./@id">
                                                                            <xsl:attribute name="href">
                                                                                <xsl:value-of select="concat('#',./@id)"/>
                                                                            </xsl:attribute>
                                                                        </xsl:if>
                                                                        <span class="valueThes">
                                                                            <xsl:value-of select="."/>
                                                                        </span>
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                </table>
                                                <!--</xsl:for-each>-->
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                    <!-- __________________for each NT of target END______________ -->
                                </table>
                            </xsl:if>
                            <!-- __________________ RTs of target (<rt> - ΣΟ) - BOTH directions ______________ -->
                            <xsl:if test="count(./rt[./text()!='']) !=0">
                                <xsl:call-template name="DisplayTargetTag">
                                    <xsl:with-param name="tagNode" select="rt"/>
                                    <xsl:with-param name="tagDisplayNameId">RT</xsl:with-param>
                                    <xsl:with-param name="addAnchors">true</xsl:with-param>
                                    <xsl:with-param name="displayFormat">normal</xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                            <!-- __________________Sources of target (<found_in> - Πηγή) ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="found_in"/>
                                <xsl:with-param name="tagDisplayNameId">Source</xsl:with-param>
                                <xsl:with-param name="addAnchors">false</xsl:with-param>
                                <xsl:with-param name="displayFormat">normal</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________ Primary Sources of target (<primary_found_in> - Πηγή (ελλ.)) ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="primary_found_in"/>
                                <xsl:with-param name="tagDisplayNameId">Source</xsl:with-param>
                                <xsl:with-param name="addAnchors">false</xsl:with-param>
                                <xsl:with-param name="displayFormat">normal</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________ Translation Sources of target (<translations_found_in> - Πηγή (αγγλ.)) ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="translations_found_in"/>
                                <xsl:with-param name="tagDisplayNameId">Source_tr</xsl:with-param>
                                <xsl:with-param name="addAnchors">false</xsl:with-param>
                                <xsl:with-param name="displayFormat">normal</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________ <use> tags of target of type <ufterm> (ΧΡ) ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="use"/>
                                <xsl:with-param name="tagDisplayNameId">USE</xsl:with-param>
                                <xsl:with-param name="addAnchors">true</xsl:with-param>
                                <xsl:with-param name="displayFormat">bold</xsl:with-param>
                            </xsl:call-template>                            
                            <!-- __________________ <created_by> ______________ -->
                            <xsl:call-template name="DisplayTargetTag">
                                <xsl:with-param name="tagNode" select="created_by"/>
                                <xsl:with-param name="tagDisplayNameId">CREATED_BY</xsl:with-param>
                                <xsl:with-param name="addAnchors">false</xsl:with-param>
                                <xsl:with-param name="displayFormat">normal</xsl:with-param>
                            </xsl:call-template>
                            <!-- __________________ FOR EACH <term> or <ufterm> (END) ____________________ -->
                            <br/>
                             </td>
                                </tr>
                            </table>
                            
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </body>
        </html>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: DisplayTargetTag
              INPUT: - tagNode: the XML Node list of the xml tag to be displayed (g.e. to_en, scope_note, translations_scope_note, topterm, uf, found_in, primary_found_in, translations_found_in, use)
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
        <!-- in case there is at least one tagNode -->
        <xsl:if test="$tagNode">
            <table width="800" cellspacing="0" cellpadding="0">
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
                                    <!--<xsl:choose>
                                    <xsl:when test="./id">-->
                                    <a>
                                        <xsl:if test="./@id">
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="concat('#',./@id)"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:call-template name="DisplayCurrentTagValue">
                                            <xsl:with-param name="displayFormat">
                                                <xsl:value-of select="$displayFormat"/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </a>
                                    <!--</xsl:when>
                                        <xsl:otherwise>
                                            <a >
                                                <xsl:call-template name="DisplayCurrentTagValue">
                                                        <xsl:with-param name="displayFormat">
                                                                <xsl:value-of select="$displayFormat"/>
                                                        </xsl:with-param>
                                                </xsl:call-template>
                                            </a>
                                        </xsl:otherwise>
                                    </xsl:choose>-->

                                </xsl:when>
                                <!-- __________________ without anchor __________________ -->
                                <xsl:otherwise>
                                    <a>
                                        <xsl:choose>          
                                        <xsl:when  test="$tagDisplayNameId = 'SN'">
                                            <!--<span width="400" style="WORD-BREAK:BREAK-ALL;">-->
                                            <span>                                                
                                                <xsl:value-of disable-output-escaping="yes" select="."/>
                                            </span>
                                        </xsl:when>                                        
                                        <xsl:otherwise>                           
                                        <xsl:call-template name="DisplayCurrentTagValue">
                                            <xsl:with-param name="displayFormat">
                                                <xsl:value-of select="$displayFormat"/>
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
        <span class="valueThes">
            <xsl:choose>
                <xsl:when test="$displayFormat = 'italics' ">
                    <i>
                        <xsl:if test="local-name(.) = 'translations_scope_note' ">
                            <xsl:value-of select="@linkClass"/>
                            <xsl:value-of select="$trSeperator"/>
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
                        <xsl:value-of select="."/>
                    </b>
                </xsl:when>
                <xsl:when test="$displayFormat = 'translations' ">
                    <xsl:value-of select="@linkClass"/>
                    <xsl:value-of select="$trSeperator"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="."/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- normal -->
                    <xsl:if test="local-name(.)='translations_scope_note'">
                        <xsl:value-of select="@linkClass"/>
                        <xsl:value-of select="$trSeperator"/>
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
</xsl:stylesheet>
