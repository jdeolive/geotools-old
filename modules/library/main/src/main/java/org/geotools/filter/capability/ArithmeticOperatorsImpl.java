package org.geotools.filter.capability;

import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.Functions;

/**
 * Implementation of the ArithmeticOperators interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ArithmeticOperatorsImpl implements ArithmeticOperators {

    boolean simpleArithmetic;
    FunctionsImpl functions;

    public ArithmeticOperatorsImpl() {
        this.simpleArithmetic = false;
        this.functions = new FunctionsImpl();
    }

    public ArithmeticOperatorsImpl( boolean simpleArtithmetic, Functions functions ) {
        this.simpleArithmetic = simpleArtithmetic;
        this.functions = toFunctionsImpl( functions );
    }
    
    public ArithmeticOperatorsImpl( ArithmeticOperators copy ) {
        this.simpleArithmetic = copy.hasSimpleArithmetic();
        this.functions = copy.getFunctions() == null ? new FunctionsImpl() :
            new FunctionsImpl( copy.getFunctions() );
    }

    public void setSimpleArithmetic( boolean simpleArithmetic ) {
        this.simpleArithmetic = simpleArithmetic;
    }
    public boolean hasSimpleArithmetic() {
        return simpleArithmetic;
    }

    public FunctionsImpl getFunctions() {
        return functions;
    }
    
    private static FunctionsImpl toFunctionsImpl( Functions functions ) {
        if( functions == null ){
            return new FunctionsImpl();
        }
        if( functions instanceof FunctionsImpl ){
            return (FunctionsImpl) functions;
        }
        else {
            return new FunctionsImpl( functions );
        }
    }
    
    public void addAll( ArithmeticOperators copy ){
        if( copy == null ) return;        
        getFunctions().addAll( copy.getFunctions());
        if( copy.hasSimpleArithmetic() ){
            this.simpleArithmetic = true;
        }        
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ArithmeticOperators[");
        if( simpleArithmetic ){
            buf.append("simpleArithmetic=true");
        }
        buf.append("]");
        if( functions != null ){
            buf.append(" with functions");            
        }
        return buf.toString();
    }
}
