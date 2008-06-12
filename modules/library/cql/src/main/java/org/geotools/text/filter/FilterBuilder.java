/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.text.filter;

import java.util.List;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.generated.parsers.ParseException;


/**
 * FilterBuilder (the original name was ExpressionBuilder) is the main entry
 * point for parsing Filters from the language.
 * <p>
 * This class was extended to generate semantic actions for all the CQL
 * production rules.
 * </p>
 * <p>
 * Aditionaly refactoring was done in order to adapt the products to the new
 * GeoAPI filter interfaces, targeting Filter 1.1.0.
 * </p>
 * <p>
 * <b>CQL</b> is an acronym for OGC Common Query Language, a query predicate
 * language whose syntax is similar to a SQL WHERE clause, defined in clause
 * 6.2.2 of the OGC Catalog Service for Web, version 2.0.1 implementation
 * specification.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * This class provides two methods, {@link #parse(String)} and
 * {@link #parse(org.opengis.filter.FilterFactory, String)},
 *
 * </p>
 *
 * @since 2.4
 * @author Created by: Ian Schneider
 * @author Extended by: Mauricio Pazos - Axios Engineering
 * @author Extended by: Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://gtsvn.refractions.net/geotools/trunk/gt/modules/unsupported/cql/src/main/java/org/geotools/text/filter/FilterBuilder.java $
 * @deprecated use the {@link CQL} utility class instead, this one is going to
 *             be set to package visibility
 *
 */
public class FilterBuilder {
    /**
     * Delimiter characted used for
     * {@link #parseFilterList(FilterFactory, String)} to distinguish between
     * the different filters in a list (for example:
     * <code>att > 1| att2 < 3</code>
     */

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * provided FilterFactory.
     *
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter. If it is null the method finds the default implementation.
     * @param input
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.Filter parse(FilterFactory filterFactory, String input)
        throws ParseException {
        return CQL.toFilter(input, filterFactory);
    }

        
    /**
     * Parses the input string in OGC CQL format into a Filter.
     * 
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression. If it is null the method finds the default implementation.
     * @param input
     *            a string containing a query predicate in OGC CQL format.
     *            If it is null the method finds the default implementation.
     * @return
     * @throws ParseException
     */
    public static List parseFilterList(FilterFactory2 filterFactory, String input)
        throws ParseException {
        return CQL.toFilterList(input, filterFactory);
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * systems default FilterFactory implementation.
     *
     * @param input
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.Filter parse(String input)
        throws ParseException {
        return CQL.toFilter(input);
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * provided FilterFactory.
     *
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression. If it is null the method finds the default implementation.
     * @param input
     *            a string containing a OGC CQL expression.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.expression.Expression parseExpression(
        FilterFactory filterFactory, String input) throws ParseException {
        return CQL.toExpression(input, filterFactory);
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * systems default FilterFactory implementation.
     *
     * @param input
     *            a string containing an OGC CQL expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.expression.Expression parseExpression(String input)
        throws ParseException {
        Expression expression = CQL.toExpression(input);

        return expression;
    }

    /**
     * Returns a formatted error string, showing the original input, along with
     * a pointer to the location of the error and the error message itself.
     *
     */
    public static String getFormattedErrorMessage(ParseException pe, String input) {
        String formattedErrorMessage = ((CQLException)pe).getSyntaxError();

        return formattedErrorMessage;
    }

    public static final void main(String[] args) throws Exception {
        CQL.main(args);
    }
}
