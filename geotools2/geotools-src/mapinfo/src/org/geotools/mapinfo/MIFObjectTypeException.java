/*
 * MIFObjectTypeException.java
 *
 * Created on January 2, 2002, 6:16 PM
 */

package org.geotools.mapinfo;

/**
 * Not all the kinds of the object type in mif file could be read in the current version 
 * of the current version of the MIFMIDReader. One reasion is the limited time , another is 
 * those types have been dealt with are not so common in the real data file. 
 * if they are met unfortunately, the Exception will be thrown. 
 * @author jianhui jin
 * @version 1.0
 */
public class MIFObjectTypeException extends java.lang.Exception {
   private String objectType=null;
    /**
 * Creates new <code>MIFObjectTypeException</code> without detail message.
     */
    public MIFObjectTypeException() {
        
    }


    /**
 * Constructs an <code>MIFObjectTypeException</code> with the specified detail message.
     * @param msg the name of the unusual object type the detail message.
     */
    public MIFObjectTypeException(String msg) {
        super(msg);
        objectType=msg;
        msg="The read method of MIF object "+msg+" have not been developed";
        System.out.println(msg);
        
    }
    /** User can get the name of the unusual object type as a string. */
    public String getObjectType(){
      return objectType;
    }
    
}


