/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.shapefile;

import cmp.LEDataStream.LEDataInputStream;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;

//Logging system
import org.apache.log4j.Logger;


/**
 * This class represnts an ESRI Shapefile.<p>
 * You construct it with a file name, and later you can read the file's
 * properties, i.e. Sizes, Types, and the data itself.<p>
 *
 * This class supports the Shapefile as set out in:-<br>
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">
 * <b>"ESRI(r) Shapefile - A Technical Description"</b><br>
 * <i>'An ESRI White Paper. May 1997'</i></a><p>
 *
 * @version $Id: Shapefile.java,v 1.9 2002/07/19 13:04:31 jmacgill Exp $
 * @author James Macgill, CCG
 */

public class Shapefile  {
    
    static final int    SHAPEFILE_ID = 9994;
    static final int    VERSION = 1000;
    
    public static final int    NULL = 0;
    public static final int    POINT = 1;
    public static final int    ARC = 3;
    public static final int    POLYGON = 5;
    public static final int    MULTIPOINT = 8;
    public static final int    ARC_M = 23;
    public static final int    UNDEFINED = -1;
    //Types 2,4,6,7 and 9 were undefined at time of writing
    
    private java.net.URL baseURL;
    private boolean initialized = false;
    private LEDataInputStream inStream = null;
    private ShapefileHeader mainHeader = null;
    private com.vividsolutions.jts.geom.Envelope bounds = null;
    
    private static Logger log = Logger.getLogger(Shapefile.class);
    /**
     * Creates and initialises a shapefile from a url.
     * @param url The url of the shapefile.
     */
    public Shapefile(java.net.URL url){
        baseURL = url;
    }
    
    private LEDataInputStream getInputStream() throws IOException{
        if (initialized) {
            return inStream;
        }
        java.net.URLConnection uc = baseURL.openConnection();
        int len = uc.getContentLength();
        if (len <= 0){
            return null;
        }
        java.io.BufferedInputStream in = new java.io.BufferedInputStream(uc.getInputStream());
        LEDataInputStream sfile = new LEDataInputStream(in);
        return sfile;
    }
    
    private cmp.LEDataStream.LEDataOutputStream getOutputStream() throws IOException{

        java.net.URLConnection connection = baseURL.openConnection();
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        java.io.BufferedOutputStream in = new java.io.BufferedOutputStream(connection.getOutputStream());
        cmp.LEDataStream.LEDataOutputStream sfile = new cmp.LEDataStream.LEDataOutputStream(in);
        return sfile;
    }
    
    private LEDataInputStream setup() throws IOException{
        if (initialized){
            return inStream;
        }
        inStream = getInputStream();
        if (inStream == null){
            throw new IOException("Failed connection or no content for " + baseURL);
        }
        mainHeader = new ShapefileHeader(inStream);
        bounds = mainHeader.getBounds();
        initialized = true;
        return inStream;
    }
    /**
     * Initialises a shapefile from disk.
     * Use Shapefile(String) if you don't want to use LEDataInputStream
     * directly (recommended).
     * @param geometryFactory Factory to use when constructing JTS geometries.
     */
    public com.vividsolutions.jts.geom.GeometryCollection read(com.vividsolutions.jts.geom.GeometryFactory geometryFactory) throws IOException, ShapefileException, com.vividsolutions.jts.geom.TopologyException {
        
        LEDataInputStream file = setup();
        if (file == null) {
            throw new IOException("Failed connection or no content for " + baseURL);
        }
        
        
        if (mainHeader.getVersion() < VERSION){System.err.println("Sf-->Warning, Shapefile format (" + mainHeader.getVersion() + ") older that supported (" + VERSION + "), attempting to read anyway");}
        if (mainHeader.getVersion() > VERSION){System.err.println("Sf-->Warning, Shapefile format (" + mainHeader.getVersion() + ") newer that supported (" + VERSION + "), attempting to read anyway");}
        
        Geometry body;
        java.util.ArrayList list = new java.util.ArrayList();
        int type = mainHeader.getShapeType();
        ShapeHandler handler = getShapeHandler(type);
        if (handler == null) {
            throw new ShapeTypeNotSupportedException("Unsuported shape type:" + type);
        }
        try {
            while (true){
                file.setLittleEndianMode(false);
                int recordNumber = file.readInt();
                int contentLength = file.readInt();
                body = handler.read(file, geometryFactory);
                list.add(body);
            }
        }
        catch (java.io.EOFException e){
            log.debug("End of Shapefile reached, EOF caught");
        }
         catch(org.geotools.shapefile.InvalidShapefileException e) {
             log.warn("Truncating feature loading. Some features may not have been read",e);
        }
        return geometryFactory.createGeometryCollection((Geometry[]) list.toArray(new Geometry[]{}));
    }
    
