/*
 * GeometryTransformer.java
 *
 * Created on October 24, 2003, 1:08 PM
 */

package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Use a ContentHandler to issue SAXParse events for a geometry. The main use
 * of this class is in a Transformer, where the events are processesed by the
 * Transformer and written as XML to an output stream.
 *
 * @author  Ian Schneider
 */
public class GeometryTransformer {
    
    Attributes NULL_ATTS = new AttributesImpl();
    ContentHandler contentHandler;
    String namespace;
    String prefix;
    CoordinateWriter coordWriter = new CoordinateWriter();
    
    /** Creates a new instance of GeometryTransformer */
    public GeometryTransformer(ContentHandler handler,String namespace,String prefix) {
        this.contentHandler = handler;
        this.namespace = namespace;
        if (prefix == "")
            prefix = null;
        this.prefix = prefix;
    }
    
    public GeometryTransformer(ContentHandler handler) {
        this(handler,GMLUtils.GML_URL, "gml");
    }
    
    public void encode(Geometry geometry) {
        String geomName = GMLUtils.getGeometryName(geometry);
        start(geomName);
        
        int geometryType = GMLUtils.getGeometryType(geometry);
        
        switch (geometryType) {
            case GMLUtils.POINT:
            case GMLUtils.LINESTRING:
                try {
                    coordWriter.writeCoordinates(geometry,contentHandler);
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
            coordWriter.writeCoordinates(geometry.getExteriorRing(), contentHandler);
        } catch (SAXException s) {
            throw new RuntimeException(s);
        }
        end(lineRing);
        end(outBound);
        
        for (int i = 0, ii = geometry.getNumInteriorRing(); i < ii; i++) {
            start(inBound);
            start(lineRing);
            try {
                coordWriter.writeCoordinates(geometry.getInteriorRingN(i), contentHandler);
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
    
    void element(String element,String content) {
        element(element,content,NULL_ATTS);
    }
    
    void element(String element,String content,Attributes atts) {
        start(element,atts);
        if (content != null)
            chars(content);
        end(element);
    }
    
    void start(String element) {
        start(element,NULL_ATTS);
    }
    
    void start(String element,Attributes atts) {
        try {
            String el = prefix == null ? element : prefix + ":" + element;
            contentHandler.startElement("", "", el, atts);
        } catch (SAXException se) {
            throw new RuntimeException(se);
        }
    }
    
    void chars(String text) {
        try {
            char[] ch = text.toCharArray();
            contentHandler.characters(ch,0,ch.length);
        } catch (SAXException se) {
            throw new RuntimeException(se);
        }
    }
    
    void end(String element) {
        try {
            String el = prefix == null ? element : prefix + ":" + element;
            contentHandler.endElement("", "", el);
        } catch (SAXException se) {
            throw new RuntimeException(se);
        }
    }
    
}
