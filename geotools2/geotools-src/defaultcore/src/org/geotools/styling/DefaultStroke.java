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
    private String color = "#000000";
    private float[] dashArray = null;//HACK: is this an aceptable return? 
    private float dashOffset = 0;
    private Graphic fillGraphic = null;
    private Graphic strokeGraphic = null;
    private String lineCap = "butt";
    private String lineJoin = "miter";
    private double opacity = 1;
    private double width = 1;
    
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
        return color;
    }
    
    /**
     * This parameter sets the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per primary-color component in the
     * order Red, Green, Blue, prefixed wih the hash (#) sign.  The hexidecimal digits between A and F
     * may be in either upper or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).  The default color is defined to be black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @param c The color of the stroke encoded as a hexidecimal RGB value.
     */
    public void setColor(String c) {
        color = c;
    }
    
    /**
     * This parameter encodes the dash pattern as a series of floats.<br>
     * The first number gives the length in pixels of the dash to draw, the second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @return The dash pattern as an array of float values in the form "dashlength gaplength ..."
     */
    public float[] getDashArray() {
        if(dashArray == null) return new float[0];
        float[] newArray = new float[dashArray.length];
        System.arraycopy(dashArray,0,newArray,0,dashArray.length);
        return newArray;
    }
    
    /**
     * This parameter encodes the dash pattern as a series of floats.<br>
     * The first number gives the length in pixels of the dash to draw, the
     * second gives the amount of space to leave, and this pattern repeats.<br>
     * If an odd number of values is given, then the pattern is expanded by
     * repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;
     * --&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;
     * ---&nbsp;&nbsp;--</code>
     *
     * @param dashPattern The dash pattern as an array of float values in the form
     * "dashlength gaplength ..."
     */
    public void setDashArray(float[] dashPattern) {
        dashArray = dashPattern;
    }
    
    /**
     * This param determins where the dash pattern should start from. 
     * @param offset The distance into the dash pattern that should act as the start.
     */
    public double getDashOffset() {
        return dashOffset;
    }
    
    /**
     * This param determins where the dash pattern should start from. 
     * @param offset The distance into the dash pattern that should act as the start.
     */
    public void setDashOffset(float offset){
        dashOffset = offset;
    }
    
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be used and specifies the fill graphic to use.
     *
     * @return The graphic to use as a stipple fill.  If null, then no Stipple fill should be used.
     */
    public Graphic getGraphicFill() {
        return fillGraphic;
    }
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be used and specifies the fill graphic to use.
     *
     * @param graphic The graphic to use as a stipple fill.  If null, then no Stipple fill should be used.
     */
    public void setGraphicFill(Graphic graphic) {
        fillGraphic = graphic;
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
        return strokeGraphic;
    }
    
    /**
     * This parameter indicates that a repeated-linear-graphic graphic stroke type will be used and specifies the graphic to use.
     *
     * Proper stroking with a linear graphic requires two "hot-spot" points within the space of the graphic to indicate where the rendering line starts and stops.
     * In the case of raster images with no special mark-up, this line will be assumed to be the middle pixel row of the image, starting from the first pixel column and
     * ending at the last pixel column.
     *
     * @param graphic The graphic to use as a linear graphic.  If null, then no graphic stroke should be used.
     */
    public void setGraphicStroke(Graphic graphic) {
        strokeGraphic = graphic;
    }
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @return The cap style.  This will be one of "butt", "round" and "square".  There is no defined default.
     */
    public String getLineCap() {
        return lineCap;
    }
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @param cap The cap style.  This can be one of "butt", "round" and "square".  There is no defined default.
     */
    public void setLineCap(String cap) {
        lineCap = cap;
    }
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @return The join style.  This will be one of "miter", "round" and
     * "bevel".  There is no defined default.
     */
    public String getLineJoin() {
        return lineJoin;
    }
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @param join The join style.  This will be one of "miter", "round" and
     * "bevel". There is no defined default.
     */
    public void setLineJoin(String join) {
        lineJoin = join;
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
        return opacity;
    }
    
    /**
     * This specifies the level of translucency to use when rendering the stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with 0.0
     * representing totally transparent and 1.0 representing totally opaque.  A linear scale of
     * translucency is used for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value is 1.0 (opaque).
     *
     * @param level The opacity of the stroke, where 0.0 is completely transparent and 1.0 is completely opaque.
     */
    public void setOpacity(double level) {
        opacity = level;
    }
    
    /**
     * This parameter gives the absolute width (thickness) of a stroke in pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative numbers are not.
     *
     * @return The width of the stroke in pixels.  This may be fractional but not negative.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * This parameter sets the absolute width (thickness) of a stroke in pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative numbers are not.
     *
     * @param pixels The width of the stroke in pixels.  This may be fractional but not negative.
     */
    public void setWidth(double pixels) {
        width = pixels;
    }
    
    
    
}
