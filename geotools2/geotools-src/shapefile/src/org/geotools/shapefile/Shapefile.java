package org.geotools.shapefile;


import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import com.vividsolutions.jts.geom.*;
import cmp.LEDataStream.*;

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
    //Types 2,4,6,7 and 9 were undefined at time or writeing
   
    /**
     * Creates and initialises a shapefile from a url
     * @param url The url of the shapefile
     */
    public Shapefile(URL url)
    throws java.io.IOException,ShapefileException {
        URLConnection uc = url.openConnection();
        int len = uc.getContentLength();
        if(len <=0){
            throw new IOException("Sf-->File feched from URL "+url+" was of zero length or could not be found");
        }
        byte data[];
        data = new byte[len];
        BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
        /*int j=0,k=0;
        while(k<len || j==-1){
            j = in.read(data,k,len-k);
            k+=j;
        }*/
        LEDataInputStream sfile = new LEDataInputStream(in);
        //ByteArrayInputStream bais = new ByteArrayInputStream(data);
        //LEDataInputStream sfile = new LEDataInputStream(bais);
        read(sfile, new GeometryFactory());
    }
    
    /**
     * Initialises a shapefile from disk.
     * Use Shapefile(String) if you don't want to use LEDataInputStream directly (recomened)
     * @param file A LEDataInputStream that conects to the shapefile to read
     */
    public static GeometryCollection read(LEDataInputStream file, GeometryFactory geometryFactory) throws IOException,ShapefileException {
        ShapefileHeader mainHeader = new ShapefileHeader(file);
        if(mainHeader.getVersion() < VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") older that supported ("+VERSION+"), attempting to read anyway");}
        if(mainHeader.getVersion() > VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") newer that supported ("+VERSION+"), attempting to read anyway");}    

        Geometry body;
        RecordHeader header;
        ArrayList list = new ArrayList();
        int type=mainHeader.getShapeType();
        
        try{
            for(;;){
                try{
                header = new RecordHeader(file);
                switch(type){
                    case(POINT):
                        body = ShapePoint.read(file,geometryFactory);
                        break;
                    case(ARC):
                        body = ShapeMultiLine.read(file,geometryFactory);
                        break;
                    case(POLYGON):
                        body = ShapePolygon.read(file,geometryFactory);
                        break;
                    default:
                        throw new ShapeTypeNotSupportedException("Sf-->Shape type "+getShapeTypeDescription(type)+" ["+type+"] not suported");
                }
                list.add(body);
                }
                catch(TopologyException te){
                    list.add(geometryFactory.createGeometryCollection(new Geometry[0]));//invalid shape so add an empty collection?
                }
            }
        }
        catch(EOFException e){

        }
        return geometryFactory.createGeometryCollection((Geometry[])list.toArray(new Geometry[]{}));
    }
    
    /**
     * Saves a shapefile to and output stream.
     * @param file A LEDataInputStream that conects to the shapefile to read
     */
    public  void writeShapefile(OutputStream os) throws IOException {
        LEDataOutputStream file = null;
        try{
            BufferedOutputStream out = new BufferedOutputStream(os);
            file = new LEDataOutputStream(out);
        }catch(Exception e){System.err.println(e);}
        //System.out.println("Writing header");
        mainHeader.write(file);
        int pos = 50; // header length in WORDS
        //records;
        //body;
        //header;
        for(int i=0;i<records.size();i++){
            //System.out.println("Writing Record");
            ShapeRecord item = (ShapeRecord)records.elementAt(i);
            item.mainindex=pos;
            item.header.write(file);
            pos+=4; // length of header in WORDS
            item.shape.write(file);
            pos+=item.header.getContentLength(); // length of shape in WORDS
            
        }
        file.flush();
        file.close();
    }
    
    public synchronized void writeIndex(OutputStream os) throws IOException {
        LEDataOutputStream file = null;
        try{
            BufferedOutputStream out = new BufferedOutputStream(os);
            file = new LEDataOutputStream(out);
        }catch(Exception e){System.err.println(e);}
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
    
    
    
    /**
     * Gets the number of records stored in this shapefile
     * @return Number of records
     */
    public int getRecordCount(){
        return records.size();
    }
    
    public Vector getRecords(){
        return records;
    }
    
    /**
     * Gets the bounding box for the whole shape file.
     * @return An array of four doubles in the form {x1,y1,x2,y2}
     */
    public double[] getBounds(){
        return mainHeader.getBounds();
    }
    
    /**
     * Gets the type of shape stored in this shapefile.
     * @return An int indicating the type
     * @see #getShapeTypeDescription()
     * @see #getShapeTypeDescription(int type)
     */
    public int getShapeType(){
        return mainHeader.getShapeType();
    }
    
    /**
     * Returns a string for the shape type of index.
     * @param index An int coresponding to the shape type to be described
     * @return A string descibing the shape type
     */
    public static String getShapeTypeDescription(int index){
        switch(index){
            case(NULL):return ("Null");
            case(POINT):return ("Points");
            case(ARC):return ("Arcs");
            case(ARC_M):return ("ArcsM");
            case(POLYGON):return ("Polygons");
            case(MULTIPOINT):return ("Multipoints");
            default:return ("Undefined");
        }
    }
     
    public synchronized void readIndex(InputStream is) throws IOException {
        LEDataInputStream file = null;
        try{
            BufferedInputStream in = new BufferedInputStream(is);
            file = new LEDataInputStream(in);
        }catch(Exception e){System.err.println(e);}
        ShapefileHeader head = new ShapefileHeader(file);
        
        int pos=0,len=0;
        file.setLittleEndianMode(false);
        file.close();
    }
}












