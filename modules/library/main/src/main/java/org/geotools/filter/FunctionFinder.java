/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.function.ClassificationFunction;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * Isolate function lookup code from Factory implementation(s).
 * <p>
 * This is done to look for two things:
 * <ul>
 * <li>org.geotools.filter.Function
 * <li>org.opengis.filter.expression.Function
 * </ul>
 * This is done as a proper utility class that accepts Hints.
 * 
 * @author Jody Garnett
 */
public class FunctionFinder {
    private Map functionExpressionCache;

    private Map functionImplCache;

    public FunctionFinder(Hints hints) {
        // currently hints are not used, need help :-P
    }

    public Function findFunction(String name) {
        return findFunction(name, null);
    }

    /**
     * Look up a function for the provided name, may return a FallbackFunction if
     * an implementation could not be found.
     * <p>
     * You can create a function to represent an SQL function or a function hosted on
     * an external service; the fallback value will be used if you evulate 
     * by a Java implementation on the classpath.
     * @param name Function name; this will need to be an exact match
     * @param parameters Set of Expressions to use as function parameters
     * @param fallbackValue Literal to use if an implementation could not be found
     * @return Function for the provided name, may be a FallbackFunction if an implementation could not be found
     */
    public Function findFunction(String name, List parameters, Literal fallbackValue ){
        try {
            Function function = findFunction( name, parameters );
            if( function instanceof FunctionImpl){
                FunctionImpl functionImpl = (FunctionImpl) function;
                functionImpl.setFallbackValue( fallbackValue );
            }
            if( function instanceof ClassificationFunction){
                ClassificationFunction classification = (ClassificationFunction) function;
                classification.setFallbackValue( fallbackValue );
            }
            return function;
        }
        catch( RuntimeException notFound ){
            // could not find an implementation
            return new FallbackFunction( name, parameters, fallbackValue );
        }
    }
    
    /**
     * Look up a function for the provided name.
     * 
     * @param name Function name; this will need to be an exact match
     * @param parameters Set of parameters required
     * @return Generated function
     * @throws a RuntimeException if an implementation for name could not be found
     */
    public Function findFunction(String name, List/* <Expression> */parameters) {
        name = functionName(name);

        try {
            // load the caches at first access
        	synchronized (this) {
			
	            if (functionExpressionCache == null) {
	                functionExpressionCache = new HashMap();
	                functionImplCache = new HashMap();
	                
	                Set functions = CommonFactoryFinder.getFunctionExpressions( null );                
	                for (Iterator it = functions.iterator(); it.hasNext();) {
	                    FunctionExpression function = (FunctionExpression) it.next();
	                    functionExpressionCache.put(function.getName().toLowerCase(), function.getClass());
	                }
	
	                functions = CommonFactoryFinder.getFunctions( null );                                
	                for (Iterator i = functions.iterator(); i.hasNext();) {
	                    FunctionImpl function = (FunctionImpl) i.next();
	                    functionImplCache.put(function.getName().toLowerCase(), function.getClass());
	                }
	            }
        	}
            
            // cache lookup
            Class clazz = (Class) functionExpressionCache.get(name.toLowerCase());
            if(clazz != null) {
                FunctionExpression function = (FunctionExpression) clazz.newInstance();
                if(parameters != null)
                    function.setParameters(parameters);
                return function;
            }
            clazz = (Class) functionImplCache.get(name.toLowerCase());
            if(clazz != null) {
                FunctionImpl function = (FunctionImpl) clazz.newInstance();
                if(parameters != null)
                    function.setParameters(parameters);
                return function;
            }    
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to create class " + name + "Function", e);
        }
        throw new RuntimeException("Unable to find function " + name);
    }

    private String functionName(String name) {
        int index = -1;

        if ((index = name.indexOf("Function")) != -1) {
            name = name.substring(0, index);
        }

        name = name.toLowerCase().trim();
        char c = name.charAt(0);
        name = name.replaceFirst("" + c, "" + Character.toUpperCase(c));

        return name;
    }
}
