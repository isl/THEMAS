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
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.ArrayList;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;


/**
 *
 * @author tzortzak
 */
public class CardOf_Source extends ApplicationBasicServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();  
        
        try {
            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            String outputMode   = request.getParameter("mode");
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                if(outputMode==null){
                    out.println("Session Invalidate");                
                }
                else{
                    response.sendRedirect("Index");
                }
                return;
            }
            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            String targetSource = u.getDecodedParameterValue(request.getParameter("source"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            
            if(targetSource==null || targetSource.length()==0){
                
                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfSource/NoSourceSelected", null)+"</errorMsg>";
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode);                
                return;
            }
            
            
            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            String output[] = {"name", ConstantParameters.source_note_kwd, ConstantParameters.primary_found_in_kwd, ConstantParameters.translations_found_in_kwd};
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            ArrayList<String> targetSources = new ArrayList<String>();
            targetSources.add(targetSource);
            /*
            //THEMASAPIClass WTA = new THEMASAPIClass(sis_session);
            String prefix_Source = dbtr.getThesaurusPrefix_Source(session, Q, sis_session.getValue());
            StringObject sourceObj = new StringObject(prefix_Source.concat(targetSource));
            */
            u.getResultsInXml_Source(SessionUserInfo, targetSources, output, Q, TA, sis_session, targetLocale, xmlResults);
            StringObject sourceNoteObj = new StringObject("");
            
            /*
            WTA.GetDescriptorComment(session, sourceObj, sourceNoteObj, new StringObject("Source"), new StringObject("source_note"));
            */
            
            
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
            
            xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
            xml.append(u.getXMLMiddle(xmlResults.toString(), "SourceDetails"));//"<source_note>" + sourceNoteObj.getValue() +"</source_note>" + "<sourceName>" + targetSource + "</sourceName>", "SourceDetails"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            
            if (outputMode == null) {
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditSourceActions/PopUpInfo_Source.xsl");
            } else if (outputMode.compareTo("edit") == 0) {
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/page_contents.xsl");
            }
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally { 
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    } 
    
    void prepareErrorMsg(String errorMsg,PrintWriter out,SessionWrapperClass sessionInstance,String outputMode){
        StringBuffer xml = new StringBuffer();
        Utilities u = new Utilities();
        xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
        xml.append(u.getXMLMiddle(errorMsg, "Details"));
        //resultsInfo = resultsInfo.concat("<termName>" +targetTerm+"</termName>");
        xml.append(u.getXMLEnd());
        if (outputMode == null) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/EditSourceActions/PopUpInfo_Source.xsl");
        } else if (outputMode.compareTo("edit") == 0) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
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
