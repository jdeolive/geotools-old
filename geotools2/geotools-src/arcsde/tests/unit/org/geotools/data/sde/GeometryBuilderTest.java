/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
package org.geotools.data.sde;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeShape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilderTest.java,v 1.7 2004/01/09 17:20:47 aaime Exp $
 */
public class GeometryBuilderTest extends TestCase {
    static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");
    static final String COORD_SYS = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]";

    /** DOCUMENT ME! */
    private GeometryBuilder geometryBuilder = null;

    /** DOCUMENT ME! */
    private WKTReader wktReader;

    /**
     * Creates a new GeometryBuilderTest object.
     *
     * @param name DOCUMENT ME!
     */
    public GeometryBuilderTest(String name) {
        super(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.wktReader = new WKTReader();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        geometryBuilder = null;
        wktReader = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetDefaultValues() {
        testGetDefaultValue(Point.class);
        testGetDefaultValue(MultiPoint.class);
        testGetDefaultValue(LineString.class);
        testGetDefaultValue(MultiLineString.class);
        testGetDefaultValue(Polygon.class);
        testGetDefaultValue(MultiPolygon.class);
    }

    /**
     * DOCUMENT ME!
     */
    public void testPointBuilder() {
        testBuildGeometries(Point.class, "pointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiPointBuilder() {
        testBuildGeometries(MultiPoint.class, "multipointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testLineStringBuilder() {
        testBuildGeometries(LineString.class, "linestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiLineStringBuilder() {
        testBuildGeometries(MultiLineString.class, "multilinestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiPolygonBuilder() {
        testBuildGeometries(MultiPolygon.class, "multipolygontest.wkt");
    }
/*
    public void testConstructShapePoint() {
      Geometry[] testPoints = null;
      try {
        testPoints = loadTestData("pointtest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testPoints);
    }

    public void testConstructShapeMultiPoint() {
      Geometry[] testMultiPoints = null;
      try {
        testMultiPoints = loadTestData("multipointtest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testMultiPoints);
    }

    public void testConstructShapeLineString() {
      Geometry[] testLineStrings = null;
      try {
        testLineStrings = loadTestData("linestringtest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testLineStrings);
    }

    public void testConstructShapeMultiLineString() {
      Geometry[] testMultiLineStrings = null;
      try {
        testMultiLineStrings = loadTestData("multilinestringtest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testMultiLineStrings);
    }

    public void testConstructShapePolygon() {
      Geometry[] testPolygons = null;
      try {
        testPolygons = loadTestData("multipolygontest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testPolygons);
    }

    public void testConstructShapeMultiPolygon() {
      Geometry[] testMultiPolygons = null;
      try {
        testMultiPolygons = loadTestData("multipolygontest.wkt");
      }
      catch (Exception ex) {
        fail(ex.getMessage());
      }
      testConstructShape(testMultiPolygons);
    }
*/
    public void testConstructShapeEmpty() {
      Geometry[] testEmptys = new Geometry[6];
      testEmptys[0] = GeometryBuilder.builderFor(Point.class).getEmpty();
      testEmptys[1] = GeometryBuilder.builderFor(MultiPoint.class).getEmpty();
      testEmptys[2] = GeometryBuilder.builderFor(LineString.class).getEmpty();
      testEmptys[3] = GeometryBuilder.builderFor(MultiLineString.class).getEmpty();
      testEmptys[4] = GeometryBuilder.builderFor(Polygon.class).getEmpty();
      testEmptys[5] = GeometryBuilder.builderFor(MultiPolygon.class).getEmpty();
      testConstructShape(testEmptys);
    }

    /**
     * tests each geometry in <code>geometries</code> using
     * <code>testConstructShape(Geometry)</code>
     */
    public void testConstructShape(Geometry []geometries)
    {
      for(int i = 0; i < geometries.length; i++)
      {
        testConstructShape(geometries[i]);
      }
    }
    /**
     * tests the building of SeShape objects from JTS Geometries.
     * To do that, recieves a Geometry object, then creates a GeometryBuilder
     * for it's geometry type and ask it to construct an equivalent SeShape.
     * With this SeShape, checks that it's number of points is equal to the
     * number of points in <code>geometry</code>, and then creates an equivalent
     * Geometry object, wich in turn is checked for equality against
     * <code>geometry</code>.
     *
     * @param geometry DOCUMENT ME!
     */
    public void testConstructShape(Geometry geometry) {

        LOGGER.fine("testConstructShape: testing " + geometry);

        Class geometryClass = geometry.getClass();
        GeometryBuilder builder = GeometryBuilder.builderFor(geometryClass);
        SeCoordinateReference cr = new SeCoordinateReference();
        cr.setCoordSysByDescription(COORD_SYS);
        Geometry equivalentGeometry = null;
        try {
          SeShape equivalentShape = builder.constructShape(geometry, cr);
          assertEquals(geometry.getNumPoints(), equivalentShape.getNumOfPoints());
          LOGGER.info("geometry and SeShape contains the same number of points: "
                      + equivalentShape.getNumOfPoints());
          LOGGER.fine("generating an SeShape's equivalent Geometry");
          equivalentGeometry = builder.construct(equivalentShape);
        }catch(Exception ex) {
          ex.printStackTrace();
          fail(ex.getMessage());
        }

        LOGGER.info("now testing both geometries for equivalence: " +
                    geometry + " -- " + equivalentGeometry);


        assertEquals(geometry.getDimension(), equivalentGeometry.getDimension());
        LOGGER.info("dimension test passed");

        assertEquals(geometry.getGeometryType(), equivalentGeometry.getGeometryType());
        LOGGER.info("geometry type test passed");

        assertEquals(geometry.getSRID(), equivalentGeometry.getSRID());
        LOGGER.info("SRID test passed");

        assertEquals(geometry.getNumPoints(), equivalentGeometry.getNumPoints());
        LOGGER.info("numPoints test passed");

        LOGGER.info(geometry.getEnvelopeInternal() + " == " + equivalentGeometry
                .getEnvelopeInternal());

        assertEquals(geometry.getEnvelopeInternal(), equivalentGeometry.getEnvelopeInternal());

        assertEquals(geometry.getArea(), equivalentGeometry.getArea(), 0.001);
        LOGGER.info("area test passed");

        assertEquals(geometry, equivalentGeometry);
        LOGGER.info("equals test passed!!!");

    }

    /**
     * DOCUMENT ME!
     *
     * @param geometryClass DOCUMENT ME!
     * @param testDataResource DOCUMENT ME!
     */
    private void testBuildGeometries(final Class geometryClass,
        final String testDataResource) {
        LOGGER.info("---- testBuildGeometries: testing " + testDataResource
            + " ----");

        try {
            geometryBuilder = GeometryBuilder.builderFor(geometryClass);
            LOGGER.info("created " + geometryBuilder.getClass().getName());

            Geometry[] expectedGeometries = loadTestData(testDataResource);
            Geometry createdGeometry;
            Geometry expectedGeometry;
            double[][][] sdeCoords;

            for (int i = 0; i < expectedGeometries.length; i++) {
                expectedGeometry = expectedGeometries[i];
                sdeCoords = geometryToSdeCoords(expectedGeometry);
                createdGeometry = geometryBuilder.newGeometry(sdeCoords);
                assertTrue(expectedGeometry.equals(createdGeometry));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param jtsGeom DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private double[][][] geometryToSdeCoords(Geometry jtsGeom) {
        int numParts;
        int numSubParts = 1;
        double[][][] sdeCoords;
        GeometryCollection gcol = null;

        if (jtsGeom instanceof MultiPolygon) {
            gcol = (GeometryCollection) jtsGeom;
        } else {
            Geometry[] geoms = { jtsGeom };
            gcol = new GeometryFactory().createGeometryCollection(geoms);
        }

        numParts = gcol.getNumGeometries();
        sdeCoords = new double[numParts][0][0];

        for (int i = 0; i < numParts; i++) {
            Geometry geom = gcol.getGeometryN(i);
            numSubParts = (geom instanceof Polygon)
                ? (((Polygon) geom).getNumInteriorRing() + 1)
                : ((geom instanceof GeometryCollection)
                ? ((GeometryCollection) geom).getNumGeometries() : 1);
            sdeCoords[i] = new double[numSubParts][0];

            Coordinate[] partCoords = null;

            for (int j = 0; j < numSubParts; j++) {
                if (geom instanceof Polygon) {
                    if (j == 0) {
                        partCoords = ((Polygon) geom).getExteriorRing()
                                      .getCoordinates();
                    } else {
                        partCoords = ((Polygon) geom).getInteriorRingN(j - 1)
                                      .getCoordinates();
                    }
                } else if (geom instanceof GeometryCollection) {
                    partCoords = ((GeometryCollection) geom).getGeometryN(j)
                                  .getCoordinates();
                } else {
                    partCoords = geom.getCoordinates();
                }

                sdeCoords[i][j] = toSdeCoords(partCoords);
            }
        }

        return sdeCoords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private double[] toSdeCoords(Coordinate[] coords) {
        int nCoords = coords.length;
        double[] sdeCoords = new double[2 * nCoords];
        Coordinate c;

        for (int i = 0, j = 1; i < nCoords; i++, j += 2) {
            c = coords[i];
            sdeCoords[j - 1] = c.x;
            sdeCoords[j] = c.y;
        }

        return sdeCoords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param resource DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Geometry[] loadTestData(final String resource)
        throws Exception {
        List testGeoms = new LinkedList();
        Geometry g;
        String line = null;

        try {
            LOGGER.info("loading test data /testData/" + resource);

            InputStream in = getClass().getResourceAsStream("/testData/"
                    + resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("#") || "".equals(line)) {
                    continue;
                }

                g = wktReader.read(line);
                LOGGER.info("loaded test geometry: " + g.toText());
                testGeoms.add(g);
            }
        } catch (ParseException ex) {
            LOGGER.severe("cant create a test geometry: " + ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            LOGGER.severe("cant load test data " + resource + ": "
                + ex.getMessage());
            throw ex;
        }

        return (Geometry[]) testGeoms.toArray(new Geometry[0]);
    }

    /**
     * given a geometry class, tests that GeometryBuilder.defaultValueFor that
     * class returns an empty geometry of the same geometry class
     *
     * @param geometryClass DOCUMENT ME!
     */
    private void testGetDefaultValue(Class geometryClass) {
        Geometry geom = GeometryBuilder.defaultValueFor(geometryClass);
        assertNotNull(geom);
        assertTrue(geom.isEmpty());
        assertTrue(geometryClass.isAssignableFrom(geom.getClass()));
    }
}
