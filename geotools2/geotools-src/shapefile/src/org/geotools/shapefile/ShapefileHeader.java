/*
 * Header.java
 *
 * Created on February 12, 2002, 3:29 PM
 */

package org.geotools.shapefile;

import cmp.LEDataStream.*;
import java.io.*;

/**
 *
 * @author  jamesm
 */
public class ShapefileHeader{
    private final static boolean DEBUG=false;
    private int fileCode = -1;
    private int fileLength = -1;
    private int indexLength = -1;
    private int version = -1;
    private int shapeType = -1;
    private double[] bounds = new double[4];
    
    public ShapefileHeader(LEDataInputStream file) throws IOException {
        file.setLittleEndianMode(false);
        fileCode = file.readInt();
        if(DEBUG)System.out.println("Sfh->Filecode "+fileCode);
        if ( fileCode != Shapefile.SHAPEFILE_ID )
            System.err.println("Sfh->WARNING filecode "+fileCode+" not a match for documented shapefile code "+Shapefile.SHAPEFILE_ID);
        
        for(int i=0;i<5;i++){
            int tmp = file.readInt();
            if(DEBUG)System.out.println("Sfh->blank "+tmp);
        }
        fileLength = file.readInt();
        
        file.setLittleEndianMode(true);
        version=file.readInt();
        shapeType=file.readInt();
       
        //read in the bounding box
        for(int i = 0;i<4;i++){
            bounds[i]=file.readDouble();
        }
        
        //skip remaining unused bytes
        file.setLittleEndianMode(false);//well they may not be unused forever...
        file.skipBytes(32);
    }
    
    public ShapefileHeader(int shapeType,double[] bbox,ShapefileShape[] shapes){
        this.shapeType = shapeType;
        version = Shapefile.VERSION;
        fileCode = Shapefile.SHAPEFILE_ID;
        bounds = bbox;
        fileLength = 0;
        for(int i=0;i<shapes.length;i++){
            fileLength+=shapes[i].getLength();
            fileLength+=4;//for each header
        }
        fileLength+=50;//space used by this, the main header
        indexLength = 50+(4*shapes.length);
    }
    
    public void setFileLength(int fileLength){
        this.fileLength = fileLength;
    }
    
    public void setBounds(double[] bbox){
        bounds = bbox;
    }
    
    public void write(LEDataOutputStream file)throws IOException {
        int pos = 0;
        file.setLittleEndianMode(false);
        file.writeInt(fileCode);
        pos+=4;
        for(int i=0;i<5;i++){
            file.writeInt(0);//Skip unused part of header
            pos+=4;
        }
        file.writeInt(fileLength);
        pos+=4;
        file.setLittleEndianMode(true);
        file.writeInt(version);
        pos+=4;
        file.writeInt(shapeType);
        pos+=4;
        //read in the bounding box
        for(int i = 0;i<4;i++){
            pos+=8;
            file.writeDouble(bounds[i]);
        }
        
        //skip remaining unused bytes
        //file.setLittleEndianMode(false);//well they may not be unused forever...
        for(int i=0;i<4;i++){
            file.writeDouble(0.0);//Skip unused part of header
            pos+=8;
        }
        if(DEBUG)System.out.println("Sfh->Position "+pos);
    }
    
    public void writeToIndex(LEDataOutputStream file)throws IOException {
        int pos = 0;
        file.setLittleEndianMode(false);
        file.writeInt(fileCode);
        pos+=4;
        for(int i=0;i<5;i++){
            file.writeInt(0);//Skip unused part of header
            pos+=4;
        }
        file.writeInt(indexLength);
        pos+=4;
        file.setLittleEndianMode(true);
        file.writeInt(version);
        pos+=4;
        file.writeInt(shapeType);
        pos+=4;
        //write the bounding box
        for(int i = 0;i<4;i++){
            pos+=8;
            file.writeDouble(bounds[i]);
        }
        
        //skip remaining unused bytes
        //file.setLittleEndianMode(false);//well they may not be unused forever...
        for(int i=0;i<4;i++){
            file.writeDouble(0.0);//Skip unused part of header
            pos+=8;
        }
        if(DEBUG)System.out.println("Sfh->Index Position "+pos);
    }
    
    public int getShapeType(){
        return shapeType;
    }
    
    public int getVersion(){
        return version;
    }
    
    public double[] getBounds(){
        return bounds;
    }
    
    public String toString()  {
        String res = new String("Sf-->type "+fileCode+" size "+fileLength+" version "+ version + " Shape Type "+shapeType);
        return res;
    }
}