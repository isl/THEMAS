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

import Utils.ConstantParameters;
import Utils.Parameters;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 *
 * @author tzortzak
 */
public class OfflineConversionClass {

    //"AAT2THEMAS"
    //"AAT2SKOS"
    //"AATIDS"
    //"SKOS2THEMAS"
    //"THEMAS2SKOS"
    private static String mode = "AAT2THEMAS";

    public static void main(String[] args) {

        //Utils.StaticClass.webAppSystemOutPrintln(OfflineConversionClass.class.getResource("OfflineConversionClass.class"));
        String basePath = null;
        try {
            basePath = URLDecoder.decode(OfflineConversionClass.class.getResource("TestConversionClass.class").getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
            
        }
        if(basePath!=null){
            
            basePath = basePath.substring(0,basePath.indexOf("WEB-INF")-1).toLowerCase().replaceAll("\\\\", "/");
            if(basePath.startsWith("file:/")){
                basePath = basePath.replaceFirst("file:/", "");
            }
            if(basePath.startsWith("/")){
                basePath = basePath.replaceFirst("/", "");
            }

        }
        else{
            basePath = System.getProperty("user.dir").replace("WEB-INF\\classes", "").replace("WEB-INF/classes", "");
        }
        Utils.StaticClass.webAppSystemOutPrintln(basePath);
        Parameters.initParams(basePath);
        //ConstantParameters.THEMASStatusLang = "en";
        //Parameters.PrimaryLang ="EN";
        //Parameters.UILang ="en";
        //Parameters.TRANSLATION_SEPERATOR =":";
        //Parameters.initParams("C:\\local_users\\tzortzak\\Projects\\THEMAS\\Code\\THEMAS\\build\\web");
        //Parameters.initParams(System.getProperty("user.dir").replace("WEB-INF\\classes", "").replace("WEB-INF/classes", ""));
        
        Utils.StaticClass.webAppSystemOutPrintln("Parameters.BaseDir = " + Parameters.BaseRealPath);
        boolean oldFilterVal = ConstantParameters.filterBts_Nts_Rts;
        String oldRefThesaurusVal = ConstantParameters.referenceThesaurusSchemeName;
        String oldSchemePrefixVal = ConstantParameters.SchemePrefix;
        

        //State arguments        
        String inputFilePath = "C:\\local_users\\tzortzak\\Projects\\THEMAS\\Code\\ThesaurusFileConverter\\test_examples\\Original Files\\AAT.xml";
        //String inputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\results.xml";
        //String outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\results3.rdf";
        String outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\results.xml";

        ConstantParameters.filterBts_Nts_Rts = false;


        ConstantParameters.referenceThesaurusSchemeName = "http://www.3d-coform.eu/PhysicalObjectTypes";
        ConstantParameters.SchemePrefix ="http://www.getty.edu/research/tools/vocabularies/aat";
        
        //for use with aatToTHEMAS function
        ArrayList<String> filterFacets = new ArrayList<String>();
        ArrayList<String> filterHierarchies = new ArrayList<String>();

        //for use with aatSpecificSubjectIds function
        ArrayList<String> targetSubjectIds = new ArrayList<String>();
/*
        targetSubjectIds.add("300041622");
        targetSubjectIds.add("300041619");
        targetSubjectIds.add("300041583");
        targetSubjectIds.add("300162131");
        targetSubjectIds.add("300190947");
        targetSubjectIds.add("300234984");
        targetSubjectIds.add("300205017");
        targetSubjectIds.add("300241584");*/
        //targetSubjectIds.add("300037316");
        //targetSubjectIds.add("300055673");
        //targetSubjectIds.add("300025591");

        //targetSubjectIds.add("300073738");
        //targetSubjectIds.add("300065581");
        //targetSubjectIds.add("300054359");
        //targetSubjectIds.add("300069413");
        //outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\results.xml";
        //ConversionActions.aatSpecificSubjectIds(inputFilePath, outputFilePath, targetSubjectIds);
        //targetSubjectIds.add("300025622");
        //targetSubjectIds.add("300132462");
        //outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\results.xml";
        //ConversionActions.aatSpecificSubjectIds(inputFilePath, outputFilePath, targetSubjectIds);

        //filterFacets.add("Objects Facet");

        //filterHierarchies.add("Object Genres");
        //filterHierarchies.add("Object Groupings and Systems");
        //filterHierarchies.add("Conditions and Effects");//76
        //filterHierarchies.add("Physical and Mental Activities");//130 terms
        //filterHierarchies.add("Visual and Verbal Communication");

        if(args!=null && args.length==10){
            mode = args[0];
            Utils.StaticClass.webAppSystemOutPrintln("Conversion Mode: " + mode);
            inputFilePath=args[1];
            Utils.StaticClass.webAppSystemOutPrintln("Input File: " + inputFilePath);
            outputFilePath = args[2];
            Utils.StaticClass.webAppSystemOutPrintln("Output File: " + outputFilePath);
            if(args[3].equals("true")){
                ConstantParameters.filterBts_Nts_Rts = true;
            }
            Utils.StaticClass.webAppSystemOutPrintln("Filter Output Terms: " + args[3]);


            String filterDelimeter = args[4];
            Utils.StaticClass.webAppSystemOutPrintln("Filter Delimeter: " + filterDelimeter);
            String filterFacetsStr = args[5];
            Utils.StaticClass.webAppSystemOutPrintln("Filter Facets: " + filterFacetsStr);
            String filterHierarchiesStr = args[6];
            Utils.StaticClass.webAppSystemOutPrintln("Filter Hierarchies: " + filterHierarchiesStr);
            String filterSubjectIdsStr = args[7];
            Utils.StaticClass.webAppSystemOutPrintln("Filter Subject Ids: " + filterSubjectIdsStr);

            if(filterFacetsStr!=null && filterFacetsStr.trim().length()>0){
                String[] facetsArray = filterFacetsStr.split(filterDelimeter);
                if(facetsArray!=null){
                    for(int k=0; k< facetsArray.length; k++){
                        String targetFacet = facetsArray[k];
                        if(filterFacets.contains(targetFacet)==false){
                            filterFacets.add(targetFacet);
                        }
                    }
                }
            }

            if(filterHierarchiesStr!=null && filterHierarchiesStr.trim().length()>0){
                String[] hierarchiesArray = filterHierarchiesStr.split(filterDelimeter);
                if(hierarchiesArray!=null){
                    for(int k=0; k< hierarchiesArray.length; k++){
                        String targetHier = hierarchiesArray[k];
                        if(filterHierarchies.contains(targetHier)==false){
                            filterHierarchies.add(targetHier);
                        }
                    }
                }
            }

            if(filterSubjectIdsStr!=null && filterSubjectIdsStr.trim().length()>0){
                String[] idsArray = filterSubjectIdsStr.split(filterDelimeter);
                if(idsArray!=null){
                    for(int k=0; k< idsArray.length; k++){
                        String targetId = idsArray[k];
                        if(targetSubjectIds.contains(targetId)==false){
                            targetSubjectIds.add(targetId);
                        }
                    }
                }
            }

            if(mode.equals("AAT2SKOS")){

                ConstantParameters.referenceThesaurusSchemeName = args[8];
                Utils.StaticClass.webAppSystemOutPrintln("ReferenceThesaurusSchemeName: " + ConstantParameters.referenceThesaurusSchemeName);

                ConstantParameters.SchemePrefix = args[9];
                Utils.StaticClass.webAppSystemOutPrintln("SchemePrefix: " + ConstantParameters.SchemePrefix);
            }

            
        }
        
        try{
            if(mode.equals("AAT2THEMAS")){
                ConversionActions.aatToTHEMAS(inputFilePath, outputFilePath, filterFacets, filterHierarchies);
            }
            else if(mode.equals("AAT2SKOS")){
                ConversionActions.aatToSkos(inputFilePath, outputFilePath, filterFacets, filterHierarchies);
            }
            else if(mode.equals("AATIDS")){
                ConversionActions.aatSpecificSubjectIds(inputFilePath, outputFilePath, targetSubjectIds);
            }
            else if(mode.equals("SKOS2THEMAS")){
                ConversionActions.skosToTHEMAS(inputFilePath, outputFilePath, filterFacets, filterHierarchies);
            }
            else if(mode.equals("THEMAS2SKOS")){
                ConversionActions.themasToSkos(inputFilePath, outputFilePath, filterFacets, filterHierarchies);
            }
        }
        catch(Exception Ex){
            Utils.StaticClass.webAppSystemOutPrintln("Exception Caught: "+Ex.getMessage());
            Utils.StaticClass.handleException(Ex);            
        }

        
  

        ConstantParameters.filterBts_Nts_Rts = oldFilterVal;
        ConstantParameters.referenceThesaurusSchemeName =  oldRefThesaurusVal;
        ConstantParameters.SchemePrefix =  oldSchemePrefixVal;

        //filterHierarchies.add("Furnishings and Equipment");
        //filterHierarchies.add("Object Genres");
        //filterHierarchies.add("Visual and Verbal Communication");
        //outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\THEMASLoad_Object_Groupings_And_Systems.xml";
        

        /*
        filterFacets = new ArrayList<String>();
        filterHierarchies = new ArrayList<String>();
        filterHierarchies.add("Object Groupings and Systems");
        outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\THEMASLoad.rdf";
        ConversionActions.aatToSkos(inputFilePath, outputFilePath, filterFacets, filterHierarchies);*/
/*
        inputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\THEMASLoad.rdf";
        outputFilePath = "C:\\Documents and Settings\\tzortzak\\Desktop\\themasdraft\\THEMASLoad2.xml";

        ConversionActions.skosToTHEMAS(inputFilePath, outputFilePath, filterFacets, filterHierarchies);
*/
    }
}
