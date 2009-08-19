package org.geotools.filter.expression;

import java.util.ArrayList;
import java.util.List;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;

public class FunctionBuilder implements Builder<Function> {
    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);    
    LiteralBuilder literal = new LiteralBuilder();
    boolean unset = false;
    private String name = null; // ie unset!
    List<Expression> args = new ArrayList<Expression>();
    
    public FunctionBuilder(){
        reset();
    }
    public FunctionBuilder(Function origional) {
        reset( origional );
    }
    
    ExpressionBuilder param(){
        return param( args.size() );
    }
    ExpressionBuilder param(int index){
        return new ExpressionBuilder( args.get(index) );
    }
    public FunctionBuilder name( String function ){
        this.name = function;
        return this;
    }
    /** Inline fallback value to use if named function is not implemented */
    public FunctionBuilder fallback( Object obj ){
        literal.value( obj );
        return this;        
    }
    /** Literal fallback value to use if named function is not implemented */
    public LiteralBuilder fallback(){
        return literal;        
    }
    
    public Function build() {
        if( name == null ){
            return null; // unset!
        }
        Expression[] arguments = new Expression[ args.size() ];
        for( int index = 0; index < args.size(); index++){
            arguments[index] = args.get( index );
        }
        return ff.function(name, arguments);
    }

    public FunctionBuilder reset() {
        name = null;
        args.clear();
        literal.unset();
        return this;
    }

    public FunctionBuilder reset(Function original) {
        name = original.getName();
        args.clear();
        args.addAll( original.getParameters() );
        literal.reset( original.getFallbackValue() );        
        return this;
    }

    public FunctionBuilder unset() {
        name = null;
        args.clear();        
        literal.unset();
        return this;
    }

}
