/*
 * Header.java
 *
 * Created on February 12, 2002, 3:29 PM
 */
package org.geotools.shapefile.shapefile;

import com.vividsolutions.jts.geom.*;

import org.geotools.shapefile.endian.*;

import java.io.*;


/**
 *
 * @author  jamesm
 */
public class ShapefileHeader {
    private final static boolean DEBUG = false;
    private int fileCode = -1;
    public int fileLength = -1;
    private int indexLength = -1;
    private int version = -1;
    private int shapeType = -1;
    private Envelope bounds;

    public ShapefileHeader(EndianDataInputStream file)
        throws IOException {
        fileCode = file.readIntBE();

        if (fileCode != Shapefile.SHAPEFILE_ID) {
            System.err.println("Sfh->WARNING filecode " + fileCode +
                " not a match for documented shapefile code " + Shapefile.SHAPEFILE_ID);
        }

        for (int i = 0; i < 5; i++) {
            int tmp = file.readIntBE();
        }

        fileLength = file.readIntBE();

        version = file.readIntLE();
        shapeType = file.readIntLE();

        //read in and store the bounding box
        double[] coords = new double[4];
        for (int i = 0; i < 4; i++){
            coords[i]= file.readDoubleLE();
        }
        bounds = new Envelope(coords[0], coords[2], coords[1], coords[3]);

        //skip remaining unused bytes
        file.skipBytes(32);
    }


    public ShapefileHeader(GeometryCollection geometries, int dims)
        throws Exception {
        ShapeHandler handle;

        if (geometries.getNumGeometries() == 0) {
            handle = new PointHandler(); //default
        } else {
            handle = Shapefile.getShapeHandler(geometries.getGeometryN(0), dims);
        }

        int numShapes = geometries.getNumGeometries();
        shapeType = handle.getShapeType();
        version = Shapefile.VERSION;
        fileCode = Shapefile.SHAPEFILE_ID;
        bounds = geometries.getEnvelopeInternal();
        fileLength = 0;

        for (int i = 0; i < numShapes; i++) {
            fileLength += handle.getLength(geometries.getGeometryN(i));
            fileLength += 4; //for each header
        }

        fileLength += 50; //space used by this, the main header
        indexLength = 50 + (4 * numShapes);
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getFileLength() {
        return this.fileLength;
    }
    
    public void write(EndianDataOutputStream file) throws IOException {
        int pos = 0;

        file.writeIntBE(fileCode);
        pos += 4;

        for (int i = 0; i < 5; i++) {
            file.writeIntBE(0); //Skip unused part of header
            pos += 4;
        }

        file.writeIntBE(fileLength);
        pos += 4;

        file.writeIntLE(version);
        pos += 4;
        file.writeIntLE(shapeType);
        pos += 4;

        file.writeDoubleLE(bounds.getMinX());
        file.writeDoubleLE(bounds.getMinY());
        file.writeDoubleLE(bounds.getMaxX());
        file.writeDoubleLE(bounds.getMaxY());
        pos += (8 * 4);

        //skip remaining unused bytes
        for (int i = 0; i < 4; i++) {
            file.writeDoubleLE(0.0); //Skip unused part of header
            pos += 8;
        }

        if (DEBUG) {
            System.out.println("Sfh->Position " + pos);
        }
    }


    public void writeToIndex(EndianDataOutputStream file)
        throws IOException {
        int pos = 0;

        file.writeIntBE(fileCode);
        pos += 4;

        for (int i = 0; i < 5; i++) {
            file.writeIntBE(0); //Skip unused part of header
            pos += 4;
        }

        file.writeIntBE(indexLength);
        pos += 4;

        file.writeIntLE(version);
        pos += 4;
        file.writeIntLE(shapeType);
        pos += 4;

        //write the bounding box
        pos += 8;
        file.writeDoubleLE(bounds.getMinX());
        pos += 8;
        file.writeDoubleLE(bounds.getMinY());
        pos += 8;
        file.writeDoubleLE(bounds.getMaxX());
        pos += 8;
        file.writeDoubleLE(bounds.getMaxY());

        //skip remaining unused bytes
        for (int i = 0; i < 4; i++) {
            file.writeDoubleLE(0.0); //Skip unused part of header
            pos += 8;
        }

        if (DEBUG) {
            System.out.println("Sfh->Index Position " + pos);
        }
    }


    public int getShapeType() {
        return shapeType;
    }


    public int getVersion() {
        return version;
    }


    public Envelope getBounds() {
        return bounds;
    }


    public String toString() {
        String res = new String("Sf-->type " + fileCode + " size " + fileLength + " version " +
                version + " Shape Type " + shapeType);

        return res;
    }
}
