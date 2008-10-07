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

package org.geotools.filter.text.commons;

import org.geotools.filter.text.commons.CompilerFactory.Language;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

/**
 * Compiler Utility class
 * 
 * <p>
 * This is an internal utility class with convenient methods for compiler actions.
 * 
 * This is intended as an internal interface used only in this module. Client modules
 * mustn't use it.
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
final public class CompilerUtil {

    private CompilerUtil(){
        // utility class
    }
    
    /**
     * Makes the Filter for the predicate
     * @param predicate
     * @return Filter
     * @throws CQLException
     */
    static final public Filter parseFilter(final Language language, final String predicate) throws CQLException {

        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";
        
        ICompiler compiler = CompilerFactory.makeCompiler(language, predicate, null);
        compiler.compileFilter();
        Filter result = compiler.getFilter();

        return result;
    }
    
    static final public Expression parseExpression(final Language language, final String predicate) throws CQLException {

        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";
        
        ICompiler compiler = CompilerFactory.makeCompiler(language, predicate, null);
        compiler.compileExpression();
        Expression result = compiler.getExpression();

        return result;
    }
    
    
}
