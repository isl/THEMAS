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
 * WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package LoginAdmin;

import Users.UsersClass;
import DB_Admin.CommonUtilsDBadmin;
import DB_Admin.ConfigDBadmin;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;

/*---------------------------------------------------------------------
                            HiddenActions
-----------------------------------------------------------------------
  Servlet for listing a set of THEMAS hidden actions
----------------------------------------------------------------------*/
public class HiddenActions extends HttpServlet {
    
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
	request.setCharacterEncoding("UTF-8");

        String basePath = request.getSession().getServletContext().getRealPath("");
        if(Parameters.BaseRealPath.length()==0){
            Parameters.BaseRealPath = basePath;
        }
	HttpSession session = request.getSession();	
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request); 
        
        PrintWriter out = response.getWriter();
        
        try{
        
            UsersClass tmsUsers = new UsersClass();
            String username = request.getParameter("username");
            String password = tmsUsers.getMD5Hex(request.getParameter("password"));
            String selectedThesaurusNAME = "";    

            // in case of expired session
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (sessionInstance.getAttribute("SessionUser") == null ) {
                // Links servlet NOT called by Login Page
                if ((username == null) || (password == null)) {
                    response.sendRedirect("LoginAdmin");
                    return;
                }
                // Links servlet called by Login Page
                else {
                    boolean loginSucceded = false;
                    synchronized(session){
                        
                        loginSucceded = tmsUsers.Authenticate(request, session,sessionInstance,username, password, selectedThesaurusNAME);
                    }
                    if (loginSucceded == false) {
                        response.sendRedirect("LoginAdmin");
                        return;
                    }
                    else if (loginSucceded == true) { // login succeded but user is not ADMINISTRATOR
                        SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
                        if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator) == false) {
                            session.setAttribute("SessionUser", null);
                            response.sendRedirect("LoginAdmin");
                            return;                        
                        }
                    }
                } 
            }
            else if(!SessionUserInfo.servletAccessControl(this.getClass().getName())){
                response.sendRedirect("LoginAdmin");
                return;
            }
            // ATTENTION: Parameters.initParams() must be called after tmsUsers.Authenticate()
            Parameters.initParams(getServletContext());

            // write XML
            //String basePath = request.getSession().getServletContext().getRealPath("");
            ConfigDBadmin config = new ConfigDBadmin(basePath);
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);          
            HiddenActionsCommon HiddenActionsCommon = new HiddenActionsCommon();
            HiddenActionsCommon.writeXML(sessionInstance, context, ConstantParameters.LMENU_HiddenActions_DIV, out, common_utils);
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
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