package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;

/**
 * Wrapper for a Shapefile arc, yet to be updated to JTS.
 */
public class ShapeArcM  {
    
    /**
     * Get the type of shape stored (Shapefile.ARC)
     */
    public int getShapeType(){
        return Shapefile.ARC_M;
    }
    /*
    public int getLength(){
        return (44+(4*numParts)+16+(16*numParts));
    }
    */
  
}