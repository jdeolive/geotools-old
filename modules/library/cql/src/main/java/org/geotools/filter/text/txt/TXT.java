/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

import java.util.List;

import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CompilerFactory;
import org.geotools.filter.text.cql2.ICompiler;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;


/**
 * TODO WARNING THIS IS A WORK IN PROGRESS.
 * 
 * TXT Query Language
 * 
 * <p>
 * TXT is an extension of CQL. This class presents the operations available 
 * to parse the TXT language and generates the correspondent filter.
 * </p>
 * 
 * 
 * @author Jody Garnett
 * @author Mauricio Pazos (Axios Engineering)
 * 
 * @since 2.5
 */
class TXT {
    
    protected TXT(){
        //
    }

    /**
     * Parses the input string in TXT format into a Filter, using the
     * systems default FilterFactory implementation.
     *
     * @param cqlPredicate
     *            a string containing a query predicate in TXT format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>txtPredicate</code>.
     */
    public static Filter toFilter(final String txtPredicate)
        throws CQLException {
        Filter filter = TXT.toFilter(txtPredicate, null);

        return filter;
    }

    /**
     * Parses the input string in TXT format into a Filter, using the
     * provided FilterFactory.
     *
     * @param cqlPredicate
     *            a string containing a query predicate in TXT format.
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter. If it is null the method finds the default implementation.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>Predicate</code>.
     */
    public static Filter toFilter(final String txtPredicate, final FilterFactory filterFactory)
        throws CQLException {

        ICompiler compiler = CompilerFactory.makeCompiler(CompilerFactory.Language.TXT, txtPredicate, filterFactory);
        compiler.compileFilter();
        Filter result = compiler.getFilter();

        return result;
    }
    

    /**
     * Parses the input string in TXT format into an Expression, using the
     * systems default FilterFactory implementation.
     *
     * @param txtExpression  a string containing an TXT expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>txtExpression</code>.
     */
    public static Expression toExpression(String txtExpression)
        throws CQLException {
        return toExpression(txtExpression, null);
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * provided FilterFactory.
     *
     * @param cqlExpression
     *            a string containing a OGC CQL expression.
     *
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression. If it is null the method finds the default implementation.    
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>cqlExpression</code>.
     */
    public static Expression toExpression(final String cqlExpression,
                                          final FilterFactory filterFactory) throws CQLException {

            ICompiler compiler = CompilerFactory.makeCompiler(CompilerFactory.Language.CQL, cqlExpression, filterFactory);
            compiler.compileExpression();           
            Expression builtFilter = compiler.getExpression();

            return builtFilter;
    }

    /**
     * Parses the input string, which has to be a list of OGC CQL predicates
     * separated by <code>|</code> into a <code>List</code> of
     * <code>Filter</code>s, using the provided FilterFactory.
     *
     * @param cqlFilterList
     *            a list of OGC CQL predicates separated by <code>|</code>
     *
     * @return a List of {@link Filter}, one for each input CQL statement
     */
    public static List<Filter> toFilterList(final String cqlFilterList)
        throws CQLException {
        
        List<Filter> filters = CQL.toFilterList(cqlFilterList, null);

        return filters;
    }
    
    
}
