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
import java.util.*;
import java.util.logging.Logger;


/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: LogicSAXParser.java,v 1.1 2002/10/23 15:32:23 ianturton Exp $
 * @author Rob Hranac, Vision for New York
 */
public class LogicSAXParser {

    /**
     * The logger for the filter module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** The (limited) REGEXP pattern. */
    private short logicType = -1;

    /** The (limited) REGEXP pattern. */
    private List subFilters = new ArrayList();

    /** The (limited) REGEXP pattern. */
    private LogicSAXParser logicFactory = null;


    private boolean isActive = false;

    private boolean isComplete = false;


    /**
     * Constructor which flags the operator as between.
     */
    public LogicSAXParser () {
        LOGGER.finer("made new logic factory");
    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void start(short logicType)
        throws IllegalFilterException {

        LOGGER.finer("got a start element: " + logicType);
        if( this.logicType != -1) {
            logicFactory = new LogicSAXParser();
            logicFactory.start(logicType);
        }
        else if( !AbstractFilter.isLogicFilter(logicType)) {
            throw new IllegalFilterException
                ("Add logic filter type does not match declared type.");
        }
        else {
            this.logicType = logicType;
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void end(short logicType)
        throws IllegalFilterException {
        
        LOGGER.finer("got an end element: " + logicType);
        if( logicFactory != null) {            

            LOGGER.finer("sending end element to nested logic filter: " + logicType);
            logicFactory.end(logicType);
            if( logicFactory.isComplete()) {            
                subFilters.add( logicFactory.create());
                logicFactory = null;
            }
        }
        else if( this.logicType == logicType ) {
            LOGGER.finer("end element matched internal type: " + this.logicType);
            isComplete = true;            
        }
        else {
            throw new IllegalFilterException
                ("Logic Factory got an end message that it can't process.");
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param logicType The limited REGEXP pattern for this string. 
     */
    public void add(Filter filter) {

        LOGGER.finer("added a filter: " + filter.toString());
        if( logicFactory != null) {            
            LOGGER.finer("adding to nested logic filter: " + filter.toString());
            logicFactory.add(filter);
        }
        else {
            LOGGER.finer("added to sub filters: " + filter.toString());
            subFilters.add(filter);
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public Filter create()
        throws IllegalFilterException {

        LogicFilter filter = null;

        LOGGER.finer("creating a logic filter");
        if( isComplete()) {
            LOGGER.finer("filter is complete, with type: " + this.logicType);
            if( logicType == AbstractFilter.LOGIC_NOT) {
                filter = new LogicFilter((Filter) subFilters.get(0), this.logicType);                 
            }
            else {
                filter = new LogicFilter(this.logicType); 
                Iterator iterator = subFilters.iterator();
                while( iterator.hasNext()) {
                    filter.addFilter( (Filter) iterator.next());
                }
            }
            return filter;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to generate incomplete logic filter.");            
        }
    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public boolean isComplete() {
        return isComplete;
    }

    
}
