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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;

/**
 * PolygonSymbolizer.
 * 
 * A symbolizer describes how a polygon feature should appear on a map.
 * The symbolizer describes not just the shape that should appear but also 
 * such graphical properties as color and opacity.
 * 
 * A symbolizer is obtained by specifying one of a small number of different
 * types of symbolizer and then supplying parameters to overide its
 * default behaviour.
 * 
 * The details of this object are taken from the OGC Styled-Layer Descriptor
 * Report (OGC 01-077) version 0.7.2.
 * Renderers can use this information when displaying styled features,
 * though it must be remembered that not all renderers will be able to
 * fully represent strokes as set out by this interface.  For example, opacity
 * may not be supported.
 *
 * The graphical parameters and their values are derived from SVG/CSS2 
 * standards with names and semantics which are as close as possible.<p>
 *
 * @version $Id: PolygonSymbolizer.java,v 1.5 2003/08/01 16:54:21 ianturton Exp $
 * @author James Macgill
 */
public interface PolygonSymbolizer extends Symbolizer{
    
    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry.
     *
     * Note that the area should be filled first before the outline 
     * is rendered.
     *
     * @return The Fill style to use when rendering the area.
     */
    Fill getFill();
    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry.
     *
     * Note that the area should be filled first before the outline 
     * is rendered.
     *
     * @param The Fill style to use when rendering the area.
     */
    void setFill(Fill fill);
    
    /**
     * Provides the graphical-symbolization parameter to use for the outline
     * of the Polygon.
     *
     * @return The Stroke style to use when rendering lines.
     */
    Stroke getStroke();
    /**
     * Provides the graphical-symbolization parameter to use for the outline
     * of the Polygon.
     *
     * @param The Stroke style to use when rendering lines.
     */
    void setStroke(Stroke stroke);
    
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     * 
     * Geometry types other than inherently area types can be used.  
     * If a line is used then the line string is closed for filling (only)
     * by connecting its end point to its start point.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an
     * XPath reference to a Geometry object, but does follow it 
     * in spirit.
     *
     * @return String The name of the attribute in the feature being styled 
     *  that should be used.  If null then the default geometry should be used.
     */
    String geometryPropertyName();
    void setGeometryPropertyName(String geometryPropertyName);
    void accept(StyleVisitor visitor);
}

