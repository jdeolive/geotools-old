/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import java.util.*;
import java.math.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * Creates an OGC filter using a SAX filter.
 *
 * <p>Possibly the worst-named class of all time, <code>FilterFilter</code>
 * extracts an OGC filter object from an XML stream and passes it to its parent
 * as a fully instantiated OGC filter object.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public class FilterFilter extends XMLFilterImpl {

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private StackLogic logicStack = new StackLogic();  

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private FactoryFilter filterFactory = new FactoryFilter();  

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private FactoryExpression expressionFactory = new FactoryExpression();  
    
    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private HandlerFilter parent;  
    
    
    
    /** Whether or not this parser should consider namespaces */
    private boolean namespaceAware = true;
    
    // Static Globals to handle some expected elements
    /** GML namespace string */
    private static final String GML_NAMESPACE = "http://www.opengis.net/gml";

    
    /**
     * Constructor with parent, which must implement GMLHandlerJTS.
     *
     * @param parent The parent of this filter.
     */
    public FilterFilter (HandlerFilter parent) {
        super();
        this.parent = parent;
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
     * @throws SAXException Some parsing error occured while reading coordinates
     */
    public void startElement(String namespaceURI, String localName, 
                             String qName, Attributes atts)
        throws SAXException {

        short filterElementType = convertType(localName);
        
        try { 
            // if at a complex filter start, add it to the logic stack
            if( FilterDefault.isLogicFilter(filterElementType) ) {
                logicStack.addLogic(filterElementType);
            }
            
            // if at a simple filter start, tell the factory
            else if( FilterDefault.isSimpleFilter(filterElementType) ) {
                filterFactory.start(filterElementType);
            }
            
            // if at an expression start, tell the factory
            else if( ExpressionDefault.isExpression(filterElementType) ) {
                expressionFactory.start(localName);
            }
            else {
            }
        }
        catch (IllegalFilterException e) {
            throw new SAXException("Attempted to construct illegal filter: " +
                                   e.getMessage());
        }
    }
    
    
    /**
     * Reads the only internal characters read by a pure GML parsers, which are
     * coordinates.  These coordinates are sent to the coordinates reader class,
     * which interprets them appropriately, depeding on the its current state.
     *
     * @param ch Raw coordinate string from the GML document.
     * @param start Beginning character position of raw coordinate string.
     * @param length Length of the character string.
     * @throws SAXException Some parsing error occured while reading coordinates.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        
        // the methods here read in both coordinates and coords and take the 
        //  grunt-work out of this task for geometry handlers
        //  see the documentation for CoordinatesReader to see what this entails
        String message = new String(ch, start, length);
        expressionFactory.message(message);
        
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
     * @throws SAXException Parsing error occured while reading coordinates.
     */
    public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {
        

        short filterElementType = convertType(localName);
        
        try {
            // if at the end of a complex filter, simplify the logic stack
            //  appropriately
            if( FilterDefault.isLogicFilter(filterElementType) ) {
                logicStack.simplifyLogic(filterElementType);
            }
            
            // if at the end of a simple filter, create it and push it on top of 
            //  the logic stack
            else if( FilterDefault.isSimpleFilter(filterElementType) ) {
                logicStack.push( filterFactory.create() );
            }
            
            // if at the end of an expression, two cases:
            //  1. at the end of an outer expression, create it and pass to filter
            //  2. at end of an inner expression, pass the message along to current
            //      outer expression
            else if( ExpressionDefault.isExpression(filterElementType) ) {
                expressionFactory.end(localName);
                if( expressionFactory.isReady() ) {
                    filterFactory.expression( expressionFactory.create() );
                }
            }
            
            // if the filter is done, pass along to the parent
            if( logicStack.isComplete() ) {
                parent.filter( (Filter) logicStack.pop() );
            }
            
        }
        catch (IllegalFilterException e) {
            throw new SAXException("Attempted to construct illegal filter: " +
                                   e.getMessage());
        }
        
    }		

    /* *************************************************************************
     * Following static methods check for certain aggregate types, based on 
     * (above) declared types.  Note that these aggregate types do not
     * necessarily map directly to the sub-classes of FilterDefault.  In most,
     * but not all, cases, a single class implements an aggregate type.
     * However, there are aggregate types that are implemented by multiple
     * classes (ie. the Math type is implemented by two seperate classes).
     * ************************************************************************/

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a logic filter type.
     */
    protected static short convertType(String filterType) {

        // matches all filter types to the default logic type
        if( filterType.equals("Or") ) {
            return FilterDefault.LOGIC_OR;
        }
        else if( filterType.equals("And") ) {
            return FilterDefault.LOGIC_AND;
        }
        else if( filterType.equals("Not") ) {
            return FilterDefault.LOGIC_NOT;
        }
        else if( filterType.equals("Equals") ) {
            return FilterDefault.GEOMETRY_EQUALS;
        }
        else if( filterType.equals("Disjoint") ) {
            return FilterDefault.GEOMETRY_DISJOINT;
        }
        else if( filterType.equals("Intersects") ) {
            return FilterDefault.GEOMETRY_INTERSECTS;
        }
        else if( filterType.equals("Touches") ) {
            return FilterDefault.GEOMETRY_TOUCHES;
        }
        else if( filterType.equals("Crosses") ) {
            return FilterDefault.GEOMETRY_CROSSES;
        }
        else if( filterType.equals("Within") ) {
            return FilterDefault.GEOMETRY_WITHIN;
        }
        else if( filterType.equals("Contains") ) {
            return FilterDefault.GEOMETRY_CONTAINS;
        }
        else if( filterType.equals("Overlaps") ) {
            return FilterDefault.GEOMETRY_OVERLAPS;
        }
        else if( filterType.equals("Beyond") ) {
            return FilterDefault.GEOMETRY_BEYOND;
        }
        else if( filterType.equals("BBOX") ) {
            return FilterDefault.GEOMETRY_BBOX;
        }
        else if( filterType.equals("PropertyIsEqualTo") ) {
            return FilterDefault.COMPARE_EQUALS;
        }
        else if( filterType.equals("PropertyIsLessThan") ) {
            return FilterDefault.COMPARE_LESS_THAN;
        }
        else if( filterType.equals("PropertyIsGreaterThan") ) {
            return FilterDefault.COMPARE_GREATER_THAN;
        }
        else if( filterType.equals("PropertyIsLessThanOrEqualTo") ) {
            return FilterDefault.COMPARE_LESS_THAN_EQUAL;
        }
        else if( filterType.equals("PropertyIsGreaterThanOrEqualTo") ) {
            return FilterDefault.COMPARE_GREATER_THAN_EQUAL;
        }
        else if( filterType.equals("PropertyIsBetween") ) {
            return FilterDefault.BETWEEN;
        }
        else if( filterType.equals("PropertyIsLike") ) {
            return FilterDefault.LIKE;
        }
        else if( filterType.equals("PropertyIsNull") ) {
            return FilterDefault.NULL;
        }
        else if( filterType.equals("Add") ) {
            return ExpressionDefault.MATH_ADD;
        }
        else if( filterType.equals("Sub") ) {
            return ExpressionDefault.MATH_SUBTRACT;
        }
        else if( filterType.equals("Mul") ) {
            return ExpressionDefault.MATH_MULTIPLY;
        }
        else if( filterType.equals("Div") ) {
            return ExpressionDefault.MATH_DIVIDE;
        }
        else if( filterType.equals("PropertyName") ) {
            return ExpressionDefault.LITERAL_DOUBLE;
        }
        else if( filterType.equals("Literal") ) {
            return ExpressionDefault.ATTRIBUTE_DOUBLE;
        }
        else {
            return -1;
        }
    }

    
}
