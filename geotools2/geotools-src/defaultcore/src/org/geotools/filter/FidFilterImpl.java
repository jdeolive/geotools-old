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
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a feature ID filter, which holds a list of feature IDs.
 *
 * This filter stores a series of feature IDs, which are used to distinguish
 * features uniquely.
 *
 * @version $Id: FidFilterImpl.java,v 1.7 2003/06/18 10:10:46 seangeo Exp $
 * @author Rob Hranac, TOPP
 */
public class FidFilterImpl extends AbstractFilterImpl implements FidFilter {

    /** Logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** List of the feature IDs. */
    Set fids = new HashSet();


    /** Empty constructor. */
    protected FidFilterImpl() {
        filterType = AbstractFilter.FID;
    }

    /**
     * Constructor with first fid set
     *
     * @param initialFid The type of comparison.
     */
    protected FidFilterImpl(String initialFid) {
        filterType = AbstractFilter.FID;
        addFid(initialFid);
    }


    /**
     * Adds a feature ID to the filter.
     *
     * @param fid A single feature ID.
     */
    public void addFid(String fid) {
        LOGGER.finest("got fid: " + fid);
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
     * Returns a flag indicating object equality.
     *
     * @return String representation of the compare filter.
     */
    public boolean equals(Object filter) {
        LOGGER.finest("condition: " + filter);
		if (filter != null && filter.getClass() == this.getClass()){
			LOGGER.finest("condition: " + ((FidFilterImpl) filter).filterType);
			if(((FidFilterImpl) filter).filterType == AbstractFilter.FID) {
				return fids.equals(((FidFilterImpl) filter).fids);
			} else {
			 	return false;
			}
		} else {
			return false;
		}
    }

    /** Returns all the fids in this filter.
     *
     * @return An array of all the fids in this filter.
     */
    public String[] getFids() {
		return (String[])fids.toArray(new String[0]);
	}

    /** Used by FilterVisitors to perform some action on this filter instance.
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
