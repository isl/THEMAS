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
package Admin_Thesaurus;



import DB_Classes.DBGeneral;
import Servlets.ApplicationBasicServlet;
import Utils.Utilities;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.SessionListener;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import neo4j_sisapi.*;
/**
 *
 * @author tzortzak
 */
public class Admin_Thesaurus extends ApplicationBasicServlet {
   
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
		 
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);      
        
        PrintWriter out = response.getWriter();        
        
        try {
            
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");

            
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }
            
            
            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            
            //parameters
            String CurrentShownDIV = (String) request.getParameter("DIV");   
            
            //data storage
            StringBuffer xml = new StringBuffer();
            
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {                
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet" + this.getServletName());
                return;
            }
           
            
            Vector<String> thesaurusVector = new Vector<String>();
            Vector<String> allHierarchies = new Vector<String>();
            Vector<String> allGuideTerms = new Vector<String>();
            
            // Get the existing Thesaurus in DB
            thesaurusVector = dbGen.GetExistingThesaurus(false, thesaurusVector,Q,sis_session);        
            
            //Get Hierarchies of current thesaurus
            dbGen.getDBAdminHierarchiesStatusesAndGuideTermsXML(SessionUserInfo,Q,sis_session,allHierarchies,allGuideTerms);
        
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
            
            xml.append(u.getXMLStart(ConstantParameters.LMENU_THESAURI));  
            xml.append(u.getDBAdminHierarchiesStatusesAndGuideTermsXML(allHierarchies,allGuideTerms,targetLocale));
            xml.append(getXMLMiddle(CurrentShownDIV,thesaurusVector));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
	
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
            
        } finally { 
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    } 
    
    public String getXMLMiddle(String CurrentShownDIV, Vector<String> thesaurusVector) {
        // get the active sessions
        int OtherActiveSessionsNO = SessionListener.activesessionsNO - 1;
        
        String XMLMiddleStr = "<content_Admin_Thesaurus>";
            if(CurrentShownDIV.compareTo(XMLMiddleStr)==0){
                //add a drop down of hierarchies and statuses
                
            }
            else{
                XMLMiddleStr += "<CurrentShownDIV>" + CurrentShownDIV + "</CurrentShownDIV>";
            }

            XMLMiddleStr += "<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>";
            // write the existing Thesaurus in DB
            int thesaurusVectorCount = thesaurusVector.size();
            XMLMiddleStr += "<existingThesaurus>";
            for(int i=0; i< thesaurusVectorCount; i++) {
                XMLMiddleStr += "<Thesaurus>" + thesaurusVector.get(i) + "</Thesaurus>";
            }
            XMLMiddleStr += "</existingThesaurus>";                        
        XMLMiddleStr += "</content_Admin_Thesaurus>";

        return XMLMiddleStr;
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
