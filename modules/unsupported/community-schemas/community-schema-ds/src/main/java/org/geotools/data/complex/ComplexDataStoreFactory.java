/*
 *    GeoTools - The Open Source Java GIS Toolkit
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

package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.XMLConfigDigester;

/**
 * DataStoreFactory for ComplexDataStore.
 * 
 * NOTE: currently this one is not registered through the geotools datastore plugin mechanism. Instead, we're
 * directly using DataAccessFactory
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class ComplexDataStoreFactory implements DataStoreFactorySpi {

	public static final Param DBTYPE = new Param("dbtype", String.class, "Fixed value 'complex'", true, "complex");
	public static final Param URL = new Param("url", URL.class, "URL to a complex datastore XML configuration file", true);
	
	public ComplexDataStoreFactory() {
		// no-op
	}

	public DataStore createDataStore(Map params) throws IOException {
		Set/*<FeatureTypeMapping>*/ mappings;
		ComplexDataStore dataStore;

		URL configFileUrl = (URL)ComplexDataStoreFactory.URL.lookUp(params);
		XMLConfigDigester configReader = new XMLConfigDigester();
		ComplexDataStoreDTO config = configReader.parse(configFileUrl);
		mappings = ComplexDataStoreConfigurator.buildMappings(config);
		
		dataStore = new ComplexDataStore(mappings);
		
		return dataStore;
	}

	public DataStore createNewDataStore(Map params) throws IOException {
		throw new UnsupportedOperationException();
	}

	public String getDisplayName() {
		return "Complex DataStore.";
	}

	public String getDescription() {
		return "Complex DataStore allows mapping of FeatureTypes to externally defined Output Schemas";
	}

	public Param[] getParametersInfo() {
		return new Param[]{ComplexDataStoreFactory.DBTYPE, ComplexDataStoreFactory.URL};
	}

	public boolean canProcess(Map params) {
		try {
			Object dbType = ComplexDataStoreFactory.DBTYPE.lookUp(params);
			Object configUrl = ComplexDataStoreFactory.URL.lookUp(params);
			return "complex".equals(dbType) && configUrl != null;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return false;
	}

	public boolean isAvailable() {
		return true;
	}

	public Map getImplementationHints() {
		return Collections.EMPTY_MAP;
	}

}
