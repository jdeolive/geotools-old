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
 * @version $Id: DefaultStyle.java,v 1.3 2002/06/04 17:57:08 loxnard Exp $
 * @author James Macgill, CCG
 */
public class DefaultStyle implements org.geotools.styling.Style {

    FeatureTypeStyle[] featureTypeStyleList;
    
    /** Creates a new instance of DefaultStyle */
    public DefaultStyle() {
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
