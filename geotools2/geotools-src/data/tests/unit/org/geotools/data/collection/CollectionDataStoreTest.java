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
package org.geotools.data.collection;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * DOCUMENT ME!
 *
 * @author Jody Garnett, Refractions Research
 */
public class CollectionDataStoreTest extends DataTestCase {
    CollectionDataStore data;

    /**
     * Constructor for MemoryDataStoreTest.
     *
     * @param arg0
     */
    public CollectionDataStoreTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        data = new CollectionDataStore(DataUtilities.collection(roadFeatures));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        data = null;
    }

    public void testGetFeatureTypes() {
        String[] names = data.getTypeNames();
        assertEquals(1, names.length);
        assertTrue(contains(names, "road"));
    }

    boolean contains(Object[] array, Object expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Like contain but based on match rather than equals
     *
     * @param array DOCUMENT ME!
     * @param expected DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    boolean containsLax(Feature[] array, Feature expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        FeatureType type = expected.getFeatureType();

        for (int i = 0; i < array.length; i++) {
            if (match(array[i], expected)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compare based on attributes not getID allows comparison of Diff contents
     *
     * @param expected DOCUMENT ME!
     * @param actual DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    boolean match(Feature expected, Feature actual) {
        FeatureType type = expected.getFeatureType();

        for (int i = 0; i < type.getAttributeCount(); i++) {
            Object av = actual.getAttribute(i);
            Object ev = expected.getAttribute(i);

            if ((av == null) && (ev != null)) {
                return false;
            } else if ((ev == null) && (av != null)) {
                return false;
            } else if (av instanceof Geometry && ev instanceof Geometry) {
                Geometry ag = (Geometry) av;
                Geometry eg = (Geometry) ev;

                if (!ag.equals(eg)) {
                    return false;
                }
            } else if (!av.equals(ev)) {
                return false;
            }
        }

        return true;
    }

    public void testGetSchema() throws IOException {
        assertSame(roadType, data.getSchema("road"));
    }

    void assertCovers(String msg, FeatureCollection c1, FeatureCollection c2) {
        if (c1 == c2) {
            return;
        }

        assertNotNull(msg, c1);
        assertNotNull(msg, c2);
        assertEquals(msg + " size", c1.size(), c2.size());

        Feature f;

        for (FeatureIterator i = c1.features(); i.hasNext();) {
            f = i.next();
            assertTrue(msg + " " + f.getID(), c2.contains(f));
        }
    }

    public void testGetFeatureReader() throws IOException, IllegalAttributeException {
        FeatureReader reader = data.getFeatureReader("road");
        assertCovered(roadFeatures, reader);
        assertEquals(false, reader.hasNext());
    }

//    public void testGetFeatureReaderMutability() throws IOException, IllegalAttributeException {
//        FeatureReader reader = data.getFeatureReader("road");
//        Feature feature;
//
//        while (reader.hasNext()) {
//            feature = (Feature) reader.next();
//            feature.setAttribute("name", null);
//        }
//
//        reader.close();
//
//        reader = data.getFeatureReader("road");
//
//        while (reader.hasNext()) {
//            feature = (Feature) reader.next();
//            assertNotNull(feature.getAttribute("name"));
//        }
//
//        reader.close();
//
//        try {
//            reader.next();
//            fail("next should fail with an IOException");
//        } catch (IOException expected) {
//        }
//    }

    public void testGetFeatureReaderConcurancy()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureReader reader1 = data.getFeatureReader("road");
        FeatureReader reader2 = data.getFeatureReader("road");

        Feature feature1;
        Feature feature2;

        while (reader1.hasNext() || reader2.hasNext()) {
            assertTrue(contains(roadFeatures, reader1.next()));
            assertTrue(contains(roadFeatures, reader2.next()));
        }

        try {
            reader1.next();
            fail("next should fail with an IOException");
        } catch (IOException expected) {
        }

        try {
            reader2.next();
            fail("next should fail with an IOException");
        } catch (IOException expected) {
        }

        reader1.close();
        reader2.close();
    }

    public void testGetFeatureReaderFilterAutoCommit()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(new DefaultQuery("road"), Transaction.AUTO_COMMIT);
        assertFalse(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.ALL), Transaction.AUTO_COMMIT);
        assertTrue(reader instanceof EmptyFeatureReader);

        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), Transaction.AUTO_COMMIT);
        assertTrue(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));
    }

    public void testGetFeatureReaderFilterTransaction()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Transaction t = new DefaultTransaction();
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.ALL), t);
        assertTrue(reader instanceof EmptyFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road"), t);
        assertTrue(reader instanceof DiffFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertTrue(reader instanceof DiffFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));

        TransactionStateDiff state = (TransactionStateDiff) t.getState(data);
        FeatureWriter writer = state.writer("road");
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals("road.rd1")) {
                writer.remove();
            }
        }

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.ALL), t);
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road"), t);
        assertEquals(roadFeatures.length - 1, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertEquals(0, count(reader));

        t.rollback();
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.ALL), t);
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road"), t);
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertEquals(1, count(reader));
    }

    void assertCovered(Feature[] features, FeatureReader reader)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (reader.hasNext()) {
                assertTrue(contains(features, reader.next()));
                count++;
            }
        } finally {
            reader.close();
        }

        assertEquals(features.length, count);
    }

    /**
     * Ensure that FeatureReader reader contains extactly the contents of array.
     *
     * @param reader DOCUMENT ME!
     * @param array DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    boolean covers(FeatureReader reader, Feature[] array)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!contains(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    boolean coversLax(FeatureReader reader, Feature[] array)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!containsLax(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    void dump(FeatureReader reader)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();
                System.out.println(count + " feature:" + feature);
                count++;
            }
        } finally {
            reader.close();
        }
    }

    void dump(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.println(i + " feature:" + array[i]);
        }
    }

    // Feature Source Testing
    public void testGetFeatureSourceRoad() throws IOException {
        FeatureSource road = data.getFeatureSource("road");

        assertSame(roadType, road.getSchema());
        assertSame(data, road.getDataStore());
        assertEquals(3, road.getCount(Query.ALL));
        assertEquals(new Envelope(1, 5, 0, 4), road.getBounds(Query.ALL));

        FeatureResults all = road.getFeatures();
        assertEquals(3, all.getCount());
        assertEquals(roadBounds, all.getBounds());

        FeatureCollection expected = DataUtilities.collection(roadFeatures);

        assertCovers("all", expected, all.collection());
        assertEquals(roadBounds, all.collection().getBounds());

        FeatureResults some = road.getFeatures(rd12Filter);
        assertEquals(2, some.getCount());
        assertEquals(rd12Bounds, some.getBounds());
        assertEquals(some.getSchema(), road.getSchema());

        DefaultQuery query = new DefaultQuery(rd12Filter, new String[] { "name" });

        FeatureResults half = road.getFeatures(query);
        assertEquals(2, half.getCount());
        assertEquals(1, half.getSchema().getAttributeCount());

        FeatureReader reader = half.reader();
        FeatureType type = reader.getFeatureType();
        reader.close();

        FeatureType actual = half.getSchema();

        assertEquals(type.getTypeName(), actual.getTypeName());
        assertEquals(type.getNamespace(), actual.getNamespace());
        assertEquals(type.getAttributeCount(), actual.getAttributeCount());

        for (int i = 0; i < type.getAttributeCount(); i++) {
            assertEquals(type.getAttributeType(i), actual.getAttributeType(i));
        }

        assertNull(type.getDefaultGeometry());
        assertEquals(type.getDefaultGeometry(), actual.getDefaultGeometry());
        assertEquals(type, actual);

        try {
            Envelope b = half.getBounds();
            assertEquals(new Envelope(1,5,0,4), b);
        } catch (IOException io) {
        }
    }

    
}
