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
package Utils;

import java.util.*;
import java.text.Collator;

/**
 *
 * @author Elias
 */
public class SortItemComparator implements Comparator{
    
    public enum SortItemComparatorField {LOG_NAME,TRANSLITERATION,THESARUS_REFERENCE_ID, LINKCLASS_TRANSLITERATION_LOGNAME};
    
    private SortItemComparatorField compareMode = SortItemComparatorField.TRANSLITERATION;
    
    public SortItemComparator(SortItemComparatorField targetMode) {

        super();
        compareMode = targetMode;        
    }


    public int compare(Object o,Object n) {
        SortItem o1 = (SortItem) o;
        SortItem n1 = (SortItem) n;
        switch(compareMode){
            case LOG_NAME:{
                String o1str =o1.getLogName();
                String n1str =n1.getLogName();
                o1str = o1str.replaceAll(" ", "_");
                n1str = n1str.replaceAll(" ", "_");   
                return o1str.compareTo(n1str);                
            }
            case TRANSLITERATION:{
                String o1str =o1.getLogNameTransliteration();
                String n1str =n1.getLogNameTransliteration();
                if(o1str!=null){
                    o1str = o1str.replaceAll(" ", "_");
                }
                if(n1str !=null){
                    n1str  = n1str .replaceAll(" ", "_");
                }
                if(o1str==null || n1str ==null || o1str.compareTo(n1str)==0){
                    String o1LNstr =o1.getLogName();
                    String n1LNstr =n1.getLogName();
                    o1LNstr = o1LNstr.replaceAll(" ", "_");
                    n1LNstr = n1LNstr.replaceAll(" ", "_");   
                    return o1LNstr.compareTo(n1LNstr);       
                }
                else{
                    return o1str.compareTo(n1str);
                }
            }
            case LINKCLASS_TRANSLITERATION_LOGNAME:{
                String o1_link_str =o1.getLinkClass();
                String o2_link_str =n1.getLinkClass();
                if(o1_link_str!=null){
                    o1_link_str = o1_link_str.replaceAll(" ", "_");
                }
                if(o2_link_str !=null){
                    o2_link_str  = o2_link_str .replaceAll(" ", "_");
                }
                if(o1_link_str.compareTo(o2_link_str)==0){
                    
                    String o1str =o1.getLogNameTransliteration();
                    String n1str =n1.getLogNameTransliteration();
                    if(o1str!=null){
                        o1str = o1str.replaceAll(" ", "_");
                    }
                    if(n1str !=null){
                        n1str  = n1str .replaceAll(" ", "_");
                    }
                    if(o1str.compareTo(n1str)==0){
                        String o1LNstr =o1.getLogName();
                        String n1LNstr =n1.getLogName();
                        o1LNstr = o1LNstr.replaceAll(" ", "_");
                        n1LNstr = n1LNstr.replaceAll(" ", "_");   
                        return o1LNstr.compareTo(n1LNstr);       
                    }
                    else{
                        return o1str.compareTo(n1str);
                    }     
                }
                else{
                    return o1_link_str.compareTo(o2_link_str);
                }
            }
            case THESARUS_REFERENCE_ID:{
                
                return Long.compare(o1.getThesaurusReferenceId(), n1.getThesaurusReferenceId());                
            }
            default :{
                return 0;
            }
        }
    }
}
