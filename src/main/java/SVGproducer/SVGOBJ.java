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

import java.awt.Font;
import java.awt.font.FontRenderContext;

/*-------------------------------------------------------------------
  Class SVGOBJ:
  It provides formatting information for the produced SVG graph...
  Used by ProduceHierarchies_common
 --------------------------------------------------------------------*/
public class SVGOBJ extends Object {
  public int type; //0=rectangular, 1=line
  public double x,  y,  w,  h;
  public double tx, ty;
  public String text, url, style;
  public String fill, textfill, stroke;
  public String fontfamily, fontsize;
  final static int OFFSET = 10;

  // rectangle
  public SVGOBJ(double X, double Y, String TEXT, String URLL, String STYLE, Font SVGFont) {
    super();
    type        = 0;
    fill        = new String("#E9E9C6");
    stroke      = new String("brown");
    fontfamily  = SVGFont.getFamily();
    fontsize    = new Integer(SVGFont.getSize()).toString(); // new String(SVGFont.getSize());
    textfill    = new String("black");
    text        = new String(TEXT);
    url         = new String(URLL);
    style         = new String(STYLE);
    x           = X;
    y           = 20 * Y;
    //w           = 8 * TEXT.length(); 
    // karam bug fix: calculate the width of the rectangle without the prefix
    String textWithoutPrefix = TEXT.substring(TEXT.indexOf("`") + 1);
    //w           = 8 * textWithoutPrefix.length(); 

    w = GetStringRealWidth(textWithoutPrefix, SVGFont) + OFFSET;

    h           = 15;
    //tx          = x + 1;
    //ty          = y + 9;
    tx          = x + 3;
    ty          = y + 11;    
  }

  // line
  public SVGOBJ(double X, double Y, double X2, double Y2) {
    super();
    type        = 1;
    fill        = new String("none");
    stroke      = new String("brown");
    //x           = 8 * X;
    x           = X + OFFSET;
    y           = 20 * Y + 15/2;
    //w           = 8 * X2;
    w           = X2;
    h           = 20 * Y2 + 15/2;
    fontfamily  = new String("");
    fontsize    = new String("");
    textfill    = new String("");
    text        = new String("");
    url         = new String("");
    style       = new String("");
    tx          = 0;
    ty          = 0;
  }
  
  /*-----------------------------------------------------------------------
                    GetStringRealWidth()
  -------------------------------------------------------------------------*/
  protected static double GetStringRealWidth(String str, Font SVGFont) {
    //Font SVGFont = new Font("Arial", Font.PLAIN, 12);
    // depricated
    // double w = Toolkit.getDefaultToolkit().getFontMetrics(SVGFont).stringWidth(str);
    
    FontRenderContext fr = new FontRenderContext(null, true, true);
    double w = SVGFont.getStringBounds(str, fr).getWidth();
   
    return w;
  }  
}

