/*
 * DefaultStroke.java
 * TODO:This is unfinised as it currently reutrns fixed values with no way to change them.
 * Created on April 11, 2002, 2:08 PM
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
public class DefaultStroke implements org.geotools.styling.Stroke {

    /** Creates a new instance of DefaultStroke */
    public DefaultStroke() {
    }
    
    /**
     * This parameter gives the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per primary-color component in the
     * order Red, Green, Blue, prefixed wih the hash (#) sign.  The hexidecimal digits between A and F
     * may be in either upper or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).  The default color is defined to be black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @return The color of the stroke encoded as a hexidecimal RGB value.
     */
    public String getColor() {
        return "#000000";
    }
    
    /**
     * This parameter encodes the dash pattern as a series of space separated floats.<br>
     * The first number gives the length in pixels of the dash to draw, the second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @return The dash pattern as a String of float values in the form "dashlength gaplength ..."
     */
    public String getDashArray() {
        return null;//HACK: is this an aceptable return, or should it be "1 0"
    }
    
    /**
     * This parameter encodes the dash pattern as a series of space separated floats.<br>
     * The first number gives the length in pixels of the dash to draw, the second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @param dasharray.  Dash pattern as a String of float values in the form "dashlength gaplength ..."
     */
    public double getDashOffset() {
        return 0;
    }
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be used and specifies the fill graphic to use.
     *
     * @return The graphic to use as a stipple fill.  If null, then no Stipple fill should be used.
     */
    public Graphic getGraphicFill() {
        return null;
    }
    
    /**
     * This parameter indicates that a repeated-linear-graphic graphic stroke type will be used and specifies the graphic to use.
     *
     * Proper stroking with a linear graphic requires two "hot-spot" points within the space of the graphic to indicate where the rendering line starts and stops.
     * In the case of raster images with no special mark-up, this line will be assumed to be the middle pixel row of the image, starting from the first pixel column and
     * ending at the last pixel column.
     *
     * @return The graphic to use as a linear graphic.  If null, then no graphic stroke should be used.
     */
    public Graphic getGraphicStroke() {
        return null;
    }
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @return The cap style.  This will be one of "butt", "round" and "square".  There is no defined default.
     */
    public String getLineCap() {
        return "butt";
    }
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @return The join style.  This will be one of "mitre", "round" and "bevel".  There is no defined default.
     */
    public String getLineJoin() {
        return "mitre";
    }
    
    /**
     * This specifies the level of translucency to use when rendering the stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with 0.0
     * representing totally transparent and 1.0 representing totally opaque.  A linear scale of
     * translucency is used for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value is 1.0 (opaque).
     *
     * @return The opacity of the stroke, where 0.0 is completely transparent and 1.0 is completely opaque.
     */
    public double getOpacity() {
        return 1.0;
    }
    
    /**
     * This parameter gives the absolute width (thickness) of a stroke in pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative numbers are not.
     *
     * @return The width of the stroke in pixels.  This may be fractional but not negative.
     */
    public double getWidth() {
        return 1;
    }
    
}
