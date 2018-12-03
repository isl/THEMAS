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


import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import Users.UserInfoClass;
import Users.UsersClass;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import java.io.*;
import javax.servlet.*;
import java.util.Calendar;
import javax.servlet.http.*;
import java.util.Enumeration;

/*---------------------------------------------------------------------
                    ApplicationBasicServlet
-----------------------------------------------------------------------
Servlet being the base class of ALL servlets for this application
----------------------------------------------------------------------*/
public class ApplicationBasicServlet extends HttpServlet {

    
    /*---------------------------------------------------------------------
                                init()
    -----------------------------------------------------------------------
    FUNCTION: - restarts SIS server if it is closed
    CALLED BY: ALL servlets for this application
    ----------------------------------------------------------------------*/
    protected void init(HttpServletRequest request, HttpServletResponse response,SessionWrapperClass sessionInstance) throws IOException {            
        try{
        // check SIS server
        HttpSession session = request.getSession();	
        sessionInstance.readSession(session,request); 
        String  basePath = request.getSession().getServletContext().getRealPath("");
        if(Parameters.BaseRealPath.length()==0){
            Parameters.BaseRealPath = basePath;
        }
        
        
        
        new CommonUtilsDBadmin(new ConfigDBadmin(basePath)).RestartDatabaseIfNeeded();
        
        String overrideUILangParameter = request.getParameter("lang");
        
        
        
        Calendar cal = Calendar.getInstance();
        UsersClass tmsUsers = new UsersClass();
        UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        
        if(overrideUILangParameter!=null && overrideUILangParameter.trim().length()>0){
            overrideUILangParameter = overrideUILangParameter.trim().toLowerCase();
            if(Parameters.SupportedUILangCodes!=null && SessionUserInfo!=null && Parameters.SupportedUILangCodes.containsKey(overrideUILangParameter)){
                SessionUserInfo.UILang = Parameters.SupportedUILangCodes.get(overrideUILangParameter);
            }
        }
        
        // check the case of the servlet being called without login (URL intergrated to other web sites) 
        // g.e. SearchResults_Hierarchies?external_user=readerAAA&external_thesaurus=AAA       (for specific thesaurus reader)
        // g.e. SearchResults_Hierarchies?external_user=readerAllThesauri&external_thesaurus=* (for ALL thesauri reader)
        
        
        
            String external_user = request.getParameter("external_user");
            String external_thesaurus = request.getParameter("external_thesaurus");
            String checkIfXMLStream = request.getParameter("answerType");
            boolean skipSessionUpdate = false;
            if (external_user != null && external_thesaurus != null) { // in case of servlet external call
                // authenticate the given parameters
                if(checkIfXMLStream==null || checkIfXMLStream.length()==0){
                    checkIfXMLStream = request.getParameter("mode");
                }
                if(checkIfXMLStream!=null || (checkIfXMLStream!=null && checkIfXMLStream.compareTo(Utils.ConstantParameters.XMLSTREAM)==0)){
                    skipSessionUpdate = true;
                }                
                
                tmsUsers.Authenticate(request, session, sessionInstance,external_user, tmsUsers.getMD5Hex(""), external_thesaurus);
                Parameters.initParams(getServletContext());
                
                String ServletParametersDescription = GetServletParametersDescription(request);
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+ cal.getTime() + " Servlet: " + request.getServletPath() + " called from user " + external_user + " with..." + ServletParametersDescription);
                    
                if(!skipSessionUpdate){
                    sessionInstance.writeBackToSession(session);
                }
                
            }
            else{
                
                if(SessionUserInfo!=null){
                    String ServletParametersDescription = GetServletParametersDescription(request);
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+cal.getTime() + " Servlet: " + request.getServletPath() + " called from user " + SessionUserInfo.name + " with..." + ServletParametersDescription);
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+ cal.getTime() + " Invalidating session " + session.getId() + " because SessionUser is null" );
                    session.invalidate();
                }
                
            }
        
         }
        catch(Exception e){
            Utils.StaticClass.webAppSystemOutPrintln("Exception catched in ApplicationBasicServlet init function for servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }
    }
    
    /*----------------------------------------------------------------------
                        GetServletParametersDescription()
    ------------------------------------------------------------------------*/
    String GetServletParametersDescription(HttpServletRequest request) throws IOException {

        UserInfoClass SessionUserInfo = null;
        try{
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request);
             SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        }catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }

        String ParametersDescription = (SessionUserInfo==null)?"":" Thesaurus: "+ SessionUserInfo.selectedThesaurus +" ";
        Utilities u = new Utilities();
        Enumeration ParameterNames = request.getParameterNames();
        
        while (ParameterNames.hasMoreElements()) {
            String paramName = (String) ParameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                ParametersDescription += " Parameter: " + u.getDecodedParameterValue(paramName) + " with value: " + u.getDecodedParameterValue(paramValue);
            } else {
                //params.put(paramName, paramValues);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < paramValues.length; i++) {
                    sb.append(paramValues[i]);
                    if (i < paramValues.length - 1) {
                        sb.append(",");
                    }
                }
                ParametersDescription += " Parameter: " + u.getDecodedParameterValue(paramName) + " with values: " + u.getDecodedParameterValue(sb.toString());
            }
        }
        
        return ParametersDescription;
    }    
    
    /*----------------------------------------------------------------------
                        SystemIsLockedForAdministrativeJobs()
    ------------------------------------------------------------------------*/
    protected boolean SystemIsLockedForAdministrativeJobs(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // in case the system is locked by an administrator for doing administrative jobs
        // redirect to a page ("System maintenance")
        
        if(Parameters.BaseRealPath.length()==0){
            Parameters.BaseRealPath = request.getSession().getServletContext().getRealPath("");
        }
        
        if (DB_Admin.DBAdminUtilities.isSystemLocked()) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"SYSTEM_IS_LOCKED!!!!!!!!!!!!!!");
            request.getSession().invalidate();
            response.sendRedirect("SystemIsUnderMaintenance");
            return true;
        }        
        return false;
    }
    
}
