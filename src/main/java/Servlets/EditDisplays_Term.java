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
 * WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package Servlets;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;

import Utils.Parameters;
import Utils.Utilities;
import Utils.SortItem;
import Utils.SortItemComparator;
import Utils.StringLocaleComparator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neo4j_sisapi.*;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class EditDisplays_Term extends ApplicationBasicServlet {
   
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
            if (SessionUserInfo == null|| !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");                
                return;
            }
            
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String targetTerm = u.getDecodedParameterValue(request.getParameter("targetTerm"));
            StringBuffer xml = new StringBuffer();
            String targetField = u .getDecodedParameterValue(request.getParameter("targetField"));
            
            if(targetField==null || targetTerm==null){
                
                
                xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
                xml.append("<targetTerm>"+Utilities.escapeXML(targetTerm)+"</targetTerm>" +
                        "<targetEditField>"+targetField+"</targetEditField>" +
                        Parameters.getXmlElementForConfigAtRenameSaveOldNameAsUf()+
                        "<resultText>"+u.translateFromMessagesXML("root/EditTerm/Edit/NothingSpecified", null,SessionUserInfo.UILang)+"</resultText>");
                xml.append(u.getXMLUserInfo(SessionUserInfo));
                xml.append(u.getXMLEnd());
                u.XmlPrintWriterTransform(out,xml ,sessionInstance.path +  "/xml-xsl/EditTermActions/Edit_Term.xsl");
                return;
            }


            //open connection
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            
            
            Q.reset_name_scope();
            String results = "";
            ArrayList<String> currentValues = new ArrayList<>();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));
            if(targetField.compareTo(ConstantParameters.term_create_kwd)==0){
                currentValues.add(Parameters.UnclassifiedTermsLogicalname);
                xml.append("<current><term><bt><name>"+Utilities.escapeXML(Parameters.UnclassifiedTermsLogicalname) + "</name></bt></term></current><targetTerm></targetTerm><targetEditField>"+targetField+"</targetEditField>"+Parameters.getXmlElementForConfigAtRenameSaveOldNameAsUf());
                
            }
            else if(targetField.compareTo(ConstantParameters.guide_term_kwd)==0){
                
                //data structures
                StringLocaleComparator strCompar = new StringLocaleComparator(targetLocale);
                StringObject BTLinkObj = new StringObject();
                ArrayList<SortItem> nts = new ArrayList<SortItem>();
                ArrayList<String> existingGuideTermsVec = new ArrayList<String>(); 
                
                //collection of db data
                dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), BTLinkObj);
                nts.addAll(dbGen.getBT_NTwithGuideTerms(SessionUserInfo, Q, sis_session, targetTerm, ConstantParameters.NT_DIRECTION));
                existingGuideTermsVec.addAll(dbGen.collectGuideLinks(SessionUserInfo.selectedThesaurus, Q, sis_session));
                if(!existingGuideTermsVec.contains("")){ //provide "No guide Term choice"
                    existingGuideTermsVec.add("");
                }
                
                Collections.sort(existingGuideTermsVec, strCompar);
                
                //write results 
                xml.append(u.getBTNTWithGuideTermsResultsInXml(targetTerm, ConstantParameters.nt_kwd,BTLinkObj.getValue(), nts,existingGuideTermsVec,targetLocale)); 
                xml.append("<targetTerm>"+Utilities.escapeXML(targetTerm)+"</targetTerm><targetEditField>"+targetField+"</targetEditField>"+Parameters.getXmlElementForConfigAtRenameSaveOldNameAsUf());
            }             
            else{
                String[] output = new String[2];
                output[0] = "name";
                output[1] = targetField;
                ArrayList<String> skipCurrentValuesInXMLKeywords = new ArrayList<>();
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.bt_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.nt_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.rt_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.term_create_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.primary_found_in_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.translations_found_in_kwd);
                skipCurrentValuesInXMLKeywords.add(ConstantParameters.uf_kwd);
                if(skipCurrentValuesInXMLKeywords.contains(targetField)){
                    currentValues.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, targetField, Q, TA, sis_session));
                }
                else{
                    results = u.getTermResultsInXml(SessionUserInfo,targetTerm, output,Q,TA,sis_session,targetLocale);
                    xml.append(results);
                }
                xml.append("<targetTerm>"+Utilities.escapeXML(targetTerm)+"</targetTerm><targetEditField>"+targetField+"</targetEditField>"+Parameters.getXmlElementForConfigAtRenameSaveOldNameAsUf());
                
            }
            
            if(targetField.compareTo(ConstantParameters.delete_term_kwd)!=0){
                getAvailableValues(SessionUserInfo,currentValues, targetField,Q, sis_session,xml,targetLocale);

                if(targetField.compareTo(ConstantParameters.translation_kwd)==0 
                        || targetField.compareTo(ConstantParameters.uf_translations_kwd)==0
                        || targetField.compareTo(ConstantParameters.translations_scope_note_kwd)==0  ){
                    
                    
                    xml.append(u.getSupportedTranslationsXML(Q,TA, sis_session, SessionUserInfo.selectedThesaurus, false, ""));                    
                }
            }
            
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);


            u.XmlPrintWriterTransform(out,xml ,sessionInstance.path +  "/xml-xsl/EditTermActions/Edit_Term.xsl");
            
           
            //out.println("<html><head/><body><div class=\"popUpEditCard_Term\" ><fieldset><legend>test</legend>keimeno</fieldset></div></body></html>");
            
            
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
    
    
    private void getAvailableValues(UserInfoClass SessionUserInfo, ArrayList<String> currentValues, 
                                    String output, QClass Q, IntegerObject sis_session, StringBuffer xml, Locale targetLocale) {
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();

        if (output.matches(ConstantParameters.bt_kwd) || 
                output.matches(ConstantParameters.nt_kwd) || 
                output.matches(ConstantParameters.rt_kwd) ||
                output.matches(ConstantParameters.term_create_kwd)) {
            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            ArrayList<SortItem> termNames = dbGen.get_Node_Names_Of_Set_In_SortItems(set_terms, true, Q, sis_session);
            Q.free_set(set_terms);

            Collections.sort(termNames, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            xml.append("<availableTerms>");
            for (SortItem termName : termNames) {
                xml.append("<name selected=\""+(currentValues.contains(termName.getLogName()) ? "yes\">":"no\">"));
                xml.append(Utilities.escapeXML(termName.getLogName()));
                xml.append("</name>");
            }
            xml.append("</availableTerms>");

        }

        if (output.matches(ConstantParameters.primary_found_in_kwd) ||
                output.matches(ConstantParameters.translations_found_in_kwd)) {

            Q.reset_name_scope();
            Q.set_current_node(new StringObject(ConstantParameters.SourceClass));
            int set_sources = Q.get_all_instances(0);
            Q.reset_set(set_sources);
            ArrayList<SortItem> sourceNames = dbGen.get_Node_Names_Of_Set_In_SortItems(set_sources, true, Q, sis_session);
            Q.free_set(set_sources);
            
            for (SortItem sourceName :  sourceNames) {
                if(sourceName.log_name_transliteration==null || sourceName.log_name_transliteration.isEmpty()){
                    sourceName.log_name_transliteration = Utilities.getTransliterationString(sourceName.getLogName(), false);
                }
            }
            Collections.sort(sourceNames, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            
            xml.append("<availableSources>");
            for (SortItem sourceName :  sourceNames) {
                xml.append("<name selected=\""+(currentValues.contains(sourceName.getLogName()) ? "yes\">":"no\">"));
                xml.append(Utilities.escapeXML(sourceName.getLogName()));
                xml.append("</name>");
            }
            xml.append("</availableSources>");
        }

        if (output.matches(ConstantParameters.translation_kwd)) {
            //no option for selecting already defined values
            /*
            Q.reset_name_scope();
            StringObject toTranslationsFromClass = new StringObject();
            StringObject toTranslationsLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.translation_kwd, toTranslationsFromClass, toTranslationsLink, Q, sis_session);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            int set_translation_term_links = Q.get_link_from_by_category(set_terms, toTranslationsFromClass, toTranslationsLink);
            Q.reset_set(set_translation_term_links);


            ArrayList<SortItem> translationNames = dbGen.get_To_SortItems_Of_LinkSet(set_translation_term_links, true, true, true, Q, sis_session);

            Q.free_set(set_terms);
            Q.free_set(set_translation_term_links);


            Collections.sort(translationNames, new GuideTermSortItemComparator(targetLocale));
            xml.append("<availableTranslations>");
            for (int i = 0; i < translationNames.size(); i++) {
            SortItem currentSortItem = translationNames.get(i);
            xml.append("<name linkClass=\"" + currentSortItem.linkClass + "\">");
            xml.append(Utilities.escapeXML(currentSortItem.log_name));
            xml.append("</name>");
            }
            xml.append("</availableTranslations>");*/
        }

        if (output.matches(ConstantParameters.uf_translations_kwd)) {
            //no option for selecting already defined values
            /*
            Q.reset_name_scope();
            StringObject ufTranslationsFromClass = new StringObject();
            StringObject ufTranslationsLink = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.uf_translations_kwd, ufTranslationsFromClass, ufTranslationsLink, Q, sis_session);

            int index = Parameters.CLASS_SET.indexOf("TERM");

            String[] DescriptorClasses = new String[SessionUserInfo.CLASS_SET_INCLUDE.get(index).size()];
            SessionUserInfo.CLASS_SET_INCLUDE.get(index).toArray(DescriptorClasses);

            Q.reset_name_scope();
            int set_terms = dbGen.get_Instances_Set(DescriptorClasses, Q, sis_session);
            Q.reset_set(set_terms);
            int set_uftranslations_labels = Q.get_link_from_by_category(set_terms, ufTranslationsFromClass, ufTranslationsLink);
            Q.reset_set(set_uftranslations_labels);
            //
            //int set_uftranslations = Q.get_to_value(set_uftranslations_labels);
            //Q.reset_set(set_uftranslations);
            //
            //ArrayList<String> uftranslationsNames = dbGen.get_Node_Names_Of_Set(set_uftranslations, true, Q, sis_session);

            ArrayList<SortItem> uftranslationsNames = dbGen.get_To_SortItems_Of_LinkSet(set_uftranslations_labels, true, true, false, Q, sis_session);


            Q.free_set(set_terms);
            Q.free_set(set_uftranslations_labels);
            //Q.free_set(set_uftranslations);

            
            Collections.sort(uftranslationsNames, new GuideTermSortItemComparator(targetLocale));
            xml.append("<availableUfTranslations>");
            for (int i = 0; i < uftranslationsNames.size(); i++) {
            SortItem currentSortItem = uftranslationsNames.get(i);
            xml.append("<name linkClass=\"" + currentSortItem.linkClass + "\">");
            xml.append(Utilities.escapeXML(currentSortItem.log_name));
            xml.append("</name>");
            }
            xml.append("</availableUfTranslations>");
             * 
             */
        }

        if (output.matches(ConstantParameters.uf_kwd)) {

            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            StringObject UsedForTermClass = new StringObject();
            dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), UsedForTermClass);
            Q.reset_name_scope();

            Q.set_current_node(UsedForTermClass);
            int set_ufs = Q.get_all_instances(0);
            Q.reset_set(set_ufs);
            ArrayList<SortItem> ufNames = dbGen.get_Node_Names_Of_Set_In_SortItems(set_ufs, true, Q, sis_session);
            Q.free_set(set_ufs);
            
            for (SortItem ufName :  ufNames) {
                if(ufName.log_name_transliteration==null || ufName.log_name_transliteration.isEmpty()){
                    ufName.log_name_transliteration = Utilities.getTransliterationString(ufName.getLogName(), false);
                }
            }
            Collections.sort(ufNames, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            
            xml.append("<availableUfs>");
            for (SortItem ufName :  ufNames) {
                xml.append("<name selected=\""+(currentValues.contains(ufName.getLogName()) ? "yes\">":"no\">"));
                xml.append(Utilities.escapeXML(ufName.getLogName()));
                xml.append("</name>");
            }
            xml.append("</availableUfs>");
        }



    }


}
