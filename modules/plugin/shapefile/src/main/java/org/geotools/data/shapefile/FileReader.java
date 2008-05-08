/**
 * 
 */
package org.geotools.data.shapefile;

/**
 * Indicates that the object reads one of the Shapefile related files controlled
 * by {@link ShpFiles}
 * 
 * @author jesse
 */
public interface FileReader {
    /**
     * An id for the reader. This is only used for debugging.
     * 
     * @return id for the reader.
     */
    String id();
}
