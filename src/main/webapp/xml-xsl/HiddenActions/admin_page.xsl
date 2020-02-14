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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"  exclude-result-prefixes="xsl fo xs fn xdt">>
    <xsl:include href="../page_head_html.xsl"/>
    <xsl:include href="../page_header.xsl"/>
    <xsl:include href="../page_footer.xsl"/>
    <xsl:include href="../Configs.xsl"/>
    <xsl:include href="HiddenActions.xsl"/>
    <xsl:variable name="locale" select="document('../../translations/translations.xml')/locale"/>
    <xsl:variable name="lang" select="//page/@language"/>
    <xsl:variable name="adminTag" select="'admin'"/>
    <xsl:output method="html"  
                    encoding="UTF-8"  
                    indent="yes" 
                    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                    doctype-system="http://www.w3.org/TR/html4/loose.dtd"
                    version="4.0" />	
    <!-- _________________________________________________________________________________________________
              template: page
        _________________________________________________________________________________________________
              FUNCTION: template that builds the structure of each page: header - leftmenu - content - footer
    _________________________________________________________________________________________________ -->
    <xsl:template match="/">
            
        <!--        <html xmlns="http://www.w3.org/1999/xhtml">-->
        <html>

            <!-- html_head -->
            <xsl:call-template name="page_head_html"/>				
            <!-- BODY: javascript code for AJAX type-ahead mechanism -->
            <!-- <body onload="loadTypeAheadActions();" onmouseup="direction='Released'" onmousemove=" SetDivPosition(event)"> -->
            <body>
                            
                <div id="all" >
                    <xsl:if test="$pageBgColor!=''">
                        <xsl:attribute name="style">
                            <xsl:text>background-color:</xsl:text>
                            <xsl:value-of select="$pageBgColor"/>
                            <xsl:text>;</xsl:text>
                        </xsl:attribute>
                    </xsl:if>
                    <!-- peplo screen -->
                    <xsl:call-template name="peploScreen"/>				
                    <div id="page">
                        <!--<script type="text/javascript" language="javascript">finishSession=true;</script>-->
                        <!-- header -->
                        <div id="header">
                            <xsl:call-template name="page_header"/>							
                        </div>
                                        
                        <xsl:if test="count(//content_THEMAS_HiddenActions) != 0">
                            <!-- only in case of THEMAS_HiddenActions servlet -->
                            <div id="admin_content" style="background: #E2E2E2; width:100%;" >
                                <div id="DisplayCardArea" style="background: #E2E2E2; width:100%;"/>
                                <xsl:call-template name="THEMAS_HiddenActions_content"/>
                            </div>
                        </xsl:if>					
                        <!-- footer -->
                        <div id="footer">                                            
                            <xsl:call-template name="page_footer"/>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
	
    <!-- _________________________________________________________________________________________________
              template: peploScreen
        _________________________________________________________________________________________________
              FUNCTION: template that defines the peplo screen (hidden initially)
    _________________________________________________________________________________________________ -->
    <xsl:template name="peploScreen">
        <div id="peploScreen" style="z-index:300; visibility:hidden; vertical-align:middle; position:absolute; display:block; top:0px; left:0px; width:100%; height:100%;  background:url(images/peplo.png) repeat; "></div>
    </xsl:template>	
</xsl:stylesheet>
