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
                <xsl:value-of select="$specificlocale/create/title/option[@lang=$lang]"/>
            </legend>
            <br/>
            
            <table border="0" align="center">
                
                <!--  UserName: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/usernameprompt/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserName_Id" type="text" size="30" name="newUserName">
                            <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
                        </input>
                        <i>
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/usernamenote/option[@lang=$lang]"/>
                        </i>
                    </td>
                </tr>
                
                <!--  Password: -->
                <tr>
                    <td width="15%" align="right">
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/passwordprompt/option[@lang=$lang]"/>
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserPassword_Id" type="text" size="30" name="newPassword_User"/>
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/passwordnote/option[@lang=$lang]"/>
                    </td>
                </tr>
                
                <!--  User Description: -->
                <tr>                    
                    <td width="15%" align="right">
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/descriptionprompt/option[@lang=$lang]"/>
                    </td>
                    
                    <td style="color:#898a5e; font-size:9px;">
                        <input id="newUserDescription_Id" type="text" size="60" name="newDescription_User"/>
                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/descriptionnote/option[@lang=$lang]"/>
                    </td>
                </tr>
                
                <xsl:if test="$THEMASUserInfo_userGroup = 'ADMINISTRATOR' ">
                    <tr>
                        <td width="15%" align="right">
                            <b>
                                <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/adminprompt/option[@lang=$lang]"/>
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
                        <td style="width:160px;">
                            <b>
                                <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/thesaurus/option[@lang=$lang]"/>
                            </b>
                        </td>
                        
                        <td style="width:160px;">
                            <b>
                                <xsl:value-of disable-output-escaping="yes" select="$specificlocale/create/usergroup/option[@lang=$lang]"/>
                            </b>
                        </td>
                        
                        <td bgcolor="#FFFFFF" style="width:50px;"></td>
                        <td bgcolor="#FFFFFF" ></td>
                    </tr>
                    
                    <tr align="center" id="thesaurusGroupCouple" name="thesaurusGroupCoupleName">
                        <!-- Thesaurus -->
                        <td bgcolor="#FFFFFF" ></td>
                        <td >
                            <select id="selectThesaurus_ID" name="selectThesaurus" style="width:160px;">
                                <xsl:for-each select="$existingThesaurusName">
                                    <xsl:sort select="."/>
                                    <option>
                                        <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </option>
                                </xsl:for-each>                                
                            </select>
                        </td>
                        
                        <!-- Group -->
                        <td >
                            <select id="selectUserGroup_ID" name="selectUserGroup">
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
                                    <xsl:value-of select="$specificlocale/create/removeimage/src/option[@lang=$lang]"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$specificlocale/create/removeimage/title/option[@lang=$lang]"/>
                                </xsl:attribute>
                            </img>
                        </td>
                        <td bgcolor="#FFFFFF" ></td>
                    </tr>
                    
                </tbody>
                
            </table>
          
            <!-- Προσθήκη: -->
            <hr/>
  
            
            <table border="0" width="100%" id="TableWithPlusIconId">
                <tr  >
                    <td width="50%" align="right" valign="middle">
                        <xsl:value-of select="$specificlocale/create/addition/option[@lang=$lang]"/>
                        </td>
         
                    <td width="50%" align="left" valign="middle">
                        <img width="20" height="20" border="0" onClick="AddThesaurusGroupInput('thesaurusGroupBody',  'thesaurusGroupCouple');">
                            <xsl:attribute name="src">
                                <xsl:value-of select="$specificlocale/create/addimage/src/option[@lang=$lang]"/>
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:value-of select="$specificlocale/create/addimage/title/option[@lang=$lang]"/>
                            </xsl:attribute>
                        </img>
                    </td>
                    
                </tr>
                
            </table>
            <input type="text" name="targetEditField" style="visibility:hidden;">
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
                            <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
               <xsl:value-of select="$specificlocale/createmerge/title/option[@lang=$lang]"/>
           </legend>          
                <br/>
                <table border="0" width="100%" align="center">
                    <tr>
                        <td width="35%" align="right">
                            <b>
                                <xsl:value-of select="$specificlocale/createmerge/usernameinfo/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <td>
                            <input id="newUserName_Id" type="text" size="30" name="newUserName" disabled="disabled" class="disabledbutton" >
                                <xsl:attribute name="value"><xsl:value-of select="//newUserInfo/name"/></xsl:attribute>
                            </input>
                            <b>
                                <xsl:value-of select="$specificlocale/createmerge/additionalfields/option[@lang=$lang]"/>
                            </b>
                        </td>
                        </tr>
            
                
                <!--  Κωδικός Χρήστη: -->
                <tr>
                    <td width="35%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/createmerge/password/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$specificlocale/createmerge/description/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td>
                        <input id="newUserDescription_Id" type="text" size="60" name="newDescription_User" >
                            <xsl:attribute name="value"><xsl:value-of select="//newUserInfo/description"/></xsl:attribute>
                        </input>
                    </td>
                </tr>
                
                <!--  Ρόλοι: -->
                <tr>
                    <td width="35%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/createmerge/groups/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td>
                        <xsl:for-each select="//newUserInfo/thesaurus">
                                <xsl:variable name="currentChoice" select="./@group"/>
                                <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                                <xsl:if test=" . != '' ">
                                <xsl:value-of select="$specificlocale/createmerge/inthesaurus/option[@lang=$lang]"/>
                                <xsl:value-of select="."/>
                                <input name="selectThesaurus" size="2" type="text" style="visibility:hidden; height:5px;">
                                    <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
                                </input>
                            </xsl:if>
                            <input name="selectUserGroup" size="2" type="text" style="visibility:hidden; height:5px;"><xsl:attribute name="value"><xsl:value-of select="./@group"/></xsl:attribute></input>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
                
            </table>
            <hr/>
            <table>
                    <tr>
                        <td colspan="2">
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/createmerge/infomsgpart1/option[@lang=$lang]"/>
                            <xsl:value-of select="//newUserInfo/name"/>
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/createmerge/infomsgpart2/option[@lang=$lang]"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/createmerge/infomsgpart3/option[@lang=$lang]"/>
                        </td>
                    </tr>
                    <tr>
                        <td valign="middle">
                            <input name="olderUserCreateChoice" type="radio" value="CommitInitialCreateUser" />
                        </td>
                        <td>
                            
                            <xsl:value-of select="$specificlocale/createmerge/choice1/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$specificlocale/createmerge/choice2a/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="//newUserInfo/name"/>
                            </b>
                            <xsl:value-of select="$specificlocale/createmerge/choice2b/option[@lang=$lang]"/>
                            <input size="30" id="olderUserRenameName_Id" name="olderUserRenameName" type="text" />
                            <xsl:value-of select="$specificlocale/createmerge/choice2c/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                         <xsl:attribute name="value">
                            <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
                <xsl:value-of select="$specificlocale/edituser/title/option[@lang=$lang]"/>
            </legend>
            <br/>
            <table border="0" width="100%" align="center">
                <!--  Όνομα Χρήστη για Login: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/edituser/username/option[@lang=$lang]"/>
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
                
                <!--  Περιγραφή: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/edituser/description/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td>
                        <input id="newUserDescription_Id" type="text" size="60" name="newDescription_User">
                            <xsl:attribute name="value"><xsl:value-of select="//userForEditingInfo/description"/></xsl:attribute>
                        </input>
                    </td>
                </tr>
                
                <!--  Ρόλοι: -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/edituser/groups/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td>
                        <xsl:for-each select="//userForEditingInfo/thesaurus">
                            <xsl:variable name="currentChoice" select="./@group"/>
                            <xsl:value-of select="$specificlocale/usergroups/node()[name()=$currentChoice]/option[@lang=$lang]"/>
                            <xsl:if test=" . != '' ">
                                <xsl:value-of select="$specificlocale/edituser/inthesaurus/option[@lang=$lang]"/>
                                <xsl:value-of select="."/>
                            </xsl:if>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
                
                <!--  Διαγραφή Κωδικού check box -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/edituser/deletepassword/option[@lang=$lang]"/>
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
                
                <!--  Διαγραφή Χρήστη check box -->
                <tr>
                    <td width="15%" align="right">
                        <b>
                            <xsl:value-of select="$specificlocale/edituser/deleteuser/option[@lang=$lang]"/>
                        </b>
                    </td>
                    
                    <td>
                        <input id="deleteUserCheckBoxId" name="deleteUserCheckBox" type="checkbox" onclick="deleteUserCheckBoxClick('deleteUserCheckBoxId')"/>
                    </td>
                </tr>
                
            </table>
            
            <input id="targetUserID" type="text" name="targetUser" style="visibility:hidden;">
                <xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
            </input>
            <input type="text" name="targetEditField" style="visibility:hidden;">
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
                            <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="window.location.reload(true);">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
                    <xsl:value-of select="$specificlocale/editusermerge/title/option[@lang=$lang]"/>
                </legend>          
                <br/>
                <table>
                    <tr>
                        <td>
                            <b>
                                <xsl:value-of select="$specificlocale/editusermerge/renameinfo/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <td>
                            <input size="30" name="targetUser" disabled="disabled" class="disabledbutton" >
                                <xsl:attribute name="value"><xsl:value-of select="$targetRenameUser"/></xsl:attribute>
                            </input>
                        </td>
                        <td>
                            <b>
                                <xsl:value-of select="$specificlocale/editusermerge/renameto/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <td>
                            <input size="30" name="olderUser" disabled="disabled" class="disabledbutton" >
                                <xsl:attribute name="value"><xsl:value-of select="$olderRename"/></xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <b>
                                <xsl:value-of select="$specificlocale/editusermerge/withdescription/option[@lang=$lang]"/>
                            </b>
                        </td>
                        <td colspan="3">
                            <input id="targetUserDescription_Id" type="text" size="60" name="targetDescription_User">
                                <xsl:attribute name="value"><xsl:value-of select="//targetUserDescription"/></xsl:attribute>
                            </input>
                        </td>
                    </tr>
                </table>
                <table>
                    <tr>
                        <td colspan="2">
                            <br/>
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/editusermerge/infomsgpart1/option[@lang=$lang]"/>
                            <xsl:value-of select="$olderRename"/>
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/editusermerge/infomsgpart2/option[@lang=$lang]"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <xsl:value-of disable-output-escaping="yes" select="$specificlocale/editusermerge/infomsgpart3/option[@lang=$lang]"/>
                        </td>
                    </tr>
                    <tr>
                        <td valign="middle">
                            <input name="olderUserRenameChoice" type="radio" value="CommitInitialRenameUser" />
                        </td>
                        <td>
                            <xsl:value-of select="$specificlocale/editusermerge/choice1a/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="$targetRenameUser"/>
                            </b>
                            <xsl:value-of select="$specificlocale/editusermerge/choice1b/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="$olderRename"/>
                            </b>
                            <xsl:value-of select="$specificlocale/editusermerge/choice1c/option[@lang=$lang]"/>
                        </td>
                    
                    </tr>
                    <tr>
                        <td valign="middle">
                            <input name="olderUserRenameChoice" type="radio" value="RenameOlderAndThenTargetUser" checked="checked"/>
                        </td>
                        <td>
                            <xsl:value-of select="$specificlocale/editusermerge/choice2a/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="$olderRename"/>
                            </b>
                            <xsl:value-of select="$specificlocale/editusermerge/choice2b/option[@lang=$lang]"/>
                            <input size="30" id="olderUserRenameName_Id" name="olderUserRenameName" type="text" /> 
                            <xsl:value-of select="$specificlocale/editusermerge/choice2c/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="$targetRenameUser"/>
                            </b>
                            <xsl:value-of select="$specificlocale/editusermerge/choice2d/option[@lang=$lang]"/>
                            <b>
                                <xsl:value-of select="$olderRename"/>
                            </b>
                            <xsl:text>.</xsl:text>
                        </td>
                    </tr>
                    
                </table>
                
                <input type="text" name="targetEditField" style="visibility:hidden;">
                    <xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
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
                            <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                        </xsl:attribute>
                    </input>
                    &#160;
                    <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
				<xsl:value-of select="$specificlocale/sharethesaurus/title/option[@lang=$lang]"/>
				<xsl:value-of select="//THEMASUserInfo/selectedThesaurus"/>
			</legend>
			<br/>
			<table border="0" align="center" id="userGroupTable" style="margin:auto;">
                            <tbody id="userGroupBody">
                                <!-- header -->
                                <tr class="contentHeadText" align="center" >
                                    <td bgcolor="#FFFFFF"></td>
                                    
                                    <td style="width:150px;">
                                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/sharethesaurus/user/option[@lang=$lang]"/>
                                    </td>
                                    
                                    <td style="width:150px;">
                                        <xsl:value-of disable-output-escaping="yes" select="$specificlocale/sharethesaurus/usergroup/option[@lang=$lang]"/>
                                    </td>
                                    
                                    <td bgcolor="#FFFFFF" style="width:50px;"></td>
                                    
                                    <td bgcolor="#FFFFFF"></td>
				
                                </tr>
                                
					<!-- _________________________________ HIDDEN row in case there is none user for this thesaurus _________________________________ -->
					<xsl:if test="count(//ThesaurusUsers_Groups/user) = 0">
						<tr align="center" id="userGroupCouple" name="userGroupCoupleName" style="visibility:hidden;">
							<!-- Χρήστης -->
                                                        <td></td>
							<td style="width:150px;">
								<select id="selectUser_ID" name="selectUser" size="1">
									<xsl:for-each select="//AllTHEMASUsers/user">
										<xsl:sort select="."/>
										<option>
											<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
											<xsl:value-of select="."/>
										</option>
									</xsl:for-each>
								</select>
							</td>
							<!-- Γκρουπ -->
							<td style="width:150px;">
								<select id="selectUserGroup_ID" name="selectUserGroup">
									<xsl:for-each select="//THEMASUsersGroups/THEMASUsersGroupName">
										<!-- <xsl:sort select="."/> -->
										<!-- do not sort groups (better to be displayed with the specifies order in XML) -->
										<option>
											<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
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
                                                                        <xsl:value-of select="$specificlocale/sharethesaurus/removeimage/src/option[@lang=$lang]"/>
                                                                    </xsl:attribute>
                                                                    <xsl:attribute name="title">
                                                                        <xsl:value-of select="$specificlocale/sharethesaurus/removeimage/title/option[@lang=$lang]"/>
                                                                    </xsl:attribute>
                                                                </img>
                                                                    
							</td>
                                                        <td></td>
						</tr>
					</xsl:if>
                                        
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
											<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
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
											<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
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
                                                                        <xsl:value-of select="$specificlocale/sharethesaurus/removeimage/src/option[@lang=$lang]"/>
                                                                    </xsl:attribute>
                                                                    <xsl:attribute name="title">
                                                                        <xsl:value-of select="$specificlocale/sharethesaurus/removeimage/title/option[@lang=$lang]"/>
                                                                    </xsl:attribute>
                                                                </img>
							</td>
                                                        <td></td>
						</tr>
					</xsl:for-each>
				</tbody>
			</table>
			<!-- Προσθήκη: -->
			<hr/>
			<table border="0" align="center" id="TableWithPlusIconId" style="margin:auto;">
				<tr>
                                    <td/>
					<td width="50%" align="right" valign="middle">
						<xsl:value-of select="$specificlocale/sharethesaurus/addition/option[@lang=$lang]"/>                                                
					</td>
					<td width="50%" align="left" valign="middle">
						<img width="20" height="20" border="0" onClick="AddUserGroupInput('userGroupBody',  'userGroupCouple');"  align="bottom">
                                                    <xsl:attribute name="src">
                                                        <xsl:value-of select="$specificlocale/sharethesaurus/addimage/src/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="title">
                                                        <xsl:value-of select="$specificlocale/sharethesaurus/addimage/title/option[@lang=$lang]"/>
                                                    </xsl:attribute>
                                                </img>
					</td>
					<td/>
				</tr>
			</table>
			<input type="text" name="targetUser" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
			</input>
			<input type="text" name="targetEditField" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
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
                                                <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </input>
					&#160;
                                        <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
                                <xsl:value-of select="$specificlocale/editpassword/title/option[@lang=$lang]"/>
			</legend>
			<br/>
			<table border="0" width="100%" align="center">
				<!--  Παλιός Κωδικός: -->
				<tr>
					<td width="15%" align="right">
						<b>
							<xsl:value-of select="$specificlocale/editpassword/oldcode/option[@lang=$lang]"/>
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
							<xsl:value-of select="$specificlocale/editpassword/newcode/option[@lang=$lang]"/>
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
							<xsl:value-of select="$specificlocale/editpassword/confirmcode/option[@lang=$lang]"/>
						</b>
					</td>
					<td>
						<input id="newUserPassword_Id2" type="password" size="30" name="newUserPassword2">
						</input>
					</td>
				</tr>
			</table>
			<input type="text" name="targetUser" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
			</input>
			<input type="text" name="targetEditField" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
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
                                                <xsl:value-of select="$specificlocale/generalsavebutton/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </input>
					&#160;
                                        <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
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
                            <xsl:value-of select="$specificlocale/editthesaurus/title/option[@lang=$lang]"/>				
			</legend>
			<br/>
			<table border="0" width="100%" align="center">
				<!--  Επιλέξτε Θησαυρό: -->
				<tr>
					<td width="150" align="right">
						<b>
							<xsl:value-of select="$specificlocale/editthesaurus/selectthesaurus/option[@lang=$lang]"/>
						</b>
					</td>
					<td>
						<select id="selectThesaurus_ID" name="selectThesaurus" size="1" style="width:160px;">
							<xsl:for-each select="//userForEditingThesaurusInfo/thesaurusOfUser/thesaurus">
								<xsl:sort select="."/>
								<option>
									<xsl:if test=". = $currentUserThesaurus ">
										<xsl:attribute name="selected">
                                                                                    <xsl:text>selected</xsl:text>
                                                                                </xsl:attribute>
									</xsl:if>
									<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</table>
			<input type="text" name="targetUser" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetUser"/></xsl:attribute>
			</input>
			<input type="text" name="targetEditField" style="visibility:hidden;">
				<xsl:attribute name="value"><xsl:value-of select="//targetEditField"/></xsl:attribute>
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
                                                <xsl:value-of select="$specificlocale/editthesaurus/okbutton/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </input>
					&#160;
                                        <input type="button" class="button" onclick="cancelAction();DisplayPleaseWaitScreen(false);">
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$specificlocale/generalcancelbutton/option[@lang=$lang]"/>
                                            </xsl:attribute>
                                        </input>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
