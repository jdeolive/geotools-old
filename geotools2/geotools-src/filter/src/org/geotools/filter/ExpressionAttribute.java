/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

/**
 * Defines a complex filter (could also be called logical filter).
 *
 * This filter holds one or more filters together and relates
 * them logically in an internally defined.
 *
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public class ExpressionAttribute extends ExpressionDefault {

    /** Holds all sub filters of this filter. */
    protected String attributePath = new String();


    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attributePath The initial (required) sub filter.
     * @param expressionType The final relation between all sub filters.
     */
    public ExpressionAttribute () {
        this.expressionType = ATTRIBUTE_UNDECLARED;
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attributePath The initial (required) sub filter.
     * @param expressionType The final relation between all sub filters.
     */
    public ExpressionAttribute (String attributePath, short expressionType) {
        this.expressionType = expressionType;
        this.attributePath = attributePath;
    }


    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param attributePath The initial (required) sub filter.
     */
    public void setAttributePath(String attributePath) {
        this.attributePath = attributePath;
    }

    /**
     * Constructor with minimum dataset for a valid expression.
     *
     * @param expressionType The final relation between all sub filters.
     */
    public void setExpressionType(short expressionType) {
        this.expressionType = expressionType;
    }

    /**
     * Gets the value of this attribute from the passed feature.
     *
     * @param feature Feature from which to extract attribute value.
     */
    public Object getValue(Feature feature) 
        throws MalformedFilterException {

        // MUST HANDLE AN ATTRIBUTE NOT FOUND EXCEPTION HERE
        Object tempAttribute = feature.getAttribute(attributePath);
        
        // Check to make sure that attribute conforms to advertised type before 
        //  returning
        if( ((tempAttribute instanceof Double) && 
             (expressionType == ATTRIBUTE_DOUBLE)) || 
            ((tempAttribute instanceof Integer) && 
             (expressionType == ATTRIBUTE_INTEGER)) ||
            ((tempAttribute instanceof String) && 
             (expressionType == ATTRIBUTE_STRING)) ||
            permissiveConstruction ) {
            return tempAttribute;
        }
        else {
            throw new MalformedFilterException
                ("Attribute does not conform to advertised type: "
                 + expressionType);
        }
        
    }
        
    
}
