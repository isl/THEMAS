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
package XMLHandling;

import Utils.ConstantParameters;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SortItem;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import neo4j_sisapi.StringObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author tzortzak
 */
public class ConversionActions {

    private static void themasConvert(String inputFilePath, String outputScheme, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        boolean processSucceded = true;
        String inputScheme = ConstantParameters.xmlschematype_THEMAS;

        //internal structures
        ArrayList<String> xmlFacets = new ArrayList<String>();
        ArrayList<String> guideTerms = new ArrayList<String>();

        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        //ArrayList<String> LinkingToSelf = new ArrayList<String>();

        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();


        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();


        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> userSelections = new HashMap<String, String>();

        ParseFileData parser = new ParseFileData();
        WriteFileData writer = new WriteFileData();


        String importThesaurusName = parser.readThesaurusName(inputFilePath, inputScheme);

        /* Step1 Read Facets specified by XML****************************************************/
        if (parser.readXMLFacets(importThesaurusName, inputFilePath, inputScheme, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +  "Failed to read Facets.");
            processSucceded = false;
        }


        /* Step2 Read Hierarchies specified by XML************************************************/
        if (processSucceded && parser.readXMLHierarchies(importThesaurusName, inputFilePath, inputScheme, hierarchyFacets, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Hierarchies.");
            processSucceded = false;
        }

        if (processSucceded) {
            parser.readTranslationCategories(inputFilePath, inputScheme, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections);
        }


        if (processSucceded && parser.readXMLTerms(inputFilePath, inputScheme, termsInfo, userSelections) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Terms.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLSources(inputFilePath, inputScheme, XMLsources) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Sources.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLGuideTerms(inputFilePath, inputScheme, guideTerms, XMLguideTermsRelations) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Guide Terms / Node Labels.");
            processSucceded = false;
        }


        ArrayList<String> filterTerms = new ArrayList<String>();

        OutputStreamWriter logFileWriter = null;

        try {
            OutputStream fout = new FileOutputStream(outputFilePath);
            OutputStream bout = new BufferedOutputStream(fout);
                logFileWriter = new OutputStreamWriter(bout, "UTF-8");

            writer.WriteFileStart(logFileWriter, outputScheme, importThesaurusName, Parameters.UILang); //default ui lang

            writer.WriteTranslationCategories(logFileWriter, outputScheme, userSelections);

            writer.WriteFacets(logFileWriter, outputScheme, importThesaurusName, xmlFacets, hierarchyFacets, termsInfo, filterFacets, filterTerms);

            writer.WriteHierarchies(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations, filterFacets, filterTerms);

            writer.WriteTerms(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations,null, filterTerms);

            writer.WriteFileEnd(logFileWriter, outputScheme);

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }


        //Call convertXMLtoSKOS method
        //boolean processSucceded=thes.convertXMLtoSKOS(xmlFilePath,rdfFilePath,xmlThesaurusFormat,include_Create_Modify_info,language_option_1,language_option_2);

        //Check if convertion succeded and print on console the last message.
        if (processSucceded) {
            Utils.StaticClass.webAppSystemOutPrintln("Process complete.");
        } else {
            Utils.StaticClass.webAppSystemOutPrintln("Process fail.");
        }
        
    }
    private static void skosConvert(String inputFilePath, String outputScheme, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        boolean processSucceded = true;
        String inputScheme = ConstantParameters.xmlschematype_skos;

        //internal structures
        ArrayList<String> xmlFacets = new ArrayList<String>();
        ArrayList<String> guideTerms = new ArrayList<String>();

        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        //ArrayList<String> LinkingToSelf = new ArrayList<String>();

        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();


        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();


        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> userSelections = new HashMap<String, String>();

        ParseFileData parser = new ParseFileData();
        WriteFileData writer = new WriteFileData();


        String importThesaurusName = parser.readThesaurusName(inputFilePath, inputScheme);

        /* Step1 Read Facets specified by XML****************************************************/
        if (parser.readXMLFacets(importThesaurusName, inputFilePath, inputScheme, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Facets.");
            processSucceded = false;
        }


        /* Step2 Read Hierarchies specified by XML************************************************/
        if (processSucceded && parser.readXMLHierarchies(importThesaurusName, inputFilePath, inputScheme, hierarchyFacets, xmlFacets) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Hierarchies.");
            processSucceded = false;
        }

        if (processSucceded) {
            parser.readTranslationCategories(inputFilePath, inputScheme, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections);
        }


        if (processSucceded && parser.readXMLTerms(inputFilePath, inputScheme, termsInfo, userSelections) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Terms.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLSources(inputFilePath, inputScheme, XMLsources) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Sources.");
            processSucceded = false;
        }

        if (processSucceded && parser.readXMLGuideTerms(inputFilePath, inputScheme, guideTerms, XMLguideTermsRelations) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Guide Terms / Node Labels.");
            processSucceded = false;
        }


        ArrayList<String> filterTerms = new ArrayList<String>();

        OutputStreamWriter logFileWriter = null;

        try {
            OutputStream fout = new FileOutputStream(outputFilePath);
            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");

            writer.WriteFileStart(logFileWriter, outputScheme, importThesaurusName, Parameters.UILang); //default ui lang

            writer.WriteTranslationCategories(logFileWriter, outputScheme, userSelections);

            writer.WriteFacets(logFileWriter, outputScheme, importThesaurusName, xmlFacets, hierarchyFacets, termsInfo, filterFacets, filterTerms);

            writer.WriteHierarchies(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations, filterFacets, filterTerms);

            writer.WriteTerms(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations, null, filterTerms);

            writer.WriteFileEnd(logFileWriter, outputScheme);

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }


        //Call convertXMLtoSKOS method
        //boolean processSucceded=thes.convertXMLtoSKOS(xmlFilePath,rdfFilePath,xmlThesaurusFormat,include_Create_Modify_info,language_option_1,language_option_2);

        //Check if convertion succeded and print on console the last message.
        if (processSucceded) {
            Utils.StaticClass.webAppSystemOutPrintln("Process complete.");
        } else {
            Utils.StaticClass.webAppSystemOutPrintln("Process fail.");
        }
    }

    public static void skosToTHEMAS(String inputFilePath, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        skosConvert(inputFilePath, ConstantParameters.xmlschematype_THEMAS, outputFilePath, filterFacets, filterHierarchies);
    }

    public static void themasToSkos(String inputFilePath, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        themasConvert(inputFilePath, ConstantParameters.xmlschematype_skos, outputFilePath, filterFacets, filterHierarchies);
    }

    public static void aatSpecificSubjectIds(String inputFilePath, String outputFilePath, ArrayList<String> targetSubjectIds) {
        ParseFileData parser = new ParseFileData();

        PrintStream stdout = System.out;
        OutputStream fout = null;
        try {
            fout = new FileOutputStream(outputFilePath);
            OutputStream bout = new BufferedOutputStream(fout);

            //OutputStreamWriter logFileWriter = new OutputStreamWriter(bout, "UTF-8");
            System.setOut(new PrintStream(bout, true, "UTF-8"));



            parser.getAAT_SpecificSubjectIds(inputFilePath, ConstantParameters.xmlschematype_aat, targetSubjectIds);

            fout.close();

        } catch (UnsupportedEncodingException ex) {
            System.setOut(stdout);
            Utils.StaticClass.webAppSystemOutPrintln("UnsupportedEncodingException Caught:" + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (FileNotFoundException ex) {
            System.setOut(stdout);
            Utils.StaticClass.webAppSystemOutPrintln("FileNotFoundException Caught:" + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            System.setOut(stdout);
            Utils.StaticClass.webAppSystemOutPrintln("IOException Caught:" + ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }
        WriteFileData.formatXMLFile(outputFilePath);
        System.setOut(stdout);
        Utils.StaticClass.webAppSystemOutPrintln("Specific Subject Ids finished");
    }

    private static void aatConvert(String inputFilePath, String outputScheme, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        boolean processSucceded = true;
        ParseFileData parser = new ParseFileData();

        ArrayList<String> xmlFacets = new ArrayList<String>();
        ArrayList<String> guideTerms = new ArrayList<String>();

        HashMap<String, String> XMLsources = new HashMap<String, String>();
        HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations = new HashMap<String, ArrayList<SortItem>>();

        HashMap<String, ArrayList<String>> hierarchyFacets = new HashMap<String, ArrayList<String>>();

        HashMap<String, NodeInfoStringContainer> termsInfo = new HashMap<String, NodeInfoStringContainer>();


        ArrayList<String> userSelectedTranslationWords = new ArrayList<String>();
        ArrayList<String> userSelectedTranslationIdentifiers = new ArrayList<String>();
        HashMap<String, String> userSelections = new HashMap<String, String>();


        //String inputFilePath =xmlFilePath;
        String inputScheme = ConstantParameters.xmlschematype_aat;

        //String outputFilePath =rdfFilePath;


        String importThesaurusName = parser.readThesaurusName(inputFilePath, inputScheme);


        
        


        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Terms.");
        if (processSucceded && parser.readAAT_XML(inputFilePath, inputScheme,
                xmlFacets,
                hierarchyFacets,
                termsInfo,
                guideTerms,
                XMLguideTermsRelations) == false) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Terms.");
            processSucceded = false;
        }


        ArrayList<String> filterTerms = new ArrayList<String>();

        if (filterFacets.size() > 0) {
            //include  every hierarchy term that is under this facet
            Iterator<String> hierEnum = hierarchyFacets.keySet().iterator();
            while (hierEnum.hasNext()) {
                String hierarchyName = hierEnum.next();
                ArrayList<String> hierFacets = hierarchyFacets.get(hierarchyName);
                for (int i = 0; i < hierFacets.size(); i++) {
                    String facetName = hierFacets.get(i);
                    if (filterFacets.contains(facetName)) {
                        filterTerms.add(hierarchyName);
                        break;
                    }
                }
            }
            filterTerms = parser.getRecursiveNts(filterTerms, termsInfo);
            
        } else if (filterHierarchies.size() > 0) {
            filterFacets.clear();
            //include all facets of the filtered hierarchies and their relevant terms
            for (int i = 0; i < filterHierarchies.size(); i++) {
                String hierName = filterHierarchies.get(i);
                ArrayList<String> hierFacets = hierarchyFacets.get(hierName);

                if (filterTerms.contains(hierName) == false) {
                    filterTerms.add(hierName);
                }

                if (hierFacets != null) {
                    for (int k = 0; k < hierFacets.size(); k++) {
                        String fname = hierFacets.get(k);
                        if (filterFacets.contains(fname) == false) {
                            filterFacets.add(fname);
                        }
                    }
                }
            }
            filterTerms = parser.getRecursiveNts(filterTerms, termsInfo);
        }

        


        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Translation Categories.");
        if (outputScheme.equals(ConstantParameters.xmlschematype_THEMAS)
                && parser.readTranslationCategoriesFromTerms(termsInfo,filterTerms, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections) == false) {
            processSucceded = false;
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Failed to read Translation Categories.");
        }


        Utils.StaticClass.webAppSystemOutPrintln("filterTerms size: " + filterTerms.size());

        WriteFileData writer = new WriteFileData();

        OutputStreamWriter logFileWriter = null;

        try {
            OutputStream fout = new FileOutputStream(outputFilePath);
            OutputStream bout = new BufferedOutputStream(fout);
            logFileWriter = new OutputStreamWriter(bout, "UTF-8");

            writer.WriteFileStart(logFileWriter, outputScheme, importThesaurusName, Parameters.UILang); //default ui lang
            
            writer.WriteTranslationCategories(logFileWriter, outputScheme, userSelections);

            writer.WriteFacets(logFileWriter, outputScheme, importThesaurusName, xmlFacets, hierarchyFacets, termsInfo, filterFacets, filterTerms);

            writer.WriteHierarchies(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations, filterFacets, filterTerms);

            writer.WriteTerms(logFileWriter, outputScheme, importThesaurusName, hierarchyFacets, termsInfo, XMLguideTermsRelations, null, filterTerms);

            writer.WriteFileEnd(logFileWriter, outputScheme);

        } catch (Exception exc) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Error in opening file: " + exc.getMessage());
            Utils.StaticClass.handleException(exc);
        }

        //Check if convertion succeded and print on console the last message.
        if (processSucceded) {
            Utils.StaticClass.webAppSystemOutPrintln("Process complete.");
        } else {
            Utils.StaticClass.webAppSystemOutPrintln("Process fail.");
        }
    }

    public static void aatToTHEMAS(String inputFilePath, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        aatConvert(inputFilePath, ConstantParameters.xmlschematype_THEMAS, outputFilePath, filterFacets, filterHierarchies);
    }

    public static void aatToSkos(String inputFilePath, String outputFilePath,
            ArrayList<String> filterFacets, ArrayList<String> filterHierarchies) {

        aatConvert(inputFilePath, ConstantParameters.xmlschematype_skos, outputFilePath, filterFacets, filterHierarchies);
    }
}
