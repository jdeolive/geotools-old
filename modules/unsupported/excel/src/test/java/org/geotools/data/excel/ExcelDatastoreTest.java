package org.geotools.data.excel;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class ExcelDatastoreTest extends TestCase {

    static ExcelDataStore[] eds;

    static boolean setup = false;

    @Override
    protected void setUp() throws Exception {
        if (!setup) {
            // TODO Auto-generated method stub
            super.setUp();
            final File test_data_dir = TestData.file(this, null);
            String[] testFiles = test_data_dir.list();
            eds = new ExcelDataStore[testFiles.length];
            int i = 0;
            for (String f : testFiles) {
                File file = org.geotools.TestData.file(this, f);

                String filename = file.getCanonicalPath();
                HashMap<String, Serializable> params = new HashMap<String, Serializable>();
                params.put("type", "excel");
                params.put("filename", filename);
                params.put("sheet", "locations");
                params.put("latcol", 0);
                params.put("longcol", 1);
                params.put("projection", "epsg:4326");
                ExcelDataStoreFactory fac = new ExcelDataStoreFactory();
                assertTrue("Can't process params", fac.canProcess(params));
                ExcelDataStore ex = (ExcelDataStore) fac.createDataStore(params);
                assertNotNull("Null data store", ex);
                // System.out.println("adding " + i + " " + ex);
                eds[i++] = ex;

            }
            setup = true;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testExcelDatastore() throws IOException {
        File file = org.geotools.TestData.file(this, "locations.xls");
        String filename = file.getCanonicalPath();
        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("type", "excel");
        params.put("filename", filename);
        params.put("sheet", "locations");
        params.put("latcol", 0);
        params.put("longcol", 1);
        params.put("projection", "epsg:4326");
        DataStore store = DataStoreFinder.getDataStore(params);
        assertNotNull("no datastore found", store);
        System.out.println(store.getInfo());
        ExcelDataStoreFactory fac = new ExcelDataStoreFactory();

        assertTrue("Can't process params", fac.canProcess(params));
        store = fac.createDataStore(params);
        assertNotNull("no datastore created", store);
        params.put("headerrow", 0);
        assertTrue("Can't process params with headerrow", fac.canProcess(params));
        params.remove("sheet");
        assertFalse("Can process params without sheet", fac.canProcess(params));
    }

    public void testGetNames() throws IOException {
        for (ExcelDataStore ed : eds) {
            System.out.println(ed);

            String[] names = ed.getTypeNames();
            System.out.println(names);
            assertEquals("Sheet Name is wrong", "locations", names[0]);
        }
    }

    public void testGetFeatureSource() throws IOException {
        for (ExcelDataStore ed : eds) {
            List<Name> names = ed.getNames();
            ExcelFeatureSource source = (ExcelFeatureSource) ed.getFeatureSource(names.get(0));
            assertNotNull("FeatureSource is null", source);
            SimpleFeatureType schema = source.getSchema();
            assertNotNull("Null Schema", schema);
            System.out.println(schema.getAttributeCount());
            List<AttributeDescriptor> attrs = schema.getAttributeDescriptors();
            for (AttributeDescriptor attr : attrs) {
                System.out.println(attr.getName() + ": " + attr.getType());
            }
            ContentFeatureCollection fts = source.getFeatures();
            System.out.println("BBox = " + source.getBounds());
            System.out.println("got " + fts.size() + " features");
            SimpleFeatureIterator its = fts.features();
            while (its.hasNext()) {
                SimpleFeature feature = its.next();
                System.out.println(feature.getID() + " " + feature.getAttribute("the_geom") + " ^"
                        + feature.getAttribute("CITY") + "^");
            }
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            Filter filter = ff.equal(ff.property("CITY"), ff.literal("Trento"), true);
            Query query = new Query("locations", filter);
            fts = source.getFeatures(query);
            System.out.println(fts.size());
            System.out.println(fts.features().next());
        }
    }
}
