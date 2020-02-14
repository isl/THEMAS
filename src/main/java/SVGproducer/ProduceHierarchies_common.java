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
package SVGproducer;


import DB_Classes.DBGeneral;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.StrLenComparator;
import Utils.Parameters;
import neo4j_sisapi.*;

import java.io.*;
import java.util.*;
import java.awt.Font;
import java.net.URLEncoder;
import javax.servlet.http.HttpSession;

/*-------------------------------------------------------------------
Class ProduceHierarchies_common:
The abstract class that implements most methods for creating an SVG
hierarchy...
--------------------------------------------------------------------*/
public abstract class ProduceHierarchies_common {
    //SIS API vars
    protected QClass Q;
    protected IntegerObject sis_session;
    protected StringObject strobj,  strobj1,  strobj2,  strobj3;
    protected StringObject /*categ,*/  categ1,  categ2,  categ3;
    protected StringObject fromcls/*,  label,  cls,  cls2*/;
    protected CMValue /*cmv,*/  cmv1;
    protected int ret_set,  ret_set1,  ret_set2,  ret_set3;
    protected IntegerObject uniq_categ,  traversed,  clsid,  sysid;
    protected CategorySet[] categs;
    // Read configuration class
    protected ReadSVGConfig I;
    protected PrintWriter writer;
    protected String HierarchiesArray[];
    protected int LIDsArray[];
    protected int TIDsArray[];
    protected int ModesArray[];
    protected ArrayList<String> V1;
    protected ArrayList<String> V2;
    protected ArrayList<String> V3;
    protected ArrayList<SVGOBJ> svgV;
    protected double relheight;
    protected double svgwidth;
    protected double svgheight;
    protected long dataModified;
    protected String theScope;
    protected String navbarEnabled;
    
    protected String resultString;
    final static String SVG_HEADER = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
            "<?xml-stylesheet href='../SVG.css' type='text/css'?>\n" +
            "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN'\n" +
            "'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>\n" +
            "<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' >\n";
    final static String SVG_HEADER_NAV = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
            "<?xml-stylesheet href='../SVG.css' type='text/css'?>\n" +
            "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN'\n" +
            "'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>\n" +
            "<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' width='700px' height='500px' onload=\"Init(evt)\" onmousedown=\"Grab(evt)\" onmouseup=\"Drop(evt)\" onmousemove=\"Drag(evt)\"  onmouseout=\"Drop(evt)\">\n";

    abstract int GetHierarchySet(UserInfoClass SessionUserInfo, String hierarchy, String lang, String style);
    String termPrefix, facetPrefix; // g.e. "EL`", "EKTClass`"
    String web_app_path; // g.e. http://139.91.183.22:8084
    // Font SVGFont = new Font("Arial", Font.PLAIN, 12); 
    Font SVGFont = new Font("Verdana", Font.PLAIN, 12);    // do NOT set the font family "Arial", because SVG is buggy for the combination of text "πώ" (ellhiko pi kai tonoymeno wmega)
    // do NOT set the size more than 16. The layout algorithm does NOT work for big font sizes!!
    // do NOT change the style (PLAIN). The code does not set it in SVG code => default (plain) is used
    ArrayList<String> DBPrefixes = new ArrayList<String>();
    //protected  Vector CurrentlyParsedNodes;
    final static String PATTERN_FOR_MARKING_CYCLIC_NODES = "||";

    protected ProduceHierarchies_common() {

        strobj = new StringObject();
        strobj1 = new StringObject();
        strobj2 = new StringObject();
        strobj3 = new StringObject();
        //categ = new StringObject();
        categ1 = new StringObject();
        categ2 = new StringObject();
        categ3 = new StringObject();
        fromcls = new StringObject();
        //label = new StringObject();
        //cls = new StringObject();
        //cls2 = new StringObject();
        //cmv = new CMValue();
        cmv1 = new CMValue();
        ret_set = -1;
        ret_set1 = -1;
        ret_set2 = -1;
        ret_set3 = -1;
        uniq_categ = new IntegerObject();
        traversed = new IntegerObject();
        clsid = new IntegerObject();
        sysid = new IntegerObject();
        //categs     = new CategorySet[1];


        Q = new QClass();
        sis_session = new IntegerObject();

        V1 = new ArrayList<String>();
        V2 = new ArrayList<String>();
        V3 = new ArrayList<String>();
        svgV = new ArrayList<SVGOBJ>();

        resultString = new String();

        relheight = 0;
        svgwidth = 0;
        svgheight = 0;

    //web_app_path = Parameters.path;
    //CurrentlyParsedNodes = new Vector();
    }

