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
import Users.DBFilters;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.NodeInfoSortItemContainer;
import Utils.Parameters;
import Utils.SortItem;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import neo4j_sisapi.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.HashMap;
import neo4j_sisapi.TMSAPIClass;
/**
 *
 * @author tzortzak
 */
public class CardOf_Term extends ApplicationBasicServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        // popup display card / edit mode / XML Stream Mode
        String outputMode = request.getParameter("mode");
        
        boolean skipClose = false;
        if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){
            response.setContentType("text/xml;charset=UTF-8");       
            skipClose = true;
        }
        else{
            response.setContentType("text/html;charset=UTF-8");
        }
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        sessionInstance.readSession(session,request);
        UserInfoClass SessionUserInfoCpy = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter(); 
        try {
            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
             
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                if(outputMode==null){
                    out.println("Session Invalidate");                
                }
                else{
                    response.sendRedirect("Index");
                }
                return;
            }

            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            //export_xml xmlExp = new export_xml();
            
            //parameters
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String targetTerm = u.getDecodedParameterValue(request.getParameter("term"));
            
            String targetTermReferenceId = u.getDecodedParameterValue(request.getParameter("referenceId"));
            
            //StringObject resultMessageObj = new StringObject();
            
            
            if(targetTerm==null || targetTerm.length()==0){
                if(targetTermReferenceId!=null && targetTermReferenceId.length()>0 && SessionUserInfo.selectedThesaurus!=null && SessionUserInfo.selectedThesaurus.length()>0){
                    
                    long refId = -1;
                    try{
                        refId = Long.parseLong(targetTermReferenceId);
                    }
                    catch(Exception ex){
                        Utils.StaticClass.handleException(ex);
                    }
                    if(refId>0){
                        //open connection
                        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
                        {
                            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                            return;
                        }

                        targetTerm = dbGen.removePrefix(Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus.toUpperCase(),refId));

                        Q.free_all_sets();
                        Q.TEST_end_query();
                        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                    }
                }
            }
            
            if(targetTerm==null || targetTerm.length()==0){
                
                //dbGen.Translate(resultMessageObj, "root/CardOfTerm/NoTermSelected", null, pathToMessagesXML);
                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfTerm/NoTermSelected", null, SessionUserInfo.UILang)+"</errorMsg>";                
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode, SessionUserInfo.UILang);
                
                return;
            }
            
            
            //data storage
            HashMap<String, NodeInfoSortItemContainer> termsInfo = new HashMap<String, NodeInfoSortItemContainer>();              
            ArrayList<Long> resultNodesIdsL = new ArrayList<Long>();            
            ArrayList<String> output = new ArrayList<String>(); //all alphabetical except use and historical notes output
            StringObject targetTermObj = new StringObject();
            StringObject TopTermClassObj = new  StringObject();
            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            float elapsedTimeSec;             
            
            output.add(ConstantParameters.id_kwd);
            if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){                
                output.add(ConstantParameters.system_transliteration_kwd);
                output.add(ConstantParameters.facet_kwd);
                
                //output.add(ConstantParameters.rbt_kwd);
                //output.add(ConstantParameters.rnt_kwd);
            }
            //should be visible
            output.add(ConstantParameters.system_referenceUri_kwd);
            output.add(ConstantParameters.tc_kwd);
            output.add(ConstantParameters.translation_kwd);
            
            output.add(ConstantParameters.topterm_kwd);
            output.add(ConstantParameters.bt_kwd);
            output.add(ConstantParameters.nt_kwd);
            output.add(ConstantParameters.rt_kwd);
            output.add(ConstantParameters.uf_kwd);
            output.add(ConstantParameters.uf_translations_kwd);
            output.add(ConstantParameters.primary_found_in_kwd);
            output.add(ConstantParameters.translations_found_in_kwd);
            output.add(ConstantParameters.created_by_kwd);
            output.add(ConstantParameters.created_on_kwd);
            output.add(ConstantParameters.modified_by_kwd);
            output.add(ConstantParameters.modified_on_kwd);
            output.add(ConstantParameters.scope_note_kwd);
            output.add(ConstantParameters.translations_scope_note_kwd);
            output.add(ConstantParameters.historical_note_kwd);
            output.add(ConstantParameters.comment_kwd);
            output.add(ConstantParameters.note_kwd);
            output.add(ConstantParameters.status_kwd);

            //open connection
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            // timer begin
            long startTime = Utilities.startTimer();
            
            //data needed
            String prefix_term = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus,Q, sis_session.getValue());
            dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus,Q, sis_session.getValue(), TopTermClassObj);
            targetTermObj.setValue(prefix_term.concat(targetTerm));
            
           
            int set_Target = Q.set_get_new();
            Q.reset_set(set_Target);
            Q.reset_name_scope();
            if(Q.set_current_node(targetTermObj)==QClass.APIFail){

                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfTerm/TermNotFound", new String[]{targetTerm}, SessionUserInfo.UILang)+"</errorMsg>";
                
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode, SessionUserInfo.UILang);
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                return;
            }
            
            Q.set_put(set_Target);
            Q.reset_set(set_Target);
            
            //temp structure - Vector
            ArrayList<String> allTerms = new ArrayList<>();
            
            //"://"+request.getLocalAddr()+":" + request.getLocalPort() + "/" + Parameters.ApplicationName +"/";
            String reqUrlPrefix = u.getExternalReaderReferenceUriPrefix(request,SessionUserInfo.selectedThesaurus);
            //System.out.println("reqUrlPrefix = "+reqUrlPrefix) ;
            
            dbGen.collectTermSetInfo(SessionUserInfo, Q,TA, sis_session, set_Target, output, termsInfo, allTerms, resultNodesIdsL);
            if(output.contains((ConstantParameters.system_referenceUri_kwd))){
                 
                 dbGen.constructReferenceURIs(reqUrlPrefix, termsInfo);
            }
            boolean skipOutput = (outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0);
            u.getResultsInXmlGuideTermSorting(allTerms, termsInfo, output, xmlResults, Q, sis_session, targetLocale,SessionUserInfo,skipOutput,skipOutput);
            
            
           
            
            // in case of LIBRARY user group, mark term as (un)editable
            //boolean UserOfGroupLIBRARY = (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Library) == true);            
            boolean termIsEditable = true;
            if (SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_Library) || 
                    (Parameters.ThesTeamEditOnlyCreatedByTerms && SessionUserInfo.userGroup.equals(Utils.ConstantParameters.Group_ThesaurusTeam))
                    ) {
                // get the logical name of the current LIBRARY user (Person`xxx)
                StringObject userLogicalName = new StringObject();
                String UserName = SessionUserInfo.name;
                String Prefix_Editor = dbtr.getThesaurusPrefix_Editor(Q, sis_session.getValue());  
                userLogicalName = new StringObject(Prefix_Editor + UserName);                        
                DBFilters dbf = new DBFilters();            
                termIsEditable = dbf.TermIsEditable(SessionUserInfo, targetTermObj, userLogicalName, Q, sis_session);
            }
            if(outputMode==null || !outputMode.equals(Utils.ConstantParameters.XMLSTREAM)){
                xmlResults.append("<termName editable=\"" + termIsEditable + "\">" + Utilities.escapeXML(targetTerm) + "</termName>");
                xmlResults.append("<current>" + "<term>" + "<isTopTerm>");
                if (targetTerm != null && targetTerm.length() > 0 && dbGen.NodeBelongsToClass(new StringObject(prefix_term.concat(targetTerm)),TopTermClassObj, false, Q, sis_session)) {
                    xmlResults.append("true");
                } else {
                    xmlResults.append("false");
                }
                xmlResults.append("</isTopTerm>" + "</term>" + "</current>");
            }
            Q.free_set(set_Target);

            //close connection and query
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);            

            // timer end
            elapsedTimeSec = Utilities.stopTimer(startTime);   
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Search results in terms --> time elapsed: " + elapsedTimeSec);
            
            
           
            if(outputMode!=null && (outputMode.compareTo("edit")==0 || outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0 )){
                if(outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS,true, SessionUserInfo.UILang));
                    xml.append(xmlResults);
                    xml.append(u.getXMLEnd());
                }
                else{
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
                    xml.append(xmlResults);
                    xml.append(u.getXMLMiddle("", "Details"));
                    xml.append(u.getXMLUserInfo(SessionUserInfo));
                    xml.append(u.getXMLEnd());
                }
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"HEREEE");
                if(outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){
                    out.append(xml.toString());
                }
                else{
                    u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
                }
            } else if(outputMode==null){
                //xml.append(ConstantParameters.xmlHeader + "<page language=\""+SessionUserInfo.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">");
                xml.append(u.getXMLStart("",true, SessionUserInfo.UILang));
                xml.append(xmlResults);
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append("</page>");
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditTermActions/PopUpInfo_Term.xsl");
            }   
        
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }finally { 
            out.flush();
            if(!skipClose){
                out.close();
            }
            
            if(outputMode != null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0){
                if(SessionUserInfoCpy==null){
                    if(session!=null) {session.invalidate();}                
                }
            }
            else {                
                sessionInstance.writeBackToSession(session);
            }
        }
    } 
    
    void prepareErrorMsg(String errorMsg,PrintWriter out,SessionWrapperClass sessionInstance,String outputMode, final String uiLang){
        StringBuffer xml = new StringBuffer();
        Utilities u = new Utilities();
        xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, uiLang));
        xml.append(u.getXMLMiddle(errorMsg, "Details"));
        //resultsInfo = resultsInfo.concat("<termName>" +targetTerm+"</termName>");
        xml.append(u.getXMLEnd());
            
        if (outputMode!=null && outputMode.compareTo("edit") == 0) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
        }
        else if (outputMode!=null &&outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
            
            out.append(xml.toString());
        }
        else{
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditTermActions/PopUpInfo_Term.xsl");
        }
              
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
