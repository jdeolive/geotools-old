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
package org.geotools.filter;


// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.Geometry;

// Geotools dependencies
import org.geotools.feature.FeatureType;
import org.geotools.gml.GMLHandlerJTS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

// J2SE dependencies
import java.util.logging.Logger;


/**
 * Creates an OGC filter using a SAX filter.
 * 
 * <p>
 * Possibly the worst-named class of all time, <code>FilterFilter</code>
 * extracts an OGC filter object from an XML stream and passes it to its
 * parent as a fully instantiated OGC filter object.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: FilterFilter.java,v 1.23 2003/10/29 23:11:47 cholmesny Exp $
 */
public class FilterFilter extends XMLFilterImpl implements GMLHandlerJTS {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** For handling and creating logic filters that come in. */
    private LogicSAXParser logicFactory;

    /** For handling and creating non-logic filters that come in */
    private FilterSAXParser filterFactory;

    /** For handling and creating expressions that come in. */
    private ExpressionSAXParser expressionFactory;

    /** Parent of the filter: must implement FilterHandler */
    private FilterHandler parent;

    /** The FeatureType to create attribute expressions against. */
    private FeatureType schema;

    /** Whether we are currently processing a logic filter. */
    private boolean isLogicFilter = false;

    /** Whether we are currently processing an fid filter. */
    private boolean isFidFilter = false;

    /** Whether Whether we are currently processing a filter. */
    protected boolean insideFilter = false;

    /** Whether we are inside a distance element or not. */
    private boolean insideDistance = false;

    /** units for a distance element attribute. somewhere else? */
    private String units;

    /**
     * Constructor with parent, which must implement GMLHandlerJTS.
     *
     * @param parent The parent of this filter, to recieve the filters created.
     * @param schema The schema that the filter will be used against.
     */
    public FilterFilter(FilterHandler parent, FeatureType schema) {
        super();
        this.parent = parent;
        this.schema = schema;
        expressionFactory = new ExpressionSAXParser(schema);
        filterFactory = new FilterSAXParser();
        logicFactory = new LogicSAXParser();
    }

    /**
     * Checks the name of the element, and sends to the appropriate filter
     * creation factory.
     *
     * @param namespaceURI The namespace of the element.
     * @param localName The local name of the element.
     * @param qName The full name of the element, including namespace prefix.
     * @param atts The element attributes.
     *
     * @throws SAXException Some parsing error occured while reading filter.
     */
    public void startElement(String namespaceURI, String localName,
        String qName, Attributes atts) throws SAXException {
        LOGGER.finer("found start element: " + localName);

        if (localName.equals("Filter")) {
            //Should we check to make sure namespace is correct?
            //perhaps let users set namespace aware...
            insideFilter = true;
        } else if (insideFilter) {
            short filterType = convertType(localName);
            LOGGER.finest("types: (xml): " + localName + "; " + "(internal): "
                + filterType);

            try {
                if (isFidFilter) {
                    if (filterType == AbstractFilter.FID) {
                        LOGGER.finer(
                            "sending attributes to existing FID filter");
                        filterFactory.setAttributes(atts);
                    } else {
                        isFidFilter = false;
                        LOGGER.finer("is fid (1): " + isFidFilter);

                        // if the filter is done, pass along to the parent
                        if (isLogicFilter) {
                            logicFactory.add(filterFactory.create());
                        } else {
                            parent.filter(filterFactory.create());
                        }
                    }
                }

                if (!isFidFilter) {
                    // if at a complex filter start, add it to the logic stack
                    LOGGER.finest("is logic?");

                    if (AbstractFilter.isLogicFilter(filterType)) {
                        LOGGER.finer("found a logic filter start");
                        isLogicFilter = true;
                        logicFactory.start(filterType);
                    } else if (AbstractFilter.isSimpleFilter(filterType)) {
                        // if at a simple filter start, tell the factory
                        LOGGER.finer("found a simple filter start");
                        filterFactory.start(filterType);

                        if (filterType == AbstractFilter.LIKE) {
                            LOGGER.finer("sending attributes for like filter");
                            filterFactory.setAttributes(atts);
                        } else if (filterType == AbstractFilter.FID) {
                            LOGGER.finer("sending attributes to new FID filter");
                            filterFactory.setAttributes(atts);
                            isFidFilter = true;
                            LOGGER.finer("is fid (3): " + isFidFilter);
                        }
                    } else if (DefaultExpression.isExpression(filterType)) {
                        // if at an expression start, tell the factory
                        LOGGER.finest("found an expression filter start");
                        expressionFactory.start(localName);
                    } else if (localName.equals("Distance")) {
                        LOGGER.finest("inside distance");

			//Not too sure what to do here, as units should be
			//required element, so an error would be nice.  
			//But geotools is also not supporting units at all,
			//so I feel like it doesn't matter so much...
                        if (("units").equals(atts.getLocalName(0))) {
                            units = atts.getValue(0);
                            LOGGER.finest("units = " + units);
                        }

                        insideDistance = true;
                    }
                }
            } catch (IllegalFilterException ife) {
                throw new SAXException("Attempted to construct illegal "
                    + "filter: " + ife.getMessage(), ife);
            }
        } else {
            parent.startElement(namespaceURI, localName, qName, atts);
        }
    }

