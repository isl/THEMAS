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

//package XMLHandling;
import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Utils.ConstantParameters;
import Utils.Linguist;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SortItem;
import Utils.SortItemComparator;
import Utils.SortItemLocaleComparator;
import Utils.Utilities;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import neo4j_sisapi.StringObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author tzortzak
 */
public class WriteFileData {

    private String getSkosSchemePrefix(String importThesaurusName) {

        if (ConstantParameters.includeThesaurusNameInScheme) {
            return ConstantParameters.SchemePrefix + "/" + importThesaurusName.toLowerCase().replaceAll(" ", "_");
        }
        return ConstantParameters.SchemePrefix;
    }

    public void WriteFileStart(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName) throws IOException {

        Utilities u = new Utilities();
        
        //locale/footer/tooltipappnameandversion
        logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"

        logFileWriter.append("<!-- " + u.translateFromTranslationsXML("locale/footer/tooltipappnameandversion", null) + " -->\r\n");
        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            logFileWriter.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n"
                    + "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n\txmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" "
                    + "\r\n\txmlns:dc=\"http://purl.org/dc/elements/1.1/\">\r\n\r\n");
            logFileWriter.append("\t<skos:ConceptScheme rdf:about=\"" + ConstantParameters.referenceThesaurusSchemeName + "\">\r\n"
                    + "\t\t<skos:prefLabel>" + Utilities.escapeXML(importThesaurusName) + "</skos:prefLabel>\r\n"
                    + "\t\t<dc:date>" + Utilities.GetNow() + "</dc:date>\r\n"
                    + "\t</skos:ConceptScheme>\r\n");

        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            //logFileWriter.append("<data thesaurus=\"" + Utilities.escapeXML(importThesaurusName) + "\" exportDate=\"" + Utilities.GetNow() + "\" \r\n\t"            
            //+ "xmlns=\"http://localhost/THEMAS\"\r\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
            logFileWriter.append("<data thesaurus=\"" + Utilities.escapeXML(importThesaurusName) + "\" exportDate=\"" + Utilities.GetNow() + "\">\r\n");
        }

        logFileWriter.flush();
    }

    public void WriteTranslationCategories(OutputStreamWriter logFileWriter, String exportScheme, Hashtable<String, String> translationPairs) throws IOException {

        if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting translation categories");
            logFileWriter.write("\r\n\t<TranslationCategories translationSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">\r\n");

            Vector<String> sortedTrCategs = new Vector<String>(translationPairs.keySet());
            Collections.sort(sortedTrCategs);
            //Enumeration<String> allTrCategories = translationPairs.keys();
            for (String languageWord : sortedTrCategs) {
                //String languageWord = allTrCategories.nextElement();
                String languageId = translationPairs.get(languageWord);

                logFileWriter.write("\t\t<TranslationPair>\r\n");
                logFileWriter.write("\t\t\t<TranslationWord>");
                logFileWriter.write(languageWord);
                logFileWriter.write("</TranslationWord>\r\n");
                logFileWriter.write("\t\t\t<TranslationIdentifier>");
                logFileWriter.write(languageId);
                logFileWriter.write("</TranslationIdentifier>\r\n");
                logFileWriter.write("\t\t</TranslationPair>\r\n");

            }
            logFileWriter.write("\t</TranslationCategories>\r\n");
        }
        logFileWriter.flush();
    }

    public void WriteGuideTerms(OutputStreamWriter logFileWriter, String exportScheme,
            Vector<String> GuideTerms) throws IOException {

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            Vector<String> GuideTermsToExport = new Vector<String>();

            GuideTermsToExport.addAll(GuideTerms);
            Collections.sort(GuideTermsToExport);

            if (GuideTermsToExport.size() > 0) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Node Labels");
                logFileWriter.append("\r\n\t<!-- Export of Node Labels - Guide Terms in SKOS not implemented yet ");
                logFileWriter.append("\r\n\t<" + ConstantParameters.XMLNodeLabelsWrapperElementName + ">\r\n");

                for (int i = 0; i < GuideTermsToExport.size(); i++) {
                    logFileWriter.append("\t\t<" + ConstantParameters.XMLNodeLabelElementName + " index=\"" + (i + 1) + "\">");
                    //logFileWriter.append("\t\t\t<name>");
                    logFileWriter.append(Utilities.escapeXML(GuideTermsToExport.get(i)));
                    //logFileWriter.append("</name>\r\n");
                    logFileWriter.append("</" + ConstantParameters.XMLNodeLabelElementName + ">\r\n");
                }

                logFileWriter.append("\t</" + ConstantParameters.XMLNodeLabelsWrapperElementName + ">\r\n");
                logFileWriter.append("\r\n\t-->\r\n");
            }

        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            Vector<String> GuideTermsToExport = new Vector<String>();
            GuideTermsToExport.addAll(GuideTerms);

            Collections.sort(GuideTermsToExport);

            if (GuideTermsToExport.size() > 0) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Node Labels");
                logFileWriter.append("\r\n\t<" + ConstantParameters.XMLNodeLabelsWrapperElementName + " count=\"" + GuideTermsToExport.size() + "\">\r\n");

                for (int i = 0; i < GuideTermsToExport.size(); i++) {
                    //logFileWriter.append("\t\t<"+ConstantParameters.XMLNodeLabelElementName+" index=\"" + (i + 1) + "\">");
                    logFileWriter.append("\t\t<" + ConstantParameters.XMLNodeLabelElementName + ">");
                    //logFileWriter.append("\t\t\t<name>");
                    logFileWriter.append(Utilities.escapeXML(GuideTermsToExport.get(i)));
                    //logFileWriter.append("</name>\r\n");
                    logFileWriter.append("</" + ConstantParameters.XMLNodeLabelElementName + ">\r\n");
                }

                logFileWriter.append("\t</" + ConstantParameters.XMLNodeLabelsWrapperElementName + ">\r\n");
            }

        }
        logFileWriter.flush();
    }

    public void WriteFacets(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            Vector<String> xmlFacets, Hashtable<String, Vector<String>> hierarchyFacets,
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            Vector<String> FacetsFilter,
            Vector<String> HierarchiesFilter) throws IOException {
        
        Vector<SortItem> xmlFacetssortItems = Utilities.getSortItemVectorFromStringVector(xmlFacets,false);
        WriteFacetsFromSortItems(logFileWriter,exportScheme,importThesaurusName,
            xmlFacetssortItems, hierarchyFacets,
            termsInfo,
            FacetsFilter,
            HierarchiesFilter);
                
                
    }
        
    
    public void WriteFacetsFromSortItems(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            Vector<SortItem> xmlFacets, Hashtable<String, Vector<String>> hierarchyFacets,
            Hashtable<String, NodeInfoStringContainer> termsInfo,
            Vector<String> FacetsFilter,
            Vector<String> HierarchiesFilter) throws IOException {

        Utilities u = new Utilities();
        Vector<String> facetFilter = new Vector<String>();
        Vector<String> hierarchiesFilter = new Vector<String>();

        if (FacetsFilter != null && FacetsFilter.size() > 0) {
            facetFilter.addAll(FacetsFilter);
        }

        if (HierarchiesFilter != null && HierarchiesFilter.size() > 0) {
            hierarchiesFilter.addAll(HierarchiesFilter);
        }

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            String schemePrefix = this.getSkosSchemePrefix(importThesaurusName);

            Hashtable<String, Vector<String>> facetHierarchyIds = new Hashtable<String, Vector<String>>();

            //getall facets and put a record in the above structure
            Vector<SortItem> facetsToExportInSortItemFormat = new Vector<SortItem>();
            Vector<String> facetsToExportInStringFormat = new Vector<String>();
            
            if (facetFilter.isEmpty()) {
                facetsToExportInSortItemFormat.addAll(xmlFacets);
                facetsToExportInStringFormat.addAll(Utilities.getStringVectorFromSortItemVector(xmlFacets));

                Enumeration<String> hierEnum = hierarchyFacets.keys();
                while (hierEnum.hasMoreElements()) {
                    String hierarchyName = hierEnum.nextElement();
                    Vector<String> facets = hierarchyFacets.get(hierarchyName);
                    if (facets != null) {
                        for (int k = 0; k < facets.size(); k++) {
                            String facetName = facets.get(k);
                            if (facetName != null && facetName.length() > 0 && facetsToExportInStringFormat.contains(facetName) == false) {
                                facetsToExportInSortItemFormat.add(new SortItem(facetName,-1,Utilities.getTransliterationString(facetName, false),-1));
                                facetsToExportInStringFormat.add(facetName);
                            }
                        }
                    }
                }
            } else {
                
                facetsToExportInStringFormat.addAll(facetFilter);
                facetsToExportInSortItemFormat.addAll(Utilities.getSortItemVectorFromStringVector(facetFilter, false));
            }

            Collections.sort(facetsToExportInSortItemFormat, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));

            for (int i = 0; i < facetsToExportInSortItemFormat.size(); i++) {
                String facetName = facetsToExportInSortItemFormat.get(i).getLogName();
                if (facetName != null && facetName.length() > 0 && facetHierarchyIds.containsKey(facetName) == false) {
                    facetHierarchyIds.put(facetName, new Vector<String>());
                }
            }

            //now find the ids of each hierarchy and fill the value vector of the main structure
            Enumeration<String> hierEnum = hierarchyFacets.keys();
            while (hierEnum.hasMoreElements()) {
                String hierarchyName = hierEnum.nextElement();
                if (hierarchiesFilter.size() > 0 && hierarchiesFilter.contains(hierarchyName) == false) {
                    continue;
                }
                String hierarchyId = "";
                NodeInfoStringContainer hierarchyInfo = termsInfo.get(hierarchyName);

                Vector<String> tcs = hierarchyInfo.descriptorInfo.get(ConstantParameters.tc_kwd);
                if (tcs != null && tcs.size() > 0) {
                    hierarchyId = ParseFileData.readSkosTC(tcs.get(0));
                }

                if (hierarchyId.length() > 0) {
                    hierarchyId = schemePrefix + "/" + hierarchyId;
                }

                Vector<String> facets = hierarchyFacets.get(hierarchyName);
                if (facets != null) {
                    for (int k = 0; k < facets.size(); k++) {
                        String facetName = facets.get(k);
                        if (facetName != null && facetName.length() > 0 && facetHierarchyIds.containsKey(facetName)) {

                            Vector<String> hierIds = facetHierarchyIds.get(facetName);
                            if (hierIds.contains(hierarchyId) == false) {
                                hierIds.add(hierarchyId);
                                facetHierarchyIds.put(facetName, hierIds);
                            }
                        }
                    }
                }

            }
            Hashtable<String, String> idToName = new Hashtable<String, String>();
            Enumeration<String> termEnum = termsInfo.keys();
            while (termEnum.hasMoreElements()) {
                String termName = termEnum.nextElement();
                String termId = "";
                NodeInfoStringContainer targetInfo = termsInfo.get(termName);
                if (targetInfo != null && targetInfo.descriptorInfo.containsKey(ConstantParameters.tc_kwd)) {
                    Vector<String> tcs = targetInfo.descriptorInfo.get(ConstantParameters.tc_kwd);
                    if (tcs != null && tcs.size() == 1) {
                        termId = ConstantParameters.SchemePrefix + "/" + tcs.get(0);
                    }
                }
                if (termId.length() > 0 && idToName.containsKey(termId) == false) {
                    idToName.put(termId, termName);
                }
            }

            //now write to file
            for (int p = 0; p < facetsToExportInSortItemFormat.size(); p++) {
                //Enumeration<String> facetHierIds = facetHierarchyIds.keys();
                //while(facetHierIds.hasMoreElements()){
                SortItem facetNameSortItem = facetsToExportInSortItemFormat.get(p);
                Vector<String> values = facetHierarchyIds.get(facetNameSortItem.getLogName());

                logFileWriter.append("\r\n\t<!-- Facet -->\r\n");
                logFileWriter.append("\t<skos:Collection>\r\n");
                logFileWriter.append("\t\t<skos:prefLabel xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(facetNameSortItem.getLogName()));
                logFileWriter.append("</skos:prefLabel>\r\n");

                for (int i = 0; i < values.size(); i++) {
                    String hierIdValue = values.get(i);
                    if (hierIdValue != null && hierIdValue.length() > 0) {
                        logFileWriter.append("\t\t<skos:member rdf:resource=\"" + Utilities.escapeXML(hierIdValue) + "\"/> <!-- " + idToName.get(hierIdValue) + " -->\r\n");
                    }
                }

                logFileWriter.append("\t\t<skos:inScheme rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + importThesaurusName + " -->\r\n");
                logFileWriter.append("\t</skos:Collection>\r\n");
            }

        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            Vector<SortItem> facetsToExportInSortItemFormat = new Vector<SortItem>();
            Vector<String> facetsToExportInStringFormat = new Vector<String>();
            if (facetFilter.isEmpty()) {
                facetsToExportInSortItemFormat.addAll(xmlFacets);
                facetsToExportInStringFormat.addAll(Utilities.getStringVectorFromSortItemVector(xmlFacets));
                
                Enumeration<String> hierEnum = hierarchyFacets.keys();
                while (hierEnum.hasMoreElements()) {
                    String hierarchyName = hierEnum.nextElement();
                    Vector<String> facets = hierarchyFacets.get(hierarchyName);
                    if (facets != null) {
                        for (int k = 0; k < facets.size(); k++) {
                            String facetName = facets.get(k);
                            if (facetName != null && facetName.length() > 0 && facetsToExportInStringFormat.contains(facetName) == false) {
                                facetsToExportInSortItemFormat.add(new SortItem(facetName,-1,Utilities.getTransliterationString(facetName, false),-1));
                            }
                        }
                    }
                }
            } else {
                facetsToExportInStringFormat.addAll(facetFilter);
                facetsToExportInSortItemFormat.addAll(Utilities.getSortItemVectorFromStringVector(facetFilter, false));
            }

            Collections.sort(facetsToExportInSortItemFormat, new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION));

            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Facets");
            logFileWriter.append("\r\n\t<facets count=\"" + facetsToExportInSortItemFormat.size() + "\">\r\n");

            for (SortItem item : facetsToExportInSortItemFormat) {
                //logFileWriter.append("\t\t<facet index=\"" + (i + 1) + "\">\r\n");
                logFileWriter.append("\t\t<facet>\r\n");
                String appendValue = "\t\t\t<name";
                
                if(item.getThesaurusReferenceId()>0){
                    appendValue+= " "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+item.getThesaurusReferenceId()+"\"";
                    if(Parameters.ShowReferenceURIalso){
                        appendValue+= " " +ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.FACET, item.getThesaurusReferenceId())+"\"";                        
                    }
                }
                appendValue+=">";
                logFileWriter.append(appendValue);
                
                logFileWriter.append(Utilities.escapeXML(item.getLogName()));
                logFileWriter.append("</name>\r\n");
                
                if(item.getLogNameTransliteration()!=null && item.getLogNameTransliteration().length()>0){
                    logFileWriter.append("\t\t\t<"+ConstantParameters.system_transliteration_kwd+">");
                    logFileWriter.append(Utilities.escapeXML(item.getLogNameTransliteration()));
                    logFileWriter.append("</"+ConstantParameters.system_transliteration_kwd+">\r\n");
                }
                
                
                logFileWriter.append("\t\t</facet>\r\n");
            }

            logFileWriter.append("\t</facets>\r\n");

        }
        logFileWriter.flush();
    }

    public void WriteTHEMASTermToSkosConcept(OutputStreamWriter logFileWriter, String importThesaurusName, String targetTermName, boolean isTopConcept,
            Hashtable<String, NodeInfoStringContainer> termsInfo, Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
            Vector<String> TermsFilter) throws IOException {

        //should add sources
        /*
         * for (int i = 0; i < pack.getSourceEn().size(); i++) {
         temp += "  <dc:source xml:lang=\"" + "en" + '"' + " rdf:resource=" + '"' + pack.getSourceEn().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         for (int i = 0; i < pack.getSourceGr().size(); i++) {
         temp += "  <dc:source xml:lang=\"" + "el" + '"' + " rdf:resource=" + '"' + pack.getSourceGr().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         */
        Vector<String> termsFilter = new Vector<String>();

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        String schemePrefix = this.getSkosSchemePrefix(importThesaurusName);
        DBGeneral dbGen = new DBGeneral();

        NodeInfoStringContainer targetTermInfo = termsInfo.get(targetTermName);

        String targetTermId = "";
        Vector<String> tcs = targetTermInfo.descriptorInfo.get(ConstantParameters.tc_kwd);

        if (tcs != null && tcs.size() > 0) {
            targetTermId = ParseFileData.readSkosTC(tcs.get(0));
        }

        if (targetTermId.length() > 0) {
            targetTermId = schemePrefix + "/" + targetTermId;
        }

        Vector<String> nts = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
        Vector<SortItem> guidTermNts = new Vector<SortItem>();
        if (XMLguideTermsRelations.containsKey(targetTermName)) {

            Vector<SortItem> tempNts = XMLguideTermsRelations.get(targetTermName);
            if (tempNts != null && tempNts.size() > 0) {
                guidTermNts.addAll(tempNts);
            }
        }

        Vector<String> bts = targetTermInfo.descriptorInfo.get(ConstantParameters.bt_kwd);
        Vector<String> rts = targetTermInfo.descriptorInfo.get(ConstantParameters.rt_kwd);
        Vector<String> ufs = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_kwd);
        Vector<String> ufTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_translations_kwd);
        Vector<String> translations = targetTermInfo.descriptorInfo.get(ConstantParameters.translation_kwd);

        Vector<String> creators = targetTermInfo.descriptorInfo.get(ConstantParameters.created_by_kwd);
        Vector<String> creationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.created_on_kwd);
        if (creationDates != null) {
            Collections.sort(creationDates);
        }
        Vector<String> modificators = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_by_kwd);
        Vector<String> modificationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_on_kwd);
        if (modificationDates != null) {
            Collections.sort(modificationDates);
        }
        Vector<String> scopeNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
        Vector<String> scopeNoteTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);

        Vector<String> historicalNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);
        //skos:historyNote
        Vector<String> commentNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.comment_kwd);
        //skos:editorialNote

        if (targetTermId.length() > 0) {
            if (isTopConcept) {
                logFileWriter.append("\r\n\t<!-- TopConcept -->\r\n");
            } else {
                logFileWriter.append("\r\n\t<!-- Concept -->\r\n");
            }
            logFileWriter.append("\t<skos:Concept rdf:about=\"" + targetTermId + "\">\r\n");
            logFileWriter.append("\t\t<skos:prefLabel xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
            logFileWriter.append(Utilities.escapeXML(targetTermName));
            logFileWriter.append("</skos:prefLabel>\r\n");

            Collections.sort(translations);
            for (int j = 0; j < translations.size(); j++) {
                String translationValue = translations.get(j);
                String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                String langCode = parts[0].toLowerCase();
                String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                logFileWriter.append("\t\t<skos:prefLabel xml:lang=\"" + langCode + "\">");
                logFileWriter.append(Utilities.escapeXML(langWord));
                logFileWriter.append("</skos:prefLabel>\r\n");
            }

            Collections.sort(ufs);
            for (int j = 0; j < ufs.size(); j++) {
                String value = ufs.get(j);

                logFileWriter.append("\t\t<skos:altLabel xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(value));
                logFileWriter.append("</skos:altLabel>\r\n");
            }
            Collections.sort(ufTranslations);
            for (int j = 0; j < ufTranslations.size(); j++) {
                String translationValue = ufTranslations.get(j);
                String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                String langCode = parts[0].toLowerCase();
                String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                logFileWriter.append("\t\t<skos:altLabel xml:lang=\"" + langCode + "\">");
                logFileWriter.append(Utilities.escapeXML(langWord));
                logFileWriter.append("</skos:altLabel>\r\n");
            }

            //broader
            Collections.sort(bts);
            for (int j = 0; j < bts.size(); j++) {
                String termName = bts.get(j);
                Vector<String> termtcs = new Vector<String>();
                if (ConstantParameters.filterBts_Nts_Rts) {
                    if (termsInfo.containsKey(termName) == false) {
                        continue;
                    }
                }
                if (termsFilter.size() > 0 && termsFilter.contains(termName) == false) {
                    continue;
                }

                if (termsInfo.containsKey(termName)) {
                    termtcs.addAll(termsInfo.get(termName).descriptorInfo.get(ConstantParameters.tc_kwd));
                }

                String termId = "";
                if (termtcs != null && termtcs.size() > 0) {
                    termId = ParseFileData.readSkosTC(termtcs.get(0));
                }
                if (termId.length() > 0) {
                    termId = schemePrefix + "/" + termId;
                }

                logFileWriter.append("\t\t<skos:broader rdf:resource=\"" + termId + "\"/> <!-- " + termName + " -->\n");

            }

            //Narrower
            Vector<SortItem> finalGuideTerms = new Vector<SortItem>();

            if (nts != null && nts.size() > 0) {
                for (int j = 0; j < nts.size(); j++) {
                    String ntVal = nts.get(j);

                    if (ntVal == null || ntVal.length() == 0) {
                        continue;
                    }

                    boolean ntValFound = false;
                    SortItem finalSortItem = new SortItem("", -1, "");
                    for (int w = 0; w < guidTermNts.size(); w++) {
                        SortItem ntSortItem = guidTermNts.get(w);
                        if (ntSortItem.log_name.equals(ntVal)) {
                            ntValFound = true;
                            finalSortItem.log_name = ntSortItem.log_name;
                            finalSortItem.linkClass = ntSortItem.linkClass;
                            break;
                        }
                    }

                    if (ntValFound == false) {
                        finalSortItem.log_name = ntVal;
                    }
                    finalGuideTerms.add(finalSortItem);
                }

            }

            Vector<String> distinctGuideTerms = new Vector<String>();
            for (int j = 0; j < finalGuideTerms.size(); j++) {
                SortItem sitem = finalGuideTerms.get(j);
                String guildeTerm = sitem.linkClass;
                if (distinctGuideTerms.contains(guildeTerm) == false) {
                    distinctGuideTerms.add(guildeTerm);
                }
            }

            Collections.sort(distinctGuideTerms);
            for (int j = 0; j < distinctGuideTerms.size(); j++) {
                String targetGuideTerm = distinctGuideTerms.get(j);
                Vector<String> ntsWithThisGuideTerm = new Vector<String>();

                for (int k = 0; k < finalGuideTerms.size(); k++) {
                    SortItem sitem = finalGuideTerms.get(k);
                    String guildeTerm = sitem.linkClass;
                    String targetNt = sitem.log_name;

                    if (targetGuideTerm.equals(guildeTerm)) {
                        ntsWithThisGuideTerm.add(targetNt);
                    }
                }

                Collections.sort(ntsWithThisGuideTerm);

                if (targetGuideTerm.length() == 0) {
                    for (int k = 0; k < ntsWithThisGuideTerm.size(); k++) {
                        String ntStr = ntsWithThisGuideTerm.get(k);
                        if (ConstantParameters.filterBts_Nts_Rts) {
                            if (termsInfo.containsKey(ntStr) == false) {
                                continue;
                            }
                        }
                        if (termsFilter.size() > 0 && termsFilter.contains(ntStr) == false) {
                            continue;
                        }

                        Vector<String> nttcs = new Vector<String>();
                        if (termsInfo.containsKey(ntStr)) {
                            nttcs.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.tc_kwd));
                        }

                        String ntId = "";
                        if (nttcs != null && nttcs.size() > 0) {
                            ntId = ParseFileData.readSkosTC(nttcs.get(0));
                        }
                        if (ntId.length() > 0) {
                            ntId = schemePrefix + "/" + ntId;
                        }

                        logFileWriter.append("\t\t<skos:narrower rdf:resource=\"" + ntId + "\"/> <!-- " + ntStr + " -->\n");
                    }

                } else {

                    Collections.sort(ntsWithThisGuideTerm);
                    if (ntsWithThisGuideTerm != null && ntsWithThisGuideTerm.size() > 0) {
                        logFileWriter.append("\t\t<skos:narrower>\r\n");
                        logFileWriter.append("\t\t\t<skos:Collection>\r\n");
                        logFileWriter.append("\t\t\t\t<skos:prefLabel>" + targetGuideTerm + "</skos:prefLabel>\r\n");

                        for (int k = 0; k < ntsWithThisGuideTerm.size(); k++) {
                            String ntStr = ntsWithThisGuideTerm.get(k);
                            if (ConstantParameters.filterBts_Nts_Rts) {
                                if (termsInfo.containsKey(ntStr) == false) {
                                    continue;
                                }
                            }
                            if (termsFilter.size() > 0 && termsFilter.contains(ntStr) == false) {
                                continue;
                            }

                            Vector<String> nttcs = new Vector<String>();
                            if (termsInfo.containsKey(ntStr)) {
                                nttcs.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.tc_kwd));
                            }

                            String ntId = "";
                            if (nttcs != null && nttcs.size() > 0) {
                                ntId = ParseFileData.readSkosTC(nttcs.get(0));
                            }
                            if (ntId.length() > 0) {
                                ntId = schemePrefix + "/" + ntId;
                            }

                            logFileWriter.append("\t\t\t\t<skos:member rdf:resource=\"" + ntId + "\"/> <!-- " + ntStr + " -->\n");

                        }

                        logFileWriter.append("\t\t\t</skos:Collection>\r\n");
                        logFileWriter.append("\t\t</skos:narrower>\r\n");
                    }

                }
            }

            Collections.sort(rts);
            for (int j = 0; j < rts.size(); j++) {
                String termName = rts.get(j);

                if (ConstantParameters.filterBts_Nts_Rts) {
                    if (termsInfo.containsKey(termName) == false) {
                        continue;
                    }
                }
                if (termsFilter.size() > 0 && termsFilter.contains(termName) == false) {
                    continue;
                }
                Vector<String> termtcs = new Vector<String>();

                if (termsInfo.containsKey(termName)) {
                    termtcs.addAll(termsInfo.get(termName).descriptorInfo.get(ConstantParameters.tc_kwd));
                }

                String termId = "";
                if (termtcs != null && termtcs.size() > 0) {
                    termId = ParseFileData.readSkosTC(termtcs.get(0));
                }
                if (termId.length() > 0) {
                    termId = schemePrefix + "/" + termId;
                }
                logFileWriter.append("\t\t<skos:related rdf:resource=\"" + termId + "\"/> <!-- " + termName + " -->\n");

            }

            if (scopeNotes != null && scopeNotes.size() > 0) {
                String scopeNoteVal = scopeNotes.get(0);
                if (scopeNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<skos:scopeNote xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(scopeNoteVal));
                    logFileWriter.append("</skos:scopeNote>\r\n");
                }
            }

            if (scopeNoteTranslations != null) {
                Collections.sort(scopeNoteTranslations);
                for (int j = 0; j < scopeNoteTranslations.size(); j++) {
                    String noteStr = scopeNoteTranslations.get(j);
                    noteStr = noteStr.replaceAll("\t", " ");
                    noteStr = noteStr.replaceAll("\r\n", " ");
                    noteStr = noteStr.replaceAll("\r", " ");
                    noteStr = noteStr.replaceAll("\n", " ");
                    noteStr = noteStr.replaceAll(" +", " ");

                    if (noteStr.indexOf(Parameters.TRANSLATION_SEPERATOR) == 2) {
                        String langCode = noteStr.substring(0, noteStr.indexOf(Parameters.TRANSLATION_SEPERATOR)).toUpperCase();
                        noteStr = noteStr.substring(3);
                        if (noteStr != null && noteStr.trim().length() > 0) {
                            noteStr = noteStr.trim();
                            logFileWriter.append("\t\t<skos:scopeNote xml:lang=\"" + langCode.toLowerCase() + "\">");
                            logFileWriter.append(Utilities.escapeXML(noteStr));
                            logFileWriter.append("</skos:scopeNote>\r\n");
                        }
                    }

                }
            }

            /*
             if (scopeNoteTranslations != null && scopeNoteTranslations.size() > 0) {
             String scopeNoteVal = scopeNoteTranslations.get(0);
             scopeNoteVal = scopeNoteVal.replaceAll("\t", " ");
             scopeNoteVal = scopeNoteVal.replaceAll(" +", " ");
             scopeNoteVal = scopeNoteVal.replaceAll("\r\n", "\n");
             scopeNoteVal = scopeNoteVal.replaceAll(" \n", "\n");

             String[] parts = scopeNoteVal.split("\n");

             if (parts != null) {
             String langCode = "";
             String value = "";
             for (int p = 0; p < parts.length; p++) {
             String partStr = parts[p];
             if (partStr.matches("[A-Z\\-]{2,6}" + Parameters.TRANSLATION_SEPERATOR)) {

             //check if langcode and value are not empty.
             if (langCode != null && langCode.length() > 0 && value != null && value.length() > 0) {
             logFileWriter.append("\t\t<skos:scopeNote xml:lang=\"" + langCode.toLowerCase() + "\">");
             logFileWriter.append(Utilities.escapeXML(value));
             logFileWriter.append("</skos:scopeNote>\r\n");
             }
             langCode = partStr.replaceFirst(Parameters.TRANSLATION_SEPERATOR, "");
             value = "";
             } else {
             if (value != null && value.length() > 0) {
             value += "\n" + partStr;
             } else {
             value = partStr;
             }
             }
             }

             //check if langcode and value are not empty.
             if (langCode != null && langCode.length() > 0 && value != null && value.length() > 0) {
             logFileWriter.append("\t\t<skos:scopeNote xml:lang=\"" + langCode.toLowerCase() + "\">");
             logFileWriter.append(Utilities.escapeXML(value));
             logFileWriter.append("</skos:scopeNote>\r\n");
             }
             }
             }*/
            if (historicalNotes != null && historicalNotes.size() > 0) {
                String historicalNoteVal = historicalNotes.get(0);
                if (historicalNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<skos:historyNote xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(historicalNoteVal));
                    logFileWriter.append("</skos:historyNote>\r\n");
                }
            }

            if (commentNotes != null && commentNotes.size() > 0) {
                String commentNoteVal = commentNotes.get(0);
                if (commentNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<skos:editorialNote xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(commentNoteVal));
                    logFileWriter.append("</skos:editorialNote>\r\n");
                }
            }

            Collections.sort(creators);
            for (int j = 0; j < creators.size(); j++) {
                String value = creators.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dc:creator>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dc:creator>\r\n");
                }
            }
            Collections.sort(creationDates);
            for (int j = 0; j < creationDates.size(); j++) {
                String value = creationDates.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dc:date>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dc:date>\r\n");
                }
            }

            Collections.sort(modificators);
            for (int j = 0; j < modificators.size(); j++) {
                String value = modificators.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dc:contributor>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dc:contributor>\r\n");
                }
            }
            Collections.sort(modificationDates);
            for (int j = 0; j < modificationDates.size(); j++) {
                String value = modificationDates.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dc:date>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dc:date>\r\n");
                }
            }

            if (isTopConcept) {
                logFileWriter.append("\t\t<skos:topConceptOf rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + importThesaurusName + " -->\r\n");
            } else {
                logFileWriter.append("\t\t<skos:inScheme rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + importThesaurusName + " -->\r\n");
            }
            logFileWriter.append("\t</skos:Concept>\r\n");
        }
    }

    public void WriteHierarchies(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            Hashtable<String, Vector<String>> hierarchyFacets, Hashtable<String, NodeInfoStringContainer> termsInfo,
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
            Vector<String> FacetsFilter,
            Vector<String> TermsFilter) throws IOException {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Hierarchies");

        Vector<String> facetFilter = new Vector<String>();
        Vector<String> termsFilter = new Vector<String>();

        if (FacetsFilter != null && FacetsFilter.size() > 0) {
            facetFilter.addAll(FacetsFilter);
        }

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            Vector<String> allHierarchies = new Vector<String>();
            if (termsFilter.size() == 0) {
                allHierarchies.addAll(hierarchyFacets.keySet());
            } else {
                Enumeration<String> hierEnum = hierarchyFacets.keys();
                while (hierEnum.hasMoreElements()) {
                    String hierName = hierEnum.nextElement();
                    if (termsFilter.contains(hierName) && allHierarchies.contains(hierName) == false) {
                        allHierarchies.add(hierName);
                    }
                }
            }
            Collections.sort(allHierarchies);

            if (allHierarchies.size() > 0) {
                for (int i = 0; i < allHierarchies.size(); i++) {
                    String hierarchyName = allHierarchies.get(i);
                    WriteTHEMASTermToSkosConcept(logFileWriter, importThesaurusName, hierarchyName, true, termsInfo, XMLguideTermsRelations, termsFilter);
                }
            }
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            Vector<String> allHierarchies = new Vector<String>();
            if (termsFilter.size() == 0) {
                allHierarchies.addAll(hierarchyFacets.keySet());
            } else {
                Enumeration<String> hierEnum = hierarchyFacets.keys();
                while (hierEnum.hasMoreElements()) {
                    String hierName = hierEnum.nextElement();
                    if (termsFilter.contains(hierName) && allHierarchies.contains(hierName) == false) {
                        allHierarchies.add(hierName);
                    }
                }
            }

            Collections.sort(allHierarchies);

            if (allHierarchies.size() > 0) {

                //logFileWriter.append("\r\n\t<hierarchies>\r\n");
                logFileWriter.append("\r\n\t<hierarchies count=\"" + allHierarchies.size() + "\">\r\n");

                for (int i = 0; i < allHierarchies.size(); i++) {
                    String hierarchyName = allHierarchies.get(i);
                    Vector<String> facets = hierarchyFacets.get(hierarchyName);
                    if (facets != null) {
                        Collections.sort(facets);
                    }

                    //logFileWriter.append("\t\t<hierarchy index=\"" + (i+1) + "\">\r\n");
                    logFileWriter.append("\t\t<hierarchy>\r\n");
                    logFileWriter.append("\t\t\t<name>");
                    logFileWriter.append(Utilities.escapeXML(hierarchyName));
                    logFileWriter.append("</name>\r\n");
                    for (int k = 0; k < facets.size(); k++) {
                        String facetName = facets.get(k);
                        if (facetFilter.size() > 0 && facetFilter.contains(facetName) == false) {
                            continue;
                        }
                        if (facetName != null && facetName.length() > 0) {
                            logFileWriter.append("\t\t\t<facet>");
                            logFileWriter.append(Utilities.escapeXML(facetName));
                            logFileWriter.append("</facet>\r\n");
                        }

                    }
                    logFileWriter.append("\t\t</hierarchy>\r\n");

                }

                logFileWriter.append("\t</hierarchies>\r\n");

            }

        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Finished Exporting Hierarchies");
        logFileWriter.flush();
    }

    public void WriteSources(OutputStreamWriter logFileWriter, String exportScheme,
            Hashtable<String, String> XMLsources) throws IOException {
        if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            Utilities u = new Utilities();
            if (XMLsources.size() > 0) {
                logFileWriter.append("\r\n\t<sources count=\"" + XMLsources.size() + "\">");
                Vector<String> sourceNames = new Vector<String>(XMLsources.keySet());
                Collections.sort(sourceNames);

                for (int k = 0; k < sourceNames.size(); k++) {
                    String targetSource = sourceNames.get(k);
                    String targetSourceNote = XMLsources.get(targetSource);
                    //logFileWriter.append("\r\n\t\t<source index=\""+(k+1)+"\">");
                    logFileWriter.append("\r\n\t\t<source>");
                    logFileWriter.append("\r\n\t\t\t<name>" + u.escapeXML(targetSource) + "</name>");
                    if (targetSourceNote != null && targetSourceNote.trim().length() > 0) {
                        targetSourceNote = targetSourceNote.replaceAll("\r\n", " ");
                        targetSourceNote = targetSourceNote.replaceAll("\r", " ");
                        targetSourceNote = targetSourceNote.replaceAll("\n", " ");
                        logFileWriter.append("\r\n\t\t\t<" + ConstantParameters.source_note_kwd + ">" + u.escapeXML(targetSourceNote) + "</" + ConstantParameters.source_note_kwd + ">");
                    }
                    logFileWriter.append("\r\n\t\t</source>");
                }
                logFileWriter.append("\r\n\t</sources>\r\n");
            }
        }

    }

    public void WriteTerms(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            Hashtable<String, Vector<String>> hierarchyFacets, Hashtable<String, NodeInfoStringContainer> termsInfo,
            Hashtable<String, Vector<SortItem>> XMLguideTermsRelations,
            Vector<String> TermsFilter) throws IOException {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Terms");
        
        Vector<String> termsFilter = new Vector<String>();

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            Vector<String> allHierarchies = new Vector<String>();
            allHierarchies.addAll(hierarchyFacets.keySet());

            Vector<SortItem> allTerms = new Vector<SortItem>();
            allTerms.addAll(Utilities.getSortItemVectorFromTermsInfo(termsInfo, false));

            allTerms.removeAll(allHierarchies);

            Collections.sort(allTerms, new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION)));

            if (allTerms.size() > 0) {
                for (SortItem termItem : allTerms) {
                    String termName = termItem.getLogName();

                    if (termsFilter.size() > 0 && termsFilter.contains(termName) == false) {
                        continue;
                    }
                    WriteTHEMASTermToSkosConcept(logFileWriter, importThesaurusName, termName, false, termsInfo, XMLguideTermsRelations, termsFilter);
                }

            }
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            DBGeneral dbGen = new DBGeneral();
            Utilities u = new Utilities();

            String[] output = {ConstantParameters.system_transliteration_kwd,ConstantParameters.system_referenceUri_kwd,ConstantParameters.facet_kwd, ConstantParameters.topterm_kwd, ConstantParameters.status_kwd, ConstantParameters.bt_kwd, ConstantParameters.nt_kwd,
                ConstantParameters.tc_kwd, ConstantParameters.translation_kwd, ConstantParameters.rt_kwd, ConstantParameters.uf_kwd, ConstantParameters.uf_translations_kwd,
                ConstantParameters.primary_found_in_kwd, ConstantParameters.translations_found_in_kwd, ConstantParameters.created_by_kwd, ConstantParameters.created_on_kwd, ConstantParameters.modified_by_kwd,
                ConstantParameters.modified_on_kwd, ConstantParameters.scope_note_kwd, ConstantParameters.translations_scope_note_kwd, ConstantParameters.historical_note_kwd};

            Vector<SortItem> allTerms = new Vector<SortItem>();
            

            
            if (termsFilter.size() == 0) {                
                allTerms.addAll(Utilities.getSortItemVectorFromTermsInfo(termsInfo, false));
            } else {
                Enumeration<String> termsEnum = termsInfo.keys();
                while (termsEnum.hasMoreElements()) {
                    String termName = termsEnum.nextElement();
                    if (termsFilter.contains(termName)) {
                        long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(termsInfo.get(termName));
                        String transliteration = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(termsInfo.get(termName),termName,false);
                        long id = -1; //Utilities.retrieveDatabaseIdFromNodeInfoStringContainer(termsInfo.get(termName));
                        allTerms.add(new SortItem(termName,id,transliteration,refId));
                    }
                }
            }

            SortItemComparator transliterationComparator =  new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            SortItemComparator linkClassTransliterationComparator =  new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            Collections.sort(allTerms,transliterationComparator);

            Vector<String> specialCategories = new Vector<String>();

            specialCategories.add(ConstantParameters.rt_kwd);
            specialCategories.add(ConstantParameters.bt_kwd);
            specialCategories.add(ConstantParameters.nt_kwd);
            specialCategories.add(ConstantParameters.translation_kwd);
            specialCategories.add(ConstantParameters.uf_translations_kwd);
            specialCategories.add(ConstantParameters.status_kwd);
            specialCategories.add(ConstantParameters.scope_note_kwd);
            specialCategories.add(ConstantParameters.translations_scope_note_kwd);
            specialCategories.add(ConstantParameters.historical_note_kwd);

            if (allTerms.size() > 0) {
                //logFileWriter.append("\r\n\t<terms>\r\n");
                logFileWriter.append("\r\n\t<terms count=\"" + allTerms.size() + "\">\r\n");
                for (SortItem termItem : allTerms) {
                    String termName = termItem.getLogName();

                    NodeInfoStringContainer targetTermInfo = termsInfo.get(termName);

                    //logFileWriter.append("\t\t<term index=\"" + (i + 1) + "\">\r\n");
                    logFileWriter.append("\t\t<term>\r\n");
                    logFileWriter.append("\t\t\t<descriptor");
                    
                    Long number = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(targetTermInfo);
                     if(number>0){
                        logFileWriter.append(" " +ConstantParameters.system_referenceIdAttribute_kwd+"=\""+number+"\"");
                        if(Parameters.ShowReferenceURIalso){
                            logFileWriter.append(" " +ConstantParameters.system_referenceUri_kwd+"=\""+u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, number)+"\"");
                        }
                    }
                            
                    logFileWriter.append(">"+Utilities.escapeXML(termName) + "</descriptor>\r\n");

                    for (int m = 0; m < output.length; m++) {
                        String category = output[m];
                        Vector<String> values = new Vector<String>();
                        if (targetTermInfo.descriptorInfo.containsKey(category)) {
                            values.addAll(targetTermInfo.descriptorInfo.get(category));
                        }

                        if(category.equals(ConstantParameters.system_referenceUri_kwd)){
                            //moved in descriptor XML element
                            /*Long number = Long.parseLong(values.get(0));
                            if(number>0){
                                logFileWriter.append("\t\t\t<" + category + " "+ConstantParameters.system_referenceIdAttribute_kwd+"=\""+number+"\">");
                                logFileWriter.append(Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, number)));
                                logFileWriter.append("</" + category + ">\r\n");
                            }*/
                            continue;
                        }
                        
                        if (specialCategories.contains(category) == false) {
                            Collections.sort(values);

                            for (int k = 0; k < values.size(); k++) {
                                logFileWriter.append("\t\t\t<" + category + ">");
                                if (category.equals(ConstantParameters.primary_found_in_kwd) || category.equals(ConstantParameters.translations_found_in_kwd)) {
                                    logFileWriter.append(Utilities.escapeXML(values.get(k)));
                                } else {
                                    logFileWriter.append(Utilities.escapeXML(values.get(k)));
                                }
                                logFileWriter.append("</" + category + ">\r\n");
                            }
                        } else {
                            if (category.equals(ConstantParameters.bt_kwd) || category.equals(ConstantParameters.rt_kwd)) {
                                //Collections.sort(values);
                                Vector<SortItem> valueSortItems = Utilities.getSortItemVectorFromStringVectorAndTermsInfo(values, termsInfo, false);
                                Collections.sort(valueSortItems, transliterationComparator);
                                for (SortItem linkItem : valueSortItems) {
                                    String val = linkItem.getLogName();
                                    if (ConstantParameters.filterBts_Nts_Rts) {
                                        if (termsInfo.containsKey(val) == false) {
                                            continue;
                                        }
                                    }
                                    if (termsFilter.size() > 0 && termsFilter.contains(val) == false) {
                                        continue;
                                    }
                                    String appendVal = "\t\t\t<" + category;
                                    if(linkItem.getThesaurusReferenceId()>0){
                                        appendVal+= " "+ConstantParameters.system_referenceIdAttribute_kwd +"=\""+linkItem.getThesaurusReferenceId()+"\"";
                                        if(Parameters.ShowReferenceURIalso){
                                            appendVal+= " "+ConstantParameters.system_referenceUri_kwd +"=\""+Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, linkItem.getThesaurusReferenceId()))+"\"";
                                        }
                                    }
                                    appendVal+=">";
                                    logFileWriter.append(appendVal);                                    
                                    logFileWriter.append(Utilities.escapeXML(val));
                                    logFileWriter.append("</" + category + ">\r\n");
                                }
                            } else if (category.equals(ConstantParameters.translations_scope_note_kwd)) {

                                for (int k = 0; k < values.size(); k++) {
                                    String noteStr = values.get(k);
                                    if (noteStr.indexOf(Parameters.TRANSLATION_SEPERATOR) == 2) {
                                        String langCode = noteStr.substring(0, noteStr.indexOf(Parameters.TRANSLATION_SEPERATOR)).toUpperCase();
                                        noteStr = noteStr.substring(3);
                                        if (noteStr != null && noteStr.trim().length() > 0) {
                                            noteStr = noteStr.trim();
                                            logFileWriter.append("\t\t\t<" + category + " " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + langCode.toUpperCase() + "\">");
                                            logFileWriter.append(Utilities.escapeXML(noteStr));
                                            logFileWriter.append("</" + category + ">\r\n");
                                        }
                                    }

                                }

                            } else if (category.equals(ConstantParameters.scope_note_kwd)
                                    || category.equals(ConstantParameters.historical_note_kwd)) {
                                if (values != null && values.size() > 0) {
                                    String noteStr = values.get(0);

                                    if (noteStr != null && noteStr.length() > 0) {
                                        logFileWriter.append("\t\t\t<" + category + ">");
                                        logFileWriter.append(Utilities.escapeXML(noteStr));
                                        logFileWriter.append("</" + category + ">\r\n");
                                    }
                                }

                            } else if (category.equals(ConstantParameters.status_kwd)) {
                                String statusVal = "";
                                if (values != null && values.size() > 0) {
                                    statusVal = values.get(0);
                                } else {
                                    if (Parameters.PrimaryLang.toLowerCase().equals("en")) {
                                        statusVal = "Under construction";
                                    } else {
                                        //greek translation of Under construction in hex form
                                        statusVal = "\u03A5\u03C0\u03CC \u03B5\u03C0\u03B5\u03BE\u03B5\u03C1\u03B3\u03B1\u03C3\u03af\u03B1";
                                    }
                                }

                                logFileWriter.append("\t\t\t<" + category + ">");
                                logFileWriter.append(Utilities.escapeXML(statusVal));
                                logFileWriter.append("</" + category + ">\r\n");
                            } else if (category.equals(ConstantParameters.translation_kwd)
                                    || category.equals(ConstantParameters.uf_translations_kwd)) {
                                Collections.sort(values);

                                for (int k = 0; k < values.size(); k++) {
                                    String translationVal = values.get(k);
                                    String langCode = "";
                                    String val = "";

                                    if (translationVal.contains(Parameters.TRANSLATION_SEPERATOR)) {
                                        langCode = Linguist.SupportedTHEMASLangcodes(translationVal.substring(0, translationVal.indexOf(Parameters.TRANSLATION_SEPERATOR, 0)));
                                        val = translationVal.replaceFirst(langCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR, "");
                                    }
                                    if (langCode != null && langCode.length() > 0 && val != null && val.length() > 0) {

                                        logFileWriter.append("\t\t\t<" + category + " " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + langCode.toUpperCase() + "\">");
                                        logFileWriter.append(Utilities.escapeXML(val));
                                        logFileWriter.append("</" + category + ">\r\n");
                                    }
                                }
                            } else if (category.equals(ConstantParameters.nt_kwd)) {
                                Vector<String> ntVals = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
                                Vector<SortItem> ntSortItems = Utilities.getSortItemVectorFromStringVectorAndTermsInfo(ntVals, termsInfo, false);
                                Collections.sort(ntSortItems, transliterationComparator);
                                
                                Vector<SortItem> guidTermNts = new Vector<SortItem>();
                                if (XMLguideTermsRelations.containsKey(termName)) {

                                    Vector<SortItem> tempNts = XMLguideTermsRelations.get(termName);
                                    if (tempNts != null && tempNts.size() > 0) {
                                        guidTermNts.addAll(tempNts);
                                    }
                                }

                                //Narrower
                                Vector<SortItem> finalGuideTerms = new Vector<SortItem>();

                                if (ntSortItems != null && ntSortItems.size() > 0) {
                                    for (SortItem ntSIVal : ntSortItems) {
                                        
                                        if (ntSIVal == null || ntSIVal.getLogName()==null || ntSIVal.getLogName().length() == 0) {
                                            continue;
                                        }

                                        boolean ntValFound = false;
                                        SortItem finalSortItem = new SortItem("", -1, "");
                                        for (SortItem ntSortItem : guidTermNts) {
                                            if (ntSortItem.log_name.equals(ntSIVal.getLogName())) {
                                                ntValFound = true;
                                                finalSortItem.log_name = ntSortItem.log_name;
                                                finalSortItem.linkClass = ntSortItem.linkClass;
                                                finalSortItem.thesarurusReferenceId = ntSIVal.getThesaurusReferenceId();
                                                finalSortItem.log_name_transliteration = ntSIVal.getLogNameTransliteration();
                                                break;
                                            }
                                        }

                                        if (ntValFound == false) {
                                            finalSortItem.log_name = ntSIVal.getLogName();
                                            finalSortItem.thesarurusReferenceId = ntSIVal.getThesaurusReferenceId();
                                            finalSortItem.log_name_transliteration = ntSIVal.getLogNameTransliteration();
                                        }
                                        finalGuideTerms.add(finalSortItem);
                                    }
                                }

                                Vector<String> distinctGuideTermsStrings = new Vector<String>();
                                Vector<SortItem> distinctGuideTermsSortItems = new Vector<SortItem>();
                                for (SortItem sitem : finalGuideTerms) {
                                    String guildeTerm = sitem.linkClass;
                                    if (distinctGuideTermsStrings.contains(guildeTerm) == false) {
                                        distinctGuideTermsSortItems.add(new SortItem(guildeTerm, -1, Utilities.getTransliterationString(sitem.linkClass,false),sitem.getThesaurusReferenceId()));
                                        distinctGuideTermsStrings.add(guildeTerm);
                                    }
                                }

                                Collections.sort(distinctGuideTermsSortItems, transliterationComparator);

                                for (SortItem targetGuideTermItem: distinctGuideTermsSortItems) {
                                     String targetGuideTerm = targetGuideTermItem.getLogName();
                                     Vector<SortItem> ntsWithThisGuideTerm = new Vector<SortItem>();
                                     
                                     for (SortItem sitem : finalGuideTerms) {
                                         String guildeTerm = sitem.linkClass;
                                         String targetNt = sitem.log_name;
                                         
                                         if (targetGuideTerm.equals(guildeTerm)) {
                                             ntsWithThisGuideTerm.add(new SortItem(targetNt,-1,guildeTerm,sitem.log_name_transliteration,sitem.getThesaurusReferenceId()));
                                         }
                                     }

                                    Collections.sort(ntsWithThisGuideTerm,linkClassTransliterationComparator);

                                    if (targetGuideTerm.length() == 0) {
                                        for (SortItem ntStr : ntsWithThisGuideTerm) {

                                            if (ConstantParameters.filterBts_Nts_Rts) {
                                                if (termsInfo.containsKey(ntStr.getLogName()) == false) {
                                                    continue;
                                                }
                                            }
                                            if (termsFilter.size() > 0 && termsFilter.contains(ntStr.getLogName()) == false) {
                                                continue;
                                            }
                                            //logFileWriter.append("\t\t\t<" + category + " " + ConstantParameters.XMLLinkClassAttributeName + "=\"\">");
                                            
                                            String appendVal = "\t\t\t<" + category;
                                            if(ntStr.getThesaurusReferenceId()>0){
                                                appendVal+= " "+ConstantParameters.system_referenceIdAttribute_kwd +"=\""+ntStr.getThesaurusReferenceId()+"\"";
                                                if(Parameters.ShowReferenceURIalso){
                                                    appendVal+= " "+ConstantParameters.system_referenceUri_kwd +"=\""+Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, ntStr.getThesaurusReferenceId()))+"\"";
                                                }
                                            }
                                            appendVal+=">";
                                            logFileWriter.append(appendVal); 
                                            logFileWriter.append(Utilities.escapeXML(ntStr.getLogName()));
                                            logFileWriter.append("</" + category + ">\r\n");
                                        }

                                    } else {

                                            for (SortItem ntStr : ntsWithThisGuideTerm) {

                                                if (ConstantParameters.filterBts_Nts_Rts) {
                                                    if (termsInfo.containsKey(ntStr.getLogName()) == false) {
                                                        continue;
                                                    }
                                                }
                                                if (termsFilter.size() > 0 && termsFilter.contains(ntStr.getLogName()) == false) {
                                                    continue;
                                                }
                                                String appendVal = "\t\t\t<" + category;
                                                if(ntStr.getThesaurusReferenceId()>0){
                                                    appendVal+= " "+ConstantParameters.system_referenceIdAttribute_kwd +"=\""+ntStr.getThesaurusReferenceId()+"\"";
                                                    if(Parameters.ShowReferenceURIalso){
                                                        appendVal+= " "+ConstantParameters.system_referenceUri_kwd +"=\""+Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, ntStr.getThesaurusReferenceId()))+"\"";
                                                    }
                                                }
                                                appendVal+=" " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + targetGuideTerm + "\">";
                                                logFileWriter.append(appendVal); 
                                                logFileWriter.append(Utilities.escapeXML(ntStr.getLogName()));
                                                logFileWriter.append("</" + category + ">\r\n");

                                            }

                                    }
                                }
                            }

                        }
                    }

                    logFileWriter.append("\t\t</term>\r\n");

                }

                logFileWriter.append("\t</terms>\r\n");
            }

        }
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Finished Exporting Terms");
        logFileWriter.flush();
    }

    public void WriteFileEnd(OutputStreamWriter logFileWriter, String exportScheme) throws IOException {

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            logFileWriter.append("\r\n</rdf:RDF>");
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            logFileWriter.append("\r\n</data>");
        }

        logFileWriter.flush();
        logFileWriter.close();
    }

    public static void formatXMLFile(String logFileNamePath) {
        //format xml
        try {

            DocumentBuilderFactory dbFactory;
            DocumentBuilder dBuilder;
            Document original = null;
            try {
                dbFactory = DocumentBuilderFactory.newInstance();
                dBuilder = dbFactory.newDocumentBuilder();
                original = dBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(logFileNamePath), "UTF-8")));
            } catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            DOMSource inSource = new DOMSource(original);

            FileOutputStream fout = new FileOutputStream(logFileNamePath);
            OutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter logFileWriter = new OutputStreamWriter(bout, "UTF-8");

            StreamResult xmlOutput = new StreamResult(logFileWriter);

            TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", 4);

            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(inSource, xmlOutput);

            logFileWriter.flush();

        } catch (Exception e) {
            Utils.StaticClass.handleException(e);
            throw new RuntimeException(e);
        } // simple exception handling, please review it     }
    }
}
