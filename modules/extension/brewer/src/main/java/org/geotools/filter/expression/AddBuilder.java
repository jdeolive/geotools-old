package org.geotools.filter.expression;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;

public class AddBuilder implements Builder<Add> {

    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    boolean unset = false;

    Expression expr1;

    Expression expr2;

    public AddBuilder() {
        reset();
    }

    public AddBuilder(Add expression) {
        reset(expression);
    }

    public AddBuilder reset() {
        unset = false;
        expr1 = Expression.NIL;
        expr2 = Expression.NIL;
        return this;
    }

    public AddBuilder reset(Add original) {
        unset = false;
        expr1 = original.getExpression1();
        expr2 = original.getExpression2();
        return this;
    }

    public AddBuilder unset() {
        unset = true;
        expr1 = null;
        expr2 = null;
        return this;
    }
    
    public Add build() {
        if (unset) {
            return null;
        }
        return ff.add(expr1, expr2);
    }
    
    public ChildExpressionBuilder<AddBuilder> expr1(){
        return new ChildExpressionBuilder<AddBuilder>(this, expr1) {            
            public Expression build() {
                expr1 = _build();
                return expr1;
            }
        };
    }
    public AddBuilder expr1( Object literal ){
        expr1 = ff.literal( literal );
        return this;
    }
    public ChildExpressionBuilder<AddBuilder> expr2(){
        return new ChildExpressionBuilder<AddBuilder>(this, expr1) {            
            public Expression build() {
                expr2 = _build();
                return expr1;
            }
        };
    }
    public AddBuilder expr2( Object literal ){
        expr2 = ff.literal( literal );
        return this;
    }
}
