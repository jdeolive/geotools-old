/*
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
 *     UNITED KINDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;

/**
 * The Fill object encapuslates the graphical-symbolization parameters for areas of geometries.<br>
 *
 * There are two types of fill: solid-color, and repeated graphic fill.
 *
 * The details of this object are taken from the OGC Styled-Layer Descriptor Report (OGC 01-077) version 0.7.2
 * Renderers can use this infomration when displaying styled features, though it must be remembered that not all renderers will be able to
 * fully represent strokes as set out by this interface, for example opacity may not be supported.
 *
 * The graphical parameters and their values are derived from SVG/CSS2 standards with names and semantics
 * which are as close as possible.<p>
 * 
 * @author  jamesm
 */
public interface Fill {
    
    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per primary-color component, in the
     * order Red, Green, Blue, prifixed wih the hash (#) sign.  The hexidecimal digits beetween A and F
     * may be in either uppoer of lower case.  For example full red is encoded as "#ff0000" (with no
     * quotation marks).  The default color is defined to be 50% gray ("#808080").
     *
     * Note: in CSS this parameter is just called Fill and not Color
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     **/
    public String getColor();
    
    
    /**
     * This specifies the level of translucency to use when rendering the fill.
     * <br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally opaque,
     * with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value is 1.0 (opaque)
     *
     * @return The opacity of the fill, where 0.0 is completly transparent
     * and 1.0 is completly opaque.
     */
    public double getOpacity();
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be 
     * used and specifies the fill graphic to use.
     * 
     * @return The graphic to use as a stipple fill.  If null then no Stipple
     * fill should be used.
     */
    public Graphic getGraphicFill();
}


