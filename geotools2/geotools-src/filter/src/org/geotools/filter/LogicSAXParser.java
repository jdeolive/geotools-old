/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

import java.util.ArrayList;
import java.util.Iterator;

// J2SE dependencies
import java.util.List;
import java.util.logging.Logger;


/**
 * Processes messages from clients to create Logic Filters.  Handles nested
 * logic filters.  Filters should call start and end when they reach logic
 * filters, and create when the filter is complete.
 *
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 * @version $Id: LogicSAXParser.java,v 1.5 2003/05/14 16:07:42 cholmesny Exp $
 */
public class LogicSAXParser {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();

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
    public LogicSAXParser() {
        LOGGER.finer("made new logic factory");
    }

    /**
     * To be called by a parser to start the creation of a logic filter. Can
     * start a nested or a base logic filter.
     *
     * @param logicType OR, or AND abstract filter type.
     *
     * @throws IllegalFilterException if filter type does not match  declared
     *         type.
     */
    public void start(short logicType) throws IllegalFilterException {
        LOGGER.finest("got a start element: " + logicType);

        if (this.logicType != -1) {
            logicFactory = new LogicSAXParser();
            logicFactory.start(logicType);
        } else if (!AbstractFilter.isLogicFilter(logicType)) {
            throw new IllegalFilterException(
                "Add logic filter type does not match declared type.");
        } else {
            this.logicType = logicType;
        }
    }

    /**
     * To be called when the sax parser reaches the end of a logic filter.
     * Tells this class to complete.
     *
     * @param logicType the Filter type.
     *
     * @throws IllegalFilterException If the end message can't be processed in
     *         this state.
     */
    public void end(short logicType) throws IllegalFilterException {
        LOGGER.finer("got an end element: " + logicType);

        if (logicFactory != null) {
            LOGGER.finer("sending end element to nested logic filter: " +
                logicType);
            logicFactory.end(logicType);

            if (logicFactory.isComplete()) {
                subFilters.add(logicFactory.create());
                logicFactory = null;
            }
        } else if (this.logicType == logicType) {
            LOGGER.finer("end element matched internal type: " +
                this.logicType);
            isComplete = true;
        } else {
            throw new IllegalFilterException(
                "Logic Factory got an end message that it can't process.");
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param filter The limited REGEXP pattern for this string.
     */
    public void add(Filter filter) {
        LOGGER.finer("added a filter: " + filter.toString());

        if (logicFactory != null) {
            LOGGER.finer("adding to nested logic filter: " + filter.toString());
            logicFactory.add(filter);
        } else {
            LOGGER.finer("added to sub filters: " + filter.toString());
            subFilters.add(filter);
        }
    }

    /**
     * Creates the the logic filter if in a complete state.
     *
     * @return The created logic filter.
     *
     * @throws IllegalFilterException if the filter is not complete.
     */
    public Filter create() throws IllegalFilterException {
        LogicFilter filter = null;

        LOGGER.finer("creating a logic filter");

        if (isComplete()) {
            LOGGER.finer("filter is complete, with type: " + this.logicType);

            if (logicType == AbstractFilter.LOGIC_NOT) {
                filter = filterFactory.createLogicFilter((Filter) subFilters.get(
                            0), this.logicType);
            } else {
                filter = filterFactory.createLogicFilter(this.logicType);

                Iterator iterator = subFilters.iterator();

                while (iterator.hasNext()) {
                    filter.addFilter((Filter) iterator.next());
                }
            }

            //reset the variables so it works right if called again.
            subFilters = new ArrayList();
            this.logicType = -1;
            isComplete = false;

            return filter;
        } else {
            throw new IllegalFilterException(
                "Attempted to generate incomplete logic filter.");
        }
    }

    /**
     * indicates if the logic filter is complete.
     *
     * @return <tt>true</tt> if this holds a complete logic filter to be
     *         created, <tt>false</tt> otherwise.
     */
    public boolean isComplete() {
        return isComplete;
    }
}
