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

import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;

import junit.framework.TestCase;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.jpox.PersistenceManagerFactoryImpl;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;

public class JpoxDataServiceTest extends TestCase {

	private FilterFactory ff;
	private JpoxDataService data;

	protected void setUp() throws Exception {
		PersistenceManagerFactoryImpl pmf = (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( "jdo.properties" );
		data = new JpoxDataService( pmf );

		ff = CommonFactoryFinder.getFilterFactory( null );
	}

	public void testData() {
		List types = data.getNames();
		assertNotNull( types );
		assertFalse( types.isEmpty() );
		assertTrue( types.get( 0 ) instanceof TypeName );

		TypeName typeName = (TypeName)data.getNames().get( 0 );
		Object descrption = data.describe( typeName );
		assertNotNull( descrption );
		assertTrue( descrption instanceof Class );
	}

	public void testSource() throws Exception {
//		List types = data.getTypeNames();
		TypeName typeName = (TypeName)data.getNames().get( 0 );
		Object descrption = data.describe( typeName );

		// Test "default access" using Transaction.AUTO_COMMIT
		Source source = data.access( typeName );

		assertNotNull( source );
		assertEquals( descrption, source.describe() );
		assertEquals( typeName, source.getName() );

		Collection content = source.content();
		assertNotNull( content );
		assertFalse( content.isEmpty() );

		// test concurrency - source2 uses seperate Transaction
		Source source2 = data.access( typeName );

		Transaction t = new DefaultTransaction( "Source Testing" );
		source2.setTransaction( t );

		Collection content2 = source2.content();
		assertNotNull( content2 );
		assertNotSame( content, content2 );
		assertEquals( content, content2 );
		assertEquals( content.size(), content2.size() );
	}

	public void testSource2() throws Exception {
		TypeName typeName1 = (TypeName)data.getNames().get( 0 );
//		TypeName typeName2 = (TypeName)data.getNames().get( 1 );

		Transaction t = new DefaultTransaction( "Source Testing" );

		Source source1 = data.access( typeName1 );
//		Source source2 = data.access( typeName2 );

		source1.setTransaction( t );
//		source2.setTransaction( t );

		Collection victoriaGeneral = source1.content( ff.equals( ff.property( "name" ), ff.literal( "Victoria General Hospital" ) ) );
		assertEquals( 1, victoriaGeneral.size() );
	}
}
