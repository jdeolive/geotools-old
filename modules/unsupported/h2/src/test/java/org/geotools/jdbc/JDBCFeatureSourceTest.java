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
package org.geotools.jdbc;

import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;


public abstract class JDBCFeatureSourceTest extends JDBCTestSupport {
    ContentFeatureSource featureSource;

    protected void setUp() throws Exception {
        super.setUp();

        featureSource = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
    }

    public void testSchema() throws Exception {
        SimpleFeatureType schema = featureSource.getSchema();
        assertEquals("ft1", schema.getTypeName());
        assertEquals(dataStore.getNamespaceURI(), schema.getName().getNamespaceURI());
        assertEquals(CRS.decode("EPSG:4326"), schema.getCoordinateReferenceSystem());

        assertEquals(4, schema.getAttributeCount());
        assertNotNull(schema.getDescriptor("geometry"));
        assertNotNull(schema.getDescriptor("intProperty"));
        assertNotNull(schema.getDescriptor("stringProperty"));
        assertNotNull(schema.getDescriptor("doubleProperty"));
    }

    public void testBounds() throws Exception {
        ReferencedEnvelope bounds = featureSource.getBounds();
        assertEquals(0d, bounds.getMinX());
        assertEquals(0d, bounds.getMinY());
        assertEquals(2d, bounds.getMaxX());
        assertEquals(2d, bounds.getMaxY());

        assertEquals(CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem());
    }

    public void testBoundsWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property("stringProperty"), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setFilter(filter);

        ReferencedEnvelope bounds = featureSource.getBounds(query);
        assertEquals(1d, bounds.getMinX());
        assertEquals(1d, bounds.getMinY());
        assertEquals(1d, bounds.getMaxX());
        assertEquals(1d, bounds.getMaxY());

        assertEquals(CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem());
    }

    public void testCount() throws Exception {
        assertEquals(3, featureSource.getCount(Query.ALL));
    }

    public void testCountWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property("stringProperty"), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setFilter(filter);
        assertEquals(1, featureSource.getCount(query));
    }

    public void testGetFeatures() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures();
        assertEquals(3, features.size());
    }

    public void testGetFeaturesWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property("stringProperty"), ff.literal("one"));

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(filter);
        assertEquals(1, features.size());

        Iterator iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals(new Integer(1), feature.getAttribute("intProperty"));
        assertEquals("one", feature.getAttribute("stringProperty"));
    }

    public void testGetFeaturesWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property("stringProperty"), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setPropertyNames(new String[] { "doubleProperty", "intProperty" });
        query.setFilter(filter);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(query);
        assertEquals(1, features.size());

        Iterator iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals(2, feature.getAttributeCount());

        assertEquals(new Double(1.1), feature.getAttribute(0));
        assertEquals(new Integer(1), feature.getAttribute(1));
    }

    public void testGetFeaturesWithSort() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        SortBy sort = ff.sort("stringProperty", SortOrder.ASCENDING);
        DefaultQuery query = new DefaultQuery();
        query.setSortBy(new SortBy[] { sort });

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(query);
        assertEquals(3, features.size());

        Iterator iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature f = (SimpleFeature) iterator.next();
        assertEquals("one", f.getAttribute("stringProperty"));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("two", f.getAttribute("stringProperty"));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("zero", f.getAttribute("stringProperty"));

        features.close(iterator);

        sort = ff.sort("stringProperty", SortOrder.DESCENDING);
        query.setSortBy(new SortBy[] { sort });
        features = featureSource.getFeatures(query);

        iterator = features.iterator();
        assertTrue(iterator.hasNext());

        f = (SimpleFeature) iterator.next();
        assertEquals("zero", f.getAttribute("stringProperty"));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("two", f.getAttribute("stringProperty"));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("one", f.getAttribute("stringProperty"));
    }
    
    public void testGetFeaturesWithMax() throws Exception {
        DefaultQuery q = new DefaultQuery(featureSource.getSchema().getTypeName());
        q.setMaxFeatures(2);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(q);
        
        // check size
        assertEquals(2, features.size());
        
        // check actual iteration
        Iterator it = features.iterator();
        int count = 0;
        while(it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(2, count);
    }
}
