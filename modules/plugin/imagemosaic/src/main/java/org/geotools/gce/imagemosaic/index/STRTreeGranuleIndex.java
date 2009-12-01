package org.geotools.gce.imagemosaic.index;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.gce.imagemosaic.index.GTDataStoreGranuleIndex.BBOXFilterExtractor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

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
@SuppressWarnings("unused")
class STRTreeGranuleIndex implements GranuleIndex {
	
	/** Logger. */
	final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(STRTreeGranuleIndex.class);

	private static class JTSIndexVisitorAdapter  implements ItemVisitor {

		private GranuleIndexVisitor adaptee;
		private Filter filter;

		/**
		 * @param indexLocation
		 */
		public JTSIndexVisitorAdapter(final GranuleIndexVisitor adaptee) {
			this(adaptee,(Query)null);
		}
		
		public JTSIndexVisitorAdapter(final GranuleIndexVisitor adaptee, Query q) {
			this.adaptee=adaptee;
			this.filter=q==null?DefaultQuery.ALL.getFilter():q.getFilter();
		}
		/**
		 * @param indexLocation
		 */
		public JTSIndexVisitorAdapter(final GranuleIndexVisitor adaptee, Filter filter) {
			this.adaptee=adaptee;
			this.filter=filter==null?DefaultQuery.ALL.getFilter():filter;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.vividsolutions.jts.index.ItemVisitor#visitItem(java.lang.Object)
		 */
		public void visitItem(Object o) {
			if(o instanceof SimpleFeature){
				final SimpleFeature f=(SimpleFeature) o;
				if(filter.evaluate(f));
				adaptee.visit(f,null);
				return;
			}
			throw new IllegalArgumentException("Unable to visit provided item"+o);

		}

	}

	private GranuleIndex originalIndex;
	
