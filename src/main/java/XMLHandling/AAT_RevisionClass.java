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
package XMLHandling;

import java.text.ParseException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author tzortzak
 */
public class AAT_RevisionClass {

    public String action ="";
    private String date ="";
    public String userName ="";

    public AAT_RevisionClass(){
        
    }

    public void setDate(String targetDate){
        
        try {

            SimpleDateFormat parseFormatter = new SimpleDateFormat("yyyy-mm-dd");


            Date curDate = (Date)parseFormatter.parse(targetDate);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-DD");
            String s = formatter.format(curDate);
            if(s!=null && s.length()>0){
                this.date = s;
            }


        } catch (ParseException ex) {
            Logger.getLogger(AAT_RevisionClass.class.getName()).log(Level.SEVERE, null, ex);
            Utils.StaticClass.handleException(ex);
            
        }


    }
    
    public String getDate(){
        return this.date;
    }
}
