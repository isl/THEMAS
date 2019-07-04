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
import Utils.ConstantParameters;
import Utils.ExternalLink;
import Utils.ExternalVocabulary;
import Utils.Linguist;
import Utils.NodeInfoStringContainer;
import Utils.Parameters;
import Utils.SortItem;
import Utils.SortItemComparator;
import Utils.Utilities;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author tzortzak
 */
public class WriteFileData {
/*
    private String getSkosSchemePrefix(String importThesaurusName) {

        if (ConstantParameters.includeThesaurusNameInScheme) {
            return ConstantParameters.SchemePrefix + "/" + importThesaurusName.replaceAll(" ", "_");
        }
        return ConstantParameters.SchemePrefix;
    }
*/
    public void WriteFileStart(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName, final String uiLang) throws IOException {

        Utilities u = new Utilities();

        //locale/footer/tooltipappnameandversion
        logFileWriter.append(ConstantParameters.xmlHeader);//+ "\r\n"

        logFileWriter.append("<!-- " + u.translateFromTranslationsXML("locale/footer/tooltipappnameandversion", null,uiLang) +" "+
                u.translateFromTranslationsXML("locale/version", null, uiLang) +" -->\r\n");
        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {
/* could use xml:base=""
and then all rdfAbouts could just include the concept name
e.g.

xml:base="http://www.ics.forth.gr/isl/CRM/">


<rdfs:Class rdf:about="E21_Person">

*/          
            String xmlBase = ConstantParameters.SchemePrefix +"/";
            String schemePrefix = (Parameters.SkosExportUsingXmlBase? "": xmlBase) ;

            
            logFileWriter.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n"
                    + "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n"
                    + "\txmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n"
                    + "\txmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n"                    
                    + "\txmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n"
                    + "\txmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n"
                    + "\txmlns:iso-thes=\"http://purl.org/iso25964/skos-thes#\""+(Parameters.SkosExportUsingXmlBase ? "\r\n\txml:base=\""+xmlBase+"\"":"")
                    //+ "\r\n\txml:base=\""+ConstantParameters.SchemePrefix+"/\""
                    +">\r\n\r\n");
            
            logFileWriter.append("\t<rdf:Description rdf:about=\""+ schemePrefix + Skos_Facet+"\">\r\n"+
		"\t\t<rdfs:subClassOf rdf:resource=\"http://www.w3.org/2004/02/skos/core#Collection\"/>\r\n"+
                "\t\t<"+ConstantParameters.XML_skos_scopeNote+" xml:lang=\"en\"> grouping of concepts of the same inherent category</"+ConstantParameters.XML_skos_scopeNote+">\r\n"+
		"\t</rdf:Description>\r\n\r\n");
            
            logFileWriter.append("\t<rdf:Description rdf:about=\"" + ConstantParameters.referenceThesaurusSchemeName + "\">\r\n"
                    + "\t\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#ConceptScheme\"/>\r\n"
                    + "\t\t<"+ConstantParameters.XML_skos_prefLabel+">" + Utilities.escapeXML(importThesaurusName) + "</"+ConstantParameters.XML_skos_prefLabel+">\r\n"
                    + "\t\t<dcterms:date>" + Utilities.GetNow() + "</dcterms:date>\r\n"
                    + "\t</rdf:Description>\r\n");

            /*
            	<rdf:Description rdf:about="https://vocabs.dariah.eu/bbt/Facet/">
		<skos:scopeNote xml:lang="en"> grouping of concepts of the same inherent category</skos:scopeNote>
		<rdfs:subClassOf rdf:resource="http://www.w3.org/2004/02/skos/core#Collection"/>
	</rdf:Description>
            */
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            //logFileWriter.append("<data thesaurus=\"" + Utilities.escapeXML(importThesaurusName) + "\" exportDate=\"" + Utilities.GetNow() + "\" \r\n\t"            
            //+ "xmlns=\"http://localhost/THEMAS\"\r\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
            logFileWriter.append("<data thesaurus=\"" + Utilities.escapeXML(importThesaurusName) + "\""+
                                      " exportDate=\"" + Utilities.GetNow() + "\""+
                                      " schemaversion=\"" + u.translateFromTranslationsXML("locale/version", null,uiLang) + "\">\r\n");
        }

