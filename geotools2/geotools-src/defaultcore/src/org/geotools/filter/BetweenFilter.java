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
 * Defines a 'between' filter (which is a specialized compare filter).
 *
 * A between filter is just shorthand for a less-than-or-equal filter
 * ANDed with a greater-than-or-equal filter.  Arguably, this would be better
 * handled using those constructs, but the OGC filter specification
 * creates its own object for this, so we do as well.
 * 
 * An important note here is that a between filter is actually a math filter,
 * so its outer (left and right) expressions must be math expressions.  This
 * is enforced by the FilterAbstract class, which considers a BETWEEN operator
 * to be a math filter.
 * 
 * @version $Id: BetweenFilter.java,v 1.4 2002/07/22 20:21:55 jmacgill Exp $
 * @author Rob Hranac, TOPP
 */
public class BetweenFilter extends CompareFilter {

    /** The 'middle' value, which must be an attribute expression. */
    protected Expression middleValue = null;


    /**
     * Constructor which flags the operator as between.
     */
    public BetweenFilter () 
        throws IllegalFilterException {
        super(BETWEEN);
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param middleValue The value of this 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void addMiddleValue(Expression middleValue)
        throws IllegalFilterException {
        if (middleValue instanceof ExpressionAttribute) {
            this.middleValue = middleValue;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to add non-attribute middle expression to between filter.");
        }
    }

    public Expression getMiddleValue(){
        return middleValue;
    }
    
    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     */
    public boolean contains(Feature feature) {

        if (middleValue == null) {
            return false;
        }
        else {
            double left = ((Number) leftValue.getValue(feature)).doubleValue();
            double right = ((Number) rightValue.getValue(feature)).doubleValue();
            double mid = ((Number) middleValue.getValue(feature)).doubleValue();
            return (left <= mid) && (right >= mid);
        }
    }


    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the between filter.
     */ 
    public String toString() {
        return "[ " + leftValue.toString() + " < " + middleValue.toString() + " < " + rightValue.toString() + " ]";
    }
    
    /** 
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
}
