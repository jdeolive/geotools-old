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

 * Created on 27.11.2003

 */
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Logger;


/**
 * Transformer that transforms feature types into (hopefully) valid XML
 * schemas. This class may be used by geoserver in the future to automatically
 * create XML schemas for the  DescribeFeatureType request.
 * <pre>
 *   FeatureType type = ...; // you have it from somewhere
 *   FeatureTypeTransformer t = new FeatureTypeTransformer();
 *   t.transform(type, System.out);
 * </pre>
 * The following table lists the mapping between java types and xml schema data
 * types for attribute types.
 * 
 * <table>
 * <tr><th>
 * Java
 * </td><th>
 * XML Schema
 * </td></tr>
 * <tr><td>
 * String
 * </td><td>
 * xs:string
 * </td></tr>
 * <tr><td>
 * Byte
 * </td><td>
 * xs:byte
 * </td></tr>
 * <tr><td>
 * Short
 * </td><td>
 * xs:short
 * </td></tr>
 * <tr><td>
 * Integer
 * </td><td>
 * xs:int
 * </td></tr>
 * <tr><td>
 * Long
 * </td><td>
 * xs:long
 * </td></tr>
 * <tr><td>
 * BigInteger
 * </td><td>
 * xs:integer
 * </td></tr>
 * <tr><td>
 * BigDecimal
 * </td><td>
 * xs:decimal
 * </td></tr>
 * <tr><td>
 * java.util.Date
 * </td><td>
 * xs:dateTime
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.Point
 * </td><td>
 * gml:PointPropertyType
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.LineString
 * </td><td>
 * gml:LineStringPropertyType
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.Polygon
 * </td><td>
 * gml:PolygonPropertyType
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.MultiPoint
 * </td><td>
 * gml:MultiPointPropertyType
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.MultiLineString
 * </td><td>
 * gml:MutliLineStringPropertyType
 * </td></tr>
 * <tr><td>
 * com.vividsolutions.jts.geom.MultiPolygon
 * </td><td>
 * gml:MultiPolygonPropertyType
 * </td></tr>
 * <tr><td>
 * org.geotools.data.Feature
 * </td><td>
 * gml:AbstractFeatureType
 * </td></tr>
 * </table>
 *
 * @author Simon Raess
 * @version $Id: FeatureTypeTransformer.java,v 1.3 2004/01/09 16:14:36 cholmesny Exp $
 *
 * @task TODO: Support GeometryCollection.
 * @task REVISIT: Should support a bit more for the header, like being able to
 *       set the schemaLocation.  It also should declare and import the gml
 *       namespace - it's always used as all extend gml:AbstractFeatureType.
 *       Also should be able to set the global substitution group, so that
 *       this type can be used directly as a feature.
 */
public class FeatureTypeTransformer extends TransformerBase {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gml.producer.FeatureTypeTransformer");
    private static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    /**
     * Creates a Translator that is capable to translate FeatureType objects
     * into a XML schema fragment.
     *
     * @param handler the content handler to use
     *
     * @return DOCUMENT ME!
     */
    public Translator createTranslator(ContentHandler handler) {
        FeatureTypeTranslator translator = new FeatureTypeTranslator(handler);

        return translator;
    }

