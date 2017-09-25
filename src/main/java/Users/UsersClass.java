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
package Users;

import DB_Classes.DBThesaurusReferences;
import DB_Classes.DBGeneral;
import Utils.ConstantParameters;
import Utils.Utilities;

import Utils.Parameters;
import Utils.SessionWrapperClass;

import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/*---------------------------------------------------------------------
                            UsersClass
-----------------------------------------------------------------------
  Class for handling the THEMAS users information keeped in \\WEB-INF\\UsersClass.xml with syntax (g.e.):
	<user>
		<name>karam</name>
		<password>karam</password>
		<thesaurus group="READER">THES1</thesaurus>
		<thesaurus group="LIBRARY">THES2</thesaurus>
		<thesaurus group="THESAURUS_TEAM">THES3</thesaurus>
		<thesaurus group="THESAURUS_COMMITTEE">THES4</thesaurus>		
		<thesaurus group="ADMINISTRATOR"></thesaurus>				
		<description></description>
	</user> 
----------------------------------------------------------------------*/
public class UsersClass {
    public static final String WebAppUsersXMLFilePath = File.separator + "WEB-INF" + File.separator + "WebAppUSERS.xml";
    public final String[] UsersGroups = {Utils.ConstantParameters.Group_Reader, "LIBRARY", "THESAURUS_TEAM", "THESAURUS_COMMITTEE", ConstantParameters.Group_Administrator,ConstantParameters.Group_External_Reader};
    public final String[] UsersGroupsGR = {"Χρήστης Αναγνώστης", "Χρήστης Βιβλιοθήκης", "Ομάδα Θησαυρού", "Επιτροπή Θησαυρού", "Διαχειριστής","Εξωτερικός Αναγνώστης"};
        
    /*---------------------------------------------------------------------
                            UsersClass()
    ----------------------------------------------------------------------*/                
    public UsersClass() {
    }  
    
