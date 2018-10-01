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
package LoginAdmin;

import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.SessionListener;
import Utils.Parameters;
import Utils.Utilities;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/*---------------------------------------------------------------------
                        LoginAdmin
-----------------------------------------------------------------------
Login page for administrators ONLY, to access hidden actions
----------------------------------------------------------------------*/
public class LoginAdmin extends HttpServlet {

    /*---------------------------------------------------------------------
                                doGet()
    ----------------------------------------------------------------------*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request); 
        
        PrintWriter out = response.getWriter();
        
        try{
            String logout = request.getParameter("logout");
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (logout != null) {
                synchronized (session) {

                    session.setAttribute("SessionUser", null);
                    if (SessionListener.activesessionsNO >0) {
                        session.invalidate();
                        if (SessionUserInfo != null) {
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"EXIT: user " + SessionUserInfo.name + " just Logged Out.");
                            SessionUserInfo = null;
                        }

                    }
                }
            }

            if (SessionUserInfo != null && SessionUserInfo.userGroup.equals(ConstantParameters.Group_Administrator)==true ) {
                response.setStatus(response.SC_MOVED_TEMPORARILY);
                response.setHeader("Location", "HiddenActions");

                out.close();
                return;
            } else {
                if (SessionListener.activesessionsNO >0) {
                    session.invalidate();
                }
            }
            String uiLang = "";
            if(SessionUserInfo!=null && SessionUserInfo.UILang!=null && SessionUserInfo.UILang.trim().length()>0){
                uiLang = SessionUserInfo.UILang;
            }
            else /*if(uiLang == null || uiLang.trim().length()==0)*/{
                uiLang = Parameters.UILang;
                
                if(uiLang == null || uiLang.trim().length()==0){
                    uiLang = getServletContext().getInitParameter("UILanguage");
                }
            }
            DisplayLoginPage(out, sessionInstance,uiLang);
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

     /*---------------------------------------------------------------------
                                DisplayLoginPage()
    ----------------------------------------------------------------------*/
    public void DisplayLoginPage(PrintWriter out, SessionWrapperClass sessionInstance, final String uiLang) {
        StringBuffer xml = new StringBuffer();

        xml.append(getXMLLoginStart(uiLang));
        xml.append(getXMLEnd());
        Utilities u = new Utilities();
        u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/HiddenActions/LoginAdmin.xsl");
    }
    
    /*---------------------------------------------------------------------
                        getXMLEnd()
    ----------------------------------------------------------------------*/
    public String getXMLEnd() {
        String XMLEnd =
                "</page>";
        return XMLEnd;
    }

    /*---------------------------------------------------------------------
                            getXMLLoginStart()
    ----------------------------------------------------------------------*/
    public String getXMLLoginStart(final String uiLang) {
        //getServletContext().getInitParameter("UILanguage")
        String XMLLoginStart =
                ConstantParameters.xmlHeader +
                "<page language=\""+uiLang+"\" mode=\"insert\">" +
                "<content>" +
                "<inputs>" +
                "<login></login>" +
                "<password></password>" +
                "</inputs>" +
                "</content>";

        return XMLLoginStart;
    }
}