<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:template name="drawSeparatorLine">        
        <tr class="rowThes">
            <td colspan="2">
                <br/>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="/">

        <xsl:for-each select="//terms/term">
            <fieldset>
                <legend>
                    <xsl:text>Card of term </xsl:text>
                    <xsl:value-of select="./descriptor/text()"/>
                </legend>	
                
                <table cellspacing="0" cellpadding="0" width="440">
                    <tbody>
                        <!-- descriptor and taxonomical codes -->
                        <tr class="rowThes">
                            <td colspan="2">
                                <span class="headerThes">
                                    <a class="aHeaderAnchorThes" name="8547">
                                        <xsl:value-of select="./descriptor/text()"/>
                                    </a>
                                </span>
                                <!-- taxonomical codes -->
                                <xsl:for-each select="./tc[./text()!='']">
                                    <span class="deweyHeaderThes">
                                        <xsl:text> -- (</xsl:text>
                                        <xsl:value-of select="."/>
                                        <xsl:text>)</xsl:text>
                                    </span>
                                </xsl:for-each>
                                <br/>							
                            </td>
                        </tr>
                        <!-- translations -->
                        <xsl:if test="count(./translations[./@linkClass!=''][./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">Tra.</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./translations[./@linkClass!=''][./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./@linkClass"/>
                                                <xsl:text>: </xsl:text>
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>                        
                        <!-- Scope Note -->
                        <xsl:if test="count(./scope_note[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">SN</span>
                                </td>
                                <td class="valueColThes">
                                    <a>
                                        <span class="valueThes">
                                            <xsl:value-of select="./scope_note/text()"  disable-output-escaping="yes" />
                                        </span>
                                    </a>
                                    <br/>                                    
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- Translations Scope Note -->
                        <xsl:if test="count(./translations_scope_note[./@linkClass!=''][./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">SN (Tra.)</span>
                                </td>
                                <td class="valueColThes">
                                    <a>
                                        <span class="valueThes">
                                            <xsl:for-each select="./translations_scope_note[./@linkClass!=''][./text()!='']">
                                                <a>
                                                    <span class="valueThes">
                                                        <xsl:value-of select="./@linkClass"/>
                                                        <xsl:text>: </xsl:text>
                                                        <xsl:value-of select="./text()" disable-output-escaping="yes"/>
                                                    </span>
                                                </a>
                                                <br/>
                                            </xsl:for-each>
                                        </span>
                                    </a>
                                    <br/>                                    
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- Facet -->
                        <xsl:if test="count(./facet[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">Facets</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./facet[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if> 
                        <!-- Top Term -->
                        <xsl:if test="count(./topterm[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">TT</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./topterm[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- UF  -->
                        <xsl:if test="count(./uf[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">UF</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./uf[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- UF Tra -->
                        <xsl:if test="count(./uf_translations[./@linkClass!=''][./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">UF</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./uf_translations[./@linkClass!=''][./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./@linkClass"/>
                                                <xsl:text>: </xsl:text>
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- BT  -->
                        <xsl:if test="count(./bt[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">BTs</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./bt[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- NT  -->
                        <xsl:if test="count(./nt[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">NTs</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./nt[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- RT  -->
                        <xsl:if test="count(./rt[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">RTs</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./rt[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- Source  -->
                        <xsl:if test="count(./primary_found_in[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">Src.</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./primary_found_in[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>
                        <!-- Source (tra.)  -->
                        <xsl:if test="count(./translations_found_in[./text()!='']) >  0">
                            <xsl:call-template name="drawSeparatorLine"/>
                            <tr class="rowThes">
                                <td class="typeColThes" valign="top">
                                    <span class="typeThes">Src. (Tra.)</span>
                                </td>
                                <td class="valueColThes">
                                    <xsl:for-each select="./translations_found_in[./text()!='']">
                                        <a>
                                            <span class="valueThes">
                                                <xsl:value-of select="./text()"/>
                                            </span>
                                        </a>
                                        <br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>

                    </tbody>
                </table>
            </fieldset>		
        </xsl:for-each> 

    </xsl:template>
</xsl:stylesheet>