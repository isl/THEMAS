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
    public long sysid;
    public long thesarurusReferenceId;
    public String log_name_transliteration;

    public SortItem() {
        log_name = new String();
        linkClass = new String();
        sysid = -1;
        thesarurusReferenceId = -1;
        log_name_transliteration = "";
    }

    public SortItem(String name) {
        this();
        log_name = name;
    }
    
    public SortItem(String name, String transliterationVal, long refIdVal) {
        this();
        log_name = name;
        thesarurusReferenceId = refIdVal;
        log_name_transliteration = transliterationVal;
    }

    public SortItem(String name, long sys_id) {
        this();
        log_name = name;
        sysid = sys_id;
    }
    
    public SortItem(String name, long sys_id, String transliterationVal, long refIdVal) {
        this();
        log_name = name;
        sysid = sys_id;
        thesarurusReferenceId = refIdVal;
        log_name_transliteration = transliterationVal;
    }

    public SortItem(String name, long sys_id, String linkClassName) {
        this();
        log_name = name;
        linkClass = linkClassName;
        sysid = sys_id;
    }
    
    public SortItem(String name, long sys_id, String linkClassName, String transliterationVal, long refIdVal) {
        this();
        log_name = name;
        linkClass = linkClassName;
        sysid = sys_id;
        thesarurusReferenceId = refIdVal;
        log_name_transliteration = transliterationVal;
    }
    
     public SortItem(CMValue cmv) {
        this();
        if(cmv!=null){
            log_name = cmv.getString();
            linkClass = "";
            sysid = cmv.getSysid();
            thesarurusReferenceId = cmv.getRefid();
            log_name_transliteration = cmv.getTransliterationString();
        }
    }


    public String getLogName() {
        return log_name;
    }
    
    public void setLogName(String newValue) {
        log_name = newValue==null?"":newValue;
    }
    
    public void setThesaurusReferenceId(long newValue) {
        thesarurusReferenceId = newValue<0?-1:newValue;
    }

    public String getLogNameTransliteration() {
        return log_name_transliteration;
    }

    public String getLinkClass() {
        return linkClass;
    }

    public long getSysId() {
        return sysid;
    }
    
    public long getThesaurusReferenceId() {
        return thesarurusReferenceId;
    }

    public String toString() {
        return log_name + " (sysid:" + sysid + " from link " + linkClass + " thesaurusReferenceId:" + thesarurusReferenceId + " lognameTransliteration: " + log_name_transliteration + ")";
    }

    @Override
    public int hashCode() {
        String hcStr = linkClass + log_name + Long.toString(sysid) + log_name_transliteration + Long.toString(thesarurusReferenceId);
        return hcStr.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        
        SortItem oSI = (SortItem) o;
        if (oSI == null) {
            return false;
        }
        //probably equality should not depend on sysId which is not permanent per node
        return (/*(sysid == oSI.sysid) &&*/ (log_name.equals(oSI.log_name)) && (linkClass.equals(oSI.linkClass)) && (thesarurusReferenceId == oSI.thesarurusReferenceId) && (log_name_transliteration.equals(oSI.log_name_transliteration)));
    }
    
    public SortItem getACopy(){
        return new SortItem(this.getLogName(),this.getSysId(),this.getLinkClass(),this.getLogNameTransliteration(),this.getThesaurusReferenceId());
    }

    public CMValue getCMValue() {
        return getCMValue("");
    }
    
    public CMValue getCMValue(String logNameValue) {
        
        CMValue retCmv = new CMValue();
        retCmv.assign_node((logNameValue==null || logNameValue.length()==0)?log_name:logNameValue, sysid, log_name_transliteration, thesarurusReferenceId);
        return retCmv;
    }
    
    

}
