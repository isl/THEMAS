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



import java.util.Vector;
import java.util.Hashtable;

/**
 *
 * @author tzortzak
 */
public class NodeInfoStringContainer {

    public static final String CONTAINER_TYPE_TERM = "TERM";
    public static final String CONTAINER_TYPE_SOURCE = "SOURCE";
    public String containerType;
    public Hashtable<String,Vector<String>> descriptorInfo = null;
    
    public NodeInfoStringContainer(String type, String[] output){
       
        containerType = new String(type);
        if(type.compareTo(CONTAINER_TYPE_TERM)==0 ||type.compareTo(CONTAINER_TYPE_SOURCE)==0  ){
            
            descriptorInfo = new Hashtable<String,Vector<String>>();
            for(int i=0; i< output.length ; i++){
                Vector<String> values = new Vector<String>();
                descriptorInfo.put(output[i], values);               
            }
        }
        
    }
}
