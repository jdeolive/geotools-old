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
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a comparison filter (can be a math comparison or generic equals).
 *
 * This filter implements a comparison - of some sort - between two expressions.
 * The comparison may be a math comparison or a generic equals comparison.  If
 * it is a math comparison, only math expressions are allowed; if it is an
 * equals comparison, any expression types are allowed.
 *
 * @version $Id: FidFilter.java,v 1.1 2002/09/05 20:36:57 robhranac Exp $
 * @author Rob Hranac, TOPP
 */
public class FidFilter extends AbstractFilter {

    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** A list of the feature IDs. */
    private Set fids = new HashSet();


    /**
     * Constructor with filter type.
     *
     * @param filterType The type of comparison.
     */
    public FidFilter () {}


    /**
     * Adds the 'left' value to this filter.
     *
     * @param fid Expression for 'left' value.
     */
    public void addFid(String fid) {
        LOGGER.finer("got it: " + fid);
        fids.add(fid);
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     */
    public boolean contains(Feature feature) {
        return fids.contains( feature.getId());
    }

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the compare filter.
     */
    public String toString() {
        StringBuffer fidFilter = new StringBuffer();
        
        Iterator fidIterator = fids.iterator();
        while( fidIterator.hasNext()) {
            fidFilter.append( fidIterator.next().toString());
            if( fidIterator.hasNext()) {
                fidFilter.append(", ");
            }
        }
        
        return "[ " + fidFilter.toString() + " ]";        
    }

    /** 
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which 
     * needs infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parent
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
