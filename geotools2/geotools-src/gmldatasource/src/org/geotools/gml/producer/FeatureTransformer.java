/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * FeatureReader.java
 *
 * Created on March 25, 2003, 4:01 PM
 */
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.*;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider<br>
 * @author Chris Holmes, TOPP
 *
 * @task TODO: Interior rings of polygons.
 * @task TODO: srs printed, multi namespaces, bbox.
 */
public class FeatureTransformer implements org.xml.sax.XMLReader {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gml.producer");
    private static final String XMLNS_GML = "xmlns:gml=\"http://www.opengis.net/gml\"";
    private static final String WFS_URI = "http://www.opengis.net/wfs";
    private static final String SRS_DECL = "srsName=\"http://www.opengis.net" +
        "/gml/srs/epsg.xml#";
    private static final String GML_URL = GMLUtils.GML_URL;
    ContentHandler contentHandler;
    String defaultNamespace;
    FeatureCollection collection;
    boolean prettyPrint = false;
    String srs = "-1";

    public void setDefaultNamespace(String namespace) {
        this.defaultNamespace = namespace;
    }

    public void setPrettyPrint(boolean pp) {
        this.prettyPrint = pp;
    }

    public synchronized void transform(FeatureCollection collection,
        java.io.OutputStream out) throws TransformerException {
        this.collection = collection;

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        // don't know what this should be, or if its even important
        InputSource inputSource = new InputSource("XXX");
        SAXSource source = new SAXSource(this, inputSource);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    private void walk(FeatureCollection collection) throws SAXException {
        contentHandler.startDocument();

        //FeatureCollectionIteration iteration = new FeatureCollectionIterat();

        FeatureCollectionIteration.Handler handler;

        if (prettyPrint) {
            handler = new PrettyOutputVisitor(contentHandler, ' ');
        } else {
            handler = new BasicOutputVisitor(contentHandler);
        }

        FeatureCollectionIteration.iteration(handler, collection);

        contentHandler.endDocument();
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotSupportedException(name);
    }

    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotSupportedException(name);
    }

    public void parse(String systemId) throws java.io.IOException, SAXException {
        walk(collection);
    }

    public void parse(InputSource input)
        throws java.io.IOException, SAXException {
        walk(collection);
    }

    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public static final void main(String[] args) throws Exception {
        //java.net.URL url = new java.io.File(args[0]).toURL();
        //org.geotools.shapefile.data.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
        //FeatureCollection fc = sds.getFeatures(null);
        //FeatureTransformer fr = new FeatureTransformer();
        //fr.setPrettyPrint(true);
        //fr.transform(fc, System.out);
    }

    class PrettyOutputVisitor extends BasicOutputVisitor {
        final char[] CR = new char[] { '\n' };
        final char[] TB;

        public PrettyOutputVisitor(ContentHandler h, char spacer) {
            super(h);
            TB = new char[5];
            java.util.Arrays.fill(TB, spacer);
        }

        protected final void CR() throws SAXException {
            output.ignorableWhitespace(CR, 0, 1);
        }

        protected final void TB(int i) throws SAXException {
            while (i > 0) {
                output.ignorableWhitespace(TB, 0, Math.min(TB.length, i));
                i -= TB.length;
            }
        }
    }

    class BasicOutputVisitor implements FeatureCollectionIteration.Handler {
        protected CoordinateWriter coordWriter = new CoordinateWriter();
        final ContentHandler output;
        final Attributes atts = new org.xml.sax.helpers.AttributesImpl();
        int indent = 0;

        public BasicOutputVisitor(ContentHandler h) {
            this.output = h;
        }

        protected void TB(int i) throws SAXException {
        }

        protected void CR() throws SAXException {
        }

