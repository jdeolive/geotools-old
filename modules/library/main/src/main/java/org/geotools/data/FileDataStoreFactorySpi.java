/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import java.io.IOException;
import java.net.URL;


/**
 * <p>
 * This interface includes some new functionality, and acts as a method of
 * discovery for DataStoreFactories which support singular files.
 * </p>
 *
 * @author dzwiers
 * @source $URL$
 */
public interface FileDataStoreFactorySpi extends DataStoreFactorySpi {
    /**
     * The list of filename extentions handled by this factory.
     *
     * @return An ordered list of file extensions which can be read by this
     *         dataStore.
     */
    public String[] getFileExtensions();

    /**
     * True if the url can be handled by this factory.
     *
     * @param f URL a url to a real file (may not be local)
     *
     * @return True when this dataStore can resolve and read the data specified
     *         by the URL.
     */
    public boolean canProcess(URL f);

    /**
     * A DataStore attached to the provided url, may be created if needed.
     *
     * @param url A URL to the data location for the single featureType of this
     *        DataStore
     *
     * @return Returns an AbstractFileDataStore created from the data source
     *         provided.
     *
     * @throws IOException
     *
     * @see AbstractFileDataStore
     */
    public DataStore createDataStore(URL url) throws IOException;

    /**
     * The typeName represented by the provided url.
     *
     * @param url The location of the datum to parse into features
     *
     * @return Returns the typename of the datum specified (on occasion this
     *         may involve starting the parse as well to get the FeatureType
     *         -- may not be instantanious).
     *
     * @throws IOException
     */
    public String getTypeName(URL url) throws IOException;
}
