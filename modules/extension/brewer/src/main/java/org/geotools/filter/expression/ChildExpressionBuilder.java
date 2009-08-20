package org.geotools.filter.expression;

import org.geotools.Builder;
import org.opengis.filter.expression.Expression;

/**
 * Child expression builder; suitable for use collecting function parameters and binary expression
 * arguments.
 * <p>
 * This builder is designed to be "chained" from a parent builder; you may return to the parent
 * builder at any time by calling end().
 * 
 * @param <P>
 *            parent builder
 */
public abstract class ChildExpressionBuilder<P extends Builder<?>> extends ExpressionBuilder {

    private P parent;

    public ChildExpressionBuilder(P parent, Expression expr1) {
        super( expr1 );
        this.parent = parent;
    }

    /** Build the parameter; adding it to the parent. */
    public abstract Expression build();

    /**
     * Build the expression and return to the parent builder.
     * <p>
     * Example use:<code>b.add().expr1().literal(1).end().expr2().literal(2).end().build();</code>
     * @see _build()
     * @return
     */
    public P end() {
        build();
        return parent;
    }
    /**
     * Internal method used to build the expression; called from build() as shown.
     * <p>
     * To use this method when implementing build:
     * <pre><code>final Expression array[] = ...
     * ChildExpressionBuilder first = new ChildExpressionBuilder<?>(this) {            
     *      public Expression build() {
     *          array[0] = _build();
     *          return array[0];
     *      }
     *  };
     * }</code></pre>
     * 
     * @return internal expression
     */
    protected Expression _build() {
        if (unset) {
            return null;
        }
        return delegate.build();
    }

    /**
     * Inline literal value.
     * <p>
     * Example:<code>b.literal( 1 );</code>
     * 
     * @param obj
     *            Object to use as the resulting literal
     */
    public P literal(Object obj) {
        literal().value(obj);
        return end();
    }

    /**
     * Inline property name value.
     * <p>
     * Example:<code>b.property("x");</code>
     */
    public P property(String xpath) {
        property().property(xpath);
        return end();
    }

}