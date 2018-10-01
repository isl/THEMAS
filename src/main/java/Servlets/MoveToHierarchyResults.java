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

import DB_Classes.DBConnect_Term;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Servlets.ApplicationBasicServlet;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;

import Utils.ConsistensyCheck;
import Utils.ConstantParameters;
import Utils.Parameters;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
MoveToHierarchyResults
-----------------------------------------------------------------------
servlet called for execution of the operation Move To Hierarchy of a descriptor
----------------------------------------------------------------------*/
public class MoveToHierarchyResults extends ApplicationBasicServlet {

    /*---------------------------------------------------------------------
    doGet()
    ----------------------------------------------------------------------*/
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

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
                return;
            }

            DBGeneral dbGen = new DBGeneral();
            Utilities u = new Utilities();

            String action = u.getDecodedParameterValue(request.getParameter("action"));
            String TargetTermName = u.getDecodedParameterValue(request.getParameter("TargetTermName"));
            String MoveFromHierarchy = u.getDecodedParameterValue(request.getParameter("MoveFromHierarchy"));
            String MoveToHierarchy = u.getDecodedParameterValue(request.getParameter("DestinationHierList"));
            String MoveBTterm = u.getDecodedParameterValue(request.getParameter("MoveBTterm"));
            String MoveToHierarchyOption = u.getDecodedParameterValue(request.getParameter("MoveToHierarchyOption"));
            String TargetBTforDeletion = u.getDecodedParameterValue(request.getParameter("TargetBTforDeletion"));
            String pathToErrorsXML = context.getRealPath("/translations/Consistencies_Error_Codes.xml");
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            // check the action parameter (moveToHier/deleteBT) and do it
            StringObject MoveToHierarchyResultsMessage = new StringObject("");
            StringObject errorMsg = new StringObject();


            // open SIS and TMS connection
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();


            //open connection and start Transaction
            if (dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, SessionUserInfo.selectedThesaurus, false) == QClass.APIFail) {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            boolean finalResult = true;

            if (action.compareTo("moveToHier") == 0) {
                finalResult = this.moveToHierAction(SessionUserInfo, Q, TA, sis_session, tms_session, TargetTermName, MoveToHierarchyOption, MoveBTterm, MoveToHierarchyResultsMessage, errorMsg, pathToErrorsXML, MoveFromHierarchy, MoveToHierarchy);
            }//Not Move to Hier Action --> Bypass Consistency checks
            else {
                // action = deleteBT
                if (DeleteBTAction(SessionUserInfo.selectedThesaurus, Q, sis_session, TA, tms_session, dbGen, TargetTermName, TargetBTforDeletion, SessionUserInfo.name, MoveToHierarchyResultsMessage, SessionUserInfo.UILang) == false) {

                    finalResult = false;
                } else {

                    String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
                    StringObject TargetTermNameUTF8WithPrefix = new StringObject(prefix.concat(TargetTermName));
                    Q.reset_name_scope();

                    if (Q.set_current_node(TargetTermNameUTF8WithPrefix) != QClass.APIFail) {

                        int check_nodes_set = Q.set_get_new();
                        Q.set_put(check_nodes_set);
                        Q.reset_set(check_nodes_set);

                        dbGen.collect_Recurcively_ALL_NTs_Of_Set(SessionUserInfo.selectedThesaurus, check_nodes_set, check_nodes_set, true, Q, sis_session);
                        Q.reset_set(check_nodes_set);

                        if (MoveToHierBugFix(SessionUserInfo.selectedThesaurus, check_nodes_set, Q, sis_session, dbGen, errorMsg,SessionUserInfo.UILang) == false) {
                            //abort transaction and close connection
                            finalResult = false;
                        } else {

                            //commit transaction and close connection
                            finalResult = true;
                        }

                    } else {
                        //abort transaction and close connection
                        finalResult = false;
                    }
                }

            }

            if(finalResult){
                //commit transaction and close connection
                Q.free_all_sets();
                Q.TEST_end_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
                /*//out.println("Success");
                out.append("      ");//instead of Success one it appeared in the screen so return something invisible
                out.flush();
                return;*/
                //response.sendRedirect("CardOf_Term?term="+request.getParameter("TargetTermName")+"&mode=edit");
                //return;
            }
            else{

                //abort transaction and close connection
                Q.free_all_sets();
                Q.TEST_abort_transaction();
                dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            }

            // XML output of servlet - START
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS, SessionUserInfo.UILang));

            // write the necessary data to be used by Move to Hierarchy operation to XML
            String xmlResults = MoveToHierarchyResultsXML(TargetTermName, finalResult, MoveToHierarchyResultsMessage);

            // XML output of servlet - MIDDLE
            // get the previously selected tab of the UP part

            xml.append(u.getXMLMiddle(xmlResults, "Move2Hier") + "<targetTerm>"
                    + TargetTermName + "</targetTerm><targetEditField>move2HierResults</targetEditField>");

            // XML output of servlet - END
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            // XSL transformation of XML output of servlet


            //out.println(u.MoveToHierarchyResultsMessage.getValue());
            //out.flush();
            //u.XmlPrintWriterTransform(out, xml, path + "/xml-xsl/moveToHierarchy.xsl");	
            u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/EditTermActions/Edit_Term.xsl");
        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }



    }

    private boolean moveToHierAction(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
            String TargetTermName, String MoveToHierarchyOption, String MoveBTterm,
            StringObject MoveToHierarchyResultsMessage,
            StringObject errorMsg, String pathToErrorsXML,
            String MoveFromHierarchy,String MoveToHierarchy) {

        
        DBGeneral dbGen = new DBGeneral();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        ConsistensyCheck consistencyChecks = new ConsistensyCheck();

        String prefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        String user = SessionUserInfo.name;
        
        if (consistencyChecks.check_Move_To_Hier_Consistencies(SessionUserInfo, Q, sis_session, dbGen, prefix, errorMsg, pathToErrorsXML,
                TargetTermName, MoveToHierarchyOption, MoveBTterm) == false) {

            //no escape XML as it will be escaped in MoveToHierarchyResultsinXML
            MoveToHierarchyResultsMessage.setValue(errorMsg.getValue());
            return false;

        } else {
            //Bug Fix --> If move node with/without subtree node looses all its bts--> Move from all hierarchie that it belongs to target hier and bt
            if (MoveToHierarchyOption.compareTo("MOVE_NODE_ONLY") == 0 || MoveToHierarchyOption.compareTo("MOVE_NODE_AND_SUBTREE") == 0) {
                if (MoveToHierarchyAction(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, TargetTermName, MoveFromHierarchy, MoveToHierarchy, MoveBTterm, MoveToHierarchyOption, user, MoveToHierarchyResultsMessage) == false) {

                    return false;
                } else {
                    //Collect in a check_nodes_set all nodes that should be checked for - TopTerm - Classes Consistensy

                    StringObject TargetTermNameUTF8WithPrefix = new StringObject(prefix.concat(TargetTermName));
                    Q.reset_name_scope();

                    if (Q.set_current_node(TargetTermNameUTF8WithPrefix) != QClass.APIFail) {

                        int check_nodes_set = Q.set_get_new();
                        Q.set_put(check_nodes_set);
                        Q.reset_set(check_nodes_set);

                        if (MoveToHierarchyOption.compareTo("MOVE_NODE_AND_SUBTREE") == 0) {
                            dbGen.collect_Recurcively_ALL_NTs_Of_Set(SessionUserInfo.selectedThesaurus, check_nodes_set, check_nodes_set, true, Q, sis_session);
                            Q.reset_set(check_nodes_set);
                        }

                        if (MoveToHierBugFix(SessionUserInfo.selectedThesaurus, check_nodes_set, Q, sis_session, dbGen, errorMsg,SessionUserInfo.UILang) == false) {
                            //abort transaction and close connection
                            return false;
                        } else {
                            //commit transaction and close connection
                            return true;
                        }

                    } else //abort transaction and close connection
                    {
                        Q.free_all_sets();
                    }
                    return false;
                }
            } else { // CONNECT_NODE_AND_SUBTREE

                if (MoveToHierarchyAction(SessionUserInfo, Q, TA, sis_session, tms_session, dbGen, TargetTermName, MoveFromHierarchy, MoveToHierarchy, MoveBTterm, MoveToHierarchyOption, user, MoveToHierarchyResultsMessage) == false) {
                    return false;
                } else {
                    return true;
                }
            }
        }        
    }
    /*---------------------------------------------------------------------
    MoveToHierarchyResultsXML()
    -----------------------------------------------------------------------
    OUTPUT: a String with the XML representation of the necessary data to be 
    used after Move to Hierarchy operation
    ----------------------------------------------------------------------*/

    String MoveToHierarchyResultsXML(String TargetTermName,boolean suceess, StringObject MoveToHierarchyResultsMessage) {
        Utilities u = new Utilities();
        String XMLstr = "";
        XMLstr += "<MoveToHierarchyData>";
        // write the basic info of the target
        XMLstr += "<Target>";
        XMLstr += "<name>" + Utilities.escapeXML(TargetTermName) + "</name>";
        XMLstr += "<targetTermCanBeMovedToHierarchy>" + "true" + "</targetTermCanBeMovedToHierarchy>";
        XMLstr += "<reasonTargetTermCannotBeMovedToHierarchy>" + "" + "</reasonTargetTermCannotBeMovedToHierarchy>";
        XMLstr += "</Target>";
        // write the result message
        XMLstr += "<MoveToHierarchyResultsMessage>" + Utilities.escapeXML(MoveToHierarchyResultsMessage.getValue()) + "</MoveToHierarchyResultsMessage>";
        if(suceess){
            XMLstr += "<succeded>" + suceess + "</succeded>";
        }
        XMLstr += "</MoveToHierarchyData>";
        
        return XMLstr;
    }

    /*---------------------------------------------------------------------
    MoveToHierarchyAction()
    ----------------------------------------------------------------------*/
    boolean MoveToHierarchyAction(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session, DBGeneral dbGen, String TargetTermName, String MoveFromHierarchy, String MoveToHierarchy, String MoveBTterm, String MoveToHierarchyOption, String user, StringObject MoveToHierarchyResultsMessage) {

        Utilities u = new Utilities();
        DBConnect_Term dbCon = new DBConnect_Term();
        // looking for Term prefix
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
        // looking for Class prefix
        String classPrefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());

        // prepare input parameters: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));
        StringObject MoveFromHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(MoveFromHierarchy));
        StringObject MoveToHierarchyUTF8WithPrefix = new StringObject(classPrefix.concat(MoveToHierarchy));
        StringObject MoveBTtermUTF8WithPrefix = new StringObject(termPrefix.concat(MoveBTterm));
        int MTHoption = 0;
        //Move Node actions modify relations of some terms. Thus modified by and on fileds must be updated. 
        //In a later version descriptor status should also be updated 

        ArrayList<String>  otherModifiedNodes = new ArrayList<String> ();
        if (MoveToHierarchyOption.compareTo("MOVE_NODE_ONLY") == 0) {
            MTHoption = TMSAPIClass.MOVE_NODE_ONLY;
            //if successfull must update modify fields of source's direct bts and nts. Also update target 
            otherModifiedNodes.addAll(getMoveNodeOnlyModifiedTerms(SessionUserInfo, Q, TA, sis_session, dbGen, TargetTermName, MoveBTterm));
        } else if (MoveToHierarchyOption.compareTo("MOVE_NODE_AND_SUBTREE") == 0) {
            MTHoption = TMSAPIClass.MOVE_NODE_AND_SUBTREE;
            //if successfull must update modify fields of source and its direct bts. Also update target
            otherModifiedNodes.addAll(getMoveNodeAndSubtreeModifiedTerms(SessionUserInfo, Q, TA, sis_session, dbGen, TargetTermName, MoveBTterm));
        } else {
            MTHoption = TMSAPIClass.CONNECT_NODE_AND_SUBTREE;
            //if successfull must update modify fields of source target
            otherModifiedNodes.addAll(getConnectNodeModifiedTerms(MoveBTterm));
        }

        int ret = TA.CHECK_MoveToHierarchy(TargetTermNameUTF8WithPrefix, MoveFromHierarchyUTF8WithPrefix,
                MoveToHierarchyUTF8WithPrefix, MoveBTtermUTF8WithPrefix, MTHoption);

        if (ret == TMSAPIClass.TMS_APISucc) { // SUCCESS
            Q.free_all_sets();
            Q.TEST_end_transaction();
            Q.TEST_begin_transaction();
            dbCon.CreateModify_Finalization(SessionUserInfo.selectedThesaurus, TargetTermNameUTF8WithPrefix, user, otherModifiedNodes,
                    dbCon.DB_MODIFY, Q, sis_session, TA, tms_session, dbGen, MoveToHierarchyResultsMessage);

            Q.free_all_sets();
            if (MoveToHierarchyResultsMessage.getValue().compareTo("") == 0) {
                MoveToHierarchyResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/SuccessMsg", new String[]{TargetTermName},SessionUserInfo.UILang));
                //MoveToHierarchyResultsMessage.setValue("Movement of term \"" + TargetTermName + "\" was successfully performed.");
                return true;
            } else {
                //MoveToHierarchyResultsMessage.setValue(errorMsg);
                return false;
            }
        } else { // FAIL
            TA.ALMOST_DONE_GetTMS_APIErrorMessage( MoveToHierarchyResultsMessage);
            MoveToHierarchyResultsMessage.setValue(MoveToHierarchyResultsMessage.getValue());
            Q.free_all_sets();
            return false;

        }

    }

    /*---------------------------------------------------------------------
    DeleteBTAction()
    ----------------------------------------------------------------------*/
    boolean DeleteBTAction(String selectedThesaurus, QClass Q, IntegerObject sis_session, TMSAPIClass TA, IntegerObject tms_session, DBGeneral dbGen, String TargetTermName, String TargetBTforDeletion, String user, StringObject MoveToHierarchyResultsMessage, final String uiLang) {

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBConnect_Term dbCon = new DBConnect_Term();
        // 1. get the BT links of TargetTermName (see DBGeneral::getBT_NT()) - BT_links_set
        // looking for Term prefix
        String termPrefix = dbtr.getThesaurusPrefix_Descriptor(selectedThesaurus, Q, sis_session.getValue());
        // looking for EKTDescriptor
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(selectedThesaurus, Q, sis_session.getValue(), thesDescriptor);
        // looking for EKT_BT
        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(selectedThesaurus, Q, sis_session.getValue(), thesBT);
        // prepare TargetTermName and TargetBTforDeletion: add prefix and convert to DB encoding
        StringObject TargetTermNameUTF8WithPrefix = new StringObject(termPrefix.concat(TargetTermName));
        StringObject TargetBTforDeletionUTF8WithPrefix = new StringObject(termPrefix.concat(TargetBTforDeletion));
        Q.reset_name_scope();
        long TargetTermSysidL = Q.set_current_node(TargetTermNameUTF8WithPrefix);
        int BT_links_set = Q.get_link_from_by_category(0, thesDescriptor, thesBT);

        // 2. parse set1 and get the sysid of the BT-link with to-value = TargetBTforDeletionUTF8WithPrefix - BTlink_sysid
        Q.reset_set(BT_links_set);
        
        /*
        StringObject node_name = new StringObject();
        IntegerObject BTlink_sysid = new IntegerObject();
        CMValue cmv = new CMValue();
        while (Q.retur_link_id(sis_session.getValue(), BT_links_set, node_name, new IntegerObject(), BTlink_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
            String BTlink_toValue = cmv.getString();
            if (BTlink_toValue.compareTo(TargetBTforDeletionUTF8WithPrefix.getValue()) == 0) {
                break;
            }
        }
        */
        
        long BTlink_sysidL = QClass.APIFail;
        ArrayList<Return_Link_Id_Row> retVals = new ArrayList<Return_Link_Id_Row>();
        if(Q.bulk_return_link_id(BT_links_set, retVals)!=QClass.APIFail){
            //while (Q.retur_link_id(BT_links_set, node_name, new IntegerObject(), BTlink_sysid, cmv, new IntegerObject()) != QClass.APIFail) {
            for(Return_Link_Id_Row row: retVals){
                String BTlink_toValue = row.get_v4_cmv().getString();
                if (BTlink_toValue.compareTo(TargetBTforDeletionUTF8WithPrefix.getValue()) == 0) {
                    BTlink_sysidL= row.get_v3_sysid();
                    break;
                }
            }
        }
        
        Q.free_set(BT_links_set);
        Utilities u = new Utilities();

        // 3. check if BTlink_sysid is Named or Unnamed link (see DBGeneral::isNamedLink()) 
        // and call Delete_Named_Attribute() or Delete_Unnamed_Attribute()
        int ret;
        Identifier BTlinkID = new Identifier(BTlink_sysidL);
        // named link
        //if (dbGen.isNamedLink(BTlink_sysidL) == true) {
        if(Q.CHECK_isUnNamedLink(BTlink_sysidL)==false){
            Identifier TargetTermID = new Identifier(TargetTermSysidL);
            ret = Q.CHECK_Delete_Named_Attribute(BTlinkID, TargetTermID);
        } // unnamed link
        else {
            ret = Q.CHECK_Delete_Unnamed_Attribute(BTlinkID);
        }
        //find out which nodes must be updated--> same as connect node --> source and target
        ArrayList<String> otherModifiedNodes = new ArrayList<String>();
        otherModifiedNodes.addAll(getRemoveBTModifiedTerms(TargetBTforDeletion));
        if (ret == QClass.APISucc) { // SUCCESS
            Q.free_all_sets();
            Q.TEST_end_transaction();
            Q.TEST_begin_transaction();
            dbCon.CreateModify_Finalization(selectedThesaurus, TargetTermNameUTF8WithPrefix, user, otherModifiedNodes, dbCon.DB_MODIFY, Q, sis_session, TA, tms_session, dbGen, MoveToHierarchyResultsMessage);

            Q.free_all_sets();
            if (MoveToHierarchyResultsMessage.getValue().compareTo("") == 0) {
                MoveToHierarchyResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/SpecificDeleteBtSuccess", new String[]{TargetBTforDeletion,TargetTermName}, uiLang));
                //MoveToHierarchyResultsMessage.setValue("Deletion of BT: \"" + TargetBTforDeletion + "\" from term: \"" + TargetTermName + "\" was successfully completed.");

                return true;
            } else {
                //MoveToHierarchyResultsMessage.setValue(modification.errorMsg);

                return false;
            }
        } else {
            MoveToHierarchyResultsMessage.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/SpecificDeleteBtFailure", new String[]{TargetBTforDeletion,TargetTermName}, uiLang));
            //MoveToHierarchyResultsMessage.setValue("Deletion of BT: \"" + TargetBTforDeletion + "\" from term: \"" + TargetTermName + "\" failed.");
            Q.free_all_sets();

            return false;
        }


    }

    ArrayList<String> getMoveNodeOnlyModifiedTerms(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen, String TargetTermName, String MoveBTterm) {
        ArrayList<String> otherModifiedNodes = new ArrayList<String>();

        //if successfull must update modify fields of source's direct bts and nts. Also update target 
        //source will be updated by CreateModify_Finalization called in DB_MODIFY mode
        ArrayList<String> sourceBts = new ArrayList<String>();
        sourceBts = dbGen.returnResults(SessionUserInfo, TargetTermName, ConstantParameters.bt_kwd, Q, TA, sis_session);
        sourceBts.trimToSize();

        ArrayList<String> sourceNts = new ArrayList<String>();
        sourceNts = dbGen.returnResults(SessionUserInfo, TargetTermName, ConstantParameters.nt_kwd, Q, TA, sis_session);
        sourceNts.trimToSize();

        otherModifiedNodes.add(MoveBTterm.trim());// += MoveBTterm.trim() + ",";

        for (int m = 0; m < sourceNts.size(); m++) {
            if(otherModifiedNodes.contains(sourceNts.get(m))==false){
                otherModifiedNodes.add(sourceNts.get(m));
            }
            //otherModifiedNodes += sourceNts.get(m) + ",";
        }
        for (int m = 0; m < sourceBts.size(); m++) {
            if(otherModifiedNodes.contains(sourceBts.get(m))==false){
                otherModifiedNodes.add(sourceBts.get(m));
            }
            //otherModifiedNodes += sourceBts.get(m) + ",";
        }

        //otherModifiedNodes = otherModifiedNodes.substring(0, otherModifiedNodes.lastIndexOf(','));
        //if (otherModifiedNodes.length() > 0) {
            //otherModifiedNodes = otherModifiedNodes;
        //}
        return otherModifiedNodes;
    }

    ArrayList<String> getMoveNodeAndSubtreeModifiedTerms(UserInfoClass SessionUserInfo, QClass Q, TMSAPIClass TA, IntegerObject sis_session, DBGeneral dbGen, String TargetTermName, String MoveBTterm) {
        //if successfull must update modify fields of source and its direct bts. Also update target
        //source will be updated by CreateModify_Finalization called in DB_MODIFY mode
        ArrayList<String> otherModifiedNodes = new ArrayList<String>();

        ArrayList<String> sourceBts = new ArrayList<String>();
        sourceBts = dbGen.returnResults(SessionUserInfo, TargetTermName, ConstantParameters.bt_kwd, Q, TA, sis_session);
        sourceBts.trimToSize();

        otherModifiedNodes.add(MoveBTterm.trim());// += MoveBTterm.trim() + ",";

        for (int m = 0; m < sourceBts.size(); m++) {
            if(otherModifiedNodes.contains(sourceBts.get(m))==false){
                otherModifiedNodes.add(sourceBts.get(m));
            }
            //otherModifiedNodes += sourceBts.get(m) + ",";
        }

        //otherModifiedNodes = otherModifiedNodes.substring(0, otherModifiedNodes.lastIndexOf(','));
        //if (otherModifiedNodes.length() > 0) {
          //  otherModifiedNodes = otherModifiedNodes;
        //}
        return otherModifiedNodes;
    }

    ArrayList<String> getConnectNodeModifiedTerms(String MoveBTterm) {
        //if successfull must update modify fields of source and target
        //source will be updated by CreateModify_Finalization called in DB_MODIFY mode
        ArrayList<String> otherModifiedNodes = new ArrayList<String>();

        otherModifiedNodes.add(MoveBTterm.trim());// += MoveBTterm.trim();

        //if (otherModifiedNodes.length() > 0) {
//            otherModifiedNodes = otherModifiedNodes;
//        }
        return otherModifiedNodes;
    }

    ArrayList<String> getRemoveBTModifiedTerms(String TargetBTforDeletion) {
        //if successfull must update modify fields of source and target
        //source will be updated by CreateModify_Finalization called in DB_MODIFY mode
        ArrayList<String> otherModifiedNodes = new ArrayList<String>();

        otherModifiedNodes.add(TargetBTforDeletion.trim());// += TargetBTforDeletion.trim();

        //if (otherModifiedNodes.length() > 0) {
          //  otherModifiedNodes = otherModifiedNodes;
        //}
        return otherModifiedNodes;
    }

    boolean MoveToHierBugFix(String selectedThesaurus, int set_check_nodes, QClass Q, IntegerObject sis_session, DBGeneral dbGen, StringObject errorMsg, final String uiLang) {


        int SisSessionId = sis_session.getValue();
        Q.reset_name_scope();
        Utilities u = new Utilities();

        ArrayList<String> checkNodes = dbGen.get_Node_Names_Of_Set(set_check_nodes, false, Q, sis_session);
        StringObject TopTermObjClass = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("TopTerm"));
        StringObject HierarchyObj = new StringObject();//(SessionUserInfo.selectedThesaurus.concat("ThesaurusNotionType"));
        StringObject TopTermHierRelationObj = new StringObject();//("belongs_to_" + SessionUserInfo.selectedThesaurus.toLowerCase().concat("_hierarchy"));

        DBThesaurusReferences dbtr = new DBThesaurusReferences();

        dbtr.getThesaurusClass_TopTerm(selectedThesaurus, Q, sis_session.getValue(), TopTermObjClass);
        dbtr.getThesaurusCategory_belongs_to_hierarchy(selectedThesaurus, Q, sis_session.getValue(), TopTermHierRelationObj);
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), HierarchyObj);
        String prefixClass = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());

        for (int i = 0; i < checkNodes.size(); i++) {

            Q.reset_name_scope();

            if (Q.set_current_node( new StringObject(checkNodes.get(i))) != QClass.APIFail) {

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\n\nChecking Node :" + checkNodes.get(i) + "\n\n");
                //COLLECT ALL TARGET NODE'S BTS UNTIL TOP TERMS
                int target_set = Q.set_get_new();
                Q.set_put( target_set);
                Q.reset_set( target_set);

                int set_recursive_bts = Q.set_get_new();
                dbGen.collect_Recurcively_ALL_BTs_Of_Set(selectedThesaurus, target_set, set_recursive_bts, false, Q, sis_session);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_recursive_bts:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n"));

                //COLLECT TARGET TERM'S CLASSES And get their TOP Terms
                Q.reset_set( target_set);
                int set_classes = Q.get_classes( target_set);
                Q.reset_set( set_classes);

                Q.reset_name_scope();
                Q.set_current_node( HierarchyObj);
                int set_class_filter = Q.get_all_instances( 0);//will include instances of THES1ObsoleteHierarchy
                Q.reset_set( set_class_filter);

                Q.set_intersect( set_classes, set_class_filter);
                Q.reset_set( set_classes);

                int set_classes_topterms = Q.get_from_node_by_category( set_classes, TopTermObjClass, TopTermHierRelationObj);
                Q.reset_set( set_classes_topterms);
                //int set_classes_topterms = Q.get_from_node( set_classes);
                //Q.reset_set(set_classes_topterms);

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_classes_topterms:\n" +dbGen.getStringList_Of_Set(set_classes_topterms, ",\n"));
                Q.reset_name_scope();

                //GET TOP TERM INSTANCES in order to filter recursive BTs of target
                Q.reset_name_scope();
                Q.set_current_node( TopTermObjClass);
                int set_top_terms = Q.get_instances( 0);
                Q.reset_set( set_top_terms);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_top_terms:\n" +dbGen.getStringList_Of_Set(set_top_terms, ",\n"));

                /*
                //FILTER TARGETTERM'S Classes instances WITH TOPTERM instansces
                Q.set_intersect(set_classes_topterms, set_top_terms);
                Q.reset_set( set_classes_topterms);
                 */

                //FILTER TARGETTERM'S RECURSIVE BTS WITH TOPTERM INSATNCES
                Q.set_intersect( set_recursive_bts, set_top_terms);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nFiltered set_recursive_bts:\n" +dbGen.getStringList_Of_Set(set_recursive_bts, ",\n"));

                //MAKE A COPY OF TARGET TERM'S FILTERED CLASSES TopTerms
                int set_classes_topterms_copy = Q.set_get_new();
                Q.set_copy( set_classes_topterms_copy, set_classes_topterms);

                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"\n\nset_classes_topterms_copy:\n" +dbGen.getStringList_Of_Set(set_classes_topterms_copy, ",\n"));

                //FIND OUT WHICH CLASSES OF TARGET NODE SHOULD BE DELETED (ALL RECURSIVE BTS OF TARGET DO NOT INCLUDE THEIR RELEVANT TOP TERMS)
                Q.set_difference( set_classes_topterms, set_recursive_bts);
                Q.reset_set( set_classes_topterms);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\n\nDelete Classes Instances:\n" + dbGen.getStringList_Of_Set(set_classes_topterms, ",\n", Q, sis_session));
                if (Q.set_get_card( set_classes_topterms) > 0) {

                    ArrayList<String> deleteInstancesClasses = dbGen.get_Node_Names_Of_Set(set_classes_topterms, true, Q, sis_session);

                    String tempStr = checkNodes.get(i);
                    tempStr = dbGen.removePrefix(tempStr);
                    int ret;
                    for (int k = 0; k < deleteInstancesClasses.size(); k++) {

                        //ensure that no top term relation will be deleted. Top Terms should not reach at this point of code but just in case
                        if (tempStr.compareTo(deleteInstancesClasses.get(k)) == 0) {
                            continue;
                        }

                        Identifier from = new Identifier(checkNodes.get(i));
                        Identifier to = new Identifier(prefixClass.concat(deleteInstancesClasses.get(k)));

                        ret = Q.CHECK_Delete_Instance( from, to);
                        if (ret == QClass.APIFail) {
                            errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/GeneralUpdateNodeError",  new String[]{tempStr},uiLang));
                            //errorMsg.setValue("An error occurred during update of node " + tempStr+".");
                            return false;
                        }
                    }

                }

                //FIND OUT WHICH CLASSES OF TARGET NODE SHOULD BE ADDED(ALL RECURSIVE BTS OF TARGET DO NOT INCLUDE THEIR RELEVANT TOP TERMS)
                Q.set_difference( set_recursive_bts, set_classes_topterms_copy);
                Q.reset_set( set_recursive_bts);
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "\n\nAdd class Instances:\n" + dbGen.getStringList_Of_Set(set_recursive_bts, ",\n", Q, sis_session));
                if (Q.set_get_card( set_recursive_bts) > 0) {

                    ArrayList<String> addInstancesClasses = dbGen.get_Node_Names_Of_Set(set_recursive_bts, true, Q, sis_session);
                    int ret;
                    for (int k = 0; k < addInstancesClasses.size(); k++) {

                        Identifier from = new Identifier(checkNodes.get(i));
                        Identifier to = new Identifier(prefixClass.concat(addInstancesClasses.get(k)));
                        ret = Q.CHECK_Add_Instance( from, to);
                        if (ret == QClass.APIFail) {

                            String tempStr = checkNodes.get(i);
                            tempStr = dbGen.removePrefix(tempStr);

                            errorMsg.setValue(u.translateFromMessagesXML("root/EditTerm/Move2Hierarchy/GeneralUpdateNodeError",  new String[]{tempStr},uiLang));
                            //errorMsg.setValue("An error occurred during update of node " + tempStr+".");
                            return false;
                        }
                    }
                }

            } else {
                Q.free_all_sets();
                return false;
            }
            Q.free_all_sets();

        }
        Q.reset_name_scope();
        //dbGen.collect_Direct_Links_Of_Set( set_check_nodes, includeTarget, user, user, SisSessionId);
        Q.free_all_sets();

        return true;
    }
    /*---------------------------------------------------------------------
    doPost()
    ----------------------------------------------------------------------*/

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