    /**
     * A FeatureTypeTranslator encodes FeatureTypes as a (hopefully) valid XML
     * schema.
     *
     * @see TransformerBase.TranslatorSupport
     */
    public static class FeatureTypeTranslator
        extends TransformerBase.TranslatorSupport {
        /**
         * Creates a new FeatureTypeTranslator. The default prefix is "xs" and
         * the default namespace is "http://www.w3.org/2001/XMLSchema".
         *
         * @param handler the content handler that receives the SAX events
         */
        public FeatureTypeTranslator(ContentHandler handler) {
            super(handler, "xs", "http://www.w3.org/2001/XMLSchema");
        }

        /**
         * Encode object o, which must be an instance of FeatureType. If it is
         * not an IllegalArgumentException will be thrown.
         *
         * @param o DOCUMENT ME!
         *
         * @throws IllegalArgumentException if supplied object is not an
         *         instance of FeatureType
         *
         * @see org.geotools.xml.transform.Translator#encode(java.lang.Object)
         */
        public void encode(Object o) throws IllegalArgumentException {
            if (o instanceof FeatureType) {
                encode((FeatureType) o);
            } else {
                throw new IllegalArgumentException(
                    "Translator does not know how to translate "
                    + o.getClass().getName());
            }
        }

        /**
         * Encode the supplied feature type.
         *
         * @param type the feature type to encode
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        protected void encode(FeatureType type) {
            AttributeType[] attributes = type.getAttributeTypes();

            try {
                startSchemaType(type.getTypeName(), type.getNamespace());

                for (int i = 0; i < attributes.length; i++) {
                    encode(attributes[i]);
                }

                endSchemaType();
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Start the schema fragment for a feature type.
         *
         * @param name
         * @param namespace DOCUMENT ME!
         *
         * @throws SAXException
         */
        protected void startSchemaType(String name, String namespace)
            throws SAXException {
            AttributesImpl atts = new AttributesImpl();

            atts.addAttribute("", "name", "name", "", name + "_Type");

            contentHandler.startElement(SCHEMA_NS, "complexType",
                "xs:complexType", atts);

            contentHandler.startElement(SCHEMA_NS, "complexContent",
                "xs:complexContent", new AttributesImpl());

            atts = new AttributesImpl();

            atts.addAttribute("", "base", "base", "", "gml:AbstractFeatureType");

            contentHandler.startElement(SCHEMA_NS, "extension", "xs:extension",
                atts);

            contentHandler.startElement(SCHEMA_NS, "sequence", "xs:sequence",
                new AttributesImpl());
        }

        /**
         * End the schema fragment for a feature type.
         *
         * @throws SAXException
         */
        protected void endSchemaType() throws SAXException {
            contentHandler.endElement(SCHEMA_NS, "sequence", "xs:sequence");

            contentHandler.endElement(SCHEMA_NS, "extension", "xs:extension");

            contentHandler.endElement(SCHEMA_NS, "complexContent",
                "xs:complexContent");

            contentHandler.endElement(SCHEMA_NS, "complexType", "xs:complexType");
        }

        /**
         * Encode an AttributeType.
         *
         * @param attribute
         *
         * @throws SAXException
         * @throws RuntimeException DOCUMENT ME!
         */
        protected void encode(AttributeType attribute)
            throws SAXException {
            Class type = attribute.getType();

            if (Number.class.isAssignableFrom(type)) {
                encodeNumber(attribute);
            } else if (Date.class.isAssignableFrom(type)) {
                encodeDate(attribute);
            } else if (type == String.class) {
                encodeString(attribute);
            } else if (Geometry.class.isAssignableFrom(type)) {
                encodeGeometry(attribute);

                /*} else if (FeatureType.class.isAssignableFrom(type)) {
                
                                      encodeFeature(attribute);*/
            } else {
                throw new RuntimeException("Cannot encode " + type.getName());
            }
        }

        /**
         * Encode an AttributeType whose value type is a String.
         *
         * @param attribute the attribute to encode
         *
         * @throws SAXException
         */
        protected void encodeString(AttributeType attribute)
            throws SAXException {
            int length = attribute.getFieldLength();

            AttributesImpl atts = createStandardAttributes(attribute);

            if (length == 0) {
                atts.addAttribute("", "type", "type", "", "xs:string");

                contentHandler.startElement(SCHEMA_NS, "element", "xs:element",
                    atts);

                contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
            } else {
                contentHandler.startElement(SCHEMA_NS, "element", "xs:element",
                    atts);

                contentHandler.startElement(SCHEMA_NS, "simpleType",
                    "xs:simpleType", new AttributesImpl());

                atts = new AttributesImpl();

                atts.addAttribute("", "base", "base", "", "xs:string");

                contentHandler.startElement(SCHEMA_NS, "restriction",
                    "xs:restriction", atts);

                atts = new AttributesImpl();

                atts.addAttribute("", "value", "value", "", "" + length);

                contentHandler.startElement(SCHEMA_NS, "maxLength",
                    "xs:maxLength", atts);

                contentHandler.endElement(SCHEMA_NS, "maxLength", "xs:maxLength");

                contentHandler.endElement(SCHEMA_NS, "restriction",
                    "xs:restriction");

                contentHandler.endElement(SCHEMA_NS, "simpleType",
                    "xs:simpleType");

                contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
            }
        }

        /**
         * Encode an AttributeType whose value type is a Number.
         *
         * @param attribute
         *
         * @throws SAXException
         * @throws RuntimeException DOCUMENT ME!
         */
        protected void encodeNumber(AttributeType attribute)
            throws SAXException {
            AttributesImpl atts = createStandardAttributes(attribute);

            Class type = attribute.getType();

            String typeString;

            if (type == Byte.class) {
                typeString = "xs:byte";
            } else if (type == Short.class) {
                typeString = "xs:short";
            } else if (type == Integer.class) {
                typeString = "xs:int";
            } else if (type == Long.class) {
                typeString = "xs:long";
            } else if (type == Float.class) {
                typeString = "xs:float";
            } else if (type == Double.class) {
                typeString = "xs:double";
            } else if (type == BigInteger.class) {
                typeString = "xs:integer";
            } else if (type == BigDecimal.class) {
                typeString = "xs:decimal";
            } else if (Number.class.isAssignableFrom(type)) {
                typeString = "xs:decimal";
            } else {
                throw new RuntimeException(
                    "Called encode number with invalid attribute type.");
            }

            atts.addAttribute("", "type", "type", "", typeString);

            contentHandler.startElement(SCHEMA_NS, "element", "xs:element", atts);

            contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
        }

        /**
         * Encode an AttributeType whose value type is a Date.
         *
         * @param attribute
         *
         * @throws SAXException
         */
        protected void encodeDate(AttributeType attribute)
            throws SAXException {
            AttributesImpl atts = createStandardAttributes(attribute);

            atts.addAttribute("", "type", "type", "", "xs:dateTime");

            contentHandler.startElement(SCHEMA_NS, "element", "xs:element", atts);

            contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
        }

        /**
         * Encode an AttributeType whose value type is a Geometry.
         *
         * @param attribute
         *
         * @throws SAXException
         * @throws RuntimeException DOCUMENT ME!
         */
        protected void encodeGeometry(AttributeType attribute)
            throws SAXException {
            AttributesImpl atts = createStandardAttributes(attribute);

            Class type = attribute.getType();

            String typeString = "";

            LOGGER.finer(type.getName());

            if (type == Point.class) {
                typeString = "gml:PointPropertyType";
            } else if (type == LineString.class) {
                typeString = "gml:LineStringPropertyType";
            } else if (type == Polygon.class) {
                typeString = "gml:PolygonPropertyType";
            } else if (type == MultiPoint.class) {
                typeString = "gml:MultiPointPropertyType";
            } else if (type == MultiLineString.class) {
                typeString = "gml:MultiLineStringPropertyType";
            } else if (type == MultiPolygon.class) {
                typeString = "gml:MultiPolygonPropertyType";
            } else if (type == GeometryCollection.class) {
                typeString = "gml:MultiGeometryPropertyType";
            } else if (type == Geometry.class) {
                typeString = "gml:GeometryAssociationType";
            } else {
                throw new RuntimeException("Unsupported type: "
                    + type.getName());
            }

            atts.addAttribute("", "type", "type", "", typeString);

            contentHandler.startElement(SCHEMA_NS, "element", "xs:element", atts);

            contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
        }

        /**
         * Encode an AttributeType whose value type is a Feature.
         *
         * @param attribute
         *
         * @return
         */

        /*protected void encodeFeature(AttributeType attribute) throws SAXException {
        
                      AttributesImpl atts = createStandardAttributes(attribute);
        
                      //atts.addAttribute("", "type", "type", "", "gml:AbstractFeatureType");
        
        
                      contentHandler.startElement(SCHEMA_NS, "element", "xs:element", atts);
        
                      encode("");
        
                      contentHandler.endElement(SCHEMA_NS, "element", "xs:element");
        
                      }*/

        /**
         * Creates standard xml attributes present on all xs:element elements.
         * These are name, maxOccurs, minOccurs and nillable.
         *
         * @param attribute the attribute type from which the information is
         *        retrieved
         *
         * @return an org.xml.sax.helpers.AttributesImpl object that contains
         *         the standard attributes
         */
        protected AttributesImpl createStandardAttributes(
            AttributeType attribute) {
            AttributesImpl atts = new AttributesImpl();

            atts.addAttribute("", "name", "name", "", attribute.getName());

            if (attribute.isNillable()) {
                atts.addAttribute("", "minOccurs", "minOccurs", "", "0");

                atts.addAttribute("", "nillable", "nillable", "", "true");
            } else {
                atts.addAttribute("", "minOccurs", "minOccurs", "", "1");

                atts.addAttribute("", "nillable", "nillable", "", "false");
            }

            return atts;
        }
    }
}
