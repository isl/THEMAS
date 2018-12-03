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
    Document   : Edit_User.xsl
    Created on : 23 Μάρτιος 2009, 6:54 μμ
    Author     : tzortzak
    Description:
    Purpose of transformation follows.
-->
<!-- _____________________________________________________________________________
			Full page for pop-up cards using for Users creation / editing
      _____________________________________________________________________________ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
	<xsl:output method="html"/>
        <xsl:include href="../Configs.xsl"/>
	<xsl:variable name="targetEditField" select="//targetEditField"/>
	<xsl:variable name="THEMASUserInfo_userGroup" select="//THEMASUserInfo/userGroup"/>
	<xsl:template match="/">
            <xsl:variable name="usercardlocale" select="document('../../translations/translations.xml')/locale/popupcards/users"/>
            <xsl:variable name="lang" select="//page/@language"/>
		<html>
			<head>
				<title>Edit_User.xsl</title>
			</head>
			<body>
				<div class="popUpEditCardLarge">
					<xsl:choose>
						<xsl:when test="$targetEditField = 'save_user_create' ">
							<xsl:call-template name="CreateNew_User">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
                                                <xsl:when test="$targetEditField = 'save_user_create_and_merge_with_older' ">
							<xsl:call-template name="Create_User_And_Merge_With_Older">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
						<xsl:when test="$targetEditField = 'save_user_edit' ">
							<xsl:call-template name="Edit_User">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
                                                <xsl:when test="$targetEditField = 'save_rename_target_and_older_user_edit'">
                                                    <xsl:call-template name="rename_target_and_older_user">
                                                        <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
                                                </xsl:when>
						<xsl:when test="$targetEditField = 'save_share_thesaurus' ">
							<xsl:call-template name="Share_Thesaurus">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
						<xsl:when test="$targetEditField = 'save_change_password' ">
							<xsl:call-template name="Edit_Password">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
						<xsl:when test="$targetEditField = 'save_change_thesaurus' ">
							<xsl:call-template name="Edit_Thesaurus">
                                                            <xsl:with-param name="specificlocale" select="$usercardlocale/editactions"/>
                                                            <xsl:with-param name="lang" select="$lang"/>
                                                        </xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>Under construction</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</body>
		</html>
	</xsl:template>
<!-- _____________________________________________________________________________
                TEMPLATE: CreateNew_User
