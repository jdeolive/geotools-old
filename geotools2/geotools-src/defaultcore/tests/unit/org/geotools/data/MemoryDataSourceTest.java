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

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SampleFeatureFixtures;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;


/*
 * MemoryDataSourceTest.java
 * JUnit based test
 *
 * Created on May 16, 2003, 10:01 AM
 */
public class MemoryDataSourceTest extends TestCase {
    public MemoryDataSourceTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MemoryDataSourceTest.class);

        return suite;
    }

    /**
     * Test of addFeature method, of class org.geotools.data.MemoryDataSource.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testAddFeature() throws Exception {
        System.out.println("testAddFeature");

        MemoryDataSource source = new MemoryDataSource();
        Feature f = SampleFeatureFixtures.createFeature();
        source.addFeature(f);
        assertEquals(1, source.getFeatures(new DefaultQuery()).size());
    }

    /**
     * Test of addFeatures method, of class org.geotools.data.MemoryDataSource.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testAddFeatures() throws Exception {
        System.out.println("testAddFeatures");

        MemoryDataSource source = new MemoryDataSource();
        ArrayList features = new ArrayList(2);
        
        features.add(SampleFeatureFixtures.createFeature());
        features.add(SampleFeatureFixtures.createFeature());

        FeatureCollection fc = FeatureCollections.newCollection();
        fc.addAll(features);
        source.addFeatures(fc);
        assertEquals(2, source.getFeatures(new DefaultQuery()).size());
    }

    /**
     * Test of getBbox method, of class org.geotools.data.MemoryDataSource.
     */
    public void testGetBbox() {
        System.out.println("testGetBbox");

        MemoryDataSource source = new MemoryDataSource();
        Feature f = SampleFeatureFixtures.createFeature();
        source.addFeature(f);

        Envelope found = source.getBbox();
        Envelope expected = new Envelope(1, 1, 2, 2);
        assertEquals(expected, found);
    }

    /**
     * Test of getFeatures method, of class org.geotools.data.MemoryDataSource.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetFeatures() throws Exception {
        System.out.println("testGetFeature");

        MemoryDataSource source = new MemoryDataSource();
        Feature f = SampleFeatureFixtures.createFeature();
        source.addFeature(f);
        assertEquals(1, source.getFeatures(new DefaultQuery()).size());
        assertEquals(1, source.getFeatures(new DefaultQuery(Filter.NONE)).size());
        assertEquals(0, source.getFeatures(new DefaultQuery(Filter.ALL)).size());
    }

    /**
     * Test of getFeatures method, of class org.geotools.data.MemoryDataSource.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetFeaturesWithMaxSet() throws Exception {
        System.out.println("testGetFeature");

        MemoryDataSource source = new MemoryDataSource();
        Feature f = SampleFeatureFixtures.createFeature();
        source.addFeature(f);

        Feature f2 = SampleFeatureFixtures.createFeature();
        f2.setAttribute("testString", "A different String");
        source.addFeature(f2);

        assertEquals(2, source.getFeatures(new DefaultQuery()).size());

        DefaultQuery maxSet = new DefaultQuery();
        maxSet.setMaxFeatures(1);
        assertEquals(1, source.getFeatures(maxSet).size());
        maxSet.setMaxFeatures(2);
        assertEquals(2, source.getFeatures(maxSet).size());
        maxSet.setMaxFeatures(3);
        assertEquals(2, source.getFeatures(maxSet).size());
    }

    /**
     * Test of getSchema method, of class org.geotools.data.MemoryDataSource.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetSchema() throws Exception {
        System.out.println("testGetSchema");

        MemoryDataSource source = new MemoryDataSource();
        Feature f = SampleFeatureFixtures.createFeature();
        source.addFeature(f);
        assertEquals(f.getFeatureType(), source.getSchema());
    }

    /**
     * Test of createMetaData method, of class
     * org.geotools.data.MemoryDataSource.
     */
    public void testCreateMetaData() {
        System.out.println("testCreateMetaData");

        MemoryDataSource source = new MemoryDataSource();
        assertNotNull(source.getMetaData());
        assertTrue("should support fast bbox retreval",
            source.getMetaData().hasFastBbox());
        assertTrue("claims to support abort",
            !source.getMetaData().supportsAbort());
        assertTrue("claims to support rollback",
            !source.getMetaData().supportsRollbacks());
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
