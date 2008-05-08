package org.geotools.data.store;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * FeatureCollection<SimpleFeatureType, SimpleFeature> decorator which decorates a feature collection "re-typing" 
 * its schema based on attributes specified in a query.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ReTypingFeatureCollection extends DecoratingFeatureCollection<SimpleFeatureType, SimpleFeature>
	implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

	SimpleFeatureType featureType;
    
	public ReTypingFeatureCollection ( FeatureCollection<SimpleFeatureType, SimpleFeature> delegate, SimpleFeatureType featureType ) {
		super(delegate);
		this.featureType = featureType;
	}
	
	public SimpleFeatureType getSchema() {
	    return featureType;
	}
	
	public  FeatureReader<SimpleFeatureType, SimpleFeature> reader() throws IOException {
		return new DelegateFeatureReader<SimpleFeatureType, SimpleFeature>( getSchema(), features() );
	}
	
	public FeatureIterator<SimpleFeature> features() {
		return new DelegateFeatureIterator<SimpleFeature>( this, iterator() );
	}

	public void close(FeatureIterator<SimpleFeature> close) {
		close.close();
	}

	public Iterator<SimpleFeature> iterator() {
		return new ReTypingIterator( delegate.iterator(), delegate.getSchema(), featureType );
	}
	
	public void close(Iterator close) {
		ReTypingIterator reType = (ReTypingIterator) close;
		delegate.close( reType.getDelegate() );
	}
}