    /*---------------------------------------------------------------------
                            TEST functions()
    ----------------------------------------------------------------------*/                
    void TEST_AddTMSUser(HttpServletRequest request) {
        // TEST
        Vector<String> thesaurusV = new Vector<String>();
        thesaurusV.add("AAA");
        thesaurusV.add("BBB");
        Vector<String> groupV = new Vector<String>();
        groupV.add(Utils.ConstantParameters.Group_Reader);
        groupV.add("LIBRARY");        
        int ret;
        HttpSession session = request.getSession();	
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request);
        ret = CreateUser(request,sessionInstance, false, "KARAM", "PASSWORD", "lalalalala", thesaurusV, groupV,CreateUserSimple_Mode,null,null);
    }      
    void TEST_AddNewThesaurusForCurrentTMSUser(HttpServletRequest request,SessionWrapperClass sessionInstance) {
        AddNewThesaurusForCurrentTMSUser(request.getSession().getServletContext().getRealPath("/"+UsersClass.WebAppUsersXMLFilePath), sessionInstance,"BBB");
    }          
    
    /*---------------------------------------------------------------------
                            translateGroup()
    ----------------------------------------------------------------------*/
    public String translateGroup(String group) {
        int THEMASUsersGroupsSize = UsersGroups.length;
        for (int i = 0; i < THEMASUsersGroupsSize; i++) {
            if (group.equals(UsersGroups[i])) {
                return UsersGroupsGR[i];
            }
        }                                        
        return group;
    }    
    
    /*---------------------------------------------------------------------
                            getResultsInXml_ForTableLayout()
    -----------------------------------------------------------------------
    INPUT: - Vector allUsers: the Vector with the users to be parsed
           - String[] output: the properties of each user to be collected
    OUTPUT: a String with the XML representation of the results
    ----------------------------------------------------------------------*/
    public void getResultsInXml(HttpServletRequest request, Vector<UserInfoClass> allUsers, String[] output, StringBuffer XMLresults) {
        Utilities u = new Utilities();
        
        XMLresults.append("<results>");
        int resultsLIMIT = allUsers.size();
        for (int i = 0; i < resultsLIMIT; i++) {
            UserInfoClass currentUserInfo = allUsers.get(i);
            XMLresults.append("<user>");
            for (int j = 0; j < output.length; j++) {
                if (output[j].equals("name")) {
                    XMLresults.append("<name>" + Utilities.escapeXML(currentUserInfo.name) + "</name>");
                } 
                else if (output[j].equals("thesaurusName")) {
                    XMLresults.append("<thesaurusSet>");
                    int thesaurusNamesSize = currentUserInfo.thesaurusNames.size();
                    for (int k = 0; k < thesaurusNamesSize; k++) {
                        XMLresults.append("<thesaurus group=\"" + Utilities.escapeXML((String)currentUserInfo.thesaurusGroups.get(k)) + "\" group_translated=\"" + Utilities.escapeXML(translateGroup((String)currentUserInfo.thesaurusGroups.get(k))) + "\">" + Utilities.escapeXML((String)currentUserInfo.thesaurusNames.get(k)) + "</thesaurus>");
                    }                    
                    XMLresults.append("</thesaurusSet>");
                }   
                else
                if (output[j].equals("description")) {
                    XMLresults.append("<description>" + Utilities.escapeXML(currentUserInfo.description) + "</description>");
                }                 
            }
            XMLresults.append("</user>");
        }
        XMLresults.append("</results>");
    }    
    
    /*---------------------------------------------------------------------
                            AddNewThesaurusForCurrentTMSUser()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String NewThesaurusName: the new created thesaurus to be added to current user
    FUNCTION: adds a new created thesaurus to current user
	<user> +
            <thesaurus group="userGroup">NewThesaurusName</thesaurus>
	</user>
    CALLED BY: any new thesaurus Create/Copy/Merge
    ----------------------------------------------------------------------*/                
    public void AddNewThesaurusForCurrentTMSUser(String THEMASUsersFileName, SessionWrapperClass sessionInstance, String NewThesaurusName) {
        // get current user's info
        UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        //String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        String currentUserGroup = SessionUserInfo.userGroup;
        if (currentUserGroup.equals("ADMINISTRATOR")) { // administrator has NO thesaurus limitations
            return;
        }
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // search for current user
        String currentUserName = SessionUserInfo.name;
        // for each element of Vector THEMASUserInfoList
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            int currentThesIndex = userInfo.thesaurusNames.indexOf(NewThesaurusName);
            if (userNameStored.equals(currentUserName)) {
                if(currentThesIndex==-1){
                    userInfo.thesaurusNames.add(NewThesaurusName);
                    userInfo.thesaurusGroups.add(currentUserGroup); 
                }
                else{
                    userInfo.thesaurusNames.set(currentThesIndex,NewThesaurusName);
                    userInfo.thesaurusGroups.set(currentThesIndex,currentUserGroup);
                }
                // replace found user-info
                THEMASUserInfoList.set(i, userInfo);
                break;
            }
        }        
        // save changes
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
    }   
    
    /*---------------------------------------------------------------------
                            SearchTMSUser()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String username: the user to be searched
    OUTPUT: - null in case the user with the given username does not exist
            - a UserInfoClass class with the full information of the given user
    ----------------------------------------------------------------------*/                
    public UserInfoClass SearchTMSUser(HttpServletRequest request, String username) {
        // load the XML file with the users to Vector THEMASUserInfoList
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // search for user with the given username
        UserInfoClass userInfo = new UserInfoClass();
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            if (userNameStored.equals(username)) {
                return userInfo;
            }
        }
        return null;
    }
    
    /*---------------------------------------------------------------------
                            GetTMSUsersNames()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
    OUTPUT: - a Vector with all the existing user names
    ----------------------------------------------------------------------*/                
    public Vector<String> GetTMSUsersNames(HttpServletRequest request) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        Vector<String> TMSUsersNames = new Vector<String>();
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            TMSUsersNames.add(userInfo.name);
        }  
        
        Collections.sort(TMSUsersNames);
        
        return TMSUsersNames;
    }    
    
    /*---------------------------------------------------------------------
                            GetTMSUsers_GroupsOfThesaurus()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String targetThesaurus: the thesaurus to be checked
           - Vector UserNamesV, GroupsV: parallel Vectors to be filled with the 
             user name - group couples of the given thesaurus
    FUNCTION: - fills parallel Vectors UserNamesV and GroupsV with the
                user name - group couples of the given thesaurus
    ----------------------------------------------------------------------*/                
    public void GetTMSUsers_GroupsOfThesaurus(HttpServletRequest request, String targetThesaurus, Vector<String> UserNamesV, Vector<String> GroupsV) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            int thesaurusIndex = userInfo.thesaurusNames.indexOf(targetThesaurus);
            if (thesaurusIndex != -1) {
                UserNamesV.add(userInfo.name);
                GroupsV.add(userInfo.thesaurusGroups.get(thesaurusIndex));
            }
        }          
    }    
    
    /*---------------------------------------------------------------------
                            GetThesaurusSetOfCurrentTMSUser()
    -----------------------------------------------------------------------
    INPUT: - HttpServlet servlet: the current servlet
           - HttpServletRequest request: the servlet's request
           - String username: the user to be searched
    OUTPUT: - null in case the user with the given username does not exist
            - a Vector with the thesaurus owned by the user
    ----------------------------------------------------------------------*/                
    public Vector<String> GetThesaurusSetOfTMSUser(HttpServletRequest request, String username) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        Vector<String> ThesaurusSetOfTMSUser = new Vector<String>();
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // search for user with the given username
        UserInfoClass userInfo = new UserInfoClass();
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            if (userNameStored.equals(username)) {
                usernameExists = true;
                break;
            }
        }
        // in case user does not exist, return null
        if (usernameExists == false) {
            return null;
        }        
        // in case of user ADMINISTRATOR or of user owning ALL thesaurus (*), return all the existing thesaurus
        //if (userInfo.thesaurusGroups.contains("ADMINISTRATOR") || userInfo.thesaurusNames.contains("*")) {
        if (userInfo.thesaurusGroups.contains(ConstantParameters.Group_Administrator)) {
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();            
            DBGeneral dbGen = new DBGeneral();
            
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class WebAppUsers GetThesaurusSetOfTMSUser()");
                return null;
            }

            // get existing thesaurus
            dbGen.GetExistingThesaurus(false, ThesaurusSetOfTMSUser, Q, sis_session);  
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        }
        else {
            ThesaurusSetOfTMSUser = userInfo.thesaurusNames;
        }
        
        return ThesaurusSetOfTMSUser;
    }    
    
    // error codes - result values of methods: CreateUser(), EditUser(), ShareThesaurus()
    public static final int TMS_USER_OPERATION_SUCCEDED                                 = 0;
    public static final int USER_NAME_ALREADY_EXISTS                                    = -1;
    public static final int USER_NAME_DOES_NOT_EXIST                                    = -2;
    public static final int NO_USER_NAME_GIVEN                                          = -3;
    public static final int NO_USER_PASSWORD_GIVEN                                      = -4;    
    public static final int THESAURUS_SET_WITH_DUBLICATE_VALUES                         = -5;    
    public static final int NOT_ALLOWED_TO_DELETE_YOURSELF                              = -6;    
    public static final int CANNOT_DELETE_LAST_ADMINISTRATOR                            = -7;    
    public static final int CANNOT_DELETE_LAST_THESAURUS_COMMITTEE_USER_OF_A_THESAURUS  = -8;  
    public static final int THESAURUS_WITHOUT_THESAURUS_COMMITTEE                       = -9;  
    public static final int USER_SET_WITH_DUBLICATE_VALUES                              = -10;  
    public static final int OLD_PASSWORD_GIVEN_INCORRECT                                = -11;  
    public static final int CONFIRM_NEW_PASSWORD_IS_DIFFERENT                           = -12;  
    public static final int AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED                  = -13;  
    public static final int USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20                   = -14;  
    public static final int NEW_USER_NAME_ALREADY_EXISTS_IN_XML                         = -15;
    public static final int NEW_USER_NAME_ALREADY_EXISTS_IN_DB                          = -16;
    public static final int USER_RENAME_FAILED                                          = -17;
    public static final int TARGET_USER_DELETION_FAILED                                 = -18;//"Failure of deleting user '" + targetUser +"' from the database."
    public static final int FORMER_USER_DELETION_FAILED                                 = -19;//"Failure of deleting user '" + olderUserName +"' from the database."    
    public static final int TARGET_USER_RENAME_FAILED                                   = -20;//"Failure of renaming user '" + targetUser +"' to '"+olderUserName+"'.";
    public static final int FORMER_USER_RENAME_FAILED                                   = -21;//"Renaming of user '" + olderUserName +"' to '"+OlderUserRenameName+"' in database failed.";
    public static final int TARGET_USER_CREATED_BY_LINKS_DELETION_FAILED                = -22;//"Failure of deleting creator links to author '"+targetUser+"'.";
    public static final int TARGET_USER_MODIFIED_BY_LINKS_DELETION_FAILED               = -23;//"Failure of deleting modifier links to author '"+targetUser+"'.";
    public static final int FORMER_USER_INSTANCE_ADDITION_FAILED                        = -24;//"Failure inserting user '" + olderUserName +"' to thesaurus where user '"+currentThes+"' belonged.";
    public static final int FORMER_USER_CREATED_BY_LINKS_ADDITION_FAILED                = -25;//"Failure of moving creator links to author '"+olderUserName+"'.";
    public static final int FORMER_USER_MODIFIED_BY_LINKS_ADDITION_FAILED               = -26;//"Failure of moving modifier links to author '"+olderUserName+"'.";
    
    public static final int NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_XML                  = -27;
    public static final int NO_NEW_FORMER_USER_NAME_GIVEN                               = -28;
    public static final int NEW_FORMER_USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20        = -29;
    public static final int NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_DB                   = -30;     
    /*---------------------------------------------------------------------
                            CreateUser()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - boolean createUserAsAdministrator: true in case a new administrator is to be created
           - String username: the given username to be added
           - String password: the given password to be added
           - String description: the given description to be added
           - Vector thesaurusV, Vector groupV: parallel Vectors for the <thesaurus group="...">...</thesaurus> tags
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful addition
    FUNCTION: adds a user to UsersClass.xml file, with format:
	<user>
		<name>username</name>
		<password>password</password>
		<thesaurus group="...">...</thesaurus>
		<description>...</description>
	</user>
    ----------------------------------------------------------------------*/         
    public static final int CreateUserSimple_Mode = 0;
    public static final int CreateUserWithOlderUserHandle_Mode = 1;
    
    public int CreateUser(HttpServletRequest request,SessionWrapperClass sessionInstance, boolean createUserAsAdministrator, String username, String password, String description, Vector thesaurusV, Vector groupV, int SimpleCreateOrOlderUserHandle, String olderUserCreateChoice, String olderUserRenameName) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. not blank username
        if (username==null || username.equals("")) {
            return NO_USER_NAME_GIVEN;
        }
       
        // check 2. username length is less than 6 or greater than 20
        if (username.length() < 2 || username.length() > 20) {
            return USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20;
        }
        
        // check 3. not blank password (check it only in case of a new user with a group different than "READER")
        boolean newUsersGroupsAreAllREADER = true;
        int groupVSize = groupV.size();
        for (int i = 0; i < groupVSize; i++) {        
            String UserGroupStr = (String)groupV.get(i);
            if (UserGroupStr.equals(Utils.ConstantParameters.Group_Reader) == false) {
                newUsersGroupsAreAllREADER = false;
                break;
            }
        }        
        if (password.equals("") && newUsersGroupsAreAllREADER == false) {
            return NO_USER_PASSWORD_GIVEN;
        }                
        
        // check 4. username must be unique
        // check if username to be added is unique. Otherwise, return NEW_USER_NAME_ALREADY_EXISTS_IN_XML
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            if (userNameStored.equals(username)) {
                return NEW_USER_NAME_ALREADY_EXISTS_IN_XML;
            }
        }  
            
        // check 5. Vector thesaurusV must contain unique values (the same thesaurus cannot have 2 different groups)
        Vector<String> uniqueThesaurusV = new Vector<String>();
        int thesaurusVSize = thesaurusV.size();
        for (int i = 0; i < thesaurusVSize; i++) {        
            String thesaurus = (String)thesaurusV.get(i);
            if (uniqueThesaurusV.contains(thesaurus) == false) {
                uniqueThesaurusV.add(thesaurus);
            }
            else {
                return THESAURUS_SET_WITH_DUBLICATE_VALUES;
            }
        }
        
        Vector<String> dbEditors = new Vector<String>();
        collectDBEditors(dbEditors);
        
        //Untill here code is common both at simple create and create with older user handle
        if(SimpleCreateOrOlderUserHandle == CreateUserSimple_Mode){
            
            if (dbEditors.contains(username)) {
                return NEW_USER_NAME_ALREADY_EXISTS_IN_DB;
            }
        }
        else if(SimpleCreateOrOlderUserHandle == CreateUserWithOlderUserHandle_Mode){
            
            if(olderUserCreateChoice.equals("CommitInitialCreateUser")){
                // check 4. username must be unique
                // check if username to be added is unique. Otherwise, return NEW_USER_NAME_ALREADY_EXISTS_IN_XML
                THEMASUserInfoListSize = THEMASUserInfoList.size();
                for (int i = 0; i < THEMASUserInfoListSize; i++) {   
                    // get info for current stored user
                    UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
                    String userNameStored = userInfo.name; 
                    if (userNameStored.equals(username)) {
                        return NEW_USER_NAME_ALREADY_EXISTS_IN_XML; 
                    }
                    
                }
                 
                //WE DO NOT CHECK IF USER EXISTS IN DB JUST CONTINUE AND WRITE TO XML
              
            }
            else if(olderUserCreateChoice.equals("RenameOlderAndThenCreateTargetUser")){
                
                
                // check 1. not blank username
                if (olderUserRenameName==null || olderUserRenameName.equals("")) {
                    return NO_NEW_FORMER_USER_NAME_GIVEN;
                }

                // check 2. username length is less than 6 or greater than 20
                if (olderUserRenameName.length() < 2 || olderUserRenameName.length() > 20) {
                    return NEW_FORMER_USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20;
                }
                
                // check 4. username must be unique
                // check if username to be added is unique. Otherwise, return NEW_USER_NAME_ALREADY_EXISTS_IN_XML
                THEMASUserInfoListSize = THEMASUserInfoList.size();
                for (int i = 0; i < THEMASUserInfoListSize; i++) {   
                    // get info for current stored user
                    UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
                    String userNameStored = userInfo.name; 
                    
                    if (userNameStored.equals(olderUserRenameName)) {
                        return NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_XML; 
                    }
                }
                
                if (dbEditors.contains(olderUserRenameName)) {
                    return NEW_FORMER_USER_NAME_ALREADY_EXISTS_IN_DB;
                }
                
                if(dbEditors.contains(username)){ // IF NOT ALL GOOD. Not an error case               
                    if(renameEditor(sessionInstance, username, olderUserRenameName)==false){
                        return FORMER_USER_RENAME_FAILED;
                    }
                }
            }
        }
        
        
        
        // save changes
        // construct a new instance of UserInfoClass class for the new user
        UserInfoClass userInfo = new UserInfoClass();
        userInfo.name = username;
        userInfo.password = password;
        userInfo.thesaurusNames = thesaurusV;
        userInfo.thesaurusGroups = groupV;
        if (createUserAsAdministrator == true) {
            userInfo.thesaurusNames.add("");
            userInfo.thesaurusGroups.add("ADMINISTRATOR");
        }
        userInfo.description = description;
        THEMASUserInfoList.add(userInfo);        

        // write UsersClass.xml file
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        
        return TMS_USER_OPERATION_SUCCEDED;
    }    
    
    /*---------------------------------------------------------------------
                            EditUser()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - boolean deletePassword: true in case the user's password is to be deleted
           - boolean deleteUser: true in case the user is to be deleted
           - String username: the given username to be edited
           - String description: the given description to be added
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful addition
    FUNCTION: edits a user to UsersClass.xml file, with format:
	<user>
		<name>username</name>
		<password>password</password>
		<thesaurus group="...">...</thesaurus>
		<description>...</description>
	</user>
    ----------------------------------------------------------------------*/                
    public int EditUser(HttpServletRequest request,SessionWrapperClass sessionInstance,boolean deletePassword, boolean deleteUser,String oldUserName, String Newusername, String description) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. no old and new username defined --> Note that old user name is hidden
        if (Newusername == null || oldUserName ==null || Newusername.equals("") || oldUserName.equals("")) {
            return NO_USER_NAME_GIVEN;
        }
        
        // check 2. Old username must exist in XML (Not necessarily in DB as user may have not performed any transaction
        // check if username to be edited exists. Otherwise, return USER_NAME_DOES_NOT_EXIST
        UserInfoClass TargetUserInfo = null;
        int indexOfuserInfo = -1;
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            TargetUserInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = TargetUserInfo.name; 
            if (userNameStored.equals(oldUserName)) {
                usernameExists = true;
                indexOfuserInfo = i;
                break;
            }
        }
        if (usernameExists == false) {
            return USER_NAME_DOES_NOT_EXIST;
        }
        
        Vector<String> dbEditors = new Vector<String>();
        boolean renameUser =false;
        // check 3. in case of user deletion, check if this can be done
        if (deleteUser == true) {
            int ret = UserCanBeDeleted(request,sessionInstance, TargetUserInfo.name);
            if (ret != TMS_USER_OPERATION_SUCCEDED) {
                return ret;
            }
        }
       else {
            if (oldUserName.compareTo(Newusername) != 0) {
                
                //Perform rename user checks
                if (Newusername.length() < 2 || Newusername.length() > 20) {
                    return USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20;
                }

                //check if this name is declared for another user in THEMASUsers.
                for (int i = 0; i < THEMASUserInfoList.size(); i++) {
                    // get info for current stored user
                    UserInfoClass userInfo = (UserInfoClass) (THEMASUserInfoList.get(i));
                    String userNameStored = userInfo.name;
                    if (userNameStored.equals(Newusername)) {
                        return NEW_USER_NAME_ALREADY_EXISTS_IN_XML;
                    }
                }
                TargetUserInfo.name = Newusername; 
                //get Editor names From DB
                collectDBEditors(dbEditors);

                if (dbEditors.contains(Newusername)) {
                    return NEW_USER_NAME_ALREADY_EXISTS_IN_DB;
                }
                else{
                    if(dbEditors.contains(oldUserName)){ // old name exists in db and new name is valid and does not. No reason to fail renaming
                        renameUser = true;
                    }
                }

            }
        }        
        TargetUserInfo.description = description;
        
        // save changes
        if (deleteUser == true) { // case of user deletion
            THEMASUserInfoList.removeElement(TargetUserInfo);
            // write UsersClass.xml file
            WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
            return TMS_USER_OPERATION_SUCCEDED;            
        }
        
        if (deletePassword == true) { // case of user password deletion
            TargetUserInfo.password = "";
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if(SessionUserInfo.name.compareTo(oldUserName)!=0){
                SessionUserInfo.password = "";
                SessionUserInfo.description = description;
                sessionInstance.setAttribute("SessionUser", SessionUserInfo);
            } 
        }
        
        if(renameUser == true){
            if(renameEditor(sessionInstance, oldUserName, Newusername)==false){
                return USER_RENAME_FAILED;
            }
            else{
                //else continue with writing back to XML                
                UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
                if(SessionUserInfo.name.compareTo(oldUserName)==0){
                    SessionUserInfo.name = Newusername;
                    SessionUserInfo.description = description;
                    sessionInstance.setAttribute("SessionUser", SessionUserInfo);
                    
                }                
            }
        }
        
        
       
        
        // inform Vector
        THEMASUserInfoList.set(indexOfuserInfo, TargetUserInfo);
        // write UsersClass.xml file
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        
        return TMS_USER_OPERATION_SUCCEDED;
    }        
    
    public int EditTargetAndOlderUser(SessionWrapperClass sessionInstance,ServletContext context,String THEMASUsersFileName, String targetUser,String targetUserDescription,String olderUserName,String OlderUserRenameName,String choice){
        
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. no old and new username defined --> Note that old user name is hidden
        if (targetUser == null || olderUserName ==null || targetUser.equals("") || olderUserName.equals("")) {
            return NO_USER_NAME_GIVEN;
        }
        
        // check 2. Old username must exist in XML (Not necessarily in DB as user may have not performed any transaction
        // check if username to be edited exists. Otherwise, return USER_NAME_DOES_NOT_EXIST
        UserInfoClass TargetUserInfo = null;
        int indexOfuserInfo = -1;
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            TargetUserInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = TargetUserInfo.name; 
            if (userNameStored.equals(targetUser)) {
                usernameExists = true;
                indexOfuserInfo = i;
                break;
            }
        }
        if (usernameExists == false) {
            return USER_NAME_DOES_NOT_EXIST;
        }
        
        if(choice.compareTo("CommitInitialRenameUser")==0){
            UsersClass wtmsUsers = new UsersClass();
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            String initialThesaurus = SessionUserInfo.selectedThesaurus;
            
            int commitActionResult = mergeEditorsTargetOverFormer(sessionInstance,context,targetUser,olderUserName);
            //reset selected thesaurus to initiallly selected
            wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, initialThesaurus, SessionUserInfo.userGroup);
        
            if(commitActionResult==TMS_USER_OPERATION_SUCCEDED){
                //else continue with writing back to XML
                
                if(SessionUserInfo.name.compareTo(targetUser)==0){
                    SessionUserInfo.name = olderUserName;
                    SessionUserInfo.description = targetUserDescription;
                    sessionInstance.setAttribute("SessionUser", SessionUserInfo);                    
                }       
                
                TargetUserInfo.description = targetUserDescription;
                TargetUserInfo.name        = olderUserName;
                // inform Vector
                THEMASUserInfoList.set(indexOfuserInfo, TargetUserInfo);
                // write UsersClass.xml file
                WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
            }
            
            return commitActionResult;
                        
        }
        else{ //choice == "RenameOlderAndThenTargetUser"
            if(OlderUserRenameName==null || OlderUserRenameName.length()==0){
                return NO_USER_NAME_GIVEN;
            }
            if(OlderUserRenameName.length()<2 || OlderUserRenameName.length()>20){
                return USER_NAME_LENGTH_MUST_BE_BETWEEN_2_AND_20;
            }
            
            //check if this name is declared for another user in THEMASUsers.
            for (int i = 0; i < THEMASUserInfoList.size(); i++) {
                // get info for current stored user
                UserInfoClass userInfo = (UserInfoClass) (THEMASUserInfoList.get(i));
                String userNameStored = userInfo.name;
                if (userNameStored.equals(OlderUserRenameName)) {
                    return NEW_USER_NAME_ALREADY_EXISTS_IN_XML;
                }
            }
            
            Vector<String> editors = new Vector<String>();
            collectDBEditors(editors);
            if(editors.contains(OlderUserRenameName)){
                return NEW_USER_NAME_ALREADY_EXISTS_IN_DB;
            }
            
            if(renameEditor(sessionInstance, olderUserName, OlderUserRenameName)==false){
                return FORMER_USER_RENAME_FAILED;
            }
            
            if(renameEditor(sessionInstance, targetUser, olderUserName)==false){
                return TARGET_USER_RENAME_FAILED;
            }
            else{
                //else continue with writing back to XML
                UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
                if(SessionUserInfo.name.compareTo(targetUser)==0){
                    SessionUserInfo.name = olderUserName;
                    SessionUserInfo.description = targetUserDescription;
                    sessionInstance.setAttribute("SessionUser", SessionUserInfo);                    
                }                
            }
            
            TargetUserInfo.description = targetUserDescription;
            TargetUserInfo.name        = olderUserName;
            
            
            // inform Vector
            THEMASUserInfoList.set(indexOfuserInfo, TargetUserInfo);
            // write UsersClass.xml file
            WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        }
        return TMS_USER_OPERATION_SUCCEDED;
    }
    /*---------------------------------------------------------------------
                            ShareThesaurus()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String targetThesaurus: the thesaurus to be shared
           - Vector usersV, Vector groupV: parallel Vectors for the user-group couples to be set for the given thesaurus
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful sharing
    FUNCTION: shares the given thesaurus to the given user-group couples
    ----------------------------------------------------------------------*/                
    public int ShareThesaurus(HttpServletRequest request, String targetThesaurus, Vector usersV, Vector groupsV) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector<UserInfoClass> THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. Vector groupsV must contain at least 1 THESAURUS_COMMITTEE
        if (groupsV.contains("THESAURUS_COMMITTEE") == false) {
            return THESAURUS_WITHOUT_THESAURUS_COMMITTEE;
        }
        
        // check 2. Vector usersV must contain unique values (the same user cannot have 2 different groups for the same thesaurus)
        Vector<String> uniqueUsersV = new Vector<String>();
        int usersVSize = usersV.size();
        for (int i = 0; i < usersVSize; i++) {        
            String user = (String)usersV.get(i);
            if (uniqueUsersV.contains(user) == false) {
                uniqueUsersV.add(user);
            }
            else {
                return USER_SET_WITH_DUBLICATE_VALUES;
            }
        }
        
        // save changes
        // 1. clear ALL users references to targetThesaurus
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = THEMASUserInfoList.get(i);
            int userThesNamesSize = userInfo.thesaurusNames.size();
            Vector<String> newthesaurusNames = new Vector<String>();
            Vector<String> newthesaurusGroups = new Vector<String>();
            for (int j = 0; j < userThesNamesSize; j++) {
                String thesName = (String)userInfo.thesaurusNames.get(j);
                String thesGroup = (String)userInfo.thesaurusGroups.get(j);
                if (thesName.equals(targetThesaurus) == false) {
                    newthesaurusNames.add(thesName);
                    newthesaurusGroups.add(thesGroup);
                }                
            }
            userInfo.thesaurusNames = newthesaurusNames;
            userInfo.thesaurusGroups = newthesaurusGroups;
            THEMASUserInfoList.set(i, userInfo);
        }        
        // 2. set the given users references to targetThesaurus
        for (int i = 0; i < usersVSize; i++) {
            String currentUserName = (String)usersV.get(i);
            String currentUserGroup = (String)groupsV.get(i);
            THEMASUserInfoListSize = THEMASUserInfoList.size();
            for (int j = 0; j < THEMASUserInfoListSize; j++) {
                // get info for current stored user
                UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(j));
                if (userInfo.name.equals(currentUserName) == true) {
                    userInfo.thesaurusNames.add(targetThesaurus);
                    userInfo.thesaurusGroups.add(currentUserGroup);
                    THEMASUserInfoList.set(j, userInfo);
                }
            }
            
        }                

        // write UsersClass.xml file
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        
        return TMS_USER_OPERATION_SUCCEDED;
    }        
    
    /*---------------------------------------------------------------------
                            DeleteThesaurusFromTMSUsers()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String targetThesaurus: the thesaurus to be deleted
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful deletion
    FUNCTION: deletes the given thesaurus from all users rights
    CALLED BY: delete thesaurus
    ----------------------------------------------------------------------*/                
    public int DeleteThesaurusFromTMSUsers(HttpServletRequest request, String targetThesaurus) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
                
        // delete ALL users references to targetThesaurus
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            for (int j = 0; j < userInfo.thesaurusNames.size(); j++) {
                String thesName = (String)userInfo.thesaurusNames.get(j);
                if (thesName.equals(targetThesaurus) == true) {
                    userInfo.thesaurusNames.remove(j);
                    userInfo.thesaurusGroups.remove(j);
                }
            }
            THEMASUserInfoList.set(i, userInfo);
        }        
        
        // write UsersClass.xml file
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        
        return TMS_USER_OPERATION_SUCCEDED;
    }            
    
    /*---------------------------------------------------------------------
                            EditUserPassword()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String targetUser: the user to be edited
           - String oldUserPassword: the old user's password given
           - String newUserPassword1: the new user's password given
           - String newUserPassword2: the new user's password given (as confirmation)
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful editing
    FUNCTION: changes the given user's password
    ----------------------------------------------------------------------*/                
    public int EditUserPassword(String THEMASUsersFileName, String targetUser, String oldUserPassword, String newUserPassword1, String newUserPassword2) {
        //String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. targetUser must exist
        UserInfoClass targetUserInfo = null;
        int indexOfuserInfo = -1;
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            targetUserInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = targetUserInfo.name; 
            if (userNameStored.equals(targetUser)) {
                usernameExists = true;
                indexOfuserInfo = i;
                break;
            }
        }
        if (usernameExists == false) {
            return USER_NAME_DOES_NOT_EXIST;
        }
        // check 2. the old user's password given must be correct
        if (oldUserPassword.equals(targetUserInfo.password) == false) {
            return OLD_PASSWORD_GIVEN_INCORRECT;
        }        
        // check 3. newUserPassword1 and newUserPassword2 must be the same
        if (newUserPassword1.equals(newUserPassword2) == false) {
            return CONFIRM_NEW_PASSWORD_IS_DIFFERENT;
        }                
        
        // save changes
        targetUserInfo.password = newUserPassword1;
        // inform Vector
        THEMASUserInfoList.set(indexOfuserInfo, targetUserInfo);                
        
        // write UsersClass.xml file
        WriteWebAppUsersXMLFile(THEMASUsersFileName, THEMASUserInfoList);
        
        return TMS_USER_OPERATION_SUCCEDED;        
    }
    
    /*---------------------------------------------------------------------
                            EditUserThesaurus()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String targetUser: the user to be edited
           - String newUserThesaurus: the selected thesaurus to be set
    OUTPUT: - TMS_USER_OPERATION_SUCCEDED in case of successful editing
    FUNCTION: changes the given user's selected thesaurus
    ----------------------------------------------------------------------*/                
    public int EditUserThesaurus(HttpServletRequest request, HttpSession session, SessionWrapperClass sessionInstance,String targetUser, String newUserThesaurus) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // check 1. targetUser must exist
        UserInfoClass targetUserInfo = null;
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            targetUserInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = targetUserInfo.name; 
            if (userNameStored.equals(targetUser)) {
                usernameExists = true;
                break;
            }
        }
        if (usernameExists == false) {
            return USER_NAME_DOES_NOT_EXIST;
        }
        
        boolean authenticationSucceded = Authenticate(request, session,sessionInstance, targetUserInfo.name, targetUserInfo.password, newUserThesaurus);
        // check 2. authentication succeded
        if (authenticationSucceded == false) {
            return AUTHENTICATION_FOR_CHANGE_THESAURUS_FAILED;
        }        
                
        return TMS_USER_OPERATION_SUCCEDED;        
    }    
    
    /*---------------------------------------------------------------------
                            UserCanBeDeleted()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String userName: the user to be checked
    OUTPUT: - USER_NAME_DOES_NOT_EXIST in case the given user does not exist,
              or is the last ADMINISTRATOR user 
              or the last THESAURUS_COMMITTEE of a thesaurus
              TMS_USER_OPERATION_SUCCEDED, otherwise
    ----------------------------------------------------------------------*/                
    public int UserCanBeDeleted(HttpServletRequest request, SessionWrapperClass sessionInstance, String userName) {
        String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // 1. check if user to be deleted exists. Otherwise, return false
        UserInfoClass GivenUserInfo = null;
        boolean usernameExists = false;
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            GivenUserInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = GivenUserInfo.name; 
            if (userNameStored.equals(userName)) {
                usernameExists = true;
                break;
            }
        }
        if (usernameExists == false) {
            return USER_NAME_DOES_NOT_EXIST;
        }     
        // check 2. check the case of userInfo being the the current user (NOT allowed)
        UserInfoClass currentUser = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        if (GivenUserInfo.name.equals(currentUser.name) == true) {
            return NOT_ALLOWED_TO_DELETE_YOURSELF;
        }
        // 2. check if user to be deleted is the last ADMINISTRATOR user
        Vector GivenUserThesaurusGroups = GivenUserInfo.thesaurusGroups;
        boolean userToBeDeletedIsAdministrator = GivenUserThesaurusGroups.contains("ADMINISTRATOR");
        if (userToBeDeletedIsAdministrator == true) {
            // count all ADMINISTRATORs
            int countOfAdministrators = 0;
            for (int i = 0; i < THEMASUserInfoListSize; i++) {
                // get info for current stored user
                UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));                
                if (userInfo.thesaurusGroups.contains("ADMINISTRATOR") == true) {
                    countOfAdministrators++;
                }
            }
            if (countOfAdministrators == 1) {
                return CANNOT_DELETE_LAST_ADMINISTRATOR;
            }
        }
        // 3. check if user to be deleted is the last THESAURUS_COMMITTEE of a thesaurus
        Vector GivenUserThesaurusNames = GivenUserInfo.thesaurusNames;
        int GivenUserThesaurusNamesSize = GivenUserThesaurusNames.size();
        // for each thesaurus of given user
        for (int i = 0; i < GivenUserThesaurusNamesSize; i++) {
            // get the group for current thesaurus
            String groupOfCurrentThesaurus = (String)GivenUserThesaurusGroups.get(i);
            if (groupOfCurrentThesaurus.equals("THESAURUS_COMMITTEE") == false) {
                continue;
            }
            // get the thesaurus name where the given user is THESAURUS_COMMITTEE
            String thesaurus_where_given_user_is_THESAURUS_COMMITTEE = (String)GivenUserThesaurusNames.get(i);
            // count all THESAURUS_COMMITTEE users for this thesaurus
            int countOf_THESAURUS_COMMITTEE_users_for_this_thesaurus = 0;
            for (int j = 0; j < THEMASUserInfoListSize; j++) {
                UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(j));                
                Vector thesNamesV = userInfo.thesaurusNames;
                Vector thesGroupsV = userInfo.thesaurusGroups;
                int size = thesNamesV.size();
                for (int k = 0; k < size; k++) {
                    String thesName = (String)thesNamesV.get(k);
                    String thesGroup = (String)thesGroupsV.get(k);
                    if (thesName.equals(thesaurus_where_given_user_is_THESAURUS_COMMITTEE) == true && thesGroup.equals("THESAURUS_COMMITTEE") == true) {
                        countOf_THESAURUS_COMMITTEE_users_for_this_thesaurus++;
                    }
                }
            }
            if (countOf_THESAURUS_COMMITTEE_users_for_this_thesaurus == 1) {
                return CANNOT_DELETE_LAST_THESAURUS_COMMITTEE_USER_OF_A_THESAURUS;
            }
        }
        
        return TMS_USER_OPERATION_SUCCEDED;
    }    
    
    /*---------------------------------------------------------------------
                            WriteWebAppUsersXMLFile()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - Vector THEMASUserInfoList: a Vector of UserInfoClass classes with 
                the users info to be written to UsersClass.xml 
    FUNCTION: writes the UsersClass.xml file with the contents of Vector THEMASUserInfoList
    ----------------------------------------------------------------------*/                
    private synchronized void WriteWebAppUsersXMLFile(String WebAppUsersFileName, Vector THEMASUserInfoList) {
        String WebAppUsersFileNameContents = Utils.ConstantParameters.xmlHeader+
                "<WebAppUsers>\r\n";
        
        // for each element of Vector THEMASUserInfoList
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            String passwordStored = userInfo.password; 
            String descriptionStored = userInfo.description; 
            Vector thesaurusNamesStored = userInfo.thesaurusNames;
            Vector thesaurusGroupsStored = userInfo.thesaurusGroups;
            WebAppUsersFileNameContents += "\t<user>\r\n";
                WebAppUsersFileNameContents += "\t\t<name>" + userNameStored + "</name>\r\n";
                WebAppUsersFileNameContents += "\t\t<password>" + passwordStored + "</password>\r\n";
                int thesaurusNamesStoredSize = thesaurusNamesStored.size();
                for (int j = 0; j < thesaurusNamesStoredSize; j++) {                
                    String thesaurusName = (String)thesaurusNamesStored.get(j);
                    String thesaurusGroup = (String)thesaurusGroupsStored.get(j);
                    WebAppUsersFileNameContents += "\t\t<thesaurus group=\"" + thesaurusGroup + "\">" + thesaurusName + "</thesaurus>\r\n";
                }
                WebAppUsersFileNameContents += "\t\t<description>" + descriptionStored + "</description>\r\n";
            WebAppUsersFileNameContents += "\t</user>\r\n\r\n";
        }
        
        WebAppUsersFileNameContents += "</WebAppUsers>";
        
        //String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        SetUTF8FileContents(WebAppUsersFileName, WebAppUsersFileNameContents);
    }    

    /*---------------------------------------------------------------------
                            ReadWebAppUsersXMLFile()
    -----------------------------------------------------------------------
    FUNCTION: returns a Vector of UserInfoClass classes with the users info found in UsersClass.xml
    ----------------------------------------------------------------------*/                
    public synchronized Vector<UserInfoClass> ReadWebAppUsersXMLFile(String WebAppUsersFileName) {
        
        Vector<UserInfoClass> WebAppUserInfoList = new Vector<UserInfoClass>();
        
        // parse UsersClass.xml with DMS api
        // ATTENTION: use trim() for each get DMS api function, because ALL of them add a leading space to each return value (BUG)
        try {
            File webXMLFile = new File(WebAppUsersFileName);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(webXMLFile);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // initialize a XMLElement with the string contents of UsersClass.xml
            //Element THEMASUsersXMLElement = document.getDocumentElement();
            // get the list of <user> tags
            String expression = "/WebAppUsers/user";
            NodeList userTags = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            
            if(userTags!=null){
                int userTagsSize = userTags.getLength();
                for (int i = 0; i < userTagsSize; i++) {
                    Node userTag = userTags.item(i);
                    String userName ="";
                    String userPassword = "";
                    String description =  "";
                    Vector<String> thesaurusGroups = new Vector<String>();
                    Vector<String> thesaurusNames = new Vector<String>();
                            
                    NodeList userTagChildNodes = userTag.getChildNodes();
                    if(userTagChildNodes==null|| userTagChildNodes.getLength()==0){
                        continue;
                    }
                    
                    int childNodesSize = userTagChildNodes.getLength();
                    for(int k=0; k< childNodesSize; k++){
                        Node childNode = userTagChildNodes.item(k);
                        String name = childNode.getNodeName();
                        if(name.equals("name")){
                            userName = childNode.getTextContent().trim();
                        }
                        else if(name.equals("password")){
                            userPassword = childNode.getTextContent().trim();
                        }
                        else if(name.equals("description")){
                            description = childNode.getTextContent().trim();    
                        }
                        else if(name.equals("thesaurus")){
                            String userThesaurus = childNode.getTextContent().trim();    
                            String userGroup = childNode.getAttributes().getNamedItem("group").getTextContent().trim();
                            thesaurusGroups.add(userGroup);
                            thesaurusNames.add(userThesaurus);
                        }
                    }
                                        
                    // inform THEMASUserInfoList
                    UserInfoClass userInfo = new UserInfoClass();
                    userInfo.name = userName;
                    userInfo.password = userPassword;
                    userInfo.thesaurusNames = thesaurusNames;
                    userInfo.thesaurusGroups = thesaurusGroups;
                    userInfo.description = description;
                    WebAppUserInfoList.add(userInfo);
                }
            }
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(document), new StreamResult(webXMLFile));
            
            /*
            
            XMLElement THEMASUsersXMLElement = new XMLElement(THEMASUsersFileNameContents);
            
            XMLElement[] userTags = THEMASUsersXMLElement.getChildren("user");
            
            
            for (int i = 0; i < userTagsSize; i++) {
                //XMLElement userTag = userTags[i];
                // get the <name> tag (only 1)
                XMLElement[] nameTags = userTag.getChildren("name");
                XMLElement nameTag = nameTags[0];
                String userName = nameTag.getText().trim();
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"User found with name: " + userName);  
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"--------------------------------------------------------------");
                // get the <password> tag (only 1)
                XMLElement[] passwordTags = userTag.getChildren("password");
                XMLElement passwordTag = passwordTags[0];
                String userPassword = passwordTag.getText().trim();                
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"password: " + userPassword);
                // get the <thesaurus> tags (many) - g.e. <thesaurus group="READER">THES1</thesaurus>
                XMLElement[] thesaurusTags = userTag.getChildren("thesaurus");  
                // for each <thesaurus> tag of current <user> tag
                int thesaurusTagsSize = thesaurusTags.length;
                Vector<String> thesaurusGroups = new Vector<String>();
                Vector<String> thesaurusNames = new Vector<String>();
                for (int j = 0; j < thesaurusTagsSize; j++) {
                    XMLElement thesaurusTag = thesaurusTags[j];
                    // get the <thesaurus> group attribute value
                    String userGroup = thesaurusTag.getAttributeValue("group").trim();
                    thesaurusGroups.add(userGroup);
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"group: " + userGroup);
                    // get the <thesaurus> value
                    String userThesaurus = thesaurusTag.getText().trim();
                    thesaurusNames.add(userThesaurus);
                    //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"thesaurus: " + userThesaurus);
                }
                // get the <description> tag (only 1)
                XMLElement[] descriptionTags = userTag.getChildren("description");
                XMLElement descriptionTag = descriptionTags[0];
                String description = descriptionTag.getText().trim();                                
                
                // inform THEMASUserInfoList
                UserInfoClass userInfo = new UserInfoClass();
                userInfo.name = userName;
                userInfo.password = userPassword;
                userInfo.thesaurusNames = thesaurusNames;
                userInfo.thesaurusGroups = thesaurusGroups;
                userInfo.description = description;
                THEMASUserInfoList.add(userInfo);
            }*/
            
        } catch (Exception ex) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"XMLElement construction failed : " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } 
        
        return WebAppUserInfoList;
    }
    
    /*---------------------------------------------------------------------
                                Authenticate()
    -----------------------------------------------------------------------
    INPUT: - HttpServletRequest request: the servlet's request
           - String username: the given username to be authenticated
           - String password: the given password to be authenticated
           - String selectedThesaurus: the given thesaurus name to be authenticated
    OUTPUT: - true in case of successful authentication, false otherwise
    FUNCTION: parses Vector THEMASUserInfoList to match the given
              username, password and selectedThesaurus. 
              In case of 
              - successful authentication, sets the session attribute "SessionUser"
              to an instance of class UserInfoClass filled with the full 
              information of the authenticated user
              - authentication failure, sets the session attribute "SessionUser" to NULL
    ----------------------------------------------------------------------*/
    public boolean Authenticate(HttpServletRequest request,HttpSession session, SessionWrapperClass sessionInstance, String username, String password, String selectedThesaurus) {
        session = request.getSession();//BUG FIX --ILIAS session is already invalid from init() but this call causes it to be valid again
        ServletContext context = session.getServletContext();
        if(sessionInstance==null){
            sessionInstance = new SessionWrapperClass();
            sessionInstance.readSession(session,request);
        }
        String THEMASUsersFileName = "";
        if(Parameters.BaseRealPath.length()>0){
            THEMASUsersFileName = Parameters.BaseRealPath+WebAppUsersXMLFilePath;
        }
        else{
             THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/"+WebAppUsersXMLFilePath);
        }
        
        // load the XML file with the users to Vector THEMASUserInfoList
        Vector THEMASUserInfoList = ReadWebAppUsersXMLFile(THEMASUsersFileName);
        
        // for each element of Vector THEMASUserInfoList
        int THEMASUserInfoListSize = THEMASUserInfoList.size();
        for (int i = 0; i < THEMASUserInfoListSize; i++) {
            // get info for current stored user
            UserInfoClass userInfo = (UserInfoClass)(THEMASUserInfoList.get(i));
            String userNameStored = userInfo.name; 
            String passwordStored = userInfo.password; 
            Vector thesaurusNamesStored = userInfo.thesaurusNames;
            Vector thesaurusGroupsStored = userInfo.thesaurusGroups;
            // compare current stored user's info with the given parameters
            // check username
            if (userNameStored.equals(username) == false) continue;
            // check password
            if (passwordStored.equals(password) == false) continue;            
            // check selectedThesaurus
            // for each element of Vector thesaurusNamesStored
            boolean selectedThesaurusAuthenticationSucceded = false;
            String userGroup = null;
            int thesaurusNamesStoredSize = thesaurusNamesStored.size();            
            for (int j = 0; j < thesaurusNamesStoredSize; j++) {
                userGroup = (String)(thesaurusGroupsStored.get(j));
                // in case of group="ADMINISTRATOR" do not check selectedThesaurus
                if (userGroup.equals("ADMINISTRATOR") == true) {
                    selectedThesaurusAuthenticationSucceded = true;
                    break;                    
                }
                String thesaurusNameStored = (String)(thesaurusNamesStored.get(j)); 
                //if (thesaurusNameStored.equals(selectedThesaurus) == true || thesaurusNameStored.equals("*") == true) {
                if (thesaurusNameStored.equals(selectedThesaurus) == true) {
                    selectedThesaurusAuthenticationSucceded = true;
                    break;
                }
            }
            
            // in case of successful authentication
            if (selectedThesaurusAuthenticationSucceded == true) {
                // set the session attribute "SessionUser" to an instance of class 
                // UserInfoClass filled with the full information of the authenticated user
                SetSessionAttributeSessionUser(sessionInstance, context, username, password, selectedThesaurus, userGroup);
                
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Welcome user: " + username + " with password: " + password + " to thesaurus: " + selectedThesaurus + " under the group:" + userGroup);
                
                return true;
            }
        }
        
        // in case of authentication failure, set the session attribute "SessionUser" to NULL
        session.setAttribute("SessionUser", null);
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Login failed for user: " + username + " with password: " + password + " to thesaurus: " + selectedThesaurus);
        return false;
    }    
    
    /*----------------------------------------------------------------------
                         UpdateSessionUserSessionAttribute()
    -----------------------------------------------------------------------
    INPUT: - UserInfoClass SessionUserInfo: the UserInfoClass object to be updated
           - String selectedThesaurus: the authenticated thesaurus name           
    FUNCTION: sets the session attribute "SessionUser" to an instance of 
              class UserInfoClass filled with the full information of the authenticated user
    ------------------------------------------------------------------------*/

    public synchronized void UpdateSessionUserSessionAttribute(UserInfoClass SessionUserInfo, String selectedThesaurus) {
        // construct an instance of class UserInfoClass

        // fill it with the given parameters
        SessionUserInfo.selectedThesaurus = selectedThesaurus;
        String SVG_CategoriesFrom_for_traverseStr = Parameters.SVG_CategoriesFrom_for_traverse;

        // fill it with the configuration values for SVG mechanism
        // SVG_CategoriesFrom_for_traverse (replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa")
        SVG_CategoriesFrom_for_traverseStr = SVG_CategoriesFrom_for_traverseStr.replaceAll("%THES%", selectedThesaurus.toUpperCase());
        SVG_CategoriesFrom_for_traverseStr = SVG_CategoriesFrom_for_traverseStr.replaceAll("%thes%", selectedThesaurus.toLowerCase());
        SessionUserInfo.SVG_CategoriesFrom_for_traverse = SVG_CategoriesFrom_for_traverseStr;

        // SVG_CategoriesNames_for_traverse (replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa")
        String SVG_CategoriesNames_for_traverseStr = Parameters.SVG_CategoriesNames_for_traverse;
        SVG_CategoriesNames_for_traverseStr = SVG_CategoriesNames_for_traverseStr.replaceAll("%THES%", selectedThesaurus.toUpperCase());
        SVG_CategoriesNames_for_traverseStr = SVG_CategoriesNames_for_traverseStr.replaceAll("%thes%", selectedThesaurus.toLowerCase());
        SessionUserInfo.SVG_CategoriesNames_for_traverse = SVG_CategoriesNames_for_traverseStr;


        // fill it with the configuration values for Alphabetical display
        String DELIMITER1 = Parameters.DELIMITER1;
        // alphabetical_From_Class
        String[] alphabetical_From_ClassArray;
        String alphabetical_From_ClassStr = Parameters.alphabetical_From_Class;
        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        alphabetical_From_ClassStr = alphabetical_From_ClassStr.replaceAll("%THES%", selectedThesaurus.toUpperCase());
        alphabetical_From_ClassStr = alphabetical_From_ClassStr.replaceAll("%thes%", selectedThesaurus.toLowerCase());
        if(alphabetical_From_ClassStr.split(DELIMITER1).length>0){
            String[] tempArray1 = alphabetical_From_ClassStr.split(DELIMITER1);
            alphabetical_From_ClassArray = new String[tempArray1.length];
            for(int i=0;i<tempArray1.length;i++){
                alphabetical_From_ClassArray[i] = tempArray1[i];
            }
            SessionUserInfo.alphabetical_From_Class = alphabetical_From_ClassArray;
        }
        // alphabetical_Links
        String[] alphabetical_LinksArray;
        String alphabetical_LinksStr = Parameters.alphabetical_Links;
        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        alphabetical_LinksStr = alphabetical_LinksStr.replaceAll("%THES%", selectedThesaurus.toUpperCase());
        alphabetical_LinksStr = alphabetical_LinksStr.replaceAll("%thes%", selectedThesaurus.toLowerCase());
        if(alphabetical_LinksStr.split(DELIMITER1).length>0){
            String[] tempArray1 = alphabetical_LinksStr.split(DELIMITER1);
            alphabetical_LinksArray = new String[tempArray1.length];
            for(int i=0;i<tempArray1.length;i++){
                alphabetical_LinksArray[i]=tempArray1[i];
            }
            SessionUserInfo.alphabetical_Links = alphabetical_LinksArray;
        }

        // CLASS_SET_INCLUDE configuration value
        String DELIMITER2 = Parameters.DELIMITER2;
        
        Vector<Vector<String>> CLASS_SET_INCLUDEVec = new Vector<Vector<String>>();
        String CLASS_SET_INCLUDEStr = Parameters.CLASS_SET_INCLUDE;

        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        CLASS_SET_INCLUDEStr = CLASS_SET_INCLUDEStr.replaceAll("%THES%", selectedThesaurus.toUpperCase());
        CLASS_SET_INCLUDEStr = CLASS_SET_INCLUDEStr.replaceAll("%thes%", selectedThesaurus.toLowerCase());
        if(CLASS_SET_INCLUDEStr.split(DELIMITER1).length>0){
            String[] tempArray1 = CLASS_SET_INCLUDEStr.split(DELIMITER1);

            for(int i=0;i<tempArray1.length;i++){

                String[] tempArray2 = tempArray1[i].split(DELIMITER2);
                Vector<String> internal = new Vector<String>();

                for(int j=0;j<tempArray2.length;j++){
                    if(!internal.contains(tempArray2[j]))
                        internal.add(tempArray2[j]);
                }
                internal.trimToSize();
                CLASS_SET_INCLUDEVec.add(internal);
            }
        }
        CLASS_SET_INCLUDEVec.trimToSize();
        SessionUserInfo.CLASS_SET_INCLUDE = CLASS_SET_INCLUDEVec;

        // set the session attribute
        //sessionInstance.setAttribute("SessionUser", SessionUserInfo);
    }

    
    public synchronized void SetSessionAttributeSessionUser(SessionWrapperClass sessionInstance,ServletContext context,  String username, String password, String selectedThesaurus, String userGroup) {
        // construct an instance of class UserInfoClass
        UserInfoClass SessionUserInfo = new UserInfoClass();
        
        // fill it with the given parameters
        SessionUserInfo.name = username;
        SessionUserInfo.password = password;
        SessionUserInfo.selectedThesaurus = selectedThesaurus;
        SessionUserInfo.userGroup = userGroup;
        
        // fill it with the configuration values for SVG mechanism
        // SVG_CategoriesFrom_for_traverse (replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa")
        String SVG_CategoriesFrom_for_traverse = context.getInitParameter("SVG_CategoriesFrom_for_traverse");
        SVG_CategoriesFrom_for_traverse = SVG_CategoriesFrom_for_traverse.replaceAll("%THES%", selectedThesaurus);
        SVG_CategoriesFrom_for_traverse = SVG_CategoriesFrom_for_traverse.replaceAll("%thes%", selectedThesaurus.toLowerCase());        
        SessionUserInfo.SVG_CategoriesFrom_for_traverse = SVG_CategoriesFrom_for_traverse;
        // SVG_CategoriesNames_for_traverse (replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa")
        String SVG_CategoriesNames_for_traverse = context.getInitParameter("SVG_CategoriesNames_for_traverse");
        SVG_CategoriesNames_for_traverse = SVG_CategoriesNames_for_traverse.replaceAll("%THES%", selectedThesaurus);
        SVG_CategoriesNames_for_traverse = SVG_CategoriesNames_for_traverse.replaceAll("%thes%", selectedThesaurus.toLowerCase());                
        SessionUserInfo.SVG_CategoriesNames_for_traverse = SVG_CategoriesNames_for_traverse;
        
        // fill it with the configuration values for Alphabetical display
        String DELIMITER1 = context.getInitParameter("DELIMITER1");
        // alphabetical_From_Class
        String[] alphabetical_From_Class;
        String TempREADSTR = context.getInitParameter("alphabetical_From_Class");
        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        TempREADSTR = TempREADSTR.replaceAll("%THES%", selectedThesaurus);
        TempREADSTR = TempREADSTR.replaceAll("%thes%", selectedThesaurus.toLowerCase());                
        if(TempREADSTR.split(DELIMITER1).length>0){
            String[] tempArray1 = TempREADSTR.split(DELIMITER1);
            alphabetical_From_Class = new String[tempArray1.length];
            for(int i=0;i<tempArray1.length;i++){
                alphabetical_From_Class[i] = tempArray1[i];
            }            
            SessionUserInfo.alphabetical_From_Class = alphabetical_From_Class;            
        }
        // alphabetical_Links
        String[] alphabetical_Links;
        TempREADSTR = context.getInitParameter("alphabetical_Links");
        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        TempREADSTR = TempREADSTR.replaceAll("%THES%", selectedThesaurus);
        TempREADSTR = TempREADSTR.replaceAll("%thes%", selectedThesaurus.toLowerCase());                        
        if(TempREADSTR.split(DELIMITER1).length>0){
            String[] tempArray1 = TempREADSTR.split(DELIMITER1);
            alphabetical_Links = new String[tempArray1.length];
            for(int i=0;i<tempArray1.length;i++){
                alphabetical_Links[i]=tempArray1[i];
            }            
            SessionUserInfo.alphabetical_Links = alphabetical_Links;
        }
        
        // CLASS_SET_INCLUDE configuration value
        String DELIMITER2 = context.getInitParameter("DELIMITER2");
        Vector<Vector<String>> CLASS_SET_INCLUDE;
        CLASS_SET_INCLUDE = new Vector<Vector<String>>();
        TempREADSTR = context.getInitParameter("CLASS_SET_INCLUDE");
        // replace keywords "%THES%" with g.e. "AAA" and "%thes%" with g.e. "aaa"
        TempREADSTR = TempREADSTR.replaceAll("%THES%", selectedThesaurus);
        TempREADSTR = TempREADSTR.replaceAll("%thes%", selectedThesaurus.toLowerCase());        
        if(TempREADSTR.split(DELIMITER1).length>0){
            String[] tempArray1 = TempREADSTR.split(DELIMITER1);
            
            for(int i=0;i<tempArray1.length;i++){
                
                String[] tempArray2 = tempArray1[i].split(DELIMITER2);
                Vector<String> internal = new Vector<String>();
                
                for(int j=0;j<tempArray2.length;j++){
                    if(!internal.contains(tempArray2[j]))
                        internal.add(tempArray2[j]);
                }
                internal.trimToSize();
                CLASS_SET_INCLUDE.add(internal);
            }            
        }
        CLASS_SET_INCLUDE.trimToSize();        
        SessionUserInfo.CLASS_SET_INCLUDE = CLASS_SET_INCLUDE;
        
        // set the session attribute
        sessionInstance.setAttribute("SessionUser", SessionUserInfo);        
    }    

    /*----------------------------------------------------------------------
                         GetUTF8FileContents()
    ------------------------------------------------------------------------
    INPUT : - fileName: the full path of a file
    FUNCTION : returns the contents of the given file to a string in UTF-8 format
    ------------------------------------------------------------------------*/
    private String GetUTF8FileContents(String fileName) {
        // opens the file for reading
        String out_buf = new String("");
        try {
            BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            String lineString = new String(""); 
            lineString = br.readLine();
            while (lineString != null) {
                String tmp = lineString;
                out_buf = out_buf + tmp + "\n";	
                lineString = br.readLine();
            }
            br.close();
        } 
        catch (IOException e)  	  { 
            System.err.println(e);
            Utils.StaticClass.handleException(e);
        }
        return out_buf;
    }
    
    /*----------------------------------------------------------------------
                         SetUTF8FileContents()
    ------------------------------------------------------------------------
    INPUT : - fileName: the full path of a file
            - contents: a string to be written in the file
    FUNCTION : writes the given file with the given String contents in UTF-8 format
    ------------------------------------------------------------------------*/        
    private void SetUTF8FileContents(String fileName, String contents) {
        try {
            BufferedWriter out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            out.write(contents);
            out.close();
        }
        catch(IOException e) { 
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+e); 
            Utils.StaticClass.handleException(e);
        }
    }
        
    public void collectDBEditors(Vector<String> resultvector){
        if(resultvector==null){
            resultvector = new Vector<String>();
        }
        
        QClass Q = new QClass();
        IntegerObject sis_session = new IntegerObject();
          
        DBGeneral dbGen = new DBGeneral();
        

        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class THEMASUsers collectDBEditors");
            return;
        }


        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Editor"));
        int set_all_editors = Q.get_all_instances(0);
        Q.reset_set(set_all_editors);
        //StringObject nodeObj = new StringObject();
        Vector<Return_Nodes_Row> retVals = new Vector<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_all_editors, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                String editorUIName = dbGen.removePrefix(row.get_v1_cls_logicalname());
                resultvector.add(editorUIName);
            }   
        }
        /*while (Q.retur_nodes(set_all_editors, nodeObj) != QClass.APIFail) {
            String editorUIName = dbGen.removePrefix(nodeObj.getValue());
            resultvector.add(editorUIName);
        }*/
        Q.free_set(set_all_editors);

        //end query and close connection
        Q.free_all_sets();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        
        return;
    }
    
    public boolean renameEditor(SessionWrapperClass sessionInstance,String oldName, String newName){
        
        if(oldName.compareTo(newName)==0){
            return true;
        }

        DBGeneral dbGen = new DBGeneral();
        
        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();
        UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class THEMASUsers renameEditor");
            return false;
        }

        String prefixEditor = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        Identifier oldIdent = new Identifier(prefixEditor.concat(oldName));
        Identifier newIdent = new Identifier(prefixEditor.concat(newName));
        Q.reset_name_scope();
        int ret = Q.CHECK_Rename_Node(oldIdent, newIdent);
        if(ret==QClass.APIFail){
            //abort transaction and close connection
            Q.free_all_sets();
            Q.TEST_abort_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        else{

            //commit transaction and close connection
            Q.free_all_sets();
            Q.TEST_end_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        }
        
        
        if(ret ==QClass.APIFail){
            return false;
        }
        return true;
    }
    
    /*
     RETURNS:
     TARGET_USER_DELETION_FAILED --> "Failure of deleting user '" + targetUser +"' from the database."
     FORMER_USER_DELETION_FAILED --> "Failure of deleting user '" + olderUserName +"' from the database."
     TARGET_USER_RENAME_FAILED   --> "Failure of renaming user '" + targetUser +"' to '"+olderUserName+"'.";
     TARGET_USER_CREATED_BY_LINKS_DELETION_FAILED  --> "Failure of deleting creator links to author '"+targetUser+"'.";
     TARGET_USER_MODIFIED_BY_LINKS_DELETION_FAILED --> "Failure of deleting modifier links to author '"+targetUser+"'.";
     FORMER_USER_INSTANCE_ADDITION_FAILED          --> "Failure inserting user '" + olderUserName +"' to thesaurus where user '"+currentThes+"' belonged.";
     FORMER_USER_CREATED_BY_LINKS_ADDITION_FAILED  --> "Failure of moving creator links to author '"+olderUserName+"'.";
     FORMER_USER_MODIFIED_BY_LINKS_ADDITION_FAILED --> "Failure of moving modifier links to author '"+olderUserName+"'.";
     TMS_USER_OPERATION_SUCCEDED --> SUCCESSS
    */
    public int  mergeEditorsTargetOverFormer(SessionWrapperClass sessionInstance,ServletContext context, String targetUser, String olderUserName){
        //NOTE: Usage of this function should be accompanied with reset odf sessionInstance to formarlly selected thesaurus
        
        //target user will be held as oldIdentifier and oldUserObj
        //former user will be held as olderUserName use 
        QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
        IntegerObject sis_session = new IntegerObject();
        IntegerObject tms_session = new IntegerObject();
        
        //tools
        DBGeneral dbGen = new DBGeneral();
        UsersClass wtmsUsers = new UsersClass();
        //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        
        //initial data - plus storage classes
        UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        
        StringObject createdByClassObj = new StringObject();
        StringObject createdByLinkObj  = new StringObject();
        StringObject modifiedByClassObj = new StringObject();
        StringObject modifiedByLinkObj  = new StringObject();
        
        
        //open connection and start Transaction
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class THEMASUsers mergeEditorsTargetOverFormer");
            return TARGET_USER_DELETION_FAILED;
        }
        
        Vector<String> thesaurusVector = new Vector<String>();
        dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);
        
        String prefixEditor = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());
        Identifier oldIdentifier = new Identifier(prefixEditor.concat(targetUser));
        Identifier newIdentifier = new Identifier(prefixEditor.concat(olderUserName));
        StringObject oldUserObj = new StringObject(oldIdentifier.getLogicalName());
        StringObject newUserObj = new StringObject(newIdentifier.getLogicalName());
        
        //CHECKING CASES WHERE ONE OF OLD AND NEW EDITOR LINKS DOES NOT
        //HAVE ANY NODES POINTING TO IT. SIMPLE SOLUTIONS MAY THEN BE IMPLEMENTED
        
        //CASE 1: 
        //Old Editor Node has no Links pointing to him. Just Delete this node as long as 
        //Database is concerned and Return success to change UsersClass.xml
        //subcases one of 2 users does not exist thus xml may be transformed without any other checks
        Q.reset_name_scope();
        if(Q.set_current_node(oldUserObj)==QClass.APIFail){
            
            //commit transaction and close connection
            Q.free_all_sets();
            Q.TEST_end_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            return TMS_USER_OPERATION_SUCCEDED;
        }        
        int set_old_editor_to_links = Q.get_link_to(0);
        Q.reset_set(set_old_editor_to_links);
        int cardOf_old_editor_to_links = Q.set_get_card(set_old_editor_to_links);
        Q.free_set(set_old_editor_to_links);
        
        if(cardOf_old_editor_to_links ==0 ){
            Q.reset_name_scope();
            if(Q.CHECK_Delete_Node(oldIdentifier)==QClass.APIFail){
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                return TARGET_USER_DELETION_FAILED;
            }
            else{
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                return TMS_USER_OPERATION_SUCCEDED;
            }
        }
        
        //CASE 2: 
        //New Editor Node has no Links pointing to him. Just Delete this node and rename old 
        //Editor Node to name requested. Return success to change UsersClass.xml
        Q.reset_name_scope();
        if (Q.set_current_node(newUserObj) == QClass.APIFail) {
            
            //commit transaction and close connection
            Q.free_all_sets();
            Q.TEST_end_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

            return TMS_USER_OPERATION_SUCCEDED;
        }
        
        int set_new_editor_to_links = Q.get_link_to(0);
        Q.reset_set(set_new_editor_to_links);
        
        int cardOf_new_editor_to_links = Q.set_get_card(set_new_editor_to_links);
        Q.free_set(set_new_editor_to_links);
        
        if(cardOf_new_editor_to_links==0){
            if(Q.CHECK_Delete_Node(newIdentifier)==QClass.APIFail){
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                return FORMER_USER_DELETION_FAILED;//"Failure of deleting user '" + olderUserName +"' from the database.";
            }
            
            if(Q.CHECK_Rename_Node(oldIdentifier, newIdentifier)==QClass.APIFail){

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

                return TARGET_USER_RENAME_FAILED;
            }
            else{

                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                return TMS_USER_OPERATION_SUCCEDED;
            }
        }
        
        
        //Case 3:
        //This is the case where both old and new Editor nodes have links pointing to them.
        //No simple solution for this case. Must traverse all thesaurus categories, Delete links from
        //old and add links to new if needed.
        
        // walk through all thesauri. THEMASAPIClass uses sessionInstance 
        // in order to find out classes thus it should always be set to the correct thesaurus
        for(int p=0 ; p<thesaurusVector.size(); p++){
           
            Vector<String> oldTermCreatedNodes = new Vector<String>();
            Vector<String> oldTermModifiedNodes = new Vector<String>();
            Vector<Long> oldTermCreatedLinkIds = new Vector<Long>();
            Vector<Long> oldTermModiifedLinkIds = new Vector<Long>();
            
            String currentThes = thesaurusVector.get(p);
            wtmsUsers.SetSessionAttributeSessionUser(sessionInstance, context, SessionUserInfo.name, SessionUserInfo.password, currentThes, SessionUserInfo.userGroup);
            
            //find out category links of interest for Person to be renamed (here: created_by and modified_by)
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.created_by_kwd, createdByClassObj, createdByLinkObj, Q, sis_session);
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.modified_by_kwd, modifiedByClassObj, modifiedByLinkObj, Q, sis_session);
            
            //read created_by links of current thes that must be deleted. 
            //This must be done on a per thesaurus basis otherwise error will occur bacause of sessionInstance internal values
            //Created_by Links are read seperately from modified_by links 
            //because we must know what kind of link we are copying when creating links to newIdentifier Editor.            
            Q.reset_name_scope();
            Q.set_current_node(oldUserObj);
            
            int set_created_by_links = Q.get_link_to_by_category(0, createdByClassObj, createdByLinkObj);
            Q.reset_set(set_created_by_links);
            Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
            if(Q.bulk_return_link(set_created_by_links, retVals)!=QClass.APIFail){
                for(Return_Link_Row row: retVals){
                    oldTermCreatedNodes.add(row.get_v1_cls());
                    oldTermCreatedLinkIds.add(row.get_Neo4j_NodeId());  
                }
            }
            /*
            no need to use return_full_link_id the same job can be done by return
            since we only ask for cls and linkId
            
            int return_full_link_id(int sessionID, int set_id, l_name cls, int *clsid, l_name label,
            int *linkid, l_name categ, l_name fromcls, int *categid, cm_value *cmv,
            int *unique_category)
            
            int return_link_id(int sessionID, int set_id, l_name cls, int *fcid, int *sysid, cm_value *cmv, int *traversed)
            
            int return_link(int sessionID, int set_id, l_name cls, l_name label, cm_value *cmv)

            StringObject fromcls = new StringObject();
            StringObject label = new StringObject();
            StringObject categ = new StringObject();
            StringObject cls = new StringObject();
            IntegerObject uniq_categ = new IntegerObject();
            IntegerObject clsID = new IntegerObject();
            IntegerObject linkID = new IntegerObject();
            IntegerObject categID = new IntegerObject();
            CMValue cmv = new CMValue();
            while(Q.retur_full_link_id(set_created_by_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                oldTermCreatedNodes.add(cls.getValue());
                oldTermCreatedLinkIds.add(linkID.getValue());                
            }
            */
            
            //Reading modified_by links of node to be renamed ->merged to the new editor
            Q.reset_name_scope();
            Q.set_current_node(oldUserObj);
            
            int set_modified_by_links = Q.get_link_to_by_category(0, modifiedByClassObj, modifiedByLinkObj);
            Q.reset_set(set_modified_by_links);
            /*
            while(Q.retur_full_link_id(set_modified_by_links, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
                oldTermModifiedNodes.add(cls.getValue());
                oldTermModiifedLinkIds.add(linkID.getValue());                
            }
            */
            //find out in a set which nodes were pointing to oldIdentifier Editor as created_by links
            Q.reset_set(set_created_by_links);
            int set_created_by_nodes_for_transfer = Q.get_from_value(set_created_by_links);
            Q.reset_set(set_created_by_nodes_for_transfer);
            
            //find out in a set which nodes were pointing to oldIdentifier Editor as modified_by links
            Q.reset_set(set_modified_by_links);
            int set_modified_by_nodes_for_transfer = Q.get_from_value(set_modified_by_links);
            Q.reset_set(set_modified_by_nodes_for_transfer);
            
            StringObject previousThesaurusName = new StringObject("");
            TA.GetThesaurusNameWithoutPrefix(previousThesaurusName);
            if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            
            //Starting created_by links deletion pointing to oldIdentifier Editor node that will be merged to newIdentifier Editor name selected
            for(int i=0; i<oldTermCreatedNodes.size() ; i++ ){
                
                String targetEditorTerm = oldTermCreatedNodes.get(i);
                long oldDeleteIdL         = oldTermCreatedLinkIds.get(i);
                
                int ret = TA.CHECK_DeleteNewDescriptorAttribute(oldDeleteIdL, new StringObject(targetEditorTerm));
                
                //int ret = WTA.DeleteNewDescriptorAttribute(SessionUserInfo.selectedThesaurus, oldDeleteId, new StringObject(targetEditorTerm));
            
                if(ret==TMSAPIClass.TMS_APIFail){
                    if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(previousThesaurusName.getValue());
                    }
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return TARGET_USER_CREATED_BY_LINKS_DELETION_FAILED;
                }
                
            }
            if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(previousThesaurusName.getValue());
            }
            TA.GetThesaurusNameWithoutPrefix(previousThesaurusName);
            if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            
            //Starting modified_by links deletion pointing to Editor node that will be merged to newIdentifier Editor name selected
            for(int i=0; i<oldTermModifiedNodes.size() ; i++ ){
                String targetEditorTerm = oldTermModifiedNodes.get(i);
                long oldDeleteIdL         = oldTermModiifedLinkIds.get(i);
                
                //int ret = WTA.DeleteNewDescriptorAttribute(SessionUserInfo.selectedThesaurus, oldDeleteId, new StringObject(targetEditorTerm));
            
                int ret = TA.CHECK_DeleteNewDescriptorAttribute(oldDeleteIdL, new StringObject(targetEditorTerm));
                if(ret==TMSAPIClass.TMS_APIFail){
                    if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(previousThesaurusName.getValue());
                    }
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return TARGET_USER_MODIFIED_BY_LINKS_DELETION_FAILED; 
                }
            }
            if(previousThesaurusName.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                    TA.SetThesaurusName(previousThesaurusName.getValue());
                }
            
            //copy links copied to newIdentifier Editor node
            //First find out which nodes are already pointing to newIdentiifer in order to ignore if needed
            
            //get existing created_by links-nodes to newIdentifier editor and exclude them from creation
            Q.reset_name_scope();
            Q.set_current_node(newUserObj);
            int set_existing_created_by_links = Q.get_link_to_by_category(0, createdByClassObj, createdByLinkObj);
            Q.reset_set(set_existing_created_by_links);
            
            int set_existing_created_by_nodes = Q.get_from_value(set_existing_created_by_links );
            Q.reset_set(set_existing_created_by_nodes);
            Q.reset_set(set_created_by_nodes_for_transfer);
            Q.set_difference(set_created_by_nodes_for_transfer, set_existing_created_by_nodes);
            Q.reset_set(set_created_by_nodes_for_transfer);
            
            //get existing modified_by links-nodes to newIdentifier editor and exclude them from creation
            Q.reset_name_scope();
            long newIdetifierSysIdL = Q.set_current_node(newUserObj);
            int set_existing_modified_by_links = Q.get_link_to_by_category(0, modifiedByClassObj, modifiedByLinkObj);
            Q.reset_set(set_existing_modified_by_links);
            
            int set_existing_modified_by_nodes = Q.get_from_value(set_existing_modified_by_links );
            Q.reset_set(set_existing_modified_by_nodes);
            Q.reset_set(set_modified_by_nodes_for_transfer);
            Q.set_difference(set_modified_by_nodes_for_transfer, set_existing_modified_by_nodes);
            Q.reset_set(set_modified_by_nodes_for_transfer);
            
            
            //free some sets
            Q.free_set(set_created_by_links);
            Q.free_set(set_modified_by_links);
            Q.free_set(set_existing_created_by_links);
            Q.free_set(set_existing_modified_by_links);
            Q.free_set(set_existing_created_by_nodes);
            Q.free_set(set_existing_modified_by_nodes);
            
            // now in sets: set_created_by_nodes_for_transfer AND: set_created_by_nodes_for_transfer we have the nodes that must 
            // be linked to newIdentifier node.
            
            StringObject currentThesEditorClass = new StringObject();
            dbtr.getThesaurusClass_Editor(SessionUserInfo.selectedThesaurus,Q, sis_session.getValue(), currentThesEditorClass);
            if(dbGen.NodeBelongsToClass(oldUserObj, currentThesEditorClass, false, Q, sis_session) && 
                    dbGen.NodeBelongsToClass(newUserObj, currentThesEditorClass, false, Q, sis_session)==false){
                Q.reset_name_scope();
                //Q.set_current_node(EditorClass);
                Identifier fromIdentifier =new Identifier(newUserObj.getValue());
                Identifier toIdentifier =  new Identifier(currentThesEditorClass.getValue());
                if(Q.CHECK_Add_Instance(fromIdentifier, toIdentifier) ==QClass.APIFail){
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return FORMER_USER_INSTANCE_ADDITION_FAILED;
                }
            }
            
            //a cmv containing the newIdentifier Editor Node to which links will be transferred form oldIdentifier Editor
            CMValue to = new CMValue();
            to.assign_node(newIdentifier.getLogicalName(), newIdetifierSysIdL);
            
            Vector<StringObject> createdByNodesToCreate = new Vector<StringObject>();
            Vector<StringObject> modifiedByNodesToCreate = new Vector<StringObject>();
            //read which terms need created_by addition
            Q.reset_set(set_created_by_nodes_for_transfer);
            
            Vector<Return_Nodes_Row> retNodeVals = new Vector<Return_Nodes_Row>();
            if(Q.bulk_return_nodes(set_created_by_nodes_for_transfer, retNodeVals)!=QClass.APIFail){
                for(Return_Nodes_Row row: retNodeVals){
                    createdByNodesToCreate.add(new StringObject(row.get_v1_cls_logicalname()));
                }
            }
            
            /*
            while(Q.retur_nodes(set_created_by_nodes_for_transfer, cls)!=QClass.APIFail){
                createdByNodesToCreate.add(new StringObject(cls.getValue()));
            }
            */
            //read which terms need modified_by addition
            Q.reset_set(set_modified_by_nodes_for_transfer);
            retNodeVals.clear();
            if(Q.bulk_return_nodes(set_modified_by_nodes_for_transfer, retNodeVals)!=QClass.APIFail){
                for(Return_Nodes_Row row: retNodeVals){
                    modifiedByNodesToCreate.add(new StringObject(row.get_v1_cls_logicalname()));
                }
            }
            /*
            while(Q.retur_nodes(set_modified_by_nodes_for_transfer, cls)!=QClass.APIFail){
                modifiedByNodesToCreate.add(new StringObject(cls.getValue()));
            }
            */
            
            int createdByCatSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node(createdByClassObj);
            Q.set_current_node(createdByLinkObj);
            Q.set_put(createdByCatSet);
            
            StringObject prevThes = new StringObject();
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            
            for(int i=0; i< createdByNodesToCreate.size(); i++){
                StringObject targetDescriptor = createdByNodesToCreate.get(i);
                int ret = TA.CHECK_CreateNewDescriptorAttribute(new StringObject(), targetDescriptor, to, createdByCatSet);
                if (ret ==TMSAPIClass.TMS_APIFail){

                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return FORMER_USER_CREATED_BY_LINKS_ADDITION_FAILED;
                }
            }
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            Q.free_set(createdByCatSet);
            
            
            int modifiedByCatSet = Q.set_get_new();
            Q.reset_name_scope();
            Q.set_current_node(modifiedByClassObj);
            Q.set_current_node(modifiedByLinkObj);
            Q.set_put(modifiedByCatSet);
            
            prevThes = new StringObject("");
            TA.GetThesaurusNameWithoutPrefix(prevThes);
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(SessionUserInfo.selectedThesaurus);
            }
            for(int i=0; i< modifiedByNodesToCreate.size(); i++){
                StringObject targetDescriptor = modifiedByNodesToCreate.get(i);
                int ret = TA.CHECK_CreateNewDescriptorAttribute( new StringObject(), targetDescriptor, to, modifiedByCatSet);
                if (ret ==TMSAPIClass.TMS_APIFail){

                    //reset to previous thesaurus name if needed
                    if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                        TA.SetThesaurusName(prevThes.getValue());
                    }
                    Q.free_set(createdByCatSet);
                    //abort transaction and close connection
                    Q.free_all_sets();
                    Q.TEST_abort_transaction();
                    dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                    return FORMER_USER_MODIFIED_BY_LINKS_ADDITION_FAILED;
                }
            }
            Q.free_set(modifiedByCatSet);
            //reset to previous thesaurus name if needed
            if(prevThes.getValue().equals(SessionUserInfo.selectedThesaurus)==false){
                TA.SetThesaurusName(prevThes.getValue());
            }
            Q.free_set(createdByCatSet);
            
            //links were deleted and created from current thesaurus. Continue to next thesaurus.
        }
        
        //Now oldIdentifier has transfered all of its links. thus it is ready to be deleted 
        Q.reset_name_scope();
        if(Q.CHECK_Delete_Node(oldIdentifier)==QClass.APIFail){

            //abort transaction and close connection
            Q.free_all_sets();
            Q.TEST_abort_transaction();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            
            return TARGET_USER_DELETION_FAILED;//"Failure of deleting user '" + targetUser +"' from the database.";
        }
        
        //commit transaction and close connection
        Q.free_all_sets();
        Q.TEST_end_transaction();
        dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);

        return TMS_USER_OPERATION_SUCCEDED;
    }
}
