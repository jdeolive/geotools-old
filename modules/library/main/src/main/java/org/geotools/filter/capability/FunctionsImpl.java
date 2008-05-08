package org.geotools.filter.capability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.capability.Functions;

/**
 * Implementation of the Functions interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FunctionsImpl implements Functions {

    Set<FunctionName> functionNames;
    
    public FunctionsImpl() {
        this( new ArrayList<FunctionName>());
    }
    public FunctionsImpl(Collection<FunctionName> functionNames) {
        this.functionNames = new HashSet<FunctionName>( functionNames );
    }
    public FunctionsImpl( FunctionName[] functionNames ) {
        if ( functionNames == null ) {
            functionNames = new FunctionName[]{};
        }
        
        this.functionNames = new HashSet<FunctionName>(
                Arrays.asList( functionNames ));
    }
    
    public FunctionsImpl( Functions copy ) {
        this.functionNames = new HashSet<FunctionName>();
        if( copy.getFunctionNames() != null ){
            for( FunctionName functionName : copy.getFunctionNames() ) {
                this.functionNames.add( new FunctionNameImpl( functionName ));
            }
        }
    }
    
    public Collection<FunctionName> getFunctionNames() {
        return functionNames;
    }
    
    public void setFunctionNames( Collection<FunctionName> functionNames ) {
        this.functionNames = new HashSet<FunctionName>(functionNames);
    }
    
    public FunctionName getFunctionName(String name) {
        if ( name == null || functionNames == null) {
            return null;
        }
        
        for ( FunctionName functionName : functionNames ) {
            if ( name.equals( functionName.getName() ) ) {
                return functionName;
            }
        }        
        return null;
    }

    public void addAll( Functions copy ) {
        if( copy == null ) return;        
        if( copy.getFunctionNames() != null ){
            for( FunctionName functionName : copy.getFunctionNames() ) {
                this.functionNames.add( new FunctionNameImpl( functionName ));
            }
        }
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("FunctionsImpl[");
        if( functionNames != null ){
            buf.append("with ");
            buf.append( functionNames.size() );
            buf.append(" functions");
        }
        buf.append("]");
        return buf.toString();
    }
}