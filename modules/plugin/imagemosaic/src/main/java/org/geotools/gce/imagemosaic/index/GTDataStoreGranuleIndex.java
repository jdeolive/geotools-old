package org.geotools.gce.imagemosaic.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.gce.imagemosaic.Granule;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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
				this.bbox=null;
			return super.visit(filter, data);
		}
		
	}
	private final URL indexLocation;

	private ShapefileDataStore tileIndexStore;

	private String typeName;

	private FileChannel channel;

	private FileLock lock;

	private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

	private String geometryPropertyName;

	private ReferencedEnvelope bounds;

	public GTDataStoreGranuleIndex(final URL indexLocation) {
		Utils.ensureNonNull("indexLocation",indexLocation);
		this.indexLocation=indexLocation;
		try{
			// lock the underlying file
			if (indexLocation.getProtocol().equals("file")) {

				//  Get a file channel for the file
				File file = DataUtilities.urlToFile(indexLocation);
				if(file.canWrite()){
					channel = new RandomAccessFile(file, "rw").getChannel();

					// Create a shared lock on the file.
					// This method blocks until it can retrieve the lock.
					lock = channel.lock(0, Long.MAX_VALUE, true);
				}
			}				
			// creating a store
			tileIndexStore = new ShapefileDataStore(this.indexLocation);
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Connected mosaic reader to its data store "+ indexLocation.toString());
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
			
			try {
				if (lock != null)
					// Release the lock
					lock.release();
			} catch (Throwable e2) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e2.getLocalizedMessage(), e2);
			} finally {
				lock = null;
			}

			try {
				if (channel != null)
					// Close the file
					channel.close();
			} catch (Throwable e3) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e3.getLocalizedMessage(), e3);
			} finally {
				channel = null	;
			}
			
			throw new  IllegalArgumentException(e);
		}
		
	}
	
	protected final ReadWriteLock rwLock= new ReentrantReadWriteLock(true);

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope)
	 */
	public List<SimpleFeature> findGranules(final BoundingBox envelope) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		final DefaultQuery q= new DefaultQuery(typeName);
		Filter filter = ff.bbox( ff.property( geometryPropertyName ), ReferencedEnvelope.reference(envelope) );
		q.setFilter(filter);
	    return findGranules(q);	
		
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope, com.vividsolutions.jts.index.ItemVisitor)
	 */
	public void  findGranules(final BoundingBox envelope, final GranuleIndexVisitor visitor) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		final DefaultQuery q= new DefaultQuery(typeName);
		Filter filter = ff.bbox( ff.property( geometryPropertyName ), ReferencedEnvelope.reference(envelope) );
		q.setFilter(filter);
	    findGranules(q,visitor);			
		

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
				
			
			try {
				if (lock != null)
					// Release the lock
					lock.release();
			} catch (Throwable e2) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e2.getLocalizedMessage(), e2);
			} finally {
				lock = null;
			}
	
			try {
				if (channel != null)
					// Close the file
					channel.close();
			} catch (Throwable e3) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e3.getLocalizedMessage(), e3);
			} finally {
				channel = null	;
			}			
		}finally{
			
			l.unlock();
		
		}
		
		
	}

	public int removeGranules(final Query query) {
		throw new UnsupportedOperationException("removeGranules is not supported, this ia read only index");
//		ImageMosaicUtils.ensureNonNull("query",query);
//		final Lock lock=rwLock.writeLock();
//		try{
//			//
//			lock.lock();
//		}finally{
//			lock.unlock();
//		}
//	
//		return 0;
		
	}

	public void addGranule(final Granule granule) {
		throw new UnsupportedOperationException("addGranule is not supported, this ia read only index");
//		ImageMosaicUtils.ensureNonNull("granuleMetadata",granule);
//		final Lock lock=rwLock.writeLock();
//		try{
//			lock.lock();
//			// check if the index has been cleared
//			if(index==null)
//				throw new IllegalStateException();
//			
//			// do your thing
//		}finally{
//			lock.unlock();
//		}	
//		
	}

	public void  findGranules(final Query q,final GranuleIndexVisitor visitor)
	throws IOException {
		Utils.ensureNonNull("q",q);

		FeatureIterator<SimpleFeature> it=null;
		FeatureCollection<SimpleFeatureType, SimpleFeature> features=null;
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();		
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
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  or empty, it's impossible to create an index!");
			
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
			if(it!=null)
				// closing he iterator to free some resources.
				if(features!=null)
					features.close(it);

		}
				
		
	}

	public List<SimpleFeature> findGranules(final Query q) throws IOException {
		Utils.ensureNonNull("q",q);

		FeatureIterator<SimpleFeature> it=null;
		FeatureCollection<SimpleFeatureType, SimpleFeature> features=null;
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();		
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

	public Collection<SimpleFeature> findGranules()throws IOException {
		return findGranules(getBounds());
	}

	public BoundingBox getBounds() {
		return bounds;
	}

}

