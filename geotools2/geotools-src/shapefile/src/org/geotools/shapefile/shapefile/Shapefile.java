package org.geotools.shapefile.shapefile;

import com.vividsolutions.jts.geom.*;

import org.geotools.shapefile.endian.*;

import java.io.*;

import java.lang.*;

import java.net.*;

import java.util.*;


/**
 *
 * This class represnts an ESRI Shape file.<p>
 * You construct it with a file name, and later
 * you can read the file's propertys, i.e. Sizes, Types, and the data itself.<p>
 * Copyright 1998 by James Macgill. <p>
 *
 * Version 1.0beta1.1 (added construct with inputstream)
 * 1.0beta1.2 (made Shape type constants public 18/Aug/98)
 *
 * This class supports the Shape file as set out in :-<br>
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf"><b>"ESRI(r) Shapefile - A Technical Description"</b><br>
 * <i>'An ESRI White Paper . May 1997'</i></a><p>
 *
 * This code is coverd by the LGPL.
 *
 * <a href="mailto:j.macgill@geog.leeds.ac.uk">Mail the Author</a>
 */
public class Shapefile {
    static final int SHAPEFILE_ID = 9994;
    static final int VERSION = 1000;
    public static final int NULL = 0;
    public static final int POINT = 1;
    public static final int POINTZ = 11;
    public static final int POINTM = 21;
    public static final int ARC = 3;
    public static final int ARCM = 23;
    public static final int ARCZ = 13;
    public static final int POLYGON = 5;
    public static final int POLYGONM = 25;
    public static final int POLYGONZ = 15;
    public static final int MULTIPOINT = 8;
    public static final int MULTIPOINTM = 28;
    public static final int MULTIPOINTZ = 18;
    public static final int UNDEFINED = -1;
    //Types 2,4,6,7 and 9 were undefined at time or writeing
    
    private URL baseURL;

    /**
     * Creates and initialises a shapefile from a url
     * @param url The url of the shapefile
     */
    public Shapefile(URL url) throws java.io.IOException {
        baseURL = url;
        // simple test just to verify that the baseURL makes sense
        URLConnection uc = baseURL.openConnection();
    }

    /**
     * Opens a buffered endian input stream from the provided url. 
     */
    private EndianDataInputStream getInputStream() throws IOException {
        InputStream is = baseURL.openConnection().getInputStream();
        if(is == null)
            throw new IOException("Could make a connection to the URL: " + baseURL);

        return new EndianDataInputStream(new BufferedInputStream(is));
    }
    
    /**
     * Opens an endian buffered output stream from the provided url
     */
    private EndianDataOutputStream getOutputStream() throws IOException {
        // OutputStream os = baseURL.openConnection().getOutputStream();
        OutputStream os = new FileOutputStream(baseURL.getFile());
        return new EndianDataOutputStream(new BufferedOutputStream(os));
    }
    

    /**
     * Gets the bounds from the shapefile's header. Warning, opens the
     * shape file each time is called, use with care.
     */
    public com.vividsolutions.jts.geom.Envelope getBounds() throws IOException {
        return getHeader().getBounds();
    }
    
    /**
     * Returns the whole shapefile header. Warning, opens the
     * shape file each time is called, use with care.
     */
    public ShapefileHeader getHeader() throws IOException {
        EndianDataInputStream file = null;
        ShapefileHeader mainHeader = null;
        try {
            file = getInputStream();
            mainHeader = new ShapefileHeader(file);
        } finally {
            if(file != null) {
                try { 
                    file.close(); 
                } catch(Exception e) {}
            }
        }
            
        return mainHeader;
    }

