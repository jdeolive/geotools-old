package org.geotools.filter.expression;

import org.opengis.filter.expression.Expression;

/**
 * An ExpressionBuilder that will splice the result into the
 * containing function when build is called.
 */
public class ParameterBuilder extends ExpressionBuilder {
    FunctionBuilder function;
    int index;
    
    ParameterBuilder( FunctionBuilder function, int index ){
        this.function = function;
        this.index = index;
    }
    
    /** Add the parameter to the function and proceed to the next one */
    public ParameterBuilder param(){
        build();
        return new ParameterBuilder( function, index+1 );
    }
    /**
     * Add the parameter to the function and return to the functionBuilder.
     * <p>
     * Example:<code>b.function("add").param().literal(1).param().propteryName("age").endParam().build();</code>
     * @return
     */
    public FunctionBuilder endParam(){
        build();
        return function;
    }
    /** Build the parameter; adding it to the function */
    public Expression build() {
        Expression expr = super.build();
        if( index < function.args.size() ){
            function.args.set(index, expr);
        }
        else if( index == function.args.size() ){
            function.args.add( expr );
        }
        return expr;
    }
}