_____________________________________________________________________________ -->
    <xsl:template name="CreateNew_User">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <xsl:variable name="existingThesaurusName" select="//pageInfo/existingThesaurus/existingThesaurusName"/>
        <xsl:variable name="THEMASUsersGroupName" select="//pageInfo/THEMASUsersGroups/THEMASUsersGroupName"/>
        <fieldset id="edit_user_create" >
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            
            <table border="0" align="center">
                
                <!--  UserName: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/usernameprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserName_Id" type="text" size="30" name="newUserName">
                            <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
                        </input>
                        <i>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/usernamenote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </i>
                    </td>
                </tr>
                
                <!--  Password: -->
                <tr>
                    <td width="15%" align="right">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/passwordprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserPassword_Id" type="text" size="30" name="newPassword_User"/>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/passwordnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                </tr>
                
                <!--  User Description: -->
                <tr>                    
                    <td width="15%" align="right">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/descriptionprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserDescription_Id" type="text" style="width:90%;" name="newDescription_User"/>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/descriptionnote/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                </tr>
                
                <xsl:if test="$THEMASUserInfo_userGroup = 'ADMINISTRATOR' ">
                    <tr>
                        <td width="15%" align="right">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/adminprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            </b>
                        </td>
                        
                        <td>
                            <input id="administratorCheckBoxId" name="administratorCheckBox" type="checkbox" onclick="administratorCheckBoxClick(this)"/>
                        </td>
                    </tr>
                </xsl:if>
                
            </table>
            
            <!--  Groups - Thesauri -->
            <hr/>

            <table border="0" align="center" id="thesaurusGroupTable" style="margin:auto;">
                <tbody id="thesaurusGroupBody">
                    <!-- header -->
                    <tr class="contentHeadText" align="center">
                        <td bgcolor="#FFFFFF"></td>
                        <td class="thesaurusFieldWidth">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/thesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            </b>
                        </td>
                        
                        <td class="thesaurusFieldWidth">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/usergroup/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            </b>
                        </td>
                        
                        <td bgcolor="#FFFFFF" style="width:50px;"></td>
                        <td bgcolor="#FFFFFF" ></td>
                    </tr>
                    <!-- HIDDEN row in case there is none role has been assigned to this user. This row will be used for the add button functionality. -->
                    <tr align="center" id="thesaurusGroupCouple" name="thesaurusGroupCoupleName" class="hiddenInput" style="visibility:collapse;">
                        <!-- Thesaurus -->
                        <td bgcolor="#FFFFFF" ></td>
                        <td>
                            <select id="selectThesaurus_ID" name="selectThesaurus" size="1" style="width: 100%;" disabled="disabled">
                                <xsl:for-each select="$existingThesaurusName">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:choose>
                                            <xsl:when test="./text()='*'">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </option>
                                </xsl:for-each>                                
                            </select>
                        </td>
                        <!-- Group -->
                        <td>
                            <select id="selectUserGroup_ID" name="selectUserGroup" size="1" style="width: 100%;" disabled="disabled">
                                <xsl:for-each select="$THEMASUsersGroupName">
                                    <option>
                                        <xsl:variable name="currentChoice" select="."/>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                        <td>
                            <img width="20" height="20" border="0" onClick="RemoveThesaurusGroupInput('thesaurusGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </img>
                        </td>                                            
                    </tr>

                                        
                    
                    <tr align="center" id="thesaurusGroupCouple" name="thesaurusGroupCoupleName">
                        <!-- Thesaurus -->
                        <td bgcolor="#FFFFFF" ></td>
                        <td>
                            <select id="selectThesaurus_ID" name="selectThesaurus" style="width: 100%;">
                                <!--<option value="*">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </option>-->
                                <xsl:for-each select="$existingThesaurusName">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:choose>
                                            <xsl:when test="./text()='*'">                                                
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>                                                    
                                            </xsl:when>
                                            <xsl:otherwise>                                                
                                                <xsl:value-of select="."/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        
                                    </option>
                                </xsl:for-each>                                
                            </select>
                        </td>
                        
                        <!-- Group -->
                        <td >
                            <select id="selectUserGroup_ID" name="selectUserGroup" style="width: 100%;">
                                <xsl:for-each select="$THEMASUsersGroupName">
                                    <option>
                                        <xsl:variable name="currentChoice" select="."/>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                        
                        <!-- icon (-) -->
                        <td>
                            <img width="20" height="20" border="0" onClick="RemoveThesaurusGroupInput('thesaurusGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </img>
                        </td>
                        <td bgcolor="#FFFFFF" ></td>
                    </tr>
                    
                </tbody>
                
            </table>
          
            <!-- Addition: -->
            <hr/>
  
            
            <table border="0" width="100%" id="TableWithPlusIconId">
                <tr  >
                    <td width="50%" align="right" valign="middle">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addition/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </td>
         
                    <td width="50%" align="left" valign="middle">
                        <img width="20" height="20" border="0" onClick="AddThesaurusGroupInput('thesaurusGroupBody',  'thesaurusGroupCouple');">
                            <xsl:attribute name="src">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </img>
                    </td>
                    
                </tr>
                
            </table>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
            </input>
        
        </fieldset>
	
        <!-- _______________ END of fieldset ______________ -->
        <table width="820">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
                <td valign="top" align="right" width="220">
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','edit_user_create', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    
    </xsl:template>
       
<!-- _____________________________________________________________________________
                TEMPLATE: Create_User_And_Merge_With_Older
                TEMPLATE FOR CREATION OF A USER THAT EXISTS IN DB BUT NOT IN WebAppUSERS.xml
