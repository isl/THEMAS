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

<!--
    Document   : SaveAll_Sources.xsl
    Created on : 30 Απρίλιος 2009, 4:57 μμ
    Author     : tzortzak
    Description:
        Purpose of transformation follows.
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
    <xsl:output method="html"/>
    <xsl:template match="/">
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
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
	<xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/saveallsources"/>
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="pageTitle">
            <xsl:value-of select="$localespecific/titleprefix/option[@lang=$lang]"/>
            <xsl:value-of select="//title"/>
        </xsl:variable>
        <html>
            <head>
                <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                </style>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <script type="text/javascript">
                    <xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/>
                </script>
            </head>
            <body style="background-color: #FFFFFF;" >
                <table width="100%">
                    <tr>
                        <td >
                            <xsl:value-of disable-output-escaping="yes" select="$localecommon/searchcriteria/option[@lang=$lang]"/>
                            <br/>
                            <xsl:value-of select="//query" />
                        </td>
                    
                        <td align="right">
                            <a href="#" class="SaveAsAndPrintLinks">
                                <xsl:attribute name="onclick">
                                    <xsl:text>saveAscode('SaveAs',null, '</xsl:text>
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
                <xsl:variable name="outputVar" select="//output"/>
                <table width="100%">
                    <tr width="100%">
                        <xsl:attribute name="style">
                                <xsl:text>background-color: </xsl:text>
                                <xsl:value-of select="$alternateRowsColor1"/> 
                                <xsl:text>;</xsl:text>
                                <!--<xsl:text>; text-align:center;</xsl:text>-->
                                <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                            </xsl:attribute>  
                        <xsl:choose>
                            <xsl:when test="count(//data/sources/source)=0">
                                <td align="left" valign="top" colspan="5">
                                    <strong>
                                        <xsl:value-of select="$localespecific/noresults/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td >
                                    <strong>
                                        <xsl:value-of select="$localespecific/number/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                                <td >
                                    <strong>
                                        <xsl:value-of select="$localespecific/source/option[@lang=$lang]"/>
                                    </strong>
                                </td>
                                <xsl:for-each select="$outputVar/node()">
                                    <td height="5">
                                        <strong>
                                            <xsl:choose>
                                                <xsl:when test="name() = 'source_note' ">
                                                    <xsl:value-of select="$localespecific/sourcenote/option[@lang=$lang]"/>
                                                </xsl:when>
                                                <xsl:when test="name() = 'primary_found_in' ">
                                                    <xsl:value-of select="$localespecific/primarysource/option[@lang=$lang]"/>
                                                </xsl:when>
                                                <xsl:when test="name() = 'translations_found_in' ">
                                                    <xsl:value-of select="$localespecific/trsource/option[@lang=$lang]"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="name()"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </strong>
                                    </td>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>
                    <tr>
                        <td valign="middle">
                            <xsl:attribute name="colspan">
                                <xsl:value-of select="count($outputVar/node())+2"/>
                            </xsl:attribute>
                            <hr/>
                        </td>
                    </tr>
                    <xsl:for-each select="//data/sources/source[./name !='']">
                        <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF" -->
                        <tr  valign="top">
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
                                <xsl:value-of select="position()"/>
                            </td>
                            <td>
                                <xsl:attribute name="id">
                                    <xsl:value-of select="./name/@id"/>
                                </xsl:attribute>
                                <xsl:value-of select="./name"/>
                            </td>
                            
                            <xsl:if test="count($outputVar/source_note)!=0">                                
                                <td>
                                    <a>
                                        <xsl:choose>
                                            <xsl:when test="count(./source_note[./text()!=''])!=0">
                                                <xsl:value-of disable-output-escaping="yes" select="./source_note"/>     
                                            </xsl:when>                     
                                            <xsl:otherwise>
                                                <xsl:text>-</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </a>
                                </td>
                                <!--<xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./source_note"/>
                                </xsl:call-template>   -->
                            </xsl:if>
                            <xsl:if test="count($outputVar/primary_found_in)!=0">
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./primary_found_in"/>
                                </xsl:call-template>                                
                            </xsl:if>
                            
                            <xsl:if test="count($outputVar/translations_found_in)!=0">
                                <xsl:call-template name="drawAttributeTd">
                                    <xsl:with-param name="nodeSet" select="./translations_found_in"/>
                                </xsl:call-template>                                
                            </xsl:if>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="drawAttributeTd">
        <xsl:param name="nodeSet"/>
        <td>
            <xsl:variable name="howmanyFields" select="count($nodeSet[node()!=''])"/>
            <!--<xsl:value-of select="$howmanyFields"/>-->
            <xsl:choose>
                <xsl:when test="$howmanyFields=0">-</xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="$nodeSet/node()">  
                        <xsl:choose>
                            <xsl:when test="../@id ">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:text>#</xsl:text>
                                        <xsl:value-of select="../@id"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <a>
                                    <xsl:value-of select="."/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="position()!= $howmanyFields">
                                <!--<xsl:text>, </xsl:text>-->
                                <br/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template> 
</xsl:stylesheet>