    /**
     * Initialises a shapefile from disk.
     * Use Shapefile(String) if you don't want to use LEDataInputStream directly (recomened)
     * @param file A LEDataInputStream that conects to the shapefile to read
     */
    public GeometryCollection read(GeometryFactory geometryFactory)
        throws IOException, ShapefileException {
        EndianDataInputStream file = getInputStream();
        ArrayList list = null;

        try {
            if (file == null) {
                throw new IOException("Failed connection or no content for " + baseURL);
            }

            ShapefileHeader mainHeader = new ShapefileHeader(file);

            if (mainHeader.getVersion() < VERSION) {
                System.err.println("Sf-->Warning, Shapefile format (" + mainHeader.getVersion() +
                    ") older that supported (" + VERSION + "), attempting to read anyway");
            }

            if (mainHeader.getVersion() > VERSION) {
                System.err.println("Sf-->Warning, Shapefile format (" + mainHeader.getVersion() +
                    ") newer that supported (" + VERSION + "), attempting to read anyway");
            }

            Geometry body;
            list = new ArrayList();
            int type = mainHeader.getShapeType();
            ShapeHandler handler = getShapeHandler(type);

            if (handler == null) {
                throw new ShapeTypeNotSupportedException("Unsuported shape type:" + type);
            }

            int recordNumber = 0;
            int contentLength = 0;

            try {
                while (true) {
                    recordNumber = file.readIntBE();
                    contentLength = file.readIntBE();

                    body = handler.read(file, geometryFactory, contentLength - 4); //-4 is the recordNumber/contentlength size
                    if(body != null)
                        list.add(body);
                }
            } catch (EOFException e) {
            } 
        } finally {
            if(file != null) {
                try { 
                    file.close(); 
                } catch(Exception e) {}
            }
        }

        return geometryFactory.createGeometryCollection((Geometry[]) list.toArray(
                new Geometry[] {  }));
    }


    /**
     * Saves a shapefile to and output stream.
     * @param file A LEDataInputStream that conects to the shapefile to read
     */

    //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m
    public void write(GeometryCollection geometries, int ShapeFileDimentions)
        throws IOException, Exception {
        EndianDataOutputStream file = getOutputStream();
        ShapefileHeader mainHeader = new ShapefileHeader(geometries, ShapeFileDimentions);
        mainHeader.write(file);

        int pos = 50; // header length in WORDS

        int numShapes = geometries.getNumGeometries();
        Geometry body;
        ShapeHandler handler;

        if (geometries.getNumGeometries() == 0) {
            handler = new PointHandler(); //default
        } else {
            handler = Shapefile.getShapeHandler(geometries.getGeometryN(0), ShapeFileDimentions);
        }

        for (int i = 0; i < numShapes; i++) {
            body = geometries.getGeometryN(i);

            //file.setLittleEndianMode(false);
            file.writeIntBE(i + 1);
            file.writeIntBE(handler.getLength(body) + 4);

            // file.setLittleEndianMode(true);
            pos += 4; // length of header in WORDS
            handler.write(body, file);
            pos += handler.getLength(body); // length of shape in WORDS
        }

        file.flush();
        file.close();
    }


    //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m
    public synchronized void writeIndex(GeometryCollection geometries, URL url,
        int ShapeFileDimentions) throws IOException, Exception {
        Geometry geom;
        
        EndianDataOutputStream file = new EndianDataOutputStream(new FileOutputStream(
                    new File(url.getFile())));


        ShapeHandler handler;
        int nrecords = geometries.getNumGeometries();
        ShapefileHeader mainHeader = new ShapefileHeader(geometries, ShapeFileDimentions);

        if (geometries.getNumGeometries() == 0) {
            handler = new PointHandler(); //default
        } else {
            handler = Shapefile.getShapeHandler(geometries.getGeometryN(0), ShapeFileDimentions);
        }

        // mainHeader.fileLength = 50 + 4*nrecords;
        mainHeader.writeToIndex(file);

        int pos = 50;
        int len = 0;

        //file.setLittleEndianMode(false);
        for (int i = 0; i < nrecords; i++) {
            geom = geometries.getGeometryN(i);
            len = handler.getLength(geom) + 4;

            file.writeIntBE(pos);
            file.writeIntBE(len);
            pos = pos + len;
        }

        file.flush();
        file.close();
    }


