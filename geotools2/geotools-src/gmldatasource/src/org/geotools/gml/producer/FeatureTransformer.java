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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionIteration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Producers gml to an output stream.
 *
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 *
 * @task TODO: Interior rings of polygons.
 * @task TODO: srs printed, multi namespaces, bbox.
 */
public class FeatureTransformer extends XMLFilterImpl implements XMLReader {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gml.producer");

    /** The uri for gml. */
    private static final String GML_URL = GMLUtils.GML_URL;

    /** The namespace declaration for gml */
    //private static final String XMLNS_GML = "xmlns:gml=\"" + GML_URL + "\"";

    /** The namespace for WFS */
    private static final String WFS_URI = "http://www.opengis.net/wfs";

    /** The attribute to specify the spatial reference system. */
    //private static final String SRS_DECL = "srsName=\"http://www.opengis.net"
    //  + "/gml/srs/epsg.xml#";

    /** handler to do the processing */
    private ContentHandler contentHandler;

    /** The namespace to use if none is provided. */
    private String defaultNamespace;

    /** The feature collection to process. */
    private FeatureCollection collection;

    /** Whether newlines and indents should be printed. */
    private boolean prettyPrint = false;

    /** The spatial reference system id */
    //private String srs = "-1";

    /**
     * Sets a default namespace to use.
     *
     * @param namespace the namespace to use, should be a uri.
     */
    public void setDefaultNamespace(String namespace) {
        this.defaultNamespace = namespace;
    }

    /**
     * Sets if newlines and indents should be used for printing.
     *
     * @param pp true if pretty printing is desired.
     */
    public void setPrettyPrint(boolean pp) {
        this.prettyPrint = pp;
    }

    /**
     * performs the sending of sax events from the passed in  feature
     * collection.
     *
     * @param collection the collection to turn to gml.
     * @param out the stream to send the output to.
     *
     * @throws TransformerException DOCUMENT ME!
     */
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

    /**
     * Performs the iteration, walking over the collection and  firing events.
     *
     * @param collection the features to walk over.
     *
     * @throws SAXException DOCUMENT ME!
     */
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

    /**
     * walks the given collection.
     *
     * @param systemId DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void parse(String systemId) throws java.io.IOException, SAXException {
        walk(collection);
    }

    /**
     * walks the given collection.
     *
     * @param input DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void parse(InputSource input)
        throws java.io.IOException, SAXException {
        walk(collection);
    }

    /**
     * sets the content handler.
     *
     * @param handler DOCUMENT ME!
     */
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    /**
     * Currently does nothing.
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final void main(String[] args) throws Exception {
        //java.net.URL url = new java.io.File(args[0]).toURL();
        //org.geotools.shapefile.data.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
        //FeatureCollection fc = sds.getFeatures(null);
        //FeatureTransformer fr = new FeatureTransformer();
        //fr.setPrettyPrint(true);
        //fr.transform(fc, System.out);
    }

    /**
     * This handler keeps track of indents and adds newlines.
     */
    class PrettyOutputVisitor extends BasicOutputVisitor {
        /** The carraige return char array. */
        private final char[] cReturn = new char[] { '\n' };

        /** The tab char array */
        private final char[] tab;

        /**
         * Constructor with handler and initial spacer character.
         *
         * @param handler the handler to use.
         * @param spacer to use as space.
         */
        public PrettyOutputVisitor(ContentHandler handler, char spacer) {
            super(handler);
            tab = new char[5];
            java.util.Arrays.fill(tab, spacer);
        }

        /**
         * prints a carraige return with newline.
         *
         * @throws SAXException DOCUMENT ME!
         */
        protected final void cReturn() throws SAXException {
            output.ignorableWhitespace(cReturn, 0, 1);
        }

        /**
         * printe a tab with the currently tracked spacing.
         *
         * @param i DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        protected final void tab(int i) throws SAXException {
            while (i > 0) {
                output.ignorableWhitespace(tab, 0, Math.min(tab.length, i));
                i -= tab.length;
            }
        }
    }

    /**
     * Outputs gml without any fancy indents or newlines.
     */
    class BasicOutputVisitor implements FeatureCollectionIteration.Handler {
        /** Used to write coordinates to out. */
        private CoordinateWriter coordWriter = new CoordinateWriter();

        /** The handler. */
        protected final ContentHandler output;

        /** blank attributes to be used when none are needed. */
        private final Attributes atts = new AttributesImpl();

        /** initial indent */
        private int indent = 0;

        /**
         * Constructor with handler.
         *
         * @param handler the handler to use.
         */
        public BasicOutputVisitor(ContentHandler handler) {
            this.output = handler;
        }

        /**
         * Handles a tab call - does nothing.
         *
         * @param i DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        protected void tab(int i) throws SAXException {
        }

        /**
         * Handles a carraige return call - does nothing.
         *
         * @throws SAXException DOCUMENT ME!
         */
        protected void cReturn() throws SAXException {
        }