	public STRTreeGranuleIndex(final Map<String,Serializable> params, DataStoreFactorySpi spi) {
		Utils.ensureNonNull("params",params);
		try{
			originalIndex= new GTDataStoreGranuleIndex(params,false,spi);
		}
		catch (Throwable e) {
			try {
				if (originalIndex != null)
					originalIndex.dispose();
			} catch (Throwable e2) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e2.getLocalizedMessage(), e2);
			} 

			
			throw new  IllegalArgumentException(e);
		}
		
	}
	
	/** The {@link STRtree} index. */
	private SoftReference<STRtree> index= new SoftReference<STRtree>(null);

	protected final ReadWriteLock rwLock= new ReentrantReadWriteLock(true);

	/**
	 * Constructs a {@link STRTreeGranuleIndex} out of a {@link FeatureCollection}.
	 * 
	 * @param features
	 * @throws IOException
	 */
	private synchronized SpatialIndex getIndex() throws IOException {
		// check if the index has been cleared
		if(index==null)
			throw new IllegalStateException();
		
		// do your thing

		/**
		 * Comment by Stefan Krueger while patching the stuff to deal with
		 * URLs instead of Files: If it is not a URL to a file, we don't
		 * need locks, because no one can change to the index.
		 */

		STRtree tree = index.get();
		if (tree == null) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("No index exits and we create a new one.");
			createIndex();
			tree = index.get();
		} else if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Index does not need to be created...");
		
		return tree;
		

	}

	/**
	 * This method shall only be called when the <code>indexLocation</code> is of protocol <code>file:</code>
	 */
	private void createIndex() {
		
		Iterator<SimpleFeature> it=null;
		Collection<SimpleFeature> features=null;
		//
		// Load tiles informations, especially the bounds, which will be
		// reused
		//
		try{

			features = originalIndex.findGranules();
			if (features == null) 
				throw new NullPointerException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");
	
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Index Loaded");
			
			//load the feature from the shapefile and create JTS index
			it = features.iterator();
			if (!it.hasNext()) 
				throw new IllegalArgumentException(
						"The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  or empty, it's impossible to create an index!");
			
			// now build the index
			// TODO make it configurable as far the index is involved
			STRtree tree = new STRtree();
			while (it.hasNext()) {
				final SimpleFeature feature = it.next();
				final Geometry g = (Geometry) feature.getDefaultGeometry();
				tree.insert(g.getEnvelopeInternal(), feature);
			}
			
			// force index construction --> STRTrees are build on first call to
			// query
			tree.build();
			
			// save the soft reference
			index= new SoftReference<STRtree>(tree);
		}
		catch (Throwable e) {
			throw new  IllegalArgumentException(e);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope)
	 */
	@SuppressWarnings("unchecked")
	public List<SimpleFeature> findGranules(final BoundingBox envelope) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			return getIndex().query(ReferencedEnvelope.reference(envelope));
		}finally{
			lock.unlock();
		}			
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.FeatureIndex#findFeatures(com.vividsolutions.jts.geom.Envelope, com.vividsolutions.jts.index.ItemVisitor)
	 */
	public void findGranules(final BoundingBox envelope, final GranuleIndexVisitor visitor) throws IOException {
		Utils.ensureNonNull("envelope",envelope);
		Utils.ensureNonNull("visitor",visitor);
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			getIndex().query(ReferencedEnvelope.reference(envelope), new JTSIndexVisitorAdapter(visitor));
		}finally{
			lock.unlock();
		}				
		

	}

	public void dispose() {
		final Lock l=rwLock.writeLock();
		try{
			l.lock();
			if(index!=null)
				index.clear();
			 
			// original index
			originalIndex.dispose();
	
			
		}finally{
			originalIndex=null;
			index= null;
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

	@SuppressWarnings("unchecked")
	public List<SimpleFeature> findGranules(Query q) throws IOException {
		Utils.ensureNonNull("q",q);
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			
			// get filter and check bbox
			final Filter filter= q.getFilter();	
			// try to combine the index bbox with the one that may come from the query.
			ReferencedEnvelope requestedBBox=extractAndCombineBBox(filter);
			
			// load what we need to load
			final List<SimpleFeature> features= getIndex().query(requestedBBox);
			if(q.equals(DefaultQuery.ALL))
				return features;
			
			final List<SimpleFeature> retVal= new ArrayList<SimpleFeature>();
			for (Iterator<SimpleFeature> it = features.iterator();it.hasNext();)
			{
				SimpleFeature f= it.next();
				if(filter.evaluate(f))
					retVal.add(f);
			}
			return retVal;
		}finally{
			lock.unlock();
		}	
	}

	private ReferencedEnvelope extractAndCombineBBox(Filter filter) {
		// TODO extract eventual bbox from query here
		final BBOXFilterExtractor bboxExtractor = new GTDataStoreGranuleIndex.BBOXFilterExtractor();
		filter.accept(bboxExtractor, null);
		ReferencedEnvelope requestedBBox=bboxExtractor.getBBox();
		
		// add eventual bbox from the underlying index to constrain search
		if(requestedBBox!=null){
			requestedBBox=(ReferencedEnvelope) requestedBBox.intersection(ReferencedEnvelope.reference(originalIndex.getBounds()));
		}
		else
			ReferencedEnvelope.reference(originalIndex.getBounds());
		return requestedBBox;
	}

	public List<SimpleFeature> findGranules() throws IOException {
		return findGranules(this.getBounds());
	}

	public void findGranules(Query q, GranuleIndexVisitor visitor)
			throws IOException {
		Utils.ensureNonNull("q",q);
		final Lock lock=rwLock.readLock();
		try{
			lock.lock();
			
			// get filter and check bbox
			final Filter filter= q.getFilter();			
			ReferencedEnvelope requestedBBox=extractAndCombineBBox(filter);
			
			// get filter and check bbox
			getIndex().query(requestedBBox,new JTSIndexVisitorAdapter(visitor,q));
			
		}finally{
			lock.unlock();
		}	
	}

	public BoundingBox getBounds() {
		return originalIndex.getBounds();
	}

	public void addGranule(final SimpleFeature granule, final Transaction transaction) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void addGranules(final Collection<SimpleFeature> granules, final Transaction transaction)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void createType(String namespace, String typeName, String typeSpec)
			throws IOException, SchemaException {
		// TODO Auto-generated method stub
		
	}

	public void createType(SimpleFeatureType featureType) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void createType(String identification, String typeSpec)
			throws SchemaException, IOException {
		// TODO Auto-generated method stub
		
	}

	public SimpleFeatureType getType() throws IOException {
		return this.originalIndex.getType();
	}

}

