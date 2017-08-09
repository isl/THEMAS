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


import DB_Admin.ScheduledBackups;
import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import java.util.*;

/*---------------------------------------------------------------------
Links
-----------------------------------------------------------------------
First servlet after successful login to THEMAS application
----------------------------------------------------------------------*/
public class Links extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);
        
        PrintWriter out = response.getWriter();

        try {
            ServletContext context = getServletContext();
            String tab = request.getParameter("tab");
            String CheckLength = request.getParameter("CheckLength");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String selectedThesaurusNAME = request.getParameter("selectedThesaurusNAME");            
            String pathToErrorsXML = context.getRealPath("/translations/Consistencies_Error_Codes.xml");
            String pathToMessagesXML = context.getRealPath("/translations/Messages.xml");
            String language = context.getInitParameter("LocaleLanguage");
            String country = context.getInitParameter("LocaleCountry");

            Utilities u = new Utilities();
            UsersClass tmsUsers = new UsersClass();
            StringBuffer xml = new StringBuffer();
            String currentTABup = new String("SearchCriteria");
            
            // in case of expired session
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

            if (sessionInstance.getAttribute("SessionUser") == null) {
                // Links servlet NOT called by Login Page
                if ((username == null) || (password == null)) {
                    response.sendRedirect("Index");
                    return;
                } // Links servlet called by Login Page
                else {
                    boolean loginSucceded = false;
                   // synchronized (session) {
                        session = request.getSession();
                        loginSucceded = tmsUsers.Authenticate(request,session, sessionInstance,username, password, selectedThesaurusNAME);
                    //}
                    if (loginSucceded == false) {
                        response.sendRedirect("Index");
                        return;
                    }
                }
            } else if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Reader) == true // reader case
                    // and (tab == NULL OR NOT one of the Search tabs)
                    && (tab == null ||
                    (tab.equals("SearchCriteria") == false &&
                    tab.equals("HierarchiesSearchCriteria") == false &&
                    tab.equals("FacetsSearchCriteria") == false &&
                    tab.equals("SourcesSearchCriteria") == false))) {
                // in case a reader tried to visit Links servlet by typing the full URL
                sessionInstance.setAttribute("SessionUser", null);
                response.sendRedirect("Index");
                return;
            }
            // ATTENTION: Parameters.initParams() must be called after tmsUsers.Authenticate()
            Parameters.initParams(getServletContext());

            
            if (Parameters.ENABLE_AUTOMATIC_BACKUPS) {
                Parameters.ENABLE_AUTOMATIC_BACKUPS = false;
                Calendar date = Calendar.getInstance();
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Backups->First Time Index is reached: " + date.getTime());
                /*
                if(Parameters.timer!=null){
                    Parameters.timer.cancel();
                    Parameters.timer =null;
                }
                Parameters.timer = new Timer();*/
                Timer timer = new Timer();
                ScheduledBackups task = new ScheduledBackups(request,sessionInstance, pathToErrorsXML, language, country);


                
                date.add(Calendar.DAY_OF_MONTH, 1);
                date.set(Calendar.HOUR_OF_DAY, Parameters.AUTOMATIC_BACKUPS_START_HOUR);//0-23
                date.set(Calendar.MINUTE, Parameters.AUTOMATIC_BACKUPS_START_MIN);
                date.set(Calendar.SECOND, Parameters.AUTOMATIC_BACKUPS_START_SEC);

                
                /*
                date.set(Calendar.MILLISECOND, 0);
                date.add(Calendar.SECOND, 8);
                */
                Long interval  = new Long(Parameters.AUTOMATIC_BACKUPS_HOURS_INTERVAL*60*60 * 1000);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Automatic Backups->Starting Date: " + date.getTime() +" Interval = " + interval.longValue());
                //Parameters.timer.scheduleAtFixedRate(task, date.getTime(), interval.longValue());
                timer.scheduleAtFixedRate(task, date.getTime(), interval.longValue());
            }

            if (tab != null && tab.length() > 0) {
                currentTABup = tab;
                
            }
            
            String leftMenuMode = ConstantParameters.LMENU_TERMS;
            if(currentTABup.compareTo("HierarchiesSearchCriteria")==0){
                leftMenuMode = ConstantParameters.LMENU_HIERARCHIES;
            }
            else if(currentTABup.compareTo("FacetsSearchCriteria")==0){
                leftMenuMode = ConstantParameters.LMENU_FACETS;
            }
            else if(currentTABup.compareTo("SourcesSearchCriteria")==0){
                leftMenuMode = ConstantParameters.LMENU_SOURCES;
            }

            SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            String XMLMiddleCustomVal = "";
            if(CheckLength!=null && CheckLength.equals("true")){
                DBGeneral dbGen = new DBGeneral();
                //pathToMessagesXML
                //SearchCriteria/LongName
                StringObject errorObj = new StringObject("");
                dbGen.Translate(errorObj, "root/SearchCriteria/LongName", null, pathToMessagesXML);
                XMLMiddleCustomVal+=errorObj.getValue();//"Check Length";
            }
            xml.append(u.getXMLStart(leftMenuMode));
            xml.append(u.getXMLMiddle(XMLMiddleCustomVal, currentTABup));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            
            
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        finally {  
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}