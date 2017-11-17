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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import DB_Classes.DBGeneral;

import Utils.ConstantParameters;
import Utils.Linguist;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SortItem;
import Utils.Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;


import XMLHandling.AAT_SubjectClass.AAT_Subject_Kind_Enum;
import XMLHandling.AAT_TermLanguage.AAT_TermLanguage_Preferred_Enum;
import XMLHandling.AAT_TermLanguage.AAT_TermLanguage_Term_Type_Enum;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import neo4j_sisapi.CMValue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author tzortzak
 */
public class ParseFileData {

    public static final String UnlabeledPrefix = "Unlabeled_";
    
    private boolean PRINTXMLMESSAGES = false;

    public String readThesaurusName(String xmlFilePath, String xmlSchemaType) {
        String returnVal = "";

        try {

            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);



            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);

                        if (openingTagName.equals("Vocabulary")) {
                            returnVal = this.parseSpecificAttibuteValue("Title", xpp);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {


                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();
                boolean insideConceptScheme = false;
                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);

                        if (openingTagName.equals("skos:ConceptScheme")) {
                            insideConceptScheme = true;
                        } else if (insideConceptScheme && openingTagName.equals("skos:prefLabel")) {
                            returnVal = this.parseSimpleContentElement(xpp);
                            break;
                        }
                    }

