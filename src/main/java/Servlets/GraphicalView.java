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
import SVGproducer.ProduceSVGGraph;
import Servlets.ApplicationBasicServlet;
import Users.DBFilters;
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
import neo4j_sisapi.tmsapi.TMSAPIClass;

/*---------------------------------------------------------------------
                            GraphicalView
-----------------------------------------------------------------------
servlet called for displaying a SVG graph
INPUT PARAMETERS: - TargetName: the name of the node which SVG graph will be displayed
                  - TargetKind: the kind of the node ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
CALLED BY: GraphicalViewIconPressed() of graphicalView.js
----------------------------------------------------------------------*/
public class GraphicalView extends ApplicationBasicServlet {
    
    /*---------------------------------------------------------------------
                                    doGet()
    ----------------------------------------------------------------------*/    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            
            
            UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                out.println("Session Invalidate");                
                response.sendRedirect("Index");                
                return;
            }
            
            String TargetKind = request.getParameter("TargetKind");
            String CalledBySVGgraph = u.getDecodedParameterValue(request.getParameter("CalledBySVGgraph"));
            String TargetName = u.getDecodedParameterValue(request.getParameter("TargetName"));
            
            ArrayList<String> targetBTterms = new ArrayList<String>();
            
            
            if (SessionUserInfo == null) {
                SessionUserInfo = new UserInfoClass();
                SessionUserInfo.userGroup = u.getDecodedParameterValue(request.getParameter("userGroup"));
                SessionUserInfo.selectedThesaurus = u.getDecodedParameterValue(request.getParameter("selectedThesaurus"));
                SessionUserInfo.SVG_CategoriesFrom_for_traverse = u.getDecodedParameterValue(request.getParameter("SVG_CategoriesFrom_for_traverse"));
                SessionUserInfo.SVG_CategoriesNames_for_traverse = u.getDecodedParameterValue(request.getParameter("SVG_CategoriesNames_for_traverse"));
                sessionInstance.writeBackToSession(session);
            }

            			
            //open connection and start Query
            if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
            {
                Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ servlet " + this.getServletName());
                return;
            }
            
            /*if (CalledBySVGgraph != null) {
            
            //Parameters passing from URL through GET may not be correct
            //and URLDecoder.decode will fail getting the correct paramValue;
            //It works fine though with the whole query String. Thus String Handling to 
            //get TargetName.
            //When tried to pass it through GraphicalViewIconPressed() JS function 
            //it did not work well plus we should include xsl template calls to <xsl:call-template name="replace-string">
            //found in search_results_terms.xsl
            String query1 = request.getQueryString();
            String TargetQuery = URLDecoder.decode(query1, "UTF-8");
            
            
            } else {
            TargetName = u.getDecodedParameterValue(request.getParameter("TargetName"));
            }
             */

            // looking for Term prefix
            String termPrefix = dbtr.getThesaurusPrefix_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            // looking for Facet prefix
            String facetPrefix = dbtr.getThesaurusPrefix_Class(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue());
            // looking for Hierarchy prefix
            String hierarchyPrefix = facetPrefix;
            if (TargetKind.compareTo("HIERARCHY") == 0) {
                String topTerm = GetHierarchyTopTerm(SessionUserInfo, Q, sis_session, hierarchyPrefix.concat(TargetName));
                TargetName = topTerm;
            }

            if (TargetKind.compareTo("DESCRIPTOR") == 0) {
                TargetName = termPrefix.concat(TargetName);
                targetBTterms = GetTargetBTs(SessionUserInfo,Q, sis_session, TargetName);
            }
            if (TargetKind.compareTo("FACET") == 0) {
                TargetName = facetPrefix.concat(TargetName);
            }

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);

            // call SVG producer
            ProduceSVGGraph SVGproducer = new ProduceSVGGraph();
            String SVG_file_name = SVGproducer.Start(SessionUserInfo, TargetName, TargetKind, termPrefix, facetPrefix, request);

            // write the necessary XML data to be used for displaying the SVG graph
            String xmlResults = GraphicalViewXML(TargetName, TargetKind, SVG_file_name,targetBTterms);
            StringBuffer xml = new StringBuffer(xmlResults);
            // XSL transformation of XML output of servlet
            
            u.XmlPrintWriterTransform(out, xml,sessionInstance.path + "/SVGproducer/SVG.xsl");
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
                      GetHierarchyTopTerm()
    -----------------------------------------------------------------------
    INPUT: - TargetHierarchy: the name of the hierarchy to be queried
    OUTPUT: a String with the name of the top term of the given hierarchy
    ----------------------------------------------------------------------*/                                
    String GetHierarchyTopTerm(UserInfoClass SessionUserInfo,QClass Q, IntegerObject sis_session, String TargetHierarchy) {
        int SISsessionID = sis_session.getValue();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        // looking for class EKTTopTerm
        StringObject thesTopTerm = new StringObject();
        dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),thesTopTerm);
        // looking for category belongs_to_ekt_hierarchy
        
        StringObject belongs_to_thes_hierarchy = new StringObject("belongs_to_" + SessionUserInfo.selectedThesaurus.toLowerCase() + "_hierarchy");
        
        Q.reset_name_scope();
        long retL = Q.set_current_node(new StringObject(TargetHierarchy));
        int linksTo = Q.get_link_to_by_category(0, thesTopTerm, belongs_to_thes_hierarchy);
        Q.reset_set(linksTo);
        StringObject cls = new StringObject();
        StringObject label = new StringObject();
        CMValue cmv = new CMValue();
        Q.return_link(linksTo, cls, label, cmv);
        
        return cls.getValue();
    }  
    
    /*-----------------------------------------------------------------------
                              GetTargetBTs()
    -------------------------------------------------------------------------*/
    ArrayList<String> GetTargetBTs(UserInfoClass SessionUserInfo, QClass Q, IntegerObject sis_session,String TargetTerm) {
        // looking for Descriptor EKTHierarchy
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        
        
        StringObject thesDescriptor = new StringObject();
        dbtr.getThesaurusClass_Descriptor(SessionUserInfo.selectedThesaurus, Q,sis_session.getValue(),thesDescriptor);
        StringObject thesBT = new StringObject();
        dbtr.getThesaurusCategory_BT(SessionUserInfo.selectedThesaurus,  Q,sis_session.getValue(),thesBT);
        
        int SISApiSession = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(new StringObject(TargetTerm));
        int linkSet = Q.get_link_from_by_category(0, thesDescriptor, thesBT);
        Q.reset_set( linkSet);
        
        int linkSetToValues = Q.get_to_value(linkSet);
        Q.free_set(linkSet);        
        Q.reset_set(linkSetToValues);
        
        // FILTER terms set depending on user group
        DBFilters dbf = new DBFilters();
        linkSetToValues = dbf.FilterTermsResults(SessionUserInfo, linkSetToValues, Q, sis_session);
        
        ArrayList<String> targetBTs = new ArrayList<String>();
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
	if(Q.bulk_return_nodes(linkSetToValues, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                String BTterm = row.get_v1_cls_logicalname();
                String BTtermWithoutPrefix = BTterm.substring(BTterm.indexOf("`") + 1);
                targetBTs.add(BTtermWithoutPrefix);
            }
        }
        /*StringObject node = new StringObject();
        while (Q.retur_nodes(linkSetToValues, node) != QClass.APIFail) {
            String BTterm = node.getValue();
            String BTtermWithoutPrefix = BTterm.substring(BTterm.indexOf("`") + 1);
            targetBTs.add(BTtermWithoutPrefix);
        }*/
        Q.free_set(linkSetToValues);                
        
        return targetBTs;
    }    
           
    /*---------------------------------------------------------------------
                      GraphicalViewXML()
    -----------------------------------------------------------------------
    INPUT: - TargetName: the name of the node which SVG graph will be displayed
           - TargetKind: the kind of the node ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
           - SVG_file_name: the name of the temporary SVG file to be displayed (g.e. SVGtempGraph2008-08-22 16-41-35.svg)
    OUTPUT: a String with the XML representation of the necessary data to be used for displaying the SVG graph
    ----------------------------------------------------------------------*/                                
    String GraphicalViewXML(String TargetName, String TargetKind, String SVG_file_name,ArrayList targetBTterms) {
        String TargetNameWithoutPrefix = TargetName.substring(TargetName.indexOf("`") + 1);
        Utilities u = new Utilities();
        // temporary SVG graph
        String fileName = "SVGproducer/SVG_temporary_files/" + SVG_file_name;//SVG_file_name.replace(" ", "%20");
        // XML
        String XMLstr = ConstantParameters.xmlHeader ;
        XMLstr += "<page language=\""+Parameters.UILang+"\" primarylanguage=\""+Parameters.PrimaryLang.toLowerCase()+"\">";
            XMLstr += "<TargetName>" + Utilities.escapeXML(TargetName) + "</TargetName>";
            XMLstr += "<TargetNameWithoutPrefix>" + Utilities.escapeXML(TargetNameWithoutPrefix) + "</TargetNameWithoutPrefix>";
            XMLstr += "<TargetKind>" + TargetKind + "</TargetKind>";
            XMLstr += "<svgFileName>" + fileName + "</svgFileName>";
            // write the categories names to be displayed in SVG legend (Parameters.SVG_CategoriesNames_for_legend)
            // and their corresponding style names (Parameters.SVG_CategoriesStyles_for_traverse)
            // only in case of SVG graph for Descriptor or Hierarchy
            if (TargetKind.compareTo("DESCRIPTOR") == 0 || TargetKind.compareTo("HIERARCHY") == 0) {
                XMLstr += "<legend_data>";
                String DELIMITER = new String(":::");
                String[] SVG_CategoriesNames_for_legend = Parameters.SVG_CategoriesNames_for_legend.split(DELIMITER);
                String[] SVG_CategoriesStyles_for_traverse = Parameters.SVG_CategoriesStyles_for_traverse.split(DELIMITER);
                int categoriesCount = SVG_CategoriesNames_for_legend.length;
                for (int i=0; i < categoriesCount; i++) {
                    String categName = SVG_CategoriesNames_for_legend[i];
                    String categStyle = SVG_CategoriesStyles_for_traverse[i];
                    XMLstr += "<categ>";
                        XMLstr += "<name>" + categName + "</name>";
                        XMLstr += "<style>" + categStyle + "</style>";
                    XMLstr += "</categ>";
                }
                XMLstr += "</legend_data>";
            }
            // targetBTterms
            // write the BTs of the target only in case of SVG graph for Descriptor
            if (TargetKind.compareTo("DESCRIPTOR") == 0) {
                XMLstr += "<targetBTterms>";
                int targetBTtermsCount = targetBTterms.size();                    
                for (int i=0; i < targetBTtermsCount; i++) {
                    XMLstr += "<name_for_display>" + Utilities.escapeXML(targetBTterms.get(i).toString()) + "</name_for_display>";
                    XMLstr += "<name_for_URL>" + Utilities.escapeXML(targetBTterms.get(i).toString())  + "</name_for_URL>";

                }                    
                XMLstr += "</targetBTterms>";
            }
            
        XMLstr += "</page>";		
        return XMLstr;
    }                        
    
    /*---------------------------------------------------------------------
                                    doPost()
    ----------------------------------------------------------------------*/                            
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}