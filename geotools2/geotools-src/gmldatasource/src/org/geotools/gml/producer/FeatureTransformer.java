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
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import java.util.IdentityHashMap;
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
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.helpers.NamespaceSupport;


/** FeatureTransformer provides a mechanism for converting Feature objects into
 * (hopefully) valid gml. This is a work in progress, so please be patient.
 * A simple example of how to use this class follows:<pre>
 *
 *   FeatureCollection collection; // can also use FeatureReader!!
 *   OutputStream out;
 *
 *   FeatureTransformer ft = new FeatureTransformer();
 *
 *   // set the indentation to 4 spaces
 *   ft.setIndentation(4);
 *
 *   // this will allow Features with the FeatureType which has the namespace
 *   // "http://somewhere.org" to be prefixed with xxx...
 *   ft.getFeatureNamespaces().declarePrefix("xxx","http://somewhere.org");
 *
 *   // transform
 *   ft.transform(collection,out);
 * </pre>
 * <b>The above example assumes a homogenous collection of Features whose
 * FeatureType has the namespace "http://somewhere.org"</b> but note that not
 * all DataSources currently provide FeatureTypes with a namespace...
 * There are two other mechanisms for prefixing your Features.<br>
 * 1) Map a specific FeatureType <b>by identity</b> to prefix and nsURI
 * <pre>
 *   FeatureType fc;
 *   FeatureTransformer ft = new FeatureTransformer();
 *   ft.getFeatureTypeNamespaces().declareNamespace(fc,"xxx","http://somewhere.org");
 * </pre>
 * 2) Provide a default namespace for any Features whose FeatureType either
 * has an empty namespace, OR, has not been mapped using the previous method.
 * This is basically a catch-all mechanism.
 * <pre>
 *   FeatureTransformer ft = new FeatureTransformer();
 *   ft.getFeatureTypeNamespaces().declareDefaultNamespace("xxx","http://somewhere.org");
 * </pre>
 * <br/>
 * The collectionNamespace and prefix property refers to the prefix and namespace
 * given to the document root and defualts to wfs,http://www.opengis.wfs.
 * 
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 * @version $Id: FeatureTransformer.java,v 1.11 2003/11/06 00:25:56 ianschneider Exp $
 * @todo Add support for schemaLocation
 */
public class FeatureTransformer extends TransformerBase {
    
    private String collectionPrefix = "wfs";
    private String collectionNamespace = "http://www.opengis/wfs";
    private NamespaceSupport nsLookup = new NamespaceSupport();
    private FeatureTypeNamespaces featureTypeNamespaces = new FeatureTypeNamespaces(nsLookup);
    
    public void setCollectionNamespace(String nsURI) {
        collectionNamespace = nsURI;
    }
    
    public String getCollectionNamespace() {
        return collectionNamespace;
    }
    
    public void setCollectionPrefix(String prefix) {
        this.collectionPrefix = prefix;
    }
    
    public String getCollectionPrefix() {
        return collectionPrefix;
    }
    
    public NamespaceSupport getFeatureNamespaces() {
        return nsLookup;
    }
    
    public FeatureTypeNamespaces getFeatureTypeNamespaces() {
        return featureTypeNamespaces;
    }
    
    public org.geotools.xml.transform.Translator createTranslator(ContentHandler handler) {
        FeatureTranslator t = new FeatureTranslator(handler,collectionPrefix,collectionNamespace,featureTypeNamespaces);
        java.util.Enumeration prefixes = nsLookup.getPrefixes();
        while (prefixes.hasMoreElements()) {
            String prefix = prefixes.nextElement().toString();
            String uri = nsLookup.getURI(prefix);
            t.getNamespaceSupport().declarePrefix(prefix,uri);
        }
        return t;
    }
    
    public static class FeatureTypeNamespaces {
        
        IdentityHashMap lookup = new IdentityHashMap();
        NamespaceSupport nsSupport;
        String defaultPrefix = null;
        
        public FeatureTypeNamespaces(NamespaceSupport nsSupport) {
            this.nsSupport = nsSupport;
        }
        
        public void declareDefaultNamespace(String prefix,String nsURI) {
            defaultPrefix = prefix;
            nsSupport.declarePrefix(prefix, nsURI);
        }
        
        public void declareNamespace(FeatureType type,String prefix,String nsURI) {
            lookup.put(type,prefix);
            nsSupport.declarePrefix(prefix,nsURI);
        }
        
        public String findPrefix(FeatureType type) {
            String pre = (String) lookup.get(type);
            if (pre == null)
                pre = defaultPrefix;
            return pre;
        }
        
    }
    
    
    /**
     * Outputs gml without any fancy indents or newlines.
     */
    public static class FeatureTranslator extends TranslatorSupport implements FeatureCollectionIteration.Handler {
        
