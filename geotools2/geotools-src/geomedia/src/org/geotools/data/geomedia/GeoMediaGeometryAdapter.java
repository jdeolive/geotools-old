/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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

package org.geotools.data.geomedia;

import com.mindprod.ledatastream.LEDataInputStream;
import com.mindprod.ledatastream.LEDataOutputStream;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * Converts geometry objects between JTS geometry types and GeoMedia GDO serlaized geometry blobs. The following tables
 * describe the mappings:
 * 
 * <P></p>
 * 
 * <P></p>
 *
 * @author Julian J. Ray
 * @version 1.0
 *
 * @todo Add interior polygons to Polygon constructor
 * @todo Add support multi-polygon types
 * @todo Add support for GeoMedia Arcs
 */
public class GeoMediaGeometryAdapter {
    // These are the Windows-based GUIDs used to identify each GeoMedia geometry type. Not used but here for reference!

    /** DOCUMENT ME! */
    private static String[] mGdoGuidStrings = {
        "0FD2FFC0-8CBC-11CF-ABDE-08003601B769", // Point Geometry
        "0FD2FFC8-8CBC-11CF-ABDE-08003601B769", // Oriented Point Geometry
        "0FD2FFC9-8CBC-11CF-ABDE-08003601B769", // Text Point Geometry
        "0FD2FFC1-8CBC-11CF-ABDE-08003601B769", // Line Geometry
        "0FD2FFC2-8CBC-11CF-ABDE-08003601B769", // Polyline Geometry
        "0FD2FFC3-8CBC-11CF-ABDE-08003601B769", // Polygon Geometry
        "0FD2FFC7-8CBC-11CF-ABDE-08003601B769", // Rectangle Geometry
        "0FD2FFC5-8CBC-11CF-ABDE-08003601B769", // Boundary Geometry
        "0FD2FFC6-8CBC-11CF-ABDE-08003601B769", // Geometry Collection (Hetereogeneous)
        "0FD2FFCB-8CBC-11CF-ABDE-08003601B769", // Composite Polyline Geometry
        "0FD2FFCC-8CBC-11CF-ABDE-08003601B769", // Composite Polygon Geometry
        "0FD2FFCA-8CBC-11CF-ABDE-08003601B769" // Arc Geometry
    };

    // This is the format of non-significant component of the GDO GUID headers represented
    // as signed ints. These are written as unsigned bytes to the output stream

    /** DOCUMENT ME! */
    private static int[] mGdoGuidByteArray = { 210, 15, 188, 140, 207, 17, 171, 222, 8, 0, 54, 1, 183, 105 };

    // Used to construct JTS geometry objects

    /** DOCUMENT ME! */
    GeometryFactory mGeometryFactory;

    /** DOCUMENT ME! */
    private Hashtable mTypeMapping;

