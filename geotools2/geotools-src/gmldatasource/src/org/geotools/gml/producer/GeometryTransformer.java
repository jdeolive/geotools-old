/*
 * GeometryTransformer.java
 *
 * Created on October 24, 2003, 1:08 PM
 */

package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.*;
import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 *
 * @author  Ian Schneider
 */
public class GeometryTransformer extends TransformerBase {
    
    public org.geotools.xml.transform.Translator createTranslator(ContentHandler handler) {
        return new GeometryTranslator(handler);
    }
    
    public static class GeometryTranslator extends TranslatorSupport {
        
        CoordinateWriter coordWriter = new CoordinateWriter();
        
        public GeometryTranslator(ContentHandler handler) {
            super(handler, "gml", GMLUtils.GML_URL);
        }
        
        public void encode(Object o) throws IllegalArgumentException {
            if (o instanceof Geometry) {
                encode( (Geometry) o);
            } else {
                throw new IllegalArgumentException("Unable to encode " + o);
            }
        }
        
        public void encode(Envelope bounds) {
            start("Box");
            try {
                Coordinate [] coords = new Coordinate[4];
                coords[0] = new Coordinate(bounds.getMinX(), bounds.getMinY());
                coords[1] = new Coordinate(bounds.getMinX(), bounds.getMaxY());
                coords[2] = new Coordinate(bounds.getMaxX(), bounds.getMaxY());
                coords[3] = new Coordinate(bounds.getMaxX(), bounds.getMinY());
                coordWriter.writeCoordinates(coords,contentHandler);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
            end("Box");
        }
        
        public void encode(Geometry geometry) {
            String geomName = GMLUtils.getGeometryName(geometry);
            start(geomName);
            
            int geometryType = GMLUtils.getGeometryType(geometry);
            
            switch (geometryType) {
                case GMLUtils.POINT:
                case GMLUtils.LINESTRING:
                    try {
                        coordWriter.writeCoordinates(geometry.getCoordinates(),contentHandler);
                    } catch (SAXException s) {
                        throw new RuntimeException(s);
                    }
                    
                    break;
                    
                case GMLUtils.POLYGON:
                    writePolygon((Polygon) geometry);
                    
                    break;
                    
                case GMLUtils.MULTIPOINT:
                case GMLUtils.MULTILINESTRING:
                case GMLUtils.MULTIPOLYGON:
                case GMLUtils.MULTIGEOMETRY:
                    
                    writeMulti((GeometryCollection) geometry,GMLUtils.getMemberName(geometryType));
                    
                    break;
            }
            
            end(geomName);
        }
        
        private void writePolygon(Polygon geometry) {
            String outBound = "outerBoundaryIs";
            String lineRing = "LinearRing";
            String inBound = "innerBoundaryIs";
            start(outBound);
            start(lineRing);
            try {
                coordWriter.writeCoordinates(geometry.getExteriorRing().getCoordinates(), contentHandler);
            } catch (SAXException s) {
                throw new RuntimeException(s);
            }
            end(lineRing);
            end(outBound);
            
            for (int i = 0, ii = geometry.getNumInteriorRing(); i < ii; i++) {
                start(inBound);
                start(lineRing);
                try {
                    coordWriter.writeCoordinates(geometry.getInteriorRingN(i).getCoordinates(), contentHandler);
                } catch (SAXException s) {
                    throw new RuntimeException(s);
                }
                end(lineRing);
                end(inBound);
            }
        }
        
        private void writeMulti(GeometryCollection geometry,String member) {
            for (int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                
                start(member);
                
                encode(geometry.getGeometryN(i));
                
                end(member);
            }
        }
        
        
    }
    
}
