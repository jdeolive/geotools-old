/*
 * BrushPatterns.java
 *
 * Created on 31 July 2002, 15:18
 */

package org.geotools.data.mapinfo;

import java.util.*;
import org.geotools.styling.*;
/**
 *
 * @author  iant
 */
final class MifStyles {
    
    private static final HashMap brushPatterns = new HashMap();
    private static final HashMap penPatterns = new HashMap();
    
    /** Creates a new instance of BrushPatterns */
    public MifStyles() {
    }
    
    static ExternalGraphic getBrushPattern(Integer pattern) {
        return (ExternalGraphic) brushPatterns.get(pattern);
    }
    static float[] getPenPattern(Integer pattern) {
        return (float[]) penPatterns.get(pattern);
    }
    static{
        brushPatterns.put(new Integer(1),null); // empty
        brushPatterns.put(new Integer(2),null); // solid
    
        penPatterns.put(new Integer(1),new float[]{0}); // blank line
        penPatterns.put(new Integer(2),null); // solid line
        penPatterns.put(new Integer(3),new float[]{1,1}); // small dots
        penPatterns.put(new Integer(4),new float[]{2,2}); // small dots
        penPatterns.put(new Integer(5),new float[]{2,1}); 
        penPatterns.put(new Integer(6),new float[]{3,1});
        penPatterns.put(new Integer(7),new float[]{5,1}); 
        penPatterns.put(new Integer(8),new float[]{10,1}); 
        penPatterns.put(new Integer(9),new float[]{3,1}); 
        penPatterns.put(new Integer(10),new float[]{1,3});
        penPatterns.put(new Integer(11),new float[]{2,4});
        penPatterns.put(new Integer(12),new float[]{3,3});
        penPatterns.put(new Integer(13),new float[]{5,5});
        penPatterns.put(new Integer(14),new float[]{2,1,1,1});
    }
}
