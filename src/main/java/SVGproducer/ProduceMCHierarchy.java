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
package SVGproducer;

import Users.DBFilters;
import DB_Classes.DBThesaurusReferences;
import Users.UserInfoClass;
import Utils.ConstantParameters;
import Utils.Parameters;
import neo4j_sisapi.*;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.text.*;
import java.nio.charset.*;
import java.nio.*;
import javax.servlet.http.HttpSession;

/*-------------------------------------------------------------------
  class ProduceMCHierarchy:
  Used to produce a MC hierarchy. It inherits all availlable methods from
  superclass ProduceHierarchies_common.
 --------------------------------------------------------------------*/
public final class ProduceMCHierarchy extends ProduceHierarchies_common {

        private int btCategoryIndex;

	public ProduceMCHierarchy(ReadSVGConfig conf) {

		super();
		I = conf;
                btCategoryIndex = -1;                
		navbarEnabled = I.navbar_enabled;
		theScope = I.hierarchy_scope;
                for(int i=0; i< I.sis_category_style.length; i++){
                    String value = I.sis_category_style[i];
                    if(value.compareTo("styleBT")==0){
                       btCategoryIndex = i;
                       break;
                    }
                }
	}
  
    /*-----------------------------------------------------------------------
                              GetHierarchySet()
    -------------------------------------------------------------------------
    FUNCTION: - gets in Vectors V1 and V2 the NTs of the given "hierarchy" parameter
    CALLED_BY: doJob() of abstract superclass ProduceHierarchies_common
    -------------------------------------------------------------------------*/
    protected int GetHierarchySet(UserInfoClass SessionUserInfo, String hierarchy, String lang, String style) {
        int SISApiSession = sis_session.getValue();
        
        // special handling for RT links
        // looking for EKT_RT
        
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        StringObject thesRT = new StringObject();
        dbtr.getThesaurusCategory_RT(SessionUserInfo.selectedThesaurus,Q,sis_session.getValue(),thesRT);
        boolean RTlinksIncludedInSVGgraph = false;
        String RT_class_name = "";
        String RT_category_name = "";
        String RT_direction = "";
        String RT_style = "";
        String RT_category_lang = "";
        
    
        //CategorySet[] categs= new CategorySet[I.sis_class_name.length + 1];
        CategorySet[] initial_categs= new CategorySet[I.sis_class_name.length];
        Q.free_all_sets();
        Q.reset_name_scope();
        int numberOfCategoriesToBeTraversed = 0;
        for (int i=0; i < I.sis_class_name.length; i++) {
            // in case the category EKT_RT is added to be displayed in SVG graph
            // do NOT traverse it because it causes cycles in the graph (special handling for them)
            
            if (I.sis_category_name[i].compareTo(thesRT.getValue()) == 0) {
                RTlinksIncludedInSVGgraph = true;
                RT_class_name = I.sis_class_name[i];
                RT_category_name = I.sis_category_name[i];
                RT_direction = I.sis_category_direction[i];
                RT_style = I.sis_category_style[i];
                RT_category_lang = I.sis_category_lang[i];
                continue;
            }
            
            String dir = I.sis_category_direction[i];
            QClass.Traversal_Direction direction;
            if (dir.equals("forw")) {
                direction = QClass.Traversal_Direction.FORWARD;
            } 
            else {
                direction = QClass.Traversal_Direction.BACKWARD;
            }
            CategorySet csobj = new CategorySet(I.sis_class_name[i], I.sis_category_name[i], direction);
            initial_categs[numberOfCategoriesToBeTraversed] = csobj;
            numberOfCategoriesToBeTraversed++;
        }
        
        
        //CategorySet end_csobj = new CategorySet("end","end", QClass.Traversal_Direction.FORWARD);
        //numberOfCategoriesToBeTraversed;
        //categs[numberOfCategoriesToBeTraversed] = end_csobj;
        int numberOfFinalCategs = 0;
        for(CategorySet cat: initial_categs){
            if(cat!=null){
                numberOfFinalCategs++;
            }
        }
        CategorySet[] categs= new CategorySet[numberOfFinalCategs];
        numberOfFinalCategs = 0;
        for(CategorySet cat: initial_categs){
            if(cat!=null){
                categs[numberOfFinalCategs] = cat;
                numberOfFinalCategs++;
            }
        }
        
        Q.set_categories(categs);

        strobj.setValue(hierarchy);
        if (Q.set_current_node(strobj)!= QClass.APIFail) {
            ret_set = Q.get_traverse_by_category(0, QClass.Traversal_Isa.NOISA);
            
            // FILTER bt links set depending on user group
            DBFilters dbf = new DBFilters();
            ret_set = dbf.FilterBTLinksSet(SessionUserInfo, ret_set, Q, sis_session);
       
            // test (trying to add RT links) - BEGIN
            
            if (RTlinksIncludedInSVGgraph == true) {
                IncludeInSVG_BidirectionalCategory(ConstantParameters.rt_kwd, ret_set, RT_class_name, RT_category_name, RT_direction, RT_style, RT_category_lang, lang);
            }
            
            // test (trying to add RT links) - END

            Q.reset_set( ret_set);
            // while (Q.retur_link(ret_set, cls, label, cmv) != Q.APIFail)
            //StringObject from_cls, categ;
            //IntegerObject uniq, tr;
            //from_cls = new StringObject();
            //categ = new StringObject();
            //uniq = new IntegerObject();
            //tr = new IntegerObject();
            Vector<Return_Full_Link_Row> retFLVals = new Vector<Return_Full_Link_Row>();
            if(Q.bulk_return_full_link(ret_set, retFLVals)!=QClass.APIFail){
                for(Return_Full_Link_Row row:retFLVals){
                    //while (Q.retur_full_link(ret_set, cls, label, categ, from_cls, cmv, uniq, tr) != QClass.APIFail) {
                    int direction = GetCategoryDirection(row.get_v4_fromcls(), row.get_v3_categ());
                    String categStyle = GetCategoryStyle(row.get_v4_fromcls(), row.get_v3_categ());
                    V3.add(categStyle);

                    String categoryLang = GetCategoryLanguage(row.get_v4_fromcls(), row.get_v3_categ());
                    String fromValue = ConvertLatinToUnicode(row.get_v1_cls(), lang);
                    String toValue = ConvertLatinToUnicode(row.get_v5_cmv().getString(), categoryLang);

                    if (direction == QClass.FORWARD) {
                        V1.add(toValue);
                        V2.add(fromValue);
                    }
                    else {
                        V1.add(fromValue);
                        V2.add(toValue);
                    }
                }
            }
            /*
            while (Q.retur_full_link(ret_set, cls, label, categ, from_cls, cmv, uniq, tr) != QClass.APIFail) {
                int direction = GetCategoryDirection(from_cls.getValue(), categ.getValue());
                String categStyle = GetCategoryStyle(from_cls.getValue(), categ.getValue());
                V3.add(categStyle);

                String categoryLang = GetCategoryLanguage(from_cls.getValue(), categ.getValue());
                String fromValue = ConvertLatinToUnicode(cls.getValue(), lang);
                String toValue = ConvertLatinToUnicode(cmv.getString(), categoryLang);

                if (direction == QClass.FORWARD) {
                    V1.add(toValue);
                    V2.add(fromValue);
                }
                else {
                    V1.add(fromValue);
                    V2.add(toValue);
                }
            }
            */
            Q.free_set(ret_set);
        }
        else {
            String UnicodeHier = ConvertLatinToUnicode(hierarchy, lang);
            svgNodeNotFoundErrorMsg(UnicodeHier);
            return -1;
        }
    
        /*
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"---------------------- MarkCyclicPaths (begin) ---------------------------------");
        MarkCyclicPaths(ConvertLatinToUnicode(hierarchy, lang), new StringBuffer(""));
        Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"---------------------- MarkCyclicPaths (end) ---------------------------------");
        */
        return 0;
    }
  
