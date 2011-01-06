/*$************************************************************************************************
 **
 ** $Id: NilExpression.java 1133 2007-12-05 14:37:40Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/expression/NilExpression.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter.expression;

import java.io.ObjectStreamException;
import java.io.Serializable;


/**
 * Placeholder class used to represent a NIL expression, evaluates to {@code null}.
 * This placeholder class allows data structures to avoid the use of {@code null}.
 * Please note that {@link Expression#NIL} is not considered a Literal with value
 * {@code null} (since the literal may have its value changed).
 *
 * @author Jody Garnett (Refractions Research, Inc.)
 * @author Martin Desruisseaux (Geomatys)
 */
public final class NilExpression implements Expression, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 4999313240542653655L;

    /**
     * Not extensible.
     */
    NilExpression() {
    }

    /**
     * Accepts a visitor.
     */
    public Object accept(ExpressionVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

    /**
     * Returns {@code null}.
     */
    public Object evaluate(Object object) {
        return null;
    }

    /**
     * Returns {@code null}.
     */
    public <T> T evaluate(Object object, Class<T> context) {
        return null;
    }

    /**
     * Returns a string representation of this expression.
     */
    @Override
    public String toString() {
        return "Expression.NIL";
    }

    /**
     * Returns the canonical instance on deserialization.
     */
    private Object readResolve() throws ObjectStreamException {
        return Expression.NIL;
    }
}
