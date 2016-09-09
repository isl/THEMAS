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

/**
 *
 * @author tzortzak
 */
import java.util.*;
import java.text.Collator;

public class StrLenComparator  implements Comparator{
    
    public static final int Descending = 0;
    public static final int Ascending = 1;
    
    private int mode;
    public StrLenComparator(int sortOrder) {

        super();
        mode = sortOrder;
    }
    
    public int compare(Object m, Object n) {

        String m1 = (String) m;
        String n1 = (String) n;
        int m1Length = m1.length();
        int n1Length = n1.length();
        
        if(m1Length > n1Length){
            if(mode == Ascending)
                return 1;
            if(mode == Descending)
                return -1;
        }
        if(m1Length < n1Length){
            if(mode == Ascending)
                return -1;
            if(mode == Descending)
                return 1;
        }

        return 0;        
    }
     

}
