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

/**
 * A memory-based datasource.
 *
 * @version $Id: MemoryDataSource.java,v 1.8 2003/07/17 07:09:53 ianschneider Exp $
 * @author James Macgill, CCG
 * @author Ian Turton, CCG
 */

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.geotools.feature.*;
import org.geotools.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

/**
 * A special datasource implementation which provides access to
 * features stored in memory.  Unlike other datasources, it is not
 * connected to any external resource.  Instead, all of the features 
 * it provides are stored internally.
 * It is very useful for testing and development when a datasource
 * with only a limited number of features is required.  It may also be
 * adapted in future to act as a cache for other datasources.
 */
public class MemoryDataSource extends AbstractDataSource 
    implements DataSource { 
    Envelope bbox = new Envelope();
    /**
     * Creates a new instance of MemoryDataSource.
     */
    public MemoryDataSource() {
    }
    
    /**
     * The feature store.
     */
    private java.util.Vector features = new java.util.Vector();
    
    /**
     * Adds a new feature to the list of those stored within the datasource.
     * The default geometry of the feature will be used to extend the bounding
     * box of the datasource.
     *
     * Note, this is specific to MemoryDataSource and should not be confused with
     * addFeatures in the DataSource interface.
     *
     * @param f The feature to add 
     */
    public String addFeature(Feature f){
        features.addElement(f);
        bbox.expandToInclude(f.getDefaultGeometry().getEnvelopeInternal());
	return f.getID();
    }

    
    /** 
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: test this.
     */
    public Set addFeatures(FeatureCollection collection) throws DataSourceException {

	Set addedFeatures = new HashSet();
	for (Iterator i = collection.iterator(); i.hasNext();){
	    addedFeatures.add(addFeature((Feature)i.next()));
	}
	return addedFeatures;
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox() {
        return bbox;
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     * @task TODO: use the query object more effectively, like maxFeatures, 
     * typename and properties elements if they exist.
     */
    public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
	Filter filter = null;
	if (query != null) {
	    filter = query.getFilter();
	}
         for (int i = 0; i < features.size() && i < query.getMaxFeatures(); i++){
            Feature f = (Feature) features.elementAt(i);
            if (filter.contains(f)){
                collection.add(f);
            }
        }
    }
    
    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks HACK: we never type check to make sure all the features are
     * of the same type, so this will only return the first feature's
     * schema.  Should this datasource allow features of different types?
     * @task REVISIT: most of this method was commented out, I've put the lines
     * back in, but I would like to know the reason they were taken out.
     */
    public FeatureType getSchema() throws DataSourceException {
	FeatureType featureType = null;
	if (features.size() > 0) {
	    Feature f = (Feature) features.elementAt(0);
	    featureType = f.getFeatureType();
	}
	return featureType;
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.  <p>
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
