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

import org.opengis.filter.expression.Expression;


/**
 * A system-independent object for holding SLD font information. This holds
 * information on the text font to use in text processing. Font-family,
 * font-style, font-weight and font-size.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public interface Font {
    /** default font-size value **/
    static final int DEFAULT_FONTSIZE = 10;

    Expression getFontFamily();

    void setFontFamily(Expression family);

    Expression getFontStyle();

    void setFontStyle(Expression style);

    Expression getFontWeight();

    void setFontWeight(Expression weight);

    Expression getFontSize();

    void setFontSize(Expression size);

    /**
     * Enumeration of allow font-style values.
     */
    interface Style {
        static final String NORMAL = "normal";
        static final String ITALIC = "italic";
        static final String OBLIQUE = "oblique";
    }

    /**
     * Enumeration of allow font-weight values.
     */
    interface Weight {
        static final String NORMAL = "normal";
        static final String BOLD = "bold";
    }
}
