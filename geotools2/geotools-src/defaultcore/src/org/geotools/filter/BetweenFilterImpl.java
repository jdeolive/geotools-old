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
 * @version $Id: BetweenFilterImpl.java,v 1.1 2002/10/24 12:02:57 ianturton Exp $
 * @author Rob Hranac, TOPP
 */
public class BetweenFilterImpl extends CompareFilterImpl {

    /** The 'middle' value, which must be an attribute expression. */
    protected Expression middleValue = null;


    /**
     * Constructor which flags the operator as between.
     */
    public BetweenFilterImpl () 
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
        
            this.middleValue = middleValue;

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
     * Returns true if the passed in object is the same as
     * this filter.  Checks to make sure the filter types are
     * the same as well as all three of the values.
     *
     * @return True if the objects are equal.
     */
     public boolean equals(Object oFilter) {
	if (oFilter.getClass() == this.getClass()){
	    BetweenFilterImpl bFilter = (BetweenFilterImpl)oFilter;
	    return (bFilter.getFilterType() == this.filterType &&
		    bFilter.getLeftValue().equals(this.leftValue) &&
		    bFilter.getMiddleValue().equals(this.middleValue) &&
		    bFilter.getRightValue().equals(this.rightValue));
	} else {
	    return false;
	}
     }
    
}
