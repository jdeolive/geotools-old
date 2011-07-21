/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import java.util.List;

import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * Factory that creates extended operators.
 * <p>
 * An "extended operator" is syntactic sugar for creating an existing filter. For instance, consider 
 * the following conventional filter:
 * <pre>
 * <PropertyIsEqualTo>
 *   <PropertyName>someAttribute</PropertyName>
 *   <Literal>foo</Literal>
 * </PropertyIsEqualTo>
 *  </pre>
 * </p>
 * <p>
 *  This filter could be replaced by an extended operator:
 *  <pre>
 *  <myns:FooEqualsTest>
 *    <PropertyName>someAttribute</PropertyName>
 *  </myns:FooEqualsTest>
 *  </pre>
 * </p>
 * <p>
 * Extended operators are identified by their name. The name should be qualified by an namespace 
 * uri to avoid name clashing. 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface ExtendedOperatorFactory {

    /**
     * The list of qualified names for extended operators that this factory produces. 
     */
    List<Name> getOperatorNames();

    /**
     * Compiles the extended operator into a Filter instance.
     * <p>
     * This method takes a set of <tt>args</tt> and uses the <tt>factory</tt> to create a filter
     * that implements the semantics of the extended operator. 
     * </p>
     * @param name The name of the operator.
     * @param args The arguments passed to the operator.
     * @param factory A filter factory.
     * 
     * @return
     */
    org.opengis.filter.Filter operator(Name name, List<Expression> args, FilterFactory factory);
}
