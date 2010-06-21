/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.geojson;

import java.io.StringWriter;
import java.util.Iterator;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class FeatureJSONTest extends GeoJSONTestSupport {

    FeatureJSON fjson = new FeatureJSON();
    SimpleFeatureType featureType;
    SimpleFeatureBuilder fb;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("feature");
        tb.setSRS("EPSG:4326");
        tb.add("int", Integer.class);
        tb.add("double", Double.class);
        tb.add("string", String.class);
        tb.add("geometry", Geometry.class);
            
        featureType = tb.buildFeatureType();
        fb = new SimpleFeatureBuilder(featureType);
    }
        
    public void testFeatureWrite() throws Exception {
        
        StringWriter writer = new StringWriter();
        fjson.writeFeature(feature(1), writer);
        
        assertEquals(strip(featureText(1)), writer.toString());
    }
    
    public void testFeatureRead() throws Exception {
        SimpleFeature f1 = feature(1);
        SimpleFeature f2 = fjson.readFeature(reader(strip(featureText(1)))); 
        assertEqualsLax(f1, f2);
    }
    
    public void testFeatureReadWithRegularGeometryAttribute() throws Exception {
        SimpleFeature f = fjson.readFeature(reader(strip("{" + 
        "   'type': 'Feature'," +
        "   'geometry': {" +
        "     'type': 'Point'," +
        "     'coordinates': [0.1, 0.1]," +
        "   }," +
        "   'properties': {" +
        "     'int': 1," +
        "     'double': 0.1," +
        "     'string': 'one'," +
        "     'otherGeometry': {" +
        "        'type': 'LineString'," +
        "        'coordinates': [[1.1, 1.2], [1.3, 1.4]]" +
        "     }"+
        "   }," +
        "   'id': 'feature.0'" +
        " }")));
        
        assertNotNull(f);
        assertTrue(f.getDefaultGeometry() instanceof Point);
        
        Point p = (Point) f.getDefaultGeometry();
        assertEquals(0.1, p.getX(), 0.1);
        assertEquals(0.1, p.getY(), 0.1);
        
        assertTrue(f.getAttribute("otherGeometry") instanceof LineString);
        assertTrue(new GeometryFactory().createLineString(new Coordinate[]{
            new Coordinate(1.1, 1.2), new Coordinate(1.3, 1.4)}).equals((LineString)f.getAttribute("otherGeometry")));
        
        assertEquals(1, ((Number)f.getAttribute("int")).intValue());
        assertEquals(0.1, ((Number)f.getAttribute("double")).doubleValue());
        assertEquals("one", f.getAttribute("string"));
    }
    
    public void testFeatureWithBoundsWrite() throws Exception {
        String json = 
            "{" + 
            "   'type': 'Feature'," +
            "   'bbox': [1.1, 1.1, 1.1, 1.1], " + 
            "   'geometry': {" +
            "     'type': 'Point'," +
            "     'coordinates': [1.1, 1.1]" +
            "   }," +
            "   'properties': {" +
            "     'int': 1," +
            "     'double': 1.1," +
            "     'string': 'one'" +
            "   }," +
            "   'id': 'feature.1'" +
            " }";
        
        fjson.setEncodeFeatureBounds(true);
        assertEquals(strip(json), fjson.toString(feature(1)));
    }
    
    public void testFeatureWithCRSWrite() throws Exception {
        fjson.setEncodeFeatureCRS(true);
        assertEquals(strip(featureWithCRSText()), fjson.toString(feature(1)));
    }

    public void testFeatureNoGeometryWrite() throws Exception {
        String json = 
            "{" + 
            "   'type': 'Feature'," +
            "   'properties': {" +
            "     'foo': 'FOO'" +
            "   }," +
            "   'id': 'feature.foo'" +
            " }";
        
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("nogeom");
        tb.add("foo", String.class);
        
        SimpleFeatureType ft = tb.buildFeatureType();
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(ft);
        b.add("FOO");
        
        SimpleFeature f = b.buildFeature("feature.foo");
        assertEquals(strip(json), fjson.toString(f));
    }
    
    String featureWithCRSText() {
        String json = 
            "{" + 
            "   'type': 'Feature'," +
            "   'crs': {" +
            "     'type': 'name'," +
            "     'properties': {" +
            "       'name': 'EPSG:4326'" + 
            "     }" +
            "   }, " + 
            "   'geometry': {" +
            "     'type': 'Point'," +
            "     'coordinates': [1.1, 1.1]" +
            "   }," +
            "   'properties': {" +
            "     'int': 1," +
            "     'double': 1.1," +
            "     'string': 'one'" +
            "   }," +
            "   'id': 'feature.1'" +
            " }";
        return json;
    }

    public void testFeatureWithCRSRead() throws Exception {
        SimpleFeature f = fjson.readFeature(reader(strip(featureWithCRSText())));
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:4326"), 
            f.getFeatureType().getCoordinateReferenceSystem()));
    }
    
    public void testFeatureCollectionWrite() throws Exception {
        StringWriter writer = new StringWriter();
        fjson.writeFeatureCollection(collection(), writer);
        assertEquals(strip(collectionText()), writer.toString());
    }
    
    public void testFeatureCollectionRead() throws Exception {
        
        FeatureCollection actual = 
            fjson.readFeatureCollection(reader(strip(collectionText())));
        assertNotNull(actual);
        
        FeatureCollection expected = collection();
        assertEquals(expected.size(), actual.size());
        
        Iterator a = actual.iterator();
        Iterator e = expected.iterator();
        
        while(e.hasNext()) {
            assertTrue(a.hasNext());
            assertEqualsLax((SimpleFeature)e.next(), (SimpleFeature) a.next());
        }
        
        actual.close(a);
        expected.close(e);
    }
    
    public void testFeatureCollectionStream() throws Exception {
        FeatureIterator<SimpleFeature> features = 
            fjson.streamFeatureCollection(reader(strip(collectionText())));
        
        FeatureCollection expected = collection();
        Iterator e = expected.iterator();
        
        while(e.hasNext()) {
            assertTrue(features.hasNext());
            assertEqualsLax((SimpleFeature)e.next(), features.next());
        }
        
        features.close();
        expected.close(e);
    }

    public void testFeatureCollectionWithBoundsWrite() throws Exception {
        fjson.setEncodeFeatureCollectionBounds(true);
        assertEquals(strip(collectionText(true, false)), fjson.toString(collection()));
    }
    
    public void testFeatureCollectionWithCRSWrite() throws Exception {
        fjson.setEncodeFeatureCollectionCRS(true);
        assertEquals(strip(collectionText(false, true)), fjson.toString(collection()));
    }
    
    public void testCRSWrite() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        StringWriter writer = new StringWriter();
        fjson.writeCRS(crs, writer);

        assertEquals(strip(crsText()), writer.toString());
    }

    public void testCRSRead() throws Exception {
        Object crs = fjson.readCRS(reader(strip(crsText())));
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("epsg:4326"), crs));
    }

    String crsText() {
        return 
            "{" + 
            "    'type': 'name',"+
            "    'properties': {"+
            "       'name': 'EPSG:4326'"+
            "     }"+
            "}";
    }
    
    SimpleFeature feature(int val) {
        fb.add(val);
        fb.add(val + 0.1);
        fb.add(toString(val));
        fb.add(new GeometryFactory().createPoint(new Coordinate(val+0.1,val+0.1)));
        
        return fb.buildFeature("feature." + val);
    }
    
    String featureText(int val) {
        return 
        "{" +
        "  'type': 'Feature'," +
        "  'geometry': {" +
        "     'type': 'Point'," +
        "     'coordinates': [" + (val+0.1) + "," + (val+0.1) + "]" +
        "   }, " +
        "'  properties': {" +
        "     'int': " + val + "," +
        "     'double': " + (val + 0.1) + "," +
        "     'string': '" + toString(val) + "'" +
        "   }," +
        "   'id':'feature." + val + "'" +
        "}";
    }
    
    FeatureCollection collection() {
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null, featureType);
        for (int i = 0; i < 3; i++) {
            collection.add(feature(i));
        }
        return collection;
    }
    
    String collectionText() {
        return collectionText(false,false);
    }
    
    String collectionText(boolean withBounds, boolean withCRS) {
        StringBuffer sb = new StringBuffer();
        sb.append("{'type':'FeatureCollection',");
        if (withBounds) {
            FeatureCollection features = collection();
            ReferencedEnvelope bbox = features.getBounds();
            sb.append("'bbox': [");
            sb.append(bbox.getMinX()).append(",").append(bbox.getMinY()).append(",")
                .append(bbox.getMaxX()).append(",").append(bbox.getMaxY());
            sb.append("],");
        }
        if (withCRS) {
            sb.append("'crs': {");
            sb.append("  'type': 'name',");
            sb.append("  'properties': {");
            sb.append("    'name': 'EPSG:4326'");
            sb.append("   }");
            sb.append("},");
        }
        sb.append("'features':[");
        for (int i = 0; i < 3; i++) {
            sb.append(featureText(i)).append(",");
        }
        sb.setLength(sb.length()-1);
        sb.append("]}");
        return sb.toString();
    }
    String toString(int val) {
        return val == 0 ? "zero" : 
                val == 1 ? "one" :
                val == 2 ? "two" : 
                val == 3 ? "three" : "four";
    }

    void assertEqualsLax(SimpleFeature f1, SimpleFeature f2) {
        assertEquals(f1.getID(), f1.getID());
        assertEquals(f1.getAttributeCount(), f2.getAttributeCount());
        
        for (int i = 0; i < f1.getAttributeCount(); i++) {
            Object o1 = f1.getAttribute(i);
            Object o2 = f2.getAttribute(i);
            
            if (o1 instanceof Geometry) {
                assertTrue(((Geometry) o1).equals((Geometry)o2));
            }
            else {
                if (o1 instanceof Number) {
                    if (o1 instanceof Integer || o1 instanceof Long) {
                        assertTrue(o2 instanceof Integer || o2 instanceof Long);
                        assertEquals(((Number)o1).intValue(), ((Number)o2).intValue());
                    }
                    else if (o1 instanceof Float || o1 instanceof Double) {
                        assertTrue(o2 instanceof Float || o2 instanceof Double);
                        assertEquals(((Number)o1).doubleValue(), ((Number)o2).doubleValue());
                    }
                    else {
                        fail();
                    }
                }
                else {
                    assertEquals(o1, o2);
                }
            }
        }
    }
}