_____________________________________________________________________________ -->
    <xsl:template name="Create_User_And_Merge_With_Older">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <xsl:variable name="LoggedUserGroup" select="//page/THEMASUserInfo/userGroup"/>
        <fieldset id="edit_user_create_and_merge">
           <legend>
               <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
           </legend>          
                <br/>
                <table border="0" width="100%" align="center">
                    <tr>
                        <td width="35%" align="right">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/usernameinfo/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        <td>
                            <input id="newUserName_Id" type="text" size="30" name="newUserName" disabled="disabled" class="disabledbutton" >
                                <xsl:attribute name="value"><xsl:value-of select="//newUserInfo/name"/></xsl:attribute>
                            </input>
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/additionalfields/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </b>
                        </td>
                        </tr>
            
                
                <!--  Κωδικός Χρήστη: -->
                <tr>
                    <td width="35%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/password/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserPassword_Id" type="text" size="30" name="newPassword_User" disabled="disabled" class="disabledbutton" >
                            <xsl:attribute name="value"><xsl:value-of select="//newUserInfo/password"/></xsl:attribute>
                        </input>
                    </td>
                </tr>
                
                <!--  Περιγραφή: -->
                <tr>
                    <td width="35%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/description/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <input id="newUserDescription_Id" type="text" style="width:90%;" name="newDescription_User" >
                            <xsl:attribute name="value"><xsl:value-of select="//newUserInfo/description"/></xsl:attribute>
                        </input>
                    </td>
                </tr>
                
                <!--  Ρόλοι: -->
                <tr>
                    <td width="35%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/groups/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <xsl:for-each select="//newUserInfo/thesaurus">
                                <xsl:variable name="currentChoice" select="./@group"/>
                                <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                <xsl:if test=" . != '' ">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/inthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                <xsl:value-of select="."/>
                                <input name="selectThesaurus" size="2" type="text" class="hiddenInput">
                                    <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                                </input>
                            </xsl:if>
                            <input name="selectUserGroup" size="2" type="text" class="hiddenInput"><xsl:attribute name="value"><xsl:value-of select="./@group"/></xsl:attribute></input>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
                
            </table>
            <hr/>
            <table>
                    <tr>
                        <td colspan="2">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/infomsgpart1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            <xsl:value-of select="//newUserInfo/name"/>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/infomsgpart2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/infomsgpart3/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                    </tr>
                    <tr>
                        <td valign="middle">
                            <input name="olderUserCreateChoice" type="radio" value="CommitInitialCreateUser" />
                        </td>
                        <td>
                            
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/choice1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            <b>
                                <xsl:value-of select="//newUserInfo/name"/>
                            </b>
                            <xsl:text>. </xsl:text>
                        </td>
                    
                    </tr>
                    <tr>
                        <td valign="middle">
                            <input name="olderUserCreateChoice" type="radio" value="RenameOlderAndThenCreateTargetUser" checked="checked" />
                        </td>
                        <td>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/choice2a/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            <b>
                                <xsl:value-of select="//newUserInfo/name"/>
                            </b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/choice2b/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            <input size="30" id="olderUserRenameName_Id" name="olderUserRenameName" type="text" />
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/createmerge/choice2c/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            <b>
                                <xsl:value-of select="//newUserInfo/name"/>
                            </b>
                            <xsl:text>.</xsl:text>
                        </td>
                    </tr>
                    
                </table>
            <input id="targetUserID" type="text" name="targetUser" style="display:none;">
                <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="display:none;">
                <xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
            </input>
            
        </fieldset>
        
        <!-- _______________ END of fieldset ______________ -->
        <table width="100%" >
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
                
                <td valign="bottom" align="right" width="220">
                    <br/>
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','edit_user_create_and_merge', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                         <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
            
        </table>
                
    </xsl:template>


<!-- _____________________________________________________________________________
                TEMPLATE: Edit_User
