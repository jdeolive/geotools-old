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

import java.io.Writer;

// J2SE dependencies
import java.util.logging.Logger;


/**
 * Exports a filter as a OGC XML Filter document.
 *
 * @author jamesm
 *
 * @task HACK: Attrrbutes are not yet supported
 * @task TODO: Support full header information for new XML file
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static java.util.HashMap comparisions = new java.util.HashMap();
    private static java.util.HashMap spatial = new java.util.HashMap();
    private static java.util.HashMap logical = new java.util.HashMap();
    private static java.util.HashMap expressions = new java.util.HashMap();

    static {
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS),
            "PropertyIsEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN),
            "PropertyIsGreaterThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),
            "PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN),
            "PropertyIsLessThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),
            "PropertyIsLessThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.LIKE), "PropertyIsLike");
        comparisions.put(new Integer(AbstractFilter.NULL), "PropertyIsNull");
        comparisions.put(new Integer(AbstractFilter.BETWEEN),
            "PropertyIsBetween");

        expressions.put(new Integer(DefaultExpression.MATH_ADD), "Add");
        expressions.put(new Integer(DefaultExpression.MATH_DIVIDE), "Div");
        expressions.put(new Integer(DefaultExpression.MATH_MULTIPLY), "Mul");
        expressions.put(new Integer(DefaultExpression.MATH_SUBTRACT), "Sub");
        expressions.put(new Integer(DefaultExpression.FUNCTION), "Function");

        //more to come
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

        logical.put(new Integer(AbstractFilter.LOGIC_AND), "And");
        logical.put(new Integer(AbstractFilter.LOGIC_OR), "Or");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT), "Not");
    }

    private Writer out;

    /**
     * Creates a new instance of XMLEncoder
     *
     * @param out The writer to write to.
     * @param filter the filter to encode.
     */
    public XMLEncoder(Writer out, Filter filter) {
        this.out = out;

        try {
            out.write("<Filter>\n");
            filter.accept(this);
            out.write("</Fitler>");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * This should never be called. This can only happen if a subclass of
     * AbstractFilter failes to implement its own version of
     * accept(FilterVisitor);
     *
     * @param filter The filter to visit
     */
    public void visit(Filter filter) {
        LOGGER.warning("exporting unknown filter type");
    }

    public void visit(BetweenFilter filter) {
        LOGGER.finer("exporting BetweenFilter");

        Expression left = (Expression) filter.getLeftValue();
        Expression right = (Expression) filter.getRightValue();
        Expression mid = (Expression) filter.getMiddleValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " +
            comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            mid.accept(this);
            out.write("<LowerBoundary>\n");
            left.accept(this);
            out.write("</LowerBoundary>\n<UpperBoundary>\n");
            right.accept(this);
            out.write("</UpperBoundary>\n");
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(LikeFilter filter) {
        LOGGER.finer("exporting like filter");

        try {
            String wcm = filter.getWildcardMulti();
            String wcs = filter.getWildcardSingle();
            String esc = filter.getEscape();
            out.write("<PropertyIsLike wildCard=\"" + wcm + "\" singleChar=\"" +
                wcs + "\" escapeChar=\"" + esc + "\">\n");
            ((Expression) filter.getValue()).accept(this);
            out.write("<Literal>\n" + filter.getPattern() + "\n</literal>\n");
            out.write("</PropertyIsLike>\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(LogicFilter filter) {
        LOGGER.finer("exporting LogicFilter");

        filter.getFilterType();

        String type = (String) logical.get(new Integer(filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");

            java.util.Iterator list = filter.getFilterIterator();

            while (list.hasNext()) {
                ((AbstractFilter) list.next()).accept(this);
            }

            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(CompareFilter filter) {
        LOGGER.finer("exporting ComparisonFilter");

        Expression left = filter.getLeftValue();
        Expression right = filter.getRightValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " +
            comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            LOGGER.fine("exporting left expression " + left);
            left.accept(this);
            LOGGER.fine("exporting right expression " + right);
            right.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(GeometryFilter filter) {
        LOGGER.finer("exporting GeometryFilter");

        Expression left = filter.getLeftGeometry();
        Expression right = filter.getRightGeometry();
        String type = (String) spatial.get(new Integer(filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            left.accept(this);
            right.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(NullFilter filter) {
        LOGGER.finer("exporting NullFilter");

        Expression expr = (Expression) filter.getNullCheckValue();

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            expr.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void visit(AttributeExpression expression) {
        LOGGER.finer("exporting ExpressionAttribute");

        try {
            out.write("<PropertyName>" + expression.getAttributePath() +
                "</PropertyName>\n");
        } catch (java.io.IOException ioe) {
            LOGGER.finer("Unable to export expresion: " + ioe);
        }
    }

    public void visit(Expression expression) {
        LOGGER.warning("exporting unknown (default) expression");
    }

    /**
     * Export the contents of a Literal Expresion
     *
     * @param expression the Literal to export
     *
     * @task TODO: Fully support GeometryExpressions so that they are writen as
     *       GML.
     */
    public void visit(LiteralExpression expression) {
        LOGGER.finer("exporting LiteralExpression");

        try {
            out.write("<Literal>" + expression.getLiteral() + "</Literal>\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion" + ioe);
        }
    }

    public void visit(MathExpression expression) {
        LOGGER.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            out.write("<" + type + ">\n");
            ((Expression) expression.getLeftValue()).accept(this);
            ((Expression) expression.getRightValue()).accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }

    public void visit(FunctionExpression expression) {
        LOGGER.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            out.write("<" + type + " name = " + expression.getName() + ">\n");

            Expression[] args = expression.getArgs();

            for (int i = 0; i < args.length; i++) {
                args[i].accept(this);
            }

            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }
}
