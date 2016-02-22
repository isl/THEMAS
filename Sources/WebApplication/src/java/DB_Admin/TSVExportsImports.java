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
package DB_Admin;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class TSVExportsImports {
    private String GenericLabel = "Generic";
    private String CommonLabel = "Common";
    
    private String LabelKey = "LABEL";
    private String PropertyKey_Type = "Type";
    private String PropertyKey_Value = "Value";
    public static String PropertyKey_Neo4j_Id = "Neo4j_Id";
    private String PropertyKey_Logicalname = "Logicalname";
    
    private String MaxNeoIdNodeLogicalName = "Telos_Object";
    private String PropertyKeyMaxNeo4jId = "MaxNeo4j_Id";
    
    enum Rels implements RelationshipType {

        RELATION, ISA, INSTANCEOF
    }
    enum Labels implements Label {

        Type_Attribute, Type_Individual, M1_Class, M2_Class, M3_Class, M4_Class, S_Class, Token, PrimitiveClass, Common, Generic
    }

    private void writeNodeInfoInTsvFile(Node n, OutputStreamWriter out, boolean onlyGeneric, boolean skipGeneric) throws IOException{
     
        String nodeId = n.getProperty(PropertyKey_Neo4j_Id).toString();
        //long nodeId = n.getId();
        String logicalname = n.getProperty(PropertyKey_Logicalname).toString();
        if(n.hasLabel(Labels.Generic)){
            if(n.hasProperty(PropertyKey_Neo4j_Id)){
                out.append(nodeId+"\t"+PropertyKey_Neo4j_Id+"\t"+n.getProperty(PropertyKey_Neo4j_Id).toString()+"\r\n");
            }
        }
        else{
            if(onlyGeneric){
                return;
            }
            else{
                if(n.hasProperty(PropertyKey_Neo4j_Id)){
                    out.append(nodeId+"\t"+PropertyKey_Neo4j_Id+"\t"+n.getProperty(PropertyKey_Neo4j_Id).toString()+"\r\n");
                }
            }
        }
        if(n.hasProperty(PropertyKey_Logicalname)){
            out.append(nodeId+"\t"+PropertyKey_Logicalname+"\t"+n.getProperty(PropertyKey_Logicalname).toString()+"\r\n");
        }
        if(n.hasProperty(PropertyKey_Type)){
            out.append(nodeId+"\t"+PropertyKey_Type+"\t"+n.getProperty(PropertyKey_Type).toString()+"\r\n");
        }
        if(n.hasProperty(PropertyKey_Value)){
            out.append(nodeId+"\t"+PropertyKey_Value+"\t"+n.getProperty(PropertyKey_Value).toString().replace("\r\n", " ").replace("\n", " ").replace("\r", " ").replace("\t", " ")+"\r\n");
        }
        Vector<String> labelLines = new Vector<String>();
        for(Label lbl : n.getLabels()){
            labelLines.add(nodeId+"\t"+LabelKey+"\t"+lbl.name()+"\r\n");
        }
        Collections.sort(labelLines);
        for(String str: labelLines){
            out.append(str);
        }
        
        if(n.hasRelationship(Rels.INSTANCEOF, Direction.OUTGOING)){
            Vector<String> outputLines = new Vector<String>();
            for(Relationship rel: n.getRelationships(Rels.INSTANCEOF, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(PropertyKey_Neo4j_Id).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(PropertyKey_Logicalname).toString();
                outputLines.add(nodeId+"\t"+Rels.INSTANCEOF.name()+"\t"+endNodeId+"\t\t<!-- "+logicalname+"\t"+Rels.INSTANCEOF.name()+"\t"+endlogicalname+" -->\r\n");
            }
            
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(skipGeneric){
            //get ingoing relationships that might be lost 
            //generic pointing to non generic
            Vector<String> outputLines = new Vector<String>();
            if(n.hasRelationship(Rels.INSTANCEOF, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.INSTANCEOF, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(PropertyKey_Neo4j_Id).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(PropertyKey_Logicalname).toString();
                    outputLines.add(genericStartNodeId+"\t"+Rels.INSTANCEOF.name()+"\t"+nodeId+"\t\t<!-- "+genericStartNodelogicalname+"\t"+Rels.INSTANCEOF.name()+"\t"+logicalname+" -->\r\n");
                }
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(n.hasRelationship(Rels.ISA, Direction.OUTGOING)){
            Vector<String> outputLines = new Vector<String>();
            for(Relationship rel: n.getRelationships(Rels.ISA, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(PropertyKey_Neo4j_Id).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(PropertyKey_Logicalname).toString();
                outputLines.add(nodeId+"\t"+Rels.ISA.name()+"\t"+endNodeId+"\t\t<!-- "+logicalname+"\t"+Rels.ISA.name()+"\t"+endlogicalname+" -->\r\n");
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(skipGeneric){
            //get ingoing relationships that might be lost 
            //generic pointing to non generic
            Vector<String> outputLines = new Vector<String>();
            if(n.hasRelationship(Rels.ISA, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.ISA, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(PropertyKey_Neo4j_Id).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(PropertyKey_Logicalname).toString();
                     outputLines.add(genericStartNodeId+"\t"+Rels.ISA.name()+"\t"+nodeId+"\t\t<!-- "+genericStartNodelogicalname+"\t"+Rels.ISA.name()+"\t"+logicalname+" -->\r\n");
                }
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(n.hasRelationship(Rels.RELATION, Direction.OUTGOING)){
            Vector<String> outputLines = new Vector<String>();
            for(Relationship rel: n.getRelationships(Rels.RELATION, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(PropertyKey_Neo4j_Id).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(PropertyKey_Logicalname).toString();
                outputLines.add(nodeId+"\t"+Rels.RELATION.name()+"\t"+endNodeId+"\t\t<!-- "+logicalname+"\t"+Rels.RELATION.name()+"\t"+endlogicalname+" -->\r\n");
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(skipGeneric){
            //get ingoing relationships that might be lost 
            //generic pointing to non generic
            Vector<String> outputLines = new Vector<String>();
             if(n.hasRelationship(Rels.RELATION, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.RELATION, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(PropertyKey_Neo4j_Id).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(PropertyKey_Logicalname).toString();
                    outputLines.add(genericStartNodeId+"\t"+Rels.RELATION.name()+"\t"+nodeId+"\t\t<!-- "+genericStartNodelogicalname+"\t"+Rels.RELATION.name()+"\t"+logicalname+" -->\r\n");
                }
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        out.flush();
        
    }
    
    
    public boolean globalExportToFile(String filepath, boolean onlyGeneric, boolean skipGeneric){
     
        GraphDatabaseService graphDb = Utils.StaticClass.getDBService();
        Transaction tx = graphDb.beginTx();
        try{
        
            OutputStreamWriter out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(filepath)), "UTF-8");
            
            Vector<Node> nodesToExport = new Vector<Node>();
            
            String query = "";
            if(onlyGeneric){
                query="Match (n:"+GenericLabel+") return n";
            }
            else{
                if(skipGeneric){
                    query="Match (n) WHERE NOT(\""+ GenericLabel+"\" in labels(n)) return n";
                }
                else{
                    query="Match (n) return n";
                }
            }
            
            Result res = graphDb.execute(query);
            while (res.hasNext()) {
                Node n = (Node) res.next().get("n");
                nodesToExport.add(n);
            }
            res.close();
            res = null;
        
            //out.append(Utils.ConstantParameters.copyrightAndLicenseForPropertiesFile);
            Collections.sort(nodesToExport, new Utils.Neo4jNodeComparator());
            for(Node n : nodesToExport){
                writeNodeInfoInTsvFile(n,out,onlyGeneric,skipGeneric);
            }
            tx.success();
            
            out.flush();
            
        }catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            if(tx!=null){
                tx.failure();
            }
            return false;
        }
        finally{
            tx.close();
        }
        
        Utils.StaticClass.closeDb();
        
        return true;
    }
    
    public boolean importSpecificFromFile(String filepath){
        try{
        
            Hashtable<Long, Hashtable<String, Vector<String>>> nodeInfo = new Hashtable<Long,Hashtable<String, Vector<String>>>();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF8"));
            String linestr;
            int line = 0;
            while ((linestr = in.readLine()) != null) {
                line++;
                if(linestr.trim().length()==0){
                    Utils.StaticClass.webAppSystemOutPrintln("Empty line encoutered at line " + line+". Just skipping and continuing");
                    continue;
                }
                if(linestr.trim().startsWith("#")){
                    Utils.StaticClass.webAppSystemOutPrintln("Skipping comment line: " + line);
                    continue;
                }
                String[] parts = linestr.split("\t");
                if(parts.length>=3){
                    long internalId = Long.parseLong(parts[0]);
                    String property = parts[1];
                    String valueStr = parts[2];
                    
                    if(nodeInfo.containsKey(internalId)==false){
                        nodeInfo.put(internalId,new Hashtable<String, Vector<String>>());
                    }
                    
                    //cast everything as String except multivalued elements - Labels and relationships
                    if(nodeInfo.get(internalId).containsKey(property)==false){
                        nodeInfo.get(internalId).put(property, new Vector<String>());
                    }
                    nodeInfo.get(internalId).get(property).add(valueStr);
                    
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Non empty line found that contains less than 3 tab seperated values at line: " + line+". Aborting import procedure");
                    return false;
                }
            }//parsing ended
            
            
            Hashtable<Long,Long> tsvToNeo4jIds = new Hashtable<Long,Long>();
            GraphDatabaseService graphDb = Utils.StaticClass.getDBService();
            
            long maxGenericNeo4jId = -1;
            
            long maxNeo4jId =-1;
            
            
            
            
            
            try(Transaction tx = graphDb.beginTx()){
                String query = "MATCH(n:"+GenericLabel+") return max (n."+PropertyKey_Neo4j_Id+") as newVal " ;
                Result res = graphDb.execute(query);

                try{
                    while(res.hasNext()){
                        Object val = res.next().get("newVal");
                        if(val instanceof Integer){
                            maxGenericNeo4jId = (int) val;
                        }
                        else{
                            maxGenericNeo4jId = (long) val;
                        }
                    }
                }
                finally{
                    res.close();
                    res = null;
                }
                
                String query1 = "MATCH(n:"+CommonLabel+") return max (n."+PropertyKey_Neo4j_Id+") as newVal " ;
                Result res1 = graphDb.execute(query1);

                try{
                    while(res1.hasNext()){
                        Object val = res1.next().get("newVal");
                        if(val instanceof Integer){
                            maxNeo4jId = (int) val;
                        }
                        else{
                            maxNeo4jId = (long) val;
                        }
                    }
                }
                finally{
                    res1.close();
                    res1 = null;
                }
                tx.success();
            }
            
            if(maxGenericNeo4jId<=0){
                Utils.StaticClass.webAppSystemOutPrintln("Could not retrieve max generic neo4j id.");
                return false;
            }
            if(maxNeo4jId<=0){
                Utils.StaticClass.webAppSystemOutPrintln("Could not retrieve max neo4j id.");
                return false;
            }
            
            
            for(long i=1; i<=maxGenericNeo4jId; i++){
                tsvToNeo4jIds.put(i, i);
            }
            if(maxNeo4jId <=1000){
                maxNeo4jId=1000;
            }
            maxNeo4jId ++;
            
            try(Transaction tx =  graphDb.beginTx()){
                    
                
                //create nodes
                Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
                while(nodeIdentifiers.hasMoreElements()){
                    long nodeId = nodeIdentifiers.nextElement();
                    //long startNodeNeo4jId = tsvToNeo4jIds.get(nodeId);
                    if(nodeId<=maxGenericNeo4jId){
                        continue;
                    }
                    Hashtable<String,Vector<String>> strVals = nodeInfo.get(nodeId);

                    

                    String logicalName = "";
                    String type = "";
                    String value = "";
                    long neo4jId = -1;
                    
                    Vector<String> labels = new Vector<String>();
                    if(strVals.containsKey(LabelKey)){
                        labels.addAll(strVals.get(LabelKey));                    
                    }

                    
                   
                    Node newNode = graphDb.createNode();
                    for(String lbl: labels){
                        Label label = DynamicLabel.label(lbl);
                        newNode.addLabel(label);
                    }
                    
                    neo4jId = maxNeo4jId++;
                    newNode.setProperty(PropertyKey_Neo4j_Id, neo4jId);
                    tsvToNeo4jIds.put(nodeId, neo4jId);

                    
                    
                    newNode.setProperty(PropertyKey_Logicalname, strVals.get(PropertyKey_Logicalname).get(0));
                    
                    if(strVals.containsKey(PropertyKey_Type)){
                        newNode.setProperty(PropertyKey_Type, strVals.get(PropertyKey_Type).get(0));
                    }
                    if(strVals.containsKey(PropertyKey_Value)){
                        if(strVals.get(PropertyKey_Type).get(0).equals("INT")){
                            newNode.setProperty(PropertyKey_Value, Integer.parseInt(strVals.get(PropertyKey_Value).get(0)));
                        }
                        else{
                            newNode.setProperty(PropertyKey_Value, strVals.get(PropertyKey_Value).get(0));
                        }
                    }

                }
                tx.success();
            }
            
            //create InstanceOf, ISA, Relation relationships
            try(Transaction tx =  graphDb.beginTx()){
                Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
                while(nodeIdentifiers.hasMoreElements()){
                    long nodeId = nodeIdentifiers.nextElement();
                    long startNodeNeo4jId = tsvToNeo4jIds.get(nodeId);
                    Hashtable<String,Vector<String>> strVals = nodeInfo.get(nodeId);
                    
                    Vector<Long> isA = new Vector<Long>();
                    Vector<Long> relation = new Vector<Long>();
                    Vector<Long> instanceOf = new Vector<Long>();
                    if(strVals.containsKey(Rels.ISA.name())){
                        Vector<String> stringVals = strVals.get(Rels.ISA.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            isA.add(tsvToNeo4jIds.get(endNodeId));
                        }
                    }
                    if(strVals.containsKey(Rels.RELATION.name())){
                        Vector<String> stringVals = strVals.get(Rels.RELATION.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            relation.add(tsvToNeo4jIds.get(endNodeId));
                        }
                    }
                    if(strVals.containsKey(Rels.INSTANCEOF.name())){
                        Vector<String> stringVals = strVals.get(Rels.INSTANCEOF.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            instanceOf.add(tsvToNeo4jIds.get(endNodeId));
                        }
                    }
                    
                    if(isA.size()==0 && instanceOf.size()==0 && relation.size()==0){
                        continue;
                    }
                    
                    Node startNode = getSingleNodesByNeo4jId(startNodeNeo4jId,graphDb);
                    
                    if(isA.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(isA, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.ISA);
                        }
                    }
                    if(instanceOf.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(instanceOf, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.INSTANCEOF);
                        }
                    }
                    if(relation.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(relation, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.RELATION);
                        }
                    }
                }
                tx.success();
            }
            
            
            try(Transaction tx = graphDb.beginTx()){
                String query3 = "MATCH(n:"+CommonLabel+") with max (n."+PropertyKey_Neo4j_Id+") as newVal " +
                        "MATCH(t:"+CommonLabel+"{"+PropertyKey_Logicalname+":\""+MaxNeoIdNodeLogicalName+"\"}) " +
                        "SET t."+PropertyKeyMaxNeo4jId+" = newVal " +
                        "return t."+PropertyKeyMaxNeo4jId+" as "+ PropertyKeyMaxNeo4jId;
                Result res3 = graphDb.execute(query3);

                if (res3 == null) {
                    Utils.StaticClass.webAppSystemOutPrintln("Set Max Neo4j Id Failed.");
                    return false;
                }
                res3.close();
                res3=null;
                tx.success();
            }
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;        
    }
    
    public boolean importGenericFromFile(String filepath){
        
        return globalImportFromFile(filepath);
    }
    
    boolean globalImportFromFile(String filepath){
        try{
            Hashtable<Long, Hashtable<String, Vector<String>>> nodeInfo = new Hashtable<Long,Hashtable<String, Vector<String>>>();
            
            //Vector<String> multivaluedElements = new Vector<String>();
            //multivaluedElements.add(LabelKey);
            //multivaluedElements.add(Rels.INSTANCEOF.name());
            //multivaluedElements.add(Rels.ISA.name());
            //multivaluedElements.add(Rels.RELATION.name());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF8"));
            String linestr;
            int line = 0;
            while ((linestr = in.readLine()) != null) {
                line++;
                if(linestr.trim().length()==0){
                    Utils.StaticClass.webAppSystemOutPrintln("Empty line encoutered at line " + line+". Just skipping and continuing");
                    continue;
                }
                if(linestr.trim().startsWith("#")){
                    Utils.StaticClass.webAppSystemOutPrintln("Skipping comment line: " + line);
                    continue;
                }
                
                String[] parts = linestr.split("\t");
                if(parts.length>=3){
                    long internalId = Long.parseLong(parts[0]);
                    String property = parts[1];
                    String valueStr = parts[2];
                    
                    if(nodeInfo.containsKey(internalId)==false){
                        nodeInfo.put(internalId,new Hashtable<String, Vector<String>>());
                    }
                    
                    //cast everything as String except multivalued elements - Labels and relationships
                    if(nodeInfo.get(internalId).containsKey(property)==false){
                        nodeInfo.get(internalId).put(property, new Vector<String>());
                    }
                    nodeInfo.get(internalId).get(property).add(valueStr);
                    
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Non empty line found that contains less than 3 tab seperated values at line: " + line+". Aborting import procedure");
                    return false;
                }
            }//parsing ended
            
            
            //Hashtable<Long,Long> tsvToNeo4jIds = new Hashtable<Long,Long>();
            
            //long maxGenericNeo4jId = 0;
            /*
            Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
            while(nodeIdentifiers.hasMoreElements()){
                long nodeId = nodeIdentifiers.nextElement();
                Hashtable<String,Vector<String>> strVals = nodeInfo.get(nodeId);
                if(strVals.containsKey(LabelKey) && strVals.get(LabelKey).contains(GenericLabel)){
                    long neo4jId = Long.parseLong(strVals.get(PropertyKey_Neo4j_Id).get(0));
                    if(neo4jId>maxGenericNeo4jId){
                        maxGenericNeo4jId = neo4jId;
                    }
                }
            }*/
            //if(maxGenericNeo4jId <=1000){
              //  maxGenericNeo4jId=1000;
            //}
            //maxGenericNeo4jId ++;
            //maxGenericNeo4jId++;
            
            GraphDatabaseService graphDb = Utils.StaticClass.getDBService();
            try(Transaction tx =  graphDb.beginTx()){
                
                //create nodes
                Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
                while(nodeIdentifiers.hasMoreElements()){
                    long nodeId = nodeIdentifiers.nextElement();
                    Hashtable<String,Vector<String>> strVals = nodeInfo.get(nodeId);


                    String logicalName = "";
                    String type = "";
                    String value = "";
                    long neo4jId = -1;
                    
                    Vector<String> labels = new Vector<String>();
                    if(strVals.containsKey(LabelKey)){
                        labels.addAll(strVals.get(LabelKey));                    
                    }

                    
                   
                    Node newNode = graphDb.createNode();
                    for(String lbl: labels){
                        Label label = DynamicLabel.label(lbl);
                        newNode.addLabel(label);
                    }
                    
                    //if(labels.contains(GenericLabel)){
                        neo4jId = Long.parseLong(strVals.get(PropertyKey_Neo4j_Id).get(0));
                        newNode.setProperty(PropertyKey_Neo4j_Id, neo4jId);
                      //  tsvToNeo4jIds.put(nodeId, neo4jId);
                    //}
                    //else{
                      //  neo4jId = maxGenericNeo4jId++;
                        //newNode.setProperty(PropertyKey_Neo4j_Id, neo4jId);
                        //tsvToNeo4jIds.put(nodeId, neo4jId);
                    //}
                    
                    
                    newNode.setProperty(PropertyKey_Logicalname, strVals.get(PropertyKey_Logicalname).get(0));
                    
                    if(strVals.containsKey(PropertyKey_Type)){
                        newNode.setProperty(PropertyKey_Type, strVals.get(PropertyKey_Type).get(0));
                    }
                    if(strVals.containsKey(PropertyKey_Value)){
                        if(strVals.get(PropertyKey_Type).get(0).equals("INT")){
                            newNode.setProperty(PropertyKey_Value, Integer.parseInt(strVals.get(PropertyKey_Value).get(0)));
                        }
                        else{
                            newNode.setProperty(PropertyKey_Value, strVals.get(PropertyKey_Value).get(0));
                        }
                    }

                }
                tx.success();
            }

                        
            try(Transaction tx =  graphDb.beginTx()){
                //create the indexes
                if(CreateIndexesAndConstraints(graphDb)==false){
                    return false;
                }
                tx.success();
            }
            
            try(Transaction tx = graphDb.beginTx()){
                String query3 = "MATCH(n:"+CommonLabel+") with max (n."+PropertyKey_Neo4j_Id+") as newVal " +
                        "MATCH(t:"+CommonLabel+"{"+PropertyKey_Logicalname+":\""+MaxNeoIdNodeLogicalName+"\"}) " +
                        "SET t."+PropertyKeyMaxNeo4jId+" = newVal " +
                        "return t."+PropertyKeyMaxNeo4jId+" as "+ PropertyKeyMaxNeo4jId;
                Result res3 = graphDb.execute(query3);

                if (res3 == null) {
                    Utils.StaticClass.webAppSystemOutPrintln("Set Max Neo4j Id Failed.");
                    return false;
                }
                res3.close();
                res3=null;
                tx.success();
            }

            Utils.StaticClass.closeDb();
            graphDb = Utils.StaticClass.getDBService();
            
            //create InstanceOf, ISA, Relation relationships
            try(Transaction tx =  graphDb.beginTx()){
                Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
                while(nodeIdentifiers.hasMoreElements()){
                    long nodeId = nodeIdentifiers.nextElement();
                    long startNodeNeo4jId = nodeId;//tsvToNeo4jIds.get(nodeId);
                    Hashtable<String,Vector<String>> strVals = nodeInfo.get(nodeId);
                    
                    Vector<Long> isA = new Vector<Long>();
                    Vector<Long> relation = new Vector<Long>();
                    Vector<Long> instanceOf = new Vector<Long>();
                    if(strVals.containsKey(Rels.ISA.name())){
                        Vector<String> stringVals = strVals.get(Rels.ISA.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            isA.add(endNodeId/*tsvToNeo4jIds.get(endNodeId)*/);
                        }
                    }
                    if(strVals.containsKey(Rels.RELATION.name())){
                        Vector<String> stringVals = strVals.get(Rels.RELATION.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            relation.add(endNodeId/*tsvToNeo4jIds.get(endNodeId)*/);
                        }
                    }
                    if(strVals.containsKey(Rels.INSTANCEOF.name())){
                        Vector<String> stringVals = strVals.get(Rels.INSTANCEOF.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            instanceOf.add(endNodeId/*tsvToNeo4jIds.get(endNodeId)*/);
                        }
                    }
                    
                    if(isA.size()==0 && instanceOf.size()==0 && relation.size()==0){
                        continue;
                    }
                    
                    Node startNode = getSingleNodesByNeo4jId(startNodeNeo4jId,graphDb);
                    
                    if(isA.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(isA, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.ISA);
                        }
                    }
                    if(instanceOf.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(instanceOf, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.INSTANCEOF);
                        }
                    }
                    if(relation.size()>0){
                        Vector<Node> toNodes = getNodesByNeo4jIds(relation, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.RELATION);
                        }
                    }
                }
                tx.success();
            }
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
    }
    
    Vector<Long> collectSequenctiallyAsubsetOfValues(int startindex,int howmanyToGet, Vector<Long> targetVals){
        Vector<Long> returnVals = new Vector<Long>();
        if(howmanyToGet<=0){
            throw new UnsupportedOperationException("collectSequenctiallyAsubsetOfValues was called with howmanyToGet: " +howmanyToGet);
        }
        int maxIndex =targetVals.size(); 
        if(startindex<maxIndex){
            for(int i = 0; i< howmanyToGet; i++){
                
                if((startindex+i)>=maxIndex){
                    break;
                }
                else{
                    returnVals.add(targetVals.get(i+startindex));
                }
            }
        }        
        return returnVals;
    }
    Node getSingleNodesByNeo4jId(long neo4jId, GraphDatabaseService graphDb){
        Node returnNode = null;
        
        String query = "MATCH(n:"+CommonLabel+"{"+PropertyKey_Neo4j_Id+":"+neo4jId+"}) RETURN n";
        Result res = graphDb.execute(query);
        try {
            while (res.hasNext()) {
                
                Map<String, Object> row = res.next();
                returnNode = (Node) row.get("n");
                return returnNode;
            }
        } 
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass()+"\t"+ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return null;
        }
        finally {
            res.close();
            res = null;
        }
        
        
        return returnNode;
    }
    
    Vector<Node> getNodesByNeo4jIds(Vector<Long> neo4jIds, GraphDatabaseService graphDb){
        Vector<Node> returnVec = new Vector<Node>();
        
        int loopIndex = 0;
        int maxIndex = neo4jIds.size();

        if(maxIndex==0){
            return returnVec;
        }
        
        while (loopIndex < maxIndex) {

            Vector<Long> subSetofIds = collectSequenctiallyAsubsetOfValues(loopIndex,500, neo4jIds);
            loopIndex += subSetofIds.size();
            if(subSetofIds.size()==0){
                break;
            }
            String query = "";
            
            if(subSetofIds.size()==1){
                query = "MATCH(n:"+CommonLabel+"{"+PropertyKey_Neo4j_Id+":"+subSetofIds.get(0)+"}) RETURN n";
                
            }
            else{
                query = " Match (n:"+CommonLabel+") WHERE n."+PropertyKey_Neo4j_Id+" IN " + subSetofIds.toString() + " "+
                        " RETURN n ";
            }
            
            Result res = graphDb.execute(query);
            try {
                while (res.hasNext()) {

                    Map<String, Object> row = res.next();
                    returnVec.add((Node) row.get("n"));
                }
            } 
            catch(Exception ex){
                Utils.StaticClass.webAppSystemOutPrintln(ex.getClass()+"\t"+ex.getMessage());
                Utils.StaticClass.handleException(ex);
                return null;
            }
            finally {
                res.close();
                res = null;
            }
        }
        
        
        return returnVec;
    }
    
    boolean CreateIndexesAndConstraints(GraphDatabaseService graphDb) {

        String query = "CREATE INDEX ON :"+CommonLabel+"("+PropertyKey_Logicalname+") ";
        
        Result res = graphDb.execute(query);

        if (res == null) {
            Utils.StaticClass.webAppSystemOutPrintln("Creation of Indexes Failed.");
            return false;
        }
        res.close();
        res = null;

        String query2 = "CREATE CONSTRAINT ON (n:"+CommonLabel+") ASSERT n."+PropertyKey_Neo4j_Id+" IS UNIQUE ";

        Result res2 = graphDb.execute(query2);

        if (res2 == null) {
            Utils.StaticClass.webAppSystemOutPrintln("Creation Constraints Failed.");
            return false;
        }
        res2.close();
        res2=null;

        
        
        Utils.StaticClass.webAppSystemOutPrintln("\nFinished Creation of Indexes and Constraints.\n");
        return true;
    }
}
