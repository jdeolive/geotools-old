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
import org.geotools.filter.*;

/**
 * @version $Id: DefaultFill.java,v 1.6 2002/06/04 16:55:37 loxnard Exp $
 * @author James Macgill, CCG
 */

public class DefaultFill implements org.geotools.styling.Fill {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultFill.class);
    private Expression color = null;
    
    private Expression opacity = null;
    
    private Graphic graphicFill = null;
    
    /** Creates a new instance of DefaultFill */
    public DefaultFill() {
        try{
            color = new ExpressionLiteral("#808080");
            opacity = new ExpressionLiteral(new Double(1.0));
        } catch (IllegalFilterException ife){
            _log.fatal("Failed to build default fill: "+ife);
            System.err.println("Failed to build default fill: "+ife);
        }
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
     * @param The color of the Fill encoded as a hexidecimal RGB value.
     */
    public void setColor(Expression rgb) {
        color = rgb;
    }
    public void setColor(String rgb){
        try{
            color = new ExpressionLiteral(rgb);
        } catch (IllegalFilterException ife){
            _log.debug("error setting color: "+ife);
        }
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
    public void setOpacity(String opacity){
        try{
            this.opacity = new ExpressionLiteral(opacity);
        } catch (IllegalFilterException ife){
            _log.debug("error setting opacity: "+ife);
        }
    }
    /**
     * This parameter indicates that a stipple-fill repeated graphic will be used and
     * specifies the fill graphic to use.
     *
     * @return graphic The graphic to use as a stipple fill.
     *         If null then no Stipple fill should be used.
     */
    
    
    public org.geotools.styling.Graphic getGraphicFill() {
        return graphicFill;
    }
    
    /**
     * Setter for property graphic.
     * @param graphic New value of property graphic.
     */
    public void setGraphicFill(org.geotools.styling.Graphic graphic) {
        this.graphicFill = graphicFill;
    }
    
}
