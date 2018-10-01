/* 
 * Copyright 2015 Institute of Computer Science,
 *                Foundation for Research and Technology - Hellas.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 * 
 * =============================================================================
 * Contact: 
 * =============================================================================
 * Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
 *     Tel: +30-2810-391632
 *     Fax: +30-2810-391638
 *  E-mail: isl@ics.forth.gr
 * WebSite: http://www.ics.forth.gr/isl/cci.html
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package Servlets;

import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Locale;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
 EditDisplays_User
 -----------------------------------------------------------------------
 servlet called for handling the Users creation / editing
 ----------------------------------------------------------------------*/
public class EditDisplays_User extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
     processRequest()
     ----------------------------------------------------------------------*/
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();

        try {
            Utilities u = new Utilities();

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");                
                return;
            }

            // get servlet parameter targetEditField:
            // 1. "user_create" when called by "New User" icon
            // 2. "save_user_create" when called by "Save" button for a new user creation
            // 3. "user_edit" when called by "Edit User" icon
            // 4. "save_user_edit" when called by "Save" button for a user editing
            // 5. "rename_target_and_older_user_edit" when called by Save Button of "Edit User" popUpCard na d new name given exists in DB but not in UsersClass.xml
            // 6. "save_rename_target_and_older_user_edit" when called by "Save" button for case 5
            // 7. "share_thesaurus" when called for sharing current thesaurus to users
            // 8. "save_share_thesaurus" when called by "Save" button for sharing current thesaurus to users
            // 9. "change_password" when called for changing current user's password
            // 10. "save_change_password" when called by "Save" button for changing current user's password
            // 11. "change_thesaurus" when called for changing current user's thesaurus
            // 12. "save_change_thesaurus" when called by "OK" button for changing current user's thesaurus
            String targetEditField = u.getDecodedParameterValue(request.getParameter("targetEditField"));

            // get the set of thesaurus owned by current user
            //UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            ArrayList<String> thesaurusVector = new ArrayList<String>();
            UsersClass tmsUsers = new UsersClass();
            thesaurusVector = tmsUsers.GetThesaurusSetOfTMSUser(request, SessionUserInfo.name);

            // case 1. called by "New User" icon 
            if (targetEditField.equals("user_create")) {
                OpenCardForUserCreation(context, sessionInstance, out, thesaurusVector);
            } // case 2. called by "Save" button for a new user creation 
            else if (targetEditField.equals("save_user_create")) {
                CreateUser(request, sessionInstance, out, SessionUserInfo.UILang);
            } // case 3. Called after Save button for user creation when user name for creation exists
            else if (targetEditField.equals("user_create_and_merge_with_older")) {
                OpenCardForUserCreationAndMergeWithOlder(request, context, sessionInstance, out);
            } // case 4. Called after Save button of case 3.
            else if (targetEditField.equals("save_user_create_and_merge_with_older")) {
                CreateUserAndHandleOlderUserName(request, sessionInstance, out, SessionUserInfo.UILang);
            } // case 5. called by "Edit User" icon 
            else if (targetEditField.equals("user_edit")) {
                String user = u.getDecodedParameterValue(request.getParameter("user"));
                OpenCardForUserEditing(request, context, sessionInstance, out, user, thesaurusVector);
            } // case 6. called by "Save" button for a user editing
            else if (targetEditField.equals("save_user_edit")) {
                EditUser(request, sessionInstance, out);
            } // case 7. called by "Edit User" icon 
            else if (targetEditField.equals("rename_target_and_older_user_edit")) {
                String targetUser = u.getDecodedParameterValue(request.getParameter("targetUser"));
                String targetUserDescription = u.getDecodedParameterValue(request.getParameter("newDescription_User"));
                String olderUser = u.getDecodedParameterValue(request.getParameter("newName_User"));

                OpenCardForTargetAndOlderUserRename(request, context, sessionInstance, out, targetUser, targetUserDescription, olderUser);
            } // case 8. called by "Save" button for a user editing
            else if (targetEditField.equals("save_rename_target_and_older_user_edit")) {
                EditTargetAndOlderUser(request, sessionInstance, out, SessionUserInfo.UILang);
            } // case 9. called for sharing current thesaurus to users
            else if (targetEditField.equals("share_thesaurus")) {
                OpenCardForSharingThesaurus(request, context, sessionInstance, out);
            } // case 10. called by "Save" button for sharing current thesaurus to users
            else if (targetEditField.equals("save_share_thesaurus")) {
                ShareThesaurus(request, sessionInstance, out);
            } // case 11. called for editing current user's password 
            else if (targetEditField.equals("change_password")) {
                OpenCardForUserPasswordEditing(request, context, sessionInstance, out);
            } // case 12. called by "Save" button for changing current user's password 
            else if (targetEditField.equals("save_change_password")) {
                EditUserPassword(request, out,SessionUserInfo.UILang);
            } // case 13. called for editing current user's thesaurus 
            else if (targetEditField.equals("change_thesaurus")) {
                OpenCardForUserThesaurusEditing(request, context, sessionInstance, out);
            } // case 14. called by "OK" button for changing current user's thesaurus
            else if (targetEditField.equals("save_change_thesaurus")) {
                EditUserThesaurus(request, session, sessionInstance, out, SessionUserInfo.UILang);
            }
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    public void OpenCardForUserCreationAndMergeWithOlder(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out) throws UnsupportedEncodingException {

        Utilities u = new Utilities();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        StringBuffer xml = new StringBuffer();
        UsersClass tmsUsers = new UsersClass();
        // get input parameters
        String newName_User = u.getDecodedParameterValue(request.getParameter("newUserName"));
        String newPassword_User = u.getDecodedParameterValue(request.getParameter("newPassword_User"));
        String newDescription_User = u.getDecodedParameterValue(request.getParameter("newDescription_User"));
        String administratorCheckBox = u.getDecodedParameterValue(request.getParameter("administratorCheckBox"));
        boolean createNewUserAsAdministrator = (administratorCheckBox != null);
        String[] selectThesaurusArray = null;
        ArrayList<String> selectThesaurusVector = new ArrayList<String>();
        String[] selectUserGroupArray = null;
        ArrayList<String> selectUserGroupVector = new ArrayList<String>();
        // in case of no Administrator creation/editing get the list of thesaurus-groups
        if (createNewUserAsAdministrator == false) {
            selectThesaurusArray = request.getParameterValues("selectThesaurus");
            if (selectThesaurusArray != null) {
                int selectThesaurusArrayLen = selectThesaurusArray.length;
                for (int i = 0; i < selectThesaurusArrayLen; i++) {
                    selectThesaurusVector.add(u.getDecodedParameterValue(selectThesaurusArray[i]));
                }
                selectUserGroupArray = request.getParameterValues("selectUserGroup");
                int selectUserGroupArrayLen = selectUserGroupArray.length;
                for (int i = 0; i < selectUserGroupArrayLen; i++) {
                    selectUserGroupVector.add(u.getDecodedParameterValue(selectUserGroupArray[i]));
                }
            }
        } else {
            selectThesaurusVector.add("");
            selectUserGroupVector.add(ConstantParameters.Group_Administrator);
        }

        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + "save_user_create_and_merge_with_older" + "</targetEditField>"); // user_create / user_edit
        xml.append("<newUserInfo>");
        xml.append("<name>" + Utilities.escapeXML(newName_User) + "</name>");
        xml.append("<password>" + Utilities.escapeXML(newPassword_User) + "</password>");
        int thesaurusNamesStoredSize = selectThesaurusVector.size();
        for (int i = 0; i < thesaurusNamesStoredSize; i++) {
            String thesaurusName = (String) selectThesaurusVector.get(i);
            String thesaurusGroup = (String) selectUserGroupVector.get(i);
            // group_translated=\"" + tmsUsers.translateGroup(thesaurusGroup) + "\"
            xml.append("<thesaurus group=\"" + thesaurusGroup + "\">" + thesaurusName + "</thesaurus>");
        }
        xml.append("<description>" + Utilities.escapeXML(newDescription_User) + "</description>");
        xml.append("</newUserInfo>");

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /**
     * 
     * @param request
     * @param context
     * @param sessionInstance
     * @param out 
     */
    private void OpenCardForSharingThesaurus(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out) {
        Utilities u = new Utilities();
        // write XML output
        StringBuffer xml = new StringBuffer();
        writeXMLForSharingThesaurus(request, sessionInstance, xml, "save_share_thesaurus");
        // transform XML output
        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /*---------------------------------------------------------------------
     writeXMLForSharingThesaurus()
     ----------------------------------------------------------------------*/
    private void writeXMLForSharingThesaurus(HttpServletRequest request, SessionWrapperClass sessionInstance, StringBuffer xml, String targetField) {
        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();
        // get the thesaurus currently selected by current user
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String CurrentThesaurus = SessionUserInfo.selectedThesaurus;

        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + targetField + "</targetEditField>"); // user_create / user_edit
        xml.append("<targetUser></targetUser>");

        // <AllTHEMASUsers>
        xml.append("<AllTHEMASUsers>");
        // print all existing users
        ArrayList<String> TMSUsersNames = new ArrayList<String>();
        TMSUsersNames = tmsUsers.GetTMSUsersNames(request);
        int TMSUsersNamesSize = TMSUsersNames.size();
        for (int i = 0; i < TMSUsersNamesSize; i++) {
            xml.append("<user>" + (String) TMSUsersNames.get(i) + "</user>");
        }
        xml.append("</AllTHEMASUsers>");
        // <THEMASUsersGroups>
        xml.append("<THEMASUsersGroups>");
        for (String group : tmsUsers.UsersGroups) { // do not display ADMINISTRATOR
            if(group.equals(ConstantParameters.Group_Administrator)){
                continue;
            }
            // tr=\"" + tmsUsers.UsersGroupsGR[i] + "\"
            xml.append("<THEMASUsersGroupName>" +group + "</THEMASUsersGroupName>");
        }
        xml.append("</THEMASUsersGroups>");

        // <ThesaurusUsers_Groups>
        xml.append("<ThesaurusUsers_Groups>");
        // print all existing users - groups of current thesaurus
        ArrayList<String> UserNamesV = new ArrayList<>();
        ArrayList<String> GroupsV = new ArrayList<>();
        ArrayList<String> allThesauriUserNamesV = new ArrayList<>();
        ArrayList<String> allThesauriGroupsV = new ArrayList<>();
        
        tmsUsers.GetTMSUsers_GroupsOfThesaurus(request, CurrentThesaurus, UserNamesV, GroupsV);
        tmsUsers.GetTMSUsers_GroupsOfThesaurus(request, ConstantParameters.AllThesauriIndicator, allThesauriUserNamesV, allThesauriGroupsV);
        
        int allThesUserNamesVSize = allThesauriUserNamesV.size();
        for (int i = 0; i < allThesUserNamesVSize; i++) {
            xml.append("<allThesauriUser group=\"" + (String) allThesauriGroupsV.get(i) + "\">" + (String) allThesauriUserNamesV.get(i) + "</allThesauriUser >");
        }
        int UserNamesVSize = UserNamesV.size();
        for (int i = 0; i < UserNamesVSize; i++) {
            // group_translated=\"" + tmsUsers.translateGroup((String) GroupsV.get(i)) + "\"
            xml.append("<user group=\"" + (String) GroupsV.get(i) + "\">" + (String) UserNamesV.get(i) + "</user>");
        }
        xml.append("</ThesaurusUsers_Groups>");

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");
    }

    /*---------------------------------------------------------------------
     OpenCardForUserEditing()
     ----------------------------------------------------------------------*/
    private void OpenCardForUserEditing(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out, String user, ArrayList thesaurusVector) {
        Utilities u = new Utilities();

        // write XML output
        StringBuffer xml = new StringBuffer();
        writeXMLForUserEditing(request, sessionInstance, xml, "save_user_edit", user,thesaurusVector);
        // transform XML output
        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /*---------------------------------------------------------------------
     writeXMLForUserEditing()
     ----------------------------------------------------------------------*/
    private void writeXMLForUserEditing(HttpServletRequest request, SessionWrapperClass sessionInstance, StringBuffer xml, String targetField, String targetUser, ArrayList thesaurusVector) {
        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + targetField + "</targetEditField>"); // user_create / user_edit
        xml.append("<targetUser>" + targetUser + "</targetUser>");

        UserInfoClass userForEditingInfo = tmsUsers.SearchTMSUser(request, targetUser);

        if (userForEditingInfo != null) {
            String userNameStored = userForEditingInfo.name;
            String passwordStored = userForEditingInfo.password;
            String descriptionStored = userForEditingInfo.description;
            ArrayList thesaurusNamesStored = userForEditingInfo.thesaurusNames;
            ArrayList thesaurusGroupsStored = userForEditingInfo.thesaurusGroups;
            xml.append("<userForEditingInfo>");
            xml.append("<name>" + userNameStored + "</name>");
            xml.append("<password>" + passwordStored + "</password>");
            int thesaurusNamesStoredSize = thesaurusNamesStored.size();
            for (int i = 0; i < thesaurusNamesStoredSize; i++) {
                String thesaurusName = (String) thesaurusNamesStored.get(i);
                String thesaurusGroup = (String) thesaurusGroupsStored.get(i);
                // group_translated=\"" + tmsUsers.translateGroup(thesaurusGroup) + "\"
                xml.append("<thesaurus group=\"" + thesaurusGroup + "\">" + thesaurusName + "</thesaurus>");
            }
            xml.append("<description>" + descriptionStored + "</description>");
            xml.append("</userForEditingInfo>");
        }
        
        
        //only administrator will be given the edit user mappings right
        if(SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator)){
            xml.append("<existingThesaurus>");
            xml.append("<existingThesaurusName>" + Utils.ConstantParameters.AllThesauriIndicator + "</existingThesaurusName>");
        
            int thesaurusVectorSize = thesaurusVector.size();
            for (int i = 0; i < thesaurusVectorSize; i++) {
                xml.append("<existingThesaurusName>" + (String) thesaurusVector.get(i) + "</existingThesaurusName>");
            }
            xml.append("</existingThesaurus>");
            
            // <THEMASUsersGroups>
            xml.append("<THEMASUsersGroups>");
            for (String group : tmsUsers.UsersGroups) { // do not display ADMINISTRATOR
                if(group.equals(ConstantParameters.Group_Administrator)){
                    continue;
                }
                // tr=\"" + tmsUsers.UsersGroupsGR[i] + "\"
                xml.append("<THEMASUsersGroupName>" + group + "</THEMASUsersGroupName>");
            }
            xml.append("</THEMASUsersGroups>");
        }
        
        
        

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");

    }

    private void OpenCardForTargetAndOlderUserRename(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out, String targetUser, String targetUserDescription, String olderUser) {
        Utilities u = new Utilities();
        StringBuffer xml = new StringBuffer();
        UsersClass tmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + "save_rename_target_and_older_user_edit" + "</targetEditField>"); // user_create / user_edit
        xml.append("<targetUser>" + Utilities.escapeXML(targetUser) + "</targetUser>");
        xml.append("<targetUserDescription>" + Utilities.escapeXML(targetUserDescription) + "</targetUserDescription>");
        xml.append("<olderUser>" + Utilities.escapeXML(olderUser) + "</olderUser>");

        UserInfoClass userForEditingInfo = new UserInfoClass();
        userForEditingInfo = tmsUsers.SearchTMSUser(request, targetUser);

        if (userForEditingInfo != null) {
            String userNameStored = userForEditingInfo.name;
            String passwordStored = userForEditingInfo.password;
            String descriptionStored = userForEditingInfo.description;
            ArrayList thesaurusNamesStored = userForEditingInfo.thesaurusNames;
            ArrayList thesaurusGroupsStored = userForEditingInfo.thesaurusGroups;
            xml.append("<userForEditingInfo>");
            xml.append("<name>" + userNameStored + "</name>");
            xml.append("<password>" + passwordStored + "</password>");
            int thesaurusNamesStoredSize = thesaurusNamesStored.size();
            for (int i = 0; i < thesaurusNamesStoredSize; i++) {
                String thesaurusName = (String) thesaurusNamesStored.get(i);
                String thesaurusGroup = (String) thesaurusGroupsStored.get(i);
                // group_translated=\"" + tmsUsers.translateGroup(thesaurusGroup) + "\"
                xml.append("<thesaurus group=\"" + thesaurusGroup + "\">" + thesaurusName + "</thesaurus>");
            }
            xml.append("<description>" + descriptionStored + "</description>");
            xml.append("</userForEditingInfo>");
        }

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");

        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }
    /*---------------------------------------------------------------------
     OpenCardForUserPasswordEditing()
     ----------------------------------------------------------------------*/

    private void OpenCardForUserPasswordEditing(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out) {
        Utilities u = new Utilities();
        // get the name of current user
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String currentUser = SessionUserInfo.name;

        // write XML output
        StringBuffer xml = new StringBuffer();
        writeXMLForUserPasswordEditing(request, sessionInstance, xml, "save_change_password", currentUser);
        // transform XML output
        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /*---------------------------------------------------------------------
     writeXMLForUserPasswordEditing()
     ----------------------------------------------------------------------*/
    private void writeXMLForUserPasswordEditing(HttpServletRequest request, SessionWrapperClass sessionInstance, StringBuffer xml, String targetField, String targetUser) {
        Utilities u = new Utilities();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        UsersClass tmsUsers = new UsersClass();

        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + targetField + "</targetEditField>");
        xml.append("<targetUser>" + targetUser + "</targetUser>");

        UserInfoClass userForEditingInfo = new UserInfoClass();
        userForEditingInfo = tmsUsers.SearchTMSUser(request, targetUser);

        if (userForEditingInfo != null) {
            String userNameStored = userForEditingInfo.name;
            String passwordStored = userForEditingInfo.password;
            String descriptionStored = userForEditingInfo.description;
            xml.append("<userForEditingPasswordInfo>");
            xml.append("<name>" + userNameStored + "</name>");
            xml.append("<password>" + passwordStored + "</password>");
            xml.append("<description>" + descriptionStored + "</description>");
            xml.append("</userForEditingPasswordInfo>");
        }

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");
    }

    /*---------------------------------------------------------------------
     OpenCardForUserThesaurusEditing()
     ----------------------------------------------------------------------*/
    private void OpenCardForUserThesaurusEditing(HttpServletRequest request, ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out) {
        Utilities u = new Utilities();
        // get the name of current user
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String currentUser = SessionUserInfo.name;

        // write XML output
        StringBuffer xml = new StringBuffer();
        writeXMLForUserThesaurusEditing(request, sessionInstance, xml, "save_change_thesaurus", currentUser);
        // transform XML output
        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /*---------------------------------------------------------------------
     writeXMLForUserThesaurusEditing()
     ----------------------------------------------------------------------*/
    private void writeXMLForUserThesaurusEditing(HttpServletRequest request, SessionWrapperClass sessionInstance, StringBuffer xml, String targetField, String targetUser) {
        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + targetField + "</targetEditField>");
        xml.append("<targetUser>" + targetUser + "</targetUser>");

        UserInfoClass userForEditingInfo = new UserInfoClass();
        userForEditingInfo = tmsUsers.SearchTMSUser(request, targetUser);

        if (userForEditingInfo != null) {
            String userNameStored = userForEditingInfo.name;
            xml.append("<userForEditingThesaurusInfo>");
            xml.append("<name>" + userNameStored + "</name>");
            xml.append("<thesaurusOfUser>");
            ArrayList thesaurusNamesStored = tmsUsers.GetThesaurusSetOfTMSUser(request, userNameStored);
            int thesaurusNamesStoredSize = thesaurusNamesStored.size();
            for (int i = 0; i < thesaurusNamesStoredSize; i++) {
                String thesaurusName = (String) thesaurusNamesStored.get(i);
                xml.append("<thesaurus>" + thesaurusName + "</thesaurus>");
            }
            xml.append("</thesaurusOfUser>");
            xml.append("</userForEditingThesaurusInfo>");
        }

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");
    }

    /*---------------------------------------------------------------------
     OpenCardForUserCreation()
     ----------------------------------------------------------------------*/
    private void OpenCardForUserCreation(ServletContext context, SessionWrapperClass sessionInstance, PrintWriter out, ArrayList thesaurusVector) {
        Utilities u = new Utilities();
        // write XML output
        StringBuffer xml = new StringBuffer();
        writeXMLForUserCreation(sessionInstance, xml, "save_user_create", "", thesaurusVector);
        // transform XML output
        u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditUserActions/Edit_User.xsl");
    }

    /*---------------------------------------------------------------------
     writeXMLForUserCreation()
     ----------------------------------------------------------------------*/
    private void writeXMLForUserCreation(SessionWrapperClass sessionInstance, StringBuffer xml, String targetField, String targetUser, ArrayList thesaurusVector) {
        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");

        xml.append(ConstantParameters.xmlHeader); // <?xml version=\"1.0\" encoding=\"UTF-8\"?>
        xml.append("<page language=\"" + SessionUserInfo.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
        xml.append("<targetEditField>" + targetField + "</targetEditField>"); // user_create / user_edit
        xml.append("<targetUser>" + targetUser + "</targetUser>");

        // <pageInfo>
        xml.append("<pageInfo>");
        // <existingThesaurus>
       
        xml.append("<existingThesaurus>");
        if(SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator)){
            xml.append("<existingThesaurusName>" + Utils.ConstantParameters.AllThesauriIndicator + "</existingThesaurusName>");
        }
        int thesaurusVectorSize = thesaurusVector.size();
        for (int i = 0; i < thesaurusVectorSize; i++) {
            xml.append("<existingThesaurusName>" + (String) thesaurusVector.get(i) + "</existingThesaurusName>");
        }
        xml.append("</existingThesaurus>");
        // <THEMASUsersGroups>
        xml.append("<THEMASUsersGroups>");
        for (String group : tmsUsers.UsersGroups) { // do not display ADMINISTRATOR
            if(group.equals(ConstantParameters.Group_Administrator)){
                continue;
            }
            // tr=\"" + tmsUsers.UsersGroupsGR[i] + "\"
            xml.append("<THEMASUsersGroupName>" + group + "</THEMASUsersGroupName>");
        }
        xml.append("</THEMASUsersGroups>");
        xml.append("</pageInfo>");

        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append("</page>");
    }

    /*---------------------------------------------------------------------
     CreateUser()
     ----------------------------------------------------------------------*/
    private void CreateUser(HttpServletRequest request, SessionWrapperClass sessionInstance, PrintWriter out, final String uiLang) throws IOException {
        Utilities u = new Utilities();
        // get input parameters
        String newName_User = u.getDecodedParameterValue(request.getParameter("newUserName"));
        String newPassword_User = u.getDecodedParameterValue(request.getParameter("newPassword_User"));
        String newDescription_User = u.getDecodedParameterValue(request.getParameter("newDescription_User"));
        String administratorCheckBox = u.getDecodedParameterValue(request.getParameter("administratorCheckBox"));
        boolean createNewUserAsAdministrator = (administratorCheckBox != null);
        String[] selectThesaurusArray = null;
        ArrayList<String> selectThesaurusVector = new ArrayList<>();
        String[] selectUserGroupArray = null;
        ArrayList<String> selectUserGroupVector = new ArrayList<>();
        // in case of no Administrator creation/editing get the list of thesaurus-groups
        if (createNewUserAsAdministrator == false) {
            selectThesaurusArray = request.getParameterValues("selectThesaurus");
            if (selectThesaurusArray != null) {
                int selectThesaurusArrayLen = selectThesaurusArray.length;
                for (int i = 0; i < selectThesaurusArrayLen; i++) {
                    selectThesaurusVector.add(u.getDecodedParameterValue(selectThesaurusArray[i]));
                }
                selectUserGroupArray = request.getParameterValues("selectUserGroup");
                int selectUserGroupArrayLen = selectUserGroupArray.length;
                for (int i = 0; i < selectUserGroupArrayLen; i++) {
                    selectUserGroupVector.add(u.getDecodedParameterValue(selectUserGroupArray[i]));
                }
            }
        }
        UsersClass tmsUsers = new UsersClass();
        int resultOfCreateUser = tmsUsers.CreateUser(request, sessionInstance, createNewUserAsAdministrator, newName_User, newPassword_User, newDescription_User, selectThesaurusVector, selectUserGroupVector, UsersClass.CreateUserSimple_Mode, null, null);

        DBGeneral dbGen = new DBGeneral();

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject();
        
        switch (resultOfCreateUser) {
            case UsersClass.NO_USER_NAME_GIVEN:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NO_USER_NAME_GIVEN", null, uiLang));
                //out.println("Failure" + "Username field is not declared.");
                out.println("Failure" + resultMessageObj.getValue());               
                return;
            case UsersClass.USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20", null, uiLang));
                //out.println("Failure" + "The field of Username must have minimum length of 2 and max length of 20 characters.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NO_USER_PASSWORD_GIVEN:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NO_USER_PASSWORD_GIVEN", null, uiLang));
                //out.println("Failure" + "User Password field is not declared.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_XML:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NEW_USER_NAME_ALREADY_EXISTS_IN_XML", new String[]{newName_User}, uiLang));
                //out.println("Failure" + "The new Username '" + newName_User + "' is already in use from another user.");                
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.THESAURUS_SET_WITH_DUBLICATE_VALUES:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/THESAURUS_SET_WITH_DUBLICATE_VALUES", null, uiLang));
                //out.println("Failure" + "Definition of different groups is not allowed in the same Thesaurus.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_DB:
                out.println("Failure" + "NEW_USER_NAME_ALREADY_EXISTS_IN_DB");
                //dbGen.Translate(resultMessageObj, "root/resultOfCreateUser/NEW_USER_NAME_ALREADY_EXISTS_IN_DB", null, pathToMessagesXML);
                //out.println("Failure" + resultMessageObj.getValue());
                return;
        }

        out.println("Success");
    }

    private void CreateUserAndHandleOlderUserName(HttpServletRequest request, SessionWrapperClass sessionInstance, PrintWriter out, final String uiLang) throws IOException {

        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();

        // get input parameters
        String newName_User = u.getDecodedParameterValue(request.getParameter("newUserName"));
        String newPassword_User = u.getDecodedParameterValue(request.getParameter("newPassword_User"));
        String newDescription_User = u.getDecodedParameterValue(request.getParameter("newDescription_User"));
        //String administratorCheckBox    = u.getDecodedParameterValue(request.getParameter("administratorCheckBox"));       

        String olderUserCreateChoice = u.getDecodedParameterValue(request.getParameter("olderUserCreateChoice"));// values: null, CommitInitialCreateUser, RenameOlderAndThenCreateTargetUser
        String olderUserRenameName = u.getDecodedParameterValue(request.getParameter("olderUserRenameName"));

        String[] selectThesaurusArray = request.getParameterValues("selectThesaurus");
        String[] selectUserGroupArray = request.getParameterValues("selectUserGroup");

        ArrayList<String> selectThesaurusVector = new ArrayList<String>();
        ArrayList<String> selectUserGroupVector = new ArrayList<String>();
        boolean createUserAsAdministrator = false;

        if (selectUserGroupArray != null) {
            int selectUserGroupArrayLen = selectUserGroupArray.length;
            for (int i = 0; i < selectUserGroupArrayLen; i++) {
                selectUserGroupVector.add(u.getDecodedParameterValue(selectUserGroupArray[i]));
            }
        }

        if (selectThesaurusArray != null) {
            int selectThesaurusArrayLen = selectThesaurusArray.length;
            for (int i = 0; i < selectThesaurusArrayLen; i++) {
                selectThesaurusVector.add(u.getDecodedParameterValue(selectThesaurusArray[i]));
            }
        }

        if (selectUserGroupVector.contains(ConstantParameters.Group_Administrator)) {
            createUserAsAdministrator = true;
        }

        int resultOfCreateUser = tmsUsers.CreateUser(request, sessionInstance, createUserAsAdministrator, newName_User, newPassword_User, newDescription_User, selectThesaurusVector, selectUserGroupVector, UsersClass.CreateUserWithOlderUserHandle_Mode, olderUserCreateChoice, olderUserRenameName);


        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject("");
        
        switch (resultOfCreateUser) {
            case UsersClass.NO_USER_NAME_GIVEN:
                //out.println("Failure" + "Username field is not declared.");
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NO_USER_NAME_GIVEN", null, uiLang));
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NO_NEW_FORMER_USER_NAME_GIVEN:
                
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NO_NEW_FORMER_USER_NAME_GIVEN", new String[]{newName_User}, uiLang));
                //out.println("Failure" + "Field of new User is not declared for the older User '" + newName_User + "'.");
                out.println("Failure" + resultMessageObj.getValue());                
                return;
            case UsersClass.USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20", null, uiLang));
                //out.println("Failure" + "The field of Username must have minimum length of 2 and max length of 20 characters.");                
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_FORMER_USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NEW_FORMER_USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20", new String[]{olderUserRenameName}, uiLang));
                //out.println("Failure" + "New field of Username '" + olderUserRenameName + "' must have minimun length of 2 and max length of 20 characters.");
                out.println("Failure" + resultMessageObj.getValue());                
                return;
            case UsersClass.NO_USER_PASSWORD_GIVEN:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NO_USER_PASSWORD_GIVEN", null, uiLang));
                //out.println("Failure" + "User Password field is not declared.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_XML:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NEW_USER_NAME_ALREADY_EXISTS_IN_XML", new String[]{newName_User}, uiLang));
                //out.println("Failure" + "The new Username '" + newName_User + "' is already in use from another user.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_XML:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_XML", new String[]{olderUserRenameName}, uiLang));
                //out.println("Failure" + "The new Username '" + olderUserRenameName + "' is already in use from another user.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.THESAURUS_SET_WITH_DUBLICATE_VALUES:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/THESAURUS_SET_WITH_DUBLICATE_VALUES", null, uiLang));
                //out.println("Failure" + "Definition of different groups is not allowed in the same Thesaurus.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_DB:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_DB", new String[]{olderUserRenameName}, uiLang));
                //out.println("Failure" + "The new Username '" + olderUserRenameName + "' is already in use from another user in database. Please choose a different one.");
                out.println("Failure" + resultMessageObj.getValue());                
                return;
            case UsersClass.FORMER_USER_RENAME_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfCreateUser/FORMER_USER_RENAME_FAILED", new String[]{newName_User,olderUserRenameName}, uiLang));
                //out.println("Failure" + "Renaming of user '" + newName_User + "' to '" + olderUserRenameName + "' failed.");
                out.println("Failure" + resultMessageObj.getValue());                
                return;
        }

        out.println("Success");
        return;

    }
    
    /*---------------------------------------------------------------------
     EditUser()
     ----------------------------------------------------------------------*/
    private void EditUser(HttpServletRequest request, SessionWrapperClass sessionInstance, PrintWriter out) throws IOException {
        
        UserInfoClass TMSCurrentUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        
        Utilities u = new Utilities();
        // get input parameters
        String oldUserName = u.getDecodedParameterValue(request.getParameter("targetUser"));
        String newName_User = u.getDecodedParameterValue(request.getParameter("newName_User"));
        String newDescription_User = u.getDecodedParameterValue(request.getParameter("newDescription_User"));
        
        UsersClass tmsUsers = new UsersClass();
        
        ArrayList<String> selectThesaurusVector = new ArrayList<>();         
        ArrayList<String> selectUserGroupVector = new ArrayList<>();
        
        boolean isAdmin = false;
        
        // administrator will also be given the opportunity to edit the mappings
        if (TMSCurrentUserInfo.userGroup.equals(ConstantParameters.Group_Administrator)) {
            
            String isAdminCheckbox = u.getDecodedParameterValue(request.getParameter("administratorCheckBox"));
            isAdmin = (isAdminCheckbox != null);
            
            String[] selectThesaurusArray = request.getParameterValues("selectThesaurus");
            if (selectThesaurusArray != null) {
                int selectThesaurusArrayLen = selectThesaurusArray.length;
                for (int i = 0; i < selectThesaurusArrayLen; i++) {
                    selectThesaurusVector.add(u.getDecodedParameterValue(selectThesaurusArray[i]));
                }
                String[] selectUserGroupArray =  request.getParameterValues("selectUserGroup");
                int selectUserGroupArrayLen = selectUserGroupArray.length;
                for (int i = 0; i < selectUserGroupArrayLen; i++) {
                    selectUserGroupVector.add(u.getDecodedParameterValue(selectUserGroupArray[i]));
                }
            }
        }
        else{
            UserInfoClass currentMappings = tmsUsers.SearchTMSUser(request, oldUserName);
            
            if(currentMappings.userGroup.equals(ConstantParameters.Group_Administrator)){
                isAdmin = true;
            }
                    
            if(currentMappings.thesaurusNames!=null && currentMappings.thesaurusGroups!=null){
                for(int i=0; i< currentMappings.thesaurusNames.size(); i++){
                    selectThesaurusVector.add(currentMappings.thesaurusNames.get(i));
                    selectUserGroupVector.add(currentMappings.thesaurusGroups.get(i));
                    if(currentMappings.thesaurusGroups.get(i).equals(ConstantParameters.Group_Administrator)){
                        isAdmin = true;
                    }
                }
            }
        }
        
        String deletePasswordCheckBox = u.getDecodedParameterValue(request.getParameter("deletePasswordCheckBox"));
        boolean deletePassword = (deletePasswordCheckBox != null);
        String deleteUserCheckBox = u.getDecodedParameterValue(request.getParameter("deleteUserCheckBox"));
        boolean deleteUser = (deleteUserCheckBox != null);

        
        int resultOfEditUser = tmsUsers.EditUser(request, sessionInstance, deletePassword, deleteUser, isAdmin, oldUserName, newName_User, newDescription_User,selectThesaurusVector,selectUserGroupVector);

        
        ServletContext context = getServletContext();
        DBGeneral dbGen = new DBGeneral();

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        //String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
        StringObject resultMessageObj = new StringObject();
        //ArrayList<String> errorArgs = new ArrayList<String>();
        
        switch (resultOfEditUser) {
            case UsersClass.NO_USER_NAME_GIVEN:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/NO_USER_NAME_GIVEN", null, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "Username field is not declared.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NOT_ALLOWED_TO_DELETE_YOURSELF:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/NOT_ALLOWED_TO_DELETE_YOURSELF", new String[]{oldUserName}, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "Self deletion of user '" + oldUserName + "' is not allowed.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.CANNOT_DELETE_LAST_ADMINISTRATOR:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/CANNOT_DELETE_LAST_ADMINISTRATOR", new String[]{oldUserName}, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "User '" + oldUserName + "' cannot be deleted. He/She is the last administrator of the system.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.CANNOT_DELETE_LAST_THESAURUS_COMMITTEE_USER_OF_A_THESAURUS:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/CANNOT_DELETE_LAST_THESAURUS_COMMITTEE_USER_OF_A_THESAURUS", new String[]{oldUserName}, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "User '" + oldUserName + "' cannot be deleted. He/She is the last user of group "Thesaurus Committee" for a Thesaurus.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20", null, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "New field of Username must have minimum length of 2 and max length of 20 characters.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_XML:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/NEW_USER_NAME_ALREADY_EXISTS_IN_XML", new String[]{newName_User}, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "The new Username '" + newName_User + "' is already in use from another user.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_DB:
                //ajax handling no need to set the error message
                out.println("Failure" + "NEW_USER_NAME_ALREADY_EXISTS_IN_DB");
                //dbGen.Translate(resultMessageObj, "root/resultOfEditUser/NEW_USER_NAME_ALREADY_EXISTS_IN_DB", null, pathToMessagesXML);
                //out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.USER_RENAME_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUser/USER_RENAME_FAILED", new String[]{oldUserName,newName_User}, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "Renaming of user '" + oldUserName + "' to '" + newName_User + "' in database failed.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
        }

        out.println("Success");
    }

    /*---------------------------------------------------------------------
     EditTargetAndOlderUser()
     ----------------------------------------------------------------------*/
    private void EditTargetAndOlderUser(HttpServletRequest request, SessionWrapperClass sessionInstance, PrintWriter out, final String uiLang) throws IOException {
        Utilities u = new Utilities();
        UsersClass tmsUsers = new UsersClass();

        String targetUser = u.getDecodedParameterValue(request.getParameter("targetUser"));
        String targetUserDescription = u.getDecodedParameterValue(request.getParameter("targetDescription_User"));
        String olderUserName = u.getDecodedParameterValue(request.getParameter("olderUser"));
        String OlderUserRenameName = u.getDecodedParameterValue(request.getParameter("olderUserRenameName"));
        String choice = u.getDecodedParameterValue(request.getParameter("olderUserRenameChoice"));
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/" + UsersClass.WebAppUsersXMLFilePath);

        int resultOfEditTargetAndOlderUser = tmsUsers.EditTargetAndOlderUser(sessionInstance, this.getServletContext(), THEMASUsersFileName, targetUser, targetUserDescription, olderUserName, OlderUserRenameName, choice);

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject();
        
        switch (resultOfEditTargetAndOlderUser) {
            case UsersClass.NO_USER_NAME_GIVEN:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/NO_USER_NAME_GIVEN", null, uiLang));
                //out.println("Failure" + "Username field is not declared.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.USER_NAME_DOES_NOT_EXIST:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/USER_NAME_DOES_NOT_EXIST", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "The renaming user: '" + targetUser + "' could not be found as a user of the system.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20", null, uiLang));
                //out.println("Failure" + "The field of Username must have minimum length of 2 and max length of 20 characters.");
                //errorArgs.add(OlderUserRenameName);
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_XML:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/NEW_USER_NAME_ALREADY_EXISTS_IN_XML", new String[]{OlderUserRenameName}, uiLang));
                //out.println("Failure" + "The new Username '" + OlderUserRenameName + "' is already in use from another user.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.NEW_USER_NAME_ALREADY_EXISTS_IN_DB:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/NEW_USER_NAME_ALREADY_EXISTS_IN_DB", new String[]{OlderUserRenameName,olderUserName}, uiLang));
                //out.println("Failure" + "Username '" + OlderUserRenameName + "' already exists in database. Please choose a different Username to rename the old user '" + olderUserName + "'.);//+"<newName>"+newName_User+"</newName>" + "<oldName>" + oldUserName + "</oldName>");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.TARGET_USER_RENAME_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/TARGET_USER_RENAME_FAILED", new String[]{targetUser,olderUserName}, uiLang));
                //out.println("Failure" + "Failure of renaming user '" + targetUser + "' to '" + olderUserName + "'.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.FORMER_USER_RENAME_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/FORMER_USER_RENAME_FAILED", new String[]{olderUserName,OlderUserRenameName}, uiLang));
                //out.println("Failure" + "Renaming of user '" + olderUserName + "' to '" + OlderUserRenameName + "' in database failed.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.TARGET_USER_DELETION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/TARGET_USER_DELETION_FAILED", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "Failure of deleting user '" + targetUser + "' from the database.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.FORMER_USER_DELETION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/FORMER_USER_DELETION_FAILED", new String[]{olderUserName}, uiLang));
                //out.println("Failure" + "Failure of deleting user '" + olderUserName + "' from the database.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.TARGET_USER_CREATED_BY_LINKS_DELETION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/TARGET_USER_CREATED_BY_LINKS_DELETION_FAILED", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "Failure of deleting creator links to author '" + targetUser + "'.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.TARGET_USER_MODIFIED_BY_LINKS_DELETION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/TARGET_USER_MODIFIED_BY_LINKS_DELETION_FAILED", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "Failure of deleting modifier links to author '" + targetUser + "'.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.FORMER_USER_INSTANCE_ADDITION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/FORMER_USER_INSTANCE_ADDITION_FAILED", new String[]{olderUserName,targetUser}, uiLang));
                //out.println("Failure" + "Failure inserting user '" + olderUserName + "' to thesaurus where user '" + targetUser + "' belonged.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.FORMER_USER_CREATED_BY_LINKS_ADDITION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/FORMER_USER_CREATED_BY_LINKS_ADDITION_FAILED", new String[]{olderUserName}, uiLang));
                //out.println("Failure" + "Failure of moving creator links to author '" + olderUserName + "'.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.FORMER_USER_MODIFIED_BY_LINKS_ADDITION_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditTargetAndOlderUser/FORMER_USER_MODIFIED_BY_LINKS_ADDITION_FAILED", new String[]{olderUserName}, uiLang));
                //out.println("Failure" + "Failure of moving modifier links to author '" + olderUserName + "'.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
        }
        out.println("Success");
    }
    /*---------------------------------------------------------------------
     EditUserPassword()
     ----------------------------------------------------------------------*/

    private void EditUserPassword(HttpServletRequest request, PrintWriter out, final String uiLang) throws IOException {
        Utilities u = new Utilities();
        // get input parameters
        String oldUserPassword = u.getDecodedParameterValue(request.getParameter("oldUserPassword"));
        String newUserPassword1 = u.getDecodedParameterValue(request.getParameter("newUserPassword1"));
        String newUserPassword2 = u.getDecodedParameterValue(request.getParameter("newUserPassword2"));
        String targetUser = u.getDecodedParameterValue(request.getParameter("targetUser"));
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/" + UsersClass.WebAppUsersXMLFilePath);

        UsersClass tmsUsers = new UsersClass();
        int resultOfEditUserPassword = tmsUsers.EditUserPassword(THEMASUsersFileName, targetUser, oldUserPassword, newUserPassword1, newUserPassword2);

        ServletContext context = getServletContext();
        DBGeneral dbGen = new DBGeneral();

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject();
        
        switch (resultOfEditUserPassword) {
            case UsersClass.USER_NAME_DOES_NOT_EXIST:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUserPassword/USER_NAME_DOES_NOT_EXIST", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "The renaming user: '" + targetUser + "' could not be found as a user of the system.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.OLD_PASSWORD_GIVEN_INCORRECT:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUserPassword/OLD_PASSWORD_GIVEN_INCORRECT", null, uiLang));
                //out.println("Failure" + "Wrong old password was given.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.CONFIRM_NEW_PASSWORD_IS_DIFFERENT:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUserPassword/CONFIRM_NEW_PASSWORD_IS_DIFFERENT", null, uiLang));
                //out.println("Failure" + "Different values of new password were given.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
        }

        out.println("Success");
    }

    /*---------------------------------------------------------------------
     EditUserThesaurus()
     ----------------------------------------------------------------------*/
    private void EditUserThesaurus(HttpServletRequest request, HttpSession session, SessionWrapperClass sessionInstance, PrintWriter out, final String uiLang) throws IOException {
        Utilities u = new Utilities();
        // get input parameters
        String newUserThesaurus = u.getDecodedParameterValue(request.getParameter("selectThesaurus"));
        String targetUser = u.getDecodedParameterValue(request.getParameter("targetUser"));

        UsersClass tmsUsers = new UsersClass();
        int resultOfEditUserThesaurus = tmsUsers.EditUserThesaurus(request, session, sessionInstance, targetUser, newUserThesaurus);
        
        ServletContext context = getServletContext();
        DBGeneral dbGen = new DBGeneral();

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject();
        
        switch (resultOfEditUserThesaurus) {
            case UsersClass.USER_NAME_DOES_NOT_EXIST:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUserThesaurus/USER_NAME_DOES_NOT_EXIST", new String[]{targetUser}, uiLang));
                //out.println("Failure" + "User '" + targetUser + "' does not exist.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfEditUserThesaurus/AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED", null, uiLang));
                //out.println("Failure" + "The change of thesaurus failed.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
        }

        out.println("Success");
    }

    /*---------------------------------------------------------------------
     ShareThesaurus()
     ----------------------------------------------------------------------*/
    private void ShareThesaurus(HttpServletRequest request, SessionWrapperClass sessionInstance, PrintWriter out) throws IOException {
        Utilities u = new Utilities();
        // get input parameters
        String[] selectUsersArray = null;
        ArrayList<String> selectUsersVector = new ArrayList<>();
        String[] selectUserGroupArray = null;
        ArrayList<String> selectUserGroupVector = new ArrayList<>();
        // get the list of users-groups
        selectUsersArray = request.getParameterValues("selectUser");
        if (selectUsersArray != null) {
            int selectUsersArrayLen = selectUsersArray.length;
            for (int i = 0; i < selectUsersArrayLen; i++) {
                selectUsersVector.add(u.getDecodedParameterValue(selectUsersArray[i]));
            }
            selectUserGroupArray = request.getParameterValues("selectUserGroup");
            int selectUserGroupArrayLen = selectUserGroupArray.length;
            for (int i = 0; i < selectUserGroupArrayLen; i++) {
                selectUserGroupVector.add(u.getDecodedParameterValue(selectUserGroupArray[i]));
            }
        }

        UsersClass tmsUsers = new UsersClass();
        // get the thesaurus currently selected by current user
        UserInfoClass TMSCurrentUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        String CurrentThesaurus = TMSCurrentUserInfo.selectedThesaurus;

        int resultOfShareThesaurus = tmsUsers.ShareThesaurus(request, CurrentThesaurus, selectUsersVector, selectUserGroupVector);
        
        
        ServletContext context = getServletContext();
        DBGeneral dbGen = new DBGeneral();

        String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);

        StringObject resultMessageObj = new StringObject();
        
        
        switch (resultOfShareThesaurus) {
            case UsersClass.THESAURUS_WITHOUT_THESAURUS_COMMITTEE:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfShareThesaurus/THESAURUS_WITHOUT_THESAURUS_COMMITTEE", null,TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "Thesaurus must have at least one \"Thesaurus Committee\" user.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
            case UsersClass.USER_SET_WITH_DUBLICATE_VALUES:
                resultMessageObj.setValue(u.translateFromMessagesXML("root/resultOfShareThesaurus/USER_SET_WITH_DUBLICATE_VALUES", null, TMSCurrentUserInfo.UILang));
                //out.println("Failure" + "There cannot be defined more than one group-role for one user at the same Thesaurus.");
                out.println("Failure" + resultMessageObj.getValue());
                return;
        }

        out.println("Success");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
