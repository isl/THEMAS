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
import Servlets.ApplicationBasicServlet;
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

public class RenameInfo_Term extends ApplicationBasicServlet {
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
         
        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        PrintWriter out = response.getWriter();
        
        try{
            StringBuffer xml = new StringBuffer();

            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }

            DBGeneral dbGen = new DBGeneral();
            
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            

            Utilities u = new Utilities();
            //Hashtable params = u.getFormParams(request);
            String term_decoded    = u.getDecodedParameterValue(request.getParameter("targetTerm"));


            Boolean caseOfUndoRenameCycle = new Boolean(false);


            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }

            String tmp = term_decoded;
            String xmlResults = u.RenameXml(tmp);
            // add necessary info for the UndoRename

            xmlResults += UndoRenameXMLInfo(SessionUserInfo.selectedThesaurus, tmp,caseOfUndoRenameCycle,Q,sis_session);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS));

            xml.append(u.getXMLMiddle(/*upPartXML + */xmlResults+ "<targetTerm>"+term_decoded+"</targetTerm><targetEditField>name</targetEditField>", "Rename" ));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            //u.XmlPrintWriterTransform(out, xml, path + "/xml-xsl/rename_term.xsl");
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/EditTermActions/Edit_Term.xsl");

            
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }
        finally{
            out.close();
            sessionInstance.writeBackToSession(session);
        }

    }

    /*---------------------------------------------------------------------
    UndoRenameXMLInfo()
    -----------------------------------------------------------------------
    INPUT: - String targetTerm: the target term to be queried for the UndoRename necessary data
    (in UI format without prefix)
    OUTPUT: a String with the XML representation of the necessary data to be 
    used by UndoRename operation
    ----------------------------------------------------------------------*/
    String UndoRenameXMLInfo(String selectedThesaurus, String targetTerm,Boolean caseOfUndoRenameCycle ,QClass Q,IntegerObject sis_session) throws IOException {
        //String targetTermUTF8 = new String(targetTerm.getBytes("ISO-8859-1"), "UTF-8");
        String targetTermUTF8 = targetTerm;
        if(targetTermUTF8==null || targetTermUTF8.length()==0){
            caseOfUndoRenameCycle= false;
            String returnMSG = "<UndoRenameData>";
            returnMSG+= "<Target>";
            returnMSG += "<name></name>";
            returnMSG += "<targetTermCanBeUndoRenamed>false</targetTermCanBeUndoRenamed>";
            returnMSG += "</Target>";
            returnMSG += "</UndoRenameData>";
            return returnMSG;
        }
        // looking for Term prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q,sis_session.getValue());
        
        StringObject targetTermWithPrefix = new StringObject(termPrefix.concat(targetTermUTF8));

        Utilities u = new Utilities();
        // check if target term can be undo-renamed
        boolean targetTermCanBeUndoRenamed = DescriptorCanBeUndoRenamed(selectedThesaurus, targetTermWithPrefix,Q,sis_session);

        // if target term can be undo-renamed, collect the undo-rename chain
        Vector<String[]> UndoRenameChain = new Vector<String[]>();
        if (targetTermCanBeUndoRenamed == true) {
            UndoRenameChain = GetUndoRenameChain(selectedThesaurus, targetTermWithPrefix,caseOfUndoRenameCycle,Q,sis_session);
        }

        // write in XML the UndoRename necessary data
        String XMLstr = "";
        XMLstr += "<UndoRenameData>";
        XMLstr += "<Target>";
        XMLstr += "<name>" + Utilities.escapeXML(targetTermUTF8) + "</name>";
        XMLstr += "<targetTermCanBeUndoRenamed>" + targetTermCanBeUndoRenamed + "</targetTermCanBeUndoRenamed>";
        XMLstr += "</Target>";
        // if target term can be undo-renamed, write in XML the undo-rename chain
        if (targetTermCanBeUndoRenamed == true) {
            XMLstr += "<UndoRenameChain";
            if (caseOfUndoRenameCycle == true) {
                XMLstr += " rename_chain_kind=\"CYCLE\">";
            } else {
                XMLstr += " rename_chain_kind=\"PATH\">";
            }
            int UndoRenameChainSize = UndoRenameChain.size();
            for (int i = 0; i < UndoRenameChainSize; i++) {
                String renameCouple[] = (String[]) (UndoRenameChain.get(i));
                XMLstr += "<couple>";
                XMLstr += "<part1>" + renameCouple[0] + "</part1>";
                XMLstr += "<part2>" + renameCouple[1] + "</part2>";
                XMLstr += "</couple>";
            }
            XMLstr += "</UndoRenameChain>";
        }
        XMLstr += "</UndoRenameData>";

        return XMLstr;
    }

    /*-----------------------------------------------------------------
    DescriptorCanBeUndoRenamed()
    -------------------------------------------------------------------
    INPUT: - targetObg, the name of the detected node
    OUTPUT: - true in case the detected node can be undo renamed
    - false in case the detected node cannot be undo renamed
    FUNCTION: checks if the target node can be undo renamed.
    This happens when the target node has at least one link
    pointing from it, under category (<thes>HierarchyTerm, <thes>_gave_name_to)
    -----------------------------------------------------------------*/
    boolean DescriptorCanBeUndoRenamed(String selectedThesaurus, StringObject targetObg,QClass Q,IntegerObject sis_session) {
        int SISApiSession = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for EKTHierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for ekt_gave_name_to 
        StringObject thes_gave_name_to = new StringObject();
        dbtr.getThesaurusCategory_gave_name_to(selectedThesaurus, Q,sis_session.getValue(),thes_gave_name_to);

        Q.reset_name_scope();
        Q.set_current_node(targetObg);
        int linkSetId = Q.get_link_from_by_category(0, thesHierarchyTerm, thes_gave_name_to);
        int setCard = Q.set_get_card(linkSetId);
        Q.free_set(linkSetId);
        if (setCard > 0) {
            return true;
        } else {
            return false;
        }
    }

    /*-----------------------------------------------------------------
    GetUndoRenameChain()
    -------------------------------------------------------------------
    INPUT: - targetObg, the name of the detected node
    OUTPUT: - a Vector with the rename chain to be undone
    FUNCTION: collects in a Vector the path or cycle of nodes including 
    targetObg and being connected with "ekt_gave_name_to" links
    -----------------------------------------------------------------*/
    Vector<String[]> GetUndoRenameChain(String selectedThesaurus, StringObject targetObg,Boolean caseOfUndoRenameCycle, QClass Q,IntegerObject sis_session ) {
        int SISApiSession = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        
        
        // looking for EKTHierarchyTerm 
        StringObject thesHierarchyTerm = new StringObject();
        dbtr.getThesaurusClass_HierarchyTerm(selectedThesaurus, Q,sis_session.getValue(),thesHierarchyTerm);
        // looking for ekt_gave_name_to 
        StringObject thes_gave_name_to = new StringObject();
        dbtr.getThesaurusCategory_gave_name_to(selectedThesaurus, Q,sis_session.getValue(),thes_gave_name_to);

        // get the set of the path or cycle of nodes including
        // targetObg and being connected with "ekt_gave_name_to" links
        int nodesSet = Q.set_get_new();
        Q.reset_name_scope();
        Q.set_current_node(targetObg);
        Q.set_put(nodesSet);
        // make the CategorySet {EKTHierarchyTerm, ekt_gave_name_to}
        Q.reset_name_scope();
        //CategorySet[] categs = new CategorySet[2];
        CategorySet[] categs = new CategorySet[1];
        categs[0] = new CategorySet(thesHierarchyTerm.getValue(), thes_gave_name_to.getValue(), QClass.Traversal_Direction.BOTH_DIR);
        //categs[1] = new CategorySet("end", "end", QClass.Traversal_Direction.BOTH_DIR);
        Q.set_categories(categs);
        Q.reset_set(nodesSet);
        int GaveNameToLinksSet = Q.get_traverse_by_category(nodesSet, QClass.Traversal_Isa.NOISA);
        Q.free_set(nodesSet);
        //StringObject cls = new StringObject();
        //StringObject label = new StringObject();
        //CMValue cmv = new CMValue();
        Q.reset_set(GaveNameToLinksSet);
        int GaveNameToLinksSetCard = Q.set_get_card(GaveNameToLinksSet);
        // collect in RenameCouplesVector the existing rename couples (path or cycle)
        // ATTENTION: the rename couples are returned from DB in RANDOM order!!!
        Vector<String[]> RenameCouplesVector = new Vector<String[]>();
        
        Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
        if(Q.bulk_return_link(GaveNameToLinksSet, retVals)!=QClass.APIFail){
            for(Return_Link_Row row:retVals){
                String renameCouple[] = new String[2];
                renameCouple[0] = dbGen.removePrefix(row.get_v1_cls());
                renameCouple[1] = dbGen.removePrefix(row.get_v3_cmv().getString());
                RenameCouplesVector.add(renameCouple);
            }
        }
        /*
        while (Q.retur_link(GaveNameToLinksSet, cls, label, cmv) != QClass.APIFail) {
            String renameCouple[] = new String[2];
            renameCouple[0] = dbGen.removePrefix(cls.getValue());
            renameCouple[1] = dbGen.removePrefix(cmv.getString());
            RenameCouplesVector.add(renameCouple);
        }*/
        Q.reset_set(GaveNameToLinksSet);
        int GaveNameToLinksSet_FROM = Q.get_from_value(GaveNameToLinksSet);
        int GaveNameToLinksSet_TO = Q.get_to_value(GaveNameToLinksSet);
        Q.free_set(GaveNameToLinksSet);
        Q.set_union(GaveNameToLinksSet_FROM, GaveNameToLinksSet_TO);
        Q.free_set(GaveNameToLinksSet_TO);
        int GaveNameToLinksNodesSetCard = Q.set_get_card(GaveNameToLinksSet_FROM);
        Q.free_set(GaveNameToLinksSet_FROM);
        // check if it is a case of rename path or cycle
        // case of cycle: cardinality of links = cardinality of nodes (g.e. x1 -> x2 -> x3 -> (x1) )
        caseOfUndoRenameCycle = false;
        if (GaveNameToLinksSetCard == GaveNameToLinksNodesSetCard) {
            caseOfUndoRenameCycle = true;
        }
        // find the beginning of the rename chain
        int RenameCouplesSize = RenameCouplesVector.size();
        String FIRST_node_OfRenameChain = "";
        if (caseOfUndoRenameCycle == true) { // chain => begin from the selected target
            String target = dbGen.removePrefix(targetObg.getValue());
            FIRST_node_OfRenameChain = target;
        } else { // path => begin from the 1st node of the rename chain
            // the 1st node of the rename chain is this node which is NOT found
            // in the 2nd part of any rename couple
            // for each rename couple
            for (int i = 0; i < RenameCouplesSize; i++) {
                // check the 1st part of the couple, if it NOT found in the 2nd part of any rename couple
                String renameCouple[] = (String[]) (RenameCouplesVector.get(i));
                String currentNode = renameCouple[0];
                boolean found = false;
                for (int j = 0; j < RenameCouplesSize; j++) {
                    String renameCouple2[] = (String[]) (RenameCouplesVector.get(j));
                    String current2ndPart = renameCouple2[1];
                    if (currentNode.compareTo(current2ndPart) == 0) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    FIRST_node_OfRenameChain = currentNode;
                }
            }
        }

        // collect in a Vector the rename couples in the CORRECT order
        Vector<String[]> UndoRenameChain = new Vector<String[]>();
        // begin from the 1st node
        String current_1stPart = FIRST_node_OfRenameChain;
        for (int i = 0; i < RenameCouplesSize; i++) {
            // find the 2nd part of the current_1stPart
            String current_2ndPart = "";
            for (int j = 0; j < RenameCouplesSize; j++) {
                String renameCouple[] = (String[]) (RenameCouplesVector.get(j));
                if (current_1stPart.compareTo(renameCouple[0]) == 0) {
                    current_2ndPart = renameCouple[1];
                    break;
                }
            }
            // inform UndoRenameChain
            String renameCouple[] = new String[2];
            renameCouple[0] = current_1stPart;
            renameCouple[1] = current_2ndPart;
            UndoRenameChain.add(renameCouple);
            // inform current_1stPart with the current_2ndPart
            current_1stPart = current_2ndPart;
        }

        return UndoRenameChain;
    }

    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