    /**
     * Creates a new GeoMediaGeometryAdapter object.
     */
    public GeoMediaGeometryAdapter() {
        // We cache a JTS geometry factory for expediency
        mGeometryFactory = new GeometryFactory();

        // Set up the geometry type mappings
        mTypeMapping = new Hashtable();
        mTypeMapping.put(new Integer(65472), Point.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65480), Point.class); // removes orientation component
        mTypeMapping.put(new Integer(65481), Point.class); // Will not show the actual text but will indicate anchor points
        mTypeMapping.put(new Integer(65473), LineString.class); // Converts simple lines to line strings
        mTypeMapping.put(new Integer(65474), LineString.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65475), Polygon.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65479), Polygon.class); // Interpret rectangles as simple polygons
        mTypeMapping.put(new Integer(65477), Polygon.class); // Interpret boundary polygons as simple polygons with no islands
        mTypeMapping.put(new Integer(65478), GeometryCollection.class); // Heterogeneous collection
        mTypeMapping.put(new Integer(65484), MultiLineString.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65483), MultiPolygon.class); // Todo: Fix this

        //mTypeMapping.put(new Integer(65482), null);                   // No equivalent for arcs: TODO look for a stroking algorithm to piecewise it
    }

    /**
     * Converts GeoMedia blobs to JTS geometry types. Performs endian-conversion on data contained in the blob.
     *
     * @param input GeoMedia geometry blob read from geomedia spatial database.
     *
     * @return JTS Geometry
     *
     * @throws IOException
     * @throws GeoMediaGeometryTypeNotKnownException
     * @throws GeoMediaUnsupportedGeometryTypeException
     */
    public Geometry deSerialize(byte[] input)
        throws IOException, GeoMediaGeometryTypeNotKnownException, GeoMediaUnsupportedGeometryTypeException {
        Geometry geom = null;

        if (input == null) {
            return geom;
        }

        // 40 bytes is the minimum size for a point geometry
        if (input.length < 40) {
            return geom;
        }

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(input);
        LEDataInputStream    binaryReader = new LEDataInputStream(byteInputStream);

        // Extract the 16 byte GUID on the front end
        // First 2 bytes (short) is the index into the type map
        int   uidNum = binaryReader.readUnsignedShort(); // reads first byte
        int[] vals = new int[14];

        for (int i = 0; i < 14; i++) {
            vals[i] = binaryReader.readUnsignedByte();
        }

        //byte[] temp = new byte[14]; // Read the rest of the 16 byte GUID header
        //binaryReader.readFully(temp);
        Class geomType = (Class) mTypeMapping.get(new Integer(uidNum));

        if (geomType == null) {
            throw new GeoMediaGeometryTypeNotKnownException();
        }

        // Delegate to the appropriate de-serializer. Throw exceptions if we come across a geometry we have not yet supported
        if (geomType == Point.class) {
            geom = createPointGeometry(binaryReader);
        } else if (geomType == LineString.class) {
            geom = createLineStringGeometry(uidNum, binaryReader);
        } else if (geomType == MultiLineString.class) {
            geom = createMultiLineStringGeometry(uidNum, binaryReader);
        } else if (geomType == Polygon.class) {
            geom = createPolygonGeometry(uidNum, binaryReader);
        } else if (geomType == GeometryCollection.class) {
            geom = createGeometryCollectionGeometry(binaryReader);
        } else if (geomType == MultiPolygon.class) { // TODO: Support this type
            throw new GeoMediaUnsupportedGeometryTypeException();
        }

        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param input DOCUMENT ME!
     * @param binaryWriter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void writeGUID(Geometry input, LEDataOutputStream binaryWriter)
        throws IOException {
        short guidFlag = 0;

        if (input instanceof Point) {
            guidFlag = (short) 65472;
        } else if (input instanceof LineString) {
            guidFlag = (short) 65474;
        } else if (input instanceof Polygon) {
            guidFlag = (short) 65475;
        }

        // No need to worry about anything else as the calling function takes care of unsupported types
        binaryWriter.writeShort(guidFlag);

        for (int i = 0; i < mGdoGuidByteArray.length; i++) {
            binaryWriter.writeByte(mGdoGuidByteArray[i]);
        }
    }

    /**
     * Converts a JTS geometry to a GeoMedia geometry blob which can be stored in a geomedia spatial database.
     *
     * @param input JTS Geometry
     *
     * @return byte[] GeoMedia blob format
     *
     * @throws IOException
     * @throws GeoMediaUnsupportedGeometryTypeException
     *
     * @todo Figure out how to write the GDO_BOUNDS_XHI etc. bounding box for SQL Server data stores
     */
    public byte[] serialize(Geometry input) throws IOException, GeoMediaUnsupportedGeometryTypeException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LEDataOutputStream    binaryWriter = new LEDataOutputStream(outputStream);

        if (input instanceof Point) {
            Point      geom = (Point) input;
            Coordinate c = geom.getCoordinate();

            writeGUID(input, binaryWriter);
            binaryWriter.writeDouble(c.x);
            binaryWriter.writeDouble(c.y);
            binaryWriter.writeDouble((double) 0.0);
        } else if (input instanceof LineString) {
            LineString   geom = (LineString) input;
            Coordinate[] coords = geom.getCoordinates();

            writeGUID(input, binaryWriter);
            binaryWriter.writeInt(coords.length);

            for (int i = 0; i < coords.length; i++) {
                binaryWriter.writeDouble(coords[i].x);
                binaryWriter.writeDouble(coords[i].y);
                binaryWriter.writeDouble((double) 0.0);
            }
        } else if (input instanceof MultiLineString) {
            MultiLineString geom = (MultiLineString) input;
            int             numGeoms = geom.getNumGeometries();

            writeGUID(input, binaryWriter);
            binaryWriter.writeInt(numGeoms);

            for (int i = 0; i < numGeoms; i++) {
                // Use recursion to serialize all the sub-geometries
                byte[] b = serialize(geom.getGeometryN(i));
                binaryWriter.writeInt(b.length);
                binaryWriter.write(b);
            }
        } else if (input instanceof Polygon) {
            Polygon      geom = (Polygon) input;
            Coordinate[] coords = geom.getExteriorRing().getCoordinates();

            writeGUID(input, binaryWriter);
            binaryWriter.writeInt(coords.length);

            for (int i = 0; i < coords.length; i++) {
                binaryWriter.writeDouble(coords[i].x);
                binaryWriter.writeDouble(coords[i].y);
                binaryWriter.writeDouble((double) 0.0);
            }
        } else {
            // Choke if we can't handle the geometry type yet...
            throw new GeoMediaUnsupportedGeometryTypeException();
        }

        return outputStream.toByteArray();
    }

    /**
     * Constructs and returns a JTS Point geometry
     *
     * @param x double
     * @param y double
     *
     * @return Point
     */
    private Point createPointGeometry(double x, double y) {
        return mGeometryFactory.createPoint(new Coordinate(x, y));
    }

    /**
     * Constructs and returns a JTS Point geometry by reading from the byte stream.
     *
     * @param reader LEDataInputStream reading from byte array containing a GeoMedia blob.
     *
     * @return Point
     *
     * @throws IOException
     */
    private Point createPointGeometry(LEDataInputStream reader)
        throws IOException {
        return createPointGeometry(reader.readDouble(), reader.readDouble());
    }

    /**
     * Constructs and returns a JTS LineString geometry.
     *
     * @param elems double[]
     *
     * @return LineString
     */
    private LineString createLineStringGeometry(double[] elems) {
        CoordinateList list = new CoordinateList();

        // Check to see if the elems list is long enough
        if ((elems.length != 0) && (elems.length < 4)) {
            return null;
        }

        // Watch for incorrectly encoded string from the server
        if ((elems.length % 2) != 0) {
            return null;
        }

        for (int i = 0; i < elems.length;) {
            list.add(new Coordinate(elems[i], elems[i + 1]));
            i += 2;
        }

        return mGeometryFactory.createLineString(CoordinateArrays.toCoordinateArray(list));
    }

    /**
     * Constructs and returns a JTS LineString geometry from a GDO blob.
     *
     * @param guid int GeoMedia GUID flag.
     * @param reader LEDataInputStream
     *
     * @return LineString
     *
     * @throws IOException
     */
    private LineString createLineStringGeometry(int guid, LEDataInputStream reader)
        throws IOException {
        double[] a = null;

        if (guid == 65473) { // GDO Line Geometry
            a = new double[4];
            a[0] = reader.readDouble(); // x1
            a[1] = reader.readDouble(); // y1
            reader.readDouble(); // z1;
            a[2] = reader.readDouble(); // x2
            a[3] = reader.readDouble(); // y2
        } else { // GDO Polyline Geometry

            int numOrdinates = reader.readInt();
            a = new double[numOrdinates * 2];

            for (int i = 0; i < numOrdinates; i++) {
                a[2 * i] = reader.readDouble(); // xn
                a[(2 * i) + 1] = reader.readDouble(); // yn
                reader.readDouble(); // zn
            }
        }

        return createLineStringGeometry(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param lineStrings DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private MultiLineString createMultiLineStringGeometry(ArrayList lineStrings) {
        LineString[] array = new LineString[lineStrings.size()];

        for (int i = 0; i < lineStrings.size(); i++) {
            array[i] = (LineString) lineStrings.get(i);
        }

        return mGeometryFactory.createMultiLineString(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param guid DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws GeoMediaUnsupportedGeometryTypeException DOCUMENT ME!
     * @throws GeoMediaGeometryTypeNotKnownException DOCUMENT ME!
     */
    private MultiLineString createMultiLineStringGeometry(int guid, LEDataInputStream reader)
        throws IOException, GeoMediaUnsupportedGeometryTypeException, GeoMediaGeometryTypeNotKnownException {
        // get the number of items in the collection
        int numItems = reader.readInt();

        // This is to hold the geometries from the collection
        ArrayList array = new ArrayList();

        for (int i = 0; i < numItems; i++) {
            // Read the size of the next blob
            int elemSize = reader.readInt();

            // Recursively create a geometry from this blob
            byte[] elem = new byte[elemSize];
            reader.readFully(elem);

            Geometry g = deSerialize(elem);
            array.add(g);
        }

        // Now we need to append these items together
        return createMultiLineStringGeometry(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param elems DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Polygon createPolygonGeometry(double[] elems) {
        CoordinateList list = new CoordinateList();

        // Check to see if the elems list is long enough
        if ((elems.length != 0) && (elems.length <= 6)) {
            return null;
        }

        // Watch for incorrectly encoded string from the server
        if ((elems.length % 2) != 0) {
            return null;
        }

        for (int i = 0; i < elems.length;) {
            list.add(new Coordinate(elems[i], elems[i + 1]));
            i += 2;
        }

        LinearRing ring = mGeometryFactory.createLinearRing(CoordinateArrays.toCoordinateArray(list));

        return mGeometryFactory.createPolygon(ring, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param guid DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private Polygon createPolygonGeometry(int guid, LEDataInputStream reader)
        throws IOException {
        double[] a = null;

        if (guid == 65475) { // Polygon Geometry

            int numOrdinates = reader.readInt();
            a = new double[numOrdinates * 2];

            for (int i = 0; i < numOrdinates; i++) {
                a[2 * i] = reader.readDouble();
                a[(2 * i) + 1] = reader.readDouble();
                reader.readDouble();
            }
        } else if (guid == 65479) { // Rectangle geomety

            // x, y, z, width, height
            double x = reader.readDouble();
            double y = reader.readDouble();
            double z = reader.readDouble();
            double w = reader.readDouble();
            double h = reader.readDouble();

            a = new double[8];
            a[0] = x;
            a[1] = y;
            a[2] = x + w;
            a[3] = y;
            a[4] = x + w;
            a[5] = y + h;
            a[6] = x;
            a[7] = y + h;
        }

        return createPolygonGeometry(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws GeoMediaUnsupportedGeometryTypeException DOCUMENT ME!
     * @throws GeoMediaGeometryTypeNotKnownException DOCUMENT ME!
     */
    private GeometryCollection createGeometryCollectionGeometry(LEDataInputStream reader)
        throws IOException, GeoMediaUnsupportedGeometryTypeException, GeoMediaGeometryTypeNotKnownException {
        // get the number of items in the collection
        int numItems = reader.readInt();

        // This is to hold the geometries from the collection
        ArrayList array = new ArrayList();

        for (int i = 0; i < numItems; i++) {
            // Read the size of the next blob
            int elemSize = reader.readInt();

            // Recursively create a geometry from this blob
            byte[] elem = new byte[elemSize];
            reader.readFully(elem);

            Geometry g = deSerialize(elem);

            if (g != null) {
                array.add(g);
            }
        }

        // Now we need to append these items together
        return createGeometryCollectionGeometry(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param geoms DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private GeometryCollection createGeometryCollectionGeometry(ArrayList geoms) {
        Geometry[] array = new Geometry[geoms.size()];

        for (int i = 0; i < geoms.size(); i++) {
            array[i] = (Geometry) geoms.get(i);
        }

        return mGeometryFactory.createGeometryCollection(array);
    }
}