                    eventType = xpp.next();
                }

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {



                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));



                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("data")) {
                            returnVal = this.parseSpecificAttibuteValue("thesaurus", xpp);
                            if(returnVal==null || returnVal.length()==0){
                                returnVal = this.parseSpecificAttibuteValue("ofThes", xpp);                                                                
                            }
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }
        return returnVal;
    }

    public void getAAT_SpecificSubjectIds(String xmlFilePath, String xmlSchemaType, ArrayList<String> targetSubjectIds) {
        if (xmlSchemaType != ConstantParameters.xmlschematype_aat) {
            return;

        }


        Utils.StaticClass.webAppSystemOutPrintln(ConstantParameters.xmlHeader);
        Utils.StaticClass.webAppSystemOutPrint("<results>");

        try {

            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));
            boolean insideSubject = false;
            String targetSubjectId = "";
            String targetFacetName = "";
            int eventType = xpp.getEventType();
            boolean showall = false;
            boolean textJustPrinted = false;
            int currentDepth = 0;

            //</editor-fold>
            while (eventType != xpp.END_DOCUMENT) {

                if (showall) {
                    if (eventType == xpp.START_TAG) {

                        currentDepth = xpp.getDepth();
                        Utils.StaticClass.webAppSystemOutPrint("\r\n");
                        for (int i = 0; i < currentDepth; i++) {
                            Utils.StaticClass.webAppSystemOutPrint("  ");
                        }
                        Utils.StaticClass.webAppSystemOutPrint("<" + xpp.getName() + ">");
                    } else if (eventType == xpp.END_TAG) {

                        if (textJustPrinted) {
                            Utils.StaticClass.webAppSystemOutPrint("</" + xpp.getName() + ">");
                            textJustPrinted = false;
                        } else {

                            Utils.StaticClass.webAppSystemOutPrint("\r\n");
                            currentDepth = xpp.getDepth();
                            for (int i = 0; i < currentDepth; i++) {
                                Utils.StaticClass.webAppSystemOutPrint("  ");
                            }

                            Utils.StaticClass.webAppSystemOutPrint("</" + xpp.getName() + ">");
                        }
                    } else if (eventType == xpp.TEXT) {

                        if (xpp.getText().trim().isEmpty() == false) {
                            Utils.StaticClass.webAppSystemOutPrint(Utilities.escapeXML(xpp.getText()));
                            textJustPrinted = true;
                        }
                    }
                }



                if (eventType == xpp.START_TAG) {
                    int depth = xpp.getDepth();

                    if (depth == 2) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                            targetSubjectId = this.parseSpecificAttibuteValue(ConstantParameters.aat_subject_id_attr, xpp);
                            insideSubject = true;
                            if (targetSubjectIds.contains(targetSubjectId)) {
                                showall = true;
                                textJustPrinted = false;
                                Utils.StaticClass.webAppSystemOutPrint("\r\n    <Subject Subject_ID=\"" + targetSubjectId + "\">");
                            }
                        }
                    }


                } else if (eventType == xpp.END_TAG) {
                    int depth = xpp.getDepth();
                    if (depth == 2) {
                        String closingTagName = this.closingTagEncoutered(xpp, null);
                        if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {

                            showall = false;

                        }
                    }

                }

                eventType = xpp.next();
            }


        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }


        Utils.StaticClass.webAppSystemOutPrintln("\r\n</results>");
    }

    public boolean readXMLFacetsInSortItems(String importThesaurusName, String xmlFilePath, String xmlSchemaType, HashMap<String,SortItem> xmlFacetSortItems) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Facets in sort Items from file: " + xmlFilePath + ".");

         
        try {

            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);


            //<editor-fold defaultstate="collapsed" desc="AAT reading not supported yet ...">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {

                Utils.StaticClass.webAppSystemOutPrintln("Reading of aat schema Facets in SortItem structures has not been implenmented yet.");
                return false;
                /*
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                boolean insideSubject = false;

                boolean insidefacet = false;
                boolean insidePrefferred = false;

                String targetSubjectId = "";
                String targetFacetName = "";
                int eventType = xpp.getEventType();

                //<editor-fold defaultstate="collapsed" desc="Show XML ...">

                boolean showall = false;
                boolean textPrinted = false;
                int currentDepth = 0;

                //</editor-fold>
                while (eventType != xpp.END_DOCUMENT) {

                    //<editor-fold defaultstate="collapsed" desc="Show XML">
                    
//                   if(showall){
//                    if (eventType == xpp.START_TAG) {
//
//                    currentDepth = xpp.getDepth();
//                    Utils.StaticClass.webAppSystemOutPrint("\r\n");
//                    for(int i=0; i<currentDepth;i++){
//                    Utils.StaticClass.webAppSystemOutPrint("  ");
//                    }
//                    Utils.StaticClass.webAppSystemOutPrint("<"+xpp.getName()+">");
//                    }
//                    else if(eventType ==xpp.END_TAG){
//
//                    if(textPrinted){
//                    Utils.StaticClass.webAppSystemOutPrintln("</"+xpp.getName()+">");
//                    textPrinted= false;
//                    }
//                    else{
//                    for(int i=0; i<currentDepth;i++){
//                    Utils.StaticClass.webAppSystemOutPrint("  ");
//                    }
//                    Utils.StaticClass.webAppSystemOutPrint("</"+xpp.getName()+">");
//                    }
//                    currentDepth--;
//                    }
//                    else if(eventType == xpp.TEXT){
//                    Utils.StaticClass.webAppSystemOutPrint(Utilities.escapeXML(xpp.getText()));
//                    textPrinted = true;
//                    }
//                    }
//
//                     
                    //</editor-fold>
                    if (eventType == xpp.START_TAG) {
                        int depth = xpp.getDepth();

                        if (depth == 2) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                targetSubjectId = this.parseSpecificAttibuteValue(ConstantParameters.aat_subject_id_attr, xpp);
                                insideSubject = true;
                                //<editor-fold defaultstate="collapsed" desc="Show XML">

//                                if(targetSubjectId.equals("300000000")
//                                || targetSubjectId.equals("300000201")
//                                || targetSubjectId.equals("300004789")
//                                || targetSubjectId.equals("300008346")
//                                || targetSubjectId.equals("300010357")
//                                || targetSubjectId.equals("300015646")
//                                || targetSubjectId.equals("300022238")
//                                || targetSubjectId.equals("300024978")
//                                || targetSubjectId.equals("300026029")
//                                || targetSubjectId.equals("300036743")
//                                || targetSubjectId.equals("300037221")
//                                || targetSubjectId.equals("300037335")
//                                || targetSubjectId.equals("300041619")
//                                || targetSubjectId.equals("300042929")
//                                || targetSubjectId.equals("300045611")
//                                || targetSubjectId.equals("300053001")
//                                || targetSubjectId.equals("300054134")
//                                || targetSubjectId.equals("300054593")
//                                || targetSubjectId.equals("300054722")
//                                || targetSubjectId.equals("300055126")
//                                || targetSubjectId.equals("300123558")
//                                || targetSubjectId.equals("300123559")
//                                || targetSubjectId.equals("300131647")
//                                || targetSubjectId.equals("300136012")
//                                || targetSubjectId.equals("300139081")
//                                || targetSubjectId.equals("300179869")
//                                || targetSubjectId.equals("300185711")
//                                || targetSubjectId.equals("300186269")
//                                || targetSubjectId.equals("300207851")
//                                || targetSubjectId.equals("300209261")
//                                || targetSubjectId.equals("300222468")
//                                || targetSubjectId.equals("300234770")
//                                || targetSubjectId.equals("300241489")
//                                || targetSubjectId.equals("300241490")
//                                || targetSubjectId.equals("300264550")
//                                || targetSubjectId.equals("300264551")
//                                || targetSubjectId.equals("300264552")
//                                || targetSubjectId.equals("300265673"))
//                                {
//                                showall=true;
//                                Utils.StaticClass.webAppSystemOutPrintln("<Subject Subject_ID=\""+targetSubjectId+"\">");
//                                }
//                                 

                                //</editor-fold>
                            }
                        } else if (insideSubject && depth == 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_record_type_tag)) {
                                String recType = this.parseSimpleContentElement(xpp);
                                if (recType.equals(ConstantParameters.aat_record_type_Facet_val)) {

                                    insidefacet = true;
                                }
                            }
                        } else if (insidefacet && depth > 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = true;
                            } else if (insidePrefferred && openingTagName.equals(ConstantParameters.aat_Term_Text_tag)) {
                                targetFacetName = this.parseSimpleContentElement(xpp);
                            }
                        }


                    } else if (eventType == xpp.END_TAG) {
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                if (insidefacet) {
                                    if (targetFacetName != null && targetFacetName.length() > 0 && xmlFacets.contains(targetFacetName) == false) {
                                        xmlFacets.add(targetFacetName);
                                    }
                                }
                                insidefacet = false;
                                insidePrefferred = false;
                                insideSubject = false;
                                targetSubjectId = "";
                                targetFacetName = "";


                                //<editor-fold defaultstate="collapsed" desc="Show XML">
                                //showall = false;
                                //</editor-fold>
                            }
                        } else if (insidePrefferred && depth > 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = false;
                            }
                        }

                    }

                    eventType = xpp.next();
                }
                */
            }
            //</editor-fold>
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="SKOS reading not supported yet ...">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                Utils.StaticClass.webAppSystemOutPrintln("Reading of SKOS schema Facets in SortItem structures has not been implenmented yet.");
                return false;
                /*
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                this.parseFacetNodes(xpp, xmlSchemaType, xmlFacets);
                */

            } 
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="THEMAS Facet reading...">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));



                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("facets")) {
                            return this.parseFacetNodesinSortItems(xpp, xmlSchemaType, xmlFacetSortItems);                            
                        }
                    }
                    eventType = xpp.next();
                }
            }
            //</editor-fold>

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Facet reading. Found " + xmlFacetSortItems.size() + " Facets.");
        return true;
    }

    
    
    public boolean readXMLFacets(String importThesaurusName, String xmlFilePath, String xmlSchemaType, ArrayList<String> xmlFacets) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Facets from file: " + xmlFilePath + ".");

        try {

            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);


            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {

                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                boolean insideSubject = false;

                boolean insidefacet = false;
                boolean insidePrefferred = false;

                String targetSubjectId = "";
                String targetFacetName = "";
                int eventType = xpp.getEventType();

                //<editor-fold defaultstate="collapsed" desc="Show XML ...">

                boolean showall = false;
                boolean textPrinted = false;
                int currentDepth = 0;

                //</editor-fold>
                while (eventType != xpp.END_DOCUMENT) {

                    //<editor-fold defaultstate="collapsed" desc="Show XML">
                    /*
                    if(showall){
                    if (eventType == xpp.START_TAG) {

                    currentDepth = xpp.getDepth();
                    Utils.StaticClass.webAppSystemOutPrint("\r\n");
                    for(int i=0; i<currentDepth;i++){
                    Utils.StaticClass.webAppSystemOutPrint("  ");
                    }
                    Utils.StaticClass.webAppSystemOutPrint("<"+xpp.getName()+">");
                    }
                    else if(eventType ==xpp.END_TAG){

                    if(textPrinted){
                    Utils.StaticClass.webAppSystemOutPrintln("</"+xpp.getName()+">");
                    textPrinted= false;
                    }
                    else{
                    for(int i=0; i<currentDepth;i++){
                    Utils.StaticClass.webAppSystemOutPrint("  ");
                    }
                    Utils.StaticClass.webAppSystemOutPrint("</"+xpp.getName()+">");
                    }
                    currentDepth--;
                    }
                    else if(eventType == xpp.TEXT){
                    Utils.StaticClass.webAppSystemOutPrint(Utilities.escapeXML(xpp.getText()));
                    textPrinted = true;
                    }
                    }

                     */
                    //</editor-fold>
                    if (eventType == xpp.START_TAG) {
                        int depth = xpp.getDepth();

                        if (depth == 2) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                targetSubjectId = this.parseSpecificAttibuteValue(ConstantParameters.aat_subject_id_attr, xpp);
                                insideSubject = true;
                                //<editor-fold defaultstate="collapsed" desc="Show XML">
                                /*
                                if(targetSubjectId.equals("300000000")
                                || targetSubjectId.equals("300000201")
                                || targetSubjectId.equals("300004789")
                                || targetSubjectId.equals("300008346")
                                || targetSubjectId.equals("300010357")
                                || targetSubjectId.equals("300015646")
                                || targetSubjectId.equals("300022238")
                                || targetSubjectId.equals("300024978")
                                || targetSubjectId.equals("300026029")
                                || targetSubjectId.equals("300036743")
                                || targetSubjectId.equals("300037221")
                                || targetSubjectId.equals("300037335")
                                || targetSubjectId.equals("300041619")
                                || targetSubjectId.equals("300042929")
                                || targetSubjectId.equals("300045611")
                                || targetSubjectId.equals("300053001")
                                || targetSubjectId.equals("300054134")
                                || targetSubjectId.equals("300054593")
                                || targetSubjectId.equals("300054722")
                                || targetSubjectId.equals("300055126")
                                || targetSubjectId.equals("300123558")
                                || targetSubjectId.equals("300123559")
                                || targetSubjectId.equals("300131647")
                                || targetSubjectId.equals("300136012")
                                || targetSubjectId.equals("300139081")
                                || targetSubjectId.equals("300179869")
                                || targetSubjectId.equals("300185711")
                                || targetSubjectId.equals("300186269")
                                || targetSubjectId.equals("300207851")
                                || targetSubjectId.equals("300209261")
                                || targetSubjectId.equals("300222468")
                                || targetSubjectId.equals("300234770")
                                || targetSubjectId.equals("300241489")
                                || targetSubjectId.equals("300241490")
                                || targetSubjectId.equals("300264550")
                                || targetSubjectId.equals("300264551")
                                || targetSubjectId.equals("300264552")
                                || targetSubjectId.equals("300265673"))
                                {
                                showall=true;
                                Utils.StaticClass.webAppSystemOutPrintln("<Subject Subject_ID=\""+targetSubjectId+"\">");
                                }
                                 */

                                //</editor-fold>
                            }
                        } else if (insideSubject && depth == 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_record_type_tag)) {
                                String recType = this.parseSimpleContentElement(xpp);
                                if (recType.equals(ConstantParameters.aat_record_type_Facet_val)) {

                                    insidefacet = true;
                                }
                            }
                        } else if (insidefacet && depth > 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = true;
                            } else if (insidePrefferred && openingTagName.equals(ConstantParameters.aat_Term_Text_tag)) {
                                targetFacetName = this.parseSimpleContentElement(xpp);
                            }
                        }


                    } else if (eventType == xpp.END_TAG) {
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                if (insidefacet) {
                                    if (targetFacetName != null && targetFacetName.length() > 0 && xmlFacets.contains(targetFacetName) == false) {
                                        xmlFacets.add(targetFacetName);
                                    }
                                }
                                insidefacet = false;
                                insidePrefferred = false;
                                insideSubject = false;
                                targetSubjectId = "";
                                targetFacetName = "";


                                //<editor-fold defaultstate="collapsed" desc="Show XML">
                                //showall = false;
                                //</editor-fold>
                            }
                        } else if (insidePrefferred && depth > 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = false;
                            }
                        }

                    }

                    eventType = xpp.next();
                }

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {


                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                this.parseFacetNodes(xpp, xmlSchemaType, xmlFacets);

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {



                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));



                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("facets")) {
                            this.parseFacetNodes(xpp, xmlSchemaType, xmlFacets);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of Facet reading. Found " + xmlFacets.size() + " Facets.");
        return true;
    }

    public AAT_SubjectTermClass parseAAT_Term(XmlPullParser xpp) {
        AAT_SubjectTermClass returnTerm = new AAT_SubjectTermClass();


        String openingTagName = xpp.getName();
        if (openingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
            returnTerm.isPreffered = true;
        } else if (openingTagName.equals(ConstantParameters.aat_Non_Preferred_Term_tag)) {
            returnTerm.isPreffered = false;
        } else {
            return null;
        }

        try {
            int eventType = xpp.next();
            while (eventType != xpp.END_DOCUMENT) {

                if (eventType == xpp.START_TAG) {
                    openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.aat_Term_Text_tag)) {
                        returnTerm.termName = this.parseSimpleContentElement(xpp);
                    } else if (openingTagName.equals(ConstantParameters.aat_Term_Language_tag)) {
                        AAT_TermLanguage newLangVal = this.parseAAT_TermLanguageNode(xpp);
                        if (newLangVal != null) {
                            returnTerm.langCodes.add(newLangVal);
                        }
                    } else if (openingTagName.equals(ConstantParameters.aat_Term_Source_ID_tag)) {
                        String newSourceId = this.parseSimpleContentElement(xpp);
                        if (newSourceId != null && returnTerm.termSources.contains(newSourceId) == false) {
                            returnTerm.termSources.add(newSourceId);
                        }
                    }

                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.aat_Preferred_Term_tag) || closingTagName.equals(ConstantParameters.aat_Non_Preferred_Term_tag)) {
                        break;
                    }
                }
                eventType = xpp.next();
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }

        return returnTerm;
    }

    public ArrayList<AAT_SubjectTermClass> parseAAT_DescriptiveNotes(XmlPullParser xpp) {
        ArrayList<AAT_SubjectTermClass> returnVals = new ArrayList<AAT_SubjectTermClass>();

        String openingTagName = xpp.getName();
        if (openingTagName.equals(ConstantParameters.aat_Descriptive_NotesWrapper_tag) == false) {
            return null;
        }

        AAT_SubjectTermClass newTermClass = new AAT_SubjectTermClass();
        boolean insideDescriptiveNote = false;

        try {
            int eventType = xpp.next();
            while (eventType != xpp.END_DOCUMENT) {

                if (eventType == xpp.START_TAG) {
                    openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.aat_Descriptive_Note_tag)) {
                        newTermClass = new AAT_SubjectTermClass();
                        insideDescriptiveNote = true;

                    } else if (insideDescriptiveNote && openingTagName.equals(ConstantParameters.aat_Descriptive_Note_Note_Text_tag)) {
                        String note = this.parseSimpleContentElement(xpp);
                        if (note != null && note.length() > 0) {
                            newTermClass.termName = note;
                        }
                    } else if (insideDescriptiveNote && openingTagName.equals(ConstantParameters.aat_Note_Language_tag)) {
                        AAT_TermLanguage newLangVal = new AAT_TermLanguage();
                        newLangVal.languageCode = Linguist.AATLanguageAcronyms(this.parseSimpleContentElement(xpp));
                        if (newLangVal.languageCode != null && newLangVal.languageCode.length() > 0) {
                            newTermClass.langCodes.add(newLangVal);
                        }
                    } else if (insideDescriptiveNote && openingTagName.equals(ConstantParameters.aat_Note_Source_ID_tag)) {
                        String source = this.parseSimpleContentElement(xpp);
                        if (source != null && newTermClass.termSources.contains(source) == false) {
                            newTermClass.termSources.add(source);
                        }
                    }
                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.aat_Descriptive_Note_tag)) {
                        insideDescriptiveNote = false;
                        if (newTermClass != null && newTermClass.termName != null && newTermClass.termName.length() > 0 && newTermClass.langCodes.size() > 0) {
                            returnVals.add(newTermClass);
                        }
                    } else if (closingTagName.equals(ConstantParameters.aat_Descriptive_NotesWrapper_tag)) {
                        break;
                    }
                }

                eventType = xpp.next();

            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }



        return returnVals;

    }

    public ArrayList<AAT_RevisionClass> parseAAT_RevisionHistory(XmlPullParser xpp) {
        ArrayList<AAT_RevisionClass> returnVals = new ArrayList<AAT_RevisionClass>();

        String openingTagName = xpp.getName();
        if (openingTagName.equals(ConstantParameters.aat_Revision_History_Wrapper_tag) == false) {
            return null;
        }

        AAT_RevisionClass newRev = new AAT_RevisionClass();
        boolean insideRevision = false;

        try {
            int eventType = xpp.next();
            while (eventType != xpp.END_DOCUMENT) {

                if (eventType == xpp.START_TAG) {
                    openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.aat_Revision_tag)) {
                        newRev = new AAT_RevisionClass();
                        insideRevision = true;

                    } else if (insideRevision && openingTagName.equals(ConstantParameters.aat_Revision_Action_tag)) {
                        String action = this.parseSimpleContentElement(xpp);
                        if (action != null && action.length() > 0) {
                            newRev.action = action;
                        }
                    } else if (insideRevision && openingTagName.equals(ConstantParameters.aat_Revision_User_Name_tag)) {
                        String uname = this.parseSimpleContentElement(xpp);
                        if (uname != null && uname.length() > 0) {
                            newRev.userName = uname;
                        }
                    } else if (insideRevision && openingTagName.equals(ConstantParameters.aat_Revision_Date_tag)) {
                        String date = this.parseSimpleContentElement(xpp);
                        if (date != null && date.length() > 0) {
                            newRev.setDate(date);
                        }
                    }
                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.aat_Revision_tag)) {
                        insideRevision = false;
                        if (newRev != null && newRev.userName != null && newRev.userName.length() > 0) {
                            returnVals.add(newRev);
                        }
                    } else if (closingTagName.equals(ConstantParameters.aat_Revision_History_Wrapper_tag)) {
                        break;
                    }
                }

                eventType = xpp.next();

            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }



        return returnVals;
    }

    public ArrayList<String> parseAAT_Associative_Relationships(XmlPullParser xpp) {
        ArrayList<String> returnVals = new ArrayList<String>();


        String openingTagName = xpp.getName();
        if (openingTagName.equals(ConstantParameters.aat_Associative_Relationships_tag) == false) {
            return null;
        }

        try {
            int eventType = xpp.next();
            while (eventType != xpp.END_DOCUMENT) {

                if (eventType == xpp.START_TAG) {
                    openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.aat_Associative_Relationships_Subject_ID_tag)) {
                        String relVal = this.parseSimpleContentElement(xpp);
                        if (relVal != null && relVal.length() > 0 && returnVals.contains(relVal) == false) {
                            returnVals.add(relVal);
                        }
                    }
                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.aat_Associative_Relationships_tag)) {
                        break;
                    }
                }
                eventType = xpp.next();
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }

        return returnVals;
    }

    public AAT_TermLanguage parseAAT_TermLanguageNode(XmlPullParser xpp) {
        AAT_TermLanguage returnVal = new AAT_TermLanguage();

        String openingTagName = xpp.getName();
        if (openingTagName.equals(ConstantParameters.aat_Term_Language_tag) == false) {
            return null;
        }

        try {
            int eventType = xpp.next();
            while (eventType != xpp.END_DOCUMENT) {

                if (eventType == xpp.START_TAG) {
                    openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.aat_Term_Language_Language_tag)) {
                        String languageText = this.parseSimpleContentElement(xpp);
                        String langCode = Linguist.AATLanguageAcronyms(languageText);
                        returnVal.languageText = languageText;
                        returnVal.languageCode = langCode;
                    } else if (openingTagName.equals(ConstantParameters.aat_Term_Language_Preferred_tag)) {
                        String value = this.parseSimpleContentElement(xpp);

                        /*
                        if(value.equals(ConstantParameters.aat_Term_Language_Preferred_Undetermined_val)){
                        returnVal.preferredTag = AAT_TermLanguage_Preferred_Enum.UNDETERMINED;
                        }
                        else
                         */
                        if (value.equals(ConstantParameters.aat_Term_Language_Preferred_Preferred_val)) {
                            returnVal.preferredTag = AAT_TermLanguage_Preferred_Enum.PREFERRED;
                        } else if (value.equals(ConstantParameters.aat_Term_Language_Preferred_Non_Preferred_val)) {
                            returnVal.preferredTag = AAT_TermLanguage_Preferred_Enum.NON_PREFERRED;
                        }
                    } else if (openingTagName.equals(ConstantParameters.aat_Term_Language_Term_Type_tag)) {
                        String value = this.parseSimpleContentElement(xpp);

                        if (value.equals(ConstantParameters.aat_Term_Language_Term_Type_Descriptor_val)) {
                            returnVal.termType = AAT_TermLanguage_Term_Type_Enum.DESCRIPTOR;
                        } else if (value.equals(ConstantParameters.aat_Term_Language_Term_Type_Used_For_Term_val)) {
                            returnVal.termType = AAT_TermLanguage_Term_Type_Enum.USED_FOR_TERM;
                        } else if (value.equals(ConstantParameters.aat_Term_Language_Term_Type_Alternate_Descriptor_val)) {
                            returnVal.termType = AAT_TermLanguage_Term_Type_Enum.ALTERNATE_DESCRIPTOR;
                        }

                    }
                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.aat_Term_Language_tag)) {
                        break;
                    }
                }
                eventType = xpp.next();
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {

            Utils.StaticClass.handleException(ex);
        }

        if (returnVal != null && returnVal.languageCode != null && returnVal.languageCode.length() > 0) {
            return returnVal;
        } else {
            return null;
        }

    }

    public boolean readXMLTerms(String xmlFilePath, String xmlSchemaType, HashMap<String, NodeInfoStringContainer> termsInfo,
            HashMap<String, String> languageSelections) {

        String[] output = Utilities.getSortedTermAllOutputArray();
        
        Utilities u = new Utilities();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "readXMLTerms Started at " + Utilities.GetNow()); // JUST READ WHTAEVER YOU SEE


        try {

            XmlPullParserFactory factory;


            // <editor-fold defaultstate="collapsed" desc="aat case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {
            } // <editor-fold defaultstate="collapsed" desc="skos case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {
                HashMap<String, String> idsToNames = new HashMap<String, String>();

                // <editor-fold defaultstate="collapsed" desc="parse All ids">
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();

                String targetId = "";
                String targetPrefferedName = "";

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {

                            if (openingTagName.equals(ConstantParameters.XML_skos_collection)) {
                                targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            } else if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            }
                        } else if (depth == 3) {
                            String targetLangCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));

                            if (openingTagName.equals(ConstantParameters.XML_skos_prefLabel) && targetLangCode != null && targetLangCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                targetPrefferedName = this.parseSimpleContentElement(xpp);
                            }
                        }
                    } else if (eventType == xpp.END_TAG) {
                        String closingTagName = this.closingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            if (closingTagName.equals(ConstantParameters.XML_skos_collection) || closingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                if (targetId != null && targetId.length() > 0) {

                                    if (idsToNames.containsKey(targetId)) {
                                        Utils.StaticClass.webAppSystemOutPrintln("Dublicate key found: " + targetId);
                                        String existingName = idsToNames.get(targetId);
                                        if (existingName != null && existingName.length() > 0 && existingName.equals(targetId) == false) {
                                            if (targetPrefferedName != null && targetPrefferedName.length() > 0 && existingName.equals(existingName) == false) {
                                                Utils.StaticClass.webAppSystemOutPrintln("More than one Name found for id: " + targetId + "  Names: " + existingName + ", " + targetPrefferedName);
                                            } else {
                                                //do nothing keep it as it is
                                                //idsToNames.put(targetId, existingName);
                                            }
                                        }
                                    } else {
                                        if (targetPrefferedName != null && targetPrefferedName.length() > 0) {
                                            idsToNames.put(targetId, targetPrefferedName);
                                        } else {
                                            idsToNames.put(targetId, targetId);
                                        }
                                    }
                                }
                                targetId = "";
                                targetPrefferedName = "";
                            }
                        }
                    }
                    eventType = xpp.next();
                }
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="terms parsing with ids">

                //HashMap<String, NodeInfoStringContainer> termsInfoIds = new HashMap<String, NodeInfoStringContainer>();
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(false);
                xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                this.parseTermNodes(xpp, xmlSchemaType, termsInfo, Parameters.TRANSLATION_SEPERATOR, output, idsToNames, languageSelections);
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Abandoned convert ids to names">

                // </editor-fold>
            } // </editor-fold>
            //</editor-fold>
            // <editor-fold defaultstate="collapsed" desc="themas case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {
                String translationSeparator = "";


                // <editor-fold defaultstate="collapsed" desc="find translations seperator">

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("TranslationCategories") || openingTagName.equals("data")) {
                            translationSeparator = this.parseSpecificAttibuteValue("translationSeperator", xpp);
                            if (translationSeparator != null && translationSeparator.trim().length() > 0) {
                                break;
                            }
                        }
                    }
                    eventType = xpp.next();
                }

                if (translationSeparator == null || translationSeparator.trim().length() == 0) {
                    translationSeparator = Parameters.TRANSLATION_SEPERATOR;
                }
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="parse terms">
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(true);
                xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.XMLTermsWrapperElementName)) {
                            return this.parseTermNodes(xpp, xmlSchemaType, termsInfo, translationSeparator, output, null, languageSelections);                            
                        }
                    }
                    eventType = xpp.next();
                }

                // </editor-fold>

            }
            // </editor-fold>

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "readXMLTerms Successfully Ended at " + Utilities.GetNow() + " found " + termsInfo.size() + " terms.");
        return true;
    }

    public boolean readXMLHierarchies(String importThesaurusName,
            String xmlFilePath, String xmlSchemaType,
            HashMap<String, ArrayList<String>> hierarchyFacets, ArrayList<String> xmlFacets) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Hierarchies from file: " + xmlFilePath + ".");

        int mainLanguageNotFound = 0;


        try {

            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);


            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                boolean insideSubject = false;
                boolean insideHierarchy = false;
                boolean insidePrefferred = false;
                String targetSubjectId = "";
                String targetHierarchyName = "";
                ArrayList<String> hierFacets = new ArrayList<String>();
                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        int depth = xpp.getDepth();

                        if (depth == 2) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                targetSubjectId = this.parseSpecificAttibuteValue(ConstantParameters.aat_subject_id_attr, xpp);
                                insideSubject = true;
                            }
                        } else if (insideSubject && depth == 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_record_type_tag)) {
                                String recType = this.parseSimpleContentElement(xpp);
                                if (recType.equals(ConstantParameters.aat_record_type_Hierarchy_Name_val)) {

                                    insideHierarchy = true;
                                }
                            } else {
                                if (openingTagName.equals(ConstantParameters.aat_Hierarchy_tag)) {
                                    String facetStr = this.parseSimpleContentElement(xpp);

                                    if (facetStr != null && facetStr.length() > 0 && hierFacets.contains(facetStr) == false) {
                                        hierFacets.add(facetStr);
                                    }
                                }
                            }
                        } else if (insideHierarchy && depth > 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = true;
                            } else if (insidePrefferred && openingTagName.equals(ConstantParameters.aat_Term_Text_tag)) {

                                targetHierarchyName = this.parseSimpleContentElement(xpp);
                            }
                        }


                    } else if (eventType == xpp.END_TAG) {
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {


                                if (insideHierarchy) {

                                    if (targetHierarchyName != null && targetHierarchyName.length() > 0 && hierarchyFacets.containsKey(targetHierarchyName) == false) {
                                        hierarchyFacets.put(targetHierarchyName, new ArrayList<String>());
                                    }

                                    ArrayList<String> existingFacets = new ArrayList<String>();
                                    if (hierarchyFacets.containsKey(targetHierarchyName)) {
                                        existingFacets.addAll(hierarchyFacets.get(targetHierarchyName));
                                    }

                                    for (int k = 0; k < hierFacets.size(); k++) {
                                        String facet = hierFacets.get(k);

                                        String[] parts = facet.split("\\|");
                                        for (int p = 0; p < parts.length; p++) {
                                            String facetStr = parts[p];
                                            if (facetStr != null) {
                                                facetStr = facetStr.trim();


                                                if (facetStr.equals(targetHierarchyName) == false) {
                                                    /*
                                                    if(xmlFacets.contains(facetStr)==false){
                                                    xmlFacets.add(facetStr);
                                                    //Utils.StaticClass.webAppSystemOutPrintln("Adding "+facetStr);
                                                    }
                                                     */
                                                    if (existingFacets.contains(facetStr) == false) {
                                                        existingFacets.add(facetStr);
                                                    }
                                                }
                                            }
                                        }


                                    }
                                    hierarchyFacets.put(targetHierarchyName, existingFacets);

                                }
                                targetSubjectId = "";
                                targetHierarchyName = "";
                                insideHierarchy = false;
                                insideSubject = false;
                                hierFacets = new ArrayList<String>();
                            }
                        } else {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                insidePrefferred = false;
                            }
                        }

                    }

                    eventType = xpp.next();
                }



                //aat allows hierarchy under hierarchy. We will not allow this
                //The subhierarchy will be considered as simple term

                ArrayList<String> hierarchiesToRemove = new ArrayList<String>();
                //clean sub hierarchies
                Iterator<String> allhiers = hierarchyFacets.keySet().iterator();
                while (allhiers.hasNext()) {
                    targetHierarchyName = allhiers.next();
                    ArrayList<String> facets = hierarchyFacets.get(targetHierarchyName);

                    for (int j = 0; j < facets.size(); j++) {
                        String facetStr = facets.get(j);
                        if (hierarchyFacets.containsKey(facetStr)) {
                            if (hierarchiesToRemove.contains(targetHierarchyName) == false) {
                                hierarchiesToRemove.add(targetHierarchyName);
                            }
                        } else {
                            if (xmlFacets.contains(facetStr) == false) {
                                xmlFacets.add(facetStr);
                            }
                        }
                    }
                }

                for (int j = 0; j < hierarchiesToRemove.size(); j++) {
                    targetHierarchyName = hierarchiesToRemove.get(j);
                    if (hierarchyFacets.containsKey(targetHierarchyName)) {
                        hierarchyFacets.remove(targetHierarchyName);
                    }
                }

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                HashMap<String, String> idsToNames = new HashMap<String, String>();

                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();

                String targetId = "";
                String targetPrefferedName = "";

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {

                            if (openingTagName.equals(ConstantParameters.XML_skos_collection)) {
                                targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            } else if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            }
                        } else if (depth == 3) {
                            String targetLangCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));

                            if (openingTagName.equals(ConstantParameters.XML_skos_prefLabel) && targetLangCode != null
                                    && targetLangCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                targetPrefferedName = this.parseSimpleContentElement(xpp);

                                /*
                                if(targetId!=null && targetId.length()>0 &&
                                idsToNames.containsKey(targetId)==false &&
                                targetPrefferedName!=null && targetPrefferedName.length()>0){
                                idsToNames.put(targetId, targetPrefferedName);
                                }
                                else{
                                Utils.StaticClass.webAppSystemOutPrintln("probably dublicate name encountered for id" +targetId );
                                }
                                 *
                                 */
                            }
                        }
                    } else if (eventType == xpp.END_TAG) {
                        String closingTagName = this.closingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            if (closingTagName.equals(ConstantParameters.XML_skos_collection) || closingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                if (targetId != null && targetId.length() > 0) {

                                    if (idsToNames.containsKey(targetId)) {
                                        Utils.StaticClass.webAppSystemOutPrintln("Dublicate key found: " + targetId);
                                        String existingName = idsToNames.get(targetId);
                                        if (existingName != null && existingName.length() > 0 && existingName.equals(targetId) == false) {
                                            if (targetPrefferedName != null && targetPrefferedName.length() > 0 && existingName.equals(existingName) == false) {
                                                Utils.StaticClass.webAppSystemOutPrintln("More than one Name found for id: " + targetId + "  Names: " + existingName + ", " + targetPrefferedName);
                                            } else {
                                                //do nothing keep it as it is
                                                //idsToNames.put(targetId, existingName);
                                            }
                                        }
                                    } else {
                                        if (targetPrefferedName != null && targetPrefferedName.length() > 0) {
                                            idsToNames.put(targetId, targetPrefferedName);
                                        } else {
                                            mainLanguageNotFound++;
                                            idsToNames.put(targetId, targetId);
                                            //idsToNames.put(targetId,"Undefined"+mainLanguageNotFound);
                                        }
                                    }
                                }
                                targetId = "";
                                targetPrefferedName = "";
                            }
                        }
                    }

                    eventType = xpp.next();
                }

                Utils.StaticClass.webAppSystemOutPrintln(idsToNames.size() + " terms found undefined in main language: " + mainLanguageNotFound);

                HashMap<String, ArrayList<String>> hierarchyFacetsIds = new HashMap<String, ArrayList<String>>();
                factory.setNamespaceAware(false);
                xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));
                this.parseHierarchyNodes(xpp, xmlSchemaType, hierarchyFacetsIds, xmlFacets);

                //now replace ids with values
                Iterator<String> enum1 = hierarchyFacetsIds.keySet().iterator();
                while (enum1.hasNext()) {
                    String hierarchyId = enum1.next();
                    ArrayList<String> facetIds = hierarchyFacetsIds.get(hierarchyId);

                    String hierarchyName = idsToNames.get(hierarchyId);
                    ArrayList<String> facetNames = new ArrayList<String>();
                    for (int k = 0; k < facetIds.size(); k++) {
                        String facetName = idsToNames.get(facetIds.get(k));
                        if (facetName != null && facetName.length() > 0 && facetNames.contains(facetName) == false) {
                            facetNames.add(facetName);

                        } else {
                            Utils.StaticClass.webAppSystemOutPrintln("Skipping Facet with id " + facetIds.get(k) + " for heirarchy with id " + hierarchyId);
                        }
                    }

                    if (hierarchyFacets.containsKey(hierarchyName) == false) {
                        hierarchyFacets.put(hierarchyName, facetNames);
                    } else {
                        ArrayList<String> existingFacets = hierarchyFacets.get(hierarchyName);
                        for (int k = 0; k < existingFacets.size(); k++) {
                            if (facetNames.contains(existingFacets.get(k)) == false) {
                                facetNames.add(existingFacets.get(k));
                            }
                        }
                    }



                }

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("hierarchies")) {
                            this.parseHierarchyNodes(xpp, xmlSchemaType, hierarchyFacets, xmlFacets);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading Hierarchies. Found: " + hierarchyFacets.size() + " hierarchies.");
        return true;
    }

    public boolean readXMLSources(String xmlFilePath, String xmlSchemaType, HashMap<String, String> XMLsources) {

        DBGeneral dbGen = new DBGeneral();
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Sources from file: " + xmlFilePath + ".");

        XmlPullParserFactory factory;
        try {
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {
            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.XMLSourcesWrapperElementName)) {
                            this.parseSourceNodes(xpp, XMLsources);
                            break;
                        }
                    }

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.XMLSourcesWrapperElementName)) {
                            this.parseTermNodesSources(xpp, XMLsources);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            }

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading Sources.");
        return true;
    }

    public ArrayList<String> findGuideTermRelatedBts(HashMap<String, AAT_SubjectClass> allSubjects, AAT_SubjectClass guideTermClass) {
        ArrayList<String> returnVals = new ArrayList<String>();

        if (guideTermClass != null && guideTermClass.parentSubjectIds != null) {
            for (int i = 0; i < guideTermClass.parentSubjectIds.size(); i++) {
                String btId = guideTermClass.parentSubjectIds.get(i);
                if (allSubjects.containsKey(btId)) {
                    AAT_SubjectClass cadidateBt = allSubjects.get(btId);

                    if (cadidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_HIERARCHY
                            || cadidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_CONCEPT) {

                        String nameVal = cadidateBt.SubjectPreferredTermName.termName;
                        if (nameVal != null && nameVal.length() > 0 && returnVals.contains(nameVal) == false) {
                            returnVals.add(nameVal);
                        }
                    } else if (cadidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_GUIDE_TERM) {

                        ArrayList<String> newBts = findGuideTermRelatedBts(allSubjects, cadidateBt);
                        for (int k = 0; k < newBts.size(); k++) {
                            String val = newBts.get(k);
                            if (val != null && val.length() > 0 && returnVals.contains(val) == false) {
                                returnVals.add(val);
                            }
                        }
                    }


                }

            }
        }

        return returnVals;
    }

    public boolean readAAT_XML(String xmlFilePath, String xmlSchemaType,
            ArrayList<String> xmlFacets,
            HashMap<String, ArrayList<String>> hierarchyFacets,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> guideTerms,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations) {
        XmlPullParserFactory factory;
        try {

            //<editor-fold defaultstate="collapsed" desc="AAT Case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {
                DBGeneral dbGen = new DBGeneral();

                String[] output = {ConstantParameters.bt_kwd, 
                    ConstantParameters.nt_kwd, 
                    ConstantParameters.rt_kwd, 
                    ConstantParameters.uf_kwd,
                    ConstantParameters.tc_kwd, 
                    ConstantParameters.translation_kwd, 
                    ConstantParameters.status_kwd, 
                    ConstantParameters.uf_translations_kwd,
                    ConstantParameters.primary_found_in_kwd,
                    ConstantParameters.translations_found_in_kwd, 
                    ConstantParameters.created_by_kwd, 
                    ConstantParameters.created_on_kwd, 
                    ConstantParameters.modified_by_kwd,
                    ConstantParameters.modified_on_kwd, 
                    ConstantParameters.scope_note_kwd, 
                    ConstantParameters.translations_scope_note_kwd,
                    ConstantParameters.historical_note_kwd,
                    ConstantParameters.comment_kwd,
                    ConstantParameters.note_kwd
                };


                HashMap<String, AAT_SubjectClass> allSubjects = new HashMap<String, AAT_SubjectClass>();

                //<editor-fold defaultstate="collapsed" desc="Parse XML">
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                boolean insideSubject = false;
                AAT_SubjectClass targetSubjectClass = new AAT_SubjectClass();

                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        int depth = xpp.getDepth();

                        if (depth == 2) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                targetSubjectClass.SubjectId = this.parseSpecificAttibuteValue(ConstantParameters.aat_subject_id_attr, xpp);
                                insideSubject = true;
                            }


                        } else if (insideSubject) {
                            if (depth == 3) {

                                String openingTagName = this.openingTagEncoutered(xpp, null);
                                if (openingTagName.equals(ConstantParameters.aat_record_type_tag)) {

                                    String recType = this.parseSimpleContentElement(xpp);
                                    if (recType.equals(ConstantParameters.aat_record_type_Facet_val)) {
                                        targetSubjectClass.SubjectKind = AAT_Subject_Kind_Enum.KIND_FACET;
                                    } else if (recType.equals(ConstantParameters.aat_record_type_Hierarchy_Name_val)) {
                                        targetSubjectClass.SubjectKind = AAT_Subject_Kind_Enum.KIND_HIERARCHY;
                                    } else if (recType.equals(ConstantParameters.aat_record_type_Guide_Term_val)) {
                                        targetSubjectClass.SubjectKind = AAT_Subject_Kind_Enum.KIND_GUIDE_TERM;
                                    } else if (recType.equals(ConstantParameters.aat_record_type_Concept_val)) {
                                        targetSubjectClass.SubjectKind = AAT_Subject_Kind_Enum.KIND_CONCEPT;
                                    } else {
                                        Utils.StaticClass.webAppSystemOutPrintln("Undefined Recorde Type encoutered: " + recType);
                                    }
                                } else if (openingTagName.equals(ConstantParameters.aat_Associative_Relationships_tag)) {
                                    
                                    ArrayList<String> relSubjectIds = parseAAT_Associative_Relationships(xpp);
                                    if (relSubjectIds != null) {
                                        targetSubjectClass.associatedSubjectIds.addAll(relSubjectIds);
                                    }
                                } else if (openingTagName.equals(ConstantParameters.aat_Descriptive_NotesWrapper_tag)) {
                                    ArrayList<AAT_SubjectTermClass> scopeNotes = this.parseAAT_DescriptiveNotes(xpp);
                                    if (scopeNotes != null) {
                                        targetSubjectClass.descriptiveNotes.addAll(scopeNotes);
                                    }
                                } else if (openingTagName.equals(ConstantParameters.aat_Revision_History_Wrapper_tag)) {
                                    ArrayList<AAT_RevisionClass> newRevs = this.parseAAT_RevisionHistory(xpp);
                                    if (newRevs != null) {
                                        targetSubjectClass.contributors.addAll(newRevs);
                                    }
                                    
                                }
                            } else if (depth > 3) {
                                String openingTagName = this.openingTagEncoutered(xpp, null);
                                if (openingTagName.equals(ConstantParameters.aat_Preferred_Term_tag)) {
                                    AAT_SubjectTermClass prefVal = this.parseAAT_Term(xpp);
                                    if (prefVal != null) {
                                        targetSubjectClass.SubjectPreferredTermName = prefVal;
                                    }
                                } else if (openingTagName.equals(ConstantParameters.aat_Non_Preferred_Term_tag)) {
                                    AAT_SubjectTermClass nonPrefVal = this.parseAAT_Term(xpp);
                                    if (nonPrefVal != null) {
                                        targetSubjectClass.nonPreferredTermNames.add(nonPrefVal);
                                    }
                                } else if (openingTagName.equals(ConstantParameters.aat_Parent_Subject_ID_tag)) {
                                    String targetParentSubjectId = this.parseSimpleContentElement(xpp);
                                    if (targetSubjectClass.parentSubjectIds.contains(targetParentSubjectId) == false) {
                                        targetSubjectClass.parentSubjectIds.add(targetParentSubjectId);
                                    }
                                }

                            }

                        }

                    } else if (eventType == xpp.END_TAG) {
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                if (targetSubjectClass != null && targetSubjectClass.SubjectId != null && targetSubjectClass.SubjectId.length() > 0) {
                                    if (allSubjects.containsKey(targetSubjectClass.SubjectId) == false) {
                                        allSubjects.put(targetSubjectClass.SubjectId, targetSubjectClass);
                                    } else {
                                        Utils.StaticClass.webAppSystemOutPrintln("Dublicate SubjectId encoutered: " + targetSubjectClass.SubjectId);
                                    }
                                }

                                targetSubjectClass = new AAT_SubjectClass();
                                insideSubject = false;
                            }
                        }

                    }

                    eventType = xpp.next();
                }

                //</editor-fold>

                //Now startProcessing of allSubjects
                int unlabeledCounter = 1;

                // <editor-fold defaultstate="collapsed" desc="facets">
                //find facets
                Iterator<String> subjectEnum = allSubjects.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetSubjectId = subjectEnum.next();
                    AAT_SubjectClass targetClass = allSubjects.get(targetSubjectId);

                    if (targetClass.SubjectKind == AAT_Subject_Kind_Enum.KIND_FACET) {
                        String targetName = targetClass.SubjectPreferredTermName.termName;

                        boolean mainLangFound = true;
                        if (targetClass.SubjectPreferredTermName.langCodes != null && targetClass.SubjectPreferredTermName.langCodes.size() > 0) {
                            mainLangFound = false;
                            for (int k = 0; k < targetClass.SubjectPreferredTermName.langCodes.size(); k++) {
                                AAT_TermLanguage checkLang = targetClass.SubjectPreferredTermName.langCodes.get(k);
                                if (checkLang.languageCode != null && checkLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                    mainLangFound = true;
                                    break;
                                }
                            }
                        }
                        if (mainLangFound == false) {


                            String renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";

                            allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                            Utils.StaticClass.webAppSystemOutPrintln("Main Language Not Found for facet: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            targetName = renamedTo;
                        }

                        if (targetName != null && targetName.length() > 0) {

                            String renamedTo = targetName;
                            while (xmlFacets.contains(renamedTo)) {
                                renamedTo = UnlabeledPrefix + (unlabeledCounter++) +"(" + targetName + ")";
                                allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                                Utils.StaticClass.webAppSystemOutPrintln("Dublicate Facet Name encoutered: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            }
                            xmlFacets.add(renamedTo);

                        } else {
                            Utils.StaticClass.webAppSystemOutPrintln("Empty Facet Name encoutered for Subject with id: " + targetSubjectId);
                        }
                    }
                }

                Utils.StaticClass.webAppSystemOutPrintln("Found " + xmlFacets.size() + " Facets.");

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="hierarchies">

                //find hierarchies
                subjectEnum = allSubjects.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetSubjectId = subjectEnum.next();
                    AAT_SubjectClass targetClass = allSubjects.get(targetSubjectId);

                    if (targetClass.SubjectKind == AAT_Subject_Kind_Enum.KIND_HIERARCHY) {
                        String targetName = targetClass.SubjectPreferredTermName.termName;

                        boolean mainLangFound = true;
                        if (targetClass.SubjectPreferredTermName.langCodes != null && targetClass.SubjectPreferredTermName.langCodes.size() > 0) {
                            mainLangFound = false;
                            for (int k = 0; k < targetClass.SubjectPreferredTermName.langCodes.size(); k++) {
                                AAT_TermLanguage checkLang = targetClass.SubjectPreferredTermName.langCodes.get(k);
                                if (checkLang.languageCode != null && checkLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                    mainLangFound = true;
                                    break;
                                }
                            }
                        }
                        if (mainLangFound == false) {

                            String renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";
                            allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                            Utils.StaticClass.webAppSystemOutPrintln("Main Language Not Found for hierarchy: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            targetName = renamedTo;

                        }



                        boolean isValidHierarchy = true;//true if it is not placed under some other Hierarchy
                        ArrayList<String> parentIds = targetClass.parentSubjectIds;
                        ArrayList<String> finalHierarchyFacets = new ArrayList<String>();
                        if (parentIds != null) {
                            for (int k = 0; k < parentIds.size(); k++) {
                                String checkParentId = parentIds.get(k);
                                if (allSubjects.containsKey(checkParentId) && checkParentId.equals(targetSubjectId) == false) {

                                    AAT_SubjectClass candidateFacet = allSubjects.get(checkParentId);
                                    if (candidateFacet.SubjectKind == AAT_Subject_Kind_Enum.KIND_FACET) {
                                        String facetName = candidateFacet.SubjectPreferredTermName.termName;
                                        if (facetName != null && facetName.length() > 0 && finalHierarchyFacets.contains(facetName) == false) {
                                            finalHierarchyFacets.add(facetName);
                                        }
                                        continue;
                                    } else {
                                        isValidHierarchy = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isValidHierarchy) {
                            if (targetName != null && targetName.length() > 0) {

                                String renamedTo = targetName;
                                while (hierarchyFacets.containsKey(renamedTo)) {
                                    renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";
                                    allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                                    Utils.StaticClass.webAppSystemOutPrintln("Dublicate Hierarchy Name encoutered: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                                }
                                hierarchyFacets.put(renamedTo, finalHierarchyFacets);
                            } else {
                                Utils.StaticClass.webAppSystemOutPrintln("Empty Hierarchy Name encoutered for Subject with id: " + targetSubjectId);
                            }
                        }
                    }
                }

                Utils.StaticClass.webAppSystemOutPrintln("Found " + hierarchyFacets.size() + " Hierarchies.");

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="guide terms">

                //find guideTerms
                subjectEnum = allSubjects.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetSubjectId = subjectEnum.next();
                    AAT_SubjectClass targetClass = allSubjects.get(targetSubjectId);

                    if (targetClass.SubjectKind == AAT_Subject_Kind_Enum.KIND_GUIDE_TERM) {
                        String targetName = targetClass.SubjectPreferredTermName.termName;
                        boolean mainLangFound = true;
                        if (targetClass.SubjectPreferredTermName.langCodes != null && targetClass.SubjectPreferredTermName.langCodes.size() > 0) {
                            mainLangFound = false;
                            for (int k = 0; k < targetClass.SubjectPreferredTermName.langCodes.size(); k++) {
                                AAT_TermLanguage checkLang = targetClass.SubjectPreferredTermName.langCodes.get(k);
                                if (checkLang.languageCode != null && checkLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                    mainLangFound = true;
                                    break;
                                }
                            }
                        }
                        if (mainLangFound == false) {

                            String renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";;
                            allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                            Utils.StaticClass.webAppSystemOutPrintln("Main Language Not Found for guide term: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            targetName = renamedTo;
                        }

                        if (targetName != null && targetName.length() > 0) {

                            String renamedTo = targetName;
                            while (guideTerms.contains(renamedTo)) {
                                renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";
                                allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                                Utils.StaticClass.webAppSystemOutPrintln("Dublicate Guide Term Name encoutered: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);

                            }
                            guideTerms.add(renamedTo);



                        } else {
                            Utils.StaticClass.webAppSystemOutPrintln("Empty Guide Term Name encoutered for Subject with id: " + targetSubjectId);
                        }
                    }
                }

                Utils.StaticClass.webAppSystemOutPrintln("Initially Found " + guideTerms.size() + " GuideTerms.");

                guideTerms.clear();
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="terms and XMLguideTermsRelations">

                //<editor-fold defaultstate="collapsed" desc="Step 1 tcs and names/renames">
                //find terms - step 1 get names/renames and tcs
                subjectEnum = allSubjects.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetSubjectId = subjectEnum.next();

                    AAT_SubjectClass targetClass = allSubjects.get(targetSubjectId);

                    if (targetClass.SubjectKind == AAT_Subject_Kind_Enum.KIND_HIERARCHY || targetClass.SubjectKind == AAT_Subject_Kind_Enum.KIND_CONCEPT) {
                        String targetName = targetClass.SubjectPreferredTermName.termName;
                        NodeInfoStringContainer targetInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                        targetInfo.descriptorInfo.get(ConstantParameters.tc_kwd).add(targetSubjectId);

                        boolean mainLangFound = true;
                        if (targetClass.SubjectPreferredTermName.langCodes != null && targetClass.SubjectPreferredTermName.langCodes.size() > 0) {
                            mainLangFound = false;
                            for (int k = 0; k < targetClass.SubjectPreferredTermName.langCodes.size(); k++) {
                                AAT_TermLanguage checkLang = targetClass.SubjectPreferredTermName.langCodes.get(k);
                                if (checkLang.languageCode != null && checkLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                    mainLangFound = true;
                                    break;
                                }
                            }
                        }
                        if (mainLangFound == false) {

                            String renamedTo = UnlabeledPrefix +(unlabeledCounter++) + "("+targetName+")";
                            allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                            Utils.StaticClass.webAppSystemOutPrintln("Main Language Not Found for term: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            targetName = renamedTo;

                        }

                        if (targetName != null && targetName.length() > 0) {

                            String renamedTo = targetName;
                            while (termsInfo.containsKey(renamedTo)) {
                                renamedTo = UnlabeledPrefix + (unlabeledCounter++) + "("+targetName+")";
                                allSubjects.get(targetSubjectId).SubjectPreferredTermName.termName = renamedTo;
                                Utils.StaticClass.webAppSystemOutPrintln("Dublicate Term Name encoutered: " + targetName + " with subject id: " + targetSubjectId + ". Renamed to " + renamedTo);
                            }
                            termsInfo.put(renamedTo, targetInfo);

                        } else {
                            Utils.StaticClass.webAppSystemOutPrintln("Empty Term Name encoutered for Subject with id: " + targetSubjectId);
                        }
                    }
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="bts nts and guide terms">
                //now find bts nts and guide terms
                subjectEnum = termsInfo.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetTermName = subjectEnum.next();
                    NodeInfoStringContainer targetInfo = termsInfo.get(targetTermName);
                    
                    ArrayList<String> tcs = targetInfo.descriptorInfo.get(ConstantParameters.tc_kwd);
                    String targetTermId = (tcs!=null && !tcs.isEmpty()) ? tcs.get(0) : "";
                    

                    AAT_SubjectClass targetClass = allSubjects.get(targetTermId);

                    ArrayList<String> parentIds = targetClass.parentSubjectIds;
                    ArrayList<String> finalBts = new ArrayList<String>();

                    if (parentIds != null) {
                        for (int k = 0; k < parentIds.size(); k++) {
                            String checkParentId = parentIds.get(k);
                            if (allSubjects.containsKey(checkParentId) && checkParentId.equals(targetTermId) == false) {

                                AAT_SubjectClass candidateBt = allSubjects.get(checkParentId);
                                if (candidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_FACET) {
                                } else if (candidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_CONCEPT
                                        || candidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_HIERARCHY) {
                                    String candidateBtName = candidateBt.SubjectPreferredTermName.termName;

                                    if (finalBts.contains(candidateBtName) == false) {
                                        finalBts.add(candidateBtName);
                                    }
                                    if (termsInfo.containsKey(candidateBtName)) {
                                        ArrayList<String> existingNts = termsInfo.get(candidateBtName).descriptorInfo.get(ConstantParameters.nt_kwd);
                                        if (existingNts.contains(targetTermName) == false) {
                                            existingNts.add(targetTermName);
                                        }
                                        termsInfo.get(candidateBtName).descriptorInfo.put(ConstantParameters.nt_kwd, existingNts);
                                    }
                                } else if (candidateBt.SubjectKind == AAT_Subject_Kind_Enum.KIND_GUIDE_TERM) {

                                    String targetGuideTermName = candidateBt.SubjectPreferredTermName.termName;
                                    ArrayList<String> bts = findGuideTermRelatedBts(allSubjects, candidateBt);

                                    for (int m = 0; m < bts.size(); m++) {
                                        String btName = bts.get(m);
                                        if (finalBts.contains(btName) == false) {
                                            finalBts.add(btName);
                                        }
                                        if (termsInfo.containsKey(btName)) {
                                            ArrayList<String> existingNts = termsInfo.get(btName).descriptorInfo.get(ConstantParameters.nt_kwd);
                                            if (existingNts.contains(targetTermName) == false) {
                                                existingNts.add(targetTermName);
                                            }
                                            termsInfo.get(btName).descriptorInfo.put(ConstantParameters.nt_kwd, existingNts);
                                        }

                                        ArrayList<SortItem> newNts = new ArrayList<SortItem>();
                                        SortItem sortItemToAdd = new SortItem(targetTermName, -1, targetGuideTermName);
                                        if (guideTerms.contains(targetGuideTermName) == false) {
                                            guideTerms.add(targetGuideTermName);
                                        }
                                        if (XMLguideTermsRelations.containsKey(btName) == false) {
                                            newNts.add(sortItemToAdd);
                                        } else {

                                            ArrayList<SortItem> existingNts = XMLguideTermsRelations.get(btName);
                                            newNts.addAll(existingNts);
                                            boolean itemExists = false;
                                            for (int j = 0; j < newNts.size(); j++) {
                                                SortItem compareSi = newNts.get(j);
                                                if (compareSi.equals(sortItemToAdd)) {
                                                    itemExists = true;
                                                    break;
                                                }
                                            }
                                            if (itemExists == false) {
                                                newNts.add(sortItemToAdd);
                                            }


                                        }
                                        XMLguideTermsRelations.put(btName, newNts);
                                    }


                                    //bts,targetTermName,targetGuideTermName

                                }
                            }
                        }
                    }
                    targetInfo.descriptorInfo.put(ConstantParameters.bt_kwd, finalBts);

                }
                //</editor-fold>


                //<editor-fold defaultstate="collapsed" desc="rts tra. ufs (and tra.) scope_note (and tra.), sources(and tra.) creators modificators and dates">
                //now find bts nts and guide terms
                subjectEnum = termsInfo.keySet().iterator();
                while (subjectEnum.hasNext()) {
                    String targetTermName = subjectEnum.next();
                    NodeInfoStringContainer targetInfo = termsInfo.get(targetTermName);
                    ArrayList<String> tcs = targetInfo.descriptorInfo.get(ConstantParameters.tc_kwd);
                    String targetTermId = (tcs!=null && !tcs.isEmpty()) ? tcs.get(0) : "";

                    AAT_SubjectClass targetClass = allSubjects.get(targetTermId);

                    
                    ArrayList<String> assocatedIds = targetClass.associatedSubjectIds;
                    ArrayList<String> finalRts = new ArrayList<String>();

                    if (assocatedIds != null) {
                        for (int k = 0; k < assocatedIds.size(); k++) {
                            String checkAssociatedId = assocatedIds.get(k);
                            if (allSubjects.containsKey(checkAssociatedId) && checkAssociatedId.equals(targetTermId) == false) {

                                AAT_SubjectClass candidateRt = allSubjects.get(checkAssociatedId);
                                if (candidateRt.SubjectKind == AAT_Subject_Kind_Enum.KIND_CONCEPT
                                        || candidateRt.SubjectKind == AAT_Subject_Kind_Enum.KIND_HIERARCHY) {
                                    String candidateRtName = candidateRt.SubjectPreferredTermName.termName;

                                    if (finalRts.contains(candidateRtName) == false) {
                                        finalRts.add(candidateRtName);
                                        if (termsInfo.containsKey(candidateRtName)) {
                                            ArrayList<String> existingRts = termsInfo.get(candidateRtName).descriptorInfo.get(ConstantParameters.rt_kwd);
                                            if (existingRts.contains(targetTermName) == false) {
                                                existingRts.add(targetTermName);
                                            }

                                            //termsInfo.get(candidateRtName).descriptorInfo.put(dbGen.rt_kwd,existingRts);

                                        }
                                    }
                                } else if (candidateRt.SubjectKind == AAT_Subject_Kind_Enum.KIND_GUIDE_TERM) {
                                    ArrayList<String> rts = this.findGuideTermRelatedBts(allSubjects, candidateRt);
                                    for (int m = 0; m < rts.size(); m++) {
                                        String candidateRtName = rts.get(m);

                                        if(candidateRtName.equals(targetTermName)){
                                            continue;
                                        }
                                        if (finalRts.contains(candidateRtName) == false) {
                                            finalRts.add(candidateRtName);
                                            if (termsInfo.containsKey(candidateRtName)) {
                                                ArrayList<String> existingRts = termsInfo.get(candidateRtName).descriptorInfo.get(ConstantParameters.rt_kwd);
                                                if (existingRts.contains(targetTermName) == false) {
                                                    existingRts.add(targetTermName);
                                                }
                                            }
                                        }
                                    }
                                    
                                    //Utils.StaticClass.webAppSystemOutPrintln("Unexpected candidateRt Type" + candidateRt.SubjectKind + " with id: " + candidateRt.SubjectId + " for subject with id: " + targetTermId);
                                }
                            }
                        }
                    }



                    targetInfo.descriptorInfo.put(ConstantParameters.rt_kwd, finalRts);

                    ArrayList<String> finalTranslations = new ArrayList<String>();
                    ArrayList<String> finalPrimarySources = new ArrayList<String>();
                    ArrayList<String> finalTranslationSources = new ArrayList<String>();
                    ArrayList<String> finalUfs = new ArrayList<String>();
                    ArrayList<String> finalTranslationUfs = new ArrayList<String>();

                    for (int i = 0; i < targetClass.SubjectPreferredTermName.termSources.size(); i++) {
                        String source = targetClass.SubjectPreferredTermName.termSources.get(i);
                        if (finalPrimarySources.contains(source) == false) {
                            finalPrimarySources.add(source);
                        }
                    }
                    for (int i = 0; i < targetClass.SubjectPreferredTermName.langCodes.size(); i++) {

                        AAT_TermLanguage checkTermLang = targetClass.SubjectPreferredTermName.langCodes.get(i);
                        if (checkTermLang.languageCode.length() > 0
                                && checkTermLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase()) == false) {
                            if ((checkTermLang.preferredTag == AAT_TermLanguage_Preferred_Enum.PREFERRED
                                    || checkTermLang.preferredTag == AAT_TermLanguage_Preferred_Enum.UNDETERMINED)
                                    && (checkTermLang.termType == AAT_TermLanguage_Term_Type_Enum.DESCRIPTOR
                                    || checkTermLang.termType == AAT_TermLanguage_Term_Type_Enum.UNDETERMINED)) {

                                String newtranslation = checkTermLang.languageCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR + targetTermName;
                                if (finalTranslations.contains(newtranslation) == false) {
                                    finalTranslations.add(newtranslation);
                                }
                                for (int j = 0; j < targetClass.SubjectPreferredTermName.termSources.size(); j++) {
                                    String source = targetClass.SubjectPreferredTermName.termSources.get(j);
                                    if (finalTranslationSources.contains(source) == false) {
                                        finalTranslationSources.add(source);
                                    }
                                }
                            } else {

                                String newUFtranslation = checkTermLang.languageCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR + targetTermName;
                                if (finalTranslationUfs.contains(newUFtranslation) == false) {
                                    finalTranslationUfs.add(newUFtranslation);
                                }
                                for (int j = 0; j < targetClass.SubjectPreferredTermName.termSources.size(); j++) {
                                    String source = targetClass.SubjectPreferredTermName.termSources.get(j);
                                    if (finalTranslationSources.contains(source) == false) {
                                        finalTranslationSources.add(source);
                                    }
                                }
                            }
                        }

                    }

                    for (int i = 0; i < targetClass.nonPreferredTermNames.size(); i++) {

                        AAT_SubjectTermClass checktermClass = targetClass.nonPreferredTermNames.get(i);

                        String baseName = checktermClass.termName;

                        for (int j = 0; j < checktermClass.langCodes.size(); j++) {
                            AAT_TermLanguage checkTermLang = checktermClass.langCodes.get(j);

                            if (checkTermLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                String newUf = baseName;
                                if (finalUfs.contains(newUf) == false) {
                                    finalUfs.add(newUf);
                                }
                                for (int k = 0; k < checktermClass.termSources.size(); k++) {
                                    String source = checktermClass.termSources.get(k);
                                    if (finalPrimarySources.contains(source) == false) {
                                        finalPrimarySources.add(source);
                                    }
                                }

                            } else {
                                if (checkTermLang.languageCode.length() > 0) {
                                    if ((checkTermLang.preferredTag == AAT_TermLanguage_Preferred_Enum.PREFERRED
                                            || checkTermLang.preferredTag == AAT_TermLanguage_Preferred_Enum.UNDETERMINED)
                                            && (checkTermLang.termType == AAT_TermLanguage_Term_Type_Enum.DESCRIPTOR
                                            || checkTermLang.termType == AAT_TermLanguage_Term_Type_Enum.UNDETERMINED)) {

                                        String newtranslation = checkTermLang.languageCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR + baseName;
                                        if (finalTranslations.contains(newtranslation) == false) {
                                            finalTranslations.add(newtranslation);
                                        }
                                        for (int k = 0; k < checktermClass.termSources.size(); k++) {
                                            String source = checktermClass.termSources.get(k);
                                            if (finalTranslationSources.contains(source) == false) {
                                                finalTranslationSources.add(source);
                                            }
                                        }
                                    } else {

                                        String newUFtranslation = checkTermLang.languageCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR + baseName;
                                        if (finalTranslationUfs.contains(newUFtranslation) == false) {
                                            finalTranslationUfs.add(newUFtranslation);
                                        }
                                        for (int k = 0; k < checktermClass.termSources.size(); k++) {
                                            String source = checktermClass.termSources.get(k);
                                            if (finalTranslationSources.contains(source) == false) {
                                                finalTranslationSources.add(source);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }


                    targetInfo.descriptorInfo.put(ConstantParameters.translation_kwd, finalTranslations);
                    targetInfo.descriptorInfo.put(ConstantParameters.uf_kwd, finalUfs);
                    targetInfo.descriptorInfo.put(ConstantParameters.uf_translations_kwd, finalTranslationUfs);


                    ArrayList<String> finalScopeNoteVec = new ArrayList<String>();
                    ArrayList<String> finalTranslationsScopeNoteVec = new ArrayList<String>();

                    String finalScopeNoteStr = "";
                    String finalTranslationsScopeNoteStr = "";
                    for (int i = 0; i < targetClass.descriptiveNotes.size(); i++) {
                        AAT_SubjectTermClass checkNote = targetClass.descriptiveNotes.get(i);

                        String baseNote = checkNote.termName;

                        if (baseNote == null || baseNote.length() == 0) {
                            continue;
                        }

                        ArrayList<AAT_TermLanguage> langCodes = checkNote.langCodes;
                        AAT_TermLanguage checkLang =(langCodes!=null && !langCodes.isEmpty()) ? langCodes.get(0) : null;
                        if (checkLang != null) {
                            if (checkLang.languageCode.toUpperCase().equals(Parameters.PrimaryLang.toUpperCase())) {
                                finalScopeNoteStr += baseNote;
                                for (int k = 0; k < checkNote.termSources.size(); k++) {
                                    String source = checkNote.termSources.get(k);
                                    if (finalPrimarySources.contains(source) == false) {
                                        finalPrimarySources.add(source);
                                    }
                                }
                            } else {
                                String translationSNStr ="";
                                
                                translationSNStr += checkLang.languageCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR+" ";
                                translationSNStr += baseNote.trim();

                                finalTranslationsScopeNoteVec.add(translationSNStr);
                                for (int k = 0; k < checkNote.termSources.size(); k++) {
                                    String source = checkNote.termSources.get(k);
                                    if (finalTranslationSources.contains(source) == false) {
                                        finalTranslationSources.add(source);
                                    }
                                }
                            }


                        }

                    }

                    targetInfo.descriptorInfo.put(ConstantParameters.primary_found_in_kwd, finalPrimarySources);
                    targetInfo.descriptorInfo.put(ConstantParameters.translations_found_in_kwd, finalTranslationSources);


                    if (finalScopeNoteStr.length() > 0) {
                        finalScopeNoteVec.add(finalScopeNoteStr);
                        targetInfo.descriptorInfo.put(ConstantParameters.scope_note_kwd, finalScopeNoteVec);
                    }

                    if (finalTranslationsScopeNoteVec.size() > 0) {
                        targetInfo.descriptorInfo.put(ConstantParameters.translations_scope_note_kwd, finalTranslationsScopeNoteVec);
                    }


                    ArrayList<String> finalCreatedBy = new ArrayList<String>();
                    ArrayList<String> finalCreatedOn = new ArrayList<String>();
                    ArrayList<String> finalModifiedBy = new ArrayList<String>();
                    ArrayList<String> finalModifiedOn = new ArrayList<String>();

                    for (int i = 0; i < targetClass.contributors.size(); i++) {
                        AAT_RevisionClass checkRev = targetClass.contributors.get(i);

                        String userName = checkRev.userName;
                        String date = checkRev.getDate();

                        if (checkRev.action.equals(ConstantParameters.aat_Revision_Action_created_val)) {
                            //creator

                            if (userName != null && userName.length() > 0 && finalCreatedBy.contains(userName) == false) {
                                finalCreatedBy.add(userName);
                            }
                            if (date != null && date.length() > 0 && finalCreatedOn.contains(date) == false) {
                                finalCreatedOn.add(date);
                            }
                        } else {
                            //modificator
                            if (userName != null && userName.length() > 0 && finalModifiedBy.contains(userName) == false) {
                                finalModifiedBy.add(userName);
                            }
                            if (date != null && date.length() > 0 && finalModifiedOn.contains(date) == false) {
                                finalModifiedOn.add(date);
                            }
                        }
                    }

                    targetInfo.descriptorInfo.put(ConstantParameters.created_by_kwd, finalCreatedBy);
                    targetInfo.descriptorInfo.put(ConstantParameters.created_on_kwd, finalCreatedOn);
                    targetInfo.descriptorInfo.put(ConstantParameters.modified_by_kwd, finalModifiedBy);
                    targetInfo.descriptorInfo.put(ConstantParameters.modified_on_kwd, finalModifiedOn);

                }


                Utils.StaticClass.webAppSystemOutPrintln("Found " + guideTerms.size() + " GuideTerms.");
                Utils.StaticClass.webAppSystemOutPrintln("Found " + termsInfo.size() + " terms.");
                // </editor-fold>


            } //</editor-fold>
        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }

        return true;

    }

    private void readAllGuideTerms(String xmlFilePath, String xmlSchemaType, ArrayList<String> guideTerms){

        XmlPullParserFactory factory;
        try {

            //<editor-fold defaultstate="collapsed" desc="SKOS Case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="THEMAS Case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();

                String targetTermId = "";
                String targetPrefferedName = "";

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.XMLNodeLabelsWrapperElementName) ){
                            this.parseGuideTermNodes(xpp, xmlSchemaType, guideTerms);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            } //</editor-fold>
        
        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }

        
        return;
    }

    public boolean readXMLGuideTerms(String xmlFilePath, String xmlSchemaType, ArrayList<String> guideTerms,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading Guide Terms / Node labels from file: " + xmlFilePath + ".");

        DBGeneral dbGen = new DBGeneral();

        readAllGuideTerms(xmlFilePath, xmlSchemaType, guideTerms);
        //read all subjectIds, subjectClass values
        //read all guide terms by storing values in ArrayList<String> guideTerms
        //read all term/guideterms - parentSubject Id pairs



        XmlPullParserFactory factory;
        try {


            //<editor-fold defaultstate="collapsed" desc="SKOS Case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                HashMap<String, String> idsToNames = new HashMap<String, String>();

                // <editor-fold defaultstate="collapsed" desc="parse All ids">
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                int eventType = xpp.getEventType();

                String targetTermId = "";
                String targetPrefferedName = "";

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {

                            if (openingTagName.equals(ConstantParameters.XML_skos_collection)) {
                                targetTermId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            } else if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                targetTermId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            }
                        } else if (depth == 3) {
                            String targetLangCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));

                            if (openingTagName.equals(ConstantParameters.XML_skos_prefLabel) && targetLangCode != null
                                    && targetLangCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                targetPrefferedName = this.parseSimpleContentElement(xpp);
                            }
                        }
                    } else if (eventType == xpp.END_TAG) {
                        String closingTagName = this.closingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            if (closingTagName.equals(ConstantParameters.XML_skos_collection) || closingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                if (targetTermId != null && targetTermId.length() > 0) {

                                    if (idsToNames.containsKey(targetTermId)) {
                                        Utils.StaticClass.webAppSystemOutPrintln("Dublicate key found: " + targetTermId);
                                        String existingName = idsToNames.get(targetTermId);
                                        if (existingName != null && existingName.length() > 0 && existingName.equals(targetTermId) == false) {
                                            if (targetPrefferedName != null && targetPrefferedName.length() > 0 && existingName.equals(existingName) == false) {
                                                Utils.StaticClass.webAppSystemOutPrintln("More than one Name found for id: " + targetTermId + "  Names: " + existingName + ", " + targetPrefferedName);
                                            } else {
                                                //do nothing keep it as it is
                                                //idsToNames.put(targetId, existingName);
                                            }
                                        }
                                    } else {
                                        if (targetPrefferedName != null && targetPrefferedName.length() > 0) {
                                            idsToNames.put(targetTermId, targetPrefferedName);
                                        } else {
                                            idsToNames.put(targetTermId, targetTermId);
                                        }
                                    }
                                }
                                targetTermId = "";
                                targetPrefferedName = "";
                            }
                        }
                    }
                    eventType = xpp.next();
                }
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Guide Terms Parsing">

                //HashMap<String, NodeInfoStringContainer> termsInfoIds = new HashMap<String, NodeInfoStringContainer>();
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                factory.setNamespaceAware(false);
                xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                String targetTermName = "";
                String targetNt = "";
                String targetGuideTerm = "";




                boolean insideConcept = false;

                eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {



                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New Term Encoutered">
                    if (eventType == xpp.START_TAG) {

                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        //check that we are in correct Depth

                        if (depth == 2) {

                            String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                insideConcept = true;

                                if (idsToNames.containsKey(targetId)) {
                                    targetTermName = idsToNames.get(targetId);
                                } else {
                                    targetTermName = targetId;
                                }
                            }
                        } else if (depth > 2) {

                            if (insideConcept) {
                                // <editor-fold defaultstate="collapsed" desc="narrower - nts">

                                if (openingTagName.equals(ConstantParameters.XML_skos_narrower)) {
                                    String narrowerId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);

                                    if (narrowerId != null && narrowerId.trim().length() > 0) {
                                    } else {
                                        ArrayList<String> collectionIds = new ArrayList<String>();
                                        targetGuideTerm = parseSkosCollectionMembers(xpp, ConstantParameters.XML_skos_narrower, collectionIds);

                                        if (collectionIds != null) {
                                            if (targetGuideTerm != null && targetGuideTerm.length() > 0) {
                                                if (guideTerms.contains(targetGuideTerm) == false) {
                                                    guideTerms.add(targetGuideTerm);
                                                }
                                                if (XMLguideTermsRelations.containsKey(targetTermName) == false) {
                                                    XMLguideTermsRelations.put(targetTermName, new ArrayList<SortItem>());
                                                }

                                                ArrayList<SortItem> finalGts = XMLguideTermsRelations.get(targetTermName);

                                                for (int k = 0; k < collectionIds.size(); k++) {
                                                    String collectionMemberId = collectionIds.get(k);
                                                    if (collectionMemberId != null && collectionMemberId.trim().length() > 0) {
                                                        String ntName = "";
                                                        if (idsToNames.containsKey(collectionMemberId)) {
                                                            ntName = idsToNames.get(collectionMemberId);
                                                        } else {
                                                            ntName = collectionMemberId;
                                                        }

                                                        if (ntName != null && ntName.trim().length() > 0) {
                                                            SortItem newSort = new SortItem(ntName, -1, targetGuideTerm);
                                                            if (finalGts.contains(newSort) == false) {
                                                                finalGts.add(newSort);
                                                            }
                                                        }
                                                    }

                                                }
                                                XMLguideTermsRelations.put(targetTermName, finalGts);
                                            }


                                        }
                                    }

                                }
                                // </editor-fold>


                            }

                        }


                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Concept Completed.">
                    else if (eventType == xpp.END_TAG) {

                        int depth = xpp.getDepth();
                        if (depth == 2) {

                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.XML_skos_concept)) {

                                insideConcept = false;
                            }

                        }
                    }
                    //</editor-fold>

                    xpp.next();
                    eventType = xpp.getEventType();
                }

                //
                // </editor-fold>



            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="THEMAS Case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals(ConstantParameters.XMLTermsWrapperElementName)) {
                            this.parseGuideTerms(xpp, guideTerms, XMLguideTermsRelations);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }
            }
            //</editor-fold>

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading Guide Terms / Node Labels. Found: " + guideTerms.size() + " Guide Terms / Node Labels.");
        return true;
    }

    private void parseHierarchyNodes(XmlPullParser xpp, String xmlSchemaType, HashMap<String, ArrayList<String>> hierarchyFacets, ArrayList<String> xmlFacets) {
        try {

            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                String facetId = "";
                String hierarchyId = "";
                boolean insideCollection = false;
                boolean insideConcept = false;

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();


                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                    if (eventType == xpp.START_TAG) {
                        //case 1 collection-meber--> Facet-Heirarchy, case 2 concept with topConceptOf

                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        //check that we are in correct Depth

                        if (depth == 2) {

                            String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            if (openingTagName.equals(ConstantParameters.XML_skos_collection)) {
                                insideCollection = true;
                                insideConcept = false;
                                facetId = targetId;
                            } else if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                insideCollection = false;
                                insideConcept = true;
                                hierarchyId = targetId;
                            }
                        } else if (depth == 3) {

                            if (insideCollection && openingTagName.equals(ConstantParameters.XML_skos_member)) {
                                String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);
                                if (facetId != null && facetId.length() > 0 && targetId != null && targetId.length() > 0) {
                                    if (hierarchyFacets.containsKey(targetId)) {
                                        ArrayList<String> facetIds = hierarchyFacets.get(targetId);
                                        if (facetIds.contains(facetId) == false) {
                                            facetIds.add(facetId);
                                        }
                                        hierarchyFacets.put(targetId, facetIds);
                                    } else {
                                        ArrayList<String> facetIds = new ArrayList<String>();
                                        facetIds.add(facetId);
                                        hierarchyFacets.put(targetId, facetIds);
                                    }
                                }
                            } else if (insideConcept && openingTagName.equals(ConstantParameters.XML_skos_topConceptOf)) {
                                if (hierarchyId != null && hierarchyId.length() > 0) {
                                    if (hierarchyFacets.containsKey(hierarchyId) == false) {
                                        hierarchyFacets.put(hierarchyId, new ArrayList<String>());
                                    }
                                }

                            }

                        }


                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            facetId = "";
                            hierarchyId = "";
                            insideCollection = false;
                            insideConcept = false;
                        }
                    }
                    //</editor-fold>
                }




            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
                if (xpp == null) {
                    return;
                }
                String elementName = xpp.getName();
                if (elementName.equals("hierarchies") == false) {
                    return;
                }
                //</editor-fold>


                String targetHierarchyName = "";
                ArrayList<String> targetHierarcyFacets = null;

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                    if (eventType == xpp.START_TAG) {
                        String currentTagName = this.openingTagEncoutered(xpp, null);
                        if (currentTagName.equals("hierarchy")) {
                            targetHierarchyName = "";
                            targetHierarcyFacets = new ArrayList<String>();

                        } else if (currentTagName.equals("name")) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                targetHierarchyName = this.readXMLTag(targetValue);
                            }
                        } else if (currentTagName.equals("facet")) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                if (targetHierarcyFacets.contains(targetValue) == false) {
                                    targetHierarcyFacets.add(targetValue);
                                }

                                if (xmlFacets.contains(targetValue) == false) {
                                    xmlFacets.add(targetValue);
                                }
                            }
                        }
                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);


                        //Check if hierarchies parsing is completed
                        if (currentTagName.equals("hierarchies")) {
                            break;
                        }

                        //Check if currentFacet parsing is completed
                        if (currentTagName.equals("hierarchy")) {
                            if (targetHierarchyName == null || targetHierarchyName.trim().length() == 0) {
                                continue;
                            } else {

                                //if hierarchy info does not exist then add it

                                if (hierarchyFacets.containsKey(targetHierarchyName) == false) {
                                    if (targetHierarcyFacets == null) {
                                        targetHierarcyFacets = new ArrayList<String>();
                                    }
                                    hierarchyFacets.put(targetHierarchyName, targetHierarcyFacets);
                                } else {
                                    if (targetHierarcyFacets != null && targetHierarcyFacets.size() > 0) {
                                        ArrayList<String> existingFacets = hierarchyFacets.get(targetHierarchyName);
                                        for (int k = 0; k < targetHierarcyFacets.size(); k++) {
                                            String checkFacet = targetHierarcyFacets.get(k);
                                            if (existingFacets.contains(checkFacet) == false) {
                                                existingFacets.add(checkFacet);
                                            }
                                        }

                                        hierarchyFacets.put(targetHierarchyName, existingFacets);
                                    }

                                }
                            }


                        }
                    }
                    //</editor-fold>
                }
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    
    private void parseHierarchyNodesWithFacetsInSortItems(XmlPullParser xpp, String xmlSchemaType, HashMap<String, ArrayList<String>> hierarchyFacets, ArrayList<SortItem> xmlFacets) {
        try {
            
            //creating a vector for String only comparison of logical name
            ArrayList<String> xmlFacetStrings = new ArrayList<String>();
            for(SortItem item: xmlFacets){
                xmlFacetStrings.add(item.getLogName());
            }

            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                String facetId = "";
                String hierarchyId = "";
                boolean insideCollection = false;
                boolean insideConcept = false;

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();


                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                    if (eventType == xpp.START_TAG) {
                        //case 1 collection-meber--> Facet-Heirarchy, case 2 concept with topConceptOf

                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        //check that we are in correct Depth

                        if (depth == 2) {

                            String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            if (openingTagName.equals(ConstantParameters.XML_skos_collection)) {
                                insideCollection = true;
                                insideConcept = false;
                                facetId = targetId;
                            } else if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                insideCollection = false;
                                insideConcept = true;
                                hierarchyId = targetId;
                            }
                        } else if (depth == 3) {

                            if (insideCollection && openingTagName.equals(ConstantParameters.XML_skos_member)) {
                                String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);
                                if (facetId != null && facetId.length() > 0 && targetId != null && targetId.length() > 0) {
                                    if (hierarchyFacets.containsKey(targetId)) {
                                        ArrayList<String> facetIds = hierarchyFacets.get(targetId);
                                        if (facetIds.contains(facetId) == false) {
                                            facetIds.add(facetId);
                                        }
                                        hierarchyFacets.put(targetId, facetIds);
                                    } else {
                                        ArrayList<String> facetIds = new ArrayList<String>();
                                        facetIds.add(facetId);
                                        hierarchyFacets.put(targetId, facetIds);
                                    }
                                }
                            } else if (insideConcept && openingTagName.equals(ConstantParameters.XML_skos_topConceptOf)) {
                                if (hierarchyId != null && hierarchyId.length() > 0) {
                                    if (hierarchyFacets.containsKey(hierarchyId) == false) {
                                        hierarchyFacets.put(hierarchyId, new ArrayList<String>());
                                    }
                                }

                            }

                        }


                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            facetId = "";
                            hierarchyId = "";
                            insideCollection = false;
                            insideConcept = false;
                        }
                    }
                    //</editor-fold>
                }




            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
                if (xpp == null) {
                    return;
                }
                String elementName = xpp.getName();
                if (elementName.equals("hierarchies") == false) {
                    return;
                }
                //</editor-fold>


                String targetHierarchyName = "";
                ArrayList<String> targetHierarcyFacets = null;

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                    if (eventType == xpp.START_TAG) {
                        String currentTagName = this.openingTagEncoutered(xpp, null);
                        if (currentTagName.equals("hierarchy")) {
                            targetHierarchyName = "";
                            targetHierarcyFacets = new ArrayList<String>();

                        } else if (currentTagName.equals("name")) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                targetHierarchyName = this.readXMLTag(targetValue);
                            }
                        } else if (currentTagName.equals("facet")) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                if (targetHierarcyFacets.contains(targetValue) == false) {
                                    targetHierarcyFacets.add(targetValue);
                                }
                                
                                if (xmlFacetStrings.contains(targetValue) == false) {
                                    SortItem newFacet = new SortItem(targetValue,-1,Utilities.getTransliterationString(targetValue, false),-1);
                                    xmlFacets.add(newFacet);
                                }
                            }
                        }
                    } //</editor-fold>
                    
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);


                        //Check if hierarchies parsing is completed
                        if (currentTagName.equals("hierarchies")) {
                            break;
                        }

                        //Check if currentFacet parsing is completed
                        if (currentTagName.equals("hierarchy")) {
                            if (targetHierarchyName == null || targetHierarchyName.trim().length() == 0) {
                                continue;
                            } else {

                                //if hierarchy info does not exist then add it

                                if (hierarchyFacets.containsKey(targetHierarchyName) == false) {
                                    if (targetHierarcyFacets == null) {
                                        targetHierarcyFacets = new ArrayList<String>();
                                    }
                                    hierarchyFacets.put(targetHierarchyName, targetHierarcyFacets);
                                } else {
                                    if (targetHierarcyFacets != null && targetHierarcyFacets.size() > 0) {
                                        ArrayList<String> existingFacets = hierarchyFacets.get(targetHierarchyName);
                                        for (int k = 0; k < targetHierarcyFacets.size(); k++) {
                                            String checkFacet = targetHierarcyFacets.get(k);
                                            if (existingFacets.contains(checkFacet) == false) {
                                                existingFacets.add(checkFacet);
                                            }
                                        }

                                        hierarchyFacets.put(targetHierarchyName, existingFacets);
                                    }

                                }
                            }


                        }
                    }
                    //</editor-fold>
                }
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private void parseSourceNodes(XmlPullParser xpp, HashMap<String, String> XMLsources) {
        try {

            // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
            if (xpp == null) {
                return;
            }
            String elementName = xpp.getName();
            if (elementName.equals(ConstantParameters.XMLSourcesWrapperElementName) == false) {
                return;
            }
            //</editor-fold>


            String targetSourceName = "";
            String targetSourceNote = "";

            while (xpp.getEventType() != xpp.END_DOCUMENT) {
                xpp.next();
                int eventType = xpp.getEventType();

                // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                if (eventType == xpp.START_TAG) {
                    String currentTagName = this.openingTagEncoutered(xpp, null);
                    if (currentTagName.equals("source")) {
                        targetSourceName = "";
                        targetSourceNote = "";

                    } else if (currentTagName.equals("name")) {
                        String targetValue = this.parseSimpleContentElement(xpp);

                        if (targetValue != null && targetValue.trim().length() > 0) {
                            targetSourceName = this.readXMLTag(targetValue);
                        }
                    } else if (currentTagName.equals("source_note")) {
                        String targetValue = this.parseSimpleContentElement(xpp);

                        if (targetValue != null && targetValue.trim().length() > 0) {
                            targetSourceNote = this.readXMLTag(targetValue);
                        }
                    }
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Source Completed. also Check if all Sources are completed">
                else if (eventType == xpp.END_TAG) {

                    String currentTagName = this.closingTagEncoutered(xpp, null);


                    //Check if hierarchies parsing is completed
                    if (currentTagName.equals("sources")) {
                        break;
                    }

                    //Check if currentFacet parsing is completed
                    if (currentTagName.equals("source")) {
                        if (targetSourceName == null || targetSourceName.trim().length() == 0) {
                            continue;
                        } else {

                            //if hierarchy info does not exist then add it

                            if (XMLsources.containsKey(targetSourceName) == false) {
                                XMLsources.put(targetSourceName, targetSourceNote);
                            } else {

                                if (targetSourceNote != null && targetSourceNote.length() > 0) {
                                    String existingNote = XMLsources.get(targetSourceName);

                                    if (existingNote.equals(targetSourceNote) == false) {
                                        existingNote += targetSourceNote;
                                    }

                                    XMLsources.put(targetSourceName, existingNote);
                                }

                            }
                        }


                    }
                }
                //</editor-fold>
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private void parseTermNodesSources(XmlPullParser xpp, HashMap<String, String> XMLsources) {

        DBGeneral dbGen = new DBGeneral();
        try {

            // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
            if (xpp == null) {
                return;
            }
            String elementName = xpp.getName();
            if (elementName.equals(ConstantParameters.XMLTermsWrapperElementName) == false) {
                return;
            }
            //</editor-fold>


            while (xpp.getEventType() != xpp.END_DOCUMENT) {
                xpp.next();
                int eventType = xpp.getEventType();

                // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                if (eventType == xpp.START_TAG) {
                    String currentTagName = this.openingTagEncoutered(xpp, null);
                    if (currentTagName.equals(ConstantParameters.primary_found_in_kwd)) {
                        String targetValue = this.readXMLTag(this.parseSimpleContentElement(xpp));
                        if (XMLsources.containsKey(targetValue) == false) {
                            XMLsources.put(targetValue, "");
                        }

                    } else if (currentTagName.equals(ConstantParameters.translations_found_in_kwd)) {
                        String targetValue = this.readXMLTag(this.parseSimpleContentElement(xpp));
                        XMLsources.put(targetValue, "");
                    }

                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Check if all Terms are completed">
                else if (eventType == xpp.END_TAG) {

                    String currentTagName = this.closingTagEncoutered(xpp, null);


                    //Check if hierarchies parsing is completed
                    if (currentTagName.equals(ConstantParameters.XMLTermsWrapperElementName)) {
                        break;
                    }
                }
                //</editor-fold>
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private void parseGuideTerms(XmlPullParser xpp, ArrayList<String> guideTerms, HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations) {

        DBGeneral dbGen = new DBGeneral();
        try {

            // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
            if (xpp == null) {
                return;
            }
            String elementName = xpp.getName();
            if (elementName.equals(ConstantParameters.XMLTermsWrapperElementName) == false) {
                return;
            }
            //</editor-fold>

            String targetTerm = "";
            String targetNt = "";
            String targetGuideTerm = "";

            while (xpp.getEventType() != xpp.END_DOCUMENT) {
                xpp.next();
                int eventType = xpp.getEventType();

                // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New hierarchy Encoutered">
                if (eventType == xpp.START_TAG) {
                    String currentTagName = this.openingTagEncoutered(xpp, null);
                    if (currentTagName.equals(ConstantParameters.XMLTermElementName)) {
                        targetTerm = "";
                        targetNt = "";
                        targetGuideTerm = "";
                    }
                    if (currentTagName.equals(ConstantParameters.XMLDescriptorElementName)) {
                        targetTerm = this.readXMLTag(this.parseSimpleContentElement(xpp));
                    }
                    if (currentTagName.equals(ConstantParameters.nt_kwd)) {

                        int howmanyAttrs = xpp.getAttributeCount();
                        for (int k = 0; k < howmanyAttrs; k++) {
                            if (xpp.getAttributeName(k).equals(ConstantParameters.XMLLinkClassAttributeName)) {
                                targetGuideTerm = xpp.getAttributeValue(k);
                                break;
                            }
                        }

                        targetNt = this.readXMLTag(this.parseSimpleContentElement(xpp));

                        if (targetNt != null && targetNt.trim().length() > 0) {

                            if (targetGuideTerm != null && targetGuideTerm.trim().length() > 0) {

                                if (guideTerms.contains(targetGuideTerm.trim()) == false) {
                                    guideTerms.add(targetGuideTerm.trim());
                                }

                                SortItem newSI = new SortItem(targetNt, -1, targetGuideTerm.trim());

                                if (XMLguideTermsRelations.containsKey(targetTerm) == false) {
                                    ArrayList<SortItem> newGTs = new ArrayList<SortItem>();
                                    newGTs.add(newSI);
                                    XMLguideTermsRelations.put(targetTerm, newGTs);

                                } else {
                                    ArrayList<SortItem> oldGTs = XMLguideTermsRelations.get(targetTerm);
                                    if (oldGTs.contains(newSI) == false) {
                                        oldGTs.add(newSI);
                                    }
                                    XMLguideTermsRelations.put(targetTerm, oldGTs);
                                }

                            }

                        }




                    }

                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Check if all Terms are completed">
                else if (eventType == xpp.END_TAG) {

                    String currentTagName = this.closingTagEncoutered(xpp, null);


                    //Check if terms parsing is completed
                    if (currentTagName.equals(ConstantParameters.XMLTermsWrapperElementName)) {
                        break;
                    }
                    if (currentTagName.equals(ConstantParameters.nt_kwd)) {
                        targetNt = "";
                        targetGuideTerm = "";
                    }
                }
                //</editor-fold>
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private String parseSkosCollectionMembers(XmlPullParser xpp, String endTag, ArrayList<String> returnVals) {
        //ArrayList<String> returnVals = new ArrayList<String>();

        String collectionName = "";
        try {
            while (xpp.getEventType() != xpp.END_DOCUMENT) {

                xpp.next();
                int eventType = xpp.getEventType();
                if (eventType == xpp.START_TAG) {

                    String openingTagName = this.openingTagEncoutered(xpp, null);
                    if (openingTagName.equals(ConstantParameters.XML_skos_prefLabel) && (collectionName == null || collectionName.length() == 0)) {
                        collectionName = this.parseSimpleContentElement(xpp);
                    }
                    if (openingTagName.equals(ConstantParameters.XML_skos_member)) {
                        String memberId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);
                        if (memberId != null && memberId.length() > 0 && returnVals.contains(memberId) == false) {
                            returnVals.add(memberId);
                        }
                    }
                } else if (eventType == xpp.END_TAG) {
                    String closingTagName = this.closingTagEncoutered(xpp, null);
                    if (closingTagName.equals(ConstantParameters.XML_skos_collection) || closingTagName.equals(endTag)) {
                        break;
                    }
                }

            }

        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }

        return collectionName;

    }

    private boolean parseTermNodes(XmlPullParser xpp, String xmlSchemaType, 
            HashMap<String, NodeInfoStringContainer> termsInfo, 
            String translationSeparator, 
            String[] output, 
            HashMap<String, String> idsToNames,
            HashMap<String, String> languageSelections) {

        try {

            // <editor-fold defaultstate="collapsed" desc="Skos Case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {
                //all concept nodes are enough

                int counter = 0;
                String termName = "";
                NodeInfoStringContainer targetTermInfo = null;

                boolean insideConcept = false;

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();


                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New Term Encoutered">
                    if (eventType == xpp.START_TAG) {

                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        int depth = xpp.getDepth();
                        //check that we are in correct Depth

                        if (depth == 2) {

                            String targetId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_about, xpp);
                            if (openingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                insideConcept = true;
                                if (idsToNames.containsKey(targetId)) {
                                    termName = idsToNames.get(targetId);

                                    if (termsInfo.containsKey(termName)) {
                                        termName = targetId;
                                    }
                                } else {
                                    termName = targetId;
                                }
                                targetTermInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                                ArrayList<String> tcs = new ArrayList<String>();
                                String tcValue = readSkosTC(targetId);

                                /*if (targetId.startsWith("http://")) {
                                String[] parts = targetId.split("/");
                                if (parts.length > 1) {
                                String tempStr = parts[parts.length - 1];
                                if (tempStr != null && tempStr.trim().length() > 0) {
                                tcValue = tempStr.trim();
                                }
                                }
                                }*/
                                tcs.add(tcValue);
                                targetTermInfo.descriptorInfo.put(ConstantParameters.tc_kwd, tcs);
                            }
                        } else if (depth > 2) {

                            if (insideConcept) {

                                // <editor-fold defaultstate="collapsed" desc="prefLabel - translations">
                                if (openingTagName.equals(ConstantParameters.XML_skos_prefLabel)) {
                                    String langCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));
                                    String targetValue = this.parseSimpleContentElement(xpp);

                                    if (langCode != null && langCode.length() > 0 && targetValue != null && targetValue.length() > 0) {

                                        //if it is the same then skip as we will find it from the idsToNames HashMap
                                        if (langCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase()) == false) {
                                            //skip languages not supported
                                            if (languageSelections.containsValue(langCode.toUpperCase())) {
                                                ArrayList<String> translationValues = targetTermInfo.descriptorInfo.get(ConstantParameters.translation_kwd);
                                                if (translationValues == null) {
                                                    translationValues = new ArrayList<String>();
                                                }
                                                String translationValue = langCode.toUpperCase() + translationSeparator + targetValue;

                                                if (translationValues.contains(translationValue) == false) {
                                                    translationValues.add(translationValue);
                                                    targetTermInfo.descriptorInfo.put(ConstantParameters.translation_kwd, translationValues);
                                                    //Utils.StaticClass.webAppSystemOutPrintln("adding translation val " + translationValue +" to term with id "+termId);
                                                }
                                            }
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="altLabel - uf and uf_translations">
                                else if (openingTagName.equals(ConstantParameters.XML_skos_altLabel)) {

                                    String langCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));
                                    String targetValue = this.parseSimpleContentElement(xpp);

                                    if (langCode != null && langCode.length() > 0 && targetValue != null && targetValue.length() > 0) {

                                        if (langCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {

                                            ArrayList<String> ufValues = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_kwd);
                                            if (ufValues == null) {
                                                ufValues = new ArrayList<String>();
                                            }
                                            String newValue = targetValue;

                                            if (ufValues.contains(newValue) == false) {
                                                ufValues.add(newValue);
                                                targetTermInfo.descriptorInfo.put(ConstantParameters.uf_kwd, ufValues);
                                            }
                                        } else {
                                            if (languageSelections.containsValue(langCode.toUpperCase())) {
                                                ArrayList<String> uf_translationValues = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_translations_kwd);
                                                if (uf_translationValues == null) {
                                                    uf_translationValues = new ArrayList<String>();
                                                }
                                                String newValue = langCode.toUpperCase() + translationSeparator + targetValue;

                                                if (uf_translationValues.contains(newValue) == false) {
                                                    uf_translationValues.add(newValue);
                                                    targetTermInfo.descriptorInfo.put(ConstantParameters.uf_translations_kwd, uf_translationValues);
                                                }
                                            }
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="related - rts">
                                else if (openingTagName.equals(ConstantParameters.XML_skos_related)) {
                                    String relatedId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);
                                    if (relatedId != null && relatedId.trim().length() > 0) {
                                        String relatedName = "";
                                        if (idsToNames.containsKey(relatedId)) {
                                            relatedName = idsToNames.get(relatedId);
                                        } else {
                                            relatedName = relatedId;
                                        }
                                        if (relatedName != null && relatedName.trim().length() > 0) {
                                            ArrayList<String> relatedValues = targetTermInfo.descriptorInfo.get(ConstantParameters.rt_kwd);
                                            if (relatedValues == null) {
                                                relatedValues = new ArrayList<String>();
                                            }
                                            if (relatedValues.contains(relatedName) == false) {
                                                relatedValues.add(relatedName);
                                                targetTermInfo.descriptorInfo.put(ConstantParameters.rt_kwd, relatedValues);
                                            }
                                        }
                                    } else {
                                        //in this case it may be a colletion with members nodes
                                        ArrayList<String> collectionIds = new ArrayList<String>();
                                        parseSkosCollectionMembers(xpp, ConstantParameters.XML_skos_related, collectionIds);
                                        if (collectionIds != null) {
                                            for (int k = 0; k < collectionIds.size(); k++) {
                                                String collectionMemberId = collectionIds.get(k);
                                                if (collectionMemberId != null && collectionMemberId.trim().length() > 0) {
                                                    String rtName = "";
                                                    if (idsToNames.containsKey(collectionMemberId)) {
                                                        rtName = idsToNames.get(collectionMemberId);
                                                    } else {
                                                        rtName = collectionMemberId;
                                                    }
                                                    if (rtName != null && rtName.trim().length() > 0) {
                                                        ArrayList<String> rtValues = targetTermInfo.descriptorInfo.get(ConstantParameters.rt_kwd);
                                                        if (rtValues == null) {
                                                            rtValues = new ArrayList<String>();
                                                        }
                                                        if (rtValues.contains(rtName) == false) {
                                                            rtValues.add(rtName);
                                                            targetTermInfo.descriptorInfo.put(ConstantParameters.rt_kwd, rtValues);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="broder - bts">
                                else if (openingTagName.equals(ConstantParameters.XML_skos_broader)) {

                                    String broaderId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);
                                    if (broaderId != null && broaderId.trim().length() > 0) {

                                        String broaderName = "";
                                        if (idsToNames.containsKey(broaderId)) {
                                            broaderName = idsToNames.get(broaderId);
                                        } else {
                                            broaderName = broaderId;
                                        }
                                        if (broaderName != null && broaderName.trim().length() > 0) {
                                            ArrayList<String> btValues = targetTermInfo.descriptorInfo.get(ConstantParameters.bt_kwd);
                                            if (btValues == null) {
                                                btValues = new ArrayList<String>();
                                            }
                                            if (btValues.contains(broaderName) == false) {
                                                btValues.add(broaderName);
                                                targetTermInfo.descriptorInfo.put(ConstantParameters.bt_kwd, btValues);
                                            }
                                        }
                                    } else {
                                        //in this case it may be a colletion with members nodes
                                        ArrayList<String> collectionIds = new ArrayList<String>();
                                        parseSkosCollectionMembers(xpp, ConstantParameters.XML_skos_broader, collectionIds);
                                        if (collectionIds != null) {
                                            for (int k = 0; k < collectionIds.size(); k++) {
                                                String collectionMemberId = collectionIds.get(k);
                                                if (collectionMemberId != null && collectionMemberId.trim().length() > 0) {
                                                    String btName = "";
                                                    if (idsToNames.containsKey(collectionMemberId)) {
                                                        btName = idsToNames.get(collectionMemberId);
                                                    } else {
                                                        btName = collectionMemberId;
                                                    }
                                                    if (btName != null && btName.trim().length() > 0) {
                                                        ArrayList<String> btValues = targetTermInfo.descriptorInfo.get(ConstantParameters.bt_kwd);
                                                        if (btValues == null) {
                                                            btValues = new ArrayList<String>();
                                                        }
                                                        if (btValues.contains(btName) == false) {
                                                            btValues.add(btName);
                                                            targetTermInfo.descriptorInfo.put(ConstantParameters.bt_kwd, btValues);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="narrower - nts">
                                else if (openingTagName.equals(ConstantParameters.XML_skos_narrower)) {
                                    String narrowerId = this.parseSpecificAttibuteValue(ConstantParameters.XML_rdf_resource, xpp);

                                    if (narrowerId != null && narrowerId.trim().length() > 0) {
                                        String ntName = "";
                                        if (idsToNames.containsKey(narrowerId)) {
                                            ntName = idsToNames.get(narrowerId);
                                        } else {
                                            ntName = narrowerId;
                                        }
                                        if (ntName != null && ntName.trim().length() > 0) {
                                            ArrayList<String> ntValues = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
                                            if (ntValues == null) {
                                                ntValues = new ArrayList<String>();
                                            }
                                            if (ntValues.contains(ntName) == false) {
                                                ntValues.add(ntName);
                                                targetTermInfo.descriptorInfo.put(ConstantParameters.nt_kwd, ntValues);
                                            }
                                        }
                                    } else {
                                        //in this case it may be a colletion with members nodes
                                        ArrayList<String> collectionIds = new ArrayList<String>();
                                        parseSkosCollectionMembers(xpp, ConstantParameters.XML_skos_narrower, collectionIds);
                                        if (collectionIds != null) {
                                            for (int k = 0; k < collectionIds.size(); k++) {
                                                String collectionMemberId = collectionIds.get(k);
                                                if (collectionMemberId != null && collectionMemberId.trim().length() > 0) {
                                                    String ntName = "";
                                                    if (idsToNames.containsKey(collectionMemberId)) {
                                                        ntName = idsToNames.get(collectionMemberId);
                                                    } else {
                                                        ntName = collectionMemberId;
                                                    }
                                                    if (ntName != null && ntName.trim().length() > 0) {
                                                        ArrayList<String> ntValues = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
                                                        if (ntValues == null) {
                                                            ntValues = new ArrayList<String>();
                                                        }
                                                        if (ntValues.contains(ntName) == false) {
                                                            ntValues.add(ntName);
                                                            targetTermInfo.descriptorInfo.put(ConstantParameters.nt_kwd, ntValues);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="creator - created_by">
                                else if (openingTagName.equals(ConstantParameters.XML_dc_creator)) {
                                    String targetValue = this.parseSimpleContentElement(xpp);
                                    if (targetValue != null && targetValue.length() > 0) {
                                        ArrayList<String> creatorValues = targetTermInfo.descriptorInfo.get(ConstantParameters.created_by_kwd);
                                        if (creatorValues == null) {
                                            creatorValues = new ArrayList<String>();
                                        }
                                        if (creatorValues.contains(targetValue) == false) {
                                            creatorValues.add(targetValue);
                                            targetTermInfo.descriptorInfo.put(ConstantParameters.created_by_kwd, creatorValues);
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="date - created_on">
                                else if (openingTagName.equals(ConstantParameters.XML_dc_date)) {
                                    String targetValue = this.parseSimpleContentElement(xpp);
                                    //targetValueFormat
                                    if (targetValue != null && targetValue.length() > 0) {
                                        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                        try {
                                            java.util.Date date = formatter.parse(targetValue);
                                            if (date != null) {
                                                targetValue = formatter.format(date);
                                            } else {
                                                targetValue = "";
                                            }
                                        } catch (ParseException ex) {
                                            targetValue = "";
                                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Date conversion error for value '" + targetValue + "' of term  " + termName);
                                            Utils.StaticClass.handleException(ex);
                                        }


                                    }
                                    if (targetValue != null && targetValue.length() > 0) {

                                        ArrayList<String> creationDateValues = targetTermInfo.descriptorInfo.get(ConstantParameters.created_on_kwd);
                                        if (creationDateValues == null) {
                                            creationDateValues = new ArrayList<String>();
                                        }
                                        if (creationDateValues.contains(targetValue) == false) {
                                            creationDateValues.add(targetValue);
                                            targetTermInfo.descriptorInfo.put(ConstantParameters.created_on_kwd, creationDateValues);
                                        }
                                    }
                                } // </editor-fold>
                                // <editor-fold defaultstate="collapsed" desc="scopeNote - scope_note and translations_scope_note">
                                else if (openingTagName.equals(ConstantParameters.XML_skos_scopeNote)) {
                                    String langCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));
                                    String targetValue = this.parseSimpleContentElement(xpp);

                                    if (langCode != null && langCode.length() > 0 && targetValue != null && targetValue.length() > 0) {

                                        if (langCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                            ArrayList<String> snValues = targetTermInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
                                            String Snvalue = "";
                                            if (snValues != null && snValues.size() > 0) {
                                                Snvalue = snValues.get(0);
                                            }

                                            if (Snvalue == null || Snvalue.length() == 0) {
                                                Snvalue = targetValue;
                                            } else if (Snvalue.equals(targetValue) == false) {
                                                Snvalue += " " + targetValue;
                                            }
                                            snValues = new ArrayList<String>();
                                            snValues.add(Snvalue);
                                            targetTermInfo.descriptorInfo.put(ConstantParameters.scope_note_kwd, snValues);

                                        } else {
                                            if (languageSelections.containsValue(langCode.toUpperCase())) {
                                                ArrayList<String> snTrValues = targetTermInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
                                                if (snTrValues == null) {
                                                    snTrValues = new ArrayList<String>();
                                                    targetTermInfo.descriptorInfo.put(ConstantParameters.translations_scope_note_kwd, new ArrayList<String>());
                                                }

                                                String Snvalue = "";
                                                //if (snTrValues != null && snTrValues.size() > 0) {
                                                //    Snvalue = snTrValues.get(0);
                                                //}

                                                //if (Snvalue == null || Snvalue.length() == 0) {
                                                    Snvalue = langCode.toUpperCase() + translationSeparator + " " + targetValue.trim();
                                                //} else if (Snvalue.equals(targetValue) == false) {
                                                //    Snvalue += "\n" + langCode.toUpperCase() + translationSeparator + "\n" + targetValue.trim();
                                                //}
                                                //snTrValues = new ArrayList<String>();
                                                //snTrValues.add(Snvalue);
                                                if(snTrValues.contains(Snvalue)==false){
                                                    targetTermInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd).add(Snvalue);
                                                }
                                            }
                                        }
                                    }

                                }
                                // </editor-fold>
                            }

                        }


                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        int depth = xpp.getDepth();
                        if (depth == 2) {

                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.XML_skos_concept)) {
                                if (termName != null && termName.length() > 0 && targetTermInfo != null) {
                                    if (termsInfo.containsKey(termName) == false) {
                                        termsInfo.put(termName, targetTermInfo);
                                        counter++;
                                        if (counter % 5000 == 0) {
                                            Utils.StaticClass.webAppSystemOutPrintln("Have Read: " + counter + " terms.");
                                        }
                                    } else {
                                        NodeInfoStringContainer ComparetargetTermInfo = termsInfo.get(termName);
                                        Utils.StaticClass.webAppSystemOutPrintln("Dublicate value found for term " + termName);
                                    }
                                }
                                insideConcept = false;
                            }

                        }
                    }
                    //</editor-fold>
                }

            } // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc="THEMAS case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {


                ArrayList<String> validAttrKeywords = new ArrayList<String>();
                validAttrKeywords.add(ConstantParameters.XMLDescriptorElementName);
                for (int i = 0; i < output.length; i++) {
                    validAttrKeywords.add(output[i]);
                }


                // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
                if (xpp == null) {
                    
                    return false;
                }
                String elementName = xpp.getName();
                if (elementName.equals(ConstantParameters.XMLTermsWrapperElementName) == false) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +" Failed to find XML container element for terms: " + ConstantParameters.XMLTermsWrapperElementName);
                    return false;
                }
                //</editor-fold>


                String targetTermName = "";
                String translit = "";
                long targetTermRefId =-1;
                NodeInfoStringContainer targetTermInfo = null;//new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New Terms Encoutered">
                    if (eventType == xpp.START_TAG) {
                        String currentTagName = this.openingTagEncoutered(xpp, null);
                        if (currentTagName.equals(ConstantParameters.XMLTermElementName)) {

                            targetTermName = "";
                            translit = "";
                            targetTermRefId =-1;
                            targetTermInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                            
                        } else if (currentTagName.equals(ConstantParameters.XMLDescriptorElementName)) {
                            
                            String longStr = this.parseSpecificAttibuteValue(ConstantParameters.system_referenceIdAttribute_kwd, xpp);
                            if(longStr!=null && longStr.length()>0){
                                
                                if(longStr!=null && longStr.trim().length()>0){
                                    try{
                                        targetTermRefId = Long.parseLong(longStr);
                                    }
                                    catch(Exception ex){
                                        Utils.StaticClass.handleException(ex);
                                    }
                                }
                            }
                            if(targetTermRefId>0){
                                if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.system_referenceUri_kwd)){
                                    targetTermInfo.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).add(""+targetTermRefId);
                                }
                            }
                            
                            if(Parameters.TransliterationAsAttribute){
                                String targetValue = this.parseSpecificAttibuteValue(ConstantParameters.system_transliteration_kwd, xpp);
                                if (targetValue != null && targetValue.trim().length() > 0) {
                                    translit = this.readXMLTag(targetValue);                                    
                                }
                            }
                            
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                targetTermName = this.readXMLTag(targetValue);
                            }
                        } else if(!Parameters.TransliterationAsAttribute && currentTagName.equals(ConstantParameters.system_transliteration_kwd)) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                translit = this.readXMLTag(targetValue);
                            }
                        }
                        else {
                            String languagePrefix = "";
                            if (currentTagName.equals(ConstantParameters.translation_kwd)
                                    || currentTagName.equals(ConstantParameters.uf_translations_kwd)
                                    || currentTagName.equals(ConstantParameters.translations_scope_note_kwd)) {

                                int howmanyAttributes = xpp.getAttributeCount();
                                for (int k = 0; k < howmanyAttributes; k++) {
                                    if (xpp.getAttributeName(k).equals(ConstantParameters.XMLLinkClassAttributeName)) {
                                        languagePrefix = xpp.getAttributeValue(k);
                                    }
                                }
                            }

                            String parsedValue = this.parseSimpleContentElement(xpp);
                            if (parsedValue != null && parsedValue.trim().length() > 0) {
                                parsedValue = readXMLTag(parsedValue);
                            }


                            if (targetTermInfo.descriptorInfo.containsKey(currentTagName)
                                    && currentTagName.equals(ConstantParameters.translations_scope_note_kwd)) {

                                targetTermInfo.descriptorInfo.get(currentTagName).add(languagePrefix + translationSeparator +" "+ parsedValue);

                                /*ArrayList<String> existingTRSN = targetTermInfo.descriptorInfo.get(currentTagName);

                                if (existingTRSN != null && existingTRSN.size() > 0) {
                                    String existingStr = existingTRSN.get(0);
                                    if (existingStr.length() > 0) {
                                        existingStr += "\n";
                                    }
                                    existingStr += languagePrefix + translationSeparator + "\n" + parsedValue;
                                    existingTRSN.set(0, existingStr);
                                } else {
                                    existingTRSN = new ArrayList<String>();
                                    existingTRSN.add(languagePrefix + translationSeparator + "\n" + parsedValue);
                                }
                                targetTermInfo.descriptorInfo.put(currentTagName, existingTRSN);*/
                            } else if (targetTermInfo.descriptorInfo.containsKey(currentTagName)
                                    && targetTermInfo.descriptorInfo.get(currentTagName).contains(parsedValue) == false) {
                                if (currentTagName.equals(ConstantParameters.translation_kwd)
                                        || currentTagName.equals(ConstantParameters.uf_translations_kwd)) {



                                    if (languagePrefix != null && languagePrefix.trim().length() > 0 && translationSeparator != null && translationSeparator.trim().length() > 0) {
                                        targetTermInfo.descriptorInfo.get(currentTagName).add(languagePrefix + translationSeparator + parsedValue);
                                    } else {
                                        targetTermInfo.descriptorInfo.get(currentTagName).add(parsedValue);
                                    }
                                } else {
                                    targetTermInfo.descriptorInfo.get(currentTagName).add(parsedValue);
                                }
                            }
                        }
                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Term Completed. also Check if all terms are completed">
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);


                        //Check if terms parsing is completed
                        if (currentTagName.equals(ConstantParameters.XMLTermsWrapperElementName)) {
                            break;
                        }

                        //Check if currentTerm parsing is completed
                        if (currentTagName.equals(ConstantParameters.XMLTermElementName)) {
                            if (targetTermInfo == null) {
                                continue;
                            } else if (targetTermName == null || targetTermName.trim().length() == 0) {
                                continue;
                            } else {

                                if(translit==null || translit.length()==0){
                                    translit = Utilities.getTransliterationString(targetTermName, false);
                                }
                                if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.system_transliteration_kwd)){
                                    targetTermInfo.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).add(translit);
                                }
                                //if term info does not exist then add it
                                if (termsInfo.containsKey(targetTermName) == false) {
                                    termsInfo.put(targetTermName, targetTermInfo);
                                } //if term inof existed then updated the old with the new parsed info
                                else {

                                    NodeInfoStringContainer existingInfo = termsInfo.get(targetTermName);
                                    //ArrayList<String> refIdInfo = targetTermInfo.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd);
                                    
                                    Iterator<String> e = targetTermInfo.descriptorInfo.keySet().iterator();
                                    while(e.hasNext()){
                                        String code = e.next();
                                        ArrayList<String> values = targetTermInfo.descriptorInfo.get(code);

                                        if (values != null && values.size() > 0) {
                                            ArrayList<String> existingValues = existingInfo.descriptorInfo.get(code);
                                            if (existingValues == null) {
                                                existingValues = new ArrayList<String>();
                                            }

                                            for (int i = 0; i < values.size(); i++) {
                                                String val = values.get(i);
                                                if (existingValues.contains(val) == false) {
                                                    if(existingValues.size()>0){
                                                        if(code.equals(ConstantParameters.system_referenceUri_kwd)){
                                                            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix +" Term: " + targetTermName +" was found with 2 different Thesaurus referenceIds: " + val +" and "+existingValues.get(0));
                                                            return false;
                                                        }                           
                                                    }
                                                    existingValues.add(val);
                                                }
                                            }
                                            existingInfo.descriptorInfo.put(code, existingValues);
                                        }
                                    }
                                    //merge the 2 nodes
                                }

                            
                                ArrayList<String> bts = termsInfo.get(targetTermName).descriptorInfo.get(ConstantParameters.bt_kwd);
                                ArrayList<String> nts = termsInfo.get(targetTermName).descriptorInfo.get(ConstantParameters.nt_kwd);
                                ArrayList<String> rts = termsInfo.get(targetTermName).descriptorInfo.get(ConstantParameters.rt_kwd);

                                for (int k = 0; k < bts.size(); k++) {
                                    String checkExists = bts.get(k);
                                    if (termsInfo.containsKey(checkExists) == false) {
                                        NodeInfoStringContainer newInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                                        newInfo.descriptorInfo.get(ConstantParameters.nt_kwd).add(targetTermName);
                                        termsInfo.put(checkExists,newInfo);
                                    }

                                }
                                for (int k = 0; k < nts.size(); k++) {
                                    String checkExists = nts.get(k);
                                    if (termsInfo.containsKey(checkExists) == false) {
                                        NodeInfoStringContainer newInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                                        newInfo.descriptorInfo.get(ConstantParameters.bt_kwd).add(targetTermName);
                                        termsInfo.put(checkExists,newInfo);
                                    }
                                }
                                for (int k = 0; k < rts.size(); k++) {
                                    String checkExists = rts.get(k);
                                    if (termsInfo.containsKey(checkExists) == false) {
                                        NodeInfoStringContainer newInfo = new NodeInfoStringContainer(NodeInfoStringContainer.CONTAINER_TYPE_TERM, output);
                                        newInfo.descriptorInfo.get(ConstantParameters.rt_kwd).add(targetTermName);
                                        termsInfo.put(checkExists,newInfo);
                                    }
                                }
                            }


                        }
                    }
                    //</editor-fold>
                }

            }
            // </editor-fold>

        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
    }

    private void parseTranslationCategories(XmlPullParser xpp, ArrayList<String> userSelectedTranslationWords, ArrayList<String> userSelectedTranslationIdentifiers, HashMap<String, String> userSelections) {
        try {

            // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
            if (xpp == null) {
                return;
            }
            String elementName = xpp.getName();
            if (elementName.equals("TranslationCategories") == false) {
                return;
            }
            //</editor-fold>


            String targetWord = "";
            String targetIdentifier = "";

            while (xpp.getEventType() != xpp.END_DOCUMENT) {
                xpp.next();
                int eventType = xpp.getEventType();

                // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New TranslationPair Encoutered">
                if (eventType == xpp.START_TAG) {
                    String currentTagName = this.openingTagEncoutered(xpp, null);
                    if (currentTagName.equals("TranslationPair")) {
                        targetWord = "";
                        targetIdentifier = "";

                    } else if (currentTagName.equals("TranslationWord")) {
                        targetWord = this.readXMLTag(this.parseSimpleContentElement(xpp));


                    } else if (currentTagName.equals("TranslationIdentifier")) {
                        targetIdentifier = this.readXMLTag(this.parseSimpleContentElement(xpp));
                    }
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all TranslationCategories are completed">
                else if (eventType == xpp.END_TAG) {

                    String currentTagName = this.closingTagEncoutered(xpp, null);


                    //Check if TranslationCategories parsing is completed
                    if (currentTagName.equals("TranslationCategories")) {
                        break;
                    }

                    //Check if current TranslationPair parsing is completed
                    if (currentTagName.equals("TranslationPair")) {

                        if (targetWord != null && targetWord.trim().length() > 0 && targetIdentifier != null && targetIdentifier.trim().length() > 0) {
                            userSelectedTranslationWords.add(targetWord);
                        }
                        userSelectedTranslationIdentifiers.add(targetIdentifier);
                        userSelections.put(targetWord, targetIdentifier);
                    }
                }
                //</editor-fold>
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private String parseSpecificAttibuteValue(String attrName, XmlPullParser xpp) {

        String returnVal = "";
        if (xpp != null) {
            int howmanyAttrs = xpp.getAttributeCount();
            for (int i = 0; i < howmanyAttrs; i++) {
                String currentAttrName = xpp.getAttributeName(i);
                if (currentAttrName.equals(attrName)) {
                    returnVal = xpp.getAttributeValue(i);
                    break;
                }
            }
        }
        return returnVal;
    }

    private void parseGuideTermNodes(XmlPullParser xpp, String xmlSchemaType, ArrayList<String> xmlGuideTerms) {

        try {
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
                if (xpp == null) {
                    return;
                }
                String elementName = xpp.getName();
                if (elementName.equals(ConstantParameters.XMLNodeLabelsWrapperElementName) == false) {
                    return;
                }
                //</editor-fold>


                //String targetFacetName = "";

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New Facet Encoutered">
                    if (eventType == xpp.START_TAG) {
                        String currentTagName = this.openingTagEncoutered(xpp, null);
                        if (currentTagName.equals(ConstantParameters.XMLNodeLabelElementName)) {
                            String targetValue = this.parseSimpleContentElement(xpp).trim();
                            if(xmlGuideTerms.contains(targetValue)==false){
                                xmlGuideTerms.add(targetValue);
                            }
                            //targetFacetName = "";
                        } 
                    } //</editor-fold>
                    
                }
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }
    
    private void parseFacetNodes(XmlPullParser xpp, String xmlSchemaType, ArrayList<String> xmlFacets) {
        
        try {
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                String targetFacetName = "";
                boolean insideCollection = false;
                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case">
                    if (eventType == xpp.START_TAG) {


                        String currentTagName = this.openingTagEncoutered(xpp, null);

                        if (currentTagName.equals(ConstantParameters.XML_skos_collection)) {
                            if (xpp.getDepth() == 2) {
                                targetFacetName = "";
                                insideCollection = true;
                            }
                        } else if (insideCollection && currentTagName.equals(ConstantParameters.XML_skos_prefLabel)) {
                            int depth = xpp.getDepth();

                            if (depth == 3) {
                                String langCode = this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp);
                                String targetValue = this.parseSimpleContentElement(xpp);

                                //Utils.StaticClass.webAppSystemOutPrintln(targetValue + " " + langCode + " " + depth);

                                if (langCode != null) {
                                    if (langCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                        if (targetValue != null && targetValue.trim().length() > 0) {
                                            targetFacetName = this.readXMLTag(targetValue);
                                            if (xmlFacets.contains(targetFacetName) == false) {
                                                xmlFacets.add(targetFacetName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case">
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);

                        if (currentTagName.equals(ConstantParameters.XML_skos_collection)) {
                            if (xpp.getDepth() == 2) {
                                insideCollection = false;
                            }
                        }
                    }
                    //</editor-fold>


                }
                //skos:Collection
                //skos:prefLabel xml:lang="en"

            } else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                HashMap<String,SortItem> xmlFacetsInSortItems = new HashMap<String,SortItem>();
                
                parseFacetNodesinSortItems(xpp, xmlSchemaType, xmlFacetsInSortItems);
                xmlFacets.addAll(xmlFacetsInSortItems.keySet());
                
            }
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        }
    }

    private boolean parseFacetNodesinSortItems(XmlPullParser xpp, String xmlSchemaType, HashMap<String, SortItem> xmlFacets) {

        try {
            
            String targetFacetName = "";
            String translit = "";
            long refId = -1;
            
            // <editor-fold defaultstate="collapsed" desc="Skos case not supported yet..">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {

                
                /*
                targetFacetName = "";
                boolean insideCollection = false;
                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    
                    if (eventType == xpp.START_TAG) {


                        String currentTagName = this.openingTagEncoutered(xpp, null);

                        if (currentTagName.equals(ConstantParameters.XML_skos_collection)) {
                            if (xpp.getDepth() == 2) {
                                targetFacetName = "";
                                insideCollection = true;
                            }
                        } else if (insideCollection && currentTagName.equals(ConstantParameters.XML_skos_prefLabel)) {
                            int depth = xpp.getDepth();

                            if (depth == 3) {
                                String langCode = this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp);
                                String targetValue = this.parseSimpleContentElement(xpp);

                                //Utils.StaticClass.webAppSystemOutPrintln(targetValue + " " + langCode + " " + depth);

                                if (langCode != null) {
                                    if (langCode.toLowerCase().equals(Parameters.PrimaryLang.toLowerCase())) {
                                        if (targetValue != null && targetValue.trim().length() > 0) {
                                            targetFacetName = this.readXMLTag(targetValue);
                                            if (xmlFacets.contains(targetFacetName) == false) {
                                                xmlFacets.add(targetFacetName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } 
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);

                        if (currentTagName.equals(ConstantParameters.XML_skos_collection)) {
                            if (xpp.getDepth() == 2) {
                                insideCollection = false;
                            }
                        }
                    }
                    


                }
                //skos:Collection
                //skos:prefLabel xml:lang="en"
                */
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "parseFacetNodesSortItems SKOS schema is not yet supported.");
                return false;
            } 
            //</editor-fold>
            
            // <editor-fold defaultstate="collapsed" desc="THEMAS schema..">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {


                // <editor-fold defaultstate="collapsed" desc="Check if the correct xpp element was given">
                if (xpp == null) {
                    return false;
                }
                String elementName = xpp.getName();
                if (elementName.equals("facets") == false) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + " Failed to retrieve XML element facets.");
                    return false;
                }
                //</editor-fold>

                while (xpp.getEventType() != xpp.END_DOCUMENT) {
                    xpp.next();
                    int eventType = xpp.getEventType();

                    // <editor-fold defaultstate="collapsed" desc="Start Tag Case --> New Facet Encoutered">
                    if (eventType == xpp.START_TAG) {
                        String currentTagName = this.openingTagEncoutered(xpp, null);
                        if (currentTagName.equals("facet")) {
                            targetFacetName = "";
                            translit = "";
                            refId = -1;
                        } else if (currentTagName.equals("name")) {
                            
                            String longStr = this.parseSpecificAttibuteValue(ConstantParameters.system_referenceIdAttribute_kwd, xpp);
                            if(longStr!=null && longStr.length()>0){
                                
                                if(longStr!=null && longStr.trim().length()>0){
                                    try{
                                        refId = Long.parseLong(longStr);
                                    }
                                    catch(Exception ex){
                                        Utils.StaticClass.handleException(ex);
                                    }
                                }
                            }
                            
                            if(Parameters.TransliterationAsAttribute){
                                String targetValue = this.parseSpecificAttibuteValue(ConstantParameters.system_transliteration_kwd, xpp);
                                    if (targetValue != null && targetValue.trim().length() > 0) {
                                    translit = this.readXMLTag(targetValue);
                                }
                            }
                            
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                targetFacetName = this.readXMLTag(targetValue);
                            }
                        }
                        else if (!Parameters.TransliterationAsAttribute && currentTagName.equals(ConstantParameters.system_transliteration_kwd)) {
                            String targetValue = this.parseSimpleContentElement(xpp);

                            if (targetValue != null && targetValue.trim().length() > 0) {
                                translit = this.readXMLTag(targetValue);
                            }                            
                        }
                    } //</editor-fold>
                    // <editor-fold defaultstate="collapsed" desc="End Tag Case --> Facet Completed. also Check if all Facets are completed">
                    else if (eventType == xpp.END_TAG) {

                        String currentTagName = this.closingTagEncoutered(xpp, null);


                        //Check if terms parsing is completed
                        if (currentTagName.equals("facets")) {
                            break;
                        }

                        //Check if currentFacet parsing is completed
                        if (currentTagName.equals("facet")) {
                            if (targetFacetName == null || targetFacetName.trim().length() == 0) {
                                continue;
                            } else {

                                //if term info does not exist then add it
                                if(translit==null || translit.trim().length()==0){
                                    translit = Utilities.getTransliterationString(targetFacetName, false);
                                }
                                SortItem newFacetObj = new SortItem(targetFacetName,-1,translit,refId);
                                
                                
                                if (xmlFacets.containsKey(targetFacetName) == false) {
                                    xmlFacets.put(targetFacetName,newFacetObj);
                                }
                                else{
                                    if(!newFacetObj.equals(xmlFacets.get(targetFacetName))){
                                        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + " Facet: "+targetFacetName + " is defined more than once.");
                                        return false;
                                    }
                                }
                            }


                        }
                    }
                    //</editor-fold>
                }
            }
            //</editor-fold>
            
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
            return false;
        }
        return true;
    }

    public String readXMLTag(String test) {
        if (test == null) {
            return null;
        }
        String newStr = test.replaceAll("\u00A0", " ");
        newStr = newStr.replaceAll(" +", " ");
        newStr = newStr.trim();
        return newStr;

    }

    // <editor-fold defaultstate="collapsed" desc="parseSimpleContentElement">
    private String parseSimpleContentElement(XmlPullParser xpp) {

        String returnVal = "";

        boolean continueSearching = true;

        if (xpp != null) {

            try {
                int eventType = xpp.getEventType();
                if (eventType != xpp.START_TAG) {
                    return returnVal;
                }
                //String openingTagName = xpp.getName();

                while (continueSearching) {

                    xpp.next();
                    eventType = xpp.getEventType();
                    if (eventType == xpp.TEXT) {
                        returnVal += this.readXMLTag(xpp.getText());
                    } else if (eventType == xpp.START_TAG) {
                        //this should not be encoutered. This code is included just for safety
                        //String openingTag =
                        continueSearching = false;
                        break;
                    } else if (eventType == xpp.END_TAG) {
                        //in any case code should return as current element should not contain children
                        //String closingTag =
                        continueSearching = false;
                        break;
                    }
                }
            } catch (XmlPullParserException ex) {
                Utils.StaticClass.handleException(ex);
            } catch (IOException ex) {
                Utils.StaticClass.handleException(ex);
            }
        }


        return returnVal;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="openingTagEncoutered">
    private String openingTagEncoutered(XmlPullParser xpp, java.util.Stack<String> elementsTrace) {
        String returnVal = "";
        returnVal = xpp.getName();

        if (PRINTXMLMESSAGES) {
            Utils.StaticClass.webAppSystemOutPrintln("Found opening tag: " + returnVal);
        }
        if (elementsTrace != null) {
            elementsTrace.push(returnVal);
        }

        return returnVal;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="closingTagEncoutered">
    private String closingTagEncoutered(XmlPullParser xpp, java.util.Stack<String> elementsTrace) {
        String returnVal = "";

        returnVal = xpp.getName();
        if (PRINTXMLMESSAGES) {
            Utils.StaticClass.webAppSystemOutPrintln("Found closing tag: " + returnVal);
        }
        if (elementsTrace != null && elementsTrace.get(elementsTrace.size() - 1).equals(returnVal)) {
            elementsTrace.pop();
        }
        return returnVal;
    }
    // </editor-fold>

    public boolean readTranslationCategoriesFromTerms(HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> filterTerms,
            ArrayList<String> userSelectedTranslationWords, ArrayList<String> userSelectedTranslationIdentifiers,
            HashMap<String, String> userSelections){
        boolean applyFiltering = (filterTerms!=null && filterTerms.size()>0);
        Iterator<String> termEnum = termsInfo.keySet().iterator();
        while(termEnum.hasNext()){
            String targetTerm = termEnum.next();
            if(applyFiltering){
                if(filterTerms.contains(targetTerm)==false){
                    continue;
                }
            }
            NodeInfoStringContainer targetInfo = termsInfo.get(targetTerm);
            if(targetInfo.descriptorInfo.containsKey(ConstantParameters.translation_kwd)){
                ArrayList<String> checkVals = targetInfo.descriptorInfo.get(ConstantParameters.translation_kwd);
                for(int k=0; k<checkVals.size();k++){
                    String val = checkVals.get(k);
                    if(val.indexOf(Parameters.TRANSLATION_SEPERATOR)>0){
                        String langcode = val.substring(0, val.indexOf(Parameters.TRANSLATION_SEPERATOR));
                        String langidentifier = Linguist.SupportedTHEMASLangcodes(langcode);
                        if(langidentifier.length()>0){
                            langidentifier = langidentifier.toUpperCase();
                            String langword = Linguist.SupportedLanguages(langidentifier);

                            if (langword != null && langword.trim().length() > 0
                                    && langidentifier != null && langidentifier.trim().length() > 0) {
                                if(userSelections.containsKey(langword)==false){
                                    userSelections.put(langword, langidentifier);
                                    userSelectedTranslationWords.add(langword);
                                    userSelectedTranslationIdentifiers.add(langidentifier);
                                }

                            }
                        }
                    }
                }
                
            }
            if(targetInfo.descriptorInfo.containsKey(ConstantParameters.uf_translations_kwd)){
                ArrayList<String> checkVals = targetInfo.descriptorInfo.get(ConstantParameters.uf_translations_kwd);
                for(int k=0; k<checkVals.size();k++){
                    String val = checkVals.get(k);
                    if(val.indexOf(Parameters.TRANSLATION_SEPERATOR)>0){
                        String langcode = val.substring(0, val.indexOf(Parameters.TRANSLATION_SEPERATOR));
                        String langidentifier = Linguist.SupportedTHEMASLangcodes(langcode);
                        if(langidentifier.length()>0){
                            langidentifier = langidentifier.toUpperCase();
                            String langword = Linguist.SupportedLanguages(langidentifier);

                            if (langword != null && langword.trim().length() > 0
                                    && langidentifier != null && langidentifier.trim().length() > 0) {
                                if(userSelections.containsKey(langword)==false){
                                    userSelections.put(langword, langidentifier);
                                    userSelectedTranslationWords.add(langword);
                                    userSelectedTranslationIdentifiers.add(langidentifier);
                                }

                            }
                        }
                    }
                }
            }
            if(targetInfo.descriptorInfo.containsKey(ConstantParameters.translations_scope_note_kwd)){
               ArrayList<String> checkVals = targetInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);
               for(int k=0; k<checkVals.size();k++){
                    String val = checkVals.get(k);
                    if(val.indexOf(Parameters.TRANSLATION_SEPERATOR)>0){
                        String langcode = val.substring(0, val.indexOf(Parameters.TRANSLATION_SEPERATOR));
                        String langidentifier = Linguist.SupportedTHEMASLangcodes(langcode);
                        if(langidentifier.length()>0){
                            langidentifier = langidentifier.toUpperCase();
                            String langword = Linguist.SupportedLanguages(langidentifier);

                            if (langword != null && langword.trim().length() > 0
                                    && langidentifier != null && langidentifier.trim().length() > 0) {
                                if(userSelections.containsKey(langword)==false){
                                    userSelections.put(langword, langidentifier);
                                    userSelectedTranslationWords.add(langword);
                                    userSelectedTranslationIdentifiers.add(langidentifier);
                                }

                            }
                        }
                    }
                }
            }
        }
        /*
         if (targetWord != null && targetWord.trim().length() > 0 && targetIdentifier != null && targetIdentifier.trim().length() > 0) {
                            userSelectedTranslationWords.add(targetWord);
                        }
                        userSelectedTranslationIdentifiers.add(targetIdentifier);
                        userSelections.put(targetWord, targetIdentifier);
                        */
        return true;
    }

    public boolean readTranslationCategories(String xmlFilePath, String xmlSchemaType, ArrayList<String> userSelectedTranslationWords,
            ArrayList<String> userSelectedTranslationIdentifiers, HashMap<String, String> userSelections) {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Start reading translation categories from file: " + xmlFilePath + ".");

        XmlPullParserFactory factory;
        try {

            //2cases -->
            //TermType/Term_Languages/Term_Language/Language
            //Vocabulary/Subject/Descriptive_Notes/Descriptive_Note/Note_Language

            //<editor-fold defaultstate="collapsed" desc="AAT Case">
            if (xmlSchemaType.equals(ConstantParameters.xmlschematype_aat)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);


                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));

                boolean insideSubject = false;
                boolean insideDescriptiveNote = false;
                boolean insideTermLanguages = false;

                ArrayList<String> langCodes = new ArrayList<String>();
                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        int depth = xpp.getDepth();

                        if (depth == 2) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_subject_tag)) {
                                insideSubject = true;
                            }
                        } else if (insideSubject && depth > 3) {
                            String openingTagName = this.openingTagEncoutered(xpp, null);
                            if (openingTagName.equals(ConstantParameters.aat_Descriptive_NotesWrapper_tag)) {
                                insideDescriptiveNote = true;

                            } else if (insideDescriptiveNote && openingTagName.equals(ConstantParameters.aat_Note_Language_tag)) {
                                String langString = Linguist.AATLanguageAcronyms(this.parseSimpleContentElement(xpp));
                                if (langString != null && langString.length() > 0) {
                                    String langCode = langString.toUpperCase();
                                    if (langCodes.contains(langCode) == false) {
                                        langCodes.add(langCode);
                                    }

                                }
                            } else if (openingTagName.equals(ConstantParameters.aat_Term_LanguagesWrapper_tag)) {
                                insideTermLanguages = true;
                            } else if (insideTermLanguages && openingTagName.equals(ConstantParameters.aat_Term_Language_Language_tag)) {
                                String langString = Linguist.AATLanguageAcronyms(this.parseSimpleContentElement(xpp));
                                if (langString != null && langString.length() > 0) {
                                    String langCode = langString.toUpperCase();
                                    if (langCodes.contains(langCode) == false) {
                                        langCodes.add(langCode);
                                    }
                                }
                            }

                        }
                    } else if (eventType == xpp.END_TAG) {
                        int depth = xpp.getDepth();
                        if (depth == 2) {
                            String closingTagName = this.closingTagEncoutered(xpp, null);
                            if (closingTagName.equals(ConstantParameters.aat_subject_tag)) {

                                insideSubject = false;
                            }
                        }
                    }

                    eventType = xpp.next();
                }

                for (int k = 0; k < langCodes.size(); k++) {
                    String langCode = langCodes.get(k);
                    String langWord = Linguist.SupportedLanguages(langCode);
                    if (langWord != null && langWord.trim().length() > 0) {
                        userSelectedTranslationWords.add(langWord);
                        userSelectedTranslationIdentifiers.add(langCode);
                        userSelections.put(langWord, langCode);

                        Utils.StaticClass.webAppSystemOutPrintln("Found tr category: " + langCode + " " + langWord);
                    }

                }


            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="SKOS Case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_skos)) {
                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                ArrayList<String> langCodes = new ArrayList<String>();
                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String langCode = Linguist.SupportedTHEMASLangcodes(this.parseSpecificAttibuteValue(ConstantParameters.XML_xml_lang, xpp));
                        if (langCode != null && langCode.length() > 0 && langCodes.contains(langCode.toUpperCase()) == false) {
                            langCodes.add(langCode.toUpperCase());
                        }
                    }
                    eventType = xpp.next();
                }

                for (int k = 0; k < langCodes.size(); k++) {
                    String langCode = langCodes.get(k);
                    String langWord = Linguist.SupportedLanguages(langCode);
                    if (langWord != null && langWord.trim().length() > 0) {
                        userSelectedTranslationWords.add(langWord);
                        userSelectedTranslationIdentifiers.add(langCode);
                        userSelections.put(langWord, langCode);

                        Utils.StaticClass.webAppSystemOutPrintln("Found tr category: " + langCode + " " + langWord);
                    }

                }

            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="THEMAS Case">
            else if (xmlSchemaType.equals(ConstantParameters.xmlschematype_THEMAS)) {

                factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(new FileInputStream(xmlFilePath), "UTF-8"));


                int eventType = xpp.getEventType();

                while (eventType != xpp.END_DOCUMENT) {

                    if (eventType == xpp.START_TAG) {
                        String openingTagName = this.openingTagEncoutered(xpp, null);
                        if (openingTagName.equals("TranslationCategories")) {
                            this.parseTranslationCategories(xpp, userSelectedTranslationWords, userSelectedTranslationIdentifiers, userSelections);
                            break;
                        }
                    }
                    eventType = xpp.next();
                }

            }
            //</editor-fold>

        } catch (FileNotFoundException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (IOException ex) {
            Utils.StaticClass.handleException(ex);
        } catch (XmlPullParserException ex) {
            Utils.StaticClass.handleException(ex);
        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "End of reading translation categories. Found " + userSelections.size() + " translation categories.");
        return true;
    }

    public ArrayList<String> getRecursiveNts(ArrayList<String> targetSet, HashMap<String, NodeInfoStringContainer> termsInfo) {
        DBGeneral dbGen = new DBGeneral();

        ArrayList<String> returnVals = new ArrayList<String>();
        returnVals.addAll(targetSet);

        ArrayList<String> newVals = new ArrayList<String>();
        newVals.addAll(targetSet);

        ArrayList<String> loopVals = new ArrayList<String>();
        while (newVals.size() > 0) {
            loopVals = new ArrayList<String>();

            for (int i = 0; i < newVals.size(); i++) {
                String checkTerm = newVals.get(i);
                ArrayList<String> nts = new ArrayList<String>();
                if (termsInfo.containsKey(checkTerm)) {
                    nts.addAll(termsInfo.get(checkTerm).descriptorInfo.get(ConstantParameters.nt_kwd));
                }

                for (int j = 0; j < nts.size(); j++) {
                    String checkNt = nts.get(j);
                    if (returnVals.contains(checkNt) == false) {
                        returnVals.add(checkNt);
                        loopVals.add(checkNt);
                    }
                }
            }
            newVals.clear();
            newVals.addAll(loopVals);
        }

        return returnVals;
    }

    public static String readSkosTC(String value) {
        if (value != null) {
            if (value.contains("/")) {
                String[] parts = value.split("/");
                if (parts != null && parts.length > 1) {
                    String returnVal = parts[parts.length - 1];
                    if (returnVal != null && returnVal.length() > 0) {
                        return returnVal;
                    } else {
                        return value;
                    }
                } else {
                    return value;
                }
            } else {
                return value;
            }

        }
        return "";
    }
}
