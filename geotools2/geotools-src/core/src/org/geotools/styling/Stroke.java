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
 * The Stroke object encapsulates the graphical-symbolization parameters for linear geometries.<br>
 *
 * There are three basic types of stroke: solid color, graphic fill (stipple),
 * and repeated linear graphic stroke.
 * A repeated linear graphic is plotted linearly and has its graphic symbol
 * bent around the curves of the line string.  A GraphicFill has the pixels
 * of the line rendered with a repeating area-fill pattern.<p>
 * If neither a graphic fill nor graphic stroke element are given, then the
 * line symbolizer should render a solid color.
 *
 * The details of this object are taken from the OGC Styled-Layer
 * Descriptor Report (OGC 01-077) version 0.7.2.
 * Renderers can use this information when displaying styled features,
 * though it must be remembered that not all renderers will be able to
 * fully represent details as set out by this interface: for example,
 * opacity may not be supported.
 *
 * The graphical parameters and their values are derived from SVG/CSS2
 * standards with names and semantics which are as close as possible.<p>
 *
 * @author James Macgill
 * @version $Revision: 1.5 $ $Date: 2002/05/27 09:07:40 $
 */
public interface Stroke {
    
    /**
     * This parameter gives the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per 
     * primary-color component in the order Red, Green, Blue, prefixed wih
     * the hash (#) sign.  The hexidecimal digits between A and F may be in
     * either upper or lower case.  For example, full red is encoded as
     * "#ff0000" (with no quotation marks).
     * The default color is defined to be black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @return The color of the stroke encoded as a hexidecimal RGB value.
     **/
    public String getColor();
    
    /**
     * This parameter gives the absolute width (thickness) of a stroke in
     * pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative
     * numbers are not.
     *
     * @return The width of the stroke in pixels.  
     *         This may be fractional but not negative.
     **/
    public double getWidth();
    
    /**
     * This specifies the level of translucency to use when rendering the
     * stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and
     * 1.0 with 0.0 representing totally transparent and 1.0 representing
     * totally opaque.  A linear scale of translucency is used for intermediate
     * values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value
     * is 1.0 (opaque).
     *
     * @return The opacity of the stroke, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public double getOpacity();
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @return The join style.  This will be one of "mitre", "round" and
     *         "bevel".  There is no defined default.
     */
    public String getLineJoin();
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @return The cap style.  This will be one of "butt", "round" and
     *         "square".  There is no defined default.
     */
    public String getLineCap();
    
    /**
     * This parameter encodes the dash pattern as a seqeuence of floats.<br>
     * The first number gives the length in pixels of the dash to draw, the
     * second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by
     * repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;
     * --&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @return The dash pattern as an array of float values in the form
     *         "dashlength gaplength ..."
     */
    public float[] getDashArray();
    
   /**
    * A dash array need not start from the begining, this method allows for
    * an offset into the dash array before starting it.
    *
    * @return The distance, in pixels, that any dash array should start from.
    */
    public double getDashOffset();
    
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     * 
     * @return The graphic to use as a stipple fill.  
     *         If null, then no Stipple fill should be used.
     */
    public Graphic getGraphicFill();
    
    /**
     * This parameter indicates that a repeated-linear-graphic graphic stroke 
     * type will be used and specifies the graphic to use.
     *  
     * Proper stroking with a linear graphic requires two "hot-spot" points 
     * within the space of the graphic to indicate where the rendering line
     * starts and stops.
     * In the case of raster images with no special mark-up, this line will
     * be assumed to be the middle pixel row of the image, starting from the
     * first pixel column and ending at the last pixel column.
     *
     * @return The graphic to use as a linear graphic.
     *         If null, then no graphic stroke should be used.
     */
    public Graphic getGraphicStroke();

}

/*
 * $Log: Stroke.java,v $
 * Revision 1.5  2002/05/27 09:07:40  jmacgill
 * fixed a number of checkstyle errors
 * improved javadoc comments
 *
 * Revision 1.4  2002/05/01 16:51:53  jmacgill
 * dash array is now returned as a float array and not a string of space seperated floats
 *
 * Revision 1.3  2002/03/28 10:54:10  jmacgill
 * work in progress
 *
 * Revision 1.2  2002/03/25 22:14:41  jmacgill
 * Updated JavaDocs
 *
 */