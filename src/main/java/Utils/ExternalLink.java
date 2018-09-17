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

/**
 *
 * @author Elias
 */
public class ExternalLink {
    
    public String linkUri;
    public String linkLabel;
    public String matchType;
    public String vocabularyIdentifier;
    
    public ExternalLink() {
        linkUri = "";
        linkLabel = "";
        matchType ="";
        vocabularyIdentifier = "";
    }
    
    public ExternalLink(String uri) {
        this();
        linkUri = uri==null?"":uri;        
    }
    
    public ExternalLink(String uri, String matchTypeVal) {
        this();
        linkUri = uri==null?"":uri;   
        matchType = matchTypeVal==null?"":matchTypeVal;   
    }
    
    public ExternalLink(String uri,String label, String matchTypeVal, String vocabId) {
        linkUri = uri==null?"":uri;   
        linkLabel = label==null?"":label;   
        matchType = matchTypeVal==null?"":matchTypeVal;   
        vocabularyIdentifier = vocabId==null?"":vocabId;   
    }
    
}