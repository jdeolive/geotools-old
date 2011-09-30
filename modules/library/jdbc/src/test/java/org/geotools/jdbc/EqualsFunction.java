package org.geotools.jdbc;

import org.geotools.filter.FunctionImpl;
import org.geotools.util.Converters;
import org.opengis.filter.expression.PropertyName;

/**
 * A test function used for jdbc tests.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class EqualsFunction extends FunctionImpl {

    public EqualsFunction() {
        setName("__equals");
    }
    
    @Override
    public Object evaluate(Object object) {
        PropertyName name = (PropertyName) getParameters().get(0);
        Object literal = getParameters().get(1).evaluate(null);
        
        Object o = name.evaluate(object);
        return o.equals(Converters.convert(literal, o.getClass()));
    }

}
