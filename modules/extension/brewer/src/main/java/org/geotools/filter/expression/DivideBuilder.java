package org.geotools.filter.expression;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;

public class DivideBuilder implements Builder<Divide> {

    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    boolean unset = false;

    Expression expr1;

    Expression expr2;

    public DivideBuilder() {
        reset();
    }

    public DivideBuilder(Divide expression) {
        reset(expression);
    }

    public DivideBuilder reset() {
        unset = false;
        expr1 = Expression.NIL;
        expr2 = Expression.NIL;
        return this;
    }

    public DivideBuilder reset(Divide original) {
        unset = false;
        expr1 = original.getExpression1();
        expr2 = original.getExpression2();
        return this;
    }

    public DivideBuilder unset() {
        unset = true;
        expr1 = null;
        expr2 = null;
        return this;
    }
    
    public Divide build() {
        if (unset) {
            return null;
        }
        return ff.divide(expr1, expr2);
    }
    
    public ChildExpressionBuilder<DivideBuilder> expr1(){
        return new ChildExpressionBuilder<DivideBuilder>(this, expr1) {            
            public Expression build() {
                expr1 = _build();
                return expr1;
            }
        };
    }
    public DivideBuilder expr1( Object literal ){
        expr1 = ff.literal( literal );
        return this;
    }
    public ChildExpressionBuilder<DivideBuilder> expr2(){
        return new ChildExpressionBuilder<DivideBuilder>(this, expr1) {            
            public Expression build() {
                expr2 = _build();
                return expr1;
            }
        };
    }
    public DivideBuilder expr2( Object literal ){
        expr2 = ff.literal( literal );
        return this;
    }
}