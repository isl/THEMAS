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

import Utils.ConstantParameters;
import Utils.SessionWrapperClass;
import Utils.Parameters;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Paths;

/**
 *
 * @author tzortzak
 */
public class DownloadFile extends ApplicationBasicServlet  {
   
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
        SessionWrapperClass sessionInstance = new SessionWrapperClass();
        init(request, response,sessionInstance);                  
        
        ServletOutputStream out = response.getOutputStream();
        try {
 
            // check for previous logon but because of ajax usage respond with Session Invalidate str
            
            if (sessionInstance.getAttribute("SessionUser") == null) {
                out.println("Session Invalidate");                
                response.sendRedirect("Index");
                return;
            }
            
            String webAppSaveResults_Folder = Parameters.Save_Results_Folder;
            String webAppSaveResults_temporary_files_Folder = Parameters.Save_Results_Temp_Folder;
            String webAppSaveResults_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder);
            //String webAppSaveResults_temporary_filesAbsolutePath = Parameters.BaseRealPath+"/"+webAppSaveResults_Folder + "/" + webAppSaveResults_temporary_files_Folder;        
            String Save_Results_file_name = request.getParameter("targetFile");
            String Full_Save_Results_file_name = "";
            if(Save_Results_file_name.contains(ConstantParameters.LogFilesFolderName)){
                Full_Save_Results_file_name = Paths.get(Parameters.BaseRealPath).resolve(Save_Results_file_name).toString();
            }
            else{
                Full_Save_Results_file_name = webAppSaveResults_temporary_filesAbsolutePath + File.separator + Save_Results_file_name;
            }
            
            File srcdoc = new File(Full_Save_Results_file_name);
            if(srcdoc.exists()){
               
                FileInputStream fis=null;
                byte[] b;
                //response.setContentType("application/x-download");
                if(Save_Results_file_name.endsWith(".xml")){
                    response.setContentType("text/xml;charset=UTF-8");
                }
                
                response.setContentLength((new Long(srcdoc.length())).intValue());
                response.setHeader("Content-Disposition", "attachment; filename="+srcdoc.getName());
                                               
                fis = new FileInputStream(srcdoc);
                int n;

                while ((n = fis.available()) > 0) {

                    b = new byte[n];
                    int result = fis.read(b);
                    if(b.length>0){
                    out.write(b, 0, b.length);

                    if (result == -1) {
                        break;
                    }
                    }
                }
            }
            else{
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +" Could not find file to download: " + Save_Results_file_name);
            }
           
            
        }catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+".Exception catched in servlet " +getServletName()+". Message:" +e.getMessage());
            Utils.StaticClass.handleException(e);
        } finally { 
            out.flush();
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

}
