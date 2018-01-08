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

import DB_Classes.DBGeneral;
import DB_Classes.DBStatistics;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Utilities;
import Utils.Parameters;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
                            Statistics
-----------------------------------------------------------------------
  Servlet for displaying Statistics
  Parameters:
   - "DIV" = StatisticsOfTerms_DIV/StatisticsOfHierarchies_DIV/StatisticsOfFacets_DIV/StatisticsOfSources_DIV/StatisticsOfUsers_DIV
             (DIV ids defined in Statistics_contents.xsl)
   - "Save" = yes / null (yes, in case the servlet is called by the "Save results" icon)
----------------------------------------------------------------------*/
public class Statistics extends ApplicationBasicServlet {
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
    
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
		
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);  

        PrintWriter out = response.getWriter();
        
        UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
            response.sendRedirect("Index");
            return;
        }
        
        try {
            String CurrentShownDIV = request.getParameter("DIV");// get servlet parameters    
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String Save = request.getParameter("Save");

            // query DB for the statistics of CurrentShownDIV
            DBStatistics DBS = new DBStatistics();
            String XMLStatisticResults = DBS.GetStatistics(sessionInstance, CurrentShownDIV, request, targetLocale);

            Utilities u = new Utilities();
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_STATISTICS, SessionUserInfo.UILang));  
            xml.append(getXMLMiddle(CurrentShownDIV, XMLStatisticResults));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            if (Save != null) { // in case the servlet is called by the "Save results" icon
                String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
                String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
                String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
                String webAppSaveResults_AbsolutePathString = context.getRealPath("/"+webAppSaveResults_Folder);
                Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
                String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
                
                String time = Utilities.GetNow();                
                String Save_Results_file_name = "Statistics_" + time;   
                
                String XSL = webAppSaveResults_AbsolutePath.resolve("SaveAll_Statistics.xsl").toString();                
                writeResultsInXMLFile(sessionInstance, XMLStatisticResults, u, "Statistics "+time, time, webAppSaveResults_temporary_filesAbsolutePath, Save_Results_file_name,pathToSaveScriptingAndLocale);
                //create html and answer with html link for redirection --> download
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", 
                                   XSL, 
                                   webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));                
                
                
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));
                //out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".xml"));
                out.flush();                
            }
            else {
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
            }
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
            out.close();
            sessionInstance.writeBackToSession(session);
	}
        
    }
    
    /*---------------------------------------------------------------------
                            getXMLMiddle()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLMiddleStr: an XML string with the necessary data of this servlet
    ----------------------------------------------------------------------*/
    public String getXMLMiddle(String CurrentShownDIV, String XMLStatisticResults) {        
        String XMLMiddleStr = "<content_Statistics>";
        XMLMiddleStr += "<CurrentShownDIV>" + CurrentShownDIV + "</CurrentShownDIV>";
        XMLMiddleStr += XMLStatisticResults;
        XMLMiddleStr += "</content_Statistics>";

        return XMLMiddleStr;
    }    

    /*---------------------------------------------------------------------
                                    doPost()
    ----------------------------------------------------------------------*/                
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	doGet(request, response);
    }
    
    /*---------------------------------------------------------------------
                            writeResultsInXMLFile()
    -----------------------------------------------------------------------
    FUNCTION: - writes the Statistics results in a temporary XML file in folder: Save_Results_Displays\Save_Results_temporary_files
    CALLED: in case the servlet is called by the "Save results" icon
    ----------------------------------------------------------------------*/    
    public void writeResultsInXMLFile(SessionWrapperClass sessionInstance, String XMLStatisticResults, Utilities u, String title, String windowTitle, String webAppSaveResults_temporary_filesAbsolutePath, String Save_Results_file_name,String pathToSaveScriptingAndLocale) {
        
        String Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath +File.separator+ Save_Results_file_name + ".xml";
              
        OutputStreamWriter out = null;
        try {
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);
            OutputStream bout = new BufferedOutputStream(fout);
            out = new OutputStreamWriter(bout, "UTF-8");
            String temp = ConstantParameters.xmlHeader;
            temp += "<page language=\""+SessionUserInfo.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">";
            temp += "<title>" + title + "</title>";
            temp += "<windowTitle>" + windowTitle + "</windowTitle>";
            temp += "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale +"</pathToSaveScriptingAndLocale>";
            
            temp += "<selectedThesaurus>" + SessionUserInfo.selectedThesaurus + "</selectedThesaurus>";
            out.write(temp);
            out.write("<results>");
            out.write(XMLStatisticResults);
            out.write("</results>");            
            out.write("</page>\n");
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
        try {
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }
    }    
}