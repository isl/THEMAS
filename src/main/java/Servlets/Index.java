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
package Servlets;


import DB_Admin.ConfigDBadmin;
import DB_Classes.DBGeneral;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



/*---------------------------------------------------------------------
Index
-----------------------------------------------------------------------
Initial servlet with login page to THEMAS application
----------------------------------------------------------------------*/
public class Index extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);
        
        String overrideUILangParameter = request.getParameter("lang");
        
        PrintWriter out = response.getWriter();
        
        
        String basePath = request.getSession().getServletContext().getRealPath("");
        ConfigDBadmin config = new ConfigDBadmin(basePath);
        String SystemOutPrefix = getServletContext().getServletContextName()+" Logs: "; //instead of Parameters.LogFilePrefix which is not initialized before Sucessfull login
        String restoreBackupTxtFilePath = Paths.get(basePath).resolve("MonitorAutomaticBackups").resolve("RestorationNeeded.txt").toString();

        Utilities u = new Utilities();
        
        String uiLang = Parameters.UILang;
        try {
            u.CheckMaintenanceCompleted(config, restoreBackupTxtFilePath, SystemOutPrefix);
        
            String logout = request.getParameter("logout");

            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (logout != null) {
                

                    sessionInstance.setAttribute("SessionUser", null);
                    if (SessionListener.activesessionsNO > 0) {
                        try{
                            session.invalidate();
                        }catch(Exception e){
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + " Session Already Invalidated");
                            if(e.getMessage().trim().equals("java.lang.IllegalStateException: invalidate: Session already invalidate")==false){
                                Utils.StaticClass.handleException(e);
                            }
                        }
                        if (SessionUserInfo != null) {
                            uiLang = SessionUserInfo.UILang;
                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "EXIT: user " + SessionUserInfo.name + " just Logged Out.");
                            SessionUserInfo = null;
                        }
                    }
                
            } else if (SessionUserInfo != null) {
                response.setStatus(response.SC_MOVED_TEMPORARILY);
                response.setHeader("Location", "Links");
                uiLang = SessionUserInfo.UILang;
                out.close();
                return;
            } else {
                if (SessionListener.activesessionsNO > 0) {
                    try {                    
                        session.invalidate();
                    } catch (IllegalStateException ex) {
                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Tried to update an invalid session: " + ex.getMessage());
                        //Utils.StaticClass.handleException(ex);
                    }                    
                }
            }
            

            //parameters has not been initialized yet in order to check the SupportedUILangCodes value
            if(overrideUILangParameter!=null && overrideUILangParameter.trim().length()>0){
                overrideUILangParameter = overrideUILangParameter.trim().toLowerCase();
                HashMap<String,String> supportedVals = Parameters.getAvailableUICodes(request.getSession().getServletContext(),false);
                if(supportedVals.containsKey(overrideUILangParameter)){
                    uiLang = supportedVals.get(overrideUILangParameter);
                }
            }
            if(uiLang==null || uiLang.trim().length()==0){
                uiLang = getServletContext().getInitParameter("UILanguage");
            }
            DisplayLoginPage(out,sessionInstance,uiLang);
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln("Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }

    }

    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    //CODE COPIED FROM UTILITIES BECAUSE WE DO NOT WANT WERE PARAMETERS TO BE INITIALIZED
     /*---------------------------------------------------------------------
    DisplayLoginPage()
    -----------------------------------------------------------------------
    INPUT: - PrintWriter out: the writer to be used
    - String path: the "THEMASLocation" initialization parameter of the application
    CALLED BY: most of the servlets in case the user must login to the application
    ----------------------------------------------------------------------*/
    public void DisplayLoginPage(PrintWriter out, SessionWrapperClass sessionInstance, final String uiLang) {
        StringBuffer xml = new StringBuffer();

        xml.append(getXMLLoginStart(uiLang));
        xml.append(getXMLEnd());
        String xsl = "";
        if(sessionInstance!= null && sessionInstance.path.endsWith("/")){
            xsl = sessionInstance.path+"xml-xsl/Login.xsl";
        }
        else{
            if(sessionInstance!=null){
                xsl = sessionInstance.path+"/xml-xsl/Login.xsl";
            }
            else{
                Logger.getLogger(Index.class.getName()).log(Level.INFO, "**********Null session instance encoutered");
                xsl = "/xml-xsl/Login.xsl";
            }
        }
        xslTransform(out, xml, xsl);
    }
    /*---------------------------------------------------------------------
    getXMLEnd()
    -----------------------------------------------------------------------
    OUTPUT: an XML string
    CALLED BY: most of the servlets for ending their XML representation
    ----------------------------------------------------------------------*/

    public String getXMLEnd() {
        String XMLEnd ="</page>";
        return XMLEnd;
    }

    /*---------------------------------------------------------------------
    GetExistingThesaurus()
    -----------------------------------------------------------------------
    OUTPUT: a Vector with the existing Thesaurus in DB
    ----------------------------------------------------------------------*/
    public ArrayList<String> GetExistingThesaurus() {
        

        // Get the existing Thesaurus in DB
        ArrayList<String> thesaurusVector = new ArrayList<String>();
        
       // open SIS and connection
        QClass Q = new neo4j_sisapi.QClass();
        IntegerObject sis_session = new IntegerObject();
        ServletContext context = getServletContext();
        Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService());
        Q.TEST_open_connection();
        Q.TEST_begin_query();
        DBGeneral dbGen = new DBGeneral();

        
        
        thesaurusVector = dbGen.GetExistingThesaurus(false, thesaurusVector, Q, sis_session);

        //end query and close connection
        Q.free_all_sets();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session, null, false);

        return thesaurusVector;
    }

    /*---------------------------------------------------------------------
    getXMLLoginStart()
    -----------------------------------------------------------------------
    INPUT: - String xsl: the name of the XSL file to be referenced (g.e. "Login.xsl")
    OUTPUT: - String XMLLoginStart: an XML string
    CALLED BY: Index and Links servlets
    ----------------------------------------------------------------------*/
    public String getXMLLoginStart(final String uiLang) {
        //getServletContext().getInitParameter("UILanguage")
        String XMLLoginStart =
                ConstantParameters.xmlHeader  +
                "<page language=\""+uiLang+"\" mode=\"insert\">" +
                "<content>" +
                "<inputs>" +
                "<login></login>" +
                "<password></password>" +
                "</inputs>" +
                "</content>";

        HashMap<String,String> availableLangs = Parameters.getAvailableUICodes(getServletContext(),true);
        XMLLoginStart += "<availableUILangs>";
        for (Map.Entry<String, String> entry : availableLangs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            XMLLoginStart+="<langcode code=\""+value+"\">"+key+"</langcode>";
        }
        XMLLoginStart += "</availableUILangs>";
        // Get the existing Thesaurus in DB
        ArrayList<String> thesaurusVector = GetExistingThesaurus();
        int thesaurusVectorCount = thesaurusVector.size();
        XMLLoginStart += "<existingThesaurus>";
        for (int i = 0; i < thesaurusVectorCount; i++) {
            XMLLoginStart += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
        }
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
        /*
        try {
            XMLTransform xmlD = new XMLTransform(xml.toString());
            xmlD.transform(out, xslFileName);

        } catch (DMSException ex) {
            Utils.StaticClass.handleException(ex);
        }
        out.flush();
                */
    }
}