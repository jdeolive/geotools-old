/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
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
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class MemorySpatialIndex {

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.imagemosaic");

	/** The {@link STRtree} index. */
	private final STRtree index;

	/**
	 * Constructs a {@link MemorySpatialIndex} out of a
	 * {@link FeatureCollection}.
	 * 
	 * @param features
	 */
	public MemorySpatialIndex(FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
		if (features == null) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER
						.warning("The provided FeatureCollection<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");
			throw new IllegalArgumentException(
					"The provided FeatureCollection<SimpleFeatureType, SimpleFeature> is null, it's impossible to create an index!");

		}
		final FeatureIterator<SimpleFeature> it = features.features();
		if (!it.hasNext()) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER
						.warning("The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  or empty, it's impossible to create an index!");
			throw new IllegalArgumentException(
					"The provided FeatureCollection<SimpleFeatureType, SimpleFeature>  or empty, it's impossible to create an index!");
		}
		index = new com.vividsolutions.jts.index.strtree.STRtree();
		while (it.hasNext()) {
			final SimpleFeature f = it.next();
			final Geometry g = (Geometry) f.getDefaultGeometry();
			index.insert(g.getEnvelopeInternal(), f);
		}
		// closing he iterator to free some resources.
		features.close(it);
		// force index construction --> STRTrees are build on first call to
		// query
		index.build();

	}

	/**
	 * Finds the features that intersects the provided {@link Envelope}:
	 * 
	 * @param envelope
	 *            The {@link Envelope} to test for intersection.
	 * @return List of {@link Feature} that intersect the providede
	 *         {@link Envelope}.
	 */
	public List<SimpleFeature> findFeatures(Envelope envelope) {
		return index.query(envelope);

	}

}
