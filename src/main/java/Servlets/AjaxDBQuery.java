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
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
                      AjaxDBQuery
-----------------------------------------------------------------------
servlet for quering DB dynamically with javascript
PARAMETERS:
    - ?DBqueryID=GET_TERMS_OF_HIERARCHY&targetHierarchy=<target-hierarchy> or
    - ?DBqueryID=GET_SYSID&Target=<target-node>&TargetKind=<DESCRIPTOR/HIERARCHY/FACET>
----------------------------------------------------------------------*/
public class AjaxDBQuery extends ApplicationBasicServlet {
    
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/            
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);       
        
        PrintWriter out = response.getWriter();
        
        try{
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();

            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            String DBqueryID = u.getDecodedParameterValue(request.getParameter("DBqueryID"));
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            // check for previous logon
            if (sessionInstance.getAttribute("SessionUser") == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                //response.sendRedirect("Index");
                return;
            }
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            if (DBqueryID.compareTo("GET_TERMS_OF_HIERARCHY") == 0) {
                String targetHierarchy = u.getDecodedParameterValue(request.getParameter("targetHierarchy"));
                GetTermsOfHierarchy(SessionUserInfo.selectedThesaurus,Q,sis_session,targetHierarchy, out,targetLocale);
            }
            if (DBqueryID.compareTo("GET_SYSID") == 0) {
                String target = u.getDecodedParameterValue(request.getParameter("Target"));
                String TargetKind = u.getDecodedParameterValue(request.getParameter("TargetKind")); // ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
                GetSysid(SessionUserInfo.selectedThesaurus,Q,sis_session,target, TargetKind, out);
            }                

            // END query session
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
		
        }catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        finally{
            out.close();
            sessionInstance.writeBackToSession(session);
        }
        
	
    }
    
    /*---------------------------------------------------------------------
                            GetTermsOfHierarchy()
    -----------------------------------------------------------------------
    INPUT: - String targetHierarchy: the Hierarchy to be queried
           - PrintWriter out: the servlet output to be written
    FUNCTION: writes to servlet output all the terms of the given Hierarchy
    ----------------------------------------------------------------------*/
    void GetTermsOfHierarchy(String selectedThesaurus,QClass Q, IntegerObject sis_session, String targetHierarchy, PrintWriter out,Locale targetLocale) {
        
        DBGeneral dbGen = new DBGeneral();
        
        
        ArrayList<String> termsVector = new ArrayList<String>();
        
        
        termsVector = dbGen.GetTermsOfHierarchy(selectedThesaurus, targetHierarchy,Q,sis_session,targetLocale);
        
        String servletOutput = "";
        int termsVectorSize = termsVector.size();
        for (int i = 0; i < termsVectorSize; i++) {
            //DO NOT ESCAPE XML THESE VALUES
            servletOutput += (String)termsVector.get(i);
            if (i != termsVectorSize - 1) {
                servletOutput += "###";
            }
        }
        out.println(servletOutput);
    }
    /*---------------------------------------------------------------------
                            GetSysid()
    -----------------------------------------------------------------------
    INPUT: - String target: the target to be queried
           - TargetKind: the kind of the node ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
           - PrintWriter out: the servlet output to be written
    FUNCTION: writes to servlet output all the sysid of the given target
    ----------------------------------------------------------------------*/
    void GetSysid(String selectedThesaurus,QClass Q, IntegerObject sis_session, String target, String TargetKind, PrintWriter out) {
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        
        String prefix = "";
        if (TargetKind.compareTo("DESCRIPTOR") == 0) {
            prefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus,Q,sis_session.getValue());
        }
        if (TargetKind.compareTo("HIERARCHY") == 0 || TargetKind.compareTo("FACET") == 0) {
            prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus,Q,sis_session.getValue());
        }        
        
        
        StringObject targetDB = new StringObject(prefix.concat(target));
        int SISsessionID = sis_session.getValue();
        Q.reset_name_scope();
        long sysidL = Q.set_current_node(targetDB);
        out.print(sysidL);
    }    

    /*---------------------------------------------------------------------
                                    doPost()
    ----------------------------------------------------------------------*/                
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	doGet(request, response);
    }
}