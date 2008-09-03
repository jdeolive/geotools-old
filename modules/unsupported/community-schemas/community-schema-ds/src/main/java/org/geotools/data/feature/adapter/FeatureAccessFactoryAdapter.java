/*
 *    Geotools2 - OpenSource mapping toolkit
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
 *
 */
package org.geotools.data.feature.adapter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.util.InternationalString;

/**
 * A {@link DataAccessFactory} that adapts any available geotools
 * {@link DataStore} to the {@link FeatureAccess} interface.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FeatureAccessFactoryAdapter implements DataAccessFactory {

    /**
     * @param expected
     *            {@link Map} instance suitable for a
     *            {@link DataStoreFactorySpi#canProcess(Map)}
     */
    public boolean canAccess(Object params) {
        if (!(params instanceof Map)) {
            return false;
        }
        DataStore dataStore = null;
        try {
            dataStore = DataStoreFinder.getDataStore((Map) params);
            return dataStore != null;
        } catch (IOException e) {
            return false;
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
    }

    public boolean canCreateContent(Object arg0) {
        return false;
    }

    public DataAccess createAccess(Object params) throws IOException {
        DataStore dataStore = DataStoreFinder.getDataStore((Map) params);
        if(dataStore instanceof DataAccess){
            return (DataAccess)dataStore;
        }
        SimpleFeatureFactory attributeFactory = new SimpleFeatureFactoryImpl();
        FeatureAccessAdapter adapter = new FeatureAccessAdapter(dataStore, attributeFactory);
        return adapter;
    }

    public Object createAccessBean() {
        return new HashMap();
    }

    public DataAccess createContent(Object bean) {
        throw new UnsupportedOperationException();
    }

    public Object createContentBean() {
        throw new UnsupportedOperationException();
    }

    public InternationalString getName() {
        return new SimpleInternationalString("FeatureAccess adapter for DataStores");
    }

    public boolean isAvailable() {
        return true;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

}
