/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.complex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * {@link DataAccessFactory} for the ComplexDataStore implementation.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class ComplexDataAccessFactory extends ComplexDataStoreFactory implements DataAccessFactory {

    public boolean canAccess(Object bean) {
        if (!(bean instanceof Map)) {
            return false;
        }
        return super.canProcess((Map) bean);
    }

    public boolean canCreateContent(Object arg0) {
        return false;
    }

    public DataAccess createAccess(Object params) throws IOException {
        FeatureAccess store = (FeatureAccess) super.createDataStore((Map) params);
        return store;
    }

    public Object createAccessBean() {
        return new HashMap();
    }

    public DataAccess createContent(Object params) {
        throw new UnsupportedOperationException();
    }

    public Object createContentBean() {
        return null;
    }

    public InternationalString getName() {
        return new SimpleInternationalString(super.getDisplayName());
    }

}