    /*-----------------------------------------------------------------------
    doJob()
    -------------------------------------------------------------------------
    FUNCTION: - The classes basic function doJob, calls all the necessary functions
    to have the work done
    RETURNS: A SVG String
    -------------------------------------------------------------------------*/
    protected String doJob(UserInfoClass SessionUserInfo, String name, String lang, String style)
            throws IOException {

//Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"doJob " + ((UserInfoClass)sessionInstance.getAttribute("SessionUser")).selectedThesaurus) ;
        resultString = "";
        V1 = new ArrayList<String>();
        V2 = new ArrayList<String>();
        V3 = new ArrayList<String>();
        svgV = new ArrayList<SVGOBJ>();
        DBGeneral dbGen = new DBGeneral();

        // BUG fix (karam): reseting of class members (without this, any graph following the first, has wrong x, y dimensions!)
        relheight = 0;
        svgwidth = 0;
        svgheight = 0;

        navbarEnabled = I.navbar_enabled;
        theScope = I.hierarchy_scope;


        name = ReplaceGuideTermMarks(name);
        String nameUnicode = ConvertUnicodeToLatin(name, lang);

        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+name);

        sis_session = new IntegerObject();

        //open connection and start Query
        if(dbGen.openConnectionAndStartQueryOrTransaction(Q, null, sis_session, null, null, true)!=QClass.APIFail){

            DBPrefixes = new ArrayList<String>();
            DBPrefixes = GetDBPrefixes();

            if (GetHierarchySet(SessionUserInfo, nameUnicode, lang, style) != 0) {
                Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "?");
            
            }
            double w = SVGOBJ.GetStringRealWidth(name, SVGFont);
            RecurseHierarchy(name, w, 0, 0, 0, 0, style);
            //RecurseHierarchy(name, name.length(), 0, 0, 0, 0, style); 
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Calling RecurseHierarchy() with name = " + name + " length = " + name.length());
            ProduceSVG(SessionUserInfo, lang);

