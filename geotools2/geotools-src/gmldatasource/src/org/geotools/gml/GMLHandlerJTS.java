/*
 */
package org.geotools.gml;

/**
 * LEVEL3 saxGML4j GML handler: Gets JTS objects.
 *
 * <p>This handler must be implemented by the parent of a GMLFilterGeometry
 * filter, in order to handle the
 * JTS objects passed to it from the child.</p>
 *
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface GMLHandlerJTS extends org.xml.sax.ContentHandler {
    
    
    /**
     * Recieves OGC simple feature type geometry from parent.
     * @param geometry the simple feature geometry
     */
    public void geometry(com.vividsolutions.jts.geom.Geometry geometry);
    
    
}
