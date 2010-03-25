/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.test;

import java.util.Properties;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

/**
 * JUnit 4 test support for test cases that require an "online" resource, such as an external server
 * or database.
 * 
 * <p>
 * 
 * See {@link OnlineTestCase} for details of behaviour and test fixture configuration.
 * 
 * <p>
 * 
 * Subclass names should end with "OnlineTest" to allow Maven to treat them specially.
 * 
 * <p>
 * 
 * This class is an adapter to {@link OnlineTestCase} that allows its use with JUnit 4. Delegation
 * is used to recycle the behaviour of {@link OnlineTestCase} without extending {@link TestCase}.
 * This is necessary because {@link TestRunner}s appear to give priority to JUnit 3 behaviour,
 * ignoring JUnit 4 annotations in suites that extend {@link TestCase}.
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 * @see OnlineTestCase
 */
public abstract class OnlineTestSupport {

    /**
     * The delegate {@link OnlineTestCase} instance.
     */
    private final DelegateOnlineTestCase delegate = new DelegateOnlineTestCase();

    @Before
    public void before() throws Exception {
        delegate.setUp();
        // disable test if fixture is null
        Assume.assumeNotNull(delegate.fixture);
    }

    @After
    public void after() throws Exception {
        delegate.tearDown();
    }

    /**
     * Subclasses must override this method to return a fixture id.
     * 
     * @return fixture id
     * @see OnlineTestCase#getFixtureId()
     */
    protected abstract String getFixtureId();

    /**
     * Override this method to connect to an online resource. Throw an exception on failure.
     * 
     * <p>
     * 
     * Subclasses do not have to override this method, but doing so allows builders to choose to
     * have this test disable itself when the online resource is not available.
     * 
     * @throws Exception
     * @see OnlineTestCase#connect()
     */
    protected void connect() throws Exception {
    }

    /**
     * Override this method to disconnect from an online resource. Throw an exception on failure.
     * 
     * @throws Exception
     * @see OnlineTestCase#disconnect()
     */
    protected void disconnect() throws Exception {
    }

    /**
     * Return properties configured in the fixture.
     * 
     * <p>
     * 
     * This method allows subclasses in other packages to access fixture properties.
     * 
     * @return properties configured in the fixture.
     */
    protected Properties getFixture() {
        return delegate.fixture;
    }

    /**
     * The delegate {@link OnlineTestCase} adapter.
     */
    private class DelegateOnlineTestCase extends OnlineTestCase {

        /**
         * @see org.geotools.test.OnlineTestCase#getFixtureId()
         */
        @Override
        protected String getFixtureId() {
            return OnlineTestSupport.this.getFixtureId();
        }

        /**
         * @see org.geotools.test.OnlineTestCase#connect()
         */
        @Override
        protected void connect() throws Exception {
            OnlineTestSupport.this.connect();
        }

        /**
         * @see org.geotools.test.OnlineTestCase#disconnect()
         */
        @Override
        protected void disconnect() throws Exception {
            OnlineTestSupport.this.disconnect();
        }

    }

}
