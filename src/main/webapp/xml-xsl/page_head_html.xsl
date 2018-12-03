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
    <!-- _________________________________________________________________________________________________
            template: page_head_html
      _________________________________________________________________________________________________
            FUNCTION: template that builds the <head> html part of each page with: title, css and javascript references
    _________________________________________________________________________________________________ -->
    <xsl:template name="page_head_html">
        <head>
            
            <title>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/header/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </title>
            
            <!-- 
              Adding deployment timestamp via maven pom to each include css or javascript file
              so that web clients get the most updated version of each css or js file as long as 
              this file has been updated also (via e.g. new deployment).   
            -->
                 
            
            <!-- CSS files -->
            <link rel="stylesheet" type="text/css" href="third-party-javascript/chosen/chosen.css?v=@DeploymentTimestamp@"/>
            
            <link rel="stylesheet" type="text/css" href="CSS/page.css?v=@DeploymentTimestamp@"/>
            <link rel="stylesheet" type="text/css" href="CSS/xml_thes.css?v=@DeploymentTimestamp@"/>
            
            <!-- JavaScript files -->
            <script language="JavaScript" type="text/javascript" src="Javascript/scripts.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/tabs.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/type_ahead.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/rename.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/criteria.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/graphicalView.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/DBadmin.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/Statistics.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/moveToHierarchy.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/THEMASUsers.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/xml_thes.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/htmlSelect_scripts.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/Admin_Thesaurus.js?v=@DeploymentTimestamp@"/>
            <script language="JavaScript" type="text/javascript" src="Javascript/translations.js?v=@DeploymentTimestamp@"/>                       
            <script language="JavaScript" type="text/javascript" src="third-party-javascript/ckeditor/ckeditor.js?v=@DeploymentTimestamp@"/>
            <!-- chosen requirement jquery/1.12.4 -->
            <script  language="JavaScript" type="text/javascript" src="https://code.jquery.com/jquery-1.12.4.min.js" integrity="sha256-ZosEbRLbNQzLpnKIkEdrPv7lOy9C27hHQ+Xp8a4MxAQ=" crossorigin="anonymous"></script>
            <script language="JavaScript" type="text/javascript" src="third-party-javascript/chosen/chosen.jquery.min.js?v=@DeploymentTimestamp@"></script>
            
            <!-- JavaScript global variables (any javascript file has access to them by simple reference to their names) -->
            <script language="JavaScript" type="text/javascript">       
                <!-- constants defined in THEMASAPIClass.java -->
                
                
                <!-- Vars for length control in input fields, NOT in use any more -->
                var THEMASAPI_LOGINAM_SIZE  = 96;
                var THEMASAPI_MAX_TIME_SIZE = 128;
                var THEMASAPI_MAX_STRING    = 500;
                var THEMASAPI_MAX_COM_LEN   = 20000;
                <!-- End of Vars for length control in input fields -->
                
                
                var UILanguage = '<xsl:value-of select="$lang"/>';
                <!-- Preload bottom image -->
                if(UILanguage=='el'){
                    (new Image()).src ='images/logo-espa-footer-gr.png';
                }
                if(UILanguage=='en'){
                    (new Image()).src ='images/logo-espa-footer-en.png';
                }
            </script>
            <!-- <xsl:value-of select="document('../translations/translations.xml')/locale/loginpage/image/title/option[@lang=$lang]/text()"/>'-->
            <!-- ThemasFav.gif -->
            <xsl:if test="$locale/header/favicon/text()!=''">
                <link rel='shortcut icon' type='image/x-icon'>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$locale/header/favicon/text()"/>
                    </xsl:attribute>
                </link>
            </xsl:if>            
            
            <style type="text/css">
                table.sht  { border: solid #66CC99;
                             border-width: 1px 1px 1px 1px; }
                th.sht, td.sht { border: solid #66CC99;
                             border-width: 1px 0px 0px 1px;
                             padding: 4px; }
                th.sht     { background-color: #339999; color: #FFFFFF; }
                tr.altsht td { background-color: #EEEEEE; }
                tbody.sht  { height: 200px;
                             overflow-y: auto; overflow-x: hidden; }
            </style>
<!--[if IE]>
    <style type="text/css">
        div.sht   {position: relative; left: 0px; top: 0px;
                   height: 200px; width: 300px;
                   overflow-y: scroll; overflow-x: auto;
                   border: solid #66CC99;
                   border-width: 0px 0px 1px 0px; }
        table.sht {border-width: 1px 1px 0px 0px;}
        thead.sht tr {position: relative;
                   top: expression(this.offsetParent.scrollTop); }
        tbody.sht {height: auto }
    </style>
<![endif]-->
        </head>
    </xsl:template>
</xsl:stylesheet>
