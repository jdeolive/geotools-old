package org.geotools.geometry.jts;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Interface implemented by the various geometry classes of LiteGO1 that allows
 * a user to retrieve the equivalent JTS geometry.  The coordinate reference
 * system of the geometry is attached as the "userData" property of the
 * returned JTS object.
 */
public interface JTSGeometry {
    /**
     * Retrieves the equivalent JTS geometry for this object.  Note that this
     * operation may be expensive if the geometry must be computed.
     */
    public Geometry getJTSGeometry();

    /**
     * This method is invoked to cause the JTS object to be recalculated the
     * next time it is requested.  This method will be called by the
     * underlying guts of the code when something has changed.
     */
    public void invalidateCachedJTSPeer();
}
