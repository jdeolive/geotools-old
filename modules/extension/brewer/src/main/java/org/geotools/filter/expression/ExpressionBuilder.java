package org.geotools.filter.expression;


import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * ExpressionBuilder acting as a simple wrapper around an Expression.
 */
public class ExpressionBuilder implements Builder<Expression> {
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
    protected boolean unset = false;
    protected Builder<? extends Expression> delegate = new NilBuilder();
    
    public ExpressionBuilder(){
        reset();    
    }
    public ExpressionBuilder( Expression expr ){
        reset( expr );
    }
    
    /**
     * Simple chaining to a known delegate.
     * <p>
     * Example:<code>b.literal().value( 1 );</code>
     */
    public LiteralBuilder literal(){
        delegate = new LiteralBuilder();
        unset = false;
        return (LiteralBuilder) delegate;
    }
    /**
     * Short cut: chaining to a known LiteralBuilder delegate.
     * <p>
     * Example:<code>b.literal( 1 );</code>
     * @param obj Object to use as the resulting literal
     */
    public LiteralBuilder literal( Object obj ){
        delegate = new LiteralBuilder().value( obj );
        unset = false;
        return (LiteralBuilder) delegate;
    }
    
    public PropertyNameBuilder property(){
        delegate = new PropertyNameBuilder();
        unset = false;
        return (PropertyNameBuilder) delegate;
    }
    
    public ExpressionBuilder property( String xpath ){
        delegate = new PropertyNameBuilder().property( xpath );
        unset = false;
        return this;
    }
    
    public FunctionBuilder function(){
        this.delegate = new FunctionBuilder();
        unset = false;
        return (FunctionBuilder) delegate;        
    }
            
    public Expression build() {
        if( unset ) {
            return null;
        }
        return delegate.build();
    }

    public ExpressionBuilder reset() {
        this.delegate = new NilBuilder();
        this.unset = false;
        return this;
    }

    public ExpressionBuilder reset(Expression original) {
        if( original == null ){
            return unset();
        }
        this.unset = false;
        if( original instanceof Literal){
            delegate = new LiteralBuilder( (Literal) original );
        }
        else if( original instanceof PropertyName){
            delegate = new PropertyNameBuilder( (PropertyName) original );
        }
        else if( original instanceof Function){
            delegate = new FunctionBuilder( (Function) original );
        }
        else {
            this.delegate = new NilBuilder();
        }
        return this;
    }

    public ExpressionBuilder unset() {
        this.unset = true;
        this.delegate = new NilBuilder();
        return this;
    }

}
