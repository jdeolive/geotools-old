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
 * @version $Id: MemoryDataSource.java,v 1.5 2003/03/28 19:17:26 cholmesny Exp $
 * @author James Macgill, CCG
 * @author Ian Turton, CCG
 */

import java.util.Set;
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
public class MemoryDataSource implements DataSource { 
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
    public void addFeature(Feature f){
        features.addElement(f);
        bbox.expandToInclude(f.getDefaultGeometry().getEnvelopeInternal());
    }
    
    /** 
     * Stops this DataSource from loading.
     * @task TODO: implement abort loading (if needed)
     */
    public void abortLoading() {
    }
    
   
    /** 
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: Implement addFeatures method
     */
    public Set addFeatures(FeatureCollection collection) throws DataSourceException {
         throw new DataSourceException("Removal of features is not yet supported by this datasource");
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
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox(boolean speed) {
        return getBbox();
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
       FeatureCollection fc = new FeatureCollectionDefault();
       getFeatures(fc,filter);
       return fc;
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {
         for (int i = 0; i < features.size(); i++){
            Feature f = (Feature) features.elementAt(i);
            if (filter.contains(f)){
                collection.addFeatures(new Feature[]{f});
            }
        }
    }
    
        /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     * @task TODO: Implement support for modification of features (single attribute)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     * @task TODO: Implement support for modification of feature (multi attribute)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     * @task TODO: Implement support for removal of features
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
      /**
     * Begins a transaction(add, remove or modify) that does not commit as 
     * each modification call is made.  If an error occurs during a transaction
     * after this method has been called then the datasource should rollback: 
     * none of the transactions performed after this method was called should
     * go through.
     */
    public void startMultiTransaction() throws DataSourceException{
	throw new DataSourceException("multi transactions not supported");
    }

    /**
     * Ends a transaction after startMultiTransaction has been called.  Similar
     * to a commit call in sql, it finalizes all of the transactions called
     * after a startMultiTransaction.
     */
    public void endMultiTransaction() throws DataSourceException {
	throw new DataSourceException("multi transactions not supported");
    }
    /**************************************************
      Data source utility methods.
     **************************************************/

    /**
     * Gets the DatasSourceMetaData object associated with this datasource.  
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the DataSourceMetaData
     * about which methods the datasource supports.
     */
    public DataSourceMetaData getMetaData(){
	return new DataSourceMetaData() {
		public boolean supportsTransactions(){ return false; }
		public boolean supportsMultiTransactions(){ return false; }
		public boolean supportsSetFeatures(){return false;}
		public boolean supportsSetSchema(){return false;}
		public boolean supportsAbort(){return false;}
		public boolean supportsGetBbox(){return true;}
	    };
    }
	    
    /**
     * Deletes the all the current Features of this datasource and adds the
     * new collection.  Primarily used as a convenience method for file 
     * datasources.  
     * @param collection - the collection to be written
     */
    public void setFeatures(FeatureCollection collection) throws DataSourceException{
	throw new DataSourceException("set feature not supported");
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks HACK: we never type check to make sure all the features are
     * of the same type, so this will only return the first feature's
     * schema.  Should this datasource allow features of different types?
     */
    public FeatureType getSchema(){
	FeatureType featureType = null;
	//if (features.size() > 0) {
	//    Feature f = (Feature) features.elementAt(0);
	//    featureType = f.getSchema();
	//}
	return featureType;
    }

    /**
     * Sets the schema that features extrated from this datasource will be 
     * created with.  This allows the user to obtain the attributes he wants,
     * by calling getSchema and then creating a new schema using the 
     * attributeTypes from the currently used schema.  
     * @param schema the new schema to be used to create features.
     */
    public void setSchema(FeatureType schema) throws DataSourceException {
	throw new DataSourceException("set schema method not supported");
    }

}
