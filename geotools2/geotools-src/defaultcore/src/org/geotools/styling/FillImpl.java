/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Center for Computational Geography
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
 *     UNITED KINGDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 */
package org.geotools.styling;

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.filter.Expression;


/**
 * @version $Id: FillImpl.java,v 1.7 2003/08/01 16:54:49 ianturton Exp $
 * @author James Macgill, CCG
 */
public class FillImpl implements org.geotools.styling.Fill {
    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private Expression color = null;
    private Expression backgroundColor = null;
    private Expression opacity = null;
    private Graphic graphicFill = null;

    /** Creates a new instance of DefaultFill */
    protected FillImpl() {
    }

    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     * The default color is defined to be 50% gray ("#808080").
     *
     * Note: in CSS this parameter is just called Fill and not Color.
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     */
    public Expression getColor() {
        return color;
    }

    /**
     * This parameter gives the solid color that will be used for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     *
     * Note: in CSS this parameter is just called Fill and not Color.
     *
     * @param rgb The color of the Fill encoded as a hexidecimal RGB value.
     */
    public void setColor(Expression rgb) {
        color = rgb;
    }

    public void setColor(String rgb) {
        color = filterFactory.createLiteralExpression(rgb);
    }

    /**
     * This parameter gives the solid color that will be used as a background for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     * The default color is defined to be transparent.
     *
     *
     * @return The color of the Fill encoded as a hexidecimal RGB value.
     */
    public Expression getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * This parameter gives the solid color that will be used as a background for a Fill.<br>
     * The color value is RGB-encoded using two hexidecimal digits per
     * primary-color component, in the order Red, Green, Blue, prefixed with
     * the hash (#) sign.
     * The hexidecimal digits between A and F may be in either upper
     * or lower case.  For example, full red is encoded as "#ff0000" (with no
     * quotation marks).
     *
     *
     *
     * @param rgb The color of the Fill encoded as a hexidecimal RGB value.
     */
    public void setBackgroundColor(Expression rgb) {
        backgroundColor = rgb;
    }

    public void setBackgroundColor(String rgb) {
        LOGGER.fine("setting bg color with " + rgb + " as a string");
        backgroundColor = filterFactory.createLiteralExpression(rgb);
    }

    /**
     * This specifies the level of translucency to use when rendering the fill.
     * <br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.
     * The default value is 1.0 (opaque).
     *
     * @return The opacity of the fill, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        return opacity;
    }

    /**
     * Setter for property opacity.
     * @param opacity New value of property opacity.
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    public void setOpacity(String opacity) {
        this.opacity = filterFactory.createLiteralExpression(opacity);
    }

    /**
     * This parameter indicates that a stipple-fill repeated graphic will be
     * used and specifies the fill graphic to use.
     *
     * @return graphic The graphic to use as a stipple fill.
     *         If null then no Stipple fill should be used.
     */
    public org.geotools.styling.Graphic getGraphicFill() {
        return graphicFill;
    }

    /**
     * Setter for property graphic.
     * @param graphicFill New value of property graphic.
     */
    public void setGraphicFill(org.geotools.styling.Graphic graphicFill) {
        this.graphicFill = graphicFill;
    }
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
}