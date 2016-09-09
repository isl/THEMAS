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

import neo4j_sisapi.*;


public class SortItem extends Object {
  public String log_name;
  public String linkClass; //If Sort Item holds the result of a term's attributes then link Class will hold the class of the link that targetTerm is linked to this SortItem
  public long  sysid;
  

  public SortItem() {
    super();
    log_name = new String();
    linkClass = new String();
    sysid = -1;
  }

  public SortItem(String name) {
    super();
    log_name = name;
    linkClass = new String();
    sysid = -1;

  }
    
  public SortItem(String name, long sys_id) {
    super();
    log_name = name;
    linkClass = new String();
    sysid = sys_id;

  }
  
  public SortItem(String name, long sys_id, String linkClassName) {
    super();
    log_name = name;
    linkClass = linkClassName;
    sysid = sys_id;

  }

  public String getLogName() {
    return log_name;
  }

  public String getLinkClass() {
    return linkClass;
  }
  
  public long getSysId() {

    return sysid;
  }
  
  public String toString() {
    return log_name + " (sysid:" + sysid + " from link " +linkClass+")"; 
  }

  @Override
  public int hashCode() {
    String hcStr = linkClass +log_name + Long.toString(sysid);
    return hcStr.hashCode();
  }

  @Override
  public boolean equals(Object o) {
      SortItem oSI = (SortItem) o;
      if(oSI==null){
          return false;
      }
      return ((sysid== oSI.sysid) && (log_name.equals(oSI.log_name)) && (linkClass.equals(oSI.linkClass)));
  }

  
}
