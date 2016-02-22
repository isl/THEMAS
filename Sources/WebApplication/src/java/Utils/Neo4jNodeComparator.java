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


import DB_Admin.TSVExportsImports;
import java.util.*;
import java.text.Collator;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Hlias
 */
public class Neo4jNodeComparator  implements Comparator{
    
    public Neo4jNodeComparator(){
        
    }
    
    public int compare(Object o,Object n) {
        
        Node o1 = (Node) o;
        Node n1 = (Node) n;
        if(o1==null || n1 ==null){
            return -1;
        }
        
        long o1Neo4jId = -1;
        long n1Neo4jId = -1;
        
        if(o1.hasProperty(TSVExportsImports.PropertyKey_Neo4j_Id)){
            Object val = o1.getProperty(TSVExportsImports.PropertyKey_Neo4j_Id);
            if(val instanceof Integer){
                o1Neo4jId = (int) val;
            }
            else{
                o1Neo4jId = (long) val;
            }
        }
        
        if(n1.hasProperty(TSVExportsImports.PropertyKey_Neo4j_Id)){
            Object val = n1.getProperty(TSVExportsImports.PropertyKey_Neo4j_Id);
            if(val instanceof Integer){
                n1Neo4jId = (int) val;
            }
            else{
                n1Neo4jId = (long) val;
            }
        }
        if(o1Neo4jId > n1Neo4jId){
            return 1;
        }
        else if(o1Neo4jId < n1Neo4jId){
            return -1;
        }
        return 0;
    }
}
