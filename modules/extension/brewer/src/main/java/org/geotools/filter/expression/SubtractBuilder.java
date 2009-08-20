package org.geotools.filter.expression;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Subtract;

public class SubtractBuilder implements Builder<Subtract> {

    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    boolean unset = false;

    Expression expr1;

    Expression expr2;

    public SubtractBuilder() {
        reset();
    }

    public SubtractBuilder(Subtract expression) {
        reset(expression);
    }

    public SubtractBuilder reset() {
        unset = false;
        expr1 = Expression.NIL;
        expr2 = Expression.NIL;
        return this;
    }

    public SubtractBuilder reset(Subtract original) {
        unset = false;
        expr1 = original.getExpression1();
        expr2 = original.getExpression2();
        return this;
    }

    public SubtractBuilder unset() {
        unset = true;
        expr1 = null;
        expr2 = null;
        return this;
    }
    
    public Subtract build() {
        if (unset) {
            return null;
        }
        return ff.subtract(expr1, expr2);
    }
    
    public ChildExpressionBuilder<SubtractBuilder> expr1(){
        return new ChildExpressionBuilder<SubtractBuilder>(this, expr1) {            
            public Expression build() {
                expr1 = _build();
                return expr1;
            }
        };
    }
    public SubtractBuilder expr1( Object literal ){
        expr1 = ff.literal( literal );
        return this;
    }
    public ChildExpressionBuilder<SubtractBuilder> expr2(){
        return new ChildExpressionBuilder<SubtractBuilder>(this, expr1) {            
            public Expression build() {
                expr2 = _build();
                return expr1;
            }
        };
    }
    public SubtractBuilder expr2( Object literal ){
        expr2 = ff.literal( literal );
        return this;
    }
}