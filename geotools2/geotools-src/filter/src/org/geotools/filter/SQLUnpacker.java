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

import java.util.*;

/**
 * Determines which parts of a Filter can be turned into valid SQL statements.
 * Given a filter it constructs two filters, one of the supported parts of
 * the filter passed in, one of the unsupported.  If one of the constructed
 * filters is null (ie the whole filter is supported or unsupported), it is the
 * clients responsibility to deal with that.  The SQLUnpacker should be tightly
 * coordinated with the SQLEncoder.  The SQLEncoder passes its Capabilities (ie
 * which filters it can encode and which it can't) to the Unpacker, and the
 * Unpacker returns a supported filter, which should be passed to the Encoder as
 * the Encoder Capabilities claimed to fully support everything in the supported 
 * filter.  The unsupported filter should be used after the SQL statement is
 * executed, testing each feature in the result set with the contains method.
 *
 * <p>
 * This Unpacker can likely be easily used with any Encoder that has a FilterCapabilities
 * of the actions it can perform.  May want to rename it FilterUnpacker, as it is 
 * likely generic enough, but for now this name should be fine, to emphasize that
 * the SQLEncoder needs to be closely linked to it to work properly.  
 *
 * @author Chris Holmes, TOPP
 */

public class SQLUnpacker {
    
    /** FilterPair is an inner class, for holding the unsupported 
       and supported filters */
    private FilterPair pair;
    /** The types of Filters that should be part of the supported Filter */
    private FilterCapabilities capabilities;
    
     /**
     * Constructor with FilterCapabilities from the Encoder used
     * in conjunction with this Unpacker.
     *
     * @param capabilities what FilterTypes should be supported.
     */
     public SQLUnpacker(FilterCapabilities capabilities) {
	 this.capabilities = capabilities;
     }

    /**
     * Performs the unpacking of the filter, to get the results of
     * the unpacking, getUnSupported() and getSupported() must be called.
     *
     * @param filter to be unpacked.
     */
    public void unPack(Filter filter){
	//this can be easily changed to return a pair, but adding a FilterPair
	//class to the Filter package seems to add unnecessary clutter.
	pair = doUnPack(filter);
    }

  /**
     * After an unPack has been called, returns the resulting
     * Filter of the unsupported parts of the unPacked filter.
     * If the unPacked filter is fully supported this returns
     * a null, it is the client's responsibility to deal with it.
     * If there are multiple unsupported subfilters they are ANDed
     * together.
     *
     * @return A filter of the unsupported parts of the unPacked filter.
     */
    public Filter getUnSupported(){
	return pair.getUnSupported();
    }

    
    /**
     * After an unPack has been called, returns the resulting
     * Filter of the supported parts of the unPacked filter.
     * If the unPacked filter is not supported at all this returns
     * a null, it is the client's responsibility to deal with it.
     * If there are multiple supported subfilters they are ANDed
     * together.
     *
     * @return A filter of the supported parts of the unPacked filter.
     */
    public Filter getSupported(){
	return pair.getSupported();
    }


