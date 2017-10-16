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

import Utils.Parameters;
import Utils.SortItem;
import Utils.SortItemComparator;
import Utils.Utilities;
import Utils.StringLocaleComparator;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class SearchResults_Terms_Hierarchical extends ApplicationBasicServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String answerType = request.getParameter("answerType");
        if (answerType != null && answerType.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
            response.setContentType("text/xml;charset=UTF-8");
            //response.setContentType("text/html;charset=UTF-8");
        } else {
            response.setContentType("text/html;charset=UTF-8");
        }

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        try {

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                //response.sendRedirect("Index");
                return;
            }

            // open SIS and TMS connection
            QClass Q = new QClass();
            TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();

            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);

            //parameters
            String targetTerm = u.getDecodedParameterValue(request.getParameter("hierarchy"));
            
            String targetTermReferenceId = u.getDecodedParameterValue(request.getParameter("referenceId"));
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
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
            String webAppSaveResults_AbsolutePathString = request.getSession().getServletContext().getRealPath("/" + webAppSaveResults_Folder);
            Path webAppSaveResults_AbsolutePath = Paths.get(webAppSaveResults_AbsolutePathString);
            String webAppSaveResults_temporary_filesAbsolutePath = webAppSaveResults_AbsolutePath.resolve(webAppSaveResults_temporary_files_Folder).toString();
            String pathToSaveScriptingAndLocale = context.getRealPath("/translations/SaveAll_Locale_And_Scripting.xml");
            String time = Utilities.GetNow();

            //Data storage
            //CMValue cmv = new CMValue();
            //StringObject cls = new StringObject();
            //StringObject label = new StringObject();       
			/*  
            ArrayList<String> topTerms = new ArrayList<String>(); //hoiw much topterms -- in howmany hierarchies targetTerm participates
            ArrayList<Integer> referencesPerHier = new ArrayList<Integer>();
            ArrayList<ArrayList<String>> allTermsOfDescriptorsTopTerms = new ArrayList<ArrayList<String>>();//for each element of topTerms hold a vector with all terms of hierarchies for sorting purpose
            ArrayList<HashMap<String, ArrayList<String>>> ntsOfDesciptorsTopTerms = new ArrayList<HashMap<String, ArrayList<String>>>();//for each topterm hold internal nt relations of each hierarchy 

            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            topTerms.addAll(dbGen.returnResults(SessionUserInfo, targetTerm, "topterm", Q, TA, sis_session));
            Collections.sort(topTerms, new StringLocaleComparator(targetLocale));

            //thesaurus data
            String prefixClass = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            StringObject BT_NTClassObj = new StringObject();
            StringObject BT_NTLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.nt_kwd, BT_NTClassObj, BT_NTLinkObj, Q, sis_session);
            Q.reset_name_scope();

            for (int i = 0; i < topTerms.size(); i++) {

                int refCounter = 0;
                ArrayList<String> newTopTermNtsVec = new ArrayList<String>();
                StringObject hierarchyObj = new StringObject(prefixClass.concat(topTerms.get(i)));
                Q.reset_name_scope();
                Q.set_current_node(hierarchyObj);
                int set_all_hier_terms = Q.get_all_instances(0);
                Q.reset_set(set_all_hier_terms);

                //find all bt/nt relations among terms of current hierarchy
                int set_bt_labels_from = Q.get_link_from_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_from);

                int set_bt_labels_to = Q.get_link_to_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_to);

                //this should preserve all bt links established between terms of the selected hierarchy
                Q.set_intersect(set_bt_labels_from, set_bt_labels_to);
                Q.reset_set(set_bt_labels_from);

                // FILTER bt links set depending on user group
                DBFilters dbf = new DBFilters();
                set_bt_labels_from = dbf.FilterBTLinksSet(SessionUserInfo, set_bt_labels_from, Q, sis_session);

                ArrayList<String> allHierTermsVec = new ArrayList<String>();
                HashMap<String, ArrayList<String>> ntsOfHierDesciptor = new HashMap<String, ArrayList<String>>();
                HashMap<String, ArrayList<String>> btsOfHierDesciptor = new HashMap<String, ArrayList<String>>();

                String topterm = dbGen.removePrefix(hierarchyObj.getValue());
                ntsOfHierDesciptor.put(topterm, newTopTermNtsVec);

                ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
                if (Q.bulk_return_link(set_bt_labels_from, retVals) != QClass.APIFail) {
                    for (Return_Link_Row row : retVals) {
                        String bt = dbGen.removePrefix(row.get_v3_cmv().getString());
                        String nt = dbGen.removePrefix(row.get_v1_cls());

                        if (allHierTermsVec.contains(bt) == false) {
                            allHierTermsVec.add(bt);
                        }
                        if (allHierTermsVec.contains(nt) == false) {
                            allHierTermsVec.add(nt);
                        }
                        ArrayList<String> btNts = ntsOfHierDesciptor.get(bt);
                        if (btNts == null) {
                            ArrayList<String> newNtsVec = new ArrayList<String>();
                            newNtsVec.add(nt);
                            ntsOfHierDesciptor.put(bt, newNtsVec);
                        } else {
                            if (btNts.contains(nt) == false) {

                                btNts.add(nt);
                                ntsOfHierDesciptor.put(bt, btNts);
                            }
                        }

                        if (ntsOfHierDesciptor.containsKey(nt) == false) {
                            ArrayList<String> newNtsVec = new ArrayList<String>();
                            ntsOfHierDesciptor.put(nt, newNtsVec);
                        }

                        ArrayList<String> ntBts = btsOfHierDesciptor.get(nt);

                        if (ntBts == null) {
                            ArrayList<String> newBtsVec = new ArrayList<String>();
                            newBtsVec.add(bt);
                            btsOfHierDesciptor.put(nt, newBtsVec);
                        } else {
                            if (ntBts.contains(bt) == false) {

                                ntBts.add(bt);
                                btsOfHierDesciptor.put(nt, ntBts);
                            }
                        }

                        if (btsOfHierDesciptor.containsKey(bt) == false) {
                            ArrayList<String> newBtsVec = new ArrayList<String>();
                            btsOfHierDesciptor.put(bt, newBtsVec);
                        }
                    }
                }
                
//                 while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {
//
//                 String bt = dbGen.removePrefix(cmv.getString());
//                 String nt = dbGen.removePrefix(cls.getValue());
//                    
//                 if (allHierTermsVec.contains(bt) == false) {
//                 allHierTermsVec.add(bt);
//                 }
//                 if (allHierTermsVec.contains(nt) == false) {
//                 allHierTermsVec.add(nt);
//                 }                    
//                 ArrayList<String> btNts = ntsOfHierDesciptor.get(bt);
//                 if (btNts == null) {
//                 ArrayList<String> newNtsVec = new ArrayList<String>();
//                 newNtsVec.add(nt);
//                 ntsOfHierDesciptor.put(bt, newNtsVec);
//                 } else {
//                 if (btNts.contains(nt) == false) {
//
//                 btNts.add(nt);
//                 ntsOfHierDesciptor.put(bt, btNts);
//                 }
//                 }
//
//                 if (ntsOfHierDesciptor.containsKey(nt) == false) {
//                 ArrayList<String> newNtsVec = new ArrayList<String>();
//                 ntsOfHierDesciptor.put(nt, newNtsVec);
//                 }
//                    
//                    
//                 ArrayList<String> ntBts = btsOfHierDesciptor.get(nt);
//                    
//                 if (ntBts == null) {
//                 ArrayList<String> newBtsVec = new ArrayList<String>();
//                 newBtsVec.add(bt);
//                 btsOfHierDesciptor.put(nt, newBtsVec);
//                 } else {
//                 if (ntBts.contains(bt) == false) {
//
//                 ntBts.add(bt);
//                 btsOfHierDesciptor.put(nt, ntBts);
//                 }
//                 }
//
//                 if (btsOfHierDesciptor.containsKey(bt) == false) {
//                 ArrayList<String> newBtsVec = new ArrayList<String>();
//                 btsOfHierDesciptor.put(bt, newBtsVec);
//                 }
//
//
//                 }
//                 

                Q.free_set(set_bt_labels_from);
                Q.free_set(set_all_hier_terms);

                Collections.sort(allHierTermsVec, new StringLocaleComparator(targetLocale));
                if (targetTerm.compareTo(topTerms.get(i)) == 0) {
                    refCounter = 1;
                } else {
                    refCounter = 0;

                    ArrayList<String> visitedTerms = new ArrayList<String>();
                    ArrayList<String> searchBTsPaths = new ArrayList<String>();
                    searchBTsPaths.addAll(btsOfHierDesciptor.get(targetTerm));
                    refCounter = searchBTsPaths.size(); //all of its bts define one path to target // no dublicates in HashMap             
                    while (true) {
                        ArrayList<String> prepareNextSearchBtsPath = new ArrayList<String>();

                        for (int m = 0; m < searchBTsPaths.size(); m++) {
                            //each one of its bts might also have one more path
                            int partialRefs = 0;
                            String newTargetTerm = searchBTsPaths.get(m);

                            ArrayList<String> checkBts = new ArrayList<String>();
                            checkBts.addAll(btsOfHierDesciptor.get(newTargetTerm));

                            if (checkBts.size() > 0) {

                                if (visitedTerms.contains(newTargetTerm) == false) {
                                    visitedTerms.add(newTargetTerm);

                                    for (int n = 0; n < checkBts.size(); n++) {

                                        String newTargetBt = checkBts.get(n);
                                        if (visitedTerms.contains(newTargetBt) == false) {
                                            partialRefs++;
                                            prepareNextSearchBtsPath.add(newTargetBt);
                                        }
                                    }

                                    if (partialRefs > 1) {
                                        refCounter += (partialRefs - 1); //for each node of loop exclude bt path already included - addded
                                    }
                                }
                            }
                            //end partial computations

                        }

                        if (prepareNextSearchBtsPath.size() == 0) {
                            break;
                        } else {
                            searchBTsPaths.clear();
                            searchBTsPaths.addAll(prepareNextSearchBtsPath);
                        }
                    }

                }

                referencesPerHier.add(refCounter);

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + targetTerm + " found " + refCounter + " in hierarchy " + topTerms.get(i));
                allTermsOfDescriptorsTopTerms.add(allHierTermsVec);
                ntsOfDesciptorsTopTerms.add(ntsOfHierDesciptor);

            }
			*/
            ArrayList<SortItem> topTerms = new ArrayList<SortItem>(); //how many topterms -- in howmany hierarchies targetTerm participates
            ArrayList<Integer> referencesPerHier = new ArrayList<Integer>();
            ArrayList<ArrayList<SortItem>> allTermsOfDescriptorsTopTerms = new ArrayList<ArrayList<SortItem>>();//for each element of topTerms hold a vector with all terms of hierarchies for sorting purpose
            ArrayList<HashMap<String, ArrayList<SortItem>>> ntsOfDesciptorsTopTerms = new ArrayList<HashMap<String, ArrayList<SortItem>>>();//for each topterm hold internal nt relations of each hierarchy 

            //open connection and start Query
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            topTerms.addAll(dbGen.returnResultsInSortItems(SessionUserInfo, targetTerm, ConstantParameters.topterm_kwd, Q, TA, sis_session));
            Collections.sort(topTerms, transliterationComparator);

            //thesaurus data
            String prefixClass = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            String prefixTerm = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            StringObject BT_NTClassObj = new StringObject();
            StringObject BT_NTLinkObj = new StringObject();
            dbGen.getKeywordPair(SessionUserInfo.selectedThesaurus, ConstantParameters.nt_kwd, BT_NTClassObj, BT_NTLinkObj, Q, sis_session);
            Q.reset_name_scope();

            for (int i = 0; i < topTerms.size(); i++) {

                int refCounter = 0;
                ArrayList<SortItem> newTopTermNtsVec = new ArrayList<SortItem>();
                StringObject hierarchyObj = new StringObject(prefixClass.concat(topTerms.get(i).getLogName()));
                Q.reset_name_scope();
                Q.set_current_node(hierarchyObj);
                int set_all_hier_terms = Q.get_all_instances(0);
                Q.reset_set(set_all_hier_terms);

                //find all bt/nt relations among terms of current hierarchy
                int set_bt_labels_from = Q.get_link_from_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_from);

                int set_bt_labels_to = Q.get_link_to_by_category(set_all_hier_terms, BT_NTClassObj, BT_NTLinkObj);
                Q.reset_set(set_bt_labels_to);

                //this should preserve all bt links established between terms of the selected hierarchy
                Q.set_intersect(set_bt_labels_from, set_bt_labels_to);
                Q.reset_set(set_bt_labels_from);

                // FILTER bt links set depending on user group
                DBFilters dbf = new DBFilters();
                set_bt_labels_from = dbf.FilterBTLinksSet(SessionUserInfo, set_bt_labels_from, Q, sis_session);

                ArrayList<SortItem> allHierTermsVec = new ArrayList<SortItem>();
                HashMap<String, ArrayList<SortItem>> ntsOfHierDesciptor = new HashMap<String, ArrayList<SortItem>>();
                HashMap<String, ArrayList<SortItem>> btsOfHierDesciptor = new HashMap<String, ArrayList<SortItem>>();

                String topterm = dbGen.removePrefix(hierarchyObj.getValue());
                ntsOfHierDesciptor.put(topterm, newTopTermNtsVec);

                ArrayList<Return_Link_Row> retVals = new ArrayList<Return_Link_Row>();
                if (Q.bulk_return_link(set_bt_labels_from, retVals) != QClass.APIFail) {
                    for (Return_Link_Row row : retVals) {
                        String btLogName = dbGen.removePrefix(row.get_v3_cmv().getString());
                        long btId = row.get_v3_cmv().getSysid();
                        long btRefId =row.get_v3_cmv().getRefid();
                        String btTransliteration = row.get_v3_cmv().getTransliterationString();
                        
                        SortItem bt = new SortItem(btLogName,btId,"",btTransliteration,btRefId);
                        
                        String ntLogName = dbGen.removePrefix(row.get_v1_cls());
                        long ntId = row.get_Neo4j_NodeId();
                        long ntRefId = row.get_v5_clsRefid();
                        String ntTransliteration = row.get_v4_clsTransliteration();

                        SortItem nt = new SortItem(ntLogName,ntId,"",ntTransliteration,ntRefId);
                        if (allHierTermsVec.contains(bt) == false) {
                            allHierTermsVec.add(bt.getACopy());
                        }
                        if (allHierTermsVec.contains(nt) == false) {
                            allHierTermsVec.add(nt.getACopy());                            
                        }
                        ArrayList<SortItem> btNts = ntsOfHierDesciptor.get(bt.getLogName());
                        if (btNts == null) {
                            ArrayList<SortItem> newNtsVec = new ArrayList<SortItem>();
                            newNtsVec.add(nt.getACopy());
                            ntsOfHierDesciptor.put(bt.getLogName(), newNtsVec);
                        } else {
                            if (btNts.contains(nt) == false) {
                                btNts.add(nt.getACopy());
                                ntsOfHierDesciptor.put(bt.getLogName(), btNts);
                            }
                        }

                        if (ntsOfHierDesciptor.containsKey(nt.getLogName()) == false) {
                            ArrayList<SortItem> newNtsVec = new ArrayList<SortItem>();
                            ntsOfHierDesciptor.put(nt.getLogName(), newNtsVec);
                        }

                        ArrayList<SortItem> ntBts = btsOfHierDesciptor.get(nt.getLogName());

                        if (ntBts == null) {
                            ArrayList<SortItem> newBtsVec = new ArrayList<SortItem>();
                            newBtsVec.add(bt);
                            btsOfHierDesciptor.put(nt.getLogName(), newBtsVec);
                        } else {
                            if (ntBts.contains(bt) == false) {

                                ntBts.add(bt);
                                btsOfHierDesciptor.put(nt.getLogName(), ntBts);
                            }
                        }

                        if (btsOfHierDesciptor.containsKey(bt.getLogName()) == false) {
                            ArrayList<SortItem> newBtsVec = new ArrayList<SortItem>();
                            btsOfHierDesciptor.put(bt.getLogName(), newBtsVec);
                        }
                    }
                }
                /*
                 while (Q.retur_link(set_bt_labels_from, cls, label, cmv) != QClass.APIFail) {

                 String bt = dbGen.removePrefix(cmv.getString());
                 String nt = dbGen.removePrefix(cls.getValue());
                    
                 if (allHierTermsVec.contains(bt) == false) {
                 allHierTermsVec.add(bt);
                 }
                 if (allHierTermsVec.contains(nt) == false) {
                 allHierTermsVec.add(nt);
                 }                    
                 ArrayList<String> btNts = ntsOfHierDesciptor.get(bt);
                 if (btNts == null) {
                 ArrayList<String> newNtsVec = new ArrayList<String>();
                 newNtsVec.add(nt);
                 ntsOfHierDesciptor.put(bt, newNtsVec);
                 } else {
                 if (btNts.contains(nt) == false) {

                 btNts.add(nt);
                 ntsOfHierDesciptor.put(bt, btNts);
                 }
                 }

                 if (ntsOfHierDesciptor.containsKey(nt) == false) {
                 ArrayList<String> newNtsVec = new ArrayList<String>();
                 ntsOfHierDesciptor.put(nt, newNtsVec);
                 }
                    
                    
                 ArrayList<String> ntBts = btsOfHierDesciptor.get(nt);
                    
                 if (ntBts == null) {
                 ArrayList<String> newBtsVec = new ArrayList<String>();
                 newBtsVec.add(bt);
                 btsOfHierDesciptor.put(nt, newBtsVec);
                 } else {
                 if (ntBts.contains(bt) == false) {

                 ntBts.add(bt);
                 btsOfHierDesciptor.put(nt, ntBts);
                 }
                 }

                 if (btsOfHierDesciptor.containsKey(bt) == false) {
                 ArrayList<String> newBtsVec = new ArrayList<String>();
                 btsOfHierDesciptor.put(bt, newBtsVec);
                 }


                 }
                 */

                Q.free_set(set_bt_labels_from);
                Q.free_set(set_all_hier_terms);

                Collections.sort(allHierTermsVec, transliterationComparator);
                if (targetTerm.compareTo(topTerms.get(i).getLogName()) == 0) {
                    refCounter = 1;
                } else {
                    refCounter = 0;

                    ArrayList<String> visitedTerms = new ArrayList<String>();
                    ArrayList<String> searchBTsPaths = new ArrayList<String>();
                    for(SortItem item : btsOfHierDesciptor.get(targetTerm)){
                        searchBTsPaths.add(item.getLogName());
                    }
                    //searchBTsPaths.addAll(btsOfHierDesciptor.get(targetTerm));
                    refCounter = searchBTsPaths.size(); //all of its bts define one path to target // no dublicates in HashMap             
                    while (true) {
                        ArrayList<String> prepareNextSearchBtsPath = new ArrayList<String>();

                        for (int m = 0; m < searchBTsPaths.size(); m++) {
                            //each one of its bts might also have one more path
                            int partialRefs = 0;
                            String newTargetTerm = searchBTsPaths.get(m);

                            ArrayList<String> checkBts = new ArrayList<String>();
                            for(SortItem item : btsOfHierDesciptor.get(newTargetTerm)){
                                checkBts.add(item.getLogName());
                            }

                            if (checkBts.size() > 0) {

                                if (visitedTerms.contains(newTargetTerm) == false) {
                                    visitedTerms.add(newTargetTerm);

                                    for (int n = 0; n < checkBts.size(); n++) {

                                        String newTargetBt = checkBts.get(n);
                                        if (visitedTerms.contains(newTargetBt) == false) {
                                            partialRefs++;
                                            prepareNextSearchBtsPath.add(newTargetBt);
                                        }
                                    }

                                    if (partialRefs > 1) {
                                        refCounter += (partialRefs - 1); //for each node of loop exclude bt path already included - addded
                                    }
                                }
                            }
                            //end partial computations

                        }

                        if (prepareNextSearchBtsPath.size() == 0) {
                            break;
                        } else {
                            searchBTsPaths.clear();
                            searchBTsPaths.addAll(prepareNextSearchBtsPath);
                        }
                    }

                }

                referencesPerHier.add(refCounter);

                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + targetTerm + " found " + refCounter + " in hierarchy " + topTerms.get(i));
                allTermsOfDescriptorsTopTerms.add(allHierTermsVec);
                ntsOfDesciptorsTopTerms.add(ntsOfHierDesciptor);

            }

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            String Save_Results_file_name = "SearchResults_Term_Hierarchical_Display_" + time;
            String XSL = webAppSaveResults_AbsolutePath.resolve("Hierarchical_Term_Display.xsl").toString();
            if (answerType != null && answerType.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0) {
                writeHierarchicalResultsInXMLFile(out, SessionUserInfo.selectedThesaurus.toUpperCase(), targetTerm, allTermsOfDescriptorsTopTerms, ntsOfDesciptorsTopTerms, topTerms, referencesPerHier, u,
                        time, "<arg1>" + Utilities.escapeXML(targetTerm) + "</arg1>", webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", pathToSaveScriptingAndLocale, targetLocale);
            } else {
                writeHierarchicalResultsInXMLFile(null, SessionUserInfo.selectedThesaurus.toUpperCase(), targetTerm, allTermsOfDescriptorsTopTerms, ntsOfDesciptorsTopTerms, topTerms, referencesPerHier, u,
                        time, "<arg1>" + Utilities.escapeXML(targetTerm) + "</arg1>", webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", pathToSaveScriptingAndLocale, targetLocale);
                u.XmlFileTransform(webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name + ".xml", XSL, webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name.concat(".html"));

                //Send HTML relative url to output and return
                out.println(webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder + "/" + Save_Results_file_name.concat(".html"));

            }

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            if(answerType!=null && answerType.compareTo(Utils.ConstantParameters.XMLSTREAM) == 0){
                if(session!=null) {session.invalidate();}
            }
            else {
                sessionInstance.writeBackToSession(session);
            }
        }
    }
	
	//Not Used - Abandoned kept only for backwards tracing
    public void writeHierarchicalResultsInXMLFile(PrintWriter outStream, String targetTerm,
            ArrayList<ArrayList<String>> allTermsOfDescriptorsTopTerms,
            ArrayList<HashMap<String, ArrayList<String>>> ntsOfDesciptorsTopTerms,
            ArrayList<String> topTerms,
            ArrayList<Integer> referencesPerHier,
            Utilities u,
            String title,
            String query,
            String fileName,
            String pathToSaveScriptingAndLocale,
            Locale targetLocale) {

        String Full_Save_Results_file_name = fileName;

        boolean streamOutput = false;
        if (outStream != null) {
            streamOutput = true;
        }
        OutputStreamWriter out = null;
        String appendVal = ConstantParameters.xmlHeader + 
                "<page language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\n"+
                "<title>" + title + "</title>"
                +"<query>" + query + "</query>";
        if (streamOutput) {
            outStream.append(appendVal);
        } else {

            try {
                //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");

                out.append(appendVal);

                out.append(//"<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">\n" +
                        //"<query>" + "Hierarchical Presentation of terms belonging to hierarchy: " + hierarchy + "</query>" +
                         "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }

        try {
            appendVal = "<results>" + "<targetTerm>" + Utilities.escapeXML(targetTerm) + "</targetTerm>";
            if (streamOutput) {
                outStream.append(appendVal);
            } else {
                out.append(appendVal);
            }

            int totalRefs = 0;

            for (int m = 0; m < topTerms.size(); m++) {

                //out.write("<hierarchy hierRefs=\""+referencesPerHier.get(m)+"\" startIndex=\""+(totalRefs+1)+ "\">");
                appendVal = "<hierarchy hierRefs=\"" + referencesPerHier.get(m) + "\" startIndex=\"" + (totalRefs + 1) + "\">";
                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }
                totalRefs += referencesPerHier.get(m);

                String hierarchy = topTerms.get(m);
                appendVal = "<topterm>" + "<name>" + Utilities.escapeXML(hierarchy) + "</name>";
                    //out.write("<topterm>");

                        //out.write("<name>");
                //out.write(Utilities.escapeXML(hierarchy));
                //out.write("</name>");
                ArrayList<String> toptermNts = ntsOfDesciptorsTopTerms.get(m).get(hierarchy);
                Collections.sort(toptermNts, new StringLocaleComparator(targetLocale));
                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }
                for (int i = 0; i < toptermNts.size(); i++) {
                    //out.write("<nt>");out.write(Utilities.escapeXML(toptermNts.get(i)));out.write("</nt>");
                    appendVal = "<nt>" + Utilities.escapeXML(toptermNts.get(i)) + "</nt>";
                    if (streamOutput) {
                        outStream.append(appendVal);
                    } else {
                        out.append(appendVal);
                    }

                }
                //out.write("</topterm>");
                if (streamOutput) {
                    outStream.append("</topterm>");
                } else {
                    out.append("</topterm>");
                }

                ntsOfDesciptorsTopTerms.get(m).remove(hierarchy);
                allTermsOfDescriptorsTopTerms.get(m).remove(hierarchy);

                int all_hier_terms_vecSize = allTermsOfDescriptorsTopTerms.get(m).size();

                for (int i = 0; i < all_hier_terms_vecSize; i++) {

                    String term = allTermsOfDescriptorsTopTerms.get(m).get(i);
                    ArrayList<String> termNts = new ArrayList<String>();
                    termNts.addAll(ntsOfDesciptorsTopTerms.get(m).get(term));

                    if (termNts.size() > 0) {
                        Collections.sort(termNts, new StringLocaleComparator(targetLocale));
                    }

                    //out.write("<term>");
                    //out.write("<name>");
                    //out.write(Utilities.escapeXML(term));
                    //out.write("</name>");
                    
                    if (streamOutput) {
                        outStream.append("<term><name>"+Utilities.escapeXML(term)+"</name>");
                    } else {
                        out.append("<term><name>"+Utilities.escapeXML(term)+"</name>");
                    }
                    

                    for (int k = 0; k < termNts.size(); k++) {
                        //out.write("<nt>");
                        //out.write(Utilities.escapeXML(termNts.get(k)));
                        //out.write("</nt>");
                        if (streamOutput) {
                            outStream.append("<nt>"+Utilities.escapeXML(termNts.get(k))+"</nt>");
                        } else {
                            out.append("<nt>"+Utilities.escapeXML(termNts.get(k))+"</nt>");
                        }
                        
                    }
                    //out.write("</term>");
                    if (streamOutput) {
                        outStream.append("</term>");
                    } else {
                        out.append("</term>");
                    }
                    
                }

                //out.write("</hierarchy>");
                if (streamOutput) {
                    outStream.append("</hierarchy>");
                } else {
                    out.append("</hierarchy>");
                }
            }

            //out.write("</results>");
            if (streamOutput) {
                outStream.append("</results></page>");
            } else {
                out.append("</results></page>");
            }
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            //out.write("</page>");
            out.close();
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

    }
	
	public void writeHierarchicalResultsInXMLFile(PrintWriter outStream, 
            String targetThesaurus,
            String targetTerm,
            ArrayList<ArrayList<SortItem>> allTermsOfDescriptorsTopTerms,
            ArrayList<HashMap<String, ArrayList<SortItem>>> ntsOfDesciptorsTopTerms,
            ArrayList<SortItem> topTerms,
            ArrayList<Integer> referencesPerHier,
            Utilities u,
            String title,
            String query,
            String fileName,
            String pathToSaveScriptingAndLocale,
            Locale targetLocale) {

            SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
        String Full_Save_Results_file_name = fileName;

        boolean streamOutput = false;
        if (outStream != null) {
            streamOutput = true;
        }
        OutputStreamWriter out = null;
        String appendVal = ConstantParameters.xmlHeader + 
                "<page thesaurus=\""+targetThesaurus+"\" language=\"" + Parameters.UILang + "\" primarylanguage=\"" + Parameters.PrimaryLang.toLowerCase() + "\">\n"+
                "<title>" + title + "</title>"
                +"<query>" + query + "</query>";
        if (streamOutput) {
            outStream.append(appendVal);
        } else {

            try {
                //OutputStream fout = new FileOutputStream(webAppSaveResults_temporary_filesAbsolutePath + "/"+ Save_Results_file_name);
                OutputStream fout = new FileOutputStream(Full_Save_Results_file_name);

                OutputStream bout = new BufferedOutputStream(fout);
                out = new OutputStreamWriter(bout, "UTF-8");

                out.append(appendVal);

                out.append(//"<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">\n" +
                        //"<query>" + "Hierarchical Presentation of terms belonging to hierarchy: " + hierarchy + "</query>" +
                         "<pathToSaveScriptingAndLocale>" + pathToSaveScriptingAndLocale + "</pathToSaveScriptingAndLocale>");

            } catch (Exception exc) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
                Utils.StaticClass.handleException(exc);
            }
        }

        try {
            appendVal = "<results>" + "<targetTerm>" + Utilities.escapeXML(targetTerm) + "</targetTerm>";
            if (streamOutput) {
                outStream.append(appendVal);
            } else {
                out.append(appendVal);
            }

            int totalRefs = 0;

            for (int m = 0; m < topTerms.size(); m++) {

                //out.write("<hierarchy hierRefs=\""+referencesPerHier.get(m)+"\" startIndex=\""+(totalRefs+1)+ "\">");
                appendVal = "<hierarchy hierRefs=\"" + referencesPerHier.get(m) + "\" startIndex=\"" + (totalRefs + 1) + "\">";
                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }
                totalRefs += referencesPerHier.get(m);

                
                SortItem hierarchy = topTerms.get(m);
                long refId =  topTerms.get(m).getThesaurusReferenceId();
                appendVal = "<topterm>" + "<name";
                if(refId>0){
                    appendVal+= " " + ConstantParameters.system_referenceIdAttribute_kwd+"=\""+refId+"\"";
                    if(Parameters.ShowReferenceURIalso){
                        appendVal+=  " " + ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(targetThesaurus, Utilities.ReferenceUriKind.TOPTERM, refId)+"\"";
                    }
                }
                if(Parameters.ShowTransliterationInAllXMLStream){
                    String transliteration = topTerms.get(m).getLogNameTransliteration();
                    if(transliteration!=null && transliteration.length()>0){
                        appendVal+= " " + ConstantParameters.system_transliteration_kwd+"=\""+transliteration+"\"";
                    } 
                }
                appendVal+=">"+ Utilities.escapeXML(hierarchy.getLogName()) + "</name>";
                    //out.write("<topterm>");

                        //out.write("<name>");
                //out.write(Utilities.escapeXML(hierarchy));
                //out.write("</name>");
                ArrayList<SortItem> toptermNts = ntsOfDesciptorsTopTerms.get(m).get(hierarchy.getLogName());
                Collections.sort(toptermNts, transliterationComparator);
                if (streamOutput) {
                    outStream.append(appendVal);
                } else {
                    out.append(appendVal);
                }
                for (int i = 0; i < toptermNts.size(); i++) {
                    
                    long ttNtrefId = toptermNts.get(i).getThesaurusReferenceId();
                    //out.write("<nt>");out.write(Utilities.escapeXML(toptermNts.get(i)));out.write("</nt>");
                    appendVal = "<nt";
                    if(ttNtrefId>0){
                        appendVal+= " " + ConstantParameters.system_referenceIdAttribute_kwd+"=\""+ttNtrefId+"\"";
                        if(Parameters.ShowReferenceURIalso){
                            appendVal+=  " " + ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(targetThesaurus, Utilities.ReferenceUriKind.TOPTERM, ttNtrefId)+"\"";
                        }
                    }
                    appendVal+=">"+ Utilities.escapeXML(toptermNts.get(i).getLogName()) + "</nt>";
                    if (streamOutput) {
                        outStream.append(appendVal);
                    } else {
                        out.append(appendVal);
                    }

                }
                //out.write("</topterm>");
                if (streamOutput) {
                    outStream.append("</topterm>");
                } else {
                    out.append("</topterm>");
                }

                ntsOfDesciptorsTopTerms.get(m).remove(hierarchy.getLogName());
                allTermsOfDescriptorsTopTerms.get(m).remove(hierarchy);

                int all_hier_terms_vecSize = allTermsOfDescriptorsTopTerms.get(m).size();

                for (int i = 0; i < all_hier_terms_vecSize; i++) {

                    SortItem term = allTermsOfDescriptorsTopTerms.get(m).get(i);
                    ArrayList<SortItem> termNts = new ArrayList<SortItem>();
                   
                    termNts.addAll(ntsOfDesciptorsTopTerms.get(m).get(term.getLogName()));

                    if (termNts.size() > 0) {
                        //if link class is to be shown the perhaps another comparator might be needed
                        // new SortItemComparator(SortItemComparator.SortItemComparatorField.LINKCLASS_TRANSLITERATION_LOGNAME)
                        Collections.sort(termNts,transliterationComparator);
                    }

                    appendVal = "<term><name";
                    long termRefId = term.getThesaurusReferenceId();
                    //out.write("<nt>");out.write(Utilities.escapeXML(toptermNts.get(i)));out.write("</nt>");
                    if(termRefId>0){
                        appendVal+= " " + ConstantParameters.system_referenceIdAttribute_kwd+"=\""+termRefId+"\"";
                        if(Parameters.ShowReferenceURIalso){
                            appendVal+=  " " + ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(targetThesaurus, Utilities.ReferenceUriKind.TERM, termRefId)+"\"";
                        }
                    }
                    appendVal+=">"+Utilities.escapeXML(term.getLogName())+"</name>";
                    
                    if (streamOutput) {
                        outStream.append(appendVal);
                    } else {
                        out.append(appendVal);
                    }
                    

                    for (int k = 0; k < termNts.size(); k++) {
                        
                        
                        long ntrefId = termNts.get(k).getThesaurusReferenceId();
                        //out.write("<nt>");out.write(Utilities.escapeXML(toptermNts.get(i)));out.write("</nt>");
                        appendVal = "<nt";
                        if(ntrefId>0){
                            appendVal+= " " + ConstantParameters.system_referenceIdAttribute_kwd+"=\""+ntrefId+"\"";
                            if(Parameters.ShowReferenceURIalso){
                                appendVal+=  " " + ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(targetThesaurus, Utilities.ReferenceUriKind.TERM, ntrefId)+"\"";
                            }
                        }
                        appendVal+=">"+Utilities.escapeXML(termNts.get(k).getLogName())+"</nt>";
                        
                        if (streamOutput) {
                            outStream.append(appendVal);
                        } else {
                            out.append(appendVal);
                        }
                        
                    }
                    //out.write("</term>");
                    if (streamOutput) {
                        outStream.append("</term>");
                    } else {
                        out.append("</term>");
                    }
                    
                }

                //out.write("</hierarchy>");
                if (streamOutput) {
                    outStream.append("</hierarchy>");
                } else {
                    out.append("</hierarchy>");
                }
            }

            //out.write("</results>");
            if (streamOutput) {
                outStream.append("</results></page>");
            } else {
                out.append("</results></page>");
            }
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in writing results " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        try {
            //out.write("</page>");
            if (!streamOutput) {
                out.close();
            }
        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in closing file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
