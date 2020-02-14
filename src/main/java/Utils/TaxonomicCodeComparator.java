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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tzortzak
 */
import java.util.*;
import java.text.Collator;

public class TaxonomicCodeComparator implements Comparator{

    Locale currentLocale;
    public TaxonomicCodeComparator(Locale locale) {

        super();
        currentLocale = locale;
    }


    public int compare(Object m,Object n) {
        
        TaxonomicCodeItem m1 = (TaxonomicCodeItem) m;
        TaxonomicCodeItem n1 = (TaxonomicCodeItem) n;
        int m1PartsDefined = m1.codeParts.size();
        int n1PartsDefined = n1.codeParts.size();
        
        int returnValue = 0; 
        

        //Collator gr_GRCollator = Collator.getInstance(currentLocale);
            
        if(m1PartsDefined > n1PartsDefined){

            for(int k=0; k< n1PartsDefined; k++){
 
                if(m1.codeParts.get(k).compareTo(n1.codeParts.get(k))!=0){
                //if(gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k)) != 0){
                    
                    //returnValue =  gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k));
                    returnValue =  m1.codeParts.get(k).compareTo(n1.codeParts.get(k));
                    break;
                }
            }

            if(returnValue == 0){
                returnValue = 1;
            }

        }
        else if(m1PartsDefined < n1PartsDefined){

            for(int k=0; k< m1PartsDefined; k++){

                //if(gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k)) != 0){
                if(m1.codeParts.get(k).compareTo(n1.codeParts.get(k))!=0){
                 
                    //returnValue =  gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k));
                    returnValue =  m1.codeParts.get(k).compareTo(n1.codeParts.get(k));
                    break;
                }
            }

            if(returnValue ==0){
                returnValue = -1;
            }
        }
        else {

            for(int k=0; k< m1PartsDefined; k++){

                //if(gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k)) != 0){
                if(m1.codeParts.get(k).compareTo(n1.codeParts.get(k))!=0){
                 
                    //returnValue =  gr_GRCollator.compare(m1.codeParts.get(k), n1.codeParts.get(k));
                    returnValue =  m1.codeParts.get(k).compareTo(n1.codeParts.get(k));
                    break;
                }
            }
            
            if(returnValue ==0) {
                String m1Translit = m1.nodeNameTransliteration;
                String n1Translit = n1.nodeNameTransliteration;
                if(m1Translit!=null && n1Translit!=null){
                    m1Translit = m1Translit.replaceAll(" ", "_");
                    n1Translit = n1Translit.replaceAll(" ", "_");
                    if(m1Translit.compareTo(n1Translit)!=0){
                        return m1Translit.compareTo(n1Translit);
                    }
                }
                
                String m1Str = m1.nodeName;
                String n1Str = n1.nodeName;
                // bug fix by karam: Collator method compare()
                // does not work properly for greek strings with blanks
                // g.e. before the following bug fix: 
                // "δημόσιος τομέας" < "δημόσιο χρέος" (correct is: >)
                // "δημόσιος Α" = "δημόσιοςΑ" (correct: <)
                m1Str = m1Str.replaceAll(" ", "_");
                n1Str = n1Str.replaceAll(" ", "_");                        
                
                //returnValue = gr_GRCollator.compare(m1Str, n1Str);
                returnValue = m1Str.compareTo(n1Str);
                //returnValue = gr_GRCollator.compare(m1.nodeName, n1.nodeName);
            }
        }
        
       // Utils.StaticClass.webAppSystemOut(Parameters.LogFilePrefix+"Compared " + m1.nodeName + " with " + n1.nodeName + " Result = " + returnValue);
        return returnValue;
    }
                  
        
  
}
