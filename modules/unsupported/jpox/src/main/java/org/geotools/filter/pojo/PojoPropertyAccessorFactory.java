package org.geotools.filter.pojo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a property accessor for plain old java objects features.
 * <p>
 * The created accessor handles a small subset of xpath expressions:
 * <ul>
 * <li>"name" which corresponds to a bean property
 * </ul>
 */
public class PojoPropertyAccessorFactory implements
		PropertyAccessorFactory {
    
    public PropertyAccessor createPropertyAccessor( Class type, String xpath, Class target, Hints hints ) {
    	try {
	        if( Feature.class.isAssignableFrom( type ) ){
	            return null; // we are not wanting to work with features
	        }
	        if( "".equals(xpath) && target == Geometry.class){
		        BeanInfo info = Introspector.getBeanInfo( type );
	            PropertyDescriptor[] properties = info.getPropertyDescriptors();
	            for ( int i = 0; i < properties.length; i++ ){
	                PropertyDescriptor property = properties[i];
	                if( target.isAssignableFrom( property.getPropertyType() ) ) {
	                	return new PojoPropertyAccessor( info, property );
	                }
	            }
	        }
	        else if( !xpath.matches("^\\w(\\w)*$")){
	            return null; // that does not look simple
	        }
	        BeanInfo info;
            info = Introspector.getBeanInfo( type );
            PropertyDescriptor[] properties = info.getPropertyDescriptors();
            for ( int i = 0; i < properties.length; i++ ){
                PropertyDescriptor property = properties[i];
                if( xpath.equalsIgnoreCase(property.getName()) ){
                    return new PojoPropertyAccessor( info, property );
                }
            }
        } catch (IntrospectionException e) {
            return null; // no property access here
        }        
        return null; // not a pojo property - goodbye!
    }
    
}