package org.geotools.repository;

import java.net.URI;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;

/**
 * Wraps up a {@link org.geotools.data.DataStoreFactorySpi}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DataStoreFactoryServiceFactory implements ServiceFactory {

	DataStoreFactorySpi factory;
	
	public DataStoreFactoryServiceFactory( DataStoreFactorySpi factory ) {
		this.factory = factory;
	}
	
	public Service createService(Catalog parent, URI id, Map params) {
		if ( factory.canProcess( params ) ) {
			return new DataStoreService( parent, params, factory );
		}
		
		return null;
	}

	public boolean canProcess(URI uri) {
		return false;
	}

	public Map createParams(URI uri) {
		return null;
	}

}