    /**
     * Reads the only internal characters read by filters.  If we are in a
     * distance filter than the distance is set in the filter factory, if not
     * we forward directly along to the expression factory.
     *
     * @param chars Raw coordinate string from the filter document.
     * @param start Beginning character position of raw string.
     * @param length Length of the character string.
     *
     * @throws SAXException Some parsing error occurred while reading
     *         coordinates.
     */
    public void characters(char[] chars, int start, int length)
        throws SAXException {
        String message = new String(chars, start, length);

        if (insideFilter) {
            try {
                if (insideDistance) {
                    LOGGER.finest("calling set distance on " + message + ", "
                        + units);
                    filterFactory.setDistance(message, units);
                } else {
                    LOGGER.finest("sending to expression factory: " + message);
                    expressionFactory.message(message);
                }
            } catch (IllegalFilterException ife) {
                throw new SAXException(ife);
            }
        } else {
            parent.characters(chars, start, length);
        }
    }

    /**
     * Checks for filter element end and - if not a Filter then sends it
     * directly to the appropriate filter factory.
     *
     * @param namespaceURI Namespace of the element.
     * @param localName Local name of the element.
     * @param qName Full name of the element, including namespace prefix.
     *
     * @throws SAXException Parsing error occurred while reading coordinates.
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        LOGGER.finer("found start element: " + localName);

        if (localName.equals("Filter")) {
            //moved by cholmes, bug fix for fid.
            if (isFidFilter && !localName.equals("FeatureId")) {
                isFidFilter = false;
                LOGGER.finer("is fid (2): " + isFidFilter);

                // if the filter is done, pass along to the parent
                try {
                    if (isLogicFilter) {
                        logicFactory.add(filterFactory.create());
                    } else {
                        parent.filter(filterFactory.create());
                    }
                } catch (IllegalFilterException e) {
                    throw new SAXException(
                        "Attempted to construct illegal filter: "
                        + e.getMessage());
                }
            }

            insideFilter = false;
        } else if (insideFilter) {
            short filterType = convertType(localName);

            try {
                // if at the end of a complex filter, simplify the logic stack
                //  appropriately
                if (AbstractFilter.isLogicFilter(filterType)) {
                    LOGGER.finest("found a logic filter end");

                    if (isFidFilter) {
                        logicFactory.add(filterFactory.create());
                        isFidFilter = false;
                    }

                    logicFactory.end(filterType);

                    // if the filter is done, pass along to the parent
                    if (logicFactory.isComplete()) {
                        LOGGER.finer("creating logic factory");
                        parent.filter(logicFactory.create());
                    }

                    //isFidFilter = false;
                } else if (AbstractFilter.isSimpleFilter(filterType)
                        && !isFidFilter) {
                    // if at the end of a simple filter, create it and push it 
                    // on top of the logic stack
                    LOGGER.finest("found a simple filter end");

                    // if the filter is done, pass along to the parent
                    if (isLogicFilter) {
                        logicFactory.add(filterFactory.create());
                    } else {
                        parent.filter(filterFactory.create());
                    }
                }
                // if at the end of an expression, two cases:
                // 1. at the end of an outer expression, create it and pass 
                //    to filter
                //  2. at end of an inner expression, pass the message along to
                //      current outer expression
                else if (DefaultExpression.isExpression(filterType)) {
                    LOGGER.finer("found an expression filter end");
                    expressionFactory.end(localName);

                    if (expressionFactory.isReady()) {
                        LOGGER.finer("expression factory is ready");
                        filterFactory.expression(expressionFactory.create());
                    }
                } else if (localName.equals("Distance")) {
                    insideDistance = false;
                }
            } catch (IllegalFilterException e) {
                throw new SAXException(
                    "Attempted to construct illegal filter: " + e.getMessage());
            }
        } else {
            parent.endElement(namespaceURI, localName, qName);
        }
    }

    /**
     * Recieves a geometry from its child filter.
     *
     * @param geometry The geometry from the filter.
     *
     * @throws RuntimeException if the filterFactory can't handle the geometry
     *
     * @task REVISIT: can we throw another exception?
     */
    public void geometry(Geometry geometry) throws RuntimeException {
        // Sends the geometry to the expression
        try {
            LOGGER.finer("got geometry: " + geometry);
            expressionFactory.geometry(geometry);

            if (expressionFactory.isReady()) {
                LOGGER.finer("expression factory made expression and sent "
                    + "to filter factory");
                filterFactory.expression(expressionFactory.create());
            }
        } catch (IllegalFilterException ife) {
            LOGGER.finer("Had problems adding geometry: " + geometry.toString());
            throw new RuntimeException("problem adding geometry to filter ", ife);
        }
    }

