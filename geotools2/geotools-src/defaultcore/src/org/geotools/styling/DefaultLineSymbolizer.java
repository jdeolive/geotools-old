/*
 * DefaultLineSymbolizer.java
 *
 * Created on April 11, 2002, 2:13 PM
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
public class DefaultLineSymbolizer implements org.geotools.styling.LineSymbolizer {
    private Stroke stroke = new DefaultStroke();
    
    /** Creates a new instance of DefaultLineSymbolizer */
    public DefaultLineSymbolizer() {
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently linear types can be used.
     * If a point geometry is used, it should be interprited as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline should be used as the line string
     * (with no end caps).
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled typicaly features only have one geometry so in
     * general the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference is given to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return null;
    }
    
    /**
     * Provides the graphical-symbolization parameter to use for the
     * liniar geometry.
     *
     * @return The Stroke style to use when rendering lines.
     */
    public Stroke getStroke() {
        return stroke;
    }
    
    /**
     * Sets the graphical-symbolization parameter to use for the
     * liniar geometry.
     *
     * @param s The Stroke style to use when rendering lines.
     */
    public void setStroke(Stroke s) {
        stroke = s;
    }
    
}