        public void handleFeatureCollection(FeatureCollection fc) {
            try {
                TB(indent++);

                AttributesImpl fcAtts = new AttributesImpl();
                LOGGER.finer("first feat is " + fc.features().next());
                LOGGER.finer("schema is " +
                    fc.features().next().getFeatureType().getNamespace());

                String ns = fc.features().next().getFeatureType().getNamespace();

                if (ns == null) {
                    ns = defaultNamespace;
                }

                fcAtts.addAttribute("", "xmlns", "xmlns", "xmlns", ns);
                fcAtts.addAttribute(ns, "wfs", "xmlns:wfs", "wfs", WFS_URI);
                output.startElement("http://www.opengis.net/wfs",
                    "featureCollection", "wfs:featureCollection", fcAtts);
                CR();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void endFeatureCollection(FeatureCollection fc) {
            try {
                TB(--indent);
                output.endElement("http://www.opengis.net/wfs",
                    "featureCollection", "wfs:featureCollection");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void endFeature(Feature f) {
            try {
                TB(--indent);

                String name = f.getFeatureType().getTypeName();
                output.endElement("", name, name);
                CR();
                TB(--indent);
                output.endElement("", "featureMember", "gml:featureMember");
                CR();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void handleAttribute(AttributeType type, Object value) {
            try {
                TB(indent);

                String name = type.getName();
                output.startElement("", name, name, atts);
                if (Geometry.class.isAssignableFrom(value.getClass())) {
                    indent++;
		    CR();
		    TB(indent++);
                    writeGeometry((Geometry) value, "dude");
		      CR();
		    TB(--indent);
		} else {
                    if (value != null) {
                        String text = value.toString();
                        text = GMLUtils.encodeXML(text);
                        output.characters(text.toCharArray(), 0, text.length());
                    }
                }
		output.endElement("", name, name);
                CR();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void handleFeature(Feature f) {
            try {
                TB(indent++);
                output.startElement("", "featureMember", "gml:featureMember",
                    atts);
                CR();
                TB(indent++);
                String name = f.getFeatureType().getTypeName();
                AttributesImpl fidAtts = new org.xml.sax.helpers.AttributesImpl();
                String fid = f.getID();

                if (fid != null) {
                    fidAtts.addAttribute("", "fid", "fid", "fids", fid);
                }

                output.startElement("", name, name, fidAtts);
                CR();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Passes off geometry writing duties to correct method.
         *
         * @param geometry OGC SF type
         * @param gid Feature collection type
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void writeGeometry(Geometry geometry, String gid)
            throws SAXException {
            //user option to just use user defined bbox for whole dataset?
            //envelope.expandToInclude(geometry.getEnvelopeInternal());
	    //-no use fc.getBounds();

            String geomName = GMLUtils.getGeometryName(geometry);
            output.startElement(GMLUtils.GML_URL, geomName, "gml:" + geomName,
                atts);

            int geometryType = GMLUtils.getGeometryType(geometry);
	 
            switch (geometryType) {
            case GMLUtils.POINT:
                writeCoordinates(geometry);
                break;

            case GMLUtils.LINESTRING:
                writeCoordinates(geometry);
                break;

            case GMLUtils.POLYGON:
              
                writePolygon((Polygon) geometry, gid);
                break;

            case GMLUtils.MULTIPOINT:
            case GMLUtils.MULTILINESTRING:
            case GMLUtils.MULTIPOLYGON:
            case GMLUtils.MULTIGEOMETRY:
		String member = getMemberName(geometryType);
		writeMulti((GeometryCollection) geometry, gid, member);
		break;
            }

            output.endElement(GMLUtils.GML_URL, geomName, "gml:" + geomName);
        
        }

        /**
         * Writes a Polygon geometry.
         *
         * @param geometry OGC SF Polygon type
         * @param gid Geometric ID
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void writePolygon(Polygon geometry, String gid)
            throws SAXException {
            String outBound = "outerBoundaryIs";
            String lineRing = "LinearRing";
            String inBound = "innerBoundaryIs";
	    CR();
	    TB(indent++);
            output.startElement(GML_URL, outBound, "gml:" + outBound, atts);
            CR();
            TB(indent++);
            output.startElement(GML_URL, lineRing, "gml:" + lineRing, atts);
            CR();
            TB(indent);
            coordWriter.writeCoordinates(geometry.getExteriorRing(), output);
            CR();
            TB(--indent);
            output.endElement(GML_URL, lineRing, "gml:" + lineRing);
            CR();
            TB(--indent);
            output.endElement(GML_URL, outBound, "gml:" + outBound);
            CR();
            TB(--indent);

            /* TODO: if (geometry.getNumInteriorRing() > 0) {
	       
	       for( int i = 0 ; i < geometry.getNumInteriorRing() ; i++ ) {
	       finalResult.append("\n         <gml:innerBoundaryIs>");
	       finalResult.append("\n          <gml:LinearRing>");
	       writeCoordinates( geometry.getInteriorRingN(i) );
	       finalResult.append("\n          </gml:LinearRing>");
	       finalResult.append("\n        </gml:innerBoundaryIs>");
	       }
	       }*/
        }

        private void writeCoordinates(Geometry geometry)
            throws SAXException {
            CR();
            TB(indent);
            coordWriter.writeCoordinates(geometry, output);
            CR();
            TB(--indent);
        }

	//TODO: srs - should only appear on outermost element of geomCollection.
	private void writeMulti(GeometryCollection geometry,
					  String gid, String member) throws SAXException {
	    for(int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
		CR();
		TB(indent);
		output.startElement(GML_URL, member, "gml:" + member, atts);
		CR();
		indent++;
		TB(indent++);
				
		writeGeometry( geometry.getGeometryN(i), gid + "." + (i + 1));
		
		CR();
		TB(--indent);
		output.endElement(GML_URL, member, "gml:" + member);
	    }
	    CR();
	    TB(--indent);
	    
	} 
	
	private String getMemberName(int geometryType){
	    String member;
	    switch (geometryType) {
	    case GMLUtils.MULTIPOINT:
		return "pointMember";
		
		
	    case GMLUtils.MULTILINESTRING:
		return "lineStringMember";
		
		
	    case GMLUtils.MULTIPOLYGON:
		return "polygonMember";
		
	    default:

		return "geometryMember";
	    }
	}

	/*private void writeMultiLineString(GeometryCollection geometry,
					  String gid) throws SAXException {
	    //finalResult.append(abstractGeometryStart1 + gid +
	    //                abstractGeometryStart2);
	    String member = "lineStringMember";
	    //CR();
	    //TB(indent);
	    for(int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
		CR();
		TB(indent);
		output.startElement(GML_URL, member, "gml:" + member, atts);
		CR();
		indent++;
		TB(indent++);
		//if (verbose) finalResult.append(GEOM_OFFSET);
		//finalResult.append("<gml:lineStringMember>");
		writeGeometry( geometry.getGeometryN(i), gid + "." + (i + 1));
		CR();
		TB(--indent);
		//--indent;
		output.endElement(GML_URL, member, "gml:" + member);
		
		//if (verbose) finalResult.append(GEOM_OFFSET);
		//finalResult.append("</gml:lineStringMember>");
	    }
	    CR();
	    TB(--indent);
	    //finalResult.append( abstractGeometryEnd );
	    } */
    }
}