    /**
     * Saves a shapefile to an output stream.
     * @param geometries The collection of geometries to write to the shapefile.
     */
    public void write(com.vividsolutions.jts.geom.GeometryCollection geometries) throws IOException {
        cmp.LEDataStream.LEDataOutputStream file = getOutputStream();
        ShapefileHeader mainHeader = new ShapefileHeader(geometries);
        mainHeader.write(file);
        int pos = 50; // header length in WORDS
        //records;
        //body;
        //header;
        int numShapes = geometries.getNumGeometries();
        Geometry body;
        ShapeHandler handler = Shapefile.getShapeHandler(geometries.getGeometryN(0));
        for (int i = 0; i < numShapes; i++){
            body = geometries.getGeometryN(i);
            file.setLittleEndianMode(false);
            file.writeInt(i);
            file.writeInt(handler.getLength(body));
            pos += 4; // length of header in WORDS
            handler.write(body, file);
            pos += handler.getLength(body); // length of shape in WORDS
        }
        file.flush();
        file.close();
    }
    /*
    public synchronized void writeIndex(GeometryCollection geometries,LEDataOutputStream file) throws IOException {
        ShapefileHeader mainHeader = new ShapefileHeader(geometries);
     
        mainHeader.writeToIndex(file);
        int pos = 50;
        int len = 0;
        file.setLittleEndianMode(false);
     
        for(int i=0;i<records.size();i++){
            //if(DEBUG)System.out.println("Writing index Record "+i);
            ShapeRecord item = (ShapeRecord)records.elementAt(i);
            file.writeInt(item.mainindex); // this should be the offset into the shp file
            len = item.header.getContentLength();
            file.writeInt(len);
            //System.out.println(pos+" "+len+4);
            pos+=len+4;
     
        }
        file.flush();
        file.close();
    }
     */
    
    
    
    
    
    /**
     * Returns a string for the shape type of index.
     * @param index An int corresponding to the shape type to be described.
     * @return A string describing the shape type.
     */
    public static String getShapeTypeDescription(int index){
        switch (index){
            case (NULL): return ("Null");
            case (POINT): return ("Points");
            case (ARC): return ("Arcs");
            case (ARC_M): return ("ArcsM");
            case (POLYGON): return ("Polygons");
            case (MULTIPOINT): return ("Multipoints");
            default: return ("Undefined");
        }
    }
    
    public static ShapeHandler getShapeHandler(Geometry geom){
        return getShapeHandler(getShapeType(geom));
    }
    
    public static ShapeHandler getShapeHandler(int type){
        switch(type){
            case Shapefile.POINT: return new PointHandler();
            case Shapefile.POLYGON: return new PolygonHandler();
            case Shapefile.ARC: return new MultiLineHandler();
        }
        return null;
    }
    
    public static int getShapeType(Geometry geom){
        if (geom instanceof com.vividsolutions.jts.geom.Point) {
            return Shapefile.POINT;
        }
        if (geom instanceof com.vividsolutions.jts.geom.Polygon) {
            return Shapefile.POLYGON;
        }
        if (geom instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            return Shapefile.POLYGON;
        }
        if (geom instanceof com.vividsolutions.jts.geom.LineString) {
            return Shapefile.ARC;
        }
        if (geom instanceof com.vividsolutions.jts.geom.MultiLineString) {
            return Shapefile.ARC;
        }
        return Shapefile.UNDEFINED;
    }
    
    public synchronized void readIndex(java.io.InputStream is) throws IOException {
        LEDataInputStream file = null;
        try {
            java.io.BufferedInputStream in = new java.io.BufferedInputStream(is);
            file = new LEDataInputStream(in);
        }catch (Exception e){System.err.println(e);}
        ShapefileHeader head = new ShapefileHeader(file);
        
        int pos = 0, len = 0;
        file.setLittleEndianMode(false);
        file.close();
    }
    
    /**
     * Getter for property bounds.
     * @return Value of property bounds - null if not known.
     */
    public com.vividsolutions.jts.geom.Envelope getBounds() {
        if (!initialized){
            try {
                setup();
            } catch (IOException e){
                return null;
            }
        }
        return bounds;
    }
    
}












