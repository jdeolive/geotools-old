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
package org.geotools.data;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A special datasource implementation which provides access to features
 * stored in memory.  Unlike other datasources, it is not connected to any
 * external resources.  Instead, all of the features  it provides are stored
 * internally.
 * 
 * <p>
 * It is very useful for testing and development when a datasource with only a
 * limited number of features is required.  It may also be adapted in future
 * to act as a cache for other datasources.
 * </p>
 *
 * @author James Macgill, CCG
 * @author Ian Turton, CCG
 * @version $Id: MemoryDataSource.java,v 1.9 2003/07/22 15:26:45 cholmesny Exp $
 */
public class MemoryDataSource extends AbstractDataSource implements DataSource {
    /** The envelope of the geometries of this datasource. */
    private Envelope bbox = new Envelope();

    /** The feature store. */
    private java.util.List features = new java.util.Vector();

    /**
     * Creates a new instance of MemoryDataSource.
     */
    public MemoryDataSource() {
    }

    /**
     * Adds a new feature to the list of those stored within the datasource.
     * The default geometry of the feature will be used to extend the
     * bounding box of the datasource. Note, this is specific to
     * MemoryDataSource and should not be confused with addFeatures in the
     * DataSource interface.
     *
     * @param feature The feature to add
     *
     * @return The id of the feature added.
     */
    public String addFeature(Feature feature) {
        features.add(feature);

        Envelope internal = feature.getDefaultGeometry().getEnvelopeInternal();
        bbox.expandToInclude(internal);

        return feature.getID();
    }

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     *
     * @return a set of id strings of the features added.
     *
     * @throws DataSourceException If anything goes wrong or if exporting is
     *         not supported.
     *
     * @task TODO: test this.
     */
    public Set addFeatures(FeatureCollection collection)
        throws DataSourceException {
        Set added = new HashSet();

        for (Iterator i = collection.iterator(); i.hasNext();) {
            added.add(addFeature((Feature) i.next()));
        }

        return added;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     */
    public Envelope getBbox() {
        return bbox;
    }

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this
     * method at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param query An OpenGIS filter; specifies which features to retrieve.
     *
     * @throws DataSourceException For all data source errors.
     *
     * @task TODO: use the query object more effectively, like maxFeatures,
     *       typename and properties elements if they exist.
     */
    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        Filter filter = null;

        if (query != null) {
            filter = query.getFilter();
        }

        for (int i = 0; (i < features.size()) && (i < query.getMaxFeatures());
                i++) {
            Feature feature = (Feature) features.get(i);

            if (filter.contains(feature)) {
                collection.add(feature);
            }
        }
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return The featureType of features contained in this schema.
     *
     * @throws DataSourceException never thrown.
     *
     * @task HACK: we never type check to make sure all the features are of
     *       the same type, so this will only return the first feature's
     *       schema. Should this datasource allow features of different
     *       types?
     * @task REVISIT: most of this method was commented out, I've put the
     *       lines back in, but I would like to know the reason they were
     *       taken out.
     */
    public FeatureType getSchema() throws DataSourceException {
        FeatureType featureType = null;

        if (features.size() > 0) {
            Feature feature = (Feature) features.get(0);
            featureType = feature.getFeatureType();
        }

        return featureType;
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that
     * clients recieve the proper information about the datasource's
     * capabilities.
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport memMeta = new MetaDataSupport();
        memMeta.setSupportsAdd(true);
        memMeta.setFastBbox(true);

        return memMeta;
    }
}
