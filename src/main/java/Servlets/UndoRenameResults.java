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
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
                            UndoRenameResults
-----------------------------------------------------------------------
servlet called for execution of the operation Undo Rename of a descriptor
----------------------------------------------------------------------*/
public class UndoRenameResults extends ApplicationBasicServlet {
    
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/    
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
	request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        try{
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                response.sendRedirect("Index");
                return;
            }

            
            DBGeneral dbGen = new DBGeneral();
            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();  

            //open connection and start Transaction
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            Utilities u = new Utilities();

            StringObject UndoRenameResultsMessage = new StringObject("");
            String TargetTermName = u.getDecodedParameterValue(request.getParameter("target"));


            if(UndoRenameAction(SessionUserInfo.selectedThesaurus, TargetTermName,UndoRenameResultsMessage, Q,sis_session,TA, tms_session)){
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }
            else{
                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

            
            // XML output of servlet - START
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS));	

            // write the necessary data to be used by Move to Hierarchy operation to XML
            String xmlResults = UndoRenameResultsXML(TargetTermName, UndoRenameResultsMessage);                

            // XML output of servlet - MIDDLE
            // get the previously selected tab of the UP part

            //if (currentTABup == null) currentTABup = "SearchResults";            
            // upPartXML + xmlResults : xreiazete gia tin hierarchical anaparastasi!!

            xml.append(u.getXMLMiddle(/*upPartXML + */xmlResults,  "Rename"));

            // XML output of servlet - END
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());  

            // XSL transformation of XML output of servlet


            out.println(UndoRenameResultsMessage);
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
            out.close();        
            sessionInstance.writeBackToSession(session);
        }
        
    }
    
    /*---------------------------------------------------------------------
                      UndoRenameResultsXML()
    -----------------------------------------------------------------------
    OUTPUT: a String with the XML representation of the necessary data to be 
            used after Move to Hierarchy operation
    ----------------------------------------------------------------------*/                                
    String UndoRenameResultsXML(String TargetTermName, StringObject UndoRenameResultsMessage) {	
        // write in XML the UndoRename necessary data
        String XMLstr = "";
        XMLstr += "<UndoRenameData>";
            XMLstr += "<Target>";
                XMLstr += "<name>" + TargetTermName + "</name>";
                XMLstr += "<targetTermCanBeUndoRenamed>" + true + "</targetTermCanBeUndoRenamed>";
            XMLstr += "</Target>";         
            // write the result message
            XMLstr += "<UndoRenameResultsMessage>" + UndoRenameResultsMessage.getValue() + "</UndoRenameResultsMessage>";                        
        XMLstr += "</UndoRenameData>";
        
        return XMLstr;
    }                    
    
    /*---------------------------------------------------------------------
                      UndoRenameAction()
    ----------------------------------------------------------------------*/                                
    boolean UndoRenameAction(String selectedThesaurus, String TargetTermName,StringObject UndoRenameResultsMessage, QClass Q,IntegerObject sis_session,TMSAPIClass TA, IntegerObject tms_session) {
        // begin transaction
        Q.TEST_begin_transaction();    
        
        // looking for Term prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q,sis_session.getValue());
        
        // prepare target term: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));

        int ret = TA.NOT_IMPLEMENTED_UndoRenameDescriptor(TargetTermNameUTF8WithPrefix);
        
        UndoRenameResultsMessage = new StringObject();
        if (ret == TMSAPIClass.TMS_APISucc) { // SUCCESS
            Utilities u = new Utilities();
            UndoRenameResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/Rename/UndoRenameSuccess", new String[]{TargetTermName}));
            //UndoRenameResultsMessage.setValue("Undo rename operation of term " + TargetTermName + " was successfully completed.");
            return true;
        }
        else { // FAIL
            TA.ALMOST_DONE_GetTMS_APIErrorMessage(UndoRenameResultsMessage);
            UndoRenameResultsMessage.setValue(UndoRenameResultsMessage.getValue());
            return false;
        }
        
    }        

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    
}