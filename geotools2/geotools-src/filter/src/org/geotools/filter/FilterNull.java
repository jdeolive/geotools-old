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
 * Defines a null filter, which checks to see if an attribute is null.
 *
 * @version $Id: FilterNull.java,v 1.4 2002/06/05 14:03:58 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class FilterNull extends FilterDefault {

    /** The null check value, which must be an attribute expression. */
    protected Expression nullCheck = null;


    /**
     * Constructor which flags the operator as between.
     */
    public FilterNull () {
        filterType = NULL;
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param nullCheckValue The value of this 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void nullCheckValue(Expression nullCheck)
        throws IllegalFilterException {
        
        if( ExpressionDefault.isAttributeExpression( nullCheck.getType() ) ) {
            this.nullCheck = nullCheck;
        }
        else {
            throw new IllegalFilterException("Attempted to add non-attribute expression to a null filter.");
        }
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws IllegalFilterException Filter is not internally consistent.
     */
    public boolean contains(Feature feature)
        throws MalformedFilterException {

        if( nullCheck == null ) {
            throw new MalformedFilterException("Null filter expression missing.");
        }
        else {
            return (nullCheck.getValue(feature) == null);
        }
    }
    
}