    /*-----------------------------------------------------------------------
                              IncludeInSVGCyclicCategory()
    -------------------------------------------------------------------------     
    FREEZED: the concatenation of ___ + categoryKeyword needs MUCH job for the 
             calculation of the SVG rectangles widths !!!!!!!!
    -------------------------------------------------------------------------*/
    void IncludeInSVG_BidirectionalCategory(String categoryKeyword, int setWithTraversedLinks, String class_name, String category_name, String direction, String style, String category_lang, String lang) {
       int SISApiSession = sis_session.getValue();
       Q.reset_set( setWithTraversedLinks);
       //int fromValuesSet = Q.get_from_value( 0);
       //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"fromValuesSet card = " + Q.set_get_card( fromValuesSet));
       //int toValuesSet = Q.get_to_value( 0);
       //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"toValuesSet card = " + Q.set_get_card( toValuesSet));
       //Q.set_union( fromValuesSet, toValuesSet);
       //int allValuesSet = fromValuesSet;
       //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"allValuesSet card = " + Q.set_get_card( allValuesSet));
       //Q.reset_set( allValuesSet);
       //int RT_linksSet = Q.get_link_from_by_category( allValuesSet, new StringObject("AAAHierarchyTerm"), new StringObject("AAA_RT"));
       int RT_linksSetForw = Q.get_link_from_by_category( 0, new StringObject(class_name), new StringObject(category_name));
       int RT_linksSetBack = Q.get_link_to_by_category( 0, new StringObject(class_name), new StringObject(category_name));
       //Q.free_set( fromValuesSet);
       //Q.free_set( allValuesSet);
       Q.reset_set( RT_linksSetForw);
       Q.reset_set( RT_linksSetBack);
       //Q.set_union(RT_linksSetForw, RT_linksSetBack);
       //Q.free_set(RT_linksSetBack);
       Vector<Return_Link_Row> retVals = new Vector<Return_Link_Row>();
       if(Q.bulk_return_link(RT_linksSetForw, retVals)!=QClass.APIFail){
           for(Return_Link_Row row:retVals){
               //while (Q.retur_link( RT_linksSet, cls, label, cmv) != Q.APIFail) {
               V3.add(style); 
                String fromValue = ConvertLatinToUnicode(row.get_v1_cls(), lang);
                String toValue = ConvertLatinToUnicode(row.get_v3_cmv().getString(), category_lang);
                //V1.add(toValue + "___" + categoryKeyword); // this needs MORE job for the calculation of the SVG rectangles widths !!!!!!!!
                V1.add(toValue + PATTERN_FOR_MARKING_CYCLIC_NODES);
                V2.add(fromValue);
           }
       }
       retVals.clear();
       if(Q.bulk_return_link(RT_linksSetBack, retVals)!=QClass.APIFail){
           for(Return_Link_Row row:retVals){
               //while (Q.retur_link( RT_linksSet, cls, label, cmv) != Q.APIFail) {
               V3.add(style); 
                String fromValue = ConvertLatinToUnicode(row.get_v3_cmv().getString(), category_lang);
                String toValue = ConvertLatinToUnicode(row.get_v1_cls(), lang);
                //V1.add(toValue + "___" + categoryKeyword); // this needs MORE job for the calculation of the SVG rectangles widths !!!!!!!!
                V1.add(toValue + PATTERN_FOR_MARKING_CYCLIC_NODES);
                V2.add(fromValue);
           }
       }
       /*
       while (Q.retur_link( RT_linksSet, cls, label, cmv) != Q.APIFail) {
            V3.add(style); 
            String fromValue = ConvertLatinToUnicode(cls.getValue(), lang);
            String toValue = ConvertLatinToUnicode(cmv.getString(), category_lang);
            //V1.add(toValue + "___" + categoryKeyword); // this needs MORE job for the calculation of the SVG rectangles widths !!!!!!!!
            V1.add(toValue + PATTERN_FOR_MARKING_CYCLIC_NODES);
            V2.add(fromValue);
        }
       */
       Q.free_set( RT_linksSetBack);
       Q.free_set( RT_linksSetForw);        
    }  
    
