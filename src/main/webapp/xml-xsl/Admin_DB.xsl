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
    <xsl:variable name="locale" select="document('../translations/translations.xml')/locale"/>
    <xsl:variable name="lang" select="//page/@language"/>
    <xsl:variable name="CurrentDIV" select="//page/content_DBadmin/CurrentShownDIV"/>
    <xsl:variable name="OtherActiveSessionsNO" select="//page/content_DBadmin/OtherActiveSessionsNO"/>
    <xsl:variable name="BLANK_VALUE_for_empty_select_item">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
    </xsl:variable>
    <xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
    <!-- _____________________________________________________________________________
              TEMPLATE: DBadmin_content
              FUNCTION: displays the contents of the DBadmin operations
    _____________________________________________________________________________ -->
    <xsl:template name="DBadmin_content">
        <xsl:variable name="tabup" select="//page/content_DBadmin/CurrentShownDIV"/>
        <div id="DisplayCardArea"/>
        <!-- _________________ TABs menu _________________ -->
        <div id="displaytabmenu">
            <table border="0" cellpadding="0" cellspacing="0" style="width:100%;">
                <tr valign="left">
                    <!-- _________________ TAB: Δημιουργία αντιγράφου ασφαλείας _________________ -->
                    <xsl:if test="$THEMASUserInfo_userGroup != 'THESAURUS_TEAM' ">
                        <td id="Create_Restore_DB_backup_ΤΑΒ"  style="width:10%;">
                            <a href="DBadmin?DIV=Create_Restore_DB_backup_DIV" onFocus="if(this.blur)this.blur()" id="Create_Restore_DB_backup_LINK" >
                                <xsl:choose>
                                    <xsl:when test="$tabup='Create_Restore_DB_backup_DIV'">
                                        <xsl:attribute name="class">
                                            <xsl:text>active</xsl:text>
                                        </xsl:attribute>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="class">
                                            <xsl:text>inactive</xsl:text>
                                        </xsl:attribute>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/db/tabs/tab1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </a>
                        </td>
                    </xsl:if>
						
                    <!-- _________________ TAB: Επιδιόρθωση βάσης δεδομένων _________________ -->
                    <xsl:if test="$THEMASUserInfo_userGroup = 'ADMINISTRATOR' ">
                        <td id="Fix_DB_TAB" style="width:10%;" >
                            <a href="DBadmin?DIV=Fix_DB_DIV" onFocus="if(this.blur)this.blur()" id="Fix_DB_LINK" >
                                <xsl:choose>
                                    <xsl:when test="$tabup='Fix_DB_DIV'">
                                        <xsl:attribute name="class">
                                            <xsl:text>active</xsl:text>
                                        </xsl:attribute>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="class">
                                            <xsl:text>inactive</xsl:text>
                                        </xsl:attribute>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$locale/primarycontentarea/db/tabs/tab2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </a>
                        </td>
                    </xsl:if>
                    <td style="width:40%; ">
                        <a class="inactive" >&#160;&#160;&#160;</a>  
                    </td>
                    <td style="width:20%; ">
                        <a class="inactive" >&#160;&#160;&#160;</a>  
                    </td>
                    <td style="width:20%; ">
                        <a class="inactive" >&#160;&#160;&#160;</a>  
                    </td>
                </tr>
            </table>
        </div>
        
        <div id="content" style="text-align:left">
            <!-- _________________ DIV: Δημιουργία ή επαναφορά αντιγράφου ασφαλείας _________________ -->
            <div id="Create_Restore_DB_backup_DIV" class="tab-body">
                <xsl:choose>
                    <xsl:when test="$tabup='Create_Restore_DB_backup_DIV'">
                        <xsl:attribute name="style">
                            <xsl:text>visibility:visible;</xsl:text>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">
                            <xsl:text>visibility:hidden;</xsl:text>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- visibility: hidden; !!!!!!!!!!!!! -->
                <xsl:call-template name="Create_Restore_DB_backup">
                    <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/db/backups"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
            <!-- _________________ DIV: Επιδιόρθωση βάσης δεδομένων _________________ -->
            <xsl:if test="$THEMASUserInfo_userGroup = 'ADMINISTRATOR' ">
                <div id="Fix_DB_DIV" class="tab-body">
                    <xsl:choose>
                        <xsl:when test="$tabup='Fix_DB_DIV'">
                            <xsl:attribute name="style">
                                <xsl:text>visibility:visible;</xsl:text>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="style">
                                <xsl:text>visibility:hidden;</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <!-- visibility: hidden; !!!!!!!!!!!!! -->
                    <xsl:call-template name="Fix_DB">
                        <xsl:with-param name="specificlocale" select="$locale/primarycontentarea/db/fixdb"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </div>
            </xsl:if>
            
        </div>
        <!-- _________________ display the current DIV_________________ -->
        <!--<script language="javascript">
            <xsl:text>DBadminDisplayDIV('</xsl:text>
            <xsl:value-of select="$CurrentDIV"/>
            <xsl:text>');</xsl:text>
        </script>-->
        <!-- in case there are other active sessions => warn user for their existence -->
        <xsl:if test="$OtherActiveSessionsNO &gt; 0">
            <!--<script language="javascript">alert('Προσοχή. Αριθμός επιπλέον ενεργών συνδέσεων με το σύστημα: <xsl:value-of select="$OtherActiveSessionsNO"/>');</script>-->
        </xsl:if>
    </xsl:template>
    <!-- _____________________________________________________________________________
              TEMPLATE: Create_DB_backup
              FUNCTION: displays the contents of the Create_DB_backup div
    _____________________________________________________________________________ -->
    <xsl:template name="Create_Restore_DB_backup">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang"/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/restoredblegend/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <form method="post" id="Restore_DB_backupForm" action="" >
                <!-- List of DB backups -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/backuplist/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <br/>
                <br/>
                &#160;
                <select id="DB_backupsListID" name="DB_backupsListNAME" size="10">
                    <xsl:for-each select="//page/content_DBadmin/filesInDBBackupFolder/DBBackup">
                        <xsl:sort order="descending"/>
                        <option>
                            <xsl:if test="position()=1 ">
                                <xsl:attribute name="selected">
                                    <xsl:text>true</xsl:text>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="value">
                                <xsl:value-of select="."/>
                            </xsl:attribute>
                            <xsl:value-of select="."/>
                        </option>
                    </xsl:for-each>
                    <xsl:if test="count(//page/content_DBadmin/filesInDBBackupFolder/DBBackup) = 0 ">
                        <!-- in case of no existing DB backups -->
                        <option>
                            <xsl:value-of select="$BLANK_VALUE_for_empty_select_item"/>
                        </option>
                    </xsl:if>
                </select>
                <br/>
                <br/>
                &#160;
                <input onClick="Restore_DB_backupDeleteButtonPressed()" type="submit">
                    <xsl:attribute name="value">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/deletebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </xsl:attribute>
                    <!-- ONLY administrator has the right to delete a DB backup -->
                    <xsl:choose>
                        <xsl:when test="$THEMASUserInfo_userGroup != 'ADMINISTRATOR' ">
                            <xsl:attribute name="disabled">
                                <xsl:text>disabled</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="class">
                                <xsl:text>disabledbutton</xsl:text>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">
                                <xsl:text>button</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </input>
                &#160;
                <input onClick="Restore_DB_backupRestoreButtonPressed()" class="button" type="submit">
                    <xsl:attribute name="value">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/restorebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </xsl:attribute>
                    <!-- ONLY administrator has the right to restore a DB backup -->
                    <xsl:choose>
                        <xsl:when test="$THEMASUserInfo_userGroup != 'ADMINISTRATOR' ">
                            <xsl:attribute name="disabled">
                                <xsl:text>disabled</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="class">
                                <xsl:text>disabledbutton</xsl:text>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">
                                <xsl:text>button</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </input>
                <!-- LINE -->
                <hr/>
                <!-- Result area -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <textarea id="Restore_DB_backup_result_textarea_ID" name="Restore_DB_backup_result_textarea_NAME" class="admintextarea" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'" disabled="disabled">
                    <xsl:value-of select="//page/content_DBadmin/RestoreDBbackupResult/RestoreDBbackupResultMessage"/>
                </textarea>
            </form>
        </fieldset>
        <br/>
        <br/>
        <br/>
        <fieldset>
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createbackup/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <form method="post" id="Create_DB_backupForm" action="">
                <!-- Description - OK button -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/descriptionprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <input type="text" id="Create_DB_backup_Description_ID" name="Create_DB_backup_Description_NAME" style="width:500px;" onkeypress="if (event.keyCode == 13) return false;">
                    <!-- disable ENTER key-->
                    <xsl:attribute name="value">
                        <xsl:value-of select="//page/content_DBadmin/CreateDBbackupResult/backupDescription"/>
                    </xsl:attribute>
                </input>	
                &#160;
                <input onClick="Create_DB_backupOKButtonPressed()"  class="button" type="submit">
                    <xsl:attribute name="value">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </xsl:attribute>
                </input>
                <!-- LINE -->
                <hr/>
                <!-- Result area -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <textarea id="Create_DB_backup_result_textarea_ID" name="Create_DB_backup_result_textarea_NAME" class="admintextarea" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'" disabled="disabled">
                    <xsl:value-of select="//page/content_DBadmin/CreateDBbackupResult/CreateDBbackupResultMessage"/>
                </textarea>
            </form>
        </fieldset>
    </xsl:template>
	
    <!-- _____________________________________________________________________________
              TEMPLATE: Fix_DB
              FUNCTION: displays the contents of the Fix_DB div
    _____________________________________________________________________________ -->
    <xsl:template name="Fix_DB">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang"/>
        <fieldset >
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <form method="post" id="Fix_DBForm" action="" style="width:100%;">
                <!-- Description of the operation -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/descriptiontitle/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <br/>
                <ul>
                    <li>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/bullet1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </li>
                    <li>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/bullet2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </li>
                </ul>
                <table >
                    <tr>
                        <td style="width:820px;" align="center">
                            <input onClick="Fix_DBOKButtonPressed()" class="button" type="submit">
                                <xsl:attribute name="value">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/okbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </input>
                        </td>
                    </tr>
                </table>
				
                <!-- LINE -->
                <hr/>
                <!-- Result area -->
                &#160;
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/result/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <textarea id="Fix_DB_result_textarea_ID" name="Fix_DB_result_textarea_NAME" class="admintextarea" onfocus="this.style.border='1px solid #000'" onblur="this.style.border='1px solid #999966'" disabled="disabled">
                    <xsl:value-of select="//page/content_DBadmin/FixDBResult/FixDBResultMessage"/>
                </textarea>
            </form>
        </fieldset>
        <br/>
        <br/>
    </xsl:template>
</xsl:stylesheet>
