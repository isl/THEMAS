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

import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.SessionListener;
import Utils.Parameters;
import Utils.Utilities;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import neo4j_sisapi.*;
//import isl.dms.DMSException;
//import isl.dms.xml.XMLTransform;
import java.util.*;

/*---------------------------------------------------------------------
                SystemIsUnderMaintenance
-----------------------------------------------------------------------
Servlet where all servlet requests are redirected during an administrative job
----------------------------------------------------------------------*/
public class SystemIsUnderMaintenance extends ApplicationBasicServlet {
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
        /*String language = getServletContext().getInitParameter("LocaleLanguage");
        String country = getServletContext().getInitParameter("LocaleCountry");
        Locale targetLocale = new Locale(language, country);*/
        
        /*Initialize System Locale*/
        Parameters.initParams(context);
        
        //Parameters.UILang = language;
        //Parameters.PrimaryLang = language;
        
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request);
        
        PrintWriter out = response.getWriter();
        try{
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            String logout = request.getParameter("logout");
            if (logout != null) {
                synchronized (session) {

                    sessionInstance.setAttribute("SessionUser", null);
                    if (SessionListener.activesessionsNO >0) {
                        session.invalidate();
                        if (SessionUserInfo != null) {
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"EXIT: user " + SessionUserInfo.name + " just Logged Out.");
                            SessionUserInfo = null;
                        }

                    }
                }
            }

            if (SessionUserInfo != null) {
                response.setStatus(response.SC_MOVED_TEMPORARILY);
                response.setHeader("Location", "Links");
                sessionInstance.writeBackToSession(session);
                out.close();
                return;
            } else {
                if (SessionListener.activesessionsNO >0) {
                    session.invalidate();
                }
            }
            DisplaySystemIsUnderMaintenancePage(out, sessionInstance);
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
    //CODE COPIED FROM UTILITIES BECAUSE WE DO NOT WANT WERE PARAMETERS TO BE INITIALIZED
     /*---------------------------------------------------------------------
                        DisplaySystemIsUnderMaintenancePage()
    -----------------------------------------------------------------------
    INPUT: - PrintWriter out: the writer to be used
           - String path: the "THEMASLocation" initialization parameter of the application
    ----------------------------------------------------------------------*/
    public void DisplaySystemIsUnderMaintenancePage(PrintWriter out, SessionWrapperClass sessionInstance) {
        StringBuffer xml = new StringBuffer();

        xml.append(getXMLLoginStart("SystemIsUnderMaintenance.xsl"));
        xml.append(getXMLEnd());
        xslTransform(out, xml,sessionInstance.path + "/xml-xsl/SystemIsUnderMaintenance.xsl");
    }
    /*---------------------------------------------------------------------
    getXMLEnd()
    -----------------------------------------------------------------------
    OUTPUT: an XML string
    CALLED BY: most of the servlets for ending their XML representation
    ----------------------------------------------------------------------*/

    public String getXMLEnd() {
        String XMLEnd =
                "<footer>" +
                "<text>index.xml:FOOTER TEXT</text>" +
                "</footer>" +
                "</page>";
        return XMLEnd;
    }

    /*---------------------------------------------------------------------
                    getXMLLoginStart()
    -----------------------------------------------------------------------
    INPUT: - String xsl: the name of the XSL file to be referenced (g.e. "Login.xsl")
    OUTPUT: - String XMLLoginStart: an XML string
    CALLED BY: Index and Links servlets
    ----------------------------------------------------------------------*/
    public String getXMLLoginStart(String xsl) {
        String XMLLoginStart =ConstantParameters.xmlHeader  +
                "<?xml-stylesheet href=\"" + xsl + "\" type=\"text/xsl\"?>" +
                "<page title=\"Δημιουργός Θησαυρού\" language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\" mode=\"insert\">" +                
                "<header>" +
                "<name>Δημιουργός Θησαυρού</name>" +
                "<logo>images/thesaurusheader.jpg</logo>" +
                "</header>" +
                "<leftmenu></leftmenu>" +
                "<content>" +
                "<inputs>" +
                "<login></login>" +
                "<password></password>" +
                "</inputs>" +
                "</content>";

        XMLLoginStart += "<existingThesaurus>";
            XMLLoginStart += "<Thesaurus>-</Thesaurus>";
        XMLLoginStart += "</existingThesaurus>";

        return XMLLoginStart;
    }
    /*---------------------------------------------------------------------
    xslTransform()
    -----------------------------------------------------------------------
    INPUT: - PrintWriter out: the writer to be used
    - StringBuffer xml: the XML StringBuffer to be parsed
    - String xslFileName: the full path of the XSL file to be used for the transormation
    FUNCTION: writes to the given writer the given XML StringBuffer tranformed 
    with the given XSL file
    ----------------------------------------------------------------------*/

    public void xslTransform(PrintWriter out, StringBuffer xml, String xslFileName) {
        Utilities u = new Utilities();
        u.XmlPrintWriterTransform(out, xml, xslFileName);
        /*try {
            XMLTransform xmlD = new XMLTransform(xml.toString());
            xmlD.transform(out, xslFileName);

        } catch (DMSException ex) {
            Utils.StaticClass.handleException(ex);
        }
        out.flush();*/
    }
}