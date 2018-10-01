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
    <!--
        Document   : search_results_users.xsl
        Created on : Παρασκευή, 27 Μαρτίου 2009, 3:39:42 μμ
        Author     : karam
    -->
    <xsl:include href="PagingInfo_User.xsl"/>
    <xsl:variable name="userslocale" select="$locale/primarycontentarea/users"/>
    <xsl:template match="/page" name="search_results_users">
        <xsl:param name="paginglocale" />
        <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
        <!--<fieldset class="links">
            <legend>
                <xsl:value-of select="$userslocale/tableresults/legend/option[@lang=$lang]"/>
            </legend>
            -->
        <table width="100%">
            <xsl:if test="//results/paging_info">
                    <xsl:call-template name="DisplayStatisticsAndPagingInfo_Users">
                        <xsl:with-param name="paginglocale" select="$paginglocale" />
                    </xsl:call-template>
                </xsl:if>
        </table>
        
            <table width="100%" style="padding-left:5px; overflow:auto;">
                
                <tr width="100%">
                    <xsl:attribute name="style">
                        <xsl:text>background-color: </xsl:text>
                        <xsl:value-of select="$alternateRowsColor1"/> 
                        <xsl:text>;</xsl:text>
                        <!--<xsl:text>; text-align:center;</xsl:text>-->
                        <!--<xsl:text>background-color: #E2E2E2;</xsl:text>-->
                    </xsl:attribute>                 
                    <xsl:choose>
                        <xsl:when test="count(//results/user)=0">
                            <td align="left" valign="top" colspan="5">
                                <strong>
                                    <xsl:value-of select="$userslocale/tableresults/noresultsmsg/option[@lang=$lang]"/>
                                </strong>
                            </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <!--<xsl:if test="//results/user[1]/@no">
                                    <td height="5">
                                            <strong>#</strong>
                                    </td>
                            </xsl:if>-->
                            
                            <xsl:for-each select="//results/user[1]/node()">
                                <xsl:choose>
                                    <xsl:when test="name() = 'thesaurusSet'">
                                        <td height="5" style="width:30%;">                                            
                                            <strong>
                                                <xsl:value-of select="$userslocale/tableresults/columns/userproperties/option[@lang=$lang]"/>
                                            </strong>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td height="5">
                                            <strong>
                                                <xsl:choose>
                                                    <xsl:when test="name() = 'name' ">
                                                        <xsl:value-of select="$userslocale/tableresults/columns/username/option[@lang=$lang]"/>
                                                    </xsl:when>
                                                    <!--<xsl:when test="name() = 'DBname' ">Όνομα βάσης χρήστη</xsl:when>
                                                    <xsl:when test="name() = 'thesaurusSet' ">
                                                        <xsl:value-of select="$userslocale/tableresults/columns/userproperties/option[@lang=$lang]"/>
                                                    </xsl:when>-->
                                                    <xsl:when test="name() = 'description' ">
                                                        <xsl:value-of select="$userslocale/tableresults/columns/userdescription/option[@lang=$lang]"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="name()"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </strong>
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>
                                
                            </xsl:for-each>
                            <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'">
                                <td>
                                    <strong>
                                        <xsl:value-of select="$userslocale/tableresults/columns/actions/legend/option[@lang=$lang]"/>
                                    </strong>
                                    <!-- <strong>Actions</strong> -->
                                </td>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </tr>
                <xsl:for-each select="//results/user">
                    <tr class="resultRow" valign="top">
                        <xsl:attribute name="onMouseOver">
                            <xsl:text>this.style.background = '</xsl:text>
                            <xsl:value-of select="$onMouseOverColor"/> 
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>                        
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
                        <!--<xsl:if test="./@no">
                                <td>
                                        <xsl:value-of select="./@no"/>
                                </td>
                        </xsl:if>-->
                        <xsl:for-each select="./node()">
                            <td class="resultRow">
                                <xsl:choose>
                                    <xsl:when test="name() = 'name' ">
                                        <a>
                                            <xsl:value-of select="."/>
                                        </a>
                                    </xsl:when>
                                    <!--<xsl:when test="name() = 'DBname' ">
                                            <a>
                                                    <xsl:value-of select="."/>
                                            </a>
                                    </xsl:when>-->
                                    <xsl:when test="name() = 'description' ">
                                        <a>
                                            <xsl:if test=".=''">-</xsl:if>
                                            <xsl:value-of select="."/>
                                        </a>
                                    </xsl:when>									
                                    <xsl:otherwise>		<!-- <thesaurusSet> -->
                                        <xsl:choose>
                                            <xsl:when test="./thesaurus ='' and ./@group != 'ADMINISTRATOR' ">-</xsl:when>
                                            <xsl:otherwise>
                                                <xsl:choose>
                                                    <xsl:when test="count(./thesaurus)=0">-</xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:variable name="howmanyFields" select="count(./thesaurus)"/>
                                                        <xsl:for-each select="./thesaurus">
                                                            <xsl:sort select="."/>
                                                            <xsl:variable name="groupVar" select="./@group"/>
                                                            <a>
                                                                <xsl:choose>
                                                                    <xsl:when test="./text()='*'">
                                                                        <xsl:value-of select="$userslocale/tableresults/columns/allThesauriDisplayText/option[@lang=$lang]"/>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <xsl:value-of select="."/>
                                                                    </xsl:otherwise>
                                                                </xsl:choose>                                                                
                                                                <xsl:if test=". != '' "> - </xsl:if>
                                                                <xsl:value-of select="$locale/usergroups/node()[name()=$groupVar]/option[@lang=$lang]"/>                                                                                                                                
                                                            </a>
                                                            <xsl:choose>
                                                                <xsl:when test="position()!= $howmanyFields">
                                                                    <xsl:text>,</xsl:text>
                                                                    <br/>
                                                                </xsl:when>
                                                            </xsl:choose>
                                                        </xsl:for-each>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </xsl:for-each>
                        <xsl:if test="$THEMASUserInfo_userGroup != 'READER'  and $THEMASUserInfo_userGroup != 'EXTERNALREADER'">
                            <td align="center" width="10%">
                                <xsl:variable name="currentJS0">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="./node()"/>
                                        <xsl:with-param name="replace" select="$Slash"/>
                                        <xsl:with-param name="with" select="$SlashJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="currentJS">
                                    <xsl:call-template name="replace-string">
                                        <xsl:with-param name="text" select="$currentJS0"/>
                                        <xsl:with-param name="replace" select="$Apos"/>
                                        <xsl:with-param name="with" select="$AposJS"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <!-- Τροποποίηση -->
                                <a href="#">
                                    <xsl:attribute name="onClick">
                                        <xsl:text>showEditFieldCard('</xsl:text>
                                        <xsl:value-of select="$currentJS"/>
                                        <xsl:text>','user_edit', 'EditDisplays_User');</xsl:text>
                                    </xsl:attribute>
                                    <img width="16" height="16" border="0" >
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="$userslocale/tableresults/columns/actions/edit/src/option[@lang=$lang]"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="title">
                                            <xsl:value-of select="$userslocale/tableresults/columns/actions/edit/title/option[@lang=$lang]"/>
                                        </xsl:attribute>

                                    </img>
                                </a>
                            </td>
                        </xsl:if>
                    </tr>
                </xsl:for-each>
            </table>
            <!--</form>-->
        <!--</fieldset>-->
        <!--  </body>
        </html>-->
    </xsl:template>
</xsl:stylesheet>
