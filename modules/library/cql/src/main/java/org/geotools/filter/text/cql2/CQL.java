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
package org.geotools.filter.text.cql2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterTransformer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;


/**
 * Utility class to parse <b>CQL</b> predicates and expressions to GeoAPI
 * {@link Filter}s and {@link Expression}s, respectively.
 *
 * <p>
 * <b>CQL</b> is an acronym for OGC Common Query Language, a query predicate
 * language whose syntax is similar to a SQL WHERE clause, defined in clause
 * 6.2.2 of the OGC Catalog Service for Web, version 2.0.1 implementation
 * specification.
 * </p>
 * <p>
 * This class provides three methods, {@link #toFilter(String)},
 * {@link #toExpression(String)} and {@link #toFilterList(String)}; and an
 * overloaded version of each one for the user to provide a
 * {@link FilterFactory} implementation to use.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Here are some usage examples. Refer to the <a
 * href="http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design">complete
 * grammar</a> to see what exactly you can do.
 *
 * <pre>
 * <code>
 * Filter f1 = CQL.toFilter(&quot;ATTR1 &lt; 10 AND ATTR2 &lt; 2 OR ATTR3 &gt; 10&quot;);
 *
 * Filter f2 = CQL.toFilter(&quot;ATTR1 IS NULL&quot;);
 *
 * Filter f3 = CQL.toFilter(&quot;ATTR1 BEFORE 2006-11-30T01:30:00Z&quot;);
 *
 * Filter f4 = CQL.toFilter(&quot;ATTR1 DOES-NOT-EXIST&quot;);
 *
 * Filter f5 = CQL.toFilter(&quot;ATTR1 BETWEEN 10 AND 20&quot;);
 *
 * Filter f6 = CQL.toFilter(&quot;CROSS(ATTR1, LINESTRING(1 2, 10 15))&quot;);
 *
 * Filter f7 = CQL.toFilter(&quot;BBOX(ATTR1, 10,20,30,40)&quot;);
 *
 * Expression expr1 = CQL.toExpression(&quot;attName&quot;);
 *
 * Expression expr2 = CQL.toExpression(&quot;attName * 2&quot;);
 *
 * Expression expr3 = CQL.toExpression(&quot;strConcat(attName, 'suffix')&quot;);
 *
 * List filters = CQL
 *                 .toFilterList(&quot;ATTR1 IS NULL|BBOX(ATTR1, 10,20,30,40)|INCLUDE&quot;);
 * </code>
 * </pre>
 *
 * @since 2.5
 * @author Mauricio Pazos (Axios Engineering)
 * @author Gabriel Roldan (Axios Engineering)
 * @version $Id$
 * @source $URL:
 *        http://svn.geotools.org/geotools/trunk/gt/modules/library/cql/src/main/java/org/geotools/filter/text/cql2/CQL.java $
 */
public class CQL {
    protected CQL() {
        // do nothing, private constructor
        // to indicate it is a pure utility class
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * systems default FilterFactory implementation.
     *
     * @param cqlPredicate
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>cqlPredicate</code>.
     */
    public static Filter toFilter(final String cqlPredicate)
        throws CQLException {
        Filter filter = CQL.toFilter(cqlPredicate, null);

        return filter;
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * provided FilterFactory.
     *
     * @param cqlPredicate
     *            a string containing a query predicate in OGC CQL format.
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter. If it is null the method finds the default implementation.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>Predicate</code>.
     */
    public static Filter toFilter(final String cqlPredicate, final FilterFactory filterFactory)
        throws CQLException {

            ICompiler compiler = CompilerFactory.makeCompiler(CompilerFactory.Language.CQL, cqlPredicate, filterFactory);
            compiler.compileFilter();
            Filter result = compiler.getFilter();

            return result;
    }
    

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * systems default FilterFactory implementation.
     *
     * @param cqlExpression
     *            a string containing an OGC CQL expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>cqlExpression</code>.
     */
    public static Expression toExpression(String cqlExpression)
        throws CQLException {
        return toExpression(cqlExpression, null);
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

    /**
     * WARNING THIS IS A WORK IN PROGRESS.
     * 
     * @param filter
     * @return
     */
    public static String toCQL( Filter filter ){
        FilterToCQL toCQL = new FilterToCQL();
        
        StringBuffer output = (StringBuffer) filter.accept( toCQL, new StringBuffer() );
        
        return output.toString();        
    }
    /**
     * WARNING THIS IS A WORK IN PROGRESS.
     * 
     * @param filter
     * @return
     */
    public static String toCQL( Expression expression ){
        FilterToCQL toCQL = new FilterToCQL();
        
        StringBuffer output = (StringBuffer) expression.accept( toCQL, new StringBuffer() );
        
        return output.toString();        
    }
    /**
     * Parses the input string, which has to be a list of OGC CQL predicates
     * separated by <code>|</code> into a <code>List</code> of
     * <code>Filter</code>s, using the provided FilterFactory.
     *
     * @param cqlSourceFilterList
     *            a list of OGC CQL predicates separated by <code>|</code>
     *
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression. If it is null the method finds the default implementation.
     * @return a List of {@link Filter}, one for each input CQL statement
     */
    public static List<Filter> toFilterList(final String cqlSourceFilterList, final FilterFactory filterFactory)
        throws CQLException {

        ICompiler compiler = CompilerFactory.makeCompiler(CompilerFactory.Language.CQL, cqlSourceFilterList, filterFactory);
        compiler.compileFilterList();
        List<Filter> results = compiler.getFilterList();
        return results;

    }


    public static final void main(String[] args) {
        System.out.println("Expression Tester (\"quit\" to finish)");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        FilterTransformer filterTransformer = new FilterTransformer();
        filterTransformer.setIndentation(4);

        while (true) {
            System.out.print(">");

            String line = null;

            try {
                line = reader.readLine();

                if (line.equals("quit")) {
                    System.out.println("Bye!");
                    break;
                }

                Object newFilter = CQL.toFilter(line);
                filterTransformer.transform(newFilter, System.out);
                System.out.println();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (CQLException cqlex) {
                System.out.println(cqlex.getSyntaxError());
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }
}
