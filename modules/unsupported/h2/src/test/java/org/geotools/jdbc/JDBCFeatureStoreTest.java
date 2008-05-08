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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.CollectionFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;


public abstract class JDBCFeatureStoreTest extends JDBCTestSupport {
    JDBCFeatureStore featureStore;

    protected void setUp() throws Exception {
        super.setUp();

        featureStore = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
    }

    public void testAddFeatures() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(featureStore.getSchema());
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null,
                featureStore.getSchema());

        for (int i = 3; i < 6; i++) {
            b.set("intProperty", new Integer(i));
            b.set("geometry", new GeometryFactory().createPoint(new Coordinate(i, i)));
            collection.add(b.buildFeature(null));
        }

        Set fids = featureStore.addFeatures(collection);
        assertEquals(3, fids.size());

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(6, features.size());

        FilterFactory ff = dataStore.getFilterFactory();

        for (Iterator f = fids.iterator(); f.hasNext();) {
            String fid = (String) f.next();
            Id filter = ff.id(Collections.singleton(ff.featureId(fid)));

            features = featureStore.getFeatures(filter);
            assertEquals(1, features.size());

            Iterator iterator = features.iterator();
            assertTrue(iterator.hasNext());

            SimpleFeature feature = (SimpleFeature) iterator.next();
            assertEquals(fid, feature.getID());
            assertFalse(iterator.hasNext());

            features.close(iterator);
        }
    }

    public void testSetFeatures() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(featureStore.getSchema());
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null,
                featureStore.getSchema());

        for (int i = 3; i < 6; i++) {
            b.set("intProperty", new Integer(i));
            b.set("geometry", new GeometryFactory().createPoint(new Coordinate(i, i)));
            collection.add(b.buildFeature(null));
        }

         FeatureReader<SimpleFeatureType, SimpleFeature> reader = new CollectionFeatureReader(collection, collection.getSchema());
        featureStore.setFeatures(reader);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(3, features.size());

        Iterator iterator = features.iterator();
        HashSet numbers = new HashSet();
        numbers.add(new Integer(3));
        numbers.add(new Integer(4));
        numbers.add(new Integer(5));

        for (int i = 3; iterator.hasNext(); i++) {
            SimpleFeature feature = (SimpleFeature) iterator.next();
            assertTrue(numbers.contains(feature.getAttribute("intProperty")));
            numbers.remove(feature.getAttribute("intProperty"));
        }

        features.close(iterator);
    }

    public void testModifyFeatures() throws IOException {
        SimpleFeatureType t = featureStore.getSchema();
        featureStore.modifyFeatures(new AttributeDescriptor[] { t.getAttribute("intProperty") },
            new Object[] { new Integer(100) }, Filter.INCLUDE);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        Iterator i = features.iterator();

        assertTrue(i.hasNext());

        while (i.hasNext()) {
            SimpleFeature feature = (SimpleFeature) i.next();
            assertEquals(new Integer(100), feature.getAttribute("intProperty"));
        }

        features.close(i);
    }

    public void testRemoveFeatures() throws IOException {
        FilterFactory ff = dataStore.getFilterFactory();
        Filter filter = ff.equals(ff.property("intProperty"), ff.literal(1));

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(3, features.size());

        featureStore.removeFeatures(filter);
        assertEquals(2, features.size());

        featureStore.removeFeatures(Filter.INCLUDE);
        assertEquals(0, features.size());
    }
}
