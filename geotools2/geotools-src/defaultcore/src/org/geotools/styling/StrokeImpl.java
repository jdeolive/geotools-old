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
 **/

package org.geotools.styling;


 /**
  *
  * TODO:This is unfinished as it currently returns fixed values with no way
  * to change them.
  *
  * @version $Id: StrokeImpl.java,v 1.1 2002/10/14 14:16:08 ianturton Exp $
  * @author James Macgill, CCG
  */

import org.geotools.filter.*;

public class StrokeImpl implements org.geotools.styling.Stroke {
    private Expression color;
    private float[] dashArray;
    private Expression dashOffset ;
    private Graphic fillGraphic;
    private Graphic strokeGraphic;
    private Expression lineCap;
    private Expression lineJoin;
    private Expression opacity;
    private Expression width;
    
    /** Creates a new instance of DefaultStroke */
    public StrokeImpl() {
        try {
            color = new ExpressionLiteral("#000000");
            dashArray = null;//HACK: is this an acceptable return?
            dashOffset = new ExpressionLiteral(new Integer(0));
            fillGraphic = null;
            strokeGraphic = null;
            lineCap = new ExpressionLiteral("butt");
            lineJoin = new ExpressionLiteral("miter");
            opacity = new ExpressionLiteral(new Integer(1));
            width = new ExpressionLiteral(new Integer(1));
        } catch (IllegalFilterException ife){
            //we should never be in here
            //TODO: log this as a fatal exception
            System.err.println("DefaultStroke constructor failed " + ife);
        }
    }
    
    /**
     * This parameter gives the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with the
     * hash (#) sign.  The hexidecimal digits between A and F may be in either
     * upper or lower case.  For example, full red is encoded as "#ff0000" 
     * (with no quotation marks).  The default color is defined to be
     * black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @return The color of the stroke encoded as a hexidecimal RGB value.
     */
    public Expression getColor() {
        return color;
    }
    
    /**
     * This parameter sets the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with the
     * hash (#) sign.  The hexidecimal digits between A and F may be in either
     * upper or lower case.  For example, full red is encoded as "#ff0000" 
     * (with no quotation marks).  The default color is defined to be
     * black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @param c The color of the stroke encoded as a hexidecimal RGB value.
     */
    public void setColor(Expression c) {
        if(c == null) {
           return;
        }
        color = c;
    }
    
