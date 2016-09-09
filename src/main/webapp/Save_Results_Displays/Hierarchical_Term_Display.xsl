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
    Document   : Hierarchical_Term_Display.xsl
    Created on : 20 Μάϊος 2009, 1:40 μμ
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
    <xsl:template match="/" >        
    <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
    <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
    <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/termhierarchical"/>
    <xsl:variable name="lang" select="page/@language"/>
    <xsl:variable name="pageTitle">
        <xsl:value-of select="$localespecific/titleprefix/option[@lang=$lang]"/>
        <xsl:value-of select="//title"/>
    </xsl:variable>
    <html>
            <head>
                
                 <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    .row {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic;  text-decoration:underline; color:black; }
                </style>
                <script language="javascript" type="text/javascript">
                    <xsl:text>var counter=0;</xsl:text>
                </script>
                 <title>
                     <xsl:value-of select="$pageTitle"/>
                </title>
                <script type="text/javascript"><xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/></script>
            </head>
            <body style="background-color: #FFFFFF;" >
             
            <table width="100%"> 
                <tr>
                    <td class="criteriaInSaves">
                        <xsl:value-of disable-output-escaping="yes" select="$localecommon/searchcriteria/option[@lang=$lang]"/>
                        <br/>
                        <xsl:value-of select="$localespecific/baselabel/option[@lang=$lang]"/>
                        <!--<xsl:value-of select="//query/base" />Ιεραρχική παρουσίαση όρου: -->
                        <b>
                            <xsl:value-of select="//query/arg1"/>
                        </b>
                        <xsl:text>.</xsl:text>
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
             
             <xsl:choose>
                <xsl:when test="count(//hierarchy/topterm)=0 ">
                    <table><tr>
                    <td align="left" valign="top" colspan="5">
                        <strong>
                            <xsl:value-of select="$localespecific/notopterms/option[@lang=$lang]"/>                            
                        </strong>
                    </td>
                    </tr></table>
            </xsl:when>
            <xsl:otherwise>
                <br/>
                <table >
                    <tr >
                            <td colspan="3" style="font-size: 11px;">
                                <xsl:value-of select="$localespecific/refs/option[@lang=$lang]"/>
                            </td>
                        </tr>
                    <xsl:variable name="targetTerm" select="//targetTerm"/>
                    <xsl:for-each select="//hierarchy">
                        
                        <tr>
                            <td style="font-size: 11px;">
                                <xsl:choose>
                                    <xsl:when test="./@hierRefs=1">
                                        <xsl:value-of select="$localespecific/oneref/option[@lang=$lang]"/>
                                        <xsl:value-of select="./topterm/name"/>         
                                        <xsl:text>.</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./@hierRefs "/>
                                        <xsl:value-of select="$localespecific/multiplerefs/option[@lang=$lang]"/>
                                        <xsl:value-of select="./topterm/name"/>  
                                        <xsl:text>. </xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td width="10"></td>
                            <td style="font-size: 11px;">
                                <i>
                                    <xsl:value-of select="$localespecific/movetoref/option[@lang=$lang]"/>
                                    <xsl:variable name="startForLoop" >1</xsl:variable>
                                    <xsl:call-template name="draw-refs">
                                        <xsl:with-param name="numOfRefs" select="./@hierRefs"/>
                                        <xsl:with-param name="startIndex" select="./@startIndex"/>
                                        <xsl:with-param name="i" select="$startForLoop"/>
                                    </xsl:call-template>
                                </i>
                            </td>
                        </tr>   
                    </xsl:for-each>
                </table>
                
                <xsl:variable name="howmanyHiers" select="count(//hierarchy)"/>
                
                <xsl:for-each select="//hierarchy">
                    <xsl:variable name="numOfIndent">0</xsl:variable> 
                    
                    <br/>

                    <strong ><span class="row"><xsl:value-of select="./topterm/name"/></span></strong>
                    <xsl:call-template name="list-nts">
                        <xsl:with-param name="node" select="./topterm"/>
                        <xsl:with-param name="howmany" select="$numOfIndent+1" ></xsl:with-param> 
                        <xsl:with-param name="localespecific" select="$localespecific"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                   <xsl:if test="position() != $howmanyHiers">
                        <br/>
                        <br/>
                        <hr/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:otherwise>
            </xsl:choose>
            </body>
        </html>
    </xsl:template>
    
     <xsl:template name="list-nts">
        <xsl:param name="node"/>
        <xsl:param name="howmany"/>
        <xsl:param name="localespecific"/>
        <xsl:param name="lang"/>
        
        <xsl:variable name="targetTerm" select="//targetTerm"/>
        <xsl:for-each select="$node[1]/nt">
            <!-- <xsl:sort select="."/> -->
            <xsl:variable name="currentNode" select="."/>
            <span class="row" ><br/> 
            <!--style="background-color:#E8E9BE">-->
                 <xsl:choose>
                     <xsl:when test="$currentNode = $targetTerm">
                         <span style="background-color:#E2E2E2">
                             
                         
                        <xsl:call-template name="draw-indent">
                            <xsl:with-param name="i" >0</xsl:with-param>
                            <xsl:with-param name="maxTimes" select="$howmany"/>
                         </xsl:call-template>
                         <a>
                             <b><xsl:value-of select="."/></b>
                         </a>
                         <script language="javascript" type="text/javascript">
                            <xsl:text>counter ++;</xsl:text>
                            <xsl:text>document.write('&lt;a id="');</xsl:text>
                            <xsl:text>document.write(counter);</xsl:text>
                            <xsl:text>document.write('"&gt;');</xsl:text>
                            <xsl:text>document.write('&lt;/a&gt;');</xsl:text>
                        </script> 
    &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
    &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<a href="#"  class="SaveAsAndPrintLinks"><xsl:value-of select="$localespecific/returntopagestart/option[@lang=$lang]"/></a>
                     </span>
                     </xsl:when>
                     <xsl:otherwise>   
                            <xsl:call-template name="draw-indent">
                                <xsl:with-param name="i" >0</xsl:with-param>
                                <xsl:with-param name="maxTimes" select="$howmany"/>
                             </xsl:call-template>
                             <xsl:value-of select="."/>
                     </xsl:otherwise>
                 </xsl:choose>
             </span>
             <xsl:if test="count(../../term[./name=$currentNode]/nt) >0 ">
                 <xsl:call-template name="list-nts">
                    <xsl:with-param name="node" select="../../term[./name=$currentNode]" />
                    <xsl:with-param name="howmany" select="$howmany+1" />
                    <xsl:with-param name="localespecific" select="$localespecific"/>
                    <xsl:with-param name="lang" select="$lang"/>
                 </xsl:call-template>
             </xsl:if>             
        </xsl:for-each>      
    </xsl:template>
    
    <xsl:template name="draw-indent">
        <xsl:param name="i"/>
        <xsl:param name="maxTimes"/>
        <xsl:variable name="tabModule">--&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:variable>
        <xsl:if test="$i &lt; $maxTimes">
            <xsl:value-of select="$tabModule"/>
            
            <xsl:call-template name="draw-indent">
                <xsl:with-param name="i" select="$i + 1"/>
                <xsl:with-param name="maxTimes" select="$maxTimes"/>
            </xsl:call-template>     
        </xsl:if>
            
    </xsl:template>
    
    <xsl:template name="draw-refs">
        <xsl:param name="numOfRefs"/>
        <xsl:param name="startIndex"/>
        <xsl:param name="i"/>
        
        <xsl:if test="not ($i &gt; $numOfRefs) ">
            
            <xsl:if test="($i &gt; 1 ) and ($i &lt; $numOfRefs )">
                <xsl:text>, </xsl:text>
            </xsl:if>
            &#160;&#160;
            <a class="SaveAsAndPrintLinks">
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text><xsl:value-of select="$i+$startIndex -1"/>
                </xsl:attribute>
                <!--<xsl:text>Αναφορά </xsl:text>--><xsl:value-of select="$i"/>
            </a>
            <xsl:call-template name="draw-refs">
                <xsl:with-param name="numOfRefs" select="$numOfRefs"/>
                <xsl:with-param name="startIndex" select="$startIndex"/>
                <xsl:with-param name="i" select="$i+1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    

</xsl:stylesheet>