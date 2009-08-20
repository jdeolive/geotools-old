package org.geotools.filter.expression;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Multiply;


public class MultiplyBuilder implements Builder<Multiply> {

    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    boolean unset = false;

    Expression expr1;

    Expression expr2;

    public MultiplyBuilder() {
        reset();
    }

    public MultiplyBuilder(Multiply expression) {
        reset(expression);
    }

    public MultiplyBuilder reset() {
        unset = false;
        expr1 = Expression.NIL;
        expr2 = Expression.NIL;
        return this;
    }

    public MultiplyBuilder reset(Multiply original) {
        unset = false;
        expr1 = original.getExpression1();
        expr2 = original.getExpression2();
        return this;
    }

    public MultiplyBuilder unset() {
        unset = true;
        expr1 = null;
        expr2 = null;
        return this;
    }
    
    public Multiply build() {
        if (unset) {
            return null;
        }
        return ff.multiply(expr1, expr2);
    }
    
    public ChildExpressionBuilder<MultiplyBuilder> expr1(){
        return new ChildExpressionBuilder<MultiplyBuilder>(this, expr1) {            
            public Expression build() {
                expr1 = _build();
                return expr1;
            }
        };
    }
    public MultiplyBuilder expr1( Object literal ){
        expr1 = ff.literal( literal );
        return this;
    }
    public ChildExpressionBuilder<MultiplyBuilder> expr2(){
        return new ChildExpressionBuilder<MultiplyBuilder>(this, expr1) {            
            public Expression build() {
                expr2 = _build();
                return expr1;
            }
        };
    }
    public MultiplyBuilder expr2( Object literal ){
        expr2 = ff.literal( literal );
        return this;
    }
}