        String fc = "FeatureCollection";
        GeometryTransformer.GeometryTranslator geometryTranslator;
        String memberString;
        String currentPrefix;
        FeatureTypeNamespaces types;
        /**
         * Constructor with handler.
         *
         * @param handler the handler to use.
         */
        public FeatureTranslator(ContentHandler handler,String prefix,String ns,FeatureTypeNamespaces types) {
            super(handler, prefix, ns);
            
            geometryTranslator = new GeometryTransformer.GeometryTranslator(handler);
            this.types = types;
            
            getNamespaceSupport().declarePrefix(geometryTranslator.getDefaultPrefix(), geometryTranslator.getDefaultNamespace());
            memberString = geometryTranslator.getDefaultPrefix() + ":featureMember";
           
        }
        
        public void encode(Object o) throws IllegalArgumentException {
            if (o instanceof FeatureCollection) {
                FeatureCollection fc = (FeatureCollection) o;
                FeatureCollectionIteration.iteration(this, fc);
            } else if (o instanceof FeatureReader) {
                // THIS IS A HACK FOR QUICK USE
                FeatureReader r = (FeatureReader) o;
                startFeatureCollection();
                try {
                    while (r.hasNext()) {
                        Feature f = r.next();
                        handleFeature(f);
                        FeatureType t = f.getFeatureType();
                        for (int i = 0, ii = f.getNumberOfAttributes(); i < ii; i++) {
                            handleAttribute(t.getAttributeType(i),f.getAttribute(i));
                        }
                        endFeature(f);
                    }
                } catch (Exception ioe ) {
                    throw new RuntimeException("Error reading Features",ioe);
                }
                endFeatureCollection();
            } else {
                throw new IllegalArgumentException("Cannot encode " + o);
            }
        }
        
        public void startFeatureCollection() {
            try {
                String element = getDefaultPrefix() == null ? fc : getDefaultPrefix() + ":" + fc;
                contentHandler.startElement("", "", element, NULL_ATTS);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        public void endFeatureCollection() {
            end(fc);
        }
        
        /**
         * Prints up the gml for a featurecollection.
         *
         * @param fc DOCUMENT ME!
         */
        public void handleFeatureCollection(FeatureCollection collection) {
            
            startFeatureCollection();
            writeBounds(collection.getBounds());
        }
        
        /**
         * writes the <code>gml:boundedBy</code> element to output based
         * on <code>fc.getBounds()</code>
         * @param fc
         * @throws SAXException if it is thorwn while writing the element or
         * coordinates
         */
        public void writeBounds(Envelope bounds) {
            try {
                String boundedBy = geometryTranslator.getDefaultPrefix() + ":" + "boundedBy";
                String box = geometryTranslator.getDefaultPrefix() + ":" + "Box";
                contentHandler.startElement("", "", boundedBy, NULL_ATTS);
                geometryTranslator.encode(bounds);
                contentHandler.endElement("","", boundedBy);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        /**
         * Sends sax for the ending of a feature collection.
         *
         * @param fc DOCUMENT ME!
         */
        public void endFeatureCollection(FeatureCollection collection) {
            endFeatureCollection();
        }
        
        /**
         * Sends sax for the ending of a feature.
         *
         * @param f DOCUMENT ME!
         */
        public void endFeature(Feature f) {
            try {
                String name = f.getFeatureType().getTypeName();
                if (currentPrefix != null)
                    name = currentPrefix + ":" + name;
                contentHandler.endElement("","", name);
                contentHandler.endElement("","",memberString);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
                
                String name = type.getName();
                if (currentPrefix != null)
                    name = currentPrefix + ":" + name;
                contentHandler.startElement("","", name, NULL_ATTS);
                
                if (Geometry.class.isAssignableFrom(value.getClass())) {
                    
                    geometryTranslator.encode( (Geometry) value );
                    
                } else {
                    if (value != null) {
                        String text = value.toString();
                        // this shouldn't be neccessary...Transformer does it for you :)
                        //text = GMLUtils.encodeXML(text);
                        contentHandler.characters(text.toCharArray(), 0, text.length());
                    }
                }
                
                contentHandler.endElement("","", name);
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Handles sax for a feature.
         *
         * @param f DOCUMENT ME!
         */
        public void handleFeature(Feature f) {
            try {
                contentHandler.startElement("","",memberString,NULL_ATTS);
                FeatureType type = f.getFeatureType();
                String name = type.getTypeName();
                currentPrefix = getNamespaceSupport().getPrefix(f.getFeatureType().getNamespace());
                if (currentPrefix == null)
                    currentPrefix = types.findPrefix(f.getFeatureType());
                if (currentPrefix == null)
                    throw new RuntimeException("Could not locate namespace for FeatureType : " + type.getTypeName());
                if (currentPrefix != null)
                    name = currentPrefix + ":" + name;
                AttributesImpl fidAtts = new org.xml.sax.helpers.AttributesImpl();
                String fid = f.getID();
                
                if (fid != null) {
                    fidAtts.addAttribute("", "fid", "fid", "fids", fid);
                }
                
                contentHandler.startElement("","", name, fidAtts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        
        
        
    }
    
}
