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
/*
 * FunctionExpression.java
 *
 * Created on 28 July 2002, 15:18
 */
package org.geotools.filter;

/**
 * Abstract class for a function expression implementation
 *
 * @author James Macgill, PSU
 */
public abstract class FunctionExpressionImpl
    extends org.geotools.filter.DefaultExpression implements FunctionExpression {
    /**
     * Creates a new instance of FunctionExpression
     */
    protected FunctionExpressionImpl() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return the type FUNCTION
     */
    public short getType() {
        return FUNCTION;
    }

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     */
    public abstract String getName();

    /**
     * Sets the arguments to be evaluated by this function.
     *
     * @param args an array of expressions to be evaluated.
     */
    public abstract void setArgs(Expression[] args);

    /**
     * Gets the number of arguments that are set.
     *
     * @return the number of args.
     */
    public abstract int getArgCount();

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}
