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

import Users.UserInfoClass;
import Users.UsersClass;
import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.SearchCriteria;
import Utils.Parameters;
import Utils.Utilities;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;

/*---------------------------------------------------------------------
 SearchResults_Users
 -----------------------------------------------------------------------
 servlet called for displaying Users
 ----------------------------------------------------------------------*/
public class SearchResults_Users extends ApplicationBasicServlet {

    final String xml_header = ConstantParameters.xmlHeader;

    /*---------------------------------------------------------------------
     processRequest()
     ----------------------------------------------------------------------*/
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (SystemIsLockedForAdministrativeJobs(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response, sessionInstance);

        PrintWriter out = response.getWriter();
        String startRecord = (String) request.getParameter("pageFirstResult");
        try {
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                if(startRecord!=null && startRecord.matches("SaveAll")){
                    out.println("Session Invalidate");
                }
                else{
                    response.sendRedirect("Index");
                }
                return;
            }

            

            // get servlet's parameters
            String updateUsersCriteria = (String) request.getParameter("updateUserCriteria");
            String ListStepStr = getServletContext().getInitParameter("ListStep");
            String THEMASUsersFileName = request.getSession().getServletContext().getRealPath("/" + UsersClass.WebAppUsersXMLFilePath);

            Utilities u = new Utilities();
            SearchCriteria searchCriteria;

            // -------------------- paging info And criteria retrieval-------------------------- 
            //initial values --> will change from the following code
            int usersPagingListStep = new Integer(ListStepStr).intValue();
            int usersPagingFirst = 1;
            int usersPagingQueryResultsCount = 0;

            if (updateUsersCriteria != null) { // detect if search was pressed or left menu option was triggered
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Users", updateUsersCriteria, request, u);
                sessionInstance.setAttribute("SearchCriteria_Users", searchCriteria);

            } else {  //else try to read criteria for this user
                searchCriteria = (SearchCriteria) sessionInstance.getAttribute("SearchCriteria_Users");
            }

            if (searchCriteria == null) {//tab pressed without any criteria previously set -- > default == list all with default output
                searchCriteria = SearchCriteria.createSearchCriteriaObject("SearchCriteria_Users", "*", request, u);
                sessionInstance.setAttribute("SearchCriteria_Users", searchCriteria);

            }

            if (startRecord == null) { //paging criteria were not passed directly through url so read them from Search criteria object
                int index = searchCriteria.pagingNames.indexOf("usersResults");
                usersPagingFirst = searchCriteria.pagingValues.get(index);
            }

            //if SaveAll icon was pressed then results file will be prepared -->nothing to do with paging
            //if no such parameter existed then paging should be READ From Search criteria --> one upper tab was pressed
            if (startRecord != null && !startRecord.matches("SaveAll")) {

                usersPagingFirst = Integer.parseInt(startRecord);
                if (usersPagingFirst != 0) {
                    int index = searchCriteria.pagingNames.indexOf("usersResults");
                    searchCriteria.pagingValues.set(index, usersPagingFirst);
                    sessionInstance.setAttribute("SearchCriteria_Terms", searchCriteria);

                } else {
                    int index = searchCriteria.pagingNames.indexOf("usersResults");
                    usersPagingFirst = searchCriteria.pagingValues.get(index).intValue();
                }
            }

            String[] input = new String[searchCriteria.input.size()];
            searchCriteria.input.toArray(input);
            String[] ops = new String[searchCriteria.operator.size()];
            searchCriteria.operator.toArray(ops);
            String[] inputValue = new String[searchCriteria.value.size()];
            searchCriteria.value.toArray(inputValue);
            String operator = searchCriteria.CombineOperator;
            String[] output = new String[searchCriteria.output.size()];
            searchCriteria.output.toArray(output);

            // handle search operators (not) starts / ends with
            //no need to inform search operators
            //u.InformSearchOperatorsAndValuesWithSpecialCharacters(input,ops, inputValue,true);
            //-------------------- paging info And criteria retrieval-------------------------- 

            StringBuffer xml = new StringBuffer();
            StringBuffer xmlResults = new StringBuffer();
            float elapsedTimeSec;

            // ---------------- SYNCHRONIZED BLOCK (BEGIN)---------------- problematic action: View All Terms            
            synchronized (this) {
                long startTime = Utilities.startTimer();

                UsersClass tmsUsers = new UsersClass();
                ArrayList allResultsUsers = tmsUsers.ReadWebAppUsersXMLFile(THEMASUsersFileName);

                //Get only those users that will appear in next page
                usersPagingQueryResultsCount = allResultsUsers.size();
                if (usersPagingFirst > usersPagingQueryResultsCount) {
                    usersPagingFirst = 1;
                }
                ArrayList<UserInfoClass> resultsUsers = new ArrayList<UserInfoClass>();
                for (int i = 0; i < usersPagingListStep; i++) {
                    if (i + usersPagingFirst > usersPagingQueryResultsCount) {
                        break;
                    }
                    UserInfoClass tmp = (UserInfoClass) allResultsUsers.get(i + usersPagingFirst - 1);
                    resultsUsers.add(tmp);
                }
                tmsUsers.getResultsInXml(request, resultsUsers, output, xmlResults);

                elapsedTimeSec = Utilities.stopTimer(startTime);
                // ---------------- SYNCHRONIZED BLOCK (END)----------------             
            }
            xml.append(u.getXMLStart(ConstantParameters.LMENU_USERS));
            xml.append("<results>");
            xml.append(u.writePagingInfoXML(usersPagingListStep, usersPagingFirst, usersPagingQueryResultsCount, elapsedTimeSec, "SearchResults_Users"));
            xml.append("</results>");

            xml.append(u.getXMLMiddle(xmlResults.toString(), "SearchUsersResults"));
            xml.append(u.getXMLUserInfo(SessionUserInfo));
            xml.append(u.getXMLEnd());

            u.XmlPrintWriterTransform(out, xml, sessionInstance.path + "/xml-xsl/page_contents.xsl");

        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + ".Exception catched in servlet " + getServletName() + ". Message:" + e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally {
            out.close();
            sessionInstance.writeBackToSession(session);
        }
    }

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
}
