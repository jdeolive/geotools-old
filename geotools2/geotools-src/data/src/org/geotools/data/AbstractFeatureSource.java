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
package org.geotools.data;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.filter.Filter;
import java.io.IOException;


/**
 * This is a starting point for providing your own FeatureSource
 * implementation.
 * 
 * <p>
 * Subclasses must implement:
 * </p>
 * 
 * <ul>
 * <li>
 * getDataStore()
 * </li>
 * <li>
 * getSchema()
 * </li>
 * <li>
 * addFeatureListener()
 * </li>
 * <li>
 * removeFeatureListener()
 * </li>
 * </ul>
 * 
 * <p>
 * You may find a FeatureSource implementations that is more specific to your
 * needs - such as JDBCFeatureSource.
 * </p>
 * 
 * <p>
 * For an example of this class customized for use please see MemoryDataStore.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc
 */
public abstract class AbstractFeatureSource implements FeatureSource {
    /**
     * Retrieve the Transaction this FeatureSource is opperating against.
     * 
     * <p>
     * For a plain FeatureSource that cannot modify this will always be
     * Transaction.AUTO_COMMIT.
     * </p>
     *
     * @return Transacstion FeatureSource is opperating against
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }

    /**
     * Provides an interface to for the Resutls of a Query.
     * 
     * <p>
     * Various queries can be made against the results, the most basic being to
     * retrieve Features.
     * </p>
     *
     * @param query
     *
     * @return
     *
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureResults getFeatures(Query query) {
        return new DefaultFeatureResults(this, query);
    }

    /**
     * Retrieve all Feature matching the Filter.
     *
     * @param filter Indicates features to retrieve
     *
     * @return FeatureResults indicating features matching filter
     *
     * @throws IOException If results could not be obtained
     */
    public FeatureResults getFeatures(Filter filter) throws IOException {
        return getFeatures( new DefaultQuery(getSchema().getTypeName(), filter ));
    }

    /**
     * Retrieve all Features.
     *
     * @return FeatureResults of all Features in FeatureSource
     *
     * @throws IOException If features could not be obtained
     */
    public FeatureResults getFeatures() throws IOException {
        return getFeatures(Filter.NONE);
    }

    /**
     * Retrieve Bounds of all Features.
     * 
     * <p>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @return null representing the lack of an optimization
     */
    public Envelope getBounds() {
        return getBounds(Query.ALL);
    }

    /**
     * Retrieve Bounds of Query results.
     * 
     * <p>
     * Currently returns null, consider getFeatures( query ).getBounds()
     * instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the bounds of
     *
     * @return null representing the lack of an optimization
     */
    public Envelope getBounds(Query query) {
        if (query.getFilter() == Filter.ALL) {
            return new Envelope();
        }

        return null; // to expensive right now :-)
    }

    /**
     * Retrieve total number of Query results.
     * 
     * <p>
     * Currently returns -1, consider getFeatures( query ).getCount() instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the count of
     *
     * @return -1 representing the lack of an optimization
     */
    public int getCount(Query query) {
        if (query.getFilter() == Filter.ALL) {
            return 0;
        }

        return -1; // too expensive right now :-)
    }
}
