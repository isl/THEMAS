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
package DB_Classes;



import Utils.Utilities;
import javax.servlet.http.*;
import java.util.ArrayList;
import neo4j_sisapi.*;
import neo4j_sisapi.TMSAPIClass;

/*---------------------------------------------------------------------
                            DBConnect_Facet
-----------------------------------------------------------------------
class with methods updating facets (basically for creation) of the SIS data base
used by DBCreate_Modify_Facet
----------------------------------------------------------------------*/ 
public class DBConnect_Facet {
    /*QClass Q = new QClass();
    DBGeneral g;
    HttpServlet ServletCaller;
    
    IntegerObject sis_session;
    IntegerObject tms_session;  */          
	
    /*----------------------------------------------------------------------
                        Constructor of DBConnect_Facet
    -----------------------------------------------------------------------*/
    public DBConnect_Facet(/*HttpSession session, HttpServlet caller, IntegerObject sisSession, IntegerObject tmsSession*/) {
       /* ServletCaller = caller;
        g = new DBGeneral();
        sis_session = sisSession;
        tms_session = tmsSession;             */
        
    }	
 
    
    public String ConnectFacet(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,  StringObject targetFacet, boolean errorIfExists, String pathToErrorsXML, final String uiLang) {
        CMValue cmv = new CMValue();
        cmv.assign_node(targetFacet.getValue(),-1, Utilities.getTransliterationString(targetFacet.getValue(), true), -1);
        return ConnectFacetCMValue(selectedThesaurus,Q,TA,sis_session,tms_session,cmv,errorIfExists,pathToErrorsXML,uiLang);
    }
    
    public String ConnectFacetCMValue(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,  CMValue targetFacetCmv, boolean errorIfExists, String pathToErrorsXML, final String uiLang) {
        String errorMSG = new String("");
        DBGeneral dbGen = new DBGeneral();
        Utilities u = new Utilities();
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus,Q,sis_session.getValue());

        
        if (targetFacetCmv.getString().trim().equals(prefix)) {
            return  u.translateFromMessagesXML("root/EditFacet/Creation/EmptyName", null, uiLang);
        }

        if (dbGen.checkCMV_exist(targetFacetCmv,Q,sis_session) == false) {
            
            int ret = TA.CreateFacetCMValue(targetFacetCmv, new CMValue());
            
            if (ret == TMSAPIClass.TMS_APIFail) {
                errorMSG = errorMSG.concat(dbGen.check_success(ret, TA, null,tms_session));
                return errorMSG;
            }
            
        } else {
            if(errorIfExists){
                
                errorMSG = errorMSG.concat(/*"<tr><td>" +*/ dbGen.check_success(TMSAPIClass.TMS_APIFail,TA,
                        u.translateFromMessagesXML("root/EditFacet/Creation/FacetExists", new String[]{dbGen.removePrefix(targetFacetCmv.getString())}, uiLang)
                        /*"Facet " + dbGen.removePrefix(targetFacet.getValue()) + " already exists in the database."*/
                        ,tms_session)/* +
                        "</td></tr>"*/);
                return errorMSG;
            }
            
        }

        return errorMSG;

    }
}
