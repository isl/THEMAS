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

import Users.DBFilters;
import Users.UserInfoClass;
import neo4j_sisapi.*;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.text.*;
import java.nio.charset.*;
import java.nio.*;
import javax.servlet.http.HttpSession;
/*-------------------------------------------------------------------
  class ProduceISAHierarchy:
  Used to produce a ISA hierarchy. It inherits all availlable methods from
  superclass ProduceHierarchies_common.
 --------------------------------------------------------------------*/
public final class ProduceISAHierarchy extends ProduceHierarchies_common {


	public ProduceISAHierarchy(ReadSVGConfig conf)
	{
		super();
		I = conf;
		I = conf;
		navbarEnabled = I.navbar_enabled;
		theScope = I.hierarchy_scope;
	}
  /*-----------------------------------------------------------------------
                              GetHierarchySet()
  -------------------------------------------------------------------------
  FUNCTION: - gets in Vectors V1 and V2 the NTs of the given "hierarchy"  parameter
  CALLED_BY: doGet() of superclass...
  -------------------------------------------------------------------------*/
  protected int GetHierarchySet(UserInfoClass SessionUserInfo, String hierarchy, String lang, String style)
  {

    Q.free_all_sets();
    Q.reset_name_scope();

    strobj.setValue(hierarchy);

    if (Q.set_current_node(strobj)!= Q.APIFail) {

       if(theScope.equals("supernodes"))
       {
       ret_set = Q.get_all_superclasses(0);
       }
       else if(theScope.equals("subnodes"))
       {
       ret_set = Q.get_all_subclasses(0);
       }

       Q.set_put(ret_set);

       Q.reset_set(ret_set);

        // FILTER hierarchies depending on user group
        DBFilters dbf = new DBFilters();
        
        Vector<Return_Isa_Row> retVals = new Vector<Return_Isa_Row>();
            if(Q.bulk_return_isA(ret_set, retVals)!=QClass.APIFail){
                for(Return_Isa_Row row:retVals){
                    //while (Q.retur_isA(ret_set, local_cls, local_cls2 ) != Q.APIFail) {
                    if(theScope.equals("supernodes") && dbf.FilterHierarchy(SessionUserInfo, new StringObject(row.get_v2_obj2_logicalname()), Q, sis_session) == false) {
                        continue;
                    }
                    else if(theScope.equals("subnodes") && dbf.FilterHierarchy(SessionUserInfo, new StringObject(row.get_v1_obj1_logicalname()), Q, sis_session) == false) {
                        continue;
                    }        
                    // karam bug fix
                    V3.add(style);
                    String str = ConvertLatinToUnicode(row.get_v1_obj1_logicalname(), lang);
                    String str2 = ConvertLatinToUnicode(row.get_v2_obj2_logicalname(), lang);
                    if(theScope.equals("supernodes")) {
                        V1.add(str2);
                        V2.add(str);
                    }
                    else if(theScope.equals("subnodes")) {
                        V1.add(str);
                        V2.add(str2);
                    }          
                }
            }
        /*
        StringObject local_cls = new StringObject();
        StringObject local_cls2 = new StringObject();
        while (Q.retur_isA(ret_set, local_cls, local_cls2 ) != Q.APIFail) {
            if(theScope.equals("supernodes") && dbf.FilterHierarchy(SessionUserInfo, local_cls2, Q, sis_session) == false) {
                continue;
            }
            else if(theScope.equals("subnodes") && dbf.FilterHierarchy(SessionUserInfo, local_cls, Q, sis_session) == false) {
                continue;
            }        
            // karam bug fix
            V3.add(style);
            String str = ConvertLatinToUnicode(local_cls.getValue(), lang);
            String str2 = ConvertLatinToUnicode(local_cls2.getValue(), lang);
            if(theScope.equals("supernodes")) {
                V1.add(str2);
                V2.add(str);
            }
            else if(theScope.equals("subnodes")) {
                V1.add(str);
                V2.add(str2);
            }          
        }
        */
        Q.free_set(ret_set);


    }
    else {
      svgNodeNotFoundErrorMsg();
      return -1;
    }
    return 0;
  }



}
