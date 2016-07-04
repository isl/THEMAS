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
//import java.net.URLDecoder;
/*---------------------------------------------------------------------
                            MoveToHierarchy
-----------------------------------------------------------------------
servlet called for handling the Move to Hierarchy operation for each descriptor
----------------------------------------------------------------------*/
public class MoveToHierarchy extends ApplicationBasicServlet {
    
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/                        
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        if (SystemIsLockedForAdministrativeJobs(request, response)) return;
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);			
        
        PrintWriter out = response.getWriter();	        
        try{
            QClass Q = new QClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str

            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");
                return;
            }

            DBGeneral dbGen = new DBGeneral();

            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }


            // get the target term
            Utilities u = new Utilities();
            
            
            StringObject reasonTargetTermCannotBeMovedToHierarchy = new StringObject("");
            Vector<String> targetBTsVector = new Vector<String>();
            Vector<String> allHierarchiesVector = new Vector<String>();
            Vector<String> targetHierarchiesVector = new Vector<String>();
            boolean targetTermCanBeMovedToHierarchy = false;

            String targetTermUTF8 = u.getDecodedParameterValue(request.getParameter("targetTerm")); 
            String language = getServletContext().getInitParameter("LocaleLanguage");
            String country = getServletContext().getInitParameter("LocaleCountry");
            Locale targetLocale = new Locale(language, country);
            // looking for Term prefix
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            String termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue());
            if(targetTermUTF8==null){
                targetTermUTF8=new String("");
                targetTermCanBeMovedToHierarchy= false;
                reasonTargetTermCannotBeMovedToHierarchy.setValue("Δεν δηλώθηκε όρος προς μετακίνηση");

            }
            else{   
                StringObject targetTermUTF8WithPrefix = new StringObject(termPrefix.concat(targetTermUTF8));

                // check if the given target term can be moved to Hierarchy
                targetTermCanBeMovedToHierarchy = dbGen.DescriptorCanBeMovedToHierarchy(SessionUserInfo.selectedThesaurus, targetTermUTF8WithPrefix, reasonTargetTermCannotBeMovedToHierarchy,Q,sis_session);

                // get the target existing Hierarchies
                Vector<String> tmpVector = new Vector<String>();
                tmpVector = dbGen.getDescriptorHierarchies(SessionUserInfo.selectedThesaurus, targetTermUTF8WithPrefix,Q,sis_session);

                /*
                if (!tmpVector.get(0).getClass().getName().equals("neo4j_sisapi.StringObject")) {
                    targetTermCanBeMovedToHierarchy = false;
                    reasonTargetTermCannotBeMovedToHierarchy.setValue("Δεν ανήκει σε καμμία Ιεραρχία");
                }
                else {
                 */
                //if (tmpVector.get(0).getClass().getName().equals("neo4j_sisapi.StringObject")) { // it has at least one Hierarchy
                if (tmpVector.size()>0) { // it has at least one Hierarchy
                    int tmpVectorSize = tmpVector.size();
                    for (int i = 0; i < tmpVectorSize; i++) {
                        String currentHierarchy = tmpVector.get(i);
                        targetHierarchiesVector.add(currentHierarchy);
                    }        
                    Collections.sort(targetHierarchiesVector);
                }

                // get the target existing BTs        
                targetBTsVector = dbGen.getBT_NT(SessionUserInfo, targetTermUTF8, ConstantParameters.BT_DIRECTION,Q,sis_session);
                Collections.sort(targetBTsVector);

                // get the DB existing Hierarchies         
                allHierarchiesVector = dbGen.getHierarchies(SessionUserInfo.selectedThesaurus, Q,sis_session,targetLocale);
            }
            
            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            // write the necessary data to be used by Move to Hierarchy operation to XML
            String xmlResults = MoveToHierarchyXML(targetTermUTF8, targetTermCanBeMovedToHierarchy, reasonTargetTermCannotBeMovedToHierarchy,
                    targetHierarchiesVector, targetBTsVector, allHierarchiesVector);        

            // XML output of servlet - START
            StringBuffer xml = new StringBuffer();
            xml.append(u.getXMLStart(ConstantParameters.LMENU_TERMS));			 

            // XML output of servlet - MIDDLE
            // get the previously selected tab of the UP part
            String currentTABup = (String)(sessionInstance.getAttribute("currentTABup"));
            //if (currentTABup == null) currentTABup = "SearchResults";            
            // upPartXML + xmlResults : xreiazete gia tin hierarchical anaparastasi!!

            xml.append(u.getXMLMiddle(xmlResults, "Move2Hier")+"<targetTerm>"+targetTermUTF8+"</targetTerm><targetEditField>move2Hier</targetEditField>");

            // XML output of servlet - END
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());  

            // XSL transformation of XML output of servlet
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/xml-xsl/EditTermActions/Edit_Term.xsl");	
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
                      MoveToHierarchyXML()
    -----------------------------------------------------------------------
    INPUT: - String targetTerm: the target term to be moved to hierarchy
    OUTPUT: a String with the XML representation of the necessary data to be 
            used by Move to Hierarchy operation
    ----------------------------------------------------------------------*/                                
    String MoveToHierarchyXML(String targetTerm,boolean targetTermCanBeMovedToHierarchy, StringObject reasonTargetTermCannotBeMovedToHierarchy, Vector targetHierarchiesVector, Vector<String> targetBTsVector,Vector  allHierarchiesVector) {	
        
        Utilities u = new Utilities();
        DBGeneral dbGen = new DBGeneral();
        
        String XMLstr = "";
        XMLstr += "<MoveToHierarchyData>";
        
            // write the basic info of the target
            XMLstr += "<Target>";
                XMLstr += "<name>" + Utilities.escapeXML(targetTerm) + "</name>";
                XMLstr += "<targetTermCanBeMovedToHierarchy>" + targetTermCanBeMovedToHierarchy + "</targetTermCanBeMovedToHierarchy>";
                XMLstr += "<reasonTargetTermCannotBeMovedToHierarchy>" + reasonTargetTermCannotBeMovedToHierarchy.getValue() + "</reasonTargetTermCannotBeMovedToHierarchy>";
            XMLstr += "</Target>"; 
            // write the existing hierarchies of the target
            XMLstr += "<TargetHierarchies>";
            int targetHierarchiesSize = targetHierarchiesVector.size();
            for (int i = 0; i < targetHierarchiesSize; i++) {
                String currentHierarchy = (String) targetHierarchiesVector.get(i);
                currentHierarchy = dbGen.removePrefix(currentHierarchy);
                currentHierarchy = currentHierarchy;
                XMLstr += "<name>" + Utilities.escapeXML(currentHierarchy) + "</name>";
            }
            XMLstr += "</TargetHierarchies>";
            // write the existing BTs of the target
            XMLstr += "<TargetBTs>";
            int targetBTsSize = targetBTsVector.size();
            for (int i = 0; i < targetBTsSize; i++) {
                String currentBT = (String)targetBTsVector.get(i);
                XMLstr += "<name>" + Utilities.escapeXML(currentBT) + "</name>";
            }
            XMLstr += "</TargetBTs>";            

            // write all the existing hierarchies of the DB
            XMLstr += "<DBHierarchies>";
            int allHierarchiesVectorSize = allHierarchiesVector.size();
            for (int i = 0; i < allHierarchiesVectorSize; i++) {
                String currentHierarchy = (String)allHierarchiesVector.get(i);
                XMLstr += "<name>" + Utilities.escapeXML(currentHierarchy) + "</name>";
            }
            XMLstr += "</DBHierarchies>";            
        XMLstr += "</MoveToHierarchyData>";		
        return XMLstr;
    }                    
	
    /*---------------------------------------------------------------------
                                    doPost()
    ----------------------------------------------------------------------*/ 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        processRequest(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 
}
