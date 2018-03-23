/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.Parameters;
import Utils.SessionWrapperClass;
import Utils.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.StringObject;
import neo4j_sisapi.TMSAPIClass;

/**
 *
 * @author Elias
 */
public class Thesaurus_ReferenceId_Info extends ApplicationBasicServlet {

    private enum RESPONSE_MESSAGES {SYSTEM_LOCKED_TRY_LATER,AUTHENTICATION_ERROR, DATABASE_CONNECTION_ERROR, THESAURUS_REFERENCE_ID_NOT_FOUND, SUCCESS};
    private enum RESPONSE_TYPES {NONE,FACET,TOPTERM,TERM};
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml;charset=UTF-8");       
        //skipClose = true;
        
        
        long refId = -1;
        String targetThesaurusName = "";
        String targetName ="";
        RESPONSE_TYPES targetType = RESPONSE_TYPES.NONE;
        PrintWriter out = response.getWriter();     
        
        //replacing code from ApplicationBasicServlet. SystemIsLockedForAdministrativeJobs that redirects to html page
        if(Parameters.BaseRealPath.length()==0){
            Parameters.BaseRealPath = request.getSession().getServletContext().getRealPath("");
        }
        if (DB_Admin.DBAdminUtilities.isSystemLocked()) {
            respond(out, request, RESPONSE_MESSAGES.SYSTEM_LOCKED_TRY_LATER, targetThesaurusName, refId, targetName, targetType);
            return;
        }
        
