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

/**
 * Interface for a function expression implementation
 *
 * @author James Macgill, PSU
 */
public interface FunctionExpression extends Expression {
    /**
     * Gets the number of arguments that are set.
     *
     * @return the number of args.
     */
    int getArgCount();

    /**
     * Gets the type of this expression.
     *
     * @return the short representation of a function expression.
     */
    short getType();

    /**
     * Gets the arguments to be evaluated by this function.
     *
     * @return an array of the args to be evaluated.
     */
    Expression[] getArgs();

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     */
    String getName();

    /**
     * Sets the arguments to be evaluated by this function.
     *
     * @param args an array of expressions to be evaluated.
     */
    void setArgs(Expression[] args);
}