_____________________________________________________________________________ -->
    <xsl:template name="Edit_User">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <xsl:variable name="LoggedUserGroup" select="//page/THEMASUserInfo/userGroup"/>
        <fieldset id="edit_user_create">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <!--  user Login name: -->
                <tr>
                    <td width="18%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/username/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <input id="newUserName_Id" type="text" size="30" name="newName_User">
                            <xsl:choose>
                                <xsl:when test="$LoggedUserGroup = 'ADMINISTRATOR' or $LoggedUserGroup = 'THESAURUS_COMMITTEE'"/>
                                <xsl:otherwise>
                                    <xsl:attribute name="disabled"><xsl:value-of select="'disabled'"/></xsl:attribute>
                                    <xsl:attribute name="class"><xsl:value-of select="'disabledbutton'"/></xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
                        </input>
                    </td>
                </tr>
                <!--  Description -->
                <tr>
                    <td width="18%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/description/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <input id="newUserDescription_Id" type="text" style="width:90%;" name="newDescription_User">
                            
                            <xsl:attribute name="value">
                                <xsl:value-of select="//userForEditingInfo/description"/>
                            </xsl:attribute>
                            
                        </input>
                    </td>
                </tr>
                <!-- is Admin checkBox -->
                <xsl:if test="//THEMASUserInfo/userGroup = 'ADMINISTRATOR' ">
                    <tr>
                        <td width="15%" align="right">
                            <b>
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/adminprompt/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                            </b>
                        </td>
                        
                        <td>
                            <input id="administratorCheckBoxId" name="administratorCheckBox" type="checkbox" onclick="administratorCheckBoxClick(this)">
                                <xsl:if test="count(//thesaurus[./@group ='ADMINISTRATOR']) > 0">
                                    <xsl:attribute name="checked">
                                        <xsl:text>checked</xsl:text>                                            
                                    </xsl:attribute>
                                </xsl:if>                                
                            </input>
                        </td>
                    </tr>
                </xsl:if>
                <!--  User roles: -->
                <tr id="rolesDefinitionRow">
                    <xsl:if test="count(//thesaurus[./@group ='ADMINISTRATOR']) > 0">
                        <xsl:attribute name="style">
                            <xsl:text>display:none;</xsl:text>                                            
                        </xsl:attribute>
                    </xsl:if>
                    <td width="18%" align="right" style="vertical-align: text-top;">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/groups/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>                     
                    <td>
                        <xsl:choose>
                            <xsl:when test="//THEMASUserInfo/userGroup = 'ADMINISTRATOR'">
                                <xsl:variable name="existingThesaurusName" select="//existingThesaurus/existingThesaurusName"/>
                                <xsl:variable name="THEMASUsersGroupName" select="//THEMASUsersGroups/THEMASUsersGroupName"/>
                                
                                <table border="0" align="center" id="thesaurusGroupTable" style="width: 500px;">
                                    <tbody id="thesaurusGroupBody">
                                        <!-- header labels -->
                                        <tr class="contentHeadText" align="center">
                                            <td class="thesaurusFieldWidth">
                                                <b>
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/thesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                                </b>
                                            </td>
                                            
                                            <td class="thesaurusFieldWidth">
                                                <b>
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/usergroup/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                                                </b>
                                            </td>
                                            
                                            <td bgcolor="#FFFFFF" style="width:50px;"></td>                                            
                                        </tr>
                                        
                                        <!-- HIDDEN row in case there is none role has been assigned to this user. This row will be used for the add button functionality. -->
                                        <tr align="center" id="thesaurusGroupCouple" name="thesaurusGroupCoupleName" class="hiddenInput" style="visibility:collapse;">
                                            <!-- Thesaurus -->
                                            <td  class="thesaurusFieldWidth">
                                                <select id="selectThesaurus_ID" name="selectThesaurus" class="thesaurusFieldWidth" size="1" disabled="disabled">
                                                    <xsl:for-each select="$existingThesaurusName">
                                                        <xsl:sort select="."/>
                                                        <option>
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="."/>
                                                            </xsl:attribute>
                                                            <xsl:choose>
                                                                <xsl:when test="./text()='*'">
                                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:value-of select="."/>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </option>
                                                    </xsl:for-each>                                
                                                </select>
                                            </td>
                                            <!-- Group -->
                                            <td style="width:150px;">
                                                <select id="selectUserGroup_ID" name="selectUserGroup" size="1" disabled="disabled">
                                                    <xsl:for-each select="$THEMASUsersGroupName">
                                                        <option>
                                                            <xsl:variable name="currentChoice" select="."/>
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="."/>
                                                            </xsl:attribute>
                                                            <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                                        </option>
                                                    </xsl:for-each>
                                                </select>
                                            </td>
                                            <td>
                                                <img width="20" height="20" border="0" onClick="RemoveThesaurusGroupInput('thesaurusGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                                    <xsl:attribute name="src">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:attribute>
                                                </img>
                                            </td>                                            
                                        </tr>
                                        <!-- for each user role -->
                                        <xsl:for-each select="//userForEditingInfo/thesaurus">
                                            <xsl:variable name="currentChoice" select="./@group"/>
                                            <xsl:variable name="thesaurusChoice" select="."/>
                                            <!--<xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>-->
                                            <xsl:if test=" $thesaurusChoice != '' ">
                                                
                                                <tr align="center" id="thesaurusGroupCouple" name="thesaurusGroupCoupleName">
                                                    
                                                    <td>
                                                        <select id="selectThesaurus_ID" name="selectThesaurus" class="thesaurusFieldWidth">
                                                            <xsl:for-each select="$existingThesaurusName">
                                                                <xsl:sort select="."/>
                                                                <option>
                                                                    <xsl:if test="$thesaurusChoice = ./text()">
                                                                        <xsl:attribute name="selected">
                                                                            <xsl:text>selected</xsl:text>
                                                                        </xsl:attribute>
                                                                    </xsl:if>
                                                                    <xsl:attribute name="value">
                                                                        <xsl:value-of select="."/>
                                                                    </xsl:attribute>
                                                                    <xsl:choose>
                                                                        <xsl:when test="./text()='*'">
                                                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:value-of select="."/>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </option>
                                                            </xsl:for-each>                                
                                                        </select>
                                                    </td>
                                                    
                                                    <td>
                                                        <select id="selectUserGroup_ID" name="selectUserGroup">
                                                            <xsl:for-each select="$THEMASUsersGroupName">
                                                                <option>
                                                                    <xsl:variable name="optionVal" select="."/>
                                                                    <xsl:if test="$currentChoice = $optionVal">
                                                                        <xsl:attribute name="selected">
                                                                            <xsl:text>selected</xsl:text>
                                                                        </xsl:attribute>
                                                                    </xsl:if>
                                                                    <xsl:attribute name="value">
                                                                        <xsl:value-of select="."/>
                                                                    </xsl:attribute>
                                                                    <xsl:value-of select="$specificlocale/usergroups/node()[name()=$optionVal]/option[@lang=$lang]"/>
                                                                </option>
                                                            </xsl:for-each>
                                                        </select>
                                                    </td>
                                                    
                                                    <td>
                                                        <img width="20" height="20" border="0" onClick="RemoveThesaurusGroupInput('thesaurusGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                                            <xsl:attribute name="src">
                                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                            </xsl:attribute>
                                                            <xsl:attribute name="title">
                                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                            </xsl:attribute>
                                                        </img>
                                                    </td>

                                                <!--<xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/inthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                <xsl:choose>
                                                    <xsl:when test="./text()='*'">
                                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="."/>
                                                    </xsl:otherwise>
                                                </xsl:choose>             
                                                -->                   
                                                
                                                </tr>
                                            </xsl:if>
                                            
                                        </xsl:for-each>
                                  
                                    
                                    </tbody>
                                </table>
                                
                                <!-- Addition -->
                                
                                <hr/>

                                <table style="width:100px; vertical-align:middle;">
                                    <tr>
                                        <td style="text-align:right;">
                                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addition/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                        </td>
                                        <td>
                                            <img width="20" height="20" border="0" onClick="AddThesaurusGroupInput('thesaurusGroupBody',  'thesaurusGroupCouple');">
                                                <xsl:attribute name="src">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                                <xsl:attribute name="title">
                                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/addimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                                </xsl:attribute>
                                            </img>
                                        </td>
                                    </tr>
                                </table>
                                
                                <br/>
                                
                            </xsl:when>
                            <xsl:otherwise>
                                 <xsl:for-each select="//userForEditingInfo/thesaurus">
                                     <xsl:variable name="currentChoice" select="./@group"/>
                                     <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                     <xsl:if test=" . != '' ">
                                         <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/inthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                         <xsl:choose>
                                             <xsl:when test="./text()='*'">
                                                 <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/create/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                             </xsl:when>
                                             <xsl:otherwise>
                                                 <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                         </xsl:choose>                                
                                     </xsl:if>
                                     <br/>
                                 </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                        
            
                       
                    </td>
                </tr>
                
                <!--  Delete password check box -->
                <tr>
                    <td width="18%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/deletepassword/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <input id="deletePasswordCheckBoxId" name="deletePasswordCheckBox" type="checkbox"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <hr/>
                    </td>
                </tr>
                <!--  Delete User check box -->
                <tr>
                    <td width="18%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/edituser/deleteuser/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    
                    <td>
                        <input id="deleteUserCheckBoxId" name="deleteUserCheckBox" type="checkbox" onclick="deleteUserCheckBoxClick('deleteUserCheckBoxId')"/>
                    </td>
                </tr>
            </table>
            
            <input id="targetUserID" type="text" name="targetUser" class="hiddenInput">
                <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
            </input>
            
        </fieldset>
        
        <!-- _______________ END of fieldset ______________ -->
        <table width="100%" >
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
                
                <td valign="bottom" align="right" width="220">
                    <input type="button" class="button" onclick="Edit_User_Save_Button_Pressed( 'EditDisplays_User','edit_user_create', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload(true);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
            
        </table>
                
    </xsl:template>

