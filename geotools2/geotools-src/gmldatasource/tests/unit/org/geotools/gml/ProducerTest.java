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
package org.geotools.gml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import junit.framework.*;

/*
 * ProducerTest.java
 * JUnit based test
 *
 */
import org.geotools.data.*;
import org.geotools.data.gml.GMLDataSource;
import org.geotools.data.memory.*;
import org.geotools.feature.*;
import org.geotools.gml.producer.*;
import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Chris Holmes, TOPP
 */
public class ProducerTest extends TestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gml");
    static int NTests = 7;
    private Feature testFeature;
    private FeatureType schema;
    private FeatureFactory featureFactory;
    FeatureCollection table = null;
    Feature polygonFeature = null;
    Feature lineFeature = null;
    Feature multiLineFeature = null;
    Feature mPointFeature = null;
    Feature mPolyFeature = null;
    Feature mGeomFeature = null;
    Object[] attributes = { "rail", null, "tronic" };
    LineString line = null;

    public ProducerTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ProducerTest.class);

        return suite;
    }

    public void setUp() throws Exception {
        System.setProperty("javax.xml.transform.TransformerFactory",
            "org.apache.xalan.processor.TransformerFactoryImpl");

        LOGGER.fine("testing producer");

        String dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder += "/tests/unit/testData";
        }

        URL url = new URL("file:///" + dataFolder + "/testGML7Features.gml");
        LOGGER.fine("Testing to load " + url + " as Feature datasource");

        DataSource ds = new GMLDataSource(url);

        AttributeType[] atts = {
            AttributeTypeFactory.newAttributeType("geoid", Integer.class),
            AttributeTypeFactory.newAttributeType("geom", Geometry.class),
            AttributeTypeFactory.newAttributeType("name", String.class)
        };

        try {
            schema = FeatureTypeFactory.newFeatureType(atts, "rail",
                    "http://www.openplans.org/data");
        } catch (SchemaException e) {
            LOGGER.finer("problem with creating schema");
        }

        LOGGER.fine("namespace is " + schema.getNamespace());

        PrecisionModel precModel = new PrecisionModel();
        int srid = 2035;

        GeometryFactory geomFactory = new GeometryFactory(precModel, srid);

        Coordinate[] points = {
            new Coordinate(15, 15), new Coordinate(15, 25),
            new Coordinate(25, 25), new Coordinate(25, 15),
            new Coordinate(15, 15)
        };
        LinearRing shell = new LinearRing(points, precModel, srid);
        Polygon the_geom = new Polygon(shell, precModel, srid);
        Polygon[] polyArr = { the_geom, the_geom };
        MultiPolygon multiPoly = geomFactory.createMultiPolygon(polyArr);
        Point point = geomFactory.createPoint(new Coordinate(3, 35));
        Point[] pointArr = { point, point, point, point };
        MultiPoint multiPoint = new MultiPoint(pointArr, precModel, srid);
        line = new LineString(points, precModel, srid);

        LineString[] lineArr = { line, line, line };
        MultiLineString multiLine = new MultiLineString(lineArr, precModel, srid);
        Geometry[] geomArr = { multiPoly, point, line, multiLine };
        GeometryCollection multiGeom = new GeometryCollection(geomArr,
                precModel, srid);
        Integer featureId = new Integer(32);
        String name = "inse<rt polygon444444";
        attributes[0] = featureId;
        attributes[1] = point;
        attributes[2] = name;

        try {
            testFeature = schema.create(attributes, "rail.1");
            attributes[1] = line;
            lineFeature = schema.create(attributes, "rail.2");
            attributes[1] = the_geom;
            polygonFeature = schema.create(attributes, "rail.3");
            attributes[1] = multiLine;
            multiLineFeature = schema.create(attributes, "rail.4");
            attributes[1] = multiPoint;
            mPointFeature = schema.create(attributes, "rail.5");
            attributes[1] = multiPoly;
            mPolyFeature = schema.create(attributes, "rail.6");
            attributes[1] = multiGeom;
            mGeomFeature = schema.create(attributes, "rail.7");
        } catch (IllegalAttributeException ife) {
            LOGGER.warning("problem in setup " + ife);
        }
    }

    /**
     * This needs to be redone, for now it is just a demo that will print some
     * sample features.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testProducer() throws Exception {
        //table = ds.getFeatures();//new FeatureCollectionDefault();
        table = FeatureCollections.newCollection();
        table.add(testFeature);
        table.add(lineFeature);
        table.add(polygonFeature);
        table.add(multiLineFeature);
        table.add(mPointFeature);
        table.add(mPolyFeature);
        table.add(mGeomFeature);
        LOGGER.fine("the feature collection is " + table + ", and "
            + "the first feat is " + table.features().next());

        FeatureTransformer fr = new FeatureTransformer();
        fr.setIndentation(4);
        fr.getFeatureTypeNamespaces().declareDefaultNamespace("test",
            "http://www.geotools.org");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        fr.transform(table, baos);
        LOGGER.fine("output is " + new String(baos.toByteArray()));
    }

    public void testFeatureReader() throws Exception {
        table = FeatureCollections.newCollection();
        table.add(testFeature);
        table.add(lineFeature);
        table.add(polygonFeature);
        LOGGER.fine("the feature collection is " + table + ", and "
            + "the first feat is " + table.features().next());

        MemoryDataStore dstore = new MemoryDataStore(table);
        FeatureReader reader = dstore.getFeatureReader("rail");
        FeatureTransformer fr = new FeatureTransformer();
        fr.setIndentation(4);
        fr.getFeatureTypeNamespaces().declareDefaultNamespace("test",
            "http://www.geotools.org");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        fr.transform(reader, baos);
        LOGGER.fine("output is " + new String(baos.toByteArray()));
    }

    public void testFeatureResults() throws Exception {
        table = FeatureCollections.newCollection();
        table.add(testFeature);
        table.add(lineFeature);
        table.add(polygonFeature);
        LOGGER.fine("the feature collection is " + table + ", and "
            + "the first feat is " + table.features().next());

        MemoryDataStore dstore = new MemoryDataStore(table);
        FeatureSource source = dstore.getFeatureSource("rail");
        FeatureResults results = source.getFeatures(Query.ALL);
        FeatureTransformer fr = new FeatureTransformer();
        fr.setIndentation(4);
        fr.getFeatureTypeNamespaces().declareNamespace(dstore.getSchema("rail"),
            "tt2", "http://www.geotools.org");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        fr.transform(results, baos);
        LOGGER.info("output is " + new String(baos.toByteArray()));
    }

    public void testMultiFeatureResults() throws Exception {
        table = FeatureCollections.newCollection();
        table.add(testFeature);
        table.add(lineFeature);
        table.add(polygonFeature);
        LOGGER.fine("the feature collection is " + table + ", and "
            + "the first feat is " + table.features().next());

        MemoryDataStore dstore = new MemoryDataStore(table);
        FeatureSource source = dstore.getFeatureSource("rail");
        FeatureResults results1 = source.getFeatures(Query.ALL);
        FeatureResults results2 = source.getFeatures(Query.ALL);
        FeatureResults[] resultsArr = { results1, results2 };
        FeatureTransformer fr = new FeatureTransformer();
        fr.setIndentation(4);
        fr.getFeatureTypeNamespaces().declareDefaultNamespace("tp2",
            "http://www.geotools.org");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        fr.transform(resultsArr, baos);
        LOGGER.fine("output is " + new String(baos.toByteArray()));
    }

    public void testNulls() throws Exception {
        attributes[1] = line;
        attributes[0] = null;
        testFeature = schema.create(attributes, "rail.19");
        table = FeatureCollections.newCollection();
        table.add(testFeature);

        FeatureTransformer fr = new FeatureTransformer();

        fr.setIndentation(2);

        FeatureTypeNamespaces ftNames = fr.getFeatureTypeNamespaces();
        ftNames.declareDefaultNamespace("gt2", "http://www.geotools.org");

        //ftNames.addSchemaLocation("http://www.geotools.org", "../gt.xsd");
        fr.addSchemaLocation("http://www.geotools.org", "../gt.xsd");
        fr.addSchemaLocation("http://www.opengis.net/wfs",
            "http://schemas.opengis.new/wfs/1.0.0/WFS-basic.xsd");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        fr.transform(table, baos);
        LOGGER.fine("output is " + baos.toString());
    }

    public void testNullGeometry() throws Exception {
        attributes[1] = null;
        attributes[2] = "null geometry";
        testFeature = schema.create(attributes, "rail.19");
        table = FeatureCollections.newCollection();
        table.add(testFeature);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        FeatureTransformer fr = new FeatureTransformer();
        fr.setIndentation(2);
        fr.getFeatureTypeNamespaces().declareDefaultNamespace("gt2",
            "http://www.geotools.org");
        fr.transform(table, baos);
        LOGGER.fine("output is " + new String(baos.toByteArray()));
    }
}
