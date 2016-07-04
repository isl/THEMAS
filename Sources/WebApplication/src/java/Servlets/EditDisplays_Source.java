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


import DB_Classes.DBCreate_Modify_Source;
import DB_Classes.DBGeneral;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Utils.StringLocaleComparator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.util.Vector;
import java.util.Collections;
import java.util.Locale;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class EditDisplays_Source extends ApplicationBasicServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    public final String source_create_kwd = "source_create";
    
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
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }
            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            String targetSource = u.getDecodedParameterValue(request.getParameter("targetSource"));
            String targetField = u .getDecodedParameterValue(request.getParameter("targetField"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);                       
           
            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            
            if(targetField==null || targetSource==null){
                xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
                xml.append("<targeSource>"+Utilities.escapeXML(targetSource)+"</targeSource>" +
                        "<targetEditField>"+targetField+"</targetEditField>" +
                        "<resultText>Σφάλμα επεξεργασίας. Πρέπει να προσδιοριστεί τόσο η πηγή στόχος όσο και το πεδίο προς επεξεργασία</resultText>");
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append(u.getXMLEnd());
                u.XmlPrintWriterTransform(out,xml ,sessionInstance.path +  "/xml-xsl/EditSourceActions/Edit_Source.xsl");
                return;
            }
            
            if(targetField.compareTo(source_create_kwd)==0){
                xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
                xml.append("<targetSource></targetSource><targetEditField>"+targetField+"</targetEditField>");
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append(u.getXMLEnd());    
                u.XmlPrintWriterTransform(out,xml ,sessionInstance.path + "/xml-xsl/EditSourceActions/Edit_Source.xsl");
                return;
            }
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            if(targetField.compareTo(DBCreate_Modify_Source.source_rename_kwd)==0 || targetField.compareTo(DBCreate_Modify_Source.source_delete_kwd)==0){
                //u.getAvailableValues(session,targetField,Q, sis_session,xml);
            } 
            else if(targetField.compareTo(DBCreate_Modify_Source.source_move_references_kwd)==0){
                Vector<String> allOtherSources = new Vector<String>();
                Q.set_current_node(new StringObject(ConstantParameters.SourceClass));
                int set_allSources = Q.get_all_instances(0);
                allOtherSources.addAll(dbGen.get_Node_Names_Of_Set(set_allSources, true, Q, sis_session));
                allOtherSources.remove(targetSource);
                Q.free_set(set_allSources);
                Collections.sort(allOtherSources, new StringLocaleComparator(targetLocale));
                xmlResults.append("<results><source><name>" + Utilities.escapeXML(targetSource) + "</name>");
                for(int i =0; i< allOtherSources.size();i++){
                    xmlResults.append("<possibleValue>" + Utilities.escapeXML(allOtherSources.get(i)) + "</possibleValue>");
                }
                xmlResults.append("</source></results>");
            }
            else{
                String[] output = new String[2];
                output[0] = "name";
                output[1] = targetField;
                Vector<String> displaySources = new Vector<String>();
                displaySources.add(targetSource);
                u.getResultsInXml_Source(SessionUserInfo, displaySources, output, Q,TA, sis_session, targetLocale, xmlResults);
                
            }
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
            xml.append(u.getXMLStart(ConstantParameters.LMENU_SOURCES));
            xml.append(xmlResults.toString() + "<targetSource>"+Utilities.escapeXML(targetSource)+"</targetSource><targetEditField>"+targetField+"</targetEditField>");
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            
            u.XmlPrintWriterTransform(out,xml ,sessionInstance.path + "/xml-xsl/EditSourceActions/Edit_Source.xsl");
                
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally { 
            out.close();
            sessionInstance.writeBackToSession(session);
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
