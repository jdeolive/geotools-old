/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.expression;

import org.geotools.factory.Hints;
import org.opengis.feature.Property;

/**
 * This class will *directly* access a Property with the name equal to xpath.
 * 
 * @author Jody Garnett
 */
public class DirectPropertyAccessorFactory implements PropertyAccessorFactory {

    static PropertyAccessor DIRECT = new DirectPropertyAccessor();

    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {
        
        if( Property.class.isAssignableFrom( type )){
            return DIRECT;
        }
        return null;
    }

    
    /**
     * Grab a value from a Property with matching name.
     * <p>
     * This restriction is used by Types.validate to ensure
     * the provided value is good.
     * 
     * @author Jody Garnett (Refractions Research Inc)
     */
    static class DirectPropertyAccessor implements PropertyAccessor {
        
        /**
         * We can handle *one* case and one case only 
         */
        public boolean canHandle(Object object, String xpath, Class target) {
            if( object instanceof Property ){
                Property property = (Property) object;
                if( property.getName() != null ){
                    return property.getName().getLocalPart().equals( xpath );
                }
                else {
                    // A property with no name? this is probably a place holder
                    // or Null Object (such as TransactionStateDiff.NULL).
                    return false;
                }
            }
            return false;
        }
        
        public Object get(Object object, String xpath, Class target)
                throws IllegalArgumentException {
            return ((Property)object).getValue();
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalArgumentException {
            ((Property)object).setValue( value );            
        }        
    }    
}
