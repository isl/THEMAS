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
package DB_Admin;



import Utils.Parameters;
import org.w3c.dom.*;
import javax.servlet.http.*;
import java.nio.file.Paths;
import java.util.Vector;

/*-----------------------------------------------------
                  class ConfigDBadmin
-------------------------------------------------------*/
public class ConfigDBadmin {
    private XML_parserDBadmin configXmlParser;
    private XML_parserDBadmin translationsXmlParser;

    /*-----------------------------------------------------
                      Config()
    -------------------------------------------------------*/
    public ConfigDBadmin(String basePath) {
        String configFileName = Paths.get(basePath).resolve("DBadmin").resolve("tms_db_admin_config_files").resolve("config.xml").toString();
        String translationsFileName = Paths.get(basePath).resolve("translations").resolve("DBAdminTranslations.xml").toString();

        // initialize configXmlParser
        configXmlParser = new XML_parserDBadmin();
        int ret = configXmlParser.init(configFileName);
        if (ret == -1) {
          String errorMsg = configXmlParser.GetErrorMessage();
          Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+errorMsg);
          return;
        }
        // initialize translationsXmlParser
        translationsXmlParser = new XML_parserDBadmin();
        ret = translationsXmlParser.init(translationsFileName);
        if (ret == -1) {
          String errorMsg = translationsXmlParser.GetErrorMessage();
          Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+errorMsg);
          return;
        }
        
        String basepath = GetConfigurationValue("Neo4j_DB_FOLDER_PATH");
        String dbSubPath = GetConfigurationValue("Neo4j_DB_PATH");
        Utils.StaticClass.initializeDatabasePath(basepath+dbSubPath);
    }
    
    
    /*----------------------------------------------------------------------
                          GetConfigurationValue()
    ------------------------------------------------------------------------*/
    public String GetConfigurationValue(String configurationTagName) {
        Node configNodes[] = null;
        configNodes = configXmlParser.GetNodeListByTag(configurationTagName);
        if (configNodes == null ||configNodes.length==0 || (configNodes[0].hasChildNodes() ==false) || configNodes[0].getFirstChild()==null) {
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" GetConfigurationValue Nothing found for tag name " +configurationTagName);
            return "";
        }
        String value = configNodes[0].getFirstChild().getNodeValue();
        return value;
    }
    
    /*----------------------------------------------------------------------
                          GetConfigurationValues()
    ------------------------------------------------------------------------*/
    public Vector<String> GetConfigurationValues(String configurationTagName) {
        Node configNodes[] = null;
        
        configNodes = configXmlParser.GetNodeListByTag(configurationTagName);
        
        if (configNodes == null) {
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" GetConfigurationValues Nothing found for tag name " + configurationTagName);
            return null;
        }
        int nodesCount = configNodes.length;
        Vector<String> values =  new Vector<String>();
        for (int i=0; i < nodesCount; i++) {
            values.add(configNodes[i].getFirstChild().getNodeValue());
        }
        return values;
    }    
    
    /*----------------------------------------------------------------------
                          GetTranslation()
    ------------------------------------------------------------------------*/
    public String GetTranslation(String translationTagName) {
        String currentLanguageTagName = Parameters.UILang;


        Node configNodes[] = null;
        configNodes = translationsXmlParser.GetNodeListByTag(translationTagName);
        if (configNodes == null) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+" GetTranslation Nothing found");
            return "";
        }
        NodeList translationsList = configNodes[0].getChildNodes();
        int translationsListCount = translationsList.getLength();        
        
        // for each child node of current subtree root
        for(int i=0; i<translationsListCount; i++) {
            Node childNode = translationsList.item(i);
            NamedNodeMap attributeNodes = childNode.getAttributes();
            if(attributeNodes!=null && attributeNodes.getNamedItem("lang")!=null){
                String xmlTagLanguage = attributeNodes.getNamedItem("lang").getNodeValue();

                if(xmlTagLanguage.equals(currentLanguageTagName)){
                    String value = childNode.getFirstChild().getNodeValue();
                    return value;
                }

            }

            
        }
        return "";
    }    
    
}
