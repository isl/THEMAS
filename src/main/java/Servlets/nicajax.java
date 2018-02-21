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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;

public class nicajax extends ApplicationBasicServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        
        try{
                
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            
            QClass Q = new QClass();            
            IntegerObject sis_session = new IntegerObject();
            
            DBGeneral dbGen = new DBGeneral();
            Utilities u = new Utilities();

            DBThesaurusReferences dbtr = new DBThesaurusReferences();

            ArrayList<String> Search_result = new ArrayList<String>();

            String inputType = u.getDecodedParameterValue(request.getParameter("inputType"));//params.get("inputType").toString();
            String inputvalue = u.getDecodedParameterValue(request.getParameter("inputvalue"));//params.get().toString();

            if (inputvalue.length() > 1) {
                out.println(inputvalue);
            }

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            
            if(inputType.equals("inputvalue_facet")==true){ //facet suggestion
                
                if (inputvalue.length() == 1) {
                    Search_result = dbGen.Search_Facets(SessionUserInfo, inputvalue,Q,sis_session,targetLocale);
                }
                
            }else if(inputType.equals("inputvalue_hierarchy")==true) { // hierarchy suggestion
                if (inputvalue.length() == 1) {
                    Search_result = dbGen.Search_Hierarchies(SessionUserInfo, inputvalue,Q,sis_session,targetLocale);
                }
            
            }else if(inputType.equals("inputvalue_term")==true) { // term suggestion                
                if (inputvalue.length() == 1) {
                    Search_result = dbGen.Search(SessionUserInfo, inputvalue,Q,sis_session,targetLocale);
                }
            } 
            else if(inputType.equals("inputvalue_Translations")==true) { // Translation suggestion                
                if (inputvalue.length() == 1) {
                    //String EnglishWordPrefix = dbtr.getThesaurusPrefix_EnglishWord(sessionInstance, Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForTranslations(SessionUserInfo, inputvalue, Q,sis_session,targetLocale);
                }
            }
            else if(inputType.equals("inputvalue_UF_Translations")==true) { // EnglishWord suggestion
                if (inputvalue.length() == 1) {

                    //String EnglishWordPrefix = dbtr.getThesaurusPrefix_EnglishWord(sessionInstance, Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForUFTranslations(SessionUserInfo, inputvalue,Q,sis_session,targetLocale);
                }
            } else if(inputType.equals("inputvalue_UF")==true) { // UF suggestion                
                if (inputvalue.length() == 1) {
                    StringObject thesUF = new StringObject();
                    dbtr.getThesaurusClass_UsedForTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),thesUF);
                    String UFPrefix = dbtr.getThesaurusPrefix_UsedForTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, UFPrefix, inputvalue, thesUF.getValue(),Q,sis_session,targetLocale);
                }                
            } else if(inputType.equals("inputvalue_DeweyNumber")==true) { // DeweyNumber suggestion                
                if (inputvalue.length() == 1) {
                    String DeweyNumberPrefix = dbtr.getThesaurusPrefix_DeweyNumber(Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, DeweyNumberPrefix, inputvalue, "DeweyNumber",Q,sis_session,targetLocale);
                }                                
            } else if(inputType.equals("inputvalue_TaxonomicCode")==true) { // TaxonomicCode suggestion                
                if (inputvalue.length() == 1) {
                    String TaxonomicCodePrefix = dbtr.getThesaurusPrefix_TaxonomicCode(Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, TaxonomicCodePrefix, inputvalue, "TaxonomicCode",Q,sis_session,targetLocale);
                }                                
            }else if(inputType.equals("inputvalue_AlternativeTerm")==true) { // AlternativeTerm suggestion                
                if (inputvalue.length() == 1) {
                    StringObject thesALT = new StringObject();
                    dbtr.getThesaurusClass_AlternativeTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),thesALT);
                    String AlternativeTermPrefix = dbtr.getThesaurusPrefix_AlternativeTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, AlternativeTermPrefix, inputvalue, thesALT.getValue(),Q,sis_session,targetLocale);
                }                                                
            } else if(inputType.equals("inputvalue_Source")==true || inputType.equals("inputvalue_source")==true) { // Source suggestion                
                if (inputvalue.length() == 1) {
                    String SourcePrefix = dbtr.getThesaurusPrefix_Source(Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, SourcePrefix, inputvalue, "Source",Q,sis_session,targetLocale);
                }                                                
            } else if(inputType.equals("inputvalue_TopTerm")==true) { // TopTerm suggestion                
                if (inputvalue.length() == 1) {
                    StringObject thesTopTerm = new StringObject();
                    dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),thesTopTerm);
                    String TopTermPrefix = dbtr.getThesaurusPrefix_TopTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, TopTermPrefix, inputvalue, thesTopTerm.getValue(),Q,sis_session,targetLocale);
                }                                                                
            } else if(inputType.equals("inputvalue_Editor")==true) { // Editor suggestion                
                if (inputvalue.length() == 1) {
                    String thesEditor = SessionUserInfo.selectedThesaurus.concat("Editor");
                    String EditorPrefix = dbtr.getThesaurusPrefix_Editor(Q,sis_session.getValue());
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, EditorPrefix, inputvalue, thesEditor,Q,sis_session,targetLocale);
                }
            } else if(inputType.equals("inputvalue_Date")==true) { // Date suggestion                
                if (inputvalue.length() == 1) {
                    String Date = "Date";
                    String DatePrefix = "";
                    Search_result = dbGen.Search_TYPE_AHEAD_ForClass(SessionUserInfo, DatePrefix, inputvalue, Date,Q,sis_session,targetLocale);
                }                
            }            
             else if(inputType.equals("inputvalue_Status")==true) { // Status suggestion                
                if (inputvalue.length() == 1) {
                    
                    Search_result.add(Parameters.Status_For_Insertion + Utils.ConstantParameters.TypeAheadSeparator);
                    Search_result.add(Parameters.Status_Approved + Utils.ConstantParameters.TypeAheadSeparator);
                    Search_result.add(Parameters.Status_For_Approval + Utils.ConstantParameters.TypeAheadSeparator);
                    Search_result.add(Parameters.Status_Under_Construction + Utils.ConstantParameters.TypeAheadSeparator);
                }
            }  
            
            //String[] options = new String[Search_result.size()];
            //Search_result.toArray(options);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            out.println("<results>");
            for(String option : Search_result){
                out.println("<option>"+Utilities.escapeXML(option)+"</option>");
            }
            /*
            for (int i = 0; i < options.length; i++) {
                // BIG KOYLAMARA!!! (karam bug fix 16/12/2008)
                //out.println(options[i]);
                out.print(options[i]);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"AJAX----------- " + options[i]);
            }*/
            out.println("</results>");


        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally{
            out.close();
            sessionInstance.writeBackToSession(session);
        }
        
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}