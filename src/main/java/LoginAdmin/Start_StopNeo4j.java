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
                            Start_StopNeo4j
-----------------------------------------------------------------------
  Servlet for starting/stopping Neo4j Database
----------------------------------------------------------------------*/
public class Start_StopNeo4j extends HttpServlet {
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
	request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request);
        
        PrintWriter out = response.getWriter();
        
        try{
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName()) || SessionUserInfo.userGroup.equals(ConstantParameters.Group_Administrator)==false) {
                response.sendRedirect("Index");
                return;
            }
            String basePath = request.getSession().getServletContext().getRealPath("");
            ConfigDBadmin config = new ConfigDBadmin(basePath);
            CommonUtilsDBadmin common_utils = new CommonUtilsDBadmin(config);          

            // get servlet parameters
            String action = request.getParameter("action"); // null / START / STOP
            
            // do the action if any
            if (action != null) {
                if (action.compareTo("START") == 0) {
                    Utils.StaticClass.getDBService();
                    //boolean serverStarted = common_utils.StartDatabase();
                    
                }
                else { // stop the server
                    Utils.StaticClass.closeDb();
                    //common_utils.StopDatabase();
                }
            }

            // write XML

            HiddenActionsCommon HiddenActionsCommon = new HiddenActionsCommon();
            HiddenActionsCommon.writeXML(sessionInstance,context,ConstantParameters.LMENU_HiddenActions_DIV, out, common_utils);        
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