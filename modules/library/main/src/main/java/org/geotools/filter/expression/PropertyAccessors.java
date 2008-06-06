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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.Hints;

/**
 * Convenience class for looking up a property accessor for a particular object type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class PropertyAccessors {
    static final List FACTORY_CACHE;
    
    static {
        FACTORY_CACHE = new ArrayList();

        // add the simple feature property accessor factory first for performance
        // reasons
        FACTORY_CACHE.add( new SimpleFeaturePropertyAccessorFactory());
         Iterator factories = FactoryRegistry
                 .lookupProviders(PropertyAccessorFactory.class);
         while (factories.hasNext()) {
            Object factory = factories.next();
            if ( factory instanceof SimpleFeaturePropertyAccessorFactory )
                continue;
            
            FACTORY_CACHE.add(factory);
         }
    }
    
    /**
     * Make sure this class won't be instantianted
     */
    private PropertyAccessors() {}

    /**
     * Looks up a {@link PropertyAccessor} for a particular object.
     * <p>
     * This method will return the first accessor that is capabile of handling the object and xpath
     * expression provided, no order is guaranteed.
     * </p>
     * 
     * @param object
     *            The target object.
     * @param xpath
     *            An xpath expression denoting a property of the target object.
     * @param hints
     *            Hints to pass on to factories.
     * 
     * @return A property accessor, or <code>null</code> if one could not be found.
     */
    public static PropertyAccessor findPropertyAccessor(Object object, String xpath, Class target,
            Hints hints) {
        if (object == null)
            return null;

        for (Iterator it = FACTORY_CACHE.iterator(); it.hasNext();) {
            PropertyAccessorFactory factory = (PropertyAccessorFactory) it.next();
            PropertyAccessor accessor = factory.createPropertyAccessor(object.getClass(), xpath,
                    target, hints);
            if (accessor != null && accessor.canHandle(object, xpath, target)) {
                return accessor;
            }
        }
        return null;
    }
}
