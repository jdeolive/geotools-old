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
 * @version $Id: DefaultStyle.java,v 1.5 2002/08/12 09:36:55 ianturton Exp $
 * @author James Macgill, CCG
 */
public class DefaultStyle implements org.geotools.styling.Style {

    FeatureTypeStyle[] featureTypeStyleList = new FeatureTypeStyle[0];
    
    /** Creates a new instance of DefaultStyle */
    public DefaultStyle() {
        featureTypeStyleList = new DefaultFeatureTypeStyle[1];
        featureTypeStyleList[0] = new DefaultFeatureTypeStyle();
        featureTypeStyleList[0].setFeatureTypeName("default");
        DefaultRule [] rules = new DefaultRule[1];
        rules[0] = new DefaultRule();
        Symbolizer[] symbolizers = new Symbolizer[3];
        symbolizers[0] = new DefaultPolygonSymbolizer();
        symbolizers[1] = new DefaultLineSymbolizer();
        symbolizers[2] = new DefaultPointSymbolizer();
        rules[0].setSymbolizers(symbolizers);
        ((DefaultFeatureTypeStyle)featureTypeStyleList[0]).setRules(rules);
    }

    public String getAbstract() {
        return "A crude implementation of a default style";
    }
    
    public FeatureTypeStyle[] getFeatureTypeStyles() {
       return featureTypeStyleList;
    }
    
    public void setFeatureTypeStyles(FeatureTypeStyle[] featureTypeStyles){
        featureTypeStyleList = featureTypeStyles;
    }
    
    public String getName() {
        return "default style";
    }
    
    public String getTitle() {
        return "default style";
    }
    
    public boolean isDefault() {
        return true;
    }
    
}
