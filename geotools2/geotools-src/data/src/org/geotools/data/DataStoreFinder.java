/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data;

import org.geotools.factory.FactoryFinder;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Enable programs to find all available datastore implementations.
 * 
 * <p>
 * In order to be located by this finder datasources must provide an
 * implementation of the {@link DataStoreFactorySpi} interface.
 * </p>
 * 
 * <p>
 * In addition to implementing this interface datasouces should have a services
 * file:<br/><code>META-INF/services/org.geotools.data.DataStoreFactorySpi</code>
 * </p>
 * 
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 * 
 * <p>
 * Example:<br/><code>org.geotools.data.mytype.MyTypeDataStoreFacotry</code>
 * </p>
 * 
 * <p>
 * The use of this class may be hidden by an implementation of the Catalog
 * interface in later release.
 * </p>
 */
public final class DataStoreFinder {
    private DataStoreFinder() {
    }

    /**
     * Checks each available datasource implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     *
     * @param params A Map object which contains a defenition of the resource
     *        to connect to. for file based resources the property 'url'
     *        should be set within this Map.
     *
     * @return The first datasource which claims to process the required
     *         resource, returns null if none can be found.
     *
     * @throws IOException If a suitable loader can be found, but it can not be
     *         attached to the specified resource without errors.
     */
    public static DataStore getDataStore(Map params) throws IOException {
        Iterator ps = getAvailableDataStores();

        while (ps.hasNext()) {
            DataStoreFactorySpi fac = (DataStoreFactorySpi) ps.next();

            if (fac.canProcess(params)) {
                return fac.createDataStore(params);
            }
        }

        return null;
    }

    /**
     * Finds all implemtaions of DataStoreFactory which have registered using
     * the services mechanism.
     *
     * @return An iterator over all discovered datastores which have registered
     *         factories.
     */
    public static Iterator getAvailableDataStores() {
        return FactoryFinder.factories(DataStoreFactorySpi.class);
    }
}
