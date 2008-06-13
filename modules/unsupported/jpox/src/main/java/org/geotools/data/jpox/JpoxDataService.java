/*
 *    GeoTools - The Open Source Java GIS Toolkit
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
package org.geotools.data.jpox;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOHelper;

import org.geotools.catalog.ServiceInfo;
import org.geotools.catalog.defaults.DefaultServiceInfo;
import org.geotools.data.DataAccess;
import org.geotools.data.Source;
import org.jpox.PersistenceManagerFactoryImpl;
import org.jpox.metadata.MetaDataHelper;
import org.opengis.feature.type.TypeName;


public class JpoxDataService implements DataAccess/*<Class>*/ {

	private Properties jdoProps;
	
	private boolean initialized = false;
	
	private PersistenceManagerFactoryImpl pmf;

	private List typesList;
	private Map typesMap;
	
	public JpoxDataService( Properties jdoProps ) {
		this.jdoProps = jdoProps;
	}

	public JpoxDataService( PersistenceManagerFactoryImpl pmf ) {
		initInternal( pmf );
	}
	
	public JpoxDataService( String fileName ) {
		initInternal( (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( fileName ) );
	}

	public JpoxDataService( InputStream is ) {
		initInternal( (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( is ) );
	}

	public void initialize() {
		initialize( jdoProps );
	}
	
	public void initialize( Properties jdoProps ) {
		initInternal( (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( jdoProps ) );
	}
	
	private void initInternal( PersistenceManagerFactoryImpl pmf ) {
		this.pmf = pmf;
		initialized = true;
	}
	
	public void dispose() {
		//Close pmf? 
		pmf = null;
		jdoProps = null;
		initialized = false;
	}
	
	public ServiceInfo getInfo() {
		URI uri = null;
		try {
			uri = new URI( pmf.getConnectionURL() );
		} catch ( URISyntaxException e ) {
			// TODO: log and move on?
		}
		return new DefaultServiceInfo( "JPOX Data Access", "JPOX Data Access for types: " + getNames(), null, uri, null, null, new String[] {}, null );
	}
	
	public Source access( TypeName typeName ) {
		Class pc = (Class)describe( typeName );
		if ( pc == null ) return null;
		
		return new JpoxPojoSource( pmf, pc );
	}

	public Object describe( TypeName typeName ) {
		return getTypesMap().get( typeName );
	}

	public List getNames() {
		if ( typesList == null ) {
			typesList = Collections.unmodifiableList( new ArrayList( getTypesMap().keySet() ) );			
		}

		return typesList;
	}

	private Map getTypesMap() {
		checkInitialized();

		if ( typesMap != null ) return typesMap;

		typesMap = new HashMap();
		
		ClassLoader cl = getClass().getClassLoader();
		Class c = null;

		String[] classNames = MetaDataHelper.getClassNames( pmf );
		
		for ( int i = 0; i < classNames.length; i++ ) {
			try {
				c = cl.loadClass(classNames[i]);
			} catch (ClassNotFoundException e) {
				//TODO: this is bad. Log!
				e.printStackTrace();
				continue;
			}
			TypeName typeName = new org.geotools.feature.type.TypeName( c.getName() );
			typesMap.put( typeName, c );
		}
		return typesMap;
	}

	private void checkInitialized() {
		if ( !initialized ) throw new IllegalStateException( "JpoxDataService not initialized!" );
	}
}