        HttpSession session = request.getSession();
        ServletContext context = getServletContext();
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);  
        
        
        try {
            
            QClass Q = new QClass(); TMSAPIClass TA = new TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            
            // check for previous logon but because of ajax usage respond with Session Invalidate str            
            UserInfoClass SessionUserInfo = (UserInfoClass)sessionInstance.getAttribute("SessionUser");             
            if (SessionUserInfo == null || !SessionUserInfo.servletAccessControl(this.getClass().getName())) {
                respond(out, request, RESPONSE_MESSAGES.AUTHENTICATION_ERROR, targetThesaurusName, refId, targetName, targetType);                
                return;
            }
            
            targetThesaurusName = SessionUserInfo.selectedThesaurus;
            
            //tools
            Utilities u = new Utilities();
            DBGeneral dbGen = new DBGeneral();
            
            DBThesaurusReferences dbtr = new DBThesaurusReferences();
            
            
            
            String targetReferenceId = u.getDecodedParameterValue(request.getParameter("referenceId"));
            
            if(targetReferenceId!=null && targetReferenceId.length()>0 && SessionUserInfo.selectedThesaurus!=null && SessionUserInfo.selectedThesaurus.length()>0){

                try{
                    refId = Long.parseLong(targetReferenceId);
                }
                catch(Exception ex){
                    Utils.StaticClass.handleException(ex);
                }
                if(refId>0){
                    //open connection
                    if(dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, null, SessionUserInfo.selectedThesaurus, true)==QClass.APIFail)
                    {
                        respond(out, request, RESPONSE_MESSAGES.DATABASE_CONNECTION_ERROR, targetThesaurusName, refId, targetName, targetType);   
                        return;
                    }

                    StringObject facetClassObj = new StringObject();
                    StringObject toptermClassObj = new StringObject();
                    StringObject descriptorClassObj = new StringObject();
                    
                    dbtr.getThesaurusClass_Facet(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), facetClassObj);
                    dbtr.getThesaurusClass_TopTerm(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), toptermClassObj);
                    dbtr.getThesaurusClass_Descriptor(SessionUserInfo.selectedThesaurus, Q, sis_session.getValue(), descriptorClassObj);

                    String fullName = Q.findLogicalNameByThesaurusReferenceId(SessionUserInfo.selectedThesaurus.toUpperCase(),refId);
                    if(fullName!=null && fullName.length()>0){
                        targetName = dbGen.removePrefix(fullName);
                        if(targetName==null || targetName.trim().length()==0){
                            respond(out, request, RESPONSE_MESSAGES.THESAURUS_REFERENCE_ID_NOT_FOUND, targetThesaurusName, refId, targetName==null?"":targetName, targetType);   
                            Q.free_all_sets();
                            Q.TEST_end_query();
                            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                            return;
                        }
                        
                        Q.reset_name_scope();
                        long nodeId = Q.set_current_node(new StringObject(fullName));
                        if(nodeId<0){
                            respond(out, request, RESPONSE_MESSAGES.THESAURUS_REFERENCE_ID_NOT_FOUND, targetThesaurusName, refId, targetName, targetType);   
                            Q.free_all_sets();
                            Q.TEST_end_query();
                            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                            return;
                        }
                        
                        int classes_set = Q.get_all_classes(0);
                        Q.reset_set(classes_set);
                        

                        ArrayList<String> classNames = dbGen.get_Node_Names_Of_Set(classes_set, false, Q, sis_session);
                        
                        if(classNames.contains(facetClassObj.getValue())){
                            targetType = RESPONSE_TYPES.FACET;
                        }
                        else if(classNames.contains(toptermClassObj.getValue())){
                            targetType = RESPONSE_TYPES.TOPTERM;
                        }
                        else if(classNames.contains(descriptorClassObj.getValue())){
                            targetType = RESPONSE_TYPES.TERM;
                        }
                        
                    }
                    else{                        
                        respond(out, request, RESPONSE_MESSAGES.THESAURUS_REFERENCE_ID_NOT_FOUND, targetThesaurusName, refId, targetName, targetType);   
                        Q.free_all_sets();
                        Q.TEST_end_query();
                        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                        return;
                    }

                    
                    Q.free_all_sets();
                    Q.TEST_end_query();
                    dbGen.CloseDBConnection(Q, null, sis_session, null, false);
                    
                    respond(out, request, RESPONSE_MESSAGES.SUCCESS, targetThesaurusName, refId, targetName, targetType); 
                }
            }
            
            
            
        }
        catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        }finally { 
            out.flush();
            if(session!=null) {
                session.invalidate();
            }            
        }
            
    }

    private void respond(PrintWriter out,HttpServletRequest request, RESPONSE_MESSAGES resultMessage, String targetThesaurusName, long refId, String retrievedName,RESPONSE_TYPES retrievedType){
        //StringBuffer xml = new StringBuffer();
        out.append(ConstantParameters.xmlHeader.trim()).append(System.lineSeparator());
        out.append("<response>").append(System.lineSeparator());
        
        
        out.append("\t<responseCode>").append(resultMessage.name()).append("</responseCode>").append(System.lineSeparator());
        out.append("\t<responseDescription>");
        switch(resultMessage){
            case SYSTEM_LOCKED_TRY_LATER:{
                out.append("System is currently locked for an adminsitrative job. Please try again later.");
                break;
            }
            case AUTHENTICATION_ERROR:{
                out.append("Authentication error occured. Propably external reader role is not configured for this thesaurus. Contact the system administrator with the followin request string that was applied to this Servlet: "+ Utilities.escapeXML(request.getQueryString()));
                break;
            }
            case DATABASE_CONNECTION_ERROR:{
                out.append("Error occured while trying to open connection to the database.Contact the system administrator with the following request string that was applied to this Servlet: "+ Utilities.escapeXML(request.getQueryString()));
                break;
            }
            case THESAURUS_REFERENCE_ID_NOT_FOUND:{
                out.append("Thesaurus reference Id:"+ refId +" was not found in thesaurus: " + targetThesaurusName);
                break;
            }
            case SUCCESS:{
                out.append("Retrieval via Thesaurus Reference id successfully completed");
                break;
            }
            default:{                
                break;
            }
        }
        out.append("</responseDescription>").append(System.lineSeparator());
        
        out.append("\t<TargetThesaurus>").append(targetThesaurusName).append("</TargetThesaurus>").append(System.lineSeparator());
        out.append("\t<ThesaurusReferencId>").append(""+refId).append("</ThesaurusReferencId>").append(System.lineSeparator());
        
        out.append("\t<RetrievedName>").append(retrievedName).append("</RetrievedName>").append(System.lineSeparator());
        out.append("\t<RetrievedType>").append(retrievedType.name()).append("</RetrievedType>").append(System.lineSeparator());
        
        
        out.append("</response>");
        
        //out.append(xml);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
