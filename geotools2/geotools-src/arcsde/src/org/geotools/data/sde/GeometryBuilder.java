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

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import java.util.*;


/**
 * Creates propper JTS Geometry objects from <code>SeShape</code> objects and
 * viceversa.
 *
 * <p>
 * <code>SeShape</code>'s are gathered from an <code>SeRow</code> ArcSDE API's
 * result object and holds it's geometry attributes as a three dimensional
 * array of <code>double</code> primitives as explained bellow.
 * </p>
 *
 * <p>
 * By this way, we avoid the creation of ArcSDE's java implementation of  OGC
 * geometries for later translation to JTS, avoiding too the dependency on the
 * ArcSDE native library wich the geometry package of the ArcSDE Java API
 * depends on.
 * </p>
 *
 * <p>
 * Given <code>double [][][]coords</code> the meaning of this array is as
 * follow:
 *
 * <ul>
 * <li>
 * coords.length reprsents the number of geometries this geometry is composed
 * of. In deed, this only applies for multipolygon geometries, for all other
 * geometry types, this will be allways <code>1</code>
 * </li>
 * <li>
 * coords[n] holds the coordinate arrays of the n'th geometry this geometry is
 * composed of. Except for multipolygons, this will allways be
 * <code>coords[0]</code>.
 * </li>
 * <li>
 * coords[n][m] holds the coordinates array for a given geometry. (i.e. [0][m]
 * for a multilinestring or [2][m] for a multipolygon composed of 3 polygons)
 * </li>
 * <li>
 * coords[n][m][l] holds the {x1, y1, x2, y2, ...,Xn, Yn} coordinates for a
 * given geometry part
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * This abstract class will use specialized subclass for constructing the
 * propper geometry type
 * </p>
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
public abstract class GeometryBuilder
{
    /** specialized geometry builders classes by it's geometry type */
    private static final Map builders = new HashMap();

    private static final Map nullGeometries = new HashMap();

    /**
     * mapping of geometry classess with their builders
     */
    static
    {
        builders.put(Point.class, PointBuilder.class);
        builders.put(MultiPoint.class, MultiPointBuilder.class);
        builders.put(LineString.class, LineStringBuilder.class);
        builders.put(MultiLineString.class, MultiLineStringBuilder.class);
        builders.put(Polygon.class, PolygonBuilder.class);
        builders.put(MultiPolygon.class, MultiPolygonBuilder.class);

        nullGeometries.put(Point.class, PointBuilder.getEmpty());
        nullGeometries.put(MultiPoint.class, MultiPointBuilder.getEmpty());
        nullGeometries.put(LineString.class, LineStringBuilder.getEmpty());
        nullGeometries.put(MultiLineString.class, MultiLineStringBuilder.getEmpty());
        nullGeometries.put(Polygon.class, PolygonBuilder.getEmpty());
        nullGeometries.put(MultiPolygon.class, MultiPolygonBuilder.getEmpty());
    }

    /** JTS geometry factory subclasses use to map SeShapes to JTS ones */
    protected GeometryFactory factory = new GeometryFactory();

    /**
     * Takes an ArcSDE's <code>SeShape</code> and builds a JTS Geometry. The
     * geometry type constructed depends on this <code>GeometryBuilder</code>
     * specialized subclass
     *
     * @param shape the ESRI's ArcSDE java api shape upon wich to create the
     *        new JTS geometry
     *
     * @return the type of JTS Geometry this subclass instance is specialized
     *         for or an empty geometry of the same class if
     *         <code>shape.isNil()</code>
     *
     * @throws SeException if it occurs fetching the coordinates array from
     *         <code>shape</code>
     * @throws DataSourceException if the
     *         <code>com.vividsolutions.jts.geom.GeometryFactory</code> this
     *         builder is backed by can't create the
     *         <code>com.vividsolutions.jts.geom.Geometry</code> with the
     *         <code>com.vividsolutions.jts.geom.Coordinate[]</code> provided
     *         by <code>newGeometry</code>
     */
    public Geometry construct(SeShape shape)
        throws SeException, DataSourceException
    {
        return shape.isNil() ? getEmpty() : newGeometry(shape.getAllCoords());
    }

    /**
     * Creates the ArcSDE Java API representation of a <code>Geometry</code>
     * object in its shape format, suitable to filter expressions as the SDE
     * API expects
     *
     * @param geometry the JTS Geometry object to get the SDE representation
     *        from
     * @param seSrs Coordinate Reference System of the underlying
     *        <code>SeLayer</code> object for wich the <code>SeShape</code> is
     *        constructed.
     *
     * @return the <code>SeShape</code> representation of passed
     *         <code>Geometry</code>
     *
     * @throws GeometryBuildingException DOCUMENT ME!
     */
    public SeShape construct(Geometry geometry, SeCoordinateReference seSrs)
        throws GeometryBuildingException
    {
        try
        {
            SeShape shape = new SeShape(seSrs);

            generateShape(geometry, shape);

            return shape;
        }
        catch (SeException ex)
        {
            throw new GeometryBuildingException(ex.getMessage(), ex);
        }
    }

    /**
     * subclasses must implement this method to fill in <code>dest</code> with
     * the propper <code>double[][][]</code>
     *
     * @param geometry the source geometry
     * @param dest destination SDE shape that must be filled with coordinates
     *        arrays
     *
     * @throws SeException if an SDE API SeException is thrown at fill time
     */
    protected abstract void generateShape(Geometry geometry, SeShape dest)
        throws SeException;

    /**
     * utility method that <code>GeometryBuilder</code> subclasses use to
     * obtain the three dimensional array of double's that forms the body of
     * the <code>SeShape</code> structure.
     *
     * @param jtsGeom the source JTS geometry
     *
     * @return the array of <code>double</code> primitives that represents
     *         <code>jtsGeom</code> in <code>SeShape</code> format
     */
    private double[][][] geometryToSdeCoords(Geometry jtsGeom)
    {
        int numParts;
        int numSubParts = 1;
        int numSubpartPoints;
        double[][][] sdeCoords;

        GeometryCollection gcol = null;

        //to simplify the algorithm, allways use GeometryCollections
        //as input geometries
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
     * Creates an array of linear coordinates as <code>SeShape</code> use  from
     * an array of JTS <code>Coordinate</code> objects
     *
     * @param coords wich coordinate to generate the linear array from
     *
     * @return the <code>SeShape</code> style coordinate array of a single
     *         geometry given by its <code>Coordinate</code> array
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
     * Builds a JTS Geometry who't type is given by the
     * <code>GeometryBuilder</code> subclass instance specialization that
     * implements it
     *
     * @param coords <code>SeShape</code>'s coordinate array to build the
     *        geometry from
     *
     * @return the JST form of the passed geometry coordinates
     *
     * @throws DataSourceException if an error occurs while creating the JTS
     *         Geometry
     */
    protected abstract Geometry newGeometry(double[][][] coords)
        throws DataSourceException;

    /**
     * returns an empty JTS geometry who's type is given by the
     * <code>GeometryBuilder</code> subclass instance specialization  that
     * implements it.
     *
     * <p>
     * this method is called in case that <code>SeShape.isNil() == true</code>
     * </p>
     *
     * @return an empty JTS geometry
     */
    protected static Geometry getEmpty()
    {
      throw new UnsupportedOperationException(
        "this method sholdn't be called directly, it's intended pourpose" +
        " is to be implemented by subclasses so they provide propper " +
        " null Geometries");
    }

    /**
     * Builds an array of JTS <code>Coordinate</code> instances that's
     * geometrically equals to the <code>SeShape</code> single coordinates
     * array passed as argument.
     *
     * @param coordList array of coordinates of a single shape part to build a
     *        <code>Coordinate</code> from
     *
     * @return a geometrically equal to <code>coordList</code> array of
     *         <code>Coordinate</code> instances
     */
    protected Coordinate[] toCoords(double[] coordList)
    {
        int nCoords = coordList.length / 2;

        Coordinate[] coords = new Coordinate[nCoords];

        for (int i = 0, j = 0; i < nCoords; i++, j++)
            coords[i] = new Coordinate(coordList[j], coordList[++j]);

        return coords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected SDEPoint[] toPointsArray(Coordinate[] coords)
    {
        int nCoords = coords.length;

        SDEPoint[] points = new SDEPoint[nCoords];

        Coordinate c;

        for (int i = 0; i < nCoords; i++)
        {
            c = coords[i];

            points[i] = new SDEPoint(c.x, c.y);
        }

        return points;
    }

    /**
     * Factory method that returns an instance of <code>GeometryBuilder</code>
     * specialized in contructing JTS geometries of the JTS Geometry class
     * passed as argument. Note that <code>jtsGeometryClass</code> must be one
     * of the supported concrete JTS Geometry classes.
     *
     * @param jtsGeometryClass
     *
     * @return
     *
     * @throws IllegalArgumentException if <code>jtsGeometryClass</code> is not
     *         a concrete JTS <code>Geometry</code> class (like
     *         <code>com.vividsolutions.jts.geom.MultiPoint.class</code>i.e.)
     */
    public static GeometryBuilder builderFor(Class jtsGeometryClass)
        throws IllegalArgumentException
    {
        GeometryBuilder builder = null;
        Class builderClass = (Class) builders.get(jtsGeometryClass);

        if (builderClass == null)
        {
            String msg = "no geometry builder is defined to construct "
                + jtsGeometryClass + " instances.";
            throw new IllegalArgumentException(msg);
        }

        try
        {
            builder = (GeometryBuilder) builderClass.newInstance();
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Cannot instantiate a "
                + builderClass.toString());
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Cannot instantiate a "
                + builderClass.toString());
        }

        return builder;
    }

    public static Geometry defaultValueFor(Class geoClass)
    {
      if(geoClass == null || geoClass.isAssignableFrom(Geometry.class))
      {
        throw new IllegalArgumentException(geoClass +
                                           " is not a valid Geometry subclass");
      }
      Geometry emptyGeom = (Geometry)nullGeometries.get(geoClass);
      return emptyGeom;
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>Point</code>s from <code>SeShape</code> points and viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class PointBuilder extends GeometryBuilder
{
    /** the empty point singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createPoint((Coordinate)null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        return factory.createPoint(new Coordinate(coords[0][0][0],
                coords[0][0][1]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param geometry DOCUMENT ME!
     * @param dest DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected void generateShape(Geometry geometry, SeShape dest)
        throws SeException
    {
        if (!(geometry instanceof Point))
        {
            throw new IllegalArgumentException("argument mus be a Point");
        }
        Point pointArg = (Point) geometry;
        SDEPoint[] sdePoint = new SDEPoint[1];
        sdePoint[0] = new SDEPoint(pointArg.getX(), pointArg.getY());
        dest.generatePoint(1, sdePoint);
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>MultiPoint</code>s from <code>SeShape</code> multipoints and
 * viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class MultiPointBuilder extends GeometryBuilder
{
    /** the empty multipoint singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createMultiPoint((Point[]) null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        int nPoints = coords[0].length;

        Coordinate[] points = new Coordinate[nPoints];

        for (int i = 0; i < nPoints; i++)
            points[i] = new Coordinate(coords[0][i][0], coords[0][i][1]);

        return factory.createMultiPoint(points);
    }

    /**
     * DOCUMENT ME!
     *
     * @param geometry DOCUMENT ME!
     * @param dest DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected void generateShape(Geometry geometry, SeShape dest)
        throws SeException
    {
        if (!(geometry instanceof MultiPoint))
        {
            throw new IllegalArgumentException("argument mus be a MultiPoint");
        }

        MultiPoint mpointArg = (MultiPoint) geometry;

        Point currPoint;

        int nPoints = mpointArg.getNumGeometries();

        SDEPoint[] sdePoints = new SDEPoint[nPoints];

        for (int i = 0; i < nPoints; i++)
        {
            currPoint = (Point) mpointArg.getGeometryN(i);

            sdePoints[i] = new SDEPoint(currPoint.getX(), currPoint.getY());
        }

        dest.generatePoint(1, sdePoints);
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>LineString</code>s from <code>SeShape</code> linestring and viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class LineStringBuilder extends GeometryBuilder
{
    /** the empty linestring singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createLineString(null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        return constructLineString(coords[0][0]);
    }

    //
    protected LineString constructLineString(double[] linearCoords)
        throws DataSourceException
    {
        LineString ls = null;

        Coordinate[] coords = toCoords(linearCoords);

        ls = factory.createLineString(coords);

        return ls;
    }

    /**
     * DOCUMENT ME!
     *
     * @param geometry DOCUMENT ME!
     * @param dest DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected void generateShape(Geometry geometry, SeShape dest)
        throws SeException
    {
        if (!(geometry instanceof LineString))
        {
            throw new IllegalArgumentException("argument mus be a LineString");
        }

        LineString lineString = (LineString) geometry;

        SDEPoint[] sdePoints = toPointsArray(lineString.getCoordinates());

        int numPoints = sdePoints.length;

        int numParts = 1; //it's allways 1 for geoms other than multipolygons

        int[] partOffsets = { 0 };

        dest.generateLine(numPoints, numParts, partOffsets, sdePoints);
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>MultiLineString</code>s from <code>SeShape</code> multilinestrings
 * and viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class MultiLineStringBuilder extends LineStringBuilder
{
    /** the empty multilinestring singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createMultiLineString(null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        MultiLineString mls = null;

        LineString[] lineStrings = null;

        int nLines = coords[0].length;

        lineStrings = new LineString[nLines];

        for (int i = 0; i < nLines; i++)
            lineStrings[i] = constructLineString(coords[0][i]);

        mls = factory.createMultiLineString(lineStrings);

        return mls;
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>Polygon</code>s from <code>SeShape</code> polygon and viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class PolygonBuilder extends GeometryBuilder
{
    /** the empty polygon singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createPolygon(null, null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords
     *
     * @return
     *
     * @throws DataSourceException
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        return buildPolygon(coords[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parts DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Polygon buildPolygon(double[][] parts)
    {
        Polygon p = null;

        double[] linearCoordArray = parts[0];

        int nHoles = parts.length - 1;

        LinearRing shell = factory.createLinearRing(toCoords(linearCoordArray));

        LinearRing[] holes = new LinearRing[nHoles];

        if (nHoles > 0)
        {
            for (int i = 0; i < nHoles; i++)
            {
                linearCoordArray = parts[i + 1];

                holes[i] = factory.createLinearRing(toCoords(linearCoordArray));
            }
        }

        p = factory.createPolygon(shell, holes);

        return p;
    }

    /**
     * DOCUMENT ME!
     *
     * @param geometry DOCUMENT ME!
     * @param dest DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected void generateShape(Geometry geometry, SeShape dest)
        throws SeException
    {
        MultiPolygon mp;

        if (geometry instanceof Polygon)
        {
            Polygon[] polys = { (Polygon) geometry };

            mp = factory.createMultiPolygon(polys);
        }
        else if (geometry instanceof MultiPolygon)
        {
            mp = (MultiPolygon) geometry;
        }
        else
        {
            throw new IllegalArgumentException(
                "argument mus be a Polygon or a MultiPolygon");
        }

        Polygon poly;

        int numParts = mp.getNumGeometries();

        List pointList = new ArrayList();

        int[] partOffsets = new int[numParts];

        int nextPartOffset = 0;

        for (int i = 0; i < numParts; i++)
        {
            partOffsets[i] = nextPartOffset;

            poly = (Polygon) mp.getGeometryN(i);

            nextPartOffset += (poly.getNumInteriorRing() + 1);

            SDEPoint[] polyPoints = toPointsArray(poly.getCoordinates());

            pointList.addAll(Arrays.asList(polyPoints));
        }

        int numPoints = pointList.size();

        SDEPoint[] ptArray = new SDEPoint[numPoints];

        pointList.toArray(ptArray);

        dest.generatePolygon(numPoints, numParts, partOffsets, ptArray);
    }
}


/**
 * <code>GeometryBuilder</code> specialized in creating  JTS
 * <code>MultiPolygon</code>s from <code>SeShape</code> multipolygons and
 * viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuilder.java,v 1.5 2003/11/14 17:21:04 groldan Exp $
 */
class MultiPolygonBuilder extends PolygonBuilder
{
    /** the empty multipolygon singleton */
    private static Geometry EMPTY;

    /**
     * DOCUMENT ME!
     *
     * @return an empty multipolygon
     */
    protected static Geometry getEmpty()
    {
        if (EMPTY == null)
        {
            EMPTY = new GeometryFactory().createMultiPolygon(null);
        }

        return EMPTY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords the SeShape's multipolygon coordinates array
     *
     * @return a <code>MultiPolygon</code> constructed based on the SDE shape,
     *         or the empty geometry if the <code>shape == null ||
     *         shape.isNil()</code>
     *
     * @throws DataSourceException if it is not possible to obtain the shape's
     *         coordinate arrays or an exception occurs while building the
     *         Geometry
     */
    protected Geometry newGeometry(double[][][] coords)
        throws DataSourceException
    {
        Polygon[] polys = null;

        int numPolys = coords.length;

        polys = new Polygon[numPolys];

        for (int i = 0; i < numPolys; i++)
        {
            try
            {
                polys[i] = buildPolygon(coords[i]);
            }
            catch (Exception ex1)
            {
                ex1.printStackTrace();
            }
        }

        MultiPolygon multiPoly = factory.createMultiPolygon(polys);

        return multiPoly;
    }
}
