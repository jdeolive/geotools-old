package org.geotools.gce.imagemosaic.index;

import java.io.IOException;
import java.util.Collection;

import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.feature.SchemaException;
import org.geotools.feature.visitor.FeatureCalc;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public interface GranuleIndex {
	
	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
	public interface GranuleIndexVisitor{
		public void visit(final SimpleFeature index, Object o);
	}

	/**
	 * Finds the features that intersects the provided {@link BoundingBox}:
	 * 
	 * @param envelope
	 *            The {@link BoundingBox} to test for intersection.
	 * @return Collection of {@link Feature} that intersect the provided
	 *         {@link BoundingBox}.
	 * @throws IOException 
	 */
	public abstract Collection<SimpleFeature> getGranules(final BoundingBox envelope)throws IOException;
	
	public abstract Collection<SimpleFeature> getGranules(final Query q) throws IOException;

	public abstract Collection<SimpleFeature> getGranules()throws IOException;
	
	/**
	 * Finds the features that intersects the provided {@link BoundingBox}:
	 * 
	 * @param envelope
	 *            The {@link BoundingBox} to test for intersection.
	 * @return List of {@link Feature} that intersect the provided
	 *         {@link BoundingBox}.
	 * @throws IOException 
	 */
	public abstract void  getGranules(final BoundingBox envelope,final  GranuleIndexVisitor visitor) throws IOException;
	
	public abstract void  getGranules( final Query q,final GranuleIndexVisitor visitor) throws IOException;	

	public abstract void dispose();
		
	public void addGranule(final SimpleFeature granule, final Transaction transaction) throws IOException;
	
	public void addGranules(final Collection<SimpleFeature> granules, final Transaction transaction) throws IOException;
	
	public void createType(String namespace, String typeName, String typeSpec) throws IOException, SchemaException;
	
	public void createType(SimpleFeatureType featureType) throws IOException;
	
	public void createType(String identification, String typeSpec) throws SchemaException, IOException;
	
	public SimpleFeatureType getType() throws IOException;
	
	public int removeGranules(final Query query);

	public BoundingBox getBounds();
	
	public void computeAggregateFunction(final Query q,final FeatureCalc function) throws IOException;
	
	public QueryCapabilities getQueryCapabilities();
}