/*
 * RecordHeader.java
 *
 * Created on February 12, 2002, 3:34 PM
 */

package org.geotools.shapefile;


import cmp.LEDataStream.*;
import java.io.*;

/**
 *
 * @author  jamesm
 */
public class RecordHeader implements Serializable{
    private int recordNumber = -1;
    private int contentLength = -1;
    public RecordHeader(LEDataInputStream file)throws IOException {
        file.setLittleEndianMode(false);
        recordNumber=file.readInt();
        contentLength=file.readInt();
    }
    
    public RecordHeader(int count,ShapefileShape shape){
        recordNumber = count;
        contentLength = shape.getLength();
    }
    
    public void write(LEDataOutputStream file)throws IOException {
        file.setLittleEndianMode(false);
        file.writeInt(recordNumber);
        file.writeInt(contentLength);
    }
    
    public int getRecordNumber(){
        return recordNumber;
    }
    
    public int getContentLength(){
        return contentLength;
    }
}

