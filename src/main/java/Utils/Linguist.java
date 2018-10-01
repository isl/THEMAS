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

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Check,find and return the html language acronym.
 *
 * @version 1.0
 * @author Rinakakis Georgios
 */
public class Linguist {

    String errorBuffer = "";
    HashMap<String, String> newLanguages = new HashMap<String, String>();

    /**
     * Takes a text and checks about known languages, which included in AAT model.
     * Return the html acronyms of the language.
     * @param String variable with language for check and replace.
     * @return Html language acronyms.
     */
    public static String SupportedLanguages(String langCode) {

        if (langCode != null && langCode.trim().length() > 0) {
            String chooseLangCode = langCode.trim().toLowerCase();

            if (chooseLangCode.equals("bg")) { return "Bulgarian"; }
            if (chooseLangCode.equals("es")) { return "Spanish"; }
            if (chooseLangCode.equals("cs")) { return "Czech"; }
            if (chooseLangCode.equals("da")) { return "Danish"; }
            if (chooseLangCode.equals("de")) { return "German"; }
            if (chooseLangCode.equals("et")) { return "Estonian"; }
            if (chooseLangCode.equals("el")) { return "Greek"; }
            if (chooseLangCode.equals("en")) { return "English"; }
            if (chooseLangCode.equals("fr")) { return "French"; }
            if (chooseLangCode.equals("ga")) { return "Irish"; }
            if (chooseLangCode.equals("it")) { return "Italian"; }
            if (chooseLangCode.equals("la")) { return "Latin"; }
            if (chooseLangCode.equals("lv")) { return "Latvian"; }
            if (chooseLangCode.equals("lt")) { return "Lithuanian"; }
            if (chooseLangCode.equals("hu")) { return "Hungarian"; }
            if (chooseLangCode.equals("mt")) { return "Maltese"; }
            if (chooseLangCode.equals("nl")) { return "Dutch"; }
            if (chooseLangCode.equals("pl")) { return "Polish"; }
            if (chooseLangCode.equals("pt")) { return "Portuguese"; }
            if (chooseLangCode.equals("ro")) { return "Romanian"; }
            if (chooseLangCode.equals("sk")) { return "Slovak"; }
            if (chooseLangCode.equals("sl")) { return "Slovenian"; }
            if (chooseLangCode.equals("fi")) { return "Finnish"; }
            if (chooseLangCode.equals("sv")) { return "Swedish"; }
            if (chooseLangCode.equals("cn")) { return "Chinese"; }

            return "";
        } else {
            return "";
        }
    }

    /*

http://publications.europa.eu/code/en/en-370200.htm

http://www.loc.gov/standards/iso639-2/php/code_list.php
    
The language versions should appear in alphabetical order of the formal titles
     in their original written forms (the codes used are the ISO codes 639-1
     in force, alpha-2 code— also see the ISO website):

Bulgarian   bg
Spanish	    es
Czech	    cs
Danish	    da
German	    de
Estonian    et
Greek	    el
English	    en
French	    fr
Irish       ga
Italian	    it
Latvian	    lv
Lithuanian  lt
Hungarian   hu
Maltese	    mt
Dutch	    nl
Polish	    pl
Portuguese  pt
Romanian    ro
Slovak	    sk
Slovenian   sl
Finnish	    fi
Swedish	    sv
     */
    private static String[] acceptableTHEMASLangCodes = {"bg", "es", "cs", "da", "de", "et", "el", "en", "fr",
    "ga", "it", "la", "lv", "lt", "hu", "mt", "nl", "pl", "pt", "ro", "sk", "sl", "fi", "sv", "cn"};


    public static String SupportedTHEMASLangcodes(String langCode, boolean applyFilter){
        if(langCode==null || langCode.trim().length()==0){
            return "";
        }

        String comparelangCode = langCode.toLowerCase();
        if(comparelangCode.equals("en-us") || comparelangCode.equals("en-gb")){
            return "en";
        }

        if(applyFilter){
            for(int i=0; i< acceptableTHEMASLangCodes.length;i++){
                if(comparelangCode.equals(acceptableTHEMASLangCodes[i])){
                    return  comparelangCode;
                }
            }


            return "";
        }
        else{
            return comparelangCode;
        }
    }


    public static  String AATLanguageAcronyms(String temp) {

        /*http://publications.europa.eu/code/en/en-370200.htm European language attrs    */
        if (!temp.isEmpty()) {
            if (temp.contains("Bulgarian"))  { return "bg";}
            if (temp.contains("Spanish"))    { return "es";}
            if (temp.contains("Czech"))      { return "cs";}
            if (temp.contains("Danish"))     { return "da";}
            if (temp.contains("German"))     { return "de";}
            if (temp.contains("Estonian"))   { return "et";}
            if (temp.contains("Greek"))      { return "el";}
            if (temp.contains("English"))    { return "en";}
            if (temp.contains("French"))     { return "fr";}
            if (temp.contains("Irish"))      { return "ga";}
            if (temp.contains("Italian"))    { return "it";}
            if (temp.contains("Latvian"))    { return "lv";}
            if (temp.contains("Lithuanian")) { return "lt";}
            if (temp.contains("Hungarian"))  { return "hu";}
            if (temp.contains("Maltese"))    { return "mt";}
            if (temp.contains("Dutch"))      { return "nl";}
            if (temp.contains("Polish"))     { return "pl";}
            if (temp.contains("Portuguese")) { return "pt";}
            if (temp.contains("Romanian"))   { return "ro";}
            if (temp.contains("Slovak"))     { return "sk";}
            if (temp.contains("Slovenian"))  { return "sl";}
            if (temp.contains("Finnish"))    { return "fi";}
            if (temp.contains("Swedish"))    { return "sv";}
            if (temp.contains("Chinese"))    { return "cn";}
            if (temp.contains("Latin"))    { return "la";}
             /*else {
                newLanguages.put(temp, "");
                temp = "";
            }

*/
        }

        return "";

    }

    /**
     * Returns string variable which contains the new languages find in AAT file,
     * along with their sum.
     * @return String variable with errors and warnings.
     */
    protected String getErrors() {
        if (newLanguages.size() > 0) {
            errorBuffer += "\n\nWarning (Report) : Found " + newLanguages.size() + " new languages.\n{\nLanguages:"; //check if exist new writings at list.
            for (int i = 0; i < newLanguages.size(); i++) {
                errorBuffer += " ~ " + newLanguages.keySet().toArray()[i].toString();
            }
            errorBuffer += "\n}";
        }
        return errorBuffer;
    }
}
