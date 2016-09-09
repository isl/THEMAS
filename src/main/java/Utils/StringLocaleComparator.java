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

import java.util.*;
import java.text.Collator;

/**
 *
 * @author tzortzak
 */
public class StringLocaleComparator implements Comparator{


    Locale currentLocale;
    public StringLocaleComparator(Locale locale) {

        super();
        currentLocale = locale;
    }


    public int compare(Object o,Object n) {
        String o1 = (String) o;
        String n1 = (String) n;
        
        // bug fix by karam: Collator method compare()
        // does not work properly for greek strings with blanks
        // g.e. before the following bug fix: 
        // "δημόσιος τομέας" < "δημόσιο χρέος" (correct is: >)
        // "δημόσιος Α" = "δημόσιοςΑ" (correct: <)
        o1 = o1.replaceAll(" ", "_");
        n1 = n1.replaceAll(" ", "_");

        Collator gr_GRCollator = Collator.getInstance(currentLocale);
        
        //Collator gr_GRCollator = Collator.getInstance(Locale.getDefault());
        //gr_GRCollator.setStrength(Collator.IDENTICAL);
        //return gr_GRCollator.getCollationKey(o1).compareTo(gr_GRCollator.getCollationKey(n1));

        return (gr_GRCollator.compare(o1, n1));
    }
}
