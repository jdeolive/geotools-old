package org.geotools.repository.postgis;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.repository.Catalog;
import org.geotools.repository.Service;
import org.geotools.repository.ServiceFactory;

public class PostGISServiceFactory implements ServiceFactory {

	public Service createService( Catalog parent, URI id, Map params ) {	
		PostgisDataStoreFactory dataStoreFactory = new PostgisDataStoreFactory();
		if ( dataStoreFactory.canProcess( params ) ) {
			return new PostGISService( parent, params, dataStoreFactory );	
		}

		return null;
	}

	public boolean canProcess( URI uri ) {
		//jdbc style?
		if ( uri.getScheme() != null && uri.getScheme().startsWith( "jdbc:" ) ) {
			//make sure postgres driver
			if ( !uri.getScheme().startsWith( "jdbc:postgresql" ) ) 
				return false;
		}
			
		return uri.getHost() != null && uri.getPath() != null;
	}

	public Map createParams( URI uri ) {
		if ( canProcess( uri ) ) {
			HashMap params = new HashMap();
			params.put( PostgisDataStoreFactory.HOST, uri.getHost() );
			params.put( PostgisDataStoreFactory.DATABASE, uri.getPath() );
			
			if ( uri.getPort() != -1 ) {
				params.put( PostgisDataStoreFactory.PORT, new Integer( uri.getPort() ) );
			}
			
			if ( uri.getQuery() != null ) {
				String user = null;
				String pass = null;
				
				StringTokenizer st = new StringTokenizer( uri.getQuery(), "&" );
				while( st.hasMoreTokens() ) {
					String parameter = st.nextToken();
					if ( parameter.startsWith( "user=" ) ) {
						user = parameter.substring( "user=".length() );
					}
					if ( parameter.startsWith( "password=" ) ) {
						pass = parameter.substring( "password=".length() );
					}
				}
				
				if ( user != null ) {
					params.put( PostgisDataStoreFactory.USER, user );
				}
				
				if ( pass != null ) {
					params.put( PostgisDataStoreFactory.PASSWD, pass );
				}
			}
			
			return params;
		}
		
		return null;
	}

}
