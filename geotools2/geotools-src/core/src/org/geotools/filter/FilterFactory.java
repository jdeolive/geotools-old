/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.FeatureType;


/**
 * This specifies the interface to create filters.
 *
 * @version $Id: FilterFactory.java,v 1.7 2003/08/07 19:55:22 cholmesny Exp $
 *
 * @task TODO: This needs to be massively overhauled.  This should be the
 *       source of immutability of filters.  See {@link FeatureTypeFactory},
 *       as that provides a good example of what this should look like.  The
 *       mutable factory to create immutable objects is a good model for this.
 *       The creation methods should only create fully formed filters.  This
 *       in turn means that all the set functions in the filters should be
 *       eliminated.  When rewriting this class/package, keep in mind
 *       FilterSAXParser in the filter module, as the factory should fit
 *       cleanly with that, and should handle sax parsing without too much
 *       memory overhead.
 * @task REVISIT: resolve errors, should all throw errors?
 */
public abstract class FilterFactory implements Factory {
    /** A cached factory to create filters. */
    private static FilterFactory factory = null;

    /**
     * Creates an instance of a Filter factory.
     *
     * @return An instance of the Filter factory.
     *
     * @throws FactoryConfigurationError If a factory is not found.
     */
    public static FilterFactory createFilterFactory()
        throws FactoryConfigurationError {
        if (factory == null) {
            factory = (FilterFactory) FactoryFinder.findFactory("org.geotools.filter.FilterFactory",
                    "org.geotools.filter.FilterFactoryImpl");
        }

        return factory;
    }

    /**
     * Creates a logic filter from two filters and a type.
     *
     * @param filter1 the first filter to join.
     * @param filter2 the second filter to join.
     * @param filterType must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public abstract LogicFilter createLogicFilter(Filter filter1,
        Filter filter2, short filterType) throws IllegalFilterException;

    /**
     * Creates an empty logic filter from a type.
     *
     * @param filterType must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public abstract LogicFilter createLogicFilter(short filterType)
        throws IllegalFilterException;

    /**
     * Creates a logic filter with an initial filter..
     *
     * @param filter the initial filter to set.
     * @param filterType Must be a logic type.
     *
     * @return the newly constructed logic filter.
     *
     * @throws IllegalFilterException If there were any problems creating the
     *         filter, including wrong type.
     */
    public abstract LogicFilter createLogicFilter(Filter filter,
        short filterType) throws IllegalFilterException;

    /**
     * Creates a BBox Expression from an envelope.
     *
     * @param env the envelope to use for this bounding box.
     *
     * @return The newly created BBoxExpression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract BBoxExpression createBBoxExpression(Envelope env)
        throws IllegalFilterException;

    /**
     * Creates an Integer Literal Expression.
     *
     * @param i the int to serve as literal.
     *
     * @return The new Literal Expression
     */
    public abstract LiteralExpression createLiteralExpression(int i);

    /**
     * Creates a Math Expression
     *
     * @return The new Math Expression
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract MathExpression createMathExpression()
        throws IllegalFilterException;

    /**
     * Creates a new Fid Filter with no initial fids.
     *
     * @return The new Fid Filter.
     */
    public abstract FidFilter createFidFilter();

    /**
     * Creates a Attribute Expression given a schema and attribute path.
     *
     * @param schema the schema to get the attribute from.
     * @param path the xPath of the attribute to compare.
     *
     * @return The new Attribute Expression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema, String path) throws IllegalFilterException;

    /**
     * Creates a Literal Expression from an Object.
     *
     * @param o the object to serve as the literal.
     *
     * @return The new Literal Expression
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract LiteralExpression createLiteralExpression(Object o)
        throws IllegalFilterException;

    /**
     * Creates a new compare filter of the given type.
     *
     * @param type the type of comparison - must be a compare type.
     *
     * @return The new compare filter.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract CompareFilter createCompareFilter(short type)
        throws IllegalFilterException;

    /**
     * Creates an empty Literal Expression
     *
     * @return The new Literal Expression.
     */
    public abstract LiteralExpression createLiteralExpression();

    /**
     * Creates a String Literal Expression
     *
     * @param s the string to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public abstract LiteralExpression createLiteralExpression(String s);

    /**
     * Creates a Double Literal Expression
     *
     * @param d the double to serve as the literal.
     *
     * @return The new Literal Expression
     */
    public abstract LiteralExpression createLiteralExpression(double d);

    /**
     * Creates a Attribute Expression with an initial schema.
     *
     * @param schema the schema to create with.
     *
     * @return The new Attribute Expression.
     */
    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema);

    /**
     * Creates a Math Expression of the given type.
     *
     * @param expressionType must be a math expression type.
     *
     * @return The new Math Expression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract MathExpression createMathExpression(short expressionType)
        throws IllegalFilterException;

    /**
     * Creates an empty Null Filter.
     *
     * @return The new Null Filter.
     */
    public abstract NullFilter createNullFilter();

    /**
     * Creates an empty Between Filter.
     *
     * @return The new Between Filter.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public abstract BetweenFilter createBetweenFilter()
        throws IllegalFilterException;

    /**
     * Creates a Geometry Filter.
     *
     * @param filterType the type to create, must be a geometry type.
     *
     * @return The new Geometry Filter.
     *
     * @throws IllegalFilterException if the filterType is not a geometry.
     */
    public abstract GeometryFilter createGeometryFilter(short filterType)
        throws IllegalFilterException;

    /**
     * Creates a Geometry Distance Filter
     *
     * @param filterType the type to create, must be beyond or dwithin.
     *
     * @return The new  Expression
     *
     * @throws IllegalFilterException if the filterType is not a geometry
     *         distance type.
     */
    public abstract GeometryDistanceFilter createGeometryDistanceFilter(
        short filterType) throws IllegalFilterException;

    /**
     * Creates a Fid Filter with an initial fid.
     *
     * @param fid the feature ID to create with.
     *
     * @return The new FidFilter.
     */
    public abstract FidFilter createFidFilter(String fid);

    /**
     * Creates a Like Filter.
     *
     * @return The new Like Filter.
     */
    public abstract LikeFilter createLikeFilter();

    /**
     * Creates a Function Expression.
     *
     * @param name the function name.
     *
     * @return The new Function Expression.
     */
    public abstract FunctionExpression createFunctionExpression(String name);
}
