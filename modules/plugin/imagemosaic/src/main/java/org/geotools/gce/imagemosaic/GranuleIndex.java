package org.geotools.gce.imagemosaic;

import java.io.IOException;
import java.util.Collection;

import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

interface GranuleIndex {
	
	interface GranuleIndexVisitor{
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
	public abstract Collection<SimpleFeature> findGranules(final BoundingBox envelope)throws IOException;
	
	public abstract Collection<SimpleFeature> findGranules(final Query q) throws IOException;

	public abstract Collection<SimpleFeature> findGranules()throws IOException;;
	/**
	 * Finds the features that intersects the provided {@link BoundingBox}:
	 * 
	 * @param envelope
	 *            The {@link BoundingBox} to test for intersection.
	 * @return List of {@link Feature} that intersect the provided
	 *         {@link BoundingBox}.
	 * @throws IOException 
	 */
	public abstract void  findGranules(final BoundingBox envelope,final  GranuleIndexVisitor visitor) throws IOException;
	
	public abstract void  findGranules( final Query q,final GranuleIndexVisitor visitor) throws IOException;	

	public abstract void dispose();
	
	
	public void addGranule(final Granule granuleMetadata);
	
	public int removeGranules(final Query query);

	public BoundingBox getBounds();
}