/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.styling;


// J2SE dependencies
//import java.util.logging.Logger;
// OpenGIS dependencies
import java.awt.FontFormatException;
import java.util.Collections;
import java.util.List;

import org.geotools.resources.Utilities;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;


/**
 * Provides a Java representation of the Font element of an SLD.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class FontImpl implements Font, Cloneable {
    /** The logger for the default core module. */

    //private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.core");
    private Expression fontFamily = null;
    private Expression fontSize = null;
    private Expression fontStyle = null;
    private Expression fontWeight = null;

    /**
     * Creates a new instance of DefaultFont
     */
    protected FontImpl() {
    }

    /**
     * Getter for property fontFamily.
     *
     * @return Value of property fontFamily.
     */
    public Expression getFontFamily() {
        return fontFamily;
    }
	public List<Expression> getFamily() {
		return Collections.singletonList(fontFamily);
	}
    /**
     * Setter for property fontFamily.
     *
     * @param fontFamily New value of property fontFamily.
     */
    public void setFontFamily(Expression fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * Getter for property fontSize.
     *
     * @return Value of property fontSize.
     */
    public Expression getFontSize() {
        return fontSize;
    }
	public Expression getSize() {
		return fontSize;
	}
    /**
     * Setter for property fontSize.
     *
     * @param fontSize New value of property fontSize.
     */
    public void setFontSize(Expression fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Getter for property fontStyle.
     *
     * @return Value of property fontStyle.
     */
    public Expression getFontStyle() {
        return fontStyle;
    }
    public Expression getStyle() {
    	return fontStyle;
    }

    /**
     * Setter for property fontStyle.
     *
     * @param fontStyle New value of property fontStyle.
     */
    public void setFontStyle(Expression fontStyle) {
        this.fontStyle = fontStyle;
    }

    /**
     * Getter for property fontWeight.
     *
     * @return Value of property fontWeight.
     */
    public Expression getFontWeight() {
        return fontWeight;
    }
    public Expression getWeight() {
    	return fontWeight;
    }

    /**
     * Setter for property fontWeight.
     *
     * @param fontWeight New value of property fontWeight.
     */
    public void setFontWeight(Expression fontWeight) {
        this.fontWeight = fontWeight;
    }

    /**
     * Creates a clone of the font.
     *
     * @see Cloneable#clone()
     */
    public Object clone() {
        try {
            // all the members are immutable expression
            // super.clone() is enough.
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This should not happen", e);
        }
    }

    /**
     * Generates the hashcode for the font.
     *
     * @return the hash code.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (fontFamily != null) {
            result = (PRIME * result) + fontFamily.hashCode();
        }

        if (fontSize != null) {
            result = (PRIME * result) + fontSize.hashCode();
        }

        if (fontStyle != null) {
            result = (PRIME * result) + fontStyle.hashCode();
        }

        if (fontWeight != null) {
            result = (PRIME * result) + fontWeight.hashCode();
        }

        return result;
    }

    /**
     * Compares this font with another for equality.  Two fonts are equal if
     * their family, style, weight  and size are equal.
     *
     * @param oth DOCUMENT ME!
     *
     * @return True if this and oth are equal.
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

            return Utilities.equals(this.fontFamily, other.fontFamily)
            && Utilities.equals(this.fontSize, other.fontSize)
            && Utilities.equals(this.fontStyle, other.fontStyle)
            && Utilities.equals(this.fontWeight, other.fontWeight);
        }

        return false;
    }
    /**
     * Utility method to capture the default font in one place.
     * @return
     */
    public static Font createDefault( FilterFactory filterFactory ) {
        Font font = new FontImpl();
        try {
            font.setFontSize(filterFactory.literal(
                    new Integer(10)));
            font.setFontStyle(filterFactory.literal("normal"));
            font.setFontWeight(filterFactory.literal("normal"));
            font.setFontFamily(filterFactory.literal("Serif"));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            throw new RuntimeException("Error creating default", ife);
        }
        return font;
    }
    
}