        logFileWriter.flush();
    }

    public void WriteTranslationCategories(OutputStreamWriter logFileWriter, String exportScheme, HashMap<String, String> translationPairs) throws IOException {

        if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting translation categories");
            logFileWriter.write("\r\n\t<TranslationCategories translationSeperator=\"" + Parameters.TRANSLATION_SEPERATOR + "\">\r\n");

            ArrayList<String> sortedTrCategs = new ArrayList<String>(translationPairs.keySet());
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
            ArrayList<String> GuideTerms) throws IOException {

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            ArrayList<String> GuideTermsToExport = new ArrayList<>();

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

            ArrayList<String> GuideTermsToExport = new ArrayList<String>();
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
            ArrayList<String> xmlFacets, HashMap<String, ArrayList<String>> hierarchyFacets,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> FacetsFilter,
            ArrayList<String> HierarchiesFilter) throws IOException {

        ArrayList<SortItem> xmlFacetssortItems = Utilities.getSortItemVectorFromStringVector(xmlFacets, false);
        HashMap<SortItem, ArrayList<SortItem>> hierarchyFacetsSortItems = new HashMap<>();
        Iterator<String> hierNames = hierarchyFacets.keySet().iterator();
        while(hierNames.hasNext()){
            String hier = hierNames.next();
            ArrayList<String> facets = hierarchyFacets.get(hier);
            hierarchyFacetsSortItems.put(new SortItem(hier,-1,Utilities.getTransliterationString(hier, false),-1), 
                new ArrayList<>( facets.stream().map( f -> new SortItem(f,-1,Utilities.getTransliterationString(f, false),-1)).collect(Collectors.toList())));
        }
        
        WriteFacetsFromSortItems(logFileWriter, exportScheme, importThesaurusName,
                xmlFacetssortItems, hierarchyFacetsSortItems,
                termsInfo,
                FacetsFilter,
                HierarchiesFilter);

    }

    public void WriteFacetsFromSortItems(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            ArrayList<SortItem> xmlFacets, 
            HashMap<SortItem, ArrayList<SortItem>> hierarchyFacets,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            ArrayList<String> FacetsFilter,
            ArrayList<String> HierarchiesFilter) throws IOException {

        Utilities u = new Utilities();
        ArrayList<String> facetFilter = new ArrayList<String>();
        ArrayList<String> hierarchiesFilter = new ArrayList<String>();

        if (FacetsFilter != null && FacetsFilter.size() > 0) {
            facetFilter.addAll(FacetsFilter);
        }

        if (HierarchiesFilter != null && HierarchiesFilter.size() > 0) {
            hierarchiesFilter.addAll(HierarchiesFilter);
        }
        
        //getall facets and put a record in the above structure
        ArrayList<SortItem> facetsToExportInSortItemFormat = new ArrayList<SortItem>();
        ArrayList<String> facetsToExportInStringFormat = new ArrayList<String>();
        if (facetFilter.isEmpty()) {
            facetsToExportInSortItemFormat.addAll(xmlFacets);
            facetsToExportInStringFormat.addAll(Utilities.getStringVectorFromSortItemVector(xmlFacets));

            Iterator<SortItem> hierEnum = hierarchyFacets.keySet().iterator();
            while (hierEnum.hasNext()) {
                SortItem hierarchyName = hierEnum.next();
                ArrayList<SortItem> facets = hierarchyFacets.get(hierarchyName);
                if (facets != null) {
                    for (SortItem facetName : facets) {
                         if (facetName != null && facetName.getLogName()!=null && facetName.getLogName().length() > 0 && facetsToExportInStringFormat.contains(facetName.getLogName()) == false) {
                            String translit = facetName.getLogNameTransliteration();
                            facetsToExportInSortItemFormat.add(new SortItem(facetName.getLogName(), -1, translit.length()>0?translit: Utilities.getTransliterationString(facetName.getLogName(), false), facetName.getThesaurusReferenceId()));
                        }
                    }
                }
            }
        } else {
            facetsToExportInStringFormat.addAll(facetFilter);
            facetsToExportInSortItemFormat.addAll(Utilities.getSortItemVectorFromStringVector(facetFilter, false));
        }

        SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
        Collections.sort(facetsToExportInSortItemFormat, transliterationComparator);


        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            String schemePrefix = (Parameters.SkosExportUsingXmlBase? "": ConstantParameters.SchemePrefix +"/") ;

            //now find the ids of each hierarchy and fill the value vector of the main structure
            HashMap<SortItem, ArrayList<SortItem>> facetHierarchyIds = new HashMap<SortItem, ArrayList<SortItem>>();

            hierarchyFacets.forEach( (key, value) -> {
                if(value!=null){
                    for(SortItem facet : value){
                        if(facetHierarchyIds.containsKey(facet)==false){
                            facetHierarchyIds.put(facet, new ArrayList<SortItem>());
                        }
                        if(facetHierarchyIds.get(facet).contains(key)==false){
                            facetHierarchyIds.get(facet).add(key);
                        }
                    }
                }
            });
            
            //now write to file
            for (SortItem facetNameSortItem :  facetsToExportInSortItemFormat) {
                //Enumeration<String> facetHierIds = facetHierarchyIds.keys();
                //while(facetHierIds.hasMoreElements()){
                ArrayList<SortItem> values = new ArrayList<>();
                if(facetHierarchyIds.containsKey(facetNameSortItem)){
                    values.addAll(facetHierarchyIds.get(facetNameSortItem));
                    Collections.sort(values,transliterationComparator);
                }
 
                
                logFileWriter.append("\r\n\t<!-- Facet -->\r\n");
                String appendVal = "\t<rdf:Description" ;
                if(facetNameSortItem.getThesaurusReferenceId()>0){
                    appendVal += " rdf:about=\""+ getSkosUri(true,schemePrefix,facetNameSortItem.getThesaurusReferenceId()) +"\"";
                }
                appendVal+=">\r\n";
                logFileWriter.append(appendVal);
                logFileWriter.append("\t\t<rdf:type rdf:resource=\""+schemePrefix+Skos_Facet+"\"/>\r\n");
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(facetNameSortItem.getLogName()));
                logFileWriter.append("</"+ConstantParameters.XML_skos_prefLabel+">\r\n");
                if(facetNameSortItem.getLogNameTransliteration()!=null && facetNameSortItem.getLogNameTransliteration().length()>0){
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_hiddenLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(facetNameSortItem.getLogNameTransliteration()));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_hiddenLabel+">\r\n");
                }

                for (SortItem hierarchyVal : values) {
                    if (hierarchyVal != null && hierarchyVal.getLogName() !=null && hierarchyVal.getLogName().length() >0  && hierarchyVal.getThesaurusReferenceId() > 0) {
                        logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_member+" rdf:resource=\"" + getSkosUri(false,schemePrefix,hierarchyVal.getThesaurusReferenceId()) + "\"/> <!-- " + Utilities.escapeXMLComment(hierarchyVal.getLogName()) + " -->\r\n");
                    }
                }

                logFileWriter.append("\t\t<skos:inScheme rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + Utilities.escapeXMLComment(importThesaurusName) + " -->\r\n");
                logFileWriter.append("\t</rdf:Description>\r\n");
            }

        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Facets");
            logFileWriter.append("\r\n\t<facets count=\"" + facetsToExportInSortItemFormat.size() + "\">\r\n");

            for (SortItem item : facetsToExportInSortItemFormat) {
                //logFileWriter.append("\t\t<facet index=\"" + (i + 1) + "\">\r\n");
                logFileWriter.append("\t\t<facet>\r\n");
                String appendValue = "\t\t\t<name";

                if (item.getThesaurusReferenceId() > 0) {
                    appendValue += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + item.getThesaurusReferenceId() + "\"";
                    if (Parameters.ShowReferenceURIalso) {
                        appendValue += " " + ConstantParameters.system_referenceUri_kwd + "=\"" + u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.FACET, item.getThesaurusReferenceId()) + "\"";
                    }
                }
                appendValue += ">";
                logFileWriter.append(appendValue);

                logFileWriter.append(Utilities.escapeXML(item.getLogName()));
                logFileWriter.append("</name>\r\n");

                if (item.getLogNameTransliteration() != null && item.getLogNameTransliteration().length() > 0) {
                    logFileWriter.append("\t\t\t<" + ConstantParameters.system_transliteration_kwd + ">");
                    logFileWriter.append(Utilities.escapeXML(item.getLogNameTransliteration()));
                    logFileWriter.append("</" + ConstantParameters.system_transliteration_kwd + ">\r\n");
                }

                logFileWriter.append("\t\t</facet>\r\n");
            }

            logFileWriter.append("\t</facets>\r\n");

        }
        logFileWriter.flush();
    }

    public void ConversionActionWriteTHEMASTermToSkosConcept(OutputStreamWriter logFileWriter, String importThesaurusName, String targetTermName, boolean isTopConcept,
            HashMap<String, NodeInfoStringContainer> termsInfo, HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            ArrayList<String> TermsFilter) throws IOException {

        //PROPABLY NOT WORKING (it search for tcs not refids)
        //should add sources
        /*
         * for (int i = 0; i < pack.getSourceEn().size(); i++) {
         temp += "  <dcterms:source xml:lang=\"" + "en" + '"' + " rdf:resource=" + '"' + pack.getSourceEn().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         for (int i = 0; i < pack.getSourceGr().size(); i++) {
         temp += "  <dcterms:source xml:lang=\"" + "el" + '"' + " rdf:resource=" + '"' + pack.getSourceGr().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         */
        ArrayList<String> termsFilter = new ArrayList<>();

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        String schemePrefix = (Parameters.SkosExportUsingXmlBase? "": ConstantParameters.SchemePrefix +"/") ;
        
        NodeInfoStringContainer targetTermInfo = termsInfo.get(targetTermName);

        /*
        String targetTermId = "";
        ArrayList<String> tcs = targetTermInfo.descriptorInfo.get(ConstantParameters.tc_kwd);

        if (tcs != null && tcs.size() > 0) {
            targetTermId = ParseFileData.readSkosTC(tcs.get(0));
        }

        if (targetTermId.length() > 0) {
            targetTermId = schemePrefix + "/" + targetTermId;
        }
*/
        String targetTermId = "";
        if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.system_referenceUri_kwd)){
            //targetTermId = getSkosUri(false, "", Long.parseLong(targetTermInfo.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).get(0)));
            targetTermId = targetTermInfo.descriptorInfo.get(ConstantParameters.system_referenceUri_kwd).get(0);
        }
        ArrayList<String> nts = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
        ArrayList<SortItem> guidTermNts = new ArrayList<SortItem>();
        if (XMLguideTermsRelations.containsKey(targetTermName)) {

            ArrayList<SortItem> tempNts = XMLguideTermsRelations.get(targetTermName);
            if (tempNts != null && tempNts.size() > 0) {
                guidTermNts.addAll(tempNts);
            }
        }

        String transliterationStr = "";
        if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.system_transliteration_kwd) &&
                targetTermInfo.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).size()>0){
            transliterationStr= targetTermInfo.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).get(0);
            
        }
        String targetTermStatus = "";
        if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.status_kwd) &&
                targetTermInfo.descriptorInfo.get(ConstantParameters.status_kwd).size()>0){
            targetTermStatus= targetTermInfo.descriptorInfo.get(ConstantParameters.status_kwd).get(0);
            
        }
        else{
            if (Parameters.PrimaryLang.toLowerCase().equals("en")) {
                targetTermStatus = "Under construction";
            } else {
                //greek translation of Under construction in hex form
                targetTermStatus = "\u03A5\u03C0\u03CC \u03B5\u03C0\u03B5\u03BE\u03B5\u03C1\u03B3\u03B1\u03C3\u03af\u03B1";
            }
        }        
        ArrayList<String> bts = targetTermInfo.descriptorInfo.get(ConstantParameters.bt_kwd);
        ArrayList<String> rts = targetTermInfo.descriptorInfo.get(ConstantParameters.rt_kwd);
        ArrayList<String> ufs = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_kwd);
        ArrayList<String> ufTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_translations_kwd);
        ArrayList<String> translations = targetTermInfo.descriptorInfo.get(ConstantParameters.translation_kwd);

        ArrayList<String> creators = targetTermInfo.descriptorInfo.get(ConstantParameters.created_by_kwd);
        ArrayList<String> creationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.created_on_kwd);
        if (creationDates != null) {
            Collections.sort(creationDates);
        }
        ArrayList<String> modificators = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_by_kwd);
        ArrayList<String> modificationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_on_kwd);
        if (modificationDates != null) {
            Collections.sort(modificationDates);
        }
        ArrayList<String> scopeNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
        ArrayList<String> scopeNoteTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);

        ArrayList<String> historicalNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);
        //skos:historyNote
        ArrayList<String> commentNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.comment_kwd);
        //skos:editorialNote
        ArrayList<String> notes = targetTermInfo.descriptorInfo.get(ConstantParameters.note_kwd);
        //skos:note

        if (targetTermId.length() > 0) {
            if (isTopConcept) {
                logFileWriter.append("\r\n\t<!-- TopConcept -->\r\n");
            } else {
                logFileWriter.append("\r\n\t<!-- Concept -->\r\n");
            }
            logFileWriter.append("\t<rdf:Description rdf:about=\"" + targetTermId + "\">\r\n");
            logFileWriter.append("\t\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\r\n");
            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_Notation+">"+targetTermId+"</"+ConstantParameters.XML_skos_Notation+">\r\n");
            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
            logFileWriter.append(Utilities.escapeXML(targetTermName));
            logFileWriter.append("</"+ConstantParameters.XML_skos_prefLabel+">\r\n");
            
            
            if(transliterationStr!=null && transliterationStr.length()>0){
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_hiddenLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(transliterationStr));
                logFileWriter.append("</"+ConstantParameters.XML_skos_hiddenLabel+">\r\n");
            }

            //status
            logFileWriter.append("\t\t<"+ConstantParameters.XML_iso_thes_status+">"+targetTermStatus+"</"+ConstantParameters.XML_iso_thes_status+">\r\n");
            
            
            Collections.sort(translations);
            for (int j = 0; j < translations.size(); j++) {
                String translationValue = translations.get(j);
                String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                String langCode = parts[0].toLowerCase();
                String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + langCode + "\">");
                logFileWriter.append(Utilities.escapeXML(langWord));
                logFileWriter.append("</"+ConstantParameters.XML_skos_prefLabel+">\r\n");
            }

            Collections.sort(ufs);
            for (int j = 0; j < ufs.size(); j++) {
                String value = ufs.get(j);

                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_altLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(value));
                logFileWriter.append("</"+ConstantParameters.XML_skos_altLabel+">\r\n");
            }
            Collections.sort(ufTranslations);
            for (int j = 0; j < ufTranslations.size(); j++) {
                String translationValue = ufTranslations.get(j);
                String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                String langCode = parts[0].toLowerCase();
                String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_altLabel+" xml:lang=\"" + langCode + "\">");
                logFileWriter.append(Utilities.escapeXML(langWord));
                logFileWriter.append("</"+ConstantParameters.XML_skos_altLabel+">\r\n");
            }

            //broader
            Collections.sort(bts);
            for (int j = 0; j < bts.size(); j++) {
                String termName = bts.get(j);
                ArrayList<String> termtcs = new ArrayList<String>();
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
                    termId = "/" + termId;
                }

                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_broader+" rdf:resource=\"" + termId + "\"/> <!-- " + Utilities.escapeXMLComment(termName) + " -->\n");

            }

            //Narrower
            ArrayList<SortItem> finalGuideTerms = new ArrayList<SortItem>();

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

            ArrayList<String> distinctGuideTerms = new ArrayList<String>();
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
                ArrayList<String> ntsWithThisGuideTerm = new ArrayList<String>();

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

                        ArrayList<String> nttcs = new ArrayList<String>();
                        if (termsInfo.containsKey(ntStr)) {
                            nttcs.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.tc_kwd));
                        }

                        String ntId = "";
                        if (nttcs != null && nttcs.size() > 0) {
                            ntId = ParseFileData.readSkosTC(nttcs.get(0));
                        }
                        if (ntId.length() > 0) {
                           // ntId = schemePrefix + "/" + ntId;
                        }

                        logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_narrower+" rdf:resource=\"" + ntId + "\"/> <!-- " + Utilities.escapeXMLComment(ntStr) + " -->\n");
                    }

                } else {

                    Collections.sort(ntsWithThisGuideTerm);
                    if (ntsWithThisGuideTerm != null && ntsWithThisGuideTerm.size() > 0) {
                        logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_narrower+">\r\n");
                        logFileWriter.append("\t\t\t<"+ConstantParameters.XML_skos_collection+">\r\n");
                        logFileWriter.append("\t\t\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">" + Utilities.escapeXML(targetGuideTerm) + "</"+ConstantParameters.XML_skos_prefLabel+">\r\n");

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

                            ArrayList<String> nttcs = new ArrayList<String>();
                            if (termsInfo.containsKey(ntStr)) {
                                nttcs.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.tc_kwd));
                            }

                            String ntId = "";
                            if (nttcs != null && nttcs.size() > 0) {
                                ntId = ParseFileData.readSkosTC(nttcs.get(0));
                            }
                            if (ntId.length() > 0) {
                             //   ntId = schemePrefix + "/" + ntId;
                            }

                            logFileWriter.append("\t\t\t\t<"+ConstantParameters.XML_skos_member+" rdf:resource=\"" + ntId + "\"/> <!-- " +Utilities.escapeXMLComment(ntStr) + " -->\n");

                        }

                        logFileWriter.append("\t\t\t</"+ConstantParameters.XML_skos_collection+">\r\n");
                        logFileWriter.append("\t\t</"+ConstantParameters.XML_skos_narrower+">\r\n");
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
                ArrayList<String> termtcs = new ArrayList<String>();

                if (termsInfo.containsKey(termName)) {
                    termtcs.addAll(termsInfo.get(termName).descriptorInfo.get(ConstantParameters.tc_kwd));
                }

                String termId = "";
                if (termtcs != null && termtcs.size() > 0) {
                    termId = ParseFileData.readSkosTC(termtcs.get(0));
                }
                if (termId.length() > 0) {
                    //termId = schemePrefix + "/" + termId;
                }
                logFileWriter.append("\t\t<skos:related rdf:resource=\"" + termId + "\"/> <!-- " + Utilities.escapeXMLComment(termName) + " -->\n");

            }

            if (scopeNotes != null && scopeNotes.size() > 0) {
                String scopeNoteVal = scopeNotes.get(0);
                if (scopeNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_scopeNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(scopeNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_scopeNote+">\r\n");
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
                            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_scopeNote+" xml:lang=\"" + langCode.toLowerCase() + "\">");
                            logFileWriter.append(Utilities.escapeXML(noteStr));
                            logFileWriter.append("</"+ConstantParameters.XML_skos_scopeNote+">\r\n");
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
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_historyNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(historicalNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_historyNote+">\r\n");
                }
            }

            if (commentNotes != null && commentNotes.size() > 0) {
                String commentNoteVal = commentNotes.get(0);
                if (commentNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_EditorialNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(commentNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_EditorialNote+">\r\n");
                }
            }
            
            if (notes != null && notes.size() > 0) {
                String noteVal = notes.get(0);
                if (noteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_Note+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(noteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_Note+">\r\n");
                }
            }

            Collections.sort(creators);
            for (int j = 0; j < creators.size(); j++) {
                String value = creators.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dcterms:creator>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dcterms:creator>\r\n");
                }
            }
            Collections.sort(creationDates);
            for (int j = 0; j < creationDates.size(); j++) {
                String value = creationDates.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dcterms:created>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dcterms:created>\r\n");
                }
            }

            Collections.sort(modificators);
            for (int j = 0; j < modificators.size(); j++) {
                String value = modificators.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dcterms:contributor>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dcterms:contributor>\r\n");
                }
            }
            Collections.sort(modificationDates);
            for (int j = 0; j < modificationDates.size(); j++) {
                String value = modificationDates.get(j);
                if (value != null && value.length() > 0) {
                    logFileWriter.append("\t\t<dcterms:modified>");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</dcterms:modified>\r\n");
                }
            }

            if (isTopConcept) {
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_topConceptOf+" rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + Utilities.escapeXMLComment(importThesaurusName) + " -->\r\n");
            } else {
                logFileWriter.append("\t\t<skos:inScheme rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + Utilities.escapeXMLComment(importThesaurusName) + " -->\r\n");
            }
            logFileWriter.append("\t</rdf:Description>\r\n");
        }
    }
    
    public void WriteTHEMASTermToSkosConceptSortItem(OutputStreamWriter logFileWriter, String importThesaurusName,
            SortItem targetSortItem, boolean isTopConcept,
            HashMap<String, NodeInfoStringContainer> termsInfo,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            HashMap<String, ArrayList<ExternalLink>> termExtLinks,
            ArrayList<String> TermsFilter) throws IOException {

        //should add sources
        /*
         * for (int i = 0; i < pack.getSourceEn().size(); i++) {
         temp += "  <dcterms:source xml:lang=\"" + "en" + '"' + " rdf:resource=" + '"' + pack.getSourceEn().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         for (int i = 0; i < pack.getSourceGr().size(); i++) {
         temp += "  <dcterms:source xml:lang=\"" + "el" + '"' + " rdf:resource=" + '"' + pack.getSourceGr().keySet().toArray()[i].toString() + '"' + "/>\n";
         }
         */
        
        ArrayList<String> termsFilter = new ArrayList<String>();

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        String schemePrefix = (Parameters.SkosExportUsingXmlBase? "": ConstantParameters.SchemePrefix +"/") ;

        NodeInfoStringContainer targetTermInfo = termsInfo.get(targetSortItem.getLogName());

        String targetTermId = getSkosUri(false,schemePrefix,targetSortItem.getThesaurusReferenceId()) ;
        /*
        ArrayList<String> tcs = targetTermInfo.descriptorInfo.get(ConstantParameters.tc_kwd);

        if (tcs != null && tcs.size() > 0) {
            targetTermId = ParseFileData.readSkosTC(tcs.get(0));
        }

        if (targetTermId.length() > 0) {
            targetTermId = schemePrefix + "/" + targetTermId;
        }
        */

        
        
        
        
        ArrayList<String> nts = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
        ArrayList<SortItem> guidTermNts = new ArrayList<>();
        if (XMLguideTermsRelations.containsKey(targetSortItem.getLogName())) {

            ArrayList<SortItem> tempNts = XMLguideTermsRelations.get(targetSortItem.getLogName());
            if (tempNts != null && tempNts.size() > 0) {
                guidTermNts.addAll(tempNts);
            }
        }

        String transliterationStr = "";
        if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.system_transliteration_kwd) &&
                targetTermInfo.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).size()>0){
            transliterationStr= targetTermInfo.descriptorInfo.get(ConstantParameters.system_transliteration_kwd).get(0);            
        }
        else if(targetSortItem.getLogNameTransliteration()!=null && targetSortItem.getLogNameTransliteration().length()>0){
            transliterationStr = targetSortItem.getLogNameTransliteration();
        }
        String targetTermStatus = "";
        if(targetTermInfo.descriptorInfo.containsKey(ConstantParameters.status_kwd) &&
                targetTermInfo.descriptorInfo.get(ConstantParameters.status_kwd).size()>0){
            targetTermStatus= targetTermInfo.descriptorInfo.get(ConstantParameters.status_kwd).get(0);
            
        }
        /*
        else{
            //if case status has not been retrieved 
            if (Parameters.PrimaryLang.toLowerCase().equals("en")) {
                targetTermStatus = "Under construction";
            } else {
                //greek translation of Under construction in hex form
                targetTermStatus = "\u03A5\u03C0\u03CC \u03B5\u03C0\u03B5\u03BE\u03B5\u03C1\u03B3\u03B1\u03C3\u03af\u03B1";
            }
        } */ 
        
        
        
        
        
        ArrayList<String> bts = targetTermInfo.descriptorInfo.get(ConstantParameters.bt_kwd);
        ArrayList<String> rts = targetTermInfo.descriptorInfo.get(ConstantParameters.rt_kwd);
        
        ArrayList<String> exactMatchNodes = new ArrayList<>();
        if(Parameters.createSKOSHierarchicalUris){
            exactMatchNodes.addAll(targetTermInfo.descriptorInfo.get(ConstantParameters.system_allHierarchicalUris_kwd));
        }
        if(Parameters.replaceExactMatchLastPartWithId){
            for(int i=0; i< exactMatchNodes.size(); i++){
                String initialString =exactMatchNodes.get(i);
                String testString = "/"+targetSortItem.getLogName();
                String replaceString = "/"+targetSortItem.getThesaurusReferenceId();

                if(initialString.endsWith(testString)){
                    int lastIndex = initialString.lastIndexOf(testString);
                    if(lastIndex>0){
                        exactMatchNodes.set(i, initialString.substring(0, lastIndex) + replaceString);
                    }
                    else{
                        exactMatchNodes.set(i, replaceString);
                    }
                }
            }
        }
        ArrayList<String> ufs = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_kwd);
        ArrayList<String> ufTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.uf_translations_kwd);
        ArrayList<String> translations = targetTermInfo.descriptorInfo.get(ConstantParameters.translation_kwd);

        
        ArrayList<String> allSources = targetTermInfo.descriptorInfo.get(ConstantParameters.primary_found_in_kwd);
        if(allSources==null){
            allSources = new ArrayList<>();
        }
        ArrayList<String> translationSources = targetTermInfo.descriptorInfo.get(ConstantParameters.translations_found_in_kwd);
        if(translationSources!=null){
            for(String trSource : translationSources){
                if(!allSources.contains(trSource)){
                    allSources.add(trSource);
                }
            }
        }
        
        if(translations!=null && translations.size()>1){
            //keep at most one tranlsation per language
            //this is a skos recommendation and this is this patch has been addded
            ArrayList<String> simpleLangCode = new ArrayList<>();
            ArrayList<String> moveToUfTranslations = new ArrayList<>();
            for(String str : translations){
                if(str.contains(Parameters.TRANSLATION_SEPERATOR)){
                    String langCode = str.substring(0, str.indexOf(Parameters.TRANSLATION_SEPERATOR)).toUpperCase().trim()+Parameters.TRANSLATION_SEPERATOR;
                    if(!simpleLangCode.contains(langCode)){
                        simpleLangCode.add(langCode);
                    }
                }
            }
            for(String lang : simpleLangCode){
                int found =0;
                for(String str : translations){
                    if(str.startsWith(lang)){
                        found++;
                        if(found>1){
                            
                            moveToUfTranslations.add(str);
                        }
                    }
                }
            }
            
            if(!moveToUfTranslations.isEmpty()){
                for(String movement : moveToUfTranslations){
                    if(ufTranslations!=null && !ufTranslations.contains(movement)){
                        ufTranslations.add(movement);
                    }
                }
                
                translations.removeAll(moveToUfTranslations);
            }
            
        }
        
        ArrayList<String> creators = targetTermInfo.descriptorInfo.get(ConstantParameters.created_by_kwd);
        ArrayList<String> creationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.created_on_kwd);
        if (creationDates != null) {
            Collections.sort(creationDates);
        }
        ArrayList<String> modificators = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_by_kwd);
        ArrayList<String> modificationDates = targetTermInfo.descriptorInfo.get(ConstantParameters.modified_on_kwd);
        if (modificationDates != null) {
            Collections.sort(modificationDates);
        }
        ArrayList<String> scopeNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.scope_note_kwd);
        ArrayList<String> scopeNoteTranslations = targetTermInfo.descriptorInfo.get(ConstantParameters.translations_scope_note_kwd);

        ArrayList<String> historicalNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.historical_note_kwd);
        //skos:historyNote
        ArrayList<String> commentNotes = targetTermInfo.descriptorInfo.get(ConstantParameters.comment_kwd);
        //skos:editorialNote
        ArrayList<String> notes = targetTermInfo.descriptorInfo.get(ConstantParameters.note_kwd);
        //skos:note

        if (targetTermId.length() > 0) {
            if (isTopConcept) {
                logFileWriter.append("\r\n\t<!-- TopConcept -->\r\n");
            } else {
                logFileWriter.append("\r\n\t<!-- Concept -->\r\n");
            }
            //logFileWriter.append("\t<skos:Concept rdf:about=\"" + targetTermId + "\">\r\n");
            logFileWriter.append("\t<rdf:Description rdf:about=\"" + targetTermId + "\">\r\n");
            logFileWriter.append("\t\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\r\n");
            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_Notation+">"+targetSortItem.getThesaurusReferenceId()+"</"+ConstantParameters.XML_skos_Notation+">\r\n");            
            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
            logFileWriter.append(Utilities.escapeXML(targetSortItem.getLogName()));
            logFileWriter.append("</"+ConstantParameters.XML_skos_prefLabel+">\r\n");
            
            if(transliterationStr!=null && transliterationStr.length()>0){
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_hiddenLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                logFileWriter.append(Utilities.escapeXML(transliterationStr));
                logFileWriter.append("</"+ConstantParameters.XML_skos_hiddenLabel+">\r\n");
            }

            if(targetTermStatus.length()>0){
                //status
                logFileWriter.append("\t\t<"+ConstantParameters.XML_iso_thes_status+">"+targetTermStatus+"</"+ConstantParameters.XML_iso_thes_status+">\r\n");
                //targetSortItem.getLogName()
            }
            if(translations!=null){
                Collections.sort(translations);
                for (int j = 0; j < translations.size(); j++) {
                    String translationValue = translations.get(j);
                    String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                    String langCode = parts[0].toLowerCase();
                    String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + langCode + "\">");
                    logFileWriter.append(Utilities.escapeXML(langWord));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_prefLabel+">\r\n");
                }
            }
            if(ufs!=null){
                Collections.sort(ufs);
                for (int j = 0; j < ufs.size(); j++) {
                    String value = ufs.get(j);

                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_altLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(value));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_altLabel+">\r\n");
                }
            }
            if(ufTranslations!=null){
                Collections.sort(ufTranslations);
                for (int j = 0; j < ufTranslations.size(); j++) {
                    String translationValue = ufTranslations.get(j);
                    String[] parts = translationValue.split(Parameters.TRANSLATION_SEPERATOR);
                    String langCode = parts[0].toLowerCase();
                    String langWord = translationValue.replaceFirst(parts[0] + Parameters.TRANSLATION_SEPERATOR, "");
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_altLabel+" xml:lang=\"" + langCode + "\">");
                    logFileWriter.append(Utilities.escapeXML(langWord));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_altLabel+">\r\n");
                }
            }

            if(bts!=null){
                //broader
                Collections.sort(bts);
                for (String btTermName : bts) {
                    ArrayList<String> termReferenceId = new ArrayList<String>();
                    if (ConstantParameters.filterBts_Nts_Rts) {
                        if (termsInfo.containsKey(btTermName) == false) {
                            continue;
                        }
                    }
                    if (termsFilter.size() > 0 && termsFilter.contains(btTermName) == false) {
                        continue;
                    }

                    if (termsInfo.containsKey(btTermName)) {
                        termReferenceId.addAll(termsInfo.get(btTermName).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd));
                    }

                    String uriVal = "";
                    long termId = -1;
                    if(!termReferenceId.isEmpty()){
                        try{
                            termId = Long.parseLong(termReferenceId.get(0));
                        }catch(NumberFormatException ex){
                            Utils.StaticClass.handleException(ex);
                        }
                    }
                    if (termId > 0) {
                        uriVal = getSkosUri(false,schemePrefix,termId) ;
                    }

                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_broader+" rdf:resource=\"" + uriVal + "\"/> <!-- " + Utilities.escapeXMLComment(btTermName) + " -->\n");

                }
            }

            //Narrower
            ArrayList<SortItem> finalGuideTerms = new ArrayList<>();

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

            ArrayList<String> distinctGuideTerms = new ArrayList<>();
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
                ArrayList<String> ntsWithThisGuideTerm = new ArrayList<>();

                for (int k = 0; k < finalGuideTerms.size(); k++) {
                    SortItem sitem = finalGuideTerms.get(k);
                    String guildeTerm = sitem.linkClass;
                    String targetNt = sitem.log_name;

                    if (targetGuideTerm.equals(guildeTerm)) {
                        ntsWithThisGuideTerm.add(targetNt);
                    }
                }

                Collections.sort(ntsWithThisGuideTerm);

                //disabling guide term output (link narrower to collection instead of concept) as it is not skos valid 
                //if (targetGuideTerm.length() == 0) {
                String targetGuideTermComment = targetGuideTerm.isEmpty() ?"": ("("+Utilities.escapeXMLComment(targetGuideTerm) +")  ");
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

                        ArrayList<String> ntReferenceIds = new ArrayList<>();
                        if (termsInfo.containsKey(ntStr)) {
                            ntReferenceIds.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd));
                        }
               
                        String ntUriVal = "";
                        long ntId = -1;
                        if (!ntReferenceIds.isEmpty()) {
                             try{
                                ntId = Long.parseLong(ntReferenceIds.get(0));
                            }catch(NumberFormatException ex){
                                Utils.StaticClass.handleException(ex);
                            }                            
                        }
                        if (ntId> 0) {
                            ntUriVal = getSkosUri(false,schemePrefix,ntId) ;
                        }

                        logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_narrower+" rdf:resource=\"" + ntUriVal + "\"/> <!-- "+ targetGuideTermComment + Utilities.escapeXMLComment(ntStr) + " -->\n");
                    }

                /* disabling guide term output (link narrower to collection instead of concept) as it is not skos valid 
                    } else {

                    Collections.sort(ntsWithThisGuideTerm);
                    if (ntsWithThisGuideTerm != null && ntsWithThisGuideTerm.size() > 0) {
                        logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_narrower+">\r\n");
                        logFileWriter.append("\t\t\t<"+ConstantParameters.XML_skos_collection+">\r\n");
                        logFileWriter.append("\t\t\t\t<"+ConstantParameters.XML_skos_prefLabel+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">" + Utilities.escapeXML(targetGuideTerm) + "</"+ConstantParameters.XML_skos_prefLabel+">\r\n");

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

                            ArrayList<String> nttcs = new ArrayList<String>();
                            if (termsInfo.containsKey(ntStr)) {
                                nttcs.addAll(termsInfo.get(ntStr).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd));
                            }

                            String ntUriVal = "";
                            long ntId = -1;
                            if (!nttcs.isEmpty()) {
                                 try{
                                    ntId = Long.parseLong(nttcs.get(0));
                                }catch(NumberFormatException ex){
                                    Utils.StaticClass.handleException(ex);
                                }                            
                            }
                            if (ntId> 0) {
                                ntUriVal = getSkosUri(false,schemePrefix,ntId) ;
                            }

                            logFileWriter.append("\t\t\t\t<"+ConstantParameters.XML_skos_member+" rdf:resource=\"" + ntUriVal + "\"/> <!-- " + Utilities.escapeXMLComment(ntStr) + " -->\n");

                        }

                        logFileWriter.append("\t\t\t</"+ConstantParameters.XML_skos_collection+">\r\n");
                        logFileWriter.append("\t\t</"+ConstantParameters.XML_skos_narrower+">\r\n");
                    }

                }*/
            }
            if(rts!=null){
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
                    ArrayList<String> termtcs = new ArrayList<String>();

                    if (termsInfo.containsKey(termName)) {
                        termtcs.addAll(termsInfo.get(termName).descriptorInfo.get(ConstantParameters.system_referenceUri_kwd));
                    }

                    String rtUriVal = "";
                    long termId = -1;
                    if (!termtcs.isEmpty()) {
                         try{
                            termId = Long.parseLong(termtcs.get(0));
                        }catch(NumberFormatException ex){
                            Utils.StaticClass.handleException(ex);
                        }                            
                    }
                    if (termId> 0) {
                        rtUriVal = getSkosUri(false,schemePrefix,termId) ;
                    }

                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_related+" rdf:resource=\"" + rtUriVal + "\"/> <!-- " + Utilities.escapeXMLComment(termName) + " -->\n");

                }
            }

            if (scopeNotes != null && scopeNotes.size() > 0) {
                String scopeNoteVal = scopeNotes.get(0);
                if (scopeNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_scopeNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(scopeNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_scopeNote+">\r\n");
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
                            logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_scopeNote+" xml:lang=\"" + langCode.toLowerCase() + "\">");
                            logFileWriter.append(Utilities.escapeXML(noteStr));
                            logFileWriter.append("</"+ConstantParameters.XML_skos_scopeNote+">\r\n");
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
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_historyNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(historicalNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_historyNote+">\r\n");
                }
            }

            if (commentNotes != null && commentNotes.size() > 0) {
                String commentNoteVal = commentNotes.get(0);
                if (commentNoteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_EditorialNote+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(commentNoteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_EditorialNote+">\r\n");
                }
            }
            
            if (notes != null && notes.size() > 0) {
                String noteVal = notes.get(0);
                if (noteVal.length() > 0) {
                    logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_Note+" xml:lang=\"" + Parameters.PrimaryLang.toLowerCase() + "\">");
                    logFileWriter.append(Utilities.escapeXML(noteVal));
                    logFileWriter.append("</"+ConstantParameters.XML_skos_Note+">\r\n");
                }
            }
            
            if(allSources!=null){
                Collections.sort(allSources);
                for (int j = 0; j < allSources.size(); j++) {
                    String value = allSources.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\t\t<dcterms:source>");
                        logFileWriter.append(Utilities.escapeXML(value));
                        logFileWriter.append("</dcterms:source>\r\n");
                    }
                }
            }

            if(creators!=null){
                Collections.sort(creators);
                for (int j = 0; j < creators.size(); j++) {
                    String value = creators.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\t\t<dcterms:creator>");
                        logFileWriter.append(Utilities.escapeXML(value));
                        logFileWriter.append("</dcterms:creator>\r\n");
                    }
                }
            }
            
            if(creationDates!=null){
                Collections.sort(creationDates);
                for (int j = 0; j < creationDates.size(); j++) {
                    String value = creationDates.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\t\t<dcterms:created>");
                        logFileWriter.append(Utilities.escapeXML(value));
                        logFileWriter.append("</dcterms:created>\r\n");
                    }
                }
            }

            if(modificators!=null){
                Collections.sort(modificators);
                for (int j = 0; j < modificators.size(); j++) {
                    String value = modificators.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\t\t<dcterms:contributor>");
                        logFileWriter.append(Utilities.escapeXML(value));
                        logFileWriter.append("</dcterms:contributor>\r\n");
                    }
                }
            }
            
            if(modificationDates!=null){
                Collections.sort(modificationDates);
                for (int j = 0; j < modificationDates.size(); j++) {
                    String value = modificationDates.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\t\t<dcterms:modified>");
                        logFileWriter.append(Utilities.escapeXML(value));
                        logFileWriter.append("</dcterms:modified>\r\n");
                    }
                }
            }
            
            
            
            if(termExtLinks!=null && termExtLinks.containsKey(targetSortItem.getLogName())){
                ArrayList<ExternalLink> extLinks = termExtLinks.get(targetSortItem.getLogName());
                for(ExternalLink extLink : extLinks){
                    if(extLink.linkUri.length()>0){
                        
                        String targetMatchElement = ConstantParameters.XML_rdfs_seeAlso;
                        if(extLink.matchType.length()>0){
                            if(extLink.matchType.equalsIgnoreCase(ConstantParameters.attr_matchType_exact_match_value)){
                                targetMatchElement = ConstantParameters.XML_skos_exactMatch;
                            }
                            else if(extLink.matchType.equalsIgnoreCase(ConstantParameters.attr_matchType_close_match_value)){
                                targetMatchElement = ConstantParameters.XML_skos_closeMatch;
                            }
                        }
                        
                        logFileWriter.append("\t\t<"+targetMatchElement+" rdf:resource=\"" + Utilities.escapeXML(extLink.linkUri) + "\"/>\r\n");                        
                    }
                }
            }
            

            if (isTopConcept) {
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_topConceptOf+" rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + Utilities.escapeXMLComment(importThesaurusName) + " -->\r\n");
            } else {
                logFileWriter.append("\t\t<"+ConstantParameters.XML_skos_inScheme+" rdf:resource=\"" + ConstantParameters.referenceThesaurusSchemeName + "\"/> <!-- " + Utilities.escapeXMLComment(importThesaurusName) + " -->\r\n");
            }
            logFileWriter.append("\t</rdf:Description>\r\n");
            
            if(exactMatchNodes!=null){
                Collections.sort(exactMatchNodes);
                for (int j = 0; j < exactMatchNodes.size(); j++) {
                    String value = exactMatchNodes.get(j);
                    if (value != null && value.length() > 0) {
                        logFileWriter.append("\r\n\t<rdf:Description rdf:about=\"" + Utilities.escapeXML(schemePrefix+ value) + "\">\r\n");

                        logFileWriter.append("\t\t<"+ConstantParameters.XML_owlSameAs+" rdf:resource=\""+targetTermId+"\"/>\r\n");
                        logFileWriter.append("\t</rdf:Description>\r\n");
                    }
                }
            }
        }
    }

    public void ConversionActionWriteHierarchies(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            HashMap<String, ArrayList<String>> hierarchyFacets, HashMap<String, NodeInfoStringContainer> termsInfo,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            ArrayList<String> FacetsFilter,
            ArrayList<String> TermsFilter) throws IOException {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Hierarchies");

        ArrayList<String> facetFilter = new ArrayList<>();
        ArrayList<String> termsFilter = new ArrayList<>();

        if (FacetsFilter != null && FacetsFilter.size() > 0) {
            facetFilter.addAll(FacetsFilter);
        }

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            ArrayList<String> allHierarchies = new ArrayList<String>();
            if (termsFilter.size() == 0) {
                allHierarchies.addAll(hierarchyFacets.keySet());
            } else {
                Iterator<String> hierEnum = hierarchyFacets.keySet().iterator();
                while (hierEnum.hasNext()) {
                    String hierName = hierEnum.next();
                    if (termsFilter.contains(hierName) && allHierarchies.contains(hierName) == false) {
                        allHierarchies.add(hierName);
                    }
                }
            }
            Collections.sort(allHierarchies);

            if (allHierarchies.size() > 0) {
                for (int i = 0; i < allHierarchies.size(); i++) {
                    String hierarchyName = allHierarchies.get(i);
                    ////PROPABLY NOT WORKING (it searched for tcs not refids)
                    ConversionActionWriteTHEMASTermToSkosConcept(logFileWriter, importThesaurusName, hierarchyName, true, termsInfo, XMLguideTermsRelations, termsFilter);
                }
            }
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            ArrayList<String> allHierarchies = new ArrayList<String>();
            if (termsFilter.size() == 0) {
                allHierarchies.addAll(hierarchyFacets.keySet());
            } else {
                Iterator<String> hierEnum = hierarchyFacets.keySet().iterator();
                while (hierEnum.hasNext()) {
                    String hierName = hierEnum.next();
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
                    ArrayList<String> facets = hierarchyFacets.get(hierarchyName);
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

    public void WriteHierarchiesFromSortItems(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            HashMap<SortItem, ArrayList<SortItem>> hierarchyFacets, 
            HashMap<String, NodeInfoStringContainer> termsInfo,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            HashMap<String, ArrayList<ExternalLink>> termExtLinks,
            ArrayList<String> FacetsFilter,
            ArrayList<String> TermsFilter) throws IOException {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Hierarchies");

        ArrayList<String> facetFilter = new ArrayList<>();
        ArrayList<String> termsFilter = new ArrayList<>();

        if (FacetsFilter != null && FacetsFilter.size() > 0) {
            facetFilter.addAll(FacetsFilter);
        }

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        ArrayList<SortItem> allHierarchies = new ArrayList<>();
        if (termsFilter.isEmpty()) {
            allHierarchies.addAll(hierarchyFacets.keySet());
        } else {

            Iterator<SortItem> hierEnum = hierarchyFacets.keySet().iterator();
            while (hierEnum.hasNext()) {
                SortItem hierName = hierEnum.next();
                if (termsFilter.contains(hierName.getLogName()) && allHierarchies.contains(hierName) == false) {
                    allHierarchies.add(hierName);
                }
            }
        }

        SortItemComparator transliterationComparator = new SortItemComparator(SortItemComparator.SortItemComparatorField.TRANSLITERATION);
        Collections.sort(allHierarchies, transliterationComparator);

        if (allHierarchies.size() > 0) {
            if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

                for (SortItem hierarchySortItem : allHierarchies) {                    
                    WriteTHEMASTermToSkosConceptSortItem(logFileWriter, importThesaurusName, hierarchySortItem, true, termsInfo, XMLguideTermsRelations, termExtLinks, termsFilter);
                }

            } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

                //logFileWriter.append("\r\n\t<hierarchies>\r\n");
                logFileWriter.append("\r\n\t<hierarchies count=\"" + allHierarchies.size() + "\">\r\n");

                for (SortItem hierarchySortItem : allHierarchies) {
                    ArrayList<SortItem> facets = hierarchyFacets.get(hierarchySortItem);
                    if (facets != null) {
                        Collections.sort(facets, transliterationComparator);
                    }

                    //logFileWriter.append("\t\t<hierarchy index=\"" + (i+1) + "\">\r\n");
                    logFileWriter.append("\t\t<hierarchy>\r\n");
                    String appendVal = "\t\t\t<name";
                    if (hierarchySortItem.getThesaurusReferenceId() > 0) {
                        appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + hierarchySortItem.getThesaurusReferenceId() + "\"";
                    }
                    appendVal += ">";
                    logFileWriter.append(appendVal);
                    logFileWriter.append(Utilities.escapeXML(hierarchySortItem.getLogName()));
                    logFileWriter.append("</name>\r\n");
                    if (hierarchySortItem.getLogNameTransliteration() != null && hierarchySortItem.getLogNameTransliteration().length() > 0) {
                        logFileWriter.append("\t\t\t<" + ConstantParameters.system_transliteration_kwd + ">");
                        logFileWriter.append(Utilities.escapeXML(hierarchySortItem.getLogNameTransliteration()));
                        logFileWriter.append("</" + ConstantParameters.system_transliteration_kwd + ">\r\n");
                    }
                    for (SortItem facet : facets) {
                        if (facet != null && facet.getLogName().length() > 0) {
                            if (facetFilter.size() > 0 && facetFilter.contains(facet.getLogName()) == false) {
                                continue;
                            }
                            appendVal = "\t\t\t<facet";
                            if (facet.getThesaurusReferenceId() > 0) {
                                appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + facet.getThesaurusReferenceId() + "\"";
                            }
                            appendVal += ">";
                            logFileWriter.append(appendVal);
                            logFileWriter.append(Utilities.escapeXML(facet.getLogName()));
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
            HashMap<String, String> XMLsources) throws IOException {
        if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            Utilities u = new Utilities();
            if (XMLsources.size() > 0) {
                logFileWriter.append("\r\n\t<sources count=\"" + XMLsources.size() + "\">");
                ArrayList<String> sourceNames = new ArrayList<String>(XMLsources.keySet());
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

    /**
     * This function is used by both Themas Export Functionality and Conversion Action tools
     * @param logFileWriter
     * @param exportScheme
     * @param importThesaurusName
     * @param hierarchyFacets
     * @param termsInfo
     * @param XMLguideTermsRelations
     * @param termExtLinks
     * @param TermsFilter
     * @throws IOException 
     */
    public void WriteTerms(OutputStreamWriter logFileWriter, String exportScheme, String importThesaurusName,
            HashMap<String, ArrayList<String>> hierarchyFacets, HashMap<String, NodeInfoStringContainer> termsInfo,
            HashMap<String, ArrayList<SortItem>> XMLguideTermsRelations,
            HashMap<String, ArrayList<ExternalLink>> termExtLinks,
            ArrayList<String> TermsFilter) throws IOException {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Exporting Terms");

        ArrayList<String> termsFilter = new ArrayList<String>();

        if (TermsFilter != null && TermsFilter.size() > 0) {
            termsFilter.addAll(TermsFilter);
        }

        if (exportScheme.equals(ConstantParameters.xmlschematype_skos)) {

            ArrayList<String> allHierarchies = new ArrayList<String>();
            allHierarchies.addAll(hierarchyFacets.keySet());

            ArrayList<SortItem> allTerms = new ArrayList<SortItem>();
            allTerms.addAll(Utilities.getSortItemVectorFromTermsInfo(termsInfo, false));
            
            allTerms.removeIf( item -> allTerms.contains(item.getLogName()));
            

            Collections.sort(allTerms, new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION)));

            if (allTerms.size() > 0) {
                for (SortItem termItem : allTerms) {
                    String termName = termItem.getLogName();

                    if (termsFilter.size() > 0 && termsFilter.contains(termName) == false) {
                        continue;
                    }
                    //avoid case of double writing (as topConcept and as simple concept)
                    if(!allHierarchies.contains(termName)){
                        WriteTHEMASTermToSkosConceptSortItem(logFileWriter, importThesaurusName, termItem, false, termsInfo, XMLguideTermsRelations, termExtLinks, termsFilter);
                    }
                }

            }
        } else if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {

            Utilities u = new Utilities();

            String[] output = {ConstantParameters.system_transliteration_kwd, 
                ConstantParameters.system_referenceUri_kwd,
                ConstantParameters.facet_kwd, 
                ConstantParameters.topterm_kwd, 
                ConstantParameters.status_kwd, 
                ConstantParameters.bt_kwd, 
                ConstantParameters.nt_kwd,
                ConstantParameters.tc_kwd, 
                ConstantParameters.translation_kwd, 
                ConstantParameters.rt_kwd, 
                ConstantParameters.uf_kwd,
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

            ArrayList<SortItem> allTerms = new ArrayList<>();

            if (termsFilter.isEmpty()) {
                allTerms.addAll(Utilities.getSortItemVectorFromTermsInfo(termsInfo, false));
            } else {
                Iterator<String> termsEnum = termsInfo.keySet().iterator();
                while (termsEnum.hasNext()) {
                    String termName = termsEnum.next();
                    if (termsFilter.contains(termName)) {
                        long refId = Utilities.retrieveThesaurusReferenceFromNodeInfoStringContainer(termsInfo.get(termName));
                        String transliteration = Utilities.retrieveTransliterationStringFromNodeInfoStringContainer(termsInfo.get(termName), termName, false);
                        long id = -1; //Utilities.retrieveDatabaseIdFromNodeInfoStringContainer(termsInfo.get(termName));
                        allTerms.add(new SortItem(termName, id, transliteration, refId));
                    }
                }
            }

            SortItemComparator transliterationComparator = new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            SortItemComparator linkClassTransliterationComparator = new SortItemComparator((SortItemComparator.SortItemComparatorField.TRANSLITERATION));
            Collections.sort(allTerms, transliterationComparator);

            ArrayList<String> specialCategories = new ArrayList<>();

            specialCategories.add(ConstantParameters.rt_kwd);
            specialCategories.add(ConstantParameters.bt_kwd);
            specialCategories.add(ConstantParameters.nt_kwd);
            specialCategories.add(ConstantParameters.translation_kwd);
            specialCategories.add(ConstantParameters.uf_translations_kwd);
            specialCategories.add(ConstantParameters.status_kwd);
            specialCategories.add(ConstantParameters.scope_note_kwd);
            specialCategories.add(ConstantParameters.translations_scope_note_kwd);
            specialCategories.add(ConstantParameters.historical_note_kwd);
            specialCategories.add(ConstantParameters.comment_kwd);
            specialCategories.add(ConstantParameters.note_kwd);

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
                    if (number > 0) {
                        logFileWriter.append(" " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + number + "\"");
                        if (Parameters.ShowReferenceURIalso) {
                            logFileWriter.append(" " + ConstantParameters.system_referenceUri_kwd + "=\"" + u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, number) + "\"");
                        }
                    }

                    logFileWriter.append(">" + Utilities.escapeXML(termName) + "</descriptor>\r\n");

                    for (int m = 0; m < output.length; m++) {
                        String category = output[m];
                        ArrayList<String> values = new ArrayList<>();
                        if (targetTermInfo.descriptorInfo.containsKey(category)) {
                            values.addAll(targetTermInfo.descriptorInfo.get(category));
                        }

                        if (category.equals(ConstantParameters.system_referenceUri_kwd)) {
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
                                ArrayList<SortItem> valueSortItems = Utilities.getSortItemVectorFromStringVectorAndTermsInfo(values, termsInfo, false);
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
                                    if (linkItem.getThesaurusReferenceId() > 0) {
                                        appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + linkItem.getThesaurusReferenceId() + "\"";
                                        if (Parameters.ShowReferenceURIalso) {
                                            appendVal += " " + ConstantParameters.system_referenceUri_kwd + "=\"" + Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, linkItem.getThesaurusReferenceId())) + "\"";
                                        }
                                    }
                                    appendVal += ">";
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
                                            logFileWriter.append("\t\t\t<" + category + " " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + Utilities.escapeXML(langCode.toUpperCase()) + "\">");
                                            logFileWriter.append(Utilities.escapeXML(noteStr));
                                            logFileWriter.append("</" + category + ">\r\n");
                                        }
                                    }

                                }

                            } else if (category.equals(ConstantParameters.scope_note_kwd)
                                    || category.equals(ConstantParameters.historical_note_kwd)
                                    || category.equals(ConstantParameters.comment_kwd)
                                    || category.equals(ConstantParameters.note_kwd)) {
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
                                        langCode = translationVal.substring(0, translationVal.indexOf(Parameters.TRANSLATION_SEPERATOR)).toUpperCase();
                                        //bug it skipped GR (Greek instead of el) and LA (Latin)
                                        //Linguist.SupportedTHEMASLangcodes(translationVal.substring(0, translationVal.indexOf(Parameters.TRANSLATION_SEPERATOR, 0)));
                                        val = translationVal.replaceFirst(langCode.toUpperCase() + Parameters.TRANSLATION_SEPERATOR, "");
                                    }
                                    if (langCode != null && !langCode.isEmpty() && val != null && !val.isEmpty()) {

                                        logFileWriter.append("\t\t\t<" + category + " " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + Utilities.escapeXML(langCode.toUpperCase()) + "\">");
                                        logFileWriter.append(Utilities.escapeXML(val));
                                        logFileWriter.append("</" + category + ">\r\n");
                                    }
                                }
                            } else if (category.equals(ConstantParameters.nt_kwd)) {
                                ArrayList<String> ntVals = targetTermInfo.descriptorInfo.get(ConstantParameters.nt_kwd);
                                ArrayList<SortItem> ntSortItems = Utilities.getSortItemVectorFromStringVectorAndTermsInfo(ntVals, termsInfo, false);
                                Collections.sort(ntSortItems, transliterationComparator);

                                ArrayList<SortItem> guidTermNts = new ArrayList<SortItem>();
                                if (XMLguideTermsRelations.containsKey(termName)) {

                                    ArrayList<SortItem> tempNts = XMLguideTermsRelations.get(termName);
                                    if (tempNts != null && tempNts.size() > 0) {
                                        guidTermNts.addAll(tempNts);
                                    }
                                }

                                //Narrower
                                ArrayList<SortItem> finalGuideTerms = new ArrayList<SortItem>();

                                if (ntSortItems != null && ntSortItems.size() > 0) {
                                    for (SortItem ntSIVal : ntSortItems) {

                                        if (ntSIVal == null || ntSIVal.getLogName() == null || ntSIVal.getLogName().length() == 0) {
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

                                ArrayList<String> distinctGuideTermsStrings = new ArrayList<String>();
                                ArrayList<SortItem> distinctGuideTermsSortItems = new ArrayList<SortItem>();
                                for (SortItem sitem : finalGuideTerms) {
                                    String guildeTerm = sitem.linkClass;
                                    if (distinctGuideTermsStrings.contains(guildeTerm) == false) {
                                        distinctGuideTermsSortItems.add(new SortItem(guildeTerm, -1, Utilities.getTransliterationString(sitem.linkClass, false), sitem.getThesaurusReferenceId()));
                                        distinctGuideTermsStrings.add(guildeTerm);
                                    }
                                }

                                Collections.sort(distinctGuideTermsSortItems, transliterationComparator);

                                for (SortItem targetGuideTermItem : distinctGuideTermsSortItems) {
                                    String targetGuideTerm = targetGuideTermItem.getLogName();
                                    ArrayList<SortItem> ntsWithThisGuideTerm = new ArrayList<SortItem>();

                                    for (SortItem sitem : finalGuideTerms) {
                                        String guildeTerm = sitem.linkClass;
                                        String targetNt = sitem.log_name;

                                        if (targetGuideTerm.equals(guildeTerm)) {
                                            ntsWithThisGuideTerm.add(new SortItem(targetNt, -1, guildeTerm, sitem.log_name_transliteration, sitem.getThesaurusReferenceId()));
                                        }
                                    }

                                    Collections.sort(ntsWithThisGuideTerm, linkClassTransliterationComparator);

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
                                            if (ntStr.getThesaurusReferenceId() > 0) {
                                                appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + ntStr.getThesaurusReferenceId() + "\"";
                                                if (Parameters.ShowReferenceURIalso) {
                                                    appendVal += " " + ConstantParameters.system_referenceUri_kwd + "=\"" + Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, ntStr.getThesaurusReferenceId())) + "\"";
                                                }
                                            }
                                            appendVal += ">";
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
                                            if (ntStr.getThesaurusReferenceId() > 0) {
                                                appendVal += " " + ConstantParameters.system_referenceIdAttribute_kwd + "=\"" + ntStr.getThesaurusReferenceId() + "\"";
                                                if (Parameters.ShowReferenceURIalso) {
                                                    appendVal += " " + ConstantParameters.system_referenceUri_kwd + "=\"" + Utilities.escapeXML(u.consrtuctReferenceUri(importThesaurusName, Utilities.ReferenceUriKind.TERM, ntStr.getThesaurusReferenceId())) + "\"";
                                                }
                                            }
                                            appendVal += " " + ConstantParameters.XMLLinkClassAttributeName + "=\"" + Utilities.escapeXML(targetGuideTerm) + "\">";
                                            logFileWriter.append(appendVal);
                                            logFileWriter.append(Utilities.escapeXML(ntStr.getLogName()));
                                            logFileWriter.append("</" + category + ">\r\n");

                                        }

                                    }
                                }
                            }

                        }
                    }
                    
                    if(termExtLinks!=null && termExtLinks.containsKey(termName)){
                        ArrayList<ExternalLink> extLinks = termExtLinks.get(termName);
                        for(ExternalLink extLink : extLinks){
                            if(extLink.linkUri.length()>0){
                                logFileWriter.append("\t\t\t<"+ConstantParameters.externalLink_kwd);
                                if(extLink.matchType.length()>0){
                                    logFileWriter.append(" "+ConstantParameters.externalLink_attr_matchType_kwd+"=\""+extLink.matchType+"\"");
                                }
                                if(extLink.vocabularyIdentifier.length()>0){
                                    logFileWriter.append(" "+ConstantParameters.externalLink_attr_vocabId_kwd+"=\""+extLink.vocabularyIdentifier+"\"");
                                }
                                logFileWriter.append(">"+Utilities.escapeXML(extLink.linkUri)+"</"+ConstantParameters.externalLink_kwd+">\r\n");
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
    
    public static String Skos_Facet = "Facet";
    public static String Skos_Concept = "Concept";
    
    private String getSkosUri(boolean isFacet, String prefix, long id){
        String retVal = prefix;
        if(isFacet){
            retVal+=Skos_Facet+"/";
        }
        else{
            retVal+=Skos_Concept+"/";//retVal+=Servlets.CardOf_Term.class.getSimpleName();
        }
        //retVal+="?"+ConstantParameters.system_referenceIdAttribute_kwd+"="+id+"&amp;mode="+ConstantParameters.XMLSTREAM;
        retVal+=id;
        return retVal;
    }

    public void WriteExtVocabularies(OutputStreamWriter logFileWriter, String exportScheme, ArrayList<ExternalVocabulary> vocabularyIdentifiers)  throws IOException {
        if (exportScheme.equals(ConstantParameters.xmlschematype_THEMAS)) {
            Utilities u = new Utilities();
            if (vocabularyIdentifiers.size() > 0) {
                logFileWriter.append("\r\n\t<"+ConstantParameters.XMLExternalVocabulariesWrapperElementName+" count=\"" + vocabularyIdentifiers.size() + "\">");
                
                ArrayList<String> vocNames = new ArrayList<>();
                for(ExternalVocabulary extVoc : vocabularyIdentifiers){
                    vocNames.add(extVoc.vocabularyIdentifier);
                }
                
                Collections.sort(vocNames);

                for (int k = 0; k < vocNames.size(); k++) {
                    String vocId = vocNames.get(k);
                    ExternalVocabulary vocObj = vocabularyIdentifiers.stream().filter(x -> x.vocabularyIdentifier.equals(vocId)).findFirst().get();
                    //logFileWriter.append("\r\n\t\t<source index=\""+(k+1)+"\">");
                    logFileWriter.append("\r\n\t\t<"+ConstantParameters.XMLExternalVocabulariesElementName+">");
                    logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_ShortName+">" + u.escapeXML(vocId) + "</"+ConstantParameters.XMLExternalVocabularies_ShortName+">");
                    
                    for(String str : vocObj.vocabularyFullName){
                        logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_FullName+">" + u.escapeXML(str) + "</"+ConstantParameters.XMLExternalVocabularies_FullName+">");
                    }
                    
                    for(String str : vocObj.vocabularyDescription){
                        logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_Description+">" + u.escapeXML(str) + "</"+ConstantParameters.XMLExternalVocabularies_Description+">");
                    }
                    for(String str : vocObj.vocabularyUri){
                        logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_Uri+">" + u.escapeXML(str) + "</"+ConstantParameters.XMLExternalVocabularies_Uri+">");
                    }
                    if(vocObj.vocabularyVersionString!=null && vocObj.vocabularyVersionString.length()>0){
                        logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_Version+">" + u.escapeXML(vocObj.vocabularyVersionString) + "</"+ConstantParameters.XMLExternalVocabularies_Version+">");
                    }
                    if(vocObj.vocabularyReleaseTimestamp!=null && vocObj.vocabularyReleaseTimestamp.length()>0){
                        logFileWriter.append("\r\n\t\t\t<"+ConstantParameters.XMLExternalVocabularies_ReleaseTimestamp+">" + u.escapeXML(vocObj.vocabularyReleaseTimestamp) + "</"+ConstantParameters.XMLExternalVocabularies_ReleaseTimestamp+">");
                    }
                    
                    logFileWriter.append("\r\n\t\t</"+ConstantParameters.XMLExternalVocabulariesElementName+">");
                }
                logFileWriter.append("\r\n\t</"+ConstantParameters.XMLExternalVocabulariesWrapperElementName+">\r\n");
            }
        }
    }
}
