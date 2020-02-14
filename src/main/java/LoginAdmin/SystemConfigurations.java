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

import Users.UserInfoClass;
import Utils.SessionWrapperClass;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import Servlets.ApplicationBasicServlet;

/**
 *
 * @author tzortzak
 */
public class SystemConfigurations extends ApplicationBasicServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        try {
            
            HttpSession session = request.getSession();
            SessionWrapperClass sessionInstance = new SessionWrapperClass();
            sessionInstance.readSession(session, request);
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName()) || SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Administrator)==false) {
                out.println("Session Invalidate");
                return;
            }
            if (SystemIsLockedForAdministrativeJobs(request, response)) {
                return;
            }

            
            Utilities u = new Utilities();
            String newUILanguage = u.getDecodedParameterValue(request.getParameter("Language"));
            String newListStep = u.getDecodedParameterValue(request.getParameter("ListStep"));
            String newTaxonomicalCodeFormat = u.getDecodedParameterValue(request.getParameter("TaxonomicalCodeFormat"));
            //String newmailHost = u.getDecodedParameterValue(request.getParameter("mailHost"));
            //String newmailList = u.getDecodedParameterValue(request.getParameter("mailList"));
            //String newAutomatic_Backups_Next_Day_Start_Time = u.getDecodedParameterValue(request.getParameter("Automatic_Backups_Next_Day_Start_Time"));
            //String newAutomatic_Backups_Hours_Interval = u.getDecodedParameterValue(request.getParameter("Automatic_Backups_Hours_Interval"));
            //String newAutomatic_Backups_Description = u.getDecodedParameterValue(request.getParameter("Automatic_Backups_Description"));
            
            Utils.StaticClass.webAppSystemOutPrintln("newUILanguage = " + newUILanguage);
            Utils.StaticClass.webAppSystemOutPrintln("newListStep = " + newListStep);
            Utils.StaticClass.webAppSystemOutPrintln("newTaxonomicalCodeFormat = " + newTaxonomicalCodeFormat);
            //Utils.StaticClass.webAppSystemOutPrintln("newmailHost = " + newmailHost);
            //Utils.StaticClass.webAppSystemOutPrintln("newmailList = " + newmailList);
            //Utils.StaticClass.webAppSystemOutPrintln("newAutomatic_Backups_Next_Day_Start_Time = " + newAutomatic_Backups_Next_Day_Start_Time);
            //Utils.StaticClass.webAppSystemOutPrintln("newAutomatic_Backups_Hours_Interval = " + newAutomatic_Backups_Hours_Interval);
            //Utils.StaticClass.webAppSystemOutPrintln("newAutomatic_Backups_Description = " + newAutomatic_Backups_Description);
            
            try {
                File webXMLFile = new File(this.getServletContext().getRealPath("/WEB-INF/web.xml"));
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(webXMLFile);
                XPath xpath = XPathFactory.newInstance().newXPath();

                String expression = "//web-app/context-param[./param-name='UILanguage']/param-value";
                NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newUILanguage);
                
                expression = "//web-app/context-param[./param-name='ListStep']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newListStep);

                expression = "//web-app/context-param[./param-name='TaxonomicalCodeFormat']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newTaxonomicalCodeFormat);

                /*
                expression = "//web-app/context-param[./param-name='mailHost']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newmailHost);
                
                expression = "//web-app/context-param[./param-name='mailList']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newmailList);
                
                expression = "//web-app/context-param[./param-name='Automatic_Backups_Next_Day_Start_Time']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newAutomatic_Backups_Next_Day_Start_Time);
                
                expression = "//web-app/context-param[./param-name='Automatic_Backups_Hours_Interval']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newAutomatic_Backups_Hours_Interval);
                
                expression = "//web-app/context-param[./param-name='Automatic_Backups_Description']/param-value";
                nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
                nodes.item(0).setTextContent(newAutomatic_Backups_Description);
                */
                // Write the DOM document to the file
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.transform(new DOMSource(document), new StreamResult(webXMLFile));
                
                //Runtime.getRuntime().exec("cmd /C net stop \"Apache Tomcat\" && net start \"Apache Tomcat\"");
            } catch (Exception e) {
                Utils.StaticClass.webAppSystemOutPrintln("Error in configuring web.xml: " + e.getMessage());
                Utils.StaticClass.handleException(e);
                //System.exit(1);
            }
            
        } finally { 
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
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
