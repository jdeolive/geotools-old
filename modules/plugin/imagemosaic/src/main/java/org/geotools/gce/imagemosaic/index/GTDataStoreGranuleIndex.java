/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.gce.imagemosaic.index;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.feature.visitor.FeatureCalc;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;

/**
 * This class simply builds an SRTREE spatial index in memory for fast indexed
 * geometric queries.
 * 
 * <p>
 * Since the {@link ImageMosaicReader} heavily uses spatial queries to find out
 * which are the involved tiles during mosaic creation, it is better to do some
 * caching and keep the index in memory as much as possible, hence we came up
 * with this index.
 * 
 * @author Simone Giannecchini, S.A.S.
 * @author Stefan Alfons Krueger (alfonx), Wikisquare.de : Support for jar:file:foo.jar/bar.properties URLs
 * @since 2.5
 *
	 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/plugin/imagemosaic/src/main/java/org/geotools/gce/imagemosaic/RasterManager.java $
 */
class GTDataStoreGranuleIndex implements GranuleIndex {
	
	/** Logger. */
	final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(GTDataStoreGranuleIndex.class);

	final static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
	
	/**
	 * Extracts a bbox from a filter in case there is at least one.
	 * 
	 * I am simply looking for the BBOX filter but I am sure we could
	 * use other filters as well. I will leave this as a todo for the moment.
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS.
	 * @todo TODO use other spatial filters as well
	 */
	@SuppressWarnings("deprecation")
	static class BBOXFilterExtractor extends DefaultFilterVisitor{

		public ReferencedEnvelope getBBox() {
			return bbox;
		}
		private ReferencedEnvelope bbox;
		@Override
		public Object visit(BBOX filter, Object data) {
			final ReferencedEnvelope bbox= new ReferencedEnvelope(
					filter.getMinX(),
					filter.getMinY(),
					filter.getMaxX(),
					filter.getMaxY(),
					null);
			if(this.bbox!=null)
				this.bbox=(ReferencedEnvelope) this.bbox.intersection(bbox);
			else
				this.bbox=bbox;
			return super.visit(filter, data);
		}
		
	}
	private DataStore tileIndexStore;

	private String typeName;

	private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

	private String geometryPropertyName;

	private ReferencedEnvelope bounds;

	private DataStoreFactorySpi spi;


	public GTDataStoreGranuleIndex(
			final Map<String, Serializable> params, 
			final boolean create, 
			final DataStoreFactorySpi spi) {
		Utils.ensureNonNull("params",params);
		Utils.ensureNonNull("spi",spi);
		this.spi=spi;
		try{
		
			// creating a store, this might imply creating it for an existing underlying store or 
			// creating a brand new one
			if(!create)
				tileIndexStore =spi.createDataStore(params);
			else
			{
				// this works only with the shapefile datastore, not with the others
				// therefore I try to catch the error to try and use themethdo without *New*
				try{
					tileIndexStore =  spi.createNewDataStore(params);
				}catch (UnsupportedOperationException e) {
					tileIndexStore =  spi.createDataStore(params);
				}
			}

			
			// is this a new store? If so we do not set any properties
			if(create)
				return;
				
			// if this is not a new store let's extract basic properties from it
			extractBasicProperties();			
		}
		catch (Throwable e) {
			try {
				if(tileIndexStore!=null)
					tileIndexStore.dispose();
			} catch (Throwable e1) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e1.getLocalizedMessage(), e1);
			}
			finally{
				tileIndexStore=null;
			}	

