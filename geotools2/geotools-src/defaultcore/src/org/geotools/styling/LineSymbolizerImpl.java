/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 */
package org.geotools.styling;

/**
 * @version $Id: LineSymbolizerImpl.java,v 1.7 2003/07/22 16:37:01 ianturton Exp $
 * @author James Macgill
 */
public class LineSymbolizerImpl implements org.geotools.styling.LineSymbolizer {
    private Stroke stroke = null;
    private String geometryName = null;

    /** Creates a new instance of DefaultLineSymbolizer */
    protected LineSymbolizerImpl() {
    }

    public int hashcode() {
        int key = 0;
        key = stroke.hashCode();
        key = (key * 13) + geometryName.hashCode();

        return key;
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently linear types can be used.
     * If a point geometry is used, it should be interpreted as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline should be used as the line string
     * (with no end caps).
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryName;
    }

    public void setGeometryPropertyName(String name) {
        geometryName = name;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the
     * linear geometry.
     *
     * @return The Stroke style to use when rendering lines.
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the graphical-symbolization parameter to use for the
     * linear geometry.
     *
     * @param stroke The Stroke style to use when rendering lines.
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
}