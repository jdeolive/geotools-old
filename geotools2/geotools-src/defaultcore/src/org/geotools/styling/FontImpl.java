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
//import java.util.logging.Logger;
// Geotools dependencies
import org.geotools.filter.Expression;
import org.geotools.util.Cloneable;
import org.geotools.util.EqualsUtils;

/** Provides a Java representation of the Font element of an SLD.
 * 
 * @version $Id: FontImpl.java,v 1.6 2003/09/06 04:52:31 seangeo Exp $
 * @author Ian Turton, CCG
 */
public class FontImpl implements Font, Cloneable {
    /**
     * The logger for the default core module.
     */

    //private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private Expression fontFamily = null;
    private Expression fontSize = null;
    private Expression fontStyle = null;
    private Expression fontWeight = null;

    /** Creates a new instance of DefaultFont */
    protected FontImpl() {
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
    
    /** Creates a clone of the font.
     * 
     * @see org.geotools.util.Cloneable#clone()
     */
    public Object clone() {        
        try {
            // all the members are immutable expression
            // super.clone() is enough.
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This should not happen",e);
        }        
    }

    /** Generates the hashcode for the font.
     *  
     *  @return the hash code.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (fontFamily != null) {
            result = PRIME * result + fontFamily.hashCode();
        }
        if (fontSize != null) {
            result = PRIME * result + fontSize.hashCode();
        }
        if (fontStyle != null) {
            result = PRIME * result + fontStyle.hashCode();
        }
        if (fontWeight != null) {
            result = PRIME * result + fontWeight.hashCode();
        }

        return result;
    }

    /** Compares this font with another for equality.
     * 
     *  Two fonts are equal if their family, style, weight 
     *  and size are equal.
     * 
     *  @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth instanceof FontImpl) {
            FontImpl other = (FontImpl) oth;
            return EqualsUtils.equals(this.fontFamily, other.fontFamily) &&
                    EqualsUtils.equals(this.fontSize, other.fontSize) &&
                    EqualsUtils.equals(this.fontStyle, other.fontStyle) &&
                    EqualsUtils.equals(this.fontWeight, other.fontWeight);
        }

        return false;
    }

}