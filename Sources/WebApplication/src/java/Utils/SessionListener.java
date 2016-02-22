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
package Utils;

import DB_Admin.ConfigDBadmin;
import DB_Admin.DBAdminUtilities;
import Users.UserInfoClass;
import java.io.*;
import javax.servlet.http.*;

/*-------------------------------------------------------------------
                    class SessionListener
  -------------------------------------------------------------------
  class to be notified of changes to:
    - the list of active sessions (HttpSessionListener)
    - the attribute lists of active sessions (HttpSessionAttributeListener)
  in this web application.
  Included in web.xml file as:
    <listener>
      <listener-class>SessionListener</listener-class>
    </listener>
 --------------------------------------------------------------------*/
public final class SessionListener implements HttpSessionListener, HttpSessionAttributeListener {
  public static int sessionsNO = 0;
  public static int activesessionsNO = 0;
  private static final String LogFilesFolder = "LogFiles";
  private static final String UploadsFolder = "Uploads";

  /*-----------------------------------------------------------------------
                           SessionListener()
  -------------------------------------------------------------------------
  FUNCTION: - overriden contructor for this class
  -------------------------------------------------------------------------*/
  public SessionListener() {
    super();
  }

  /*-----------------------------------------------------------------------
                        sessionCreated()
  -------------------------------------------------------------------------
  INPUT: - HttpSessionEvent se: the notification event
  FUNCTION: notification that a session was created
  -------------------------------------------------------------------------*/
  public void sessionCreated(HttpSessionEvent se) {
    HttpSession session=se.getSession();
    ++sessionsNO;
    ++activesessionsNO;
    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+">>>>>>>>>>>>>> sessionCreated: sessionsNO = " + sessionsNO + " activesessionsNO = " + activesessionsNO);
    // garbage collection in case activesessionsNO == 1 (the 1st)
    if (activesessionsNO == 1) {
        SVGtempFilesGarbageCollection(session);
        SaveAll_Search_ResultsGarbageCollection(session);
        mainFolder_GarbageCollection(session,LogFilesFolder);
        mainFolder_GarbageCollection(session,UploadsFolder);
        //sync the static variable of system locked value with the existence or not of the lock file
        if(DB_Admin.DBAdminUtilities.isSystemLocked()){
            DBAdminUtilities dbadmin = new DBAdminUtilities();
            String baseAppPath = Parameters.BaseRealPath;
            if(baseAppPath.length()>0){
                ConfigDBadmin config = new ConfigDBadmin(baseAppPath);
                dbadmin.LockSystemForAdministrativeJobs(config);
            }            
        }
        else{
            DBAdminUtilities dbadmin = new DBAdminUtilities();
            dbadmin.UnlockSystemForAdministrativeJobs();
        }
    }    
  }

    /*-----------------------------------------------------------------------
    sessionDestroyed()
    -------------------------------------------------------------------------
    INPUT: - HttpSessionEvent se: the notification event
    FUNCTION: notification that a session was invalidated
    -------------------------------------------------------------------------*/
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        if (activesessionsNO > 0) {
            --activesessionsNO;
        }
        UserInfoClass SessionUserInfo = (UserInfoClass) session.getAttribute("SessionUser");
        if (SessionUserInfo != null) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"EXIT:>>>>>>>>> sessionDestroyed: sessionsNO = " + sessionsNO + " activesessionsNO = " + activesessionsNO + " user: " + SessionUserInfo.name + " exited.");
        } else {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+">>>>>>>>>>>>>> sessionDestroyed: sessionsNO = " + sessionsNO + " activesessionsNO = " + activesessionsNO);
        }

    }

  /*-----------------------------------------------------------------------
            SVGtempFilesGarbageCollection()
  -------------------------------------------------------------------------
  FUNCTION: - clears the temporary SVG files stored in SVGproducer\SVG_temporary_files
  -----------------------------------------------------------------------*/
  private void SVGtempFilesGarbageCollection(HttpSession session) {
    String webAppSVG_temporary_filesPath = session.getServletContext().getInitParameter("SVG_temporary_filesPath"); // g.e. "SVGproducer/SVG_temporary_files"
    String webAppSVG_temporary_filesAbsolutePath = session.getServletContext().getRealPath("/"+webAppSVG_temporary_filesPath);
    File DirectoryContentsFile = new File(webAppSVG_temporary_filesAbsolutePath);
    if(DirectoryContentsFile==null|| DirectoryContentsFile.list()==null){
        return;
    }
    String[] DirectoryContents = DirectoryContentsFile.list();
    int DirectoryContentsCount = DirectoryContents.length;
    for (int i = 0; i < DirectoryContentsCount; i++) {
        File SVG_temp_file = new File(webAppSVG_temporary_filesAbsolutePath + "\\" + DirectoryContents[i]);
        SVG_temp_file.delete();
    }
  }  

  /*-----------------------------------------------------------------------
            SaveAll_Search_ResultsGarbageCollection()
  -------------------------------------------------------------------------
  FUNCTION: - clears the temporary xml and html files stored in Save_Results_Displays\Save_Results_temporary_files
  -----------------------------------------------------------------------*/
  private void SaveAll_Search_ResultsGarbageCollection(HttpSession session){
      
    String mainFolder =  session.getServletContext().getInitParameter("Save_Results_Folder");
    String webAppSave_temporary_filesPath = session.getServletContext().getInitParameter("Save_Results_Temp_Folder"); 
    String webAppSave_temporary_filesAbsolutePath = session.getServletContext().getRealPath("/"+mainFolder + "/"+webAppSave_temporary_filesPath);
    String[] DirectoryContents = new File(webAppSave_temporary_filesAbsolutePath).list();
    int DirectoryContentsCount = DirectoryContents.length;
    for (int i = 0; i < DirectoryContentsCount; i++) {
        File Save_temp_file = new File(webAppSave_temporary_filesAbsolutePath + "\\" + DirectoryContents[i]);
        Save_temp_file.delete();
    }
    
  }
  
  private void mainFolder_GarbageCollection(HttpSession session,String mainFolder){
    
    String webAppSave_temporary_filesAbsolutePath = session.getServletContext().getRealPath("/"+mainFolder);
    String[] DirectoryContents = new File(webAppSave_temporary_filesAbsolutePath).list();
    int DirectoryContentsCount = DirectoryContents.length;
    for (int i = 0; i < DirectoryContentsCount; i++) {
        File Save_temp_file = new File(webAppSave_temporary_filesAbsolutePath + "/" + DirectoryContents[i]);
        Save_temp_file.delete();
    }
    
  }
  /*-----------------------------------------------------------------------
                        attributeAdded()
  -------------------------------------------------------------------------
  INPUT: - HttpSessionBindingEvent be: the notification event
  FUNCTION: Notification that an attribute has been added to a session.
            Called after the attribute is added
  -------------------------------------------------------------------------*/
  public void attributeAdded(HttpSessionBindingEvent be) {
  }

  /*-----------------------------------------------------------------------
                        attributeRemoved()
  -------------------------------------------------------------------------
  INPUT: - HttpSessionBindingEvent be: the notification event
  FUNCTION: Notification that an attribute has been removed from a session.
            Called after the attribute is removed
  -------------------------------------------------------------------------*/
  public void attributeRemoved(HttpSessionBindingEvent be) {
  }

  /*-----------------------------------------------------------------------
                        attributeReplaced()
  -------------------------------------------------------------------------
  INPUT: - HttpSessionBindingEvent be: the notification event
  FUNCTION: Notification that an attribute has been replaced in a session.
            Called after the attribute is replaced
  -------------------------------------------------------------------------*/
  public void attributeReplaced(HttpSessionBindingEvent be) {
  }

}
