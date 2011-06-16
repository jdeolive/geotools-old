package org.geotools.jdbc;

import org.geotools.filter.FunctionImpl;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A test function used for jdbc tests.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class WKTEqualsFunction extends FunctionImpl {

    public WKTEqualsFunction() {
        setName("__wktEquals");
    }
    
    @Override
    public Object evaluate(Object object) {
        PropertyName name = (PropertyName) getParameters().get(0);
        String wkt = getParameters().get(1).evaluate(null, String.class);
        
        Geometry g = name.evaluate(object, Geometry.class);
        return g != null && g.toText().equals(wkt);
    }

}
