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

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a complex filter (could also be called logical filter).
 *
 * This filter holds one or more filters together and relates
 * them logically in an internally defined manner.
 *
 * @version $Id: ExpressionAttribute.java,v 1.1 2002/06/22 19:08:30 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
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
        Object tempAttribute = null;
        // MUST HANDLE AN ATTRIBUTE NOT FOUND EXCEPTION HERE
            try{
                tempAttribute = feature.getAttribute(attributePath);
            }
            catch(IllegalFeatureException ife){
                throw new MalformedFilterException(ife.toString());
            }
        // Check to make sure that attribute conforms to advertised type before 
        // returning
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
