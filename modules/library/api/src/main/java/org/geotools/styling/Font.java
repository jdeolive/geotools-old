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

    /**
     * @deprecated this method will be replaced by getFamily in 2.6.x
     */
    @Deprecated
    Expression getFontFamily();

    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontFamily(Expression family);

    /**
     * @deprecated this method will be replaced by getStyle in 2.6.x
     */
    @Deprecated
    Expression getFontStyle();

    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontStyle(Expression style);

    /**
     * @deprecated this method will be replaced by getWeight in 2.6.x
     */
    @Deprecated
    Expression getFontWeight();

    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontWeight(Expression weight);

    /**
     * @deprecated this method will be replaced by getSize in 2.6.x
     */
    @Deprecated
    Expression getFontSize();

    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
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
