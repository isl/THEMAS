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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import java.text.Collator;

/**
 *
 * @author tzortzak
 */
public class SortItemLocaleComparator implements Comparator{


    Locale currentLocale;


    public SortItemLocaleComparator(Locale locale) {

        super();
        currentLocale = locale;
        
    }


    public int compare(Object o,Object n) {
        SortItem o1 = (SortItem) o;
        SortItem n1 = (SortItem) n;
        Collator gr_GRCollator = Collator.getInstance(currentLocale);
        String o1str =o1.getLogName();
        String n1str =n1.getLogName();
        
        // bug fix by karam: Collator method compare()
        // does not work properly for greek strings with blanks
        // g.e. before the following bug fix: 
        // "δημόσιος τομέας" < "δημόσιο χρέος" (correct is: >)
        // "δημόσιος Α" = "δημόσιοςΑ" (correct: <)
        o1str = o1str.replaceAll(" ", "_");
        n1str = n1str.replaceAll(" ", "_");        

        return (gr_GRCollator.compare(o1str.toLowerCase(currentLocale), n1str.toLowerCase(currentLocale)));
    }
}
