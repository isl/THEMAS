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
package DB_Admin;

import DB_Classes.DBGeneral;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import neo4j_sisapi.Configs;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;

/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class CsvExports {
    
    
    public boolean globalCsvExportToFolder(String folderPath, neo4j_sisapi.Utilities.CsvExportMode csvMode){
     
        DBGeneral dbGen = new DBGeneral();
        
        QClass Q = new QClass();
        IntegerObject sis_session = new IntegerObject();
        neo4j_sisapi.Utilities apiUtils = new neo4j_sisapi.Utilities();
        
        Map<Configs.Labels, ArrayList<Long>> labelIds = new HashMap();
        Map<Configs.Rels, Map<Long, ArrayList<Long>>> relIds = new HashMap();
        
        Map<Configs.Attributes, Map<Long, ArrayList<Object>>> attrVals = new HashMap();
        
        
        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)==QClass.APIFail)
        {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class " + CsvExports.class.getName());
            return false;
        }

        
        for(Configs.Labels lbl : Configs.Labels.values()){
            ArrayList<Long> lblIds = apiUtils.csv_export_GetLabelIds(Q,lbl, csvMode);
            labelIds.put(lbl, lblIds);
        }
        
        
        for(Configs.Rels rel : Configs.Rels.values()){
            Map<Long, ArrayList<Long>> rIds = apiUtils.csv_export_GetRelIds(Q, rel, csvMode);
            relIds.put(rel, rIds);
        }
        
        for(Configs.Attributes attr : Configs.Attributes.values()){
            if(attr.equals(Configs.Attributes.Neo4j_Id)){
                continue;
            }
            Map<Long, ArrayList<Object>> targetAttrVals = apiUtils.csv_export_GetProperty(Q,attr, csvMode);
            attrVals.put(attr, targetAttrVals);
        }


        //end query and close connection
        Q.free_all_sets();
        Q.TEST_end_query();
        dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        
        String exportBaseName = "";
        switch(csvMode){
            case ALL:{
                exportBaseName = "CsvData";
                break;
            }
            case ONLY_GENERIC:{
                exportBaseName = Configs.Labels.Generic.name();
                break;
            }
            case ONLY_DATA:{
                exportBaseName = "SpecificData";
                break;
            }
            default:{
                break;
            }
        }
        
        
        if(exportBaseName.isEmpty()){
            return false;
        }
        
        for(Map.Entry<Configs.Labels, ArrayList<Long>> x : labelIds.entrySet()){
            if(x.getValue().isEmpty()){
                continue;
            }
            
            String fName = exportBaseName+"_label_"+x.getKey().name()+".csv";
            try{
                OutputStreamWriter out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(folderPath+fName)), "UTF-8");
                
                out.append(Configs.Attributes.Neo4j_Id.name()+System.lineSeparator());
                for(Long l : x.getValue()){
                    out.append(l+System.lineSeparator());
                }
                out.flush();
                out.close();
            } catch(Exception ex){
                Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                Utils.StaticClass.handleException(ex);

                return false;
            }           
        }
        
        
         for(Map.Entry<Configs.Rels, Map<Long, ArrayList<Long>>> y : relIds.entrySet()){
            if(y.getValue().isEmpty()){
                continue;
            }
            
            String fName = exportBaseName+"_rel_"+y.getKey().name()+".csv";
            try{
                OutputStreamWriter out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(folderPath+fName)), "UTF-8");
                
                out.append(Configs.csvExportFromNodeLabel+","+Configs.csvExportToNodeLabel+System.lineSeparator());
                for(Map.Entry<Long, ArrayList<Long>> l : y.getValue().entrySet()){
                    for(Long toVal : l.getValue()){
                        out.append(l.getKey()+","+toVal+System.lineSeparator());
                    }                    
                }
                out.flush();
                out.close();
            } catch(Exception ex){
                Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                Utils.StaticClass.handleException(ex);

                return false;
            }           
        }
        
        for(Map.Entry<Configs.Attributes, Map<Long, ArrayList<Object>>> z : attrVals.entrySet()){
            if(z.getValue().isEmpty()){
                continue;
            }
            if(z.getKey().equals(Configs.Attributes.Value)){
                ArrayList<String> supportedTypes = new ArrayList();
                supportedTypes.add("INT");
                supportedTypes.add("LONG");
                supportedTypes.add("STR");
                for(String type : supportedTypes){
                    
                    ArrayList<Object> flatList = new ArrayList();
                    z.getValue().values().forEach(x -> flatList.addAll(x));
                    
                    if(type.equals("STR") && !flatList.stream().anyMatch(x -> x instanceof String)){
                        continue;
                    }
                    if(type.equals("INT") && !flatList.stream().anyMatch(x -> x instanceof Integer)){
                        continue;
                    }
                    if(type.equals("LONG") && !flatList.stream().anyMatch(x -> x instanceof Long)){
                        continue;
                    }
                    String fName = exportBaseName+"_prop_"+z.getKey().name()+"_"+type+"_.csv";
                    try{
                        OutputStreamWriter out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(folderPath+fName)), "UTF-8");

                        out.append(Configs.Attributes.Neo4j_Id.name()+","+z.getKey().name()+System.lineSeparator());
                        for(Map.Entry<Long, ArrayList<Object>> l : z.getValue().entrySet()){
                            for(Object toVal : l.getValue()){
                                if(type.equals("STR") && toVal instanceof String){
                                    out.append(l.getKey()+",\""+toVal.toString().replace("\"", "\\\"")+"\""+System.lineSeparator());
                                }
                                else if(type.equals("INT") && toVal instanceof Integer){
                                    out.append(l.getKey()+","+Integer.parseInt(toVal.toString())+System.lineSeparator());
                                }
                                else if(type.equals("LONG") && toVal instanceof Long){
                                    out.append(l.getKey()+","+Long.parseLong(toVal.toString())+System.lineSeparator());
                                }                        
                            }                    
                        }
                        out.flush();
                        out.close();
                    } catch(Exception ex){
                        Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                        Utils.StaticClass.handleException(ex);

                        return false;
                    }       
                }
            }
            else{
                String fName = exportBaseName+"_prop_"+z.getKey().name()+".csv";
                try{
                    OutputStreamWriter out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(folderPath+fName)), "UTF-8");

                    out.append(Configs.Attributes.Neo4j_Id.name()+","+z.getKey().name()+System.lineSeparator());
                    for(Map.Entry<Long, ArrayList<Object>> l : z.getValue().entrySet()){
                        for(Object toVal : l.getValue()){
                            if(toVal instanceof String){
                                out.append(l.getKey()+",\""+toVal.toString().replace("\"", "\\\"")+"\""+System.lineSeparator());
                            }
                            else if(toVal instanceof Integer){
                                out.append(l.getKey()+","+Integer.parseInt(toVal.toString())+System.lineSeparator());
                            }
                            else if(toVal instanceof Long){
                                out.append(l.getKey()+","+Long.parseLong(toVal.toString())+System.lineSeparator());
                            }                        
                        }                    
                    }
                    out.flush();
                    out.close();
                } catch(Exception ex){
                    Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                    Utils.StaticClass.handleException(ex);

                    return false;
                }        
            }
            //(z.getKey().equals(Configs.AttributesOfTypeString.Value)? "_STR_":"")
               
        }
        
        
        return true;
    }
        
    
    
}