			throw new  IllegalArgumentException(e);
		}
		
	}

	/**
	 * If the underlying store has been disposed we throw an {@link IllegalStateException}.
	 * <p>
	 * We need to arrive here with at least a read lock!
	 * @throws IllegalStateException in case the underlying store has been disposed. 
	 */
	private void checkStore()throws IllegalStateException{
		if(tileIndexStore==null)
			throw new IllegalStateException("The index sore has been disposed already.");
	}
	private void extractBasicProperties() throws IOException {
		final String[] typeNames = tileIndexStore.getTypeNames();
		if (typeNames.length <= 0)
			throw new IllegalArgumentException("Problems when opening the index, no typenames for the schema are defined");

		// loading all the features into memory to build an in-memory index.
		typeName = typeNames[0];
		
		featureSource = tileIndexStore.getFeatureSource(typeName);
		if (featureSource == null) 
			throw new NullPointerException(
					"The provided FeatureSource<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");
		bounds=featureSource.getBounds();
		
		
		final FeatureType schema = featureSource.getSchema();
		geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
	}
	
	private final ReadWriteLock rwLock= new ReentrantReadWriteLock(true);

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope)
	 */
	public List<SimpleFeature> getGranules(final BoundingBox envelope) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		final DefaultQuery q= new DefaultQuery(typeName);
		Filter filter = ff.bbox( ff.property( geometryPropertyName ), ReferencedEnvelope.reference(envelope) );
		q.setFilter(filter);
	    return getGranules(q);	
		
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope, com.vividsolutions.jts.index.ItemVisitor)
	 */
	public void  getGranules(final BoundingBox envelope, final GranuleIndexVisitor visitor) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		final DefaultQuery q= new DefaultQuery(typeName);
		Filter filter = ff.bbox( ff.property( geometryPropertyName ), ReferencedEnvelope.reference(envelope) );
		q.setFilter(filter);
	    getGranules(q,visitor);			
		

	}

	public void dispose() {
		final Lock l=rwLock.writeLock();
		try{
			l.lock();
			try {
				if(tileIndexStore!=null)
					tileIndexStore.dispose();
			} catch (Throwable e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
			finally{
				tileIndexStore=null;
			}	
						
		}finally{
			
			l.unlock();
		
		}
		
		
	}

	public int removeGranules(final Query query) {
		Utils.ensureNonNull("query",query);
		final Lock lock=rwLock.writeLock();
		try{
			lock.lock();
			// check if the index has been cleared
			checkStore();		
			
			FeatureStore<SimpleFeatureType, SimpleFeature> fs=null;
			try{
				// create a writer that appends this features
				fs = (FeatureStore<SimpleFeatureType, SimpleFeature>) tileIndexStore.getFeatureSource(typeName);
				final int retVal=fs.getCount(query);
				fs.removeFeatures(query.getFilter());
				
				//update bounds
				bounds=tileIndexStore.getFeatureSource(typeName).getBounds();
				
				return retVal;
				
			}
			catch (Throwable e) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
				return -1;
			}
			// do your thing
		}finally{
			lock.unlock();
		}			
	}

	public void addGranule(final SimpleFeature granule, final Transaction transaction) throws IOException {
		addGranules(Collections.singleton(granule),transaction);
	}
	
	public void addGranules(final Collection<SimpleFeature> granules, final Transaction transaction) throws IOException {
		Utils.ensureNonNull("granuleMetadata",granules);
		final Lock lock=rwLock.writeLock();
		try{
			lock.lock();
			// check if the index has been cleared
			checkStore();
			
			
			FeatureWriter<SimpleFeatureType, SimpleFeature> fw =null;
			try{
				// create a writer that appends this features
				fw = tileIndexStore.getFeatureWriterAppend(typeName,transaction);

				//add them all
				for(SimpleFeature f:granules){
					
					// create a new feature
					final SimpleFeature feature = fw.next();
					
					// get attributes and copy them over
					for(int i=f.getAttributeCount()-1;i>=0;i--){
						Object attribute = f.getAttribute(i);
						
						
						// special case for postgis
						if(spi instanceof PostgisNGJNDIDataStoreFactory||spi instanceof PostgisNGDataStoreFactory)
						{
							final AttributeDescriptor descriptor = tileIndexStore.getSchema(typeName).getDescriptor(i);
							if(descriptor.getType().getBinding().equals(String.class))
							{
								// escape the string correctly
								attribute=((String) attribute).replace("\\", "\\\\");
							}
							
						}
						
						feature.setAttribute(i, attribute);
					}
					
					//write down
					fw.write();

				}
			}
			catch (Throwable e) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
			}finally{
				if(fw!=null)
					fw.close();
			}
			
			// do your thing
			
			//update bounds
			bounds=tileIndexStore.getFeatureSource(typeName).getBounds();
		}finally{
			lock.unlock();
		}	
		
	}

	public void  getGranules(final Query q,final GranuleIndexVisitor visitor)
	throws IOException {
		Utils.ensureNonNull("q",q);

		FeatureCollection<SimpleFeatureType, SimpleFeature> features=null;
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();		
			checkStore();
			
			//
			// Load tiles informations, especially the bounds, which will be
			// reused
			//
			features = featureSource.getFeatures( q );
		
			if (features == null) 
				throw new NullPointerException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");
	
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Index Loaded");
						
			
			//load the feature from the shapefile and create JTS index
			if (features.size()<=0) 
				throw new IllegalArgumentException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  is empty, it's impossible to create an index!");
			
			features.accepts( new AbstractFeatureVisitor(){
			    public void visit( Feature feature ) {
			        if(feature instanceof SimpleFeature)
			        {
			        	final SimpleFeature sf= (SimpleFeature) feature;
			        	visitor.visit(sf, null);
			        }
			    }            
			}, new NullProgressListener() );

		}
		catch (Throwable e) {
			throw new  IllegalArgumentException(e);
		}
		finally{
			lock.unlock();

		}
				
		
	}

	public List<SimpleFeature> getGranules(final Query q) throws IOException {
		Utils.ensureNonNull("q",q);

		FeatureIterator<SimpleFeature> it=null;
		FeatureCollection<SimpleFeatureType, SimpleFeature> features=null;
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();		
			checkStore();
			
			//
			// Load tiles informations, especially the bounds, which will be
			// reused
			//
			features = featureSource.getFeatures( q );
		
			if (features == null) 
				throw new NullPointerException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");
	
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Index Loaded");
						
			
			//load the feature from the shapefile and create JTS index
			it = features.features();
			if (!it.hasNext()) 
				throw new IllegalArgumentException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  or empty, it's impossible to create an index!");
			
			// now build the index
			// TODO make it configurable as far the index is involved
			final ArrayList<SimpleFeature> retVal= new ArrayList<SimpleFeature>(features.size());
			while (it.hasNext()) {
				final SimpleFeature feature = it.next();
				retVal.add(feature);
			}
			return retVal;

		}
		catch (Throwable e) {
			throw new  IllegalArgumentException(e);
		}
		finally{
			lock.unlock();
			if(it!=null)
				// closing he iterator to free some resources.
				if(features!=null)
					features.close(it);

		}
	}

	public Collection<SimpleFeature> getGranules()throws IOException {
		return getGranules(getBounds());
	}

	public BoundingBox getBounds() {
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			checkStore();
				
			return bounds;
		}finally{
			lock.unlock();
		}
	}

	public void createType(String namespace, String typeName, String typeSpec) throws IOException, SchemaException {
		Utils.ensureNonNull("typeName",typeName);
		Utils.ensureNonNull("typeSpec",typeSpec);
		final Lock lock=rwLock.writeLock();
		try{
			lock.lock();
			checkStore();
			
			final SimpleFeatureType featureType= DataUtilities.createType(namespace, typeName, typeSpec);
			tileIndexStore.createSchema(featureType);
			extractBasicProperties();
			
		}finally{
			lock.unlock();
		}			

		
	}

	public void createType(SimpleFeatureType featureType) throws IOException {
		Utils.ensureNonNull("featureType",featureType);
		final Lock lock=rwLock.writeLock();
		try{
			lock.lock();
			checkStore();

			tileIndexStore.createSchema(featureType);
			extractBasicProperties();
			
		}finally{
			lock.unlock();
		}				
		
	}

	public void createType(String identification, String typeSpec) throws SchemaException, IOException {
		Utils.ensureNonNull("typeSpec",typeSpec);
		Utils.ensureNonNull("identification",identification);
		final Lock lock=rwLock.writeLock();
		try{
			lock.lock();
			checkStore();
			final SimpleFeatureType featureType= DataUtilities.createType(identification, typeSpec);
			tileIndexStore.createSchema(featureType);
			extractBasicProperties();
			
		}finally{
			lock.unlock();
		}			
		
	}

	public SimpleFeatureType getType() throws IOException {
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			checkStore();
			
			return tileIndexStore.getSchema(typeName);
		}finally{
			lock.unlock();
		}			
		
	}

	public void computeAggregateFunction(Query query, FeatureCalc function) throws IOException {
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			checkStore();
			FeatureSource<SimpleFeatureType, SimpleFeature> fs = tileIndexStore.getFeatureSource(tileIndexStore.getTypeNames()[0]);
				
			if(fs instanceof ContentFeatureSource)
				((ContentFeatureSource)fs).accepts(query, function, null);
			else
			{
				final FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fs.getFeatures(query);
				collection.accepts(function, null);
				
			}
		}finally{
			lock.unlock();
		}		
		
	}

	public QueryCapabilities getQueryCapabilities() {
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			checkStore();
			
			return tileIndexStore.getFeatureSource(typeName).getQueryCapabilities();
		} catch (IOException e) {
			if(LOGGER.isLoggable(Level.INFO))
				LOGGER.log(Level.INFO,"Unable to collect QueryCapabilities",e);
			return null;
		}finally{
			lock.unlock();
		}	
	}

}