        /**
         * Prints up the gml for a featurecollection.
         *
         * @param fc DOCUMENT ME!
         */
        public void handleFeatureCollection(FeatureCollection fc) {
            try {
                tab(indent++);

                AttributesImpl fcAtts = new AttributesImpl();
                LOGGER.finer("first feat is " + fc.features().next());
                LOGGER.finer("schema is "
                    + fc.features().next().getFeatureType().getNamespace());

                String ns = fc.features().next().getFeatureType().getNamespace();

                if (ns == null) {
                    ns = defaultNamespace;
                }

                fcAtts.addAttribute("", "xmlns", "xmlns", "xmlns", ns);
                fcAtts.addAttribute(ns, "wfs", "xmlns:wfs", "wfs", WFS_URI);
                output.startElement("http://www.opengis.net/wfs",
                    "featureCollection", "wfs:featureCollection", fcAtts);
                cReturn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Sends sax for the ending of a feature collection.
         *
         * @param fc DOCUMENT ME!
         */
        public void endFeatureCollection(FeatureCollection fc) {
            try {
                tab(--indent);
                output.endElement("http://www.opengis.net/wfs",
                    "featureCollection", "wfs:featureCollection");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Sends sax for the ending of a feature.
         *
         * @param f DOCUMENT ME!
         */
        public void endFeature(Feature f) {
            try {
                tab(--indent);

                String name = f.getFeatureType().getTypeName();
                output.endElement("", name, name);
                cReturn();
                tab(--indent);
                output.endElement("", "featureMember", "gml:featureMember");
                cReturn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * handles sax for an attribute.
         *
         * @param type DOCUMENT ME!
         * @param value DOCUMENT ME!
         */
        public void handleAttribute(AttributeType type, Object value) {
            try {
                tab(indent);

                String name = type.getName();
                output.startElement("", name, name, atts);

                if (Geometry.class.isAssignableFrom(value.getClass())) {
                    indent++;
                    cReturn();
                    tab(indent++);
                    writeGeometry((Geometry) value, "dude");
                    cReturn();
                    tab(--indent);
                } else {
                    if (value != null) {
                        String text = value.toString();
                        text = GMLUtils.encodeXML(text);
                        output.characters(text.toCharArray(), 0, text.length());
                    }
                }

                output.endElement("", name, name);
                cReturn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Handles sax for a feature.
         *
         * @param f DOCUMENT ME!
         */
        public void handleFeature(Feature f) {
            try {
                tab(indent++);
                output.startElement("", "featureMember", "gml:featureMember",
                    atts);
                cReturn();
                tab(indent++);

                String name = f.getFeatureType().getTypeName();
                AttributesImpl fidAtts = new org.xml.sax.helpers.AttributesImpl();
                String fid = f.getID();

                if (fid != null) {
                    fidAtts.addAttribute("", "fid", "fid", "fids", fid);
                }

                output.startElement("", name, name, fidAtts);
                cReturn();
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
            //String inBound = "innerBoundaryIs";
            cReturn();
            tab(indent++);
            output.startElement(GML_URL, outBound, "gml:" + outBound, atts);
            cReturn();
            tab(indent++);
            output.startElement(GML_URL, lineRing, "gml:" + lineRing, atts);
            cReturn();
            tab(indent);
            coordWriter.writeCoordinates(geometry.getExteriorRing(), output);
            cReturn();
            tab(--indent);
            output.endElement(GML_URL, lineRing, "gml:" + lineRing);
            cReturn();
            tab(--indent);
            output.endElement(GML_URL, outBound, "gml:" + outBound);
            cReturn();
            tab(--indent);

            // TODO: if (geometry.getNumInteriorRing() > 0) {
            //for( int i = 0 ; i < geometry.getNumInteriorRing() ; i++ ) {
            //finalResult.append("\n         <gml:innerBoundaryIs>");
            //finalResult.append("\n          <gml:LinearRing>");
            //writeCoordinates( geometry.getInteriorRingN(i) );
            //finalResult.append("\n          </gml:LinearRing>");
            //finalResult.append("\n        </gml:innerBoundaryIs>");
            //}
            //}
        }

        /**
         * Writes the coordinates for a geometry.
         *
         * @param geometry DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void writeCoordinates(Geometry geometry)
            throws SAXException {
            cReturn();
            tab(indent);
            coordWriter.writeCoordinates(geometry, output);
            cReturn();
            tab(--indent);
        }

        /**
         * Writes a multi - point, linestring, poly or geometry collection.
         *
         * @param geometry DOCUMENT ME!
         * @param gid DOCUMENT ME!
         * @param member DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         *
         * @task TODO: srs should only appear on outermost element of
         *       geomCollection.
         */
        private void writeMulti(GeometryCollection geometry, String gid,
            String member) throws SAXException {
            for (int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                cReturn();
                tab(indent);
                output.startElement(GML_URL, member, "gml:" + member, atts);
                cReturn();
                indent++;
                tab(indent++);

                writeGeometry(geometry.getGeometryN(i), gid + "." + (i + 1));

                cReturn();
                tab(--indent);
                output.endElement(GML_URL, member, "gml:" + member);
            }

            cReturn();
            tab(--indent);
        }

        //move to gml utils?
        private String getMemberName(int geometryType) {
            //String member;

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
    }
}
