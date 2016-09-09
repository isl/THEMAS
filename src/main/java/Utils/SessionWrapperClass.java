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
package Utils;

import Users.UserInfoClass;
import javax.servlet.http.HttpSession;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author tzortzak
 */
public class SessionWrapperClass {

    Vector<String> validAttrs;
    Hashtable<String, Object> keywords ;
    public String path;
    
    public  SessionWrapperClass(){
        validAttrs = new Vector<String>();
        validAttrs.add("currentTABup");
        validAttrs.add("SessionUser");
        validAttrs.add("SearchCriteria_Facets");
        validAttrs.add("SearchCriteria_Hierarchies");
        validAttrs.add("SearchCriteria_Terms");
        validAttrs.add("SearchCriteria_Sources");
        validAttrs.add("SearchCriteria_Users");
        keywords = new Hashtable<String, Object>();
    }
    
    public  SessionWrapperClass(SessionWrapperClass copy){
        this();
        copyContents(copy);
    }
    
   
    
    public void readSession(HttpSession session, HttpServletRequest request){
        
        String reqURL = request.getRequestURL().toString();
        String reqServletPath = request.getServletPath();
        int ServletPathIndex = reqURL.indexOf(reqServletPath);
        if(reqServletPath!=null&& reqServletPath.length()>0 && ServletPathIndex>0){
            path=reqURL.substring(0,ServletPathIndex);
        }
        else{
            path=reqURL;
            Utils.StaticClass.webAppSystemOutPrintln("Assigning reqURL to path. reqURL = "+reqURL);
        }
        String currentTABup = (String)session.getAttribute("currentTABup") ;
        UserInfoClass SessionUserInfo = (UserInfoClass) session.getAttribute("SessionUser") ;
        SearchCriteria SearchCriteria_Facets = (SearchCriteria) session.getAttribute("SearchCriteria_Facets") ;
        SearchCriteria SearchCriteria_Hierarchies = (SearchCriteria) session.getAttribute("SearchCriteria_Hierarchies") ;
        SearchCriteria SearchCriteria_Terms = (SearchCriteria) session.getAttribute("SearchCriteria_Terms") ;
        SearchCriteria SearchCriteria_Sources = (SearchCriteria) session.getAttribute("SearchCriteria_Sources") ;
        SearchCriteria SearchCriteria_Users = (SearchCriteria) session.getAttribute("SearchCriteria_Users") ;
        
        
        
        if(currentTABup!=null){
            keywords.put("currentTABup", currentTABup);
        }
        
        if(SessionUserInfo!=null){
            keywords.put("SessionUser", SessionUserInfo);
        }
        if(SearchCriteria_Facets!=null){
            keywords.put("SearchCriteria_Facets", SearchCriteria_Facets);
        }
        if(SearchCriteria_Hierarchies!=null){
            keywords.put("SearchCriteria_Hierarchies", SearchCriteria_Hierarchies);
        }
        if(SearchCriteria_Terms!=null){
            keywords.put("SearchCriteria_Terms", SearchCriteria_Terms);
        }
        if(SearchCriteria_Sources!=null){
            keywords.put("SearchCriteria_Sources", SearchCriteria_Sources);
        }
        if(SearchCriteria_Users!=null){
            keywords.put("SearchCriteria_Users", SearchCriteria_Users);
        }
    }
    
    public void setAttribute(String kwd,Object value){
        if(validAttrs.contains(kwd)){
            if(value==null){
                keywords.remove(kwd);
            }
            else{
                keywords.put(kwd, value);
            }
            
        }
    }
    
    public void copyContents(SessionWrapperClass source){
        
        String currentTABupCopy = (String)source.getAttribute("currentTABup") ;
        UserInfoClass SessionUserInfoCopy = (UserInfoClass) source.getAttribute("SessionUser") ;
        SearchCriteria SearchCriteria_FacetsCopy = (SearchCriteria) source.getAttribute("SearchCriteria_Facets") ;
        SearchCriteria SearchCriteria_HierarchiesCopy = (SearchCriteria) source.getAttribute("SearchCriteria_Hierarchies") ;
        SearchCriteria SearchCriteria_TermsCopy = (SearchCriteria) source.getAttribute("SearchCriteria_Terms") ;
        SearchCriteria SearchCriteria_SourcesCopy = (SearchCriteria) source.getAttribute("SearchCriteria_Sources") ;
        SearchCriteria SearchCriteria_UsersCopy = (SearchCriteria) source.getAttribute("SearchCriteria_Users") ;
        path = source.path;
        
        if(currentTABupCopy!=null){
            keywords.put("currentTABup", currentTABupCopy);
        }
        
        if(SessionUserInfoCopy!=null){
            keywords.put("SessionUser", SessionUserInfoCopy);
        }
        if(SearchCriteria_FacetsCopy!=null){
            keywords.put("SearchCriteria_Facets", SearchCriteria_FacetsCopy);
        }
        if(SearchCriteria_HierarchiesCopy!=null){
            keywords.put("SearchCriteria_Hierarchies", SearchCriteria_HierarchiesCopy);
        }
        if(SearchCriteria_TermsCopy!=null){
            keywords.put("SearchCriteria_Terms", SearchCriteria_TermsCopy);
        }
        if(SearchCriteria_SourcesCopy!=null){
            keywords.put("SearchCriteria_Sources", SearchCriteria_SourcesCopy);
        }
        if(SearchCriteria_UsersCopy!=null){
            keywords.put("SearchCriteria_Users", SearchCriteria_UsersCopy);
        }
    }
    
    public Object getAttribute(String kwd){
        return keywords.get(kwd);
    }
    
    public void writeBackToSession(HttpSession session){
        if(session==null ){
            return;
        }
        
        synchronized (session) {
            try {
                //first make all supported attrs null
                for (int i = 0; i < validAttrs.size(); i++) {
                    String targetAttr = validAttrs.get(i);
                    session.setAttribute(targetAttr, null);

                }

                //then write all values held in keywords Hashtable back to session.
                //Hashtable will not accept null values
                for (int i = 0; i < validAttrs.size(); i++) {
                    Object value = keywords.get(validAttrs.get(i));
                    session.setAttribute(validAttrs.get(i), value);

                }
            } 
            catch (IllegalStateException ex) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Tried to update an invalid session: " + ex.getMessage());
                //Utils.StaticClass.handleException(ex);
            }
        }
    }
}
