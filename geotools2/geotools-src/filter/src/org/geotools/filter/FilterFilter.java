/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.filter;

// J2SE dependencies
import java.util.logging.Logger;
import java.util.*;
import java.math.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.Geometry;

// Geotools dependencies
import org.geotools.feature.*;
import org.geotools.gml.GMLHandlerJTS;


/**
 * Creates an OGC filter using a SAX filter.
 *
 * <p>Possibly the worst-named class of all time, <code>FilterFilter</code>
 * extracts an OGC filter object from an XML stream and passes it to its parent
 * as a fully instantiated OGC filter object.</p>
 * 
 * @version $Id: FilterFilter.java,v 1.15 2002/12/13 19:43:08 cholmesny Exp $
 * @author Rob Hranac, Vision for New York
 */
public class FilterFilter 
    extends XMLFilterImpl 
    implements GMLHandlerJTS {

    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private LogicSAXParser logicFactory;  

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private FilterSAXParser filterFactory;  

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private ExpressionSAXParser expressionFactory;  
    
    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private FilterHandler parent;  
    
    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private FeatureType schema;  
        
    /** Whether or not this parser should consider namespaces. */
    private boolean namespaceAware = true;
    
    /** Whether or not this parser should consider namespaces. */
    private boolean isLogicFilter = false;
    
    /** Whether or not this parser should consider namespaces. */
    private boolean isFidFilter = false;
    
    /** Whether or not this parser should consider namespaces. */
    private boolean insideFilter = false;
    
    // Static Globals to handle some expected elements
    /** GML namespace string. */
    private static final String GML_NAMESPACE = "http://www.opengis.net/gml";

    
    /**
     * Constructor with parent, which must implement GMLHandlerJTS.
     *
     * @param parent The parent of this filter.
     */
    public FilterFilter (FilterHandler parent, FeatureType schema) {
        super();
        this.parent = parent;
        this.schema = schema;
        expressionFactory = new ExpressionSAXParser(schema);
        filterFactory = new FilterSAXParser();  
        logicFactory = new LogicSAXParser();
    }
    
    
    /**
     * Checks for GML element start and - if not a coordinates element - sends
     * it directly on down the chain to the appropriate parent handler.  If it
     * is a coordinates (or coord) element, it uses internal methods to set the
     * current state of the coordinates reader appropriately. 
     *
     * @param namespaceURI The namespace of the element.
     * @param localName The local name of the element.
     * @param qName The full name of the element, including namespace prefix.
     * @param atts The element attributes.
     * @throws SAXException Some parsing error occured while reading
     * coordinates.
     */
    public void startElement(String namespaceURI, String localName, 
                             String qName, Attributes atts)
        throws SAXException {


        LOGGER.finer("found start element: " + localName);
        if(localName.equals("Filter")) {
            insideFilter = true;
        }
        else if (insideFilter) {          
            short filterElementType = convertType(localName);
            LOGGER.finest("types: (xml): " + localName + "; "  + 
                          "(internal): " + filterElementType);
            
            try { 
                if(isFidFilter) {
                    if(filterElementType == AbstractFilter.FID ){
                        LOGGER.finer("sending attributes to existing FID filter");
                        filterFactory.setAttributes(atts);
                    }
                    else {
                        isFidFilter = false;             
                        LOGGER.finer("is fid (1): " + isFidFilter);
                        // if the filter is done, pass along to the parent
                        if( isLogicFilter ) {
                            logicFactory.add( filterFactory.create());
                        }
                        else {
                            parent.filter( filterFactory.create());
                        }
                    }
                }
                else {
                    // if at a complex filter start, add it to the logic stack
                    LOGGER.finest("is logic?");
                    if( AbstractFilter.isLogicFilter(filterElementType) ) {
                        LOGGER.finer("found a logic filter start");
                        isLogicFilter = true;
                        logicFactory.start(filterElementType);
                    }
                    
                    // if at a simple filter start, tell the factory
                    else if( AbstractFilter.isSimpleFilter(filterElementType) ) {
                        LOGGER.finer("found a simple filter start");
                        filterFactory.start(filterElementType);
                        if(filterElementType == AbstractFilter.LIKE ){
                            LOGGER.finer("sending attributes for like filter");
                            filterFactory.setAttributes(atts);
                        }
                        else if(filterElementType == AbstractFilter.FID ){
                            LOGGER.finer("sending attributes to new FID filter");
                            filterFactory.setAttributes(atts);
                            isFidFilter = true;
                            LOGGER.finer("is fid (3): " + isFidFilter);
                        }                            
                    }
                
                    // if at an expression start, tell the factory
                    else if( DefaultExpression.isExpression(filterElementType) ) {
                        LOGGER.finest("found an expression filter start");
                        expressionFactory.start(localName);
                    }
                }
            }
            catch (IllegalFilterException e) {
                throw new SAXException("Attempted to construct illegal filter: " +
                                       e.getMessage(), e);
            }
        }
        else {
            parent.startElement(namespaceURI, localName, qName, atts);
        }
    }
        
    
    /**
     * Reads the only internal characters read by pure GML parsers, which are
     * coordinates.  These coordinates are sent to the coordinates reader
     * class, which interprets them appropriately, depeding on the its current
     * state.
     *
     * @param ch Raw coordinate string from the GML document.
     * @param start Beginning character position of raw coordinate string.
     * @param length Length of the character string.
     * @throws SAXException Some parsing error occurred while reading
     * coordinates.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        
        // the methods here read in both coordinates and coords and take the 
        //  grunt-work out of this task for geometry handlers
        //  see the documentation for CoordinatesReader to see what this entails
        String message = new String(ch, start, length);
        
        if(insideFilter) {          
            try{
                LOGGER.finest("sending to expression factory: " + message);
                expressionFactory.message(message);
            }
            catch(IllegalFilterException ife){
                throw new SAXException(ife);
            }
        }
        else {
            parent.characters(ch, start, length);
        }
    }
    
    /**
     * Checks for GML element end and - if not a coordinates element - sends it
     * directly on down the chain to the appropriate parent handler.  If it is
     * a coordinates (or coord) element, it uses internal methods to set the 
     * current state of the coordinates reader appropriately.
     *
     * @param namespaceURI Namespace of the element.
     * @param localName Local name of the element.
     * @param qName Full name of the element, including namespace prefix.
     * @throws SAXException Parsing error occurred while reading coordinates.
     */
    public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {
        

        LOGGER.finer("found end element: " + localName);
        if(localName.equals("Filter")) {
	    //moved by cholmes, bug fix for fid.
	    if( isFidFilter && !localName.equals("FeatureId") ) {
		isFidFilter = false;             
		LOGGER.finer("is fid (2): " + isFidFilter);
		// if the filter is done, pass along to the parent
		try {
		    if( isLogicFilter ) {
			logicFactory.add( filterFactory.create());
		    }
		    else {
			parent.filter( filterFactory.create());
		    }
		}  catch (IllegalFilterException e) {
                throw new SAXException("Attempted to construct illegal filter: " +
                                       e.getMessage());
		}

	    }
	
	insideFilter = false;
	}    
        else if (insideFilter) {          
            short filterElementType = convertType(localName);
        
            try {
		    {  
		// if at the end of a complex filter, simplify the logic stack
                    //  appropriately
                    if( AbstractFilter.isLogicFilter(filterElementType) ) {
                        LOGGER.finest("found a logic filter end");
                        logicFactory.end( filterElementType);
                        
                        // if the filter is done, pass along to the parent
                        if( logicFactory.isComplete()) {
                            LOGGER.finer("creating logic factory");
                            parent.filter( logicFactory.create());
                        }
                    }
                    
                    
                    // if at the end of a simple filter, create it and push it on 
                    //  top of the logic stack
                    else if( AbstractFilter.isSimpleFilter(filterElementType) && !isFidFilter) {
                        LOGGER.finest("found a simple filter end");
                        
                        // if the filter is done, pass along to the parent
                        if( isLogicFilter ) {
                            logicFactory.add( filterFactory.create());
                        }
                        else {
                            parent.filter( filterFactory.create());
                        }
                    }
                    
                    // if at the end of an expression, two cases:
                    //  1. at the end of an outer expression, create it and pass to filter
                    //  2. at end of an inner expression, pass the message along to 
                    //      current outer expression
                    else if( DefaultExpression.isExpression(filterElementType) ) {
                        LOGGER.finer("found an expression filter end");
                        expressionFactory.end(localName);
                        if( expressionFactory.isReady() ) {
                            LOGGER.finer("expression factory is ready");
                            filterFactory.expression( expressionFactory.create());
                        }
                    }
                }
            }
            catch (IllegalFilterException e) {
                throw new SAXException("Attempted to construct illegal filter: " +
                                       e.getMessage());
            }
        }
        else {
            parent.endElement(namespaceURI, localName, qName);
        }
    }		
        
    /**
     * Gets geometry.
     *
     * @param geometry The geometry from the filter.
     */
    public void geometry(Geometry geometry) {

        // Sends the geometry to the expression
        try {
            LOGGER.finer("got geometry: " + geometry);
            expressionFactory.geometry(geometry);
            if( expressionFactory.isReady() ) {
                LOGGER.finer("expression factory made expression and sent to filter factory");
                filterFactory.expression( expressionFactory.create());
            }
        }
        catch(IllegalFilterException e) {
            LOGGER.finer("Had problems adding geometry: " + geometry.toString());
        }
    }


    /* ************************************************************************
     * Following static methods check for certain aggregate types, based on 
     * (above) declared types.  Note that these aggregate types do not
     * necessarily map directly to the sub-classes of AbstractFilter.  In most,
     * but not all, cases, a single class implements an aggregate type.
     * However, there are aggregate types that are implemented by multiple
     * classes (ie. the Math type is implemented by two separate classes).
     * ***********************************************************************/

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a logic filter type.
     */
    protected static short convertType(String filterType) {

        // matches all filter types to the default logic type
        if( filterType.equals("Or") ) {
            return AbstractFilter.LOGIC_OR;
        }
        else if( filterType.equals("And") ) {
            return AbstractFilter.LOGIC_AND;
        }
        else if( filterType.equals("Not") ) {
            return AbstractFilter.LOGIC_NOT;
        }
        else if( filterType.equals("Equals") ) {
            return AbstractFilter.GEOMETRY_EQUALS;
        }
        else if( filterType.equals("Disjoint") ) {
            return AbstractFilter.GEOMETRY_DISJOINT;
        }
        else if( filterType.equals("Intersects") ) {
            return AbstractFilter.GEOMETRY_INTERSECTS;
        }
        else if( filterType.equals("Touches") ) {
            return AbstractFilter.GEOMETRY_TOUCHES;
        }
        else if( filterType.equals("Crosses") ) {
            return AbstractFilter.GEOMETRY_CROSSES;
        }
        else if( filterType.equals("Within") ) {
            return AbstractFilter.GEOMETRY_WITHIN;
        }
        else if( filterType.equals("Contains") ) {
            return AbstractFilter.GEOMETRY_CONTAINS;
        }
        else if( filterType.equals("Overlaps") ) {
            return AbstractFilter.GEOMETRY_OVERLAPS;
        }
        else if( filterType.equals("Beyond") ) {
            return AbstractFilter.GEOMETRY_BEYOND;
        }
        else if( filterType.equals("BBOX") ) {
            return AbstractFilter.GEOMETRY_BBOX;
        }
        else if( filterType.equals("PropertyIsEqualTo") ) {
            return AbstractFilter.COMPARE_EQUALS;
        }
        else if( filterType.equals("PropertyIsLessThan") ) {
            return AbstractFilter.COMPARE_LESS_THAN;
        }
        else if( filterType.equals("PropertyIsGreaterThan") ) {
            return AbstractFilter.COMPARE_GREATER_THAN;
        }
        else if( filterType.equals("PropertyIsLessThanOrEqualTo") ) {
            return AbstractFilter.COMPARE_LESS_THAN_EQUAL;
        }
        else if( filterType.equals("PropertyIsGreaterThanOrEqualTo") ) {
            return AbstractFilter.COMPARE_GREATER_THAN_EQUAL;
        }
        else if( filterType.equals("PropertyIsBetween") ) {
            return AbstractFilter.BETWEEN;
        }
        else if( filterType.equals("PropertyIsLike") ) {
            return AbstractFilter.LIKE;
        }
        else if( filterType.equals("PropertyIsNull") ) {
            return AbstractFilter.NULL;
        }
        else if( filterType.equals("FeatureId") ) {
            return AbstractFilter.FID;
        }
        else if( filterType.equals("Add") ) {
            return DefaultExpression.MATH_ADD;
        }
        else if( filterType.equals("Sub") ) {
            return DefaultExpression.MATH_SUBTRACT;
        }
        else if( filterType.equals("Mul") ) {
            return DefaultExpression.MATH_MULTIPLY;
        }
        else if( filterType.equals("Div") ) {
            return DefaultExpression.MATH_DIVIDE;
        }
        else if( filterType.equals("PropertyName") ) {
            return DefaultExpression.LITERAL_DOUBLE;
        }
        else if( filterType.equals("Literal") ) {
            return DefaultExpression.ATTRIBUTE_DOUBLE;
        }
        else {
            return -1;
        }
    }

    
}