            //end query and close connection
            Q.free_all_sets();
            Q.TEST_end_query();
            dbGen.CloseDBConnection(Q, null, sis_session, null, false);
        } else {
            Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ class ProduceHierarchies_common.");
            svgConnectionErrorMsg();

        }

        return resultString;


    }

    /*---------------------------------------------------------------------
    GetDBPrefixes()
    -----------------------------------------------------------------------
    OUTPUT: a Vector with the declared prefixes in the DB (instances of class Prefix)
    ----------------------------------------------------------------------*/
    public ArrayList<String> GetDBPrefixes() {
        int SISsession = sis_session.getValue();
        Q.reset_name_scope();
        Q.set_current_node(new StringObject("Prefix"));
        int set_c = Q.get_instances(0);

        ArrayList<String> prefixes = new ArrayList<String>();
        Q.reset_set(set_c);
        ArrayList<Return_Nodes_Row> retVals = new ArrayList<Return_Nodes_Row>();
        if(Q.bulk_return_nodes(set_c, retVals)!=QClass.APIFail){
            for(Return_Nodes_Row row:retVals){
                prefixes.add(row.get_v1_cls_logicalname());
            }
        }
        /*
        StringObject Cname = new StringObject();
        while ((Q.retur_nodes(set_c, Cname)) != QClass.APIFail) {
            prefixes.add(Cname.getValue());
        }*/
        Q.free_set(set_c);

        //Sort is needed for alphabetical sort bug fix when prefixes like EL` and THES1EL` are defined
        StrLenComparator strLen = new StrLenComparator(StrLenComparator.Descending);
        Collections.sort(prefixes, strLen);

        return prefixes;
    }

    /*-----------------------------------------------------------------------
    ConvertUnicodeToLatin()
    -------------------------------------------------------------------------*/
    protected String ConvertUnicodeToLatin(String name, String lang) {
        /*
        if (lang.equals("EN")) {
            return name;
        } else if (lang.equals("FR")) {
            return name;
        } else if (lang.equals("DE")) {
            return name;
        } else if (lang.equals("GR")) {
            return neo4j_sisapi.GreekConverter.Uni2ISO7String(name);
        } else {
         *
         */
            return name;
        //}
    }

    /*-----------------------------------------------------------------------
    ConvertLatinToUnicode()
    -------------------------------------------------------------------------*/
    protected String ConvertLatinToUnicode(String name, String lang) {
        /*
         if (lang.equals("EN")) {
            return name;
        } else if (lang.equals("FR")) {
            return name;
        } else if (lang.equals("DE")) {
            return name;
        } else if (lang.equals("GR")) {
            return neo4j_sisapi.GreekConverter.ISO72UniString(name);
        } else {*/
            return name;
        //}
    }


    /*-----------------------------------------------------------------------
    svgConnectionErrorMsg()
    -------------------------------------------------------------------------
    FUNCTION: - Creates the contents of an SVG structure displaying a Connection
    Error Message
    CALLED_BY: doJob() of this Class
    -------------------------------------------------------------------------*/
    protected void svgConnectionErrorMsg() {

        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "I'm sorry! Problem connecting with Neo4j Database");

        resultString += SVG_HEADER;
        resultString += "<text x=\"173.0\" y=\"29.0\" style=\"font-family:Arial; fontsize:14pt; fill:black;\">I'm sorry! Problem connecting with SIS at port of machine</text>";
        resultString += "</svg>\n";
    }

    /*-----------------------------------------------------------------------
    svgNodeNotFoundErrorMsg()
    -------------------------------------------------------------------------
    FUNCTION: - Creates the contents of an SVG structure displaying a Node Not
    Found Error Message
    CALLED_BY: doJob() of this Class
    -------------------------------------------------------------------------*/
    protected void svgNodeNotFoundErrorMsg() {
        svgNodeNotFoundErrorMsg();
    }

    protected void svgNodeNotFoundErrorMsg(String node) {
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + "Not an SIS node!");

        node = ReplaceBadChars(node);
        resultString += SVG_HEADER;
        resultString += "<text x=\"173.0\" y=\"29.0\" style=\"font-family:Arial; fontsize:14pt; fill:black;\"> " + node + " Not an SIS node!</text>";
        resultString += "</svg>\n";
    }


    /*---------------------------------------------------------------------
    SortParallelVectors()
    -----------------------------------------------------------------------
    OUTPUT: sorts alphabetically the 1st Vector V1 and pararelly the parallel Vector V2
    ----------------------------------------------------------------------*/
    public void SortParallelVectors(ArrayList V1, ArrayList V2) {
        // fill Vector V2 with prefixes the corresponding values of Vector V1
        // separated with "@@@@@"
        int size = V1.size();
        for (int i = 0; i < size; i++) {
            String str1 = V1.get(i).toString();
            String str2 = V2.get(i).toString();
            V2.set(i,(str1 + "@@@@@" + str2));
        }
        // sort both Vectors V1 and V2
        Collections.sort(V1);
        Collections.sort(V2);
        // remove from Vector V2 the prefixes added above
        for (int i = 0; i < size; i++) {
            String str2 = V2.get(i).toString();
            String old_value = str2.substring(str2.indexOf("@@@@@") + 5);
            V2.set(i,old_value);
        }
    }

    /*-----------------------------------------------------------------------
    RecurseHierarchy()
    -------------------------------------------------------------------------
    INPUT: - String topterm: the given "hierarchy" HttpServlet parameter
    - int maxsiblingsize: the lenght of the above parameter
    - int maxparentsize: 0
    FUNCTION: -
    CALLED_BY: doJob() of this class
    -------------------------------------------------------------------------*/
