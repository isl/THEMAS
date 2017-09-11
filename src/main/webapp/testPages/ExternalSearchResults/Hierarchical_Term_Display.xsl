<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/" > 
		<xsl:variable name="lang" select="page/@language"/>
    <!--
                
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
    -->         
            <xsl:choose>
                <xsl:when test="count(//hierarchy/topterm)=0 ">
                    <table><tr>
                    <td align="left" valign="top" colspan="5">
                        <strong>
                            <xsl:text>No participation in hierarchies.</xsl:text>
                        </strong>
                    </td>
                    </tr></table>
            </xsl:when>
            <xsl:otherwise>
                <br/>
                <table >
                    <tr >
						<td colspan="3" style="font-size: 11px;">
							<!--<xsl:value-of select="$localespecific/refs/option[@lang=$lang]"/>-->
							<xsl:text>References:</xsl:text>
						</td>
					</tr>
                    <xsl:variable name="targetTerm" select="//targetTerm"/>
                    <xsl:for-each select="//hierarchy">
                        
                        <tr>
                            <td style="font-size: 11px;">
                                <xsl:choose>
                                    <xsl:when test="./@hierRefs=1">
                                        <!--<xsl:value-of select="$localespecific/oneref/option[@lang=$lang]"/>-->
										<xsl:text>1 reference in hierarchy: </xsl:text>
                                        <xsl:value-of select="./topterm/name"/>         
                                        <xsl:text>.</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./@hierRefs "/>
                                        <xsl:text> references in hierarchy: </xsl:text>
                                        <xsl:value-of select="./topterm/name"/>  
                                        <xsl:text>. </xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td width="10"></td>
                            <!--<td style="font-size: 11px;">
                                <i>
                                    
									<xsl:text> Move to reference: </xsl:text>
                                    <xsl:variable name="startForLoop" >1</xsl:variable>
                                    <xsl:call-template name="draw-refs">
                                        <xsl:with-param name="numOfRefs" select="./@hierRefs"/>
                                        <xsl:with-param name="startIndex" select="./@startIndex"/>
                                        <xsl:with-param name="i" select="$startForLoop"/>
                                    </xsl:call-template>
                                </i>
                            </td>-->
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
            
    </xsl:template>
    
     <xsl:template name="list-nts">
        <xsl:param name="node"/>
        <xsl:param name="howmany"/>
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
    &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<!--<a href="#"  class="SaveAsAndPrintLinks">
	<xsl:text>Return to start page</xsl:text>
	</a>-->
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