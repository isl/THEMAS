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
package DB_Classes;



import Utils.Utilities;
import java.util.*;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;


/*---------------------------------------------------------------------
DBRemove_Facet
-----------------------------------------------------------------------
general class with methods used for the deletion of facets from the DB
----------------------------------------------------------------------*/
public class DBRemove_Facet {

    /*
    QClass Q = new QClass();
    DBGeneral dbGen;
    HttpServlet ServletCaller;
    
    IntegerObject sis_session;
    IntegerObject tms_session;*/

    /*----------------------------------------------------------------------
    Constructor of DBRemove_Facet
    -----------------------------------------------------------------------*/
    public DBRemove_Facet(/*HttpSession session,HttpServlet caller, IntegerObject sisSession, IntegerObject tmsSession*/) {
        /*ServletCaller = caller;
        sis_session = sisSession;
        tms_session = tmsSession;
        dbGen = new DBGeneral();*/
        
    }

    /*---------------------------------------------------------------------
    AbandonFacet()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetFacet: the Facet to be abandoned
    FUNCTION: abandons the given facet
    ----------------------------------------------------------------------*/
    public String AbandonFacet(TMSAPIClass TA,IntegerObject tms_session,  DBGeneral dbGen, StringObject targetFacet) {
        String errorMsg = new String("");

        int ret = TA.NOT_IMPLEMENTED_AbandonFacet(targetFacet); 
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret, TA, null,tms_session);
        }

        return errorMsg;
    }

    /*---------------------------------------------------------------------
    UndoAbandonFacet()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetFacet: the Facet to be undo abandoned
    FUNCTION: undo abandons the given facet
    ----------------------------------------------------------------------*/
    public String UndoAbandonFacet(TMSAPIClass TA,IntegerObject tms_session, DBGeneral dbGen,StringObject targetFacet ) {

        // CHECK AGAIN
        String errorMsg = new String("");

        int ret = TA.NOT_IMPLEMENTED_UndoAbandonFacet(targetFacet);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret,TA,  null,tms_session);
        }

        return errorMsg;
    }

    /*---------------------------------------------------------------------
    DeleteFacet()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetFacet: the NEW Facet to be deleted
    FUNCTION: deletes the given NEW facet (if it exists)
    ----------------------------------------------------------------------*/
    public String DeleteFacet(QClass Q,TMSAPIClass TA,IntegerObject sis_session, IntegerObject tms_session,  DBGeneral dbGen,StringObject targetFacet) {
        String errorMsg = new String("");

        
        
        if (dbGen.check_exist(targetFacet.getValue(),Q,sis_session) == false) {
            Vector<String> errorArgs = new Vector<String>();
            errorArgs.add(targetFacet.getValue());
            StringObject translatedMsgObj = new StringObject("");
            dbGen.Translate(translatedMsgObj, "root/EditFacet/Deletion/FacetNotFound", errorArgs, Utilities.getMessagesXml());
            //errorMsg = "Facet " + targetFacet + " does not exist";
            errorMsg = translatedMsgObj.getValue();
            return errorMsg;
        }

        int ret = TA.CHECK_DeleteFacet(targetFacet);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret,TA,  null,tms_session);
        }

        return errorMsg;
    }
}
