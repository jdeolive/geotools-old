/* $Id: GeometryConversionTest.java,v 1.1 2003/08/08 07:33:04 seangeo Exp $
 *
 * Created on 4/08/2003
 */
package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import junit.framework.TestCase;
import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;
import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;
import oracle.sdoapi.geom.Geometry;


/**
 * Test the geometry conversion.
 * 
 * <p>
 * NOTE: Oracle SDO geometries don't override equals() so we must do the comparison on their
 * toString output.
 * </p>
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: GeometryConversionTest.java,v 1.1 2003/08/08 07:33:04 seangeo Exp $ Last Modified: $Date: 2003/08/08 07:33:04 $
 */
public class GeometryConversionTest extends TestCase {
    private GeometryFactory jtsFactory;
    private oracle.sdoapi.geom.GeometryFactory sdoFactory;
    private AdapterJTS adapterJTS;

    /**
     * Constructor for GeometryConversionTest.
     *
     * @param arg0
     */
    public GeometryConversionTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        jtsFactory = new GeometryFactory();
        sdoFactory = OraSpatialManager.getGeometryFactory();
        adapterJTS = new AdapterJTS(jtsFactory, sdoFactory);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        adapterJTS = null;
        jtsFactory = null;
        sdoFactory = null;
    }

    public void testInvalidTypes() throws Exception {
        boolean exceptionThrown = false;

        Geometry geom = sdoFactory.createPoint(10.0, 10.0);

        try {
            Object output = adapterJTS.exportGeometry(String.class, geom);
        } catch (GeometryOutputTypeNotSupportedException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        exceptionThrown = false;

        try {
            geom = adapterJTS.importGeometry("String");
        } catch (GeometryInputTypeNotSupportedException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    public void testPointConversion() throws Exception {
        Geometry sdoIn = sdoFactory.createPoint(10.0, 5.0);
        Point point = (Point) adapterJTS.exportGeometry(Point.class, sdoIn);

        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(5.0, point.getY(), 0.001);

        Geometry sdoOut = adapterJTS.importGeometry(point);
        assertEquals(sdoIn.toString(), sdoOut.toString());
    }

    public void testLineConversion() throws Exception {
        Geometry sdoIn = sdoFactory.createLineString(new double[] { -10.0, -5.0, 10.0, 5.0 });
        LineString line = (LineString) adapterJTS.exportGeometry(LineString.class, sdoIn);

        assertEquals(2, line.getNumPoints());
        Coordinate[] coords = line.getCoordinates();
        assertEquals(-10.0, coords[0].x, 0.001);
        assertEquals(-5.0, coords[0].y, 0.001);
        assertEquals(10.0, coords[1].x, 0.001);
        assertEquals(5.0, coords[1].y, 0.001);

        Geometry sdoOut = adapterJTS.importGeometry(line);
        assertEquals(sdoIn.toString(), sdoOut.toString());
    }

    public void testPolygonConversion() throws Exception {
        oracle.sdoapi.geom.LineString sdoline = sdoFactory.createLineString(new double[] {
                    -10.0, -5.0, -10.0, 5.0, 10.0, 5.0, 10.0, -5.0, -10.0, -5.0
                });
        Geometry sdoIn = sdoFactory.createPolygon(sdoline, new oracle.sdoapi.geom.LineString[0]);
        Polygon polygon = (Polygon) adapterJTS.exportGeometry(Polygon.class, sdoIn);

        assertEquals(0, polygon.getNumInteriorRing());

        LineString exterior = polygon.getExteriorRing();
        Coordinate[] coords = exterior.getCoordinates();
        assertEquals(5, coords.length);
        assertEquals(-10.0, coords[0].x, 0.001);
        assertEquals(-5.0, coords[0].y, 0.001);
        assertEquals(-10.0, coords[1].x, 0.001);
        assertEquals(5.0, coords[1].y, 0.001);
        assertEquals(10.0, coords[2].x, 0.001);
        assertEquals(5.0, coords[2].y, 0.001);
        assertEquals(10.0, coords[3].x, 0.001);
        assertEquals(-5.0, coords[3].y, 0.001);
        assertEquals(-10.0, coords[4].x, 0.001);
        assertEquals(-5.0, coords[4].y, 0.001);

        Geometry sdoOut = adapterJTS.importGeometry(polygon);
        assertEquals(sdoIn.toString(), sdoOut.toString());
    }
}
