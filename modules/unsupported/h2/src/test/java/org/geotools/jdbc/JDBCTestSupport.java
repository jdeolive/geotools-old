/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.geotools.feature.FeatureFactoryImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;

import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Test support class for jdbc test cases.
 * <p>
 * This test class fires up a live instance of an h2 database to provide a
 * live database to work with.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class JDBCTestSupport extends TestCase {
    /**
     * map of test setup class to boolean which tracks which 
     * setups can obtain a connection and which cannot
     */
    static Map dataSourceAvailable = new HashMap();
    
    static {
        //turn up logging
        /*
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").setLevel(Level.FINE);
        org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").addHandler(handler);
        */
    }

    protected JDBCTestSetup setup;
    protected JDBCDataStore dataStore;

    /**
     * Override to check if a database connection can be obtained, if not
     * tests are ignored.
     */
    public void run(TestResult result) {
        JDBCTestSetup setup = createTestSetup();
        
        //check if the data source is available for this setup
        Boolean available = 
            (Boolean) dataSourceAvailable.get( setup.getClass() );
        if ( available == null || available.booleanValue() ) {
            //test the connection
            try {
                DataSource dataSource = setup.createDataSource();
                Connection cx = dataSource.getConnection();
                cx.close();
            } catch (Throwable t) {
                dataSourceAvailable.put( setup.getClass(), Boolean.FALSE );
                return;
            }
            
            super.run(result);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        //create the test harness
        if (setup == null) {
            setup = createTestSetup();
        }

        setup.setUp();

        //initialize the database
        setup.initializeDatabase();

        //initialize the data
        setup.setUpData();

        FilterCapabilities filterCapabilities = new FilterCapabilities();
        filterCapabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
        filterCapabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
        filterCapabilities.addType(PropertyIsNull.class);
        filterCapabilities.addType(PropertyIsBetween.class);
        filterCapabilities.addType(Id.class);
        filterCapabilities.addType(IncludeFilter.class);
        filterCapabilities.addType(ExcludeFilter.class);
        filterCapabilities.addType(PropertyIsLike.class);

        //create the dataStore
        //TODO: replace this with call to datastore factory
        dataStore = new JDBCDataStore();
        dataStore.setSQLDialect(setup.createSQLDialect(dataStore));
        dataStore.setNamespaceURI("http://www.geotools.org/test");
        dataStore.setDataSource(setup.getDataSource());
        dataStore.setDatabaseSchema("geotools");
        dataStore.setFilterFactory(new FilterFactoryImpl());
        dataStore.setGeometryFactory(new GeometryFactory());
        dataStore.setFeatureFactory(new FeatureFactoryImpl());
        dataStore.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
        dataStore.setFilterCapabilities(filterCapabilities);

        setup.setUpDataStore(dataStore);
    }

    protected abstract JDBCTestSetup createTestSetup();

    protected void tearDown() throws Exception {
        super.tearDown();

        setup.tearDown();

        dataStore.dispose();
    }
}
