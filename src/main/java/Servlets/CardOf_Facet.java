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
import Utils.SortItem;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import neo4j_sisapi.*;
import java.util.Vector;
import java.util.Locale;

/**
 *
 * @author tzortzak
 */
public class CardOf_Facet extends ApplicationBasicServlet {

    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String outputMode = request.getParameter("mode");
        boolean skipClose =false;
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
        init(request, response,sessionInstance);

        PrintWriter out = response.getWriter(); 
        
        try {
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
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
            
            
            //parameters
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country  = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String targetFacet = u.getDecodedParameterValue(request.getParameter("facet"));
            String targetFacetReferenceId = u.getDecodedParameterValue(request.getParameter("referenceId"));
            //String outputMode  = u .getDecodedParameterValue(request.getParameter("mode"));
            
            //Data storage
            StringBuffer xml = new StringBuffer();
            
            
            Vector<String> outputVec = new Vector<String>();
            outputVec.add("name");
            if(outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){                
                outputVec.add(ConstantParameters.system_transliteration_kwd);                
            }
            outputVec.add("hierarchy");
            
            
            String[] output = new String[outputVec.size()];
            outputVec.toArray(output);
            
            if(targetFacet==null || targetFacet.length()==0){
                if(targetFacetReferenceId!=null && targetFacetReferenceId.length()>0 && SessionUserInfo.selectedThesaurus!=null && SessionUserInfo.selectedThesaurus.length()>0){
                    
                    long refId = -1;
                    try{
                        refId = Long.parseLong(targetFacetReferenceId);
                    }
                    catch(Exception ex){
                        Utils.StaticClass.handleException(ex);
                    }
                    if(refId>0){
                        //open connection
                        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
                        {
                            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                            return;
                        }

                        targetFacet = dbGen.removePrefix(Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus.toUpperCase(),refId));

                        Q.free_all_sets();
                        Q.TEST_end_query();
                        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                    }
                }
            }
            
            if(targetFacet==null || targetFacet.length()==0){
                
                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfFacet/NoFacetSelected", null)+"</errorMsg>";
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode);
                return;
            }
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            
            Q.reset_name_scope();
            
            CMValue checkIfFacetExistsCmv = new CMValue();
            String prefix_Class= dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus,Q, sis_session.getValue());
            Q.reset_name_scope();
            
            if(Q.set_current_node_and_retrieve_Cmv(new StringObject(prefix_Class.concat(targetFacet)),checkIfFacetExistsCmv)==QClass.APIFail){

                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfFacet/FacetNotFound", new String[]{targetFacet})+"</errorMsg>";
                
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode);
                Q.free_all_sets();
                Q.TEST_end_query();
                dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                return;
            }
            Vector<SortItem> tmp = new Vector<SortItem>();            
            tmp.add(dbGen.getSortItemFromCMValue(checkIfFacetExistsCmv, true));//tmp created just in order to reuse code that finds hierarchies of a facet set
            
            boolean skipOutput = (outputMode!=null && outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0);
            
            String xmlResults = u.getResultsInXml_FacetUsingHierarchySortItems(SessionUserInfo,output, tmp,Q,sis_session,targetLocale,dbGen,skipOutput);

            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
          
            
            
            if(outputMode!=null && (outputMode.compareTo("edit")==0 || outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0 )){
                if(outputMode.compareTo(Utils.ConstantParameters.XMLSTREAM)==0){
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_FACETS,true));
                    xml.append(xmlResults);  
                    xml.append(u.getXMLEnd());
                }
                else{
                    xml.append(u.getXMLStart(ConstantParameters.LMENU_FACETS));
                    xml.append(u.getXMLMiddle(xmlResults + "<facetName>" + Utilities.escapeXML(targetFacet) + "</facetName>", "FacetDetails"));  
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
            } 
            else if (outputMode == null) {
                xml.append(u.getXMLStart(ConstantParameters.LMENU_FACETS));
                xml.append(u.getXMLMiddle(xmlResults + "<facetName>" + Utilities.escapeXML(targetFacet) + "</facetName>", "FacetDetails"));  
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append(u.getXMLEnd());
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditFacetActions/PopUpInfo_Facet.xsl");
            } 
            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        finally { 
            out.flush();
            if(!skipClose){
                out.close();
            }
            sessionInstance.writeBackToSession(session);
        }
    } 
    
    void prepareErrorMsg(String errorMsg,PrintWriter out,SessionWrapperClass sessionInstance,String outputMode){
        StringBuffer xml = new StringBuffer();
        Utilities u = new Utilities();
        xml.append(u.getXMLStart(ConstantParameters.LMENU_FACETS));
        xml.append(u.getXMLMiddle(errorMsg, "Details"));
        //resultsInfo = resultsInfo.concat("<termName>" +targetTerm+"</termName>");
        xml.append(u.getXMLEnd());
        if (outputMode == null) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditFacetActions/PopUpInfo_Facet.xsl");
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
