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
import junit.framework.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilderTest.java,v 1.5 2003/11/17 17:15:08 groldan Exp $
 */
public class GeometryBuilderTest extends TestCase
{
    static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");

    /** DOCUMENT ME! */
    private GeometryBuilder geometryBuilder = null;

    /** DOCUMENT ME! */
    private WKTReader wktReader;

    /**
     * Creates a new GeometryBuilderTest object.
     *
     * @param name DOCUMENT ME!
     */
    public GeometryBuilderTest(String name)
    {
        super(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        this.wktReader = new WKTReader();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception
    {
        geometryBuilder = null;
        wktReader = null;
        super.tearDown();
    }

    public void testGetDefaultValues()
    {
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
    public void testPointBuilder()
    {
        testBuildGeometries(Point.class, "pointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiPointBuilder()
    {
        testBuildGeometries(MultiPoint.class, "multipointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testLineStringBuilder()
    {
        testBuildGeometries(LineString.class, "linestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiLineStringBuilder()
    {
        testBuildGeometries(MultiLineString.class, "multilinestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     */
    public void testMultiPolygonBuilder()
    {
        testBuildGeometries(MultiPolygon.class, "multipolygontest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @param geometryClass DOCUMENT ME!
     * @param testDataResource DOCUMENT ME!
     */
    private void testBuildGeometries(final Class geometryClass,
        final String testDataResource)
    {
        LOGGER.info("---- testBuildGeometries: testing " + testDataResource
            + " ----");

        String failMsg = "Expected and created by GeometryBuilder geometries does not match";

        try
        {
            geometryBuilder = GeometryBuilder.builderFor(geometryClass);

            LOGGER.info("created " + geometryBuilder.getClass().getName());

            Geometry[] expectedGeometries = loadTestData(testDataResource);
            Geometry createdGeometry;
            Geometry expectedGeometry;
            double[][][] sdeCoords;

            for (int i = 0; i < expectedGeometries.length; i++)
            {
                expectedGeometry = expectedGeometries[i];
                sdeCoords = geometryToSdeCoords(expectedGeometry);
                createdGeometry = geometryBuilder.newGeometry(sdeCoords);
                assertTrue(expectedGeometry.equals(createdGeometry));
            }
        }
        catch (Exception ex)
        {
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
    private double[][][] geometryToSdeCoords(Geometry jtsGeom)
    {
        int numParts;

        int numSubParts = 1;

        int numSubpartPoints;

        double[][][] sdeCoords;

        GeometryCollection gcol = null;

        if (jtsGeom instanceof MultiPolygon)
        {
            gcol = (GeometryCollection) jtsGeom;
        }
        else
        {
            Geometry[] geoms = { jtsGeom };

            gcol = new GeometryFactory().createGeometryCollection(geoms);
        }

        numParts = gcol.getNumGeometries();

        sdeCoords = new double[numParts][0][0];

        for (int i = 0; i < numParts; i++)
        {
            Geometry geom = gcol.getGeometryN(i);

            numSubParts = (geom instanceof Polygon)
                ? (((Polygon) geom).getNumInteriorRing() + 1)
                : ((geom instanceof GeometryCollection)
                ? ((GeometryCollection) geom).getNumGeometries() : 1);

            sdeCoords[i] = new double[numSubParts][0];

            Coordinate[] partCoords = null;

            for (int j = 0; j < numSubParts; j++)
            {
                if (geom instanceof Polygon)
                {
                    if (j == 0)
                    {
                        partCoords = ((Polygon) geom).getExteriorRing()
                                      .getCoordinates();
                    }
                    else
                    {
                        partCoords = ((Polygon) geom).getInteriorRingN(j - 1)
                                      .getCoordinates();
                    }
                }
                else if (geom instanceof GeometryCollection)
                {
                    partCoords = ((GeometryCollection) geom).getGeometryN(j)
                                  .getCoordinates();
                }
                else
                {
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
    private double[] toSdeCoords(Coordinate[] coords)
    {
        int nCoords = coords.length;
        double[] sdeCoords = new double[2 * nCoords];

        Coordinate c;
        for (int i = 0, j = 1; i < nCoords; i++, j += 2)
        {
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
        throws Exception
    {
        List testGeoms = new LinkedList();
        Geometry g;
        String line = null;

        try
        {
            LOGGER.info("loading test data /testData/" + resource);

            InputStream in = getClass().getResourceAsStream("/testData/"
                    + resource);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if(line.startsWith("#") || "".equals(line))
                  continue;
                g = wktReader.read(line);
                LOGGER.info("loaded test geometry: " + g.toText());
                testGeoms.add(g);
            }
        }
        catch (ParseException ex)
        {
            LOGGER.severe("cant create a test geometry: " + ex.getMessage());
            throw ex;
        }
        catch (IOException ex)
        {
            LOGGER.severe("cant load test data " + resource + ": "
                + ex.getMessage());
            throw ex;
        }

        return (Geometry[]) testGeoms.toArray(new Geometry[0]);
    }

    /**
     * given a geometry class, tests that GeometryBuilder.defaultValueFor
     * that class returns an empty geometry of the same geometry class
     */
    private void testGetDefaultValue(Class geometryClass)
    {
      Geometry geom = GeometryBuilder.defaultValueFor(geometryClass);
      assertNotNull(geom);
      assertTrue(geom.isEmpty());
      assertTrue(geometryClass.isAssignableFrom(geom.getClass()));
    }

}
