/*
 * DefaultRule.java
 *
 * Created on April 12, 2002, 2:49 PM
 */

package org.geotools.styling;

/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 *
 * @author jamesm
 */
public class DefaultRule implements org.geotools.styling.Rule {

    /** Creates a new instance of DefaultRule */
    public DefaultRule() {
    }

    public Graphic[] getLegendGraphic() {
        return null;//TODO: implement a proper return here
    }
    
    public double getMaxScaleDenominator() {
        return Double.MAX_VALUE;//HACK: not nice this
    }
    
    public double getMinScaleDenominator() {
        return Double.MIN_VALUE;//HACK: not nice this
    }
    
    public Symbolizer[] getSymbolizers() {
        return new Symbolizer[]{new DefaultPolygonSymbolizer(),new DefaultLineSymbolizer()};   
    }
    
}
