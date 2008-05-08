package org.geotools.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.opengis.filter.expression.Function;

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
