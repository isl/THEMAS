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
package SVGproducer;

import Users.UserInfoClass;
import Utils.Parameters;
import neo4j_sisapi.*;
import java.io.*;
import java.lang.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.SimpleDateFormat;

/*-------------------------------------------------------------------
  Class ProduceSVGGraph
  Class used to demonstrate the use of the SVGProduceEngine
 --------------------------------------------------------------------*/
public final class ProduceSVGGraph {

    /*-----------------------------------------------------------------------
                              Start()
    -------------------------------------------------------------------------
    FUNCTION: Triggers the SVG creation process...
    INPUT: - TargetName: the name of the node which SVG graph will be displayed
           - TargetKind: the kind of the node ('DESCRIPTOR' or 'HIERARCHY' or 'FACET')
           - termPrefix: g.e. EL`
           - facetPrefix: g.e. EKTClass`
           - req: the HttpServletRequest of the caller servlet (g.e. GraphicalView)
    -------------------------------------------------------------------------*/
    public String Start(UserInfoClass SessionUserInfo, String TargetName, String TargetKind, String termPrefix, String facetPrefix, HttpServletRequest request) {
        // webAppSVG_temporary_filesAbsolutePath: the absolute path of the web application where temporary SVG files are stored
        // (g.e. C:\local_users\karam\JAVA-Tools\THEMAS\build\web\SVGproducer\SVG_temporary_files)
        //String webAppSVG_temporary_filesPath = request.getSession().getServletContext().getInitParameter("SVG_temporary_filesPath"); // g.e. "SVGproducer/SVG_temporary_files"
        String webAppSVG_temporary_filesPath = Parameters.SVG_temporary_filesPath; // g.e. "SVGproducer/SVG_temporary_files"
        String webAppSVG_temporary_filesAbsolutePath = request.getSession().getServletContext().getRealPath("/"+webAppSVG_temporary_filesPath);
        String SVGcode = "";
        String SVG_file_path = "";
        String SVG_file_name = "";
        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Start " + ((UserInfoClass)sessionInstance.getAttribute("SessionUser")).selectedThesaurus) ;
        
        try {
            // Create a configuration object
            //ReadSVGConfig conf = new ReadSVGConfig(request.getSession());      
            ReadSVGConfig conf = new ReadSVGConfig(SessionUserInfo);
            // set conf hierarchy_names array with the input parameter TargetName
            conf.hierarchy_names = new String[1];
            conf.hierarchy_names[0] = TargetName;
            String hierarchyNames[] = conf.hierarchy_names;

            String hierarchyNameLang[] = conf.hierarchy_name_lang;
            String hierarchyNameStyle[] = conf.hierarchy_name_style;
			
            ProduceHierarchies_common SVGproducer = null;
            if (TargetKind.compareTo("DESCRIPTOR") == 0 || TargetKind.compareTo("HIERARCHY") == 0) { 
                SVGproducer = new ProduceMCHierarchy(conf); // MC: multiple categories
            }        
            if (TargetKind.compareTo("FACET") == 0) {
                SVGproducer = new ProduceISAHierarchy(conf);
            }
            SVGproducer.termPrefix = termPrefix;
            SVGproducer.facetPrefix = facetPrefix;

            try {
                String tempString = new String();

                SVG_file_name = "SVGtempGraph" + GetNow().replaceAll(":", "-") + ".svg";
                SVG_file_path = webAppSVG_temporary_filesAbsolutePath + File.separator + SVG_file_name;
                BufferedWriter out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVG_file_path), "UTF-8"));
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Start2222222 " + ((UserInfoClass)sessionInstance.getAttribute("SessionUser")).selectedThesaurus) ;
                tempString = SVGproducer.doJob(SessionUserInfo,hierarchyNames[0], hierarchyNameLang[0], hierarchyNameStyle[0]);

                // remove all unwanted prefixes		
                int DBPrefixesCount = SVGproducer.DBPrefixes.size();
                for (int i=0; i< DBPrefixesCount; i++) {
                    tempString = tempString.replaceAll(SVGproducer.DBPrefixes.get(i).toString(), "");
                }                
                out.write(tempString);
                out.close();
                out= null;
                
                SVGcode = tempString;
                tempString = null;
            }
            catch (Exception e) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+e);
                Utils.StaticClass.handleException(e);
            }
        } // try
        catch(Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+e);
            Utils.StaticClass.handleException(e);
        }

        return SVG_file_name;
    }
    
    /*----------------------------------------------------------------------
                         GetNow()
    ------------------------------------------------------------------------
    FUNCTION: - current date and time in "yyyy-MM-dd HH:mm:ss" format
    ------------------------------------------------------------------------*/
    public String GetNow() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }        
}
