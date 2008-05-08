/**
 * 
 */
package org.geotools.data.shapefile;

/**
 * Indicates that the object writes to one of the Shapefile related files
 * controlled by {@link ShpFiles}
 * 
 * @author jesse
 */
public interface FileWriter extends FileReader {
    /**
     * An id for the writer. This is only used for debugging.
     * 
     * @return id for the writer.
     */
    String id();
}
