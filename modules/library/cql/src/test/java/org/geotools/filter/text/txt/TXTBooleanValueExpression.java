/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.CQLBooleanValueExpression;
import org.geotools.filter.text.cql2.CompilerFactory.Language;

/**
 * TXT Boolean expression test
 *
 * <p>
 * Executes the and/or predicates test implemented for CQL using the TXT compiler
 * </p>
 * @see CQLBooleanValueExpression
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class TXTBooleanValueExpression extends CQLBooleanValueExpression {

    public TXTBooleanValueExpression() {
        super(Language.TXT);
    }
    
}
