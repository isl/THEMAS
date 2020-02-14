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
package LoginAdmin;

import DB_Admin.CommonUtilsDBadmin;
import Users.UserInfoClass;
import Utils.SessionWrapperClass;
import Utils.SessionListener;
import Utils.Parameters;
import Utils.Utilities;
import java.io.PrintWriter;
import javax.servlet.http.*;
import javax.servlet.ServletContext;
/*---------------------------------------------------------------------
                       HiddenActionsCommon
-----------------------------------------------------------------------
 class with common functions for the set of THEMAS hidden actions-servlets
----------------------------------------------------------------------*/
public class HiddenActionsCommon {        
    /*---------------------------------------------------------------------
                            getXMLMiddle()
    -----------------------------------------------------------------------
    OUTPUT: - String XMLStr: an XML string with the necessary data of this servlet
    ----------------------------------------------------------------------*/    
    private String getXMLMiddle(ServletContext context,CommonUtilsDBadmin common_utils, String translationsXml, final String uiLang) {
        // get the active sessions
        Utilities u = new Utilities();
        int OtherActiveSessionsNO = SessionListener.activesessionsNO - 1;
        
        StringBuffer XMLMiddleStr = new StringBuffer("<content_THEMAS_HiddenActions>");
            XMLMiddleStr.append("<OtherActiveSessionsNO>" + OtherActiveSessionsNO + "</OtherActiveSessionsNO>");
            // get the SIS server current status
            String Neo4jStatus = ""; // ON / OFF
            
            boolean Neo4jIsRunning = Utils.StaticClass.isDbready();
            if (Neo4jIsRunning == true) {
                Neo4jStatus = "ON";
            }
            else {
                Neo4jStatus = "OFF";
            }        
            //Start_StopNeo4j
            
            XMLMiddleStr.append("<Start_StopNeo4j>");
                XMLMiddleStr.append("<Neo4jStatus>" + Neo4jStatus + "</Neo4jStatus>");        
            XMLMiddleStr.append("</Start_StopNeo4j>");            
            // check the system if it is locked / unlocked
            String SystemStatus = "ON";
            if (DB_Admin.DBAdminUtilities.isSystemLocked()) {
                SystemStatus = "OFF";
            }
            XMLMiddleStr.append("<Lock_UnlockSystem>");
                XMLMiddleStr.append("<SystemStatus>" + SystemStatus + "</SystemStatus>");        
            XMLMiddleStr.append("</Lock_UnlockSystem>");                        
        XMLMiddleStr.append("</content_THEMAS_HiddenActions>");
        
        XMLMiddleStr.append("<SystemConfigurations>");
            XMLMiddleStr.append("<UILanguage>");
            XMLMiddleStr.append(uiLang);
            XMLMiddleStr.append("</UILanguage>");
            XMLMiddleStr.append("<ListStep>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("ListStep")));
            XMLMiddleStr.append("</ListStep>");
            XMLMiddleStr.append("<TaxonomicalCodeFormat>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("TaxonomicalCodeFormat")));
            XMLMiddleStr.append("</TaxonomicalCodeFormat>");
            //XMLMiddleStr.append("<mailHost>");
            //XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("mailHost")));
            //XMLMiddleStr.append("</mailHost>");
            //XMLMiddleStr.append("<mailList>");
            //XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("mailList")));
            //XMLMiddleStr.append("</mailList>");
            XMLMiddleStr.append("<Automatic_Backups_Next_Day_Start_Time>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("Automatic_Backups_Next_Day_Start_Time")));
            XMLMiddleStr.append("</Automatic_Backups_Next_Day_Start_Time>");
            XMLMiddleStr.append("<Automatic_Backups_Description>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("Automatic_Backups_Description")));
            XMLMiddleStr.append("</Automatic_Backups_Description>");
            XMLMiddleStr.append("<Automatic_Backups_Hours_Interval>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("Automatic_Backups_Hours_Interval")));
            XMLMiddleStr.append("</Automatic_Backups_Hours_Interval>");
            XMLMiddleStr.append("<DELIMETER>");
            XMLMiddleStr.append(Utilities.escapeXML(context.getInitParameter("DELIMITER1")));
            XMLMiddleStr.append("</DELIMETER>");

            XMLMiddleStr.append("</SystemConfigurations>");
            if(translationsXml!=null && translationsXml.length()>0){
            
                XMLMiddleStr.append(translationsXml);
            }
        /*
            String Delimeter = Utilities.escapeXML(context.getInitParameter("DELIMITER1"));
            String currentTranslationWords =Utilities.escapeXML(context.getInitParameter("TRANSLATION_WORDS"));
            String currentTranslationIdentifiers =Utilities.escapeXML(context.getInitParameter("TRANSLATION_IDENTIFIERS"));
            String prefixSuffix =Utilities.escapeXML(context.getInitParameter("PrefixSuffix"));

            if(currentTranslationWords != null && currentTranslationIdentifiers!= null && prefixSuffix != null && Delimeter!=null){
                String[] Words = currentTranslationWords.split(Delimeter);
                String[] Identifiers = currentTranslationIdentifiers.split(Delimeter);

                if(Words.length == Identifiers.length){
                    for(int i=0; i< Words.length ; i++){
                        XMLMiddleStr.append("<TranslationPair>");
                            XMLMiddleStr.append("<TranslationWord>");
                            XMLMiddleStr.append(Words[i]);
                            XMLMiddleStr.append("</TranslationWord>");
                            XMLMiddleStr.append("<TranslationIdentifier>");
                            XMLMiddleStr.append(Identifiers[i]);
                            XMLMiddleStr.append("</TranslationIdentifier>");
                        XMLMiddleStr.append("</TranslationPair>");
                    }
                }
            }            
        XMLMiddleStr.append("</Translations>");
*/
        return XMLMiddleStr.toString();
    }                
    
    /*---------------------------------------------------------------------
                            writeXML()
    ----------------------------------------------------------------------*/    
    public void writeXML(SessionWrapperClass sessionInstance,ServletContext context,String currentTab, PrintWriter out, CommonUtilsDBadmin common_utils) {
        StringBuffer xml = new StringBuffer();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        Utilities u = new Utilities();
        xml.append(u.getXMLStart(currentTab, SessionUserInfo.UILang));  
        xml.append(getXMLMiddle(context,common_utils,"",SessionUserInfo.UILang));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());
        u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/HiddenActions/admin_page.xsl");                
    }

    public void writeXML(SessionWrapperClass sessionInstance,ServletContext context,String currentTab, PrintWriter out, CommonUtilsDBadmin common_utils, String translationsXML) {
        StringBuffer xml = new StringBuffer();
        UserInfoClass SessionUserInfo = (UserInfoClass) sessionInstance.getAttribute("SessionUser");
        Utilities u = new Utilities();
        xml.append(u.getXMLStart(currentTab, SessionUserInfo.UILang));
        xml.append(getXMLMiddle(context,common_utils,translationsXML,SessionUserInfo.UILang));
        xml.append(u.getXMLUserInfo(SessionUserInfo));
        xml.append(u.getXMLEnd());
        
        u.XmlPrintWriterTransform(out, xml,sessionInstance.path +  "/xml-xsl/HiddenActions/admin_page.xsl");
    }
}