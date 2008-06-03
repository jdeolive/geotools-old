package org.geotools.filter;

import java.util.List;

import org.geotools.filter.capability.FunctionsImpl;
import org.opengis.filter.capability.Functions;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.go.CommonFactory;

/**
 * FunctionFactory allowing access to both a Function implementation
 * and description.
 * <p>
 * This factory makes available symbology 
 * @author Jody
 */
public class FunctionFactory {
	FunctionsImpl functions = null;
	/**
	 * FilterCapabilities data structure that
	 * captures what functions are available via
	 * this Factory.
	 * 
	 * @return
	 */
	public synchronized Functions getFunctions(){
		if( functions == null ){
			functions = new FunctionsImpl();
			functions.getFunctionNames().add( CategorizeFunction.NAME );
		}
		return functions;
	}
	/**
	 * Create a function 
	 * @param name
	 * @return
	 */
	public Function createFunction( String name, List<Expression> parameters, Literal fallback ){
		if( "Categorize".equalsIgnoreCase(name)){
			return new CategorizeFunction(parameters, fallback);
		}
		else {
			return new FallbackFunction( name, parameters, fallback );
		}
	}
}
