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

import Utils.SessionWrapperClass;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Elias
 */
public class UsersClassTest {
    
    public UsersClassTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getMD5Hex method, of class UsersClass.
    
    @Test
    public void testGetMD5Hex() {
        System.out.println("getMD5Hex");
        String inputString =""; //(Empty string)
        String expResult = "d41d8cd98f00b204e9800998ecf8427e";        
        UsersClass instance = new UsersClass();
        String result = instance.getMD5Hex(inputString);
        System.out.println("result of "+inputString+": " + result);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    } */
    
    /**
     * Test of TEST_AddTMSUser method, of class UsersClass.
     
    @Test
    public void testTEST_AddTMSUser() {
        System.out.println("TEST_AddTMSUser");
        HttpServletRequest request = null;
        UsersClass instance = new UsersClass();
        instance.TEST_AddTMSUser(request);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of TEST_AddNewThesaurusForCurrentTMSUser method, of class UsersClass.
     
    @Test
    public void testTEST_AddNewThesaurusForCurrentTMSUser() {
        System.out.println("TEST_AddNewThesaurusForCurrentTMSUser");
        HttpServletRequest request = null;
        SessionWrapperClass sessionInstance = null;
        UsersClass instance = new UsersClass();
        instance.TEST_AddNewThesaurusForCurrentTMSUser(request, sessionInstance);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getResultsInXml method, of class UsersClass.
     
    @Test
    public void testGetResultsInXml() {
        System.out.println("getResultsInXml");
        HttpServletRequest request = null;
        ArrayList<UserInfoClass> allUsers = null;
        String[] output = null;
        StringBuffer XMLresults = null;
        UsersClass instance = new UsersClass();
        instance.getResultsInXml(request, allUsers, output, XMLresults);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    

    /**
     * Test of AddNewThesaurusForCurrentTMSUser method, of class UsersClass.
     
    @Test
    public void testAddNewThesaurusForCurrentTMSUser() {
        System.out.println("AddNewThesaurusForCurrentTMSUser");
        String THEMASUsersFileName = "";
        SessionWrapperClass sessionInstance = null;
        String NewThesaurusName = "";
        UsersClass instance = new UsersClass();
        instance.AddNewThesaurusForCurrentTMSUser(THEMASUsersFileName, sessionInstance, NewThesaurusName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */

