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

import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;


/**
 * Encodes a filter into a SQL WHERE statement.  It should hopefully be generic
 * enough that any SQL database will work with it, though it has only been
 * tested with MySQL.  This generic SQL encoder should eventually be able to
 * encode all filters except Geometry Filters (currently LikeFilters are not
 * yet fully implemented, but when they are they should be generic enough).
 * This is because the OGC's SFS for SQL document specifies two ways of doing
 * SQL databases, one with native geometry types and one without.  To
 * implement an encoder for one of the two types simply subclass off of this
 * encoder and put in the proper GeometryFilter visit method.  Then add the
 * filter types supported to the capabilities in the static
 * capabilities.addType block.
 *
 * @author Chris Holmes, TOPP
 *
 * @task TODO: Implement LikeFilter encoding, need to figure out escape chars,
 *       the rest of the code should work right.  Once fixed be sure to add
 *       the LIKE type to capabilities, so others know that they can be
 *       encoded.
 */
public class SQLEncoder implements org.geotools.filter.FilterVisitor {
    //use these when Like is implemented.

    /** The standard SQL multicharacter wild card. */

    //private static String SQL_WILD_MULTI = "%";  

    /** The standard SQL single character wild card. */

    //private static String SQL_WILD_SINGLE = "_";

    /** The filter types that this class can encode */
    private static FilterCapabilities capabilities = new FilterCapabilities();

    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /**
     * sets the capabilities for this encoder
     */
    static {
        capabilities.addType(AbstractFilter.LOGIC_OR);
        capabilities.addType(AbstractFilter.LOGIC_AND);
        capabilities.addType(AbstractFilter.LOGIC_NOT);
        capabilities.addType(AbstractFilter.COMPARE_EQUALS);
	capabilities.addType(AbstractFilter.COMPARE_NOT_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
        capabilities.addType(AbstractFilter.NULL);
        capabilities.addType(AbstractFilter.BETWEEN);
    }

    private static java.util.HashMap comparisions = new java.util.HashMap();
    private static java.util.HashMap spatial = new java.util.HashMap();
    private static java.util.HashMap logical = new java.util.HashMap();
    private static java.util.HashMap expressions = new java.util.HashMap();

    static {
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS), "=");
	comparisions.put(new Integer(AbstractFilter.COMPARE_NOT_EQUALS), "!=");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN), ">");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),
            ">=");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN), "<");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),
            "<=");
        comparisions.put(new Integer(AbstractFilter.LIKE), "LIKE");
        comparisions.put(new Integer(AbstractFilter.NULL), "IS NULL");
        comparisions.put(new Integer(AbstractFilter.BETWEEN), "BETWEEN");

        expressions.put(new Integer(DefaultExpression.MATH_ADD), "+");
        expressions.put(new Integer(DefaultExpression.MATH_DIVIDE), "/");
        expressions.put(new Integer(DefaultExpression.MATH_MULTIPLY), "*");
        expressions.put(new Integer(DefaultExpression.MATH_SUBTRACT), "-");

        //more to come?
        spatial.put(new Integer(AbstractFilter.GEOMETRY_EQUALS), "Equals");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT), "Disjoint");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),
            "Intersects");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES), "Touches");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CROSSES), "Crosses");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_WITHIN), "Within");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS), "Contains");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS), "Overlaps");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BEYOND), "Beyond");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BBOX), "BBOX");

        logical.put(new Integer(AbstractFilter.LOGIC_AND), "AND");
        logical.put(new Integer(AbstractFilter.LOGIC_OR), "OR");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT), "NOT");
    }

    //use these when Like is implemented.

    /** The escaped version of the single wildcard for the REGEXP pattern. */

    //private String escapedWildcardSingle = "\\.\\?";

    /** The escaped version of the multiple wildcard for the REGEXP pattern. */

    //private String escapedWildcardMulti = "\\.\\*";

    /** used for constructing the string from visiting the filters. */
    protected Writer out;

    /** the encoded string for return */
    private String encodedSQL;

    /**
     * Empty constructor
     */
    public SQLEncoder() {
    }

    /**
     * Convenience constructor to perform the whole encoding process at once.
     *
     * @param out the writer to encode the SQL to.
     * @param filter the Filter to be encoded.
     *
     * @throws SQLEncoderException If there were problems encoding
     */
    public SQLEncoder(Writer out, Filter filter) throws SQLEncoderException {
        if (capabilities.fullySupports(filter)) {
            this.out = out;

            try {
                out.write("WHERE ");
                filter.accept(this);

                //out.write(";"); this should probably be added by client.
            } catch (java.io.IOException ioe) {
                log.warning("Unable to export filter: " + ioe);
		throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    /**
     * Performs the encoding, sends the encoded sql to the writer passed in.
     *
     * @param out the writer to encode the SQL to.
     * @param filter the Filter to be encoded.
     *
     * @throws SQLEncoderException If filter type not supported, or if there
     * were io problems.
     */
    public void encode(Writer out, Filter filter) throws SQLEncoderException {
        if (capabilities.fullySupports(filter)) {
            this.out = out;

            try {
                out.write("WHERE ");
                filter.accept(this);

                //out.write(";");
            } catch (java.io.IOException ioe) {
                log.warning("Unable to export filter" + ioe);
		throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    /**
     * Performs the encoding, returns a string of the encoded SQL.
     *
     * @param filter the Filter to be encoded.
     *
     * @return the string of the SQL where statement.
     *
     * @throws SQLEncoderException  If filter type not supported, or if there
     * were io problems.
     */
    public String encode(Filter filter) throws SQLEncoderException {
        StringWriter output = new StringWriter();
        encode(output, filter);

        return output.getBuffer().toString();
    }

    /**
     * Describes the capabilities of this encoder.
     *
     * @return The capabilities supported by this encoder.
     */
    public static FilterCapabilities getCapabilities() {
        return capabilities; //maybe clone?  Make immutable somehow
    }

    /**
     * This should never be called. This can only happen if a subclass of
     * AbstractFilter failes to implement its own version of
     * accept(FilterVisitor);
     *
     * @param filter The filter to visit
     */
    public void visit(Filter filter) {
        log.warning("exporting unknown filter type");
    }

    /**
     * Writes the SQL for the Between Filter.
     *
     * @param filter the  Filter to be visited.
     */
    public void visit(BetweenFilter filter) {
        log.finer("exporting BetweenFilter");

        DefaultExpression left = (DefaultExpression) filter.getLeftValue();
        DefaultExpression right = (DefaultExpression) filter.getRightValue();
        DefaultExpression mid = (DefaultExpression) filter.getMiddleValue();
        log.finer("Filter type id is " + filter.getFilterType());
        log.finer("Filter type text is " +
            comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            mid.accept(this);
            out.write(" BETWEEN ");
            left.accept(this);
            out.write(" AND ");
            right.accept(this);
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the SQL for the Like Filter.  Assumes the current java
     * implemented wildcards for the Like Filter: . for multi and .? for
     * single. And replaces them with the SQL % and _, respectively.
     * Currently  does nothing, and should not be called, not included in the
     * capabilities.
     *
     * @param filter the Like Filter to be visited.
     *
     * @task TODO: LikeFilter doesn't work right...revisit this when it does.
     *       Need to think through the escape char, so it works right when
     *       Java uses one, and escapes correctly with an '_'.
     */
    public void visit(LikeFilter filter) {
        /*        log.finer("exporting like filter");
           try{
               String pattern = filter.getPattern();
               pattern = pattern.replaceAll(escapedWildcardMulti, SQL_WILD_MULTI);
               pattern = pattern.replaceAll(escapedWildcardSingle, SQL_WILD_SINGLE);
               //pattern = pattern.replace('\\', ''); //get rid of java escapes.
                   //TODO escape the '_' char, as it could be in our string and will
                   //mess up the SQL wildcard matching
               ((ExpressionDefault)filter.getValue()).accept(this);
               out.write(" LIKE ");
               out.write("'"+pattern+"'");
               //if (pattern.indexOf(esc) != -1) { //if it uses the escape char
               //out.write(" ESCAPE " + "'" + esc + "'");  //this needs testing
               //} TODO figure out when to add ESCAPE clause, probably just for the '_' char.
           } catch (java.io.IOException ioe){
               log.warning("Unable to export filter" + ioe);
               }*/
    }

    /**
     * Writes the SQL for the Logic Filter.
     *
     * @param filter the logic statement to be turned into SQL.
     */
    public void visit(LogicFilter filter) {
        log.finer("exporting LogicFilter");

        filter.getFilterType();

        String type = (String) logical.get(new Integer(filter.getFilterType()));

        try {
            java.util.Iterator list = filter.getFilterIterator();

            if (filter.getFilterType() == AbstractFilter.LOGIC_NOT) {
                out.write(" NOT (");
                ((AbstractFilter) list.next()).accept(this);
                out.write(")");
            } else { //AND or OR
                out.write("(");

                while (list.hasNext()) {
                    ((AbstractFilter) list.next()).accept(this);

                    if (list.hasNext()) {
                        out.write(" " + type + " ");
                    }
                }

                out.write(")");
            }
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the SQL for a Compare Filter.
     *
     * @param filter the comparison to be turned into SQL.
     */
    public void visit(CompareFilter filter) {
        log.finer("exporting SQL ComparisonFilter");

        DefaultExpression left = (DefaultExpression) filter.getLeftValue();
        DefaultExpression right = (DefaultExpression) filter.getRightValue();
        log.finer("Filter type id is " + filter.getFilterType());
        log.finer("Filter type text is " +
            comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            left.accept(this);
            out.write(" " + type + " ");
            right.accept(this);
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the SQL for the Geometry Filter.
     *
     * @param filter the geometry logic to be turned into SQL.
     */

    //TODO: Implement this function...or declare abstract and have children implement.
    public void visit(GeometryFilter filter) {
        //if implementing BBox for use with an sql datasource be
        //sure to implement an equals method for the BBoxExpression, 
        //as there is none now, and it is needed to test the unpacking
    }

    /**
     * Writes the SQL for the Null Filter.
     *
     * @param filter the null filter to be written to SQL.
     */
    public void visit(NullFilter filter) {
        log.finer("exporting NullFilter");

        DefaultExpression expr = (DefaultExpression) filter.getNullCheckValue();

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            expr.accept(this);
            out.write(" IS NULL ");
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the SQL for the attribute Expression.
     *
     * @param expression the attribute to turn to SQL.
     */
    public void visit(AttributeExpression expression) {
        log.finer("exporting ExpressionAttribute");

        try {
            out.write("\"" + expression.getAttributePath() + "\"");
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export expresion" + ioe);
        }
    }

    /**
     * Writes the SQL for the attribute Expression.
     *
     * @param expression the attribute to turn to SQL.
     */
    public void visit(Expression expression) {
        log.warning("exporting unknown (default) expression");
    }

    /**
     * Export the contents of a Literal Expresion
     *
     * @param expression the Literal to export
     *
     * @task TODO: Fully support GeometryExpression literals
     */
    public void visit(LiteralExpression expression) {
        log.finer("exporting LiteralExpression");

        try {
            Object literal = expression.getLiteral();
            short type = expression.getType();

            switch (type) {
            case DefaultExpression.LITERAL_DOUBLE:
            case DefaultExpression.LITERAL_INTEGER:
                out.write(literal.toString());

                break;

            case DefaultExpression.LITERAL_STRING:
                out.write("'" + literal + "'");

                break;

                //case LITERAL_GEOMETRY is SQL implementation specific.	    
            }
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export expresion" + ioe);
        }
    }

    /**
     * Writes the SQL for the Math Expression.
     *
     * @param expression the Math phrase to be written.
     */
    public void visit(MathExpression expression) {
        log.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            ((DefaultExpression) expression.getLeftValue()).accept(this);
            out.write(" " + type + " ");
            ((DefaultExpression) expression.getRightValue()).accept(this);
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export expresion" + ioe);
        }
    }

    public void visit(FunctionExpression expression) {
        log.warning("Unable to export functions");
    }
}
