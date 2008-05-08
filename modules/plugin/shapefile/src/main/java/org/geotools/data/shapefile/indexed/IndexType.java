/**
 * 
 */
package org.geotools.data.shapefile.indexed;

import org.geotools.data.shapefile.ShpFileType;

/**
 * Enumerates the different types of Shapefile geometry indices there are.
 * 
 * @author jesse
 */
public enum IndexType {
    /**
     * Don't use indexing
     */
    NONE(null),
    /**
     * This is an RTree 
     */
    EXPERIMENTAL_UNSUPPORTED_GRX(ShpFileType.GRX),
    /**
     * The same index as mapserver. Its the most reliable and is the default
     */
    QIX(ShpFileType.QIX);

    public final ShpFileType shpFileType;

    private IndexType(ShpFileType shpFileType) {
        this.shpFileType = shpFileType;
    }
}