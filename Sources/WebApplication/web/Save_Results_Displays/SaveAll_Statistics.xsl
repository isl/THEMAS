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
    <xsl:output method="html"/>
    <xsl:template match="/">
        
        <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
        <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
        <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/statistics"/>
        <xsl:variable name="lang" select="page/@language"/>
        <xsl:variable name="pageTitle" select="//windowTitle"/>
        <html>
            <head>
                <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; text-decoration:underline; color:black; }
                    fieldset { text-align:left; border: 1px solid  #666666;}
                    fieldset.index{float: inherit; position:relative; top:68px; width: 35em; text-align:center; }
                    fieldset.links { float: inherit; width: 61em }
                    legend {background: #FFFFFF; border: 1px solid  #FFFFFF; font-weight:bold; padding: 1px 6px; font-size: 12px;  font-family: verdana, arial, helvetica, sans-serif;}
                </style>
                <title>
                    <xsl:value-of select="$pageTitle"/>
                </title>
                <script type="text/javascript">
                    <xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/>
                </script>
            </head>
            <body style="background-color: #FFFFFF;">
                <table width="100%">
                    <tr>
                        <td>
                            <b>
                                <xsl:value-of select="//title"/>
                            </b>
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
                <hr/>
                <xsl:choose>
                    <xsl:when test="count(//results/StatisticsOfTerms)!=0">
                        <xsl:call-template name="StatisticsOfTerms">
                            <xsl:with-param name="localespecific" select="$localespecific/terms"/>
                            <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(//results/StatisticsOfHierarchies)!=0">
                        <xsl:call-template name="StatisticsOfHierarchies">
                            <xsl:with-param name="localespecific" select="$localespecific/hierarchies"/>
                            <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(//results/StatisticsOfFacets)!=0">
                        <xsl:call-template name="StatisticsOfFacets">
                            <xsl:with-param name="localespecific" select="$localespecific/facets"/>
                            <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(//results/StatisticsOfSources)!=0">
                        <xsl:call-template name="StatisticsOfSources">
                            <xsl:with-param name="localespecific" select="$localespecific/sources"/>
                            <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(//results/StatisticsOfUsers)!=0">
                        <xsl:call-template name="StatisticsOfUsers">
                            <xsl:with-param name="localespecific" select="$localespecific/users"/>
                            <xsl:with-param name="lang" select="$lang"/>
                        </xsl:call-template>
                    </xsl:when>
                </xsl:choose>
            </body>
        </html>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: StatisticsOfTerms
              FUNCTION: displays the statistics for terms
    _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfTerms">
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:value-of select="$localespecific/legendprefix/option[@lang=$lang]"/>
                <xsl:value-of select="//selectedThesaurus"/>
            </legend>
            <br/>
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
                            <xsl:value-of select="$localespecific/description/option[@lang=$lang]"/>
                        </strong>
                    </td>
                    <td height="5">
                        <strong>
                            <xsl:value-of select="$localespecific/number/option[@lang=$lang]"/>
                        </strong>
                    </td>
                </tr>
                
                <tr  class="resultRow" valign="top">
                    <xsl:attribute name="onMouseOver">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$onMouseOverColor"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="bgColor">
                        <xsl:value-of select="$alternateRowsColor2"/>                                         
                    </xsl:attribute>               
                    <xsl:attribute name="onMouseOut">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$alternateRowsColor2"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute>                                    
                                
                    <td class="resultRow">
                        <xsl:value-of select="$localespecific/totalterms/option[@lang=$lang]"/>
                    </td>
                    <td class="resultRow">
                        <xsl:value-of select="//StatisticsOfTerms/total"/>
                    </td>
                </tr>
                <tr class="resultRow" valign="top">
                    <xsl:attribute name="onMouseOver">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$onMouseOverColor"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="bgColor">
                        <xsl:value-of select="$alternateRowsColor1"/>                                         
                    </xsl:attribute>               
                    <xsl:attribute name="onMouseOut">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$alternateRowsColor1"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute> 
                    <td class="resultRow">
                        <xsl:value-of select="$localespecific/totalufterms/option[@lang=$lang]"/>
                    </td>
                    <td class="resultRow">
                        <xsl:value-of select="//StatisticsOfTerms/total_XA"/>
                    </td>
                </tr>
                <tr class="resultRow" valign="top">
                    <xsl:attribute name="onMouseOver">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$onMouseOverColor"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="bgColor">
                        <xsl:value-of select="$alternateRowsColor2"/>                                         
                    </xsl:attribute>               
                    <xsl:attribute name="onMouseOut">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$alternateRowsColor2"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute> 
                    <td class="resultRow">
                        <xsl:value-of select="$localespecific/totaltranslationterms/option[@lang=$lang]"/>
                    </td>
                    <td class="resultRow">
                        <xsl:value-of select="//StatisticsOfTerms/total_PrefferedTranslations"/>
                    </td>
                </tr>
                <tr class="resultRow" valign="top">
                    <xsl:attribute name="onMouseOver">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$onMouseOverColor"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="bgColor">
                        <xsl:value-of select="$alternateRowsColor1"/>                                         
                    </xsl:attribute>               
                    <xsl:attribute name="onMouseOut">
                        <xsl:text>this.bgColor = '</xsl:text>
                        <xsl:value-of select="$alternateRowsColor1"/> 
                        <xsl:text>';</xsl:text>
                    </xsl:attribute> 
                    <td class="resultRow">
                        <xsl:value-of select="$localespecific/totaluftranslationterms/option[@lang=$lang]"/>
                    </td>
                    <td class="resultRow">
                        <xsl:value-of select="//StatisticsOfTerms/total_NonPrefferedTranslations"/>
                    </td>
                </tr>
            </table>
        </fieldset>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: StatisticsOfHierarchies
              FUNCTION: displays the statistics for hierarchies
    _____________________________________________________________________________ -->
    <xsl:template name="StatisticsOfHierarchies">
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:value-of select="$localespecific/legendprefix/option[@lang=$lang]"/>
                <xsl:value-of select="//selectedThesaurus"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td>
                        <xsl:value-of select="$localespecific/totalhiers/option[@lang=$lang]"/>
                        <xsl:value-of select="//StatisticsOfHierarchies/total"/>
                    </td>
                </tr>
            </table>
            <br/>
            <!-- Αριθμός όρων και ΑΟ ανά ιεραρχία -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$localespecific/subfieldlegend/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$localespecific/hierarchy/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/terms/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/ufs/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/translations/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/uf_translations/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//StatisticsOfHierarchies/NumberOfTermsAndTranslationsPerHierarchy/hierarchy">
                        <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"  -->
                        <tr class="resultRow" valign="top">
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
                            <td class="resultRow">
                                <xsl:value-of select="./name"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTerms"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTranslations"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
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
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:value-of select="$localespecific/legendprefix/option[@lang=$lang]"/>
                <xsl:value-of select="//selectedThesaurus"/>
            </legend>
            <br/>
            <!-- Συνολικός αριθμός μικροθησαυρών -->
            <table>
                <tr>
                    <td>
                        <xsl:value-of select="$localespecific/totalfacets/option[@lang=$lang]"/>
                        <xsl:value-of select="//StatisticsOfFacets/total"/>
                    </td>
                </tr>
            </table>
            <br/>
            <!-- Αριθμός ιεραρχιών, όρων και Μεταφράσεων ανά μικροθησαυρό -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$localespecific/subfieldlegend/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$localespecific/facet/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/hierarchies/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/terms/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/ufs/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/translations/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/uf_translations/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//StatisticsOfFacets/NumberOfHierarchiesAndTermsAndTranslationsPerFacet/facet">
                        <!--  onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF" -->
                        <tr class="resultRow" valign="top">
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
                            <td class="resultRow">
                                <xsl:value-of select="./name"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfHierarchies"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTerms"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfNonPrefferedTerms"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTranslations"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfNonPrefferedTranslations"/>
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
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:value-of select="$localespecific/legendprefix/option[@lang=$lang]"/>
                <xsl:value-of select="//selectedThesaurus"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td>
                        <xsl:value-of select="$localespecific/totalsources/option[@lang=$lang]"/>
                        <xsl:value-of select="//StatisticsOfSources/total"/>
                    </td>
                </tr>
            </table>
            <br/>
            <!-- Αριθμός όρων ανά πηγή (ελληνική/αγγλική) -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$localespecific/subfieldlegend/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$localespecific/source/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/primarysource/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/translationssource/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//NumberOfTermsPerTranslationsSource/source">
                        <!--  onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"-->
                        <tr class="resultRow" valign="top">
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
                            <td class="resultRow">
                                <xsl:value-of select="./name"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTermsPerTermSource"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTermsPerTranslationSource"/>
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
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:value-of select="$localespecific/legendprefix/option[@lang=$lang]"/>
                <xsl:value-of select="//selectedThesaurus"/>
            </legend>
            <br/>
            <table>
                <tr>
                    <td>
                        <xsl:value-of select="$localespecific/totalusers/option[@lang=$lang]"/>
                        <xsl:value-of select="//StatisticsOfUsers/total"/>
                    </td>
                </tr>
            </table>
            <br/>
            <!-- Αριθμός όρων δημιουργία και τελευταίας τροποποίησης ανά χρήστη -->
            <fieldset>
                <legend>
                    <xsl:value-of select="$localespecific/subfieldlegend/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$localespecific/user/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/created/option[@lang=$lang]"/>
                            </strong>
                        </td>
                        <td height="5">
                            <strong>
                                <xsl:value-of select="$localespecific/lastmodified/option[@lang=$lang]"/>
                            </strong>
                        </td>
                    </tr>
                    <xsl:for-each select="//StatisticsOfUsers/NumberOfTermsCreatedAndLastModifiedPerUser/user">
                        <!-- onMouseOver="this.bgColor = '#F2F2F2'" onMouseOut="this.bgColor = '#FFFFFF'" bgcolor="#FFFFFF"  -->
                        <tr class="resultRow" valign="top">
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
                            <td class="resultRow">
                                <xsl:value-of select="./name"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTermsCreated"/>
                            </td>
                            <td class="resultRow">
                                <xsl:value-of select="./NumberOfTermsLastModified"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </fieldset>
    </xsl:template>
</xsl:stylesheet>