    /**
     * This parameter sets the solid color that will be used for a stroke.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component in the order Red, Green, Blue, prefixed with the
     * hash (#) sign.  The hexidecimal digits between A and F may be in either
     * upper or lower case.  For example, full red is encoded as "#ff0000" 
     * (with no quotation marks).  The default color is defined to be
     * black ("#000000").
     *
     * Note: in CSS this parameter is just called Stroke and not Color.
     *
     * @param c The color of the stroke encoded as a hexidecimal RGB value.
     */
    public void setColor(String c) {
            setColor(new ExpressionLiteral(c));
    }
    
    
    /**
     * This parameter encodes the dash pattern as a series of floats.<br>
     * The first number gives the length in pixels of the dash to draw,
     * the second gives the amount of space to leave, and this pattern
     * repeats.<br>
     * If an odd number of values is given, then the pattern is expanded
     * by repeating it twice to give an even number of values.
     * The default is to draw an unbroken line.<br>
     *
     * For example, "2 1 3 2" would produce:<br>
     * <code>--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;
     * ---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--&nbsp;---&nbsp;&nbsp;--</code>
     *
     * @return The dash pattern as an array of float values in the form
     * "dashlength gaplength ..."
     */
    public float[] getDashArray() {
        if (dashArray == null) {
            return new float[0];
        }
        float[] newArray = new float[dashArray.length];
        System.arraycopy(dashArray, 0, newArray, 0, dashArray.length);
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
     * @param dashPattern The dash pattern as an array of float values in the
     * form "dashlength gaplength ..."
     */
    public void setDashArray(float[] dashPattern) {
        dashArray = dashPattern;
    }
    
    /**
     * This param determines where the dash pattern should start from.
     */
    public Expression getDashOffset() {
        return dashOffset;
    }
    
    /**
     * This param determines where the dash pattern should start from.
     * @param offset The distance into the dash pattern that should act as
     *        the start.
     */
    public void setDashOffset(Expression offset){
        if (offset == null) {
            return;
        }
        dashOffset = offset;
    }
    
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will
     * be used and specifies the fill graphic to use.
     *
     * @return The graphic to use as a stipple fill.
     *         If null, then no Stipple fill should be used.
     */
    public Graphic getGraphicFill() {
        return fillGraphic;
    }
    
    /**
     * This parameter indicates that a stipple-fill repeated graphic will
     * be used and specifies the fill graphic to use.
     *
     * @param graphic The graphic to use as a stipple fill.
     *        If null, then no Stipple fill should be used.
     */
    public void setGraphicFill(Graphic graphic) {
        fillGraphic = graphic;
    }
    
    /**
     * This parameter indicates that a repeated-linear-graphic graphic
     * stroke type will be used and specifies the graphic to use.
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
    public Graphic getGraphicStroke() {
        return strokeGraphic;
    }
    
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
     * @param graphic The graphic to use as a linear graphic.
     *        If null, then no graphic stroke should be used.
     */
    public void setGraphicStroke(Graphic graphic) {
        strokeGraphic = graphic;
    }
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @return The cap style.  This will be one of "butt", "round" and "square"
     *         There is no defined default.
     */
    public Expression getLineCap() {
        return lineCap;
    }
    
    /**
     * This parameter controls how line strings should be capped.
     *
     * @param cap The cap style. This can be one of "butt", "round" and "square"
     *        There is no defined default.
     */
    public void setLineCap(Expression cap) {
        if (cap == null) {
            return;
        }
        lineCap = cap;
    }
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @return The join style.  This will be one of "mitre", "round" and
     * "bevel".  There is no defined default.
     */
    public Expression getLineJoin() {
        return lineJoin;
    }
    
    /**
     * This parameter controls how line strings should be joined together.
     *
     * @param join The join style.  This will be one of "mitre", "round" and
     * "bevel". There is no defined default.
     */
    public void setLineJoin(Expression join) {
        if (join == null) {
            return;
        }
        lineJoin = join;
    }
    
    /**
     * This specifies the level of translucency to use when rendering the
     * stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally
     * opaque.  A linear scale of translucency is used for intermediate
     * values.<br>
     * For example, "0.65" would represent 65% opacity.
     * The default value is 1.0 (opaque).
     *
     * @return The opacity of the stroke, where 0.0 is completely transparent
     * and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        return opacity;
    }
    
    /**
     * This specifies the level of translucency to use when rendering the
     * stroke.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally
     * opaque.  A linear scale of translucency is used for intermediate
     * values.<br>
     * For example, "0.65" would represent 65% opacity.
     * The default value is 1.0 (opaque).
     *
     * @param level The opacity of the stroke, where 0.0 is completely
     * transparent and 1.0 is completely opaque.
     */
    public void setOpacity(Expression level) {
        if (level == null) {
            return;
        }
        opacity = level;
    }
    
    /**
     * This parameter gives the absolute width (thickness) of a stroke in
     * pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative
     * numbers are not.
     *
     * @return The width of the stroke in pixels.  This may be fractional
     * but not negative.
     */
    public Expression getWidth() {
        return width;
    }
    
    /**
     * This parameter sets the absolute width (thickness) of a stroke in
     * pixels encoded as a float.
     * The default is 1.0.  Fractional numbers are allowed but negative
     * numbers are not.
     *
     * @param expr The width of the stroke in pixels.  This may be fractional
     * but not negative.
     */
    
    
    public void setWidth(Expression expr){
        if (expr == null) {
            return;
        }
        width = expr;
    }
    
    
}
