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
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import java.io.IOException;


/**
 * Description
 * 
 * <p>
 * Details
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class DefaultFeatureResults implements FeatureResults {
    protected Query query;
    protected FeatureSource featureSource;

    /**
     * FeatureResults query against featureSource.
     * 
     * <p>
     * Please note that is object will not be valid after the transaction has
     * closed.
     * </p>
     *
     * @param source
     * @param query
     */
    public DefaultFeatureResults(FeatureSource source, Query query) {
        this.query = query;
        this.featureSource = source;
    }

    /**
     * FeatureSchema for provided query.
     * 
     * <p>
     * If query.retrieveAllProperties() is <code>true</code> the FeatureSource
     * getSchema() will be returned.
     * </p>
     * 
     * <p>
     * If query.getPropertyNames() is used to limit the result of the Query a
     * sub type will be returned based on FeatureSource.getSchema().
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public FeatureType getSchema() throws IOException {
        if (query.retrieveAllProperties()) {
            return featureSource.getSchema();
        } else {
            try {
                return DataUtilities.createSubType(featureSource.getSchema(),
                    query.getPropertyNames());
            } catch (SchemaException e) {
                throw new DataSourceException("Could not create schema", e);
            }
        }
    }

    /**
     * Returns transaction from featureSource (if it is a FeatureStore), or
     * Transaction.AUTO_COMMIT if it is not.
     *
     * @return Transacstion this FeatureResults opperates against
     */
    protected Transaction getTransaction() {
        if (featureSource instanceof FeatureStore) {
            FeatureStore featureStore = (FeatureStore) featureSource;

            return featureStore.getTransaction();
        } else {
            return Transaction.AUTO_COMMIT;
        }
    }

    /**
     * Retrieve a FeatureReader for this Query
     *
     * @return FeatureReader for this Query
     *
     * @throws IOException If results could not be obtained
     */
    public FeatureReader reader() throws IOException {
        FeatureReader reader = featureSource.getDataStore().getFeatureReader(getSchema(),
                query.getFilter(), getTransaction());
        int maxFeatures = query.getMaxFeatures();

        if (maxFeatures == query.DEFAULT_MAX) {
            return reader;
        } else {
            return new MaxFeatureReader(reader, maxFeatures);
        }
    }

    /**
     * Returns the bounding box of this FeatureResults
     * 
     * <p>
     * This implementation will generate the correct results from reader() if
     * the provided FeatureSource does not provide an optimized result via
     * FeatureSource.getBounds( Query ).
     * </p>
     * If the feature has no geometry, then an empty envelope is returned.
     *
     * @return
     *
     * @throws IOException If bounds could not be obtained
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() throws IOException {
        Envelope bounds;

        bounds = featureSource.getBounds(query);

        if (bounds != null) {
            return bounds;
        }

        try {
            Feature feature;
            bounds = new Envelope();

            FeatureReader reader = reader();

            //if (reader.getFeatureType().getDefaultGeometry() == null) {
            //    throw new IOException("No default Geometry specified");
            //}
            while (reader.hasNext()) {
                feature = reader.next();
                bounds.expandToInclude(feature.getBounds());
            }

            reader.close();

            return bounds;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not read feature ", e);
        }
    }

    /**
     * Number of Features in this query.
     * 
     * <p>
     * This implementation will generate the correct results from reader() if
     * the provided FeatureSource does not provide an optimized result via
     * FeatureSource.getCount( Query ).
     * </p>
     *
     * @return
     *
     * @throws IOException If feature could not be read
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureResults#getCount()
     */
    public int getCount() throws IOException {
        int count;
        count = featureSource.getCount(query);

        if (count != -1) {
            // we have an optimization!
            return count;
        }

        // Okay lets count the FeatureReader
        try {
            count = 0;

            FeatureReader reader = reader();

            for (; reader.hasNext(); count++) {
                reader.next();
            }

            reader.close();

            return count;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not read feature ", e);
        }
    }

    public FeatureCollection collection() throws IOException {
        try {
            FeatureCollection collection = FeatureCollections.newCollection();
            Feature feature;
            FeatureReader reader = reader();

            while (reader.hasNext()) {
                collection.add(reader.next());
            }

            reader.close();

            return collection;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not read feature ", e);
        }
    }
}