    /**
     * Test of SearchTMSUser method, of class UsersClass.
     
    @Test
    public void testSearchTMSUser() {
        System.out.println("SearchTMSUser");
        HttpServletRequest request = null;
        String username = "";
        UsersClass instance = new UsersClass();
        UserInfoClass expResult = null;
        UserInfoClass result = instance.SearchTMSUser(request, username);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of GetTMSUsersNames method, of class UsersClass.
     
    @Test
    public void testGetTMSUsersNames() {
        System.out.println("GetTMSUsersNames");
        HttpServletRequest request = null;
        UsersClass instance = new UsersClass();
        ArrayList<String> expResult = null;
        ArrayList<String> result = instance.GetTMSUsersNames(request);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of GetTMSUsers_GroupsOfThesaurus method, of class UsersClass.
     
    @Test
    public void testGetTMSUsers_GroupsOfThesaurus() {
        System.out.println("GetTMSUsers_GroupsOfThesaurus");
        HttpServletRequest request = null;
        String targetThesaurus = "";
        ArrayList<String> UserNamesV = null;
        ArrayList<String> GroupsV = null;
        UsersClass instance = new UsersClass();
        instance.GetTMSUsers_GroupsOfThesaurus(request, targetThesaurus, UserNamesV, GroupsV);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of GetThesaurusSetOfTMSUser method, of class UsersClass.
     
    @Test
    public void testGetThesaurusSetOfTMSUser() {
        System.out.println("GetThesaurusSetOfTMSUser");
        HttpServletRequest request = null;
        String username = "";
        UsersClass instance = new UsersClass();
        ArrayList<String> expResult = null;
        ArrayList<String> result = instance.GetThesaurusSetOfTMSUser(request, username);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of CreateUser method, of class UsersClass.
     
    @Test
    public void testCreateUser() {
        System.out.println("CreateUser");
        HttpServletRequest request = null;
        SessionWrapperClass sessionInstance = null;
        boolean createUserAsAdministrator = false;
        String username = "";
        String password = "";
        String description = "";
        ArrayList thesaurusV = null;
        ArrayList groupV = null;
        int SimpleCreateOrOlderUserHandle = 0;
        String olderUserCreateChoice = "";
        String olderUserRenameName = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.CreateUser(request, sessionInstance, createUserAsAdministrator, username, password, description, thesaurusV, groupV, SimpleCreateOrOlderUserHandle, olderUserCreateChoice, olderUserRenameName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of EditUser method, of class UsersClass.
     
    @Test
    public void testEditUser() {
        System.out.println("EditUser");
        HttpServletRequest request = null;
        SessionWrapperClass sessionInstance = null;
        boolean deletePassword = false;
        boolean deleteUser = false;
        boolean isAdmin = false;
        String oldUserName = "";
        String Newusername = "";
        String description = "";
        ArrayList<String> selectThesaurusVector = null;
        ArrayList<String> selectUserGroupVector = null;
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.EditUser(request, sessionInstance, deletePassword, deleteUser, isAdmin, oldUserName, Newusername, description, selectThesaurusVector, selectUserGroupVector);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of EditTargetAndOlderUser method, of class UsersClass.
     
    @Test
    public void testEditTargetAndOlderUser() {
        System.out.println("EditTargetAndOlderUser");
        SessionWrapperClass sessionInstance = null;
        ServletContext context = null;
        String THEMASUsersFileName = "";
        String targetUser = "";
        String targetUserDescription = "";
        String olderUserName = "";
        String OlderUserRenameName = "";
        String choice = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.EditTargetAndOlderUser(sessionInstance, context, THEMASUsersFileName, targetUser, targetUserDescription, olderUserName, OlderUserRenameName, choice);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of ShareThesaurus method, of class UsersClass.
     
    @Test
    public void testShareThesaurus() {
        System.out.println("ShareThesaurus");
        HttpServletRequest request = null;
        String targetThesaurus = "";
        ArrayList usersV = null;
        ArrayList groupsV = null;
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.ShareThesaurus(request, targetThesaurus, usersV, groupsV);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of DeleteThesaurusFromTMSUsers method, of class UsersClass.
     
    @Test
    public void testDeleteThesaurusFromTMSUsers() {
        System.out.println("DeleteThesaurusFromTMSUsers");
        HttpServletRequest request = null;
        String targetThesaurus = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.DeleteThesaurusFromTMSUsers(request, targetThesaurus);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of EditUserPassword method, of class UsersClass.
     
    @Test
    public void testEditUserPassword() {
        System.out.println("EditUserPassword");
        String THEMASUsersFileName = "";
        String targetUser = "";
        String oldUserPassword = "";
        String newUserPassword1 = "";
        String newUserPassword2 = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.EditUserPassword(THEMASUsersFileName, targetUser, oldUserPassword, newUserPassword1, newUserPassword2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of EditUserThesaurus method, of class UsersClass.
     
    @Test
    public void testEditUserThesaurus() {
        System.out.println("EditUserThesaurus");
        HttpServletRequest request = null;
        HttpSession session = null;
        SessionWrapperClass sessionInstance = null;
        String targetUser = "";
        String newUserThesaurus = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.EditUserThesaurus(request, session, sessionInstance, targetUser, newUserThesaurus);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of UserCanBeDeleted method, of class UsersClass.
     
    @Test
    public void testUserCanBeDeleted() {
        System.out.println("UserCanBeDeleted");
        HttpServletRequest request = null;
        SessionWrapperClass sessionInstance = null;
        String userName = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.UserCanBeDeleted(request, sessionInstance, userName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of ReadWebAppUsersXMLFile method, of class UsersClass.
     
    @Test
    public void testReadWebAppUsersXMLFile() {
        System.out.println("ReadWebAppUsersXMLFile");
        String WebAppUsersFileName = "";
        UsersClass instance = new UsersClass();
        ArrayList<UserInfoClass> expResult = null;
        ArrayList<UserInfoClass> result = instance.ReadWebAppUsersXMLFile(WebAppUsersFileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of Authenticate method, of class UsersClass.
     
    @Test
    public void testAuthenticate() {
        System.out.println("Authenticate");
        HttpServletRequest request = null;
        HttpSession session = null;
        SessionWrapperClass sessionInstance = null;
        String username = "";
        String password = "";
        String selectedThesaurus = "";
        UsersClass instance = new UsersClass();
        boolean expResult = false;
        boolean result = instance.Authenticate(request, session, sessionInstance, username, password, selectedThesaurus);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of UpdateSessionUserSessionAttribute method, of class UsersClass.
     
    @Test
    public void testUpdateSessionUserSessionAttribute() {
        System.out.println("UpdateSessionUserSessionAttribute");
        UserInfoClass SessionUserInfo = null;
        String selectedThesaurus = "";
        UsersClass instance = new UsersClass();
        instance.UpdateSessionUserSessionAttribute(SessionUserInfo, selectedThesaurus);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of SetSessionAttributeSessionUser method, of class UsersClass.
     
    @Test
    public void testSetSessionAttributeSessionUser() {
        System.out.println("SetSessionAttributeSessionUser");
        SessionWrapperClass sessionInstance = null;
        ServletContext context = null;
        String username = "";
        String password = "";
        String selectedThesaurus = "";
        String userGroup = "";
        String uiLang = "";
        UsersClass instance = new UsersClass();
        instance.SetSessionAttributeSessionUser(sessionInstance, context, username, password, selectedThesaurus, userGroup, uiLang);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of collectDBEditors method, of class UsersClass.
     
    @Test
    public void testCollectDBEditors() {
        System.out.println("collectDBEditors");
        ArrayList<String> resultvector = null;
        UsersClass instance = new UsersClass();
        instance.collectDBEditors(resultvector);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of renameEditor method, of class UsersClass.
     
    @Test
    public void testRenameEditor() {
        System.out.println("renameEditor");
        SessionWrapperClass sessionInstance = null;
        String oldName = "";
        String newName = "";
        UsersClass instance = new UsersClass();
        boolean expResult = false;
        boolean result = instance.renameEditor(sessionInstance, oldName, newName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of mergeEditorsTargetOverFormer method, of class UsersClass.
     
    @Test
    public void testMergeEditorsTargetOverFormer() {
        System.out.println("mergeEditorsTargetOverFormer");
        SessionWrapperClass sessionInstance = null;
        ServletContext context = null;
        String targetUser = "";
        String olderUserName = "";
        UsersClass instance = new UsersClass();
        int expResult = 0;
        int result = instance.mergeEditorsTargetOverFormer(sessionInstance, context, targetUser, olderUserName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
