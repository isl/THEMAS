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
import java.util.ArrayList;
import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;

/**
 *
 * @author tzortzak
 */
public class DBRemove_Hierarchy {
    /*
    QClass Q = new QClass();
    DBGeneral dbGen;
    HttpServlet ServletCaller;
    
    IntegerObject sis_session;
    IntegerObject tms_session;*/
    /*----------------------------------------------------------------------
    Constructor of DBRemove_Hierarchy
    -----------------------------------------------------------------------*/
    public DBRemove_Hierarchy() {
       /* ServletCaller = caller;
        sis_session = sisSession;
        tms_session = tmsSession;        
        dbGen = new DBGeneral();*/
    }

    /*---------------------------------------------------------------------
    AbandonHierarchy()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetHierarchy: the Hierarchy to be abandoned
    FUNCTION: abandon the given Hierarchy
    ----------------------------------------------------------------------*/
    public String AbandonHierarchy(TMSAPIClass TA,IntegerObject tms_session, DBGeneral dbGen, StringObject targetHierarchy) {
        String errorMsg = new String("");

        int ret = TA.NOT_IMPLEMENTED_AbandonHierarchy(targetHierarchy);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret, TA, null,tms_session);
        }

        return errorMsg;
    }

    /*---------------------------------------------------------------------
    UndoAbandonHierarchy()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetHierarchy: the Hierarchy to be undo abandoned
    FUNCTION: undo abandon the given Hierarchy
    ----------------------------------------------------------------------*/
    public String UndoAbandonHierarchy(TMSAPIClass TA,IntegerObject tms_session, DBGeneral dbGen, StringObject targetHierarchy) {

        // CHECK AGAIN
        String errorMsg = new String("");

        int ret = TA.NOT_IMPLEMENTED_UndoAbandonHierarchy(targetHierarchy);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret, TA, null,tms_session);
        }
        

        return errorMsg;
    }

    /*---------------------------------------------------------------------
    DeleteHierarchy()
    -----------------------------------------------------------------------
    INPUT: - StringObject targetHierarchy: the NEW Hierarchy to be deleted
    FUNCTION: deletes the given NEW Hierarchy (if it exists)
    ----------------------------------------------------------------------*/
    public String DeleteHierarchy(QClass Q,TMSAPIClass TA,IntegerObject sis_session,IntegerObject tms_session, DBGeneral dbGen, StringObject targetHierarchy) {
        String errorMsg = new String("");
 
        if (dbGen.check_exist(targetHierarchy.getValue(),Q,sis_session) == false) {
            
            Utilities u = new Utilities();            
            errorMsg = u.translateFromMessagesXML("root/EditHierarchy/Deletion/HierarchyNotFound", new String[]{targetHierarchy.getValue()});
            //errorMsg = "Hierarchy " + targetHierarchy + " does not exist";
            
            return errorMsg;
        }

        int ret = TA.CHECK_DeleteHierarchy(targetHierarchy);
        if (ret == TMSAPIClass.TMS_APIFail) {
            errorMsg += dbGen.check_success(ret,TA,  null,tms_session);
        }
        
        return errorMsg;
    }

}