//protected double RecurseHierarchy(String topterm, int maxsiblingsize, int maxparentsize, int mode, int LID, int TID, String style) {
    protected double RecurseHierarchy(String topterm, double maxsiblingsize, double maxparentsize, int mode, int LID, int TID, String style) {
        String x, y;
        int i;
        //int maxkidsize=0;
        double maxkidsize = 0;
        int kidno = 0;
        double kidsheight = 0;
        double termheight = 0;
        ArrayList<String> V = new ArrayList<String>();
        ArrayList<String> Vstyles = new ArrayList<String>();
        ArrayList<String> hlpV = new ArrayList<String>();
        double w;

        /* freezed
        boolean cycleDetected = CycleDetection(topterm, null);            
        boolean isAdded = CurrentlyParsedNodes.contains(topterm);
        CurrentlyParsedNodes.add(topterm);
         */

        // if (cycleDetected == false || isAdded == false) {
        for (i = 0; i < V2.size(); i++) {
            x = V2.get(i).toString();

            if (x.compareTo(topterm) == 0) {
                y = V1.get(i).toString();
                V.add(y);
                String currentStyle = V3.get(i).toString();
                Vstyles.add(currentStyle);
                // TRYING TO FIX THE BUG of wrong cordinates for FRENCH graphs SVGproducer = new ProduceISAHierarchy(conf);
                //String translatedTerm = LanguageSwitch(sessionInstance, LID, y);

                String translatedTerm;

                translatedTerm = y;
                
                w = SVGOBJ.GetStringRealWidth(translatedTerm, SVGFont);
                if (w > maxkidsize) {
                    maxkidsize = w;
                }
                ++kidno;
            }
        }
        //}

        String url = new String();
        if (V.size() > 0) {
            // before calling recursively this function for each node of this level,
            // sort alphabetically the nodes of this level (Vector V) and pararelly the parallel Vector Vstyles
            SortParallelVectors(V, Vstyles);

            for (i = 0; i < V.size(); i++) {
                double tmp = RecurseHierarchy(V.get(i).toString(), maxkidsize, maxparentsize + 6 + maxsiblingsize, mode, LID, TID, Vstyles.get(i).toString());
                kidsheight += tmp;
                hlpV.add(Double.toString(tmp));
            }
            for (i = 0; i < hlpV.size(); i++) {
                double tmp = Double.parseDouble(hlpV.get(i).toString());
                String translatedTopTerm;
                translatedTopTerm = topterm;

                // line
                // karam bug fix: calculate the width of the rectangle without the prefix
                String translatedTopTermWithoutPrefix = translatedTopTerm.substring(translatedTopTerm.indexOf("`") + 1);

                w = SVGOBJ.GetStringRealWidth(translatedTopTermWithoutPrefix, SVGFont);
                svgV.add(new SVGOBJ(maxparentsize + w, kidsheight / kidno, maxparentsize + 6 + maxsiblingsize, tmp));

            //svgV.add(new SVGOBJ(maxparentsize + translatedTopTermWithoutPrefix.length(), kidsheight/kidno, maxparentsize + 6 + maxsiblingsize, tmp));
            }

            termheight = kidsheight / kidno;

            // rectangular
            svgV.add(new SVGOBJ(maxparentsize, termheight, topterm, url, style, SVGFont));
        } else {
            termheight = ++relheight;
            /* freezed
            if (cycleDetected == true && isAdded == true) {
            topterm += "...(cycle detected)";
            }
             */
            // rectangular
            svgV.add(new SVGOBJ(maxparentsize, termheight, topterm, url, style, SVGFont));
        }

        if (termheight > svgheight) {
            svgheight = termheight;
        }
        
        w = SVGOBJ.GetStringRealWidth(topterm, SVGFont);
        if (maxparentsize + w > svgwidth) {
            //if (maxparentsize + topterm.length() > svgwidth) {
            svgwidth = maxparentsize + w;
        }
        return termheight;
    }


    /*-----------------------------------------------------------------------
    CycleDetection() - freezed and not used
    -------------------------------------------------------------------------
    it was written so as to detect any possible cycle in a graph, but when 
    category EKTHierarchyTerm->EKT_RT was added, SIS server crashed in
    get_traverse_by_category() before RecurseHierarchy() was called 
    -------------------------------------------------------------------------*/
    boolean CycleDetection(String rootTarget, String targetChild) {
        String currentTarget = "";
        if (targetChild == null) { // 1st call
            currentTarget = rootTarget;
        } else { // any next call
            currentTarget = targetChild;
        }
        // for each from-value: search from current target
        for (int i = 0; i < V2.size(); i++) {
            String fromValue = V2.get(i).toString();
            // if found
            if (fromValue.compareTo(currentTarget) == 0) {
                // get the to-value
                String toValue = V1.get(i).toString();
                // in case of link from target to itself => cycle
                if (toValue.compareTo(rootTarget) == 0) {
                    return true;
                } // otherwise, continue the check with the found child of the target
                else {
                    return CycleDetection(rootTarget, toValue);
                }
            }
        }
        return false;
    }

    /*-----------------------------------------------------------------------
    GivenClassIsHierarchy()
    -------------------------------------------------------------------------*/
    boolean GivenClassIsHierarchy(String selectedThesaurus, String className, String lang) {
        String classNameDB = ConvertUnicodeToLatin(className, lang);
        // looking for EKTHierarchy
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject thesHierarchy = new StringObject();
        dbtr.getThesaurusClass_Hierarchy(selectedThesaurus, Q, sis_session.getValue(), thesHierarchy);

        int API_sessionID = sis_session.getValue();
        // get the classes of the given class
        Q.reset_name_scope();
        long xL = Q.set_current_node(new StringObject(classNameDB));
        int classesSet = Q.get_all_classes(0);
        // make a set with EKTHierarchy
        int set = Q.set_get_new();
        Q.reset_name_scope();
        long yL = Q.set_current_node(thesHierarchy);
        int z = Q.set_put(set);
        // get their intersection
        int k = Q.set_intersect(set, classesSet);
        boolean GivenClassIsHierarchy = false;
        int l = Q.set_get_card(set);
        if (Q.set_get_card(set) == 1) {
            GivenClassIsHierarchy = true;
        }
        Q.free_all_sets();
        return GivenClassIsHierarchy;
    }

    /*-----------------------------------------------------------------------
    ProduceSVG()
    -------------------------------------------------------------------------
    FUNCTION: - REPLACED the following ProduceSVGFile_OLD() function by karam at 14/6/04 to fix the
    bug which didn't fill SVG file with special french characters correctly
    CALLED_BY: doJob() of this Class
    -------------------------------------------------------------------------*/
    protected void ProduceSVG(UserInfoClass SessionUserInfo, String lang) throws java.io.UnsupportedEncodingException {

        String SVGHeader = "";
        StringBuffer FileContents = new StringBuffer();
        String SVGFooter = "";



        if (navbarEnabled.equals("true")) {
            SVGHeader += SVG_HEADER_NAV;
        } else if (navbarEnabled.equals("false")) {
            SVGHeader += SVG_HEADER;
        }

        SVGHeader += "<desc> Hierarchy svg file </desc>\n";
        SVGHeader += "<script  xmlns:xlink='http://www.w3.org/1999/xlink' type=\"text/ecmascript\" xlink:href=\"../SVGScripts.js\"/>\n" +
                "<script  xmlns:xlink='http://www.w3.org/1999/xlink' type=\"text/ecmascript\" xlink:href=\"../../Javascript/graphicalView.js\"/>\n";

        FileContents.append("<g id=\"group1\">\n");



        for (int i = 0; i < svgV.size(); i++) {

            SVGOBJ obj = (SVGOBJ) (svgV.get(i));
            if (obj.type == 0) {

                FileContents.append("<g>\n");
                String currentText = obj.text;
                if(obj.style.compareTo("styleTR")==0 || obj.style.compareTo("styleUFTranslations")==0){
                    currentText = currentText.replaceFirst(ConstantParameters.languageIdentifierSuffix, Parameters.TRANSLATION_SEPERATOR + " ");
                    obj.w = SVGOBJ.GetStringRealWidth(currentText, SVGFont) + SVGOBJ.OFFSET;
                }
                
                //String currentText = obj.text;
                boolean f1 = currentText.startsWith(termPrefix);
                boolean f2 = currentText.startsWith(facetPrefix);
                if (currentText.endsWith(PATTERN_FOR_MARKING_CYCLIC_NODES) == true) {
                    currentText = currentText.substring(0, currentText.lastIndexOf(PATTERN_FOR_MARKING_CYCLIC_NODES));
                }

                // Toraki requirement:
                // ενώ εμφανίζει τον μη προτιμώμενο, δεν δίνει τον προτιμώμενο (ίσως δεν έχει και νόημα εδώ η σύνδεση τότε).
                // Πολύκαρπος: εννοούν ότι από έναν όρο εμφανίζεται το to-value(s) του category 
                // (AAAHierarchyTerm->AAA_UF-> AAAUsedForTerm) (μη προτιμώμενο), ενώ όταν κάνουμε κλικ σε αυτό, 
                // δεν εμφανίζεται το αντίστοιχο from-value (προτιμώμενο). Είναι λογικό, διότι στο configuration 
                // των SVG γράφων έχει επιλεχθεί να εμφανίζεται αυτό το category μόνο με forward direction. 
                // π.χ. AAAEL`εμπορεύματα. Οπότε και θα μπορούσε να καταργηθεί το anchor σε όλα τα AAAUsedForTerms 
                // (ίσως δεν έχει και νόημα εδώ η σύνδεση τότε). 
                boolean objectIsUsedForTerm = (obj.style.compareTo("styleUF") == 0);
                if ((f1 == true || f2 == true) && objectIsUsedForTerm == false) {
                    //if (f1 == true || f2 == true) {

                    FileContents.append("<a xlink:href=\"javascript:followlink();\" onclick=\"GraphicalViewIconPressed('");
                    FileContents.append("/GraphicalView','");
                    String current;
                    if (f1) {
                        current = currentText.substring(currentText.indexOf(termPrefix) + termPrefix.length());
                    } else {
                        current = currentText.substring(currentText.indexOf(facetPrefix) + facetPrefix.length());
                    }
                    current.replaceAll("'", "\\'");
                    FileContents.append(URLEncoder.encode(current, "UTF-8").replaceAll("\\+", "%20"));
                    FileContents.append("','");
                    if (f1 == true) { // case of graph of a descriptor
                        FileContents.append("DESCRIPTOR");
                    }
                    if (f2 == true) { // case of graph of a Facet
                        // check the case of Facet / Hierarchy
                        if (GivenClassIsHierarchy(SessionUserInfo.selectedThesaurus, currentText, lang) == true) {
                            FileContents.append("HIERARCHY");
                        } else {
                            FileContents.append("FACET");
                        }
                    }

                    FileContents.append("','true','" + SessionUserInfo.userGroup + "','" + SessionUserInfo.selectedThesaurus + "','" + SessionUserInfo.SVG_CategoriesFrom_for_traverse + "','" + SessionUserInfo.SVG_CategoriesNames_for_traverse + "')\">");


                }

                FileContents.append("<rect" + " class=\"" + obj.style + "\"" + " x=\"" + obj.x + "\" y=\"" + obj.y + "\" width=\"" + obj.w + "\" height=\"" + obj.h + "\"></rect>\n");
                //FileContents += "<text" + " x=\"" + obj.tx + "\" y=\"" + obj.ty + "\" style=\"font-family:" + obj.fontfamily + "; fontsize:" + obj.fontsize + "; fill:" + obj.textfill + ";\">" + currentText + "</text>\n";
                FileContents.append("<text" + " x=\"" + obj.tx + "\" y=\"" + obj.ty + "\" font-family=\"" + obj.fontfamily + "\" font-size=\"" + obj.fontsize + "\" fill=\"" + obj.textfill + "\">" + ReplaceBadChars(currentText) + "</text>\n");

                // Toraki requirement:
                // ενώ εμφανίζει τον μη προτιμώμενο, δεν δίνει τον προτιμώμενο (ίσως δεν έχει και νόημα εδώ η σύνδεση τότε).
                // Πολύκαρπος: εννοούν ότι από έναν όρο εμφανίζεται το to-value(s) του category 
                // (AAAHierarchyTerm->AAA_UF-> AAAUsedForTerm) (μη προτιμώμενο), ενώ όταν κάνουμε κλικ σε αυτό, 
                // δεν εμφανίζεται το αντίστοιχο from-value (προτιμώμενο). Είναι λογικό, διότι στο configuration 
                // των SVG γράφων έχει επιλεχθεί να εμφανίζεται αυτό το category μόνο με forward direction. 
                // π.χ. AAAEL`εμπορεύματα. Οπότε και θα μπορούσε να καταργηθεί το anchor σε όλα τα AAAUsedForTerms 
                // (ίσως δεν έχει και νόημα εδώ η σύνδεση τότε).         
                if ((f1 == true || f2 == true) && objectIsUsedForTerm == false) {
                    //if (f1 == true || f2 == true) {
                    FileContents.append("</a>");
                }

                FileContents.append("</g>\n");
            }
            if (obj.type == 1) {
                FileContents.append("<g style=\"fill:" + obj.fill + "; stroke:" + obj.stroke + "\">\n");
                FileContents.append("<line" + " x1=\"" + obj.x + "\" y1=\"" + obj.y + "\" x2=\"" + obj.w + "\" y2=\"" + obj.h + "\"/>\n");
                FileContents.append("</g>\n");
            }
        }

        FileContents.append("</g>\n");


        if (navbarEnabled.equals("true")) {


            FileContents.append("<g id=\"group2\" transform = \"translate(0 0) scale(0.1)\">\n");
            FileContents.append("<rect x=\"0\" y=\"0\" style=\"fill:#E9E9C6; stroke:brown;\">\n");
            FileContents.append("</rect>");
            FileContents.append("</g>\n");



            FileContents.append("<g id=\"group3\" transform=\"translate(0 0) scale(0.1)\">\n");
            FileContents.append("<rect x=\"20\" y=\"20\" width=\"500px\" height=\"500px\" opacity=\"0.7\" stroke-width=\"2\" style=\"fill:cyan; stroke:black;\">\n");
            FileContents.append("</rect>\n");
            FileContents.append("</g>\n");
        // karam
        //FileContents += "<script  xmlns:xlink='http://www.w3.org/1999/xlink' type=\"text/ecmascript\" xlink:href=\"./SVGScripts.js\"/>\n";      
        // FileContents.append("<script  xmlns:xlink='http://www.w3.org/1999/xlink' type=\"text/ecmascript\" xlink:href=\"../SVGScripts.js\"/>\n"); 
        // FileContents.append("<script  xmlns:xlink='http://www.w3.org/1999/xlink' type=\"text/ecmascript\" xlink:href=\"../../Javascript/graphicalView.js\"/>\n"); 
        }

        SVGFooter += "</svg>\n";



        try {

            resultString += SVGHeader;
            resultString += FileContents.toString();
            resultString += SVGFooter;


        } catch (Exception e) {
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix + e.getMessage());
            Utils.StaticClass.handleException(e);
            resultString += "End of stream";
        }

    }

    protected String ReplaceGuideTermMarks(String theString) {
        // Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"ReplaceGuideTermMarks " + theString);
        //	theString = theString.replaceAll("@#","<");
        //  theString = theString.replaceAll("#@",">");
        return theString;
    }

    protected String ReplaceBadChars(String theString) {
        theString = theString.replaceAll("\u00A2", "¢");
        /*
        theString = theString.replaceAll("&","&quot");
        theString = theString.replaceAll("<","&lt;");
        theString = theString.replaceAll(">","&gt;");
        
         */
        theString = theString.replaceAll("\\\\", "\\\\");
        theString = theString.replaceAll("&", "&amp;");
        theString = theString.replaceAll("<", "&lt;");
        theString = theString.replaceAll(">", "&gt;");
        theString = theString.replaceAll("'", "&apos;");
        theString = theString.replaceAll("\"", "&quot;");
        //theString = neo4j_sisapi.GreekConverter.ISO72UniString(theString);


        return theString;
    }
}