    /**
     * Converts the string representation of the expression to the
     * AbstractFilter or DefaultExpression short type.
     *
     * @param filterType Type of filter for check.
     *
     * @return the short representation of the filter.
     */
    protected static short convertType(String filterType) {
        // matches all filter types to the default logic type
        if (filterType.equals("Or")) {
            return AbstractFilter.LOGIC_OR;
        } else if (filterType.equals("And")) {
            return AbstractFilter.LOGIC_AND;
        } else if (filterType.equals("Not")) {
            return AbstractFilter.LOGIC_NOT;
        } else if (filterType.equals("Equals")) {
            return AbstractFilter.GEOMETRY_EQUALS;
        } else if (filterType.equals("Disjoint")) {
            return AbstractFilter.GEOMETRY_DISJOINT;
        } else if (filterType.equals("DWithin")) {
            return AbstractFilter.GEOMETRY_DWITHIN;
        } else if (filterType.equals("Intersects")) {
            return AbstractFilter.GEOMETRY_INTERSECTS;
        } else if (filterType.equals("Touches")) {
            return AbstractFilter.GEOMETRY_TOUCHES;
        } else if (filterType.equals("Crosses")) {
            return AbstractFilter.GEOMETRY_CROSSES;
        } else if (filterType.equals("Within")) {
            return AbstractFilter.GEOMETRY_WITHIN;
        } else if (filterType.equals("Contains")) {
            return AbstractFilter.GEOMETRY_CONTAINS;
        } else if (filterType.equals("Overlaps")) {
            return AbstractFilter.GEOMETRY_OVERLAPS;
        } else if (filterType.equals("Beyond")) {
            return AbstractFilter.GEOMETRY_BEYOND;
        } else if (filterType.equals("BBOX")) {
            return AbstractFilter.GEOMETRY_BBOX;
        } else if (filterType.equals("PropertyIsEqualTo")) {
            return AbstractFilter.COMPARE_EQUALS;
        } else if (filterType.equals("PropertyIsNotEqualTo")) {
            return AbstractFilter.COMPARE_NOT_EQUALS;
        } else if (filterType.equals("PropertyIsLessThan")) {
            return AbstractFilter.COMPARE_LESS_THAN;
        } else if (filterType.equals("PropertyIsGreaterThan")) {
            return AbstractFilter.COMPARE_GREATER_THAN;
        } else if (filterType.equals("PropertyIsLessThanOrEqualTo")) {
            return AbstractFilter.COMPARE_LESS_THAN_EQUAL;
        } else if (filterType.equals("PropertyIsGreaterThanOrEqualTo")) {
            return AbstractFilter.COMPARE_GREATER_THAN_EQUAL;
        } else if (filterType.equals("PropertyIsBetween")) {
            return AbstractFilter.BETWEEN;
        } else if (filterType.equals("PropertyIsLike")) {
            return AbstractFilter.LIKE;
        } else if (filterType.equals("PropertyIsNull")) {
            return AbstractFilter.NULL;
        } else if (filterType.equals("FeatureId")) {
            return AbstractFilter.FID;
        } else if (filterType.equals("Add")) {
            return DefaultExpression.MATH_ADD;
        } else if (filterType.equals("Sub")) {
            return DefaultExpression.MATH_SUBTRACT;
        } else if (filterType.equals("Mul")) {
            return DefaultExpression.MATH_MULTIPLY;
        } else if (filterType.equals("Div")) {
            return DefaultExpression.MATH_DIVIDE;
        } else if (filterType.equals("PropertyName")) {
            return DefaultExpression.LITERAL_DOUBLE;
        } else if (filterType.equals("Literal")) {
            return DefaultExpression.ATTRIBUTE_DOUBLE;
        } else {
            return -1;
        }
    }
}
