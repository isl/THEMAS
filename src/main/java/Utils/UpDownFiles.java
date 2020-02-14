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
package Utils;

/*
 * UpDownFiles.java
 *
 * Created on 24 ��������� 2005, 2:24 ��
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import java.io.*;
import java.util.HashMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

//import org.apache.commons.fileupload.
//import org.apache.commons.fileupload.disk.*;
//import org.apache.commons.fileupload.servlet.*;

/**
 * UpDownFiles class is used in Upload and Download servlet page. It has methods to prepare a
 * file download or upload. It has set() and get() methods used to set and retrieve the private attributes of the class.
 */
public class UpDownFiles {
    
    public static HashMap<String, String> uploadParams = new HashMap<String, String>();
    
    /**
     *
     */
    
    /**
     * Creates a new UpDownFiles instance.
     */
    public UpDownFiles(){
    }
    
    /**
     *
     * @param req
     * @param formData
     * @return
     
    public String[] prepareToUpXML(HttpServletRequest request, String[] formData){
        String[] dom = null;
        uploadParams.clear();
        
        try {
            // Create a new file upload handler
            FileItemFactory factory = new DiskFileItemFactory();
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            //DiskFileUpload uis deprecated
            //org.apache.commons.fileupload.servlet.ServletFileUpload upload = new org.apache.commons.fileupload.servlet.ServletFileUpload();

            
            
            java.util.List items = upload.parseRequest(request);
            java.util.Iterator iter = items.iterator();
            
            dom = new String[items.size()];
            int i=0;
            int k=0;
            
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                
                if (!item.isFormField()) {	// item is a file.
                    String filename = item.getName();
                    
                    if (!filename.endsWith(".xml")) {
                        // //Utils.StaticClass.webAppSystemOutPrintln("\t\t\t"+filename+"is not a file\n");
                        continue;
                    }
                    
                    if (filename != null && (filename.length()>0)){
                        //  //Utils.StaticClass.webAppSystemOutPrintln("\n\n\t\t\t\t============\tFile: " + filename +"\n");
                        
                        // store file contents and filename
                        dom[i++] = item.getString("UTF-8");
                        dom[i++] = filename;
                    }
                } else {
                    // //Utils.StaticClass.webAppSystemOutPrintln("\t\t\t------"+item.getFieldName()+", "+item.getString("UTF-8")+" added to formData!!!!!!\n");
                    formData[k++] = item.getString();
                    
                    uploadParams.put(item.getFieldName(), item.getString("UTF-8"));
                }
            }
        } catch (FileUploadException e){
            dom = null;
            //Utils.StaticClass.webAppSystemOutPrintln("File upload ERROR occured in UpDownFiles.prepareToUp");
            Utils.StaticClass.handleException(e);
        } catch (Exception e){
            dom = null;
            //Utils.StaticClass.webAppSystemOutPrintln("ERROR occured in UpDownFiles.prepareToUp");
            Utils.StaticClass.handleException(e);
        } finally {
            return dom;
        }
    }
    */
    /**
     *
     * @param req
     * @param formData
     * @return
     */
    public FileItem[] prepareToUpBinary(HttpServletRequest request, String[] formData){
        
        FileItem[] dom = null;
        uploadParams.clear();
        
        try {
            
            // Create a new file upload handler
            FileItemFactory factory = new DiskFileItemFactory();
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            
            java.util.List items = upload.parseRequest(request);
            java.util.Iterator iter = items.iterator();
            
            dom = new FileItem[items.size()];
            int i=0;
            int k=0;
            
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                
                if (!item.isFormField()) {	// item is a file.
                    String filename = item.getName();
                    //uploadParams.put("Filename", filename);
                    
                    if (filename != null && (filename.length()>0)){
                        
                        dom[i++] = item;
                    }
                } else {
                    // //Utils.StaticClass.webAppSystemOutPrintln("\t\t\t------"+item.getFieldName()+", "+item.getString("UTF-8")+" added to formData!!!!!!\n");
                    // formData[k++] = item.getString();
                    
                    uploadParams.put(item.getFieldName(), item.getString("UTF-8"));
                }
            }
            
        } catch (FileUploadException e){
            dom = null;
            //Utils.StaticClass.webAppSystemOutPrintln("File upload ERROR occured in UpDownFiles.prepareToUp");
            Utils.StaticClass.handleException(e);
        } catch (Exception e){
            dom = null;
            //Utils.StaticClass.webAppSystemOutPrintln("ERROR occured in UpDownFiles.prepareToUp");
            Utils.StaticClass.handleException(e);
        } finally {
            return dom;
        }
    }
    
    /**
     *
     * @param pw
     * @param xml
     * @return
    
    public boolean prepareToDownXML(PrintWriter pw, String xml){
        try{
            pw.write(ConstantParameters.xmlHeader);
            pw.write(xml);
            pw.flush();
            return true;
            
        }catch(Exception e){
            //Utils.StaticClass.webAppSystemOutPrintln("ERROR occured in UpDownFiles.prepareToDown");
            Utils.StaticClass.handleException(e);
            return false;
        }
    } */
    
    /**
     *
     * @param pw
     * @param xml
     * @return
     */
    public boolean prepareToDownBinary(File file, ServletOutputStream out){
        try{
            
            // Open the file stream
            FileInputStream in = new FileInputStream(file);
            
            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
            in.close();
            return true;
            
        }catch(Exception e){
            //Utils.StaticClass.webAppSystemOutPrintln("ERROR occured in UpDownFiles.prepareToDown");
            Utils.StaticClass.handleException(e);
            return false;
        }
    }
    
    
    /**
     *
     * @param item
     * @param filename
     * @return
     */
    public boolean writeBinary(FileItem item, String filename){
        try{
            File file = new File(filename);
            item.write(file);
            return true;
        }catch(Exception e){
            //Utils.StaticClass.webAppSystemOutPrintln("ERROR occured in UpDownFiles.writeBinary");
            Utils.StaticClass.handleException(e);
            return false;
        }
    }
    
    
    
}
