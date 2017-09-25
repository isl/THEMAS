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
package Utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
/**
 *
 * @author tzortzak
 */
public class TaxonomicCodeItem {
    
    public Vector<String> codeParts;
    public String nodeName;
    public String nodeNameTransliteration;
    
    
    public TaxonomicCodeItem(String name, String nodeNameTranslit) {
                
        codeParts =new Vector<String>();
        codeParts.add("");
        nodeName = name;
        nodeNameTransliteration = nodeNameTranslit;
        
        String name_split[] = name.split("`");
        
        if(name_split.length >1 ){
            nodeName = name_split[1];
        }
        
    }

    public TaxonomicCodeItem(String constrCode,String name, String nodeNameTranslit) throws NumberFormatException {
        
        
        String term_split[] = name.split("`");
        codeParts =new Vector<String>();
        
        if(term_split.length ==2 ){
            //String codeStr = term_split[1];
            
            constrCode = constrCode.trim();
            if(constrCode.length() != 0){

                String [] codeStrParts = constrCode.split("\\.");
                
                    try{
                        
                        for(int i=0; i< codeStrParts.length; i++){
                            
                            //if this subStr is not a valid integer representing string then exception is thrown 
                            Integer.parseInt(codeStrParts[i]);
                            
                            //First Code Part Should Consist of 3 digits with '0' as filler
                            if(i==0){
                                int initialCategoryLength = codeStrParts[i].length();
                                String initialCategoryStr = "";
                                
                                for(int m=0;m<3-initialCategoryLength;m++)
                                    initialCategoryStr += "0";
                                
                                initialCategoryStr += codeStrParts[i] ;
                                codeParts.add(initialCategoryStr);
                            }
                            else
                                codeParts.add(codeStrParts[i]);
                        }
                    }
                    catch(NumberFormatException e){
                        //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+name + " caused exception due to Dewey Number: " + codeStr);
                        codeParts.clear();
                        codeParts.add(" ");
                        Utils.StaticClass.handleException(e);
                    }

            }
    
            
        }
        else{
            codeParts.add("");
        }
            
            
        nodeName = name;
        nodeNameTransliteration = nodeNameTranslit;
        String name_split[] = name.split("`");
        
        if(name_split.length >1 ){
    
            nodeName = name_split[1];
        }
        
        
        
    }
    
    public String getTaxCode(){
        if(codeParts ==null || codeParts.size()==0){
            return null;
        }
        String taxCode="";
        for(int i=0; i< codeParts.size(); i++){
            taxCode+=codeParts.get(i);
            if(i<codeParts.size()-1){
                taxCode+=".";
            }
        }
        
        return taxCode;
    }

}