    /*-----------------------------------------------------------------------
                              MarkCyclicPaths()
    -------------------------------------------------------------------------     
    FREEZED: an old try to prevent cyclic paths in SVG graphs (g.e. for RT links)
             TIME CONSUMING!!!!
    -------------------------------------------------------------------------*/
    void MarkCyclicPaths(String currentRoot, StringBuffer currentPath) {
        boolean endOfPath = true;
        for (int i=0; i < V2.size(); i++) {
            String fromValue = V2.elementAt(i).toString();
            String toValue = V1.elementAt(i).toString();
            if (fromValue.compareTo(currentRoot) == 0) {
                endOfPath = false;
                if (currentPath.toString().indexOf(toValue + "###") != -1) {
                    Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"Cycle for path: " + currentPath.toString() + "###" + fromValue + "###" + toValue);
                    // concat to V1[i] the value "_cycle detected"
                    V1.set(i, toValue + " (cycle detected)");
                }
                else {
                    StringBuffer newPath = new StringBuffer(currentPath);
                    newPath.append(fromValue + "###");
                    MarkCyclicPaths(toValue, newPath);
                }
            }
        }
        if (endOfPath == true) {
            //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"New path:" + currentPath.toString() + "###" + currentRoot); 
        }
    }  

  /*-----------------------------------------------------------------------
                              GetCategoryDirection()
  -------------------------------------------------------------------------*/
  protected int GetCategoryDirection(String fromCateg, String categName)
  {
    for(int i=0; i < I.sis_class_name.length; i++) {
      if (fromCateg.equals(I.sis_class_name[i]) && categName.equals(I.sis_category_name[i])) {
        String dir = I.sis_category_direction[i];
        int direction;
        if (dir.equals("forw")) {
           return QClass.FORWARD;
        }
        else {
           return QClass.BACKWARD;
        }
      }
    }
    //at this point category was not found
    //check if category was a guide Term that has the prefix AAA_BT
    if(btCategoryIndex>=0 && categName.startsWith(I.sis_category_name[btCategoryIndex])){
        String dir = I.sis_category_direction[btCategoryIndex];
        int direction;
        if (dir.equals("forw")) {
           return QClass.FORWARD;
        }
        else {
           return QClass.BACKWARD;
        }
    }
    
    return QClass.FORWARD;
  }

  /*-----------------------------------------------------------------------
                              GetCategoryStyle()
  -------------------------------------------------------------------------*/
  protected String GetCategoryStyle(String fromCateg, String categName)
  {
    for(int i=0; i < I.sis_class_name.length; i++) {
      if (fromCateg.equals(I.sis_class_name[i]) && categName.equals(I.sis_category_name[i])) {
        return I.sis_category_style[i];
      }
    }
    //at this point category was not found
    //check if category was a guide Term that has the prefix AAA_BT
    if(btCategoryIndex>=0 && categName.startsWith(I.sis_category_name[btCategoryIndex])){
        return I.sis_category_style[btCategoryIndex];
    }
    if(categName.contains("uf_translation")){
        return "styleUFTranslations";
    }
    return "styleTR";
  }


  /*-----------------------------------------------------------------------
                              GetCategoryLanguage()
  -------------------------------------------------------------------------*/
  protected String GetCategoryLanguage(String fromCateg, String categName)
  {
    for(int i=0; i < I.sis_class_name.length; i++) {
      if (fromCateg.equals(I.sis_class_name[i]) && categName.equals(I.sis_category_name[i])) {
        return I.sis_category_lang[i];
      }
    }
    //at this point category was not found
    //check if category was a guide Term that has the prefix AAA_BT
    if(btCategoryIndex>=0 && categName.startsWith(I.sis_category_name[btCategoryIndex])){
        return I.sis_category_lang[btCategoryIndex];
    }
    return "TR";
  }

}
