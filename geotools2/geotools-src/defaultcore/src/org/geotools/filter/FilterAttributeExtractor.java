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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple visitor that extracts every attribute used by a filter or an expression
 *
 * @author wolf
 */
public class FilterAttributeExtractor implements FilterVisitor {
    protected Set attributeNames = new HashSet();

    /**
     * DOCUMENT ME!
     *
     * @return an unmofiable set of the attribute names found so far during the visit
     */
    public Set getAttributeNameSet() {
        return Collections.unmodifiableSet(attributeNames);
    }

    /**
     * DOCUMENT ME!
     *
     * @return an array of the attribute names found so far during the visit
     */
    public String[] getAttributeNames() {
        return (String[]) attributeNames.toArray(new String[attributeNames.size()]);
    }

    /**
     * Resets the attributes found so that a new attribute search can be performed
     */
    public void clear() {
        attributeNames = new HashSet();
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
     */
    public void visit(Filter filter) {
        if (filter instanceof BetweenFilter) {
            visit((BetweenFilter) filter);
        } else if (filter instanceof CompareFilter) {
            visit((CompareFilter) filter);
        } else if (filter instanceof GeometryFilter) {
            visit((GeometryFilter) filter);
        } else if (filter instanceof LikeFilter) {
            visit((LikeFilter) filter);
        } else if (filter instanceof LogicFilter) {
            visit((LogicFilter) filter);
        } else if (filter instanceof NullFilter) {
            visit((NullFilter) filter);
        } else if (filter instanceof FidFilter) {
            visit((FidFilter) filter);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
     */
    public void visit(BetweenFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }

        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }

        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
     */
    public void visit(CompareFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }

        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
     */
    public void visit(GeometryFilter filter) {
        if (filter.getLeftGeometry() != null) {
            filter.getLeftGeometry().accept(this);
        }

        if (filter.getRightGeometry() != null) {
            filter.getRightGeometry().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
     */
    public void visit(LikeFilter filter) {
        if (filter.getValue() != null) {
            filter.getValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
     */
    public void visit(LogicFilter filter) {
        for (Iterator it = filter.getFilterIterator(); it.hasNext();) {
            Filter f = (Filter) it.next();
            f.accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
     */
    public void visit(NullFilter filter) {
        if (filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
     */
    public void visit(FidFilter filter) {
        // nothing to do, the feature id is implicit and should always be
        // included, but cannot be derived from the filter itself 
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
     */
    public void visit(AttributeExpression expression) {
        attributeNames.add(expression.getAttributePath());
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
     */
    public void visit(Expression expression) {
        if (expression instanceof AttributeExpression) {
            visit((AttributeExpression) expression);
        } else if (expression instanceof LiteralExpression) {
            visit((LiteralExpression) expression);
        } else if (expression instanceof MathExpression) {
            visit((MathExpression) expression);
        } else if (expression instanceof FunctionExpression) {
            visit((FunctionExpression) expression);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
     */
    public void visit(LiteralExpression expression) {
        // nothing to do
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
     */
    public void visit(MathExpression expression) {
        if (expression.getLeftValue() != null) {
            expression.getLeftValue().accept(this);
        }

        if (expression.getRightValue() != null) {
            expression.getRightValue().accept(this);
        }
    }

    /**
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
     */
    public void visit(FunctionExpression expression) {
        Expression[] args = expression.getArgs();

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                args[i].accept(this);
            }
        }
    }
}
