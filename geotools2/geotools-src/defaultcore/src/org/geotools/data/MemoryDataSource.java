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
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A special datasource implementation which provides access to features
 * stored in memory.
 * 
 * <p>Unlike other datasources, it is not connected to any external
 * resources.  Instead, all of the features  it provides are stored
 * internally.
 * <p>
 * It is very useful for testing and development when a datasource with only a
 * limited number of features is required.  It may also be adapted in future
 * to act as a cache for other datasources.
 * </p>
 * <p>
 * The origional MemoryDataSource has been patched:</p>
 * <ul>
 * <li>To allow subclassing operations have fields have been made protected
 *     </li>
 * <li>For correct testing clones of the the stored Features are returned
 *     </li>
 * <li>To allow transaction support operations have been recast in terms
 *     of getFeaturesList()
 *     </li> 
 * </ul>
 * 
 * Known Bugs:
 * <ul>
 * <li>Assumes a consistent FeatureType 
 *     </li>
 * </ul>
 * @author James Macgill, CCG
 * @author Ian Turton, CCG
 * @author jgarnett, Refractions Research 
 * @version $Id: MemoryDataSource.java,v 1.11 2003/09/22 17:48:49 cholmesny Exp $
 */
public class MemoryDataSource extends AbstractDataSource implements DataSource {
    
    /** The envelope of the geometries of this datasource. */
    protected Envelope bbox = new Envelope();

    /** The feature store. */
    protected java.util.List features = new java.util.Vector();

    /**
     * Creates a new instance of MemoryDataSource.
     */
    public MemoryDataSource() {
    }
    /**
     * Provides access to the list of Features this DataSource represents.
     * <p>
     * Overriding this method allows subclasses control the functionality
     * of this DataSource. 
     * </p>
     * <p>
     * This list should only be valid for the duration of a transaction and
     * should <b>not</b> be returned to client code! A transaction may commit
     * rollback the List returned by this function.
     * </p>
     * <p>
     * I belive the "correct" thing to do would be to serve up a List from
     * a static repository of named FeatureTypes.
     * </p>
     * @return
     */
    protected List getFeaturesList(){
        return features;
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
       Feature newFeature;
            try {
                newFeature = feature.getFeatureType().duplicate( feature );
            } catch (IllegalAttributeException e) {
                // Warning?
                newFeature = feature;
            }
        getFeaturesList().add(newFeature);

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
        // I am unclear what happens when a feature is added twice?
        Set added = new HashSet();

        for (FeatureIterator i = collection.features(); i.hasNext();) {
            // need to make a copy - now done in addFeature
            Feature feature = i.next();
            added.add( addFeature( feature ) );
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
        List features = getFeaturesList();
        for (int i = 0; (i < features.size()) && (i < query.getMaxFeatures());
                i++) {
            Feature feature = (Feature) features.get(i);            
            if (filter.contains(feature)) {
                try {
                    collection.add(feature.getFeatureType().duplicate(feature) );
                } catch (IllegalAttributeException e) {
                    // If you are having trouble with this method
                    // you will need to extend FeatureTypes.copyFeature( Feature )
                    // to handle creating a duplicate of your attribute.
                    //
                    throw new DataSourceException( "Could not duplicate "+feature.getID(), e );                    
                }
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
