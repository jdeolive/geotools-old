/*
 * ShapefileUtilities.java
 *
 * Created on November 5, 2003, 11:54 AM
 */

package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Date;
import org.geotools.data.shapefile.dbf.DbaseFileException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;

/**
 *
 * @author  Ian Schneider
 */
public class ShapefileUtilities {
    
    private ShapefileUtilities() {}
    
    /**
     * Marshal a given Object into the given Class.
     */
    public static Object forAttribute(final Object o,Class colType) {
        Object object;
        if(colType == Integer.class) {
            object = o;
        } else if ((colType == Short.class) || (colType == Byte.class)) {
            object = new Integer(((Number) o).intValue());
        } else if (colType == Double.class) {
            object = o;
        } else if (colType == Float.class) {
            object = new Double(((Number) o).doubleValue());
        } else if (Number.class.isAssignableFrom(colType)) {
            object = o;
        } else if(colType == String.class) {
            if (o == null) {
                object = o;
            } else {
                object = o.toString();
            }
        } else if (colType == Boolean.class) {
            object = o;
        } else if (java.util.Date.class.isAssignableFrom(colType)) {
            object = o;
        } else {
            if (colType != null) {
                throw new RuntimeException("Cannot convert " + colType.getName());
            } else {
                throw new RuntimeException("Null Class for conversion");
            }
        }
        
        return object;
    }
    
    
    
}
