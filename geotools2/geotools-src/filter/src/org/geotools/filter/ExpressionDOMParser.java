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
 * ExpressionXmlParser.java
 *
 * Created on 03 July 2002, 10:21
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.TopologyException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// J2SE dependencies
import java.util.logging.Logger;


/**
 * parsez short sections of gml for use in expressions and filters Hopefully we
 * can get away without a full parser here.
 *
 * @author iant
 */
public final class ExpressionDOMParser {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Factory for creating filters. */
    private static final FilterFactory FILTER_FACT = FilterFactory
        .createFilterFactory();

    /** Factory for creating geometry objects */
    private static GeometryFactory gfac = new GeometryFactory();

    /** int representation of a box */
    private static final int GML_BOX = 1;

    /** int representation of a polygon */
    private static final int GML_POLYGON = 2;

    /** int representation of a linestring */
    private static final int GML_LINESTRING = 3;

    /** int representation of a point */
    private static final int GML_POINT = 4;

    /** number of coordinates in a box */
    private static final int NUM_BOX_COORDS = 5;

    /**
     * Creates a new instance of ExpressionXmlParser
     */
    private ExpressionDOMParser() {
    }

    /**
     * parses an expression for a filter.
     *
     * @param root the root node to parse, should be an filter expression.
     *
     * @return the geotools representation of the expression held in the node.
     */
    public static Expression parseExpression(Node root) {
        LOGGER.finer("parsingExpression " + root.getLocalName());

        //NodeList children = root.getChildNodes();
        //LOGGER.finest("children "+children);
        if ((root == null) || (root.getNodeType() != Node.ELEMENT_NODE)) {
            LOGGER.finer("bad node input ");

            return null;
        }

        LOGGER.finer("processing root " + root.getLocalName());

        Node child = root;
       
        System.out.println("ExpressionParser: NodeValue=" + child.getNodeValue() +" NodeName=" + child.getNodeName() + ": LocalName=" + child.getLocalName() + ": NameSpaceURI=" + child.getNamespaceURI());
        String childName = (child.getLocalName()!=null)?child.getLocalName():child.getNodeName(); 
        if (childName.equalsIgnoreCase("Literal")) {
            LOGGER.finer("processing literal " + child);

            NodeList kidList = child.getChildNodes();
            LOGGER.finest("literal elements (" + kidList.getLength() + ") "
                + kidList.toString());

            for (int i = 0; i < kidList.getLength(); i++) {
                Node kid = kidList.item(i);
                LOGGER.finest("kid " + i + " " + kid);

                if (kid == null) {
                    LOGGER.finest("Skipping ");

                    continue;
                }

                if (kid.getNodeValue() == null) {
                    /* it might be a gml string so we need to convert it into
                     * a geometry this is a bit tricky since our standard
                     * gml parser is SAX based and we're a DOM here.
                     */
                    LOGGER.finer("node " + kid.getNodeValue() + " namespace "
                        + kid.getNamespaceURI());
                    LOGGER.fine("a literal gml string?");

                    try {
                        Geometry geom = parseGML(kid);

                        if (geom != null) {
                            LOGGER.finer("built a " + geom.getGeometryType()
                                + " from gml");
                            LOGGER.finer("\tpoints: " + geom.getNumPoints());
                        } else {
                            LOGGER.finer(
                                "got a null geometry back from gml parser");
                        }

                        return FILTER_FACT.createLiteralExpression(geom);
                    } catch (IllegalFilterException ife) {
                        LOGGER.warning("Problem building GML/JTS object: "
                            + ife);
                    }

                    return null;
                }

                if (kid.getNodeValue().trim().length() == 0) {
                    LOGGER.finest("empty text element");

                    continue;
                }

                // debuging only

                /*switch(kid.getNodeType()){
                   case Node.ELEMENT_NODE:
                       LOGGER.finer("element :"+kid);
                       break;
                   case Node.TEXT_NODE:
                       LOGGER.finer("text :"+kid);
                       break;
                   case Node.ATTRIBUTE_NODE:
                       LOGGER.finer("Attribute :"+kid);
                       break;
                   case Node.CDATA_SECTION_NODE:
                       LOGGER.finer("Cdata :"+kid);
                       break;
                   case Node.COMMENT_NODE:
                       LOGGER.finer("comment :"+kid);
                       break;
                   } */
                String nodeValue = kid.getNodeValue();
                LOGGER.finer("processing " + nodeValue);

//                System.out.println("Node value is: " + nodeValue);
                // see if it's an int
                try {
                    try {
                        Integer intLit = new Integer(nodeValue);
                        LOGGER.finer("An integer");

                        return FILTER_FACT.createLiteralExpression(intLit);
                    } catch (NumberFormatException e) {
                        /* really empty */
                    }

                    // A double?
                    try {
                        Double doubleLit = new Double(nodeValue);
                        LOGGER.finer("A double");

                        return FILTER_FACT.createLiteralExpression(doubleLit);
                    } catch (NumberFormatException e) {
                        /* really empty */
                    }

                    // must be a string (or we have a problem)
                    LOGGER.finer("defaulting to string");

                    return FILTER_FACT.createLiteralExpression(nodeValue);
                } catch (IllegalFilterException ife) {
                    LOGGER.finer("Unable to build expression " + ife);

                    return null;
                }
            }
        }
        if (childName.equalsIgnoreCase("add")) {
            try {
                LOGGER.fine("processing an Add");

                //Node left = null;
                //Node right = null;
                MathExpression math = FILTER_FACT.createMathExpression(DefaultExpression.MATH_ADD);
                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add left value -> " + value + "<-");
                math.addLeftValue(parseExpression(value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add right value -> " + value + "<-");
                math.addRightValue(parseExpression(value));

                return math;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build expression " + ife);

                return null;
            }
        }

        if (childName.equalsIgnoreCase("sub")) {
            try {
                //NodeList kids = child.getChildNodes();
                MathExpression math;
                math = FILTER_FACT.createMathExpression(DefaultExpression.MATH_SUBTRACT);

                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add left value -> " + value + "<-");
                math.addLeftValue(parseExpression(value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add right value -> " + value + "<-");
                math.addRightValue(parseExpression(value));

                return math;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build expression " + ife);

                return null;
            }
        }

        if (childName.equalsIgnoreCase("mul")) {
            try {
                //NodeList kids = child.getChildNodes();
                MathExpression math;
                math = FILTER_FACT.createMathExpression(DefaultExpression.MATH_MULTIPLY);

                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add left value -> " + value + "<-");
                math.addLeftValue(parseExpression(value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add right value -> " + value + "<-");
                math.addRightValue(parseExpression(value));

                return math;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build expression " + ife);

                return null;
            }
        }

        if (childName.equalsIgnoreCase("div")) {
            try {
                MathExpression math;
                math = FILTER_FACT.createMathExpression(DefaultExpression.MATH_DIVIDE);

                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add left value -> " + value + "<-");
                math.addLeftValue(parseExpression(value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finer("add right value -> " + value + "<-");
                math.addRightValue(parseExpression(value));

                return math;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build expression " + ife);

                return null;
            }
        }

       

        if (childName.equalsIgnoreCase("PropertyName")) {
            try {
                //NodeList kids = child.getChildNodes();
                AttributeExpression attribute = FILTER_FACT
                    .createAttributeExpression(null);
                attribute.setAttributePath(child.getFirstChild().getNodeValue());

                return attribute;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build expression: " + ife);

                return null;
            }
        }

        if (childName.equalsIgnoreCase("Function")) {
            FunctionExpression func = null;
            Element param = (Element) child;

            NamedNodeMap map = param.getAttributes();

            for (int k = 0; k < map.getLength(); k++) {
                String res = map.item(k).getNodeValue();
                String name = map.item(k).getLocalName();
                if (name == null){
                    name = map.item(k).getNodeName();
                }
                LOGGER.fine("attribute " + name + " with value of " + res);

                if (name.equalsIgnoreCase("name")) {
                    func = FILTER_FACT.createFunctionExpression(res);
                }
            }

            if (func == null) {
                LOGGER.severe("failed to find function in " + child);

                return null;
            }

            int argCount = func.getArgCount();
            Expression[] args = new Expression[argCount];
            Node value = child.getFirstChild();

            for (int i = 0; i < argCount; i++) {
                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                args[i] = parseExpression(value);
                value = value.getNextSibling();
            }

            func.setArgs(args);

            return func;
        }

        if (child.getNodeType() == Node.TEXT_NODE) {
            LOGGER.finer("processing a text node " + root.getNodeValue());

            String nodeValue = root.getNodeValue();
            LOGGER.finer("Text name " + nodeValue);

            // see if it's an int
            try {
                try {
                    Integer intLiteral = new Integer(nodeValue);

                    return FILTER_FACT.createLiteralExpression(intLiteral);
                } catch (NumberFormatException e) {
                    /* really empty */
                }

                try {
                    Double doubleLit = new Double(nodeValue);

                    return FILTER_FACT.createLiteralExpression(doubleLit);
                } catch (NumberFormatException e) {
                    /* really empty */
                }

                return FILTER_FACT.createLiteralExpression(nodeValue);
            } catch (IllegalFilterException ife) {
                LOGGER.finer("Unable to build expression " + ife);
            }
        }

        return null;
    }

    /**
     * Parses the gml of this node to jts.
     *
     * @param root the parent node of the gml to parse.
     *
     * @return the java representation of the geometry contained in root.
     */
    public static Geometry parseGML(Node root) {
        LOGGER.finer("processing gml " + root);

        List coordList;
        int type = 0;
        Node child = root;
        String childName = (child.getLocalName()!=null)?child.getLocalName():child.getNodeName(); 
        if (childName.equalsIgnoreCase("gml:box")) {
            LOGGER.finer("box");
            type = GML_BOX;
            coordList = parseCoords(child);

            com.vividsolutions.jts.geom.Envelope env = new com.vividsolutions.jts.geom.Envelope();

            for (int i = 0; i < coordList.size(); i++) {
                env.expandToInclude((Coordinate) coordList.get(i));
            }

            Coordinate[] coords = new Coordinate[NUM_BOX_COORDS];
            coords[0] = new Coordinate(env.getMinX(), env.getMinY());
            coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
            coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
            coords[3] = new Coordinate(env.getMaxX(), env.getMinY());
            coords[4] = new Coordinate(env.getMinX(), env.getMinY());

            com.vividsolutions.jts.geom.LinearRing ring = null;

            try {
                ring = gfac.createLinearRing(coords);
            } catch (com.vividsolutions.jts.geom.TopologyException tope) {
                LOGGER.fine("Topology Exception in GMLBox" + tope);

                return null;
            }

            return gfac.createPolygon(ring, null);
        }

        if (childName.equalsIgnoreCase("gml:polygon")) {
            LOGGER.finer("polygon");
            type = GML_POLYGON;

            LinearRing outer = null;
            List inner = new ArrayList();
            NodeList kids = root.getChildNodes();

            for (int i = 0; i < kids.getLength(); i++) {
                Node kid = kids.item(i);
                LOGGER.finer("doing " + kid);

                String kidName = (kid.getLocalName()!=null)?kid.getLocalName():kid.getNodeName(); 
                if (kidName.equalsIgnoreCase("gml:outerBoundaryIs")) {
                    outer = (LinearRing) parseGML(kid);
                }

                if (kidName.equalsIgnoreCase("gml:innerBoundaryIs")) {
                    inner.add((LinearRing) parseGML(kid));
                }
            }

            if (inner.size() > 0) {
                return gfac.createPolygon(outer,
                    (LinearRing[]) inner.toArray(new LinearRing[0]));
            } else {
                return gfac.createPolygon(outer, null);
            }
        }

        if (childName.equalsIgnoreCase("gml:outerBoundaryIs")
                || childName.equalsIgnoreCase("gml:innerBoundaryIs")) {
            LOGGER.finer("Boundary layer");

            NodeList kids = ((Element) child).getElementsByTagName(
                    "gml:LinearRing");

            return parseGML(kids.item(0));
        }

        if (childName.equalsIgnoreCase("gml:linearRing")) {
            LOGGER.finer("LinearRing");
            coordList = parseCoords(child);

            com.vividsolutions.jts.geom.LinearRing ring = null;

            try {
                ring = gfac.createLinearRing((Coordinate[]) coordList.toArray(
                            new Coordinate[] {}));
            } catch (TopologyException te) {
                LOGGER.finer("Topology Exception build linear ring: " + te);

                return null;
            }

            return ring;
        }

        if (childName.equalsIgnoreCase("gml:linestring")) {
            LOGGER.finer("linestring");
            type = GML_LINESTRING;
            coordList = parseCoords(child);

            com.vividsolutions.jts.geom.LineString line = null;
            line = gfac.createLineString((Coordinate[]) coordList.toArray(
                        new Coordinate[] {}));

            return line;
        }

        if (childName.equalsIgnoreCase("gml:point")) {
            LOGGER.finer("point");
            type = GML_POINT;
            coordList = parseCoords(child);

            com.vividsolutions.jts.geom.Point point = null;
            point = gfac.createPoint((Coordinate) coordList.get(0));

            return point;
        }

        if (childName.toLowerCase().startsWith("gml:multiPolygon")) {
            LOGGER.finer("MultiPolygon");

            List multi = new ArrayList();

            // parse all children thru parseGML
            NodeList kids = child.getChildNodes();

            for (int i = 0; i < kids.getLength(); i++) {
                multi.add(parseGML(kids.item(i)));
            }

            return gfac.createMultiPolygon((Polygon[]) multi.toArray(
                    new Polygon[0]));
        }

        return null;
    }

    /**
     * Parses a dom node into a coordinate list.
     *
     * @param root the root node representation of gml:coordinates.
     *
     * @return the coordinates in a list.
     */
    public static java.util.List parseCoords(Node root) {
        LOGGER.finer("parsing coordinate(s) " + root);

        List clist = new ArrayList();
        NodeList kids = root.getChildNodes();

        for (int i = 0; i < kids.getLength(); i++) {
            Node child = kids.item(i);
            LOGGER.finer("doing " + child);

            //if (child.getLocalName().equalsIgnoreCase("gml:coordinate")) {
            //  String internal = child.getNodeValue();
            //}
            
            
            String childName = (child.getLocalName()!=null)?child.getLocalName():child.getNodeName(); 
            if (childName.equalsIgnoreCase("gml:coordinates")) {
                LOGGER.finer("coordinates "
                    + child.getFirstChild().getNodeValue());

                NodeList grandKids = child.getChildNodes();

                for (int k = 0; k < grandKids.getLength(); k++) {
                    Node grandKid = grandKids.item(k);

                    if (grandKid.getNodeValue() == null) {
                        continue;
                    }

                    if (grandKid.getNodeValue().trim().length() == 0) {
                        continue;
                    }

                    String outer = grandKid.getNodeValue().trim();
                    StringTokenizer ost = new StringTokenizer(outer, " ");

                    while (ost.hasMoreTokens()) {
                        String internal = ost.nextToken();
                        StringTokenizer ist = new StringTokenizer(internal, ",");
                        double xCoord = Double.parseDouble(ist.nextToken());
                        double yCoord = Double.parseDouble(ist.nextToken());
                        double zCoord = Double.NaN;

                        if (ist.hasMoreTokens()) {
                            zCoord = Double.parseDouble(ist.nextToken());
                        }

                        clist.add(new Coordinate(xCoord, yCoord, zCoord));
                    }
                }
            }
        }

        return clist;
    }
}
