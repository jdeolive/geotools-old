/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.geotools.styling;

/**
 * A style object is quite hard to set up, involving fills, strokes,
 * symbolizers and rules.
 * @version $Id: BasicPolygonStyle.java,v 1.2 2002/08/09 18:16:08 jmacgill Exp $
 * @author James Macgill, CCG
 */
public class BasicPolygonStyle extends DefaultStyle implements org.geotools.styling.Style {

    FeatureTypeStyle[] featureTypeStyleList; 
    
    /** Creates a new instance of BasicPolygonStyle */
    public BasicPolygonStyle() {
         this(null,null);
    }
    
     public BasicPolygonStyle(Fill fill, Stroke stroke) {
         DefaultPolygonSymbolizer polysym = new DefaultPolygonSymbolizer();
         polysym.setFill(fill);
         polysym.setStroke(stroke);
         DefaultRule rule = new DefaultRule();
         rule.setSymbolizers(new Symbolizer[]{polysym});
         DefaultFeatureTypeStyle fts = new DefaultFeatureTypeStyle();
         fts.setRules(new Rule[]{rule});
         this.setFeatureTypeStyles(setFeatureTypeStyles(new FeatureTypeStyle[]{fts}));
    }

    public String getAbstract() {
        return "A simple polygon style";
    }
    
   
    
    public String getName() {
        return "default polygon style";
    }
    
    public String getTitle() {
        return "default polygon style";
    }
   
}
