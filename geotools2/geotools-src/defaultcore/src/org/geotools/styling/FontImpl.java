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

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.filter.Expression;


/**
 * @version $Id: FontImpl.java,v 1.2 2002/10/16 16:57:21 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class FontImpl implements Font {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    private Expression fontFamily = null;
    private Expression fontSize = null;
    private Expression fontStyle = null;
    private Expression fontWeight = null;
    /** Creates a new instance of DefaultFont */
    protected FontImpl() {
        try {
            fontSize = new org.geotools.filter.ExpressionLiteral(new Integer(10));
            fontStyle = new org.geotools.filter.ExpressionLiteral("normal");
            fontWeight = new org.geotools.filter.ExpressionLiteral("normal");
            fontFamily = new org.geotools.filter.ExpressionLiteral("Courier");
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultFont: " + ife);
        }
    }
 
    /** Getter for property fontFamily.
     * @return Value of property fontFamily.
     */
    public Expression getFontFamily() {
        return fontFamily;
    }    
    
    /** Setter for property fontFamily.
     * @param fontFamily New value of property fontFamily.
     */
    public void setFontFamily(Expression fontFamily) {
        this.fontFamily = fontFamily;
    }
    
    /** Getter for property fontSize.
     * @return Value of property fontSize.
     */
    public Expression getFontSize() {
        return fontSize;
    }
    
    /** Setter for property fontSize.
     * @param fontSize New value of property fontSize.
     */
    public void setFontSize(Expression fontSize) {
        this.fontSize = fontSize;
    }
    
    /** Getter for property fontStyle.
     * @return Value of property fontStyle.
     */
    public Expression getFontStyle() {
        return fontStyle;
    }
    
    /** Setter for property fontStyle.
     * @param fontStyle New value of property fontStyle.
     */
    public void setFontStyle(Expression fontStyle) {
        this.fontStyle = fontStyle;
    }
    
    /** Getter for property fontWeight.
     * @return Value of property fontWeight.
     */
    public Expression getFontWeight() {
        return fontWeight;
    }
    
    /** Setter for property fontWeight.
     * @param fontWeight New value of property fontWeight.
     */
    public void setFontWeight(Expression fontWeight) {
        this.fontWeight = fontWeight;
    }
    
}
