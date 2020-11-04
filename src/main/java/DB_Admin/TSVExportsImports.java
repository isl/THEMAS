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
import Utils.Utilities;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import neo4j_sisapi.Configs;
import neo4j_sisapi.Configs.Attributes;
import neo4j_sisapi.Configs.Labels;
import neo4j_sisapi.Configs.Rels;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.TMSAPIClass;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class TSVExportsImports {
    //private String GenericLabel = "Generic";
    //private String CommonLabel = "Common";
    
    private String LabelKey = "LABEL";
    
    //private String Neo4j_Key_For_Type = "Type";
    //private String Neo4j_Key_For_Value = "Value";
    //public static String Neo4j_Key_For_Neo4j_Id = "Neo4j_Id";
    
    //private String Neo4j_Key_For_Logicalname = "Logicalname";
    //private String Neo4j_Key_For_Transliteration = "Transliteration";
    //private String Neo4j_Key_For_ReferenceId = "ReferenceId";
    
    
    //private String Neo4j_Key_For_MaxThesaurusReferenceId = "MaxThesaurusReferenceId";
    
    //private String Neo4j_Key_For_MaxThesaurusFacetId = "MaxThesaurusFacetId";
    //private String Neo4j_Key_For_MaxThesaurusHierarchyId = "MaxThesaurusHierarchyId";
    //private String Neo4j_Key_For_MaxThesaurusTermId = "MaxThesaurusTermId";
    //private String Neo4j_Key_For_MaxSourceId = "MaxSourceId";
    
    //private String Neo4j_Node_LogicalName_For_MaxNeo4jId = "Telos_Object";
    //private String Neo4j_Node_LogicalName_For_MaxTHESFacetId = "%THES%Facet";
    //private String Neo4j_Node_LogicalName_For_MaxTHESHierarchyId = "%THES%Hierarchy";
    //private String Neo4j_Node_LogicalName_For_MaxTHESTermId = "%THES%HierarchyTerm";
    //private String Neo4j_Node_LogicalName_For_MaxSourceId = "Source";
        
    
    

    private void writeNodeInfoInTsvFile(Node n, OutputStreamWriter out, boolean onlyGeneric, boolean skipGeneric) throws IOException{
     
        String nodeId = n.getProperty(Attributes.Neo4j_Id.name()).toString();
        //long nodeId = n.getId();
        String logicalname = n.getProperty(Attributes.Logicalname.name()).toString();
        if(n.hasLabel(Labels.Generic)){
            if(n.hasProperty(Attributes.Neo4j_Id.name())){
                out.append(nodeId+"\t"+Attributes.Neo4j_Id.name()+"\t"+n.getProperty(Attributes.Neo4j_Id.name()).toString()+"\r\n");
            }
        }
        else{
            if(onlyGeneric){
                return;
            }
            else{
                if(n.hasProperty(Attributes.Neo4j_Id.name())){
                    out.append(nodeId+"\t"+Attributes.Neo4j_Id.name()+"\t"+n.getProperty(Attributes.Neo4j_Id.name()).toString()+"\r\n");
                }
            }
        }
        
        if(n.hasProperty(Attributes.Logicalname.name())){
            out.append(nodeId+"\t"+Attributes.Logicalname.name()+"\t"+n.getProperty(Attributes.Logicalname.name()).toString()+"\r\n");
        }        
        if(n.hasProperty(Attributes.Transliteration.name())){
            out.append(nodeId+"\t"+Attributes.Transliteration.name()+"\t"+n.getProperty(Attributes.Transliteration.name()).toString()+"\r\n");
        }
        if(n.hasProperty(Attributes.ThesaurusReferenceId.name())){
            out.append(nodeId+"\t"+Attributes.ThesaurusReferenceId.name()+"\t"+n.getProperty(Attributes.ThesaurusReferenceId.name()).toString()+"\r\n");
        }
        
        if(n.hasProperty(Attributes.MaxNeo4j_Id.name())){
            out.append(nodeId+"\t"+Attributes.MaxNeo4j_Id.name()+"\t"+n.getProperty(Attributes.MaxNeo4j_Id.name()).toString()+"\t\t<!-- Just exporting the data found. It will be calculated again during import -->\r\n");
        }
        if(n.hasProperty(neo4j_sisapi.Configs.Neo4j_Key_For_MaxThesaurusReferenceId)){
            out.append(nodeId+"\t"+neo4j_sisapi.Configs.Neo4j_Key_For_MaxThesaurusReferenceId+"\t"+n.getProperty(neo4j_sisapi.Configs.Neo4j_Key_For_MaxThesaurusReferenceId).toString()+"\t\t<!-- Just exporting the data found. It will be calculated again during import -->\r\n");
        }
        
        if(n.hasProperty(Attributes.Type.name())){
            out.append(nodeId+"\t"+Attributes.Type.name()+"\t"+n.getProperty(Attributes.Type.name()).toString()+"\r\n");
        }
        if(n.hasProperty(Attributes.Value.name())){
            out.append(nodeId+"\t"+Attributes.Value.name()+"\t"+n.getProperty(Attributes.Value.name()).toString().replace("\r\n", " ").replace("\n", " ").replace("\r", " ").replace("\t", " ")+"\r\n");
        }
        
        
        ArrayList<String> labelLines = new ArrayList<>();
        for(Label lbl : n.getLabels()){
            labelLines.add(nodeId+"\t"+LabelKey+"\t"+lbl.name()+"\r\n");
        }
        Collections.sort(labelLines);
        for(String str: labelLines){
            out.append(str);
        }
        
        if(n.hasRelationship(Rels.INSTANCEOF, Direction.OUTGOING)){
            ArrayList<String> outputLines = new ArrayList<>();
            for(Relationship rel: n.getRelationships(Rels.INSTANCEOF, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(Attributes.Logicalname.name()).toString();
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
            ArrayList<String> outputLines = new ArrayList<String>();
            if(n.hasRelationship(Rels.INSTANCEOF, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.INSTANCEOF, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(Attributes.Logicalname.name()).toString();
                    outputLines.add(genericStartNodeId+"\t"+Rels.INSTANCEOF.name()+"\t"+nodeId+"\t\t<!-- "+genericStartNodelogicalname+"\t"+Rels.INSTANCEOF.name()+"\t"+logicalname+" -->\r\n");
                }
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(n.hasRelationship(Rels.ISA, Direction.OUTGOING)){
            ArrayList<String> outputLines = new ArrayList<>();
            for(Relationship rel: n.getRelationships(Rels.ISA, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(Attributes.Logicalname.name()).toString();
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
            ArrayList<String> outputLines = new ArrayList();
            if(n.hasRelationship(Rels.ISA, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.ISA, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(Attributes.Logicalname.name()).toString();
                     outputLines.add(genericStartNodeId+"\t"+Rels.ISA.name()+"\t"+nodeId+"\t\t<!-- "+genericStartNodelogicalname+"\t"+Rels.ISA.name()+"\t"+logicalname+" -->\r\n");
                }
            }
            Collections.sort(outputLines);
            for(String str: outputLines){
                out.append(str);
            }
        }
        if(n.hasRelationship(Rels.RELATION, Direction.OUTGOING)){
            ArrayList<String> outputLines = new ArrayList<String>();
            for(Relationship rel: n.getRelationships(Rels.RELATION, Direction.OUTGOING)){
                //long endNodeId = rel.getEndNode().getId();
                String endNodeId = rel.getEndNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                if(onlyGeneric){
                    if(rel.getEndNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                }
                String endlogicalname = rel.getEndNode().getProperty(Attributes.Logicalname.name()).toString();
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
            ArrayList<String> outputLines = new ArrayList<String>();
             if(n.hasRelationship(Rels.RELATION, Direction.INCOMING)){
                for(Relationship rel: n.getRelationships(Rels.RELATION, Direction.INCOMING)){
                    //long genericStartNodeId = rel.getStartNode().getId();
                    String genericStartNodeId = rel.getStartNode().getProperty(Attributes.Neo4j_Id.name()).toString();
                    if(rel.getStartNode().hasLabel(Labels.Generic)==false){
                        continue;
                    }
                    String genericStartNodelogicalname = rel.getStartNode().getProperty(Attributes.Logicalname.name()).toString();
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
            
            ArrayList<Node> nodesToExport = new ArrayList<Node>();
            
            String query = "";
            if(onlyGeneric){
                query="Match (n:"+Configs.Labels.Generic.name()+") return n";
            }
            else{
                if(skipGeneric){
                    query="Match (n) WHERE NOT(\""+ Configs.Labels.Generic.name()+"\" in labels(n)) return n";
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
            
        } catch(Exception ex){
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
    
    public boolean importSpecificFromFile(String filepath, boolean recomputeTransliteration){
        try{//called from create thesaurus indexes should have been created
        
            HashMap<Long, HashMap<String, ArrayList<String>>> nodeInfo = new HashMap<Long,HashMap<String, ArrayList<String>>>();
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
                        nodeInfo.put(internalId,new HashMap<String, ArrayList<String>>());
                    }
                    
                    //cast everything as String except multivalued elements - Labels and relationships
                    if(nodeInfo.get(internalId).containsKey(property)==false){
                        nodeInfo.get(internalId).put(property, new ArrayList<String>());
                    }
                    nodeInfo.get(internalId).get(property).add(valueStr);
                    
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Non empty line found that contains less than 3 tab seperated values at line: " + line+". Aborting import procedure");
                    return false;
                }
            }//parsing ended
            
            
            HashMap<Long,Long> tsvToNeo4jIds = new HashMap<Long,Long>();
            GraphDatabaseService graphDb = Utils.StaticClass.getDBService();
            
            long maxGenericNeo4jId = -1;
            
            long maxNeo4jId =-1;
            
            
            
            
            
            try(Transaction tx = graphDb.beginTx()){
                String query = "MATCH(n:"+Configs.Labels.Generic.name()+") return max (n."+Attributes.Neo4j_Id.name()+") as newVal " ;
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
                
                String query1 = "MATCH(n:"+Configs.Labels.Common.name()+") return max (n."+Attributes.Neo4j_Id.name()+") as newVal " ;
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
            /*
            Delete relationships for all nodes that might have been defined before            
            
            
            //Not correct patch also deleting data from other thesauri also
            
            try(Transaction tx =  graphDb.beginTx()){
                
                Iterator<Long> deleteNodes = nodeInfo.keySet().iterator();
                while(deleteNodes.hasNext()){
                    long nodeId = deleteNodes.next();
                    if(nodeId<=maxGenericNeo4jId){
                        continue;
                    }
                    HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);
                    
                    String logName = strVals.get(Attributes.Logicalname.name()).get(0);
                    if(logName.length()>0){
                        
                        graphDb.findNodes(Label.label(Configs.CommonLabelName), Attributes.Logicalname.name(), logName).stream().forEach((n) -> {
                            if(n!=null){
                                System.out.println("deleting node: "+ logName);
                                n.getRelationships().forEach((rel)->{
                                rel.delete();
                            });

                            n.delete();
                        }
                        });
                    
                        
                    }
                    
                    
                }
                
                tx.success();
            }
            */
                    
            try(Transaction tx =  graphDb.beginTx()){
                //create nodes
                Iterator<Long> nodeIdentifiers = nodeInfo.keySet().iterator();
                while(nodeIdentifiers.hasNext()){
                    long nodeId = nodeIdentifiers.next();
                    //long startNodeNeo4jId = tsvToNeo4jIds.get(nodeId);
                    if(nodeId<=maxGenericNeo4jId){
                        continue;
                    }
                    HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);

                    

                    String logName = strVals.get(Attributes.Logicalname.name()).get(0);
                    String type = "";
                    String value = "";
                    long neo4jId = -1;
                    
                    ArrayList<String> labels = new ArrayList<String>();
                    if(strVals.containsKey(LabelKey)){
                        labels.addAll(strVals.get(LabelKey));                    
                    }
                    
                    Node newNode = null;
                    boolean alreadyExisted = false;
                    if(labels.contains(Configs.Labels.UniqueInDB.name())){
                        Optional<Node> existing = graphDb.findNodes(Label.label(Configs.Labels.Common.name()), Attributes.Logicalname.name(),logName).stream().findFirst();
                        if(existing.isPresent()){
                            alreadyExisted = true;
                            newNode = existing.get();
                        }                        
                    }
                    
                    if(!alreadyExisted){
                        newNode = graphDb.createNode();
                    }
                    //graphDb.createNode();
                    for(String lbl: labels){
                        Label label = Label.label(lbl);
                        if(!alreadyExisted || !newNode.hasLabel(label)){
                            newNode.addLabel(label);
                        }
                    }
                    
                    if(alreadyExisted){
                        tsvToNeo4jIds.put(nodeId, (Long)newNode.getProperty(Attributes.Neo4j_Id.name()));
                    }
                    else{
                        neo4jId = maxNeo4jId++;
                        newNode.setProperty(Attributes.Neo4j_Id.name(), neo4jId);
                        tsvToNeo4jIds.put(nodeId, neo4jId);
                        newNode.setProperty(Attributes.Logicalname.name(), logName);
                    }
                    
                    //keep the same transliteration if recompute transliteration for all that do not have any value
                    if(recomputeTransliteration){
                        // for all individuals
                        if(labels.contains(Labels.Type_Individual.name())){
                            String transliterationName = Utilities.getTransliterationString(logName,true);
                            newNode.setProperty(Attributes.Transliteration.name(),transliterationName);
                        }
                    }
                    else{
                        if(strVals.containsKey(Attributes.Transliteration.name())){
                            newNode.setProperty(Attributes.Transliteration.name(), strVals.get(Attributes.Transliteration.name()).get(0));
                        }
                    }      
                    if(strVals.containsKey(Attributes.ThesaurusReferenceId.name()) && strVals.get(Attributes.ThesaurusReferenceId.name()).get(0)!=null && strVals.get(Attributes.ThesaurusReferenceId.name()).get(0).length()>0){
                        try{
                            long refId = Long.parseLong(strVals.get(Attributes.ThesaurusReferenceId.name()).get(0));
                            newNode.setProperty(Attributes.ThesaurusReferenceId.name(),refId);
                        }
                        catch(NumberFormatException ex){
                            Utils.StaticClass.webAppSystemOutPrintln("Exception while trying to parseLong from String value: "+strVals.get(Attributes.ThesaurusReferenceId.name()).get(0));
                            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                            Utils.StaticClass.handleException(ex);
                            return false;
                        }
                    }
                    
                                        
                    if(strVals.containsKey(Configs.Neo4j_Key_For_MaxThesaurusReferenceId)){
                        long maxRefId = Long.parseLong(strVals.get(Configs.Neo4j_Key_For_MaxThesaurusReferenceId).get(0));
                            
                        newNode.setProperty(Configs.Neo4j_Key_For_MaxThesaurusReferenceId, maxRefId);
                    }
                    
                    if(strVals.containsKey(Attributes.Type.name())){
                        newNode.setProperty(Attributes.Type.name(), strVals.get(Attributes.Type.name()).get(0));
                    }
                    if(strVals.containsKey(Attributes.Value.name())){
                        if(strVals.get(Attributes.Type.name()).get(0).equals("INT")){
                            newNode.setProperty(Attributes.Value.name(), Integer.parseInt(strVals.get(Attributes.Value.name()).get(0)));
                        }
                        else{
                            newNode.setProperty(Attributes.Value.name(), strVals.get(Attributes.Value.name()).get(0));
                        }
                    }

                }
                tx.success();
            }
            
            //create InstanceOf, ISA, Relation relationships
            try(Transaction tx =  graphDb.beginTx()){
                Iterator<Long> nodeIdentifiers = nodeInfo.keySet().iterator();
                while(nodeIdentifiers.hasNext()){
                    long nodeId = nodeIdentifiers.next();
                    long startNodeNeo4jId = tsvToNeo4jIds.get(nodeId);
                    HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);
                    
                    ArrayList<Long> isA = new ArrayList();
                    ArrayList<Long> relation = new ArrayList();
                    ArrayList<Long> instanceOf = new ArrayList();
                    
                    if(strVals.containsKey(Rels.ISA.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.ISA.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            isA.add(tsvToNeo4jIds.get(endNodeId));
                        }
                    }
                    if(strVals.containsKey(Rels.RELATION.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.RELATION.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            relation.add(tsvToNeo4jIds.get(endNodeId));
                        }
                    }
                    if(strVals.containsKey(Rels.INSTANCEOF.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.INSTANCEOF.name());
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
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(isA, graphDb);
                        
                        ArrayList<Long> existingOnes = new ArrayList<>();
                        startNode.getRelationships(Rels.ISA, Direction.OUTGOING).forEach((r)->{
                            existingOnes.add(r.getEndNodeId());
                        });
                        for(Node toNode : toNodes){
                            if(!existingOnes.contains(toNode.getId())){
                                startNode.createRelationshipTo(toNode, Rels.ISA);
                            }
                        }
                    }
                    if(instanceOf.size()>0){
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(instanceOf, graphDb);
                        ArrayList<Long> existingOnes = new ArrayList<>();
                        startNode.getRelationships(Rels.INSTANCEOF, Direction.OUTGOING).forEach((r)->{
                            existingOnes.add(r.getEndNodeId());
                        });
                        for(Node toNode : toNodes){
                            if(!existingOnes.contains(toNode.getId())){
                                startNode.createRelationshipTo(toNode, Rels.INSTANCEOF);
                            }
                        }
                    }
                    if(relation.size()>0){
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(relation, graphDb);
                        ArrayList<Long> existingOnes = new ArrayList<>();
                        startNode.getRelationships(Rels.RELATION, Direction.OUTGOING).forEach((r)->{
                            existingOnes.add(r.getEndNodeId());
                        });
                        for(Node toNode : toNodes){
                            if(!existingOnes.contains(toNode.getId())){
                                startNode.createRelationshipTo(toNode, Rels.RELATION);
                            }
                        }
                    }
                }
                tx.success();
            }
            
            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new neo4j_sisapi.TMSAPIClass();
            
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            
              
            ArrayList<String> thesauriVector = new ArrayList<String>();
             
            //retrieve all thesauri and update the max facet/hierarchy/termvalues. also update the source value
            
            //Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService());
            //Q.TEST_open_connection();
            //Q.TEST_begin_query();
            DBGeneral dbGen = new DBGeneral();
            dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, "",true);
            thesauriVector = dbGen.GetExistingThesaurus(false, thesauriVector, Q, sis_session);
            
            //update MaxNeo4j_Id property in Telos_Object node
            if(Q.resetCounter_For_Neo4jId()==QClass.APIFail){
                Utils.StaticClass.webAppSystemOutPrintln("Set Max Neo4j Id Failed.");
                return false;
            }
          
            for(int i=0; i< thesauriVector.size(); i++){
                TA.SetThesaurusName(thesauriVector.get(i));
                if(TA.resetCounter_For_ThesarusReferenceId(thesauriVector.get(i),-1)==QClass.APIFail){
                    Utils.StaticClass.webAppSystemOutPrintln("Setting Max Thesaurus reference Id Failed for thesaurus: " + thesauriVector.get(i));
                    return false;
                }
            }

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        
       
        
        
        return true;        
    }
    
    public boolean importGenericFromFile(String filepath, boolean recomputeTransliteration){
        
        return globalImportFromFile(filepath, recomputeTransliteration);
    }
    
    boolean globalImportFromFile(String filepath, boolean recomputeTransliteration){
        try{
            HashMap<Long, HashMap<String, ArrayList<String>>> nodeInfo = new HashMap<Long,HashMap<String, ArrayList<String>>>();
            
            //ArrayList<String> multivaluedElements = new ArrayList<String>();
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
                        nodeInfo.put(internalId,new HashMap<String, ArrayList<String>>());
                    }
                    
                    //cast everything as String except multivalued elements - Labels and relationships
                    if(nodeInfo.get(internalId).containsKey(property)==false){
                        nodeInfo.get(internalId).put(property, new ArrayList<String>());
                    }
                    nodeInfo.get(internalId).get(property).add(valueStr);
                    
                }
                else{
                    Utils.StaticClass.webAppSystemOutPrintln("Non empty line found that contains less than 3 tab seperated values at line: " + line+". Aborting import procedure");
                    return false;
                }
            }//parsing ended
            
            
            //HashMap<Long,Long> tsvToNeo4jIds = new HashMap<Long,Long>();
            
            //long maxGenericNeo4jId = 0;
            /*
            Enumeration<Long> nodeIdentifiers = nodeInfo.keys();
            while(nodeIdentifiers.hasMoreElements()){
                long nodeId = nodeIdentifiers.nextElement();
                HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);
                if(strVals.containsKey(LabelKey) && strVals.get(LabelKey).contains(GenericLabel)){
                    long neo4jId = Long.parseLong(strVals.get(Neo4j_Key_For_Neo4j_Id).get(0));
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
                Iterator<Long> nodeIdentifiers = nodeInfo.keySet().iterator();
                while(nodeIdentifiers.hasNext()){
                    long nodeId = nodeIdentifiers.next();
                    HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);


                    String logicalName = "";
                    String type = "";
                    String value = "";
                    long neo4jId = -1;
                    
                    ArrayList<String> labels = new ArrayList<String>();
                    if(strVals.containsKey(LabelKey)){
                        labels.addAll(strVals.get(LabelKey));                    
                    }

                    
                   
                    Node newNode = graphDb.createNode();
                    for(String lbl: labels){
                        Label label = Label.label(lbl);
                        newNode.addLabel(label);
                    }
                    
                    //if(labels.contains(GenericLabel)){
                        neo4jId = Long.parseLong(strVals.get(Attributes.Neo4j_Id.name()).get(0));
                        newNode.setProperty(Attributes.Neo4j_Id.name(), neo4jId);
                      //  tsvToNeo4jIds.put(nodeId, neo4jId);
                    //}
                    //else{
                      //  neo4jId = maxGenericNeo4jId++;
                        //newNode.setProperty(Neo4j_Key_For_Neo4j_Id, neo4jId);
                        //tsvToNeo4jIds.put(nodeId, neo4jId);
                    //}
                    
                    String logName = strVals.get(Attributes.Logicalname.name()).get(0);
                    newNode.setProperty(Attributes.Logicalname.name(), logName);
                    
                    //keep the same transliteration if recompute transliteration for all that do not have any value
                    if(recomputeTransliteration){ 
                        // for all individuals
                        if(labels.contains(Labels.Type_Individual.name())){
                            String transliterationName = Utilities.getTransliterationString(logName,true);
                            newNode.setProperty(Attributes.Transliteration.name(),transliterationName);
                        }
                    }
                    else{
                        if(strVals.containsKey(Attributes.Transliteration.name())){
                            newNode.setProperty(Attributes.Transliteration.name(), strVals.get(Attributes.Transliteration.name()).get(0));
                        }
                    }      
                    if(strVals.containsKey(Attributes.ThesaurusReferenceId.name()) && strVals.get(Attributes.ThesaurusReferenceId.name()).get(0)!=null && strVals.get(Attributes.ThesaurusReferenceId.name()).get(0).length()>0){
                        try{
                            long refId = Long.parseLong(strVals.get(Attributes.ThesaurusReferenceId.name()).get(0));
                            newNode.setProperty(Attributes.ThesaurusReferenceId.name(),refId);
                        }
                        catch(NumberFormatException ex){
                            Utils.StaticClass.webAppSystemOutPrintln("Exception while trying to parseLong from String value: "+strVals.get(Attributes.ThesaurusReferenceId.name()).get(0));
                            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
                            Utils.StaticClass.handleException(ex);
                            return false;
                        }
                    }
                    
                    if(strVals.containsKey(Configs.Neo4j_Key_For_MaxThesaurusReferenceId)){
                        long maxRefId = Long.parseLong(strVals.get(Configs.Neo4j_Key_For_MaxThesaurusReferenceId).get(0));
                        newNode.setProperty(Configs.Neo4j_Key_For_MaxThesaurusReferenceId, maxRefId);
                    }
                    
                    if(strVals.containsKey(Attributes.Type.name())){
                        newNode.setProperty(Attributes.Type.name(), strVals.get(Attributes.Type.name()).get(0));
                    }
                    if(strVals.containsKey(Attributes.Value.name())){
                        if(strVals.get(Attributes.Type.name()).get(0).equals("INT")){
                            newNode.setProperty(Attributes.Value.name(), Integer.parseInt(strVals.get(Attributes.Value.name()).get(0)));
                        }
                        else{
                            newNode.setProperty(Attributes.Value.name(), strVals.get(Attributes.Value.name()).get(0));
                        }
                    }

                }
                tx.success();
            }

            QClass Q = new neo4j_sisapi.QClass();
            TMSAPIClass TA = new neo4j_sisapi.TMSAPIClass();
            IntegerObject sis_session = new IntegerObject();
            IntegerObject tms_session = new IntegerObject();
            DBGeneral dbGen = new DBGeneral();
            dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, "",true);
            
            if(!TA.createThesaurusDatabaseIndexesAndConstraints(graphDb)){
                return false;
            }
            
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
            
            Utils.StaticClass.closeDb();
            graphDb = Utils.StaticClass.getDBService();
            
            //create InstanceOf, ISA, Relation relationships
            try(Transaction tx =  graphDb.beginTx()){
                Iterator<Long> nodeIdentifiers = nodeInfo.keySet().iterator();
                while(nodeIdentifiers.hasNext()){
                    long nodeId = nodeIdentifiers.next();
                    long startNodeNeo4jId = nodeId;//tsvToNeo4jIds.get(nodeId);
                    HashMap<String,ArrayList<String>> strVals = nodeInfo.get(nodeId);
                    
                    ArrayList<Long> isA = new ArrayList<Long>();
                    ArrayList<Long> relation = new ArrayList<Long>();
                    ArrayList<Long> instanceOf = new ArrayList<Long>();
                    if(strVals.containsKey(Rels.ISA.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.ISA.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            isA.add(endNodeId/*tsvToNeo4jIds.get(endNodeId)*/);
                        }
                    }
                    if(strVals.containsKey(Rels.RELATION.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.RELATION.name());
                        for(String str : stringVals){
                            long endNodeId = Long.parseLong(str);
                            relation.add(endNodeId/*tsvToNeo4jIds.get(endNodeId)*/);
                        }
                    }
                    if(strVals.containsKey(Rels.INSTANCEOF.name())){
                        ArrayList<String> stringVals = strVals.get(Rels.INSTANCEOF.name());
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
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(isA, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.ISA);
                        }
                    }
                    if(instanceOf.size()>0){
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(instanceOf, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.INSTANCEOF);
                        }
                    }
                    if(relation.size()>0){
                        ArrayList<Node> toNodes = getNodesByNeo4jIds(relation, graphDb);
                        for(Node toNode : toNodes){
                            startNode.createRelationshipTo(toNode, Rels.RELATION);
                        }
                    }
                }
                tx.success();
            }
            
            
            
            ArrayList<String> thesauriVector = new ArrayList<String>();
             
            //retrieve all thesauri and update the max facet/hierarchy/termvalues. also update the source value
            
            /*
            Q.TEST_create_SIS_CS_Session(Utils.StaticClass.getDBService());
            Q.TEST_open_connection();
            Q.TEST_begin_query();
            */
            dbGen.openConnectionAndStartQueryOrTransaction(Q, TA, sis_session, tms_session, "",true);
            thesauriVector = dbGen.GetExistingThesaurus(false, thesauriVector, Q, sis_session);

            //update MaxNeo4j_Id property in Telos_Object node
            if(Q.resetCounter_For_Neo4jId()==QClass.APIFail){
                Utils.StaticClass.webAppSystemOutPrintln("Set Max Neo4j Id Failed.");
                    return false;
            }
            
             for(int i=0; i< thesauriVector.size(); i++){
                 TA.SetThesaurusName(thesauriVector.get(i));
                 if(TA.resetCounter_For_ThesarusReferenceId(thesauriVector.get(i),-1)==QClass.APIFail){
                     Utils.StaticClass.webAppSystemOutPrintln("Setting Max Thesaurus reference Id Failed for thesaurus: " + thesauriVector.get(i));
                     return false;
                 }
            }
            
            //end query and close connection
            
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, TA, sis_session, tms_session, true);
        
           
            /*
            for(int i=0; i< thesauriVector.size(); i++){
                
                String thesaurusName = thesauriVector.get(i);            
                String FacetThesName = Neo4j_Node_LogicalName_For_MaxTHESFacetId.replace("%THES%", thesaurusName);
                String HierarchyThesName = Neo4j_Node_LogicalName_For_MaxTHESHierarchyId.replace("%THES%", thesaurusName);
                String TermThesName = Neo4j_Node_LogicalName_For_MaxTHESTermId.replace("%THES%", thesaurusName);

                
                //Facet Count:       (Store id in %THES%Facet - property MaxThesaurusFacetId. URI produced as /%Thes%/Facet/0001)
                //===================
                //MATCH (n:S_Class:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'%THES%Facet'}) RETURN count(n) // n.Logicalname order by n.Logicalname

                
                //update MaxThesarusFacetId property in %THES%Facet node
                try(Transaction tx = graphDb.beginTx()){
                    //MATCH (n:S_Class:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'%THES%Facet'}) RETURN count(n) // n.Logicalname order by n.Logicalname
                    String query4 = "MATCH(n:"+Labels.S_Class.name()+":"+Labels.Type_Individual.name()+":"+CommonLabel+") - [:INSTANCEOF]->(m:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\"" +FacetThesName+ "\"}) with count(n) as newVal " +
                            "MATCH(t:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\""+FacetThesName+"\"}) " +
                            "SET t."+Neo4j_Key_For_MaxThesaurusFacetId+" = newVal " +
                            "return t."+Neo4j_Key_For_MaxThesaurusFacetId+" as "+ Neo4j_Key_For_MaxThesaurusFacetId;
                    Result res4 = graphDb.execute(query4);

                    if (res4 == null) {
                        Utils.StaticClass.webAppSystemOutPrintln("Set Max Thesaurus Facet Id Failed.");
                        return false;
                    }
                    res4.close();
                    res4=null;
                    tx.success();
                }
                
                //Hierarchies Count:       (Store id in %THES%Hierarchy - property MaxThesaurusHierarchyId. URI produced as /%Thes%/Hierarchy/0001)
                //===================
                //MATCH (n:S_Class:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'%THES%Hierarchy'}) RETURN  count(n) // n.Logicalname order by n.Logicalname

                //update MaxThesaurusHierarchyId property in %THES%Hierarchy node
                try(Transaction tx = graphDb.beginTx()){
                    //MATCH (n:S_Class:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'%THES%Hierarchy'}) RETURN  count(n) // n.Logicalname order by n.Logicalname
                    String query5 = "MATCH(n:"+Labels.S_Class.name()+":"+Labels.Type_Individual.name()+":"+CommonLabel+")  - [:INSTANCEOF]->(m:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\"" +HierarchyThesName+ "\"}) with count(n) as newVal " +
                            "MATCH(t:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\""+HierarchyThesName+"\"}) " +
                            "SET t."+Neo4j_Key_For_MaxThesaurusHierarchyId+" = newVal " +
                            "return t."+Neo4j_Key_For_MaxThesaurusHierarchyId+" as "+ Neo4j_Key_For_MaxThesaurusHierarchyId;
                    Result res5 = graphDb.execute(query5);

                    if (res5 == null) {
                        Utils.StaticClass.webAppSystemOutPrintln("Set Max Thesaurus Hierarchy Id Failed.");
                        return false;
                    }
                    res5.close();
                    res5=null;
                    tx.success();
                }
                
                //Terms Count:       (Store id in %THES%HierarchyTerm - property MaxThesaurusTermId. URI produced as /%Thes%/Term/0001) (includes TopTerms)
                //===================
                //MATCH(m:Common{Logicalname:"%THES%HierarchyTerm"}) <-[:ISA*0..]-(k)<-[:INSTANCEOF]-(n) RETURN count(n) // n.Logicalname order by n.Logicalname;

                //update MaxThesaurusTermId property in %THES%HierarchyTerm node
                try(Transaction tx = graphDb.beginTx()){
                    //MATCH(m:Common{Logicalname:"%THES%HierarchyTerm"}) <-[:ISA*0..]-(k)<-[:INSTANCEOF]-(n) RETURN count(n) // n.Logicalname order by n.Logicalname;
                    String query6 = "MATCH(m:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\"" +TermThesName+ "\"}) <-[:ISA*0..]-(k)<-[:INSTANCEOF]-(n) with count(n) as newVal " +
                            "MATCH(t:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\""+TermThesName+"\"}) " +
                            "SET t."+Neo4j_Key_For_MaxThesaurusTermId+" = newVal " +
                            "return t."+Neo4j_Key_For_MaxThesaurusTermId+" as "+ Neo4j_Key_For_MaxThesaurusTermId;
                    Result res6 = graphDb.execute(query6);

                    if (res6 == null) {
                        Utils.StaticClass.webAppSystemOutPrintln("Set Max Thesaurus Term Id Failed.");
                        return false;
                    }
                    res6.close();
                    res6=null;
                    tx.success();
                }
            }
            
            //Sources Count:       (Store id in Source - property MaxSourceId. URI produced as /Source/0001)
            //===================
            //MATCH (n:Token:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'Source'}) RETURN count(n) // n.Logicalname order by n.Logicalname;

            //update MaxSourceId property in Source node
            try(Transaction tx = graphDb.beginTx()){
                //MATCH (n:Token:Type_Individual:Common) - [:INSTANCEOF]->(m:Common{Logicalname:'Source'}) RETURN count(n) // n.Logicalname order by n.Logicalname;
                String query7 = "MATCH(n:"+Labels.Token.name()+":"+Labels.Type_Individual.name()+":"+CommonLabel+") - [:INSTANCEOF]->(m:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\"" +Neo4j_Node_LogicalName_For_MaxSourceId+ "\"}) with count(n) as newVal " +
                        "MATCH(t:"+CommonLabel+"{"+Neo4j_Key_For_Logicalname+":\""+Neo4j_Node_LogicalName_For_MaxSourceId+"\"}) " +
                        "SET t."+Neo4j_Key_For_MaxSourceId+" = newVal " +
                        "return t."+Neo4j_Key_For_MaxSourceId+" as "+ Neo4j_Key_For_MaxSourceId;
                Result res7 = graphDb.execute(query7);

                if (res7 == null) {
                    Utils.StaticClass.webAppSystemOutPrintln("Set Max Source Id Failed.");
                    return false;
                }
                res7.close();
                res7=null;
                tx.success();
            }*/
        }
        catch(Exception ex){
            Utils.StaticClass.webAppSystemOutPrintln(ex.getClass() +" " + ex.getMessage());
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
    }
    
    ArrayList<Long> collectSequenctiallyAsubsetOfValues(int startindex,int howmanyToGet, ArrayList<Long> targetVals){
        ArrayList<Long> returnVals = new ArrayList<Long>();
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
        
        String query = "MATCH(n:"+Configs.Labels.Common.name()+"{"+Attributes.Neo4j_Id.name()+":"+neo4jId+"}) RETURN n";
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
    
    ArrayList<Node> getNodesByNeo4jIds(ArrayList<Long> neo4jIds, GraphDatabaseService graphDb){
        ArrayList<Node> returnVec = new ArrayList<Node>();
        
        int loopIndex = 0;
        int maxIndex = neo4jIds.size();

        if(maxIndex==0){
            return returnVec;
        }
        
        while (loopIndex < maxIndex) {

            ArrayList<Long> subSetofIds = collectSequenctiallyAsubsetOfValues(loopIndex,500, neo4jIds);
            loopIndex += subSetofIds.size();
            if(subSetofIds.size()==0){
                break;
            }
            String query = "";
            
            if(subSetofIds.size()==1){
                query = "MATCH(n:"+Configs.Labels.Common.name()+"{"+Attributes.Neo4j_Id.name()+":"+subSetofIds.get(0)+"}) RETURN n";
                
            }
            else{
                query = " Match (n:"+Configs.Labels.Common.name()+") WHERE n."+Attributes.Neo4j_Id.name()+" IN " + subSetofIds.toString() + " "+
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
    
    
}