<!-- _____________________________________________________________________________

    TEMPLATE FOR RENAME OF USER TO A USER THAT EXISTS IN DB BUT NOT IN WebAppUSERS.xml
    _______________________________________________________________________________-->
    <xsl:template name="rename_target_and_older_user">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <xsl:variable name="targetRenameUser" select="//targetUser"/>
        <xsl:variable name="olderRename" select="//olderUser"/>
        <fieldset id="rename_target_and_older_user_edit">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>          
            <br/>
            <table>
                <tr>
                    <td>
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/renameinfo/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input size="30" name="targetUser" disabled="disabled" class="disabledbutton" >
                            <xsl:attribute name="value">
                                <xsl:value-of select="$targetRenameUser"/>
                            </xsl:attribute>
                        </input>
                    </td>
                    <td>
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/renameto/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input size="30" name="olderUser" disabled="disabled" class="disabledbutton" >
                            <xsl:attribute name="value">
                                <xsl:value-of select="$olderRename"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/withdescription/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td colspan="3">
                        <input id="targetUserDescription_Id" type="text" style="width:90%;" name="targetDescription_User">
                            <xsl:attribute name="value">
                                <xsl:value-of select="//targetUserDescription"/>
                            </xsl:attribute>
                        </input>
                    </td>
                </tr>
            </table>
            <table>
                <tr>
                    <td colspan="2">
                        <br/>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/infomsgpart1/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        <xsl:value-of select="$olderRename"/>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/infomsgpart2/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/infomsgpart3/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                    </td>
                </tr>
                <tr>
                    <td valign="middle">
                        <input name="olderUserRenameChoice" type="radio" value="CommitInitialRenameUser" />
                    </td>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice1a/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="$targetRenameUser"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice1b/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="$olderRename"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice1c/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                    </td>
                    
                </tr>
                <tr>
                    <td valign="middle">
                        <input name="olderUserRenameChoice" type="radio" value="RenameOlderAndThenTargetUser" checked="checked"/>
                    </td>
                    <td>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice2a/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="$olderRename"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice2b/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <input size="30" id="olderUserRenameName_Id" name="olderUserRenameName" type="text" /> 
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice2c/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="$targetRenameUser"/>
                        </b>
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editusermerge/choice2d/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        <b>
                            <xsl:value-of select="$olderRename"/>
                        </b>
                        <xsl:text>.</xsl:text>
                    </td>
                </tr>
                    
            </table>
                
            <input type="text" name="targetEditField"  class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <br/>
        <table width="100%">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
                    
                <td valign="bottom" align="right" width="220">
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','rename_target_and_older_user_edit', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- _____________________________________________________________________________
                      TEMPLATE: Share_Thesaurus
    _____________________________________________________________________________ -->
    <xsl:template name="Share_Thesaurus">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <fieldset id="edit_share_thesaurus">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                <xsl:value-of select="//THEMASUserInfo/selectedThesaurus"/>
            </legend>
            <br/>
            <xsl:variable name="currentUserGroup" select="//THEMASUserInfo/userGroup"/>
                                        
            <table border="0" align="center" id="userGroupTable" style="margin:auto;">
                <tbody id="userGroupBody">
                    <!-- header -->
                    <tr class="contentHeadText" align="center" >
                        <td bgcolor="#FFFFFF"></td>
                                    
                        <td style="width:150px;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/user/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                                    
                        <td style="width:150px;">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/usergroup/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'yes'"/> </xsl:call-template>    
                        </td>
                                    
                        <td bgcolor="#FFFFFF" style="width:150px;"></td>
                                    
                        <td bgcolor="#FFFFFF"></td>
				
                    </tr>
                                
                    <!-- _________________________________ HIDDEN row in case there is none user for this thesaurus _________________________________ 
                    <xsl:if test="count(//ThesaurusUsers_Groups/user) = 0">-->
                    <tr align="center" id="userGroupCouple" name="userGroupCoupleName" class="hiddenInput" style="visibility:collapse;">
                        <!-- User -->
                        <td></td>
                        <td style="width:150px;">
                            <select id="selectUser_ID" name="selectUser" size="1" disabled="disabled">
                                <xsl:for-each select="//AllTHEMASUsers/user">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                        <!-- Group -->
                        <td style="width:150px;">
                            <select id="selectUserGroup_ID" name="selectUserGroup" disabled="disabled">
                                <xsl:for-each select="//THEMASUsersGroups/THEMASUsersGroupName">
                                    <!-- <xsl:sort select="."/> -->
                                    <!-- do not sort groups (better to be displayed with the specifies order in XML) -->
                                    <option>
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                        <xsl:variable name="currentChoice" select="."/>
                                        <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>                                
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                        <!-- icon (-) -->
                        <td>
                            <img width="20" height="20" border="0" onClick="RemoveUserGroupInput('userGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                <xsl:attribute name="src">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </xsl:attribute>
                            </img>
                                                                    
                        </td>
                        <td></td>
                    </tr>
                        
                        
                    <!-- for each user-group couple with global thesaurus access -->
                    <xsl:for-each select="//ThesaurusUsers_Groups/allThesauriUser">
                        <xsl:variable name="currentAllThesUser">
                            <xsl:value-of select="."/>
                        </xsl:variable>
                        <xsl:variable name="currentAllThesUserGroup">
                            <xsl:value-of select="./@group"/>
                        </xsl:variable>
                        <tr align="center" id="userGroupCouple" name="userGroupCoupleName">
                            <td></td>                                
                            <xsl:choose>
                                <xsl:when test="$currentUserGroup='ADMINISTRATOR'">
                                    <td>
                                        <select size="1" disabled="disabled">
                                            <xsl:for-each select="//AllTHEMASUsers/user">
                                                <xsl:sort select="."/>
                                                <option>
                                                    <xsl:if test=". = $currentAllThesUser ">
                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                    <xsl:value-of select="."/>
                                                </option>
                                            </xsl:for-each>
                                        </select>
                                    </td>
                                    <td>
                                        <select  disabled="disabled">
                                            <xsl:for-each select="//THEMASUsersGroups/THEMASUsersGroupName" >
                                                <option>                                                                                    
                                                    <xsl:if test=". = $currentAllThesUserGroup">
                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                    <xsl:variable name="currentAllThesChoice" select="."/>
                                                    <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentAllThesChoice]/option[@lang=$lang]"/>
                                                </option>
                                            </xsl:for-each>
                                        </select>
                                    </td>
                                    <!--<td>
                                        <img width="20" height="20" border="0" onClick="RemoveUserGroupInput('userGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                            <xsl:attribute name="src">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                            </xsl:attribute>
                                        </img>
                                    </td>-->
                                </xsl:when>
                                <xsl:otherwise>
                                    <td>
                                        <select size="1" disabled="disabled">
                                            <xsl:for-each select="//AllTHEMASUsers/user">
                                                <xsl:sort select="."/>
                                                <option>
                                                    <xsl:if test=". = $currentAllThesUser ">
                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                    <xsl:value-of select="."/>
                                                </option>
                                            </xsl:for-each>
                                        </select>
                                    </td>
                                    <td>
                                        <select  disabled="disabled">
                                            <xsl:for-each select="//THEMASUsersGroups/THEMASUsersGroupName" >
                                                <option>                                                                                    
                                                    <xsl:if test=". = $currentAllThesUserGroup ">
                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                    <xsl:variable name="currentAllThesChoice" select="."/>
                                                    <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentAllThesChoice]/option[@lang=$lang]"/>
                                                </option>
                                            </xsl:for-each>
                                        </select>
                                    </td>
                                    
                                                                    
                                </xsl:otherwise>
                            </xsl:choose>
                            <td style="vertical-align: bottom;">
                                <label style="font-style:italic;" diabled="disabled">
                                    <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/allThesauriDisplayText/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                </label>
                            </td>    
                            <td></td>
                        </tr>
                    </xsl:for-each>
                                        
                                        
                    <!-- for each user-group couple -->
                    <xsl:for-each select="//ThesaurusUsers_Groups/user">
                        <xsl:variable name="currentUser">
                            <xsl:value-of select="."/>
                        </xsl:variable>
                        <xsl:variable name="currentUserGroup">
                            <xsl:value-of select="./@group"/>
                        </xsl:variable>
                        <tr align="center" id="userGroupCouple" name="userGroupCoupleName">
                            <!-- Χρήστης -->
                            <td></td>
                            <td>
                                <select id="selectUser_ID" name="selectUser" size="1">
                                    <xsl:for-each select="//AllTHEMASUsers/user">
                                        <xsl:sort select="."/>
                                        <option>
                                            <xsl:if test=". = $currentUser ">
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                            <xsl:value-of select="."/>
                                        </option>
                                    </xsl:for-each>
                                </select>
                            </td>
                            <!-- Γκρουπ -->
                            <td>
                                <select id="selectUserGroup_ID" name="selectUserGroup">
                                    <xsl:for-each select="//THEMASUsersGroups/THEMASUsersGroupName">
                                        <!-- <xsl:sort select="."/> -->
                                        <!-- do not sort groups (better to be displayed with the specifies order in XML) -->
                                        <option>
                                                                                    
                                            <xsl:if test=". = $currentUserGroup ">
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                            <xsl:variable name="currentChoice" select="."/>
                                            <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                        </option>
                                    </xsl:for-each>
                                </select>
                            </td>
                            <!-- icon (-) -->
                            <td>
                                <img width="20" height="20" border="0" onClick="RemoveUserGroupInput('userGroupTable', this.parentNode.parentNode)" name="MinusIcon">
                                    <xsl:attribute name="src">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/removeimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                                    </xsl:attribute>
                                </img>
                            </td>
                            <td></td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
            <!-- Add row: -->
            <hr/>
            <table border="0" align="center" id="TableWithPlusIconId" style="margin:auto;">
                <tr>
                    <td/>
                    <td width="50%" align="right" valign="middle">
                        <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/addition/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>                                                    
                    </td>
                    <td width="50%" align="left" valign="middle">
                        <img width="20" height="20" border="0" onClick="AddUserGroupInput('userGroupBody',  'userGroupCouple');"  align="bottom">
                            <xsl:attribute name="src">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/addimage/src/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/sharethesaurus/addimage/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                            </xsl:attribute>
                        </img>
                    </td>
                    <td/>
                </tr>
            </table>
            <input type="text" name="targetUser" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetUser"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <!-- _______________ END of fieldset ______________ -->
        <table width="100%">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','edit_share_thesaurus', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
					&#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
        
    <!-- _____________________________________________________________________________
                      TEMPLATE: Edit_Password
    _____________________________________________________________________________ -->
    <xsl:template name="Edit_Password">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <fieldset id="edit_password">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editpassword/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <!--  Παλιός Κωδικός: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editpassword/oldcode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="oldUserPassword_Id" type="password" size="30" name="oldUserPassword">
                        </input>
                    </td>
                </tr>
                <!--  Νέος Κωδικός: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editpassword/newcode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="newUserPassword_Id1" type="password" size="30" name="newUserPassword1">
                        </input>
                    </td>
                </tr>
                <!--  Επιβεβαίωση Κωδικού: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editpassword/confirmcode/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <input id="newUserPassword_Id2" type="password" size="30" name="newUserPassword2">
                        </input>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetUser" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetUser"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <!-- _______________ END of fieldset ______________ -->
        <table width="100%">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','edit_password', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalsavebutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
					&#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- _____________________________________________________________________________
                      TEMPLATE: Edit_Thesaurus
    _____________________________________________________________________________ -->
    <xsl:template name="Edit_Thesaurus">
        <xsl:param name="specificlocale"/>
        <xsl:param name="lang" />
        <xsl:variable name="currentUserThesaurus">
            <xsl:value-of select="//THEMASUserInfo/selectedThesaurus"/>
        </xsl:variable>
        <fieldset id="edit_thesaurus">
            <legend>
                <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editthesaurus/title/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    				
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <!--  Επιλέξτε Θησαυρό: -->
                <tr>
                    <td width="150" align="right">
                        <b>
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editthesaurus/selectthesaurus/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </b>
                    </td>
                    <td>
                        <select id="selectThesaurus_ID" name="selectThesaurus" size="1" class="thesaurusFieldWidth">
                            <xsl:for-each select="//userForEditingThesaurusInfo/thesaurusOfUser/thesaurus">
                                <xsl:sort select="."/>
                                <option>
                                    <xsl:if test=". = $currentUserThesaurus ">
                                        <xsl:attribute name="selected">
                                            <xsl:text>selected</xsl:text>
                                        </xsl:attribute>
                                    </xsl:if>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </option>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>
            </table>
            <input type="text" name="targetUser" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetUser"/>
                </xsl:attribute>
            </input>
            <input type="text" name="targetEditField" class="hiddenInput">
                <xsl:attribute name="value">
                    <xsl:value-of select="//targetEditField"/>
                </xsl:attribute>
            </input>
        </fieldset>
        <!-- _______________ END of fieldset ______________ -->
        <table width="100%">
            <tr>
                <td id="resultOf_Edit">
                    <br/>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="right">
                    <input type="button" class="button" onclick="getServletResult( 'EditDisplays_User','edit_thesaurus', '','selectedIndexOnly')">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/editthesaurus/okbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
					&#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:call-template name="getTranslationMessage"> <xsl:with-param name="targetLangElements" select="$specificlocale/generalcancelbutton/option"/> <xsl:with-param name="targetLang" select="$lang"/> <xsl:with-param name="disableEscape" select="'no'"/> </xsl:call-template>    
                        </xsl:attribute>
                    </input>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