    /**
     * Returns a string for the shape type of index.
     * @param index An int coresponding to the shape type to be described
     * @return A string descibing the shape type
     */
    public static String getShapeTypeDescription(int index) {
        switch (index) {
            case (NULL):
                return ("Null");

            case (POINT):
                return ("Points");

            case (POINTZ):
                return ("Points Z");

            case (POINTM):
                return ("Points M");

            case (ARC):
                return ("Arcs");

            case (ARCM):
                return ("ArcsM");

            case (ARCZ):
                return ("ArcsM");

            case (POLYGON):
                return ("Polygon");

            case (POLYGONM):
                return ("PolygonM");

            case (POLYGONZ):
                return ("PolygonZ");

            case (MULTIPOINT):
                return ("Multipoint");

            case (MULTIPOINTM):
                return ("MultipointM");

            case (MULTIPOINTZ):
                return ("MultipointZ");

            default:
                return ("Undefined");
        }
    }


    public static ShapeHandler getShapeHandler(Geometry geom, int ShapeFileDimentions)
        throws Exception {
        return getShapeHandler(getShapeType(geom, ShapeFileDimentions));
    }


    public static ShapeHandler getShapeHandler(int type) throws InvalidShapefileException {
        switch (type) {
            case Shapefile.POINT:
                return new PointHandler();

            case Shapefile.POINTZ:
                return new PointHandler(Shapefile.POINTZ);

            case Shapefile.POINTM:
                return new PointHandler(Shapefile.POINTM);

            case Shapefile.POLYGON:
                return new PolygonHandler();

            case Shapefile.POLYGONM:
                return new PolygonHandler(Shapefile.POLYGONM);

            case Shapefile.POLYGONZ:
                return new PolygonHandler(Shapefile.POLYGONZ);

            case Shapefile.ARC:
                return new MultiLineHandler();

            case Shapefile.ARCM:
                return new MultiLineHandler(Shapefile.ARCM);

            case Shapefile.ARCZ:
                return new MultiLineHandler(Shapefile.ARCZ);

            case Shapefile.MULTIPOINT:
                return new MultiPointHandler();

            case Shapefile.MULTIPOINTM:
                return new MultiPointHandler(Shapefile.MULTIPOINTM);

            case Shapefile.MULTIPOINTZ:
                return new MultiPointHandler(Shapefile.MULTIPOINTZ);
        }

        return null;
    }


    //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m
    public static int getShapeType(Geometry geom, int ShapeFileDimentions)
        throws ShapefileException {
        if ((ShapeFileDimentions != 2) && (ShapeFileDimentions != 3) && (ShapeFileDimentions != 4)) {
            throw new ShapefileException(
                "invalid ShapeFileDimentions for getShapeType - expected 2,3,or 4 but got " +
                ShapeFileDimentions + "  (2=x,y ; 3=x,y,m ; 4=x,y,z,m)");

            //ShapeFileDimentions = 2;
        }

        if (geom instanceof Point) {
            switch (ShapeFileDimentions) {
                case 2:
                    return Shapefile.POINT;

                case 3:
                    return Shapefile.POINTM;

                case 4:
                    return Shapefile.POINTZ;
            }
        }

        if (geom instanceof MultiPoint) {
            switch (ShapeFileDimentions) {
                case 2:
                    return Shapefile.MULTIPOINT;

                case 3:
                    return Shapefile.MULTIPOINTM;

                case 4:
                    return Shapefile.MULTIPOINTZ;
            }
        }

        if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
            switch (ShapeFileDimentions) {
                case 2:
                    return Shapefile.POLYGON;

                case 3:
                    return Shapefile.POLYGONM;

                case 4:
                    return Shapefile.POLYGONZ;
            }
        }

        if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
            switch (ShapeFileDimentions) {
                case 2:
                    return Shapefile.ARC;

                case 3:
                    return Shapefile.ARCM;

                case 4:
                    return Shapefile.ARCZ;
            }
        }

        return Shapefile.UNDEFINED;
    }


    public synchronized ArrayList readIndex()
        throws IOException {
        EndianDataInputStream file = getInputStream();

        ShapefileHeader head = new ShapefileHeader(file);

        int pos = 0;
        int len = 0;
        
        ArrayList indexes = new ArrayList();
        
        try {
            while(true) {
                IndexRecord ir = new IndexRecord();
                ir.offset = file.readIntBE();
                ir.length = file.readIntBE();
                indexes.add(ir);
            }
        } catch (EOFException e) {}

        //file.setLittleEndianMode(false);
        file.close();
        
        return indexes;
    }
    
    public class IndexRecord {
        public int offset;
        public int length;
    }
    
}
