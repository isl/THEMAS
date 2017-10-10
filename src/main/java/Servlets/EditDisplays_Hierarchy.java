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
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Locale;

import neo4j_sisapi.*;

/**
 *
 * @author tzortzak
 */
public class EditDisplays_Hierarchy extends ApplicationBasicServlet {
   
    
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
            
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }
            
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            Utilities u = new Utilities();
            DBGeneral  dbGen = new DBGeneral();
            StringBuffer xml = new StringBuffer();
            
            String targetHierarchy = u.getDecodedParameterValue(request.getParameter("targetHierarchy"));
            String targetField = u.getDecodedParameterValue(request.getParameter("targetField"));
            
            if(targetField==null || targetHierarchy==null){
                
                xml.append(u.getXMLStart(ConstantParameters.LMENU_HIERARCHIES));
                xml.append("<targetHierarchy>"+Utilities.escapeXML(targetHierarchy)+"</targetHierarchy>" +
                        "<targetEditField>"+targetField+"</targetEditField>" +
                        "<resultText>"+u.translateFromMessagesXML("root/EditHierarchy/Edit/NothingSpecified", null)+"</resultText>");
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append(u.getXMLEnd());
                u.XmlPrintWriterTransform(out,xml ,sessionInstance.path + "/xml-xsl/EditHierarchyActions/Edit_Hierarchy.xsl");
                return;
            }
            ArrayList<String> availableFacets = new ArrayList<String>();
            ArrayList<String> currentFacets = new ArrayList<String>();

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            if(targetField.compareTo("hierarchy_create")==0 || targetField.compareTo("hierarchy_facets")==0){
                
                availableFacets.addAll(dbGen.getAvailableFacets(SessionUserInfo.selectedThesaurus, Q, sis_session, targetLocale));
            }
            if(targetField.compareTo("hierarchy_facets")==0){
                //ArrayList<String> tempAvailableFacets = dbGen.getAvailableFacets(SessionUserInfo.selectedThesaurus, Q,sis_session,targetLocale);
                
                currentFacets.addAll(dbGen.getSelectedFacets(SessionUserInfo.selectedThesaurus,targetHierarchy, Q, sis_session, targetLocale));
                /*
                for (int i = 0; i < tempAvailableFacets.size(); i++) {
                    if (!currentFacets.contains(tempAvailableFacets.get(i))) {
                        availableFacets.add(tempAvailableFacets.get(i));
                    }
                }*/
            }
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
            if(targetField.compareTo("hierarchy_create")==0){
                
                xml.append("<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">");
                xml.append("<availableFacets>");
                for(int i=0 ; i<availableFacets.size();i++){
                    xml.append("<name selected=\""+(currentFacets.contains(availableFacets.get(i)) ? "yes\">":"no\">"));
                    xml.append(availableFacets.get(i));
                    xml.append("</name>");
                }

                xml.append("</availableFacets>");
                xml.append("<targetHierarchy>"+targetHierarchy+"</targetHierarchy>");
                xml.append("<targetEditField>"+targetField+"</targetEditField>");
                xml.append("</page>");
            }
            else if(targetField.compareTo("hierarchy_facets")==0){
                
                xml.append("<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">");
                /*
                xml.append("<currentFacets>");
                for(int i=0 ; i<currentFacets.size();i++){
                    xml.append("<name>");
                    xml.append(currentFacets.get(i));
                    xml.append("</name>");
                }

                xml.append("</currentFacets>");
                */
                xml.append("<availableFacets>");
                for(int i=0 ; i<availableFacets.size();i++){
                    xml.append("<name selected=\""+(currentFacets.contains(availableFacets.get(i)) ? "yes\">":"no\">"));
                    xml.append(availableFacets.get(i));
                    xml.append("</name>");
                }

                xml.append("</availableFacets>");
                xml.append("<targetHierarchy>"+targetHierarchy+"</targetHierarchy>");
                xml.append("<targetEditField>"+targetField+"</targetEditField>");
                xml.append("</page>");
                
            }
            else{
                xml.append("<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">");
                xml.append("<targetHierarchy>"+targetHierarchy+"</targetHierarchy>");
                xml.append("<targetEditField>"+targetField+"</targetEditField>");
                xml.append("</page>");
            }
            
            
            u.XmlPrintWriterTransform(out,xml ,sessionInstance.path +  "/xml-xsl/EditHierarchyActions/Edit_Hierarchy.xsl");
            
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