    /**
     * Performs the actual recursive unpacking of the filter.  
     *
     * @return A filter of the unsupported parts of the unPacked filter.
     */
    private FilterPair doUnPack(Filter filter) {
	/* Implementation notes:
	 * This is recursive, so it's worth explaining.  The base cases are either
	 * the filter is fully supported, ie all of its subFilters are supported, and
	 * thus it can be totally encoded, or it is not supported.  The recursive cases
	 * are when the filter is not fully supported and it is an AND or a NOT filter.
	 * In these cases the filter can be split up, and each subfilter can return some
	 * supported filters and some unsupported filters.  If it is an OR filter and not
	 * fully supported we can descend no further, as each part of the OR needs to be 
	 * tested, we can't put part in the SQL statement and part in the filter.  So if
	 * it is an AND filter, we get teh subFilters and call doUnPack on each subFilter,
	 * combining the Unsupported and Supported FilterPairs of each subFilter into a single
	 * filter pair, which is the pair we will return.  If a subfilter in turn is an AND with
	 * its own subfilters, they return their own unSupported and Supported filters, because
	 * it will eventually hit the base case.  The base cases return null for half of the
	 * filter pair, and return the filter for the other half, depending on if it's unsupported
	 * or supported.  For the NOT filter, it just descends further, unpacking the filter
	 * inside the NOT, and then tacking NOTs on the supported and unsupported sub filters.
	 */
	FilterPair retPair;
	FilterPair subPair;
	Filter subSup = null;  //for logic iteration
	Filter subUnSup = null; //for logic iteration
       	Filter retSup = null;   //for return pair
	Filter retUnSup = null; //for return pair
	if (capabilities.fullySupports(filter)) {
	    retSup = filter;
	} else {
	    short type = ((AbstractFilter)filter).getFilterType();
	    if (type == AbstractFilter.LOGIC_OR) {
		retUnSup = filter;
	    } else if (type == AbstractFilter.LOGIC_AND &&
		      capabilities.supports(AbstractFilter.LOGIC_AND)){
		   //one special case not covered, when capabilities does not support
		    // AND and it perfectly splits the filter into unsupported and supported
		Iterator filters = ((LogicFilter)filter).getFilterIterator();
		while (filters.hasNext()) {
		    subPair = doUnPack((Filter)filters.next());
		    subSup = subPair.getSupported();
		    subUnSup = subPair.getUnSupported();
		    retSup = combineFilters(retSup, subSup);
		    retUnSup = combineFilters(retUnSup, subUnSup);  
		}
	    } else if ( type == AbstractFilter.LOGIC_NOT &&
			capabilities.supports(AbstractFilter.LOGIC_NOT)){
		Iterator filters = ((LogicFilter)filter).getFilterIterator();
		subPair = doUnPack((Filter)filters.next()); //NOT only has one
		subSup = subPair.getSupported();
		subUnSup = subPair.getUnSupported();
		    if (subSup != null){
			retSup = subUnSup.not();
		    }
		    if (subUnSup != null){
			retUnSup = subUnSup.not(); 
		    }		
	    } else { //it's not supported and has no logic sub filters
		retUnSup = filter;
	    } 
	   
	}
	retPair = new FilterPair(retSup, retUnSup);
	return retPair;
    }


    /**
     * Combines two filters, which may be null, into one.  If one
     * is null and the other not, it returns the one that's not.  If
     * both are null returns null.
     *
     * @param filter1 one filter to be combined.
     * @param filter2 the other filter to be combined.
     * @return the resulting combined filter.
     */
    private Filter combineFilters(Filter filter1, Filter filter2){
	Filter retFilter;
	if (filter1 != null) {
	    if (filter2 != null){
		retFilter = filter1.and(filter2);
	    } else {
		retFilter = filter1;
	    }
	} else {
	    if (filter2 != null) {
		retFilter = filter2;
	    } else {
		retFilter = null;
	    }
	}
	return retFilter;
    }


    /**
     * An inner class to hold a pair of Filters.
     * Reasoning behind inner class is that if made public it would
     * clutter up the filter folder with a FilterPair, which seems
     * like it might be widely used, but is only necessary for one
     * class.  To return filter pairs would be slightly cleaner, in terms
     * of writing code, but this way is cleaner for the geotools filter module.
     */    
    private class FilterPair {
	/** half of the filter pair */
	private Filter supported;
	/** the other half */
	private Filter unSupported;
	
	/**
	 * Constructor takes the two filters of the pair.
	 *
	 * @param supported The filter that can be encoded.
	 * @param unSupported the filter that can't be encoded.
	 */
	public FilterPair(Filter supported, Filter unSupported){
	    this.supported = supported;
	    this.unSupported = unSupported;
	}
	
	/** 
	 * Accessor method.
	 *
	 *@return the supported Filter.
	 */
	public Filter getSupported(){
	    return supported;
	}
	
	/** 
	 * Accessor method.
	 *
	 *@return the unSupported Filter.
	 */
	public Filter getUnSupported(){
	    return unSupported;
	}
    }
    
}
