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
 * FilterXMLParser.java
 *
 * Created on 10 July 2002, 17:14
 */
package org.geotools.filter;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// J2SE dependencies
import java.util.logging.Logger;


/**
 * A dom based parser to build filters as per OGC 01-067
 *
 * @author Ian Turton, CCG
 * @version $Id: FilterDOMParser.java,v 1.11 2004/05/07 20:20:46 jmacgill Exp $
 *
 * @task TODO: split this class up into multiple methods.
 */
public final class FilterDOMParser {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Factory to create filters. */
    private static final FilterFactory FILTER_FACT = FilterFactory
        .createFilterFactory();

    /** Number of children in a between filter. */
    private static final int NUM_BETWEEN_CHILDREN = 3;

    /** Map of comparison names to their filter types. */
    private static java.util.Map comparisions = new java.util.HashMap();

    /** Map of spatial filter names to their filter types. */
    private static java.util.Map spatial = new java.util.HashMap();

    /** Map of logical filter names to their filter types. */
    private static java.util.Map logical = new java.util.HashMap();

    static {
        comparisions.put("PropertyIsEqualTo",
            new Integer(AbstractFilter.COMPARE_EQUALS));
        comparisions.put("PropertyIsNotEqualTo",
            new Integer(AbstractFilter.COMPARE_NOT_EQUALS));
        comparisions.put("PropertyIsGreaterThan",
            new Integer(AbstractFilter.COMPARE_GREATER_THAN));
        comparisions.put("PropertyIsGreaterThanOrEqualTo",
            new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL));
        comparisions.put("PropertyIsLessThan",
            new Integer(AbstractFilter.COMPARE_LESS_THAN));
        comparisions.put("PropertyIsLessThanOrEqualTo",
            new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL));
        comparisions.put("PropertyIsLike", new Integer(AbstractFilter.LIKE));
        comparisions.put("PropertyIsNull", new Integer(AbstractFilter.NULL));
        comparisions.put("PropertyIsBetween",
            new Integer(AbstractFilter.BETWEEN));
        comparisions.put("FeatureId", new Integer(AbstractFilter.FID));

        spatial.put("Equals", new Integer(AbstractFilter.GEOMETRY_EQUALS));
        spatial.put("Disjoint", new Integer(AbstractFilter.GEOMETRY_DISJOINT));
        spatial.put("Intersects",
            new Integer(AbstractFilter.GEOMETRY_INTERSECTS));
        spatial.put("Touches", new Integer(AbstractFilter.GEOMETRY_TOUCHES));
        spatial.put("Crosses", new Integer(AbstractFilter.GEOMETRY_CROSSES));
        spatial.put("Within", new Integer(AbstractFilter.GEOMETRY_WITHIN));
        spatial.put("Contains", new Integer(AbstractFilter.GEOMETRY_CONTAINS));
        spatial.put("Overlaps", new Integer(AbstractFilter.GEOMETRY_OVERLAPS));
        spatial.put("Beyond", new Integer(AbstractFilter.GEOMETRY_BEYOND));
        spatial.put("BBOX", new Integer(AbstractFilter.GEOMETRY_BBOX));

        logical.put("And", new Integer(AbstractFilter.LOGIC_AND));
        logical.put("Or", new Integer(AbstractFilter.LOGIC_OR));
        logical.put("Not", new Integer(AbstractFilter.LOGIC_NOT));
    }

    /**
     * Creates a new instance of FilterXMLParser
     */
    private FilterDOMParser() {
    }

    /**
     * Parses the filter using DOM.
     *
     * @param root a dom node containing FILTER as the root element.
     *
     * @return DOCUMENT ME!
     *
     * @task TODO: split up this insanely long method.
     */
    public static Filter parseFilter(Node root) {
        LOGGER.finer("parsingFilter " + root.getLocalName());

        //NodeList children = root.getChildNodes();
        //LOGGER.finest("children "+children);
        if ((root == null) || (root.getNodeType() != Node.ELEMENT_NODE)) {
            LOGGER.finest("bad node input ");

            return null;
        }

        LOGGER.finest("processing root " + root.getLocalName() + " " + root.getNodeName());

        Node child = root;
        String childName = child.getLocalName();
        if(childName==null){
            childName= child.getNodeName();//HACK ?
        }
        LOGGER.finest("looking up " + childName);

        if (comparisions.containsKey(childName)) {
            LOGGER.finer("a comparision filter " + childName);

            //boolean like = false;
            //boolean between = false;
            try {
                short type = ((Integer) comparisions.get(childName)).shortValue();
                CompareFilter filter = null;
                LOGGER.finer("type is " + type);

                if (type == AbstractFilter.FID) {
                    FidFilter fidFilter = FILTER_FACT.createFidFilter();
                    Element fidElement = (Element) child;
                    fidFilter.addFid(fidElement.getAttribute("fid"));

                    Node sibling = fidElement.getNextSibling();

                    while (sibling != null) {
                        LOGGER.finer("Parsing another FidFilter");

                        if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                            fidElement = (Element) sibling;
                            
                            String fidElementName = fidElement.getLocalName();
                            if(fidElementName==null){
                                fidElementName= fidElement.getNodeName();//HACK ?
                            }
                            if ("FeatureId".equals(fidElementName)) {
                                fidFilter.addFid(fidElement.getAttribute("fid"));
                            }
                        }

                        sibling = sibling.getNextSibling();
                    }

                    return fidFilter;
                } else if (type == AbstractFilter.BETWEEN) {
                    BetweenFilter bfilter = FILTER_FACT.createBetweenFilter();

                    NodeList kids = child.getChildNodes();

                    if (kids.getLength() < NUM_BETWEEN_CHILDREN) {
                        throw new IllegalFilterException(
                            "wrong number of children in Between filter: expected 3 got "
                            + kids.getLength());
                    }

                    Node value = child.getFirstChild();

                    while (value.getNodeType() != Node.ELEMENT_NODE) {
                        value = value.getNextSibling();
                    }

                    // first expression
                    //value = kid.getFirstChild();
                    //while(value.getNodeType() != Node.ELEMENT_NODE ) 
                    //value = value.getNextSibling();
                    LOGGER.finer("add middle value -> " + value + "<-");
                    bfilter.addMiddleValue(ExpressionDOMParser.parseExpression(
                            value));

                    for (int i = 0; i < kids.getLength(); i++) {
                        Node kid = kids.item(i);

                        String kidName = (kid.getLocalName()!=null)?kid.getLocalName():kid.getNodeName(); 
                        if (kidName.equalsIgnoreCase("LowerBoundary")) {
                            value = kid.getFirstChild();

                            while (value.getNodeType() != Node.ELEMENT_NODE) {
                                value = value.getNextSibling();
                            }

                            LOGGER.finer("add left value -> " + value + "<-");
                            bfilter.addLeftValue(ExpressionDOMParser
                                .parseExpression(value));
                        }

                        if (kidName.equalsIgnoreCase("UpperBoundary")) {
                            value = kid.getFirstChild();

                            while (value.getNodeType() != Node.ELEMENT_NODE) {
                                value = value.getNextSibling();
                            }

                            LOGGER.finer("add right value -> " + value + "<-");
                            bfilter.addRightValue(ExpressionDOMParser
                                .parseExpression(value));
                        }
                    }

                    return bfilter;
                } else if (type == AbstractFilter.LIKE) {
                    String wildcard = null;
                    String single = null;
                    String escape = null;
                    String pattern = null;
                    Expression value = null;
                    NodeList map = child.getChildNodes();

                    for (int i = 0; i < map.getLength(); i++) {
                        Node kid = map.item(i);

                        if ((kid == null)
                                || (kid.getNodeType() != Node.ELEMENT_NODE)) {
                            continue;
                        }

                        String res = (kid.getLocalName()!=null)?kid.getLocalName():kid.getNodeName(); 

                        if (res.equalsIgnoreCase("PropertyName")) {
                            value = ExpressionDOMParser.parseExpression(kid);
                        }

                        if (res.equalsIgnoreCase("Literal")) {
                            pattern = ExpressionDOMParser.parseExpression(kid)
                                                         .toString();
                        }
                    }

                    NamedNodeMap kids = child.getAttributes();

                    for (int i = 0; i < kids.getLength(); i++) {
                        Node kid = kids.item(i);

                        //if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                        String res = (kid.getLocalName()!=null)?kid.getLocalName():kid.getNodeName(); 

                        if (res.equalsIgnoreCase("wildCard")) {
                            wildcard = kid.getNodeValue();
                        }

                        if (res.equalsIgnoreCase("singleChar")) {
                            single = kid.getNodeValue();
                        }

                        if (res.equalsIgnoreCase("escapeChar")
                                || res.equalsIgnoreCase("escape")) {
                            escape = kid.getNodeValue();
                        }
                    }

                    if (!((wildcard == null) || (single == null)
                            || (escape == null) || (pattern == null))) {
                        LikeFilter lfilter = FILTER_FACT.createLikeFilter();
                        LOGGER.finer("Building like filter " + value.toString()
                            + "\n" + pattern + " " + wildcard + " " + single
                            + " " + escape);
                        lfilter.setValue(value);
                        lfilter.setPattern(pattern, wildcard, single, escape);

                        return lfilter;
                    }

                    LOGGER.finer("Problem building like filter\n" + pattern
                        + " " + wildcard + " " + single + " " + escape);

                    return null;
                } else if (type == AbstractFilter.NULL) {
                    return parseNullFilter(child);
                } else {
                    filter = new CompareFilterImpl(type);
                }

                // find and parse left and right values
                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finest("add left value -> " + value + "<-");
                filter.addLeftValue(ExpressionDOMParser.parseExpression(value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finest("add right value -> " + value + "<-");
                filter.addRightValue(ExpressionDOMParser.parseExpression(value));
                return filter;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build filter: " + ife);

                return null;
            }
        } else if (spatial.containsKey(childName)) {
            LOGGER.finest("a spatial filter " + childName);

            try {
                short type = ((Integer) spatial.get(childName)).shortValue();
                GeometryFilter filter = FILTER_FACT.createGeometryFilter(type);
                Node value = child.getFirstChild();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finest("add left value -> " + value + "<-");
                filter.addLeftGeometry(ExpressionDOMParser.parseExpression(
                        value));
                value = value.getNextSibling();

                while (value.getNodeType() != Node.ELEMENT_NODE) {
                    value = value.getNextSibling();
                }

                LOGGER.finest("add right value -> " + value + "<-");

                String valueName = (value.getLocalName()!=null)?value.getLocalName():value.getNodeName(); 
                if (!(valueName.equalsIgnoreCase("Literal")
                        || valueName.equalsIgnoreCase("propertyname"))) {
                    Element literal = value.getOwnerDocument().createElement("literal");

                    literal.appendChild(value);
                    LOGGER.finest("Built new literal " + literal);
                    value = literal;
                }

                filter.addRightGeometry(ExpressionDOMParser.parseExpression(
                        value));

                return filter;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build filter: " + ife);

                return null;
            }
        } else if (logical.containsKey(childName)) {
            LOGGER.finest("a logical filter " + childName);

            try {
                short type = ((Integer) logical.get(childName)).shortValue();
                LOGGER.finest("logic type " + type);

                LogicFilter filter = FILTER_FACT.createLogicFilter(type);
                NodeList map = child.getChildNodes();

                for (int i = 0; i < map.getLength(); i++) {
                    Node kid = map.item(i);

                    if ((kid == null)
                            || (kid.getNodeType() != Node.ELEMENT_NODE)) {
                        continue;
                    }

                    LOGGER.finest("adding to logic filter " + kid.getLocalName());
                    filter.addFilter(parseFilter(kid));
                }

                return filter;
            } catch (IllegalFilterException ife) {
                LOGGER.warning("Unable to build filter: " + ife);

                return null;
            }
        }

        LOGGER.warning("unknown filter " + root);

        return null;
    }

    /**
     * Parses a null filter from a node known to be a null node.
     *
     * @param nullNode the PropertyIsNull node.
     *
     * @return a null filter of the expression contained in null node.
     *
     * @throws IllegalFilterException DOCUMENT ME!
     */
    private static NullFilter parseNullFilter(Node nullNode)
        throws IllegalFilterException {
        LOGGER.info("parsing null node: " + nullNode);

        NullFilter nFilter = FILTER_FACT.createNullFilter();
        Node value = nullNode.getFirstChild();

        while (value.getNodeType() != Node.ELEMENT_NODE) {
            value = value.getNextSibling();
        }

        LOGGER.finest("add null value -> " + value + "<-");
        nFilter.nullCheckValue(ExpressionDOMParser.parseExpression(value));

        return nFilter;
    }

}