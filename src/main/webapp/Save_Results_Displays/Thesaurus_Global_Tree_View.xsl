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
            <xsl:variable name="numOfIndent">0</xsl:variable>
            <xsl:variable name="pathToSaveScriptingAndLocale" select="//pathToSaveScriptingAndLocale"/>
            <xsl:variable name="localecommon" select="document($pathToSaveScriptingAndLocale)/root/common"/>
            <xsl:variable name="localespecific" select="document($pathToSaveScriptingAndLocale)/root/GlobalThesaurusTreeView"/>        
            <xsl:variable name="lang" select="page/@language"/>
            <xsl:variable name="pageTitle">
                <xsl:value-of select="$localespecific/titleprefix/option[@lang=$lang]"/>
                <xsl:value-of select="/page/targetThesaurus"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="/page/title"/>
            </xsl:variable>
            <html>
                <head>
                     <title>
                       <xsl:value-of select="$pageTitle"/>
                   </title>
                    <link rel="stylesheet" type="text/css" href="../../third-party-javascript/mktree/mktree.css"/>                    
                    <script language="JavaScript" type="text/javascript" src="../../third-party-javascript/mktree/mktree.js"></script>
                    
                   <script type="text/javascript"><xsl:value-of select="$localecommon/browserdetectionsaveasscript/option[@lang=$lang]"/></script>
                   
                   <style rel="stylesheet" type="text/css">
                       ul.mktree  li.liBullet  .bullet {background: none; }  
                       
                   </style>
                   <style rel="stylesheet" type="text/css">
                    td {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    .row {font-size: 12px; font-family: verdana, arial, helvetica, sans-serif; text-decoration:none; color:black;}
                    a.SaveAsAndPrintLinks { font-size: 11px; font-family: verdana, arial, helvetica, sans-serif; font-style:italic; 
                                           text-decoration:underline; color:black; }
                </style>
                   </head>
                   
                   <body style="background-color: #FFFFFF;">
                       <table width="100%">
                           
                           <tr>
                               <td class="criteriaInSaves">
                                   <!--<xsl:value-of disable-output-escaping="yes" select="$localecommon/searchcriteria/option[@lang=$lang]"/>
                                   <br/>
                                   -->
                                   <xsl:value-of select="$localespecific/baselabel/option[@lang=$lang]"/>
                                   <b>
                                       <xsl:value-of select="/page/targetThesaurus"/>
                                   </b>.
                               </td>
                               
                               <td align="right">
                                   <!-- Not Saving correctly and thus this cell is disabled
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
                                   -->
                               </td>
                           </tr>
                           
                           <tr>
                                <td colspan="2" align="left">
                                    <br/>
                                    <a>
                                        <xsl:attribute name="href"><xsl:value-of select="/page/query/typicalHierarchicalLocation"/></xsl:attribute>
                                        <xsl:value-of select="$localespecific/linktotypical/option[@lang=$lang]"/>
                                    </a>
                                    <br/>                                    
                                </td>
                            </tr>
                       </table>
                       
                       <xsl:choose>
                           
                           <xsl:when test="count(//topterm)=0 ">
                               <table>
                                   <tr>
                                       <td align="left" valign="top" colspan="5">
                                           <b>
                                           <xsl:value-of select="$localespecific/noterms/option[@lang=$lang]"/>        
                                           </b>
                                       </td>
                                   </tr>
                               </table>
                           </xsl:when>
                           
                           <xsl:otherwise>
                               	   <xsl:for-each select="//topterm">
				
				
				<br/>
                                        <ul class="mktree" id="tree1"  style="padding-left:0px; margin-left:0px;">
                                            <li class="liOpen" style="padding-left:0px; margin-left:0px;"> <span class="row"><b><xsl:value-of select="./name"/></b></span>
                                        <ul>
                                            <xsl:call-template name="list-nts">
                                                <xsl:with-param name="node" select="."/>
                                                <xsl:with-param name="howmany" select="$numOfIndent+1"/>
                                            </xsl:call-template>
                                        </ul>

                                    </li> 
                                </ul>
                                   </xsl:for-each>
                       
                   </xsl:otherwise>
               </xsl:choose>
           </body>
       </html>
   </xsl:template>
	
    <xsl:template name="list-nts">
        <xsl:param name="node"></xsl:param>
        <xsl:param name="howmany"></xsl:param>
    	
        <xsl:for-each select="$node[1]/nt">    
             <xsl:call-template name="draw-indent">
                <xsl:with-param name="i" >0</xsl:with-param>
                <xsl:with-param name="maxTimes" select="$howmany"/>
             </xsl:call-template>              
             <xsl:variable name="currentNode" select="."/>
             
             <li class="liOpen"> <span class="row">
                 <xsl:choose>
                     <xsl:when test="$howmany=1">
                         
                             <xsl:value-of select="."/>
                         
                     </xsl:when>
                     <xsl:otherwise>
                         <span style="font-style:italic;">
                            <xsl:value-of select="."/>
                        </span>
                     </xsl:otherwise>
                 </xsl:choose>
             </span>

             
                 <xsl:if test="count(//term[./name=$currentNode]/nt) >0 ">
                     <ul>
                         <xsl:call-template name="list-nts">
                             <xsl:with-param name="node" select="//term[./name=$currentNode]" />
                             <xsl:with-param name="howmany" select="$howmany+1" />
                         </xsl:call-template>
                     </ul>
                 </xsl:if>
             </li>
     		
        </xsl:for-each>   
         
    </xsl:template>
    
    
    
    <xsl:template name="draw-indent">
        <xsl:param name="i"></xsl:param>
        <xsl:param name="maxTimes"></xsl:param>

        <xsl:if test="$i &lt; $maxTimes">
    
            <xsl:call-template name="draw-indent">
                <xsl:with-param name="i" select="$i + 1"/>
                <xsl:with-param name="maxTimes" select="$maxTimes"/>
            </xsl:call-template>     
                
        </xsl:if>

    </xsl:template>


</xsl:stylesheet>







