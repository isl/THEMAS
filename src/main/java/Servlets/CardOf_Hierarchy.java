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
import Utils.StringLocaleComparator;
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
import java.util.Collections;
/**
 * 
 * @author tzortzak
 */
public class CardOf_Hierarchy extends ApplicationBasicServlet {
   
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
            QClass Q = new QClass();
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

            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            
            
            //parameters
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country  = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String targetHierarchy = u.getDecodedParameterValue(request.getParameter("hierarchy"));
            
            //Data storage
            StringBuffer xml = new StringBuffer();
            String output[] = {"name", "facet", ConstantParameters.system_referenceUri_kwd};
            
            if(targetHierarchy==null || targetHierarchy.length()==0){
                
                String errorMsg = "<errorMsg>"+u.translateFromMessagesXML("root/CardOfHierarchy/NoHierarchySelected", null,SessionUserInfo.UILang)+"</errorMsg>";
                prepareErrorMsg(errorMsg,out,sessionInstance,outputMode);
                return;
            }
            
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            String prefix_class = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            StringObject hierObj = new StringObject(prefix_class.concat(targetHierarchy));
        
            String reqUrlPrefix = u.getExternalReaderReferenceUriPrefix(request,SessionUserInfo.selectedThesaurus);
            String xmlResults = getHierarchyResultsInXml(SessionUserInfo, reqUrlPrefix, targetHierarchy,hierObj, output, null, Q,  sis_session, dbGen, targetLocale);

            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
          
            xml.append(u.getXMLStart(ConstantParameters.LMENU_HIERARCHIES, SessionUserInfo.UILang));
            xml.append(u.getXMLMiddle(xmlResults + "<hierarchyName>" + Utilities.escapeXML(targetHierarchy) + "</hierarchyName>", "HierarchyDetails"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());
            
            if (outputMode == null) {
                u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditHierarchyActions/PopUpInfo_Hierarchy.xsl");
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
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        xml.append(u.getXMLStart(ConstantParameters.LMENU_HIERARCHIES, SessionUserInfo.UILang));
        xml.append(u.getXMLMiddle(errorMsg, "Details"));
        //resultsInfo = resultsInfo.concat("<termName>" +targetTerm+"</termName>");
        xml.append(u.getXMLEnd());
        if (outputMode == null) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/EditHierarchyActions/PopUpInfo_Hierarchy.xsl");
        } else if (outputMode.compareTo("edit") == 0) {
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/page_contents.xsl");
        }
              
    }
    
    private String getHierarchyResultsInXml(UserInfoClass SessionUserInfo, String reqUrlPrefix, String hierarchyWithoutPrefix, StringObject hierObj, String[] output, String for_deletion, QClass Q, IntegerObject sis_session, DBGeneral dbGen, Locale targetLocale) {

        //StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);
        Utilities u = new Utilities();
        ArrayList<SortItem> v = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append("<current>");
        sb.append("<hierarchy>");

        // (delete icon was pressed from Search results TAB)
        sb.append("<for_deletion>");
        if (for_deletion != null) {
            sb.append("true");
        }
        sb.append("</for_deletion>");

        StringObject belongsToHierClass = new StringObject();
        StringObject belongsToHierarchyLink = new StringObject();
        dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.belongs_to_hier_kwd, belongsToHierClass, belongsToHierarchyLink, Q, sis_session);

        Q.reset_name_scope();
        CMValue hierCmv = new CMValue();
        Q.set_current_node_and_retrieve_Cmv(hierObj,hierCmv);
        
        
        long refId  = hierCmv.getRefid();
        if (Parameters.OnlyTopTermsHoldReferenceId) {
            //int card1 = Q.set_get_card(set_sub_classes);

            
            int set_topterms = Q.get_from_node_by_category(0, belongsToHierClass, belongsToHierarchyLink);

            //int card = Q.set_get_card(set_topterms);
            //String s1 = ""+card1+" " + card2 + " " + card3;
            ArrayList<SortItem> currCheck  = dbGen.get_Node_Names_Of_Set_In_SortItems(set_topterms, true, Q, sis_session);
            if(currCheck!=null && !currCheck.isEmpty()){
                refId = currCheck.get(0).getThesaurusReferenceId();
            }
        } 
        Q.reset_name_scope();
        
        //String referenceUri = 
        // for each value of output = {"name", "facets", "letter_code", "created", "created_by", "modified", "modified_by"}
        for (int j = 0; j < output.length; j++) {
            if (output[j].equals("name")) {
                sb.append("<name");
                if (refId > 0) {
                    sb.append(" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"").append(refId).append("\"");
                }
                sb.append(">").append(Utilities.escapeXML(hierarchyWithoutPrefix)).append("</name>");
            } else {

                if (output[j].equals("letter_code")) {
                    v.addAll(dbGen.returnResults_HierarchyInSortItems(SessionUserInfo, hierarchyWithoutPrefix, output[j], Q, sis_session, targetLocale));
                    
                    if (!v.isEmpty()) {
                        //Collections.sort(v, strCompar);
                        for (int k = 0; k < v.size(); k++) {
                            ArrayList<String> temp = new ArrayList<>();
                            //temp.addAll((Vector) v.get(k)); //BUG??
                            temp.add(v.get(k).getLogName());

                            sb.append("<" + output[j] + ">");

                            sb.append("<name>");
                            sb.append(Utilities.escapeXML(temp.get(0).toString()));
                            sb.append("</name>");
                            
                            sb.append("<editable>");
                            if(temp.size()>1){ //BUG?? this check did not exist
                                sb.append(Utilities.escapeXML(temp.get(1).toString()));
                            }
                            sb.append("</editable>");
                            

                            sb.append("</" + output[j] + ">");
                        }

                    }

                } else if(output[j].equals(ConstantParameters.system_referenceUri_kwd) && refId>0){
                    sb.append("<" + ConstantParameters.system_referenceUri_kwd + ">");
                    sb.append(Utilities.escapeXML(reqUrlPrefix+u.getExternalReaderReferenceUriSuffix(false, refId)));                    
                    sb.append("</" + ConstantParameters.system_referenceUri_kwd + ">");
                }
                else{
                    v.addAll(dbGen.returnResults_HierarchyInSortItems(SessionUserInfo, hierarchyWithoutPrefix, output[j], Q, sis_session, targetLocale));
                    if (!v.isEmpty()) {
                        sb.append("<" + output[j] + ">");
                        for (int k = 0; k < v.size(); k++) {
                            sb.append("<name");
                            if (v.get(k).getThesaurusReferenceId() > 0) {
                                sb.append(" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"").append(v.get(k).getThesaurusReferenceId()).append("\"");
                            }
                            sb.append(">").append(Utilities.escapeXML(v.get(k).getLogName())).append("</name>");
                        }
                        sb.append("</" + output[j] + ">");
                    }
                }
            }
            v.clear();
        }
        sb.append("</hierarchy>");
        sb.append("</current>");

        return sb.toString();
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
