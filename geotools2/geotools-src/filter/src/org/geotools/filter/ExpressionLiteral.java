/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.*;

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines an expression that holds a literal for return.
 *
 * @author Rob Hranac, Vision for New York
 * @version 04/18/02
 */
public class ExpressionLiteral extends ExpressionDefault {


    /** Holds a reference to the literal. */
    protected Object literal = null;


    /**
     * Constructor with literal.
     *
     * @throws IllegalFilterException This literal type is not in scope.
     */
    public ExpressionLiteral () {
    }


    /**
     * Constructor with literal.
     *
     * @param literal The literal to store inside this expression.
     * @throws IllegalFilterException This literal type is not in scope.
     */
    public ExpressionLiteral (Object literal)
        throws IllegalFilterException {
        
        this.setLiteral(literal);
    }


    /**
     * Return the literal type.
     */
    public short getType() {
        return expressionType;
    }

    /**
     * Set the literal.
     *
     * @param literal The literal to store inside this expression.
     * @throws IllegalFilterException This literal type is not in scope.
     */
    public void setLiteral(Object literal)
        throws IllegalFilterException {
        
        if( literal instanceof Double ) {
            expressionType = LITERAL_DOUBLE;
        }
        else if( literal instanceof Integer ) {
            expressionType = LITERAL_INTEGER;
        }
        else if( literal instanceof String ) {
            expressionType = LITERAL_STRING;
        }
        else if( literal instanceof Geometry ) {
            expressionType = LITERAL_GEOMETRY;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to add a literal with non-supported type " +
                 "(ie. not Double, Integer, String).");
        }

        this.literal = literal;
    }


    /**
     * Gets the value of this literal.
     *
     * @param feature Required by the interface but not used.
     * @throws MalformedFilterException This literal type is not in scope.
     */
    public Object getValue(Feature feature)
        throws MalformedFilterException {

        return literal;
    }
        
    
}
